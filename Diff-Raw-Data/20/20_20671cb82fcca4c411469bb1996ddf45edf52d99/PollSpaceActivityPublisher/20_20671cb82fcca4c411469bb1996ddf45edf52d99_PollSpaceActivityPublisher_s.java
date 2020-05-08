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
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.exoplatform.container.ExoContainerContext;
 import org.exoplatform.poll.service.Poll;
 import org.exoplatform.poll.service.Poll.PollAction;
 import org.exoplatform.poll.service.PollEventListener;
 import org.exoplatform.poll.service.PollService;
 import org.exoplatform.poll.service.Utils;
 import org.exoplatform.services.log.ExoLogger;
 import org.exoplatform.services.log.Log;
 import org.exoplatform.social.core.BaseActivityProcessorPlugin;
 import org.exoplatform.social.core.activity.model.ExoSocialActivity;
 import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
 import org.exoplatform.social.core.identity.model.Identity;
 import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
 import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
 import org.exoplatform.social.core.manager.ActivityManager;
 import org.exoplatform.social.core.manager.IdentityManager;
 import org.exoplatform.social.core.processor.I18NActivityUtils;
 
 public class PollSpaceActivityPublisher extends PollEventListener{
 
   public static final String POLL_APP_ID          = "ks-poll:spaces";
   
   public static final String POLL_COMMENT_APP_ID  = "poll:spaces";
   
   private static final Log   LOG                  = ExoLogger.getExoLogger(PollSpaceActivityPublisher.class);
 
   public static final String POLL_LINK_KEY        = "PollLink";
   
   public static final String UPDATE_POLL_TITLE_ID   = "update_poll";
   
   public static final String VOTE_POLL_TITLE_ID   = "vote_poll";
   
   public static final String VOTE_AGAIN_POLL_TITLE_ID = "vote_again_poll";
   
   public static final String VOTE_VALUE           = "VOTE_VALUE";
   
   private ExoSocialActivity activity(Identity author, String title, String body, Map<String, String> templateParams) throws Exception {
     ExoSocialActivity activity = new ExoSocialActivityImpl();
     activity.setUserId(author.getId());
     activity.setTitle(title);
     activity.setBody(body);
     activity.setType(POLL_APP_ID);
     activity.setTemplateParams(templateParams);
     return activity;
   }
   
   private void savePollForActivity(Poll poll) {
     PollService pollService = (PollService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(PollService.class);
     IdentityManager identityManager = (IdentityManager) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(IdentityManager.class);
     try {
       Identity pollOwnerIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, poll.getOwner(), false);
       Map<String, String> templateParams = new HashMap<String, String>();
       String pollPath = poll.getParentPath()+"/"+poll.getId();
       String activityId = pollService.getActivityIdForOwner(pollPath);
       if (activityId != null) {
         ExoSocialActivity activity = getManager().getActivity(activityId);
         if (activity != null) {
           poll.setInfoVote();
           activity.setBody(Utils.getInfoVote(poll));
           activity.setTitle(poll.getQuestion());
           ExoSocialActivityImpl comment = new ExoSocialActivityImpl();
           String currentName = Utils.getCurrentUserVote(poll);
           if (currentName.equals(poll.getOwner())) {
             comment.setUserId(pollOwnerIdentity.getId());
             comment.setTitle(poll.getPollAction().getMessage(Utils.getUserVote(poll, poll.getOwner())));
             templateParams.put(VOTE_VALUE, Utils.getUserVote(poll, poll.getOwner()));
           } else {
             Identity currentIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, currentName, false);
             comment.setUserId(currentIdentity.getId());
             comment.setTitle(poll.getPollAction().getMessage(Utils.getUserVote(poll,currentName)));
             templateParams.put(VOTE_VALUE, Utils.getUserVote(poll, currentName));
           }
           if (poll.getPollAction().equals(PollAction.Update_Poll)) {
             comment.setTitleId(UPDATE_POLL_TITLE_ID);
           }
           if (poll.getPollAction().equals(PollAction.Vote_Poll)) {
             comment.setTitleId(VOTE_POLL_TITLE_ID);
           } 
           if (poll.getPollAction().equals(PollAction.Vote_Again_Poll)) {
             comment.setTitleId(VOTE_AGAIN_POLL_TITLE_ID);
           }
           templateParams.put(BaseActivityProcessorPlugin.TEMPLATE_PARAM_TO_PROCESS, VOTE_VALUE);
           comment.setType(POLL_COMMENT_APP_ID);
           comment.setTemplateParams(templateParams);
           getManager().updateActivity(activity);
           getManager().saveComment(activity, comment);
          if (PollAction.Update_Poll.equals(poll.getPollAction())) {
            saveCommentToTopicActivity(poll, comment.getTitle(), "forum.update-poll");
          }
         } else {
           activityId = null;
           poll.setInfoVote();
         }
       }
       if (activityId == null) {
         ExoSocialActivity newActivity = activity(pollOwnerIdentity, poll.getQuestion(), Utils.getInfoVote(poll), templateParams);
         
         //set stream owner
         Identity spaceIdentity = getSpaceIdentity(poll);
         if (spaceIdentity != null) {
          pollOwnerIdentity =  spaceIdentity;
         }
         templateParams.put(POLL_LINK_KEY, poll.getLink());
         getManager().saveActivityNoReturn(pollOwnerIdentity, newActivity);
         
         newActivity.setTemplateParams(templateParams);
         
         if (pollService.getActivityIdForOwner(pollPath) == null) {
           saveCommentToTopicActivity(poll, "A poll has been added to the topic.", "forum.add-poll");
         }
         pollService.saveActivityIdForOwner(pollPath, newActivity.getId());
       }
     } catch (Exception e) {
       LOG.error("Can not record Activity for space when create poll ", e.getMessage());
     }
   }
   
   public void saveCommentToTopicActivity(Poll poll, String title, String titleId) {
     PollService pollService = (PollService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(PollService.class);
     IdentityManager identityManager = (IdentityManager) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(IdentityManager.class);
     String topicActivityId = pollService.getActivityIdForOwner(poll.getParentPath());
     if (topicActivityId != null) {
       ExoSocialActivity topicActivity = getManager().getActivity(topicActivityId);
       if (poll.isInTopic() && topicActivity != null) {
         ExoSocialActivityImpl comment = new ExoSocialActivityImpl();
         Identity pollOwnerIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, poll.getOwner(), false);
         comment.setUserId(pollOwnerIdentity.getId());
         comment.setType("ks-forum:spaces");
         comment.setTitle(title);
         I18NActivityUtils.addResourceKey(comment, titleId, null);
         getManager().saveComment(topicActivity, comment);
       }
     }
   }
   
   public void savePoll(Poll poll) {
     savePollForActivity(poll);
   }
   
   public void closePoll(Poll poll) {
     saveCommentToTopicActivity(poll, "Poll has been closed.", "forum.close-poll");
   }
   
   public void pollRemove(String pollId) {
     try {
       PollService pollService = (PollService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(PollService.class);
       Poll poll = pollService.getPoll(pollId);
       String pollPath = poll.getParentPath()+"/"+poll.getId();
       String activityId = pollService.getActivityIdForOwner(pollPath);
       ExoSocialActivity activity = getManager().getActivity(activityId);
       getManager().deleteActivity(activity);
       saveCommentToTopicActivity(poll, "Poll has been removed.", "forum.remove-poll");
     } catch (Exception e) {
       LOG.error("Fail to remove poll "+e.getMessage());
     }
   }
   
   private ActivityManager getManager() {
     return (ActivityManager) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(ActivityManager.class);
   }
   
   private Identity getSpaceIdentity(Poll poll) {
     IdentityManager identityM = (IdentityManager) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(IdentityManager.class);
     String path = poll.getParentPath();
     String spaceName = "";
     if (path.contains("forumCategoryspaces")) {
       String[] tab = path.split("/");
       spaceName = tab[tab.length-2].replace("forumSpace", "");
     }
     if ("".equals(spaceName)) {
       return null;
     } else {
       return identityM.getOrCreateIdentity(SpaceIdentityProvider.NAME, spaceName, false);
     }
   }
   
 }
