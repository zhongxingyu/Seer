 package com.rdonasco.security.services;
 
 import com.rdonasco.common.dao.BaseDAO;
 import com.rdonasco.common.exceptions.DataAccessException;
 import com.rdonasco.common.i18.I18NResource;
 import com.rdonasco.config.dao.ConfigElementDAO;
 import com.rdonasco.config.data.ConfigElement;
 import com.rdonasco.config.parsers.ValueParser;
 import com.rdonasco.config.services.ConfigDataManagerLocal;
 import com.rdonasco.config.services.ConfigDataManagerProxyRemote;
 import com.rdonasco.config.util.ConfigDataValueObjectConverter;
 import com.rdonasco.config.vo.ConfigAttributeVO;
 import com.rdonasco.datamanager.services.DataManager;
 import com.rdonasco.datamanager.utils.CommonConstants;
 import com.rdonasco.security.dao.ActionDAO;
 import com.rdonasco.security.exceptions.CapabilityManagerException;
 import com.rdonasco.security.model.Action;
 import com.rdonasco.security.utils.SecurityEntityValueObjectConverter;
 import com.rdonasco.security.utils.SecurityEntityValueObjectDataUtility;
 import com.rdonasco.security.vo.ActionVO;
 import com.rdonasco.security.vo.CapabilityVO;
 import com.rdonasco.security.vo.CapabilityVOBuilder;
 import com.rdonasco.security.vo.ResourceVO;
 import com.rdonasco.security.vo.ResourceVOBuilder;
 import com.rdonasco.security.vo.UserCapabilityVO;
 import com.rdonasco.security.vo.UserCapabilityVOBuilder;
 import com.rdonasco.security.vo.UserSecurityProfileVO;
 import com.rdonasco.security.vo.UserSecurityProfileVOBuilder;
 import java.util.List;
 import java.util.logging.Logger;
 import javax.ejb.EJB;
 import org.jboss.arquillian.container.test.api.Deployment;
 import org.jboss.arquillian.junit.Arquillian;
 import org.jboss.shrinkwrap.api.ArchivePaths;
 import org.jboss.shrinkwrap.api.ShrinkWrap;
 import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
 import org.jboss.shrinkwrap.api.spec.JavaArchive;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import static org.junit.Assert.*;
 
 /**
  * Unit test for simple App.
  */
 @RunWith(Arquillian.class)
 public class SystemSecurityInitializerLocalTest
 {
 
 	private static final Logger LOG = Logger.getLogger(SystemSecurityInitializerLocalTest.class.getName());
 	@EJB
 	private SystemSecurityInitializerLocal systemSecurityInitializer;
 	@EJB
 	private ConfigDataManagerProxyRemote configDataManagerProxyUnderTest;
 	@EJB
 	private CapabilityManagerLocal capabilityManager;
 
 	@Deployment
 	public static JavaArchive createTestArchive()
 	{
 		return ShrinkWrap.create(JavaArchive.class, "SecurityClientTest.jar")
 				.addPackage(BaseDAO.class.getPackage())
 				.addPackage(DataAccessException.class.getPackage())
 				.addPackage(I18NResource.class.getPackage())
 				.addPackage(CommonConstants.class.getPackage())
 				.addPackage(DataManager.class.getPackage())
 				.addPackage(ConfigElementDAO.class.getPackage())
 				.addPackage(ValueParser.class.getPackage())
 				.addPackage(ConfigElement.class.getPackage())
 				.addPackage(ConfigDataManagerLocal.class.getPackage())
 				.addPackage(ConfigAttributeVO.class.getPackage())
 				.addPackage(ActionDAO.class.getPackage())
 				.addPackage(Action.class.getPackage())
 				.addPackage(CapabilityManagerLocal.class.getPackage())
 				.addPackage(ConfigDataValueObjectConverter.class.getPackage())
 				.addPackage(SecurityEntityValueObjectConverter.class.getPackage())
 				.addAsManifestResource(new ByteArrayAsset("<beans/>".getBytes()), ArchivePaths.create("beans.xml"))
 				.addAsResource(I18NResource.class.getPackage(), "i18nResource.properties", "/WEB-INF/classes/net/baligya/i18n")
 				.addAsManifestResource("persistence.xml", ArchivePaths.create("persistence.xml"));
 
 
 	}
 
 
 	@Test
 	public void testInitializeDefaultSystemAccessCapabilities() throws Exception
 	{
 		System.out.println("InitializeDefaultSystemAccessCapabilities");
 		systemSecurityInitializer.initializeDefaultSystemAccessCapabilities();
 		String resourceXPath = new StringBuilder(SystemSecurityInitializerLocal.DEFAULT_CAPABILITY_ELEMENT_XPATH)
 				.append("/Logon To System/resource").toString();
 		String resourceName = configDataManagerProxyUnderTest.loadValue(resourceXPath, String.class);
 		assertEquals("system",resourceName);
 		
 		String actionXPath = new StringBuilder(SystemSecurityInitializerLocal.DEFAULT_CAPABILITY_ELEMENT_XPATH)
 				.append("/Logon To System/action").toString();
 		List<ConfigAttributeVO> attributes = configDataManagerProxyUnderTest.findConfigAttributesWithXpath(actionXPath);
		System.out.println("attrbuteSize:" + attributes.size());
 		assertNotNull(attributes);
 		assertEquals("logon",attributes.get(0).getValue());
 		assertEquals("logoff",attributes.get(1).getValue());
 	}
 
 	/*
 	 @Test
 	 public void testInitializeDefaultSystemAccessCapabilities() throws Exception
 	 {
 	 System.out.println("initializeDefaultSystemAccessCapabilities");
 	 systemSecurityInitializer.initializeDefaultSystemAccessCapabilities();
 	 String title;
 	 String resourceName;
 	 String action;
 	 for (String[] capabilityInitString : SystemSecurityInitializerLocal.DEFAULT_CAPABILITY_ELEMENTS)
 	 {
 	 title = capabilityInitString[SystemSecurityInitializerLocal.ELEMENT_CAPABILITY_TITLE];
 	 CapabilityVO capability = capabilityManager.findCapabilityWithTitle(title);
 	 assertNotNull("capability" + title + " not found",capability);
 	 //			resourceName = capabilityInitString[SystemSecurityInitializerLocal.ELEMENT_RESOURCE];
 	 //			for (int i = SystemSecurityInitializerLocal.ELEMENT_RESOURCE + 1; i < capabilityInitString.length; i++)
 	 //			{
 	 //				action = capabilityInitString[i];
 	 //			}
 	 }
 
 	 }
 	 */
 	// ------ utility methods below here ------ //
 	private ActionVO createTestDataActionNamed(String name) throws
 			CapabilityManagerException
 	{
 		ActionVO savedAction = capabilityManager.findOrAddActionNamedAs(name);
 		return savedAction;
 	}
 
 	private ResourceVO createTestDataResourceNamed(String name) throws
 			CapabilityManagerException
 	{
 		ResourceVO resourceToAdd = new ResourceVOBuilder()
 				.setName(name)
 				.setDescription(name + "description")
 				.createResourceVO();
 
 		ResourceVO resourceAdded = capabilityManager.addResource(resourceToAdd);
 		return resourceAdded;
 	}
 
 	private CapabilityVO createTestDataCapabilityWithActionAndResourceName(
 			final String actionName,
 			final String resourceName) throws CapabilityManagerException
 	{
 		ActionVO action = createTestDataActionNamed(actionName);
 		ResourceVO resource = createTestDataResourceNamed(resourceName + SecurityEntityValueObjectDataUtility.generateRandomID());
 		final String capabilityTitle = "capability to " + action.getName() + " " + resource.getName();
 		CapabilityVO capabilityVO = new CapabilityVOBuilder()
 				.addAction(action)
 				.setResource(resource)
 				.setTitle(capabilityTitle)
 				.setDescription(capabilityTitle + " description")
 				.createCapabilityVO();
 		CapabilityVO savedCapabilityVO = capabilityManager.createNewCapability(capabilityVO);
 		return savedCapabilityVO;
 	}
 
 	private UserCapabilityVO createTestDataUserCapabilityVO(
 			CapabilityVO capabilityVO)
 	{
 		UserCapabilityVO userCapabilityVO = new UserCapabilityVOBuilder()
 				.setCapability(capabilityVO)
 				.createUserCapabilityVO();
 		return userCapabilityVO;
 	}
 
 	private UserSecurityProfileVO createTestDataUserProfileWithCapability()
 			throws CapabilityManagerException
 	{
 		UserSecurityProfileVO userProfile = createTestDataWithoutCapability();
 		CapabilityVO capabilityVO = createTestDataCapabilityWithActionAndResourceName("edit", "pets");
 		UserCapabilityVO userCapabilityVO = createTestDataUserCapabilityVO(capabilityVO);
 		userProfile.addCapbility(userCapabilityVO);
 		return userProfile;
 	}
 
 	private UserSecurityProfileVO createTestDataWithoutCapability()
 	{
 		UserSecurityProfileVO userProfile = new UserSecurityProfileVOBuilder()
 				.setLoginId("rdonasco" + SecurityEntityValueObjectDataUtility.generateRandomID())
 				.setPassword("rdonasco")
 				.createUserSecurityProfileVO();
 		return userProfile;
 	}
 }
