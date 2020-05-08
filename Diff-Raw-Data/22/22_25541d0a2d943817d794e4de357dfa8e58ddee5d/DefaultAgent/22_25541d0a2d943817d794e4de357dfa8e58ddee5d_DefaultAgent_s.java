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
 package br.com.caelum.integracao.server.agent;
 
 import java.io.IOException;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import br.com.caelum.integracao.http.DefaultHttp;
 import br.com.caelum.integracao.http.Method;
 import br.com.caelum.integracao.server.ExecuteCommandLine;
 import br.com.caelum.integracao.server.Project;
 import br.com.caelum.integracao.server.queue.Job;
 
 /**
  * Default implementation of an agent.
  * 
  * @author guilherme silveira
  */
 public class DefaultAgent implements Agent {
 
 	private static final int CONFLICT = 409;
 
 	private static final int GONE = 410;
 
 	private final Logger logger = LoggerFactory.getLogger(DefaultAgent.class);
 
 	private final String baseUri;
 
 	public DefaultAgent(String baseUri) {
 		this.baseUri = baseUri;
 	}
 
 	public AgentStatus getStatus() {
 		Method post = new DefaultHttp().post(baseUri + "/job/current");
 		try {
 			try {
 				post.send();
 			} catch (IOException e) {
 				logger.debug("Setting the agent as unavailable.", e);
 				return AgentStatus.UNAVAILABLE;
 			}
 			if (post.getResult() == GONE) {
 				return AgentStatus.FREE;
 			}
 			if (post.getResult() != 200) {
 				return AgentStatus.UNAVAILABLE;
 			}
 			return AgentStatus.BUSY;
 		} finally {
 			post.close();
 		}
 	}
 
 	public boolean stop(Job currentJob) {
 		Method post = new DefaultHttp().post(baseUri + "/job/stop");
 		post.with("jobId", "" + currentJob.getId());
 		try {
 			try {
 				post.send();
 			} catch (IOException e) {
 				logger.debug("Could not stop the job.", currentJob.getId());
 				return false;
 			}
 			if (post.getResult() != 200) {
 				try {
 					logger.debug("Could not stop the job: " + post.getResult() + " with: " + post.getContent());
 				} catch (IOException e) {
 					logger.debug("Could not stop the job: " + post.getResult(), e);
 				}
 				return false;
 			}
 			return true;
 		} finally {
 			post.close();
 		}
 	}
 
 	public boolean register(Project project) {
 		Method post = new DefaultHttp().post(baseUri + "/project/register");
 		post.with("project.name", project.getName());
 		post.with("project.uri", project.getUri());
 		post.with("project.scmType", project.getControlType().getName());
 		try {
 			post.send();
 			int result = post.getResult();
 			if (result != 200) {
 				logger
 						.error("Unable to register " + project.getName() + "@" + baseUri + " due to "
 								+ post.getContent());
 			}
 			return result == 200;
 		} catch (IOException e) {
 			logger.error("Unable to register " + project.getName() + "@" + baseUri, e);
 			return false;
 		} finally {
 			post.close();
 		}
 	}
 
 	public boolean execute(ExecuteCommandLine command, Job job, String mySelf) {
 		Method post = new DefaultHttp().post(baseUri + "/job/execute");
 		post.with("jobId", "" + job.getId());
 		post.with("revision", job.getBuild().getRevision().getName());
 		post.with("project.name", job.getBuild().getProject().getName());
 		post.with("resultUri", "http://" + mySelf + "/integracao/finish/job/" + job.getId());
 		for (int i = 0; i < command.getStartCommands().size(); i++) {
 			post.with("startCommand[" + i + "]", command.getStartCommands().get(i).getValue());
 		}
 		for (int i = 0; i < command.getStopCommands().size(); i++) {
 			post.with("stopCommand[" + i + "]", command.getStopCommands().get(i).getValue());
 		}
 		int k = 0;
 		for(String directory : command.getPhase().getDirectoriesToCopy()) {
 			post.with("directoryToCopy[" + (k++) + "]",directory);
 		}
 		try {
 			post.send();
 			int result = post.getResult();
 			if (result != 200) {
 				logger
 						.error("Unable to execute command.id=" + command.getId() + "@" + baseUri + " due to "
 								+ post.getContent());
 			}
 			return result == 200;
 		} catch (IOException e) {
 			logger.error("Unable to execute command.id=" + command.getId() + "@" + baseUri, e);
 			return false;
 		} finally {
 			post.close();
 		}
 	}
 
 
 }
