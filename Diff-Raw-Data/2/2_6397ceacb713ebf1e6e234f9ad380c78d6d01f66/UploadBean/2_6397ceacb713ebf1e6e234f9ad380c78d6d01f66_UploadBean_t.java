 /**********************************************************************************
  * $URL$
  * $Id$
  ***********************************************************************************
  *
  * Copyright (c) 2007, 2008 The Sakai Foundation.
  *
  * Licensed under the Educational Community License, Version 1.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.opensource.org/licenses/ecl1.php
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  **********************************************************************************/
 
 package org.sakaiproject.assignment2.tool.beans;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.sakaiproject.assignment2.exception.UploadException;
 import org.sakaiproject.assignment2.logic.ExternalLogic;
 import org.sakaiproject.assignment2.logic.UploadGradesLogic;
 import org.sakaiproject.assignment2.model.UploadAllOptions;
 import org.sakaiproject.assignment2.tool.producers.ViewSubmissionsProducer;
 import org.springframework.web.multipart.MultipartFile;
 
 import uk.org.ponder.messageutil.TargettedMessage;
 import uk.org.ponder.messageutil.TargettedMessageList;
 
 public class UploadBean
 {
 	private UploadAllOptions uploadOptions;
 	private Map<String, MultipartFile> uploads;
 	
 	private static final String FAILURE = "failure";
 
 	private TargettedMessageList messages;
 	public void setTargettedMessageList(TargettedMessageList messages)
 	{
 		this.messages = messages;
 	}
 
 	private UploadGradesLogic uploadGradesLogic;
 	public void setUploadGradesLogic(UploadGradesLogic uploadGradesLogic)
 	{
 		this.uploadGradesLogic = uploadGradesLogic;
 	}
 
 	public void setMultipartMap(Map<String, MultipartFile> uploads)
 	{
 		this.uploads = uploads;
 	}
 
 	public UploadAllOptions getUploadOptions()
 	{
 		if (uploadOptions == null)
 			uploadOptions = new UploadAllOptions();
 		return uploadOptions;
 	}
 	
 	private ExternalLogic externalLogic;
 	public void setExternalLogic(ExternalLogic externalLogic) {
 		this.externalLogic = externalLogic;
 	}
 
 	public String processUploadGradesCSV()
 	{
 		if (uploadOptions == null || uploadOptions.assignmentId == null ) {
 			messages.addMessage(new TargettedMessage("No assignmentId was passed " +
 					"in the request to processUploadGradesCSV. Cannot continue.", new Object[] {},
 					TargettedMessage.SEVERITY_ERROR));
             return FAILURE;
         }
 
 		if (uploads.isEmpty()) 
 		{
 			messages.addMessage(new TargettedMessage("assignment2.upload_grades.missing_file", new Object[] {},
 					TargettedMessage.SEVERITY_ERROR));
 			return FAILURE;
 		}
 	
 		MultipartFile uploadedFile = uploads.get("file");
 		
 		if (uploadedFile.getSize() == 0)
 		{
 			messages.addMessage(new TargettedMessage("assignment2.upload_grades.missing_file", new Object[] {},
 					TargettedMessage.SEVERITY_ERROR));
 			return FAILURE;
 		}
 		
		if (!uploadedFile.getOriginalFilename().endsWith("csv"))
 		{
 			messages.addMessage(new TargettedMessage("assignment2.upload_grades.not_csv", new Object[] {},
 					TargettedMessage.SEVERITY_ERROR));
 			return FAILURE;
 		}
 		
 		// now let's parse the content of the file so we can do some validation
 		// on the data before we attempt to save it. 
 		String contextId = externalLogic.getCurrentContextId();
 		
 		File newFile = null;
 		try {
 			newFile = File.createTempFile(uploadedFile.getName(), ".csv");
 			uploadedFile.transferTo(newFile);
 		} catch (IOException ioe) {
 			throw new UploadException(ioe.getMessage(), ioe);
 		}
 		
 		// retrieve the displayIdUserId info once and re-use it
 		Map<String, String> displayIdUserIdMap = externalLogic.getUserDisplayIdUserIdMapForStudentsInSite(contextId);		
 		List<List<String>> parsedContent = uploadGradesLogic.getCSVContent(newFile);
 		
 		// let's check that the students included in the file are actually in the site
 		List<String> invalidDisplayIds = uploadGradesLogic.getInvalidDisplayIdsInContent(displayIdUserIdMap, parsedContent);
 		if (invalidDisplayIds != null && !invalidDisplayIds.isEmpty()) {
 			messages.addMessage(new TargettedMessage("assignment2.upload_grades.user_not_in_site", 
 					new Object[] {getListAsString(invalidDisplayIds)}, TargettedMessage.SEVERITY_ERROR ));
 			return FAILURE;
 		}
 		
 		// check that the grades are valid
 		List<String> displayIdsWithInvalidGrade = uploadGradesLogic.getStudentsWithInvalidGradesInContent(parsedContent, contextId);
 		if (displayIdsWithInvalidGrade != null && !displayIdsWithInvalidGrade.isEmpty()) {
 			messages.addMessage(new TargettedMessage("assignment2.upload_grades.grades_not_valid", 
 					new Object[] {getListAsString(displayIdsWithInvalidGrade)}, TargettedMessage.SEVERITY_ERROR ));
 			return FAILURE;
 		}
 		
 		// let's upload the grades now
 		List<String> usersNotUpdated = uploadGradesLogic.uploadGrades(displayIdUserIdMap, uploadOptions.assignmentId, parsedContent);
 		
 		if (!usersNotUpdated.isEmpty()) {
 			messages.addMessage(new TargettedMessage("assignment2.upload_grades.upload_successful_with_exception",
 					new Object[] {getListAsString(usersNotUpdated)}, TargettedMessage.SEVERITY_INFO));
 		} else {
 			messages.addMessage(new TargettedMessage("assignment2.upload_grades.upload_successful",
 					new Object[] {}, TargettedMessage.SEVERITY_INFO));
 		}
 		
 		// let's proceed with the grade upload
 		return ViewSubmissionsProducer.VIEW_ID;
 	}
 
 	private String getListAsString(List<String> itemList) {
 		StringBuilder sb = new StringBuilder();
 		if (itemList != null) {
 			for (int i = 0; i < itemList.size(); i++) {
 				if (i != 0) {
 					sb.append(", ");
 				}
 				
 				sb.append(itemList.get(i));
 			}
 		}
 		
 		return sb.toString();
 	}
 	
 	/*
 	 * **** This is the original code for a full upload. For now, we are only updating grades via the csv
 	 public String processUpload()
 	{
 		MultipartFile upFile = null;
 		boolean isZip = false;
 		if (uploads.isEmpty())
 			messages.addMessage(new TargettedMessage("assignment2.uploadall.alert.zipFile"));
 		else
 		{
 			upFile = uploads.get("file");
 			if (upFile.getSize() == 0)
 			{
 				messages.addMessage(new TargettedMessage("assignment2.uploadall.alert.zipFile"));
 			}
 			else if ("application/zip".equals(upFile.getContentType()))
 			{
 				isZip = true;
 			}
 		}
 
 		// check that at least 1 option has been selected
 		if ((uploadOptions == null
 				|| (!uploadOptions.feedbackText && !uploadOptions.gradeFile
 						&& !uploadOptions.feedbackAttachments)) && isZip)
 		{
 			messages.addMessage(new TargettedMessage("assignment2.uploadall.alert.choose.element"));
 		}
 		else
 		{
 			try
 			{
 				File f = null;
 				if (isZip)
 				{
 					f = File.createTempFile(upFile.getName(), ".zip");
 					upFile.transferTo(f);
 					updownLogic.uploadAll(uploadOptions, f);
 				}
 				else
 				{
 					f = File.createTempFile(upFile.getName(), ".csv");
 					upFile.transferTo(f);
 					updownLogic.uploadCSV(uploadOptions, f);
 				}
 			}
 			catch (IOException ioe)
 			{
 				messages.addMessage(new TargettedMessage("assignment2.uploadall.exception",
 						new Object[] { ioe.getMessage() }));
 			}
 			catch (UploadException ue)
 			{
 				messages.addMessage(new TargettedMessage("assignment2.uploadall.exception",
 						new Object[] { ue.getMessage() }));
 			}
 		}
 
 		return ViewSubmissionsProducer.VIEW_ID;
 	}*/
 }
