 package gov.usgs.cida.coastalhazards.jpa;
 
 import gov.usgs.cida.coastalhazards.exception.CycleIntroductionException;
 import gov.usgs.cida.coastalhazards.gson.GsonUtil;
 import gov.usgs.cida.coastalhazards.model.Item;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import javax.persistence.EntityManager;
 import javax.persistence.EntityTransaction;
 import javax.persistence.Query;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 
 /**
  *
  * @author Jordan Walker <jiwalker@usgs.gov>
  */
 public class ItemManager implements AutoCloseable {
     
     private static final Logger log = Logger.getLogger(ItemManager.class);
     private EntityManager em;
     
     public ItemManager() {
         em = JPAHelper.getEntityManagerFactory().createEntityManager();
     }
 
     public Item load(String itemId) {
         Item item = null;
         item = em.find(Item.class, itemId);
        if (item != null) {
            List<Item> children = item.getChildren();
            List<Item> replaceList = new LinkedList<>();
            if (children != null) {
                for (Item child : children) {
                    replaceList.add(load(child.getId()));
                }
                item.setChildren(replaceList);
             }
         }
         return item;
     }
 
     public synchronized String persist(Item item) throws CycleIntroductionException {
         String id = null;
         if (anyCycles(item)) {
             throw new CycleIntroductionException();
         }
         EntityTransaction transaction = em.getTransaction();
 		try {
 			transaction.begin();
             List<Item> children = item.getChildren();
             List<Item> replaceList = new LinkedList<>();
             if (children != null) {
                 for (Item child : children) {
                     replaceList.add(load(child.getId()));
                 }
                 item.setChildren(replaceList);
             }
 			em.persist(item);
 			id = item.getId();
 			transaction.commit();
             fixEnabledStatus();
 
 		} catch (Exception ex) {
             log.debug("Exception during save", ex);
             if (transaction.isActive()) {
                 transaction.rollback();
             }
             id = null;
 		}
         return id;
     }
 
     public synchronized String merge(Item item) throws CycleIntroductionException {
         String id = null;
         if (anyCycles(item)) {
             throw new CycleIntroductionException();
         }
         EntityTransaction transaction = em.getTransaction();
         try {
             transaction.begin();
             em.merge(item);
             id = item.getId();
             transaction.commit();
             fixEnabledStatus();
         } catch (Exception ex) {
             log.debug("Transaction failed on merge", ex);
             if (transaction.isActive()) {
                 transaction.rollback();
             }
             id = null;
         }
         return id;
     }
     
     public boolean delete(String itemId) {
         boolean deleted = false;
         EntityTransaction transaction = em.getTransaction();
         try {
             transaction.begin();
             Item item = em.find(Item.class, itemId);
             em.remove(item);
             transaction.commit();
             fixEnabledStatus();
             deleted = true;
         } catch (Exception ex) {
             log.debug("Transaction failed on delete", ex);
             if (transaction.isActive()) {
                 transaction.rollback();
             }
         }
         return deleted;
     }
 
     /**
      * Query the database for items and return it as a json string
      *
      * @param queryText keywords to query on
      * @param types item types to include in results
      * @param sortBy sort the results according to this technique
      * @param count max items to return (TODO paging)
      * @param bbox search bounding box (TODO fix this)
      * @param subtree whether to return entire subtree for aggregated items
      * @param showDisabled whether to include disabled items in the search
      * @return JSON result of items
      */
     public String query(List<String> queryText, List<String> types, String sortBy,
             int count, String bbox, boolean subtree, boolean showDisabled) {
         StringBuilder builder = new StringBuilder();
         List<String> queryParams = new LinkedList<>();
         int paramIndex = 1;
         builder.append("select i from Item i where i.enabled = true");
         // show disabled means return enable == true and false, so 1=1, narrow it down if !showDisabled
         if (showDisabled) {
             builder.append(" or i.enabled = false");
         }
         boolean hasQueryText = isEmpty(queryText);
         boolean hasType = isEmpty(types);
         List<Item.Type> typesList = new LinkedList<>();
         if (hasQueryText || hasType) {
             if (hasQueryText) {
                 builder.append(" and ");
 				List<String> likes = new ArrayList<>();
 				for (String keyword : queryText) {
 					if (StringUtils.isNotBlank(keyword)) {
                         queryParams.add('%' + keyword + "%");
                         StringBuilder likeBuilder = new StringBuilder();
                         likeBuilder.append(" lower(i.summary.keywords) like lower(?")
                                 .append(paramIndex)
                                 .append(")");
                         likeBuilder.append(" or lower(i.summary.full.title) like lower(?")
                                 .append(paramIndex)
                                 .append(")");
                         likes.add(likeBuilder.toString());
                         paramIndex++;
                     }
                 }
                 builder.append(StringUtils.join(likes, " or"));
             }
             if (hasType) {
                 builder.append(" and");
                 for (String type : types) {
                     typesList.add(Item.Type.valueOf(type));
                 }
                 builder.append(" i.type in(:types)");
             }
         }
 //        if ("popularity".equals(sortBy)) {
 //            builder.append(" order by i.rank.totalScore desc");
 //        } else if (false/*replace with other sort options */) {
         // TODO add order by clause
 //        }
         if (StringUtils.isNotBlank(bbox)) {
             //TODO bbox stuff here
         }
 
         String jsonResult = "";
 
         Query query = em.createQuery(builder.toString(), Item.class);
         for (int i = 0; i < queryParams.size(); i++) {
             String param = queryParams.get(i);
             query.setParameter(i + 1, param);
         }
         if (hasType) {
             query.setParameter("types", typesList);
         }
         if (count > 0) {
             query.setMaxResults(count);
         }
 
         List<Item> resultList = query.getResultList();
         Map<String, Object> resultMap = new HashMap<>();
         resultMap.put("subtree", subtree);
         resultMap.put("items", resultList);
         if (subtree) {
             jsonResult = GsonUtil.getSubtreeGson().toJson(resultMap, HashMap.class);
         }
         else {
             jsonResult = GsonUtil.getIdOnlyGson().toJson(resultMap, HashMap.class);
         }
 
         return jsonResult;
     }
     
     /**
      * Run this after inserting, updating or deleting
      * @param @return number of items updated from query
      */
     public int fixEnabledStatus() {
         int status = -1;
         EntityTransaction transaction = em.getTransaction();
         try {
             transaction.begin();
             Query update = em.createNativeQuery("UPDATE item SET enabled=\n" +
                     "CASE WHEN item.id IN(SELECT id FROM get_subtree(:id))\n" +
                         "THEN TRUE\n" +
                         "ELSE FALSE\n" +
                     "END");
             update.setParameter("id", Item.UBER_ID);
             status = update.executeUpdate();
             transaction.commit();
         } catch (Exception ex) {
             log.debug("Transaction failed on update enabled", ex);
             if (transaction.isActive()) {
                 transaction.rollback();
             }
         }
         return status;
     }
     
     public boolean anyCycles(Item item) {
         boolean cycles = false;
         for (Item child : item.getChildren()) {
             if (isCycle(item.getId(), child.getId())) {
                 cycles = true;
             }
         }
         return cycles;
     }
     
     /**
      * True is bad, don't want cycles
      * @param parentId parent wanting to add child to
      * @param childId child to check cycle on
      * @return whether there is a cycle
      */
     public boolean isCycle(String parentId, String childId) {
         if (StringUtils.isBlank(parentId) || StringUtils.isBlank(childId)) {
             throw new IllegalArgumentException("Parent and child must be valid IDs");
         }
         
         boolean cycle = false;
         Query subtree = em.createNativeQuery("SELECT id FROM get_subtree(:childId)");
         subtree.setParameter("childId", childId);
         List<Object> resultList = subtree.getResultList();
         for (Object result : resultList) {
 //            if (result.length != 1) {
 //                throw new IllegalStateException("Something went wrong with the query");
 //            }
             String id = (String)result;
             //int level = (int)result[1];
             if (parentId.equals(id)) {
                 cycle = true;
             }
         }
         return cycle;
     }
 
     private boolean isEmpty(List<String> args) {
         boolean result = false;
         if (args != null) {
             for (String str : args) {
                 if (StringUtils.isNotBlank(str)) {
                     result = true;
                 }
             }
         }
         return result;
     }
     
     @Override
     public void close() {
         JPAHelper.close(em);
     }
 
 }
