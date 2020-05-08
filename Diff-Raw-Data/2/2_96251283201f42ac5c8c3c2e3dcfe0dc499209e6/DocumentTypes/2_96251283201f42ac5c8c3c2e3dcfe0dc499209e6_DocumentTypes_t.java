 package uk.ac.ebi.arrayexpress.utils;
 
 /*
  * Copyright 2009-2010 European Molecular Biology Laboratory
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
 
 public enum DocumentTypes {
     EXPERIMENTS("experiments", "ae.experiments.file.location"),
    FILES("files", "ae.files.persistence.file.location"),
     PROTOCOLS("protocols", "ae.protocols.file.location"),
     ARRAYDESIGNS("array_designs", "ae.designs.file.location");
 
     private final String textName;
     private final String persistenceDocumetLocation;
 
 
     DocumentTypes(String textName, String persistenceDocumetLocation) {
         this.textName = textName;
         this.persistenceDocumetLocation = persistenceDocumetLocation;
     }
 
     public String getTextName() {
         return textName;
     }
 
     public String getPersistenceDocumetLocation() {
         return persistenceDocumetLocation;
     }
 
     public static DocumentTypes getInstanceByName(String name) {
 		for (DocumentTypes resourceType : DocumentTypes.values()) {
 			if (resourceType.textName.equals(name)) return resourceType;
 		}
 		throw new IllegalArgumentException("There is no DocumentTypes with name: " + name);
 	}
 
 }
