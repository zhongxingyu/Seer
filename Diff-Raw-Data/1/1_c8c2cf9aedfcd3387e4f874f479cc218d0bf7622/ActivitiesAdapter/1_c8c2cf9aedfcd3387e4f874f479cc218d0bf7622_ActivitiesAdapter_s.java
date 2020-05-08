 package com.home.giraffe.ui;
 
 import android.content.Context;
 import android.text.Html;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.*;
 import com.actionbarsherlock.app.ActionBar;
 import com.home.giraffe.R;
 import com.home.giraffe.interfaces.IImageLoader;
 import com.home.giraffe.objects.*;
 import com.home.giraffe.storages.ObjectsStorage;
 import roboguice.RoboGuice;
 
 import java.util.List;
 
 public class ActivitiesAdapter extends ArrayAdapter<ActivityItem> {
     IImageLoader mImageLoader;
     ObjectsStorage mObjectsStorage;
 
     private List<ActivityItem> mItems;
 
     public ActivitiesAdapter(Context context, int textViewResourceId, List<ActivityItem> objects) {
         super(context, textViewResourceId, objects);
         mItems = objects;
         mImageLoader = RoboGuice.getInjector(context).getProvider(IImageLoader.class).get();
         mObjectsStorage = RoboGuice.getInjector(context).getProvider(ObjectsStorage.class).get();
     }
 
     @Override
     public View getView(int position, View convertView, ViewGroup parent) {
         ActivityItem item = mItems.get(position);
         switch (item.getType()) {
             case Discussion:
             case File:
             case Poll:
             case Document:
                 return getPostView(item);
             case Promotion:
                 break;
             case Like:
                 break;
         }
 
         return getUnknownView();
     }
 
     private View getUnknownView() {
         return LayoutInflater.from(getContext()).inflate(R.layout.unknown_object, null);
     }
 
     private View getProjectView(BaseObject object) {
         return getUnknownView();
     }
 
     private View getSpaceView(BaseObject object) {
         return getUnknownView();
     }
 
     private View getFileView(BaseObject object) {
         return getUnknownView();
     }
 
     private View getPostView(ActivityItem activityItem) {
         View view = LayoutInflater.from(getContext()).inflate(R.layout.post_object, null);
 
         Post post = (Post)mObjectsStorage.get(activityItem.getId());
         Actor actor = (Actor)mObjectsStorage.get(post.getActorId());
 
         ImageView icon = (ImageView) view.findViewById(R.id.icon);
         if(/*activityItem.isQuestion() TODO*/true){
             icon.setImageResource(R.drawable.ic_question_discussion);
         }
         else{
             icon.setImageResource(R.drawable.ic_discussion);
         }
 
         TextView userDisplayName = (TextView) view.findViewById(R.id.userDisplayName);
         userDisplayName.setText(actor.getDisplayName());
 
         TextView title = (TextView) view.findViewById(R.id.title);
         title.setText(post.getTitle());
 
         TextView content = (TextView) view.findViewById(R.id.content);
         content.setText(post.getContent());
 
         ImageView avatar = (ImageView) view.findViewById(R.id.avatar);
         mImageLoader.DisplayImage(actor.getAvatarUrl(), avatar);
 
         TextView replies = (TextView) view.findViewById(R.id.replies);
         replies.setText(Integer.toString(post.getReplyCount()));
 
         TextView likes = (TextView) view.findViewById(R.id.likes);
         likes.setText(Integer.toString(post.getLikeCount()));
 
         if(!post.getCommentIds().isEmpty())
             addComments(post, view);
 
         return view;
     }
 
     private void addComments(Post post, View postView) {
         final RelativeLayout comments_layout = (RelativeLayout)postView.findViewById(R.id.comments_layout);
         final LinearLayout comments = (LinearLayout)postView.findViewById(R.id.comments);
         final ImageView arrow = (ImageView)postView.findViewById(R.id.imageArrow);
 
         for (String commentId : post.getCommentIds()){
             Comment comment = (Comment) mObjectsStorage.get(commentId);
             Actor actor = (Actor)mObjectsStorage.get(comment.getActorId());
 
             LinearLayout commentView = (LinearLayout)LayoutInflater.from(getContext()).inflate(R.layout.comment_object, comments, false);
 
             TextView content = (TextView)commentView.findViewById(R.id.content);
             content.setText(Html.fromHtml("<b>" + actor.getDisplayName() + "</b>" + " " + comment.getContent()));
 
             ImageView avatar = (ImageView) commentView.findViewById(R.id.avatar);
             mImageLoader.DisplayImage(actor.getAvatarUrl(), avatar);
 
             comments.addView(commentView);
         }
 
         comments_layout.setVisibility(View.VISIBLE);
         comments_layout.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 if(comments.getVisibility() == View.VISIBLE){
                     comments.setVisibility(View.GONE);
                     arrow.setBackgroundResource(R.drawable.ic_arrow_down);
                 }
                 else{
                     comments.setVisibility(View.VISIBLE);
                     arrow.setBackgroundResource(R.drawable.ic_arrow_up);
                 }
             }
         });
     }
 
     private View getPersonView(BaseObject object) {
         return getUnknownView();
     }
 
     private View getGroupView(BaseObject object) {
         return getUnknownView();
     }
 
     private View getInstanceView(BaseObject object) {
         return getUnknownView();
     }
 
     private View getMessageView(BaseObject object) {
         return getUnknownView();
     }
 }
