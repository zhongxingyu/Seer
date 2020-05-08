 package grisu.backend.model.job.gt5;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import grisu.backend.model.ProxyCredential;
 import grisu.backend.model.job.Job;
 import grisu.backend.model.job.JobSubmitter;
 import grisu.backend.model.job.ServerJobSubmissionException;
 import grisu.control.JobConstants;
 import grisu.jcommons.interfaces.InformationManager;
 import grith.jgrith.CredentialHelpers;
 
 import org.apache.log4j.Logger;
 import org.globus.gram.Gram;
 import org.globus.gram.GramException;
 import org.globus.gram.GramJob;
 import org.globus.gram.WaitingForCommitException;
 import org.globus.gram.internal.GRAMConstants;
 import org.ietf.jgss.GSSCredential;
 import org.ietf.jgss.GSSException;
 
 
 public class GT5Submitter extends JobSubmitter {
 
 	static final Logger myLogger = Logger.getLogger(GT5Submitter.class
 			.getName());
 	
 	private String getContactString(String handle) {
 		try {
 			final URL url = new URL(handle);
 			myLogger.debug("job handle is " + handle);	
 			myLogger.debug("returned handle is " + url.getHost());
 			return url.getHost();
 		} catch (final MalformedURLException ex1) {
 			myLogger.error(ex1.getLocalizedMessage());
 			return null;
 		}
 	}
 
 	@Override
 	public int getJobStatus(String handle, ProxyCredential credential){
 		return getJobStatus(handle, credential, true);
 	}
 	
 	public int getJobStatus(String handle, ProxyCredential credential, boolean restart) {
 		
 		final Gram5JobListener l = Gram5JobListener.getJobListener();
 
 		final String contact = getContactString(handle);
 		GramJob job = new GramJob(null);
 		GramJob restartJob = new GramJob(null);
 		GSSCredential cred = credential.getGssCredential();
 		
 		try {
 			// lets try to see if gateway is working first...
 			Gram.ping(cred,contact);
 		} catch (final GramException ex) {
 			myLogger.info(ex);
 			// have no idea what the status is, gateway is down:	
 			return translateToGrisuStatus(GRAMConstants.STATUS_UNSUBMITTED, ex.getErrorCode(), 0);
 
 		} catch (final GSSException ex) {
 			myLogger.error(ex);
 			return translateToGrisuStatus(GRAMConstants.STATUS_UNSUBMITTED, 0 , 0);
 		}
 
 		try {
 			job.setID(handle);
 			job.setCredentials(cred);
 			Gram.jobStatus(job);
 			int jobStatus = job.getStatus();
 			if (jobStatus == GramJob.STATUS_DONE || jobStatus == GramJob.STATUS_FAILED){
 				job.signal(GramJob.SIGNAL_COMMIT_END);
 			}
 			return translateToGrisuStatus(jobStatus,job.getError(),job.getError());
 			
 		} catch (final GramException ex) {
 			myLogger.debug("ok, normal method of getting exit status is not working. need to restart job.");
 			if ((ex.getErrorCode() == 156  || 
 					ex.getErrorCode() == GramException.CONNECTION_FAILED || 
 					ex.getErrorCode() == 79) &&
 					restart) {
 				// maybe the job finished, but maybe we need to kick job manager
 
 				myLogger.debug("restarting job");
 				final String rsl = "&(restart=" + handle + ")";
 				restartJob = new GramJob(rsl);
 				restartJob.setCredentials(cred);
 				try {
 					restartJob.request(contact, false);
 				} catch (final GramException ex1) {
 					if (ex1.getErrorCode() == 131) {
 						// job is still running but proxy expired
 						return translateToGrisuStatus(GRAMConstants.STATUS_ACTIVE,131,0);
 					} 
 					// something is really wrong
 					return translateToGrisuStatus(GRAMConstants.STATUS_FAILED, restartJob.getError(),0);
 				} catch (final GSSException ex1) {
 					myLogger.error(ex1);
 					return translateToGrisuStatus(GRAMConstants.STATUS_UNSUBMITTED, 0 , 0);
 				}
 
 				// nope, not done yet.
 				return getJobStatus(handle, credential, false);
 			} else if (ex.getErrorCode() == 156){
 				// second restart didn't work - assume the job is done 
 				// this bit is only needed during transition between releases
 				return translateToGrisuStatus(GRAMConstants.STATUS_DONE, 0 , 0);
 				
 			} else {
 				myLogger.error("something else is wrong. error code is " + ex.getErrorCode());
 				myLogger.error(ex);
 				return translateToGrisuStatus(GRAMConstants.STATUS_UNSUBMITTED, 0 , 0);
 			}
 			
 		} catch (final GSSException ex) {
 			myLogger.error(ex);
 			return translateToGrisuStatus(GRAMConstants.STATUS_UNSUBMITTED, 0 , 0);
 		} catch (final MalformedURLException ex) {
 			myLogger.error(ex);
 			return translateToGrisuStatus(GRAMConstants.STATUS_UNSUBMITTED, 0 , 0);
 		}
 
 	}
 
 	@Override
 	public String getServerEndpoint(String server) {
 		return server;
 	}
 
 	@Override
 	public int killJob(String handle, ProxyCredential cred) {
 		
 		final GramJob job = new GramJob(null);
 		try {			
 			job.setID(handle);
 			job.setCredentials(cred.getGssCredential());
 			try {
 				Gram.cancel(job);
 				Gram.jobStatus(job);
 			} catch (final GramException ex) {
 				myLogger.error(ex.getLocalizedMessage());
 			} catch (final GSSException ex) {
 				myLogger.error(ex.getLocalizedMessage());
 			}
 
			return getJobStatus(handle, cred, true);
 		} catch (final MalformedURLException ex) {
 			myLogger.error(ex.getLocalizedMessage());
 			return JobConstants.UNDEFINED;
 		} 
 	}
 
 	@Override
 	protected String submit(InformationManager infoManager, String host,
 			String factoryType, Job job) throws ServerJobSubmissionException {
 
 		RSLFactory f = RSLFactory.getRSLFactory();
 		String rsl = null;
 
 		try {
 			rsl = f.create(job.getJobDescription(), job.getFqan()).toString();
 		} catch (RSLCreationException rex) {
 			throw new ServerJobSubmissionException(rex);
 		}
 
 		myLogger.debug("RSL is ... " + rsl);
 		GSSCredential credential = null;
 
 		try {
 			credential = CredentialHelpers.convertByteArrayToGSSCredential(job
 					.getCredential().getCredentialData());
 
 			GramJob gt5Job = new GramJob(rsl);
 			final Gram5JobListener l = Gram5JobListener.getJobListener();
 			gt5Job.setCredentials(credential);
 			gt5Job.addListener(l);
 
 			try {
 				gt5Job.request(host, false);
 			} catch (WaitingForCommitException cex) {
 				gt5Job.signal(GramJob.SIGNAL_COMMIT_REQUEST);
 			}
 			//gt5Job.bind();
 			//gt5Job.getStatus();
 
 			return gt5Job.getIDAsString();
 
 		} catch (GSSException gss) {
 			myLogger.error(gss);
 			throw new ServerJobSubmissionException("job credential is invalid");
 		} catch (GramException gex){
 			throw new ServerJobSubmissionException(gex.getLocalizedMessage(),gex);
 		}
 		
 	}
 
 	private int translateToGrisuStatus(int status,int failureCode ,int exitCode) {
 
 		int grisu_status = Integer.MIN_VALUE;
 		if (status == GRAMConstants.STATUS_DONE) {
 			grisu_status = JobConstants.DONE + exitCode;
 		} else if (status == GRAMConstants.STATUS_STAGE_IN) {
 			grisu_status = JobConstants.STAGE_IN;
 		} else if (status == GRAMConstants.STATUS_STAGE_OUT) {
 			grisu_status = JobConstants.STAGE_IN;
 		} else if (status == GRAMConstants.STATUS_PENDING) {
 			grisu_status = JobConstants.PENDING;
 		} else if (status == GRAMConstants.STATUS_UNSUBMITTED) {
 			grisu_status = JobConstants.UNSUBMITTED;
 		} else if (status == GRAMConstants.STATUS_ACTIVE) {
 			grisu_status = JobConstants.ACTIVE;
 		} else if (status == GRAMConstants.STATUS_FAILED) {
 			if (failureCode == GramException.USER_CANCELLED){
 				grisu_status = JobConstants.KILLED;
 			} else {
 				grisu_status = JobConstants.FAILED;
 			}
 		} else if (status == GRAMConstants.STATUS_SUSPENDED) {
 			grisu_status = JobConstants.ACTIVE;
 		} else {
 			// needed for transition period to deal with jobs submitted without two-phase commit
 			if (failureCode == 156){
 				grisu_status = JobConstants.DONE;
 			} else {
 				grisu_status = JobConstants.UNSUBMITTED;
 			}
 		}
 		return grisu_status;
 
 	}
 }
