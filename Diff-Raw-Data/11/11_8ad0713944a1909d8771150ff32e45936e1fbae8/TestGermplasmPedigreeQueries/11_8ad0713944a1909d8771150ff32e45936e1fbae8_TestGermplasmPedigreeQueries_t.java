 package org.generationcp.middleware.manager.test;
 
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.generationcp.middleware.manager.DatabaseConnectionParameters;
 import org.generationcp.middleware.manager.FindGermplasmByNameModes;
 import org.generationcp.middleware.manager.GermplasmNameType;
 import org.generationcp.middleware.manager.ManagerFactory;
 import org.generationcp.middleware.manager.Operation;
 import org.generationcp.middleware.manager.api.GermplasmDataManager;
 import org.generationcp.middleware.pojos.Attribute;
 import org.generationcp.middleware.pojos.Bibref;
 import org.generationcp.middleware.pojos.Country;
 import org.generationcp.middleware.pojos.Germplasm;
 import org.generationcp.middleware.pojos.GermplasmPedigreeTree;
 import org.generationcp.middleware.pojos.GermplasmPedigreeTreeNode;
 import org.generationcp.middleware.pojos.Location;
 import org.generationcp.middleware.pojos.Method;
 import org.generationcp.middleware.pojos.Name;
 import org.generationcp.middleware.pojos.UserDefinedField;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 public class TestGermplasmPedigreeQueries 
 {
 	private static ManagerFactory factory;
 	private static GermplasmDataManager manager;
 
 	@BeforeClass
 	public static void setUp() throws Exception 
 	{
 		DatabaseConnectionParameters local = new DatabaseConnectionParameters("testDatabaseConfig.properties", "local");
 		DatabaseConnectionParameters central = new DatabaseConnectionParameters("testDatabaseConfig.properties", "central");
 		factory = new ManagerFactory(local, central);
 		manager = factory.getGermplasmDataManager();
 	}
 	
 	@Test
 	public void testGetMethodById() throws Exception
 	{
 		Method method = manager.getMethodByID(new Integer(1));
 		System.out.println(method);
 	}
 	
 	@Test
 	public void testGetAllMethods() throws Exception
 	{
 		List<Method> methods = manager.getAllMethods();
 		
 		List<Method> sublist = methods.subList(0, 4);
 		for(Method method : sublist)
 		{
 			System.out.println(method);
 		}
 	}
 	
 	@Test
 	public void testGetUserDefinedFieldById() throws Exception
 	{
 		UserDefinedField udfld = manager.getUserDefinedFieldByID(new Integer(1));
 		System.out.println(udfld);
 	}
 	
 	@Test
 	public void testGetCountryById() throws Exception
 	{
 		Country country = manager.getCountryById(229);
 		System.out.println(country);
 	}
 	
 	@Test
 	public void testGetLocationById() throws Exception
 	{
 		Location location = manager.getLocationByID(new Integer(1));
 		System.out.println(location);
 	}
 	
 	@Test
 	public void testGetBibliographicalReferenceById() throws Exception
 	{
 		Bibref bibref = manager.getBibliographicReferenceByID(new Integer(1));
 		System.out.println(bibref);
 	}
 	
 	@Test
 	public void testGetGermplasmDescendants() throws Exception
 	{
 		List<Object[]> germplsmList = manager.findDescendants(47888, 0, 20);
 		
 		for(Object[] object : germplsmList)
 		{
 			System.out.println("progenitor number: " + object[0]);
 			System.out.println(object [1]);
 		}
 	}
 	
 	@Test
 	public void testCountGermplasmDescendants() throws Exception
 	{
 		Integer count = manager.countDescendants(47888);
 		System.out.println("COUNT "+ count);
 	}
 	
 	@Test
 	public void testGetProgenitorByGID() throws Exception
 	{
 		Germplasm germplasm = manager.getParentByGIDAndProgenitorNumber(779745, 10);
 		System.out.println(germplasm);
 	}
 	
 	@Test
 	public void testGeneratePedigreeTree() throws Exception
 	{
 		GermplasmPedigreeTree tree = manager.generatePedigreeTree(new Integer(306436), 4);
		if(tree != null)
		{
			printNode(tree.getRoot(), 1);
		}
 	}
 	
 	@Test
 	public void testGetManagementNeighbors() throws Exception
 	{
 		List<Germplasm> neighbors = manager.getManagementNeighbors(new Integer(625));
 		
 		System.out.println("RESULTS:");
 		for(Germplasm g : neighbors)
 		{
 			String name = g.getPreferredName() != null ? g.getPreferredName().getNval() : null;
 			System.out.println(g.getGid() + " : " + name);
 		}
 	}
 	
 	@Test
 	public void testGetGroupRelatives() throws Exception
 	{
 		List<Germplasm> neighbors = manager.getGroupRelatives(new Integer(1));
 		
 		System.out.println("RESULTS:");
 		for(Germplasm g : neighbors)
 		{
 			String name = g.getPreferredName() != null ? g.getPreferredName().getNval() : null;
 			System.out.println(g.getGid() + " : " + name);
 		}
 	}
 	
 	@Test
 	public void testGetGenerationHistory() throws Exception
 	{
 		List<Germplasm> results = manager.getGenerationHistory(new Integer(50533));
 		
 		System.out.println("RESULTS:");
 		for(Germplasm g : results)
 		{
 			String name = g.getPreferredName() != null ? g.getPreferredName().getNval() : null;
 			System.out.println(g.getGid() + " : " + name);
 		}
 	}
 	
 	@Test
 	public void testGetDerivativeNeighborhood() throws Exception
 	{
 		GermplasmPedigreeTree tree = manager.getDerivativeNeighborhood(new Integer(1), 3, 3);
		if(tree != null)
		{
			printNode(tree.getRoot(), 1);
		}
 	}
 	
 	/**
 	@Test
 	public void testFindGermplasmByExample() throws Exception
 	{
 		Germplasm sample = new Germplasm();
 		Location location = new Location();
 		location.setLname("International");
 		sample.setLocation(location);
 		
 		List<Germplasm> results = manager.findGermplasmByExample(sample, 0, 5);
 		Assert.assertTrue(results != null);
 		Assert.assertTrue(!results.isEmpty());
 		for(Germplasm result : results)
 		{
 			System.out.println(result);
 		}
 	}
 	
 	@Test
 	public void testCountGermplasmByExample() throws Exception
 	{
 		Germplasm sample = new Germplasm();
 		Location location = new Location();
 		location.setLname("International");
 		sample.setLocation(location);
 		
 		System.out.println(manager.countGermplasmByExample(sample));
 	}
 	
 	@Test
 	public void testFindGermplasmByExampleWithAttribute() throws Exception
 	{
 		Germplasm sample = new Germplasm();
 		Attribute attribute = new Attribute(1);
 		attribute.setAval("FOR DISTRIBUTION");
 		sample.getAttributes().add(attribute);
 		
 		List<Germplasm> results = manager.findGermplasmByExample(sample, 0, 5);
 		Assert.assertTrue(results != null);
 		Assert.assertTrue(!results.isEmpty());
 		for(Germplasm result : results)
 		{
 			System.out.println(result);
 		}
 	}
 	
 	@Test
 	public void testCountGermplasmByExampleWithAttribute() throws Exception
 	{
 		Germplasm sample = new Germplasm();
 		Attribute attribute = new Attribute(1);
 		attribute.setAval("FOR DISTRIBUTION");
 		sample.getAttributes().add(attribute);
 		
 		System.out.println(manager.countGermplasmByExample(sample));
 	}
 	**/
 	
 	private void printNode(GermplasmPedigreeTreeNode node, int level)
 	{
 		StringBuffer tabs = new StringBuffer();
 		
 		for(int ctr = 1; ctr < level; ctr++)
 		{
 			tabs.append("\t");
 		}
 		
 		String name = node.getGermplasm().getPreferredName() != null ? node.getGermplasm().getPreferredName().getNval() : null;
 		System.out.println(tabs.toString() + node.getGermplasm().getGid() + " : " + name);
 		
 		for(GermplasmPedigreeTreeNode parent : node.getLinkedNodes())
 		{
 			printNode(parent, level+1);
 		}
 	}
 	
 	@AfterClass
 	public static void tearDown() throws Exception 
 	{
 		factory.close();
 	}
 
 }
