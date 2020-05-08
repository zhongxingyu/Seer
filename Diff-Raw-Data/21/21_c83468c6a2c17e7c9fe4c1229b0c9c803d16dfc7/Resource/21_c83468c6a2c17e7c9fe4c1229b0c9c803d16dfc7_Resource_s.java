 /**
  *    Copyright 2012-2013 Trento RISE
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 
 package eu.trentorise.smartcampus.filestorage.model;
 
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlRootElement;
 
 /**
  * <i>Resource</i> represents any type of file stored in the storage
  * 
  * @author mirko perillo
  * 
  */
 @XmlRootElement
 @XmlAccessorType(XmlAccessType.FIELD)
 public class Resource {
 	/**
 	 * id of the resource
 	 */
 	private String id;
 	/**
 	 * name of the resource
 	 */
 	private String name;
 	/**
 	 * the content
 	 */
 	private byte[] content;
 	/**
 	 * content type of the resource
 	 */
 	private String contentType;
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public byte[] getContent() {
 		return content;
 	}
 
 	public void setContent(byte[] content) {
 		this.content = content;
 	}
 
 	public String getContentType() {
 		return contentType;
 	}
 
 	public void setContentType(String contentType) {
 		this.contentType = contentType;
 	}
 
 	public String getId() {
 		return id;
 	}
 
 	public void setId(String id) {
 		this.id = id;
 	}
 
 }
