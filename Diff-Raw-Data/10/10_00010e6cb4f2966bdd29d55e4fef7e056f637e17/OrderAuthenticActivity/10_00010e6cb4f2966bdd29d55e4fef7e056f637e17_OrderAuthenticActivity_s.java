 package com.guoli.hotel.activity.order;
 
 import java.util.HashMap;
 import java.util.regex.Pattern;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.content.Intent;
 import android.os.Bundle;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.Toast;
 
 import com.guoli.hotel.GuoliApplication;
 import com.guoli.hotel.R;
 import com.guoli.hotel.activity.BaseActivity2;
 import com.guoli.hotel.activity.user.LoginActivity;
 import com.guoli.hotel.net.Action;
 import com.guoli.hotel.net.GuoliRequest;
 import com.guoli.hotel.utils.DialogUtils;
 import com.guoli.hotel.utils.JsonUtils;
 import com.guoli.hotel.utils.LoginUtils;
 import com.guoli.hotel.widget.BottomTabbar;
 import com.msx7.core.Controller;
 import com.msx7.core.Manager;
 import com.msx7.core.command.ErrorCode;
 import com.msx7.core.command.IResponseListener;
 import com.msx7.core.command.model.Response;
 
 public class OrderAuthenticActivity extends BaseActivity2 {
     public static final String PARAM_AUTHER="login_for_result";
 
     EditText mPhoneEditText;
     EditText mValidateEditText;
     Dialog mDialog;
     boolean isResult;
     
     @Override
     public void onAfterCreate(Bundle savedInstanceState) {
         setTitle(R.string.order_title_search);
        GuoliApplication application=  ((GuoliApplication)Controller.getApplication());
         for (Activity activity : application.activities) {
             activity.finish();
         }
         application.activities.clear();
         application.activities.add(this);
         isResult=getIntent().getBooleanExtra(PARAM_AUTHER, false);
         // 检查是否已经登录
         if (0 == LoginUtils.isLogin){
             Intent intent=new Intent(this, LoginActivity.class);
             if(getIntent().hasExtra(LoginActivity.PARAM_ORDER)){
                 intent.putExtra(LoginActivity.PARAM_ORDER, true);
             }else {
                 intent.putExtra(LoginActivity.PARAM_SEACRCH, true);
             }
             startActivityForResult(intent, 0);
         }else if(!isResult){
             onLoginSuccess();
         }else {
             setResult(LoginActivity.RESULT_LOGIN_OK);
             finish();
         }
         new BottomTabbar(this, 2);
         findViewById(R.id.search_btn).setOnClickListener(mSearchListener);
         mPhoneEditText = (EditText) findViewById(R.id.editText1);
         mValidateEditText = (EditText) findViewById(R.id.editText2);
         findViewById(R.id.button1).setOnClickListener(mGetValidateListener);
     }
 
     @Override
     public int getContentId() {
         return R.layout.order_authentic_activity;
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
         if (LoginUtils.isLogin==2) {
             onLoginSuccess();
         }
     }
     
 
     /**
      * 登录成功之后的操作
      */
     public void onLoginSuccess() {
         if(isResult){
             setResult(LoginActivity.RESULT_LOGIN_OK);
             finish();
             return;
         }
         Intent intent = new Intent(this, OrderHotelListAcivity.class);
         startActivity(intent);
         finish();
     }
 
     /**
      * 非注册用户查询成功之后的切换
      * 
      * @param bundle
      */
     public void onSuccessSwitchOrderList(Bundle bundle) {
         Intent intent = new Intent(OrderAuthenticActivity.this, OrderHotelListAcivity.class);
         intent.putExtras(bundle);
         startActivity(intent);
     }
 
     View.OnClickListener mSearchListener = new View.OnClickListener() {
 
         @Override
         public void onClick(View v) {
             if(TextUtils.isEmpty(mPhoneEditText.getText().toString())){
                 DialogUtils.showDialog("提示", "请输入手机号码", v.getContext());
                 return ;
             }
             if(!validatePhone(mPhoneEditText.getText().toString())){
                 DialogUtils.showDialog("提示", "手机号码格式不正确", v.getContext());
                 return ;
             }
             if(TextUtils.isEmpty(mValidateEditText.getText().toString())){
                 Toast.makeText(OrderAuthenticActivity.this, "请输入验证码", Toast.LENGTH_SHORT).show();
                 return;
             }
             mDialog=DialogUtils.showProgressDialog(v.getContext(), "正在登录中...");
             HashMap<String, String> map=new HashMap<String, String>();
             map.put("mobile", mPhoneEditText.getText().toString());
             map.put("authcode", mValidateEditText.getText().toString());
             Manager.getInstance().executePoset(new GuoliRequest(Action.User.USER_UNLOGIN, map),
                     mUnLoginResponseListener);
         }
     };
     View.OnClickListener mGetValidateListener = new View.OnClickListener() {
 
         @Override
         public void onClick(View v) {
         	if (TextUtils.isEmpty(mPhoneEditText.getText().toString().trim())) {
         		DialogUtils.showDialog("提示", "请输入手机号码", v.getContext());
                 return ;
 			}
         	
             if(!validatePhone(mPhoneEditText.getText().toString())){
                 DialogUtils.showDialog("提示", "手机号码格式不正确", v.getContext());
                 return ;
             }
             HashMap<String, String> map=new HashMap<String, String>();
             map.put("mobile", mPhoneEditText.getText().toString());
             Manager.getInstance().executePoset(new GuoliRequest(Action.General.MobileValidate, map), mValidateResponseListener);
         }
     };
     IResponseListener mUnLoginResponseListener = new IResponseListener() {
 
         @Override
         public void onSuccess(Response arg0) {
           HashMap<String, String> map=  JsonUtils.convertJsonToHashMap(arg0.result.toString());
           if(map.containsKey("message")&&map.containsKey("success")&&"1".equals(map.get("success"))){
               Toast.makeText(OrderAuthenticActivity.this, map.get("message").toString(), Toast.LENGTH_SHORT).show();
               LoginUtils.isLogin=1;
               LoginUtils.mobile=mPhoneEditText.getText().toString();
               onLoginSuccess();
           }else{
               DialogUtils.showDialog("提示", "验证码不正确", OrderAuthenticActivity.this);
               Toast.makeText(OrderAuthenticActivity.this, "您输入的验证码有误", Toast.LENGTH_SHORT).show();
           }
           if(mDialog!=null&&mDialog.isShowing())mDialog.cancel();
         }
 
         @Override
         public void onError(Response arg0) {
             if(mDialog!=null&&mDialog.isShowing())mDialog.cancel();
             Toast.makeText(OrderAuthenticActivity.this, ErrorCode.getErrorCodeString(arg0.errorCode), Toast.LENGTH_SHORT).show();
         }
     };
 
     IResponseListener mValidateResponseListener = new IResponseListener() {
 
         @Override
         public void onSuccess(Response arg0) {
             Log.d("MSG", arg0.result.toString());
         }
 
         @Override
         public void onError(Response arg0) {
             Toast.makeText(OrderAuthenticActivity.this, ErrorCode.getErrorCodeString(arg0.errorCode), Toast.LENGTH_SHORT).show();
         }
     };
     public boolean validatePhone(String phone) {
         if (!TextUtils.isEmpty(phone)&&Pattern.matches("[1]{1}[0-9]{10}", phone)) {            
                 return true;
         }
         return false;
     }
 }
