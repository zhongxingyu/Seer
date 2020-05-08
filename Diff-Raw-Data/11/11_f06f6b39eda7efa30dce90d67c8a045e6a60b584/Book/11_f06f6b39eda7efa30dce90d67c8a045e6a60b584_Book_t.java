 package book;
 
 import java.util.ArrayList;
 import java.util.List;
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 import javax.persistence.ManyToMany;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToOne;
 import org.hibernate.Criteria;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 import org.hibernate.criterion.Example;
 import org.hibernate.criterion.Order;
 import org.hibernate.criterion.Restrictions;
 
 /**
  @author Team Cosmos:
          Erni Ali,
          Phil Vaca,
          Randy Zaatri
 
  Solution for CS157B Project #1
  Book.java is a class that creates the Book entity table.
  It store the title of books and also have relations with tables such as:
  genres, ISBN, publishers, and authors.
  */
 @Entity
 public class Book
 {
    private long id;
    private String title;
    private List<Author> authors = new ArrayList<>();
    private List<Genre> genres = new ArrayList<>();
    private static ISBN isbn;
    private Publisher publisher;
    private String publishedDate;
 
    public Book()
    {
    }
 
    public Book(String title, String publishedDate, ISBN isbn)
    {
       this.title = title;
       this.isbn = isbn;
       this.publishedDate = publishedDate;
    }
 
    @ManyToMany
    @JoinTable(name="Book_Author",
                joinColumns={@JoinColumn(name="book_id")},
                inverseJoinColumns={@JoinColumn(name="author_id")})
    public List<Author> getAuthors()
    {
       return authors;
    }
    public void setAuthors(List<Author> authors)
    {
       this.authors = authors;
    }
 
    @ManyToMany
    @JoinTable(name="Book_Genre",
                joinColumns={@JoinColumn(name="book_id")},
                inverseJoinColumns={@JoinColumn(name="genre_id")})
    public List<Genre> getGenres()
    {
       return genres;
    }
 
    public void setGenres(List<Genre> genres)
    {
       this.genres = genres;
    }
 
    @Id
    @GeneratedValue
    @Column(name="book_id")
    public long getId()
    {
       return id;
    }
 
    public void setId(long id)
    {
       this.id = id;
    }
 
    @OneToOne(cascade=CascadeType.ALL, fetch=FetchType.LAZY)
    @JoinColumn(name="isbn_id")
    public ISBN getIsbn()
    {
       return isbn;
    }
    public void setIsbn(ISBN isbn)
    {
       this.isbn = isbn;
    }
 
    @Column(name="pub_date")
    public String getPublishedDate()
    {
       return publishedDate;
    }
    public void setPublishedDate(String publishedDate)
    {
       this.publishedDate = publishedDate;
    }
 
 
 
    @Column(name="title")
    public String getTitle()
    {
       return title;
    }
    public void setTitle(String title)
    {
       this.title = title;
    }
 
 
    @ManyToOne
    @JoinColumn(name="pub_id")
    public Publisher getPublisher()
    {
       return publisher;
    }
    public void setPublisher(Publisher publisher)
    {
       this.publisher = publisher;
    }
 
     /**
      * Load the Student table.
      */
     public static void load()
     {
         Session session = HibernateContext.getSession();
 
         //Find publishers
         Publisher pb = Publisher.find("Pocket Books");
         Publisher pearson = Publisher.find("Pearson");
         Publisher prentice = Publisher.find("Prentice Hall");
         Publisher gb = Publisher.find("Gallery Books");
         Publisher dales = Publisher.find("Dales Large Print Books");
         Publisher randomHouse = Publisher.find("Random House Digital, Inc");
 
         //set the publishers of a book.
         Book bagOfBones = new Book("Bag of Bones", "2008",
                    new ISBN("9781451678628"));
         bagOfBones.setPublisher(pb);
 
         Book lostHorizon = new Book("Lost Horizon", "1933",
                 new ISBN("9781453239766"));
         lostHorizon.setPublisher(pb);
 
         Book bambi = new Book("Bambi: A Life in the Woods", "1923",
                 new ISBN("9780671666071"));
         bambi.setPublisher(pb);
 
         Book databaseSystems = new Book("Database Systems, the Complete Book",
                 "2008", new ISBN("9780131873254"));
         databaseSystems.setPublisher(pearson);
 
         Book mathBook = new Book("Algebra: Tools for a Changing World", "1998",
                 new ISBN("9780134330693"));
         mathBook.setPublisher(prentice);
 
         Book under = new Book("Under the Dome", "2009",
                 new ISBN("9781476735474"));
         under.setPublisher(gb);
 
         Book murder = new Book("Murder at School", "2002",
                 new ISBN("9781842622001"));
         murder.setPublisher(dales);
 
         Book shining = new Book("The Shining", "1977",
                 new ISBN("9780307743657"));
         shining.setPublisher(gb);
 
         Book salem = new Book("Salem's Lot", "1975",
                 new ISBN("9780307743671"));
         salem.setPublisher(randomHouse);
 
         //Find the authors.
         Author king = Author.find("Stephen", "King");
         Author james = Author.find("James", "Hilton");
         Author felix = Author.find("Felix", "Salten");
         Author hector = Author.find("Hector", "Garcia-Milina");
         Author jeff = Author.find("Jeffry", "Ullman");
         Author prent = Author.find("Prentice", "Hall");
         Author jenn = Author.find("Jennifer", "Widom");
 
         //add authors to book.
         bagOfBones.getAuthors().add(king);
         lostHorizon.getAuthors().add(james);
         bambi.getAuthors().add(felix);
         databaseSystems.getAuthors().add(jenn);
         databaseSystems.getAuthors().add(hector);
         databaseSystems.getAuthors().add(jeff);
         mathBook.getAuthors().add(prent);
         under.getAuthors().add(king);
         murder.getAuthors().add(james);
         shining.getAuthors().add(king);
         salem.getAuthors().add(king);
 
         //Find the genres
         Genre mystery = Genre.find("Mystery");
         Genre horror = Genre.find("Horror");
         Genre education = Genre.find("Education");
         Genre math = Genre.find("Math");
         Genre database = Genre.find("Database");
         Genre thriller = Genre.find("Thriller");
         Genre childBook = Genre.find("Child Book");
         Genre adventure = Genre.find("Adventure");
 
         //add genres to books.
         bagOfBones.getGenres().add(mystery);
         bagOfBones.getGenres().add(horror);
         bagOfBones.getGenres().add(thriller);
         lostHorizon.getGenres().add(adventure);
         bambi.getGenres().add(childBook);
         bambi.getGenres().add(adventure);
         databaseSystems.getGenres().add(database);
         mathBook.getGenres().add(math);
         mathBook.getGenres().add(education);
         under.getGenres().add(horror);
         under.getGenres().add(thriller);
         murder.getGenres().add(horror);
         murder.getGenres().add(mystery);
         shining.getGenres().add(horror);
         shining.getGenres().add(thriller);
         salem.getGenres().add(horror);
 
         // Load the Student table in a transaction.
         Transaction tx = session.beginTransaction();
         {
             session.save(bagOfBones);
             session.save(lostHorizon);
             session.save(bambi);
             session.save(databaseSystems);
             session.save(mathBook);
             session.save(under);
             session.save(murder);
             session.save(shining);
             session.save(salem);
         }
         tx.commit();
         session.close();
 
         System.out.println("Book table loaded.");
     }
 
      /**
      * Fetch the book with a matching title.
      * @param title the title to match.
      * @return the book or null.
      */
     public static Book find(String title)
     {
        // Query by example.
         Book prototype = new Book();
         prototype.setTitle(title);
         Example example = Example.create(prototype);
 
         Session session = HibernateContext.getSession();
         Criteria criteria = session.createCriteria(Book.class);
         criteria.add(example);
 
         Book book = (Book) criteria.uniqueResult();
 
         session.close();
         return book;
     }
 
     public static void list()
     {
         Session session = HibernateContext.getSession();
         Criteria criteria = session.createCriteria(Book.class);
         criteria.addOrder(Order.asc("title"));
 
         List<Book> books = criteria.list();
         System.out.println("All books:");
         System.out.printf("ID %-35s %6s %10s      %s \n", "Book Title", "Year",
               "ISBN13", "Publisher");
 
         // Loop over each student.
         for (Book book : books)
         {
             book.print();
 
             for(Author author: book.getAuthors())
             {
                System.out.printf("    Author: %s %s\n", author.getFirstname(),
                        author.getLastname());
             }
         }
     }
 
    public static String getList()
    {
       String list = "";
       Session session = HibernateContext.getSession();
       Criteria criteria = session.createCriteria(Book.class);
       criteria.addOrder(Order.asc("id"));
 
       List<Book> books = criteria.list();
       list += "All Books:";
 
       for (Book book : books)
       {
          list+= "\n \n" + book.getId() + ". " + book.getTitle() + " | Year: " +
                  book.getPublishedDate() + " | ISBN: " + isbn.getISBNNumber();
          for (Author author : book.getAuthors())
          {
             list+= "\n        " + author.getFirstname() + " "
                     + author.getLastname();
          }
       }
       return list;
    }
 
    public static String getList(boolean typeToggle, String attribute,
                                         String findAttribute, String findValue)
    {
       String list = "";
       Session session = HibernateContext.getSession();
       Criteria criteria = session.createCriteria(Book.class);
 
       //Do only if ordering was specified
       if (attribute != null)
       {
         if (typeToggle)
         {
           criteria.addOrder(Order.asc(attribute));
         }
         else
         {
           criteria.addOrder(Order.desc(attribute));
         }
       }
 
       //Do only selection was specified
       if (findAttribute != null && findValue != null)
       {
           criteria.add(Restrictions.like(findAttribute,"%"+findValue+"%"));
       }
 
       List<Book> books = criteria.list();
       list += "All Books:";
 
       for (Book book : books)
       {
          list+= "\n \n" + book.getId() + ". " + book.getTitle() + " | Year: " +
                  book.getPublishedDate() + " | ISBN: " + isbn.getISBNNumber();
          for (Author author : book.getAuthors())
          {
            list+= "\n        " + author.getFirstname() + " "
                    + author.getLastname();
          }
       }
       return list;
    }
 
     /**
      Prints the id, title, year, isbn number, and name of publishers for that
      book.
     */
    private void print()
    {
       System.out.printf("%d: %-35s %6s %14s %s \n", id, title, publishedDate,
               isbn.getISBNNumber(), publisher.getName());
    }
 
 }
