 package com.socialathlete.dao;
 
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceUnit;
 import javax.persistence.Query;
 import java.util.List;
 import java.util.Iterator;
 import java.util.ArrayList;
 import javax.persistence.NoResultException;
 
 
 import org.springframework.stereotype.Service;
 
 import com.socialathlete.domain.SAUser;
import com.socialathlete.web.sausersignup.SAUserSignUpService;
 
 @Service
 public class UserDAOImpl implements UserDAO {
 	
 	private EntityManagerFactory emf;
 	
 	
 	@PersistenceUnit
 	public void setEntityManagerFactory(EntityManagerFactory emf) {
         this.emf = emf;
     }
 	
 	public List<String> getFollowersByUserId(String userid){
 		EntityManager em = this.emf.createEntityManager();
 		try {
 			Query query = em.createQuery("SELECT sa.accountHandle  from SAUser u join u.following p join p.socialAccount sa where u.username = ?1");
 			query.setParameter(1, userid);
 			List results = query.getResultList();
 			ArrayList <String> result = new ArrayList();
 			if(results.size()!= 0)
 			{
 				Iterator it = results.iterator();
 				while(it.hasNext())
 				{
 					result.add((String) it.next());
 				}
 				
 			}
 			
 			return result;
 		}
 		finally {
 			if (em != null) {
 				em.close();
 			}
 			
 		}
 	}
 	
 	public List<String> getTeamsByLeague(String league){
 		EntityManager em = this.emf.createEntityManager();
 		try {
 			Query query = em.createQuery("SELECT st.teamName  from SATeam st join st.league p where p.leagueName = ?1");
 			query.setParameter(1, league);
 			List results = query.getResultList();
 			ArrayList <String> result = new ArrayList();
 			if(results.size()!= 0)
 			{
 				Iterator it = results.iterator();
 				while(it.hasNext())
 				{
 					result.add((String) it.next());
 				}
 				
 			}
 			
 			return result;
 		}
 		finally {
 			if (em != null) {
 				em.close();
 			}
 			
 		}
 	}
 	
 	public SAUser getUserbyUsername(String username){
 		EntityManager em = this.emf.createEntityManager();
 		try {
 			
 			SAUser result = null;
 			
 			try{
 			Query query = em.createQuery("from SAUser u where u.username = ?1");
 			query.setParameter(1, username);
 			result = (SAUser) query.getSingleResult();
 		
 			}
 			catch(Exception e)
 			{
 				result = null;
 				return result;
 			}
 			
 			
 			return result;
 		}
 		finally {
 			if (em != null) {
 				em.close();
 			}
 			
 		}
 	}
 }
