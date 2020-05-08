 package de.hswt.hrm.plant.service;
 
 import static org.mockito.Mockito.*;
 
 import java.util.Collection;
 
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 import de.hswt.hrm.common.database.exception.DatabaseException;
 import de.hswt.hrm.common.database.exception.ElementNotFoundException;
 import de.hswt.hrm.place.dao.core.IPlaceDao;
import de.hswt.hrm.place.dao.jdbc.PlaceDao;
 import de.hswt.hrm.place.model.Place;
 import de.hswt.hrm.plant.dao.core.IPlantDao;
 import de.hswt.hrm.plant.dao.jdbc.PlantDao;
 import de.hswt.hrm.plant.model.Plant;
 import de.hswt.hrm.test.database.AbstractDatabaseTest;
 
 public class PlantServiceTest extends AbstractDatabaseTest {
     // FIXME: handle dependencies correctly!
     
     private Place createPlace() {
         Place place = new Place("Somewhere", "11123", "SimCity", "Skywalkerway 5", "110a",
                 "Todesstern", "Laser");
         return place;
     }
     
     private void comparePlantFields(final Plant expected, final Plant actual) {
         assertEquals("AirPerformance not set correclty.", 
                 expected.getAirPerformance().orNull(), expected.getAirPerformance().orNull());
         assertEquals("ConstructionYear not set correctly.",
                 expected.getConstructionYear().orNull(), expected.getConstructionYear().orNull());
         assertEquals("Current not set correclty.",
                 expected.getCurrent().orNull(), expected.getCurrent().orNull());
         assertEquals("Description not set correctly.",
                 expected.getDescription(), expected.getDescription());
         assertEquals("InspectionInterval not set correctly.",
                 expected.getInspectionInterval(), expected.getInspectionInterval());
         assertEquals("Manufactor not set correctly.",
                 expected.getManufactor().orNull(), expected.getManufactor().orNull());
         assertEquals("MotorPower not set correctly.",
                 expected.getMotorPower().orNull(), expected.getMotorPower().orNull());
         assertEquals("MotorRpm not set correctly.",
                 expected.getMotorRpm().orNull(), expected.getMotorRpm().orNull());
         assertEquals("Note not set correctly.", 
                 expected.getNote().orNull(), expected.getNote().orNull());
         assertEquals("NumberOfElements not set correctly.", 
                 expected.getNumberOfElements(), expected.getNumberOfElements());
         assertEquals("Place not set correctly.", 
                 expected.getPlace().orNull(), expected.getPlace().orNull());
         assertEquals("Type not set correctly.", 
                 expected.getType().orNull(), expected.getType().orNull());
         assertEquals("VentilatorPerformance not set correctly.", 
                 expected.getVentilatorPerformance().orNull(), expected.getVentilatorPerformance().orNull());
         assertEquals("Voltage not set correctly.", 
                 expected.getVoltage().orNull(), expected.getVoltage().orNull());
     }
     
     @Test
     public void testFindAll() throws DatabaseException {
        IPlaceDao placeDao = new PlaceDao();
         IPlantDao plantDao = new PlantDao(placeDao);
         PlantService plantService = new PlantService(plantDao);
         
         Plant plant1 = new Plant(6, "Test plant");
         plant1.setPlace(createPlace());
         Plant plant2 = new Plant(12, "Another test plant");
         plant2.setPlace(createPlace());
         plantService.insert(plant1);
         plantService.insert(plant2);
         
         Collection<Plant> plants = plantService.findAll();
         assertEquals("Count of retrieved plants does not match", 2, plants.size());
     }
     
     @Test
     public void testFindById() throws ElementNotFoundException, DatabaseException {
        IPlaceDao placeDao = new PlaceDao();
         IPlantDao plantDao = new PlantDao(placeDao);
         PlantService plantService = new PlantService(plantDao);
         
         Plant expected = new Plant(12, "Another test plant");
         expected.setPlace(createPlace());
         Plant parsed = plantService.insert(expected);
         
         Plant retrieved = plantService.findById(parsed.getId());
         assertEquals("ID not set correctly.", parsed.getId(), retrieved.getId());
         comparePlantFields(expected, retrieved);
     }
     
     @Test
     public void testInsert() throws ElementNotFoundException, DatabaseException {
        IPlaceDao placeDao = new PlaceDao();
         IPlantDao plantDao = new PlantDao(placeDao);
         PlantService plantService = new PlantService(plantDao);
         
         Place place = createPlace();
         Plant expected = new Plant(12, "Another test plant");
         expected.setAirPerformance("Best performance ever!");
         expected.setConstructionYear(2012);
         expected.setCurrent("12 A");
         expected.setManufactor("ACME Labs");
         expected.setMotorPower("30 PS");
         expected.setMotorRpm("1500 RPM");
         expected.setNote("Remember, remember the 5th of November..");
         expected.setNumberOfElements(10);
         expected.setPlace(place);
         expected.setType("Don't know");
         expected.setVentilatorPerformance("Best performance ever!");
         expected.setVoltage("220 V");
         Plant parsed = plantService.insert(expected);
         
         comparePlantFields(expected, parsed);
         Plant retrieved = plantService.findById(parsed.getId());
         comparePlantFields(expected, retrieved);
     }
     
     @Test
     public void testUpdate() throws ElementNotFoundException, DatabaseException {
        IPlaceDao placeDao = new PlaceDao();
         IPlantDao plantDao = new PlantDao(placeDao);
         PlantService plantService = new PlantService(plantDao);
         
         Plant expected = new Plant(12, "Another test plant");
         expected.setPlace(createPlace());
         Plant parsed = plantService.insert(expected);
         
         parsed.setAirPerformance("Best performance ever!");
         plantService.update(parsed);
         
         Plant retrieved = plantService.findById(parsed.getId());
         comparePlantFields(parsed, retrieved);
     }
 }
