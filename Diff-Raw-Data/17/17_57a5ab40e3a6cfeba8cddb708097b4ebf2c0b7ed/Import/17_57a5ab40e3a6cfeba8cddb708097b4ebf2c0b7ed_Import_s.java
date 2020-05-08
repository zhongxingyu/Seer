 package net.sf.beezle.jasmin.scss;
 
import net.sf.beezle.sushi.util.Strings;

 public class Import {
     private final String[] mediaList;
 
    public Import(String mediaListHead, String[] mediaListTail) {
        this.mediaList = Strings.cons(mediaListHead, mediaListTail);
     }
 }
