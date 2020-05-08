 package com.jalinsuara.android.search;
 
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.text.Html;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.TextView;
 
 import com.jalinsuara.android.R;
 import com.jalinsuara.android.helper.DateUtils;
 import com.jalinsuara.android.helpers.lazylist.ImageLoader;
 
 public class SearchResultAdapter extends BaseAdapter {
 
 	private ArrayList<SearchResult> mList;
 	private ImageLoader mImageLoader;
 	private Context mContext;
 	private LayoutInflater inflater;
 
 	public SearchResultAdapter(Context context, ArrayList<SearchResult> list) {
 		mList = list;
 		mImageLoader = new ImageLoader(context);
 		mContext = context;
 		inflater = (LayoutInflater) context
 				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 	}
 
 	@Override
 	public int getCount() {
 		return mList.size();
 	}
 
 	@Override
 	public Object getItem(int arg0) {
 		return mList.get(arg0);
 	}
 
 	@Override
 	public long getItemId(int position) {
 		return mList.get(position).getId();
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		SearchResult object = mList.get(position);
 		if (convertView == null) {
 			if (object.isNews()) {
 				convertView = inflater.inflate(R.layout.list_item_news, null);
 			} else {
 				convertView = inflater.inflate(R.layout.list_item_sub_project,
 						null);
 			}
 
 		} else {
 			SearchResult obj = (SearchResult) convertView.getTag();
 			if (obj.isNews() == object.isNews()) {
 
 			} else {
 				if (object.isNews()) {
 					convertView = inflater.inflate(R.layout.list_item_news,
 							null);
 				} else {
 					convertView = inflater.inflate(
 							R.layout.list_item_sub_project, null);
 				}
 			}
 		}
 
 		convertView.setTag(object);
 		if (object.isNews()) {
 			TextView titleTextview = ((TextView) convertView
 					.findViewById(R.id.list_item_news_title_textview));
 			TextView descriptionTextview = ((TextView) convertView
 					.findViewById(R.id.list_item_news_content_textview));
 			TextView dateTextView = ((TextView) convertView
 					.findViewById(R.id.list_item_news_date_textview));
 
 			dateTextView.setText(DateUtils.toStringDateOnly(object.getNews()
 					.getUpdatedAt()));
 			titleTextview.setText(object.getNews().getTitle());
//			descriptionTextview.setText(Html.fromHtml(object.getNews()
//					.getDescription()));
 		} else {
 			TextView titleTextview = ((TextView) convertView
 					.findViewById(R.id.list_item_sub_project_title_textview));
 //			TextView descriptionTextview = ((TextView) convertView
 //					.findViewById(R.id.list_item_sub_project_content_textview));
 
 			titleTextview.setText(object.getProjects().getName());
 		}
 
 		return convertView;
 	}
 }
