 package org.ivyweb.android.cslearn.models;
 
 import com.j256.ormlite.field.DatabaseField;
 
 public class Choice {
 
 	@DatabaseField(generatedId = true)
 	int id;
 	@DatabaseField(canBeNull = false)
 	String content;
 	
 	@DatabaseField(canBeNull = false)
 	boolean isRight;
 	
	@DatabaseField(canBeNull = false, foreign = true)
	ChoiceQuestion question;
	
 	public Choice() {}
 
	public Choice(String content, boolean isRight, ChoiceQuestion question) {
 		super();
 		this.content = content;
 		this.isRight = isRight;
		this.question = question;
 	}
 
 	public String getContent() {
 		return content;
 	}
 
 	public void setContent(String content) {
 		this.content = content;
 	}
 
 	public boolean isRight() {
 		return isRight;
 	}
 
 	public void setRight(boolean isRight) {
 		this.isRight = isRight;
 	}
 
 	public int getId() {
 		return id;
 	}
 	
	public ChoiceQuestion getQuestion() {
		return this.question;
	}
	
 }
