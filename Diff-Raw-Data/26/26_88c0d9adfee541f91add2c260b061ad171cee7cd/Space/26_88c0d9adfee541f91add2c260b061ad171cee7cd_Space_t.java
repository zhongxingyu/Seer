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
 	private String fileBase;		// prefix for fetched files
 	private int debugLevel;			// level of desired debug output
 	private MediaActions media;		// object for multi-media actions
 	private Boolean started;		// has the startup rule been invoked?
 	private Rule onStartup;			// rule for space startup
 	private Rule actorEntry;		// rule for new actor entry
 	private Rule actorExit;			// rule for actor exit
 	
 	// these are only used for testing (simulated actor walks)
 	private Coord entryPos;		// where new actors enter the scene
 	private Coord exitPos;		// where actors exit the scene
 	private Actor lastActor;	// last actor we were testing
 	private int lastRegion;		// number of regions completed for this actor
 	private final float step = 10.0F;	// test-walk step size (in mm)
 
 
 	public Space() {
 		regions = new LinkedList<Region>();
 		debugLevel = 1;			// basic debug info
 		name = null;			// we do not yet have a name
 		fileBase = null;		// we do not yet have a prefix
 		media = null;			// we do not yet have a media player
 		db = null;				// we have not yet created a parser
 		entryPos = null;		// we don't have any regions yet
 		exitPos = null;			// we don't have any regions yet
 		onStartup = null;		// no startup rule yet
 		actorEntry = null;		// no actor entry rule yet
 		actorExit = null;		// no actor exit rule yet
 		lastActor = null;		// we haven't tested any actors yet
 		lastRegion = 0;			// there are no walks in progress
 		started = false;		// we have not triggered the start-up rule
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
 	 * set the media file location prefix for this space
 	 * @param prefix
 	 */
 	public void prefix( String prefix ) {
 		this.fileBase = prefix;
 	}
 	
 	/**
 	 * set the display window for images in this space
 	 * 
 	 * @param container
 	 */
 	public void media( MediaActions mediaPlayer ) {
 		media = mediaPlayer;
 	}
 	
 	/**
 	 * add a new region to the space
 	 * 
 	 * @param r	region to be added
 	 */
 	public void addRegion(Region r) {
 		regions.add(r);
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
 	 * add a new actor to the space
 	 * 
 	 * @param a	the new actor
 	 */
 	public void addActor(Actor a) {
 		if (!started) {
 			if (onStartup != null)
 				onStartup.checkTriggered(null, Rule.EventType.STARTUP, media);
 			started = true;
 		}
 		if (actorEntry != null)
 			actorEntry.checkTriggered(a,  Rule.EventType.ENTRY,  media);
 	}
 	
 	/**
 	 * remove an actor from the space
 	 * 
 	 * @param a	the new actor
 	 */
 	public void dropActor(Actor a) {
 		if (actorExit != null)
 			actorExit.checkTriggered(a,  Rule.EventType.EXIT,  media);
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
 
 		// see if we have processed the start-up rule yet
 		if (!started) {
 			if (onStartup != null)
 				onStartup.checkTriggered(null, Rule.EventType.STARTUP, media);
 			started = true;
 		}
 		
 		// if this was just a start-up event, no other rules will trigger
 		if (a == null || newPosn == null)
 			return false;
 
 		// check each region if this triggers entry/exit rules
 		boolean changes = false;
 		Iterator<Region> it = regions.iterator();
 		while(it.hasNext()) {
 			Region r = (Region) it.next();
 			changes |= r.processPosition(a, newPosn, media);
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
 	 * @param path	ABSOLUTE path name of description file
 	 * 		because this is specified as a distinct parameter to the
 	 * 		program, I decided not to make it relative to fildBase
 	 * 
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
 			throw new IOException("Unable to open/read " + path, e);
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
 			
 			// CLEANUP - parse region XML descriptions in Region.java
 
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
 	 * @param path	ABSOLUTE path name of description file
 	 * 	 	because this is specified as a distinct parameter to the
 	 * 		program, I decided not to make it relative to fildBase		
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
 			throw new IOException("Unable to open/read " + path, e);
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
 			String value;
 			
 			// CLEANUP - parse rule XML descriptions in Rule.java
 
 			// create the RegionEvent callback handler
 			//		note that all media file names are to be interpreted relative
 			//		to a base prefix we got at start up time
 			RegionEvent r = new RegionEvent();
 			for( Node p = n.getFirstChild();
 					p != null;
 					p = p.getNextSibling() ) {
 				if (p.getNodeName().equals("image")) {
 					x = p.getAttributes().getNamedItem("file");
 					if (x != null) {
 						value = x.getNodeValue();
 						if (fileBase != null && !value.equals("cancel"))
 							value = fileBase + "/" + value;
 					} else
 						value = null;
 					r.setImage( value );
 				}
 				if (p.getNodeName().equals("sound")) {
 					x = p.getAttributes().getNamedItem("file");
 					if (x != null) {
 						value = x.getNodeValue();
 						if (fileBase != null && !value.equals("cancel"))
 							value = fileBase + "/" + value;
 					} else
 						value = null;
 					r.setSound( value );
 				}
 				if (p.getNodeName().equals("text")) {
					// TODO - add support for size/color/font/style parms
 					x = p.getAttributes().getNamedItem("file");
 					if (x != null) {
 						value = x.getNodeValue();
 						if (fileBase != null && !value.equals("cancel"))
 							value = fileBase + "/" + value;
 					} else
 						value = null;
 					r.setText( value );
 				}
 			}
 
 			// gather the rule attributes and create the rule
 			String ruleName = n.getAttributes().getNamedItem("name").getNodeValue();
 			String s = n.getAttributes().getNamedItem("event").getNodeValue();
 			Rule.EventType etype = Rule.eventType(s);
 			x = n.getAttributes().getNamedItem("state");
 			int iState = (x == null) ? -1 : Integer.parseInt(x.getNodeValue());
 			x = n.getAttributes().getNamedItem("next");			
 			int nState = (x == null) ? -1 : Integer.parseInt(x.getNodeValue());
 			s = n.getAttributes().getNamedItem("region").getNodeValue();
 			if (!s.equals("NONE")) {
 				Region region = getRegion(s);
 				new Rule(ruleName, region, etype, iState, nState, r);
 			} else if (etype == Rule.EventType.ENTRY) {
 				actorEntry = new Rule(ruleName, null, etype, -1, -1, r);
 			} else if (etype == Rule.EventType.EXIT) {
 				actorExit = new Rule(ruleName, null, etype, -1, -1, r);
 			} else if (etype == Rule.EventType.STARTUP) {
 				onStartup = new Rule(ruleName, null, etype, -1, -1, r);
 			}
 
 			if (debugLevel > 1) {
 				String descr = "    Rule:";
 				descr += " region=" + s;
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
 		// not all regions have names
 		if (name != null && !name.equals(""))
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
 		
 		// dump out the pan-region rules
 		if (onStartup != null)
 			out += onStartup.toXML();
 		if (actorEntry != null)
 			out += actorEntry.toXML();
 		if (actorEntry != null)
 			out += actorEntry.toXML();
 		
 		// then dump out the rules in each defined region
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
 
 		// figure out where tests should start and end
 		if (entryPos == null || exitPos == null) {
 			float farLeft = -1;
 			float farRight = 1;
 			float firstX = 0;
 			float lastX = 0;
 
 			// run through all the regions noting left/right expanse
 			for( int i = 1; i < this.numRegions(); i++) {
 				Region r = this.getRegion(i);
 				if (r == null)
 					break;
 				float x = r.getCenter().x;
 				if (x - r.getRadius() < farLeft)
 					farLeft = x - r.getRadius();
 				if (x + r.getRadius() > farRight)
 					farRight = x + r.getRadius();
 				if (firstX == 0 && x != 0)
 					firstX = x;
 				else if (lastX == 0 && x != 0)
 					lastX = x;
 			}
 			
 			// assign entry and exit positions to be outside of this range
 			float border = 100;		// how far outside the range
 			if (firstX > lastX) {
 				entryPos = new Coord(farRight + border, 0, 0);
 				exitPos = new Coord(farLeft - border, 0, 0);
 			} else {
 				exitPos = new Coord(farRight + border, 0, 0);
 				entryPos = new Coord(farLeft - border, 0, 0);
 			}
 			
 			// and trigger the start-up processing
 			processPosition(null, null);
 		}
 		
 		Coord posn;			// actor's current position
 		Coord goal;			// actor's next goal
 		String goal_name;	// name of that goal
 		
 		// if we are starting a new actor, put him at the entry point
 		if (a != lastActor) {
 			posn = entryPos;
 			goal = entryPos;
 			goal_name = "ENTRANCE";
 			a.lastPosition(entryPos);
 			addActor(a);
 			lastActor = a;
 			lastRegion = 0;		
 		} else if (lastRegion < numRegions()) {
 			posn = a.lastPosition();
 			Region r = getRegion(lastRegion);
 			goal = r.getCenter();
 			goal_name = r.getName();
 		} else {
 			posn = a.lastPosition();
 			goal = exitPos;
 			goal_name = "EXIT";
 		}
 
 		// has this actor yet reached that goal
 		if (goal.dist(posn) < 1.0F) {			// we've reached our goal
 			if (debugLevel > 1)
 				System.out.println("   ... Actor " + a + " at " + goal_name);
 			lastRegion++;
 			if (lastRegion > numRegions()) {	// we've finished this walk
 				if (debugLevel > 1)
 					System.out.println("   ... Actor " + a + " visited all " + lastRegion + " regions");
 				dropActor(lastActor);
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
