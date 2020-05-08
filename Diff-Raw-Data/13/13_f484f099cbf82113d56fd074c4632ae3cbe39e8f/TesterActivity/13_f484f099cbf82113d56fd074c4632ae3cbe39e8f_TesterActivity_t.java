 package tw.edu.chu.csie.e_learning.ui;
 
 import java.io.IOException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.TimeZone;
 
 import org.apache.http.client.ClientProtocolException;
 import org.json.JSONException;
 
 import tw.edu.chu.csie.e_learning.R;
 import tw.edu.chu.csie.e_learning.R.layout;
 import tw.edu.chu.csie.e_learning.R.menu;
 import tw.edu.chu.csie.e_learning.provider.ClientDBProvider;
 import tw.edu.chu.csie.e_learning.server.exception.HttpException;
 import tw.edu.chu.csie.e_learning.server.exception.ServerException;
 import tw.edu.chu.csie.e_learning.ui.MaterialActivity.RequestToServer;
 import tw.edu.chu.csie.e_learning.util.FileUtils;
 import tw.edu.chu.csie.e_learning.util.LearningUtils;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.app.Activity;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ProgressBar;
 import android.widget.Toast;
 
 /**
  * 內部人員測試用的輔助頁面
  * @author yuan
  *
  */
 public class TesterActivity extends Activity implements OnClickListener {
 
 	private ProgressBar sendProgress;
 	private Button sql_clear_target;
 	private Button startTimer, connTimerBind, unconnTimerBind, getTimerMin, stopTimer;
 	private Button nowTime, startTime, learningTime, remainderTime, overTime;
 	private Button sendStopSendAll, sendAddPeople, sendSubPeople, sendSaveUserStatus;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_tester);
 		
 		// 設定ActionBar
 		getActionBar().setDisplayHomeAsUpEnabled(true);
 		
 		Button btn1 = (Button)findViewById(R.id.tester_btn1);
 		btn1.setText("取得學習點01教材路徑");
 		btn1.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				Toast.makeText(getBaseContext(), new FileUtils().getMaterialFilePath(getBaseContext(), 19), 0).show();
 			}
 		});
 		
 		Button btn2 = (Button)findViewById(R.id.tester_btn2);
 		btn2.setText("寫入標地1進SQLite");
 		btn2.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				ClientDBProvider db = new ClientDBProvider(getBaseContext());
 				db.target_insert(01, "Test!!!", "map_01_02_03.png", "01.html", 3,0);
 			}
 		});
 		
 		Button btn3 = (Button)findViewById(R.id.tester_btn3);
 		btn3.setText("清除標地SQLite");
 		btn3.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				ClientDBProvider db = new ClientDBProvider(getBaseContext());
 				db.delete(null, "chu_target");
 			}
 		});
 		
 		// ---------------------------------------------------------------------------------------------------------------------------------
 		sql_clear_target = (Button)findViewById(R.id.tester_sqlite_clear_target);
 		sql_clear_target.setOnClickListener(this);
 		
 		// ---------------------------------------------------------------------------------------------------------------------------------
 		nowTime = (Button)findViewById(R.id.tester_time_now);
 		nowTime.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				Date nowDate = new Date(System.currentTimeMillis());
 				Toast.makeText(TesterActivity.this, "Now: "+nowDate.getTime(), 0).show();
 				
 				//Toast.makeText(TesterActivity.this, "Now: "+nowDate.getHours()+":"+nowDate.getMinutes()+":"+nowDate.getSeconds(), 0).show();
 				
 				// 顯示時間
 				Calendar nowCalendar = Calendar.getInstance();
 				nowCalendar.setTime(nowDate);
 				Toast.makeText(TesterActivity.this, "Now: "+nowCalendar.get(Calendar.HOUR_OF_DAY)+":"+nowCalendar.get(Calendar.MINUTE)+":"+nowCalendar.get(Calendar.SECOND), 0).show();
 				
 			}
 		});
 		
 		startTime = (Button)findViewById(R.id.tester_time_start);
 		startTime.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				// 取得開始學習時間
 				ClientDBProvider dbcon = new ClientDBProvider(TesterActivity.this);
 				String[] query = dbcon.search("chu_user", "In_Learn_Time", null);
 				String startDateDB = null;
 				if(query.length>0) startDateDB = query[0];
 				//Toast.makeText(TesterActivity.this, "start String: "+startDateDB, 0).show();
 				
 				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 				Date startDate = new Date();
 				try {
 					startDate = format.parse(startDateDB);
 					
 					Toast.makeText(TesterActivity.this, "start: "+startDate.getTime(), 0).show();
 					//Toast.makeText(TesterActivity.this, "start: "+startDate.getHours()+":"+startDate.getMinutes()+":"+startDate.getSeconds(), 0).show();
 				} catch (ParseException e) {
 					e.printStackTrace();
 				}
 			}
 		});
 		
 		learningTime = (Button)findViewById(R.id.tester_time_learning);
 		learningTime.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				Date learningDate = new LearningUtils(TesterActivity.this).getLearningDate();
 				
 				Calendar learningCal = Calendar.getInstance();
 				learningCal.setTime(learningDate);
 				learningCal.setTimeZone(TimeZone.getTimeZone("UTC"));
 				
 				//Toast.makeText(TesterActivity.this, "Learning: "+learningDate.getTime(), 0).show();
 				Toast.makeText(TesterActivity.this, "Learning: "+learningCal.get(Calendar.HOUR_OF_DAY)+":"+learningCal.get(Calendar.MINUTE)+":"+learningCal.get(Calendar.SECOND), 0).show();
 			}
 		});
 		
 		remainderTime = (Button)findViewById(R.id.tester_time_remainder);
 		remainderTime.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				Calendar learningCal = Calendar.getInstance();
 				learningCal.setTime(new LearningUtils(TesterActivity.this).getRemainderLearningDate());
 				learningCal.setTimeZone(TimeZone.getTimeZone("UTC"));
 				
				Toast.makeText(TesterActivity.this, "Remainder: "+learningCal.get(Calendar.HOUR_OF_DAY)+":"+learningCal.get(Calendar.MINUTE)+":"+learningCal.get(Calendar.SECOND), 0).show();
				Toast.makeText(TesterActivity.this, "Remainder: "+new LearningUtils(TesterActivity.this).getRemainderLearningMinTime(), 0).show();
 				//Toast.makeText(TesterActivity.this, "Limit: "+new LearningUtils(TesterActivity.this).getRemainderLearningMinTime(), 0).show();
 			}
 		});
 		
 		overTime = (Button)findViewById(R.id.tester_time_over);
 		overTime.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				Toast.makeText(TesterActivity.this, "Over: "+new LearningUtils(TesterActivity.this).isLearningOver(), 0).show();
 			}
 		});
 		// ---------------------------------------------------------------------------------------------------------------------------------
 		//sendProgress = (ProgressBar)findViewById(R.id.tester_send_progress);
 		
 		
 		sendStopSendAll = (Button)findViewById(R.id.tester_send_stop_all_send);
 		sendStopSendAll.setOnClickListener(this);
 		
 		sendAddPeople = (Button)findViewById(R.id.tester_send_addpeople);
 		sendAddPeople.setOnClickListener(this);
 		
 		sendSubPeople = (Button)findViewById(R.id.tester_send_subpeople);
 		sendSubPeople.setOnClickListener(this);
 		
 		/*sendSaveUserStatus = (Button)findViewById(R.id.tester_send_save_user_status);
 		sendSaveUserStatus.setOnClickListener(this);*/
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.tester, menu);
 		return true;
 	}
 	@Override
 	public boolean onMenuItemSelected(int featureId, MenuItem item) {
 		switch (item.getItemId()) {
 	      case android.R.id.home:
 	          finish();
 	          return true;
 	      default:
 	          return super.onMenuItemSelected(featureId, item);
 	     }
 	}
 
 	@Override
 	public void onClick(View v) {
 		ClientDBProvider clientdb; 
 		RequestToServer request;
 		
 		switch(v.getId()) {
 		case R.id.tester_sqlite_clear_target:
 			clientdb = new ClientDBProvider(TesterActivity.this);
 			clientdb.delete(null, "chu_target");
 			break;
 		
 		case R.id.tester_send_stop_all_send:
 			Toast.makeText(this, "XDDD", 0).show();
 			break;
 		case R.id.tester_send_addpeople:
 			// 加人數
 			request = new RequestToServer();
 			request.execute("addPeople","12");
 			break;
 		case R.id.tester_send_subpeople:
 			// 減人數
 			request = new RequestToServer();
 			request.execute("subPeople","12");
 			break;
 		case R.id.tester_send_save_user_status:
 			Toast.makeText(this, "XDDD", 0).show();
 			break;
 		}
 	}
 	
 	// =================================================================================================================================
 	/**
 	 * 加人數、減人數
 	 */
 	public class RequestToServer extends AsyncTask<String, Void, Void>
 	{
 		private LearningUtils learn = new LearningUtils(TesterActivity.this);
 		@Override
 		protected Void doInBackground(String... params) 
 		{
 			try {
 				changeOfPerson(params[0], params[1]);
 			} catch (ClientProtocolException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			} catch (HttpException e) {
 				e.printStackTrace();
 			} catch (JSONException e) {
 				e.printStackTrace();
 			} catch (ServerException e) {
 				e.printStackTrace();
 			}
 			return null;
 		}
 		
 		private void changeOfPerson(String action,String point) throws ClientProtocolException, IOException, HttpException, JSONException, ServerException
 		{
 			if(action == "addPeople") learn.addPeople(point);
 			else if(action == "subPeople") learn.subPeople(point);
 			else Toast.makeText(getBaseContext(), "ERROR~!!", Toast.LENGTH_SHORT).show();
 		}
 		
 		@Override
 		protected void onPreExecute() {
 			super.onPreExecute();
 			Toast.makeText(TesterActivity.this, "傳送人數加減", 0).show();
 		}
 		
 		@Override
 		protected void onPostExecute(Void result) {
 			super.onPostExecute(result);
 			Toast.makeText(TesterActivity.this, "已傳送人數加減", 0).show();
 		}
 	}
 	
 }
