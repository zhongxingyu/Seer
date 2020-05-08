 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package jp.co.antenna.XfoJavaCtl;
 
 import java.io.*;
 
 /**
  * XfoObj Class is the object class of XSL Formatter
  * 
  * @author Test User
  */
 public class XfoObj {
     // Consts
     public static final int EST_NONE = 0;
     public static final int EST_STDOUT = 1;
     public static final int EST_STDERR = 2;
     
     // Attributes
     private String executable;
     private String commandline;
     private Runtime r;
     private String inputDoc;
     private String outputDoc;
     private String exitLevel;
     private String printer;
     private String stylesheetDoc;
     private MessageListener messageListener;
     private String multivol;
     private String optionFile;
     
     // Methods
     /**
      * Create the instance of XfoObj, and initialize it.
      * 
      * @throws Exception
      */
     public XfoObj () throws XfoException {
         // Check EVs and test if XslCmd.exe exists.
         String axf_home = System.getenv("AXF43_HOME");
         if (axf_home == null)
             throw new XfoException(4, 1, "Can't find %AXF43_HOME%");
         this.executable = axf_home + "\\XslCmd.exe";
         // setup attributes
         this.Clear();
     }
     
     /**
      * Cleanup (initialize) XSL Formatter engine.
      */
     public void Clear () {
         // reset attributes        
         this.r = Runtime.getRuntime();
         this.commandline = this.executable;
         this.inputDoc = null;
         this.outputDoc = null;
         this.exitLevel = null;
         this.stylesheetDoc = null;
         this.multivol = null;
         this.optionFile = null;
     }
     
     /**
      * Execute formatting and outputs to a PDF. 
      * 
      * @throws jp.co.antenna.XfoJavaCtl.XfoException
      */
     public void execute () throws XfoException {
         // Run Formatter with Runtime.exec()
         String localCommandline = this.commandline;
         Process process;
         int exitCode = -1;
         if (this.inputDoc != null)
             localCommandline += this.inputDoc;
         if (this.stylesheetDoc != null)
             localCommandline += this.stylesheetDoc;
         if (this.optionFile != null)
             localCommandline += this.optionFile;
         if (this.outputDoc != null)
             localCommandline += this.outputDoc;
         if (this.exitLevel != null)
             localCommandline += this.exitLevel;
         if (this.printer != null)
             localCommandline += this.printer;
         if (this.multivol != null)
             localCommandline += this.multivol;
         try {
             process = this.r.exec(localCommandline);
             exitCode = process.waitFor();
         } catch (Exception e) {}
         if (exitCode != 0)
             throw new XfoException(4, 1, "Something went wrong.");
     }
     
     /**
      * Executes the formatting of XSL-FO document specified for src, and outputs it to dst in the output form specified for dst.
      * 
      * @param src   XSL-FO Document
      * @param dst   output stream
      * @param outDevice output device. Please refer to a setPrinterName method about the character string to specify. 
      * @throws jp.co.antenna.XfoJavaCtl.XfoException
      */
     public void render (InputStream src, OutputStream dst, String outDevice) throws XfoException {
         // Fill this in.
     }
     
     /**
      * Register the MessageListener interface to the instance of implemented class.
      * <br>The error that occurred during the formatting process can be received as the event. 
      * 
      * @param listener The instance of implemented class
      */
     public void setMessageListener (MessageListener listener) {
         // Fake it.
         this.messageListener = listener;
     }
     
     /**
      * Set the URI of XML document to be formatted.
      * <br>If specified "@STDIN", XML document reads from stdin. The document that is read from stdin is assumed to be FO. 
      * 
      * @param uri URI of XML document
      * @throws jp.co.antenna.XfoJavaCtl.XfoException
      */
     public void setDocumentURI (String uri) throws XfoException {
         // Set the URI...
         if (uri != null && !uri.equals(""))
             this.inputDoc = " -d " + uri;
         else
             this.inputDoc = null;
     }
     
     /**
      * Specifies the output file path of the formatted result.
      * <br>When the printer is specified as an output format by setPrinterName, a
      * printing result is saved to the specified file by the printer driver.
      * <br>When output format other than a printer is specified, it is saved at the 
      * specified file with the specified output format.
      * <br>When omitted, or when "@STDOUT" is specified, it comes to standard output. 
      * 
      * @param path Path name of output file
      * @throws jp.co.antenna.XfoJavaCtl.XfoException
      */
     public void setOutputFilePath (String path) throws XfoException {
         // Set the path...
         if (path != null && !path.equals(""))
             this.outputDoc = " -o " + path;
         else
             this.outputDoc = null;
     }
     
     /**
      * Set the error level to abort formatting process.
      * <br>XSL Formatter will stop formatting when the detected error level is equal to setExitLevel setting or higher.
      * <br>The default value is 2 (Warning). Thus if an error occurred and error level is 2 (Warning) or higher, the 
      * formatting process will be aborted. Please use the value from 1 to 4. When the value of 5 or more is specified, 
      * it is considered to be the value of 4. If a error-level:4 (fatal error) occurs, the formatting process will be 
      * aborted unconditionally. Note: An error is not displayed regardless what value may be specified to be this property. 
      * 
      * @param level Error level to abort
      * @throws jp.co.antenna.XfoJavaCtl.XfoException
      */
     public void setExitLevel (int level) throws XfoException {
         // Set the level...
         this.exitLevel = " -extlevel " + String.valueOf(level);
     }
     
     /**
      * Set the command-line string for external XSLT processor. For example:
      * <DL><DD>xslt -o %3 %1 %2 %param</DL>
      * %1 to %3 means following:
      * <DL><DD>%1 : XML Document 
      * <DD>%2 : XSL Stylesheet 
      * <DD>%3 : XSLT Output File 
      * <DD>%param : xsl:param</DL>
      * %1 to %3 are used to express only parameter positions. Do not replace them with actual file names.
      * <br>In case you use XSL:param for external XSLT processor, set the name and value here.
      * <br>In Windows version, default MSXML3 will be used. 
      * 
      * @param cmd Command-line string for external XSLT processor
      * @throws jp.co.antenna.XfoJavaCtl.XfoException
      */
     public void setExternalXSLT (String cmd) throws XfoException {
         // Fill this in....
     }
     
     public void setPrinterName (String prn) {
         if (prn != null && !prn.equals(""))
             this.printer = " -p " + prn;
         else
             this.printer = null;
     }
     
     public void setBatchPrint (boolean bat) {
         // Fake it. 
     }
     
     public void setStylesheetURI (String uri) {
         if (uri != null && !uri.equals(""))
             this.stylesheetDoc = " -s " + uri;
         else
             this.stylesheetDoc = null;
     }
     
     public void setOptionFileURI (String path) {
         // -i 
         if (path != null && !path.equals(""))
             this.optionFile = " -i " + path;
         else
             this.optionFile = null;
     }
     
     public void setErrorStreamType (int type) {
         // Fake it.
     }
     
     public void releaseObjectEx () throws XfoException {
         // fake it?
         this.Clear();
     }
     
     public void setMultivol (boolean multiVol) {
         if (multiVol) {
             this.multivol = " -multivol ";
         } else {
             this.multivol = null;
         }
     }
     
     public void setPdfEmbedAllFontsEx (int embedLevel) throws XfoException {
         // fill it in
     }
     
     public void setPdfImageCompression (int compressionMethod) {
         // fill it in
     }
 }
