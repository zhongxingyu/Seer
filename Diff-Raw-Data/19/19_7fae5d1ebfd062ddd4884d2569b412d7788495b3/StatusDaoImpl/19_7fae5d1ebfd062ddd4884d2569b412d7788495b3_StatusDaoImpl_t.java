 package com.rcviana.dao.impl;
 
import java.sql.Timestamp;

 import org.hibernate.Session;
 
 import com.rcviana.dao.StatusDao;
 import com.rcviana.persistance.HibernateUtil;
 import com.rcviana.qualifier.StatusQualifierDb;
 
 /**
  * 
  * @author Robson Viana
  *
  */
 @StatusQualifierDb
 public class StatusDaoImpl implements StatusDao {
 
 	public String getStatusDB() {
 		
 		Session session = HibernateUtil.getSessionFactory().openSession();
 		
		Timestamp sysdate = (Timestamp)session.createSQLQuery(
				"SELECT SYSDATE();")
 				.uniqueResult();
 		
		return sysdate.toString();
 	}
 }
