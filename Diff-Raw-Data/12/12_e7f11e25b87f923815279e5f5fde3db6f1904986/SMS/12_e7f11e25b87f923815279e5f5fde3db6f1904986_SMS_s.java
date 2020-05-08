 /**
  * 
  */
 package cn.game189.sms;
 
 import com.k99k.tools.encrypter.Encrypter;
 
 import android.app.Activity;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.graphics.Color;
 import android.telephony.SmsManager;
 import android.telephony.TelephonyManager;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.Button;
 import android.widget.FrameLayout;
 import android.widget.LinearLayout;
 import android.widget.PopupWindow;
 import android.widget.TextView;
 
 /**
  * 短代计费代码.
  * 
  * <pre>
  * 使用方法：
  * 1.在项目中引入sms.jar包;
  * 2.在AndroidManifest.xml中添加: 
  * {@code 
  * 
  * <!-- 声明权限 -->
  * <uses-permission android:name="android.permission.SEND_SMS" /> 
  * <uses-permission android:name="android.permission.READ_PHONE_STATE" />
  * (前一版本需要设置Activity,本版本不再需要)
  * 
  * }
  * 3.创建SMSListener处理不同的计费点短信发送结果,具体使用详见样例代码。
  * 4.调用静态方法SMS.checkFee()判断是否已计费,注意feeName参数为计费点标识(不可含有#号),每个计费点必须不同。在未成功计费时会自动弹出计费确认提示框，此时checkFee方法返回false。在弹出计费提示框后，通过SMSListener的smsOK接口可判断用户是否确认计费并成功发送短信，可在smsOK方法中处理本次计费成功后的操作（如打开关卡，提供对应道具等），在短信发送成功后会自动加密保存已计费状态(与设备号绑定加密)，下次调用checkFee方法时将返回true。
  * 5.可调用SMS.getResult()随时查看当前错误码.
  * </pre>
  * 
  * @author keel
  *
  */
 public class SMS {
 
 	public static final int version = 1;
 	/**
 	 * 目的号码
 	 */
 	private static String DEST_NUM = "[请使用规范对应费用的短代目的号码]";
 	/**
 	 * 短信内容
 	 */
 	private static String TXT_SMS = "[请使用平台短代串码]";
 	/**
 	 * 提示语
 	 */
 	private static String TXT_TIP = "[请说明计费点内容和购买效果],此操作将发送一条计费短信,收取您x元费用,是否确认?";
 	/**
 	 * 短代是否已发的记录标识
 	 */
 	private static String STR_CHECK = "CHECK_SMS";
 	/**
 	 * 确定发送短信按钮文字 
 	 */
 	private static  String TXT_BT1 = "确定";
 	/**
 	 * 取消发送短信按钮文字
 	 */
 	private static  String TXT_BT2 = "取消";
 	/**
 	 * 发送中提示
 	 */
 	private static  String TXT_SENDING = "短信发送中,请稍侯......";
 	/**
 	 * 发送成功提示
 	 */
 	private static  String TXT_SENT = "发送成功!";
 	/**
 	 * 发送失败提示
 	 */
 	private static  String TXT_ERR = "发送失败!请确认手机使用电信手机卡,短信功能正常,内存空间足够.";
 	
 	
 	private static final String TAG = "EGAME_SMS";
 	
 	/**
 	 * 回传发送结果的SMSListener
 	 */
 	private static SMSListener smsListener;
 	
 	private static Activity actv;
 	
 	private static SharedPreferences ini;
 	private static Button bt1;
 	private static Button bt2;
 	private static TextView txt1;
 	private SmsManager smsm=SmsManager.getDefault();
 	private final static String SENT = "EGAME_SMS_SENT";
 	private final static int SMS_CANCEL = 102;
 	private final static int SMS_SENT_OK = 103;
 	private final static int SMS_SENT_ERR = 104;
 	private final static int SMS_END = 105;
 	private static int sms_close = SMS_CANCEL;
 	private final static int WARP = FrameLayout.LayoutParams.WRAP_CONTENT;
 	private final static int FILL = FrameLayout.LayoutParams.FILL_PARENT;
 	/**
 	 * 是否已注册短信Broadcast的Receiver
 	 */
 	private static boolean isReg = false;
 	/**
 	 * <pre>
 	 * 错误码:
 	 * 0.初始值或取消发送
 	 * 1.已计过费
 	 * 2.发送短信成功
 	 * -1.发送失败
 	 * -2.无卡
 	 * -3.非电信卡
 	 * -4.获取终端码失败
 	 * -5.保存计费点失败
 	 * -6.获取存档数据有误
 	 * -7.获取存档时读终端码失败
 	 * -8.获取存档时feeName不匹配
 	 * -9.获取存档时终端码不匹配
 	 * -10.获取存档发生异常 
 	 * -11.feeName不合法
 	 * </pre>
 	 */
 	private static int result = 0;
 	public final static int RE_ALREADY_FEE = 1;
 	public final static int RE_SMS_SENT = 2;
 	public final static int RE_INIT = 0;
 	public final static int RE_SEND_ERR = -1;
 	public final static int RE_NO_CARD = -2;
 	public final static int RE_NO_TELECOM = -3;
 	public final static int RE_ERR_NO_IMEI = -4;
 	public final static int RE_ERR_SAVE = -5;
 	public final static int RE_ERR_READ_DATA = -6;
 	public final static int RE_ERR_READ_NO_IMEI = -7;
 	public final static int RE_ERR_READ_FEENAME = -8;
 	public final static int RE_ERR_READ_IMEI = -9;
 	public final static int RE_ERR_READ = -10;
 	public final static int RE_ERR_SAVE_FEENAME = -11;
 	public final static int RE_ERR_UNSAVE = -12;
 	
 	
 	/**
 	 * 避免高速点击产生多个计费提示框的锁
 	 */
 	private static boolean lock = false;
 	
 	/**
 	 * 发送点击按钮锁
 	 */
 	private static boolean sendLock = false;
 	
 	private  static PopupWindow pop;
 	
 	private static Context context;
 	/**
 	 * 显示计费提示框
 	 * @param a 发起调用的Activity
 	 */
 	private static void toSMS(Activity a,View view){
 		try {
 			if (pop == null) {
 				makePopupWindow(a,view);
 			}
 			if (!flag) {
 				updatePop();
 				pop.showAtLocation(view, Gravity.CENTER, 0, 0);
 				flag = true;
 			}else{
 				clear();
 			}
 		} catch (Exception e) {
 			clear();
 		}
     }
 	
 	private static void updatePop(){
 		txt1.setText(TXT_TIP);
 		bt1.setVisibility(View.VISIBLE);
 		bt2.setVisibility(View.VISIBLE);
 		bt1.setOnClickListener(new OnClickListener(){
   			public void onClick(View v) {
   				//锁住发送按钮的点击
   				if (!sendLock) {
   					sendLock = true;
   					bt1.setVisibility(View.GONE);
   					txt1.setText(TXT_SENDING);
   					bt2.setVisibility(View.GONE);
   					inst.new SendThread().start();
   				}
   			}
           }); 
 		pop.update();
 	}
 	
 	/**
 	 * 清除窗口
 	 */
 	public static void clear(){
		lock = false;
 		flag = false;
		if (pop != null) {
 			try {
 				pop.dismiss();
 			} catch (Exception e) {
 				Log.e("SMS2", "clear err.......");
 			}
 		}
 		if (isReg) {
 			SMS.context.unregisterReceiver(smsCheck);
 		}
 	}
 	
 	
 	private static void makePopupWindow(Context cx,View view) {
 		context = cx;
 	    LinearLayout layout = new LinearLayout(cx);
 	    
 	  //短信提示界面样式
   		layout.setOrientation(LinearLayout.VERTICAL);
   		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FILL,WARP);
   		//params.setMargins(20, 20, 20, 20);
   		params.gravity = Gravity.CENTER;
   		layout.setLayoutParams(params);
   		layout.setPadding(10, 10, 10, 10);
   		layout.setBackgroundColor(Color.argb(100, 80, 80, 80));
   		
   		LinearLayout up = new LinearLayout(cx);
   		FrameLayout.LayoutParams p_up = new FrameLayout.LayoutParams(FILL,WARP);
   		up.setLayoutParams(p_up);
   		up.setBackgroundColor(Color.argb(255, 36, 36, 36));
   		up.setPadding(15, 15, 15, 15);
   		txt1 = new TextView(cx);
   		ViewGroup.LayoutParams ww = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
   		txt1.setLayoutParams(ww);
   		txt1.setText(TXT_TIP);
   		txt1.setTextColor(Color.argb(255, 255, 255, 255));
   		up.addView(txt1);
   		layout.addView(up);
   		
   		LinearLayout down = new LinearLayout(cx);
   		down.setLayoutParams(p_up);
   		down.setBackgroundColor(Color.argb(255, 36, 36, 36));
   		down.setGravity(Gravity.CENTER_HORIZONTAL);
   		//warp,warp,1 
   		LinearLayout.LayoutParams ww1 = new LinearLayout.LayoutParams(WARP,WARP,1);
   		bt1 = new Button(cx);
   		bt1.setLayoutParams(ww1);
   		bt1.setPadding(15, 15, 15, 15);
   		bt1.setText(TXT_BT1);
   		bt1.setTextColor(Color.argb(255, 0, 0, 0));
   		bt1.setBackgroundColor(Color.argb(255, 255, 255, 255));
   		down.addView(bt1);
   		//按钮间隔
   		TextView empTxt = new TextView(cx);
   		empTxt.setWidth(40);
   		down.addView(empTxt);
   		bt2 = new Button(cx);
   		bt2.setLayoutParams(ww1);
   		bt2.setPadding(15, 15, 15, 15);
   		bt2.setText(TXT_BT2);
   		bt1.setTextColor(Color.argb(255, 0, 0, 0));
   		bt2.setBackgroundColor(Color.argb(255, 255, 255, 255));
   		down.addView(bt2);
   		layout.addView(down);
   		
   		bt1.setOnClickListener(new OnClickListener(){
   			public void onClick(View v) {
   				//锁住发送按钮的点击
   				if (!sendLock) {
   					sendLock = true;
   					bt1.setVisibility(View.GONE);
   					txt1.setText(TXT_SENDING);
   					bt2.setVisibility(View.GONE);
   					inst.new SendThread().start();
   				}
   			}
           }); 
   		
   		bt2.setOnClickListener(new OnClickListener() {
   			public void onClick(View v) {
   				//mHandler.sendEmptyMessage(sms_close);
   				actv.runOnUiThread(inst.new SHandler(sms_close));
   			}
   		});
   		//创建pop
 	    pop = new PopupWindow(layout,LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT,true);
         //设置PopupWindow外部区域是否可触摸
         pop.setOutsideTouchable(false);
 	        
 //	    if (!flag ) {
 //	    	Log.e("SMS2", "-------showAtLocation");
 //	    	pop.showAtLocation(view, Gravity.CENTER, 0, 0);
 //	    	flag = true;
 //		}
 	        
 	}
 	
 	private static boolean flag = false;
 	
 	private static SMS inst = new SMS();
 	
 	private class SendThread extends Thread{
 
 		/* (non-Javadoc)
 		 * @see java.lang.Thread#run()
 		 */
 		@Override
 		public void run() {
 			//是否电信卡,同时提前保存计费成功存档,防止发送过程中意外中止未记下发成功的存档,然后发送短信
 			if (checkIMSI() && saveFee(STR_CHECK)) {
 				// 发短信
 				try {
 					PendingIntent sentPI = PendingIntent.getBroadcast(context, 0,
 							new Intent(SENT), 0);
 					SMS.context.registerReceiver(smsCheck, new IntentFilter(SENT));
 					isReg = true;
 					smsm.sendTextMessage(DEST_NUM, null,TXT_SMS, sentPI, null);
 				} catch (Exception e) {
 					result = RE_SEND_ERR;
 					actv.runOnUiThread(inst.new SHandler(SMS_SENT_ERR));
 //					mHandler.sendEmptyMessage(SMS_SENT_ERR);
 				}
 			}else{
 //				mHandler.sendEmptyMessage(SMS_SENT_ERR);
 				actv.runOnUiThread(inst.new SHandler(SMS_SENT_ERR));
 			}
 		}
 
 	}
 	
 	
 	private static BroadcastReceiver smsCheck = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context arg0, Intent arg1) {
 			switch (getResultCode()) {
 			case Activity.RESULT_OK:
 				result = RE_SMS_SENT;
 				Log.i(TAG, "SMS send ok");
 				sms_close = SMS_END;
 //				mHandler.sendEmptyMessage(SMS_SENT_OK);
 				actv.runOnUiThread(inst.new SHandler(SMS_SENT_OK));
 				break;
 			default:
 				result = RE_SEND_ERR;
 				Log.e(TAG, "SMS send err:" + getResultCode());
 //				mHandler.sendEmptyMessage(SMS_SENT_ERR);
 				actv.runOnUiThread(inst.new SHandler(SMS_SENT_ERR));
 				break;
 			}
 			sendLock = false;
 		}
 	};
 
 	/**
 	 * 发送成功
 	 */
 	private  static void sendOK(){
 		bt1.setVisibility(View.INVISIBLE);
 		bt2.setVisibility(View.VISIBLE);
 		txt1.setText(TXT_SENT);
 		bt1.setClickable(false);
 		bt2.setText("关闭");
 		//调用smsListener
 		smsListener.smsOK(STR_CHECK);
 	}
 	
 	/**
 	 * 发送失败
 	 */
 	private  static void sendErr(){
 		unSaveFee(STR_CHECK);
 		bt1.setVisibility(View.INVISIBLE);
 		bt2.setVisibility(View.VISIBLE);
 		txt1.setText(TXT_ERR);
 		bt1.setClickable(false);
 		bt2.setText("关闭");
 		smsListener.smsFail(STR_CHECK,result);
 	}
 	
 	/**
 	 * 取消发送
 	 */
 	private  static void sendCancel(){
 		//删除计费成功存档
 		unSaveFee(STR_CHECK);
 		clear();
 		smsListener.smsCancel(STR_CHECK, RE_INIT);
 	}
 	
 	/**
 	 * 发送完成后关闭
 	 */
 	private  static void end(){
 		clear();
 	}
 	
 //	/**
 //	 * 消息处理Handler,用于更新界面
 //	 */
 //	private  static Handler mHandler = new Handler(){
 //		@Override
 //		public void handleMessage(Message msg) {
 //			super.handleMessage(msg);
 //			switch (msg.what) {
 //			case SMS_SENT_OK:
 //				sendOK();
 //				break;
 //			case SMS_SENT_ERR:
 //				sendErr();
 //				break;
 //			case SMS_CANCEL:
 //				sendCancel();
 //				break;
 //			case SMS_END:
 //				end();
 //				break;
 //			}
 //			
 //		}
 //	};
 	
 	
 	class SHandler implements Runnable {
 
 		SHandler(int msg){
 			this.msg = msg;
 		}
 		
 		private int msg;
 		
 		public void run() {
 			switch (msg) {
 			case SMS_SENT_OK:
 				sendOK();
 				break;
 			case SMS_SENT_ERR:
 				sendErr();
 				break;
 			case SMS_CANCEL:
 				sendCancel();
 				break;
 			case SMS_END:
 				end();
 				break;
 			}
 		}
 
 	}
 	
 	/**
 	 * 获取错误码
 	 * @return
 	 */
 	public static int getResult(){
 		return result;
 	}
 	
 	/**
 	 * 游戏启动
 	 * @param activity
 	 */
 	public static void gameStart(Activity activity) {
 		Intent intent = new Intent("com.egame.opengameaction");
 	//actionType（int） 调用类型：1 游戏启动，2 游戏退出，3 触发计费点。
 	//packageName （String） 应用包名 activity.getPackageName();
 		intent.putExtra("actionType", 1);
 		intent.putExtra("packageName", activity.getPackageName());
 		Log.d(TAG, "game start");
 		activity.sendBroadcast(intent);
 	}
 	
 	/**
 	 * 游戏退出
 	 * @param activity
 	 */
 	public static void gameExit(Activity activity) {
 		Intent intent = new Intent("com.egame.opengameaction");
 		intent.putExtra("actionType", 2);
 		intent.putExtra("packageName", activity.getPackageName());
 		Log.d(TAG, "game exit");
 		activity.sendBroadcast(intent);
 	}
 	
 	/**
 	 * 验证计费点.为短代使用的公开静态方法.是短代功能入口
 	 * @param feeName 计费点标识符
 	 * @param activity Activity 不能为null
 	 * @param listener SMSListener接口,处理发送成功和失败的操作,不能为null
 	 * @param feeCode 短代代码
 	 * @param tip 短代提示语
 	 * @param okInfo 短代发送成功的提示语
 	 * @param isRepeat  是否可重复计费（true为软计费点,false为硬计费点）
 	 * @return 是否计过费
 	 */
 	public static boolean checkFee(String feeName,Activity activity,SMSListener sListener,String feeCode,String tip,String okInfo,boolean isRepeat){
 		//防止快速点击出现多个弹窗
 		if (lock) {
 			return false;
 		}else{
 			lock = true;
 		}
 		if (activity == null || sListener == null ) {
 			Log.e(TAG, "checkFee - Activity or SMSListener is null!!");
 			return false;
 		}
 		View view = activity.getWindow().getDecorView();
 		//初始化状态
 		isReg = false;
 		clear();
 		result = RE_INIT;
 		actv = activity;
 		//判断是否已计过费
 		if (!isRepeat && isFee(feeName)) {
 			result = RE_ALREADY_FEE;
 			lock = false;
 			return true;
 		}
 		sms_close = SMS_CANCEL;
 		sendLock = false;
 		// 根据费用大小确定目的号码
 		String fee = feeCode.substring(0,2);
 //		char feeType = feeCode.charAt(3);
 		DEST_NUM = "106598110"+fee;
 //		if (feeType == '3') {
 //			DEST_NUM = "106598112"+fee;
 //		}else if(feeType == '1'){
 //			DEST_NUM = "106598110"+fee;
 //		}else if(feeType == '4'){
 //			DEST_NUM = "106598115"+fee;
 //		}
 		// 短代位置标识字符串，用于区分不同的计费点,其他的计费点需要修改此标识
 		STR_CHECK = feeName;
 		// 短代串
 		TXT_SMS = feeCode;
 		// 短代计费前的提示语
 		TXT_TIP = tip;
 		// 计费成功的提示语
 		TXT_SENT = okInfo;
 		//smsListener获取结果
 		smsListener = sListener;
 		toSMS(actv,view);
 		
 		Intent intent = new Intent("com.egame.opengameaction");
 		intent.putExtra("actionType", 3);
 		intent.putExtra("packageName", activity.getPackageName());
 		activity.sendBroadcast(intent);
 		Log.d(TAG, "opengameaction");
 		return false;
 	}
 
 	/**
 	 * 保存计费成功结果,注意feeName不能包含#号,否则直接返回false
 	 * @param feeName
 	 * @param context Context
 	 */
 	private static boolean saveFee(String feeName){
 		if (feeName.indexOf("#")>=0) {
 			result = RE_ERR_SAVE_FEENAME;
 			return false;
 		}
 		
 		try {
 			if (ini==null) {
 				ini = actv.getSharedPreferences("EGAME_SMS",0);
 			}
 			SharedPreferences.Editor editor = ini.edit();
 			String imei = getIMEI(actv);
 			if (imei == null) {
 				result = RE_ERR_NO_IMEI;
 				return false;
 			}
 			//feeName#imei#currentTimeMillis 将存档与手机imei绑定并进行加密保存
 			String save = feeName+"#"+imei+"#"+System.currentTimeMillis();
 			String enc = Encrypter.encrypt(save);
 			editor.putString(feeName, enc);
 			editor.commit();
 		} catch (Exception e) {
 			Log.e(TAG, "saveFee error.",e);
 			result = RE_ERR_SAVE;
 			return false;
 		}
 		return true;
 	}
 	
 	/**
 	 * 删除存档
 	 * @param feeName 
 	 * @return
 	 */
 	private static boolean unSaveFee(String feeName){
 		try {
 			if (ini==null) {
 				ini = actv.getSharedPreferences("EGAME_SMS",0);
 			}
 			SharedPreferences.Editor editor = ini.edit();
 			editor.remove(feeName);
 			editor.commit();
 			return true;
 		} catch (Exception e) {
 			Log.e(TAG, "unSaveFee error.",e);
 			result = RE_ERR_UNSAVE;
 			return false;
 		}
 	}
 	
 	/**
 	 * 判断是否已扣费
 	 * @param feeName
 	 * @param context Context
 	 */
 	private static boolean isFee(String feeName){
 		try {
 			if (ini==null) {
 				ini = actv.getSharedPreferences("EGAME_SMS",0);
 			}
 			String fee = ini.getString(feeName, "");
 			if (fee.equals("")) {
 				return false;
 			}
 			String imei = getIMEI(actv);
 			if (imei == null) {
 				result = RE_ERR_READ_NO_IMEI;
 				return false;
 			}
 			String des = Encrypter.decrypt(fee);
 			String[] sa = des.split("#");
 			if (sa.length != 3) {
 				result = RE_ERR_READ_DATA;
 				return false;
 			}
 			if (!feeName.equals(sa[0])) {
 				result = RE_ERR_READ_FEENAME;
 				return false;
 			}
 			if (!imei.equals(sa[1])) {
 				result = RE_ERR_READ_IMEI;
 				return false;
 			}
 		} catch (Exception e) {
 			Log.e(TAG, "isFee error.",e);
 			result = RE_ERR_READ;
 			return false;
 		}
 		return true;
 	}
 	
 
 	private boolean checkIMSI(){
 			TelephonyManager telManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);  
 			/** 获取SIM卡的IMSI码 
 			 * SIM卡唯一标识：IMSI 国际移动用户识别码（IMSI：International Mobile Subscriber Identification Number）是区别移动用户的标志， 
 			 * 储存在SIM卡中，可用于区别移动用户的有效信息。IMSI由MCC、MNC、MSIN组成，其中MCC为移动国家号码，由3位数字组成， 
 			 * 唯一地识别移动客户所属的国家，我国为460；MNC为网络id，由2位数字组成， 
 			 * 用于识别移动客户所归属的移动网络，中国移动为00，中国联通为01,中国电信为03；MSIN为移动客户识别码，采用等长11位数字构成。 
 			 * 唯一地识别国内GSM移动通信网中移动客户。所以要区分是移动还是联通，只需取得SIM卡中的MNC字段即可 
 			 */
 			String imsi = telManager.getSubscriberId();
 			if (imsi != null) {
 				if (imsi.startsWith("46003") || imsi.startsWith("46005")) {
 					// 中国电信
 					return true;
 				}else{
 					result = RE_NO_TELECOM;
 				}
 	//			if (imsi.startsWith("46000") || imsi.startsWith("46002")) {// 因为移动网络编号46000下的IMSI已经用完，所以虚拟了一个46002编号，134/159号段使用了此编号
 	//				// 中国移动
 	//			} else if (imsi.startsWith("46001")) {
 	//				// 中国联通
 	//			} else if (imsi.startsWith("46003")) {
 	//				// 中国电信
 	//			}
 			}else{
 				result = RE_NO_CARD;
 			}
 			return false;
 		}
 
 	private static String getIMEI(Context context){
 		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
 		return tm.getDeviceId();
 	}
 
 }
