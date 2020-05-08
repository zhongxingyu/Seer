 package com.hidden.data.db;
 
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.AbstractApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 import org.springframework.stereotype.Component;
 
 import com.hidden.data.db.dao.AuthorDao;
 import com.hidden.data.db.dao.BookDao;
 import com.hidden.data.db.model.Author;
 import com.hidden.data.db.model.Book;
 
 @Component("dataBaseApp")
 public class DataBaseApp {
 
 	@Autowired
 	private BookDao bookDao;
 	@Autowired
 	private AuthorDao authorDao;
 
	@SuppressWarnings("resource")
 	public static void main(String[] args) {
		AbstractApplicationContext ctx = new ClassPathXmlApplicationContext(
 				"applicationContext.xml");
		// context will get closed at JVM runtime
		ctx.registerShutdownHook();
 		DataBaseApp dataBaseApp = (DataBaseApp) ctx.getBean("dataBaseApp");
 		dataBaseApp.start();
 	}
 
 	private void start() {
 		List<Book> allBooks = bookDao.loadAll();
 		for (Book book : allBooks) {
 			System.out.println(book.toString());
 		}
 
 		List<Author> allAuthors = authorDao.loadAll();
 		for (Author author : allAuthors) {
 			System.out.println(author.toString());
 		}
 
 		Author author = Author.createEmptyAuthor();
 		author.setName("New author");
 		authorDao.save(author);
 
 	}
 }
