 /**
  *<p>Title: </p>
  *<p>Description:  </p>
  *<p>Copyright:TODO</p>
  *@author 
  *@version 1.0
  */
 
 package edu.common.dynamicextensions.DEIntegration;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import net.sf.ehcache.CacheException;
 
 import org.hibernate.Session;
 
 import edu.common.dynamicextensions.entitymanager.EntityManager;
 import edu.common.dynamicextensions.entitymanager.EntityManagerUtil;
 import edu.common.dynamicextensions.exception.DynamicExtensionsApplicationException;
 import edu.common.dynamicextensions.exception.DynamicExtensionsSystemException;
 import edu.wustl.common.exception.BizLogicException;
 import edu.wustl.common.util.dbManager.DAOException;
 import edu.wustl.common.util.dbManager.DBUtil;
 import edu.common.dynamicextensions.util.global.Constants;
 
 /**
  * 
  * @author shital_lawhale
  *
  */
 public class DEIntegration implements IntegrationInterface
 {
 
     private static Map entityMap = new HashMap();
     private static Map categoryEntityMap = new HashMap();
 
     public Long addAssociation(Long hookEntityId, Long dynamicEntityId,
             boolean isEntityFromXmi, boolean isCategory)
             throws DynamicExtensionsApplicationException,
             DynamicExtensionsSystemException, BizLogicException
     {
         return null;
     }
 
     /**
      *This method returns the Category Record Id BasedOn HookEntityRecId 
      *Steps:
      *1) it will search for root enity of this category entity 
      *2) and then it will return record id of category based on root entity rec id 
      */
     public Collection getCategoryRecIdBasedOnHookEntityRecId(
             Long categoryContainerId, Long staticRecId, Long hookEntityId)
             throws DynamicExtensionsSystemException, SQLException
     {
 
         Collection catRecIds = new HashSet();
 
         String entityTableName = "";
         String columnName = "";
         String catTableName = "";//EntityManager.getInstance().getDynamicTableName(
         //  categoryContainerId);
         Long enitityContainerId = null;
 
         if (categoryEntityMap.containsKey(categoryContainerId.toString()))
         {
             enitityContainerId = (Long) categoryEntityMap
                     .get(categoryContainerId.toString());
         }
 
         else
         {
             enitityContainerId = EntityManager.getInstance()
                     .getCategoryRootContainerId(categoryContainerId);
             categoryEntityMap.put(categoryContainerId.toString(),
                     enitityContainerId);
         }
 
         if (entityMap.containsKey(enitityContainerId.toString()))
         {
             entityTableName = (String) entityMap.get(enitityContainerId
                     .toString());
             columnName = (String) entityMap
                     .get(hookEntityId + "_" + enitityContainerId);
            catTableName = (String) entityMap.get(categoryContainerId
                    .toString());
         }
 
         else
         //if(entityTableName==null)
         {
             entityTableName = EntityManager.getInstance().getDynamicTableName(
                     enitityContainerId);
             columnName = EntityManager.getInstance()
                     .getColumnNameForAssociation(hookEntityId,
                             enitityContainerId);
             catTableName = EntityManager.getInstance().getDynamicTableName(
                     categoryContainerId);
 
             entityMap.put(enitityContainerId.toString(), entityTableName);
             entityMap.put(hookEntityId + "_" + enitityContainerId,
                     columnName);
             entityMap.put(categoryContainerId.toString(), catTableName);
 
         }
         Session session = null;
         ResultSet resultSet = null;
         Statement statement = null;
         Connection connection = null;
         try
         {
             session = DBUtil.getCleanSession();
             connection = session.connection();
             statement = connection.createStatement();
 
             String entitySql = "select identifier from " + entityTableName
                     + " where " + columnName + " = " + staticRecId;
             //EntityManagerUtil util = new EntityManagerUtil();
             resultSet = statement.executeQuery(entitySql.toString()); //util.executeQuery(entitySql);
             List recIdList = new ArrayList();
             while (resultSet.next())
             {
                 recIdList.add(resultSet.getLong(1));
             }
             Iterator recIt = recIdList.iterator();
             while (recIt.hasNext())
             {
                 String catSql = "select identifier from " + catTableName
                         + " where CATEGORY_ROOT_ID= " + recIt.next();
                 ResultSet catRecResultSet = null;
                 PreparedStatement preparedStmt = null;
                 try
                 {
                     preparedStmt = connection.prepareStatement(catSql);
                     catRecResultSet = preparedStmt.executeQuery();
                     while (catRecResultSet.next())
                     {
                         catRecIds.add(catRecResultSet.getLong(1));
                     }
                 }
                 catch (Exception e)
                 {
                     throw new DynamicExtensionsSystemException(
                             "Error while executing query", e);
                 }
                 finally
                 {
                     catRecResultSet.close();
                     preparedStmt.close();
                 }
 
             }
 
         }
         catch (BizLogicException e)
         {
             throw new DynamicExtensionsSystemException(
                     "Error while opening session", e);
         }
         finally
         {
             resultSet.close();
             statement.close();
             connection.close();
             session.close();
         }
 
         return catRecIds;
     }
 
     /**
      * 
      * @param hookEntityId
      * @return the container Id of the DE entities that are associated with given static hook entity
      */
     public Collection getDynamicEntitiesContainerIdFromHookEntity(
             Long hookEntityId) throws DynamicExtensionsSystemException
     {
         /**
          * get all associated de entities with static entity and get its container id
          */
         Collection dynamicList = new HashSet();
         dynamicList = EntityManager.getInstance()
                 .getDynamicEntitiesContainerIdFromHookEntity(hookEntityId);
         return dynamicList;
     }
 
     /**
      * 
      * @param containerId
      * @return whether this entity is simple DE form /category. 
      */
     public boolean isCategory(Long containerId)
             throws DynamicExtensionsSystemException
     {
         Long contId = null;
 
         if (categoryEntityMap.containsKey((containerId + Constants.ISCATEGORY)))
         {
             contId = (Long) categoryEntityMap.get(containerId
                     + Constants.ISCATEGORY);
         }
         else
         // if(contId == null)
         {
             contId = EntityManager.getInstance().isCategory(containerId);
             categoryEntityMap.put(containerId + Constants.ISCATEGORY, contId);
         }
         if (contId != null)
             return true;
         return false;
 
     }
 
     /**
      *This method returns the entry Record Id BasedOn HookEntityRecId 
      */
     public Collection getDynamicEntityRecordIdFromHookEntityRecordId(
             String hookEntityRecId, Long containerId, Long hookEntityId)
             throws DynamicExtensionsSystemException, SQLException
     {
         Collection recIdList = new HashSet();
         String tableName = "";
         String columnName = "";
         if (entityMap.containsKey(containerId.toString()))
         {
             tableName = (String) entityMap.get(containerId.toString());
             columnName = (String) entityMap.get(hookEntityId + "_"
                     + containerId);
         }
         else
         // if(tableName ==null)
         {
             tableName = EntityManager.getInstance().getDynamicTableName(
                     containerId);
             columnName = EntityManager.getInstance()
                     .getColumnNameForAssociation(hookEntityId, containerId);
 
             entityMap.put(containerId.toString(), tableName);
             entityMap.put(hookEntityId + "_" + containerId, columnName);
         }
         EntityManagerUtil entityManagerUtil = new EntityManagerUtil();
         String entitySql = "select identifier from " + tableName + " where "
                 + columnName + "=" + hookEntityRecId;
         recIdList = entityManagerUtil.getResultInList(entitySql);
 
         return recIdList;
     }
 
     /**
      * 
      */
     public void associateRecords(Long containerId, Long staticEntityRecordId,
             Long dynamicEntityRecordId, Long hookEntityId)
             throws DynamicExtensionsApplicationException,
             DynamicExtensionsSystemException, BizLogicException, DAOException
     {
     }
 
     /**
      * 
      * @param categoryContainerId
      * @param staticRecId
      * @return the record id of the category depending on hook entity record id. 
      */
     public Collection getCategoriesContainerIdFromHookEntity(Long hookEntityId)
             throws DynamicExtensionsSystemException
     {
         Collection dynamicList = new HashSet();
         dynamicList = EntityManager.getInstance()
                 .getCategoriesContainerIdFromHookEntity(hookEntityId);
         return dynamicList;
     }
 
     /**
      * this method returns DynamicRecord From association id
      * @param recEntryId
      * @return
      * @throws DynamicExtensionsApplicationException 
      * @throws DynamicExtensionsSystemException 
      * @throws CacheException 
      * @throws SQLException 
      */
     public Collection getDynamicRecordFromStaticId(String recEntryId,
             Long containerId, String hookEntityId)
             throws DynamicExtensionsSystemException,
             DynamicExtensionsApplicationException, CacheException, SQLException
     {
         Collection recIdList = new HashSet();
         if (isCategory(containerId))
         {
             recIdList = getCategoryRecIdBasedOnHookEntityRecId(containerId,
                     new Long(recEntryId), new Long(hookEntityId));
         }
         else
         {
             recIdList = getDynamicEntityRecordIdFromHookEntityRecordId(
                     recEntryId, containerId, new Long(hookEntityId));
         }
         return recIdList;
     }
 
 }
