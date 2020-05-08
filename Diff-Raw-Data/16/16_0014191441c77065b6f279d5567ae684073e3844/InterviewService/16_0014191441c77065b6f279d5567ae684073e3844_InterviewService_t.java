 package jp.recruit.hps.movie.server.service;
 
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import jp.recruit.hps.movie.server.meta.InterviewMeta;
 import jp.recruit.hps.movie.server.model.Company;
 import jp.recruit.hps.movie.server.model.Interview;
 import jp.recruit.hps.movie.server.model.User;
 
 import org.slim3.datastore.Datastore;
 import org.slim3.util.BeanUtil;
 
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.Transaction;
 
 public class InterviewService {
     private static InterviewMeta meta = InterviewMeta.get();
 
     public static Interview createInterview(Map<String, Object> input,
             User user, Company company) {
         if (getInterviewCount(user.getKey(), company.getKey()) > 0) {
             return null;
         }
         Interview interview = new Interview();
         Key key = Datastore.allocateId(Interview.class);
         BeanUtil.copy(input, interview);
         interview.setKey(key);
         interview.getUserRef().setModel(user);
         interview.getCompanyRef().setModel(company);
         company.setInterviewCount(company.getInterviewCount() + 1);
        
         Transaction tx = Datastore.beginTransaction();
         Datastore.put(interview);
         tx.commit();
        
        Transaction tx2 = Datastore.beginTransaction();
        Datastore.put(company);
        tx2.commit();
         return interview;
     }
 
     public static Interview createInterview(User user, Company company,
             Date startDate) {
         Map<String, Object> map = new HashMap<String, Object>();
         map.put("startDate", startDate);
         map.put("updateDate", new Date());
         return createInterview(map, user, company);
     }
 
     public static void updateInterview(Interview interview, Date startDate) {
         interview.setStartDate(startDate);
         interview.setUpdateDate(new Date());
         Datastore.put(interview);
     }
 
     public static Interview getInterview(Key key) {
         try {
             return Datastore.query(meta).filter(meta.key.equal(key)).asSingle();
         } catch (Exception e) {
             return null;
         }
 
     }
 
     public static Interview getInterviewByCompanyKeyAndUserKey(Key companyKey,
             Key userKey) {
         try {
             return Datastore
                 .query(meta)
                 .filter(
                     meta.companyRef.equal(companyKey),
                     meta.userRef.equal(userKey))
                 .filterInMemory(meta.startDate.greaterThanOrEqual(new Date()))
                 .asList()
                 .get(0);
         } catch (Exception e) {
             return null;
         }
     }
 
     public static List<Interview> getInterviewListByCompanyKey(Key key) {
         try {
             return Datastore
                 .query(meta)
                 .filter(meta.companyRef.equal(key))
                 .filterInMemory(meta.startDate.greaterThanOrEqual(new Date()))
                 .sortInMemory(meta.startDate.asc)
                 .asList();
         } catch (Exception e) {
             return null;
         }
     }
 
     public static List<Interview> getInterviewListByUserKey(Key key) {
         try {
             return Datastore
                 .query(meta)
                 .filter(meta.userRef.equal(key))
                 .filterInMemory(meta.startDate.greaterThanOrEqual(new Date()))
                 .sortInMemory(meta.startDate.asc)
                 .asList();
         } catch (Exception e) {
             return null;
         }
     }
     
     public static List<Interview> getNewInterviewList(int limit) {
         try {
             return Datastore
                 .query(meta)
                 .sort(meta.updateDate.desc)
                 .limit(limit)
                 .asList();
         } catch (Exception e) {
             return null;
         }
     }
 
     public static int getInterviewCount(Key userKey, Key companyKey) {
         return Datastore
             .query(meta)
             .filter(
                 meta.userRef.equal(userKey),
                 meta.companyRef.equal(companyKey))
             .filterInMemory(meta.startDate.greaterThanOrEqual(new Date()))
             .count();
     }
 }
