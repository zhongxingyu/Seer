 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package cz.muni.fi.pa165.jtravelagency.service;
 
 import cz.muni.fi.pa165.jtravelagency.dao.ReservationDAO;
 import cz.muni.fi.pa165.jtravelagency.dto.CustomerDTO;
 import cz.muni.fi.pa165.jtravelagency.dto.ExcursionDTO;
 import cz.muni.fi.pa165.jtravelagency.dto.ReservationDTO;
 import cz.muni.fi.pa165.jtravelagency.dto.TripDTO;
 import cz.muni.fi.pa165.jtravelagency.entity.Customer;
 import cz.muni.fi.pa165.jtravelagency.entity.Excursion;
 import cz.muni.fi.pa165.jtravelagency.entity.Reservation;
 import cz.muni.fi.pa165.jtravelagency.entity.Trip;
 import cz.muni.fi.pa165.jtravelagency.util.DTOAndEntityMapper;
 import java.util.ArrayList;
 import java.util.List;
 import junit.framework.TestCase;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
 import static org.mockito.Mockito.*;
 import org.mockito.runners.MockitoJUnitRunner;
 
 /**
  *
  * @author jakub
  */
 @RunWith(MockitoJUnitRunner.class)
 public class ReservationServiceImplTest extends TestCase {
     
     
     @InjectMocks
     private ReservationServiceImpl service;
     
     @Mock
     private ReservationDAO reservationDAO;
     
     
     /*public ReservationServiceImplTest(String testName) {
         super(testName);
         service = new ReservationServiceImpl();
         reservationDAO = mock(ReservationDAOImpl.class);
         service.setReservationDAO(reservationDAO);
     }*/
     
     @Override
     protected void setUp() throws Exception {
         super.setUp();
     }
     
     @Override
     protected void tearDown() throws Exception {
         super.tearDown();
     }
 
 
     /**
      * Test of create method, of class ReservationServiceImpl.
      */
     @Test
     public void testCreate() {
         try{
             service.create(null);
             fail();
         }   catch(IllegalArgumentException ex){
             // OK
         }
         
         verify(reservationDAO,never()).createReservation(null);
         
         ReservationDTO reservationDTO = newReservationDTO();        
         Reservation reservation = newReservation();
         service.create(reservationDTO);
         
         verify(reservationDAO).createReservation(reservation);
         
         //doNothing().when(reservationDAO).createReservation(any(Reservation.class));
         //service.create(DTOAndEntityMapper.entityToDto(reservation));
         //verify(reservationDAO,times(0)).createReservation(reservation);
         
     }
     
     /**
      * Test of wrong create method - catching IllegalArgumentException, of class
      * ReservationServiceImpl.
      */
     @Test(expected = IllegalArgumentException.class)
     public void testCreateException() {
         ReservationDTO reservationDTO = newReservationDTO();    
         reservationDTO.setId(1l);
         try {
             service.create(reservationDTO);
             fail();
         }
         finally {
             verify(reservationDAO,never()).createReservation(null);
         }
     }
     
     /**
      * Test of delete method, of class ReservationServiceImpl.
      */
     @Test
     public void testDelete() {
         try{
             service.delete(null);
             fail();
         }   catch(IllegalArgumentException ex){
             // OK
         }
         
         verify(reservationDAO,never()).deleteReservation(null);
         
         ReservationDTO reservationDTO = newReservationDTO();
         Reservation reservation = newReservation();
         service.create(reservationDTO);
         
         verify(reservationDAO,times(0)).deleteReservation(reservation);
         
         service.delete(reservationDTO);
   
         verify(reservationDAO,times(1)).createReservation(reservation);
         verify(reservationDAO,times(1)).deleteReservation(reservation);
     }
 
     /**
      * Test of update method, of class ReservationServiceImpl.
      */
     @Test
     public void testUpdate() {
         try {
             service.update(null);
             fail();
         } catch(IllegalArgumentException ex){
             // OK
         }
          
         ReservationDTO reservationDTO = newReservationDTO();
         reservationDTO.setTrip(new TripDTO());
         service.update(reservationDTO);
         
         verify(reservationDAO,never()).createReservation(null);
         verify(reservationDAO,times(1)).updateReservation(DTOAndEntityMapper.dtoToEntity(reservationDTO, Reservation.class));
     }
 
     /**
      * Test of get method, of class ReservationServiceImpl.
      */
     @Test
     public void testGet() {
         try {
             service.get(null);
             fail();
         } catch(IllegalArgumentException ex){
             // OK
         }
         
        verify(service,never()).create(null);
        verify(service,never()).update(null);
         
     }
 
     /**
      * Test of getAll method, of class ReservationServiceImpl.
      *
     @Test
     public void testGetAll() {
         System.out.println("getAll");
         ReservationServiceImpl instance = new ReservationServiceImpl();
         List expResult = null;
         List result = instance.getAll();
         assertEquals(expResult, result);
         // TODO review the generated test code and remove the default call to fail.
         fail("The test case is a prototype.");
     }
 
     /**
      * Test of getByTrip method, of class ReservationServiceImpl.
      *
     @Test
     public void testGetByTrip() {
         System.out.println("getByTrip");
         TripDTO trip = null;
         ReservationServiceImpl instance = new ReservationServiceImpl();
         List expResult = null;
         List result = instance.getByTrip(trip);
         assertEquals(expResult, result);
         // TODO review the generated test code and remove the default call to fail.
         fail("The test case is a prototype.");
     }
 
     /**
      * Test of getByCustomer method, of class ReservationServiceImpl.
      *
     @Test
     public void testGetByCustomer() {
         System.out.println("getByCustomer");
         CustomerDTO customer = null;
         ReservationServiceImpl instance = new ReservationServiceImpl();
         List expResult = null;
         List result = instance.getByCustomer(customer);
         assertEquals(expResult, result);
         // TODO review the generated test code and remove the default call to fail.
         fail("The test case is a prototype.");
     }*/
     
     private ReservationDTO newReservationDTO() {
         ReservationDTO reservationDTO = new ReservationDTO();
         reservationDTO.setCustomer(new CustomerDTO());
         List<ExcursionDTO> excursions = new ArrayList<ExcursionDTO>();
         ExcursionDTO excursion = new ExcursionDTO();
         excursions.add(excursion);
         reservationDTO.setExcursions(excursions);
         reservationDTO.setTrip(new TripDTO());
         return reservationDTO;
     }
     
     private Reservation newReservation() {
         Reservation reservation = new Reservation();
         reservation.setCustomer(new Customer());
         List<Excursion> excursions = new ArrayList<Excursion>();
         Excursion excursion = new Excursion();
         excursions.add(excursion);
         reservation.setExcursions(excursions);
         reservation.setTrip(new Trip());
         return reservation;
     }
 }
