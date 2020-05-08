 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package cz.muni.fi.pa165.jtravelagency.service;
 
 import cz.muni.fi.pa165.jtravelagency.dao.ExcursionDAOImpl;
 import cz.muni.fi.pa165.jtravelagency.dto.ExcursionDTO;
 import cz.muni.fi.pa165.jtravelagency.dto.TripDTO;
 import cz.muni.fi.pa165.jtravelagency.entity.Excursion;
 import cz.muni.fi.pa165.jtravelagency.util.DTOAndEntityMapper;
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.List;
 import static junit.framework.Assert.assertEquals;
 import static junit.framework.Assert.fail;
 import junit.framework.TestCase;
 import org.joda.time.DateTime;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
 import static org.mockito.Mockito.*;
 import org.mockito.runners.MockitoJUnitRunner;
 
 /**
  *
  * @author mvaraga
  */
 @RunWith(MockitoJUnitRunner.class)
 public class ExcursionServiceImplTest extends TestCase {
 
     @InjectMocks
     private ExcrursionServiceImpl service;
     @Mock
     private ExcursionDAOImpl dao;
 
 //    public ExcrursionServiceImplTest(String testName) {
 //        super(testName);
 //    }
     @Override
     protected void setUp() throws Exception {
         super.setUp();
     }
 
     @Override
     protected void tearDown() throws Exception {
         super.tearDown();
     }
 
     /**
      * Test of create method, of class ExcrursionServiceImpl.
      */
     @Test
     public void testCreate() {
         doThrow(new IllegalArgumentException()).when(dao).createExcursion(null);
 
         try {
             service.create(null);
             fail();
         } catch (IllegalArgumentException ex) {
             //OK
         }
 
 
         ExcursionDTO excursionDto = newExcursionDto();
         //doNothing().when(dao).createExcursion(DTOAndEntityMapper.dtoToEntity(excursionDto));       
         Excursion excursion = DTOAndEntityMapper.dtoToEntity(excursionDto, Excursion.class);//newExcursion();
         service.create(excursionDto);
         verify(dao).createExcursion(excursion);
 
         verify(dao, never()).deleteExcursion(excursion);
         verify(dao, never()).updateExcursion(null);
 
         // dao.createExcursion(excursion);
     }
 
     /**
      * Test of get method, of class ExcrursionServiceImpl.
      */
     public void testGet() {
         doThrow(new IllegalArgumentException()).when(dao).getExcursion(null);
         doThrow(new IllegalArgumentException()).when(dao).getExcursion(-1l);
 
         try {
             service.get(null);
             fail();
         } catch (IllegalArgumentException ex) {
             //OK
         }
 
         try {
             service.get(-1l);
             fail();
         } catch (IllegalArgumentException ex) {
             //OK
         }
 
         verify(dao, never()).createExcursion(null);
         verify(dao, times(1)).getExcursion(null);
         verify(dao, never()).updateExcursion(null);
         verify(dao, times(1)).getExcursion(-1l);
 
         ExcursionDTO excursionDto = newExcursionDto("excursion1");
         excursionDto.setId(1l);
 
         when(dao.getExcursion(1l)).thenReturn(DTOAndEntityMapper.dtoToEntity(excursionDto, Excursion.class));
         assertEquals(excursionDto, service.get(excursionDto.getId()));
 
         verify(dao, times(1)).getExcursion(1l);
         verify(dao, times(0)).createExcursion(DTOAndEntityMapper.dtoToEntity(excursionDto, Excursion.class));
         verify(dao, never()).updateExcursion(DTOAndEntityMapper.dtoToEntity(excursionDto, Excursion.class));
     }
 
     /**
      * Test of update method, of class ExcrursionServiceImpl.
      */
     public void testUpdate() {
         doThrow(new IllegalArgumentException()).when(dao).updateExcursion(null);
 
         try {
             service.update(null);
             fail();
         } catch (IllegalArgumentException ex) {
             //OK
         }
 
         verify(dao, never()).createExcursion(null);
         verify(dao, times(1)).updateExcursion(null);
         verifyNoMoreInteractions(dao);
 
         ExcursionDTO excursionDto = newExcursionDto();
         service.update(excursionDto);
         Excursion excursion = DTOAndEntityMapper.dtoToEntity(excursionDto, Excursion.class);
 
         verify(dao, times(1)).updateExcursion(excursion);
         verify(dao, times(0)).createExcursion(excursion);
     }
 
     /**
      * Test of delete method, of class ExcrursionServiceImpl.
      */
     public void testDelete() {
         doThrow(new IllegalArgumentException()).when(dao).deleteExcursion(null);
 
         try {
             service.delete(null);
             fail();
         } catch (IllegalArgumentException ex) {
             //OK
         }
 
         verify(dao, never()).createExcursion(null);
         verify(dao, times(1)).deleteExcursion(null);
         verify(dao, never()).updateExcursion(null);
         verifyNoMoreInteractions(dao);
 
         ExcursionDTO excursionDto = newExcursionDto();
         service.delete(excursionDto);
         Excursion excursion = DTOAndEntityMapper.dtoToEntity(excursionDto, Excursion.class);
 
         verify(dao, times(1)).deleteExcursion(excursion);
         verify(dao, times(0)).createExcursion(excursion);
         verify(dao, never()).updateExcursion(excursion);
     }
 
     /**
      * Test of getAll method, of class ExcrursionServiceImpl.
      */
     public void testGetAll() {
         when(dao.getAllExcursions()).thenReturn(new ArrayList<Excursion>());
         ArrayList<ExcursionDTO> dtoList = new ArrayList<ExcursionDTO>();
         assertEquals(new ArrayList<Excursion>(), dao.getAllExcursions());
 
         ExcursionDTO excursionDto1 = newExcursionDto("excursion1");
         ExcursionDTO excursionDto2 = newExcursionDto("excursion2");
         ExcursionDTO excursionDto3 = newExcursionDto("excursion3");
 
         excursionDto1.setId(1l);
         excursionDto2.setId(2l);
         excursionDto3.setId(3l);
 
         dtoList.add(excursionDto1);
         dtoList.add(excursionDto2);
         dtoList.add(excursionDto3);
 
         List<Excursion> entityList = new ArrayList<Excursion>();
         entityList.add(DTOAndEntityMapper.dtoToEntity(excursionDto1, Excursion.class));
         entityList.add(DTOAndEntityMapper.dtoToEntity(excursionDto2, Excursion.class));
         entityList.add(DTOAndEntityMapper.dtoToEntity(excursionDto3, Excursion.class));
 
         when(dao.getAllExcursions()).thenReturn(entityList);
         assertEquals(dtoList, service.getAll());
         verify(dao, times(2)).getAllExcursions();
     }
 
     private ExcursionDTO newExcursionDto(String description) {
         ExcursionDTO excursionDto = new ExcursionDTO();
         excursionDto.setDescription(description);
         excursionDto.setExcursionDate(new DateTime(2013, 10, 12, 10, 00));
         excursionDto.setPrice(new BigDecimal(10));
         excursionDto.setTrip(new TripDTO());
         return excursionDto;
     }
 
     private ExcursionDTO newExcursionDto() {
         return newExcursionDto("Description");
     }
}
