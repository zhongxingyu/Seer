 package gov.usgs.cida.coastalhazards.jpa;
 
 import com.google.gson.Gson;
 import gov.usgs.cida.coastalhazards.model.Item;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import javax.persistence.EntityManager;
 import javax.persistence.EntityTransaction;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Query;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.StringUtils;
 
 /**
  *
  * @author Jordan Walker <jiwalker@usgs.gov>
  */
 public class ItemManager {
 
 	@PersistenceContext
 	private EntityManager em;
 
 	public ItemManager() {
 		em = JPAHelper.getEntityManagerFactory().createEntityManager();
 	}
 
 	public String load(String itemId) {
 		String jsonItem = null;
 		
 		Item item = em.find(Item.class, itemId);
 		if (null != item) {
 			jsonItem = item.toJSON();
 		} else {
 			File onDiskItem = new File(FileUtils.getTempDirectory(), itemId);
 			if (onDiskItem.exists()) {
 				try {
 					jsonItem = IOUtils.toString(new FileInputStream(onDiskItem));
 				} catch (IOException ex) {
 					// Ignore - pass back null
 				}
 			}
 		}
 		
 		return jsonItem;
 	}
 
 	public synchronized String save(String item) {
 		String id = "ERR";
         EntityTransaction transaction = em.getTransaction();
 		try {
 			transaction.begin();
 			Item itemObj = Item.fromJSON(item);
 			em.persist(itemObj);
 			id = itemObj.getId();
 			transaction.commit();
 		} catch (Exception ex) {
             if (transaction.isActive()) {
                 transaction.rollback();
             }
 		}
 		return id;
 	}
 	
 	public String savePreview(Item item) {
         String id = item.getId();
 		try {
 			File onDiskItem = new File(FileUtils.getTempDirectory(), id);
 			FileUtils.write(onDiskItem, item.toJSON());
 			onDiskItem.deleteOnExit();
 		} catch (Exception ex) {
 			id = "ERR";
 		}
 		return id;
 	}
 
 	public String query(String queryText, String type, String sortBy, int count, String bbox) {
         StringBuilder builder = new StringBuilder();
         builder.append("select i from Item i");
         boolean hasQueryText = StringUtils.isNotBlank(queryText);
         boolean hasType = StringUtils.isNotBlank(type);
         if (hasQueryText || hasType) {
            builder.append(" where ");
             if (hasQueryText) {
                 String[] words = queryText.split(" ");
                
                 for (int i = 0; i<words.length; i++) {
                    words[i] = "lower(i.summary.full.text) like lower('%" + words[i] + "%')";
                 }
                 String like = StringUtils.join(words, " or ");
                builder.append(like);
             }
             if (hasQueryText && hasType) {
                 builder.append(" and");
             }
             if (hasType) {
                builder.append(" i.type in('").append(type).append("')");
             }
         }
         if ("popularity".equals(sortBy)) {
             builder.append(" order by i.rank.totalScore desc");
         }
         if (count > 0) {
             builder.append(" limit ").append(count);
         }
         if (StringUtils.isNotBlank(bbox)) {
             //do bbox stuff here
         }
         
 		Query query = em.createQuery(builder.toString(), Item.class);
 		List<Item> resultList = query.getResultList();
 		Map<String, List> resultMap = new HashMap<String, List>();
 		resultMap.put("items", resultList);
 		return new Gson().toJson(resultMap, HashMap.class);
 	}
 }
