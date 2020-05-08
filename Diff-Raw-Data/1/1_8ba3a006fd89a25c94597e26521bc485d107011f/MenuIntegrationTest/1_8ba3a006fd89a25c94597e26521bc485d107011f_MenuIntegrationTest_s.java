 package org.youfood.integrationtest;
 
 import org.dbunit.DatabaseUnitException;
 import org.dbunit.database.DatabaseConnection;
 import org.dbunit.database.IDatabaseConnection;
 import org.dbunit.dataset.DataSetException;
 import org.dbunit.dataset.IDataSet;
 import org.dbunit.dataset.xml.FlatXmlDataSet;
 import org.dbunit.operation.DatabaseOperation;
 import org.eclipse.persistence.internal.jpa.EntityManagerImpl;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.youfood.exception.CoreException;
 import org.youfood.model.Menu;
import org.youfood.module.JPAModule;
 import org.youfood.module.TestModule;
 import org.youfood.service.MenuService;
 import org.youfood.utils.GuiceJUnitRunner;
 import org.youfood.utils.JPAControl;
 
 import javax.inject.Inject;
 import javax.persistence.EntityManager;
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.List;
 
 import static org.junit.Assert.*;
 
 /**
  * @author Antoine ROUAZE <antoine.rouaze AT zenika.com>
  */
 @RunWith(GuiceJUnitRunner.class)
 @GuiceJUnitRunner.GuiceModules({TestModule.class})
 public class MenuIntegrationTest {
 
     private static IDataSet dataSet;
 
     @Inject
     private static JPAControl control;
     @Inject
     private MenuService menuService;
     @Inject
     private EntityManager em;
 
     @Test
     public void testAddMenu() {
         Menu menu = new Menu();
         menu.setName("sushi");
         menuService.addMenu(menu);
         Menu result = menuService.getMenuById(menu.getId());
         assertEquals(result, menu);
     }
 
     @Test
     public void testGetMenu() {
         Menu expected = new Menu();
         expected.setId(1000L);
         expected.setName("pizza");
         Menu result = menuService.getMenuById(1000L);
         assertEquals(expected, result);
     }
 
     @Test
     public void testGetListMenu() {
         List<Menu> menus = menuService.getAllMenu();
         assertEquals(menus.size(), 4);
     }
 
     @Test
     public void testUpdateMenu() {
         String updateName = "poulet";
         Menu expected = new Menu();
         expected.setId(1000L);
         expected.setName(updateName);
         Menu original = menuService.getMenuById(1000L);
         assertNotSame(expected, original);
         original.setName(updateName);
         menuService.updateMenu(original);
         Menu result = menuService.getMenuById(1000L);
         assertEquals(expected, result);
     }
 
     @Test
     public void testDeleteMenu() {
         Menu menu = new Menu();
         menu.setId(1000L);
         menuService.removeMenu(menu);
         Menu result = menuService.getMenuById(1000L);
         assertNull(result);
     }
 
     @Before
     public void setUp() {
         try {
             IDatabaseConnection connection = new DatabaseConnection(((EntityManagerImpl) em.getDelegate()).getServerSession().getAccessor().getConnection());
             DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet);
         } catch (DatabaseUnitException e) {
             throw new CoreException("Unable to create connection", e);
         } catch (SQLException e) {
             throw new CoreException("Unable to execute insertion", e);
         }
     }
 
     @BeforeClass
     public static void setUpClass() {
         try {
             dataSet = new FlatXmlDataSet(ClassLoader.getSystemResourceAsStream("insert.xml"));
             control.startJpa();
         } catch (IOException e) {
             throw new CoreException("Unable to read insert.xml DataSet file", e);
         } catch (DataSetException e) {
             throw new CoreException("Unable to read insert.xml DataSet file", e);
         }
     }
 
     @AfterClass
     public static void treadDown() {
         control.stopJpa();
     }
 }
