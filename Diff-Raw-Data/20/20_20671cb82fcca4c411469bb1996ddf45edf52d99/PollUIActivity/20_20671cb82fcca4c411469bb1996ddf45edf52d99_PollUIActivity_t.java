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
 package org.exoplatform.forum.ext.impl;
 
 import org.apache.commons.lang.ArrayUtils;
 import org.exoplatform.social.webui.activity.BaseUIActivity;
 import org.exoplatform.webui.config.annotation.ComponentConfig;
 import org.exoplatform.webui.config.annotation.EventConfig;
 import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
 
 @ComponentConfig(lifecycle = UIFormLifecycle.class, template = "classpath:groovy/forum/social-integration/plugin/space/PollUIActivity.gtmpl", events = {
   @EventConfig(listeners = BaseUIActivity.ToggleDisplayLikesActionListener.class),
   @EventConfig(listeners = BaseUIActivity.ToggleDisplayCommentFormActionListener.class),
   @EventConfig(listeners = BaseUIActivity.LikeActivityActionListener.class),
   @EventConfig(listeners = BaseUIActivity.SetCommentListStatusActionListener.class),
   @EventConfig(listeners = BaseUIActivity.PostCommentActionListener.class),
   @EventConfig(listeners = BaseUIActivity.DeleteActivityActionListener.class, confirm = "UIActivity.msg.Are_You_Sure_To_Delete_This_Activity"),
   @EventConfig(listeners = BaseUIActivity.DeleteCommentActionListener.class, confirm = "UIActivity.msg.Are_You_Sure_To_Delete_This_Comment") })
 public class PollUIActivity extends BaseKSActivity {
 
   @SuppressWarnings("unused")
   private String[] getVotes(String infoVote) {
     String[] tab = infoVote.split("\\|");
     tab = (String[]) ArrayUtils.removeElement(tab, tab[tab.length-1]);
     return tab;
   }
   
   @SuppressWarnings("unused")
   private String getNumberOfVotes(String infoVote) {
     String[] tab = infoVote.split("\\|");
     return tab[tab.length-1];
   }
   
   @SuppressWarnings("unused")
   private String getLink() {
     return getActivityParamValue(PollSpaceActivityPublisher.POLL_LINK_KEY);
   }
   
  @SuppressWarnings("unused")
  private String getSpacePrettyName() {
    return getActivityParamValue(PollSpaceActivityPublisher.SPACE_PRETTY_NAME);
  }
  
 }
