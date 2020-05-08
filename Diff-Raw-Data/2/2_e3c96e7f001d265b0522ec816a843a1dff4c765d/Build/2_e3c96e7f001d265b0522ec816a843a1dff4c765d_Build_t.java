 /***
  * 
  * Copyright (c) 2009 Caelum - www.caelum.com.br/opensource All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 1. Redistributions of source code must retain the above copyright notice,
  * this list of conditions and the following disclaimer. 2. Redistributions in
  * binary form must reproduce the above copyright notice, this list of
  * conditions and the following disclaimer in the documentation and/or other
  * materials provided with the distribution. 3. Neither the name of the
  * copyright holders nor the names of its contributors may be used to endorse or
  * promote products derived from this software without specific prior written
  * permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 package br.com.caelum.integracao.server;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 
 import org.hibernate.validator.Min;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import br.com.caelum.integracao.server.build.Tab;
 import br.com.caelum.integracao.server.dao.Database;
 import br.com.caelum.integracao.server.log.LogFile;
 import br.com.caelum.integracao.server.plugin.Plugin;
 import br.com.caelum.integracao.server.plugin.PluginException;
 import br.com.caelum.integracao.server.plugin.PluginToRun;
 import br.com.caelum.integracao.server.queue.Job;
 import br.com.caelum.integracao.server.queue.Jobs;
 import br.com.caelum.integracao.server.scm.Revision;
 import br.com.caelum.integracao.server.scm.ScmControl;
 import br.com.caelum.integracao.server.scm.ScmException;
 import br.com.caelum.integracao.zip.Zipper;
 
 /**
  * Represents an build either in process or already processed.
  * 
  * @author guilherme silveira
  * 
  */
 @Entity
 public class Build {
 
 	private static final Logger logger = LoggerFactory.getLogger(Build.class);
 
 	@ManyToOne
 	private Project project;
 
 	@Id
 	@GeneratedValue
 	private Long id;
 
 	@Min(0)
 	private Long buildCount;
 
 	@ManyToOne
 	private Revision revision;
 
 	@Min(0)
 	private int currentPhase = 0;
 
 	private boolean successSoFar = true;
 
 	private boolean finished = false;
 
 	/** when the build started */
 	private Calendar startTime = new GregorianCalendar();
 
 	/** when the build finished */
 	private Calendar finishTime;
 
 	@OneToMany(mappedBy = "build")
 	private List<Job> jobs = new ArrayList<Job>();
 
 	@OneToMany(mappedBy = "build")
 	private List<Tab> tabs = new ArrayList<Tab>();
 
 	@Column(length = 10000)
 	private String resultMessage;
 
 	Build() {
 	}
 
 	public Build(Project project) {
 		this.project = project;
 		this.buildCount = project.nextBuild();
 
 		// clears the base directory before using it
 		File baseDirectory = getBaseDirectory();
 		remove(baseDirectory);
 		baseDirectory.mkdirs();
 	}
 
 	public Revision getRevision() {
 		return revision;
 	}
 
 	public Long getBuildCount() {
 		return buildCount;
 	}
 
 	public File[] getContent() {
 		return getBaseDirectory().listFiles();
 	}
 
 	public File getBaseDirectory() {
 		return new File(project.getBuildsDirectory(), directoryName());
 	}
 
 	private String directoryName() {
 		return "build-" + buildCount;
 	}
 
 	public File getFile(String filename) {
 		return new File(getBaseDirectory(), filename);
 	}
 
 	public boolean setRevisionAsNextOne(Projects projects, Builds builds, Database database, String name) {
 		this.currentPhase = 0;
 		logger.debug("Starting executing build for " + project.getName() + " at "
 				+ project.getBaseDir().getAbsolutePath());
 		File file = getFile("revision-checkout.txt");
 		projects.register(new Tab(this, "Checkout commands and log", "revision-checkout.txt"));
 		LogFile logFile = null;
 		try {
 			logFile = new LogFile(file);
 			ScmControl control = project.getControl();
 			if (name == null) {
 				this.revision = project.extractNextRevision(this, builds, control, logFile);
 			} else {
 				this.revision = project.extractRevision(builds, control, logFile, name);
 			}
 			return checkoutContent(database, logFile, control);
 		} catch (Exception ex) {
 			finish(false, "Unable to retrieve revision for " + project.getName(), ex, database);
 			return false;
 		} finally {
 			if (logFile != null) {
 				logFile.close();
 			}
 		}
 	}
 
 	private boolean checkoutContent(Database database, LogFile logFile, ScmControl control) throws ScmException {
 		int result = control.checkoutOrUpdate(revision.getName(), new PrintWriter(logFile.getWriter()) {
 			public void close() {
 			}
 		});
 		if (result != 0) {
 			finish(false, "Unable to checkout project from scm.", null, database);
 			return false;
 		}
 		if (!getRevisionContent().exists()) {
 			try {
 				File revisionDirectory = new File(project.getBaseDir(), project.getName());
 				String scmPattern = control.getIgnorePattern();
 				int zipped = new Zipper(revisionDirectory).ignore(scmPattern).addExactly("").logTo(logFile.getWriter())
 						.zip(getRevisionContent());
 				if (zipped == 0) {
 					logFile.getWriter().println("Did not zip any files.");
 					finish(false, "Unable to zip revision content.", null, database);
 					return false;
 				}
 			} catch (IOException ex) {
 				finish(false, "Unable to zip files for this revision", ex, database);
 				return false;
 			}
 		}
 		return true;
 	}
 
 	public void start(Jobs jobs, Database db) throws ScmException {
 		for (PluginToRun toRun : project.getPlugins()) {
 			try {
 				Plugin found = toRun.getPlugin(db);
 				if (!found.before(this)) {
 					finish(false,
 							"Plugin " + toRun.getType().getInformation().getName() + " told us to stop the build",
 							null, db);
 					return;
 				}
 			} catch (PluginException e) {
 				finish(false, "Plugin " + toRun.getType().getInformation().getName() + " was not instantiated", null,
 						db);
 				return;
 			}
 		}
 		List<Phase> phases = project.getPhases();
 		if (!phases.isEmpty()) {
 			Phase phase = phases.get(0);
 			if (phase.getCommandCount()==0) {
 				finish(false, "There are no commands at the first phase.", null, db);
 			} else {
 				phase.execute(this, jobs);
 			}
 		} else {
 			finish(false, "There were no phases to run.", null, db);
 		}
 	}
 
 	void finish(boolean success, String cause, Exception ex, Database db) {
 		this.finished = true;
 		StringWriter writer = new StringWriter();
 		writer.write(cause + "\n");
 		if (ex != null) {
 			ex.printStackTrace(new PrintWriter(writer, true));
 		}
 		this.resultMessage = writer.getBuffer().toString();
 		this.successSoFar = success;
 		this.finishTime = new GregorianCalendar();
 		for (PluginToRun toRun : project.getPlugins()) {
 			try {
 				Plugin found = toRun.getPlugin(db);
 				found.after(this);
 			} catch (PluginException e) {
 				cause += " (plugin error: " + toRun.getType().getInformation().getName() + ")";
 				logger.error("plugin error", e);
 			}
 		}
 	}
 
 	public Project getProject() {
 		return this.project;
 	}
 
 	public int getCurrentPhase() {
 		return currentPhase;
 	}
 
 	public boolean isSuccessSoFar() {
 		return successSoFar;
 	}
 
 	public boolean isFinished() {
 		return finished;
 	}
 
 	public double getRuntime() {
 		Calendar f = Calendar.getInstance();
 		if (finishTime != null) {
 			f = finishTime;
 		}
		return (f.getTimeInMillis() - startTime.getTimeInMillis()) / 1000.0 / 60.0;
 	}
 
 	public Calendar getFinishTime() {
 		return finishTime;
 	}
 
 	public Calendar getStartTime() {
 		return startTime;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	public Long getId() {
 		return id;
 	}
 
 	public boolean buildStatusChangedFromLastBuild() {
 		Build previous = getProject().getBuild(getBuildCount() - 1);
 		if (previous == null) {
 			// first build ever
 			return true;
 		}
 		return previous.isSuccessSoFar() != isSuccessSoFar();
 	}
 
 	public List<Job> getJobsFor(Phase phase) {
 		List<Job> clients = new ArrayList<Job>();
 		for (Job client : jobs) {
 			if (phase.getCommands().contains(client.getCommand())) {
 				clients.add(client);
 			}
 		}
 		return clients;
 	}
 
 	public void remove(Database database) {
 		database.getSession().delete(this);
 		remove(getBaseDirectory());
 	}
 
 	private void remove(File dir) {
 		if (!dir.exists()) {
 			return;
 		}
 		for (File found : dir.listFiles()) {
 			if (found.isDirectory()) {
 				remove(found);
 			} else {
 				found.delete();
 			}
 		}
 		dir.delete();
 	}
 
 	public void failed() {
 		this.successSoFar = false;
 	}
 
 	public void proceed(Phase actualPhase, Database database) throws IOException {
 		List<Job> jobs = getJobsFor(actualPhase);
 		int finished = 0;
 		for (Job j : jobs) {
 			if (j.isFinished()) {
 				finished++;
 			}
 		}
 		boolean executedAllCommands = finished == actualPhase.getCommandCount();
 		if (executedAllCommands) {
 			logger.debug("Preparing to execute plugins for " + getProject().getName() + " with success = "
 					+ successSoFar);
 			boolean thisResult = actualPhase.runAfter(this, database);
 			successSoFar &= thisResult;
 			if (successSoFar) {
 				currentPhase++;
 				if (project.getPhases().size() != currentPhase) {
 					Phase nextPhase = project.getPhases().get(currentPhase);
 					if(nextPhase.getCommandCount()==0) {
 						finish(false, "Phase " + nextPhase.getName() + " has no commands", null, database);
 					} else {
 						nextPhase.execute(this, new Jobs(database));
 					}
 				} else {
 					finish(true, "Well done.", null, database);
 				}
 			} else {
 				finish(false, "One or more commands failed.", null, database);
 			}
 		}
 	}
 
 	public String getRevisionName() {
 		if (revision == null) {
 			return "unknown";
 		}
 		return revision.getName();
 	}
 
 	public List<Tab> getTabs() {
 		return tabs;
 	}
 
 	public File getRevisionContent() {
 		return new File(project.getBuildsDirectory(), "revision-" + revision.getName() + ".zip");
 	}
 
 	public String getResultMessage() {
 		return resultMessage;
 	}
 
 	public boolean hasRun(Phase phase) {
 		int position = phase.getProject().getPhases().indexOf(phase);
 		if ((currentPhase == position) && isFinished()) {
 			return true;
 		}
 		return currentPhase > position;
 	}
 
 	public boolean isRunning(Phase phase) {
 		int position = phase.getProject().getPhases().indexOf(phase);
 		return currentPhase == position && !isFinished();
 	}
 
 	public boolean hasSucceeded(Phase phase) {
 		List<Job> jobs = getJobsFor(phase);
 		for (Job j : jobs) {
 			if (!j.isSuccess()) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	public File getArtifactsToPush() {
 		return getFile("artifactsToPush.zip");
 	}
 
 	public void publishArtifact(File directory) throws IOException {
 		new Zipper(directory).addExactly("").logTo(new PrintWriter(System.out)).zip(getArtifactsToPush(), true);
 	}
 	
 }
