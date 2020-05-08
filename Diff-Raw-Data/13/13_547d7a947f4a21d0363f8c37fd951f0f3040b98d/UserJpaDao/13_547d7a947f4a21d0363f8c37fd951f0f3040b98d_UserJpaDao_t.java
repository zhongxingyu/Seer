 package jacs.database.exceed.jpa;
 
 import jacs.database.exceed.dao.UserDAO;
 import jacs.database.exceed.model.User;
 
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.EntityTransaction;
 import javax.persistence.Persistence;
 import javax.persistence.PersistenceUnit;
 import javax.persistence.PersistenceUnitUtil;
 import javax.persistence.Query;
 
 /**
  * @author GoDParTicle
  *
  */
 public class UserJpaDao implements UserDAO{
 	private static final String PERSISTENCE_UNIT = "exceedvote";
 	
 	private static EntityManagerFactory emf;
 	private static EntityManager em;
 	static {
 		emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT);
 		em = emf.createEntityManager();
 	}
 	
 	public  String regisUser(String username,String password) {
 		System.out.printf("Saving username : %s\n", username);
 		EntityTransaction tx = em.getTransaction();
 		try {
 			if(!checkMatchUser(findUserByName(username)))	{
 				User user = new User(username, password, "student");
 				tx.begin();
 				em.persist(user);
 				tx.commit();
 				System.out.printf("User saved. id = %d : Username = %s\n",user.getId(),user.getUsername());
 				System.out.printf("Save User Complete !!! \n");
 				return "REGIS_SUCCESS" + ","+ user.getUsername() +","+user.getType();
 			}
 		} catch (Exception ex) {
 			System.out.println("Exception occurred!");
 			if (tx.isActive()) tx.rollback();
 		}
 		return "REGIS_FAILED : "+username+" - Already used.";
 	}
 	public String deleteUser(String username,String password)	{
 		EntityTransaction tx = em.getTransaction();
 		try	{
 			if(checkMatchUser(findUserByName(username)))	{
 				User user = findUserByName(username);
 				tx.begin();
 				em.remove(user);
 				tx.commit();
 				System.out.printf("Remove. id = %d : Username = %s\n", user.getId(),user.getUsername());
 				return "REMOVE_SUCCESS";
 			}
 		}catch(Exception ex)	{
 			System.out.println("Exception occurred!");
 			if(tx.isActive()) tx.rollback();
 		}
 		return "REMOVE_FAILED";
 	}
 	public  String loginUser(String username,String password) {
		User user = findUserByName(username);
		String msg = "";
 		if(!checkMatchUser(user)) return "LOGIN_FAILED";
		else{
			if(user.getType().equalsIgnoreCase("committee"))	{
				msg = "LOGIN_SUCCESS,"+user.getUsername()+","+user.getType()+",5";
			}else	{
				msg = "LOGIN_SUCCESS,"+user.getUsername()+","+user.getType()+",1";
			}
		}
		return msg;
 	}
 	
 	public String changePassword(User user,String new_password)	{
 		System.out.printf("change password user : %s\nOld password : %s\nNew password : %s\n",user.getUsername(),user.getPassword(),new_password);
 		EntityTransaction tx = em.getTransaction();
 		String msg = "null";
 		try{
 			tx.begin();
 			User u = em.find(User.class,(int)user.getId());
 			u.setPassword(new_password);
 			em.merge(u);
 			tx.commit();
 			msg = "CHANGE_PASSWORD_SUCCESSED,"+u.getUsername()+","+u.getPassword();
 		}catch(Exception ex)	{
 			System.err.println("Exception occured!");
 			if(tx.isActive()) tx.rollback();
 		}finally	{
 			em.close();
 		}
 		
 		if(msg.equalsIgnoreCase("null"))
 			return "CHANGE_PASSWORD_FAILED,"+user.getUsername()+","+user.getPassword();
 		else 
 			return msg;
 	}
 	
 	public boolean checkMatchUser(User user)	{
 		if( user == null)	return false;
 		return true;
 	}
 	public User findUserByName(String username)	{
 		Query query =
 				em.createQuery("SELECT user from User user WHERE user.username = :username");
 		query.setParameter("username", username);
 		List<User> result = query.getResultList();
 		if(!(result.size() == 0))	return result.get(0);
 		return null;
 	}
 	
 	public List<User> findAllUser()	{
 		Query query = em.createQuery("SELECT user from User user ORDER by user.type");
 		List<User> result = query.getResultList();
 		return result;
 	}
 	
 
 }
