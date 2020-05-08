 package com.quanleimu.view;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener; 
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.LinearLayout.LayoutParams;
 import android.widget.BaseAdapter;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.quanleimu.activity.QuanleimuApplication;
 import com.quanleimu.activity.R;
 import com.quanleimu.activity.R.drawable;
 import com.quanleimu.activity.R.id;
 import com.quanleimu.activity.R.layout;
 import com.quanleimu.entity.Filterss;
 
 public class SiftOptionListView extends BaseView {
 
 	public int temp = -1;
 	public List<Filterss> listFilterss = new ArrayList<Filterss>();
 	public ListView lv;
 	
 	Bundle bundle = null;
 	
 	protected void Init(){
 		LayoutInflater inflater = LayoutInflater.from(getContext());
 		this.addView(inflater.inflate(R.layout.siftlist, null));
 		
 		temp = bundle.getInt("temp");
 		
 		lv = (ListView) findViewById(R.id.lv_test);
 		if(listFilterss != null && listFilterss.size() != 0)
 		{
 			lv.setAdapter(new ItemList());
 		}
 		
 		lv.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
 					long arg3) {
 				
 				if(null != m_viewInfoListener){
 					if(arg2 != 0){
 						bundle.putString("value", listFilterss.get(temp)
 								.getValuesList().get(arg2-1).getValue());
 						// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 						bundle.putString("label", listFilterss.get(temp)
 								.getLabelsList().get(arg2-1).getLabel());
 					}else{
 						bundle.putString("all", "不限");
 					}
 					
 					m_viewInfoListener.onBack(1234, bundle);
 				}
 			}
 		});
 	}
 	
 	public SiftOptionListView(Context context, Bundle bundle_){
 		super(context);
 		
 		bundle = bundle_;
 		
 		listFilterss = QuanleimuApplication.getApplication().getListFilterss();
 		
 		Init();
 	}
 
 	@Override
 	public TitleDef getTitleDef(){
 		TitleDef title = new TitleDef();
 		title.m_visible = true;
 		title.m_title = bundle.getString("title");
 		title.m_leftActionHint = bundle.getString("back"); 
 		return title;
 	}
 	
 	@Override
 	public TabDef getTabDef(){
 		TabDef tab = new TabDef();
 		tab.m_visible = false;
 		return tab;
 	}
 
 
 	class ItemList extends BaseAdapter {
 
 		@Override
 		public int getCount() {
 			// TODO Auto-generated method stub
 			return listFilterss.get(temp).getLabelsList().size()+1;
 		}
 
 		@Override
 		public Object getItem(int position) {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public long getItemId(int position) {
 			// TODO Auto-generated method stub 
 			return 0;
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_siftlist,	null);
 			
 			if(position==0){ 
 				convertView.setBackgroundResource(R.drawable.btn_top_bg);
 			}else if(position>listFilterss.get(temp).getLabelsList().size()-1){
 //				convertView.setBackgroundResource(R.drawable.btn_m_bg);
 				convertView.setBackgroundResource(R.drawable.btn_down_bg);
 			}else{
 				convertView.setBackgroundResource(R.drawable.btn_m_bg);
 			}
 			convertView.setPadding(10, 10, 10, 10);
 			
 			TextView txts = (TextView) convertView
 			.findViewById(R.id.siftlisttxt);
 			if(position == 0){
 				txts.setText("不限");
 			}else{
 				txts.setText(listFilterss.get(temp).getLabelsList().get(position-1)
 						.getLabel());
 			}
 			
 			
 			return convertView;
 		}
 
 	}
 
 	
 }
 
