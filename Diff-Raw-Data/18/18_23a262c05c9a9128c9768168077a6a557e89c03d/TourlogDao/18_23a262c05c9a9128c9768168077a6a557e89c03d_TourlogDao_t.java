 package dao;
 
 import java.util.ArrayList;
 
 import org.hibernate.HibernateException;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 
 import tables.*;
 
 public class TourlogDao {
 	Session session=HibernateSessionFactory.currentSession();
 	public void close(){
 		session.close();
 	}
 	public void addTourlog(Tourlog ad){
 
 		
 		Transaction tran=session.beginTransaction();
 		session.save(ad);
 	    tran.commit();
 		
 	} 
 	public Tourlog findTourlogyid(int  id){
 
 		
 		Transaction tran=session.beginTransaction();
 		String hql="from Tourlog where tourLogId=' "+id+"'";
 		Query query =session.createQuery(hql);
 		Tourlog ad=(Tourlog)query.uniqueResult();
 	    tran.commit();
 		return ad;
 		
 	} 
 	public void updateTourlog(Tourlog ad){
 
 		
 		Transaction tran=session.beginTransaction();
 		session.update(ad);
 	    tran.commit();
 		
 	} 
 	public void deleteTourlog(Tourlog ad){
 
 		
 		Transaction tran=session.beginTransaction();
 		session.delete(ad);
 	    tran.commit();
 		
 	} 
 	public ArrayList<Tourlog> getTourlogAll(){
 
 		
 		Transaction tran=session.beginTransaction();
		String hqlsql = "from Tourlog order by tourLogId desc";
 		 Query query = session.createQuery(hqlsql);
 		 ArrayList<Tourlog> resultStu = (ArrayList<Tourlog>)query.list();
 	    tran.commit();
 	
 		return resultStu;
 	} 
 	 public ArrayList<Tourlog> queryByPage(int pageSize, int pageNow) {
 	       
 	        ArrayList<Tourlog> sftlist = new ArrayList();
 	        try {
 	            
 	            //String hql = "from Employee emp";// ѯHQL
	           String HQL = "from Tourlog order by tourLogId desc";// ѯHQL
 	            Query q = session.createQuery(HQL);// ִвѯ
 	            q.setFirstResult(pageSize * (pageNow - 1));
 	            q.setMaxResults(pageSize);
 	            sftlist = (ArrayList<Tourlog>)q.list();
 	        } catch (HibernateException e) {
 	            e.printStackTrace();
 	            System.out.println("ѯʧ");
 	        } finally {
 	        	HibernateSessionFactory.closeSession();// رsession
 	        }
 	        return sftlist;
 	    }
 	 public ArrayList<Tourlog> queryByPageUser(ArrayList<Userinfo> ulist,int pageSize, int pageNow) {
 	       
 	        ArrayList<Tourlog> sftlist = new ArrayList();
 	        try {
 
 	        	String temp="";
 	        	if(ulist.isEmpty()){
 	        		return sftlist;
 	        	}
 	        	
 	        	int i;
 	        	for(i=0;i<ulist.size()-1;i++){
 	        		temp+=ulist.get(i).getUid()+",";
 	        		System.out.println(temp);
 	        	}
 	        	temp+=ulist.get(i).getUid();
	        	String Hql="select d from Tourlog d where d.author in ("+temp+") order by tourLogId desc";
 	            Query q = session.createQuery(Hql);// ִвѯ
 	            q.setFirstResult(pageSize * (pageNow - 1));
 	            q.setMaxResults(pageSize);
 	            sftlist = (ArrayList<Tourlog>)q.list();
 	        } catch (HibernateException e) {
 	            e.printStackTrace();
 	            System.out.println("ѯʧ");
 	        } finally {
 	        	HibernateSessionFactory.closeSession();// رsession
 	        }
 	        return sftlist;
 	    }
 }
