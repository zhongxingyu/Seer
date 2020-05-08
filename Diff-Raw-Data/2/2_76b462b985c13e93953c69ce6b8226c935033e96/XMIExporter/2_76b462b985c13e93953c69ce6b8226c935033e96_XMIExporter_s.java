 /*
  *
  */
 
 package edu.common.dynamicextensions.xmi.exporter;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.StringTokenizer;
 
 import javax.jmi.model.ModelPackage;
 import javax.jmi.model.MofPackage;
 import javax.jmi.reflect.RefPackage;
 import javax.jmi.xmi.XmiReader;
 import javax.jmi.xmi.XmiWriter;
 import javax.xml.transform.TransformerException;
 
 import org.apache.commons.lang.StringUtils;
 import org.netbeans.api.mdr.CreationFailedException;
 import org.netbeans.api.mdr.MDRepository;
 import org.omg.uml.UmlPackage;
 import org.omg.uml.foundation.core.AssociationEnd;
 import org.omg.uml.foundation.core.Attribute;
 import org.omg.uml.foundation.core.Classifier;
 import org.omg.uml.foundation.core.CorePackage;
 import org.omg.uml.foundation.core.Dependency;
 import org.omg.uml.foundation.core.Feature;
 import org.omg.uml.foundation.core.Generalization;
 import org.omg.uml.foundation.core.Operation;
 import org.omg.uml.foundation.core.Parameter;
 import org.omg.uml.foundation.core.Relationship;
 import org.omg.uml.foundation.core.Stereotype;
 import org.omg.uml.foundation.core.TaggedValue;
 import org.omg.uml.foundation.core.UmlAssociation;
 import org.omg.uml.foundation.core.UmlClass;
 import org.omg.uml.foundation.datatypes.AggregationKindEnum;
 import org.omg.uml.foundation.datatypes.CallConcurrencyKindEnum;
 import org.omg.uml.foundation.datatypes.ChangeableKindEnum;
 import org.omg.uml.foundation.datatypes.DataTypesPackage;
 import org.omg.uml.foundation.datatypes.Multiplicity;
 import org.omg.uml.foundation.datatypes.MultiplicityRange;
 import org.omg.uml.foundation.datatypes.OrderingKindEnum;
 import org.omg.uml.foundation.datatypes.ParameterDirectionKindEnum;
 import org.omg.uml.foundation.datatypes.ScopeKindEnum;
 import org.omg.uml.foundation.datatypes.VisibilityKindEnum;
 import org.omg.uml.modelmanagement.Model;
 import org.omg.uml.modelmanagement.ModelManagementPackage;
 import org.openide.util.Lookup;
 
 import edu.common.dynamicextensions.dao.impl.DynamicExtensionDAO;
 import edu.common.dynamicextensions.domain.BooleanAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.DateAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.DomainObjectFactory;
 import edu.common.dynamicextensions.domain.EntityGroup;
 import edu.common.dynamicextensions.domain.FileAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.NumericAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.StringAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.userinterface.SelectControl;
 import edu.common.dynamicextensions.domaininterface.AbstractAttributeInterface;
 import edu.common.dynamicextensions.domaininterface.AbstractMetadataInterface;
 import edu.common.dynamicextensions.domaininterface.AssociationDisplayAttributeInterface;
 import edu.common.dynamicextensions.domaininterface.AssociationInterface;
 import edu.common.dynamicextensions.domaininterface.AttributeInterface;
 import edu.common.dynamicextensions.domaininterface.AttributeTypeInformationInterface;
 import edu.common.dynamicextensions.domaininterface.EntityGroupInterface;
 import edu.common.dynamicextensions.domaininterface.EntityInterface;
 import edu.common.dynamicextensions.domaininterface.RoleInterface;
 import edu.common.dynamicextensions.domaininterface.SemanticPropertyInterface;
 import edu.common.dynamicextensions.domaininterface.TaggedValueInterface;
 import edu.common.dynamicextensions.domaininterface.databaseproperties.ColumnPropertiesInterface;
 import edu.common.dynamicextensions.domaininterface.databaseproperties.ConstraintKeyPropertiesInterface;
 import edu.common.dynamicextensions.domaininterface.databaseproperties.ConstraintPropertiesInterface;
 import edu.common.dynamicextensions.domaininterface.databaseproperties.TablePropertiesInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.ControlInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.ListBoxInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.TextAreaInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.TextFieldInterface;
 import edu.common.dynamicextensions.domaininterface.validationrules.RuleInterface;
 import edu.common.dynamicextensions.domaininterface.validationrules.RuleParameterInterface;
 import edu.common.dynamicextensions.entitymanager.EntityManager;
 import edu.common.dynamicextensions.entitymanager.EntityManagerConstantsInterface;
 import edu.common.dynamicextensions.entitymanager.EntityManagerInterface;
 import edu.common.dynamicextensions.entitymanager.EntityManagerUtil;
 import edu.common.dynamicextensions.exception.DataTypeFactoryInitializationException;
 import edu.common.dynamicextensions.exception.DynamicExtensionsApplicationException;
 import edu.common.dynamicextensions.exception.DynamicExtensionsSystemException;
 import edu.common.dynamicextensions.util.DynamicExtensionsUtility;
 import edu.common.dynamicextensions.util.global.DEConstants.AssociationDirection;
 import edu.common.dynamicextensions.util.global.DEConstants.AssociationType;
 import edu.common.dynamicextensions.util.global.DEConstants.Cardinality;
 import edu.common.dynamicextensions.xmi.XMIConstants;
 import edu.common.dynamicextensions.xmi.XMIUtilities;
 import edu.wustl.common.util.logger.Logger;
 import edu.wustl.common.util.logger.LoggerConfig;
 import edu.wustl.dao.HibernateDAO;
 import edu.wustl.dao.daofactory.DAOConfigFactory;
 import edu.wustl.dao.exception.DAOException;
 import edu.wustl.dao.query.generator.ColumnValueBean;
 import edu.wustl.metadata.util.PackageName;
 
 /**
  * @author falguni_sachde
  * @author pavan_kalantri
  *
  */
 public class XMIExporter
 {
 
 	//Repository
 	private MDRepository repository;
 
 	static
 	{
 		LoggerConfig.configureLogger(System.getProperty("user.dir"));
 	}
 	private static final Logger LOGGER = Logger.getCommonLogger(XMIExporter.class);
 	private static final String UML_MM = "UML";
 	// UML extent
 	private UmlPackage umlPackage;
 
 	//Model
 	private Model umlModel = null;
 	//Leaf package
 	private org.omg.uml.modelmanagement.UmlPackage logicalModel = null;
 	private org.omg.uml.modelmanagement.UmlPackage dataModel = null;
 	private Map<String, UmlClass> entityUMLClassMappings = null;
 	private Map<String, UmlClass> entityDataClassMappings = null;
 	private Map<EntityInterface, List<String>> entityForeignKeyAttributes = null;
 	private Map<String, String> foreignKeyOperationNameMapping = null;
 	private String packageName = null;
 	private String xmiVersion;
 	private String groupName;
 	private String filename;
 	private String hookEntityName;
 
 	private EntityGroupInterface entityGroup;
 	private EntityInterface staticEntity;
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.xmi.exporter.XMIExportInterface#exportXMI(java.lang.String, javax.jmi.reflect.RefPackage, java.lang.String)
 	 */
 	public void writeXMIFile(final RefPackage extent) throws IOException, TransformerException
 	{
 		//get xmi writer
 		final XmiWriter writer = XMIUtilities.getXMIWriter();
 		String outputFilename = filename;
 		if (XMIConstants.XMI_VERSION_1_1.equals(xmiVersion))
 		{
 			//Write to temporary file
 			outputFilename = XMIConstants.TEMPORARY_XMI1_1_FILENAME;
 		}
 
 		//get output stream for file : appendmode : false
 		final FileOutputStream outputStream = new FileOutputStream(outputFilename, false);
 		repository.beginTrans(true);
 		try
 		{
 			writer.write(outputStream, extent, xmiVersion);
 			if (XMIConstants.XMI_VERSION_1_1.equals(xmiVersion))
 			{
 				convertXMI(outputFilename, filename);
 			}
 			//			System.out.println( "XMI written successfully");
 		}
 		finally
 		{
 			repository.endTrans(true);
 			repository.shutdown();
 			// shutdown the repository to make sure all caches are flushed to disk
 			//MDRManager.getDefault().shutdownAll();
 			XMIUtilities.cleanUpRepository(filename);
 			outputStream.close();
 			if (new File(XMIConstants.TEMPORARY_XMI1_1_FILENAME).exists())
 			{
 				new File(XMIConstants.TEMPORARY_XMI1_1_FILENAME).delete();
 			}
 		}
 	}
 
 	/**
 	 * @param srcFilename
 	 * @param targetFilename
 	 * @throws TransformerException
 	 * @throws FileNotFoundException
 	 *
 	 */
 	private void convertXMI(final String srcFilename, final String targetFilename)
 			throws FileNotFoundException, TransformerException
 	{
 		final InputStream xsltFileStream = this.getClass().getClassLoader().getResourceAsStream(
 				XMIConstants.XSLT_FILENAME);
 		XMIUtilities.transform(srcFilename, targetFilename, xsltFileStream);
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.xmi.exporter.XMIExportInterface#exportXMI(java.lang.String, edu.common.dynamicextensions.domaininterface.EntityGroupInterface, java.lang.String)
 	 */
 	public void exportXMI() throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		if (XMIConstants.XMI_VERSION_1_1.equalsIgnoreCase(xmiVersion)
 				&& !XMIConstants.NONE.equalsIgnoreCase(hookEntityName))
 		{
 			staticEntity = XMIExporterUtility.getHookEntityName(entityGroup);
 			XMIExporterUtility.addHookEntitiesToGroup(staticEntity, entityGroup);
 		}
 		exportXMI(entityGroup, null);
 	}
 
 	/**
 	 * It will retrieve the entity group with the given name using the passed hibernateDao.
 	 * @param hibernateDao hibernateDao used for retrieving the entity group.
 	 * @param groupName entity group name to retrieve.
 	 * @return entity group with given name
 	 * @throws DynamicExtensionsApplicationException exception.
 	 * @throws DynamicExtensionsSystemException exception.
 	 * @throws DAOException exception.
 	 */
 	private EntityGroupInterface getEntityGroup(HibernateDAO hibernateDao, String groupName)
 			throws DynamicExtensionsApplicationException, DynamicExtensionsSystemException,
 			DAOException
 	{
 		ColumnValueBean colValueBean = new ColumnValueBean("name", groupName);
 		List entityGroupList = hibernateDao.retrieve(EntityGroup.class.getName(), colValueBean);
 
 		//List entityGroupList = hibernateDao.retrieve(EntityGroup.class.getName(), "name", groupName);
 
 		if (entityGroupList == null || entityGroupList.isEmpty())
 		{
 			throw new DynamicExtensionsApplicationException(
 					"Specified group does not exist. Could not export to XMI");
 		}
 		return ((EntityGroupInterface) entityGroupList.get(0));
 	}
 
 	/**
 	 * @param filename
 	 * @param entityGroup
 	 * @param xmiVersion
 	 * @param modelpackageName
 	 * @throws CreationFailedException
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DAOException
 	 * @throws DynamicExtensionsApplicationException
 	 * @throws Exception
 	 */
 	public void exportXMI(final EntityGroupInterface entityGroup, final String modelpackageName)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		try
 		{
 			init();
 			if (entityGroup != null)
 			{
 				//groupName = entityGroup.getName();
 				//UML Model generation
 				generateUMLModel(entityGroup, modelpackageName);
 
 				//Data Model creation
 				if (XMIConstants.XMI_VERSION_1_1.equalsIgnoreCase(xmiVersion))
 				{
 					generateDataModel(entityGroup);
 				}
 
 				writeXMIFile(umlPackage);
 			}
 		}
 		catch (final CreationFailedException e)
 		{
 			throw new DynamicExtensionsApplicationException("Unable to create UML Package ", e);
 		}
 		catch (final TransformerException e)
 		{
 			throw new DynamicExtensionsApplicationException("Unable to write the XMI ", e);
 		}
 		catch (final IOException e)
 		{
 			throw new DynamicExtensionsApplicationException("Unable to write the XMI ", e);
 		}
 	}
 
 	/**
 	 * @param entityGroup
 	 * @param xmiVersion
 	 * @throws DynamicExtensionsApplicationException
 	 * @throws DynamicExtensionsSystemException
 	 */
 	private void generateDataModel(final EntityGroupInterface entityGroup)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		if (entityGroup != null)
 		{
 			initializeEntityForeignKeysMap(entityGroup.getEntityCollection());
 			Collection<UmlClass> sqlTableClasses;
 			//Generate relationships between table classes
 			Collection<Relationship> sqlRelationships;
 			if (XMIConstants.XMI_VERSION_1_2.equals(xmiVersion))
 			{
 				sqlTableClasses = getDataClasses(getEntities(entityGroup));
 				dataModel.getOwnedElement().addAll(sqlTableClasses);
 				sqlRelationships = getSQLRelationShips(getEntities(entityGroup));
 			}
 			else
 			{
 				sqlTableClasses = getDataClasses(entityGroup.getEntityCollection());
 				dataModel.getOwnedElement().addAll(sqlTableClasses);
 				sqlRelationships = getSQLRelationShips(entityGroup.getEntityCollection());
 			}
 			dataModel.getOwnedElement().addAll(sqlRelationships);
 		}
 	}
 
 	/**
 	 * @param entityCollection
 	 */
 	private void initializeEntityForeignKeysMap(final Collection<EntityInterface> entityCollection)
 	{
 		entityForeignKeyAttributes = new HashMap<EntityInterface, List<String>>();
 		if (entityCollection != null)
 		{
 			entityCollection.iterator();
 			for (EntityInterface entity : entityCollection)
 			{
 				final Collection<AssociationInterface> entityAssociations = entity
 						.getAssociationCollection();
 				if (entityAssociations != null)
 				{
 					for (AssociationInterface association : entityAssociations)
 					{
 						handleForiegnKeyOperationNameMapForAssociation(association);
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * This method will update the foreign key map for the given association
 	 * @param association association to be verify
 	 */
 	private void handleForiegnKeyOperationNameMapForAssociation(
 			final AssociationInterface association)
 	{
 		if (association.getConstraintProperties() != null)
 		{
 			final String associationType = getAssociationType(association);
 			//For one-to-one and one-to-many association foreign key is in target entity
 			if ((associationType.equals(XMIConstants.ASSOC_ONE_MANY))
 					|| (associationType.equals(XMIConstants.ASSOC_ONE_ONE)))
 			{
 
 				Collection<ConstraintKeyPropertiesInterface> cnstKeyPropColl = association
 						.getConstraintProperties().getTgtEntityConstraintKeyPropertiesCollection();
 				Integer counter = 1;
 				for (final ConstraintKeyPropertiesInterface cnstrKeyProp : cnstKeyPropColl)
 				{
 					String foreignKey = cnstrKeyProp.getTgtForiegnKeyColumnProperties().getName();
 					addForeignKeyAttribute(association.getTargetEntity(), foreignKey);
 					//Generate foreign key operation name and add it to foreignKeyOperationNameMappings map
 					String foreignKeyOperationName = generateForeignkeyOperationName(association
 							.getTargetEntity().getName(), association.getEntity().getName().concat(
 							counter.toString()));
 					foreignKeyOperationNameMapping.put(foreignKey, foreignKeyOperationName);
 					counter++;
 				}
 			}
 			//For many-to-one association foreign key is in source entity
 			else if (associationType.equals(XMIConstants.ASSOC_MANY_ONE))
 			{
 				Collection<ConstraintKeyPropertiesInterface> cnstKeyPropColl = association
 						.getConstraintProperties().getSrcEntityConstraintKeyPropertiesCollection();
 				final Integer counter = 1;
 				for (final ConstraintKeyPropertiesInterface cnstrKeyProp : cnstKeyPropColl)
 				{
 					String foreignKey = cnstrKeyProp.getTgtForiegnKeyColumnProperties().getName();
 					addForeignKeyAttribute(association.getEntity(), foreignKey);
 					String foreignKeyOperationName = generateForeignkeyOperationName(association
 							.getEntity().getName(), association.getTargetEntity().getName().concat(
 							counter.toString()));
 					//Generate foreign key operation name and add it to foreignKeyOperationNameMappings map
 					foreignKeyOperationNameMapping.put(foreignKey, foreignKeyOperationName);
 				}
 			}
 		}
 	}
 
 	/**
 	 * @param targetEntity
 	 * @param entity
 	 * @return
 	 */
 	private String generateForeignkeyOperationName(final String foreignKeyEntityName,
 			final String primaryKeyEntityName)
 	{
 		return (XMIConstants.FOREIGN_KEY_PREFIX + foreignKeyEntityName + XMIConstants.SEPARATOR + primaryKeyEntityName);
 	}
 
 	/**
 	 * @param targetEntity
 	 * @param targetEntityKey
 	 */
 	private void addForeignKeyAttribute(final EntityInterface entity,
 			final String entityForeignKeyAttribute)
 	{
 		if ((entity != null) && (entityForeignKeyAttribute != null))
 		{
 			List<String> foreignKeys = entityForeignKeyAttributes.get(entity);
 			if (foreignKeys == null)
 			{
 				foreignKeys = new ArrayList<String>();
 			}
 			foreignKeys.add(entityForeignKeyAttribute);
 			entityForeignKeyAttributes.put(entity, foreignKeys);
 		}
 	}
 
 	/**
 	 * @param entityCollection
 	 * @param xmiVersion
 	 * @return
 	 * @throws DataTypeFactoryInitializationException
 	 */
 	private Collection<Relationship> getSQLRelationShips(
 			final Collection<EntityInterface> entityCollection)
 			throws DataTypeFactoryInitializationException
 	{
 		final ArrayList<Relationship> sqlRelationships = new ArrayList<Relationship>();
 		if (entityCollection != null)
 		{
 			final Iterator<EntityInterface> entityCollnIter = entityCollection.iterator();
 			while (entityCollnIter.hasNext())
 			{
 				final EntityInterface entity = entityCollnIter.next();
 				final Collection<Relationship> entitySQLAssociations = createSQLRelationships(entity);
 				sqlRelationships.addAll(entitySQLAssociations);
 			}
 		}
 		return sqlRelationships;
 	}
 
 	/**
 	 * @param entity
 	 * @param xmiVersion
 	 * @return
 	 * @throws DataTypeFactoryInitializationException
 	 */
 	private Collection<Relationship> createSQLRelationships(final EntityInterface entity)
 			throws DataTypeFactoryInitializationException
 	{
 		//Associations
 		final ArrayList<Relationship> entitySQLRelationships = new ArrayList<Relationship>();
 		if (entity != null)
 		{
 			//Association relationships
 			Collection<AssociationInterface> entityAssociations;
 			if (XMIConstants.XMI_VERSION_1_2.equals(xmiVersion))
 			{
 				entityAssociations = entity.getAssociationCollectionExcludingCollectionAttributes();
 			}
 			else
 			{
 				entityAssociations = entity.getAssociationCollection();
 			}
 			if (entityAssociations != null)
 			{
 				final Iterator<AssociationInterface> entityAssociationsIter = entityAssociations
 						.iterator();
 				while (entityAssociationsIter.hasNext())
 				{
 					final AssociationInterface association = entityAssociationsIter.next();
 					//For each association add the sourceEntityKey as foreign key operation
 					final UmlAssociation sqlAssociation = createSQLAssociation(association);
 					if (sqlAssociation != null)
 					{
 						entitySQLRelationships.add(sqlAssociation);
 					}
 				}
 			}
 			//Generalization relationships
 			if (entity.getParentEntity() != null)
 			{
 				final UmlClass parentClass = entityDataClassMappings.get(entity.getParentEntity()
 						.getName());
 				final UmlClass childClass = entityDataClassMappings.get(entity.getName());
 				createForeignKeyAttribute(entity);
 				final Generalization generalization = createGeneralization(parentClass, childClass);
 				entitySQLRelationships.add(generalization);
 			}
 		}
 		return entitySQLRelationships;
 	}
 
 	/**
 	 * This will add the column created for inheritance relationship to the data
 	 * class and there foreignKey constraint operation name
 	 * @param entity child entity
 	 * @throws DataTypeFactoryInitializationException
 	 */
 	private void createForeignKeyAttribute(final EntityInterface entity)
 			throws DataTypeFactoryInitializationException
 	{
 		final Collection<ConstraintKeyPropertiesInterface> cnstKeyPropColl = entity
 				.getConstraintProperties().getSrcEntityConstraintKeyPropertiesCollection();
 		final Classifier foreignKeySQLClass = getSQLClassForEntity(entity.getName());
 		String columnName;
 		for (final ConstraintKeyPropertiesInterface cnstrKeyProp : cnstKeyPropColl)
 		{
 			columnName = cnstrKeyProp.getTgtForiegnKeyColumnProperties().getName();
 			Attribute foreignKeyAttribute = searchAttribute(foreignKeySQLClass, columnName);
 			//Create attribute if does not exist
 			if (foreignKeyAttribute == null)
 			{
 				//Datatype of foreign key and prmary key will be same
 				//AttributeInterface primaryKeyAttr = getPrimaryKeyAttribute(primaryKeyEntity);
 				final AttributeInterface primaryKeyAttr = cnstrKeyProp.getSrcPrimaryKeyAttribute();
 				foreignKeyAttribute = createDataAttribute(columnName, primaryKeyAttr.getDataType());
 				final String foreignKeyOprName = generateForeignkeyOperationName(entity.getName(),
 						entity.getParentEntity().getName());
 				foreignKeyOperationNameMapping
 						.put(foreignKeyAttribute.getName(), foreignKeyOprName);
 				foreignKeySQLClass.getFeature().add(foreignKeyAttribute);
 				//Add foreign key operation
 				foreignKeySQLClass.getFeature().add(createForeignKeyOperation(foreignKeyAttribute));
 
 			}
 		}
 	}
 
 	/**
 	 * @param association
 	 * @param targetEntityAttribute
 	 * @return
 	 */
 	@SuppressWarnings("unchecked")
 	private Operation createForeignKeyOperation(final Attribute foreignKeyColumn)
 	{
 		final String foreignKeyOperationName = getForeignkeyOperationName(foreignKeyColumn
 				.getName());
 		final Operation foreignKeyOperation = umlPackage.getCore().getOperation().createOperation(
 				foreignKeyOperationName, VisibilityKindEnum.VK_PUBLIC, false,
 				ScopeKindEnum.SK_INSTANCE, false, CallConcurrencyKindEnum.CCK_SEQUENTIAL, false,
 				false, false, null);
 
 		foreignKeyOperation.getStereotype().addAll(
 				getOrCreateStereotypes(XMIConstants.FOREIGN_KEY,
 						XMIConstants.STEREOTYPE_BASECLASS_ATTRIBUTE));
 		foreignKeyOperation.getTaggedValue().add(
 				createTaggedValue(XMIConstants.STEREOTYPE, XMIConstants.FOREIGN_KEY));
 
 		//Return parameter
 		final Parameter returnParameter = createParameter(null, null,
 				ParameterDirectionKindEnum.PDK_RETURN);
 		final Parameter inParameter = createParameter(foreignKeyColumn.getName(), foreignKeyColumn
 				.getType(), ParameterDirectionKindEnum.PDK_IN);
 		inParameter.getTaggedValue().add(
 				createTaggedValue(XMIConstants.TYPE, foreignKeyColumn.getType().getName()));
 
 		foreignKeyOperation.getParameter().add(returnParameter);
 		foreignKeyOperation.getParameter().add(inParameter);
 		foreignKeyOperationNameMapping.put(foreignKeyColumn.getName(), foreignKeyOperationName);
 		return foreignKeyOperation;
 	}
 
 	/**
 	 * @param name
 	 * @return
 	 */
 	private String getForeignkeyOperationName(final String foreignKeyName)
 	{
 		String operationName = null;
 		if ((foreignKeyOperationNameMapping != null) && (foreignKeyName != null))
 		{
 			operationName = foreignKeyOperationNameMapping.get(foreignKeyName);
 		}
 		return operationName;
 	}
 
 	/**
 	 * @return
 	 */
 	private Parameter createParameter(final String parameterName, final Classifier parameterType,
 			final ParameterDirectionKindEnum direction)
 	{
 		//Return parameter
 		final Parameter parameter = umlPackage.getCore().getParameter().createParameter(
 				parameterName, VisibilityKindEnum.VK_PUBLIC, false, null, direction);
 		parameter.setType(parameterType);
 		return parameter;
 	}
 
 	/**
 	 * @param association
 	 * @return
 	 * @throws DataTypeFactoryInitializationException
 	 */
 	@SuppressWarnings("unchecked")
 	private UmlAssociation createSQLAssociation(final AssociationInterface association)
 			throws DataTypeFactoryInitializationException
 	{
 		UmlAssociation sqlAssociation = null;
 		if ((association != null) && (association.getConstraintProperties() != null))
 		{
 			final ConstraintPropertiesInterface constraintProperties = association
 					.getConstraintProperties();
 			final String associationName = getAssociationName(association);
 			sqlAssociation = umlPackage.getCore().getUmlAssociation().createUmlAssociation(
 					associationName, VisibilityKindEnum.VK_PUBLIC, false, false, false, false);
 			if (sqlAssociation != null)
 			{
 				//Set the ends for the association
 				//End that is on the 'many' side of the association will have foreign key oprn name & on 'one' side will be primary key oprn name
 				final String associationType = getAssociationType(association);
 				final Classifier sourceSQLClass = getSQLClassForEntity(association.getEntity()
 						.getName());
 				final Classifier targetSQLClass = getSQLClassForEntity(association
 						.getTargetEntity().getName());
 				final RoleInterface sourceRole = association.getSourceRole();
 				final RoleInterface targetRole = association.getTargetRole();
 
 				if ((associationType.equals(XMIConstants.ASSOC_ONE_ONE))
 						|| (associationType.equals(XMIConstants.ASSOC_ONE_MANY)))
 				{
 
 					getForeignKeyAttribute(association.getTargetEntity(), constraintProperties
 							.getTgtEntityConstraintKeyPropertiesCollection(), association
 							.getSourceRole().getName());
 					//One-One OR One-Many source will have primary key, target has foreign key
 					/*sourceRole.setName(getPrimaryKeyOperationName(
 							association.getEntity().getName(), constraintProperties
 									.getSrcEntityConstraintKeyProperties()
 									.getTgtForiegnKeyColumnProperties().getName()));
 					targetRole.setName(getForeignkeyOperationName(constraintProperties
 							.getTgtEntityConstraintKeyProperties()
 							.getTgtForiegnKeyColumnProperties().getName()));*/
 					sourceRole.setName(association.getSourceRole().getName());
 					targetRole.setName(association.getTargetRole().getName());
 
 					sqlAssociation.getConnection().add(
 							getAssociationEnd(sourceRole, sourceSQLClass,
 									XMIConstants.ASSN_SRC_ENTITY));
 					sqlAssociation.getConnection().add(
 							getAssociationEnd(targetRole, targetSQLClass,
 									XMIConstants.ASSN_TGT_ENTITY));
 
 				}
 				else if (associationType.equals(XMIConstants.ASSOC_MANY_ONE))
 				{
 					getForeignKeyAttribute(association.getEntity(), constraintProperties
 							.getSrcEntityConstraintKeyPropertiesCollection(), association
 							.getTargetRole().getName());
 					//Many-One source will have foreign key, target primary key
 					/*sourceRole.setName(getForeignkeyOperationName(constraintProperties
 							.getTgtEntityConstraintKeyProperties()
 							.getTgtForiegnKeyColumnProperties().getName()));
 					targetRole.setName(getPrimaryKeyOperationName(association.getTargetEntity()
 							.getName(), constraintProperties.getSrcEntityConstraintKeyProperties()
 							.getTgtForiegnKeyColumnProperties().getName()));*/
 					sourceRole.setName(association.getSourceRole().getName());
 					targetRole.setName(association.getTargetRole().getName());
 					sqlAssociation.getConnection().add(
 							getAssociationEnd(sourceRole, sourceSQLClass,
 									XMIConstants.ASSN_SRC_ENTITY));
 					sqlAssociation.getConnection().add(
 							getAssociationEnd(targetRole, targetSQLClass,
 									XMIConstants.ASSN_TGT_ENTITY));
 
 				}
 				else if (associationType.equals(XMIConstants.ASSOC_MANY_MANY))
 				{
 					handleManyToManyAssociation(association);
 					return null;
 				}
 				//set the direction
 				final TaggedValue directionTaggedValue = getDirectionTaggedValue(association);
 				if (directionTaggedValue != null)
 				{
 					sqlAssociation.getTaggedValue().add(directionTaggedValue);
 				}
 				sqlAssociation.getStereotype().addAll(
 						getOrCreateStereotypes(XMIConstants.FOREIGN_KEY,
 								XMIConstants.STEREOTYPE_BASECLASS_ASSOCIATION));
 				sqlAssociation.getTaggedValue().add(
 						createTaggedValue(XMIConstants.STEREOTYPE, XMIConstants.FOREIGN_KEY));
 			}
 		}
 		return sqlAssociation;
 
 	}
 
 	/**
 	 * @param foreignKeyEntity
 	 * @param cnstKeyPropColl
 	 * @param implementedAssociationName
 	 * @return null
 	 * @throws DataTypeFactoryInitializationException
 	 */
 	@SuppressWarnings("unchecked")
 	private Attribute getForeignKeyAttribute(final EntityInterface foreignKeyEntity,
 			final Collection<ConstraintKeyPropertiesInterface> cnstKeyPropColl,
 			final String implementedAssociationName) throws DataTypeFactoryInitializationException
 	{
 		final Classifier foreignKeySQLClass = getSQLClassForEntity(foreignKeyEntity.getName());
 		String columnName;
 		for (final ConstraintKeyPropertiesInterface cnstrKeyProp : cnstKeyPropColl)
 		{
 			columnName = cnstrKeyProp.getTgtForiegnKeyColumnProperties().getName();
 			Attribute foreignKeyAttribute = searchAttribute(foreignKeySQLClass, columnName);
 			final EntityInterface primaryKeyEntity = cnstrKeyProp.getSrcPrimaryKeyAttribute()
 					.getEntity();
 			//Create attribute if does not exist
 			if (foreignKeyAttribute == null)
 			{
 				//Datatype of foreign key and prmary key will be same
 				//AttributeInterface primaryKeyAttr = getPrimaryKeyAttribute(primaryKeyEntity);
 				final AttributeInterface primaryKeyAttr = cnstrKeyProp.getSrcPrimaryKeyAttribute();
 				foreignKeyAttribute = createDataAttribute(columnName, primaryKeyAttr.getDataType());
 				foreignKeySQLClass.getFeature().add(foreignKeyAttribute);
 				//Add foreign key operation
 				foreignKeySQLClass.getFeature().add(createForeignKeyOperation(foreignKeyAttribute));
 			}
 			String implementedAssociation;
 			//TODO  check
 			if (XMIConstants.XMI_VERSION_1_2.equals(xmiVersion))
 			{
 				implementedAssociation = packageName + XMIConstants.COLON_SEPARATOR
 						+ foreignKeyEntity.getName() + XMIConstants.COLON_SEPARATOR
 						+ implementedAssociationName + XMIConstants.COLON_SEPARATOR + columnName
 						+ XMIConstants.COLON_SEPARATOR + primaryKeyEntity.getName();
 			}
 			else
 			{
 				implementedAssociation = packageName + XMIConstants.DOT_SEPARATOR
 						+ foreignKeyEntity.getName() + XMIConstants.DOT_SEPARATOR
 						+ implementedAssociationName;
 			}
 			if (foreignKeyAttribute != null)
 			{
 				foreignKeyAttribute.getTaggedValue().add(
 						createTaggedValue(XMIConstants.TAGGED_VALUE_IMPLEMENTS_ASSOCIATION,
 								implementedAssociation));
 			}
 		}
 
 		return null;
 	}
 
 	/**
 	 * @param constraintProperties
 	 * @throws DataTypeFactoryInitializationException
 	 */
 	@SuppressWarnings("unchecked")
 	private void handleManyToManyAssociation(final AssociationInterface association)
 			throws DataTypeFactoryInitializationException
 	{
 		//Create corelation table
 		final UmlClass corelationTable = createCoRelationTable(association);
 
 		if (corelationTable != null)
 		{
 			//Relation with source entity
 			final UmlClass sourceClass = (UmlClass) getSQLClassForEntity(association.getEntity()
 					.getName());
 			final UmlClass targetClass = (UmlClass) getSQLClassForEntity(association
 					.getTargetEntity().getName());
 			final RoleInterface role = getRole(AssociationType.ASSOCIATION, null, Cardinality.ONE,
 					Cardinality.ONE);
 			final UmlAssociation srcToCoRelnTable = createAssocForCorelnTable(sourceClass,
 					corelationTable, association.getSourceRole(), role);
 			//Create relation with target entity
 			final UmlAssociation coRelnTableToTarget = createAssocForCorelnTable(corelationTable,
 					targetClass, role, association.getTargetRole());
 			//Add to data model
 			dataModel.getOwnedElement().add(corelationTable);
 			dataModel.getOwnedElement().add(coRelnTableToTarget);
 			dataModel.getOwnedElement().add(srcToCoRelnTable);
 		}
 	}
 
 	/**
 	 * @param association
 	 * @return
 	 */
 	@SuppressWarnings("unchecked")
 	private UmlAssociation createAssocForCorelnTable(final UmlClass sourceEntity,
 			final UmlClass targetEntity, final RoleInterface sourceRole,
 			final RoleInterface targetRole)
 	{
 		final UmlAssociation umlAssociation = umlPackage.getCore().getUmlAssociation()
 				.createUmlAssociation(null, VisibilityKindEnum.VK_PUBLIC, false, false, false,
 						false);
 		final AssociationEnd sourceEnd = getAssociationEnd(sourceRole, sourceEntity,
 				XMIConstants.ASSN_SRC_ENTITY);
 		final AssociationEnd targetEnd = getAssociationEnd(targetRole, targetEntity,
 				XMIConstants.ASSN_TGT_ENTITY);
 		umlAssociation.getConnection().add(targetEnd);
 		umlAssociation.getConnection().add(sourceEnd);
 		umlAssociation.getTaggedValue().add(
 				createTaggedValue(XMIConstants.TAGGED_NAME_ASSOC_DIRECTION,
 						XMIConstants.TAGGED_VALUE_ASSOC_SRC_DEST));
 		return umlAssociation;
 	}
 
 	/**
 	 * @param association
 	 * @return
 	 * @throws DataTypeFactoryInitializationException
 	 */
 	@SuppressWarnings("unchecked")
 	private UmlClass createCoRelationTable(final AssociationInterface association)
 			throws DataTypeFactoryInitializationException
 	{
 		UmlClass tableName = null;
 		final ConstraintPropertiesInterface constraintProperties = association
 				.getConstraintProperties();
 		if (constraintProperties != null)
 		{
 			final String coRelationTableName = constraintProperties.getName();
 			tableName = createDataClass(coRelationTableName);
 
 			final Collection<Feature> coRelationAttributes = createCoRelationTableAttribsAndOperns(association);
 			//Add to co-relation class
 			tableName.getFeature().addAll(coRelationAttributes);
 		}
 		return tableName;
 	}
 
 	/**
 	 * @param association
 	 * @return
 	 * @throws DataTypeFactoryInitializationException
 	 */
 	@SuppressWarnings("unchecked")
 	private Collection<Feature> createCoRelationTableAttribsAndOperns(
 			final AssociationInterface association) throws DataTypeFactoryInitializationException
 	{
 		final ArrayList<Feature> corelationTableFeatures = new ArrayList<Feature>();
 		final ConstraintPropertiesInterface constraintProperties = association
 				.getConstraintProperties();
 		final Collection<ConstraintKeyPropertiesInterface> srcCnstKeyProps = constraintProperties
 				.getSrcEntityConstraintKeyPropertiesCollection();
 		final Collection<ConstraintKeyPropertiesInterface> tgtCnstKeyProps = constraintProperties
 				.getTgtEntityConstraintKeyPropertiesCollection();
 		for (final ConstraintKeyPropertiesInterface cnstrKeyProp : srcCnstKeyProps)
 		{
 			final Attribute coRelationAttribute = createCoRelationalAttributeAndOperns(
 					cnstrKeyProp, association.getEntity());
 			//Add "implements-association tagged value
 			String srcAttribImplementedAssocn = null;
 			if (XMIConstants.XMI_VERSION_1_2.equals(xmiVersion))
 			{
 				//TODO- check
 				srcAttribImplementedAssocn = packageName + XMIConstants.COLON_SEPARATOR
 						+ association.getTargetEntity().getName() + XMIConstants.COLON_SEPARATOR
 						+ association.getSourceRole().getName() + XMIConstants.COLON_SEPARATOR
 						+ cnstrKeyProp.getTgtForiegnKeyColumnProperties().getName()
 						+ XMIConstants.COLON_SEPARATOR + association.getEntity().getName();
 			}
 			else
 			{
 				srcAttribImplementedAssocn = packageName + XMIConstants.DOT_SEPARATOR
 						+ association.getTargetEntity().getName() + XMIConstants.DOT_SEPARATOR
 						+ association.getSourceRole().getName();
 			}
 			coRelationAttribute.getTaggedValue().add(
 					createTaggedValue(XMIConstants.TAGGED_VALUE_IMPLEMENTS_ASSOCIATION,
 							srcAttribImplementedAssocn));
 			corelationTableFeatures.add(coRelationAttribute);
 			final String foreignKeyOprName = generateForeignkeyOperationName(association
 					.getTargetEntity().getName(), constraintProperties.getName());
 			foreignKeyOperationNameMapping.put(coRelationAttribute.getName(), foreignKeyOprName);
 			corelationTableFeatures.add(createForeignKeyOperation(coRelationAttribute));
 		}
 		for (final ConstraintKeyPropertiesInterface cnstrKeyProp : tgtCnstKeyProps)
 		{
 			final Attribute coRelationAttribute = createCoRelationalAttributeAndOperns(
 					cnstrKeyProp, association.getTargetEntity());
 			String targetAttribImplementedAssocn;
 			if (XMIConstants.XMI_VERSION_1_2.equals(xmiVersion))
 			{
 				targetAttribImplementedAssocn = packageName + XMIConstants.COLON_SEPARATOR
 						+ association.getEntity().getName() + XMIConstants.COLON_SEPARATOR
 						+ association.getTargetRole().getName() + XMIConstants.COLON_SEPARATOR
 						+ cnstrKeyProp.getTgtForiegnKeyColumnProperties().getName()
 						+ XMIConstants.COLON_SEPARATOR + association.getEntity().getName();
 			}
 			else
 			{
 				targetAttribImplementedAssocn = packageName + XMIConstants.DOT_SEPARATOR
 						+ association.getEntity().getName() + XMIConstants.DOT_SEPARATOR
 						+ association.getTargetRole().getName();
 			}
 			coRelationAttribute.getTaggedValue().add(
 					createTaggedValue(XMIConstants.TAGGED_VALUE_IMPLEMENTS_ASSOCIATION,
 							targetAttribImplementedAssocn));
 			corelationTableFeatures.add(coRelationAttribute);
 			final String foreignKeyOprName = generateForeignkeyOperationName(constraintProperties
 					.getName(), association.getEntity().getName());
 			foreignKeyOperationNameMapping.put(coRelationAttribute.getName(), foreignKeyOprName);
 			corelationTableFeatures.add(createForeignKeyOperation(coRelationAttribute));
 		}
 		return corelationTableFeatures;
 	}
 
 	/**
 	 * It will create the Attribute to be added in the corelational table sets its name & datatype
 	 * @param cnstrKeyProp
 	 * @param entity
 	 * @return
 	 * @throws DataTypeFactoryInitializationException
 	 */
 	private Attribute createCoRelationalAttributeAndOperns(
 			final ConstraintKeyPropertiesInterface cnstrKeyProp, final EntityInterface entity)
 			throws DataTypeFactoryInitializationException
 	{
 		Attribute coRelationAttribute = null;
 		if (cnstrKeyProp.getSrcPrimaryKeyAttribute() != null)
 		{
 			final String corelationTableAttributeName = generateCorelationAttributeName(entity,
 					cnstrKeyProp.getTgtForiegnKeyColumnProperties().getName());
 			coRelationAttribute = createDataAttribute(corelationTableAttributeName, cnstrKeyProp
 					.getSrcPrimaryKeyAttribute().getDataType());
 		}
 		return coRelationAttribute;
 	}
 
 	/**
 	 * Generates qualified name for the corelation table attributes
 	 * Column name generated as <EntityName>_<EntityAttribute>
 	 * @param entity
 	 * @param sourceEntityKey
 	 * @return
 	 */
 	private String generateCorelationAttributeName(final EntityInterface entity,
 			final String sourceEntityKey)
 	{
		return entity.getName() + XMIConstants.SEPARATOR + sourceEntityKey;
 	}
 
 	/**
 	 * @param association
 	 * @return
 	 */
 	private TaggedValue getDirectionTaggedValue(final AssociationInterface association)
 	{
 		TaggedValue directionTaggedValue;
 		if (association.getAssociationDirection().equals(AssociationDirection.BI_DIRECTIONAL))
 		{
 			directionTaggedValue = createTaggedValue(XMIConstants.TAGGED_NAME_ASSOC_DIRECTION,
 					XMIConstants.TAGGED_VALUE_ASSOC_BIDIRECTIONAL);
 		}
 		else
 		{
 			directionTaggedValue = createTaggedValue(XMIConstants.TAGGED_NAME_ASSOC_DIRECTION,
 					XMIConstants.TAGGED_VALUE_ASSOC_SRC_DEST);
 		}
 		return directionTaggedValue;
 	}
 
 	/**
 	 * @param association
 	 * @return
 	 */
 	private String getAssociationName(final AssociationInterface association)
 	{
 		return association.getName();
 	}
 
 	/**
 	 * @param targetEntity
 	 * @param targetEntityKey
 	 * @return
 	 */
 	private Attribute searchAttribute(final Classifier targetEntityUmlClass,
 			final String attributeName)
 	{
 		Attribute umlAttribute = null;
 		if ((targetEntityUmlClass != null) && (attributeName != null))
 		{
 			final List entityAttributes = targetEntityUmlClass.getFeature();
 			if (entityAttributes != null)
 			{
 				final Iterator entityAttribIter = entityAttributes.iterator();
 				while (entityAttribIter.hasNext())
 				{
 					final Object attribute = entityAttribIter.next();
 					if ((attribute instanceof Attribute)
 							&& ((Attribute) attribute).getName().equals(attributeName))
 					{
 						umlAttribute = (Attribute) attribute;
 						break;
 					}
 				}
 			}
 		}
 		return umlAttribute;
 	}
 
 	/**
 	 * @param entityCollection
 	 * @return
 	 * @throws DynamicExtensionsApplicationException
 	 * @throws DynamicExtensionsSystemException
 	 */
 	@SuppressWarnings("unchecked")
 	private Collection<UmlClass> getDataClasses(final Collection<EntityInterface> entityCollection)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		final ArrayList<UmlClass> sqlTableClasses = new ArrayList<UmlClass>();
 		if (entityCollection != null)
 		{
 			final Iterator entityCollectionIter = entityCollection.iterator();
 			while (entityCollectionIter.hasNext())
 			{
 				final EntityInterface entity = (EntityInterface) entityCollectionIter.next();
 				final UmlClass sqlTableClass = createDataClass(entity);
 				if (sqlTableClass != null)
 				{
 					sqlTableClasses.add(sqlTableClass);
 					entityDataClassMappings.put(entity.getName(), sqlTableClass);
 					//Create dependency with parent
 					final UmlClass entityUmlClass = entityUMLClassMappings.get(entity.getName());
 					dataModel.getOwnedElement()
 							.add(createDependency(entityUmlClass, sqlTableClass));
 				}
 
 			}
 		}
 		return sqlTableClasses;
 	}
 
 	/**
 	 * @param entity
 	 * @return
 	 * @throws DynamicExtensionsApplicationException
 	 * @throws DynamicExtensionsSystemException
 	 */
 	@SuppressWarnings("unchecked")
 	private UmlClass createDataClass(final EntityInterface entity)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		UmlClass entityDataClass = null;
 		if (entity != null)
 		{
 			final TablePropertiesInterface tableProps = entity.getTableProperties();
 			if (tableProps != null)
 			{
 				final String tableName = tableProps.getName();
 				entityDataClass = createDataClass(tableName);
 				final List<String> foreignKeyAttributes = entityForeignKeyAttributes.get(entity);
 				//Entity Attributes & Operations(Primary Key) of data class
 				if (XMIConstants.XMI_VERSION_1_2.equals(xmiVersion))
 				{
 					entityDataClass.getFeature().addAll(
 							getSQLClassAttributesAndOperations(getAllAttributesForEntity(entity),
 									foreignKeyAttributes));
 				}
 				else
 				{
 					entityDataClass.getFeature().addAll(
 							getSQLClassAttributesAndOperations(entity.getEntityAttributes(),
 									foreignKeyAttributes));
 				}
 			}
 		}
 		return entityDataClass;
 	}
 
 	/**
 	 * @param tableName
 	 * @return
 	 */
 	@SuppressWarnings("unchecked")
 	private UmlClass createDataClass(final String tableName)
 	{
 		final String appName = DynamicExtensionDAO.getInstance().getAppName();
 		final String dbType = DAOConfigFactory.getInstance().getDAOFactory(appName)
 				.getDataBaseType();
 		final UmlClass dataClass = umlPackage.getCore().getUmlClass().createUmlClass(tableName,
 				VisibilityKindEnum.VK_PUBLIC, false, false, false, false, false);
 		//Table stereotype
 		dataClass.getStereotype()
 				.addAll(
 						getOrCreateStereotypes(XMIConstants.TABLE,
 								XMIConstants.STEREOTYPE_BASECLASS_CLASS));
 		dataClass.getTaggedValue().add(
 				createTaggedValue(XMIConstants.STEREOTYPE, XMIConstants.TABLE));
 		dataClass.getTaggedValue().add(
 				createTaggedValue(XMIConstants.TAGGED_VALUE_GEN_TYPE, dbType));
 		dataClass.getTaggedValue().add(
 				createTaggedValue(XMIConstants.TAGGED_VALUE_PRODUCT_NAME, dbType));
 		return dataClass;
 	}
 
 	/**
 	 * @param foreignKeyAttributes
 	 * @param allAttributes
 	 * @return
 	 * @throws DataTypeFactoryInitializationException
 	 */
 	@SuppressWarnings("unchecked")
 	private Collection<Feature> getSQLClassAttributesAndOperations(
 			final Collection<AttributeInterface> entityAttributes,
 			final List<String> entityForeignKeys) throws DataTypeFactoryInitializationException
 	{
 		//Add attributes and operations
 		final ArrayList<Feature> classFeatures = new ArrayList<Feature>();
 		//Add Attributes and primary keys as operations
 		if (entityAttributes != null)
 		{
 			final Iterator entityAttributesIter = entityAttributes.iterator();
 			while (entityAttributesIter.hasNext())
 			{
 				final AttributeInterface attribute = (AttributeInterface) entityAttributesIter
 						.next();
 				//process all attribute datatype other than File type attribute
 				if (attribute.getDataType() != EntityManagerConstantsInterface.FILE_ATTRIBUTE_TYPE)
 				{
 					final Attribute umlAttribute = createDataAttribute(attribute);
 					classFeatures.add(umlAttribute);
 					//If primary key : add as operation
 					if (attribute.getIsPrimaryKey())
 					{
 						final Operation primaryKeyOperationSpecn = createPrimaryKeyOperation(
 								attribute, umlAttribute);
 						if (primaryKeyOperationSpecn != null)
 						{
 							classFeatures.add(primaryKeyOperationSpecn);
 						}
 					}
 
 					//If attribute is a foreign key attribute add foreign key operation
 					//elseif  attribute not in foreign key list, add "mapped-attributes" tagged value
 					if (isForeignKey(umlAttribute.getName(), entityForeignKeys))
 					{
 						final Operation foreignKeyOperationSpecn = createForeignKeyOperation(umlAttribute);
 						if (foreignKeyOperationSpecn != null)
 						{
 							classFeatures.add(foreignKeyOperationSpecn);
 						}
 					}
 					else
 					{
 						if (XMIConstants.XMI_VERSION_1_2.equals(xmiVersion))
 						{
 							final String tagValue = XMIConstants.PACKAGE_NAME_LOGICAL_VIEW
 									+ XMIConstants.DOT_SEPARATOR
 									+ XMIConstants.PACKAGE_NAME_LOGICAL_MODEL
 									+ XMIConstants.DOT_SEPARATOR
 									+ PackageName.getLogicalPackageName(packageName,
 											XMIConstants.DOT_SEPARATOR)
 									+ XMIConstants.DOT_SEPARATOR + attribute.getEntity().getName()
 									+ XMIConstants.DOT_SEPARATOR + attribute.getName();
 							umlAttribute.getTaggedValue().add(
 									createTaggedValue(XMIConstants.TAGGED_VALUE_MAPPED_ATTRIBUTES,
 											tagValue));
 						}
 						else if (XMIConstants.XMI_VERSION_1_1.equals(xmiVersion))
 						{
 							umlAttribute.getTaggedValue().add(
 									createTaggedValue(XMIConstants.TAGGED_VALUE_MAPPED_ATTRIBUTES,
 											packageName + XMIConstants.DOT_SEPARATOR
 													+ attribute.getEntity().getName()
 													+ XMIConstants.DOT_SEPARATOR
 													+ attribute.getName()));
 						}
 					}
 				}
 			}
 		}
 		return classFeatures;
 	}
 
 	/**
 	 * @param attribute
 	 * @param foreignKeyAttributes
 	 * @return
 	 */
 	private boolean isForeignKey(final String attribute, final List<String> foreignKeyAttributes)
 	{
 		boolean isForiegnKey = false;
 		if ((attribute != null) && (foreignKeyAttributes != null))
 		{
 			final Iterator foreignKeyAttributesIter = foreignKeyAttributes.iterator();
 			while (foreignKeyAttributesIter.hasNext())
 			{
 				final String foreignKey = (String) foreignKeyAttributesIter.next();
 				if (attribute.equals(foreignKey))
 				{
 					isForiegnKey = true;
 					break;
 				}
 			}
 		}
 		return isForiegnKey;
 	}
 
 	/**
 	 * @param attribute
 	 * @return
 	 * @throws DataTypeFactoryInitializationException
 	 */
 	@SuppressWarnings("unchecked")
 	private Attribute createDataAttribute(final AttributeInterface entityAttribute)
 			throws DataTypeFactoryInitializationException
 	{
 		Attribute dataColumn = null;
 		if (entityAttribute != null)
 		{
 			final ColumnPropertiesInterface columnProperties = entityAttribute
 					.getColumnProperties();
 			if (columnProperties != null)
 			{
 				final String columnName = columnProperties.getName();
 				dataColumn = createDataAttribute(columnName, entityAttribute.getDataType());
 				if (entityAttribute.getIsPrimaryKey())
 				{
 					dataColumn.getTaggedValue().add(
 							createTaggedValue(XMIConstants.STEREOTYPE, XMIConstants.PRIMARY_KEY));
 					dataColumn.getStereotype().addAll(
 							getOrCreateStereotypes(XMIConstants.PRIMARY_KEY,
 									XMIConstants.STEREOTYPE_BASECLASS_ATTRIBUTE));
 				}
 			}
 
 		}
 		return dataColumn;
 	}
 
 	/**
 	 * @param columnName
 	 * @param dataType
 	 * @return
 	 * @throws DataTypeFactoryInitializationException
 	 */
 	@SuppressWarnings("unchecked")
 	private Attribute createDataAttribute(final String columnName, final String dataType)
 			throws DataTypeFactoryInitializationException
 	{
 		final Attribute dataColumn = umlPackage.getCore().getAttribute().createAttribute(
 				columnName, VisibilityKindEnum.VK_PUBLIC, false, ScopeKindEnum.SK_INSTANCE, null,
 				ChangeableKindEnum.CK_CHANGEABLE, ScopeKindEnum.SK_CLASSIFIER,
 				OrderingKindEnum.OK_UNORDERED, null);
 		final Classifier typeClass = getOrCreateDataType(DatatypeMappings.get(dataType)
 				.getSQLClassMapping());
 		dataColumn.setType(typeClass);
 		dataColumn.getStereotype().addAll(
 				getOrCreateStereotypes(XMIConstants.COLUMN,
 						XMIConstants.STEREOTYPE_BASECLASS_ATTRIBUTE));
 		dataColumn.getTaggedValue().add(
 				createTaggedValue(XMIConstants.STEREOTYPE, XMIConstants.COLUMN));
 		return dataColumn;
 	}
 
 	/**
 	 * @param entityGroup
 	 * @param xmiVersion
 	 * @return
 	 * @throws DynamicExtensionsApplicationException
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DAOException
 	 */
 	@SuppressWarnings("unchecked")
 	private void generateUMLModel(final EntityGroupInterface entityGroup,
 			final String modelPackageName) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		if (entityGroup != null)
 		{
 			//Create package for entity group
 			final org.omg.uml.modelmanagement.UmlPackage umlGroupPackage = getLeafPackage(
 					entityGroup, modelPackageName);
 
 			//CLASSES : CREATE : create classes for entities
 			Collection<UmlClass> umlEntityClasses;
 			//Relationships  : ASSOCIATIONS/GENERALIZATION/DEPENDENCIES :CREATE
 			Collection<Relationship> umlRelationships;
 			if (XMIConstants.XMI_VERSION_1_2.equals(xmiVersion))
 			{
 				umlEntityClasses = createUMLClasses(getEntities(entityGroup));
 				//CLASSES : ADD : add entity classes to group package
 				umlGroupPackage.getOwnedElement().addAll(umlEntityClasses);
 				umlRelationships = getUMLRelationships(getEntities(entityGroup));
 			}
 			else
 			{
 				umlEntityClasses = createUMLClasses(entityGroup.getEntityCollection());
 				//CLASSES : ADD : add entity classes to group package
 				umlGroupPackage.getOwnedElement().addAll(umlEntityClasses);
 				umlRelationships = getUMLRelationships(entityGroup.getEntityCollection());
 			}
 
 			//Relationships :ADD : Add relationships to package
 			umlGroupPackage.getOwnedElement().addAll(umlRelationships);
 
 			//TAGGED VALUES : Create
 			final Collection<TaggedValue> groupTaggedValues = getTaggedValues(entityGroup);
 			//TAGGED VALUES : Add
 			umlGroupPackage.getTaggedValue().addAll(groupTaggedValues);
 
 		}
 	}
 
 	/**
 	 * @param taggedValueCollection
 	 * @return
 	 * @throws DynamicExtensionsApplicationException
 	 * @throws DynamicExtensionsSystemException
 	 */
 	private Collection<TaggedValue> getTaggedValues(
 			final AbstractMetadataInterface abstractMetadataObj)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		final ArrayList<TaggedValue> taggedValues = new ArrayList<TaggedValue>();
 		taggedValues.add(createTaggedValue(XMIConstants.TAGGED_VALUE_DESCRIPTION,
 				abstractMetadataObj.getDescription()));
 		if (abstractMetadataObj.getId() != null)
 		{
 			taggedValues.add(createTaggedValue(XMIConstants.TAGGED_VALUE_ID, abstractMetadataObj
 					.getId().toString()));
 		}
 		addConceptCodeTaggedValues(abstractMetadataObj, taggedValues);
 		if (abstractMetadataObj instanceof AttributeInterface)
 		{
 			final AttributeInterface attribute = (AttributeInterface) abstractMetadataObj;
 			//Tag values for Validation rules like mandatory, unique, range(min, max)
 			addRuleTagVaues(taggedValues, attribute);
 			if (XMIConstants.XMI_VERSION_1_2.equals(xmiVersion))
 			{
 				EntityManagerInterface entityManager = EntityManager.getInstance();
 				final AttributeTypeInformationInterface attrTypeInfo = attribute
 						.getAttributeTypeInformation();
 				//setting UI properties tag values
 				final ControlInterface control = entityManager
 						.getControlByAbstractAttributeIdentifier(attribute.getId());
 				setUIPropertiesTagValues(taggedValues, attrTypeInfo, control, attribute);
 			}
 		}
 		else if (abstractMetadataObj instanceof AssociationInterface
 				&& XMIConstants.XMI_VERSION_1_2.equals(xmiVersion))
 		{//Association tag values
 			EntityManagerInterface entityManager = EntityManager.getInstance();
 			final ControlInterface control = entityManager
 					.getControlByAbstractAttributeIdentifier(abstractMetadataObj.getId());
 			setAssociationTagValues(control, taggedValues);
 		}
 		final Collection<TaggedValueInterface> taggedValueCollection = abstractMetadataObj
 				.getTaggedValueCollection();
 		if (taggedValueCollection != null)
 		{
 			final Iterator<TaggedValueInterface> taggedValueCollnIter = taggedValueCollection
 					.iterator();
 			while (taggedValueCollnIter.hasNext())
 			{
 				taggedValues.add(createTaggedValue(taggedValueCollnIter.next()));
 			}
 		}
 		return taggedValues;
 	}
 
 	/**
 	 * @param control
 	 * @param taggedValues
 	 */
 	private void setAssociationTagValues(final ControlInterface control,
 			final List<TaggedValue> taggedValues)
 	{
 		if (control != null)
 		{
 			final SelectControl selectControl = (SelectControl) control;
 			if (selectControl.getSeparator() != null)
 			{//Seperator
 				taggedValues.add(createTaggedValue(XMIConstants.TAGGED_VALUE_SEPARATOR,
 						selectControl.getSeparator()));
 			}
 			final Collection<AssociationDisplayAttributeInterface> associationDisplayAttrColl = selectControl
 					.getAssociationDisplayAttributeCollection();
 			if ((associationDisplayAttrColl != null) && !associationDisplayAttrColl.isEmpty())
 			{// Attributes to be displayed in drop down
 				final StringBuffer attributeNames = new StringBuffer();
 				for (final AssociationDisplayAttributeInterface associationDisplayAttribute : associationDisplayAttrColl)
 				{
 					attributeNames.append(XMIConstants.COMMA).append(
 							associationDisplayAttribute.getAttribute().getName());
 				}
 				taggedValues.add(createTaggedValue(
 						XMIConstants.TAGGED_VALUE_ATTRIBUTES_IN_ASSOCIATION_DROP_DOWN,
 						attributeNames.toString()));
 			}
 			if (selectControl instanceof ListBoxInterface)
 			{
 				final ListBoxInterface listBox = (ListBoxInterface) control;
 				if ((listBox.getIsMultiSelect() != null) && (listBox.getNoOfRows() != null)
 						&& listBox.getIsMultiSelect().booleanValue())
 				{//Multiselect
 					taggedValues.add(createTaggedValue(XMIConstants.TAGGED_VALUE_MULTISELECT,
 							listBox.getNoOfRows().toString()));
 				}
 			}
 		}
 	}
 
 	/**
 	 * @param abstractMetadataObj
 	 * @return
 	 */
 	private void addConceptCodeTaggedValues(final AbstractMetadataInterface abstractMetadataObj,
 			final List<TaggedValue> taggedValues)
 	{
 		final Collection<SemanticPropertyInterface> semanticPropertyCollection = abstractMetadataObj
 				.getOrderedSemanticPropertyCollection();
 		if (abstractMetadataObj instanceof EntityInterface)
 		{
 			for (final SemanticPropertyInterface semanticProperty : semanticPropertyCollection)
 			{
 				if (semanticProperty.getSequenceNumber() == 0)
 				{
 					taggedValues.add(createTaggedValue(
 							XMIConstants.TV_OBJECT_CLASS_CONCEPT_CODE, semanticProperty
 									.getConceptCode()));
 					taggedValues.add(createTaggedValue(
 							XMIConstants.TV_OBJECT_CLASS_CONCEPT_DEF,
 							semanticProperty.getConceptDefinition()));
 					taggedValues.add(createTaggedValue(
 							XMIConstants.TAGGED_VALUE_OBJECT_CLASS_CONCEPT_DEFINITION_SOURCE,
 							semanticProperty.getConceptPreferredName()));
 					taggedValues.add(createTaggedValue(
 							XMIConstants.TAGGED_VALUE_OBJECT_CLASS_CONCEPT_PREFERRED_NAME,
 							semanticProperty.getConceptDefinitionSource()));
 				}
 				else
 				{
 					taggedValues.add(createTaggedValue(
 							XMIConstants.TAGGED_VALUE_OBJECT_CLASS_QUALIFIER_CONCEPT_CODE
 									+ semanticProperty.getSequenceNumber(), semanticProperty
 									.getConceptCode()));
 					taggedValues.add(createTaggedValue(
 							XMIConstants.TAGGED_VALUE_OBJECT_CLASS_QUALIFIER_CONCEPT_DEFINITION
 									+ semanticProperty.getSequenceNumber(), semanticProperty
 									.getConceptDefinition()));
 					taggedValues
 							.add(createTaggedValue(
 									XMIConstants.TAGGED_VALUE_OBJECT_CLASS_QUALIFIER_CONCEPT_DEFINITION_SOURCE
 											+ semanticProperty.getSequenceNumber(),
 									semanticProperty.getConceptPreferredName()));
 					taggedValues.add(createTaggedValue(
 							XMIConstants.TAGGED_VALUE_OBJECT_CLASS_QUALIFIER_CONCEPT_PREFERRED_NAME
 									+ semanticProperty.getSequenceNumber(), semanticProperty
 									.getConceptDefinitionSource()));
 				}
 			}
 		}
 		else if (abstractMetadataObj instanceof AttributeInterface)
 		{
 			for (final SemanticPropertyInterface semanticProperty : semanticPropertyCollection)
 			{
 				if (semanticProperty.getSequenceNumber() == 0)
 				{
 					taggedValues.add(createTaggedValue(
 							XMIConstants.TAGGED_VALUE_PROPERTY_CONCEPT_CODE, semanticProperty
 									.getConceptCode()));
 					taggedValues.add(createTaggedValue(
 							XMIConstants.TAGGED_VALUE_PROPERTY_CONCEPT_DEFINITION, semanticProperty
 									.getConceptDefinition()));
 					taggedValues.add(createTaggedValue(
 							XMIConstants.TAGGED_VALUE_PROPERTY_CONCEPT_DEFINITION_SOURCE,
 							semanticProperty.getConceptPreferredName()));
 					taggedValues.add(createTaggedValue(
 							XMIConstants.TAGGED_VALUE_PROPERTY_CONCEPT_PREFERRED_NAME,
 							semanticProperty.getConceptDefinitionSource()));
 				}
 				else
 				{
 					taggedValues.add(createTaggedValue(
 							XMIConstants.TAGGED_VALUE_PROPERTY_QUALIFIER_CONCEPT_CODE
 									+ semanticProperty.getSequenceNumber(), semanticProperty
 									.getConceptCode()));
 					taggedValues.add(createTaggedValue(
 							XMIConstants.TAGGED_VALUE_PROPERTY_QUALIFIER_CONCEPT_DEFINITION
 									+ semanticProperty.getSequenceNumber(), semanticProperty
 									.getConceptDefinition()));
 					taggedValues.add(createTaggedValue(
 							XMIConstants.TAGGED_VALUE_PROPERTY_QUALIFIER_CONCEPT_DEFINITION_SOURCE
 									+ semanticProperty.getSequenceNumber(), semanticProperty
 									.getConceptPreferredName()));
 					taggedValues.add(createTaggedValue(
 							XMIConstants.TAGGED_VALUE_PROPERTY_QUALIFIER_CONCEPT_PREFERRED_NAME
 									+ semanticProperty.getSequenceNumber(), semanticProperty
 									.getConceptDefinitionSource()));
 				}
 			}
 		}
 		//So that user added concept codes are not over written while running the SIW process through caDSR
 		taggedValues.add(createTaggedValue(XMIConstants.TAGGED_VALUE_OWNER_REVIEWED, "1"));
 		taggedValues.add(createTaggedValue(XMIConstants.TAGGED_VALUE_CURATOR_REVIEWED, "1"));
 
 	}
 
 	/**
 	 * @param taggedValues
 	 * @param attrTypeInfo
 	 */
 	private void setUIPropertiesTagValues(final List<TaggedValue> taggedValues,
 			final AttributeTypeInformationInterface attributeTypeInformation,
 			final ControlInterface control, final AttributeInterface attribute)
 	{
 		if (attributeTypeInformation instanceof DateAttributeTypeInformation)
 		{
 			taggedValues.add(createTaggedValue(XMIConstants.TAGGED_VALUE_DATE_FORMAT,
 					((DateAttributeTypeInformation) attributeTypeInformation).getFormat()));
 		}
 		else if (attributeTypeInformation instanceof BooleanAttributeTypeInformation)
 		{ // NOPMD by suhas_khot on 12/20/10 2:06 PM
 			// TODO Auto-generated if
 		}
 		else if (attributeTypeInformation instanceof FileAttributeTypeInformation)
 		{ // NOPMD by suhas_khot on 12/20/10 2:06 PM
 			// TODO auto generated if
 		}
 		else
 		{// String attribute type information
 			if (attributeTypeInformation instanceof StringAttributeTypeInformation)
 			{//String attribute
 				final StringAttributeTypeInformation stringAttributeTypeInformation = (StringAttributeTypeInformation) attributeTypeInformation;
 				final Integer maxLength = stringAttributeTypeInformation.getSize();
 				if (maxLength != null)
 				{
 					taggedValues.add(createTaggedValue(XMIConstants.TAGGED_VALUE_MAX_LENGTH,
 							maxLength.toString()));
 				}
 				if (control != null)
 				{
 					if (control instanceof TextFieldInterface)
 					{
 						final TextFieldInterface textField = (TextFieldInterface) control;
 						addTextFieldTagValues(textField, taggedValues);
 					}
 					else if (control instanceof TextAreaInterface)
 					{
 						final TextAreaInterface textArea = (TextAreaInterface) control;
 						addTextAreaTagValues(textArea, taggedValues);
 					}
 				}
 			}
 			else
 			{//Number attribute
 				final Integer precision = ((NumericAttributeTypeInformation) attributeTypeInformation)
 						.getDecimalPlaces();
 				if (precision != null)
 				{
 					taggedValues.add(createTaggedValue(XMIConstants.TAGGED_VALUE_PRECISION,
 							precision.toString()));
 				}
 			}
 		}
 		if ((attribute.getIsIdentified() != null) && attribute.getIsIdentified().booleanValue())
 		{// PHI attribute
 			taggedValues.add(createTaggedValue(XMIConstants.TAGGED_VALUE_PHI_ATTRIBUTE, attribute
 					.getIsIdentified().toString()));
 		}
 		if ((attributeTypeInformation.getDefaultValue() != null)
 				&& (attributeTypeInformation.getDefaultValue().getValueAsObject() != null))
 		{//Default value
 			taggedValues.add(createTaggedValue(XMIConstants.TAGGED_VALUE_DEFAULT_VALUE,
 					attributeTypeInformation.getDefaultValue().getValueAsObject().toString()));
 		}
 	}
 
 	/**
 	 * @param textField
 	 * @param taggedValues
 	 */
 	private void addTextFieldTagValues(final TextFieldInterface textField,
 			final List<TaggedValue> taggedValues)
 	{
 		final Integer width = textField.getColumns();
 		if (width != null)
 		{
 			taggedValues.add(createTaggedValue(XMIConstants.TAGGED_VALUE_DISPLAY_WIDTH, width
 					.toString()));
 		}
 		final Boolean isPassword = textField.getIsPassword();
 		if ((isPassword != null) && isPassword.booleanValue())
 		{
 			taggedValues.add(createTaggedValue(XMIConstants.TAGGED_VALUE_PASSWORD, isPassword
 					.toString()));
 		}
 		final Boolean isUrl = textField.getIsUrl();
 		if ((isUrl != null) && isUrl.booleanValue())
 		{
 			taggedValues.add(createTaggedValue(XMIConstants.TAGGED_VALUE_URL, isUrl.toString()));
 		}
 	}
 
 	/**
 	 * @param textArea
 	 * @param taggedValues
 	 */
 	private void addTextAreaTagValues(final TextAreaInterface textArea,
 			final List<TaggedValue> taggedValues)
 	{
 		final Integer noOfRows = textArea.getRows();
 		if ((noOfRows != null) && (noOfRows.intValue() > 0))
 		{
 			taggedValues.add(createTaggedValue(XMIConstants.TAGGED_VALUE_MULTILINE, noOfRows
 					.toString()));
 		}
 	}
 
 	/**
 	 * @param taggedValues
 	 * @param attributeInterface
 	 */
 	private void addRuleTagVaues(final List<TaggedValue> taggedValues,
 			final AttributeInterface attributeInterface)
 	{
 		final Collection<RuleInterface> ruleColl = attributeInterface.getRuleCollection();
 		for (final RuleInterface rule : ruleColl)
 		{
 			final String ruleTag = XMIConstants.TAGGED_VALUE_RULE + XMIConstants.SEPARATOR
 					+ rule.getName();
 			String ruleValue = "";
 
 			final Collection<RuleParameterInterface> ruleParamColl = rule
 					.getRuleParameterCollection();
 			if ((ruleParamColl == null) || ruleParamColl.isEmpty())
 			{
 				taggedValues.add(createTaggedValue(ruleTag, ruleValue));
 			}
 			else
 			{
 				for (final RuleParameterInterface ruleParam : ruleParamColl)
 				{
 					final StringBuffer temp = new StringBuffer(ruleTag);
 					temp.append(XMIConstants.SEPARATOR).append(ruleParam.getName());
 					ruleValue = ruleParam.getValue();
 					taggedValues.add(createTaggedValue(temp.toString(), ruleValue));
 				}
 			}
 		}
 	}
 
 	/**
 	 * @param interface1
 	 * @return
 	 */
 	private TaggedValue createTaggedValue(final TaggedValueInterface taggedValueIntf)
 	{
 		TaggedValue tagValue = null;
 		if (taggedValueIntf != null)
 		{
 			tagValue = createTaggedValue(taggedValueIntf.getKey(), taggedValueIntf.getValue());
 		}
 		return tagValue;
 	}
 
 	/**
 	 * @param entityCollection
 	 * @return
 	 */
 	private Collection<Relationship> getUMLRelationships(
 			final Collection<EntityInterface> entityCollection)
 	{
 		final ArrayList<Relationship> umlRelationships = new ArrayList<Relationship>();
 		if (entityCollection != null)
 		{
 			final Iterator<EntityInterface> entityCollnIter = entityCollection.iterator();
 			while (entityCollnIter.hasNext())
 			{
 				final EntityInterface entity = entityCollnIter.next();
 				final Collection<Relationship> entityAssociations = createUMLRelationships(entity);
 				umlRelationships.addAll(entityAssociations);
 			}
 		}
 		return umlRelationships;
 	}
 
 	/**
 	 * @param entity
 	 * @param xmiVersion
 	 * @return
 	 */
 	private Collection<Relationship> createUMLRelationships(final EntityInterface entity)
 	{
 		final ArrayList<Relationship> entityUMLRelationships = new ArrayList<Relationship>();
 		if (entity != null)
 		{
 			//Association relationships
 			Collection<AssociationInterface> entityAssociations;
 			if (XMIConstants.XMI_VERSION_1_2.equals(xmiVersion))
 			{
 				entityAssociations = entity.getAssociationCollectionExcludingCollectionAttributes();
 			}
 			else
 			{
 				entityAssociations = entity.getAssociationCollection();
 			}
 			if (entityAssociations != null)
 			{
 				final Iterator<AssociationInterface> entityAssociationsIter = entityAssociations
 						.iterator();
 				while (entityAssociationsIter.hasNext())
 				{
 					final AssociationInterface association = entityAssociationsIter.next();
 					final UmlAssociation umlAssociation = createUMLAssociation(association);
 					if (umlAssociation != null)
 					{
 						entityUMLRelationships.add(umlAssociation);
 					}
 				}
 			}
 			//generalizations
 			final EntityInterface parentEntity = entity.getParentEntity();
 			if (parentEntity != null)
 			{
 				final UmlClass parentClass = entityUMLClassMappings.get(parentEntity.getName());
 				final UmlClass childClass = entityUMLClassMappings.get(entity.getName());
 				final Generalization generalization = createGeneralization(parentClass, childClass);
 				entityUMLRelationships.add(generalization);
 			}
 		}
 		return entityUMLRelationships;
 	}
 
 	/**
 	 * @param parentEntity
 	 * @param entity
 	 */
 	private Generalization createGeneralization(final UmlClass parentClass,
 			final UmlClass childClass)
 	{
 		final Generalization generalization = umlPackage.getCore().getGeneralization()
 				.createGeneralization(null, VisibilityKindEnum.VK_PUBLIC, false, null);
 		final org.omg.uml.foundation.core.GeneralizableElement parent = parentClass;
 		final org.omg.uml.foundation.core.GeneralizableElement child = childClass;
 		generalization.setParent(parent);
 		generalization.setChild(child);
 		return generalization;
 	}
 
 	/**
 	 * @param association
 	 * @return
 	 */
 	@SuppressWarnings("unchecked")
 	private UmlAssociation createUMLAssociation(final AssociationInterface association)
 	{
 		UmlAssociation umlAssociation = null;
 		final CorePackage corePackage = umlPackage.getCore();
 		if (association != null)
 		{
 			umlAssociation = corePackage.getUmlAssociation()
 					.createUmlAssociation(association.getName(), VisibilityKindEnum.VK_PUBLIC,
 							false, false, false, false);
 
 			if (umlAssociation != null)
 			{
 				//Set the ends
 				AssociationEnd sourceEnd = null;
 				final EntityInterface sourceEntity = association.getEntity();
 				if (sourceEntity != null)
 				{
 					final Classifier sourceClass = getUMLClassForEntity(sourceEntity.getName());
 					sourceEnd = getAssociationEnd(association.getSourceRole(), sourceClass,
 							XMIConstants.ASSN_SRC_ENTITY);
 				}
 
 				final EntityInterface targetEntity = association.getTargetEntity();
 				if (targetEntity != null)
 				{
 					final Classifier targetClass = getUMLClassForEntity(targetEntity.getName());
 					final AssociationEnd targetEnd = getAssociationEnd(association.getTargetRole(),
 							targetClass, XMIConstants.ASSN_TGT_ENTITY);
 					umlAssociation.getConnection().add(targetEnd);
 				}
 				umlAssociation.getConnection().add(sourceEnd);
 				//set the direction
 				final TaggedValue directionTaggedValue = getDirectionTaggedValue(association);
 				if (directionTaggedValue != null)
 				{
 					umlAssociation.getTaggedValue().add(directionTaggedValue);
 				}
 				//If association is many-to-many add "correlation-table" tagged value
 				if (XMIConstants.ASSOC_MANY_MANY.equals(getAssociationType(association))
 						&& (association.getConstraintProperties() != null))
 				{
 					final String corelnTableName = association.getConstraintProperties().getName();
 					umlAssociation.getTaggedValue().add(
 							createTaggedValue(XMIConstants.TAGGED_VALUE_CORELATION_TABLE,
 									corelnTableName));
 				}
 
 			}
 		}
 		return umlAssociation;
 	}
 
 	/***
 	 * Creates a tagged value given the specified <code>name</code>.
 	 *
 	 * @param name the name of the tagged value to create.
 	 * @param value the value to populate on the tagged value.
 	 * @return returns the new TaggedValue
 	 */
 	@SuppressWarnings("unchecked")
 	protected TaggedValue createTaggedValue(final String name, final String value)
 	{
 		TaggedValue taggedValue = null;
 		if (name != null)
 		{
 			final Collection values = new HashSet();
 			if (value != null)
 			{
 				values.add(value);
 			}
 
 			taggedValue = umlPackage.getCore().getTaggedValue().createTaggedValue(name,
 					VisibilityKindEnum.VK_PUBLIC, false, values);
 			if (XMIConstants.XMI_VERSION_1_2.equals(xmiVersion))
 			{
 				taggedValue.setType(umlPackage.getCore().getTagDefinition().createTagDefinition(
 						name, VisibilityKindEnum.VK_PUBLIC, false, null, null));
 			}
 		}
 		return taggedValue;
 	}
 
 	/**
 	 * @param name
 	 * @return
 	 */
 	private Classifier getUMLClassForEntity(final String entityName)
 	{
 		Classifier umlClass = null;
 		if ((entityUMLClassMappings != null) && (entityName != null))
 		{
 			umlClass = entityUMLClassMappings.get(entityName);
 		}
 		return umlClass;
 	}
 
 	/**
 	 * @param entityName
 	 * @return
 	 */
 	private Classifier getSQLClassForEntity(final String entityName)
 	{
 		Classifier sqlClass = null;
 		if ((entityDataClassMappings != null) && (entityName != null))
 		{
 			sqlClass = entityDataClassMappings.get(entityName);
 		}
 		return sqlClass;
 	}
 
 	/**
 	 * @param role
 	 * @return
 	 */
 	private AssociationEnd getAssociationEnd(final RoleInterface role, final Classifier assocClass,
 			final String assnEntityType)
 	{
 		final int minCardinality = role.getMinimumCardinality().ordinal();
 		final int maxCardinality = role.getMaximumCardinality().ordinal();
 		// primary end association
 		final AssociationEnd associationEnd = umlPackage.getCore().getAssociationEnd()
 				.createAssociationEnd(
 						role.getName(),
 						VisibilityKindEnum.VK_PUBLIC,
 						false,
 						true,
 						OrderingKindEnum.OK_ORDERED,
 						AggregationKindEnum.AK_NONE,
 						ScopeKindEnum.SK_INSTANCE,
 						createMultiplicity(umlPackage.getCore().getDataTypes(), minCardinality,
 								maxCardinality), ChangeableKindEnum.CK_CHANGEABLE);
 		associationEnd.setParticipant(assocClass);
 		if (XMIConstants.XMI_VERSION_1_2.equals(xmiVersion))
 		{
 			associationEnd.getTaggedValue().add(
 					createTaggedValue(XMIConstants.TAGGED_VALUE_ASSN_ENTITY, assnEntityType));
 
 			//if role.associationtype is of containment type then set TAGGED_VALUE_CONTAINMENT to value 'containment'
 			//else set TAGGED_VALUE_CONTAINMENT_UNSPECIFIED to value 'unspecified'
 
 			if (role.getAssociationsType().getValue().equals(
 					AssociationType.CONTAINTMENT.getValue()))
 			{
 
 				associationEnd.getTaggedValue().add(
 						createTaggedValue(XMIConstants.TAGGED_VALUE_CONTAINMENT,
 								XMIConstants.TAGGED_VALUE_CONTAINMENT));
 
 			}
 			else
 			{
 				associationEnd.getTaggedValue().add(
 						createTaggedValue(XMIConstants.TAGGED_VALUE_CONTAINMENT_UNSPECIFIED,
 								XMIConstants.TAGGED_VALUE_CONTAINMENT_UNSPECIFIED));
 			}
 		}
 		//associationEnd.setNavigable(true);
 		return associationEnd;
 	}
 
 	/**
 	 * @param entityCollection
 	 * @return
 	 * @throws DynamicExtensionsApplicationException
 	 * @throws DynamicExtensionsSystemException
 	 */
 	private Collection<UmlClass> createUMLClasses(final Collection<EntityInterface> entityCollection)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		final ArrayList<UmlClass> umlEntityClasses = new ArrayList<UmlClass>();
 		if (entityCollection != null)
 		{
 			final Iterator entityCollectionIter = entityCollection.iterator();
 			while (entityCollectionIter.hasNext())
 			{
 				final EntityInterface entity = (EntityInterface) entityCollectionIter.next();
 				final UmlClass umlEntityClass = createUMLClass(entity);
 				if (umlEntityClass != null)
 				{
 					umlEntityClasses.add(umlEntityClass);
 					entityUMLClassMappings.put(entity.getName(), umlEntityClass);
 				}
 			}
 		}
 		return umlEntityClasses;
 	}
 
 	/**
 	 * @param entity
 	 * @param xmiversion
 	 * @return
 	 * @throws DynamicExtensionsApplicationException
 	 * @throws DynamicExtensionsSystemException
 	 */
 	@SuppressWarnings("unchecked")
 	private UmlClass createUMLClass(final EntityInterface entity)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		UmlClass umlEntityClass = null;
 		if (entity != null)
 		{
 			//Get class name , create uml class
 			final String className = XMIUtilities.getClassNameForEntity(entity);
 			final CorePackage corePackage = umlPackage.getCore();
 			umlEntityClass = corePackage.getUmlClass().createUmlClass(className,
 					VisibilityKindEnum.VK_PUBLIC, false, false, false, entity.isAbstract(), false);
 			//Create and add attributes to class
 			Collection<Attribute> umlEntityAttributes;
 			if (XMIConstants.XMI_VERSION_1_2.equals(xmiVersion))
 			{
 				umlEntityAttributes = createUMLAttributes(getAllAttributesForEntity(entity));
 			}
 			else
 			{
 				umlEntityAttributes = createUMLAttributes(entity.getEntityAttributes());
 			}
 			umlEntityClass.getFeature().addAll(umlEntityAttributes);
 			//Create and add tagged values to entity
 			final Collection<TaggedValue> entityTaggedValues = getTaggedValues(entity);
 			umlEntityClass.getTaggedValue().addAll(entityTaggedValues);
 			umlEntityClass.getTaggedValue().add(
 					createTaggedValue(XMIConstants.TAGGED_VALUE_DOCUMENTATION, entity.getName()));
 			umlEntityClass.getTaggedValue().add(
 					createTaggedValue(XMIConstants.TAGGED_VALUE_DESCRIPTION, entity.getName()));
 		}
 		return umlEntityClass;
 	}
 
 	/**
 	 * Get collection of all attributes for a Entity
 	 * @param entity
 	 * @return
 	 * @throws DynamicExtensionsApplicationException
 	 * @throws DynamicExtensionsSystemException
 	 */
 	private Collection<AttributeInterface> getAllAttributesForEntity(final EntityInterface entity)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		//Collection of all attributes
 		final Collection<AttributeInterface> attributesCollection = new ArrayList<AttributeInterface>();
 		final Map<Long, AttributeInterface> attributes = new HashMap<Long, AttributeInterface>();
 		for (final AttributeInterface attribute : entity.getEntityAttributes())
 		{
 			attributes.put(attribute.getId(), attribute);
 		}
 		//create and add collection-type-attribute to the entity
 		final Collection<AssociationInterface> associationCollection = new ArrayList<AssociationInterface>();
 		associationCollection.addAll(entity.getAssociationCollection());
 		final DomainObjectFactory domainObjectFactory = DomainObjectFactory.getInstance();
 		final TaggedValueInterface tagValueForCollectionTypeAttr = domainObjectFactory
 				.createTaggedValue();
 		//		tagValueForCollectionTypeAttribute.setKey("CollectionTypeAttribute");
 		//		tagValueForCollectionTypeAttribute.setValue("true");
 
 		for (final AssociationInterface association : associationCollection)
 		{
 			if (association.getIsCollection())
 			{
 				final Collection<AbstractAttributeInterface> attributeCollection = association
 						.getTargetEntity().getAllAbstractAttributes();
 				final Collection<AbstractAttributeInterface> filteredAttributeCollection = EntityManagerUtil
 						.filterSystemAttributes(attributeCollection);
 				final List<AbstractAttributeInterface> attributesList = new ArrayList<AbstractAttributeInterface>(
 						filteredAttributeCollection);
 				AttributeInterface collectionTypeAttribute = (AttributeInterface) attributesList
 						.get(0);
 				collectionTypeAttribute.setName(association.getName());
 				entity.addAttribute(collectionTypeAttribute);
 				attributes.put(collectionTypeAttribute.getId(), collectionTypeAttribute);
 				//				collectionTypeAttribute.addTaggedValue(tagValueForCollectionTypeAttribute);
 				final EntityManagerInterface entityManager = EntityManager.getInstance();
 				final ControlInterface control = entityManager
 						.getControlByAbstractAttributeIdentifier(association.getId());
 				if ((control != null) && (control instanceof ListBoxInterface))
 				{
 					final ListBoxInterface listBox = (ListBoxInterface) control;
 					if ((listBox.getIsMultiSelect() != null) && (listBox.getNoOfRows() != null)
 							&& listBox.getIsMultiSelect().booleanValue())
 					{
 						tagValueForCollectionTypeAttr.setKey(XMIConstants.TAGGED_VALUE_MULTISELECT);
 						tagValueForCollectionTypeAttr.setValue(listBox.getNoOfRows().toString());
 						collectionTypeAttribute.addTaggedValue(tagValueForCollectionTypeAttr);
 						final TaggedValueInterface tagValueColnTypeAttrName = domainObjectFactory
 								.createTaggedValue();
 						tagValueColnTypeAttrName
 								.setKey(XMIConstants.TAGGED_VALUE_MULTISELECT_TABLE_NAME);
 						final String tableName = association.getTargetEntity().getTableProperties()
 								.getName();
 						tagValueColnTypeAttrName.setValue(tableName);
 						collectionTypeAttribute.addTaggedValue(tagValueColnTypeAttrName);
 					}
 				}
 			}
 		}
 		attributesCollection.addAll(attributes.values());
 		return attributesCollection;
 	}
 
 	/**
 	 * @param entity
 	 * @return
 	 * @throws DynamicExtensionsApplicationException
 	 * @throws DynamicExtensionsSystemException
 	 */
 	private Collection<Attribute> createUMLAttributes(
 			final Collection<AttributeInterface> entityAttributes)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		final ArrayList<Attribute> umlAttributes = new ArrayList<Attribute>();
 		if (entityAttributes != null)
 		{
 			final Iterator entityAttributesIter = entityAttributes.iterator();
 			while (entityAttributesIter.hasNext())
 			{
 				final AttributeInterface attribute = (AttributeInterface) entityAttributesIter
 						.next();
 				if (attribute.getDataType() != EntityManagerConstantsInterface.FILE_ATTRIBUTE_TYPE
 						|| XMIConstants.XMI_VERSION_1_2.equals(xmiVersion))
 				{
 					final Attribute umlAttribute = createUMLAttribute(attribute);
 					umlAttributes.add(umlAttribute);
 				}
 
 			}
 		}
 		return umlAttributes;
 	}
 
 	/**
 	 * @param entityAttribute
 	 * @return
 	 * @throws DynamicExtensionsApplicationException
 	 * @throws DynamicExtensionsSystemException
 	 */
 	@SuppressWarnings("unchecked")
 	private Attribute createUMLAttribute(final AttributeInterface entityAttribute)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		Attribute umlAttribute = null;
 		if (entityAttribute != null)
 		{
 			final String attributeName = XMIUtilities.getAttributeName(entityAttribute);
 			final CorePackage corePackage = umlPackage.getCore();
 			umlAttribute = corePackage.getAttribute().createAttribute(
 					attributeName,
 					VisibilityKindEnum.VK_PUBLIC,
 					false,
 					ScopeKindEnum.SK_INSTANCE,
 					createAttributeMultiplicity(corePackage.getDataTypes(), entityAttribute
 							.getIsNullable()), ChangeableKindEnum.CK_CHANGEABLE,
 					ScopeKindEnum.SK_CLASSIFIER, OrderingKindEnum.OK_UNORDERED, null);
 			Classifier typeClass = getOrCreateDataType(DatatypeMappings.get(
 					entityAttribute.getDataType()).getJavaClassMapping());
 			umlAttribute.setType(typeClass);
 			//Tagged Values
 			final Collection<TaggedValue> attributeTaggedValues = getTaggedValues(entityAttribute);
 			umlAttribute.getTaggedValue().addAll(attributeTaggedValues);
 			umlAttribute.getTaggedValue()
 					.add(
 							createTaggedValue(XMIConstants.TAGGED_VALUE_TYPE, entityAttribute
 									.getDataType()));
 			umlAttribute.getTaggedValue().add(
 					createTaggedValue(XMIConstants.TAGGED_VALUE_DESCRIPTION, entityAttribute
 							.getName()));
 
 			if (entityAttribute.getIsPrimaryKey())
 			{
 				//System.out.println("Found primary key " + entityAttribute.getName());
 				umlAttribute.getTaggedValue().add(
 						createTaggedValue(XMIConstants.STEREOTYPE, XMIConstants.PRIMARY_KEY));
 				umlAttribute.getStereotype().addAll(
 						getOrCreateStereotypes(XMIConstants.PRIMARY_KEY,
 								XMIConstants.STEREOTYPE_BASECLASS_ATTRIBUTE));
 
 			}
 		}
 		return umlAttribute;
 	}
 
 	/**
 	 * @param corePackage
 	 * @param string
 	 * @return
 	 */
 
 	@SuppressWarnings("unchecked")
 	private Classifier getOrCreateDataType(final String type)
 	{
 
 		Object datatype = XMIUtilities.find(umlModel, type);
 		if (datatype == null)
 		{
 			final String[] names = StringUtils.split(type, '.');
 			if ((names != null) && (names.length > 0))
 			{
 				// the last name is the type name
 				final String typeName = names[names.length - 1];
 				names[names.length - 1] = null;
 				final String packageName = StringUtils.join(names, XMIConstants.DOT_SEPARATOR);
 				final org.omg.uml.modelmanagement.UmlPackage datatypesPackage = getOrCreatePackage(
 						packageName, logicalModel);
 				//Create Datatype
 				if (datatypesPackage == null)
 				{
 					datatype = XMIUtilities.find(umlModel, typeName);
 					//Create Datatype Class
 					if (datatype == null)
 					{
 						datatype = umlPackage.getCore().getDataType().createDataType(typeName,
 								VisibilityKindEnum.VK_PUBLIC, false, false, false, false);
 						umlModel.getOwnedElement().add(datatype);
 					}
 				}
 				else
 				{
 					datatype = XMIUtilities.find(datatypesPackage, typeName);
 					//Create Datatype Class
 					if (datatype == null)
 					{
 						datatype = umlPackage.getCore().getUmlClass().createUmlClass(typeName,
 								VisibilityKindEnum.VK_PUBLIC, false, false, false, false, false);
 						datatypesPackage.getOwnedElement().add(datatype);
 					}
 				}
 			}
 		}
 		return (Classifier) datatype;
 	}
 
 	/**
 	 * @param dataTypes
 	 * @param b
 	 * @return
 	 */
 	private static Multiplicity createAttributeMultiplicity(final DataTypesPackage dataTypes,
 			final boolean required)
 	{
 		{
 			Multiplicity mult;
 			if (required)
 			{
 				mult = createMultiplicity(dataTypes, 1, 1);
 			}
 			else
 			{
 				mult = createMultiplicity(dataTypes, 0, 1);
 			}
 			return mult;
 		}
 
 	}
 
 	/**
 	 * @param dataTypes
 	 * @param i
 	 * @param j
 	 * @return
 	 */
 	@SuppressWarnings("unchecked")
 	protected static Multiplicity createMultiplicity(final DataTypesPackage dataTypes,
 			final int lower, final int upper)
 	{
 		final Multiplicity mult = dataTypes.getMultiplicity().createMultiplicity();
 		final MultiplicityRange range = dataTypes.getMultiplicityRange().createMultiplicityRange(
 				lower, upper);
 		mult.getRange().add(range);
 		return mult;
 	}
 
 	@SuppressWarnings("unchecked")
 	protected Collection getOrCreateStereotypes(final String names, final String baseClass)
 	{
 		final Collection<Stereotype> stereotypes = new HashSet<Stereotype>();
 		String[] stereotypeNames = null;
 		if (names != null)
 		{
 			stereotypeNames = names.split(",");
 		}
 		if ((stereotypeNames != null) && (stereotypeNames.length > 0))
 		{
 			for (final String stereotypeName : stereotypeNames)
 			{
 				final String name = StringUtils.trimToEmpty(stereotypeName);
 
 				final Collection<String> baseClasses = new ArrayList<String>();
 				baseClasses.add(baseClass);
 				Stereotype stereotype = umlPackage.getCore().getStereotype()
 						.createStereotype(name, VisibilityKindEnum.VK_PUBLIC, false, false, false,
 								false, null, baseClasses);
 
 				umlModel.getOwnedElement().add(stereotype);
 
 				stereotypes.add(stereotype);
 			}
 		}
 		return stereotypes;
 	}
 
 	/**
 	 *
 	 */
 	private org.omg.uml.modelmanagement.UmlPackage getLeafPackage(
 			final EntityGroupInterface entityGroup, final String modelPackageName)
 	{
 		final Collection<TaggedValueInterface> tvColl = entityGroup.getTaggedValueCollection();
 		for (final TaggedValueInterface tv : tvColl)
 		{
 			if (tv.getKey().equalsIgnoreCase(XMIConstants.TAGGED_NAME_PACKAGE_NAME))
 			{
 				String completePackageName = tv.getValue();
 				packageName = completePackageName;
 			}
 		}
 		if ((packageName == null) && (modelPackageName != null))
 		{
 			packageName = modelPackageName;
 		}
 
 		final org.omg.uml.modelmanagement.UmlPackage leafPackage = getOrCreatePackage(packageName,
 				logicalModel);
 		return leafPackage;
 	}
 
 	/**
 	 * @param modelManagement
 	 * @param umlModel
 	 * @param string
 	 * @return
 	 */
 	private org.omg.uml.modelmanagement.UmlPackage getOrCreatePackage(String packageName,
 			org.omg.uml.modelmanagement.UmlPackage parentPackage)
 	{
 		final ModelManagementPackage modelManagement = umlPackage.getModelManagement();
 
 		Object newPackage = null;
 		String logicalPackageName = StringUtils.trimToEmpty(PackageName.getLogicalPackageName(
 				packageName, XMIConstants.DOT_SEPARATOR));
 		if (StringUtils.isNotEmpty(logicalPackageName))
 		{
 			final StringTokenizer stringTokenizer = new StringTokenizer(logicalPackageName,
 					XMIConstants.DOT_SEPARATOR);
 			if (stringTokenizer != null)
 			{
 				org.omg.uml.modelmanagement.UmlPackage parentPckg = parentPackage;
 				while (stringTokenizer.hasMoreTokens())
 				{
 					final String token = stringTokenizer.nextToken();
 					newPackage = XMIUtilities.find(parentPckg, token);
 
 					if (newPackage == null)
 					{
 						newPackage = modelManagement.getUmlPackage().createUmlPackage(token,
 								VisibilityKindEnum.VK_PUBLIC, false, false, false, false);
 						parentPckg.getOwnedElement().add(newPackage);
 					}
 					parentPckg = (org.omg.uml.modelmanagement.UmlPackage) newPackage;
 				}
 			}
 		}
 		return (org.omg.uml.modelmanagement.UmlPackage) newPackage;
 	}
 
 	/**
 	 * @throws CreationFailedException
 	 * @throws DynamicExtensionsSystemException
 	 * @throws Exception
 	 */
 	public void init() throws CreationFailedException, DynamicExtensionsSystemException
 	{
 		//		Cleanup repository files
 		XMIUtilities.cleanUpRepository(filename);
 		initializeUMLPackage();
 		initializeModel();
 		initializePackageHierarchy();
 		entityUMLClassMappings = new HashMap<String, UmlClass>();
 		entityDataClassMappings = new HashMap<String, UmlClass>();
 		foreignKeyOperationNameMapping = new HashMap<String, String>();
 	}
 
 	/**
 	 *
 	 */
 	@SuppressWarnings("unchecked")
 	private void initializePackageHierarchy()
 	{
 		final org.omg.uml.modelmanagement.UmlPackage logicalView = getOrCreatePackage(
 				XMIConstants.PACKAGE_NAME_LOGICAL_VIEW, umlModel);
 		logicalModel = getOrCreatePackage(XMIConstants.PACKAGE_NAME_LOGICAL_MODEL, logicalView);
 		if (XMIConstants.XMI_VERSION_1_1.equalsIgnoreCase(xmiVersion))
 		{
 			dataModel = getOrCreatePackage(XMIConstants.PACKAGE_NAME_DATA_MODEL, logicalView);
 		}
 	}
 
 	/**
 	 *
 	 */
 	private void initializeModel()
 	{
 		//Initialize model
 		final ModelManagementPackage modelManagementPackage = umlPackage.getModelManagement();
 
 		//Create Logical Model
 		umlModel = modelManagementPackage.getModel().createModel(XMIConstants.MODEL_NAME,
 				VisibilityKindEnum.VK_PUBLIC, false, false, false, false);
 	}
 
 	/**
 	 * @throws CreationFailedException
 	 * @throws Exception
 	 * @throws CreationFailedException
 	 *
 	 */
 	private void initializeUMLPackage() throws DynamicExtensionsSystemException,
 			CreationFailedException
 	{
 		//Get UML Package
 		umlPackage = (UmlPackage) repository.getExtent(XMIConstants.UML_INSTANCE);
 		if (umlPackage == null)
 		{
 			// UML extent does not exist -> create it (note that in case one want's to instantiate
 			// a metamodel other than MOF, they need to provide the second parameter of the createExtent
 			// method which indicates the metamodel package that should be instantiated)
 			final XmiReader reader = (XmiReader) Lookup.getDefault().lookup(XmiReader.class);
 			umlPackage = (UmlPackage) repository.createExtent(XMIConstants.UML_INSTANCE,
 					getUmlPackage(repository, reader));
 		}
 	}
 
 	/** Finds "UML" package in a given extent
 	 * @param umlMM MOF extent that should be searched for "UML" package.
 	 */
 	/*private static MofPackage getUmlPackage(ModelPackage umlMM)
 	{
 		// iterate through all instances of package
 		for (Iterator it = umlMM.getMofPackage().refAllOfClass().iterator(); it.hasNext();)
 		{
 			MofPackage pkg = (MofPackage) it.next();
 			// is the package topmost and is it named "UML"?
 			if (pkg.getContainer() == null && "UML".equals(pkg.getName()))
 			{
 				// yes -> return it
 				return pkg;
 			}
 		}
 		// a topmost package named "UML" could not be found
 		return null;
 	}*/
 
 	/**
 	 * @param core
 	 * @param umlPrimaryClass
 	 * @param umlDependentClass
 	 * @return
 	 */
 	@SuppressWarnings("unchecked")
 	private Dependency createDependency(final UmlClass umlPrimaryClass,
 			final UmlClass umlDependentClass)
 	{
 		final CorePackage corePackage = umlPackage.getCore();
 		final Dependency dependency = corePackage.getDependency().createDependency(null,
 				VisibilityKindEnum.VK_PUBLIC, false);
 		corePackage.getAClientClientDependency().add(umlDependentClass, dependency);
 		corePackage.getASupplierSupplierDependency().add(umlPrimaryClass, dependency);
 		dependency.getStereotype().addAll(
 				getOrCreateStereotypes(XMIConstants.TAGGED_VALUE_DATASOURCE,
 						XMIConstants.TAGGED_VALUE_DEPENDENCY));
 		dependency.getTaggedValue().add(
 				createTaggedValue(XMIConstants.STEREOTYPE, XMIConstants.TAGGED_VALUE_DATASOURCE));
 		return dependency;
 	}
 
 	/**
 	 *
 	 */
 	/*private static void generateXMIForIntegrationTables()
 	{
 		EntityGroupInterface entityGroup = null;
 		EntityGroupManagerInterface entityGroupManager = EntityGroupManager.getInstance();
 		XMIExporter exporter = new XMIExporter();
 		try
 		{
 			entityGroup = entityGroupManager.getEntityGroupByName("newsurgery");
 			if (entityGroup == null)
 			{
 				throw new DynamicExtensionsSystemException(
 						"Entity group newsurgery not found ,test case to import xmi is failed");
 			}
 			exporter.exportXMI("deintegration.xmi", entityGroup, XMIConstants.XMI_VERSION_1_1);
 		}
 		catch (Exception e)
 		{
 			Logger.out.debug(e.getMessage());
 		}
 	}*/
 
 	/**
 	 * @param associationType
 	 * @param name
 	 * @param minCard
 	 * @param maxCard
 	 * @return
 	 */
 	private static RoleInterface getRole(final AssociationType associationType, final String name,
 			final Cardinality minCard, final Cardinality maxCard)
 	{
 		final RoleInterface role = DomainObjectFactory.getInstance().createRole();
 		role.setAssociationsType(associationType);
 		role.setName(name);
 		role.setMinimumCardinality(minCard);
 		role.setMaximumCardinality(maxCard);
 		return role;
 	}
 
 	/**
 	 * @param attribute
 	 * @param umlAttribute
 	 * @return
 	 * @throws DataTypeFactoryInitializationException
 	 */
 	public Operation createPrimaryKeyOperation(final AttributeInterface attribute,
 			final Attribute umlAttribute) throws DataTypeFactoryInitializationException
 	{
 		//Primary key operation name is generated considering the case that only one primary key will be present.
 		final String primaryKeyOperationName = getPrimaryKeyOperationName(attribute.getEntity()
 				.getName(), attribute.getName());
 		return createPrimaryKeyOperation(primaryKeyOperationName, attribute.getDataType(),
 				umlAttribute);
 	}
 
 	/**
 	 * @param primaryKeyOperationName
 	 * @param umlAttribute
 	 * @param attribute
 	 * @return
 	 */
 	@SuppressWarnings("unchecked")
 	private Operation createPrimaryKeyOperation(final String primaryKeyOperationName,
 			final String primaryKeyDataType, final Attribute umlAttribute)
 	{
 		final Operation primaryKeyOperation = umlPackage.getCore().getOperation().createOperation(
 				primaryKeyOperationName, VisibilityKindEnum.VK_PUBLIC, false,
 				ScopeKindEnum.SK_INSTANCE, false, CallConcurrencyKindEnum.CCK_SEQUENTIAL, false,
 				false, false, null);
 		primaryKeyOperation.getStereotype().addAll(
 				getOrCreateStereotypes(XMIConstants.PRIMARY_KEY,
 						XMIConstants.STEREOTYPE_BASECLASS_ATTRIBUTE));
 		primaryKeyOperation.getTaggedValue().add(
 				createTaggedValue(XMIConstants.STEREOTYPE, XMIConstants.PRIMARY_KEY));
 
 		//Return parameter
 		final Parameter returnParameter = createParameter(null, null,
 				ParameterDirectionKindEnum.PDK_RETURN);
 		final Parameter primaryKeyParam = createParameter(umlAttribute.getName(), umlAttribute
 				.getType(), ParameterDirectionKindEnum.PDK_IN);
 
 		primaryKeyParam.getTaggedValue().add(
 				createTaggedValue(XMIConstants.TYPE, primaryKeyDataType));
 
 		primaryKeyOperation.getParameter().add(returnParameter);
 		primaryKeyOperation.getParameter().add(primaryKeyParam);
 		//Add to map storing AttributeName -> OperationName
 		return primaryKeyOperation;
 	}
 
 	/**
 	 * @param attribute
 	 * @return
 	 */
 	private String getPrimaryKeyOperationName(final String entityName, final String attributeName)
 	{
 		String primaryKeyName = null;
 		if ((entityName != null) && (attributeName != null))
 		{
 			primaryKeyName = ("PK_" + entityName + "_" + attributeName);
 		}
 		return primaryKeyName;
 	}
 
 	/**
 	 * @param association
 	 * @return
 	 */
 	private String getAssociationType(final AssociationInterface association)
 	{
 		String associationType = null;
 		if (association != null)
 		{
 			final Cardinality sourceCardinality = association.getSourceRole()
 					.getMaximumCardinality();
 			final Cardinality targetCardinality = association.getTargetRole()
 					.getMaximumCardinality();
 			if ((sourceCardinality.equals(Cardinality.ONE))
 					&& (targetCardinality.equals(Cardinality.ONE)))
 			{
 				associationType = XMIConstants.ASSOC_ONE_ONE;
 			}
 			else if ((sourceCardinality.equals(Cardinality.ONE))
 					&& (targetCardinality.equals(Cardinality.MANY)))
 			{
 				associationType = XMIConstants.ASSOC_ONE_MANY;
 			}
 			if ((sourceCardinality.equals(Cardinality.MANY))
 					&& (targetCardinality.equals(Cardinality.ONE)))
 			{
 				associationType = XMIConstants.ASSOC_MANY_ONE;
 			}
 			if ((sourceCardinality.equals(Cardinality.MANY))
 					&& (targetCardinality.equals(Cardinality.MANY)))
 			{
 				associationType = XMIConstants.ASSOC_MANY_MANY;
 			}
 		}
 		return associationType;
 	}
 
 	/**
 	 * Get collection of entities
 	 * @param entityGroup
 	 * @return
 	 */
 	private Collection<EntityInterface> getEntities(final EntityGroupInterface entityGroup)
 	{
 		final List<EntityInterface> entityList = new ArrayList<EntityInterface>();
 		final Map<Long, EntityInterface> entityMap = new HashMap<Long, EntityInterface>();
 
 		final Collection<EntityInterface> entityCollection = entityGroup.getEntityCollection();
 		final Collection<EntityInterface> multiSelectentityCollection = new ArrayList<EntityInterface>();
 		for (final EntityInterface entity : entityCollection)
 		{
 			final Collection<AssociationInterface> associationCollection = new ArrayList<AssociationInterface>();
 			associationCollection.addAll(entity.getAllAssociations());
 			//if entity is not associated with any entity then also put it inside entity map
 			if (associationCollection.isEmpty())
 			{
 				entityMap.put(entity.getId(), entity);
 			}
 
 			for (final AssociationInterface association : associationCollection)
 			{
 				if (association.getIsCollection())
 				{
 					multiSelectentityCollection.add(association.getTargetEntity());
 				}
 				entityMap.put(entity.getId(), entity);
 				//Add entity only if its association is not of multiselect type ,and both target and src entity's group is same
 				if (!association.getIsCollection()
 						&& (association.getEntity().getEntityGroup().getName()
 								.equalsIgnoreCase(association.getTargetEntity().getEntityGroup()
 										.getName())))
 				{
 					entityMap.put(association.getTargetEntity().getId(), association
 							.getTargetEntity());
 				}
 			}
 		}
 		//this removes the multi select entities from entity list
 		for (final EntityInterface msEntity : multiSelectentityCollection)
 		{
 			if (entityMap.containsKey(msEntity.getId()))
 			{
 				entityMap.remove(msEntity.getId());
 			}
 		}
 		entityList.addAll(entityMap.values());
 		return entityList;
 	}
 
 	/**
 	 * args[0]= Group Name
 	 * args[1]=File name
 	 * args[2] = xmi version
 	 * args[3]=hook entity name
 	 * @param args arguments array
 	 */
 	public static void main(final String[] args)
 	{
 
 		try
 		{
 			final XMIExporter xmiExporter = new XMIExporter();
 			xmiExporter.initilizeInstanceVariables(args);
 			xmiExporter.retrieveEntityGroups();
 			xmiExporter.exportXMI();
 
 		}
 		catch (final Exception e)
 		{
 			LOGGER.error("Exception occured while Exporting the XMI ", e);
 		}
 	}
 
 	/**
 	 * This method will retrive Entity group & static entity required for exporting the xmi.
 	 * it will initialize the global variables foe it.
 	 * @throws DAOException exception.
 	 * @throws DynamicExtensionsApplicationException exception.
 	 * @throws DynamicExtensionsSystemException exception.
 	 */
 	private void retrieveEntityGroups() throws DAOException, DynamicExtensionsApplicationException,
 			DynamicExtensionsSystemException
 	{
 		HibernateDAO hibernateDao = null;
 		try
 		{
 			hibernateDao = DynamicExtensionsUtility.getHibernateDAO();
 			entityGroup = getEntityGroup(hibernateDao, groupName);
 		}
 		catch (DAOException e)
 		{
 			throw new DynamicExtensionsSystemException(
 					"Error occured while retrieving the entityGroup.", e);
 		}
 		finally
 		{
 			DynamicExtensionsUtility.closeDAO(hibernateDao);
 		}
 
 	}
 
 	/**
 	 * It will validate & initialize all the instance variables which are reqiured for
 	 * exporting the xmi.
 	 * @param args arguments array from which variables should be initialized.
 	 * @throws DynamicExtensionsApplicationException exception.
 	 */
 	public void initilizeInstanceVariables(final String[] args)
 			throws DynamicExtensionsApplicationException
 	{
 		validate(args);
 		groupName = args[0];
 		filename = args[1];
 		hookEntityName = "";
 		if ((args.length > 2) && !XMIConstants.XMI_VERSION_1_1.equalsIgnoreCase(args[2].trim()))
 		{
 			xmiVersion = XMIConstants.XMI_VERSION_1_2;
 		}
 		else
 		{
 			xmiVersion = args[2];
 		}
 		if ((args.length > 3) && !"".equals(args[3].trim()))
 		{
 			hookEntityName = args[3].trim();
 
 		}
 		/*if(XMIConstants.XMI_VERSION_1_1.equals(xmiVersion) && XMIConstants.NONE.equals(hookEntityName))
 		{
 			throw new DynamicExtensionsApplicationException("To export xmi in 1.1 Version you have to specify the Hook Entity. else export in xmi version 1.2");
 		}*/
 		generateLogForVariables();
 		repository = XMIUtilities.getRepository(filename);
 	}
 
 	/**
 	 *It will log all the variables for information.
 	 */
 	private void generateLogForVariables()
 	{
 		LOGGER.info("************EXPORT XMI GIVEN PARAMETERS***********");
 		LOGGER.info("FILE NAME  = " + filename);
 		LOGGER.info("ENTITY GROUP NAME = " + groupName);
 		LOGGER.info("XMI VERSION = " + xmiVersion);
 		LOGGER.info("HOOK ENTITY = " + hookEntityName);
 		LOGGER.info("**************************************************");
 
 	}
 
 	/**
 	 * It will validate the given arguments with what was expected.
 	 * @param args arguments array.
 	 * @throws DynamicExtensionsApplicationException exception is thrown if expected arguments are not proper.
 	 */
 	private void validate(final String[] args) throws DynamicExtensionsApplicationException
 	{
 		if (args.length < 2)
 		{
 			throw new DynamicExtensionsApplicationException(
 					"Please specify all parameters. '-Dgroupname <groupname> -Dfilename <export filename> -Dversion <version>'");
 		}
 		if ((args[0] == null) || "".equals(args[0].trim()))
 		{
 			throw new DynamicExtensionsApplicationException(
 					"Please specify groupname to be exported");
 		}
 		if ((args[1] == null) || "".equals(args[1].trim()))
 		{
 			throw new DynamicExtensionsApplicationException(
 					"Kindly specify the filename where XMI should be exported.");
 		}
 
 	}
 
 	/**
 	 * Finds "UML" package -> this is the topmost package of UML metamodel - that's the
 	 * package that needs to be instantiated in order to create a UML extent
 	 * @return Mof Package
 	 * @throws DynamicExtensionsSystemException
 	 * @throws Exception exception
 	 */
 	public static MofPackage getUmlPackage(final MDRepository repository, final XmiReader reader)
 			throws DynamicExtensionsSystemException
 	{
 		// get the MOF extent containing definition of UML metamodel
 		ModelPackage umlMM = (ModelPackage) repository.getExtent(UML_MM);
 		MofPackage result = null;
 		try
 		{
 			if (umlMM == null)
 			{
 				// it is not present -> create it
 				umlMM = (ModelPackage) repository.createExtent(UML_MM);
 			}
 			// find package named "UML" in this extent
 			result = getUmlPackage(umlMM);
 			reader.read(UmlPackage.class.getResource("resources/01-02-15_Diff.xml").toString(),
 					umlMM);
 			// try to find the "UML" package again
 			result = getUmlPackage(umlMM);
 			if (result == null)
 			{
 				// it cannot be found -> UML metamodel is not loaded -> load it from XMI
 				reader.read(UmlPackage.class.getResource("resources/01-02-15_Diff.xml").toString(),
 						umlMM);
 				// try to find the "UML" package again
 				result = getUmlPackage(umlMM);
 			}
 		}
 		catch (final Exception e)
 		{
 			throw new DynamicExtensionsSystemException(
 					"Exception occured while Intializing UML Package", e);
 		}
 		return result;
 	}
 
 	/** Finds "UML" package in a given extent
 	 * @param umlMM MOF extent that should be searched for "UML" package.
 	 * @return Mof Package
 	 */
 
 	public static MofPackage getUmlPackage(final ModelPackage umlMM)
 	{
 		// iterate through all instances of package
 		MofPackage pkg = null;
 		for (final Iterator it = umlMM.getMofPackage().refAllOfClass().iterator(); it.hasNext();)
 		{
 			pkg = (MofPackage) it.next();
 			LOGGER.info("\n\nName = " + pkg.getName());
 
 			// is the package topmost and is it named "UML"?
 			if ((pkg.getContainer() == null) && "UML".equals(pkg.getName()))
 			{
 				// yes -> return it
 				break;
 			}
 		}
 		// a topmost package named "UML" could not be found
 		return pkg;
 	}
 
 }
