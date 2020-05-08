 package com.quanleimu.view;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 
 import com.quanleimu.activity.QuanleimuApplication;
 import com.quanleimu.activity.R;
 import com.quanleimu.entity.UserBean;
 import com.quanleimu.util.Communication;
 import com.quanleimu.util.ErrorHandler;
 import com.quanleimu.util.Helper;
 
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.view.LayoutInflater;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class OpinionBackView extends BaseView {
 
 	private EditText etOpinion;
 	private String content = "";
 //	private String phoneMark = "";
 	private String mobile = "";
 	private UserBean user;
 	private String result;
 	private int opinionType = -1;//-1 for feedback, 0 for prosecute, 1 for appeal
 //	private boolean prosecute = false;
 	private String adId = "";
 
 	protected void Init(){
 		LayoutInflater inflater = LayoutInflater.from(getContext());
 		this.addView(inflater.inflate(R.layout.opinionback, null));
 		
 		user = (UserBean) Helper.loadDataFromLocate(getContext(), "user");
 		if (user != null) {
 			mobile = user.getPhone();
 		}
 
 //		// 手机管理器
 //		TelephonyManager tm = (TelephonyManager) this
 //				.getSystemService(TELEPHONY_SERVICE);
 //		phoneMark = tm.getDeviceId();
 
 		etOpinion = (EditText) findViewById(R.id.etOpinion);
 		etOpinion.findFocus();
 		if(0 == opinionType){
 			etOpinion.setHint("请留下举报原因");
 		}
 		else if(1 == opinionType){
 			etOpinion.setHint("请留下申诉原因");
 		}
 	}
 	
 	public OpinionBackView(Context context, Bundle bundle){
 		super(context, bundle);
 		Init();
 	}
 	
 	public OpinionBackView(Context context, Bundle bundle, int opinionType, String adId){
 		super(context, bundle);
 //		this.prosecute = prosecute;
 		this.opinionType = opinionType;
 		this.adId = adId;
 		Init();
 	}
 
 	@Override
 	public boolean onRightActionPressed(){
 		content = etOpinion.getText().toString();
 		if (content.equals("")) {
 			Toast.makeText(getContext(), "内容不能为空",
 					Toast.LENGTH_SHORT).show();
 		} else {
 			
 			pd = ProgressDialog.show(getContext(), "提示", "请稍候...");
 			pd.setCancelable(true);
 			new Thread(new OpinionBackThread()).start();
 		}
 		return true;
 	}//called when right button on title bar pressed, return true if handled already, false otherwise
 	
 	public TitleDef getTitleDef(){
 		TitleDef title = new TitleDef();
 		title.m_visible = true;
 		title.m_leftActionHint = "返回";
 		title.m_rightActionHint = "确定";
 		return title;
 	}
 	public TabDef getTabDef(){
 		TabDef tab = new TabDef();
 		tab.m_visible = false;
 		return tab;
 	}
 
 	Handler myHandler = new Handler() {
 
 		@Override
 		public void handleMessage(Message msg) {
 			// TODO Auto-generated method stub
 			if(pd!=null){
 				pd.dismiss();
 			}
 			switch (msg.what) {
 			case 0:
 				Toast.makeText(getContext(), "提交成功！", Toast.LENGTH_SHORT)
 						.show();
 				m_viewInfoListener.onBack();
 				break;
 			case 1:
 				Toast.makeText(getContext(), "提交失败！", Toast.LENGTH_SHORT)
 				.show();
 				
 				break;
 			}
 			super.handleMessage(msg);
 		}
 
 	}; 
 	
 	class OpinionBackThread implements Runnable {
 		@Override
 		public void run() {
 			// String url =
 			// "http://www.baixing.com/iphone/feedback/v1/?device=android";
 			// url = url + "&content="+URLEncoder.encode(content)
 			// +"&androidUniqueIdentifier="+phoneMark+"&mobile="+mobile;
			String apiName = -1 == opinionType ? "feedback" : (0 == opinionType ? "report" : "appeal");
 			ArrayList<String> list = new ArrayList<String>();
 
 			list.add("mobile=" + mobile);
 			list.add((-1 != opinionType ? "description=" : "feedback=") + URLEncoder.encode(etOpinion.getText().toString()));
 			if(-1 != opinionType){
 				list.add("adId=" + adId);
 			}
 
 			String url = Communication.getApiUrl(apiName, list);
 			try {
 				result = Communication.getDataByUrl(url, true);
 				if (result != null) {
 					myHandler.sendEmptyMessage(0);
 				} else {
 					myHandler.sendEmptyMessage(1);
 				}
 			} catch (UnsupportedEncodingException e) {
 				QuanleimuApplication.getApplication().getErrorHandler().sendEmptyMessage(ErrorHandler.ERROR_NETWORK_UNAVAILABLE);
 				if(pd != null){
 					pd.dismiss();
 				}
 			} catch (IOException e) {
 				QuanleimuApplication.getApplication().getErrorHandler().sendEmptyMessage(ErrorHandler.ERROR_NETWORK_UNAVAILABLE);
 				if(pd != null){
 					pd.dismiss();
 				}				
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 		}
 	}
 	
 	@Override
 	public void onAttachedToWindow(){
 		etOpinion.postDelayed(new Runnable(){
 			@Override
 			public void run(){
 				etOpinion.requestFocus();
 				InputMethodManager inputMgr = 
 						(InputMethodManager) OpinionBackView.this.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
 				inputMgr.showSoftInput(etOpinion, InputMethodManager.SHOW_FORCED);
 			}			
 		}, 100);
 
 	}
 }
