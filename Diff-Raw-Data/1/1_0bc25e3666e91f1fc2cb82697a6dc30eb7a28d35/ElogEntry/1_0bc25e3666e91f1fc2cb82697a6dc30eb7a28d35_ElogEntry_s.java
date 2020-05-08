 /*-
  * Copyright © 2009 Diamond Light Source Ltd.
  *
  * This file is part of GDA.
  *
  * GDA is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  *
  * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along
  * with GDA. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package gda.util;
 
 import gda.configuration.properties.LocalProperties;
 import gda.data.metadata.GDAMetadataProvider;
 import gda.device.DeviceException;
 
 import java.io.File;
import org.apache.http.HttpEntity;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpStatus;
 import org.apache.commons.httpclient.methods.PostMethod;
 import org.apache.commons.httpclient.methods.multipart.FilePart;
 import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
 import org.apache.commons.httpclient.methods.multipart.Part;
 import org.apache.commons.httpclient.methods.multipart.StringPart;
 import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Used to send ELog entries consisting of text and images to an ELog server.
  */
 public class ElogEntry {
 	
 	static final String defaultURL = "http://rdb.pri.diamond.ac.uk/devl/php/elog/cs_logentryext_bl.php";
 	
 	/**
 	 * logger that can be used for logging messages to go into eLog - needs to be selected in the logger config
 	 */
 	public static final Logger elogger = LoggerFactory.getLogger(ElogEntry.class);
 	
 	/**
 	 * Creates an ELog entry. Default ELog server is "http://rdb.pri.diamond.ac.uk/devl/php/elog/cs_logentryext_bl.php"
 	 * which is the development database. "http://rdb.pri.diamond.ac.uk/php/elog/cs_logentryext_bl.php" is the
 	 * production database. The java.properties file contains the property "gda.elog.targeturl" which can be set to be
 	 * either the development or production databases.
 	 * 
 	 * @param title
 	 *            The ELog title
 	 * @param content
 	 *            The ELog content
 	 * @param userID
 	 *            The user ID e.g. epics or gda or abc12345
 	 * @param visit
 	 *            The visit number
 	 * @param logID
 	 *            The type of log book, The log book ID: Beam Lines: - BLB16, BLB23, BLI02, BLI03, BLI04, BLI06, BLI11,
 	 *            BLI16, BLI18, BLI19, BLI22, BLI24, BLI15, DAG = Data Acquisition, EHC = Experimental Hall
 	 *            Coordinators, OM = Optics and Meteorology, OPR = Operations, E
 	 * @param groupID
 	 *            The group sending the ELog, DA = Data Acquisition, EHC = Experimental Hall Coordinators, OM = Optics
 	 *            and Meteorology, OPR = Operations CS = Control Systems, GroupID Can also be a beam line,
 	 * @param fileLocations
 	 *            The image file names with path to upload
 	 * @throws ELogEntryException
 	 */
 	public static void post(String title, String content, String userID, String visit, String logID, String groupID,
 			String[] fileLocations) throws ELogEntryException {
 		String targetURL = defaultURL;
 		try {
 
 			File targetFile = null;
 			Part[] parts = new Part[6];
 
 			String entryType = "41";// entry type is always a log (41)
 
 			String titleForPost = visit == null ? title : "Visit: " + visit + " - " + title;
 			
 			parts[0] = new StringPart("txtTITLE", titleForPost);
 			parts[1] = new StringPart("txtCONTENT", content);
 			parts[2] = new StringPart("txtLOGBOOKID", logID);
 			parts[3] = new StringPart("txtGROUPID", groupID);
 			parts[4] = new StringPart("txtENTRYTYPEID", entryType);
 			parts[5] = new StringPart("txtUSERID", userID);
 
 			Part[] tempParts;
 			
 			
 			if(fileLocations != null){
 				FilePart filePart = null;
 
 				for (int i = 1; i < fileLocations.length + 1; i++) {
 					targetFile = new File(fileLocations[i - 1]);
 					filePart = new FilePart(targetFile.getName(), targetFile);
 					filePart.setContentType("image/png");
 					filePart.setName("userfile" + i);
 
 					tempParts = new Part[parts.length + 1];
 					System.arraycopy(parts, 0, tempParts, 0, parts.length);
 					tempParts[tempParts.length - 1] = filePart;
 					parts = tempParts;
 				}
 			}
 			
 			PostMethod filePost = null;
 
 			targetURL  = LocalProperties.get("gda.elog.targeturl", defaultURL);
 			filePost = new PostMethod(targetURL);
 			filePost.setRequestEntity(new MultipartRequestEntity(parts, filePost.getParams()));
 
 			HttpClient client = new HttpClient();
 
 			HttpConnectionManagerParams params = client.getHttpConnectionManager().getParams();
 			params.setConnectionTimeout(5000);
 
 			int status = client.executeMethod(filePost);
 
 			String responseBody = filePost.getResponseBodyAsString();
 			if (!responseBody.contains("New Log Entry ID")) {
 				throw new ELogEntryException("Upload failed, status=" + HttpStatus.getStatusText(status)
 						+ " response="+filePost.getResponseBodyAsString()
 						+ " targetURL = " + targetURL
 						+ " titleForPost = " + titleForPost
 						+ " logID = " + logID
 						+ " groupID = " + groupID
 						+ " entryType = " + entryType
 						+ " userID = " + userID);
 			}
 			filePost.releaseConnection();
 		} catch (ELogEntryException e) {
 			throw e;
 		} catch (Exception e) {
 			throw new ELogEntryException("Error in ELogger.  Database:" + targetURL, e);
 		}
 	}
 	
 	/**
 	 * Async version of post @see ElogEntry.post
 	 * 
 	 * @param title
 	 * @param content
 	 * @param userID
 	 * @param visit
 	 * @param logID
 	 * @param groupID
 	 * @param fileLocations
 	 */
 	public static void postAsyn(final String title, final String content, final String userID, final String visit,
 			final String logID, final String groupID, final String[] fileLocations) {
 
 		Thread t = uk.ac.gda.util.ThreadManager.getThread(new Runnable() {
 			@Override
 			public void run() {
 				try {
 					ElogEntry.post(title, content, userID, visit, logID, groupID, fileLocations);
 				} catch (Exception e) {
 					Logger logger = LoggerFactory.getLogger(ElogEntry.class);
 					logger.error(e.getMessage(), e);
 				}
 			}
 		}, "ElogEntry: "+title);
 
 		t.start();
 	}
 	
 	/**
 	 * Create a logger event of the class gda.util.ElogEntry which can be used
 	 * as a filter in logback configuration
 	 * The resultant message is of the form visit + "%%" + title + "%%" + content
 	 * which is understood by the associated ELogAppender class.
 	 * @param title
 	 * @param content
 	 */ 
 	public static void postViaLogger(String title, String content) {
 		String visit;
 		try {
 			visit = GDAMetadataProvider.getInstance().getMetadataValue("visit");
 		} catch (DeviceException e) {
 			visit = "unknown";
 		}
 		elogger.info(visit + "%%" + title + "%%" + content);
 	}
 }
