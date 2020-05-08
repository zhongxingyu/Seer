 package models;
 
 import java.util.*;
 import play.Logger;
 import play.db.ebean.*;
 import play.db.ebean.Model.Finder;
 import play.data.validation.Constraints.*;
 import play.data.format.Formats.*;
 import javax.persistence.*;
 import java.util.regex.PatternSyntaxException;
 
 @Table(uniqueConstraints=@UniqueConstraint(columnNames={"title"}))
 @Entity
 public class Tag extends Model {
 
   @Id
   public Long id;
 
   @Required
   @NonEmpty
   @MinLength(3)
   @MaxLength(40)
   public String title;
 
  @ManyToMany(cascade = CascadeType.REMOVE)
   public List<Note> notes = new ArrayList<Note>();
 
   public Tag(String title) {
     this.title = title;
   }
 
   public static Finder<Long, Tag> find = new Finder(Long.class, Tag.class);
 
   public static List<Tag> all() {
     return find.all();
   }
 
   public static Tag create(Tag tag) {
     tag.save();
     return tag;
   }
 
  public static List<Tag> createOrFindAllFromString(String dirtyList) {
     String list = dirtyList.trim().replaceAll(" +", " ");
     List<Tag> tags = new ArrayList<Tag>();
     String[] tagArray = new String[]{list};
 
     try {
       tagArray = list.split("\\s+");
     } catch (PatternSyntaxException ex) {
       Logger.error("Error creating tags");
       return null;
     }
 
     for(String title : tagArray) {
       Tag tag = findByTitle(title) == null ? create(new Tag(title)) : findByTitle(title);
       if(!tags.contains(findByTitle(title))) {
         tags.add(findByTitle(title));
       }
     }
 
     return tags;
   }
 
   public static void clean() {
     for(Tag tag : find.all()) {
       if(tag.notes.isEmpty()) {
         tag.delete();
       }
     }
   }
 
   public static void delete(Long id) {
     find.ref(id).delete();
   }
 
   public static Tag findByTitle(String title) {
     return find.where().eq("title", title).findUnique();
   }
 }
