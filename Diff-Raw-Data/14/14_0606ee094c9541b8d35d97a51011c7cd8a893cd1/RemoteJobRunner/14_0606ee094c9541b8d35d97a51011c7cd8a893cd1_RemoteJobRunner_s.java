 package edu.berkeley.icsi.cdfs.wlgen;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Queue;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.mapreduce.Job;
 
 public final class RemoteJobRunner {
 
 	private static final String JOB_SUBMISSION_TIME_KEY = "job.submission.time";
 
 	private static final long SLEEP_TIME = 100;
 
 	private RemoteJobRunner() {
 	}
 
 	public static void submitAndWait(final List<MapReduceJob> jobs, final int mapLimit) throws IOException,
 			InterruptedException,
 			ClassNotFoundException {
 
 		System.out.println("Launching " + jobs.size() + " jobs");
 
 		final Queue<MapReduceJob> queuedJobs = new LinkedList<MapReduceJob>(jobs);
 		final List<MapReduceJob> runningJobs = new LinkedList<MapReduceJob>();
 		final Map<Job, String> progressMap = new HashMap<Job, String>();
 		int runningMaps = 0;
 
 		while (true) {
 
			if (queuedJobs.isEmpty()) {
 				break;
 			}
 
 			// Submit jobs until mapLimit is reached
 			while (true) {
 
 				final MapReduceJob job = queuedJobs.peek();
 				if (runningMaps + job.getNumMapTasks() > mapLimit) {
 					break;
 				}
 
 				// Remove job
 				queuedJobs.poll();
 
 				// Mark submission time
 				final long now = System.currentTimeMillis();
 				job.getConfiguration().setLong(JOB_SUBMISSION_TIME_KEY, now);
 
 				// Submit job
 				System.out.println("Submitting  " + job + ", (" + queuedJobs.size() + " job still in queue)");
 				job.submit();
 				runningJobs.add(job);
 
 				// Move on to next job
 				runningMaps += job.getNumMapTasks();
 			}
 
 			Thread.sleep(SLEEP_TIME);
 
 			// Wait until a job has finished
 			while (true) {
 
 				boolean atLeastOneJobFinished = false;
 				final Iterator<MapReduceJob> it = runningJobs.iterator();
 
 				while (it.hasNext()) {
 
 					final MapReduceJob runningJob = it.next();
 					if (runningJob.isComplete()) {
 						it.remove();
 						final Configuration conf = runningJob.getConfiguration();
 						final long now = System.currentTimeMillis();
 						final long timeFromSubmission = now - conf.getLong(JOB_SUBMISSION_TIME_KEY, -1L);
 						System.out.println(runningJob.getJobName() + " finished after " + timeFromSubmission);
 						progressMap.remove(runningJob);
 						atLeastOneJobFinished = true;
 						runningMaps -= runningJob.getNumMapTasks();
 						continue;
 					}
 
 					final String progressBar = getProgressBar(runningJob.mapProgress(), runningJob.reduceProgress());
 
 					if (!progressBar.equals(progressMap.put(runningJob, progressBar))) {
 						System.out.println(runningJob.getJobName() + " " + progressBar);
 					}
 				}
 
 				if (atLeastOneJobFinished) {
 					break;
 				}
 
 				Thread.sleep(SLEEP_TIME);
 			}
 		}
 	}
 
 	private static String getProgressBar(final float mapProgress, final float reduceProgress) {
 
 		final StringBuilder sb = new StringBuilder(104);
 
 		sb.append('[');
 		final int mp = Math.round(mapProgress / 2.0f);
 		final int rp = Math.round(reduceProgress / 2.0f);
 
 		for (int i = 0; i < 50; ++i) {
 			if (i < mp) {
 				sb.append('M');
 			} else {
 				sb.append(' ');
 			}
 		}
 
 		sb.append(']');
 		sb.append('[');
 
 		for (int i = 0; i < 50; ++i) {
 			if (i < rp) {
 				sb.append('R');
 			} else {
 				sb.append(' ');
 			}
 		}
 
 		sb.append(']');
 
 		return sb.toString();
 	}
 }
