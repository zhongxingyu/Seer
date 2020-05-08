 package org.bestgrid.virtscreen.model.gromacs;
 
 import grisu.control.ServiceInterface;
 import grisu.control.exceptions.JobPropertiesException;
 import grisu.control.exceptions.JobSubmissionException;
 import grisu.frontend.control.jobMonitoring.RunningJobManager;
 import grisu.frontend.control.login.LoginManager;
 import grisu.frontend.model.job.JobObject;
 import grisu.model.dto.GridFile;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.io.FileUtils;
 import org.bestgrid.virtscreen.control.VirtScreenEnvironment;
 import org.bestgrid.virtscreen.view.GrisuVirtScreen;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class GromacsJob {
 
 	static final Logger myLogger = LoggerFactory.getLogger(GromacsJob.class);
 
 	public static final File GROMACS_JOB_CONTROL_SCRIPT = new File(
 			VirtScreenEnvironment.VIRTSCREEN_PLUGIN_DIR, "gromacs.sh");
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) throws Exception {
 
 		ServiceInterface si = LoginManager.login("bestgrid", true);
 
 		JobObject job = new JobObject(si);
 
 		job.setJobname("mpi_gromacs_2cpus");
 
 		job.setCpus(2);
 		job.setForce_single(true);
 
 		job.setApplication("GROMACS");
 		job.setApplicationVersion("4.5.4");
 		job.setSubmissionLocation("pan:pan.nesi.org.nz");
 
 		job.addInputFileUrl("/home/markus/Desktop/gromacs_one/md_relax_2.mdp");
 		job.addInputFileUrl("/home/markus/Desktop/gromacs_one/4a55new_loopall_p110a_md_1_pme.gro");
 		job.addInputFileUrl("/home/markus/Desktop/gromacs_one/4a55new_loopall_p110a.top");
 
 		job.addInputFileUrl("/media/data/work/Workspaces/Grisu/grisu-virtscreen/src/main/resources/gromacs.sh");
 
 
 		
 		job.setCommandline("sh gromacs.sh");
 		job.setWalltimeInSeconds(3600 * 23);
 
 		job.createJob("/nz/nesi");
 
 		job.submitJob();
 
 	}
 	private GridFile groFile;
 	private GridFile topFile;
 
 	private List<GridFile> mbpFiles;
 	private File mdpFileOrder;
 
 	private int cpus = 64;
 
 	private int walltimeInSeconds;
 	private String email;
 	private boolean email_on_finish = false;
 
 	private boolean email_on_start = false;
 
 	private JobObject job;
 
 	private ServiceInterface si;
 
 	public GromacsJob(ServiceInterface si) {
 		this.si = si;
 		this.job = new JobObject(si);
 	}
 
 	public synchronized void createAndSubmitJob()
 			throws JobSubmissionException, JobPropertiesException {
 
 		// job.setTimestampJobname(FilenameUtils.getBaseName(goldConfFile
 		// .getName()));
 		job.setTimestampJobname("gromacs");
 		job.setApplication("GROMACS");
 		job.setApplicationVersion(GrisuVirtScreen.GROMACS_VERSION);
 		job.setSubmissionLocation(GrisuVirtScreen.SUBMISSION_LOCATION);
 		
 		job.setEmail_address(email);
 		job.setEmail_on_job_finish(email_on_finish);
 		job.setEmail_on_job_start(email_on_start);
 
 		String commandline = "/share/apps/gromacs/gromacs_workflow_new.sh -d . -i "+getGroFile().getName()+" -t "+getTopFile().getName() + " -f "+mdpFileOrder.getName();
 		if ( this.cpus > 1 ) {
 			commandline = commandline + " -mpi";
 		}
 		job.setCommandline(commandline);
 		job.setCpus(this.cpus);
 		job.setForce_single(true);
 		job.setWalltimeInSeconds(walltimeInSeconds);
 
 		job.addInputFileUrl(GROMACS_JOB_CONTROL_SCRIPT.getPath());
 		job.addInputFileUrl(getGroFile().getUrl());
 		job.addInputFileUrl(getTopFile().getUrl());
 		
 		job.addInputFileUrl(mdpFileOrder.getAbsolutePath());
 
 		for (GridFile f : getMbpFiles()) {
 			job.addInputFileUrl(f.getUrl());
 		}
 
 		RunningJobManager.getDefault(si).createJob(job,
				"/nz/nesi/projects/nesi00031");
 		//GrisuVirtScreen.SUBMISSION_VO);
 
 		final Map<String, String> additionalJobProperties = new HashMap<String, String>();
 
 		// final String dir = this.goldConfFile.getDirectory();
 		// final String conc = this.goldConfFile.getConcatenatedOutput();
 		//
 		// additionalJobProperties.put("result_directory", dir);
 		// additionalJobProperties.put("concatenated_output", conc);
 
 		try {
 			job.submitJob(additionalJobProperties);
 		} catch (final InterruptedException e) {
 			myLogger.error(e.getLocalizedMessage(), e);
 		}
 
 		job = null;
 
 	}
 
 	public int getCpus() {
 		return cpus;
 	}
 
 	public String getEmail() {
 		return email;
 	}
 
 	public GridFile getGroFile() {
 		return groFile;
 	}
 
 	public JobObject getJobObject() {
 		return this.job;
 	}
 
 	public List<GridFile> getMbpFiles() {
 		return mbpFiles;
 	}
 
 	public GridFile getTopFile() {
 		return topFile;
 	}
 
 	public int getWalltimeInSeconds() {
 		return walltimeInSeconds;
 	}
 
 	public boolean isEmail_on_finish() {
 		return email_on_finish;
 	}
 
 	public boolean isEmail_on_start() {
 		return email_on_start;
 	}
 
 	public void setCpus(int cpus) {
 		this.cpus = cpus;
 	}
 
 	public void setEmail(String emailAddress) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void setEmail_on_finish(boolean email_on_finish) {
 		this.email_on_finish = email_on_finish;
 	}
 
 	public void setEmail_on_start(boolean email_on_start) {
 		this.email_on_start = email_on_start;
 	}
 
 	public void setGroFile(GridFile groFile) {
 		this.groFile = groFile;
 	}
 
 	public void setMbpFiles(List<GridFile> mdpFiles) {
 		this.mbpFiles = mdpFiles;
 		try {
 			mdpFileOrder = File.createTempFile("mdp_order_", ".txt");
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		} 
 		StringBuffer temp = new StringBuffer();
 		for (GridFile f : mdpFiles) {
 			temp.append(f.getName()+"\n");
 		}
 		try {
 			FileUtils.writeStringToFile(mdpFileOrder, temp.toString());
 		} catch (IOException e) {
 			throw new RuntimeException();
 		}
 	}
 
 	public void setTopFile(GridFile topFile) {
 		this.topFile = topFile;
 	}
 
 	public void setWalltimeInSeconds(int walltimeInSeconds) {
 		this.walltimeInSeconds = walltimeInSeconds;
 	}
 
 }
