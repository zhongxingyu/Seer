 package au.edu.uq.cmm.benny;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Writer;
 import java.nio.charset.Charset;
 import java.util.Properties;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.codec.binary.Base64;
 import org.apache.log4j.Logger;
 
import au.edu.uq.cmm.aclslib.authenticator.Authenticator;
 
 /**
  * The Benny servlet provides a simple HTTP-based service for checking a
  * username and password against an ACLS service ... using the ACLS login
  * protocol and a dummy ACLS facility.
  * <p>
  * The credentials can be supplied as URL parameters (e.g. 
  * {@literal "...?user=fred&password=secret"} or as a Authorization
  * header using the Basic authentication scheme.  If neither forms is
  * used, the servlet responds with a WWW-Authenticate challenge.  The
  * response codes are:
  * <li>
  *   <li>200 OK - the credentials were accepted</li>
  *   <li>403 Forbidden - the credentials were rejected</li>
  *   <li>401 Unauthorized - no credentials were supplied</li>
  *   <li>500 Internal Service Error - something went wrong: check the logs.</li>
  * </li>
  * There may be a text or HTML response body, but a client should rely on the
  * response status code when determining the request's outcome.
  * 
  * @author scrawley
  */
 @SuppressWarnings("serial")
 public class Benny extends HttpServlet {
     private static final Logger LOG = Logger.getLogger(Authenticator.class);
     private static final String PROPS_RESOURCE = "/benny.properties";
     private static final Pattern BASIC_AUTH_PATTERN =
             Pattern.compile("Basic\\s+([a-z0-9+/=]+)\\s*", Pattern.CASE_INSENSITIVE);
     private static final Charset UTF_8 = Charset.forName("UTF-8");
     private Authenticator authenticator;
     private String realm;
 
     @Override
     public void init(ServletConfig config) throws ServletException {
         super.init(config);
         Properties props = new Properties();
         InputStream is = getClass().getResourceAsStream(PROPS_RESOURCE);
         if (is == null) {
             throw new ServletException("Cannot find resource: " + PROPS_RESOURCE);
         }
         try {
             props.load(is);
         } catch (IOException ex) {
             throw new ServletException(
                     "Cannot load the configuration properties in resource " + 
                     PROPS_RESOURCE);
         }
         try {
             realm = props.getProperty("benny.realm");
             authenticator = new Authenticator(
                     props.getProperty("benny.serverHost"), 
                     Integer.parseInt(props.getProperty("benny.serverPort")),
                     props.getProperty("benny.dummyFacility"));
         } catch (Exception ex) {
             throw new ServletException("Cannot instantiate the authenticator");
         }
     }
 
     @Override
     protected void doGet(HttpServletRequest req, HttpServletResponse resp)
             throws ServletException, IOException {
         String user = req.getParameter("user");
         String password = req.getParameter("password");
         if (user == null && password == null) {
             String[] credentials = getBasicAuthCredentials(req);
             if (credentials == null) {
                 resp.setHeader("WWW-Authenticate", "Basic realm=" + realm);
                 respond(resp, HttpServletResponse.SC_UNAUTHORIZED, "No credentials provided");
                 return;
             }
             user = credentials[0];
             password = credentials[1];
         }
         try {
             LOG.debug("checking user='" + user + "', password='" + password + "'");
             boolean ok = authenticator.authenticate(user, password);
             if (ok) {
                 respond(resp, HttpServletResponse.SC_OK, "Credentials accepted");
             } else {
                 respond(resp, HttpServletResponse.SC_FORBIDDEN, "Credentials rejected");
             }
         } catch (IOException ex) {
             throw ex;
         } catch (Exception ex) {
             LOG.error("Unexpected exception", ex);
             respond(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Service error");
         }
     }
 
     private String[] getBasicAuthCredentials(HttpServletRequest req) {
         String authorization = req.getHeader("Authorization");
         if (authorization == null) {
             // No header
             return null;
         }
         Matcher matcher = BASIC_AUTH_PATTERN.matcher(authorization);
         if (matcher.matches()) {
             String base64 = matcher.group(1);
             String userPass = new String(Base64.decodeBase64(base64), UTF_8);
             int colonPos = userPass.indexOf(":");
             if (colonPos <= 0 || colonPos == userPass.length() - 1) {
                 // Malformed <user-pass>
                 return null;
             } else {
                 return new String[] {
                         userPass.substring(0, colonPos),
                         userPass.substring(colonPos + 1)
                 };
             }
         } else {
             // Don't understand this authorization scheme
             return null;
         }
     }
 
     private void respond(HttpServletResponse resp, int status, String msg) 
             throws IOException {
         resp.setContentType("text/plain");
         resp.setStatus(status);
         Writer w = resp.getWriter();
         try {
             w.write(msg + "\r\n");
         } finally {
             w.close();
         }
     }
 }
