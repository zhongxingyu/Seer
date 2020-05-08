 package hu.juranyi.zsolt.heritrixremote;
 
 import hu.juranyi.zsolt.heritrixremote.model.Heritrix;
 import hu.juranyi.zsolt.heritrixremote.model.HeritrixCall;
 import hu.juranyi.zsolt.heritrixremote.model.Job;
 import hu.juranyi.zsolt.heritrixremote.model.JobState;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.lang.reflect.Method;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 
 /**
  * With HeritrixRemote you can easily control your running Heritrix especially
  * when you've got a lot of crawling jobs to manage.
  *
  * @author Zsolt Jurányi
  */
 public class HeritrixRemote {
     // TODO javadoc, make all private -> protected
     // TODO hiba esetén kilépés - 1. megoldás: az adott helyen System.exit(#), enum+own int-ből vagy static final int-ből
     // TODO hiba esetén kilépés - 1. megoldás: a getter-ek/fetch-erek exception-t dobnak, és itt a main class lép ki; az exception-ök tartalmazzák az exit code-ot (static final int)
     // TODO tudni kéne detektálni ha nincs CURL telepítve
     // TODO exit code-okkal kéne kilépni ha hiba van!
     // TODO esetleg a statusnál is lehetne valami exit code, pl. 100=UNBUILT, 101=PAUSED, ... de ez persze csak 1 job-nál értelmes
     // TODO store - ehhez localhost kell, dátumozott mappába másolja, csak ha FINISHED
 
     public static final String VERSION = "1.0b";
     public static Heritrix heritrix;
     public static String[] arguments;
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         printHeader();
         if (args.length < 4) {
             printUsage();
         } else {
             arguments = args;
             heritrix = new Heritrix(args[0], args[1]);
             try {
                 Method commandMethod = HeritrixRemote.class.getDeclaredMethod(args[2] + "Command", (Class<?>[]) null);
                 commandMethod.invoke(null, (Object[]) null);
             } catch (NoSuchMethodException ex) {
                 printUsage();
             } catch (Exception ex) {
                 ex.printStackTrace();
             }
         }
     }
 
     private static void printHeader() {
         System.out.println("HeritrixRemote by Zsolt Juranyi");
         System.out.println("Version " + VERSION);
         System.out.println();
     }
 
     private static void printUsage() {
         InputStream usageIS = HeritrixRemote.class.getResourceAsStream("Usage.txt");
         BufferedReader r = new BufferedReader(new InputStreamReader(usageIS));
         try {
             while (r.ready()) {
                 System.out.println(r.readLine());
             }
         } catch (IOException ex) {
         }
     }
 
     private static List<Job> fetchNeededJobs() {
         if (arguments[3].equalsIgnoreCase("all")) {
             // need all jobs
             return heritrix.getJobs();
         } else {
             // need jobs by state or id
             ArrayList<Job> neededJobs = new ArrayList<Job>();
             try {
                 JobState neededState = JobState.valueOf(arguments[3]); // throws exception on invalid enum value
 
                 // valid job state -> job filter
                 for (Job job : heritrix.getJobs()) {
                     if (job.getState().equals(neededState)) {
                         neededJobs.add(job);
                     }
                 }
             } catch (IllegalArgumentException ex) {
                 // invalid job state -> job id
                 Job job = new Job(heritrix, Arrays.asList(new String[]{arguments[3]}));
                 // TODO ide lehetne valami csekkolást, hogy létezik-e ilyen job
                 neededJobs.add(job);
             }
             return neededJobs;
         }
     }
 
     private static void basicAction(String action, JobState[] allowedJobStates) {
         List<JobState> allowed = Arrays.asList(allowedJobStates);
         for (Job job : fetchNeededJobs()) {
             if (allowed.contains(job.getState())) {
                 System.out.print("Action '" + action + "' on job '" + job.getDir() + "' ... ");
                 try {
                     new HeritrixCall(heritrix).path("job/" + job.getDir()).data("action=" + action).getResponse();
                     // TODO kell itt valamit csekkolni? :-)
                 } catch (Exception ex) {
                     System.out.println("ERROR: Failed to execute command '" + action + "' on job '" + job.getDir() + "'.");
                     System.exit(1); // TODO EXIT WITH ERROR CODE
                 }
             } else {
                 System.out.println("Skipping " + job.getDir() + ", its state is " + job.getState().toString());
             }
         }
     }
 
     /*
      * xxxCommand() methods: they will be called from main() where xxx is arg[2]
      * -------------------------------------------------------------------------
      */
     private static void statusCommand() {
         String lineFormat = "%-8s | %-15s | %s\n";
        
         // table header
         System.out.printf(lineFormat, "STATE", "START TIME", "JOB DIRECTORY");
         for (int i = 0; i < 78; i++) {
             System.out.print(((9 == i || 27 == i) ? "+" : "-"));
         }
         System.out.println();
        
         // table rows
         for (Job job : fetchNeededJobs()) {
             Date startDate = job.getStartDate();
             String startDateStr = (null == startDate) ? "N/A" : new SimpleDateFormat("yyyyMMdd HHmmss").format(startDate);
             System.out.printf(lineFormat, job.getState().toString(), startDateStr, job.getDir());
         }
     }
 
     private static void createCommand() { // TODO createCommand()
         // will not use fetchNeededJobs nor basicAction
         // receives:
         // arg[3] = URL1[,URL2,...]
         // and optionally arg[4] = "use" and arg[5] = CXML_NAME
         // XML put-nál üres body jön ha sikeres volt
         // call rescan!
         // hibaüzi, ha már van ilyen job!!!
     }
 
     private static void buildCommand() {
         basicAction("build", new JobState[]{JobState.UNBUILT});
     }
 
     private static void launchCommand() {
         basicAction("launch", new JobState[]{JobState.READY});
         // TODO wait (?)
     }
 
     private static void unpauseCommand() {
         basicAction("unpause", new JobState[]{JobState.PAUSED});
     }
 
     private static void pauseCommand() {
         basicAction("pause", new JobState[]{JobState.RUNNING});
     }
 
     private static void teardownCommand() {
        basicAction("teardown", new JobState[]{JobState.PAUSED, JobState.FINISHED});
     }
 
     private static void terminateCommand() {
        basicAction("teardown", new JobState[]{JobState.PAUSED, JobState.RUNNING, JobState.FINISHED});
         // TODO wait (?)        
     }
 }
