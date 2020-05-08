 package pl.project.blog;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 import pl.project.blog.domain.Post;
 import org.jcouchdb.db.Database;
 import org.jcouchdb.db.Options;
 import org.jcouchdb.document.ValueAndDocumentRow;
 import org.jcouchdb.document.ViewAndDocumentsResult;
 import org.jcouchdb.exception.NotFoundException;
 import org.jcouchdb.exception.UpdateConflictException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.beans.factory.annotation.Required;
 import org.springframework.security.providers.encoding.ShaPasswordEncoder;
 import org.springframework.util.Assert;
 import pl.project.blog.auth.Roles;
 import pl.project.blog.domain.AppDocument;
 import pl.project.blog.domain.Comment;
 import pl.project.blog.domain.PostTag;
 import pl.project.blog.domain.Tag;
 import pl.project.blog.domain.User;
 
 /**
  *
  * @author Jarosław Bela
  */
 public class BlogServiceImpl implements BlogService, InitializingBean {
 
     private static Logger log = LoggerFactory.getLogger(BlogServiceImpl.class);
     private Database database;
     //
     private List<Tag> availableTags = null;
 
     @Required
     public void setDatabase(Database database) {
         Assert.notNull(database, "systemDatabase can't be null");
         this.database = database;
     }
 
     /**
      * {@inheritDoc}
      */
     public List<Post> listPosts(Boolean initializeCollections) {
         List<Post> posts = new ArrayList<Post>();
 
         if (initializeCollections) {
             ViewAndDocumentsResult<Object, AppDocument> result = database.queryViewAndDocuments("post/all", Object.class, AppDocument.class, null, null);
 
             Post lastPost = null;
             for (ValueAndDocumentRow<Object, AppDocument> row : result.getRows()) {
                 AppDocument doc = row.getDocument();
 
                 if (doc instanceof Post) {
                     posts.add((Post) doc);
                     lastPost = (Post) doc;
                 }
 
                 if (doc instanceof Comment) {
                     if (lastPost != null) {
                         lastPost.addComment((Comment) doc);
                     }
                 }
 
                 if (doc instanceof PostTag) {
                     if (lastPost != null) {
                         try {
                             lastPost.addTag(database.getDocument(Tag.class, ((PostTag) doc).getTag_id()));
                         } catch (NotFoundException ex) {
                             log.error("Dokument o id = '%s' nie istnieje", ((PostTag) doc).getTag_id());
                         }
                     }
                 }
             }
 
             // Sort comments and posts by data
             Collections.sort(posts);
             for (Post post : posts) {
                 Collections.sort(post.getComments());
             }
         } else {
             ViewAndDocumentsResult<Object, Post> result = database.queryViewAndDocuments("post/byCreateDate", Object.class, Post.class, Options.option().descending(true), null);
 
             for (ValueAndDocumentRow<Object, Post> row : result.getRows()) {
                 posts.add(row.getDocument());
             }
         }
 
         return posts;
     }
 
     /**
      * {@inheritDoc}
      */
     public Post createPost(Post post) {
 
         try {
             persist(post);
         } catch (IllegalStateException ex) {
             log.error("Document already had a revision set!");
         } catch (UpdateConflictException ex) {
             log.error("There's an update conflict while updating the document!");
         }
 
         return post;
     }
 
     public Post updatePost(Post post) {
 
         try {
             // Usuń stare tagi
             deleteTagsFromPost(post.getId());
             post.setTags(new ArrayList<Tag>());
             update(post);
         } catch (IllegalStateException ex) {
             log.error("Document already had a revision set!");
         } catch (UpdateConflictException ex) {
             log.error("There's an update conflict while updating the document!");
         }
 
         return post;
     }
 
     public Comment createComment(Comment comment) {
 
         try {
             persist(comment);
         } catch (IllegalStateException ex) {
             log.error("Document already had a revision set!");
         } catch (UpdateConflictException ex) {
             log.error("There's an update conflict while updating the document!");
         }
 
         return comment;
     }
 
     /**
      * {@inheritDoc}
      */
     public void deletePost(Post post) {
         if (post != null) {
             for (Comment comment : post.getComments()) {
                 delete(comment.getId(), comment.getRevision());
             }
             deleteTagsFromPost(post.getId());
             delete(post.getId(), post.getRevision());
         }
     }
 
     private void deleteTagsFromPost(String postId) {
         ViewAndDocumentsResult<Object, PostTag> result = database.queryViewAndDocuments("relation/post-tag", Object.class, PostTag.class, Options.option().key(postId), null);
         for (ValueAndDocumentRow<Object, PostTag> row : result.getRows()) {
             delete(row.getDocument().getId(), row.getDocument().getRevision());
 
             Tag tag = database.getDocument(Tag.class, row.getDocument().getTag_id());
             tag.setCount(tag.getCount() - 1);
             database.updateDocument(tag);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public Post getPost(String id, Boolean initializeCollections) {
         try {
             Post post = null;
 
             if (initializeCollections) {
                 ViewAndDocumentsResult<Object, AppDocument> result = database.queryViewAndDocuments("post/byId", Object.class, AppDocument.class, Options.option().key(id), null);
 
                 List<Comment> comments = new ArrayList<Comment>();
                 List<Tag> tags = new ArrayList<Tag>();
 
                 for (ValueAndDocumentRow<Object, AppDocument> row : result.getRows()) {
                     AppDocument doc = row.getDocument();
 
                     System.out.println("Dokument :" + doc);
                     if (doc instanceof Post) {
                         post = (Post) doc;
                     }
                     if (doc instanceof Comment) {
                         comments.add((Comment) doc);
                     }
                     if (doc instanceof PostTag) {
                         tags.add(database.getDocument(Tag.class, ((PostTag) doc).getTag_id()));
                     }
                 }
 
                 if (post != null) {
                     post.setComments(comments);
                     post.setTags(tags);
                 }
             } else {
                 post = database.getDocument(Post.class, id);
             }
 
             return post;
         } catch (NotFoundException ex) {
             return null;
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public List<Comment> getCommentsForPost(String id) {
         List<Comment> comments = new ArrayList<Comment>();
 
         ViewAndDocumentsResult<Object, Comment> result = database.queryViewAndDocuments("comment/byPostId", Object.class, Comment.class, Options.option().key(id), null);
 
         for (ValueAndDocumentRow<Object, Comment> row : result.getRows()) {
             comments.add(row.getDocument());
         }
 
         return comments;
     }
 
     /**
      * {@inheritDoc}
      */
     public User getUser(String userName) {
         ViewAndDocumentsResult<Object, User> result = database.queryViewAndDocuments("user/byName", Object.class, User.class, new Options().key(userName), null);
 
         if (result.getRows().size() == 0 || result.getRows().size() > 1) {
             return null;
         }
 
         return result.getRows().get(0).getDocument();
     }
 
     public void afterPropertiesSet() throws Exception {
         initialize();
     }
 
     private void initialize() {
 
         // utwórz przykładowe tagi
         List<Tag> tags = getAvailableTags(false);
         if (tags.isEmpty()) {
             for (int i = 0; i < 10; i++) {
                 Tag tag = new Tag();
                 tag.setName("tag" + i);
                tag.setCount(0);
                 persist(tag);
                 tags.add(tag);
             }
         }
 
         if (listPosts(false).isEmpty()) {
             for (int i = 0; i < 5; i++) {
                 Post post = new Post();
 
                 post.setTitle("Title " + i);
                 post.setContent("Content " + i);
                 post.setCreateDate(new Date(110, 10, 22, 21, 36, 1 + i));
 
                 for (int l = 0; l < 7; l++) {
                     Comment comment = new Comment();
                     comment.setAuthor("Jarek" + l);
                     comment.setContent("Tekst komentarza" + l);
                     comment.setCreated(new Date(110, 10, 22, 21, 36, 1 + l));
 
                     post.addComment(comment);
                 }
                 post.addTag(tags.get(i));
                 post.addTag(tags.get(i + 1));
                 post.addTag(tags.get(i + 2));
                 post.addTag(tags.get(i + 3));
                 post.addTag(tags.get(i + 4));
 
                 persist(post);
             }
         }
 
         if (getUser("admin") == null) {
             User user = new User();
             user.setName("admin");
             user.setPasswordHash(new ShaPasswordEncoder(256).encodePassword("admin", null));
             user.setRoles(Arrays.asList(Roles.ROLE_ADMIN.getRoleName(), Roles.ROLE_USER.getRoleName()));
             persist(user);
         }
     }
 
     public List<Tag> getAvailableTags(Boolean forceReload) {
         if (forceReload || availableTags == null) {
             availableTags = new ArrayList<Tag>();
 
             ViewAndDocumentsResult<Object, Tag> result = database.queryViewAndDocuments("tag/byName", Object.class, Tag.class, null, null);
             for (ValueAndDocumentRow<Object, Tag> row : result.getRows()) {
                 availableTags.add(row.getDocument());
             }
         }
 
         return availableTags;
     }
 
     /**
      * Metoda do zapisu encji w bazie danych.
      * 
      * @param appDocument
      */
     private void persist(AppDocument appDocument)
             throws IllegalStateException, UpdateConflictException {
         appDocument.beforePersist(database);
         database.createDocument(appDocument);
         appDocument.afterPersist(database);
     }
 
     /**
      * Metoda do uaktualniania encji w bazie danych.
      * 
      * @param appDocument
      */
     private void update(AppDocument appDocument)
             throws IllegalStateException, UpdateConflictException {
         appDocument.beforeUpdate(database);
         database.updateDocument(appDocument);
         appDocument.afterUpdate(database);
     }
 
     /**
      * Metoda do uaktualniania encji w bazie danych.
      *
      * @param appDocument
      */
     private void delete(String id, String rev) {
         database.delete(id, rev);
     }
 }
