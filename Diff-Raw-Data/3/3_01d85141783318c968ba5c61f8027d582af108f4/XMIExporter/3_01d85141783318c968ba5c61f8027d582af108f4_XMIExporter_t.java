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
 import edu.common.dynamicextensions.entitymanager.EntityManagerConstantsInterface;
 
 import org.apache.commons.lang.StringUtils;
 import org.netbeans.api.mdr.CreationFailedException;
 import org.netbeans.api.mdr.MDRManager;
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
 import edu.common.dynamicextensions.entitymanager.EntityGroupManager;
 import edu.common.dynamicextensions.entitymanager.EntityGroupManagerInterface;
 import edu.common.dynamicextensions.entitymanager.EntityManager;
 import edu.common.dynamicextensions.entitymanager.EntityManagerInterface;
 import edu.common.dynamicextensions.entitymanager.EntityManagerUtil;
 import edu.common.dynamicextensions.exception.DataTypeFactoryInitializationException;
 import edu.common.dynamicextensions.exception.DynamicExtensionsApplicationException;
 import edu.common.dynamicextensions.exception.DynamicExtensionsSystemException;
 import edu.common.dynamicextensions.util.global.DEConstants.AssociationDirection;
 import edu.common.dynamicextensions.util.global.DEConstants.AssociationType;
 import edu.common.dynamicextensions.util.global.DEConstants.Cardinality;
 import edu.common.dynamicextensions.xmi.XMIConstants;
 import edu.common.dynamicextensions.xmi.XMIUtilities;
 import edu.wustl.common.util.logger.Logger;
 import edu.wustl.dao.daofactory.DAOConfigFactory;
 import edu.wustl.dao.exception.DAOException;
 
 /**
  * @author falguni_sachde
  * @author pavan_kalantri
  *
  */
 public class XMIExporter implements XMIExportInterface
 {
 
 	//Repository
 	private static MDRepository repository = XMIUtilities.getRepository();
 
 	// UML extent
 	private static UmlPackage umlPackage;
 
 	//Model
 	private static Model umlModel = null;
 	//Leaf package
 	private static org.omg.uml.modelmanagement.UmlPackage logicalModel = null;
 	private static org.omg.uml.modelmanagement.UmlPackage dataModel = null;
 	private static HashMap<String, UmlClass> entityUMLClassMappings = null;
 	private static HashMap<String, UmlClass> entityDataClassMappings = null;
 	private static HashMap<EntityInterface, List<String>> entityForeignKeyAttributes = null;
 	private static HashMap<String, String> foreignKeyOperationNameMappings = null;
 	private static String packageName = null;
 	private static String versionOfxmi = null;
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.xmi.exporter.XMIExportInterface#exportXMI(java.lang.String, javax.jmi.reflect.RefPackage, java.lang.String)
 	 */
 	public void exportXMI(String filename, RefPackage extent, String xmiVersion)
 			throws IOException, TransformerException
 	{
 		//get xmi writer
 		XmiWriter writer = XMIUtilities.getXMIWriter();
 		String outputFilename = filename;
 		if (XMIConstants.XMI_VERSION_1_1.equals(xmiVersion))
 		{
 			//Write to temporary file
 			outputFilename = XMIConstants.TEMPORARY_XMI1_1_FILENAME;
 		}
 
 		//get output stream for file : appendmode : false
 		FileOutputStream outputStream = new FileOutputStream(outputFilename, false);
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
 			// shutdown the repository to make sure all caches are flushed to disk
 			MDRManager.getDefault().shutdownAll();
 			XMIUtilities.cleanUpRepository();
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
 	private void convertXMI(String srcFilename, String targetFilename)
 			throws FileNotFoundException, TransformerException
 	{
 		InputStream xsltFileStream = this.getClass().getClassLoader().getResourceAsStream(
 				XMIConstants.XSLT_FILENAME);
 		XMIUtilities.transform(srcFilename, targetFilename, xsltFileStream);
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.common.dynamicextensions.xmi.exporter.XMIExportInterface#exportXMI(java.lang.String, edu.common.dynamicextensions.domaininterface.EntityGroupInterface, java.lang.String)
 	 */
 	public void exportXMI(String filename, EntityGroupInterface entityGroup, String xmiVersion)
 			throws Exception
 	{
 		this.exportXMI(filename, entityGroup, xmiVersion, null);
 	}
 
 	/**
 	 * @param filename
 	 * @param entityGroup
 	 * @param xmiVersion
 	 * @param modelpackageName
 	 * @throws Exception
 	 */
 	public void exportXMI(String filename, EntityGroupInterface entityGroup, String xmiVersion,
 			String modelpackageName) throws Exception
 	{
 		init();
 		if (entityGroup != null)
 		{
 			versionOfxmi = xmiVersion;
 			//groupName = entityGroup.getName();
 			//UML Model generation
 			generateUMLModel(entityGroup, modelpackageName);
 			//Data Model creation
 			generateDataModel(entityGroup);
 			exportXMI(filename, umlPackage, xmiVersion);
 		}
 	}
 
 	/**
 	 * @param entityGroup
 	 * @param xmiVersion
 	 * @throws DynamicExtensionsApplicationException 
 	 * @throws DynamicExtensionsSystemException 
 	 */
 	@SuppressWarnings("unchecked")
 	private void generateDataModel(EntityGroupInterface entityGroup)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		if (entityGroup != null)
 		{
 			initializeEntityForeignKeysMap(entityGroup.getEntityCollection());
 			Collection<UmlClass> sqlTableClasses;
 			//Generate relationships between table classes
 			Collection<Relationship> sqlRelationships;
 			if (XMIConstants.XMI_VERSION_1_2.equals(versionOfxmi))
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
 	private void initializeEntityForeignKeysMap(Collection<EntityInterface> entityCollection)
 	{
 		entityForeignKeyAttributes = new HashMap<EntityInterface, List<String>>();
 		if (entityCollection != null)
 		{
 			Iterator entityCollnIter = entityCollection.iterator();
 			while (entityCollnIter.hasNext())
 			{
 				EntityInterface entity = (EntityInterface) entityCollnIter.next();
 				if (entity != null)
 				{
 					Collection<AssociationInterface> entityAssociations = entity
 							.getAssociationCollection();
 					if (entityAssociations != null)
 					{
 						Iterator entityAssocnCollnIter = entityAssociations.iterator();
 						while (entityAssocnCollnIter.hasNext())
 						{
 							AssociationInterface association = (AssociationInterface) entityAssocnCollnIter
 									.next();
 							if ((association != null)
 									&& (association.getConstraintProperties() != null))
 							{
 								String associationType = getAssociationType(association);
 								String foreignKeyOperationName = null;
 								String foreignKey = null;
 								Collection<ConstraintKeyPropertiesInterface> cnstKeyPropColl;
 
 								//For one-to-one and one-to-many association foreign key is in target entity
 								if ((associationType.equals(XMIConstants.ASSOC_ONE_MANY))
 										|| (associationType.equals(XMIConstants.ASSOC_ONE_ONE)))
 								{
 
 									cnstKeyPropColl = association.getConstraintProperties()
 											.getTgtEntityConstraintKeyPropertiesCollection();
 									Integer counter = 1;
 									for (ConstraintKeyPropertiesInterface cnstrKeyProp : cnstKeyPropColl)
 									{
 										foreignKey = cnstrKeyProp
 												.getTgtForiegnKeyColumnProperties().getName();
 										addForeignKeyAttribute(association.getTargetEntity(),
 												foreignKey);
 										//Generate foreign key operation name and add it to foreignKeyOperationNameMappings map
 										foreignKeyOperationName = generateForeignkeyOperationName(
 												association.getTargetEntity().getName(),
 												association.getEntity().getName().concat(
 														counter.toString()));
 										foreignKeyOperationNameMappings.put(foreignKey,
 												foreignKeyOperationName);
 										counter++;
 									}
 								}
 								//For many-to-one association foreign key is in source entity
 								else if (associationType.equals(XMIConstants.ASSOC_MANY_ONE))
 								{
 									cnstKeyPropColl = association.getConstraintProperties()
 											.getSrcEntityConstraintKeyPropertiesCollection();
 									Integer counter = 1;
 									for (ConstraintKeyPropertiesInterface cnstrKeyProp : cnstKeyPropColl)
 									{
 										foreignKey = cnstrKeyProp
 												.getTgtForiegnKeyColumnProperties().getName();
 										addForeignKeyAttribute(association.getEntity(), foreignKey);
 										foreignKeyOperationName = generateForeignkeyOperationName(
 												association.getEntity().getName(), association
 														.getTargetEntity().getName().concat(
 																counter.toString()));
 										//Generate foreign key operation name and add it to foreignKeyOperationNameMappings map
 										foreignKeyOperationNameMappings.put(foreignKey,
 												foreignKeyOperationName);
 									}
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * @param targetEntity
 	 * @param entity
 	 * @return
 	 */
 	private String generateForeignkeyOperationName(String foreignKeyEntityName,
 			String primaryKeyEntityName)
 	{
 		return (XMIConstants.FOREIGN_KEY_PREFIX + foreignKeyEntityName + XMIConstants.SEPARATOR + primaryKeyEntityName);
 	}
 
 	/**
 	 * @param targetEntity
 	 * @param targetEntityKey
 	 */
 	private void addForeignKeyAttribute(EntityInterface entity, String entityForeignKeyAttribute)
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
 			Collection<EntityInterface> entityCollection)
 			throws DataTypeFactoryInitializationException
 	{
 		ArrayList<Relationship> sqlRelationships = new ArrayList<Relationship>();
 		if (entityCollection != null)
 		{
 			Iterator<EntityInterface> entityCollnIter = entityCollection.iterator();
 			while (entityCollnIter.hasNext())
 			{
 				EntityInterface entity = entityCollnIter.next();
 				Collection<Relationship> entitySQLAssociations = createSQLRelationships(entity);
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
 	private Collection<Relationship> createSQLRelationships(EntityInterface entity)
 			throws DataTypeFactoryInitializationException
 	{
 		//Associations
 		ArrayList<Relationship> entitySQLRelationships = new ArrayList<Relationship>();
 		if (entity != null)
 		{
 			//Association relationships
 			Collection<AssociationInterface> entityAssociations;
 			if (XMIConstants.XMI_VERSION_1_2.equals(versionOfxmi))
 			{
 				entityAssociations = entity.getAssociationCollectionExcludingCollectionAttributes();
 			}
 			else
 			{
 				entityAssociations = entity.getAssociationCollection();
 			}
 			if (entityAssociations != null)
 			{
 				Iterator<AssociationInterface> entityAssociationsIter = entityAssociations
 						.iterator();
 				while (entityAssociationsIter.hasNext())
 				{
 					AssociationInterface association = entityAssociationsIter.next();
 					//For each association add the sourceEntityKey as foreign key operation
 					UmlAssociation sqlAssociation = createSQLAssociation(association);
 					if (sqlAssociation != null)
 					{
 						entitySQLRelationships.add(sqlAssociation);
 					}
 				}
 			}
 			//Generalization relationships
 			if (entity.getParentEntity() != null)
 			{
 				UmlClass parentClass = entityDataClassMappings.get(entity.getParentEntity()
 						.getName());
 				UmlClass childClass = entityDataClassMappings.get(entity.getName());
 				createForeignKeyAttribute(entity);
 				Generalization generalization = createGeneralization(parentClass, childClass);
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
 	private void createForeignKeyAttribute(EntityInterface entity)
 			throws DataTypeFactoryInitializationException
 	{
 		Collection<ConstraintKeyPropertiesInterface> cnstKeyPropColl = entity
 				.getConstraintProperties().getSrcEntityConstraintKeyPropertiesCollection();
 		Classifier foreignKeySQLClass = getSQLClassForEntity(entity.getName());
 		String columnName;
 		for (ConstraintKeyPropertiesInterface cnstrKeyProp : cnstKeyPropColl)
 		{
 			columnName = cnstrKeyProp.getTgtForiegnKeyColumnProperties().getName();
 			Attribute foreignKeyAttribute = searchAttribute(foreignKeySQLClass, columnName);
 			//Create attribute if does not exist
 			if (foreignKeyAttribute == null)
 			{
 				//Datatype of foreign key and prmary key will be same
 				//AttributeInterface primaryKeyAttr = getPrimaryKeyAttribute(primaryKeyEntity);
 				AttributeInterface primaryKeyAttr = cnstrKeyProp.getSrcPrimaryKeyAttribute();
 				foreignKeyAttribute = createDataAttribute(columnName, primaryKeyAttr.getDataType());
 				String foreignKeyOprName = generateForeignkeyOperationName(entity.getName(), entity
 						.getParentEntity().getName());
 				foreignKeyOperationNameMappings.put(foreignKeyAttribute.getName(),
 						foreignKeyOprName);
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
 	private Operation createForeignKeyOperation(Attribute foreignKeyColumn)
 	{
 		String foreignKeyOperationName = getForeignkeyOperationName(foreignKeyColumn.getName());
 		Operation foreignKeyOperation = umlPackage.getCore().getOperation().createOperation(
 				foreignKeyOperationName, VisibilityKindEnum.VK_PUBLIC, false,
 				ScopeKindEnum.SK_INSTANCE, false, CallConcurrencyKindEnum.CCK_SEQUENTIAL, false,
 				false, false, null);
 
 		foreignKeyOperation.getStereotype().addAll(
 				getOrCreateStereotypes(XMIConstants.FOREIGN_KEY,
 						XMIConstants.STEREOTYPE_BASECLASS_ATTRIBUTE));
 		foreignKeyOperation.getTaggedValue().add(
 				createTaggedValue(XMIConstants.STEREOTYPE, XMIConstants.FOREIGN_KEY));
 
 		//Return parameter
 		Parameter returnParameter = createParameter(null, null,
 				ParameterDirectionKindEnum.PDK_RETURN);
 		Parameter inParameter = createParameter(foreignKeyColumn.getName(), foreignKeyColumn
 				.getType(), ParameterDirectionKindEnum.PDK_IN);
 		inParameter.getTaggedValue().add(
 				createTaggedValue(XMIConstants.TYPE, foreignKeyColumn.getType().getName()));
 
 		foreignKeyOperation.getParameter().add(returnParameter);
 		foreignKeyOperation.getParameter().add(inParameter);
 		foreignKeyOperationNameMappings.put(foreignKeyColumn.getName(), foreignKeyOperationName);
 		return foreignKeyOperation;
 	}
 
 	/**
 	 * @param name
 	 * @return
 	 */
 	private String getForeignkeyOperationName(String foreignKeyName)
 	{
 		if ((foreignKeyOperationNameMappings != null) && (foreignKeyName != null))
 		{
 			return ((String) foreignKeyOperationNameMappings.get(foreignKeyName));
 		}
 		return null;
 	}
 
 	/**
 	 * @return
 	 */
 	private Parameter createParameter(String parameterName, Classifier parameterType,
 			ParameterDirectionKindEnum direction)
 	{
 		//Return parameter
 		Parameter parameter = umlPackage.getCore().getParameter().createParameter(parameterName,
 				VisibilityKindEnum.VK_PUBLIC, false, null, direction);
 		parameter.setType(parameterType);
 		return parameter;
 	}
 
 	/**
 	 * @param association
 	 * @return
 	 * @throws DataTypeFactoryInitializationException
 	 */
 	@SuppressWarnings("unchecked")
 	private UmlAssociation createSQLAssociation(AssociationInterface association)
 			throws DataTypeFactoryInitializationException
 	{
 		UmlAssociation sqlAssociation = null;
 		CorePackage corePackage = umlPackage.getCore();
 		if ((association != null) && (association.getConstraintProperties() != null))
 		{
 			ConstraintPropertiesInterface constraintProperties = association
 					.getConstraintProperties();
 			String associationName = getAssociationName(association);
 			sqlAssociation = corePackage.getUmlAssociation().createUmlAssociation(associationName,
 					VisibilityKindEnum.VK_PUBLIC, false, false, false, false);
 			if (sqlAssociation != null)
 			{
 				//Set the ends for the association 
 				//End that is on the 'many' side of the association will have foreign key oprn name & on 'one' side will be primary key oprn name
 				String associationType = getAssociationType(association);
 				Classifier sourceSQLClass = getSQLClassForEntity(association.getEntity().getName());
 				Classifier targetSQLClass = getSQLClassForEntity(association.getTargetEntity()
 						.getName());
 				RoleInterface sourceRole = association.getSourceRole();
 				RoleInterface targetRole = association.getTargetRole();
 
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
 				TaggedValue directionTaggedValue = getDirectionTaggedValue(association);
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
 	private Attribute getForeignKeyAttribute(EntityInterface foreignKeyEntity,
 			Collection<ConstraintKeyPropertiesInterface> cnstKeyPropColl,
 			String implementedAssociationName) throws DataTypeFactoryInitializationException
 	{
 		Classifier foreignKeySQLClass = getSQLClassForEntity(foreignKeyEntity.getName());
 		String columnName;
 		for (ConstraintKeyPropertiesInterface cnstrKeyProp : cnstKeyPropColl)
 		{
 			columnName = cnstrKeyProp.getTgtForiegnKeyColumnProperties().getName();
 			Attribute foreignKeyAttribute = searchAttribute(foreignKeySQLClass, columnName);
 			EntityInterface primaryKeyEntity = cnstrKeyProp.getSrcPrimaryKeyAttribute().getEntity();
 			//Create attribute if does not exist
 			if (foreignKeyAttribute == null)
 			{
 				//Datatype of foreign key and prmary key will be same
 				//AttributeInterface primaryKeyAttr = getPrimaryKeyAttribute(primaryKeyEntity);
 				AttributeInterface primaryKeyAttr = cnstrKeyProp.getSrcPrimaryKeyAttribute();
 				foreignKeyAttribute = createDataAttribute(columnName, primaryKeyAttr.getDataType());
 				foreignKeySQLClass.getFeature().add(foreignKeyAttribute);
 				//Add foreign key operation
 				foreignKeySQLClass.getFeature().add(createForeignKeyOperation(foreignKeyAttribute));
 			}
 			String implementedAssociation = null;
 			//TODO  check
 			if (versionOfxmi.equals(XMIConstants.XMI_VERSION_1_2))
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
 	private void handleManyToManyAssociation(AssociationInterface association)
 			throws DataTypeFactoryInitializationException
 	{
 		//Create corelation table
 		UmlClass corelationTable = createCoRelationTable(association);
 
 		if (corelationTable != null)
 		{
 			//Relation with source entity
 			UmlClass sourceClass = (UmlClass) getSQLClassForEntity(association.getEntity()
 					.getName());
 			UmlClass targetClass = (UmlClass) getSQLClassForEntity(association.getTargetEntity()
 					.getName());
 			RoleInterface role = getRole(AssociationType.ASSOCIATION, null, Cardinality.ONE,
 					Cardinality.ONE);
 			UmlAssociation srcToCoRelnTable = createAssocForCorelnTable(sourceClass,
 					corelationTable, association.getSourceRole(), role);
 			//Create relation with target entity
 			UmlAssociation coRelnTableToTarget = createAssocForCorelnTable(corelationTable,
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
 	private UmlAssociation createAssocForCorelnTable(UmlClass sourceEntity, UmlClass targetEntity,
 			RoleInterface sourceRole, RoleInterface targetRole)
 	{
 		UmlAssociation umlAssociation = umlPackage.getCore().getUmlAssociation()
 				.createUmlAssociation(null, VisibilityKindEnum.VK_PUBLIC, false, false, false,
 						false);
 		AssociationEnd sourceEnd = getAssociationEnd(sourceRole, sourceEntity,
 				XMIConstants.ASSN_SRC_ENTITY);
 		AssociationEnd targetEnd = getAssociationEnd(targetRole, targetEntity,
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
 	private UmlClass createCoRelationTable(AssociationInterface association)
 			throws DataTypeFactoryInitializationException
 	{
 		ConstraintPropertiesInterface constraintProperties = association.getConstraintProperties();
 		if (constraintProperties != null)
 		{
 			String coRelationTableName = constraintProperties.getName();
 			UmlClass corelationClass = createDataClass(coRelationTableName);
 
 			Collection<Feature> coRelationAttributes = createCoRelationTableAttribsAndOperns(association);
 			//Add to co-relation class
 			corelationClass.getFeature().addAll(coRelationAttributes);
 
 			return corelationClass;
 		}
 		return null;
 	}
 
 	/**
 	 * @param association
 	 * @return
 	 * @throws DataTypeFactoryInitializationException 
 	 */
 	@SuppressWarnings("unchecked")
 	private Collection<Feature> createCoRelationTableAttribsAndOperns(
 			AssociationInterface association) throws DataTypeFactoryInitializationException
 	{
 		ArrayList<Feature> corelationTableFeatures = new ArrayList<Feature>();
 		ConstraintPropertiesInterface constraintProperties = association.getConstraintProperties();
 		Collection<ConstraintKeyPropertiesInterface> srcCnstKeyProps = constraintProperties
 				.getSrcEntityConstraintKeyPropertiesCollection();
 		Collection<ConstraintKeyPropertiesInterface> tgtCnstKeyProps = constraintProperties
 				.getTgtEntityConstraintKeyPropertiesCollection();
 		for (ConstraintKeyPropertiesInterface cnstrKeyProp : srcCnstKeyProps)
 		{
 			Attribute coRelationAttribute = createCoRelationalAttributeAndOperns(cnstrKeyProp,
 					association.getEntity());
 			//Add "implements-association tagged value 
 			String srcAttribImplementedAssocn = null;
 			if (versionOfxmi.equals(XMIConstants.XMI_VERSION_1_2))
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
 			String foreignKeyOprName = generateForeignkeyOperationName(association
 					.getTargetEntity().getName(), constraintProperties.getName());
 			foreignKeyOperationNameMappings.put(coRelationAttribute.getName(), foreignKeyOprName);
 			corelationTableFeatures.add(createForeignKeyOperation(coRelationAttribute));
 		}
 		for (ConstraintKeyPropertiesInterface cnstrKeyProp : tgtCnstKeyProps)
 		{
 			Attribute coRelationAttribute = createCoRelationalAttributeAndOperns(cnstrKeyProp,
 					association.getTargetEntity());
 			String targetAttribImplementedAssocn = null;
 			if (versionOfxmi.equals(XMIConstants.XMI_VERSION_1_2))
 			{
 				//TODO- check
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
 			String foreignKeyOprName = generateForeignkeyOperationName(constraintProperties
 					.getName(), association.getEntity().getName());
 			foreignKeyOperationNameMappings.put(coRelationAttribute.getName(), foreignKeyOprName);
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
 			ConstraintKeyPropertiesInterface cnstrKeyProp, EntityInterface entity)
 			throws DataTypeFactoryInitializationException
 	{
 		Attribute coRelationAttribute = null;
 		if (cnstrKeyProp.getSrcPrimaryKeyAttribute() != null)
 		{
 			String corelationTableAttributeName = generateCorelationAttributeName(entity,
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
 	private String generateCorelationAttributeName(EntityInterface entity, String sourceEntityKey)
 	{
 		return entity.getName() + XMIConstants.SEPARATOR + sourceEntityKey;
 	}
 
 	/**
 	 * @param association
 	 * @return
 	 */
 	private TaggedValue getDirectionTaggedValue(AssociationInterface association)
 	{
 		TaggedValue directionTaggedValue = null;
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
 	private String getAssociationName(AssociationInterface association)
 	{
 		return association.getName();
 	}
 
 	/**
 	 * @param targetEntity
 	 * @param targetEntityKey
 	 * @return
 	 */
 	private Attribute searchAttribute(Classifier targetEntityUmlClass, String attributeName)
 	{
 		if ((targetEntityUmlClass != null) && (attributeName != null))
 		{
 			List entityAttributes = targetEntityUmlClass.getFeature();
 			if (entityAttributes != null)
 			{
 				Iterator entityAttribIter = entityAttributes.iterator();
 				while (entityAttribIter.hasNext())
 				{
 					Object attribute = entityAttribIter.next();
 					if (attribute instanceof Attribute
 							&& ((Attribute) attribute).getName().equals(attributeName))
 					{
 						return (Attribute) attribute;
 					}
 				}
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * @param entityCollection
 	 * @return
 	 * @throws DynamicExtensionsApplicationException 
 	 * @throws DynamicExtensionsSystemException 
 	 */
 	@SuppressWarnings("unchecked")
 	private Collection<UmlClass> getDataClasses(Collection<EntityInterface> entityCollection)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		ArrayList<UmlClass> sqlTableClasses = new ArrayList<UmlClass>();
 		if (entityCollection != null)
 		{
 			Iterator entityCollectionIter = entityCollection.iterator();
 			while (entityCollectionIter.hasNext())
 			{
 				EntityInterface entity = (EntityInterface) entityCollectionIter.next();
 				UmlClass sqlTableClass = createDataClass(entity);
 				if (sqlTableClass != null)
 				{
 					sqlTableClasses.add(sqlTableClass);
 					entityDataClassMappings.put(entity.getName(), sqlTableClass);
 					//Create dependency with parent
 					UmlClass entityUmlClass = (UmlClass) entityUMLClassMappings.get(entity
 							.getName());
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
 	private UmlClass createDataClass(EntityInterface entity)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		UmlClass entityDataClass = null;
 		if (entity != null)
 		{
 			TablePropertiesInterface tableProps = entity.getTableProperties();
 			if (tableProps != null)
 			{
 				String tableName = tableProps.getName();
 				entityDataClass = createDataClass(tableName);
 				List<String> foreignKeyAttributes = entityForeignKeyAttributes.get(entity);
 				//Entity Attributes & Operations(Primary Key) of data class
 				if (XMIConstants.XMI_VERSION_1_2.equals(versionOfxmi))
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
 	private UmlClass createDataClass(String tableName)
 	{
 		String appName = DynamicExtensionDAO.getInstance().getAppName();
 		String dbType = DAOConfigFactory.getInstance().getDAOFactory(appName).getDataBaseType();
 		UmlClass dataClass = umlPackage.getCore().getUmlClass().createUmlClass(tableName,
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
 			Collection<AttributeInterface> entityAttributes, List<String> entityForeignKeys)
 			throws DataTypeFactoryInitializationException
 	{
 		//Add attributes and operations
 		ArrayList<Feature> classFeatures = new ArrayList<Feature>();
 		//Add Attributes and primary keys as operations
 		if (entityAttributes != null)
 		{
 			Iterator entityAttributesIter = entityAttributes.iterator();
 			while (entityAttributesIter.hasNext())
 			{
 				AttributeInterface attribute = (AttributeInterface) entityAttributesIter.next();
 				//process all attribute datatype other than File type attribute
 				if (attribute.getDataType() != EntityManagerConstantsInterface.FILE_ATTRIBUTE_TYPE)
 				{
 					Attribute umlAttribute = createDataAttribute(attribute);
 					classFeatures.add(umlAttribute);
 					//If primary key : add as operation
 					if (attribute.getIsPrimaryKey())
 					{
 						Operation primaryKeyOperationSpecn = createPrimaryKeyOperation(attribute,
 								umlAttribute);
 						if (primaryKeyOperationSpecn != null)
 						{
 							classFeatures.add(primaryKeyOperationSpecn);
 						}
 					}
 
 					//If attribute is a foreign key attribute add foreign key operation  
 					//elseif  attribute not in foreign key list, add "mapped-attributes" tagged value
 					if (isForeignKey(umlAttribute.getName(), entityForeignKeys))
 					{
 						Operation foreignKeyOperationSpecn = createForeignKeyOperation(umlAttribute);
 						if (foreignKeyOperationSpecn != null)
 						{
 							classFeatures.add(foreignKeyOperationSpecn);
 						}
 					}
 					else
 					{
 						if (umlAttribute != null
 								&& versionOfxmi.equals(XMIConstants.XMI_VERSION_1_2))
 						{
 							String tagValue = XMIConstants.PACKAGE_NAME_LOGICAL_VIEW
 									+ XMIConstants.DOT_SEPARATOR
 									+ XMIConstants.PACKAGE_NAME_LOGICAL_MODEL
 									+ XMIConstants.DOT_SEPARATOR + packageName
 									+ XMIConstants.DOT_SEPARATOR + attribute.getEntity().getName()
 									+ XMIConstants.DOT_SEPARATOR + attribute.getName();
 							umlAttribute.getTaggedValue().add(
 									createTaggedValue(XMIConstants.TAGGED_VALUE_MAPPED_ATTRIBUTES,
 											tagValue));
 						}
 						else if (umlAttribute != null
 								&& versionOfxmi.equals(XMIConstants.XMI_VERSION_1_1))
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
 	private boolean isForeignKey(String attribute, List<String> foreignKeyAttributes)
 	{
 		if ((attribute != null) && (foreignKeyAttributes != null))
 		{
 			Iterator foreignKeyAttributesIter = foreignKeyAttributes.iterator();
 			while (foreignKeyAttributesIter.hasNext())
 			{
 				String foreignKey = (String) foreignKeyAttributesIter.next();
 				if (attribute.equals(foreignKey))
 				{
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * @param attribute
 	 * @return
 	 * @throws DataTypeFactoryInitializationException 
 	 */
 	@SuppressWarnings("unchecked")
 	private Attribute createDataAttribute(AttributeInterface entityAttribute)
 			throws DataTypeFactoryInitializationException
 	{
 		Attribute dataColumn = null;
 		if (entityAttribute != null)
 		{
 			ColumnPropertiesInterface columnProperties = entityAttribute.getColumnProperties();
 			if (columnProperties != null)
 			{
 				String columnName = columnProperties.getName();
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
 	private Attribute createDataAttribute(String columnName, String dataType)
 			throws DataTypeFactoryInitializationException
 	{
 		Attribute dataColumn = umlPackage.getCore().getAttribute().createAttribute(columnName,
 				VisibilityKindEnum.VK_PUBLIC, false, ScopeKindEnum.SK_INSTANCE, null,
 				ChangeableKindEnum.CK_CHANGEABLE, ScopeKindEnum.SK_CLASSIFIER,
 				OrderingKindEnum.OK_UNORDERED, null);
 		Classifier typeClass = getOrCreateDataType(DatatypeMappings.get(dataType)
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
 	private void generateUMLModel(EntityGroupInterface entityGroup, String modelPackageName)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException,
 			DAOException
 	{
 		if (entityGroup != null)
 		{
 			//Create package for entity group
 			org.omg.uml.modelmanagement.UmlPackage umlGroupPackage = getLeafPackage(entityGroup,
 					modelPackageName);
 
 			//CLASSES : CREATE : create classes for entities 
 			Collection<UmlClass> umlEntityClasses = null;
 			//Relationships  : ASSOCIATIONS/GENERALIZATION/DEPENDENCIES :CREATE
 			Collection<Relationship> umlRelationships = null;
 			if (XMIConstants.XMI_VERSION_1_2.equals(versionOfxmi))
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
 			Collection<TaggedValue> groupTaggedValues = getTaggedValues(entityGroup);
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
 	private Collection<TaggedValue> getTaggedValues(AbstractMetadataInterface abstractMetadataObj)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		ArrayList<TaggedValue> taggedValues = new ArrayList<TaggedValue>();
 		taggedValues.add(createTaggedValue(XMIConstants.TAGGED_VALUE_DESCRIPTION,
 				abstractMetadataObj.getDescription()));
 		if (abstractMetadataObj.getId() != null)
 		{
 			taggedValues.add(createTaggedValue(XMIConstants.TAGGED_VALUE_ID, abstractMetadataObj
 					.getId().toString()));
 		}
 		/*taggedValues.add(createTaggedValue("publicId",abstractMetadataObj.getPublicId()));
 		taggedValues.add(createTaggedValue("createdDate",Utility.parseDateToString(abstractMetadataObj.getCreatedDate(), Constants.DATE_PATTERN_MM_DD_YYYY)));
 		taggedValues.add(createTaggedValue("lastUpdated",Utility.parseDateToString(abstractMetadataObj.getLastUpdated(), Constants.DATE_PATTERN_MM_DD_YYYY)));*/
 
 		addConceptCodeTaggedValues(abstractMetadataObj, taggedValues);
 		EntityManagerInterface entityManager = null;
 		if (abstractMetadataObj instanceof AttributeInterface)
 		{
 			AttributeInterface attribute = (AttributeInterface) abstractMetadataObj;
 			//Tag values for Validation rules like mandatory, unique, range(min, max)
 			addRuleTagVaues(taggedValues, attribute);
 
 			AttributeTypeInformationInterface attrTypeInfo = attribute
 					.getAttributeTypeInformation();
 
 			if (XMIConstants.XMI_VERSION_1_2.equals(versionOfxmi))
 			{
 				entityManager = EntityManager.getInstance();
 				//setting UI properties tag values			
 				ControlInterface control = entityManager
 						.getControlByAbstractAttributeIdentifier(attribute.getId());
 				setUIPropertiesTagValues(taggedValues, attrTypeInfo, control, attribute);
 			}
 		}
 		else if (abstractMetadataObj instanceof AssociationInterface)
 		{//Association tag values
 			AssociationInterface association = (AssociationInterface) abstractMetadataObj;
 			if (XMIConstants.XMI_VERSION_1_2.equals(versionOfxmi))
 			{
 				entityManager = EntityManager.getInstance();
 				ControlInterface control = entityManager
 						.getControlByAbstractAttributeIdentifier(association.getId());
 				setAssociationTagValues(control, taggedValues);
 			}
 		}
 
 		Collection<TaggedValueInterface> taggedValueCollection = abstractMetadataObj
 				.getTaggedValueCollection();
 		if (taggedValueCollection != null)
 		{
 			Iterator<TaggedValueInterface> taggedValueCollnIter = taggedValueCollection.iterator();
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
 	private void setAssociationTagValues(ControlInterface control,
 			ArrayList<TaggedValue> taggedValues)
 	{
 		if (control != null)
 		{
 			SelectControl selectControl = (SelectControl) control;
 			if (selectControl.getSeparator() != null)
 			{//Seperator
 				taggedValues.add(createTaggedValue(XMIConstants.TAGGED_VALUE_SEPARATOR,
 						selectControl.getSeparator()));
 			}
 			Collection<AssociationDisplayAttributeInterface> associationDisplayAttrColl = selectControl
 					.getAssociationDisplayAttributeCollection();
 			if (associationDisplayAttrColl != null && !associationDisplayAttrColl.isEmpty())
 			{// Attributes to be displayed in drop down
 				StringBuffer attributeNames = new StringBuffer();
 				for (AssociationDisplayAttributeInterface associationDisplayAttribute : associationDisplayAttrColl)
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
 				ListBoxInterface listBox = (ListBoxInterface) control;
 				if (listBox.getIsMultiSelect() != null && listBox.getNoOfRows() != null
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
 	private void addConceptCodeTaggedValues(AbstractMetadataInterface abstractMetadataObj,
 			ArrayList<TaggedValue> taggedValues)
 	{
 		Collection<SemanticPropertyInterface> semanticPropertyCollection = abstractMetadataObj
 				.getOrderedSemanticPropertyCollection();
 		if (abstractMetadataObj instanceof EntityInterface)
 		{
 			for (SemanticPropertyInterface semanticProperty : semanticPropertyCollection)
 			{
 				if (semanticProperty.getSequenceNumber() == 0)
 				{
 					taggedValues.add(createTaggedValue(
 							XMIConstants.TAGGED_VALUE_OBJECT_CLASS_CONCEPT_CODE, semanticProperty
 									.getConceptCode()));
 					taggedValues.add(createTaggedValue(
 							XMIConstants.TAGGED_VALUE_OBJECT_CLASS_CONCEPT_DEFINITION,
 							semanticProperty.getConceptDefinition()));
 					taggedValues.add(createTaggedValue(
 							XMIConstants.TAGGED_VALUE_OBJECT_CLASS_CONCEPT_DEFINITION_SOURCE,
 							semanticProperty.getTerm()));
 					taggedValues.add(createTaggedValue(
 							XMIConstants.TAGGED_VALUE_OBJECT_CLASS_CONCEPT_PREFERRED_NAME,
 							semanticProperty.getThesaurasName()));
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
 									semanticProperty.getTerm()));
 					taggedValues.add(createTaggedValue(
 							XMIConstants.TAGGED_VALUE_OBJECT_CLASS_QUALIFIER_CONCEPT_PREFERRED_NAME
 									+ semanticProperty.getSequenceNumber(), semanticProperty
 									.getThesaurasName()));
 				}
 			}
 		}
 		else if (abstractMetadataObj instanceof AttributeInterface)
 		{
 			for (SemanticPropertyInterface semanticProperty : semanticPropertyCollection)
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
 							semanticProperty.getTerm()));
 					taggedValues.add(createTaggedValue(
 							XMIConstants.TAGGED_VALUE_PROPERTY_CONCEPT_PREFERRED_NAME,
 							semanticProperty.getThesaurasName()));
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
 									.getTerm()));
 					taggedValues.add(createTaggedValue(
 							XMIConstants.TAGGED_VALUE_PROPERTY_QUALIFIER_CONCEPT_PREFERRED_NAME
 									+ semanticProperty.getSequenceNumber(), semanticProperty
 									.getThesaurasName()));
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
 	private void setUIPropertiesTagValues(ArrayList<TaggedValue> taggedValues,
 			AttributeTypeInformationInterface attributeTypeInformation, ControlInterface control,
 			AttributeInterface attribute)
 	{
 		if (attributeTypeInformation instanceof DateAttributeTypeInformation)
 		{
 			taggedValues.add(createTaggedValue(XMIConstants.TAGGED_VALUE_DATE_FORMAT,
 					((DateAttributeTypeInformation) attributeTypeInformation).getFormat()));
 		}
 		else if (attributeTypeInformation instanceof BooleanAttributeTypeInformation)
 		{
 
 		}
 		else if (attributeTypeInformation instanceof FileAttributeTypeInformation)
 		{
 
 		}
 		else
 		{// String attribute type information
 			if (attributeTypeInformation instanceof StringAttributeTypeInformation)
 			{//String attribute
 				StringAttributeTypeInformation stringAttributeTypeInformation = (StringAttributeTypeInformation) attributeTypeInformation;
 				Integer maxLength = stringAttributeTypeInformation.getSize();
 				if (maxLength != null)
 				{
 					taggedValues.add(createTaggedValue(XMIConstants.TAGGED_VALUE_MAX_LENGTH,
 							maxLength.toString()));
 				}
 				if (control != null)
 				{
 					if (control instanceof TextFieldInterface)
 					{
 						TextFieldInterface textField = (TextFieldInterface) control;
 						addTextFieldTagValues(textField, taggedValues);
 					}
 					else if (control instanceof TextAreaInterface)
 					{
 						TextAreaInterface textArea = (TextAreaInterface) control;
 						addTextAreaTagValues(textArea, taggedValues);
 					}
 				}
 			}
 			else
 			{//Number attribute
 				Integer precision = ((NumericAttributeTypeInformation) attributeTypeInformation)
 						.getDecimalPlaces();
 				if (precision != null)
 				{
 					taggedValues.add(createTaggedValue(XMIConstants.TAGGED_VALUE_PRECISION,
 							precision.toString()));
 				}
 			}
 		}
 		if (attribute.getIsIdentified() != null && attribute.getIsIdentified().booleanValue())
 		{// PHI attribute
 			taggedValues.add(createTaggedValue(XMIConstants.TAGGED_VALUE_PHI_ATTRIBUTE, attribute
 					.getIsIdentified().toString()));
 		}
 		if (attributeTypeInformation.getDefaultValue() != null
 				&& attributeTypeInformation.getDefaultValue().getValueAsObject() != null)
 		{//Default value
 			taggedValues.add(createTaggedValue(XMIConstants.TAGGED_VALUE_DEFAULT_VALUE,
 					attributeTypeInformation.getDefaultValue().getValueAsObject().toString()));
 		}
 	}
 
 	/**
 	 * @param textField
 	 * @param taggedValues
 	 */
 	private void addTextFieldTagValues(TextFieldInterface textField,
 			ArrayList<TaggedValue> taggedValues)
 	{
 		Integer width = textField.getColumns();
 		if (width != null)
 		{
 			taggedValues.add(createTaggedValue(XMIConstants.TAGGED_VALUE_DISPLAY_WIDTH, width
 					.toString()));
 		}
 		Boolean isPassword = textField.getIsPassword();
 		if (isPassword != null && isPassword.booleanValue())
 		{
 			taggedValues.add(createTaggedValue(XMIConstants.TAGGED_VALUE_PASSWORD, isPassword
 					.toString()));
 		}
 		Boolean isUrl = textField.getIsUrl();
 		if (isUrl != null && isUrl.booleanValue())
 		{
 			taggedValues.add(createTaggedValue(XMIConstants.TAGGED_VALUE_URL, isUrl.toString()));
 		}
 	}
 
 	/**
 	 * @param textArea
 	 * @param taggedValues
 	 */
 	private void addTextAreaTagValues(TextAreaInterface textArea,
 			ArrayList<TaggedValue> taggedValues)
 	{
 		Integer noOfRows = textArea.getRows();
 		if (noOfRows != null && noOfRows.intValue() > 0)
 		{
 			taggedValues.add(createTaggedValue(XMIConstants.TAGGED_VALUE_MULTILINE, noOfRows
 					.toString()));
 		}
 	}
 
 	/**
 	 * @param taggedValues
 	 * @param attributeInterface
 	 */
 	private void addRuleTagVaues(ArrayList<TaggedValue> taggedValues,
 			AttributeInterface attributeInterface)
 	{
 		Collection<RuleInterface> ruleColl = attributeInterface.getRuleCollection();
 		for (RuleInterface rule : ruleColl)
 		{
 			String ruleTag = XMIConstants.TAGGED_VALUE_RULE + XMIConstants.SEPARATOR
 					+ rule.getName();
 			String ruleValue = "";
 
 			Collection<RuleParameterInterface> ruleParamColl = rule.getRuleParameterCollection();
 			if (ruleParamColl == null || ruleParamColl.isEmpty())
 			{
 				taggedValues.add(createTaggedValue(ruleTag, ruleValue));
 			}
 			else
 			{
 				for (RuleParameterInterface ruleParam : ruleParamColl)
 				{
 					StringBuffer temp = new StringBuffer(ruleTag);
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
 	private TaggedValue createTaggedValue(TaggedValueInterface taggedValueIntf)
 	{
 		if (taggedValueIntf != null)
 		{
 			return createTaggedValue(taggedValueIntf.getKey(), taggedValueIntf.getValue());
 		}
 		return null;
 	}
 
 	/**
 	 * @param entityCollection
 	 * @return
 	 */
 	private Collection<Relationship> getUMLRelationships(
 			Collection<EntityInterface> entityCollection)
 	{
 		ArrayList<Relationship> umlRelationships = new ArrayList<Relationship>();
 		if (entityCollection != null)
 		{
 			Iterator<EntityInterface> entityCollnIter = entityCollection.iterator();
 			while (entityCollnIter.hasNext())
 			{
 				EntityInterface entity = entityCollnIter.next();
 				Collection<Relationship> entityAssociations = createUMLRelationships(entity);
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
 	private Collection<Relationship> createUMLRelationships(EntityInterface entity)
 	{
 		ArrayList<Relationship> entityUMLRelationships = new ArrayList<Relationship>();
 		if (entity != null)
 		{
 			//Association relationships
 			Collection<AssociationInterface> entityAssociations = null;
 			if (XMIConstants.XMI_VERSION_1_2.equals(versionOfxmi))
 			{
 				entityAssociations = entity.getAssociationCollectionExcludingCollectionAttributes();
 			}
 			else
 			{
 				entityAssociations = entity.getAssociationCollection();
 			}
 			if (entityAssociations != null)
 			{
 				Iterator<AssociationInterface> entityAssociationsIter = entityAssociations
 						.iterator();
 				while (entityAssociationsIter.hasNext())
 				{
 					AssociationInterface association = entityAssociationsIter.next();
 					UmlAssociation umlAssociation = createUMLAssociation(association);
 					if (umlAssociation != null)
 					{
 						entityUMLRelationships.add(umlAssociation);
 					}
 				}
 			}
 			//generalizations
 			EntityInterface parentEntity = entity.getParentEntity();
 			if (parentEntity != null)
 			{
 				UmlClass parentClass = entityUMLClassMappings.get(parentEntity.getName());
 				UmlClass childClass = entityUMLClassMappings.get(entity.getName());
 				Generalization generalization = createGeneralization(parentClass, childClass);
 				entityUMLRelationships.add(generalization);
 			}
 		}
 		return entityUMLRelationships;
 	}
 
 	/**
 	 * @param parentEntity
 	 * @param entity
 	 */
 	private Generalization createGeneralization(UmlClass parentClass, UmlClass childClass)
 	{
 		Generalization generalization = umlPackage.getCore().getGeneralization()
 				.createGeneralization(null, VisibilityKindEnum.VK_PUBLIC, false, null);
 		org.omg.uml.foundation.core.GeneralizableElement parent = (org.omg.uml.foundation.core.GeneralizableElement) parentClass;
 		org.omg.uml.foundation.core.GeneralizableElement child = (org.omg.uml.foundation.core.GeneralizableElement) childClass;
 		generalization.setParent(parent);
 		generalization.setChild(child);
 		return generalization;
 	}
 
 	/**
 	 * @param association
 	 * @return
 	 */
 	@SuppressWarnings("unchecked")
 	private UmlAssociation createUMLAssociation(AssociationInterface association)
 	{
 		UmlAssociation umlAssociation = null;
 		CorePackage corePackage = umlPackage.getCore();
 		if (association != null)
 		{
 			umlAssociation = corePackage.getUmlAssociation()
 					.createUmlAssociation(association.getName(), VisibilityKindEnum.VK_PUBLIC,
 							false, false, false, false);
 
 			if (umlAssociation != null)
 			{
 				//Set the ends
 				AssociationEnd sourceEnd = null;
 				EntityInterface sourceEntity = association.getEntity();
 				if (sourceEntity != null)
 				{
 					Classifier sourceClass = getUMLClassForEntity(sourceEntity.getName());
 					sourceEnd = getAssociationEnd(association.getSourceRole(), sourceClass,
 							XMIConstants.ASSN_SRC_ENTITY);
 				}
 
 				EntityInterface targetEntity = association.getTargetEntity();
 				if (targetEntity != null)
 				{
 					Classifier targetClass = getUMLClassForEntity(targetEntity.getName());
 					AssociationEnd targetEnd = getAssociationEnd(association.getTargetRole(),
 							targetClass, XMIConstants.ASSN_TGT_ENTITY);
 					umlAssociation.getConnection().add(targetEnd);
 				}
 				umlAssociation.getConnection().add(sourceEnd);
 				//set the direction
 				TaggedValue directionTaggedValue = getDirectionTaggedValue(association);
 				if (directionTaggedValue != null)
 				{
 					umlAssociation.getTaggedValue().add(directionTaggedValue);
 				}
 				//If association is many-to-many add "correlation-table" tagged value
 				if (XMIConstants.ASSOC_MANY_MANY.equals(getAssociationType(association))
 						&& association.getConstraintProperties() != null)
 				{
 					String corelnTableName = association.getConstraintProperties().getName();
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
 	protected static TaggedValue createTaggedValue(String name, String value)
 	{
 		if (name != null)
 		{
 			Collection values = new HashSet();
 			if (value != null)
 			{
 				values.add(value);
 			}
 
 			TaggedValue taggedValue = umlPackage.getCore().getTaggedValue().createTaggedValue(name,
 					VisibilityKindEnum.VK_PUBLIC, false, values);
 			if (versionOfxmi.equals(XMIConstants.XMI_VERSION_1_2))
 			{
 				taggedValue.setType(umlPackage.getCore().getTagDefinition().createTagDefinition(
 						name, VisibilityKindEnum.VK_PUBLIC, false, null, null));
 			}
 
 			return taggedValue;
 		}
 		return null;
 	}
 
 	/**
 	 * @param name
 	 * @return
 	 */
 	private Classifier getUMLClassForEntity(String entityName)
 	{
 		if ((entityUMLClassMappings != null) && (entityName != null))
 		{
 			return (Classifier) entityUMLClassMappings.get(entityName);
 		}
 		return null;
 	}
 
 	/**
 	 * @param entityName
 	 * @return
 	 */
 	private Classifier getSQLClassForEntity(String entityName)
 	{
 		if ((entityDataClassMappings != null) && (entityName != null))
 		{
 			return (Classifier) entityDataClassMappings.get(entityName);
 		}
 		return null;
 	}
 
 	/**
 	 * @param role
 	 * @return
 	 */
 	private AssociationEnd getAssociationEnd(RoleInterface role, Classifier assocClass,
 			String assnEntityType)
 	{
 		int minCardinality = role.getMinimumCardinality().ordinal();
 		int maxCardinality = role.getMaximumCardinality().ordinal();
 		// primary end association
 		AssociationEnd associationEnd = umlPackage.getCore().getAssociationEnd()
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
 		if (versionOfxmi.equals(XMIConstants.XMI_VERSION_1_2))
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
 	private Collection<UmlClass> createUMLClasses(Collection<EntityInterface> entityCollection)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		ArrayList<UmlClass> umlEntityClasses = new ArrayList<UmlClass>();
 		if (entityCollection != null)
 		{
 			Iterator entityCollectionIter = entityCollection.iterator();
 			while (entityCollectionIter.hasNext())
 			{
 				EntityInterface entity = (EntityInterface) entityCollectionIter.next();
 				UmlClass umlEntityClass = createUMLClass(entity);
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
 	private UmlClass createUMLClass(EntityInterface entity)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		UmlClass umlEntityClass = null;
 		if (entity != null)
 		{
 			//Get class name , create uml class 
 			String className = XMIUtilities.getClassNameForEntity(entity);
 			CorePackage corePackage = umlPackage.getCore();
 			umlEntityClass = corePackage.getUmlClass().createUmlClass(className,
 					VisibilityKindEnum.VK_PUBLIC, false, false, false, entity.isAbstract(), false);
 			//Create and add attributes to class
 			Collection<Attribute> umlEntityAttributes;
 			if (XMIConstants.XMI_VERSION_1_2.equals(versionOfxmi))
 			{
 				umlEntityAttributes = createUMLAttributes(getAllAttributesForEntity(entity));
 			}
 			else
 			{
 				umlEntityAttributes = createUMLAttributes(entity.getEntityAttributes());
 			}
 			umlEntityClass.getFeature().addAll(umlEntityAttributes);
 			//Create and add tagged values to entity
 			Collection<TaggedValue> entityTaggedValues = getTaggedValues(entity);
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
 	private Collection<AttributeInterface> getAllAttributesForEntity(EntityInterface entity)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		//Collection of all attributes
 		Collection<AttributeInterface> attributesCollection = new ArrayList<AttributeInterface>();
 		Map<Long, AttributeInterface> attributes = new HashMap<Long, AttributeInterface>();
 		for (AttributeInterface attribute : entity.getEntityAttributes())
 		{
 			attributes.put(attribute.getId(), attribute);
 		}
 		//create and add collection-type-attribute to the entity
 		Collection<AssociationInterface> associationCollection = new ArrayList<AssociationInterface>();
 		associationCollection.addAll(entity.getAssociationCollection());
 		DomainObjectFactory domainObjectFactory = DomainObjectFactory.getInstance();
 		TaggedValueInterface tagValueForCollectionTypeAttr = domainObjectFactory
 				.createTaggedValue();
 		//		tagValueForCollectionTypeAttribute.setKey("CollectionTypeAttribute");
 		//		tagValueForCollectionTypeAttribute.setValue("true");
 		AttributeInterface collectionTypeAttribute = null;
 		for (AssociationInterface association : associationCollection)
 		{
 			if (association.getIsCollection())
 			{
 				Collection<AbstractAttributeInterface> attributeCollection = association
 						.getTargetEntity().getAllAbstractAttributes();
 				Collection<AbstractAttributeInterface> filteredAttributeCollection = EntityManagerUtil
 						.filterSystemAttributes(attributeCollection);
 				List<AbstractAttributeInterface> attributesList = new ArrayList<AbstractAttributeInterface>(
 						filteredAttributeCollection);
 				collectionTypeAttribute = (AttributeInterface) attributesList.get(0);
 				collectionTypeAttribute.setName(association.getName());
 				entity.addAttribute(collectionTypeAttribute);
 				attributes.put(collectionTypeAttribute.getId(), collectionTypeAttribute);
 				//				collectionTypeAttribute.addTaggedValue(tagValueForCollectionTypeAttribute);
 				EntityManagerInterface entityManager = EntityManager.getInstance();
 				ControlInterface control = entityManager
 						.getControlByAbstractAttributeIdentifier(association.getId());
 				if ((control != null) && (control instanceof ListBoxInterface))
 				{
 					ListBoxInterface listBox = (ListBoxInterface) control;
 					if (listBox.getIsMultiSelect() != null && listBox.getNoOfRows() != null
 							&& listBox.getIsMultiSelect().booleanValue())
 					{
 						tagValueForCollectionTypeAttr.setKey(XMIConstants.TAGGED_VALUE_MULTISELECT);
 						tagValueForCollectionTypeAttr.setValue(listBox.getNoOfRows().toString());
 						collectionTypeAttribute.addTaggedValue(tagValueForCollectionTypeAttr);
 						TaggedValueInterface tagValueForColnTypeAttributeTName = domainObjectFactory
 								.createTaggedValue();
 						tagValueForColnTypeAttributeTName
 								.setKey(XMIConstants.TAGGED_VALUE_MULTISELECT_TABLE_NAME);
 						String tableName = association.getTargetEntity().getTableProperties()
 								.getName();
 						tagValueForColnTypeAttributeTName.setValue(tableName);
 						collectionTypeAttribute.addTaggedValue(tagValueForColnTypeAttributeTName);
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
 			Collection<AttributeInterface> entityAttributes)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		ArrayList<Attribute> umlAttributes = new ArrayList<Attribute>();
 		if (entityAttributes != null)
 		{
 			Iterator entityAttributesIter = entityAttributes.iterator();
 			while (entityAttributesIter.hasNext())
 			{
 				AttributeInterface attribute = (AttributeInterface) entityAttributesIter.next();
 				if (attribute.getDataType() != EntityManagerConstantsInterface.FILE_ATTRIBUTE_TYPE)
 				{
 					Attribute umlAttribute = createUMLAttribute(attribute);
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
 	private Attribute createUMLAttribute(AttributeInterface entityAttribute)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		Attribute umlAttribute = null;
 		if (entityAttribute != null)
 		{
 			String attributeName = XMIUtilities.getAttributeName(entityAttribute);
 			Classifier typeClass = null;
 			CorePackage corePackage = umlPackage.getCore();
 			umlAttribute = corePackage.getAttribute().createAttribute(
 					attributeName,
 					VisibilityKindEnum.VK_PUBLIC,
 					false,
 					ScopeKindEnum.SK_INSTANCE,
 					createAttributeMultiplicity(corePackage.getDataTypes(), entityAttribute
 							.getIsNullable()), ChangeableKindEnum.CK_CHANGEABLE,
 					ScopeKindEnum.SK_CLASSIFIER, OrderingKindEnum.OK_UNORDERED, null);
 			typeClass = getOrCreateDataType(DatatypeMappings.get(entityAttribute.getDataType())
 					.getJavaClassMapping());
 			umlAttribute.setType(typeClass);
 			//Tagged Values
 			Collection<TaggedValue> attributeTaggedValues = getTaggedValues(entityAttribute);
 			umlAttribute.getTaggedValue().addAll(attributeTaggedValues);
 			umlAttribute.getTaggedValue()
 					.add(
 							createTaggedValue(XMIConstants.TAGGED_VALUE_TYPE, entityAttribute
 									.getDataType()));
 			umlAttribute.getTaggedValue().add(
					createTaggedValue(XMIConstants.TAGGED_VALUE_DESCRIPTION, entityAttribute.getName()));
 
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
 	private static Classifier getOrCreateDataType(String type)
 	{
 
 		Object datatype = XMIUtilities.find(umlModel, type);
 		if (datatype == null)
 		{
 			String[] names = StringUtils.split(type, '.');
 			if (names != null && names.length > 0)
 			{
 				// the last name is the type name
 				String typeName = names[names.length - 1];
 				names[names.length - 1] = null;
 				String packageName = StringUtils.join(names, XMIConstants.DOT_SEPARATOR);
 				org.omg.uml.modelmanagement.UmlPackage datatypesPackage = getOrCreatePackage(
 						packageName, logicalModel);
 				//Create Datatype 
 				if (datatypesPackage != null)
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
 				else
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
 			}
 		}
 		return (Classifier) datatype;
 	}
 
 	/**
 	 * @param dataTypes
 	 * @param b
 	 * @return
 	 */
 	private static Multiplicity createAttributeMultiplicity(DataTypesPackage dataTypes,
 			boolean required)
 	{
 		{
 			Multiplicity mult = null;
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
 	protected static Multiplicity createMultiplicity(DataTypesPackage dataTypes, int lower,
 			int upper)
 	{
 		Multiplicity mult = dataTypes.getMultiplicity().createMultiplicity();
 		MultiplicityRange range = dataTypes.getMultiplicityRange().createMultiplicityRange(lower,
 				upper);
 		mult.getRange().add(range);
 		return mult;
 	}
 
 	@SuppressWarnings("unchecked")
 	protected static Collection getOrCreateStereotypes(String names, String baseClass)
 	{
 		Collection<Stereotype> stereotypes = new HashSet<Stereotype>();
 		String[] stereotypeNames = null;
 		if (names != null)
 		{
 			stereotypeNames = names.split(",");
 		}
 		if (stereotypeNames != null && stereotypeNames.length > 0)
 		{
 			for (int ctr = 0; ctr < stereotypeNames.length; ctr++)
 			{
 				String name = StringUtils.trimToEmpty(stereotypeNames[ctr]);
 
 				// see if we can find the stereotype first
 				Stereotype stereotype = null;//ModelElementFinder.find(this.umlPackage, name);
 				if (stereotype == null || !Stereotype.class.isAssignableFrom(stereotype.getClass()))
 				{
 					Collection<String> baseClasses = new ArrayList<String>();
 					baseClasses.add(baseClass);
 					stereotype = umlPackage.getCore().getStereotype().createStereotype(name,
 							VisibilityKindEnum.VK_PUBLIC, false, false, false, false, null,
 							baseClasses);
 
 					umlModel.getOwnedElement().add(stereotype);
 				}
 				stereotypes.add(stereotype);
 			}
 		}
 		return stereotypes;
 	}
 
 	/**
 	 * 
 	 */
 	private org.omg.uml.modelmanagement.UmlPackage getLeafPackage(EntityGroupInterface entityGroup,
 			String modelPackageName)
 	{
 		String completePackageName = "";
 		Collection<TaggedValueInterface> tvColl = entityGroup.getTaggedValueCollection();
 		for (TaggedValueInterface tv : tvColl)
 		{
 			if (tv.getKey().equalsIgnoreCase(XMIConstants.TAGGED_NAME_PACKAGE_NAME))
 			{
 				completePackageName = tv.getValue();
 				packageName = completePackageName;
 			}
 		}
 		if (packageName == null && modelPackageName != null)
 		{
 			packageName = modelPackageName;
 		}
 
 		org.omg.uml.modelmanagement.UmlPackage leafPackage = getOrCreatePackage(packageName,
 				logicalModel);
 		return leafPackage;
 	}
 
 	/**
 	 * @param modelManagement
 	 * @param umlModel
 	 * @param string
 	 * @return
 	 */
 	@SuppressWarnings("unchecked")
 	private static org.omg.uml.modelmanagement.UmlPackage getOrCreatePackage(String packageName,
 			org.omg.uml.modelmanagement.UmlPackage parentPackage)
 	{
 		ModelManagementPackage modelManagement = umlPackage.getModelManagement();
 
 		Object newPackage = null;
 		packageName = StringUtils.trimToEmpty(packageName);
 		if (StringUtils.isNotEmpty(packageName))
 		{
 			StringTokenizer stringTokenizer = new StringTokenizer(packageName,
 					XMIConstants.DOT_SEPARATOR);
 			if (stringTokenizer != null)
 			{
 				while (stringTokenizer.hasMoreTokens())
 				{
 					String token = stringTokenizer.nextToken();
 					newPackage = XMIUtilities.find(parentPackage, token);
 
 					if (newPackage == null)
 					{
 						newPackage = modelManagement.getUmlPackage().createUmlPackage(token,
 								VisibilityKindEnum.VK_PUBLIC, false, false, false, false);
 						parentPackage.getOwnedElement().add(newPackage);
 					}
 					parentPackage = (org.omg.uml.modelmanagement.UmlPackage) newPackage;
 				}
 			}
 		}
 		return (org.omg.uml.modelmanagement.UmlPackage) newPackage;
 	}
 
 	/**
 	 * @throws CreationFailedException
 	 * @throws Exception
 	 */
 	public void init() throws CreationFailedException, Exception
 	{
 		//		Cleanup repository files
 		XMIUtilities.cleanUpRepository();
 		initializeUMLPackage();
 		initializeModel();
 		initializePackageHierarchy();
 		entityUMLClassMappings = new HashMap<String, UmlClass>();
 		entityDataClassMappings = new HashMap<String, UmlClass>();
 		foreignKeyOperationNameMappings = new HashMap<String, String>();
 	}
 
 	/**
 	 * 
 	 */
 	@SuppressWarnings("unchecked")
 	private void initializePackageHierarchy()
 	{
 		org.omg.uml.modelmanagement.UmlPackage logicalView = getOrCreatePackage(
 				XMIConstants.PACKAGE_NAME_LOGICAL_VIEW, umlModel);
 		logicalModel = getOrCreatePackage(XMIConstants.PACKAGE_NAME_LOGICAL_MODEL, logicalView);
 		dataModel = getOrCreatePackage(XMIConstants.PACKAGE_NAME_DATA_MODEL, logicalView);
 	}
 
 	/**
 	 * 
 	 */
 	private void initializeModel()
 	{
 		//Initialize model
 		ModelManagementPackage modelManagementPackage = umlPackage.getModelManagement();
 
 		//Create Logical Model
 		umlModel = modelManagementPackage.getModel().createModel(XMIConstants.MODEL_NAME,
 				VisibilityKindEnum.VK_PUBLIC, false, false, false, false);
 	}
 
 	/**
 	 * @throws Exception 
 	 * @throws CreationFailedException 
 	 * 
 	 */
 	private void initializeUMLPackage() throws CreationFailedException, Exception
 	{
 		//Get UML Package
 		umlPackage = (UmlPackage) repository.getExtent(XMIConstants.UML_INSTANCE);
 		if (umlPackage == null)
 		{
 			// UML extent does not exist -> create it (note that in case one want's to instantiate
 			// a metamodel other than MOF, they need to provide the second parameter of the createExtent
 			// method which indicates the metamodel package that should be instantiated)
 			umlPackage = (UmlPackage) repository.createExtent(XMIConstants.UML_INSTANCE,
 					getUmlPackage());
 		}
 	}
 
 	/** Finds "UML" package -> this is the topmost package of UML metamodel - that's the
 	 * package that needs to be instantiated in order to create a UML extent
 	 */
 	private static MofPackage getUmlPackage() throws Exception
 	{
 		// get the MOF extent containing definition of UML metamodel
 		ModelPackage umlMM = (ModelPackage) repository.getExtent(XMIConstants.UML_MM);
 		if (umlMM == null)
 		{
 			// it is not present -> create it
 			umlMM = (ModelPackage) repository.createExtent(XMIConstants.UML_MM);
 		}
 		// find package named "UML" in this extent
 		MofPackage result = getUmlPackage(umlMM);
 		if (result == null)
 		{
 			// it cannot be found -> UML metamodel is not loaded -> load it from XMI
 			XmiReader reader = (XmiReader) Lookup.getDefault().lookup(XmiReader.class);
 			reader.read(UmlPackage.class.getResource("resources/01-02-15_Diff.xml").toString(),
 					umlMM);
 			// try to find the "UML" package again
 			result = getUmlPackage(umlMM);
 		}
 		return result;
 	}
 
 	/** Finds "UML" package in a given extent
 	 * @param umlMM MOF extent that should be searched for "UML" package.
 	 */
 	private static MofPackage getUmlPackage(ModelPackage umlMM)
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
 	}
 
 	/**
 	 * @param core
 	 * @param umlPrimaryClass
 	 * @param umlDependentClass
 	 * @return
 	 */
 	@SuppressWarnings("unchecked")
 	private static Dependency createDependency(UmlClass umlPrimaryClass, UmlClass umlDependentClass)
 	{
 		CorePackage corePackage = umlPackage.getCore();
 		Dependency dependency = corePackage.getDependency().createDependency(null,
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
 
 	public static void main(String args[]) throws Exception
 	{
 		/*System.setOut(new PrintStream(new FileOutputStream(
 			    "deexport"+  ".out.log", 
 			    true)));
 		 */
 
 		//Variables.databaseName = "MYSQL";
 		generateXMIForIntegrationTables();
 		//test();		//For internal testing
 		//createSmokingHistory();
 
 		/*if(args.length<3)
 		{
 			throw new Exception("Please specify all parameters. '-Dgroupname <groupname> -Dfilename <export filename> -Dversion <version>'");
 		}
 		String groupName = args[0];
 		if(groupName==null)
 		{
 			throw new Exception("Please specify groupname to be exported");
 		}
 		else
 		{
 			String filename = args[1]; 
 			if(filename==null)
 			{
 				throw new Exception("Kindly specify the filename where XMI should be exported.");
 			}
 			else
 			{
 				String xmiVersion = args[2]; 
 				if(xmiVersion==null)
 				{
 					System.out.println("Export version not specified. Exporting as XMI 1.2");
 					xmiVersion = XMIConstants.XMI_VERSION_1_2;
 				}
 				XMIExporter xmiExporter = new XMIExporter();
 				EntityGroupInterface entityGroup = xmiExporter.getEntityGroup(groupName);
 				xmiExporter.addHookEntitiesToGroup(entityGroup);
 				//xmiExporter.getHookEntityUmlClass(entityGroup);
 				if(entityGroup==null)
 				{
 					throw new Exception("Specified group does not exist. Could not export to XMI");
 				}
 				else
 				{
 					xmiExporter.exportXMI(filename, entityGroup, xmiVersion);
 				}
 			}
 
 		}*/
 	}
 
 	//}
 
 	/**
 	 * 
 	 */
 	private static void generateXMIForIntegrationTables()
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
 	}
 
 	/**
 	 * @param associationType
 	 * @param name
 	 * @param minCard
 	 * @param maxCard
 	 * @return
 	 */
 	private static RoleInterface getRole(AssociationType associationType, String name,
 			Cardinality minCard, Cardinality maxCard)
 	{
 		RoleInterface role = DomainObjectFactory.getInstance().createRole();
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
 	public Operation createPrimaryKeyOperation(AttributeInterface attribute, Attribute umlAttribute)
 			throws DataTypeFactoryInitializationException
 	{
 		//Primary key operation name is generated considering the case that only one primary key will be present.
 		String primaryKeyOperationName = getPrimaryKeyOperationName(
 				attribute.getEntity().getName(), attribute.getName());
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
 	private Operation createPrimaryKeyOperation(String primaryKeyOperationName,
 			String primaryKeyDataType, Attribute umlAttribute)
 	{
 		Operation primaryKeyOperation = umlPackage.getCore().getOperation().createOperation(
 				primaryKeyOperationName, VisibilityKindEnum.VK_PUBLIC, false,
 				ScopeKindEnum.SK_INSTANCE, false, CallConcurrencyKindEnum.CCK_SEQUENTIAL, false,
 				false, false, null);
 		primaryKeyOperation.getStereotype().addAll(
 				getOrCreateStereotypes(XMIConstants.PRIMARY_KEY,
 						XMIConstants.STEREOTYPE_BASECLASS_ATTRIBUTE));
 		primaryKeyOperation.getTaggedValue().add(
 				createTaggedValue(XMIConstants.STEREOTYPE, XMIConstants.PRIMARY_KEY));
 
 		//Return parameter
 		Parameter returnParameter = createParameter(null, null,
 				ParameterDirectionKindEnum.PDK_RETURN);
 		Parameter primaryKeyParam = createParameter(umlAttribute.getName(), umlAttribute.getType(),
 				ParameterDirectionKindEnum.PDK_IN);
 
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
 	private String getPrimaryKeyOperationName(String entityName, String attributeName)
 	{
 		if ((entityName != null) && (attributeName != null))
 		{
 			return ("PK_" + entityName + "_" + attributeName);
 		}
 		return null;
 	}
 
 	/**
 	 * @param association
 	 * @return
 	 */
 	private String getAssociationType(AssociationInterface association)
 	{
 		if (association != null)
 		{
 			Cardinality sourceCardinality = association.getSourceRole().getMaximumCardinality();
 			Cardinality targetCardinality = association.getTargetRole().getMaximumCardinality();
 			if ((sourceCardinality.equals(Cardinality.ONE))
 					&& (targetCardinality.equals(Cardinality.ONE)))
 			{
 				return XMIConstants.ASSOC_ONE_ONE;
 			}
 			else if ((sourceCardinality.equals(Cardinality.ONE))
 					&& (targetCardinality.equals(Cardinality.MANY)))
 			{
 				return XMIConstants.ASSOC_ONE_MANY;
 			}
 			if ((sourceCardinality.equals(Cardinality.MANY))
 					&& (targetCardinality.equals(Cardinality.ONE)))
 			{
 				return XMIConstants.ASSOC_MANY_ONE;
 			}
 			if ((sourceCardinality.equals(Cardinality.MANY))
 					&& (targetCardinality.equals(Cardinality.MANY)))
 			{
 				return XMIConstants.ASSOC_MANY_MANY;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Get collection of entities
 	 * @param entityGroup
 	 * @return
 	 */
 	private Collection<EntityInterface> getEntities(EntityGroupInterface entityGroup)
 	{
 		List<EntityInterface> entityList = new ArrayList<EntityInterface>();
 		Map<Long, EntityInterface> entityMap = new HashMap<Long, EntityInterface>();
 
 		Collection<EntityInterface> entityCollection = entityGroup.getEntityCollection();
 		Collection<EntityInterface> multiSelectentityCollection = new ArrayList<EntityInterface>();
 		for (EntityInterface entity : entityCollection)
 		{
 			Collection<AssociationInterface> associationCollection = new ArrayList<AssociationInterface>();
 			associationCollection.addAll(entity.getAllAssociations());
 			//if entity is not associated with any entity then also put it inside entity map
 			if (associationCollection.size() == 0)
 			{
 				entityMap.put(entity.getId(), entity);
 			}
 
 			for (AssociationInterface association : associationCollection)
 			{
 				if (association.getIsCollection())
 				{
 					multiSelectentityCollection.add(association.getTargetEntity());
 				}
 				//Add entity only if its association is not of multiselect type ,and both target and src entity's group is same
 				if (!association.getIsCollection()
 						&& (association.getEntity().getEntityGroup().getName()
 								.equalsIgnoreCase(association.getTargetEntity().getEntityGroup()
 										.getName())))
 				{
 
 					entityMap.put(entity.getId(), entity);
 					entityMap.put(association.getTargetEntity().getId(), association
 							.getTargetEntity());
 				}
 			}
 		}
 		//this removes the multi select entities from entity list
 		for (EntityInterface msEntity : multiSelectentityCollection)
 		{
 			if (entityMap.containsKey(msEntity.getId()))
 			{
 				entityMap.remove(msEntity.getId());
 			}
 		}
 		entityList.addAll(entityMap.values());
 		return entityList;
 	}
 }
