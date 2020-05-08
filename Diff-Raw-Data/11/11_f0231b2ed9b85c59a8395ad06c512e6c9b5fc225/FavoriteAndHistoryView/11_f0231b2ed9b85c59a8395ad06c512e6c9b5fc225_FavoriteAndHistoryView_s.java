 package com.quanleimu.view;
 
 import java.util.ArrayList;
 import java.util.List;
 import com.quanleimu.activity.QuanleimuApplication;
 import com.quanleimu.activity.R;
 import com.quanleimu.adapter.GoodsListAdapter;
 import com.quanleimu.entity.GoodsDetail;
 import com.quanleimu.entity.GoodsList;
 import com.quanleimu.imageCache.SimpleImageLoader;
 import com.quanleimu.jsonutil.JsonUtil;
 import com.quanleimu.util.Communication;
 import com.quanleimu.util.ErrorHandler;
 import com.quanleimu.util.GoodsListLoader;
 import com.quanleimu.util.Helper;
 import com.quanleimu.widget.PullToRefreshListView;
 import com.quanleimu.widget.PullToRefreshListView.E_GETMORE;
 
 import android.content.Context;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ImageView;
 
 public class FavoriteAndHistoryView extends BaseView implements PullToRefreshListView.OnRefreshListener, PullToRefreshListView.OnGetmoreListener, GoodDetailView.IListHolder{
 	private boolean isFav = false;
 	static final int MSG_UPDATEFAV = 1;
 	static final int MSG_UPDATEHISTORY = 2;
 	static final int MSG_DELETEAD = 3;
 	static final int MSG_DELETEALL = 4;
 	static final int MSG_GOTMOREFAV = 5;
 	static final int MSG_GOTMOREHISTORY = 6;
 	static final int MSG_NOMOREFAV = 7;
 	static final int MSG_NOMOREHISTORY = 8;
 	private GoodsListAdapter adapter = null;
 	private PullToRefreshListView pullListView = null;
 	private Bundle bundle = null;
 	private int buttonStatus = -1;//-1:edit 0:finish
 	private GoodsListLoader glLoader = null;
 	private GoodsList tempGoodsList = null;
 	public FavoriteAndHistoryView(Context context, Bundle bundle, boolean isFav){
 		super(context);
 		this.isFav = isFav;
 		this.bundle = bundle;
 		Init();
 	}
 	protected void Init() {
 		
 		LayoutInflater inflater = LayoutInflater.from(getContext());
 		addView(inflater.inflate(R.layout.personallistview, null));
 		pullListView = (PullToRefreshListView)this.findViewById(R.id.plvlist);
 		pullListView.setOnRefreshListener(this);
 		pullListView.setOnGetMoreListener(this);
 		pullListView.setOnItemClickListener(new OnItemClickListener(){
 			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
 				position = position - pullListView.getHeaderViewsCount();
 				if(position < 0 || position >= tempGoodsList.getData().size()) return;
 				m_viewInfoListener.onNewView(new GoodDetailView(FavoriteAndHistoryView.this.getContext(), bundle, glLoader, position, FavoriteAndHistoryView.this));
 			}
 			
 		});
 		
 		glLoader = new GoodsListLoader(null, myHandler, null, tempGoodsList);
 		
 		tempGoodsList = new GoodsList(isFav ? QuanleimuApplication.getApplication().getListMyStore() : QuanleimuApplication.getApplication().getListLookHistory());
 		glLoader.setGoodsList(tempGoodsList);
 		glLoader.setHasMore(false);
 		
 		adapter = new GoodsListAdapter(this.getContext(), tempGoodsList.getData());
 		adapter.setMessageOutOnDelete(myHandler, MSG_DELETEAD);
 //		adapter.setList(tempGoodsList.getData());		
 		pullListView.setAdapter(adapter);
 
 	}
 	
 	@Override
 	public void onAttachedToWindow(){			
 		super.onAttachedToWindow();
 		TitleDef title = new TitleDef();
 		title.m_visible = true;
 		title.m_leftActionHint = "返回";
 		title.m_title = isFav ? "收藏的信息" : "浏览历史";
 		title.m_rightActionHint = "编辑";
 		m_viewInfoListener.onTitleChanged(title);
 		adapter.setHasDelBtn(false);
 		adapter.notifyDataSetChanged();
 		pullListView.invalidateViews();
 		this.buttonStatus = -1;
 
 	}
 	
 	
 	@Override
 	public void onResume(){
 		super.onResume();
 		
 		for(int i = 0; i < pullListView.getChildCount(); ++i){
 			ImageView imageView = (ImageView)pullListView.getChildAt(i).findViewById(R.id.ivInfo);
 			
 			if(	null != imageView	
 					&& null != imageView.getTag() && imageView.getTag().toString().length() > 0
 					/*&& null != imageView.getDrawable()
 					&& imageView.getDrawable() instanceof AnimationDrawable*/){
 				SimpleImageLoader.showImg(imageView, imageView.getTag().toString(), getContext());
 			}
 		}
 		
 		glLoader.setHasMoreListener(null);
 		glLoader.setHandler(myHandler);
 		adapter.setList(glLoader.getGoodsList().getData());
 		pullListView.setSelectionFromHeader(glLoader.getSelection());
 	}	
 	
 	@Override
 	public void onPause(){
 		super.onPause();
 		
 		for(int i = 0; i < pullListView.getChildCount(); ++i){
 			ImageView imageView = (ImageView)pullListView.getChildAt(i).findViewById(R.id.ivInfo);
 			
 			if(	null != imageView	
 					&& null != imageView.getTag() && imageView.getTag().toString().length() > 0
 					/*&& null != imageView.getDrawable()
 					&& imageView.getDrawable() instanceof AnimationDrawable*/){
 				SimpleImageLoader.Cancel(imageView.getTag().toString(), imageView);
 			}
 		}
 	}	
 	
 	@Override
 	public boolean onRightActionPressed(){
 		if(-1 == buttonStatus){
 			if(this.m_viewInfoListener != null){
 				TitleDef title = getTitleDef();
 				title.m_rightActionHint = "完成";
 				title.m_leftActionHint = "清空";
 				title.m_leftActionStyle = EBUTT_STYLE.EBUTT_STYLE_NORMAL;
 				m_viewInfoListener.onTitleChanged(title);
 			}
 			if(adapter != null){
 				adapter.setHasDelBtn(true);
 			}
 			buttonStatus = 0;
 		}
 		else{
 			if(this.m_viewInfoListener != null){
 				TitleDef title = getTitleDef();
 				title.m_rightActionHint = "编辑";
 				m_viewInfoListener.onTitleChanged(title);
 			}
 			adapter.setHasDelBtn(false);
 			buttonStatus = -1;
 		}
 		adapter.notifyDataSetChanged();
 		pullListView.invalidateViews();
 		return true;
 	}
 
 	@Override
 	public boolean onLeftActionPressed() {
 		if(0 == buttonStatus){
 			myHandler.sendEmptyMessage(MSG_DELETEALL);
 			return true;
 		}
 		else{
 			return onBack();
 		}
 	}
 
 	@Override
 	public TitleDef getTitleDef() {
 		TitleDef title = new TitleDef();
 		title.m_visible = true;
 		title.m_leftActionHint = "返回";
 		title.m_title = isFav ? "收藏的信息" : "浏览历史";
 		title.m_rightActionHint = "编辑";
 		return title;
 	}
 
 	@Override
 	public TabDef getTabDef() {
 		TabDef tab = new TabDef();
 		tab.m_visible = false;
 		return tab;
 	}
 	
 	static private void removeGoods(GoodsDetail detail, List<GoodsDetail> fromList){
 		for(GoodsDetail o : fromList){
 			if(o.equals(detail)){
 				fromList.remove(o);
 				break;
 			}
 		}
 		
 		//fromList.remove(detail);
 	}
 	
 	Handler myHandler = new Handler() {
 		@Override
 		public void handleMessage(Message msg) {
 			switch (msg.what) {
 			case MSG_UPDATEFAV:
 				if (pd != null) {
 					pd.dismiss();
 				}
 				
 				tempGoodsList = JsonUtil.getGoodsListFromJson(glLoader.getLastJson());
 				if(null == tempGoodsList || 0 == tempGoodsList.getData().size()){
 					Message msg2 = Message.obtain();
 					msg2.what = ErrorHandler.ERROR_SERVICE_UNAVAILABLE;
 					QuanleimuApplication.getApplication().getErrorHandler().sendMessage(msg2);
 
 					pullListView.onFail();
 				}else{
 					List<GoodsDetail> tmp = new ArrayList<GoodsDetail>();
 					List<GoodsDetail> favList = QuanleimuApplication.getApplication().getListMyStore();
 					
 					if(tempGoodsList.getData().size() <= favList.size()){
 						for(int i = tempGoodsList.getData().size() - 1; i >= 0; -- i){
 							boolean exist = false;
 							for(int j = 0; j < tempGoodsList.getData().size(); ++ j){
 								if(favList.get(i).equals(tempGoodsList.getData().get(j))){
									tmp.add(tempGoodsList.getData().get(j));
 									favList.set(i, tempGoodsList.getData().get(j));
 									exist = true;
 									break;
 								}
 							}
 							if(!exist){
 								favList.remove(i);
 							}
 						}
 					}
 					tempGoodsList.setData(tmp);
 					
 					QuanleimuApplication.getApplication().setListMyStore(favList);
 					Helper.saveDataToLocate(FavoriteAndHistoryView.this.getContext(), "listMyStore", favList);
 					adapter.setList(tempGoodsList.getData());
 					glLoader.setGoodsList(tempGoodsList);
 					glLoader.setHasMore(tempGoodsList.getData().size() < favList.size());
 				}
 
 				pullListView.onRefreshComplete();
 				
 				break;
 			case MSG_UPDATEHISTORY:
 				if(pd != null){
 					pd.dismiss();
 				}
 
 				tempGoodsList = JsonUtil.getGoodsListFromJson(glLoader.getLastJson());
 				if(null == tempGoodsList || 0 == tempGoodsList.getData().size()){
 					Message msg2 = Message.obtain();
 					msg2.what = ErrorHandler.ERROR_SERVICE_UNAVAILABLE;
 					QuanleimuApplication.getApplication().getErrorHandler().sendMessage(msg2);
 
 					pullListView.onFail();
 				}else{
 					List<GoodsDetail> tmp = new ArrayList<GoodsDetail>();
 					List<GoodsDetail> historyList = QuanleimuApplication.getApplication().getListLookHistory();
 					
 					if(tempGoodsList.getData().size() <= historyList.size()){
 						for(int i = tempGoodsList.getData().size() - 1; i >= 0; -- i){
 							boolean exist = false;
 							for(int j = 0; j < tempGoodsList.getData().size(); ++ j){
 								if(historyList.get(i).equals(tempGoodsList.getData().get(j))){
									tmp.add(tempGoodsList.getData().get(j));
 									historyList.set(i, tempGoodsList.getData().get(j));
 									exist = true;
 									break;
 								}
 							}
 							if(!exist){
 								historyList.remove(i);
 							}
 						}
 					}					
 					tempGoodsList.setData(tmp);
 					
 					QuanleimuApplication.getApplication().setListLookHistory(historyList);
 					Helper.saveDataToLocate(FavoriteAndHistoryView.this.getContext(), "listLookHistory", historyList);
 					adapter.setList(tempGoodsList.getData());
 					glLoader.setGoodsList(tempGoodsList);
 					glLoader.setHasMore(tempGoodsList.getData().size() < historyList.size());
 					
 					pullListView.onRefreshComplete();
 				}
 				
 				break;
 			case MSG_DELETEAD:
 				int pos = msg.arg2;
 				if(isFav){
 					List<GoodsDetail> goodsList = QuanleimuApplication.getApplication().getListMyStore();
 					goodsList.remove(pos);
 					if(goodsList != tempGoodsList.getData())	
 						tempGoodsList.getData().remove(pos);
 					//QuanleimuApplication.getApplication().setListMyStore(goodsList);
 					Helper.saveDataToLocate(FavoriteAndHistoryView.this.getContext(), "listMyStore", goodsList);
 				}
 				else{
 					List<GoodsDetail> goodsList = QuanleimuApplication.getApplication().getListLookHistory();
 					goodsList.remove(pos);
 					if(goodsList != tempGoodsList.getData())	
 						tempGoodsList.getData().remove(pos);
 					//QuanleimuApplication.getApplication().setListLookHistory(goodsList);
 					Helper.saveDataToLocate(FavoriteAndHistoryView.this.getContext(), "listLookHistory", goodsList);
 				}	
 				
 				adapter.setList(tempGoodsList.getData());
 				adapter.notifyDataSetChanged();
 				pullListView.invalidateViews();
 				adapter.setUiHold(false);
 				break;
 			case MSG_DELETEALL:
 				List<GoodsDetail> goodsList = new ArrayList<GoodsDetail>();
 				if(isFav){
 					QuanleimuApplication.getApplication().setListMyStore(new ArrayList<GoodsDetail>(goodsList));
 					Helper.saveDataToLocate(FavoriteAndHistoryView.this.getContext(), "listMyStore", new ArrayList<GoodsDetail>(goodsList));
 				}
 				else{
 					QuanleimuApplication.getApplication().setListLookHistory(goodsList);
 					Helper.saveDataToLocate(FavoriteAndHistoryView.this.getContext(), "listLookHistory", new ArrayList<GoodsDetail>(goodsList));
 				}
 				
 				glLoader.getGoodsList().setData(goodsList);
 				glLoader.setHasMore(false);
 				adapter.setList(tempGoodsList.getData());
 				adapter.notifyDataSetChanged();
 				pullListView.invalidateViews();
 				
 				if(FavoriteAndHistoryView.this.m_viewInfoListener != null){
 					TitleDef title = getTitleDef();
 					title.m_rightActionHint = "编辑";
 					title.m_leftActionHint = "设置";
 					m_viewInfoListener.onTitleChanged(title);
 				}
 				adapter.setHasDelBtn(false);
 				buttonStatus = -1;
 				break;
 				
 			case MSG_GOTMOREFAV:
 			case MSG_GOTMOREHISTORY:
 			case MSG_NOMOREFAV:
 			case MSG_NOMOREHISTORY:
 			case ErrorHandler.ERROR_NETWORK_UNAVAILABLE:	
 				if (pd != null) {
 					pd.dismiss();
 				}
 				onResult(msg.what, glLoader);
 				break;
 			}
 		}
 	};
 	
 	private static int ITEMS_PER_REQUEST = 30;
 	public void updateAdsThread(boolean isFav, boolean isGetMore){
 
 		ArrayList<String> list = new ArrayList<String>();
 		List<GoodsDetail> details = isFav ? QuanleimuApplication.getApplication().getListMyStore() : 
 		QuanleimuApplication.getApplication().getListLookHistory();
 		
 		int startIndex = 0;
 		if(isGetMore){//Notice: should ensure that tempGoodsList is shorter than whole list, Or unexpected results may occur
 			startIndex = tempGoodsList.getData().size();
 		}
 		//list.add("start=0");//this param is controled by param0 of startFetching()
 		if (details != null && details.size() > startIndex) {
 			String ids = "id:" + details.get(startIndex).getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID);
 			for (int i = startIndex+1; i < details.size() && i < startIndex + ITEMS_PER_REQUEST; ++i) {
 				ids += " OR " + "id:" + details.get(i).getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID);
 			}
 			list.add("query=(" + ids + ")");
 		}
 		
 		list.add("rt=1");
 		
 		int msgGotFirst = (isFav ? MSG_UPDATEFAV : MSG_UPDATEHISTORY);
 		int msgGotMore = (isFav ? MSG_GOTMOREFAV : MSG_GOTMOREHISTORY);
 		int msgNoMore = (isFav ? MSG_NOMOREFAV : MSG_NOMOREHISTORY);
 		
 		glLoader.setParams(list);
 		glLoader.setRows(ITEMS_PER_REQUEST);
 		
 		if(isGetMore)
 			glLoader.startFetching(true, msgGotMore, msgGotMore, msgNoMore, Communication.E_DATA_POLICY.E_DATA_POLICY_NETWORK_UNCACHEABLE);//trick:: param0 is set to true to avoid setting of "start=n>0"
 		else
 			glLoader.startFetching(true, msgGotFirst, msgGotMore, msgNoMore, Communication.E_DATA_POLICY.E_DATA_POLICY_NETWORK_UNCACHEABLE);
 	}
 	
 	@Override
 	public void onRefresh() {
 		if((isFav && QuanleimuApplication.getApplication().getListMyStore() != null 
 				&& QuanleimuApplication.getApplication().getListMyStore().size() > 0)
 			|| (!isFav && QuanleimuApplication.getApplication().getListLookHistory() != null
 				&& QuanleimuApplication.getApplication().getListLookHistory().size() > 0)){
 			updateAdsThread(isFav, false);		
 		}
 		else{
 			this.pullListView.onRefreshComplete();
 		}
 	}
 	@Override
 	public void onGetMore() {
 		if((isFav && QuanleimuApplication.getApplication().getListMyStore() != null 
 				&& tempGoodsList != null
 				&& tempGoodsList.getData().size() < QuanleimuApplication.getApplication().getListMyStore().size())
 			|| (!isFav && QuanleimuApplication.getApplication().getListLookHistory() != null
 					&& tempGoodsList != null
 					&& tempGoodsList.getData().size() < QuanleimuApplication.getApplication().getListLookHistory().size())){
 			updateAdsThread(isFav, true);		
 		}
 		else{
 			this.pullListView.onGetMoreCompleted(E_GETMORE.E_GETMORE_NO_MORE);
 		}
 		
 	}
 	@Override
 	public void startFecthingMore() {
 		updateAdsThread(isFav, true);
 	}
 	
 	@Override
 	public boolean onResult(int msg, GoodsListLoader loader) {
 		if(msg == MSG_GOTMOREFAV || msg == MSG_GOTMOREHISTORY){
 			GoodsList moreGoodsList = JsonUtil.getGoodsListFromJson(loader.getLastJson());
 			if(isFav){	
 				if(null == moreGoodsList || 0 == moreGoodsList.getData().size()){
 
 					pullListView.onGetMoreCompleted(E_GETMORE.E_GETMORE_NO_MORE);
 					glLoader.setHasMore(false);
 					return false;
 				}else{					
 					List<GoodsDetail> favList = QuanleimuApplication.getApplication().getListMyStore();
 					if(tempGoodsList.getData().size() < favList.size()){
 						List<GoodsDetail> tmp = new ArrayList<GoodsDetail>();
 						 
 						for(int i = moreGoodsList.getData().size() + tempGoodsList.getData().size() - 1; i >= tempGoodsList.getData().size(); -- i){
 							boolean exist = false;
 							for(int j = 0; j < moreGoodsList.getData().size(); ++ j){
 								if(favList.get(i).equals(moreGoodsList.getData().get(j))){
									tmp.add(moreGoodsList.getData().get(j));
 									favList.set(i, moreGoodsList.getData().get(j));
 									exist = true;
 									break;
 								}
 							}
 							if(!exist){
 								favList.remove(i);
 							}
 						}
 						List<GoodsDetail> prev = tempGoodsList.getData();
 						prev.addAll(tmp);
 						tempGoodsList.setData(prev);
 					}
 					
 					QuanleimuApplication.getApplication().setListMyStore(favList);
 					Helper.saveDataToLocate(FavoriteAndHistoryView.this.getContext(), "listMyStore", favList);
 					
 					adapter.setList(tempGoodsList.getData());
 					adapter.notifyDataSetChanged();
 					loader.setHasMore(tempGoodsList.getData().size() < favList.size());
 					
 					pullListView.onGetMoreCompleted(E_GETMORE.E_GETMORE_OK);
 					return true;
 				}
 			}else{
 				if(null == moreGoodsList || 0 == moreGoodsList.getData().size()){
 
 					pullListView.onGetMoreCompleted(E_GETMORE.E_GETMORE_NO_MORE);
 					glLoader.setHasMore(false);
 					return false;
 				}else{
 					List<GoodsDetail> historyList = QuanleimuApplication.getApplication().getListLookHistory();
 					if(tempGoodsList.getData().size() < historyList.size()){
 						List<GoodsDetail> tmp = new ArrayList<GoodsDetail>();
 						for(int i = moreGoodsList.getData().size() + tempGoodsList.getData().size() - 1; i >= tempGoodsList.getData().size(); -- i){
 							boolean exist = false;
 							for(int j = 0; j < moreGoodsList.getData().size(); ++ j){
 								if(historyList.get(i).equals(moreGoodsList.getData().get(j))){
									tmp.add(moreGoodsList.getData().get(j));
 									historyList.set(i, moreGoodsList.getData().get(j));
 									exist = true;
 									break;
 								}
 							}
 							if(!exist){
 								historyList.remove(i);
 							}
 						}
 						List<GoodsDetail> prev = tempGoodsList.getData();
 						prev.addAll(tmp);
 						tempGoodsList.setData(prev);
 					}
 
 					QuanleimuApplication.getApplication().setListLookHistory(historyList);
 					Helper.saveDataToLocate(FavoriteAndHistoryView.this.getContext(), "listLookHistory", historyList);
 					
 					adapter.setList(tempGoodsList.getData());
 					adapter.notifyDataSetChanged();
 					loader.setHasMore(tempGoodsList.getData().size() < historyList.size());
 					pullListView.onGetMoreCompleted(E_GETMORE.E_GETMORE_OK);                                                                                                     
 					return true;
 				}
 			}
 		}else if(msg == MSG_NOMOREFAV || msg == MSG_NOMOREHISTORY){
 			glLoader.setHasMore(false);
 			pullListView.onGetMoreCompleted(E_GETMORE.E_GETMORE_NO_MORE);
 			return false;
 		}else if(msg == ErrorHandler.ERROR_NETWORK_UNAVAILABLE){
 			Message msg2 = Message.obtain();
 			msg2.what = ErrorHandler.ERROR_NETWORK_UNAVAILABLE;
 			QuanleimuApplication.getApplication().getErrorHandler().sendMessage(msg2);
 
 			pullListView.onFail();
 			return false;
 		}else if(msg == MSG_UPDATEHISTORY || msg == MSG_UPDATEFAV){
 			pullListView.onRefreshComplete();
 			return false;
 		}
 		
 		return false;
 	}
 }
