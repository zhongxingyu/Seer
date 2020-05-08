 package org.googolplex.javalib.controllers;
 
 import org.googolplex.javalib.dao.AuthorDAO;
 import org.googolplex.javalib.dao.BookDAO;
 import org.googolplex.javalib.domain.Author;
 import org.googolplex.javalib.domain.Book;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import java.util.List;
 
 @Controller
 @RequestMapping(method = RequestMethod.GET, value = "/test")
 @Transactional
 public class TestController {
     @Autowired
     private BookDAO bookDAO;
 
     @Autowired
     private AuthorDAO authorDAO;
 
     @RequestMapping(method = RequestMethod.GET, value = "/create_book")
     public String createBook(@RequestParam String name, Model model) {
         Book book = bookDAO.createWithName(name);
 
         model.addAttribute("action", "create");
         model.addAttribute("type", "book");
         model.addAttribute("object", book);
 
        return "display_object_view";
     }
     
     @RequestMapping(method = RequestMethod.GET, value = "/create_author")
     public String createAuthor(@RequestParam String name, Model model) {
         Author author = authorDAO.createWithName(name);
         
         model.addAttribute("action", "create");
         model.addAttribute("type", "author");
         model.addAttribute("object", author);
 
        return "display_object_view";
     }
 
     @RequestMapping(method = RequestMethod.GET, value = "/list_books")
     public String displayBooks(Model model) {
         List<Book> books = bookDAO.list();
 
         model.addAttribute("books", books);
 
         return "books_list_view";
     }
     
 }
