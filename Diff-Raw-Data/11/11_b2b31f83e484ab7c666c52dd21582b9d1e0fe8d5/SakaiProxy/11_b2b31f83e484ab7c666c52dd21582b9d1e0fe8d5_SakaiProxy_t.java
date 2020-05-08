 /*************************************************************************************
  * Copyright 2006, 2008 Sakai Foundation
  *
  * Licensed under the Educational Community License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License. 
  * You may obtain a copy of the License at
  *
  *       http://www.osedu.org/licenses/ECL-2.0
  *
  * Unless required by applicable law or agreed to in writing, software 
  * distributed under the License is distributed on an "AS IS" BASIS, 
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
  * See the License for the specific language governing permissions and 
  * limitations under the License.
 
  *************************************************************************************/
 package org.sakaiproject.blog.cover;
 
 import org.sakaiproject.component.cover.ComponentManager;
 
 public class SakaiProxy
 {
     private static org.sakaiproject.blog.api.SakaiProxy m_instance = null;
     
     private SakaiProxy() {}
     
 	 /**
      * Access the component instance: special cover only method.
      * 
      * @return the component instance.
      */
     public static org.sakaiproject.blog.api.SakaiProxy getInstance()
     {
         if (ComponentManager.CACHE_COMPONENTS)
         {
             if (m_instance == null)
             {
                 m_instance = (org.sakaiproject.blog.api.SakaiProxy) ComponentManager
                         .get(org.sakaiproject.blog.api.SakaiProxy.class);
             }
             
             return m_instance;
         }
         else
         {
             return (org.sakaiproject.blog.api.SakaiProxy) ComponentManager
                     .get(org.sakaiproject.blog.api.SakaiProxy.class);
         }
     }
     
 	public static String getServerUrl()
 	{
 		org.sakaiproject.blog.api.SakaiProxy sp = getInstance();
 		return sp.getServerUrl();
 	}
 	
	public static String getBlogPageId(String siteId)
 	{
 		org.sakaiproject.blog.api.SakaiProxy sp = getInstance();
		return sp.getBlogPageId(siteId);
 	}
 	
	public static String getBlogToolId(String siteId)
 	{
 		org.sakaiproject.blog.api.SakaiProxy sp = getInstance();
		return sp.getBlogToolId(siteId);
 	}
 }
