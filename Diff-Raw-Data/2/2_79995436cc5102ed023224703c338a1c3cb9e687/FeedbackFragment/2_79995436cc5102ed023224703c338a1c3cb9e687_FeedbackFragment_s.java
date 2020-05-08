 package com.baixing.view.fragment;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 
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
 
 import com.baixing.activity.BaseFragment;
 import com.baixing.data.GlobalDataManager;
 import com.baixing.entity.UserBean;
 import com.baixing.network.api.ApiParams;
 import com.baixing.network.api.BaseApiCommand;
 import com.baixing.tracking.TrackConfig.TrackMobile.PV;
 import com.baixing.util.ErrorHandler;
 import com.baixing.util.Util;
 import com.baixing.util.ViewUtil;
 import com.quanleimu.activity.R;
 
 
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
 		title.m_title = getArguments() != null && getArguments().containsKey(ARG_COMMON_TITLE) ? getArguments().getString(ARG_COMMON_TITLE) : "反馈信息";
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
 	public View onInitializeView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		View rootView = inflater.inflate(R.layout.opinionback, null);
 		
 		user = (UserBean) Util.loadDataFromLocate(getActivity(), "user", UserBean.class);
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
 				ViewUtil.showToast(getActivity(), message, false);
 				if (code == 0)
 				{
 					finishFragment();
 				}
 			}catch(JSONException e){
				e.printStackTrace();
 			}
 			
 			break;
 		case 1:
 			ViewUtil.showToast(activity, "提交失败！", false);
 			
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
 //			ArrayList<String> list = new ArrayList<String>();
 			ApiParams params = new ApiParams();
 			params.addParam("mobile", mobile);
 			if (opinionType == -1) {
 				params.addParam("feedback", content);
 			}
 			else {
 				params.addParam("description", content);
 				params.addParam("adId", adId);
 			}
 			
 
 			try {
 //				String url = Communication.getApiUrl(apiName, list);
 				result = BaseApiCommand.createCommand(apiName, false, params).executeSync(GlobalDataManager.getInstance().getApplicationContext());//ApiClient.getInstance().invokeApi(Api.createPost(apiName), params);//Communication.getDataByUrl(url, true);
 				if (result != null) {
 //					myHandler.sendEmptyMessage(0);
 					sendMessage(0, null);
 				} else {
 //					myHandler.sendEmptyMessage(1);
 					sendMessage(1, null);
 				}
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 		}
 	}
 
 
 
 	@Override
 	public void handleRightAction() {
 		content = etOpinion.getText().toString().trim();
 		String contact = ((TextView)getView().findViewById(R.id.et_contact)).getText().toString().trim();
 		if (content.equals("")) {
 			ViewUtil.showToast(getActivity(), "内容不能为空",false);
 		} else {
 			if(contact != null && !contact.equals("")){
 				content += "    联系方式: " + contact;
 			}
 			showSimpleProgress();
 			new Thread(new FeedbackThread()).start();
 		}
 	}
 	
 	public boolean hasGlobalTab()
 	{
 		return false;
 	}
 	
 }
