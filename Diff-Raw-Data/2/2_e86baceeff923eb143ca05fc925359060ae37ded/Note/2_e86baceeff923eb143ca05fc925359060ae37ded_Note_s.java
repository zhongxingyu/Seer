 package models;
 
 import java.util.*;
 import play.db.ebean.*;
 import play.data.validation.Constraints.*;
 import play.data.format.Formats.*;
 import play.Logger;
 import javax.persistence.*;
 
 @Entity
 public class Note extends Model {
 
 	@Id
 	public Long id;
 
 	@Required
 	@NonEmpty
 	@MinLength(2)
 	@MaxLength(30)
 	public String title;
 	public String text;
 	public String author;
 
 	@ManyToMany
 	public List<Tag> tags = new ArrayList<Tag>();
 
 	@OneToMany(mappedBy="note", cascade=CascadeType.ALL)
 	public List<Comment> comments = new ArrayList<Comment>();
 
 	public Note(String title, User author) {
 		this.title = title;
 		this.author = author.email;
 	}
 
 	public Note(String title, String text, User author) {
 		this.title = title;
 		this.text = text;
 		this.author = author.email;
 	}
 
 	public static Finder<Long, Note> find = new Finder(Long.class, Note.class);
 
     public static List<Note> notesBy(String user) {
         return find.where()
             .eq("author", user)
             .findList();
     }
    
 	public static List<Note> all() {
 		return find.all();
 	}
 
 	public static Note create(Note note) {
 		note.save();
 		return note;
 	}
 
 	public static Note create(Note note, String tags) {
 		note.save();
 		if(tags != null) {
 			note.tags = Tag.createOrFindAllFromString(tags);
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
 		find.ref(id).delete();
 	}
 
 	public Note addComment(String author, String content) {
         Comment comment = new Comment(this, author, content);
         this.comments.add(comment);
         this.save();
         return this;
     }
 }
