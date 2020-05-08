 package br.org.indt.ndg.mobile.download;
 
 import br.org.indt.ndg.lwuit.control.SurveysControl;
 import br.org.indt.ndg.lwuit.model.XFormSurvey;
 import br.org.indt.ndg.lwuit.ui.CheckNewSurveyList;
 import br.org.indt.ndg.lwuit.ui.GeneralAlert;
 import br.org.indt.ndg.lwuit.ui.StatusScreenDownload;
 import br.org.indt.ndg.mobile.AppMIDlet;
 import br.org.indt.ndg.mobile.FileSystem;
 import br.org.indt.ndg.mobile.NdgConsts;
 import br.org.indt.ndg.mobile.Resources;
 import br.org.indt.ndg.mobile.SurveyList;
 import br.org.indt.ndg.mobile.logging.Logger;
 import br.org.indt.ndg.mobile.xmlhandle.Parser;
 import java.io.ByteArrayInputStream;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.Vector;
 import javax.microedition.io.Connection;
 import javax.microedition.io.ConnectionNotFoundException;
 import javax.microedition.io.Connector;
 import javax.microedition.io.HttpConnection;
 import javax.microedition.io.file.FileConnection;
 import javax.xml.parsers.ParserConfigurationException;
 import org.xml.sax.SAXException;
 
 public class DownloadNewSurveys implements Runnable{
 
     private static DownloadNewSurveys dns;
     /** buffer length to download */
     private static final int MAX_DL_SIZE = 1024;
 
     private final byte STEP1 = '1';
     private final byte STEP2 = '2';
     private final Hashtable m_surveysDirFiles = new Hashtable();
 
     private String urlAck;
     private XFormSurvey[] m_surveysToDownload = null; // used only for XForms
     private Boolean operationCanceled = Boolean.FALSE;
     private String[] acceptableTypes = {"text/xml", "application/xml"};
     private byte currentStep = '0';
 
     private HttpConnection httpConnection;
 
     private InputStream httpInputStream;
     private Thread thread = null;
 
     private String serverStatus = Resources.CONNECTING;
     private ErrorsHandler mErrorsHandler = new ErrorsHandler();
 
     private DownloadNewSurveys() {
         updateRequestUrls();
     }
 
     public static DownloadNewSurveys getInstance() {
         if (dns == null) {
             dns = new DownloadNewSurveys();
         }
         return dns;
     }
 
     public String ServerStatus()
     {
         return serverStatus;
     }
 
     private void updateRequestUrls() {
         String urlBase = AppMIDlet.getInstance().getSettings().getStructure().getReceiveSurveyURL();
         urlAck = urlBase + "?do=ack&imei=" + AppMIDlet.getInstance().getIMEI();
     }
 
     public void check() {
         setOperationAsNotCanceled();
         serverStatus = Resources.CONNECTING;
         AppMIDlet.getInstance().setDisplayable(StatusScreenDownload.class);
         currentStep = STEP1;
         try { Thread.sleep(500); } catch (InterruptedException ex) {}
         thread = new Thread(this);
         thread.start();
     }
 
     public void download() {
         setOperationAsNotCanceled();
         AppMIDlet.getInstance().setDisplayable(StatusScreenDownload.class);
         currentStep = STEP2;
         try { Thread.sleep(500); } catch (InterruptedException ex) {}
         thread = new Thread(this);
         thread.start();
     }
 
     public void run() {
         try {
             updateRequestUrls();
             if (currentStep == STEP1) {
                 showListNewSurveys();
             } else if (currentStep == STEP2) {
                 int openRosaSurveys = 0;
                 if ( m_surveysToDownload != null ) {
                     openRosaSurveys = m_surveysToDownload.length;
                 }
                 String[] surveys = SurveysControl.getInstance().getAvailableSurveysToDownload();
 
                 if( surveys != null ) {
                     if ( (surveys.length - openRosaSurveys) > 0 ) {//NDG surveys ready to be downloaded
                         SurveyDownloader downloaderNDG = new NDGSurveyDownloader( AppMIDlet.getInstance().getSettings().getStructure().getReceiveSurveyURL()
                                                                         + "?do=download&imei="
                                                                         + AppMIDlet.getInstance().getIMEI());
                         downloaderNDG.downloadSurveys();
                     }
                 }
                 if ( m_surveysToDownload!= null && m_surveysToDownload.length > 0 ) {
                     SurveyDownloader downloaderOpenRosa = new XFormsSurveyDownloader(m_surveysToDownload);
                     downloaderOpenRosa.downloadSurveys();
                 }
                 AppMIDlet.getInstance().setDisplayable( br.org.indt.ndg.lwuit.ui.SurveyList.class );
             }
         } catch( Exception e ) {
             Logger.getInstance().logException( "Exception while downloading surveys" );
             AppMIDlet.getInstance().setDisplayable( br.org.indt.ndg.lwuit.ui.SurveyList.class );
         }
     }
 
     private void sendAck() {
         boolean ackOK = true;
         try {
             Connection conn = null;
             conn = Connector.open(urlAck, Connector.READ);
             httpConnection = (HttpConnection) conn;
             if (httpConnection.getResponseCode() != HttpConnection.HTTP_OK) {
                 ackOK = false;
             }
         } catch (IOException ex) {
             ackOK = false;
         }
 
         if (!ackOK) {
             removeInvalidSurveys();
             cancelOperation();
             GeneralAlert.getInstance().addCommand( GeneralAlert.DIALOG_OK, true );
             GeneralAlert.getInstance().show(Resources.CHECK_NEW_SURVEYS, Resources.EDOWNLOAD_ACK_ERROR, GeneralAlert.ERROR );
             AppMIDlet.getInstance().setDisplayable(br.org.indt.ndg.lwuit.ui.SurveyList.class);
         }
     }
 
     private void removeInvalidSurveys() {
         Enumeration e = m_surveysDirFiles.keys();
         while (e.hasMoreElements()) {
             String dir = (String)e.nextElement();
             String xmlFile = (String) m_surveysDirFiles.get(dir);
             try {
                  FileConnection fconn = (FileConnection) Connector.open(xmlFile);
                  if (fconn.exists()) {
                      fconn.delete();
                  }
                  fconn.close();
                  fconn = (FileConnection) Connector.open(dir);
                  if (fconn.exists()) {
                      fconn.delete();
                  }
                  fconn.close();
             } catch (Exception ex){
                 //ignore
             }
         }
     }
 
     private void showListNewSurveys() throws Exception {
         String filename = AppMIDlet.getInstance().getRootDir() + NdgConsts.NEW_SURVEYS_LIST;
         FileConnection fconn = null;
         DataOutputStream out = null;
         DataInputStream dis = null;
         String[] surveysTitlesNDG = null;
         String[] surveysTitlesOpenRosa = null;
 
         try {
             fconn = (FileConnection) Connector.open(filename);
             if ( fconn.exists()) {
                 fconn.delete();
             }
             fconn.create();
             out = fconn.openDataOutputStream();
             String ndgList = AppMIDlet.getInstance().getSettings().getStructure().getReceiveSurveyURL()
                             +"?do=list&imei=" + AppMIDlet.getInstance().getIMEI();
 
             downloadResource( ndgList , acceptableTypes, out);
             out.flush();
             out.close();
             if (!isOperationCanceled()) {
                  // Parse the surveys list
                 dis = fconn.openDataInputStream();
                 dis.available();
                 NDGSurveysListHandler ndgSurveyListHandler = new NDGSurveysListHandler();
                 surveysTitlesNDG = ndgSurveyListHandler.parse(dis);
             }
         } catch (SAXException ex) {
             Logger.getInstance().log(ex.getClass().getName() + "::" + ex.getMessage());
             mErrorsHandler.handleSurveyParserError();
         } catch (ParserConfigurationException ex) {
             Logger.getInstance().log(ex.getClass().getName() + "::" + ex.getMessage());
             mErrorsHandler.handleSurveyParserConfigurationError( ex.getMessage() );
         } catch (ConnectionNotFoundException ex) {
             Logger.getInstance().log(ex.getClass().getName() + "::" + ex.getMessage());
             mErrorsHandler.handleSurveyDownloadConnectionError(ex.getMessage().trim());
         } catch(IOException ex) {
             Logger.getInstance().log(ex.getClass().getName() + "::" + ex.getMessage());
             mErrorsHandler.handleSurveyInputOutpuError( ex.getMessage() );
         }  catch (SecurityException ex) {
             Logger.getInstance().log(ex.getClass().getName() + "::" + ex.getMessage());
             mErrorsHandler.handleSurveyDownloadSecurityError();
         } finally {
             try {
                 boolean canceled = false;
                 synchronized (operationCanceled) {
                     if (operationCanceled == Boolean.FALSE)
                         canceled = true;
                 }
                 if (canceled) {
                     fconn.delete();
                 }
                 if (out != null) {
                     out.close();
                 }
                 if (dis != null) {
                     dis.close();
                 }
                 if (fconn != null) {
                     fconn.close();
                 }
             } catch (IOException e) {
                 // ignore
             }
         }
         try {
             fconn = (FileConnection) Connector.open(filename);
             if(!fconn.exists()) fconn.create();
             else {
                 fconn.delete();
                 fconn.create();
             }
             out = fconn.openDataOutputStream();
             String openRosaList = AppMIDlet.getInstance().getSettings().getStructure().getReceiveSurveyURL()
                                 + "?deviceID=" + AppMIDlet.getInstance().getIMEI();
 
             downloadResource( openRosaList , acceptableTypes, out);
             out.flush();
             out.close();
             if (!isOperationCanceled()) {
                  // Parse the surveys list
                 dis = fconn.openDataInputStream();
 
                 XFormsSurveysListHandler xFormsSurveyListHandler = new XFormsSurveysListHandler();
                 surveysTitlesOpenRosa = xFormsSurveyListHandler.parse(dis);
                 m_surveysToDownload = xFormsSurveyListHandler.getSurveysToDownload();
             }
         } catch (SAXException ex) {
             Logger.getInstance().log(ex.getClass().getName() + "::" + ex.getMessage());
             mErrorsHandler.handleSurveyParserError();
         } catch (ParserConfigurationException ex) {
             Logger.getInstance().log(ex.getClass().getName() + "::" + ex.getMessage());
             mErrorsHandler.handleSurveyParserConfigurationError( ex.getMessage() );
         } catch (ConnectionNotFoundException ex) {
             Logger.getInstance().log(ex.getClass().getName() + "::" + ex.getMessage());
             mErrorsHandler.handleSurveyDownloadConnectionError(ex.getMessage().trim());
         } catch(IOException ex) {
             Logger.getInstance().log(ex.getClass().getName() + "::" + ex.getMessage());
             mErrorsHandler.handleSurveyInputOutpuError( ex.getMessage() );
         }  catch (SecurityException ex) {
             Logger.getInstance().log(ex.getClass().getName() + "::" + ex.getMessage());
             mErrorsHandler.handleSurveyDownloadSecurityError();
         } finally {
             try {
                 boolean canceled = false;
                 synchronized (operationCanceled) {
                     if (operationCanceled == Boolean.FALSE)
                         canceled = true;
                 }
                 if (canceled) {
                     fconn.delete();
                 }
                 if (out != null) {
                     out.close();
                 }
                 if (dis != null) {
                     dis.close();
                 }
                 if (fconn != null) {
                     fconn.close();
                 }
             } catch (IOException e) {
                 // ignore
             }
         }
         Vector titles = new Vector();
         if( surveysTitlesNDG != null ) {
             for ( int i=0; i< surveysTitlesNDG.length; i++ ) {
                 titles.addElement(surveysTitlesNDG[i]);
             }
         }
         if( surveysTitlesOpenRosa != null ) {
             for ( int j=0; j< surveysTitlesOpenRosa.length; j++ ) {
                 titles.addElement(surveysTitlesOpenRosa[j]);
             }
         }
 
         String[] surveysTitles = new String[titles.size()];
         titles.copyInto(surveysTitles);
 
         SurveysControl.getInstance().setAvaiableSurveyToDownload(surveysTitles);
         if( surveysTitles.length > 0 ) {
             AppMIDlet.getInstance().setDisplayable(CheckNewSurveyList.class);
         } else {
             GeneralAlert.getInstance().addCommand(GeneralAlert.DIALOG_OK, true);
             GeneralAlert.getInstance().show(Resources.DOWNLOAD_SURVEYS, Resources.THERE_ARE_NO_NEW_SURVEYS, GeneralAlert.INFO);
             AppMIDlet.getInstance().setDisplayable(br.org.indt.ndg.lwuit.ui.SurveyList.class);
         }
     }
 
     public void cancelOperation() {
         synchronized (operationCanceled) {
             operationCanceled = Boolean.TRUE;
         }
     }
 
    public void cancelAndKillOperation() {
         synchronized (operationCanceled) {
             operationCanceled = Boolean.TRUE;
             if(thread != null && thread.isAlive()) {
                 thread.interrupt();
             }
              AppMIDlet.getInstance().setDisplayable(br.org.indt.ndg.lwuit.ui.SurveyList.class);
         }
     }
 
    /**
      * Download data from URL param and write in Output Stream param.
      *
      * @param urlParam URL to download
      * @param acceptableTypes mime types acceptable list to download.
      * @param output output stream to write downloaded data
      *
      */
     public void downloadResource(String urlParam, String[] acceptableTypes,
             OutputStream output) {
         Connection conn = null;
         StringBuffer acceptField;
         int responseCode = 0;
         String retryAfterField;
         int retryInterval;
         String mediaType;
         try {
             if (isOperationCanceled()) {
                 AppMIDlet.getInstance().setDisplayable(br.org.indt.ndg.lwuit.ui.SurveyList.class);
                 return;
             }
 
             for (;;) {
                 if (isOperationCanceled())
                     break;
                 conn = Connector.open(urlParam, Connector.READ);
                 httpConnection = (HttpConnection) conn;
 
                 // 256 is given to avoid resizing without adding lengths
                 acceptField = new StringBuffer(256);
 
                 // there must be one or more acceptable media types
                 acceptField.append(acceptableTypes[0]);
                 for (int i = 1; i < acceptableTypes.length; i++) {
                     acceptField.append(", ");
                     acceptField.append(acceptableTypes[i]);
                 }
                 httpConnection.setRequestProperty("Accept",
                                                   acceptField.toString());
                 httpConnection.setRequestMethod(HttpConnection.GET);
 
                 try {
                     responseCode = httpConnection.getResponseCode();
                 } catch (IOException ioe) {
                     Logger.getInstance().log("ioe: " + ioe.getMessage());
                     // -3 user clicks cancel in winsock open
                     if (ioe.getMessage().trim().equals("-3")) {
                         // user canceled
                         cancelOperation();
                     } else {
                         cancelOperation();
                         GeneralAlert.getInstance().addCommand(GeneralAlert.DIALOG_OK, true);
                         GeneralAlert.getInstance().showCodedAlert(Resources.NETWORK_FAILURE, ioe.getMessage().trim(), GeneralAlert.ERROR);
                         AppMIDlet.getInstance().setDisplayable(br.org.indt.ndg.lwuit.ui.SurveyList.class);
                     }
                     return;
                 } catch (SecurityException e) {
                     cancelOperation();
                     GeneralAlert.getInstance().addCommand(GeneralAlert.DIALOG_OK, true);
                     GeneralAlert.getInstance().showCodedAlert(Resources.NETWORK_FAILURE, Resources.HTTP_UNAUTHORIZED, GeneralAlert.ERROR);
                     AppMIDlet.getInstance().setDisplayable(br.org.indt.ndg.lwuit.ui.SurveyList.class);
                 }
 
                 // if the server is currently unable to handle the request due
                 // to a temporary overloading or maintenance of the server then
                 // retry after a interval.
                 if (responseCode != HttpConnection.HTTP_UNAVAILABLE) {
                     break;
                 }
                 retryAfterField = httpConnection.getHeaderField("Retry-After");
                 if (retryAfterField == null) {
                     break;
                 }
 
                 try {
                     /*
                      * see if the retry interval is in seconds, and
                      * not an absolute date
                      */
                     retryInterval = Integer.parseInt(retryAfterField);
                     if (retryInterval > 0) {
                         if (retryInterval > 60) {
                             // only wait 1 min
                             retryInterval = 60;
                         }
 
                         Thread.sleep(retryInterval * 1000);
                     }
                 } catch (InterruptedException ie) {
                     // ignore thread interrupt
                     break;
                 } catch (NumberFormatException ne) {
                     // ignore bad format
                     break;
                 }
 
                 httpConnection.close();
             } // end for
 
             if (isOperationCanceled()) {
                 AppMIDlet.getInstance().setDisplayable(br.org.indt.ndg.lwuit.ui.SurveyList.class);
                 return;
             }
 
             if (responseCode != HttpConnection.HTTP_OK) {
                 cancelOperation();
                 GeneralAlert.getInstance().addCommand( GeneralAlert.DIALOG_OK, true );
                 GeneralAlert.getInstance().showCodedAlert(Resources.CHECK_NEW_SURVEYS, String.valueOf(responseCode), GeneralAlert.ERROR );
                 AppMIDlet.getInstance().setDisplayable(br.org.indt.ndg.lwuit.ui.SurveyList.class);
                 return;
             }
             mediaType = getMediaType(httpConnection.getType());
 
             if (mediaType != null) {
                 boolean goodType = false;
 
                 for (int i = 0; i < acceptableTypes.length; i++) {
                     if (mediaType.equals(acceptableTypes[i])) {
                         goodType = true;
                         break;
                     }
                 }
 
                 if (!goodType) {
                     cancelOperation();
                     GeneralAlert.getInstance().addCommand( GeneralAlert.DIALOG_OK, true );
                     GeneralAlert.getInstance().show(Resources.CHECK_NEW_SURVEYS, Resources.EDOWNLOAD_FAILED_INVALID_MIME_TYPE + mediaType, GeneralAlert.ERROR );
                     AppMIDlet.getInstance().setDisplayable(br.org.indt.ndg.lwuit.ui.SurveyList.class);
                     return;
                 }
             }
             httpInputStream = httpConnection.openInputStream();
 
             transferData(httpInputStream, output);
 
             if (isOperationCanceled()) {
                 // Close the streams or connections this method opened.
                 //try { httpInputStream.close(); } catch (Exception e) {}
                 //try { conn.close(); } catch (Exception e) {}
                 AppMIDlet.getInstance().setDisplayable(br.org.indt.ndg.lwuit.ui.SurveyList.class);
             }
 
         } catch (IOException ioe) {
                 cancelOperation();
                 GeneralAlert.getInstance().addCommand( GeneralAlert.DIALOG_OK, true );
                 GeneralAlert.getInstance().showCodedAlert(Resources.NETWORK_FAILURE, ioe.getMessage().trim(), GeneralAlert.ERROR );
                 AppMIDlet.getInstance().setDisplayable(br.org.indt.ndg.lwuit.ui.SurveyList.class);
         	return;
         } catch (SecurityException e) {
                 cancelOperation();
                 GeneralAlert.getInstance().addCommand(GeneralAlert.DIALOG_OK, true);
                 GeneralAlert.getInstance().showCodedAlert(Resources.NETWORK_FAILURE, Resources.HTTP_UNAUTHORIZED, GeneralAlert.ERROR);
                 AppMIDlet.getInstance().setDisplayable(br.org.indt.ndg.lwuit.ui.SurveyList.class);
         } finally {
             // Close the streams or connections this method opened.
             try {
                 httpInputStream.close();
             } catch (Exception e) {
                 // ignore
             }
 
             try {
                 conn.close();
             } catch (Exception e) {
                 // ignore
             }
         }
     }
 
     private boolean isOperationCanceled() {
         boolean result = false;
         synchronized (operationCanceled) {
             if (operationCanceled == Boolean.TRUE) {
                 result = true;
             }
         }
         return result;
     }
 
     private void setOperationAsNotCanceled() {
         synchronized(operationCanceled) {
             operationCanceled = Boolean.FALSE;
         }
     }
 
    /**
      * Make the parse of mime-types from content-type field
      * The media-type everything for the ';' that marks the parameters.
      *
      * @param contentType http content-type field value
      *
      * @return mime type
      */
     private static String getMediaType(String contentType) {
         int semiColon;
 
         if (contentType == null) {
             return null;
         }
 
         semiColon = contentType.indexOf(';');
         if (semiColon < 0) {
             return contentType.toLowerCase();
         }
 
         return contentType.substring(0, semiColon).toLowerCase();
     }
 
     /**
      * Make the download of data.
      *     *
      * @param in a input stream from data.
      * @param out o output stream to write data.
      *
      */
     private void transferData(InputStream in, OutputStream out) {
         byte[] buffer = new byte[MAX_DL_SIZE];
         int bytesRead;
         int totalBytesWritten = 0;
 
         if ((int) httpConnection.getLength() <= 0) {
 
             cancelOperation();
             GeneralAlert.getInstance().addCommand( GeneralAlert.DIALOG_OK, true );
             GeneralAlert.getInstance().show(Resources.CHECK_NEW_SURVEYS, Resources.EDOWNLOAD_FAILED_INVALID_DATA, GeneralAlert.ERROR );
             AppMIDlet.getInstance().setDisplayable(br.org.indt.ndg.lwuit.ui.SurveyList.class);
             return;
         }
         int totalKbytes = (int) httpConnection.getLength() / MAX_DL_SIZE;
 
         try {
             StringBuffer sb = new StringBuffer();
             for (;;) {
                 if (isOperationCanceled()) {
                     break;
                 }
 
                 bytesRead = in.read(buffer);
                 if (bytesRead == -1) {
                     if (totalKbytes != (totalBytesWritten / MAX_DL_SIZE)) {
                         cancelOperation();
                         GeneralAlert.getInstance().addCommand( GeneralAlert.DIALOG_OK, true );
                         GeneralAlert.getInstance().show(Resources.CHECK_NEW_SURVEYS, Resources.EDOWNLOAD_INCOMPLETED, GeneralAlert.ERROR );
                         AppMIDlet.getInstance().setDisplayable(br.org.indt.ndg.lwuit.ui.SurveyList.class);
                     }
                     break;
                 }
 
                String sBuffer = new String(buffer, 0, bytesRead);
                 sb.append(sBuffer);
                 totalBytesWritten += bytesRead;
             }
             byte[] outbyte = sb.toString().getBytes("UTF-8");
             out.write(outbyte, 0, outbyte.length);
         } catch (IOException ioe) {
             cancelOperation();
             GeneralAlert.getInstance().addCommand( GeneralAlert.DIALOG_OK, true );
             GeneralAlert.getInstance().showCodedAlert(Resources.NETWORK_FAILURE, ioe.getMessage().trim(), GeneralAlert.ERROR );
             AppMIDlet.getInstance().setDisplayable(br.org.indt.ndg.lwuit.ui.SurveyList.class);
             return;
         }
          catch (SecurityException e) {
             cancelOperation();
             GeneralAlert.getInstance().addCommand(GeneralAlert.DIALOG_OK, true);
             GeneralAlert.getInstance().showCodedAlert(Resources.NETWORK_FAILURE, Resources.HTTP_UNAUTHORIZED, GeneralAlert.ERROR);
             AppMIDlet.getInstance().setDisplayable(br.org.indt.ndg.lwuit.ui.SurveyList.class);
         }
         return;
     }
 
 
     private abstract class SurveyDownloader {
 
         public abstract void downloadSurveys();
         protected abstract void parseAndSaveSurveys() throws IOException, SecurityException;
         protected abstract String getCurrentSurveyId();
         protected abstract String getCurrentSurveyTitle();
 
         protected final SurveyHandler m_surveyHandler;
         protected final String m_surveyEndTag;
         protected final String m_surveyDirPrefix;
         protected final StringBuffer m_unprocessedBuffer = new StringBuffer();
         protected boolean m_allBytesRead = false;
         protected String m_notDownloadedSurveys = "";
 
         protected SurveyDownloader( SurveyHandler surveyHandler, String surveyDirPrefix, String surveyEndTag ) {
             m_surveyHandler = surveyHandler;
             m_surveyDirPrefix = surveyDirPrefix;
             m_surveyEndTag = surveyEndTag;
         }
 
         protected void downloadSurvey( String downloadUrl ) {
             Connection conn = null;
             StringBuffer acceptField;
             int responseCode = 0;
             String retryAfterField;
             int retryInterval;
             String mediaType;
 
             try {
                 serverStatus = Resources.CONNECTING;
                 AppMIDlet.getInstance().setDisplayable(StatusScreenDownload.class);
                 if (isOperationCanceled()) {
                     return;
                 }
                 for (;;) {
                     String urlValidated = fixParseError( downloadUrl );//this is a work around for parsing error
                                                                        //MUST be investigated in the near future
                                                                        //"?"(question marks) while parsing XML in XFormsSurveysListHandelr are removed form original string
                     conn = Connector.open( urlValidated , Connector.READ);
 
                     if (isOperationCanceled()) {
                         break;
                     }
 
                     httpConnection = (HttpConnection) conn;
 
                     // 256 is given to avoid resizing without adding lengths
                     acceptField = new StringBuffer(256);
 
                     // there must be one or more acceptable media types
                     acceptField.append(acceptableTypes[0]);
                     for (int i = 1; i < acceptableTypes.length; i++) {
                         acceptField.append(", ");
                         acceptField.append(acceptableTypes[i]);
                     }
 
                     httpConnection.setRequestProperty("Accept",
                                                       acceptField.toString());
 
                     httpConnection.setRequestMethod(HttpConnection.GET);
 
                     try {
                         responseCode = httpConnection.getResponseCode();
                     } catch (IOException ioe) {
                         // -3 user clicks cancel in winsock open
                         if (ioe.getMessage().trim().equals("-3")) {
                             // user canceled
                             cancelOperation();
                         } else {
                             cancelOperation();
                             GeneralAlert.getInstance().addCommand(GeneralAlert.DIALOG_OK, true);
                             GeneralAlert.getInstance().showCodedAlert(Resources.NETWORK_FAILURE, ioe.getMessage(), GeneralAlert.ERROR);
                             AppMIDlet.getInstance().setDisplayable(br.org.indt.ndg.lwuit.ui.SurveyList.class);
                         }
                         return;
                     } catch (SecurityException e) {
                         cancelOperation();
                         GeneralAlert.getInstance().addCommand(GeneralAlert.DIALOG_OK, true);
                         GeneralAlert.getInstance().showCodedAlert(Resources.NETWORK_FAILURE, Resources.HTTP_UNAUTHORIZED, GeneralAlert.ERROR);
                         AppMIDlet.getInstance().setDisplayable(br.org.indt.ndg.lwuit.ui.SurveyList.class);
                     }
 
                     responseCode = httpConnection.getResponseCode();
                     if (responseCode != HttpConnection.HTTP_UNAVAILABLE) {
                         break;
                     }
 
                     retryAfterField = httpConnection.getHeaderField("Retry-After");
                     if (retryAfterField == null) {
                         break;
                     }
 
                     try {
                         /*
                          * see if the retry interval is in seconds, and
                          * not an absolute date
                          */
                         retryInterval = Integer.parseInt(retryAfterField);
                         if (retryInterval > 0) {
                             if (retryInterval > 60) {
                                 // only wait 1 min
                                 retryInterval = 60;
                             }
 
                             Thread.sleep(retryInterval * 1000);
                         }
                     } catch (InterruptedException ie) {
                         // ignore thread interrupt
                         break;
                     } catch (NumberFormatException ne) {
                         // ignore bad format
                         break;
                     }
 
                     httpConnection.close();
                 } // end for
 
                     if (isOperationCanceled()) {
                     AppMIDlet.getInstance().setDisplayable(br.org.indt.ndg.lwuit.ui.SurveyList.class);
                     return;
                 }
 
                 if (responseCode != HttpConnection.HTTP_OK) {
                     cancelOperation();
 
                     GeneralAlert.getInstance().addCommand( GeneralAlert.DIALOG_OK, true );
                     GeneralAlert.getInstance().showCodedAlert(Resources.CHECK_NEW_SURVEYS, String.valueOf(responseCode), GeneralAlert.ERROR );
                     AppMIDlet.getInstance().setDisplayable(br.org.indt.ndg.lwuit.ui.SurveyList.class);
                     return;
                 }
 
                 String mediaTypeStr = httpConnection.getType();
                 mediaType = getMediaType(mediaTypeStr);
 
                 if (mediaType != null) {
                     boolean goodType = false;
 
                     for (int i = 0; i < acceptableTypes.length; i++) {
                         if (mediaType.equals(acceptableTypes[i])) {
                             goodType = true;
                             break;
                         }
                     }
 
                     if (!goodType) {
                         cancelOperation();
                         GeneralAlert.getInstance().addCommand( GeneralAlert.DIALOG_OK, true );
                         GeneralAlert.getInstance().show(Resources.CHECK_NEW_SURVEYS, Resources.EDOWNLOAD_FAILED_INVALID_MIME_TYPE + mediaType, GeneralAlert.ERROR );
                         AppMIDlet.getInstance().setDisplayable(br.org.indt.ndg.lwuit.ui.SurveyList.class);
                         return;
                     }
                 }
                 httpInputStream = httpConnection.openInputStream();
                 receiveDataAndSaveSurveys(httpInputStream);
 
             } catch (IOException ioe) {
                 cancelOperation();
                 GeneralAlert.getInstance().addCommand(GeneralAlert.DIALOG_OK, true);
                 GeneralAlert.getInstance().showCodedAlert(Resources.NETWORK_FAILURE, ioe.getMessage().trim(), GeneralAlert.ERROR);
                 AppMIDlet.getInstance().setDisplayable(br.org.indt.ndg.lwuit.ui.SurveyList.class);
                 return;
             } catch (SecurityException e) {
                 cancelOperation();
                 GeneralAlert.getInstance().addCommand(GeneralAlert.DIALOG_OK, true);
                 GeneralAlert.getInstance().showCodedAlert(Resources.NETWORK_FAILURE, Resources.HTTP_UNAUTHORIZED, GeneralAlert.ERROR);
                 AppMIDlet.getInstance().setDisplayable(br.org.indt.ndg.lwuit.ui.SurveyList.class);
             } finally {
                 // Close the streams or connections this method opened.
                 try {
                     httpInputStream.close();
                 } catch (Exception e) {
                     // ignore
                 }
 
                 try {
                     conn.close();
                 } catch (Exception e) {
                     // ignore
                 }
             }
         }
 
         protected void cleanupBeforeSurveyDownload() {
             m_surveysDirFiles.clear();
             m_notDownloadedSurveys = "";
         }
 
         private void receiveDataAndSaveSurveys( InputStream in ) {
             if ((int) httpConnection.getLength() <= 0) {
                 cancelOperation();
                 GeneralAlert.getInstance().addCommand( GeneralAlert.DIALOG_OK, true );
                 GeneralAlert.getInstance().show(Resources.CHECK_NEW_SURVEYS, Resources.EDOWNLOAD_FAILED_INVALID_DATA, GeneralAlert.ERROR );
                 AppMIDlet.getInstance().setDisplayable(br.org.indt.ndg.lwuit.ui.SurveyList.class);
                 return;
             }
             serverStatus = Resources.DOWNLOADING_NEW_SURVEYS;
             AppMIDlet.getInstance().setDisplayable(StatusScreenDownload.class);
 
             byte[] buffer = new byte[MAX_DL_SIZE];
             int bytesRead = 0;
             int totalBytesRead = 0;
             int totalBytes = (int) httpConnection.getLength();
             m_unprocessedBuffer.delete(0, m_unprocessedBuffer.length());
             try {
                 for (;;) {
                     if (isOperationCanceled()) {
                         return;
                     }
                     bytesRead = in.read(buffer);
 
                     if (bytesRead == -1) {
                         if (totalBytes != totalBytesRead) {
                             cancelOperation();
                             GeneralAlert.getInstance().addCommand( GeneralAlert.DIALOG_OK, true );
                             GeneralAlert.getInstance().show(Resources.CHECK_NEW_SURVEYS, Resources.EDOWNLOAD_INCOMPLETED, GeneralAlert.ERROR );
                             AppMIDlet.getInstance().setDisplayable(br.org.indt.ndg.lwuit.ui.SurveyList.class);
                             break;
                         }
                         break;
                     }
                     totalBytesRead += bytesRead;
                     m_allBytesRead = (totalBytes <= (totalBytesRead ) ? true : false);
                     String sBuffer = new String(buffer, 0, bytesRead);
                     m_unprocessedBuffer.append(sBuffer);
                 }
                 parseAndSaveSurveys();
             } catch (IOException ioe) {
                 cancelOperation();
                 GeneralAlert.getInstance().addCommand( GeneralAlert.DIALOG_OK, true );
                 GeneralAlert.getInstance().showCodedAlert(Resources.NETWORK_FAILURE, ioe.getMessage().trim(), GeneralAlert.ERROR );
                 AppMIDlet.getInstance().setDisplayable(br.org.indt.ndg.lwuit.ui.SurveyList.class);
             } catch (SecurityException e) {
                 cancelOperation();
                 GeneralAlert.getInstance().addCommand(GeneralAlert.DIALOG_OK, true);
                 GeneralAlert.getInstance().showCodedAlert(Resources.NETWORK_FAILURE, Resources.HTTP_UNAUTHORIZED, GeneralAlert.ERROR);
                 AppMIDlet.getInstance().setDisplayable(br.org.indt.ndg.lwuit.ui.SurveyList.class);
             }
         }
 
         protected void saveSurvey(byte[] surveyBytes) throws IOException {
             Parser parser = new Parser(m_surveyHandler);
             ByteArrayInputStream bais = new ByteArrayInputStream(surveyBytes);
             parser.parseInputStream(bais);
             String surveyID = getCurrentSurveyId();
             FileConnection fconnDir = (FileConnection) Connector.open(AppMIDlet.getInstance().getRootDir() + m_surveyDirPrefix + surveyID + "/");
             if (!fconnDir.exists()) {
                 fconnDir.mkdir();
                 fconnDir.close();
                 FileConnection fconnSurvey = (FileConnection) Connector.open(AppMIDlet.getInstance().getRootDir() + m_surveyDirPrefix + surveyID + "/" + NdgConsts.SURVEY_NAME);
                 if (!fconnSurvey.exists()) fconnSurvey.create();
                 else {
                     fconnSurvey.delete();
                     fconnSurvey.create();
                 }
                 DataOutputStream dos = fconnSurvey.openDataOutputStream();
                 dos.write(surveyBytes, 0, surveyBytes.length);
                 dos.flush();
                 dos.close();
                 fconnSurvey.close();
                 m_surveysDirFiles.put(AppMIDlet.getInstance().getRootDir() + m_surveyDirPrefix + surveyID + "/", AppMIDlet.getInstance().getRootDir() + m_surveyDirPrefix + surveyID + "/" + NdgConsts.SURVEY_NAME);
             } else {
                 //The following surveys were not downloaded since they already exist in mobile.
                 m_notDownloadedSurveys += "\n"  + getCurrentSurveyTitle();
             }
         }
 
         protected void finalizeSurveyDownload() {
             if (!isOperationCanceled()) {
                 sendAck();
                 if (!isOperationCanceled()) {
                     AppMIDlet.getInstance().setFileSystem(new FileSystem(AppMIDlet.getInstance().getRootDir()));
                     AppMIDlet.getInstance().setSurveyList(new SurveyList());
                     // Send Alert about now downloaded surveys
                     if (!m_notDownloadedSurveys.equals("")) {
                         GeneralAlert.getInstance().addCommand(GeneralAlert.DIALOG_OK, true);
                         GeneralAlert.getInstance().show(Resources.CHECK_NEW_SURVEYS, Resources.SURVEY_NOT_DOWNLOADED + m_notDownloadedSurveys, GeneralAlert.ALARM);
                     }
                 }
             } else {
                 cancelOperation();
                 removeInvalidSurveys();
             }
         }
 
         private String fixParseError(String downloadUrl) {
             if( downloadUrl.indexOf("ReceiveSurveys?do") >=0 ) {
                 return downloadUrl;//url seems to be OK
             } else {
                 int start = downloadUrl.indexOf("ReceiveSurveys");
                 String url = downloadUrl.substring(0, start + "ReceiveSurveys".length() )
                                             + "?"//missing question mark from parser
                                             + downloadUrl.substring(start + "ReceiveSurveys".length() );
                 return url;
             }
         }
     }
 
     private class NDGSurveyDownloader extends SurveyDownloader {
         private static final String mSurveyStartTag = "<survey";
         private String mUrlDownload;
 
 
         public NDGSurveyDownloader( String aUrlDownload ) {
             super( new NDGSurveyHandler(), NdgConsts.NDG_SURVEY_DIR_PREFIX, NdgConsts.XML_TAG_END_SURVEY);
             mUrlDownload = aUrlDownload;
         }
 
         public void downloadSurveys() {
             cleanupBeforeSurveyDownload();
             downloadSurvey(mUrlDownload);
             finalizeSurveyDownload();
         }
 
         protected String getCurrentSurveyId() {
             return ((NDGSurveyHandler)m_surveyHandler).getSurveyID();
         }
 
         protected String getCurrentSurveyTitle() {
             return ((NDGSurveyHandler)m_surveyHandler).getSurveyTitle();
         }
 
         protected void parseAndSaveSurveys() throws IOException, SecurityException {
             String ssb = m_unprocessedBuffer.toString();
             int startIndex = ssb.indexOf( mSurveyStartTag );
             int endIndex = ssb.indexOf( m_surveyEndTag );
 
             if ( startIndex < 0 || endIndex < 0 || endIndex < startIndex ) {
                 GeneralAlert.getInstance().addCommand( GeneralAlert.DIALOG_OK, true );
                 GeneralAlert.getInstance().show( Resources.SURVEY_LOCALIZED, Resources.CORRUPTED_SURVEY, GeneralAlert.ERROR );
                 return;
             }
 
             do {
                 if  ( endIndex > 0 ) {
                     String surveyCompleted = ssb.substring(0, endIndex + m_surveyEndTag.length());
                     String rest = ssb.substring(endIndex + m_surveyEndTag.length(), ssb.length());
                     m_unprocessedBuffer.delete(0, m_unprocessedBuffer.length());
                     m_unprocessedBuffer.append(rest);
                     byte[] out = surveyCompleted.getBytes();
                     saveSurvey(out);
                     ssb = m_unprocessedBuffer.toString();
                 }
                 startIndex = ssb.indexOf( mSurveyStartTag );
                 endIndex = ssb.indexOf( m_surveyEndTag );
                 if( startIndex < 0 ) {
                     break;//end of survey(s)
                 } else if ( startIndex >= 0 && endIndex < 0 ) {
                     GeneralAlert.getInstance().addCommand( GeneralAlert.DIALOG_OK, true );
                     GeneralAlert.getInstance().show( Resources.SURVEY_LOCALIZED, Resources.ONE_SURVEY_CORRUPTED, GeneralAlert.ERROR );
                     break;
                 }
             } while (true);
         }
     }
 
     private class XFormsSurveyDownloader extends SurveyDownloader {
 
         private XFormSurvey[] m_surveys = null;
         private int m_currentIndex = 0;
 
         public XFormsSurveyDownloader( XFormSurvey[] surveys ) {
             super(new XFormsSurveyHandler(), NdgConsts.XFORMS_SURVEY_DIR_PREFIX, NdgConsts.XFORMS_TAG_END_SURVEY);
             if (surveys == null)
                 throw new NullPointerException(); // can't pass null
             m_surveys = surveys;
         }
 
         public void downloadSurveys() {
             cleanupBeforeSurveyDownload();
             for( m_currentIndex = 0; m_currentIndex< m_surveys.length; m_currentIndex++ ) {
                 downloadSurvey(m_surveys[m_currentIndex].getDownloadUrl());
             }
             finalizeSurveyDownload();
         }
 
         /**
          * This is more like a workaround method. Documented, direct realationship
          * between formId/name from survey list discovery and surveys body itself was
          * not found (though it most probably exist)
          * @return  current surveys id/name based on form discovery list formID/name
          */
         protected String getCurrentSurveyId() {
             return m_surveys[m_currentIndex].getFormId();
         }
         protected String getCurrentSurveyTitle() {
             return m_surveys[m_currentIndex].getDisplayableName();
         }
 
         protected void parseAndSaveSurveys() throws IOException, SecurityException {
             if (m_allBytesRead) {
                 byte[] out = m_unprocessedBuffer.toString().getBytes();
                 saveSurvey(out);
             }
         }
     }
 
     public class ErrorsHandler {
 
         public void handleSurveyParserError() {
             GeneralAlert.getInstance().addCommand( GeneralAlert.DIALOG_OK, true );
             GeneralAlert.getInstance().show(Resources.CHECK_NEW_SURVEYS, Resources.EPARSE_SAX , GeneralAlert.ERROR );
         }
 
         private void handleSurveyParserConfigurationError( String message ) {
             GeneralAlert.getInstance().addCommand( GeneralAlert.DIALOG_OK, true );
             GeneralAlert.getInstance().show(Resources.CHECK_NEW_SURVEYS, Resources.EPARSE_GENERAL + message , GeneralAlert.ERROR );
         }
 
         private void handleSurveyDownloadConnectionError(String message) {
             GeneralAlert.getInstance().addCommand( GeneralAlert.DIALOG_OK, true );
             GeneralAlert.getInstance().showCodedAlert(Resources.NETWORK_FAILURE, message, GeneralAlert.ERROR );
             AppMIDlet.getInstance().setDisplayable( br.org.indt.ndg.lwuit.ui.SurveyList.class );
         }
 
         private void handleSurveyInputOutpuError(String message) {
             GeneralAlert.getInstance().addCommand( GeneralAlert.DIALOG_OK, true );
             GeneralAlert.getInstance().show(Resources.CHECK_NEW_SURVEYS, Resources.ERROR_TITLE + message , GeneralAlert.ERROR );
         }
 
         private void handleSurveyDownloadSecurityError() {
             cancelOperation();
             GeneralAlert.getInstance().addCommand(GeneralAlert.DIALOG_OK, true);
             GeneralAlert.getInstance().showCodedAlert(Resources.NETWORK_FAILURE, Resources.HTTP_UNAUTHORIZED, GeneralAlert.ERROR);
         }
     }
 }
