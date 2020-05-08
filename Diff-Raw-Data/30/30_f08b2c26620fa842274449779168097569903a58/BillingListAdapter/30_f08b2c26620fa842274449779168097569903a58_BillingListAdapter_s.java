 
 package de.uni.stuttgart.informatik.ToureNPlaner.UI.Adapters;
 
 import java.util.ArrayList;
 
 import de.uni.stuttgart.informatik.ToureNPlaner.Data.*;
 import de.uni.stuttgart.informatik.ToureNPlaner.Net.Session;
 import android.content.Context;
 import android.view.Gravity;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AbsListView;
 import android.widget.BaseExpandableListAdapter;
 import android.widget.TextView;
 
 public class BillingListAdapter extends BaseExpandableListAdapter {
 	private ArrayList<BillingItem> billinglist ;
     private String[] billingCaptions;
     private String[][] billingItem;
     private Context context;
     
     public int count = 50; /* starting amount */
     public boolean isEnd;
     public int precount = 0;
 
     public ArrayList<BillingItem> getBillinglist() {
 		return billinglist;
 	}
 
 	public void setBillinglist(ArrayList<BillingItem> billinglist) {
 		this.billinglist = billinglist;
 	}
 
 	public BillingListAdapter(Context context,ArrayList<BillingItem> billinglist){
     	this.context = context;
     	this.billinglist = billinglist;
     	
     	SetupList();
     	
     }
     
     public void SetupList(){
     	isEnd = false;
     	billingCaptions = new String[billinglist.size()];
      	billingItem = new String[billinglist.size()][10];
       	if(count >= billinglist.size()){
       		count = billinglist.size();
       		isEnd=true;
        	}
        	
     	for (int i = 0;i<count;i++){
     		billingCaptions[i] = "Tour " + String.valueOf(billinglist.get(i).getRequestid());
     	}
     	
     	for (int i = 0;i<count;i++){	
     		billingItem[i][0] = "reqID: " +billinglist.get(i).getRequestid();
     		billingItem[i][1] = "userID: "+billinglist.get(i).getUserid();
     		billingItem[i][2] = "algorithmus: "+billinglist.get(i).getAlgorithm();
     		billingItem[i][3] = "request: "+billinglist.get(i).getRequest();
     		billingItem[i][4] = "response: "+billinglist.get(i).getResponse();
     		billingItem[i][5] = "cost: "+billinglist.get(i).getCost();
     		billingItem[i][6] = "requestDate: "+billinglist.get(i).getRequestdate();
     		billingItem[i][7] = "finishedDate: "+billinglist.get(i).getFinishdate();
     		billingItem[i][8] = "duration: "+billinglist.get(i).getDuration();
     		billingItem[i][9] = "status: "+billinglist.get(i).getStatus();
     	}   	
     }
     
     public Object getChild(int groupPosition, int childPosition) {
         return billingItem[groupPosition][childPosition];
     }
 
     public long getChildId(int groupPosition, int childPosition) {
         return childPosition;
     }
 
     public int getChildrenCount(int groupPosition) {
         return billingItem[groupPosition].length;
     }
 
     public TextView getGenericView() {
         // Layout parameters for the ExpandableListView
         AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
                 ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
 
         TextView textView = new TextView(context);
         textView.setLayoutParams(lp);
         // Center the text vertically
         textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
         // Set the text starting position
         textView.setPadding(36, 0, 0, 0);
         textView.setTextSize(20);
         return textView;
     }
 
     public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
         View convertView, ViewGroup parent) {
         TextView textView = getGenericView();
         textView.setText(getChild(groupPosition, childPosition).toString());
         return textView;
     }
 
     public Object getGroup(int groupPosition) {
         return billingCaptions[groupPosition];
     }
 
     public int getGroupCount() {
         return count ;
     }
 
     public long getGroupId(int groupPosition) {
         return groupPosition;
     }
 
     public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
             ViewGroup parent) {
         TextView textView = getGenericView();
         textView.setText(getGroup(groupPosition).toString());
         return textView;
     }
 
     public boolean isChildSelectable(int groupPosition, int childPosition) {
         return true;
     }
 
     public boolean hasStableIds() {
         return true;
     }
 
 }
 
