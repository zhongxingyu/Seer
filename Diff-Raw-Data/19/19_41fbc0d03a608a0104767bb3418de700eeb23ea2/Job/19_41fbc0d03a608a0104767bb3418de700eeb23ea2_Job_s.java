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
 package br.com.caelum.integracao.server.queue;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.net.UnknownHostException;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.ManyToOne;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import br.com.caelum.integracao.server.Build;
 import br.com.caelum.integracao.server.BuildCommand;
 import br.com.caelum.integracao.server.Client;
 import br.com.caelum.integracao.server.Config;
 import br.com.caelum.integracao.server.Phase;
 import br.com.caelum.integracao.server.Project;
 import br.com.caelum.integracao.server.agent.Agent;
 import br.com.caelum.integracao.server.dao.Database;
 import br.com.caelum.integracao.zip.Unzipper;
 import br.com.caelum.vraptor.interceptor.multipart.UploadedFile;
 
 /**
  * Represents a schedulable job in the system.<br>
  * Jobs are commands which will run or ran in an agent and have a maximum number of retries.<br>
  * 
  * @author guilherme silveira
  */
 @Entity
 public class Job {
 
 	private static final Logger logger = LoggerFactory.getLogger(Job.class);
 
 	@ManyToOne
 	private Build build;
 
 	@ManyToOne
 	private BuildCommand command;
 
 	@ManyToOne
 	private Client client;
 
 	@Id
 	@GeneratedValue
 	private Long id;
 	@Temporal(TemporalType.TIMESTAMP)
 	private Calendar schedulingTime = new GregorianCalendar();
 
 	private boolean finished;
 
 	@Temporal(TemporalType.TIMESTAMP)
 	private Calendar startTime;
 
 	private Calendar finishTime;
 	
 	private int timesTriedSoFar = 0;
 
 	private boolean success = false;
 
 	Job() {
 	}
 
 	public Job(Build build, BuildCommand command) {
 		this.build = build;
 		this.command = command;
 	}
 
 	public boolean executeAt(Client at, Config config) throws UnknownHostException {
 		logger.debug("Trying to execute " + command + " @ " + at);
 		Agent agent = at.getAgent();
 		if (agent.register(build.getProject())) {
 			if (agent.execute(this, config.getUrl(), build)) {
 				useClient(at);
 				this.startTime = Calendar.getInstance();
 				timesTriedSoFar++;
 				return true;
 			}
 		}
 		return false;
 	}
 
 	void useClient(Client at) {
 		this.client = at;
 	}
 
 	public void finish(String result, boolean success, Database database, String zipOutput, UploadedFile content,
 			String artifactsOutput, UploadedFile artifacts) throws IOException {
 
 		Project project = build.getProject();
 		logger.debug("Job #" + id + " - finishing " + project.getName() + " build count " + build.getBuildCount() + " phase "
 				+ command.getPhase().getName() + " command " + command.getId());
 
		unzip(content, zipOutput, "report-copy-result.txt", "");
		unzip(artifacts, artifactsOutput, "artifacts-copy-result.txt", "/artifacts");
		if (artifacts != null) {
			build.publishArtifact(getFile(command.getId() + "/artifacts"));
		}

 		this.success = success;
 		if (!success) {
 			build.failed();
 		}
 		this.finished = true;
 		this.finishTime = Calendar.getInstance();
 
 		Phase phase = command.getPhase();
 		File file = getFile(command.getName() + ".txt");
 		if(file.getParentFile().mkdirs()){
 			PrintWriter writer = new PrintWriter(new FileWriter(file), true);
 			writer.print(result);
 			writer.close();
 		}
 
 		build.proceed(phase, database);
 
 	}
 
 	private void unzip(UploadedFile uploaded, String output, String logFilename, String pathToUnzip) throws IOException {
 		File baseDir = getFile(command.getId() + "" + pathToUnzip);
 		if(baseDir.mkdirs()) {
 			PrintWriter unzipLog = new PrintWriter(new FileWriter(new File(baseDir, logFilename)), true);
 			unzipLog.append(output);
 			if (uploaded != null) {
 				new Unzipper(baseDir).logTo(unzipLog).unzip(uploaded.getFile());
 			}
 			unzipLog.close();
 		}
 	}
 
 	private File getFile(String name) {
 		File file = build.getFile(command.getPhase().getName() + "/" + name);
 		return file;
 	}
 
 	public Build getBuild() {
 		return build;
 	}
 
 	public Client getClient() {
 		return client;
 	}
 
 	public BuildCommand getCommand() {
 		return command;
 	}
 
 	public Long getId() {
 		return id;
 	}
 
 	public boolean isFinished() {
 		return finished;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	public Calendar getSchedulingTime() {
 		return schedulingTime;
 	}
 
 	public Calendar getStartTime() {
 		return startTime;
 	}
 
 	public Calendar getFinishTime() {
 		return finishTime;
 	}
 
 	public double getRuntime() {
 		Calendar f = Calendar.getInstance();
 		if (finishTime != null) {
 			f = finishTime;
 		}
 		return (f.getTimeInMillis() - startTime.getTimeInMillis()) / 1000.0;
 	}
 
 	public boolean isSuccess() {
 		return success;
 	}
 
 	void setFinished(boolean b) {
 		this.finished = true;
 	}
 
 	public void reschedule() {
 		logger.debug("Rescheduling job " + getId());
 		this.client = null;
 		this.startTime = null;
 	}
 
 	public boolean canReschedule() {
 		return timesTriedSoFar<3;
 	}
 
 }
