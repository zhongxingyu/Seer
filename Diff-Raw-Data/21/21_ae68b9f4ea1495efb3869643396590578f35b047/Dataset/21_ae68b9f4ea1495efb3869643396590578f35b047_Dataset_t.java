 /*******************************************************************************
  * Copyright (c) 2012 Emanuele.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/gpl.html
  * 
  * Contributors:
  *     Emanuele - initial API and implementation
  ******************************************************************************/
 package game.core;
 
 import game.configuration.Configurable;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 public class Dataset extends Configurable implements Iterable<Instance> {
 	
 	private static final int COMMITCYCLE = 100;
 	
 	static {
 		try {
 			Class.forName("org.sqlite.JDBC");
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 			System.exit(0);
 		}
 	}
 	
 	private static final Map<String, Connection> connectionRegistry = new HashMap<>();
 	
 	private Connection connection = null;
 	
 	public InstanceTemplate template;
 	
 	public String databaseCacheFile = null;
 	
 	public boolean ready = false;
 	
 	public List<Integer> indices = new ArrayList<>();
 	
 	public Dataset() {
 		setAsInternalOptions("databaseCacheFile", "indices", "ready", "template");
 	}
 	
 	public Dataset(InstanceTemplate template, String cacheFileName) {
 		this();
 		this.template = template;
 		createDatabaseCacheFile(cacheFileName);
 	}
 	
	public Dataset(Dataset base, List<Integer> indices) {
 		this();
 		this.template = base.template;
 		this.connection = base.connection;
 		this.databaseCacheFile = base.databaseCacheFile;
 		this.indices = new ArrayList<>(indices);
 		this.ready = true;
 	}
 	
 	public int size() {
 		return indices.size();
 	}
 	
 	private void createDatabaseCacheFile(String cacheFileName) {
 		cacheFileName = getAvailableCacheFile(cacheFileName);
 		databaseCacheFile = cacheFileName + ".db";
 		try {
 			connection = DriverManager.getConnection("jdbc:sqlite:"+databaseCacheFile);
 			connection.setAutoCommit(false);
 			Statement statement = connection.createStatement();
 			statement.executeUpdate("CREATE TABLE Data (id INTEGER PRIMARY KEY AUTOINCREMENT, content BLOB NOT NULL);");
 			statement.close();
 			connectionRegistry.put(databaseCacheFile, connection);
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private String getAvailableCacheFile(String cacheFileName) {
 		final String suffix = ".cache_";
 		if (!new File(cacheFileName + ".db").exists())
 			return cacheFileName;
 		
 		if (cacheFileName.matches(suffix + "\\d+$")) {
 			cacheFileName = cacheFileName.substring(0, cacheFileName.indexOf(suffix));
 		}
 		int number = 0;
 		while(new File(cacheFileName + suffix + number + ".db").exists())
 			number++;
 		return cacheFileName + suffix + number;
 	}
 
 	public void setDatabaseCacheFile(String databaseCacheFile) {
 		this.databaseCacheFile = databaseCacheFile;
 		
 		if (connection != null)
 			return;
 		
 		if (connectionRegistry.containsKey(databaseCacheFile)) {
 			connection = connectionRegistry.get(databaseCacheFile);
 		} else {
 			try {
 				connection = DriverManager.getConnection("jdbc:sqlite:"+databaseCacheFile);
 				connection.setAutoCommit(false);
 				Statement statement = connection.createStatement();
 				ResultSet res = statement.executeQuery("SELECT COUNT(*) AS count FROM Data;");
 				res.next();
 				int count = res.getInt("count");
 				for(int i = 0; i < count; i++)
 					indices.add(i);
 				statement.close();
 				connectionRegistry.put(databaseCacheFile, connection);
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	public void add(Instance instance) {
 		if (ready)
 			return; // Cannot add instances once a dataset is ready
 		try {
 			ByteArrayOutputStream baos = new ByteArrayOutputStream();
 		    ObjectOutputStream oout = new ObjectOutputStream(baos);
 		    oout.writeObject(template.serialize(instance));
 		    oout.close();
 		    
 			PreparedStatement statement = connection.prepareStatement("INSERT INTO Data(id, content) VALUES (?, ?);");
 			statement.setInt(1, indices.size());
 			statement.setBytes(2, baos.toByteArray());
 			statement.executeUpdate();
 			indices.add(indices.size());
 			if (indices.size() % COMMITCYCLE == 0)
 				connection.commit();
 		} catch (SQLException | IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
	public void addIndex(int index) {
 		if (ready)
 			return;
 		
 		indices.add(index);
 	}
 	
 	public void setReadyState() {
 		try {
 			connection.commit();
 			ready = true;
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public class InstanceIterator implements Iterator<Instance> {
 		
 		private Iterator<Integer> indexIterator;
 		private int currentId;
 
 		private InstanceIterator() {
 			indexIterator = indices.iterator();
 		}
 		
 		@Override
 		public boolean hasNext() {
 			return indexIterator.hasNext();
 		}
 
 		@Override
 		public Instance next() {
 			Instance ret = null;
 			try {
 				Statement statement = connection.createStatement();
 				currentId = indexIterator.next();
 				ResultSet result = statement.executeQuery("SELECT content FROM Data WHERE id = " + currentId + ";");
 				result.next();
 				
 				ByteArrayInputStream bais = new ByteArrayInputStream(result.getBytes("content"));
 				ObjectInputStream oin = new ObjectInputStream(bais);
 			    ret = template.deserialize(oin.readObject());
 
 				statement.close();
 			} catch (SQLException | IOException | ClassNotFoundException e) {
 				e.printStackTrace();
 			}
 			return ret;
 		}
 		
 		public int getCurrentIndex() {
 			return currentId;
 		}
 
 		@Override
 		public void remove() {
 			throw new UnsupportedOperationException("Cannot remove instances from dataset");
 		}
 
 	}
 	
 	public enum SampleType {
 		EVERYTHING, IN_OUT, IN_ENC, IN_OUT_PRED, IN_OUT_ENC
 	}
 	
 	public class SampleIterator implements Iterator<Sample> {
 		
 		private SampleType type;
 		private Block inputEncoder;
 		private Block outputEncoder;
 		private InstanceIterator instanceIterator = instanceIterator();
 		private List currentInputSequence;
 		private List currentOutputSequence;
 		private List currentPredictionSequence;
 		private Encoding currentInputEncoding;
 		private Encoding currentOutputEncoding;
 		private Encoding currentPredictionEncoding;
 		private int indexInInstance;
 		
 		private SampleIterator(SampleType type) {
 			if (type == SampleType.IN_OUT_ENC || type == SampleType.EVERYTHING)
 				throw new UnsupportedOperationException("Cannot use a sample iterator with encoding if you don't specify the encoders");
 			this.type = type;
 			prepareForNextInstance();
 		}
 		
 		public SampleIterator(SampleType type, Block inputEncoder, Block outputEncoder) {
 			this.type = type;
 			this.inputEncoder = inputEncoder;
 			this.outputEncoder = outputEncoder;
 			prepareForNextInstance();
 		}
 		
 		public void reset() {
 			instanceIterator = instanceIterator();
 			prepareForNextInstance();
 		}
 		
 		private void prepareForNextInstance() {
 			Instance inst = instanceIterator.next();
 			currentInputSequence = inst.getInput();
 			currentOutputSequence = inst.getOutput();
 			if (inst.getPrediction() != null)
 				currentPredictionSequence = inst.getPrediction();
 			if (inputEncoder != null && outputEncoder != null) {
 				currentInputEncoding = inputEncoder.transform(inst.getInput());
 				currentOutputEncoding = outputEncoder.transform(inst.getOutput());
 			}
 			currentPredictionEncoding = inst.getPredictionEncoding();
 			indexInInstance = 0;
 		}
 
 		@Override
 		public boolean hasNext() {
 			return instanceIterator.hasNext() ||
 					indexInInstance < currentInputSequence.size();
 		}
 
 		@Override
 		public Sample next() {
 			if (indexInInstance == currentInputSequence.size()) {
 				prepareForNextInstance();
 			}
 			
 			Sample ret = null;
 			switch(type) {
 			case EVERYTHING:
 				ret = new Sample(
 						currentInputSequence.get(indexInInstance),
 						currentInputEncoding.getElement(indexInInstance),
 						currentOutputSequence.get(indexInInstance),
 						currentOutputEncoding.getElement(indexInInstance),
 						currentPredictionSequence.get(indexInInstance),
 						currentPredictionEncoding.getElement(indexInInstance));
 				break;
 			case IN_OUT:
 				ret = new Sample(
 						currentInputSequence.get(indexInInstance),
 						currentOutputSequence.get(indexInInstance));
 				break;
 			case IN_OUT_PRED:
 				ret = new Sample(
 						currentInputSequence.get(indexInInstance),
 						currentOutputSequence.get(indexInInstance),
 						currentPredictionSequence.get(indexInInstance));
 				break;
 			case IN_OUT_ENC:
 				ret = new Sample(
 						currentInputSequence.get(indexInInstance),
 						currentInputEncoding.getElement(indexInInstance),
 						currentOutputSequence.get(indexInInstance),
 						currentOutputEncoding.getElement(indexInInstance));
 				break;
 			default:
 				break;
 			}
 			
 			indexInInstance++;
 			return ret;
 		}
 		
 		public Block getOutputEncoder() {
 			return outputEncoder;
 		}
 
 		@Override
 		public void remove() {
 			throw new UnsupportedOperationException("Cannot remove samples from dataset");
 		}
 		
 	}
 
 	public InstanceIterator instanceIterator() {
 		if (!ready)
 			return null;
 		
 		return new InstanceIterator();
 	}
 	
 	public SampleIterator sampleIterator(boolean includePrediction) {
 		if (!ready)
 			return null;
 		
 		if (includePrediction)
 			return new SampleIterator(SampleType.IN_OUT_PRED);
 		else
 			return new SampleIterator(SampleType.IN_OUT);
 	}
 	
 	public SampleIterator encodedSampleIterator(Block inputEncoder, Block outputEncoder, boolean includePrediction) {
 		if (!ready)
 			return null;
 		
 		if (includePrediction)
 			return new SampleIterator(SampleType.EVERYTHING, inputEncoder, outputEncoder);
 		else
 			return new SampleIterator(SampleType.IN_OUT_ENC, inputEncoder, outputEncoder);
 	}
 	
 	public List<Dataset> getFolds(int folds, boolean random) {
 		if (!ready)
 			return null;
 		
 		List<Dataset> ret = new ArrayList<>(folds);
 		List<Integer> temp = new ArrayList<>(indices);
 		if (random)
 			Collections.shuffle(temp);
 		int foldSize = indices.size() / folds;
 		for(int fold = 0; fold < folds; fold++) {
 			ret.add(new Dataset(this, temp.subList(fold*foldSize, (fold+1)*foldSize)));
 		}
 		return ret;
 	}
 	
 	public List<Dataset> getComplementaryFolds(List<Dataset> folds) {
 		if (!ready)
 			return null;
 		
 		List<Dataset> ret = new ArrayList<>(folds.size());
 		List<Integer> temp = new ArrayList<>(indices);
 		for(Dataset fold: folds) {
 			List<Integer> complement = new ArrayList<>(temp);
 			complement.removeAll(fold.indices);
 			ret.add(new Dataset(this, complement));
 		}
 		return ret;
 	}
 	
 	public Dataset getRandomSubset(double percent) {
 		if (!ready)
 			return null;
 		
 		assert(percent > 0 && percent <= 1);
 		List<Integer> temp = new ArrayList<>(indices);
 		Collections.shuffle(temp);
 		return new Dataset(this, temp.subList(0, (int)(percent*temp.size())));
 	}
 
 	@Override
 	public Iterator<Instance> iterator() {
 		return instanceIterator();
 	}
 	
 }
