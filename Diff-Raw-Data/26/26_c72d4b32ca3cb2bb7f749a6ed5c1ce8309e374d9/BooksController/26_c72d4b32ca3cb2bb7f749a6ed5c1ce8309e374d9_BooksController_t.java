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
        return homePage();
     }
 
     public Model delete(final String isbn)
     {
         bookService.delete(isbn);
        return homePage();
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
            return model.put("book", book.get()).
                    put("Publishers", bookService.getPublishers()).
                    put("Tags", bookService.getTags());
         }
        return render("html:example/error.html");
     }
 
     public Model update(final Book book)
     {
         bookService.updateBook(book);
        return homePage();
    }

    private Model homePage()
    {
         return render("html:example/index.html").
                 put("books", bookService.getBooks()).
                 put("Tags", bookService.getTags()).
                 put("Publishers", bookService.getPublishers());
     }
 }
