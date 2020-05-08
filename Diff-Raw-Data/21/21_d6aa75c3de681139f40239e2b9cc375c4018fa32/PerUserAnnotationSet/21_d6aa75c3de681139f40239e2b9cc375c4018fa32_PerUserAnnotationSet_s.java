 package us.physion.ovation.ui.detailviews;
 
 import com.google.common.base.Function;
 import com.google.common.collect.Iterables;
 import us.physion.ovation.DataContext;
 import us.physion.ovation.DataStoreCoordinator;
 import us.physion.ovation.domain.OvationEntity;
 import us.physion.ovation.domain.User;
 import us.physion.ovation.domain.mixin.Owned;
 import us.physion.ovation.ui.TableTreeKey;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * Created with IntelliJ IDEA.
  * User: huecotanks
  * Date: 4/3/13
  * Time: 1:11 PM
  * To change this template use File | Settings | File Templates.
  */
 public abstract class PerUserAnnotationSet implements TableTreeKey{
     String username;
     String userURI;
     boolean isOwner;
     boolean current;
     Set<String> uris;
 
     public PerUserAnnotationSet(User u, boolean currentUser, boolean isOwner, Set<String> uris) {
         userURI = u.getURI().toString();
         this.current = currentUser;
         username = u.getUsername();
         this.isOwner = isOwner;
         this.uris = uris;
     }
 
 
     abstract protected void refreshAnnotations(User u, Iterable<OvationEntity> entities);


        public String getID()
     {
         return userURI;
     }
 
     boolean isCurrentUser() {
         return current;
     }
 
     String getUsername()
     {
         return username;
     }
 
     public int compareTo(Object t) {
         if (t instanceof UserPropertySet)
         {
             UserPropertySet s = (UserPropertySet)t;
 
             if (s.isCurrentUser())
             {
                 if (this.isCurrentUser())
                     return 0;
                 return 1;
             }
             if (this.isCurrentUser())
                 return -1;
             return this.getUsername().compareTo(s.getUsername());
         }
         else{
             throw new UnsupportedOperationException("Object type '" + t.getClass() + "' cannot be compared with object type " + this.getClass());
         }
     }
 
     public boolean isEditable()
     {
         return isCurrentUser();
     }
 
     public boolean isExpandedByDefault()
     {
         return isCurrentUser();
     }
 
     public String getDisplayName(String suffix) {
         String s = username + "'s " + suffix;
         if (isOwner) {
             return s + " (owner)";
         }
         return s;
     }
 
     public void refresh(DataStoreCoordinator dsc) {
         final DataContext c = dsc.getContext();
         User u = (User)c.getObjectWithURI(getID());
 
         Iterable<OvationEntity> entities = Iterables.transform(uris, new Function<String, OvationEntity>() {
             @Override
             public OvationEntity apply(String uri) {
                 return c.getObjectWithURI(uri);
             }
         });
 
         refreshOwner(u, entities);
         refreshAnnotations(u, entities);
 
         username = u.getUsername();
         this.current = c.getAuthenticatedUser().getUuid().equals(u.getUuid());
 
     }
 
     private void refreshOwner(User u, Iterable<OvationEntity> entities) {
         boolean owner = false;
         for (OvationEntity entity : entities)
         {
             owner = owner || (entity instanceof Owned && isOwnedBy(u, (Owned) entity));
         }
         this.isOwner = owner;
     }
 
     //TODO: move to Owned interface
     private boolean isOwnedBy(User u, Owned entity) {
         return entity.getOwner().getUuid().equals(u.getUuid());
     }
 }
