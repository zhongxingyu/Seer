 package org.rekdev.guava.collections.gtug;
 
 import java.util.*;
 
 import com.google.common.collect.*;
 
 public class Multisets {
     class BlogPost {
         public Collection<String> getTags() {
             return null;
         }
     }
 
     /*
      * distinct tags:  tags.keySet().size();
      * count for "java" tag:  tags.containsKey( "java" ) ? tags.get( "java" ) : 0;
      * total count:  oh crap...gotta iterate or something
      */
     public Map<String,Integer> oldSchool_getTagTallies() {
         Map<String,Integer> tags = new HashMap<String,Integer>();
         for ( BlogPost post : getAllBlogPosts() ) {
             for ( String tag : post.getTags() ) {
                 int value = tags.containsKey( tag ) ? tags.get( tag ) : 0;
                tags.put( tag, ++value );
             }
         }
         return tags;
     }
 
     /*
      * distinct tags:  tags.elementSet().size()
      * count for "java" tag:  tags.count( "java" );  
      * total count:  tags.size();
      */
     public Multiset<String> multiset_getTagTallies() {
         Multiset<String> tags = HashMultiset.create();
         for ( BlogPost post : getAllBlogPosts() ) {
             tags.addAll( post.getTags() );
         }
         return tags;
     }
 
     public Collection<BlogPost> getAllBlogPosts() {
         return null;
     }
 
 }
