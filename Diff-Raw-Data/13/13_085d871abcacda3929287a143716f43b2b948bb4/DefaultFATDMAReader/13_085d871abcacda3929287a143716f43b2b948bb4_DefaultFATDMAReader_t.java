 package dk.frv.eavdam.io;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Scanner;
 
 import dk.frv.eavdam.data.FATDMACell;
 import dk.frv.eavdam.data.FATDMAChannel;
 import dk.frv.eavdam.data.FATDMADefaultChannel;
 import dk.frv.eavdam.data.FATDMANode;
 
 public class DefaultFATDMAReader {
 	public static String defaultPath = "share/data/";
 	public static String defaultFile = "default.fatdma.dat";
 	
 	private static Map<String,List<FATDMACell>> cacheValues = null;
 	
 	/**
 	 * Reads the default FATDMA values to a HashMap from a file. HashMap contains the two default values.
 	 * 
 	 * @param dir Directory/path of the file. If null, defaultPath will be used.
 	 * @param filename Name of the file. If null, defaultFile will be used.
 	 * @return HashMap that contains the default values for each attribute.
 	 */
 	public static Map<String,List<FATDMACell>> readDefaultValues(String dir, String filename){
 		if(cacheValues != null) return cacheValues;
 		
 		Map<String, List<FATDMACell>> values = new HashMap<String, List<FATDMACell>>();
 		
 		if(dir == null) dir = defaultPath;
 		if(filename == null || filename.length() == 0) filename = defaultFile;
 		
 		if(!dir.endsWith("/") || !dir.endsWith("\\")) dir += "/";
 		
 		try{
 			Scanner in = new Scanner(new File(dir+filename));
 		
 			String currentCell = "", currentBSReport = "";
 			List<FATDMACell> channelValues = null;
 			while(in.hasNext()){
 				String line = in.nextLine();
 				if(line == null || line.startsWith("#") || line.trim().length() == 0) continue;
 				
 				
 				/*
 				  1-I	0	1	250 or 750	125 or 375	1	250 or 750	Base Station report*	0	   
 						312	1	0 or 1125	877	1	0 or 1125	Data Link Management + general purpose *		   
 						602 0, 1, 2 or 3	0 or 1125	12	0, 1, 2 or 3	0 or 1125	General purpose *		   
 				  1-II	125 	1	250 or 750	0 or 500	1	250 or 750	Base Station report*	125	   
 						877	1	0 or 1125	312	1	0 or 1125	Data Link Management + general purpose *		   
 						12	0, 1, 2 or 3	0 or 1125	602	0, 1, 2 or 3	0 or 1125	General purpose *
 				 */
 				
 				String[] columns = line.split("\t");
 				if(columns.length < 9) continue;
 				
 
 				
 				FATDMACell cell = new FATDMACell();
 					
 				if(columns[0].length() > 0){
 					if(channelValues != null){
 						List<FATDMACell> dcl = new ArrayList<FATDMACell>();
 						for(FATDMACell dc : channelValues){
 							dcl.add(dc);
 						}
 						
 						values.put(currentCell, dcl);
 					}
 						
 					channelValues = new ArrayList<FATDMACell>();
 						
 					cell.setCell(columns[0]);
 					cell.setBaseStationReportStartingSlot(new Integer(Integer.parseInt(columns[8])));
 					
 					currentCell = columns[0];
 					currentBSReport = columns[8];
 
 				}else{
 					cell.setCell(currentCell);
 					cell.setBaseStationReportStartingSlot(new Integer(Integer.parseInt(currentBSReport)));
 				}
 					
 				//Channel A
 				//Two objects, one for semaphore and one for non-semaphore node.
 				FATDMADefaultChannel channelA = new FATDMADefaultChannel();
 				channelA.setChannel("A");
 				
 				FATDMANode semA = new FATDMANode();
 				FATDMANode nonSemA = new FATDMANode();
 				
 				semA.setSemaphore(true);
 				nonSemA.setSemaphore(false);
 				
 				//index 1:
 				Integer startingSlot = Integer.parseInt(columns[1].trim());
 				semA.setStartingSlot(startingSlot);
 				nonSemA.setStartingSlot(startingSlot);
 				
 				//index 2:
 				Integer reservationBlock = null;
 				try{
 					if(columns[2].indexOf(",") > 0){ //case: 0, 1, 2 or 3
 						reservationBlock = new Integer(6);
 					}else if(columns[2].indexOf("or") > 0){ //case; 1 or 2
 						reservationBlock = new Integer(7);
 					}else{
 						reservationBlock = Integer.parseInt(columns[2].trim());
 					}
 				}catch(Exception e){
 					e.printStackTrace();
 				}
 				
 				semA.setBlockSize(reservationBlock);
 				nonSemA.setBlockSize(reservationBlock);
 				
 				//Index 3:
 				String[] incrementsA = columns[3].split("or");
 				semA.setIncrement(new Integer(Integer.parseInt(incrementsA[0].trim())));
 				
 				if(incrementsA.length > 1)
 					nonSemA.setIncrement(new Integer(Integer.parseInt(incrementsA[1].trim())));
 				else
 					nonSemA.setIncrement(new Integer(Integer.parseInt(incrementsA[0].trim())));
 				
 				//Index 7:
 				semA.setUsage(columns[7]);
 				nonSemA.setUsage(columns[7]);
 					
 				channelA.setSemaphoreNode(semA);
 				channelA.setNonSemaphoreNode(nonSemA);
 				
 				cell.setChannelA(channelA);
 				
 				
 				
 				//Channel B
 				FATDMADefaultChannel channelB = new FATDMADefaultChannel();
 				channelB.setChannel("B");
 				FATDMANode semB = new FATDMANode();
 				FATDMANode nonSemB = new FATDMANode();
 				
 				semB.setSemaphore(true);
 				nonSemB.setSemaphore(false);
 				
 				//Index 4:
 				String[] ssB = columns[4].split("or");
 				semB.setStartingSlot(new Integer(Integer.parseInt(ssB[0].trim())));
 				
 				if(ssB.length > 1)
 					nonSemB.setStartingSlot(new Integer(Integer.parseInt(ssB[1].trim())));
 				else
 					nonSemB.setStartingSlot(new Integer(Integer.parseInt(ssB[0].trim())));
 
 				
 				//Index 5:
 				Integer reservationBlockB = null;
 				try{
 					if(columns[5].indexOf(",") > 0){ //case: 0, 1, 2 or 3
 						reservationBlockB = new Integer(6);
 					}else if(columns[5].indexOf("or") > 0){ //case; 1 or 2
 						reservationBlockB = new Integer(7);
 					}else{
 						reservationBlockB = Integer.parseInt(columns[5].trim());
 					}
 				}catch(Exception e){
 					e.printStackTrace();
 				}
 				
 				semB.setBlockSize(reservationBlockB);
 				nonSemB.setBlockSize(reservationBlockB);
 				
 				
 				//Index 6:
 				String[] incrementsB = columns[6].split("or");
 				semB.setIncrement(new Integer(Integer.parseInt(incrementsB[0].trim())));
 				
 				if(incrementsB.length > 1)
 					nonSemB.setIncrement(new Integer(Integer.parseInt(incrementsB[1].trim())));
 				else
 					nonSemB.setIncrement(new Integer(Integer.parseInt(incrementsB[0].trim())));
 				
 				//Index 7:
 				semB.setUsage(columns[7]);
 				nonSemB.setUsage(columns[7]);
 				
 				
 				channelB.setSemaphoreNode(semB);
 				channelB.setNonSemaphoreNode(nonSemB);
 				
 				cell.setChannelB(channelB);
 
 				channelValues.add(cell);
 			}
 			
			if(channelValues != null){ //Store the last values...
				List<FATDMACell> dcl = new ArrayList<FATDMACell>();
				for(FATDMACell dc : channelValues){
					dcl.add(dc);
				}
//				System.out.println(currentCell);
				values.put(currentCell, dcl);
			}
			
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 		
 		cacheValues = values;
 		
 		return values;
 	}
 	
 	/**
 	 * Used for testing this class.
 	 * 
 	 * Prints the parsed values.
 	 * 
 	 * @param args
 	 */
 	public static void main(String args[]){
 		Map<String,List<FATDMACell>> cells = DefaultFATDMAReader.readDefaultValues(null, null);
 		for(String c : cells.keySet()){
 			
 			for(FATDMACell cell : cells.get(c)){
				if(cell.getCell().equals("36-II"))
					System.out.println(c+": "+cell.toString());
 			}
 			
 		}
 	}
 }
