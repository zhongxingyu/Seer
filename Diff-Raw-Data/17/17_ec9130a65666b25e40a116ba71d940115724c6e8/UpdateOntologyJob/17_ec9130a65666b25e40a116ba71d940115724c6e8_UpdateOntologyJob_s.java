 package uk.ac.ebi.arrayexpress.jobs;
 
 /*
 * Copyright 2009-2011 European Molecular Biology Laboratory
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
 
 import org.quartz.JobExecutionContext;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import uk.ac.ebi.arrayexpress.app.ApplicationJob;
 import uk.ac.ebi.arrayexpress.components.JobsController;
 import uk.ac.ebi.arrayexpress.components.Ontologies;
 import uk.ac.ebi.arrayexpress.utils.StringTools;
 import uk.ac.ebi.arrayexpress.utils.efo.EFOLoader;
 
 import java.io.File;
 import java.io.InputStream;
 import java.net.URI;
 
 public class UpdateOntologyJob extends ApplicationJob
 {
     // logging machinery
     private final Logger logger = LoggerFactory.getLogger(getClass());
 
     public void doExecute( JobExecutionContext jec ) throws Exception
     {
         // check the version of EFO from update location; if newer, copy it
         // over to our location and launch a reload process
         String efoLocation = getPreferences().getString("bs.efo.update.source");
         URI efoURI = new URI(efoLocation);
         logger.info("Checking EFO ontology version from [{}]", efoURI.toString());
         String version = EFOLoader.getOWLVersion(efoURI);
         String loadedVersion = ((Ontologies)getComponent("Ontologies")).getEfo().getVersionInfo();
         if ( null != version
                 && !version.equals(loadedVersion)
                && Float.parseFloat(version) > Float.parseFloat(loadedVersion)
                 ) {
             // we have newer version, let's fetch it and copy it over to our working location
             logger.info("Updating EFO with version [{}]", version);
             InputStream is = null;
             try {
                 is = efoURI.toURL().openStream();
                 File efoFile = new File(getPreferences().getString("bs.efo.location"));
                 StringTools.stringToFile(StringTools.streamToString(is, "UTF-8"), efoFile, "UTF-8");
                 getApplication().sendEmail("EFO update",
                         "Experimental Factor Ontology has been updated to version [" + version + "]" + StringTools.EOL
                             + StringTools.EOL
                             + "Application [${variable.appname}]" + StringTools.EOL
                             + "Host [${variable.hostname}]" + StringTools.EOL
                 );
                 ((JobsController) getComponent("JobsController")).executeJob("reload-efo");
             } finally {
                 if ( null != is ) {
                     is.close();
                 }
             }
             
         } else {
             logger.info("Current ontology version [{}] is up-to-date", loadedVersion);
         }
     }
 }
