 package com.hoyoul.wordroid.dto;
 
 import java.util.Date;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
 import javax.persistence.Table;
 
 @Entity
 @Table(name="WordroidWord")
 public class Word {
 	@Id
 	@GeneratedValue
 	@Column(name="ID")
 	private Integer id;
 	@Column(name="WORD")
 	private String word;
 	@Column(name="MEAN")
 	private String mean;
 	@Column(name="BOX")
 	private Integer box;
 	@Column(name="LASTSTUDYTIME")
 	private Date lastStudyTime;
 	@Column(name="DUETIME")
 	private Date dueTime;
 	
 	@ManyToOne(cascade={CascadeType.ALL})
 	@JoinColumn(name="WORDSETID")
 	private Wordset wordset;
 
 	public Word(){}
 	
 	public Word(String word, String mean) {
 		this.word = word;
 		this.mean = mean;
 	}
 	
 	public Word(Wordset wordset, String word, String mean){
 		this.wordset = wordset;
 		this.word = word;
 		this.mean = mean;
 	}
 	
 	public Wordset getWordset() {
 		return wordset;
 	}
 
 	public void setWordset(Wordset wordset) {
 		this.wordset = wordset;
 	}
 
 	public Integer getId() {
 		return id;
 	}
 
 	public void setId(Integer id) {
 		this.id = id;
 	}
 
 	public String getWord() {
 		return word;
 	}
 
 	public void setWord(String word) {
 		this.word = word;
 	}
 
 	public String getMean() {
 		return mean;
 	}
 
 	public void setMean(String mean) {
 		this.mean = mean;
 	}
 
 	public Integer getBox() {
 		return box;
 	}
 
 	public void setBox(Integer box) {
 		this.box = box;
 	}
 
 	public Date getLastStudyTime() {
 		return lastStudyTime;
 	}
 
 	public void setLastStudyTime(Date lastStudyTime) {
 		this.lastStudyTime = lastStudyTime;
 	}
 
 	public Date getDueTime() {
 		return dueTime;
 	}
 
 	public void setDueTime(Date dueTime) {
 		this.dueTime = dueTime;
 	}
 }
