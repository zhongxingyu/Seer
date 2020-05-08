 package nl.nikhef.jgridstart;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Properties;
 
 public class Organisation extends Properties {
     
     ///
     /// Static members
     ///
 
     /** global list of all organisations */
     protected static HashMap<String, Organisation> organisations = null;
     
     /** Get an organisation by identifier */
     public static Organisation get(String org) {
 	if (organisations==null) readAll();
 	return organisations.get(org);
     }
     /** Return the list of organisations */
     public static Organisation[] getAll() {
 	if (organisations==null) readAll();
 	return organisations.values().toArray(new Organisation[0]);
     }
     /** Load the list of organisations from the configuration file */
     protected static void readAll() {
 	// load organisations
 	Properties allProps = new Properties();
 	try {
 	    allProps.load(Organisation.class.
 		    getResourceAsStream("/resources/conf/organisations.properties"));
 	} catch (IOException e) {
 	    // TODO Auto-generated catch block
 	    e.printStackTrace();
 	}
 	// parse into objects
 	organisations = new HashMap<String, Organisation>();
 	Enumeration<Object> e = allProps.keys();
 	while (e.hasMoreElements()) {
 	    String key = (String)e.nextElement();
 	    String[] keyParts = key.split("\\.", 2);
 	    if (!organisations.containsKey(keyParts[0]))
 		organisations.put(keyParts[0], new Organisation(keyParts[0]));
 	    organisations.get(keyParts[0]).setProperty(keyParts[1], allProps.getProperty(key));
 	}
     }
     
     /** Returns a list of &gt;option&lt; elements to put in an html select */
     public static String getAllOptionsHTML() {
 	if (organisations==null) readAll();
 	String r = "";
 	for (Iterator<Organisation> it = organisations.values().iterator(); it.hasNext(); ) {
 	    r += it.next().getOptionHTML()+"\n";
 	}
 	return r;
     }
 
     ///
     /// Non-static members
     ///
     
     /** Create a new origanisation */
     protected Organisation(String id) {
 	setProperty("id", id);
     }
     
     /** Returns an organisation property. If the key is not found in this organisation,
      * the organisation pointed by "ref" is tried, when available.
      * 
      * This is done instead of using a parent property to allow referencing organisations
      * that are defined later in the configuration file. */
     @Override
     public String getProperty(String key) {
 	String val = super.getProperty(key);
 	if (val==null && containsKey("ref"))
	    val = organisations.get(getProperty("ref")).getProperty(key);
 	return val;
     }
     
     /** Returns the formatted postal address for this organisation */
     public String getAddress() {
 	String r =
 	    getProperty("ra.name") + "\n" +
 	    getProperty("ra.address").replaceAll(",\\s+", "\n") + "\n";
 	if (getProperty("ra.fax")!=null)
 	    r += "\nfax: " + getProperty("ra.fax") + "\n";
 	return r;
     }
 
     /** Returns the organisation's name in html */
     public String getNameHTML() throws UnsupportedEncodingException {
 	String r = getProperty("desc");
 	if (r==null) r = getProperty("name");;
 	if (getProperty("url")!=null)
 	    r = "<a href='"+URLEncoder.encode(getProperty("url"), "UTF-8")+"'>"+r+"</a>";
 	return r;
     }
     
     /** Returns an html &gt;option&lt; tag for embedding in a &gt;select&lt; tag */
     public String getOptionHTML() {
 	return 
 		"<option value='"+getProperty("id")+"'>" +
 		getProperty("desc")+
 		"</option>";
     }
 }
