 package cz.muni.fi.pv243.et.data.impl;
 
 import cz.muni.fi.pv243.et.data.ExpenseReportListProducer;
 import cz.muni.fi.pv243.et.model.ExpenseReport;
 import cz.muni.fi.pv243.et.model.Person;
 import cz.muni.fi.pv243.et.model.ReportStatus;
 import org.apache.lucene.search.Query;
 import org.hibernate.Session;
 import org.hibernate.search.jpa.FullTextEntityManager;
 import org.hibernate.search.jpa.FullTextQuery;
 import org.hibernate.search.jpa.Search;
 import org.hibernate.search.query.DatabaseRetrievalMethod;
 import org.hibernate.search.query.ObjectLookupMethod;
 import org.hibernate.search.query.dsl.QueryBuilder;
 import org.jboss.solder.logging.Logger;
 
 import javax.ejb.Stateless;
 import javax.inject.Inject;
 import javax.persistence.EntityManager;
 import java.io.Serializable;
 import java.util.Collection;
 import java.util.List;
 
 @Stateless
 public class ExpenseReportListProducerImpl implements ExpenseReportListProducer {
 
     @Inject
     private EntityManager em;
 
     @Inject
     private Session session;
 
     @Override
     public ExpenseReport get(Long id) {
         if (id == null) {
             throw new IllegalArgumentException("id is null");
         }
         return em.find(ExpenseReport.class, id);
     }
 
     public Collection<ExpenseReport> getAllForSubmitter(Person submitter) {
         FullTextEntityManager ftem = Search.getFullTextEntityManager(em);
         QueryBuilder queryBuilder = ftem.getSearchFactory().buildQueryBuilder().forEntity(ExpenseReport.class).get();
         Query query = queryBuilder.keyword().onField("submitter.id").matching(submitter.getId()).createQuery();
 
         FullTextQuery fullTextQuery = ftem.createFullTextQuery(query, ExpenseReport.class);
         fullTextQuery.initializeObjectsWith(ObjectLookupMethod.SKIP, DatabaseRetrievalMethod.FIND_BY_ID);
 
         return fullTextQuery.getResultList();
 //        return session.createQuery("SELECT report FROM ExpenseReport report WHERE report.submitter.id = :submitterId")
 //                .setParameter("submitterId", submitter.getId()).list();
     }
 
     @Override
     public Collection<ExpenseReport> getAllForVerifier(Person verifier) {
         FullTextEntityManager ftem = Search.getFullTextEntityManager(em);
         QueryBuilder queryBuilder = ftem.getSearchFactory().buildQueryBuilder().forEntity(ExpenseReport.class).get();
         Query query = queryBuilder.keyword().onField("verifier.id").matching(verifier.getId()).createQuery();
 
         FullTextQuery fullTextQuery = ftem.createFullTextQuery(query, ExpenseReport.class);
         fullTextQuery.initializeObjectsWith(ObjectLookupMethod.SKIP, DatabaseRetrievalMethod.FIND_BY_ID);
 
         return fullTextQuery.getResultList();
     }
 
     @Override
     public Collection<ExpenseReport> getAllWithNoVerifierAssigned() {
         FullTextEntityManager ftem = Search.getFullTextEntityManager(em);
         QueryBuilder queryBuilder = ftem.getSearchFactory().buildQueryBuilder().forEntity(ExpenseReport.class).get();
         Query allAssigned = queryBuilder.range().onField("verifier.id").above(0).createQuery();
 //        Query query = queryBuilder.bool().must(allAssigned).not().createQuery();
 
         Query statusSubmitted = queryBuilder.keyword().onField("status").matching(ReportStatus.SUBMITTED).createQuery();
         Query query = queryBuilder.bool()
                 .must(allAssigned).not()
                 .must(statusSubmitted)
                 .createQuery();
 
         FullTextQuery fullTextQuery = ftem.createFullTextQuery(query, ExpenseReport.class);
         fullTextQuery.initializeObjectsWith(ObjectLookupMethod.SKIP, DatabaseRetrievalMethod.FIND_BY_ID);
 
         return fullTextQuery.getResultList();
     }
 
 
     @Override
     public Collection<ExpenseReport> getAllBy(ReportStatus status) {
        FullTextEntityManager ftem = Search.getFullTextEntityManager(em);
        QueryBuilder queryBuilder = ftem.getSearchFactory().buildQueryBuilder().forEntity(ExpenseReport.class).get();
        Query query = queryBuilder.keyword().onField("status").matching(status).createQuery();

        FullTextQuery fullTextQuery = ftem.createFullTextQuery(query, ExpenseReport.class);
        fullTextQuery.initializeObjectsWith(ObjectLookupMethod.SKIP, DatabaseRetrievalMethod.FIND_BY_ID);

        return fullTextQuery.getResultList();
     }
 
     @Override
     public Collection<ExpenseReport> getAllForSubmitterWithStatus(Person submitter, ReportStatus status) {
         FullTextEntityManager ftem = Search.getFullTextEntityManager(em);
         QueryBuilder queryBuilder = ftem.getSearchFactory().buildQueryBuilder().forEntity(ExpenseReport.class).get();
         Query forPerson = queryBuilder.keyword().onField("submitter.id").matching(submitter.getId()).createQuery();
        Query forStatus = queryBuilder.keyword().onField("status").matching(status).createQuery();
         Query query = queryBuilder.bool()
                 .must(forPerson)
                 .must(forStatus)
                 .createQuery();
 
         FullTextQuery fullTextQuery = ftem.createFullTextQuery(query, ExpenseReport.class);
         fullTextQuery.initializeObjectsWith(ObjectLookupMethod.SKIP, DatabaseRetrievalMethod.FIND_BY_ID);
 
         return fullTextQuery.getResultList();
     }
 
     @Override
     public Collection<ExpenseReport> getAllForVerifierWithStatus(Person verifier, ReportStatus status) {
         FullTextEntityManager ftem = Search.getFullTextEntityManager(em);
         QueryBuilder queryBuilder = ftem.getSearchFactory().buildQueryBuilder().forEntity(ExpenseReport.class).get();
         Query forPerson = queryBuilder.keyword().onField("verifier.id").matching(verifier.getId()).createQuery();
         Query forStatus = queryBuilder.keyword().onField("status").matching(status).createQuery();
         Query query = queryBuilder.bool()
                 .must(forPerson)
                 .must(forStatus)
                 .createQuery();
 
         FullTextQuery fullTextQuery = ftem.createFullTextQuery(query, ExpenseReport.class);
         fullTextQuery.initializeObjectsWith(ObjectLookupMethod.SKIP, DatabaseRetrievalMethod.FIND_BY_ID);
 
         return fullTextQuery.getResultList();
     }
 
     @Override
     public Collection<ExpenseReport> getAll() {
         return session.createQuery("SELECT report FROM ExpenseReport report").list();
     }
 
 
 }
