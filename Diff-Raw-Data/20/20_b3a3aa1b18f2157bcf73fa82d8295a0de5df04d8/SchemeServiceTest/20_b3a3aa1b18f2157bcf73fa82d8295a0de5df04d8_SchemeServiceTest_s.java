 package de.hswt.hrm.scheme.service.test;
 
 import static org.mockito.Mockito.*;
 import static org.junit.Assert.*;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 
 import org.junit.Test;
 
 import de.hswt.hrm.catalog.dao.core.ICatalogDao;
 import de.hswt.hrm.common.database.exception.DatabaseException;
 import de.hswt.hrm.common.database.exception.ElementNotFoundException;
 import de.hswt.hrm.common.database.exception.SaveException;
 import de.hswt.hrm.component.dao.core.ICategoryDao;
 import de.hswt.hrm.component.dao.core.IComponentDao;
 import de.hswt.hrm.component.dao.jdbc.CategoryDao;
 import de.hswt.hrm.component.dao.jdbc.ComponentDao;
 import de.hswt.hrm.component.model.Attribute;
 import de.hswt.hrm.component.model.Category;
 import de.hswt.hrm.component.model.Component;
 import de.hswt.hrm.place.dao.core.IPlaceDao;
 import de.hswt.hrm.place.dao.jdbc.PlaceDao;
 import de.hswt.hrm.place.model.Place;
 import de.hswt.hrm.plant.dao.core.IPlantDao;
 import de.hswt.hrm.plant.dao.jdbc.PlantDao;
 import de.hswt.hrm.plant.model.Plant;
 import de.hswt.hrm.scheme.dao.core.ISchemeComponentDao;
 import de.hswt.hrm.scheme.dao.core.ISchemeDao;
 import de.hswt.hrm.scheme.dao.jdbc.SchemeComponentDao;
 import de.hswt.hrm.scheme.dao.jdbc.SchemeDao;
 import de.hswt.hrm.scheme.model.Direction;
 import de.hswt.hrm.scheme.model.Scheme;
 import de.hswt.hrm.scheme.model.SchemeComponent;
 import de.hswt.hrm.scheme.service.SchemeService;
 import de.hswt.hrm.test.database.AbstractDatabaseTest;
 
 public class SchemeServiceTest extends AbstractDatabaseTest {
 
     private ICategoryDao createCategoryDao() {
         ICatalogDao catalogDao = mock(ICatalogDao.class);
         return new CategoryDao(catalogDao);
     }
 
     private IPlantDao createPlantDao() {
         IPlaceDao placeDao = new PlaceDao();
         return new PlantDao(placeDao);
     }
 
     private SchemeService createSchemeService() {
         return new SchemeService(createSchemeDao(), createSchemeComponentDao());
     }
 
     private ISchemeDao createSchemeDao() {
         return new SchemeDao(createPlantDao());
     }
 
     private ISchemeComponentDao createSchemeComponentDao() {
         IComponentDao componentDao = new ComponentDao(createCategoryDao());
         ISchemeDao schemeDao = createSchemeDao();
         return new SchemeComponentDao(schemeDao, componentDao);
     }
 
     private Plant createTestPlant() throws SaveException {
         return createTestPlant("Some plant", "Somewhere");
     }
 
     private Plant createTestPlant(final String name, final String placeName) throws SaveException {
         IPlantDao plantDao = createPlantDao();
         // FIXME Added two Strings to plant - untested
         Plant plant = new Plant(name, "Schwimmhalle", "Keller");
         plant.setPlace(new Place(placeName, "55555", "City", "Street", "15"));
         return plantDao.insert(plant);
     }
 
     private Component createTestComponent() throws SaveException {
         ICategoryDao categoryDao = createCategoryDao();
         IComponentDao componentDao = new ComponentDao(categoryDao);
         Category cat = categoryDao.insert(new Category("SomeCat", 1, 1, 1, true));
 
         Component component = new Component("SomeComp", new byte[1], new byte[1], new byte[1],
                 new byte[1], 1, true);
         component.setCategory(cat);
         component = componentDao.insert(component);
 
         return component;
     }
 
     private List<Attribute> createTestAttributes() throws SaveException {
         ICategoryDao categoryDao = createCategoryDao();
         IComponentDao componentDao = new ComponentDao(categoryDao);
 
         Component component = createTestComponent();
 
         List<Attribute> attributes = new ArrayList<>();
         attributes.add(componentDao.addAttribute(component, "FirstAttribute"));
         attributes.add(componentDao.addAttribute(component, "SecondAttribute"));
 
         return attributes;
     }
 
     @Test
     public void testInsertScheme() throws SaveException {
         Plant plant = createTestPlant();
         ISchemeDao schemeDao = createSchemeDao();
 
         Scheme scheme = new Scheme(plant);
         schemeDao.insert(scheme);
 
         // TODO: check if scheme can be resolved correctly
     }
 
     @Test
     public void testSetAttributes() throws DatabaseException {
         List<Attribute> attributes = createTestAttributes();
         ISchemeDao schemeDao = createSchemeDao();
         ISchemeComponentDao schemeComponentDao = createSchemeComponentDao();
         Scheme scheme = new Scheme(createTestPlant());
         scheme = schemeDao.insert(scheme);
         SchemeComponent schemeComp = new SchemeComponent(scheme, 0, 0, Direction.leftRight,
                 attributes.get(0).getComponent());
 
        // FIXME: again wrong usage of the API possible -> scheme should be mandatory
        schemeComp.setScheme(scheme);
         schemeComp = schemeComponentDao.insert(schemeComp);
 
         Attribute attr = attributes.get(0);
         schemeComponentDao.setAttributeValue(schemeComp, attr, "Some value");
         attr = attributes.get(1);
         schemeComponentDao.setAttributeValue(schemeComp, attr, "Some other value");
     }
 
     @Test
     public void testPlantUpdateScheme() throws ElementNotFoundException, DatabaseException {
         ISchemeDao schemeDao = createSchemeDao();
         Scheme scheme = new Scheme(createTestPlant());
         Scheme parsed = schemeDao.insert(scheme);
 
         parsed.setPlant(createTestPlant("New Plant", "Somewhere else"));
         schemeDao.update(parsed);
 
         Scheme fromDb = schemeDao.findById(parsed.getId());
         assertEquals("Plant not updated.", parsed.getPlant().get().getId(), fromDb.getPlant().get()
                 .getId());
     }
 
     @Test
     public void testFindCurrentSchemeByPlant() throws ElementNotFoundException, DatabaseException {
         ISchemeDao schemeDao = createSchemeDao();
         Plant plant = createTestPlant();
         Scheme firstScheme = new Scheme(plant);
         schemeDao.insert(firstScheme);
         Scheme secondScheme = new Scheme(plant);
         Scheme parsed = schemeDao.insert(secondScheme);
 
         Scheme fromDb = schemeDao.findCurrentSchemeByPlant(plant);
         assertEquals("Not the last inserted scheme returned.", parsed.getId(), fromDb.getId());
         assertNotNull("Timestamp not set correctly.", fromDb.getTimestamp());
     }
 
     @Test
     public void testCopyScheme() throws ElementNotFoundException, DatabaseException {
         Component component = createTestComponent();
         ISchemeDao schemeDao = createSchemeDao();
         SchemeService service = createSchemeService();
         ISchemeComponentDao schemeComponentDao = createSchemeComponentDao();
         Scheme scheme = new Scheme(createTestPlant());
         scheme = schemeDao.insert(scheme);
         SchemeComponent schemeComp = new SchemeComponent(scheme, 0, 0, Direction.leftRight,
                 component);
         schemeComp = schemeComponentDao.insert(schemeComp);
         SchemeComponent comp2 = new SchemeComponent(scheme, 10, 10, Direction.leftRight, component);
         comp2.setScheme(scheme);
         schemeComponentDao.insert(comp2);
 
         Scheme copy = service.copy(scheme);
         assertEquals(2, copy.getSchemeComponents().size());
         assertTrue("Scheme not copied to new scheme.", copy.getId() != scheme.getId());
     }
 
     @Test
     public void testUpdateScheme() throws ElementNotFoundException, DatabaseException {
         Component component = createTestComponent();
         ISchemeDao schemeDao = createSchemeDao();
         SchemeService service = createSchemeService();
         ISchemeComponentDao schemeComponentDao = createSchemeComponentDao();
         Scheme scheme = new Scheme(createTestPlant());
         scheme = schemeDao.insert(scheme);
         SchemeComponent schemeComp = new SchemeComponent(scheme, 0, 0, Direction.leftRight,
                component);
        schemeComp.setScheme(scheme);
         schemeComp = schemeComponentDao.insert(schemeComp);
         SchemeComponent comp2 = new SchemeComponent(scheme, 10, 10, Direction.leftRight, component);
        comp2.setScheme(scheme);
         schemeComponentDao.insert(comp2);
 
         Collection<SchemeComponent> comps = service.findSchemeComponents(scheme);
         Iterator<SchemeComponent> it = comps.iterator();
         it.next();
         it.remove();
 
         service.update(scheme, comps);
 
         Scheme fromDB = service.findById(scheme.getId());
         assertEquals(1, fromDB.getSchemeComponents().size());
     }
 }
