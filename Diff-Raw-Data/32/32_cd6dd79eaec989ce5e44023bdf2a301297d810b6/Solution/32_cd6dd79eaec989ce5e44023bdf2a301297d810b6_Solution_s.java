 /**
  * 
  * Governs the fields and behavior of a solution in our application
  * to a question. The solution "references" the question that it is
  * answering by storing the id of the question
  * 
  * @author Dan Sanders, 4/29/13
  *
  */
 
 package com.huskysoft.interviewannihilator.model;
 
 import java.util.Date;
 
 public class Solution implements Likeable {
 
 	private int id;
 	private String text;
 	private int questionId;
 	private int authorId;
 	private Date dateCreated;
 	private int likes;
 	private int dislikes;
 	
 	public Solution() {
 		
 	}
 	
 	/** Called when the user of our application wants to add a solution to the
 	 *  database. Our database will populate the rest of the fields of the
 	 *  solution
 	 *  
 	 * @param questionId the id of the question that this solution
 	 * is "answering"
 	 * @param text the solution's text
 	 */
 	public Solution(int questionId, String text) {
 		this.questionId = questionId;
 		this.text = text;
 	}
 	
 	public int getId() {
 		return id;
 	}
 	
 	public String getText() {
 		return text;
 	}
 	
 	public int getQuestionId() {
 		return questionId;
 	}
 	
 	public int getAuthorId() {
 		return authorId;
 	}
 	
 	public Date getDateCreated() {
 		return dateCreated;
 	}
 	
 	public int getLikes() {
 		return likes;
 	}
 
 	public int getDislikes() {
 		return dislikes;
 	}
 	
 	public void setId(int id) {
 		this.id = id;
 	}
 
 	public void setText(String text) {
 		this.text = text;
 	}
 
 	public void setQuestionId(int questionId) {
 		this.questionId = questionId;
 	}
 
 	public void setAuthorId(int authorId) {
 		this.authorId = authorId;
 	}
 
 	public void setDateCreated(Date dateCreated) {
 		this.dateCreated = dateCreated;
 	}
 
 	public void setLikes(int likes) {
 		this.likes = likes;
 	}
 
 	public void setDislikes(int dislikes) {
 		this.dislikes = dislikes;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + authorId;
 		int i;
 		if (dateCreated == null) {
 			i = 0;
 		}
 		else {
 			i = dateCreated.hashCode();
 		}
		result = prime * result
		+ i;
 		result = prime * result + dislikes;
 		result = prime * result + id;
 		result = prime * result + likes;
 		result = prime * result + questionId;
 		int j;
 		if (text == null) {
 			j = 0;
 		}
 		else {
 			j = text.hashCode();
 		}
 		result = prime * result + j;
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj) {
 			return true;
 		}
 		if (obj == null) {
 			return false;
 		}
 		if (!(getClass().equals(obj.getClass()))) {
 			return false;
 		}
 		Solution other = (Solution) obj;
 		if (authorId != other.authorId) {
 			return false;
 		}
 		if (dateCreated == null) {
 			if (other.dateCreated != null) {
 				return false;
 			}
 		} 
 		else if (!dateCreated.equals(other.dateCreated)) {
 			return false;
 		}
 		if (dislikes != other.dislikes) {
 			return false;
 		}
 		if (id != other.id) {
 			return false;
 		}
 		if (likes != other.likes) {
 			return false;
 		}
 		if (questionId != other.questionId) {
 			return false;
 		}
 		if (text == null) {
 			if (other.text != null) {
 				return false;
 			}
 		} 
 		else if (!text.equals(other.text)) {
 			return false;
 		}
 		return true;
 	}
 
 	@Override
 	public String toString() {
 		return "Solution [id=" + id + ", text=" + text + ", questionId="
 		+ questionId + ", authorId=" + authorId + ", dateCreated="
 		+ dateCreated + ", likes=" + likes + ", dislikes=" + dislikes
 		+ "]";
 	}
 }
