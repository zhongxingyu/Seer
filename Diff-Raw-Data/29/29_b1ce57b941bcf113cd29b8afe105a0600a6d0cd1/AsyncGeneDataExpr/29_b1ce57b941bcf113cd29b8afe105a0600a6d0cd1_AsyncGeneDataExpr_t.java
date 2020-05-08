 package edu.ucdenver.ccp.PhenoGen.tools.analysis;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import javax.mail.MessagingException;
 import javax.mail.SendFailedException;
 
 import javax.servlet.http.HttpSession;
 
 import edu.ucdenver.ccp.util.Debugger;
 import edu.ucdenver.ccp.PhenoGen.data.Dataset;
 import edu.ucdenver.ccp.PhenoGen.data.Dataset.DatasetVersion;
 import edu.ucdenver.ccp.PhenoGen.data.User;
 import edu.ucdenver.ccp.PhenoGen.driver.ExecHandler;
 import edu.ucdenver.ccp.PhenoGen.web.mail.Email;
 import edu.ucdenver.ccp.PhenoGen.tools.analysis.Statistic;
 import edu.ucdenver.ccp.PhenoGen.driver.RException;
 import edu.ucdenver.ccp.PhenoGen.driver.R_session;
 import edu.ucdenver.ccp.util.ObjectHandler;
 
 /* for handling exceptions in Threads */
 import au.com.forward.threads.ThreadReturn;
 import au.com.forward.threads.ThreadInterruptedException;
 import au.com.forward.threads.ThreadException;
 import java.io.*;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.logging.Level;
 
 
 import org.apache.log4j.Logger;
 
 
 /* for logging messages */
 import org.apache.log4j.Logger;
 
 public class AsyncGeneDataExpr extends Thread {
         private String[] rErrorMsg = null;
         Statistic myStatistic=null;
         HttpSession session=null;
         private Logger log = null;
         private String perlDir = "", fullPath = "";
         private String rFunctDir = "";
         private String userFilesRoot = "";
         private String urlPrefix = "";
         private String perlEnvVar="";
         private String ucscDir="";
         private String bedDir="";
         private String ver="";
         private Connection dbConn = null;
         String outputDir="";
         String pListFile="";
         boolean doneThread=false;
 
         int maxThreadCount=1;
         
         
         ArrayList<String> DSPathList=new ArrayList<String>();
         ArrayList<String> sampleFileList=new ArrayList<String>();
         ArrayList<String> groupFileList=new ArrayList<String>();
         ArrayList<String> outGroupFileList=new ArrayList<String>();
         ArrayList<String> outIndivFileList=new ArrayList<String>();
         ArrayList<String> tissueList=new ArrayList<String>();
         ArrayList<String> platformList=new ArrayList<String>();
         AsyncGeneDataTools prevThread;
         ArrayList<Thread> threadList;
         BufferedWriter outGroup;
         BufferedWriter outIndiv;
         SyncAndClose sac;
        
         
     public AsyncGeneDataExpr(HttpSession inSession,String pListFile,String outputDir,AsyncGeneDataTools prevThread,ArrayList<Thread> threadList,int maxThreadCount,BufferedWriter outGroup,BufferedWriter outIndiv,SyncAndClose sac,String ver ) {
                 this.session = inSession;
                 this.pListFile=pListFile;
                 this.outputDir=outputDir;
                 log = Logger.getRootLogger();
                 log.debug("in GeneDataTools.setSession");
                 this.session = inSession;
                 this.prevThread=prevThread;
                 this.threadList=threadList;
                 this.maxThreadCount=maxThreadCount;
                 this.outGroup=outGroup;
                 this.outIndiv=outIndiv;
                 this.sac=sac;
                 this.ver=ver;
 
                 log.debug("AsyncGeneDataExpr Start");
 
                 //this.selectedDataset = (Dataset) session.getAttribute("selectedDataset");
                 //this.selectedDatasetVersion = (Dataset.DatasetVersion) session.getAttribute("selectedDatasetVersion");
                 //this.publicDatasets = (Dataset[]) session.getAttribute("publicDatasets");
                 this.dbConn = (Connection) session.getAttribute("dbConn");
                 //log.debug("db");
                 this.perlDir = (String) session.getAttribute("perlDir") + "scripts/";
                 //log.debug("perl" + perlDir);
                 String contextRoot = (String) session.getAttribute("contextRoot");
                 //log.debug("context" + contextRoot);
                 String appRoot = (String) session.getAttribute("applicationRoot");
                 //log.debug("app" + appRoot);
                 this.fullPath = appRoot + contextRoot;
                 //log.debug("fullpath");
                 this.rFunctDir = (String) session.getAttribute("rFunctionDir");
                 //log.debug("rFunction");
                 this.userFilesRoot = (String) session.getAttribute("userFilesRoot");
                 //log.debug("userFilesRoot");
                 this.urlPrefix = (String) session.getAttribute("mainURL");
                 if (urlPrefix.endsWith(".jsp")) {
                     urlPrefix = urlPrefix.substring(0, urlPrefix.lastIndexOf("/") + 1);
                 }
                 //log.debug("mainURL");
                 this.perlEnvVar = (String) session.getAttribute("perlEnvVar");
                 //log.debug("PerlEnv");
                 this.ucscDir = (String) session.getAttribute("ucscDir");
                 //log.debug("ucsc");
                 this.bedDir = (String) session.getAttribute("bedDir");
                 //log.debug("bedDir");
                 myStatistic = new Statistic();
                 myStatistic.setSession(session);
                 myStatistic.setRFunctionDir(this.rFunctDir);
     }
     
     public void add(String DSPath,String sampleFile,String groupFile,String outGroupFile,String outIndivFile,String tissue,String platform) {
         DSPathList.add(DSPath);
         sampleFileList.add(sampleFile);
         groupFileList.add(groupFile);
         outGroupFileList.add(outGroupFile);
         outIndivFileList.add(outIndivFile);
         tissueList.add(tissue);
         platformList.add(platform);
     }
 
     /*public void run() throws RuntimeException {
         Thread thisThread = Thread.currentThread();
         boolean done=prevThread.isDone();
         log.debug("WAITING PREVTHREAD");
         while(!done){
             try{
                 //log.debug("WAITING PREVTHREAD");
                 thisThread.sleep(5000);
             }catch(InterruptedException er){
                 log.error("wait interrupted",er);
             }
             done=prevThread.isDone();
         }
         log.debug("Done Waiting Starting");
         Date start=new Date();
         try{
             File indivf=new File(outputDir+"Panel_Expr_indiv_tmp.txt");
             File groupf=new File(outputDir+"Panel_Expr_group_tmp.txt");
             File indivfinal=new File(outputDir+"Panel_Expr_indiv.txt");
             File groupfinal=new File(outputDir+"Panel_Expr_group.txt");
             
             BufferedWriter outGroup=new BufferedWriter(new FileWriter(groupf));
             BufferedWriter outIndiv=new BufferedWriter(new FileWriter(indivf));
             while (!DSPathList.isEmpty()) {
                 String sampleFile=sampleFileList.remove(0);
                 String DSPath=DSPathList.remove(0);
                 String outIndivFile=outIndivFileList.remove(0);
                 String outGroupFile=outGroupFileList.remove(0);
                 String groupFile=groupFileList.remove(0);
                 String tissue=tissueList.remove(0);
                 String platform=platformList.remove(0);
                 String outGroupPath=outputDir+outGroupFile;
                 String outIndivPath=outputDir+outIndivFile;
                 try {
                     myStatistic.callOutputRawSpecificGeneBoth(platform,
                             DSPath,
                             "v3",
                             sampleFile,
                             outputDir + "tmp_psList.txt",
                             outIndivFile,
                             outGroupFile,
                             outputDir);
                     HashMap groupNum = readGroups(groupFile);
                     processGroupFile(outGroupPath, groupNum, tissue, outGroup);
                     processIndivFile(outIndivPath, tissue, outIndiv);
                     File group = new File(outGroupPath);
                     group.delete();
                     File indiv = new File(outIndivPath);
                     indiv.delete();
                     
                 } catch (RException e) {
                     log.error("Error outputing Avg Values", e);
                     Email myAdminEmail = new Email();
                         myAdminEmail.setSubject("Exception in R_session: output.Raw.Specific.Gene.Both");
                         myAdminEmail.setContent("There was an error while running output.Raw.Specific.Gene.Both("
                                 + ") from AsyncGeneDataExpr Thread.\n");
                         try {
                             myAdminEmail.sendEmailToAdministrator();
                         } catch (Exception mailException) {
                             log.error("error sending message", mailException);
                             throw new RuntimeException();
                         }
                 }
                 R_session myR_session = new R_session();
                 //construct call to R
                 String[] rArgs = new String[5];
                 rArgs[0] = "InputFile = '"+DSPath+"'";
                 rArgs[1] = "Version = 'v3'";
                 rArgs[2] = "SampleFile = '"+sampleFile+ "'";
                 rArgs[3] = "XMLFileName = '" +outputDir + "Gene.xml'";
                 rArgs[4] = "plotFileName ='" + outputDir + tissue+"_exonCorHeatMap.txt'";
 
                 try {
                     //Call R
                     if ((rErrorMsg = myR_session.callR(this.rFunctDir, "Affymetrix.Exon.HeatMap", rArgs, outputDir, -99)) != null) {
                         String errorMsg = new ObjectHandler().getAsSeparatedString(rErrorMsg, "<BR>");
                         log.debug("after R call for ExonHeatMap, got errorMsg. It is " + errorMsg);
                         throw new RException(errorMsg);
                     } //else {
                       //  completedSuccessfully = true;
                     //}
                 } catch (RException er) {
                     log.error("In Exception of run getExonHeatMapData R_session", er);
                     Email myAdminEmail = new Email();
                     myAdminEmail.setSubject("Exception thrown in R_session");
                     myAdminEmail.setContent("There was an error while running Affymetrix.Exon.HeatMap.R_HeatMapCorrData("
                             + rArgs[0] + " , " + rArgs[1] + " , " + rArgs[2] + " , " + rArgs[3] + " , " + rArgs[4] + ") from AsyncGeneDataExpr Thread.\n");
                     try {
                         myAdminEmail.sendEmailToAdministrator();
                     } catch (Exception mailException) {
                         log.error("error sending message", mailException);
                         throw new RuntimeException();
                     }
                 }
             }
             outGroup.close();
             outIndiv.close();
             groupf.renameTo(groupfinal);
             indivf.renameTo(indivfinal);
             Date end=new Date();
             try{
                 PreparedStatement ps=dbConn.prepareStatement(updateSQL, 
                                                     ResultSet.TYPE_SCROLL_INSENSITIVE,
                                                     ResultSet.CONCUR_UPDATABLE);
                 long returnTimeMS=end.getTime()-start.getTime();
                 ps.setLong(1, returnTimeMS);
                 ps.setString(2, "AsyncGeneDataExpr completed successfully");
                 ps.setInt(3, usageID);
                 ps.executeUpdate();
                 ps.close();
             }catch(SQLException ex){
                 log.error("Error saving AsyncGeneDataExpr Timing",ex);
             }
         }catch(IOException e){
             Date end=new Date();
             try{
                 PreparedStatement ps=dbConn.prepareStatement(updateSQL, 
                                                     ResultSet.TYPE_SCROLL_INSENSITIVE,
                                                     ResultSet.CONCUR_UPDATABLE);
                 long returnTimeMS=end.getTime()-start.getTime();
                 ps.setLong(1, returnTimeMS);
                 ps.setString(2, "AsyncGeneDataExpr had errors:"+e.getMessage());
                 ps.setInt(3, usageID);
                 ps.executeUpdate();
                 ps.close();
             }catch(SQLException ex){
                 log.error("Error saving AsyncGeneDataExpr Timing",ex);
             }
         }
     }*/
     
     
     public void run() throws RuntimeException {
         doneThread=false;
         Thread thisThread = Thread.currentThread();
         boolean done=prevThread.isDone();
         log.debug("WAITING PREVTHREAD");
         while(!done){
             try{
                 //log.debug("WAITING PREVTHREAD");
                 thisThread.sleep(5000);
             }catch(InterruptedException er){
                 log.error("wait interrupted",er);
             }
             done=prevThread.isDone();
         }
         log.debug("Done Waiting Starting");
         //wait for other Expr threads to finish
         boolean waiting=true;
         while(waiting){
             int waitingOnCount=0;
             boolean reachedMySelf=false;
             for(int i=0;i<threadList.size()&&!reachedMySelf;i++){
                 if(thisThread.equals(threadList.get(i))){
                     reachedMySelf=true;
                 }else{
                     if(threadList.get(i).isAlive()){
                         waitingOnCount++;
                     }
                 }
             }
             if(waitingOnCount<maxThreadCount){
                 waiting=false;
             }else{
                 try{
                     //log.debug("WAITING PREVTHREAD");
                     thisThread.sleep(5000);
                 }catch(InterruptedException er){
                     log.error("wait interrupted",er);
                 }
             }
         }
         Date start=new Date();
         //try{
             
             
             
             int loopcount=0;
             
             while (!DSPathList.isEmpty()  && loopcount<6) {
                 String psListFile=outputDir + "tmp_psList.txt";
                 File psFile=new File(psListFile);
                 if(psFile.length()>0){
                     loopcount=0;
                     String sampleFile=sampleFileList.remove(0);
                     String DSPath=DSPathList.remove(0);
                     String outIndivFile=outIndivFileList.remove(0);
                     String outGroupFile=outGroupFileList.remove(0);
                     String groupFile=groupFileList.remove(0);
                     String tissue=tissueList.remove(0);
                     String tissueNoSpaces=tissue.replaceAll(" ", "_");
                     String platform=platformList.remove(0);
                     String outGroupPath=outputDir+outGroupFile;
                     String outIndivPath=outputDir+outIndivFile;
                     try {
                         myStatistic.callHeatMapOutputRawSpecificBoth(platform,
                                 DSPath,
                                 ver,
                                 sampleFile,
                                 outputDir + "tmp_psList.txt",
                                 outIndivFile,
                                 outGroupFile,
                                 outputDir,
                                 //outputDir + "Gene.xml",
                                 outputDir + tissueNoSpaces +"_exonCorHeatMap.txt",
                                 thisThread.getId());
 
                         sac.processFiles(this,groupFile,outGroupPath,outIndivPath,tissue);
                         doneThread=true;
                         sac.done(this,"AsyncGeneDataExpr completed normally");
                     } catch (RException e) {
                         doneThread=true;
                         sac.done(this,"AsyncGeneDataExpr("+tissue+","+thisThread.getId()+") had errors.");
                         log.error("Error outputing Avg Values", e);
                         Email myAdminEmail = new Email();
                             myAdminEmail.setSubject("Exception in R_session: output.Raw.Specific.Gene.Both");
                             myAdminEmail.setContent("There was an error while running output.Raw.Specific.Gene.Both("+tissue+","+thisThread.getId()+","+
                                     DSPath+","+sampleFile+","+outputDir+
                                     ") from AsyncGeneDataExpr Thread.\n");
                             try {
                                 myAdminEmail.sendEmailToAdministrator((String) session.getAttribute("adminEmail"));
                             } catch (Exception mailException) {
                                 log.error("error sending message", mailException);
                                 throw new RuntimeException();
                             }
                     }
                 }else{
                    
                    log.debug("NO PROBESETS:"+psListFile);
                     try {
                         thisThread.wait(20000);
                     } catch (InterruptedException ex) {
                         log.error("Error NO PROBESETS",ex);
                         //Logger.getLogger(AsyncGeneDataExpr.class.getName()).log(Level.SEVERE, null, ex);
                     }
                 }
                 loopcount++;
             }
             
             
         /*}catch(IOException e){
             sac.done(this, "AsyncGeneDataExpr had errors:"+e.getMessage());
             Date end=new Date();
             try{
                 PreparedStatement ps=dbConn.prepareStatement(updateSQL, 
                                                     ResultSet.TYPE_SCROLL_INSENSITIVE,
                                                     ResultSet.CONCUR_UPDATABLE);
                 long returnTimeMS=end.getTime()-start.getTime();
                 ps.setLong(1, returnTimeMS);
                 ps.setString(2, "AsyncGeneDataExpr had errors:"+e.getMessage());
                 ps.setInt(3, usageID);
                 ps.executeUpdate();
                 ps.close();
             }catch(SQLException ex){
                 log.error("Error saving AsyncGeneDataExpr Timing",ex);
             }
         }*/
     }
 
     public boolean isDone() {
         return doneThread;
     }
     
     
     
 }
  
