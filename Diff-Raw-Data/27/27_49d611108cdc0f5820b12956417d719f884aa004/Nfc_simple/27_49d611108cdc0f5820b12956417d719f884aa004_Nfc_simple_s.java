 package com.gmail.matsushige.nfcv2;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.graphics.Color;
 import android.nfc.NfcAdapter;
 import android.nfc.Tag;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.ToggleButton;
 
 import com.gmail.matsushige.R;
 import com.gmail.matsushige.nfcv2.db.ActLogDatabase;
 import com.gmail.matsushige.nfcv2.db.Database;
 import com.gmail.matsushige.nfcv2.db.TemporaryUsersDatabaseHelper;
 import com.gmail.matsushige.nfcv2.db.TemporaryUsersDatabaseOperate;
 import com.gmail.matsushige.nfcv2.db.UsersDatabase;
 
 import java.util.Calendar;
 
 public class Nfc_simple extends BaseActivity {
 	private TextView timeText;
 	private byte[] id;
 //	private String tech = "";
 	private String type = "";
 //	public static UsbManager mUsbManager;
 //	public static PendingIntent mPermissionIntent;
 //	public static final String ACTION_USB_PERMISSION = "com.gmail.matsushige.RelaySample.action.USB_PERMISSION";
 //	public static UsbAccessory mAccessory;
 
 	private static final int FIRST_USER = 1;
 	private static final int TEMPORARY_USER = 2;
 	private static final int REGULAR_USER = 3;
 	
 	public static String cardOwner = "";
 	public static String regCode = "";
 	
 	public testBroadcastReceiver testReceiver;
 	public test2BroadcastReceiver test2Receiver;
 	public int screenState = 0;	
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.nfc_main);
 		
 //		mUsbManager = (UsbManager) getSystemService(USB_SERVICE);
 //		mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(
 //				ACTION_USB_PERMISSION), 0);
 //		IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
 //		filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
 //		registerReceiver(Relay.mUsbReceiver, filter);
 		Relay.test(getApplicationContext());
 		
 		linearLayout = (LinearLayout) findViewById(R.id.linearLayoutMain);
 		LayoutInflater li = getLayoutInflater();
 		li.inflate(R.layout.nfc_main_start, linearLayout);
 		
 		checkOutletId();
 	}// onCreate
 	
 	@Override
 	public void onResume() {
 		super.onResume();
 		Relay.setRelayOnResume();
 		
 		testReceiver = new testBroadcastReceiver();
 		IntentFilter filter = new IntentFilter();
 		filter.addAction("TEST_RECEIVE_ACTION");
 		registerReceiver(testReceiver, filter);
 		
 		test2Receiver = new test2BroadcastReceiver();
 		IntentFilter filter2 = new IntentFilter();
 		filter2.addAction("TEST2_RECEIVE_ACTION");
 		filter2.addAction("TEST3_RECEIVE_ACTION");
 		registerReceiver(test2Receiver, filter2);
 		
 		SharedPreferences pref = getSharedPreferences("SNS_OUTLET", MODE_PRIVATE);
 		if (!(pref.getBoolean("timerSet", false))) {
 			DayAlarmManager.regularShortTimerSet(getApplicationContext());
 			Editor edit = pref.edit();
 			edit.putBoolean("timerSet", true);
 			edit.commit();
 		}else{
 			Log.d("Activity", "already_set");
 		}
 
 		String action = getIntent().getAction();
 		if (action.equals(NfcAdapter.ACTION_TECH_DISCOVERED)) {
 			readNfc();
 		} else if (action.equals(NfcAdapter.ACTION_TAG_DISCOVERED)) {
 			readNfc();
 		}// if
 	}// onResume
 
 	@Override
 	public void onPause() {
 		super.onPause();
 		if (!(CountRelayTime.isUsed)) {
 			Relay.closeAccessory();
 		}
 		unregisterReceiver(testReceiver);
 		unregisterReceiver(test2Receiver);
 	}// onPause
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 //		unregisterReceiver(Relay.mUsbReceiver);
 		Relay.unRegisterReceiver(getApplicationContext());
 	}// onDestroy
 
 
 	private void firstUsersPic(){
 		linearLayout.removeAllViews();
 		LayoutInflater li = getLayoutInflater();
 		li.inflate(R.layout.nfc_main_first, linearLayout);
 		
 		screenState = FIRST_USER;
 		
 		startCountTimeFirstUser();
 		Button registYesButton = (Button) findViewById(R.id.buttonResistYes);
 		Button registNoButton = (Button) findViewById(R.id.buttonResistNo);
 		
 		registYesButton.setOnClickListener(new OnClickListener() {
 			
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				if (CountTimeFirstUser.isUsed) {
 					CountTimeFirstUser.retainUserData = true;
 					CountTimeFirstUser.finish = true;
 				}
 				regCode = GenerateRegisterCode();
 				Log.d("Activity", regCode);
 				
 				usersInput(type, hex(id).toUpperCase());
 //				if (type.equals("f")) {
 //					usersInput(type, hex(id).toUpperCase());
 //				} else if (type.equals("b")) {
 //					usersInput(type, hex(id).toUpperCase());
 //				} else if (type.equals("a")) {
 //					usersInput(type, hex(id).toUpperCase());
 //				}
 				if (!(SendDataService.getIsUsed())) {
 					Intent intent = new Intent(getApplicationContext(),
 							SendDataService.class);
 					startService(intent);
 				}
 				tempUsersPic();
 			}// onClick
 		});
 		
 		registNoButton.setOnClickListener(new OnClickListener() {
 			
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				if (CountTimeFirstUser.isUsed) {
 					CountTimeFirstUser.finish = true;
 				}
 				changeMainXto0();
 			}// onClick
 		});
 	}// firstUsersPic
 
 	private void tempUsersPic(){
 		linearLayout.removeAllViews();
 		LayoutInflater li = getLayoutInflater();
 		li.inflate(R.layout.nfc_main_temporary, linearLayout);
 		
 		screenState = TEMPORARY_USER;
 		
 		CountRelayTime.maxCount = 10;
 		
 		//TODO 以下操作部分
 		TextView userNameText = (TextView) findViewById(R.id.textViewUserName);
 		TextView explainText = (TextView) findViewById(R.id.textViewExplain);
 		userNameText.setText("ユーザー登録番号は、\n「"+ regCode +"」です。");
 		userNameText.setTextColor(Color.RED);
 		explainText.setText("みんなでおでんき\n(http://odenki.org)\nにアクセスしてユーザ登録番号を入力してください");
 		ImageView qrCode = (ImageView) findViewById(R.id.imageViewQR);
 		qrCode.setImageBitmap(MakeQRCode.getQRCode("http://odenki.org/api/outlet/" + regCode));
 		
 		Button powerCancelButton = (Button) findViewById(R.id.buttonPowerCancel);
 		ToggleButton relay1Toggle = (ToggleButton)findViewById(R.id.toggleRelay1);
 		ToggleButton relay2Toggle = (ToggleButton)findViewById(R.id.toggleRelay2);
 		
 		/** すでにリレー使用中であればトグルボタンを押した状態にする */
 		if(Relay.getRelay(0).isClosed()){
 			relay1Toggle.setChecked(true);
 		}
 		if(Relay.getRelay(1).isClosed()){
 			relay2Toggle.setChecked(true);
 		}
 
 		startCountTimeAllUser();
 		
 		powerCancelButton.setOnClickListener(new OnClickListener() {
 
 			public void onClick(View v) {
 				Relay.getRelay(0).open();
 				Relay.getRelay(1).open();
 				if(CountTimeAllUser.isUsed){
 					CountTimeAllUser.finish = true;
 				}
 				if(CountRelayTime.isUsed){
 					CountRelayTime.finish = true;
 				}
 				changeMainXto0();
 			}// onClick
 		});
 
 		relay1Toggle.setOnClickListener(new OnClickListener() {
 			
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				if(CountTimeAllUser.isUsed){
 					CountTimeAllUser.retainUserData = true;
 					CountTimeAllUser.finish = true;
 				}
 				if(!(CountRelayTime.isUsed)){
 					startCountRelayTime();
 				}
 				if(Relay.getRelay(0).isOpened()){
 					Relay.getRelay(0).close();
 				}else if(Relay.getRelay(0).isClosed()){
 					Relay.getRelay(0).open();
 				}
 				relayState(0);
 			}// onClick
 		});
 	
 		relay2Toggle.setOnClickListener(new OnClickListener() {
 			
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				if(CountTimeAllUser.isUsed){
 					CountTimeAllUser.retainUserData = true;
 					CountTimeAllUser.finish = true;
 				}
 				if(!(CountRelayTime.isUsed)){
 					startCountRelayTime();
 				}
 				if(Relay.getRelay(1).isOpened()){
 					Relay.getRelay(1).close();
 				}else if(Relay.getRelay(1).isClosed()){
 					Relay.getRelay(1).open();
 				}
 				relayState(1);
 			}// onClick
 		});
 	}// tempUsersPic
 	
 	private void reguUsersPic(){
 		linearLayout.removeAllViews();
 		LayoutInflater li = getLayoutInflater();
 		li.inflate(R.layout.nfc_main_regular, linearLayout);
 		
 		screenState = REGULAR_USER;
 		
 		TextView userNameText = (TextView) findViewById(R.id.textViewUserName);
 		userNameText.setText(cardOwner + "さん、こんにちは。");
 
 		Button powerCancelButton = (Button) findViewById(R.id.buttonPowerCancel);
 		ToggleButton relay1Toggle = (ToggleButton)findViewById(R.id.toggleRelay1);
 		ToggleButton relay2Toggle = (ToggleButton)findViewById(R.id.toggleRelay2);
 		
 		/** すでにリレー使用中であればトグルボタンを押した状態にする */
 		if(Relay.getRelay(0).isClosed()){
 			relay1Toggle.setChecked(true);
 		}
 		if(Relay.getRelay(1).isClosed()){
 			relay2Toggle.setChecked(true);
 		}
 
 		CountRelayTime.maxCount = 120;
 		startCountTimeAllUser();
 		
 		powerCancelButton.setOnClickListener(new OnClickListener() {
 
 			public void onClick(View v) {
 				Relay.getRelay(0).open();
 				Relay.getRelay(1).open();
 				if(CountTimeAllUser.isUsed){
 					CountTimeAllUser.finish = true;
 				}
 				if(CountRelayTime.isUsed){
 					CountRelayTime.finish =true;
 				}
 				changeMainXto0();
 			}// onClick
 		});
 
 		relay1Toggle.setOnClickListener(new OnClickListener() {
 			
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				if(CountTimeAllUser.isUsed){
 					CountTimeAllUser.retainUserData = true;
 					CountTimeAllUser.finish = true;
 				}
 				if(!(CountRelayTime.isUsed)){
 					startCountRelayTime();
 				}
 				if(Relay.getRelay(0).isOpened()){
 					Relay.getRelay(0).close();
 				}else if(Relay.getRelay(0).isClosed()){
 					Relay.getRelay(0).open();
 				}
 				relayState(0);
 			}// onClick
 		});
 	
 		relay2Toggle.setOnClickListener(new OnClickListener() {
 			
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				if(CountTimeAllUser.isUsed){
 					CountTimeAllUser.retainUserData = true;
 					CountTimeAllUser.finish = true;
 				}
 				if(!(CountRelayTime.isUsed)){
 					startCountRelayTime();
 				}
 				if(Relay.getRelay(1).isOpened()){
 					Relay.getRelay(1).close();
 				}else if(Relay.getRelay(1).isClosed()){
 					Relay.getRelay(1).open();
 				}
 				relayState(1);
 			}// onClick
 		});
 	}// reguUsersPic	
 	
 	public void changeMainXto0() { // スタート画面へ
 		timeText = (TextView) findViewById(R.id.textViewTime);
 		timeText.setText("");
 		linearLayout.removeAllViews();
 		LayoutInflater li = getLayoutInflater();
 		li.inflate(R.layout.nfc_main_start, linearLayout);
 		screenState = 0;
 	}// changeMainXto0
 
 	public void readNfc() {
 		Intent intent = getIntent();
 		id = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
 		Tag tag = (Tag) intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
 
 //		String idD = "ID : " + hex(id);
 		String[] techList = tag.getTechList();
 		String tech = "TechList : ";
 		for (String w : techList) {
 			tech += w;
 		}// for
 
 		if (tech.contains("NfcF")) {
 			type = "f";
 			recordId(type, hex(id).toUpperCase());
 		}// if <NfcF>
 		else if (tech.contains("NfcB")) {
 			type = "b";
 			recordId(type, hex(id).toUpperCase());
 		}// else if <NfcB>
 		else if (tech.contains("NfcA")) {
 			type = "a";
 			recordId(type, hex(id).toUpperCase());
 		}// else if <NfcA>
 		else {
 			Toast.makeText(getApplicationContext(), "対応していないカードです",
 					Toast.LENGTH_SHORT).show();
 		}// else
 	}// readNfc
 
 	public String hex(byte[] data) {
 		String hexText = "";
 		for (byte v : data) {
 			hexText += String.format("%02x", new Object[] { v });
 		}// for
 		return hexText;
 	}// hex
 
 	public void recordId(String type, String id) {
 
 		long timestamp = Calendar.getInstance().getTimeInMillis();
 		Database.write(this, type, id, timestamp);
 		
 		//TODO プリファレンスチェック
 		SharedPreferences pref = getSharedPreferences("SNS_OUTLET", MODE_PRIVATE);
 		String userType = pref.getString("userType", "");
 		String userId = pref.getString("userId", "");
 		if(userType.equals("") && userId.equals("")){
 			recordPreference(type, id);
 		}else if(type.equals(userType) && id.equals(userId)){
 			Toast.makeText(getApplicationContext(), "あなたは使用中です", Toast.LENGTH_SHORT).show();
 		}else{
 			Toast.makeText(getApplicationContext(), "ほかのユーザが使用中です", Toast.LENGTH_SHORT).show();
 			return;
 		}
 		
 		UsersDatabase.checkResist(this, type, id);
 		
 		if (!("".equals(cardOwner))) {
 			ActLogDatabase.getTheInstance(this).write(this, id, cardOwner, "タッチ", timestamp);
 			reguUsersPic();
 		} else {
			TemporaryUsersDatabaseOperate.checkRegisteredData(getApplicationContext(), type, id);
 			if(!("".equals(cardOwner))){
 				ActLogDatabase.getTheInstance(this).write(this, id, cardOwner, "タッチ", timestamp);
 				tempUsersPic();
 			}else{
 				cardOwner = "未登録者";
 				ActLogDatabase.getTheInstance(this).write(this, id, cardOwner, "タッチ", timestamp);
 				firstUsersPic();
 			}// else
 		}// else
 	}// recordId
 	
 	/** outletId（プリファレンス保存）を"AB"に変更 */
 	private void checkOutletId(){
 		SharedPreferences pref = getSharedPreferences("SNS_OUTLET", MODE_PRIVATE);
 		String outletId = pref.getString("outletId", "AA");
 		if (outletId == "AA"){
 			Log.d("Activity", "first_pref");
 			Editor edit = pref.edit();
 			edit.putString("outletId", "AB");
 			edit.commit();
 		}else{
 			Log.d("Activity", "second~_pref");
 		}// else
 	}// checkOutletId
 	
 	/** TemporaryUsersDatabaseに記録 */
 	public void usersInput(String type, String id){
		TemporaryUsersDatabaseOperate.write(getApplicationContext(), type, id, regCode);
 		Toast.makeText(getApplicationContext(), "登録しました" + "(" +regCode + ")", Toast.LENGTH_SHORT).show();
 		
		TemporaryUsersDatabaseOperate.checkRegisteredData(getApplicationContext(), type, id);
 		long time = Calendar.getInstance().getTimeInMillis();
 		ActLogDatabase.getTheInstance(this).write(this, id, cardOwner, "登録", time);
 	}// usersInput
 	
 	public void relayState(int target){
 		String state ="";
 		if(Relay.getRelay(target).isClosed()){
 			state = " ON";
 		}else if(Relay.getRelay(target).isOpened()){
 			state = " OFF";
 		}
 		long time = Calendar.getInstance().getTimeInMillis();
 		ActLogDatabase.getTheInstance(this).write(this, hex(id).toUpperCase(), cardOwner, "リレー" + (target + 1)+ state, time);
 	}// relayState
 		
 	/** ユーザ登録番号作成手順
 	 *　1.プリファレンスに保存されたコンセントIDを取得し、英字部分とする
 	 *　2.仮登録ユーザデータベースの"register_code"から最後に発行された数字を取得する
 	 *　3.取得した数字に1足した値をユーザ登録番号の数字部分とする
 	 *　4.英字部分と数字部分を組み合わせる */
 	private String GenerateRegisterCode(){
 		String outletCode = generateOutletCode();
 		String preNumber = getPreviousNumber(outletCode);
 		String numberCode = generateNumberCode(preNumber);
 		String registerCode = outletCode + numberCode;
 		Log.d("AService", registerCode);
 		return registerCode;
 	}
 	
 	/** 英字部分作成 */
 	private String generateOutletCode(){
 		SharedPreferences pref = getSharedPreferences("SNS_OUTLET", MODE_PRIVATE);
 		String outletCode = pref.getString("outletId", "AA");
 		return outletCode;
 	}
 	/** 仮登録データベースから最後に発行された数字を取得 */
 	private String getPreviousNumber(String outletCode){
 		String preNumber = "";
 		SQLiteDatabase db = (new TemporaryUsersDatabaseHelper(this)).getReadableDatabase();
 		/** "temporary_users"の"register_time"列のデータを降順にし、Cursorを取得 */
 		String[] columns = {"register_code", "register_time"};
 		Cursor c = db.query("tempusers", columns, null, null, null, null, "register_time desc");
 		if (c.getCount() > 0) {
 			c.moveToFirst();
 			String regCode = c.getString(c.getColumnIndex("register_code"));
 			preNumber = regCode.replace(outletCode, "");
 			Log.d("AService", "getRegCode:" + preNumber);
 		}
 		db.close();
 		if(preNumber.equals("")){
 			preNumber = "9999";
 			Log.d("AService", "reset");
 		}
 		return preNumber;
 	}// getPreviousNumber
 
 	/** 数字部分の作成 */
 	private String generateNumberCode(String preNumber){
 	    int intNumber = 0;
 	    String numberCode = "";
 	    /** String → int変換 */
 	    try{
 		int intPreNumber = Integer.parseInt(preNumber);
 		if(intPreNumber >9998){
 			intNumber = 0;
 		}else{
 			intNumber = intPreNumber + 1;
 		}// else
 		numberCode = adjust4digit(intNumber);
 	    }catch(Exception e){
 	    	Log.e("AService", e.toString());
 	    }
 		return numberCode;
 	}// generateNuberCode
 	
 	/** 数字を4桁にする */
 	private String adjust4digit(int rawData){
 		String newData = "";
 		if(rawData < 10){
 			newData = "000" + rawData;
 		}else if(rawData < 100){
 			newData = "00" + rawData;
 		}else if(rawData < 1000){
 			newData = "0" + rawData;
 		}else{
 			newData = "" + rawData;
 		}// else
 		return newData;
 	}// adjust4digit
 	
 	private void startCountRelayTime(){
 		if (!(CountRelayTime.isUsed)) {
 			Intent intent = new Intent(getApplicationContext(), CountRelayTime.class);
 //			intent.putExtra("cardType", type);
 			startService(intent);
 		}else{
 			Toast.makeText(getApplicationContext(), "CountRelayTimeisUsed", Toast.LENGTH_SHORT).show();
 		}// else
 	}// startCountTime
 	
 	private void startCountTimeAllUser(){
 		if (!(CountRelayTime.isUsed)) {
 			if (!(CountTimeAllUser.isUsed)) {
 				Intent intent = new Intent(getApplicationContext(),
 						CountTimeAllUser.class);
 				startService(intent);
 			} else {
 				Toast.makeText(getApplicationContext(), "CountTime2isUsed",
 						Toast.LENGTH_SHORT).show();
 			}// else
 		}else{
 			Toast.makeText(getApplicationContext(), "countTime > countTime2", Toast.LENGTH_SHORT).show();
 		} // else
 	}// startCountTimeAllUser
 	
 	private void startCountTimeFirstUser(){
 		if(!(CountTimeFirstUser.isUsed)){
 			Intent intent = new Intent(getApplicationContext(),
 					CountTimeFirstUser.class);
 			startService(intent);
 		}// if
 	}// startCountTimeFirstUser
 	
 	private void recordPreference(String type, String id){
 		SharedPreferences sdf = getSharedPreferences("SNS_OUTLET", MODE_PRIVATE);
 		Editor edit = sdf.edit();
 		edit.putString("userType", type);
 		edit.putString("userId", id);
 		edit.commit();
 	}// recordPreference
 	
 	private void resetPreference(){
 		SharedPreferences sdf = getSharedPreferences("SNS_OUTLET", MODE_PRIVATE);
 		Editor edit = sdf.edit();
 		edit.putString("userType", "");
 		edit.putString("userId", "");
 		edit.commit();
 	}// resetPreference
 	
 	public class testBroadcastReceiver extends BroadcastReceiver {
 
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			// TODO Auto-generated method stub
 			Bundle bundle = intent.getExtras();
 			int count = bundle.getInt("count");
 			/** 本登録ユーザ画面であればテキスト表示 */
 			if (screenState == REGULAR_USER || screenState == TEMPORARY_USER) {
 				timeText = (TextView) findViewById(R.id.textViewTime);
 //				timeText.setText("あと"+(CountRelayTime.maxCount - count)+"秒で終了します");
 				timeText.setText("あと" + secToMin(CountRelayTime.maxCount - count) +"で終了します");
 			}// if
 			if(count == CountRelayTime.maxCount){
 				resetPreference();
 				Toast.makeText(getApplicationContext(), "使用可能時間が過ぎました", Toast.LENGTH_SHORT).show();
 				changeMainXto0();
 			}// if
 		}// onReceive
 		
 		private String secToMin(int sec){
 			String minSecData = "";
 			if(sec > 59){
 				int syou = sec / 60; // "/":商
 				int amari = sec % 60; //  "%":余
 				minSecData = syou + "分" + amari + "秒";
 			}else{
 				minSecData = sec + "秒";
 			}// else
 			return minSecData;
 		}// secToMin
 		
 	}// testBroadcastReceiver
 	
 	/** CountTime2から受け取る */
 	public class test2BroadcastReceiver extends BroadcastReceiver{
 
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			// TODO Auto-generated method stub
 			String action = intent.getAction();
 			Bundle bundle = intent.getExtras();
 			if(action.equals("TEST2_RECEIVE_ACTION")){
 				int count = bundle.getInt("count");
 				if (screenState == REGULAR_USER || screenState == TEMPORARY_USER) {
 					timeText = (TextView) findViewById(R.id.textViewTime);
 					timeText.setText("あと" + (CountTimeAllUser.maxCount - count)
 							+ "秒でスタート画面に戻ります");
 				}
 				if (count == CountTimeAllUser.maxCount) {
 					// resetPreference();
 					changeMainXto0();
 				}// if
 			}else if(action.equals("TEST3_RECEIVE_ACTION")){
 				int count = bundle.getInt("count");
 				if(screenState == FIRST_USER){
 					timeText = (TextView) findViewById(R.id.textViewTime);
 					timeText.setText("あと" + (CountTimeFirstUser.maxCount - count) + "秒でスタート画面に戻ります");
 				}
 				if(count == CountTimeFirstUser.maxCount){
 					changeMainXto0();
 				}
 			}
 		}// onReceive
 
 	}// test2BroadcastReceiver
 }// Nfc_simple
 
