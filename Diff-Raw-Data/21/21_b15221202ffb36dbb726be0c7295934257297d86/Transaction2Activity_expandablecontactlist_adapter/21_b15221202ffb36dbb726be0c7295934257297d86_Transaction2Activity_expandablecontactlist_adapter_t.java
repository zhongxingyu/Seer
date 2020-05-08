 package com.example.payback;
 
 import java.util.HashMap;
 import java.util.List;
 import android.content.Context;
 import android.graphics.Typeface;
 import android.view.LayoutInflater;
 import android.view.View;
import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
 import android.widget.TextView;
 
 public class Transaction2Activity_expandablecontactlist_adapter extends BaseExpandableListAdapter {
 
 //  private final SparseArray<Transaction2Activity_expandablecontactlist_group> groups;
 //  private LayoutInflater inflater;
 //  private Activity activity;
 //  private List<Friend> list;
   
   private Context context;
   private List<String> listHeader;
   private HashMap<String, List<Friend>> listChild;
 
   public Transaction2Activity_expandablecontactlist_adapter(Context context, List<String> listHeader, HashMap<String, List<Friend>> listChild) {
 	  this.context = context;
       this.listHeader = listHeader;
       this.listChild = listChild;
   }
       
   @Override
   public Object getChild(int groupPosition, int childPosition) {
       return this.listChild.get(this.listHeader.get(groupPosition)).get(childPosition);
       }
 
   @Override
   public long getChildId(int groupPosition, int childPosition) {
     return childPosition;
   }
 
   @Override
  public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
 
       final String childText = getChild(groupPosition, childPosition).toString();
 
       if (convertView == null) {
           LayoutInflater infalInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
           convertView = infalInflater.inflate(R.layout.activity_transaction2_expandablecontactlist_item, null);
       }
      
      final CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.checkBox);
      checkBox.setChecked(listChild.get(listHeader.get(groupPosition)).get(childPosition).isSelected());

      checkBox.setOnClickListener(new OnClickListener() {
    	    @Override
    	    public void onClick(View arg0) {
    	        final boolean isChecked = checkBox.isChecked();
    	        listChild.get(listHeader.get(groupPosition)).get(childPosition).setSelected(isChecked);
    	    }
    	});
 
       TextView txtListChild = (TextView) convertView.findViewById(R.id.contactname);
 
       txtListChild.setText(childText);
       return convertView;
     
   }
  
  
 
   @Override
   public int getChildrenCount(int groupPosition) {
 	return this.listChild.get(this.listHeader.get(groupPosition)).size();
   }
 
   @Override
   public Object getGroup(int groupPosition) {
 	return this.listHeader.get(groupPosition);
   }
 
   @Override
   public int getGroupCount() {
 	  return this.listHeader.size();
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
     return 0;
   }
 
   @Override
   public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
 		  
 	  String headerTitle = (String) getGroup(groupPosition);
       if (convertView == null) {
           LayoutInflater infalInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
           convertView = infalInflater.inflate(R.layout.activity_transaction2_expandablecontactlist_group, null);
       }
 
       TextView ListHeader = (TextView) convertView.findViewById(R.id.grouptext1);
       ListHeader.setTypeface(null, Typeface.BOLD);
       ListHeader.setText(headerTitle);
 
       return convertView;
 
   }
 
   @Override
   public boolean hasStableIds() {
     return false;
   }
 
   @Override
   public boolean isChildSelectable(int groupPosition, int childPosition) {
     return true;
   }
 } 
