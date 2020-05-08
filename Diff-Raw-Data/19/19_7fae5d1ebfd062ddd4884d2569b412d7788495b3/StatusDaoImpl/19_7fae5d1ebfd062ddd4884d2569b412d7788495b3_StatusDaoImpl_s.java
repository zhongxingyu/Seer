 package com.rcviana.dao.impl;
 
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
 		
		String statusDB = (String)session.createSQLQuery(
				"SELECT TO_CHAR(sysdate, 'YYYY-MM-DD HH24:MI:SS') DATETIME " +
				"FROM SYS.DUAL")
 				.uniqueResult();
 		
		return statusDB;
 	}
 }
