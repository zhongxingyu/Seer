 package cz.muni.fi.pompe.crental.service;
 
 import cz.muni.fi.pompe.crental.dao.DAOCar;
 import cz.muni.fi.pompe.crental.dto.DTOCar;
 import cz.muni.fi.pompe.crental.entity.Car;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.fail;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import static org.mockito.Mockito.*;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
 import org.mockito.MockitoAnnotations;
 import org.mockito.runners.MockitoJUnitRunner;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.dao.DataAccessException;
 import org.springframework.dao.DataIntegrityViolationException;
 
 /**
  *
  * @author Patrik Pompe <325292@mail.muni.cz>
  */
 @RunWith(MockitoJUnitRunner.class)
 public class CarServiceTest extends AbstractIntegrationTest {
 
     @Mock
     private DAOCar MockDAOCar;
     @InjectMocks
     @Autowired
     private CarService carService;
     private DTOCar carDTO;
     private Car car;
 
     @Before
     public void setUp() {
         carDTO = new DTOCar();
         carDTO.setCarType("BMW X5");
         carDTO.setEvidencePlate("123456");
         car = new Car();
         car.setCarType("BMW X5");
         car.setEvidencePlate("123456");
     }
 
     @Test
     public void testCreateNewCar() {
 
         carService.createCar(carDTO);
         verify(MockDAOCar, times(1)).createCar(car);     //check that method createCar() was invoked one time
 
         doReturn(Arrays.asList(car)).when(MockDAOCar).getAllCars();
         assertEquals(1, carService.getAllCars().size());
 
         car.setId(Long.MIN_VALUE);
         carDTO.setId(Long.MIN_VALUE);
         doThrow(new DataIntegrityViolationException("fail")).when(MockDAOCar).createCar(car);
         try {
             carService.createCar(carDTO);
             fail("cannot persist instance of car with setted id");
         } catch (DataAccessException ex) {
         }
 
     }
 
     @Test
     public void testDeleteCar() {
 
         carDTO.setId(1l);
         carService.deleteCar(carDTO);
         verify(MockDAOCar, times(1)).deleteCar(1l);
 
         List<Car> result = new ArrayList<>();
         doReturn(result).when(MockDAOCar).getAllCars();
         assertEquals(result.size(), carService.getAllCars().size());
 
         carDTO.setId(null);
         doThrow(new DataIntegrityViolationException("fail")).when(MockDAOCar).deleteCar(null);
 
         try {
             carService.deleteCar(carDTO);
         } catch (DataAccessException ex) {
         }
     }
 
     @Test
     public void testUpdateCar() {
         carDTO.setId(1l);
         car.setId(1l);
         carService.updateCar(carDTO);
         verify(MockDAOCar, times(1)).updateCar(car);
 
         carDTO.setId(null);
         car.setId(null);
         doThrow(new DataIntegrityViolationException("fail")).when(MockDAOCar).updateCar(car);
 
         try {
             carService.updateCar(carDTO);
         } catch (DataAccessException ex) {
         }
     }
 
     @Test
     public void testGetAllCars() {
         carService.getAllCars();
         verify(MockDAOCar, times(1)).getAllCars();
 
        List<DTOCar> DTOCars = new ArrayList<>();
        DTOCars.add(carDTO);
 
        doReturn(Arrays.asList(car)).when(MockDAOCar).getAllCars();
         assertEquals(1, carService.getAllCars().size());
     }
 
     @Test
     public void testGetCarById() {
         carService.getCarById(1l);
         verify(MockDAOCar, times(1)).getCarById(1l);
 
         car.setId(Long.MIN_VALUE);
         carDTO.setId(Long.MIN_VALUE);
         doReturn(car).when(MockDAOCar).getCarById(anyLong());
         assertEquals(carDTO, carService.getCarById(Long.MIN_VALUE));
 
         doReturn(null).when(MockDAOCar).getCarById(1l);
         assertNull(carService.getCarById(1l));
     }
 
     @Test
     public void testGetFreeCars() {
         carService.getFreeCars(new Date(), new Date());
        verify(MockDAOCar, times(1)).getFreeCars(new Date(), new Date());
 
         List<Car> resultCars = new ArrayList<>();
        when(MockDAOCar.getFreeCars(new Date(), new Date())).thenReturn(resultCars);
         
        assertEquals(resultCars.size(), carService.getFreeCars(new Date(), new Date()).size());
     }
 }
