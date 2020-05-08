 package com.quanleimu.view.fragment;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.os.Message;
 import android.util.Pair;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.LinearLayout.LayoutParams;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.quanleimu.activity.BaseActivity;
 import com.quanleimu.activity.BaseFragment;
 import com.quanleimu.activity.QuanleimuApplication;
 import com.quanleimu.activity.R;
 import com.quanleimu.entity.Filterss;
 import com.quanleimu.entity.values;
 import com.quanleimu.jsonutil.JsonUtil;
 import com.quanleimu.util.Communication;
 import com.quanleimu.util.TrackConfig.TrackMobile.Key;
 import com.quanleimu.util.TrackConfig.TrackMobile.PV;
 import com.quanleimu.util.Tracker;
 import com.quanleimu.util.Util;
 
 public class FilterFragment extends BaseFragment implements View.OnClickListener{
 	
 	private static final int MSG_UPDATE_KEYWORD = 3;
 	
 	public List<String> listsize = new ArrayList<String>();
 
 	
 	
 	// 定义变量
 	public String backPageName = "";
 	private EditText ed_sift;
 
 	public int temp;
 	public String res = "";
 	public String value_resl = "";
 	public int idselected;
 //	TextView tvmeta = null;
 
 	private Map<Integer, TextView> selector = new HashMap<Integer, TextView>();
 	private Map<String, EditText> editors = new HashMap<String, EditText>();
 
 	public List<Filterss> listFilterss = new ArrayList<Filterss>();
 
 //	private Map<String, String> labelmap = new HashMap<String, String>();
 //
 //	public Map<String, String> valuemap = new HashMap<String, String>();
 
 	public String categoryEnglishName = "";
 	public String json = "";
 
 	private final int MSG_MULTISEL_BACK = 0;
 	
 	private PostParamsHolder parametersHolder;
 
 	@Override
 	public void onStackTop(boolean isBack) {
 		super.onStackTop(isBack);
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		Bundle bundle = getArguments();
 		categoryEnglishName = bundle.getString("categoryEnglishName");
 		backPageName = bundle.getString(ARG_COMMON_BACK_HINT);
 		
 		PostParamsHolder params = (PostParamsHolder) getArguments().getSerializable("filterResult");
 		getArguments().remove("filterResult");
 		
 		parametersHolder = (PostParamsHolder) getArguments().getSerializable("savedFilter");
 		if (parametersHolder == null)
 		{
 			parametersHolder = new PostParamsHolder();
 		}
 		
 		if (params != null)
 		{
 			parametersHolder.merge(params);
 		}
 		
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		View v = inflater.inflate(R.layout.sifttest, null);
 		
 		ed_sift = (EditText) v.findViewById(R.id.edsift);
 		ed_sift.clearFocus();
 		
 		v.findViewById(R.id.filter_confirm).setOnClickListener(this);
 		v.findViewById(R.id.filter_clear).setOnClickListener(this);
 		
 		return v;
 	}
 	
 	public void onResume()
 	{
 		super.onResume();
 		this.pv = PV.LISTINGFILTER;
 		Tracker.getInstance().pv(this.pv).append(Key.SECONDCATENAME, categoryEnglishName).end();
 		
 		// AND 地区_s:m7259
 		Pair<Long, String> pair = Util
 				.loadJsonAndTimestampFromLocate(
 						getActivity(),
 						"saveFilterss"
 								+ categoryEnglishName
 								+ QuanleimuApplication.getApplication().cityEnglishName);
		json = pair.second;
 		long time = pair.first;
 		if (json == null || json.length() == 0) {
 			showSimpleProgress();
 			new Thread(new GetGoodsListThread(true)).start();
 		} else {
 			if (time + 24 * 3600 < System.currentTimeMillis()/1000) {
 				sendMessage(1, null);
 				showSimpleProgress();
 				
 				new Thread(new GetGoodsListThread(false)).start();
 			} else {
 				// sendMessage(1, null);
 				loadSiftFrame(getView());
 			}
 		}
 	}
 	
 	public void handleRightAction(){
 		finishFragment(requestCode, parametersHolder);
 	}//called when right button on title bar pressed, return true if handled already, false otherwise
 	
 	public void initTitle(TitleDef title){
 		title.m_visible = true;
 		title.m_title = "更多筛选";
 		title.m_leftActionHint = "返回";
 		title.m_rightActionHint = "确定";
 	}
 	public void initTab(TabDef tab){
 		tab.m_visible = false;
 	}
 	
 	public void onPause()
 	{
 		super.onPause();
 		
 		collectValue(getArguments());
 	}
 	
 	
 	
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		// TODO Auto-generated method stub
 		super.onSaveInstanceState(outState);
 	}
 
 	private void collectValue(Bundle bundle)
 	{
 //		String result = "";
 //		String resultLabel = "";
 
 		String str = ed_sift.getText().toString().trim();
 
 		
 		for(int i = 0; i < editors.size(); ++i){
 			String key = editors.keySet().toArray()[i].toString();
 			
 			EditText txtEditor = (EditText)editors.get(key);
 			String textInput = txtEditor.getText().toString();
 			String uiValue = editors.get(key).getTag() != null ? textInput + editors.get(key).getTag() : textInput;
 			if(textInput.length() > 0){
 				parametersHolder.put(key, uiValue, textInput);
 			}
 			else
 			{
 				parametersHolder.remove(key);
 			}
 		}
 
 		
 		if (!str.equals("")) {
 			parametersHolder.put("", str, str);
 		}
 		else
 		{
 			parametersHolder.remove("");
 		}
 		
 	}
 	
 	@Override
 	public void onFragmentBackWithData(int message, Object obj) {
 //		if (message == 1234) {
 //			Bundle data = (Bundle)obj;
 //			
 //			String s = data.getString("all"); 
 //			if(s==null || s.equals("")){
 //				res = data.getString("label");
 //				value_resl = data.getString("value");
 //				
 //				if(temp < listFilterss.size() && listFilterss.get(temp).toString().length() > 0){
 //					valuemap.put(listFilterss.get(temp).getName(), value_resl);
 //				}
 //				selector.get(temp).setText(res);
 //			}else{
 //				//res = datas.getString("label");
 //				//value_resl = datas.getString("value");
 //				
 //				if(temp < listFilterss.size() && listFilterss.get(temp).toString().length() > 0){
 //					valuemap.remove(listFilterss.get(temp).getName());
 //				}
 //				selector.get(temp).setText(s);
 //			}
 //		}
 //		else 
 			if(MSG_MULTISEL_BACK == message){
 			if(obj instanceof MultiLevelSelectionFragment.MultiLevelItem){
 				final String txt = ((MultiLevelSelectionFragment.MultiLevelItem)obj).txt;
 				selector.get(temp).setText(txt);
 				if(((MultiLevelSelectionFragment.MultiLevelItem)obj).id != null 
 						&&!((MultiLevelSelectionFragment.MultiLevelItem)obj).id.equals("")){
 					parametersHolder.put(listFilterss.get(temp).getName(), ((MultiLevelSelectionFragment.MultiLevelItem)obj).txt, ((MultiLevelSelectionFragment.MultiLevelItem)obj).id);
 //					labelmap.put(listFilterss.get(temp).getName(), ((MultiLevelSelectionFragment.MultiLevelItem)obj).txt);
 //					valuemap.put(listFilterss.get(temp).getName(), ((MultiLevelSelectionFragment.MultiLevelItem)obj).id);					
 				}
 				else{
 					if(temp < listFilterss.size() && listFilterss.get(temp).toString().length() > 0){
 //						valuemap.remove(listFilterss.get(temp).getName());
 //						labelmap.remove(listFilterss.get(temp).getName());
 						parametersHolder.remove(listFilterss.get(temp).getName());
 					}					
 				}
 			}
 		}
 	}
 	
 	
 	class GetGoodsListThread implements Runnable {
 		private boolean isUpdate;
 		public GetGoodsListThread(boolean isUpdate){
 			this.isUpdate = isUpdate;
 		}
 		@Override
 		public void run() {
 			String apiName = "category_meta_filter";
 			ArrayList<String> list = new ArrayList<String>();
 
 			list.add("categoryEnglishName=" + categoryEnglishName);
 			list.add("cityEnglishName=" + QuanleimuApplication.getApplication().cityEnglishName);
 
 			String url = Communication.getApiUrl(apiName, list);
 			try {
 				json = Communication.getDataByUrl(url, false);
 				if (json != null) {
 					Util.saveJsonAndTimestampToLocate(FilterFragment.this.getAppContext(), "saveFilterss"+categoryEnglishName+QuanleimuApplication.getApplication().cityEnglishName, json, System.currentTimeMillis()/1000);
 					if(isUpdate){
 						sendMessage(1, null);
 					}
 				} else {
 					sendMessage(2, null);
 				}
 			} catch (UnsupportedEncodingException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			} catch (Communication.BXHttpException e){
 				
 			}
 			
 			hideProgress();
 		}
 	}
 	
 	private void loadSiftFrame(View rootView)
 	{
 		listFilterss = JsonUtil.getFilters(json).getFilterssList();
 //		QuanleimuApplication.getApplication().setListFilterss(listFilterss);
 		LinearLayout ll_meta = (LinearLayout) rootView.findViewById(R.id.meta);
 		LayoutInflater inflater = LayoutInflater.from(rootView
 				.getContext());
 		ll_meta.removeAllViews();
 		if (listFilterss == null || listFilterss.size() == 0) {
 			View v = new View(ll_meta.getContext());
 			v.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, 0));
 			ll_meta.addView(v);
 		} else {
 			for (int i = 0; i < listFilterss.size(); ++i) {
 				if (listFilterss.get(i) == null)
 				{
 					continue;
 				}
 				
 				View v = null;
 				TextView tvmetatxt = null;
 
 				final String paramsKey = listFilterss.get(i).getName();
 				if (listFilterss.get(i).getControlType().equals("select")) {
 					v = inflater.inflate(R.layout.item_filter_select, null);
 					tvmetatxt = (TextView) v.findViewById(R.id.filter_label);
 					tvmetatxt.setText(listFilterss.get(i).getDisplayName());
 
 					TextView tvmeta = (TextView) v.findViewById(R.id.filter_value);
 					tvmeta.setTag(paramsKey);
 					
 					if (parametersHolder.containsKey(paramsKey)) {
 						String preValue = parametersHolder.getData(paramsKey); 
 						String preLabel= parametersHolder.getUiData(paramsKey); 
 						boolean valid = false;
 						if (preLabel != null) {
 							tvmeta.setText(preLabel);
 							valid = true;
 						}
 						if (!valid) {
 							List<values> values = listFilterss.get(i)
 									.getValuesList();
 							for (int z = 0; z < values.size(); ++z) {
 								if (values.get(z).getValue()
 										.equals(preValue)) {
 									tvmeta.setText(listFilterss.get(i)
 											.getLabelsList().get(z)
 											.getLabel());
 									valid = true;
 									break;
 								}
 							}
 
 							if (!valid) {
 								tvmeta.setText("请选择");
 							}
 						}
 					} else {
 						tvmeta.setText("请选择");
 					}
 					selector.put(i, tvmeta);
 
 					v.setTag(i);
 
 					v.setOnClickListener(new OnClickListener() {
 						@Override
 						public void onClick(View v) {
 							temp = Integer.parseInt(v.getTag().toString());
 							Bundle bundle = createArguments(null, null);
 							bundle.putAll(getArguments());
 							bundle.putInt("temp", temp);
 							bundle.putString("title", listFilterss
 									.get(temp).getDisplayName());
 							bundle.putString("back", "筛选");
 
 							// if(null != m_viewInfoListener){
 							// m_viewInfoListener.onNewView(new
 							// SiftOptionListView(getContext(), bundle));
 							// }
 							Filterss fss = listFilterss.get(temp);
 							if (fss.getLevelCount() > 0) {
 								String selectedValue = parametersHolder.getData(fss.getName());
 								bundle.putString("selectedValue", selectedValue);
 								
 								ArrayList<MultiLevelSelectionFragment.MultiLevelItem> items = new ArrayList<MultiLevelSelectionFragment.MultiLevelItem>();
 								MultiLevelSelectionFragment.MultiLevelItem head = new MultiLevelSelectionFragment.MultiLevelItem();
 								head.txt = "全部";
 								head.id = "";
 								items.add(head);
 								for (int i = 0; i < fss.getLabelsList()
 										.size(); ++i) {
 									MultiLevelSelectionFragment.MultiLevelItem t = new MultiLevelSelectionFragment.MultiLevelItem();
 									t.txt = fss.getLabelsList().get(i)
 											.getLabel();
 									t.id = fss.getValuesList().get(i)
 											.getValue();
 									items.add(t);
 								}
 								
 								bundle.putInt(ARG_COMMON_REQ_CODE,
 										MSG_MULTISEL_BACK);
 								bundle.putSerializable("items", items);
 								bundle.putInt("maxLevel",
 										fss.getLevelCount() - 1);
 								((BaseActivity) getActivity())
 										.pushFragment(
 												new MultiLevelSelectionFragment(),
 												bundle, false);
 							}
 						}
 					});
 
 				}
 				// else
 				// if(listFilterss.get(i).getControlType().equals(""))
 				else {
 					v = inflater.inflate(R.layout.item_filter_edit, null);
 					tvmetatxt = (TextView) v.findViewById(R.id.filter_label);
 					tvmetatxt.setText(listFilterss.get(i).getDisplayName());
 					EditText tvmeta = (EditText) v.findViewById(R.id.filter_input);
 					
 					TextView unitmeta = (TextView) v.findViewById(R.id.filter_unit);
 					String unitS = listFilterss.get(i).getUnit();
 					if (unitS != null)
 					{
 						unitmeta.setText(unitS);
 						tvmeta.setTag(unitS);
 					}
 					
 					final String key = listFilterss.get(i).getName();
 					editors.put(key,
 							(EditText) tvmeta);
 					String preValue = parametersHolder.getData(paramsKey);
 					if (preValue != null) {
 						tvmeta.setText(preValue);
 //						valuemap.put(listFilterss.get(i).getName(),
 //								preValue);
 					}
 				}
 //				TextView border = new TextView(
 //						rootView.getContext());
 				View border = new View(rootView.getContext());
 				border.setLayoutParams(new LayoutParams(
 						LayoutParams.FILL_PARENT, getResources().getDimensionPixelSize(R.dimen.filter_gap), 1));
 //				border.setBackgroundResource(R.drawable.list_divider);
 
 				ll_meta.addView(v);
 				ll_meta.addView(border);
 			}
 
 
 		}
 		
 		String keyWords = parametersHolder.getData("");
 		if (keyWords != null) {
 //				((TextView) SiftFragment.this.findViewById(R.id.edsift))
 //						.setText(keyWords);
 			sendMessage(MSG_UPDATE_KEYWORD, keyWords);
 		}
 	}
 	
 
 	@Override
 	protected void handleMessage(Message msg, Activity activity, View rootView) {
 
 		switch (msg.what) {
 		case 1:
 			hideProgress();
 			
 			if (rootView != null)
 			{
 				loadSiftFrame(rootView);
 			}
 
 			break;
 		case 2:
 			hideProgress();
 			
 			Toast.makeText(activity, "服务当前不可用，请稍后重试！", Toast.LENGTH_SHORT).show();
 			break;
 		case MSG_UPDATE_KEYWORD:
 			((TextView) rootView.findViewById(R.id.edsift))
 			.setText((String) msg.obj);
 			break;
 		}
 
 	}
 
 	@Override
 	public void onClick(View v) {
 		if (v.getId() == R.id.filter_clear)
 		{
 			//Remove keywrod.
 			ed_sift.setText("");
 			parametersHolder.remove(""); //Remove keyword
 			
 			//Remove all select value.
 			for (TextView selectValue : selector.values())
 			{
 				selectValue.setText("请选择");
 				parametersHolder.remove((String) selectValue.getTag());
 			}
 			
 			//Remove edit values.
 			for (String key : editors.keySet())
 			{
 				EditText edit = editors.get(key);
 				edit.setText("");
 				parametersHolder.remove(key);
 			}
 			
 		}
 		else if (v.getId() == R.id.filter_confirm)
 		{
 			handleRightAction();
 		}
 	}
 
 	@Override
 	public int getEnterAnimation() {
 		return R.anim.zoom_enter;
 	}
 
 	@Override
 	public int getExitAnimation() {
 		return R.anim.zoom_exit;
 	}
 
 	
 }
