 package com.quanleimu.view.fragment;
 
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Context;
 import android.os.Bundle;
 import android.os.Message;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.util.Pair;
 import android.view.Gravity;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.LinearLayout.LayoutParams;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.quanleimu.activity.BaseFragment;
 import com.quanleimu.activity.QuanleimuApplication;
 import com.quanleimu.activity.R;
 import com.quanleimu.adapter.CommonItemAdapter;
 import com.quanleimu.entity.FirstStepCate;
 import com.quanleimu.entity.SecondStepCate;
 import com.quanleimu.jsonutil.JsonUtil;
 import com.quanleimu.util.Communication;
 import com.quanleimu.util.Helper;
 
 public class SearchFragment extends BaseFragment {
 
 	private static final int REQ_SELECT_SEARCH_CATEGORY = 147;
 	private static final int MSG_SEARCH_RESULT = 1;
 
 	private EditText etSearch;
 	private TextView tvClear;
 	private ListView lvSearchHistory;
 	private View searchResult;
 	private ListView lvSearchResultList;
 	private View noResultView;
 	
 	
 
 	/**
 	 * 设置布局显示为目标有多大就多大
 	 */
 	private LayoutParams WClayoutParams = new LinearLayout.LayoutParams(
 			LinearLayout.LayoutParams.WRAP_CONTENT,
 			LinearLayout.LayoutParams.WRAP_CONTENT);
 	/**
 	 * 设置布局显示目标最大化
 	 */
 	private LayoutParams FFlayoutParams = new LinearLayout.LayoutParams(
 			LinearLayout.LayoutParams.FILL_PARENT,
 			LinearLayout.LayoutParams.FILL_PARENT);
 
 	// 定义变量
 	private FirstStepCate firstStepCategory;
 	
 	public String searchContent = "";
 
 	List<Pair<SecondStepCate, Integer>> categoryResultCountList;
 
 	private List<String> listRemark = new ArrayList<String>();
 
 	protected void initTitle(TitleDef title) {
 		title.m_visible = true;
 		title.m_titleControls = LayoutInflater.from(getActivity()).inflate(
 				R.layout.title_search, null);
 		etSearch = (EditText) title.m_titleControls.findViewById(R.id.etSearch);
 		title.m_rightActionHint = "取消";
 
 		etSearch.setOnKeyListener(new View.OnKeyListener() {
 
 			@Override
 			public boolean onKey(View v, int keyCode, KeyEvent event) {
 				if (keyCode == KeyEvent.KEYCODE_ENTER) {
 					SearchFragment.this.doSearch();
 					return true;
 				} else if (keyCode == KeyEvent.KEYCODE_BACK)
 				{
 					if (categoryResultCountList != null && categoryResultCountList.size() > 0)
 						SearchFragment.this.showSearchResult(false);
 					else
 						SearchFragment.this.showSearchHistory();
 					return false;
 				}else
 				{
 					return false;
 				}
 			}
 		});
 		
 		etSearch.addTextChangedListener(new TextWatcher() {
 			
 			@Override
 			public void onTextChanged(CharSequence s, int start, int before, int count) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public void beforeTextChanged(CharSequence s, int start, int count,
 					int after) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public void afterTextChanged(Editable s) {
 				if (s.length() == 0)
 					SearchFragment.this.getTitleDef().m_rightActionHint = "取消";
 				else
 					SearchFragment.this.getTitleDef().m_rightActionHint = "搜索";
 				SearchFragment.this.refreshHeader();
 			}
 		});
 		
 		etSearch.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				SearchFragment.this.showSearchHistory();
 			}
 		});;
 		
 	}
 
 	public void initTab(TabDef tab) {
 		tab.m_visible = false;
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 
 		View rootV = inflater.inflate(R.layout.search, null);
 
 		// 通过ID获取控件
 		lvSearchHistory = (ListView) rootV.findViewById(R.id.lvSearchHistory);
 		lvSearchResultList = (ListView) rootV
 				.findViewById(R.id.lvSearchResultList);
 		searchResult = rootV.findViewById(R.id.searchResult);
 		noResultView = rootV.findViewById(R.id.noResultView);
 
 		// 添加自定义布局
 		LinearLayout layout = new LinearLayout(getActivity());
 		layout.setOrientation(LinearLayout.HORIZONTAL);
 		
 		tvClear = new TextView(getActivity());
 		tvClear.setTextSize(22);
 		tvClear.setText("清除历史记录");
 		tvClear.setGravity(Gravity.CENTER_VERTICAL);
 		layout.addView(tvClear, FFlayoutParams);
 		layout.setGravity(Gravity.CENTER);
 
 		LinearLayout loadingLayout = new LinearLayout(getActivity());
 		loadingLayout.addView(layout, WClayoutParams);
 		loadingLayout.setGravity(Gravity.CENTER);
 		lvSearchHistory.addFooterView(loadingLayout);
 		
 		lvSearchHistory.setVisibility(View.GONE);
 		lvSearchHistory.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View arg1,
 					int arg2, long arg3) {
 				if (arg2 <= (listRemark.size() - 1)) {
 					searchContent = listRemark.get(arg2);
 					etSearch.setText(searchContent);
 					etSearch.setSelection(searchContent.length(), searchContent.length());
 					showSearchResult(true);
 				} else {
 					listRemark.clear();
 					QuanleimuApplication.getApplication().setListRemark(
 							listRemark);
 					lvSearchHistory.setVisibility(View.GONE);
 					tvClear.setVisibility(View.GONE);
 				}
 			}
 		});		
 		
 		tvClear.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				listRemark.clear();
 				QuanleimuApplication.getApplication().setListRemark(listRemark);
 				lvSearchHistory.setVisibility(View.GONE);
 				v.setVisibility(View.GONE);
 
 				// 将搜索记录保存本地
 				Helper.saveDataToLocate(getActivity(), "listRemark", listRemark);
 			}
 		});
 
 		listRemark = QuanleimuApplication.getApplication().getListRemark();
 
 		lvSearchResultList.setOnItemClickListener(new OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
 					long arg3) {
 				SecondStepCate cate = (SecondStepCate) arg1.getTag();
 
 				Bundle bundle = new Bundle();
 				bundle.putString("searchContent", searchContent);
 				bundle.putString("actType", "search");
 				bundle.putString("name", "");
 				bundle.putString("categoryEnglishName", cate.getEnglishName());
 				pushFragment(new GetGoodFragment(), bundle);
 			}
 		});
 		
 		return rootV;
 	}
 	
 	@Override
 	public void onStackTop(boolean isBack) {
 
 		if (isBack)
 		{
 			etSearch.setText(searchContent);
 			this.showSearchResult(false);
 		}
 		else
 		{		
 			this.showSearchHistory();
 			etSearch.postDelayed(new Runnable(){
 				@Override
 				public void run(){
 					if (etSearch != null)
 					{
 						etSearch.requestFocus();
 						InputMethodManager inputMgr = 
 								(InputMethodManager) etSearch.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
 						inputMgr.showSoftInput(etSearch, InputMethodManager.SHOW_IMPLICIT);
 					}
 				}			
 			}, 100);
 		}
 		
 	}
 
 	private void showSearchHistory() {
 		if (listRemark != null && listRemark.size() != 0) {
 			searchResult.setVisibility(View.GONE);
 			noResultView.setVisibility(View.GONE);			
 			lvSearchHistory.setVisibility(View.VISIBLE);
 			tvClear.setVisibility(View.VISIBLE);
 			
 			CommonItemAdapter adapter = new CommonItemAdapter(getActivity(),
 					listRemark, 0x1FFFFFFF, false);
 			adapter.setHasArrow(false);
 			lvSearchHistory.setAdapter(adapter);
 		}
 	}
 
 	@Override
 	public void onPause() {
 		InputMethodManager inputMgr = (InputMethodManager) getActivity()
 				.getSystemService(Context.INPUT_METHOD_SERVICE);
 		inputMgr.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
 
 		super.onPause();
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		if (null == searchContent || searchContent.length() == 0) {
 			etSearch.postDelayed(new Runnable() {
 				@Override
 				public void run() {
 					etSearch.requestFocus();
 				}
 			}, 300);
 		}
 	}
 
 	@Override
 	protected void onFragmentBackWithData(int requestCode, Object result) {
 		if (requestCode == REQ_SELECT_SEARCH_CATEGORY) {
 			firstStepCategory = (FirstStepCate) result;
 			showSearchResult(true);
 		}
 	}
 
 	@Override
 	public void handleRightAction() {
 		if (etSearch.getText().length() > 0)
 			this.doSearch();
 		else
 			this.finishFragment();
 	}
 
 	private void doSearch() {
 		searchContent = etSearch.getText().toString().trim();
 		if (searchContent.equals("")) {
 			Toast.makeText(getActivity(), "搜索内容不能为空", Toast.LENGTH_SHORT)
 					.show();
 		} else {
 			addToListRemark(searchContent);
 			showSearchResult(true);
 		}
 	}
 
 	/**
 	 * 
 	 */
 	private void showSearchResult(boolean search) {
 		lvSearchHistory.setVisibility(View.GONE);
 		this.hideSoftKeyboard();
 		if (search)
 		{
 			this.showProgress("消息", "搜索中...", false);			
 			new Thread(new SearchCategoryListThread()).start();
 		}
 		else
 		{
 			this.sendMessage(MSG_SEARCH_RESULT, categoryResultCountList);
 		}
 	}
 
 	/**
 	 * @param searchContent
 	 * 
 	 */
 	private void addToListRemark(String searchContent) {
 		if (listRemark == null || listRemark.size() == 0) {
 			listRemark = new ArrayList<String>();
 			listRemark.add(searchContent);
 		} else if (!listRemark.contains(searchContent)) {
 			listRemark.add(searchContent);
 		}
 		QuanleimuApplication.getApplication().setListRemark(listRemark);
 		// 将搜索记录保存本地
 		Helper.saveDataToLocate(getActivity(), "listRemark", listRemark);
 	}
 
 	@Override
 	protected void handleMessage(Message msg, Activity activity, View rootView) {
 		switch (msg.what) {
 		case MSG_SEARCH_RESULT:
 			if (categoryResultCountList == null || categoryResultCountList.isEmpty())
 			{
 				noResultView.setVisibility(View.VISIBLE);
 				searchResult.setVisibility(View.GONE);
 			}
 			else
 			{
 				searchResult.setVisibility(View.VISIBLE);
 				noResultView.setVisibility(View.GONE);
 
 				ResultListAdapter adapter = new ResultListAdapter(activity,
 						R.layout.item_categorysearch, categoryResultCountList);
 				lvSearchResultList.setAdapter(adapter);
 			}
 			this.hideProgress();
 			break;
 		}
 	}
 	
 	
 
 	class SearchCategoryListThread implements Runnable {
 		@Override
 		public void run() {
 			String apiName = "ad_search";
 			List<String> parameters = new ArrayList<String>();
 			if (SearchFragment.this.firstStepCategory != null) 
 			{
 				parameters.add("categoryEnglishName="
 						+ SearchFragment.this.firstStepCategory
 								.getEnglishName());
 			}
 			parameters.add("query="
 					+ URLEncoder.encode(SearchFragment.this.searchContent));
 //			parameters.add("timestamp="+new Date().getTime());
 			String apiUrl = Communication.getApiUrl(apiName, parameters);
 			try {
 				String json = Communication.getDataByUrl(apiUrl, false);
 				categoryResultCountList = JsonUtil
 						.parseAdSearchCategoryCountResult(json);
 			} catch (Exception e) {
 				categoryResultCountList = null;
				Log.e(TAG, e.getMessage()==null?"网络请求失败":e.getMessage());
 				getActivity().runOnUiThread(new Runnable(){
 					@Override
 					public void run(){
 						Toast.makeText(getActivity(), "网络请求失败,请稍后重试",
 								Toast.LENGTH_SHORT).show();
 					}
 				});
 			}
 			SearchFragment.this.sendMessage(MSG_SEARCH_RESULT,
 					categoryResultCountList);			
 		}
 	}
 
 	class ResultListAdapter extends ArrayAdapter<Pair<SecondStepCate, Integer>> {
 		List<Pair<SecondStepCate, Integer>> objects;
 
 		public ResultListAdapter(Context context, int textViewResourceId,
 				List<Pair<SecondStepCate, Integer>> objects) {
 			super(context, textViewResourceId, objects);
 			this.objects = objects;
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			if (convertView == null) {
 				convertView = LayoutInflater.from(
 						SearchFragment.this.getActivity()).inflate(
 						R.layout.item_categorysearch, null);
 			}
 
 			TextView tvCategoryName = (TextView) convertView
 					.findViewById(R.id.tvCategoryName);
 			TextView tvCategoryCount = (TextView) convertView
 					.findViewById(R.id.tvCategoryCount);
 			Pair<SecondStepCate, Integer> pair = objects.get(position);
 			convertView.setTag(pair.first);
 			tvCategoryName.setText(pair.first.getName());
 			tvCategoryCount.setText(String.format("(%d)",
 					pair.second.intValue()));
 
 			return convertView;
 		}
 	}
 
 }
