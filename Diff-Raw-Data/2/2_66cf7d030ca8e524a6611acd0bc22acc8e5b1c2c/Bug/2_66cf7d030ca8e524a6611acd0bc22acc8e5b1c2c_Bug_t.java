 /*
  * This file is part of the Alitheia system, developed by the SQO-OSS
  * consortium as part of the IST FP6 SQO-OSS project, number 033331.
  *
  * Copyright 2007 by the SQO-OSS consortium members <info@sqo-oss.eu>
  * Copyright 2008 by Panos Louridas <louridas@aueb.gr>
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are
  * met:
  *
  *     * Redistributions of source code must retain the above copyright
  *       notice, this list of conditions and the following disclaimer.
  *
  *     * Redistributions in binary form must reproduce the above
  *       copyright notice, this list of conditions and the following
  *       disclaimer in the documentation and/or other materials provided
  *       with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
  * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
  * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
  * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
  * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  */
 
 package eu.sqooss.service.db;
 
 import eu.sqooss.service.db.DAObject;
 import java.util.Properties;
 
 /**
  * This class represents the data relating to bugs, stored in the database
  */
 public class Bug extends DAObject {
     /**
     * The commit which resolves the bug
      */
     private Commit commit;
     /**
      * A description of the bug
      */
     private String description;
     /**
      * The properties related to the bug
      */
     private Properties properties;
 
     public Bug() {
         // Nothing to do here
     }
 
     public Commit getCommit() {
         return commit;
     }
     
     public void setCommit(Commit commit) {
         this.commit = commit;
     }
     
     public String getDescription() {
         return description;
     }
 
     public void setDescription(String description) {
         this.description = description;
     }
     
     public Properties getProperties() {
         return properties;
     }
     
     public void setProperties(Properties properties) {
         this.properties = properties;
     }
 }
 
 //vi: ai nosi sw=4 ts=4 expandtab
 
