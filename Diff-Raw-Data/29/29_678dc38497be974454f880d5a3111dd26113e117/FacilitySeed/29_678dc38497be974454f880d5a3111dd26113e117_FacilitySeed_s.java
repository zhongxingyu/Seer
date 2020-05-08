 package org.motechproject.ghana.national.tools.seed.data;
 
 import org.motechproject.ghana.national.domain.Facility;
 import org.motechproject.ghana.national.repository.AllFacilities;
import org.motechproject.ghana.national.service.FacilityService;
 import org.motechproject.ghana.national.tools.seed.Seed;
import org.motechproject.ghana.national.tools.seed.data.source.OldGhanaFacility;
 import org.motechproject.ghana.national.tools.seed.data.source.FacilitySource;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import java.util.List;
 
 import static ch.lambdaj.Lambda.on;
 import static ch.lambdaj.Lambda.project;
 
 @Component
 public class FacilitySeed extends Seed {
 
     @Autowired
     AllFacilities allFacilities;
 
     @Autowired
     FacilitySource facilitySource;
 
     @Autowired
    FacilityService facilityService;
 
     @Override
     protected void load() {
         try {
            List<OldGhanaFacility> oldGhanaFacilities = project(allFacilities.facilities(), OldGhanaFacility.class, on(Facility.class).getMrsFacility().getName(), on(Facility.class).getMrsFacility().getId());
             for (OldGhanaFacility oldGhanaFacility : facilitySource.getMotechFacilityNameAndIds()) {
                 String OpenMrsFacilityId = OldGhanaFacility.findByName(oldGhanaFacilities, oldGhanaFacility.getName()).getId();
                 allFacilities.saveLocally(new Facility().mrsFacilityId(OpenMrsFacilityId).motechId(oldGhanaFacility.getId()));
             }
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
     }
 }
