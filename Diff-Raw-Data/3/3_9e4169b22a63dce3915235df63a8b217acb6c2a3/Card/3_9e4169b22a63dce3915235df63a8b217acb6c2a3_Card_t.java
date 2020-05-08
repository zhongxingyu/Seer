 package models;
 
 import java.util.Date;
 import java.util.List;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
 
 import play.db.ebean.Model;
 
 /**
  * This is the Card model, featuring persistence using Ebean
  * 
  * @author Tiago Garcia
  * @see http://github.com/tiagorg
  */
 @SuppressWarnings("serial")
 @Entity
 public class Card extends Model {
 
 	public enum Type {
 		PROJECT, STORY, TASK, BUG, ENHANCEMENT
 	}
 
 	public enum Status {
 		BACKLOG, IN_PROGRESS, VERIFY, SIGNED_OFF
 	}
 
 	@Id
 	private Long id;
 
 	@Column(nullable = false)
 	private Type type;
 
 	@Column(nullable = false)
 	private Status status;
 
 	@Column(nullable = false)
 	private String title;
 
 	@Column(nullable = false)
 	private String description;
 
 	@Column(nullable = false)
 	private String assignee;
 
 	@Column(nullable = false)
 	private Date createdDate;
 
 	@Column(nullable = false)
 	private Date modifiedDate;
 
 	@ManyToOne(cascade = { CascadeType.REMOVE })
 	@JoinColumn(name="parent_id")
 	private Card parent;
 
 	@OneToMany(mappedBy = "parent", cascade = { CascadeType.REMOVE })
	@OrderBy("modifiedDate")
 	private List<Card> children;
 
 	/*
 	 * Getters and setters
 	 */
 
 	public Long getId() {
 		return id;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	public Type getType() {
 		return type;
 	}
 
 	public void setType(Type type) {
 		this.type = type;
 	}
 
 	public Status getStatus() {
 		return status;
 	}
 
 	public void setStatus(Status status) {
 		this.status = status;
 	}
 
 	public String getTitle() {
 		return title;
 	}
 
 	public void setTitle(String title) {
 		this.title = title;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	public String getAssignee() {
 		return assignee;
 	}
 
 	public void setAssignee(String assignee) {
 		this.assignee = assignee;
 	}
 
 	public Date getCreatedDate() {
 		return createdDate;
 	}
 
 	public void setCreatedDate(Date createdDate) {
 		this.createdDate = createdDate;
 	}
 
 	public Date getModifiedDate() {
 		return modifiedDate;
 	}
 
 	public void setModifiedDate(Date modifiedDate) {
 		this.modifiedDate = modifiedDate;
 	}
 
 	public Card getParent() {
 		return parent;
 	}
 
 	public void setParent(Card parent) {
 		this.parent = parent;
 	}
 
 	public List<Card> getChildren() {
 		return children;
 	}
 
 	public void setChildren(List<Card> children) {
 		this.children = children;
 	}
 
 	/*
 	 * Persistence
 	 */
 	private static Finder<Long, Card> find = new Finder<Long, Card>(Long.class,
 			Card.class);
 
 	/**
 	 * Retrieves all the cards.
 	 * 
 	 * @return a list of cards
 	 */
 	public static List<Card> allProjects() {
 		return find.where().eq("type", Card.Type.PROJECT).findList();
 	}
 
 	/**
 	 * Retrieves a particular card.
 	 * 
 	 * @param id
 	 *           the card id
 	 * @return the card
 	 */
 	public static Card byId(Long id) {
 		return find.byId(id);
 	}
 
 	/**
 	 * Retrieves the reference for a particular card.
 	 * 
 	 * @param id
 	 *           the card id
 	 * @return the card
 	 */
 	public static Card ref(Long id) {
 		return find.ref(id);
 	}
 
 	/**
 	 * Persists a brand new card.
 	 * 
 	 * @param card
 	 *           the card
 	 */
 	public static void create(Card card) {
 		card.status = Status.BACKLOG;
 		card.createdDate = card.modifiedDate = new Date();
 		card.save();
 	}
 
 	/**
 	 * Updates an existing card.
 	 * 
 	 * @param card
 	 *           the card
 	 */
 	public static void update(Card card) {
 		card.setModifiedDate(new Date());
 		card.save();
 	}
 
 	/**
 	 * Deletes a card.
 	 * 
 	 * @param id
 	 *           the card id
 	 */
 	public static void delete(Long id) {
 		find.ref(id).delete();
 	}
 
 }
