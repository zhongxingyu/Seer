 package mingleplugin;
 
 import hudson.Extension;
 import hudson.Util;
 import hudson.model.AbstractDescribableImpl;
 import hudson.model.AbstractProject;
 import hudson.model.Descriptor;
 import hudson.model.Hudson;
 
 import java.net.URL;
 import java.net.MalformedURLException;
 import java.util.regex.Pattern;
 import java.util.regex.PatternSyntaxException;
 import java.util.Scanner;
 import java.util.NoSuchElementException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.Arrays;
 import java.util.List;
 import java.util.ArrayList;
 import java.net.HttpURLConnection;
 import java.net.ProtocolException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.InputStream;
 import java.io.IOException;
 import java.lang.IllegalArgumentException;
 import java.lang.IllegalStateException;
 
 //import javax.servlet.ServletException;
 
 import hudson.util.FormValidation;
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.QueryParameter;
 import com.thoughtworks.xstream.*;
 import com.thoughtworks.xstream.io.xml.StaxDriver;
 
 /**
  * Class for the Mingle connection using the API v2.
  *
  * @author Birk Brauer
  * @version 0.7
  */
 public class MingleRestService extends AbstractDescribableImpl<MingleRestService> {
 
   /**
    * Regexp pattern that identifies Mingle Card.
    * If this pattern changes help pages (help-issue-pattern_xy.html) must be updated 
    * <p>
    * First character must be the character #, then digits.
    * See #392 and #404
    */
   protected static Pattern DEFAULT_CARD_PATTERN = Pattern.compile("#([0-9]+)");
 
 
   /**
    * URL of mingle, like <tt>http://mingle:80/</tt>.
    * Mandatory. Normalized to end with '/'
    */
   public final URL url;
 
   /**
    * User name needed to login.
    */
   public final String userName;
 
   /**
    * Password needed to login.
    */
   public final String password;
 
   /**
    * Mingle project name. e.g. "scrum".
    */
   public final String project;
 
   /**
    * user defined pattern
    */    
   private final String userPattern;
   
   private transient Pattern userPat;
 
   /**
    * True if this mingle is configured to allow Confluence-style Wiki comment. Wait? Wat?
    */
   public final boolean supportsWikiStyleComment;
 
   // XStream set up:
   XStream xstream = new XStream(new StaxDriver());
 
   /**
    * conected mingle card... saved HERE?? Or inside the MingleBuildAction?
    * right now: saved inside the Build, but:
    * TODO: How to access the Build from here?
    */
 
 
   // This was needed because a URL in JSON is just a String!
   public MingleRestService(String url, String userName, String password, String project, String userPattern, boolean supportsWikiStyleComment) throws MalformedURLException {
     this(new URL(url), userName, password, project, userPattern, supportsWikiStyleComment);
   }
 
   @DataBoundConstructor
   public MingleRestService(URL url, String userName, String password, String project, String userPattern, boolean supportsWikiStyleComment) {
 
     xstream.alias("card", MingleCard.class);
     xstream.alias("property", MingleCardProperty.class);
     xstream.alias("project", MingleProject.class);
     xstream.alias("user", MingleUser.class);
 
     if(!url.toExternalForm().endsWith("/")) {
      try { 
         url = new URL(url.toExternalForm()+"/");
       } catch (MalformedURLException e) {
         throw new AssertionError(e); // impossible
       }
     }
     this.url = url;
 
     this.userName = (userName == "") ? null : userName;
     this.password = (password == "") ? null : password;
     this.supportsWikiStyleComment = supportsWikiStyleComment;
 
     this.userPattern = Util.fixEmpty(userPattern);
     if (this.userPattern != null) {
       this.userPat = Pattern.compile(this.userPattern);
     } else {
       this.userPat = null;
     }
 
     if (project == null || project == "") project = null; // if project is empty we still can get the projects from the server
     else project = project.replaceAll("\\W", "_").toLowerCase();
     this.project = project;
   }
 
   /**
    * Gets the effective {@link MingleRestService} associated with the given project.
    *
    * @return null
    *      if no such was found.
    */
   public static MingleRestService get(AbstractProject<?,?> p) {
       MingleProjectProperty jpp = p.getProperty(MingleProjectProperty.class);
       if(jpp!=null) {
           MingleRestService site = jpp.getSite();
           if(site!=null)
               return site;
       }
       // none is explicitly configured. try the default ---
       // if only one is configured, that must be it.
       MingleRestService[] sites = MingleProjectProperty.DESCRIPTOR.getSites();
       if(sites.length==1) return sites[0];
       return null;
   }
 
   public String getName() {
     return url.toExternalForm();
   }
 
   public URL getUrl() {
     return url;
   }
 
 /**
  * Generates a URL for the mingle REST call.
  * 
  * @return URL returns a URL including username and password.
  *
  * @throws MalformedURLException thrown if there is a error inside any part of the URL.
  */
   public URL generateRestUrl(String action) throws MalformedURLException {
     // if the given path already has a protocol included it's mostlikly already a valid URL
     if (action.indexOf("://") != -1) {
       try {
         URL url_t = new URL(action);
         return url_t;
       } catch (MalformedURLException e) {
         // if not, do nothing just let rest of the function generate a URL
         // maybe it's just part of a parameter like "?img=http://""
       }
     }
 
     String url_s;
 
     int port = url.getPort();
     if (port == -1) port = url.getDefaultPort(); // if no port set use default
     if (port == -1) port = 80; // if no default is set for protocol use 80 because it's a REST call
 
     url_s = url.getProtocol()+"://"+url.getHost()+":"+port;
 
     // if a path is given add it:
     if (!url.getPath().equals("") && !url.getPath().equals("/") ) {
       url_s += "/"+url.getPath();
     }
 
     if (action.charAt(0) == '/') action = action.substring(1);
     url_s += "/api/v2/"+action;
 
     return new URL(url_s);
   }
 
 /**
  * Generates a URL for link to the mingle system. The user have to be logged in to see the card.
  * 
  * @return URL returns a URL to the mingle system.
  *
  * @throws MalformedURLException thrown if there is a error inside any part of the URL.
  */
   public URL getCardUrl(int cardnumber) throws MalformedURLException {
     if (project == null) throw new MalformedURLException("No project given yet");
 
     String url_s;
     String protocol = url.getProtocol();
 
     int port = url.getPort();
     if (port == -1) port = url.getDefaultPort(); // if no port set use default
     if (port == -1) port = 80; // if no default is set for protocol use 80 because it's a REST call
 
     if ( !(protocol.equals("http") || protocol.equals("https")) ) protocol = "http";
     
     url_s = protocol+"://"+url.getHost()+":"+port;
 
     if (! "".equals(url.getPath()) ) {
       url_s += "/"+url.getPath();
     }
 
     url_s += "/projects/"+project+"/cards/"+cardnumber;
 
     return new URL(url_s);
   }
 
 
 /**
  * Gets a mingle card by it's unique number.
  *
  * @param int number The unique number of the requested card.
  * 
  * @return MingleCard Returns a mingle card by it's unique number. Returns null if the request failed or the URL is wrong.
  */
   public MingleCard getCard(int number) {
     if (project == null) return null;
 
     String xml;
 
     try {
       xml = doMingleCall("projects/"+project+"/cards/"+number+".xml", "GET" , null);
     } catch (MalformedURLException e) {
       return null;
     }
 
     // convert XML to some kind of useful MingleCart or MingleSomething-object using XStream:
     MingleCard card = (MingleCard)xstream.fromXML(xml);
     return card;
   }
 
 /**
  * Overrides a card on the mingle server with the given card.
  *
  * @param number The unique number of the requested card.
  * @param card The new mingle card that should replace the version on the mingle server.
  *
  * @trows IllegalArgumentException throws an IllegalArgumentException if any of the passed parameters is invalid.
  */
   public void updateCardByNumber(int number, MingleCard card) throws IllegalArgumentException {
     if (project == null) throw new IllegalArgumentException("No project given yet");
 
     // Converts a MingleCard to a XML String.
     String xml = xstream.toXML(card);
     try {
       URL url = new URL (doMingleCall("projects/"+project+"/cards/"+number+".xml", "PUT", xml));
     } catch (MalformedURLException e) {
       throw new IllegalArgumentException();
     }
   }
 
 /**
  * Creates a new empty card on the mingle server. The simple way, close to the Mingle API.
  *
  * @param String name The name of the new mingle card that should be created on the server.
  * @param String cardtype The cardtype of the new mingle card that should be created on the server.
  *
  * @return URL The URL of the new created card will be returned.
  *
  * @throws MalformedURLException thrown if the returned URL by mingle is invalid.
  */
   public URL createEmptyCard(String name, String cardtype) throws MalformedURLException {
     if (project == null) throw new MalformedURLException("No project given yet");
 
     // generates XML-string for card creation:
     String xml = "<card><name>"+name+"</name><card_type_name>"+cardtype+"</card_type_name></card>";
 
     // creates a new card:
     URL url = new URL(doMingleCall("projects/"+project+"/cards.xml", "POST", xml));
     
     return url;
   }
 
 /**
  * Creates a new card on the mingle server.
  *
  * @param MingleCard card The new mingle card that should be created on the server.
  *
  * @return int The unique card number of the new created card will be returned. Returns -1 if the creation failed.
  */
   public int createCard(MingleCard card) {
 
     URL url;
     try {
       // creates a new card:
       url = createEmptyCard(card.getName(), card.getCardtype());
     } catch (MalformedURLException e) {
       return -1;
     }
     
     // get cardnumber out of url:
     String urlpath = url.getPath();
     int cardnumber = Integer.parseInt(urlpath.substring(urlpath.lastIndexOf("/cards/")+7, urlpath.lastIndexOf(".xml")));
     // int cardnumber = (int)urlpath.substring(urlpath.lastIndexOf("/cards/")+7, urlpath.lastIndexOf(".xml") - (urlpath.lastIndexOf("/cards/")+7));
     
     try {
       // updates the new created card with the passed content:
       updateCardByNumber(cardnumber, card);
     } catch (IllegalArgumentException e) {
       deleteCardByNumber(cardnumber);
       return -1;
     }
 
     return cardnumber;
   }
 
   public void deleteCardByNumber(int number) {
     try {
       URL url = new URL (doMingleCall("projects/"+project+"/cards/"+number+".xml", "DELETE", null));
     } catch (NullPointerException e) {
       // nix, da kein Project angegeben
     } catch (MalformedURLException e) {
       // nix
     }
     //TODO: delte card in local Java cache... but there is no cache yet?
   }
 
   //TODO: Do we need: Method getListOfCards(view, page, filters[], sort, order, tagged_with ) and so on?
   //TODO: Create filter with MQL for /projects/new/cards/execute_mql.xml --> http://www.thoughtworks-studios.com/docs/mingle/12.2/help/mingle_api_execute_mql.html
 
 /**
  * Gets all projects from the server
  *
  * @return List<MingleProject> ArrayList of Strings with the names of all available projects on this mingle server
  */
   public List<MingleProject> getProjects() {
     String url_s;
     List projects = new ArrayList<MingleProject>();
 
     try {
       String xml = doMingleCall("projects.xml", "GET", null);
       projects = (ArrayList<MingleProject>)xstream.fromXML(xml);
     } catch(MalformedURLException e) {
       return null;
     }
 
     return projects;
   }
 
 /**
  * Performs a REST call on the mingle server and returns a XML string if we requested a ressource or a link
  * if we updated or created a ressource on the Mingle system.
  *
  * @param url URL The url that represents the RESTful url for the mingle server.
  * @param method String method This will set the used HTTP method. Only GET, POST, PUT or DELETE are supported.
  * @param xml String A string that represents a MingleObject in XML form or null if just requesting data.
  * 
  * @return String XML object that can be parsed as a MingleObject if this was requested or a URL to the 
  *                ressource which has just been updated. Returns an empty string if an IO error occurs.
  */
   public String doMingleCall(String url_s, String method, String xml) throws MalformedURLException {
 
     URL url = generateRestUrl(url_s);
 
     // Default HTTP method is GET:
     if (method == null) method = "GET";
 
     String resultString = "";
 
     try {
       // Set up connection:
       HttpURLConnection connection = (HttpURLConnection)url.openConnection();
       connection.setDoInput(true);
 
       if (xml != null) connection.setDoOutput(true);
       // if UserName and password is set we need a output and authentication
       if (userName != null && !userName.trim().isEmpty() && 
           password != null && !password.trim().isEmpty() ) {
         connection.setDoOutput(true);
         String userPassword = userName + ":" + password;
         String encoding = new sun.misc.BASE64Encoder().encode(userPassword.getBytes());
         connection.setRequestProperty("Authorization", "Basic " + encoding);
       }
     
       // Checks for valid Http method. Mingle supports: GET, POST, PUT or DELETE.
       if (method == "GET" || method == "POST" || method == "PUT" || method == "DELETE") {
         connection.setRequestMethod(method);
       }
       else connection.setRequestMethod("GET");
       connection.setFollowRedirects(true);
       connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); // maybe with "; charset=utf-8" in the end?
       connection.connect();
       // Output stuff:
       if (xml != null) {
         OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
         out.write(xml);
         out.close();
       }
     
       // Input stuff:
       InputStream is = connection.getInputStream();
       // convert InputStream to String
       Scanner scanner = new Scanner(is, "UTF-8").useDelimiter("\\A"); // or "\\Z" ?
       if (scanner.hasNext()) {
         try {
           resultString = scanner.next();
         } catch (java.util.NoSuchElementException e) {
           resultString = "";
         }
       }
 
       /**
        * "Result: If you were authorized to perform the operation, and the operation succeeded, 
        * you will be returned a LOCATION ATTRIBUTE in the http header of the response, which 
        * is a URL to the updated resource."
        */
   
       //get the LOCATION ATTRIBUTE from the HTTP Header:
       if (resultString == "") resultString = connection.getHeaderField("Location");
 
     }
     catch (IOException e) {
       return "";
     }
 
     return resultString;
   }
 
   /**
    * Gets the user-defined issue pattern if any.
    * 
    * @return the pattern or null
    */
   public Pattern getUserPattern() {
     if (userPattern == null) {
       return null;
     }
     
     if (userPat == null) {
       Pattern p = Pattern.compile(userPattern);
       userPat = p;
     }
     return userPat;
   }
 
   public Pattern getCardPattern() {
     if (getUserPattern() != null) {
       return getUserPattern();
     }
     
     return DEFAULT_CARD_PATTERN;
   } 
 
 
   // DESCRIPTOR:
   @Extension
   public static class DescriptorImpl extends Descriptor<MingleRestService> {
 
     @Override
     public String getDisplayName() {
       return "Mingle Rest Service";
     }
     
 
     /**
      * Checks if the content inside the URL contains the given string.
      */
     private boolean findTextInUrl(URL url, String text) throws IOException {
       // opens a http connection to url
       String resultString = "";
       HttpURLConnection connection = (HttpURLConnection)url.openConnection();
       connection.setFollowRedirects(true);
       connection.setDoOutput(false);
       connection.connect();
   
       // save response
       InputStream is = connection.getInputStream();
       Scanner scanner = new Scanner(is, "UTF-8").useDelimiter("\\A"); // or "\\Z" ?
       if (scanner.hasNext()) {
         try {
           resultString = scanner.next();
         } catch (java.util.NoSuchElementException e) {
           resultString = "";
         }
       }
       
       // actual check
       if (resultString.indexOf(text) != -1)  return true;
       return false;
     }
 
 
     /**
      * Checks if the Mingle URL is accessible and exists.
      */
     public FormValidation doUrlCheck(@QueryParameter final String value)
       throws IOException {
       // this can be used to check existence of any file in any URL, so
       // admin only
       if (!Hudson.getInstance().hasPermission(Hudson.ADMINISTER))
         return FormValidation.ok();
 
       return new FormValidation.URLCheck() {
         @Override
         protected FormValidation check() throws IOException {
           String url = Util.fixEmpty(value);
           if (url == null) {
             return FormValidation.error("The Mingle URL is a mandatory field!");
           }
 
           // normalize url with "/" at the end:
           if (url.charAt( url.length()-1) != '/') {
             url += "/";
           }
           
           // call urls to check if mingle can be reached
           try {
             URL loginURL = new URL(url + "profile/login");
             // checks if target url contains the mingle login page
             if (!findTextInUrl(loginURL, "<title>Login Profile - Mingle</title>") )
               return FormValidation.error("This is a valid URL but it doesnt look like mingle.");
             URL restUrl = new URL(url + "api/v2/projects.xml");
             if (!findTextInUrl(restUrl, "Incorrect username or password.") )
               return FormValidation.error("Couln't access the mingle API on the given URL. Please check if the Rest-API is activated.");
             return FormValidation.ok();
           } catch (IOException e) { // why catch it here when it is thrown anyway?
             LOGGER.log(Level.WARNING,"Unable to connect to " + url, e);
             return FormValidation.error("Unable to connect to " + url);
           }
         }
       }.check();
     }
     
     public FormValidation doCheckUserPattern(@QueryParameter String value) throws IOException {
       String userPattern = Util.fixEmpty(value);
       if (userPattern == null) {// userPattern not entered yet
         return FormValidation.ok();
       }
       try {
         Pattern.compile(userPattern);
         return FormValidation.ok();
       } catch (PatternSyntaxException e) {
         return FormValidation.error(e.getMessage());
       }
     }
     
     /**
      * Checks if the user name and password are valid.
      */
     public FormValidation doValidate( @QueryParameter String url,
                                       @QueryParameter String userName,
                                       @QueryParameter String password,
                                       @QueryParameter String project)
                 throws IOException {
         url = Util.fixEmpty(url);
         if (url == null) {// URL not entered yet
           return FormValidation.error("No URL given");
         }
         MingleRestService serv = new MingleRestService(new URL(url), userName, password, null, null, false);
 
         project = project.replaceAll("\\W", "_").toLowerCase();
 
         try {
           // Check if project exists:
 
           // TODO:Refactoring: replace all the following with List<MingleProject> projects = serv.getProjects(); + catch errors + iterate and check if it exists
 
           String projectsXML = serv.doMingleCall("projects.xml", "GET", null);
           if (projectsXML.indexOf("Incorrect username or password.") != -1) {
             LOGGER.log(Level.WARNING, "Failed to login to mingle at " + url);
             return FormValidation.error("Failed to login to mingle at " + url);
           }
           if (projectsXML.indexOf(project) == -1) {
             LOGGER.log(Level.WARNING, "The project name \""+project+"\" can't be found on the mingle server.");
             return FormValidation.error("The project name \""+project+"\" can't be found on the mingle server.");
           } else return FormValidation.ok("Success");
         } catch (MalformedURLException e) {
           LOGGER.log(Level.WARNING, "Could not create a valid URL: " + url, e);
           return FormValidation.error(e.getMessage());
         } catch (IOException e) {
           LOGGER.log(Level.WARNING, "Failed to process the mingle server answer at " + url, e);
           return FormValidation.error(e.getMessage());
         }
 
     }
 
   }
     
   private static final Logger LOGGER = Logger.getLogger(MingleRestService.class.getName());
 
 }
