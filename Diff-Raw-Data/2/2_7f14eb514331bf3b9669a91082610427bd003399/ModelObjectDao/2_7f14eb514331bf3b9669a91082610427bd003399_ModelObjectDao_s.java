 package com.siniatech.siniabugs.dao.impl;
 
 import java.math.BigInteger;
 
 import org.hibernate.StatelessSession;
 import org.joda.time.DateTime;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.orm.hibernate3.HibernateTemplate;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.siniatech.siniabugs.dao.api.IModelObjectDao;
 import com.siniatech.siniabugs.model.api.IBugsUser;
 import com.siniatech.siniabugs.model.api.IModelObject;
 import com.siniatech.siniautils.collection.ListHelper;
 
 abstract public class ModelObjectDao<T extends IModelObject> implements IModelObjectDao<T> {
 
     @Autowired
     protected HibernateTemplate hibernateTemplate;
 
     private Long getNextUid() {
         StatelessSession session = hibernateTemplate.getSessionFactory().openStatelessSession();
        BigInteger uid = ListHelper.head( session.createSQLQuery( "select nextUid() from dual" ).list() );
         return uid.longValue();
     }
 
     @Transactional
     public void create( T t, IBugsUser savedBy ) {
         t.setUid( getNextUid() );
         t.setCreator( savedBy );
         t.setLastEditor( savedBy );
         t.setVersionStart( new DateTime() );
         hibernateTemplate.saveOrUpdate( t );
     }
 
     @Transactional
     public void delete( T t1, IBugsUser deletedBy ) {
         DateTime deletedAt = new DateTime();
         archiveCurrentRecord( t1, deletedAt );
         t1.setUid( getNextUid() );
         t1.setVersionStart( deletedAt );
         t1.setVersionEnd( deletedAt.plusMillis( 1 ) );
         t1.setLastEditor( deletedBy );
         hibernateTemplate.saveOrUpdate( t1 );
 
     }
 
     private void archiveCurrentRecord( T t1, DateTime at ) {
         assert !t1.isHistorical();
         T t2 = createHistoricalFrom( t1 );
         t2.setVersionEnd( at );
         hibernateTemplate.saveOrUpdate( t2 );
     }
 
     @Transactional
     public T createHistoricalFrom( T t1 ) {
         T t2 = newInstanceHistorical();
         copyGenericFields( t1, t2 );
         copyNonGenericFields( t1, t2 );
         return t2;
     }
 
     @Transactional
     public void update( T t, IBugsUser updatedBy ) {
         DateTime updatedAt = new DateTime();
         T orig = readByUid( t.getUid() );
         archiveCurrentRecord( orig, updatedAt );
         copyGenericFields( orig, t );
         t.setUid( getNextUid() );
         t.setLastEditor( updatedBy );
         t.setVersionStart( updatedAt );
         t.setVersionEnd( null );
         hibernateTemplate.saveOrUpdate( t );
     }
 
     @Transactional
     public T copy( T t1 ) {
         T t2 = newInstance();
         copyGenericFields( t1, t2 );
         copyNonGenericFields( t1, t2 );
         return t2;
     }
 
     private void copyGenericFields( T t1, T t2 ) {
         t2.setCreator( t1.getCreator() );
         t2.setId( t1.getId() );
         t2.setUid( t1.getUid() );
         t2.setLastEditor( t1.getLastEditor() );
         t2.setVersionStart( t1.getVersionStart() );
         t2.setVersionEnd( t1.getVersionEnd() );
     }
 
     abstract protected void copyNonGenericFields( T source, T target );
 }
