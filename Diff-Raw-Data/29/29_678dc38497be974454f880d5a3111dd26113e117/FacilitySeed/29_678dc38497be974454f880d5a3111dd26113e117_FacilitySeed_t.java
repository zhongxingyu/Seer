 package org.motechproject.ghana.national.tools.seed.data;
 
 import org.motechproject.ghana.national.domain.Facility;
 import org.motechproject.ghana.national.repository.AllFacilities;
 import org.motechproject.ghana.national.tools.seed.Seed;
 import org.motechproject.ghana.national.tools.seed.data.source.FacilitySource;
import org.motechproject.ghana.national.tools.seed.data.source.OldGhanaFacility;
import org.motechproject.mrs.model.MRSFacility;
import org.motechproject.mrs.services.MRSFacilityAdaptor;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
import java.util.ArrayList;
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
    MRSFacilityAdaptor facilityAdaptor;
 
     @Override
     protected void load() {
         try {
            final List<Facility> facilities = facilities();
            List<OldGhanaFacility> oldGhanaFacilities = project(facilities, OldGhanaFacility.class,
                    on(Facility.class).getMrsFacility().getName(), on(Facility.class).getMrsFacility().getId());
             for (OldGhanaFacility oldGhanaFacility : facilitySource.getMotechFacilityNameAndIds()) {
                 String OpenMrsFacilityId = OldGhanaFacility.findByName(oldGhanaFacilities, oldGhanaFacility.getName()).getId();
                 allFacilities.saveLocally(new Facility().mrsFacilityId(OpenMrsFacilityId).motechId(oldGhanaFacility.getId()));
             }
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
     }

    private List<Facility> facilities() {
        final List<MRSFacility> mrsFacilities = facilityAdaptor.getFacilities();
        final ArrayList<Facility> facilities = new ArrayList<Facility>();
        for (MRSFacility mrsFacility : mrsFacilities) {
            Facility facility = new Facility();
            facility.mrsFacility(mrsFacility);
            facilities.add(facility);
        }
        return facilities;
    }
 }
