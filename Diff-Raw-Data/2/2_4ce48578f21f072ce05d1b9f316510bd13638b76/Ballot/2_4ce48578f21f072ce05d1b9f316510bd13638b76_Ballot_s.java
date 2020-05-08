 package com.exceedvote.entity;
 
 import java.io.Serializable;
 import javax.persistence.*;
 
 
 /**
  * The persistent class for the ballot database table.
  * @author Kunat Pipatanakul
  * @version 2012.11.11
  */
 @Entity
 public class Ballot implements Serializable {
 	private static final long serialVersionUID = 1L;
 	@Id
 	@GeneratedValue(strategy=GenerationType.IDENTITY)
 	private int id;
 	@ManyToOne
 	@JoinColumn(name="choice")
 	private Choice choice;
 	@ManyToOne
 	@JoinColumn(name="questionid")
 	private Statement questionid;
 	@ManyToOne
 	@JoinColumn(name="user")
 	private User user;
 
     /**
      * Constructor
      */
     public Ballot() {
     }
     /**
      * Constructor with user question choice object.
      * @param user User that vote this.
      * @param question Statement this vote are on.
      * @param choice Choice that voter choose.
      */
     public Ballot(User user,Statement question,Choice choice){
     	this.choice= choice;
     	this.questionid = question;
     	this.user = user;
     }
 
 	/**
 	 * getId
 	 * @return id of the ballot
 	 */
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
 	public int getId() {
 		return this.id;
 	}
 
 	/**
 	 * setId 
 	 * @param id of the ballot
 	 */
 	public void setId(int id) {
 		this.id = id;
 	}
 
 
 	/**
 	 * getChoice 
 	 * @return choice of this ballot.
 	 */
 	public Choice getChoice() {
 		return this.choice;
 	}
 
 	/**
 	 * setChoice
 	 * @param choice Choice of this ballot
 	 */
 	public void setChoice(Choice choice) {
 		this.choice = choice;
 	}
 
 
 	/**
 	 * getQuestionid
 	 * @return Statement this ballot are in.
 	 */
 	public Statement getQuestionid() {
 		return this.questionid;
 	}
 
 	/**
 	 * setQuestionid
 	 * @param questionid Statement of this ballot.
 	 */
 	public void setQuestionid(Statement questionid) {
 		this.questionid = questionid;
 	}
 
 
 	/**
 	 * getUser
 	 * @return user that vote this ballot
 	 */
 	public User getUser() {
 		return this.user;
 	}
 
 	/**
 	 * setUser
 	 * @param user User who vote this ballot.
 	 */
 	public void setUser(User user) {
 		this.user = user;
 	}
 
 }
