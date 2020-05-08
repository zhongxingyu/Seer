 /*
  * Copyright (C) 2003-2013 eXo Platform SAS.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 package org.exoplatform.forum.ext.activity;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.commons.lang.StringEscapeUtils;
 import org.exoplatform.forum.service.Post;
 import org.exoplatform.forum.service.Topic;
 import org.exoplatform.social.core.activity.model.ExoSocialActivity;
 import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
 
 /**
  * Created by The eXo Platform SAS
  * Author : thanh_vucong
  *          thanh_vucong@exoplatform.com
  * Jan 10, 2013  
  */
 public class ForumActivityBuilder {
 
   public static final String FORUM_ACTIVITY_TYPE = "ks-forum:spaces";
 
   public static final String FORUM_ID_KEY      = "ForumId";
 
   public static final String CATE_ID_KEY       = "CateId";
 
   public static final String POST_TYPE         = "Post";
 
   public static final String POST_ID_KEY       = "PostId";
 
   public static final String POST_OWNER_KEY    = "PostOwner";
 
   public static final String POST_LINK_KEY     = "PostLink";
 
   public static final String POST_NAME_KEY     = "PostName";
 
   public static final String TOPIC_ID_KEY      = "TopicId";
 
   public static final String TOPIC_OWNER_KEY   = "TopicOwner";
 
   public static final String TOPIC_LINK_KEY    = "TopicLink";

  public static final String TOPIC_NAME_KEY    = "TopicName";
   
   public static final String TOPIC_POST_COUNT_KEY    = "NumberOfReplies";
   
   public static final String TOPIC_VOTE_RATE_KEY    = "TopicVoteRate";
   
   private ForumActivityBuilder() {
     
   }
   
   public static ExoSocialActivity createActivityComment(Post post, ForumActivityContext ctx) {
     ExoSocialActivity activity = new ExoSocialActivityImpl();
     String body = getThreeFirstLines(post);
 
     //activity.setUserId(post.getOwner());
     String title = StringEscapeUtils.unescapeHtml(post.getMessage());
     activity.setTitle(title);
     activity.setBody(body);
     activity.isComment(true);
     activity.setType(FORUM_ACTIVITY_TYPE);
     
     //activity.setTitleId(title); => Resource Bundle Key
     
     //
     Map<String, String> templateParams = new HashMap<String, String>();
     
     templateParams.put(POST_ID_KEY, post.getId());
     templateParams.put(POST_LINK_KEY, post.getLink());
     templateParams.put(POST_NAME_KEY, post.getName());
     templateParams.put(POST_OWNER_KEY, post.getOwner());
     //
     templateParams.put(FORUM_ID_KEY, post.getForumId());
     templateParams.put(CATE_ID_KEY, post.getCategoryId());
     templateParams.put(TOPIC_ID_KEY, post.getTopicId());
     activity.setTemplateParams(templateParams);
     return activity;
   }
   
   public static ExoSocialActivity createActivityComment(Topic topic, ForumActivityContext ctx) {
     ExoSocialActivity activity = new ExoSocialActivityImpl();
     String body = getThreeFirstLines(topic);
     
     //activity.setUserId(topic.getOwner());
     String title = StringEscapeUtils.unescapeHtml(topic.getTopicName());
     activity.setTitle(title);
     activity.setBody(body);
     activity.isComment(true);
     activity.setType(FORUM_ACTIVITY_TYPE);
 
     return activity;
   }
   
   public static String getThreeFirstLines(Topic topic) {
     return topic.getDescription();
   }
   
   public static String getThreeFirstLines(Post post) {
     return post.getMessage();
   }
   
   public static ExoSocialActivity createActivity(Topic topic, ForumActivityContext ctx) {
     ExoSocialActivity activity = new ExoSocialActivityImpl();
     String body = getThreeFirstLines(topic);
     
     
     //processing in execute of task.
     //avoid get Identity here to write UT
     //activity.setUserId(topic.getOwner());
     String title = StringEscapeUtils.unescapeHtml(topic.getTopicName());
     activity.setTitle(title);
     activity.setBody(body);
     activity.isComment(false);
     activity.isHidden(false);
     activity.setType(FORUM_ACTIVITY_TYPE);
     
     //
     Map<String, String> templateParams = new HashMap<String, String>();
     
     templateParams.put(TOPIC_POST_COUNT_KEY, "" + topic.getPostCount());
     templateParams.put(TOPIC_VOTE_RATE_KEY, "" + topic.getVoteRating());
     templateParams.put(TOPIC_ID_KEY, topic.getId());
     templateParams.put(TOPIC_LINK_KEY, topic.getLink());
    templateParams.put(TOPIC_NAME_KEY, topic.getTopicName());
     templateParams.put(TOPIC_OWNER_KEY, topic.getOwner());
     
     //
     templateParams.put(FORUM_ID_KEY, topic.getForumId());
     templateParams.put(CATE_ID_KEY, topic.getCategoryId());
     activity.setTemplateParams(templateParams);
     return activity;
   }
   
   public static ExoSocialActivity updateNumberOfReplies(Topic topic, ExoSocialActivity activity) {
     //
     Map<String, String> templateParams = activity.getTemplateParams();
     
     templateParams.put(TOPIC_POST_COUNT_KEY, "" + topic.getPostCount());
     return activity;
   }
   
   public static ExoSocialActivity updateVoteRate(Topic topic, ExoSocialActivity activity) {
     //
     Map<String, String> templateParams = activity.getTemplateParams();
     templateParams.put(TOPIC_VOTE_RATE_KEY, "" + topic.getVoteRating());
     return activity;
   }
   
 }
