 package edu.wustl.cab2b.server.util;
 
 import java.sql.Connection;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.TreeSet;
 
 import org.apache.log4j.Logger;
 
 import edu.common.dynamicextensions.domaininterface.AttributeInterface;
 import edu.wustl.cab2b.common.exception.RuntimeException;
 import edu.wustl.cab2b.common.queryengine.result.IRecord;
 import edu.wustl.cab2b.common.util.Utility;
 import edu.wustl.cab2b.server.cache.EntityCache;
 import edu.wustl.cab2b.server.category.CategoryCache;
 import edu.wustl.cab2b.server.datalist.DataListOperationsController;
 import edu.wustl.cab2b.server.path.PathFinder;
 import edu.wustl.common.bizlogic.DefaultBizLogic;
 import edu.wustl.common.exception.BizLogicException;
 import edu.wustl.common.querysuite.metadata.path.CuratedPath;
 import edu.wustl.common.security.exceptions.UserNotAuthorizedException;
 import edu.wustl.common.util.global.Constants;
 
 /**
  * Operations class for utility bean
  * @author chandrakant_talele
  */
 public class UtilityOperations extends DefaultBizLogic {
     private static final Logger logger = edu.wustl.common.util.logger.Logger.getLogger(UtilityOperations.class);
 
     /**
      * Hibernate DAO Type to use.
      */
     private static int daoType = Constants.HIBERNATE_DAO;
 
     /**
      * @param cab2bObject
      */
     public void insert(Object cab2bObject) {
         try {
             insert(cab2bObject, daoType);
             logger.debug(cab2bObject.getClass().getName() + " inserted Successfully ");
         } catch (BizLogicException e) {
             throw (new RuntimeException(e.getMessage(), e));
         } catch (UserNotAuthorizedException e) {
             throw (new RuntimeException(e.getMessage(), e));
         }
     }
 
     /**
      * @param cab2bObject
      */
     public void update(Object cab2bObject) {
         try {
             update(cab2bObject, daoType);
             logger.debug(cab2bObject.getClass().getName() + " inserted Successfully ");
         } catch (BizLogicException e) {
             logger.debug(Utility.getStackTrace(e));
             throw (new RuntimeException(e.getMessage(), e));
         } catch (UserNotAuthorizedException e) {
             logger.debug(Utility.getStackTrace(e));
             throw (new RuntimeException(e.getMessage(), e));
         }
     }
 
     /**
      * This method returns the list of tree set containing the unique record values for a given entity identifier.
      * Tree set stores the values in sorted order. 
      * @param entityId entityId
      * @return List of TreeSet
      */
     public List<TreeSet<Comparable<?>>> getUniqueRecordValues(Long entityId) {
         //getting record list
         List<IRecord> entityRecordList = new DataListOperationsController().getEntityRecords(entityId);
         if (entityRecordList == null || entityRecordList.size() == 0) {
             return null;
         }
        IRecord rec = entityRecordList.get(0);
         //initializing column value set
        List<TreeSet<Comparable<?>>> entityRecordValues = initEntityRecordValues(rec.getAttributes().size());
         for (IRecord entityRecord : entityRecordList) {
             process(entityRecord, entityRecordValues);
         }
         return entityRecordValues;
     }
    private List<TreeSet<Comparable<?>>> initEntityRecordValues(int size) {
        List<TreeSet<Comparable<?>>> list = new ArrayList<TreeSet<Comparable<?>>>(size);
        for (int i = 0; i < size; i++) {
            list.add(i, new TreeSet<Comparable<?>>());
        }
        return list;
    }
 
     private void process(IRecord record, List<TreeSet<Comparable<?>>> entityRecordValues) {
         int index = 0;
         List<AttributeInterface> attributeList = Utility.getAttributeList(record.getAttributes());
         for (AttributeInterface attribute : attributeList) {
             TreeSet<Comparable<?>> columnValues = entityRecordValues.get(index);
             Comparable<?> attributeValue = (Comparable<?>) record.getValueForAttribute(attribute);
             if (attributeValue != null) {
                 columnValues.add(attributeValue);
             }
             index++;
         }
     }
 
     /**
      * Method to refresh Path and Entity cache. 
      * 
      * @param refreshEntityCache If true refreshes entity cache along with path. If false, only rereshes path cache.
      */
     public void refreshPathAndEntityCache(boolean refreshEntityCache) {
         logger.info("Successful access on remote bean!!!!");
         Connection connection = ConnectionUtil.getConnection();
         PathFinder.refreshCache(connection, refreshEntityCache);
     }
 
     /**
      * Method to refresh category cache.
      */
     public void refreshCategoryAndEntityCache() {
         EntityCache.getInstance().refreshCache();
         Connection connection = ConnectionUtil.getConnection();
         CategoryCache.getInstance().refreshCategoryCache(connection);
     }
 
     /**
      * Adds the curated path to cache
      * @param curatedPath path to be added to cache.
      */
     public void addCuratedPathToCache(CuratedPath curatedPath) {
         Connection connection = ConnectionUtil.getConnection();
         PathFinder.getInstance(connection).addCuratedPath(curatedPath);
     }
 }
