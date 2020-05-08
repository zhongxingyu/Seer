 
 package edu.common.dynamicextensions.entitymanager;
 
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Stack;
 
 import org.hibernate.HibernateException;
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 
 import edu.common.dynamicextensions.domain.AbstractAttribute;
 import edu.common.dynamicextensions.domain.Association;
 import edu.common.dynamicextensions.domain.Attribute;
 import edu.common.dynamicextensions.domain.BooleanAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.CategoryAssociation;
 import edu.common.dynamicextensions.domain.CategoryAttribute;
 import edu.common.dynamicextensions.domain.CategoryEntity;
 import edu.common.dynamicextensions.domain.DateAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.DomainObjectFactory;
 import edu.common.dynamicextensions.domain.DoubleAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.Entity;
 import edu.common.dynamicextensions.domain.FileAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.FloatAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.IntegerAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.LongAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.NumericAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.ObjectAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.ShortAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.StringAttributeTypeInformation;
 import edu.common.dynamicextensions.domaininterface.AbstractAttributeInterface;
 import edu.common.dynamicextensions.domaininterface.AbstractEntityInterface;
 import edu.common.dynamicextensions.domaininterface.AssociationInterface;
 import edu.common.dynamicextensions.domaininterface.AttributeInterface;
 import edu.common.dynamicextensions.domaininterface.AttributeMetadataInterface;
 import edu.common.dynamicextensions.domaininterface.AttributeTypeInformationInterface;
 import edu.common.dynamicextensions.domaininterface.BaseAbstractAttributeInterface;
 import edu.common.dynamicextensions.domaininterface.CategoryAssociationInterface;
 import edu.common.dynamicextensions.domaininterface.CategoryAttributeInterface;
 import edu.common.dynamicextensions.domaininterface.CategoryEntityInterface;
 import edu.common.dynamicextensions.domaininterface.EntityInterface;
 import edu.common.dynamicextensions.domaininterface.RoleInterface;
 import edu.common.dynamicextensions.domaininterface.databaseproperties.ConstraintPropertiesInterface;
 import edu.common.dynamicextensions.exception.DataTypeFactoryInitializationException;
 import edu.common.dynamicextensions.exception.DynamicExtensionsApplicationException;
 import edu.common.dynamicextensions.exception.DynamicExtensionsSystemException;
 import edu.common.dynamicextensions.processor.ProcessorConstants;
 import edu.common.dynamicextensions.util.DynamicExtensionsUtility;
 import edu.common.dynamicextensions.util.global.Constants;
 import edu.common.dynamicextensions.util.global.Variables;
 import edu.common.dynamicextensions.util.global.Constants.AssociationType;
 import edu.common.dynamicextensions.util.global.Constants.Cardinality;
 import edu.wustl.common.exception.BizLogicException;
 import edu.wustl.common.util.Utility;
 import edu.wustl.common.util.dbManager.DBUtil;
 import edu.wustl.common.util.logger.Logger;
 
 /**
  * This class provides the methods that builds the queries that are required for
  * creation and updating of the tables of the entities.These queries are as per SQL-99 standard.
  * Theses methods can be over-ridden  in the database specific query builder class to
  * provide any database-specific implementation.
  *
  * @author rahul_ner
  */
 class DynamicExtensionBaseQueryBuilder
 		implements
 			EntityManagerConstantsInterface,
 			EntityManagerExceptionConstantsInterface,
 			DynamicExtensionsQueryBuilderConstantsInterface
 {
 
 	EntityManagerUtil entityManagerUtil = new EntityManagerUtil();
 
 	/**
 
 	 *
 	 * @param entity 
 	 * @param revQueries
 
 	/**
 	 * This method builds the list of all the queries that need to be executed in order to
 	 * create the data table for the entity and its associations.
 	 * @param entity entity for which the queries are formed.
 	 * @param revQueries for every data table query this method creates a reverse query.
 	 * @param hibernateDAO
 	 * @return List<String> list of queries
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	public List<String> getCreateEntityQueryList(Entity entity, List<String> revQueries)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		List<String> queries = new ArrayList<String>();
 
 		// Get query to create main table with primitive attributes.
 		queries.addAll(getCreateMainTableQuery(entity, revQueries));
 
 		return queries;
 	}
 
 	/**
 	 * This method builds the list of all the queries that need to be executed in order to
 	 * create the data table for the entity and its associations.
 	 * @param entity entity for which the queries are formed.
 	 * @param revQueries for every data table query this method creates a reverse query.
 	 * @param hibernateDAO
 	 * @return List<String> list of queries
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	public List<String> getUpdateEntityQueryList(Entity entity, List<String> revQueries)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		List<String> queries = new ArrayList<String>();
 
 		queries.addAll(getForeignKeyConstraintQuery(entity, revQueries));
 
 		// Get query to create associations, it involves altering source/target 
 		// table or creating middle table depending upon the cardinalities.
 		queries.addAll(getCreateAssociationsQueryList(entity, revQueries));
 
 		return queries;
 	}
 
 	/**
 	 * This method builds the list of all the queries that need to be executed in order to
 	 * create the data table for the entity and its associations.
 	 * @param catEntity category entity for which the queries are formed.
 	 * @param revQueries for every data table query this method creates a reverse query.
 	 * @return List<String> list of queries
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	public List<String> getUpdateCategoryEntityQueryList(CategoryEntityInterface catEntity,
 			List<String> revQueries) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		List<String> queries = new ArrayList<String>();
 
 		// Get queries for foreign key constraint for inheritance and to create associations. 
 		// It involves altering source/target table or creating middle table depending upon the cardinalities.
 		queries.addAll(getForeignKeyConstraintQuery(catEntity, revQueries));
 		queries.addAll(getCreateAssociationsQueryList(catEntity, revQueries));
 
 		return queries;
 	}
 
 	/**
 	 * This method is used to execute the data table queries for entity in case of editing the entity.
 	 * This method takes each attribute of the entity and then scans for any changes and builds the 
 	 * alter query for each attribute for the entity.
 	 * @param entity entity for which the queries are formed.
 	 * @param dbaseCopy database copy of the entity.
 	 * @param attrRlbkQries roll back queries list.
 	 * @return List<String> list of queries.
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	public List<String> getUpdateEntityQueryList(Entity entity, Entity dbaseCopy,
 			List<String> attrRlbkQries) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		Logger.out.debug("getUpdateEntityQueryList : Entering method");
 		List<String> updateQries = new ArrayList<String>();
 
 		List<String> entInheQries = getInheritanceQueryList(entity, dbaseCopy, attrRlbkQries);
 		// Get the query for any attribute that is modified.
 		List<String> updAttrQries = getUpdateAttributeQueryList(entity, dbaseCopy, attrRlbkQries);
 
 		// Get the query for any association that is modified.
 		List<String> updAssoQries = getUpdateAssociationsQueryList(entity, dbaseCopy, attrRlbkQries);
 
 		updateQries.addAll(entInheQries);
 		updateQries.addAll(updAttrQries);
 		updateQries.addAll(updAssoQries);
 
 		Logger.out.debug("getUpdateEntityQueryList Exiting method");
 
 		return updateQries;
 	}
 
 	/**
 	 * This method is used to execute the data table queries for entity in case of editing the entity.
 	 * This method takes each attribute of the entity and then scans for any changes and builds the 
 	 * alter query for each attribute for the entity.
 	 * @param catEntity category entity for which the queries are formed.
 	 * @param dbaseCopy database copy of the entity.
 	 * @param attrRlbkQries roll back queries list.
 	 * @return List<String> list of queries.
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	public List<String> getUpdateEntityQueryList(CategoryEntity catEntity,
 			CategoryEntity dbaseCopy, List<String> attrRlbkQries)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		Logger.out.debug("getUpdateEntityQueryList : Entering method");
 		List<String> updateQries = new ArrayList<String>();
 
 		List<String> entInheQries = getInheritanceQueryList(catEntity, dbaseCopy, attrRlbkQries);
 
 		// Get the query for any attribute that is modified.
 		List<String> updAttrQries = getUpdateAttributeQueryList(catEntity, dbaseCopy, attrRlbkQries);
 
 		// Get the query for any association that is modified.
 		List<String> updAssoQries = getUpdateAssociationsQueryList(catEntity, dbaseCopy,
 				attrRlbkQries);
 
 		updateQries.addAll(entInheQries);
 		updateQries.addAll(updAttrQries);
 		updateQries.addAll(updAssoQries);
 
 		Logger.out.debug("getUpdateEntityQueryList Exiting method");
 
 		return updateQries;
 	}
 
 	/**
 	 *
 	 * @param entity
 	 * @param dbaseCopy
 	 * @param attrRlbkQries
 	 * @return
 	 */
 	private List<String> getInheritanceQueryList(Entity entity, Entity dbaseCopy,
 			List<String> attrRlbkQries)
 	{
 		List<String> queries = new ArrayList<String>();
 		if (entity.getTableProperties() != null)
 		{
 			if (isParentChanged(entity, dbaseCopy))
 			{
 				String frnCnstrRlbkQry = "";
 				if (dbaseCopy.getParentEntity() != null)
 				{
 					String frnCnstrRemQry = QueryBuilderFactory.getQueryBuilder()
 							.getForeignKeyRemoveConstraintQueryForInheritance(dbaseCopy,
 									dbaseCopy.getParentEntity());
 					frnCnstrRlbkQry = getForeignKeyConstraintQueryForInheritance(dbaseCopy);
 					queries.add(frnCnstrRemQry);
 					attrRlbkQries.add(frnCnstrRlbkQry);
 				}
 
 				if (entity.getParentEntity() != null)
 				{
 					String frnCnstrAddQry = getForeignKeyConstraintQueryForInheritance(entity);
 
 					frnCnstrRlbkQry = getForeignKeyRemoveConstraintQueryForInheritance(entity,
 							entity.getParentEntity());
 
 					queries.add(frnCnstrAddQry);
 					attrRlbkQries.add(frnCnstrRlbkQry);
 				}
 			}
 		}
 
 		return queries;
 	}
 
 	/**
 	 *
 	 * @param catEntity
 	 * @param dbaseCopy
 	 * @param attrRlbkQries
 	 * @return
 	 */
 	private List<String> getInheritanceQueryList(CategoryEntity catEntity,
 			CategoryEntity dbaseCopy, List<String> attrRlbkQries)
 	{
 		List<String> queries = new ArrayList<String>();
 		if (catEntity.getTableProperties() != null)
 		{
 			if (isParentChanged(catEntity, dbaseCopy))
 			{
 				String frnCnstrRlbkQry = "";
 				if (dbaseCopy.getParentCategoryEntity() != null)
 				{
 					String frnCnstrRemQry = getForeignKeyRemoveConstraintQueryForInheritance(
 							dbaseCopy, dbaseCopy.getParentCategoryEntity());
 					frnCnstrRlbkQry = getForeignKeyConstraintQueryForInheritance(dbaseCopy,
 							dbaseCopy.getParentCategoryEntity());
 					queries.add(frnCnstrRemQry);
 					attrRlbkQries.add(frnCnstrRlbkQry);
 				}
 
 				if (catEntity.getParentCategoryEntity() != null)
 				{
 					String frnCnstrntAdQry = getForeignKeyConstraintQueryForInheritance(dbaseCopy,
 							dbaseCopy.getParentCategoryEntity());
 
 					frnCnstrRlbkQry = getForeignKeyRemoveConstraintQueryForInheritance(catEntity,
 							catEntity.getParentCategoryEntity());
 
 					queries.add(frnCnstrntAdQry);
 					attrRlbkQries.add(frnCnstrRlbkQry);
 				}
 			}
 		}
 
 		return queries;
 	}
 
 	/**
 	 * This method returns association values for the entity's given record.
 	 * e.g if user1 is associated with study1 and study2, this method returns the
 	 * list of record identifiers of study1 and study2 as the return value for the 
 	 * association between user and study.
 	 * @param entity
 	 * @param recordId
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	public Map<Association, List<?>> getAssociationGetRecordQueryList(EntityInterface entity,
 			Long recordId) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		Collection<AssociationInterface> associations = entity.getAssociationCollection();
 		Iterator<AssociationInterface> assocIter = associations.iterator();
 		StringBuffer mnyToOneAssQry = new StringBuffer();
 		mnyToOneAssQry.append(SELECT_KEYWORD + WHITESPACE);
 		List<Association> manyToOneAssocns = new ArrayList<Association>();
 
 		Map<Association, List<?>> assocValues = new HashMap<Association, List<?>>();
 
 		while (assocIter.hasNext())
 		{
 			Association association = (Association) assocIter.next();
 
 			String tableName = DynamicExtensionsUtility.getTableName(association);
 			String sourceKey = association.getConstraintProperties().getSourceEntityKey();
 			String targetKey = association.getConstraintProperties().getTargetEntityKey();
 			StringBuffer query = new StringBuffer();
 
 			RoleInterface sourceRole = association.getSourceRole();
 			RoleInterface targetRole = association.getTargetRole();
 			Cardinality srcMaxCard = sourceRole.getMaximumCardinality();
 			Cardinality tgtMaxCard = targetRole.getMaximumCardinality();
 			if (srcMaxCard == Cardinality.MANY && tgtMaxCard == Cardinality.MANY)
 			{
 				// For many to many, get values from the middle table.
 				query.append(SELECT_KEYWORD + WHITESPACE + targetKey);
 				query.append(WHITESPACE + FROM_KEYWORD + WHITESPACE + tableName + WHITESPACE);
 				query
 						.append(WHITESPACE + WHERE_KEYWORD + WHITESPACE + sourceKey + EQUAL
 								+ recordId);
 				assocValues.put(association, getAssociationRecordValues(query.toString()));
 			}
 			else if (srcMaxCard == Cardinality.MANY && tgtMaxCard == Cardinality.ONE)
 			{
 				// For all many to one associations of a single entity,  
 				// create a single query to get values for the target records.
 				if (manyToOneAssocns.size() != 0)
 				{
 					mnyToOneAssQry.append(COMMA);
 				}
 				mnyToOneAssQry.append(WHITESPACE + sourceKey + WHITESPACE);
 				manyToOneAssocns.add(association);
 			}
 			else
 			{
 				// For one to many or one to one association, get target record values 
 				// from the target entity table.
 				query.append(SELECT_KEYWORD + WHITESPACE + IDENTIFIER);
 				query.append(WHITESPACE + FROM_KEYWORD + WHITESPACE + tableName + WHITESPACE);
 				query
 						.append(WHITESPACE + WHERE_KEYWORD + WHITESPACE + targetKey + EQUAL
 								+ recordId);
 
 				List<Long> recordIds = getAssociationRecordValues(query.toString());
 
 				if (association.getSourceRole().getAssociationsType().equals(
 						AssociationType.CONTAINTMENT)
 						|| (association.getSourceRole().getAssociationsType().equals(
 								AssociationType.ASSOCIATION) && association.getIsCollection()))
 				{
 					List<Map<AbstractAttributeInterface, Object>> cntnmntRecords = new ArrayList<Map<AbstractAttributeInterface, Object>>();
 
 					for (Long cntnmntRecId : recordIds)
 					{
 						Map<AbstractAttributeInterface, Object> recordMap = EntityManager
 								.getInstance().getRecordById(association.getTargetEntity(),
 										cntnmntRecId);
 						cntnmntRecords.add(recordMap);
 					}
 					assocValues.put(association, cntnmntRecords);
 				}
 				else
 				{
 					assocValues.put(association, recordIds);
 				}
 			}
 		}
 
 		mnyToOneAssQry.append(WHITESPACE + FROM_KEYWORD + WHITESPACE
 				+ entity.getTableProperties().getName() + WHITESPACE);
 		mnyToOneAssQry.append(WHITESPACE + WHERE_KEYWORD + WHITESPACE + IDENTIFIER + EQUAL
 				+ recordId);
 
 		int noOfMany2OneAsso = manyToOneAssocns.size();
 		if (noOfMany2OneAsso != 0)
 		{
 			Statement statement = null;
 			ResultSet resultSet = null;
 			try
 			{
 				Connection conn = DBUtil.getConnection();
 				statement = conn.createStatement();
 				resultSet = statement.executeQuery(mnyToOneAssQry.toString());
 
 				resultSet.next();
 				for (int i = 0; i < noOfMany2OneAsso; i++)
 				{
 					Long tgtRecId = resultSet.getLong(i + 1);
 					List<Long> values = new ArrayList<Long>();
 					values.add(tgtRecId);
 					assocValues.put(manyToOneAssocns.get(i), values);
 				}
 			}
 			catch (SQLException e)
 			{
 				throw new DynamicExtensionsSystemException("Exception in query execution", e);
 			}
 			finally
 			{
 				try
 				{
 					resultSet.close();
 					statement.close();
 				}
 				catch (SQLException e)
 				{
 					throw new DynamicExtensionsApplicationException(e.getMessage());
 				}
 			}
 		}
 
 		return assocValues;
 	}
 
 	/**
 	 * This method returns association values for the entity's given record.
 	 * e.g if user1 is associated with study1 and study2, this method returns the
 	 * list of record identifiers of study1 and study2 as the return value for the 
 	 * association between user and study.
 	 * @param associations
 	 * @param entRecResult
 	 * @param entRecord
 	 * @param recordId
 	 * @throws DynamicExtensionsSystemException
 	 * @throws IOException
 	 * @throws ClassNotFoundException
 	 */
 	public void putAssociationValues(List<AssociationInterface> associations,
 			EntityRecordResultInterface entRecResult, EntityRecordInterface entRecord, Long recordId)
 			throws DynamicExtensionsSystemException, IOException, ClassNotFoundException
 	{
 		Iterator<AssociationInterface> assocIter = associations.iterator();
 		StringBuffer mnyToOneAssQry = new StringBuffer();
 		mnyToOneAssQry.append(SELECT_KEYWORD + WHITESPACE);
 		List<Association> manyToOneAssocns = new ArrayList<Association>();
 
 		while (assocIter.hasNext())
 		{
 			Association association = (Association) assocIter.next();
 
 			int index = entRecResult.getEntityRecordMetadata().getAttributeList().indexOf(
 					association);
 
 			String tableName = DynamicExtensionsUtility.getTableName(association);
 			String sourceKey = association.getConstraintProperties().getSourceEntityKey();
 			String targetKey = association.getConstraintProperties().getTargetEntityKey();
 			StringBuffer query = new StringBuffer();
 
 			RoleInterface sourceRole = association.getSourceRole();
 			RoleInterface targetRole = association.getTargetRole();
 			Cardinality srcMaxCard = sourceRole.getMaximumCardinality();
 			Cardinality tgtMaxCard = targetRole.getMaximumCardinality();
 			if (srcMaxCard == Cardinality.MANY && tgtMaxCard == Cardinality.MANY)
 			{
 				// For many to many, get values from the middle table.
 				query.append(SELECT_KEYWORD + WHITESPACE + targetKey);
 				query.append(WHITESPACE + FROM_KEYWORD + WHITESPACE + tableName + WHITESPACE);
 				query
 						.append(WHITESPACE + WHERE_KEYWORD + WHITESPACE + sourceKey + EQUAL
 								+ recordId);
 
 				List<Long> manyToManyRecIds = getAssociationRecordValues(query.toString());
 				entRecord.getRecordValueList().set(index, manyToManyRecIds);
 			}
 			else if (srcMaxCard == Cardinality.MANY && tgtMaxCard == Cardinality.ONE)
 			{
 				// For all many to one associations of a single entity, create a single query 
 				// to get values for the target records.
 				if (manyToOneAssocns.size() != 0)
 				{
 					mnyToOneAssQry.append(COMMA);
 				}
 				mnyToOneAssQry.append(WHITESPACE + sourceKey + WHITESPACE);
 				manyToOneAssocns.add(association);
 			}
 			else
 			{
 				// For one to many or one to one associations, get target record values
 				// from the target entity table.
 				query.append(SELECT_KEYWORD + WHITESPACE + IDENTIFIER);
 				query.append(WHITESPACE + FROM_KEYWORD + WHITESPACE + tableName + WHITESPACE);
 				query
 						.append(WHITESPACE + WHERE_KEYWORD + WHITESPACE + targetKey + EQUAL
 								+ recordId);
 				query.append(" and " + getRemoveDisbledRecordsQuery(""));
 
 				List<Long> recordIds = getAssociationRecordValues(query.toString());
 
 				if (association.getSourceRole().getAssociationsType().equals(
 						AssociationType.CONTAINTMENT))
 				{
 					List<AbstractAttributeInterface> tgtAttributes = new ArrayList<AbstractAttributeInterface>(
 							association.getTargetEntity().getAbstractAttributeCollection());
 					EntityRecordResultInterface cntnmntEntRec = EntityManager.getInstance()
 							.getEntityRecords(association.getTargetEntity(), tgtAttributes,
 									recordIds);
 					entRecord.getRecordValueList().set(index, cntnmntEntRec);
 				}
 				else
 				{
 					entRecord.getRecordValueList().set(index, recordIds);
 				}
 			}
 		}
 
 		if (manyToOneAssocns.size() != 0)
 		{
 			String srcEntName = manyToOneAssocns.get(0).getEntity().getTableProperties().getName();
 			mnyToOneAssQry.append(WHITESPACE + FROM_KEYWORD + WHITESPACE + srcEntName + WHITESPACE);
 			mnyToOneAssQry.append(WHITESPACE + WHERE_KEYWORD + WHITESPACE + IDENTIFIER + EQUAL
 					+ recordId);
 
 			ResultSet resultSet = null;
 			try
 			{
 				resultSet = EntityManagerUtil.executeQuery(mnyToOneAssQry.toString());
 				resultSet.next();
 				for (int i = 0; i < manyToOneAssocns.size(); i++)
 				{
 					Long tgtRecId = resultSet.getLong(i + 1);
 					List<Long> values = new ArrayList<Long>();
 					values.add(tgtRecId);
 					AssociationInterface association = manyToOneAssocns.get(i);
 					int index = entRecResult.getEntityRecordMetadata().getAttributeList().indexOf(
 							association);
 					entRecord.getRecordValueList().set(index, values);
 				}
 			}
 			catch (SQLException e)
 			{
 				throw new DynamicExtensionsSystemException("Exception in query execution", e);
 			}
 			finally
 			{
 				if (resultSet != null)
 				{
 					try
 					{
 						resultSet.close();
 					}
 					catch (SQLException e)
 					{
 						throw new DynamicExtensionsSystemException(e.getMessage(), e);
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * This method returns the queries to insert data for the association.
 	 * @param asso
 	 * @param recIds
 	 * @param srcRecId
 	 * @return List<String> list of queries.
 	 * @throws DynamicExtensionsApplicationException
 	 * @throws DynamicExtensionsSystemException
 	 */
 	public List<String> getAssociationInsertDataQuery(AssociationInterface asso, List<Long> recIds,
 			Long srcRecId) throws DynamicExtensionsApplicationException,
 			DynamicExtensionsSystemException
 	{
 		List<String> queries = new ArrayList<String>();
 
 		if (recIds.isEmpty())
 		{
 			return queries;
 		}
 
 		Association association = (Association) asso;
 		if (association.getSourceRole().getAssociationsType().equals(AssociationType.CONTAINTMENT))
 		{
 			verifyCardinalityConstraints(asso, srcRecId);
 		}
 
 		String sourceKey = association.getConstraintProperties().getSourceEntityKey();
 		String targetKey = association.getConstraintProperties().getTargetEntityKey();
 
 		StringBuffer query = new StringBuffer();
 		RoleInterface srcRole = association.getSourceRole();
 		RoleInterface tgtRole = association.getTargetRole();
 
 		Cardinality srcMaxCard = srcRole.getMaximumCardinality();
 		Cardinality tgtMaxCard = tgtRole.getMaximumCardinality();
 
 		if (srcMaxCard == Cardinality.MANY && tgtMaxCard == Cardinality.MANY)
 		{
 			Long identifier;
 			// For many to many, insert into middle table.
 			for (int i = 0; i < recIds.size(); i++)
 			{
 				identifier = entityManagerUtil.getNextIdentifier(asso.getConstraintProperties()
 						.getName());
 				query = new StringBuffer();
 				query.append("INSERT INTO " + association.getConstraintProperties().getName()
 						+ " ( ");
 				query.append(IDENTIFIER + "," + sourceKey + "," + targetKey);
 				query.append(" ) VALUES ( ");
 				query.append(identifier.toString());
 				query.append(COMMA);
 				query.append(srcRecId.toString());
 				query.append(COMMA);
 				query.append(recIds.get(i));
 				query.append(CLOSING_BRACKET);
 				queries.add(query.toString());
 			}
 
 		}
 		else if (srcMaxCard == Cardinality.MANY && tgtMaxCard == Cardinality.ONE)
 		{
 			// For many to one, update source entity table.
 			query.append(UPDATE_KEYWORD);
 			query.append(WHITESPACE + association.getEntity().getTableProperties().getName());
 			query.append(WHITESPACE + SET_KEYWORD + WHITESPACE + sourceKey + EQUAL + recIds.get(0)
 					+ WHITESPACE);
 			query.append(WHERE_KEYWORD + WHITESPACE + IDENTIFIER + EQUAL + srcRecId);
 			queries.add(query.toString());
 
 		}
 		else
 		{
 			// For one to one & one to many, update target entity table.
 			String recordId = recIds.toString();
 			recordId = recordId.replace("[", OPENING_BRACKET);
 			recordId = recordId.replace("]", CLOSING_BRACKET);
 
 			query.append(UPDATE_KEYWORD);
 			query.append(WHITESPACE + asso.getTargetEntity().getTableProperties().getName());
 			query.append(WHITESPACE + SET_KEYWORD + WHITESPACE + targetKey + EQUAL + srcRecId
 					+ WHITESPACE);
 			query.append(WHERE_KEYWORD + WHITESPACE + IDENTIFIER + WHITESPACE + IN_KEYWORD
 					+ WHITESPACE + recordId);
 			queries.add(query.toString());
 		}
 
 		return queries;
 	}
 
 	/**
 	 * This method returns the queries to insert data for the association.
 	 * @param catAsso
 	 * @param recordIds
 	 * @param srcRecId
 	 * @return List<String> list of queries.
 	 * @throws DynamicExtensionsApplicationException
 	 * @throws DynamicExtensionsSystemException
 	 */
 	public List<String> getAssociationInsertDataQuery(CategoryAssociationInterface catAsso,
 			List<Long> recordIds, Long srcRecId) throws DynamicExtensionsApplicationException,
 			DynamicExtensionsSystemException
 	{
 		List<String> queries = new ArrayList<String>();
 
 		if (recordIds.isEmpty())
 		{
 			return queries;
 		}
 
 		CategoryAssociation catAssocn = (CategoryAssociation) catAsso;
 		verifyCardinalityConstraints(catAssocn, srcRecId);
 		String targetKey = catAssocn.getConstraintProperties().getTargetEntityKey();
 		StringBuffer query = new StringBuffer();
 
 		// For one to one & one to many, update target entity table.
 		String recordId = recordIds.toString();
 		recordId = recordId.replace("[", OPENING_BRACKET);
 		recordId = recordId.replace("]", CLOSING_BRACKET);
 
 		query.append(UPDATE_KEYWORD);
 		query.append(WHITESPACE
 				+ catAssocn.getTargetCategoryEntity().getTableProperties().getName());
 		query.append(WHITESPACE + SET_KEYWORD + WHITESPACE + targetKey + EQUAL + srcRecId
 				+ WHITESPACE);
 		query.append(WHERE_KEYWORD + WHITESPACE + IDENTIFIER + WHITESPACE + IN_KEYWORD + WHITESPACE
 				+ recordId);
 		queries.add(query.toString());
 
 		return queries;
 	}
 
 	/**
 	 * This method creates the queries to remove records for the containment association.
 	 * @param association
 	 * @param recordIds
 	 * @param queries
 	 * @param isLogicalDeletion
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	public void getContenmentAssociationRemoveDataQueryList(AssociationInterface association,
 			List<Long> recordIds, List<String> queries, boolean isLogicalDeletion)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		if (recordIds == null || recordIds.isEmpty())
 		{
 			return;
 		}
 
 		List<Long> chldrnRecIds = getRecordIdListForContainment(association, recordIds);
 		if (chldrnRecIds == null || chldrnRecIds.isEmpty())
 		{
 			return;
 		}
 
 		EntityInterface tgtEntity = association.getTargetEntity();
 
 		// Check if these records are referred to by some other incoming association, if so 
 		// then this should not be disabled.
 		Collection<AssociationInterface> associations = tgtEntity.getAssociationCollection();
 		for (AssociationInterface tgtEntAsso : associations)
 		{
 			if (tgtEntAsso.getSourceRole().getAssociationsType().equals(
					AssociationType.CONTAINTMENT))
 			{
 				getContenmentAssociationRemoveDataQueryList(tgtEntAsso, chldrnRecIds, queries,
 						isLogicalDeletion);
 			}
 		}
 
 		String tableName = DynamicExtensionsUtility.getTableName(association);
 
 		StringBuffer query = new StringBuffer();
 		if (isLogicalDeletion)
 		{
 			query.append(UPDATE_KEYWORD);
 			query.append(WHITESPACE + tableName + WHITESPACE);
 			query.append(SET_KEYWORD + Constants.ACTIVITY_STATUS_COLUMN + EQUAL + "'"
 					+ Constants.ACTIVITY_STATUS_DISABLED + "'");
 			query.append(WHERE_KEYWORD + WHITESPACE + IDENTIFIER + WHITESPACE + IN_KEYWORD);
 			query.append(WHITESPACE + getListToString(chldrnRecIds) + WHITESPACE);
 
 		}
 		else
 		{
 			query.append(DELETE_KEYWORD);
 			query.append(WHITESPACE + tableName + WHITESPACE);
 			query.append(WHERE_KEYWORD + WHITESPACE + IDENTIFIER + WHITESPACE + IN_KEYWORD);
 			query.append(WHITESPACE + getListToString(chldrnRecIds) + WHITESPACE);
 		}
 
 		queries.add(query.toString());
 	}
 
 	/**
 	 * @param entity
 	 * @param recordId
 	 * @param incomingAsso
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	public void validateForDeleteRecord(EntityInterface entity, Long recordId,
 			Collection<AssociationInterface> incomingAsso) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		List<Long> recordIds = new ArrayList<Long>();
 		recordIds.add(recordId);
 		validateForDeleteRecord(entity, recordIds, incomingAsso);
 	}
 
 	/**
 	 * This method checks if the record id of given entity is 
 	 * referred to by some other entity in some association.
 	 * @param entity
 	 * @param recordIds
 	 * @param incomingAsso
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	public void validateForDeleteRecord(EntityInterface entity, List<Long> recordIds,
 			Collection<AssociationInterface> incomingAsso) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		if (incomingAsso == null)
 		{
 			incomingAsso = EntityManager.getInstance().getIncomingAssociations(entity);
 		}
 
 		String tableName = "";
 		String sourceKey = "";
 		String targetKey = "";
 
 		for (AssociationInterface association : incomingAsso)
 		{
 			tableName = DynamicExtensionsUtility.getTableName(association);
 			sourceKey = association.getConstraintProperties().getSourceEntityKey();
 			targetKey = association.getConstraintProperties().getTargetEntityKey();
 
 			StringBuffer query = new StringBuffer();
 			query.append(SELECT_KEYWORD + COUNT_KEYWORD + "(*)");
 			query.append(FROM_KEYWORD + tableName);
 
 			RoleInterface sourceRole = association.getSourceRole();
 			RoleInterface targetRole = association.getTargetRole();
 
 			Cardinality srcMaxCard = sourceRole.getMaximumCardinality();
 			Cardinality tgtMaxCard = targetRole.getMaximumCardinality();
 
 			// Commented query part checking for Disabled records. Since delete record functionality is removed, 
 			// we no longer need to check records 'Disabled'.
 			// Moreover the removed query part was not executed on Oracle because of syntax error.
 			if (srcMaxCard == Cardinality.MANY && tgtMaxCard == Cardinality.MANY)
 			{
 				// For many to many check into middle table.
 				String srcTable = association.getEntity().getTableProperties().getName();
 				query.append(" AS m_table join " + srcTable);
 				query.append(" AS s_table on m_table." + sourceKey + "= s_table." + IDENTIFIER);
 				query.append(WHERE_KEYWORD + targetKey + WHITESPACE + IN_KEYWORD + WHITESPACE
 						+ getListToString(recordIds));
 				// query.append(" and " + DynamicExtensionBaseQueryBuilder.getRemoveDisbledRecordsQuery("s_table"));
 			}
 			else if (srcMaxCard == Cardinality.MANY && tgtMaxCard == Cardinality.ONE)
 			{
 				query.append(WHERE_KEYWORD + sourceKey + WHITESPACE + IN_KEYWORD + WHITESPACE
 						+ getListToString(recordIds));
 				// query.append(" and " + DynamicExtensionBaseQueryBuilder.getRemoveDisbledRecordsQuery(""));
 			}
 			else
 			{
 				// For one to one & one to many, check target entity table.
 				query.append(WHERE_KEYWORD + IDENTIFIER + WHITESPACE + IN_KEYWORD + WHITESPACE
 						+ getListToString(recordIds));
 				// query.append(" and " + targetKey);
 				// query.append(" and " + DynamicExtensionBaseQueryBuilder.getRemoveDisbledRecordsQuery(""));
 			}
 
 			if (entityManagerUtil.getNoOfRecord(query.toString()) != 0)
 			{
 				List<String> placeHolders = new ArrayList<String>();
 				placeHolders.add(association.getEntity().getName());
 				throw new DynamicExtensionsApplicationException(
 						"This record is refered by some record of ["
 								+ association.getEntity().getName() + "] ", null, DYEXTN_A_014,
 						placeHolders);
 			}
 		}
 	}
 
 	/**
 	 * This method returns containment record id list for a given parent record id list.
 	 * @param association
 	 * @param recordIds
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 */
 	public List<Long> getRecordIdListForContainment(AssociationInterface association,
 			List<Long> recordIds) throws DynamicExtensionsSystemException
 	{
 		String tableName = DynamicExtensionsUtility.getTableName(association);
 
 		String targetKey = association.getConstraintProperties().getTargetEntityKey();
 
 		StringBuffer cntnmntRecIdQry = new StringBuffer();
 		cntnmntRecIdQry.append(SELECT_KEYWORD + WHITESPACE + IDENTIFIER);
 		cntnmntRecIdQry.append(WHITESPACE + FROM_KEYWORD + WHITESPACE + tableName + WHITESPACE);
 		cntnmntRecIdQry.append(WHERE_KEYWORD + WHITESPACE + targetKey + WHITESPACE + IN_KEYWORD);
 		cntnmntRecIdQry.append(WHITESPACE + getListToString(recordIds) + WHITESPACE);
 
 		List<Long> results = entityManagerUtil.getResultInList(cntnmntRecIdQry.toString());
 
 		return results;
 	}
 
 	/**
 	 * This method returns the queries to remove the the association.
 	 * @param association
 	 * @param recId
 	 * @return
 	 */
 	public String getAssociationRemoveDataQuery(Association association, Long recId)
 	{
 		String tableName = DynamicExtensionsUtility.getTableName(association);
 		String sourceKey = association.getConstraintProperties().getSourceEntityKey();
 		String targetKey = association.getConstraintProperties().getTargetEntityKey();
 
 		StringBuffer query = new StringBuffer();
 
 		RoleInterface sourceRole = association.getSourceRole();
 		RoleInterface targetRole = association.getTargetRole();
 
 		Cardinality srcMaxCard = sourceRole.getMaximumCardinality();
 		Cardinality tgtMaxCard = targetRole.getMaximumCardinality();
 		if (srcMaxCard == Cardinality.MANY && tgtMaxCard == Cardinality.MANY)
 		{
 			// For many to many, delete all the records referred to by this recordId.
 			query.append(DELETE_KEYWORD + WHITESPACE + tableName + WHITESPACE + WHERE_KEYWORD
 					+ WHITESPACE + sourceKey);
 			query.append(WHITESPACE + EQUAL);
 			query.append(recId.toString());
 		}
 		else if (srcMaxCard == Cardinality.MANY && tgtMaxCard == Cardinality.ONE)
 		{
 			query.append(UPDATE_KEYWORD);
 			query.append(WHITESPACE + tableName);
 			query.append(WHITESPACE + SET_KEYWORD + WHITESPACE + sourceKey + EQUAL + WHITESPACE
 					+ "null" + WHITESPACE);
 			query.append(WHERE_KEYWORD + WHITESPACE + sourceKey + EQUAL + recId);
 		}
 		else
 		{
 			// For one to many and one to one, update target entity's records
 			// (set value in target column key = null) that are referring to 
 			// this record by setting it to null.
 			query.append(UPDATE_KEYWORD);
 			query.append(WHITESPACE + tableName);
 			query.append(WHITESPACE + SET_KEYWORD + WHITESPACE + targetKey + EQUAL + WHITESPACE
 					+ "null" + WHITESPACE);
 			query.append(WHERE_KEYWORD + WHITESPACE + targetKey + EQUAL + recId);
 		}
 
 		return query.toString();
 	}
 
 	/**
 	 * This method returns the main data table CREATE query that is associated with the entity.
 	 * @param entity
 	 * @param revQueries
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 */
 	protected List<String> getCreateMainTableQuery(Entity entity, List<String> revQueries)
 			throws DynamicExtensionsSystemException
 	{
 		List<String> queries = new ArrayList<String>();
 		if (entity.getTableProperties() != null)
 		{
 			String actStatus = Constants.ACTIVITY_STATUS_COLUMN + WHITESPACE
 					+ getDataTypeForStatus();
 
 			String tableName = entity.getTableProperties().getName();
 			StringBuffer query = new StringBuffer(CREATE_TABLE + " " + tableName + " "
 					+ OPENING_BRACKET + " " + actStatus + COMMA);
 			if (!EntityManagerUtil.isIdAttributePresent(entity))
 			{
 				query.append(IDENTIFIER).append(WHITESPACE).append(getDataTypeForIdentifier())
 						.append(NOT_KEYWORD).append(WHITESPACE).append(NULL_KEYWORD).append(COMMA);
 			}
 
 			Collection<AttributeInterface> attributes = entity.getAttributeCollection();
 
 			if (attributes != null && !attributes.isEmpty())
 			{
 				Iterator<AttributeInterface> attrIter = attributes.iterator();
 				while (attrIter.hasNext())
 				{
 					Attribute attribute = (Attribute) attrIter.next();
 
 					if (isAttributeColumnToBeExcluded(attribute))
 					{
 						// Column is not created if it is multi-select, file type etc.
 						continue;
 					}
 					if (attribute.getAttributeTypeInformation() instanceof FileAttributeTypeInformation)
 					{
 						query = query.append(extraColumnQueryStringForFileAttribute(attribute)
 								+ COMMA);
 					}
 
 					String type = "";
 
 					// Get column info for attribute.
 					String attrQueryPart = getQueryPartForAttribute(attribute, type, true);
 					query = query.append(attrQueryPart);
 					query = query.append(COMMA);
 				}
 			}
 
 			if (attributes != null && !attributes.isEmpty())
 			{
 				Iterator<AttributeInterface> attrIter = attributes.iterator();
 				while (attrIter.hasNext())
 				{
 					Attribute attribute = (Attribute) attrIter.next();
 					if (attribute.getIsPrimaryKey()
 							&& attribute.getColumnProperties().getName() != null
 							&& !attribute.getColumnProperties().getName().equalsIgnoreCase(
 									IDENTIFIER))
 					{
 						query = query.append(CONSTRAINT_KEYWORD + WHITESPACE
 								+ attribute.getColumnProperties().getName() + entity.getId()
 								+ WHITESPACE + UNIQUE_KEYWORD + OPENING_BRACKET
 								+ attribute.getColumnProperties().getName() + CLOSING_BRACKET);
 						query = query.append(COMMA);
 					}
 				}
 			}
 
 			// Identifier set as primary key.
 			query = query.append(PRIMARY_KEY_CONSTRAINT_FOR_ENTITY_DATA_TABLE + ")");
 
 			// Add create query.
 			queries.add(query.toString());
 
 			String reverseQuery = getReverseQueryForAbstractEntityTable(entity.getTableProperties()
 					.getName());
 			revQueries.add(reverseQuery);
 		}
 
 		return queries;
 	}
 
 	/**
 	 * This method builds the list of all the queries that need to be executed in order to
 	 * create the data table for the entity and its associations.
 	 * @param catEntity
 	 * @param revQueries
 	 * @param hibernateDAO
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	public List<String> getCreateCategoryQueryList(CategoryEntityInterface catEntity,
 			List<String> revQueries) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		List<String> queries = getCreateCategoryEntityTableQuery(catEntity, revQueries);
 		return queries;
 	}
 
 	/**
 	 * This method is used to create a table for each category entity.
 	 * @param catEntity
 	 * @param revQueries
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 */
 	protected List<String> getCreateCategoryEntityTableQuery(CategoryEntityInterface catEntity,
 			List<String> revQueries) throws DynamicExtensionsSystemException
 	{
 		List<String> queries = new ArrayList<String>();
 
 		if (catEntity.getTableProperties() != null)
 		{
 			String actStatus = Constants.ACTIVITY_STATUS_COLUMN + WHITESPACE
 					+ getDataTypeForStatus();
 
 			String tableName = catEntity.getTableProperties().getName();
 			StringBuffer query = new StringBuffer(CREATE_TABLE + " " + tableName + " "
 					+ OPENING_BRACKET + " " + actStatus + COMMA);
 			query.append(IDENTIFIER).append(WHITESPACE).append(getDataTypeForIdentifier()).append(
 					WHITESPACE).append(NOT_KEYWORD).append(WHITESPACE).append(NULL_KEYWORD).append(
 					COMMA);
 			query = query.append("record_Id" + WHITESPACE + getDataTypeForIdentifier() + WHITESPACE
 					+ "NOT NULL" + COMMA);
 			query = query.append(PRIMARY_KEY_CONSTRAINT_FOR_ENTITY_DATA_TABLE + ")"); //identifier set as primary key
 			queries.add(query.toString());
 
 			String reverseQuery = getReverseQueryForAbstractEntityTable(catEntity
 					.getTableProperties().getName());
 			revQueries.add(reverseQuery);
 		}
 
 		return queries;
 	}
 
 	/**
 	 * getForeignKeyConstraintQuery.
 	 * @param entity
 	 * @param revQueries
 	 * @return
 	 */
 	private List<String> getForeignKeyConstraintQuery(Entity entity, List<String> revQueries)
 	{
 		List<String> queries = new ArrayList<String>();
 		EntityInterface parentEntity = entity.getParentEntity();
 
 		// Add foreign key query for inheritance.
 		if (parentEntity != null)
 		{
 			String frnKeyCnstrQry = getForeignKeyConstraintQueryForInheritance(entity);
 			queries.add(frnKeyCnstrQry);
 			String frnCnstrRemQry = getForeignKeyRemoveConstraintQueryForInheritance(entity,
 					parentEntity);
 			revQueries.add(frnCnstrRemQry);
 		}
 
 		return queries;
 	}
 
 	/**
 	 * @param catEntity
 	 * @param revQueries
 	 * @return
 	 */
 	private List<String> getForeignKeyConstraintQuery(CategoryEntityInterface catEntity,
 			List<String> revQueries)
 	{
 		List<String> queries = new ArrayList<String>();
 		CategoryEntity parentCatEntity = (CategoryEntity) catEntity.getParentCategoryEntity();
 
 		// Add foreign key query for inheritance.
 		if (parentCatEntity != null && parentCatEntity.isCreateTable())
 		{
 			catEntity.getAttributeByName(IDENTIFIER);
 			String frnKeyCnstrQry = getForeignKeyConstraintQueryForInheritance(catEntity,
 					parentCatEntity);
 			queries.add(frnKeyCnstrQry);
 			String frnCnstrRemQry = getForeignKeyRemoveConstraintQueryForInheritance(catEntity,
 					parentCatEntity);
 			revQueries.add(frnCnstrRemQry);
 		}
 
 		return queries;
 	}
 
 	/**
 	 * This method returns the query to add foreign key constraint in the given child entity
 	 * that refers to identifier column of the parent.
 	 * @param entity
 	 * @return
 	 */
 	protected String getForeignKeyConstraintQueryForInheritance(EntityInterface entity)
 	{
 		EntityInterface parentEntity = entity.getParentEntity();
 		return getForeignKeyConstraintQueryForInheritance(entity, parentEntity);
 	}
 
 	/**
 	 * @param entity
 	 * @param parentEntity
 	 * @return
 	 */
 	protected String getForeignKeyConstraintQueryForInheritance(AbstractEntityInterface entity,
 			AbstractEntityInterface parentEntity)
 	{
 		StringBuffer frnKeyCnstrQry = new StringBuffer();
 		String frnCnstrName = entity.getTableProperties().getConstraintName() + UNDERSCORE
 				+ parentEntity.getTableProperties().getConstraintName();
 
 		frnKeyCnstrQry.append(ALTER_TABLE).append(WHITESPACE).append(
 				entity.getTableProperties().getName()).append(WHITESPACE).append(ADD_KEYWORD)
 				.append(WHITESPACE).append(CONSTRAINT_KEYWORD).append(WHITESPACE).append(
 						frnCnstrName).append(FOREIGN_KEY_KEYWORD).append(OPENING_BRACKET).append(
 						IDENTIFIER).append(CLOSING_BRACKET).append(WHITESPACE).append(
 						REFERENCES_KEYWORD).append(WHITESPACE).append(
 						parentEntity.getTableProperties().getName()).append(OPENING_BRACKET)
 				.append(IDENTIFIER).append(CLOSING_BRACKET);
 
 		return frnKeyCnstrQry.toString();
 	}
 
 	/**
 	 * This method returns the query to add foreign key constraint in the  
 	 * given child entity that refers to identifier column of the parent.
 	 * @param entity
 	 * @param parentEntity
 	 * @return
 	 */
 	protected String getForeignKeyRemoveConstraintQueryForInheritance(
 			AbstractEntityInterface entity, AbstractEntityInterface parentEntity)
 	{
 		StringBuffer frnKeyCnstrQry = new StringBuffer();
 		String frnCnstrName = entity.getTableProperties().getConstraintName() + UNDERSCORE
 				+ parentEntity.getTableProperties().getConstraintName();
 
 		frnKeyCnstrQry.append(ALTER_TABLE).append(WHITESPACE).append(
 				entity.getTableProperties().getName()).append(WHITESPACE).append(DROP_KEYWORD)
 				.append(WHITESPACE).append(CONSTRAINT_KEYWORD).append(WHITESPACE).append(
 						frnCnstrName);
 
 		return frnKeyCnstrQry.toString();
 	}
 
 	/**
 	 * This method returns the database type for identifier.
 	 * @return String database type for the identifier.
 	 * @throws DynamicExtensionsSystemException exception is thrown if factory is not instantiated properly.
 	 */
 	protected String getDataTypeForIdentifier() throws DynamicExtensionsSystemException
 	{
 		DataTypeFactory dataTypeFactory = DataTypeFactory.getInstance();
 		return dataTypeFactory.getDatabaseDataType("Integer");
 	}
 
 	/**
 	 * This method returns the database type for identifier.
 	 * @return String database type for the identifier.
 	 * @throws DynamicExtensionsSystemException exception is thrown if factory is not instantiated properly.
 	 */
 	protected String getDataTypeForStatus() throws DynamicExtensionsSystemException
 	{
 		DataTypeFactory dataTypeFactory = DataTypeFactory.getInstance();
 		return dataTypeFactory.getDatabaseDataType("String");
 	}
 
 	/**
 	 * This method returns true if a column in not to be created for the attribute.
 	 * @return
 	 */
 	protected boolean isAttributeColumnToBeExcluded(AttributeInterface attribute)
 	{
 		// This method is to be deleted.
 		return false;
 	}
 
 	/**
 	 * This method builds the query part for the primitive attribute
 	 * @param attribute
 	 * @param type
 	 * @param procCnstrn
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 */
 	protected String getQueryPartForAttribute(Attribute attribute, String type, boolean procCnstrn)
 			throws DynamicExtensionsSystemException
 	{
 		String attributeQry = null;
 		if (attribute != null)
 		{
 			String columnName = attribute.getColumnProperties().getName();
 			String nullConstraint = "";
 			if (procCnstrn)
 			{
 				nullConstraint = "NULL";
 
 				if (!attribute.getIsNullable())
 				{
 					nullConstraint = "NOT NULL";
 				}
 			}
 
 			attributeQry = columnName + WHITESPACE + type + WHITESPACE
 					+ getDatabaseTypeAndSize(attribute) //+ WHITESPACE + defaultConstraint
 					+ WHITESPACE + nullConstraint;
 		}
 
 		return attributeQry;
 	}
 
 	/**
 	 * This method builds the query part for the primitive attribute
 	 * @param attribute
 	 * @param type
 	 * @param procCnstrn
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 */
 	protected String getQueryPartForCategoryAttribute(CategoryAttributeInterface attribute,
 			String type, boolean procCnstrn) throws DynamicExtensionsSystemException
 	{
 		String attributeQry = null;
 		if (attribute != null)
 		{
 			String columnName = attribute.getColumnProperties().getName();
 			String nullConstraint = "";
 			if (procCnstrn)
 			{
 				nullConstraint = "NULL";
 			}
 
 			attributeQry = columnName + WHITESPACE + type + WHITESPACE + getDataTypeForIdentifier()
 					+ WHITESPACE + nullConstraint;
 		}
 
 		return attributeQry;
 	}
 
 	/**
 	 * This method returns the database type and size of the attribute 
 	 * passed to it, which becomes the part of the query for that attribute.
 	 * @param attribute
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 */
 	protected String getDatabaseTypeAndSize(AttributeMetadataInterface attribute)
 			throws DynamicExtensionsSystemException
 	{
 		try
 		{
 			DataTypeFactory dataTypeFactory = DataTypeFactory.getInstance();
 
 			AttributeTypeInformationInterface attrTypeInfo = attribute
 					.getAttributeTypeInformation();
 			if (attrTypeInfo instanceof StringAttributeTypeInformation)
 			{
 				return dataTypeFactory.getDatabaseDataType("String");
 			}
 			else if (attrTypeInfo instanceof IntegerAttributeTypeInformation)
 			{
 				return dataTypeFactory.getDatabaseDataType("Integer");
 			}
 			else if (attrTypeInfo instanceof DateAttributeTypeInformation)
 			{
 				DateAttributeTypeInformation dateAttrTypInfo = (DateAttributeTypeInformation) attrTypeInfo;
 
 				String format = dateAttrTypInfo.getFormat();
 				if (format != null && format.equalsIgnoreCase(ProcessorConstants.DATE_TIME_FORMAT))
 				{
 					return dataTypeFactory.getDatabaseDataType("DateTime");
 				}
 				else
 				{
 					return dataTypeFactory.getDatabaseDataType("Date");
 				}
 			}
 			else if (attrTypeInfo instanceof FloatAttributeTypeInformation)
 			{
 				return dataTypeFactory.getDatabaseDataType("Float");
 			}
 			else if (attrTypeInfo instanceof BooleanAttributeTypeInformation)
 			{
 				return dataTypeFactory.getDatabaseDataType("Boolean");
 			}
 			else if (attrTypeInfo instanceof DoubleAttributeTypeInformation)
 			{
 				return dataTypeFactory.getDatabaseDataType("Double");
 			}
 			else if (attrTypeInfo instanceof LongAttributeTypeInformation)
 			{
 				return dataTypeFactory.getDatabaseDataType("Long");
 			}
 			else if (attrTypeInfo instanceof ShortAttributeTypeInformation)
 			{
 				return dataTypeFactory.getDatabaseDataType("Short");
 			}
 			if (attrTypeInfo instanceof FileAttributeTypeInformation)
 			{
 				return dataTypeFactory.getDatabaseDataType("File");
 			}
 			else if (attrTypeInfo instanceof ObjectAttributeTypeInformation)
 			{
 				return dataTypeFactory.getDatabaseDataType("Object");
 			}
 		}
 		catch (DataTypeFactoryInitializationException e)
 		{
 			throw new DynamicExtensionsSystemException("Could Not get data type attribute", e);
 		}
 
 		return null;
 	}
 
 	/**
 	 * This method gives the opposite query of "CREATE TABLE" query for the data table.
 	 * @param tableName
 	 * @return
 	 */
 	protected String getReverseQueryForAbstractEntityTable(String tableName)
 	{
 		String query = null;
 		if (tableName != null && tableName.length() > 0)
 		{
 			query = "Drop table" + " " + tableName;
 		}
 
 		return query;
 	}
 
 	/**
 	 * This method returns all the CREATE table entries for associations present in the entity.
 	 * @param entity
 	 * @param revQueries
 	 * @return
 	 * @throws DynamicExtensionsApplicationException
 	 * @throws DynamicExtensionsSystemException
 	 */
 	protected List<String> getCreateAssociationsQueryList(Entity entity, List<String> revQueries)
 			throws DynamicExtensionsApplicationException, DynamicExtensionsSystemException
 	{
 		Collection<AssociationInterface> associations = entity.getAssociationCollection();
 
 		List<String> assoQueries = new ArrayList<String>();
 		if (associations != null && !associations.isEmpty())
 		{
 			Iterator<AssociationInterface> assoIter = associations.iterator();
 			while (assoIter.hasNext())
 			{
 				AssociationInterface association = assoIter.next();
 				if (((Association) association).getIsSystemGenerated())
 				{
 					// No need to process system generated associations.
 					continue;
 				}
 
 				boolean isAddAssoQry = true;
 				String assoQuery = getQueryPartForAssociation(association, revQueries, isAddAssoQry);
 				assoQueries.add(assoQuery);
 			}
 		}
 
 		return assoQueries;
 	}
 
 	/**
 	 * @param entity
 	 * @param revQueries
 	 * @return
 	 * @throws DynamicExtensionsApplicationException
 	 * @throws DynamicExtensionsSystemException
 	 */
 	protected List<String> getCreateAssociationsQueryList(CategoryEntityInterface entity,
 			List<String> revQueries) throws DynamicExtensionsApplicationException,
 			DynamicExtensionsSystemException
 	{
 		Collection<CategoryAssociationInterface> associations = entity
 				.getCategoryAssociationCollection();
 
 		List<String> assoQueries = new ArrayList<String>();
 		if (associations != null && !associations.isEmpty())
 		{
 			for (CategoryAssociationInterface catAsso : associations)
 			{
 				boolean isAddAssoQry = true;
 				String assoQuery = getQueryPartForAssociation(catAsso, revQueries, isAddAssoQry);
 				assoQueries.add(assoQuery);
 			}
 		}
 
 		return assoQueries;
 	}
 
 	/**
 	 * This method builds the query part for the association.
 	 * @param association
 	 * @param revQueries
 	 * @param isAddAssoQuery
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 */
 	public String getQueryPartForAssociation(AssociationInterface association,
 			List<String> revQueries, boolean isAddAssoQuery)
 			throws DynamicExtensionsSystemException
 	{
 		Logger.out.debug("Entering getQueryPartForAssociation method");
 
 		EntityInterface srcEntity = association.getEntity();
 		EntityInterface tgtEntity = association.getTargetEntity();
 		RoleInterface sourceRole = association.getSourceRole();
 		RoleInterface targetRole = association.getTargetRole();
 
 		Cardinality srcMaxCard = sourceRole.getMaximumCardinality();
 		Cardinality tgtMaxCard = targetRole.getMaximumCardinality();
 
 		ConstraintPropertiesInterface constraintProperties = association.getConstraintProperties();
 		String tableName = "";
 
 		String dataType = getDataTypeForIdentifier();
 		StringBuffer query = new StringBuffer();
 		if (srcMaxCard == Cardinality.MANY && tgtMaxCard == Cardinality.MANY)
 		{
 			// For many to many, a middle table is created.
 			tableName = constraintProperties.getName();
 
 			query.append(CREATE_TABLE + WHITESPACE + tableName + WHITESPACE + OPENING_BRACKET
 					+ WHITESPACE + IDENTIFIER + WHITESPACE + dataType + WHITESPACE + NOT_KEYWORD
 					+ WHITESPACE + NULL_KEYWORD + COMMA);
 			query.append(constraintProperties.getSourceEntityKey() + WHITESPACE + dataType + COMMA);
 			query.append(constraintProperties.getTargetEntityKey() + WHITESPACE + dataType + COMMA
 					+ WHITESPACE);
 			query.append(PRIMARY_KEY_CONSTRAINT_FOR_ENTITY_DATA_TABLE + CLOSING_BRACKET);
 			String rollbackQuery = DROP_KEYWORD + WHITESPACE + TABLE_KEYWORD + WHITESPACE
 					+ tableName;
 
 			if (isAddAssoQuery)
 			{
 				revQueries.add(rollbackQuery);
 			}
 			else
 			{
 				revQueries.add(query.toString());
 				query = new StringBuffer(rollbackQuery);
 			}
 		}
 		else if (srcMaxCard == Cardinality.MANY && tgtMaxCard == Cardinality.ONE)
 		{
 			// For many to one, a column is added into source entity table.
 			tableName = srcEntity.getTableProperties().getName();
 			String columnName = constraintProperties.getSourceEntityKey();
 			query.append(getAddAttributeQuery(tableName, columnName, dataType, revQueries,
 					isAddAssoQuery));
 		}
 		else
 		{
 			// For one to one and one to many, a column is added into target entity table.
 			tableName = tgtEntity.getTableProperties().getName();
 			String columnName = constraintProperties.getTargetEntityKey();
 			query.append(getAddAttributeQuery(tableName, columnName, dataType, revQueries,
 					isAddAssoQuery));
 		}
 
 		Logger.out.debug("Exiting getQueryPartForAssociation method");
 
 		return query.toString();
 	}
 
 	/**
 	 * This method builds the query part for the association.
 	 * @param catAsso
 	 * @param revQueries
 	 * @param isAddAssoQuery
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 */
 	public String getQueryPartForAssociation(CategoryAssociationInterface catAsso,
 			List<String> revQueries, boolean isAddAssoQuery)
 			throws DynamicExtensionsSystemException
 	{
 		Logger.out.debug("Entering getQueryPartForAssociation method");
 
 		StringBuffer query = new StringBuffer();
 
 		CategoryEntityInterface tgtCatEntity = catAsso.getTargetCategoryEntity();
 
 		// For one to one and one to many, a column is added into target entity table.
 		String tableName = tgtCatEntity.getTableProperties().getName();
 		String columnName = catAsso.getConstraintProperties().getTargetEntityKey();
 		query.append(getAddAttributeQuery(tableName, columnName, getDataTypeForIdentifier(),
 				revQueries, isAddAssoQuery));
 
 		Logger.out.debug("Exiting getQueryPartForAssociation  method");
 
 		return query.toString();
 	}
 
 	/**
 	 * @param tableName
 	 * @param columnName
 	 * @param dataType
 	 * @param revQueries
 	 * @param isAddAssoQry
 	 * @return
 	 */
 	protected String getAddAttributeQuery(String tableName, String columnName, String dataType,
 			List<String> revQueries, boolean isAddAssoQry)
 	{
 		StringBuffer query = new StringBuffer();
 		query.append(ALTER_TABLE + WHITESPACE + tableName + WHITESPACE + ADD_KEYWORD + WHITESPACE);
 		query.append(columnName + WHITESPACE + dataType + WHITESPACE);
 		String rollbackQuery = ALTER_TABLE + WHITESPACE + tableName + WHITESPACE + DROP_KEYWORD
 				+ WHITESPACE + COLUMN_KEYWORD + WHITESPACE + columnName;
 
 		if (isAddAssoQry)
 		{
 			revQueries.add(rollbackQuery);
 			return query.toString();
 		}
 		else
 		{
 			revQueries.add(query.toString());
 			return rollbackQuery;
 		}
 	}
 
 	/**
 	 * This method returns queries for any attribute that is modified.
 	 * @param entity
 	 * @param dbaseCopy
 	 * @param attrRlbkQries
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	protected List<String> getUpdateAttributeQueryList(Entity entity, Entity dbaseCopy,
 			List<String> attrRlbkQries) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		Logger.out.debug("Entering getUpdateAttributeQueryList method");
 
 		Collection<AttributeInterface> attributes = entity.getAttributeCollection();
 
 		List<String> attrQueries = new ArrayList<String>();
 		if (attributes != null && !attributes.isEmpty())
 		{
 			Iterator<AttributeInterface> attrIter = attributes.iterator();
 
 			while (attrIter.hasNext())
 			{
 				Attribute attribute = (Attribute) attrIter.next();
 				Attribute savedAttribute = (Attribute) dbaseCopy.getAttributeByIdentifier(attribute
 						.getId());
 
 				if (isAttributeColumnToBeAdded(attribute, savedAttribute))
 				{
 					// Either the attribute is newly added or previously excluded(file type/multi select) 
 					// attribute.
 					String attributeQuery = processAddAttribute(attribute, attrRlbkQries);
 					attrQueries.add(attributeQuery);
 				}
 				else
 				{
 					// Check for other modification in the attributes such a unique constraint change.
 					List<String> modifiedAttrQueries = processModifyAttribute(attribute,
 							savedAttribute, attrRlbkQries);
 					attrQueries.addAll(modifiedAttrQueries);
 				}
 			}
 		}
 
 		processRemovedAttributes(entity, dbaseCopy, attrQueries, attrRlbkQries);
 
 		Logger.out.debug("Exiting getUpdateAttributeQueryList method");
 
 		return attrQueries;
 	}
 
 	/**
 	 * This method returns queries for any attribute that is modified.
 	 * @param catEntity
 	 * @param dbaseCopy
 	 * @param attrRlbkQries
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	protected List<String> getUpdateAttributeQueryList(CategoryEntity catEntity,
 			CategoryEntity dbaseCopy, List<String> attrRlbkQries)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		Logger.out.debug("Entering getUpdateAttributeQueryList method");
 
 		Collection<CategoryAttributeInterface> attributes = catEntity
 				.getCategoryAttributeCollection();
 
 		List<String> attrQueries = new ArrayList<String>();
 		if (attributes != null)
 		{
 			for (CategoryAttributeInterface catAttribute : attributes)
 			{
 				CategoryAttribute attribute = (CategoryAttribute) catAttribute;
 				CategoryAttribute savedAttribute = (CategoryAttribute) dbaseCopy
 						.getAttributeByIdentifier(attribute.getId());
 
 				if (isAbstarctAttributeColumnToBeAdded(attribute, savedAttribute))
 				{
 					// Either the attribute is newly added or previously excluded(file type/multi select) 
 					// attribute.
 					String attributeQuery = processAddAttribute(attribute, attrRlbkQries);
 					attrQueries.add(attributeQuery);
 				}
 			}
 		}
 
 		processRemovedAttributes(catEntity, dbaseCopy, attrQueries, attrRlbkQries);
 
 		Logger.out.debug("Exiting getUpdateAttributeQueryList method");
 
 		return attrQueries;
 	}
 
 	/**
 	 * @param entity
 	 * @param dbaseCopy
 	 * @param attrRlbkQries
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 */
 	protected List<String> getUpdateAssociationsQueryList(Entity entity, Entity dbaseCopy,
 			List<String> attrRlbkQries) throws DynamicExtensionsSystemException
 	{
 		Logger.out.debug("Entering getUpdateAssociationsQueryList method");
 
 		List<String> assoQueries = new ArrayList<String>();
 		boolean isAddAssoQuery = true;
 
 		Collection<AssociationInterface> associations = entity.getAssociationCollection();
 
 		if (associations != null && !associations.isEmpty())
 		{
 			Iterator<AssociationInterface> assoIter = associations.iterator();
 
 			while (assoIter.hasNext())
 			{
 				Association association = (Association) assoIter.next();
 				Association assoDbaseCopy = (Association) dbaseCopy
 						.getAssociationByIdentifier(association.getId());
 
 				if (association.getIsSystemGenerated())
 				{
 					continue;
 				}
 				if (assoDbaseCopy == null)
 				{
 					isAddAssoQuery = true;
 					String newAssoQuery = getQueryPartForAssociation(association, attrRlbkQries,
 							isAddAssoQuery);
 					assoQueries.add(newAssoQuery);
 				}
 				else
 				{
 					if (isCardinalityChanged(association, assoDbaseCopy))
 					{
 						isAddAssoQuery = false;
 						String savedAssoRemQry = getQueryPartForAssociation(assoDbaseCopy,
 								attrRlbkQries, isAddAssoQuery);
 						assoQueries.add(savedAssoRemQry);
 
 						isAddAssoQuery = true;
 						String newAssoAddQuery = getQueryPartForAssociation(association,
 								attrRlbkQries, isAddAssoQuery);
 						assoQueries.add(newAssoAddQuery);
 					}
 				}
 			}
 		}
 
 		processRemovedAssociation(entity, dbaseCopy, assoQueries, attrRlbkQries);
 
 		Logger.out.debug("Exiting getUpdateAssociationsQueryList method");
 
 		return assoQueries;
 	}
 
 	/**
 	 * @param entity
 	 * @param dbaseCopy
 	 * @param attrRlbkQries
 	 * @return
 	 */
 	protected List<String> getUpdateAssociationsQueryList(CategoryEntity catEntity,
 			CategoryEntity dbaseCopy, List<String> attrRlbkQries)
 			throws DynamicExtensionsSystemException
 	{
 		Logger.out.debug("Entering getUpdateAssociationsQueryList method");
 
 		List<String> assoQueries = new ArrayList<String>();
 		boolean isAddAssoQry = true;
 
 		Collection<CategoryAssociationInterface> associations = catEntity
 				.getCategoryAssociationCollection();
 
 		if (associations != null)
 		{
 			for (CategoryAssociationInterface catAsso : associations)
 			{
 				CategoryAssociation assoDbaseCopy = (CategoryAssociation) dbaseCopy
 						.getAssociationByIdentifier(catAsso.getId());
 
 				if (isAbstarctAttributeColumnToBeAdded(catAsso, assoDbaseCopy))
 				{
 					isAddAssoQry = true;
 					String newAssoQuery = getQueryPartForAssociation(catAsso, attrRlbkQries,
 							isAddAssoQry);
 					assoQueries.add(newAssoQuery);
 				}
 			}
 		}
 
 		processRemovedAssociation(catEntity, dbaseCopy, assoQueries, attrRlbkQries);
 
 		Logger.out.debug("ExitinggetUpdateAssociationsQueryList method");
 
 		return assoQueries;
 	}
 
 	/**
 	 * This method processes all the attributes that were previously saved but now removed by editing.
 	 * @param entity
 	 * @param dbaseCopy
 	 * @param attrQueries
 	 * @param attrRlbkQries
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	protected void processRemovedAttributes(Entity entity, Entity dbaseCopy,
 			List<String> attrQueries, List<String> attrRlbkQries)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		Collection<AttributeInterface> savedAttr = dbaseCopy.getAttributeCollection();
 
 		if (entity.getTableProperties() != null)
 		{
 			String tableName = entity.getTableProperties().getName();
 
 			if (savedAttr != null && !savedAttr.isEmpty())
 			{
 				Iterator<AttributeInterface> savedAttrIter = savedAttr.iterator();
 				while (savedAttrIter.hasNext())
 				{
 					Attribute savedAttribute = (Attribute) savedAttrIter.next();
 					Attribute attribute = (Attribute) entity
 							.getAttributeByIdentifier(savedAttribute.getId());
 
 					if (attribute == null && isDataPresent(tableName, savedAttribute))
 					{
 						throw new DynamicExtensionsApplicationException(
 								"data is present ,attribute can not be deleted", null, DYEXTN_A_013);
 					}
 
 					// Attribute is removed or modified, so its column needs to be removed.
 					if (isAttributeColumnToBeRemoved(attribute, savedAttribute))
 					{
 						List<String> columnName = new ArrayList<String>();
 						columnName.add(savedAttribute.getColumnProperties().getName());
 
 						String type = "";
 						String remAttrRlbkQry = ALTER_TABLE + WHITESPACE + tableName + WHITESPACE
 								+ ADD_KEYWORD + WHITESPACE + OPENING_BRACKET
 								+ getQueryPartForAttribute(savedAttribute, type, true);
 
 						if (savedAttribute.getAttributeTypeInformation() instanceof FileAttributeTypeInformation)
 						{
 							columnName.add(savedAttribute.getColumnProperties().getName()
 									+ UNDERSCORE + FILE_NAME);
 							columnName.add(savedAttribute.getColumnProperties().getName()
 									+ UNDERSCORE + CONTENT_TYPE);
 							remAttrRlbkQry = remAttrRlbkQry + COMMA
 									+ extraColumnQueryStringForFileAttribute(savedAttribute);
 						}
 
 						remAttrRlbkQry = remAttrRlbkQry + CLOSING_BRACKET;
 
 						String remAttrQuery = getDropColumnQuery(tableName, columnName);
 
 						attrQueries.add(remAttrQuery);
 						attrRlbkQries.add(remAttrRlbkQry);
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * This method processes all the attributes that were previously saved but now removed by editing.
 	 * @param catEntity
 	 * @param dbaseCopy
 	 * @param attrQueries
 	 * @param attrRlbkQries
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	protected void processRemovedAttributes(CategoryEntity catEntity, CategoryEntity dbaseCopy,
 			List<String> attrQueries, List<String> attrRlbkQries)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		Collection<CategoryAttributeInterface> savedAttr = dbaseCopy
 				.getCategoryAttributeCollection();
 
 		if (catEntity.getTableProperties() != null)
 		{
 			String tableName = catEntity.getTableProperties().getName();
 
 			if (savedAttr != null)
 			{
 				for (CategoryAttributeInterface catAttr : savedAttr)
 				{
 					CategoryAttribute categoryAttr = (CategoryAttribute) catEntity
 							.getAttributeByIdentifier(catAttr.getId());
 
 					if (categoryAttr == null && isDataPresent(tableName, catAttr))
 					{
 						throw new DynamicExtensionsApplicationException(
 								"data is present ,attribute can not be deleted", null, DYEXTN_A_013);
 					}
 
 					// Attribute is removed or modified, so its column needs to be removed.
 					if (isAttributeColumnToBeRemoved(categoryAttr, catAttr))
 					{
 						String columnName = catAttr.getColumnProperties().getName();
 
 						String remAttrQuery = ALTER_TABLE + WHITESPACE + tableName + WHITESPACE
 								+ DROP_KEYWORD + WHITESPACE + COLUMN_KEYWORD + WHITESPACE
 								+ columnName;
 						String type = "";
 
 						String remAttrRlbkQry = ALTER_TABLE + WHITESPACE + tableName + WHITESPACE
 								+ ADD_KEYWORD + WHITESPACE
 								+ getQueryPartForCategoryAttribute(catAttr, type, true);
 
 						attrQueries.add(remAttrQuery);
 						attrRlbkQries.add(remAttrRlbkQry);
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * This method returns true if an attribute is changed whether its column needs to be removed.
 	 * @param attribute attribute
 	 * @param dbaseCopy dataBaseCopy of the  attribute
 	 * @return true if its column needs to be removed
 	 */
 	protected boolean isAttributeColumnToBeRemoved(AttributeInterface attribute,
 			AttributeInterface dbaseCopy)
 	{
 		boolean isColRemoved = false;
 
 		if (attribute == null)
 		{
 			// Attribute removed now.
 			isColRemoved = true;
 
 			if (isAttributeColumnToBeExcluded(dbaseCopy))
 			{
 				isColRemoved = false;
 			}
 		}
 		else
 		{
 			// Previously not excluded, but now needs to excluded.
 			if (!attribute.getColumnProperties().getName().equalsIgnoreCase(IDENTIFIER))
 			{
 				if (!isAttributeColumnToBeExcluded(dbaseCopy)
 						&& isAttributeColumnToBeExcluded(attribute))
 				{
 					isColRemoved = true;
 				}
 			}
 		}
 
 		return isColRemoved;
 	}
 
 	/**
 	 * This method returns true if a category attribute is changed whether its column needs to be removed.
 	 * @param catAttr attribute
 	 * @param dbaseCopy dataBaseCopy of the  attribute
 	 * @return true if its column to be removed
 	 */
 	protected boolean isAttributeColumnToBeRemoved(CategoryAttributeInterface catAttr,
 			CategoryAttributeInterface dbaseCopy)
 	{
 		boolean isColRemoved = false;
 
 		if (catAttr == null && dbaseCopy != null)
 		{
 			// Attribute removed now.
 			isColRemoved = true;
 		}
 
 		return isColRemoved;
 	}
 
 	/**
 	 * @param association
 	 * @param dbaseCopy
 	 * @return
 	 */
 	protected boolean isCardinalityChanged(Association association, Association dbaseCopy)
 	{
 		Cardinality srcMaxCard = association.getSourceRole().getMaximumCardinality();
 		Cardinality tgtMaxCard = association.getTargetRole().getMaximumCardinality();
 
 		Cardinality srcMaxCardDbCpy = dbaseCopy.getSourceRole().getMaximumCardinality();
 		Cardinality tgtMaxCardDbCpy = dbaseCopy.getTargetRole().getMaximumCardinality();
 
 		if (!srcMaxCard.equals(srcMaxCardDbCpy) || !tgtMaxCard.equals(tgtMaxCardDbCpy))
 		{
 			return true;
 		}
 
 		return false;
 	}
 
 	/**
 	 * This method processes any associations that are deleted from the entity.
 	 * @param entity
 	 * @param dbaseCopy
 	 * @param assoQueries
 	 * @param attrRlbkQries
 	 * @throws DynamicExtensionsSystemException
 	 */
 	protected void processRemovedAssociation(Entity entity, Entity dbaseCopy,
 			List<String> assoQueries, List<String> attrRlbkQries)
 			throws DynamicExtensionsSystemException
 	{
 		Logger.out.debug("Entering processRemovedAssociation method");
 
 		Collection<AssociationInterface> savedAsso = dbaseCopy.getAssociationCollection();
 
 		if (entity.getTableProperties() != null)
 		{
 			if (savedAsso != null && !savedAsso.isEmpty())
 			{
 				Iterator<AssociationInterface> savedAssoIter = savedAsso.iterator();
 				while (savedAssoIter.hasNext())
 				{
 					Association savedAssociation = (Association) savedAssoIter.next();
 					Association association = (Association) entity
 							.getAssociationByIdentifier(savedAssociation.getId());
 
 					// Removed.
 					if (association == null)
 					{
 						boolean isAddAssoQuery = false;
 						String remAssoQuery = getQueryPartForAssociation(savedAssociation,
 								attrRlbkQries, isAddAssoQuery);
 						assoQueries.add(remAssoQuery);
 					}
 				}
 			}
 		}
 
 		Logger.out.debug("Exiting processRemovedAssociation method");
 	}
 
 	/**
 	 * This method processes any associations that are deleted from the entity.
 	 * @param entity
 	 * @param dbaseCopy
 	 * @param assoQueries
 	 * @param attrRlbkQries
 	 * @throws DynamicExtensionsSystemException
 	 */
 	protected void processRemovedAssociation(CategoryEntity catEntity, CategoryEntity dbaseCopy,
 			List<String> assoQueries, List<String> attrRlbkQries)
 			throws DynamicExtensionsSystemException
 	{
 		Logger.out.debug("Entering processRemovedAssociation method");
 
 		Collection<CategoryAssociationInterface> savedAsso = dbaseCopy
 				.getCategoryAssociationCollection();
 
 		if (catEntity.getTableProperties() != null)
 		{
 			if (savedAsso != null)
 			{
 				for (CategoryAssociationInterface savedCatAsso : savedAsso)
 				{
 					CategoryAssociation association = (CategoryAssociation) catEntity
 							.getAssociationByIdentifier(savedCatAsso.getId());
 
 					// Removed.
 					if (association == null)
 					{
 						boolean isAddAssoQuery = false;
 						String remAssoQuery = getQueryPartForAssociation(savedCatAsso,
 								attrRlbkQries, isAddAssoQuery);
 						assoQueries.add(remAssoQuery);
 					}
 				}
 			}
 		}
 
 		Logger.out.debug("Exiting processRemovedAssociation method");
 	}
 
 	/**
 	 * This method returns true if a category attribute is changed whether its column needs to be added.
 	 * @param attribute
 	 * @param dbaseCopy
 	 * @return
 	 */
 	protected boolean isAttributeColumnToBeAdded(AttributeInterface attribute,
 			AttributeInterface dbaseCopy)
 	{
 		boolean isColAdded = false;
 
 		if (dbaseCopy == null)
 		{
 			// Newly added.
 			if (!isAttributeColumnToBeExcluded(attribute))
 			{
 				isColAdded = true;
 			}
 		}
 		else
 		{
 			// Previously excluded and now need to be added.
 			if (isAttributeColumnToBeExcluded(dbaseCopy)
 					&& !isAttributeColumnToBeExcluded(attribute))
 			{
 				isColAdded = true;
 			}
 		}
 
 		return isColAdded;
 	}
 
 	/**
 	 * This method returns true if a category attribute is changed whether its column needs to be added.
 	 * @param attribute
 	 * @param dbaseCopy
 	 * @return
 	 */
 	protected boolean isAbstarctAttributeColumnToBeAdded(BaseAbstractAttributeInterface attribute,
 			BaseAbstractAttributeInterface dbaseCopy)
 	{
 		boolean isColAdded = false;
 
 		if (dbaseCopy == null && attribute != null)
 		{
 			// Newly added.
 			isColAdded = true;
 		}
 
 		return isColAdded;
 	}
 
 	/**
 	 * This method takes the edited attribute and its database copy and then looks for 
 	 * any changes. Changes could be in terms of data table query viz. change in the 
 	 * constraint NOT NULL AND UNIQUE.
 	 * @param attribute
 	 * @param savedAttr
 	 * @param attrRlbkQries
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	protected List<String> processModifyAttribute(Attribute attribute, Attribute savedAttr,
 			List<String> attrRlbkQries) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		List<String> mdfyAttrQries = new ArrayList<String>();
 
 		if (isAttributeColumnToBeExcluded(attribute))
 		{
 			return mdfyAttrQries;
 		}
 
 		String tableName = attribute.getEntity().getTableProperties().getName();
 		String columnName = attribute.getColumnProperties().getName();
 
 		String newTypeClass = attribute.getAttributeTypeInformation().getClass().getName();
 		String oldTypeClass = savedAttr.getAttributeTypeInformation().getClass().getName();
 
 		if (!newTypeClass.equals(oldTypeClass))
 		{
 			checkIfDataTypeChangeAllowable(attribute);
 			mdfyAttrQries = getAttributeDataTypeChangedQuery(attribute, savedAttr, attrRlbkQries);
 		}
 
 		if (attribute.getIsPrimaryKey() && !savedAttr.getIsPrimaryKey())
 		{
 			String uniqCnstrQry = ALTER_TABLE + WHITESPACE + tableName + WHITESPACE + ADD_KEYWORD
 					+ WHITESPACE + CONSTRAINT_KEYWORD + WHITESPACE + columnName + UNDERSCORE
 					+ UNIQUE_CONSTRAINT_SUFFIX + WHITESPACE + UNIQUE_KEYWORD + WHITESPACE
 					+ OPENING_BRACKET + columnName + CLOSING_BRACKET;
 			String uniqCnstrRlbkQry = ALTER_TABLE + WHITESPACE + tableName + WHITESPACE
 					+ DROP_KEYWORD + WHITESPACE + CONSTRAINT_KEYWORD + WHITESPACE + columnName
 					+ UNDERSCORE + UNIQUE_CONSTRAINT_SUFFIX;
 
 			mdfyAttrQries.add(uniqCnstrQry);
 			attrRlbkQries.add(uniqCnstrRlbkQry);
 		}
 		else if (!attribute.getIsPrimaryKey() && savedAttr.getIsPrimaryKey())
 		{
 			String uniqCnstrQry = ALTER_TABLE + WHITESPACE + tableName + WHITESPACE + DROP_KEYWORD
 					+ WHITESPACE + CONSTRAINT_KEYWORD + WHITESPACE + columnName + UNDERSCORE
 					+ UNIQUE_CONSTRAINT_SUFFIX;
 			String uniqCnstrRlbkQry = ALTER_TABLE + WHITESPACE + tableName + WHITESPACE
 					+ ADD_KEYWORD + WHITESPACE + CONSTRAINT_KEYWORD + WHITESPACE + columnName
 					+ UNDERSCORE + UNIQUE_CONSTRAINT_SUFFIX + WHITESPACE + UNIQUE_KEYWORD
 					+ WHITESPACE + OPENING_BRACKET + columnName + CLOSING_BRACKET;
 
 			mdfyAttrQries.add(uniqCnstrQry);
 			attrRlbkQries.add(uniqCnstrRlbkQry);
 		}
 
 		return mdfyAttrQries;
 	}
 
 	/**
 	 * @param attribute
 	 * @throws DynamicExtensionsApplicationException
 	 * @throws DynamicExtensionsSystemException
 	 */
 	private void checkIfDataTypeChangeAllowable(Attribute attribute)
 			throws DynamicExtensionsApplicationException, DynamicExtensionsSystemException
 	{
 		EntityInterface entity = attribute.getEntity();
 		String tableName = entity.getTableProperties().getName();
 		if (isDataPresent(tableName))
 		{
 			throw new DynamicExtensionsApplicationException(
 					"Can not change the data type of the attribute", null, DYEXTN_A_009);
 		}
 	}
 
 	/**
 	 * @param tableName
 	 * @param savedAttr
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 */
 	public boolean isDataPresent(String tableName, Attribute savedAttr)
 			throws DynamicExtensionsSystemException
 	{
 		boolean isDataPresent = false;
 
 		StringBuffer query = new StringBuffer();
 		if (!isAttributeColumnToBeExcluded(savedAttr))
 		{
 			query.append(SELECT_KEYWORD).append(WHITESPACE).append("COUNT").append(OPENING_BRACKET)
 					.append(ASTERIX).append(CLOSING_BRACKET).append(WHITESPACE)
 					.append(FROM_KEYWORD).append(WHITESPACE).append(tableName).append(WHITESPACE)
 					.append(WHERE_KEYWORD).append(WHITESPACE).append(
 							savedAttr.getColumnProperties().getName()).append(WHITESPACE).append(
 							"IS").append(WHITESPACE).append(NOT_KEYWORD).append(WHITESPACE).append(
 							NULL_KEYWORD).append(WHITESPACE);
 
 			if (!(savedAttr.getAttributeTypeInformation() instanceof FileAttributeTypeInformation)
 					&& !(savedAttr.getAttributeTypeInformation() instanceof ObjectAttributeTypeInformation))
 			{
 				query.append(AND_KEYWORD).append(WHITESPACE).append(
 						savedAttr.getColumnProperties().getName()).append(WHITESPACE).append(
 						NOT_KEYWORD).append(WHITESPACE).append(LIKE_KEYWORD).append(WHITESPACE)
 						.append("''");
 			}
 
 			ResultSet resultSet = null;
 			try
 			{
 				resultSet = entityManagerUtil.executeQuery(query.toString());
 				resultSet.next();
 				Long count = resultSet.getLong(1);
 				if (count > 0)
 				{
 					isDataPresent = true;
 				}
 			}
 			catch (SQLException e)
 			{
 				throw new DynamicExtensionsSystemException(
 						"Can not check the availability of data", e);
 			}
 			finally
 			{
 				if (resultSet != null)
 				{
 					try
 					{
 						resultSet.close();
 					}
 					catch (SQLException e)
 					{
 						throw new DynamicExtensionsSystemException(e.getMessage(), e);
 					}
 				}
 			}
 		}
 		else
 		{
 			Collection<Integer> records = EntityManager.getInstance().getAttributeRecordsCount(
 					savedAttr.getEntity().getId(), savedAttr.getId());
 
 			if (records != null && !records.isEmpty())
 			{
 				Integer count = (Integer) records.iterator().next();
 				if (count > 0)
 				{
 					isDataPresent = true;
 				}
 			}
 		}
 
 		return isDataPresent;
 	}
 
 	/**
 	 * @param tableName
 	 * @param savedAttr
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 */
 	public boolean isDataPresent(String tableName, CategoryAttributeInterface savedAttr)
 			throws DynamicExtensionsSystemException
 	{
 		boolean isDataPresent = false;
 
 		StringBuffer query = new StringBuffer();
 		if (savedAttr != null)
 		{
 			query.append(SELECT_KEYWORD).append(WHITESPACE).append("COUNT").append(OPENING_BRACKET)
 					.append(ASTERIX).append(CLOSING_BRACKET).append(WHITESPACE)
 					.append(FROM_KEYWORD).append(WHITESPACE).append(tableName).append(WHITESPACE)
 					.append(WHERE_KEYWORD).append(WHITESPACE).append(
 							savedAttr.getColumnProperties().getName()).append(WHITESPACE).append(
 							"IS").append(WHITESPACE).append(NOT_KEYWORD).append(WHITESPACE).append(
 							NULL_KEYWORD).append(WHITESPACE).append(AND_KEYWORD).append(WHITESPACE)
 					.append(savedAttr.getColumnProperties().getName()).append(WHITESPACE).append(
 							NOT_KEYWORD).append(WHITESPACE).append(LIKE_KEYWORD).append(WHITESPACE)
 					.append("''");
 
 			ResultSet resultSet = null;
 			try
 			{
 				resultSet = entityManagerUtil.executeQuery(query.toString());
 				resultSet.next();
 				Long count = resultSet.getLong(1);
 				if (count > 0)
 				{
 					isDataPresent = true;
 				}
 
 			}
 			catch (SQLException e)
 			{
 				throw new DynamicExtensionsSystemException(
 						"Can not check the availability of data", e);
 			}
 			finally
 			{
 				if (resultSet != null)
 				{
 					try
 					{
 						resultSet.close();
 					}
 					catch (SQLException e)
 					{
 						throw new DynamicExtensionsSystemException(e.getMessage(), e);
 					}
 				}
 			}
 		}
 
 		return isDataPresent;
 	}
 
 	/**
 	 * @param tableName
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 */
 	public boolean isDataPresent(String tableName) throws DynamicExtensionsSystemException
 	{
 		StringBuffer query = new StringBuffer();
 		query.append(SELECT_KEYWORD).append(WHITESPACE).append("COUNT").append(OPENING_BRACKET)
 				.append(ASTERIX).append(CLOSING_BRACKET).append(WHITESPACE).append(FROM_KEYWORD)
 				.append(WHITESPACE).append(tableName);
 
 		ResultSet resultSet = null;
 		try
 		{
 			resultSet = entityManagerUtil.executeQuery(query.toString());
 			resultSet.next();
 			Long count = resultSet.getLong(1);
 			if (count > 0)
 			{
 				return true;
 			}
 			else
 			{
 				return false;
 			}
 		}
 		catch (SQLException e)
 		{
 			throw new DynamicExtensionsSystemException("Can not check the availability of data", e);
 		}
 		finally
 		{
 			if (resultSet != null)
 			{
 				try
 				{
 					resultSet.close();
 				}
 				catch (SQLException e)
 				{
 					throw new DynamicExtensionsSystemException(e.getMessage(), e);
 				}
 			}
 		}
 	}
 
 	/**
 	 * This method returns the query for the attribute to modify its data type.
 	 * @param attribute
 	 * @param savedAttr
 	 * @param modifyAttributeRollbackQuery
 	 * @param tableName
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 */
 	protected List<String> getAttributeDataTypeChangedQuery(Attribute attribute,
 			Attribute savedAttr, List<String> mdfyAttrRlbkQries)
 			throws DynamicExtensionsSystemException
 	{
 		String tableName = attribute.getEntity().getTableProperties().getName();
 		String type = "";
 		String mdfyAttrRlbkQry = "";
 
 		String mdfyAttrQuery = getQueryPartForAttribute(attribute, type, false);
 		mdfyAttrQuery = ALTER_TABLE + tableName + ADD_KEYWORD + OPENING_BRACKET + mdfyAttrQuery;
 
 		mdfyAttrRlbkQry = getQueryPartForAttribute(savedAttr, type, false);
 		mdfyAttrRlbkQry = ALTER_TABLE + WHITESPACE + tableName + WHITESPACE + MODIFY_KEYWORD
 				+ WHITESPACE + mdfyAttrRlbkQry;
 
 		String nullQueryKeyword = "";
 		String nullQueryRlbkKeyword = "";
 		List<String> mdfyAttrQries = new ArrayList<String>();
 		mdfyAttrQries.add(ALTER_TABLE + tableName + DROP_KEYWORD + COLUMN_KEYWORD
 				+ savedAttr.getColumnProperties().getName());
 
 		if (attribute.getIsNullable() && !savedAttr.getIsNullable())
 		{
 			nullQueryKeyword = WHITESPACE + NULL_KEYWORD + WHITESPACE;
 			nullQueryRlbkKeyword = WHITESPACE + NOT_KEYWORD + WHITESPACE + NULL_KEYWORD + WHITESPACE;
 		}
 		else if (!attribute.getIsNullable() && savedAttr.getIsNullable())
 		{
 			nullQueryKeyword = WHITESPACE + NOT_KEYWORD + WHITESPACE + NULL_KEYWORD + WHITESPACE;
 			nullQueryRlbkKeyword = WHITESPACE + NULL_KEYWORD + WHITESPACE;
 
 		}
 
 		// Added by: Kunal : Two more extra columns file name and content type 
 		// need to be added to the table.
 		if (attribute.getAttributeTypeInformation() instanceof FileAttributeTypeInformation)
 		{
 			mdfyAttrQuery = mdfyAttrQuery + COMMA
 					+ extraColumnQueryStringForFileAttribute(attribute);
 			mdfyAttrRlbkQry = mdfyAttrRlbkQry + COMMA
 					+ dropExtraColumnQueryStringForFileAttribute(attribute);
 		}
 
 		mdfyAttrQuery = mdfyAttrQuery + nullQueryKeyword;
 		mdfyAttrRlbkQry = mdfyAttrRlbkQry + nullQueryRlbkKeyword;
 		mdfyAttrRlbkQries.add(mdfyAttrRlbkQry);
 
 		mdfyAttrQuery += CLOSING_BRACKET;
 		mdfyAttrQries.add(mdfyAttrQuery);
 
 		return mdfyAttrQries;
 	}
 
 	/**
 	 * This method builds the query part for the newly added attribute.
 	 * @param attribute Newly added attribute in the entity.
 	 * @param attrRlbkQries This list is updated with the roll back queries for the actual queries.
 	 * @return String The actual query part for the new attribute.
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	protected String processAddAttribute(Attribute attribute, List<String> attrRlbkQries)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		String columnName = attribute.getColumnProperties().getName();
 		String tableName = attribute.getEntity().getTableProperties().getName();
 		String type = "";
 		String newAttrQuery = ALTER_TABLE + WHITESPACE + tableName + WHITESPACE + ADD_KEYWORD
 				+ WHITESPACE + OPENING_BRACKET + getQueryPartForAttribute(attribute, type, true);
 
 		String newAttrRlbkQry = ALTER_TABLE + WHITESPACE + tableName + WHITESPACE + DROP_KEYWORD
 				+ WHITESPACE + COLUMN_KEYWORD + WHITESPACE + OPENING_BRACKET + columnName;
 		if (attribute.getAttributeTypeInformation() instanceof FileAttributeTypeInformation)
 		{
 			newAttrQuery += COMMA + extraColumnQueryStringForFileAttribute(attribute);
 			newAttrRlbkQry += COMMA + dropExtraColumnQueryStringForFileAttribute(attribute);
 		}
 
 		newAttrQuery += CLOSING_BRACKET;
 		newAttrRlbkQry += CLOSING_BRACKET;
 		attrRlbkQries.add(newAttrRlbkQry);
 
 		return newAttrQuery;
 	}
 
 	/**
 	 * This method builds the query part for the newly added attribute.
 	 * @param attribute Newly added attribute in the entity.
 	 * @param attrRlbkQries This list is updated with the roll back queries for the actual queries.
 	 * @return String The actual query part for the new attribute.
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	protected String processAddAttribute(CategoryAttribute attribute, List<String> attrRlbkQries)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		String columnName = attribute.getColumnProperties().getName();
 		String tableName = attribute.getCategoryEntity().getTableProperties().getName();
 		String type = "";
 		String newAttrQuery = ALTER_TABLE + WHITESPACE + tableName + WHITESPACE + ADD_KEYWORD
 				+ WHITESPACE + getQueryPartForCategoryAttribute(attribute, type, true);
 
 		String newAttrRlbkQry = ALTER_TABLE + WHITESPACE + tableName + WHITESPACE + DROP_KEYWORD
 				+ WHITESPACE + COLUMN_KEYWORD + WHITESPACE + columnName;
 
 		attrRlbkQries.add(newAttrRlbkQry);
 
 		return newAttrQuery;
 	}
 
 	/**
 	 * This method constructs the query part for adding two 
 	 * extra columns when an attribute of type file is created.
 	 * @param attribute FileAttribute
 	 * @return queryString
 	 * @throws DynamicExtensionsSystemException
 	 */
 	private String extraColumnQueryStringForFileAttribute(Attribute attribute)
 			throws DynamicExtensionsSystemException
 	{
 		Attribute stringAttr = (Attribute) DomainObjectFactory.getInstance()
 				.createStringAttribute();
 
 		String query = attribute.getColumnProperties().getName() + UNDERSCORE + FILE_NAME
 				+ WHITESPACE + getDatabaseTypeAndSize(stringAttr) + COMMA + WHITESPACE
 				+ attribute.getColumnProperties().getName() + UNDERSCORE + CONTENT_TYPE
 				+ WHITESPACE + getDatabaseTypeAndSize(stringAttr);
 
 		return query;
 	}
 
 	/**
 	 * This method constructs the query part for dropping 
 	 * the extra columns created while creating an attribute of type file.
 	 * @param attribute FileAttribute
 	 * @return query string
 	 * @throws DynamicExtensionsSystemException
 	 */
 	private String dropExtraColumnQueryStringForFileAttribute(Attribute attribute)
 			throws DynamicExtensionsSystemException
 	{
 		String query = attribute.getColumnProperties().getName() + UNDERSCORE + FILE_NAME + COMMA
 				+ WHITESPACE + attribute.getColumnProperties().getName() + UNDERSCORE
 				+ CONTENT_TYPE;
 
 		return query;
 	}
 
 	/**
 	 * This method executes the queries which generate and or manipulate the data table associated with the entity.
 	 * @param entity Entity for which the data table queries are to be executed.
 	 * @param rlbkQryStack
 	 * @param reverseQueryList2
 	 * @param queryList2
 	 * @param hibernateDAO
 	 * @param session Hibernate Session through which connection is obtained to fire the queries.
 	 * @throws DynamicExtensionsSystemException Whenever there is any exception , this exception is thrown with proper message and the exception is
 	 * wrapped inside this exception.
 	 */
 	public Stack<String> executeQueries(List<String> queries, List<String> revQueries,
 			Stack<String> rlbkQryStack) throws DynamicExtensionsSystemException
 	{
 		Session session = null;
 		Transaction transaction = null;
 		Connection connection = null;
 
 		try
 		{
 			session = DBUtil.getCleanSession();
 			transaction = session.beginTransaction();
 		}
 		catch (BizLogicException e)
 		{
 			throw new DynamicExtensionsSystemException(
 					"Exception occured while getting the new session", e, DYEXTN_S_002);
 		}
 		catch (HibernateException e)
 		{
 			throw new DynamicExtensionsSystemException(
 					"Exception occured while getting the new transaction", e, DYEXTN_S_002);
 		}
 
 		Iterator<String> revQryIter = revQueries.iterator();
 
 		try
 		{
 			connection = session.connection();
 			if (queries != null && !queries.isEmpty())
 			{
 				Iterator<String> queryIter = queries.iterator();
 				while (queryIter.hasNext())
 				{
 					String query = queryIter.next();
 					System.out.println("Query: " + query);
 
 					PreparedStatement statement = null;
 					try
 					{
 						statement = connection.prepareStatement(query);
 					}
 					catch (SQLException e)
 					{
 						throw new DynamicExtensionsSystemException(
 								"Exception occured while executing the data table query", e);
 					}
 					try
 					{
 						statement.executeUpdate();
 						if (revQryIter.hasNext())
 						{
 							rlbkQryStack.push(revQryIter.next());
 						}
 					}
 					catch (SQLException e)
 					{
 						throw new DynamicExtensionsSystemException(
 								"Exception occured while forming the data tables for entity", e,
 								DYEXTN_S_002);
 					}
 					finally
 					{
 						try
 						{
 							statement.close();
 						}
 						catch (SQLException e)
 						{
 							e.printStackTrace();
 							throw new DynamicExtensionsSystemException(
 									"Exception occured while closing statement", e);
 						}
 					}
 				}
 			}
 		}
 		catch (HibernateException e)
 		{
 			throw new DynamicExtensionsSystemException(
 					"Cannot obtain connection to execute the data query", e, DYEXTN_S_001);
 		}
 		finally
 		{
 			try
 			{
 				transaction.commit();
 				session.close();
 			}
 			catch (HibernateException e)
 			{
 				throw new DynamicExtensionsSystemException(
 						"Exception occured while commiting transaction", e, DYEXTN_S_002);
 			}
 		}
 
 		return rlbkQryStack;
 	}
 
 	/**
 	 * @param query
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 */
 	public Object[] executeDMLQuery(String query) throws DynamicExtensionsSystemException
 	{
 		Session session = null;
 
 		try
 		{
 			session = DBUtil.currentSession();
 		}
 		catch (HibernateException e1)
 		{
 			throw new DynamicExtensionsSystemException(
 					"Unable to exectute the queries .....Cannot access connection from session",
 					e1, DYEXTN_S_002);
 		}
 		try
 		{
 			System.out.println("DMLQuery is : " + query);
 			Connection conn = session.connection();
 			Statement statement = null;
 			ResultSet resultSet = null;
 
 			try
 			{
 				statement = conn.createStatement();
 				resultSet = statement.executeQuery(query);
 
 				List<Object> objects = new ArrayList<Object>();
 				int count = 1;
 				while (resultSet.next())
 				{
 					objects.add(resultSet.getObject(count));
 				}
 
 				return objects.toArray();
 			}
 			catch (SQLException e)
 			{
 				throw new DynamicExtensionsSystemException(
 						"Exception occured while forming the data tables for entity", e,
 						DYEXTN_S_002);
 			}
 			finally
 			{
 				try
 				{
 					statement.close();
 				}
 				catch (SQLException e)
 				{
 					e.printStackTrace();
 					throw new DynamicExtensionsSystemException(
 							"Exception occured while closing statement", e);
 				}
 				if (resultSet != null)
 				{
 					try
 					{
 						resultSet.close();
 					}
 					catch (SQLException e)
 					{
 						throw new DynamicExtensionsSystemException(e.getMessage(), e);
 					}
 				}
 			}
 		}
 		catch (HibernateException e)
 		{
 			throw new DynamicExtensionsSystemException(
 					"Cannot obtain connection to execute the data query", e, DYEXTN_S_001);
 		}
 	}
 
 	/**
 	 * This method executes the query that selects record identifiers of the target entity 
 	 * that are associated to the source entity for a given association.
 	 * @param query
 	 * @return List of record identifiers of the target entity .
 	 * @throws DynamicExtensionsSystemException
 	 */
 	protected List<Long> getAssociationRecordValues(String query)
 			throws DynamicExtensionsSystemException
 	{
 		List<Long> assoRecords = new ArrayList<Long>();
 
 		Statement statement = null;
 		ResultSet resultSet = null;
 		try
 		{
 			Connection conn = DBUtil.getConnection();
 			statement = conn.createStatement();
 			resultSet = statement.executeQuery(query);
 
 			while (resultSet.next())
 			{
 				Long recordId = resultSet.getLong(1);
 				assoRecords.add(recordId);
 			}
 		}
 		catch (Exception e)
 		{
 			throw new DynamicExtensionsSystemException("Exception in query execution", e);
 		}
 		finally
 		{
 			try
 			{
 				resultSet.close();
 				statement.close();
 			}
 			catch (SQLException e)
 			{
 				throw new DynamicExtensionsSystemException("can not close result set", e);
 			}
 		}
 
 		return assoRecords;
 	}
 
 	/**
 	 * This method make sure the cardinality constraints are properly followed.
 	 * e.g for one to one association, it checks if target entity's record id is 
 	 * not associated to any other record.
 	 * source entity.
 	 * @param asso
 	 * @param srcRecId
 	 * @throws DynamicExtensionsApplicationException
 	 * @throws DynamicExtensionsSystemException
 	 */
 	protected void verifyCardinalityConstraints(AssociationInterface asso, Long srcRecId)
 			throws DynamicExtensionsApplicationException, DynamicExtensionsSystemException
 	{
 		EntityInterface tgtEnt = asso.getTargetEntity();
 		Cardinality srcMaxCard = asso.getSourceRole().getMaximumCardinality();
 		Cardinality tgtMaxCard = asso.getTargetRole().getMaximumCardinality();
 
 		String columnName = "";
 		String tableName = "";
 
 		if (tgtMaxCard == Cardinality.ONE && srcMaxCard == Cardinality.ONE)
 		{
 			tableName = tgtEnt.getTableProperties().getName();
 			columnName = asso.getConstraintProperties().getTargetEntityKey();
 
 			String query = SELECT_KEYWORD + WHITESPACE + COUNT_KEYWORD + OPENING_BRACKET + "*"
 					+ CLOSING_BRACKET + WHITESPACE + FROM_KEYWORD + WHITESPACE + tableName
 					+ WHITESPACE + WHERE_KEYWORD + WHITESPACE + columnName + WHITESPACE + EQUAL
 					+ WHITESPACE + srcRecId;
 
 			ResultSet resultSet = null;
 			try
 			{
 				resultSet = entityManagerUtil.executeQuery(query);
 				resultSet.next();
 
 				// If another source record is already using target record, throw exception.
 				if (resultSet.getInt(1) != 0)
 				{
 					throw new DynamicExtensionsApplicationException(
 							"Cardinality constraint violated", null, DYEXTN_A_005);
 				}
 			}
 			catch (SQLException e)
 			{
 				throw new DynamicExtensionsSystemException(e.getMessage(), e);
 			}
 			finally
 			{
 				if (resultSet != null)
 				{
 					try
 					{
 						resultSet.close();
 					}
 					catch (SQLException e)
 					{
 						throw new DynamicExtensionsSystemException(e.getMessage(), e);
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * This method make sure the cardinality constraints are properly followed.
 	 * e.g for one to one association, it checks if target entity's record id is 
 	 * not associated to any other record.
 	 * source entity.
 	 * @param catAsso
 	 * @param srcRecId
 	 * @throws DynamicExtensionsApplicationException
 	 * @throws DynamicExtensionsSystemException
 	 */
 	protected void verifyCardinalityConstraints(CategoryAssociationInterface catAsso, Long srcRecId)
 			throws DynamicExtensionsApplicationException, DynamicExtensionsSystemException
 	{
 		CategoryEntityInterface catEntity = catAsso.getTargetCategoryEntity();
 		String columnName = "";
 		String tableName = "";
 
 		if (catEntity.getNumberOfEntries() == 1)
 		{
 			tableName = catEntity.getTableProperties().getName();
 			columnName = catAsso.getConstraintProperties().getTargetEntityKey();
 
 			String query = SELECT_KEYWORD + WHITESPACE + COUNT_KEYWORD + OPENING_BRACKET + "*"
 					+ CLOSING_BRACKET + WHITESPACE + FROM_KEYWORD + WHITESPACE + tableName
 					+ WHITESPACE + WHERE_KEYWORD + WHITESPACE + columnName + WHITESPACE + EQUAL
 					+ WHITESPACE + srcRecId;
 
 			ResultSet resultSet = null;
 			try
 			{
 				entityManagerUtil.executeQuery(query);
 				resultSet.next();
 				// If another source record is already using target record, throw exception.
 				if (resultSet.getInt(1) != 0)
 				{
 					throw new DynamicExtensionsApplicationException(
 							"Cardinality constraint violated", null, DYEXTN_A_005);
 				}
 			}
 			catch (SQLException e)
 			{
 				throw new DynamicExtensionsSystemException(e.getMessage(), e);
 			}
 			finally
 			{
 				if (resultSet != null)
 				{
 					try
 					{
 						resultSet.close();
 					}
 					catch (SQLException e)
 					{
 						throw new DynamicExtensionsSystemException(e.getMessage(), e);
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * @param inputs
 	 * @return
 	 */
 	private String getListToString(List<Long> inputs)
 	{
 		String query = inputs.toString();
 		query = query.replace("[", OPENING_BRACKET);
 		query = query.replace("]", CLOSING_BRACKET);
 
 		return query;
 	}
 
 	/**
 	 * @param entity
 	 * @param dbaseCopy
 	 * @return
 	 */
 	public boolean isParentChanged(Entity entity, Entity dbaseCopy)
 	{
 		boolean isParentChanged = false;
 		if (entity.getParentEntity() != null
 				&& !entity.getParentEntity().equals(dbaseCopy.getParentEntity()))
 		{
 			isParentChanged = true;
 		}
 		else if (entity.getParentEntity() == null && dbaseCopy.getParentEntity() != null)
 		{
 			isParentChanged = true;
 		}
 
 		return isParentChanged;
 	}
 
 	/**
 	 * @param entity
 	 * @param dbaseParentId
 	 * @return
 	 */
 	public boolean isParentChanged(Entity entity, Long dbaseParentId)
 	{
 		boolean isParentChanged = false;
 		if (entity.getParentEntity() != null
 				&& !entity.getParentEntity().getId().equals(dbaseParentId))
 		{
 			isParentChanged = true;
 		}
 		else if (entity.getParentEntity() == null && dbaseParentId != null)
 		{
 			isParentChanged = true;
 		}
 
 		return isParentChanged;
 	}
 
 	/**
 	 * @param catEntity
 	 * @param dbaseCopy
 	 * @return
 	 */
 	public boolean isParentChanged(CategoryEntity catEntity, CategoryEntity dbaseCopy)
 	{
 		boolean isParentChanged = false;
 		if (catEntity.getParentCategoryEntity() != null
 				&& !catEntity.getParentCategoryEntity().equals(dbaseCopy.getParentCategoryEntity()))
 		{
 			isParentChanged = true;
 		}
 		else if (catEntity.getParentCategoryEntity() == null
 				&& dbaseCopy.getParentCategoryEntity() != null)
 		{
 			isParentChanged = true;
 		}
 
 		return isParentChanged;
 	}
 
 	/**
 	 *
 	 * @param attribute
 	 * @param value
 	 * @return
 	 * @throws DynamicExtensionsSystemException 
 	 */
 	public Object getFormattedValue(AbstractAttribute attribute, Object value)
 			throws DynamicExtensionsSystemException
 	{
 		String frmtedValue = null;
 		if (attribute == null)
 		{
 			throw new DynamicExtensionsSystemException("Attribute is null");
 		}
 
 		AttributeTypeInformationInterface attrTypInfo = ((Attribute) attribute)
 				.getAttributeTypeInformation();
 
 		if (attrTypInfo instanceof StringAttributeTypeInformation)
 		{
 			// Quick fix.
 			if (value instanceof List)
 			{
 				if (((List) value).size() > 0)
 				{
 					frmtedValue = "'"
 							+ DynamicExtensionsUtility
 									.getEscapedStringValue((String) ((List) value).get(0)) + "'";
 				}
 			}
 			else
 			{
 				frmtedValue = "'" + DynamicExtensionsUtility.getEscapedStringValue((String) value)
 						+ "'";
 			}
 		}
 		else if (attrTypInfo instanceof DateAttributeTypeInformation)
 		{
 			String dateFormat = ((DateAttributeTypeInformation) attrTypInfo).getFormat();
 			if (dateFormat == null)
 			{
 				dateFormat = Constants.DATE_PATTERN_MM_DD_YYYY;
 			}
 
 			String str = null;
 			if (value instanceof Date)
 			{
 				str = Utility.parseDateToString(((Date) value), dateFormat);
 			}
 			else
 			{
 				str = (String) value;
 			}
 
 			if (dateFormat.equals(ProcessorConstants.MONTH_YEAR_FORMAT))
 			{
 				if (str.length() != 0)
 				{
 					str = DynamicExtensionsUtility.formatMonthAndYearDate(str);
 					if (Variables.databaseName.equals(Constants.ORACLE_DATABASE))
 					{
 						str = str.substring(0, str.length() - 4);
 					}
 				}
 			}
 
 			if (dateFormat.equals(ProcessorConstants.YEAR_ONLY_FORMAT))
 			{
 				if (str.length() != 0)
 				{
 					str = DynamicExtensionsUtility.formatYearDate(str);
 					if (Variables.databaseName.equals(Constants.ORACLE_DATABASE))
 					{
 						str = str.substring(0, str.length() - 4);
 					}
 				}
 			}
 			// For MySQL5 if user does not enter any value for date field, it gets saved as 00-00-0000,
 			// which is throwing exception so to avoid it store null value in database.
 			if (Variables.databaseName.equals(Constants.MYSQL_DATABASE) && str.trim().length() == 0)
 			{
 				frmtedValue = null;
 			}
 			else
 			{
 				frmtedValue = Variables.strTodateFunction + "('" + str.trim() + "','"
 						+ DynamicExtensionsUtility.getSQLDateFormat(dateFormat) + "')";
 			}
 		}
 		else
 		{
 			// Quick fix.
 			if (value instanceof List)
 			{
 				if (((List) value).size() > 0)
 				{
 					frmtedValue = ((List) value).get(0).toString();
 				}
 			}
 			else
 			{
 				frmtedValue = value.toString();
 			}
 
 			// In case of MySQL5, if the column data type is one of double, float or integer, 
 			// then it is not possible to pass '' as  a value in insert-update query so pass null as value.
 			if (Variables.databaseName.equals(Constants.MYSQL_DATABASE))
 			{
 				if (attrTypInfo instanceof NumericAttributeTypeInformation)
 				{
 					if (frmtedValue.trim().length() == 0)
 					{
 						frmtedValue = null;
 					}
 				}
 				else if (attrTypInfo instanceof BooleanAttributeTypeInformation)
 				{
 					if ("false".equals(frmtedValue))
 					{
 						frmtedValue = "0";
 					}
 					else
 					{
 						frmtedValue = "1";
 					}
 				}
 			}
 
 			if (frmtedValue != null)
 			{
 				frmtedValue = "'" + frmtedValue + "'";
 			}
 		}
 
 		Logger.out.debug("getFormattedValue The formatted value for attribute "
 				+ attribute.getName() + "is " + frmtedValue);
 
 		return frmtedValue;
 	}
 
 	/**
 	 * @param attribute
 	 * @param value
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 */
 	public boolean isValuePresent(AttributeInterface attribute, Object value)
 			throws DynamicExtensionsSystemException
 	{
 		boolean isPresent = false;
 
 		String tableName = attribute.getEntity().getTableProperties().getName();
 		String columnName = attribute.getColumnProperties().getName();
 		Object frmtedValue = QueryBuilderFactory.getQueryBuilder().getFormattedValue(
 				(AbstractAttribute) attribute, value);
 
 		StringBuffer query = new StringBuffer();
 		query.append(SELECT_KEYWORD).append(WHITESPACE).append(COUNT_KEYWORD).append(
 				OPENING_BRACKET).append(ASTERIX).append(CLOSING_BRACKET).append(WHITESPACE).append(
 				FROM_KEYWORD).append(WHITESPACE).append(tableName).append(WHITESPACE).append(
 				WHERE_KEYWORD).append(WHITESPACE).append(columnName).append(EQUAL).append(
 				frmtedValue).append(" and " + getRemoveDisbledRecordsQuery(""));
 
 		ResultSet resultSet = null;
 		try
 		{
 			resultSet = EntityManagerUtil.executeQuery(query.toString());
 			resultSet.next();
 			Long count = resultSet.getLong(1);
 			if (count > 0)
 			{
 				isPresent = true;
 			}
 		}
 		catch (SQLException e)
 		{
 			throw new DynamicExtensionsSystemException("Can not check the availability of value", e);
 		}
 		finally
 		{
 			if (resultSet != null)
 			{
 				try
 				{
 					resultSet.close();
 					DBUtil.closeConnection();
 				}
 				catch (SQLException e)
 				{
 					throw new DynamicExtensionsSystemException(e.getMessage(), e);
 				}
 			}
 		}
 
 		return isPresent;
 	}
 
 	/**
 	 * @param tableName
 	 * @return
 	 */
 	public static String getRemoveDisbledRecordsQuery(String tableName)
 	{
 		String prefix = "";
 		if (tableName != null && !tableName.equals(""))
 		{
 			prefix = tableName + ".";
 		}
 
 		return " " + prefix + Constants.ACTIVITY_STATUS_COLUMN + " <> '"
 				+ Constants.ACTIVITY_STATUS_DISABLED + "' ";
 	}
 
 	/**
 	 * @param asso
 	 * @param srcEntRecId
 	 * @param tgtEntRecId
 	 * @throws DynamicExtensionsSystemException
 	 */
 	public static void associateRecords(AssociationInterface asso, Long srcEntRecId,
 			Long tgtEntRecId) throws DynamicExtensionsSystemException
 	{
 		RoleInterface srcRole = asso.getSourceRole();
 		RoleInterface tgtRole = asso.getTargetRole();
 
 		Cardinality srcMaxCard = srcRole.getMaximumCardinality();
 		Cardinality tgtMaxCard = tgtRole.getMaximumCardinality();
 
 		ConstraintPropertiesInterface constraint = asso.getConstraintProperties();
 
 		String tableName = DynamicExtensionsUtility.getTableName(asso);
 
 		StringBuffer query = new StringBuffer();
 		query.append(UPDATE_KEYWORD).append(tableName).append(SET_KEYWORD);
 
 		if (srcMaxCard == Cardinality.MANY && tgtMaxCard == Cardinality.MANY)
 		{
 			query = new StringBuffer();
 			query.append(INSERT_INTO_KEYWORD).append(tableName).append(OPENING_BRACKET).append(
 					constraint.getSourceEntityKey()).append(COMMA).append(
 					constraint.getTargetEntityKey()).append(CLOSING_BRACKET).append("values")
 					.append(OPENING_BRACKET).append(srcEntRecId).append(COMMA).append(tgtEntRecId)
 					.append(CLOSING_BRACKET);
 		}
 		else if (srcMaxCard == Cardinality.MANY && tgtMaxCard == Cardinality.ONE)
 		{
 			query.append(constraint.getSourceEntityKey()).append(EQUAL).append(tgtEntRecId).append(
 					WHERE_KEYWORD).append(IDENTIFIER).append(EQUAL).append(srcEntRecId);
 		}
 		else
 		{
 			query.append(constraint.getTargetEntityKey()).append(EQUAL).append(srcEntRecId).append(
 					WHERE_KEYWORD).append(IDENTIFIER).append(EQUAL).append(tgtEntRecId);
 		}
 
 		Connection conn = null;
 		try
 		{
 			conn = DBUtil.getConnection();
 			Statement stmt = conn.createStatement();
 			stmt.execute(query.toString());
 			conn.commit();
 		}
 		catch (HibernateException e)
 		{
 			connectionRollBack(conn);
 		}
 		catch (SQLException e)
 		{
 			connectionRollBack(conn);
 		}
 		finally
 		{
 			DBUtil.closeConnection();
 		}
 	}
 
 	/**
 	 * This method rolls back the connection.
 	 * @param connection
 	 * @throws DynamicExtensionsSystemException
 	 */
 	private static void connectionRollBack(Connection connection)
 			throws DynamicExtensionsSystemException
 	{
 		try
 		{
 			connection.rollback();
 		}
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 		}
 
 		throw new DynamicExtensionsSystemException("Can not execute query");
 	}
 
 	/**
 	 * This method generates the alter table query to drop columns.
 	 * @param tableName
 	 * @param columnNames
 	 * @return String altered query
 	 */
 	protected String getDropColumnQuery(String tableName, List<String> columnNames)
 	{
 		StringBuffer alterQuery = new StringBuffer();
 
 		alterQuery.append(ALTER_TABLE);
 		alterQuery.append(tableName);
 		alterQuery.append(WHITESPACE);
 		alterQuery.append(DROP_KEYWORD);
 		alterQuery.append(OPENING_BRACKET);
 
 		for (int i = 0; i < columnNames.size(); i++)
 		{
 			alterQuery.append(columnNames.get(i));
 			if (i != columnNames.size() - 1)
 			{
 				alterQuery.append(COMMA);
 			}
 		}
 
 		alterQuery.append(CLOSING_BRACKET);
 
 		return alterQuery.toString();
 	}
 
 }
