 package tira;
 
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 
 
 /**
  * Execution manager.
  * Schedules threaded execution of experiment runs. 
  * 
  * @author tim
  *
  */
 public class TiraApp {
     
     //private JSONObject tiraConfig;
     private JSONObject system;
     private Map<String,AProgram> programs = new HashMap<String,AProgram>();
     private BlockingQueue<AProgram> executionQueue = new LinkedBlockingQueue<AProgram>();
     private BlockingQueue<AProgram> waitingQueue = new LinkedBlockingQueue<AProgram>();
     private JSONArray programPaths;
     private String dataRoot;
     private List<String> pNames = new LinkedList<String>();
     private boolean EXIT = false;
     
     
     public TiraApp(JSONObject systemConfig) throws JSONException
     {
         
         //this.tiraConfig = tiraConfig;
         this.system = systemConfig;
         
         if(System.getProperty("os.name").contains("Windows")){system.put("/", "\\");}
         else {system.put("/", "/");}
         
         if(system.get(Util.PROGRAMROOT) instanceof String)
         {
             this.programPaths = (new JSONArray()).put(system.getString(Util.PROGRAMROOT));
         }
         else {this.programPaths = system.getJSONArray(Util.PROGRAMROOT);}
         //this.programPath = Util.substitute(system.getString(Util.PROGRAMROOT), system);
         this.dataRoot = Util.substitute(system.getString(Util.DATAROOT), system);
         startWaitingQueue();
         startExecutionQueue(system.getInt(Util.WORKER));        
     }
     
     private void startWaitingQueue() {
         new Thread(new Runnable() {            
             public void run() {
                 while(true){try{
                     AProgram program = waitingQueue.take();
                     Thread.sleep(500);
                     executionQueue.put(program);}
                 catch (InterruptedException e) {e.printStackTrace();}
             }}}).start();           
     }
 
     private void startExecutionQueue(int numWorker) throws JSONException {
         final JSONObject pendingRuns = (new JSONObject()).put(Util.STATE, Util.TODO);
         for(int i=0; i<numWorker; ++i)
         {
             new Thread(new Runnable() {
                 public void run() {
                     while(true){
                         try {
                             if(EXIT) {break;}
                             AProgram program = executionQueue.take();                            
                             JSONArray runs = program.readRuns(pendingRuns);
                             if(runs.length()==0){waitingQueue.put(program);}
                             else
                             {
                                 executionQueue.put(program);
                                 for(int i=0; i<runs.length(); ++i)
                                 {
                                     program.execute(runs.getJSONObject(i).getString(Util.ID));
                                 }
                             }
                         }
                         catch (InterruptedException e) {break;}
                         catch (JSONException e) {e.printStackTrace();}
                         catch (IOException e) {e.printStackTrace();}
                     }
                 }
 
             }).start();
         }       
     }
 
     public AProgram getProgram(String programName)
     {
         return programs.get(programName);
     }
     
     
     public File readData(String filepath)
     {
         File file = new File(dataRoot, filepath);
         if(file.exists()){return file;}
         return null;
     }
     
     public void loadPrograms() throws JSONException, InterruptedException, IOException
     {
         System.out.println("Loading programs...");
         for(int i=0; i<programPaths.length(); ++i)
         {           
             File pDir = new File(programPaths.getString(i));
             loadPrograms(pDir,pDir,(new JSONObject()).put(Util.SYSTEM, system));
         }
     }
     
     private void loadPrograms(File programRoot, File programDir, JSONObject baseRecord) throws InterruptedException, JSONException, IOException {
        if(!programDir.exists()){return;}
         File recordFile = new File(programDir,"record.json");
         JSONObject programRecord;
         if(recordFile.exists())
         {
             programRecord = new JSONObject(Util.fileToString(recordFile));
             Util.augmentDeep(programRecord,baseRecord);
             programRecord.getJSONObject(Util.SYSTEM).put(Util.PROGRAMROOT, programRoot.getAbsolutePath());
             if(programRecord.has(Util.MAIN)||programRecord.has(Util.DATABASE))
             {
                 String programName = programDir.getPath().replaceFirst(programRoot.getPath(), "").substring(1).replace("\\", "/");
                 programRecord.getJSONObject("SYSTEM").put(Util.PNAME, programName);
                 AProgram program = ProgramFactory.createProgram(programRecord);                    
                 programs.put(programName, program);
                 pNames.add(programName);
                 if(programRecord.has(Util.MAIN)){executionQueue.put(program);}
                 System.out.println(programName+" loaded.");
             }
             //System.out.println(programRecord.toString(1));
         }
         else {programRecord = new JSONObject(baseRecord.toString());}
               
         for(File dir : programDir.listFiles())
         {
             if(dir.isDirectory()){loadPrograms(programRoot,dir,programRecord);}        
         }      
     }
 
     public String[] getProgramsInFolder(String program) {
         StringBuilder folder = new StringBuilder();
         for(String pname : pNames)
         {
             if(pname.startsWith(program)) {
                 folder.append(":");folder.append(pname);
             }
         }
         return folder.toString().split(":");
     }
 
     public void exit(){EXIT=true;}
 
     public static void main(String[] args) throws JSONException, IOException, InterruptedException {
         //load tira config.
         JSONObject tiraConfig = new JSONObject(Util.fileToString(new File("programs/record.json")));
         TiraApp app = new TiraApp(tiraConfig);
         //parse record folder and load programs.
         app.loadPrograms();
         String text = java.util.UUID.randomUUID().toString();
         JSONObject runConfig = (new JSONObject("{other:[\"?!\",\"!!\"]}")).put("text", text).put("punct", "$other");
         //JSONObject runConfig = (new JSONObject()).put("dir", "~/code-in-progress/tira/tira-7/mini_corpus").put("dataset", "01");
         app.getProgram("examples/echo").createRuns(runConfig);
         //app.getProgram("gillam12").createRuns(runConfig);
 
         //TODO: terminate on poisson pill.
         //Thread.sleep(10000);
         //app.exit();
     }
 }
 
