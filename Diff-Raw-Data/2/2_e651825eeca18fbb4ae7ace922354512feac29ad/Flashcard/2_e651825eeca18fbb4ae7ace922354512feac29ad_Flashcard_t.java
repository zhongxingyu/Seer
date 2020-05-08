 package entity;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 
 @Entity
 @Table(name="FLASHCARD")
 public class Flashcard {
 
 	@Id
 	@GeneratedValue
 	@Column(name="fid")
 	public int id;
 	
 	@Column(name="question")
 	public String question;
 	
 	@Column(name="answer")
 	public String answer;
 	
 	@Column(name="topic")
 	public String topic;
 	
 	@ManyToOne
	@JoinColumn(name = "userid")
 	private User user; 
 
 	public User getUser() {
 		return user;
 	}
 
 	public void setUser(User user) {
 		this.user = user;
 	}
 
 	public int getId() {
 	return id;
 	}
 
 	public void setId(int id) {
 		this.id = id;
 	}
 
 	public String getQuestion() {
 		return question;
 	}
 
 	public void setQuestion(String question) {
 		this.question = question;
 	}
 
 	public String getAnswer() {
 		return answer;
 	}
 
 	public void setAnswer(String answer) {
 		this.answer = answer;
 	}
 
 	public String getTopic() {
 		return topic;
 	}
 
 	public void setTopic(String topic) {
 		this.topic = topic;
 	}
 }
