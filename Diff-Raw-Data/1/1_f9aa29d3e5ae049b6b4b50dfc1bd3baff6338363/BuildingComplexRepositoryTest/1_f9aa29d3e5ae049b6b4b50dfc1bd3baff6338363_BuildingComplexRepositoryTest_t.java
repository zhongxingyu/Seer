 package com.flatmates.board.repository;
 
 import com.flatmates.board.domain.entity.BuildingComplex;
 import com.flatmates.board.domain.repository.BuildingComplexRepository;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 
 import java.util.Collection;
 import java.util.LinkedList;
 
 import org.junit.Before;
 import org.junit.Test;
 
 
 public class BuildingComplexRepositoryTest {
 
 	
 	BuildingComplexRepository buildingRepo =new SimpleBuildingComplexRepository();
 	@Before
 	public void setUp() throws Exception {
 	}
 	@Test
 	public void testSaveBuildingComplex() {
 		BuildingComplex expected = createBuildingComplex();
 		String id = buildingRepo.saveBuildingComplex(expected);
 		BuildingComplex actual = buildingRepo.findById(id);
 		assertEquals(expected.getAddress(),actual.getAddress());
 		assertEquals(expected.getId(),actual.getId());
 		assertNotNull(actual.getId());
 	}
 	@Test
 	public void testRemoveBuildingComplex() {
 		BuildingComplex expected = createBuildingComplex();
 		String id = buildingRepo.saveBuildingComplex(expected);
 		BuildingComplex actual = buildingRepo.findById(id);
 		assertNotNull(actual.getId());
 		buildingRepo.removeBuildingComplex(actual);
 		assertNull(buildingRepo.findById(id));
 	}
 
 	@Test
 	public void testQueryByAddress() {
 		BuildingComplex expected = createBuildingComplex();
 		System.out.println("-------expected id : "+ expected.getId());
 		String id = buildingRepo.saveBuildingComplex(expected);
 		System.out.println("++++++++++++id : "+ id);
 		Collection<BuildingComplex> actualList = new LinkedList<BuildingComplex>();
 		actualList = buildingRepo.queryByAddress(expected.getAddress());
 		BuildingComplex actual = (BuildingComplex)actualList.toArray()[0];
 		assertEquals(expected.getAddress(),actual.getAddress());
 		System.out.println("*************actual id : "+ actual.getId()+"  ====>"+expected.getId());
 		assertEquals(expected.getId(),actual.getId());
 	}
 
 	@Test
 	public void testFindById() {
 		BuildingComplex expected = createBuildingComplex();
 		String id = buildingRepo.saveBuildingComplex(expected);
 		BuildingComplex actual = buildingRepo.findById(id);
 		assertNotNull(actual.getId());
 		assertEquals(expected.getId(),actual.getId());
 		assertEquals(expected.getAddress(),actual.getAddress());
 	}
 
 	@Test
 	public void testUpdateBuildingComplexAddress() {
 		BuildingComplex expected = createBuildingComplex();
 		String id = buildingRepo.saveBuildingComplex(expected);
 		buildingRepo.updateBuildingComplexAddress(id, "new address");
 		BuildingComplex actual = buildingRepo.findById(id);
 		assertEquals("new address",actual.getAddress());
 	}
 
 	@Test
 	public void testListAll() {
 		BuildingComplex expected = createBuildingComplex();
 		BuildingComplex expected2 = createBuildingComplex();
                expected2.setAddress("ida 2");
 		String id = buildingRepo.saveBuildingComplex(expected);
 		String id2 = buildingRepo.saveBuildingComplex(expected2);
 		Collection<BuildingComplex> actualList = new LinkedList<BuildingComplex>();
 		actualList = buildingRepo.listAll();
 		assertEquals(2,actualList.size());
 	}
 
 	@Test
 	public void testRemoveAll() {
 		BuildingComplex expected = createBuildingComplex();
 		String id = buildingRepo.saveBuildingComplex(expected);
 		Collection<BuildingComplex> actualList = new LinkedList<BuildingComplex>();
 		actualList = buildingRepo.listAll();
 		assertEquals(1,actualList.size());
 		buildingRepo.removeAll();
 		actualList = buildingRepo.listAll();
 		assertEquals(0,actualList.size());
 	}
 	private BuildingComplex createBuildingComplex() {
 		BuildingComplex building = new BuildingComplex();
 		building.setAddress("ida");
 		return building;
 	}
 }
