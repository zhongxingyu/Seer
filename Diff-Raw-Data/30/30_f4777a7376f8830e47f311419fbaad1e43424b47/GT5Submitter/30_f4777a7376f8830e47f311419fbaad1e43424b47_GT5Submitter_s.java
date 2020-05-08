 package grisu.backend.model.job.gt5;
 
 import grisu.backend.hibernate.JobDAO;
 import grisu.backend.model.job.Job;
 import grisu.backend.model.job.JobSubmitter;
 import grisu.backend.model.job.ServerJobSubmissionException;
 import grisu.control.JobConstants;
 import grith.jgrith.credential.Credential;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import org.globus.gram.Gram;
 import org.globus.gram.GramException;
 import org.globus.gram.GramJob;
 import org.globus.gram.WaitingForCommitException;
 import org.globus.gram.internal.GRAMConstants;
 import org.globus.gram.internal.GRAMProtocolErrorConstants;
 import org.ietf.jgss.GSSCredential;
 import org.ietf.jgss.GSSException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class GT5Submitter extends JobSubmitter {
 
 	static final Logger myLogger = LoggerFactory.getLogger(GT5Submitter.class
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
 	public int getJobStatus(Job job, Credential credential) {
 		return getJobStatus(job, credential, true);
 	}
 
 	public int getJobStatus(Job grisuJob, Credential credential,
 			boolean restart) {
 
 		final String handle = grisuJob.getJobhandle();
 
 		final Gram5JobListener l = Gram5JobListener.getJobListener();
 
 		final String contact = getContactString(handle);
 		final GramJob job = new GramJob(null);
 		GramJob restartJob = new GramJob(null);
 		final GSSCredential cred = credential.getCredential();
 
        // try to get the state from the notification listener cache
        Integer jobStatus = l.getStatus(handle);
        Integer error = l.getError(handle);
        if (jobStatus != null &&
              (jobStatus == GRAMConstants.STATUS_DONE ||
               jobStatus == GRAMConstants.STATUS_FAILED)) {
              return translateToGrisuStatus(jobStatus, error, error);			
        }
		
 		try {
 			// lets try to see if gateway is working first...
 			Gram.ping(cred, contact);
 		} catch (final GramException ex) {
 			myLogger.info("pinging " + contact + " failed. Returning status 'Unsubmitted'.", ex);
 			// have no idea what the status is, gateway is down:
 			return translateToGrisuStatus(GRAMConstants.STATUS_UNSUBMITTED,
 					ex.getErrorCode(), 0);
 		} catch (final GSSException ex) {
 			myLogger.info("pinging " + contact + " failed. Returning status 'Unsubmitted'.", ex);
 			return translateToGrisuStatus(GRAMConstants.STATUS_UNSUBMITTED, 0,
 					0);
 		}
 
 		try {
 			job.setID(handle);
 			job.setCredentials(cred);
 			Gram.jobStatus(job);
 			jobStatus = job.getStatus();
 			if ((jobStatus == GRAMConstants.STATUS_DONE)
 					|| (jobStatus == GRAMConstants.STATUS_FAILED)) {
 				job.signal(GRAMConstants.SIGNAL_COMMIT_END);
 			}
 			return translateToGrisuStatus(jobStatus, job.getError(),
 					job.getError());
 
 		} catch (final GramException ex) {
 			myLogger.debug("ok, normal method of getting exit status is not working. need to restart job.");
 			if (((ex.getErrorCode() == 156)
 					|| (ex.getErrorCode() == GRAMProtocolErrorConstants.CONNECTION_FAILED) || (ex
 							.getErrorCode() == 79)) && restart) {
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
 						return translateToGrisuStatus(
 								GRAMConstants.STATUS_ACTIVE, 131, 0);
 					}
 					// something is really wrong
 					myLogger.error("restarting job " + handle + " failed. returning status 'Failed'.", ex1);
 					return translateToGrisuStatus(GRAMConstants.STATUS_FAILED,
 							restartJob.getError(), 0);
 				} catch (final GSSException ex1) {
 					myLogger.error("restarting job " + handle + " failed. returning status 'Unsubmitted'.", ex1);
 					return translateToGrisuStatus(
 							GRAMConstants.STATUS_UNSUBMITTED, 0, 0);
 				}
 
 				grisuJob.setJobhandle(restartJob.getIDAsString());
 				final JobDAO jobdao = new JobDAO();
 				jobdao.saveOrUpdate(grisuJob);
 
 				// nope, not done yet.
 				return getJobStatus(grisuJob, credential, false);
 			} else if (ex.getErrorCode() == 156) {
 				// second restart didn't work - assume the job is done
 				// this bit is only needed during transition between releases
 				return translateToGrisuStatus(GRAMConstants.STATUS_DONE, 0, 0);
 
 			} else {
 				myLogger.error("something else is wrong. error code is "
 						+ ex.getErrorCode());
 				myLogger.error(ex.getLocalizedMessage(), ex);
 				return translateToGrisuStatus(GRAMConstants.STATUS_UNSUBMITTED,
 						0, 0);
 			}
 
 		} catch (final GSSException ex) {
 			myLogger.error(ex.getLocalizedMessage(), ex);
 			return translateToGrisuStatus(GRAMConstants.STATUS_UNSUBMITTED, 0,
 					0);
 		} catch (final MalformedURLException ex) {
 			myLogger.error(ex.getLocalizedMessage(), ex);
 			return translateToGrisuStatus(GRAMConstants.STATUS_UNSUBMITTED, 0,
 					0);
 		}
 
 	}
 
 	@Override
 	public String getServerEndpoint(String server) {
 		return server;
 	}
 
 	@Override
 	public int killJob(Job grisuJob, Credential cred) {
 
 		final GramJob job = new GramJob(null);
 		try {
 			job.setID(grisuJob.getJobhandle());
 			job.setCredentials(cred.getCredential());
 			try {
 				Gram.cancel(job);
 				Gram.jobStatus(job);
 			} catch (final GramException ex) {
 				myLogger.error(ex.getLocalizedMessage());
 			} catch (final GSSException ex) {
 				myLogger.error(ex.getLocalizedMessage());
 			}
 
 			return getJobStatus(grisuJob, cred, true);
 		} catch (final MalformedURLException ex) {
 			myLogger.error(ex.getLocalizedMessage());
 			return JobConstants.UNDEFINED;
 		}
 	}
 
 	@Override
 	protected String submit(String host,
 			String factoryType, Job job) throws ServerJobSubmissionException {
 
 		final RSLFactory f = RSLFactory.getRSLFactory();
 		String rsl = null;
 
 		try {
 			rsl = f.create(job.getJobDescription(), job.getFqan()).toString();
 		} catch (final RSLCreationException rex) {
 			throw new ServerJobSubmissionException(rex);
 		}
 
 		myLogger.debug("RSL is ... " + rsl);
 		GSSCredential credential = null;
 
 		try {
 			// credential =
 			// CredentialHelpers.convertByteArrayToGSSCredential(job
 			// .getCredential().getCredentialData());
 			credential = job.getCredential().getCredential();
 
 			final GramJob gt5Job = new GramJob(rsl);
 			final Gram5JobListener l = Gram5JobListener.getJobListener();
 			gt5Job.setCredentials(credential);
 			gt5Job.addListener(l);
 
 			try {
 				gt5Job.request(host, false);
 			} catch (final WaitingForCommitException cex) {
 				gt5Job.signal(GRAMConstants.SIGNAL_COMMIT_REQUEST);
 			}
 			// gt5Job.bind();
 			// gt5Job.getStatus();
 
 			job.setSubmittedJobDescription(rsl);
 
 			return gt5Job.getIDAsString();
 
 		} catch (final GSSException gss) {
 			myLogger.error(gss.getLocalizedMessage(), gss);
 			throw new ServerJobSubmissionException("job credential is invalid");
 		} catch (final GramException gex) {
 			throw new ServerJobSubmissionException(gex.getLocalizedMessage(),
 					gex);
 		}
 
 	}
 
 	private int translateToGrisuStatus(int status, int failureCode, int exitCode) {
 
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
 			if (failureCode == GRAMProtocolErrorConstants.USER_CANCELLED) {
 				grisu_status = JobConstants.KILLED;
 			} else {
 				grisu_status = JobConstants.FAILED;
 			}
 		} else if (status == GRAMConstants.STATUS_SUSPENDED) {
 			grisu_status = JobConstants.ACTIVE;
 		} else {
 			// needed for transition period to deal with jobs submitted without
 			// two-phase commit
 			if (failureCode == 156) {
 				grisu_status = JobConstants.DONE;
 			} else {
 				grisu_status = JobConstants.UNSUBMITTED;
 			}
 		}
 		return grisu_status;
 
 	}
 }
