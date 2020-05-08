 /**
  * Copyright 2010 Eric Taix
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and 
  * limitations under the License.
  * 
  */
 package com.bigpupdev.synodroid.data;
 
 import com.bigpupdev.synodroid.R;
 
 import android.content.Context;
 
 /**
  * Here are defined all torrent'status
  * 
  * @author Eric Taix (eric.taix at gmail.com)
  */
 public enum TaskStatus {
 	TASK_UNKNOWN, 
 	TASK_WAITING, 
 	TASK_DOWNLOADING, 
 	TASK_SEEDING, 
 	TASK_PAUSED, 
 	TASK_FINISHED, 
 	TASK_HASH_CHECKING, 
 	TASK_ERROR, 
 	TASK_ERROR_BROKEN_LINK, 
 	TASK_ERROR_DISK_FULL, 
 	TASK_PRE_SEEDING, 
 	TASK_FINISHING, 
 	TASK_ERROR_DEST_NO_EXIST, 
 	TASK_ERROR_DEST_DENY, 
 	TASK_ERROR_QUOTA_REACHED, 
 	TASK_ERROR_TIMEOUT, 
 	TASK_ERROR_EXCEED_MAX_FS_SIZE, 
 	TASK_ERROR_EXCEED_MAX_TEMP_FS_SIZE, 
 	TASK_ERROR_EXCEED_MAX_DEST_FS_SIZE, 
 	TASK_ERROR_TORRENT_DUPLICATE, 
 	TASK_ERROR_TORRENT_INVALID,
 	TASK_FILEHOSTING_WAITING,
 	TASK_EXTRACTING,
 	TASK_ERROR_NAME_TOO_LONG_ENCRYPTION,
 	TASK_ERROR_NAME_TOO_LONG,
 	TASK_ERROR_FILE_NO_EXIST,
 	TASK_ERROR_REQUIRED_PREMIUM,
 	TASK_ERROR_NOT_SUPPORT_TYPE,
 	TASK_ERROR_FTP_ENCRYPTION_NOT_SUPPORT_TYPE,
 	TASK_ERROR_EXTRACT_FAIL,
 	TASK_ERROR_EXTRACT_WRONG_PASSWORD,
 	TASK_ERROR_EXTRACT_INVALID_ARCHIVE,
 	TASK_ERROR_EXTRACT_QUOTA_REACHED,
 	TASK_ERROR_EXTRACT_DISK_FULL,
 	TASK_ERROR_REQUIRED_ACCOUNT;
 
 	/**
 	 * Return a localized status label
 	 * 
 	 * @param ctxP
 	 * @param statusP
 	 * @return
 	 */
 	public static String getLabel(Context ctxP, String statusP) {
 		try{
 			TaskStatus status = TaskStatus.valueOf(statusP);
 			switch (status) {
 			case TASK_FILEHOSTING_WAITING:
 			case TASK_WAITING:
 				return ctxP.getString(R.string.detail_status_waiting);
 			case TASK_DOWNLOADING:
 				return ctxP.getString(R.string.detail_status_downloading);
 			case TASK_PRE_SEEDING:
 			case TASK_SEEDING:
 				return ctxP.getString(R.string.detail_status_seeding);
 			case TASK_PAUSED:
 				return ctxP.getString(R.string.detail_status_paused);
 			case TASK_FINISHING:
 			case TASK_FINISHED:
 				return ctxP.getString(R.string.detail_status_finished);
 			case TASK_HASH_CHECKING:
 				return ctxP.getString(R.string.detail_status_hash_checking);
 			case TASK_UNKNOWN:
 				return ctxP.getString(R.string.detail_unknown);
 			case TASK_ERROR_DEST_NO_EXIST:
 				return ctxP.getString(R.string.detail_status_error_no_exist);
 			case TASK_ERROR_DEST_DENY:
 				return ctxP.getString(R.string.detail_status_error_denied);
 			case TASK_ERROR_EXTRACT_QUOTA_REACHED:
 			case TASK_ERROR_QUOTA_REACHED:
 				return ctxP.getString(R.string.detail_status_error_quota);
 			case TASK_ERROR_TIMEOUT:
 				return ctxP.getString(R.string.detail_status_error_timeout);
 			case TASK_ERROR_EXCEED_MAX_FS_SIZE:
 				return ctxP.getString(R.string.detail_status_error_max_fs_size);
 			case TASK_ERROR_BROKEN_LINK:
 				return ctxP.getString(R.string.detail_status_error_broken);
 			case TASK_ERROR_EXTRACT_DISK_FULL:
 			case TASK_ERROR_DISK_FULL:
 				return ctxP.getString(R.string.detail_status_error_full);
 			case TASK_ERROR_EXCEED_MAX_TEMP_FS_SIZE:
 				return ctxP.getString(R.string.detail_status_error_max_temp_file);
 			case TASK_ERROR_EXCEED_MAX_DEST_FS_SIZE:
 				return ctxP.getString(R.string.detail_status_error_max_dest_file);
 			case TASK_ERROR_TORRENT_DUPLICATE:
 				return ctxP.getString(R.string.detail_status_error_duplicate);
 			case TASK_ERROR_TORRENT_INVALID:
 				return ctxP.getString(R.string.detail_status_error_invalid);
 			case TASK_EXTRACTING:
 				return ctxP.getString(R.string.detail_status_extracting);
 			case TASK_ERROR_NAME_TOO_LONG_ENCRYPTION:
 			case TASK_ERROR_NAME_TOO_LONG:
 				return ctxP.getString(R.string.detail_status_error_name_too_long);
 			case TASK_ERROR_FILE_NO_EXIST:
 				return ctxP.getString(R.string.detail_status_error_not_exist);
 			case TASK_ERROR_REQUIRED_PREMIUM:
 				return ctxP.getString(R.string.detail_status_error_premium);
 			case TASK_ERROR_NOT_SUPPORT_TYPE:
 			case TASK_ERROR_FTP_ENCRYPTION_NOT_SUPPORT_TYPE:
 				return ctxP.getString(R.string.detail_status_error_not_supported);
 			case TASK_ERROR_EXTRACT_FAIL:
 				return ctxP.getString(R.string.detail_status_error_extract_failed);
 			case TASK_ERROR_EXTRACT_WRONG_PASSWORD:
 				return ctxP.getString(R.string.detail_status_error_extract_wrong_password);
 			case TASK_ERROR_EXTRACT_INVALID_ARCHIVE:
 				return ctxP.getString(R.string.detail_status_error_invalid_archive);
 			case TASK_ERROR_REQUIRED_ACCOUNT:
 				return ctxP.getString(R.string.detail_status_error_account);
 			case TASK_ERROR:
 			default:
 				return ctxP.getString(R.string.detail_status_error);
 			}
 		} catch (IllegalArgumentException e){
 			return statusP;
 		}
 		
 	}
 }
