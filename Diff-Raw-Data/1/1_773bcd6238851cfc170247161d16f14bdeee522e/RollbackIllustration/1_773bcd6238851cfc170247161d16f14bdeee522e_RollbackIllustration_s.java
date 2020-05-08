 package BookCatalog.Client;
 
 import java.util.Date;
 
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.Transaction;
 import org.hibernate.cfg.AnnotationConfiguration;
 import org.hibernate.cfg.Configuration;
 import org.hibernate.tool.hbm2ddl.SchemaExport;
 
 import BookCatalog.POJOs.Author;
 import BookCatalog.POJOs.Book;
 import BookCatalog.POJOs.ComputerBook;
 import BookCatalog.POJOs.Publisher;
import Chapter.Nine.pojo.Subscriber;
 
 // This sample code was taken from Chapter 9.
 // It was moved here because Chapter 9 was missing code to populate the database.
 // So I modified it to work with Chapter 4's database.
 public class RollbackIllustration {
   public static void main(String[] args) {
     Session session = getSession();
     Transaction transaction = session.beginTransaction();
 
     Author author = (Author) session.createQuery("from Author where name='Dave'").uniqueResult();
     System.out.println("Name of author acquired from DB... " + author.getName());
     transaction.commit();
 
     // System.out.println("Name of author acquired from DB... " + author.getName());
 
     System.out.println("Begin transaction:");
     session = getSession();
     transaction = session.beginTransaction();
 
     System.out.println("Change name of author");
     author.setName("quentin");
 
     System.out.println("Name of author before rollback... " + author.getName());
     transaction.rollback();
     System.out.println("Name of author after rollback... " + author.getName());
 
     session = getSession();
     transaction = session.beginTransaction();
 
     System.out.println("About to refresh author!");
     session.refresh(author);
     System.out.println("Name of author after refresh... " + author.getName());
 
     transaction.commit();
     System.out.println("Name of author after commit... " + author.getName());
   }
 
    private static void populate() {
      Session session = getSession();
      Transaction transaction = session.beginTransaction();
 
      Author jeff = new Author("Jeff");
      session.save(jeff);
 
      Author dave = new Author("Dave");
      session.save(dave);
 
      Book book = new Book();
      session.save(book);
 
      book.setTitle("Pro Hibernate 3 ");
      book.setPages(200);
      book.setPublicationDate(new Date());
 
      book.addAuthor(jeff);
      book.addAuthor(dave);
 
      ComputerBook computerBook = new ComputerBook();
      session.save(computerBook);
 
      computerBook.setTitle("Building Portals with the Java Portlet API");
      computerBook.setPages(350);
      computerBook.setSoftwareName("Apache Pluto");
 
      computerBook.addAuthor(jeff);
      computerBook.addAuthor(dave);
 
      Publisher publisher = new Publisher();
      session.save(publisher);
 
      publisher.setName("Apress");
      publisher.addBook(book);
      publisher.addBook(computerBook);
 
      transaction.commit();
    }
 
    // The following was derived from the book "Hibernate Made Easy" by Cameron McKenzie.
    // Specifically, HibernateUtil's getSession and getInitializedConfiguration methods.
    private static SessionFactory factory;
 
    private static Session getSession() {
      if (factory == null) {
        Configuration config = new AnnotationConfiguration();
 
        // add all of your JPA annotated classes here!!!
        config.addAnnotatedClass(Author.class);
        config.addAnnotatedClass(Book.class);
        config.addAnnotatedClass(ComputerBook.class);
        config.addAnnotatedClass(Publisher.class);
 
        config.configure();
 
        // generate the tables
        SchemaExport schemaExport = new SchemaExport(config);
 
        boolean script = true; // print the DDL to the console
        boolean export = true; // export the script to the database
 
        schemaExport.create(script, export);
        factory = config.buildSessionFactory();
 
        populate();
      }
 
      return factory.getCurrentSession();
    }
 }
