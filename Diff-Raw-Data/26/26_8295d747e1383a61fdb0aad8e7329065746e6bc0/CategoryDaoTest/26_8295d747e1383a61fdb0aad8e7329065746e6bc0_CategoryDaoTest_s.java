 package de.hswt.hrm.component.dao.jdbc;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import java.util.Collection;
 
 import org.junit.Test;
 
 import de.hswt.hrm.catalog.dao.core.ICatalogDao;
 import de.hswt.hrm.catalog.dao.jdbc.CatalogDao;
 import de.hswt.hrm.catalog.model.Catalog;
 import de.hswt.hrm.common.database.exception.DatabaseException;
 import de.hswt.hrm.common.database.exception.ElementNotFoundException;
 import de.hswt.hrm.component.dao.core.ICategoryDao;
 import de.hswt.hrm.component.model.Category;
 import de.hswt.hrm.test.database.AbstractDatabaseTest;
 
 public class CategoryDaoTest extends AbstractDatabaseTest {
 
    private static Catalog getCatalog(final int id) {
        return new Catalog(id, "SomeCatalog");
     }
     
     private static void compareCategoryFields(final Category expected, final Category actual) {
         assertEquals("Catalog not set correctly.", expected.getCatalog().orNull(), actual.getCatalog().orNull());
         assertEquals("Default rating not set correctly.", expected.getDefaultBoolRating(), actual.getDefaultBoolRating());
         assertEquals("Default quantifier not set correctly.", expected.getDefaultQuantifier(), actual.getDefaultQuantifier());
         assertEquals("Height not set correctly.", expected.getHeight(), actual.getHeight());
         assertEquals("Name not set correctly.", expected.getName(), actual.getName());
         assertEquals("Width not set correctly.", expected.getWidth(), actual.getWidth());
     }
     
     @Test
     public void testFindAll() throws ElementNotFoundException, DatabaseException {
         ICatalogDao catalogDao = new CatalogDao();
         ICategoryDao categoryDao = new CategoryDao(catalogDao);
         
         Category cat1 = new Category("Some cat", 4, 4, 1, true);
         Category cat2 = new Category("Some other cat", 2, 4, 1, true);
        Catalog catalog = catalogDao.insert(getCatalog(-1));
         cat2.setCatalog(catalog);
         categoryDao.insert(cat1);
         categoryDao.insert(cat2);
         
         Collection<Category> cats = categoryDao.findAll();
         assertEquals("Not all categories retrieved.", 2, cats.size());
     }
     
     @Test
     public void testInsert() throws ElementNotFoundException, DatabaseException {
         ICatalogDao catalogDao = new CatalogDao();
         ICategoryDao categoryDao = new CategoryDao(catalogDao);
         
        Catalog catalog = catalogDao.insert(getCatalog(-1));
         Category cat = new Category("Some cat", 4, 4, 1, true);
         cat.setCatalog(catalog);
         
         Category parsed = categoryDao.insert(cat);
         compareCategoryFields(cat, parsed);
         assertTrue("ID not set correctly.", parsed.getId() >= 0);
         
         Category fromDb = categoryDao.findById(parsed.getId());
         compareCategoryFields(cat, fromDb);
         assertEquals("ID not set correctly.", parsed.getId(), fromDb.getId());
     }
     
     @Test
     public void testInsertWithNewCatalog() throws ElementNotFoundException, DatabaseException {
         ICatalogDao catalogDao = new CatalogDao();
         ICategoryDao categoryDao = new CategoryDao(catalogDao);
         
         Category cat = new Category("Some cat", 4, 4, 1, true);
        Catalog newCatalog = getCatalog(-1);
         cat.setCatalog(newCatalog);
         
         Category parsed = categoryDao.insert(cat);
         Category fromDb = categoryDao.findById(parsed.getId());
         
         Catalog actual = fromDb.getCatalog().get();
         assertTrue("Catalog not inserted correctly.", actual.getId() >= 0);
         assertEquals("Catalog not inserted correctly.", newCatalog.getName(), actual.getName());
     }
     
     @Test
     public void testUpdate() throws ElementNotFoundException, DatabaseException {
         ICatalogDao catalogDao = new CatalogDao();
         ICategoryDao categoryDao = new CategoryDao(catalogDao);
         
         Category cat = new Category("Some cat", 4, 4, 1, true);
        Catalog catalog = catalogDao.insert(getCatalog(-1));
         Category parsed = categoryDao.insert(cat);
 
        Catalog changedCatalog = getCatalog(-1);;
         changedCatalog.setName("Changed");
         parsed.setCatalog(changedCatalog);
         parsed.setHeight(2);
         parsed.setName("Updated name");
         categoryDao.update(parsed);
         
         Category fromDb = categoryDao.findById(parsed.getId());
         compareCategoryFields(parsed, fromDb);
         assertEquals("ID not set correctly.", parsed.getId(), fromDb.getId());
     }
 }
