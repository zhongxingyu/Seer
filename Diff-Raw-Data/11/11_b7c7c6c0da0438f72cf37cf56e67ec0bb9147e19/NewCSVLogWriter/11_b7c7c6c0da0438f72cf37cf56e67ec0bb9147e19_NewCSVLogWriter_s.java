 // CSVLogWriter
 package org.eclipse.stem.util.loggers.views;
 
 /*******************************************************************************
  * Copyright (c) 2007,2008 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
 import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
 import org.eclipse.emf.edit.provider.IItemPropertySource;
 import org.eclipse.emf.edit.ui.provider.PropertySource;
 import org.eclipse.stem.adapters.time.TimeProvider;
 import org.eclipse.stem.core.common.CommonPackage;
 import org.eclipse.stem.core.graph.Node;
 import org.eclipse.stem.core.graph.NodeLabel;
 import org.eclipse.stem.core.model.Decorator;
 import org.eclipse.stem.core.model.STEMTime;
 import org.eclipse.stem.definitions.adapters.relativevalue.RelativeValueProviderAdapter;
 import org.eclipse.stem.diseasemodels.standard.AggregatingSIDiseaseModel;
 import org.eclipse.stem.diseasemodels.standard.DiseaseModel;
 import org.eclipse.stem.diseasemodels.standard.DiseaseModelLabel;
 import org.eclipse.stem.diseasemodels.standard.DiseaseModelLabelValue;
 import org.eclipse.stem.diseasemodels.standard.provider.StandardItemProviderAdapterFactory;
 import org.eclipse.stem.jobs.simulation.ISimulation;
 import org.eclipse.stem.util.loggers.Activator;
 
 /**
  * A log writer writing CSV files containing data for a single state of the simulation.
  * Columns contain location IDs as well as simulation time and iteration id.
  */
 
 public class NewCSVLogWriter extends LogWriter {
 	
 	private static final String DEFAULT_ID = "data";
 
 	private static final String CSV_EXT = ".csv";
 	
 	private String directoryName;
 
 	//private final static String DUBLIN_CORE_CLASS = "DublinCore";
 	
 	private static SimpleDateFormat dateFormat = new SimpleDateFormat("EEE d MMM yy", Locale.getDefault());
 	
 	/**
 	 * CSV Header Label for time column
 	 */
 	public static final String TIME_LABEL = "time";
 
 	/**
 	 * CSV Header Label for iteration column
 	 */
 	public static final String ITERATION_LABEL = "iteration";
 
 	/**
 	 * Name of all run parameter files (one per scenario folder)
 	 */
 	public static final String RUN_PARAMETER_FILE_NAME = "runparameters.csv";
 
 	private final int FLUSH_EACH_ITERATION = 50; // How often do we flush the files?
 
 	private int icount = 0;
 
 	private static StandardItemProviderAdapterFactory itemProviderFactory;
 	
 	/**
 	 * FileWriters are stored here, organized by state (e.g. S, E, I, R) and geographic level
 	 */
 	
 	private final Map<StateLevelMap, FileWriter> fileWriters;
 	
 	/**
 	 * 
 	 * @param dirName
 	 * @param simulation
 	 * @param dm
 	 */
 	public NewCSVLogWriter(final String dirName, final ISimulation simulation, DiseaseModel dm) {
 		needsHeader = true;
 		done = false;
 		icount = 0;
 		//final String currentDate = getDateTimeNumeric();
 		String uniqueID = DEFAULT_ID;
 		if (simulation != null) {
 			uniqueID = simulation.getUniqueIDString();
 			while(uniqueID==null) {
 				uniqueID = simulation.getUniqueIDString();
 			}
 		}
 		
 		String diseaseName = dm.getDiseaseName();
 		try {
 			this.directoryName = dirName + sep+uniqueID + sep + diseaseName.trim() + sep;
 		} catch(Exception e) {
 			Activator.logError("Failed ot create directory for CSV Logger", e);
 		}
 		
 		// remove illegal strings
 		directoryName= directoryName.replaceAll("\"", "");
 			
 		final File dir = new File(this.directoryName);
 
 		if ((!dir.exists()) || (!dir.isDirectory())) {
 			// create it.
 			boolean success = dir.mkdirs();
 			if (!success) {
 				Activator.logError(
 						"Failed to Create Driectory" + directoryName,
 						new IOException("Failed to Create Driectory"
 								+ directoryName));
 			}
 			
 		}
 		logRunParameters(dm);
 		fileWriters = new HashMap<StateLevelMap, FileWriter>();
 	} // CSVLogWriter
 
 
 	/**
 	 * Must implement this method to log data in run thread
 	 * 
 	 * @param rvp
 	 * 
 	 */
 	@Override
 	public void logHeader(final RelativeValueProviderAdapter rvp) {
 		/*
 		 * TODO do we need a header?
 		 */
 	}// log header
 
 	/**
 	 * logHeader New method
 	 * 
 	 * @param sim Simulation to log
 	 * @param dm Disease Model
 	 * @param nodeLevels Level information for each node in the simulation
 	 * @param timeProvider Time provider
 	 */
 	
 	public void logHeader(ISimulation sim, DiseaseModel dm, Map<Node, Integer>nodeLevels, TimeProvider timeProvider) {	
 		String dirs = this.directoryName;
 		File dir = new File(dirs);
 		if(!dir.exists()) {
 			boolean success = dir.mkdirs();
 			if(!success) {
 				Activator.logError(
 						"Failed to Create Driectory" + directoryName,
 						new IOException("Failed to Create Driectory"
 								+ directoryName));
 			}
 		}
 		// Find min/max level
 		int minLevel = Integer.MAX_VALUE;
 		int maxLevel = -1;
 		for(int level:nodeLevels.values()) {
 			if(level < minLevel)minLevel = level;
 			if(level > maxLevel)maxLevel = level;
 		}
 		try {
 			
 			// Get the first node in the canonical graph and look at the labels.
 			// We will generate one log file for each label
 			// Find the first node that has some labels and use those as representative labels
 			// for every node
 			
 			EList<NodeLabel> labels = null;
 			Iterator<Node> it = sim.getScenario().getCanonicalGraph().getNodes().values().iterator();
 			while(it.hasNext()) {
 				Node n = it.next();
				if(n.getLabels().size() > 0) {labels = n.getLabels();break;}
 			}
 			if(labels == null) {
 				// We couldn't find any labels. PANIC!
				Activator.logError("Cannot log, no labels found!", new Exception());
 			}
 			for(int i=0;i<labels.size();++i) {
 				NodeLabel label = labels.get(i);
 				
 				// We only generate log files for labels that are DiseaseModelLabels
 				if(!(label instanceof DiseaseModelLabel)) {
 					continue;
 				}
 				
 				StandardItemProviderAdapterFactory itemProviderFactory = getItemProviderFactory();
 				DiseaseModelLabel dmLabel = (DiseaseModelLabel)label;
 				DiseaseModelLabelValue dmlv = dmLabel.getCurrentDiseaseModelLabelValue();
 				IItemPropertySource propertySource = (IItemPropertySource) itemProviderFactory
 				.adapt(dmlv, PropertySource.class);
 				if(propertySource == null) continue;
 				final List<IItemPropertyDescriptor> properties = propertySource
 					.getPropertyDescriptors(null);
 				
 				Decorator dec = dmLabel.getDecorator();
 				
 				if(dec == null) {
 					Activator.logError("Found a null decorator for label: "+label, new Exception());
 					continue;
 				}
 				
 				if(!dec.equals(dm) && !(dec instanceof AggregatingSIDiseaseModel)) { // ToDo: Why would getDecorator() for a label return the aggregating model? If there are more than 1 disease model, it should return all
 					continue;
 				}
 				for(IItemPropertyDescriptor property:properties) {	
 					for(int level = minLevel;level <=maxLevel;++level) {
 						StateLevelMap slm = new StateLevelMap(property.getDisplayName(property), level);
 						String file = dirs+sep+property.getDisplayName(property)+"_"+level+CSV_EXT;
 						FileWriter fw = new FileWriter(file);
 						fileWriters.put(slm, fw);
 						Iterator<Node> nodeIterator = this.getNodeIterator(level, nodeLevels);
 						// Iterate over nodes for a given resolution
 						fw.write(ITERATION_LABEL);
 						fw.write(",");
 						fw.write(TIME_LABEL);
 						fw.write(",");
 						while(nodeIterator.hasNext()) {
 							Node node = nodeIterator.next();
 							String id = this.filterLocationId(node.getURI().toString());
 							fw.write(id);
 							if(nodeIterator.hasNext()) fw.write(",");
 						}
 						fw.write("\n");
 					}
 				}
 			}
 					
 			needsHeader = false;
 		} catch(IOException ioe) {
 			Activator.logError("Error writing log header ", ioe);
 		}
 	}
 	
 	/**
 	 * Creates a log file for run parameters. Only one of these is created per
 	 * diseaseFolder
 	 * 
 	 * @param dm Disease Model
 	 */
 	
 	public void logRunParameters(DiseaseModel dm) {
 		if (dm == null) { // check
 			return;
 		}
 		
 		try {
 			final FileWriter fwp = new FileWriter(directoryName
 					+ RUN_PARAMETER_FILE_NAME);
 
 			
 			final ComposedAdapterFactory itemProviderFactory = new ComposedAdapterFactory(
 					ComposedAdapterFactory.Descriptor.Registry.INSTANCE);
 
 			final IItemPropertySource propertySource = (IItemPropertySource) itemProviderFactory
 					.adapt(dm, IItemPropertySource.class);
 			
 			final List<IItemPropertyDescriptor> properties = propertySource
 				.getPropertyDescriptors(dm);
 		
 			StringBuilder header = new StringBuilder();
 			StringBuilder values = new StringBuilder();
 			
 			for (int i=0;i<properties.size();++i) {
 				IItemPropertyDescriptor descriptor = properties.get(i);
 				final EStructuralFeature feature = (EStructuralFeature) descriptor
 						.getFeature(null);
 				
 				EClass containingClass = feature.getEContainingClass();
 				if(containingClass.equals(CommonPackage.eINSTANCE.getDublinCore())) continue;
 				
 				Object value = dm.eGet(feature);
 				header.append(feature.getName());
 				header.append(",");
 				value = value.toString().replace(',', ' ');
 				value = value.toString().replace('\n', ' ');
 				values.append(value.toString().replace(',', ' '));
 				values.append(",");
 			}
 			header.setLength(header.length()-1); // drop last ,
 			values.setLength(values.length()-1); // drop last ,
 			fwp.write(header.toString());
 			fwp.write("\n");
 			fwp.write(values.toString());
 			fwp.flush();
 			fwp.close();
 		} catch (final IOException e) {
 			// debugging for now
 			Activator.logError("Error creating file writer for RUNPARAMETER ", e );
 
 		}
 		 
 	}// logRunParameters
 
 	/**
 	 * Must implement this method to log data in run thread
 	 * Need to implement TimeProviderAdapterFactory.INSTANCE.adapt().....only Graph will work right now....later in CORE
 	 * @param rvp
 	 * @param timeProvider
 	 */
 	@Override
 	public void logData(final RelativeValueProviderAdapter rvp, final TimeProvider timeProvider) {
 		// Not used
 	}// log data
 
 	/**
 	 * Log data from from a simulation after cycle
 	 * 
 	 * @param sim Simulation
 	 * @param dm DiseaseModel
 	 * @param nodeLevels Node levels
 	 * @param timeProvider
 	 * @param beforeStart
 	 */
 
 	@SuppressWarnings("boxing")
 	@Override
 	public void logData(ISimulation sim, DiseaseModel dm, Map<Node, Integer>nodeLevels, TimeProvider timeProvider, boolean beforeStart) {
 		
 		// Increment iteration count
 		this.icount++;
 
 		try {
 			// Iterate over the map resolutions
 			Iterator<Integer> resolution_it = this.getResolutionIterator(nodeLevels);
 			while(resolution_it.hasNext()) {
 				int resolution = resolution_it.next();
 
 				Iterator<Node> nodeIterator = this.getNodeIterator(resolution, nodeLevels);
 				boolean firstNode = true;
 				// Iterate over nodes for a given resolution
 				while(nodeIterator.hasNext()) {
 					Node n = nodeIterator.next();
 					EList<NodeLabel> labels = n.getLabels();
 					for(int i=0;i<labels.size();++i) {
 						NodeLabel label = labels.get(i);
 						
 						if(!(label instanceof DiseaseModelLabel)) {
 							continue;
 						}
 						
 						StandardItemProviderAdapterFactory itemProviderFactory = getItemProviderFactory();
 						DiseaseModelLabel dmLabel = (DiseaseModelLabel)label;
 						// The current value
 						DiseaseModelLabelValue dmlv = dmLabel.getCurrentDiseaseModelLabelValue();
 						IItemPropertySource propertySource = (IItemPropertySource) itemProviderFactory
 						.adapt(dmlv, PropertySource.class);
 						if(propertySource == null) continue;
 						final List<IItemPropertyDescriptor> properties = propertySource
 							.getPropertyDescriptors(null);
 						
 						//final RelativeValueProviderAdapter rvp = (RelativeValueProviderAdapter) RelativeValueProviderAdapterFactory.INSTANCE
 						//	.adapt(label, RelativeValueProvider.class);
 						//if(rvp == null) continue;
 						
 						Decorator dec = dmLabel.getDecorator();
 						
 						if(dec == null) {
 							Activator.logError("Found a null decorator for label: "+label, new Exception());
 							continue;
 						}
 						
 						if(!dec.equals(dm) && !(dec instanceof AggregatingSIDiseaseModel)) { // ToDo: Why would getDecorator() for a label return the aggregating model? If there are more than 1 disease model, it should return all
 							continue;
 						}
 						
 						StringBuilder sb = new StringBuilder();
 						for(IItemPropertyDescriptor itemDescriptor:properties) {
 								sb.setLength(0);
 								StateLevelMap slm = new StateLevelMap(itemDescriptor.getDisplayName(itemDescriptor), resolution);
 								FileWriter fw = fileWriters.get(slm);
 								if(fw == null) {
 									Activator.logError("Error, no file writer found for "+slm, null);
 									continue;
 								}
 								// Before writing the values for the first location,
 								// add iteration, time columns
 									
 								if(firstNode) {
 									sb.append(this.icount);
 									sb.append(",");
 									STEMTime time = timeProvider.getTime();
 									if(time == null) {
 										time = sim.getScenario().getSequencer().getStartTime();
 									}
 									if(!beforeStart)
 										// Add increment
 										time = time.addIncrement(sim.getScenario().getSequencer().getTimeDelta());
 									
 									String timeString;
 									timeString = dateFormat.format(time.getTime());
 									
 									sb.append(timeString);
 									sb.append(",");
 								}
 									
 								EStructuralFeature feature = (EStructuralFeature) itemDescriptor
 									.getFeature(null);
 								
 								double value = ((Double) dmlv.eGet(feature))
 									.doubleValue();
 								
 								// Write relative value for the property
 								// ItemPropertyDescriptor property = (ItemPropertyDescriptor)itemDescriptor;
 								// double value = rvp.getRelativeValue(property)*rvp.getDenominator(); // log absolute value, not relative
 								
 								sb.append(value);
 								if(nodeIterator.hasNext()) sb.append(",");
 								else sb.append("\n"); // new line
 								fw.write(sb.toString());
 							} // For each property in label
 						} // For each node label							
 					firstNode = false;
 					} // For each node
 				} //  For each node resolution
 			needsHeader = false;
 		} catch(IOException ioe) {
 			Activator.logError("Error writing log header ", ioe);
 		}
 		
 		if(icount % FLUSH_EACH_ITERATION == 0) this.flushLoggerData();
 	}
 	
 	/*
 	 * private Graph getGraph(final Node node) { return (Graph)
 	 * node.eContainer().eContainer(); } // getGraph
 	 */
 
 	/**
 	 * Flush and Close the file
 	 */
 	@Override
 	public void flushLoggerData() {
 		Collection<FileWriter> writers = fileWriters.values();
 		for(FileWriter writer:writers) {
 			try {
 				writer.flush();
 			} catch(IOException ioe) {
 				Activator.logError("Cannot flush log file", ioe);
 			}
 		}
 		
 	} // flush All Data
 	
 	/**
 	 * Flush and Close the file
 	 */
 	@Override
 	public void closeLoggerData() {
 		Collection<FileWriter> writers = fileWriters.values();
 		for(FileWriter writer:writers) {
 			try {
 				writer.close();
 			} catch(IOException ioe) {
 				Activator.logError("Cannot close log file", ioe);
 			}
 		}
 	} // closeLoggerData
 
 	@SuppressWarnings("boxing")
 	private Iterator<Integer> getResolutionIterator(Map<Node, Integer>nodeLevels) {
 		ArrayList<Integer> list = new ArrayList();
 		Collection<Integer>vals = nodeLevels.values();
 		for(int level:vals) {
 			if(!list.contains(level)) list.add(level);
 		}
 		return list.iterator();
 	}
 	
 	// Return all nodes at the same level
 	// Sort by the name alphabetically for convenience
 	
 	@SuppressWarnings("boxing")
 	private Iterator<Node> getNodeIterator(int level, Map<Node, Integer>nodeLevels) {
 		ArrayList<Node> list = new ArrayList();
 		Set<Node>ns = nodeLevels.keySet();
 		for(Node n:ns) {
 			if(nodeLevels.get(n) == level) list.add(n);
 		}
 		Collections.sort(list, new Comparator<Node>() {
 
 			public int compare(Node n1, Node n2) {
 				String s1 = n1.getURI().toString();
 				String s2 = n2.getURI().toString();
 				return s1.compareTo(s2);
 			}
 			
 		});
 		return list.iterator();
 	}
 	
 	/**
 	 * filters the location id prefix from the beginning of
 	 *  a nodes ID for generation of a file name
 	 * @param unfiltered
 	 * @return the filtered file name using location id.
 	 */
 	private String filterLocationId(String unfiltered) {
 		int last = unfiltered.indexOf(LOCATIONID_PREFIX);
 		if (last >=0) {
 			last += LOCATIONID_PREFIX.length();
 			return unfiltered.substring(last, unfiltered.length());
 		} 
 		return unfiltered;
 	}
 	
 	/**
 	 * Used as key in map with FileWriters. FileWriters are key'd by
 	 * the label (i.e. disease state) and by 
 	 */
 	private class StateLevelMap {
 		private final String state;
 		private final int level;
 		
 		public StateLevelMap(String state, int level) {
 			this.state = state;
 			this.level = level;
 		}
 		
 		public int getLevel() {return this.level;}
 		public String getState() {return this.state;}
 		
 		@Override
 		public int hashCode() {
 			return state.hashCode() + level; // ugh
 		}
 		
 		@Override
 		public boolean equals(Object o) {
 			if(!(o instanceof StateLevelMap)) return false;
 			StateLevelMap slm = (StateLevelMap)o;
 			return (slm.getState().equals(this.state) && slm.getLevel() == level);
 		}
 		
 		@Override
 		public String toString() {
 			return this.state+"_"+this.level;
 		}
 	}
 
 	/**
 	 * @return the instance of the Standard Item Provider factory.
 	 */
 	private static StandardItemProviderAdapterFactory getItemProviderFactory() {
 		if (itemProviderFactory == null) {
 			itemProviderFactory = new StandardItemProviderAdapterFactory();
 		}
 		return itemProviderFactory;
 	} // getItemProviderFactory
 }
