 package com.freeroom.web.example.beans;
 
 import com.freeroom.di.annotations.Bean;
 import com.freeroom.di.annotations.Inject;
 import com.freeroom.web.Model;
 import com.google.common.base.Optional;
 
 import static com.freeroom.web.Model.render;
 
 @Bean
 public class BooksController
 {
     @Inject
     private BookService bookService;
 
     public Model create(final Book book)
     {
         bookService.addBook(book);
        return render("html:example/index.html").
                put("books", bookService.getBooks()).
                put("Tags", bookService.getTags()).
                put("Publishers", bookService.getPublishers());
     }
 
     public Model delete(final String isbn)
     {
         bookService.delete(isbn);
        return render("html:example/index.html").
                put("books", bookService.getBooks()).
                put("Tags", bookService.getTags()).
                put("Publishers", bookService.getPublishers());
     }
 
     public Model search(final String bookName)
     {
         return render("html:example/index.html").
                 put("books", bookService.findByName(bookName)).
                 put("Tags", bookService.getTags()).
                 put("Publishers", bookService.getPublishers());
     }
 
     public Model edit(final String isbn)
     {
         final Optional<Book> book = bookService.get(isbn);
         final Model model = render("html:example/edit.html");
         if (book.isPresent()) {
            model.put("book", book.get());
         }
        return model.put("Publishers", bookService.getPublishers()).
                     put("Tags", bookService.getTags());
     }
 
     public Model update(final Book book)
     {
         bookService.updateBook(book);
         return render("html:example/index.html").
                 put("books", bookService.getBooks()).
                 put("Tags", bookService.getTags()).
                 put("Publishers", bookService.getPublishers());
     }
 }
