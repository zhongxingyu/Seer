 package org.motechproject.openmrs.services;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.motechproject.mrs.model.Facility;
 import org.motechproject.mrs.services.MRSFacilityAdapter;
 import org.motechproject.openmrs.OpenMRSTestAuthenticationProvider;
 import org.motechproject.openmrs.security.OpenMRSSession;
 import org.openmrs.api.LocationService;
 import org.openmrs.api.context.Context;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.support.GenericApplicationContext;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import java.util.Arrays;
 import java.util.List;
 import java.util.ResourceBundle;
 
 import static ch.lambdaj.Lambda.having;
 import static ch.lambdaj.Lambda.on;
 import static ch.lambdaj.Lambda.select;
 import static junit.framework.Assert.assertEquals;
 import static junit.framework.Assert.assertNotNull;
 import static org.hamcrest.Matchers.equalTo;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = {"classpath*:applicationOpenmrsAPI.xml"})
 public class OpenMRSFacilityAdapterIT {
     @Autowired
     private GenericApplicationContext applicationContext;
 
     @Autowired
     MRSFacilityAdapter MRSFacilityAdapter;
 
     @Autowired
     LocationService mrsLocationService;
 
     @Autowired
     OpenMRSSession openMRSSession;
 
     @Before
     public void setUp() {
         ResourceBundle resourceBundle = ResourceBundle.getBundle("openmrs");
         OpenMRSTestAuthenticationProvider.login(resourceBundle.getString("openmrs.admin.username"), resourceBundle.getString("openmrs.admin.password"));
         openMRSSession.open();
         openMRSSession.authenticate();
     }
 
     @After
     public void tearDown(){
         openMRSSession.close();
     }
 
     @Test
     public void testSaveLocation() {
         Facility facility = new Facility("my facility", "ghana", "region", "district", "kaseena");
         final Facility savedFacility = MRSFacilityAdapter.saveFacility(facility);
         authorizeAndRollback(new DirtyData() {
             public void rollback() {
                 mrsLocationService.purgeLocation(mrsLocationService.getLocation(Integer.parseInt(savedFacility.getId())));
             }
         });
         assertNotNull(savedFacility);
         assertEquals(facility.getCountry(), savedFacility.getCountry());
         assertEquals(facility.getCountyDistrict(), savedFacility.getCountyDistrict());
         assertEquals(facility.getRegion(), savedFacility.getRegion());
         assertEquals(facility.getStateProvince(), savedFacility.getStateProvince());
         assertEquals(facility.getName(), savedFacility.getName());
     }
 
     @Test
     public void testGetLocations() {
         int size = MRSFacilityAdapter.getFacilities().size();
         String facilityName = "my facility";
         Facility facility = new Facility(facilityName, "ghana", "region", "district", "kaseena");
         final Facility savedFacility = MRSFacilityAdapter.saveFacility(facility);
         List<Facility> facilities = MRSFacilityAdapter.getFacilities();
         authorizeAndRollback(new DirtyData() {
             public void rollback() {
                 mrsLocationService.purgeLocation(mrsLocationService.getLocation(Integer.parseInt(savedFacility.getId())));
             }
         });
         int alteredSize = facilities.size();
         List<Facility> addedFacilities = select(facilities, having(on(Facility.class).getName(), equalTo(facilityName)));
         assertEquals(size + 1, alteredSize);
         assertEquals(Arrays.asList(savedFacility), addedFacilities);
 
     }
 
     @Test
     public void testGetLocationsByName() {
         String facilityName = "my facility";
         Facility facility = new Facility(facilityName, "ghana", "region", "district", "kaseena");
         final Facility savedFacility = MRSFacilityAdapter.saveFacility(facility);
         final List<Facility> facilities = MRSFacilityAdapter.getFacilities(facilityName);
         assertEquals(Arrays.asList(savedFacility), facilities);
 
         authorizeAndRollback(new DirtyData() {
             public void rollback() {
                 mrsLocationService.purgeLocation(mrsLocationService.getLocation(Integer.parseInt(savedFacility.getId())));
             }
         });
     }
 
     private void authorizeAndRollback(DirtyData dirtyData) {
         openMRSSession.open();
         ResourceBundle resourceBundle = ResourceBundle.getBundle("openmrs");
         Context.authenticate(resourceBundle.getString("openmrs.admin.username"), resourceBundle.getString("openmrs.admin.password"));
         dirtyData.rollback();
         openMRSSession.close();
     }
 
     interface DirtyData {
         void rollback();
     }
 }
