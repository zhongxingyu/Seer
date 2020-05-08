 package com.osastudio.newshub.net;
 /**
  * network tools
  */
 import org.apache.http.client.methods.HttpGet;
 import org.json.JSONObject;
 
 import com.osastudio.newshub.data.NewsResult;
 import com.osastudio.newshub.utils.Utils;
 
 import android.content.Context;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.AsyncTask;
 import android.os.Handler;
 import android.os.Message;
 
 public class Net {
 	private String httpPath ;
 	private Context context = null;
 	private static ConnectivityManager conn_Manager = null;
 	public final static int NetIsOK = 1;
 	public final static int NetTipMessage_show = 2;
 	private Handler handler = null;
 	public Net(Context context, Handler handler){
 		this.context = context;
 		this.handler = handler;
 		conn_Manager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
 	}
 	/**
 	 * is network ok?
 	 * true  锟借备锟窖撅拷锟斤拷锟斤拷锟斤拷锟斤拷
 	 * false 锟借备未锟斤拷锟斤拷锟斤拷锟斤拷
 	 * @return
 	 */
 	public  boolean PhoneIsOnLine(){
 		if (conn_Manager != null) {
 	         NetworkInfo networkInfo = conn_Manager.getActiveNetworkInfo();
 	         if (networkInfo != null) {
 	        	 Utils.log("PhoneIsOnLine networkInfo", "available="+networkInfo.isAvailable()+" Connected="+networkInfo.isConnected());
 	             return networkInfo.isAvailable() && networkInfo.isConnected();
 	         }
 	      }
 		return false;
 	}
 	/**
 	 * is server ok
 	 * @param url  锟斤拷路锟斤拷锟斤拷锟斤拷锟斤拷址
 	 * @return
 	 * true   锟斤拷路锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷
 	 * false  锟斤拷锟斤拷锟斤拷锟斤拷锟轿达拷锟斤拷锟�
 	 */
    private boolean NetIsOnLine(Context context, String path) {
       ExtraParameter extras = new ExtraParameter();
      extras.checkConnectivityOnly = true;
       String jsonString = NewsBaseApi.getString(context, new HttpGet(path), 
             extras);
       JSONObject jsonObject = NewsResult.toJsonObject(jsonString);
       return (jsonObject != null) ? new NewsResult(jsonObject).isSuccess()
             : false;
    }
 	
 	
 	/**
 	 * 锟叫断凤拷锟斤拷锟斤拷锟斤拷锟斤拷
 	 * @param HttpPath
 	 */
 	public void ExecutNetTask(Context context, String HttpPath){
 		this.httpPath = HttpPath;
 		new NetTask().execute(context);
 	}
 	
 	static class NetResult {
 		private boolean netflag ;
 
 		public boolean isNetflag() {
 			return netflag;
 		}
 	
 		public void setNetflag(boolean netflag) {
 			this.netflag = netflag;
 		} 
 	   
 	}
 	
 	class NetTask extends AsyncTask<Context, Void, NetResult> {
 
 		@Override
 		protected NetResult doInBackground(Context... params) {
 			boolean flag = NetIsOnLine(params[0], httpPath);
 			NetResult result = new NetResult();
 			result.setNetflag(flag);
 			return result;
 		}
 
 		@Override
 		protected void onPostExecute(NetResult result) {
 			if(!result.isNetflag()){
 			   HandlerMessage(NetTipMessage_show, result.isNetflag());
 			} else {
 				HandlerMessage(NetIsOK, result.isNetflag());
 			}
 			super.onPostExecute(result);
 		}
       
 	}
 	
 	/**
 	 * 锟斤拷锟酵硷拷锟斤拷锟较�
 	 * @param index
 	 * @param flag
 	 */
 	private void HandlerMessage(int index,boolean flag){
 		Message message = new Message();
 		message.what = index;
 		message.obj = flag;
 		handler.sendMessage(message);
 	}
 }
