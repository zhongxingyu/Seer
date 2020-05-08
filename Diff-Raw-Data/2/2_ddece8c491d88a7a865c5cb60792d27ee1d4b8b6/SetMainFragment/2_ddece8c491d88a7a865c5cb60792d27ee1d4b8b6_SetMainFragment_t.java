 package com.quanleimu.view.fragment;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.ComponentName;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Message;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 import com.quanleimu.activity.BaseFragment;
 import com.quanleimu.activity.QuanleimuApplication;
 import com.quanleimu.activity.QuanleimuMainActivity;
 import com.quanleimu.activity.R;
 import com.quanleimu.entity.UserBean;
 import com.quanleimu.util.BXUpdateService;
 import com.quanleimu.util.Communication;
 import com.quanleimu.util.ParameterHolder;
 import com.quanleimu.util.Util;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 //import com.quanleimu.entity.AuthDialogListener;
 //import com.quanleimu.entity.WeiboAccessTokenWrapper;
 //import com.weibo.net.AccessToken;
 //import com.weibo.net.Oauth2AccessTokenHeader;
 //import com.weibo.net.Utility;
 //import com.weibo.net.Weibo;
 //import com.weibo.net.WeiboParameters;
 
 public class SetMainFragment extends BaseFragment implements View.OnClickListener {
 
     private final int MSG_NETWORK_ERROR = 0;
     private final int MSG_DOWNLOAD_APP = 1;
     private final int MSG_INSTALL_APP = 3;
     private final int MSG_HAS_NEW_VERSION = 4;
 
     private ProgressDialog pd;
 
     private String serverVersion = "";
 
     // 定义控件
     public Dialog changePhoneDialog;
     private UserBean user;
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
 
         View setmain = inflater.inflate(R.layout.setmain, null);
         ((RelativeLayout) setmain.findViewById(R.id.setFlowOptimize)).setOnClickListener(this);
         ((RelativeLayout) setmain.findViewById(R.id.setBindID)).setOnClickListener(this);
         ((RelativeLayout) setmain.findViewById(R.id.setCheckUpdate)).setOnClickListener(this);
         ((RelativeLayout) setmain.findViewById(R.id.setAbout)).setOnClickListener(this);
         ((RelativeLayout) setmain.findViewById(R.id.setFeedback)).setOnClickListener(this);
 
 //		WeiboAccessTokenWrapper tokenWrapper = (WeiboAccessTokenWrapper)Helper.loadDataFromLocate(this.getActivity(), "weiboToken");
 //		AccessToken token = null;
 //		if(tokenWrapper != null && tokenWrapper.getToken() != null){
 //			token = new AccessToken(tokenWrapper.getToken(), QuanleimuApplication.kWBBaixingAppSecret);
 //			token.setExpiresIn(tokenWrapper.getExpires());
 //		}
 //		String nick = (String)Helper.loadDataFromLocate(this.getActivity(), "weiboNickName");
 //		if(token != null && nick != null){
 //			((TextView)setmain.findViewById(R.id.tvWeiboNick)).setText(nick);
 //			if(QuanleimuApplication.getWeiboAccessToken() == null){
 //				QuanleimuApplication.setWeiboAccessToken(token);
 //			}
 //		}
 
 //		final TextView textImg = (TextView)setmain.findViewById(R.id.textView3);
 //		if(QuanleimuApplication.isTextMode()){
 //			textImg.setText("文字");
 //		}
 //		else{
 //			textImg.setText("图片");
 //		}
 //		((RelativeLayout)setmain.findViewById(R.id.rlTextImage)).setOnClickListener(new View.OnClickListener() {
 //
 //			@Override
 //			public void onClick(View v) {
 //				if(textImg.getText().equals("图片")){
 //					textImg.setText("文字");
 //					QuanleimuApplication.setTextMode(true);
 //				}
 //				else{
 //					textImg.setText("图片");
 //					QuanleimuApplication.setTextMode(false);
 //				}
 //			}
 //		});
 
 //		((TextView)setmain.findViewById(R.id.personMark)).setText(QuanleimuApplication.getApplication().getPersonMark());
 
         refreshUI(setmain);
 
         return setmain;
     }
 
     private void refreshUI(View rootView) {
 
         user = Util.getCurrentUser();
 
         TextView bindIdTextView = (TextView) rootView.findViewById(R.id.setBindIdtextView);
         if (user == null || user.getPhone() == null || user.getPhone().equals("")) {
             bindIdTextView.setText(R.string.label_login);
         } else {
             bindIdTextView.setText(R.string.label_logout);
         }
 
 
     }
 
 
     public void onResume() {
         super.onResume();
 //		((TextView)getView().findViewById(R.id.personMark)).setText(QuanleimuApplication.getApplication().getPersonMark());
         this.refreshUI(getView());
     }
 
     @Override
     public void initTitle(TitleDef title) {
         title.m_visible = true;
         title.m_title = "设置";
         title.m_leftActionHint = "完成";
         title.m_leftActionStyle = EBUTT_STYLE.EBUTT_STYLE_NORMAL;
     }
 
     @Override
     public void initTab(TabDef tab) {
         tab.m_visible = false;
     }
 
     private void logoutAction() {
         AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
         builder.setTitle(R.string.dialog_confirm_logout)
                 .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialogInterface, int i) {
                         Util.logout();
                         Toast.makeText(getAppContext(), "已退出", 1).show();
                     }
                 })
                 .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int id) {
                         dialog.dismiss();
                     }
                 }).create().show();
     }
 
     @Override
     public void onClick(View v) {
         switch (v.getId()) {
             case R.id.setFlowOptimize:
                 showFlowOptimizeDialog();
                 break;
             case R.id.setBindID:
                if (user == null || user.getPhone() == null || user.getPhone().equals("")) {
                     Bundle bundle = createArguments(null, "用户中心");
                     pushFragment(new LoginFragment(), bundle);
                 } else {
                     //TODO jiawu 加入确认退出过程
                     logoutAction();
                     refreshUI(getView());
                 }
 
                 break;
             case R.id.setCheckUpdate:
                 checkNewVersion();
                 break;
             case R.id.setAbout:
                 pushFragment(new AboutUsFragment(), null);
                 break;
             case R.id.setFeedback:
                 pushFragment(new FeedbackFragment(), createArguments(null, null));
                 break;
             default:
                 Toast.makeText(getAppContext(), "no action", 1).show();
                 break;
         }
         // 手机号码
 //		if(v.getId() == R.id.rlWeibo){
 //			if(QuanleimuApplication.getWeiboAccessToken() != null){
 //				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 //				builder.setTitle("提示:")
 //						.setMessage("是否解除绑定？")
 //						.setNegativeButton("否", null)
 //						.setPositiveButton("是",
 //								new DialogInterface.OnClickListener() {
 //	
 //									@Override
 //									public void onClick(DialogInterface dialog,
 //											int which) {
 //										
 //										Helper.saveDataToLocate(getActivity(), "weiboToken", null);
 //										Helper.saveDataToLocate(getActivity(), "weiboNickName", null);
 //										QuanleimuApplication.setWeiboAccessToken(null);
 //										Weibo.getInstance().setAccessToken(null);
 //										Weibo.getInstance().setRequestToken(null);
 //										Weibo.getInstance().setupConsumerConfig("", "");
 //										((TextView)findViewById(R.id.tvWeiboNick)).setText("");
 //									}
 //								});
 //				builder.create().show();	
 //			}
 //			else{
 //				Weibo weibo = Weibo.getInstance();
 //				weibo.setupConsumerConfig(QuanleimuApplication.kWBBaixingAppKey, QuanleimuApplication.kWBBaixingAppSecret);
 //				weibo.setRedirectUrl("http://www.baixing.com");
 ////				weibo.authorize((BaseActivity)this.getContext(), new AuthDialogListener());
 //                WeiboParameters parameters=new WeiboParameters();
 //                parameters.add("forcelogin", "true");
 //                Utility.setAuthorization(new Oauth2AccessTokenHeader());
 //                AuthDialogListener lsn = new AuthDialogListener(getActivity(), new AuthDialogListener.AuthListener(){
 //                	@Override
 //                	public void onComplete(){
 //                		String nick = (String)Helper.loadDataFromLocate(getActivity(), "weiboNickName");
 //                		((TextView)findViewById(R.id.tvWeiboNick)).setText(nick);
 //                	}
 //                }); 
 //                weibo.dialog(getActivity(), 
 //                		parameters, lsn);
 //                lsn.setInAuthrize(true);
 //			}
 //		}
                                     /*
         final View root = getView();
 		// 签名档
 		if (v.getId() == ((RelativeLayout) root.findViewById(R.id.rlMark)).getId()) {
 			pushFragment(new MarkLableFragment(), null );
 		}
 
 		// 清空缓存
 		else if (v.getId() == ((RelativeLayout) root.findViewById(R.id.rlClearCache)).getId()) {
 
 			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 			builder.setTitle(R.string.dialog_title_info)
 					.setMessage(R.string.dialog_message_confirm_clear_cache)
 					.setNegativeButton(R.string.no, null)
 					.setPositiveButton(R.string.yes,
 							new DialogInterface.OnClickListener() {
 
 								@Override
 								public void onClick(DialogInterface dialog,
 										int which) {
 									String[] files = getActivity().fileList();
 									for(int i=0;i<files.length;i++){
 										String file_path = files[i];
 										getActivity().deleteFile(file_path);
 									}
 									
 									QuanleimuApplication.getApplication().ClearCache();
 									
 									//清空签名档
 //									((TextView)root.findViewById(R.id.personMark)).setText("");
 								}
 							});
 			builder.create().show();
 		}
 		
  */
     }
 
     @Override
     protected void handleMessage(Message msg, Activity activity, View rootView) {
         super.handleMessage(msg, activity, rootView);
 
         switch (msg.what) {
             case MSG_NETWORK_ERROR:
                 Toast.makeText(getActivity(), msg.obj.toString(), 1).show();
                 break;
             case MSG_DOWNLOAD_APP:
                 updateAppDownload(msg.obj.toString());
                 break;
             case MSG_INSTALL_APP:
                 updateAppInstall();
                 break;
             case MSG_HAS_NEW_VERSION:
                 final String apkUrl = msg.obj.toString();
 
                 AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                 builder.setTitle("检查更新")
                         .setMessage("当前版本: " + QuanleimuApplication.version
                                 + "\n发现新版本: " + serverVersion
                                 + "\n是否更新？")
                         .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                             @Override
                             public void onClick(DialogInterface dialogInterface, int i) {
                                 sendMessage(MSG_DOWNLOAD_APP, apkUrl);
                             }
                         })
                         .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                             @Override
                             public void onClick(DialogInterface dialog, int id) {
                                 dialog.dismiss();
                             }
                         }).create().show();
                 break;
         }
 
         if (pd != null) {
             pd.hide();
         }
 
     }
 
     /**
      * 省流量设置
      */
     private void showFlowOptimizeDialog() {
         int checkedIdx = QuanleimuApplication.isTextMode() ? 1 : 0;
         AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
         builder.setTitle(R.string.label_flow_optimize)
                 .setSingleChoiceItems(R.array.item_flow_optimize, checkedIdx, new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int i) {
                         QuanleimuApplication.setTextMode(i == 1);
                         dialog.dismiss();
                         String tip = (i == 1) ? "省流量模式" : "图片模式";
                         Toast.makeText(getActivity(), "已切换至" + tip, 1).show();
                     }
                 })
                 .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int id) {
                         dialog.dismiss();
                     }
                 }).create().show();
     }
 
     /**
      * 检查版本更新
      */
     private void checkNewVersion() {
         ParameterHolder params = new ParameterHolder();
         params.addParameter("clientVersion", QuanleimuApplication.version);
 
         pd = ProgressDialog.show(SetMainFragment.this.getActivity(), "提示", "请稍候...");
         pd.show();
         Communication.executeAsyncGetTask("check_version", params, new Communication.CommandListener() {
 
             @Override
             public void onServerResponse(String serverMessage) {
                 try {
                     JSONObject respond = new JSONObject(serverMessage);
                     JSONObject error = respond.getJSONObject("error");
                     final String apkUrl = respond.getString("apkUrl");
                     serverVersion = respond.getString("serverVersion");
 
                     if (!"0".equals(error.getString("code"))) {
                         sendMessage(MSG_NETWORK_ERROR, error.getString("message"));
                     } else {
                         if (respond.getBoolean("hasNew")) {
                             sendMessage(MSG_HAS_NEW_VERSION, apkUrl);
                         } else {
 
                         }
                     }
                 } catch (JSONException e) {
                     sendMessage(MSG_NETWORK_ERROR, "网络异常");
                 }
 
             }
 
             @Override
             public void onException(Exception ex) {
                 sendMessage(MSG_NETWORK_ERROR, "网络异常");
             }
         });
     }
 
     private void updateAppDownload(String apkUrl) {
         //开启更新服务UpdateService
         //这里为了把update更好模块化，可以传一些updateService依赖的值
         //如布局ID，资源ID，动态获取的标题,这里以app_name为例
         Intent updateIntent =new Intent(getAppContext(), BXUpdateService.class);
         updateIntent.putExtra("titleId",R.string.app_name);
         updateIntent.putExtra("apkUrl", apkUrl);
         getAppContext().startService(updateIntent);
     }
 
     private void updateAppInstall() {
 
     }
 
 }
