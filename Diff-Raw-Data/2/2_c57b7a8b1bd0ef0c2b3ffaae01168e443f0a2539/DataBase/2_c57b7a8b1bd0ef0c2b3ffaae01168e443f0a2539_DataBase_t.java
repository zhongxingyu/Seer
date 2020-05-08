 package base;
 
 import base.entities.ClaimsEntity;
 import base.entities.UsersEntity;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Persistence;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 public class DataBase {
     public static void deleteUser( String [] names)
     {
         EntityManagerFactory emf = Persistence.createEntityManagerFactory("authPU");
         EntityManager em = emf.createEntityManager();
         em.getTransaction().begin();
         for ( String name : names)
         {
             UsersEntity user = em.find(UsersEntity.class, name);
             em.remove(user);
         }
         em.getTransaction().commit();
     }
     public static void addUser( Map<String,String[]> parameters)
     {
         EntityManagerFactory emf = Persistence.createEntityManagerFactory("authPU");
         EntityManager em = emf.createEntityManager();
         em.getTransaction().begin();
         UsersEntity user = new UsersEntity();
         user.setUserName(parameters.get("user-name")[0]);
         user.setUserPass(parameters.get("user-password")[0]);
 
         List<String> roles = new ArrayList<String>();
         roles.add(parameters.get("user-role")[0]);
 
         user.setRoles(roles);
         em.persist(user);
         em.getTransaction().commit();
     }
 
     public static void addClaim( Map<String, String[]> params)
     {
         EntityManagerFactory emf = Persistence.createEntityManagerFactory("persUnit");
         EntityManager em = emf.createEntityManager();
         em.getTransaction().begin();
         ClaimsEntity clm = new ClaimsEntity( params.get("name")[0]);
         clm.setTelephone(params.get("telephone")[0]);
         clm.setBuildings_list(params.get("buildings_list")[0]);
         clm.setRoom(params.get("room")[0]);
         clm.setDevice_type(params.get("device_type")[0]);
         clm.setDevice_number(params.get("device_number")[0]);
         clm.setProblem_description(params.get("problem_description")[0]);
         clm.setPriority(params.get("priority")[0]);
         em.persist( clm);
         em.getTransaction().commit();
     }
 
     public static List<UsersEntity> listUsers()
     {
         EntityManagerFactory emf = Persistence.createEntityManagerFactory("authPU");
         EntityManager em = emf.createEntityManager();
         return em.createQuery("select u from UsersEntity u").getResultList();
     }
 
 
     public static List<ClaimsEntity> listClaims()
     {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("persUnit");
         EntityManager em = emf.createEntityManager();
         return em.createQuery("select m from ClaimsEntity m").getResultList();
     }
 }
