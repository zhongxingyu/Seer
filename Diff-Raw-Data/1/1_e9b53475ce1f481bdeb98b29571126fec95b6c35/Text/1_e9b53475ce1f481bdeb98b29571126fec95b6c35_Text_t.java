 /*
  *  Copyright 2010 The MITRE Corporation (http://www.mitre.org/). All Rights Reserved.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.mitre.medcafe.util;
 
 import java.io.*;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.regex .*;
 
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.util.*;
 
 /**
  *  Representation of the text data
  *  @author: Jeffrey Hoyt
  */
 public class Text
 {
 	private String patientId = "";
 	private String userId = "";
 	private String title = "";
 	private String text = "";
 	
 	public static final String SAVE_TEXT_INSERT = "INSERT INTO user_text (username, patient_id, subject, note) values (?,?,?,?)";
 	public static final String SAVE_TEXT_UPDATE = "UPDATE user_text SET note = ? where  username = ? and patient_id = ? and subject = ?";
 	public static final String SAVE_TEXT_SELECT_CNT = "SELECT count(*) from user_text where username = ? and patient_id = ? and subject = ?";
 	public static final String SAVE_TEXT_SELECT = "SELECT note, note_added from user_text where username = ? and patient_id = ? and subject = ?";
 	public static final String SAVE_TEXTS_SELECT = "SELECT subject, note, note_added from user_text where username = ? and patient_id = ? ";
 	public static final String SAVE_TEXT_DELETE = "DELETE from user_text where username = ? and patient_id = ? and subject= ?";
 	  
 	public Text(String patientId, String userId, String title, String text)	
 	{
 		this(patientId, userId, title);
 		
 		this.text = text;
 	}
 	
 	public Text(String patientId, String userId, String title)	
 	{
 		this(patientId, userId);
 		this.title = title;
 		
 	}
 	
 	public Text(String patientId, String userId)	
 	{
 		this.patientId = patientId;
 		this.userId = userId;
 	}
 	
 	public String getPatientId() {
 		return patientId;
 	}
 	public void setPatientId(String patientId) {
 		this.patientId = patientId;
 	}
 	public String getUserId() {
 		return userId;
 	}
 	public void setUserId(String userId) {
 		this.userId = userId;
 	}
 	public String getTitle() {
 		return title;
 	}
 	public void setTitle(String title) {
		//title = CharEncoding.forHTML(title);
 		this.title = title;
 	}
 	public String getText() {
 		return text;
 	}
 	public void setText(String text) {
 		this.text = text;
 	}
 	  
 
 }
