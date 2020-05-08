 package fr.cg95.cvq.dao.payment.hibernate;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.commons.lang3.StringUtils;
 import org.hibernate.Hibernate;
 import org.hibernate.Query;
 import org.hibernate.type.Type;
 
 import fr.cg95.cvq.business.payment.ExternalAccountItem;
 import fr.cg95.cvq.business.payment.ExternalDepositAccountItem;
 import fr.cg95.cvq.business.payment.ExternalInvoiceItem;
 import fr.cg95.cvq.business.payment.ExternalTicketingContractItem;
 import fr.cg95.cvq.business.payment.Payment;
 import fr.cg95.cvq.business.payment.PaymentState;
 import fr.cg95.cvq.dao.jpa.JpaTemplate;
 import fr.cg95.cvq.dao.hibernate.HibernateUtil;
 import fr.cg95.cvq.dao.payment.IPaymentDAO;
 import fr.cg95.cvq.util.Critere;
 import fr.cg95.cvq.util.DateUtils;
 
 /**
  * Hibernate implementation of the {@link IPaymentDAO} interface.
  * 
  * @author bor@zenexity.fr
  * @author rdj@zenexity.fr
  */
 public class PaymentDAO extends JpaTemplate<Payment,Long> implements IPaymentDAO {
 	  
     public PaymentDAO() {
         super();
     }
 
     @SuppressWarnings("unchecked")
     public List<Payment> findByHomeFolder(final Long homeFolderId) {
         return HibernateUtil.getSession()
             .createQuery("from Payment as payment where payment.homeFolderId = :folder "
                     + "order by payment.initializationDate desc")
             .setParameter("folder", homeFolderId)
             .list(); 
     }
 
     public Payment findByCvqReference(final String cvqReference) {
         return (Payment) HibernateUtil.getSession()
             .createQuery("from Payment as payment where payment.cvqReference = :cvqReference")
             .setParameter("cvqReference", cvqReference)
             .uniqueResult(); 
     }
 
     public Payment findByBankReference(String bankReference) {
         return (Payment) HibernateUtil.getSession()
             .createQuery("from Payment as payment where payment.bankReference = :bankReference")
             .setParameter("bankReference", bankReference)
             .uniqueResult();
     }
 
 
     @SuppressWarnings("unchecked")
     @Override
     public List<Payment> findByIds(List<Long> paymentsIds) {
         StringBuffer sb = new StringBuffer();
         sb.append("from Payment as payment where id in (");
         sb.append(StringUtils.join(paymentsIds, ","));
         sb.append(" ) ");
 
         Query query = HibernateUtil.getSession().createQuery(sb.toString());
         return query.list();
     }
 
     @SuppressWarnings("unchecked")
     public List<Payment> search(final Set<Critere> criteria, final String sort, String dir,
             int recordsReturned, int startIndex) {
 
         StringBuffer sb = new StringBuffer();
         sb.append("from Payment as payment").append(" where 1 = 1 ");
 
         List<Object> parametersValues = new ArrayList<Object>();
         List<Type> parametersTypes = new ArrayList<Type>();
 
         // go through all the criteria and create the query
         for (Critere searchCrit : criteria) {
             if (searchCrit.getAttribut().equals(Payment.SEARCH_BY_HOME_FOLDER_ID)) {
                 sb.append(" and payment.homeFolderId " + searchCrit.getComparatif() + " ?");
                 parametersValues.add(searchCrit.getLongValue());
                 parametersTypes.add(Hibernate.LONG);
             
             } else if (searchCrit.getAttribut().equals(Payment.SEARCH_BY_REQUESTER_LASTNAME)) {
                 sb.append(" and lower(payment.requesterLastName) " + searchCrit.getSqlComparatif()
                         + " lower(?)");
                 parametersValues.add(searchCrit.getSqlStringValue());
                 parametersTypes.add(Hibernate.STRING);
 
             } else if (searchCrit.getAttribut().equals(Payment.SEARCH_BY_CVQ_REFERENCE)) {
                 sb.append(" and payment.cvqReference " + searchCrit.getComparatif() + " ? ");
                 parametersValues.add(searchCrit.getSqlStringValue());
                 parametersTypes.add(Hibernate.STRING);
 
             } else if (searchCrit.getAttribut().equals(Payment.SEARCH_BY_BANK_REFERENCE)) {
                 sb.append(" and payment.bankReference " + searchCrit.getComparatif() + " ? ");
                 parametersValues.add(searchCrit.getSqlStringValue());
                 parametersTypes.add(Hibernate.STRING);
                 
             } else if (searchCrit.getAttribut().equals(Payment.SEARCH_BY_INITIALIZATION_DATE)) {
                 sb.append(" and payment.initializationDate " + searchCrit.getComparatif() + " ?");
                 parametersValues.add(searchCrit.getDateValue());
                 parametersTypes.add(Hibernate.TIMESTAMP);
                 
             } else if (searchCrit.getAttribut().equals(Payment.SEARCH_BY_PAYMENT_STATE)) {
                 sb.append(" and payment.state " + searchCrit.getComparatif() + " ?");
                parametersValues.add(searchCrit.getSqlStringValue());
                 parametersTypes.add(Hibernate.STRING);
                 
             } else if (searchCrit.getAttribut().equals(Payment.SEARCH_BY_BROKER)) {
                 sb.append(" and payment.broker " + searchCrit.getComparatif() + " ?");
                 parametersValues.add(searchCrit.getSqlStringValue());
                 parametersTypes.add(Hibernate.STRING);
             
             } else if (searchCrit.getAttribut().equals(Payment.SEARCH_BY_PAYMENT_MODE)) {
                 sb.append(" and payment.paymentMode " + searchCrit.getComparatif() + " ?");
                parametersValues.add(searchCrit.getSqlStringValue());
                 parametersTypes.add(Hibernate.STRING);
             }
         }
         
         if (sort != null) {
             if (sort.equals(Payment.SEARCH_BY_HOME_FOLDER_ID))
                 sb.append(" order by payment.homeFolderId");
             else if (sort.equals(Payment.SEARCH_BY_REQUESTER_LASTNAME))
                 sb.append(" order by payment.requesterLastName");
             else if (sort.equals(Payment.SEARCH_BY_INITIALIZATION_DATE))
                 sb.append(" order by payment.initializationDate");
             else
                 sb.append(" order by payment.id");
         } else {
             // default sort order
             sb.append(" order by payment.id");
         }
 
         if (dir != null && dir.equals("desc"))
             sb.append(" desc");
 
         Query query = HibernateUtil.getSession().createQuery(sb.toString());
         query.setParameters(parametersValues.toArray(), parametersTypes.toArray(new Type[0]));
 
         if (recordsReturned > 0)
             query.setMaxResults(recordsReturned);
         query.setFirstResult(startIndex);
         return query.list();
     }
 
     private void invoiceCritereToHQL(Set<Critere> criteria, StringBuffer sb, 
             List<Object> values, List<Type> types ) {
         for (Critere searchCrit : criteria) {
             if (searchCrit.getAttribut().equals(ExternalInvoiceItem.SEARCH_BY_EXTERNAL_HOME_FOLDER)) {
                 sb.append(" and externalInvoiceItem.externalHomeFolderId in " + 
                         searchCrit.getStringValue());
             } else if (searchCrit.getAttribut().equals(ExternalInvoiceItem.SEARCH_BY_EXTERNAL_SERVICE_LABEL)) {
                 sb.append(" and externalInvoiceItem.externalServiceLabel " + searchCrit.getComparatif() + " ?");
                 values.add(searchCrit.getSqlStringValue());
                 types.add(Hibernate.STRING);
             } else if (searchCrit.getAttribut().equals(ExternalInvoiceItem.SEARCH_BY_EXTERNAL_INVOICE_ID)) {
                 sb.append(" and externalInvoiceItem.externalItemId " + searchCrit.getComparatif() + " ?");
                 values.add(searchCrit.getSqlStringValue());
                 types.add(Hibernate.STRING);
             } else if (searchCrit.getAttribut().equals(ExternalInvoiceItem.SEARCH_BY_EXPIRATION_DATE)) {
                 sb.append(" and externalInvoiceItem.expirationDate " + searchCrit.getComparatif() + " ? ");
                 values.add(searchCrit.getDateValue());
                 types.add(Hibernate.TIMESTAMP);
             } else if (searchCrit.getAttribut().equals(ExternalInvoiceItem.SEARCH_BY_INVOICE_STATE)) {
                 sb.append(" and externalInvoiceItem.isPaid " + searchCrit.getComparatif() + " ? ");
                 values.add(searchCrit.getValue());
                 types.add(Hibernate.BOOLEAN);
             } else if (searchCrit.getAttribut().equals(ExternalInvoiceItem.SEARCH_BY_BROKER)) {
                 sb.append(" and externalInvoiceItem.supportedBroker" + searchCrit.getComparatif() + " ?");
                 values.add(searchCrit.getSqlStringValue());
                 types.add(Hibernate.STRING);
             } else if (searchCrit.getAttribut().equals(ExternalInvoiceItem.SEARCH_BY_EXTERNAL_APPLICATION)) {
                 sb.append(" and externalInvoiceItem.externalApplicationId" + searchCrit.getComparatif() + " ?");
                 values.add(searchCrit.getSqlStringValue());
                 types.add(Hibernate.STRING);
             }
         }
     }
 
     private void depositAccountCritereToHQL(Set<Critere> criteria, StringBuffer sb, 
             List<Object> values, List<Type> types ) {
         for (Critere searchCrit : criteria) {
             if (searchCrit.getAttribut().equals(ExternalDepositAccountItem.SEARCH_BY_EXTERNAL_HOME_FOLDER)) {
                 sb.append(" and externalDepositAccountItem.externalHomeFolderId in " + 
                         searchCrit.getStringValue());
             } else if (searchCrit.getAttribut().equals(ExternalDepositAccountItem.SEARCH_BY_EXTERNAL_SERVICE_LABEL)) {
                 sb.append(" and externalDepositAccountItem.externalServiceLabel " + searchCrit.getComparatif() + " ?");
                 values.add(searchCrit.getSqlStringValue());
                 types.add(Hibernate.STRING);
             } else if (searchCrit.getAttribut().equals(ExternalDepositAccountItem.SEARCH_BY_EXTERNAL_DEPOSIT_ACCOUNT_ID)) {
                 sb.append(" and externalDepositAccountItem.externalItemId " + searchCrit.getComparatif() + " ?");
                 values.add(searchCrit.getSqlStringValue());
                 types.add(Hibernate.STRING);
             } else if (searchCrit.getAttribut().equals(ExternalDepositAccountItem.SEARCH_BY_BROKER)) {
                 sb.append(" and externalDepositAccountItem.supportedBroker" + searchCrit.getComparatif() + " ?");
                 values.add(searchCrit.getSqlStringValue());
                 types.add(Hibernate.STRING);
             } else if (searchCrit.getAttribut().equals(ExternalDepositAccountItem.SEARCH_BY_EXTERNAL_APPLICATION)) {
                 sb.append(" and externalDepositAccountItem.externalApplicationId" + searchCrit.getComparatif() + " ?");
                 values.add(searchCrit.getSqlStringValue());
                 types.add(Hibernate.STRING);
             }
         }
     }
 
     private void ticketingContractCritereToHQL(Set<Critere> criteria, StringBuffer sb, 
             List<Object> values, List<Type> types ) {
         for (Critere searchCrit : criteria) {
             if (searchCrit.getAttribut().equals(ExternalTicketingContractItem.SEARCH_BY_EXTERNAL_HOME_FOLDER)) {
                 sb.append(" and externalTicketingContractItem.externalHomeFolderId in " + 
                         searchCrit.getStringValue());
             } else if (searchCrit.getAttribut().equals(ExternalTicketingContractItem.SEARCH_BY_EXTERNAL_SERVICE_LABEL)) {
                 sb.append(" and externalTicketingContractItem.externalServiceLabel " + searchCrit.getComparatif() + " ?");
                 values.add(searchCrit.getSqlStringValue());
                 types.add(Hibernate.STRING);
             } else if (searchCrit.getAttribut().equals(ExternalTicketingContractItem.SEARCH_BY_EXTERNAL_TICKETING_CONTRACT_ID)) {
                 sb.append(" and externalTicketingContractItem.externalItemId " + searchCrit.getComparatif() + " ?");
                 values.add(searchCrit.getSqlStringValue());
                 types.add(Hibernate.STRING);
             } else if (searchCrit.getAttribut().equals(ExternalTicketingContractItem.SEARCH_BY_BROKER)) {
                 sb.append(" and externalTicketingContractItem.supportedBroker" + searchCrit.getComparatif() + " ?");
                 values.add(searchCrit.getSqlStringValue());
                 types.add(Hibernate.STRING);
             } else if (searchCrit.getAttribut().equals(ExternalTicketingContractItem.SEARCH_BY_EXTERNAL_APPLICATION)) {
                 sb.append(" and externalTicketingContractItem.externalApplicationId" + searchCrit.getComparatif() + " ?");
                 values.add(searchCrit.getSqlStringValue());
                 types.add(Hibernate.STRING);
             }
         }
     }
 
     @SuppressWarnings("unchecked")
     public List<ExternalAccountItem> searchInvoices(final Set<Critere> criteria, final String sort, String dir,
             int recordsReturned, int startIndex) {
         StringBuffer sb = new StringBuffer("from ExternalInvoiceItem as externalInvoiceItem where 1 = 1 ");
         List<Object> values = new ArrayList<Object>();
         List<Type> types = new ArrayList<Type>();
         invoiceCritereToHQL(criteria,sb, values, types);
 
         if (dir != null && dir.equals("desc"))
             sb.append(" desc");
 
         Query query = HibernateUtil.getSession().createQuery(sb.toString());
         query.setParameters(values.toArray(new Object[0]), types.toArray(new Type[0]));
 
         if (recordsReturned > 0)
             query.setMaxResults(recordsReturned);
         query.setFirstResult(startIndex);
         return query.list();
     }
 
     @SuppressWarnings("unchecked")
     public List<ExternalAccountItem> searchDepositAccounts(final Set<Critere> criteria, final String sort, String dir,
             int recordsReturned, int startIndex) {
         StringBuffer sb = new StringBuffer("from ExternalDepositAccountItem as externalDepositAccountItem where 1 = 1 ");
         List<Object> values = new ArrayList<Object>();
         List<Type> types = new ArrayList<Type>();
         depositAccountCritereToHQL(criteria,sb, values, types);
 
         if (dir != null && dir.equals("desc"))
             sb.append(" desc");
 
         Query query = HibernateUtil.getSession().createQuery(sb.toString());
         query.setParameters(values.toArray(new Object[0]), types.toArray(new Type[0]));
 
         if (recordsReturned > 0)
             query.setMaxResults(recordsReturned);
         query.setFirstResult(startIndex);
         return query.list();
     }
 
     @SuppressWarnings("unchecked")
     public List<ExternalAccountItem> searchTicketingContracts(final Set<Critere> criteria, final String sort, String dir,
             int recordsReturned, int startIndex) {
         StringBuffer sb = new StringBuffer("from ExternalTicketingContractItem as externalTicketingContractItem where 1 = 1 ");
         List<Object> values = new ArrayList<Object>();
         List<Type> types = new ArrayList<Type>();
         ticketingContractCritereToHQL(criteria,sb, values, types);
 
         if (dir != null && dir.equals("desc"))
             sb.append(" desc");
 
         Query query = HibernateUtil.getSession().createQuery(sb.toString());
         query.setParameters(values.toArray(new Object[0]), types.toArray(new Type[0]));
 
         if (recordsReturned > 0)
             query.setMaxResults(recordsReturned);
         query.setFirstResult(startIndex);
         return query.list();
     }
 
     @SuppressWarnings("unchecked")
     public List<Payment> searchNotCommited() {
 
         StringBuffer sb = new StringBuffer()
             .append("from Payment as payment").append(" where 1 = 1 ");
 
         List<Type> typeList = new ArrayList<Type>();
         List<Object> objectList = new ArrayList<Object>();
 
         Date dateTest = DateUtils.getShiftedDate(Calendar.HOUR, -3);
 
         sb.append("and payment.initializationDate < ? ");
         objectList.add(dateTest);
         typeList.add(Hibernate.TIMESTAMP);
 
         sb.append("and payment.state = ? ");
         objectList.add(PaymentState.INITIALIZED.name());
         typeList.add(Hibernate.STRING);
 
         sb.append("and payment.commitAlert = ?");
         objectList.add(false);
         typeList.add(Hibernate.BOOLEAN);
 
         Type[] typeTab = typeList.toArray(new Type[typeList.size()]);
         Object[] objectTab = objectList.toArray(new Object[objectList.size()]);
 
 
         return HibernateUtil.getSession()
                 .createQuery(sb.toString())
                 .setParameters(objectTab, typeTab)
                 .list();
     }
    
     /**
      * A customized search method for cases where we just want the payments
      * count.
      */
     protected Long searchCount(final Set<Critere> criteria) {
 
         StringBuffer sbSelect = new StringBuffer();
         sbSelect.append("select count(*) from Payment as payment");
 
         StringBuffer sb = new StringBuffer(" where 1 = 1 ");
 
         List<Type> typeList = new ArrayList<Type>();
         List<Object> objectList = new ArrayList<Object>();
         
         // go through all the criteria and create the query
         for (Critere searchCrit : criteria) {
             if (searchCrit.getAttribut().equals(Payment.SEARCH_BY_HOME_FOLDER_ID)) {
                 sb.append(" and payment.homeFolderId " + searchCrit.getComparatif() + " ?");
                 objectList.add(searchCrit.getLongValue());
                 typeList.add(Hibernate.LONG);
 
             } else if (searchCrit.getAttribut().equals(Payment.SEARCH_BY_REQUESTER_LASTNAME)) {
                 sb.append(" and lower(payment.requesterLastName) " + searchCrit.getSqlComparatif()
                         + " lower(?)");
                 objectList.add(searchCrit.getSqlStringValue());
                 typeList.add(Hibernate.STRING);
 
             } else if (searchCrit.getAttribut().equals(Payment.SEARCH_BY_CVQ_REFERENCE)) {
                 sb.append(" and payment.cvqReference " + searchCrit.getComparatif() + " ? ");
                 objectList.add(searchCrit.getSqlStringValue());
                 typeList.add(Hibernate.STRING);
 
             } else if (searchCrit.getAttribut().equals(Payment.SEARCH_BY_BANK_REFERENCE)) {
                 sb.append(" and payment.bankReference " + searchCrit.getComparatif() + " ? ");
                 objectList.add(searchCrit.getSqlStringValue());
                 typeList.add(Hibernate.STRING);
                 
             } else if (searchCrit.getAttribut().equals(Payment.SEARCH_BY_INITIALIZATION_DATE)) {
                 sb.append(" and payment.initializationDate " + searchCrit.getComparatif() + " ?");
                 objectList.add(searchCrit.getDateValue());
                 typeList.add(Hibernate.TIMESTAMP);
                 
             } else if (searchCrit.getAttribut().equals(Payment.SEARCH_BY_PAYMENT_STATE)) {
                 sb.append(" and payment.state " + searchCrit.getComparatif() + " ?");
                objectList.add(searchCrit.getSqlStringValue());
                 typeList.add(Hibernate.STRING);
                 
             } else if (searchCrit.getAttribut().equals(Payment.SEARCH_BY_BROKER)) {
                 sb.append(" and payment.broker " + searchCrit.getComparatif() + " ?");
                 objectList.add(searchCrit.getSqlStringValue());
                 typeList.add(Hibernate.STRING);
                 
             } else if (searchCrit.getAttribut().equals(Payment.SEARCH_BY_PAYMENT_MODE)) {
                 sb.append(" and payment.paymentMode " + searchCrit.getComparatif() + " ?");
                objectList.add(searchCrit.getSqlStringValue());
                 typeList.add(Hibernate.STRING);
             }
         }
         
         sbSelect.append(sb);
         Type[] typeTab = typeList.toArray(new Type[0]);
         Object[] objectTab = objectList.toArray(new Object[0]);
         return (Long) HibernateUtil.getSession()
             .createQuery(sbSelect.toString())
             .setParameters(objectTab, typeTab)
             .iterate().next();
     }
 
     public Long count(final Set<Critere> criteria) {
         return searchCount(criteria).longValue();
     }
 
     public Long invoicesCount(final Set<Critere> criteria) {
         StringBuffer sb = new StringBuffer("select count(*) from ExternalInvoiceItem as externalInvoiceItem where 1 = 1 ");
         List<Object> values = new ArrayList<Object>();
         List<Type> types = new ArrayList<Type>();
         invoiceCritereToHQL(criteria,sb, values, types);
         return (Long) HibernateUtil.getSession()
             .createQuery(sb.toString())
             .setParameters(values.toArray(new Object[0]), types.toArray(new Type[0]))
             .iterate().next();
     }
 
     public Long depositAccountsCount(final Set<Critere> criteria) {
         StringBuffer sb = new StringBuffer("select count(*) from ExternalDepositAccountItem as externalDepositAccountItem where 1 = 1 ");
         List<Object> values = new ArrayList<Object>();
         List<Type> types = new ArrayList<Type>();
         depositAccountCritereToHQL(criteria,sb, values, types);
         return (Long) HibernateUtil.getSession()
             .createQuery(sb.toString())
             .setParameters(values.toArray(new Object[0]), types.toArray(new Type[0]))
             .iterate().next();
     }
 
     public Long ticketingContractsCount(Set<Critere> criteria) {
         StringBuffer sb = new StringBuffer("select count(*) from ExternalTicketingContractItem as externalTicketingContractItem where 1 = 1 ");
         List<Object> values = new ArrayList<Object>();
         List<Type> types = new ArrayList<Type>();
         ticketingContractCritereToHQL(criteria,sb, values, types);
         return (Long) HibernateUtil.getSession()
             .createQuery(sb.toString())
             .setParameters(values.toArray(new Object[0]), types.toArray(new Type[0]))
             .iterate().next();
     }
 }
