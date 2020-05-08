 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.cherrot.govproject.dao.jpa;
 
 import com.cherrot.govproject.dao.UserDao;
 import com.cherrot.govproject.dao.exceptions.IllegalOrphanException;
 import com.cherrot.govproject.dao.exceptions.NonexistentEntityException;
 import com.cherrot.govproject.model.Comment;
 import com.cherrot.govproject.model.Post;
 import com.cherrot.govproject.model.SiteLog;
 import com.cherrot.govproject.model.User;
 import com.cherrot.govproject.model.Usermeta;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.persistence.EntityManager;
 import javax.persistence.EntityNotFoundException;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Query;
 import javax.persistence.criteria.CriteriaQuery;
 import javax.persistence.criteria.Root;
 import org.springframework.stereotype.Repository;
 import org.springframework.transaction.annotation.Transactional;
 
 /**
  *
  * @author cherrot
  */
 @Repository
 public class UserJpaDao implements UserDao {
 
     @PersistenceContext
     private EntityManager em;
 //    public UserJpaDao(UserTransaction utx, EntityManagerFactory emf) {
 //        this.utx = utx;
 //        this.emf = emf;
 //    }
 //    private UserTransaction utx = null;
 //    private EntityManagerFactory emf = null;
 //
 //    @Override
 //    public EntityManager getEntityManager() {
 //        return emf.createEntityManager();
 //    }
 
     @Override
     @Transactional
     public void create(User user) {
         if (user.getSiteLogList() == null) {
             user.setSiteLogList(new ArrayList<SiteLog>());
         }
         if (user.getPostList() == null) {
             user.setPostList(new ArrayList<Post>());
         }
         if (user.getUsermetaList() == null) {
             user.setUsermetaList(new ArrayList<Usermeta>());
         }
         if (user.getCommentList() == null) {
             user.setCommentList(new ArrayList<Comment>());
         }
 //        EntityManager em = null;
 //        try {
 //            em = getEntityManager();
 //            em.getTransaction().begin();
             List<SiteLog> attachedSiteLogList = new ArrayList<SiteLog>();
             for (SiteLog siteLogListSiteLogToAttach : user.getSiteLogList()) {
                 siteLogListSiteLogToAttach = em.getReference(siteLogListSiteLogToAttach.getClass(), siteLogListSiteLogToAttach.getId());
                 attachedSiteLogList.add(siteLogListSiteLogToAttach);
             }
             user.setSiteLogList(attachedSiteLogList);
             List<Post> attachedPostList = new ArrayList<Post>();
             for (Post postListPostToAttach : user.getPostList()) {
                 postListPostToAttach = em.getReference(postListPostToAttach.getClass(), postListPostToAttach.getId());
                 attachedPostList.add(postListPostToAttach);
             }
             user.setPostList(attachedPostList);
             List<Usermeta> attachedUsermetaList = new ArrayList<Usermeta>();
             for (Usermeta usermetaListUsermetaToAttach : user.getUsermetaList()) {
                 usermetaListUsermetaToAttach = em.getReference(usermetaListUsermetaToAttach.getClass(), usermetaListUsermetaToAttach.getId());
                 attachedUsermetaList.add(usermetaListUsermetaToAttach);
             }
             user.setUsermetaList(attachedUsermetaList);
             List<Comment> attachedCommentList = new ArrayList<Comment>();
             for (Comment commentListCommentToAttach : user.getCommentList()) {
                 commentListCommentToAttach = em.getReference(commentListCommentToAttach.getClass(), commentListCommentToAttach.getId());
                 attachedCommentList.add(commentListCommentToAttach);
             }
             em.persist(user);
             for (SiteLog siteLogListSiteLog : user.getSiteLogList()) {
                 User oldUserOfSiteLogListSiteLog = siteLogListSiteLog.getUser();
                 siteLogListSiteLog.setUser(user);
                 siteLogListSiteLog = em.merge(siteLogListSiteLog);
                 if (oldUserOfSiteLogListSiteLog != null) {
                     oldUserOfSiteLogListSiteLog.getSiteLogList().remove(siteLogListSiteLog);
                     oldUserOfSiteLogListSiteLog = em.merge(oldUserOfSiteLogListSiteLog);
                 }
             }
             for (Post postListPost : user.getPostList()) {
                 User oldUserOfPostListPost = postListPost.getUser();
                 postListPost.setUser(user);
                 postListPost = em.merge(postListPost);
                 if (oldUserOfPostListPost != null) {
                     oldUserOfPostListPost.getPostList().remove(postListPost);
                     oldUserOfPostListPost = em.merge(oldUserOfPostListPost);
                 }
             }
             for (Usermeta usermetaListUsermeta : user.getUsermetaList()) {
                 User oldUserOfUsermetaListUsermeta = usermetaListUsermeta.getUser();
                 usermetaListUsermeta.setUser(user);
                 usermetaListUsermeta = em.merge(usermetaListUsermeta);
                 if (oldUserOfUsermetaListUsermeta != null) {
                     oldUserOfUsermetaListUsermeta.getUsermetaList().remove(usermetaListUsermeta);
                     oldUserOfUsermetaListUsermeta = em.merge(oldUserOfUsermetaListUsermeta);
                 }
             }
             for (Comment commentListComment : user.getCommentList()) {
                 User oldUserOfCommentListComment = commentListComment.getUser();
                 commentListComment.setUser(user);
                 commentListComment = em.merge(commentListComment);
                 if (oldUserOfCommentListComment != null) {
                     oldUserOfCommentListComment.getCommentList().remove(commentListComment);
                     oldUserOfCommentListComment = em.merge(oldUserOfCommentListComment);
                 }
             }
 //            em.getTransaction().commit();
 //        }
 //        finally {
 //            if (em != null) {
 //                em.close();
 //            }
 //        }
     }
 
     @Override
     @Transactional
     public void edit(User user) throws IllegalOrphanException, NonexistentEntityException, Exception {
 //        EntityManager em = null;
         try {
 //            em = getEntityManager();
 //            em.getTransaction().begin();
             User persistentUser = em.find(User.class, user.getId());
             List<SiteLog> siteLogListOld = persistentUser.getSiteLogList();
             List<SiteLog> siteLogListNew = user.getSiteLogList();
             List<Post> postListOld = persistentUser.getPostList();
             List<Post> postListNew = user.getPostList();
             List<Usermeta> usermetaListOld = persistentUser.getUsermetaList();
             List<Usermeta> usermetaListNew = user.getUsermetaList();
             List<Comment> commentListOld = persistentUser.getCommentList();
             List<Comment> commentListNew = user.getCommentList();
             List<String> illegalOrphanMessages = null;
             for (SiteLog siteLogListOldSiteLog : siteLogListOld) {
                 if (!siteLogListNew.contains(siteLogListOldSiteLog)) {
                     if (illegalOrphanMessages == null) {
                         illegalOrphanMessages = new ArrayList<String>();
                     }
                     illegalOrphanMessages.add("You must retain SiteLog " + siteLogListOldSiteLog + " since its user field is not nullable.");
                 }
             }
             for (Post postListOldPost : postListOld) {
                 if (!postListNew.contains(postListOldPost)) {
                     if (illegalOrphanMessages == null) {
                         illegalOrphanMessages = new ArrayList<String>();
                     }
                     illegalOrphanMessages.add("You must retain Post " + postListOldPost + " since its user field is not nullable.");
                 }
             }
             for (Usermeta usermetaListOldUsermeta : usermetaListOld) {
                 if (!usermetaListNew.contains(usermetaListOldUsermeta)) {
                     if (illegalOrphanMessages == null) {
                         illegalOrphanMessages = new ArrayList<String>();
                     }
                     illegalOrphanMessages.add("You must retain Usermeta " + usermetaListOldUsermeta + " since its user field is not nullable.");
                 }
             }
             for (Comment commentListOldComment : commentListOld) {
                 if (!commentListNew.contains(commentListOldComment)) {
                     if (illegalOrphanMessages == null) {
                         illegalOrphanMessages = new ArrayList<String>();
                     }
                     illegalOrphanMessages.add("You must retain Comment " + commentListOldComment + " since its user field is not nullable.");
                 }
             }
             if (illegalOrphanMessages != null) {
                 throw new IllegalOrphanException(illegalOrphanMessages);
             }
             List<SiteLog> attachedSiteLogListNew = new ArrayList<SiteLog>();
             for (SiteLog siteLogListNewSiteLogToAttach : siteLogListNew) {
                 siteLogListNewSiteLogToAttach = em.getReference(siteLogListNewSiteLogToAttach.getClass(), siteLogListNewSiteLogToAttach.getId());
                 attachedSiteLogListNew.add(siteLogListNewSiteLogToAttach);
             }
             siteLogListNew = attachedSiteLogListNew;
             user.setSiteLogList(siteLogListNew);
             List<Post> attachedPostListNew = new ArrayList<Post>();
             for (Post postListNewPostToAttach : postListNew) {
                 postListNewPostToAttach = em.getReference(postListNewPostToAttach.getClass(), postListNewPostToAttach.getId());
                 attachedPostListNew.add(postListNewPostToAttach);
             }
             postListNew = attachedPostListNew;
             user.setPostList(postListNew);
             List<Usermeta> attachedUsermetaListNew = new ArrayList<Usermeta>();
             for (Usermeta usermetaListNewUsermetaToAttach : usermetaListNew) {
                 usermetaListNewUsermetaToAttach = em.getReference(usermetaListNewUsermetaToAttach.getClass(), usermetaListNewUsermetaToAttach.getId());
                 attachedUsermetaListNew.add(usermetaListNewUsermetaToAttach);
             }
             usermetaListNew = attachedUsermetaListNew;
             user.setUsermetaList(usermetaListNew);
             List<Comment> attachedCommentListNew = new ArrayList<Comment>();
             for (Comment commentListNewCommentToAttach : commentListNew) {
                 commentListNewCommentToAttach = em.getReference(commentListNewCommentToAttach.getClass(), commentListNewCommentToAttach.getId());
                 attachedCommentListNew.add(commentListNewCommentToAttach);
             }
             user = em.merge(user);
             for (SiteLog siteLogListNewSiteLog : siteLogListNew) {
                 if (!siteLogListOld.contains(siteLogListNewSiteLog)) {
                     User oldUserOfSiteLogListNewSiteLog = siteLogListNewSiteLog.getUser();
                     siteLogListNewSiteLog.setUser(user);
                     siteLogListNewSiteLog = em.merge(siteLogListNewSiteLog);
                     if (oldUserOfSiteLogListNewSiteLog != null && !oldUserOfSiteLogListNewSiteLog.equals(user)) {
                         oldUserOfSiteLogListNewSiteLog.getSiteLogList().remove(siteLogListNewSiteLog);
                         oldUserOfSiteLogListNewSiteLog = em.merge(oldUserOfSiteLogListNewSiteLog);
                     }
                 }
             }
             for (Post postListNewPost : postListNew) {
                 if (!postListOld.contains(postListNewPost)) {
                     User oldUserOfPostListNewPost = postListNewPost.getUser();
                     postListNewPost.setUser(user);
                     postListNewPost = em.merge(postListNewPost);
                     if (oldUserOfPostListNewPost != null && !oldUserOfPostListNewPost.equals(user)) {
                         oldUserOfPostListNewPost.getPostList().remove(postListNewPost);
                         oldUserOfPostListNewPost = em.merge(oldUserOfPostListNewPost);
                     }
                 }
             }
             for (Usermeta usermetaListNewUsermeta : usermetaListNew) {
                 if (!usermetaListOld.contains(usermetaListNewUsermeta)) {
                     User oldUserOfUsermetaListNewUsermeta = usermetaListNewUsermeta.getUser();
                     usermetaListNewUsermeta.setUser(user);
                     usermetaListNewUsermeta = em.merge(usermetaListNewUsermeta);
                     if (oldUserOfUsermetaListNewUsermeta != null && !oldUserOfUsermetaListNewUsermeta.equals(user)) {
                         oldUserOfUsermetaListNewUsermeta.getUsermetaList().remove(usermetaListNewUsermeta);
                         oldUserOfUsermetaListNewUsermeta = em.merge(oldUserOfUsermetaListNewUsermeta);
                     }
                 }
             }
             for (Comment commentListNewComment : commentListNew) {
                 if (!commentListOld.contains(commentListNewComment)) {
                     User oldUserOfCommentListNewComment = commentListNewComment.getUser();
                     commentListNewComment.setUser(user);
                     commentListNewComment = em.merge(commentListNewComment);
                     if (oldUserOfCommentListNewComment != null && !oldUserOfCommentListNewComment.equals(user)) {
                         oldUserOfCommentListNewComment.getCommentList().remove(commentListNewComment);
                         oldUserOfCommentListNewComment = em.merge(oldUserOfCommentListNewComment);
                     }
                 }
             }
 //            em.getTransaction().commit();
         }
         catch (Exception ex) {
             String msg = ex.getLocalizedMessage();
             if (msg == null || msg.length() == 0) {
                 Integer id = user.getId();
                 if (find(id) == null) {
                     throw new NonexistentEntityException("The user with id " + id + " no longer exists.");
                 }
             }
             throw ex;
         }
 //        finally {
 //            if (em != null) {
 //                em.close();
 //            }
 //        }
     }
//FIXME 还没写完！！！
     @Override
     @Transactional
     public void destroy(Integer id) throws IllegalOrphanException, NonexistentEntityException {
 //        EntityManager em = null;
 //        try {
 //            em = getEntityManager();
 //            em.getTransaction().begin();
             User user;
             try {
                 user = em.getReference(User.class, id);
                 user.getId();
             } catch (EntityNotFoundException enfe) {
                 throw new NonexistentEntityException("The user with id " + id + " no longer exists.", enfe);
             }
             List<String> illegalOrphanMessages = null;
             List<SiteLog> siteLogListOrphanCheck = user.getSiteLogList();
             for (SiteLog siteLogListOrphanCheckSiteLog : siteLogListOrphanCheck) {
                 if (illegalOrphanMessages == null) {
                     illegalOrphanMessages = new ArrayList<String>();
                 }
                 illegalOrphanMessages.add("This User (" + user + ") cannot be destroyed since the SiteLog " + siteLogListOrphanCheckSiteLog + " in its siteLogList field has a non-nullable user field.");
             }
             List<Post> postListOrphanCheck = user.getPostList();
             for (Post postListOrphanCheckPost : postListOrphanCheck) {
                 if (illegalOrphanMessages == null) {
                     illegalOrphanMessages = new ArrayList<String>();
                 }
                 illegalOrphanMessages.add("This User (" + user + ") cannot be destroyed since the Post " + postListOrphanCheckPost + " in its postList field has a non-nullable user field.");
             }
             List<Usermeta> usermetaListOrphanCheck = user.getUsermetaList();
             for (Usermeta usermetaListOrphanCheckUsermeta : usermetaListOrphanCheck) {
                 if (illegalOrphanMessages == null) {
                     illegalOrphanMessages = new ArrayList<String>();
                 }
                 illegalOrphanMessages.add("This User (" + user + ") cannot be destroyed since the Usermeta " + usermetaListOrphanCheckUsermeta + " in its usermetaList field has a non-nullable user field.");
             }
             if (illegalOrphanMessages != null) {
                 throw new IllegalOrphanException(illegalOrphanMessages);
             }
             em.remove(user);
 //            em.getTransaction().commit();
 //        }
 //        finally {
 //            if (em != null) {
 //                em.close();
 //            }
 //        }
     }
 
     @Override
     public List<User> findEntities() {
         return findEntities(true, -1, -1);
     }
 
     @Override
     public List<User> findEntities(int maxResults, int firstResult) {
         return findEntities(false, maxResults, firstResult);
     }
 
     private List<User> findEntities(boolean all, int maxResults, int firstResult) {
 //        EntityManager em = getEntityManager();
 //        try {
             CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
             cq.select(cq.from(User.class));
             Query q = em.createQuery(cq);
             if (!all) {
                 q.setMaxResults(maxResults);
                 q.setFirstResult(firstResult);
             }
             return q.getResultList();
 //        }
 //        finally {
 //            em.close();
 //        }
     }
 
     @Override
     public User find(Integer id) {
 //        EntityManager em = getEntityManager();
 //        try {
             return em.find(User.class, id);
 //        }
 //        finally {
 //            em.close();
 //        }
     }
 
     @Override
     public User findByLogin(String loginName) {
         return em.createNamedQuery("User.findByLogin", User.class)
                 .setParameter("login", loginName).getSingleResult();
     }
 
     @Override
     public List<User> findEntitiesByUserLevel(int userLevel) {
         return em.createNamedQuery("User.findByUserLevel", User.class)
                 .setParameter("userLevel", userLevel).getResultList();
     }
 
     @Override
     public int getCount() {
 //        EntityManager em = getEntityManager();
 //        try {
             CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
             Root<User> rt = cq.from(User.class);
             cq.select(em.getCriteriaBuilder().count(rt));
             Query q = em.createQuery(cq);
             return ( (Long) q.getSingleResult() ).intValue();
 //        }
 //        finally {
 //            em.close();
 //        }
     }
 
     @Override
     public void save(User model) {
         if (model.getId() == null) {
             create(model);
         } else {
             try {
                 edit(model);
             }
             catch (Exception ex) {
                 Logger.getLogger(CommentmetaJpaDao.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
     }
 
 }
