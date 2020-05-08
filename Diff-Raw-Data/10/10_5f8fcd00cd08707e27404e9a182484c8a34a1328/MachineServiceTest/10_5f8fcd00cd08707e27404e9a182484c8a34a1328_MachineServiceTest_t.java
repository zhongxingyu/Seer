 package cz.muni.fi.pa165.pujcovnastroju.test.service;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.fail;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.InjectMocks;
 import org.mockito.Matchers;
 import org.mockito.Mock;
 import org.mockito.Mockito;
 import org.mockito.invocation.InvocationOnMock;
 import org.mockito.runners.MockitoJUnitRunner;
 import org.mockito.stubbing.Answer;
 import org.springframework.dao.DataAccessException;
 import org.springframework.dao.DataAccessResourceFailureException;
 
 import cz.muni.fi.pa165.pujcovnastroju.converter.MachineTypeDTOConverter;
 import cz.muni.fi.pa165.pujcovnastroju.dao.MachineDAO;
 import cz.muni.fi.pa165.pujcovnastroju.dto.MachineDTO;
 import cz.muni.fi.pa165.pujcovnastroju.entity.Machine;
 import cz.muni.fi.pa165.pujcovnastroju.entity.MachineTypeEnum;
 import cz.muni.fi.pa165.pujcovnastroju.service.MachineService;
 import cz.muni.fi.pa165.pujcovnastroju.serviceimpl.MachineServiceImpl;
import org.hibernate.annotations.common.util.impl.Log_$logger;
 
 @RunWith(MockitoJUnitRunner.class)
 public class MachineServiceTest extends AbstractTest {
 
 	@Mock
 	MachineDAO mockMachineDao;
 
 	@InjectMocks
 	private MachineService service = new MachineServiceImpl();
 
 	@Before
 	public void setUp() {
 
 		Mockito.when(mockMachineDao.create(Matchers.any(Machine.class)))
 				.thenAnswer(new Answer<Machine>() {
 					@Override
 					public Machine answer(InvocationOnMock inv)
 							throws Throwable {
 						Object[] args = inv.getArguments();
 						return (Machine) args[0];
 
 					}
 				});
 
 		Mockito.when(mockMachineDao.update(Matchers.any(Machine.class)))
 				.thenAnswer(new Answer<Machine>() {
 					@Override
 					public Machine answer(InvocationOnMock inv)
 							throws Throwable {
 						Object[] args = inv.getArguments();
 						return (Machine) args[0];
 
 					}
 				});
 
 		Mockito.when(mockMachineDao.delete(Matchers.any(Machine.class)))
 				.thenAnswer(new Answer<Machine>() {
 					@Override
 					public Machine answer(InvocationOnMock inv)
 							throws Throwable {
 						Object[] args = inv.getArguments();
 						return (Machine) args[0];
 
 					}
 				});
 
 		Mockito.when(mockMachineDao.read(Long.valueOf(1))).thenReturn(
 				new Machine());
 
 		List<Machine> allMachines = new ArrayList<>();
 		allMachines.add(new Machine());
 		allMachines.add(new Machine());
 		Mockito.when(mockMachineDao.getAllMachines()).thenReturn(allMachines);
 
 		List<Machine> typedMachines = new ArrayList<>();
 		typedMachines.add(new Machine());
 		Mockito.when(mockMachineDao.getMachinesByType(MachineTypeEnum.CAN_OPENER))
 				.thenReturn(typedMachines);
 		Mockito.doThrow(
 				new IllegalArgumentException(
 						"Error occured during creating machine."))
 				.when(mockMachineDao).create(null);
 		Mockito.doThrow(
 				new IllegalArgumentException(
 						"Error occured during deleting machine."))
 				.when(mockMachineDao).delete(null);
 		Mockito.doThrow(
 				new IllegalArgumentException(
 						"Error occured during updating machine."))
 				.when(mockMachineDao).update(null);
 		Mockito.doThrow(
 				new IllegalArgumentException(
 						"Error occured during retrieving machine."))
 				.when(mockMachineDao).read(null);
 
 	}
 
 	@Test
 	public void testCreate() {
 		MachineDTO dto = new MachineDTO();
 		try {
 			service.create(null);
 			fail();
 		} catch (DataAccessException e) {
 		}
 		service.create(dto);
 		assertNotNull(dto);
 
 		assertEquals(true, true);
 	}
 
 	@Test
 	public void testDelete() {
 		try {
 			service.delete(null);
 			fail();
 		} catch (DataAccessException e) {
 		}
 
 		MachineDTO dto = new MachineDTO();
 		try {
 			MachineDTO deleted;
 			deleted = service.delete(dto);
 			assertNotNull(deleted);
 		} catch (Exception e) {
 			fail("Unexpected exception was thrown " + e);
 		}
 	}
 
 	@Test
 	public void testUpdate() {
 		try {
 			service.update(null);
 			fail();
 		} catch (DataAccessException e) {
 		}
 		try {
 			MachineDTO dto = new MachineDTO();
 			dto.setId(Long.valueOf(1));
 			dto.setType(MachineTypeDTOConverter
 					.entityToDto(MachineTypeEnum.CAN_OPENER));
 			dto.setLabel("abc");
 			MachineDTO updated = service.update(dto);
 			assertNotNull(updated);
 			assertEquals(dto, updated);
 		} catch (Exception e) {
 			fail("Unexpected exception was thrown " + e);
 		}
 	}
 
 	@Test
 	public void testRead() {
 		try {
 			service.read(null);
 			fail();
 		} catch (DataAccessException e) {
 		}
 
 		try {
 			MachineDTO dto = service.read(Long.valueOf(1));
 			assertNotNull(dto);
 		} catch (Exception e) {
 			fail("Unexpected exception was thrown " + e);
 		}
 	}
 
 	@Test
 	public void testGetAllMachines() {
 		MachineDTO m1 = new MachineDTO();
 		MachineDTO m2 = new MachineDTO();
 		try {
 			service.create(m1);
 			service.create(m2);
 
 			List<MachineDTO> list = service.getAllMachines();
 			assertNotNull(list);
 			assertEquals(2, list.size());
 		} catch (Exception e) {
 			fail("Unexpected exception was thrown " + e);
 		}
 	}
 
 	@Test
 	public void testGetMachinesByType() {
 		MachineDTO m1 = new MachineDTO();
 		MachineDTO m2 = new MachineDTO();
 		try {
 			service.create(m1);
 			m1.setType(MachineTypeDTOConverter
 					.entityToDto(MachineTypeEnum.BULDOZER));
 			service.create(m2);
 			m2.setType(MachineTypeDTOConverter
 					.entityToDto(MachineTypeEnum.CAN_OPENER));
 
 			List<MachineDTO> list = service
 					.getMachineDTOsByType(MachineTypeDTOConverter
 							.entityToDto(MachineTypeEnum.CAN_OPENER));
 			assertNotNull(list);
 			assertEquals(1, list.size());
 		} catch (Exception e) {
 			fail("Unexpected exception was thrown " + e);
 		}
 	}
 
 }
