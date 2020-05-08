 /*
  * $Id$
  */
 package org.xins.util.ant.sourceforge;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.security.SecureRandom;
 import java.security.cert.X509Certificate;
 import javax.net.ssl.HttpsURLConnection;
 import javax.net.ssl.SSLContext;
 import javax.net.ssl.TrustManager;
 import javax.net.ssl.X509TrustManager;
 import org.apache.commons.httpclient.Cookie;
 import org.apache.commons.httpclient.cookie.CookiePolicy;
 import org.apache.commons.httpclient.Header;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpException;
 import org.apache.commons.httpclient.HttpState;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.httpclient.methods.PostMethod;
 import org.apache.commons.net.ftp.FTPClient;
 import org.apache.commons.net.ftp.FTPReply;
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.Project;
 import org.apache.tools.ant.Task;
 import org.xins.util.text.FastStringBuffer;
 
 /**
  * Apache Ant task that creates a new XINS release at the SourceForge site.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</a>)
  *
  * @since XINS 0.127
  */
 public final class AddReleaseTask extends Task {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
     * The FTP server to upload the distribution files to.
     */
    private static final String FTP_SERVER = "upload.sourceforge.net";
 
    /**
     * The directory on the FTP server to upload the distribution files to.
     */
    private static final String FTP_DIR = "incoming";
 
    /**
     * The base URL for adding a release to a SourceForge package.
     */
    private static final String ADD_RELEASE_URL = "https://sourceforge.net/project/admin/newrelease.php";
 
    /**
     * The <em>debug</em> log level.
     */
    //private static int DEBUG = Project.MSG_DEBUG;
    private static int DEBUG = Project.MSG_INFO;
 
    /**
     * The <em>verbose</em> log level.
     */
    //private static int VERBOSE = Project.MSG_VERBOSE;
    private static int VERBOSE = Project.MSG_INFO;
 
    /**
     * The <em>information</em> log level.
     */
    private static int INFO = Project.MSG_INFO;
 
    /**
     * The <em>error</em> log level.
     */
    private static int ERROR = Project.MSG_ERR;
 
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>AddReleaseTask</code>.
     */
    public AddReleaseTask() {
       HttpState httpState = new HttpState();
       httpState.setCookiePolicy(CookiePolicy.COMPATIBILITY);
 
       _httpClient = new HttpClient();
       _httpClient.setConnectionTimeout(9000); // 9 seconds
       _httpClient.setTimeout(7000);           // 7 seconds
       _httpClient.setState(httpState);
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The SourceForge user to log in as.
     */
    private String _user;
 
    /**
     * The password for the SourceForge user to log in as.
     */
    private String _password;
 
    /**
     * The file to upload.
     */
    private String _file;
 
    /**
     * The SourceForge group ID for the package.
     */
    private String _groupID;
 
    /**
     * The SourceForge ID for the package.
     */
    private String _packageID;
 
    /**
     * The SourceForge release name.
     */
    private String _releaseName;
 
    /**
     * The location of the keystore file.
     */
    private String _keystore;
 
    /**
     * The <code>HttpClient</code> object to use.
     */
    private HttpClient _httpClient;
 
    /**
     * The <em>GET</em> method to use for executing requests.
     */
    private GetMethod _method;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Returns the given <code>String</code> unless it is an empty string, in
     * which case <code>null</code> is returned.
     *
     * @param s
     *    the input {@link String}, can be <code>null</code>.
     *
     * @return
     *    the input {@link String}, unless it is empty, in which case
     *    <code>null</code> is returned.
     */
    private String nullIfEmpty(String s) {
       if (s == null || s.length() < 1) {
          return null;
       } else {
          return s;
       }
    }
 
    /**
     * Sets the SourceForge user to log in as. Neither <code>null</code> nor
     * <code>""</code> are valid.
     *
     * @param user
     *    the SourceForge user.
     */
    public void setUser(String user) {
       _user = nullIfEmpty(user);
    }
 
    /**
     * Sets the password for the SourceForge user to log in as. Neither
     * <code>null</code> nor <code>""</code> are valid.
     *
     * @param password
     *    the password for the SourceForge user.
     */
    public void setPassword(String password) {
       _password = nullIfEmpty(password);;
    }
 
    /**
     * Sets the file to upload. Neither <code>null</code> nor <code>""</code>
     * are valid.
     *
     * @param file
     *    the path to the file to upload.
     */
    public void setFile(String file) {
       _file = nullIfEmpty(file);;
    }
 
    /**
     * Sets the SourceForge group ID. Neither <code>null</code> nor
     * <code>""</code> are valid.
     *
     * @param groupID
     *    the SourceForge group ID.
     */
    public void setGroup(String groupID) {
       _groupID = nullIfEmpty(groupID);;
    }
 
    /**
     * Sets the SourceForge package ID. Neither <code>null</code> nor
     * <code>""</code> are valid.
     *
    * @param packageID.
     *    the SourceForge package ID.
     */
    public void setPackage(String packageID) {
       _packageID = nullIfEmpty(packageID);
    }
 
    /**
     * Sets the SourceForge release name. Neither <code>null</code> nor
     * <code>""</code> are valid. Normally something like <code>"0.15"</code>.
     *
     * @param releaseName
     *    the name for this release.
     */
    public void setRelease(String releaseName) {
       _releaseName = nullIfEmpty(releaseName);
    }
 
    /**
     * Sets the location of the keystore file. Neither <code>null</code> nor
     * <code>""</code> are valid.
     *
     * @param keystore
     *    the location of the keystore file.
     */
    public void setKeystore(String keystore) {
       _keystore = nullIfEmpty(keystore);
    }
 
    /**
     * Fails with the specified error message.
     *
     * @param message
     *    the error message, can be <code>null</code>.
     *
     * @return
     *    the constructed build exception, never <code>null</code>.
     */
    private BuildException fail(String message) {
       log(message, ERROR);
       return new BuildException(message);
    }
    /**
     * Fails with the specified error message and cause exception.
     *
     * @param message
     *    the error message, can be <code>null</code>.
     *
     * @param cause
     *    the cause exception, can be <code>null</code>.
     *
     * @return
     *    the constructed build exception, never <code>null</code>.
     */
    private BuildException fail(String message, Throwable cause) {
       log(message, ERROR);
       return new BuildException(message, cause);
    }
 
    /**
     * Called by the project to let the task do its work.
     *
     * @throws BuildException
     *    if something goes wrong with the build.
     */
    public void execute() throws BuildException {
 
       // Check preconditions
       if (_file == null) {
          throw fail("The file to upload must be set, but it is not.");
       } else if (_user == null) {
          throw fail("The SourceForge user must be set, but it is not.");
       } else if (_password == null) {
          throw fail("The password for the SourceForge user must be set, but it is not.");
       } else if (_groupID == null) {
          throw fail("The SourceForge group ID must be set, but it is not.");
       } else if (_releaseName == null) {
          throw fail("The SourceForge release name must be set, but it is not.");
       } else if (_packageID == null) {
          throw fail("The SourceForge package ID must be set, but it is not.");
       } else if (_keystore == null) {
          throw fail("The keystore location must be set, but it is not.");
       }
 
       // Upload the file
       uploadFile();
 
       // Use our own keystore
       log("Using keystore file \"" + _keystore + "\".", DEBUG);
       System.setProperty("javax.net.ssl.trustStore", _keystore);
 
       // Login to SourceForge site
       login();
 
       // Create a new release
       addRelease();
    }
 
    /**
     * Uploads the distribution file to the SourceForge FTP server.
     *
     * @throws BuildException
     *    if the uploading fails.
     */
    private void uploadFile() throws BuildException {
 
       // Create a File object
       File f = new File(_file);
 
       // Create stream to file
       FileInputStream in;
       String fileName;
       try {
          in = new FileInputStream(f);
          fileName = f.getName();
       } catch (FileNotFoundException fnf) {
          throw fail("Unable to open file \"" + _file + "\".");
       }
 
       // Determine URL for display purposes
       String url = "ftp://" + FTP_SERVER + '/' + FTP_DIR + '/' + fileName;
 
       // Create an FTP client
       FTPClient ftp = new FTPClient();
 
       try {
          // Connect
          log("Connecting to FTP server \"" + FTP_SERVER + "\".", VERBOSE);
          ftp.connect(FTP_SERVER);
 
          // Check reply code to verify success
          int code = ftp.getReplyCode();
          if(! FTPReply.isPositiveCompletion(code)) {
             throw fail("FTP server " + FTP_SERVER + " refused connection.");
          }
 
          // Login
          String mailAddress = _user + "@users.sourceforge.net";
          log("Logging in anonymously with e-mail address " + mailAddress + '.', DEBUG);
          ftp.login("anonymous", mailAddress);
 
          // Set file type
          log("Setting file type to binary.", DEBUG);
          ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
 
          // Change to correct directory
          log("Changing working directory to \"" + FTP_DIR + "\".", DEBUG);
          ftp.changeWorkingDirectory(FTP_DIR);
 
          // Upload file
          log("Uploading " + _file + " to " + url, VERBOSE);
          if (!ftp.storeFile(fileName, in)) {
             code = ftp.getReplyCode();
             String replyString = ftp.getReplyString().trim();
 
             String expectedOverwriteError = "550 " + fileName + ": Overwrite permission denied";
             if (expectedOverwriteError.equals(replyString)) {
                log("File " + fileName + " already uploaded. Not overwriting.", INFO);
             } else {
                throw fail("Failed to upload " + _file + " to " + url + ". Reply: \"" + replyString + "\".");
             }
          } else {
             log("Uploaded " + _file + " to " + url, DEBUG);
          }
 
       } catch(IOException e) {
          throw fail("Failed to upload " + _file + " to " + url + " due to " + e.getClass().getName() + '.', e);
       } finally {
          if(ftp.isConnected()) {
             try {
                ftp.disconnect();
             } catch(IOException e) {
                // Ignore
             }
          }
       }
    }
 
    /**
     * Performs a login on the SourceForge web site.
     *
     * @throws BuildException
     *    if the login failed.
     */
    private void login() throws BuildException {
 
       // Create parameter string
       FastStringBuffer buffer = new FastStringBuffer(256);
       buffer.append("https://sourceforge.net/account/login.php?return_to=&form_loginname=");
       buffer.append(_user);
       buffer.append("&form_pw=");
       buffer.append(_password);
       buffer.append("&persistent_login=1&login=Login+With+SSL");
       String url = buffer.toString();
 
       GetMethod method = new GetMethod(url);
 
       log("Logging in to SourceForge site as \"" + _user + "\".", VERBOSE);
       try {
          // Execute the request
          log("Executing request \"" + url + "\".", DEBUG);
          int code = _httpClient.executeMethod(method);
 
          // Check status code
          final int EXPECTED_CODE = 302;
          if (code != EXPECTED_CODE) {
             throw fail("Expected result code " + EXPECTED_CODE + " while logging in. Received: " + method.getStatusLine());
          }
 
          // Get all cookies
          Cookie[] cookies = _httpClient.getState().getCookies();
          int cookieCount = cookies == null ? 0 : cookies.length;
          if (cookieCount < 1) {
             throw fail("SourceForge login failed. No cookies returned.");
          }
 
          // Log all cookies
          log("Received " + cookieCount + " cookies.", DEBUG);
          for (int i = 0; i < cookieCount; i++) {
             log("Received cookie: " + cookies[i].toExternalForm(), DEBUG);
          }
 
       } catch (IOException e) {
          throw fail("I/O error during SourceForge login.", e);
       }
 
       log("Logged in to SourceForge site as \"" + _user + "\".", DEBUG);
    }
 
    private void addRelease() throws BuildException {
 
       // Construct the URL
       FastStringBuffer buffer = new FastStringBuffer(256);
       buffer.append(ADD_RELEASE_URL);
       buffer.append("?group_id=");
       buffer.append(_groupID);
       buffer.append("&package_id=");
       buffer.append(_packageID);
       buffer.append("&release_name=");
       buffer.append(_releaseName);
       buffer.append("&submit=Create+This+Release");
       String url = buffer.toString();
       GetMethod method = new GetMethod(url);
 
       try {
          // Execute the request
          log("Creating release \"" + _releaseName + "\" for group " + _groupID + ", package " + _packageID + '.', VERBOSE);
          log("Current cookie count is " + _httpClient.getState().getCookies().length);
          log("Executing request \"" + url + "\".", DEBUG);
          int code = _httpClient.executeMethod(method);
          log("Received status line: " + method.getStatusLine(), DEBUG);
          log("Current cookie count is " + _httpClient.getState().getCookies().length, DEBUG);
          log("Created release \"" + _releaseName + "\" for group " + _groupID + ", package " + _packageID + '.', INFO);
       } catch (IOException e) {
          throw fail("Caught " + e.getClass().getName() + " while adding release.", e);
       }
    }
 }
