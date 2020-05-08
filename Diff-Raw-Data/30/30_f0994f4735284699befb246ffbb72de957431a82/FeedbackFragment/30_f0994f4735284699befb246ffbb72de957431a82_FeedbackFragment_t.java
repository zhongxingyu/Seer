 package com.quanleimu.view.fragment;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.content.Context;
 import android.os.Bundle;
 import android.os.Message;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.quanleimu.activity.BaseFragment;
 import com.quanleimu.activity.QuanleimuApplication;
 import com.quanleimu.activity.R;
 import com.quanleimu.entity.UserBean;
 import com.quanleimu.util.Communication;
 import com.quanleimu.util.ErrorHandler;
 import com.quanleimu.util.Helper;
import com.quanleimu.util.TrackConfig.TrackMobile.PV;
 
 
 public class FeedbackFragment extends BaseFragment {
 	
 	private EditText etOpinion;
 	private String content = "";
 //	private String phoneMark = "";
 	private String mobile = "";
 	private UserBean user;
 	private String result;
 	private int opinionType = -1;//-1 for feedback, 0 for prosecute, 1 for appeal
 //	private boolean prosecute = false;
 	private String adId = "";
 	
 	public void initTitle(TitleDef title){
 		title.m_visible = true;
 		title.m_leftActionHint = "返回";
 		title.m_rightActionHint = "确定";
 	}
 	public void initTab(TabDef tab){
 		tab.m_visible = false;
 	}
 	
 	@Override
 	public int[] excludedOptionMenus() {
 		return new int[]{OPTION_FEEDBACK};
 	}
 	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		this.pv = PV.FEEDBACK;
	}
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		Bundle bunle = getArguments() != null ? getArguments() : new Bundle();
 		if (bunle.containsKey("type"))
 		{
 			this.opinionType = bunle.getInt("type");
 		}
 		
 		if (bunle.containsKey("adId"))
 		{
 			this.adId = bunle.getString("adId");
 		}
 	}
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		View rootView = inflater.inflate(R.layout.opinionback, null);
 		
 		user = (UserBean) Helper.loadDataFromLocate(getActivity(), "user");
 		if (user != null) {
 			mobile = user.getPhone();
 		}
 
 //		// 手机管理器
 //		TelephonyManager tm = (TelephonyManager) this
 //				.getSystemService(TELEPHONY_SERVICE);
 //		phoneMark = tm.getDeviceId();
 
 		etOpinion = (EditText) rootView.findViewById(R.id.etOpinion);
 		etOpinion.findFocus();
 		if(-1 != opinionType){
 			rootView.findViewById(R.id.et_contact).setVisibility(View.GONE);
 		}else{
 			if(mobile != null && !mobile.equals("")){
 				((TextView)rootView.findViewById(R.id.et_contact)).setText(mobile);
 			}
 		}
 		
 		if(0 == opinionType){
 			etOpinion.setHint("请留下举报原因");
 		}
 		else if(1 == opinionType){
 			etOpinion.setHint("请留下申诉原因");
 		}
 		
 		return rootView;
 	}
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 		
 		etOpinion.postDelayed(new Runnable(){
 			@Override
 			public void run(){
 				if (etOpinion != null)
 				{
 					etOpinion.requestFocus();
 					InputMethodManager inputMgr = 
 							(InputMethodManager) etOpinion.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
 					inputMgr.showSoftInput(etOpinion, InputMethodManager.SHOW_IMPLICIT);
 				}
 			}			
 		}, 100);
 	}
 	
 	
 	
 	@Override
 	protected void handleMessage(Message msg, Activity activity, View rootView) {
 
 		// TODO Auto-generated method stub
 		hideProgress();
 		
 		switch (msg.what) {
 		case 0:
 			try{
 				JSONObject jsonObject = new JSONObject(result);
 				JSONObject json = jsonObject.getJSONObject("error");
 				int code = json.getInt("code");
 				String message = json.getString("message");
 				Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
 				if (code == 0)
 				{
 					finishFragment();
 				}
 			}catch(JSONException e){
 				e.printStackTrace();
 			}
 			
 			break;
 		case 1:
 			Toast.makeText(activity, "提交失败！", Toast.LENGTH_SHORT)
 			.show();
 			
 			break;
 		}
 	}
 
 
 	class FeedbackThread implements Runnable {
 		@Override
 		public void run() {
 			// String url =
 			// "http://www.baixing.com/iphone/feedback/v1/?device=android";
 			// url = url + "&content="+URLEncoder.encode(content)
 			// +"&androidUniqueIdentifier="+phoneMark+"&mobile="+mobile;
 			String apiName = -1 == opinionType ? "feedback" : (0 == opinionType ? "report" : "appeal");
 			ArrayList<String> list = new ArrayList<String>();
 
 			list.add("mobile=" + mobile);
 			list.add((-1 != opinionType ? "description=" : "feedback=") + URLEncoder.encode(content));
 			if(-1 != opinionType){
 				list.add("adId=" + adId);
 			}
 
 			String url = Communication.getApiUrl(apiName, list);
 			try {
 				result = Communication.getDataByUrl(url, true);
 				if (result != null) {
 //					myHandler.sendEmptyMessage(0);
 					sendMessage(0, null);
 				} else {
 //					myHandler.sendEmptyMessage(1);
 					sendMessage(1, null);
 				}
 			} catch (UnsupportedEncodingException e) {
 				QuanleimuApplication.getApplication().getErrorHandler().sendEmptyMessage(ErrorHandler.ERROR_NETWORK_UNAVAILABLE);
 				hideProgress();
 				
 			} catch (IOException e) {
 				QuanleimuApplication.getApplication().getErrorHandler().sendEmptyMessage(ErrorHandler.ERROR_NETWORK_UNAVAILABLE);
 				hideProgress();
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 		}
 	}
 
 
 
 	@Override
 	public void handleRightAction() {
 		content = etOpinion.getText().toString();
 		String contact = ((TextView)getView().findViewById(R.id.et_contact)).getText().toString();
 		if(contact != null && !contact.equals("")){
 			content += "    联系方式: " + contact;
 		}
 		if (content.equals("")) {
 			Toast.makeText(getActivity(), "内容不能为空",
 					Toast.LENGTH_SHORT).show();
 		} else {
 			
 			showSimpleProgress();
 			new Thread(new FeedbackThread()).start();
 		}
 	}
 	
 	
 	
 }
