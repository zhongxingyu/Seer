 package domainControllerTests;
 
 import Hibernate.NewHibernateUtil;
 import Strategies.IPreuStrategy;
 import Strategies.PreuAmbDescompte;
 import Strategies.PreuAmbDescompteId;
 import domainControllers.trBuscarHabitacions;
 import domainModel.*;
 import java.util.ArrayList;
 import java.util.Date;
 import org.hibernate.HibernateException;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import static org.junit.Assert.fail;
 import org.junit.*;
 import tupleTypes.HotelAmbHabitacions;
 
 public class trBuscarHabitacionsTest {
     
     public trBuscarHabitacionsTest() {
     }
 
     @BeforeClass
     public static void setUpClass() throws Exception {
         
     }
 
     @AfterClass
     public static void tearDownClass() throws Exception {
     }
     
     @Before
     public void setUp() {
     }
     
     @Test
     public void testBuscarHabitacionsWithNoLocationsShouldThrowAnException() {
         clearDatabase();
         trBuscarHabitacions tx = new trBuscarHabitacions("Manresa", new Date(100), new Date(101),3);
         try {
             tx.executa();
             fail();
         } catch (Exception ex) {
             ex.printStackTrace();
             Assert.assertEquals("No existeix aquesta poblacio", ex.getMessage());
         }
     }
     
     @Test
     public void testBuscarHabitacionsWithNoHotelsWithRoomsAvailableShouldThrowAnException() {
         clearDatabase();
         insertLocation();
         trBuscarHabitacions tx = new trBuscarHabitacions("Olesa", new Date(100), new Date(101),3);
         try {
             tx.executa();
             fail();
         } catch (Exception ex) {
             ex.printStackTrace();
             Assert.assertEquals("hotelsNoDisp", ex.getMessage());
         }
         deleteLocation();
     }
     
     @Test
     public void testBuscarHabitacionsWithAHotelWithOneRoomAvailableShouldReturnOneAvailableRoom() {
         
         insertLocationWithOneHotelAndOneRoom();
         trBuscarHabitacions tx = new trBuscarHabitacions("Manresa", new Date(100), new Date(101),3);
         try {
             tx.executa();
             ArrayList<HotelAmbHabitacions> hAmbHab = tx.obtenirResultat();
             Assert.assertEquals(hAmbHab.size(), 1);
         } catch (Exception ex) {
             ex.printStackTrace();
             fail();
         }
     }
     
     @Test
     public void testBuscarHabitacionsWithAHotelWithOneRoomWithABlockingBookingAvailableShouldReturnNoAvailableRooms() {
         
         clearDatabase();
         
         insertLocationWithOneHotelAndOneRoomWithABlockingBooking();
         trBuscarHabitacions tx = new trBuscarHabitacions("Milan", new Date(100), new Date(103),3);
         try {
             tx.executa();
             ArrayList<HotelAmbHabitacions> hAmbHab = tx.obtenirResultat();
             fail();
         } catch (Exception ex) {
             ex.printStackTrace();
             Assert.assertEquals(ex.getMessage(), "hotelsNoDisp");
         }
     }
     
     @Test
     public void testBuscarHabitacionsWithAHotelWithOneRoomWithANonBlockingBookingAvailableShouldReturnOneAvailableRoom() {
         
         clearDatabase();
         
         insertLocationWithOneHotelAndOneRoomWithANonBlockingBooking();
         trBuscarHabitacions tx = new trBuscarHabitacions("Barcelona", new Date(102), new Date(103),3);
         try {
             tx.executa();
             ArrayList<HotelAmbHabitacions> hAmbHab = tx.obtenirResultat();
             Assert.assertEquals(hAmbHab.size(), 1);
         } catch (Exception ex) {
             ex.printStackTrace();
             fail();
         }
     }
 
     
     @Test
     public void testBuscarHabitacionsWithAHotelWithThreeRoomsWithANonBlockingBookingShouldReturnThreeAvailableRooms() {
         
         clearDatabase();
         
         insertLocationWithOneHotelAndThreeRoomsWithANonBlockingBooking();
         trBuscarHabitacions tx = new trBuscarHabitacions("Paris", new Date(200), new Date(203),3);
         try {
             tx.executa();
             ArrayList<HotelAmbHabitacions> hAmbHab = tx.obtenirResultat();
             Assert.assertEquals(1, hAmbHab.size());
             Assert.assertEquals(3, hAmbHab.get(0).habitacions.get(0).numeroDisponibles);
         } catch (Exception ex) {
             ex.printStackTrace();
             fail();
         }
        clearDatabase();
     }
     
     
     private void deleteLocation() {
         
         Session session = NewHibernateUtil.getSessionFactory().getCurrentSession();
         session.beginTransaction();
         Query query = session.createQuery("delete from Poblacio");
         query.executeUpdate();
     }
 
     private void insertLocation() throws HibernateException {
         Session session = NewHibernateUtil.getSessionFactory().getCurrentSession();
         session.beginTransaction();
         
         Poblacio p = new Poblacio("Olesa");
         session.merge(p);
         session.getTransaction().commit();
     }
     
     private void insertLocationWithOneHotelAndOneRoom() {
         Session session = NewHibernateUtil.getSessionFactory().getCurrentSession();
         session.beginTransaction();
         
         Poblacio p = new Poblacio("Manresa");
         Categoria c = new Categoria("Categoria1");
         Hotel h = new Hotel("Hotel", "desc", "Categoria1", "Manresa");
         HabitacioId hId = new HabitacioId("Hotel", 1);
         TipusHabitacio tipusHab = new TipusHabitacio("Suite", 3, "descHabitacio");
         IPreuStrategy descompte = new PreuAmbDescompte(new PreuTipusHabitacioId("Hotel", "Suite"), 300f);
         PreuTipusHabitacio preuTipusHab = new PreuTipusHabitacio(new PreuTipusHabitacioId("Hotel", "Suite"), 1000f, descompte);
         Habitacio hab = new Habitacio(hId, "Suite");
 
         session.saveOrUpdate(p);
         session.saveOrUpdate(c);
         session.saveOrUpdate(h);
         session.saveOrUpdate(tipusHab);
         session.saveOrUpdate(preuTipusHab);
         session.saveOrUpdate(descompte);
         session.saveOrUpdate(hab);
         session.getTransaction().commit();
     }
     
     private void insertLocationWithOneHotelAndOneRoomWithABlockingBooking() {
         Session session = NewHibernateUtil.getSessionFactory().getCurrentSession();
         session.beginTransaction();
         
         Poblacio p = new Poblacio("Milan");
         Categoria c = new Categoria("Categoria1");
         Hotel h = new Hotel("Hotel3", "desc", "Categoria1", "Milan");
         HabitacioId hId = new HabitacioId("Hotel3", 3);
         TipusHabitacio tipusHab = new TipusHabitacio("Suite3", 3, "descHabitacio");
         IPreuStrategy descompte = new PreuAmbDescompte(new PreuTipusHabitacioId("Hotel3", "Suite3"), 300f);
         PreuTipusHabitacio preuTipusHab = new PreuTipusHabitacio(new PreuTipusHabitacioId("Hotel3", "Suite3"), 1000f, descompte);
         Habitacio hab = new Habitacio(hId, "Suite3");
         Reserva res = new Reserva(new ReservaId("Hotel3", 3, new Date(100)), new Date(100), new Date(101), 1000d);
         
         session.saveOrUpdate(p);
         session.saveOrUpdate(c);
         session.saveOrUpdate(h);
         session.saveOrUpdate(tipusHab);
         session.saveOrUpdate(preuTipusHab);
         session.saveOrUpdate(descompte);
         session.saveOrUpdate(hab);
         session.saveOrUpdate(res);
         session.getTransaction().commit();
     }
     
     private void insertLocationWithOneHotelAndOneRoomWithANonBlockingBooking() {
         Session session = NewHibernateUtil.getSessionFactory().getCurrentSession();
         session.beginTransaction();
         
         Poblacio p = new Poblacio("Barcelona");
         Categoria c = new Categoria("Categoria1");
         Hotel h = new Hotel("Hotel2", "desc", "Categoria1", "Barcelona");
         HabitacioId hId = new HabitacioId("Hotel2", 2);
         TipusHabitacio tipusHab = new TipusHabitacio("Suite2", 3, "descHabitacio");
         IPreuStrategy descompte = new PreuAmbDescompte(new PreuTipusHabitacioId("Hotel2", "Suite2"), 300f);
         PreuTipusHabitacio preuTipusHab = new PreuTipusHabitacio(new PreuTipusHabitacioId("Hotel2", "Suite2"), 1000f, descompte);
         Habitacio hab = new Habitacio(hId, "Suite2");
         Reserva res = new Reserva(new ReservaId("Hotel2", 2, new Date(100)), new Date(100), new Date(101), 1000d);
         
         session.saveOrUpdate(p);
         session.saveOrUpdate(c);
         session.saveOrUpdate(h);
         session.saveOrUpdate(tipusHab);
         session.saveOrUpdate(preuTipusHab);
         session.saveOrUpdate(descompte);
         session.saveOrUpdate(hab);
         session.saveOrUpdate(res);
         session.getTransaction().commit();
     }
     
     private void insertLocationWithOneHotelAndThreeRoomsWithANonBlockingBooking() {
         Session session = NewHibernateUtil.getSessionFactory().getCurrentSession();
         session.beginTransaction();
         
         Poblacio p = new Poblacio("Paris");
         Categoria c = new Categoria("Categoria1");
         Hotel h = new Hotel("Hotel4", "desc", "Categoria1", "Paris");
         HabitacioId hId = new HabitacioId("Hotel4", 2);
         TipusHabitacio tipusHab = new TipusHabitacio("Suite3", 3, "descHabitacio");
         IPreuStrategy descompte = new PreuAmbDescompte(new PreuTipusHabitacioId("Hotel4", "Suite3"), 300f);
         PreuTipusHabitacio preuTipusHab = new PreuTipusHabitacio(new PreuTipusHabitacioId("Hotel4", "Suite3"), 1000f, descompte);
         Habitacio hab = new Habitacio(hId, "Suite3");
         Reserva res = new Reserva(new ReservaId("Hotel4", 2, new Date(100)), new Date(100), new Date(101), 1000d);
         
         HabitacioId hId2 = new HabitacioId("Hotel4", 5);
         Habitacio hab2 = new Habitacio(hId2, "Suite3");
         Reserva res2 = new Reserva(new ReservaId("Hotel4", 5, new Date(102)), new Date(102), new Date(103), 1000d);
         
         HabitacioId hId3 = new HabitacioId("Hotel4", 6);
         Habitacio hab3 = new Habitacio(hId3, "Suite3");
         Reserva res3 = new Reserva(new ReservaId("Hotel4", 6, new Date(104)), new Date(104), new Date(105), 1000d);
         
         session.saveOrUpdate(p);
         session.saveOrUpdate(c);
         session.saveOrUpdate(h);
         session.saveOrUpdate(tipusHab);
         session.saveOrUpdate(preuTipusHab);
         session.saveOrUpdate(descompte);
         session.saveOrUpdate(hab);
         session.saveOrUpdate(hab2);
         session.saveOrUpdate(hab3);
         session.saveOrUpdate(res);
         session.saveOrUpdate(res2);
         session.saveOrUpdate(res3);
         session.getTransaction().commit();
     }
 
     @After
     public void tearDown() {
     }
 
     private void clearDatabase() throws HibernateException {
         
         Session session = NewHibernateUtil.getSessionFactory().openSession();
         session.beginTransaction();
         Query query = session.createQuery("delete from Reserva");
         query.executeUpdate();
         query = session.createQuery("delete from Habitacio");
         query.executeUpdate();
         query = session.createQuery("delete from PreuAmbDescompte");
         query.executeUpdate();
         query = session.createQuery("delete from PreuAmbPercentatge");
         query.executeUpdate();
         query = session.createQuery("delete from PreuTipusHabitacio");
         query.executeUpdate();
         query = session.createQuery("delete from TipusHabitacio");
         query.executeUpdate();
         query = session.createQuery("delete from Hotel");
         query.executeUpdate();
         query = session.createQuery("delete from Categoria");
         query.executeUpdate();
         query = session.createQuery("delete from Poblacio");
         query.executeUpdate();
         session.getTransaction().commit();
     }
 
 }
