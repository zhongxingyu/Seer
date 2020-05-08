 
 package edu.common.dynamicextensions.entitymanager;
 
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.sql.Blob;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.List;
 
 import edu.common.dynamicextensions.dao.impl.DynamicExtensionDAO;
 import edu.common.dynamicextensions.domain.AbstractAttribute;
 import edu.common.dynamicextensions.domain.Attribute;
 import edu.common.dynamicextensions.domain.BooleanAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.DateAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.DomainObjectFactory;
 import edu.common.dynamicextensions.domain.FileAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.StringAttributeTypeInformation;
 import edu.common.dynamicextensions.domaininterface.AttributeInterface;
 import edu.common.dynamicextensions.domaininterface.AttributeTypeInformationInterface;
 import edu.common.dynamicextensions.domaininterface.CategoryAttributeInterface;
 import edu.common.dynamicextensions.exception.DynamicExtensionsApplicationException;
 import edu.common.dynamicextensions.exception.DynamicExtensionsSystemException;
 import edu.common.dynamicextensions.processor.ProcessorConstants;
 import edu.common.dynamicextensions.util.DynamicExtensionsUtility;
 import edu.wustl.common.util.Utility;
 import edu.wustl.common.util.global.CommonServiceLocator;
 import edu.wustl.common.util.logger.Logger;
 import edu.wustl.dao.JDBCDAO;
 import edu.wustl.dao.daofactory.DAOConfigFactory;
 import edu.wustl.dao.daofactory.IDAOFactory;
 import edu.wustl.dao.exception.DAOException;
 
 /**
  * This class does the specific work according to db2.
  * @author pavan_kalantri
  *
  */
 public class DynamicExtensionDb2QueryBuilder extends DynamicExtensionBaseQueryBuilder
 {
 
 	/**
 	 * This method builds the query part for the primitive attribute
 	 * @param attribute primitive attribute for which to build the query.
 	 * @return String query part of the primitive attribute.
 	 * @throws DynamicExtensionsSystemException
 	 */
 	protected String getQueryPartForAttribute(Attribute attribute, String type,
 			boolean processConstraints) throws DynamicExtensionsSystemException
 	{
 
 		String attributeQuery = null;
 		if (attribute != null)
 		{
 
 			String columnName = attribute.getColumnProperties().getName();
 			String nullConstraint = "";
 			// Dead code removed because no need to keep it there.
 
 			if (processConstraints && !attribute.getIsNullable())
 			{
 				nullConstraint = "NOT NULL";
 			}
 
 			attributeQuery = columnName + WHITESPACE + type + WHITESPACE
 					+ getDatabaseTypeAndSize(attribute) //+ WHITESPACE + defaultConstraint
 					+ WHITESPACE + nullConstraint;
 		}
 		return attributeQuery;
 	}
 
 	/**
 	 * This method builds the query part for the primitive attribute
 	 * @param attribute primitive attribute for which to build the query.
 	 * @return String query part of the primitive attribute.
 	 * @throws DynamicExtensionsSystemException
 	 */
 	protected String getQueryPartForCategoryAttribute(CategoryAttributeInterface attribute,
 			String type, boolean processConstraints) throws DynamicExtensionsSystemException
 	{
 		String attributeQuery = null;
 		if (attribute != null)
 		{
 			String columnName = attribute.getColumnProperties().getName();
 			String nullConstraint = "";
 			//dead code Removed
 			attributeQuery = columnName + WHITESPACE + type + WHITESPACE
 					+ getDataTypeForIdentifier() + WHITESPACE + nullConstraint;
 		}
 		return attributeQuery;
 	}
 
 	/**
 	 * 
 	 */
 	public Object getFormattedValue(AbstractAttribute attribute, Object value)
 	{
 
 		String formattedvalue = null;
 		AttributeTypeInformationInterface attributeInformation = ((Attribute) attribute)
 				.getAttributeTypeInformation();
 		if (attribute == null)
 		{
 			formattedvalue = null;
 		}
 
 		else if (attributeInformation instanceof StringAttributeTypeInformation)
 		{
 			// quick fix.
 			if (value instanceof List)
 			{
 				if (((List) value).size() > 0)
 				{
 					formattedvalue = "'"
 							+ DynamicExtensionsUtility
 									.getEscapedStringValue((String) ((List) value).get(0)) + "'";
 				}
 			}
 			else
 			{
 				formattedvalue = "'"
 						+ DynamicExtensionsUtility.getEscapedStringValue((String) value) + "'";
 			}
 		}
 		else if (attributeInformation instanceof DateAttributeTypeInformation)
 		{
 			String dateFormat = ((DateAttributeTypeInformation) attributeInformation).getFormat();
 			if (dateFormat == null)
 			{
 				dateFormat = CommonServiceLocator.getInstance().getDatePattern();
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
 
 			if (dateFormat.equals(ProcessorConstants.MONTH_YEAR_FORMAT) && str.length() != 0)
 			{
 				str = DynamicExtensionsUtility.formatMonthAndYearDate(str,false);
 			}
 
 			if (dateFormat.equals(ProcessorConstants.YEAR_ONLY_FORMAT) && str.length() != 0)
 			{
 				
 				str = DynamicExtensionsUtility.formatYearDate(str,false);
 
 			}
 			// if user not enter any value for date field its getting saved as 00-00-0000 ,which is throwing exception
 			//So to avoid it store null value in database
 			if (str.trim().length() == 0)
 			{
 				formattedvalue = null;
 
 			}
 			else
 			{
 				String appName=DynamicExtensionDAO.getInstance().getAppName();
 				IDAOFactory factory = DAOConfigFactory.getInstance().getDAOFactory(appName);
 				JDBCDAO jdbcDAO;
 				try
 				{
 					jdbcDAO = (JDBCDAO) factory.getJDBCDAO();
 					formattedvalue = jdbcDAO.getStrTodateFunction() + "('" + str.trim() + "','"
 					+ DynamicExtensionsUtility.getSQLDateFormat(dateFormat) + "')";
 				}
 				catch (DAOException e)
 				{
 					Logger.out.error(e.getMessage());
 				}
 			}
 		}
 		else
 		{
 			// quick fix.
 			if (value instanceof List)
 			{
 				if (((List) value).size() > 0)
 				{
 					formattedvalue = ((List) value).get(0).toString();
 				}
 			}
 			else
 			{
 				formattedvalue = value.toString();
 			}
 
 			//In case of DB2 ,if the column datatype double ,float ,integer then its not possible to pass '' as  in insert-update query
 			//so instead pass null as value.
 			if (attributeInformation instanceof BooleanAttributeTypeInformation)
 			{
 				if ("false".equals(formattedvalue))
 				{
 					formattedvalue = "0";
 				}
 				else
 				{
 					formattedvalue = "1";
 				}
 			}
 			else if (formattedvalue != null && formattedvalue.trim().length() == 0)
 			{
 				formattedvalue = null;
 
 			}
 
 		}
 		Logger.out.debug("getFormattedValue The formatted value for attribute "
 				+ attribute.getName() + "is " + formattedvalue);
 		return formattedvalue;
 	}
 
 	/**
 	 *This method create the query for altering the column of given attribute to add not null constraint on it
 	 *@param attribute on which the constraint is to be applied
 	 *@return query 
 	 */
 	protected String addNotNullConstraintQuery(AttributeInterface attribute)
 			throws DynamicExtensionsSystemException
 	{
 		/*StringBuffer query=new StringBuffer();
 		String tableName=attribute.getEntity().getTableProperties().getName();
 		String columnName=attribute.getColumnProperties().getName();
 		query.append(ALTER_TABLE).append(WHITESPACE).append(tableName).append(WHITESPACE).append(ALTER_COLUMN_KEYWORD).append(WHITESPACE)
 		.append(columnName).append(WHITESPACE).append(SET_KEYWORD).append(WHITESPACE).append(NOT_KEYWORD).append(WHITESPACE).append(NULL_KEYWORD);
 		return query.toString();*/
 		return null;
 	}
 
 	/**
 	 *This method create the query for altering the column of given attribute to add null constraint on it
 	 *@param attribute on which the constraint is to be applied
 	 *@return query 
 	 */
 	protected String dropNotNullConstraintQuery(AttributeInterface attribute)
 			throws DynamicExtensionsSystemException
 	{
 		/*	StringBuffer query=new StringBuffer();
 			String tableName=attribute.getEntity().getTableProperties().getName();
 			String columnName=attribute.getColumnProperties().getName();
 			query.append(ALTER_TABLE).append(WHITESPACE).append(tableName).append(WHITESPACE).append(ALTER_COLUMN_KEYWORD).append(WHITESPACE)
 			.append(columnName).append(WHITESPACE).append(SET_KEYWORD).append(WHITESPACE).append(NULL_KEYWORD);
 			return query.toString();*/
 		return null;
 	}
 
 	/**
 	 * @param tableName
 	 * @param columnName
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 */
 	public boolean isDataPresent(String tableName, Attribute savedAttribute)
 			throws DynamicExtensionsSystemException
 	{
 		boolean dataPresent = false;
 		StringBuffer queryBuffer = new StringBuffer();
 		if (!isAttributeColumnToBeExcluded(savedAttribute))
 		{
 			queryBuffer.append(SELECT_KEYWORD).append(WHITESPACE).append("COUNT").append(
 					OPENING_BRACKET).append(ASTERIX).append(CLOSING_BRACKET).append(WHITESPACE)
 					.append(FROM_KEYWORD).append(WHITESPACE).append(tableName).append(WHITESPACE)
 					.append(WHERE_KEYWORD).append(WHITESPACE).append(
 							savedAttribute.getColumnProperties().getName()).append(WHITESPACE)
 					.append("IS").append(WHITESPACE).append(NOT_KEYWORD).append(WHITESPACE).append(
 							NULL_KEYWORD);
 			ResultSet resultSet = null;
 			JDBCDAO jdbcDao = null;
 			try
 			{
 				jdbcDao=DynamicExtensionsUtility.getJDBCDAO();
 				resultSet=jdbcDao.getQueryResultSet(queryBuffer.toString());
 				resultSet.next();
 				Long count = resultSet.getLong(1);
 				if (count > 0)
 				{
 					dataPresent = true;
 				}
 			}
 			catch (DAOException e)
 			{
 				throw new DynamicExtensionsSystemException(
 						"Can not check the availability of data", e);
 			}
 			catch (SQLException e)
 			{
 				throw new DynamicExtensionsSystemException(
 						"Can not check the availability of data", e);
 			}
 			finally
 			{
 				try
 				{
 					DynamicExtensionsUtility.closeJDBCDAO(jdbcDao);
 				}
 				catch (DAOException e)
 				{
 					throw new DynamicExtensionsSystemException(e.getMessage(), e);
 				}
 				
 			}
 		}
 		else
 		{
 			Collection<Integer> recordCollection = EntityManager.getInstance()
 					.getAttributeRecordsCount(savedAttribute.getEntity().getId(),
 							savedAttribute.getId());
 			if (recordCollection != null && !recordCollection.isEmpty())
 			{
 				Integer count = (Integer) recordCollection.iterator().next();
 				if (count > 0)
 				{
 					dataPresent = true;
 				}
 			}
 		}
 		return dataPresent;
 	}
 
 	/**
 	 * This method returns the query for the attribute to modify its data type.
 	 * @param attribute
 	 * @param savedAttribute
 	 * @param modifyAttributeRollbackQuery
 	 * @param tableName
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 */
 	protected List<String> getAttributeDataTypeChangedQuery(Attribute attribute,
 			Attribute savedAttribute, List mdfyAttRbkQryLst)
 			throws DynamicExtensionsSystemException
 	{
 		String tableName = attribute.getEntity().getTableProperties().getName();
 		String type = "";
 		String mdfyAttrRlbkQry = "";
 
 		String mdfAttrQry = getQueryPartForAttribute(attribute, type, false);
 		mdfAttrQry = ALTER_TABLE + tableName + ADD_KEYWORD + mdfAttrQry;
 
 		mdfyAttrRlbkQry = getQueryPartForAttribute(savedAttribute, type, false);
 		mdfyAttrRlbkQry = ALTER_TABLE + WHITESPACE + tableName + WHITESPACE + MODIFY_KEYWORD
 				+ WHITESPACE + mdfyAttrRlbkQry;
 
 		String nullQueryKeyword = "";
 		String nullQueryRollbackKeyword = "";
 		List<String> mdfyAttrQryLst = new ArrayList<String>();
 		mdfyAttrQryLst.add(ALTER_TABLE + tableName + DROP_KEYWORD + COLUMN_KEYWORD
 				+ savedAttribute.getColumnProperties().getName());
 
 		if (attribute.getIsNullable() && !savedAttribute.getIsNullable())
 		{
 			nullQueryKeyword = WHITESPACE + NULL_KEYWORD + WHITESPACE;
 			nullQueryRollbackKeyword = WHITESPACE + NOT_KEYWORD + WHITESPACE + NULL_KEYWORD
 					+ WHITESPACE;
 		}
 		else if (!attribute.getIsNullable() && savedAttribute.getIsNullable())
 		{
 			nullQueryKeyword = WHITESPACE + NOT_KEYWORD + WHITESPACE + NULL_KEYWORD + WHITESPACE;
 			nullQueryRollbackKeyword = WHITESPACE + NULL_KEYWORD + WHITESPACE;
 
 		}
 		if (attribute.getAttributeTypeInformation() instanceof FileAttributeTypeInformation)
 		{
 
 			mdfAttrQry = mdfAttrQry + extraColumnQueryStringForFileAttribute(attribute);
 			mdfyAttrRlbkQry = mdfyAttrRlbkQry
 					+ dropExtraColumnQueryStringForFileAttribute(attribute);
 
 		}
 		mdfAttrQry = mdfAttrQry + nullQueryKeyword;
 		mdfyAttrRlbkQry = mdfyAttrRlbkQry + nullQueryRollbackKeyword;
 		mdfyAttRbkQryLst.add(mdfyAttrRlbkQry);
 
 		mdfyAttrQryLst.add(mdfAttrQry);
 
 		return mdfyAttrQryLst;
 	}
 
 	/**
 	 * This method contrsucts the query part for adding two extra columns when
 	 * an attribute of type File is created in existing entity
 	 * @param attribute FileAttribute
 	 * @return queryString
 	 * @throws DynamicExtensionsSystemException
 	 */
 	private String extraColumnQueryStringForFileAttributeInEditCase(Attribute attribute)
 			throws DynamicExtensionsSystemException
 	{
 		Attribute stringAttribute = (Attribute) DomainObjectFactory.getInstance()
 				.createStringAttribute();
 		String queryString = ADD_KEYWORD + WHITESPACE + attribute.getName() + UNDERSCORE
 				+ FILE_NAME + WHITESPACE + getDatabaseTypeAndSize(stringAttribute) + WHITESPACE
 				+ ADD_KEYWORD + WHITESPACE + attribute.getName() + UNDERSCORE + CONTENT_TYPE
 				+ WHITESPACE + getDatabaseTypeAndSize(stringAttribute);
 		return queryString;
 	}
 
 	/**
 	 * This method constructs the query part for dropping the extra columns
 	 * created while creating an attribute of type File
 	 * @param attribute FileAttribute
 	 * @return queryString
 	 * @throws DynamicExtensionsSystemException
 	 */
 
 	private String dropExtraColumnQueryStringForFileAttributeInEditCase(Attribute attribute)
 			throws DynamicExtensionsSystemException
 	{
 		String queryString = DROP_KEYWORD + WHITESPACE + attribute.getName() + UNDERSCORE
 				+ FILE_NAME + WHITESPACE + DROP_KEYWORD + attribute.getName() + UNDERSCORE
 				+ CONTENT_TYPE;
 		return queryString;
 	}
 
 	/**
 	 * This method builds the query part for the newly added attribute.
 	 * @param attribute Newly added attribute in the entity.
 	 * @param attributeRollbackQueryList This list is updated with the rollback queries for the actual queries.
 	 * @return List<String> The actual query list for the new attribute.
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	protected String processAddAttribute(Attribute attribute, List attributeRollbackQueryList)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		String columnName = attribute.getColumnProperties().getName();
 		String tableName = attribute.getEntity().getTableProperties().getName();
 		String type = "";
 
 		String newAttributeQuery = ALTER_TABLE + WHITESPACE + tableName + WHITESPACE + ADD_KEYWORD
 				+ WHITESPACE + getQueryPartForAttribute(attribute, type, true);
 
 		String newAttributeRollbackQuery = ALTER_TABLE + WHITESPACE + tableName + WHITESPACE
 				+ DROP_KEYWORD + WHITESPACE + COLUMN_KEYWORD + WHITESPACE + columnName;
 		if (attribute.getAttributeTypeInformation() instanceof FileAttributeTypeInformation)
 		{
 
 			newAttributeQuery += extraColumnQueryStringForFileAttributeInEditCase(attribute);
 			newAttributeRollbackQuery += dropExtraColumnQueryStringForFileAttributeInEditCase(attribute);
 
 		}
 		
 		attributeRollbackQueryList.add(newAttributeRollbackQuery);
 		return newAttributeQuery;
 	}
 
 	
 	
 	/**
 	 * Converts Blob data type to Object data type for db2 database 
 	 * @param valueObj
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 */
 	protected Object convertValueToObject(Object valueObj) throws DynamicExtensionsSystemException
 	{
 		Object value = "";
 
 		Blob blob = (Blob) valueObj;
 		try
 		{
 			value = new ObjectInputStream(blob.getBinaryStream()).readObject();
 		}
 		catch (SQLException e)
 		{
 			throw new DynamicExtensionsSystemException("Error while converting the data", e);
 		}
 		catch (IOException e)
 		{
 			throw new DynamicExtensionsSystemException("Error while converting the data", e);
 		}
 		catch (ClassNotFoundException e)
 		{
 			throw new DynamicExtensionsSystemException("Error while converting the data", e);
 		}
 		return value;
 	}
 
 }
 	
 
 
