 package models;
 
 import org.ocpsoft.prettytime.PrettyTime;
 
 import java.util.*;
 
 import javax.persistence.*;
 
 import com.avaje.ebean.Expr;
 import com.avaje.ebean.*;
 
 import play.db.ebean.*;
 import play.data.validation.Constraints.*;
 import play.data.format.Formats.*;
 import play.Logger;
 
 import securesocial.core.java.SecureSocial;
 
 import helpers.UnauthorizedException;
 
 @Entity
 public class Note extends Model implements Authorizable {
 
 	@Id
 	public Long id;
 
 	@Required
 	@NonEmpty
 	@MinLength(2)
 	@MaxLength(30)
 	public String title;
 
   @Column(columnDefinition = "TEXT")
 	public String content;
 
 	@ManyToOne
   public User author;
 
  @ManyToMany(cascade=CascadeType.ALL)
  @JoinTable(name="note_tags", joinColumns=@JoinColumn(name="note_id"), inverseJoinColumns=@JoinColumn(name="tag_id"))
 	public List<Tag> tags = new ArrayList<Tag>();
 
   @Column(name = "created_at")
   public Date createdAt;
  
   @Column(name = "updated_at")
   public Date updatedAt;
 
 	@OneToMany(mappedBy="note", cascade=CascadeType.ALL)
 	public List<Comment> comments = new ArrayList<Comment>();
 
   @OneToMany(mappedBy="note", cascade=CascadeType.ALL)
   public List<S3File> images = new ArrayList<S3File>();
 
 	@ManyToMany(cascade=CascadeType.REMOVE)
 	@JoinTable(name="up_votes")
 	public List<User> upVotes = new ArrayList<User>();
 
   // Needed for ebean finder to be able to order notes by rating. Should not be needed since upVotes and downVotes exist
   public int rating;
 
 	@ManyToMany(cascade=CascadeType.REMOVE)
 	@JoinTable(name="down_votes")
 	public List<User> downVotes = new ArrayList<User>();
 
   public Note(String title){
     this.title = title;
   }
 	public Note(String title, User author) {
 		this.title = title;
 		this.author = author;
 	}
 
 	public Note(String title, String content, User author) {
 		this.title = title;
 		this.content = content;
 		this.author = author;
 	}
 
 	public static Finder<Long, Note> find = new Finder(Long.class, Note.class);
 
   public static List<Note> notesBy(User author) {
     return find.where()
         .eq("author", author)
         .findList();
   }
     
 	public static PagingList<Note> all(int resultsPerPage) {
     return find.findPagingList(resultsPerPage);
 	}
 
 	public static Note create(Note note, String tagList, User author, S3File image) {
     note.author = author;
     note.save();
     note.updateTags(tagList);
     note.saveManyToManyAssociations("tags");
     note.addFile(image);
 		return note;
 	}
 
   public static Note update(Long id, Note noteUpdates, String tagList) {
     Note note = find.ref(id);
     note.title = noteUpdates.title;
     note.content = noteUpdates.content;
     note.update();
     note.updateTags(tagList);
     note.saveManyToManyAssociations("tags");
     return note;
   }
 
 	public static void delete(Long id) {
 		Note note = find.ref(id);
     note.delete();
	  //Tag.clean();
 	}
 
   public void updateTags(String tagList) {
     if(tagList != null && !tagList.equals("") && !tagList.equals(" ")) {
      this.tags = Tag.createOrFindAllFromString(this, tagList);
     }
   }
 
   public void addFile(S3File image) {
     if(image != null){
       // TODO: Only add image files
       image.note = this;
       this.images.add(image);
       this.save();
     }
   }
 
 	public Note addComment(String content, User author) {
     Comment comment = new Comment(id, content, author);
     this.comments.add(comment);
     this.save();
     return this;
   }
 
   public void toggleUpVote(User user){
   	if(user == null){
   		// Do nothing
   	}
   	else if(upVotes.contains(user)){
       rating--;
   		upVotes.remove(user);
   		this.saveManyToManyAssociations("upVotes");
   	} else {
   		if(downVotes.contains(user)){
         rating++;
   			downVotes.remove(user);
   			this.saveManyToManyAssociations("downVotes");
   		}
       rating++;
   		upVotes.add(user);
   		this.saveManyToManyAssociations("upVotes");
   	}
   	this.save();
   }
 
   public void toggleDownVote(User user){
     rating--;
   	if(user == null){
   		// Do nothing
   	} else if(downVotes.contains(user)){
       rating++;
   		downVotes.remove(user);
   		this.saveManyToManyAssociations("downVotes");
   	} else {
   		if(upVotes.contains(user)){
         rating--;
   			upVotes.remove(user);
   			this.saveManyToManyAssociations("upVotes");
   		}
       rating--;
   		downVotes.add(user);
   		this.saveManyToManyAssociations("downVotes");
   	}
   	this.save();
   }
 
   public int getVoteStatus(User user){
   	if(upVotes.contains(user)){
   		return 1;
   	} else if (downVotes.contains(user)) {
   		return -1;
   	} else {
   		return 0;
   	}
   }
 
   public int getScore(){
   	return (upVotes.size() - downVotes.size());
   }
 
 	public String extract(int length) {
 		if(content != null) {
 		  return content.length() > length ? content.substring(0, length-1) + " ..." : content;
 		}
 		return null;
 	}
 
   public static List<Note> similarNotes(Note note) {
     List<Note> allNotes = find.all();   
 
     // Do not compare with self 
     allNotes.remove(note);
     Collections.sort(allNotes, new NoteComparator(note));
     Collections.reverse(allNotes);
     int numberOfNotes = allNotes.size();
     if (numberOfNotes > 6)
       numberOfNotes = 6;
     return allNotes.subList(0, numberOfNotes);
   }
 
   /**
    * Returns a list of notes related to the search query. 
    * Will look at both content and titles and order them by rating. 
    */ 
   public static List<Note> searchNotes(String query) {
     List<Note> result = new ArrayList<Note>();
     for (String word : query.split("\\s")) {
       List<Note> wordResult = find.where()
         .or(Expr.ilike("title", "%"+word+"%"), Expr.ilike("content", "%"+word+"%")).orderBy("rating")
         .findList();
         for (Note note : wordResult) {
           if (!result.contains(note))
             result.add(note);
         }
     }
     Collections.reverse(result);
     return result;
   }
 
   @Override
   public void save() {
     createdAt();
     super.save();
   }
  
   @Override
   public void update() {
     updatedAt();
     super.update();
   }
  
   @PrePersist
   void createdAt() {
     this.createdAt = new Date();
   }
  
   @PreUpdate
   void updatedAt() {
     this.updatedAt = new Date();
   }
 
   public String getCreatedAt() {
     PrettyTime p = new PrettyTime(new Locale("en"));
     if (this.createdAt != null)
       return p.format(this.createdAt);
     return "yesterday, 4:30pm";
   }
 
   public String getUpdatedAt() {
     PrettyTime p = new PrettyTime(new Locale("en"));
     if (this.updatedAt != null)
       return p.format(this.updatedAt);
     return null;
   }
 
   @Override
   public boolean allows(User user) {
     if(user == null) {
       return false;
     }
     return (this.author.equals(user) || user.privilege.equals(PrivilegeLevel.ADMIN));
   }
 
   public static class NoteComparator implements Comparator<Note> {
 
     Note note;
     public NoteComparator (Note note) {
       super();
       this.note = note;
     }
 
     @Override
     public int compare(Note o1, Note o2) {
       int matchingTagsO1 = 0;
       int matchingTagsO2 = 0;
       for (Tag tag : o1.tags) {
         if (note.tags.contains(tag))
           matchingTagsO1++;
       }
       for (Tag tag : o2.tags) {
         if (note.tags.contains(tag))
           matchingTagsO2++;
       }
       return matchingTagsO1 - matchingTagsO2;
     }
   }
 }
