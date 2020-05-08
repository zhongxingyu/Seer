 /*
  */
 package org.fit.pis.library.data;
 
 import java.util.Date;
 import java.util.List;
 import javax.ejb.Stateless;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Query;
 
 /**
  *
  * @author Lukáš Černý <cerny.l@gmail.com>
  */
 @Stateless
 public class ExemplarManager {
     @PersistenceContext
     private EntityManager em;
 
     public void save(Exemplar e)
     {
     	em.merge(e);
     }
 	
     public void remove(Exemplar e)
     {
     	em.remove(em.merge(e));
     }
         
 	public void flush()
 	{
 		em.flush();
 	}
 
	/**
	 * Refreah Exemplar
	 */
	public void refresh(Exemplar e) {
		em.flush();
		em.refresh(e);
	}
	
 	public Exemplar findByIdexemplar(Integer id) {
 		em.flush();
 		
 		try {
 			Query query = em.createNamedQuery("Exemplar.findByIdexemplar");
 			query.setParameter("idexemplar", id);
 			return (Exemplar) query.getSingleResult();
 		} catch (Exception e) {
 			return null;
 		}
 	}
 	
 	/**
 	 * Find book exemplars
 	 * @param book
 	 * @return 
 	 */
 	public List<Exemplar> findByBook(Book book) {
//		em.flush();
 		
//		try {
 			Query query = em.createNamedQuery("Exemplar.findByBook");
 			query.setParameter("book", book);
			
			// refresh info
			List<Exemplar> exemplars = query.getResultList();
			for (Exemplar e : exemplars) {
				refresh(e);
			}
			
			return exemplars;
//		} catch (Exception e) {
//			return null;
//		}
 	}
 	
 	/**
 	 * Find book exemplars
 	 * @param book
 	 * @return 
 	 */
 //	public List<Exemplar> findByBookUser(Book book, User user) {
 //		em.flush();
 //		
 //		try {
 //			Query query = em.createNamedQuery("Exemplar.findByBookUserDate");
 //			query.setParameter("book", book);
 //			query.setParameter("user", user);
 //			query.setParameter("date", null);
 //			return query.getResultList();
 //		} catch (Exception e) {
 //			return null;
 //		}
 //	}
         public List<Exemplar> findByIdbook(Integer id) {
 		em.flush();
 		
 		try {
 			Query query = em.createNamedQuery("Exemplar.findByIdbook");
 			query.setParameter("idbook", id);
 			return (List<Exemplar>) query.getResultList();
 		} catch (Exception e) {
 			return null;
 		}
 	}
         
 	
     public List<Exemplar> find(Integer idbook) {
 		// genre SQL
 		String genreSQL = "";
 		/*if (genre != null) {
 			genreSQL = " AND b.genre.idgenre = :idgenreFilter ";
 		}
 		*/
 		Query query = em.createQuery(
 				"SELECT DISTINCT e FROM Exemplar e "
 //				+ "JOIN b.Author a "
 				+ "WHERE "
 				//+	"e.state LIKE :name AND "
 				+	"e.aquired BETWEEN -1000 AND 4000 "
 //				+	"AND a.name LIKE :author "
 				+ genreSQL 
 				+ "ORDER BY e.idbook ASC"
 				);
 		//Query query = em.createQuery("SELECT b FROM Book b WHERE b.name LIKE :name AND b.authors.name LIKE :authorname" + genreSQL);
 		//query.setParameter("name", "%" + name + "%");
 //		query.setParameter("author", "%" + author + "%");
 	
 		//query.setParameter("isbn_issn", "%" + isbn_issn + "%");
 		// genre
 
 		//query.setParameter("yearFrom", yearFrom);
 		//query.setParameter("yearTo", yearTo);
 		return (List<Exemplar>) query.getResultList();
 	}    
         
     @SuppressWarnings("unchecked")
     public List<Exemplar> findAll()
     {
 		return em.createNamedQuery("Exemplar.findAll").getResultList();
     }
 }
