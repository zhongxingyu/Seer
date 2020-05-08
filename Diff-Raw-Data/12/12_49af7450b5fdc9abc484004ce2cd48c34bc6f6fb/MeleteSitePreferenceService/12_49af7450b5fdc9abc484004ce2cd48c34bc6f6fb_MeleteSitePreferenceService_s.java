 /*
  * Created on Oct 11, 2006
  *
 * Copyright (c) 2004, 2005, 2006, 2007 Foothill College, ETUDES Project 
 *   
 * Licensed under the Apache License, Version 2.0 (the "License"); you 
 * may not use this file except in compliance with the License. You may 
 * obtain a copy of the License at 
 *   
 * http://www.apache.org/licenses/LICENSE-2.0 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing 
 * permissions and limitations under the License. 
  */
 package org.sakaiproject.api.app.melete;
 
 /**
  * @author Faculty
  *
  * TODO To change the template for this generated type comment go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 public interface MeleteSitePreferenceService {
 	public abstract String getPrefSiteId();
 	public abstract void setPrefSiteId(String prefSiteId);
 	public abstract boolean isPrintable();
 	public abstract void setPrintable(boolean printable);
 }
