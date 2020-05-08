 package org.motechproject.ghana.national.repository;
 
 import ch.lambdaj.function.convert.Converter;
 import org.ektorp.CouchDbConnector;
 import org.ektorp.ViewQuery;
 import org.ektorp.support.View;
 import org.motechproject.dao.MotechAuditableRepository;
 import org.motechproject.ghana.national.domain.Facility;
 import org.motechproject.ghana.national.tools.Utility;
 import org.motechproject.mrs.services.MRSFacilityAdaptor;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.stereotype.Repository;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import static ch.lambdaj.Lambda.convert;
 import static ch.lambdaj.Lambda.extract;
 import static ch.lambdaj.Lambda.having;
 import static ch.lambdaj.Lambda.on;
 import static ch.lambdaj.Lambda.selectUnique;
 import static org.hamcrest.core.Is.is;
 
 @Repository
 public class AllFacilities extends MotechAuditableRepository<Facility> {
     private MRSFacilityAdaptor facilityAdaptor;
 
     @Autowired
     public AllFacilities(@Qualifier("couchDbConnector") CouchDbConnector db, MRSFacilityAdaptor facilityAdaptor) {
         super(Facility.class, db);
         this.facilityAdaptor = facilityAdaptor;
     }
 
     @Override
     public void add(Facility facility) {
         final org.motechproject.mrs.model.Facility savedFacility = facilityAdaptor.saveFacility(facility.mrsFacility());
         facility.mrsFacilityId(Integer.parseInt(savedFacility.getId()));
         super.add(facility);
     }
 
     public List<Facility> facilitiesByName(String name) {
         return getFacilitiesWithAllinfo(facilityAdaptor.getFacilities(name));
     }
 
     public List<Facility> facilities() {
         final List<org.motechproject.mrs.model.Facility> mrsFacilities = facilityAdaptor.getFacilities();
         final ArrayList<Facility> facilities = new ArrayList<Facility>();
         for (org.motechproject.mrs.model.Facility mrsFacility : mrsFacilities) {
             final Facility facility = new Facility(mrsFacility);
             facilities.add(facility);
         }
         return facilities;
     }
 
     private List<Facility> getFacilitiesWithAllinfo(List<org.motechproject.mrs.model.Facility> mrsFacilities) {
         final List<String> facilityIdsAsString = extract(mrsFacilities, on(org.motechproject.mrs.model.Facility.class).getId());
         final List<Facility> facilities = findByFacilityIds(convert(facilityIdsAsString, Utility.stringToIntegerConverter()));
 
         return convert(mrsFacilities, new Converter<org.motechproject.mrs.model.Facility, Facility>() {
             @Override
             public Facility convert(org.motechproject.mrs.model.Facility mrsFacility) {
                 final Facility facility = (Facility) selectUnique(facilities, having(on(Facility.class).mrsFacilityId(),
                         is(Integer.valueOf(mrsFacility.getId()))));
                return facility.mrsFacility(mrsFacility);

             }
         });
     }
 
     @View(name = "find_by_facility_id", map = "function(doc) { if(doc.type === 'Facility') emit(doc.mrsFacilityId, doc) }")
     public List<Facility> findByFacilityIds(List<Integer> facilityIds) {
         ViewQuery viewQuery = createQuery("find_by_facility_id").keys(facilityIds);
         return db.queryView(viewQuery, Facility.class);
     }
 }
