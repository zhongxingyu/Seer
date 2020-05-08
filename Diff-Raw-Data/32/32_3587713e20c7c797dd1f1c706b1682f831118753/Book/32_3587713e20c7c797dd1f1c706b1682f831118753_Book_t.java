 package com.hidden.data.db.model;
 
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 public class Book implements NotNullEntity, PersistentEntity {
 
	public static final String LINE_BREAK = System
 			.getProperty("line.separator");
 
 	private Integer id;
 	private String title;
 	private String content;
 	private Author author;
 	private List<String> bookLines;
 
 	public static Book createEmptyBook() {
 		return new Book();
 	}
 
 	protected Book() {
 		// Used by Hibernate
 	}
 
 	@Override
 	public boolean isEmpty() {
 		return getTitle() == null;
 	}
 
 	@Override
 	public Integer getId() {
 		return id;
 	}
 
 	public String getTitle() {
 		return title;
 	}
 
 	public void setContent(String content) {
 		this.content = content;
 	}
 
 	public void setTitle(String title) {
 		this.title = title;
 	}
 
 	public Author getAuthor() {
 		return author;
 	}
 
 	public void setAuthor(Author author) {
 		this.author = author;
 	}
 
 	public List<String> getBookLines() {
 		if (isEmpty()) {
 			return Collections.<String> emptyList();
 		}
 		if (bookLines == null) {
 			bookLines = Collections.unmodifiableList(Arrays.asList(content
 					.split(LINE_BREAK)));
 		}
 		return bookLines;
 	}
 }
