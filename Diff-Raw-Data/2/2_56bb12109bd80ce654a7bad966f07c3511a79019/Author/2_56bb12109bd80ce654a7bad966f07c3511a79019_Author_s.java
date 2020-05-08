 package book;
 
 import java.util.ArrayList;
 import java.util.List;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 import javax.persistence.ManyToMany;
 import org.hibernate.Criteria;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 import org.hibernate.criterion.Example;
 import org.hibernate.criterion.Order;
 
 /**
  @author Team Cosmos:
          Erni Ali,
          Phil Vaca,
          Randy Zaatri
 
  Solution for CS157B Project #1
  Author.java is a class that creates the Author entity table.
  It stores the firstname and last name of authors and authors can write
  multiple books.
  */
 @Entity
 public class Author
 {
    private String firstname;
    private String lastname;
    private long id;
    private List<Book> books = new ArrayList<>();
 
    public Author()
    {
    }
 
    public Author(String firstname, String lastname)
    {
       this.firstname = firstname;
       this.lastname = lastname;
    }
 
    @ManyToMany
    @JoinTable(name="Book_Author",
                joinColumns={@JoinColumn(name="author_id")},
                inverseJoinColumns={@JoinColumn(name="book_id")})
    public List<Book> getBooks()
    {
       return books;
    }
 
    public void setBooks(List<Book> books)
    {
       this.books = books;
    }
 
    @Id
    @GeneratedValue
    @Column(name = "author_id")
    public long getId()
    {
       return id;
    }
 
    public void setId(long id)
    {
       this.id = id;
    }
 
    @Column(name = "first_name")
    public String getFirstname()
    {
       return firstname;
    }
 
    public void setFirstname(String firstname)
    {
       this.firstname = firstname;
    }
 
    @Column(name = "last_name")
    public String getLastname()
    {
       return lastname;
    }
 
    public void setLastname(String lastname)
    {
       this.lastname = lastname;
    }
 
    /**
     Load the author table
    */
    public static void load()
    {
       Session session = HibernateContext.getSession();
       Transaction tx = session.beginTransaction();
       {
          session.save(new Author("Stephen", "King"));
          session.save(new Author("James", "Hilton"));
          session.save(new Author("Felix", "Salten"));
          session.save(new Author("Hector", "Garcia-Milina"));
          session.save(new Author("Jeffry", "Ullman"));
          session.save(new Author("Jennifer", "Widom"));
          session.save(new Author("Prentice", "Hall"));
       }
       tx.commit();
       session.close();
 
       System.out.println("Author table loaded.");
    }
 
    /**
     Fetch the author with a matching name.
 
     @param firstname the first name of author to match.
     @param lastname the last name of author to match.
     @return the author or null.
     */
    public static Author find(String firstname, String lastname)
    {
       // Query by example.
       Author prototype = new Author();
       prototype.setFirstname(firstname);
       prototype.setLastname(lastname);
       Example example = Example.create(prototype);
 
       Session session = HibernateContext.getSession();
       Criteria criteria = session.createCriteria(Author.class);
       criteria.add(example);
 
       Author author = (Author) criteria.uniqueResult();
 
       session.close();
       return author;
    }
 
    /**
     List the titles by author sorted by first name.
    */
    public static void list()
    {
       Session session = HibernateContext.getSession();
       Criteria criteria = session.createCriteria(Author.class);
      criteria.addOrder(Order.asc("firstName"));
 
       List<Author> authors = criteria.list();
       System.out.println("Titles that are written by Author.");
 
       // Loop over each student.
       for (Author author : authors)
       {
          author.print();
 
          for (Book books : author.getBooks())
          {
             System.out.printf("    Books: %s (%s)\n", books.getTitle(),
                     books.getPublishedDate());
 
          }
       }
    }
 
    /**
     Print the id, first name, and last name of authors.
    */
    private void print()
    {
       System.out.printf("%d: %s %s\n", id, firstname, lastname);
    }
 }
