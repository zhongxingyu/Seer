 package org.bestgrid.virtscreen.model.szybki;
 
 import grisu.control.ServiceInterface;
 import grisu.control.exceptions.JobPropertiesException;
 import grisu.control.exceptions.JobSubmissionException;
 import grisu.frontend.control.jobMonitoring.RunningJobManager;
 import grisu.frontend.control.login.LoginManager;
 import grisu.frontend.model.job.JobObject;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.commons.io.FilenameUtils;
 import org.bestgrid.virtscreen.control.VirtScreenEnvironment;
 
 public class SzybkiJob {
 
 	public static final File SZYBKI_JOB_CONTROL_SCRIPT = new File(
			VirtScreenEnvironment.VIRTSCREEN_PLUGIN_DIR, "gold.sh");
 
 	public static final File SZYBKI_HELPER_PY_SCRIPT = new File(
			VirtScreenEnvironment.VIRTSCREEN_PLUGIN_DIR, "gold.py");
 
 	public static void main(String[] args) throws Exception {
 
 		ServiceInterface si = LoginManager.loginCommandline("Local");
 
 		SzybkiInputFile input = new SzybkiInputFile(si);
 
 		input.setInputFile("/home/markus/Desktop/jack/szybki/example.param");
 
 		SzybkiJob job = new SzybkiJob(si, input);
 
 		job.createAndSubmitJob();
 
 	}
 
 	private final SzybkiInputFile szybkiInputFile;
 
 	private JobObject job;
 
 	private final ServiceInterface si;
 
 	private final int cpus = 1;
 	private final int walltimeInSeconds = 600;
 
 	public SzybkiJob(ServiceInterface si, SzybkiInputFile szybkiInputFile) {
 		this.si = si;
 		this.szybkiInputFile = szybkiInputFile;
 
 		job = new JobObject(si);
 	}
 
 	public void createAndSubmitJob() throws JobSubmissionException,
			JobPropertiesException {
 
 		job.setTimestampJobname(FilenameUtils.getBaseName(szybkiInputFile
 				.getName()));
 		job.setApplication("szybki");
 		job.setApplicationVersion("1.3.4");
 		job.setSubmissionLocation("szybki@er171.ceres.auckland.ac.nz:ng2.auckland.ac.nz");
 
 		job.setCommandline("sh szybki " + this.szybkiInputFile.getName());
 		job.setCpus(this.cpus);
 		job.setMemory(2L * 2147483648L * new Long(this.cpus));
 		// job.setHostCount(1);
 		job.setForce_single(true);
 		job.setWalltimeInSeconds(walltimeInSeconds);
 
 		job.addInputFileUrl(SZYBKI_JOB_CONTROL_SCRIPT.getPath());
 		job.addInputFileUrl(SZYBKI_HELPER_PY_SCRIPT.getPath());
 		job.addInputFileUrl(szybkiInputFile.getJobConfFile().getPath());
 
 		// for (String url : goldConfFile.getFilesToStageIn()) {
 		// job.addInputFileUrl(url);
 		// }
 
 		RunningJobManager.getDefault(si).createJob(job,
				"/ARCS/BeSTGRID/Drug_discovery/Local");
 
 		Map<String, String> additionalJobProperties = new HashMap<String, String>();
 
 		try {
 			job.submitJob(additionalJobProperties);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 
 		job = null;
 
 	}
 }
