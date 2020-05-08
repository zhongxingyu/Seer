 /*
 Copyright [2012] [Carlo P. Micieli]
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
 	http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
 package com.github.carlomicieli.services;
 
 import java.util.List;
 
 import org.bson.types.ObjectId;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.mongodb.core.MongoTemplate;
 import org.springframework.data.mongodb.core.query.Query;
 import org.springframework.data.mongodb.core.query.Update;
 
 import static org.springframework.data.mongodb.core.query.Criteria.where;
 import static org.springframework.data.mongodb.core.query.Query.query;
 
 import com.github.carlomicieli.domain.Book;
 
 public class BookService {
 	private MongoTemplate mongoTemplate;
 	
 	@Autowired
 	public BookService(MongoTemplate mongoTemplate) {
 		this.mongoTemplate = mongoTemplate;
 	}
 	
 	public void insert(Book book) {
 		mongoTemplate.insert(book);
 	}
 	
 	public void saveOrUpdate(Book book) {
 		mongoTemplate.save(book);
 	}
 	
 	public List<Book> getAllBooks() {
 		return mongoTemplate.findAll(Book.class);
 	}
 	
 	public List<Book> getAllBooks(int offset, int max) {
 		Query q = new Query().skip(offset).limit(max);
 		return mongoTemplate.find(q, Book.class);
 	}
 	
 	public Book findById(ObjectId id) {
 		return mongoTemplate.findById(id, Book.class); 
 	}
 
 	public Book findByTitle(String title) {
 		return mongoTemplate.findOne(new Query(where("title").is(title)), Book.class);
 	}
 	
 	public List<Book> findByTag(String tag) {
 		return mongoTemplate.find(new Query(where("tags").is(tag)), Book.class);
 	}
 
 	public List<Book> findByCommentAuthor(String commentAuthor) {
 		return mongoTemplate.find(new Query(where("comments.author").is(commentAuthor)), Book.class);
 	}
 
 	public void incLikeIt(Book b) {
		mongoTemplate.updateFirst(query(where("_id").is(b.getId())), new Update().inc("likeIt", 1), Book.class);
 	}
 }
