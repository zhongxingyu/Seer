 package com.home.giraffe.tasks;
 
 import android.net.Uri;
 import android.support.v4.app.FragmentActivity;
 import android.text.TextUtils;
 import com.home.giraffe.Constants;
 import com.home.giraffe.network.HttpResponse;
 import com.home.giraffe.network.NetworkUtils;
 import com.home.giraffe.objects.Actor;
 import com.home.giraffe.objects.BaseObject;
 import com.home.giraffe.objects.Comment;
 import com.home.giraffe.objects.Jive.*;
 import com.home.giraffe.objects.Post;
 import com.home.giraffe.objects.activity.BaseObjectContainer;
 import com.home.giraffe.objects.socialnews.CreatedSocialNewsItem;
 import com.home.giraffe.objects.socialnews.JoinedSocialNewsItem;
 import com.home.giraffe.objects.socialnews.LevelSocialNewsItem;
 import com.home.giraffe.objects.socialnews.SocialNews;
 import com.home.giraffe.utils.Utils;
 
 public class GetBaseObjectsListTask extends BaseTaskLoader<BaseObjectContainer> {
     private BaseObjectContainer mBaseObjectContainer;
     private String mUrl;
 
 
     public GetBaseObjectsListTask(FragmentActivity activity, String url) {
         super(activity);
         mUrl = url;
         mBaseObjectContainer = new BaseObjectContainer();
     }
 
     @Override
     public BaseObjectContainer loadInBackground() {
         Utils.d("Started GetBaseObjectsListTask");
 
         try {
             if(mUrl == null)
                 setUrlToHome();
 
             mBaseObjectContainer.setCurrent(mUrl);
             Utils.d("Activities url: " + mUrl);
 
             JiveContainers jiveContainers = mRequestsManager.getJiveContainers(mUrl);
             Utils.d("Received " + jiveContainers.getList().size() + " jive records");
 
             if(jiveContainers.getList().isEmpty()){
                 return mBaseObjectContainer;
             }
 
             if(jiveContainers.getLinks() != null){
                 mBaseObjectContainer.setNext(jiveContainers.getLinks().getNext());
                 mBaseObjectContainer.setPrevious(jiveContainers.getLinks().getPrevious());
             }
 
             for (JiveContainer jiveContainer : jiveContainers.getList()) {
                 processJiveContainer(jiveContainer);
             }
         } catch (Exception e) {
             mUiManager.showError(getActivity(), e);
         }
         finally {
             Utils.d("Finished GetBaseObjectsListTask");
         }
 
         return mBaseObjectContainer;
     }
 
     private void setUrlToHome(){
         try {
             HttpResponse response =
                     mConnector.getRequest(mSettingsManager.getCommunityUrl() +
                             Constants.HOME);
 
             String location = NetworkUtils.getLocationFromHeaders(response.getHeaders());
             Utils.d("Location page: " + location);
 
             if(!TextUtils.isEmpty(location)){
                 Uri uri = Uri.parse(location);
                 String streamSource = uri.getQueryParameter("streamSource");
                 if(streamSource != null){
                     if(streamSource.equalsIgnoreCase(JiveStreamSource.custom.name())){
                         String streamId = uri.getQueryParameter("streamID");
                         if(streamId != null) {
                             mUrl = mSettingsManager.getCommunityUrl() +
                                     String.format(Constants.CUSTOM_STREAM, streamId);
                             return;
                         }
                     }
 
                     Utils.w("Doesn't support %s stream source type", streamSource);
                     // doesn't support Email Watches https://github.com/NevRA/Giraffe/issues/25
                 }
 
                 if (uri.getLastPathSegment().equalsIgnoreCase("actions")){
                     Utils.w("Doesn't support actions page");
                     // doesn't support Action https://github.com/NevRA/Giraffe/issues/26
                 }
 
                 if (uri.getLastPathSegment().equalsIgnoreCase("inbox")){
                     mUrl = mSettingsManager.getCommunityUrl() + Constants.INBOX;
                     return;
                 }
             }
         } catch (Exception e) {
             Utils.e(e);
         }
 
         mUrl = mSettingsManager.getCommunityUrl() + Constants.ALL_ACTIVITIES;
     }
 
     private void processJiveContainer(JiveContainer jiveContainer) throws Exception {
         JiveActor jiveActor = jiveContainer.getActor();
         JiveVerbTypes jiveVerbTypes = jiveContainer.getVerbType();
         JiveObject jiveObject = jiveContainer.getObject();
 
         if(jiveObject == null)
             return; // for some group updates etc.
 
         switch (jiveObject.getType()) {
             case JiveMessage:
                 break;
             case JiveComment:
                 break;
             case JiveDiscussion:
                 break;
             case JiveDocument:
                 break;
             case JiveFile:
                 break;
             case JivePost:
                 break;
             case JivePoll:
                 break;
             case JiveLevel:
                 break;
             case JiveGroup:
                 break;
 
             case Unsupported:
             case JiveIdea:
             case JiveVideo:
             case JiveUpdate:
             case JiveProject:
             case JiveSpace:
             case JiveInstance:
             case JivePerson:
             case JiveTask:
             default:
                 return;
         }
 
         mObjectsStorage.add(new Actor(jiveActor));
 
         Utils.v("Trying to process: %s", jiveObject.getId() != null ?
                 jiveObject.getId() :
                 jiveObject.getDisplayName());
 
         if (        jiveVerbTypes == JiveVerbTypes.JivePromoted) {
             processJivePromotion(jiveContainer);
         }else if (  jiveVerbTypes == JiveVerbTypes.JiveJoined) {
             processJiveJoined(jiveContainer);
         }else if (  jiveVerbTypes == JiveVerbTypes.JiveLiked) {
             processJiveLike(jiveContainer);
         } else if ( jiveVerbTypes == JiveVerbTypes.JiveCompleted) {
             processJiveTask(jiveContainer);
         } else if ( jiveVerbTypes == JiveVerbTypes.JiveCreated &&
                     jiveObject.getType() == JiveTypes.JiveGroup) {
             processJiveCreated(jiveContainer);
         } else if ( jiveVerbTypes == JiveVerbTypes.JiveCreated) {
             processJivePost(jiveContainer);
         } else if ( jiveVerbTypes == JiveVerbTypes.JiveReplied ||
                     jiveVerbTypes == JiveVerbTypes.JiveCommented) {
             processJiveComment(jiveContainer);
         }
     }
 
     private void processJiveComment(JiveContainer jiveContainer) throws Exception {
         JiveObject jiveParent = jiveContainer.getJive().getParent();
         if(jiveParent == null){
             Utils.w("Unsupported parent type"); // bookmarks, etc...
             return;
         }
 
         if(!Utils.isValidPostType(jiveParent.getType())){    // comments available only for posts (document, file, ...)
             Utils.w("Unsupported type \n\turl: %s \n\ttype: %s", jiveParent.getId(), jiveParent.getType());
             return;
         }
 
         Post post = (Post) mObjectsStorage.get(jiveParent.getId());
 
         if (post == null) {
             JivePost jivePost = mRequestsManager.getPost(jiveParent.getId());
             post = Utils.getPostFromObjectType(jivePost.getType());
             post.fromJivePost(jivePost);
         }
 
         Comment comment = new Comment(jiveContainer);
         post.addComment(comment);
 
         addObjectToActivities(post);
     }
 
     private void processJivePost(JiveContainer jiveContainer) throws Exception {
         JiveObject jiveObject = jiveContainer.getObject();
 
         if(!Utils.isValidPostType(jiveObject.getType())){
             Utils.w("Unsupported post type: %s", jiveObject.getType());
             return;
         }
 
         Post post = Utils.getPostFromObjectType(jiveObject.getType());
         post.fromJiveActivityContainer(jiveContainer);
         addObjectToActivities(post);
     }
 
     private void addObjectToStorage(BaseObject object){
         mObjectsStorage.add(object);
     }
 
     private void addObjectToActivities(BaseObject object){
         addObjectToStorage(object);
 
         BaseObject activityItem = mBaseObjectContainer.getActivity(object.getId());
         if (activityItem == null) {
             mBaseObjectContainer.addActivity(object);
         }
     }
 
     private void processJiveTask(JiveContainer jiveContainer) {
     }
 
     private void processJiveLike(JiveContainer jiveContainer) {
     }
 
     private void processJiveCreated(JiveContainer jiveContainer) {
         CreatedSocialNewsItem joinedNews = new CreatedSocialNewsItem(jiveContainer);
 
         SocialNews socialNews = (SocialNews) mBaseObjectContainer.getActivity(Constants.SocialNewsId);
         if(socialNews == null){
             socialNews = new SocialNews();
             mBaseObjectContainer.addActivity(socialNews);
         }
 
         socialNews.addNews(joinedNews);
     }
 
     private void processJiveJoined(JiveContainer jiveContainer) {
         JoinedSocialNewsItem joinedNews = new JoinedSocialNewsItem(jiveContainer);
 
         SocialNews socialNews = (SocialNews) mBaseObjectContainer.getActivity(Constants.SocialNewsId);
         if(socialNews == null){
             socialNews = new SocialNews();
             mBaseObjectContainer.addActivity(socialNews);
         }
 
         socialNews.addNews(joinedNews);
     }
 
     private void processJivePromotion(JiveContainer jiveContainer) {
         LevelSocialNewsItem levelNews = new LevelSocialNewsItem(jiveContainer);
 
         SocialNews socialNews = (SocialNews) mBaseObjectContainer.getActivity(Constants.SocialNewsId);
         if(socialNews == null){
             socialNews = new SocialNews();
             mBaseObjectContainer.addActivity(socialNews);
         }
 
         socialNews.addNews(levelNews);
     }
 }
