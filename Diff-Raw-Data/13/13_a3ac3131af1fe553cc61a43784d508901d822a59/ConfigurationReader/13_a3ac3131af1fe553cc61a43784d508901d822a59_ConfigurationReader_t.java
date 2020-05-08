 package org.espressoOtr.exs.conf;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.espressootr.lib.file.FileReaderManager;
 import org.espressootr.lib.text.splitter.FastSplitter;
  
 public class ConfigurationReader
 {
     
     public static Map<String, String> settingConfigurations(String confFilePath)
     {
         Map<String, String> confKv = getConfigData(confFilePath);
         
         Set<String> confKeySet = confKv.keySet();
         
         for (String key : confKeySet)
         {
             System.setProperty(key, confKv.get(key));
         }
         
         return confKv;
         
     }
     
     private static Map<String, String> getConfigData(String confFilePath)
     {
         Map<String, String> confKv = new HashMap<String, String>();
         try
         {
             List<String> confDatas = FileReaderManager.readLineFromFile(confFilePath);
             
             FastSplitter fs = new FastSplitter(' ');
             
             for (String confData : confDatas)
             {
            	if(confData.length()>0)
            		confKv.put(fs.splitToArray(confData)[0], fs.splitToArray(confData)[1].replace("\"", ""));
             }
             
         }
         catch (IOException e)
         {
             e.printStackTrace();
         }
         
         return confKv;
     }
     
 }
