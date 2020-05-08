 package Main;
 
 import java.io.IOException;
 import java.nio.charset.Charset;
 import java.nio.charset.StandardCharsets;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.util.ArrayList;
 import java.util.Scanner;
 
 /**
  * Simple job management class. Supports reading jobs (one per line) from a
  * file. Also supports issuing those jobs to clients and a simple expiration
  * system.
  * 
  * @author Cole Christie
  * 
  */
 public class JobManagement {
 	private ArrayList<Jobs> jobqueue;
 	private ArrayList<Jobs> jobsent;
 	private Charset ENCODING = StandardCharsets.UTF_8;
 
 	/**
 	 * Default construcutor.
 	 */
 	public JobManagement() {
 		jobqueue = new ArrayList<Jobs>();
 		jobsent = new ArrayList<Jobs>();
 	}
 
 	/**
 	 * Returns the number of jobs yet to be assigned
 	 * 
 	 * @return
 	 */
 	public int UnassignedCount() {
 		return jobqueue.size();
 	}
 
 	/**
 	 * Returns the number of jobs that have been assigned, but not yet
 	 * acknowledged as complete
 	 * 
 	 * @return
 	 */
 	public int AssignedCount() {
		return jobqueue.size();
 	}
 
 	/**
 	 * Reads a list of jobs (one per line) from a file
 	 * 
 	 * @param filepath
 	 * @throws IOException
 	 */
 	public int Load(String filepath) throws IOException {
 		int AddeCounter = 0;
 		Path path = Paths.get(filepath);
 		try (Scanner scanner = new Scanner(path, ENCODING.name())) {
 			while (scanner.hasNextLine()) {
 				// Read each line into the array list
 				String line = scanner.nextLine();
 				if (line != null && !line.isEmpty()) {
 					Jobs jobUnit = new Jobs(line);
 					jobqueue.add(jobUnit);
 					AddeCounter++;
 				}
 			}
 		}
 		return AddeCounter;
 	}
 
 	/**
 	 * Populates the job queue with 10 sample jobs for windows based systems
 	 */
 	public void SampleWindows() {
 		for (int loop = 0; loop < 10; loop++) {
 			Jobs jobUnit = new Jobs("cmd date /T");
 			jobqueue.add(jobUnit);
 		}
 	}
 
 	/**
 	 * Populates the job queue with 10 sample jobs for linux/unix based systems
 	 */
 	public void SampleLinux() {
 		for (int loop = 0; loop < 10; loop++) {
 			Jobs jobUnit = new Jobs("date");
 			jobqueue.add(jobUnit);
 		}
 	}
 
 	/**
 	 * Assigns a job to a client and returns the string contain what that job is
 	 * (what work needs to be done)
 	 * 
 	 * @param clientsName
 	 * @return
 	 */
 	public String Assign(String clientsName) {
 		int size = jobqueue.size();
 		if (size > 0) {
 			Jobs jobUnit = jobqueue.get(0); // Pull the data at spot 0
 			jobqueue.remove(0); // Remove it from the queue
 			jobUnit.SetIssued(clientsName); // Add who it was issued to
 			jobUnit.SetTimeIssued(); // Update the time issued to now
 			jobsent.add(jobUnit); // Add that data into the issued list
 			String jobWork = jobUnit.GetWork();
 			return jobWork; // Return the extracted data
 		} else {
 			return "";
 		}
 	}
 
 	/**
 	 * Clears the queue of jobs that have NOT already been sent to clients
 	 */
 	public void ClearUnsentQueue() {
 		jobqueue.clear();
 	}
 	
 	/**
 	 * Clears the queue of jobs that HAVE already been sent to clients
 	 */
 	public void ClearSentQueue() {
 		jobsent.clear();
 	}
 }
