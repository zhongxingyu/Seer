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
 
 import java.io.IOException;
 import java.util.List;
 
 import javax.annotation.PostConstruct;
 import javax.annotation.PreDestroy;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import br.com.caelum.integracao.http.DefaultHttp;
 import br.com.caelum.integracao.server.Application;
 import br.com.caelum.integracao.server.Client;
 import br.com.caelum.integracao.server.agent.AgentControl;
 import br.com.caelum.integracao.server.agent.AgentStatus;
 import br.com.caelum.integracao.server.agent.Clients;
 import br.com.caelum.integracao.server.agent.DefaultAgent;
 import br.com.caelum.integracao.server.dao.Database;
 import br.com.caelum.integracao.server.dao.DatabaseFactory;
 import br.com.caelum.vraptor.ioc.ApplicationScoped;
 
 @ApplicationScoped
 public class QueueThread {
 
 	private final Logger logger = LoggerFactory.getLogger(QueueThread.class);
 
 	private final DatabaseFactory factory;
 
 	private Thread thread;
 
 	private Object waiter = new Object();
 
 	private boolean shouldRun = true;
 
 	public QueueThread(DatabaseFactory factory) {
 		this.factory = factory;
 	}
 
 	@PostConstruct
 	public void startup() {
 		logger.info("Starting QueueThread");
 		this.thread = new Thread(new Runnable() {
 			public void run() {
 				while (shouldRun) {
 					Database db = new Database(factory);
 					try {
 						stopOldJobs(db);
 					} catch (Exception ex) {
 						logger.error("Something really nasty ocurred while stopping jobs", ex);
 					} finally {
 						if (db.hasTransaction()) {
 							db.rollback();
 						}
						db.close();
 					}
 					try {
 						startJobs(db);
 					} catch (Exception ex) {
 						logger.error("Something really nasty ocurred while executing the job queue", ex);
 					} finally {
 						if (db.hasTransaction()) {
 							db.rollback();
 						}
 						db.close();
 					}
 					if (shouldRun) {
 						try {
 							synchronized (waiter) {
 								waiter.wait(60000);
 							}
 						} catch (InterruptedException e) {
 							logger.debug("Was waiting but someone waked me up.");
 						}
 					}
 				}
 				logger.info("QueueThread is stopping.");
 			}
 
 		});
 		thread.start();
 	}
 
 	private void startJobs(Database db) {
 		db.beginTransaction();
 		JobQueue queue = new DefaultJobQueue(new Jobs(db), new Clients(db), new Application(db).getConfig(), new AgentControl());
 		int result = queue.iterate();
 		db.commit();
 		logger.debug("Job queue started " + result + " jobs");
 	}
 
 	private void stopOldJobs(Database db) {
 		db.beginTransaction();
 		List<Job> jobs = new Jobs(db).runningJobs();
 		int result = 0;
 		AgentControl control  =new AgentControl();
 		for (Job job : jobs) {
 			long timeSpent = System.currentTimeMillis() - job.getStartTime().getTimeInMillis();
 			int minutes = new Application(db).getConfig()
 					.getMaximumTimeForAJob();
 			Client client = job.getClient();
 			AgentStatus status = control.to(client.getBaseUri()).getStatus();
 			Job currentJob = client.getCurrentJob();
 			if (status.equals(AgentStatus.UNAVAILABLE) || status.equals(AgentStatus.FREE)) {
 				job.reschedule();
 				if (currentJob!=null && currentJob.equals(job)) {
 					logger.error("Leaving the job because the server just told me there is nothing running there..."
 							+ "Did the client break or was it sending me the info right now?");
 					client.leaveJob();
 				}
 				continue;
 			}	
 			if (timeSpent > minutes * 60 * 1000) {
 				if(currentJob!=null && currentJob.equals(job)) {
 					if(client.stop(new DefaultAgent(client.getBaseUri(), new DefaultHttp()))) {
 						result++;
 					}
 				} else {
 					try {
 						job.finish("killing job because there was no response and the client is not actually running it", false, db, "", null);
 						result++;
 					} catch (IOException e) {
 						logger.error("Tried to kill job but couldnt.", e);
 					}
 				}
 			}
 		}
 		db.commit();
 		logger.debug("Job queue killed " + result + " old jobs");
 	}
 
 	@PreDestroy
 	public void destroy() {
 		if (thread != null && thread.isAlive()) {
 			logger.debug("Shutting down queue thread");
 			shouldRun = false;
 			thread.interrupt();
 		}
 	}
 
 	public void wakeup() {
 		synchronized (waiter) {
 			waiter.notify();
 		}
 	}
 
 }
