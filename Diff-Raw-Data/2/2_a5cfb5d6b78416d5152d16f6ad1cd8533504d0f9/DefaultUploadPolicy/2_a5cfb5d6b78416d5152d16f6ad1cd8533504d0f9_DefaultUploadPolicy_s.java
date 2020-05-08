 //
 // $Id: DefaultUploadPolicy.java 152 2007-05-16 16:34:42 +0000 (mer., 16 mai
 // 2007) etienne_sf $
 // 
 // jupload - A file upload applet.
 // Copyright 2007 The JUpload Team
 // 
 // Created: 2006-05-04
 // Creator: Etienne Gauthier
 // Last modified: $Date$
 //
 // This program is free software; you can redistribute it and/or modify it under
 // the terms of the GNU General Public License as published by the Free Software
 // Foundation; either version 2 of the License, or (at your option) any later
 // version. This program is distributed in the hope that it will be useful, but
 // WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 // FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 // details. You should have received a copy of the GNU General Public License
 // along with this program; if not, write to the Free Software Foundation, Inc.,
 // 675 Mass Ave, Cambridge, MA 02139, USA.
 
 package wjhk.jupload2.policies;
 
 import java.awt.BorderLayout;
 import java.awt.GridLayout;
 import java.awt.SystemColor;
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.ByteArrayOutputStream;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintStream;
 import java.io.UnsupportedEncodingException;
 import java.net.ConnectException;
 import java.net.Socket;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.Locale;
 import java.util.ResourceBundle;
 import java.util.Vector;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.regex.PatternSyntaxException;
 
 import javax.swing.BorderFactory;
 import javax.swing.Icon;
 import javax.swing.JButton;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JProgressBar;
 import javax.swing.UIManager;
 import javax.swing.text.BadLocationException;
 
 import netscape.javascript.JSException;
 import netscape.javascript.JSObject;
 import wjhk.jupload2.JUploadApplet;
 import wjhk.jupload2.exception.JUploadException;
 import wjhk.jupload2.exception.JUploadExceptionUploadFailed;
 import wjhk.jupload2.filedata.DefaultFileData;
 import wjhk.jupload2.filedata.FileData;
 import wjhk.jupload2.gui.JUploadFileFilter;
 import wjhk.jupload2.gui.JUploadTextArea;
 import wjhk.jupload2.upload.HttpConnect;
 
 /**
  * This class implements all {@link wjhk.jupload2.policies.UploadPolicy}
  * methods. Its way of working is he same as the JUpload version 1. <BR>
  * The simplest way to use this policy is given in the presentation of
  * {@link UploadPolicy}. The DefaultUploadPolicy is used when no
  * <I>uploadPolicy</I> parameter is given to the applet, or this parameter has
  * 'DefaultUploadPolicy' as a value. <BR>
  * <P>
  * The <U>default behavior</U> is representated below. It can be overrided by
  * adding parameters to the applet. All available parameters are shown in the
  * presentation of {@link UploadPolicy}.
  * </P>
  * <UL>
  * <LI>Default implementation for all
  * {@link wjhk.jupload2.policies.UploadPolicy} methods.
  * <LI>Files are uploaded all in one HTTP request.
  * <LI>No handling for particular kind of files: files are transmitted without
  * any transformation.
  * <LI>The file are transmitted to the server with the navigator cookies,
  * userAgent and Protocol. This make upload occurs within the current user
  * session on the server. So, it allows right management and context during the
  * management of uploaded files, on the server.
  * </UL>
  * 
  * @author Etienne Gauthier
  * @version $Revision$
  */
 
 public class DefaultUploadPolicy implements UploadPolicy {
 
     // //////////////////////////////////////////////////////////////////////////////////////////////
     // /////////////////// APPLET PARAMETERS
     // ///////////////////////////////////////////////////
     // //////////////////////////////////////////////////////////////////////////////////////////////
 
     /**
      * applet contains the reference of the Applet. It's useful to interact with
      * it. <BR>
      * It also allows acccess to the navigator properties, if the html tag
      * MAYSCRIPT is put in the APPLET tag. This allows this class to get the
      * cookie, userAgent and protocol, to upload files in the current user
      * session on the server. <BR>
      * Default : no default value
      */
     private JUploadApplet applet = null;
 
     /**
      * Contains the applet parameter of the same name. If a valid URL is given
      * here, the navigator will get redirected to this page, after a successful
      * upload.
      */
     private String afterUploadURL = UploadPolicy.DEFAULT_AFTER_UPLOAD_URL;
 
     /**
      * Contains the allowedFileExtensions applet paramater.
      */
     private boolean allowHttpPersistent = UploadPolicy.DEFAULT_ALLOW_HTTP_PERSISTENT;
 
     /**
      * Contains the allowedFileExtensions applet paramater.
      */
     private String allowedFileExtensions = UploadPolicy.DEFAULT_ALLOWED_FILE_EXTENSIONS;
 
     /**
      * Indicate whether the log window is shown or not to the user. In all cases
      * it remains in memory, and stores all debug information. This allows a log
      * information, in case of an error occurs.
      * 
      * @see #urlToSendErrorTo
      */
     private boolean showLogWindow = UploadPolicy.DEFAULT_SHOW_LOGWINDOW;
 
     /**
      * The current debug level.
      */
     private int debugLevel = UploadPolicy.DEFAULT_DEBUG_LEVEL;
 
     /**
      * This String contains the filenameEncoding parameter. All details about
      * the available applet parameters are displayed in the <a
      * href="UploadPolicy.html@parameters">Upload Policy javadoc page</a>.
      */
     private String filenameEncoding = UploadPolicy.DEFAULT_FILENAME_ENCODING;
 
     /**
      * The lang parameter, given to the applet.
      */
     private String lang = UploadPolicy.DEFAULT_LANG;
 
     /**
      * The look and feel is used as a parameter of the
      * UIManager.setLookAndFeel(String) method. See the parameters list on the
      * {@link UploadPolicy} page.
      */
     private String lookAndFeel = UploadPolicy.DEFAULT_LOOK_AND_FEEL;
 
     /**
      * The applet will do as may HTTP requests to upload all files, with the
      * number as a maximum number of files for each HTTP request. <BR>
      * Default : -1
      */
     private int nbFilesPerRequest = UploadPolicy.DEFAULT_NB_FILES_PER_REQUEST;
 
     /**
      * Current value (or default value) of the maxChunkSize applet parameter.
      * <BR>
      * Default : Long.MAX_VALUE
      */
     private long maxChunkSize = UploadPolicy.DEFAULT_MAX_CHUNK_SIZE;
 
     /**
      * Current value (or default value) of the maxFileSize applet parameter.
      * <BR>
      * Default : Long.MAX_VALUE
      */
     private long maxFileSize = UploadPolicy.DEFAULT_MAX_FILE_SIZE;
 
     /**
      * The URL where files should be posted. <BR>
      * Default : no default value. (mandatory)
      */
     private String postURL = UploadPolicy.DEFAULT_POST_URL;
 
     /**
      * @see UploadPolicy#getServerProtocol()
      */
     private String serverProtocol = UploadPolicy.DEFAULT_SERVER_PROTOCOL;
 
     /**
      * @see UploadPolicy#getStringUploadError()
      */
     private String stringUploadError = UploadPolicy.DEFAULT_STRING_UPLOAD_ERROR;
 
     /**
      * @see UploadPolicy#getStringUploadSuccess()
      */
     private String stringUploadSuccess = UploadPolicy.DEFAULT_STRING_UPLOAD_SUCCESS;
 
     /**
      * If an error occurs during upload, and this attribute is not null, the
      * applet asks the user if wants to send the debug ouput to the
      * administrator. If yes, the full debug information is POSTed to this URL.
      * It's a little development on the server side to send a mail to the
      * webmaster, or just log this error into a log file.
      * 
      * @see UploadPolicy#sendDebugInformation(String)
      */
     private String urlToSendErrorTo = UploadPolicy.DEFAULT_URL_TO_SEND_ERROR_TO;
 
     /**
      * Optional name of a form (in the same document like the applet) which is
      * used to populate POST parameters.
      */
     private String formData = UploadPolicy.DEFAULT_FORMDATA;
 
     private String afterUploadTarget = UploadPolicy.DEFAULT_AFTER_UPLOAD_TARGET;
 
     private final static String CRLF = System.getProperty("line.separator");
 
     // //////////////////////////////////////////////////////////////////////////////////////////////
     // /////////////////// INTERNAL ATTRIBUTE
     // ///////////////////////////////////////////////////
     // //////////////////////////////////////////////////////////////////////////////////////////////
 
     /**
      * This Vector contains headers that will be added for each upload. It may
      * contains specific cookies, for instance.
      * 
      * @see #onAppendHeader(StringBuffer)
      */
     private Vector<String> headers = new Vector<String>();
 
     /**
      * The text area, where message are to be displayed.
      * 
      * @see #displayMsg(String)
      */
     private JUploadTextArea logWindow = null;
 
     /**
      * The resourceBundle contains all localized String (and others ??)
      */
     private ResourceBundle resourceBundle = null;
 
     /**
      * This stream is used to store all information that could be useful, in
      * case a problem occurs. Is content can then be sent to the webmaster.
      */
     private PrintStream debugOut = null;
 
     /**
      * The actual file, used for the debug log.
      */
     private File debugFile = null;
 
     /**
      * This flag prevents endless repeats of opening the debug log, if that
      * failed for some reason.
      */
     private boolean debugOk = true;
 
     /**
      * cookie is the value of the javascript <I>document.cookie</I> property.
      */
     private String cookie = null;
 
     /**
      * userAgent is the value of the javascript <I>navigator.userAgent</I>
      * property.
      */
     private String userAgent = null;
 
     /**
      * This constant defines the upper limit of lines, kept in the log window.
      */
     private final static int MAX_DEBUG_LINES = 10000;
 
     protected Pattern patternSuccess = Pattern
             .compile(UploadPolicy.DEFAULT_STRING_UPLOAD_SUCCESS);
 
     protected Pattern patternError = Pattern
             .compile(UploadPolicy.DEFAULT_STRING_UPLOAD_ERROR);
 
     // //////////////////////////////////////////////////////////////////////////////////////////////
     // /////////////////// CONSTRUCTORS
     // ///////////////////////////////////////////////////
     // //////////////////////////////////////////////////////////////////////////////////////////////
 
     /**
      * The main constructor : use default values, and the given postURL.
      * 
      * @param theApplet The current applet. As the reference to the current
      *            upload policy exists almost everywhere, this parameter allows
      *            any access to anyone on the applet... including reading the
      *            applet parameters.
      * @throws JUploadException If an applet parameter is invalid
      */
     public DefaultUploadPolicy(JUploadApplet theApplet) throws JUploadException {
         // Call default constructor for all default initialization;.
         this.applet = theApplet;
         this.logWindow = theApplet.getLogWindow();
 
         // ////////////////////////////////////////////////////////////////////////////
         // get the afterUploadURL applet parameter.
         setAfterUploadURL(UploadPolicyFactory.getParameter(theApplet,
                 PROP_AFTER_UPLOAD_URL, DEFAULT_AFTER_UPLOAD_URL, this));
 
         // ////////////////////////////////////////////////////////////////////////////
         // get the allowedFileExtensions applet parameter
         setAllowedFileExtensions(UploadPolicyFactory.getParameter(theApplet,
                 PROP_ALLOWED_FILE_EXTENSIONS, DEFAULT_ALLOWED_FILE_EXTENSIONS,
                 this));
 
         setAllowHttpPersistent(UploadPolicyFactory
                 .getParameter(theApplet, PROP_ALLOW_HTTP_PERSISTENT,
                         DEFAULT_ALLOW_HTTP_PERSISTENT, this));
 
         // ////////////////////////////////////////////////////////////////////////////
         // get the showLogWindow applet parameter.
         setShowLogWindow(UploadPolicyFactory.getParameter(theApplet,
                 PROP_SHOW_LOGWINDOW, DEFAULT_SHOW_LOGWINDOW, this));
 
         // ////////////////////////////////////////////////////////////////////////////
         // get the debug level. This control the level of debug messages that
         // are written
         // in the log window (see displayDebugMessage). In all cases, the full
         // output
         // is written in the debugBufferString (see also urlToSendErrorTo)
         setDebugLevel(UploadPolicyFactory.getParameter(theApplet,
                 PROP_DEBUG_LEVEL, DEFAULT_DEBUG_LEVEL, this), false);
 
         // ////////////////////////////////////////////////////////////////////////////
         // get the filenameEncoding. If not null, it should be a valid argument
         // for
         // the URLEncoder.encode method.
         setFilenameEncoding(UploadPolicyFactory.getParameter(theApplet,
                 PROP_FILENAME_ENCODING, DEFAULT_FILENAME_ENCODING, this));
 
         // Force the look and feel of the current system.
         setLookAndFeel(UploadPolicyFactory.getParameter(theApplet,
                 PROP_LOOK_AND_FEEL, DEFAULT_LOOK_AND_FEEL, this));
 
         // /////////////////////////////////////////////////////////////////////////////
         // get the maximum number of files to upload in one HTTP request.
         setNbFilesPerRequest(UploadPolicyFactory.getParameter(theApplet,
                 PROP_NB_FILES_PER_REQUEST, DEFAULT_NB_FILES_PER_REQUEST, this));
 
         // /////////////////////////////////////////////////////////////////////////////
         // get the maximum size of a file on one HTTP request (indicate if the
         // file
         // must be splitted before upload, see UploadPolicy comment).
         setMaxChunkSize(UploadPolicyFactory.getParameter(theApplet,
                 PROP_MAX_CHUNK_SIZE, DEFAULT_MAX_CHUNK_SIZE, this));
 
         // /////////////////////////////////////////////////////////////////////////////
         // get the maximum size of an uploaded file.
         setMaxFileSize(UploadPolicyFactory.getParameter(theApplet,
                 PROP_MAX_FILE_SIZE, DEFAULT_MAX_FILE_SIZE, this));
 
         // /////////////////////////////////////////////////////////////////////////////
         // get the URL where files must be posted.
         setPostURL(UploadPolicyFactory.getParameter(theApplet, PROP_POST_URL,
                 DEFAULT_POST_URL, this));
 
         // /////////////////////////////////////////////////////////////////////////////
         // get the server protocol.
         // It is used by Coppermine Picture Gallery (nice tool) to control that
         // the user
         // sending the cookie uses the same http protocol that the original
         // connexion.
         // Please have a look tp the UploadPolicy.serverProtocol attribute.
         setServerProtocol(UploadPolicyFactory.getParameter(theApplet,
                 PROP_SERVER_PROTOCOL, DEFAULT_SERVER_PROTOCOL, this));
 
         setStringUploadError(UploadPolicyFactory.getParameter(theApplet,
                 PROP_STRING_UPLOAD_ERROR, DEFAULT_STRING_UPLOAD_ERROR, this));
 
         setStringUploadSuccess(UploadPolicyFactory
                 .getParameter(theApplet, PROP_STRING_UPLOAD_SUCCESS,
                         DEFAULT_STRING_UPLOAD_SUCCESS, this));
 
         // /////////////////////////////////////////////////////////////////////////////
         // get the URL where the full debug output can be sent when an error
         // occurs.
         setUrlToSendErrorTo(UploadPolicyFactory.getParameter(theApplet,
                 PROP_URL_TO_SEND_ERROR_TO, DEFAULT_URL_TO_SEND_ERROR_TO, this));
 
         // /////////////////////////////////////////////////////////////////////////////
         // Get resource file.
         setLang(UploadPolicyFactory.getParameter(theApplet, PROP_LANG,
                 DEFAULT_LANG, this));
 
         this.formData = UploadPolicyFactory.getParameter(theApplet,
                 PROP_FORMDATA, DEFAULT_FORMDATA, this);
 
         this.afterUploadTarget = UploadPolicyFactory.getParameter(theApplet,
                 PROP_AFTER_UPLOAD_TARGET, DEFAULT_AFTER_UPLOAD_TARGET, this);
 
         // /////////////////////////////////////////////////////////////////////////////
         // Load session data read from the navigator:
         // - cookies.
         // - User-Agent : useful, as the server will then see a post request
         // coming from the same navigator.
         //
         try {
             // Test, to avoid a crash under linux
             JSObject applet = JSObject.getWindow(getApplet());
             JSObject doc = (JSObject) applet.getMember("document");
             this.cookie = (String) doc.getMember("cookie");
 
             JSObject nav = (JSObject) applet.getMember("navigator");
             this.userAgent = (String) nav.getMember("userAgent");
 
             displayDebug("cookie: " + this.cookie, 10);
             displayDebug("userAgent: " + this.userAgent, 10);
         } catch (JSException e) {
             displayWarn("JSException (" + e.getClass() + ": " + e.getMessage()
                     + ") in DefaultUploadPolicy, trying default values.");
 
             // If we can't have access to the JS objects, we're in development :
             // Let's put some 'hard value', to test the applet from the
             // development tool (mine is eclipse).
 
             // felfert: I need different values so let's make that
             // configurable...
             this.cookie = System.getProperty("debug_cookie");
             this.userAgent = System.getProperty("debug_agent");
             /*
              * Exemple of parameter when calling the JVM:
              * -Ddebug_cookie="Cookie:
              * cpg146_data=YTo0OntzOjI6IklEIjtzOjMyOiJhZGU3MWIxZmU4OTZjNThhZjQ5N2FiY2ZiNmFlZTUzOCI7czoyOiJhbSI7aToxO3M6NDoibGFuZyI7czo2OiJmcmVuY2giO3M6MzoibGl2IjthOjI6e2k6MDtOO2k6MTtzOjQ6IjE0ODgiO319;
              * cpg143_data=YTozOntzOjI6IklEIjtzOjMyOiI4NjhhNmQ4ZmNlY2IwMTc5YTJiNmZlMGY3YWQzNThkNSI7czoyOiJhbSI7aToxO3M6NDoibGFuZyI7czo2OiJmcmVuY2giO30%3D;
              * 8387c97d1f683b758a67a0473b586126=5ed998846fec70d6d2f73971b9cbbf0b;
              * b1d7468cf1b317c97c7c284f6bb14ff8=587b82a7abb3d2aca134742b1df9acf7"
              * -Ddebug_agent="userAgent: Mozilla/5.0 (Windows; U; Windows NT
              * 5.0; fr; rv:1.8.1.3) Gecko/20070309 Firefox/2.0.0.3"
              */
         }
         // The cookies and user-agent will be added to the header sent by the
         // applet:
         if (this.cookie != null)
             addHeader("Cookie: " + this.cookie);
         if (this.userAgent != null)
             addHeader("User-Agent: " + this.userAgent);
 
         // We let the UploadPolicyFactory call the displayParameterStatus
         // method, so that the
         // initialization is finished, including for classes which inherit from
         // DefaultUploadPolicy.
     }
 
     // //////////////////////////////////////////////////////////////////////////////////////////////
     // /////////////////// UploadPolicy methods
     // ///////////////////////////////////////////////////
     // //////////////////////////////////////////////////////////////////////////////////////////////
     // getters and setters are sorted below
 
     /**
      * @see wjhk.jupload2.policies.UploadPolicy#addHeader(java.lang.String)
      */
     public void addHeader(String header) {
         this.headers.add(header);
     }
 
     /**
      * @see wjhk.jupload2.policies.UploadPolicy#beforeUpload()
      */
     public void beforeUpload() {
         // Default: no special action.
     }
 
     /**
      * The default behaviour (see {@link DefaultUploadPolicy}) is to check that
      * the stringUploadSuccess applet parameter is present in the response from
      * the server. The return is tested, in the order below: <DIR>
      * <LI>True, if the stringUploadSuccess was not given as an applet
      * parameter (no test at all).
      * <LI>True, if the stringUploadSuccess string is present in the
      * serverOutputBody.
      * <LI>True, If previous condition is not filled, but the HTTP header
      * "HTTP(.*)200OK$" is present: the test is currently non blocking, because
      * I can not test all possible HTTP configurations.<BR>
      * <LI>False if the previous conditions are not fullfilled. </DIR>
      * 
      * @param status The HTTP response code
      * @param msg The full HTTP response message (e.g. "404 Not found").
      * @param body The body of the HTTP answer.
      * @return True or False, indicating if the upload is a success or not.
      * @see UploadPolicy#checkUploadSuccess(int, String, String)
      */
     public boolean checkUploadSuccess(int status, String msg, String body)
             throws JUploadException {
         displayDebug("HTTP status: " + msg, 40);
         if (status != 200)
             throw new JUploadExceptionUploadFailed("Received HTTP status "
                     + msg);
 
         if (!this.stringUploadError.equals("")) {
             Matcher m = this.patternError.matcher(body);
             if (m.find()) {
                 String errmsg = "An error occurs during upload (but the applet couldn't find the error message)";
                 if (m.groupCount() > 0) {
                     errmsg = m.group(1);
                     if (errmsg.equals("")) {
                         errmsg = "An unknown error occurs during upload.";
                     }
                 }
                 // Let's display the error message to the user.
                 alertStr(errmsg);
 
                 throw new JUploadExceptionUploadFailed(getClass().getName()
                         + ".checkUploadSuccess(): " + errmsg);
             }
             displayDebug("No error message found in HTTP response body", 50);
         }
 
         if (this.stringUploadSuccess.equals(""))
             // No chance to check the correctness of this upload. -> Assume Ok
             return true;
 
         // The success string should be in the http body
         if (this.patternSuccess.matcher(body).find())
             return true;
 
         // stringUploadSuccess was defined but we did not find it.
         // This is most certainly an error as http-status 200 does *not* refer
         // to the correctness of the content. It merely means that the protocol
         // handling was ok. -> throw an exception
         throw new JUploadExceptionUploadFailed(getClass().getName()
                 + ".checkUploadSuccess(): The string \""
                 + this.stringUploadSuccess
                 + "\" was not found in the response body");
     } // checkUploadSuccess
 
     /**
      * @see wjhk.jupload2.policies.UploadPolicy#afterUpload(Exception, String)
      */
     public void afterUpload(Exception e, @SuppressWarnings("unused")
     String serverOutput) {
         // If there was no error, and afterUploadURL is defined, let's try to go
         // to this URL.
         if (e == null && getAfterUploadURL() != null) {
             try {
                 if (getDebugLevel() >= 100) {
                     alertStr("No switch to getAfterUploadURL, because debug level is "
                             + getDebugLevel() + " (>=100)");
                 } else {
                     // Let's change the current URL to edit names and comments,
                     // for the selected album. Ok, let's go and add names and
                     // comments to the newly updated pictures.
                     String target = getAfterUploadTarget();
                     getApplet().getAppletContext().showDocument(
                             new URL(getAfterUploadURL()),
                             (null == target) ? "_self" : target);
                 }
             } catch (Exception ee) {
                 // Oops, no navigator. We are probably in debug mode, within
                 // eclipse for instance.
                 displayErr(ee);
             }
         }
     }
 
     /** @see UploadPolicy#alertStr(String) */
     public void alertStr(String str) {
         JOptionPane.showMessageDialog(null, str, "Alert",
                 JOptionPane.WARNING_MESSAGE);
     }
 
     /** @see UploadPolicy#alert(String) */
     public void alert(String key) {
         alertStr(getString(key));
     }
 
     /** @see UploadPolicy#alert(String,String) */
     public void alert(String key, String arg1) {
         alertStr(getString(key, arg1));
     }
 
     /**
      * The DefaultUpload accepts all file types: we just return an instance of
      * FileData, without any test.
      * 
      * @see UploadPolicy#createFileData(File)
      */
     public FileData createFileData(File file) {
         return new DefaultFileData(file, this);
     }
 
     /**
      * Default implementation of
      * {@link wjhk.jupload2.policies.UploadPolicy#createTopPanel(JButton, JButton, JButton, JPanel)}.
      * IT creates a JPanel, containing the three given JButton. It creates the
      * same panel as the original JUpload.
      * 
      * @see wjhk.jupload2.policies.UploadPolicy#createTopPanel(JButton, JButton,
      *      JButton, JPanel)
      */
     public JPanel createTopPanel(JButton browse, JButton remove,
             JButton removeAll, @SuppressWarnings("unused")
             JPanel mainPanel) {
         JPanel jPanel = new JPanel();
 
         jPanel.setLayout(new GridLayout(1, 3, 10, 5));
         jPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
         jPanel.add(browse);
         jPanel.add(removeAll);
         jPanel.add(remove);
 
         mainPanel.setBorder(BorderFactory
                 .createLineBorder(SystemColor.controlDkShadow));
 
         return jPanel;
     }
 
     /**
      * @see wjhk.jupload2.policies.UploadPolicy#createProgressPanel(javax.swing.JProgressBar,
      *      javax.swing.JButton, javax.swing.JButton, javax.swing.JPanel)
      */
     public JPanel createProgressPanel(JProgressBar progressBar,
             JButton uploadButton, JButton stopButton,
             @SuppressWarnings("unused")
             JPanel mainPanel) {
         JPanel jPanel = new JPanel();
 
         jPanel.setLayout(new BorderLayout(10, 0));
         jPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
         jPanel.add(uploadButton, BorderLayout.LINE_START);
         jPanel.add(progressBar, BorderLayout.CENTER);
         jPanel.add(stopButton, BorderLayout.LINE_END);
         return jPanel;
     }
 
     /**
      * @see UploadPolicy#displayErr(Exception)
      */
     public void displayErr(Exception e) {
         ByteArrayOutputStream bs = new ByteArrayOutputStream();
         PrintStream ps = new PrintStream(bs);
         e.printStackTrace(ps);
         ps.close();
         displayErr(bs.toString());
     }
 
     /**
      * @see UploadPolicy#displayErr(String)
      */
     public void displayErr(String err) {
         // If debug is off, the log window may not be visible. We switch the
         // debug to on, to be sure that some
         // information will be displayed to the user.
         if (getDebugLevel() <= 0)
             setDebugLevel(1);
         displayMsg("[ERROR] ", err);
     }
 
     /**
      * @see wjhk.jupload2.policies.UploadPolicy#displayErr(java.lang.String,
      *      java.lang.Exception)
      */
     public void displayErr(String err, Exception e) {
         displayErr(err);
         displayErr(e);
     }
 
     /**
      * @see UploadPolicy#displayInfo(String)
      */
     public void displayInfo(String info) {
         displayMsg("[INFO] ", info);
     }
 
     /**
      * @see UploadPolicy#displayWarn(String)
      */
     public void displayWarn(String warn) {
         displayMsg("[WARN] ", warn);
     }
 
     /**
      * @see UploadPolicy#displayDebug(String, int)
      */
     public void displayDebug(String debug, int minDebugLevel) {
         final String tag = "[DEBUG] ";
         if (this.debugLevel >= minDebugLevel) {
             // displayMsg will add the message to the debugStrignBuffer.
             displayMsg(tag, debug);
         } else {
             // Let's store all text in the debug BufferString
             addMsgToDebugLog(timestamp(tag, debug));
         }
     }
 
     /** @see UploadPolicy#getString(String) */
     public String getString(String key) {
         String ret = this.resourceBundle.getString(key);
         return ret;
     }
 
     /** @see UploadPolicy#getString(String,String) */
     public String getString(String key, String value1) {
         String ret = this.resourceBundle.getString(key).replaceAll("\\{1\\}",
                 value1);
         return ret;
     }
 
     /** @see UploadPolicy#getString(String,String,String) */
     public String getString(String key, String value1, String value2) {
         String ret = this.resourceBundle.getString(key).replaceAll("\\{1\\}",
                 value1).replaceAll("\\{2\\}", value2);
         return ret;
     }
 
     /** @see UploadPolicy#getString(String,String,String,String) */
     public String getString(String key, String value1, String value2,
             String value3) {
         String ret = this.resourceBundle.getString(key).replaceAll("\\{1\\}",
                 value1).replaceAll("\\{2\\}", value2).replaceAll("\\{3\\}",
                 value3);
         return ret;
     }
 
     /**
      * @see UploadPolicy#getString(String,int)
      */
     public String getString(String key, int value1) {
         return getString(key, Integer.toString(value1));
     }
 
     /**
      * @see UploadPolicy#getUploadFilename(FileData, int)
      */
     public String getUploadFilename(FileData fileData,
             @SuppressWarnings("unused")
             int index) throws JUploadException {
         if (this.filenameEncoding == null || this.filenameEncoding.equals(""))
             return fileData.getFileName();
         try {
             return URLEncoder.encode(fileData.getFileName(),
                     this.filenameEncoding);
         } catch (UnsupportedEncodingException e) {
             throw new JUploadException(e);
         }
     }
 
     /** @see UploadPolicy#getUploadName(FileData, int) */
     public String getUploadName(@SuppressWarnings("unused")
     FileData fileData, int index) {
         // This is the original way of working of JUpload.
         // It can easily be modified, by using another UploadPolicy.
         return "File" + index;
     }
 
     /** @see wjhk.jupload2.policies.UploadPolicy#isUploadReady() */
     public boolean isUploadReady() {
         // Default : nothing to do before upload, so we're ready.
         return true;
     }
 
     /** @see UploadPolicy#onAppendHeader(StringBuffer) */
     public StringBuffer onAppendHeader(StringBuffer sb) {
         Iterator it = this.headers.iterator();
         String header;
         while (it.hasNext()) {
             header = (String) it.next();
             displayDebug(header, 90);
             sb.append(header);
             sb.append("\r\n");
         }
         return sb;
     }// appendHeader
 
     /**
      * Default implementation of the
      * {@link wjhk.jupload2.policies.UploadPolicy#onSelectFile(wjhk.jupload2.filedata.FileData)}.
      * Nothing's done.
      */
     public void onSelectFile(@SuppressWarnings("unused")
     FileData fileData) {
         // Default implementation : no action
     }
 
     /** @see UploadPolicy#sendDebugInformation(String) */
     public void sendDebugInformation(String description) {
 
         if (null != this.urlToSendErrorTo) {
             if (JOptionPane.showConfirmDialog(null,
                     getString("questionSendMailOnError"), getString("Confirm"),
                     JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                 displayDebug("Within response == true", 60);
 
                 // The message is written in english, as it is not sure that the
                 // webmaster speaks the same language as the current user.
                 String query = null;
                 String action = null;
                 Socket sock = null;
                 DataOutputStream dataout = null;
                 BufferedReader datain = null;
                 StringBuffer sbHttpResponseBody = null;
                 StringBuffer request = null;
                 String line;
 
                 // During debug output, we need to make shure that the debug
                 // log is not changed, so we set debugOk to false
                 // temporarily. -> Everything goes to stdout.
                 boolean localDebugOk = this.debugOk;
                 this.debugOk = false;
 
                 try {
                     this.debugOut.flush();
                     // First, calculate the size of the strings we will send.
                     BufferedReader debugIn = new BufferedReader(new FileReader(
                             this.debugFile));
                     int contentLength = 0;
                     while ((line = debugIn.readLine()) != null) {
                         contentLength += URLEncoder
                                 .encode(line + "\n", "UTF-8").length();
                     }
                     debugIn.close();
                     debugIn = new BufferedReader(new FileReader(this.debugFile));
 
                     query = "description="
                             + URLEncoder.encode(description, "UTF-8")
                             + "&log="
                             + URLEncoder
                                     .encode(
                                             "\n\nAn error occured during upload, in JUpload\n"
                                                     + "All debug information is available below\n\n\n\n",
                                             "UTF-8");
                     request = new StringBuffer();
                     contentLength += query.length();
                     URL url = new URL(this.urlToSendErrorTo);
                     request
                             .append("POST ")
                             .append(url)
                             .append(" ")
                             .append(getServerProtocol())
                             .append("\r\n")
                             .append("Host: ")
                             .append(url.getHost())
                             .append("\r\n")
                             .append("Accept: */*\r\n")
                             .append(
                                     "Content-type: application/x-www-form-urlencoded\r\n")
                             .append("Connection: close\r\n").append(
                                     "Content-length: ").append(contentLength)
                             .append("\r\n");
                     // Get specific headers for this upload.
                     onAppendHeader(request);
                     // Blank line (end of header)
                     request.append("\r\n").append(query);
 
                     sock = new HttpConnect(this).Connect(url);
                     dataout = new DataOutputStream(new BufferedOutputStream(
                             sock.getOutputStream()));
                     datain = new BufferedReader(new InputStreamReader(sock
                             .getInputStream()));
 
                     // Send http request to server
                     action = "send bytes (1)";
                     dataout.writeBytes(request.toString());
                     dataout.writeBytes(query);
                     while ((line = debugIn.readLine()) != null) {
                         dataout.writeBytes(URLEncoder.encode(line + "\n",
                                 "UTF-8"));
                     }
                     debugIn.close();
                     // We are done with the debug log, so re-enable it.
                     this.debugOk = localDebugOk;
                     action = "flush";
                     dataout.flush();
                     action = "wait for server answer";
                     String strUploadSuccess = getStringUploadSuccess();
                     boolean uploadSuccess = false;
                     boolean readingHttpBody = false;
                     sbHttpResponseBody = new StringBuffer();
                     // Now, we wait for the full answer (which should mean that
                     // the uploaded message has been treated on the server).
                     while ((line = datain.readLine()) != null) {
                         // Is this upload a success ?
                         action = "test success";
                         if (line.matches(strUploadSuccess)) {
                             uploadSuccess = true;
                         }
 
                         // Store the http body
                         if (readingHttpBody) {
                             action = "sbHttpResponseBody";
                             sbHttpResponseBody.append(line).append("\n");
                         }
                         if (line.length() == 0) {
                             // Next lines will be the http body (or perhaps we
                             // already are in the body, but it's Ok anyway)
                             action = "readingHttpBody";
                             readingHttpBody = true;
                         }
                     }
                     // Is our upload a success ?
                     if (!uploadSuccess) {
                         throw new JUploadExceptionUploadFailed(
                                 getString("errHttpResponse"));
                     }
 
                 } catch (Exception e) {
                     this.debugOk = localDebugOk;
                     displayErr(getString("errDuringLogManagement") + " ("
                             + action + ")", e);
                 } finally {
                     this.debugOk = localDebugOk;
                     try {
                         dataout.close();
                     } catch (Exception e) {
                         displayErr(getString("errDuringLogManagement")
                                 + " (dataout.close)", e);
                     }
                     dataout = null;
                     try {
                         // Throws java.io.IOException
                         datain.close();
                     } catch (Exception e) {
                         // Nothing to do.
                     }
                     datain = null;
                     try {
                         // Throws java.io.IOException
                         sock.close();
                     } catch (Exception e) {
                         displayErr(getString("errDuringLogManagement")
                                 + " (sock.close)", e);
                     }
                     sock = null;
                     displayDebug("Sent to server: " + request.toString(), 100);
                     displayDebug("Body received: "
                             + sbHttpResponseBody.toString(), 100);
 
                 }
             }
         }
     }// sendDebugInformation
 
     /**
      * This method manages all applet parameters. It allows javascript to update
      * their value, for instance after the user chooses a value in a list ...
      * 
      * @throws JUploadException
      * @see wjhk.jupload2.policies.UploadPolicy#setProperty(java.lang.String,
      *      java.lang.String)
      */
     public void setProperty(String prop, String value) throws JUploadException {
         if (prop.equals(PROP_AFTER_UPLOAD_URL)) {
             setAfterUploadURL(value);
         } else if (prop.equals(PROP_ALLOW_HTTP_PERSISTENT)) {
             setAllowHttpPersistent(Boolean.parseBoolean(value));
         } else if (prop.equals(PROP_ALLOWED_FILE_EXTENSIONS)) {
             setAllowedFileExtensions(value);
         } else if (prop.equals(PROP_DEBUG_LEVEL)) {
             setDebugLevel(UploadPolicyFactory.parseInt(value, this.debugLevel,
                     this));
         } else if (prop.equals(PROP_LANG)) {
             setLang(value);
         } else if (prop.equals(PROP_FILENAME_ENCODING)) {
             setFilenameEncoding(value);
         } else if (prop.equals(PROP_LOOK_AND_FEEL)) {
             setLookAndFeel(value);
         } else if (prop.equals(PROP_MAX_CHUNK_SIZE)) {
             setMaxChunkSize(UploadPolicyFactory.parseLong(value,
                     this.maxChunkSize, this));
         } else if (prop.equals(PROP_MAX_FILE_SIZE)) {
             setMaxFileSize(UploadPolicyFactory.parseLong(value,
                     this.maxFileSize, this));
         } else if (prop.equals(PROP_NB_FILES_PER_REQUEST)) {
             setNbFilesPerRequest(UploadPolicyFactory.parseInt(value,
                     this.nbFilesPerRequest, this));
         } else if (prop.equals(PROP_POST_URL)) {
             setPostURL(value);
         } else if (prop.equals(PROP_SERVER_PROTOCOL)) {
             setServerProtocol(value);
         } else if (prop.equals(PROP_STRING_UPLOAD_SUCCESS)) {
             setStringUploadSuccess(value);
         } else if (prop.equals(PROP_URL_TO_SEND_ERROR_TO)) {
             setUrlToSendErrorTo(value);
         } else {
             displayWarn("Unknown applet parameter: " + prop
                     + " (in DefaultUploadPolicy.setProperty)");
         }
 
     }
 
     /**
      * This method displays the applet parameter list, according to the current
      * debugLevel. It is called by the {@link #setDebugLevel(int)} method. It
      * should be override by any subclasses, that should display its own
      * parameters, then call <I>super.displayParameterStatus()</I>.
      * 
      * @see UploadPolicy#displayParameterStatus()
      */
     public void displayParameterStatus() {
         // Let's handle the language:
         if (getDebugLevel() >= 20) {
             displayDebug(
                     "=======================================================================",
                     20);
             displayDebug("======= Parameters managed by DefaultUploadPolicy",
                     20);
             // /////////////////////////////////////////////////////////////////////////////
             // Let's display some information to the user, about the received
             // parameters.
             displayInfo("JUpload applet, version " + JUploadApplet.VERSION
                     + " (" + JUploadApplet.LAST_MODIFIED
                     + "), available at http://jupload.sourceforge.net/");
             displayDebug("Java version: " + System.getProperty("java.version"),
                     20);
             displayDebug("Cookie: " + this.cookie, 20);
             displayDebug("userAgent: " + this.userAgent, 20);
 
             displayDebug("List of all applet parameters:", 20);
             displayDebug("  language: "
                     + this.resourceBundle.getLocale().getLanguage(), 20);
             displayDebug("  country: "
                     + this.resourceBundle.getLocale().getCountry(), 20);
 
             displayDebug("afterUploadURL: " + getAfterUploadURL(), 20);
             displayDebug("  allowedFileExtensions: "
                     + getAllowedFileExtensions(), 20);
             displayDebug("debug: " + this.debugLevel, 1);
             displayDebug("filenameEncoding: " + this.filenameEncoding, 20);
             displayDebug("lang: " + this.lang, 20);
             displayDebug("maxChunkSize: " + this.maxChunkSize, 20);
             if (this.maxFileSize == Long.MAX_VALUE) {
                 // If the maxFileSize was not given, we display its value only
                 // in debug mode.
                 displayDebug("maxFileSize  : " + this.maxFileSize, 20);
             } else {
                 // If the maxFileSize was given, we always inform the user.
                 displayInfo("maxFileSize  : " + this.maxFileSize);
             }
             displayDebug("nbFilesPerRequest: " + this.nbFilesPerRequest, 20);
             displayDebug("postURL: " + this.postURL, 20);
             displayDebug("serverProtocol: " + this.serverProtocol, 20);
             displayDebug("showLogWindow: " + getShowLogWindow(), 20);
             displayDebug("stringUploadSuccess: " + this.stringUploadSuccess, 20);
             displayDebug("stringUploadError: " + this.stringUploadError, 20);
             displayDebug("urlToSendErrorTo: " + this.urlToSendErrorTo, 20);
             displayDebug("", 20);
         }
     }
 
     private final String normalizeURL(String url) throws JUploadException {
         if (null == url || url.length() == 0)
             return getApplet().getDocumentBase().toString();
         URI uri = null;
         try {
             uri = new URI(url);
             if (null == uri.getScheme())
                 uri = getApplet().getDocumentBase().toURI().resolve(url);
             if (!uri.getScheme().equals("http")
                     && !uri.getScheme().equals("https")
                     && !uri.getScheme().equals("ftp")) {
                 throw new JUploadException("URI scheme " + uri.getScheme()
                         + " not supported.");
             }
         } catch (URISyntaxException e) {
             throw new JUploadException(e);
         }
         return uri.toString();
     }
 
     // //////////////////////////////////////////////////////////////////////////////////////////////
     // /////////////////// getters / setters
     // ///////////////////////////////////////////////////
     // //////////////////////////////////////////////////////////////////////////////////////////////
 
     /** @see UploadPolicy#getAfterUploadURL() */
     public String getAfterUploadURL() {
         return this.afterUploadURL;
     }
 
     /**
      * Set the {@link #afterUploadURL}
      * 
      * @param afterUploadURL The URL to use.
      * @throws JUploadException
      */
     protected void setAfterUploadURL(String afterUploadURL)
             throws JUploadException {
         if (null == afterUploadURL)
             return;
         this.afterUploadURL = normalizeURL(afterUploadURL);
     }
 
     /**
      * @see wjhk.jupload2.policies.UploadPolicy#getAllowHttpPersistent()
      */
     public boolean getAllowHttpPersistent() {
         return this.allowHttpPersistent;
     }
 
     /** @see UploadPolicy#getAllowedFileExtensions() */
     public String getAllowedFileExtensions() {
         return this.allowedFileExtensions;
     }
 
     /** @param allowedFileExtensions the allowedFileExtensions to set */
     protected void setAllowedFileExtensions(String allowedFileExtensions) {
         if (allowedFileExtensions == null || allowedFileExtensions.equals("")) {
             this.allowedFileExtensions = null;
         } else {
             this.allowedFileExtensions = (allowedFileExtensions.startsWith("/") ? ""
                     : "/")
                     + allowedFileExtensions.toLowerCase()
                     + (allowedFileExtensions.endsWith("/") ? "" : "/");
         }
     }
 
     protected void setAllowHttpPersistent(boolean value) {
         this.allowHttpPersistent = value;
     }
 
     /** @see UploadPolicy#getApplet() */
     public JUploadApplet getApplet() {
         return this.applet;
     }
 
     /** @see UploadPolicy#getDebugLevel() */
     public int getDebugLevel() {
         return this.debugLevel;
     }
 
     /** @see UploadPolicy#setDebugLevel(int) */
     public void setDebugLevel(int debugLevel) {
         setDebugLevel(debugLevel, true);
     }
 
     /**
      * Set the debug level.
      * 
      * @param debugLevel The new debuglevel.
      * @param displayAppletParameterList Flag. If set to true, the applet's
      *            parameters are shown.
      */
     public void setDebugLevel(int debugLevel, boolean displayAppletParameterList) {
         // If the debugLevel was previously set, we inform the user of this
         // change.
         if (this.debugLevel >= 0) {
             displayInfo("Debug level set to " + debugLevel);
         }
         this.debugLevel = debugLevel;
 
         // The log window may become visible or hidden, depending on the current
         // debug level.
         if (getApplet().getUploadPanel() != null) {
             getApplet().getUploadPanel().showOrHideLogWindow();
 
             // Let's display the current applet parameters.
             if (displayAppletParameterList) {
                 displayParameterStatus();
             }
         }
     }
 
     /**
      * This method sets the current language to take into account. It loads the
      * {@link #resourceBundle}, which will allow the applet to display the
      * texts in the correct language.
      * 
      * @param lang The new language to take into account. See the
      *            java.util.Locale(String) constructor for a list of valid
      *            values.
      */
     protected void setLang(String lang) {
         Locale locale;
         this.lang = lang;
         if (lang == null) {
             displayInfo("lang = null, taking default language");
             locale = Locale.getDefault();
         } else {
             // If we have a 5 characters lang string, then it should look like
             // ll_CC, where ll is the language code
             // and CC is the Contry code.
             if (lang.length() == 5
                     && (lang.substring(2, 3).equals("_") || lang
                             .substring(2, 3).equals("-"))) {
                 String language = lang.substring(0, 2);
                 String country = lang.substring(3, 5);
                 displayDebug("setLang - language read: " + language, 50);
                 displayDebug("setLang - country read: " + country, 50);
                 locale = new Locale(language, country.toUpperCase());
             } else {
                 locale = new Locale(lang);
             }
         }
         this.resourceBundle = ResourceBundle.getBundle(
                 "wjhk.jupload2.lang.lang", locale);
     }
 
     protected String getLookAndFeel() {
         return this.lookAndFeel;
     }
 
     /** @param lookAndFeel the lookAndFeel to set */
     protected void setLookAndFeel(String lookAndFeel) {
         this.lookAndFeel = lookAndFeel;
         if (lookAndFeel != null && !lookAndFeel.equals("")
                 && !lookAndFeel.equals("java")) {
             // We try to call the UIManager.setLookAndFeel() method. We catch
             // all possible exceptions, to prevent
             // that the applet is blocked.
             try {
                 if (!lookAndFeel.equals("system")) {
                     // Correction given by Fritz. Thanks to him.
                     UIManager.setLookAndFeel(lookAndFeel);
                 } else {
                     UIManager.setLookAndFeel(UIManager
                             .getSystemLookAndFeelClassName());
                 }
             } catch (Exception e) {
                 displayErr(e);
             }
         }
     }
 
     /** @see wjhk.jupload2.policies.UploadPolicy#getMaxChunkSize() */
     public long getMaxChunkSize() {
         return this.maxChunkSize;
     }
 
     /** @param maxChunkSize the maxChunkSize to set */
     protected void setMaxChunkSize(long maxChunkSize) {
         if (maxChunkSize < 0) {
             displayDebug(
                     "maxChunkSize<0 which is invalid. Switched to the default value (Long.MAX_VALUE)",
                     1);
             maxChunkSize = Long.MAX_VALUE;
         }
         this.maxChunkSize = maxChunkSize;
     }
 
     /** @see wjhk.jupload2.policies.UploadPolicy#getMaxFileSize() */
     public long getMaxFileSize() {
         return this.maxFileSize;
     }
 
     /** @param maxFileSize the maxFileSize to set */
     protected void setMaxFileSize(long maxFileSize) {
         if (maxFileSize < 0) {
             displayDebug(
                     "maxFileSize<0 which is invalid. Switched to the default value (Long.MAX_VALUE)",
                     1);
             maxFileSize = Long.MAX_VALUE;
         }
         this.maxFileSize = maxFileSize;
     }
 
     /** @see wjhk.jupload2.policies.UploadPolicy#getNbFilesPerRequest() */
     public int getNbFilesPerRequest() {
         return this.nbFilesPerRequest;
     }
 
     /** @param nbFilesPerRequest the nbFilesPerRequest to set */
     protected void setNbFilesPerRequest(int nbFilesPerRequest) {
         if (nbFilesPerRequest < 0) {
             displayDebug(
                     "nbFilesPerRequest<0 which is invalid. Switched to the default value (Integer.MAX_VALUE)",
                     1);
             nbFilesPerRequest = Integer.MAX_VALUE;
         }
         this.nbFilesPerRequest = nbFilesPerRequest;
     }
 
     /** @see UploadPolicy#getFilenameEncoding() */
     public String getFilenameEncoding() {
         return this.filenameEncoding;
     }
 
     /** @param filenameEncoding the filenameEncoding to set */
     protected void setFilenameEncoding(String filenameEncoding) {
         this.filenameEncoding = filenameEncoding;
     }
 
     /** @see wjhk.jupload2.policies.UploadPolicy#getPostURL() */
     public String getPostURL() {
         return this.postURL;
     }
 
     /**
      * @throws JUploadException
      * @see wjhk.jupload2.policies.UploadPolicy#setPostURL(String)
      */
     public void setPostURL(String postURL) throws JUploadException {
         // Be more forgiving about postURL:
         // - If none is specified, use the original DocumentBase of the applet.
         // - If a non-absolute URI (an URI without protocol and server) is
         // specified,
         // prefix it with "http://servername"
         // - If a relative URI is specified, prefix it with the DocumentBase's
         // parent
         this.postURL = normalizeURL(postURL);
     }
 
     /** @see wjhk.jupload2.policies.UploadPolicy#getServerProtocol() */
     public String getServerProtocol() {
         return this.serverProtocol;
     }
 
     /**
     * @param serverProtocol the serverProtocol to set
      * @throws JUploadException
      */
     protected void setServerProtocol(String value)
             throws JUploadException {
         if (null == value || value.equals("")) {
             if (null == this.postURL)
                 throw new JUploadException("postURL not set");
             try {
                 value = new HttpConnect(this).getProtocol();
             } catch (ConnectException e) {
                 throw new JUploadException(e);
             }
         }
         this.serverProtocol = value;
     }
 
     /** @see wjhk.jupload2.policies.UploadPolicy#getServerProtocol() */
     public boolean getShowLogWindow() {
         return this.showLogWindow;
     }
 
     /** @param showLogWindow the new showLogWindow value */
     protected void setShowLogWindow(boolean showLogWindow) {
         this.showLogWindow = showLogWindow;
         // The log window may become visible or hidden, depending on this
         // parameter.
         if (getApplet().getUploadPanel() != null) {
             getApplet().getUploadPanel().showOrHideLogWindow();
         }
     }
 
     /** @see wjhk.jupload2.policies.UploadPolicy#getStringUploadError() */
     public String getStringUploadError() {
         return this.stringUploadError;
     }
 
     /** @see wjhk.jupload2.policies.UploadPolicy#getStringUploadSuccess() */
     public String getStringUploadSuccess() {
         return this.stringUploadSuccess;
     }
 
     /**
      * @param stringUploadError the stringUploadError to set
      * @throws JUploadException
      */
     protected void setStringUploadError(String stringUploadError)
             throws JUploadException {
         this.stringUploadError = stringUploadError;
         try {
             this.patternError = Pattern.compile(stringUploadError);
         } catch (PatternSyntaxException e) {
             throw new JUploadException(
                     "Invalid regex in parameter stringUploadError");
         }
     }
 
     /**
      * @param stringUploadSuccess the stringUploadSuccess to set
      * @throws JUploadException
      */
     protected void setStringUploadSuccess(String stringUploadSuccess)
             throws JUploadException {
         this.stringUploadSuccess = stringUploadSuccess;
         try {
             this.patternSuccess = Pattern.compile(stringUploadSuccess);
         } catch (PatternSyntaxException e) {
             throw new JUploadException(
                     "Invalid regex in parameter stringUploadSuccess");
         }
     }
 
     /** @see wjhk.jupload2.policies.UploadPolicy#getUrlToSendErrorTo() */
     public String getUrlToSendErrorTo() {
         return this.urlToSendErrorTo;
     }
 
     /**
      * @param urlToSendErrorTo the urlToSendErrorTo to set
      * @throws JUploadException
      */
     protected void setUrlToSendErrorTo(String urlToSendErrorTo)
             throws JUploadException {
         if (null == urlToSendErrorTo)
             return;
         String tmp = normalizeURL(urlToSendErrorTo);
         if (tmp.startsWith("ftp://")) {
             throw new JUploadException(
                     "urlToSendErrorTo: ftp scheme not supported.");
         }
         this.urlToSendErrorTo = tmp;
     }
 
     /** @see wjhk.jupload2.policies.UploadPolicy#getFormdata() */
     public String getFormdata() {
         return this.formData;
     }
 
     /** @see wjhk.jupload2.policies.UploadPolicy#getAfterUploadTarget() */
     public String getAfterUploadTarget() {
         return this.afterUploadTarget;
     }
 
     // //////////////////////////////////////////////////////////////////////////////////////////////
     // /////////////////// Internal methods
     // ///////////////////////////////////////////////////
     // //////////////////////////////////////////////////////////////////////////////////////////////
 
     /**
      * Delete the current log. (called upon applet termination)
      */
     public void deleteLog() {
         System.out.println("Deleting log");
         try {
             if (null != this.debugOut) {
                 this.debugOut.close();
                 this.debugOut = null;
             }
             if (null != this.debugFile) {
                 this.debugFile.delete();
                 this.debugFile = null;
             }
         } catch (Exception e) {
             // nothing to do
         }
     }
 
     /**
      * This methods allows the applet to store all messages (debug, warning,
      * info, errors...) into a StringBuffer. If any problem occurs, the whole
      * output (displayed or not by the displayDebug, for instance) can be stored
      * in a file, or sent to the webmaster. This can help to identify and
      * correct problems that can occurs on the various computer configurations.
      * 
      * @param msg
      */
     private void addMsgToDebugLog(String msg) {
         // If uploading lots of chunks, the buffer gets too large, resulting in
         // a OutOfMemoryError on the heap so we now use a temporary file for the
         // debug log.
         if (this.debugOk) {
             try {
                 if (null == this.debugOut) {
                     this.getApplet().registerUnload(this, "deleteLog");
                     this.debugFile = File
                             .createTempFile("jupload_", "_log.txt");
                     this.debugOut = new PrintStream(new FileOutputStream(
                             this.debugFile));
                 }
                 boolean endsLF = msg.endsWith("\n");
                 msg = msg.replaceAll("\n", CRLF);
                 if (endsLF)
                     this.debugOut.print(msg);
                 else
                     this.debugOut.println(msg);
             } catch (IOException e) {
                 this.debugOk = false;
                 System.err.println("IO error on debuglog "
                         + this.debugFile.getPath()
                         + "\nFallback to standard output.");
                 System.out.println(msg);
             }
         } else {
             System.out.println(msg);
         }
     }
 
     private final String timestamp(String tag, String s) {
         final String stamp = new SimpleDateFormat("HH:mm:ss.SSS ")
                 .format(new Date())
                 + tag;
         final boolean endsLF = s.endsWith("\n");
         if (endsLF)
             s = s.substring(0, s.length() - 1);
         return (stamp + s.replaceAll("\n", "\n" + stamp) + (endsLF ? "\n" : ""));
     }
 
     /**
      * Displays a message. If the logWindow panel is set, the message is
      * displayed on it. If not, the System.out.println function is used.
      * 
      * @param msg The message to display.
      */
     private synchronized void displayMsg(String tag, String msg) {
         msg = timestamp(tag, msg);
         if (this.logWindow == null) {
             System.out.println(msg);
         } else {
             this.logWindow.append(msg);
             if (!msg.endsWith("\n"))
                 this.logWindow.append("\n");
             int lc = this.logWindow.getLineCount();
             if (lc > MAX_DEBUG_LINES) {
                 int end;
                 try {
                     end = this.logWindow.getLineEndOffset(lc - MAX_DEBUG_LINES);
                     this.logWindow.replaceRange("", 0, end);
                 } catch (BadLocationException e) {
                     e.printStackTrace();
                 }
             }
         }
         // Let's store all text in the debug logfile
         addMsgToDebugLog(msg);
     }
 
     /**
      * This method returns the response for the
      * {@link JUploadFileFilter#accept(File)} which just calls this method. This
      * method checks that the file extension corresponds to the
      * allowedFileExtensions applet parameter.
      * 
      * @see UploadPolicy#fileFilterAccept(File)
      */
     public boolean fileFilterAccept(File file) {
         if (file.isDirectory()) {
             return true;
         } else if (this.allowedFileExtensions == null
                 || this.allowedFileExtensions.equals("")) {
             return true;
         } else {
             // Get the file extension
             String filename = file.getName();
             int point = filename.lastIndexOf('.');
             // Do our filename ends with a point ?
             if (point == filename.length() - 1) {
                 return false;
             }
             String extension = filename.substring(point + 1).toLowerCase();
             // allowedFileExtensions is :
             // - a list of file extensions,
             // - in lower case,
             // - separated by slash
             // - A slash has been added at the beginning in
             // setAllowedFileExtensions
             // - A slash has been added at the end in setAllowedFileExtensions
             // So, we just look for the /ext/ string in the stored
             // allowedFileExtensions.
             return (this.allowedFileExtensions.indexOf("/" + extension + "/")) >= 0;
         }
     }
 
     /** @see UploadPolicy#fileFilterGetDescription() */
     public String fileFilterGetDescription() {
         if (this.allowedFileExtensions == null
                 || this.allowedFileExtensions.equals(""))
             return null;
 
         return "JUpload file filter (" + this.allowedFileExtensions + ")";
     }
 
     /**
      * Returns null: the default icon is used.
      * 
      * @see UploadPolicy#fileViewGetIcon(File)
      */
     public Icon fileViewGetIcon(@SuppressWarnings("unused")
     File file) {
         return null;
     }
 
 }
