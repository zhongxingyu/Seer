 package org.jbundle.model.util;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.net.URL;
 import java.net.URLDecoder;
 import java.net.URLEncoder;
 import java.util.Map;
 import java.util.logging.ConsoleHandler;
 import java.util.logging.Level;
 import java.util.logging.LogManager;
 import java.util.logging.Logger;
 import java.util.logging.SimpleFormatter;
 
 import javax.swing.JComponent;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.border.Border;
 import javax.swing.border.LineBorder;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.jbundle.model.Service;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.xml.sax.ErrorHandler;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.xml.sax.SAXParseException;
 
 /**
 * Basic utilities.
  */
 public class Util extends Object
 {
     /**
      * A new line.
      */
     public static final char NEW_LINE = '\n';
 
     /**
      * Add this param and data to this URL.
      * @param strOldURL The original URL to add this param to.
      * @param strParam The parameter to add.
      * @param strData The data this parameter is set to.
      * @return The new URL string.
      */
     public static String addURLParam(String strOldURL, String strParam, String strData)
     {
         return Util.addURLParam(strOldURL, strParam, strData, true);
     }
     /**
      * Add this param and data to this URL.
      * @param strOldURL The original URL to add this param to.
      * @param strParam The parameter to add.
      * @param strData The data this parameter is set to.
      * @param bAddIfNull Add an empty param if the data is null?
      * @return The new URL string.
      */
     public static String addURLParam(String strOldURL, String strParam, String strData, boolean bAddIfNull)
     {
         String strURL = strOldURL;
         if ((strOldURL == null) || (strOldURL.length() == 0))
             strURL = "?";
         else if (strOldURL.indexOf('?') == -1)
             strURL += "?";
         else
             strURL += "&";
         if (strData == null)
         {
             if (!bAddIfNull)
                 return strOldURL; // Don't add a null param.
             strData = Constant.BLANK;
         }
         try {
             strURL += URLEncoder.encode(strParam, Constant.URL_ENCODING) + '=' + URLEncoder.encode(strData, Constant.URL_ENCODING);
         } catch (java.io.UnsupportedEncodingException ex) {
             ex.printStackTrace();
         }
         return strURL;
     }
     /**
      * Parse this URL formatted string into properties.
      * @properties The properties object to add the params to.
      * @args The arguments to parse (each formatted as key=value).
      */
     public static void parseArgs(Map<String,Object> properties, String[] args)
     {
         if (args == null)
             return;
         for (int i = 0; i < args.length; i++)
             Util.addParam(properties, args[i], false);
     }
     /**
      * Parse this URL formatted string into properties.
      * @properties The properties object to add the params to.
      * @args The URL to parse (formatted as: XYZ?key1=value1&key2=value2).
      */
     public static void parseArgs(Map<String,Object> properties, String strURL)
     {
         int iIndex = 0;
         int iStartIndex = strURL.indexOf('?') + 1;  // Start of first param (0 if no ?)
         while ((iIndex = strURL.indexOf('=', iIndex)) != -1)
         {
             int iEndIndex = strURL.indexOf('&', iIndex);
             if (iEndIndex == -1)
                 iEndIndex = strURL.length();
             if (iStartIndex < iEndIndex)
                 Util.addParam(properties, strURL.substring(iStartIndex, iEndIndex), true);
             iStartIndex = iEndIndex + 1;
             iIndex++;
         }
     }
     /**
      * Parse the param line and add it to this properties object.
      * (ie., key=value).
      * @properties The properties object to add this params to.
      * @param strParam param line in the format param=value
      */
     public static void addParam(Map<String,Object> properties, String strParams, boolean bDecodeString)
     {
         int iIndex = strParams.indexOf('=');
         int iEndIndex = strParams.length();
         if (iIndex != -1)
         {
             String strParam = strParams.substring(0, iIndex);
             String strValue = strParams.substring(iIndex + 1, iEndIndex);
             if (bDecodeString)
             {
                 try {
                     strParam = URLDecoder.decode(strParam, Constant.URL_ENCODING);
                     strValue = URLDecoder.decode(strValue, Constant.URL_ENCODING);
                 } catch (java.io.UnsupportedEncodingException ex) {
                     ex.printStackTrace();
                 }
             }
             properties.put(strParam, strValue);
         }
     }
     /**
      * Add this param to this path.
      * @param strOldURL The original URL to add this param to.
      * @param strParam The parameter to add.
      * @param strData The data this parameter is set to.
      * @param bAddIfNull Add an empty param if the data is null?
      * @return The new URL string.
      */
     public static String addToPath(String basePath, String fileName)
     {
         String strFileSeparator = System.getProperty("file.separator");
         char chFileSeparator = '/';
         if ((strFileSeparator != null) && (strFileSeparator.length() == 1))
             chFileSeparator = strFileSeparator.charAt(0);
         StringBuilder sb = new StringBuilder();
         if (basePath != null)
             sb.append(basePath);
         if (sb.length() > 0)
         	if (sb.charAt(sb.length() - 1) != chFileSeparator)
         		sb.append(chFileSeparator);
         if (fileName != null)
         {
             if ((fileName.length() > 0) && (fileName.charAt(0) == chFileSeparator) && (sb.length() > 0))
                 sb.append(fileName.substring(1));
             else
                 sb.append(fileName);
         }
         return sb.toString();
     }
     /**
      * Is this string a valid number?
      * @param string The string to check.
      * @return true if this string is a valid number (all characters numeric).
      */
     public static boolean isNumeric(String string)
     {
     	return Util.isNumeric(string, false);
     }
     /**
      * Is this string a valid number?
      * @param string The string to check.
      * @return true if this string is a valid number (all characters numeric).
      */
     public static boolean isNumeric(String string, boolean bAllowFormatting)
     {
         if ((string == null) || (string.length() == 0))
             return false;
         boolean bIsNumeric = true;
         for (int i = 0; i < string.length(); i++)
         {
             if (!Character.isDigit(string.charAt(i)))
             {
             	if ((!bAllowFormatting)
             		|| (Character.isLetter(string.charAt(i))))
             			bIsNumeric = false;
             }
         }
         return bIsNumeric;
     }
     /**
      * Set the message data as a XML String.
      * @return
      */
     public static Document convertXMLToDOM(String strXML)
     {
         DocumentBuilder db = Util.getDocumentBuilder();
         // Parse the input file
         Document doc = null;
         try {
             synchronized (db)
             {   // db is not thread safe
                 doc = db.parse(new InputSource(new StringReader(strXML)));
             }
         } catch (SAXException se) {
             System.out.println(se.getMessage());
             return null;
         } catch (IOException ioe) {
             System.out.println(ioe);
             return null;
         } catch (Exception ioe) {
             System.out.println(ioe);
             return null;
         }
         return doc;
     }
     /**
      * Send this document as text to this output stream.
      * @param doc
      * @return The converted XML
      */
     public static String convertDOMToXML(Node doc)
     {
         StringWriter out = new StringWriter();
         Util.convertDOMToXML(doc, out);
         return out.toString();
     }
     /**
      * Send this document as text to this output stream.
      * @param doc
      */
     public static void convertDOMToXML(Node doc, Writer out)
     {
         try {
             // Use a Transformer for output
             TransformerFactory tFactory = TransformerFactory.newInstance();
             Transformer transformer = tFactory.newTransformer();
 
             DOMSource source = new DOMSource(doc);
 
             StreamResult result = new StreamResult(out);
             transformer.transform(source, result);
         } catch (TransformerConfigurationException tce) {
             // Error generated by the parser
             tce.printStackTrace();
         } catch (TransformerException te) {
             // Error generated by the parser
             te.printStackTrace();
         }
     }
     /**
      * Get the document builder.
      * The document builder is the parsing XML engine.
      * NOTE: Since the document builder can't be considered thread safe, you MUST synchronize on db before doing any document commands
      * @return The document builder.
      */
     private static final ThreadLocal < DocumentBuilder > m_dbs = 
         new ThreadLocal < DocumentBuilder > () {
             @Override protected DocumentBuilder initialValue() {
             DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 
             // Optional: set various configuration options
             dbf.setValidating(false);
             dbf.setNamespaceAware(true);
             dbf.setIgnoringElementContentWhitespace(false);
             
             DocumentBuilder db = null;
             try {
                 db = dbf.newDocumentBuilder();
             } catch (ParserConfigurationException ex) {
                 ex.printStackTrace();
                 System.exit(1);
             }
             // Set an ErrorHandler before parsing
             db.setErrorHandler(new MyErrorHandler(null));
 
             return db;
         }
     };
 
     public static DocumentBuilder getDocumentBuilder()
     {
         return m_dbs.get();
     }
     /**
      * MyErrorHandler receives error notifications from the DocumentBuilder.
      */
     static class MyErrorHandler extends Object
         implements ErrorHandler
     {
         /**
          * Constructor.
          */
         public MyErrorHandler(Object obj)
         {
         }
         /**
           * Receive notification of a recoverable error.
           */
         public void error(SAXParseException exception)
         {
             System.out.println(exception.getMessage());
         }
         /**
           * Receive notification of a non-recoverable error.
           */
         public void fatalError(SAXParseException exception)
         {
             System.out.println(exception.getMessage());
         }
         /**
           * Receive notification of a recoverable warning.
           */
         public void warning(SAXParseException exception)
         {
             System.out.println(exception.getMessage());
         }
     }
     public static StringBuffer addStartTag(StringBuffer sbXML, String strTag)
     {
         return sbXML.append('<').append(strTag).append('>');
     }
     public static StringBuffer addEndTag(StringBuffer sbXML, String strTag)
     {
         return sbXML.append("</").append(strTag).append('>');
     }
    /**
     * Add this map as an xml string.
     */
    public static StringBuffer addXMLMap(StringBuffer sbXML, Map<String,Object> mapMessage)
    {
        if (sbXML == null)
            sbXML = new StringBuffer();
        if (mapMessage != null)
        {
            for (String strParam : mapMessage.keySet())
            {
                Object objValue = mapMessage.get(strParam);
                if (objValue != null)
                    Util.getXML(sbXML, strParam, objValue);
            }
        }
        return sbXML;
    }
    /**
     * Convert this value to a XML string.
     * @param This value's tag.
     * @param objValue The raw data value.
     * @return The XML string for this value.
     */
    public static StringBuffer getXML(StringBuffer sbXML, String strParam, Object objValue)
    {
        Util.addStartTag(sbXML, strParam);
        if (objValue instanceof Map)
        {
            Util.addXMLMap(sbXML, (Map)objValue);
        }
        else
        {
            String strValue = Constant.BLANK;
            if (objValue != null)
                strValue = objValue.toString();
            if (Util.isCData(strValue))
                strValue = CDATA_START + strValue + CDATA_END;
            sbXML.append(strValue);
        }
        return Util.addEndTag(sbXML, strParam).append(NEW_LINE);
    }
    public static final String CDATA_START = "![CDATA[";
    public static final String CDATA_END = "]]";
    /**
     * Is this a CData string?
     * @param string The string to analyze.
     * @return true if this string must be a CDATA area (if it has carriage-returns).
     */
    public static boolean isCData(String string)
    {
        if (string != null)
        {
            for (int i = 0; i < string.length(); i++)
            {
                char chChar = string.charAt(i);
                if ((chChar == 0x9)
                    || (chChar == 0xD)
                    || (chChar == 0xA))
                        return true;        // Carriage return/Line feed/newline
            }
        }
        return false;
    }
    /**
     * Fix this string so it can be a DOM element name.
     */
    public static String fixDOMElementName(String string)
    {
        for (int i = 0; i < string.length(); i++)
        {
            if (!Character.isJavaIdentifierPart(string.charAt(i)))
                string = string.substring(0, i) + "." + string.substring(i + 1);
        }
        return string;
    }
    /**
     * Fake disable a control.
     */
    public static void setEnabled(Component component, boolean bEnabled)
    {
        if (component instanceof JPanel)
        {
            for (int i = 0; i < ((JPanel)component).getComponentCount(); i++)
            {
                JComponent componentSub = (JComponent)((JPanel)component).getComponent(i);
                Util.setEnabled(componentSub, bEnabled);
            }
        }
        else if (component instanceof JScrollPane)
        {
            JComponent componentSub = (JComponent)((JScrollPane)component).getViewport().getView();
            Util.setEnabled(componentSub, bEnabled);
        }
        else
            component.setEnabled(bEnabled);
    }
    /**
     * A utility method to get an Input stream from a filename or URL string.
     * @param strFilename The filename or url to open as an Input Stream.
     * @return The imput stream (or null if there was an error).
     */
    public static InputStream getInputStream(String strFilename, Service app)
    {
        InputStream streamIn = null;
        if ((strFilename != null) && (strFilename.length() > 0))
        {
            try   {
                URL url = null;
                if (strFilename.indexOf(':') != -1)
                    url = new URL(strFilename);
                if (url == null)
                    if (app != null)
                        url = app.getResourceURL(strFilename, null);
                if (url != null)
                    streamIn = url.openStream();
            } catch (Exception ex)  {
                streamIn = null;
            }
            if (streamIn == null)
            {
                File file = new File(strFilename);
                try   {
                    streamIn = new FileInputStream(file);
                } catch (FileNotFoundException ex)  {
                    streamIn = null;
                }
            }
        }
        return streamIn;
    }
 
    public static final Border GRAY_BORDER = new LineBorder(Color.LIGHT_GRAY);
    public static final Border BLACK_BORDER = new LineBorder(Color.BLACK);
 
     /**
      *
      */
     public static String getFullClassName(String strClassName) {
         return Util.getFullClassName(null, strClassName);
     }
     /**
      *
      */
     public static String getFullClassName(String strPackage, String strClass) {
         if (strPackage != null)
             if (strPackage.length() > 0) {
                 if (strPackage.charAt(strPackage.length() - 1) != '.')
                     strPackage = strPackage + '.';
             }
         if (strClass != null)
             if (strClass.length() > 0) {
                 if (strClass.indexOf('.') == -1)
                     if (strPackage != null)
                         strClass = strPackage + strClass;
                 if (strClass.charAt(0) == '.')
                     strClass = Constant.ROOT_PACKAGE + strClass.substring(1);
             }
         return strClass;
     }
    /**
     * Convert this class name by inserting this package after the domain.
     * ie., com.xyz.abc.ClassName -> com.xyz.newpackage.abc.ClassName.
     * @param className
     * @param stringToInsert
     * @return Converted string
     */
    public static String convertClassName(String className, String stringToInsert)
    {
        int iStartSeq = className.indexOf('.');	// Constants.ROOT_PACKAGE.length();
        if (iStartSeq != -1)
     	   iStartSeq = className.indexOf('.', iStartSeq + 1);
        int domainSeq = iStartSeq;
        if (className.indexOf(Constant.THIN_SUBPACKAGE, iStartSeq) == iStartSeq + 1)
            iStartSeq = iStartSeq + Constant.THIN_SUBPACKAGE.length();
        className = className.substring(0, domainSeq + 1) + stringToInsert + className.substring(iStartSeq + 1);
        return className;
    }
    /**
     * Get the package name of this class name
     * @param className
     * @return
     */
    public static String getPackageName(String className)
    {
 	   return Util.getPackageName(className, false);
    }
    /**
     * Get the package name of this class name
     * @param className
     * @return
     */
    public static String getPackageName(String className, boolean resource)
    {
        String packageName = null;
        if (className != null)
        {
        	if (resource)
        		if (className.endsWith(PROPERTIES))
        			className = className.substring(0, className.length() - PROPERTIES.length());
            if (className.lastIndexOf('.') != -1)
                packageName = className.substring(0, className.lastIndexOf('.'));
        }
        return packageName;
    }
    public static final String PROPERTIES = ".properties";
    /**
     * Get this image's full filename.
  * @param strSubDirectory The sub-directory.
  * @param fixRelativePaths TODO
  * @param filename The filename of this image (if no path, assumes images/buttons; if not ext assumes .gif).
     * @return The full (relative) filename for this image.
     */
    public static String getFullFilename(String strFilename, String strSubDirectory, String fileLocation, boolean fixRelativePaths)
    {
 	   String localLocation = fileLocation.substring(fileLocation.lastIndexOf('/') + 1);
        if (((strFilename.indexOf(File.separator) == -1) && (strFilename.indexOf('/') == -1)) && (strSubDirectory != null))
            strFilename = fileLocation + File.separator + strSubDirectory + File.separator + strFilename;
        else if ((strFilename.indexOf(localLocation + File.separator) == -1) && (strFilename.indexOf(localLocation + "/") == -1))
            strFilename = fileLocation + File.separator + strFilename;
        else if ((strFilename.indexOf(localLocation + File.separator) == 0) || (strFilename.indexOf(localLocation + "/") == 0))
            strFilename = fileLocation + strFilename.substring(localLocation.length());
 	   if (fixRelativePaths)
 	   {	// Remove '/../'
 		   while (strFilename.indexOf("/..") != -1)
 		   {
 			   int startPath = strFilename.indexOf("/..");
 			   int startPrev = strFilename.lastIndexOf("/", startPath - 1);
 			   if (startPrev == -1)
 				   break;
 			   strFilename = strFilename.substring(0, startPrev) + strFilename.substring(startPath + 3);
 		   }
 	   }
        return strFilename;
    }
    /**
     * Get the logger.
     * @return The logger.
     */
    public static Logger getLogger()
    {
        if (m_logger == null)
        {
            m_logger = Logger.getLogger(Constant.ROOT_PACKAGE.substring(0, Constant.ROOT_PACKAGE.length() - 1));
            try {
                LogManager logManager = LogManager.getLogManager();
                logManager.reset();//removeAllGlobalHandlers();
                ConsoleHandler fh = new ConsoleHandler();
                fh.setFormatter(new SimpleFormatter());
                // Send logger output to our FileHandler.
                m_logger.addHandler(fh);
            } catch (java.security.AccessControlException ex)    {
                // Access error if running in an applet.
         	   m_logger = Logger.getAnonymousLogger();
            } finally {
                try {
 	               // Request that every detail gets logged.
 //                 m_logger.setLevel(Level.OFF);
 //                 m_logger.setLevel(Level.ALL);
 	               m_logger.setLevel(Level.WARNING);        	   
                } catch (java.security.AccessControlException ex)    {
             	   ex.printStackTrace();	// Never
                }
            }
        }
        return m_logger;
    }
    /**
     * 
     */
    public static void setLogger(Logger logger)
    {
        m_logger = logger;
 //     m_logger.setLevel(Level.ALL);
        if (m_logger != null)
     	   m_logger.setLevel(Level.WARNING);
    }
    private static Logger m_logger = null;
 }
