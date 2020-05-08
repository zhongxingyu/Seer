 package com.example.helloandroid.weatherforecast.adapter;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import android.content.Context;
 import android.graphics.Color;
 import android.view.Gravity;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AbsListView;
 import android.widget.BaseExpandableListAdapter;
 import android.widget.ExpandableListView;
 import android.widget.Filter;
 import android.widget.Filterable;
 import android.widget.TextView;
 
 /**
  * 
  * @author i-zqluo
  * 自定义的可伸缩性列表的适配器<br />
  * 例如以下代码所示，使用时，A,B二级列表项将在一级列表组group1下：
  * <code>
  * String[] groups={"group1", "group2", "group3"};
  * String[][] childs={{"A", "B"},{"C", "D"},{"E", "F"}};
  * provinceList= (ExpandableListView)findViewById(R.id.provinceList);
  * BaseExpandableListAdapter adapter=new MyListAdapter(this, groups, childs);
  * provinceList.setAdapter(adapter);
  * </code>
  * 使用时注意各级列表间的对应关系
  */
 public class MyListAdapter extends BaseExpandableListAdapter implements Filterable {
 	
 	/**
 	 * 定义的一级菜单项数组
 	 */
 	private String[] groups;
 	/**
 	 * 定义的二级菜单项数组
 	 */
 	private String[][] childs;
 	
 	/**
 	 * 全部的一级菜单项数组
 	 */
 	private String[] allGroups;
 	
 	/**
 	 * 全部的二级菜单项数组
 	 */
 	private String[][] allChilds;
 	
 	/**
 	 * 记录上下文Context，用于产生一个TextView实例
 	 * @see #getGenericView
 	 */
 	private Context context;
 	
 	/**
 	 * 自定义的城市过滤器
 	 */
 	private CityFilter filter;
 	
 	/**
 	 * 记录当前使用此适配器的伸缩性列表
 	 */
 	private ExpandableListView provinceList;
 	
 	/**
 	 * 构造方法，指定一级菜单项，二组菜单项，来构造一个可伸缩性列表的适配器
 	 * 其中的childs要与groups的索引值对应
 	 * <br />
 	 * @param context 为 <code>Context</code>对象，用于记录此控件的上下文本对象
 	 * @param groups 为<code>String</code>类型数组，定义的一级菜单项数组
 	 * @param childs 为<code>String</code>类型数组,与groups索引值相对应的二级菜单项二维数组
 	 */
 	public MyListAdapter(Context context, ExpandableListView listView, String[] groups, String[][] childs) {
 		this.context = context;
 		this.groups = groups;
 		this.childs = childs;
 		allGroups = groups;
 		allChilds = childs;
 		this.provinceList = listView;
 	}
 	
 	
 	/**
 	 * 由一级菜单的索引值<code>groupPosition</code>与二级菜单的索引值<code>childPosition</code>
 	 * 来返回指定的对象值<br />
 	 * @param groupPostion为一级菜单项的索引
 	 * @param childPostion为对应的二级菜单索引
 	 * @return 二级菜单项的内容
 	 */
 	@Override
 	public Object getChild(int groupPosition, int childPosition) {
 		return childs[groupPosition][childPosition];
 	}
 
 	/**
 	 * 由一级菜单的索引值<code>groupPosition</code>与二级菜单的索引值<code>childPosition</code>
 	 * 来返回子项的ID
 	 * @param groupPostion为一级菜单项的索引
 	 * @param childPostion为对应的二级菜单索引
 	 * @return 二级子菜单项的ID
 	 */
 	@Override
 	public long getChildId(int groupPosition, int childPosition) {
 		return childPosition;
 	}
 	
 	/**
 	 * @return 二级子列表项的<code>View<code>对象
 	 */
 	@Override
 	public View getChildView(int groupPosition, int childPosition,
 			boolean isLastChild, View convertView, ViewGroup parent) {
 		TextView textView = null;
 		//这里判断convertView是否为空，是为了它的复用，提高性能
 		if(convertView==null) {
 			//产生一个TextView组件
 			textView = getGenericView();
 			//设置它的文本内容
 			textView.setText(getChild(groupPosition, childPosition).toString());
 		} else {
 			textView = (TextView)convertView;
 			textView.setText(getChild(groupPosition, childPosition).toString());
 		}
 		
 		return textView;
 	}
 
 	/**
 	 * 得到应一级列表项的子列表项个数
 	 * @param groupPosition一级列表的索引值
 	 * @return 对应二组级列表个数
 	 */
 	@Override
 	public int getChildrenCount(int groupPosition) {
 		return childs[groupPosition].length;
 	}
 
 	/**
 	 * 由一级列表索引值<code>groupPosition</code>得到一级列表的项的内容
 	 * @return 一级列表项内容
 	 */
 	@Override
 	public Object getGroup(int groupPosition) {
 		return groups[groupPosition];
 	}
 
 	/**
 	 * 一级列表的个数
 	 * @return 返回一级列表个数
 	 */
 	@Override
 	public int getGroupCount() {
 		return groups.length;
 	}
 
 	/**
 	 * 得到一级列表ID,即索引值
 	 * @param groupPosition为一级列表的索引值
 	 * @return 一级列表ID，从<code>0</code>开始
 	 */
 	@Override
 	public long getGroupId(int groupPosition) {
 		return groupPosition;
 	}
 
 	/**
 	 * @return 一级列表对应的<code>View</code>对象实例
 	 */
 	@Override
 	public View getGroupView(int groupPosition, boolean isExpanded,
 			View convertView, ViewGroup parent) {
 		TextView textView = null;
 		if(convertView==null){
 			textView = getGenericView();
 			textView.setText(getGroup(groupPosition).toString());
 		}else {
 			textView = (TextView)convertView;
 			textView.setText(getGroup(groupPosition).toString());
 		}
 		
 		return textView;
 	}
 
 	@Override
 	public boolean hasStableIds() {
 		return true;
 	}
 
 	/**
 	 * 判断指定的二组选项是否可选
 	 * @param groupPosition 一级列表项索引值
 	 * @param childPosition 二级列表项索引值
 	 * @return 布尔型
 	 */
 	@Override
 	public boolean isChildSelectable(int groupPosition, int childPosition) {
 		return true;
 	}
 	
 	/**
 	 * 产生一个TextView对象实例
 	 * @return 一个TextView组件实例
 	 */
 	 private TextView getGenericView() {
 		AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
 				ViewGroup.LayoutParams.FILL_PARENT, 48);
 		
 		TextView textView = new TextView(context);
 		
 		textView.setLayoutParams(lp);
 		textView.setTextSize(18);
		textView.setHeight( 24 );
 		textView.setTextColor(Color.GRAY);
 		textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
 		textView.setPadding(50, 0, 0, 0);
 		return textView;
 	}
 
 	/**
 	 * 返回此伸缩性列表的过滤器
 	 * @return Filter 一个过滤器对象
 	 */
 	@Override
 	public Filter getFilter() {
 		if(filter == null) {
 			filter = new CityFilter();
 		}
 		return filter;
 	}
 	
 	/**
 	 * 
 	 * Descriptions 该内部类继承了abstract class Filter，必须实现其中的两个抽象方法performFiltering和publishResults，用于过滤对象列表
 	 *
 	 * @version 2013-6-4
 	 * @author PSET
 	 * @since JDK1.6
 	 *
 	 */
 	private class CityFilter extends Filter {
 
 		/**
 		 * 由条件字符串来过滤列表，返回过滤后的结果封装对象FilterResults
 		 * @param constraint 过滤的条件字符串
 		 * @return 一个封装过滤的结果类
 		 */
 		@Override
 		protected FilterResults performFiltering(CharSequence constraint) {
 			FilterResults results = new FilterResults();
 			
 			//用于存放符合条件的省份索引值,各与之对应匹配的城市
 			Map<Integer, ArrayList<Integer>> values = new HashMap<Integer, ArrayList<Integer>>();
 			//当过滤条件为空时，返回所有的省份与城市
 			if(constraint == null || constraint.length() == 0) {
 				for(int i=0; i<allGroups.length; i++) {
 					ArrayList<Integer> index = new ArrayList<Integer>();
 					//添加所有与之对应的城市
 					for(int j=0; j<allChilds[i].length; j++) {
 						index.add(j);
 					}
 					values.put(i, index);
 				}
 			} else {
 				String filterStr = constraint.toString();
 				for(int i=0; i<allGroups.length; i++) {
 					//查找省名是否包含用户输入的字符串
 					if(allGroups[i].contains(filterStr)) {
 						ArrayList<Integer> index = new ArrayList<Integer>();
 						//添加所有与之对应的城市
 						for(int j=0; j<allChilds[i].length; j++) {
 							index.add(j);
 						}
 						values.put(i, index);
 					} else {
 						ArrayList<Integer> index = new ArrayList<Integer>();
 						//如果省份名没有，则查找它下面的城市名是否包含
 						for(int j=0; j<allChilds[i].length; j++) {
 							if(allChilds[i][j].contains(filterStr)) {
 								index.add(j);
 							}
 						}
 						//如果添加进入了城市，说明存在，则它的省份也添加进去
 						if(index.size() > 0) {
 							values.put(i, index);
 						} else {
 							index = null;
 						}
 					}
 				}
 			}
 			
 			results.values = values;
 			results.count = values.size();
 			
 			return results;
 		}
 
 		/**
 		 * 解析过滤得到的结果results，更新适配器的列表数据
 		 * @param constraint 过滤条件
 		 * @param results 过滤得到的结果
 		 */
 		@SuppressWarnings("unchecked")
 		@Override
 		protected void publishResults(CharSequence constraint,
 				FilterResults results) {
 			//得到过滤后的省份索引列表
 			Map<Integer, ArrayList<Integer>> filterResult = (Map<Integer, ArrayList<Integer>>) results.values;
 			int count = filterResult.size();
 			//如果值存在
 			if( count > 0) {
 				String[] newGroups = new String[count];
 				String[][] newChilds = new String[count][];
 				int index = 0;
 				int length = 0;
 				//得到新的groups和childs
 				for(int i=0; i<allGroups.length; i++) {
 					if(filterResult.containsKey(i)) {
 						newGroups[index] = allGroups[i];
 						//符合条件的城市
 						ArrayList<Integer> citys = filterResult.get(i);
 						length = citys.size();
 						newChilds[index] = new String[length];
 						for(int j = 0; j< length; j++) {
 							newChilds[index][j] = allChilds[i][citys.get(j)];
 						}
 						index = index + 1;
 					}
 				}
 				//设置groups和childs
 				groups = newGroups;
 				childs = newChilds;
 				
 				//更新列表
 				notifyDataSetChanged();
 				
 				//判断是否展开列表
 				count = getGroupCount();
 				if(count < 34) {
 					//展开伸缩性列表
 					for(int i=0; i<count; i++) {
 						provinceList.expandGroup(i);
 					}
 				} else {
 					//收缩伸缩性列表
 					for(int i=0; i<count; i++) {
 						provinceList.collapseGroup(i);
 					}
 				}
 			} else {
 				//没有过滤值，则通知为无效的数据更新
 				notifyDataSetInvalidated();
 			}
 		}
 	}
 }
