 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * The main testing class.
  * This will run all the registered workloads and output the stats.
  */
 public class TestMaster {
    // Some constants for DB connections.
    public static final String DB_URL =
         "jdbc:mysql://localhost:3306/cgat?autoReconnect=true";
    public static final String DB_USER = "cgat";
    public static final String DB_PASS = "csc560";
 
    public static void main(String[] args) {
       Workload[] workloads = {
                               //new StupidWorkload(),
                               //new ProfileWorkload(),
                              //new AssignTaskWorkload(),
                              new GroupMemWorkload(),
                               };
 
       for (Workload workload : workloads) {
          System.out.println("Running Workload [" + workload.getClass().getCanonicalName() + "]...");
          Stats results = workload.executeMySQL();
          System.out.println("Completed Workload [" + workload.getClass().getCanonicalName() + "]...");
          System.out.println(results + "\n");
       }
    }
 }
