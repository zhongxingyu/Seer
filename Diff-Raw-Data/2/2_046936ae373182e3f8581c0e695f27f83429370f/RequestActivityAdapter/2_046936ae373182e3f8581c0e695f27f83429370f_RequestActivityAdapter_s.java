 package autobahn.android.adapters;
 
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.Typeface;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseExpandableListAdapter;
 import android.widget.TextView;
 import com.example.autobahn.R;
 
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Nosfistis
  * Date: 7/8/2013
  * Time: 2:43 μμ
  * To change this template use File | Settings | File Templates.
  */
 public class RequestActivityAdapter extends BaseExpandableListAdapter {
     private final Activity context;
     private final ArrayList<String> groupList;
     private final LinkedHashMap<String, ArrayList<String>> parameterInfoCollection;
 
     public RequestActivityAdapter(Activity context, ArrayList<String> groupList, LinkedHashMap<String, ArrayList<String>> headerCollection) {
         this.context = context;
         this.groupList = groupList;
         this.parameterInfoCollection = headerCollection;
     }
 
     @Override
     public int getGroupCount() {
         return groupList.size();
     }
 
     @Override
     public int getChildrenCount(int groupPosition) {
         return parameterInfoCollection.get(groupList.get(groupPosition)).size();
     }
 
     @Override
     public Object getGroup(int groupPosition) {
         return groupList.get(groupPosition);
     }
 
     @Override
     public Object getChild(int groupPosition, int childPosition) {
         return parameterInfoCollection.get(groupList.get(groupPosition)).get(childPosition);
     }
 
     @Override
     public long getGroupId(int groupPosition) {
         return groupPosition;
     }
 
     @Override
     public long getChildId(int groupPosition, int childPosition) {
         return childPosition;
     }
 
     @Override
     public boolean hasStableIds() {
         return true;
     }
 
     @Override
     public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
         String parameterName = (String) getGroup(groupPosition);
         if (convertView == null) {
             LayoutInflater infalInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
             convertView = infalInflater.inflate(R.layout.group_heading, null);
         }
        TextView item = (TextView) convertView.findViewById(R.id.parameter);
         item.setTypeface(null, Typeface.BOLD);
         item.setText(parameterName);
         return convertView;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
         final String parameter = (String) getChild(groupPosition, childPosition);
         LayoutInflater inflater = context.getLayoutInflater();
 
         if (convertView == null) {
             convertView = inflater.inflate(R.layout.child_row, null);
         }
 
         TextView item = (TextView) convertView.findViewById(R.id.parameter);
         item.setText(parameter);
         return convertView;
     }
 
     @Override
     public boolean isChildSelectable(int groupPosition, int childPosition) {
         return true;
     }
 }
