 package com.github.npcompete;
 
 import org.w3c.dom.Document;
 
 import java.io.File;
 
 import static com.github.npcompete.BMLogger.print;
 import static com.github.npcompete.BMLogger.Severe;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Bijesh
  * Date: 18/8/13
  * Time: 6:47 PM
  * To change this template use File | Settings | File Templates.
  */
 public class BharathMatrimonyAntTask {
 
     private BMProject project;
 
      private BharathMatrimonyAntTask(String projectPath,String currentPackage,String intentPackage){
 //         JOptionPane.showMessageDialog(null,"executed from jar");
 //         scanForManifest("");
            execute(projectPath,currentPackage,intentPackage);
      }
 
     private void execute(String projectPath,String currentPackage,String intentPackage){
 
           String fileSeparator =  fileSeparator();
           File manifestFile = getManifestFile(projectPath,fileSeparator);
           project = constructBMProject(projectPath,manifestFile, currentPackage, intentPackage, systemOS(), fileSeparator);
 
          renamePackageNameInManifest(project,intentPackage);
           parseSourceAndResources(project);
           renameFolder(project);
 
           print(Severe.HIGH,"BM Automation completed successfully!!!!");
     }
 
     private void renameFolder(BMProject project){
          File currentpackageFolder = new File(project.getRootPath()+project.getFileSeparator()+"src"+project.getFileSeparator()+"com"+project.getFileSeparator()+getPackageName(project.getCurrentPackageName()));
          File intentpackageFolder = new File(project.getRootPath()+project.getFileSeparator()+"src"+project.getFileSeparator()+"com"+project.getFileSeparator()+getPackageName(project.getIntentedPackageName()));
          currentpackageFolder.renameTo(intentpackageFolder);
     }
 
     private String getPackageName(String pack){
         String retVal = "";
         String[] vals = pack.split("\\.");
         retVal = vals[vals.length-1];
         return retVal;
     }
 
 
     private void renamePackageNameInManifest(BMProject project,String intentPackage){
           BMUtility.setAttributeValue(project.getManifestFile(),"manifest","package",intentPackage);
     }
 
 
     private void parseSourceAndResources(BMProject project){
         parsingSource(project);
         parsingResource(project);
         parsingManifest(project);
     }
     private void parsingSource(BMProject project){
         traverse(new File(project.getRootPath()+project.getFileSeparator()+"src"),".java",project.getCurrentPackageName(),project.getIntentedPackageName());
     }
     private void parsingResource(BMProject project){
         traverse(new File(project.getRootPath()+project.getFileSeparator()+"res"),".xml",project.getCurrentPackageName(),project.getIntentedPackageName());
     }
     private void parsingManifest(BMProject project){
          BMUtility.renamePackage(project.getManifestFile(),project.getCurrentPackageName(),project.getIntentedPackageName());
     }
 
     private void traverse(File f,String fileType,String currentName,String intentName){
         File[] files = f.listFiles();
         for(File file:files){
             if(file.isDirectory()){
                 traverse(file,fileType,currentName,intentName);
             }else if(file.getName().endsWith(fileType)){
                 BMUtility.renamePackage(file,currentName,intentName);
             }
         }
     }
 
     private String systemOS(){
         return System.getProperty("os.name");
     }
 
     private String fileSeparator(){
         return System.getProperty("file.separator");
     }
 
     /**
      *   now this is hard coded later get the project path and append the manifest file
      * @return file
      */
     private File getManifestFile(String projectPath,String fileSeparator){
 
          return new File(projectPath+fileSeparator+"AndroidManifest.xml");
     }
 
 
 
     /**
      * This method will construct the project model which contains all the needed information about the project
      */
     private BMProject constructBMProject(String projectPath,File manifestFile, String currentPackage, String intentPackage, String os, String fileSeparator){
 
            String currentPackageInManifest = getCurrentPackagename(manifestFile);
            if(intentPackage.equals(currentPackageInManifest)){
               print(Severe.HIGH, "The pacakage name in the manifest is same as argument so automation will exit ");
               System.exit(0);
            }
            return new BMProject(projectPath,manifestFile,currentPackage,intentPackage,os,fileSeparator);
     }
 
 
 
     private String getCurrentPackagename(File manifestFile){
         String retVal = "";
 
         print(Severe.LOW,"in getCurrentPackage...");
 
         Document doc = BMUtility.getDocument(manifestFile);
         retVal = BMUtility.getAttributeValue(doc,"manifest","package");
 
 
         return retVal;
     }
 
     /**
      * This method will scan for AndoridManifest.xml in the project's root directory.
      * This method is not implemented now add functionality of the same which is done for ArtisanSupport tools.
      */
     private boolean scanForManifest(String projectPath){
         boolean returnFlag = true;
 
         return returnFlag;
     }
     private String appendLanguage(BMProject androidProject){
          return androidProject.getCurrentPackageName().concat(androidProject.getLanguageToBuild());
     }
       public static void main(String[] str){
 //          str = new String[]{"/home/npcompete/TempBiju/TestBMAutomation/bm_all_langs","com.bharatmatrimony","com.bharatmatrimony_tamil"};
           new BharathMatrimonyAntTask(str[0],str[1],str[2]);
       }
 
 }
