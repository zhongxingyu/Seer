 package ActiveSpace;
 
 import java.io.IOException;
 import java.io.InvalidObjectException;
 import java.net.URI;
 import java.net.URISyntaxException;
 
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.xml.sax.SAXException;
 
 /**
  * a Space is a collection of regions with associated rules
  * 
  * @author cwkampe
  */
 public class Space {
 
 	private String name;			// name of this space
 	private DocumentBuilder db;		// parser instance
 	private List<Region> regions;	// list of registered regions
 	private int debugLevel;			// level of desired debug output
 	
 	// these are only used for testing (simulated actor walks)
 	private Coord entryPos;		// where new actors enter the scene
 	private Actor lastActor;	// last actor we were testing
 	private int lastRegion;		// number of regions completed for this actor
 	private final float step = 10.0F;	// test-walk step size (in mm)
 
 
 	public Space() {
 		regions = new LinkedList<Region>();
 		debugLevel = 1;			// basic debug info
 		name = null;			// we do not yet have a name
 		db = null;				// we have not yet created a parser
 		entryPos = null;		// we don't have any regions yet
 		lastActor = null;		// we haven't tested any actors yet
 		lastRegion = 0;			// there are no walks in progress
 	}
 
 	public void debug(int level) {		// control the level of diagnostics
 		debugLevel = level;
 	}
 	
 	/**
 	 * @return the name of this space
 	 */
 	public String name() {
 		return this.name;
 	}
 	
 	/**
 	 * set the name of this space
 	 * @param newname
 	 */
 	public void name( String newname ) {
 		this.name = newname;
 	}
 	
 	/**
 	 * add a new region to the space
 	 * 
 	 * @param r	region to be added
 	 */
 	public void addRegion(Region r) {
 		regions.add(r);
 		
 		// for test purposes, use first region as entry point;
 		if (entryPos == null)
 			entryPos = r.getCenter();
 	}
 
 	/**
 	 * return the number of regions defined in this space
 	 * 
 	 * @return number of defined regions
 	 */
 	public int numRegions() {
 		return regions.size();
 	}
 
 	/**
 	 * return a reference to the n'th defined region
 	 * 
 	 * @param num	index of the desired region
 	 * @return		reference to the desired region
 	 */
 	public Region getRegion( int num ) {
 		int n = 0;
 		Iterator<Region> it = regions.iterator();
 		while(it.hasNext()) {
 			Region r = (Region) it.next();
 			if (n == num)
 				return r;
 			n++;
 		}
 
 		throw new ArrayIndexOutOfBoundsException("illegal region index:" + num);
 	}
 
 	/**
 	 * return a reference to a named region
 	 * 
 	 * @param name	name of the desired region
 	 * @return		reference to the desired region
 	 */
 	public Region getRegion( String name ) {
 		Iterator<Region> it = regions.iterator();
 		while(it.hasNext()) {
 			Region r = (Region) it.next();
 			if (name.equals(r.getName()))
 				return r;
 		}
 
 		throw new ArrayIndexOutOfBoundsException("unknown region:" + name);
 	}
 
 	/**
 	 * check an actor's updated position against all regions and trigger
 	 * any appropriate actions
 	 * 
 	 * @param a			actor in question
 	 * @param newPosn	actor's new position
 	 * @return			whether or not any changes actually happened
 	 */
 	public boolean processPosition(Actor a, Coord newPosn) {
 
 		boolean changes = false;
 
 		// check each region if this triggers entry/exit rules
 		Iterator<Region> it = regions.iterator();
 		while(it.hasNext()) {
 			Region r = (Region) it.next();
 			changes |= r.processPosition(a, newPosn);
 		}
 
 		return changes;
 	}
 
 	/**
 	 * wrapper that assumes we want to use Y values
 	 */
 	public void readRegions( String path ) 
 			throws InvalidObjectException, ParserConfigurationException, URISyntaxException, IOException {
 		readRegions( path, false );
 	}
 	/**
 	 * initialize the region map from an XML description
 	 * 
 	 * @param path	name of description file
 	 * @param ignoreY whether or not we should ignore Y values
 	 * @return			true if initialization was successful
 	 * @throws ParserConfigurationException 
 	 * @throws IOException 
 	 * @throws URISyntaxException 
 	 */
 	public void readRegions( String path, boolean ignoreY )
 			throws ParserConfigurationException, URISyntaxException, IOException, InvalidObjectException {	
 
 		if (debugLevel > 0)
 			System.out.println("Loading regions from: " + path);
 
 		// create a parser and read the document
 		if (db == null) {
 			DocumentBuilderFactory dbf =
 					DocumentBuilderFactory.newInstance();
 			db = dbf.newDocumentBuilder();
 		}
 		URI uri = new URI(path);
 		
 		Document doc;
 		try {
 			doc = db.parse(uri.toString());
 		} catch (SAXException e) {
 			throw new IOException("XML parse error in " + path, e);
 		} catch (IOException e) {
 			throw new IOException("Unable to read " + path, e);
 		}
 		
 		// make sure it contains region descriptions
 		Element root = doc.getDocumentElement();
 		if (!root.getNodeName().equals("regions")) {
 			throw new InvalidObjectException(path + ": document type not 'regions'");
 		}
 		
 		// see if this region has a name
 		Node n = root.getAttributes().getNamedItem("name");
 		if (n != null) {
 			this.name = n.getNodeValue();
 			if (debugLevel > 1)
 				System.out.println("  Space: " + this.name);
 		}
 		
 		/* pull out the region descriptions */
 		for( n = root.getFirstChild(); 
 				n != null; 
 				n = n.getNextSibling() ) {
 			if (!n.getNodeName().equals("region"))
 				continue;
 			
 			// TODO (refactor) - parse region XML descriptions in Region.java
 
 			String name = n.getAttributes().getNamedItem("name").getNodeValue();
 			float radius = Float.parseFloat(n.getAttributes().getNamedItem("radius").getNodeValue());
 
 			/* find the position under each region */
 			for( Node p = n.getFirstChild();
 					p != null;
 					p = p.getNextSibling() ) {
 				if (!p.getNodeName().equals("position"))
 					continue;
 
 				float x = Float.parseFloat(p.getAttributes().getNamedItem("x").getNodeValue());
 				float y = ignoreY ? 0 : 
 					Float.parseFloat(p.getAttributes().getNamedItem("y").getNodeValue());
 				float z = Float.parseFloat(p.getAttributes().getNamedItem("z").getNodeValue());
 
 				/* register this region in this space */
 				Region r = new Region(name, new Coord(x,y,z), radius);
 				addRegion(r);
 				if (debugLevel > 1)
 					System.out.println("    Region: " + r);
 			}
 		}
 
 	}
 
 	/**
 	 * initialize the action rules from an XML description
 	 * 
 	 * @param path	name of description file
 	 * @return			true if initialization was successful
 	 * 
 	 * @throws ParserConfigurationException 
 	 * @throws IOException 
 	 * @throws URISyntaxException 
 	 */
 	public void readRules( String path ) 
 			throws IOException, URISyntaxException, ParserConfigurationException, InvalidObjectException {
 
 		if (debugLevel > 0)
 			System.out.println("Loading rules from: " + path);
 
 		// create a parser and read the document
 		if (db == null) {
 			DocumentBuilderFactory dbf =
 					DocumentBuilderFactory.newInstance();
 			db = dbf.newDocumentBuilder();
 		}
 		URI uri = new URI(path);
 		Document doc;
 		try {
 			doc = db.parse(uri.toString());
 		} catch (SAXException e) {
 			throw new IOException("XML parse error in " + path, e);
 		} catch (IOException e) {
 			throw new IOException("Unable to read " + path, e);
 		}
 
 		// make sure it contains rules
 		Element root = doc.getDocumentElement();
 		if (!root.getNodeName().equals("rules")) {
 			throw new InvalidObjectException(path + ": document type not 'rules'");
 		}
 
 		/* pull out the rule descriptions */
 		for( Node n = root.getFirstChild(); 
 				n != null; 
 				n = n.getNextSibling() ) {
 			if (!n.getNodeName().equals("rule"))
 				continue;
 			
 			Node x;
 			
 			// TODO (refactor) - parse rule XML descriptions in Rule.java
 
 			// create the RegionEvent callback handler
 			RegionEvent r = new RegionEvent();
 			for( Node p = n.getFirstChild();
 					p != null;
 					p = p.getNextSibling() ) {
 				if (p.getNodeName().equals("image")) {
 					x = p.getAttributes().getNamedItem("file");
 					r.setImage( (x == null) ? null : x.getNodeValue());
 				}
 				if (p.getNodeName().equals("sound")) {
 					x = p.getAttributes().getNamedItem("file");
 					r.setSound( (x == null) ? null : x.getNodeValue());
 				}
 				if (p.getNodeName().equals("text")) {
 					x = p.getAttributes().getNamedItem("file");
 					r.setText( (x == null) ? null : x.getNodeValue());
 				}
 			}
 
 			// gather the rule attributes and create the rule
 			String ruleName = n.getAttributes().getNamedItem("name").getNodeValue();
 			String s = n.getAttributes().getNamedItem("region").getNodeValue();
 			Region region = getRegion(s);
 			s = n.getAttributes().getNamedItem("event").getNodeValue();
 			Rule.EventType etype = Rule.eventType(s);
 			x = n.getAttributes().getNamedItem("state");
 			int iState = (x == null) ? -1 : Integer.parseInt(x.getNodeValue());
 			x = n.getAttributes().getNamedItem("next");			
 			int nState = (x == null) ? -1 : Integer.parseInt(x.getNodeValue());
 
 			new Rule(ruleName, region, etype, iState, nState, r);
 
 			if (debugLevel > 1) {
 				String descr = "    Rule:";
 				descr += " region=" + region.getName();
 				descr += " " + etype;
 				descr += ", name=" + ruleName;
 				descr += ", s=" + iState;
 				descr += ", n=" + nState;
 				System.out.println(descr);
 			}
 		}
 	}
 
 	/**
 	 * dump the configured regions for this space in XML
 	 * 
 	 * @return	String containing the saved regions
 	 */
 	public String regionsToXML() {
 		
 		String out = "<regions";
		if (name != null)
 			out += " name=\"" + name + "\"";
 		out +=">\n";
 		Iterator<Region> it = regions.iterator();
 		while(it.hasNext()) {
 			Region r = (Region) it.next();
 			out += r.toXML();
 		}
 		out += "</regions>\n";
 		
 		return out;
 	}
 	
 	/**
 	 * dump the configured rules for this space in XML
 	 * 
 	 * @return	String containing the saved rules
 	 */
 	public String rulesToXML() {
 		
 		String out = "<rules>\n";
 		Iterator<Region> it = regions.iterator();
 		while(it.hasNext()) {
 			Region r = (Region) it.next();
 			out += r.rulesToXML();
 		}
 		out += "</rules>\n";
 		
 		return out;
 	}
 	
 	/**
 	 * generate a pretty list of rules
 	 */
 	public String listRules() {
 		String out = "";
 		Iterator<Region> it = regions.iterator();
 		while(it.hasNext()) {
 			Region r = (Region) it.next();
 			out += r.listRules();
 		}
 		
 		return out;
 	}
 
 	/**
 	 * test entry-point to automatically walk a space
 	 * 
 	 * 	It is called frequently (e.g. from an applet update routine)
 	 * 	and each time is expected to walk the specified Actor one step
 	 * 	closer to his next goal.  To do this, it tries to keep track of
 	 * 	who we last moved and where he has been so far ... hence this
 	 *	is highly state-full code
 	 * 
 	 * @param actor to be moved
 	 * @return false if this actor is through with his tour
 	 */
 	public boolean test(Actor a) {
 
 		// if we are starting a new actor, put him at the first region
 		if (a != lastActor) {
 			a.lastPosition(entryPos);
 			lastRegion = 0;
 			lastActor = a;
 		}
 
 		// see if this actor has reached his latest goal
 		Coord posn = a.lastPosition();
 		Region r = getRegion(lastRegion);
 		Coord goal = r.getCenter();
 		if (goal.dist(posn) < 1.0F) {			// we've reached our goal
 			if (debugLevel > 1)
 				System.out.println("   ... Actor " + a + " at " + r.getName());
 			lastRegion++;
 			if (lastRegion >= numRegions()) {	// we've finished this walk
 				if (debugLevel > 1)
 					System.out.println("   ... Actor " + a + " visited all " + lastRegion + " regions");
 				lastActor = null;
 				return false;				// move on to next actor
 			}
 			return true;		// continue (with new goal) on the next call
 		}
 
 		// move one step closer (on each axis) to our next goal
 		float x = towards(posn.x, goal.x, step);
 		float y = towards(posn.y, goal.y, step);
 		float z = towards(posn.z, goal.z, step);
 		Coord nextPos = new Coord(x,y,z);
 		if (processPosition(a, nextPos) && debugLevel > 1)
 			System.out.println("   ... by moving from " + posn + " to " + nextPos);
 		a.lastPosition(nextPos);
 
 		return true;	// continue moving this actor
 	} 
 
 	/**
 	 * figure out what the next step is in moving towards a goal
 	 *    (used to guide test actor wandering)
 	 * 
 	 * @param current	coordinate
 	 * @param goal	coordinate
 	 * @param	step	maximum step distance
 	 * @return	next coordinate along the path
 	 */
 	private static float towards( float current, float goal, float step ) {
 		if (goal - current > step)
 			return( current + step );
 		else if (current - goal > step)
 			return( current - step );
 		else
 			return goal;
 	}
 }
