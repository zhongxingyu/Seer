 package org.motechproject.ghana.national.repository;
 
 import ch.lambdaj.function.convert.Converter;
 import org.apache.commons.collections.CollectionUtils;
 import org.ektorp.CouchDbConnector;
 import org.ektorp.ViewQuery;
 import org.ektorp.support.View;
 import org.motechproject.dao.MotechAuditableRepository;
 import org.motechproject.ghana.national.domain.Facility;
 import org.motechproject.mrs.model.MRSFacility;
 import org.motechproject.mrs.services.MRSFacilityAdaptor;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.stereotype.Repository;
 
 import java.util.ArrayList;
 import java.util.List;
 
import static ch.lambdaj.Lambda.*;
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
         saveMRSFacility(facility);
         super.add(facility);
     }
 
     @Override
     public void update(Facility facility) {
         Facility existingFacility = findByMrsFacilityId(facility.mrsFacilityId());
         existingFacility.phoneNumber(facility.phoneNumber());
         existingFacility.additionalPhoneNumber1(facility.additionalPhoneNumber1());
         existingFacility.additionalPhoneNumber2(facility.additionalPhoneNumber2());
         existingFacility.additionalPhoneNumber3(facility.additionalPhoneNumber3());
         saveMRSFacility(facility);
         super.update(existingFacility);
     }
 
     private void saveMRSFacility(Facility facility) {
         final MRSFacility savedFacility = facilityAdaptor.saveFacility(facility.mrsFacility());
         facility.mrsFacilityId(savedFacility.getId());
     }
 
 
     public void saveLocally(Facility facility) {
         super.add(facility);
     }
 
     public List<Facility> facilitiesByName(String name) {
         return getFacilitiesWithAllInfo(facilityAdaptor.getFacilities(name));
     }
 
     public List<Facility> facilities() {
         final List<MRSFacility> mrsFacilities = facilityAdaptor.getFacilities();
         final ArrayList<Facility> facilities = new ArrayList<Facility>();
         for (MRSFacility mrsFacility : mrsFacilities) {
             Facility facility = findByMrsFacilityId(mrsFacility.getId());
             if (facility == null)
                 facility = new Facility();
             facility.mrsFacility(mrsFacility);
             facilities.add(facility);
         }
         return facilities;
     }
 
     private List<Facility> getFacilitiesWithAllInfo(List<MRSFacility> mrsFacilities) {
         final List<String> facilityIdsAsString = extract(mrsFacilities, on(MRSFacility.class).getId());
         final List<Facility> facilities = findByFacilityIds(facilityIdsAsString);
 
         return convert(mrsFacilities, new Converter<MRSFacility, Facility>() {
             @Override
             public Facility convert(MRSFacility mrsFacility) {
                 final Facility facility = (Facility) selectUnique(facilities, having(on(Facility.class).mrsFacilityId(),
                         is(mrsFacility.getId())));
                 return (facility != null) ? facility.mrsFacility(mrsFacility) : new Facility(mrsFacility);
             }
         });
     }
 
     public Facility getFacility(String mrsFacilityId) {
         MRSFacility mrsFacility = facilityAdaptor.getFacility(mrsFacilityId);
         return (mrsFacility != null) ? findByMrsFacilityId(mrsFacilityId).mrsFacility(mrsFacility) : null;
     }
 
     @View(name = "find_by_facility_ids", map = "function(doc) { if(doc.type === 'Facility') emit(doc.mrsFacilityId, doc) }")
     public List<Facility> findByFacilityIds(List<String> facilityIds) {
         ViewQuery viewQuery = createQuery("find_by_facility_ids").keys(facilityIds);
         return db.queryView(viewQuery, Facility.class);
     }
 
     @View(name = "find_by_mrs_facility_id", map = "function(doc) { if(doc.type === 'Facility') emit(doc.mrsFacilityId, doc) }")
     public Facility findByMrsFacilityId(String facilityId) {
         ViewQuery viewQuery = createQuery("find_by_mrs_facility_id").key(facilityId).includeDocs(true);
         List<Facility> facilities = db.queryView(viewQuery, Facility.class);
         return CollectionUtils.isEmpty(facilities) ? null : facilities.get(0);
     }
     @View(name = "find_by_motech_facility_id", map = "function(doc) { if(doc.type === 'Facility') emit(doc.motechId, doc) }")
     public Facility findByMotechFacilityId(String facilityId) {
         ViewQuery viewQuery = createQuery("find_by_motech_facility_id").key(facilityId).includeDocs(true);
         List<Facility> facilities = db.queryView(viewQuery, Facility.class);
         return CollectionUtils.isEmpty(facilities) ? null : facilities.get(0);
     }
 
     public Facility getFacilityByMotechId(String motechFacilityId) {
         Facility facility = findByMotechFacilityId(motechFacilityId);
         return (facility != null) ? facility.mrsFacility(facilityAdaptor.getFacility(facility.mrsFacilityId())) : null;
     }
 }
