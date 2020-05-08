 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package cz.muni.fi.pa165.web;
 
 import cz.muni.fi.pa165.library.services.LoanService;
 import cz.muni.fi.pa165.library.dtos.BookTO;
 import cz.muni.fi.pa165.library.dtos.CustomerTO;
 import cz.muni.fi.pa165.library.dtos.LoanTO;
 import cz.muni.fi.pa165.library.services.BookService;
 import cz.muni.fi.pa165.library.services.CustomerService;
 import java.util.List;
 import net.sourceforge.stripes.action.DefaultHandler;
 import net.sourceforge.stripes.action.ForwardResolution;
 import net.sourceforge.stripes.action.RedirectResolution;
 import net.sourceforge.stripes.action.Resolution;
 import net.sourceforge.stripes.action.UrlBinding;
 import net.sourceforge.stripes.integration.spring.SpringBean;
 import net.sourceforge.stripes.validation.Validate;
 import net.sourceforge.stripes.validation.ValidateNestedProperties;
 import net.sourceforge.stripes.validation.ValidationErrorHandler;
 import net.sourceforge.stripes.validation.ValidationErrors;
 import org.joda.time.LocalDate;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.security.core.userdetails.User;
 
 /**
  *
  * @author MiškoHu
  */
 public class LoanEditBean extends BaseBean implements ValidationErrorHandler {
 
     @SpringBean
     private LoanService loanService;
     @SpringBean
     private CustomerService custService;
     @SpringBean
     private BookService bookService;
     @ValidateNestedProperties(value = {
         @Validate(on = {"returnBook"}, field = "conditionReturned", required = true)
     })
     private LoanTO loan;
     private CustomerTO customer;
     private List<BookTO> books;
     private List<LoanTO> loans;
     private BookTO book;
 
     public Resolution displayAvailable() {
         customer = custService.findCustomerById(Long.parseLong(getContext().getRequest().getParameter("customer.id")));
         books = bookService.findNotBorrowedBooks();
         return new ForwardResolution("/loan/available.jsp");
     }
 
     public Resolution create() {
         BookTO temBook = bookService.findBookById(Long.parseLong(getContext().getRequest().getParameter("book.id")));
         customer = custService.findCustomerById(Long.parseLong(getContext().getRequest().getParameter("customer.id")));
         LoanTO tempLoan = new LoanTO();
         tempLoan.setBook(temBook);
         tempLoan.setCustomer(customer);
         tempLoan.setFromDate(new LocalDate());
         loanService.createLoan(tempLoan);
         return new RedirectResolution(getClass()).addParameter("customer.id", customer.getId());
     }
 
     public Resolution findByBook() {
         book = bookService.findBookById(Long.parseLong(getContext().getRequest().getParameter("book.id")));
         loans = loanService.findLoansForBook(book);
         return new ForwardResolution("/loan/loans_for_book.jsp");
     }
 
     public Resolution returnBook() {
         LoanTO loan2 = loanService.findLoanById(Long.parseLong(getContext().getRequest().getParameter("loan.id")));
         loan2.setConditionReturned(loan.getConditionReturned());
         loanService.returnBook(loan2);
         return new RedirectResolution(getClass(), "findByCustomer").addParameter("customer.id", loan2.getCustomer().getId());
     }
 
     public Resolution findByCustomer() {
         customer = custService.findCustomerById(Long.parseLong(getContext().getRequest().getParameter("customer.id")));
         loans = loanService.findLoansForCustomer(customer);
         return new ForwardResolution("/loan/loans_for_customer.jsp");
     }
 
     public Resolution myLoans() {
         Authentication authc = SecurityContextHolder.getContext().getAuthentication();
         User user = (User) authc.getPrincipal();
         System.out.println(user.getUsername());
         customer = custService.findCustomerByUserName(user.getUsername());
         loans = loanService.findLoansForCustomer(customer);
         return new ForwardResolution("/loan/loans_for_customer.jsp");
     }
 
     @Override
     public Resolution handleValidationErrors(ValidationErrors errors) {
         return new RedirectResolution("/index.jsp");
     }
 
     public CustomerTO getCustomer() {
         return customer;
     }
 
     public void setCustomer(CustomerTO customer) {
         this.customer = customer;
     }
 
     public List<BookTO> getBooks() {
         return books;
     }
 
     public void setBooks(List<BookTO> books) {
         this.books = books;
     }
 
     public List<LoanTO> getLoans() {
         return loans;
     }
 
     public void setLoans(List<LoanTO> loans) {
         this.loans = loans;
     }
 
     public BookTO getBook() {
         return book;
     }
 
     public void setBook(BookTO book) {
         this.book = book;
     }
 
     public LoanTO getLoan() {
         return loan;
     }
 
     public void setLoan(LoanTO loan) {
         this.loan = loan;
     }
 }
