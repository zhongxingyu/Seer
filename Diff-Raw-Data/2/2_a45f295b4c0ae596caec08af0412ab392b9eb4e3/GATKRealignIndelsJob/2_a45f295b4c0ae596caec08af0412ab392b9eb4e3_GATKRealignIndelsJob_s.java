 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package bwa_picard_gatk_pipeline.sge;
 
 import bwa_picard_gatk_pipeline.GlobalConfiguration;
 import java.io.File;
 import java.io.IOException;
 import org.apache.commons.io.FilenameUtils;
 
 /**
  *
  * @author wim
  */
 public class GATKRealignIndelsJob extends Job {
     
     private String sgeName;   
     private File dedupBam;   
     private GlobalConfiguration gc;
     
 
     public GATKRealignIndelsJob(File dedupBam, GlobalConfiguration gc) throws IOException {        
         
         super(FilenameUtils.removeExtension(dedupBam.getAbsolutePath()) + "_createRealignTargets.sh");        
         this.gc = gc;
         this.dedupBam = dedupBam;
         
         addCommands();
         
         
         sgeName = "createRealignTarget_"+dedupBam.getName();
        
     }
     
     
     
     
     @Override    
     public String getSGEName() {
         return sgeName;
     }
     
     private void addCommands() throws IOException 
     {
         String baseName = FilenameUtils.getBaseName(dedupBam.getAbsolutePath()); 
         File logFile = new File(dedupBam.getParentFile(), baseName + "createRealignTargets.log");  
         File tmpDir = new File("/tmp", baseName);    
         File realignTarget = new File(FilenameUtils.removeExtension(dedupBam.getAbsolutePath()) + "_realignTargets.intever"); 
         
                
         //add sge hostname and date information to log
         addCommand("uname -n >> " + logFile.getAbsolutePath());
         addCommand("\n");
         addCommand("date >> " + logFile.getAbsolutePath());
         addCommand("\n");
         
         //create a tmp dir
         addCommand("mkdir " + tmpDir);
         addCommand("\n");
         
         String knownIndels = "";
         if(gc.getKnownIndels()!= null)
         {
             knownIndels = "--known "+gc.getKnownIndels().getAbsolutePath();
         }        
         
         addCommand("java -jar "+gc.getGatk().getAbsolutePath() +" -T RealignerTargetCreator "+" -R "+gc.getReferenceFile().getAbsolutePath()+" -I "+dedupBam.getAbsolutePath()+ " -o "+realignTarget.getAbsolutePath()+knownIndels);
                  
     
     }
     
     
 }
