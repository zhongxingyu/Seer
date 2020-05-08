 package com.openske.drools;
 
 import java.io.File;
 import java.util.Collection;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.filefilter.FileFilterUtils;
 import org.drools.builder.ResourceType;
 
 import com.openske.engine.Engine;
 import com.openske.engine.EngineIOFilter;
 
 public class DroolsResourceHelper {
     
     @SuppressWarnings("unchecked")
     public static Collection<File> listResources(ResourceType type) {
         String suffix = null;
         if(ResourceType.DRL.equals(type)) {
             suffix = "drl";
         } else if(ResourceType.DRF.equals(type)) {
            suffix = "rf";
         }
         return FileUtils.listFiles(Engine.currentWorkingDirectory(), FileFilterUtils.suffixFileFilter(suffix), new EngineIOFilter());
     }
 }
