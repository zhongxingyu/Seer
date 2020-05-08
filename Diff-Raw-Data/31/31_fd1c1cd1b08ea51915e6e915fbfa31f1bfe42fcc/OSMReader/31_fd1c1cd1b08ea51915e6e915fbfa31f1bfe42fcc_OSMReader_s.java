 /**
  * 
  */
 package net.skyebook;
 
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.zip.GZIPInputStream;
 
 import edu.poly.bxmc.betaville.jme.map.GPSCoordinate;
 import edu.poly.bxmc.betaville.osm.Node;
 
 /**
  * @author Skye Book
  *
  */
 public class OSMReader {
 	
 	private long totalBytesRead = 0;
 	private BufferedReader br;
 	
 	// line prefixes
 	private static final String nodePrefix = "<node";
 	private static final String wayPrefix = "<way";
 	private static final String relationPrefix = "<relation";
 	private static final String tagPrefix = "<tag";
 	private static final String nodeReferencePrefix = "<nd";
 	
 	// line suffixes
 	private static final String nodeSuffix = "</node";
 	private static final String waySuffix = "</way";
 	private static final String relationSuffix = "</relation";
 	private static final String tagSuffix = "</tag";
 	private static final String nodeReferenceSuffix = "</nd";
 	
 	private boolean relationsHaveBeenProcessed = false;
 	private boolean waysHaveBeenProcessed = false;
 	
 	public void read() throws FileNotFoundException, IOException{
 		GZIPInputStream is = new GZIPInputStream(new FileInputStream("/Users/skyebook/Downloads/new-york.osm.gz"));
 		br = new BufferedReader(new InputStreamReader(is));
 		long start = System.currentTimeMillis();
 		while(br.ready()){
 			readLine(br.readLine());
 		}
 		System.out.println("Read Took " + (System.currentTimeMillis()-start));
 	}
 	
 	public void readPlain() throws FileNotFoundException, IOException{
 		FileInputStream fis= new FileInputStream("/Users/skyebook/Downloads/new-york.osm.xml");
 		br = new BufferedReader(new InputStreamReader(fis));
 		long start = System.currentTimeMillis();
 		while(br.ready()){
 			readLine(br.readLine());
 		}
 		System.out.println("Read Took " + (System.currentTimeMillis()-start));
 	}
 	
 	public void readLine(String line) throws IOException{
 		totalBytesRead+=line.length();
 		
 		// what type of element is this?
 		if(line.contains(nodePrefix)){
 			Node node = new Node();
 			double lat = -1;
 			double lon = -1;
 			// get the information
 			String[] attributes = line.split("=\"");
 			for(int i=0; i<attributes.length; i++){
 				String attr=attributes[i];
				if(attr.endsWith("id")){
 					node.setId(Long.parseLong(getNextAttributeValue(attributes[i+1])));
 				}
				else if(attr.endsWith("lat")){
 					lat = Double.parseDouble(getNextAttributeValue(attributes[i+1]));
 					if(lon!=-1){
 						node.setLocation(new GPSCoordinate(0, lat, lon));
 					}
 				}
				else if(attr.endsWith("lon")){
 					lon = Double.parseDouble(getNextAttributeValue(attributes[i+1]));
 					if(lat!=-1){
 						node.setLocation(new GPSCoordinate(0, lat, lon));
 					}
 				}
				else if(attr.endsWith("user")){
 					// Will implement this later if the need arises
 				}
				else if(attr.endsWith("uid")){
 					// Will implement this later if the need arises
 				}
				else if(attr.endsWith("visible")){
 					// Will implement this later if the need arises
 				}
				else if(attr.endsWith("version")){
 					// Will implement this later if the need arises
 				}
				else if(attr.endsWith("changeset")){
 					// Will implement this later if the need arises
 				}
				else if(attr.endsWith("timestamp")){
 					// Will implement this later if the need arises
 				}
 			}
			//System.out.println(node.getId()+" "+node.getLocation().toString());
 
 			// move forward until the node is complete
 			if(!isLineSelfContained(line)){
 				String subLine = "";
 				while(br.ready() && !subLine.contains(nodeSuffix)){
 					subLine = br.readLine();
 					//System.out.println("Subline caught: " + subLine);
 				}
 			}
 		}
 		else if(line.contains(wayPrefix)){
 			
 		}
 		else if(line.contains(relationPrefix)){
 			
 		}
 		else if(line.contains(tagPrefix)){
 			
 		}
 		else if(line.contains(nodeReferencePrefix)){
 			
 		}
 	}
 	
 	private String getNextAttributeValue(String nextToken){
 		return nextToken.substring(0, nextToken.indexOf("\""));
 	}
 	
 	/**
 	 * Checks if the XML element starts and ends on the same line
 	 * @param line The content of the line to check
 	 * @return True if the element ends on the same line as it begins, otherwise false
 	 */
 	private boolean isLineSelfContained(String line){
 		return line.endsWith("/>");
 	}
 	
 	private long getTotalBytesRead(){
 		return totalBytesRead;
 	}
 	
 	public static void main(String[] args) throws FileNotFoundException, IOException{
 		OSMReader reader = new OSMReader();
 		reader.read();
 		//reader.readPlain();
 		System.out.println(reader.getTotalBytesRead() + " bytes read");
 	}
 
 }
