 /*
  * Copyright 2004-2012 ICEsoft Technologies Canada Corp.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the
  * License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS
  * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
  * express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 package org.icemobile.samples.spring.mediacast;
 
 import javax.validation.constraints.Size;
 
 public class UploadForm {
 	
 	public static final String PAGE_UPLOAD = "upload";
 	public static final String PAGE_GALLERY = "gallery";
 	public static final String PAGE_VIEWER = "viewer";
 	public static final String PAGE_ALL = "all";
 
 
 	public static final String DESKTOP = "d";
 	public static final String MOBILE = "m";
 	public static final String TABLET = "t";
 	
 	@Size(max = 164)
 	private String description;
 	
 	private String l;//layout
 	
 	private String p = PAGE_UPLOAD;//page
 	
 	private String id;
 	
 	private String form;
 	
	
 	public String getForm() {
 		return form;
 	}
 
 	public void setForm(String form) {
 		this.form = form;
 	}
 
 	public String getId() {
 		return id;
 	}
 
 	public void setId(String id) {
 		this.id = cleanParam(id);
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	public String getL() {
 		return l;
 	}
 
 	public void setL(String layout) {
 		if( notNullOrEmpty(layout)){
 			this.l = cleanParam(layout);
 			if( TABLET.equals(l) ){
 				p = PAGE_ALL;
 			}
 			else if( PAGE_ALL.equals(p)){
 				p = PAGE_UPLOAD;
 			}
 		}
 	}
 	
 	public String cleanParam(String param){
 		if( param != null && param.indexOf(",") > 0 ){
 			param = param.substring(0,param.indexOf(","));
 		}
 		return param;
 	}
 	
 	private boolean notNullOrEmpty(String param){
 		if( param != null && param.length() > 0 ){
 			return true;
 		}
 		return false;
 	}
 
 	public String getP() {
 		return p;
 	}
 
 	public void setP(String p) {
 		this.p = cleanParam(p);
 	}
 
 	@Override
 	public String toString() {
 		return "UploadForm [description=" + description
 				+ ", l=" + l + ", p=" + p + ", id=" + id + ", form="
 				+ form  + "]";
 	}
 
 
 }
