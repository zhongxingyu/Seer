 package org.mule.galaxy.impl.jcr;
 
 import java.io.IOException;
 import java.util.List;
 
 import javax.jcr.RepositoryException;
 import javax.jcr.Session;
 
 import org.mule.galaxy.DuplicateItemException;
 import org.mule.galaxy.Item;
 import org.mule.galaxy.NotFoundException;
 import org.mule.galaxy.collab.Comment;
 import org.mule.galaxy.collab.CommentManager;
 import org.mule.galaxy.event.EntryCommentCreatedEvent;
 import org.mule.galaxy.event.EventManager;
 import org.mule.galaxy.impl.jcr.onm.AbstractReflectionDao;
 import org.mule.galaxy.util.SecurityUtils;
 import org.springmodules.jcr.JcrCallback;
 
 public class CommentManagerImpl extends AbstractReflectionDao<Comment> implements CommentManager {
 
     private EventManager eventManager;
 
     public CommentManagerImpl() throws Exception {
         super(Comment.class, "comments", true);
     }
 
     public List<Comment> getComments(final String artifactId) {
         return getComments(artifactId, false);
     }
 
     @SuppressWarnings("unchecked")
     public List<Comment> getComments(final String artifactId, final boolean includeChildren) {
         return (List<Comment>) execute(new JcrCallback() {
             public Object doInJcr(Session session) throws IOException, RepositoryException {
                 StringBuilder qstr = new StringBuilder();
                 qstr.append("/jcr:root/comments/*[");
                 if (!includeChildren) {
                     qstr.append("not(@parent) and");
                 }
                 qstr.append("@item='")
                         .append(artifactId)
                         .append("'] order by @date ascending");
                 return query(qstr.toString(), session);
             }
         });
     }
 
     @SuppressWarnings("unchecked")
     public List<Comment> getRecentComments(final int maxResults) {
         return (List<Comment>) execute(new JcrCallback() {
             public Object doInJcr(Session session) throws IOException, RepositoryException {
                 return query("/jcr:root/comments/* order by @date descending", session, maxResults);
             }
         });
     }
 
     public void addComment(Comment c) {
         try {
             save(c);
         } catch (DuplicateItemException e1) {
             // should never happen
             throw new RuntimeException(e1);
         } catch (NotFoundException e1) {
             // should never happen
             throw new RuntimeException(e1);
         }
 
         // fire the event
         // FIXME: null pointer on test and itemNotFoundException over rpc
         Item item = c.getItem();
         Comment parent = c;
         while (item == null) {
            parent = c.getParent();
             item = parent.getItem();
         }
         EntryCommentCreatedEvent event = new EntryCommentCreatedEvent(item, c);
         event.setUser(SecurityUtils.getCurrentUser());
         eventManager.fireEvent(event);
     }
 
     public Comment getComment(String commentId) throws NotFoundException {
         return get(commentId);
     }
 
     public EventManager getEventManager() {
         return eventManager;
     }
 
     public void setEventManager(final EventManager eventManager) {
         this.eventManager = eventManager;
     }
 
 
 }
