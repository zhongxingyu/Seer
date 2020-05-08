 package com.bookshop.controllers;
 
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.mongodb.core.MongoTemplate;
 import org.springframework.data.mongodb.core.query.Criteria;
 import org.springframework.data.mongodb.core.query.Query;
 import org.springframework.http.MediaType;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import com.bookshop.domain.Book;
 
 @Controller
 @RequestMapping("/books")
 public class BookController {
 
 	@Autowired
 	private MongoTemplate mongoTemplate;
 
 	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
 	public @ResponseBody List<Book> findAllBooks() {
		List<Book> allBooks = mongoTemplate.findAll(Book.class);
		return allBooks;
 	}
 
 	@RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
 	public @ResponseBody Book findBook(@PathVariable("id") String id) {
 		Query query = Query.query(Criteria.where("_id").is(id));
 		return mongoTemplate.findOne(query, Book.class);
 	}
 
 	@RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Book addBook(@RequestBody Book book) {
 		mongoTemplate.save(book);
 		return book;
 	}
 	
 	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
 	public void deleteBook(@PathVariable("id") String id){
 		Query query = Query.query(Criteria.where("_id").is(id));
 		mongoTemplate.remove(query, Book.class);
 	}
 }
