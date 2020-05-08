 package edu.uconn.pha;
 
 import android.content.Context;
 import android.graphics.Color;
 import android.util.Log;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseExpandableListAdapter;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.TextView;
 import edu.uconn.model.Permission;
 import edu.uconn.model.Policy;
 
 public class PermissionsList extends BaseExpandableListAdapter {
 	private static final String TAG = PermissionsList.class.getName();
 	private Policy policy;
 	private PermissionsActivity context;
 
 	public PermissionsList(Context context, Policy policy) {
 		this.policy = policy;
 		this.context = (PermissionsActivity) context;
 	}
 
 	@Override
 	public Object getChild(int groupPosition, int childPosition) 
 	{   
 		return childPosition;
 	}
 
 	@Override
 	public long getChildId(int groupPosition, int childPosition) 
 	{
 		return childPosition;
 	}
 
 	@Override
 	public View getChildView(final int groupPosition, final int childPosition,
 			boolean isLastChild, View convertView, ViewGroup parent) 
 	{
 		// policy = context.getPolicy();
 		Permission permission = policy.getRole(groupPosition).getPermission(childPosition);
 
 		// Initialize 
 		View view = null;
		if( convertView != null ) {
			view = convertView;
		} else {
 			view = context.getLayoutInflater().inflate(R.layout.permissions_object, parent, false); 
		}
 
 		// Permission label
 		TextView permissionLabel = (TextView) view.findViewById( R.id.permission );
 		permissionLabel.setText(permission.getName());
 
 		// Read Label
 		TextView read = (TextView) view.findViewById( R.id.read );
 		read.setText( "View" );
 
 		// Read Checkbox
 		CheckBox cbr = (CheckBox) view.findViewById( R.id.checkread );
 		cbr.setChecked( permission.canRead() );
 		// cbr.setFocusableInTouchMode(false);
 		cbr.setOnCheckedChangeListener(new OnCheckedChangeListener()
 		{
 			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
 			{
                 Log.d(TAG, "--- Read position: " +groupPosition+"/"+childPosition+" "+isChecked+" ---");
 				policy.setPermissionRead(groupPosition, childPosition, isChecked);
 				context.setPermissionRead(groupPosition, childPosition, isChecked);
 			}
 		});
 
 
 		// Write Label
 		TextView write = (TextView) view.findViewById( R.id.write );
 		write.setText( "Update" );
 
 		// Write Checkbox
 		CheckBox cbw = (CheckBox) view.findViewById( R.id.checkwrite );
 		cbw.setChecked( permission.canWrite() );
 		cbw.setOnCheckedChangeListener(new OnCheckedChangeListener()
 		{
 			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
 			{
                 Log.d(TAG, "--- Write position: " +groupPosition+"/"+childPosition+" "+isChecked+" ---");
 				policy.setPermissionWrite(groupPosition, childPosition, isChecked);
 				context.setPermissionWrite(groupPosition, childPosition, isChecked);
 			}
 		});
 
 		return view;
 	}
 
 	@Override
 	public int getChildrenCount(int groupPosition) 
 	{   
 		return policy.getRole(groupPosition).numPermissions();
 	}
 
 	@Override
 	public Object getGroup(int groupPosition) {
 		return groupPosition;
 	}
 
 	@Override
 	public int getGroupCount() 
 	{   
 		return policy.numRoles();
 	}
 
 	@Override
 	public long getGroupId(int groupPosition) 
 	{   
 		return groupPosition;
 	}
 
 	@Override
 	public View getGroupView(int groupPosition, boolean isExpanded,
 			View convertView, ViewGroup parent) 
 	{			
 		// New TextView
 		TextView tv = new TextView(context);
 
 		// Design
 		tv.setPadding(10, 10, 10, 10); 
 
 		if ( isExpanded ) {
 			tv.setBackgroundColor(Color.rgb(20,20,20));
 			tv.setTextColor(Color.WHITE);
 		} else {
 			tv.setBackgroundColor(Color.rgb(10,10,10));
 			tv.setTextColor(Color.GRAY);
 		}
 
 		// Content
 		tv.setText(policy.getRole(groupPosition).getName());
 
 		// Return
 		return tv;
 	}
 
 	@Override
 	public boolean hasStableIds()
 	{
 		return true;
 	}
 
 	@Override
 	public boolean isChildSelectable(int groupPosition, int childPosition) 
 	{
 		return false;
 	}     
 }
