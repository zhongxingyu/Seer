 package kwitches.service.dao;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import kwitches.meta.BBSDataModelMeta;
 import kwitches.model.BBSDataModel;
 
 import org.slim3.datastore.Datastore;
 import org.slim3.datastore.ModelQuery;
 import org.slim3.memcache.Memcache;
 
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.Transaction;
 
 /**
  * BBSDataModelのDAO(Singleton)
  * @author voidy21
  */
 public class BBSDataModelDao {
     /** 引数を指定しない場合のデフォルト取得件数 */
     public static final int DEFAULT_LIMIT = 30;
 
     private static final BBSDataModelMeta meta =  BBSDataModelMeta.get();
     private static BBSDataModelDao instance = new BBSDataModelDao();
 
     private static final Object MAX_ID = "max_id";
 
     private BBSDataModelDao(){}
 
     public static BBSDataModelDao GetInstance(){
              return instance;
     }
 
     public List<BBSDataModel> getBBSDataList() {
         return Datastore.query(meta)
                         .sort(meta.id.desc)
                         .limit(DEFAULT_LIMIT)
                         .asList();
     }
 
     public List<BBSDataModel> getBBSDataList(int offset, int limit) {
         ArrayList<Integer> idList = new ArrayList<Integer>(limit);
         int startId = BBSDataModelDao.getMaxId() - offset;
         for (int i = 0; i < limit; i++)
             idList.add(new Integer(startId - i));
 
         return Datastore
             .query(meta)
             .filter(meta.id.in(idList))
             .sort(meta.id.desc)
             .asList();
     }
 
     public List<BBSDataModel> getBBSData(int resNumber) {
         return Datastore.query(meta)
                         .filter(meta.id.equal(resNumber))
                         .asList();
     }
 
     public void putBBSData(BBSDataModel bbsDataModel) {
         Transaction tx = Datastore.beginTransaction();
         Datastore.put(bbsDataModel);
         Memcache.delete(MAX_ID);
         tx.commit();
     }
 
     public void deleteBBSData(int resNumber) {
         Key key = Datastore.query(meta)
                         .filter(meta.id.equal(resNumber))
                         .asSingle().getKey();
         Transaction tx = Datastore.beginTransaction();
         Datastore.delete(key);
         tx.commit();
     }
 
     public List<BBSDataModel> searchBBSData(List<String> token) {
         if (token == null) {
             return null;
         }
         ModelQuery<BBSDataModel> query = Datastore.query(meta);
         return query.filter(meta.invertedIndex.in(token)).sort(meta.id.desc).limit(1000).asList();
     }
 
     public static int getMaxId() {
         Integer maxId = (Integer) Memcache.get(MAX_ID);
         if (maxId == null){
             maxId = getMaxIdActually();
         }
         return maxId.intValue();
      }
 
     public static Integer getMaxIdActually() {
         Integer maxId = new Integer(Datastore.query(meta).max(meta.id));
         Memcache.put(MAX_ID, maxId);
         return maxId;
     }
 }
