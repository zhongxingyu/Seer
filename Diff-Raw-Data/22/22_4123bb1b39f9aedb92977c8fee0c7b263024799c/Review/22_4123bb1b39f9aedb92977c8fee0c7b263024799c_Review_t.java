 package com.bk.model;
 
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.persistence.*;
 
/** @author Andrei Petraru
  * Date: 3/13/13
  */
 
 @Entity
 public class Review extends AbstractEntity {
 
	private Integer likes;
 	private Integer rating;
 
 	@ElementCollection(fetch = FetchType.EAGER)
 	private Set<Long> customerLikes = new HashSet<>();
 
 	@Lob
 	private String comment;
 
 	@Temporal(TemporalType.DATE)
 	@Column(name = "publish_date")
 	private Date publishDate;
 
 	@ManyToOne
 	private Book book;
 
 	@ManyToOne
 	private Customer customer;
 
 	public Integer getLikes() {
		return likes;
 	}
 
 	public void addCustomerLike(Long customerId) {
 		customerLikes.add(customerId);
 		updateLikes();
 	}
 
 	public void removeCustomerLike(Long customerId) {
 		customerLikes.remove(customerId);
 		updateLikes();
 	}
 
 	public boolean isLikedByCustomer(Long customerId) {
 		return customerLikes.contains(customerId);
 	}
 
 	public Integer getRating() {
 		return rating;
 	}
 
 	public void setRating(Integer rating) {
 		this.rating = rating;
 	}
 
 	public String getComment() {
 		return comment;
 	}
 
 	public void setComment(String comment) {
 		this.comment = comment;
 	}
 
 	public Date getPublishDate() {
 		return publishDate;
 	}
 
 	public void setPublishDate(Date publishDate) {
 		this.publishDate = publishDate;
 	}
 
 	public Book getBook() {
 		return book;
 	}
 
 	public void setBook(Book book) {
 		this.book = book;
 	}
 
 	public Customer getCustomer() {
 		return customer;
 	}
 
 	public void setCustomer(Customer customer) {
 		this.customer = customer;
 	}
 
 	private void updateLikes() {
 		likes = customerLikes.size();
 	}
 }
