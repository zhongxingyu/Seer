 package dk.statsbiblioteket.broadcasttranscoder.persistence.dao;
 
 import dk.statsbiblioteket.broadcasttranscoder.persistence.TranscodingStateEnum;
 import dk.statsbiblioteket.broadcasttranscoder.persistence.entities.TranscodingRecord;
 import org.hibernate.Criteria;
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 import org.hibernate.criterion.Order;
 import org.hibernate.criterion.Restrictions;
 
 import java.lang.reflect.ParameterizedType;
 import java.lang.reflect.Type;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: abr
  * Date: 1/28/13
  * Time: 11:48 AM
  * To change this template use File | Settings | File Templates.
  */
 public abstract class TranscodingRecordDao<T extends TranscodingRecord> extends GenericHibernateDAO<T, String> implements TranscodingProcessInterface<T>{
 
     private Class<T> type;
 
     protected TranscodingRecordDao(Class<T> type, HibernateUtilIF util) {
         super(type, util);
         this.type = type;
     }
 
 
     @Override
     public void markAsChangedInDoms(String programpid, long timestamp){
         T record = readOrCreate(programpid);
         Long previousTimestamp = record.getDomsLatestTimestamp();
        if (timestamp > previousTimestamp){
             record.setDomsLatestTimestamp(timestamp);
             record.setTranscodingState(TranscodingStateEnum.PENDING);
             update(record);
         }
     }
 
     @Override
     public void markAsAlreadyTranscoded(String programpid, long timestamp){
         T record = read(programpid);
         Long previousTimestamp = record.getDomsLatestTimestamp();
        if (timestamp > previousTimestamp){
             record.setTranscodingState(TranscodingStateEnum.COMPLETE);
             record.setLastTranscodedTimestamp(timestamp);
             update(record);
         }
     }
 
     @Override
     public void markAsFailed(String programpid,long timestamp, String message){
         T record = read(programpid);
         Long previousTimestamp = record.getDomsLatestTimestamp();
        if (timestamp > previousTimestamp){
             record.setTranscodingState(TranscodingStateEnum.FAILED);
             record.setDomsLatestTimestamp(timestamp);
             record.setFailureMessage(message);
             update(record);
         }
     }
 
     @Override
     public void markAsRejected(String programpid, long timestamp, String message){
         T record = read(programpid);
         Long previousTimestamp = record.getDomsLatestTimestamp();
        if (timestamp > previousTimestamp){
             record.setTranscodingState(TranscodingStateEnum.REJECTED);
             record.setDomsLatestTimestamp(timestamp);
             record.setFailureMessage(message);
             update(record);
         }
     }
 
     @Override
     public long getLatestTranscodingTimestamp(String programPid) {
         T record = read(programPid);
         return record.getLastTranscodedTimestamp();
     }
 
     @Override
     public long getLatestChangeInDomsTimestamp(String programPid) {
         T record = read(programPid);
         return record.getDomsLatestTimestamp();
     }
 
     @Override
     public List<T> getAllTranscodings(long since, TranscodingStateEnum state){
         Session session = getSession();
         Transaction tx = session.beginTransaction();
 
         Type mySuperClass = getClass().getGenericSuperclass();
         Type tType =  ((ParameterizedType)mySuperClass).getActualTypeArguments()[0];
 
 
         Criteria criteria = session.createCriteria(type)
                 .add(Restrictions.ge("domsLatestTimestamp", since))
                 .addOrder(Order.asc("domsLatestTimestamp"));
         if (state!= null){
             criteria = criteria.add(Restrictions.eq("transcodingState",state));
         }
         List<T> jobs = criteria
                 .list();
         tx.commit();
         session.close();
         return jobs;
     }
 
 }
