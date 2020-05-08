 /**
  *<p>Title: </p>
  *<p>Description:  </p>
  *<p>Copyright://TODO</p>
  *@author Vishvesh Mulay
  *@author Rahul Ner
  *@version 1.0
  */
 
 package edu.common.dynamicextensions.entitymanager;
 
 import java.sql.Types;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import edu.common.dynamicextensions.dao.impl.DynamicExtensionDAO;
 import edu.common.dynamicextensions.domain.Attribute;
 import edu.common.dynamicextensions.domain.AttributeTypeInformation;
 import edu.common.dynamicextensions.domain.DateAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.DomainObjectFactory;
 import edu.common.dynamicextensions.domain.Entity;
 import edu.common.dynamicextensions.domain.EntityGroup;
 import edu.common.dynamicextensions.domain.FileAttributeRecordValue;
 import edu.common.dynamicextensions.domain.FileAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.FileExtension;
 import edu.common.dynamicextensions.domain.ObjectAttributeRecordValue;
 import edu.common.dynamicextensions.domain.StringAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.StringValue;
 import edu.common.dynamicextensions.domain.TaggedValue;
 import edu.common.dynamicextensions.domain.databaseproperties.TableProperties;
 import edu.common.dynamicextensions.domain.userinterface.Container;
 import edu.common.dynamicextensions.domaininterface.AssociationInterface;
 import edu.common.dynamicextensions.domaininterface.AttributeInterface;
 import edu.common.dynamicextensions.domaininterface.AttributeMetadataInterface;
 import edu.common.dynamicextensions.domaininterface.AttributeTypeInformationInterface;
 import edu.common.dynamicextensions.domaininterface.CaDSRValueDomainInfoInterface;
 import edu.common.dynamicextensions.domaininterface.EntityGroupInterface;
 import edu.common.dynamicextensions.domaininterface.EntityInterface;
 import edu.common.dynamicextensions.domaininterface.NumericTypeInformationInterface;
 import edu.common.dynamicextensions.domaininterface.ObjectAttributeRecordValueInterface;
 import edu.common.dynamicextensions.domaininterface.PermissibleValueInterface;
 import edu.common.dynamicextensions.domaininterface.SemanticPropertyInterface;
 import edu.common.dynamicextensions.domaininterface.StringValueInterface;
 import edu.common.dynamicextensions.domaininterface.TaggedValueInterface;
 import edu.common.dynamicextensions.domaininterface.UserDefinedDEInterface;
 import edu.common.dynamicextensions.domaininterface.databaseproperties.ConstraintPropertiesInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.ContainerInterface;
 import edu.common.dynamicextensions.domaininterface.validationrules.RuleInterface;
 import edu.common.dynamicextensions.domaininterface.validationrules.RuleParameterInterface;
 import edu.common.dynamicextensions.exception.DynamicExtensionsApplicationException;
 import edu.common.dynamicextensions.exception.DynamicExtensionsSystemException;
 import edu.common.dynamicextensions.exception.DynamicExtensionsValidationException;
 import edu.common.dynamicextensions.processor.ProcessorConstants;
 import edu.common.dynamicextensions.ui.util.ControlConfigurationsFactory;
 import edu.common.dynamicextensions.util.DynamicExtensionsBaseTestCase;
 import edu.common.dynamicextensions.util.DynamicExtensionsUtility;
 import edu.common.dynamicextensions.util.global.DEConstants.AssociationDirection;
 import edu.common.dynamicextensions.util.global.DEConstants.AssociationType;
 import edu.common.dynamicextensions.util.global.DEConstants.Cardinality;
 import edu.common.dynamicextensions.util.global.DEConstants.ValueDomainType;
 import edu.common.dynamicextensions.validation.ValidatorRuleInterface;
 import edu.wustl.common.util.global.Constants;
 import edu.wustl.common.util.global.Status;
 import edu.wustl.common.util.logger.Logger;
 import edu.wustl.dao.daofactory.DAOConfigFactory;
 
 public class TestEntityManager extends DynamicExtensionsBaseTestCase
 {
 
 	/**
 	 *
 	 */
 	public TestEntityManager()
 	{
 		super();
 		//TODO Auto-generated constructor stub
 	}
 
 	/**
 	 * @param arg0 name
 	 */
 	public TestEntityManager(String arg0)
 	{
 		super(arg0);
 		//TODO Auto-generated constructor stub
 	}
 
 	/**
 	 * @see edu.common.dynamicextensions.util.DynamicExtensionsBaseTestCaseUtility#setUp()
 	 */
 	protected void setUp()
 	{
 		super.setUp();
 	}
 
 	/**
 	 * @see edu.common.dynamicextensions.util.DynamicExtensionsBaseTestCaseUtility#tearDown()
 	 */
 	protected void tearDown()
 	{
 		super.tearDown();
 	}
 
 	/**
 	 * PURPOSE : To test the whether editing of an existing entity works properly or not.
 	 * EXPECTED BEHAVIOR : Changes in the existing entity should be stored properly and the changes made to the
 	 * attributes of the entity should get reflected properly in the data tables.
 	 * TEST CASE FLOW :
 	 * 1. Create entity with some attributes
 	 * 2. Save entity using entity manager.
 	 * 3. Add an extra attribute to the entity
 	 * 4. Save the entity again.
 	 * 5. Check whether the added attribute is retrieved back properly from the database.
 	 * 6. Check whether a column is newly added or not  to the data table of the entity.
 	 */
 	public void testEditEntity()
 	{
 		EntityGroupInterface entityGroup = DomainObjectFactory.getInstance().createEntityGroup();
 		entityGroup.setName("test_" + new Double(Math.random()).toString());
 		//Step 1
 		Entity entity = (Entity) createAndPopulateEntity();
 		entity.setName("Stock Quote");
 		entity.setEntityGroup(entityGroup);
 		entityGroup.addEntity(entity);
 		EntityManagerInterface EntityManagerInterface = EntityManager.getInstance();
 
 		try
 		{
 			//Step 2
 			EntityInterface savedEntity = EntityManagerInterface.persistEntity(entity);
 
 			//Step 3 Edit entity-- Add extra attribute
 			AttributeInterface floatAtribute = DomainObjectFactory.getInstance()
 					.createFloatAttribute();
 			floatAtribute.setName("Price");
 
 			savedEntity.addAbstractAttribute(floatAtribute);
 			//Step 4
 			EntityInterface editedEntity = EntityManagerInterface.persistEntity(savedEntity);
 
 			Map dataValue = new HashMap();
 
 			//          dataValue.put(floatAtribute, "15.90");
 			//          entityManagerInterface.insertData(editedEntity, dataValue);
 			//
 			//          dataValue.put(floatAtribute, "16.90");
 			//          entityManagerInterface.insertData(editedEntity, dataValue);
 			//
 			//          Long id = new EntityManagerUtil().getNextIdentifier(editedEntity.getTableProperties().getName());
 			//          System.out.println(id);
 
 			//Edit entity
 			AttributeInterface floatAtribute1 = DomainObjectFactory.getInstance()
 					.createFloatAttribute();
 			floatAtribute.setName("NewPrice");
 			editedEntity.addAbstractAttribute(floatAtribute1);
 
 			assertEquals(noOfDefaultColumns + 1, getColumnCount("select * from "
 					+ editedEntity.getTableProperties().getName()));
 
 			int rowCount = (Integer) executeQuery("select count(*) from "
 					+ editedEntity.getTableProperties().getName(), INT_TYPE, 1);
 			assertEquals(0, rowCount);
 
 			//Step 5
 			EntityInterface newEditedEntity = EntityManagerInterface.persistEntity(editedEntity);
 			dataValue.put(floatAtribute1, "21");
 			EntityManagerInterface.insertData(newEditedEntity, dataValue);
 			//Step 6
 
 			assertEquals(noOfDefaultColumns + 2, getColumnCount("select * from "
 					+ editedEntity.getTableProperties().getName()));
 
 			rowCount = (Integer) executeQuery("select count(*) from "
 					+ editedEntity.getTableProperties().getName(), INT_TYPE, 1);
 			assertEquals(1, rowCount);
 
 			EntityManagerInterface.insertData(newEditedEntity, dataValue);
 			rowCount = (Integer) executeQuery("select count(*) from "
 					+ editedEntity.getTableProperties().getName(), INT_TYPE, 1);
 			assertEquals(2, rowCount);
 		}
 		catch (DynamicExtensionsSystemException e)
 		{
 			e.printStackTrace();
 			fail();
 		}
 		catch (DynamicExtensionsApplicationException e)
 		{
 			e.printStackTrace();
 			fail();
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			fail();
 		}
 	}
 
 	/**
 	 * PURPOSE : To test the whether editing of an existing entity (removing an attribute) works properly or not.
 	 * EXPECTED BEHAVIOR : Changes in the existing entity should be stored properly and the changes made to the
 	 * attributes of the entity should get reflected properly in the data tables.
 	 * TEST CASE FLOW :
 	 * 1. Create entity with some attributes
 	 * 2. Save entity using entity manager.
 	 * 3. Remove an attribute from the entity
 	 * 4. Save the entity again.
 	 * 5. Check whether the removed attribute is not present in the entity
 	 * 6. Check whether a column is removed or not  from the data table of the entity.
 	 */
 	public void testEditEntityRemoveAttribute()
 	{
 		EntityGroupInterface entityGroup = DomainObjectFactory.getInstance().createEntityGroup();
 		entityGroup.setName("test_" + new Double(Math.random()).toString());
 		//Step 1
 		Entity entity = (Entity) createAndPopulateEntity();
 		entity.setName("Stock Quote");
 		//EntityManagerInterface entityManagerInterface = EntityManager.getInstance();
 		EntityManagerInterface EntityManagerInterface = EntityManager.getInstance();
 
 		try
 		{
 			AttributeInterface floatAtribute = DomainObjectFactory.getInstance()
 					.createFloatAttribute();
 			floatAtribute.setName("Price");
 			AttributeInterface commentsAttributes = DomainObjectFactory.getInstance()
 					.createStringAttribute();
 			commentsAttributes.setName("comments");
 
 			entity.addAbstractAttribute(floatAtribute);
 			entity.addAbstractAttribute(commentsAttributes);
 			entityGroup.addEntity(entity);
 			entity.setEntityGroup(entityGroup);
 			//Step 2
 			EntityInterface savedEntity = EntityManagerInterface.persistEntity(entity);
 
 			//          Map dataValue = new HashMap();
 			//          dataValue.put(floatAtribute, "15.90");
 			//          entityManagerInterface.insertData(savedEntity, dataValue);
 
 			assertEquals(getColumnCount("select * from "
 					+ savedEntity.getTableProperties().getName()), noOfDefaultColumns + 2);
 
 			AttributeInterface savedFloatAtribute = null;
 			//Collection collection = savedEntity.getAttributeCollection();
 			//collection =  EntityManagerUtil.filterSystemAttributes(collection);
 			//Iterator itr = collection.iterator();
 
 			savedFloatAtribute = savedEntity.getAttributeByIdentifier(floatAtribute.getId());
 			System.out.println("id is: " + savedFloatAtribute.getId());
 
 			//remove attribute
 			//Step 3
 			savedEntity.removeAbstractAttribute(savedFloatAtribute);
 			//Step 4
 			EntityInterface editedEntity = EntityManagerInterface.persistEntity(savedEntity);
 			//Step 6
 
 			assertEquals(noOfDefaultColumns + 1, getColumnCount("select * from "
 					+ editedEntity.getTableProperties().getName()));
 		}
 		catch (DynamicExtensionsSystemException e)
 		{
 			fail();
 			Logger.out.debug(e.getStackTrace());
 		}
 		catch (DynamicExtensionsApplicationException e)
 		{
 			e.printStackTrace();
 			Logger.out.debug(e.getStackTrace());
 			fail();
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			fail();
 			Logger.out.debug(e.getStackTrace());
 		}
 	}
 
 	/**
 	 * PURPOSE : To test the behavior of entity manager when an entity with null attribute is tried for saving.
 	 * EXPECTED BEHAVIOR : Entity manager should throw an application exception with proper error code.
 	 * TEST CASE FLOW :
 	 * 1. Create entity with null attribute
 	 * 2. Save entity using entity manager.
 	 * 3. Check whether the application exception is thrown or not.
 	 */
 	public void testCreateEnityWithNullAttribute()
 	{
 		try
 		{
 			//Step 1
 			Entity entity = (Entity) createAndPopulateEntity();
 			EntityGroup entityGroup = (EntityGroup) DomainObjectFactory.getInstance()
 					.createEntityGroup();
 			entityGroup.setName("testGroup" + new Double(Math.random()).toString());
 			entity.addAbstractAttribute(null);
 			EntityManagerUtil.addIdAttribute(entity);
 			entity.setEntityGroup(entityGroup);
 			entityGroup.addEntity(entity);
 			//EntityManagerInterface entityManagerInterface = EntityManager.getInstance();
 			EntityManagerInterface EntityManagerInterface = EntityManager.getInstance();
 			//Step 2
 			EntityManagerInterface.validateEntity(entity);
 			fail();
 		}
 		catch (DynamicExtensionsApplicationException e)
 		{
 			//Step 3
 			Logger.out
 					.debug("Exception should occur coz the attribute is null.. validation should fail");
 		}
 		catch (DynamicExtensionsSystemException e)
 		{
 			fail();
 			Logger.out.debug(e.getStackTrace());
 		}
 	}
 
 	/**
 	 * PURPOSE : To test whether the created date is set on saving of entity or not.
 	 * EXPECTED BEHAVIOR : Before saving the created date is null. After the entity is saved created date should be set.
 	 * TEST CASE FLOW :
 	 * 1. Create entity with null created date
 	 * 2. Save entity using entity manager.
 	 * 3. Check whether the created date is set or not.
 	 */
 	public void testCreateEntityNull()
 	{
 		EntityGroup entityGroup = (EntityGroup) DomainObjectFactory.getInstance()
 				.createEntityGroup();
 		entityGroup.setName("testGroup" + new Double(Math.random()).toString());
 		//Step 1
 		Entity entity = (Entity) createAndPopulateEntity();
 		entity.setName("test");
 		entityGroup.addEntity(entity);
 		entity.setEntityGroup(entityGroup);
 		EntityManagerInterface EntityManagerInterface = EntityManager.getInstance();
 
 		//EntityManagerInterface entityManagerInterface = EntityManager.getInstance();
 
 		try
 		{
 			//Step 2
 			EntityInterface savedEntity = EntityManagerInterface.persistEntity(entity);
 			assertEquals(savedEntity.getName(), entity.getName());
 
 			String tableName = entity.getTableProperties().getName();
 			String query = "Select * from " + tableName;
 			getColumnCount(query);
 
 			//Step 3
 			assertNotNull(savedEntity.getCreatedDate());
 		}
 		catch (DynamicExtensionsSystemException e)
 		{
 			fail();
 			Logger.out.debug(e.getStackTrace());
 		}
 		catch (DynamicExtensionsApplicationException e)
 		{
 			fail();
 			Logger.out.debug(e.getStackTrace());
 		}
 	}
 
 	/**
 	 * PURPOSE : To test the behavior when attribute type is changed while editing the entity.
 	 * EXPECTED BEHAVIOR : Change in the attribute type should be stored properly. Also the changes in the data type
 	 * of the column due to the change in attribute type is reflected properly in the data table or not.
 	 * 1. Create entity with integer attribute.
 	 * 2. Save entity using entity manager.
 	 * 3. Check whether the associated column type is numeric or not.
 	 * 4. Change the attribute type to string.
 	 * 5. Save the entity again.
 	 * 6. Check whether the associated column type is changed or not.
 	 */
 	public void testEditAttributeTypeChange()
 	{
 		EntityGroup entityGroup = (EntityGroup) DomainObjectFactory.getInstance()
 				.createEntityGroup();
 		entityGroup.setName("testGroup" + new Double(Math.random()).toString());
 		Entity entity;
 		EntityManagerInterface EntityManagerInterface = EntityManager.getInstance();
 		String appName=DynamicExtensionDAO.getInstance().getAppName();
 		String dyType=DAOConfigFactory.getInstance().getDAOFactory(appName).getDataBaseType();
 		try
 		{
 			//Step 1
 			entity = (Entity) createAndPopulateEntity();
 			entityGroup.addEntity(entity);
 			entity.setEntityGroup(entityGroup);
 			AttributeInterface ssn = DomainObjectFactory.getInstance().createIntegerAttribute();
 			ssn.setName("SSN of participant");
 			entity.addAbstractAttribute(ssn);
 			entity.setName("test");
 			//Step 2
 			entity = (Entity) EntityManagerInterface.persistEntity(entity);
 
 			//Step 3
 			if (dyType
 					.equals(edu.common.dynamicextensions.util.global.DEConstants.MYSQL_DATABASE))
 			{
 				assertEquals(getColumntype(
 						"select * from " + entity.getTableProperties().getName(), 3), Types.INTEGER);
 			}
 			else if (dyType
 					.equals(edu.common.dynamicextensions.util.global.DEConstants.DB2_DATABASE))
 			{
 				assertEquals(getColumntype(
 						"select * from " + entity.getTableProperties().getName(), 3), Types.DECIMAL);
 			}
 			else
 			{
 				assertEquals(getColumntype(
 						"select * from " + entity.getTableProperties().getName(), 3), Types.NUMERIC);
 			}
 
 			//Step 4
 			AttributeTypeInformationInterface stringAttributeType = new StringAttributeTypeInformation();
 			ssn.setAttributeTypeInformation(stringAttributeType);
 			//Step 5
 			//entity = (Entity) EntityManager.getInstance().persistEntity(entity);
 			entity = (Entity) EntityManagerInterface.persistEntity(entity);
 
 			//Step 6
 			if (dyType
 					.equals(edu.common.dynamicextensions.util.global.DEConstants.MYSQL_DATABASE))
 			{
 
 				assertEquals(getColumntype(
 						"select * from " + entity.getTableProperties().getName(), 3),
 						Types.LONGVARCHAR);
 			}
 			else
 			{
 				assertEquals(getColumntype(
 						"select * from " + entity.getTableProperties().getName(), 3), Types.VARCHAR);
 			}
 		}
 		catch (DynamicExtensionsApplicationException e)
 		{
 			fail();
 			e.printStackTrace();
 		}
 		catch (DynamicExtensionsSystemException e)
 		{
 			fail();
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * PURPOSE : To check the behavior when user tries modify data type of the attribute,
 	 * when data is present for that column.
 	 * EXPECTED BEHAVIOR : for oracle it should throw exception.for mysql  it works.
 	 */
 	public void testEditAttributeTypeChangeDataExists()
 	{
 		Entity entity = null;
 		EntityManagerInterface EntityManagerInterface = EntityManager.getInstance();
 		EntityGroup entityGroup = (EntityGroup) DomainObjectFactory.getInstance()
 				.createEntityGroup();
 		entityGroup.setName("testGroup" + new Double(Math.random()).toString());
 		try
 		{
 			entity = (Entity) createAndPopulateEntity();
 			entityGroup.addEntity(entity);
 			entity.setEntityGroup(entityGroup);
 			AttributeInterface ssn = DomainObjectFactory.getInstance().createIntegerAttribute();
 			ssn.setName("SSN of participant");
 			entity.addAbstractAttribute(ssn);
 			entity.setName("test");
 			entity = (Entity) EntityManagerInterface.persistEntity(entity);
 
 			Map dataValue = new HashMap();
 			dataValue.put(ssn, 101202);
 
 			EntityManagerInterface.insertData(entity, dataValue);
 
 			AttributeTypeInformationInterface dateAttributeType = new StringAttributeTypeInformation();
 			ssn.setAttributeTypeInformation(dateAttributeType);
 
 			entity = (Entity) EntityManager.getInstance().persistEntity(entity);
 		}
 		catch (DynamicExtensionsApplicationException e)
 		{
 			e.printStackTrace();
 		}
 		catch (DynamicExtensionsSystemException e)
 		{
 			fail();
 			e.printStackTrace();
 			/*   assertTrue("Data exists so can't modify its data type",true);
 			 ResultSetMetaData metaData = executeQueryForMetadata("select * from "
 			 + entity.getTableProperties().getName());
 
 			 try
 			 {
 			 assertEquals(metaData.getColumnType(2),Types.NUMERIC);
 			 }
 			 catch (SQLException e1)
 			 {
 			 fail();
 			 e1.printStackTrace();
 			 }
 			 e.printStackTrace();*/
 		}
 	}
 
 	/**
 	 * PURPOSE : To test the method persistEntityGroup
 	 * EXPECTED BEHAVIOR : The new entity group should be stored correctly and should be retrieved back correctly.
 	 * 1. Create entity group.
 	 * 2. Save entityGroup using entity manager.
 	 * 3. Check whether the saved entity group is retrieved back properly or not.
 	 */
 	/*public void testCreateEntityGroup()
 	{
 		try
 		{
 			EntityGroupManagerInterface entityGroupManager = EntityGroupManager.getInstance();
 			//Step 1
 			EntityGroup entityGroup = (EntityGroup) new MockEntityManager().initializeEntityGroup();
 			//Step 2
 			entityGroupManager.persistEntityGroup(entityGroup);
 			//Step 3
 			Collection collection = ((EntityManager) EntityManager.getInstance()).getAllObjects(EntityGroupInterface.class.getName());
 			assertTrue(collection.contains(entityGroup));
 		}
 		catch (Exception e)
 		{
 			Logger.out.debug(e.getMessage());
 			e.printStackTrace();
 			fail("Exception occured");
 		}
 	}*/
 
 	/**
 	 * PURPOSE : To test the method getEntityGroupByName
 	 * EXPECTED BEHAVIOR : The new entity group should be stored correctly and should be retrieved back correctly
 	 * if the name of that entity group is given.
 	 * TEST CASE FLOW :
 	 * 1. Create entity group.
 	 * 2. Save entityGroup using entity manager.
 	 * 3. Check whether the saved entity group is retrieved back properly or not given it's name.
 	 */
 	public void testGetEntityGroupByName()
 	{
 		try
 		{
 			//Step 1
 			EntityGroupManagerInterface entityGroupManager = EntityGroupManager.getInstance();
 			//Step 2
 			EntityGroup entityGroup = (EntityGroup) new MockEntityManager().initializeEntityGroup();
 			entityGroupManager.persistEntityGroup(entityGroup);
 			//Step 3
 			EntityGroupInterface entityGroupInterface = entityGroupManager
 					.getEntityGroupByName(entityGroup.getName());
 			assertNotNull(entityGroupInterface);
 		}
 		catch (Exception e)
 		{
 			Logger.out.debug(e.getMessage());
 			e.printStackTrace();
 			fail("Exception occured");
 		}
 	}
 
 	/**
 	 * PURPOSE : To test the method getEntitiesByConceptCode
 	 * EXPECTED BEHAVIOR : All the entities that are associated with the given concept code should be retrieved back
 	 * correctly
 	 * TEST CASE FLOW :
 	 * 1. Create entity with some concept code
 	 * 2. Save entity using entity manager.
 	 * 3. Check whether the saved entity is retrieved back properly or not given it's concept code.
 	 */
 	public void testGetEntitiesByConceptCode()
 	{
 		EntityGroup entityGroup = (EntityGroup) DomainObjectFactory.getInstance()
 				.createEntityGroup();
 		entityGroup.setName("testGroup" + new Double(Math.random()).toString());
 		EntityManagerInterface EntityManagerInterface = EntityManager.getInstance();
 		try
 		{
 			//Step 1
 			Entity entity = (Entity) new MockEntityManager().initializeEntity(entityGroup);
 			SemanticPropertyInterface semanticPropertyInterface = (SemanticPropertyInterface) new MockEntityManager()
 					.initializeSemanticProperty();
 			entity.addSemanticProperty(semanticPropertyInterface);
 			//Step 2
 			entity = (Entity) EntityManagerInterface.persistEntity(entity);
 
 			//Step 3
 			Collection entityCollection = (Collection) EntityManagerInterface
 					.getEntitiesByConceptCode(semanticPropertyInterface.getConceptCode());
 			assertTrue(entityCollection != null && entityCollection.size() > 0);
 		}
 		catch (Exception e)
 		{
 			Logger.out.debug(e.getMessage());
 			fail("Exception occured");
 		}
 	}
 
 	/**
 	 * PURPOSE : To test the method getEntityGroupByName
 	 * EXPECTED BEHAVIOR : The new entity group should be stored correctly and should be retrieved back correctly
 	 * if the name of that entity group is given.
 	 * TEST CASE FLOW :
 	 * 1. Create entity group.
 	 * 2. Save entityGroup using entity manager.
 	 * 3. Check whether the saved entity group is retrieved back properly or not given it's name.
 	 */
 	public void testGetEntityByName()
 	{
 		EntityManagerInterface EntityManagerInterface = EntityManager.getInstance();
 		try
 		{
 			EntityGroup entityGroup = (EntityGroup) DomainObjectFactory.getInstance()
 					.createEntityGroup();
 			entityGroup.setName("testGroup" + new Double(Math.random()).toString());
 			//Step 1
 			Entity entity = (Entity) new MockEntityManager().initializeEntity(entityGroup);
 			//Step 2
 			entity = (Entity) EntityManagerInterface.persistEntity(entity);
 			//Step 3
 			EntityInterface entityInterface = (EntityInterface) EntityManagerInterface
 					.getEntityByName(entity.getName());
 			assertNotNull(entityInterface);
 		}
 		catch (Exception e)
 		{
 			Logger.out.debug(e.getMessage());
 			fail("Exception occured");
 		}
 	}
 
 	/**
 	 * This method tests GetAttribute method.
 	 *
 	 */
 	public void testGetAttribute()
 	{
 		EntityManagerInterface EntityManagerInterface = EntityManager.getInstance();
 		try
 		{
 			DomainObjectFactory factory = DomainObjectFactory.getInstance();
 			EntityGroup entityGroup = (EntityGroup) factory.createEntityGroup();
 			entityGroup.setName("testGroup" + new Double(Math.random()).toString());
 			// create user
 			EntityInterface user = createAndPopulateEntity();
 			AttributeInterface userNameAttribute = factory.createStringAttribute();
 			userNameAttribute.setName("user name");
 			user.setName("user");
 			user.addAbstractAttribute(userNameAttribute);
 			entityGroup.addEntity(user);
 			user.setEntityGroup(entityGroup);
 			user = (Entity) EntityManagerInterface.persistEntity(user);
 
 			AttributeInterface attributeInterface = (AttributeInterface) EntityManagerInterface
 					.getAttribute("user", "user name");
 			assertNotNull(attributeInterface);
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			Logger.out.debug(e.getMessage());
 			fail("Exception occured");
 		}
 
 	}
 
 	/**
 	 * Test case to check the behaviour when first entity is saved with a collection attribute and then the that
 	 * attribute is made non-collection. Expected behavior is that after editing the attribute in
 	 * such a way the column for that attribute should get added to the data table. This column was not present in
 	 * earlier scenario when the attribute was a collection attribute.
 	 */
 	public void testEditEntityWithCollectionAttribute()
 	{
 		EntityManagerInterface EntityManagerInterface = EntityManager.getInstance();
 		try
 		{
 			DomainObjectFactory factory = DomainObjectFactory.getInstance();
 
 			EntityGroup entityGroup = (EntityGroup) factory.createEntityGroup();
 			entityGroup.setName("testGroup" + new Double(Math.random()).toString());
 
 			EntityInterface user = createAndPopulateEntity();
 			EntityManagerUtil.addIdAttribute(user);
 
 			// Step 2
 			EntityInterface address = createAndPopulateEntity();
 			EntityManagerUtil.addIdAttribute(address);
 			address.setName("address");
 
 			AttributeInterface streetAttribute = factory.createStringAttribute();
 			streetAttribute.setName("street name");
 			address.addAbstractAttribute(streetAttribute);
 
 			// Step 3
 			AssociationInterface association = factory.createAssociation();
 			association.setTargetEntity(address);
 			association.setAssociationDirection(AssociationDirection.SRC_DESTINATION);
 			association.setName("UserAddress");
 			association.setSourceRole(getRole(AssociationType.ASSOCIATION, "User",
 					Cardinality.ZERO, Cardinality.ONE));
 			association.setTargetRole(getRole(AssociationType.ASSOCIATION, "address",
 					Cardinality.ZERO, Cardinality.MANY));
 			association.setIsCollection(new Boolean(true));
 
 			user.addAbstractAttribute(association);
 			DynamicExtensionsUtility.getConstraintPropertiesForAssociation(association);
 			entityGroup.addEntity(user);
 			user.setEntityGroup(entityGroup);
 			entityGroup.addEntity(address);
 			address.setEntityGroup(entityGroup);
 
 			user = (Entity) EntityManagerInterface.persistEntity(user);
 
 			Entity savedEntity = (Entity) EntityManagerInterface.getEntityByIdentifier(user.getId()
 					.toString());
 			assertEquals(user.getName(), savedEntity.getName());
 
 			Map dataValue = new HashMap();
 			List dataList = new ArrayList();
 
 			Map dataMapFirst = new HashMap();
 			dataMapFirst.put(streetAttribute, "xyz");
 			dataList.add(dataMapFirst);
 
 			Map dataMapSecond = new HashMap();
 			dataMapSecond.put(streetAttribute, "abc");
 			dataList.add(dataMapSecond);
 
 			//dataValue.put(commentsAttributes, "this is not default comment");
 			dataValue.put(association, dataList);
 
 			Long recordId = EntityManagerInterface.insertData(savedEntity, dataValue);
 
 			Map map = EntityManagerInterface.getRecordById(savedEntity, recordId);
 
 			System.out.println(map);
 
 			dataMapFirst.put(streetAttribute, "rajesh");
 
 			EntityManagerInterface.editData(savedEntity, dataValue, recordId);
 
 			map = EntityManagerInterface.getRecordById(savedEntity, recordId);
 
 			System.out.println(map);
 
 			//			String tableName = newEntity.getTableProperties().getName();
 			//			System.out.println("===========table name " + tableName);
 			//			assertTrue(isTablePresent(tableName));
 			//			ResultSetMetaData metaData = executeQueryForMetadata("select * from " + tableName);
 			//			assertEquals(metaData.getColumnCount(), noOfDefaultColumns);
 			//
 			//			userNameAttribute = (AttributeInterface) newEntity.getAttributeByIdentifier(userNameAttribute.getId());
 			//			userNameAttribute.setIsCollection(false);
 			//
 			//			newEntity = (Entity) EntityManagerInterface.persistEntity(newEntity);
 			//
 			//			tableName = newEntity.getTableProperties().getName();
 			//			assertTrue(isTablePresent(tableName));
 			//			metaData = executeQueryForMetadata("select * from " + tableName);
 			//			assertEquals(metaData.getColumnCount(), noOfDefaultColumns + 1);
 			//
 			//			userNameAttribute = (AttributeInterface) newEntity.getAttributeByIdentifier(userNameAttribute.getId());
 			//			userNameAttribute.setIsCollection(true);
 			//
 			//			newEntity = (Entity) EntityManagerInterface.persistEntity(newEntity);
 			//
 			//			tableName = newEntity.getTableProperties().getName();
 			//			assertTrue(isTablePresent(tableName));
 			//			metaData = executeQueryForMetadata("select * from " + tableName);
 			//			assertEquals(metaData.getColumnCount(), noOfDefaultColumns);
 		}
 		catch (Exception e)
 		{
 			Logger.out.debug(e.getMessage());
 			fail("Exception occured");
 		}
 	}
 
 	/**
 	 * This method tests GetRecordById method for the condition where record and entity does exists
 	 */
 
 	public void testDeleteRecordById()
 	{
 		EntityManagerInterface EntityManagerInterface = EntityManager.getInstance();
 		try
 		{
 			EntityGroup entityGroup = (EntityGroup) DomainObjectFactory.getInstance()
 					.createEntityGroup();
 			entityGroup.setName("testGroup" + new Double(Math.random()).toString());
 			Entity entity = (Entity) new MockEntityManager().initializeEntity(entityGroup);
 			entity = (Entity) EntityManagerInterface.persistEntity(entity);
 
 			Entity newEntity = (Entity) EntityManagerInterface.getEntityByIdentifier(entity.getId()
 					.toString());
 			Map dataValue = new HashMap();
 			Collection collection = newEntity.getAttributeCollection();
 			collection = EntityManagerUtil.filterSystemAttributes(collection);
 			Iterator attrIterator = collection.iterator();
 			int i = 0;
 			while (attrIterator.hasNext())
 			{
 				AttributeInterface attribute = (AttributeInterface) attrIterator.next();
 				AttributeTypeInformationInterface typeInfo = attribute
 						.getAttributeTypeInformation();
 
 				if (typeInfo instanceof StringAttributeTypeInformation)
 				{
 					dataValue.put(attribute, "temp" + i);
 				}
 				else if (attribute instanceof DateAttributeTypeInformation)
 				{
 					dataValue.put(attribute, "11-01-2006");
 				}
 
 				i++;
 			}
 
 			EntityManagerInterface.insertData(newEntity, dataValue);
 			int column = (Integer) executeQuery("select count(*) from "
 					+ newEntity.getTableProperties().getName(), INT_TYPE, 1);
 
 			assertEquals(1, column);
 
 			assertEquals("Person", entity.getName());
 			boolean isRecordDeleted = EntityManagerInterface.deleteRecord(entity, new Long(1));
 			assertTrue(isRecordDeleted);
 
 			assertEquals(Status.ACTIVITY_STATUS_DISABLED.toString(), getActivityStatus(entity, 1L));
 		}
 		catch (DynamicExtensionsSystemException e)
 		{
 			Logger.out.debug(e.getMessage());
 			fail("Exception occured");
 		}
 		catch (DynamicExtensionsApplicationException e)
 		{
 			Logger.out.debug(e.getMessage());
 			fail("Exception occured");
 		}
 		catch (Exception e)
 		{
 			Logger.out.debug(e.getMessage());
 			fail("Exception occured");
 		}
 	}
 
 	/**
 	 * This method test for inserting data for a multi select attribute
 	 */
 	/*public void testInsertDataWithMultiSelectAttribute()
 	{
 	    EntityGroup entityGroup = (EntityGroup) DomainObjectFactory.getInstance().createEntityGroup();
 	    entityGroup.setName("testGroup"+ new Double(Math.random()).toString());
 	    Entity entity = (Entity) createAndPopulateEntity();
 	    entity.setName("Stock Quote");
 	    EntityManagerInterface EntityManagerInterface = EntityManager.getInstance();
 
 	    DomainObjectFactory factory = DomainObjectFactory.getInstance();
 
 	    try
 	    {
 	        //Edit entity
 	        AttributeInterface floatAtribute = factory.createFloatAttribute();
 	        floatAtribute.setName("Price");
 
 	        AttributeInterface commentsAttributes = factory.createStringAttribute();
 	        commentsAttributes.setName("comments");
 	        commentsAttributes.setIsCollection(true);
 
 	        entity.addAbstractAttribute(floatAtribute);
 	        entity.addAbstractAttribute(commentsAttributes);
 	        entityGroup.addEntity(entity);
 	        entity.setEntityGroup(entityGroup);
 	        EntityInterface savedEntity = EntityManagerInterface.persistEntity(entity);
 
 	        Map dataValue = new HashMap();
 	        dataValue.put(floatAtribute, "15.90");
 	        List<String> commentsValues = new ArrayList<String>();
 	        commentsValues.add("comments1");
 	        commentsValues.add("comments2");
 	        commentsValues.add("comments3");
 	        commentsValues.add("comments4");
 
 	        dataValue.put(commentsAttributes, commentsValues);
 
 	        EntityManagerInterface.insertData(savedEntity, dataValue);
 
 	        ResultSet resultSet = executeQuery("select * from "
 	                + savedEntity.getTableProperties().getName());
 	        resultSet.next();
 	        assertEquals(1, resultSet.getInt(3));
 	        ResultSetMetaData metadata = resultSet.getMetaData();
 	        assertEquals(metadata.getColumnCount(), noOfDefaultColumns + 1);
 
 	        boolean isRecordDeleted = EntityManagerInterface.deleteRecord(savedEntity, new Long(1));
 	        assertTrue(isRecordDeleted);
 
 	        assertEquals(Status.ACTIVITY_STATUS_DISABLED.toString(), getActivityStatus(entity, 1L));
 	    }
 	    catch (DynamicExtensionsSystemException e)
 	    {
 	        fail();
 	        Logger.out.debug(e.getStackTrace());
 	    }
 	    catch (DynamicExtensionsApplicationException e)
 	    {
 	        fail();
 	        Logger.out.debug(e.getStackTrace());
 	    }
 	    catch (Exception e)
 	    {
 	        e.printStackTrace();
 	        fail();
 
 	        Logger.out.debug(e.getStackTrace());
 	    }
 	}*/
 
 	/**
 	 * This method test for updating record for an entity.
 	 */
 	public void testEditRecord()
 	{
 		EntityGroup entityGroup = (EntityGroup) DomainObjectFactory.getInstance()
 				.createEntityGroup();
 		entityGroup.setName("testGroup" + new Double(Math.random()).toString());
 		Entity study = (Entity) createAndPopulateEntity();
 		study.setName("Study");
 		EntityManagerInterface EntityManagerInterface = EntityManager.getInstance();
 
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 
 		try
 		{
 			AttributeInterface name = factory.createStringAttribute();
 			name.setName("Study name");
 
 			AttributeInterface userNames = factory.createStringAttribute();
 			userNames.setName("users");
 
 			AttributeInterface studyDate = factory.createDateAttribute();
 			((DateAttributeTypeInformation) studyDate.getAttributeTypeInformation())
 					.setFormat(ProcessorConstants.SQL_DATE_ONLY_FORMAT);
 			studyDate.setName("Date");
 
 			study.addAbstractAttribute(name);
 			study.addAbstractAttribute(userNames);
 			study.addAbstractAttribute(studyDate);
 			entityGroup.addEntity(study);
 			study.setEntityGroup(entityGroup);
 			EntityInterface savedStudy = EntityManagerInterface.persistEntity(study);
 
 			Map dataValue = new HashMap();
 
 			dataValue.put(name, "Java Study");
 			dataValue.put(userNames, "a");
 			dataValue.put(studyDate, "11-02-2006");
 
 			Long recordId = EntityManagerInterface.insertData(savedStudy, dataValue);
 
 			Map map = EntityManagerInterface.getRecordById(savedStudy, recordId);
 
 			String userName = (String) map.get(userNames);
 			assertEquals("a", userName);
 
 			dataValue.put(userNames, "b");
 			dataValue.put(studyDate, "12-02-2006");
 
 			EntityManagerInterface.editData(savedStudy, dataValue, recordId);
 
 			map = EntityManagerInterface.getRecordById(savedStudy, recordId);
 			userName = (String) map.get(userNames);
 			assertEquals("b", userName);
 			assertEquals("12-02-2006", map.get(studyDate));
 		}
 		catch (DynamicExtensionsSystemException e)
 		{
 			fail();
 			e.printStackTrace();
 			Logger.out.debug(e.getStackTrace());
 		}
 		catch (DynamicExtensionsApplicationException e)
 		{
 			fail();
 			e.printStackTrace();
 			Logger.out.debug(e.getStackTrace());
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			fail();
 			Logger.out.debug(e.getStackTrace());
 		}
 	}
 
 	/**
 	 * This method test for updating record for an entity.
 	 */
 	public void testEditRecordForContainmentAssociation()
 	{
 		EntityManagerInterface EntityManagerInterface = EntityManager.getInstance();
 		try
 		{
 			DomainObjectFactory factory = DomainObjectFactory.getInstance();
 
 			EntityGroup entityGroup = (EntityGroup) factory.createEntityGroup();
 			entityGroup.setName("testGroup" + new Double(Math.random()).toString());
 
 			EntityInterface user = createAndPopulateEntity();
 			EntityManagerUtil.addIdAttribute(user);
 			user.setName("user");
 
 			EntityInterface userGroup = createAndPopulateEntity();
 			EntityManagerUtil.addIdAttribute(userGroup);
 			userGroup.setName("userGroup");
 
 			// Step 2
 			EntityInterface address = createAndPopulateEntity();
 			EntityManagerUtil.addIdAttribute(address);
 			address.setName("address");
 
 			AttributeInterface streetAttribute = factory.createStringAttribute();
 			streetAttribute.setName("street name");
 			address.addAbstractAttribute(streetAttribute);
 
 			// Step 3
 			AssociationInterface association = factory.createAssociation();
 			association.setTargetEntity(address);
 			association.setAssociationDirection(AssociationDirection.SRC_DESTINATION);
 			association.setName("UserAddress");
 			association.setSourceRole(getRole(AssociationType.CONTAINTMENT, "User",
 					Cardinality.ZERO, Cardinality.ONE));
 			association.setTargetRole(getRole(AssociationType.CONTAINTMENT, "address",
 					Cardinality.ZERO, Cardinality.MANY));
 
 			// Step 3
 			AssociationInterface associationUserGroup = factory.createAssociation();
 			associationUserGroup.setTargetEntity(address);
 			associationUserGroup.setAssociationDirection(AssociationDirection.SRC_DESTINATION);
 			associationUserGroup.setName("associationUserGroup");
 			associationUserGroup.setSourceRole(getRole(AssociationType.CONTAINTMENT, "userGroup",
 					Cardinality.ZERO, Cardinality.ONE));
 			associationUserGroup.setTargetRole(getRole(AssociationType.CONTAINTMENT, "address",
 					Cardinality.ZERO, Cardinality.MANY));
 
 			user.addAbstractAttribute(association);
 			DynamicExtensionsUtility.getConstraintPropertiesForAssociation(association);
 			entityGroup.addEntity(user);
 			user.setEntityGroup(entityGroup);
 
 			userGroup.addAbstractAttribute(associationUserGroup);
 			DynamicExtensionsUtility.getConstraintPropertiesForAssociation(associationUserGroup);
 			entityGroup.addEntity(userGroup);
 			userGroup.setEntityGroup(entityGroup);
 
 			entityGroup.addEntity(address);
 			address.setEntityGroup(entityGroup);
 
 			user = (Entity) EntityManagerInterface.persistEntity(user);
 
 			userGroup = (Entity) EntityManagerInterface.persistEntity(userGroup);
 
 			Entity savedEntity = (Entity) EntityManagerInterface.getEntityByIdentifier(user.getId()
 					.toString());
 			assertEquals(user.getName(), savedEntity.getName());
 
 			Map dataValue = new HashMap();
 			Map newDataValue = new HashMap();
 			List dataList = new ArrayList();
 			List newDataList = new ArrayList();
 
 			Map dataMapFirst = new HashMap();
 			dataMapFirst.put(streetAttribute, "xyz");
 			dataList.add(dataMapFirst);
 			newDataList.add(dataMapFirst);
 
 			Map dataMapSecond = new HashMap();
 			dataMapSecond.put(streetAttribute, "abc");
 			dataList.add(dataMapSecond);
 			newDataList.add(dataMapSecond);
 
 			//dataValue.put(commentsAttributes, "this is not default comment");
 			dataValue.put(association, dataList);
 
 			newDataValue.put(associationUserGroup, newDataList);
 
 			Long recordId = EntityManagerInterface.insertData(savedEntity, dataValue);
 
 			Long newRecordId = EntityManagerInterface.insertData(userGroup, newDataValue);
 
 			Map map = EntityManagerInterface.getRecordById(savedEntity, recordId);
 
 			System.out.println(map);
 
 			dataMapFirst.put(streetAttribute, "rajesh");
 
 			EntityManagerInterface.editData(savedEntity, dataValue, recordId);
 
 			map = EntityManagerInterface.getRecordById(savedEntity, recordId);
 
 			System.out.println(map);
 		}
 		catch (Exception e)
 		{
 			Logger.out.debug(e.getMessage());
 			e.printStackTrace();
 			fail("Exception occured");
 		}
 	}
 
 	/**
 	 * This method test for updating data for a multiSelect attribute
 	 */
 	/* public void testEditRecordWithMultiselectAttrubuteUpdate()
 	 {
 	     EntityGroup entityGroup = (EntityGroup) DomainObjectFactory.getInstance().createEntityGroup();
 	     entityGroup.setName("testGroup"+ new Double(Math.random()).toString());
 	     Entity study = new Entity();
 	     study.setName("Study");
 	     EntityManagerInterface EntityManagerInterface = EntityManager.getInstance();
 
 	     DomainObjectFactory factory = DomainObjectFactory.getInstance();
 
 	     try
 	     {
 	         AttributeInterface name = factory.createStringAttribute();
 	         name.setName("Study name");
 
 	         AttributeInterface studyDate = factory.createDateAttribute();
 	         studyDate.setName("Date");
 
 	         AttributeInterface userNames = factory.createStringAttribute();
 	         userNames.setName("users");
 	         userNames.setIsCollection(true);
 
 	         study.addAttribute(name);
 	         study.addAttribute(userNames);
 	         study.addAttribute(studyDate);
 	         entityGroup.addEntity(study);
 	         study.setEntityGroup(entityGroup);
 	         EntityInterface savedStudy = EntityManagerInterface.persistEntity(study);
 
 	         Map dataValue = new HashMap();
 	         List<String> userNameList = new ArrayList<String>();
 	         userNameList.add("a");
 	         userNameList.add("b");
 	         userNameList.add("c");
 
 	         dataValue.put(name, "Java Study");
 	         dataValue.put(userNames, userNameList);
 	         dataValue.put(studyDate, "11-20-2006");
 
 	         Long recordId = EntityManagerInterface.insertData(savedStudy, dataValue);
 
 	         Map map = EntityManagerInterface.getRecordById(savedStudy, recordId);
 
 	         int noOfUsers = ((List) map.get(userNames)).size();
 	         assertEquals(3, noOfUsers);
 	         System.out.println(map);
 
 	         dataValue.clear();
 	         userNameList.clear();
 	         userNameList.add("d");
 
 	         dataValue.put(userNames, userNameList);
 	         dataValue.put(studyDate, "12-20-2006");
 
 	         EntityManagerInterface.editData(savedStudy, dataValue, recordId);
 
 	         map = EntityManagerInterface.getRecordById(savedStudy, recordId);
 	         noOfUsers = ((List) map.get(userNames)).size();
 	         assertEquals(1, noOfUsers);
 	         assertEquals("12-20-2006", (String) map.get(studyDate));
 
 	         System.out.println(map);
 	     }
 	     catch (DynamicExtensionsSystemException e)
 	     {
 	         fail();
 	         Logger.out.debug(e.getStackTrace());
 	     }
 	     catch (DynamicExtensionsApplicationException e)
 	     {
 	         fail();
 	         Logger.out.debug(e.getStackTrace());
 	     }
 	     catch (Exception e)
 	     {
 	         e.printStackTrace();
 	         fail();
 	         Logger.out.debug(e.getStackTrace());
 	     }
 	 }*/
 
 	/**
 	 * This method edits an existing attribute to a file type attribute.
 	 */
 	public void testEditFileAttribute()
 	{
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 		EntityGroup entityGroup = (EntityGroup) factory.createEntityGroup();
 		entityGroup.setName("testGroup" + new Double(Math.random()).toString());
 		EntityManagerInterface EntityManagerInterface = EntityManager.getInstance();
 
 		// create user
 		EntityInterface user = createAndPopulateEntity();
 		user.setEntityGroup(entityGroup);
 		entityGroup.addEntity(user);
 		user.setName("User");
 		AttributeInterface resume = factory.createStringAttribute();
 		resume.setName("Resume");
 		user.addAbstractAttribute(resume);
 
 		user.setEntityGroup(entityGroup);
 		entityGroup.addEntity(user);
 
 		try
 		{
 			int rowCount = (Integer) executeQuery("select count(*) from dyextn_file_extensions",
 					INT_TYPE, 1);
 			System.out.println(rowCount);
 
 			// save the entity
 			user = EntityManagerInterface.persistEntity(user);
 
 			assertEquals(getColumnCount("select * from " + user.getTableProperties().getName()),
 					noOfDefaultColumns + 1);
 
 			//Edit attribute: change attribute type to file attribiute type.
 			Collection<FileExtension> allowedExtn = new HashSet<FileExtension>();
 
 			FileExtension txtExtn = new FileExtension();
 			txtExtn.setFileExtension("txt");
 			allowedExtn.add(txtExtn);
 
 			FileExtension pdfExtn = new FileExtension();
 			pdfExtn.setFileExtension("pdf");
 			allowedExtn.add(pdfExtn);
 
 			allowedExtn.add(txtExtn);
 			allowedExtn.add(pdfExtn);
 
 			AttributeTypeInformation fileTypeInformation = factory
 					.createFileAttributeTypeInformation();
 
 			((FileAttributeTypeInformation) fileTypeInformation).setMaxFileSize(20F);
 			((FileAttributeTypeInformation) fileTypeInformation)
 					.setFileExtensionCollection(allowedExtn);
 
 			resume.setAttributeTypeInformation(fileTypeInformation);
 
 			user = EntityManagerInterface.persistEntity(user);
 
 			//DBUtil.closeConnection();
 			//DBUtil.Connection();
 
 			assertEquals(getColumnCount("select * from " + user.getTableProperties().getName()),
 					noOfDefaultColumnsForfile);
 
 			//executeQuery("select * from dyextn_file_extensions");
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			fail();
 		}
 	}
 
 	/**
 	 * This method tests for creating a entity with file attribute.
 	 */
 	public void testCreateFileAttribute()
 	{
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 		EntityGroup entityGroup = (EntityGroup) factory.createEntityGroup();
 		entityGroup.setName("testGroup" + new Double(Math.random()).toString());
 		EntityManagerInterface EntityManagerInterface = EntityManager.getInstance();
 
 		// create user
 		EntityInterface user = createAndPopulateEntity();
 		user.setName("User");
 		AttributeInterface resume = factory.createFileAttribute();
 		resume.setName("Resume");
 		user.addAbstractAttribute(resume);
 		user.setEntityGroup(entityGroup);
 		entityGroup.addEntity(user);
 
 		Collection<FileExtension> allowedExtn = new HashSet<FileExtension>();
 
 		FileExtension txtExtn = new FileExtension();
 		txtExtn.setFileExtension("txt");
 		allowedExtn.add(txtExtn);
 
 		FileExtension pdfExtn = new FileExtension();
 		pdfExtn.setFileExtension("pdf");
 		allowedExtn.add(pdfExtn);
 
 		allowedExtn.add(txtExtn);
 		allowedExtn.add(pdfExtn);
 
 		((FileAttributeTypeInformation) resume.getAttributeTypeInformation()).setMaxFileSize(20F);
 		((FileAttributeTypeInformation) resume.getAttributeTypeInformation())
 				.setFileExtensionCollection(allowedExtn);
 
 		try
 		{
 			int beforeCount = (Integer) executeQuery("select count(*) from dyextn_file_extensions",
 					INT_TYPE, 1);
 			user = EntityManagerInterface.persistEntity(user);
 			assertEquals(getColumnCount("select * from " + user.getTableProperties().getName()),
 					noOfDefaultColumnsForfile);
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			fail();
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void testCreateEntity()
 	{
 		EntityManagerInterface EntityManagerInterface = EntityManager.getInstance();
 
 		try
 		{
 			EntityGroup entityGroup = (EntityGroup) DomainObjectFactory.getInstance()
 					.createEntityGroup();
 			entityGroup.setName("testGroup" + new Double(Math.random()).toString());
 			Entity entity = (Entity) new MockEntityManager().initializeEntity(entityGroup);
 			TaggedValueInterface taggedValue = DomainObjectFactory.getInstance()
 					.createTaggedValue();
 			taggedValue.setKey("a");
 			taggedValue.setValue("b");
 			entity.addTaggedValue(taggedValue);
 			entity = (Entity) EntityManagerInterface.persistEntity(entity);
 
 			Entity newEntity = (Entity) EntityManagerInterface.getEntityByIdentifier(entity.getId()
 					.toString());
 			assertEquals(entity.getName(), newEntity.getName());
 
 			String tableName = entity.getTableProperties().getName();
 			String query = "Select * from " + tableName;
 			getColumnCount(query);
 		}
 		catch (Exception e)
 		{
 			Logger.out.debug(e.getMessage());
 			e.printStackTrace();
 			fail("Exception occured");
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void testCreateEntityWithEntityGroup()
 	{
 		EntityManagerInterface EntityManagerInterface = EntityManager.getInstance();
 
 		try
 		{
 			EntityGroup entityGroup = (EntityGroup) DomainObjectFactory.getInstance()
 					.createEntityGroup();
 			entityGroup.setName("testGroup" + new Double(Math.random()).toString());
 			Entity entity = (Entity) new MockEntityManager().initializeEntity(entityGroup);
 			entity = (Entity) EntityManagerInterface.persistEntity(entity);
 			Entity newEntity = (Entity) EntityManagerInterface.getEntityByIdentifier(entity.getId()
 					.toString());
 			//  Checking whether metadata information is saved properly or not.
 			assertEquals(entity.getName(), newEntity.getName());
 			//Collection collection = newEntity.getEntityGroupCollection();
 			//Iterator iter = collection.iterator();
 			EntityGroup eg = (EntityGroup) newEntity.getEntityGroup();
 			assertEquals(eg.getId(), entityGroup.getId());
 		}
 		catch (Exception e)
 		{
 			Logger.out.debug(e.getMessage());
 			fail("Exception occured");
 		}
 	}
 
 	/**
 	 * This method tests GetRecordById method for the condition where record and entity does exists
 	 */
 	public void testGetRecordById()
 	{
 		EntityManagerInterface EntityManagerInterface = EntityManager.getInstance();
 
 		try
 		{
 			EntityGroup entityGroup = (EntityGroup) DomainObjectFactory.getInstance()
 					.createEntityGroup();
 			entityGroup.setName("testGroup" + new Double(Math.random()).toString());
 			Entity entity = (Entity) new MockEntityManager().initializeEntity(entityGroup);
 			entity = (Entity) EntityManagerInterface.persistEntity(entity);
 
 			Entity newEntity = (Entity) EntityManagerInterface.getEntityByIdentifier(entity.getId()
 					.toString());
 			Map dataValue = new HashMap();
 			Collection collection = newEntity.getAttributeCollection();
 			collection = EntityManagerUtil.filterSystemAttributes(collection);
 			Iterator attrIterator = collection.iterator();
 			int i = 0;
 			while (attrIterator.hasNext())
 			{
 				AttributeInterface attribute = (AttributeInterface) attrIterator.next();
 				AttributeTypeInformationInterface typeInfo = attribute
 						.getAttributeTypeInformation();
 
 				if (typeInfo instanceof StringAttributeTypeInformation)
 				{
 					dataValue.put(attribute, "temp" + i);
 				}
 				else if (attribute instanceof DateAttributeTypeInformation)
 				{
 					dataValue.put(attribute, "11-01-2006");
 				}
 
 				i++;
 			}
 
 			EntityManagerInterface.insertData(newEntity, dataValue);
 
 			assertEquals("Person", entity.getName());
 			Map map = EntityManagerInterface.getRecordById(entity, new Long(1));
 
 			System.out.println(map);
 		}
 		catch (DynamicExtensionsSystemException e)
 		{
 			Logger.out.debug(e.getMessage());
 			fail("Exception occured");
 		}
 		catch (DynamicExtensionsApplicationException e)
 		{
 			Logger.out.debug(e.getMessage());
 			fail("Exception occured");
 		}
 	}
 
 	/**
 	 * This method tests GetRecordById method for the condition wheer record does not exists for given id.
 	 */
 	public void testGetRecordByIdNoRecord()
 	{
 		EntityManagerInterface EntityManagerInterface = EntityManager.getInstance();
 
 		try
 		{
 			EntityGroup entityGroup = (EntityGroup) DomainObjectFactory.getInstance()
 					.createEntityGroup();
 			entityGroup.setName("testGroup" + new Double(Math.random()).toString());
 			Entity entity = (Entity) new MockEntityManager().initializeEntity(entityGroup);
 			entity = (Entity) EntityManagerInterface.persistEntity(entity);
 
 			Map map = EntityManager.getInstance().getRecordById(entity, new Long(1));
 			assertEquals(0, map.size());
 		}
 		catch (DynamicExtensionsSystemException e)
 		{
 			Logger.out.debug(e.getMessage());
 			fail("Exception occured");
 		}
 		catch (DynamicExtensionsApplicationException e)
 		{
 			Logger.out.debug(e.getMessage());
 			fail("Exception occured");
 		}
 	}
 
 	/**
 	 * This method tests GetRecordById method for the condition where entity is not saved , entity  is null or record id id null
 	 */
 	public void testGetRecordByIdEntityNotSaved()
 	{
 		EntityManagerInterface EntityManagerInterface = EntityManager.getInstance();
 		Entity entity = null;
 
 		try
 		{
 			EntityGroup entityGroup = (EntityGroup) DomainObjectFactory.getInstance()
 					.createEntityGroup();
 			entityGroup.setName("testGroup" + new Double(Math.random()).toString());
 			entity = (Entity) new MockEntityManager().initializeEntity(entityGroup);
 
 			Map map = EntityManagerInterface.getRecordById(entity, new Long(1));
 			fail("Exception should have be thrown since entity is not saved");
 		}
 		catch (DynamicExtensionsSystemException e)
 		{
 			assertTrue(true);
 		}
 		catch (DynamicExtensionsApplicationException e)
 		{
 			Logger.out.debug(e.getMessage());
 			fail("Unexpected Exception occured");
 		}
 
 		try
 		{
 			Map map = EntityManagerInterface.getRecordById(null, new Long(1));
 			fail("Exception should have be thrown since entity is not saved");
 		}
 		catch (DynamicExtensionsSystemException e)
 		{
 			assertTrue(true);
 		}
 		catch (DynamicExtensionsApplicationException e)
 		{
 			Logger.out.debug(e.getMessage());
 		}
 
 		try
 		{
 			entity = (Entity) EntityManagerInterface.persistEntity(entity);
 
 			Map map = EntityManagerInterface.getRecordById(entity, null);
 			fail("Exception should have be thrown since entity is not saved");
 		}
 		catch (DynamicExtensionsSystemException e)
 		{
 			assertTrue(true);
 		}
 		catch (DynamicExtensionsApplicationException e)
 		{
 			Logger.out.debug(e.getMessage());
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void testCreateContainer()
 	{
 		EntityManagerInterface EntityManagerInterface = EntityManager.getInstance();
 
 		try
 		{
 			Container container = (Container) new MockEntityManager().getContainer("abc");
 			EntityManagerInterface.persistEntity((EntityInterface) container.getAbstractEntity());
 			Collection list = EntityManagerInterface.getAllContainers();
 			assertNotNull(list);
 			Iterator iter = list.iterator();
 			boolean flag = false;
 			while (iter.hasNext())
 			{
 				Container cont = (Container) iter.next();
 				if (cont.getId().equals(container.getId()))
 				{
 					flag = true;
 					break;
 				}
 			}
 			assertTrue(flag);
 		}
 		catch (Exception e)
 		{
 			Logger.out.debug(e.getMessage());
 			fail("Exception occured");
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void testCreateContainerForContainerWithoutEntity()
 	{
 		EntityManagerInterface EntityManagerInterface = EntityManager.getInstance();
 
 		try
 		{
 			EntityGroup entityGroup = (EntityGroup) DomainObjectFactory.getInstance()
 					.createEntityGroup();
 			entityGroup.setName("testGroup" + new Double(Math.random()).toString());
 			Container container = new Container();
 			container.setCaption("testcontainer");
 			Entity entityInterface = (Entity) new MockEntityManager().initializeEntity(entityGroup);
 			EntityManagerUtil.addIdAttribute(entityInterface);
 			container.setAbstractEntity(entityInterface);
 			Collection<ContainerInterface> listOfContainers = new HashSet<ContainerInterface>();
 			listOfContainers.add(container);
 			entityInterface.setContainerCollection(listOfContainers);
 			EntityManagerInterface.persistEntity((EntityInterface) container.getAbstractEntity());
 			Collection list = EntityManagerInterface.getAllContainers();
 			assertNotNull(list);
 			Iterator iter = list.iterator();
 			boolean flag = false;
 			while (iter.hasNext())
 			{
 				Container cont = (Container) iter.next();
 				if (cont.getId().equals(container.getId()))
 				{
 					flag = true;
 					break;
 				}
 			}
 			assertTrue(flag);
 		}
 		catch (Exception e)
 		{
 			Logger.out.debug(e.getMessage());
 			fail("Exception occured");
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void testEditEntityForNewAddedAttribute()
 	{
 		EntityManagerInterface EntityManagerInterface = EntityManager.getInstance();
 
 		try
 		{
 			EntityGroup entityGroup = (EntityGroup) DomainObjectFactory.getInstance()
 					.createEntityGroup();
 			entityGroup.setName("testGroup" + new Double(Math.random()).toString());
 			Entity entity = (Entity) new MockEntityManager().initializeEntity(entityGroup);
 			entity = (Entity) EntityManagerInterface.persistEntity(entity);
 			AttributeInterface attr = new MockEntityManager().initializeStringAttribute("na", "ab");
 			attr.setEntity(entity);
 			entity.addAbstractAttribute(attr);
 			entity = (Entity) EntityManagerInterface.persistEntity(entity);
 
 			//Checking whether metadata information is saved properly or not.
 			String tableName = entity.getTableProperties().getName();
 		}
 		catch (Exception e)
 		{
 			Logger.out.debug(e.getMessage());
 			fail("Exception occured");
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void testEditEntityForModifiedIsNullableAttribute()
 	{
 		EntityManagerInterface EntityManagerInterface = EntityManager.getInstance();
 
 		try
 		{
 			EntityGroup entityGroup = (EntityGroup) DomainObjectFactory.getInstance()
 					.createEntityGroup();
 			entityGroup.setName("testGroup" + new Double(Math.random()).toString());
 			Entity entity = (Entity) new MockEntityManager().initializeEntity(entityGroup);
 			AttributeInterface attr = new MockEntityManager().initializeStringAttribute("na", "ab");
 			attr.setEntity(entity);
 			entity.addAbstractAttribute(attr);
 			entity = (Entity) EntityManagerInterface.persistEntity(entity);
 			attr = (AttributeInterface) entity.getAttributeByIdentifier(attr.getId());
 			attr.setIsNullable(new Boolean(false));
 			entity = (Entity) EntityManagerInterface.persistEntity(entity);
 			Attribute savedAttribute = (Attribute) entity.getAttributeByIdentifier(attr.getId());
 			assertFalse(savedAttribute.getIsNullable());
 			//Checking whether metadata information is saved properly or not.
 			String tableName = entity.getTableProperties().getName();
 		}
 		catch (Exception e)
 		{
 			Logger.out.debug(e.getMessage());
 			fail("Exception occured");
 		}
 	}
 
 	/**
 	 * This method tests for creating a entity with file attribute.
 	 */
 	public void testInsertRecordForFileAttribute1()
 	{
 		EntityManagerInterface EntityManagerInterface = EntityManager.getInstance();
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 		EntityGroup entityGroup = (EntityGroup) factory.createEntityGroup();
 		entityGroup.setName("testGroup" + new Double(Math.random()).toString());
 
 		// create user
 		EntityInterface user = createAndPopulateEntity();
 		user.setEntityGroup(entityGroup);
 		entityGroup.addEntity(user);
 		user.setName("User");
 
 		AttributeInterface age = factory.createIntegerAttribute();
 		age.setName("Age");
 		user.addAbstractAttribute(age);
 
 		AttributeInterface resume = factory.createFileAttribute();
 		resume.setName("Resume");
 		user.addAbstractAttribute(resume);
 
 		Collection<FileExtension> allowedExtn = new HashSet<FileExtension>();
 
 		FileExtension txtExtn = new FileExtension();
 		txtExtn.setFileExtension("txt");
 		allowedExtn.add(txtExtn);
 
 		FileExtension pdfExtn = new FileExtension();
 		pdfExtn.setFileExtension("pdf");
 		allowedExtn.add(pdfExtn);
 
 		allowedExtn.add(txtExtn);
 		allowedExtn.add(pdfExtn);
 
 		((FileAttributeTypeInformation) resume.getAttributeTypeInformation()).setMaxFileSize(20F);
 		((FileAttributeTypeInformation) resume.getAttributeTypeInformation())
 				.setFileExtensionCollection(allowedExtn);
 
 		try
 		{
 			user = EntityManagerInterface.persistEntity(user);
 
 			FileAttributeRecordValue fileRecord = new FileAttributeRecordValue();
 			fileRecord.setContentType("PDF");
 			fileRecord.setFileName("test.pdf");
 			String fileContent = "this is content of the file";
 			fileRecord.setFileContent(fileContent.getBytes());
 
 			Map dataValue = new HashMap();
 			dataValue.put(age, "45");
 			dataValue.put(resume, fileRecord);
 
 			Long recordId = EntityManagerInterface.insertData(user, dataValue);
 			FileAttributeRecordValue attributeRecordValue = EntityManagerInterface
 					.getFileAttributeRecordValueByRecordId(resume, recordId);
 
 			int rowCount = (Integer) executeQuery("select count(*) from "
 					+ user.getTableProperties().getName(), INT_TYPE, 1);
 			assertEquals(1, rowCount);
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			fail();
 		}
 	}
 
 	/**
 	 * This method tests for creating a entity with file attribute.
 	 */
 	public void testInsertRecordForFileAttribute()
 	{
 		EntityManagerInterface EntityManagerInterface = EntityManager.getInstance();
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 		EntityGroup entityGroup = (EntityGroup) factory.createEntityGroup();
 		entityGroup.setName("testGroup" + new Double(Math.random()).toString());
 
 		// create user
 		EntityInterface user = createAndPopulateEntity();
 		user.setEntityGroup(entityGroup);
 		entityGroup.addEntity(user);
 		user.setName("User");
 
 		AttributeInterface age = factory.createIntegerAttribute();
 		age.setName("Age");
 		user.addAbstractAttribute(age);
 
 		AttributeInterface resume = factory.createFileAttribute();
 		resume.setName("Resume");
 		user.addAbstractAttribute(resume);
 
 		Collection<FileExtension> allowedExtn = new HashSet<FileExtension>();
 
 		FileExtension txtExtn = new FileExtension();
 		txtExtn.setFileExtension("txt");
 		allowedExtn.add(txtExtn);
 
 		FileExtension pdfExtn = new FileExtension();
 		pdfExtn.setFileExtension("pdf");
 		allowedExtn.add(pdfExtn);
 
 		allowedExtn.add(txtExtn);
 		allowedExtn.add(pdfExtn);
 
 		((FileAttributeTypeInformation) resume.getAttributeTypeInformation()).setMaxFileSize(20F);
 		((FileAttributeTypeInformation) resume.getAttributeTypeInformation())
 				.setFileExtensionCollection(allowedExtn);
 
 		try
 		{
 			user = EntityManagerInterface.persistEntity(user);
 
 			FileAttributeRecordValue fileRecord = new FileAttributeRecordValue();
 			fileRecord.setContentType("PDF");
 			fileRecord.setFileName("tp.java");
 			String fileContent = "this is content of the file";
 			//File f = new File("C:\\BinaryBlobType.java");
 			fileRecord.setFileContent(fileContent.getBytes());
 
 			Map dataValue = new HashMap();
 			dataValue.put(age, "45");
 			dataValue.put(resume, fileRecord);
 
 			Long recordId = EntityManagerInterface.insertData(user, dataValue);
 
 			int rowCount = (Integer) executeQuery("select count(*) from "
 					+ user.getTableProperties().getName(), INT_TYPE, 1);
 			assertEquals(1, rowCount);
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			fail();
 		}
 	}
 
 	/**
 	 * This method tests for creating a entity with file attribute.
 	 */
 	public void testGerRecordForFileAttribute()
 	{
 		EntityManagerInterface EntityManagerInterface = EntityManager.getInstance();
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 		EntityGroup entityGroup = (EntityGroup) factory.createEntityGroup();
 		entityGroup.setName("testGroup" + new Double(Math.random()).toString());
 
 		// create user
 		EntityInterface user = createAndPopulateEntity();
 		user.setEntityGroup(entityGroup);
 		entityGroup.addEntity(user);
 		user.setName("User");
 
 		AttributeInterface age = factory.createIntegerAttribute();
 		age.setName("Age");
 		user.addAbstractAttribute(age);
 
 		AttributeInterface resume = factory.createFileAttribute();
 		resume.setName("Resume");
 		user.addAbstractAttribute(resume);
 
 		Collection<FileExtension> allowedExtn = new HashSet<FileExtension>();
 
 		FileExtension txtExtn = new FileExtension();
 		txtExtn.setFileExtension("txt");
 		allowedExtn.add(txtExtn);
 
 		FileExtension pdfExtn = new FileExtension();
 		pdfExtn.setFileExtension("pdf");
 		allowedExtn.add(pdfExtn);
 
 		allowedExtn.add(txtExtn);
 		allowedExtn.add(pdfExtn);
 
 		((FileAttributeTypeInformation) resume.getAttributeTypeInformation()).setMaxFileSize(20F);
 		((FileAttributeTypeInformation) resume.getAttributeTypeInformation())
 				.setFileExtensionCollection(allowedExtn);
 
 		try
 		{
 			user = EntityManagerInterface.persistEntity(user);
 
 			FileAttributeRecordValue fileRecord = new FileAttributeRecordValue();
 			fileRecord.setContentType("PDF");
 			fileRecord.setFileName("tp.java");
 			String fileContent = "this is content of the file";
 			fileRecord.setFileContent(fileContent.getBytes());
 
 			Map dataValue = new HashMap();
 			dataValue.put(age, "45");
 			dataValue.put(resume, fileRecord);
 
 			Long recordId = EntityManagerInterface.insertData(user, dataValue);
 
 			dataValue = EntityManagerInterface.getRecordById(user, recordId);
 
 			assertEquals("45", dataValue.get(age).toString());
 			assertEquals("tp.java", dataValue.get(resume).toString());
 
 			System.out.println(dataValue);
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			fail();
 		}
 	}
 
 	/**
 	 * This method tests for creating a entity with file attribute.
 	 */
 	public void testEditRecordForFileAttribute()
 	{
 		EntityManagerInterface EntityManagerInterface = EntityManager.getInstance();
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 		EntityGroup entityGroup = (EntityGroup) factory.createEntityGroup();
 		entityGroup.setName("testGroup" + new Double(Math.random()).toString());
 
 		// create user
 		EntityInterface user = createAndPopulateEntity();
 		user.setEntityGroup(entityGroup);
 		entityGroup.addEntity(user);
 		user.setName("User");
 
 		AttributeInterface age = factory.createIntegerAttribute();
 		age.setName("Age");
 		user.addAbstractAttribute(age);
 
 		AttributeInterface resume = factory.createFileAttribute();
 		resume.setName("Resume");
 		user.addAbstractAttribute(resume);
 
 		Collection<FileExtension> allowedExtn = new HashSet<FileExtension>();
 
 		FileExtension txtExtn = new FileExtension();
 		txtExtn.setFileExtension("txt");
 		allowedExtn.add(txtExtn);
 
 		FileExtension pdfExtn = new FileExtension();
 		pdfExtn.setFileExtension("pdf");
 		allowedExtn.add(pdfExtn);
 
 		allowedExtn.add(txtExtn);
 		allowedExtn.add(pdfExtn);
 
 		((FileAttributeTypeInformation) resume.getAttributeTypeInformation()).setMaxFileSize(20F);
 		((FileAttributeTypeInformation) resume.getAttributeTypeInformation())
 				.setFileExtensionCollection(allowedExtn);
 
 		try
 		{
 			user = EntityManagerInterface.persistEntity(user);
 
 			FileAttributeRecordValue fileRecord = new FileAttributeRecordValue();
 			fileRecord.setContentType("PDF");
 			fileRecord.setFileName("tp.java");
 			String fileContent = "this is content of the file";
 			fileRecord.setFileContent(fileContent.getBytes());
 
 			Map dataValue = new HashMap();
 			dataValue.put(age, "45");
 			dataValue.put(resume, fileRecord);
 
 			Long recordId = EntityManagerInterface.insertData(user, dataValue);
 			//recordId = entityManager.insertData(user, dataValue);
 
 			dataValue = EntityManagerInterface.getRecordById(user, recordId);
 			dataValue.clear();
 			fileRecord.setFileName("new file name");
 			fileRecord.setFileContent("modified file contents".getBytes());
 			dataValue.put(resume, fileRecord);
 			EntityManagerInterface.editData(user, dataValue, recordId);
 
 			dataValue = EntityManagerInterface.getRecordById(user, recordId);
 
 			assertEquals("new file name", dataValue.get(resume).toString());
 
 			System.out.println(dataValue);
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			fail();
 		}
 	}
 
 	/**
 	 * This method tests for creating a entity with file attribute.
 	 */
 	/*
 		public void testDeleteRecordForFileAttribute()
 		{
 			EntityManagerInterface EntityManagerInterface = EntityManager.getInstance();
 			DomainObjectFactory factory = DomainObjectFactory.getInstance();
 			EntityGroup entityGroup = (EntityGroup) factory.createEntityGroup();
 			entityGroup.setName("testGroup" + new Double(Math.random()).toString());
 
 			EntityInterface user = createAndPopulateEntity();
 			user.setEntityGroup(entityGroup);
 			entityGroup.addEntity(user);
 			user.setName("User");
 
 			AttributeInterface age = factory.createIntegerAttribute();
 			age.setName("Age");
 			user.addAbstractAttribute(age);
 
 			AttributeInterface resume = factory.createFileAttribute();
 			resume.setName("Resume");
 			user.addAbstractAttribute(resume);
 
 			Collection<FileExtension> allowedExtn = new HashSet<FileExtension>();
 
 			FileExtension txtExtn = new FileExtension();
 			txtExtn.setFileExtension("txt");
 			allowedExtn.add(txtExtn);
 
 			FileExtension pdfExtn = new FileExtension();
 			pdfExtn.setFileExtension("pdf");
 			allowedExtn.add(pdfExtn);
 
 			allowedExtn.add(txtExtn);
 			allowedExtn.add(pdfExtn);
 
 			((FileAttributeTypeInformation) resume.getAttributeTypeInformation()).setMaxFileSize(20F);
 			((FileAttributeTypeInformation) resume.getAttributeTypeInformation()).setFileExtensionCollection(allowedExtn);
 
 			try
 			{
 				user = EntityManagerInterface.persistEntity(user);
 
 				FileAttributeRecordValue fileRecord = new FileAttributeRecordValue();
 				fileRecord.setContentType("PDF");
 				fileRecord.setFileName("tp.java");
 				String fileContent = "this is content of the file";
 				fileRecord.setFileContent(fileContent.getBytes());
 
 				Map dataValue = new HashMap();
 				dataValue.put(age, "45");
 				dataValue.put(resume, fileRecord);
 
 				Long recordId = EntityManagerInterface.insertData(user, dataValue);
 
 				dataValue = EntityManagerInterface.getRecordById(user, recordId);
 
 				ResultSet resultSet = executeQuery("select count(*) from dyextn_attribute_record");
 				resultSet.next();
 				int beforeCnt = resultSet.getInt(1);
 				resultSet.close();
 
 				EntityManagerInterface.deleteRecord(user, recordId);
 
 				resultSet = executeQuery("select count(*) from dyextn_attribute_record");
 				resultSet.next();
 				int afterCnt = resultSet.getInt(1);
 
 				assertEquals(1, beforeCnt - afterCnt);
 			}
 			catch (Exception e)
 			{
 				e.printStackTrace();
 				fail();
 			}
 		}*/
 
 	/**
 	 * PURPOSE : to test the method persistEntityMetadata.
 	 * EXPECTED BEHAVIOR : It should only save the metadata information of the entity and not create the data table for the entity.
 	 * TEST CASE FLOW :
 	 * 1.Create an entity
 	 * 2.Populate the entity.
 	 * 3.Test that the metadata information is properly saved or not.
 	 * 4.Check that the data table is not created.
 	 */
 	public void testPersistEntityMetadata()
 	{
 		EntityManagerInterface EntityManagerInterface = EntityManager.getInstance();
 
 		try
 		{
 			EntityGroup entityGroup = (EntityGroup) DomainObjectFactory.getInstance()
 					.createEntityGroup();
 			entityGroup.setName("testGroup" + new Double(Math.random()).toString());
 			//Step 1
 			Entity entity = (Entity) new MockEntityManager().initializeEntity(entityGroup);
 			EntityManagerUtil.addIdAttribute(entity);
 			TaggedValueInterface taggedValue = DomainObjectFactory.getInstance()
 					.createTaggedValue();
 			//Step 2
 			taggedValue.setKey("a");
 			taggedValue.setValue("b");
 			entity.addTaggedValue(taggedValue);
 			entity = (Entity) EntityManagerInterface.persistEntityMetadata(entity);
 
 			//Step 3
 			Entity newEntity = (Entity) EntityManagerInterface.getEntityByIdentifier(entity.getId()
 					.toString());
 			assertEquals(entity.getName(), newEntity.getName());
 			//Step 4
 			String tableName = entity.getTableProperties().getName();
 			assertFalse(isTablePresent(tableName));
 		}
 		catch (Exception e)
 		{
 			Logger.out.debug(e.getMessage());
 			e.printStackTrace();
 			fail("Exception occured");
 		}
 	}
 
 	/**
 	 * This method tests the creation of entity group
 	 */
 	public void testCreateEntityGroupForRollback()
 	{
 		EntityGroup entityGroup = null;
 		EntityGroupManagerInterface entityGroupManager = EntityGroupManager.getInstance();
 
 		try
 		{
 			MockEntityManager mock = new MockEntityManager();
 			entityGroup = (EntityGroup) mock.initializeEntityGroup();
 			for (int i = 0; i <= 11; i++)
 			{
 				Entity entity = (Entity) mock.initializeEntity(entityGroup);
 				entityGroup.addEntity(entity);
 				if (i == 10)
 				{
 					entity = (Entity) mock.initializeEntity(entityGroup);
 					TableProperties tableProperties = new TableProperties();
 					tableProperties.setName("@#$%@$%$^");
 					entity.setTableProperties(tableProperties);
 					entityGroup.addEntity(entity);
 				}
 			}
 			entityGroupManager.persistEntityGroup(entityGroup);
 			fail();
 		}
 		catch (Exception e)
 		{
 			boolean isTablePresent = isTablePresent(entityGroup.getEntityCollection().iterator()
 					.next().getTableProperties().getName());
 			assertFalse(isTablePresent);
 			//e.printStackTrace();
 		}
 	}
 
 	/**
 	 * This method tests the creation of entity group
 	 */
 	public void testCreateEntityGroupForMultipleEntities()
 	{
 		EntityGroup entityGroup = null;
 		EntityGroupManagerInterface entityGroupManager = EntityGroupManager.getInstance();
 
 		try
 		{
 			MockEntityManager mock = new MockEntityManager();
 			entityGroup = (EntityGroup) mock.initializeEntityGroup();
 			for (int i = 0; i <= 11; i++)
 			{
 				Entity entity = (Entity) mock.initializeEntity(entityGroup);
 				entityGroup.addEntity(entity);
 			}
 			entityGroup = (EntityGroup) entityGroupManager.persistEntityGroup(entityGroup);
 			Iterator iterator = entityGroup.getEntityCollection().iterator();
 			while (iterator.hasNext())
 			{
 				Entity entity = (Entity) iterator.next();
 				boolean isTablePresent = isTablePresent(entity.getTableProperties().getName());
 				assertTrue(isTablePresent);
 			}
 		}
 		catch (Exception e)
 		{
 			fail();
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * PURPOSE : To test the method persistEntityGroupMetadata
 	 * EXPECTED BEHAVIOR : Entity group metadata should be stored properly without creating the data tables
 	 * for the associated entities.
 	 * TEST CASE FLOW :
 	 * 1. Create entity group
 	 * 2. Create entities and add them to the entity group.
 	 * 3. Call the method persistEntityGroupMetadata to store the metdata information.
 	 * 4. Check whether the metadata is stored correctly.
 	 * 5. Check that the data tables are not created for the associated entities.
 	 */
 	public void testPersistEntityGroupMetadataForMultipleEntities()
 	{
 		EntityGroup entityGroup = null;
 		EntityGroupManagerInterface entityGroupManager = EntityGroupManager.getInstance();
 
 		try
 		{
 			//Step 1
 			MockEntityManager mock = new MockEntityManager();
 			entityGroup = (EntityGroup) mock.initializeEntityGroup();
 			entityGroup.setEntityCollection(new HashSet());
 			//Step 2
 			for (int i = 0; i <= 9; i++)
 			{
 				Entity entity = (Entity) mock.initializeEntity(entityGroup);
 				entityGroup.addEntity(entity);
 			}
 			//Step 3
 			entityGroup = (EntityGroup) entityGroupManager.persistEntityGroupMetadata(entityGroup);
 			//Step 4
 			Collection entityCollection = entityGroup.getEntityCollection();
 			assertEquals(10, entityCollection.size());
 			Iterator iterator = entityCollection.iterator();
 			while (iterator.hasNext())
 			{
 				Entity entity = (Entity) iterator.next();
 				//Step 5
 				boolean isTablePresent = isTablePresent(entity.getTableProperties().getName());
 				assertFalse(isTablePresent);
 			}
 		}
 		catch (Exception e)
 		{
 			fail();
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 *
 	 *
 	 */
 	/*public void testCreateEntityForRollbackQuery()
 	{
 		EntityManagerInterface EntityManagerInterface = EntityManager.getInstance();
 		Entity entity = null;
 		try
 		{
 			EntityGroupInterface entityGroup = DomainObjectFactory.getInstance().createEntityGroup();
 			entityGroup.setName("test_" + new Double(Math.random()).toString());
 			entity = (Entity) new MockEntityManager().initializeEntity(entityGroup);
 			TableProperties tableProperties = new TableProperties();
 			tableProperties.setName("Created_table");
 			entity.setTableProperties(tableProperties);
 			String query = "create table Created_table (id integer)";
 			executeQueryDDL(query);
 			EntityManagerInterface.persistEntity(entity);
 			fail("Exception should have occured but did not");
 		}
 		catch (DynamicExtensionsSystemException e)
 		{
 			Logger.out.info("Exception because of wrong table name.");
 			Logger.out.info(e.getMessage());
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 		finally
 		{
 			executeQueryDDL("drop table Created_table");
 			try
 			{
 				Entity newEntity = (Entity) EntityManagerInterface.getEntityByIdentifier(entity.getId().toString());
 				fail();
 			}
 			catch (DynamicExtensionsApplicationException e)
 			{
 				Logger.out.info("Entity object not found in the database ....");
 			}
 			catch (Exception e1)
 			{
 				e1.printStackTrace();
 			}
 		}
 	}*/
 
 	/**
 	 *
 	 *
 	 */
 	/*public void testCreateEntityForQueryException()
 	{
 		EntityManagerInterface EntityManagerInterface = EntityManager.getInstance();
 
 		try
 		{
 			EntityGroupInterface entityGroup = DomainObjectFactory.getInstance().createEntityGroup();
 			entityGroup.setName("test_" + new Double(Math.random()).toString());
 			Entity entity = (Entity) new MockEntityManager().initializeEntity(entityGroup);
 			TableProperties tableProperties = new TableProperties();
 			tableProperties.setName("!##$$%");
 			entity.setTableProperties(tableProperties);
 			entity = (Entity) EntityManagerInterface.persistEntity(entity);
 			fail("Exception should have occured but did not");
 		}
 		catch (DynamicExtensionsSystemException e)
 		{
 			Logger.out.info("Exception because of wrong table name.");
 			Logger.out.info(e.getMessage());
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 	}*/
 
 	/**
 	 *
 	 */
 	public void testgetAllContainersByEntityGroupId()
 	{
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 		EntityGroupManagerInterface entityGroupManager = EntityGroupManager.getInstance();
 
 		//        ContainerInterface userContainer = factory.createContainer();
 		//        userContainer.setCaption("userContainer");
 		//        EntityInterface user = createAndPopulateEntity();
 		//        user.setName("User");
 		//        userContainer.setEntity(user);
 		//
 		//        ContainerInterface managerContainer = factory.createContainer();
 		//        managerContainer.setCaption("managerContainer");
 		//        EntityInterface manager = createAndPopulateEntity();
 		//        manager.setName("Manager");
 		//        managerContainer.setEntity(manager);
 		//
 		//        EntityGroupInterface userGroup = factory.createEntityGroup();
 		//        userGroup.setName("test_" + new Double(Math.random()).toString());
 		//        userGroup.addEntity(user);
 		//        userGroup.addEntity(manager);
 		//
 		//        user.setEntityGroup(userGroup);
 		//        manager.setEntityGroup(userGroup);
 
 		ContainerInterface studyContainer = factory.createContainer();
 		studyContainer.setCaption("newstudyContainer");
 		EntityInterface study = createAndPopulateEntity();
 		study.setName("study");
 		studyContainer.setAbstractEntity(study);
 		Collection<ContainerInterface> containerCollection = new HashSet<ContainerInterface>();
 		containerCollection.add(studyContainer);
 		study.setContainerCollection(containerCollection);
 
 		ContainerInterface javaStudyContainer = factory.createContainer();
 		javaStudyContainer.setCaption("javaStudyContainer");
 		EntityInterface javaStudy = createAndPopulateEntity();
 		javaStudy.setName("javaStudy");
 		javaStudyContainer.setAbstractEntity(javaStudy);
 		Collection<ContainerInterface> containers = new HashSet<ContainerInterface>();
 		containers.add(javaStudyContainer);
 		javaStudy.setContainerCollection(containers);
 
 		EntityGroupInterface studyGroup = factory.createEntityGroup();
 		studyGroup.setName("test_" + new Double(Math.random()).toString());
 		studyGroup.addEntity(study);
 		studyGroup.addEntity(javaStudy);
 
 		study.setEntityGroup(studyGroup);
 		javaStudy.setEntityGroup(studyGroup);
 
 		try
 		{
 			//            userContainer = entityManager.persistContainer(userContainer);
 			//            managerContainer = entityManager.persistContainer(managerContainer);
 			//            studyContainer = entityManager.persistContainer(studyContainer);
 			//            javaStudyContainer = entityManager.persistContainer(javaStudyContainer);
 			EntityGroup entityGroup = (EntityGroup) entityGroupManager
 					.persistEntityGroup(studyGroup);
 
 			Collection studyGroupContainerCollection = entityGroup.getEntityCollection();
 
 			assertEquals(2, studyGroupContainerCollection.size());
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			fail();
 		}
 	}
 
 	/**
 	 *
 	 *
 	 */
 	public void testGetContainerByEntityIdentifier()
 	{
 		EntityManagerInterface EntityManagerInterface = EntityManager.getInstance();
 		try
 		{
 			ContainerInterface containerInterface = (Container) new MockEntityManager()
 					.getContainer("abc");
 			EntityInterface entityInterface = (EntityInterface) containerInterface
 					.getAbstractEntity();
 
 			EntityManagerInterface.persistEntity(entityInterface);
 			assertTrue(EntityManagerInterface.getEntityByIdentifier(entityInterface.getId())
 					.getContainerCollection().contains(containerInterface));
 		}
 		catch (DynamicExtensionsApplicationException e)
 		{
 			e.printStackTrace();
 		}
 		catch (DynamicExtensionsSystemException e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * PURPOSE : to test the method insertData for attribute having default value specified.
 	 * EXPECTED BEHAVIOR : since default value is only for display purpose, it should not be persisted
 	 *                     if user has explicitly removed this value /not mentioned for the attribute.
 	 *
 	 * TEST CASE FLOW :
 	 * 1.Create an entity
 	 * 2.Persist the entity.
 	 * 3.Insert data without specifying value for the attribute that has some default value.
 	 * 4.Check that the value for that attribute is null.
 	 */
 	public void testInsertDataWithdefaultValue()
 	{
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 		EntityGroupInterface entityGroup = factory.createEntityGroup();
 		entityGroup.setName("test_" + new Double(Math.random()).toString());
 		EntityManagerInterface EntityManagerInterface = EntityManager.getInstance();
 		Entity entity = (Entity) createAndPopulateEntity();
 		entity.setName("Stock Quote");
 
 		try
 		{
 			//Step 1.
 			AttributeInterface commentsAttributes = factory.createStringAttribute();
 			commentsAttributes.setName("comments");
 			StringValueInterface defaultValue = factory.createStringValue();
 			defaultValue.setValue("this is default value");
 			commentsAttributes.getAttributeTypeInformation().setDefaultValue(defaultValue);
 
 			entity.addAbstractAttribute(commentsAttributes);
 
 			AttributeInterface tempAttributes = factory.createStringAttribute();
 			tempAttributes.setName("temp");
 			entity.addAbstractAttribute(tempAttributes);
 			EntityManagerUtil.addIdAttribute(entity);
 			entityGroup.addEntity(entity);
 			entity.setEntityGroup(entityGroup);
 			//Step 2.
 			EntityInterface savedEntity = EntityManagerInterface.persistEntity(entity);
 
 			//Step 3.
 			Map dataValue = new HashMap();
 			//dataValue.put(commentsAttributes, "this is not default comment");
 			dataValue.put(tempAttributes, "temp");
 
 			EntityManagerInterface.insertData(savedEntity, dataValue);
 
 			//Step 4.
 			String status = (String) executeQuery("select * from "
 					+ savedEntity.getTableProperties().getName(), STRING_TYPE, 3);
 
 			assertEquals(null, status);
 		}
 		catch (DynamicExtensionsSystemException e)
 		{
 			fail();
 			Logger.out.debug(e.getStackTrace());
 		}
 		catch (DynamicExtensionsApplicationException e)
 		{
 			fail();
 			Logger.out.debug(e.getStackTrace());
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			fail();
 			Logger.out.debug(e.getStackTrace());
 		}
 	}
 
 	/**
 	 * PURPOSE : to test the fix for bug 3209.
 	 *
 	 *
 	 * EXPECTED BEHAVIOR : If file attribute is added during edit,its column should not be getting added
 	 *
 	 * TEST CASE FLOW :
 	 * 1.Create an entity
 	 * 2.Persist the entity.
 	 * 3.edit entity adding a file attribute
 	 * 4. persist again
 	 * 5. chk for no of column for that entity
 	 */
 	public void testEditEntityToAddFileAttribute()
 	{
 		EntityManagerInterface EntityManagerInterface = EntityManager.getInstance();
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 		EntityGroup entityGroup = (EntityGroup) factory.createEntityGroup();
 		entityGroup.setName("testGroup" + new Double(Math.random()).toString());
 
 		try
 		{
 			//Step 1.
 			EntityInterface user = createAndPopulateEntity();
 			user.setEntityGroup(entityGroup);
 			entityGroup.addEntity(user);
 			user.setName("user");
 
 			AttributeInterface commentsAttributes = factory.createStringAttribute();
 			commentsAttributes.setName("comments");
 			user.addAbstractAttribute(commentsAttributes);
 
 			//Step 2.
 			EntityInterface savedEntity = EntityManagerInterface.persistEntity(user);
 
 			//assertEquals(getColumnCount("select * from " + savedEntity.getTableProperties().getName()), noOfDefaultColumns + 1);
 
 			//Step 3.
 			AttributeInterface resume = factory.createFileAttribute();
 			resume.setName("resume");
 			user.addAbstractAttribute(resume);
 
 			//Step 4.
 			savedEntity = EntityManagerInterface.persistEntity(user);
 
 			//Step 5.
 
 			assertEquals(getColumnCount("select * from "
 					+ savedEntity.getTableProperties().getName()), noOfDefaultColumnsForfile + 1);
 		}
 		catch (DynamicExtensionsSystemException e)
 		{
 			fail();
 			Logger.out.debug(e.getStackTrace());
 		}
 		catch (DynamicExtensionsApplicationException e)
 		{
 			fail();
 			Logger.out.debug(e.getStackTrace());
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			fail();
 			Logger.out.debug(e.getStackTrace());
 		}
 	}
 
 	/**
 	 * PURPOSE : to test the method GetMainContainer
 	 *
 	 *
 	 * EXPECTED BEHAVIOR : GetMainContainer method should return the main container given the entity group id
 	 *
 	 * TEST CASE FLOW :
 	 * 1.Create an entity
 	 * 2.Create an entity Group
 	 * 3.Add entity in entity group
 	 * 4.Create an container
 	 * 5.set its entity to the created entity
 	 * 6.Persist the container.
 	 * 7. invoke getMainContainer
 	 * 8. test if retruned container is same or not
 	 */
 	/*public void testGetMainContainer()
 	{
 	    DomainObjectFactory factory = DomainObjectFactory.getInstance();
 	    EntityManagerInterface EntityManagerInterface = EntityManager.getInstance();
 
 	    try
 	    {
 	        //Step 1.
 	        EntityInterface user = createAndPopulateEntity();
 	        user.setName("user");
 
 	        //Step 2.
 	        EntityGroupInterface userGroup = factory.createEntityGroup();
 	        ((EntityGroup) userGroup).setCurrent(true);
 
 	        //Step 3.
 	        userGroup.addEntity(user);
 	        user.addEntityGroupInterface(userGroup);
 
 	        //Step 4.
 	        ContainerInterface userContainer = factory.createContainer();
 
 	        userContainer.setCaption("User Container");
 
 	        //Step 5.
 	        userContainer.setEntity(user);
 	        user.getContainerCollection().add(userContainer);
 
 	        //Step 6.
 	        //entityManagerInterface.persistContainer(userContainer);
 	        EntityManagerInterface.persistEntity(user);
 
 	        //Step 7.
 	        Collection<NameValueBean> containerNameValueCollection = entityManagerInterface
 	                .getMainContainer(userGroup.getId());
 
 	        //Step 8.
 	        assertEquals(containerNameValueCollection.size(), 1);
 	        NameValueBean containerNameValue = containerNameValueCollection.iterator().next();
 
 	        assertEquals(containerNameValue.getName(), userContainer.getCaption());
 	        assertEquals(containerNameValue.getValue(), userContainer.getId().toString());
 	    }
 	    catch (Exception e)
 	    {
 	        e.printStackTrace();
 	    }
 	}*/
 
 	/**
 	 *
 	 * Commenting this test case as per bug #5094 as we should be able to see abstract forms for future editing.
 	 * To avoid data entry in abstract forms, we need to check the abstract attribute for entity in code. - Ashish 18/9/07
 	 *
 	 * PURPOSE : to test the method GetMainContainer
 	 *
 	 *
 	 * EXPECTED BEHAVIOR : GetMainContainer method should return null when
 	 *                     main entity within given is abstract
 	 *
 	 * TEST CASE FLOW :
 	 * 1.Create an abstract entity
 	 * 2.Create an entity Group
 	 * 3.Add entity in entity group
 	 * 4.Create an container
 	 * 5.set its entity to the created entity
 	 * 6.Persist the container.
 	 * 7. invoke getMainContainer
 	 * 8. test if retruned container null or not
 	 */
 	//  public void testGetMainContainerForAbstractEntity()
 	//  {
 	//      //EntityManagerInterface entityManagerInterface = NewEntityManger.getInstance();
 	//      DomainObjectFactory factory = DomainObjectFactory.getInstance();
 	//
 	//      try
 	//      {
 	//          //Step 1.
 	//          EntityInterface user = createAndPopulateEntity();
 	//          user.setAbstract(true);
 	//          user.setName("user");
 	//
 	//          //Step 2.
 	//          EntityGroupInterface userGroup = factory.createEntityGroup();
 	//          ((EntityGroup) userGroup).setCurrent(true);
 	//
 	//          //Step 3.
 	//          userGroup.addEntity(user);
 	//          user.addEntityGroupInterface(userGroup);
 	//
 	//          //Step 4.
 	//          ContainerInterface userContainer = factory.createContainer();
 	//          userContainer.setCaption("User Container");
 	//
 	//          //Step 5.
 	//          userContainer.setEntity(user);
 	//
 	//          //Step 6.
 	//          entityManagerInterface.persistContainer(userContainer);
 	//
 	//          //Step 7.
 	//          Collection<NameValueBean> containerNameValueCollection = entityManagerInterface
 	//                  .getMainContainer(userGroup.getId());
 	//
 	//          //Step 8.
 	//          assertEquals(0, containerNameValueCollection.size());
 	//
 	//      }
 	//      catch (Exception e)
 	//      {
 	//          e.printStackTrace();
 	//      }
 	//
 	//  }
 	/**
 	 *
 	 */
 	public void testInsertDataForDate()
 	{
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 		EntityGroupInterface entityGroup = factory.createEntityGroup();
 		entityGroup.setName("test_" + new Double(Math.random()).toString());
 		//Step 1
 		Entity entity = (Entity) createAndPopulateEntity();
 		entity.setName("Stock Quote");
 		EntityManagerInterface EntityManagerInterface = EntityManager.getInstance();
 
 		try
 		{
 			AttributeInterface floatAtribute = DomainObjectFactory.getInstance()
 					.createFloatAttribute();
 			floatAtribute.setName("Price");
 
 			AttributeInterface commentsAttributes = DomainObjectFactory.getInstance()
 					.createStringAttribute();
 			commentsAttributes.setName("coomments");
 
 			AttributeInterface startDate = DomainObjectFactory.getInstance().createDateAttribute();
 			startDate.setName("startDate");
 			((DateAttributeTypeInformation) startDate.getAttributeTypeInformation())
 					.setFormat(ProcessorConstants.DATE_TIME_FORMAT);
 
 			AttributeInterface endDate = DomainObjectFactory.getInstance().createDateAttribute();
 			((DateAttributeTypeInformation) endDate.getAttributeTypeInformation())
 					.setFormat(ProcessorConstants.SQL_DATE_ONLY_FORMAT);
 			endDate.setName("endDate");
 
 			entity.addAbstractAttribute(floatAtribute);
 			entity.addAbstractAttribute(commentsAttributes);
 			entity.addAbstractAttribute(startDate);
 			entity.addAbstractAttribute(endDate);
 			entityGroup.addEntity(entity);
 			entity.setEntityGroup(entityGroup);
 			//Step 2
 			EntityInterface savedEntity = EntityManagerInterface.persistEntity(entity);
 
 			Map dataValue = new HashMap();
 			dataValue.put(floatAtribute, "15.90");
 			dataValue.put(startDate, "11-12-1982 10:11");
 			dataValue.put(endDate, "01-12-1982");
 
 			Long recordId = EntityManagerInterface.insertData(savedEntity, dataValue);
 
 			dataValue = EntityManagerInterface.getRecordById(entity, recordId);
 
 			assertEquals("11-12-1982 10:11", dataValue.get(startDate));
 			assertEquals("01-12-1982", dataValue.get(endDate));
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			fail();
 		}
 	}
 
 	/**
 	 * Create a date only attribute, and try to insert a future date value
 	 * Data should not get inserted and a validation message with cause should be shown to the user.
 	 */
 	public void testInsertFutureDateForDateOnlyFormat()
 	{
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 		EntityGroupInterface entityGroup = factory.createEntityGroup();
 		entityGroup.setName("test_date_only_future_date");
 
 		//Step 1
 		Entity entity = (Entity) createAndPopulateEntity();
 		entity.setName("Stock");
 
 		EntityManagerInterface entityManager = EntityManager.getInstance();
 
 		Long recordId = null;
 		try
 		{
 			Attribute date = (Attribute) factory.createDateAttribute();
 			date.setName("Date");
 			((DateAttributeTypeInformation) date.getAttributeTypeInformation())
 					.setFormat(ProcessorConstants.SQL_DATE_ONLY_FORMAT);
 
 			RuleInterface dateRule = factory.createRule();
 			dateRule.setName("date");
 			date.getRuleCollection().add(dateRule);
 
 			date.getRuleCollection().add(dateRule);
 
 			entity.addAbstractAttribute(date);
 			entityGroup.addEntity(entity);
 			entity.setEntityGroup(entityGroup);
 
 			//Step 2
 			EntityInterface savedEntity = entityManager.persistEntity(entity);
 
 			Map dataValue = new HashMap();
 			dataValue.put("date", "11-16-1982");
 
 			for (RuleInterface rule : date.getRuleCollection())
 			{
 				ValidatorRuleInterface validatorRule = ControlConfigurationsFactory.getInstance()
 						.getValidatorRule(rule.getName());
 				validatorRule.validate(date, "08-16-2020", null, "Date");
 			}
 
 			recordId = entityManager.insertData(savedEntity, dataValue);
 			assertEquals(recordId, null);
 		}
 		catch (DynamicExtensionsValidationException e)
 		{
 			System.out.println("Could not insert data....");
 			System.out
 					.println("Validation failed. Input date should be lesser than or equal to today's date");
 			assertEquals("Validation failed", e.getMessage());
 			assertEquals(recordId, null);
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			fail();
 		}
 	}
 
 	/**
 	 * Insert data for dates with different date formats.
 	 */
 	public void testInsertDataForDateWithDifferentFormats()
 	{
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 		EntityGroupInterface entityGroup = factory.createEntityGroup();
 		entityGroup.setName("DATE_TEST" + new Double(Math.random()).toString());
 
 		//Step 1
 		Entity entity = (Entity) createAndPopulateEntity();
 		entity.setName("DateForm");
 		EntityManagerInterface EntityManagerInterface = EntityManager.getInstance();
 
 		try
 		{
 			AttributeInterface dateOnly = DomainObjectFactory.getInstance().createDateAttribute();
 			((DateAttributeTypeInformation) dateOnly.getAttributeTypeInformation())
 					.setFormat(ProcessorConstants.SQL_DATE_ONLY_FORMAT);
 			dateOnly.setName("dateOnly");
 
 			AttributeInterface dateTime = DomainObjectFactory.getInstance().createDateAttribute();
 			((DateAttributeTypeInformation) dateTime.getAttributeTypeInformation())
 					.setFormat(ProcessorConstants.DATE_TIME_FORMAT);
 			dateTime.setName("dateTime");
 
 			AttributeInterface yearOnlyDate = DomainObjectFactory.getInstance()
 					.createDateAttribute();
 			((DateAttributeTypeInformation) yearOnlyDate.getAttributeTypeInformation())
 					.setFormat(ProcessorConstants.YEAR_ONLY_FORMAT);
 			yearOnlyDate.setName("yearOnlyDate");
 
 			AttributeInterface monthYearDate = DomainObjectFactory.getInstance()
 					.createDateAttribute();
 			((DateAttributeTypeInformation) monthYearDate.getAttributeTypeInformation())
 					.setFormat(ProcessorConstants.MONTH_YEAR_FORMAT);
 			monthYearDate.setName("monthYearDate");
 
 			entity.addAbstractAttribute(dateOnly);
 			entity.addAbstractAttribute(dateTime);
 			entity.addAbstractAttribute(yearOnlyDate);
 			entity.addAbstractAttribute(monthYearDate);
 
 			entityGroup.addEntity(entity);
 			entity.setEntityGroup(entityGroup);
 			//Step 2
 			EntityInterface savedEntity = EntityManagerInterface.persistEntity(entity);
 
 			Map dataValue = new HashMap();
 			dataValue.put(dateOnly, "01-12-1982");
 			dataValue.put(dateTime, "11-12-1982 10:11");
 			dataValue.put(yearOnlyDate, "1900");
 			dataValue.put(monthYearDate, "09-1900");
 
 			Long recordId = EntityManagerInterface.insertData(savedEntity, dataValue);
 
 			dataValue = EntityManagerInterface.getRecordById(entity, recordId);
 
 			assertEquals("11-12-1982 10:11", dataValue.get(dateTime));
 			assertEquals("01-12-1982", dataValue.get(dateOnly));
 			assertEquals("1900", dataValue.get(yearOnlyDate));
 			assertEquals("09-1900", dataValue.get(monthYearDate));
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			fail();
 		}
 	}
 
 	/**
 	 * Create a date attribute, specify range for the attribute, try to insert
 	 * a date value which is out of the range. Data should not get inserted and
 	 * a validation message with cause should be shown to the user.
 	 */
 	public void testInsertDataForDateWithRange()
 	{
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 		EntityGroupInterface entityGroup = factory.createEntityGroup();
 		entityGroup.setName("test_date_range");
 
 		//Step 1
 		Entity entity = (Entity) createAndPopulateEntity();
 		entity.setName("Stock Quote");
 
 		EntityManagerInterface entityManager = EntityManager.getInstance();
 
 		Long recordId = null;
 		try
 		{
 			Attribute date = (Attribute) factory.createDateAttribute();
 			date.setName("DateRange");
 			((DateAttributeTypeInformation) date.getAttributeTypeInformation())
 					.setFormat(ProcessorConstants.SQL_DATE_ONLY_FORMAT);
 
 			RuleInterface dateRange = factory.createRule();
 			dateRange.setName("dateRange");
 
 			RuleParameterInterface minValue = factory.createRuleParameter();
 			minValue.setName("min");
 			minValue.setValue("11-12-1982");
 			dateRange.getRuleParameterCollection().add(minValue);
 
 			date.getRuleCollection().add(dateRange);
 
 			RuleParameterInterface maxValue = factory.createRuleParameter();
 			maxValue.setName("max");
 			maxValue.setValue("11-15-1982");
 			dateRange.getRuleParameterCollection().add(maxValue);
 
 			entity.addAbstractAttribute(date);
 			entityGroup.addEntity(entity);
 			entity.setEntityGroup(entityGroup);
 
 			//Step 2
 			EntityInterface savedEntity = entityManager.persistEntity(entity);
 
 			Map dataValue = new HashMap();
 			dataValue.put("date", "11-15-1982");
 
 			Map<String, String> rulesMap = new HashMap<String, String>();
 			rulesMap.put("min", "11-12-1982");
 			rulesMap.put("max", "11-15-1982");
 
 			for (RuleInterface rule : date.getRuleCollection())
 			{
 				ValidatorRuleInterface validatorRule = ControlConfigurationsFactory.getInstance()
 						.getValidatorRule(rule.getName());
 				validatorRule.validate(date, "11-16-1982", rulesMap, "LeapYear");
 			}
 
 			recordId = entityManager.insertData(savedEntity, dataValue);
 			assertEquals(recordId, null);
 		}
 		catch (DynamicExtensionsValidationException e)
 		{
 			System.out.println("Could not insert data....");
 			System.out
 					.println("Validation failed. Input date should be lesser than or equal to 11-15-1982");
 			assertEquals("Validation failed", e.getMessage());
 			assertEquals(recordId, null);
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			fail();
 		}
 	}
 
 	/**
 	 * Create a date only attribute, create and set allowfuturedate rule to this attribute.
 	 * And try to insert a future date, valueData should get inserted.
 	 */
 	public void testDataForFutureDateRule()
 	{
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 		EntityGroupInterface entityGroup = factory.createEntityGroup();
 		entityGroup.setName("test_future_date");
 
 		//Step 1
 		Entity entity = (Entity) createAndPopulateEntity();
 		entity.setName("Appointments");
 
 		EntityManagerInterface entityManager = EntityManager.getInstance();
 
 		Long recordId = null;
 		try
 		{
 			Attribute dateOnly = (Attribute) factory.createDateAttribute();
 			((DateAttributeTypeInformation) dateOnly.getAttributeTypeInformation())
 					.setFormat(ProcessorConstants.SQL_DATE_ONLY_FORMAT);
 			dateOnly.setName("dateOnly");
 
 			Attribute dateTime = (Attribute) factory.createDateAttribute();
 			((DateAttributeTypeInformation) dateTime.getAttributeTypeInformation())
 					.setFormat(ProcessorConstants.DATE_TIME_FORMAT);
 			dateTime.setName("dateTime");
 
 			Attribute yearOnlyDate = (Attribute) factory.createDateAttribute();
 			((DateAttributeTypeInformation) yearOnlyDate.getAttributeTypeInformation())
 					.setFormat(ProcessorConstants.YEAR_ONLY_FORMAT);
 			yearOnlyDate.setName("yearOnlyDate");
 
 			Attribute monthYearDate = (Attribute) factory.createDateAttribute();
 			((DateAttributeTypeInformation) monthYearDate.getAttributeTypeInformation())
 					.setFormat(ProcessorConstants.MONTH_YEAR_FORMAT);
 			monthYearDate.setName("monthYearDate");
 
 			RuleInterface allowfuturedate = factory.createRule();
 			allowfuturedate.setName("allowfuturedate");
 			allowfuturedate.setIsImplicitRule(false);
 			dateOnly.getRuleCollection().add(allowfuturedate);
 			dateTime.getRuleCollection().add(allowfuturedate);
 			yearOnlyDate.getRuleCollection().add(allowfuturedate);
 			monthYearDate.getRuleCollection().add(allowfuturedate);
 			
 			entity.addAbstractAttribute(dateOnly);
 			entity.addAbstractAttribute(dateTime);
 			entity.addAbstractAttribute(yearOnlyDate);
 			entity.addAbstractAttribute(monthYearDate);
 			entityGroup.addEntity(entity);
 			entity.setEntityGroup(entityGroup);
 
 			//Step 2
 			EntityInterface savedEntity = entityManager.persistEntity(entity);
 
 			for (RuleInterface rule : dateOnly.getRuleCollection())
 			{
 				ValidatorRuleInterface validatorRule = ControlConfigurationsFactory.getInstance()
 						.getValidatorRule(rule.getName());
 				validatorRule.validate((AttributeMetadataInterface) dateOnly, "08-16-2020", null, "Date");
 			}
 			for (RuleInterface rule : dateTime.getRuleCollection())
 			{
 				ValidatorRuleInterface validatorRule = ControlConfigurationsFactory.getInstance()
 						.getValidatorRule(rule.getName());
 				validatorRule.validate((AttributeMetadataInterface) dateTime, "11-12-2020 10:11", null, "Date");
 			}
 			for (RuleInterface rule : yearOnlyDate.getRuleCollection())
 			{
 				ValidatorRuleInterface validatorRule = ControlConfigurationsFactory.getInstance()
 						.getValidatorRule(rule.getName());
 				validatorRule.validate((AttributeMetadataInterface) yearOnlyDate, "2020", null, "Date");
 			}
 			for (RuleInterface rule : monthYearDate.getRuleCollection())
 			{
 				ValidatorRuleInterface validatorRule = ControlConfigurationsFactory.getInstance()
 						.getValidatorRule(rule.getName());
 				validatorRule.validate((AttributeMetadataInterface) monthYearDate, "09-2020", null, "Date");
 			}
 
 			Map dataValue = new HashMap();
 			dataValue.put(dateOnly, "11-16-2018");
 			dataValue.put(dateTime, "11-12-2018 10:11");
 			dataValue.put(yearOnlyDate, "2018");
 			dataValue.put(monthYearDate, "09-2018");
 
 			recordId = entityManager.insertData(savedEntity, dataValue);
 
 			dataValue = entityManager.getRecordById(entity, recordId);
 
 			assertEquals("11-12-2018 10:11", dataValue.get(dateTime));
 			assertEquals("11-16-2018", dataValue.get(dateOnly));
 			assertEquals("2018", dataValue.get(yearOnlyDate));
 			assertEquals("09-2018", dataValue.get(monthYearDate));
 		}
 		catch (DynamicExtensionsValidationException e)
 		{
 			e.printStackTrace();
 			fail();
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			fail();
 		}
 	}
 	
 	/**
 	 * Create a date only attribute, create and set allowfuturedate rule to this attribute.
 	 * And try to insert a date less than or equal to today's date,
 	 * validation error must be thrown
 	 */
 	public void testInvalidDateForFutureDateRule()
 	{
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 		EntityGroupInterface entityGroup = factory.createEntityGroup();
 		entityGroup.setName("test_future_date");
 
 		//Step 1
 		Entity entity = (Entity) createAndPopulateEntity();
 		entity.setName("ExpiryDate");
 
 		EntityManagerInterface entityManager = EntityManager.getInstance();
 
 		Long recordId = null;
 		try
 		{
 			Attribute date = (Attribute) factory.createDateAttribute();
 			date.setName("FutureDate");
 			((DateAttributeTypeInformation) date.getAttributeTypeInformation())
 					.setFormat(ProcessorConstants.SQL_DATE_ONLY_FORMAT);
 
 			RuleInterface allowfuturedate = factory.createRule();
 			allowfuturedate.setName("allowfuturedate");
 			allowfuturedate.setIsImplicitRule(false);
 			date.getRuleCollection().add(allowfuturedate);
 
 			entity.addAbstractAttribute(date);
 			entityGroup.addEntity(entity);
 			entity.setEntityGroup(entityGroup);
 
 			//Step 2 save entity
 			EntityInterface savedEntity = entityManager.persistEntity(entity);
 
 			Map dataValue = new HashMap();
 			dataValue.put(date, "11-16-2000");
 
 			for (RuleInterface rule : date.getRuleCollection())
 			{
 				ValidatorRuleInterface validatorRule = ControlConfigurationsFactory.getInstance()
 						.getValidatorRule(rule.getName());
 				validatorRule.validate(date, "11-16-2000", null, "Date");
 			}
 
 			recordId = entityManager.insertData(savedEntity, dataValue);
 		}
 		catch (DynamicExtensionsValidationException e)
 		{
 			System.out.println("Could not insert data....");
 			System.out
 					.println("Validation failed. Input date should be greater than today's date when future date rule is used.");
 			assertEquals("Validation failed", e.getMessage());
 			assertEquals(recordId, null);
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			fail();
 		}
 	}
 
 	/**
 	 * PURPOSE : to test the basic delete functionality
 	 *
 	 *
 	 * EXPECTED BEHAVIOR : delete method should disable the record and not actually delete it.
 	 *
 	 * TEST CASE FLOW :
 	 * 1.Create an entity
 	 * 2.add two attributes
 	 * 3.persist entity
 	 * 4.add record.
 	 * 5. check the activity status - should be active
 	 * 6.delete that record
 	 * 7. check the activity status - should be disable
 	 */
 	public void testDeleteRecordById1()
 	{
 		EntityManagerInterface EntityManagerInterface = EntityManager.getInstance();
 
 		try
 		{
 			//step 1
 			DomainObjectFactory factory = DomainObjectFactory.getInstance();
 			EntityGroupInterface entityGroup = factory.createEntityGroup();
 			entityGroup.setName("test_" + new Double(Math.random()).toString());
 
 			Entity entity = (Entity) createAndPopulateEntity();
 			entity.setName("Stock Quote");
 
 			AttributeInterface floatAtribute = factory.createFloatAttribute();
 			floatAtribute.setName("Price");
 
 			AttributeInterface commentsAttributes = factory.createStringAttribute();
 			commentsAttributes.setName("comments");
 
 			//step 2
 			entity.addAbstractAttribute(floatAtribute);
 			entity.addAbstractAttribute(commentsAttributes);
 			entityGroup.addEntity(entity);
 			entity.setEntityGroup(entityGroup);
 			//step 3
 			entity = (Entity) EntityManagerInterface.persistEntity(entity);
 
 			//step 4
 			Map dataValue = new HashMap();
 			dataValue.put(floatAtribute, "122.34");
 			dataValue.put(commentsAttributes, "test1123");
 
 			EntityManagerInterface.insertData(entity, dataValue);
 
 			//step 5
 			int rowCount = (Integer) executeQuery("select count(*) from "
 					+ entity.getTableProperties().getName(), INT_TYPE, 1);
 			assertEquals(1, rowCount);
 
 			String activityStatus = (String) executeQuery("select "
 					+ Constants.ACTIVITY_STATUS_COLUMN + " from "
 					+ entity.getTableProperties().getName(), STRING_TYPE, 1);
 			assertEquals(Status.ACTIVITY_STATUS_ACTIVE.toString(), activityStatus);
 
 			//step 6
 			boolean isRecordDeleted = EntityManagerInterface.deleteRecord(entity, new Long(1));
 
 			//step 7
 			assertTrue(isRecordDeleted);
 			activityStatus = (String) executeQuery("select " + Constants.ACTIVITY_STATUS_COLUMN
 					+ " from " + entity.getTableProperties().getName(), STRING_TYPE, 1);
 			assertEquals(Status.ACTIVITY_STATUS_DISABLED.toString(), activityStatus);
 
 			rowCount = (Integer) executeQuery("select count(*) from "
 					+ entity.getTableProperties().getName(), INT_TYPE, 1);
 
 			assertEquals(1, rowCount);
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			Logger.out.debug(e.getMessage());
 			fail("Exception occured");
 		}
 	}
 
 	/**
 	 * PURPOSE : to test the behaviour of  GetAllRecords method for deleted records
 	 * EXPECTED BEHAVIOR : GetAllRecords method should filter the deleted records.
 	 *
 	 * TEST CASE FLOW :
 	 * 1.Create an entity
 	 * 2.add two attributes
 	 * 3.persist entity
 	 * 4.add 2 records.
 	 * 5. call  getAllRecords - should return 2 records
 	 * 6.delete 2nd  record
 	 * 7. call  getAllRecords - should return 1 records
 	 */
 	public void testGetAllRecordsForDeletedRecords()
 	{
 		EntityManagerInterface EntityManagerInterface = EntityManager.getInstance();
 
 		try
 		{
 			//step 1
 			DomainObjectFactory factory = DomainObjectFactory.getInstance();
 			EntityGroupInterface entityGroup = factory.createEntityGroup();
 			entityGroup.setName("test_" + new Double(Math.random()).toString());
 
 			Entity entity = (Entity) createAndPopulateEntity();
 			entity.setName("Stock Quote");
 
 			AttributeInterface floatAtribute = factory.createFloatAttribute();
 			floatAtribute.setName("Price");
 
 			AttributeInterface commentsAttributes = factory.createStringAttribute();
 			commentsAttributes.setName("comments");
 
 			//step 2
 			entity.addAbstractAttribute(floatAtribute);
 			entity.addAbstractAttribute(commentsAttributes);
 			entityGroup.addEntity(entity);
 			entity.setEntityGroup(entityGroup);
 			//step 3
 			entity = (Entity) EntityManagerInterface.persistEntity(entity);
 
 			//step 4
 			Map dataValue = new HashMap();
 			dataValue.put(floatAtribute, "1.1");
 			dataValue.put(commentsAttributes, "test1");
 
 			EntityManagerInterface.insertData(entity, dataValue);
 
 			dataValue.put(floatAtribute, "1.2");
 			dataValue.put(commentsAttributes, "test2");
 			EntityManagerInterface.insertData(entity, dataValue);
 
 			//step 5
 			List<EntityRecord> recordList = EntityManagerInterface.getAllRecords(entity);
 			assertEquals(2, recordList.size());
 
 			//step 6
 			boolean isRecordDeleted = EntityManagerInterface.deleteRecord(entity, new Long(2));
 
 			//step 7
 			assertTrue(isRecordDeleted);
 			recordList = EntityManagerInterface.getAllRecords(entity);
 			assertEquals(1, recordList.size());
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			Logger.out.debug(e.getMessage());
 			fail("Exception occured");
 		}
 	}
 
 	/**
 	 *  PURPOSE: This method test for deleting data for a containtment relationship between two entities
 	 *  EXPECTED BEHAVIOUR: Data of containted entity should also be disabled
 	 *
 	 *  TEST CASE FLOW: 1. create User
 	 *                  2. Create Address
 	 *                  3. Add Association with      User(1) ------->(1) Address containtment association
 	 *                  4. persist entities.
 	 *                  5. Insert Data
 	 *                  6. Delete User record, address should also be deleted.
 	 */
 	public void testDeleteDataForContaintmentOneToOne()
 	{
 		EntityManagerInterface EntityManagerInterface = EntityManager.getInstance();
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 		EntityGroupInterface entityGroup = factory.createEntityGroup();
 		entityGroup.setName("test_" + new Double(Math.random()).toString());
 
 		try
 		{
 			// Step 1
 			EntityInterface user = createAndPopulateEntity();
 			AttributeInterface userNameAttribute = factory.createStringAttribute();
 			userNameAttribute.setName("user name");
 			user.setName("user");
 			user.addAbstractAttribute(userNameAttribute);
 
 			// Step 2
 			EntityInterface address = createAndPopulateEntity();
 			address.setName("address");
 
 			AttributeInterface streetAttribute = factory.createStringAttribute();
 			streetAttribute.setName("street name");
 			address.addAbstractAttribute(streetAttribute);
 
 			AttributeInterface cityAttribute = factory.createStringAttribute();
 			cityAttribute.setName("city name");
 			address.addAbstractAttribute(cityAttribute);
 
 			// Step 3
 			AssociationInterface association = factory.createAssociation();
 			association.setTargetEntity(address);
 			association.setAssociationDirection(AssociationDirection.SRC_DESTINATION);
 			association.setName("UserAddress");
 			association.setSourceRole(getRole(AssociationType.CONTAINTMENT, "User",
 					Cardinality.ZERO, Cardinality.ONE));
 			association.setTargetRole(getRole(AssociationType.ASSOCIATION, "address",
 					Cardinality.ZERO, Cardinality.ONE));
 
 			user.addAbstractAttribute(association);
 			DynamicExtensionsUtility.getConstraintPropertiesForAssociation(association);
 			entityGroup.addEntity(user);
 			user.setEntityGroup(entityGroup);
 			entityGroup.addEntity(address);
 			address.setEntityGroup(entityGroup);
 			// Step 4
 			EntityInterface savedEntity = EntityManagerInterface.persistEntity(user);
 
 			Map dataValue = new HashMap();
 			Map addressDataValue = new HashMap();
 			addressDataValue.put(streetAttribute, "Laxmi Road");
 			addressDataValue.put(cityAttribute, "Pune");
 
 			List<Map> addressDataValueMapList = new ArrayList<Map>();
 			addressDataValueMapList.add(addressDataValue);
 
 			dataValue.put(userNameAttribute, "rahul");
 			dataValue.put(association, addressDataValueMapList);
 
 			// Step 5
 			EntityManagerInterface.insertData(savedEntity, dataValue);
 
 			System.out.println("show sql: " + System.getProperty("show_sql"));
 
 			// Step 6
 
 			assertEquals(Status.ACTIVITY_STATUS_ACTIVE.toString(), getActivityStatus(user, 1L));
 			assertEquals(Status.ACTIVITY_STATUS_ACTIVE.toString(), getActivityStatus(address, 1L));
 
 			EntityManagerInterface.deleteRecord(savedEntity, 1L);
 
 			assertEquals(Status.ACTIVITY_STATUS_DISABLED.toString(), getActivityStatus(user, 1L));
 			assertEquals(Status.ACTIVITY_STATUS_DISABLED.toString(), getActivityStatus(address, 1L));
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			Logger.out.debug(DynamicExtensionsUtility.getStackTrace(e));
 			fail();
 		}
 	}
 
 	/**
 	 *  PURPOSE: This method test for deleting data for a containtment relationship between two entities having one to many asso.
 	 *  EXPECTED BEHAVIOUR: Data of containted entity should also be disabled
 	 *
 	 *  TEST CASE FLOW: 1. create User
 	 *                  2. Create Address
 	 *                  3. Add Association with      User(1) ------->(*) Address containtment association
 	 *                  4. persist entities.
 	 *                  5. Insert Data
 	 *                  6. Delete User record, all associated addresses should also be deleted.
 	 */
 	public void testDeleteDataForContaintmentOneToMany()
 	{
 		EntityManagerInterface EntityManagerInterface = EntityManager.getInstance();
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 		EntityGroupInterface entityGroup = factory.createEntityGroup();
 		entityGroup.setName("test_" + new Double(Math.random()).toString());
 
 		try
 		{
 			// Step 1
 			EntityInterface user = createAndPopulateEntity();
 			AttributeInterface userNameAttribute = factory.createStringAttribute();
 			userNameAttribute.setName("user name");
 			user.setName("user");
 			user.addAbstractAttribute(userNameAttribute);
 			user.setEntityGroup(entityGroup);
 			entityGroup.addEntity(user);
 			// Step 2
 			EntityInterface address = createAndPopulateEntity();
 			address.setName("address");
 
 			AttributeInterface streetAttribute = factory.createStringAttribute();
 			streetAttribute.setName("street name");
 			address.addAbstractAttribute(streetAttribute);
 
 			AttributeInterface cityAttribute = factory.createStringAttribute();
 			cityAttribute.setName("city name");
 			address.addAbstractAttribute(cityAttribute);
 
 			// Step 3
 			AssociationInterface association = factory.createAssociation();
 			association.setTargetEntity(address);
 			association.setAssociationDirection(AssociationDirection.SRC_DESTINATION);
 			association.setName("UserAddress");
 			association.setSourceRole(getRole(AssociationType.CONTAINTMENT, "User",
 					Cardinality.ZERO, Cardinality.ONE));
 			association.setTargetRole(getRole(AssociationType.ASSOCIATION, "address",
 					Cardinality.ZERO, Cardinality.MANY));
 
 			user.addAbstractAttribute(association);
 			DynamicExtensionsUtility.getConstraintPropertiesForAssociation(association);
 			entityGroup.addEntity(user);
 			user.setEntityGroup(entityGroup);
 			entityGroup.addEntity(address);
 			address.setEntityGroup(entityGroup);
 			// Step 4
 			EntityInterface savedEntity = EntityManagerInterface.persistEntity(user);
 
 			Map dataValue = new HashMap();
 			Map officeAddress = new HashMap();
 			officeAddress.put(streetAttribute, "Laxmi Road");
 			officeAddress.put(cityAttribute, "Pune");
 
 			Map homeAddress = new HashMap();
 			homeAddress.put(streetAttribute, "Baner Road");
 			homeAddress.put(cityAttribute, "Pune");
 
 			List<Map> addressDataValueMapList = new ArrayList<Map>();
 			addressDataValueMapList.add(officeAddress);
 			addressDataValueMapList.add(homeAddress);
 
 			dataValue.put(userNameAttribute, "rahul");
 			dataValue.put(association, addressDataValueMapList);
 
 			// Step 5
 			EntityManagerInterface.insertData(savedEntity, dataValue);
 
 			assertEquals(Status.ACTIVITY_STATUS_ACTIVE.toString(), getActivityStatus(user, 1L));
 			assertEquals(Status.ACTIVITY_STATUS_ACTIVE.toString(), getActivityStatus(address, 1L));
 			assertEquals(Status.ACTIVITY_STATUS_ACTIVE.toString(), getActivityStatus(address, 2L));
 
 			// Step 6
 			EntityManagerInterface.deleteRecord(savedEntity, 1L);
 
 			assertEquals(Status.ACTIVITY_STATUS_DISABLED.toString(), getActivityStatus(user, 1L));
 			assertEquals(Status.ACTIVITY_STATUS_DISABLED.toString(), getActivityStatus(address, 1L));
 			assertEquals(Status.ACTIVITY_STATUS_DISABLED.toString(), getActivityStatus(address, 2L));
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			Logger.out.debug(DynamicExtensionsUtility.getStackTrace(e));
 			fail();
 		}
 	}
 
 	/** PURPOSE: This method test for deleting data for a one to many lookup association relationship between two entities.
 	 *  EXPECTED BEHAVIOUR: Data of looked up entity should not be disabled
 	 *
 	 *  TEST CASE FLOW: 1. create User
 	 *                  2. Create Study
 	 *                  3. Add Association with      User(1) ------->(*) Study lookup association
 	 *                  4. persist entities.
 	 *                  5. Insert Data
 	 *                  6. Delete User record,  associated study should NOT be deleted.
 	 */
 	public void testDeleteDataForAssociationOne2Many()
 	{
 		EntityManagerInterface EntityManagerInterface = EntityManager.getInstance();
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 		EntityGroupInterface entityGroup = factory.createEntityGroup();
 		entityGroup.setName("test_" + new Double(Math.random()).toString());
 
 		try
 		{
 			//create user
 			EntityInterface user = createAndPopulateEntity();
 			AttributeInterface userNameAttribute = factory.createStringAttribute();
 			userNameAttribute.setName("user name");
 			user.setName("user");
 			user.addAbstractAttribute(userNameAttribute);
 
 			//create study
 			EntityInterface study = createAndPopulateEntity();
 			AttributeInterface studyNameAttribute = factory.createStringAttribute();
 			studyNameAttribute.setName("study name");
 			study.setName("study");
 			study.addAbstractAttribute(studyNameAttribute);
 
 			//Associate user (1)------ >(*)study
 			AssociationInterface association = factory.createAssociation();
 			association.setTargetEntity(study);
 			association.setAssociationDirection(AssociationDirection.SRC_DESTINATION);
 			association.setName("primaryInvestigator");
 			association.setSourceRole(getRole(AssociationType.ASSOCIATION, "primaryInvestigator",
 					Cardinality.ZERO, Cardinality.ONE));
 			association.setTargetRole(getRole(AssociationType.ASSOCIATION, "study",
 					Cardinality.ZERO, Cardinality.MANY));
 
 			user.addAbstractAttribute(association);
 			DynamicExtensionsUtility.getConstraintPropertiesForAssociation(association);
 			user.setEntityGroup(entityGroup);
 			entityGroup.addEntity(study);
 			study.setEntityGroup(entityGroup);
 			entityGroup.addEntity(user);
 			//persist entity
 			EntityInterface savedEntity = EntityManagerInterface.persistEntity(user);
 
 			//Insert Data
 			Map dataValue = new HashMap();
 			dataValue.put(studyNameAttribute, "study");
 			EntityManagerInterface.insertData(study, dataValue);
 
 			dataValue.clear();
 			dataValue.put(studyNameAttribute, "study1");
 			EntityManagerInterface.insertData(study, dataValue);
 
 			dataValue.clear();
 			List<Long> targetIdList = new ArrayList<Long>();
 			targetIdList.add(1L);
 			targetIdList.add(2L);
 
 			dataValue.put(userNameAttribute, "rahul");
 			dataValue.put(association, targetIdList);
 
 			EntityManagerInterface.insertData(savedEntity, dataValue);
 
 			assertEquals(Status.ACTIVITY_STATUS_ACTIVE.toString(), getActivityStatus(user, 1L));
 			assertEquals(Status.ACTIVITY_STATUS_ACTIVE.toString(), getActivityStatus(study, 1L));
 			assertEquals(Status.ACTIVITY_STATUS_ACTIVE.toString(), getActivityStatus(study, 2L));
 
 			//Step 6:
 			EntityManagerInterface.deleteRecord(savedEntity, 1L);
 
 			assertEquals(Status.ACTIVITY_STATUS_DISABLED.toString(), getActivityStatus(user, 1L));
 
 			assertEquals(Status.ACTIVITY_STATUS_ACTIVE.toString(), getActivityStatus(study, 1L));
 			assertEquals(Status.ACTIVITY_STATUS_ACTIVE.toString(), getActivityStatus(study, 2L));
 		}
 		catch (Exception e)
 		{
 			Logger.out.debug(DynamicExtensionsUtility.getStackTrace(e));
 			e.printStackTrace();
 			fail();
 		}
 	}
 
 	/** PURPOSE: This method test for deleting data of one entity which is referred by a record of some
 	 *           other entity.
 	 *  EXPECTED BEHAVIOUR: In such a cases, record is not allowed to be deleted, appropriate applicationException should be thrown.
 	 *
 	 *  TEST CASE FLOW: 1. create User
 	 *                  2. Create Study
 	 *                  3. Add Association with      User(1) ------->(*) Study lookup association
 	 *                  4. persist entities.
 	 *                  5. Insert Data for study as "study"
 	 *                  6. Insert Data for study as  "study1"
 	 *                  7. Delete "study1" should be successful.
 	 *                  8. Insert Data for user  as "user" that refers to "study"
 	 *                  9. Delete "Study" : should throw exception as it is referred by "user"
 	 * @throws Exception
 	 */
 	public void testDeleteReferedData() throws Exception
 	{
 		EntityManagerInterface EntityManagerInterface = EntityManager.getInstance();
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 		EntityGroupInterface entityGroup = factory.createEntityGroup();
 		entityGroup.setName("test_" + new Double(Math.random()).toString());
 		EntityInterface study = null;
 
 		try
 		{
 			//Step 1
 			EntityInterface user = createAndPopulateEntity();
 			AttributeInterface userNameAttribute = factory.createStringAttribute();
 			userNameAttribute.setName("user name");
 			user.setName("user");
 			user.addAbstractAttribute(userNameAttribute);
 
 			//Step 2
 			study = createAndPopulateEntity();
 			AttributeInterface studyNameAttribute = factory.createStringAttribute();
 			studyNameAttribute.setName("study name");
 			study.setName("study");
 			study.addAbstractAttribute(studyNameAttribute);
 
 			//Step 3
 			AssociationInterface association = factory.createAssociation();
 			association.setTargetEntity(study);
 			association.setAssociationDirection(AssociationDirection.SRC_DESTINATION);
 			association.setName("primaryInvestigator");
 			association.setSourceRole(getRole(AssociationType.ASSOCIATION, "primaryInvestigator",
 					Cardinality.ZERO, Cardinality.MANY));
 			association.setTargetRole(getRole(AssociationType.ASSOCIATION, "study",
 					Cardinality.ZERO, Cardinality.ONE));
 
 			user.addAbstractAttribute(association);
 			DynamicExtensionsUtility.getConstraintPropertiesForAssociation(association);
 			user.setEntityGroup(entityGroup);
 			entityGroup.addEntity(study);
 			study.setEntityGroup(entityGroup);
 			entityGroup.addEntity(user);
 			//Step 4
 			EntityInterface savedEntity = EntityManagerInterface.persistEntity(user);
 
 			//Step 5
 			Map dataValue = new HashMap();
 			dataValue.put(studyNameAttribute, "study");
 			EntityManagerInterface.insertData(study, dataValue);
 
 			//Step 6
 			dataValue.clear();
 			dataValue.put(studyNameAttribute, "study1");
 			EntityManagerInterface.insertData(study, dataValue);
 
 			//Step 7
 			EntityManagerInterface.deleteRecord(study, 1L);
 			assertTrue(true);
 
 			assertEquals(Status.ACTIVITY_STATUS_ACTIVE.toString(), getActivityStatus(study, 2L));
 			assertEquals(Status.ACTIVITY_STATUS_DISABLED.toString(), getActivityStatus(study, 1L));
 
 			//Step 8
 			dataValue.clear();
 			List<Long> targetIdList = new ArrayList<Long>();
 			targetIdList.add(2L);
 
 			dataValue.put(userNameAttribute, "rahul");
 			dataValue.put(association, targetIdList);
 
 			EntityManagerInterface.insertData(savedEntity, dataValue);
 
 			//Step 9
 			EntityManagerInterface.deleteRecord(study, 2L);
 			fail();
 		}
 		catch (DynamicExtensionsApplicationException e)
 		{
 			assertTrue(true);
 			assertEquals(Status.ACTIVITY_STATUS_ACTIVE.toString(), getActivityStatus(study, 2L));
 			Logger.out.debug(DynamicExtensionsUtility.getStackTrace(e));
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			Logger.out.debug(DynamicExtensionsUtility.getStackTrace(e));
 			fail();
 		}
 	}
 
 	/**
 	* fix for bug 4075
 	*/
 	public void testInsertValueWithQuote()
 	{
 		EntityManagerInterface EntityManagerInterface = EntityManager.getInstance();
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 		EntityGroupInterface entityGroup = factory.createEntityGroup();
 		entityGroup.setName("test_" + new Double(Math.random()).toString());
 
 		try
 		{
 			// Step 1
 			EntityInterface specimen = createAndPopulateEntity();
 			specimen.setName("specimen");
 			specimen.setAbstract(true);
 			AttributeInterface barcode = factory.createStringAttribute();
 			barcode.setName("barcode");
 			specimen.addAbstractAttribute(barcode);
 			entityGroup.addEntity(specimen);
 			specimen.setEntityGroup(entityGroup);
 			Map dataValue = new HashMap();
 
 			dataValue.put(barcode, "123'456");
 
 			EntityManagerInterface.persistEntity(specimen);
 			Long recordId = EntityManagerInterface.insertData(specimen, dataValue);
 
 			assertTrue(true);
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			fail();
 		}
 	}
 
 	/** PURPOSE: This method tests for creating value domain information object
 	 *
 	 *  EXPECTED BEHAVIOUR: Attribute should be created successfully along with value domain information object.
 	 *  TEST CASE FLOW: 1.create test entity
 	 *                  2.create cadsr value domain information object
 	 * @throws Exception
 	 */
 	public void testCreateEntityAttributeWithValueDomain()
 	{
 		EntityManagerInterface EntityManagerInterface = EntityManager.getInstance();
 
 		try
 		{
 			DomainObjectFactory factory = DomainObjectFactory.getInstance();
 			EntityGroupInterface entityGroup = factory.createEntityGroup();
 			entityGroup.setName("test_" + new Double(Math.random()).toString());
 
 			// Step 1
 			EntityInterface specimen = createAndPopulateEntity();
 			specimen.setName("specimen");
 			specimen.setAbstract(true);
 			AttributeInterface barcode = factory.createStringAttribute();
 			barcode.setName("barcode");
 
 			//Step 2
 			CaDSRValueDomainInfoInterface caDSRValueDomainInfoInterface = factory
 					.createCaDSRValueDomainInfo();
 			caDSRValueDomainInfoInterface.setValueDomainType(ValueDomainType.ENUMERATED);
 			caDSRValueDomainInfoInterface.setDatatype("java.lang.String");
 			caDSRValueDomainInfoInterface.setName("java.lang.String");
 			barcode.setCaDSRValueDomainInfo(caDSRValueDomainInfoInterface);
 
 			SemanticPropertyInterface semanticPropertyInterface = factory.createSemanticProperty();
 			semanticPropertyInterface.setConceptDefinition("definition");
 			semanticPropertyInterface.setConceptCode("C1123");
 			semanticPropertyInterface.setSequenceNumber(1);
 			semanticPropertyInterface.setTerm("term");
 			semanticPropertyInterface.setThesaurasName("thesaurasName");
 
 			barcode.addSemanticProperty(semanticPropertyInterface);
 
 			specimen.addAbstractAttribute(barcode);
 
 			AttributeInterface label = factory.createStringAttribute();
 			label.setName("label");
 			label.setPublicId("public id 1");
 			specimen.addAbstractAttribute(label);
 
 			EntityInterface tissueSpecimen = createAndPopulateEntity();
 			tissueSpecimen.setParentEntity(specimen);
 			tissueSpecimen.populateEntityForConstraintProperties(false);
 			entityGroup.addEntity(tissueSpecimen);
 			tissueSpecimen.setEntityGroup(entityGroup);
 			entityGroup.addEntity(specimen);
 			specimen.setEntityGroup(entityGroup);
 			specimen = EntityManagerInterface.persistEntity(specimen);
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			fail();
 		}
 	}
 
 	/**
 	 *
 	 *
 	 */
 	public void testInsertObjectAttribute()
 	{
 		EntityManagerInterface EntityManagerInterface = EntityManager.getInstance();
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 		EntityGroup entityGroup = (EntityGroup) factory.createEntityGroup();
 		entityGroup.setName("testGroup" + new Double(Math.random()).toString());
 
 		// create user
 		EntityInterface user = createAndPopulateEntity();
 		user.setEntityGroup(entityGroup);
 		entityGroup.addEntity(user);
 
 		user.setName("User");
 
 		AttributeInterface age = factory.createIntegerAttribute();
 		age.setName("Age");
 		user.addAbstractAttribute(age);
 
 		AttributeInterface info = factory.createObjectAttribute();
 		info.setName("Resume");
 		user.addAbstractAttribute(info);
 		user.addAttribute(info);
 
 		try
 		{
 			user = EntityManagerInterface.persistEntity(user);
 
 			ObjectAttributeRecordValueInterface objectRecord = factory
 					.createObjectAttributeRecordValue();
 			objectRecord.setObject(new TaggedValue());
 			objectRecord.setClassName(TaggedValue.class.getName());
 
 			Map dataValue = new HashMap();
 			dataValue.put(age, "45");
 			dataValue.put(info, objectRecord);
 
 			Long recordId = EntityManagerInterface.insertData(user, dataValue);
 
 			int rowCount = (Integer) executeQuery("select count(*) from "
 					+ user.getTableProperties().getName(), INT_TYPE, 1);
 			assertEquals(1, rowCount);
 		}
 		catch (Throwable e)
 		{
 			e.printStackTrace();
 			fail();
 		}
 	}
 
 	public void testGerRecordForObjectAttribute()
 	{
 		EntityManagerInterface EntityManagerInterface = EntityManager.getInstance();
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 		EntityGroupInterface entityGroup = factory.createEntityGroup();
 		entityGroup.setName("test_" + new Double(Math.random()).toString());
 
 		// create user
 		EntityInterface user = createAndPopulateEntity();
 		user.setName("User");
 
 		AttributeInterface age = factory.createIntegerAttribute();
 		age.setName("Age");
 		user.addAbstractAttribute(age);
 
 		AttributeInterface info = factory.createObjectAttribute();
 		info.setName("Resume");
 		user.addAbstractAttribute(info);
 		user.addAttribute(info);
 		entityGroup.addEntity(user);
 		user.setEntityGroup(entityGroup);
 		try
 		{
 			TaggedValue taggedValue = new TaggedValue();
 			taggedValue.setKey("rahul");
 			taggedValue.setValue("ner");
 
 			user = EntityManagerInterface.persistEntity(user);
 
 			ObjectAttributeRecordValueInterface objectRecord = factory
 					.createObjectAttributeRecordValue();
 			objectRecord.setObject(taggedValue);
 			objectRecord.setClassName(TaggedValue.class.getName());
 
 			Map dataValue = new HashMap();
 			dataValue.put(age, "45");
 			dataValue.put(info, objectRecord);
 
 			Long recordId = EntityManagerInterface.insertData(user, dataValue);
 
 			int rowCount = (Integer) executeQuery("select count(*) from "
 					+ user.getTableProperties().getName(), INT_TYPE, 1);
 			assertEquals(1, rowCount);
 
 			dataValue = EntityManagerInterface.getRecordById(user, recordId);
 
 			assertEquals("45", dataValue.get(age).toString());
 			ObjectAttributeRecordValue attributeRecordValue = (ObjectAttributeRecordValue) dataValue
 					.get((info));
 			TaggedValue taggedValue1 = (TaggedValue) attributeRecordValue.getObject();
 
 			assertTrue(taggedValue1.getKey().equals("rahul"));
 			assertTrue(taggedValue1.getValue().equals("ner"));
 		}
 		catch (Throwable e)
 		{
 			e.printStackTrace();
 			fail();
 		}
 	}
 
 	public void testGerEntityRecordResultForObjectAttribute()
 	{
 		EntityManagerInterface EntityManagerInterface = EntityManager.getInstance();
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 		EntityGroupInterface entityGroup = factory.createEntityGroup();
 		entityGroup.setName("test_" + new Double(Math.random()).toString());
 
 		// create user
 		EntityInterface user = createAndPopulateEntity();
 		user.setName("User");
 
 		AttributeInterface age = factory.createIntegerAttribute();
 		age.setName("Age");
 		user.addAbstractAttribute(age);
 
 		AttributeInterface info = factory.createObjectAttribute();
 		info.setName("Resume");
 		user.addAbstractAttribute(info);
 		user.addAttribute(info);
 		entityGroup.addEntity(user);
 		user.setEntityGroup(entityGroup);
 		try
 		{
 			TaggedValue taggedValue = new TaggedValue();
 			taggedValue.setKey("rahul");
 			taggedValue.setValue("ner");
 
 			user = EntityManagerInterface.persistEntity(user);
 
 			ObjectAttributeRecordValueInterface objectRecord = factory
 					.createObjectAttributeRecordValue();
 			objectRecord.setObject(taggedValue);
 			objectRecord.setClassName(TaggedValue.class.getName());
 
 			Map dataValue = new HashMap();
 			dataValue.put(age, "45");
 			dataValue.put(info, objectRecord);
 
 			Long recordId = EntityManagerInterface.insertData(user, dataValue);
 
 			int rowCount = (Integer) executeQuery("select count(*) from "
 					+ user.getTableProperties().getName(), INT_TYPE, 1);
 			assertEquals(1, rowCount);
 
 			List userAttribute = new ArrayList();
 			userAttribute.add(age);
 			userAttribute.add(info);
 
 			EntityRecordResultInterface recordResult = EntityManagerInterface.getEntityRecords(
 					user, userAttribute, null);
 
 			EntityRecordInterface record = recordResult.getEntityRecordList().iterator().next();
 			assertEquals("45", record.getRecordValueList().get(0).toString());
 
 			ObjectAttributeRecordValue attributeRecordValue = (ObjectAttributeRecordValue) dataValue
 					.get((info));
 			TaggedValue taggedValue1 = (TaggedValue) attributeRecordValue.getObject();
 			assertTrue(taggedValue1.getKey().equals("rahul"));
 			assertTrue(taggedValue1.getValue().equals("ner"));
 		}
 		catch (Throwable e)
 		{
 			e.printStackTrace();
 			fail();
 		}
 	}
 
 	public void testEditRecordForObjectAttribute()
 	{
 		EntityManagerInterface EntityManagerInterface = EntityManager.getInstance();
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 		EntityGroupInterface entityGroup = factory.createEntityGroup();
 		entityGroup.setName("test_" + new Double(Math.random()).toString());
 
 		// create user
 		EntityInterface user = createAndPopulateEntity();
 		user.setName("User");
 
 		AttributeInterface age = factory.createIntegerAttribute();
 		age.setName("Age");
 		user.addAbstractAttribute(age);
 
 		AttributeInterface info = factory.createObjectAttribute();
 		info.setName("Resume");
 		user.addAbstractAttribute(info);
 		user.addAttribute(info);
 		entityGroup.addEntity(user);
 		user.setEntityGroup(entityGroup);
 		try
 		{
 			TaggedValue taggedValue = new TaggedValue();
 			taggedValue.setKey("rahul");
 			taggedValue.setValue("ner");
 
 			user = EntityManagerInterface.persistEntity(user);
 
 			ObjectAttributeRecordValueInterface objectRecord = factory
 					.createObjectAttributeRecordValue();
 			objectRecord.setObject(taggedValue);
 			objectRecord.setClassName(TaggedValue.class.getName());
 
 			Map dataValue = new HashMap();
 			dataValue.put(age, "45");
 			dataValue.put(info, objectRecord);
 
 			Long recordId = EntityManagerInterface.insertData(user, dataValue);
 
 			int rowCount = (Integer) executeQuery("select count(*) from "
 					+ user.getTableProperties().getName(), INT_TYPE, 1);
 			assertEquals(1, rowCount);
 
 			dataValue = EntityManagerInterface.getRecordById(user, recordId);
 
 			assertEquals("45", dataValue.get(age).toString());
 			ObjectAttributeRecordValue attributeRecordValue = (ObjectAttributeRecordValue) dataValue
 					.get((info));
 			TaggedValue taggedValue1 = (TaggedValue) attributeRecordValue.getObject();
 
 			assertTrue(taggedValue1.getKey().equals("rahul"));
 			assertTrue(taggedValue1.getValue().equals("ner"));
 
 			taggedValue1.setValue("ner1");
 
 			objectRecord.setClassName("new class name");
 			objectRecord.setObject(taggedValue1);
 
 			dataValue.clear();
 			dataValue.put(age, "54");
 			dataValue.put(info, objectRecord);
 
 			EntityManagerInterface.editData(user, dataValue, recordId);
 
 			dataValue = EntityManagerInterface.getRecordById(user, recordId);
 
 			assertEquals("54", dataValue.get(age).toString());
 			TaggedValue taggedValue2 = (TaggedValue) ((ObjectAttributeRecordValueInterface) dataValue
 					.get(info)).getObject();
 			assertTrue(taggedValue2.getKey().equals("rahul"));
 			assertTrue(taggedValue2.getValue().equals("ner1"));
 
 			assertEquals("new class name", ((ObjectAttributeRecordValueInterface) dataValue
 					.get(info)).getClassName());
 		}
 		catch (Throwable e)
 		{
 			e.printStackTrace();
 			fail();
 		}
 	}
 
 	/**
 	 *
 	 *
 	 */
 	/*
 		public void testDeleteRecordForObjectAttribute()
 		{
 			EntityManagerInterface EntityManagerInterface = EntityManager.getInstance();
 			DomainObjectFactory factory = DomainObjectFactory.getInstance();
 			EntityGroupInterface entityGroup = factory.createEntityGroup();
 			entityGroup.setName("test_" + new Double(Math.random()).toString());
 
 			// create user
 			EntityInterface user = createAndPopulateEntity();
 			user.setName("User");
 
 			AttributeInterface age = factory.createIntegerAttribute();
 			age.setName("Age");
 			user.addAbstractAttribute(age);
 
 			AttributeInterface info = factory.createObjectAttribute();
 			info.setName("Resume");
 			user.addAbstractAttribute(info);
 			user.addAttribute(info);
 			entityGroup.addEntity(user);
 			user.setEntityGroup(entityGroup);
 			try
 			{
 				TaggedValue taggedValue = new TaggedValue();
 				taggedValue.setKey("rahul");
 				taggedValue.setValue("ner");
 
 				user = EntityManagerInterface.persistEntity(user);
 
 				ObjectAttributeRecordValueInterface objectRecord = factory.createObjectAttributeRecordValue();
 				objectRecord.setObject(taggedValue);
 				objectRecord.setClassName(TaggedValue.class.getName());
 
 				Map dataValue = new HashMap();
 				dataValue.put(age, "45");
 				dataValue.put(info, objectRecord);
 
 				Long recordId = EntityManagerInterface.insertData(user, dataValue);
 
 				ResultSet resultSet = executeQuery("select count(*) from dyextn_attribute_record");
 				resultSet.next();
 				int beforeCnt = resultSet.getInt(1);
 				resultSet.close();
 
 				EntityManagerInterface.deleteRecord(user, recordId);
 
 				resultSet = executeQuery("select count(*) from dyextn_attribute_record");
 				resultSet.next();
 				int afterCnt = resultSet.getInt(1);
 
 				assertEquals(1, beforeCnt - afterCnt);
 			}
 			catch (Throwable e)
 			{
 				e.printStackTrace();
 				fail();
 			}
 		}*/
 
 	/**
 	 *
 	 *
 	 */
 	public void testEditEntityToAddObjectAttribute()
 	{
 		EntityManagerInterface EntityManagerInterface = EntityManager.getInstance();
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 		EntityGroupInterface entityGroup = factory.createEntityGroup();
 		entityGroup.setName("test_" + new Double(Math.random()).toString());
 
 		// create user
 		EntityInterface user = createAndPopulateEntity();
 		user.setName("User");
 
 		AttributeInterface age = factory.createIntegerAttribute();
 		age.setName("Age");
 		user.addAbstractAttribute(age);
 		entityGroup.addEntity(user);
 		user.setEntityGroup(entityGroup);
 		try
 		{
 			user = EntityManagerInterface.persistEntity(user);
 
 			AttributeInterface info = factory.createObjectAttribute();
 			info.setName("Resume");
 			user.addAbstractAttribute(info);
 			user.addAttribute(info);
 
 			user = EntityManagerInterface.persistEntity(user);
 
 			ObjectAttributeRecordValueInterface objectRecord = factory
 					.createObjectAttributeRecordValue();
 			objectRecord.setObject(new TaggedValue());
 			objectRecord.setClassName(TaggedValue.class.getName());
 
 			Map dataValue = new HashMap();
 			dataValue.put(age, "45");
 			dataValue.put(info, objectRecord);
 
 			Long recordId = EntityManagerInterface.insertData(user, dataValue);
 
 			int rowCOunt = (Integer) executeQuery("select count(*) from "
 					+ user.getTableProperties().getName(), INT_TYPE, 1);
 			assertEquals(1, rowCOunt);
 		}
 		catch (Throwable e)
 		{
 			e.printStackTrace();
 			fail();
 		}
 	}
 
 	/**
 	 *
 	 *
 	 */
 	public void testGetAllGroupBeans()
 	{
 		try
 		{
 			EntityManager.getInstance().getAllEntityGroupBeans();
 		}
 		catch (Exception e)
 		{
 			fail();
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Create one entity group, add one entity to it and save it.
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	public void testSaveEntityGroupWithIDGenerator() throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		EntityGroupManagerInterface EntityManager = EntityGroupManager.getInstance();
 		EntityGroupInterface entityGroup = DomainObjectFactory.getInstance().createEntityGroup();
 		EntityInterface e1 = new MockEntityManager().initializeEntity3();
 		entityGroup.addEntity(e1);
 		e1.setEntityGroup(entityGroup);
 		try
 		{
 			EntityManager.persistEntityGroup(entityGroup);
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			fail();
 		}
 	}
 
 	/**
 	 * Test case to create entity with one attribute having permissible values.
 	 */
 	public void testCreateEntityWithAttributeHavingPermissibleValues()
 	{
 		try
 		{
 			DomainObjectFactory factory = DomainObjectFactory.getInstance();
 
 			EntityGroupInterface entityGroup = factory.createEntityGroup();
 			entityGroup.setName("Entity Group 1");
 
 			// Create vitals entity.
 			EntityInterface vitalsEntity = createAndPopulateEntity();
 			vitalsEntity.setName("vitalsEntity");
 
 			AttributeInterface bmi = factory.createStringAttribute();
 			bmi.setName("BMI");
 			((StringAttributeTypeInformation) bmi.getAttributeTypeInformation()).setSize(40);
 
 			vitalsEntity.addAbstractAttribute(bmi);
 
 			// Add permissible values.
 			UserDefinedDEInterface userDefinedDE = factory.createUserDefinedDE();
 
 			PermissibleValueInterface permissibleValue1 = factory.createStringValue();
 			((StringValue) permissibleValue1).setValue("Underweight: 18.5 or below");
 
 			PermissibleValueInterface permissibleValue2 = factory.createStringValue();
 			((StringValue) permissibleValue2).setValue("Healthy Weight: 18.5 - 24.9");
 
 			PermissibleValueInterface permissibleValue3 = factory.createStringValue();
 			((StringValue) permissibleValue3).setValue("Overweight: 25.0 - 29.9");
 
 			PermissibleValueInterface permissibleValue4 = factory.createStringValue();
 			((StringValue) permissibleValue4).setValue("Obese: 30.0 and above");
 
 			userDefinedDE.addPermissibleValue(permissibleValue1);
 			userDefinedDE.addPermissibleValue(permissibleValue2);
 			userDefinedDE.addPermissibleValue(permissibleValue3);
 			userDefinedDE.addPermissibleValue(permissibleValue4);
 
 			StringAttributeTypeInformation bmiTypeInfo = (StringAttributeTypeInformation) bmi
 					.getAttributeTypeInformation();
 			bmiTypeInfo.setDataElement(userDefinedDE);
 			bmiTypeInfo.setDefaultValue(permissibleValue2);
 
 			entityGroup.addEntity(vitalsEntity);
 
 			EntityGroupManager.getInstance().persistEntityGroup(entityGroup);
 		}
 		catch (DynamicExtensionsSystemException e)
 		{
 			e.printStackTrace();
 			fail();
 		}
 		catch (DynamicExtensionsApplicationException e)
 		{
 			e.printStackTrace();
 			fail();
 		}
 	}
 
 	/**
 	 * unique rule
 	 */
 	public void testUniqueRule()
 	{
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 		EntityGroupInterface entityGroup = factory.createEntityGroup();
 		entityGroup.setName("unique");
 
 		//Step 1
 		Entity entity = (Entity) createAndPopulateEntity();
 		entity.setName("unique_rule");
 		EntityManagerInterface EntityManagerInterface = EntityManager.getInstance();
 		Long recordId1 = null;
 		Long recordId2 = null;
 
 		try
 		{
 			Attribute floatAtribute = (Attribute) factory.createFloatAttribute();
 			floatAtribute.setName("Price");
 
 			NumericTypeInformationInterface numericAttributeTypeInfo = (NumericTypeInformationInterface) factory
 					.createFloatAttributeTypeInformation();
 			numericAttributeTypeInfo.setDecimalPlaces(2);
 
 			floatAtribute.setAttributeTypeInformation(numericAttributeTypeInfo);
 
 			RuleInterface uniqueRule = factory.createRule();
 			uniqueRule.setName("unique");
 			floatAtribute.getRuleCollection().add(uniqueRule);
 
 			entity.addAbstractAttribute(floatAtribute);
 			entityGroup.addEntity(entity);
 			entity.setEntityGroup(entityGroup);
 
 			//Step 2
 			EntityInterface savedEntity = EntityManagerInterface.persistEntity(entity);
 
 			Map dataValue = new HashMap();
 			dataValue.put(floatAtribute, "15.90");
 
 			recordId1 = EntityManagerInterface.insertData(savedEntity, dataValue);
 
 			dataValue = EntityManagerInterface.getRecordById(entity, recordId1);
 
 			for (RuleInterface rule : floatAtribute.getRuleCollection())
 			{
 				ValidatorRuleInterface validatorRule = ControlConfigurationsFactory.getInstance()
 						.getValidatorRule(rule.getName());
 				validatorRule.validate(floatAtribute, "15.90", null, "Date");
 			}
 
 			Map dataValue2 = new HashMap();
 			dataValue2.put(floatAtribute, "15.90");
 
 			recordId2 = EntityManagerInterface.insertData(savedEntity, dataValue2);
 			assertEquals(recordId2, null);
 		}
 		catch (DynamicExtensionsValidationException e)
 		{
 			System.out.println("Could not insert data....");
 			System.out.println("Validation failed. Input data should be unique");
 			assertEquals(recordId2, null);
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			fail();
 		}
 	}
 
 	/**
 	 * numeric data types implicit rule.
 	 */
 	public void testImplicitRuleForFloatDataType()
 	{
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 		EntityGroupInterface entityGroup = factory.createEntityGroup();
 		entityGroup.setName("NumberValidations");
 
 		//Step 1
 		Entity entity = (Entity) createAndPopulateEntity();
 		entity.setName("Numeric");
 		EntityManagerInterface entityManager = EntityManager.getInstance();
 
 		Long recordId = null;
 
 		try
 		{
 			Attribute floatTypeAttribute = (Attribute) factory.createFloatAttribute();
 			floatTypeAttribute.setName("floatTypeAttribute");
 
 			RuleInterface rule1 = factory.createRule();
 			rule1.setName("number");
 			rule1.setIsImplicitRule(true);
 			floatTypeAttribute.getRuleCollection().add(rule1);
 
 			entity.addAbstractAttribute(floatTypeAttribute);
 			entityGroup.addEntity(entity);
 			entity.setEntityGroup(entityGroup);
 
 			//Step 2
 			EntityInterface savedEntity = entityManager.persistEntity(entity);
 
 			String incorrectValue = "ABC";
 
 			Map dataValue = new HashMap();
 			dataValue.put(floatTypeAttribute, incorrectValue);
 
 			for (RuleInterface rule : floatTypeAttribute.getRuleCollection())
 			{
 				ValidatorRuleInterface validatorRule = ControlConfigurationsFactory.getInstance()
 						.getValidatorRule(rule.getName());
 				validatorRule.validate(floatTypeAttribute, incorrectValue, null, floatTypeAttribute
 						.getName());
 			}
 
 			recordId = entityManager.insertData(savedEntity, dataValue);
 			assertEquals(recordId, null);
 		}
 		catch (DynamicExtensionsValidationException e)
 		{
 			System.out.println("Could not insert data....");
 			System.out.println(e.getMessage());
 			assertEquals(recordId, null);
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			fail();
 		}
 	}
 
 	/**
 	 * Test duplicate form name validations.
 	 */
 	public void testDuplicateFormName()
 	{
 		EntityInterface mainForm = null;
 		EntityInterface subForm = null;
 		ContainerInterface mainFormContainer = null;
 		ContainerInterface subFormContainer = null;
 		AssociationInterface association = null;
 
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 		EntityManagerInterface entityManager = EntityManager.getInstance();
 
 		EntityGroupInterface entityGroup = factory.createEntityGroup();
 		entityGroup.setName("DuplicateFormsValidations");
 
 		// Create an entity.
 		mainForm = (Entity) createAndPopulateEntity();
 		mainForm.setName("mainForm");
 
 		entityGroup.addEntity(mainForm);
 		mainForm.setEntityGroup(entityGroup);
 
 		mainFormContainer = factory.createContainer();
 		mainFormContainer.setCaption("mainForm");
 
 		mainFormContainer.setAbstractEntity(mainForm);
 		mainForm.getContainerCollection().add(mainFormContainer);
 
 		subForm = (Entity) createAndPopulateEntity();
 		subForm.setName("mainForm");
 
 		subFormContainer = factory.createContainer();
 		subFormContainer.setCaption("mainForm");
 
 		subFormContainer.setAbstractEntity(subForm);
 		subForm.getContainerCollection().add(subFormContainer);
 
 		try
 		{
 			try
 			{
 				DynamicExtensionsUtility.checkIfEntityPreExists(entityGroup, subFormContainer,
 						subFormContainer.getCaption(), mainFormContainer);
 
 				// Association should not get created as sub form's name is same as that of main form.
 				association = factory.createAssociation();
 				association.setTargetEntity(subForm);
 				association.setAssociationDirection(AssociationDirection.SRC_DESTINATION);
 				association.setName("mainForm-->subForm association");
 				association.setSourceRole(getRole(AssociationType.CONTAINTMENT, mainForm.getName(),
 						Cardinality.ONE, Cardinality.ONE));
 				association.setTargetRole(getRole(AssociationType.CONTAINTMENT, subForm.getName(),
 						Cardinality.ONE, Cardinality.MANY));
 				ConstraintPropertiesInterface constraintProperties = DynamicExtensionsUtility
						.getConstraintProperties(association);
 				association.setConstraintProperties(constraintProperties);
 				mainForm.addAssociation(association);
 				DynamicExtensionsUtility.getConstraintPropertiesForAssociation(association);
 
 				entityManager.persistEntity(mainForm);
 			}
 			catch (DynamicExtensionsApplicationException e)
 			{
 				assertEquals(e.getMessage(), "Duplicate form name within same entity group!");
 				assertEquals(e.getErrorCode(),
 						EntityManagerExceptionConstantsInterface.DYEXTN_A_019);
 				assertEquals(mainForm.getId(), null);
 				assertEquals(association, null);
 			}
 
 			try
 			{
 				entityManager.persistEntity(mainForm);
 
 				DynamicExtensionsUtility.checkIfEntityPreExists(entityGroup, subFormContainer,
 						subFormContainer.getCaption());
 
 				// Entity group here must not contain sub form as its name is same as that of main form.
 				entityGroup.addEntity(subForm);
 				subForm.setEntityGroup(entityGroup);
 			}
 			catch (DynamicExtensionsApplicationException e)
 			{
 				assertEquals(e.getMessage(), "Duplicate form name within same entity group!");
 				assertEquals(e.getErrorCode(),
 						EntityManagerExceptionConstantsInterface.DYEXTN_A_019);
 				assertNotNull(mainForm.getId());
 				assertEquals(mainForm.getAssociationCollection().size(), 0);
 				assertNotNull(entityGroup.getEntityByName(subForm.getName()));
 			}
 
 			try
 			{
 				subForm.setName("subForm");
 				subFormContainer.setCaption("subForm");
 
 				DynamicExtensionsUtility.checkIfEntityPreExists(entityGroup, subFormContainer,
 						subFormContainer.getCaption());
 
 				// Since sub form name is different from that of main form now,
 				//  entity group must contain this sub form.
 				entityGroup.addEntity(subForm);
 				subForm.setEntityGroup(entityGroup);
 
 				// Likewise, association should get created between main form and sub form.
 				association = factory.createAssociation();
 				association.setTargetEntity(subForm);
 				association.setAssociationDirection(AssociationDirection.SRC_DESTINATION);
 				association.setName("mainForm-->subForm association");
 				association.setSourceRole(getRole(AssociationType.CONTAINTMENT, mainForm.getName(),
 						Cardinality.ONE, Cardinality.ONE));
 				association.setTargetRole(getRole(AssociationType.CONTAINTMENT, subForm.getName(),
 						Cardinality.ONE, Cardinality.MANY));
 				ConstraintPropertiesInterface constraintProperties = DynamicExtensionsUtility
						.getConstraintProperties(association);
 				association.setConstraintProperties(constraintProperties);
 
				mainForm.addAssociation(association);
				DynamicExtensionsUtility.getConstraintPropertiesForAssociation(association);

 				entityManager.persistEntity(mainForm);
 
 				// sub form should now be a persistent object, and the main form's association
 				// collection must now contain an association whose target entity is sub form.
 				assertNotNull(mainForm.getId());
 				assertNotNull(subForm.getId());
 				assertNotNull(association);
 				assertNotNull(association.getId());
 				assertNotNull(entityGroup.getEntityByName(subForm.getName()));
 				assertNotSame(mainForm.getAssociationCollection().size(), 0);
 			}
 			catch (DynamicExtensionsApplicationException e)
 			{
 				e.printStackTrace();
 				fail();
 			}
 		}
 		catch (DynamicExtensionsSystemException e)
 		{
 			e.printStackTrace();
 			fail();
 		}
 	}
 	
 	/**
 	 * Module tested: DE sql auditing of update queries generated by the DE 
 	 * Verifying conditions: Insert data for an entity, check contents of table DYXTN_SQL_AUDIT 
 	 * Classes involved : EntityManager, CatgeoryManager, DynamicExtensionBaseQueryBuilder
 	 */
 	public void testSqlAudit()
 	{
 		//Test for auditing of data of type Date
 		int rowCountBeforeExecutingQuery = (Integer) executeQuery(
 				"select count(*) from dyextn_sql_audit", INT_TYPE, 1);
 		testInsertDataForDate();
 		int rowCountAfterExecutingQuery = (Integer) executeQuery(
 				"select count(*) from dyextn_sql_audit", INT_TYPE, 1);
 		if (!(rowCountAfterExecutingQuery > rowCountBeforeExecutingQuery))
 		{
 			fail("Queries for data of type 'Date' are not audited!!");
 		}
 		
 		//Test for auditing of data of type File
 		rowCountBeforeExecutingQuery = (Integer) executeQuery(
 				"select count(*) from dyextn_sql_audit", INT_TYPE, 1);
 		testInsertRecordForFileAttribute();
 		rowCountAfterExecutingQuery = (Integer) executeQuery(
 				"select count(*) from dyextn_sql_audit", INT_TYPE, 1);
 		if (!(rowCountAfterExecutingQuery > rowCountBeforeExecutingQuery))
 		{
 			fail("Queries for data of type 'File' are not audited!!");
 		}
 
 		//Test for auditing of data of type Object
 		rowCountBeforeExecutingQuery = (Integer) executeQuery(
 				"select count(*) from dyextn_sql_audit", INT_TYPE, 1);
 		testInsertObjectAttribute();
 		rowCountAfterExecutingQuery = (Integer) executeQuery(
 				"select count(*) from dyextn_sql_audit", INT_TYPE, 1);
 		if (!(rowCountAfterExecutingQuery > rowCountBeforeExecutingQuery))
 		{
 			fail("Queries for data of type 'Object' are not audited!!");
 		}
 		
 	}
 
 }
