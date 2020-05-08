 package org.exoplatform.forum.ext.impl;
 
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.exoplatform.container.PortalContainer;
 import org.exoplatform.forum.ext.activity.ForumActivityBuilder;
 import org.exoplatform.forum.service.DataStorage;
 import org.exoplatform.forum.service.MessageBuilder;
 import org.exoplatform.forum.service.Post;
 import org.exoplatform.forum.service.Topic;
 import org.exoplatform.portal.application.PortalRequestContext;
 import org.exoplatform.portal.webui.util.Util;
 import org.exoplatform.services.log.ExoLogger;
 import org.exoplatform.services.log.Log;
 import org.exoplatform.social.core.activity.model.ExoSocialActivity;
 import org.exoplatform.social.webui.activity.BaseUIActivity;
 import org.exoplatform.webui.application.WebuiRequestContext;
 import org.exoplatform.webui.config.annotation.ComponentConfig;
 import org.exoplatform.webui.config.annotation.EventConfig;
 import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
 import org.exoplatform.webui.core.lifecycle.WebuiBindingContext;
 import org.exoplatform.webui.event.Event;
 import org.exoplatform.webui.form.UIFormTextAreaInput;
 
 @ComponentConfig(lifecycle = UIFormLifecycle.class, template = "classpath:groovy/forum/social-integration/plugin/space/ForumUIActivity.gtmpl", events = {
     @EventConfig(listeners = BaseUIActivity.ToggleDisplayLikesActionListener.class),
     @EventConfig(listeners = BaseUIActivity.ToggleDisplayCommentFormActionListener.class),
     @EventConfig(listeners = BaseUIActivity.LikeActivityActionListener.class),
     @EventConfig(listeners = BaseUIActivity.SetCommentListStatusActionListener.class),
     @EventConfig(listeners = ForumUIActivity.PostCommentActionListener.class),
     @EventConfig(listeners = BaseUIActivity.DeleteActivityActionListener.class, confirm = "UIActivity.msg.Are_You_Sure_To_Delete_This_Activity"),
     @EventConfig(listeners = BaseUIActivity.DeleteCommentActionListener.class, confirm = "UIActivity.msg.Are_You_Sure_To_Delete_This_Comment") })
 public class ForumUIActivity extends BaseKSActivity {
 
   private static final Log LOG = ExoLogger.getLogger(ForumUIActivity.class);
 
   public ForumUIActivity() {
     
   }
 
   /*
    * used by template, see line 201 ForumUIActivity.gtmpl
    */
   @SuppressWarnings("unused")
   private String getReplyLink() {
     String viewLink = getActivityParamValue(ForumActivityBuilder.TOPIC_LINK_KEY);
     
     StringBuffer sb = new StringBuffer(viewLink);
     if (sb.lastIndexOf("/") == -1 || sb.lastIndexOf("/") != sb.length() - 1) {
       sb.append("/");
     }
     // add signal to show reply form
     sb.append("false");
     return sb.toString();
   }
 
   private String getLink(String tagLink, String nameLink) {
     String viewLink = getActivityParamValue(ForumActivityBuilder.TOPIC_LINK_KEY);
     return String.format(tagLink, viewLink, nameLink);
   }
   
   public String getViewLink() {
     String link = getActivityParamValue(ForumActivityBuilder.TOPIC_LINK_KEY);
     return link;
   }
 
   /*
    * used by Template, line 160 ForumUIActivity.gtmpl
    */
   @SuppressWarnings("unused")
   private String getActivityContentTitle(WebuiBindingContext _ctx, String herf) throws Exception {
     String title = getActivity().getTitle();
     String linkTag = "";
     try {
      linkTag = getLink(herf, title);
     } catch (Exception e) { // WebUIBindingContext
       LOG.debug("Failed to get activity content and title ", e);
     }
     return linkTag;
   }
   
   public String getNumberOfReplies() {
     ExoSocialActivity activity = getActivity();
     Map<String, String> templateParams = activity.getTemplateParams();
     
     String got = templateParams.get(ForumActivityBuilder.TOPIC_POST_COUNT_KEY);
     return String.format("%s Replies", got);
   }
   
   public String getRate() {
     ExoSocialActivity activity = getActivity();
     Map<String, String> templateParams = activity.getTemplateParams();
     
     String got = templateParams.get(ForumActivityBuilder.TOPIC_VOTE_RATE_KEY);
     return String.format("Rate: %s", got);
   }
   
   public boolean isTopicActivity() {
     String value = getActivityParamValue(ForumActivityBuilder.TOPIC_ID_KEY);
     if (value != null && value.length() > 0) {
       return true;
     }
     return false;
   }
   
   public void createPost(String message, WebuiRequestContext requestContext) throws Exception {
     
     DataStorage dataStorage = (DataStorage) PortalContainer.getInstance().getComponentInstanceOfType(DataStorage.class);
     String topicId = getActivityParamValue(ForumActivityBuilder.TOPIC_ID_KEY);
     String categoryId = getActivityParamValue(ForumActivityBuilder.CATE_ID_KEY);
     String forumId = getActivityParamValue(ForumActivityBuilder.FORUM_ID_KEY);
     Topic topic = dataStorage.getTopic(categoryId, forumId, topicId, "");
     
     //
     Post post = new Post();
     post.setOwner(requestContext.getRemoteUser());
     post.setIcon("IconsView");
     post.setName("Re: " + topic.getTopicName());
     post.setLink(topic.getLink());
     
     PortalRequestContext context = Util.getPortalRequestContext();
     String remoteAddr = ((HttpServletRequest)context.getRequest()).getRemoteAddr() ;
     
     //getRemoteAddr()
     post.setRemoteAddr(remoteAddr);
 
     post.setModifiedBy(requestContext.getRemoteUser());
     post.setIsApproved(true);
     post.setMessage(message);
     
     dataStorage.savePost(categoryId, forumId, topicId, post, true, new MessageBuilder());
     
     //
     long numberOfReplies = topic.getPostCount() + 1;
     topic.setPostCount(numberOfReplies);
     dataStorage.saveTopic(categoryId, forumId, topic, false, false, new MessageBuilder());
   }
   
   public static class PostCommentActionListener extends BaseUIActivity.PostCommentActionListener {
     @Override
     public void execute(Event<BaseUIActivity> event) throws Exception {
       ForumUIActivity uiActivity = (ForumUIActivity) event.getSource();
       if (uiActivity.isTopicActivity() == false) {
         super.execute(event);
         return;
       }
       
       WebuiRequestContext requestContext = event.getRequestContext();
       UIFormTextAreaInput uiFormComment = uiActivity.getChild(UIFormTextAreaInput.class);
       String message = uiFormComment.getValue();
       uiFormComment.reset();
       
       //
       uiActivity.saveComment(requestContext.getRemoteUser(), message);
       
       //
       uiActivity.createPost(message, requestContext);
 
       uiActivity.setCommentFormFocused(true);
       requestContext.addUIComponentToUpdateByAjax(uiActivity);
 
       uiActivity.getParent().broadcast(event, event.getExecutionPhase());
     }
   }
 
 }
