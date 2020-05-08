 package org.exoplatform.ks.ext.impl;
 
 
import org.exoplatform.social.webui.activity.BaseUIActivity;
 import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
 import org.exoplatform.webui.core.lifecycle.WebuiBindingContext;
 
 @ComponentConfig (
    lifecycle = UIFormLifecycle.class,
    template = "classpath:groovy/ks/social-integration/plugin/space/WikiUIActivity.gtmpl",
    events = {
        @EventConfig(listeners = BaseUIActivity.ToggleDisplayLikesActionListener.class),
        @EventConfig(listeners = BaseUIActivity.ToggleDisplayCommentFormActionListener.class),
        @EventConfig(listeners = BaseUIActivity.LikeActivityActionListener.class),
        @EventConfig(listeners = BaseUIActivity.SetCommentListStatusActionListener.class),
        @EventConfig(listeners = BaseUIActivity.PostCommentActionListener.class),
        @EventConfig(listeners = BaseUIActivity.DeleteActivityActionListener.class, confirm = "UIActivity.msg.Are_You_Sure_To_Delete_This_Activity"),
        @EventConfig(listeners = BaseUIActivity.DeleteCommentActionListener.class, confirm = "UIActivity.msg.Are_You_Sure_To_Delete_This_Comment") 
      }
 )
 public class WikiUIActivity extends BaseKSActivity {
 
   public WikiUIActivity() {
   }
   
   String getActivityMessage(WebuiBindingContext _ctx) throws Exception {
     String activityType = getActivityParamValue(WikiSpaceActivityPublisher.ACTIVITY_TYPE_KEY);
     if (activityType.equalsIgnoreCase(WikiSpaceActivityPublisher.ADD_PAGE_TYPE)) {
       return _ctx.appRes("WikiUIActivity.label.page-create");
     } else if (WikiSpaceActivityPublisher.UPDATE_PAGE_TYPE.equalsIgnoreCase(activityType)) {
       return _ctx.appRes("WikiUIActivity.label.page-update");
     }
     return "";
   }
 
   String getPageName() {
     return getActivityParamValue(WikiSpaceActivityPublisher.PAGE_TITLE_KEY);
   }
 
   String getPageURL() {
     return getActivityParamValue(WikiSpaceActivityPublisher.URL_KEY);
   }
   
   String getPageExcerpt(){
     return getActivityParamValue(WikiSpaceActivityPublisher.PAGE_EXCERPT);
   }
 
 }
