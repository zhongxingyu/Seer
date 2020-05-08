 /*******************************************************************************
  * JBoss, Home of Professional Open Source
  * Copyright 2010, Red Hat, Inc. and individual contributors
  * by the @authors tag. See the copyright.txt in the distribution for a
  * full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  *******************************************************************************/
 package org.richfaces.tests.metamer.bean;
 
 import java.io.InputStream;
 import java.util.Properties;
 import javax.annotation.PostConstruct;
 import javax.faces.bean.ApplicationScoped;
 import javax.faces.bean.ManagedBean;
 
 import org.richfaces.log.RichfacesLogger;
import org.richfaces.log.Logger;
 
 /**
  * Vendor and version information for project Metamer.
  *
  * @author asmirnov@exadel.com, <a href="mailto:ppitonak@redhat.com">Pavol Pitonak</a>
  * @version $Revision$ 
  */
 @ManagedBean(name = "metamer")
 @ApplicationScoped
 public final class VersionBean {
 
     private static final Logger LOGGER = RichfacesLogger.APPLICATION.getLogger();
     private String implementationVendor;
     private String implementationVersion;
     private String implementationTitle;
     private String scmRevision;
     private String scmTimestamp;
     private String fullVersion;
 
     /**
      * Initializes the managed bean.
      */
     @PostConstruct
     public void init() {
         Properties properties = new Properties();
         try {
             InputStream inStream = getClass().getClassLoader().getResourceAsStream("version.properties");
             properties.load(inStream);
         } catch (Exception e) {
             LOGGER.warn("Unable to load version.properties using PomVersion.class.getClassLoader().getResourceAsStream(...)", e);
         }
 
         implementationTitle = properties.getProperty("Implementation-Title");
         implementationVendor = properties.getProperty("Implementation-Vendor");
         implementationVersion = properties.getProperty("Implementation-Version");
         scmRevision = properties.getProperty("SCM-Revision");
         scmTimestamp = properties.getProperty("SCM-Timestamp");
     }
 
     public String getVendor() {
         return implementationVendor;
     }
 
     public String getTitle() {
         return implementationTitle;
     }
 
     public String getRevision() {
         return scmRevision;
     }
 
     public String getTimestamp() {
         return scmTimestamp;
     }
 
     public String getVersion() {
         return implementationVersion;
     }
 
     public String getFullVersion() {
         if (fullVersion != null) {
             return fullVersion;
         }
 
         if (implementationVersion == null) {
             implementationVersion = "Metamer: RichFaces Testing Application, version unknown";
             return implementationVersion;
         }
 
         fullVersion = implementationTitle + " by " + implementationVendor + ", version " + implementationVersion + " SVN r. " + scmRevision;
         return fullVersion;
     }
 
     @Override
     public String toString() {
         return getFullVersion();
     }
 }
