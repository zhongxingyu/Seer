 package org.amanzi.awe.afp.exporters;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.amanzi.awe.afp.filters.AfpRowFilter;
 import org.amanzi.awe.afp.models.AfpFrequencyDomainModel;
 import org.amanzi.awe.afp.models.AfpModel;
 import org.amanzi.awe.afp.models.AfpModelUtils;
 import org.amanzi.awe.console.AweConsolePlugin;
 import org.amanzi.neo.services.INeoConstants;
 import org.amanzi.neo.services.enums.DatasetRelationshipTypes;
 import org.amanzi.neo.services.enums.NetworkRelationshipTypes;
 import org.amanzi.neo.services.enums.NodeTypes;
 import org.amanzi.neo.services.node2node.NodeToNodeTypes;
 import org.amanzi.neo.services.node2node.NodeToNodeRelationService.NodeToNodeRelationshipTypes;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.neo4j.graphdb.Direction;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Relationship;
 import org.neo4j.graphdb.RelationshipType;
 import org.neo4j.graphdb.ReturnableEvaluator;
 import org.neo4j.graphdb.StopEvaluator;
 import org.neo4j.graphdb.TraversalPosition;
 import org.neo4j.graphdb.Traverser;
 import org.neo4j.graphdb.Traverser.Order;
 
 /**
  * Writes the data from the neo4j database to external file
  * 
  * @author Rahul
  *
  */
 
 
 public class AfpExporter extends Job{
 	private Node afpRoot;
 	private Node afpDataset;
 	
 	protected static final String AMANZI_STR = ".amanzi";
 	private static final String DATA_SAVER_DIR = "AfpTemp";
 	public static final String tmpAfpFolder = getTmpFolderPath();
 	public static final String PATH_SEPARATOR = "/";
 	
 	public static final int CONTROL = 0;
 	public static final int CELL = 1;
 	public static final int INTERFERENCE = 2;
 	public static final int NEIGHBOUR = 3;
 	public static final int FORBIDDEN = 4;
 	public static final int EXCEPTION = 5;
 	public static final int CLIQUES = 6;
 	
 	/** The Control File*/
 	public final String[] fileNames = {
 			"InputControlFile.awe"
 			, "InputCellFile.awe"
 			,"InputInterferenceFile.awe"
 			,"InputNeighboursFile.awe"
 			,"InputForbiddenFile.awe"
 			,"InputExceptionFile.awe"
 			,"InputCliquesFile.awe"
 	};
 	
 	public String[] domainDirPaths;	
 	
 	public final String logFileName = "logfile.awe";
 	public final String outputFileName = "outputFile.awe";
 	
 	private int maxTRX = -1;
 	private File[] files;
 	private File[][] inputFiles;
 	private AfpModel model;
 	AfpFrequencyDomainModel models[];
 	
 	public static final int NEIGH = 0;
 	public static final int INTERFER = 1;
 	public static final int TRIANGULATION = 2;
 	public static final int SHADOWING = 3;
 	
 	public static final int CoA = 0;
 	public static final int AdA = 1;
 	public static final int CoT = 2;
 	public static final int AdT = 3;
 	
 	public static final float CO_SITE_SCALING_FACTOR = 1;
 	public static final float CO_SECTOR_SCALING_FACTOR = 1;
 	
 	// default values of the Control file 
 	int defaultGMaxRTperCell = 1;
 	int defaultSiteSpacing = 2;
 	int defaultCellSpacing = 0;
 	int defaultRegNbrSpacing=1;
 	int defaultMinNbrSpacing =0;
 	int defaultSecondNbrSpacing = 1;
 	int defaultRecalculateAll=1;
 	int defaultUseTraffic=1;
 	int defaultUseSONbrs=0;
 	int defaultQuality=100;
 	int defaultDecomposeInCliques=0;
 	int defaultExistCliques=0;
 	int defaultHoppingType=0;
 	int defaultUseGrouping=0;
 	int defaultNrOfGroups=1;
 	
 	static int count;
 	
 	public AfpExporter(Node afpRoot, Node afpDataset, AfpModel model){
 		super("Write Input files");
 		this.afpRoot = afpRoot;
 		this.afpDataset = afpDataset;
 		this.model = model;
 		
 	}
 	
 
 	@Override
 	public IStatus run(IProgressMonitor monitor) {
 		createFiles();
 		writeFilesNew(monitor);
 		return Status.OK_STATUS;
 	}
 	
 	private void createFiles(){
 		createTmpFolder();
 		
 		
 		models = model.getFreqDomains(false).toArray(new AfpFrequencyDomainModel[0]);
 		inputFiles = new File[models.length][fileNames.length];
 		domainDirPaths = new String[models.length];
 		for(int i = 0; i < models.length; i++){
 			String dirName = models[i].getName();
 			
 			try {
 				File modelDir = new File(tmpAfpFolder + dirName);
 				if (!modelDir.exists())
 					modelDir.mkdir();
 				domainDirPaths[i] = modelDir.getAbsolutePath() + PATH_SEPARATOR;
 				for (int j = 0; j < fileNames.length; j++){
 					inputFiles[i][j] = new File(tmpAfpFolder + dirName + PATH_SEPARATOR + fileNames[j]);
 					inputFiles[i][j].createNewFile();
 				}
 			} catch (IOException e) {
 				AweConsolePlugin.exception(e);
 			}
 		}
 		
 	}	
 	
 	
 	
 	
 	
 	
 	public void writeFilesNew(IProgressMonitor monitor){
 		
 		monitor.beginTask("Write Files", model.getTotalTRX());
 		Traverser sectorTraverser = model.getTRXList(null);
 		
 		try {
 			
 			BufferedWriter[] cellWriters = new BufferedWriter[models.length];
 			BufferedWriter[] intWriters = new BufferedWriter[models.length];
 			
 			for(int i = 0; i < models.length; i++){
 				cellWriters[i] = new BufferedWriter(new FileWriter(inputFiles[i][CELL]));
 				intWriters[i] = new BufferedWriter(new FileWriter(inputFiles[i][INTERFERENCE]));
 			}
 	    
 		    for (Node sectorNode : sectorTraverser) {
 		    	HashMap<Node,String[][]> sectorIntValues = getSectorInterferenceValues(sectorNode);
 		    	Traverser trxTraverser = AfpModelUtils.getTrxTraverser(sectorNode);
 	
 		    	for (Node trxNode: trxTraverser){
 		    		count++;
 		    		monitor.worked(1);
 		    		if (count %100 == 0)
 		    			AweConsolePlugin.info(count + " trxs processed");
 		    		for (int i = 0; i < models.length; i++){
 		    			AfpFrequencyDomainModel mod = models[i];
 			    		String filterString = mod.getFilters();
 			    		if (filterString != null && !filterString.trim().isEmpty()){
 				    		AfpRowFilter rf = AfpRowFilter.getFilter(mod.getFilters());
 				    		if (rf != null){
 					    		if (rf.equal(trxNode)){
 					    			ArrayList<Integer> freq = new ArrayList<Integer>();
 					    			StringBuilder sb = new StringBuilder();
 					    			sb.append(Long.toString(trxNode.getId()));
 					    			sb.append(" ");
 					    			
 //					    			String trxNo = (String)trxNode.getProperty(INeoConstants.PROPERTY_NAME_NAME, "0");
 //									 if (Character.isLetter(trxNo.charAt(0))){
 //										 trxNo = Integer.toString(Character.getNumericValue(trxNo.charAt(0)) - Character.getNumericValue('A')+ 1);
 //									 }
 					    			
 					    			
 					    			
 					    			for (Node plan : trxNode.traverse(Order.DEPTH_FIRST, StopEvaluator.DEPTH_ONE, new ReturnableEvaluator(){
 
 										@Override
 										public boolean isReturnableNode(TraversalPosition pos) {
 											if (pos.currentNode().getProperty(INeoConstants.PROPERTY_NAME_NAME, "").equals("original"))
 												return true;
 											if(pos.currentNode().getProperty(INeoConstants.AFP_PROPERTY_ORIGINAL_NAME, true).equals(true))
 												return true;
 											return false;
 										}
 					    				
 					    			}, DatasetRelationshipTypes.PLAN_ENTRY, Direction.OUTGOING)){
 										 try{
 											 Integer[] frequencies = (Integer[])plan.getProperty("arfcn", new Integer[0]);
 											 for(Integer f : frequencies)
 												 freq.add(f);
 										 }catch (ClassCastException e){
 											 int[] frequencies = (int[])plan.getProperty("arfcn", new int[0]);
 											 for(int f : frequencies)
 												 freq.add(f);
 										 }
 										 
 										 
 									}
 					    			
 					    			Integer[] freqArray = freq.toArray(new Integer[0]);
 					    			if (freqArray.length > 1){
 					    				for (int j = 0; j < freqArray.length; j++){
 					    					sb.append(1 + "-");//add trxid as 1 always
 					    					sb.append(j);
 							    			sb.append(" ");
 							    			sb.append(1);//non-relevant
 							    			sb.append(" ");
 							    			sb.append(1);//required
 							    			sb.append(" ");
 							    			sb.append(1);//given
 											sb.append(" " + freqArray[i]);//required frequencies
 											sb.append("\n");
 							    			cellWriters[i].write(sb.toString());
 										}	
 					    			}
 					    			
 					    			else{
 					    				sb.append(1);
 						    			sb.append(" ");
 						    			sb.append(1);//non-relevant
 						    			sb.append(" ");
 						    			sb.append(1);//required
 						    			sb.append(" ");
 						    			sb.append(1);//given
 										sb.append(" " + freqArray[0]);//required frequencies
 										sb.append("\n");
 						    			cellWriters[i].write(sb.toString());
 									}
 					    			
 					    			writeInterferenceForTrx(sectorNode, trxNode, intWriters[i], sectorIntValues, rf);
 					    			
 					    			
 					    		}
 				    		}
 			    		}
 			    	}
 			    	
 		    	
 		    	}
 		    
 		    }
 		    
 		    //close the writers and create control files
 		    for (int i = 0; i < models.length; i++){
 		    	cellWriters[i].close();
 		    	intWriters[i].close();
 		    	createControlFile(i);
 		    }
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 	}
 	
 	private void writeInterferenceForTrx(Node sector, Node trx, BufferedWriter intWriter, HashMap<Node,String[][]> sectorIntValues, AfpRowFilter rf) throws IOException{
 		
 		DecimalFormat df = new DecimalFormat("0.0000000000");
 		StringBuilder trxSb = new StringBuilder();
 		trxSb.append("SUBCELL 0 0 1 1 ");
 		int numberofinterferers = 0;
 		StringBuilder sbAllInt = new StringBuilder();
 		
 		for(Node intSector : sectorIntValues.keySet()){
 			
 			for (Node intTrx : intSector.traverse(Order.DEPTH_FIRST, StopEvaluator.DEPTH_ONE, new ReturnableEvaluator(){
 
 				@Override
 				public boolean isReturnableNode(TraversalPosition pos) {
 					if (pos.currentNode().getProperty(INeoConstants.PROPERTY_TYPE_NAME).equals(NodeTypes.TRX.getId()))
 						return true;
 					
 					return false;
 				}
 			}, NetworkRelationshipTypes.CHILD, Direction.OUTGOING)){
 				String trxId = (String)trx.getProperty(INeoConstants.PROPERTY_NAME_NAME, "0");
 				if (sector.equals(intSector) && trxId.equals((String)trx.getProperty(INeoConstants.PROPERTY_NAME_NAME, "0")))
 					continue;
 	    		if (rf != null){
 		    		if (!(rf.equal(intTrx))){
 		    			continue;
 		    		}
 	    		}
 				
 				
 //				char c = trxId.charAt(0);
 //				 if (Character.isDigit(c)){
 //					 c = (char)((c- '1') + 'A');
 //				 }
 				StringBuilder sbSubCell = new StringBuilder();
 				sbSubCell.append("INT 0\t0\t");
 //				String[] values = sectorIntValues.get(intSector)[1];
 				float[] trxValues = calculateInterference(trx, intTrx, sectorIntValues.get(intSector));
 				for (int i = 0; i < trxValues.length; i++){
 					sbSubCell.append(df.format(trxValues[i]) + " ");
 				}
 				sbSubCell.append(intTrx.getId());
 				sbSubCell.append("A");
 				sbAllInt.append(sbSubCell);
 				sbAllInt.append("\n");
 				numberofinterferers++;
 			}
 			
 		}
 		
 		String trxId = (String)trx.getProperty(INeoConstants.PROPERTY_NAME_NAME, "0");
 //		char c = trxId.charAt(0);
 //		 if (Character.isDigit(c)){
 //			 c = (char)((c- '1') + 'A');
 //		 }
 
 		trxSb.append(numberofinterferers);
 		trxSb.append(" ");
 		trxSb.append(trx.getId());
 		trxSb.append("A");
 		trxSb.append("\n");
 		if(numberofinterferers >0) {
 			intWriter.write(trxSb.toString());
 			intWriter.write(sbAllInt.toString());
 		}
 		
 		
 	}
 	
 
 	private float[] calculateInterference(Node trx1, Node trx2, String[][] values){
 		Node sector1 = trx1.getSingleRelationship(NetworkRelationshipTypes.CHILD, Direction.INCOMING).getStartNode();
 		Node sector2 = trx2.getSingleRelationship(NetworkRelationshipTypes.CHILD, Direction.INCOMING).getStartNode();
 		Node site1 = sector1.getSingleRelationship(NetworkRelationshipTypes.CHILD, Direction.INCOMING).getStartNode();
 		Node site2 = sector2.getSingleRelationship(NetworkRelationshipTypes.CHILD, Direction.INCOMING).getStartNode();
 		float[] calculatedValues = new float[4];
 		boolean isBCCH1 = false;
 		boolean isHopping1 = false;
 		boolean isBCCH2 = false;
 		boolean isHopping2 = false;
 		int index = 0;
 		if ((Boolean)trx1.getProperty(INeoConstants.PROPERTY_BCCH_NAME))
 			isBCCH1 = true;
 		if ((Integer)trx1.getProperty(INeoConstants.PROPERTY_HOPPING_TYPE_NAME) >= 1)
 			isHopping1 = true;
 		
 		if ((Boolean)trx2.getProperty(INeoConstants.PROPERTY_BCCH_NAME))
 			isBCCH2 = true;
 		if ((Integer)trx2.getProperty(INeoConstants.PROPERTY_HOPPING_TYPE_NAME) >= 1)
 			isHopping2 = true;
 		
 		if (isBCCH1){
 			if (isBCCH2)
 				index = AfpModel.BCCHBCCH;
 			else index = isHopping2 ? AfpModel.BCCHSFH : AfpModel.BCCHNHBB; 
 		}
 		else if (isHopping1){
 			if (isBCCH2)
 				index = AfpModel.SFHBCCH;
 			else index = isHopping2 ? AfpModel.SFHSFH : AfpModel.SFHNHBB; 
 		}
 		else{
 			if (isBCCH2)
 				index = AfpModel.NHBBBCCH;
 			else index = isHopping2 ? AfpModel.NHBBSFH : AfpModel.NHBBNHBB; 
 		}
 		
 		for (int j = 0; j < values[0].length; j++){
 			//CoA
 			float val = 0;
 			for (int i = 0; i < values.length; i++){
 				try{
 					val = Float.parseFloat(values[i][j]);
 				}
 				catch(Exception e){
 					val = 0;
 				}
 				float scalingFactor = 0;
 				
 				if (j ==CoA || j == CoT){
 					if (i == NEIGH){
 						scalingFactor = model.coNeighbor[index] / 100;
 					}
 					else if (i == INTERFER){
 						scalingFactor = model.coInterference[index] / 100;
 					}
 					else if (i == TRIANGULATION){
 						scalingFactor = model.coTriangulation[index] / 100;
 					}
 					else if (i == SHADOWING){
 						scalingFactor = model.coShadowing[index] / 100;
 					}
 				}
 				else if (j ==AdA || j == AdT){
 					if (i == NEIGH){
 						scalingFactor = model.adjNeighbor[index] / 100;
 					}
 					else if (i == INTERFER){
 						scalingFactor = model.adjInterference[index] / 100;
 					}
 					else if (i == TRIANGULATION){
 						scalingFactor = model.adjTriangulation[index] / 100;
 					}
 					else if (i == SHADOWING){
 						scalingFactor = model.adjShadowing[index] / 100;
 					}
 				}
 					
 				calculatedValues[j] += val*scalingFactor;
 			}
 			
 			
 			//co-site
 			if (site1.equals(site2)){
 				calculatedValues[j] += CO_SITE_SCALING_FACTOR * model.siteSeparation[index] / 100; 
 			}
 			if (sector1.equals(sector2)){
 				calculatedValues[j] += CO_SECTOR_SCALING_FACTOR * model.sectorSeparation[index] / 100; 
 			}
 		}
 		
 		
 		return calculatedValues;
 		
 		
 	}
 	
 	
 	public HashMap<Node,String[][]> getSectorInterferenceValues(Node sector){
 		
 		
 		DecimalFormat df = new DecimalFormat("0.0000000000");
 		
 		//values in 2-D array for each interfering node
 		//array[neighbourArray, intArray, TriArray, shadowArray]
 		//neighbourArray[CoA, AdjA, CoT, AdjT]
 		HashMap<Node, String[][]> intValues = new HashMap<Node, String[][]>();
 		
 		//Add this sector to calculate co-sector TRXs
 		String[][] coSectorTrxValues = new String[][]{
 				{Float.toString(1),Float.toString(1),Float.toString(1),Float.toString(1)},
 				{},{},{}};
 		intValues.put(sector, coSectorTrxValues);
 		
 		for (Node proxySector : sector.traverse(Order.DEPTH_FIRST, StopEvaluator.DEPTH_ONE, new ReturnableEvaluator(){
 
 			@Override
 			public boolean isReturnableNode(TraversalPosition pos) {
 				if (pos.currentNode().getProperty(INeoConstants.PROPERTY_TYPE_NAME).equals(NodeTypes.SECTOR_SECTOR_RELATIONS.getId()) ||
 						pos.currentNode().getProperty(INeoConstants.PROPERTY_TYPE_NAME).equals(NodeTypes.PROXY.getId()))
 					return true;
 				
 				return false;
 			}
 		}, NetworkRelationshipTypes.INTERFERENCE, Direction.OUTGOING, NetworkRelationshipTypes.NEIGHBOURS, Direction.OUTGOING,
 		DatasetRelationshipTypes.PROXY, Direction.OUTGOING)){
 			for (Relationship relation : proxySector.getRelationships(NetworkRelationshipTypes.INTERFERS, NetworkRelationshipTypes.NEIGHBOUR, NodeToNodeRelationshipTypes.PROXYS)){
 				if (relation.getEndNode().equals(proxySector))
 					continue;
 				Node intProxySector = relation.getEndNode();
 				
				Relationship relationship = null;
				relationship = intProxySector.getSingleRelationship(DatasetRelationshipTypes.PROXY, Direction.INCOMING);
				if(relationship == null){
					relationship = intProxySector.getSingleRelationship(NetworkRelationshipTypes.INTERFERENCE, Direction.INCOMING);
				}
				if(relationship == null){
					relationship = intProxySector.getSingleRelationship(NetworkRelationshipTypes.NEIGHBOURS, Direction.INCOMING);
 				}
				if(relationship == null) {
					continue;
 				}
				Node intSector = null;
				intSector = relationship.getStartNode();
 				RelationshipType type = relation.getType();
 				boolean isProxy = false;
 				int typeIndex = NEIGH;
 				if (type.equals(NodeToNodeRelationshipTypes.PROXYS)){
 					isProxy = true;
 					Node fileNode = intProxySector.getSingleRelationship(NetworkRelationshipTypes.CHILD, Direction.INCOMING).getStartNode();
 					if (fileNode.getProperty("node2node", "").equals(NodeToNodeTypes.NEIGHBOURS))
 						typeIndex = NEIGH;
 					else if (fileNode.getProperty("node2node", "").equals(NodeToNodeTypes.INTERFERENCE_MATRIX))
 						typeIndex = INTERFER;
 					else if (fileNode.getProperty("node2node", "").equals(NodeToNodeTypes.TRIANGULATION))
 						typeIndex = TRIANGULATION;
 					else if (fileNode.getProperty("node2node", "").equals(NodeToNodeTypes.SHADOWING))
 						typeIndex = SHADOWING;
 				}
 				else if (type.equals(NetworkRelationshipTypes.NEIGHBOUR))
 					typeIndex = NEIGH;
 				else if (type.equals(NetworkRelationshipTypes.INTERFERS))
 					typeIndex = INTERFER;
 				
 				String[][] prevValue = new String[4][4];
 				if (intValues.containsKey(intSector))
 					prevValue = intValues.get(intSector);
 				
 				String[] value = new String[4];
 				
 				if (isProxy){
 					try {
 						value[CoA] = df.format(relation.getProperty("co", "0")).toString();
 					} catch (Exception e) {
 						value[CoA] = (String)relation.getProperty("co", "0");
 					}
 					
 					try {
 						value[AdA] = df.format(relation.getProperty("adj", "0")).toString();
 					} catch (Exception e) {
 						value[AdA] = (String)relation.getProperty("adj", "0");
 					}
 					
 					try {
 						value[CoT] = df.format(relation.getProperty("coT", "0")).toString();
 					} catch (Exception e) {
 						value[CoT] = (String)relation.getProperty("coT", "0");
 					}
 					
 					try {
 						value[AdT] = df.format(relation.getProperty("adjT", "0")).toString();
 					} catch (Exception e) {
 						value[AdT] = (String)relation.getProperty("adjT", "0");
 					}
 				}
 				else if (typeIndex == INTERFER){
 					try {
 						value[CoA] = df.format(relation.getProperty("CoA", "0")).toString();
 					} catch (Exception e) {
 						value[CoA] = (String)relation.getProperty("CoA", "0");
 					}
 					
 					try {
 						value[AdA] = df.format(relation.getProperty("AdA", "0")).toString();
 					} catch (Exception e) {
 						value[AdA] = (String)relation.getProperty("AdA", "0");
 					}
 					
 					try {
 						value[CoT] = df.format(relation.getProperty("CoT", "0")).toString();
 					} catch (Exception e) {
 						value[CoT] = (String)relation.getProperty("CoT", "0");
 					}
 					
 					try {
 						value[AdT] = df.format(relation.getProperty("AdT", "0")).toString();
 					} catch (Exception e) {
 						value[AdT] = (String)relation.getProperty("AdT", "0");
 					}
 				}//end if
 				else if(typeIndex == NEIGH){
 					value[CoA] = Double.toString(0.5);
 					value[AdA] = Double.toString(0.05);
 					value[CoT] = Double.toString(0.5);
 					value[AdT] = Double.toString(0.05);
 				}
 				
 				prevValue[typeIndex] = value;
 				
 				intValues.put(intSector, prevValue);
 				
 			}
 		}
 		
 		
 
 		
 		return intValues;
 			
 	}
 	
 	
 	
 	
 	
 	
 	
 	/**
 	 * Creates the Control file to be given as input to the C++ engine
 	 */
 	public void createControlFile(int domainIndex){
 		if (maxTRX < 0) {
 			maxTRX = defaultGMaxRTperCell;
 		}
 		
 		try {
 			BufferedWriter writer  = new BufferedWriter(new FileWriter( inputFiles[domainIndex][CONTROL]));
 			
 			
 			writer.write("SiteSpacing " + defaultSiteSpacing);
 			writer.newLine();
 			
 			writer.write("CellSpacing " + defaultCellSpacing);
 			writer.newLine();
 			
 			writer.write("RegNbrSpacing " + defaultRegNbrSpacing);
 			writer.newLine();
 
 			writer.write("MinNbrSpacing " + defaultMinNbrSpacing);
 			writer.newLine();
 
 			writer.write("SecondNbrSpacing " + defaultSecondNbrSpacing);
 			writer.newLine();
 
 			writer.write("RecalculateAll " + defaultRecalculateAll);
 			writer.newLine();
 
 			writer.write("UseTraffic " + defaultUseTraffic);
 			writer.newLine();
 
 			writer.write("UseSONbrs " + defaultUseSONbrs);
 			writer.newLine();
 
 			writer.write("Quality " + defaultQuality);
 			writer.newLine();
 
 			writer.write("DecomposeInCliques " + defaultDecomposeInCliques);
 			writer.newLine();
 
 			writer.write("ExistCliques " + defaultExistCliques);
 			writer.newLine();
 
 			writer.write("GMaxRTperCell " + maxTRX);
 			writer.newLine();
 
 			writer.write("GMaxRTperSite " + maxTRX);
 			writer.newLine();
 
 			writer.write("HoppingType " + defaultHoppingType);
 			writer.newLine();
 			
 			writer.write("UseGrouping " + defaultUseGrouping);
 			writer.newLine();
 
 			writer.write("NrOfGroups " + defaultNrOfGroups);
 			writer.newLine();
 			
 			writer.write("LogFile " + "\"" + this.domainDirPaths[domainIndex] + this.logFileName + "\"");
 			writer.newLine();
 			
 			// TBD count TRXs
 			writer.write("CellCardinality " + "0");
 			writer.newLine();
 			
 			writer.write("CellFile " + "\"" + this.inputFiles[domainIndex][CELL].getAbsolutePath() + "\"");
 			writer.newLine();
 			
 			writer.write("NeighboursFile " + "\"" + this.inputFiles[domainIndex][NEIGHBOUR].getAbsolutePath() + "\"");
 			writer.newLine();
 			
 			writer.write("InterferenceFile " + "\"" + this.inputFiles[domainIndex][INTERFERENCE].getAbsolutePath() + "\"");
 			writer.newLine();
 
 			writer.write("OutputFile " + "\"" + this.domainDirPaths[domainIndex] + this.outputFileName + "\"");
 			writer.newLine();
 			
 			writer.write("CliquesFile " + "\"" + this.inputFiles[domainIndex][CLIQUES].getAbsolutePath() + "\"");
 			writer.newLine();
 
 			writer.write("ForbiddenFile " + "\"" + this.inputFiles[domainIndex][FORBIDDEN].getAbsolutePath() + "\"");
 			writer.newLine();
 			
 			writer.write("ExceptionFile " + "\"" + this.inputFiles[domainIndex][EXCEPTION].getAbsolutePath() + "\"");
 			writer.newLine();
 			
 			writer.write("Carriers " + parseCarriers(getFrequencies(domainIndex)));
 			writer.newLine();
 
 			writer.close();
 		}catch (Exception e){
 			AweConsolePlugin.exception(e);
 		}
 	}
 	
 	private String getFrequencies(int domainIndex) {
     	StringBuffer carriers = new StringBuffer();
     	int cnt =0;
 		boolean first = true;
 		String[] franges = models[domainIndex].getFrequencies();
     			
 		String[] freqList = AfpModel.rangeArraytoArray(franges);
 		for(String f: freqList) {
 			if(!first) {
 				carriers.append(",");
 			}
 			carriers.append(f);
 			cnt++;
 			first = false;
 		}
 		return carriers.toString();
 	}
 	
 	private String parseCarriers(String commaSeparated){
 		int numCarriers = commaSeparated.split("\\,").length;
 		String spaceSeparated = commaSeparated.replaceAll(",", " ");
 		spaceSeparated = numCarriers + " " + spaceSeparated;
 		
 		return spaceSeparated;
 	}
 	
 	
 	
 	/**
 	 * Gets the site name and sector no of the sector
 	 * @param sector the sector node
 	 * @return string array containg site name and sector no
 	 */
 	public String[] parseSectorName(Node sector){
 		Node site = sector.getSingleRelationship(NetworkRelationshipTypes.CHILD, Direction.INCOMING).getOtherNode(sector);
 		String siteName = site.getProperty(INeoConstants.PROPERTY_NAME_NAME).toString();
 		String sectorValues[] = new String[2];
 		sectorValues[0] = siteName;
 		String sectorName = sector.getProperty(INeoConstants.PROPERTY_NAME_NAME).toString();
 		if (sectorName.length() > siteName.length() && sectorName.substring(0, siteName.length()).equals(siteName)){
 			sectorValues[1] = sector.getProperty(INeoConstants.PROPERTY_NAME_NAME).toString().substring(siteName.length());
 		}
 		else sectorValues[1] = sectorName;
 		
 		char sectorNo = sectorValues[1].charAt(sectorValues[1].length() - 1);
 		if (Character.isLetter(sectorNo))
 			sectorValues[1] = //sectorValues[1].substring(0, sectorValues[1].length() - 1) + 
 								Integer.toString(Character.getNumericValue(sectorNo) - Character.getNumericValue('A')+ 1);
 			
 		return sectorValues;
 	}
 	public String getSectorNameForInterList(Node sector){
 		Node site = sector.getSingleRelationship(NetworkRelationshipTypes.CHILD, Direction.INCOMING).getOtherNode(sector);
 		String siteName = site.getProperty(INeoConstants.PROPERTY_NAME_NAME).toString();
 
 		String sectorName = sector.getProperty(INeoConstants.PROPERTY_NAME_NAME).toString();
 		if (sectorName.length() > siteName.length() && sectorName.substring(0, siteName.length()).equals(siteName)){
 			sectorName = sector.getProperty(INeoConstants.PROPERTY_NAME_NAME).toString().substring(siteName.length());
 		}
 		char sectorNo = sectorName.charAt(sectorName.length() - 1);
 		if (Character.isDigit(sectorNo))
 			sectorName = siteName + sectorNo;
 		else
 			sectorName = siteName + (Character.getNumericValue(sectorNo) - Character.getNumericValue('A')+ 1);
 		
 		return sectorName;
 	}
 	
 	
 	public String[] getAllTrxNames(Node sector){
 		ArrayList<String> names = new ArrayList<String>();
 		for (Node trx : sector.traverse(Order.DEPTH_FIRST, StopEvaluator.END_OF_GRAPH, new ReturnableEvaluator(){
 
 			@Override
 			public boolean isReturnableNode(TraversalPosition pos) {
 				if (pos.currentNode().getProperty(INeoConstants.PROPERTY_TYPE_NAME).equals(NodeTypes.TRX.getId()))
 					return true;
 				
 				return false;
 			}
 		}, NetworkRelationshipTypes.CHILD, Direction.OUTGOING)){
 			String name = (String)trx.getProperty(INeoConstants.PROPERTY_NAME_NAME, "");
 			if (Character.isDigit(name.charAt(0))){
 //				name = Integer.toString(Character.getNumericValue(name.charAt(0)) - Character.getNumericValue('A')+ 1);
 				name = Character.toString((char)(name.charAt(0) + 'A' - '1'));
 //				name = Integer.toString();
 			}
 			names.add(name);
 			
 		}
 		return names.toArray(new String[0]);
 	}
 	
 	
 	
 	private void createTmpFolder(){
 		File file = new File(this.tmpAfpFolder);
 		if (!file.exists())
 			file.mkdir();
 	}
 	
 	
 	public static String getTmpFolderPath(){
 		
 		File dir = new File(System.getProperty("user.home"));
         if (!dir.exists()) {
             dir.mkdir();
         }
         dir = new File(dir, AMANZI_STR);
         if (!dir.exists()) {
             dir.mkdir();
         }
         dir = new File(dir, DATA_SAVER_DIR);
         if (!dir.exists()) {
             dir.mkdir();
         }
 		
 		return dir.getPath() + PATH_SEPARATOR;
 	}
 
 
 }
