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
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.TableItem;
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
 
 
 public class AfpExporter {
 	private Node afpRoot;
 	private Node afpDataset;
 	
 	protected static final String AMANZI_STR = ".amanzi";
 	private static final String DATA_SAVER_DIR = "AfpTemp";
 	public final String tmpAfpFolder = getTmpFolderPath();
 	
 	public static final int CONTROL = 0;
 	public static final int CELL = 1;
 	public static final int NEIGHBOUR = 2;
 	public static final int INTERFERENCE = 3;
 	public static final int FORBIDDEN = 4;
 	public static final int EXCEPTION = 5;
 	public static final int CLIQUES = 6;
 	
 	/** The Control File*/
 	public final String[] fileNames = {tmpAfpFolder + "InputControlFile.awe",
 			tmpAfpFolder + "InputCellFile.awe",
 			tmpAfpFolder + "InputNeighboursFile.awe",
 			tmpAfpFolder + "InputInterferenceFile.awe",
 			tmpAfpFolder + "InputForbiddenFile.awe",
 			tmpAfpFolder + "InputExceptionFile.awe",
 			tmpAfpFolder + "InputCliquesFile.awe"};
 	
 	
 	public final String paramFileName = tmpAfpFolder + "param.awe";
 	public final String logFileName = tmpAfpFolder + "logfile.awe";
 	public final String outputFileName = tmpAfpFolder + "outputFile.awe";
 	
 	private int maxTRX = -1;
 	private File[] files;
 	private AfpModel model;
 	
 	public File[] cellFilesArray;
 	private ArrayList<File> cellFiles = new ArrayList<File>();
 	public File[] interferenceFiles;
 	public File[] outputFiles;
 	
 	public static final int NEIGH = 0;
 	public static final int INTERFER = 1;
 	public static final int TRIANGULATION = 2;
 	public static final int SHADOWING = 3;
 	
 	public static final int CoA = 0;
 	public static final int AdA = 1;
 	public static final int CoT = 2;
 	public static final int AdT = 3;
 	
 	static int count;
 	
 	public AfpExporter(Node afpRoot, Node afpDataset, AfpModel model){
 		this.afpRoot = afpRoot;
 		this.afpDataset = afpDataset;
 		this.model = model;
 		createFiles();
 	}
 	
 	public void createFiles(){
 		createTmpFolder();
 		files = new File[fileNames.length];
 		
 		for (int i = 0; i < files.length; i++){
 			files[i] = new File(fileNames[i]);
 			try {
 				files[i].createNewFile();
 			} catch (IOException e) {
 				AweConsolePlugin.exception(e);
 			}
 		}
 	}	
 	
 	public void createCarrierFile(){
 		
 		try{
 			BufferedWriter writer  = new BufferedWriter(new FileWriter(files[CELL]));
 			
 			for (Node sector : afpRoot.traverse(Order.DEPTH_FIRST, StopEvaluator.END_OF_GRAPH, new ReturnableEvaluator(){
 
 				@Override
 				public boolean isReturnableNode(TraversalPosition pos) {
 					if (pos.currentNode().getProperty(INeoConstants.PROPERTY_TYPE_NAME).equals(NodeTypes.SECTOR.getId()))
 						return true;
 					
 					return false;
 				}
 			}, NetworkRelationshipTypes.CHILD, Direction.OUTGOING)){
 				String sectorValues[] = parseSectorName(sector);
 				
 				for (Node trx : sector.traverse(Order.DEPTH_FIRST, StopEvaluator.END_OF_GRAPH, new ReturnableEvaluator(){
 
 					@Override
 					public boolean isReturnableNode(TraversalPosition pos) {
 						if (pos.currentNode().getProperty(INeoConstants.PROPERTY_TYPE_NAME).equals(NodeTypes.TRX.getId()))
 							return true;
 						
 						return false;
 					}
 				}, NetworkRelationshipTypes.CHILD, Direction.OUTGOING)){
 					ArrayList<Integer> freq = new ArrayList<Integer>();
 					String trxNo = (String)trx.getProperty(INeoConstants.PROPERTY_NAME_NAME, "0");
 					 if (Character.isLetter(trxNo.charAt(0))){
 						 trxNo = Integer.toString(Character.getNumericValue(trxNo.charAt(0)) - Character.getNumericValue('A')+ 1);
 					 }
 					 
 					 for (Node plan : trx.traverse(Order.DEPTH_FIRST, StopEvaluator.DEPTH_ONE, ReturnableEvaluator.ALL_BUT_START_NODE, DatasetRelationshipTypes.PLAN_ENTRY, Direction.OUTGOING)){
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
 					 
 					 if (freqArray.length > maxTRX){
 						 maxTRX = freqArray.length; 
 					 }
 					
 					
 					//write values to cell file
 					writer.write(sectorValues[0]);//siteNmae
 					writer.write(sectorValues[1]);//sectorNo
 					writer.write(" " + trxNo);//trxNo
 					writer.write(" " + 1);//non-relevant
 					if (freqArray.length == 0)
 						 writer.write(" " + 1);//number of frequencies required
 					 else
 						 writer.write(" " + freqArray.length);//number of frequencies required
 					 writer.write(" " + freqArray.length);//number of frequencies given
 					 for (Integer frequency : freqArray){
 						 writer.write(" " + frequency);//required frequencies
 					 }
 					 writer.newLine();
 				}
 				
 				
 			}
 			
 			writer.close();
 		}
 		catch(Exception e){
 			AweConsolePlugin.exception(e);
 		}
 	}
 	
 	
 	
 	
	public void writeFilesNew(){
		
 		
 		Traverser sectorTraverser = model.getTRXList(null);
 //		BufferedWriter writer;
 		
 		try {
 			
 			AfpFrequencyDomainModel models[] = model.getFreqDomains(false).toArray(new AfpFrequencyDomainModel[0]);
 			cellFilesArray = new File[models.length];
 			interferenceFiles = new File[models.length];
 			outputFiles = new File[models.length];
 			BufferedWriter[] writers = new BufferedWriter[models.length];
 			BufferedWriter[] intWriters = new BufferedWriter[models.length];
 			BufferedWriter[] outWriters = new BufferedWriter[models.length];
 			
 			for(int i = 0; i < models.length; i++){
 				cellFilesArray[i] = new File(tmpAfpFolder + "Cell_" + models[i].getName() + ".awe");
 				interferenceFiles[i] = new File(tmpAfpFolder + "Int_" + models[i].getName() + ".awe");
 				writers[i] = new BufferedWriter(new FileWriter(cellFilesArray[i]));
 				intWriters[i] = new BufferedWriter(new FileWriter(interferenceFiles[i]));
 //				outWriters[i] = new BufferedWriter(new FileWriter(outputFiles[i]));
 			}
 	    
 		    for (Node sectorNode : sectorTraverser) {
 		    	HashMap<Node,String[][]> sectorIntValues = getSectorInterferenceValues(sectorNode);
 		    	Traverser trxTraverser = AfpModelUtils.getTrxTraverser(sectorNode);
 	
 		    	for (Node trxNode: trxTraverser){
 		    		count++;
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
 					    			sb.append(Long.toString(sectorNode.getId()));
 					    			sb.append(" ");
 					    			
 					    			String trxNo = (String)trxNode.getProperty(INeoConstants.PROPERTY_NAME_NAME, "0");
 									 if (Character.isLetter(trxNo.charAt(0))){
 										 trxNo = Integer.toString(Character.getNumericValue(trxNo.charAt(0)) - Character.getNumericValue('A')+ 1);
 									 }
 					    			
 					    			
 					    			
 					    			for (Node plan : trxNode.traverse(Order.DEPTH_FIRST, StopEvaluator.DEPTH_ONE, new ReturnableEvaluator(){
 
 										@Override
 										public boolean isReturnableNode(TraversalPosition pos) {
 											if (pos.currentNode().getProperty(INeoConstants.PROPERTY_NAME_NAME, "").equals("original"))
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
 					    					sb.append(trxNo + "-");
 					    					sb.append(j);
 							    			sb.append(" ");
 							    			sb.append(1);//non-relevant
 							    			sb.append(" ");
 							    			sb.append(1);//required
 							    			sb.append(" ");
 							    			sb.append(1);//given
 											sb.append(" " + freqArray[i]);//required frequencies
 											sb.append("\n");
 							    			writers[i].write(sb.toString());
 										}	
 					    			}
 					    			
 					    			else{
 					    				sb.append(trxNo);
 						    			sb.append(" ");
 						    			sb.append(1);//non-relevant
 						    			sb.append(" ");
 						    			sb.append(1);//required
 						    			sb.append(" ");
 						    			sb.append(1);//given
 										sb.append(" " + freqArray[0]);//required frequencies
 										sb.append("\n");
 						    			writers[i].write(sb.toString());
 									}
 					    			
					    			writeInterferenceForTrx(sectorNode, trxNode, intWriters[i], sectorIntValues);
 					    			
 					    			
 					    		}
 				    		}
 			    		}
 			    	}
 			    	
 		    	
 		    	}
 		    
 		    }
 		    
 		    for (int i = 0; i < writers.length; i++){
 		    	writers[i].close();
 		    	intWriters[i].close();
 		    }
 		
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		
 		
 		
 		
 	}
 	
	private void writeInterferenceForTrx(Node sector, Node trx, BufferedWriter intWriter, HashMap<Node,String[][]> sectorIntValues) throws IOException{
 		
 		StringBuilder trxSb = new StringBuilder();
 		trxSb.append("SUBCELL 0 0 1 1 ");
 		int numberofinterferers = 0;
 		StringBuilder sbAllInt = new StringBuilder();
 		
 		for(Node intSector : sectorIntValues.keySet()){
 			
			for (Node intTrx : sector.traverse(Order.DEPTH_FIRST, StopEvaluator.DEPTH_ONE, new ReturnableEvaluator(){
 
 				@Override
 				public boolean isReturnableNode(TraversalPosition pos) {
 					if (pos.currentNode().getProperty(INeoConstants.PROPERTY_TYPE_NAME).equals(NodeTypes.TRX.getId()))
 						return true;
 					
 					return false;
 				}
 			}, NetworkRelationshipTypes.CHILD, Direction.OUTGOING)){
 				String trxId = (String)trx.getProperty(INeoConstants.PROPERTY_NAME_NAME, "0");
 				char c = trxId.charAt(0);
 				 if (Character.isDigit(c)){
 					 c = (char)((c- '1') + 'A');
 				 }
 				StringBuilder sbSubCell = new StringBuilder();
 				sbSubCell.append("INT 0\t0\t");
 //				String[] values = sectorIntValues.get(intSector)[1];
 				float[] trxValues = calculateInterference(trx, intTrx, sectorIntValues.get(intSector));
 				for (int i = 0; i < trxValues.length; i++)
 					sbSubCell.append(trxValues[i] + " ");
 				sbSubCell.append(intSector.getId());
 				sbSubCell.append(c);
 				sbAllInt.append(sbSubCell);
 				sbAllInt.append("\n");
 				numberofinterferers++;
 			}
 			
 		}
 		
 		String trxId = (String)trx.getProperty(INeoConstants.PROPERTY_NAME_NAME, "0");
 		char c = trxId.charAt(0);
 		 if (Character.isDigit(c)){
 			 c = (char)((c- '1') + 'A');
 		 }
 
 		trxSb.append(numberofinterferers);
 		trxSb.append(" ");
 		trxSb.append(sector.getId());
 		trxSb.append(c);
 		trxSb.append("\n");
 		if(numberofinterferers >0) {
 			intWriter.write(trxSb.toString());
 			intWriter.write(sbAllInt.toString());
 		}
 		
 		
 	}
 	
 
 	private float[] calculateInterference(Node trx1, Node trx2, String[][] values){
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
 						scalingFactor = model.coNeighbor[index];
 					}
 					else if (i == INTERFER){
 						scalingFactor = model.coInterference[index];
 					}
 					else if (i == TRIANGULATION){
 						scalingFactor = model.coTriangulation[index];
 					}
 					else if (i == SHADOWING){
 						scalingFactor = model.coShadowing[index];
 					}
 				}
 				else if (j ==AdA || j == AdT){
 					if (i == NEIGH){
 						scalingFactor = model.adjNeighbor[index];
 					}
 					else if (i == INTERFER){
 						scalingFactor = model.adjInterference[index];
 					}
 					else if (i == TRIANGULATION){
 						scalingFactor = model.adjTriangulation[index];
 					}
 					else if (i == SHADOWING){
 						scalingFactor = model.adjShadowing[index];
 					}
 				}
 				
 				
 					
 				scalingFactor = model.coNeighbor[index];
 				calculatedValues[j] += val*scalingFactor;
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
 		for (Node proxySector : sector.traverse(Order.DEPTH_FIRST, StopEvaluator.DEPTH_ONE, new ReturnableEvaluator(){
 
 			@Override
 			public boolean isReturnableNode(TraversalPosition pos) {
 				if (pos.currentNode().getProperty(INeoConstants.PROPERTY_TYPE_NAME).equals(NodeTypes.SECTOR_SECTOR_RELATIONS.getId()))
 					return true;
 				
 				return false;
 			}
 		}, NetworkRelationshipTypes.INTERFERENCE, Direction.OUTGOING, NetworkRelationshipTypes.NEIGHBOURS, Direction.OUTGOING)){
 			for (Relationship relation : proxySector.getRelationships(NetworkRelationshipTypes.INTERFERS, NetworkRelationshipTypes.NEIGHBOURS)){
 				if (relation.getEndNode().equals(proxySector))
 					continue;
 				Node intSector;
 				Node intProxySector = relation.getEndNode();
 				if (intProxySector.hasRelationship(NetworkRelationshipTypes.INTERFERENCE, Direction.INCOMING)){
 					intSector = intProxySector.getSingleRelationship(NetworkRelationshipTypes.INTERFERENCE, Direction.INCOMING).getStartNode();
 				}
 				else {
 					intSector = intProxySector.getSingleRelationship(NetworkRelationshipTypes.NEIGHBOURS, Direction.INCOMING).getStartNode();
 				}
 				RelationshipType type = relation.getType();
 				int typeIndex = NEIGH;
 				if (type.equals(NetworkRelationshipTypes.INTERFERS))
 					typeIndex = INTERFER;
 				
 				String[][] prevValue = new String[4][4];
 				if (intValues.containsKey(intSector))
 					prevValue = intValues.get(intSector);
 				
 				String[] value = new String[4]; 
 				if (typeIndex == INTERFER){
 					try {
 						value[CoA] = df.format(relation.getProperty("CoA")).toString();
 					} catch (Exception e) {
 						value[CoA] = (String)relation.getProperty("CoA");
 					}
 					
 					try {
 						value[AdA] = df.format(relation.getProperty("AdA")).toString();
 					} catch (Exception e) {
 						value[AdA] = (String)relation.getProperty("AdA");
 					}
 					
 					try {
 						value[CoT] = df.format(relation.getProperty("CoT")).toString();
 					} catch (Exception e) {
 						value[CoT] = (String)relation.getProperty("CoT");
 					}
 					
 					try {
 						value[AdT] = df.format(relation.getProperty("AdT")).toString();
 					} catch (Exception e) {
 						value[AdT] = (String)relation.getProperty("AdT");
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
 	
 	
 	
 	
 	
 	public void writeFiles(){
 		
 		try{
 			BufferedWriter carrierWriter  = new BufferedWriter(new FileWriter(files[CELL]));
 			BufferedWriter intWriter  = new BufferedWriter(new FileWriter(files[INTERFERENCE]));
 			StringBuilder carrierSB = new StringBuilder();
 			StringBuilder intSB = new StringBuilder();
 			for (Node sector : afpRoot.traverse(Order.DEPTH_FIRST, StopEvaluator.END_OF_GRAPH, new ReturnableEvaluator(){
 
 				@Override
 				public boolean isReturnableNode(TraversalPosition pos) {
 					if (pos.currentNode().getProperty(INeoConstants.PROPERTY_TYPE_NAME).equals(NodeTypes.SECTOR.getId()))
 						return true;
 					
 					return false;
 				}
 			}, NetworkRelationshipTypes.CHILD, Direction.OUTGOING)){
 				String sectorValues[] = parseSectorName(sector);
 				carrierSB.append(sectorValues[0]);
 				carrierSB.append(sectorValues[1]);
 				String sectorName = getSectorNameForInterList(sector);
 				
 								
 				
 				
 				for (Node trx : sector.traverse(Order.DEPTH_FIRST, StopEvaluator.DEPTH_ONE, new ReturnableEvaluator(){
 
 					@Override
 					public boolean isReturnableNode(TraversalPosition pos) {
 						if (pos.currentNode().getProperty(INeoConstants.PROPERTY_TYPE_NAME).equals(NodeTypes.TRX.getId()))
 							return true;
 						
 						return false;
 					}
 				}, NetworkRelationshipTypes.CHILD, Direction.OUTGOING)){
 					
 					StringBuilder sbCell = new StringBuilder();
 					sbCell.append("SUBCELL 0 0 1 1 ");
 					int numberofinterferers =0;
 					StringBuilder sbAllInt = new StringBuilder();
 					
 					ArrayList<Integer> freq = new ArrayList<Integer>();
 					String trxNo = (String)trx.getProperty(INeoConstants.PROPERTY_NAME_NAME, "0");
 					String trxName = trxNo;
 					 if (Character.isLetter(trxNo.charAt(0))){
 						 trxNo = Integer.toString(Character.getNumericValue(trxNo.charAt(0)) - Character.getNumericValue('A')+ 1);
 					 }
 					 else{
 						 trxName = Character.toString((char)(trxNo.charAt(0) + 'A' - '1'));
 					 }
 					 carrierSB.append(" " + trxNo);
 					 carrierSB.append(" " + 1);//non-relevant
 					 
 					 for (Node plan : trx.traverse(Order.DEPTH_FIRST, StopEvaluator.DEPTH_ONE, ReturnableEvaluator.ALL_BUT_START_NODE, DatasetRelationshipTypes.PLAN_ENTRY, Direction.OUTGOING)){
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
 					 
 					 if (freqArray.length > maxTRX){
 						 maxTRX = freqArray.length; 
 					 }
 					
 					if (freqArray.length == 0)
 						carrierSB.append(" " + 1);//number of frequencies required
 					else
 						carrierSB.append(" " + freqArray.length);//number of frequencies required
 					carrierSB.append(" " + freqArray.length);//number of frequencies given
 					 for (Integer frequency : freqArray){
 						 carrierSB.append(" " + frequency);//required frequencies
 					 }
 					 carrierSB.append("\n");
 					 
 					 /*for (Node proxySector : sector.traverse(Order.DEPTH_FIRST, StopEvaluator.DEPTH_ONE, new ReturnableEvaluator(){
 
 							@Override
 							public boolean isReturnableNode(TraversalPosition pos) {
 								if (pos.currentNode().getProperty(INeoConstants.PROPERTY_TYPE_NAME).equals(NodeTypes.SECTOR_SECTOR_RELATIONS.getId()))
 									return true;
 								
 								return false;
 							}
 						}, NetworkRelationshipTypes.INTERFERENCE, Direction.OUTGOING)){
 						 for (Relationship relation : proxySector.getRelationships(NetworkRelationshipTypes.INTERFERS, Direction.OUTGOING)){
 								Node interferenceProxySector = relation.getEndNode();
 								Node interferenceSector = interferenceProxySector.getSingleRelationship(NetworkRelationshipTypes.INTERFERENCE, Direction.INCOMING).getOtherNode(interferenceProxySector);
 								String subSectorName = getSectorNameForInterList(interferenceSector);	
 								
 								StringBuilder sbSubCell = new StringBuilder();
 								DecimalFormat df = new DecimalFormat("0.0000000000");
 								sbSubCell.append("INT 0\t0\t"); 
 								
 								try {
 									sbSubCell.append(df.format(relation.getProperty("CoA")).toString());
 								} catch (Exception e) {
 									sbSubCell.append((String)relation.getProperty("CoA"));
 								}
 								sbSubCell.append(" ");
 								
 								try {
 									sbSubCell.append(df.format(relation.getProperty("CoT")).toString());
 								} catch (Exception e) {
 									sbSubCell.append((String)relation.getProperty("CoT"));
 								}
 								sbSubCell.append(" ");
 								
 								try {
 									sbSubCell.append(df.format(relation.getProperty("AdA")).toString());
 								} catch (Exception e) {
 									sbSubCell.append((String)relation.getProperty("AdA"));
 								}
 								sbSubCell.append(" ");
 								
 								try {
 									sbSubCell.append(df.format(relation.getProperty("AdT")).toString());
 								} catch (Exception e) {
 									sbSubCell.append((String)relation.getProperty("AdT"));
 								}
 								sbSubCell.append(" ");
 								sbSubCell.append(subSectorName);
 								String[] intTrxNames = getAllTrxNames(interferenceSector);
 								for(String intName: intTrxNames){
 									sbAllInt.append(sbSubCell);
 									sbAllInt.append(intName);
 									sbAllInt.append("\n");
 									numberofinterferers++;
 								}
 								
 							}
 						 	
 					 	}
 
 
 					 	sbCell.append(numberofinterferers);
 						sbCell.append(" ");
 						sbCell.append(sectorName);
 						sbCell.append(trxName);
 						sbCell.append("\n");
 						if(numberofinterferers >0) {
 							intWriter.write(sbCell.toString());
 							intWriter.write(sbAllInt.toString());
 						}*/
 					 
 					
 					 
 						carrierWriter.write(carrierSB.toString());
 					 
 				}
 				
 				
 			}
 			
 			carrierWriter.close();
 		}
 		catch(Exception e){
 			AweConsolePlugin.exception(e);
 		}
 	}
 	
 	
 	
 	
 	
 	
 	/**
 	 * Creates the Control file to be given as input to the C++ engine
 	 */
 	public void createControlFile(HashMap<String, String> parameters){
 		if (maxTRX < 0)
 			maxTRX = Integer.parseInt(parameters.get("GMaxRTperCell"));
 		
 		try {
 			BufferedWriter writer  = new BufferedWriter(new FileWriter(files[CONTROL]));
 			
 			
 			writer.write("SiteSpacing " + parameters.get("SiteSpacing"));
 			writer.newLine();
 			
 			writer.write("CellSpacing " + parameters.get("CellSpacing"));
 			writer.newLine();
 			
 			writer.write("RegNbrSpacing " + parameters.get("RegNbrSpacing"));
 			writer.newLine();
 
 			writer.write("MinNbrSpacing " + parameters.get("MinNbrSpacing"));
 			writer.newLine();
 
 			writer.write("SecondNbrSpacing " + parameters.get("SecondNbrSpacing"));
 			writer.newLine();
 
 			writer.write("RecalculateAll " + parameters.get("RecalculateAll"));
 			writer.newLine();
 
 			writer.write("UseTraffic " + parameters.get("UseTraffic"));
 			writer.newLine();
 
 			writer.write("UseSONbrs " + parameters.get("UseSONbrs"));
 			writer.newLine();
 
 			writer.write("Quality " + parameters.get("Quality"));
 			writer.newLine();
 
 			writer.write("DecomposeInCliques " + parameters.get("DecomposeInCliques"));
 			writer.newLine();
 
 			writer.write("ExistCliques " + parameters.get("ExistCliques"));
 			writer.newLine();
 
 			writer.write("GMaxRTperCell " + maxTRX);
 			writer.newLine();
 
 			writer.write("GMaxRTperSite " + maxTRX);
 			writer.newLine();
 
 			writer.write("HoppingType " + parameters.get("HoppingType"));
 			writer.newLine();
 			
 			writer.write("UseGrouping " + parameters.get("UseGrouping"));
 			writer.newLine();
 
 			writer.write("NrOfGroups " + parameters.get("NrOfGroups"));
 			writer.newLine();
 			
 			writer.write("LogFile " + "\"" + this.logFileName + "\"");
 			writer.newLine();
 
 			writer.write("CellCardinality " + parameters.get("CellCardinality"));
 			writer.newLine();
 			
 			writer.write("CellFile " + "\"" + this.cellFilesArray[0] + "\"");
 			writer.newLine();
 			
 			writer.write("NeighboursFile " + "\"" + this.fileNames[NEIGHBOUR] + "\"");
 			writer.newLine();
 			
 			writer.write("InterferenceFile " + "\"" + this.interferenceFiles[0] + "\"");
 			writer.newLine();
 
 			writer.write("OutputFile " + "\"" + this.outputFileName + "\"");
 			writer.newLine();
 			
 			writer.write("CliquesFile " + "\"" + this.fileNames[CLIQUES] + "\"");
 			writer.newLine();
 
 			writer.write("ForbiddenFile " + "\"" + this.fileNames[FORBIDDEN] + "\"");
 			writer.newLine();
 			
 			writer.write("ExceptionFile " + "\"" + this.fileNames[EXCEPTION] + "\"");
 			writer.newLine();
 			
 			writer.write("Carriers " + parseCarriers(parameters.get("Carriers")));
 			writer.newLine();
 
 			writer.close();
 		}catch (Exception e){
 			AweConsolePlugin.exception(e);
 		}
 	}
 	
 	private String parseCarriers(String commaSeparated){
 		int numCarriers = commaSeparated.split("\\,").length;
 		String spaceSeparated = commaSeparated.replaceAll(",", " ");
 		spaceSeparated = numCarriers + " " + spaceSeparated;
 		
 		return spaceSeparated;
 	}
 	
 	/**
 	 * Creates the neighbors file for input to the C++ engine
 	 */
 	public void createNeighboursFile(){
 		Node startSector = null;
 		
 		try {
 			BufferedWriter writer  = new BufferedWriter(new FileWriter(files[NEIGHBOUR]));
 			
 			for (Node neighbour : afpRoot.traverse(Order.DEPTH_FIRST, StopEvaluator.DEPTH_ONE, ReturnableEvaluator.ALL_BUT_START_NODE, NetworkRelationshipTypes.NEIGHBOUR_DATA, Direction.OUTGOING)){
 				//TODO: put condition here to get the desired neighbour list in case of multiple neighbour list
 				startSector = neighbour.getSingleRelationship(NetworkRelationshipTypes.CHILD, Direction.OUTGOING).getEndNode();
 				break;				
 			}
 			
 			if (startSector != null){			
 				for (Node proxySector : startSector.traverse(Order.DEPTH_FIRST, StopEvaluator.END_OF_GRAPH, ReturnableEvaluator.ALL, NetworkRelationshipTypes.NEXT, Direction.OUTGOING)){
 					if (!proxySector.getProperty(INeoConstants.PROPERTY_TYPE_NAME).equals(NodeTypes.SECTOR_SECTOR_RELATIONS.getId()))
 						 continue;
 					Node sector = proxySector.getSingleRelationship(NetworkRelationshipTypes.NEIGHBOURS, Direction.INCOMING).getOtherNode(proxySector);
 					String sectorValues[] = parseSectorName(sector);			
 					writer.write("CELL " + sectorValues[0] + " " + sectorValues[1]);
 					writer.newLine();
 					for (Node neighbourProxySector: proxySector.traverse(Order.DEPTH_FIRST, StopEvaluator.DEPTH_ONE, ReturnableEvaluator.ALL_BUT_START_NODE, NetworkRelationshipTypes.NEIGHBOUR, Direction.OUTGOING)){
 						Node neighbourSector = neighbourProxySector.getSingleRelationship(NetworkRelationshipTypes.NEIGHBOURS, Direction.INCOMING).getOtherNode(neighbourProxySector);
 						sectorValues = parseSectorName(neighbourSector);
 						writer.write("NBR " + sectorValues[0] + " " + sectorValues[1]);
 						writer.newLine();
 					}
 				}
 			}
 			else {
 				AweConsolePlugin.info("No Neighbours data stored in the database");
 			}
 			
 			writer.close();
 		}catch (Exception e){
 			AweConsolePlugin.exception(e);
 		}
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
 	
 	public Long[] getAllTrxIds(Node sector){
 		ArrayList<Long> ids = new ArrayList<Long>();
 		for (Node trx : sector.traverse(Order.DEPTH_FIRST, StopEvaluator.END_OF_GRAPH, new ReturnableEvaluator(){
 
 			@Override
 			public boolean isReturnableNode(TraversalPosition pos) {
 				if (pos.currentNode().getProperty(INeoConstants.PROPERTY_TYPE_NAME).equals(NodeTypes.TRX.getId()))
 					return true;
 				
 				return false;
 			}
 		}, NetworkRelationshipTypes.CHILD, Direction.OUTGOING)){
 			long id = trx.getId();
 			ids.add(id);
 			
 		}
 		return ids.toArray(new Long[0]);
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
 	
 	
 	/**
 	 * Creates the interference file for input to the C++ engine
 	 */
 	public void createInterferenceFile(IProgressMonitor monitor){
 		Node startSector = null;
 
 		try {
 			BufferedWriter writer  = new BufferedWriter(new FileWriter(files[INTERFERENCE]));
 //			writer.write("ASSET like Site Interference Table");
 			writer.newLine();
 			
 			for (Node interferer : afpRoot.traverse(Order.DEPTH_FIRST, StopEvaluator.DEPTH_ONE, ReturnableEvaluator.ALL_BUT_START_NODE, NetworkRelationshipTypes.INTERFERENCE_DATA, Direction.OUTGOING)){
 				//TODO: put condition here to get the desired interference list in case of multiple interference list
 				startSector = interferer.getSingleRelationship(NetworkRelationshipTypes.CHILD, Direction.OUTGOING).getEndNode();
 				break;				
 			}
 			
 			if (startSector != null){			
 				for (Node proxySector : startSector.traverse(Order.DEPTH_FIRST, StopEvaluator.END_OF_GRAPH, ReturnableEvaluator.ALL, NetworkRelationshipTypes.NEXT, Direction.OUTGOING)){
 					
 					if(monitor != null ) {
 						if(monitor.isCanceled())
 							return;
 					}
 					if (!proxySector.getProperty(INeoConstants.PROPERTY_TYPE_NAME).equals(NodeTypes.SECTOR_SECTOR_RELATIONS.getId()))
 						 continue;
 					
 					Node sector = proxySector.getSingleRelationship(NetworkRelationshipTypes.INTERFERENCE, Direction.INCOMING).getOtherNode(proxySector);
 					String sectorName = getSectorNameForInterList(sector);
 					
 					
 					
 					String[] trxNames = getAllTrxNames(sector);
 					for(String name: trxNames){
 						StringBuilder sbCell = new StringBuilder();
 						sbCell.append("SUBCELL 0 0 1 1 ");
 						int numberofinterferers =0;
 						
 						
 						StringBuilder sbAllInt = new StringBuilder();
 						for (Relationship relation : proxySector.getRelationships(NetworkRelationshipTypes.INTERFERS, Direction.OUTGOING)){
 							Node interferenceProxySector = relation.getEndNode();
 							Node interferenceSector = interferenceProxySector.getSingleRelationship(NetworkRelationshipTypes.INTERFERENCE, Direction.INCOMING).getOtherNode(interferenceProxySector);
 							String subSectorName = getSectorNameForInterList(interferenceSector);	
 							
 							StringBuilder sbSubCell = new StringBuilder();
 							DecimalFormat df = new DecimalFormat("0.0000000000");
 							sbSubCell.append("INT 0\t0\t"); 
 							
 							try {
 								sbSubCell.append(df.format(relation.getProperty("CoA")).toString());
 							} catch (Exception e) {
 								sbSubCell.append((String)relation.getProperty("CoA"));
 							}
 							sbSubCell.append(" ");
 							
 							try {
 								sbSubCell.append(df.format(relation.getProperty("CoT")).toString());
 							} catch (Exception e) {
 								sbSubCell.append((String)relation.getProperty("CoT"));
 							}
 							sbSubCell.append(" ");
 							
 							try {
 								sbSubCell.append(df.format(relation.getProperty("AdA")).toString());
 							} catch (Exception e) {
 								sbSubCell.append((String)relation.getProperty("AdA"));
 							}
 							sbSubCell.append(" ");
 							
 							try {
 								sbSubCell.append(df.format(relation.getProperty("AdT")).toString());
 							} catch (Exception e) {
 								sbSubCell.append((String)relation.getProperty("AdT"));
 							}
 							sbSubCell.append(" ");
 							sbSubCell.append(subSectorName);
 							String[] intTrxNames = getAllTrxNames(interferenceSector);
 							for(String intName: intTrxNames){
 								sbAllInt.append(sbSubCell);
 								sbAllInt.append(intName);
 								sbAllInt.append("\n");
 								numberofinterferers++;
 							}
 							
 						}
 						
 						sbCell.append(numberofinterferers);
 						sbCell.append(" ");
 						sbCell.append(sectorName);
 						sbCell.append(name);
 						sbCell.append("\n");
 						if(numberofinterferers >0) {
 							writer.write(sbCell.toString());
 							writer.write(sbAllInt.toString());
 						}
 						
 					}
 				}
 			}
 			else {
 				AweConsolePlugin.info("No Interference data stored");
 			}
 			writer.close();
 		}catch (Exception e){
 			AweConsolePlugin.exception(e);
 		}
 	}
 	
 	/**
 	 * Creates the exception file for input to the C++ engine
 	 */
 	public void createExceptionFile(){
 		Node startSector = null;
 		try {
 			BufferedWriter writer  = new BufferedWriter(new FileWriter(files[EXCEPTION]));
 			Traverser exceptionList = afpRoot.traverse(Order.DEPTH_FIRST, StopEvaluator.DEPTH_ONE, ReturnableEvaluator.ALL_BUT_START_NODE, NetworkRelationshipTypes.EXCEPTION_DATA, Direction.OUTGOING);
 			
 			for (Node exception : exceptionList){
 				//TODO: put condition here to get the desired exception list in case of multiple interference list
 				startSector = exception.getSingleRelationship(NetworkRelationshipTypes.CHILD, Direction.OUTGOING).getEndNode();
 				break;				
 			}
 			
 			if (startSector != null){	
 				for (Node proxySector : startSector.traverse(Order.DEPTH_FIRST, StopEvaluator.END_OF_GRAPH, ReturnableEvaluator.ALL, NetworkRelationshipTypes.NEXT, Direction.OUTGOING)){
 					if (!proxySector.getProperty("type").equals("sector_sector_relations"))
 						 continue;
 					Node sector = proxySector.getSingleRelationship(NetworkRelationshipTypes.EXCEPTIONS, Direction.INCOMING).getOtherNode(proxySector);		
 					String sectorValues[] = parseSectorName(sector);
 					for (Relationship relation : proxySector.getRelationships(NetworkRelationshipTypes.EXCEPTION, Direction.OUTGOING)){
 						Node exceptionProxySector = relation.getEndNode();
 						Node exceptionSector = exceptionProxySector.getSingleRelationship(NetworkRelationshipTypes.EXCEPTIONS, Direction.INCOMING).getOtherNode(exceptionProxySector);
 						String exceptionSectorValues[] = parseSectorName(exceptionSector);
 						writer.write(sectorValues[0] + " " + sectorValues[1] + " " + exceptionSectorValues[0] + " " + exceptionSectorValues[1] + " " +
 									relation.getProperty("new_spacing"));
 						writer.newLine();
 					}
 				}
 			}
 			else {
 				AweConsolePlugin.info("No Exception data stored");
 			}
 			writer.close();
 		}catch (Exception e){
 			AweConsolePlugin.exception(e);
 		}
 	}
 	
 	/**
 	 * Creates the forbidden file for input to the C++ engine
 	 */
 	public void createForbiddenFile(){
 		try {
 			BufferedWriter writer  = new BufferedWriter(new FileWriter(files[FORBIDDEN]));
 			
 			 for (Node sector : afpRoot.traverse(Order.DEPTH_FIRST, StopEvaluator.END_OF_GRAPH, ReturnableEvaluator.ALL_BUT_START_NODE, NetworkRelationshipTypes.CHILD, Direction.OUTGOING)){
 				 if (!sector.getProperty("type").equals("sector"))
 				 	 continue;
 				 String sectorValues[] = parseSectorName(sector);			
 				 
 				 Object obj = sector.getProperty("numberofforbidden", null);
 				 if (!(obj == null)){
 					 String numForbidden = obj.toString();
 					 writer.write(sectorValues[0]);
 					 writer.write(" " + sectorValues[1]);
 					 writer.write(" " + numForbidden);
 					 obj = sector.getProperty("forb_fr_list");
 					 
 					 if (obj != null){
 						 Integer temp[]  = new Integer[2];
 						 if (obj.getClass() == temp.getClass()){
 							 Integer forbiddenFrequencies[] = (Integer[]) obj;
 							 for (Integer frequency : forbiddenFrequencies){
 								 writer.write(" " + frequency);
 							 }
 						 }
 						 else{
 							 int forbiddenFrequencies[] = (int[])obj;
 							 for (int frequency : forbiddenFrequencies){
 								 writer.write(" " + frequency);
 							 }
 						 }
 						 
 					 }
 					 writer.newLine();
 				 }
 			 }
 
 			
 			writer.close();
 		}catch (Exception e){
 			AweConsolePlugin.exception(e);
 		}
 	}
 	
 	/**
 	 * Creates the cliques file for input to the C++ engine
 	 */
 	public void createCliquesFile(){
 
 		try {
 			BufferedWriter writer  = new BufferedWriter(new FileWriter(files[CLIQUES]));
 			
 			/**
 			 *Write code here to write the file
 			 */
 			
 			writer.close();
 		}catch (Exception e){
 			AweConsolePlugin.exception(e);
 		}
 	}
 	
 	
 	
 	
 /*	public void createParamFile(){
 		File file = getFile(this.paramFileName);
 		try {
 			file.createNewFile();
 			BufferedWriter writer  = new BufferedWriter(new FileWriter(file));
 			writer.write(fileNames[CONTROL] + "\n\r");
 			writer.close();
 		}catch (Exception e){
 			AweConsolePlugin.exception(e);
 		}
 	}
 */	
 	private void createTmpFolder(){
 		File file = new File(this.tmpAfpFolder);
 		if (!file.exists())
 			file.mkdir();
 	}
 	
 	/**
 	 * Gets the file instance for the specified file name
 	 * @param name the name for that file
 	 * @return returns the file instance
 	 */
 	
 	/*private File getFile(String name){
 		
 		File file = new File(this.tmpAfpFolder);
 		if (!file.exists())
 			file.mkdir();
 		
 		file = new File(name);
 		return file; 
 	}*/
 	
 	public String getTmpFolderPath(){
 		
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
 		
 		return dir.getPath() + "/";
 	}
 
 }
