 /**
  * Stateless bean pro práci s daty knihy
  */
 package org.fit.pis.library.data;
 
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.ResultSet;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 
 import javax.ejb.Stateless;
 import javax.faces.application.FacesMessage;
 import javax.faces.context.FacesContext;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Query;
 
 /**
  *
  * @author Vojtěch Sysel <xsysel03@setud.fit.vutbr.cz>
  */
 @Stateless
 public class BookManager {
 
 	@PersistenceContext
 	private EntityManager em;
 	Statement stmt = null;
 	ResultSet rs = null;
 	private int id;
 
 	public void save(Book b) {
 		em.merge(b);
 
 	}
 
 	public void remove(Book b) {
 		em.remove(em.merge(b));
 	}
 
 	public void flush() {
 		em.flush();
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<Book> findAll() {
 		return em.createQuery("SELECT b FROM Book b ORDER BY b.name ASC, b.year ASC").getResultList();
 	}
 
 	/**
 	 * Find book by id
 	 * @param id
 	 * @return Book or null
 	 */
 	public Book findByIdbook(Integer id) {
 		em.flush();
 
 		try {
 			Query query = em.createNamedQuery("Book.findByIdbook");
 			query.setParameter("idbook", id);
 			return (Book) query.getSingleResult();
 		} catch (Exception e) {
 			return null;
 		}
 	}
 
 	/**
 	 * Filter user list by some parameters
 	 * @param permitNumber
 	 * @param forename
 	 * @param surname
 	 * @param email
 	 * @param level
 	 * @return 
 	 */
 	public List<Book> find(String name, Author author, Date yearFrom, Date yearTo, Genre genre, String isbn_issn) {
 		// genre SQL
 		String authorSQL = "", genreSQL = "";
 		if (genre != null) {
 			genreSQL = " AND b.genre.idgenre = :idgenreFilter ";
 		}
 		// author SQL
 		if (author != null) {
 			authorSQL = " AND :author MEMBER OF b.authorCollection ";
 		}
 
 		Query query = em.createQuery(
 				"SELECT DISTINCT b FROM Book b "
 				+ "WHERE "
 				+ "b.name LIKE :name AND "
 				+ "b.year BETWEEN :yearFrom AND :yearTo AND "
 				+ "b.code LIKE :isbn_issn "
 				+ genreSQL
 				+ authorSQL
 				+ "ORDER BY b.name ASC, b.year ASC");
 		
 		query.setParameter("name", "%" + name + "%");
 		query.setParameter("isbn_issn", "%" + isbn_issn + "%");
 		query.setParameter("yearFrom", yearFrom);
 		query.setParameter("yearTo", yearTo);
 		
 		// genre
 		if (genre != null) {
 			query.setParameter("idgenreFilter", genre.getIdgenre());
 		}
 		// author
 		if (author != null) {
 			query.setParameter("author", author);
 		}
 		return (List<Book>) query.getResultList();
 	}
 
 	public Integer new_id() {
 		Query query = em.createQuery(
 				"SELECT b.idbook FROM Book b ORDER BY b.idbook DESC").setMaxResults(1);
 		return (Integer) query.getSingleResult();
 	}
 
 	public void refresh(Book e) {
 		em.flush();
 		em.refresh(e);
 	}
 
 	public List<Book> find(String name, Author author, Genre genre, Publisher publisher, String code, String city) {
 		em.flush();
 
 		String authorSQL = "", genreSQL = "", publisherSQL = "";
 
 		if (author != null) {
 			authorSQL = " AND :author MEMBER OF b.authorCollection ";
 		}
 
 		if (genre != null) {
 			genreSQL = " AND b.genre.idgenre = :idgenreFilter ";
 		}
 
 		if (publisher != null) {
			genreSQL = " AND b.publisher.idpublisher = :idpublisherFilter ";
 		}
 
 		Query query = em.createQuery(
 				"SELECT b FROM Book b "
 				+ "WHERE "
 				+ "b.name LIKE :name AND "
 				+ "b.code LIKE :isbn_issn AND "
 				+ "b.place LIKE :city "
 				+ genreSQL
 				+ publisherSQL
 				+ authorSQL
 				+ "ORDER BY b.name ASC, b.year ASC");
 
 		query.setParameter("name", "%" + name + "%");
 		query.setParameter("city", "%" + city + "%");
 		query.setParameter("isbn_issn", "%" + code + "%");
 		// genre
 		if (genre != null) {
 			query.setParameter("idgenreFilter", genre.getIdgenre());
 		}
 		// publisher
 		if (publisher != null) {
 			query.setParameter("idpublisherFilter", publisher.getIdpublisher());
 		}
 		// author
 		if (author != null) {
 			query.setParameter("author", author);
 		}
 
 		List<Book> books = query.getResultList();
 
 		for (Book b : books) {
 			refresh(b);
 		}
 
 		return books;
 	}
 }
