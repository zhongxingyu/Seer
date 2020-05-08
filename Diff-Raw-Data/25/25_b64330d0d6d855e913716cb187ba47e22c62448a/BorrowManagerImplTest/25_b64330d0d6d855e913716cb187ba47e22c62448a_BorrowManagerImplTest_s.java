 package cz.muni.fi.pv168;
 
 import org.junit.After;
 import javax.sql.DataSource;
 import javax.naming.NamingException;
 import java.sql.SQLException;
 import java.util.GregorianCalendar;
 import java.util.Calendar;
 import org.junit.Before;
 import org.junit.Test;
 
 import static org.junit.Assert.*;
 
 /**
  * Created by IntelliJ IDEA.
  * User: fivekeyem, janinko
  * Date: 3/14/11
  */
 public class BorrowManagerImplTest {
 
     private BorrowManager manager;
     private CDManager cdManager;
     private CustomerManager customerManager;
     private DataSource ds;
 
     @Before
     public void setUp() throws SQLException, NamingException  {
         ds = HelperDB.prepareDataSourceTest();
         HelperDB.createTables(ds);
         cdManager = new CDManagerImpl();
         cdManager.setDs(ds);
         customerManager = new CustomerManagerImpl();
         customerManager.setDs(ds);
         manager = new BorrowManagerImpl(cdManager,customerManager);
         manager.setDs(ds);
     }
 
     @After
     public void tearDown() throws SQLException {
         HelperDB.dropTables(ds);
     }
 
     @Test
     public void testCreateBorrow() {
         Borrow borrow = createSampleBorrow();
 
         // should be the same
        borrow.setId(0);
         manager.createBorrow(borrow);
         int borrowId = borrow.getId();
 
         Borrow managerBorrow = manager.getBorrowById(borrowId);
         
         assertTrue(borrow.equals(managerBorrow));
         
 
         // should be the same
         borrow = createSampleBorrow2();
        borrow.setId(0);
         manager.createBorrow(borrow);
         borrowId = borrow.getId();
 
         managerBorrow = manager.getBorrowById(borrowId);
         
         assertEquals(borrow, managerBorrow);
 
 
         // when we try to create null borrow, we should get an exception
         try {
             manager.createBorrow(null);
             fail();
         } catch (NullPointerException ex) {}
         catch (Exception ex) {
             fail();
         }
 
         // when we try to create a borrow with null CD, we should get an exception
         try {
             borrow = createSampleBorrow();
             borrow.setCd(null);
             manager.createBorrow(borrow);
             fail();
        } catch (IllegalArgumentException ex) {}
         catch (Exception ex) {
             fail();
         }
 
         // when we try to create a borrow with null customer, we should get an exception
         try {
             borrow = createSampleBorrow();
             borrow.setCustomer(null);
             manager.createBorrow(borrow);
             fail();
        } catch (IllegalArgumentException ex) {}
         catch (Exception ex) {
             fail();
         }
 
         // when we try to create a borrow with set id, we should get an exception
         try {
             borrow = createSampleBorrow();
             borrow.setId(5);
             manager.createBorrow(borrow);
             fail();
         } catch (IllegalArgumentException ex) {}
         catch (Exception ex) {
             fail();
         }
 
 
     }
 
     @Test
     public void testDeleteBorrow() {
         Borrow borrow1 = createSampleBorrow();
         Borrow borrow2 = createSampleBorrow2();
 
         manager.createBorrow(borrow1);
         manager.createBorrow(borrow2);
 
         Borrow managerBorrow1 = manager.getBorrowById(borrow1.getId());
         Borrow managerBorrow2 = manager.getBorrowById(borrow2.getId());
 
         assertEquals(borrow1, manager.deleteBorrow(managerBorrow1));
         assertEquals(borrow2, manager.deleteBorrow(managerBorrow2));
 
         // when we try to delete null borrow, we should get an exception
         try {
             manager.deleteBorrow(null);
             fail();
         } catch (NullPointerException ex) {}
         catch (Exception ex) {
             fail();
         }
 
         // when we try to delete a nonexistent borrow, we should get an exception
         try {
             manager.deleteBorrow(borrow1);
             fail();
         } catch (IllegalArgumentException ex) {}
         catch (Exception ex) {
             fail();
         }
     }
 
     @Test
     public void testUpdateBorrow() {
         Borrow borrow = createSampleBorrow();
         manager.createBorrow(borrow);
         Borrow managerBorrow = manager.getBorrowById(borrow.getId());
 
         // borrows should be the same
         borrow.setCd(new CD(2, "The Test Album 2", 2011));
         borrow.setActive(false);
         manager.updateBorrow(borrow);
         assertEquals(borrow, managerBorrow);
 
         // attributes should be the same
         assertEquals(borrow.getCd(), managerBorrow.getCd());
         assertEquals(borrow.getCustomer(), managerBorrow.getCustomer());
         assertTrue(borrow.getActive() == managerBorrow.getActive());
 
 
     }
 
     @Test
     public void testGetAllBorrows() {
 
     }
 
     @Test
     public void testGetBorrowByID() {
 
     }
 
 
     private Borrow createSampleBorrow() {
         Borrow borrow = new Borrow();
 
        borrow.setId(1);
         CD cd = new CD(0, "The Test Album", 2011);
         cd = cdManager.createCD(cd);
         borrow.setCd(cd);
         Customer customer = new Customer(0, "Test User");
         customer = customerManager.createCustomer(customer);
         borrow.setCustomer(customer);
        // Calendar from = new GregorianCalendar(2011, 3, 5, 12, 30, 7) ;    TODO
        // Calendar to = new GregorianCalendar(2011, 4, 4, 16, 19, 3) ;
        // borrow.setFrom(from);
        // borrow.setTo(to);
         borrow.setActive(false);
 
         return borrow;
     }
 
 
     private Borrow createSampleBorrow2() {
         Borrow borrow = new Borrow();
 
        borrow.setId(2);
         CD cd = new CD(0, "The Test Album 2", 2011);
         cd = cdManager.createCD(cd);
         borrow.setCd(cd);
         Customer customer = new Customer(0, "Test User 2");
         customer = customerManager.createCustomer(customer);
         borrow.setCustomer(customer);
         //Calendar from = new GregorianCalendar(2011, 4, 7, 12, 30, 7) ; TODO
         //Calendar to = new GregorianCalendar(2011, 5, 6, 18, 0, 0) ;
         //borrow.setFrom(from);
         //borrow.setTo(to);
         borrow.setActive(true);
 
         return borrow;
     }
 
 
 }
