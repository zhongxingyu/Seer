 package cz.muni.fi.pa165.web;
 
 import cz.muni.fi.pa165.library.dtos.BookTO;
 import cz.muni.fi.pa165.library.dtos.ImpressionTO;
 import cz.muni.fi.pa165.library.services.BookService;
 import cz.muni.fi.pa165.library.services.ImpressionService;
 import java.util.List;
 import net.sourceforge.stripes.action.Before;
 import net.sourceforge.stripes.action.DefaultHandler;
 import net.sourceforge.stripes.action.ForwardResolution;
 import net.sourceforge.stripes.action.RedirectResolution;
 import net.sourceforge.stripes.action.Resolution;
 import net.sourceforge.stripes.action.UrlBinding;
 import net.sourceforge.stripes.controller.LifecycleStage;
 import net.sourceforge.stripes.integration.spring.SpringBean;
 import net.sourceforge.stripes.validation.Validate;
 import net.sourceforge.stripes.validation.ValidateNestedProperties;
 
 /**
  *
  * @author Mjartan
  */
 public class BookEditBean extends BaseBean {
 
     @SpringBean
     private BookService bookService;
     @SpringBean
     private ImpressionService impressionService;
     @ValidateNestedProperties(value = {
         @Validate(on = {"create", "save"}, field = "condition", required = true)
     })
     private BookTO book;
     private ImpressionTO impression;
     private List<BookTO> impressionBooks;
 
     @DefaultHandler
     public Resolution listBooks() {
         impressionBooks = bookService.findBooksForImpression(impression);
         return new ForwardResolution("/book/list.jsp");
     }
 
     public Resolution listBooksForImpression() {
         ImpressionTO tempIm = impressionService.findImpressionById(Long.parseLong(getContext().getRequest().getParameter("impression.id")));
         impressionBooks = bookService.findBooksForImpression(tempIm);
         return new ForwardResolution("/book/list.jsp");
     }
 
     public Resolution createNewBook() {
         return new ForwardResolution("/book/insert.jsp");
     }
 
     public Resolution editBook() {
         return new ForwardResolution("/book/edit.jsp");
     }
 
     public Resolution deleteBook() {
         bookService.deleteBook(book);
         return new RedirectResolution(getClass(), "listBooks").addParameter("impression.id", book.getImpression().getId());
     }
 
     @Before(stages = LifecycleStage.BindingAndValidation, on = {"editBook", "deleteBook"})
     public void loadBook() {
         book = bookService.findBookById(Long.parseLong(getContext().getRequest().getParameter("book.id")));
     }
 
     @Before(stages = LifecycleStage.BindingAndValidation, on = {"create", "listBooks"})
     public void Impression() {
         impression = impressionService.findImpressionById(Long.parseLong(getContext().getRequest().getParameter("impression.id")));
     }
 
     public Resolution create() {
         book.setImpression(impression);
         bookService.createBook(book);
         return new RedirectResolution(getClass(), "listBooks").addParameter("impression.id", book.getImpression().getId());
     }
 
     public Resolution save() {
         book.setImpression(impressionService.findImpressionById(book.getImpression().getId())); //vo forme je iba impression.id, tak to treba znovu najst
         bookService.updateBook(book);
         return new RedirectResolution(getClass(), "listBooks").addParameter("impression.id", book.getImpression().getId());
     }
 
     public BookTO getBook() {
         return book;
     }
 
     public void setBook(BookTO book) {
         this.book = book;
     }
 
     public ImpressionTO getImpression() {
         return impression;
     }
 
     public void setImpression(ImpressionTO impression) {
         this.impression = impression;
     }
 
     public List<BookTO> getImpressionBooks() {
         return impressionBooks;
     }
 
     public void setImpressionBooks(List<BookTO> impressionBooks) {
         this.impressionBooks = impressionBooks;
     }
 }
