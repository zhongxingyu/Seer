 /*
  * //
  * // Copyright (C) 2009 Boutros-Labs(German cancer research center) b110-it@dkfz.de
  * //
  * //
  * //    This program is free software: you can redistribute it and/or modify
  * //    it under the terms of the GNU General Public License as published by
  * //    the Free Software Foundation, either version 3 of the License, or
  * //    (at your option) any later version.
  * //
  * //    This program is distributed in the hope that it will be useful,
  * //    but WITHOUT ANY WARRANTY; without even the implied warranty of
  * //    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * //
  * //    You should have received a copy of the GNU General Public License
  * //    along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  *
  */
 
 package cellHTS.pages;
 
 import org.apache.tapestry5.annotations.*;
 import org.apache.tapestry5.upload.services.UploadedFile;
 import org.apache.tapestry5.ioc.internal.util.TapestryException;
 import org.apache.tapestry5.ioc.annotations.Inject;
 import org.apache.tapestry5.ioc.Messages;
 import org.apache.tapestry5.MarkupWriter;
 import org.apache.tapestry5.ComponentResources;
 import org.apache.tapestry5.RenderSupport;
 import org.apache.tapestry5.StreamResponse;
 import org.apache.tapestry5.json.JSONObject;
 import org.apache.tapestry5.beaneditor.BeanModel;
 import org.apache.tapestry5.services.BeanModelSource;
 import org.apache.tapestry5.services.Request;
 import org.chenillekit.tapestry.core.components.InPlaceCheckbox;
 
 
 import java.io.*;
 import java.util.*;
 import java.util.regex.Pattern;
 import java.util.regex.Matcher;
 
 import data.*;
 import cellHTS.classes.*;
 import cellHTS.components.Layout;
 import cellHTS.services.ZIPStreamResponse;
 import cellHTS.dao.Semaphore;
 
 
 /**
  *
  * this is the main class of the application. it contains the main logic of the wizard etc.
  *
  * Created by IntelliJ IDEA.
  * User: oliverpelz
  * Date: 10.11.2008
  * Time: 11:09:37
  * To change this template use File | Settings | File Templates.
  */
 @IncludeJavaScriptLibrary(value = {"${tapestry.scriptaculous}/prototype.js", "divEnabler.js","../components/browserDetect.js","leightbox.js"})
 public class CellHTS2 {
 
     @Inject
     private ComponentResources resc;
     @Inject
     private BeanModelSource beanModelSource;
     @Inject
     private Messages msg;
     @Inject
     private Semaphore semaphore;
 
     //this bool stores if we run cellHTS2 for the first time...this is important for a lot of variable init stuff
     @Persist
     private boolean notFirstRun;
     //this bool stores if we are making a new run
     @Persist
     private boolean notNewRun;
 
 
     @Persist
     private HashMap<String, DataFile> dataFileList;
     //this correspons to files which have already been edited
     @Persist
     private HashSet<String> excludeFilesFromParsing;
 
 
     @Persist
     private String annotFile;
     @Persist
     private String descriptionFile;
     @Persist
     private String plateListFile;
     @Persist
     private String plateConfFile;
     @Persist
     private String screenLogFile;
     //this is a fixed regexp you want to search your filename parameters with
     @Persist
     private String fixRegExp;
 
     @Persist
     private Boolean isFlashValid;
 
     //this three must be persist because we check on it later
     @Persist
     private UploadedFile uploadedPlatelistFile;
     @Persist
     private UploadedFile uploadedPlateConfigFile;
     @Persist
     private UploadedFile uploadedScreenlogFile;
 
     //upload variables, should not be  persistent
     private UploadedFile uploadedDataFile;
 
     private UploadedFile uploadedAnnotFile;
 
     private UploadedFile uploadedDescriptionFile;
 
     private UploadedFile uploadedSessionFile;
 
 
                                  
     @Persist
     private String errorDatafileMsg; //we need this for the first run to not show msg error found in datafile upload
 
 
     @Persist
     private String sessionFileUploadErrorMsg;
 
     @Persist
     private String errorPlateconfFileMsg;
 
     @Persist
     private String errorScreenlogFileMsg;
 
     @Persist
     private String errorAnnotFileMsg;
 
     @Persist
     private String errorDescriptionFileMsg;
 
     @Persist
     private String errorPlatelistFileMsg;
 
     @Persist
     private boolean noErrorUploadFile;
 
     @Persist
     private boolean noErrorPlateConfFile;
 
     @Persist
     private boolean noErrorScreenlogFile;
 
     @Persist
     private boolean noErrorAnnotFile;
 
     @Persist
     private boolean noErrorDescriptionFile;
 
     @Persist
     private boolean errorSessionFileUpload;
 
     @Persist
     private boolean errorPlatelistFileUpload;
 
     @Persist
     private File jobNameDir;
 
     //these are for validation erro messagees
     @Persist
     private boolean validateErrorFileView;
 
     @InjectPage
     private CellHTS2 cellHTS2Page;
 
     @Persist
     private String channelLabel1;
     @Persist
     private String channelLabel2;
 
     @Persist
     private boolean isDualChannel;
 
     @Persist
     private Integer plateFormat;
 
 
     //this is for making the menu in selectbox
     @Persist
     private ChannelTypes channel;
 
     @Persist
     private PlateTypes plate;
 
     @Persist
     private NormalizationTypes normalTypes;
 
     @Persist
     private LogTransform logTransform;
 
     @Persist
     private NormalScalingTypes normalScaling;
 
     @Persist
     private ResultsScalingTypes resultsScaling;
 
     @Persist
     private SummerizeReplicates sumRep;
 
     @Persist
     private ViabilityChannel viabilityChannel;
 
     @Persist
     private String viabilityFunction;
 
     @Persist
     private String googleAnalyticsTrackerID;
 
     @InjectPage
     private Results resultPage;
 
     @InjectPage
     private AdvancedFileImporter advancedFileImporter;
 
     //this variable states if all required input has been made in a certain step of the cellHTS2
     //wizard to enable next step: can be enabled, disabled and none
     @Persist
     private String enableWizardsNewPage;
 
     //Ajax checkbox for parsing filenames option
     @Persist
     private boolean parseFileParams;
     //this is a temp value variable for the checkbox template
 
     //    @Persist
     //    private boolean showAllNext = false;
     @Persist
     private Map<String, String> descriptionMap;
 
     //this time we use the component annotation instead of tml component
     @Component
     private Layout layout;
 
     @Persist
     private Experiment experiment;
 
     //name of the wizard's linkbuttons
     @Persist
     private String backLinkName;
     @Persist
     private String nextLinkName;
     //if the back/forward wizard's linkbuttons are enabled or disabled 
     @Persist
     private boolean backDisable;
     @Persist
     private boolean nextDisable;
     //this variable stores the current page(div) were visiting at
     //one means we are on the first page etc.
     @Persist
     private int currentPagePointer;
     //this stores the successfull filled-in pages...this is necessary when going back in wizard
     //and then back forth
     @Persist
     private HashMap<Integer, Boolean> activatedPages;
 
     @Persist
     private boolean errorNextLink;
 
     @Persist
     private String nextLinkErrorMsg;
 
     @Environmental
     private RenderSupport pageRenderSupport;
 
     //get regex patterns out of the config
     @Persist
     private Pattern annotBodyPattern;
     @Persist
     private Pattern annotHeaderPattern;
     @Persist
     private Pattern descriptionBodyPattern;
     @Persist
     private Pattern descriptionHeaderPattern;
     @Persist
     private Pattern dataFilePattern;
     @Persist
     private Pattern plateConfigHeaderPattern;
     @Persist
     private Pattern plateConfigBodyPattern;
 
     @Persist
     private ArrayList<Plate> clickedWellsAndPlates;
 
     @Persist
     private int posWellAmount;
     @Persist
     private int negWellAmount;
     @Persist
     private String emailAddress;
     @Persist
     private boolean isEmailMandantory;
     @Inject
     private Request request;   
 
     @Persist
     private String osLinefeed;
 
     @Persist
     private String uploadPath;
 
     //this stores the object type of the page we are coming from (if we are coming at all from another page)
     @Persist
     private String activatedFromPage;
     //these are possible files generated by the advanced file importer
     @Persist
     private ArrayList<File> datafilesFromAdvancedFileImporter;
     @Persist
     private File plateConfigFileFromAdvancedFileImporter;
     @Persist
     private File screenlogFileFromAdvancedFileImporter;
     @Persist
     private File annotationFileFromAdvancedFileImporter;
     
 
 
     /**
      *
      * this will be started everytime the page gets reloaded
      *
      */
 
     public void onActivate() {
    //    public void setupRender() {
         //coming from different pages result in different destinies
         activatedFromPageDestiny();
 
         //do a lot of init stuff...and do it only once as long as the whole java app lives!!!!
         if (!notFirstRun) {
             checkAndCreateUploadDirectory();
             //never come in here again!!!
 
             //this is for providing a temp path for the tool...either via command line or via app.properties file
             if(System.getProperty("upload-path")!=null) {
 
                 //get from command line
                 uploadPath=System.getProperty("upload-path");
 
 
             }
             else {
                 //else get from properties file
                 uploadPath=msg.get("upload-path");
             }
 
             if(!uploadPath.endsWith(File.separator)) {
                     uploadPath=uploadPath+File.separator;
             }
 
             notFirstRun = true;
 
 
             osLinefeed=getBrowserOSLineFeed();
             FileParser.lineFeed = osLinefeed;
             FileCreator.lineFeed = osLinefeed;
 
             googleAnalyticsTrackerID=msg.get("google-analytics-tracker-id");
             if(googleAnalyticsTrackerID!=null) {
                 if(googleAnalyticsTrackerID.equals("\"\"")|| googleAnalyticsTrackerID.contains("missing")) {
                   //set to null to check this in the tml
                   googleAnalyticsTrackerID=null;
                 }
 
             }
             //set the app.properties value of the max parallel runs in the semaphore service
             //this is needed because semaphore is a service which cant inject messages by itself
             int maxRuns = Integer.parseInt(msg.get("max-parallel-runs"));
             semaphore.initMaxParallelRuns(maxRuns);
 
             if(msg.get("result-type").equals("email"))  {
                  isEmailMandantory=true;
             }
 
 
             //this happens in the beginning or when dropping all files
             //so we have to drop the plate format information as well
             plateFormat = null;
             enableWizardsNewPage = "none";
             dataFileList = new HashMap<String, DataFile>();
             errorDatafileMsg = ""; //we need this for the first run to not show msg error found in datafile upload
             errorPlateconfFileMsg = "";
             errorAnnotFileMsg = "";
             errorDescriptionFileMsg = "";
             fixRegExp="";               
             channelLabel1 = "FLuc";
             channelLabel2 = "RLuc";
             normalTypes = NormalizationTypes.median;
             logTransform = LogTransform.NO;
             normalScaling = NormalScalingTypes.additive;
             resultsScaling = ResultsScalingTypes.none;
             sumRep = SummerizeReplicates.mean;
             viabilityChannel = ViabilityChannel.NO;
             parseFileParams = true;
             descriptionMap = new HashMap<String, String>();
             activatedPages = new HashMap<Integer, Boolean>();
             excludeFilesFromParsing = new HashSet<String>();             
             experiment = new Experiment();
             backLinkName = "back";
             nextLinkName = "next";
             backDisable = true;
             nextDisable = true;
             nextLinkErrorMsg = "";
             clickedWellsAndPlates= new ArrayList<Plate>();
             annotBodyPattern = Configuration.ANNOTFILE_BODY_PATTERN;
             annotHeaderPattern = Configuration.ANNOTFILE_HEADER_PATTERN;
             descriptionBodyPattern = Configuration.DESCRIPTIONFILE_BODY_PATTERN;
             descriptionHeaderPattern = Configuration.DESCRIPTIONFILE_HEADER_PATTERN;
             dataFilePattern = Configuration.DATAFILE_PATTERN;
             plateConfigHeaderPattern = Configuration.PLATECONFIG_HEADER_PATTERN;
             plateConfigBodyPattern = Configuration.PLATECONFIG_BODY_PATTERN;
 
 
         }
         //this will run everytime we make a new job
         if (!notNewRun) {
 
             //reset it
             notNewRun = true;
 
             initNewRun();
 
 
             jobNameDir = getNewJobnameDir();
 
 
 
         }
         
     }
     public void activatedFromPageDestiny() {
     //get the classname of the page which linked to this cellHTS2 page....
         if(activatedFromPage==null) {
             //if we are not coming from any other page we MUST have called this page by typing the url in the browser
             activatedFromPage="CellHTS2";
             return;
             
         }
 
         
         //activatedFromPage looks like this : cellHTS.pages.CellHTS2
         String[]tmp =  activatedFromPage.split("\\.");
         if(tmp.length>0) {
             activatedFromPage= tmp[tmp.length-1];
         }
         System.out.println("active: "+activatedFromPage);
         //if we are coming from the email results page, we want to start
         //a complete new analysis
         if (activatedFromPage.equals("SuccessPage")) {
             //if the session already exists!
            if(notFirstRun) {
                 //only make a new run if session already exists
                notNewRun=false;
            }
             //after we are comming from we will set the comming from to the new one: cellHTS2
             activatedFromPage="CellHTS2";
 
         }
         //if we are comming from AdvancedFileImporter we have to insert all the generated files into web cellHTS2 
         else if (activatedFromPage.equals("AdvancedFileImporter")) {
             
             if(datafilesFromAdvancedFileImporter!=null) {
                
                 //check and parse all the uploaded filesnames for plate,replicate,channel combinations in the file NAME
                checkAndPutSingleFileToDataFileList(datafilesFromAdvancedFileImporter);
                //parse the file params to make them visible
 
                 //clear the submitted datafiles....we dont need them anymore
                datafilesFromAdvancedFileImporter=null;
 
                //go to the data file upload step
                currentPagePointer=2;
 
                 if(plateConfigFileFromAdvancedFileImporter!=null|| screenlogFileFromAdvancedFileImporter!=null)  {
                     //update our parameters...this will be done in the uploadfilegrid component too but we have to do this
                     //before because we are relying on it
                     FileParser.parseDataFilenameParams(dataFileList, excludeFilesFromParsing,fixRegExp);
                     initalizePlateNWellMap();
                    
                     //currentPagePointer=3;
                     if(!errorScreenlogFileMsg.equals(""))  {
                         //dont show if no screenlog file data was found
                         errorScreenlogFileMsg="";
                     }
                 }
 
                 //if we generated Annotation make a new annotation file
                 if(plateConfigFileFromAdvancedFileImporter!=null) {
                     plateConfFileToPlateDesignerLayout(plateConfigFileFromAdvancedFileImporter);
                     plateConfigFileFromAdvancedFileImporter=null;
                 }
                 if(screenlogFileFromAdvancedFileImporter!=null) {
                     screenlogFileToPlateDesignerLayout(screenlogFileFromAdvancedFileImporter);
                     screenlogFileFromAdvancedFileImporter=null;
                 }
 
                 //if we have got an annotation file from the advanced file importer
                 if(annotationFileFromAdvancedFileImporter!=null) {
                     loadAnnotationFile(annotationFileFromAdvancedFileImporter);
                     annotationFileFromAdvancedFileImporter=null;
 
                     //dont show message Error no screen log file
                    //dont show message error no well could be parsed
                     //currentPagePointer=5;
                 }
 
 
 
 
             }
 
             //after we are comming from we will set the comming from to the new one: cellHTS2
             activatedFromPage="CellHTS2";
         }
 
     }
     /**
      *
      * before actually rendering the template we will set the
      * back and forth button to disable or enable depending on which step we are
      *
      */
     @BeforeRenderTemplate
     public void renderTempl() {
         //first process the back link button
         if (currentPagePointer == 1) {
             backDisable = true;
         } else {
             backDisable = false;
         }
 
         //now to the forward button
         if (activatedPages.containsKey(currentPagePointer)) {
             nextDisable = activatedPages.get(currentPagePointer);
         } else {
             nextDisable = true;
         }
     }
 
     /**
      *
      * get the channel for the template
      *
      * @return
      */
     public boolean getIsDualChannel
             () {
         if (channel != null) {
             if (channel.equals(ChannelTypes.dual)) {
                 isDualChannel = true;
             } else {
                 isDualChannel = false;
             }
         } else {
             isDualChannel = false;
         }
         return isDualChannel;
     }
 
 
     /**
      *
      *   here we catch the back and next action events
      *   this activates when somebody clicks the actual activated links
      *  it just counts on which step were at
      *
      */
     public void onActionFromBackLink() {
         //if we slide back...no errors can be possible
         errorNextLink = false;
         //default:no error msg
         nextLinkErrorMsg = "";
         resetErrorMsgs();
 
         if (currentPagePointer > 1) {
             currentPagePointer--;
         }
     }
 
 
     /**
      *
      *  here lay some error messages which can occur when not filling out
      *  one specific step correctly
      *
      */
     public void onActionFromNextLink() {
         //default:show no error msg
         errorNextLink = false;
         //default:no error msg
         nextLinkErrorMsg = "";
 
         //(step 1) here we define some events which should be executed when a specific next button was pressed
         if (currentPagePointer == 1) {
             //if we are hopping from page1 ->2 update the experiment's channel
             //to have it later in the experiments description bean edit form
             experiment.setChannel1(channelLabel1);
             experiment.setChannel2(channelLabel2);
             
             //check if we have not set up "please select" in the drop down menue
             if (channel == null) {
                 errorNextLink = true;
                 nextLinkErrorMsg = "Error: please select either single or dual channel in the drop down menue";
             }
             if (channelLabel1.equals("") || channelLabel2.equals("")) {
                 errorNextLink = true;
                 nextLinkErrorMsg = "Error: if you select dual channel you have to provide some names for them";
             }
             if(!errorNextLink) {
                 //if we had uploaded a wrong file but instead clicked successfully on next, let the session file upload dissapear
                 errorSessionFileUpload = false;
                 resetErrorMsgs();
                 //TODO: page refresh doesnt work properly at the moment (there are some browser incompatibelites..have to wait till tapestry development fixed it)
                 activatedPages.put(currentPagePointer + 1, false);
             }
         }
 
         //(step 2) check if all mandantory filename parameters are given
         else if (currentPagePointer == 2) {
             int channel1Amount = 0;
             int channel2Amount = 0;
             //iterate through all given files
 
             if (dataFileList.isEmpty()) {
                 errorNextLink = true;
                 nextLinkErrorMsg = "Error: no valid files were uploaded";
             } else {
                 Iterator fileIterator = dataFileList.keySet().iterator();
                 HashSet<String> uniqueIDs = new HashSet<String>();
                 
                 while (fileIterator.hasNext()) {
                     String key = (String) fileIterator.next();
                     DataFile file = dataFileList.get(key);
                     //if single channel
                     if (!isDualChannel) {
                         //check if all mandantory file params are there
                         if (file.getPlateNumber() == null || file.getReplicate() == null) {
                             errorNextLink = true;
                             nextLinkErrorMsg = "Error: mandantory file parameters plate number or replicate missing";
                         }
                         String id = file.getPlateNumber()+"_"+file.getReplicate();
                         if(!uniqueIDs.contains(id)) {
                             uniqueIDs.add(id);
                         }
                         else {
                             errorNextLink = true;
                             nextLinkErrorMsg = "Error: two files cannot have the same plate, replicate combination";
                         }
 
                     } else {
                         if (file.getPlateNumber() == null || file.getReplicate() == null || file.getChannel() == null) {
                             errorNextLink = true;
                             nextLinkErrorMsg = "Error: mandantory file parameters plate number, replicate or channel number missing";
                         }
                         if (file.getChannel() == null) {
                             //do nothing
                         } else if (file.getChannel() == 1) {
                             channel1Amount++;
                         } else if (file.getChannel() == 2) {
                             channel2Amount++;
                         }
                         String id = file.getPlateNumber()+"_"+file.getReplicate()+"_"+file.getChannel();
                         if(!uniqueIDs.contains(id)) {
                             uniqueIDs.add(id);
                         }
                         else {
                             errorNextLink = true;
                             nextLinkErrorMsg = "Error: two files cannot have the same plate, replicate and channel combination";
                         }
                     }
 
                 }
                 //channel amount must be equal
                 if (channel1Amount != channel2Amount) {
                     errorNextLink = true;
                     nextLinkErrorMsg = "Error: there must be equal channel1 and 2 amount";
                 }
 
                 if(!errorNextLink) {
                 //now this is an important step:
                 //init the platesNWell datastructure to work with
                     initalizePlateNWellMap();
                     resetErrorMsgs();
                     
                 }
             }
         }
 
         //(step3) we can only carry on if at least one positive well was selected in the all plate
         else if (currentPagePointer == 3) {
             errorNextLink = true;
             nextLinkErrorMsg = "Error: please define at least one positive or negative well position at all";
 
             posWellAmount = getWellTypeAmountOfAllPlates("pos");
             negWellAmount = getWellTypeAmountOfAllPlates("neg");
             //these is only temporary we dont need the information later
             Integer posNegWellAmount = getPosNegWellAmountOfAllPlates();
             if(posNegWellAmount >0) {
                errorNextLink = false;
                resetErrorMsgs();
                 
                 
             }
         }
 
         //(step 4) some combinations of cellHTS2 program parameters are impossible in conjunction
         else if (currentPagePointer == 4) {
            
             //some of the normalization methods MUST have negatives
             if (negWellAmount == 0 && (normalTypes.equals(NormalizationTypes.negatives) || normalTypes.equals(NormalizationTypes.NPI))) {
                 errorNextLink = true;
                 nextLinkErrorMsg = "Error: Missing negative wells when using normalization method Negatives or NPI ";
             }
             if (logTransform.equals(LogTransform.YES) && normalScaling.equals(NormalScalingTypes.additive)) {
                 errorNextLink = true;
                 nextLinkErrorMsg = "Error: If normalization scaling method is Additive, log transform can only be set to NO";
             }
 
         }
 
         //only go to next div if we are errorfree
         if (!errorNextLink) {
             experiment.setDualChannel(isDualChannel);
             currentPagePointer++;
             resetErrorMsgs();
             
         }
     }
 
 
 
     /**
      *
      * catch html events of type onXXX..we need this to get the change from the drop down without submitting the form
      * this catches step1
      *
      *
      * @param type the string of the channel drop down either single or dual
      */
     @OnEvent(component = "channel", value = "change")
     public JSONObject onChangeChannelEvent(String type) {
         //this first select has to be executed so we will check here if the request was ajax enabled
         if(!request.isXHR()) {
             throw new TapestryException("your browser cannot handle AJAX XHR requests. Talk to your ISP to configure the proxy accordingly",null);
         }
 
         if (type.equals("single")) {
             channel = ChannelTypes.single;
             isDualChannel = true;
             //activate the forward link
             activatedPages.put(currentPagePointer, false);
 
         } else if (type.equals("dual")) {
             channel = ChannelTypes.dual;
             isDualChannel = false;
             activatedPages.put(currentPagePointer, false);
 
         } else {
             //deactivate the current page if not single or dual was chosen
             //TODO: the refresh doesnt work 100% in all browsers therefore we cant deavtivate it : activatedPages.put(currentPagePointer,true);
             //instead we will activate it as well:
             activatedPages.put(currentPagePointer, false);
         }
         return new JSONObject().put("dummy", "dummy");
     }
     
     /**
      *
      * these 3 methods are to get the values on the server without submitting them ...ajax
      *
      * @param label the submitted textstring
      */
     @OnEvent(component = "channel1Textfield", value = "blur")
     public JSONObject onChannel1Textfield(String label) {
         if(!request.isXHR()) {
             throw new TapestryException("your browser cannot handle AJAX XHR requests. Talk to your ISP to configure the proxy accordingly",null);
         }
        channelLabel1=label;
         return new JSONObject().put("dummy", "dummy");
     }
 
     /**
      *
      * these 3 methods are to get the values on the server without submitting them ...ajax
      *
      * @param label
      */
     @OnEvent(component = "channel2Textfield", value = "blur")
     public JSONObject onChannel2Textfield(String label) {
         if(!request.isXHR()) {
             throw new TapestryException("your browser cannot handle AJAX XHR requests. Talk to your ISP to configure the proxy accordingly",null);
         }
        channelLabel2=label;
         return new JSONObject().put("dummy", "dummy");
     }
 
     /**
      *
      * these 3 methods are to get the values on the server without submitting them ...ajax
      *
      * @param function
      */
     @OnEvent(component = "viabilityFunctionTextfield", value = "blur")
     public JSONObject onViabilityFunctionTextfield(String function) {
         if(!request.isXHR()) {
             throw new TapestryException("your browser cannot handle AJAX XHR requests. Talk to your ISP to configure the proxy accordingly",null);
         }
        viabilityFunction=function;
         return new JSONObject().put("dummy", "dummy");
     }
 
 
     /**
      *
      * these 3 methods are to get the values on the server without submitting them ...ajax
      *
      * @param label
      */
     @OnEvent(component = "emailAddressTextfield", value = "blur")
     public JSONObject onEmailAddressTextfield(String label) {
         if(!request.isXHR()) {
             throw new TapestryException("your browser cannot handle AJAX XHR requests. Talk to your ISP to configure the proxy accordingly",null);
         }
        emailAddress=label;
         //use this to lose the focus of the textfield because we want to jump to the head of the page
         JSONObject json = new JSONObject();
         json.put("loseFocus","true");
         return json;
     }
 
 
     
     /**
      *
      *   this method will be started when successfully uploaded a platelist file
      *
      */
     void onSuccessFromPlatelistFileUpload() {
         errorPlatelistFileUpload = false;
         errorPlatelistFileMsg="";
         String filename = uploadedPlatelistFile.getFileName();
         //upload the file to a specific path on the server
         String newFilePath = jobNameDir.getAbsolutePath() + File.separator + filename;
         File copied = new File(newFilePath);
         uploadedPlatelistFile.write(copied);
         Pattern headerPattern = Configuration.PLATELIST_HEADER_PATTERN;
         Pattern bodyPattern = Configuration.PLATELIST_BODY_PATTERN;
 
 
 
         HashMap<String,Integer[]> resultMap=new HashMap<String,Integer[]>();
         String errorMsg[]=FileParser.parsePlatelistFile(headerPattern,bodyPattern,copied,resultMap);
         if (errorMsg[0].equals("false")) {
             errorPlatelistFileUpload = true;
             errorPlatelistFileMsg = "Error: "+errorMsg[1];
         }
         else if (resultMap.size() == 0) {
             errorPlatelistFileUpload = true;
             errorPlatelistFileMsg ="Error: cannot find platelist entry at all";
         }
         else {
             for(String singleFile : resultMap.keySet()) {
 
                 //check if files match 
                  if(dataFileList.containsKey(singleFile)) {
                      
                      Integer[] arr = resultMap.get(singleFile);
                      DataFile file = dataFileList.get(singleFile);
                      //file.setFileName(singleFile);
                      file.setPlateNumber(arr[0]);
                      file.setReplicate(arr[1]);
                      file.setChannel(arr[2]);
                      
                      //exclude this file from parsing again
                      excludeFilesFromParsing.add(singleFile);
 
                  }
                 
             }
         }
         if(copied.exists()) {
             //we dont want to use this plate list file at all instead of for parsing the filename parameters
             copied.delete();
         }
         
 
     }
 
     
     /**
      *
      *  this method will be started when uploading a session file
      *
      */
     void onSuccessFromSessionFileUpload() {
         //default:show no error msg
         errorSessionFileUpload = false;
         //default:no error msg
         sessionFileUploadErrorMsg = "";
 
         String filename = uploadedSessionFile.getFileName();
         //now that our session file contains all generated data files in the zip we will make a new job id
         //create a jobname and files and everything
 
         jobNameDir = this.getNewJobnameDir();
         File newJobNameDir = jobNameDir;
         
         //upload the file to a specific path on the server
         String newFilePath = jobNameDir.getAbsolutePath() + File.separator + filename;
         File copied = new File(newFilePath);
         //uploadedSessionFile.write(copied);
         // check if the jobnamedir does exist and create it in case of not
         writeFileProxy(uploadedSessionFile,copied);
 
         PersistentCellHTS2 persistentCellHTS2 = null;
         //unpack the file...
 
         //keep a log when errors appear
         String errorMsg[] = new String[2];
         ArrayList<File> files = ShellEnvironment.unzip(errorMsg, newFilePath, jobNameDir.getAbsolutePath());
         if (errorMsg[0].equals("true")) {
             errorSessionFileUpload = true;
             sessionFileUploadErrorMsg = "Error: "+errorMsg[1];
         } else if (files.size() == 0) {
             errorSessionFileUpload = true;
             sessionFileUploadErrorMsg = "Error: empty zip file";
         } else {
 
             //search for the session file in the dir
             Pattern p = Pattern.compile(".PST$");
             Matcher m;
             String sessionFile=null;
             for(File tempFile : files) {
                m = p.matcher(tempFile.getName());
                if(m.find()) {
                    sessionFile=tempFile.getAbsolutePath();
                    break;
                }
             }
             if(sessionFile==null) {
                 errorSessionFileUpload = true;
                 sessionFileUploadErrorMsg = "Error: zipfile does not content persistent file";
                 //dont go any further
                 return;
             }
 
 
             FileInputStream fis = null;
             ObjectInputStream in = null;
             try {
                 fis = new FileInputStream(sessionFile);
                 in = new ObjectInputStream(fis);
                 persistentCellHTS2 = (PersistentCellHTS2) in.readObject();
                 in.close();
                 if(persistentCellHTS2==null) {
                     errorSessionFileUpload = true;
                     sessionFileUploadErrorMsg = "Error: cannot read persistence data...maybe you used an older cellHTS2 version which is not compatible (check build/version number)";
                 }
             }
             catch (IOException ex) {
                 errorSessionFileUpload = true;
                 sessionFileUploadErrorMsg = "Error: cannot read persistence data...maybe you used an older cellHTS2 version which is not compatible (check build/version number)";
                 ex.printStackTrace();
             }
             catch (ClassNotFoundException ex) {
                 errorSessionFileUpload = true;
                 sessionFileUploadErrorMsg = "Error: cannot read persistence data...maybe you used an older cellHTS2 version which is not compatible (check build/version number)";
                 ex.printStackTrace();
             }
         }
         //retrieve persistent data
         if (!errorSessionFileUpload && persistentCellHTS2 != null) {
 
             initNewRun();
             experiment = persistentCellHTS2.getExperiment();
             activatedPages = persistentCellHTS2.getActivatedPages();
             descriptionMap = persistentCellHTS2.getDescriptionMap();
             posWellAmount = persistentCellHTS2.getPosWellAmount();
             negWellAmount = persistentCellHTS2.getNegWellAmount();
             descriptionFile = persistentCellHTS2.getDescriptionFile();
             annotFile = persistentCellHTS2.getAnnotFile();
             plateListFile = persistentCellHTS2.getPlateListFile();
             plateConfFile = persistentCellHTS2.getPlateConfFile();
             isDualChannel = persistentCellHTS2.isDualChannel();
             channelLabel1 = persistentCellHTS2.getChannelLabel1();
             channelLabel2 = persistentCellHTS2.getChannelLabel2();
             plateFormat = persistentCellHTS2.getPlateFormat();
             channel = persistentCellHTS2.getChannel();
             plate = persistentCellHTS2.getPlate();
             normalTypes = persistentCellHTS2.getNormalTypes();
             logTransform = persistentCellHTS2.getLogTransform();
             normalScaling = persistentCellHTS2.getNormalScaling();
             resultsScaling = persistentCellHTS2.getResultsScaling();
             sumRep = persistentCellHTS2.getSumRep();
             noErrorUploadFile = persistentCellHTS2.isNoErrorUploadFile();
             noErrorPlateConfFile = persistentCellHTS2.isNoErrorPlateConfFile();
             noErrorAnnotFile = persistentCellHTS2.isNoErrorAnnotFile();
             noErrorDescriptionFile = persistentCellHTS2.isNoErrorDescriptionFile();
             validateErrorFileView = persistentCellHTS2.isValidateErrorFileView();
 
             errorDatafileMsg = persistentCellHTS2.getErrorDatafileMsg();
             errorPlateconfFileMsg = persistentCellHTS2.getErrorPlateconfFileMsg();
             errorAnnotFileMsg = persistentCellHTS2.getErrorAnnotFileMsg();
             errorDescriptionFileMsg = persistentCellHTS2.getErrorDescriptionFileMsg();
           
             enableWizardsNewPage = persistentCellHTS2.getEnableWizardsNewPage();
             parseFileParams = persistentCellHTS2.isParseFileParams();
             backDisable = persistentCellHTS2.isBackDisable();
             nextDisable = persistentCellHTS2.isNextDisable();
             currentPagePointer = persistentCellHTS2.getCurrentPagePointer();
             errorNextLink = persistentCellHTS2.isErrorNextLink();
             nextLinkErrorMsg = persistentCellHTS2.getNextLinkErrorMsg();
             dataFileList = persistentCellHTS2.getDataFileList();
             excludeFilesFromParsing = persistentCellHTS2.getExcludeFilesFromParsing();
             screenLogFile = persistentCellHTS2.getScreenLogFile();
             clickedWellsAndPlates = persistentCellHTS2.getClickedWellsAndPlates();
             viabilityChannel=persistentCellHTS2.getViabilityChannel();
             emailAddress= persistentCellHTS2.getEmailAddress();
             fixRegExp=persistentCellHTS2.getFixRegExp();
             viabilityFunction=persistentCellHTS2.getViabilityFunction();
             
             //email processing is nothing which you want to be saved
             //isEmailMandantory=persistentCellHTS2.isEmailMandantory();
 
             //restore the old one
             jobNameDir = newJobNameDir;
 
 
         }
         
 
 
     }
 
 
     /**
      *
      *  this method will be started when trying to upload a
      *  data file
      *  step2
      *
      */
     void onSuccessFromDataFileUpload() {
 
         String filename = uploadedDataFile.getFileName();
 
         //upload the file to a specific path on the server
         String newFilePath = jobNameDir.getAbsolutePath() + File.separator + filename;
 
 
         File copied = new File(newFilePath);
         //uploadedDataFile.write(copied);
         //check if the jobnamedir does exist and if not create it now
         writeFileProxy(uploadedDataFile,copied);
 
 
         //all files to add..single file vs. multifilearchives
         ArrayList<File> filesToCheck = new ArrayList<File>();
         //check if we got an archive
         Pattern zipFilePattern = Configuration.ALLOWED_DATA_ARCHIVES;
         Matcher m = zipFilePattern.matcher(filename);
         //if we have a zip file
         String[] zipErrorMsg = new String[2];
         zipErrorMsg[0]="false";
         if (m.find()) {
             //extract archive
             filesToCheck = ShellEnvironment.plainUnzip(zipErrorMsg, newFilePath, jobNameDir.getAbsolutePath());
             int i = 0;
         } else {
             filesToCheck.add(copied);
         }
         noErrorUploadFile = true;
 
         String falseFiles="";
         if (zipErrorMsg[0].equals("false")) {
              checkAndPutSingleFileToDataFileList(filesToCheck);
 
         }else {
             noErrorUploadFile = false;
             errorDatafileMsg = zipErrorMsg[1];
         }
 
         
 
 
 
     }
     public void checkAndPutSingleFileToDataFileList(ArrayList<File> filesToCheck) {
             int thisPlateFormat = 0;
             //add all files uploaded in this round
             for (File singleFile : filesToCheck) {
 
                 //check the complete file with a regexp
                 //and only add it if it passes the text
                 String[] result = FileParser.checkFileRegExp(dataFilePattern, singleFile, null);
 
                 if (result[0].equals("true")) { //leave datafile obj empty cause we calculate this in our UploadedFileGrid component
                     thisPlateFormat = Integer.parseInt(result[1]);
                       System.out.println("i am in this method!:"+thisPlateFormat);
                     //iterate over all valid enum values
                     boolean foundvalidFormat = false;
                     String validPlateFormat = "";
                     for (PlateTypes type : PlateTypes.values()) {
                         int plateFormat = convertPlateTypesToInt(type);
                         //build this string for error msg's
                         validPlateFormat += " " + plateFormat;
                         if (plateFormat == thisPlateFormat) {
 //                            this.plateFormat = plateFormat;
                             foundvalidFormat = true;
                             break;
                         }
                     }
                     if (!foundvalidFormat) {
                         noErrorUploadFile = false;
                         errorDatafileMsg = "Error: you uploaded datafile(s) which do not fit into valid plate formats: " + validPlateFormat + " wells. Filename: "+singleFile.getName();
                         break;
                     }
                     //check if plateFormat differs between uploaded files
                     if (plateFormat != null) {
                         if (plateFormat != thisPlateFormat) {
                             noErrorUploadFile = false;
                             errorDatafileMsg = "Error: you uploaded datafiles which have no equal plateformat: " + thisPlateFormat + " vs " + plateFormat;
                             //erase the list before because its useless ...which is the correct format?
                             dataFileList.clear();
                             break;
                         }
 
                     }
                     else {
                         plateFormat = thisPlateFormat;
                     }
                       System.out.println("we will put!:"+thisPlateFormat);
                     //check if we have a valid format
                     dataFileList.put(singleFile.getName(), new DataFile(singleFile.getName()));
                     
                     //we reset this
                     errorNextLink=false;
 
 
                 } else {
                     //if error occured erase the uploaded file
                     //singleFile.delete();
                     //if error accured show it in the page
                     noErrorUploadFile = false;
                     errorDatafileMsg = "Error in : " + singleFile.getName() + " msg : " + result[1];
                     //do not process any further file
                     break;
                 }
 
             }
 
 
             if (noErrorUploadFile && dataFileList.size()>0) {
 
 
                 plate = convertIntToPlateTypes(thisPlateFormat);
                 //enable goto next page
                 activatedPages.put(currentPagePointer, false);
                 //we have to do this if we update our list..otherwise the plate configurator drop down menue
                 //will stay the same if you go back
                 clickedWellsAndPlates.clear();
 
             }
 
 
         //show/enable both back and forward link in the next step..this is for the plateconfig
         activatedPages.put(currentPagePointer + 1, false);
         //show enable forward link in the second next page too ..this for the statistics parameterization
         activatedPages.put(currentPagePointer + 2, false);
     }
 
     /**
      *
      *  this is the callback for the checkbox to use intelligent filename parsing
      *
      * @param id the submitted id
      * @param checked if checked or not
      */
     @OnEvent(component = "inplacecheckbox", value = InPlaceCheckbox.EVENT_NAME)
     public JSONObject inPlaceCheckbox(String id, boolean checked) {
         if(!request.isXHR()) {
             throw new TapestryException("your browser cannot handle AJAX XHR requests. Talk to your ISP to configure the proxy accordingly",null);
         }
 
         parseFileParams = checked;
         return new JSONObject().put("dummy", "dummy");
     }
 
     //
     /**
      *
      * this will be called if you drop all uploaded files
      *
      */
     public void onActionFromDropDataFileList() {
         //TODO:should we erase the files on the server??
         dataFileList.clear();
         excludeFilesFromParsing.clear();
         noErrorUploadFile=false;
         errorDatafileMsg ="";             
         errorPlatelistFileMsg="";
         
         //if we drop all the datafiles the uploaded plate list file should dissapear too
         this.uploadedPlatelistFile=null;
         this.uploadedPlateConfigFile=null;
         
     }
 
     //
     /**
      *
      * when a user tries uploading a plate config file
      *
      */
     public void onSuccessFromPlateConfigFileUpload() {
 
         String filename = uploadedPlateConfigFile.getFileName();
 
         //upload the file to a specific path on the server
         String newFilePath = jobNameDir + File.separator + filename;
 
         File copied = new File(newFilePath);
         uploadedPlateConfigFile.write(copied);
 
         plateConfFileToPlateDesignerLayout(copied);
     }
 
         public void plateConfFileToPlateDesignerLayout(File copied) {
         //we use an array to simulate call by reference in the parsePlateConfigFile method
         Integer[] wells = {0};
 
         HashMap<Integer, HashMap<String, String>> tempParseResult = new HashMap<Integer, HashMap<String, String>>();
         String[] result = FileParser.parsePlateConfigFile(plateConfigBodyPattern, copied, plateConfigHeaderPattern, tempParseResult, wells);
         //the results we can use for our plate to display are stored in plate 0
         //so extract only them
 
         if (result[0].equals("true")) {
 
             if(tempParseResult.size()==0) {
                 result[0]="false";
                 result[1]="Error: cannot find pos/neg Plate Config file entry at all";
             }
 
             //add them to sampleWellMap (the first plate)  and all the other plates
             //javascript plate.js rely on the well_xxx ids so we have to translate them here
             HashMap<String, String> tmp = new HashMap<String, String>();
 
 
            for(Integer plateNum : tempParseResult.keySet()){
 
                 ArrayList<Plate> ourTargetPlates=new ArrayList<Plate>();
 
                 for(Plate plate : clickedWellsAndPlates) {
 
                    if(plateNum.equals(plate.getPlateNum())) {
                         ourTargetPlates.add(plate);
                        
                    }
                 }                 
            
                  //iterate over keys
                 HashMap<String, String> key = tempParseResult.get(plateNum);
                 Iterator iterat = key.keySet().iterator();
                  
                 while (iterat.hasNext()) {
                     String wellID = (String) iterat.next();
                     String wellType = key.get(wellID);
                     //translate it to js
                     wellID = "well_"+wellID;
                     //put them into the plateArr
                     if(plateNum==0) {
                         //put this well into all plates!
 
                         for(int i=0;i<clickedWellsAndPlates.size();i++) {
                            clickedWellsAndPlates.get(i).getWellsArray().put(wellID,wellType);
                         }
                     }
                     else {
                         for(Plate plate : ourTargetPlates) {
                             plate.getWellsArray().put(wellID,wellType);
                         }
                     }
                 }
             }
            //sometimes we got the following scenario: define some plates in the list, upload a big plateconfig file with more files than the ones defined in the list
             // which in the plateconfig file got no common wells on the "all" plate. Because we are uploaded only a few files there will be common wells over all of the few
           //so we will calculate our own all plate again here...i know this is some kind of redundant but I cant see any other fast solution
            rebuildZeroPlate();
 
 
                
 
 
             boolean validFormat = false;
             String allowedTypes = "";
             for (PlateTypes type : PlateTypes.values()) {
                 int plateFormat = convertPlateTypesToInt(type);
                 allowedTypes += " " + plateFormat;
                 if (wells[0] == plateFormat) {
                     validFormat = true;
                 }
 
             }
             if (validFormat) {
                 plateFormat = wells[0];
                 plate = convertIntToPlateTypes(wells[0]);
             } else {
                 result[0] = "false";
                 result[1] = "Error: unknown wells format " + wells + " allowed types are " + allowedTypes;
             }
         }
 
 
         
         if (result[0].equals("false")) {
             noErrorPlateConfFile = false;
             errorPlateconfFileMsg = result[1];
         } else {
             noErrorPlateConfFile = true;
             //enable goto next page
             //next step should be enabled all
             activatedPages.put(currentPagePointer + 1, false);
 
         }
 
 
     }
 
     /**
      *
      * when a user tries uploading a screenlog file
      *
      */
     public void onSuccessFromScreenlogFileUpload() {
 
         String filename = uploadedScreenlogFile.getFileName();
 
         //upload the file to a specific path on the server
         String newFilePath = jobNameDir + File.separator + filename;
 
         File copied = new File(newFilePath);
         uploadedScreenlogFile.write(copied);
         screenlogFileToPlateDesignerLayout(copied);
 
     }
     
     public void screenlogFileToPlateDesignerLayout(File copied) {
         //we use an array to simulate call by reference in the parsePlateConfigFile method
         Pattern headerPattern = Configuration.SCREENLOG_HEADER_PATTERN;
 
        
         HashMap<Integer,ArrayList<String[]>> contaminatedData = new HashMap<Integer,ArrayList<String[]>>();
 
         String[] result =FileParser.parseScreenlogFile(headerPattern,copied,contaminatedData);
 
 
         //count amount of wells which are defined in the screenlog in more than one plate
         HashMap<String,Integer> countMultiWells = new HashMap<String,Integer>();
         if(result[0].equals("true")) {
 
              if(contaminatedData.size()<1) {
                 noErrorScreenlogFile = false;
                 errorScreenlogFileMsg = "Warning: no entry found in Screenlogfile";
                 return;
              }
 
             for(Plate plate  : clickedWellsAndPlates) {
                 int plateNum = plate.getPlateNum();
                 int repliNum = plate.getReplicateNum();
 
                 if(contaminatedData.containsKey(plateNum)) {
                     ArrayList<String[]> allLines = contaminatedData.get(plateNum);
 
                     for(String[] line : allLines ) {
                         Integer repNum=Integer.parseInt(line[0]);
                         String channel=line[1];
                         String wellID=line[2];
                         String comment=line[3];
 
                         //TODO: Multichannel screenlog files can contain a channel column
                         // but I dont see any reason at all why contaminated wells should only work for one or the other channel
                         //in my opinion we have to defined contaminated wells for all channels for one plate/replicate combination
                         //so we disregard channels here...we first would have to modify plate class either!!!
 
                         if(repNum==repliNum) {
                             
                             plate.getWellsArray().put("well_"+wellID,"cont1");    //TODO:maybe change cont1 to comment but than we have to modify plate.js too here
 
                             //count how many times a cont well exists at a specific position
                             if(!countMultiWells.containsKey(wellID)) {
                                 countMultiWells.put(wellID,1);
                             }
                             else {
                                 int oldNum = countMultiWells.get(wellID);
                                 countMultiWells.put(wellID,oldNum+1);
                             }
                         }
 
 
                     }
 
 
                 }
             }
             //rebuild plate zero..get all common among all of the plates in the set
             rebuildZeroPlate();
 
 
             noErrorScreenlogFile = true;
         }
         else {
            noErrorScreenlogFile = false;
            errorScreenlogFileMsg = result[1]; 
         }
 
     }
 
 
 
 
     /**
      *
      * when a user tries uploading a annot file
      *
      */
     public void onSuccessFromAnnotFileUpload
             () {
         String filename = uploadedAnnotFile.getFileName();
 
         //upload the file to a specific path on the server
         String newFilePath = jobNameDir + File.separator + filename;
 
         File copied = new File(newFilePath);
         uploadedAnnotFile.write(copied);
         loadAnnotationFile(copied);
     }
 
     public void loadAnnotationFile(File copied) {
         String result[] = FileParser.checkFileRegExp(annotBodyPattern, copied, annotHeaderPattern);
 
         if (result[0].equals("true")) {
             annotFile = copied.getName();
             noErrorAnnotFile = true;
 
         } else {
             noErrorAnnotFile = false;
             errorAnnotFileMsg = result[1];
 
         }
     }
 
 
     /**
      *
      *   activated when user tries uploading description file
      *
      */
     public void onSuccessFromDescriptionFileUpload() {
         String filename = uploadedDescriptionFile.getFileName();
         //upload the file to a specific path on the server
         String newFilePath = jobNameDir + File.separator + filename;
 
         File copied = new File(newFilePath);
         uploadedDescriptionFile.write(copied);
         String result[] = FileParser.checkFileRegExp(descriptionBodyPattern, copied, descriptionHeaderPattern);
         if (result[0].equals("true")) {
             descriptionFile = copied.getName();
             noErrorDescriptionFile = true;
             //parse the file with cellhts
             if(!FileParser.parseDescriptionFile(copied,experiment))  {
                 //somethings wrong while parsing
                 noErrorDescriptionFile = false;
                 errorDescriptionFileMsg = "Error: error parsing description file";
                 return;
             }
 
 
             experiment.setScreentype("quantitative");
 
            
 
 
         } else {
             noErrorDescriptionFile = false;
             errorDescriptionFileMsg = result[1];
             return;
         }
 
     }
 
     /**
      *
      * activated when user clicks on activate
      *
      */
     public void onSuccessFromUpdateExperimentDesc() {
         //update or create the file
         createOrupdateDescriptionFile();
     }
 
 
     public void onFailureFromDataFileUpload() {
 
         noErrorUploadFile = false;
         errorDatafileMsg = "Error: could not upload the file, maybe its too big to upload";
 
     }
 
     public void onFailureFromPlateConfigFileUpload
             () {
         noErrorUploadFile = false;
         errorDatafileMsg = "Error: could not upload the file, maybe its too big to upload";
 
     }
 
     public void onFailureFromAnnotFileUpload
             () {
         noErrorUploadFile = false;
         errorDatafileMsg = "Error: could not upload the file, maybe its too big to upload";
 
     }
 
     public void onFailureFromDescriptionFileUpload
             () {
         noErrorUploadFile = false;
         errorDatafileMsg = "Error: could not upload the file, maybe its too big to upload";
 
     }
 
     /**
      *
      * if we want to drop the annotation file
      *
      */
      public void onActionFromDropAnnotfile() {
          noErrorAnnotFile=false;
          errorAnnotFileMsg="";
          annotFile=null;
      }
 
     /**
      *
      * if we click on drop description file
      *
      */
      public void onActionFromDropDescriptionfile() {
          noErrorDescriptionFile = false;
          errorDescriptionFileMsg = "";
          experiment.clear();
          descriptionFile = null;
 
 
      }
 
 
 
     /**
      *
      * //this is the link to start the cellHTS2 page main logic
      *
      * @return
      */
     public Object onActionFromStartCellHTS2() {
         //TODO:show any errors ajax style
         errorNextLink=false;
 
         nextLinkErrorMsg = "";
         //all checks befor were no mandantory checks so we will do this now again and more stringent
 
         //step1 check
         if (isDualChannel && (channelLabel1.equals("") || channelLabel2.equals(""))) {
             nextLinkErrorMsg += " Error: Step1: channel labels mustnt be empty\n ";
             errorNextLink = true;
 
         }
         //if we are external ...
         if(isEmailMandantory) {
             if(emailAddress==null) {
                 emailAddress="";
             }
             //check if email was valid
             String emailRegexp = msg.get("emailAddressTextfield-regexp");
             Pattern p = Pattern.compile(emailRegexp);
             Matcher m = p.matcher(emailAddress);
              if(!m.find()) {
                  errorNextLink = true;
                  nextLinkErrorMsg += "Error: please provide a valid email address";
                  return cellHTS2Page;
              }
         }
         //if we are internal
         else {
             String exp = experiment.getTitle();
              if( exp==null) {
                 exp="";
             }
             //check if the experiments title (in this case the screen id) was valid
             String screenIDRegexp = msg.get("screenID-regexp");
             Pattern p = Pattern.compile(screenIDRegexp);
             Matcher m = p.matcher(exp);
 
             //TODO: comment it in if we will use strict screen ids later....
 //             if(!m.find()) {
 //                 errorNextLink = true;
 //                 nextLinkErrorMsg += " Missing valid Screen ID ";
 //                 return cellHTS2Page;
 //             }
         }
 
            //call by ref
             String[]errorMsgArr=new String[1];
             boolean hasfailed =generateCellHTS2Files(errorMsgArr);
             String hasfailedMsg = errorMsgArr[0];
 
             if(hasfailed==true) {
                 errorNextLink = true;
                 nextLinkErrorMsg+= hasfailedMsg;
             }
 
             if (errorNextLink) {
                   return cellHTS2Page;
             }
 
         //this will run and show the resultpage...put all the relevat stuff into it
         resultPage.initNewRun();
 
 //        String screenName = experiment.getScreen();
 //        replace spaces with _ underscore ..we will use this as our filename prefix and i cant stand files with spaces in between
 //        if(screenName!=null) {
 //           screenName=screenName.replaceAll("\\s+","_");
 //        }
         if(experiment.getTitle()==null) {
             experiment.setTitle("");
         }
         if(experiment.getScreenID()==null) {
             experiment.setScreenID("");
         }
         String screenName;
         //either we have a screenID (internal) or a screen title (external)
         if(experiment.getTitle().equals("")) {
             screenName=experiment.getScreenID();
         }
         else {
             screenName=experiment.getTitle();
         }
 
         String sessionFile=null;
         if(isEmailMandantory) {
             File sessionFileObj=createSessionFile();
             sessionFile = sessionFileObj.getAbsolutePath();
         }
 
          //start cellHTS2 analysis in interactive mode with progress bar and showing current step
             resultPage.putAll(jobNameDir,screenName,emailAddress,
                    annotFile, descriptionFile, plateListFile, plateConfFile, screenLogFile,sessionFile,
                    channelLabel1, channelLabel2,
                    channel, normalTypes, normalScaling, resultsScaling, sumRep, logTransform,viabilityChannel,viabilityFunction);
             return resultPage;
         
     }
 
 
     /**
      *
      *   start a new analysis...therefore most of the page obj have to be killed
      *
      */
     public void onActionFromNewAnalysis() {
         //reset everything and restart it
         notNewRun = false;
     }
 
 
     /**
      *
      *  when we are want to save the whole session
      *
      * @return
      */
     public StreamResponse onActionFromSaveSession() {
         
 
         //first create/overwrite all the files which we also would when starting a new job
         //these files will be placed into the zip file in the next step
 
         String[]errorMsgArr=new String[1];
         errorNextLink=generateCellHTS2Files(errorMsgArr);
         nextLinkErrorMsg = errorMsgArr[0];
         if (errorNextLink) {
             return null;
         }
         File zipFile = createSessionFile();
 
         
         try {
             //make a stream to sent it to browser
             FileInputStream iStream = new FileInputStream(zipFile);
             return new ZIPStreamResponse(iStream, jobNameDir.getName() + "_PERSISTENT");
 
         } catch (Exception e) {
             return null;
         }
     }
 
     /**
      *
      *  create a session file
      *
      * @return the session file
      */
     public File createSessionFile() {
         String filename = jobNameDir.getAbsolutePath()+File.separator +jobNameDir.getName()+ "_PERSISTENT.PST";
         String zipFile = jobNameDir.getAbsolutePath()+File.separator + jobNameDir.getName() +"_PERSISTENT.ZIP";
 
 //make a new Persistant obj
         PersistentCellHTS2 persistentObj = new PersistentCellHTS2(
                 experiment,
                 activatedPages,
                 descriptionMap,
                 posWellAmount,
                 negWellAmount,
                 descriptionFile,
                 plateListFile,
                 annotFile,
                 plateConfFile,
                 isDualChannel,
                 channelLabel1,
                 channelLabel2,
                 plateFormat,
                 channel,
                 plate,
                 normalTypes,
                 logTransform,
                 normalScaling,
                 resultsScaling,
                 sumRep,
                 noErrorUploadFile,
                 noErrorPlateConfFile,
                 noErrorAnnotFile,
                 noErrorDescriptionFile,
                 validateErrorFileView,
                 errorDatafileMsg,
                 errorPlateconfFileMsg,
                 errorAnnotFileMsg,
                 errorDescriptionFileMsg,
                 enableWizardsNewPage,
                 parseFileParams,
                 backDisable,
                 nextDisable,
                 currentPagePointer,
                 errorNextLink,
                 nextLinkErrorMsg,
                // uploadDir,
                // downloadDir,
                // jobName,
                 jobNameDir,
                 dataFileList,
                 excludeFilesFromParsing,
                 screenLogFile,
                 clickedWellsAndPlates,
                 viabilityChannel,
                 emailAddress,
                 isEmailMandantory,
                 fixRegExp,
                 viabilityFunction
         );
         //stream the persistent obj
         FileOutputStream fos = null;
         ObjectOutputStream out = null;
         try {
             fos = new FileOutputStream(filename);
             out = new ObjectOutputStream(fos);
             out.writeObject(persistentObj);
             out.close();
         }
         catch (IOException ex) {
             ex.printStackTrace();
         }
 
         ArrayList<String> fileNames = new ArrayList<String>();
         ArrayList<String> filePathes = new ArrayList<String>();
 
         fileNames.add(filename);
         filePathes.add(null);//no inner zip directories possible for filename[0]
         //if we want to make an already uploaded files independant session, we will have to
         //add all the uploaded files to the zip file and generate an new job on upload session file later
 
         //add all the uploaded datafiles
         for(String file : dataFileList.keySet()) {
             fileNames.add(jobNameDir.getAbsolutePath()+File.separator+file);
             filePathes.add(null);
         }
         String []tmpArr = {plateListFile,plateConfFile,screenLogFile,descriptionFile};
         for(String iterateVal: tmpArr ) {
            fileNames.add(jobNameDir.getAbsolutePath()+File.separator+iterateVal);
 
             filePathes.add(null);
         }
 
         //annot file is optional
         if(annotFile!=null) {
             fileNames.add(jobNameDir.getAbsolutePath()+File.separator+annotFile);
             filePathes.add(null);
         }
 
 
         String fileNamesArr[] = new String[fileNames.size()];
         String filePathesArr[] = new String[filePathes.size()];
 
         fileNames.toArray(fileNamesArr);
         filePathes.toArray(filePathesArr);
         ShellEnvironment.zipFiles(fileNamesArr, filePathesArr, zipFile);
         return new File(zipFile);
     }
 
     public Object onActionFromShowAdvancedFileImporter(){
        advancedFileImporter.setInit(false);
        advancedFileImporter.setUploadPath(jobNameDir.getAbsolutePath());
        advancedFileImporter.setContainsMultiChannelData(isDualChannel);
        return advancedFileImporter;
     }
 
 
     /**
      *
      *  event handler methods for radio buttons...onEvent mixin style :-)
      *
      */
     @OnEvent(component = "normalMedian", value = "click")
     public JSONObject setNormalMedian() {
         if(!request.isXHR()) {
             throw new TapestryException("your browser cannot handle AJAX XHR requests. Talk to your ISP to configure the proxy accordingly",null);
         }
         normalTypes = NormalizationTypes.median;
         return new JSONObject().put("dummy", "dummy");
     }
     /**
      *
      *  event handler methods for radio buttons...onEvent mixin style :-)
      *
      */
     @OnEvent(component = "normalShort", value = "click")
     public JSONObject setNormalShort() {
         if(!request.isXHR()) {
             throw new TapestryException("your browser cannot handle AJAX XHR requests. Talk to your ISP to configure the proxy accordingly",null);
         }
         normalTypes = NormalizationTypes.shorth;
         return new JSONObject().put("dummy", "dummy");
     }
     /**
      *
      *  event handler methods for radio buttons...onEvent mixin style :-)
      *
      */
     @OnEvent(component = "normalMean", value = "click")
     public JSONObject setNormalMean() {
         if(!request.isXHR()) {
             throw new TapestryException("your browser cannot handle AJAX XHR requests. Talk to your ISP to configure the proxy accordingly",null);
         }
         normalTypes = NormalizationTypes.mean;
         return new JSONObject().put("dummy", "dummy");
     }
     /**
      *
      *  event handler methods for radio buttons...onEvent mixin style :-)
      *
      */
     @OnEvent(component = "normalNegatives", value = "click")
     public JSONObject setNormalNegatives() {
         if(!request.isXHR()) {
             throw new TapestryException("your browser cannot handle AJAX XHR requests. Talk to your ISP to configure the proxy accordingly",null);
         }
         normalTypes = NormalizationTypes.negatives;
         return new JSONObject().put("dummy", "dummy");
     }
     /**
      *
      *  event handler methods for radio buttons...onEvent mixin style :-)
      *
      */
     @OnEvent(component = "normalPOC", value = "click")
     public JSONObject setNormalPOC() {
         if(!request.isXHR()) {
             throw new TapestryException("your browser cannot handle AJAX XHR requests. Talk to your ISP to configure the proxy accordingly",null);
         }
         normalTypes = NormalizationTypes.POC;
         return new JSONObject().put("dummy", "dummy");
     }
     /**
      *
      *  event handler methods for radio buttons...onEvent mixin style :-)
      *
      */
     @OnEvent(component = "normalNPI", value = "click")
     public JSONObject setNormalNPI() {
         if(!request.isXHR()) {
             throw new TapestryException("your browser cannot handle AJAX XHR requests. Talk to your ISP to configure the proxy accordingly",null);
         }
         normalTypes = NormalizationTypes.NPI;
         return new JSONObject().put("dummy", "dummy");
     }
     /**
      *
      *  event handler methods for radio buttons...onEvent mixin style :-)
      *
      */
     @OnEvent(component = "normalBScore", value = "click")
     public JSONObject setNormalBScore() {
         if(!request.isXHR()) {
             throw new TapestryException("your browser cannot handle AJAX XHR requests. Talk to your ISP to configure the proxy accordingly",null);
         }
         normalTypes = NormalizationTypes.Bscore;
         return new JSONObject().put("dummy", "dummy");
     }
     /**
      *
      *  event handler methods for radio buttons...onEvent mixin style :-)
      *
      */
     @OnEvent(component = "normalLocfit", value = "click")
     public JSONObject setNormalLocfit() {
         if(!request.isXHR()) {
             throw new TapestryException("your browser cannot handle AJAX XHR requests. Talk to your ISP to configure the proxy accordingly",null);
         }
         normalTypes = NormalizationTypes.locfit;
         return new JSONObject().put("dummy", "dummy");
     }
     /**
      *
      *  event handler methods for radio buttons...onEvent mixin style :-)
      *
      */
     @OnEvent(component = "normalLoess", value = "click")
     public JSONObject setNormalLoess() {
         if(!request.isXHR()) {
             throw new TapestryException("your browser cannot handle AJAX XHR requests. Talk to your ISP to configure the proxy accordingly",null);
         }
         normalTypes = NormalizationTypes.loess;
 
         return new JSONObject().put("dummy", "dummy");
     }
     /**
      *
      *  event handler methods for radio buttons...onEvent mixin style :-)
      *
      */
     @OnEvent(component = "logYes", value = "click")
     public JSONObject setLogYes() {
         if(!request.isXHR()) {
             throw new TapestryException("your browser cannot handle AJAX XHR requests. Talk to your ISP to configure the proxy accordingly",null);
         }
         logTransform = LogTransform.YES;
         return new JSONObject().put("dummy", "dummy");
     }
     /**
      *
      *  event handler methods for radio buttons...onEvent mixin style :-)
      *
      */
     @OnEvent(component = "logNo", value = "click")
     public JSONObject setLogNo() {
         if(!request.isXHR()) {
             throw new TapestryException("your browser cannot handle AJAX XHR requests. Talk to your ISP to configure the proxy accordingly",null);
         }
         logTransform = LogTransform.NO;
         return new JSONObject().put("dummy", "dummy");
     }
     /**
      *
      *  event handler methods for radio buttons...onEvent mixin style :-)
      *
      */
     @OnEvent(component = "scaleAdditive", value = "click")
     public JSONObject setScaleAdditive() {
         if(!request.isXHR()) {
             throw new TapestryException("your browser cannot handle AJAX XHR requests. Talk to your ISP to configure the proxy accordingly",null);
         }
         normalScaling = NormalScalingTypes.additive;
         return new JSONObject().put("dummy", "dummy");
     }
     /**
      *
      *  event handler methods for radio buttons...onEvent mixin style :-)
      *
      */
     @OnEvent(component = "scaleMultiplicative", value = "click")
     public JSONObject setScaleMultiplicative() {
         if(!request.isXHR()) {
             throw new TapestryException("your browser cannot handle AJAX XHR requests. Talk to your ISP to configure the proxy accordingly",null);
         }
         normalScaling = NormalScalingTypes.multiplicative;
         return new JSONObject().put("dummy", "dummy");
     }
     /**
      *
      *  event handler methods for radio buttons...onEvent mixin style :-)
      *
      */
     @OnEvent(component = "resultScalingNo", value = "click")
     public JSONObject setResultScalingNo() {
         if(!request.isXHR()) {
             throw new TapestryException("your browser cannot handle AJAX XHR requests. Talk to your ISP to configure the proxy accordingly",null);
         }
         resultsScaling = ResultsScalingTypes.none;
         return new JSONObject().put("dummy", "dummy");
     }
     /**
      *
      *  event handler methods for radio buttons...onEvent mixin style :-)
      *
      */
     @OnEvent(component = "resultScalingPlate", value = "click")
     public JSONObject setResultScalingPlate() {
         if(!request.isXHR()) {
             throw new TapestryException("your browser cannot handle AJAX XHR requests. Talk to your ISP to configure the proxy accordingly",null);
         }
         resultsScaling = ResultsScalingTypes.byPlate;
         return new JSONObject().put("dummy", "dummy");
     }
 
     @OnEvent(component = "resultScalingBatch", value = "click")
     public JSONObject setResultScalingBatch() {
         if(!request.isXHR()) {
             throw new TapestryException("your browser cannot handle AJAX XHR requests. Talk to your ISP to configure the proxy accordingly",null);
         }
         resultsScaling = ResultsScalingTypes.byBatch;
         return new JSONObject().put("dummy", "dummy");
     }
     /**
      *
      *  event handler methods for radio buttons...onEvent mixin style :-)
      *
      */
     @OnEvent(component = "resultScalingExperiment", value = "click")
     public JSONObject setResultScalingExperiment() {
         if(!request.isXHR()) {
             throw new TapestryException("your browser cannot handle AJAX XHR requests. Talk to your ISP to configure the proxy accordingly",null);
         }
         resultsScaling = ResultsScalingTypes.byExperiment;
         return new JSONObject().put("dummy", "dummy");
     }
 
     /**
      *
      *  event handler methods for radio buttons...onEvent mixin style :-)
      *
      */
     @OnEvent(component = "sumMean", value = "click")
     public JSONObject setSumMean() {
         if(!request.isXHR()) {
             throw new TapestryException("your browser cannot handle AJAX XHR requests. Talk to your ISP to configure the proxy accordingly",null);
         }
         sumRep = SummerizeReplicates.mean;
         return new JSONObject().put("dummy", "dummy");
     }
     /**
      *
      *  event handler methods for radio buttons...onEvent mixin style :-)
      *
      */
     @OnEvent(component = "sumMedian", value = "click")
     public JSONObject setSumMedian () {
         if(!request.isXHR()) {
             throw new TapestryException("your browser cannot handle AJAX XHR requests. Talk to your ISP to configure the proxy accordingly",null);
         }
         sumRep = SummerizeReplicates.median;
         return new JSONObject().put("dummy", "dummy");
     }
     /**
      *
      *  event handler methods for radio buttons...onEvent mixin style :-)
      *
      */
     @OnEvent(component = "sumMax", value = "click")
     public JSONObject setSumMax() {
         if(!request.isXHR()) {
             throw new TapestryException("your browser cannot handle AJAX XHR requests. Talk to your ISP to configure the proxy accordingly",null);
         }
         sumRep = SummerizeReplicates.max;
         return new JSONObject().put("dummy", "dummy");
     }
     /**
      *
      *  event handler methods for radio buttons...onEvent mixin style :-)
      *
      */
     @OnEvent(component = "sumMin", value = "click")
     public JSONObject setSumMin() {
         if(!request.isXHR()) {
             throw new TapestryException("your browser cannot handle AJAX XHR requests. Talk to your ISP to configure the proxy accordingly",null);
         }
         sumRep = SummerizeReplicates.min;
         return new JSONObject().put("dummy", "dummy");
     }
 
 
     /**
      *
      *  event handler methods for radio buttons...onEvent mixin style :-)
      *
      */
     @OnEvent(component = "sumClosest", value = "click")
     public JSONObject setSumClosest() {
         if(!request.isXHR()) {
             throw new TapestryException("your browser cannot handle AJAX XHR requests. Talk to your ISP to configure the proxy accordingly",null);
         }
         sumRep = SummerizeReplicates.closestToZero;
         return new JSONObject().put("dummy", "dummy");
     }
     /**
      *
      *  event handler methods for radio buttons...onEvent mixin style :-)
      *
      */
     @OnEvent(component = "sumFurthest", value = "click")
     public JSONObject setSumFurthest() {
         if(!request.isXHR()) {
             throw new TapestryException("your browser cannot handle AJAX XHR requests. Talk to your ISP to configure the proxy accordingly",null);
         }
         sumRep = SummerizeReplicates.furthestFromZero;
         return new JSONObject().put("dummy", "dummy");
     }
 
     /**
      *   set back to the javascript
      *
      * @return a JSONObj
      */
     @OnEvent(component = "viabilityChannelYes", value = "click")
     public JSONObject setViabilityChannelYes() {
         if(!request.isXHR()) {
             throw new TapestryException("your browser cannot handle AJAX XHR requests. Talk to your ISP to configure the proxy accordingly",null);
         }
         viabilityChannel = ViabilityChannel.YES;
 
         JSONObject json = new JSONObject();
         json.put("viabilityChannel","true");
         return json;
 
     }
     /**
      *   set back to the javascript
      *
      * @return a JSONObj
      */
     @OnEvent(component = "viabilityChannelNo", value = "click")
     public JSONObject setViabilityChannelNo() {
         if(!request.isXHR()) {
             throw new TapestryException("your browser cannot handle AJAX XHR requests. Talk to your ISP to configure the proxy accordingly",null);
         }
         viabilityChannel = ViabilityChannel.NO;
         JSONObject json = new JSONObject();
         json.put("viabilityChannel","false");
         return json;
     }
 
 
 
     /**
      *
      * this is the model for the description editor
      *
      * @return
      */
     public BeanModel getModel
             () {
         BeanModel model = beanModelSource.create(Experiment.class, false, resc.getMessages());
         //if we are using the external version we will need the title instead of the jobID
         if(msg.get("result-type").equals("email")) {
             model.exclude("screenID");
         }
         else {
         //if we are using the internal version we dont need the title field because we will use      
             model.exclude("title");
         }
         return model;
 
     }
     public String getChannelLabel1
             () {
         return channelLabel1;
     }
 
     public void setChannelLabel1
             (String
                     channelLabel1) {
         this.channelLabel1 = channelLabel1;
     }
 
     public String getChannelLabel2
             () {
         return channelLabel2;
     }
 
     public void setChannelLabel2
             (String
                     channelLabel2) {
         this.channelLabel2 = channelLabel2;
     }
 
 
     public ChannelTypes getChannel
             () {
         return channel;
     }
 
     public void setChannel
             (ChannelTypes
                     channel) {
         this.channel = channel;
     }
 
 
     public PlateTypes getPlate
             () {
         return plate;
     }
 
     public void setPlate
             (PlateTypes
                     plate) {
         this.plate = plate;
     }
 
     public int getPlateFormat
             () {
         return plateFormat;
     }
 
     public void setPlateFormat
             (
                     int plateFormat) {
         this.plateFormat = plateFormat;
     }
 
     public ArrayList<Plate> getClickedWellsAndPlates() {
         return clickedWellsAndPlates;
     }
 
     public void setClickedWellsAndPlates(ArrayList<Plate> clickedWellsAndPlates) {
         this.clickedWellsAndPlates = clickedWellsAndPlates;
     }
 
     public NormalizationTypes getNormalTypes
             () {
         return normalTypes;
     }
 
     public String getAnnotFile
             () {
         return annotFile;
     }
 
     public void setAnnotFile
             (String
                     annotFile) {
         this.annotFile = annotFile;
     }
 
     public String getDescriptionFile
             () {
         return descriptionFile;
     }
 
     public void setDescriptionFile
             (String
                     descriptionFile) {
         this.descriptionFile = descriptionFile;
     }
 
     public void setNormalTypes
             (NormalizationTypes
                     normalTypes) {
         this.normalTypes = normalTypes;
     }
 
     public Pattern getAnnotBodyPattern
             () {
         return annotBodyPattern;
     }
 
     public void setAnnotBodyPattern
             (Pattern
                     annotBodyPattern) {
         this.annotBodyPattern = annotBodyPattern;
     }
 
     public Pattern getAnnotHeaderPattern
             () {
         return annotHeaderPattern;
     }
 
     public void setAnnotHeaderPattern
             (Pattern
                     annotHeaderPattern) {
         this.annotHeaderPattern = annotHeaderPattern;
     }
 
     public Pattern getDescriptionBodyPattern
             () {
         return descriptionBodyPattern;
     }
 
     public void setDescriptionBodyPattern
             (Pattern
                     descriptionBodyPattern) {
         this.descriptionBodyPattern = descriptionBodyPattern;
     }
 
     public Pattern getDescriptionHeaderPattern
             () {
         return descriptionHeaderPattern;
     }
 
     public void setDescriptionHeaderPattern
             (Pattern
                     descriptionHeaderPattern) {
         this.descriptionHeaderPattern = descriptionHeaderPattern;
     }//methods for radio button normalization menue
 
     public NormalizationTypes getNormalMedian
             () {
         return NormalizationTypes.median;
     }
 
     public NormalizationTypes getNormalShort
             () {
         return NormalizationTypes.shorth;
     }
 
     public NormalizationTypes getNormalMean
             () {
         return NormalizationTypes.mean;
     }
 
     public NormalizationTypes getNormalNegatives
             () {
         return NormalizationTypes.negatives;
     }
 
     public NormalizationTypes getNormalNPI
             () {
         return NormalizationTypes.NPI;
     }
 
     public NormalizationTypes getNormalPOC
             () {
         return NormalizationTypes.POC;
     }
 
     public NormalizationTypes getNormalBScore
             () {
         return NormalizationTypes.Bscore;
     }
 
     public NormalizationTypes getNormalLoess
             () {
         return NormalizationTypes.loess;
     }
 
     public NormalizationTypes getNormalLocfit
             () {
         return NormalizationTypes.locfit;
     }
 
     public LogTransform getLogTransform
             () {
         return logTransform;
     }
 
     public void setLogTransform
             (LogTransform
                     logTransform) {
         this.logTransform = logTransform;
     }
 
     public LogTransform getLogYes
             () {
         return LogTransform.YES;
     }
 
     public LogTransform getLogNo
             () {
         return LogTransform.NO;
     }
 
     public NormalScalingTypes getNormalScaling
             () {
         return normalScaling;
     }
 
     public void setNormalScaling
             (NormalScalingTypes
                     normalScaling) {
         this.normalScaling = normalScaling;
     }
 
     public NormalScalingTypes getScaleAdditive
             () {
         return NormalScalingTypes.additive;
     }
 
     public NormalScalingTypes getScaleMultiplicative
             () {
         return NormalScalingTypes.multiplicative;
     }
 
     public ResultsScalingTypes getResultsScaling
             () {
         return resultsScaling;
     }
 
     public void setResultsScaling
             (ResultsScalingTypes
                     resultsScaling) {
         this.resultsScaling = resultsScaling;
     }
 
     public ResultsScalingTypes getResultScalingNo
             () {
         return ResultsScalingTypes.none;
     }
 
     public ResultsScalingTypes getResultScalingBatch
             () {
         return ResultsScalingTypes.byBatch;
     }
 
     public ResultsScalingTypes getResultScalingExperiment
             () {
         return ResultsScalingTypes.byExperiment;
     }
 
     public ResultsScalingTypes getResultScalingPlate
             () {
         return ResultsScalingTypes.byPlate;
     }
 
     public SummerizeReplicates getSumRep
             () {
         return sumRep;
     }
 
     public void setSumRep
             (SummerizeReplicates
                     sumRep) {
         this.sumRep = sumRep;
     }
 
     public SummerizeReplicates getSumMean
             () {
         return SummerizeReplicates.mean;
     }
 
     public SummerizeReplicates getSumClosest
             () {
         return SummerizeReplicates.closestToZero;
     }
 
     public SummerizeReplicates getSumFurthest
             () {
         return SummerizeReplicates.furthestFromZero;
     }
 
     public SummerizeReplicates getSumMax
             () {
         return SummerizeReplicates.max;
     }
 
     public SummerizeReplicates getSumMedian
             () {
         return SummerizeReplicates.median;
     }
 
     public SummerizeReplicates getSumMin
             () {
         return SummerizeReplicates.min;
     }
     public ViabilityChannel getViabilityChannelYes() {
        return ViabilityChannel.YES;
     }
     public ViabilityChannel getViabilityChannelNo() {
        return ViabilityChannel.NO;
     }
 
 
     public boolean getFalseBool
             () {
         return false;
     }
 
     public HashMap<String, DataFile> getDataFileList
             () {
         return dataFileList;
     }
 
     public void setDataFileList
             (HashMap<String, DataFile> dataFileList) {
         this.dataFileList = dataFileList;
     }
 
     public boolean getNoErrorUploadFile
             () {
         return noErrorUploadFile;
     }
 
     public void setNoErrorUploadFile
             (
                     boolean noErrorUploadFile) {
         this.noErrorUploadFile = noErrorUploadFile;
     }
 
     public String getErrorDatafileMsg
             () {
         return errorDatafileMsg;
     }
 
     public void setErrorDatafileMsg
             (String
                     errorDatafileMsg) {
         this.errorDatafileMsg = errorDatafileMsg;
     }
 
     public UploadedFile getUploadedDataFile
             () {
         return uploadedDataFile;
     }
 
     public void setUploadedDataFile
             (UploadedFile
                     uploadedDataFile) {
         this.uploadedDataFile = uploadedDataFile;
     }
 
     public UploadedFile getUploadedPlateConfigFile
             () {
         return uploadedPlateConfigFile;
     }
 
     public void setUploadedPlateConfigFile
             (UploadedFile
                     uploadedPlateConfigFile) {
         this.uploadedPlateConfigFile = uploadedPlateConfigFile;
     }
 
     public String getErrorPlateconfFileMsg
             () {
         return errorPlateconfFileMsg;
     }
 
     public void setErrorPlateconfFileMsg
             (String
                     errorPlateconfFileMsg) {
         this.errorPlateconfFileMsg = errorPlateconfFileMsg;
     }
 
     public boolean isNoErrorPlateConfFile
             () {
         return noErrorPlateConfFile;
     }
 
     public void setNoErrorPlateConfFile
             (
                     boolean noErrorPlateConfFile) {
         this.noErrorPlateConfFile = noErrorPlateConfFile;
     }
 
     public UploadedFile getUploadedAnnotFile
             () {
         return uploadedAnnotFile;
     }
 
     public void setUploadedAnnotFile
             (UploadedFile
                     uploadedAnnotFile) {
         this.uploadedAnnotFile = uploadedAnnotFile;
     }
 
     public UploadedFile getUploadedDescriptionFile
             () {
         return uploadedDescriptionFile;
     }
 
     public void setUploadedDescriptionFile
             (UploadedFile
                     uploadedDescriptionFile) {
         this.uploadedDescriptionFile = uploadedDescriptionFile;
     }
 
     public String getErrorAnnotFileMsg
             () {
         return errorAnnotFileMsg;
     }
 
     public void setErrorAnnotFileMsg
             (String
                     errorAnnotFileMsg) {
         this.errorAnnotFileMsg = errorAnnotFileMsg;
     }
 
     public String getErrorDescriptionFileMsg
             () {
         return errorDescriptionFileMsg;
     }
 
     public void setErrorDescriptionFileMsg
             (String
                     errorDescriptionFileMsg) {
         this.errorDescriptionFileMsg = errorDescriptionFileMsg;
     }
 
     public boolean isNoErrorAnnotFile
             () {
         return noErrorAnnotFile;
     }
 
     public void setNoErrorAnnotFile
             (
                     boolean noErrorAnnotFile) {
         this.noErrorAnnotFile = noErrorAnnotFile;
     }
 
     public boolean isNoErrorDescriptionFile
             () {
         return noErrorDescriptionFile;
     }
 
     public void setNoErrorDescriptionFile
             (
                     boolean noErrorDescriptionFile) {
         this.noErrorDescriptionFile = noErrorDescriptionFile;
     }
 
     /**
      *
      * check if all essential file parameters are set
      *
      * @return true if set, false if not set
      */
     private boolean isEssentialFileParamsSet
             () {
         boolean allAvailable = true;
         Iterator tempIterator = dataFileList.keySet().iterator();
 
         while (tempIterator.hasNext()) {
             String filename = (String) tempIterator.next();
             DataFile dataFile = dataFileList.get(filename);
             if (dataFile.getPlateNumber() == null || dataFile.getReplicate() == null) {
                 allAvailable = false;
             }
         }
         return allAvailable;
     }
 
     public boolean isValidateErrorFileView
             () {
         return validateErrorFileView;
     }
 
     public void setValidateErrorFileView
             (
                     boolean validateErrorFileView) {
         this.validateErrorFileView = validateErrorFileView;
     }
 
     public Results getResultPage
             () {
         return resultPage;
     }
 
     public void setResultPage
             (Results
                     resultPage) {
         this.resultPage = resultPage;
     }
 
     
 
     public String getPlateListFile
             () {
         return plateListFile;
     }
 
     public void setPlateListFile
             (String
                     plateListFile) {
         this.plateListFile = plateListFile;
     }
 
     public String getPlateConfFile
             () {
         return plateConfFile;
     }
 
     public void setPlateConfFile
             (String
                     plateConfFile) {
         this.plateConfFile = plateConfFile;
     }
 
     public int convertPlateTypesToInt
             (PlateTypes
                     plate) {
 
         String plateFormat = plate.toString();
 
         int thisPlateFormat = Integer.parseInt(plateFormat.substring(1));
 
         return thisPlateFormat;
     }
 
     /**
      *
      * the plates are called 96,384 etc. but enums cant start with a number, only with a letter so we have to convert them
      *
      * @param plateNum
      * @return
      */
     public PlateTypes convertIntToPlateTypes
             (
                     int plateNum) {
         PlateTypes thisType = PlateTypes.valueOf("P" + plateNum);
         if (thisType == null) {
             TapestryException missingClickedWellMap = new TapestryException("unknown plate format P" + plateNum, null);
             throw missingClickedWellMap;
         }
         return thisType;
     }
 
     public String getEnableWizardsNewPage
             () {
         return enableWizardsNewPage;
     }
 
     public void setEnableWizardsNewPage
             (String
                     enableWizardsNewPage) {
         this.enableWizardsNewPage = enableWizardsNewPage;
     }
 
     public boolean getHasPlateFormat
             () {
         if (plateFormat == null) {
             return false;
         } else {
             return true;
         }
     }
 
     public boolean isParseFileParams
             () {
         return parseFileParams;
     }
 
     public void setParseFileParams
             (
                     boolean parseFileParams) {
         this.parseFileParams = parseFileParams;
     }
 
     /**
      *
      * this places a javascript into our page to control enabling and disbling of "back" and "forward" buttons
      * ...This is deprecated
      *
      * @param writer
      */
     void afterRender(MarkupWriter writer) {
 
         //after rendering we will start a javascript method which will
         //enable and disable all divs which are not the current one
         JSONObject jsonObj = new JSONObject();
         //first get all available divs which we have and will send to javascript as well as the one
         //which we want to enable
         for (int i = 0; i < activatedPages.size(); i++) {
             //for (String entry : availableDIVs) {
             jsonObj.put("" + i++, "step" + i + "DIV");
         }
         //the div you want to enable and the divs you want to disable
         pageRenderSupport.addScript("new DIVEnabler('step%sDIV','%s');", currentPagePointer, jsonObj.toString());
 
 
     }
 
     /**
      *
      * this method initalizes a new run and cleans all the setted parameters etc. This must be executed everytime
      * we make a new analtsis
      *
      */
     public void initNewRun() {
         //create a new experiment obj
             experiment.clear();
             activatedPages.clear();
             descriptionMap.clear();
             excludeFilesFromParsing.clear();
             clickedWellsAndPlates.clear();
             currentPagePointer = 1;
 
 //activate first page
             activatedPages.put(currentPagePointer, false);
             posWellAmount = 0;
             negWellAmount = 0;
             annotFile = null;
             viabilityFunction=null;
             descriptionFile = null;
             plateListFile = null;
             plateConfFile = null;
 
 
 
     uploadedPlatelistFile=null;
     uploadedPlateConfigFile=null;
     uploadedScreenlogFile=null;
     uploadedDataFile=null;
     uploadedAnnotFile=null;
     uploadedDescriptionFile=null;
     uploadedSessionFile=null;
 
 
             emailAddress=null;
 
             isDualChannel = false;
 
             channelLabel1 = "FLuc";
             channelLabel2 = "RLuc";
 
             dataFileList.clear();
             plateFormat = null;
             channel = null;
             plate = null;
 
             normalTypes = NormalizationTypes.median;
             logTransform = LogTransform.NO;
             normalScaling = NormalScalingTypes.additive;
             resultsScaling = ResultsScalingTypes.none;
             sumRep = SummerizeReplicates.mean;
 
             noErrorPlateConfFile = false;
             noErrorAnnotFile = false;
             noErrorDescriptionFile = false;
             noErrorUploadFile = false;
             validateErrorFileView = false;
             errorPlatelistFileUpload=false;
             noErrorScreenlogFile=true;
             this.errorSessionFileUpload=false;
 
 
             errorDatafileMsg = ""; //we need this for the first run to not show msg error found in datafile upload
             errorPlateconfFileMsg = "";
             errorAnnotFileMsg = "";
             errorDescriptionFileMsg = "";
             errorPlatelistFileMsg="";
             errorScreenlogFileMsg="";
             this.sessionFileUploadErrorMsg="";
             
 
             //TODO:do we still need this variable?
             enableWizardsNewPage = "none";
             parseFileParams = true;
             backDisable = true;
             nextDisable = true;
             errorNextLink = false;
             nextLinkErrorMsg = "";
             fixRegExp="";
             
 
             jobNameDir=null;
         //restart the advanced file importer
             advancedFileImporter.setInit(false);
     }
 
     public void setDatafilesFromAdvancedFileImporter(ArrayList<File> outputFiles) {
         datafilesFromAdvancedFileImporter=outputFiles;
     }
 
     public Layout getLayout
             () {
         return layout;
     }
 
     public void setLayout
             (Layout
                     layout) {
         this.layout = layout;
     }
 
     public Experiment getExperiment
             () {
         return experiment;
     }
 
     public void setExperiment
             (Experiment
                     experiment) {
         this.experiment = experiment;
     }
 
     public String getBackLinkName
             () {
         return backLinkName;
     }
 
 
     public String getNextLinkName
             () {
         return nextLinkName;
     }
 
     public boolean isBackDisable
             () {
         return backDisable;
     }
 
     public void setBackDisable
             (
                     boolean backDisable) {
         this.backDisable = backDisable;
     }
 
     public boolean isNextDisable
             () {
         return nextDisable;
     }
 
     public void setNextDisable
             (
                     boolean nextDisable) {
         this.nextDisable = nextDisable;
     }
 
     //getter for the tml
     public CellHTS2 getThis
             () {
         return this;
     }
 
     public boolean isErrorNextLink
             () {
         return errorNextLink;
     }
 
     public void setErrorNextLink
             (
                     boolean errorNextLink) {
         this.errorNextLink = errorNextLink;
     }
 
     public String getNextLinkErrorMsg
             () {
         return nextLinkErrorMsg;
     }
 
     public void setNextLinkErrorMsg
             (String
                     nextLinkErrorMsg) {
         this.nextLinkErrorMsg = nextLinkErrorMsg;
     }
 
     public UploadedFile getUploadedSessionFile() {
         return uploadedSessionFile;
     }
 
     public void setUploadedSessionFile(UploadedFile uploadedSessionFile) {
         this.uploadedSessionFile = uploadedSessionFile;
     }
 
     public HashSet<String> getExcludeFilesFromParsing() {
         return excludeFilesFromParsing;
     }
 
     public void setExcludeFilesFromParsing(HashSet<String> excludeFilesFromParsing) {
         this.excludeFilesFromParsing = excludeFilesFromParsing;
     }
     //TODO: there are so much iterating over the clickedWellsAndPlates array we should make an own class out of it with fancy iterating and finding and manipulating methods
     /**
      *
      * get the amount of a welltype e.g. pos, neg or other for all the plates of your platese
      *
      * @param wellType   a type e.g. "pos"
      * @return the number
      */
     public int getWellTypeAmountOfAllPlates(String wellType) {
         int returnCnt=0;
         for(int i=0; i < clickedWellsAndPlates.size(); i++) {
             HashMap<String,String> wellsArray =clickedWellsAndPlates.get(i).getWellsArray();
             Iterator wellIterator = wellsArray.keySet().iterator();
             while(wellIterator.hasNext()) {
                 String wellID = (String)wellIterator.next();
                 String thisWellType = wellsArray.get(wellID);
                 if(thisWellType !=null) {
                     if(thisWellType.equals(wellType)) {
                         returnCnt++;
                     }
                 }
             }
 
         }
         return returnCnt;
     }
 
     /**
      *
      * this counts all the setted wells on all the plates
      *
      * @return amount of setted wells
      */
     public int getPosNegWellAmountOfAllPlates() {
         int returnCnt=0;
         for(int i=0; i < clickedWellsAndPlates.size(); i++) {
             HashMap<String,String> wellsArray =clickedWellsAndPlates.get(i).getWellsArray();
             Iterator wellIterator = wellsArray.keySet().iterator();
             while(wellIterator.hasNext()) {
                 String wellID = (String)wellIterator.next();
                 String thisWellType = wellsArray.get(wellID);
                 if(thisWellType !=null&& wellID!=null) {
                     if(thisWellType.equals("pos")||thisWellType.equals("neg")) {
                         returnCnt++;
                      //   System.out.println("this is the result:"+returnCnt+":"+wellID+" "+thisWellType+" plate "+i);
                     }
 
                  }
             }
 
         }
         return returnCnt;
     }
     public void activatedFromOtherPage(String type) {
         activatedFromPage=type;
     }
 
     public boolean getErrorSessionFileUpload() {
         return errorSessionFileUpload;
     }
 
     public void setErrorSessionFileUpload(boolean errorSessionFileUpload) {
         this.errorSessionFileUpload = errorSessionFileUpload;
     }
 
     public String getSessionFileUploadErrorMsg() {
         return sessionFileUploadErrorMsg;
     }
 
     public void setSessionFileUploadErrorMsg(String sessionFileUploadErrorMsg) {
         this.sessionFileUploadErrorMsg = sessionFileUploadErrorMsg;
     }
 
     public ViabilityChannel getViabilityChannel() {
         return viabilityChannel;
     }
 
     public void setViabilityChannel(ViabilityChannel viabilityChannel) {
         this.viabilityChannel = viabilityChannel;
     }
 
     public String getFixRegExp() {
         return fixRegExp;
     }
 
     public void setFixRegExp(String fixRegExp) {
         this.fixRegExp = fixRegExp;
     }//this will initaliz the plateNWellMap from the uploaded data files
     //this step is important before accessing/writing to the plateNWell data
 
     public UploadedFile getUploadedScreenlogFile() {
         return uploadedScreenlogFile;
     }
 
     public void setUploadedScreenlogFile(UploadedFile uploadedScreenlogFile) {
         this.uploadedScreenlogFile = uploadedScreenlogFile;
     }
 
     public boolean isNoErrorScreenlogFile() {
         return noErrorScreenlogFile;
     }
 
     public void setNoErrorScreenlogFile(boolean noErrorScreenlogFile) {
         this.noErrorScreenlogFile = noErrorScreenlogFile;
     }
 
     public String getErrorScreenlogFileMsg() {
         return errorScreenlogFileMsg;
     }
 
     public void setErrorScreenlogFileMsg(String errorScreenlogFileMsg) {
         this.errorScreenlogFileMsg = errorScreenlogFileMsg;
     }
 
     public String getEmailAddress() {
         return emailAddress;
     }
     public boolean getIsEmailMandantory() {
         return isEmailMandantory;
     }
 
     /**
      *
      *  this initalizes a new plate and well map object (its called clickedwellsandplates)
      *  through the submitted datafilelist you uploaded
      *
      */
     public void initalizePlateNWellMap() {
 
         int replicatesAmount = 0;
         int platesAmount = 0;
         //sort out the plates and samples for contaminated wells
 
         //sort keys
         ArrayList sortedKeys = new ArrayList();
         sortedKeys.addAll(dataFileList.keySet());
         Collections.sort(sortedKeys);
 
         Iterator dataFileIterator = sortedKeys.iterator();
 
         //this stores all the available plate and replicate numbers
         TreeMap<Integer,TreeSet<Integer>> uniquePlateNRepNumbers = new TreeMap<Integer,TreeSet<Integer>>();
 
 
         while (dataFileIterator.hasNext()) {
             String fileName = (String) dataFileIterator.next();
             DataFile dataFile = (DataFile) dataFileList.get(fileName);
             System.out.println(dataFile.toString());
             Integer plateNo = dataFile.getPlateNumber();
             Integer replicateNo = dataFile.getReplicate();
 
             if(!uniquePlateNRepNumbers.containsKey(plateNo)) {
                 uniquePlateNRepNumbers.put(plateNo,new TreeSet<Integer>());
             }
             uniquePlateNRepNumbers.get(plateNo).add(replicateNo);
 
             
         }
         
         //so now initialize plates (every plate is a combination of a plate and a replicate)
         // with the same order in the array as in the dropdown list
         // and wellmaps
 
         //when we load a session we will go into here but already have defined wells and plates
         //so dont do this twice
         //this is some bruteforce approach and is needed because when we are starting an old session
         //we will get in here too but have already defined plate config layout so we want to keep this information in that case
         //BACKUP
         HashMap<Integer, HashMap<Integer, HashMap<String, String>>> definedWellsBefore = new HashMap<Integer, HashMap<Integer, HashMap<String, String>>>();
         for (Plate plate : clickedWellsAndPlates) {
                 Integer plateNo = plate.getPlateNum();
                 Integer repliNo = plate.getReplicateNum();
                 HashMap<String,String> tempWells = plate.getWellsArray();
                 if(!definedWellsBefore.containsKey(plateNo)) {
                   definedWellsBefore.put(plateNo,new HashMap<Integer, HashMap<String, String>>());
                 }
                 if(!definedWellsBefore.get(plateNo).containsKey(repliNo)) {
                    definedWellsBefore.get(plateNo).put(repliNo,new HashMap<String, String>());
                 }
             //finally store it
                 definedWellsBefore.get(plateNo).put(repliNo,tempWells);
 
         }
 
         //erase it
         clickedWellsAndPlates.clear();
         //first after clearage...we need an all plate again
         clickedWellsAndPlates.add(0,new Plate(0, 0, 0));
         int jsID = 0;
             for (int plateNo : uniquePlateNRepNumbers.keySet()) {
                  for(int repNo : uniquePlateNRepNumbers.get(plateNo)) {
                       
                     jsID++;
 
                     Plate tempPlate = new Plate(jsID, plateNo,repNo);
                     clickedWellsAndPlates.add(tempPlate);   //plateNumber is the javascript access number +1
 
                     //if we already have wells defined add them here
                     if(definedWellsBefore.containsKey(plateNo)) {
                         if(definedWellsBefore.get(plateNo).containsKey(repNo)) {
                             tempPlate.setWellsArray(definedWellsBefore.get(plateNo).get(repNo));
                             
                         }
                     }
                      
 
                  }
 
             }
             
        //next we will recalculate the "all" plate
        //this means collect all wells on the all plate which are on any other plate
        rebuildZeroPlate();
 
     }
 
     /**
      *
      *  create or update the description file...if we update we do this by using the experiment obj
      *
      * @return
      */
     public String createOrupdateDescriptionFile() {
         if (descriptionFile == null) {
             descriptionFile = "Description.txt";
             FileCreator.createDescriptionFile(experiment, jobNameDir.getAbsolutePath() + File.separator + descriptionFile);
         }
         else {
 
             FileCreator.editDescriptionFile(experiment, jobNameDir.getAbsolutePath() + File.separator + descriptionFile);
         }
         return descriptionFile;
     }
 
     /**
      *
      *  generate all necessary inputfiles we are created through the interface
      *  returns if generating all these files was successful and through a call by reference parameter the possible error message.
      *
      * @param errorMsg
      * @return
      */
     public boolean generateCellHTS2Files(String []errorMsg) {
         errorMsg[0]="";
         boolean okFile;
         boolean finalTestFail=false;
 
          //step2 check and create a platelist file
         String newPListFile = "Platelist.txt";
         String newPListFilePath = jobNameDir.getAbsolutePath() + File.separator + "Platelist.txt";
         //TODO:check if dual channel we need channel info in the grid to proceed
         File pListTempFile = new File(newPListFilePath);
         okFile = FileCreator.createPlatelistFile(dataFileList, pListTempFile);
 
         if (!okFile) {
             errorMsg[0] += "Error: Step2: could not create PlateList.txt file. Most likely:<br/> you forgot to set plate Number or replicate number"
                     + " for each plate file or upload no datafile at all<br/>";
             finalTestFail = true;
         } else {
             plateListFile = newPListFile;
         }
         //Step3 create a plate configuration file
         String newPConfFile = "PlateConfig.txt";
         String newPConfFilePath = jobNameDir.getAbsolutePath() + File.separator + newPConfFile;
         File pConfTempFile = new File(newPConfFilePath);
 
         //create screenlog file as well
         String newScreenlogFile = "Screenlog.txt";
         String newScreenlogFilePath = jobNameDir.getAbsolutePath() + File.separator + newScreenlogFile;
         File pScreenlogFile = new File(newScreenlogFilePath);
 
 
         okFile = FileCreator.createPlateConfigFile(newPConfFilePath, newScreenlogFilePath, clickedWellsAndPlates, plateFormat,isDualChannel);
         if (!okFile) {
             errorMsg[0] += "Error: Step3: could not create PlateConf.txt file. Most likely:<br/> you forgot to set define any wells at all<br/>";
             finalTestFail = true;
         } else {
             plateConfFile = newPConfFile;
             screenLogFile = "Screenlog.txt";
         }
 
         createOrupdateDescriptionFile();
 
 
 
 
        return finalTestFail;
     }
 
     /**
      * creates a new Jobname path in the upload_path and returns a file obj with the full path
      *
      * @return
      */
     private File getNewJobnameDir() {
         String jobNamePath="";
 	String jobName="";
         try {
 
         jobName  = File.createTempFile("JOB", File.separator,new File(uploadPath)).getName();
 
        
             
         jobNamePath = uploadPath+jobName;
 
         //delete the file,we dont need it we will create a dir with this name
         (new File(jobNamePath)).delete();
         
         //new File(jobNamePath).mkdirs();   //dont create it yet ... create it only if someone uploads a file
         }catch(IOException e) {
 
             e.printStackTrace();
             return null;
         }
         return new File(jobNamePath);
     }
 
     /**
      * gets the name of the operating system on client through browser
      *
      * @return
      */
     public String getUserAgent(){
         Locale locale = request.getLocale();
         return request.getHeader("User-Agent");
     }
 
     /**
      *
      *  get the linefeed which corresponds to the operating system
      *
      * @return   the linefeed type '\n'
      */
     public String getBrowserOSLineFeed() {
          String browser;
          browser = getUserAgent();
 
          Pattern p = Pattern.compile(".*Windows.*");
          Matcher m = p.matcher(browser);
          if(m.find()) {
             return "\r\n";     
 
          }
          p= Pattern.compile(".*Mac OS X.*");
          m=p.matcher(browser);
          if(m.find()) {
             return "\n";
 
          }
          p= Pattern.compile(".*Linux.*");
          m=p.matcher(browser);
          if(m.find()) {
             return "\n";
 
          }
          else {
              return "\n";
          }
 
 
     }
 
     public void setNotNewRun(boolean notNewRun) {
         this.notNewRun = notNewRun;
     }
 
     public UploadedFile getUploadedPlatelistFile() {
         return uploadedPlatelistFile;
     }
 
     public void setUploadedPlatelistFile(UploadedFile uploadedPlatelistFile) {
         this.uploadedPlatelistFile = uploadedPlatelistFile;
     }
 
     public String getErrorPlatelistFileMsg() {
         return errorPlatelistFileMsg;
     }
 
     public void setErrorPlatelistFileMsg(String errorPlatelistFileMsg) {
         this.errorPlatelistFileMsg = errorPlatelistFileMsg;
     }
 
     public boolean getErrorPlatelistFileUpload() {
         return errorPlatelistFileUpload;
     }
 
     public void setErrorPlatelistFileUpload(boolean errorPlatelistFileUpload) {
         this.errorPlatelistFileUpload = errorPlatelistFileUpload;
     }
 
     public String getViabilityFunction() {
         return viabilityFunction;
     }
 
     public void setViabilityFunction(String viabilityFunction) {
         this.viabilityFunction = viabilityFunction;
     }
 
 
     //TODO: there are so much iterating over the clickedWellsAndPlates array we should make an own class out of it with fancy iterating and finding and manipulating methods
     /**
      *
      *
      * this method gets all common wells which are defined on all the plates so we can put them into the all plate
      *
      * @return all commonly defined wells
      */
     public HashMap<String,String> getAllWells() {
         int size = clickedWellsAndPlates.size();
         HashMap<String,String> returnList = new HashMap<String,String>();
 
         HashMap<String,Integer> countWells = new HashMap<String,Integer>();
         //disregard the all plate...we dont want to count it
          for(int i=1; i < clickedWellsAndPlates.size(); i++) {
             HashMap<String,String> wellsArray =clickedWellsAndPlates.get(i).getWellsArray();
             Iterator wellIterator = wellsArray.keySet().iterator();
             while(wellIterator.hasNext()) {
                 String wellID = (String)wellIterator.next();
                 String thisWellType = wellsArray.get(wellID);
                 if(thisWellType !=null && wellID != null) {
                      //init if not before
                      if(!countWells.containsKey(wellID+"-"+thisWellType)) {
                          //quick hack...i am in a hurry ...TODO: better would be a two dimensional array here
                         countWells.put(wellID+"-"+thisWellType,0);
                      }
 
                          int tempCount = countWells.get(wellID+"-"+thisWellType);
                          tempCount++;
                          countWells.put(wellID+"-"+thisWellType,tempCount);
 
                 }
             }
 
         }
         for(String wellID : countWells.keySet()) {
            if(countWells.get(wellID)==size-1) {  //minus one because we dont add the all plate because on this we want to add it
                String idTypeArr[]= wellID.split("-");
                returnList.put(idTypeArr[0],idTypeArr[1]);
            }
         }
         return returnList;
     }
 
     /**
      *
      *  this rebuilds the all plate which means it collects all the wells which are common among all the other plates and
      *  draw them on the zero plate
      *
      */
     public void rebuildZeroPlate() {
         HashMap<String,String> allWells = getAllWells();
             //quick fix...overwrite old ones which are no longer there
             clickedWellsAndPlates.get(0).setWellsArray(new HashMap<String,String>());
             for(String wellID:allWells.keySet()) {
                 String wellType = allWells.get(wellID);
                 clickedWellsAndPlates.get(0).getWellsArray().put(wellID,wellType);
             }
     }
 
     public String getGoogleAnalyticsTrackerID() {
         return googleAnalyticsTrackerID;
     }
     public boolean getCheckIsGoogleAnalyticsTrackerIDDefined() {
         if(googleAnalyticsTrackerID==null) {
             return false;
         }
         else return true;
     }
 
     public boolean getIsUploadedPlatelistFileDefined(){
         if(this.uploadedPlatelistFile==null) {
             return false;
         }
         else {
             if(this.uploadedPlatelistFile.getFileName()!=null) {
                 return true;
             }
             else {
                 return false;
             }
         }
         
     }
     
     public boolean getIsUploadedPlateConfigFileDefined(){
        if(this.uploadedPlateConfigFile==null) {
             return false;
         }
         else {
             if(this.uploadedPlateConfigFile.getFileName()!=null) {
                 return true;
             }
             else {
                 return false;
             }
         }
     }
     public boolean getIsUploadedScreenlogFileDefined(){
         if(this.uploadedScreenlogFile==null) {
             return false;
         }
         else {
             if(this.uploadedScreenlogFile.getFileName()!=null) {
                 return true;
             }
             else {
                 return false;
             }
         }
     }
     public void resetErrorMsgs() {
         sessionFileUploadErrorMsg="";
         errorDatafileMsg="";
         errorPlatelistFileMsg="";
         nextLinkErrorMsg = "";
         errorPlateconfFileMsg= "";
         errorScreenlogFileMsg = "";
         errorAnnotFileMsg="";
         errorDescriptionFileMsg = "";
         
     }
     //this is a proxy for the uploaded files write method
     //because due to the permanent polling of google and msn bots
     //we should only create the jobnamedir when we are writing the first
     //file AND NOT when someone loads the page  ...google bots are polling the site every 3 minutes so it
     //would create a directory every so interval
 
     //this is the prox for  uploadedPlatelistFile.write(copied);
     public void writeFileProxy(UploadedFile file, File copied)  {
         //if the directory of the file does not exist
         File dir = new File(copied.getParent());
         if(!dir.exists()) {
             dir.mkdirs();
         }
         file.write(copied);
     }
     
     public void checkAndCreateUploadDirectory() {
         String uploadPath = msg.get("upload-path");
         File uploadPathObj = new File(uploadPath);
         if(!uploadPathObj.exists()) {
             if(!uploadPathObj.mkdirs()) {
                 throw new TapestryException("Cannot create directory on the server to upload files: "+uploadPath+".\nCheck read/write permissions or change file upload property in apps.properties file",null);
             }
         }
         if(uploadPathObj.canRead()&&uploadPathObj.canWrite()) {
             //check if we can create a temp file in the new dir
             File tmpFile =new File(uploadPath+"tmp.txt");
             try{
 
                 tmpFile.createNewFile();
                 if(tmpFile.canWrite()) {
                     tmpFile.delete();
                     return;
                 }
 
             }catch(IOException e) {
                 e.printStackTrace();
                 if(tmpFile.exists()){
                     tmpFile.delete();
                 }
                 throw new TapestryException("Cannot write a test file in the upload path: "+uploadPath+"tmp.txt"+".\nCheck read/write permissions or change file upload property in apps.properties file",null);
             }
 
          }else {
                 throw new TapestryException("Cannot read or write directory: "+uploadPath+".\nCheck read/write permissions",null);
         }
     }
 
     public File getAnnotationFileFromAdvancedFileImporter() {
         return annotationFileFromAdvancedFileImporter;
     }
 
     public void setAnnotationFileFromAdvancedFileImporter(File annotationFileFromAdvancedFileImporter) {
         this.annotationFileFromAdvancedFileImporter = annotationFileFromAdvancedFileImporter;
     }
     public Boolean getIsFlashValid() {
         return isFlashValid;
     }
 
     public void setIsFlashValid(Boolean flashValid) {
         isFlashValid = flashValid;
     }
 
     public void setPlateConfScreenlogFileFromAdvancedFileImporter(File plateConf,File screenlog) {
         plateConfigFileFromAdvancedFileImporter=plateConf;
         screenlogFileFromAdvancedFileImporter=screenlog;
     }
 }
 
