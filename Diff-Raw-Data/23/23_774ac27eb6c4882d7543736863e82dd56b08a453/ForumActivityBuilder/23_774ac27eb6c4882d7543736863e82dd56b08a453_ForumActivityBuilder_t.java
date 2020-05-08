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
 import org.exoplatform.forum.common.CommonUtils;
 import org.exoplatform.forum.common.TransformHTML;
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
 
   public static final String TOPIC_POST_COUNT_KEY    = "NumberOfReplies";
   
   public static final String TOPIC_VOTE_RATE_KEY    = "TopicVoteRate";
 
   private static final int NUMBER_CHARS    = 430;
   private static final int NUMBER_OF_LINES    = 4;
   
   public static final String SPACE_GROUP_ID  = "SpaceGroupId";
   
   private ForumActivityBuilder() {
     
   }
   
   public static ExoSocialActivity createActivityComment(Post post, ForumActivityContext ctx) {
     ExoSocialActivity activity = new ExoSocialActivityImpl();
     String title = getFourFirstLines(post.getMessage());
 
     //activity.setUserId(post.getOwner());
     activity.setTitle(title);
     activity.setBody(post.getMessage());
     activity.isComment(true);
     activity.setType(FORUM_ACTIVITY_TYPE);
     
     //activity.setTitleId(title); => Resource Bundle Key
     
     //
     Map<String, String> templateParams = new HashMap<String, String>();
     
     templateParams.put(POST_ID_KEY, post.getId());
     templateParams.put(POST_LINK_KEY, post.getLink() + "/" + post.getId());
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
     String body = getFourFirstLines(topic.getDescription());
     
     //activity.setUserId(topic.getOwner());
     String title = StringEscapeUtils.unescapeHtml(topic.getTopicName());
     activity.setTitle(title);
     activity.setBody(body);
     activity.isComment(true);
     activity.setType(FORUM_ACTIVITY_TYPE);
 
     return activity;
   }
   
   public static String getFourFirstLines(String str) {
    str = CommonUtils.decodeSpecialCharToHTMLnumber(str);
     return getNumberFirstLines(str.replaceAll("&nbsp;", ""), 4);
   }
   
   /**
    * No more than 4 lines
    * No more than 430 characters
    * If the content is larger than these limits, we add "..." at the end of the abstract.
    * @param content
    * @param line
    * @return
    */
   public static String getNumberFirstLines(String content, int line) {
     String[] tab = TransformHTML.getPlainText(content).replaceAll("(?m)^\\s*$[\n\r]{1,}", "").split("\\r?\\n");
     
     //
     int numberOfLine = Math.min(NUMBER_OF_LINES, tab.length);
     StringBuilder sb = new StringBuilder();
     
     //
     for (int i=0; i<numberOfLine; i++) {
       sb.append(StringEscapeUtils.unescapeHtml(tab[i]));
       
       //
       if(i + 1 < numberOfLine) {
         sb.append("<br/>");
       }
     }
     
     //
     return trunc(sb.toString(), NUMBER_CHARS, tab.length > NUMBER_OF_LINES);
   }
   
   /**
    * Truncates large Strings showing a portion of the String's head and tail
    * with the head cut out and replaced with '...'.
    * 
    * @param str
    *            the string to truncate
    * @param head
    *            the amount of the head to display
    * @return the head truncated string
    */
   public static final String trunc(String str, int head, boolean needTail) {
     StringBuffer buf = null;
 
     // Return as-is if String is smaller than or equal to the head plus the
     // tail plus the number of characters added to the trunc representation
     // plus the number of digits in the string length.
     buf = new StringBuffer();
     
     if (str.length() <= (head + 7 + str.length() / 10)) {
       buf.append(str);
       if (needTail) {
         buf.append("...");
       }
       
       return buf.toString();
     }
 
     //
     buf.append(str.substring(0, head)).append("...");
     return buf.toString();
   }
   
   public static ExoSocialActivity createActivity(Topic topic, ForumActivityContext ctx) {
     ExoSocialActivity activity = new ExoSocialActivityImpl();
     String body = getFourFirstLines(topic.getDescription());
     
     
     //processing in execute of task.
     //avoid get Identity here to write UT
     //activity.setUserId(topic.getOwner());
     activity.setTitle(CommonUtils.decodeSpecialCharToHTMLnumber(topic.getTopicName()));
     activity.setBody(body);
     activity.isComment(false);
     activity.isHidden(false);
     activity.setType(FORUM_ACTIVITY_TYPE);
     
     //
     Map<String, String> templateParams = new HashMap<String, String>();
     
     templateParams.put(TOPIC_POST_COUNT_KEY, "" + topic.getPostCount());
     templateParams.put(TOPIC_VOTE_RATE_KEY, "" + topic.getVoteRating());
     templateParams.put(TOPIC_ID_KEY, topic.getId());
     templateParams.put(TOPIC_OWNER_KEY, topic.getOwner());
     
     //
     templateParams.put(FORUM_ID_KEY, topic.getForumId());
     templateParams.put(CATE_ID_KEY, topic.getCategoryId());
     
     if (ForumActivityUtils.hasSpace(topic.getForumId())) {
       templateParams.put(SPACE_GROUP_ID, ForumActivityUtils.getSpaceGroupId(topic.getForumId()));
     }
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
