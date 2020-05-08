 package model;
 
 import java.util.Date;
 
 public class Post implements Comparable<Post>{
 	
 	long postId;
 	String content;
 	String topic;
 	Date date;
 	String title;
 	String postKey;
 	String email;
	byte[] image;
 	
 	public Post(){
 		
 	}
 	
 	public long getPostId(){
 		return postId;
 	}
 	public void setPostId(long postId){
 		this.postId = postId;
 	}
 	public String getContent(){
 		return content;
 	}
 	public void setContent(String content){
 		this.content = content;
 	}
 	public String getTopic(){
 		return topic;
 	}
 	public void setTopic(String topic){
 		this.topic = topic;
 	}
 	public Date getDate(){
 		return date;
 	}
 	public void setDate(Date date){
 		this.date = date;
 	}
 	public String getTitle(){
 		return title;
 	}
 	public void setTitle(String title){
 		this.title = title;
 	}
 	public String getPostKey(){
 		return postKey;
 	}
 	public void setPostKey(String postKey){
 		this.postKey = postKey;
 	}
 	public String getEmail(){
 		return email;
 	}
 	public void setEmail(String email){
 		this.email = email;
 	}
	public byte[] getImage() {
		return this.image;
	}
 
	public void setImage(byte[] image) {
		this.image = image;
	}
	
 	@Override
 	public int compareTo(Post arg0) {
 		if(arg0.date.after(date)){
 			return 1;
 		}
 		else if(arg0.date.before(date)){
 			return -1;
 		}
 		return 0;
 	}
 }
