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
 
 package br.com.caelum.integracao.client;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.util.Calendar;
 import java.util.List;
 import java.util.Scanner;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import br.com.caelum.integracao.client.project.Project;
 import br.com.caelum.integracao.client.project.ProjectRunResult;
 import br.com.caelum.integracao.http.DefaultHttp;
 import br.com.caelum.integracao.http.Http;
 import br.com.caelum.integracao.http.Method;
 import br.com.caelum.vraptor.ioc.ApplicationScoped;
 
 @ApplicationScoped
 public class CurrentJob {
 
 	private final Logger logger = LoggerFactory.getLogger(CurrentJob.class);
 
 	private Project project;
 	private Thread thread;
 	private final Settings point;
 
 	private Calendar start;
 
 	private File outputFile;
 
 	private String jobId;
 
 	public CurrentJob(Settings settings) {
 		this.point = settings;
 	}
 
 	public boolean isRunning() {
 		return project != null;
 	}
 
 	public Project getProject() {
 		return project;
 	}
 
 	public Thread getThread() {
 		return thread;
 	}
 
 	public synchronized void start(String jobId, Project project, final String revision, final List<String> startCommand,
 			final List<String> stopCommand, final String resultUri) {
 		if (isRunning()) {
 			throw new RuntimeException("Cannot take another job as im currently processing " + this.project.getName());
 		}
 		this.jobId = jobId;
 		this.start = Calendar.getInstance();
 		this.project = project;
 		Runnable runnable = new Runnable() {
 			public void run() {
 				try {
 					executeBuildFor(revision, startCommand, stopCommand, resultUri);
 				} finally {
 					CurrentJob.this.jobId = null;
 					CurrentJob.this.project = null;
 					CurrentJob.this.thread = null;
 					CurrentJob.this.start = null;
 					CurrentJob.this.outputFile = null;
 				}
 			}
 		};
 		this.thread = new Thread(runnable, project.getName() + " revision " + revision);
 		thread.start();
 	}
 
 	private void executeBuildFor(String revision, List<String> startCommand, List<String> stopCommand, String resultUri) {
 		ProjectRunResult checkoutResult = null;
 		ProjectRunResult startResult = null;
 		ProjectRunResult stopResult = null;
 		try {
 
 			this.outputFile = File.createTempFile("integra-client-run-", ".txt");
 			outputFile.deleteOnExit();
 
 			checkoutResult = project.checkout(point.getBaseDir(), revision, outputFile);
 			if (!checkoutResult.failed()) {
 				startResult = project.run(this.point.getBaseDir(), startCommand, outputFile);
 			}
 
 		} catch (Exception e) {
 			logger.debug("Something wrong happened during the checkout/build", e);
 			StringWriter writer = new StringWriter();
 			e.printStackTrace(new PrintWriter(writer, true));
 			startResult = new ProjectRunResult(writer.toString(), -1);
 		} finally {
 			if (project != null) {
 				try {
 					if (stopCommand != null && stopCommand.size() != 0) {
 						File stopFile = File.createTempFile("integra-client-run-", ".txt");
 						stopFile.deleteOnExit();
 						stopResult = project.run(this.point.getBaseDir(), stopCommand, stopFile);
 					} else {
 						stopResult = new ProjectRunResult("No command to run.", 0);
 					}
 				} catch (IOException e) {
 					logger.error("Unable to stop command on server!", e);
 				} finally {
 					logger.debug("Job " + project.getName() + " has finished");
 					Http http = new DefaultHttp();
 					logger.debug("Acessing uri " + resultUri + " to finish the job");
 					Method post = http.post(resultUri);
 					try {
 						addTo(post, checkoutResult, "checkout");
 						addTo(post, startResult, "start");
 						addTo(post, stopResult, "stop");
 						post.with("success", ""
 								+ !(checkoutResult.failed() || startResult.failed() || stopResult.failed()));
 						post.send();
 						if (post.getResult() != 200) {
 							logger.error(post.getContent());
 							throw new RuntimeException("The server returned a problematic answer: " + post.getResult());
 						}
 					} catch (Exception e) {
 						logger
 								.error(
 										"Was unable to notify the server of this request... maybe the server think im still busy.",
 										e);
 					}
 				}
 			}
 		}
 	}
 
 	private void addTo(Method post, ProjectRunResult result, String prefix) {
 		if (result != null) {
 			post.with(prefix + "Result", result.getContent());
 		} else {
 			post.with(prefix + "Result", "unable to read result");
 		}
 	}
 
 	public synchronized boolean stop(String jobIdToStop) {
		logger.debug("Stopping job, looking for " + jobIdToStop);
 		if(this.jobId == null) {
 			logger.warn("Could not stop " + jobIdToStop + " because I am not running anything");
 			return true;
 		}
 		if(!this.jobId.equals(jobIdToStop)) {
 			logger.error("Could not stop " + jobIdToStop + " because I am running: " + this.jobId);
 			return false;
 		}
 		if (this.thread != null) {
 			this.thread.interrupt();
 		}
 		if (this.project != null) {
 			this.project.stop();
 		}
 		return true;
 	}
 
 	public double getTime() {
 		return (System.currentTimeMillis() - start.getTimeInMillis()) / 1000.0;
 	}
 
 	public Calendar getStart() {
 		return start;
 	}
 
 	public String getOutputContent() throws FileNotFoundException {
 		Scanner sc = new Scanner(new FileInputStream(this.outputFile)).useDelimiter("117473826478234211");
 		try {
 			if (sc.hasNext()) {
 				return sc.next();
 			}
 			return "";
 		} finally {
 			sc.close();
 		}
 	}
 
 }
