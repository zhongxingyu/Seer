 package models;
 
 import java.util.*;
 
 import javax.persistence.*;
 
 import com.avaje.ebean.Expr;
 
 import play.db.ebean.*;
 import play.data.validation.Constraints.*;
 import play.data.format.Formats.*;
 import play.Logger;
 
 import securesocial.core.java.SecureSocial;
 
 import helpers.UnauthorizedException;
 
 @Entity
 public class Note extends Model {
 
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
 
 	@ManyToMany(cascade = CascadeType.REMOVE)
 	public List<Tag> tags = new ArrayList<Tag>();
 
 	@OneToMany(mappedBy="note", cascade=CascadeType.ALL)
 	public List<Comment> comments = new ArrayList<Comment>();
 
 	@ManyToMany(cascade=CascadeType.REMOVE)
 	@JoinTable(name="up_votes")
 	public List<User> upVotes = new ArrayList<User>();
 
   // Needed for ebean finder to be able to order notes by rating. Should not be needed since upVotes and downVotes exist
   public int rating;
 
 	@ManyToMany(cascade=CascadeType.REMOVE)
 	@JoinTable(name="down_votes")
 	public List<User> downVotes = new ArrayList<User>();
 
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
         .eq("author", author.id)
         .findList();
   }
     
 	public static List<Note> all() {
 		return find.all();
 	}
 
 	public static Note create(Note note) {
 		note.save();
 		return note;
 	}
 
 	public static Note create(Note note, String tagsList, User author) {
     note.author = author;
 		note.save();
 		if(tagsList != null && !tagsList.equals("") && !tagsList.equals(" ")) {
       note.tags.clear();
 			note.tags.addAll(Tag.createOrFindAllFromString(tagsList));
 			note.saveManyToManyAssociations("tags");
 		}
 		return note;
 	}
 
 	public static void addTag(Long id, Tag tag) {
 		Note note = find.ref(id);
 		note.tags.add(tag);
 		note.saveManyToManyAssociations("tags");
 	}
 
 	public static void delete(Long id) {
 		Note note = find.ref(id);
     if(note.author.equals(User.currentUser())) {
 		  note.delete();
 	   	Tag.clean();
     } else {
       throw new UnauthorizedException(User.currentUser(), "delete");
     }
 	}
 
 	public Note addComment(String content, User author) {
     Comment comment = new Comment(id, content, author);
     this.comments.add(comment);
     this.save();
     return this;
   }
 
   public static Note update(Note note, String tagsList) {
     Note existingNote = find.ref(note.id);
     existingNote.title = note.title;
     existingNote.content = note.content;
     existingNote.save();
     return note;
   }
 
   public void toggleUpVote(User user){
   	if(user == null){
   		// Do nothing
   		Logger.debug("Vote: User is null!");
   	}
   	else if(upVotes.contains(user)){
   		Logger.debug("Vote: removed upvote!");
       rating--;
   		upVotes.remove(user);
   		this.saveManyToManyAssociations("upVotes");
   	} else {
   		if(downVotes.contains(user)){
   			Logger.debug("Vote: removed old downvote!");
         rating++;
   			downVotes.remove(user);
   			this.saveManyToManyAssociations("downVotes");
   		}
   		Logger.debug("Vote: Added upvote!");
       rating++;
   		upVotes.add(user);
   		this.saveManyToManyAssociations("upVotes");
   	}
   	Logger.debug("Uservotestatus: " + getVoteStatus(user));
   	this.save();
   }
 
   public void toggleDownVote(User user){
     rating--;
   	if(user == null){
   		// Do nothing
   		Logger.debug("Vote: User is null!");
   	} else if(downVotes.contains(user)){
   		Logger.debug("Vote: removed downvote!");
       rating++;
   		downVotes.remove(user);
   		this.saveManyToManyAssociations("downVotes");
   	} else {
   		if(upVotes.contains(user)){
   			Logger.debug("Vote: removed old upvote!");
         rating--;
   			upVotes.remove(user);
   			this.saveManyToManyAssociations("upVotes");
   		}
   		Logger.debug("Vote: Added downvote!");
       rating--;
   		downVotes.add(user);
   		this.saveManyToManyAssociations("downVotes");
   	}
   	Logger.debug("Uservotestatus: " + getVoteStatus(user));
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
   	Logger.debug("upVotes: " + upVotes.size());
   	Logger.debug("downVotes: " + downVotes.size());
   	Logger.debug("upvotes: "+ upVotes + "downvotes:" + downVotes);
   	return (upVotes.size() - downVotes.size());
   }
 
 	public String extract(int length) {
 		if(content != null) {
 		  return content.length() > length ? content.substring(0, length-1) + " ..." : content;
 		}
 		return null;
 	}
 
   public static List<Note> similarNotes(Note note) {
     List<Note> allNotes = Note.all();   
 
     // Do not compare with self 
     allNotes.remove(note);
     Collections.sort(allNotes, new NoteComparator(note));
     Collections.reverse(allNotes);
     return allNotes;
   }
 
   // Returns a list of notes related to the search query. 
   // Will look at both content and titles and order them by rating. 
   public static List<Note> searchNotes(String query) {
     List<Note> result = new ArrayList<Note>();
     for (String word : query.split("\\s")) {
      result.addAll(find.where()
         .or(Expr.ilike("title", "%"+word+"%"), Expr.ilike("content", "%"+word+"%")).orderBy("rating")
        .findList());
     }

     return result;
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
