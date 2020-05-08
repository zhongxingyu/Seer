 package com.quanleimu.view;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnCancelListener;
 import android.location.Location;
 import android.util.Log;
 import android.view.View;
 import android.widget.TextView;
 
 import com.quanleimu.activity.R;
 import com.quanleimu.adapter.GoodsListAdapter;
 import com.quanleimu.adapter.GoodsListAdapter.GroupItem;
 import com.quanleimu.entity.BXLocation;
 import com.quanleimu.entity.Filterss;
 import com.quanleimu.entity.GoodsDetail;
 import com.quanleimu.entity.GoodsDetail.EDATAKEYS;
 import com.quanleimu.entity.values;
 import com.quanleimu.view.fragment.MultiLevelSelectionFragment;
 import com.quanleimu.view.fragment.PostParamsHolder;
 import com.quanleimu.view.fragment.MultiLevelSelectionFragment.MultiLevelItem;
 
 /**
  * 
  * @author liuchong
  *
  */
 public class FilterUtil {
 
 	public static interface FilterSelectListener
 	{
 		public void onItemSelect(MultiLevelItem item);
 		public void onCancel();
 	}
 	
 	public static class CustomizeItem extends MultiLevelSelectionFragment.MultiLevelItem
 	{
 		
 	}
 	
 	public static List<GoodsListAdapter.GroupItem> createFilterGroup(List<Filterss> fss, PostParamsHolder paramsHolder, List<GoodsDetail> list)
 	{
 		final int skipCount = 3;//FIXME: define a field..
		if (list == null || list.size() == 0)
 		{
 			return null;
 		}
 		
 		StringBuffer buf = new StringBuffer();
 		if (paramsHolder.containsKey(""))
 		{
 			buf.append(paramsHolder.getData("")).append("+");
 		}
 
 		int skiped = 0;
 		for (int i=0; i<fss.size(); i++)
 		{
 			Filterss f = fss.get(i);
 			if (f.getControlType().equals("select"))
 			{
 				if (skiped < skipCount)
 				{
 					skiped++;
 				}
 				else if (paramsHolder.containsKey(f.getName()))
 				{
 					buf.append(paramsHolder.getUiData(f.getName())).append("+");
 				}
 			}
 		}
 		
 		List<GoodsListAdapter.GroupItem> groups = new ArrayList<GoodsListAdapter.GroupItem>();
 		if (buf.length() > 0)
 		{
 			buf.deleteCharAt(buf.length()-1);
 			GroupItem g = new GroupItem();
 			g.filterHint = "\"" + buf.toString() + "\"";
 			g.resultCount = list.size();
 			groups.add(g);
 		}
 		
 		return groups;
 	}
 	
	public static List<GoodsListAdapter.GroupItem> createDistanceGroup(List<Filterss> fss, List<GoodsDetail> ls, BXLocation currentLocation, int[] conditions)
 	{
 		List<GoodsDetail> detailList = new ArrayList<GoodsDetail>();
 		detailList.addAll(ls);
 		List<GoodsListAdapter.GroupItem> groups = new ArrayList<GoodsListAdapter.GroupItem>();
 		
 		for (int i=0; i<conditions.length; i++)
 		{
 			int count = 0;
 			for (int j=0; j<detailList.size();)
 			{
 				float results[] = {0.0f, 0.0f, 0.0f};
 				String lat = detailList.get(j).getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_LAT);
 				String lon = detailList.get(j).getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_LON);
 				
 				double latD = 0;
 				double lonD = 0;
 				try
 				{
 					latD = Double.parseDouble(lat);
 					lonD = Double.parseDouble(lon);
 				}
 				catch(Throwable t)
 				{
 					Log.d("GetGoodView", "ad nearby lacks lat & lon");
 				}
 
 				if (latD != 0 && lonD != 0 && currentLocation != null)
 				{
 					Location.distanceBetween(latD, lonD, currentLocation.fLat, currentLocation.fLon, results);
 				}
 				
 				if (results[0] > conditions[i])
 				{
 					Log.d("LIST", "find first item distance > " + conditions[i] + " " + detailList.get(j).getValueByKey(EDATAKEYS.EDATAKEYS_TITLE));
 					break;
 				}
 
 				detailList.remove(j);
 				count ++;
 				
 				
 //				if (results[i] != 0.0 && results[i] < conditions[i])
 //				{
 //					count++;
 //					detailList.remove(j);
 //				}
 //				else
 //				{
 //					j++;
 //				}
 			}
 			
 			
 			if (count > 0)
 			{
 				GroupItem item = new GroupItem();
 				item.resultCount = count;
 				item.filterHint = "附近" + getDisplayDistance(conditions[i])  + "的信息";
 				groups.add(item);
 				Log.d("LIST", "group:" +item.filterHint + "(" + item.resultCount + " of " + ls.size());
 			}
 		}
 		
 		if (detailList.size() > 0)
 		{
 			GroupItem item = new GroupItem();
 			item.resultCount = detailList.size();
 			item.filterHint =  getDisplayDistance(conditions[conditions.length-1]) + "以外的信息";
 			groups.add(item);
 			Log.d("LIST", "group:" +item.filterHint + "(" + item.resultCount + " of " + ls.size());
 		}
 		
 		return groups;
 	}
 	
 	private static String getDisplayDistance(int distance)
 	{
 		
 		String unit = "米";
 		String number = distance + "";
 
 		if (distance > 1000)
 		{
 			unit = "公里";
 			int kilo_number = (int)(distance/1000);
 			int fractor_kilo_number = (int)((distance-(kilo_number*1000))/100);
 			number = ""+kilo_number+"."+fractor_kilo_number;
 		}
 		
 		return number + unit;
 		
 	}
 	
 	
 
 	public static void startSelect(Context context, CustomizeItem[] customizeItems, Filterss fss, final FilterSelectListener listener)
 	{
 		String title = "选择" + fss.getDisplayName();
 		
 		final int skipCount = customizeItems == null ? 0 : customizeItems.length;
 		
 		final ArrayList<MultiLevelSelectionFragment.MultiLevelItem> items = new ArrayList<MultiLevelSelectionFragment.MultiLevelItem>();
 		MultiLevelSelectionFragment.MultiLevelItem head = new MultiLevelSelectionFragment.MultiLevelItem();
 		head.txt = "所有" + fss.getDisplayName();
 		head.id = "";
 		items.add(head);
 		if (skipCount != 0)
 		{
 			for (int i=0; i<skipCount; i++)
 			{
 				items.add(customizeItems[i]);
 			}
 		}
 		
 		for (int i = 0; i < fss.getLabelsList()
 				.size(); ++i) {
 			MultiLevelSelectionFragment.MultiLevelItem t = new MultiLevelSelectionFragment.MultiLevelItem();
 			t.txt = fss.getLabelsList().get(i)
 					.getLabel();
 			t.id = fss.getValuesList().get(i)
 					.getValue();
 			items.add(t);
 		}
 		AlertDialog.Builder builder = new AlertDialog.Builder(context);
 		builder.setTitle(title).setItems(getItemList(items), new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int which) {
 				if (which >= 0 && which < items.size())
 				{
 					listener.onItemSelect(items.get(which));
 				}
 			}
 		}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
 			
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				listener.onCancel();
 			}
 		});
 		builder.setCancelable(true);
 		builder.setOnCancelListener(new OnCancelListener() {
 			
 			@Override
 			public void onCancel(DialogInterface dialog) {
 				listener.onCancel();
 			}
 		});
 		
 		builder.create().show();
 	}
 	
 	private static CharSequence[] getItemList(List<MultiLevelItem> items)
 	{
 		CharSequence[] list = new CharSequence[items.size()];
 		
 		int i=0;
 		for (MultiLevelItem item : items)
 		{
 			list[i++] = item.txt;
 		}
 		
 		return list;
 	}
 	
 	public static void updateFilter(PostParamsHolder holder, MultiLevelItem item, String name)
 	{
 		if (item == null || name == null)
 		{
 			return;
 		}
 		
 		if(item.id != null 
 				&&!item.id.equals("")){
 			holder.put(name, item.txt, item.id);
 		}
 		else{
 			holder.remove(name);
 		}
 		
 	}
 	
 	
 	public static void loadFilterBar(List<Filterss> filters, PostParamsHolder paramsHolder, View[] actionItems)
 	{
 
 		final int MAX = actionItems.length;
 		int added = 0;
 		for (int i=0; i<filters.size() && added < MAX;i++ )
 		{
 			Filterss f = filters.get(i);
 			if (f.getControlType().equals("select"))
 			{
 				View item = actionItems[added];//filterParent.getChildAt(added); //(View) inflator.inflate(R.layout.filter_item, null);
 				item.setTag(f);
 				item.setVisibility(View.VISIBLE);
 				TextView text = (TextView) item.findViewById(R.id.filter_name);
 				initFilterLable(text, paramsHolder, f);
 				added++;
 			}
 		}
 		
 		while (added < MAX)
 		{
 			actionItems[added].setVisibility(View.GONE);
 			added++;
 		}
 	}
 	
 	public static void updateFilterLabel(View[] actionItems, String label, Filterss f)
 	{
 		for (View v : actionItems)
 		{
 			Object tag = v.getTag();
 			if (tag instanceof Filterss && f.getName().equals( ((Filterss) tag).getName()))
 			{
 				TextView text = (TextView) v.findViewById(R.id.filter_name);
 				text.setText(label);
 				break;
 			}
 		}
 	}
 	
 	private static void initFilterLable(TextView text, PostParamsHolder holder, Filterss filter)
 	{
 		if (holder != null
 				&& holder.containsKey(filter
 						.getName())) {
 			String preValue = holder.getData(filter
 					.getName());
 			boolean valid = false;
 			String label = holder.getUiData(filter.getName());
 			if (label != null) {
 				text.setText(label);
 				valid = true;
 			}
 			if (!valid) {
 				List<values> values = filter.
 						getValuesList();
 				for (int z = 0; z < values.size(); ++z) {
 					if (values.get(z).getValue()
 							.equals(preValue)) {
 						text.setText(filter
 								.getLabelsList().get(z)
 								.getLabel());
 						valid = true;
 						break;
 					}
 				}
 
 				if (!valid) {
 					text.setText("所有" + filter.getDisplayName());
 				}
 			}
 		} else {
 			text.setText("所有" + filter.getDisplayName());
 		}
 	}
 	
 	
 	
 }
