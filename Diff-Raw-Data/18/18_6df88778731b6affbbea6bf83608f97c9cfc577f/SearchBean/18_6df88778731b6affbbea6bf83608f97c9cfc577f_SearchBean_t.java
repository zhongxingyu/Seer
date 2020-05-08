 package de.chkal.pf4demo.web.search;
 
 import java.util.List;
 
 import javax.enterprise.context.RequestScoped;
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import org.ocpsoft.rewrite.annotation.Join;
 import org.ocpsoft.rewrite.annotation.Parameter;
 import org.ocpsoft.rewrite.annotation.RequestAction;
import org.ocpsoft.urlbuilder.Address;
 
 import de.chkal.pf4demo.dao.BookDao;
 import de.chkal.pf4demo.model.Book;
 
 @Named
 @RequestScoped
 @Join(path = "/search", to = "/faces/search.xhtml")
 public class SearchBean
 {
 
   @Parameter("q")
    private String query;
 
    private List<Book> books;
 
    @Inject
    private BookDao bookDao;
 
   public String search() {
      return Address.begin()
               .path("/faces/search.xhtml")
               .query("faces-redirect", true)
               .query("q", query)
               .toString();
   }
   
    @RequestAction
   public void init()
    {
       if (query != null && query.trim().length() > 0) {
          books = bookDao.findByQuery(query);
       }
    }
 
    public String getQuery()
    {
       return query;
    }
 
    public void setQuery(String query)
    {
       this.query = query;
    }
 
    public List<Book> getBooks()
    {
       return books;
    }
 
 }
