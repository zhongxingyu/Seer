 package com.tobbetu.en4s;
 
 import java.util.List;
 
 import android.content.Context;
 import android.graphics.Point;
 import android.util.Log;
 import android.view.Display;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.WindowManager;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 import com.tobbetu.en4s.backend.Complaint;
 import com.tobbetu.en4s.backend.Image;
 import com.tobbetu.en4s.backend.Login;
 import com.tobbetu.en4s.backend.User;
 import com.tobbetu.en4s.helpers.BetterAsyncTask;
 import com.tobbetu.en4s.service.EnforceService;
 
 public class BugListAdapter extends ArrayAdapter<Complaint> {
 
     private final String TAG = "BugListAdapter";
     private final Context context;
     private final List<Complaint> complaints;
     private final int tabPosition;
     private Complaint complaint;
     private final User user = Login.getMe();
     private ImageView ivUp;
 
     public BugListAdapter(Context context, List<Complaint> complaints, int pos) {
 
         super(context, R.layout.bug_list_item, complaints);
 
         this.context = context;
         this.complaints = complaints;
         this.tabPosition = pos;
     }
 
     @Override
     public View getView(int position, View convertView, ViewGroup parent) {
 
         ViewHolder holder;
 
         if (position == complaints.size() - 1) {
             loadMore();
         }
 
         if (convertView == null) {
             LayoutInflater inflater = (LayoutInflater) context
                     .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
             convertView = inflater.inflate(R.layout.bug_list_item, parent,
                     false);
 
             holder = new ViewHolder();
             holder.problemImage = (ImageView) convertView
                     .findViewById(R.id.ivProblemImage);
             holder.complaintTitle = (TextView) convertView
                     .findViewById(R.id.tvItem);
             holder.tvAdditionalInfo = (TextView) convertView
                     .findViewById(R.id.tvDate);
             holder.tvUpVoteCount = (TextView) convertView
                     .findViewById(R.id.tvUpVoteCount);
             holder.tvCommentCount = (TextView) convertView
                     .findViewById(R.id.tvCommentCount);
            holder.upvoteImage = (ImageView) convertView
                    .findViewById(R.id.ivUp);
 
             convertView.setTag(holder);
         } else {
             holder = (ViewHolder) convertView.getTag();
         }
 
         complaint = complaints.get(position);
 
         holder.problemImage.setImageResource(R.drawable.loading);
         complaint.getImage(0, Image.SIZE_512, holder.problemImage);
 
         WindowManager wm = (WindowManager) context
                 .getSystemService(Context.WINDOW_SERVICE);
         Display display = wm.getDefaultDisplay();
         Point size = new Point();
         display.getSize(size);
         int tmpWidth = size.x;
 
         LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(
                 tmpWidth, tmpWidth);
         convertView.findViewById(R.id.complaintItemInfoLayout).setLayoutParams(
                 llParams);
         holder.problemImage.setLayoutParams(llParams);
 
         holder.complaintTitle.setText(complaint.getTitle().substring(0, 1)
                 .toUpperCase()
                 + complaint.getTitle().substring(1).trim());
         holder.tvUpVoteCount.setText("" + complaint.getUpVote());
         holder.tvCommentCount.setText("" + complaint.getCommentsCount());
 
         if (complaint.alreadyVoted(user))
            holder.upvoteImage.setImageResource(R.drawable.up_voted);
        else
            holder.upvoteImage.setImageResource(R.drawable.up);
 
         // position
         if (tabPosition == 0) {
             holder.tvAdditionalInfo.setText(R.string.bla_hot);
         } else if (tabPosition == 1) {
             holder.tvAdditionalInfo.setText(complaint
                     .getDateAsString(this.context));
         } else if (tabPosition == 2) {
             holder.tvAdditionalInfo.setText(complaint.getDistance(this.context,
                     EnforceService.getLocation().getLatitude(), EnforceService
                             .getLocation().getLongitude()));
         } else {
             holder.tvAdditionalInfo
                     .setText(String.format(context.getString(R.string.bla_top),
                             complaint.getUpVote()));
         }
 
         return convertView;
     }
 
     private void loadMore() {
         new ComplaintListTask().execute();
     }
 
     private class ComplaintListTask extends
             BetterAsyncTask<Void, List<Complaint>> {
 
         @Override
         protected List<Complaint> task(Void... arg0) throws Exception {
             String sinceId = complaints.get(complaints.size() - 1).getId();
             Log.d(TAG, "Loading more -> " + sinceId);
 
             switch (tabPosition) {
             case 0: // Hot
                 return Complaint.getHotList(sinceId);
             case 1: // New
                 return Complaint.getNewList(sinceId);
             case 2: // Near
                 return Complaint.getNearList(sinceId, EnforceService
                         .getLocation().getLatitude(), EnforceService
                         .getLocation().getLongitude());
             case 3: // Top
                 return Complaint.getTopList(sinceId);
             default:
                 throw new RuntimeException();
             }
         }
 
         @Override
         protected void onSuccess(List<Complaint> result) {
             complaints.addAll(result);
             BugListAdapter.this.notifyDataSetChanged();
         }
 
         @Override
         protected void onFailure(Exception error) {
             // TODO Auto-generated method stub
         }
     }
 
     static class ViewHolder {
 
         ImageView problemImage;
        ImageView upvoteImage;
         TextView complaintTitle;
         TextView tvAdditionalInfo;
         TextView tvUpVoteCount;
         TextView tvCommentCount;
 
     }
 }
