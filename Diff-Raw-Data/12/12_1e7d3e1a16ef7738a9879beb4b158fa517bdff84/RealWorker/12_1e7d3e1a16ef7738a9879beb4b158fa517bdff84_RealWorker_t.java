 package nl.tudelft.cloud_computing_project.worker;
 
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.concurrent.TimeoutException;
 
 
 import nl.tudelft.cloud_computing_project.model.Database;
 import nl.tudelft.cloud_computing_project.model.Job;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.sql2o.Sql2o;
 
 
 public class RealWorker {
 	private static final Logger LOG = LoggerFactory.getLogger(RealWorker.class);
 	//Worker EC2 instance ID
 	private String my_id = "";
 	//sql query to fetch assigned jobs to this worker
 	private final String jobs_sql = 
 			"SELECT Job.id, Job.filename, Job.filesize" +
 					" FROM Job " +
 					" JOIN Assignment ON" +
 					" Assignment.job_id = Job.id" +
 					" WHERE Assignment.worker_instanceid = :my_id" +
 					" ORDER BY Assignment.order";
 
 
 
 	private Sql2o sql2o;
 	//Jobs in the order they should be executed
 	private Iterable<Job> jobs;
 	private long wget_time_out = 180000; // 3min
 
 
 
 	//******** Support methods ***********
 
 	/**
 	 * simple http request to get current worker instance ID
 	 * @return String
 	 * @throws Exception
 	 */
 	private String getInstanceID() {
 
 		String url = "http://169.254.169.254/latest/meta-data/instance-id";
 		StringBuffer response = new StringBuffer();
 
 		try {
 			URL obj = new URL(url);
 			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
 			con.setRequestMethod("GET");
 			BufferedReader in = new BufferedReader( new InputStreamReader(con.getInputStream()));
 			String inputLine;
 			response = new StringBuffer();
 			while ((inputLine = in.readLine()) != null) {
 				response.append(inputLine);
 			}
 			in.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return response.toString();
 	}
 
 	/**
 	 * updates the status of the job with id = jid, to completed
 	 * @param jid
 	 */
 	private void updateJobStatusOnComplete(int jid){
 		String update_sql =
 				"UPDATE Job " +
 						"SET jobstatus = " + Job.JobStatus.COMPLETED.code +
 						" WHERE Job.id = :jid";
 
 		sql2o.createQuery(update_sql).addParameter("jid", jid).executeUpdate();
 	}
 
 	/**
 	 * deletes assignment for current worker
 	 * @param jid
 	 */
 	private void deleteAssignment(int jid){
 		String delete_sql = 
 				"DELETE FROM Assignment " +
 						"WHERE Assignment.job_id = :jid";
 
 		sql2o.createQuery(delete_sql).addParameter("jid", jid).executeUpdate();
 	}
 
 	/**
 	 * increments the number of failures of the job with id = jid
 	 * @param jid
 	 */
 	private void updateJobNumOfFailures(int jid){
 		String update_sql =
 				"UPDATE Job " +
 						"SET Job.num_failures = Job.num_failures + 1" +
 						" Where Job.id = :jid";
 		sql2o.createQuery(update_sql).addParameter("jid", jid).executeUpdate();
 	}
 
 	/**
 	 * discards tesseract job result (-dealiong with the output is not part of this project)
 	 */
 	private void discardJob(){
 		String rmCommand = "rm img.png out.txt";
 		Process pRM;
 		try {
 			pRM = Runtime.getRuntime().exec(rmCommand);
 			pRM.waitFor();
 			if(pRM.exitValue() != 0)   
 			{  
 				LOG.error("Worker " + my_id + "failed to discard job...");
 			}
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Calculates de appropriate time_out for a command according to the job size
 	 * @param filesize
 	 * @return
 	 */
 	private long getAppropriateTimeOut(long filesize){
 		
 		long timeout = 0;
 		int n = Math.round(filesize/100000);
 		for(int i=0; i < n; i++){
 			timeout += 240000; //4 minutes per MB
 		}
 		return timeout;
 	}
 
	
 	//******** Worker Logic ***********
 
 	/**
 	 * pulls from DB the assigned jobs for current worker
 	 */
 	private void fetchAssignedJobs(){
 		LOG.info("Started fetching jobs for worker " + my_id);
 		jobs = sql2o.createQuery(jobs_sql).addParameter("my_id", my_id).executeAndFetch(Job.class);
 	}
 
 	/**
 	 * executes the jobs assigned to current worker: runs wget + tesseract 
 	 * @throws Exception
 	 */
 	private void executeJobs() throws Exception{
 		for(Job j: jobs){
 			String wgetCommand = "wget -q " + j.getFilename() + " -O img.jpg";
 			Process p1, p2;
 			ProcessThread pt1, pt2 = null;
 			try {
 				p1= Runtime.getRuntime().exec(wgetCommand);
 				LOG.info("Worker " + my_id + " - started downloading " + j.getFilename());
 				pt1 = new ProcessThread(p1);
 				pt1.start();
 				try {
 					pt1.join(wget_time_out );
 					if (pt1.exit != 0){
 						LOG.error("Worker " + my_id + " wget command failed. Exit value=" + pt1.exit);
 						updateJobNumOfFailures(j.getId());
 						deleteAssignment(j.getId());
 					}
 					else if(pt1.exit == 0){
 						LOG.info("Worker " + my_id + " - finished downloading, starting tesseract command");
 						String tesseractCommand = "tesseract img.jpg out";
 						p2 = Runtime.getRuntime().exec(tesseractCommand);
 						pt2 = new ProcessThread(p2);
 						long tesseract_time_out = getAppropriateTimeOut(j.getFilesize());
 						LOG.warn("Timout for tesseract command set to: " + tesseract_time_out/1000.0 + "s.");
 						pt2.start();
 						try{
 							pt2.join(tesseract_time_out);
 							if(pt2.exit != 0){
 								LOG.error("Worker " + my_id + " Tesseract job failed. Exit value=" + pt2.exit);
 								updateJobNumOfFailures(j.getId());
 								deleteAssignment(j.getId());
 							}
 							else if(pt2.exit == 0){
 								LOG.info("Worker " + my_id + " - finished job");
 								//Current method selection for handling tesseract output
 								updateJobStatusOnComplete(j.getId());
 								deleteAssignment(j.getId());
 								discardJob();
 							}
 							else{
 								LOG.warn("Worker " + my_id + " tessaract operation timeout");
 								updateJobNumOfFailures(j.getId());
 								deleteAssignment(j.getId());
 								throw new TimeoutException();
 							}
 						}
 						catch(InterruptedException ex) {
 							pt2.interrupt();
 							Thread.currentThread().interrupt();
 							throw ex;
 						} 
 
 
 					}
 					else{
 						LOG.warn("Worker " + my_id + " wget operation timeout");
 						throw new TimeoutException();
 					}
 				} catch(InterruptedException ex) {
 					pt2.interrupt();
 					Thread.currentThread().interrupt();
 					throw ex;
 				} 
 			} catch (Exception e) {
 				LOG.error("Exception occured while executing jobs in worker " + my_id, e);
 				e.printStackTrace();
 			} 
 		}
 		LOG.info("Worker " + my_id + " completed assigned jobs!");
 	}
 
 
 	//******** Support classes ***********
 
 	private class ProcessThread extends Thread {
 		private final Process process;
 		private Integer exit = 1;
 		private ProcessThread(Process process) {
 			this.process = process;
 		}
 		public void run() {
 			try { 
 				exit = process.waitFor();
 			} catch (InterruptedException ignore) {
 				return;
 			}
 		}  
 	}
 
 
 	//******** Worker execution entry point ***********
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		RealWorker worker = new RealWorker();
 		worker.sql2o = Database.getConnection();
 		worker.my_id = worker.getInstanceID();
 		// check own assignments in DB and fetch jobs to run
 		while(true){
 			try {
 				worker.fetchAssignedJobs();
 				// run list of assigned jobs
 				worker.executeJobs();
 				Thread.sleep(10000);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 }
