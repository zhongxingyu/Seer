 package org.motechproject.ghana.national.tools.seed.data;
 
 import org.apache.log4j.Logger;
 import org.motechproject.ghana.national.domain.OutPatientVisit;
 import org.motechproject.ghana.national.repository.AllOutPatientVisits;
 import org.motechproject.ghana.national.tools.seed.Seed;
 import org.motechproject.ghana.national.tools.seed.data.source.OutPatientVisitSource;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 @Component
 public class OutPatientVisitMigrationSeed extends Seed {
 
     private static Logger LOG = Logger.getLogger(MobileMidwifeMigrationSeed.class);
 
     @Autowired
     private OutPatientVisitSource outPatientVisitSource;
 
     @Autowired
     private AllOutPatientVisits allOutPatientVisists;
 
     @Override
     protected void load() {
 
         try {
             for (OutPatientVisit outPatientVisist : outPatientVisitSource.getOutPatientVisitList()) {
                allOutPatientVisists.add(outPatientVisist);
             }
         } catch (Exception e) {
             LOG.info("Exception occurred while migrating OutPatientVisits :" + e);
         }
     }
 }
