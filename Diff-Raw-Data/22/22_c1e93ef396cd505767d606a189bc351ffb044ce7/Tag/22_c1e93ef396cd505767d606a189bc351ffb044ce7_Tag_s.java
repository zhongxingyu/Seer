 /**
  * Tag
  * 13.04.2012
  * @author Philipp Haussleiter
  *
  */
 package models;
 
 import helpers.StopList;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import javax.persistence.Entity;
 import org.apache.commons.lang.StringUtils;
 import play.Logger;
 import play.cache.Cache;
 import play.db.jpa.Model;
 
 @Entity
 public class Tag extends Model {
 
     public String name;
 
     public static List<Tag> findOrCreateTagsForText(String text) {
         List<Tag> tags = new ArrayList<Tag>();
         for (String tag : getTagList(text)) {
             if (tag.trim().length() > 2) {
                 tags.add(Tag.findOrCreateTagByName(tag.trim()));
             }
         }
         return tags;
     }
 
     public static String tagsToIds(Set<Tag> tags) {
         Set<Long> ids = new HashSet<Long>();
         for (Tag tag : tags) {
             ids.add(tag.id);
         }
         return StringUtils.join(ids, ", ");
     }
 
     public static String tagsToNames(Set<Tag> tags) {
         Set<String> names = new HashSet<String>();
         for (Tag tag : tags) {
             names.add(tag.name);
         }
         return StringUtils.join(names, ", ");
     }
 
     public static Tag findOrCreateTagByName(String name) {
         String key = "tag_" + name;
         Tag tag = Cache.get(key, Tag.class);
         if (tag == null) {
             tag = Tag.find("name = ?", name).first();
         }
         if (tag == null) {
             tag = new Tag();
             tag.name = name;
             tag.save();
             Logger.info("creating Tag: " + tag.name);
         }
         Cache.set(key, tag, "1d");
         Logger.info("using Tag: " + tag);
         return tag;
     }
 
     @Override
     public String toString() {
         return this.name;
     }
 
     private static Set<String> getTagList(String text) {
         text = cleanQuestion(text);
         Set<String> tags = new HashSet<String>();
         for (String tag : text.split(" ")) {
             if (!StopList.LIST_DE.contains(tag.trim()) && !StopList.LIST_EN.contains(tag.trim())) {
                 tags.add(tag);
             }
         }
         return tags;
     }
 
     private static String cleanQuestion(String text) {
         text = text.toLowerCase().replace("%", " ");
        String findList[] = {":", "=", "\n", "_", "-", "?", ".", ",", "!", "/", "\\", "\"", "„", "“", "(", ")", "[", "]", "{", "}", "<", ">"};
         for (String find : findList) {
             text = text.replace(find, " ");
         }
         return text;
     }
 }
