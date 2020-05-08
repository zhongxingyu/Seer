 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package net.wazari.plugin;
 
 import java.io.File;
 import java.util.Arrays;
 import java.util.logging.Logger;
 import net.wazari.common.plugins.GenericImporter;
 import net.wazari.common.plugins.Importer.Capability;
 import net.wazari.common.plugins.Importer.ProcessCallback;
 import net.wazari.common.plugins.Importer.SanityStatus;
 
 /**
  *
  * @author kevinpouget
  */
 public class OSXPreviewWrapper  extends GenericImporter {
     private static final String PREVIEW = "/Applications/Preview.app/Contents/MacOS/Preview" ;
     private static final Logger log = Logger.getLogger(OSXPreviewWrapper.class.getName());
 
     @Override
     public String getName() {
         return "OSX Preview wrapper" ;
     }
 
     @Override
     public String getVersion() {
         return "1" ;
     }
 
     @Override
     public String getTargetSystem() {
         return "OSX" ;
     }
 
     @Override
     public String getSupportedFilesDesc() {
         return "images" ;
     }
 
     @Override
     public Capability[] supports() {
        return new Capability[]{Capability.FULLSCREEN_SINGLE} ;
     }
 
     @Override
     public boolean supports(String type, String ext, Capability cap) {
         if (type.contains("image"))
             return Arrays.asList(supports()).contains(cap) ;
         else return false ;
     }
 
     @Override
     public SanityStatus sanityCheck(ProcessCallback cb) {
         if (new File(PREVIEW).canExecute()) {
             return SanityStatus.PASS;
         } else {
             return SanityStatus.FAIL ;
         }
     }
 
     @Override
     public int getPriority() {
         return 3 ;
     }
 
     @Override
     public void fullscreenFile(ProcessCallback cb, String path) {
         cb.exec(new String[]{PREVIEW, path});
     }
 }
