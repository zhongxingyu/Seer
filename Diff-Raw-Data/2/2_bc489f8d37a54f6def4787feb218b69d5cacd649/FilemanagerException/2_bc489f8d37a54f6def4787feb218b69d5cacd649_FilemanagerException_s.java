 /*
  * C5Connector.Java - The Java backend for the filemanager of corefive.
  * It's a bridge between the filemanager and a storage backend and 
  * works like a transparent VFS or proxy.
  * Copyright (C) Thilo Schwarz
  * 
  * == BEGIN LICENSE ==
  * 
  * Licensed under the terms of any of the following licenses at your
  * choice:
  * 
  *  - GNU General Public License Version 2 or later (the "GPL")
  *    http://www.gnu.org/licenses/gpl.html
  * 
  *  - GNU Lesser General Public License Version 2.1 or later (the "LGPL")
  *    http://www.gnu.org/licenses/lgpl.html
  * 
  *  - Mozilla Public License Version 1.1 or later (the "MPL")
  *    http://www.mozilla.org/MPL/MPL-1.1.html
  * 
  * == END LICENSE ==
  */
 package de.thischwa.c5c.exception;
 
 import de.thischwa.c5c.FilemanagerAction;
 import de.thischwa.c5c.UserObjectProxy;
 
 /**
  * Thrown to indicate known exceptions of the filemanager. The messages based on the localized known messages provided by
 * {@link UserObjectProxy#getFilemanagerErrorMessage(String)}.
  */
 public class FilemanagerException extends C5CException {
 	
 	private static final long serialVersionUID = 1L;
 	
 	public enum Key {
 		AuthorizationRequired("AUTHORIZATION_REQUIRED"),
 		InvalidAction("INVALID_ACTION"),
 		ModeError("MODE_ERROR"),
 		DirectoryAlreadyExists("DIRECTORY_ALREADY_EXISTS"),
 		FileAlreadyExists("FILE_ALREADY_EXISTS"),
 		UnableToCreateDirectory("UNABLE_TO_CREATE_DIRECTORY"),
 		InvalidVar("INVALID_VAR"),
 		DirectoryNotExist("DIRECTORY_NOT_EXIST"),
 		UnableToOpenDirectory("UNABLE_TO_OPEN_DIRECTORY"),
 		ErrorRenamingDirectory("ERROR_RENAMING_DIRECTORY"),
 		ErrorRenamingFile("ERROR_RENAMING_FILE"),
 		InvalidDirectoryOrFile("INVALID_DIRECTORY_OR_FILE"),
 		InvalidFileUpload("INVALID_FILE_UPLOAD"),
 		UploadFilesSmallerThan("UPLOAD_FILES_SMALLER_THAN"),
 		UploadImagesOnly("UPLOAD_IMAGES_ONLY"),
 		UploadImagesTypeJpegGifPng("UPLOAD_IMAGES_TYPE_JPEG_GIF_PNG"),
 		FileNotExists("FILE_DOES_NOT_EXIST"),
 		LanguageFileNotFound("LANGUAGE_FILE_NOT_FOUND");
 		
 		private String propertyName;
 		
 		private Key(String propertyName) {
 			this.propertyName = propertyName;
 		}
 		
 		public String getPropertyName() {
 			return propertyName;
 		}
 	}
 
 	public FilemanagerException(FilemanagerAction mode, Key key, String... params) {
 		super(mode, buildMessage(key, params));
 	}
 
 	public FilemanagerException(Key key, String... params) {
 		super(buildMessage(key, params));
 	}
 
 	private static String buildMessage(Key key, String... params) {
 		String rawMsg = UserObjectProxy.getFilemanagerErrorMessage(key);
 		String msg = (params == null || params.length == 0) ? rawMsg : String.format(rawMsg, ((Object[]) params));
 		return msg;
 	}
 }
