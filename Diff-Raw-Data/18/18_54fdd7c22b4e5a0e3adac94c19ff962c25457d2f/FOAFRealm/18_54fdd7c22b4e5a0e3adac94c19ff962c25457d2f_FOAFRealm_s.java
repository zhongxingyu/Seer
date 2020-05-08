 /*
  * Copyright 2007 Wyona
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.wyona.org/licenses/APACHE-LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 
 package org.wyona.yanel.impl.map;
 
 import org.apache.log4j.Category;
 
 import org.wyona.yarep.core.Repository;
 import org.wyona.yarep.core.RepositoryFactory;
 
 /**
  *
  */
 public class FOAFRealm extends org.wyona.yanel.core.map.Realm {
 
     private Category log = Category.getInstance(FOAFRealm.class);
 
     Repository profilesRepo;
 
     /**
      *
      */
     public FOAFRealm(String name, String id, String mountPoint, java.io.File configFile) throws Exception {
         super(name, id, mountPoint, configFile);
         log.error("DEBUG: Custom FOAF Realm implementation!");
 
	String repoConfig = "/home/michi/src/wyona/wyona/misc/foaf/jcr-data-repository.xml";
	//String repoConfig = "/home/michi/src/wyona-svn/wyona/misc/foaf/jcr-data-repository.xml";
        profilesRepo = new RepositoryFactory().newRepository("profiles", new java.io.File(repoConfig));
     }
 
     /**
      *
      */
     public Repository getProfilesRepository() {
         return profilesRepo;
     }
 
     /**
      *
      */
     public void destroy() throws Exception {
         this.destroy();
         getProfilesRepository().close();
     }
 }
