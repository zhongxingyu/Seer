 package tira;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 public class LocalProgram extends AProgram{
     
     private JSONObject record;    
     private JSONObject baseConfig = new JSONObject();
     
     private long counter = 0;
     // parameter --> value --> [id_1, ..., id_n]
     private Map<String,Map<String,Set<String>>> index = new HashMap<String,Map<String,Set<String>>>();
     // id --> run
     private Map<String,JSONObject> runs = new HashMap<String,JSONObject>();
     private TreeSet<String> indexedIds = new TreeSet<String>();
     
     
     public LocalProgram(JSONObject programRecord) throws JSONException, IOException
     {
         this.record = new JSONObject(programRecord.toString());
         system.put(Util.DATA, ".").put(Util.PROGRAM, ".").put(Util.DATAROOT,".");
         if(record.has("SYSTEM"))
         {
             this.system = Util.override(system, programRecord.getJSONObject("SYSTEM"));
             system.put(Util.PROGRAM, new File(Util.substitute(system.getString(Util.PROGRAM), system)).getAbsolutePath());
             system.put(Util.DATA, new File(Util.substitute(system.getString(Util.DATA), system)).getAbsolutePath());
             system.put(Util.DATAROOT, new File(Util.substitute(system.getString(Util.DATAROOT), system)).getAbsolutePath());
             record.remove("SYSTEM");            
             //System.out.println(system);
         }
         
         initBaseConfig();
         parseDataAndLoadRuns(new File(system.getString(Util.DATA)));
     }
 
     private void initBaseConfig() throws JSONException
     {
         @SuppressWarnings("unchecked")
         Iterator<String> keyIter = record.keys();
         while(keyIter.hasNext()) {
             String key = keyIter.next();
             if(record.get(key) instanceof String && key.toUpperCase().equals(key)) 
                 {baseConfig.put(key, record.get(key));}
             else if(record.get(key) instanceof JSONObject &&
                      record.getJSONObject(key).has(Util.VALUE))
                 {baseConfig.put(key, record.getJSONObject(key).getString(Util.VALUE));}
         }
     }
     
     
     private void parseDataAndLoadRuns(File dataDir) throws JSONException, IOException
     {
         if(!dataDir.exists()){dataDir.mkdirs();}
         else if(dataDir.isDirectory())
         {
             for(File dataFile : dataDir.listFiles())
             {
                 if(dataFile.isDirectory()){parseDataAndLoadRuns(dataFile);}
                 else if(dataFile.getName().endsWith(".run"))
                 {
                     //System.out.println("load run " + dataFile.getName());
                     loadRuns(dataFile);
                 }
             }
         }
     }
     
     private void loadRuns(File dataFile) throws IOException, JSONException {
         String editsString = Util.fileToString(dataFile);
         StringBuilder sb = new StringBuilder();
         sb.append("[").append(editsString).append("]");
         JSONArray edits = new JSONArray(sb.toString());
         //System.out.println(edits.length());
         for(int i=0; i<edits.length(); ++i)
         {
             JSONObject edit = edits.getJSONObject(i);
             String id = edit.getString(Util.ID);
             if(!runs.containsKey(id)){runs.put(id, edit);}
             else {Util.override(runs.get(id),edit);}
         }
         counter = runs.size();
         index(runs);
     }
     
     private void index(Map<String,JSONObject> runs) throws JSONException {
         for(JSONObject run : runs.values()){ index(run); }       
     }
 
     private void index(JSONObject run) throws JSONException {
         @SuppressWarnings("unchecked")
         Iterator<String> keyIter = run.keys();
         while(keyIter.hasNext()) {
             String key = keyIter.next();
             String value = run.getString(key);
             String id = run.getString(Util.ID);
             indexedIds.add(id);
             addToIndex(key,value,id);
         }
     }
     
     private void addToIndex(String key, String value, String id)
     {
         if(!index.containsKey(key)){index.put(key, new HashMap<String,Set<String>>());}            
         if(!index.get(key).containsKey(value)){index.get(key).put(value, new TreeSet<String>());}
         index.get(key).get(value).add(id);
     }    
     
     @Override
     public synchronized String[] createRuns(JSONObject runConfig) {
         try {
             runConfig = new JSONObject(runConfig.toString());
             List<JSONObject> runs = Util.decompose(runConfig, baseConfig);
             String[] runIds = new String[runs.size()];
             for(int i=0; i<runs.size(); ++i){runIds[i]=createRun(runs.get(i));}
             return runIds;
         }
         catch (Exception e) {e.printStackTrace();}
         return null;
     }
     
     public synchronized String createRun(JSONObject runConfig) throws Exception {
         JSONArray array = readRuns(runConfig);
         if(array.length()>1) {throw new Exception("Invalid run configuration:\n"+array.toString());}
         else if(array.length()==1)
         {            
             String id = array.getJSONObject(0).getString(Util.ID);
             if(array.getJSONObject(0).getString(Util.STATE).equals(Util.ERROR))
             {updateRun(id,(new JSONObject()).put(Util.STATE, Util.TODO));}
             return id;
         }
         else{
             String id = getFreshId();
             //clone run to prevent (unnoticed) changes from outside the database.
             JSONObject runCopy = new JSONObject(runConfig.toString());
             runCopy.put(Util.ID, id);
             runCopy.put(Util.STATE, Util.TODO);
             runs.put(id, runCopy);
             index(runCopy);
             writeEdit(runCopy);
             return id;
         }
     }
 
     @Override
     public JSONArray readRuns(JSONObject runConfig) {
         if(runConfig.length()==0){return new JSONArray(runs.values());}
         JSONArray result = new JSONArray();
         Set<String> ids = new TreeSet<String>(indexedIds);
         try{
             @SuppressWarnings("unchecked")
             Iterator<String> keyIter = runConfig.keys();
             while(keyIter.hasNext()) {
                 Set<String> toRetain;
                 String key = keyIter.next();       
                 if(runConfig.get(key) instanceof String)
                 {
                     String value = runConfig.getString(key);
                     if(value.equals("")){continue;} //TODO: Check if this makes always sense.
                     if(!index.containsKey(key) || !index.get(key).containsKey(value)){return result;}
                     toRetain = index.get(key).get(value);
                 }
                 else // assume JSONArray
                 {
                     JSONArray array = runConfig.getJSONArray(key);
                     toRetain = new TreeSet<String>();
                     for(int i=0; i<array.length(); ++i)
                     {
                         String value = array.getString(i);
                         if(value.equals("")){continue;} //TODO: Check if this makes always sense.
                         if(!index.containsKey(key) || !index.get(key).containsKey(value)){return result;}
                         toRetain.addAll(index.get(key).get(value));
                     }
                 }
                 ids.retainAll(toRetain);
                 if(ids.isEmpty()) {return result;} 
             }
         }
         catch (JSONException e) {e.printStackTrace();}
         catch (NullPointerException e) {return result;}
         
         for(String id : ids){result.put(runs.get(id));}
         return result;
     }
     
     private String getFreshId() {
         while(runs.containsKey(String.valueOf(counter))){++counter;}
         return String.valueOf(counter);
     }
     
     private synchronized void writeEdit(JSONObject run) throws IOException, JSONException {
         //substitute $DATA with values.
         File runDir = new File(system.getString(Util.DATA),run.getString(Util.ID));
         runDir.mkdirs();        
        FileWriter writer = new FileWriter(new File(runDir,run.getString(Util.ID)+".run"),true);
         writer.write(run.toString(1));
         writer.write(",");
        writer.flush();       
     }
 
     @Override
     public void updateRun(String runId, JSONObject update) {
         try {
             JSONObject run = runs.get(runId);
             JSONObject edit = new JSONObject();
             @SuppressWarnings("unchecked")
             Iterator<String> keyIter = update.keys();
             while(keyIter.hasNext()) {
                 String key = keyIter.next();             
                 if(run.has(key)){removeFromIndex(key,run.getString(key),runId);}
                 String updateValue = Util.substitute(update.getString(key),run);
                 run.put(key, updateValue);
                 edit.put(key, updateValue);
                 addToIndex(key,updateValue,runId);                               
             }
             String timestamp = Util.dateFormat.format(new Date());
             run.put(Util.LASTMOD, timestamp);
             edit.put(Util.LASTMOD, timestamp);
             edit.put(Util.ID, runId);
             writeEdit(edit);
         }
         catch (JSONException e) {e.printStackTrace();}
         catch (IOException e) { e.printStackTrace();}     
         
     }
     
     private void removeFromIndex(String key, String value, String id)
     {
         index.get(key).get(value).remove(id);
     }
 
     @Override
     public synchronized JSONObject takeRun(String runId) {
         try {
             JSONObject run = runs.get(runId);
             String currentState = run.getString(Util.STATE);
             if(currentState.equals(Util.TODO))
             {
                 updateRun(runId,(new JSONObject()).put(Util.STATE, Util.RUNNING));
                 return new JSONObject(run.toString());
             }            
         }
         catch (JSONException e) { e.printStackTrace();}
         return null;
     }
 
     @Override
     public void deleteRuns(JSONObject runConfig) {
         // TODO Auto-generated method stub
         
     }
     
     @Override
     public JSONObject getProgramRecord() {return record;}   
     @Override
     public JSONObject getDefaultConfig() {return baseConfig;} 
     @Override   
     public String getInfo() {
         try {            
             File file = new File(system.getString(Util.PROGRAM),"info.html");
             return Util.fileToString(file);
         }
         catch(Exception e) {return "";}
     }
     
     public static void main(String[] args) throws JSONException, IOException, InterruptedException {
         JSONObject programRecord = new JSONObject(Util.fileToString(new File("programs/echo/record.json")));
         LocalProgram p = new LocalProgram(programRecord);
         System.out.println(p.getDefaultConfig().toString());
         String[] ids = p.createRuns(new JSONObject("{text:[\"hallo welt\",\"halloe rest\"],punct:\"!\"}"));
         System.out.println(Arrays.asList(ids));
         for(String id : ids)
         {
             //p.updateRun(id, new JSONObject("{result:\"42\"}"));
             p.execute(id);  
         }
         System.out.println(p.readRuns((new JSONObject()).put(Util.STATE, Util.DONE)));
     }
 
 }
