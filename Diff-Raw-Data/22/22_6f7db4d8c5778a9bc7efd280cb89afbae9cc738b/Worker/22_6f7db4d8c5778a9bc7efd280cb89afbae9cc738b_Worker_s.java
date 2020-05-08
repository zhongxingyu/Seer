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
 
 import java.io.File;
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.math.NumberUtils;
 import org.apache.log4j.Logger;
 import org.dcm4che2.net.CommandUtils;
 import org.rsna.isn.dao.ConfigurationDao;
 import org.rsna.isn.dao.JobDao;
 import org.rsna.isn.domain.Device;
 import org.rsna.isn.domain.Exam;
 import org.rsna.isn.domain.Job;
 import org.rsna.isn.prepcontent.dcm.CFind;
 import org.rsna.isn.prepcontent.dcm.CFindResponse;
 import org.rsna.isn.prepcontent.dcm.CMove;
 import org.rsna.isn.prepcontent.dcm.CMoveResponse;
 import org.rsna.isn.util.Environment;
 import org.rsna.isn.util.FileUtil;
 
 /**
  * Worker thread that processes jobs.
  *
  * @author Wyatt Tellis
  * @since 1.0.0
  * @version 3.1.0
  */
 class Worker extends Thread
 {
 	private static final Logger logger = Logger.getLogger(Worker.class);
 
 	private final Job job;
 
 	private final JobDao dao = new JobDao();
 
 	Worker(ThreadGroup group, Job job, String name)
 	{
 		super(group, name);
 
 		this.job = job;
 	}
 
 	@Override
 	public void run()
 	{
 		logger.info("Started processing " + job);
 
 		try
 		{
 			Exam exam = job.getExam();
 
 			String mrn = exam.getMrn();
 			if (StringUtils.isEmpty(mrn))
 			{
 				dao.updateStatus(job, Job.RSNA_FAILED_TO_PREPARE_CONTENT, "No MRN");
 
 				return;
 			}
 
 			String accNum = exam.getAccNum();
 			if (StringUtils.isEmpty(accNum))
 			{
 				dao.updateStatus(job, Job.RSNA_FAILED_TO_PREPARE_CONTENT, "No accession number");
 
 				return;
 			}
 
 
 
 			try
 			{
 				List<CFindResponse> findRsps = CFind.findStudies(job);
 				if (findRsps.isEmpty())
 				{
 					dao.updateStatus(job, Job.RSNA_UNABLE_TO_FIND_IMAGES,
 							"Could not find any images for this exam.");
 
 					logger.error("Could not find any images associated with " + job);
 
 					return;
 				}
 
 				// Remove any duplicate studies
 				// Need this for complex setups like the Mayo
 				Map<String, CFindResponse> studies = 
 						new HashMap<String, CFindResponse>();
 				for (CFindResponse response : findRsps)
 				{
 					String studyUid = response.getStudyUid();
 					if (studies.containsKey(studyUid))
 					{
 						CFindResponse temp = studies.get(studyUid);
 						String tempAe = temp.getDevice().getAeTitle();
 						String responseAe = response.getDevice().getAeTitle();
 
 						if (response.getCount() > temp.getCount())
 						{
 							logger.warn("Study uid " + studyUid + " was found on both "
 									+ tempAe + " and "
 									+ responseAe + ". Using instance from " + responseAe);
 
 							studies.put(studyUid, response);
 						}
 						else
 						{
 							logger.warn("Study uid " + studyUid + " was found on both "
 									+ tempAe + " and "
 									+ responseAe + ". Using instance from " + tempAe);
 						}
 					}
 					else
 					{
 						studies.put(studyUid, response);
 					}
 				}
 
 
 
 
 				File dcmDir = Environment.getDcmDir();
 				File jobDir = FileUtil.newFile(dcmDir, job.getJobId());
 
 				for (CFindResponse findRsp : studies.values())
 				{
 					Device device = findRsp.getDevice();
 					String ae = device.getAeTitle();
 
 
 					int expectedCount = findRsp.getCount();
 					String studyUid = findRsp.getStudyUid();
 
 					File patDir = FileUtil.newFile(jobDir, mrn);
 					File examDir = FileUtil.newFile(patDir, (accNum));
 					File studyDir = FileUtil.newFile(examDir, studyUid);
 
 					int dirCount = FileUtil.getFileCount(studyDir);
 
 					if (expectedCount > 0 && dirCount == expectedCount)
 					{
 						// We already got the images, so there's no need to do a 
 						// C-MOVE
 						logger.warn("Skipping C-MOVE of study " + studyUid
 								+ " for " + job + ". Using cached copy "
 								+ "of images from directory " + studyDir);
 
 						continue;
 					}
 					else if (expectedCount > 0 && dirCount > expectedCount)
 					{
 						// We got more images than expected, so let's just
 						// wait to see how many total we get
 
 						logger.warn("Found cached copy of images for "
 								+ studyUid + " for " + job + " in directory "
 								+ studyDir + " with more images that expected. "
 								+ "Expected " + expectedCount + ", found " + dirCount + ".  "
 								+ "Going to skip C-MOVE and wait for all images to arrive.");
 
 						// We don't really know how many images to expect
 						expectedCount = 0;
 					}
 					else
 					{
 						// Do the C-MOVE
 
 						CMoveResponse moveRsp =
 								CMove.retrieveStudy(device, job, studyUid, expectedCount);
 						if (moveRsp == null)
 						{
 							logger.fatal("C-MOVE of study " + studyUid + " from " + ae
 									+ " for job " + job + " failed due to an unknown error");
 
 							dao.updateStatus(job, Job.RSNA_DICOM_C_MOVE_FAILED,
 									"C-MOVE of study " + studyUid + " from "
 									+ ae + " failed due to an unknown error");
 
 							return;
 						}
 						else
 						{
 							int status = moveRsp.getStatus();
 							if (status != CommandUtils.SUCCESS)
 							{
 								String comment = moveRsp.getComments();
 
 								logger.fatal("C-MOVE of study " + studyUid + " from "
 										+ ae + " for job " + job + " failed. Error code "
 										+ "returned by remote PACS is: " + status + ".  "
 										+ "Error comment returned by remote PACS is: " + comment);
 
 								dao.updateStatus(job, Job.RSNA_DICOM_C_MOVE_FAILED,
 										"C-MOVE of study " + studyUid + " from "
 										+ ae + " failed. Error code returned "
 										+ "by remote PACS is: " + status + ".  "
 										+ "Error comment returned by "
 										+ "remote PACS is: \"" + comment + "\".  ");
 
 								return;
 							}
 						}
 
 						// Make sure we actually got the expected number of images
 						dirCount = FileUtil.getFileCount(studyDir);
 						if (expectedCount > 0 && dirCount == expectedCount)
 						{
 							logger.info("Received " + dirCount + " objects "
 									+ "for study " + studyUid + " from " + ae + " "
 									+ "for " + job);
 
 							continue;
 						}
 					}
 
 					// Wait to see if images arrive at a later point in time
 					// Some PACS systems (e.g. Intelerad) do an asynchronous
 					// C-MOVE
 					ConfigurationDao configDao = new ConfigurationDao();
					String config = configDao.getConfiguration("fail_on_incomplete_study");
 					boolean fail = Boolean.parseBoolean(config);
 
 					dirCount = waitForImages(studyDir, expectedCount);
 					if (dirCount == 0)
 					{
 						// Nothing arrived so just mark the job as failed
 						logger.warn("Retrieval of study " + studyUid + " from "
 								+ ae + " for " + job + " failed.  "
 								+ "No images were received.");
 
 						dao.updateStatus(job, Job.RSNA_DICOM_C_MOVE_FAILED,
 								"Retrieval of study " + studyUid + " from "
 								+ ae + " failed.  No images were received.");
 
 						return;
 					}
 					else if (expectedCount > 0 && fail && dirCount < expectedCount)
 					{
 						// We got fewer images than expected. 
 						logger.warn("Retrieval of study " + studyUid + " from "
 								+ ae + " for " + job + " failed.  "
 								+ "Only " + dirCount + " of " + expectedCount
 								+ " images were received.");
 
 						dao.updateStatus(job, Job.RSNA_DICOM_C_MOVE_FAILED,
 								"Retrieval of study " + studyUid + " from "
 								+ ae + " failed. Only " + dirCount
 								+ " of " + expectedCount + " images were received.");
 
 						return;
 					}
 					else
 					{
 						// We're done, go on to the next study (if any)
 						
 						if (expectedCount > 0 && dirCount < expectedCount)
 						{
 							logger.warn("Retrieval of study " + studyUid + " for "
 									+ job + " was partially completed.  A total of " 
 									+ dirCount + " out of " + expectedCount
 									+ " images were received and stored in " + studyDir);
 						}
 						else
 						{							
 							logger.info("Retrieval of study " + studyUid + " for "
 									+ job + " was successful.  A total of " + dirCount
 									+ " images were received and stored in " + studyDir);
 						}
 
 						continue;
 					}
 				}
 
 				dao.updateStatus(job, Job.RSNA_WAITING_FOR_TRANSFER_CONTENT);
 
 				logger.info("Successfully processed " + job);
 			}
 			catch (Exception ex)
 			{
 				logger.error("Uncaught exception while processing job " + job, ex);
 
 				dao.updateStatus(job, Job.RSNA_FAILED_TO_PREPARE_CONTENT, ex);
 			}
 		}
 		catch (SQLException ex)
 		{
 			logger.error("Uncaught exception while updating status of job " + job, ex);
 		}
 		catch (Throwable ex)
 		{
 			logger.fatal("Uncaught exception while processing job " + job, ex);
 		}
 	}
 
 	@SuppressWarnings("SleepWhileInLoop")
 	private int waitForImages(File dir, int expected) throws SQLException, InterruptedException
 	{
 		int current = FileUtil.getFileCount(dir);
 		int prev = current;
 
 
 		ConfigurationDao config = new ConfigurationDao();
		String str = config.getConfiguration("retrieve-timeout-secs");
 
 		long timeout = NumberUtils.toLong(str, 600) * 1000L;
 		long start = System.currentTimeMillis();
 		long now = System.currentTimeMillis();
 		long elapsed = now - start;
 
 
 		while (elapsed < timeout)
 		{
 			current = FileUtil.getFileCount(dir);
 			if (expected > 0 && current >= expected)
 				return current;
 
 			if (current != prev)
 			{
 				// Reset the countdown timer when we get new images
 				prev = current;
 				start = System.currentTimeMillis();
 			}
 			else if (expected > 0 && current == expected)
 			{
 				// We got everything
 
 				return current;
 			}
 			else if (expected > 0 && current > expected)
 			{
 				// We got more images than expected, so we can't
 				// trust the value of expected. 
 				logger.warn("More images were found in " + dir
 						+ " than were expected. Expecting "
 						+ expected + ", found " + current + ". ");
 
 				expected = 0;
 			}
 
 			now = System.currentTimeMillis();
 			elapsed = now - start;
 
 			long remaining = (timeout - elapsed) / 1000L;
 
 			dao.updateComments(job, Job.RSNA_STARTED_DICOM_C_MOVE,
 					"Waiting for images. Timeout expires in " + remaining + " secs.");
 
 			sleep(1000);
 		}
 
 
 		return current;
 	}
 
 }
