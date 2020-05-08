 package org.sakaiproject.assignment2.logic.impl;
 
 import java.io.BufferedInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipOutputStream;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.sakaiproject.assignment2.logic.AssignmentBundleLogic;
 import org.sakaiproject.assignment2.logic.AssignmentLogic;
 import org.sakaiproject.assignment2.logic.AssignmentPermissionLogic;
 import org.sakaiproject.assignment2.logic.AssignmentSubmissionLogic;
 import org.sakaiproject.assignment2.logic.ExternalContentLogic;
 import org.sakaiproject.assignment2.logic.ExternalGradebookLogic;
 import org.sakaiproject.assignment2.logic.ExternalLogic;
 import org.sakaiproject.assignment2.logic.GradeInformation;
 import org.sakaiproject.assignment2.logic.ZipExportLogic;
 import org.sakaiproject.assignment2.model.Assignment2;
 import org.sakaiproject.assignment2.model.AssignmentSubmission;
 import org.sakaiproject.assignment2.model.AssignmentSubmissionVersion;
 import org.sakaiproject.assignment2.model.AttachmentBase;
 import org.sakaiproject.content.api.ContentResource;
 import org.sakaiproject.entity.api.Entity;
 import org.sakaiproject.entity.api.ResourceProperties;
 import org.sakaiproject.exception.PermissionException;
 import org.sakaiproject.exception.ServerOverloadException;
 import org.sakaiproject.user.api.User;
 import org.sakaiproject.util.Validator;
 
 public class ZipExportLogicImpl implements ZipExportLogic
 {
 	private static Log log = LogFactory.getLog(ZipExportLogicImpl.class);
 
 	private AssignmentLogic assignmentLogic;
 
 	public void setAssignmentLogic(AssignmentLogic assignmentLogic)
 	{
 		this.assignmentLogic = assignmentLogic;
 	}
 
 	private AssignmentSubmissionLogic assignmentSubmissionLogic;
 
 	public void setAssignmentSubmissionLogic(
 			AssignmentSubmissionLogic assignmentSubmissionLogic)
 	{
 		this.assignmentSubmissionLogic = assignmentSubmissionLogic;
 	}
 
 	private ExternalGradebookLogic gradebookLogic;
 
 	public void setGradebookLogic(ExternalGradebookLogic gradebookLogic)
 	{
 		this.gradebookLogic = gradebookLogic;
 	}
 
 	private ExternalLogic externalLogic;
 
 	public void setExternalLogic(ExternalLogic externalLogic)
 	{
 		this.externalLogic = externalLogic;
 	}
 
 	private ExternalContentLogic contentLogic;
 	public void setExternalContentLogic(ExternalContentLogic contentLogic)
 	{
 		this.contentLogic = contentLogic;
 	}
 
 	private AssignmentBundleLogic bundle;
 
 	public void setAssignmentBundleLogic(AssignmentBundleLogic bundle)
 	{
 		this.bundle = bundle;
 	}
 	
 	private AssignmentPermissionLogic permissionLogic;
 	public void setAssignmentPermissionLogic(AssignmentPermissionLogic permissionLogic) {
 		this.permissionLogic = permissionLogic;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.sakaiproject.assignment2.tool.handlerhooks.ZipExporterI#getSubmissionsZip(java.io.OutputStream, java.lang.Long)
 	 */
 	public void getSubmissionsZip(OutputStream outputStream, Long assignmentId)
 			throws PermissionException
 	{
 		Assignment2 assignment = assignmentLogic.getAssignmentById(assignmentId);
 		if (log.isDebugEnabled())
 			log.debug(this + ": getSubmissionsZip reference=" + assignmentId);
 
 		StringBuilder exceptionMessage = new StringBuilder();
 		if (gradebookLogic.isCurrentUserAbleToGrade(assignment.getContextId()))
 		{
 			List<AssignmentSubmission> submissions = assignmentSubmissionLogic
 				.getViewableSubmissionsWithHistoryForAssignmentId(assignment.getId());
 			
 			zipSubmissions(assignment, submissions, outputStream, exceptionMessage);
 
 			if (exceptionMessage.length() > 0)
 			{
 				// log any error messages
 				if (log.isDebugEnabled())
 					log.debug(this + assignmentId.toString()
 							+ exceptionMessage.toString());
 			}
 		}
 	} // getSubmissionsZip
 
 	protected void zipSubmissions(Assignment2 assignment,
 			List<AssignmentSubmission> submissionsWithHistory, OutputStream outputStream,
 			StringBuilder exceptionMessage)
 	{
 		String assignmentTitle = assignment.getTitle();
 		String contextId = externalLogic.getCurrentContextId();
 		String currUserId = externalLogic.getCurrentUserId();
 		
 		List<String> viewableStudents = permissionLogic.getViewableStudentsForUserForItem(currUserId, assignment);
 		Map<String, User> userIdUserMap = externalLogic.getUserIdUserMap(viewableStudents);
 
 		String formatWithTime = bundle.getString("assignment2.assignment_grade_assignment.downloadall.filename_date_format_with_time");
 		String formatNoTime = bundle.getString("assignment2.assignment_grade_assignment.downloadall.filename_date_format");
 		DateFormat df_withTime = new SimpleDateFormat(formatWithTime, bundle.getLocale());
 		DateFormat df_noTime = new SimpleDateFormat(formatNoTime, bundle.getLocale());
 
 		// only create a file if the file is graded OR the file is ungraded w/ at least one submission
 		if (!assignment.isGraded() && (submissionsWithHistory == null || submissionsWithHistory.isEmpty())) {
 		    if (log.isDebugEnabled()) log.debug("Nothing to download!!");
 		} else {
 		    try
 		    {
 		        ZipOutputStream out = new ZipOutputStream(outputStream);
 
 		        // create the folder structure - named after the assignment's title
 		        String root = Validator.escapeZipEntry(assignmentTitle) + Entity.SEPARATOR;
 
 		        if (submissionsWithHistory != null && !submissionsWithHistory.isEmpty())
 		        {
 		            // Create the ZIP file
 
 		            // to keep our folder names unique (otherwise the zip will be corrupt)
 		            List<String> submissionFolderNames = new ArrayList<String>();
 
 		            for (AssignmentSubmission s : submissionsWithHistory)
 		            {
 		                // only create the folder if the student has made a submission
 		                if (s.getSubmissionHistorySet() != null && !s.getSubmissionHistorySet().isEmpty()) {
 		                    User submitterUser = userIdUserMap.get(s.getUserId());
 		                    if (submitterUser != null) {
 		                        // the zip will contain a folder for each submission with the submitter's name
 		                        String submissionFolder = submitterUser.getSortName();
 
 		                        // make sure the folder name is unique in case you have two
 		                        // students with the same name in your class. otherwise
 		                        // zip file will be corrupt
 		                        if (submissionFolderNames.contains(submissionFolder)) {
 		                            submissionFolder = getUniqueFolderName(submissionFolder, submissionFolderNames);
 		                        }
 		                        submissionFolderNames.add(submissionFolder);
 
 		                        Set<AssignmentSubmissionVersion> versionHistory = s.getSubmissionHistorySet();
 
 		                        if (versionHistory != null && !versionHistory.isEmpty()) {
 		                            // we need to keep the file names unique
 		                            List<String> versionFolderNames = new ArrayList<String>(); 
 
 		                            for (AssignmentSubmissionVersion version : versionHistory) {
 		                                // only include submitted versions
 		                                if (version.getSubmittedDate() != null) {
 		                                    // we will create a folder for each submitted version for
 		                                    // this student
 		                                    String versionFolder = root + submissionFolder + Entity.SEPARATOR 
 		                                    + df_withTime.format(version.getSubmittedDate());
 
 		                                    // we need to make it unique in case 2 versions
 		                                    // were submitted with the same hour:minutes AM/PM
 		                                    // time stamp. otherwise the zip file will be corrupt
 		                                    if (versionFolderNames.contains(versionFolder)) {
 		                                        versionFolder = getUniqueFolderName(versionFolder, versionFolderNames);
 		                                    }
 
 		                                    versionFolderNames.add(versionFolder);
 
 		                                    ZipEntry versionFolderEntry = new ZipEntry(
		                                            versionFolder);
 		                                    out.putNextEntry(versionFolderEntry);
		                                    out.flush();
 		                                    out.closeEntry();
 
 		                                    // inside this folder, we will put the submission info
 		                                    if (version.getSubmittedText() != null && version.getSubmittedText().trim().length() > 0)
 		                                    {
 		                                        // create the text file only when a text
 		                                        // exists
 		                                        ZipEntry textEntry = new ZipEntry(versionFolder + Entity.SEPARATOR 
		                                                + bundle.getString("assignment2.assignment_grade_assignment.downloadall.filename_submitted_text")
		                                                + ".txt");
 		                                        out.putNextEntry(textEntry);
 		                                        byte[] text = version.getSubmittedText().getBytes();
 		                                        out.write(text);
 		                                        textEntry.setSize(text.length);
 		                                        out.closeEntry();
 		                                    }
 
 		                                    // add the submission attachments
 		                                    if (version.getSubmissionAttachSet() != null && !version.getSubmissionAttachSet().isEmpty()) {
 		                                        zipAttachments(out, root + submissionFolder,
 		                                                versionFolder, version.getSubmissionAttachSet());
 		                                    }
 		                                }
 		                            }
 		                        }
 		                    }
 		                }
 		            }
 		        }
 
 		        if (assignment.isGraded() && assignment.getGradableObjectId() != null) {
 		            // the buffer used to store grade information
 		            StringBuilder gradesBuilder = new StringBuilder();
 		            gradesBuilder.append(
 		                    bundle.getFormattedMessage("assignment2.assignment_grade-assignment.downloadall.header",
 		                            new Object[] {assignmentTitle}))
 		                            .append("\n");
 
 		            // now iterate through all GRADABLE students in this class to create the grades file
 		            List<String> gradableStudents = permissionLogic.getGradableStudentsForUserForItem(currUserId, assignment);
 
 		            if (gradableStudents != null && !gradableStudents.isEmpty()) {
 		                // get the grade information
 		                Map<String, GradeInformation> userIdGradeMap = gradebookLogic.getGradeInformationForStudents(gradableStudents, assignment.getContextId(), assignment.getGradableObjectId());
 
 		                for (String studentId : gradableStudents) {
 		                    // get their User info
 		                    User student = userIdUserMap.get(studentId);
 		                    if (student != null) {
 		                        gradesBuilder.append("\"");
 		                        gradesBuilder.append(student.getDisplayId());
 		                        gradesBuilder.append("\"");
 		                        gradesBuilder.append(",");
 		                        gradesBuilder.append("\"");
 		                        gradesBuilder.append(student.getSortName());
 		                        gradesBuilder.append("\"");
 		                        gradesBuilder.append(",");
 
 		                        // now check for grade information
 		                        GradeInformation gradeInfo = userIdGradeMap.get(studentId);
 		                        if (gradeInfo != null) {
 		                            String gradebookGrade = "";
 		                            String gradebookComment = "";
 
 		                            if (gradeInfo.getGradebookGrade() != null) {
 		                                gradebookGrade = gradeInfo.getGradebookGrade();
 		                            }
 		                            if (gradeInfo.getGradebookComment() != null) {
 		                                gradebookComment = gradeInfo.getGradebookComment();
 		                            }
 		                            gradesBuilder.append("\"");
 		                            gradesBuilder.append(gradebookGrade);
 		                            gradesBuilder.append("\"");
 		                            gradesBuilder.append(",");
 		                            gradesBuilder.append("\"");
 		                            gradesBuilder.append(gradebookComment);
 		                            gradesBuilder.append("\"");
 		                            gradesBuilder.append(",");
 		                        } else {
 		                            gradesBuilder.append(",,");
 		                        }
 
 		                        gradesBuilder.append("\n");
 		                    }
 		                }
 
 		                // create a grades.csv file into zip
 		                ZipEntry gradesCSVEntry = new ZipEntry(root + assignmentTitle + "-" + contextId + "_" + df_noTime.format(new Date()) + ".csv");
 		                out.putNextEntry(gradesCSVEntry);
 		                byte[] grades = gradesBuilder.toString().getBytes();
 		                out.write(grades);
 		                gradesCSVEntry.setSize(grades.length);
 		                out.closeEntry();
 		            }
 		        }
 
 		        // Complete the ZIP file
 		        out.finish();
 		        out.flush();
 		        out.close();
 		    }
 		    catch (IOException e)
 		    {
 		        exceptionMessage.append("Cannot establish the IO to create zip file. ");
 		        log
 		        .debug(this
 		                + ": getSubmissionsZip--IOException unable to create the zip file for assignment "
 		                + assignmentTitle);
 		    }
 		}
 	}
 
 	private void zipAttachments(ZipOutputStream out, String submittersName,
 	        String sSubAttachmentFolder, Set<? extends AttachmentBase> attachments)
 	{
 	    int attachedUrlCount = 0;
 	    for (AttachmentBase r : attachments)
 	    {
 	        InputStream content = null;
 	        BufferedInputStream bContent = null;
 	        try
 	        {
 	            ContentResource resource = contentLogic.getContentResource(r
 	                    .getAttachmentReference());
 
 	            if (resource == null) 
 	            {
 	                log.warn("Unable to retrieve ContentResource with reference:" + 
 	                        r.getAttachmentReference() + ". This attachment was " +
 	                "not included in the zip file.");
 	            } else
 	            {
 	                String contentType = resource.getContentType();
 
 	                ResourceProperties props = resource.getProperties();
 	                String displayName = props.getPropertyFormatted(props
 	                        .getNamePropDisplayName());
 
 	                // for URL content type, encode a redirect to the body URL
 	                if (contentType.equalsIgnoreCase(ResourceProperties.TYPE_URL))
 	                {
 	                    displayName = "attached_URL_" + attachedUrlCount;
 	                    attachedUrlCount++;
 	                }
 
 	                // buffered stream input
 	                content = resource.streamContent();
 	                byte data[] = new byte[1024 * 10];
 	                bContent = new BufferedInputStream(content,
 	                        data.length);
 
 	                ZipEntry attachmentEntry = new ZipEntry(sSubAttachmentFolder
 	                        + Entity.SEPARATOR + displayName);
 	                out.putNextEntry(attachmentEntry);
 	                int bCount = -1;
 	                while ((bCount = bContent.read(data, 0, data.length)) != -1)
 	                {
 	                    out.write(data, 0, bCount);
 	                }
 	                out.closeEntry();
 	                content.close();
 	            }
 	        }
 	        catch (IOException e)
 	        {
 	            log	.warn(this + ": getSubmissionsZip--IOException: Problem in " +
 	            		"creating the attachment file: submittersName="
 	                    + submittersName + " attachment reference=" + r);
 	        }
 	        catch (ServerOverloadException e)
 	        {
 	            log.warn(this + ": getSubmissionsZip--ServerOverloadException: " +
 	            		"submittersName=" + submittersName + " attachment reference=" + r);
 	        }
 	        finally
 	        {
 	            if (content != null)
 	            {
 	                try
 	                {
 	                    content.close();
 	                }
 	                catch (IOException e)
 	                {
 	                    log.warn("IOException when closing content stream", e);
 	                }
 	            }
 	            if (bContent != null)
 	            {
 	                try
 	                {
 	                    bContent.close();
 	                }
 	                catch (IOException e)
 	                {
 	                    log.warn("IOException when closing bContent stream", e);
 	                }
 	            }
 	        }
 	    } // for
 	}
 	
 	/**
 	 * We can't have two folders at the same level with the same name, so we
 	 * need to append _1, _2, etc until we make it unique. This may occur if
 	 * we have two students with the same name or if a student makes multiple submissions 
 	 * within the same minute (ie at 12:56 PM)
 	 * @param currFolderName
 	 * @param existingFolderNames
 	 * @return a unique file name built from the given currFileName
 	 */
 	private String getUniqueFolderName(String currFolderName, List<String> existingFolderNames) {
 		int extension = 1;
 		String folderName = currFolderName;
 		while (existingFolderNames.contains(folderName)) {
 			folderName = currFolderName + "_" + extension;
 			extension++;
 		}
 		
 		return folderName;
 	}
 }
