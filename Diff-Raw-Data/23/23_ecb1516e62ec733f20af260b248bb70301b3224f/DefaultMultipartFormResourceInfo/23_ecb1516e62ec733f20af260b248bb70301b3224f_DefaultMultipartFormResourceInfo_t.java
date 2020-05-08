 /*
  * Copyright 2011 Spark Young (sparkyoungs@gmail.com). All Rights Reserved.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.archboy.clobaframe.io.http.impl;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Date;
 import org.apache.commons.fileupload.FileItem;
 import org.apache.commons.fileupload.disk.DiskFileItemFactory;
 import org.archboy.clobaframe.io.http.MultipartFormResourceInfo;
 
 /**
  *
  * @author young
  */
 public class DefaultMultipartFormResourceInfo implements MultipartFormResourceInfo{
 
 	private FileItem fileItem;
 
 	/**
 	 * This object is used to hold the disk temporary file.
 	 *
 	 * Because the {@link DiskFileItemFactory} object keep the marker object, so
 	 * this ResourceInfo keep the {@link DiskFileItemFactory} object is same as keep
 	 * the marker object.
 	 * 
 	 * Details see:
 	 * {@link org.apache.commons.fileupload.disk.DiskFileItemFactory#createItem}
 	 */
 	//private Object fileCleanerMarker;
 
	//private final Logger logger = LoggerFactory.getLogger(DefaultMultipartFormResourceInfo.class);
 
 	public DefaultMultipartFormResourceInfo(FileItem fileItem){ //, Object fileCleanerMarker) {
 		this.fileItem = fileItem;
 		//this.fileCleanerMarker = fileCleanerMarker;
 	}
 
 	@Override
 	public boolean isFormField() {
 		return fileItem.isFormField();
 	}
 
 	@Override
 	public String getFileName() {
 		return fileItem.getName();
 	}
 
 	@Override
 	public String getContentAsString() {
 		return fileItem.getString();
 	}
 
 	@Override
 	public long getContentLength() {
 		return fileItem.getSize();
 	}
 
 	@Override
 	public String getContentType() {
 		return fileItem.getContentType();
 	}
 
 	@Override
 	public InputStream getInputStream() throws IOException {
 		return fileItem.getInputStream();
 	}
 
 	@Override
 	public InputStream getInputStream(long start, long length) throws IOException {
 		throw new UnsupportedOperationException("Does not supported.");
 	}
 
 	@Override
 	public boolean isSeekable() {
 		return false;
 	}
 
 	@Override
 	public String getFieldName() {
 		return fileItem.getFieldName();
 	}
 
 	@Override
 	public Date getLastModified() {
 		return new Date();
 	}
 }
