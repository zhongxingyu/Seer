 package de.fhb.autobday.manager.group;
 
 import com.stvconsultants.easygloss.javaee.JavaEEGloss;
 import de.fhb.autobday.dao.AbdContactFacade;
 import de.fhb.autobday.dao.AbdGroupFacade;
 import de.fhb.autobday.data.AbdContact;
 import de.fhb.autobday.data.AbdGroup;
 import de.fhb.autobday.data.AbdGroupToContact;
 import java.util.ArrayList;
 import javax.persistence.EntityManager;
 import static org.easymock.EasyMock.*;
 import static org.junit.Assert.assertEquals;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 
 /**
  * Integrationtest
  * 
  * @author 
  * Andy Klay <klay@fh-brandenburg.de>,
  * Christoph Ott
  */
 public class GroupManagerTestIntegration {
 	
 	private static JavaEEGloss gloss;
 	
 	private static GroupManager managerUnderTest;
 	
 	private static AbdGroupFacade groupDAO;
 	private static AbdContactFacade contactDAO;
 	
 	private EntityManager emMock;
 	
 	private AbdGroup group;
 	private AbdContact contact;
 	
 	@BeforeClass
 	public static void setUpClass(){
 		groupDAO = new AbdGroupFacade();
 		contactDAO = new AbdContactFacade();
 	}
 	
 	@Before
 	public void setUp() {
 		
 		gloss= new JavaEEGloss();
 		
 		//create Mocks
 		emMock = createMock(EntityManager.class);
 		
 		//set EntityManagers
 		groupDAO.setEntityManager(emMock);
 		contactDAO.setEntityManager(emMock);
 		
 		//set Objekts to inject
 		gloss.addEJB(groupDAO);
 		gloss.addEJB(contactDAO);
 		
 		//create Manager with Mocks
 		managerUnderTest=gloss.make(GroupManager.class);
 		
 		group = new AbdGroup("friends");
 		contact = new AbdContact("maja");
 	}
 	
 	
 	/**
 	 * test GetGroup of GroupManager
 	 * 
 	 */
 	@Test
 	public void testGetGroup()throws Exception {
 		System.out.println("testGetGroup");
 		
 		expect(emMock.find(AbdGroup.class, group.getId())).andReturn(group);
 		
 		// Setup is finished need to activate the mock
 		replay(emMock);
 		
 		managerUnderTest.getGroup(group.getId());
 		
 		//verify
 		verify(emMock);
 	}
 	
 	
 	/**
 	 * test SetTemplate of GroupManager
 	 * 
 	 */
 	@Test
 	public void testSetTemplate()throws Exception {
 		System.out.println("testSetTemplate");
 		
 		expect(emMock.find(AbdGroup.class, group.getId())).andReturn(group);
 		
 		// Setup is finished need to activate the mock
 		replay(emMock);
 		
 		managerUnderTest.setTemplate(group.getId(), "template");
 		
 		//verify
 		verify(emMock);
 	}
 	
 	/**
 	 * test GetTemplate of GroupManager
 	 * 
 	 */
 	@Test
 	public void testGetTemplate()throws Exception {
 		System.out.println("testGetTemplate");
 		
 		String expectedOutput = "template";
 		
 		group.setTemplate(expectedOutput);
 		
 		expect(emMock.find(AbdGroup.class, group.getId())).andReturn(group);
 		
 		// Setup is finished need to activate the mock
 		replay(emMock);
 		
 		assertEquals(expectedOutput, managerUnderTest.getTemplate(group.getId()));
 		
 		//verify
 		verify(emMock);
 	}
 	
 	
 	/**
 	 * test TestTemplate of GroupManager
 	 * 
 	 */
 	@Test
 	public void testTestTemplate()throws Exception {
 		System.out.println("testTestTemplate");
 		
 		group.setTemplate("Hello ${name} ${sex}");
 		contact.setName("biene");
 		contact.setSex('w');
 		
 		expect(emMock.find(AbdGroup.class, group.getId())).andReturn(group);
 		expect(emMock.find(AbdContact.class, contact.getId())).andReturn(contact);
 		
 		// Setup is finished need to activate the mock
 		replay(emMock);
 		
		assertEquals("Hello biene ", managerUnderTest.testTemplate(group.getId(), contact.getId()));
 		
 		//verify
 		verify(emMock);
 	}
 	
 	
 	/**
 	 * test SetActive of GroupManager
 	 * 
 	 */
 	@Test
 	public void testSetActive()throws Exception {
 		System.out.println("testSetActive");
 		
 		expect(emMock.find(AbdGroup.class, group.getId())).andReturn(group);
 		expect(emMock.merge(group)).andReturn(group);
 		
 		// Setup is finished need to activate the mock
 		replay(emMock);
 		
 		managerUnderTest.setActive(group, true);
 		
 		//verify
 		verify(emMock);
 	}
 	
 	
 	/**
 	 * test GetAllContactsFromGroup of GroupManager
 	 * 
 	 */
 	@Test
 	public void testGetAllContactsFromGroup()throws Exception {
 		System.out.println("testGetAllContactsFromGroup");
 		
 		//prepare test variables
 		AbdContact contactIch = new AbdContact("1");
 		AbdContact contactDu = new AbdContact("2");
 		AbdGroupToContact gContactIch = new AbdGroupToContact("meineGruppe", "ich");
 		AbdGroupToContact gContactDu = new AbdGroupToContact("meineGruppe", "du");
 		gContactIch.setAbdContact(contactIch);
 		gContactDu.setAbdContact(contactDu);
 		
 		ArrayList<AbdGroupToContact> abdGroupToContactCollection = new ArrayList<AbdGroupToContact>();
 		abdGroupToContactCollection.add(gContactIch);
 		abdGroupToContactCollection.add(gContactDu);
 		
 		ArrayList<AbdContact> outputCollection = new ArrayList<AbdContact>();
 		outputCollection.add(contactIch);
 		outputCollection.add(contactDu);
 		
 		group.setAbdGroupToContactCollection(abdGroupToContactCollection);
 		
 		expect(emMock.find(AbdGroup.class, group.getId())).andReturn(group);
 		
 		// Setup is finished need to activate the mock
 		replay(emMock);
 		
 		assertEquals(outputCollection, managerUnderTest.getAllContactsFromGroup(group));
 		
 		//verify
 		verify(emMock);
 	}
 	
 	
 }
