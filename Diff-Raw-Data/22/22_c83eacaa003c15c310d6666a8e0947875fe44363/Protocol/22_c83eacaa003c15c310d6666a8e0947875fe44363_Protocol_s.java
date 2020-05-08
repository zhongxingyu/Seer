 package org.fourdnest.androidclient.comm;
 
 import java.util.List;
 
 import org.fourdnest.androidclient.Egg;
 import org.fourdnest.androidclient.Nest;
 import org.fourdnest.androidclient.Tag;
 
 
 public interface Protocol {
     
    public String sendEgg(Egg egg);
    public List<Tag> topTags(int count);
    public void setNest(Nest nest);
 
 }
