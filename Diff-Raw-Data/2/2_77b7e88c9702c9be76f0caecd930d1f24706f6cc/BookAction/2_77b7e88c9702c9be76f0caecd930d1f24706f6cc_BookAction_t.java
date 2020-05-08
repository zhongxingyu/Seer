 package no.conduct.web;
 
 import com.opensymphony.xwork2.ActionContext;
 import com.opensymphony.xwork2.ActionSupport;
 import no.conduct.dao.BookDAO;
 import no.conduct.dao.BookDAOImpl;
 import no.conduct.domain.Book;
 import org.apache.struts2.ServletActionContext;
 import javax.enterprise.context.RequestScoped;
 import javax.inject.Inject;
 import javax.inject.Named;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public class BookAction extends ActionSupport {
 
     @Inject
     private Logger log;
 
     private Long id;
     private static final long serialVersionUID = 9149826260758390091L;
     private Book book;
     private List<Book> bookList = new ArrayList<Book>();
 
     @Inject
     private BookDAO bookDAO;
 
     public String execute() {
 
         return SUCCESS;
     }
 
     public String add() {
 
 
        log.info("BookAction add..");
 
         if (book.getTitle().isEmpty())
             return INPUT;
 
         bookDAO.add(book);
 
         return SUCCESS;
     }
 
     public String list() {
 
         this.bookList = bookDAO.listAll();
 
         return SUCCESS;
     }
 
     public String delete() {
         log.info("delete");
         bookDAO.deleteBookById(getId());
         return SUCCESS;
     }
 
     public Book getBook() {
         return book;
     }
 
 
     public void setBook(Book book) {
         this.book = book;
     }
 
     public List<Book> getBookList() {
         return bookList;
     }
 
 
     public void setBookList(List<Book> booksList) {
         this.bookList = booksList;
     }
 
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
 }
