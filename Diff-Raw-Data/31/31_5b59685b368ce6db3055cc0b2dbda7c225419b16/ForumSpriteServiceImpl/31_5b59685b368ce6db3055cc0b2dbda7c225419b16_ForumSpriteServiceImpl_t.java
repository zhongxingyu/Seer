 package net.pickapack.notice.service;
 
 import com.j256.ormlite.dao.Dao;
 import net.pickapack.notice.model.forum.*;
 import net.pickapack.model.ModelElement;
 import net.pickapack.service.AbstractService;
 
 import java.sql.SQLException;
 import java.util.Arrays;
 import java.util.List;
 
 public class ForumSpriteServiceImpl extends AbstractService implements ForumSpriteService {
     private Dao<ForumUser, Long> forumUsers;
     private Dao<Forum, Long> forums;
     private Dao<ForumThread, Long> forumThreads;
     private Dao<ForumThreadMessage, Long> forumThreadMessages;
     private Dao<ForumUserPermission, Long> forumUserPermissions;
 
     @SuppressWarnings("unchecked")
     public ForumSpriteServiceImpl(){
         super(ServiceManager.DATABASE_URL, Arrays.<Class<? extends ModelElement>>asList(ForumUser.class, Forum.class, ForumThread.class, ForumThreadMessage.class, ForumUserPermission.class));
 
        this.forumUsers = (Dao<ForumUser, Long>) createDao(ForumUser.class);
        this.forums = (Dao<Forum, Long>) createDao(Forum.class);
        this.forumThreads = (Dao<ForumThread, Long>) createDao(ForumThread.class);
        this.forumThreadMessages = (Dao<ForumThreadMessage, Long>) createDao(ForumThreadMessage.class);
        this.forumUserPermissions = (Dao<ForumUserPermission, Long>) createDao(ForumUserPermission.class);
     }
 
     @Override
     public List<ForumUser> getAllForumUsers() {
         return this.getAllItems(this.forumUsers);
     }
 
     @Override
     public ForumUser getForumUserById(long forumUserId) {
         return this.getItemById(this.forumUsers, forumUserId);
     }
 
     @Override
     public ForumUser getForumUserByName(String name) {
         try {
             List<ForumUser> result = this.forumUsers.queryForEq("name", name);
             return result.isEmpty() ? null : result.get(0);
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
     }
 
     @Override
     public ForumUser getForumUserByEmail(String email) {
         try {
             List<ForumUser> result = this.forumUsers.queryForEq("email", email);
             return result.isEmpty() ? null : result.get(0);
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
     }
 
     @Override
     public void addForumUser(ForumUser forumUser) {
         this.addItem(this.forumUsers, ForumUser.class, forumUser);
     }
 
     @Override
     public void removeForumUserById(long forumUserId) {
         this.removeItemById(this.forumUsers, ForumUser.class, forumUserId);
     }
 
     @Override
     public void updateForumUser(ForumUser forumUser) {
         this.updateItem(this.forumUsers, ForumUser.class, forumUser);
     }
 
     @Override
     public List<Forum> getAllForums() {
         return this.getAllItems(this.forums);
     }
 
     @Override
     public List<Forum> getForumsByParent(Forum parent) {
         return this.getItemsByParent(this.forums, parent);
     }
 
     @Override
     public Forum getForumById(long forumId) {
         return this.getItemById(this.forums, forumId);
     }
 
     @Override
     public Forum getForumByName(String name) {
         try {
             List<Forum> result = this.forums.queryForEq("name", name);
             return result.isEmpty() ? null : result.get(0);
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
     }
 
     @Override
     public Forum getForumByLabel(String label) {
         try {
             List<Forum> result = this.forums.queryForEq("label", label);
             return result.isEmpty() ? null : result.get(0);
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
     }
 
     @Override
     public void addForum(Forum forum) {
         this.addItem(this.forums, Forum.class, forum);
     }
 
     @Override
     public void removeForumById(long forumId) {
         this.removeItemById(this.forums, Forum.class, forumId);
     }
 
     @Override
     public void updateForum(Forum forum) {
         this.updateItem(this.forums, Forum.class, forum);
     }
 
     @Override
     public List<ForumThread> getForumThreadsByParent(Forum parent) {
         return this.getItemsByParent(this.forumThreads, parent);
     }
 
     @Override
     public List<ForumThread> getForumThreadsBySubject(String subject) {
         try {
             return this.forumThreads.queryForEq("subject", subject);
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
     }
 
     @Override
     public ForumThread getForumThreadById(long forumThreadId) {
         return this.getItemById(this.forumThreads, forumThreadId);
     }
 
     @Override
     public ForumThread getForumThreadByParentAndLabel(Forum parent, String label) {
         try {
             return this.forumThreads.queryForFirst(this.forumThreads.queryBuilder().where().eq("parentId", parent.getId()).and().eq("label", label).prepare());
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
     }
 
     @Override
     public void addForumThread(ForumThread forumThread) {
         this.addItem(this.forumThreads, ForumThread.class, forumThread);
     }
 
     @Override
     public void removeForumThreadById(long forumThreadId) {
         this.removeItemById(this.forumThreads, ForumThread.class, forumThreadId);
     }
 
     @Override
     public void updateForumThread(ForumThread forumThread) {
         this.updateItem(this.forumThreads, ForumThread.class, forumThread);
     }
 
     @Override
     public List<ForumThreadMessage> getForumThreadMessagesByParent(ForumThread parent) {
         return this.getItemsByParent(this.forumThreadMessages, parent);
     }
 
     @Override
     public List<ForumThreadMessage> getForumThreadMessagesBySubject(String subject) {
         try {
             return this.forumThreadMessages.queryForEq("subject", subject);
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
     }
 
     @Override
     public List<ForumThreadMessage> getForumThreadMessagesByUserId(long forumUserId) {
         try {
             return this.forumThreadMessages.queryForEq("userId", forumUserId);
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
     }
 
     @Override
     public ForumThreadMessage getForumThreadMessageById(long forumThreadMessageId) {
         return this.getItemById(this.forumThreadMessages, forumThreadMessageId);
     }
 
     @Override
     public void addForumThreadMessage(ForumThreadMessage forumThreadMessage) {
         this.addItem(this.forumThreadMessages, ForumThreadMessage.class, forumThreadMessage);
     }
 
     @Override
     public void removeForumThreadMessageById(long forumThreadMessageId) {
         this.removeItemById(this.forumThreadMessages, ForumThreadMessage.class, forumThreadMessageId);
     }
 
     @Override
     public void updateForumThreadMessage(ForumThreadMessage forumThreadMessage) {
         this.updateItem(this.forumThreadMessages, ForumThreadMessage.class, forumThreadMessage);
     }
 
     @Override
     public ForumUserPermission getForumUserPermissionByParent(ForumUser parent) {
         return this.getFirstItemByParent(this.forumUserPermissions, parent);
     }
 
     @Override
     public void addForumUserPermission(ForumUserPermission forumUserPermission) {
         this.addItem(this.forumUserPermissions, ForumUserPermission.class, forumUserPermission);
     }
 
     @Override
     public void removeForumUserPermissionByParent(ForumUser parent) {
         this.removeItemById(this.forumUserPermissions, ForumUserPermission.class, this.getFirstItemByParent(this.forumUserPermissions, parent).getId());
     }
 
     @Override
     public void updateForumUserPermission(ForumUserPermission forumUserPermission) {
         this.updateItem(this.forumUserPermissions, ForumUserPermission.class, forumUserPermission);
     }
 }
