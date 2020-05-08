 package com.quanleimu.util;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.client.HttpClient;
 
 import android.os.Handler;
 import android.util.Log;
 
 import com.quanleimu.entity.GoodsList;
 import com.quanleimu.util.NetworkProtocols;
 
 
 public class GoodsListLoader {
 	
 	public interface HasMoreListener{
 		public void onHasMoreStatusChanged();
 	};
 	
 	private GoodsList mGoodsList = null;//only an outlet here: set yours (if not set, we'll create one empty for you), and pass it out to others
 	private List<String> params = null;
 	private String mFields = "";
 	private boolean mIsFirst = true;
 	private boolean mHasMore = true;
 	private int mCurIndex = 0;
 	private int mRows = 30;
 	private Handler mHandler = new Handler();
 	
 	private static String mApiName = "ad_list";
 	
 	private static String mLastJson = null;
 	
 	private GetGoodsListThread mCurThread = null;
 	
 	private HasMoreListener hasMoreListener = null;
 	
 	public enum E_LISTDATA_STATUS{
 		E_LISTDATA_STATUS_OFFLINE,
 		
 		E_LISTDATA_STATUS_ONLINE
 	};
 	private E_LISTDATA_STATUS mStatusListdataExisting = E_LISTDATA_STATUS.E_LISTDATA_STATUS_OFFLINE;
 	private E_LISTDATA_STATUS mStatusListdataRequesting = E_LISTDATA_STATUS.E_LISTDATA_STATUS_OFFLINE;
 	
 	public final static int MSG_FINISH_GET_FIRST = 0;
 	public final static int MSG_FINISH_GET_MORE = 1;
 	public final static int MSG_NO_MORE = 2;
 	public final static int MSG_FIRST_FAIL = 0x0FFFFFFF;
 	public final static int MSG_EXCEPTION = 0xFFFFFFFF;
 	
 	public GoodsListLoader(List<String> params, Handler handler, String fields, GoodsList goodsList){
 		this.params = params;
 		
 		mHandler = handler;
 		
 		if(null != fields){
 			mFields = fields;
 		}
 		
 		if(null != goodsList){
 			mGoodsList = goodsList;
 		}
 	}
 	
 	public void setParams(List<String> params){
 		this.params = params;
 	}
 	
 	public void setHandler(Handler handler){
 		if(mHandler != handler){
 			cancelFetching();
 			mHandler = handler;
 		}
 	}
 
 	public void setRows(int rows){
 		mRows = rows;
 	}
 	
 	public String getLastJson(){
 		return mLastJson;
 	}
 	
 	public void setGoodsList(GoodsList list){
 		mGoodsList = list;
 	}
 	
 	public GoodsList getGoodsList(){
 		if(null ==  mGoodsList){
 			mGoodsList = new GoodsList();
 		}
 		
 		return mGoodsList;
 	}
 
 	public void setHasMore(boolean hasMore){
 		if(mHasMore != hasMore){
 			mHasMore = hasMore;
 			if(null != hasMoreListener){
 				hasMoreListener.onHasMoreStatusChanged();
 			}
 		}
 	}
 	
 	public boolean hasMore(){
 		return mHasMore;
 	}
 	
 	public int getSelection(){
 		return mCurIndex;
 	}
 	
 	public void setSelection(int selection){
 		mCurIndex = selection;
 	}
 	
 	public void cancelFetching(){
 		if(null != mCurThread){
 			mCurThread.cancel();
 		}
 	}
 	
 	public void setHasMoreListener(HasMoreListener listener){
 		hasMoreListener = listener;
 	}
 	
 	public E_LISTDATA_STATUS getDataStatus(){
 		return mStatusListdataExisting;
 	}
 	
 	public E_LISTDATA_STATUS getRequestDataStatus(){
 		return mStatusListdataRequesting;
 	}
 	
 	public void startFetching(boolean isFirst, Communication.E_DATA_POLICY dataPolicy_){
 		cancelFetching();
 		
 		mStatusListdataRequesting = ((dataPolicy_==Communication.E_DATA_POLICY.E_DATA_POLICY_ONLY_LOCAL) ? E_LISTDATA_STATUS.E_LISTDATA_STATUS_OFFLINE : E_LISTDATA_STATUS.E_LISTDATA_STATUS_ONLINE);
 		
 		mIsFirst = isFirst;
 
 		mCurThread = new GetGoodsListThread(dataPolicy_);		
 		new Thread(mCurThread).start();	
 	}	
 	
 	public void startFetching(boolean isFirst, int msgGotFirst, int msgGotMore, int msgNoMore, Communication.E_DATA_POLICY dataPolicy_){
 		cancelFetching();
 		
 		mStatusListdataRequesting = ((dataPolicy_==Communication.E_DATA_POLICY.E_DATA_POLICY_ONLY_LOCAL) ? E_LISTDATA_STATUS.E_LISTDATA_STATUS_OFFLINE : E_LISTDATA_STATUS.E_LISTDATA_STATUS_ONLINE);
 		
 		mIsFirst = isFirst;
 		mCurThread = new GetGoodsListThread(msgGotFirst, msgGotMore, msgNoMore, dataPolicy_);		
 		
 		new Thread(mCurThread).start();	
 	}
 	
 	class GetGoodsListThread implements Runnable {
 		private int msgFirst = GoodsListLoader.MSG_FINISH_GET_FIRST;
 		private int msgMore = GoodsListLoader.MSG_FINISH_GET_MORE;
 		private int msgNoMore = GoodsListLoader.MSG_NO_MORE;
 		private Communication.E_DATA_POLICY dataPolicy = Communication.E_DATA_POLICY.E_DATA_POLICY_ONLY_LOCAL;
 		
 		private boolean mCancel = false;
 		private HttpClient mHttpClient = null;
 		
 		GetGoodsListThread(Communication.E_DATA_POLICY dataPolicy_){
 			dataPolicy = dataPolicy_;
 		}
 		
 		GetGoodsListThread(int errFirst, int errMore, int errNoMore, Communication.E_DATA_POLICY dataPolicy_){
 			msgFirst = errFirst;
 			msgMore = errMore;
 			msgNoMore = errNoMore;
 			dataPolicy = dataPolicy_;
 		}
 		
 		public void cancel(){
 			mCancel = true;
 			
 			if(null != mHttpClient){
 				mHttpClient.getConnectionManager().shutdown();
 			}
 			
 			//Log.d("GoodsListLoader", "http connection has been shutdown!!");
 		}
 		
 		private void exit(){
 			GoodsListLoader.this.mCurThread = null;
 		}
 		
 		@Override
 		public void run() {
 			if(mCancel) {
 				exit();
 				return;
 			}
 			
 			ArrayList<String> list = new ArrayList<String>();
 
 			if(null != mFields && mFields.length() > 0)
 				list.add("fields=" + URLEncoder.encode(mFields));
 			
 			if(params != null){
 				list.addAll(params);
 			}
 			
 			list.add("start=" + (mIsFirst ? 0 : mGoodsList.getData().size()));
			//list.add("rt=1");
 			if(mRows > 0)
 				list.add("rows=" + mRows);
 
 			if(mCancel) {
 				exit();
 				return;
 			}
 			
 			String url = Communication.getApiUrl(mApiName, list);
 			
 //			if(Communication.E_DATA_POLICY.E_DATA_POLICY_ONLY_LOCAL != dataPolicy){
 //				Log.d("ListViewUrl", url);
 //			}
 			
 			mHttpClient = NetworkProtocols.getInstance().getHttpClient();
 			
 			try {
 				if(mCancel) {
 					exit();
 					return;
 				}
 				
 				mLastJson = Communication.getDataByUrl(mHttpClient, url, dataPolicy);
 
 				if(mCancel) {
 					exit();
 					return;
 				}
 				
 				if (mLastJson != null) {
 					if (!mIsFirst) {
 						mHandler.sendEmptyMessage(msgMore);
 					} else {
 						mIsFirst = false;
 						mHandler.sendEmptyMessage(msgFirst);
 					}
 					
 					//only when data is valid, do we need to updata listdata status
 					GoodsListLoader.this.mStatusListdataExisting = (dataPolicy == Communication.E_DATA_POLICY.E_DATA_POLICY_NETWORK_CACHEABLE/* || dataPolicy == Communication.E_DATA_POLICY.E_DATA_POLICY_NETWORK_UNCACHEABLE*/) ?
 															E_LISTDATA_STATUS.E_LISTDATA_STATUS_ONLINE : (dataPolicy == Communication.E_DATA_POLICY.E_DATA_POLICY_ONLY_LOCAL) ?
 															E_LISTDATA_STATUS.E_LISTDATA_STATUS_OFFLINE : 	GoodsListLoader.this.mStatusListdataExisting;
 				} else {
 					if(!mIsFirst){
 						mHandler.sendEmptyMessage(msgNoMore);
 					}else{
 						mHandler.sendEmptyMessage(MSG_FIRST_FAIL);
 					}
 				}
 				
 				exit();
 				return;
 			} catch (UnsupportedEncodingException e) {
 			} catch (IOException e) {
 				if(!mCancel){
 					mHandler.sendEmptyMessage(ErrorHandler.ERROR_NETWORK_UNAVAILABLE);
 				}
 				exit();
 				return;
 			} catch (Communication.BXHttpException e){
 				
 			}
 			
 			if(!mCancel){
 				mHandler.sendEmptyMessage(MSG_EXCEPTION);
 			}
 			exit();
 		}
 	}
 }
