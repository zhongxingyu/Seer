 package models;
 
 import java.util.Date;
 import java.util.List;
 
 import org.bson.types.ObjectId;
 
 import com.google.code.morphia.annotations.Embedded;
 import com.google.code.morphia.annotations.Indexed;
 import com.google.code.morphia.annotations.PrePersist;
 import com.google.code.morphia.annotations.Property;
 import com.google.common.base.Objects;
 
 
 /**
  * @author Muhammad Fahied
  */
 
 
 @Embedded
 public class  SPostit{
 	@Indexed
 	@Property("_id")
 	public String _id = new ObjectId().toString();
 
 	@Property("content")
 	public String content;
 	
 	@Property("date")
	public String postedAt = new Date().toString();;
 	
 	// Variables to store xy position of  on Flash App
 	@Property("xpos")
 	public int xpos;
 	@Property("ypos")
 	public int ypos;
 
 	// Variables to store xy position of  on Web App
 	@Property("wxpos")
 	public int wxpos;
 	@Property("wypos")
 	public int wypos;
 	
 	@Embedded()
     public List <SComment> scomments;
 	
 	@PrePersist
 	public void prePersist(){
		//postedAt = new Date().toString();
 		wxpos = 0;
 		wypos = 0;
 	}
 	
     
    public SPostit() {
 
 	}
 
     public SPostit(String content) {
         this.content = content;
     }
     
     // for flash
     // {"content":"hurray", "xpos":120, "ypos":32}
 	public SPostit(String content, int xpos, int ypos){
 		this.content = content;
 		this.xpos = xpos;
 		this.ypos = ypos;
 	}
 	
 	// for web
 	public SPostit(int wxpos, int wypos){
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
         		.add("_id", _id)
                 .add("content", content)
                 .add("xpos", xpos)
                 .add("ypos", ypos)
                .add("postedAt", postedAt)
                 .toString();
     }
 
 }
