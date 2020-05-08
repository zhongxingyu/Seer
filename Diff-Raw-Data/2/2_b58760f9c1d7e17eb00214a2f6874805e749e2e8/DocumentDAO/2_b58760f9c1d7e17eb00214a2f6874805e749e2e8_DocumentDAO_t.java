 package fr.cg95.cvq.dao.document.hibernate;
 
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.Hashtable;
 import java.util.List;
 
 import org.hibernate.Criteria;
 import org.hibernate.criterion.Criterion;
 import org.hibernate.criterion.Order;
 import org.hibernate.criterion.Projections;
 import org.hibernate.criterion.Restrictions;
 
 import fr.cg95.cvq.business.document.ContentType;
 import fr.cg95.cvq.business.document.Document;
 import fr.cg95.cvq.business.document.DocumentState;
 import fr.cg95.cvq.dao.document.IDocumentDAO;
 import fr.cg95.cvq.dao.hibernate.GenericDAO;
 import fr.cg95.cvq.dao.hibernate.HibernateUtil;
 import fr.cg95.cvq.util.Critere;
 
 /**
  * Implementation of the {@link IDocumentDAO} interface.
  * 
  * @author bor@zenexity.fr
  */
 public class DocumentDAO extends GenericDAO implements IDocumentDAO {
 
     @SuppressWarnings("unchecked")
     public List<Document> listProvidedDocuments(final Long docTypeId,
         final Long homeFolderId, final Long individualId) {
 
         Criteria crit = HibernateUtil.getSession().createCriteria(Document.class);
         if (docTypeId != null)
             crit.createCriteria("documentType").add(
                     Critere.compose("id", docTypeId, Critere.EQUALS));
         if (homeFolderId != null)
             crit.add(Critere.compose("homeFolderId", homeFolderId, Critere.EQUALS));
         if (individualId != null)
             crit.add(Critere.compose("individualId", individualId, Critere.EQUALS));
 
         return (List<Document>)crit.list();
     }
 
     @SuppressWarnings("unchecked")
     public List<Document> listByHomeFolder (final Long homeFolderId, int max) {
         Criteria crit = HibernateUtil.getSession().createCriteria(Document.class);
         crit.add(Critere.compose("homeFolderId", homeFolderId, Critere.EQUALS));
         crit.addOrder(Order.desc("creationDate"));
 
         if (max != -1)
             crit.setMaxResults(max);
 
         return (List<Document>)crit.list();
     }
 
     @SuppressWarnings("unchecked")
     public List<Document> listByIndividual(final Long individualId) {
         Criteria crit = HibernateUtil.getSession().createCriteria(Document.class);
         crit.add(Critere.compose("individualId", individualId, Critere.EQUALS));
         crit.addOrder(Order.asc("id"));
         return (List<Document>)crit.list();
     }
 
     public Integer searchCount(Hashtable<String,Object> searchParams) {
         Criteria criteria = this.buildSearchCriteria(searchParams);
         criteria.setProjection(Projections.rowCount());
         return ((Integer)criteria.list().get(0)).intValue();
     }
 
     @SuppressWarnings("unchecked")
     public List<Document> search(Hashtable<String,Object> searchParams,
         int max,int offset) {
         Criteria criteria = this.buildSearchCriteria(searchParams);
         criteria.addOrder(Order.desc("creationDate"));
 
         if(max > -1) criteria.setMaxResults(max);
         if(offset > -1) criteria.setFirstResult(offset);
 
         return (List<Document>)criteria.list();
     }
 
     @Override
     public List<Long> listByMissingComputedValues() {
         List<BigInteger> ids = HibernateUtil.getSession().createSQLQuery(
             "select document_id from document_binary where content_type is null or content_type = '"
                 + ContentType.OCTET_STREAM + "' or preview is null").list();
         List<Long> result = new ArrayList<Long>(ids.size());
         for (BigInteger id : ids) {
             result.add(id.longValue());
         }
         return result;
     }
 
     @Override
     public List<Long> listOutdated() {
         return HibernateUtil.getSession()
            .createQuery("select id from Document where state in (?, ?, ?) and endValidityDate < ?")
             .setString(0, DocumentState.PENDING.toString())
             .setString(1, DocumentState.CHECKED.toString())
             .setString(2, DocumentState.VALIDATED.toString())
             .setDate(3, new Date())
             .list();
     }
 
     private Criteria buildSearchCriteria(Hashtable<String,Object> params) {
         Criteria criteria = HibernateUtil.getSession()
             .createCriteria(Document.class);
         Hashtable<String,Object> specials = new Hashtable<String,Object>();
 
         for(String key : params.keySet()) {
             if(key == "individualId" || key == "homeFolderId")
                 specials.put(key, params.get(key));
             else
                 criteria.add(this.processParam(key, params.get(key)));
         }
 
         if (specials.containsKey("individualId")
             && specials.containsKey("homeFolderId")) {
             Criterion crt;
             if (specials.get("individualId") instanceof Collection<?>)
                 crt = this.processParam("individualId",
                     (Collection<?>)specials.get("individualId"));
             else
                 crt = this.processParam("individualId",
                     specials.get("individualId"));
 
             criteria.add(Restrictions.or(crt, 
                 this.processParam("homeFolderId",specials.get("homeFolderId"))));
         } else if (specials.containsKey("individualId")) {
             criteria.add(this.processParam("individualId",
                 specials.get("individualId")));
         }  else if (specials.containsKey("homeFolderId")) {
             criteria.add(this.processParam("homeFolderId",
                 specials.get("homeFolderId")));
         }
         return criteria;
     }
 
     private <T extends Collection<?>> Criterion processParam(String key, T param) {
         return Restrictions.in(key, param);
     }
 
     private <T> Criterion processParam(String key, T param) {
         return Restrictions.eq(key, param);
     }
 }
