 package org.apache.maven.archiva.meeper;
 
 /*
  * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
  * agreements. See the NOTICE file distributed with this work for additional information regarding
  * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance with the License. You may obtain a
  * copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software distributed under the License
  * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
  * or implied. See the License for the specific language governing permissions and limitations under
  * the License.
  */
 
 import org.apache.commons.lang.builder.ReflectionToStringBuilder;
 
 /**
  * Stores a synced repository data.
  * 
  * @author <a href="mailto:carlos@apache.org">Carlos Sanchez</a>
  * @version $Id$
  */
 public class SyncedRepository
 {
 
     public static final String PROTOCOL_SSH = "rsync_ssh";
 
     public static final String PROTOCOL_RSYNC = "rsync";
 
     public static final String PROTOCOL_SVN = "svn";
 
     private String groupId;
 
     private String location;
 
     private String protocol;
 
     private String contactName;
 
     private String contactMail;
 
     private String sshOptions;
 
     private String svnUrl;
 
    private StringBuffer out = new StringBuffer();

    private StringBuffer err = new StringBuffer();
 
     public void setGroupId( String groupId )
     {
         this.groupId = groupId;
     }
 
     public String getGroupId()
     {
         return groupId;
     }
 
     public void setContactName( String contactName )
     {
         this.contactName = contactName;
     }
 
     public String getContactName()
     {
         return contactName;
     }
 
     public void setContactMail( String contactMail )
     {
         this.contactMail = contactMail;
     }
 
     public String getContactMail()
     {
         return contactMail;
     }
 
     public void setLocation( String location )
     {
         this.location = location;
     }
 
     public String getLocation()
     {
         return location;
     }
 
     public void setProtocol( String protocol )
     {
         this.protocol = protocol;
     }
 
     public String getProtocol()
     {
         return protocol;
     }
 
     public String getSshOptions()
     {
         return sshOptions;
     }
 
     public void setSshOptions( String sshOptions )
     {
         this.sshOptions = sshOptions;
     }
 
     public String getSvnUrl()
     {
         return svnUrl;
     }
 
     public void setSvnUrl( String svnUrl )
     {
         this.svnUrl = svnUrl;
     }
 
     public String toString()
     {
         return ReflectionToStringBuilder.toString( this );
     }
 
    public StringBuffer getOut()
     {
         return out;
     }
 
    public StringBuffer getErr()
     {
         return err;
     }
 
 }
