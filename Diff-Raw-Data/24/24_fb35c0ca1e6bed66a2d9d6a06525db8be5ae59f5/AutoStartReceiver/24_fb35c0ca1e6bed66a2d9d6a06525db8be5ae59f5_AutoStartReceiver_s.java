 package com.ipuweb.freezealarm;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.util.Log;
 
 public class AutoStartReceiver extends BroadcastReceiver {
 	private Context ctx;
 
     // BroadcastIntentを受信した場合の処理
     @Override
     public void onReceive(Context context, Intent intent) {
     	Log.d("freezeAlart", "Start Receiver actionName=" + intent.getAction());
     	ctx = context;
     	
     	// Android 電源ON時に自動でサービスを起動
     	// TODO:isseium actionName を調べてifを設定
     	Intent serviceIntent = new Intent(context, FreezeAlarmService.class);
 	    context.startService(serviceIntent);
 	    
 	    // AlarmManager からの受信（freezeAlarm)
 	    if(intent.getAction().equals("freezeAlarm")){
 	    	Log.d("freezeAlart", "Alarm");
 			WeatherAccessor wa = new WeatherAccessor();
 			
 			// 対象地域を取得し、クエリにマッピング
 			// TODO: 定数化
 		    SharedPreferences pref = context.getSharedPreferences("freezealarm", Context.MODE_PRIVATE);
 		    String locationId = pref.getString("locate", "0");	// デフォルトは 0.盛岡
 		    String[] locations = context.getResources().getStringArray(R.array.location_array);
 		    String[] location_labels = context.getResources().getStringArray(R.array.location_array_label);
 			JSONArray weathers = wa.getTommorowWeather(locations[Integer.parseInt(locationId)]);	// TODO:isseium getTommorowWeather って名前わるい
 			
 			try {
 		        String targetDate = this.getTargetDate();
 		        for (int i = 0; i < weathers.length(); i++) {
 		            JSONObject weather = weathers.getJSONObject(i);
 		            String date = weather.getString("date");
 		            Integer minC = Integer.parseInt(weather.getString("tempMinC"));
 		            
 		            // 対象日であったら判定
 		            Log.d("Loading date", date);
 		            Log.d("Target date", targetDate);
 		        	if(targetDate.equals(date)){
 			            
 		        		// アラートレベル
 		            	String alarmText, alarmTitle;
 		            
 		            	// TODO:isseium string.xml, 多言語対応
 		        		alarmText = "明日の" + location_labels[Integer.parseInt(locationId)] + "の最低気温は、" + Integer.toString(minC) + "度です。";
 		        		if(minC < -4){
 		        			alarmTitle = "水道管凍結対策が必要です。";
 		        			alarmText  = "【警報】" + alarmText;
 		        		}else if(minC < 0){
 		        			alarmTitle = "水道管凍結に注意してください。";
 		        			alarmText  = "【注意】" + alarmText;
 		        		}else{
 		        			alarmTitle = "水道管凍結の恐れはありません。";
 		        			alarmText  = "【安全】" + alarmText;
 		        		}
 		        		
 	        			Log.d("ALERM", alarmText);
 	        			
 	        			// タップ後の画面
 				        Intent nextIntent = new Intent(this.ctx, WeatherDetailActivity.class);
 				        // TODO:isseium 定数化
 				        nextIntent.putExtra("location_label", location_labels[Integer.parseInt(locationId)] );
 				        nextIntent.putExtra("minC", minC );
 				        
 	        			this.alarm(alarmTitle, alarmText + alarmTitle, nextIntent);
 		        	}
 		        }
 			}catch(Exception e){
 				Log.e("ERROR", e.getMessage());
 			}
 	    }
     }
     
     /**
      * 通知領域に表示する
      * @param title			
      * @param description
      */
 	public void alarm(String title, String description, Intent intent){
     	Log.d("freezeAlart", "Notification description=" + description);
         NotificationManager mNotificationManager = (NotificationManager) this.ctx.getSystemService(Context.NOTIFICATION_SERVICE);
         Notification notification = new Notification(R.drawable.ic_launcher, description,System.currentTimeMillis());
         PendingIntent contentIntent = PendingIntent.getActivity(this.ctx, 0, intent, 0);
         notification.setLatestEventInfo(
                 ctx.getApplicationContext(),
                 title,
                 description,
                 contentIntent);
 //        notification.flags = Notification.FLAG_ONGOING_EVENT;  // タップしても消さない
         notification.vibrate = new long[]{0, 200, 100, 200, 100, 200};
         mNotificationManager.notify(R.string.app_name, notification);
         
 	}
     
 	public String getTargetDate(){
         // 現在時刻取得
		long timeMillisEnd = System.currentTimeMillis() + 86400 * 1000; // 明日
		// TODO 現在時刻が午前0-3時のときは、当日の日付を返す
 		
 		// フォーマット定義
 		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
 		return sdf1.format(new Date(timeMillisEnd));
 	}
 	
 }
