 /**
  *  Abstract POJO for data persistence.
  */
 package fr.lalourche.model;
 
 import java.util.List;
 
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.MappedSuperclass;
 
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.Transaction;
 
 
 /**
  * @author Lalourche
  *
  */
 @MappedSuperclass
public abstract class Entity
 {
 
   /** Id. */
   @Id
   @GeneratedValue
   private Long id_;
 
   /**
    * @return the id
    */
   public final Long getId()
   {
     return id_;
   }
 
   /**
    * @param id the id to set
    */
   public final void setId(Long id)
   {
     id_ = id;
   }
 
   /**
    * Lists the entities.
    * @param clazz the class to query
    * @return the entities list.
    */
   public static List<? extends Entity> list(Class<? extends Entity> clazz)
   {
     SessionFactory sf = HibernateUtil.getSessionFactory();
     Session session = sf.openSession();
 
     String query = "from " + clazz.getSimpleName();
     List<Message> messages = session.createQuery(query).list();
 
     session.close();
 
     return messages;
   }
 
   /**
    * Reads an entity.
    * @param id the id of the entity
    * @param clazz the class to query
    * @return the entity.
    */
   public static Entity read(Long id, Class<? extends Entity> clazz)
   {
     SessionFactory sf = HibernateUtil.getSessionFactory();
     Session session = sf.openSession();
 
     Entity e = (Entity) session.get(clazz, id);
     session.close();
     return e;
   }
 
   /**
    * Saves current entity in database.
    */
   public final void save()
   {
     SessionFactory sf = HibernateUtil.getSessionFactory();
     Session session = sf.openSession();
 
     Transaction transaction = session.beginTransaction();
 
     Long id = (Long) session.save(this);
     session.flush();
     setId(id);
 
     transaction.commit();
 
     session.close();
   }
 
   /**
    * Updates current entity in database.
    */
   public final void update()
   {
     SessionFactory sf = HibernateUtil.getSessionFactory();
     Session session = sf.openSession();
 
     session.beginTransaction();
 
     session.merge(this);
 
     session.getTransaction().commit();
 
     session.close();
   }
 
   /**
    * Deletes current entity from database.
    */
   public final void delete()
   {
     SessionFactory sf = HibernateUtil.getSessionFactory();
     Session session = sf.openSession();
 
     Transaction transaction = session.beginTransaction();
 
     session.delete(this);
 
     transaction.commit();
 
     session.close();
   }
 
   /**
    * Delete all the entities of a specified class.
    * @param clazz the class to delete
    */
   public static void deleteAll(Class<? extends Entity> clazz)
   {
     SessionFactory sf = HibernateUtil.getSessionFactory();
     Session session = sf.openSession();
 
     Transaction transaction = session.beginTransaction();
 
     String query = "delete from " + clazz.getSimpleName();
     session.createQuery(query).executeUpdate();
 
     transaction.commit();
 
     session.close();
   }
 
   /**
    * Counts all the entities of a specified class.
    * @param clazz the class to count
    * @return the count
    */
   public static long count(Class<? extends Entity> clazz)
   {
     Long result;
 
     SessionFactory sf = HibernateUtil.getSessionFactory();
     Session session = sf.openSession();
 
     String query = "select count(*) from " + clazz.getSimpleName();
     result = (Long) session.createQuery(query).iterate().next();
 
     session.close();
 
     return result.longValue();
   }
 
   //CHECKSTYLE:OFF There is a need to implement this method
   //also in the mother class.
   /* (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
     return "(" + id_ + ")";
   }
  //CHECKSTYLE:ON
 }
