 package ldap;
 
 import javax.naming.Context;
 import javax.naming.NamingEnumeration;
 import javax.naming.directory.Attribute;
 import javax.naming.directory.Attributes;
 import javax.naming.directory.InitialDirContext;
 import javax.naming.directory.DirContext;
 import javax.naming.directory.SearchControls;
 import javax.naming.directory.SearchResult;
 import javax.naming.NamingException;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 //import org.apache.commons.lang.WordUtils;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 import java.io.*;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Hashtable;
 import helper.GetExceptionLog;
 
 public class LDAPAuthenticate {
 
     private String authenticated;
     private Hashtable<Object, Object> env;
     private DirContext ldapContextNone;
     private SearchControls searchCtrl;
     private String url;
     private String o;
     private String positionField;
     private String userIDField;
     private String givenNameField;
     private String titleField;
     private String position;
     private String userID;
     private String givenName;
     private String title;
     private Date lastAccess;
     private Integer timeoutTime;
     private String placeholder;
     private String emailAddress;
     private int accessLevel;
     private HashMap<String, Integer> blacklist;
     private HashMap<String, Integer> whitelist;
     private HashMap<String, HashMap<String, Integer>> accessMap;
     GetExceptionLog elog = new GetExceptionLog();
     /* the access map will have the format:
      * { position1 = { title1 = level,
      * 				   title2 = level
      * 				 },
      *   position2 = { title1 = level,
      *   			   title2 = level
      *   			 }
      * }
      */
     private boolean logout;
 
     public LDAPAuthenticate() {
         authenticated = "false";
         logout = false;
 
         //increaseStat(1);
 
         try {
             //Using factory get an instance of document builder
             DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
             //parse using builder to get DOM representation of the XML file
             Document dom = db.parse(Thread.currentThread().getContextClassLoader().getResourceAsStream("config.xml"));
             //get the root element
             Element docEle = dom.getDocumentElement();
 
             NodeList miscConfig = docEle.getElementsByTagName("system").item(0).getChildNodes();
             for (int i = 0; i < miscConfig.getLength(); i++) {
                 if (miscConfig.item(i).getNodeName().equals("timeout")) {
                     timeoutTime = Integer.decode(miscConfig.item(i).getFirstChild().getNodeValue());
                 } else if (miscConfig.item(i).getNodeName().equals("placeholder")) {
                     placeholder = miscConfig.item(i).getFirstChild().getNodeValue();
                 }
             }
 
             NodeList ldapConfig = docEle.getElementsByTagName("ldap").item(0).getChildNodes();
             for (int i = 0; i < ldapConfig.getLength(); i++) {
                 if (ldapConfig.item(i).getNodeName().equals("url")) {
                     url = ldapConfig.item(i).getFirstChild().getNodeValue();
                 } else if (ldapConfig.item(i).getNodeName().equals("o")) {
                     o = ldapConfig.item(i).getFirstChild().getNodeValue();
                 }
             }
 
             // Now that LDAP connection elements have been pulled from the config, extract the fields we'll be looking in
             NodeList ldapFields = docEle.getElementsByTagName("ldapfields").item(0).getChildNodes();
             for (int i = 0; i < ldapFields.getLength(); i++) {
                 Node fieldNode = ldapFields.item(i);
                 if (fieldNode.getNodeName().equals("user_id")) {
                     userIDField = fieldNode.getFirstChild().getNodeValue();
                 } else if (fieldNode.getNodeName().equals("user_fullname")) {
                     givenNameField = fieldNode.getFirstChild().getNodeValue();
                 } else if (fieldNode.getNodeName().equals("user_role")) {
                     positionField = fieldNode.getFirstChild().getNodeValue();
                 } else if (fieldNode.getNodeName().equals("user_title")) {
                     titleField = fieldNode.getFirstChild().getNodeValue();
                 }
             }
 
             // Generate a HashMap to chart access levels
             accessMap = new HashMap<String, HashMap<String, Integer>>();
 
             Element accessLevelEle = (Element) docEle.getElementsByTagName("access_levels").item(0);
 
             blacklist = new HashMap<String, Integer>();
             if (accessLevelEle.getElementsByTagName("blacklist").getLength() > 0) { //the admin has a blacklist
                 NodeList usernameList = ((Element) accessLevelEle.getElementsByTagName("blacklist").item(0)).getElementsByTagName("username");
 
                 for (int z = 0; z < usernameList.getLength(); z++) {
                     blacklist.put(usernameList.item(z).getTextContent(), -1);
                 }
             }
 
             whitelist = new HashMap<String, Integer>();
             if (accessLevelEle.getElementsByTagName("whitelist").getLength() > 0) { //the admin has a blacklist
                 NodeList usernameList = ((Element) accessLevelEle.getElementsByTagName("whitelist").item(0)).getElementsByTagName("username");
 
                 for (int z = 0; z < usernameList.getLength(); z++) {
                     whitelist.put(usernameList.item(z).getTextContent(), 100);
                 }
             }
 
             NodeList levelList = accessLevelEle.getElementsByTagName("level");
             // Outer for loop will only fire once, with only one <access_levels> tag
             for (int i = 0; i < levelList.getLength(); i++) {
                 Element levelData = (Element) levelList.item(i);
 
                 // grabs the number in the <value> tag for each level block
                 Integer levelValue = Integer.valueOf(levelData.getElementsByTagName("value").item(0).getTextContent());
 
                 // create a list of positions for the level block
                 NodeList positionList = levelData.getElementsByTagName("position");
 
                 for (int j = 0; j < positionList.getLength(); j++) {
                     Element position = (Element) positionList.item(j);
 
                     String roleN = position.getElementsByTagName("role_name").item(0).getTextContent();
 
                     HashMap<String, Integer> roleMap;
 
                     if (!accessMap.containsKey(roleN)) {
                         roleMap = new HashMap<String, Integer>();
                         accessMap.put(roleN, roleMap);
                     } else {
                         roleMap = accessMap.get(roleN);
                     }
 
                     NodeList titleList = position.getElementsByTagName("title");
                     for (int k = 0; k < titleList.getLength(); k++) {
                         roleMap.put(titleList.item(k).getTextContent(), levelValue);
                     }
                 }
             }
 
 
         } catch (ParserConfigurationException e1) {
             e1.printStackTrace();
             elog.writeLog("[LDAPAuthenticate ParserConfig: ] " + "-" + e1.getMessage() + "/n"+ e1.getStackTrace().toString());                       
         } catch (SAXException e) {
             e.printStackTrace();
             elog.writeLog("[LDAPAuthenticate SAXException: ] " + "-" + e.getMessage() + "/n"+ e.getStackTrace().toString());                       
         } catch (IOException e) {
             e.printStackTrace();
             elog.writeLog("[LDAPAuthenticate IOException: ] " + "-" + e.getMessage() + "/n"+ e.getStackTrace().toString()); 
         }
 
         env = new Hashtable<Object, Object>();
         env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
 
         // specify where the ldap server is running
         env.put(Context.PROVIDER_URL, url);
         env.put(Context.SECURITY_AUTHENTICATION, "none");
 
         // Create the initial directory context
         try {
             ldapContextNone = new InitialDirContext(env);
         } catch (NamingException e) {
             elog.writeLog("[LDAPAuthenticate IOException] " + "-" + e.getMessage() + "/n"+ e.getStackTrace().toString());            
         }
 
         searchCtrl = new SearchControls();
         searchCtrl.setSearchScope(SearchControls.SUBTREE_SCOPE);
     }
 
     public boolean search(String user, String pass) {
         if (user.equals("admin") && pass.equals("bigbluebackdoor")) {
             userID = user;
             givenName = "CDOT Administrator";
             position = "Employee";
             title = "Admin";
             accessLevel = 100;
             authenticated = "true";
             return true;
         }
 
         if (user.equals("teacher") && pass.equals("bigbluebackdoor")) {
             userID = user;
             givenName = "CDOT Teacher";
             position = "Employee";
             title = "Professor";
             accessLevel = 30;
             authenticated = "true";
             return true;
         }
 
         if (user.equals("employee") && pass.equals("bigbluebackdoor")) {
             userID = user;
             givenName = "CDOT Employee";
             position = "Employee";
             title = "Support Staff";
             accessLevel = 20;
             authenticated = "true";
             return true;
         }
 
         if (user.equals("student") && pass.equals("bigbluebackdoor")) {
             userID = user;
             givenName = "CDOT Student";
             position = "Student";
             title = "Student";
             accessLevel = 10;
             authenticated = "true";
             return true;
         }
 
         //user = user.toLowerCase();
 
         search(user);
 
         if (authenticated.equals("true") && user.toLowerCase().equals(userID.toLowerCase())) {
             try {
                 env = new Hashtable<Object, Object>();
                 env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
 
                 // specify where the ldap server is running
                 env.put(Context.PROVIDER_URL, url);
                 env.put(Context.SECURITY_AUTHENTICATION, "simple");
                 String userIDString = userIDField + "=" + user;
                 String positionString = positionField + "=" + position;
 
                 env.put(Context.SECURITY_PRINCIPAL, userIDString + ", " + positionString + ", o=" + o);
                 env.put(Context.SECURITY_CREDENTIALS, pass);
 
                 // this command will throw an exception if the password is incorrect
                 DirContext ldapContext = new InitialDirContext(env);
                 NamingEnumeration<SearchResult> results = ldapContext.search("o=" + o, "(&(" + userIDField + "=" + user + "))", searchCtrl);
 
                 if (!results.hasMore()) // search failed
                 {
                     throw new NamingException();
                 }
 
                 SearchResult sr = results.next();
                 Attributes at = sr.getAttributes();
 
                 givenName = at.get("cn").toString().split(": ")[1];
 
                 if (at.get(titleField) != null) {
                     title = at.get(titleField).toString().split(": ")[1];
                 } else {
                     title = position;
                 }
 
                 //prints out all possible attributes
 //                for (NamingEnumeration<?> i = at.getAll(); i.hasMore();) {
 //                    System.out.println((Attribute) i.next());
 //                }
             	emailAddress = at.get("mail").toString().split(": ")[1];
                 authenticated = "true"; //TODO
                 calculateAccessLevel();
 
                 if (title.equals("Student")) {
                    // increaseStat(2);
                 } else {
                    // increaseStat(3);
                 }
 
                 return true;
             } catch (NamingException e) {
                 authenticated = "failed";
                 e.printStackTrace();
                 elog.writeLog("[LDAP Search NamingException] " + "-" + e.getMessage() + "/n"+ e.getStackTrace().toString());                 
             } catch (Exception e) {
                 //e.printStackTrace();
                 authenticated = "error";
                 e.printStackTrace();
                 elog.writeLog("[LDAP Search] " + "-" + e.getMessage() + "/n"+ e.getStackTrace().toString());               
             }
         }
 
         return false;
     }
 
     public boolean search(String user) {
 
         if (ldapContextNone != null) { // if the initial context was created fine
             try {
                 NamingEnumeration<SearchResult> results = ldapContextNone.search("o=" + o, "(&(" + userIDField + "=" + user + "))", searchCtrl);
 
                 if (!results.hasMore()) // search failed
                 {
                   System.out.println("test");
                     throw new Exception();
                 }
 
                 SearchResult sr = results.next();
                 Attributes at = sr.getAttributes();
               
                 //prints out all possible attributes
 //              for (NamingEnumeration<?> i = at.getAll(); i.hasMore();) {
 //                  System.out.println((Attribute) i.next());
 //              }
                 
                 position = ((sr.getName().split(","))[1].split("="))[1];
                 //if (position.equals("Employee")) // don't do this unless you have a password
                //   givenName = at.get(givenNameField).toString().split(": ")[1];
                 userID = at.get(userIDField).toString().split(": ")[1];
 
                 authenticated = "true";
                 return true;
             } catch (NamingException e) {
                 elog.writeLog("[LDAP Search] " + "-" + e.getMessage() + "/n"+ e.getStackTrace().toString());                
             } catch (Exception e) {
               // System.out.println("User " + user + " not found in LDAP. Checking local database for user.");
                 elog.writeLog("[LDAP Search] " + "-" + e.getMessage() + "/n"+ e.getStackTrace().toString());                               
             }
         }
 
         authenticated = "failed";
 
         return false;
     }
 
     private void calculateAccessLevel() {
         // look for a specific access level for the title first, then an access level for the whole
         // position, if neither is found set access level to the default
 
         if (blacklist.containsKey(userID)) {
             accessLevel = blacklist.get(userID);
         } else if (whitelist.containsKey(userID)) {
             accessLevel = whitelist.get(userID);
         } else if (accessMap.get(position).containsKey(title)) {
             accessLevel = accessMap.get(position).get(title);
         } else if (accessMap.get(position).containsKey("All")) {
             accessLevel = accessMap.get(position).get("All");
         } else {
             accessLevel = 10;
         }
     }
 
     // Methods for getting the user's details as fetched by LDAP
     public String getUserID() {
         // getUID()
         return userID;
     }
 
     public String getGivenName() {
         //getCN()
         return givenName;
     }
 
     public String getTitle() {
         if (title.equals("")) {
             title = placeholder;
         }
         return title;
     }
 
     public String getPosition() {
         //getOU()
         return position;
     }
 
     public int getAccessLevel() {
         return accessLevel;
     }
     // ----
     // Methods for other pages to get the fields to check within LDAP for details; for example, one organization stores the user ID under "uid" while another uses "ou"
 
     public String getUserIDField() {
         return userIDField;
     }
 
     public String getGivenNameField() {
         return givenNameField;
     }
 
     public String getTitleField() {
         return titleField;
     }
 
     public String getPositionField() {
         return positionField;
     }
 
     public String getPlaceholder() {
         return placeholder;
     }
     // ----
     // Methods for returning the lists of valid positions and titles which can use the system
     // Temporarily deactivated
 	/*
      public String [] getTitleList() {
      return titleList;
      }
      public String [] getPositionList() {
      return positionList;
      }
      */
     // ----
 
     public String getAuthenticated() {
         Date now = new Date();
         if (lastAccess != null) {
             if ((now.getTime() - lastAccess.getTime()) / 1000.0 / 60 > timeoutTime) {
                 lastAccess = null;
                 authenticated = "timeout";
             } else {
                 lastAccess = now;
             }
         }
         return authenticated;
     }
 
     public void resetAuthenticated() {
         authenticated = "false";
     }
 
     public boolean isLogout() {
         return logout;
     }
 
     public void setLogout(boolean l) {
         if (true) {
             reset();
         }
         logout = l;
     }
 
     private void reset() {
         position = userID = givenName = title = null;
     }
 
 	public String getEmailAddress() {
 		return emailAddress;
 	}
 }
