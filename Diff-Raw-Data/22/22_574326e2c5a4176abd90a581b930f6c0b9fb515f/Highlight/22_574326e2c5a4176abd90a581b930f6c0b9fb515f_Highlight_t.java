 package sara;
 
 import com.googlecode.objectify.*;
 import com.googlecode.objectify.annotation.*;
 import javax.persistence.Id;
 import com.google.gson.Gson;
 import java.util.Date;
 import java.util.Arrays;
 
 public class Highlight {
 	@Id Long id;
 	@Parent Key<SARADocument> document;
 	String comment;
   String difficulty;
   String usefulness;
 
  String[] nodeIDs;
   int[] startOffsets;
   int[] endOffsets;
   Long timestamp;
 
  public Highlight(Key<SARADocument> document, String comment, String difficulty, String usefulness, int[] startOffsets, int[] endOffsets, String[] nodeIDs) {
     this.document = document;
     this.comment = comment;
     this.difficulty = difficulty;
     this.usefulness = usefulness;
     this.startOffsets = startOffsets;
     this.endOffsets = endOffsets;
    this.nodeIDs = nodeIDs;
     this.timestamp = (Long)(new Date().getTime());
   }
 
   private Highlight() {}
 
   public Long getId() { return id; }
 
   public String toString() {
     return "Highlight #" + this.id.toString();
   }
 
   public String toJson() {
     Gson gson = new Gson();
     String json = gson.toJson(this);
     return json;
   }
 
   public static Highlight constructFromJson(String json) {
     Gson gson = new Gson();
     return gson.fromJson(json, Highlight.class);
   }
 
 }
