 package com.taiwan.imageload;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.BaseExpandableListAdapter;
 import android.widget.ExpandableListView;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.kosbrother.tool.ChildArticle;
 import com.kosbrother.tool.Group;
 import com.novel.reader.ArticleActivity;
 import com.novel.reader.R;
 import com.novel.reader.api.NovelAPI;
 import com.novel.reader.entity.Bookmark;
 import com.novel.reader.entity.Novel;
 
 public class ExpandListAdapter extends BaseExpandableListAdapter {
 
     private static LayoutInflater inflater = null;
     private final Activity        activity;
     public ArrayList<Group>       theGroups;
     private final Novel           theNovel;
     private final Bookmark        theNovelBookmark;
    private final int             expandGroup;
 
     public ExpandListAdapter(Activity a, ArrayList<Group> mGroups, Novel mNovel, int expandGroup) {
 
         activity = a;
         theGroups = mGroups;
 
         inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
         theNovel = mNovel;
         theNovelBookmark = NovelAPI.getNovelBookmark(theNovel.getId(), a);
         this.expandGroup = expandGroup;
     }
 
     @Override
     public Object getChild(int groupPosition, int childPosition) {
         return null;
     }
 
     @Override
     public long getChildId(int groupPosition, int childPosition) {
         return 0;
     }
 
     @Override
     public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
 
         final ChildArticle child = theGroups.get(groupPosition).getChildItem(childPosition);
 
         View vi = convertView;
         vi = inflater.inflate(R.layout.item_expandible_child, null);
         TextView text = (TextView) vi.findViewById(R.id.expandlist_child);
         String childString = child.getTitle();
         text.setText(childString);
 
         vi.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
 
                 ArrayList<Integer> articleIDs = new ArrayList<Integer>();
 
                 for (int i = 0; i < theGroups.size(); i++) {
                     for (int j = 0; j < theGroups.get(i).getChildrenCount(); j++) {
                         articleIDs.add(theGroups.get(i).getChildItem(j).getId());
                     }
                 }
 
                 ArrayList<Integer> articleNums = new ArrayList<Integer>();
 
                 for (int i = 0; i < theGroups.size(); i++) {
                     for (int j = 0; j < theGroups.get(i).getChildrenCount(); j++) {
                         articleNums.add(theGroups.get(i).getChildItem(j).getNum());
                     }
                 }
 
                 int position = 0;
                 for (int i = 0; i < groupPosition; i++) {
                     position = position + theGroups.get(i).getChildrenCount();
                 }
                 position = position + childPosition;
 
                 Intent intent = new Intent(activity, ArticleActivity.class);
                 Bundle bundle = new Bundle();
                 bundle.putIntegerArrayList("ArticleIDs", articleIDs);
                 bundle.putInt("ArticlePosition", position);
                 bundle.putInt("ArticleId", child.getId());
                 bundle.putString("ArticleTitle", child.getTitle());
                 bundle.putString("NovelName", theNovel.getName());
                 bundle.putString("NovelPic", theNovel.getPic());
                 bundle.putInt("NovelId", theNovel.getId());
                 bundle.putInt("ArticleNum", child.getNum());
                 bundle.putIntegerArrayList("ArticleNums", articleNums);
                 intent.putExtras(bundle);
                 activity.startActivity(intent);
             }
         });
 
         if (theNovelBookmark != null) {
             if (theNovelBookmark.getArticleTitle().equals(childString))
                 text.setTextColor(Color.parseColor("#FF1919"));
         }
 
         return vi;
     }
 
     @Override
     public int getChildrenCount(int groupPosition) {
         return theGroups.get(groupPosition).getChildrenCount();
     }
 
     @Override
     public Object getGroup(int groupPosition) {
         return theGroups.get(groupPosition);
     }
 
     @Override
     public int getGroupCount() {
         return theGroups.size();
     }
 
     @Override
     public void onGroupCollapsed(int groupPosition) {
         super.onGroupCollapsed(groupPosition);
     }
 
     @Override
     public void onGroupExpanded(int groupPosition) {
         super.onGroupExpanded(groupPosition);
     }
 
     @Override
     public long getGroupId(int groupPosition) {
         return groupPosition;
     }
 
     @Override
     public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
 
         Group group = theGroups.get(groupPosition);
 
         View vi = convertView;
         vi = inflater.inflate(R.layout.item_expandbile_parent, null);
         TextView text = (TextView) vi.findViewById(R.id.expandlist_parent);
         String groupString = group.getTitle();
         text.setText(groupString);
 
         int id = (!isExpanded) ? R.drawable.right_arrow : R.drawable.up_arrow;
         ImageView image = (ImageView) vi.findViewById(R.id.expandlist_parent_button);
         image.setImageResource(id);

         if ((expandGroup != -1) && (expandGroup == groupPosition)) {
            ExpandableListView eLV = (ExpandableListView) parent;
             eLV.expandGroup(expandGroup);
         }
 
         return vi;
     }
 
     @Override
     public boolean hasStableIds() {
         return true;
     }
 
     @Override
     public boolean isChildSelectable(int groupPosition, int childPosition) {
         return true;
     }
 
 }
