 package com.thoughtworks.controllers;
 
 import com.thoughtworks.models.Book;
 import com.thoughtworks.models.BookDetail;
 import com.thoughtworks.models.Books;
 import com.thoughtworks.models.IssuedBook;
 import com.thoughtworks.repositories.BookDetailRepository;
 import com.thoughtworks.repositories.BookRepository;
 import com.thoughtworks.repositories.IssuedBookRepository;
 import com.thoughtworks.repositories.UserRepository;
 import lombok.NoArgsConstructor;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.ui.ModelMap;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.WebDataBinder;
 import org.springframework.web.bind.annotation.*;
 import org.springframework.web.bind.support.SessionStatus;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.validation.Valid;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 @NoArgsConstructor
 @Controller
 @SessionAttributes(types = Book.class)
 public class BookController {
   @Autowired
   private BookRepository repository;
 
   @Autowired
   private IssuedBookRepository issuedBookRepository;
 
   @Autowired
   private BookDetailRepository bookDetailRepository;
 
   @Qualifier("userRepository")
   @Autowired
   private UserRepository userRepository;
 
   @InitBinder
   public void setAllowedFields(WebDataBinder dataBinder) {
     dataBinder.setDisallowedFields("id");
   }
 
   @RequestMapping(value = "/book/return", method = RequestMethod.GET)
   public String returnBooks(Map<String, Object> model, HttpServletRequest request) {
     Object attribute = request.getSession().getAttribute("isLogin");
     if (attribute != null) {
       attribute = request.getSession().getAttribute("employeeId");
       List<IssuedBook> issuedBooks = issuedBookRepository.findBooksByEmployeeId((Long) attribute);
       Books books = new Books();
       List<Book> bookList = new ArrayList<Book>();
       for (IssuedBook issuedBook : issuedBooks) {
         Book book = repository.findOne(issuedBook.getBookId());
         bookList.add(book);
       }
       books.setBookList(bookList);
       model.put("books", books);
       return "book/return";
     }
     return "redirect:/";
 
   }
 
   @RequestMapping(value = "/book/index", method = RequestMethod.GET)
   public String index(Map<String, Object> model, HttpServletRequest request) {
     Object attribute = request.getSession().getAttribute("isLogin");
     if (attribute != null) {
       Books books = new Books();
       Iterable<Book> all = repository.findAll();
       for (Book book : all) {
         book.setAvailableCopies(book.getBookDetails().size() - issuedBookRepository.findBooksById(book.getId()).size());
       }
       books.setBookList((List<Book>) all);
       model.put("books", books);
       return "book/index";
     }
     return "redirect:/";
   }
 
   @RequestMapping(value = "/new", method = RequestMethod.GET)
   public String initCreationOfBook(Map<String, Object> model, HttpServletRequest request) {
     Object attribute = request.getSession().getAttribute("isAdmin");
     if (attribute != null) {
       boolean isAdmin = Boolean.parseBoolean(attribute.toString());
       if (isAdmin) {
         Book book = new Book();
         model.put("book", book);
         return "book/add";
       }
     }
     return "book/index";
   }
 
   @RequestMapping(value = "/new", method = RequestMethod.POST)
   public String processCreationOfBook(@ModelAttribute("book") @Valid Book book, BindingResult result, SessionStatus status, HttpServletRequest request) {
     Integer noOfCopies = Integer.valueOf(request.getParameter("noOfCopies"));
     Date dateOfPurchase = new Date((request.getParameter("dateOfPurchase")));
 
     if (result.hasErrors()) {
       return "book/add";
     } else {
       if (noOfCopies > 0) {
         BookDetail bookDetail;
         List<BookDetail> bookDetails = new ArrayList<BookDetail>();
         for (int i = 0; i < noOfCopies; i++) {
           bookDetail = new BookDetail();
 
           bookDetail.setDateOfPurchase(dateOfPurchase);
           bookDetail.setActive(true);
           bookDetail.setBook(book);
           bookDetails.add(bookDetail);
         }
         book.setBookDetails(bookDetails);
       }
       this.repository.save(book);
       status.setComplete();
       return "book/index";
     }
   }
 
   @RequestMapping(value = "/{bookId}/edit", method = RequestMethod.GET)
   public String initUpdateOfBook(@Valid @PathVariable("bookId") Long bookId, Model model) {
     Book book = this.repository.findOne(bookId);
     model.addAttribute(book);
     return "book/add";
   }
 
   @RequestMapping(value = "/{bookId}/edit", method = RequestMethod.PUT)
   public String processUpdateOfBook(@Valid Book book, BindingResult result, SessionStatus status, HttpServletRequest request) {
     Integer noOfCopies = Integer.valueOf(request.getParameter("noOfCopies"));
     Date dateOfPurchase = new Date((request.getParameter("dateOfPurchase")));
 
     if (result.hasErrors()) {
       return "book/add";
     } else {
       if (noOfCopies > 0) {
         if (book.getBookDetails().size() > 0) {
           bookDetailRepository.delete(book.getBookDetails());
         }
         BookDetail bookDetail;
         List<BookDetail> bookDetails = new ArrayList<BookDetail>();
         for (int i = 0; i < noOfCopies; i++) {
           bookDetail = new BookDetail();
 
           bookDetail.setDateOfPurchase(dateOfPurchase);
           bookDetail.setActive(true);
           bookDetail.setBook(book);
           bookDetails.add(bookDetail);
         }
         book.setBookDetails(bookDetails);
       }
       this.repository.save(book);
       status.setComplete();
       return "book/index";
     }
   }
 
   @RequestMapping(value = "/books/{bookId}/deleteBook", method = RequestMethod.GET, headers = "Accept=application/json")
   public String deleteBook(@PathVariable("bookId") Long bookId, SessionStatus status, HttpServletResponse response, HttpServletRequest request, Map<String, Object> model) {
     Book book = repository.findOne(bookId);
     book.setActive(false);
     this.repository.save(book);
     status.setComplete();
     response.setStatus(200);
 //
 //      Iterable<BookDetail> bookDetail = bookDetailRepository.findAll();
 //      List<BookDetail> bookDetails = new ArrayList<BookDetail>();
 //      bookDetails.addAll((Collection<? extends BookDetail>)bookDetail);
 //      model.put("bookDetails", bookDetails);
 //      return "book/index";
     return "redirect:/";
   }
 
   @RequestMapping(value = "/{bookId}/issue", method = RequestMethod.GET)
   public String issueBook(@Valid @PathVariable("bookId") Long bookId, HttpServletRequest request, ModelMap model) {
     Object attribute = request.getSession().getAttribute("employeeId");
     if (attribute != null) {
       IssuedBook book = new IssuedBook();
       book.setBookId(bookId);
       book.setEmployeeId((Long) attribute);
       book.setIssueDate(new Date());
       book.setActive(true);
       issuedBookRepository.save(book);
 //      model.addAttribute("message", "yahoo");
       return "redirect:/book/return";
     }
     return "redirect:/book/index";
   }
 
   @RequestMapping(value = "/book/borrower", method = RequestMethod.GET)
   public
   @ResponseBody
   String borrower(Long bookId) {
     List<IssuedBook> issuedBooks = issuedBookRepository.findBooksById(bookId);
     List<String> userNames = new ArrayList<String>();
     for (IssuedBook book : issuedBooks) {
       userNames.add(userRepository.findOne(book.getEmployeeId()).getUserName());
     }
 
     return userNames.toString();
   }
 }
