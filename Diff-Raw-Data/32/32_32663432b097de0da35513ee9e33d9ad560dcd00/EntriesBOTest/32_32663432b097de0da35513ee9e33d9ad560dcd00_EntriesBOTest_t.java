 package com.soebes.casestudy;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.List;
 import java.util.TimeZone;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Persistence;
import javax.persistence.criteria.CriteriaBuilder;
 import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;
 
 import org.apache.log4j.Logger;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 import com.google.common.base.Joiner;
 import com.soebes.casestudy.bo.EntriesBO;
 
 public class EntriesBOTest extends BOTestBase {
     private static Logger LOGGER = Logger.getLogger(EntriesBOTest.class);
 
     private EntityManagerFactory emf;
 
     private EntityManager em;
 
     @BeforeClass
     public void beforeClass() {
         emf = Persistence.createEntityManagerFactory("hibernate-search-example");
 
         // EntityManagerFactory emf =
         // cfg.addProperties( properties ) //add some properties
         // .setInterceptor( myInterceptorImpl ) // set an interceptor
         // .addAnnotatedClass( MyAnnotatedClass.class ) //add a class to be
         // mapped
         // .addClass( NonAnnotatedClass.class ) //add an hbm.xml file using the
         // Hibernate convention
         // .addRerousce( "mypath/MyOtherCLass.hbm.xml ) //add an hbm.xml file
         // .addRerousce( "mypath/orm.xml ) //add an EJB3 deployment descriptor
         // .configure("/mypath/hibernate.cfg.xml") //add a regular
         // hibernate.cfg.xml
         // .buildEntityManagerFactory(); //Create the entity manager factory
         em = emf.createEntityManager();
         LOGGER.debug("beforeClass()");
 
         LOGGER.debug("beforeClass(done)");
     }
 
     private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
 
    @Test
     public void testGet() {
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<EntriesBO> criteria = criteriaBuilder.createQuery(EntriesBO.class);
        Root<EntriesBO> from = criteria.from(EntriesBO.class);

        criteria.where( criteriaBuilder.equal(from.get("isDraft"), String.valueOf("false")));
        
         List<EntriesBO> resultList = em.createQuery(criteria).getResultList();
         
         LOGGER.info("Number of entries:" + resultList.size());
         for (EntriesBO entriesBO : resultList) {
             Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT-2:00"));
             cal.setTimeInMillis(entriesBO.getTimestamp() * 1000);
 
             StringBuilder sb = new StringBuilder("Id:" + entriesBO.getId() + " draft: " + entriesBO.getIsDraft() + " time:"
                     + DATE_FORMAT.format(cal.getTime()) + "(" + entriesBO.getTimestamp() + ") " + " Title:" + entriesBO.getTitle());
             sb.append(" Categories:" + Joiner.on(',').join(entriesBO.getCategories()));
 
             LOGGER.info(sb.toString());
         }
     }
 
 }
