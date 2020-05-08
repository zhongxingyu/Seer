 package org.fourdnest.androidclient.comm;
 
 import java.util.List;
 
 import org.fourdnest.androidclient.Egg;
 import org.fourdnest.androidclient.Nest;
 import org.fourdnest.androidclient.Tag;
 
 
 public interface Protocol {
     
    String sendEgg(Egg egg);
    List<Tag> topTags(int count);
    void setNest(Nest nest);
 
 }
