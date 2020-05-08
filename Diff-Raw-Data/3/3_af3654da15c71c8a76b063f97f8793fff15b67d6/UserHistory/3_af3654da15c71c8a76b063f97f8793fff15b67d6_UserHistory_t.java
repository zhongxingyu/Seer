 package eu.elderspaces.model.profile;
 
 import java.util.List;
 import java.util.Map;
 
 import com.google.common.base.Objects;
 
import com.google.common.collect.Maps;
 import eu.elderspaces.model.Event;
 import eu.elderspaces.model.Person;
 import eu.elderspaces.model.Post;
 
 public class UserHistory {
     
     private Person user;
     private List<Post> posts;
     private final Map<Event, String> eventResponses = Maps.newHashMap();
     
     public UserHistory() {
     
     }
     
     public UserHistory(final Person user, final List<Post> posts) {
     
         this.user = user;
         this.posts = posts;
     }
     
     public Person getUser() {
     
         return user;
     }
     
     public void setUser(final Person user) {
     
         this.user = user;
     }
     
     public List<Post> getPosts() {
     
         return posts;
     }
     
     public void setPosts(final List<Post> posts) {
     
         this.posts = posts;
     }
     
     public Map<Event, String> getEventResponses() {
     
         return eventResponses;
     }
     
     @Override
     public boolean equals(final Object o) {
     
         if (o == null || getClass() != o.getClass()) {
             return false;
         }
         
         final UserHistory that = (UserHistory) o;
         
         return Objects.equal(user, that.user) && Objects.equal(posts, that.posts)
                 && Objects.equal(eventResponses, that.eventResponses);
         
     }
     
     @Override
     public int hashCode() {
     
         return Objects.hashCode(user, posts, eventResponses);
     }
     
     @Override
     public String toString() {
     
         return Objects.toStringHelper(this).addValue(user).addValue(posts).addValue(eventResponses)
                 .toString();
     }
     
 }
