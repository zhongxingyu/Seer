 package com.tzachsolomon.expandablelistcheckbox;
 
 import java.util.ArrayList;
 
 
 
 import android.app.ExpandableListActivity;
 import android.content.Context;
 
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseExpandableListAdapter;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 
 public class ExpandableListCheckBoxActivity extends ExpandableListActivity {
 	private Items m_Items;
 ;
 
 	/** Called when the activity is first created. */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		//
 		super.onCreate(savedInstanceState);
 
 		m_Items = new Items();
 
 		m_Items.addEmptyGroup("Group1", false);
 		m_Items.addEmptyGroup("Group2", true);
		
		m_Items.addChildToGroup(0, "Child1", true);
		m_Items.addChildToGroup(0, "Child2", false);
		
		MyBaseExpandableListAdapter m_Adapter = new MyBaseExpandableListAdapter(this, m_Items);
		
		setListAdapter(m_Adapter);
		
		
		
		
 
 	}
 
 	
 	public class MyBaseExpandableListAdapter extends BaseExpandableListAdapter {
 
 		private Items m_Items;
 		private Context m_Context;
 
 		public MyBaseExpandableListAdapter(Context i_Context, Items i_Items) {
 			m_Items = i_Items;
 			m_Context = i_Context;
 		}
 
 		@Override
 		public Object getChild(int arg0, int arg1) {
 			// 
 			return null;
 		}
 
 		@Override
 		public long getChildId(int groupPosition, int childPosition) {
 			// 
 			return 0;
 		}
 
 		@Override
 		public View getChildView(int groupPosition, int childPosition,
 				boolean isLastChild, View convertView, ViewGroup parent) {
 			// 
 			CheckBox cb;
 			if (convertView == null) {
 				LayoutInflater layoutInflater = (LayoutInflater) m_Context
 						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 				convertView = layoutInflater.inflate(R.layout.group_row, null);
 
 			}
 
 			cb = (CheckBox) convertView.findViewById(R.id.checkBoxGroupRow);
 			cb.setTag("" + groupPosition + "," + childPosition);
 			cb.setText(m_Items.getChildText(groupPosition,childPosition));
 			cb.setChecked(m_Items.getChildState(groupPosition,childPosition));
 			cb.setOnCheckedChangeListener(onCheckedChangedChild);
 
			return convertView;
 			
 		
 		}
 
 		@Override
 		public int getChildrenCount(int groupPosition) {
 			// 
 			return m_Items.getChildrenCount(groupPosition);
 		}
 
 		@Override
 		public Object getGroup(int groupPosition) {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public int getGroupCount() {
 			// 
 			return m_Items.getGroupCount();
 		}
 
 		@Override
 		public long getGroupId(int groupPosition) {
 			// TODO Auto-generated method stub
 			return 0;
 		}
 
 		@Override
 		public View getGroupView(int groupPosition, boolean isExpanded,
 				View convertView, ViewGroup parent) {
 			//
 			CheckBox cb;
 			if (convertView == null) {
 				LayoutInflater layoutInflater = (LayoutInflater) m_Context
 						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 				convertView = layoutInflater.inflate(R.layout.group_row, null);
 
 			}
 
 			cb = (CheckBox) convertView.findViewById(R.id.checkBoxGroupRow);
 			cb.setTag("" + groupPosition);
 			cb.setText(m_Items.getGroupText(groupPosition));
 			cb.setChecked(m_Items.getGroupState(groupPosition));
 			cb.setOnCheckedChangeListener(onCheckedChangedGroup);
 
			return convertView;
 		}
 
 		@Override
 		public boolean hasStableIds() {
 			//
 			return false;
 		}
 
 		@Override
 		public boolean isChildSelectable(int groupPosition, int childPosition) {
 			//
 			return false;
 		}
 		
 		OnCheckedChangeListener onCheckedChangedGroup = new OnCheckedChangeListener() {
 			
 			@Override
 			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 				// 
 				String tag = buttonView.getTag().toString();
 				int groupId = Integer.valueOf(tag);
 				m_Items.setGroupState(groupId,isChecked);
 				
 			}
 		};
 		
 		OnCheckedChangeListener onCheckedChangedChild = new OnCheckedChangeListener() {
 			
 			@Override
 			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 				// 
 				String tag = buttonView.getTag().toString();
 				int groupId = Integer.valueOf(tag.split(",")[0]);
 				int childId = Integer.valueOf(tag.split(",")[1]);
 				m_Items.setChildState(groupId,childId,isChecked);
 			}
 		};
 
 	}
 	
 	public class Items {
 		private ArrayList<String> m_GroupItemsText;
 		private ArrayList<Boolean> m_GroupItemsState;
 		private ArrayList<ArrayList<String>> m_ChildItemsText;
 		private ArrayList<ArrayList<Boolean>> m_ChildItemsState;
 
 		public Context m_Context;
 
 		public Items() {
 			m_GroupItemsState = new ArrayList<Boolean>();
 			m_GroupItemsText = new ArrayList<String>();
 			m_ChildItemsState = new ArrayList<ArrayList<Boolean>>();
 			m_ChildItemsText = new ArrayList<ArrayList<String>>();
 		}
 
		public void addChildToGroup(int i_GroupId, String i_ChildText, boolean i_ChildState) {
			//
			if ( i_GroupId < m_ChildItemsText.size()){
				m_ChildItemsState.get(i_GroupId).add(i_ChildState);
				m_ChildItemsText.get(i_GroupId).add(i_ChildText);
			}
			
			
		}

 		public void setChildState(int groupId, int childId, boolean isChecked) {
 			// 
 			m_ChildItemsState.get(groupId).set(childId, isChecked);
 			
 		}
 
 		public boolean getChildState(int groupPosition, int childPosition) {
 			// 
 			
 			return m_ChildItemsState.get(groupPosition).get(childPosition);
 		}
 
 		public CharSequence getChildText(int groupPosition, int childPosition) {
 			// TODO Auto-generated method stub
 			return m_ChildItemsText.get(groupPosition).get(childPosition);
 		}
 
 		public void setGroupState(int groupId, boolean isChecked) {
 			// 
 			m_GroupItemsState.set(groupId, isChecked);
 			
 		}
 
 		public boolean getGroupState(int groupPosition) {
 			// 
 			return m_GroupItemsState.get(groupPosition);
 		}
 
 		public int getGroupCount() {
 			// 
 			return m_GroupItemsState.size();
 		}
 
 		public int getChildrenCount(int groupPosition) {
 			// 
 			return m_ChildItemsState.get(groupPosition).size();
 		}
 
 		public int addGroup(String i_GroupName, boolean i_State) {
 			//
 			m_GroupItemsText.add(i_GroupName);
 			m_GroupItemsState.add(i_State);
 
 			// returning the new group id
 			return m_GroupItemsState.size() - 1;
 		}
 
 		public int addEmptyGroup(String i_GroupName, boolean i_State) {
 			int groupId;
 
 			groupId = addGroup(i_GroupName, i_State);
 			addChildrenToGroup(groupId, null, null);
 
 			return groupId;
 		}
 
 		public void addChildrenToGroup(int i_GroupId,
 				ArrayList<String> i_ChildText, ArrayList<Boolean> i_ChildState) {
 			if (i_GroupId < m_GroupItemsText.size()) {
 				if (i_ChildState == null) {
 					i_ChildState = new ArrayList<Boolean>();
 				}
 				if (i_ChildText == null) {
 					i_ChildText = new ArrayList<String>();
 				}
 
 				m_ChildItemsState.add(i_GroupId, i_ChildState);
 				m_ChildItemsText.add(i_GroupId, i_ChildText);
 
 			}
 		}
 
 		public CharSequence getGroupText(int groupPosition) {
 			//
 			if (groupPosition < m_GroupItemsText.size()) {
 				return m_GroupItemsText.get(groupPosition);
 			} else {
 				return null;
 			}
 		}
 
 	}
 
 }
