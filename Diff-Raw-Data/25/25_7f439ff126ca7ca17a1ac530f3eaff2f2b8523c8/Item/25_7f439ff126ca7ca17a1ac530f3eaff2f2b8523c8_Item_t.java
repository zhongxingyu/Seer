 package edu.unsw.triangle.model;
 
 import java.io.Serializable;
 import java.util.Date;
 
 public class Item implements Serializable
 {
 	public enum ItemStatus
 	{
 		NOT_ACTIVE,
 		ACTIVE,
 		SOLD,
 		UNSOLD,
 		PENDING;
 	}
 	
 	private static final long serialVersionUID = 1L;
 	
 	private final int MILLISECOND = 1000;
 	
 	private int id;
 	private String title;
 	private String category;
 	private String picture;
 	private String description;
 	private String postage;
 	private float reserve;
 	private float start;
 	private float increment;
 	private String owner;
 	private ItemStatus status;
 	private String bidder;
 	private float bid;
 	private Date startTime;
	// Default 10 min aucion period time
	private int period = 10;
 	
 	public int getId() {
 		return id;
 	}
 	public void setId(int id) {
 		this.id = id;
 	}
 	public float getStart() {
 		return start;
 	}
 	public float getIncrement() {
 		return increment;
 	}
 	public String getOwner() {
 		return owner;
 	}
 	public Item setOwner(String username)
 	{
 		this.owner = username;
 		return this;
 	}
 	
 	public ItemStatus getStatus() {
 		return status;
 	}
 	public String getBidder() {
 		return bidder;
 	}
 	public float getBid() {
 		return bid;
 	}
 	public void setStart(float start) {
 		this.start = start;
 	}
 	public void setIncrement(float increment) {
 		this.increment = increment;
 	}
 	public Item setStatus(ItemStatus status) {
 		this.status = status;
 		return this;
 	}
 	public Item setBidder(String bidder) {
 		this.bidder = bidder;
 		return this;
 	}
 	public Item setBid(float bid) {
 		this.bid = bid;
 		return this;
 	}
 
 	public String getTitle() {
 		return title;
 	}
 	public Item setTitle(String title) {
 		this.title = title;
 		return this;
 	}
 	public String getCategory() {
 		return category;
 	}
 	public Item setCategory(String category) {
 		this.category = category;
 		return this;
 	}
 	public String getPicture() {
 		return picture;
 	}
 	public Item setPicture(String picture) {
 		this.picture = picture;
 		return this;
 	}
 	public String getDescription() {
 		return description;
 	}
 	public Item setDescription(String description) {
 		this.description = description;
 		return this;
 	}
 	public String getPostage() {
 		return postage;
 	}
 	public Item setPostage(String postage) {
 		this.postage = postage;
 		return this;
 	}
 	public float getReserve() {
 		return reserve;
 	}
 	public Item setReserve(float reserve) {
 		this.reserve = reserve;
 		return this;
 	}
 	public Item setPeriod(int value) {
 		this.period = value;
 		return this;
 	}
 	public Date getStartTime() {
 		return (Date) startTime.clone();
 	}
 	public int getPeriod() {
 		return period;
 	}
 	public Item setStartTime(Date startTime) {
 		this.startTime = startTime;
 		return this;
 	}
 	
 	public int getTimeLeft()
 	{
 		long now = new Date().getTime();
 		long end = getStartTime().getTime() + (getPeriod() * 60 * MILLISECOND);
 		long difference = end - now;
 		return (int) difference / (60 * MILLISECOND);
 	}
 }
