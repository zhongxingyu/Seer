 /**
  * 
  */
 package net.skyebook;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.LineNumberReader;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.zip.GZIPInputStream;
 
 import net.skyebook.RelationLink.Type;
 
 import edu.poly.bxmc.betaville.jme.map.GPSCoordinate;
 import edu.poly.bxmc.betaville.osm.KeyMatcher;
 import edu.poly.bxmc.betaville.osm.Node;
 import edu.poly.bxmc.betaville.osm.Relation;
 import edu.poly.bxmc.betaville.osm.RelationMemeber;
 import edu.poly.bxmc.betaville.osm.tag.AbstractTag;
 
 /**
  * @author Skye Book
  *
  */
 public class OSMScanner {
 
 	private long totalBytesRead = 0;
 	private LineNumberReader br;
 
 	// line prefixes
 	private static final String nodePrefix = "<node";
 	private static final String wayPrefix = "<way";
 	private static final String relationPrefix = "<relation";
 	private static final String tagPrefix = "<tag";
 	private static final String nodeReferencePrefix = "<nd";
 	private static final String memberReferencePrefix = "<member";
 	private static final String changesetPrefix = "<changeset";
 	private static final String boundingBoxPrefix = "<bound box";
 
 	// line suffixes
 	private static final String nodeSuffix = "</node>";
 	private static final String waySuffix = "</way>";
 	private static final String relationSuffix = "</relation>";
 	private static final String tagSuffix = "</tag>";
 	private static final String memberReferenceSuffix = "</member>";
 	private static final String changesetSuffix = "</changeset>";
 	private static final String boundingBoxSuffix = "</bound box>";
 
 	private boolean relationsHaveBeenProcessed = false;
 	private boolean waysHaveBeenProcessed = false;
 	private boolean nodesHaveBeenProcessed = false;
 	
 	// consider this a checkpoint that can be used to run the scanner back to a position before starting
 	private boolean reachedFirstWay = false;
 	private boolean reachedFirstRelation = false;
 	
 	private boolean goStraightToRelations = true;
 
 	private int lastWeirdTag=-1;
 
 	private ExecutorService threadPool = Executors.newFixedThreadPool(1);
 
 	private ArrayList<DBActions> pool = new ArrayList<DBActions>();
 	private int connectionCount=4;
 
 	private boolean isMemoryAvailable(){
 		return (float)Runtime.getRuntime().freeMemory()/(float)Runtime.getRuntime().maxMemory() < .75f;
 	}
 
 	private DBActions getConnection(){
 		return pool.get(0);
 		/*
 		// look for a pool until one is empty
 		while(true){
 			for(DBActions dba : pool){
 				if(!dba.isBusy()) return dba;
 			}
 		}
 		 */
 	}
 
 	public void read() throws FileNotFoundException, IOException, NumberFormatException, InstantiationException, IllegalAccessException{
 
 		for(int i=0; i<connectionCount; i++){
 			pool.add(new DBActions("root", "root", "osm"));
 		}
 
 
 		br = new LineNumberReader(new InputStreamReader(new GZIPInputStream(new FileInputStream("/home/skye/planet-110615.osm.gz"))));
 		long start = System.currentTimeMillis();
 
 
 		// find the start of the ways
 		/*
 		while(br.ready()){
 			while(lineStartsNode(br.readLine().trim())){}
 			while(lineStartsWay(br.readLine().trim())){}
 			while(lineStartsRelation(br.readLine().trim())){}
 		}
 
 		// find the start of the relations
 
 		// go back to the beginning
 		br.setLineNumber(0);
 		 */
 
 		// read all of the nodes
 		while(br.ready()){
 			readLine(br.readLine());
 		}
 
 
 		System.out.println("Read Took " + (System.currentTimeMillis()-start));
 	}
 
 	public void readPlain() throws FileNotFoundException, IOException, NumberFormatException, InstantiationException, IllegalAccessException{
 		FileInputStream fis= new FileInputStream("/Users/skyebook/Downloads/new-york.osm.xml");
 		br = new LineNumberReader(new InputStreamReader(fis));
 		long start = System.currentTimeMillis();
 		while(br.ready()){
 			readLine(br.readLine());
 		}
 		System.out.println("Read Took " + (System.currentTimeMillis()-start));
 	}
 
 	public void readLine(String line) throws IOException, NumberFormatException, InstantiationException, IllegalAccessException{
 		totalBytesRead+=line.length();
 
 		// what type of element is this?
 		if(/*waysHaveBeenProcessed && relationsHaveBeenProcessed && */line.contains(nodePrefix)){
 			if(!reachedFirstRelation && goStraightToRelations) return;
 			Node node = new Node();
 			double lat = Double.NaN;
 			double lon = Double.NaN;
 			// get the information
 			String[] attributes = getAttributes(line);
 			for(int i=0; i<attributes.length; i++){
 				String attr=attributes[i];
 				if(attr.endsWith(" id")){
 					//System.out.println(attributes[i]);
 					//System.out.println(attributes[i+1]);
 					node.setId(Long.parseLong(getNextAttributeValue(attributes[i+1])));
 				}
 				else if(attr.endsWith(" lat")){
 					String val = getNextAttributeValue(attributes[i+1]);
 					lat = Double.parseDouble(val);
 					if(lon!=Double.NaN){
 						node.setLocation(new GPSCoordinate(0, lat, lon));
 					}
 				}
 				else if(attr.endsWith(" lon")){
 					String val = getNextAttributeValue(attributes[i+1]);
 					lon = Double.parseDouble(val);
 					if(lat!=Double.NaN){
 						node.setLocation(new GPSCoordinate(0, lat, lon));
 					}
 				}
 				else if(attr.endsWith(" user")){
 					// Will implement this later if the need arises
 				}
 				else if(attr.endsWith(" uid")){
 					// Will implement this later if the need arises
 				}
 				else if(attr.endsWith(" visible")){
 					// Will implement this later if the need arises
 				}
 				else if(attr.endsWith(" version")){
 					// Will implement this later if the need arises
 				}
 				else if(attr.endsWith(" changeset")){
 					// Will implement this later if the need arises
 				}
 				else if(attr.endsWith(" timestamp")){
 					// Will implement this later if the need arises
 				}
 			}
 			//System.out.println(node.getId()+" "+node.getLocation().toString());
 
 			// move forward until the node is complete
 			if(!isLineSelfContained(line)){
 				String subLine = "";
 				while(br.ready()){
 					subLine = br.readLine().trim();
 					// we are done with this node
 					if(subLine.contains(nodeSuffix)) break;
 
 					if(subLine.startsWith(tagPrefix)){
 						String[] subLineAttributes = getAttributes(subLine);
 						AbstractTag tag = null;
 						String value = null;
 						for(int i=0; i<subLineAttributes.length; i++){
 							String attr=subLineAttributes[i];
 							if(attr.endsWith(" k")){
 								// Apparently not all keys are included!
 								//Class<? extends AbstractTag> c = KeyMatcher.getKey(getNextAttributeValue(subLineAttributes[i+1]));
 								if(KeyMatcher.getKey(getNextAttributeValue(subLineAttributes[i+1]))==null){
 									System.out.println("Unknown Key Found: " + getNextAttributeValue(subLineAttributes[i+1]));
 								}
 								tag = new AbstractTag();
 								tag.setKey(getNextAttributeValue(subLineAttributes[i+1]));
 								if(value!=null) tag.setValue(value);
 							}
 							else if(attr.endsWith(" v")){
 								value = getNextAttributeValue(subLineAttributes[i+1]);
 								if(tag!=null) tag.setValue(value);
 							}
 						}
 						node.addTag(tag);
 					}
 				}
 			}
 
 			// insert node into database
 			addNode(node);
 		}
 		else if(/*relationsHaveBeenProcessed && !waysHaveBeenProcessed && */line.contains(wayPrefix)){
 			if(!reachedFirstWay) reachedFirstWay=true;
 			if(!reachedFirstRelation && goStraightToRelations) return;
 			ShallowWay way = new ShallowWay();
 
 
 			// get the information
 			String[] attributes = getAttributes(line);
 			for(int i=0; i<attributes.length; i++){
 				String attr=attributes[i];
 				if(attr.endsWith(" id")){
 					way.setId(Long.parseLong(getNextAttributeValue(attributes[i+1])));
 				}
 				else if(attr.endsWith(" user")){
 					// Will implement this later if the need arises
 				}
 				else if(attr.endsWith(" uid")){
 					// Will implement this later if the need arises
 				}
 				else if(attr.endsWith(" visible")){
 					// Will implement this later if the need arises
 				}
 				else if(attr.endsWith(" version")){
 					// Will implement this later if the need arises
 				}
 				else if(attr.endsWith(" changeset")){
 					// Will implement this later if the need arises
 				}
 				else if(attr.endsWith(" timestamp")){
 					// Will implement this later if the need arises
 				}
 			}
 			//System.out.println(node.getId()+" "+node.getLocation().toString());
 
 			// move forward until the node is complete
 			if(!isLineSelfContained(line)){
 				String subLine = "";
 				while(br.ready()){
 					subLine = br.readLine().trim();
 					// we are done with this way
 					if(subLine.contains(waySuffix)) break;
 
 					if(subLine.startsWith(tagPrefix)){
 						String[] subLineAttributes = getAttributes(subLine);
 						AbstractTag tag = null;
 						String value = null;
 						for(int i=0; i<subLineAttributes.length; i++){
 							String attr=subLineAttributes[i];
 							if(attr.endsWith(" k")){
 								// Apparently not all keys are included!
 								//Class<? extends AbstractTag> c = KeyMatcher.getKey(getNextAttributeValue(subLineAttributes[i+1]));
 								if(KeyMatcher.getKey(getNextAttributeValue(subLineAttributes[i+1]))==null){
 									System.out.println("Unknown Key Found: " + getNextAttributeValue(subLineAttributes[i+1]));
 								}
 								tag = new AbstractTag();
 								tag.setKey(getNextAttributeValue(subLineAttributes[i+1]));
 								if(value!=null) tag.setValue(value);
 							}
 							else if(attr.endsWith(" v")){
 								value = getNextAttributeValue(subLineAttributes[i+1]);
 								if(tag!=null) tag.setValue(value);
 							}
 						}
 						way.addTag(tag);
 					}
 					else if(subLine.startsWith(nodeReferencePrefix)){
 						String[] subLineAttributes = getAttributes(subLine);
 						for(int i=0; i<subLineAttributes.length; i++){
 							String attr=subLineAttributes[i];
 							if(attr.endsWith(" ref")){
 								way.addNodeReference(Long.parseLong(getNextAttributeValue(subLineAttributes[i+1])));
 							}
 						}
 					}
 				}
 			}
 
 
 			addWay(way);
 		}
 		else if(/*!relationsHaveBeenProcessed && */line.contains(relationPrefix)){
 			if(!reachedFirstRelation) reachedFirstRelation=true;
 			Relation relation = createRelation(line);
 			addRelation(relation);
 		}
 		else if(line.trim().startsWith(tagPrefix)){
 			if(lastWeirdTag+1!=br.getLineNumber()){
 				br.setLineNumber(br.getLineNumber()-2);
 				System.out.println(br.readLine());
 				System.out.println(br.readLine());
 			}
 			lastWeirdTag=br.getLineNumber();
 			System.out.println("ERROR("+br.getLineNumber()+"): "+line);
 			//System.out.println("This should not have occurred (tag prefix caught at wrong level)");
 		}
 		else if(line.trim().startsWith(nodeReferencePrefix)){
 			System.out.println("This should not have occurred (node reference caught at wrong level");
 		}
 		else if(line.trim().startsWith(boundingBoxPrefix)){
 			// read through bbox
 			System.out.println("bbox");
 			if(!line.trim().endsWith("/>")){
 				while(br.ready()){
 					String bbLine = br.readLine();
 					if(bbLine.contains(boundingBoxSuffix)) break;
 				}
 			}
 		}
 		else if(line.trim().startsWith(changesetPrefix)){
 			// read through changeset
 			if(!line.trim().endsWith("/>")){
 				while(br.ready()){
 					String chSetLine = br.readLine();
 					if(chSetLine.contains(changesetSuffix)) break;
 				}
 			}
 			//if(br.getLineNumber()%100000==0) System.out.println("Line " + br.getLineNumber() + " is a changeset");
 		}
 	}
 
 	/**
 	 * Creates a relation from a line.  Since relations can be nested, this element has been broken
 	 * off into its own method that can work recursively
 	 * @param line
 	 * @return
 	 * @throws NumberFormatException
 	 * @throws IOException
 	 * @throws IllegalAccessException 
 	 * @throws InstantiationException 
 	 */
 	private Relation createRelation(String line) throws NumberFormatException, IOException, InstantiationException, IllegalAccessException{
 		Relation relation = new Relation();
 		String[] attributes = getAttributes(line);
 		for(int i=0; i<attributes.length; i++){
 			String attr=attributes[i];
 			if(attr.endsWith(" id")){
 				// get the relation id
 				relation.setId(Long.parseLong(getNextAttributeValue(attributes[i+1])));
 			}
 		}
 
 		// move forward until the node is complete
 		if(!isLineSelfContained(line)){
 			String subLine = "";
 			while(br.ready()){
 				subLine = br.readLine().trim();
 				// we are done with this relation
 				if(subLine.endsWith(relationSuffix)) break;
 
 
 				if(subLine.startsWith(nodeReferencePrefix)){
 
 				}
 				else if(subLine.startsWith(tagPrefix)){
 					String[] subLineAttributes = getAttributes(subLine);
 					AbstractTag tag = null;
 					String value = null;
 					for(int i=0; i<subLineAttributes.length; i++){
 						String attr=subLineAttributes[i];
 						if(attr.endsWith(" k")){
 							// Apparently not all keys are included!
 							//Class<? extends AbstractTag> c = KeyMatcher.getKey(getNextAttributeValue(subLineAttributes[i+1]));
 							if(KeyMatcher.getKey(getNextAttributeValue(subLineAttributes[i+1]))==null){
 								System.out.println("Unknown Key Found: " + getNextAttributeValue(subLineAttributes[i+1]));
 							}
 							tag = new AbstractTag();
 							tag.setKey(getNextAttributeValue(subLineAttributes[i+1]));
 							if(value!=null) tag.setValue(value);
 						}
 						else if(attr.endsWith(" v")){
 							value = getNextAttributeValue(subLineAttributes[i+1]);
 							if(tag!=null) tag.setValue(value);
 						}
 					}
 					relation.addTag(tag);
 				}
 				else if(subLine.startsWith(memberReferencePrefix)){
 					RelationLink link = new RelationLink();
 					String role = null;
 					String[] subLineAttributes = getAttributes(subLine);
 					for(int i=0; i<subLineAttributes.length; i++){
 						String attr=subLineAttributes[i];
 						if(attr.endsWith(" type")){
 							String type = getNextAttributeValue(subLineAttributes[i+1]).toLowerCase();
 							link.setType(Type.valueOf(type));
 						}
 						else if(attr.endsWith(" ref")){
 							link.setId(Long.parseLong(getNextAttributeValue(subLineAttributes[i+1])));
 						}
 						else if(attr.endsWith(" role")){
 							role = getNextAttributeValue(subLineAttributes[i+1]);
 
 							// eh.. this is crummy code here
 							if(role==null) role="";
 						}
 					}
 					relation.addMemeber(new RelationMemeber(link, role));
 				}
 				else if(lineStartsRelation(subLine)){
 					relation.addMemeber(new RelationMemeber(createRelation(subLine), ""));
 				}
 				else{
 					System.out.println("Unknown prefix found: " + subLine);
 				}
 			}
 		}
 		// grab the nodes
 		// is this something we want? (search for tags)
 
 		return relation;
 	}
 
 	private void addNode(final Node node){
 
 		try {
 			getConnection().addNode(node);
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		/*
 		threadPool.submit(new Runnable() {
 
 			@Override
 			public void run() {
 				try {
 					getConnection().addNode(node);
 				} catch (SQLException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		});
 		 */
 	}
 
 	private void addWay(final ShallowWay way){
 
 		try {
 			getConnection().addWay(way);
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		/*
 		threadPool.submit(new Runnable() {
 
 			@Override
 			public void run() {
 				try {
 					getConnection().addWay(way);
 				} catch (SQLException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		});
 		 */
 	}
 
 	private void addRelation(final Relation relation){
 
 		try {
 			getConnection().addRelation(relation);
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		/*
 		threadPool.submit(new Runnable() {
 
 			@Override
 			public void run() {
 				try {
 					getConnection().addRelation(relation);
 				} catch (SQLException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		});
 		 */
 	}
 
 	private String[] getAttributes(String line){
 		return line.split("=\"");
 	}
 
 	private String getNextAttributeValue(String nextToken){
 		try{
 		return nextToken.substring(0, nextToken.indexOf("\""));
 		}catch(StringIndexOutOfBoundsException e){
 			return " ";
 		}
 	}
 
 	private boolean lineStartsNode(String line){
 		return line.startsWith(nodePrefix);
 	}
 
 	private boolean lineStartsWay(String line){
 		return line.startsWith(wayPrefix);
 	}
 
 	private boolean lineStartsRelation(String line){
 		return line.startsWith(relationPrefix);
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
 
 	public static void main(String[] args) throws FileNotFoundException, IOException, NumberFormatException, InstantiationException, IllegalAccessException{
 		OSMScanner reader = new OSMScanner();
 		reader.read();
 		//reader.readPlain();
 		System.out.println(reader.getTotalBytesRead() + " bytes read");
 	}
 
 }
