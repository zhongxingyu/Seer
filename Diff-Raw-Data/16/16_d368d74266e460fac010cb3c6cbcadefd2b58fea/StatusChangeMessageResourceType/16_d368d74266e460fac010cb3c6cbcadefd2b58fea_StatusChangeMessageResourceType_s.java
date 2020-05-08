 /**
 
  * Copyright (C) 2011 Shaun Johnson, LMXM LLC
  * 
  * This file is part of Universal Task Executor.
  * 
  * Universal Task Executor is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by the Free
  * Software Foundation, either version 3 of the License, or (at your option) any
  * later version.
  * 
  * Universal Task Executor is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * Universal Task Executor. If not, see <http://www.gnu.org/licenses/>.
  */
 package net.lmxm.ute.resources.types;
 
 import javax.swing.Icon;
 
 import net.lmxm.ute.enums.ActionCommand;
 import net.lmxm.ute.resources.ResourceCategory;
 
 /**
  * The Enum StatusChangeMessageResourceType.
  */
 public enum StatusChangeMessageResourceType implements ResourceType {
 
 	DISABLED_TASK_SKIPPED,
 
 	FILE_DELETE_DIRECTORY_DELETE_FINISHED,
 	FILE_DELETE_DIRECTORY_DELETE_STARTED,
 	FILE_DELETE_ERROR,
 	FILE_DELETE_FILE_DELETE_FINISHED,
 	FILE_DELETE_FILE_DELETE_STARTED,
 	FILE_DELETE_FILE_DOES_NOT_EXIST_ERROR,
 	FILE_DELETE_PATH_DOES_NOT_EXIST_ERROR,
 
 	FIND_REPLACE_EXECUTION_FINISHED,
 	FIND_REPLACE_NO_MATCHING_FILES,
 	FIND_REPLACE_NOT_FILE_ERROR,
 
 	GROOVY_COMPILATION_ERROR,
 	GROOVY_EXECUTION_ERROR,
 	GROOVY_EXECUTION_FINISHED,
 	GROOVY_EXECUTION_STARTED,
 
 	HTTP_DOWNLOAD_ERROR,
 	HTTP_DOWNLOAD_FILE_LIST_EMPTY,
 	HTTP_DOWNLOAD_FINISHED,
 	HTTP_DOWNLOAD_NO_RESPONSE_ERROR,
 	HTTP_DOWNLOAD_STATUS_ERROR,
 
 	JOB_ABORTED,
 	JOB_FINISHED,
 	JOB_STARTED,
 	JOB_STOPPED,
 
     MAVEN_REPOSITORY_ARTIFACT_DEPLOYED,
     MAVEN_REPOSITORY_ARTIFACT_DEPLOYING,
     MAVEN_REPOSITORY_ARTIFACT_DOWNLOADED,
     MAVEN_REPOSITORY_ARTIFACT_DOWNLOADING,
     MAVEN_REPOSITORY_ARTIFACT_INVALID_DESCRIPTOR,
     MAVEN_REPOSITORY_ARTIFACT_MISSING_DESCRIPTOR,
     MAVEN_REPOSITORY_ARTIFACT_RESOLVED,
     MAVEN_REPOSITORY_ARTIFACT_RESOLVING,
 
     MAVEN_REPOSITORY_METADATA_DEPLOYED,
     MAVEN_REPOSITORY_METADATA_DEPLOYING,
     MAVEN_REPOSITORY_METADATA_RESOLVED,
     MAVEN_REPOSITORY_METADATA_RESOLVING,
 
     MAVEN_REPOSITORY_DOWNLOAD_COMPLETED,
     MAVEN_REPOSITORY_DOWNLOAD_INITIATED,
     MAVEN_REPOSITORY_DOWNLOAD_FAILED,
     MAVEN_REPOSITORY_DOWNLOAD_STARTED,
     MAVEN_REPOSITORY_DOWNLOAD_SUCCEEDED,
     MAVEN_REPOSITORY_DOWNLOAD_UNABLE_TO_DELETE_LOCAL_REPOSITORY,
     MAVEN_REPOSITORY_TRANSFER_COMPLETED,
     MAVEN_REPOSITORY_TRANSFER_FAILED,
     MAVEN_REPOSITORY_TRANSFER_PROGRESSED,
     MAVEN_REPOSITORY_TRANSFER_STARTED,
     MAVEN_REPOSITORY_TRANSFER_CORRUPTED,
     MAVEN_REPOSITORY_UPLOAD_INITIATED,
     MAVEN_REPOSITORY_UPLOAD_SUCCEEDED,
 
	SUBVERSION_AUTHENCITAION_FAILED,
 
 	SUBVERSION_EXPORT_DIRECTORY_ADDED,
 	SUBVERSION_EXPORT_ERROR,
 	SUBVERSION_EXPORT_FILE,
 	SUBVERSION_EXPORT_FILE_ADDED,
 	SUBVERSION_EXPORT_FILE_ADDED_AS,
 	SUBVERSION_EXPORT_FINISHED,
 	SUBVERSION_EXPORT_REPORT_ERROR,
 	SUBVERSION_EXPORT_STARTED,
 
 	SUBVERSION_UPDATE_ERROR,
 	SUBVERSION_UPDATE_EVENT_ADD,
 	SUBVERSION_UPDATE_EVENT_DELETE,
 	SUBVERSION_UPDATE_EVENT_FAILED_EXTERNAL,
 	SUBVERSION_UPDATE_EVENT_LOCK_FAILED,
 	SUBVERSION_UPDATE_EVENT_LOCKED,
 	SUBVERSION_UPDATE_EVENT_UPDATE,
 	SUBVERSION_UPDATE_EVENT_UPDATE_COMPLETED,
 	SUBVERSION_UPDATE_EVENT_UPDATE_EXTERNAL,
 	SUBVERSION_UPDATE_EVENT_UPDATE_NONE,
 	SUBVERSION_UPDATE_FINISHED,
 	SUBVERSION_UPDATE_STARTED;
 
 	/*
 	 * (non-Javadoc)
 	 * @see net.lmxm.ute.resources.ResourceType#getActionCommand()
 	 */
 	@Override
 	public ActionCommand getActionCommand() {
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see net.lmxm.ute.resources.ResourceType#getIcon()
 	 */
 	@Override
 	public Icon getIcon() {
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see net.lmxm.ute.resources.ResourceType#getResourceCategory()
 	 */
 	@Override
 	public ResourceCategory getResourceCategory() {
 		return ResourceCategory.STATUS_CHANGE_MESSAGE;
 	}
 }
