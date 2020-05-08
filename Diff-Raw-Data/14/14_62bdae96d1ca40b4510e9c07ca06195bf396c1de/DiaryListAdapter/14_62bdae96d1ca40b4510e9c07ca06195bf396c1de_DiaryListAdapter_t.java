 package com.yvelabs.timerecording;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Context;
 import android.text.Html;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 import com.yvelabs.timerecording.utils.DateUtils;
 import com.yvelabs.timerecording.utils.LogUtils;
 import com.yvelabs.timerecording.utils.TypefaceUtils;
 
 public class DiaryListAdapter extends BaseAdapter {
 	private List<List<EventRecordModel>> recordList;
 	private Context context;
 	private LayoutInflater mInflater;
 	
 	static class ViewHolder {
 		private TextView eventDateTv;
 		private TextView contentTv;
 	}
 	
 	public DiaryListAdapter (Context context, List<List<EventRecordModel>> recordList) {
 		this.context = context;
 		this.recordList = recordList;
 		this.mInflater = LayoutInflater.from(context);
 	}
 
 	@Override
 	public int getCount() {
 		return recordList == null ? 0 : recordList.size();
 	}
 
 	@Override
 	public Object getItem(int arg0) {
 		return recordList == null ? null : recordList.get(arg0);
 	}
 
 	@Override
 	public long getItemId(int position) {
 		return position;
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		View rowView = convertView;
 		List<EventRecordModel> modelList = recordList.get(position);
 		
 		if (rowView == null) {
 			rowView = mInflater.inflate(R.layout.diray_list_item, null);
 			ViewHolder holder = new ViewHolder();
 			holder.eventDateTv = (TextView) rowView.findViewById(R.id.diary_list_item_event_date);
 			holder.contentTv = (TextView) rowView.findViewById(R.id.diary_list_item_content_tv);
 			rowView.setTag(holder);
 		}
 		
 		ViewHolder holder = (ViewHolder) rowView.getTag();
 		
 		if (modelList.size() > 0) {
 			new TypefaceUtils().setTypeface(holder.eventDateTv, TypefaceUtils.MOBY_MONOSPACE);
 			holder.eventDateTv.setText(DateUtils.format(modelList.get(0).getEventDate(), DateUtils.DEFAULT_DATE_PATTERN));
 			
 			holder.contentTv.setText(Html.fromHtml(getDiaryContent(modelList)));
 			
 			//getDiaryContent(modelList)
 		}
 		
 		return rowView;
 	}
 	
 	private String getDiaryContent (List<EventRecordModel> modelList) {
 		StringBuilder contentStr = new StringBuilder();
 		List<List<EventRecordModel>> categoryList = getCategoryList(modelList);
 		
 		for (List<EventRecordModel> subList : categoryList) {
 			long totalCategoryUsingTime = 0;
 			if (subList.size() > 0) {
 				contentStr.append("<big><b>").append(subList.get(0).getEventCategoryName()).append("</b></big><br>");
 				for (EventRecordModel model : subList) {
 					contentStr.append("");
 					contentStr.append(model.getEventName()).append("");
 					contentStr.append("<i>( ").append(DateUtils.getTime(model.getUseingTime())).append(" )</i>").append("<br>");
 					if (model.getSummary() != null && model.getSummary().length() > 0) {
 						contentStr.append("");
 						contentStr.append("<small>").append(model.getSummary()).append("</small><br>");
 					}
 					totalCategoryUsingTime = totalCategoryUsingTime + model.getUseingTime();
 				}
 				contentStr.append("<i><strong><font color='#717174'>").append(context.getString(R.string.total_using_time)).append(" ");
 				contentStr.append(DateUtils.getTime(totalCategoryUsingTime)).append("</font></strong></i><br>");
 			}
 			
 		}
 		return contentStr.substring(0, contentStr.length() - 4);
 	}
 	
 	private List<List<EventRecordModel>> getCategoryList (List<EventRecordModel> modelList) {
 		List<List<EventRecordModel>> list = new ArrayList<List<EventRecordModel>>();
 		
		
 		for (EventRecordModel originalModel : modelList) {
			boolean existCategoryFlag = false; //exist category
 			for (List<EventRecordModel> subList : list) {
 				if (subList.size() > 0 && 
 						subList.get(0).getEventCategoryName().equals(originalModel.getEventCategoryName())) {
 					subList.add(originalModel);
 					existCategoryFlag = true;
 					break;
 				}
 			}
 			
 			if (existCategoryFlag == false) {
 				List<EventRecordModel> subList = new ArrayList<EventRecordModel>();
 				subList.add(originalModel);
 				list.add(subList);
 			}
 		}
 		
 		return list;
 	}
 	
 
 }
