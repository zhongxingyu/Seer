 package es.rchavarria.raccount.db.dao;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import junit.framework.Assert;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import es.rchavarria.raccount.bussines.util.DateUtils;
 import es.rchavarria.raccount.db.Session;
 import es.rchavarria.raccount.db.dao.populators.MovementPopulator;
 import es.rchavarria.raccount.db.isession.DBSession;
 import es.rchavarria.raccount.db.isession.DataBaseTests;
 import es.rchavarria.raccount.model.Account;
 import es.rchavarria.raccount.model.Concept;
 import es.rchavarria.raccount.model.Movement;
 
 public class MovementDAOTest {
 
     private Session session;
 
     @Before
     public void setUp() throws Exception {
     	session = DBSession.getSession();
         DataBaseTests.setup(session);
     }
 
     @After
     public void tearDown() throws Exception {
     	DataBaseTests.teardown(session);
     	session.close();
     }
 
     @Test
     public void testFind() throws DAOException {
         Movement m = createMovement();
 
         MovementDAO dao = new MovementDAO(session);
         long id = dao.insert(m);
         m.setIdMovement(id);
 
         Movement m2 = dao.find(id);
         dao.delete(m);
 
         Assert.assertEquals(m, m2);
         // assertEqualsMovement(m, m2);
     }
 
     @Test
     public void testUpdate() throws DAOException {
         Movement m = createMovement();
 
         MovementDAO dao = new MovementDAO(session);
         long id = dao.insert(m);
         m.setIdMovement(id);
 
         m.setDescription("new desc");
         dao.update(m);
 
         Movement m2 = dao.find(id);
         dao.delete(m);
 
         Assert.assertEquals(m.getDescription(), m2.getDescription());
     }
 
     @Test
     public void testInsert() throws DAOException {
         Movement m = createMovement();
 
         MovementDAO dao = new MovementDAO(session);
         long id = dao.insert(m);
         m.setIdMovement(id);
         
         int count = dao.count();
         
         dao.delete(m);
 
         assertTrue(id != -1);
         assertEquals(count, 1);
     }
 
     @Test
     public void testListAll() throws Exception {
         MovementDAO dao = new MovementDAO(session);
         new MovementPopulator(session).populate(10);
         
         List<Movement> list = dao.listAll();
         assertFalse(list.isEmpty());
     }
 
     @Test
     public void testCount() throws DAOException {
         MovementDAO dao = new MovementDAO(session);
         Movement m = createMovement();
         long id = dao.insert(m);
         m.setIdMovement(id);
         int nMovements = dao.count();
 
         int nAll = dao.listAll().size();
         dao.delete(m);
 
         Assert.assertEquals(nAll, nMovements);
     }
 
     @Test
     public void testListLastN() throws Exception {
     	new MovementPopulator(session).populate(100);
 
     	MovementDAO dao = new MovementDAO(session);
         AccountDAO aDAO = new AccountDAO(session);
         Account a = aDAO.find(4L);
 
         List<Movement> list = dao.listLastN(a, 5);
         Assert.assertEquals(5, list.size());
         list = dao.listLastN(a, 50);
         Assert.assertEquals(50, list.size());
     }
 
     private Movement createMovement() throws DAOException {
         Movement m = new Movement();
         m.setDescription("TEST description");
         m.setAmount(3.4d);
         m.setFinalBalance(55d);
         Date d = null;
         try {
             d = new SimpleDateFormat("dd/MM/yy").parse("15/05/80");
         } catch (Throwable t) {
             d = new Date();
         }
         m.setMovementDate(d);
         m.setAccount(new AccountDAO(session).listAll().get(0));
         m.setConcept(new ConceptDAO(session).listAll().get(0));
         return m;
     }
     
     @Test
     public void testGetExpensesEqualsZero() throws DAOException {
         Account account = new AccountDAO(session).find(1L);
         Concept concept = new ConceptDAO(session).find(1L);
         int month = new GregorianCalendar().get(Calendar.MONTH) + 1;
         Date start = DateUtils.getFirstDayOfMonth(month);
         Date end = DateUtils.getLastDayOfMonth(month);
         
         MovementDAO dao = new MovementDAO(session);
         double expense = dao.getExpenses(account, concept, start, end);
         
         assertEquals(0.0d, expense, 0.1d);
     }
    
    @Test
    public void testGetExpensesOneMovement() throws DAOException {
        // data for this test
        double amount = 3.4d;
        Concept concept = new ConceptDAO(session).find(1L);
        Account account = new AccountDAO(session).find(1L);
        Movement m = new Movement();
        m.setDescription("TEST description");
        m.setAmount(amount);
        m.setFinalBalance(55d);
        m.setMovementDate(new Date());
        m.setAccount(account);
        m.setConcept(concept);

        // insert movement
        MovementDAO dao = new MovementDAO(session);
        long id = dao.insert(m);
        m.setIdMovement(id);
        
        // get expenses of this month
        int month = new GregorianCalendar().get(Calendar.MONTH) + 1;
        Date start = DateUtils.getFirstDayOfMonth(month);
        Date end = DateUtils.getLastDayOfMonth(month);
        double expenses = dao.getExpenses(account, concept, start, end);
        
        // clean up
        dao.delete(m);
        
        // asserts
        assertEquals(amount, expenses, 0.1d);
    }
 }
