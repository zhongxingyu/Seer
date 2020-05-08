 package client;
 
 import java.util.List;
 
 import net.berinle.model.Item;
 
 import org.apache.log4j.Logger;
 import org.hibernate.SessionFactory;
 import org.hibernate.cfg.AnnotationConfiguration;
 import org.hibernate.classic.Session;
 import org.hibernate.criterion.Restrictions;
 
 import org.hibernate.*;
 import java.util.*;
 import java.lang.StringBuffer;
 
 public class SybaseInClause {
 	private static Logger log = Logger.getLogger(SybaseInClause.class);
 	public static void main(String[] args) {
 		SessionFactory sf = new AnnotationConfiguration().configure("hibernate-sybase.cfg.xml").buildSessionFactory();
 		Session s = sf.openSession();
 		
 		List<Integer> ids = s.createQuery("select id from Item").list();
 		log.debug("ids: " + ids.size());
 		
 		
 		Criteria crit = s.createCriteria(Item.class);
 		
 		/*List result = s.createCriteria(Item.class)
 		.add(Restrictions.in("id", ids))
 		.list();*/
 		
 		List result = new ArrayList();
 		List subList = new ArrayList();
 		int startIndex = 0, endIndex = 0;
 		String params = "";
 		if(ids.size() > 100){
 			int totalSize = ids.size();
 			
 			while(endIndex != (totalSize-1)){
 				endIndex = startIndex + 100;
 				subList = ids.subList(startIndex, endIndex);
 				params = getParameterList(subList);
				crit.add(Restrictions.in("id", subList));
 				
 				result.add(crit.list());
 				
 				startIndex = endIndex;
 				if((endIndex + 100) < (totalSize-1)){
 					endIndex += 100;
 				} else{
 					endIndex = totalSize-1;
 				}
 			}
 			
 			subList = ids.subList(startIndex, endIndex);
 			params = getParameterList(subList);
			crit.add(Restrictions.in("id", subList));
 							
 			result.add(crit.list());			
 		}
 		
 		s.beginTransaction();
 		s.getTransaction().commit();
 		s.close();
 		sf.close();
 		
 		log.debug("result: " + result.size());
 	}
 	
 	public static String getParameterList(List aList){
 		StringBuffer sb = new StringBuffer();
 		for(int i=0; i<aList.size()-1; i++){
 			sb.append(aList.get(i) + ",");		
 		}
 		sb.append(aList.get(aList.size()-1));
 		
 		return sb.toString();
 	}
 }
