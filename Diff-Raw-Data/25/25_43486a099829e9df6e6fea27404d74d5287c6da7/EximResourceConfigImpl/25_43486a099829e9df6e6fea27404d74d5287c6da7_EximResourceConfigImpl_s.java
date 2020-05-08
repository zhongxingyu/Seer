 /*
  * This is a common dao with basic CRUD operations and is not limited to any 
  * persistent layer implementation
  * 
  * Copyright (C) 2008  Imran M Yousuf (imyousuf@smartitengineering.com)
  * 
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3 of the License, or (at your option) any later version.
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  */
 package com.smartitengineering.exim.impl;
 
 import com.smartitengineering.exim.AssociationConfig;
 import com.smartitengineering.exim.EximResourceConfig;
 import java.util.Map;
 
 /**
  * This implementation represents the configuration of a class. 2 configuration
  * objects are considered to be equal if they represent the same class.
  * @author imyousuf
  * @since 0.4
  */
 public class EximResourceConfigImpl
     implements EximResourceConfig {
 
     private boolean associateExportPolicyAsUri;
     private boolean accessByPropertyEnabled;
     private boolean identityCustomizerImplemented;
     private boolean exporterImplemented;
     private boolean importerImplemented;
     private Class domainClass;
     private String pathToResource;
     private String idPrefix;
     private String idPropertyName;
     private String name;
     private Map<String, AssociationConfig> associationConfigs;
 
     public void setAccessByPropertyEnabled(boolean accessByPropertyEnabled) {
         this.accessByPropertyEnabled = accessByPropertyEnabled;
     }
 
     public void setAssociateExportPolicyAsUri(boolean associateExportPolicyAsUri) {
         this.associateExportPolicyAsUri = associateExportPolicyAsUri;
     }
 
     public void setAssociationConfigs(
         Map<String, AssociationConfig> associationConfigs) {
         this.associationConfigs = associationConfigs;
     }
 
     public void setDomainClass(Class domainClass) {
         this.domainClass = domainClass;
     }
 
     public void setExporterImplemented(boolean exporterImplemented) {
         this.exporterImplemented = exporterImplemented;
     }
 
     public void setIdPropertyName(String idPropertyName) {
         this.idPropertyName = idPropertyName;
     }
 
     public void setIdPrefix(String idPrefix) {
         this.idPrefix = idPrefix;
     }
 
     public void setIdentityCustomizerImplemented(
         boolean identityCustomizerImplemented) {
         this.identityCustomizerImplemented = identityCustomizerImplemented;
     }
 
     public void setImporterImplemented(boolean importerImplemented) {
         this.importerImplemented = importerImplemented;
     }
 
     public void setPathToResource(String pathToResource) {
         this.pathToResource = pathToResource;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public boolean isAccessByPropertyEnabled() {
         return accessByPropertyEnabled;
     }
 
     public boolean isAssociateExportPolicyAsUri() {
         return associateExportPolicyAsUri;
     }
 
     public Map<String, AssociationConfig> getAssociationConfigs() {
         return associationConfigs;
     }
 
     public Class getDomainClass() {
         return domainClass;
     }
 
     public boolean isExporterImplemented() {
         return exporterImplemented;
     }
 
     public String getIdPropertyName() {
         return idPropertyName;
     }
 
     public String getIdPrefix() {
         return idPrefix;
     }
 
     public boolean isIdentityCustomizerImplemented() {
         return identityCustomizerImplemented;
     }
 
     public boolean isImporterImplemented() {
         return importerImplemented;
     }
 
     public String getPathToResource() {
         return pathToResource;
     }
 
     public String getName() {
         return name;
     }
 }
