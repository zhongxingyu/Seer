 package com.scholastic.sbam.server.database.objects;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.hibernate.Criteria;
 import org.hibernate.SQLQuery;
 import org.hibernate.criterion.Order;
 import org.hibernate.criterion.Restrictions;
 
 import com.extjs.gxt.ui.client.Style.SortDir;
 import com.scholastic.sbam.server.database.codegen.AgreementContact;
 import com.scholastic.sbam.server.database.codegen.AgreementContactId;
 import com.scholastic.sbam.server.database.codegen.Contact;
 import com.scholastic.sbam.server.database.util.HibernateAccessor;
 import com.scholastic.sbam.shared.objects.AgreementContactInstance;
 import com.scholastic.sbam.shared.objects.ContactInstance;
 
 /**
  * Sample database table accessor class, extending HibernateAccessor, and implementing custom get/find methods.
  * 
  * @author Bob Lacatena
  *
  */
 public class DbAgreementContact extends HibernateAccessor {
 	
 	static String objectName = AgreementContact.class.getSimpleName();
 	
 	public static AgreementContactInstance getInstance(AgreementContact dbInstance) {
 		AgreementContactInstance instance = new AgreementContactInstance();
 
 		instance.setAgreementId(dbInstance.getId().getAgreementId());
 		instance.setContactId(dbInstance.getId().getContactId());
 		
 		instance.setRenewalContact(dbInstance.getRenewalContact());
 		
 		instance.setStatus(dbInstance.getStatus());
 		instance.setCreatedDatetime(dbInstance.getCreatedDatetime());
 		
 		return instance;
 	}
 	
 	public static AgreementContact getById(int agreementId, int contactId) {
 		AgreementContactId asid = new AgreementContactId();
 		asid.setAgreementId(agreementId);
 		asid.setContactId(contactId);
 		try {
 			AgreementContact instance = (AgreementContact) sessionFactory.getCurrentSession().get(getObjectReference(objectName), asid);
 			return instance;
 		} catch (RuntimeException re) {
         	re.printStackTrace();
             System.out.println(re.getMessage());
 			throw re;
 		}
 	}
 	
 	public static List<AgreementContact> findAll() {
 		List<Object> results = findAll(objectName);
 		List<AgreementContact> reasons = new ArrayList<AgreementContact>();
 		if (results != null)
 			for (int i = 0; i < results.size(); i++)
 				reasons.add((AgreementContact) results.get(i));
 		return reasons;
 	}
 	
 	public static List<AgreementContact> findByAgreementId(int agreementId, char status, char neStatus) {
         try
         {
             Criteria crit = sessionFactory.getCurrentSession().createCriteria(getObjectReference(objectName));
             if (agreementId > 0)
             	crit.add(Restrictions.eq("id.agreementId", agreementId));
             if (status != 0)
             	crit.add(Restrictions.like("status", status));
             if (neStatus != 0)
             	crit.add(Restrictions.ne("status", neStatus));
             crit.addOrder(Order.asc("id.contactId"));
             @SuppressWarnings("unchecked")
 			List<AgreementContact> objects = crit.list();
             return objects;
         }
         catch(Exception e)
         {
         	e.printStackTrace();
             System.out.println(e.getMessage());
         }
         return new ArrayList<AgreementContact>();
 	}
 	
 	public static List<Object []> findFiltered(String filter, boolean doBoolean, char status, char neStatus) {
 		return findFiltered(filter, doBoolean, status, neStatus, null, null);
 	}
 	
 	public static List<Object []> findFiltered(String filter, boolean doBoolean, char status, char neStatus, String sortField, SortDir sortDirection) {
         try
         {
         	if (filter == null || filter.trim().length() == 0)
         		return new ArrayList<Object []>();
         		
 //        	AppConstants.TypedTerms typedTerms = AppConstants.parseTypedFilterTerms(filter);
         		
         	String sqlQuery = "SELECT {agreement.*}, {agreement_contact.*}, {contact.*} FROM agreement, agreement_contact, contact WHERE agreement.`status` <> '" + neStatus + "' " +
         						" AND agreement_contact.status <> '" + neStatus + "' " +
         						" AND agreement.id = agreement_contact.agreement_id " + 
         						" AND agreement_contact.contact_id = contact.contact_id ";
         	
         	filter = filter.replaceAll("'", "''");
 			if (doBoolean) {
 				sqlQuery += " AND ( MATCH (full_name,address1,city,zip) AGAINST ('" + filter + "' IN BOOLEAN MODE)";
 				sqlQuery += " OR MATCH (e_mail,e_mail_2,phone,phone_2,fax) AGAINST ('" + filter + "' IN BOOLEAN MODE)";
 				sqlQuery += " OR MATCH (contact.note) AGAINST ('" + filter + "' IN BOOLEAN MODE) ) ";
 			} else {
 				sqlQuery += " AND MATCH (full_name,address1,city,zip) AGAINST ('" + filter + "')";
 				sqlQuery += " OR MATCH (e_mail,e_mail_2,phone,phone_2,fax) AGAINST ('" + filter + "')";
 				sqlQuery += " OR MATCH (contact.note) AGAINST ('" + filter + "') ) ";
 			}
         	
         	
 //        	if (typedTerms.getNumbers().size() > 0) {
 //	        	sqlQuery += " AND ( ";
 //	        	for (int i = 0; i < typedTerms.getNumbers().size(); i++) {
 //	        		if (i > 0) sqlQuery += " OR ";
 //	        		String numberLike = " like '%" + typedTerms.getNumbers().get(i) + "%' ";
 //	        		sqlQuery += " agreement.id" + numberLike;
 //	        		sqlQuery += " OR agreement.id_check_digit" + numberLike;
 //	        	}
 //	        	sqlQuery += " ) ";
 //        	}
         	
 //			We take advantage of the MATCH and full text index for this part
 //        	if (typedTerms.getWords().size() > 0) {
 //        		sqlQuery += " AND ( ";
 //        		for (int i = 0; i < typedTerms.getWords().size(); i++) {
 //        			if (i > 0) sqlQuery += " OR ";
 //        			String wordLike = " like '%" + typedTerms.getWords().get(i) + "%' ";
 //        			sqlQuery += " agreement_contact.note" + wordLike;
 //        			sqlQuery += " OR contact.description" + wordLike;
 //        			//	Only apply it to contact code if it's a word, not a phrase (i.e. no blanks)
 //        			if (typedTerms.getWords().get(i).indexOf(' ') < 0)
 //        				sqlQuery += " OR contact.contact_code" + wordLike;
 //        		}
 //        		sqlQuery += " ) ";
 //        	}
         	
  //       	System.out.println(sqlQuery);
         	
             sqlQuery += " order by ";
             if (sortField != null && sortField.length() > 0) {
             	sqlQuery += sortField;
             	if (sortDirection == SortDir.DESC)
             		sqlQuery += " DESC,";
             	else
             		sqlQuery += ",";
             }
             sqlQuery += " agreement_contact.agreement_id, contact.full_name";
             
             SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(sqlQuery);
             
             query.addEntity("agreement",		getObjectReference("Agreement"));
             query.addEntity("agreement_contact",getObjectReference("AgreementContact"));
             query.addEntity("contact",			getObjectReference("Contact"));
             
             @SuppressWarnings("unchecked")
 			List<Object []> objects = query.list();
             return objects;
         }
         catch(Exception e)
         {
         	e.printStackTrace();
             System.out.println(e.getMessage());
         }
         return new ArrayList<Object []>();
 	}
 
 	public static void setDescriptions(AgreementContactInstance instance) {
 		if (instance.getContact() == null) {
 			if (instance.getContactId() > 0) {
 				Contact contact = DbContact.getByCode(instance.getContactId());
 				if (contact != null) {
 					instance.setContact(DbContact.getInstance(contact));
 					DbContact.setDescriptions(instance.getContact());
 				} else
 					instance.setContact(ContactInstance.getUnknownInstance(instance.getContactId()));
 			} else {
 				instance.setContact(ContactInstance.getEmptyInstance());
 			}
 		}
 	}
 }
