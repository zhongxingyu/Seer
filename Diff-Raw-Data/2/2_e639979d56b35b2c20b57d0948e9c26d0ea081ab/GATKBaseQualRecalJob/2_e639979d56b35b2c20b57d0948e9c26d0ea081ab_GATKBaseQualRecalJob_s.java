 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package bwa_picard_gatk_pipeline.sge.gatk.baseQualRecalJob;
 
 import bwa_picard_gatk_pipeline.GlobalConfiguration;
 import bwa_picard_gatk_pipeline.sge.Job;
 import java.io.File;
 import java.io.IOException;
 import org.apache.commons.io.FilenameUtils;
 
 /**
  *
  * @author Wim Spee
  */
 public class GATKBaseQualRecalJob extends Job {
     
     private String sgeName;    
     private File realignedBam;
     private File bqsrBam;   
     
     private GlobalConfiguration gc;
     
 
     public GATKBaseQualRecalJob(File realignedBam, File bqsrBam, GlobalConfiguration gc) throws IOException {        
         
         super(FilenameUtils.removeExtension(realignedBam.getAbsolutePath()) + "_bqsr.sh");        
         this.gc = gc;        
         this.realignedBam = realignedBam;
         this.bqsrBam = bqsrBam;
         
         sgeThreads = gc.getGatkSGEThreads();
         
         addCommands();
         
         
         sgeName = "bqsr"+realignedBam.getName();
         close();
     }
     
     
     
     
     @Override    
     public String getSGEName() {
         return sgeName;
     }
     
     private void addCommands() throws IOException 
     {
         String baseName = FilenameUtils.getBaseName(realignedBam.getAbsolutePath()); 
         File logFile = new File(realignedBam.getParentFile(), baseName + "_bqsr.log");  
        // File tmpDir = new File("/tmp", baseName);          
         
        File recalibrationReport = new File(FilenameUtils.removeExtension(realignedBam.getAbsolutePath()) + "_ recalibration_report.grp"); 
         
         String appendAlloutputToLog = " >> "+ logFile.getAbsolutePath() + " 2>&1";
                
         //add sge hostname and date information to log
         addCommand("uname -n >> " + logFile.getAbsolutePath());
         addCommand("\n");
         addCommand("date >> " + logFile.getAbsolutePath());
         addCommand("\n");
         
 //        //create a tmp dir
 //        addCommand("mkdir " + tmpDir);
 //        addCommand("\n");
           
           
           String knownVariants = "";
             if(gc.getBqsrKnownVariants()!= null)
             {
                 for(File knownVariantFile : gc.getBqsrKnownVariants())
                 {
                  knownVariants = knownVariants + " -knownSites "+knownVariantFile.getAbsolutePath();
                 }
             
             }        
           
           
          
           
 //       
         addCommand( "java "+
                     " -Xmx"+gc.getGatkSGEMemory()+"G"+
                     " -jar "+gc.getGatk().getAbsolutePath() +
                     " -T BaseRecalibrator " +
                     " -R "+gc.getReferenceFile().getAbsolutePath()+                  
                     " -I "+realignedBam.getAbsolutePath()+ 
                     " -o "+recalibrationReport.getAbsolutePath()+   
                     knownVariants+        
                     appendAlloutputToLog);        
         
     
         
         addCommand("\n");
         
        
         
         addCommand( "java "+
                     " -Xmx"+gc.getGatkSGEMemory()+"G"+
                     " -jar "+gc.getGatk().getAbsolutePath() +
                     " -T PrintReads " +
                     " -R "+gc.getReferenceFile().getAbsolutePath()+                  
                     " -I "+realignedBam.getAbsolutePath()+ 
                     " -BQSR "+recalibrationReport.getAbsolutePath()+
                     " -o "+bqsrBam.getAbsolutePath()+
                     appendAlloutputToLog);        
         
     
         
         addCommand("\n");
         //remove the tmp dir from the sge host
       //  addCommand("rm -rf " + tmpDir.getAbsolutePath() + appendAlloutputToLog);
     //    addCommand("\n");
         addCommand("echo finished " + appendAlloutputToLog);
         addCommand("date " + appendAlloutputToLog);
                  
     
     }
     
     
 }
