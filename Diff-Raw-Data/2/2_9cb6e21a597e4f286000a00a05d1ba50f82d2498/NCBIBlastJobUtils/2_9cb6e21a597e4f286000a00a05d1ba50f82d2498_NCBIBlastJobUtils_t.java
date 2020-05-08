 package uk.org.mygrid.cagrid.servicewrapper.service.ncbiblast.invoker;
 
 import gov.nih.nci.cagrid.common.Utils;
 import gov.nih.nci.cagrid.metadata.service.Fault;
 
 import java.io.StringReader;
 import java.rmi.RemoteException;
 
 import org.apache.log4j.Logger;
 import org.jdom.Document;
 import org.jdom.output.XMLOutputter;
 
 import schema.EBIApplicationResult;
 import uk.org.mygrid.cagrid.domain.common.JobStatus;
 import uk.org.mygrid.cagrid.domain.ncbiblast.NCBIBLASTOutput;
 import uk.org.mygrid.cagrid.servicewrapper.service.ncbiblast.converter.NCBIBlastConverter;
 import uk.org.mygrid.cagrid.servicewrapper.service.ncbiblast.job.service.globus.resource.NCBIBlastJobResource;
 import uk.org.mygrid.cagrid.servicewrapper.serviceinvoker.InvokerException;
 import uk.org.mygrid.cagrid.servicewrapper.serviceinvoker.ncbiblast.NCBIBlastInvoker;
 
 /**
  * Utility methods for updating an NCBIBlast job resource.
  * 
  * @author Stian Soiland-Reyes
  * 
  */
 public class NCBIBlastJobUtils {
 
 	private static Logger logger = Logger.getLogger(NCBIBlastJobUtils.class);
 
 	private NCBIBlastInvoker invoker = InvokerFactory.getInvoker();
 
 	private NCBIBlastConverter converter = new NCBIBlastConverter();
 
 	/**
 	 * Update the {@link JobStatus} of the given job.
 	 * 
 	 * @param job
 	 * @throws RemoteException
 	 */
 	public void updateStatus(NCBIBlastJobResource job) throws RemoteException {
 		if (isFinished(job) && job.getNCBIBlastOutput() != null
 				|| job.getFault() != null) {
 			// No need to check status again, and the return data has been
 			// fetched
 			return;
 		}
 		String jobID = job.getJobID();
 		if (jobID == null || jobID.equals("")) {
 			// Too early, no job id set yet
 			return;
 		}
 
 		String status;
 		try {
 			status = invoker.checkStatus(jobID);
 		} catch (InvokerException e) {
 			logger.warn("Could not check status for " + jobID, e);
 			job.setFault(new Fault("Could not check status for " + jobID,
 					"Can't check status"));
 			throw new RemoteException("Could not check status for " + jobID, e);
 		}
 		logger.info("Status for " + jobID + " is " + status);
 		JobStatus jobStatus;
 		try {
 			jobStatus = JobStatus.fromValue(status.toLowerCase());
 		} catch (IllegalArgumentException ex) {
 			job.setFault(new Fault("Unknown status type for " + jobID + ": "
 					+ status, "Unknown status"));
 			logger.warn("Unknown status type for " + jobID + ": " + status, ex);
 			throw new RemoteException("Unknown status type " + status);
 		}
 		job.setJobStatus(jobStatus);
 	}
 
 	/**
 	 * Update the outputs of a given job. If the job status is not
 	 * {@link JobStatus#done}, or the output has already been fetched, this
 	 * method does nothing.
 	 * 
 	 * @param job
 	 * @throws RemoteException
 	 */
 	public void updateOutputs(NCBIBlastJobResource job) throws RemoteException {
 		if (!job.getJobStatus().equals(JobStatus.done)
 				|| job.getNCBIBlastOutput() != null) {
 			// Too early/late
 			return;
 		}
 		String jobID = job.getJobID();
 		if (jobID == null || jobID.equals("")) {
 			// Too early, no job id set yet
 			return;
 		}
 
 		Document data;
 		try {
 			data = invoker.poll(jobID);
 		} catch (InvokerException e) {
 			job.setFault(new Fault("Can't poll for job ID " + jobID,
 					"Can't poll"));
 			logger.warn("Can't poll for jobID " + jobID, e);
 			throw new RemoteException("Can't poll for jobID " + jobID, e);
 		}
 
 		
 
 		XMLOutputter xmlOutputter = new XMLOutputter();
 		String dataString = xmlOutputter.outputString(data);
 		StringReader xmlReader = new StringReader(dataString);
 		EBIApplicationResult eBIApplicationResult;
 		try {
 			eBIApplicationResult = (EBIApplicationResult) Utils.deserializeObject(xmlReader, 
 					EBIApplicationResult.class);
 			job.setEBIApplicationResult(eBIApplicationResult);
 		} catch (Exception e) {
			logger.warn("Could not parse/serialize returned data:\n" + dataString, e);
 		}
 		
 		logger.info("Data returned for " + jobID + " is: \n" + dataString);
 		NCBIBLASTOutput output = converter.convertNCBIBlastOutput(data);
 		job.setNCBIBlastOutput(output);
 	}
 
 	/**
 	 * Check if a job is finished. A job is considered finish if it has a job
 	 * status (not updated), and the status is either {@link JobStatus#error},
 	 * {@link JobStatus#not_found} or {@link JobStatus#done} - in which case it
 	 * also need to have a
 	 * {@link NCBIBlastJobResource#getNCBIBlastOutput()}.
 	 * 
 	 * @param job
 	 * @return <code>true</code> if the job is considered finished.
 	 */
 	public boolean isFinished(NCBIBlastJobResource job) {
 		JobStatus jobStatus = job.getJobStatus();
 		if (jobStatus == null) {
 			return false;
 		}
 		if (jobStatus.equals(JobStatus.error)
 				|| jobStatus.equals(JobStatus.not_found)) {
 			return true;
 		}
 		if (jobStatus.equals(JobStatus.done)
 				&& job.getNCBIBlastOutput() != null) {
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Update faults. Currently does not do much, as faults are set on
 	 * occurrence. If the
 	 * {@link NCBIBlastJobResource#getNCBIBlastOutput()} is not null, the
 	 * current fault is removed.
 	 * 
 	 * @param job
 	 * @throws RemoteException
 	 */
 	public void updateFault(NCBIBlastJobResource job) throws RemoteException {
 		if (job.getNCBIBlastOutput() != null) {
 			// No fault anymore
 			job.setFault(null);
 		}
 	}
 
 }
