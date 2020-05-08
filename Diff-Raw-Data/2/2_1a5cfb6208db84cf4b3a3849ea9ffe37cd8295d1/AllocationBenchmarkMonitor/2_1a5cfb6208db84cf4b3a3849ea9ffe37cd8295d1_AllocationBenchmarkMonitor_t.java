 package nl.tudelft.cloud_computing_project.experimental.allocation_benchmarks;
 
 import java.io.File;
 import java.io.FileWriter;
 
 import nl.tudelft.cloud_computing_project.CloudOCR;
 import nl.tudelft.cloud_computing_project.Monitor;
 import nl.tudelft.cloud_computing_project.model.Database;
 
 import org.sql2o.Sql2o;
 import org.sql2o.data.Table;
 
 public class AllocationBenchmarkMonitor {
 
 	private static final String get_uncompleted_jobs_number	
 			= "SELECT "
 			+ "COUNT(*) AS uncompletedjobs "
 			+ "FROM Job "
 			+ "WHERE "
 			+ "Job.jobstatus = 1 ";
 	private static final String get_completed_jobs_number 
 			= "SELECT "
 			+ "COUNT(*) AS completedjobs "
 			+ "FROM Job "
 			+ "WHERE "
 			+ "Job.jobstatus = 2 ";
 	private static final int AVG_EXECUTABLE_JOBS_PER_MACHINE_PER_HOUR = Integer.parseInt((String)CloudOCR.Configuration.get("AVG_EXECUTABLE_JOBS_PER_MACHINE_PER_HOUR"));
 	private static Sql2o sql2o;
 
 	
 	public static void monitorAllocationBenchmark() {
 
 		try {
 			int completedJobsNumber;
 			int uncompletedJobsNumber;
 			int allocatedInstancesNum;
 			int optimalInstanceNumber;
 			Table jobsDBInfo;
 
 			FileWriter fw = new FileWriter(new File("Allocation_Benchmark_Output.csv"));
 			String result = "";
 			boolean completedAllJobs = false;
 
 			while (!completedAllJobs) {
 
 				//Completed Jobs
 				sql2o = Database.getConnection();
 				jobsDBInfo = sql2o.createQuery(get_completed_jobs_number, "get_completed_jobs_number").executeAndFetchTable();
 				completedJobsNumber = jobsDBInfo.rows().get(0).getInteger("completedjobs");
 
 				//Uncompleted Jobs
 				sql2o = Database.getConnection();
 				jobsDBInfo = sql2o.createQuery(get_uncompleted_jobs_number, "get_uncompleted_jobs_number").executeAndFetchTable();
 				uncompletedJobsNumber = jobsDBInfo.rows().get(0).getInteger("uncompletedjobs");
 
 				//Optimal VM number
 				optimalInstanceNumber = (int) Math.ceil((double)uncompletedJobsNumber/(double)AVG_EXECUTABLE_JOBS_PER_MACHINE_PER_HOUR);
 
 				//Allocated VM
 				allocatedInstancesNum = Monitor.getInstance().getNumRunningOrPendingInstances();
 
				result = completedJobsNumber + "," + uncompletedJobsNumber + "," + optimalInstanceNumber + "," + allocatedInstancesNum + "\n";
 
 				fw.append(result);
 				fw.flush();
 
 				//Condition for termination
 				if(uncompletedJobsNumber == 0)
 					completedAllJobs = true;
 
 				try {
 					Thread.sleep(60000);
 				} catch (InterruptedException e) {
 					System.err.println("monitorAllocationBenchmark nterrupted while sleeping");
 				}
 			}
 
 			fw.close();
 
 		}
 		catch (Exception e) {
 			System.err.println("Error in writing results: " + e.getMessage());
 			e.printStackTrace();
 		} 
 	}
 	
 }
