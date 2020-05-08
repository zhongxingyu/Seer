 package grisu.backend.model.job.gt5;
 
 import grith.jgrith.plainProxy.LocalProxy;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.logging.Level;
 
 import org.apache.log4j.Logger;
 import org.globus.gram.Gram;
 import org.globus.gram.GramException;
 import org.globus.gram.GramJob;
 import org.globus.gram.GramJobListener;
 import org.globus.gram.internal.GRAMConstants;
 import org.globus.gram.internal.GRAMProtocolErrorConstants;
 import org.globus.gsi.GlobusCredentialException;
 import org.ietf.jgss.GSSCredential;
 import org.ietf.jgss.GSSException;
 
 public class Gram5Client implements GramJobListener {
 
 	private static HashMap<String, Integer> statuses = new HashMap<String, Integer>();
 	private static HashMap<String, Integer> errors = new HashMap<String, Integer>();
 	static final Logger myLogger = Logger
 			.getLogger(Gram5Client.class.getName());
 
 	public static void main(String[] args) {
 
 		final String testRSL = args[1];
 		final String contact = "ng1.canterbury.ac.nz";
 		try {
 
 			final Gram gram = new Gram();
 			Gram.ping(contact);
 
 			final GramJob testJob = new GramJob(testRSL);
 			testJob.setCredentials(LocalProxy.loadGSSCredential());
 
 			final Gram5Client gram5 = new Gram5Client();
 			testJob.addListener(gram5);
 
 			// testJob.bind();
 
 			testJob.request("ng1.canterbury.ac.nz", true);
 			testJob.bind();
 			Gram.registerListener(testJob);
 			Gram.jobStatus(testJob);
 
 			System.out
 					.println("job status is : " + testJob.getStatusAsString());
 			System.out.println("the job is : " + testJob.toString());
 			System.out.println("number of currently active jobs : "
 					+ Gram.getActiveJobs());
 
 			while (true) {
 				Gram.jobStatus(testJob);
 				System.out.println("job status is : "
 						+ testJob.getStatusAsString());
 				Thread.sleep(1000);
 			}
 
 		} catch (final GlobusCredentialException gx) {
 			gx.printStackTrace();
 		} catch (final GramException grx) {
 			grx.printStackTrace();
 		} catch (final GSSException gssx) {
 			gssx.printStackTrace();
 		} catch (final Exception ex) {
 			ex.printStackTrace();
 		}
 
 	}
 
 	public Gram5Client() {
 	}
 
 	private String getContactString(String handle) {
 		try {
 			final URL url = new URL(handle);
 			myLogger.debug("job handle is " + handle);	
 			myLogger.debug("returned handle is " + url.getHost());
 			return url.getHost();
 		} catch (final MalformedURLException ex1) {
 			java.util.logging.Logger.getLogger(Gram5Client.class.getName())
 					.log(Level.SEVERE, null, ex1);
 			return null;
 		}
 	}
 
 	public int[] getJobStatus(String handle, GSSCredential cred) {
 
 		final int[] results = new int[2];
 
 		// we need this to catch quick failure
 		Integer status = statuses.get(handle);
 		myLogger.debug("status is " + status);
 		if ((status != null) && (status == GRAMConstants.STATUS_FAILED)) {
 			myLogger.debug("job failed : " + errors.get(handle));
 			results[0] = status;
 			results[1] = errors.get(handle);
 			return results;
 		}
 
 		final String contact = getContactString(handle);
 		final GramJob job = new GramJob(null);
 		try {
 			// lets try to see if gateway is working first...
 			Gram.ping(cred,contact);
 		} catch (final GramException ex) {
 			myLogger.info(ex);
 			// have no idea what the status is, gateway is down:
 			return new int[] { GRAMConstants.STATUS_UNSUBMITTED, 0 };
 		} catch (final GSSException ex) {
 			myLogger.error(ex);
 		}
 
 		try {
 			job.setID(handle);
 			job.setCredentials(cred);
 			job.bind();
 			Gram.jobStatus(job);
 			myLogger.debug("job status is " + job.getStatusAsString());
 			myLogger.debug("job error is " + job.getError());
 		} catch (final GramException ex) {
 			myLogger.debug("ok, normal method of getting exit status is not working. need to restart job.");
			if (ex.getErrorCode() == 156 /* job contact not found*/) {
 				// maybe the job finished, but maybe we need to kick job manager
 
 				myLogger.debug("restarting job");
 				final String rsl = "&(restart=" + handle + ")";
 				final GramJob restartJob = new GramJob(rsl);
 				restartJob.setCredentials(cred);
 				restartJob.addListener(this);
 				try {
 
 					restartJob.request(contact, false);
 				} catch (final GramException ex1) {
 					// ok, now we are really done
 					return new int[] { GRAMConstants.STATUS_DONE, 0 };
 				} catch (final GSSException ex1) {
 					throw new RuntimeException(ex1);
 				}
 
 				// nope, not done yet.
 				return getJobStatus(handle, cred);
 			} else {
 				myLogger.error("something else is wrong. error code is " + ex.getErrorCode());
 				myLogger.error(ex);
 			}
 			
 		} catch (final GSSException ex) {
 			myLogger.error(ex);
 		} catch (final MalformedURLException ex) {
 			myLogger.error(ex);
 
 		}
 		status = job.getStatus();
 		final int error = job.getError();
 		return new int[] { status, error };
 	}
 
 	public int kill(String handle, GSSCredential cred) {
 		try {
 			final GramJob job = new GramJob(null);
 			job.setID(handle);
 			job.setCredentials(cred);
 			try {
 				new Gram();
 				Gram.cancel(job);
 				// job.signal(job.SIGNAL_CANCEL);
 			} catch (final GramException ex) {
 				java.util.logging.Logger.getLogger(Gram5Client.class.getName())
 						.log(Level.SEVERE, null, ex);
 			} catch (final GSSException ex) {
 				java.util.logging.Logger.getLogger(Gram5Client.class.getName())
 						.log(Level.SEVERE, null, ex);
 			}
 			final int status = job.getStatus();
 			return status;
 		} catch (final MalformedURLException ex) {
 			java.util.logging.Logger.getLogger(Gram5Client.class.getName())
 					.log(Level.SEVERE, null, ex);
 			throw new RuntimeException(ex);
 		}
 	}
 
 	public void statusChanged(GramJob job) {
 		myLogger.debug("job status changed  " + job.getStatusAsString());
 		statuses.put(job.getIDAsString(), job.getStatus());
 		errors.put(job.getIDAsString(), job.getError());
 		myLogger.debug("the job is : " + job.toString());
 	}
 
 	public String submit(String rsl, String endPoint, GSSCredential cred) {
 		final GramJob job = new GramJob(rsl);
 		job.setCredentials(cred);
 		job.addListener(this);
 		try {
 			job.request(endPoint, false);
 			Gram.jobStatus(job);
 			return job.getIDAsString();
 		} catch (final GramException ex) {
 			java.util.logging.Logger.getLogger(Gram5Client.class.getName())
 					.log(Level.SEVERE, null, ex);
 			return null;
 		} catch (final GSSException ex) {
 			java.util.logging.Logger.getLogger(Gram5Client.class.getName())
 					.log(Level.SEVERE, null, ex);
 			return null;
 		}
 	}
 }
