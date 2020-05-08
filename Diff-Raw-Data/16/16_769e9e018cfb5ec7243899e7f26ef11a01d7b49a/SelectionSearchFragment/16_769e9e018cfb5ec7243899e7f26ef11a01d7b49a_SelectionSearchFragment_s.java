 package com.quanleimu.view.fragment;
 
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Context;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.AdapterView;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.AdapterView.OnItemClickListener;
 
 import com.quanleimu.activity.BaseFragment;
 import com.quanleimu.activity.BaseFragment.TabDef;
 import com.quanleimu.activity.BaseFragment.TitleDef;
 import com.quanleimu.activity.R;
 import com.quanleimu.adapter.BXAlphabetSortableAdapter;
 import com.quanleimu.adapter.CheckableAdapter;
 import com.quanleimu.adapter.CommonItemAdapter;
 import com.quanleimu.adapter.BXAlphabetSortableAdapter.BXHeader;
 import com.quanleimu.adapter.BXAlphabetSortableAdapter.BXPinyinSortItem;
 import com.quanleimu.adapter.CheckableAdapter.CheckableItem;
 
 public class SelectionSearchFragment extends BaseFragment implements View.OnClickListener{
 	public static final int MSG_SELECTIONVIEW_BACK = 0x00000011;
 	//定义控件
 	private Button btnCancel;
 	private EditText etSearch;
 	private ListView lvSearchResult;
 	private BXAlphabetSortableAdapter adapter;
 	private static final int MSG_DOFILTER = 1;
 	
 	
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		List<? extends Object> selections = (List) getArguments().getSerializable("selections");
 		boolean hasNextLevel = getArguments().getBoolean("hasNextLevel", false);
 		
 		if(hasNextLevel){
 			adapter = new CommonItemAdapter(getContext(), selections, 10, false);
 		}
 		else{
 			adapter = new CheckableAdapter(getContext(), (List<CheckableItem>)selections, 10, false);
 		}
 	}
 
 
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		View v = inflater.inflate(R.layout.search, null);
 	
 		btnCancel = (Button)v.findViewById(R.id.btnCancel);
 		btnCancel.setText("取消");
 		
 		etSearch = (EditText)v.findViewById(R.id.etSearch);
 		etSearch.setFocusableInTouchMode(true);
 		etSearch.addTextChangedListener(new TextWatcher(){
 			@Override
 			public void afterTextChanged (Editable s){
 				
 			}
 			
 			public void beforeTextChanged (CharSequence s, int start, int count, int after){
 				
 			}
 			
 			public void onTextChanged (CharSequence s, int start, int before, int count){
 				sendMessage(MSG_DOFILTER, null);
 			}
 		});
 
 		
 		lvSearchResult = (ListView) v.findViewById(R.id.lvSearchHistory);
 		lvSearchResult.setDivider(null);
 		lvSearchResult.setOnItemClickListener(new OnItemClickListener(){
 			
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View arg1,
 					int arg2, long arg3) {
 				if(adapter.getItem(arg2) instanceof BXHeader) return;
 				
 				BXPinyinSortItem item = (BXPinyinSortItem)adapter.getItem(arg2);
 //				if(m_viewInfoListener != null){
 //					m_viewInfoListener.onBack(MSG_SELECTIONVIEW_BACK, item.obj);
 //				}
 				finishFragment(MSG_SELECTIONVIEW_BACK, item.obj);
 			}
 		});
 		lvSearchResult.setAdapter(adapter);
 		btnCancel.setOnClickListener(this);
 		
 		return v;
 	} 
 	
 	public void onResume(){
 		super.onResume();
 		//QuanleimuApplication.getApplication().setActivity_type("search");
 		
 		etSearch.postDelayed(new Runnable(){
 			@Override
 			public void run(){
 				etSearch.requestFocus();
 				InputMethodManager inputMgr = 
 						(InputMethodManager) SelectionSearchFragment.this.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
 				inputMgr.showSoftInput(etSearch, InputMethodManager.SHOW_FORCED);
 //				if(!inputMgr.isActive())
 //					inputMgr.toggleSoftInput(0, 0);
 			}			
 		}, 100);
 	}
 	
 	public void initTitle(TitleDef title){
 		title.m_visible = false;
 	}
 	
 	public void initTab(TabDef tab){
 		tab.m_visible = false;
 	}
 	
 
 
 	@Override
 	public void onClick(View v) {
 		switch(v.getId())
 		{
 			case R.id.btnCancel:
 //				if(m_viewInfoListener != null){
 //					m_viewInfoListener.onBack();
 //				}
 				finishFragment();
 				break;
 		}
 	}
 
 
 
 	@Override
 	protected void handleMessage(Message msg, Activity activity, View rootView) {
 
 		switch (msg.what) {
 		case MSG_DOFILTER:
 			adapter.doFilter(etSearch.getText().toString());
 			break;
 		case 2:
 
 			break;
 		}
 	
 	}
 			
 	
 	
 }
