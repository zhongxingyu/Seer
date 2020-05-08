 package com.sanisidro.service;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Persistence;
 import javax.persistence.Query;
 
 import com.sanisidro.Exception.AppExceptions;
 import com.sanisidro.entity.User;
 import com.sanisidro.entity.UserType;
 import com.sanisidro.to.UserTO;
 import com.sanisidro.to.UserTypeTO;
 
 public class UserService {
 	
 	public static long coutUsers () throws Exception
 	{
 		long totalUsers = 0;
 		EntityManagerFactory emf = Persistence.createEntityManagerFactory("SanIsidro");
 		EntityManager em = emf.createEntityManager();			
 		Query query =  em.createQuery("select count(u) from User u");	
 		
 		
 		Number result = (Number) query.getSingleResult();
 		totalUsers = result.longValue();
 		if (em != null)
 		{
 			em.close();
 		}
 		
 		return totalUsers;
 	}
 	
 	public static List <UserTO> getAllCustomers(int first, int maxResutl, UserTO userTO)
 	{
 		List<UserTO> userTOs = new ArrayList<UserTO>();
 		try 
 		{			
 			EntityManagerFactory emf = Persistence.createEntityManagerFactory("SanIsidro");
 			EntityManager em = emf.createEntityManager();			
			Query query =  em.createQuery("select u from User");
 			//query.setParameter("1", userTO.getDni() + "%");
 			query.setFirstResult(first);
 			query.setMaxResults(maxResutl);
 			@SuppressWarnings("unchecked")
 			List <User> list = query.getResultList();
 			for (User s: list)
 			{				
 				userTOs.add(GenericEntityTO.getTO(s, new UserTO()));
 			}
 			if (em != null)
 			{
 				em.close();
 			}
 			
 		}
 		catch (Exception e) 
 		{
 			System.out.println("Error getAllCustomers(): " + e.getMessage());
 		}
 		
 		return userTOs;
 		
 	}	
 	
 	public static UserTO updateUser (UserTO userTO) throws Exception
 	{
 		UserTO u = GenericService.find(new User(), new UserTO(), userTO.getId_user());
 		if (u.getDni() == userTO.getDni())
 		{
 			if (u.getType().getId() != userTO.getType().getId())
 			{
 				userTO.setType(GenericService.find(new UserType(), new UserTypeTO(), new Long (userTO.getType().getId())));
 			}
 			userTO = GenericService.update(new User(), userTO);
 			return userTO;
 		}
 		else if (!existDni(userTO))
 		{
 			userTO = GenericService.update( new User(), userTO);			
 			return userTO;
 		}
 		else
 		{
 			throw new Exception(AppExceptions.DUPLICATE_DNI);
 		}	
 		
 	}
 	
 	public static UserTO createUser(UserTO userTO) throws Exception
 	{				
 		if (!existDni(userTO))
 		{
 			userTO = GenericService.create( new User(), userTO);			
 			return userTO;
 		}
 		else
 		{
 			throw new Exception(AppExceptions.DUPLICATE_DNI);
 		}
 		
 	}
 	
 	public static boolean existDni(UserTO userTO)
 	{
 		boolean exist = false;
 		
 		EntityManagerFactory emf = Persistence.createEntityManagerFactory("SanIsidro");
 		EntityManager em = emf.createEntityManager();
 		Query query = em.createQuery("select u from User u where u.dni = ?1");
 		query.setParameter(1, userTO.getDni());
 		@SuppressWarnings("unchecked")
 		List<User> list = query.getResultList();
 		if (list.size() > 0)
 		{
 			exist = true;
 		}
 		if (em != null)
 		{
 			em.close();
 		}
 		
 		return exist;
 	}
 
 }
