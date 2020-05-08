 package com.quanleimu.view;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.ProgressDialog;
 import android.os.Handler;
 import android.os.Message;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 import com.quanleimu.activity.BaseActivity;
 import com.quanleimu.activity.R;
 import com.quanleimu.adapter.BXAlphabetSortableAdapter.BXHeader;
 import com.quanleimu.adapter.BXAlphabetSortableAdapter.BXPinyinSortItem;
 import com.quanleimu.adapter.CheckableAdapter;
 import com.quanleimu.adapter.CheckableAdapter.CheckableItem;
 import android.widget.ListAdapter;
 import com.quanleimu.adapter.CommonItemAdapter;
 import com.quanleimu.jsonutil.JsonUtil;
 import com.quanleimu.util.Communication;
 import java.util.LinkedHashMap;
 import com.quanleimu.entity.PostGoodsBean;
 public class MultiLevelSelectionView extends BaseView {
 	public static class MultiLevelItem extends Object{
 		public String txt;
 		public String id;
 		@Override
 		public String toString(){
 			return txt;
 		}
 	}
 	private final int MESSAGE_GET_METAOBJ = 1;
 	private int message;
 	private List<MultiLevelItem>items = null;
 	private String title = "请选择"; 
 	private String json = null;
 	private String id = null;
 	ListAdapter adapter = null;
 	private int remainLevel = 0;
 	private ListView listView = null;
 	
 	public MultiLevelSelectionView(BaseActivity context, List<MultiLevelItem>items, int backMessage, int remainLevel){
 		super(context);
 		message = backMessage;
 		this.items = items;
 //		init();
 		this.remainLevel = remainLevel;
 	}
 	
 	public MultiLevelSelectionView(BaseActivity context, String id, String name, int backMessage, int remainLevel){
 		super(context);
 		this.id = id;
 		title = name;
 		message = backMessage;
 		this.remainLevel = remainLevel;
 	}
 	
 	public void setTitle(String title){
 		this.title = title;
 	}
 	
 	@Override
 	protected void onAttachedToWindow(){
 		
 		if(null == adapter){
 			if(items == null || items.size() == 0){
 				pd = ProgressDialog.show(getContext(), "提示", "请稍候...");
 				pd.setCancelable(true);
 				pd.show();
 				(new Thread(new GetMetaDataThread(id))).start();
 			}
 			else{
 				init(remainLevel > 0);
 			}
 		}
 		super.onAttachedToWindow();
 	}
 	
 	public void onResume(){
 		if(listView != null)
 			listView.requestFocus();
 	}
 
 	private void init(final boolean hasNextLevel){
 		LayoutInflater inflater = LayoutInflater.from(this.getContext());
 		View v = inflater.inflate(R.layout.post_othersview, null);
 		this.addView(v);
 
 		final ListView lv = (ListView) v.findViewById(R.id.post_other_list);
 		if(lv!=null) listView = lv;
 
 		final List<CheckableItem> checkList = new ArrayList<CheckableItem>();
 		if(!hasNextLevel){
 			for(int i = 0; i < items.size(); ++ i){
 				CheckableItem t = new CheckableItem();
 				t.txt = items.get(i).txt;
 				t.checked = false;
 				t.id = items.get(i).id;
 				checkList.add(t);
 			}
 			adapter = new CheckableAdapter(this.getContext(), checkList, 10, true);
 		}
 		else{
 			adapter = new CommonItemAdapter(this.getContext(), items, 10, true);
 //			adapter = new com.quanleimu.adapter.BXAlphabetAdapter(this.getContext(), items);
 		}
 		lv.setAdapter(adapter);
 		lv.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
 				if(position == 0 && (items.size() > 10 
 						&& !(adapter.getItem(0) instanceof BXHeader) && !(adapter.getItem(0) instanceof BXPinyinSortItem))){
 					if(m_viewInfoListener != null){
 						if(hasNextLevel){
 							m_viewInfoListener.onNewView(new SelectionSearchView(MultiLevelSelectionView.this.getContext(), items, true));
 						}
 						else{
 							m_viewInfoListener.onNewView(new SelectionSearchView(MultiLevelSelectionView.this.getContext(), checkList, false));
 						}
 						return;
 					}
 				}
 				if((position == 1 || position == 0 || position == 2) && adapter.getItem(position).toString().equals("全部")){
 					MultiLevelItem nItem = new MultiLevelItem();
 					nItem.id = MultiLevelSelectionView.this.id;
 					nItem.txt = MultiLevelSelectionView.this.title;
 					m_viewInfoListener.onBack(message, nItem);
 					return;
 				}
 				if(hasNextLevel){
 					if(null != m_viewInfoListener){
 						MultiLevelItem item = adapter.getItem(position) instanceof BXPinyinSortItem ? 
 								(MultiLevelItem)((BXPinyinSortItem)adapter.getItem(position)).obj
 								: (MultiLevelItem)adapter.getItem(position);
 						MultiLevelSelectionView nextV = 
 								new MultiLevelSelectionView((BaseActivity)MultiLevelSelectionView.this.getContext(), 
 										item.id, 
 										item.txt, 
 										message,
 										MultiLevelSelectionView.this.remainLevel - 1); 
 						
 						m_viewInfoListener.onNewView(nextV);
 					}
 				}
 				else{
 					CheckableItem item = adapter.getItem(position) instanceof BXPinyinSortItem ? 
 							(CheckableItem)((BXPinyinSortItem)adapter.getItem(position)).obj
 							: (CheckableItem)adapter.getItem(position);
 
 					((CheckableAdapter)adapter).setItemCheckStatus(position, !item.checked);
 //					item.checked = !item.checked;
 //					List<CheckableItem>lists = (List<CheckableItem>)((CheckableAdapter)adapter).getList();
 //					lists.set(position, item);
 //					((CheckableAdapter)adapter).setList(lists);
 					MultiLevelItem mItem = new MultiLevelItem();
 					mItem.id = item.id;
 					mItem.txt = item.txt;
 
 					if(null != m_viewInfoListener){
 						m_viewInfoListener.onBack(message, mItem);
 					}
 				}
 			}
 		});
 	}
 	
 	@Override
 	public boolean onRightActionPressed(){
 		return true;
 	}
 	
 	@Override
 	public void onPreviousViewBack(int message, Object obj){
 		if(message == SelectionSearchView.MSG_SELECTIONVIEW_BACK){
 			if(adapter instanceof CommonItemAdapter && obj instanceof MultiLevelItem){
 				if(null != m_viewInfoListener){
 					MultiLevelSelectionView nextV = 
 							new MultiLevelSelectionView((BaseActivity)MultiLevelSelectionView.this.getContext(), 
 									((MultiLevelItem)obj).id, 
 									((MultiLevelItem)obj).txt, 
 									this.message,
 									MultiLevelSelectionView.this.remainLevel - 1); 
 					
 					m_viewInfoListener.onNewView(nextV);
 				}
 			}
 			else if(adapter instanceof CheckableAdapter && obj instanceof CheckableItem){
 				MultiLevelItem mItem = new MultiLevelItem();
 				mItem.id = ((CheckableItem)obj).id;
 				mItem.txt = ((CheckableItem)obj).txt;
 	
 				if(null != m_viewInfoListener){
 					m_viewInfoListener.onBack(this.message, mItem);
 				}
 			}
 			return;
 		}
 		if(this.m_viewInfoListener != null){
 			this.m_viewInfoListener.onBack(message, obj);
 		}
 	}
 	
 	@Override
 	public TitleDef getTitleDef(){
 		TitleDef title = new TitleDef();
 		title.m_visible = true;
 		title.m_title = this.title;
 //		title.m_leftActionHint = "发布";
 //		if(!singleSelection){
 //			title.m_rightActionHint = "完成";
 //		}
 		return title;
 	}
 	
 	@Override
 	public TabDef getTabDef(){
 		TabDef tab = new TabDef();
 		tab.m_visible = false;
 		tab.m_tabSelected = ETAB_TYPE.ETAB_TYPE_PUBLISH;
 		return tab;
 	}
 	
 	Handler myHandler = new Handler() {
 		@Override
 		public void handleMessage(Message msg) {
 			switch (msg.what) {
 			case MESSAGE_GET_METAOBJ:
 				if(pd != null){
 					pd.dismiss();
 				}
 				if(json != null){
 					LinkedHashMap<String, PostGoodsBean> beans = JsonUtil.getPostGoodsBean(json);
 					if(beans != null){
 						PostGoodsBean bean = beans.get((String)beans.keySet().toArray()[0]);
 						if(MultiLevelSelectionView.this.items == null || MultiLevelSelectionView.this.items.size() == 0){
 							MultiLevelSelectionView.this.items = new ArrayList<MultiLevelItem>();
 							if(bean.getLabels() != null){
 								if(bean.getLabels().size() > 1){
 									MultiLevelItem tAll = new MultiLevelItem();
 									tAll.txt ="全部";
 									tAll.id = null;
 									MultiLevelSelectionView.this.items.add(tAll);
 								}
 								for(int i = 0; i < bean.getLabels().size(); ++ i){
 									MultiLevelItem t = new MultiLevelItem();
 									t.txt = bean.getLabels().get(i);
 									t.id = bean.getValues().get(i);
 									MultiLevelSelectionView.this.items.add(t);
 								}
 							}
 							else{
 								if(m_viewInfoListener != null){
 									MultiLevelItem nItem = new MultiLevelItem();
 									nItem.id = MultiLevelSelectionView.this.id;
 									nItem.txt = MultiLevelSelectionView.this.title;
 									m_viewInfoListener.onBack(message, nItem);
 									return;
 								}
 							}
 //							MultiLevelSelectionView.this.init(bean.getSubMeta().equals("1"));
 						}
 						else{
 //							MultiLevelSelectionView.this.init(bean.getSubMeta().equals("1") || bean.getLabels().size() > 0);
 						}
 						MultiLevelSelectionView.this.init(MultiLevelSelectionView.this.remainLevel > 0); 
 					}
 				}
 				break;
 			}
 		}
 	};
 	
 	class GetMetaDataThread implements Runnable {
 		private String id;
 		public GetMetaDataThread(String id) {
 			this.id = id;
 		}
 
 		@Override
 		public void run() {
 			String apiName = "metaobject";
 			ArrayList<String> list = new ArrayList<String>();
 			list.add("objIds=" + id);
 			String url = Communication.getApiUrl(apiName, list);
 			try {
 				json = Communication.getDataByUrl(url);
 			}
 			catch(Exception e){
 				e.printStackTrace();
 			}
 			myHandler.sendEmptyMessage(MESSAGE_GET_METAOBJ);
 		}
 	}
 
 }
