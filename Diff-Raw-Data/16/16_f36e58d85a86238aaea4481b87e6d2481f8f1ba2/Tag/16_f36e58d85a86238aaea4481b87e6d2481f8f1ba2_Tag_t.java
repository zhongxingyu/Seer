 package models.domain;
 
 import com.google.common.base.Function;
 
 import javax.annotation.Nullable;
 
 /**
  * User: Knut Haugen <knuthaug@gmail.com>
  * 2011-09-25
  */
 public final class Tag {
     private final String name;
     private final String url;
     private final int id;
 
     public Tag(int id, String name, String url) {
         this.id = id;
         this.name = name;
         this.url = url;
     }
 
 
     public static Function<Tag, String> name() {
         return new Function<Tag, String>() {
             public String apply(Tag tag) {
                return tag.name;
             }
         };
     }
 }
