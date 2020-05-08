 package models;
 
 import java.util.Date;
 import java.util.List;
 
 import org.bson.types.ObjectId;
 
 import com.google.code.morphia.annotations.Embedded;
 import com.google.code.morphia.annotations.Indexed;
 import com.google.code.morphia.annotations.Property;
 import com.google.common.base.Objects;
 
 
 /**
  * @author Muhammad Fahied
  */
 
 
 @Embedded
 public class  SVideo{
 	@Indexed
 	@Property("id")
 	public String id = new ObjectId().toString();
 
 	@Property("title")
 	public String title;
 	
 	@Property("uri")
 	public String uri;
 	
 	
 	@Property("date")
 	public String postedAt = new Date().toString();
 
 	// Variables to store xy position of  on Web App
 	@Property("wxpos")
 	public int wxpos;
 	
 	@Property("wypos")
 	public int wypos;
 	
 	@Property("taskId")
 	public String taskId;
 	
 	@Embedded()
     public List <SComment> scomments;
 	
 //	@PrePersist
 //	public void prePersist(){
 //		//postedAt = new Date().toString();
 //		wxpos = 0;
 //		wypos = 0;
 //	}
 	
     public SVideo() {
 		// TODO Auto-generated constructor stub
 	}
 
    public SVideo(String title,String uri, String taskId) {
         this.uri = uri;
         this.title = title;
         this.taskId = taskId;
     }
     
     // for flash
     // {"content":"hurray", "xpos":120, "ypos":32}
 	public SVideo(int wxpos, int wypos){
 		this.wxpos = wxpos;
 		this.wypos = wypos;
 	}
 	
 	
 	//delete all comments on this postit
 	public void clearSComments(){
 	    this.scomments = null;
 	}
 	
 	public SComment postComment(String content) {
 		SComment newComment = new SComment(content);
 		this.scomments.add(newComment);
 		return newComment;
 	}
 	
 //	public SComment findCommentByAuthorId(String authorId) {
 //        if (null == authorId) return null;
 //        for (SComment comment: this.scomments) {
 //           if (authorId.equals(comment.authorId) return comment;
 //        }
 //        return null;
 //    }
 	
 	
 
     @Override
     public String toString() {
         return Objects.toStringHelper(this)
         		.add("id", id)
                 .add("title", title)
                 .add("uri", uri)
                 .add("wxpos", wxpos)
                 .add("wypos", wypos)
                 .add("postedAt", postedAt)
                 .toString();
     }
 
 }
