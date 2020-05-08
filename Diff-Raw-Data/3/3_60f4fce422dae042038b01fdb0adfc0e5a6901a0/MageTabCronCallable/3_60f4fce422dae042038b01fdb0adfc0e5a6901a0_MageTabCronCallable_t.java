 package uk.ac.ebi.fgpt.sampletab.arrayexpress;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.concurrent.Callable;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.ebi.arrayexpress2.magetab.exception.ParseException;
 import uk.ac.ebi.fgpt.sampletab.subs.Event;
 import uk.ac.ebi.fgpt.sampletab.subs.TrackingManager;
 import uk.ac.ebi.fgpt.sampletab.utils.ProcessUtils;
 
 
 public class MageTabCronCallable implements Callable<Boolean> {
 
     private final String idfFTPPath;
     private final File idfOut;
     private final String sdrfFTPPath;
     private final File sdrfOut;
     private final File outSampleTabPre;
 
     private static final String SUBSEVENT = "Source Update";
 
     private Logger log = LoggerFactory.getLogger(getClass());
     
     public MageTabCronCallable(String idfFTPPath, File idfOut, String sdrfFTPPath, File sdrfOut, File outSampleTabPre) {
         this.idfFTPPath = idfFTPPath;
         this.idfOut = idfOut;
         this.sdrfFTPPath = sdrfFTPPath;
         this.sdrfOut = sdrfOut;
         this.outSampleTabPre = outSampleTabPre;
     }
     
     @Override
     public Boolean call() {
         String accession = sdrfOut.getAbsoluteFile().getParentFile().getName();
         //try to register this with subs tracking
         Event event = TrackingManager.getInstance().registerEventStart(accession, SUBSEVENT);
 
         boolean toReturn = false;
         
         try {
             
             String filename = sdrfOut.getAbsolutePath();
             log.info("Curl downloading "+filename);
             String bashcom = "curl -z "+filename+" -o "+filename+" "+sdrfFTPPath;
             ProcessUtils.doCommand(bashcom, null);  
 
             filename = idfOut.getAbsolutePath();
             log.info("Curl downloading "+filename);
             bashcom = "curl -z "+filename+" -o "+filename+" "+idfFTPPath;            
             ProcessUtils.doCommand(bashcom, null);  
             
             MageTabToSampleTab mttst = new MageTabToSampleTab();
             try {
                 mttst.convert(idfOut, outSampleTabPre);
                 toReturn = true;
             } catch (IOException e) {
                 log.error("Unable to write "+outSampleTabPre, e);
                toReturn = false;
             } catch (ParseException e) {
                 log.error("Unable to parse "+idfOut, e);
                toReturn = false;
             }  
             
         } finally {
             //try to register this with subs tracking
             TrackingManager.getInstance().registerEventEnd(event);
         }
         
         return toReturn;
     }
 }
