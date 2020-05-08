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
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.util.Calendar;
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import br.com.caelum.integracao.client.project.Project;
 import br.com.caelum.integracao.client.project.ProjectRunResult;
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
 
 	public synchronized void start(Project project, final String revision, final List<String> command,
 			final String resultUri, final String clientId) {
 		if (isRunning()) {
 			throw new RuntimeException("Cannot take another job as im currently processing " + this.project.getName());
 		}
 		this.start = Calendar.getInstance();
 		this.project = project;
 		Runnable runnable = new Runnable() {
 			public void run() {
 				try {
 					executeBuildFor(revision, command, resultUri, clientId);
 				} finally {
 					CurrentJob.this.project = null;
 					CurrentJob.this.thread = null;
 					CurrentJob.this.start = null;
 				}
 			}
 		};
 		this.thread = new Thread(runnable, project.getName() + " revision " + revision);
 		thread.start();
 	}
 
 	private void executeBuildFor(String revision, List<String> command, String resultUri, String clientId) {
 		ProjectRunResult result = null;
 		boolean success = false;
 		try {
 			result = project.run(point.getBaseDir(), revision, command);
 			success = result.getResult() == 0;
 		} catch (IOException e) {
 			StringWriter writer = new StringWriter();
 			e.printStackTrace(new PrintWriter(writer, true));
 			result = new ProjectRunResult(writer.toString(), -1);
 			success = false;
 		} finally {
 			if (project != null) {
 				logger.debug("Job " + project.getName() + " has finished");
				Http http = new Http();
 				logger.debug("Acessing uri " + resultUri + " to finish the job");
 				Method post = http.post(resultUri);
 				try {
 					if (result != null) {
 						post.with("result", result.getContent());
 					} else {
 						post.with("result", "unable-to-read-result");
 					}
 					post.with("project.name", project.getName());
 					post.with("revision", revision);
 					post.with("success", "" + success).with("client.id", clientId).send();
 					if (post.getResult() != 200) {
 						logger.error(post.getContent());
 						throw new RuntimeException("The server returned a problematic answer: " + post.getResult());
 					}
 				} catch (Exception e) {
 					logger.error(
 							"Was unable to notify the server of this request... maybe the server think im still busy.",
 							e);
 				}
 			}
 		}
 	}
 
 	public synchronized void stop() {
 		logger.debug("Stopping job " + project.getName());
 		if (this.thread != null) {
 			this.thread.interrupt();
 		}
 		if (this.project != null) {
 			this.project.stop();
 		}
 	}
 	
 	public double getTime() {
 		return (System.currentTimeMillis() - start.getTimeInMillis())/1000.0;
 	}
 	
 	public Calendar getStart() {
 		return start;
 	}
 
 }
