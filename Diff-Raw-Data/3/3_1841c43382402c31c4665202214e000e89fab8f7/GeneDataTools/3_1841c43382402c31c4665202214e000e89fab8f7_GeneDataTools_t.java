 package edu.ucdenver.ccp.PhenoGen.tools.analysis;
 
 import edu.ucdenver.ccp.PhenoGen.driver.RException;
 import edu.ucdenver.ccp.PhenoGen.driver.R_session;
 import edu.ucdenver.ccp.PhenoGen.data.AsyncUpdateDataset;
 import edu.ucdenver.ccp.PhenoGen.data.Dataset;
 import edu.ucdenver.ccp.PhenoGen.data.User;
 import edu.ucdenver.ccp.PhenoGen.data.Bio.Gene;
 import edu.ucdenver.ccp.PhenoGen.data.Bio.BQTL;
 import edu.ucdenver.ccp.PhenoGen.data.Bio.EQTL;
 import edu.ucdenver.ccp.PhenoGen.data.Bio.Transcript;
 import edu.ucdenver.ccp.PhenoGen.data.Bio.TranscriptCluster;
 import edu.ucdenver.ccp.PhenoGen.driver.PerlHandler;
 import edu.ucdenver.ccp.PhenoGen.driver.PerlException;
 import edu.ucdenver.ccp.PhenoGen.driver.ExecHandler;
 import edu.ucdenver.ccp.PhenoGen.driver.ExecException;
 import edu.ucdenver.ccp.util.FileHandler;
 import edu.ucdenver.ccp.util.ObjectHandler;
 import edu.ucdenver.ccp.PhenoGen.tools.analysis.Statistic;
 import edu.ucdenver.ccp.PhenoGen.tools.analysis.AsyncGeneDataExpr;
 import edu.ucdenver.ccp.PhenoGen.tools.analysis.AsyncGeneDataTools;
 
 import java.util.GregorianCalendar;
 import java.util.Date;
 
 import javax.servlet.http.HttpSession;
 import java.sql.Connection;
 import java.sql.SQLException;
 
 import org.apache.log4j.Logger;
 
 import edu.ucdenver.ccp.PhenoGen.web.mail.*;
 import java.io.*;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.Timestamp;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Properties;
 import java.util.Set;
 
 
 
 public class GeneDataTools {
     private ArrayList<Thread> threadList=new ArrayList<Thread>();
     private String[] rErrorMsg = null;
     private R_session myR_session = new R_session();
     //private PerlHandler myPerl_session=null;
     private ExecHandler myExec_session = null;
     private HttpSession session = null;
     private User userLoggedIn = null;
     //private Dataset[] publicDatasets = null;
     //private Dataset selectedDataset = null;
     //private Dataset.DatasetVersion selectedDatasetVersion = null;
     private Connection dbConn = null;
     private Logger log = null;
     private String perlDir = "", fullPath = "";
     private String rFunctDir = "";
     private String userFilesRoot = "";
     private String urlPrefix = "";
     private int validTime=7*24*60*60*1000;
     private String perlEnvVar="";
     private String ucscDir="";
     private String ucscGeneDir="";
     private String bedDir="";
     private String geneSymbol="";
     private String ucscURL="";
     private String ucscURLfilter="";
     private String deMeanURL="";
     private String deFoldDiffURL="";
     private String chrom="";
     private String dbPropertiesFile="";
     private String ensemblDBPropertiesFile="";
     private int minCoord=0;
     private int maxCoord=0;
     FileHandler myFH=new FileHandler();
     private int usageID=-1;
     private int maxThreadRunning=1;
     String outputDir="";
     
     private String getNextID="select TRANS_DETAIL_USAGE_SEQ.nextVal from dual";
     private String insertUsage="insert into TRANS_DETAIL_USAGE (TRANS_DETAIL_ID,INPUT_ID,IDECODER_RESULT,RUN_DATE,ORGANISM) values (?,?,?,?,?)";
     String updateSQL="update TRANS_DETAIL_USAGE set TIME_TO_RETURN=? , RESULT=? where TRANS_DETAIL_ID=?";
     
     
 
     public GeneDataTools() {
         log = Logger.getRootLogger();
 
     }
     
     public int[] getOrganismSpecificIdentifiers(String organism,Connection dbConn){
         
             int[] ret=new int[2];
             String organismLong="Mouse";
             if(organism.equals("Rn")){
                 organismLong="Rat";
             }
             String atQuery="select Array_type_id from array_types "+
                         "where array_name like 'Affymetrix GeneChip "+organismLong+" Exon 1.0 ST Array'";
             String rnaIDQuery="select rna_dataset_id from RNA_DATASET "+
                         "where organism = '"+organism+"' and visible=1";
             PreparedStatement ps=null;
             try {
                 ps = dbConn.prepareStatement(atQuery);
                 ResultSet rs = ps.executeQuery();
                 while(rs.next()){
                     ret[0]=rs.getInt(1);
                 }
                 ps.close();
             } catch (SQLException ex) {
                 log.error("SQL Exception retreiving Array_Type_ID from array_types for Organism="+organism ,ex);
                 try {
                     ps.close();
                 } catch (Exception ex1) {
                    
                 }
             }
             try {
                 ps = dbConn.prepareStatement(rnaIDQuery);
                 ResultSet rs = ps.executeQuery();
                 while(rs.next()){
                     ret[1]=rs.getInt(1);
                 }
                 ps.close();
             } catch (SQLException ex) {
                 log.error("SQL Exception retreiving RNA_dataset_ID from RNA_DATASET for Organism="+organism ,ex);
             try {
                 ps.close();
             } catch (Exception ex1) {
                 
             }
             }
             return ret;
         
     }
 
     /**
      * Calls the Perl script WriteXML_RNA.pl and R script ExonCorrelation.R.
      * @param ensemblID       the ensemblIDs as a comma separated list
      * @param panel 
      * @param organism        the organism         
      * 
      */
     public void getGeneCentricData(String inputID,String ensemblIDList,
             String panel,
             String organism,int RNADatasetID,int arrayTypeID) {
         
         //Setup a String in the format YYYYMMDDHHMM to append to the folder
         Date start = new Date();
         GregorianCalendar gc = new GregorianCalendar();
         gc.setTime(start);
         String rOutputPath = "";
         outputDir="";
         String result="";
         
         try{
             PreparedStatement ps=dbConn.prepareStatement(getNextID, 
 						ResultSet.TYPE_SCROLL_INSENSITIVE,
 						ResultSet.CONCUR_UPDATABLE);
             ResultSet rs=ps.executeQuery();
             if (rs.next()){
                 usageID=rs.getInt(1);
             }
             ps.close();
         }catch(SQLException e){
             log.error("Error getting Transcription Detail Usage ID.next()",e);
         }
         try{
             PreparedStatement ps=dbConn.prepareStatement(insertUsage, 
 						ResultSet.TYPE_SCROLL_INSENSITIVE,
 						ResultSet.CONCUR_UPDATABLE);
             ps.setInt(1, usageID);
             ps.setString(2,inputID);
             ps.setString(3, ensemblIDList);
             ps.setTimestamp(4, new Timestamp(start.getTime()));
             ps.setString(5, organism);
             ps.execute();
             ps.close();
         }catch(SQLException e){
             log.error("Error saving Transcription Detail Usage",e);
         }
         
         //EnsemblIDList can be a comma separated list break up the list
         String[] ensemblList = ensemblIDList.split(",");
         String ensemblID1 = ensemblList[0];
         boolean error=false;
         if(ensemblID1!=null && !ensemblID1.equals("")){
             //Define output directory
             outputDir = fullPath + "tmpData/geneData/" + ensemblID1 + "/";
             session.setAttribute("geneCentricPath", outputDir);
             log.debug("checking for path:"+outputDir);
             String folderName = ensemblID1;
             //String publicPath = H5File.substring(H5File.indexOf("/Datasets/") + 10);
             //publicPath = publicPath.substring(0, publicPath.indexOf("/Affy.NormVer.h5"));
             
             try {
                 File geneDir=new File(outputDir);
                 File errorFile=new File(outputDir+"errMsg.txt");
                 if(geneDir.exists()){
                     Date lastMod=new Date(geneDir.lastModified());
                     Date prev2Months=new Date(start.getTime()-(60*24*60*60*1000));
                     if(lastMod.before(prev2Months)||errorFile.exists()){
                         if(myFH.deleteAllFilesPlusDirectory(geneDir)){
                              generateFiles(organism,rOutputPath,ensemblIDList,folderName,ensemblID1,RNADatasetID,arrayTypeID);
                              result="old files, regenerated all files";
                         }else{
                             error=true;
                         }
                     }else{
                         //do nothing just need to set session var
                         String errors;
                         errors = loadErrorMessage();
                         if(errors.equals("")){
                             getUCSCUrls(ensemblID1);
                             result="cache hit files not generated";
                         }else{
                             error=true;
                             this.setError(errors);
                         }
                     }
                 }else{
                     generateFiles(organism,rOutputPath,ensemblIDList,folderName,ensemblID1,RNADatasetID,arrayTypeID);
                     result="NewGene generated successfully";
                 }
                 
             } catch (Exception e) {
                 error=true;
                 
                 log.error("In Exception getting Gene Centric Results", e);
                 Email myAdminEmail = new Email();
                 String fullerrmsg=e.getMessage();
                     StackTraceElement[] tmpEx=e.getStackTrace();
                     for(int i=0;i<tmpEx.length;i++){
                         fullerrmsg=fullerrmsg+"\n"+tmpEx[i];
                     }
                 myAdminEmail.setSubject("Exception thrown getting Gene Centric Results");
                 myAdminEmail.setContent("There was an error while getting gene centric results.\n"+fullerrmsg);
                 try {
                     myAdminEmail.sendEmailToAdministrator((String) session.getAttribute("adminEmail"));
                 } catch (Exception mailException) {
                     log.error("error sending message", mailException);
                     throw new RuntimeException();
                 }
             }
         }else{
             error=true;
             setError("No Ensembl IDs");
         }
         if(error){
             result=(String)session.getAttribute("genURL");
         }
         this.setReturnSessionVar(error,ensemblID1);
         
         try{
             PreparedStatement ps=dbConn.prepareStatement(updateSQL, 
 						ResultSet.TYPE_SCROLL_INSENSITIVE,
 						ResultSet.CONCUR_UPDATABLE);
             Date end=new Date();
             long returnTimeMS=end.getTime()-start.getTime();
             ps.setLong(1, returnTimeMS);
             ps.setString(2, result);
             ps.setInt(3, usageID);
             ps.executeUpdate();
             ps.close();
         }catch(SQLException e){
             log.error("Error saving Transcription Detail Usage",e);
         }
     }
     
     public ArrayList<Gene> getRegionData(String chromosome,int minCoord,int maxCoord,
             String panel,
             String organism,int RNADatasetID,int arrayTypeID,double pValue) {
         
         chromosome=chromosome.toLowerCase();
         
         //Setup a String in the format YYYYMMDDHHMM to append to the folder
         Date start = new Date();
         GregorianCalendar gc = new GregorianCalendar();
         gc.setTime(start);
         String datePart=Integer.toString(gc.get(gc.MONTH)+1)+
                 Integer.toString(gc.get(gc.DAY_OF_MONTH))+
                 Integer.toString(gc.get(gc.YEAR))+"_"+
                 Integer.toString(gc.get(gc.HOUR_OF_DAY))+
                 Integer.toString(gc.get(gc.MINUTE))+
                 Integer.toString(gc.get(gc.SECOND));
         String rOutputPath = "";
         outputDir="";
         String result="";
         this.minCoord=minCoord;
         this.maxCoord=maxCoord;
         this.chrom=chromosome;
         String inputID=organism+":"+chromosome+":"+minCoord+"-"+maxCoord;
         try{
             PreparedStatement ps=dbConn.prepareStatement(getNextID, 
 						ResultSet.TYPE_SCROLL_INSENSITIVE,
 						ResultSet.CONCUR_UPDATABLE);
             ResultSet rs=ps.executeQuery();
             if (rs.next()){
                 usageID=rs.getInt(1);
             }
             ps.close();
         }catch(SQLException e){
             log.error("Error getting Transcription Detail Usage ID.next()",e);
         }
         try{
             PreparedStatement ps=dbConn.prepareStatement(insertUsage, 
 						ResultSet.TYPE_SCROLL_INSENSITIVE,
 						ResultSet.CONCUR_UPDATABLE);
             ps.setInt(1, usageID);
             ps.setString(2,inputID);
             ps.setString(3, "");
             ps.setTimestamp(4, new Timestamp(start.getTime()));
             ps.setString(5, organism);
             ps.execute();
             ps.close();
         }catch(SQLException e){
             log.error("Error saving Transcription Detail Usage",e);
         }
         
         //EnsemblIDList can be a comma separated list break up the list
         boolean error=false;
 
             //Define output directory
             outputDir = fullPath + "tmpData/regionData/" +organism+ chromosome+"_"+minCoord+"_"+maxCoord+"_"+datePart + "/";
             //session.setAttribute("geneCentricPath", outputDir);
             log.debug("checking for path:"+outputDir);
             String folderName = organism+chromosome+"_"+minCoord+"_"+maxCoord+"_"+datePart;
             //String publicPath = H5File.substring(H5File.indexOf("/Datasets/") + 10);
             //publicPath = publicPath.substring(0, publicPath.indexOf("/Affy.NormVer.h5"));
             RegionDirFilter rdf=new RegionDirFilter(organism+ chromosome+"_"+minCoord+"_"+maxCoord+"_");
             File mainDir=new File(fullPath + "tmpData/regionData");
             File[] list=mainDir.listFiles(rdf);
             try {
                 File geneDir=new File(outputDir);
                 File errorFile=new File(outputDir+"errMsg.txt");
                 if(geneDir.exists()){
                         //do nothing just need to set session var
                         String errors;
                         errors = loadErrorMessage();
                         if(errors.equals("")){
                             getUCSCUrls("Region");
                             result="cache hit files not generated";
                         }else{
                             error=true;
                             this.setError(errors);
                         }
                 }else{
                     if(list.length>0){
                         outputDir=list[0].getAbsolutePath()+"/";
                         int second=outputDir.lastIndexOf("/",outputDir.length()-2);
                         folderName=outputDir.substring(second+1,outputDir.length()-1);
                         String errors;
                         errors = loadErrorMessage();
                         if(errors.equals("")){
                             getUCSCUrls("Region");
                             result="cache hit files not generated";
                         }else{
                             error=true;
                             this.setError(errors);
                         }
                     }else{
                         generateRegionFiles(organism,folderName,RNADatasetID,arrayTypeID);
                         result="New Region generated successfully";
                     }
                 }
                 
             } catch (Exception e) {
                 error=true;
                 
                 log.error("In Exception getting Gene Centric Results", e);
                 Email myAdminEmail = new Email();
                 String fullerrmsg=e.getMessage();
                     StackTraceElement[] tmpEx=e.getStackTrace();
                     for(int i=0;i<tmpEx.length;i++){
                         fullerrmsg=fullerrmsg+"\n"+tmpEx[i];
                     }
                 myAdminEmail.setSubject("Exception thrown getting Gene Centric Results");
                 myAdminEmail.setContent("There was an error while getting gene centric results.\n"+fullerrmsg);
                 try {
                     myAdminEmail.sendEmailToAdministrator((String) session.getAttribute("adminEmail"));
                 } catch (Exception mailException) {
                     log.error("error sending message", mailException);
                     throw new RuntimeException();
                 }
             }
         if(error){
             result=(String)session.getAttribute("genURL");
         }
         this.setReturnSessionVar(error,folderName);
         ArrayList<Gene> ret=Gene.readGenes(outputDir+"Region.xml");
         ret=this.mergeOverlapping(ret);
         this.addHeritDABG(ret,minCoord,maxCoord,organism,chromosome,RNADatasetID, arrayTypeID);
         ArrayList<String> tissues=new ArrayList<String>();
         //ArrayList<EQTL> probeeQTLs=this.getProbeEQTLs(minCoord, maxCoord, chromosome, arrayTypeID,tissues);
         HashMap transInQTLsCore=getTransControlledFromEQTLs(minCoord,maxCoord,chromosome,arrayTypeID,pValue,"core"); //genes in region controled from where?
         HashMap transInQTLsExtended=getTransControlledFromEQTLs(minCoord,maxCoord,chromosome,arrayTypeID,pValue,"extended");
         HashMap transInQTLsFull=getTransControlledFromEQTLs(minCoord,maxCoord,chromosome,arrayTypeID,pValue,"full");
         //addQTLS(ret,probeeQTLs);
         addFromQTLS(ret,transInQTLsCore,transInQTLsExtended,transInQTLsFull);
         try{
             PreparedStatement ps=dbConn.prepareStatement(updateSQL, 
 						ResultSet.TYPE_SCROLL_INSENSITIVE,
 						ResultSet.CONCUR_UPDATABLE);
             Date end=new Date();
             long returnTimeMS=end.getTime()-start.getTime();
             ps.setLong(1, returnTimeMS);
             ps.setString(2, result);
             ps.setInt(3, usageID);
             ps.executeUpdate();
             ps.close();
         }catch(SQLException e){
             log.error("Error saving Transcription Detail Usage",e);
         }
         return ret;
     }
 
     public boolean generateFiles(String organism,String rOutputPath, String ensemblIDList,String folderName,String ensemblID1,int RNADatasetID,int arrayTypeID) {
         log.debug("generate files");
         AsyncGeneDataTools prevThread=null;
         boolean completedSuccessfully = false;
         log.debug("outputDir:"+outputDir);
         File outDirF = new File(outputDir);
         //Mkdir if some are missing    
         if (!outDirF.exists()) {
             log.debug("make output dir");
             outDirF.mkdirs();
         }
         
         boolean createdXML=this.createImagesXMLFiles(organism,ensemblIDList,arrayTypeID,ensemblID1,RNADatasetID);
         
         if(!createdXML){ 
             
         }else{       
             boolean ucscComplete=getUCSCUrls(ensemblID1);
             if(!ucscComplete){
                    completedSuccessfully=false;
             }
             prevThread=callAsyncGeneDataTools(chrom, minCoord, maxCoord,arrayTypeID,RNADatasetID);
             boolean createdExpressionfile=callPanelExpr(chrom,minCoord,maxCoord,arrayTypeID,RNADatasetID,prevThread);
             if(!createdExpressionfile){
                    completedSuccessfully=false;
             }
         }
         return completedSuccessfully;
     }
     
     public boolean generateRegionFiles(String organism,String folderName,int RNADatasetID,int arrayTypeID) {
         log.debug("generate files");
         boolean completedSuccessfully = false;
         log.debug("outputDir:"+outputDir);
         File outDirF = new File(outputDir);
         //Mkdir if some are missing    
         if (!outDirF.exists()) {
             log.debug("make output dir");
             outDirF.mkdirs();
         }
         
         boolean createdXML=this.createRegionImagesXMLFiles(folderName,organism,arrayTypeID,RNADatasetID);
         
         if(!createdXML){ 
             
         }else{       
             boolean ucscComplete=getUCSCUrls("Region");
             if(!ucscComplete){
                    completedSuccessfully=false;
             }
         }
         return completedSuccessfully;
     }
     
      	public boolean createCircosFiles(String perlScriptDirectory, String perlEnvironmentVariables, String[] perlScriptArguments,String filePrefixWithPath){
    		// 
    	    boolean completedSuccessfully=false;
    	    String circosErrorMessage;
    		log.debug(" in GeneDataTools.createCircosFiles ");
    		// perlScriptArguments is an array of strings
    		// perlScriptArguments[0] is "perl" or maybe includes the path??
    		// perlScriptArguments[1] is the filename including directory of the perl script
    		// perlScriptArguments[2] ... are argument inputs to the perl script
    		//for (int i=0; i<perlScriptArguments.length; i++){
    		//	log.debug(i + "::" + perlScriptArguments[i]);
    		//}
    
         //set environment variables so you can access oracle. Environment variables are pulled from perlEnvironmentVariables which is a comma separated list
         String[] envVar=perlEnvironmentVariables.split(",");
     
         for (int i = 0; i < envVar.length; i++) {
             log.debug(i + " EnvVar::" + envVar[i]);
         }
         
        
         //construct ExecHandler which is used instead of Perl Handler because environment variables were needed.
         myExec_session = new ExecHandler(perlScriptDirectory, perlScriptArguments, envVar, filePrefixWithPath);
 
         try {
 
             myExec_session.runExec();
 
         } catch (ExecException e) {
             log.error("In Exception of createCircosFiles Exec_session", e);
             Email myAdminEmail = new Email();
             myAdminEmail.setSubject("Exception thrown in Exec_session");
             circosErrorMessage = "There was an error while running ";
             circosErrorMessage = circosErrorMessage + " " + perlScriptArguments[1] + " (";
             for(int i=2; i<perlScriptArguments.length; i++){
             	circosErrorMessage = circosErrorMessage + " " + perlScriptArguments[i];
             }
             circosErrorMessage = circosErrorMessage + ")\n\n"+myExec_session.getErrors();
             myAdminEmail.setContent(circosErrorMessage);
             try {
                 myAdminEmail.sendEmailToAdministrator((String) session.getAttribute("adminEmail"));
             } catch (Exception mailException) {
                 log.error("error sending message", mailException);
                 throw new RuntimeException();
             }
         }
         
         if(myExec_session.getExitValue()!=0){
             Email myAdminEmail = new Email();
             myAdminEmail.setSubject("Exception thrown in Exec_session");
             circosErrorMessage = "There was an error while running ";
             circosErrorMessage = circosErrorMessage + " " + perlScriptArguments[1] + " (";
             for(int i=2; i<perlScriptArguments.length; i++){
             	circosErrorMessage = circosErrorMessage + " " + perlScriptArguments[i];
             }
             circosErrorMessage = circosErrorMessage + ")\n\n"+myExec_session.getErrors();
             myAdminEmail.setContent(circosErrorMessage);
             try {
                 myAdminEmail.sendEmailToAdministrator((String) session.getAttribute("adminEmail"));
             } catch (Exception mailException) {
                 log.error("error sending message", mailException);
                 throw new RuntimeException();
             }
         }else{
             completedSuccessfully=true;
         }
         
    		return completedSuccessfully;
    	} 
     
     public boolean createImagesXMLFiles(String organism,String ensemblIDList,int arrayTypeID,String ensemblID1,int rnaDatasetID){
         boolean completedSuccessfully=false;
         try{
             int publicUserID=new User().getUser_id("public",dbConn);
 
             Properties myProperties = new Properties();
             File myPropertiesFile = new File(dbPropertiesFile);
             myProperties.load(new FileInputStream(myPropertiesFile));
 
             String dsn="dbi:"+myProperties.getProperty("PLATFORM") +":"+myProperties.getProperty("DATABASE");
             String dbUser=myProperties.getProperty("USER");
             String dbPassword=myProperties.getProperty("PASSWORD");
 
             File ensPropertiesFile = new File(ensemblDBPropertiesFile);
             Properties myENSProperties = new Properties();
             myENSProperties.load(new FileInputStream(ensPropertiesFile));
             String ensHost=myENSProperties.getProperty("HOST");
             String ensPort=myENSProperties.getProperty("PORT");
             String ensUser=myENSProperties.getProperty("USER");
             String ensPassword=myENSProperties.getProperty("PASSWORD");
             //construct perl Args
             String[] perlArgs = new String[19];
             perlArgs[0] = "perl";
             perlArgs[1] = perlDir + "writeXML_RNA.pl";
             perlArgs[2] = ucscDir+ucscGeneDir;
             perlArgs[3] = outputDir;
             perlArgs[4] = outputDir + "Gene.xml";
 
             if (organism.equals("Rn")) {
                 perlArgs[5] = "Rat";
             } else if (organism.equals("Mm")) {
                 perlArgs[5] = "Mouse";
             }
             perlArgs[6] = "Core";
             perlArgs[7] = ensemblIDList;
             perlArgs[8] = bedDir;
             perlArgs[9] = Integer.toString(arrayTypeID);
             perlArgs[10] = Integer.toString(rnaDatasetID);
             perlArgs[11] = Integer.toString(publicUserID);
             perlArgs[12] = dsn;
             perlArgs[13] = dbUser;
             perlArgs[14] = dbPassword;
             perlArgs[15] = ensHost;
             perlArgs[16] = ensPort;
             perlArgs[17] = ensUser;
             perlArgs[18] = ensPassword;
 
 
             //set environment variables so you can access oracle pulled from perlEnvVar session variable which is a comma separated list
             String[] envVar=perlEnvVar.split(",");
 
             for (int i = 0; i < envVar.length; i++) {
                 log.debug(i + " EnvVar::" + envVar[i]);
             }
 
 
             //construct ExecHandler which is used instead of Perl Handler because environment variables were needed.
             myExec_session = new ExecHandler(perlDir, perlArgs, envVar, fullPath + "tmpData/geneData/"+ensemblID1+"/");
 
             try {
 
                 myExec_session.runExec();
 
             } catch (ExecException e) {
                 log.error("In Exception of run writeXML_RNA.pl Exec_session", e);
                 setError("Running Perl Script to get Gene and Transcript details/images.");
                 Email myAdminEmail = new Email();
                 myAdminEmail.setSubject("Exception thrown in Exec_session");
                 myAdminEmail.setContent("There was an error while running "
                         + perlArgs[1] + " (" + perlArgs[2] +" , "+perlArgs[3]+" , "+perlArgs[4]+" , "+perlArgs[5]+" , "+perlArgs[6]+","+perlArgs[7]+","+perlArgs[8]+","+perlArgs[9]+","+perlArgs[10]+","+perlArgs[11]+
                         ")\n\n"+myExec_session.getErrors());
                 try {
                     myAdminEmail.sendEmailToAdministrator((String) session.getAttribute("adminEmail"));
                 } catch (Exception mailException) {
                     log.error("error sending message", mailException);
                     throw new RuntimeException();
                 }
             }
 
             if(myExec_session.getExitValue()!=0){
                 Email myAdminEmail = new Email();
                 myAdminEmail.setSubject("Exception thrown in Exec_session");
                 myAdminEmail.setContent("There was an error while running "
                         + perlArgs[1] + " (" + perlArgs[2] +" , "+perlArgs[3]+" , "+perlArgs[4]+" , "+perlArgs[5]+" , "+perlArgs[6]+
                         ")\n\n"+myExec_session.getErrors());
                 try {
                     myAdminEmail.sendEmailToAdministrator((String) session.getAttribute("adminEmail"));
                 } catch (Exception mailException) {
                     log.error("error sending message", mailException);
                     throw new RuntimeException();
                 }
             }else{
                 completedSuccessfully=true;
             }
         }catch(Exception e){
             log.error("Error getting DB properties or Public User ID.",e);
             String fullerrmsg=e.getMessage();
                     StackTraceElement[] tmpEx=e.getStackTrace();
                     for(int i=0;i<tmpEx.length;i++){
                         fullerrmsg=fullerrmsg+"\n"+tmpEx[i];
                     }
             Email myAdminEmail = new Email();
                 myAdminEmail.setSubject("Exception thrown in GeneDataTools.java");
                 myAdminEmail.setContent("There was an error setting up to run writeXML_RNA.pl\n\nFull Stacktrace:\n"+fullerrmsg);
                 try {
                     myAdminEmail.sendEmailToAdministrator((String) session.getAttribute("adminEmail"));
                 } catch (Exception mailException) {
                     log.error("error sending message", mailException);
                     throw new RuntimeException();
                 }
         }
         return completedSuccessfully;
     }
     
     public boolean createRegionImagesXMLFiles(String folderName,String organism,int arrayTypeID,int rnaDatasetID){
         boolean completedSuccessfully=false;
         try{
             int publicUserID=new User().getUser_id("public",dbConn);
 
             Properties myProperties = new Properties();
             File myPropertiesFile = new File(dbPropertiesFile);
             myProperties.load(new FileInputStream(myPropertiesFile));
 
             String dsn="dbi:"+myProperties.getProperty("PLATFORM") +":"+myProperties.getProperty("DATABASE");
             String dbUser=myProperties.getProperty("USER");
             String dbPassword=myProperties.getProperty("PASSWORD");
 
             File ensPropertiesFile = new File(ensemblDBPropertiesFile);
             Properties myENSProperties = new Properties();
             myENSProperties.load(new FileInputStream(ensPropertiesFile));
             String ensHost=myENSProperties.getProperty("HOST");
             String ensPort=myENSProperties.getProperty("PORT");
             String ensUser=myENSProperties.getProperty("USER");
             String ensPassword=myENSProperties.getProperty("PASSWORD");
             //construct perl Args
             String[] perlArgs = new String[20];
             perlArgs[0] = "perl";
             perlArgs[1] = perlDir + "writeXML_Region.pl";
             perlArgs[2] = ucscDir+ucscGeneDir;
             perlArgs[3] = outputDir;
             perlArgs[4] = folderName;
             if (organism.equals("Rn")) {
                 perlArgs[5] = "Rat";
             } else if (organism.equals("Mm")) {
                 perlArgs[5] = "Mouse";
             }
             perlArgs[6] = "Core";
             if(chrom.startsWith("chr")){
                 chrom=chrom.substring(3);
             }
             perlArgs[7] = chrom;
             perlArgs[8] = Integer.toString(minCoord);
             perlArgs[9] = Integer.toString(maxCoord);
             perlArgs[10] = Integer.toString(arrayTypeID);
             perlArgs[11] = Integer.toString(rnaDatasetID);
             perlArgs[12] = Integer.toString(publicUserID);
             perlArgs[13] = dsn;
             perlArgs[14] = dbUser;
             perlArgs[15] = dbPassword;
             perlArgs[16] = ensHost;
             perlArgs[17] = ensPort;
             perlArgs[18] = ensUser;
             perlArgs[19] = ensPassword;
 
 
             //set environment variables so you can access oracle pulled from perlEnvVar session variable which is a comma separated list
             String[] envVar=perlEnvVar.split(",");
 
             for (int i = 0; i < envVar.length; i++) {
                 log.debug(i + " EnvVar::" + envVar[i]);
             }
 
 
             //construct ExecHandler which is used instead of Perl Handler because environment variables were needed.
             myExec_session = new ExecHandler(perlDir, perlArgs, envVar, outputDir);
 
             try {
 
                 myExec_session.runExec();
 
             } catch (ExecException e) {
                 log.error("In Exception of run writeXML_RNA.pl Exec_session", e);
                 setError("Running Perl Script to get Gene and Transcript details/images.");
                 Email myAdminEmail = new Email();
                 myAdminEmail.setSubject("Exception thrown in Exec_session");
                 myAdminEmail.setContent("There was an error while running "
                         + perlArgs[1] + " (" + perlArgs[2] +" , "+perlArgs[3]+" , "+perlArgs[4]+" , "+perlArgs[5]+" , "+perlArgs[6]+","+perlArgs[7]+","+perlArgs[8]+","+perlArgs[9]+","+perlArgs[10]+","+perlArgs[11]+
                         ")\n\n"+myExec_session.getErrors());
                 try {
                     myAdminEmail.sendEmailToAdministrator((String) session.getAttribute("adminEmail"));
                 } catch (Exception mailException) {
                     log.error("error sending message", mailException);
                     throw new RuntimeException();
                 }
             }
 
             if(myExec_session.getExitValue()!=0){
                 Email myAdminEmail = new Email();
                 myAdminEmail.setSubject("Exception thrown in Exec_session");
                 myAdminEmail.setContent("There was an error while running "
                         + perlArgs[1] + " (" + perlArgs[2] +" , "+perlArgs[3]+" , "+perlArgs[4]+" , "+perlArgs[5]+" , "+perlArgs[6]+
                         ")\n\n"+myExec_session.getErrors());
                 try {
                     myAdminEmail.sendEmailToAdministrator((String) session.getAttribute("adminEmail"));
                 } catch (Exception mailException) {
                     log.error("error sending message", mailException);
                     throw new RuntimeException();
                 }
             }else{
                 completedSuccessfully=true;
             }
         }catch(Exception e){
             log.error("Error getting DB properties or Public User ID.",e);
             String fullerrmsg=e.getMessage();
                     StackTraceElement[] tmpEx=e.getStackTrace();
                     for(int i=0;i<tmpEx.length;i++){
                         fullerrmsg=fullerrmsg+"\n"+tmpEx[i];
                     }
             Email myAdminEmail = new Email();
                 myAdminEmail.setSubject("Exception thrown in GeneDataTools.java");
                 myAdminEmail.setContent("There was an error setting up to run writeXML_RNA.pl\n\nFull Stacktrace:\n"+fullerrmsg);
                 try {
                     myAdminEmail.sendEmailToAdministrator((String) session.getAttribute("adminEmail"));
                 } catch (Exception mailException) {
                     log.error("error sending message", mailException);
                     throw new RuntimeException();
                 }
         }
         return completedSuccessfully;
     }
     
     public AsyncGeneDataTools callAsyncGeneDataTools(String chr, int min, int max,int arrayTypeID,int rnaDS_ID){
         AsyncGeneDataTools agdt;         
         agdt = new AsyncGeneDataTools(session,outputDir,chr, min, max,arrayTypeID,rnaDS_ID,usageID);
         log.debug("Getting ready to start");
         agdt.start();
         log.debug("Started AsyncGeneDataTools");
         return agdt;
     }
     
     
     
     public boolean callPanelExpr(String chr, int min, int max,int arrayTypeID,int rnaDS_ID,AsyncGeneDataTools prevThread){
         boolean error=false;
         //create File with Probeset Tissue herit and DABG
         String datasetQuery="select rd.dataset_id, rd.tissue "+
                             "from rnadataset_dataset rd "+
                             "where rd.rna_dataset_id = "+rnaDS_ID+" "+
                             "order by rd.tissue";
         
         Date start=new Date();
         try{
             PreparedStatement ps = dbConn.prepareStatement(datasetQuery);
             ResultSet rs = ps.executeQuery();
             try{
                 
                 log.debug("Getting ready to start");
                 File indivf=new File(outputDir+"Panel_Expr_indiv_tmp.txt");
                 File groupf=new File(outputDir+"Panel_Expr_group_tmp.txt");
                 BufferedWriter outGroup=new BufferedWriter(new FileWriter(groupf));
                 BufferedWriter outIndiv=new BufferedWriter(new FileWriter(indivf));
                 ArrayList<AsyncGeneDataExpr> localList=new ArrayList<AsyncGeneDataExpr>();
                 SyncAndClose sac=new SyncAndClose(start,localList,dbConn,outGroup,outIndiv,usageID,outputDir);
                 while(rs.next()){
                     AsyncGeneDataExpr agde=new AsyncGeneDataExpr(session,outputDir+"tmp_psList.txt",outputDir,prevThread,threadList,maxThreadRunning,outGroup,outIndiv,sac);
                     String dataset_id=Integer.toString(rs.getInt("DATASET_ID"));
                     int iDSID=rs.getInt("DATASET_ID");
                     String tissue=rs.getString("TISSUE");
                     String tissueNoSpaces=tissue.replaceAll(" ", "_");
                     edu.ucdenver.ccp.PhenoGen.data.Dataset sDataSet=new edu.ucdenver.ccp.PhenoGen.data.Dataset();
                     edu.ucdenver.ccp.PhenoGen.data.Dataset curDS=sDataSet.getDataset(iDSID,dbConn);
                     String DSPath=userFilesRoot+"public/Datasets/"+curDS.getNameNoSpaces()+"_Master/Affy.NormVer.h5";
                     String sampleFile=userFilesRoot+"public/Datasets/"+curDS.getNameNoSpaces()+"_Master/v3_samples.txt";
                     String groupFile=userFilesRoot+"public/Datasets/"+curDS.getNameNoSpaces()+"_Master/v3_groups.txt";
                     String outGroupFile="group_"+tissueNoSpaces+"_exprVal.txt";
                     String outIndivFile="indiv_"+tissueNoSpaces+"_exprVal.txt";
                     agde.add(DSPath,sampleFile,groupFile,outGroupFile,outIndivFile,tissue,curDS.getPlatform());
                     threadList.add(agde);
                     localList.add(agde);
                     agde.start();     
                 }
                 ps.close();
                 
                 log.debug("Started AsyncGeneDataExpr");
             }catch(IOException ioe){
                 
             }
             
         }catch(SQLException e){
             error=true;
             log.error("Error getting dataset id",e);
             setError("SQL Error occurred while setting up Panel Expression");
         }
         return error;
     }
     
     
     
     private boolean getUCSCUrls(String ensemblID1){
         boolean error=false;
         String[] urls;
         try{
                 urls=myFH.getFileContents(new File(outputDir + ensemblID1+".url"));
                 this.geneSymbol=urls[0];
                 session.setAttribute("geneSymbol", this.geneSymbol);
                 this.ucscURL=urls[1];
                 this.ucscURLfilter=urls[2];
                 int start=urls[1].indexOf("position=")+9;
                 int end=urls[1].indexOf("&",start);
                 String position=urls[1].substring(start,end);
                 String[] split=position.split(":");
                 String chromosome=split[0].substring(3);
                 String[] split2=split[1].split("-");
                 this.minCoord=Integer.parseInt(split2[0]);
                 this.maxCoord=Integer.parseInt(split2[1]);
                 this.chrom=chromosome;
                 log.debug(ucscURL+"\n"+ucscURLfilter);
         }catch(IOException e){
                 log.error("Error reading url file "+outputDir + ensemblID1,e);
                 setError("Reading URL File");
                 error=true;
         }
         return error;
     }
     
     private String loadErrorMessage(){
         String ret="";
         try{
                 File err=new File(outputDir +"errMsg.txt");
                 if(err.exists()){
                     String[] tmp=myFH.getFileContents(new File(outputDir +"errMsg.txt"));
                     if(tmp!=null){
                         if(tmp.length>=1){
                             ret=tmp[0];
                         }
                         for(int i=1;i<tmp.length;i++){
                             ret=ret+"\n"+tmp;
                         }
                     }
                 }
         }catch(IOException e){
                 log.error("Error reading errMsg.txt file "+outputDir ,e);
                 setError("Reading errMsg File");
         }
         return ret;
     }
     
     private void setError(String errorMessage){
         String tmp=(String)session.getAttribute("genURL");
         if(tmp==null||tmp.equals("")||!tmp.startsWith("ERROR:")){
             session.setAttribute("genURL","ERROR: "+errorMessage);
         }
     }
     
     private void setReturnSessionVar(boolean error,String folderName){
         if(!error){
             session.setAttribute("genURL",urlPrefix + "tmpData/geneData/" + folderName + "/");
             session.setAttribute("ucscURL", this.ucscURL);
             session.setAttribute("ucscURLFiltered", this.ucscURLfilter);
             session.setAttribute("curOutputDir",outputDir);
         }else{
             String tmp=(String)session.getAttribute("genURL");
             if(tmp.equals("")||!tmp.startsWith("ERROR:")){
                 session.setAttribute("genURL","ERROR:Unknown Error");
             }
             session.setAttribute("ucscURL", "");
             session.setAttribute("ucscURLFiltered", "");
             if(folderName!=null && !folderName.equals("")){
                 try{
                     new FileHandler().writeFile((String)session.getAttribute("genURL"),outputDir+"errMsg.txt");
                 }catch(IOException e){
                     log.error("Error writing errMsg.txt",e);
                 }
             }
         }
     }
     public HttpSession getSession() {
         return session;
     }
 
     public String formatDate(GregorianCalendar gc) {
         String ret;
         String year = Integer.toString(gc.get(GregorianCalendar.YEAR));
         String month = Integer.toString(gc.get(GregorianCalendar.MONTH) + 1);
         if (month.length() == 1) {
             month = "0" + month;
         }
         String day = Integer.toString(gc.get(GregorianCalendar.DAY_OF_MONTH));
         if (day.length() == 1) {
             day = "0" + day;
         }
         String hour = Integer.toString(gc.get(GregorianCalendar.HOUR_OF_DAY));
         if (hour.length() == 1) {
             hour = "0" + hour;
         }
         String minute = Integer.toString(gc.get(GregorianCalendar.MINUTE));
         if (minute.length() == 1) {
             minute = "0" + minute;
         }
         ret = year + month + day + hour + minute;
         return ret;
     }
     
     public void setSession(HttpSession inSession) {
         log.debug("in GeneDataTools.setSession");
         this.session = inSession;
         
         log.debug("start");
         this.dbConn = (Connection) session.getAttribute("dbConn");
         log.debug("db");
         this.perlDir = (String) session.getAttribute("perlDir") + "scripts/";
         log.debug("perl"+perlDir);
         String contextRoot = (String) session.getAttribute("contextRoot");
         log.debug("context"+contextRoot);
         String appRoot = (String) session.getAttribute("applicationRoot");
         log.debug("app"+appRoot);
         this.fullPath = appRoot + contextRoot;
         log.debug("fullpath");
         this.rFunctDir = (String) session.getAttribute("rFunctionDir");
         log.debug("rFunction");
         this.userFilesRoot = (String) session.getAttribute("userFilesRoot");
         log.debug("userFilesRoot");
         this.urlPrefix=(String)session.getAttribute("mainURL");
         if(urlPrefix.endsWith(".jsp")){
             urlPrefix=urlPrefix.substring(0,urlPrefix.lastIndexOf("/")+1);
         }
         log.debug("mainURL");
         this.perlEnvVar=(String)session.getAttribute("perlEnvVar");
         log.debug("PerlEnv");
         this.ucscDir=(String)session.getAttribute("ucscDir");
         this.ucscGeneDir=(String)session.getAttribute("ucscGeneDir");
         log.debug("ucsc");
         this.bedDir=(String) session.getAttribute("bedDir");
         log.debug("bedDir");
         
         this.dbPropertiesFile = (String)session.getAttribute("dbPropertiesFile");
         this.ensemblDBPropertiesFile = (String)session.getAttribute("ensDbPropertiesFile");
         
     }
     
     public ArrayList<Gene> mergeOverlapping(ArrayList<Gene> initialList){
         ArrayList<Gene> mainGenes=new ArrayList<Gene>();
         ArrayList<Gene> rnaGenes=new ArrayList<Gene>();
         ArrayList<Gene> singleExon=new ArrayList<Gene>();
         for(int i=0;i<initialList.size();i++){
             if(initialList.get(i).getSource().equals("Ensembl")){
                 mainGenes.add(initialList.get(i));
             }else{
                 rnaGenes.add(initialList.get(i));
             }
         }
         for(int i=0;i<rnaGenes.size();i++){
             double maxOverlap=0;
             int maxIndex=-1;
             for(int j=0;j<mainGenes.size();j++){
                 double overlapPerc=calculateOverlap(rnaGenes.get(i),mainGenes.get(j));
                 if(overlapPerc>maxOverlap){
                     maxOverlap=overlapPerc;
                     maxIndex=j;
                 }
             }
             if(maxIndex>-1){
                 //merge into mainGene at maxIndex
                 ArrayList<Transcript> rnaTrans=rnaGenes.get(i).getTranscripts();
                 mainGenes.get(maxIndex).addTranscripts(rnaTrans);
             }else{
                 //add to main
                 if(rnaGenes.get(i).isSingleExon()){
                     singleExon.add(rnaGenes.get(i));
                 }else{
                     mainGenes.add(rnaGenes.get(i)); 
                 }
             }
         }
         for(int i=0;i<singleExon.size();i++){
             mainGenes.add(singleExon.get(i));
         }
         return mainGenes;
     }
     
     public void addHeritDABG(ArrayList<Gene> list,int min,int max,String organism,String chr,int rnaDS_ID,int arrayTypeID){
         //get all probesets for region with herit and dabg
         if(chr.startsWith("chr")){
             chr=chr.substring(3);
         }
         HashMap probesets=new HashMap();
         String probeQuery="select phd.probeset_id, rd.tissue, phd.herit,phd.dabg "+
                             "from probeset_herit_dabg phd , rnadataset_dataset rd "+
                             "where rd.rna_dataset_id = "+rnaDS_ID+" "+
                             "and phd.dataset_id=rd.dataset_id "+
                             "and phd.probeset_id in ("+
                                 "select s.Probeset_ID "+
                                 "from Chromosomes c, Affy_Exon_ProbeSet s "+
                                 "where s.chromosome_id = c.chromosome_id "+
                                 "and substr(c.name,1,2) = '"+chr+"' "+
                             "and "+
                             "((s.psstart >= "+min+" and s.psstart <="+max+") OR "+
                             "(s.psstop >= "+min+" and s.psstop <= "+max+")) "+
                             "and s.psannotation <> 'transcript' " +
                             "and s.Array_TYPE_ID = "+arrayTypeID+") "+ 
                             "order by phd.probeset_id,rd.tissue";
         
         
         try{
             log.debug("herit/DABG SQL\n"+probeQuery);
             PreparedStatement ps = dbConn.prepareStatement(probeQuery);
             ResultSet rs = ps.executeQuery();
             while(rs.next()){
                 String probeset=Integer.toString(rs.getInt("PROBESET_ID"));
                 double herit=rs.getDouble("herit");
                 double dabg=rs.getDouble("dabg");
                 String tissue=rs.getString("TISSUE");
                 //log.debug("adding"+probeset);
                 if(probesets.containsKey(probeset)){
                     HashMap phm=(HashMap)probesets.get(probeset);
                     HashMap val=new HashMap();
                     val.put("herit", herit);
                     val.put("dabg", dabg);
                     phm.put(tissue, val);
                 }else{
                     HashMap phm=new HashMap();
                     HashMap val=new HashMap();
                     val.put("herit", herit);
                     val.put("dabg", dabg);
                     phm.put(tissue, val);
                     probesets.put(probeset, phm);
                 }
             }
             ps.close();
             log.debug("HashMap size:"+probesets.size());
         }catch(SQLException e){
             log.error("Error retreiving Herit/DABG.",e);
             System.err.println("Error retreiving Herit/DABG.");
             e.printStackTrace(System.err);
         }
         if(probesets!=null){
             //fill probeset data for each Gene
             for(int i=0;i<list.size();i++){
                 Gene curGene=list.get(i);
                 curGene.setHeritDabg(probesets);
             }
         }
         
     }
     
     
     //calculate the % of gene1 that overlaps gene2
     public double calculateOverlap(Gene gene1, Gene gene2){
         double ret=0;
         //needs to be on same strand
         if(gene1.getStrand().equals(gene2.getStrand())){
             long gene1S=gene1.getStart();
             long gene1E=gene1.getEnd();
             long gene2S=gene2.getStart();
             long gene2E=gene2.getEnd();
             
             long gene1Len=gene1E-gene1S;
             if(gene1S>gene2S&&gene1S<gene2E){
                 long end=gene2E;
                 if(gene1E<gene2E){
                     end=gene1E;
                 }
                 double len=end-gene1S;
                 ret=len/gene1Len*100;
             }else if(gene1E>gene2S&&gene1E<gene2E){
                 long start=gene2S;
                 double len=gene1E-start;
                 ret=len/gene1Len*100;
             }else if(gene1S<gene2S&&gene1E>gene2E){
                 double len=gene2E-gene2S;
                 ret=len/gene1Len*100;
             }
         }
         return ret;
     }
     
     public ArrayList<EQTL> getProbeEQTLs(int min,int max,String chr,int arrayTypeID,ArrayList<String> tissues){
         ArrayList<EQTL> eqtls=new ArrayList<EQTL>();
         if(chr.startsWith("chr")){
             chr=chr.substring(3);
         }
         HashMap probesets=new HashMap();
         String qtlQuery="select eq.identifier,eq.lod_score,eq.p_value,eq.fdr,eq.marker,eq.marker_chromosome,eq.marker_mb,eq.lower_limit,eq.upper_limit,eq.tissue "+
                           "from Chromosomes c, Affy_Exon_ProbeSet s "+
                           "left outer join expression_qtls eq on eq.identifier = TO_CHAR (s.probeset_id) "+
                           "where s.chromosome_id = c.chromosome_id "+
                           "and substr(c.name,1,2) = '"+chr+"'"+
                           "and ((s.psstart >= "+min+" and s.psstart <="+max+") OR "+
                           "(s.psstop >= "+min+" and s.psstop <= "+max+")) "+
                           "and s.psannotation <> 'transcript' " +
                           "and s.Array_TYPE_ID = "+arrayTypeID+" "+
                           "and eq.lod_score>2.5 "+
                           "order by eq.identifier";
         
         try{
             log.debug("SQL\n"+qtlQuery);
             PreparedStatement ps = dbConn.prepareStatement(qtlQuery);
             ResultSet rs = ps.executeQuery();
             while(rs.next()){
                 String psID=rs.getString(1);
                 double lod=rs.getDouble(2);
                 double pval=rs.getDouble(3);
                 double fdr=rs.getDouble(4);
                 String marker=rs.getString(5);
                 String marker_chr=rs.getString(6);
                 double marker_loc=rs.getDouble(7);
                 double lower=rs.getDouble(8);
                 double upper=rs.getDouble(9);
                 String tissue=rs.getString(10);
                 EQTL eqtl=new EQTL(psID,marker,marker_chr,marker_loc,tissue,lod,pval,fdr,lower,upper);
                 eqtls.add(eqtl);
                 if(!tissues.contains(tissue)){
                     tissues.add(tissue);;
                 }
             }
             ps.close();
             //log.debug("EQTL size:"+eqtls.size());
             log.debug("Tissue Size:"+tissues.size());
         }catch(SQLException e){
             log.error("Error retreiving EQTLs.",e);
         }
         return eqtls;
     }
     
     public HashMap getTransControlledFromEQTLs(int min,int max,String chr,int arrayTypeID,double pvalue,String level){
         if(chr.startsWith("chr")){
             chr=chr.substring(3);
         }
         HashMap transcriptClusters=new HashMap();
         String qtlQuery="select aep.transcript_cluster_id,c1.name,aep.strand,aep.psstart,aep.psstop,aep.pslevel, s.tissue,lse.pvalue, s.snp_name,c2.name,s.snp_start,s.snp_end,eq.LOD_SCORE "+
                             "from affy_exon_probeset aep, location_specific_eqtl lse, snps s, chromosomes c1,chromosomes c2, expression_QTLS eq "+
                             "where substr(c1.name,1,2)='"+chr+"' "+
                             "and ((aep.psstart >="+min+" and aep.psstart <="+max+") or (aep.psstop>="+min+" and aep.psstop <="+max+")or (aep.psstop<="+min+" and aep.psstop >="+max+")) "+
                             "and aep.psannotation = 'transcript' "+
                             "and aep.pslevel = '"+level+"' "+
                             "and aep.array_type_id="+arrayTypeID+" "+
                             "and lse.probe_id=aep.probeset_id "+
                             "and s.snp_id=lse.snp_id "+
                             "and lse.pvalue >= "+(-Math.log10(pvalue))+" "+
                             "and aep.chromosome_id=c1.chromosome_id "+
                             "and s.chromosome_id=c2.chromosome_id "+
                             "and TO_CHAR(aep.probeset_id)=eq.identifier (+) "+
                             "and (s.tissue=eq.tissue or eq.tissue is null) "+
                             "order by aep.probeset_id,s.tissue,s.chromosome_id,s.snp_start";
         
         try{
             log.debug("SQL eQTL FROM QUERY\n"+qtlQuery);
             PreparedStatement ps = dbConn.prepareStatement(qtlQuery);
             ResultSet rs = ps.executeQuery();
             
             TranscriptCluster curTC=null;
             while(rs.next()){
                 String tcID=rs.getString(1);
                 //log.debug("process:"+tcID);
                 String tcChr=rs.getString(2);
                 int tcStrand=rs.getInt(3);
                 long tcStart=rs.getLong(4);
                 long tcStop=rs.getLong(5);
                 String tcLevel=rs.getString(6);
                 
                 if(curTC==null||!tcID.equals(curTC.getTranscriptClusterID())){
                     if(curTC!=null){
                         transcriptClusters.put(curTC.getTranscriptClusterID(), curTC);
                     }
                     curTC=new TranscriptCluster(tcID,tcChr,Integer.toString(tcStrand),tcStart,tcStop,tcLevel);
                     //log.debug("create transcript cluster:"+tcID);
                 }
                 String tissue=rs.getString(7);
                 double pval=Math.pow(10, (-1*rs.getDouble(8)));
                 String marker_name=rs.getString(9);
                 String marker_chr=rs.getString(10);
                 long marker_start=rs.getLong(11);
                 long marker_end=rs.getLong(12);
                 double tcLODScore=rs.getDouble(13);
                 curTC.addEQTL(tissue,pval,marker_name,marker_chr,marker_start,marker_end,tcLODScore);
             }
             if(curTC!=null){
                 transcriptClusters.put(curTC.getTranscriptClusterID(), curTC);
             }
             ps.close();
             log.debug("Transcript Cluster Size:"+transcriptClusters.size());
         }catch(SQLException e){
             log.error("Error retreiving EQTLs.",e);
             e.printStackTrace(System.err);
         }
         return transcriptClusters;
     }
     
     public ArrayList<TranscriptCluster> getTransControllingEQTLs(int min,int max,String chr,int arrayTypeID,double pvalue,String level,String organism,String circosTissue,String circosChr){
         if(chr.startsWith("chr")){
             chr=chr.substring(3);
         }
         ArrayList<TranscriptCluster> transcriptClusters=new ArrayList<TranscriptCluster>();
         HashMap tmpHM=new HashMap();
         /*String qtlQuery="select aep.transcript_cluster_id,c2.name,aep.strand,aep.psstart,aep.psstop,aep.pslevel, s.tissue,lse.pvalue, s.snp_name,c.name,s.snp_start,s.snp_end,eq.LOD_SCORE "+
                             "from location_specific_eqtl lse, snps s, chromosomes c ,chromosomes c2, affy_exon_probeset aep, expression_qtls eq "+
                             "where s.snp_id=lse.snp_id "+
                             "and lse.pvalue>= "+(-Math.log10(pvalue))+" "+
                             "and substr(c.name,1,2)='"+chr+"' "+
                             "and ((s.snp_start>="+min+" and s.snp_start<="+max+") or (s.snp_end>="+min+" and s.snp_end<="+max+") or (s.snp_start<="+min+" and s.snp_end>="+max+")) "+
                             "and lse.probe_id=aep.probeset_id "+
                             "and aep.pslevel='"+level+"' "+
                             "and aep.psannotation='transcript' "+
                             "and aep.array_type_id="+arrayTypeID+" "+
                             "and TO_CHAR(aep.probeset_id) = eq.identifier (+) "+
                             "and (s.tissue=eq.tissue or eq.tissue is null) "+
                             "and s.chromosome_id=c.chromosome_id "+
                             "and c2.chromosome_id=aep.chromosome_id "+
                             "order by aep.transcript_cluster_id,s.tissue,aep.chromosome_id,aep.psstart";*/
         
         String qtlQuery="select aep.transcript_cluster_id,c2.name,aep.strand,aep.psstart,aep.psstop,aep.pslevel, s.tissue,lse.pvalue, s.snp_name,c.name,s.snp_start,s.snp_end "+
                             "from location_specific_eqtl lse, snps s, chromosomes c ,chromosomes c2, affy_exon_probeset aep "+
                             "where s.snp_id=lse.snp_id "+
                             "and lse.probe_id=aep.probeset_id "+
                             "and c2.chromosome_id=aep.chromosome_id "+
                             "and c.chromosome_id=s.chromosome_id "+
                             "and lse.pvalue>= "+(-Math.log10(pvalue))+" "+
                             "and aep.transcript_cluster_id in "+
                                 "(select aep.transcript_cluster_id "+
                                 "from location_specific_eqtl lse, snps s, chromosomes c1 , affy_exon_probeset aep "+
                                 "where s.snp_id=lse.snp_id "+
                                 "and lse.pvalue>= "+(-Math.log10(pvalue))+" "+
                                 "and ((s.snp_start>="+min+" and s.snp_start<="+max+") or (s.snp_end>="+min+" and s.snp_end<="+max+") or (s.snp_start<="+min+" and s.snp_end>="+max+")) "+
                                 "and s.chromosome_id=c1.chromosome_id "+
                                 "and substr(c1.name,1,2)='"+chr+"' "+
                                 "and lse.probe_id=aep.probeset_id "+
                                 "and aep.pslevel='"+level+"' "+
                                 "and aep.psannotation='transcript' "+
                                 "and aep.array_type_id="+arrayTypeID+") "+
                             "order by aep.transcript_cluster_id, s.tissue";
         
         String qtlQuery2="select aep.transcript_cluster_id,c2.name,aep.strand,aep.psstart,aep.psstop,aep.pslevel, s.tissue,lse.pvalue, s.snp_name,c.name,s.snp_start,s.snp_end,eq.LOD_SCORE "+
                             "from location_specific_eqtl lse, snps s, chromosomes c ,chromosomes c2, affy_exon_probeset aep, expression_qtls eq "+
                             "where s.snp_id=lse.snp_id "+
                             "and lse.pvalue< "+(-Math.log10(pvalue))+" "+
                             "and substr(c.name,1,2)='"+chr+"' "+
                             "and ((s.snp_start>="+min+" and s.snp_start<="+max+") or (s.snp_end>="+min+" and s.snp_end<="+max+") or (s.snp_start<="+min+" and s.snp_end>="+max+")) "+
                             "and lse.probe_id=aep.probeset_id "+
                             "and aep.pslevel='"+level+"' "+
                             "and aep.psannotation='transcript' "+
                             "and aep.array_type_id="+arrayTypeID+" "+
                             "and TO_CHAR(aep.probeset_id) = eq.identifier (+) "+
                             "and (s.tissue=eq.tissue or eq.tissue is null) "+
                             "and s.chromosome_id=c.chromosome_id "+
                             "and c2.chromosome_id=aep.chromosome_id "+
                             "order by aep.transcript_cluster_id,s.tissue,aep.chromosome_id,aep.psstart";
         
         try{
             log.debug("SQL eQTL FROM QUERY\n"+qtlQuery);
             PreparedStatement ps = dbConn.prepareStatement(qtlQuery);
             ResultSet rs = ps.executeQuery();
             
             TranscriptCluster curTC=null;
             while(rs.next()){
                 String tcID=rs.getString(1);
                 //log.debug("process:"+tcID);
                 String tcChr=rs.getString(2);
                 int tcStrand=rs.getInt(3);
                 long tcStart=rs.getLong(4);
                 long tcStop=rs.getLong(5);
                 String tcLevel=rs.getString(6);
                 
                 if(curTC==null||!tcID.equals(curTC.getTranscriptClusterID())){
                     if(curTC!=null){
                         tmpHM.put(curTC.getTranscriptClusterID(),curTC);
                         //transcriptClusters.add(curTC);
                     }
                     curTC=new TranscriptCluster(tcID,tcChr,Integer.toString(tcStrand),tcStart,tcStop,tcLevel);
                     //log.debug("create transcript cluster:"+tcID);
                 }
                 String tissue=rs.getString(7);
                 double pval=Math.pow(10, (-1*rs.getDouble(8)));
                 String marker_name=rs.getString(9);
                 String marker_chr=rs.getString(10);
                 long marker_start=rs.getLong(11);
                 long marker_end=rs.getLong(12);
                 //double tcLODScore=rs.getDouble(13);
                 if(marker_chr.equals(chr) && (marker_start>=min && marker_start<=max) || (marker_end>=min && marker_end<=max) || (marker_start<=min && marker_end>=max) ){
                     curTC.addRegionEQTL(tissue,pval,marker_name,marker_chr,marker_start,marker_end,-1);
                 }else{
                     curTC.addEQTL(tissue,pval,marker_name,marker_chr,marker_start,marker_end,-1);
                 }
             }
             if(curTC!=null){
                 tmpHM.put(curTC.getTranscriptClusterID(),curTC);
                 //transcriptClusters.add(curTC);
             }
             ps.close();
             ps = dbConn.prepareStatement(qtlQuery2);
             rs = ps.executeQuery();
             
             while(rs.next()){
                 String tcID=rs.getString(1);
                 String tissue=rs.getString(7);
                 double pval=Math.pow(10, (-1*rs.getDouble(8)));
                 String marker_name=rs.getString(9);
                 String marker_chr=rs.getString(10);
                 long marker_start=rs.getLong(11);
                 long marker_end=rs.getLong(12);
                 double tcLODScore=rs.getDouble(13);
                 if(tmpHM.containsKey(tcID)){
                     TranscriptCluster tmpTC=(TranscriptCluster)tmpHM.get(tcID);
                     tmpTC.addRegionEQTL(tissue,pval,marker_name,marker_chr,marker_start,marker_end,tcLODScore);
                 }
                 
             }
             
             ps.close();
             Set keys=tmpHM.keySet();
             Iterator itr=keys.iterator();
             try{
             BufferedWriter out=new BufferedWriter(new FileWriter(new File(outputDir+"transcluster.txt")));
             while(itr.hasNext()){
                 TranscriptCluster tmpC=(TranscriptCluster)tmpHM.get(itr.next().toString());
                 if(tmpC!=null){
                     if(tmpC.getTissueRegionEQTLs().size()>0){
                         transcriptClusters.add(tmpC);
                         String line=tmpC.getTranscriptClusterID()+"\t"+tmpC.getChromosome()+"\t"+tmpC.getStart()+"\t"+tmpC.getEnd()+"\t"+tmpC.getStrand()+"\n";
                         out.write(line);
                     }
                 }
             }
             out.flush();
             out.close();
             }catch(IOException e){
                 log.error("I/O Exception trying to output transcluster.txt file.",e);
             }
             
             File ensPropertiesFile = new File(ensemblDBPropertiesFile);
             Properties myENSProperties = new Properties();
             String ensHost="";
             String ensPort="";
             String ensUser="";
             String ensPassword="";
             try{
                 myENSProperties.load(new FileInputStream(ensPropertiesFile));
                 ensHost=myENSProperties.getProperty("HOST");
                 ensPort=myENSProperties.getProperty("PORT");
                 ensUser=myENSProperties.getProperty("USER");
                 ensPassword=myENSProperties.getProperty("PASSWORD");
             }catch(IOException e){
                 log.error("I/O Exception trying to read properties file.",e);
             }
             
             boolean error=false;
             String[] perlArgs = new String[9];
             perlArgs[0] = "perl";
             perlArgs[1] = perlDir + "writeGeneIDs.pl";
             perlArgs[2] = outputDir+"transcluster.txt";
             perlArgs[3] = outputDir+"TC_to_Gene.txt";
             if (organism.equals("Rn")) {
                 perlArgs[4] = "Rat";
             } else if (organism.equals("Mm")) {
                 perlArgs[4] = "Mouse";
             }
             perlArgs[5] = ensHost;
             perlArgs[6] = ensPort;
             perlArgs[7] = ensUser;
             perlArgs[8] = ensPassword;
 
 
             //set environment variables so you can access oracle pulled from perlEnvVar session variable which is a comma separated list
             String[] envVar=perlEnvVar.split(",");
 
             for (int i = 0; i < envVar.length; i++) {
                 log.debug(i + " EnvVar::" + envVar[i]);
             }
 
 
             //construct ExecHandler which is used instead of Perl Handler because environment variables were needed.
             myExec_session = new ExecHandler(perlDir, perlArgs, envVar, outputDir+"toGeneID");
 
             try {
 
                 myExec_session.runExec();
 
             } catch (ExecException e) {
                 error=true;
                 log.error("In Exception of run writeGeneIDs.pl Exec_session", e);
                 setError("Running Perl Script to match Transcript Clusters to Genes.");
                 Email myAdminEmail = new Email();
                 myAdminEmail.setSubject("Exception thrown in Exec_session");
                 myAdminEmail.setContent("There was an error while running "
                         + perlArgs[1] + " (" + perlArgs[2] +" , "+perlArgs[3]+" , "+perlArgs[4]+")\n\n"+myExec_session.getErrors());
                 try {
                     myAdminEmail.sendEmailToAdministrator((String) session.getAttribute("adminEmail"));
                 } catch (Exception mailException) {
                     log.error("error sending message", mailException);
                     throw new RuntimeException();
                 }
             }
 
             if(myExec_session.getExitValue()!=0){
                 error=true;
                 Email myAdminEmail = new Email();
                 myAdminEmail.setSubject("Exception thrown in Exec_session");
                 myAdminEmail.setContent("There was an error while running "
                         + perlArgs[1] + " (" + perlArgs[2] +" , "+perlArgs[3]+" , "+perlArgs[4]+
                         ")\n\n"+myExec_session.getErrors());
                 try {
                     myAdminEmail.sendEmailToAdministrator((String) session.getAttribute("adminEmail"));
                 } catch (Exception mailException) {
                     log.error("error sending message", mailException);
                     throw new RuntimeException();
                 }
             }
             if(!error){
                 try{
                     BufferedReader in = new BufferedReader(new FileReader(new File(outputDir+"TC_to_Gene.txt")));
                     while(in.ready()){
                         String line=in.readLine();
                         String[] tabs=line.split("\t");
                         String tcID=tabs[0];
                         String ensID=tabs[1];
                         String geneSym=tabs[2];
                         String sStart=tabs[3];
                         String sEnd=tabs[4];
                         String sOverlap=tabs[5];
                         String sOverlapG=tabs[6];
                         String description="";
                         if(tabs.length>7){
                             description=tabs[7];
                         }
                         if(tmpHM.containsKey(tcID)){
                             TranscriptCluster tmpTC=(TranscriptCluster)tmpHM.get(tcID);
                             tmpTC.addGene(ensID,geneSym,sStart,sEnd,sOverlap,sOverlapG,description);
                         }
                     }
                     in.close();
                     BufferedWriter out= new BufferedWriter(new FileWriter(new File(outputDir+"TranscriptClusterDetails.txt")));
                     for(int i=0;i<transcriptClusters.size();i++){
                         TranscriptCluster tc=transcriptClusters.get(i);
                         HashMap hm=tc.getTissueRegionEQTLs();
                         Set key=hm.keySet();
                         Object[] tissue=key.toArray();
                         for(int j=0;j<tissue.length;j++){
                             String line="";
                             ArrayList<EQTL> tmpEQTLArr=(ArrayList<EQTL>)hm.get(tissue[j].toString());
                             if(tmpEQTLArr!=null && tmpEQTLArr.size()>0){
                                 EQTL tmpEQTL=tmpEQTLArr.get(0);
                                 if(tmpEQTL.getMarkerChr().equals(chr) && 
                                         ((tmpEQTL.getMarker_start()>=min && tmpEQTL.getMarker_start()<=max) || 
                                         (tmpEQTL.getMarker_end()>=min && tmpEQTL.getMarker_end()<=max) || 
                                         (tmpEQTL.getMarker_start()<=min && tmpEQTL.getMarker_end()>=max))
                                         ){
                                     line=tmpEQTL.getMarkerName()+"\t"+tmpEQTL.getMarkerChr()+"\t"+tmpEQTL.getMarker_start();
                                     line=line+"\t"+tc.getTranscriptClusterID()+"\t"+tc.getChromosome()+"\t"+tc.getStart()+"\t"+tc.getEnd();
                                     String tmpGeneSym=tc.getGeneSymbol();
                                     if(tmpGeneSym==null||tmpGeneSym.equals("")){
                                         tmpGeneSym=tc.getGeneID();
                                     }
                                     if(tmpGeneSym==null||tmpGeneSym.equals("")){
                                         tmpGeneSym=tc.getTranscriptClusterID();
                                     }
                                     line=line+"\t"+tmpGeneSym+"\t"+tissue[j].toString()+"\t"+tmpEQTL.getNegLogPVal()+"\n";
                                     out.write(line);
                                 }
                             }
                         }
                         
                     }
                     out.close();
                 }catch(IOException e){
                     log.error("Error reading Gene - Transcript IDs.",e);
                 }
                circosTissue=circosTissue.replaceAll(";;", ";");
                circosChr=circosChr.replaceAll(";;", ";");
                 //run circos scripts
                 boolean errorCircos=false;
                 perlArgs = new String[7];
                 perlArgs[0] = "perl";
                 perlArgs[1] = perlDir + "callCircosReverse.pl";
                 perlArgs[2] = Double.toString(-Math.log10(pvalue));
                 perlArgs[3] = organism;
                 perlArgs[4] = outputDir.substring(0,outputDir.length()-1);
                 perlArgs[5] = circosTissue;
                 perlArgs[6] = circosChr;
 
                 //set environment variables so you can access oracle pulled from perlEnvVar session variable which is a comma separated list
                 
                 for (int i = 0; i < perlArgs.length; i++) {
                     log.debug(i + " perlArgs::" + perlArgs[i]);
                 }
                 
                 for (int i = 0; i < envVar.length; i++) {
                     log.debug(i + " EnvVar::" + envVar[i]);
                 }
 
 
                 //construct ExecHandler which is used instead of Perl Handler because environment variables were needed.
                 myExec_session = new ExecHandler(perlDir, perlArgs, envVar, outputDir+"circos_"+pvalue);
 
                 try {
 
                     myExec_session.runExec();
 
                 } catch (ExecException e) {
                     error=true;
                     log.error("In Exception of run callCircosReverse.pl Exec_session", e);
                     setError("Running Perl Script to match create circos plot.");
                     Email myAdminEmail = new Email();
                     myAdminEmail.setSubject("Exception thrown in Exec_session");
                     myAdminEmail.setContent("There was an error while running "
                             + perlArgs[1] + " (" + perlArgs[2] +" , "+perlArgs[3]+" , "+perlArgs[4]+")\n\n"+myExec_session.getErrors());
                     try {
                         myAdminEmail.sendEmailToAdministrator((String) session.getAttribute("adminEmail"));
                     } catch (Exception mailException) {
                         log.error("error sending message", mailException);
                         throw new RuntimeException();
                     }
                 }
                 
             }
             log.debug("Transcript Cluster Size:"+transcriptClusters.size());
         }catch(SQLException e){
             log.error("Error retreiving EQTLs.",e);
             e.printStackTrace(System.err);
         }
         return transcriptClusters;
     }
     
     public ArrayList<BQTL> getBQTLs(int min,int max,String chr,String organism){
         if(chr.startsWith("chr")){
             chr=chr.substring(3);
         }
         ArrayList<BQTL> bqtl=new ArrayList<BQTL>();
         String query="select pq.*,c.name from public_qtls pq, chromosomes c "+
                         "where pq.organism='"+organism+"' "+
                         "and ((pq.qtl_start>="+min+" and pq.qtl_start<="+max+") or (pq.qtl_end>="+min+" and pq.qtl_end<="+max+") or (pq.qtl_start<="+min+" and pq.qtl_end>="+max+")) "+
                         "and substr(c.name,1,2)='"+chr+"' "+
                         "and c.chromosome_id=pq.chromosome";
         try{ 
         try{
             log.debug("SQL eQTL FROM QUERY\n"+query);
             PreparedStatement ps = dbConn.prepareStatement(query);
             ResultSet rs = ps.executeQuery();
             while(rs.next()){
                 String id=Integer.toString(rs.getInt(1));
                 String mgiID=rs.getString(2);
                 String rgdID=rs.getString(3);
                 String symbol=rs.getString(5);
                 String name=rs.getString(6);
                 double lod=rs.getDouble(8);
                 double pvalue=rs.getDouble(9);
                 String trait=rs.getString(10);
                 String subTrait=rs.getString(11);
                 String traitMethod=rs.getString(12);
                 String phenotype=rs.getString(13);
                 String diseases=rs.getString(14);
                 String rgdRef=rs.getString(15);
                 String pubmedRef=rs.getString(16);
                 String relQTLs=rs.getString(17);
                 String candidGene=rs.getString(18);
                 long start=rs.getLong(19);
                 long stop=rs.getLong(20);
                 String mapMethod=rs.getString(21);
                 String chromosome=rs.getString(22);
                 BQTL tmpB=new BQTL(id,mgiID,rgdID,symbol,name,trait,subTrait,traitMethod,phenotype,diseases,rgdRef,pubmedRef,mapMethod,relQTLs,candidGene,lod,pvalue,start,stop,chromosome);
                 bqtl.add(tmpB);
             }
             ps.close();
             
         }catch(SQLException e){
             log.error("Error retreiving bQTLs.",e);
             e.printStackTrace(System.err);
         }
         }catch(Exception er){
             er.printStackTrace(System.err);
         }
         return bqtl;
     }
     
     public void addQTLS(ArrayList<Gene> genes, ArrayList<EQTL> eqtls){
         HashMap eqtlInd=new HashMap();
         for(int i=0;i<eqtls.size();i++){
             EQTL tmp=eqtls.get(i);
             eqtlInd.put(tmp.getProbeSetID(), i);
         }
         for(int i=0;i<genes.size();i++){
             genes.get(i).addEQTLs(eqtls,eqtlInd,log);
         }
     }
     
     public void addFromQTLS(ArrayList<Gene> genes, HashMap transcriptClustersCore,HashMap transcriptClustersExt,HashMap transcriptClustersFull){
         for(int i=0;i<genes.size();i++){
             if(genes.get(i).getGeneID().startsWith("ENS")){
                 genes.get(i).addTranscriptCluster(transcriptClustersCore,transcriptClustersExt,transcriptClustersFull,log);
             }
         }
     }
     
 }
 
 
 class RegionDirFilter implements FileFilter{
     String toCheck="";
     
     RegionDirFilter(String toCheck){
         this.toCheck=toCheck;
     }
     
     public boolean accept(File file) {
         boolean ret=true;
         if(!file.isDirectory()){
             ret=false;
         }
         if(!file.getName().startsWith(toCheck)){
             ret=false;
         }
         return ret;
     }
     
 }
