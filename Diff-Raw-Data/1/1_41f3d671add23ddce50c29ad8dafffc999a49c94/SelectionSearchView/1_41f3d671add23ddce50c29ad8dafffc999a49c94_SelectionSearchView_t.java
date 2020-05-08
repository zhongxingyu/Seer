 package com.quanleimu.view;
 
 import java.util.List;
 import com.quanleimu.activity.QuanleimuApplication;
 import com.quanleimu.activity.R;
 import android.content.Context;
 import android.os.Handler;
 import android.os.Message;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 
 import com.quanleimu.adapter.BXAlphabetSortableAdapter;
 import com.quanleimu.adapter.CheckableAdapter;
 import com.quanleimu.adapter.BXAlphabetSortableAdapter.BXPinyinSortItem;
 import com.quanleimu.adapter.CheckableAdapter.CheckableItem;
 import com.quanleimu.adapter.CommonItemAdapter;
 public class SelectionSearchView extends BaseView implements View.OnClickListener{
 	public static final int MSG_SELECTIONVIEW_BACK = 0x00000011;
 	//定义控件
 	private Button btnCancel;
 	private EditText etSearch;
 	private ListView lvSearchResult;
 	private BXAlphabetSortableAdapter adapter;
 	private static final int MSG_DOFILTER = 1; 
 			
 	protected void Init(){
 		LayoutInflater inflater = LayoutInflater.from(getContext());
 		this.addView(inflater.inflate(R.layout.search, null));
 	
 		btnCancel = (Button)findViewById(R.id.btnCancel);
 		btnCancel.setText("取消");
 		
 		etSearch = (EditText)findViewById(R.id.etSearch);
 		etSearch.setFocusableInTouchMode(true);
 		etSearch.addTextChangedListener(new TextWatcher(){
 			@Override
 			public void afterTextChanged (Editable s){
 				
 			}
 			
 			public void beforeTextChanged (CharSequence s, int start, int count, int after){
 				
 			}
 			
 			public void onTextChanged (CharSequence s, int start, int before, int count){
 				myHandler.sendEmptyMessage(MSG_DOFILTER);
 			}
 		});
 
 		
 		lvSearchResult = (ListView) findViewById(R.id.lvSearchHistory);
		lvSearchResult.setDivider(null);
 		lvSearchResult.setOnItemClickListener(new OnItemClickListener(){
 			
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View arg1,
 					int arg2, long arg3) {
 				// TODO Auto-generated method stub
 				BXPinyinSortItem item = (BXPinyinSortItem)adapter.getItem(arg2);
 				if(m_viewInfoListener != null){
 					m_viewInfoListener.onBack(MSG_SELECTIONVIEW_BACK, item.obj);
 				}
 			}
 		});
 		lvSearchResult.setAdapter(adapter);
 		btnCancel.setOnClickListener(this);
 	}
 	
 	public SelectionSearchView(Context context, List<? extends Object> selections, boolean hasNextLevel){
 		super(context);
 		
 //		this.selections = new ArrayList<BXPinyinSortItem>();
 
 //		for (int i = 0; i < selections.size(); ++i) {
 //			BXPinyinSortItem item = new BXPinyinSortItem();
 //			item.pinyin = "";
 //			if(selections.get(i).toString().equals("全部")){
 //				continue;
 //			}
 //			else{
 //				for(int j = 0; j < selections.get(i).toString().length(); ++ j){
 //					item.pinyin += " " + BXHanzi2Pinyin.hanziToPinyin(String.valueOf(selections.get(i).toString().charAt(j)));
 //				}
 //			}
 //			item.pinyin = item.pinyin.trim();
 //			item.obj = selections.get(i);
 //			this.selections.add(item);
 //		}
 
 		if(hasNextLevel){
 			adapter = new CommonItemAdapter(getContext(), selections, 10, false);
 		}
 		else{
 			adapter = new CheckableAdapter(getContext(), (List<CheckableItem>)selections, 10, false);
 		}
 		
 		Init();
 	}
 	
 	public void onResume(){
 		QuanleimuApplication.getApplication().setActivity_type("search");
 	}
 	
 	public TitleDef getTitleDef(){
 		TitleDef title = new TitleDef();
 		title.m_visible = false;
 		return title;
 	}
 	
 	public TabDef getTabDef(){
 		TabDef tab = new TabDef();
 		tab.m_visible = false;
 		return tab;
 	}
 	
 
 
 	@Override
 	public void onClick(View v) {
 		switch(v.getId())
 		{
 			case R.id.btnCancel:
 				if(m_viewInfoListener != null){
 					m_viewInfoListener.onBack();
 				}
 				break;
 		}
 	}
 	
 	@Override
 	public void onAttachedToWindow(){
 		super.onAttachedToWindow();
 		etSearch.postDelayed(new Runnable(){
 			@Override
 			public void run(){
 				etSearch.requestFocus();
 				InputMethodManager inputMgr = 
 						(InputMethodManager) SelectionSearchView.this.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
 				inputMgr.showSoftInput(etSearch, InputMethodManager.SHOW_FORCED);
 //				if(!inputMgr.isActive())
 //					inputMgr.toggleSoftInput(0, 0);
 			}			
 		}, 100);
 	}
 	
 	Handler myHandler = new Handler() {
 		@Override
 		public void handleMessage(Message msg) {
 			switch (msg.what) {
 			case MSG_DOFILTER:
 				adapter.doFilter(etSearch.getText().toString());
 				break;
 			case 2:
 	
 				break;
 			}
 			
 			super.handleMessage(msg);
 		}
 	};
 }
