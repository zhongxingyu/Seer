 /*
  * #%L
  * Java code for use with WiscScan.
  * %%
 * Copyright (C) 2008 - 2014 Board of Regents of the University of
  * Wisconsin-Madison.
  * %%
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
  * #L%
  */
 
 package loci.wiscscan.auth;
 
 /**
  * Data structure for managing secure Kerberos logins.
  *
  * @author Hanly De Los Santos
  */
 public class Credentials {
 
 	// Members
 	public String m_password;
 	public String m_username;
 
 	public String getM_password() {
 		return m_password;
 	}
 	public void setM_password(String m_password) {
 		this.m_password = m_password;
 	}
 	public String getM_username() {
 		return m_username;
 	}
 	public void setM_username(String m_username) {
 		this.m_username = m_username;
 	}
 
 }
