 /**
  * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
  *
  * This program is licensed to you under the Apache License Version 2.0,
  * and you may not use this file except in compliance with the Apache License Version 2.0.
  * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the Apache License Version 2.0 is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
  */
 package org.sonatype.jettytestsuite;
 
import java.util.ArrayList;
 import java.util.List;
 
 /**
  * The Class WebappContext.
  * 
  * @author cstamas
  */
 public class WebappContext
 {
 
     /** The name. */
     private String name;
 
     /** The context path. */
     private String contextPath;
 
     /** The authentication info. */
     private AuthenticationInfo authenticationInfo;
 
     /** The servlet infos. */
     private List<ServletInfo> servletInfos;

     private List<ServletFilterInfo> servletFilterInfos;
 
     /**
      * Gets the name.
      * 
      * @return the name
      */
     public String getName()
     {
         return name;
     }
 
     /**
      * Sets the name.
      * 
      * @param name the new name
      */
     public void setName( String name )
     {
         this.name = name;
         setContextPath( "/" + name );
     }
 
     /**
      * Gets the context path.
      * 
      * @return the context path
      */
     public String getContextPath()
     {
         return contextPath;
     }
 
     /**
      * Sets the context path.
      * 
      * @param contextPath the new context path
      */
     public void setContextPath( String contextPath )
     {
         this.contextPath = contextPath;
     }
 
     /**
      * Gets the authentication info.
      * 
      * @return the authentication info
      */
     public AuthenticationInfo getAuthenticationInfo()
     {
         return authenticationInfo;
     }
 
     /**
      * Sets the authentication info.
      * 
      * @param authenticationInfo the new authentication info
      */
     public void setAuthenticationInfo( AuthenticationInfo authenticationInfo )
     {
         this.authenticationInfo = authenticationInfo;
     }
 
     /**
      * Gets the servlet infos.
      * 
      * @return the servlet infos
      */
     public List<ServletInfo> getServletInfos()
     {
         return servletInfos;
     }
 
     /**
      * Sets the servlet infos.
      * 
      * @param servletInfos the new servlet infos
      */
     public void setServletInfos( List<ServletInfo> servletInfos )
     {
         this.servletInfos = servletInfos;
     }
 
     public List<ServletFilterInfo> getServletFilterInfos()
     {
        if ( servletFilterInfos == null )
        {
            servletFilterInfos = new ArrayList<ServletFilterInfo>();
        }
         return servletFilterInfos;
     }
 
     public void setServletFilterInfos( List<ServletFilterInfo> servletFilterInfos )
     {
         this.servletFilterInfos = servletFilterInfos;
    }

 }
