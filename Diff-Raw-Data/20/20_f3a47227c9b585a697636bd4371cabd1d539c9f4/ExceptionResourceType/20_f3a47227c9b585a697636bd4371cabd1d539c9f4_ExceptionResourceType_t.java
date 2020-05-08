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
 
 import static net.lmxm.ute.resources.types.ResourceValueType.MESSAGE;
 
 import javax.swing.Icon;
 
 import net.lmxm.ute.enums.ActionCommand;
 import net.lmxm.ute.resources.ResourceCategory;
 
 public enum ExceptionResourceType implements ResourceType {
 
 	ERROR_CREATING_PREFERENCES_FILE,
 	ERROR_LOADING_CONFIGURATION_FILE,
 	ERROR_LOADING_PREFERENCES_FILE,
 	ERROR_SAVING_CONFIGURATION_FILE,
     FILE_SYSTEM_LOCATION_NODE_MISSING,
     HTTP_LOCATION_NODE_MISSING,
     HTTP_DOWNLOAD_ERROR,
     HTTP_DOWNLOAD_FILE_LIST_EMPTY,
     HTTP_DOWNLOAD_NO_RESPONSE,
     HTTP_DOWNLOAD_STATUS_ERROR,
 	INVALID_CONFIGURATION_FILE,
 	INVALID_PATTERN,
     JOB_NODE_MISSING,
     MAVEN_REPOSITORY_LOCATION_NODE_MISSING,
     PREFERENCE_NODE_MISSING,
 	PREFERENCES_FILE_ALREADY_EXISTS,
     PREFERENCES_MUST_BE_ASSIGNED_VALUES,
     PROPERTY_NODE_MISSING,
     SUBVERSION_REPOSITORY_LOCATION_NODE_MISSING,
     TASK_NODE_MISSING,
    UNEXPECTED_ERROR,
 	UNSUPPORTED_JOB_TYPE,
 	UNSUPPORTED_SCOPE,
 	UNSUPPORTED_SCOPE_TYPE,
 	UNSUPPORTED_SUBVERSION_DEPTH,
 	UNSUPPORTED_SUBVERSION_DEPTH_TYPE,
 	UNSUPPORTED_TASK_TYPE;
 
     private final ResourceValueType[] types = {MESSAGE};
 
 	public ActionCommand getActionCommand() {
 		return null;
 	}
 
 	public Icon getIcon() {
 		return null;
 	}
 
 	public ResourceCategory getResourceCategory() {
 		return ResourceCategory.EXCEPTION;
 	}
 
     public ResourceValueType[] getResourceValueTypes() {
         return types;
     }
 }
