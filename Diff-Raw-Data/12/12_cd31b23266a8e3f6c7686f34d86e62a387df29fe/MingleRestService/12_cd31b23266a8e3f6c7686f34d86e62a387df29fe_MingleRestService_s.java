 package mingleplugin;
 
 import java.net.URL;
 import java.net.MalformedURLException;
 import java.util.regex.Pattern;
 import java.util.regex.PatternSyntaxException;
 import java.util.Scanner;
 import java.util.NoSuchElementException;
 import java.net.HttpURLConnection;
 import java.net.ProtocolException;
 
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.QueryParameter;
 
 // TODO: Imports for Input/Output Streams etc.
 
 
 /**
  * Class for the Mingle connection using the API v2.
  *
  * @author Birk Brauer
  * @version 0.7
  */
 public class MingleRestService {
 /**
  * URL of mingle, like <tt>http://mingle/api/v2/projects/scrum/</tt>.
  * Mandatory. Normalized to end with '/'
  */
   public final URL url;
 
 /**
  * User name needed to login. Optional?
  */
   public final String userName;
 
 /**
  * Password needed to login. Optional?
  */
   public final String password;
 
 /**
  * True if this mingle is configured to allow Confluence-style Wiki comment. Wait? Wat?
  */
   public final boolean supportsWikiStyleComment;
 
 // XStream set up:
   XStream xstream = new XStream(new StaxDriver());
  xstream.alias("MingleObject", MingleObject.class);
  xstream.alias("MingleCard", MingleCard.class);
  xstream.alias("MingleCardProperty", MingleCardProperty.class);
 
 
   @DataBoundConstructor
   public MingleRestService(URL url, String userName, String password, boolean supportsWikiStyleComment) {
     if(!url.toExternalForm().endsWith("/")) {
       try {
         url = new URL(url.toExternalForm()+"/");
       } catch (MalformedURLException e) {
         throw new AssertionError(e); // impossible
       }
     }
     this.url = url;
     this.userName = Util.fixEmpty(userName);
     this.password = Util.fixEmpty(password);
     this.supportsWikiStyleComment = supportsWikiStyleComment;
   }
 
   public String getUrlAsString() {
     return url.toExternalForm();
   }
 
   public URL getUrl() {
     return url;
   }
 
 /**
  * Generates a URL for the mingle REST call.
  * 
  * @return URL returns a URL including username and password.
  */
   public URL generateRestUrl(String action) {
     return new URL(url.getProtocol()+"://"+userName+":"+password+"@"+url.getHost()+url.getPort()+"/"+url.getPath()+action);
   }
 
 /**
  * Gets a mingle card by it's unique number.
  *
  * @param int number The unique number of the requested card.
  * 
  * @return MingleCard Returns a mingle card by it's unique number.
  */
   public MingleCard getCardByNumber(int number) {
     String xml = doMingleCall(generateRestUrl("cards/"+number+".xml"), "GET" , null);
     // convert XML to some kind of useful MingleCart or MingleSomething-object using XStream:
     MingleCard card = (MingleCard)xstream.fromXML(xml);
     return card;
   }
 
 /**
  * Overrides a card on the mingle server with the given card.
  *
  * @param number The unique number of the requested card.
  * @param card The new mingle card that should replace the version on the mingle server.
  */
   public void updateCardByNumber(int number, MingleCard card) {
     // Converts a MingleCard to a XML String.
 
     String xml = xstream.toXML(card);
     doMingleCall(generateRestUrl("cards/"+number+".xml"), "PUT", xml);
   }
 
 /**
  * Creates a new empty card on the mingle server. The simple way, close to the Mingle API.
  *
  * @param String name The name of the new mingle card that should be created on the server.
  * @param String cardtype The cardtype of the new mingle card that should be created on the server.
  *
  * @return URL The URL of the new created card will be returned.
  */
   public int createEmptyCard(String name, String cardtype) {
     // generates XML-string for card creation:
     String xml = "<card><name>"+name+"</name><card_type_name>"+cardtype+"</card_type_name></card>";
 
     // creates a new card:
     URL url = new URL(doMingleCall(generateRestUrl("cards.xml"), "POST", xml));
     
     return url;
   }
 
 /**
  * Creates a new card on the mingle server.
  *
  * @param MingleCard card The new mingle card that should be created on the server.
  *
  * @return int The unique card number of the new created card will be returned.
  */
   public int createCard(MingleCard card) {
     // creates a new card:
     URL url = createEmptyCard(card.getName(), card.getCardtype());
     
     // get cardnumber out of url:
     String urlpath = url.getPath();
     int cardnumber = (int)urlpath.substring(urlpath.lastIndexOf("/cards/")+7, urlpath.lastIndexOf(".xml"));
     // int cardnumber = (int)urlpath.substring(urlpath.lastIndexOf("/cards/")+7, urlpath.lastIndexOf(".xml") - (urlpath.lastIndexOf("/cards/")+7));
     
     // updates the new created card with the passed content:
     updateCardByNumber(cardnumber, card);
 
     return cardnumber;
   }
 
   //TODO: Do we need: Method getListOfCards(view, page, filters[], sort, order, tagged_with ) and so on?
 
 
 /**
  * Performs a REST call on the mingle server and returns a XML string if we requested a ressource or a link
  * if we updated or created a ressource on the Mingle system.
  *
  * @param url URL The url that represents the RESTful url for the mingle server.
  * @param method String method This will set the used HTTP method. Only GET, POST, PUT or DELETE are supported.
  * @param xml String A string that represents a MingleObject in XML form or null if just requesting data.
  * 
  * @return String XML object that can be parsed as a MingleObject if this was requested or a URL to the 
  *                ressource which has just been updated.
  */
   public String doMingleCall(URL url, String method, String xml) {
     // Default HTTP method is GET:
     if (method == null) method = "GET";
 
     // Set up connection:
     //HttpURLConnection connection = new HttpURLConnection(url);
     HttpURLConnection connection = (HttpURLConnection)url.openConnection();
     connection.setDoInput(true);
     if (xml != null) connection.setDoOutput(true);
     else method = "GET"; // if nothing to update/delete is given it's obviously that we should use GET
 
     // Checks for valid Http method. Mingle supports: GET, POST, PUT or DELETE.
     if (method == "GET" || method == "POST" || method == "PUT" || method == "DELETE") {
       connection.setRequestMethod(method);
     }
     else throw ProtocolException();
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
     resultString = "";
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
 
     //get the LOCCATION ATTRIBUTE from the HTTP Header:
    if (resultString == "") resultString = connection.getHeaderFieldDate("Location");
 
     return resultString;
   }
 
 }
