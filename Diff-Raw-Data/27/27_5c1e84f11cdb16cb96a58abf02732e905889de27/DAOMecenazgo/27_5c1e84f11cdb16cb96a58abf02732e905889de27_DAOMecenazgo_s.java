 /**
  * Clase DAOMecenazgo: Objeto de acceso de datos para la clase Mecenazgo
  * 
  * @author Jerson Chinchilla
  * @author Matthew Bertelsen
  * @author Mauricio Fernandez
  * 	@version 18/08/2013		 
  */
 package logica;
 
 import javax.persistence.*;
 
 import logica.*;
 
 public class DAOMecenazgo {
 	
 	/**
 	 * Crea una instancia en la tabla de mecenazgos.
 	 * @param pmecenazgo: Objeto Mecenazgo.
 	 */	
 	public static void crear(Mecenazgo pmecenazgo){
 		EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
 		em.getTransaction().begin();
 		try{
 			em.persist(pmecenazgo);
 			em.getTransaction().commit();
 		}
 		catch ( Exception ex ){
 			em.getTransaction().rollback();
 			System.out.println("Error al guardar");
 			ex.printStackTrace();
 		}
 		finally{
 			em.close();
 		}
 	}
 	
 	/**
 	 * Busca una instancia en la tabla de mecenazgos usando el id del mecenazgo.
 	 * @param pidMecenazgo: id del mecenazgo
 	 */	
 	public static Mecenazgo buscar(Mecenazgo pMecenazgo){
 		EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
 		Mecenazgo mecenazgo = null;
 		try{
			mecenazgo = em.find(Mecenazgo.class, pMecenazgo.getId());
 		}
 		catch (Exception ex){
 			System.out.println("Error");
 			ex.printStackTrace();
 		}
 		finally{
 			em.close();
 		}
 		return mecenazgo;
 	}
 	
 	/**
 	 * Actualiza una instancia en la tabla de mecenazgos.
 	 * @param pmecenazgo: Objeto Mecenazgo.
 	 */	
 	public static void actualizar(Mecenazgo pmecenazgo){
 		EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
 		EntityTransaction tx = em.getTransaction();
 		tx.begin();
 		try{
 			em.merge(pmecenazgo);
 			tx.commit();
 			System.out.println("Actualizacion exitosa");
 		}
 		catch(Exception ex){
 			tx.rollback();
 			System.out.println("Error");
 			ex.printStackTrace();
 		}
 		finally{
 			em.close();
 		}
 	}
 	
 	/**
 	 * Actualiza una instancia en la tabla de mecenazgos.
 	 * @param pmecenazgo: Objeto Mecenazgo.
 	 */		
 	public static void borrar(Mecenazgo pmecenazgo){
 		EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
 		em.getTransaction().begin();
 		try{
 			Mecenazgo pMerge = em.merge(pmecenazgo);
 		    em.remove(pMerge);
 			em.getTransaction().commit();
 			System.out.println("Borrado exitoso");
 		}
 		catch (Exception ex){
 			ex.printStackTrace();
 		}
 		finally{
 			em.close();
 		}
 	}
 
 }
