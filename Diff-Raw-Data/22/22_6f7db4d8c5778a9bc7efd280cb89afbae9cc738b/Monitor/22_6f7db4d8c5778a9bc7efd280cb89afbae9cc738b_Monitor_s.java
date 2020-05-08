 /* Copyright (c) <2010>, <Radiological Society of North America>
  * All rights reserved.
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
  * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
  * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
  * Neither the name of the <RSNA> nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
  * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
  * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
  * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
  * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY
  * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
  * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
  * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
  * OF SUCH DAMAGE.
  */
 package org.rsna.isn.prepcontent;
 
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.log4j.LogManager;
 import org.apache.log4j.Logger;
 import org.rsna.isn.dao.JobDao;
 import org.rsna.isn.domain.Exam;
 import org.rsna.isn.domain.Job;
 
 /**
  * This class monitors the RSNA database for new jobs. If it finds a new job, it
  * will spawn a worker thread to process the job. Currently a max of five
  * concurrent worker threads are allowed.
  *
  * @author Wyatt Tellis
  * @version 3.1.0
  * @since 1.0.0
  */
 class Monitor extends Thread
 {
 	private static final Logger logger = Logger.getLogger(Monitor.class);
 
 	private static final String IMAGES_AVAILABLE[] =
 	{
		"COMPLETED", "DICTATED", "PRELIMINARY", "FINALIZED", "REVISED",
		"ADDENDED", "NON-REPORTABLE"
 	};
 
 	private final ThreadGroup group = new ThreadGroup("workers");
 
 	private boolean keepRunning;
 
 	Monitor()
 	{
 		super("monitor");
 	}
 
 	@Override
 	@SuppressWarnings("SleepWhileInLoop")
 	public void run()
 	{
 		logger.info("Started monitor thread");
 
 		JobDao dao = new JobDao();
 
 		try
 		{
 			Set<Job> interruptedJobs = new HashSet<Job>();
 			interruptedJobs.addAll(dao.getJobsByStatus(Job.RSNA_STARTED_DICOM_C_MOVE));
 
 			for (Job job : interruptedJobs)
 			{
 				dao.updateStatus(job, Job.RSNA_WAITING_FOR_PREPARE_CONTENT, "Retrying job");
 
 				logger.info("Retrying " + job);
 			}
 		}
 		catch (Exception ex)
 		{
 			logger.fatal("Uncaught exception while restarting interrupted jobs.", ex);
 
 			return;
 		}
 
 		keepRunning = true;
 		while (keepRunning)
 		{
 			try
 			{
 				Set<Job> jobsToProcess = new HashSet<Job>();
 
 
 				//
 				// Evaluate newly created jobs
 				//
 				logger.debug("Retrieving list of new jobs...");
 				Set<Job> newJobs = dao.getJobsByStatus(Job.RSNA_WAITING_FOR_PREPARE_CONTENT);
 				for (Job job : newJobs)
 				{
 					Exam exam = job.getExam();
 					if (exam == null)
 					{
 						// This is pretty serious and suggests there's a
 						// database problem
 						dao.updateStatus(job,
 								Job.RSNA_FAILED_TO_PREPARE_CONTENT, "Unable to load exam data");
 
 						logger.warn("Unable to load exam data for " + job);
 
 						continue;
 					}
 
 
 					if (!isExamReadyForSend(exam, job))
 					{
 						if ("CANCELED".equals(exam.getStatus()))
 						{
 							dao.updateStatus(job, Job.RSNA_FAILED_TO_PREPARE_CONTENT,
 									"Exam has been canceled");
 
 							logger.warn("Exam has been canceled for " + job);
 						}
 						else if (job.isSendOnComplete())
 						{
 							dao.updateStatus(job, Job.RSNA_WAITING_FOR_EXAM_COMPLETION);
 
 							logger.debug("Waiting for exam completion for " + job);
 						}
 						else
 						{
 							dao.updateStatus(job, Job.RSNA_WAITING_FOR_EXAM_FINALIZATION);
 
 							logger.debug("Report is pending finalization for " + job);
 						}
 
 						continue;
 					}
 
 					if (isDelayNeeded(exam, job))
 					{
 						dao.updateStatus(job, Job.RSNA_WAITING_FOR_DELAY_EXPIRATION);
 
 						continue;
 					}
 
 					jobsToProcess.add(job);
 				}
 
 
 
 				//
 				// Evaluate jobs that are waiting for exam completion
 				//
 				logger.debug("Retrieving list of jobs with exams awaiting completion.");
 				Set<Job> jobsAwaitingCompletion = dao.getJobsByStatus(Job.RSNA_WAITING_FOR_EXAM_COMPLETION);
 				for (Job job : jobsAwaitingCompletion)
 				{
 					Exam exam = job.getExam();
 					if (exam == null)
 					{
 						// This is pretty serious and suggests there's a
 						// database problem
 						dao.updateStatus(job,
 								Job.RSNA_FAILED_TO_PREPARE_CONTENT, "Unable to load exam data");
 
 						logger.warn("Unable to load exam data for " + job);
 
 						continue;
 					}
 
 					if (!isExamReadyForSend(exam, job))
 					{
 						if ("CANCELED".equals(exam.getStatus()))
 						{
 							dao.updateStatus(job, Job.RSNA_FAILED_TO_PREPARE_CONTENT,
 									"Exam has been canceled");
 
 							logger.warn("Exam has been canceled for " + job);
 						}
 
 
 						continue;
 					}
 					
 					jobsToProcess.add(job);
 				}
 
 
 
 
 
 
 				//
 				// Evaluate jobs that are waiting for a final report
 				//
 				logger.debug("Retrieving list of jobs with exams awaiting reports.");
 				Set<Job> jobsWaitingForReport = dao.getJobsByStatus(Job.RSNA_WAITING_FOR_EXAM_FINALIZATION);
 				for (Job job : jobsWaitingForReport)
 				{
 					Exam exam = job.getExam();
 					if (exam == null)
 					{
 						// This is pretty serious and suggests there's a
 						// database problem
 						dao.updateStatus(job,
 								Job.RSNA_FAILED_TO_PREPARE_CONTENT, "Unable to load exam data");
 
 						logger.warn("Unable to load exam data for " + job);
 
 						continue;
 					}
 
 
 
 					if (!isExamReadyForSend(exam, job))
 					{
 						if ("CANCELED".equals(exam.getStatus()))
 						{
 							dao.updateStatus(job, Job.RSNA_FAILED_TO_PREPARE_CONTENT,
 									"Exam has been canceled");
 
 							logger.warn("Exam has been canceled for " + job);
 						}
 
 
 						continue;
 					}
 
 					if (isDelayNeeded(exam, job))
 					{
 						dao.updateStatus(job, Job.RSNA_WAITING_FOR_DELAY_EXPIRATION);
 
 						continue;
 					}
 
 					jobsToProcess.add(job);
 				}
 
 
 
 
 
 				//
 				// Evaluate jobs that are waiting for transmit delay to
 				// expire
 				//
 				logger.debug("Retrieving list of jobs waiting for delay expiration...");
 				Set<Job> jobsWaitingForTransmitDelay = dao.getJobsByStatus(Job.RSNA_WAITING_FOR_DELAY_EXPIRATION);
 				for (Job job : jobsWaitingForTransmitDelay)
 				{
 					Exam exam = job.getExam();
 					if (exam == null)
 					{
 						// This is pretty serious and suggests there's a
 						// database problem
 						dao.updateStatus(job,
 								Job.RSNA_FAILED_TO_PREPARE_CONTENT, "Unable to load exam data");
 
 						logger.warn("Unable to load exam data for " + job);
 
 						continue;
 					}
 
 					if (isDelayNeeded(exam, job))
 						continue;
 
 
 					jobsToProcess.add(job);
 				}
 
 
 
 				for (Job job : jobsToProcess)
 				{
 					if (group.activeCount() >= 5)
 					{
 						logger.warn("Too many jobs active.  Pausing monitor thread");
 
 						Thread.sleep(10 * 1000);
 
 						break;
 					}
 
 
 					Exam exam = job.getExam();
 					String mrn = exam.getMrn();
 					String accNum = exam.getAccNum();
 
 					//
 					// Make sure we're not already processing this MRN/acc #
 					// combo.  Some PACS don't like multiple C-MOVE requests
 					// for the same exam. 
 					//
 					List<Job> otherJobs = dao.findJobs(mrn, accNum, Job.RSNA_STARTED_DICOM_C_MOVE);
 					if (!otherJobs.isEmpty())
 					{
 						continue;
 					}
 
 					String name = "worker-" + job.getJobId();
 
 
 					Thread active[] = new Thread[group.activeCount()];
 					int count = group.enumerate(active);
 
 					boolean alreadyRunning = false;
 					for (int i = 0; i < count && i < active.length; i++)
 					{
 						Thread t = active[i];
 						if (t == null)
 							continue;
 
 						if (name.equals(t.getName()))
 						{
 							logger.warn("Unable to start thread for " + job
 									+ ". An existing thread is already active.");
 
 							alreadyRunning = true;
 							break;
 						}
 					}
 
 
 					if (!alreadyRunning)
 					{
 						dao.updateStatus(job, Job.RSNA_STARTED_DICOM_C_MOVE);
 
 						Worker worker = new Worker(group, job, name);
 						worker.start();
 					}
 				}
 
 				sleep(1000);
 			}
 			catch (InterruptedException ex)
 			{
 				logger.fatal("Monitor thread interrupted", ex);
 
 				LogManager.shutdown();
 
 				System.exit(1);
 			}
 			catch (Throwable ex)
 			{
 				logger.fatal("Uncaught exception while processing jobs. "
 						+ "Monitor thread shutdown", ex);
 
 				LogManager.shutdown();
 
 				System.exit(1);
 			}
 		}
 
 
 
 		logger.info("Stopped monitor thread");
 	}
 
 	public void stopRunning() throws InterruptedException
 	{
 		keepRunning = false;
 
 		join(10 * 1000);
 	}
 
 	private boolean isExamReadyForSend(Exam exam, Job job)
 	{
 		String status = exam.getStatus();
 		boolean noReport = job.isSendOnComplete();
 
 
		if ("FINALIZED".equals(status))
 			return true;
		else if ("NON-REPORTABLE".equals(status))
 			return true;
 		else if (noReport && ArrayUtils.contains(IMAGES_AVAILABLE, status))
 			return true;
 		else
 			return false;
 	}
 
 	private boolean isDelayNeeded(Exam exam, Job job)
 	{
 		if (job.isSendOnComplete())
 			return false;
 
 		long age = System.currentTimeMillis()
 				- exam.getStatusTimestamp().getTime();
 
 		if (age < 0)
 			age = 0;
 
 		long delay = job.getDelay() * 3600000L;
 
 		if (delay < 0)
 			delay = 0;
 
 		return age < delay;
 	}
 
 }
