 package br.org.indt.ndg.lwuit.control;
 
 import br.org.indt.ndg.lwuit.model.CategoryAnswer;
 import br.org.indt.ndg.lwuit.model.ChoiceAnswer;
 import br.org.indt.ndg.lwuit.model.DateAnswer;
 import br.org.indt.ndg.lwuit.model.DescriptiveAnswer;
 import br.org.indt.ndg.lwuit.model.ImageAnswer;
 import br.org.indt.ndg.lwuit.model.NDGAnswer;
 import br.org.indt.ndg.lwuit.model.NumericAnswer;
 import br.org.indt.ndg.lwuit.model.TimeAnswer;
 import br.org.indt.ndg.lwuit.ui.GeneralAlert;
 import br.org.indt.ndg.lwuit.ui.ResultList;
 import br.org.indt.ndg.lwuit.ui.WaitingScreen;
 import br.org.indt.ndg.mobile.AppMIDlet;
 import br.org.indt.ndg.mobile.FileSystem;
 import br.org.indt.ndg.mobile.Resources;
 import br.org.indt.ndg.mobile.SortsKeys;
 import br.org.indt.ndg.mobile.logging.Logger;
 import br.org.indt.ndg.mobile.structures.ResultStructure;
 import com.nokia.xfolite.xforms.dom.XFormsDocument;
 import com.nokia.xfolite.xforms.submission.XFormsXMLSerializer;
 import com.nokia.xfolite.xml.dom.Document;
 import com.nokia.xfolite.xml.dom.Element;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.PrintStream;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.Random;
 import java.util.Vector;
 import javax.microedition.io.ConnectionNotFoundException;
 import javax.microedition.io.Connector;
 import javax.microedition.io.file.FileConnection;
 import javax.microedition.location.Location;
 
 /**
  *
  * @author kgomes
  */
 public class PersistenceManager {
     private static final int VERSION = 2;//1-without conditional categories;2-with conditional categories
 
     private static PersistenceManager instance = null;
     private SaveResultsObserver m_saveObserver = null;
     private boolean error = false;
     private String resultId;
     private Vector vQuestions;
     private ResultStructure mAnswers = null;
     private XFormsDocument xFormDoc = null;
 
     private static String ORX_NAMESPACE = "http://openrosa.org/xforms/metadata";
     private static String INSTANCE_NAME = "orx:instanceID";
     private static String META_NAME = "orx:meta";
     private static String TIME_START_NAME = "orx:timeStart";
     private static String TIME_END_NAME = "orx:timeEnd";
     private static String DEVICE_ID_NAME = "orx:deviceID";
 
     private PersistenceManager(){
     }
 
     public static PersistenceManager getInstance(){
         if(instance==null){
             instance = new PersistenceManager();
         }
         return instance;
     }
 
     public String generateUniqueID() {
         Random rnd = new Random();
         long uniqueID = (((System.currentTimeMillis() >>>16)<<16)+rnd.nextLong());
         return Integer.toHexString((int) uniqueID);
     }
 
     public boolean getError() {
         return error;
     }
 
     public void save(Vector _vQuestions, SaveResultsObserver saveObserver) {
         m_saveObserver = saveObserver;
         vQuestions = _vQuestions;
 
         mAnswers = SurveysControl.getInstance().getResult();
 
         if(!AddCoordinates()){
            return;
         }
 
         WaitingScreen.show(Resources.SAVING_RESULT);
         SaveResultRunnable srr = new SaveResultRunnable();
         Thread t = new Thread(srr);
         t.setPriority(Thread.MIN_PRIORITY);
         t.start();
     }
 
     public void saveXForm(XFormsDocument document, SaveResultsObserver saveObserver){
         m_saveObserver = saveObserver;
         xFormDoc = document;
 
         WaitingScreen.show(Resources.SAVING_RESULT);
         SaveXFormResultRunnable srr = new SaveXFormResultRunnable();
         Thread t = new Thread(srr);
         t.setPriority(Thread.MIN_PRIORITY);
         t.start();
     }
 
     public void saveXFormRun(){
         //Create path
         if(xFormDoc == null){
             return;
         }
 
         String surveyDir = AppMIDlet.getInstance().getFileSystem().getSurveyDirName();
         String UID = generateUniqueID();
 
         String filename;  //check whether to create new file or use existing filename
         String fname;  //filename without root/survey directory part
         Document instanceDocument = xFormDoc.getModel().getDefaultInstance().getDocument();
         boolean isLocalFile = AppMIDlet.getInstance().getFileSystem().isLocalFile();
         if (isLocalFile) {
             fname = AppMIDlet.getInstance().getFileSystem().getResultFilename();
         } else {
             fname = "r_" + "_" + AppMIDlet.getInstance().getIMEI() + "_" + UID + ".xml";
             AddMeta(instanceDocument);
         }
 
         filename = Resources.ROOT_DIR + surveyDir + fname;
 
         XFormsXMLSerializer serilizer = new XFormsXMLSerializer();
         try {
             FileConnection fCon = (FileConnection)Connector.open(filename);
             if(!fCon.exists()){
                 fCon.create();
             }
             OutputStream stream = fCon.openOutputStream();
             serilizer.serialize(stream, instanceDocument.getDocumentElement(), null);
             stream.close();
             fCon.close();
 
             AppMIDlet.getInstance().setDisplayable(ResultList.class);
         } catch (IOException ex) {
             ex.printStackTrace();
         }
     }
 
     /***
      * Adds required javaRosa meta tags.
      * https://bitbucket.org/javarosa/javarosa/wiki/OpenRosaMetaDataSchema
      * @param instanceDocument
      */
     private void AddMeta(Document instanceDocument){
 
         Element docElem = instanceDocument.getDocumentElement();
         docElem.setAttribute("xmlns:orx", ORX_NAMESPACE);
 
         Element metaElem = instanceDocument.createElement(META_NAME);
         Element instanceElem = instanceDocument.createElement(INSTANCE_NAME);
         Element timeStartElem = instanceDocument.createElement(TIME_START_NAME);
         Element timeEndElem = instanceDocument.createElement(TIME_END_NAME);
         Element deviceIdElem = instanceDocument.createElement(DEVICE_ID_NAME);
 
         instanceElem.setText(generateUniqueID());
         deviceIdElem.setText(AppMIDlet.getInstance().getIMEI());
 
         metaElem.appendChild(instanceElem);
         metaElem.appendChild(deviceIdElem);
         metaElem.appendChild(timeStartElem);
         metaElem.appendChild(timeEndElem);
 
         docElem.insertBefore(metaElem, docElem.getChild(0));
     }
 
 
     private void save2() {
         String surveyId = String.valueOf(SurveysControl.getInstance().getSurveyIdNumber());
         try {
             String surveyDir = AppMIDlet.getInstance().getFileSystem().getSurveyDirName();
             boolean isLocalFile = AppMIDlet.getInstance().getFileSystem().isLocalFile();
 
             String UID = generateUniqueID();
 
             String filename;  //check whether to create new file or use existing filename
             String fname;  //filename without root/survey directory part
            
             if (isLocalFile) {
                 fname = AppMIDlet.getInstance().getFileSystem().getResultFilename();
             } else {
                 fname = "r_" + surveyId/*survey.getIdNumber()*/ + "_" + AppMIDlet.getInstance().getIMEI() + "_" + UID + ".xml";
             }
 
             AppMIDlet.getInstance().getFileSystem().deleteDir( "b_" + fname );//delete old binaries
 
             filename = Resources.ROOT_DIR + surveyDir + fname;
 
             resultId = extractResultId(fname);
             if (!isLocalFile) {
                 resultId = UID;
             }
 
             FileConnection connection = (FileConnection) Connector.open(filename);
             if(!connection.exists()) connection.create();
 
             if (isLocalFile) {
                 connection.delete();
                 connection.create();
             }
 
             String displayName = getResultDisplayName();
 
             FileSystem fs = AppMIDlet.getInstance().getFileSystem();
             fs.storeFilename(displayName, fname);
             AppMIDlet.getInstance().setFileSystem(fs);
 
             OutputStream out = connection.openOutputStream();
             writeAnswerToStream(out, false);
 
             out.close();
             connection.close();
             save3( fname );//this file is used to send reuslt to server
         } catch( ConnectionNotFoundException e ) {
             error = true;
             Logger.getInstance().log(e.getMessage());
             GeneralAlert.getInstance().addCommand( ExitCommand.getInstance());
             GeneralAlert.getInstance().show(Resources.ERROR_TITLE, Resources.ECREATE_RESULT, GeneralAlert.ERROR );
         } catch( IOException e ) {
             error = true;
             Logger.getInstance().log(e.getMessage());
             GeneralAlert.getInstance().addCommand( ExitCommand.getInstance());
             GeneralAlert.getInstance().show(Resources.ERROR_TITLE, Resources.EWRITE_RESULT, GeneralAlert.ERROR );
         }
         catch(Exception e){
             Logger.getInstance().log(e.getMessage());
             e.printStackTrace();
         }
         finally {
             System.gc();
         }
     }
 
     private void save3( String name ) throws IOException {
         String surveyDir = AppMIDlet.getInstance().getFileSystem().getSurveyDirName();
 
         String fname = "b_"+ name;
         String dirname = Resources.ROOT_DIR + surveyDir + fname;
 
         FileConnection directory = (FileConnection) Connector.open(dirname);
         if( !directory.exists() ) {
             directory.mkdir();
         }
         directory.close();
 
         String filename = Resources.ROOT_DIR + surveyDir + fname + "/" + fname;
         FileConnection connection = (FileConnection) Connector.open(filename);
         if( connection.exists() ) {
             connection.delete();
         }
         connection.create();
 
         OutputStream out = connection.openOutputStream();
         writeAnswerToStream(out, true);
 
         out.close();
         connection.close();
     }
 
     /***
      *
      * @return boolean - if false saving will be aborted
      */
     private boolean AddCoordinates(){
         AppMIDlet.getInstance().getFileStores().createResultStructure();
         if (!AppMIDlet.getInstance().getSettings().getStructure().getGpsConfigured())
         {
             AppMIDlet.getInstance().getFileStores().getResultStructure().resetLocation();
             return true; //gps in settings is switched off
         }
 
         if(AppMIDlet.getInstance().getFileStores().getResultStructure().isLocationValid()){
             // we do not set new location if it was already in survey and survey is modified
             // it is kept by resultHandler
             return true;
         }
 
         Location loc = AppMIDlet.getInstance().getLocation();
         if (loc == null || loc.getQualifiedCoordinates() == null) {
             GeneralAlert.getInstance().addCommand(GeneralAlert.DIALOG_OK, true);
             GeneralAlert.getInstance().show(Resources.WARNING, Resources.ADD_LOCATION_FAILURE, GeneralAlert.DIALOG_OK);
             return true;
         }
 
         if(!AppMIDlet.getInstance().locationObtained()){
             GeneralAlert.getInstance().addCommand(GeneralAlert.DIALOG_YES_NO, true);
             int dialogRetVal = GeneralAlert.getInstance().show( Resources.WARNING, Resources.LOCATION_OUT_OF_DATE, GeneralAlert.DIALOG_YES_NO);
             if(GeneralAlert.RESULT_NO == dialogRetVal){
                 return false;
             }
         }
 
         double latitude = loc.getQualifiedCoordinates().getLatitude();
         double longtitude = loc.getQualifiedCoordinates().getLongitude();
         AppMIDlet.getInstance().getFileStores().getResultStructure().setLatitude(Double.toString(latitude));
         AppMIDlet.getInstance().getFileStores().getResultStructure().setLongitude(Double.toString(longtitude));
 
         return true;
     }
 
     private void writeAnswerToStream( OutputStream out, boolean appendBinaryData ) {
         PrintStream output = new PrintStream(out);
         output.println("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
         output.print("<result ");
         output.print("r_id=\"" + resultId + "\" ");
         // *****************************   PROTOCOL DEFINITION *************************
         //
         //****************************************************************************************************
         //Type of Msg |      SurveyID			  |      Number of  Msgs  |    	ResultID  	   | UserID
         //      9           9999999999                          99             a9b9c9d9       999999999999999
         //****************************************************************************************************
         //end of header
         output.print("s_id=\"" + String.valueOf(SurveysControl.getInstance().getSurveyIdNumber()) + "\" ");
         output.print("u_id=\"" + AppMIDlet.getInstance().getIMEI() + "\" ");
 
         long timeTaken = new Date().getTime();
         output.print("time=\"" + String.valueOf(timeTaken) + "\" " );//convert to seconds
         output.print( "version=\"" + VERSION + "\">" );
         output.println();
 
         if( AppMIDlet.getInstance().getFileStores().getResultStructure() != null &&
             AppMIDlet.getInstance().getFileStores().getResultStructure().isLocationValid())
         {
             String latitude = AppMIDlet.getInstance().getFileStores().getResultStructure().getLatitude();
             String longitude = AppMIDlet.getInstance().getFileStores().getResultStructure().getLongitude();
 
             output.println("<latitude>" + latitude + "</latitude>");
             output.println("<longitude>" + longitude + "</longitude>");
         }
 
 
         output.println("<title>" + AppMIDlet.getInstance().u2x(getResultDisplayName()) + "</title>");
 
         Vector/*<CategoryAnwser>*/ anwsers = mAnswers.getAllAnwsers();
         for( int i = 0; i< anwsers.size(); i++ ){
             CategoryAnswer category = (CategoryAnswer)anwsers.elementAt(i);
             output.print("<category " + "name=\"" + AppMIDlet.getInstance().u2x(category.getName() ) + "\" ");
             output.println("id=\"" + category.getId() + "\">");
             for ( int subCat = 0; subCat < category.getSubcategoriesCount(); subCat++ ) {
                 /** Subcategory **/
                output.print("<subcategory id=\"" + (subCat + 1) + "\">" );
                 Enumeration questionIndex = category.getSubCategoryAnswers( subCat ).keys();
                 Vector keys = new Vector(category.getSubCategoryAnswers( subCat ).size());
                 while( questionIndex.hasMoreElements() ) {
                     keys.addElement(questionIndex.nextElement());
                 }
                 SortsKeys sorts = new SortsKeys();
                 sorts.qsort(keys);
                 questionIndex = keys.elements();
                 while( questionIndex.hasMoreElements() ) {
                     NDGAnswer answer = (NDGAnswer) category.getSubCategoryAnswers( subCat ).get( questionIndex.nextElement() );
                     String type = answer.getType();
                     output.print("<answer " + "type=\"" + type + "\" ");
                     output.print("id=\"" + answer.getId() + "\" ");
                     output.print("visited=\"" + answer.getVisited() + "\"");
                     if (answer instanceof TimeAnswer ) {
                         output.print(" convention=\"" + ((TimeAnswer)answer).getConvetionString() + "\"");
                         output.print(">");
                         output.println();
                         answer.save(output);
                     } else if ( answer instanceof ImageAnswer ) {
                         output.print(">");
                         output.println();
                         ((ImageAnswer)answer).save( output, appendBinaryData );
                     } else {
                         output.print(">");
                         output.println();
                         answer.save(output);
                     }
                     output.println("</answer>");
                 }
                output.print("</subcategory>");
             }
             output.println("</category>");
         }
         output.println("</result>");
     }
 
     private String extractResultId(String filename){
         String result = "";
         int i = filename.lastIndexOf('_');
         int z = filename.indexOf('.');
         if((i>0) && (z>0)){
             result = filename.substring(i+1,z);
         }
         return result;
     }
 
     private String getResultDisplayName() {
         String result = "";
         //take first answer and use it to prepare displayable survey title
         boolean found = false;
         NDGAnswer answer = null;
         for( int i = 0; i< mAnswers.getAllAnwsers().size(); i++ ) {
             try {
                 CategoryAnswer categoryanswer =  mAnswers.getCategoryAnswers( String.valueOf(i+1) );
                 Hashtable table = categoryanswer.getSubCategoryAnswers(0);
                 answer = (NDGAnswer)table.get( String.valueOf( "1" ) );
                 found = true;
                 break;
             } catch (Exception ex ) {
                 //do nothing
             }
         }
         if( !found ) {
             return resultId;
         }
 
         if (( answer instanceof ChoiceAnswer) || (answer instanceof ImageAnswer)) {
             result = resultId;
         } else if ( (answer  instanceof DescriptiveAnswer) || (answer instanceof NumericAnswer ) ) {
             result = (String) answer.getValue();
             if ( result.trim().equals("") ) {
                 result = resultId;
             }
         } else if ( answer instanceof DateAnswer ) {
             Date date = new Date( ( (DateAnswer)answer).getDate() );
             result = date.toString();
         } else if ( answer instanceof TimeAnswer ) {
             Date time = new Date( ((TimeAnswer)answer).getTime() );
             result = time.toString();
         }
         return result;
     }
 
     public boolean isEditing() {
         return AppMIDlet.getInstance().getFileSystem().isLocalFile();
     }
 
     private void resultsSaved() {
         if ( m_saveObserver != null) {
             m_saveObserver.onResultsSaved();
             m_saveObserver = null;
         }
     }
 
     class SaveResultRunnable implements Runnable {
         public void run() {
             PersistenceManager.getInstance().save2();
             AppMIDlet.getInstance().getFileStores().resetQuestions();
             AppMIDlet.getInstance().getFileSystem().loadResultFiles();
             AppMIDlet.getInstance().setResultList(new br.org.indt.ndg.mobile.ResultList());
             PersistenceManager.getInstance().resultsSaved();
         }
     }
 
     class SaveXFormResultRunnable implements Runnable {
         public void run() {
             PersistenceManager.getInstance().saveXFormRun();
             AppMIDlet.getInstance().getFileSystem().loadResultFiles();
             AppMIDlet.getInstance().setResultList(new br.org.indt.ndg.mobile.ResultList());
             PersistenceManager.getInstance().resultsSaved();
         }
     }
 }
