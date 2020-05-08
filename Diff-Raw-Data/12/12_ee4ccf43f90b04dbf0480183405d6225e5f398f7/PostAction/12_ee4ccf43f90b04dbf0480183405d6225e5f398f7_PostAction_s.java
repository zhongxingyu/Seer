 /*
  * Copyright (c) 2003, Rafael Steil
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, 
  * with or without modification, are permitted provided 
  * that the following conditions are met:
  * 
  * 1) Redistributions of source code must retain the above 
  * copyright notice, this list of conditions and the 
  * following  disclaimer.
  * 2)  Redistributions in binary form must reproduce the 
  * above copyright notice, this list of conditions and 
  * the following disclaimer in the documentation and/or 
  * other materials provided with the distribution.
  * 3) Neither the name of "Rafael Steil" nor 
  * the names of its contributors may be used to endorse 
  * or promote products derived from this software without 
  * specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT 
  * HOLDERS AND CONTRIBUTORS "AS IS" AND ANY 
  * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, 
  * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
  * MERCHANTABILITY AND FITNESS FOR A PARTICULAR 
  * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL 
  * THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE 
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, 
  * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
  * IN CONTRACT, STRICT LIABILITY, OR TORT 
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN 
  * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
  * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE
  * 
  * This file creation date: May 3, 2003 / 5:05:18 PM
  * The JForum Project
  * http://www.jforum.net
  */
 package net.jforum.view.forum;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 
 import net.jforum.Command;
 import net.jforum.JForum;
 import net.jforum.SessionFacade;
 import net.jforum.entities.Post;
 import net.jforum.entities.Topic;
 import net.jforum.entities.User;
 import net.jforum.entities.UserSession;
 import net.jforum.model.DataAccessDriver;
 import net.jforum.model.ForumModel;
 import net.jforum.model.PostModel;
 import net.jforum.model.TopicModel;
 import net.jforum.model.UserModel;
 import net.jforum.repository.ForumRepository;
 import net.jforum.repository.RankingRepository;
 import net.jforum.repository.SecurityRepository;
 import net.jforum.repository.SmiliesRepository;
 import net.jforum.repository.TopicRepository;
 import net.jforum.security.PermissionControl;
 import net.jforum.security.SecurityConstants;
 import net.jforum.util.I18n;
 import net.jforum.util.concurrent.executor.QueuedExecutor;
 import net.jforum.util.mail.EmailSenderTask;
 import net.jforum.util.mail.TopicSpammer;
 import net.jforum.util.preferences.ConfigKeys;
 import net.jforum.util.preferences.SystemGlobals;
 
 /**
  * @author Rafael Steil
 * @version $Id: PostAction.java,v 1.16 2004/10/24 21:59:24 rafaelsteil Exp $
  */
 public class PostAction extends Command 
 {
 	private static final Logger logger = Logger.getLogger(PostAction.class);
 	
     public void list() throws Exception 
 	{
         PostModel pm = DataAccessDriver.getInstance().newPostModel();
         UserModel um = DataAccessDriver.getInstance().newUserModel();
         TopicModel tm = DataAccessDriver.getInstance().newTopicModel();
 
         int topicId = Integer.parseInt(JForum.getRequest().getParameter("topic_id"));
         Topic topic = tm.selectById(topicId);
         
         // The topic exists?
         if (topic.getId() == 0) {
             this.topicNotFound();
             return;
         }
 
         // Shall we proceed?
         if (!TopicsCommon.isTopicAccessible(topic.getForumId())) {
         	return;
         }
 
         tm.incrementTotalViews(topic.getId());
 
         ((HashMap) SessionFacade.getAttribute("topics_tracking")).put(new Integer(topic.getId()),
                 new Long(topic.getLastPostTimeInMillis().getTime()));
 
         int count = SystemGlobals.getIntValue(ConfigKeys.POST_PER_PAGE);
         int start = ViewCommon.getStartPage();
 
         ArrayList posts = pm.selectAllByTopicByLimit(topicId, start, count);
         List helperList = new ArrayList();
         Map usersMap = new HashMap();
 
         int userId = SessionFacade.getUserSession().getUserId();
         int anonymousUser = SystemGlobals.getIntValue(ConfigKeys.ANONYMOUS_USER_ID);
         PermissionControl pc = SecurityRepository.get(userId);
 
         boolean canEdit = false;
         if (pc.canAccess(SecurityConstants.PERM_MODERATION_POST_EDIT)) {
             canEdit = true;
         }
 
         Iterator iter = posts.iterator();
         while (iter.hasNext()) {
             Post p = (Post) iter.next();
             if (canEdit || (p.getUserId() != anonymousUser && p.getUserId() == userId)) {
                 p.setCanEdit(true);
             }
 
             Integer posterId = new Integer(p.getUserId());
             if (!usersMap.containsKey(posterId)) {
                 User u = um.selectById(p.getUserId());
                 u.setSignature(PostCommon.processText(u.getSignature()));
                 u.setSignature(PostCommon.processSmilies(u.getSignature(), 
                 		SmiliesRepository.getSmilies()));
 
                 usersMap.put(posterId, u);
             }
 
             helperList.add(PostCommon.preparePostForDisplay(p));
         }
 
         boolean isModerator = (pc.canAccess(SecurityConstants.PERM_MODERATION))
                 && (pc.canAccess(SecurityConstants.PERM_MODERATION_FORUMS, Integer.toString(topic
                         .getForumId())));
 
         // Set the topic status as read
         tm.updateReadStatus(topic.getId(), userId, true);
 
         JForum.getContext().put("rssEnabled", SystemGlobals.getBoolValue(ConfigKeys.RSS_ENABLED));
         JForum.getContext().put("canRemove",
                 SecurityRepository.canAccess(SecurityConstants.PERM_MODERATION_POST_REMOVE));
         JForum.getContext().put("canEdit",
                 SecurityRepository.canAccess(SecurityConstants.PERM_MODERATION_POST_EDIT));
         JForum.getContext().put("moduleAction", "post_show.htm");
         JForum.getContext().put("topic", topic);
         JForum.getContext().put("rank", new RankingRepository());
         JForum.getContext().put("posts", helperList);
         JForum.getContext().put("forum", ForumRepository.getForum(topic.getForumId()));
         JForum.getContext().put("users", usersMap);
         JForum.getContext().put("topicId", new Integer(topicId));
         JForum.getContext().put("watching",
                 tm.isUserSubscribed(topicId, SessionFacade.getUserSession().getUserId()));
         JForum.getContext().put("pageTitle",
                 SystemGlobals.getValue(ConfigKeys.FORUM_NAME) + " - " + topic.getTitle());
         JForum.getContext().put("isAdmin",
                 SecurityRepository.canAccess(SecurityConstants.PERM_ADMINISTRATION));
         JForum.getContext().put(
                 "readonly",
                 !SecurityRepository.canAccess(SecurityConstants.PERM_READ_ONLY_FORUMS, Integer
                         .toString(topic.getForumId())));
 
         JForum.getContext().put(
                 "isModerator",
                 SecurityRepository.canAccess(SecurityConstants.PERM_MODERATION)
                         && SecurityRepository.canAccess(SecurityConstants.PERM_MODERATION_FORUMS,
                                 Integer.toString(topic.getForumId())));
 
         // Topic Status
         JForum.getContext().put("STATUS_LOCKED", new Integer(Topic.STATUS_LOCKED));
         JForum.getContext().put("STATUS_UNLOCKED", new Integer(Topic.STATUS_UNLOCKED));
 
         // Pagination
         int totalPosts = tm.getTotalPosts(topic.getId());
         JForum.getContext().put("totalPages", new Double(Math.ceil( (double)totalPosts / (double)count )));
         JForum.getContext().put("recordsPerPage", new Integer(count));
         JForum.getContext().put("totalRecords", new Integer(totalPosts));
         JForum.getContext().put("thisPage", new Double(Math.ceil( (double)(start+1) / (double)count )));
         JForum.getContext().put("start", new Integer(start));
     }
 
     private void topicNotFound() {
         JForum.getContext().put("moduleAction", "message.htm");
         JForum.getContext().put("message", I18n.getMessage("PostShow.TopicNotFound"));
     }
 
     private void postNotFound() {
         JForum.getContext().put("moduleAction", "message.htm");
         JForum.getContext().put("message", I18n.getMessage("PostShow.PostNotFound"));
     }
 
     public void insert() throws Exception {
         int forumId = Integer.parseInt(JForum.getRequest().getParameter("forum_id"));
 
         if (!this.anonymousPost(forumId)
                 || this.isForumReadonly(forumId,
                         JForum.getRequest().getParameter("topic_id") != null)) {
             return;
         }
 
         ForumModel fm = DataAccessDriver.getInstance().newForumModel();
 
         if (JForum.getRequest().getParameter("topic_id") != null) {
             int topicId = Integer.parseInt(JForum.getRequest().getParameter("topic_id"));
             Topic t = DataAccessDriver.getInstance().newTopicModel().selectById(topicId);
 
             if (t.getStatus() == Topic.STATUS_LOCKED) {
                 this.topicLocked();
                 return;
             }
 
             JForum.getContext().put("topic", t);
             JForum.getContext().put("setType", false);
         } 
         else {
             JForum.getContext().put("setType", true);
         }
 
         JForum.getContext().put("forum", ForumRepository.getForum(forumId));
         JForum.getContext().put("action", "insertSave");
         JForum.getContext().put("moduleAction", "post_form.htm");
         JForum.getContext().put("start", JForum.getRequest().getParameter("start"));
         JForum.getContext().put("isNewPost", true);
         JForum.getContext().put("htmlAllowed", SecurityRepository.canAccess(
         		SecurityConstants.PERM_HTML_DISABLED, Integer.toString(forumId)));
         JForum.getContext().put("canCreateStickyOrAnnouncementTopics",
                 SecurityRepository.canAccess(SecurityConstants.PERM_CREATE_STICKY_ANNOUNCEMENT_TOPICS));
 
         int userId = SessionFacade.getUserSession().getUserId();
         JForum.getContext().put("user", DataAccessDriver.getInstance().newUserModel().selectById(userId));
     }
 
     public void edit() throws Exception {
         this.edit(false, null);
     }
 
     private void edit(boolean preview, Post p) throws Exception {
         int sUserId = SessionFacade.getUserSession().getUserId();
         int aId = SystemGlobals.getIntValue(ConfigKeys.ANONYMOUS_USER_ID);
         boolean canAccess = false;
 
         if (!preview) {
             PostModel pm = DataAccessDriver.getInstance().newPostModel();
             p = pm.selectById(Integer.parseInt(JForum.getRequest().getParameter("post_id")));
 
             // The post exist?
             if (p.getId() == 0) {
                 this.postNotFound();
                 return;
             }
         }
 
         boolean isModerator = SecurityRepository
                 .canAccess(SecurityConstants.PERM_MODERATION_POST_EDIT);
         canAccess = (isModerator || p.getUserId() == sUserId);
 
         if ((sUserId != aId) && canAccess) {
             Topic topic = DataAccessDriver.getInstance().newTopicModel().selectById(p.getTopicId());
 
             if (!TopicsCommon.isTopicAccessible(topic.getForumId())) {
                 return;
             }
 
             if (topic.getStatus() == Topic.STATUS_LOCKED && !isModerator) {
                 this.topicLocked();
                 return;
             }
 
             if (preview && JForum.getRequest().getParameter("topic_type") != null) {
                 topic.setType(Integer.parseInt(JForum.getRequest().getParameter("topic_type")));
             }
 
             JForum.getContext().put("forum", ForumRepository.getForum(p.getForumId()));
             JForum.getContext().put("action", "editSave");
 
             JForum.getContext().put("post", p);
             JForum.getContext().put("setType", p.getId() == topic.getFirstPostId());
             JForum.getContext().put("topic", topic);
             JForum.getContext().put("moduleAction", "post_form.htm");
             JForum.getContext().put("start", JForum.getRequest().getParameter("start"));
             JForum.getContext().put("htmlAllowed", SecurityRepository.canAccess(
             		SecurityConstants.PERM_HTML_DISABLED, Integer.toString(topic.getForumId())));
             JForum.getContext().put("canCreateStickyOrAnnouncementTopics",
                     SecurityRepository.canAccess(SecurityConstants.PERM_CREATE_STICKY_ANNOUNCEMENT_TOPICS));
         } 
         else {
             JForum.getContext().put("moduleAction", "message.htm");
             JForum.getContext().put("message", I18n.getMessage("CannotEditPost"));
         }
 
         User u = DataAccessDriver.getInstance().newUserModel().selectById(sUserId);
         u.setSignature(PostCommon.processText(u.getSignature()));
         u.setSignature(PostCommon.processSmilies(u.getSignature(), SmiliesRepository.getSmilies()));
         JForum.getContext().put("user", u);
     }
 
     public void quote() throws Exception {
         PostModel pm = DataAccessDriver.getInstance().newPostModel();
         Post p = pm.selectById(Integer.parseInt(JForum.getRequest().getParameter("post_id")));
 
         if (!this.anonymousPost(p.getForumId())) {
             return;
         }
 
         Topic t = DataAccessDriver.getInstance().newTopicModel().selectById(p.getTopicId());
 
         if (!TopicsCommon.isTopicAccessible(t.getForumId())) {
             return;
         }
 
         if (t.getStatus() == Topic.STATUS_LOCKED) {
             this.topicLocked();
             return;
         }
 
         if (p.getId() == 0) {
             this.postNotFound();
             return;
         }
 
         JForum.getContext().put("forum", ForumRepository.getForum(p.getForumId()));
         JForum.getContext().put("action", "insertSave");
         JForum.getContext().put("post", p);
 
         UserModel um = DataAccessDriver.getInstance().newUserModel();
         User u = um.selectById(p.getUserId());
 
        JForum.getContext().put("topic",
                DataAccessDriver.getInstance().newTopicModel().selectById(p.getTopicId()));
         JForum.getContext().put("quote", "true");
         JForum.getContext().put("quoteUser", u.getUsername());
         JForum.getContext().put("moduleAction", "post_form.htm");
         JForum.getContext().put("setType", false);
         JForum.getContext().put("start", JForum.getRequest().getParameter("start"));
 
         int userId = SessionFacade.getUserSession().getUserId();
         JForum.getContext().put("user",
                 DataAccessDriver.getInstance().newUserModel().selectById(userId));
     }
 
     public void editSave() throws Exception {
         PostModel pm = DataAccessDriver.getInstance().newPostModel();
         TopicModel tm = DataAccessDriver.getInstance().newTopicModel();
 
         Post p = pm.selectById(Integer.parseInt(JForum.getRequest().getParameter("post_id")));
         p = PostCommon.fillPostFromRequest(p);
 
         // The user wants to preview the message before posting it?
         if (JForum.getRequest().getParameter("preview") != null) {
             JForum.getContext().put("preview", true);
             Post postPreview = new Post(p);
             JForum.getContext().put("postPreview", PostCommon.preparePostForDisplay(postPreview));
 
             this.edit(true, p);
         } else {
             Topic t = tm.selectById(p.getTopicId());
 
             if (!TopicsCommon.isTopicAccessible(t.getForumId())) {
                 return;
             }
 
             if (t.getStatus() == Topic.STATUS_LOCKED
                     && !SecurityRepository.canAccess(SecurityConstants.PERM_MODERATION_POST_EDIT)) {
                 this.topicLocked();
                 return;
             }
 
             pm.update(p);
 
             // Updates the topic title
             if (t.getFirstPostId() == p.getId()) {
                 t.setTitle(p.getSubject());
                 t.setType(Integer.parseInt(JForum.getRequest().getParameter("topic_type")));
                 tm.update(t);
                 ForumRepository.reloadForum(t.getForumId());
                 TopicRepository.clearCache(t.getForumId());
             }
 
             if (JForum.getRequest().getParameter("notify") == null) {
                 tm.removeSubscription(p.getTopicId(), SessionFacade.getUserSession().getUserId());
             }
 
             String path = JForum.getRequest().getContextPath() + "/posts/list/";
             String start = JForum.getRequest().getParameter("start");
             if (start != null && !start.equals("0")) {
                 path += start + "/";
             }
 
             path += p.getTopicId() + SystemGlobals.getValue(ConfigKeys.SERVLET_EXTENSION) + "#"
                     + p.getId();
             JForum.setRedirect(path);
         }
     }
 
     public void insertSave() throws Exception {
         Topic t = new Topic();
         t.setId(-1);
         t.setForumId(Integer.parseInt(JForum.getRequest().getParameter("forum_id")));
 
         if (!TopicsCommon.isTopicAccessible(t.getForumId())
                 || this.isForumReadonly(t.getForumId(), 
                 		JForum.getRequest().getParameter("topic_id") != null)) {
             return;
         }
 
         TopicModel tm = DataAccessDriver.getInstance().newTopicModel();
         PostModel pm = DataAccessDriver.getInstance().newPostModel();
         ForumModel fm = DataAccessDriver.getInstance().newForumModel();
 
         if (JForum.getRequest().getParameter("topic_id") != null) {
             t = tm.selectById(Integer.parseInt(JForum.getRequest().getParameter("topic_id")));
 
             // Cannot insert new messages on locked topics
             if (t.getStatus() == Topic.STATUS_LOCKED) {
                 this.topicLocked();
                 return;
             }
         }
 
         if (JForum.getRequest().getParameter("topic_type") != null) {
             t.setType(Integer.parseInt(JForum.getRequest().getParameter("topic_type")));
         }
 
         UserSession us = SessionFacade.getUserSession();
         User u = new User();
         u.setId(us.getUserId());
         u.setUsername(us.getUsername());
 
         t.setPostedBy(u);
 
         // Set the Post
         Post p = PostCommon.fillPostFromRequest();
         p.setForumId(Integer.parseInt(JForum.getRequest().getParameter("forum_id")));
 
         boolean preview = (JForum.getRequest().getParameter("preview") != null);
         if (!preview) {
             // If topic_id is -1, then is the first post
             if (t.getId() == -1) {
                 t.setTime(new Date());
                 t.setTitle(JForum.getRequest().getParameter("subject"));
 
                 int topicId = tm.addNew(t);
                 t.setId(topicId);
                 fm.incrementTotalTopics(t.getForumId(), 1);
             } 
             else {
                 tm.incrementTotalReplies(t.getId());
                 tm.incrementTotalViews(t.getId());
 
                 // Ok, we have an answer. Time to notify the subscribed users
                 if (SystemGlobals.getBoolValue(ConfigKeys.MAIL_NOTIFY_ANSWERS)) {
                     try {
                         ArrayList usersToNotify = tm.notifyUsers(t);
                         
                         // we only have to send an email if there are users subscribed to the topic
                         if (usersToNotify != null && usersToNotify.size() > 0) {
                             QueuedExecutor.getInstance().execute(
                                     new EmailSenderTask(new TopicSpammer(t, usersToNotify)));
                         }
                     } 
                     catch (Exception e) {
                         logger.warn("Error while sending notification emails: " + e);
                     }
                 }
             }
 
             // Topic watch
             if (JForum.getRequest().getParameter("notify") != null) {
                 this.watch(tm, t.getId(), u.getId());
             }
 
             p.setTopicId(t.getId());
 
             // Save the remaining stuff
             int postId = pm.addNew(p);
 
             if (JForum.getRequest().getParameter("topic_id") == null) {
                 t.setFirstPostId(postId);
             }
 
             t.setLastPostId(postId);
             tm.update(t);
 
             fm.setLastPost(t.getForumId(), postId);
 
             ForumRepository.reloadForum(t.getForumId());
             TopicRepository.clearCache(t.getForumId());
 
             String path = JForum.getRequest().getContextPath() + "/posts/list/";
 
             String start = JForum.getRequest().getParameter("start");
             if (start != null && !start.equals("") && !start.equals("0")) {
                 path += this.startPage(t, Integer.parseInt(start)) + "/";
             }
 
             path += t.getId() + SystemGlobals.getValue(ConfigKeys.SERVLET_EXTENSION) + "#" + postId;
 
             JForum.setRedirect(path);
             ((HashMap) SessionFacade.getAttribute("topics_tracking")).put(new Integer(t.getId()),
                     new Long(p.getTime().getTime()));
         } 
         else {
             JForum.getContext().put("preview", true);
             JForum.getContext().put("post", p);
             JForum.getContext().put("topic", t);
             JForum.getContext().put("start", JForum.getRequest().getParameter("start"));
 
             Post postPreview = new Post(p);
             JForum.getContext().put("postPreview", PostCommon.preparePostForDisplay(postPreview));
 
             this.insert();
         }
     }
 
     private int startPage(Topic t, int currentStart) {
         int postsPerPage = SystemGlobals.getIntValue(ConfigKeys.POST_PER_PAGE);
 
         int newStart = (((t.getTotalReplies() + 1) / postsPerPage) * postsPerPage);
         if (newStart > currentStart) {
             return newStart;
         } else {
             return currentStart;
         }
     }
 
     public void delete() throws Exception {
         if (!SecurityRepository.canAccess(SecurityConstants.PERM_MODERATION_POST_REMOVE)) {
             JForum.getContext().put("moduleAction", "message.htm");
             JForum.getContext().put("message", I18n.getMessage("CannotRemovePost"));
 
             return;
         }
 
         // Post
         PostModel pm = DataAccessDriver.getInstance().newPostModel();
         Post p = pm.selectById(Integer.parseInt(JForum.getRequest().getParameter("post_id")));
 
         TopicModel tm = DataAccessDriver.getInstance().newTopicModel();
         Topic t = tm.selectById(p.getTopicId());
 
         if (!TopicsCommon.isTopicAccessible(t.getForumId())) {
             return;
         }
 
         if (p.getId() == 0) {
             this.postNotFound();
             return;
         }
 
         pm.delete(p);
 
         // Topic
         tm.decrementTotalReplies(p.getTopicId());
 
         int maxPostId = tm.getMaxPostId(p.getTopicId());
         if (maxPostId > -1) {
             tm.setLastPostId(p.getTopicId(), maxPostId);
         }
 
         // Forum
         ForumModel fm = DataAccessDriver.getInstance().newForumModel();
 
         maxPostId = fm.getMaxPostId(p.getForumId());
         if (maxPostId > -1) {
             fm.setLastPost(p.getForumId(), maxPostId);
         }
 
         // It was the last remaining post in the topic?
         int totalPosts = tm.getTotalPosts(p.getTopicId());
         if (totalPosts > 0) {
             String page = JForum.getRequest().getParameter("start");
             String returnPath = JForum.getRequest().getContextPath() + "/posts/list/";
 
             if (page != null && !page.equals("") && !page.equals("0")) {
                 int postsPerPage = SystemGlobals.getIntValue(ConfigKeys.POST_PER_PAGE);
                 int newPage = Integer.parseInt(page);
 
                 if (totalPosts % postsPerPage == 0) {
                     newPage -= postsPerPage;
                 }
 
                 returnPath += newPage + "/";
             }
 
             JForum.setRedirect(returnPath + p.getTopicId()
                     + SystemGlobals.getValue(ConfigKeys.SERVLET_EXTENSION));
         } else {
             // Ok, all posts were removed. Time to say goodbye
             Topic topic = new Topic();
             topic.setId(p.getTopicId());
             tm.delete(topic);
 
             tm.removeSubscriptionByTopic(p.getTopicId());
 
             fm.decrementTotalTopics(p.getForumId(), 1);
 
             JForum.setRedirect(JForum.getRequest().getContextPath() + "/forums/show/"
                     + p.getForumId() + SystemGlobals.getValue(ConfigKeys.SERVLET_EXTENSION));
         }
 
         ForumRepository.reloadForum(p.getForumId());
         TopicRepository.clearCache(p.getForumId());
     }
 
     private void watch(TopicModel tm, int topicId, int userId) throws Exception {
         if (!tm.isUserSubscribed(topicId, userId)) {
             tm.subscribeUser(topicId, userId);
         }
     }
 
     public void watch() throws Exception {
         int topicId = Integer.parseInt(JForum.getRequest().getParameter("topic_id"));
         int userId = SessionFacade.getUserSession().getUserId();
 
         this.watch(DataAccessDriver.getInstance().newTopicModel(), topicId, userId);
         this.list();
     }
 
     public void unwatch() throws Exception {
         if (this.isUserLogged()) {
             int topicId = Integer.parseInt(JForum.getRequest().getParameter("topic_id"));
             int userId = SessionFacade.getUserSession().getUserId();
             String start = JForum.getRequest().getParameter("start");
 
             DataAccessDriver.getInstance().newTopicModel().removeSubscription(topicId, userId);
 
             String returnPath = JForum.getRequest().getContextPath() + "/posts/list/";
             if (start != null && !start.equals("")) {
                 returnPath += start + "/";
             }
 
             returnPath += topicId + SystemGlobals.getValue(ConfigKeys.SERVLET_EXTENSION);
 
             JForum.getContext().put("moduleAction", "message.htm");
             JForum.getContext().put("message",
                     I18n.getMessage("ForumBase.unwatched", new String[] { returnPath }));
         } else {
             ViewCommon.contextToLogin();
         }
     }
 
     private boolean isUserLogged() {
         return (SessionFacade.getAttribute("logged") != null && SessionFacade
                 .getAttribute("logged").equals("1"));
     }
 
     private void topicLocked() {
         JForum.getContext().put("moduleAction", "message.htm");
         JForum.getContext().put("message", I18n.getMessage("PostShow.topicLocked"));
     }
 
     private boolean isForumReadonly(int forumId, boolean isReply) throws Exception {
         if (!SecurityRepository.canAccess(SecurityConstants.PERM_READ_ONLY_FORUMS, Integer
                 .toString(forumId))) {
             if (isReply) {
                 this.list();
             } else {
                 JForum.setRedirect(JForum.getRequest().getContextPath() + "/forums/show/" + forumId
                         + SystemGlobals.getValue(ConfigKeys.SERVLET_EXTENSION));
             }
 
             return true;
         }
 
         return false;
     }
 
     private boolean anonymousPost(int forumId) {
         // Check if anonymous posts are allowed
         if (!this.isUserLogged()
                 && !SecurityRepository.canAccess(SecurityConstants.PERM_ANONYMOUS_POST, Integer
                         .toString(forumId))) {
             ViewCommon.contextToLogin();
 
             return false;
         }
 
         return true;
     }
 }
