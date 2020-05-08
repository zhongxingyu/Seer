 package jag.kumamoto.apps.StampRally;
 
 import java.io.IOException;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import jag.kumamoto.apps.StampRally.Data.QuizData;
 import jag.kumamoto.apps.StampRally.Data.StampPin;
 import jag.kumamoto.apps.StampRally.Data.User;
 import jag.kumamoto.apps.gotochi.R;
 import aharisu.util.DataGetter;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.Window;
 import android.webkit.WebView;
 import android.widget.Button;
 
 
 /**
  * 
  * スタンプラリーのチェックポイントの情報を表示するアクティビティ
  * 
  * @author aharisu
  *
  */
 public class LocationInfoActivity extends Activity{
 	
 	private User mUser;
 	
 	private QuizData[] mQuizes;
 	
 	@Override protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 				
 		Bundle extras = getIntent().getExtras();
 		if(extras == null) {
 			setResult(Activity.RESULT_CANCELED);
 			finish();
 			return;
 		}
 		
 		StampPin pin = (StampPin)extras.getParcelable(ConstantValue.ExtrasStampPin);
 		boolean isShowGoQuiz = extras.getBoolean(ConstantValue.ExtrasShowGoQuiz, false);
 		
 		if(pin == null) {
 			setResult(Activity.RESULT_CANCELED);
 			finish();
 			return;
 		}
 		
 		mUser = extras.getParcelable(ConstantValue.ExtrasUser);
 		
 		
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.location_infomation);
 		
 		try {
 			((WebView)findViewById(R.id_location_info.webview)) .loadData(
 					DataGetter.getHTML(this, R.raw.test_location_info),
 					"text/html",
 					"utf-8");
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 				
 		
 		View goQuizFrame = findViewById(R.id_location_info.go_quiz_frame);
 		if(isShowGoQuiz) {
 			goQuizFrame.setVisibility(View.VISIBLE);
 			findViewById(R.id_location_info.progress_frame).setVisibility(View.VISIBLE);
 			
 			Button goQuiz = (Button)findViewById(R.id_location_info.go_quiz);
 			goQuiz.setEnabled(false);
 			goQuiz.setText(null);
 			goQuiz.setOnClickListener(createGoQuizOnClickListener());
 			
			getAsyncQuizDataFromServer(pin.id);
 		} else {
 			goQuizFrame.setVisibility(View.GONE);
 		}
 		
 	}
 	
 	private View.OnClickListener createGoQuizOnClickListener() {
 		return new View.OnClickListener() {
 			@Override public void onClick(View v) {
 				if(mQuizes == null) {
 					//TORO 何かがおかしいぞと表示
 					return;
 				}
 				
 				Intent intent = new Intent(LocationInfoActivity.this, QuizActivity.class);
 				intent.putExtra(ConstantValue.ExtrasQuizData, mQuizes);
 				
 				if(mUser != null) {
 					intent.putExtra(ConstantValue.ExtrasUser, mUser);
 				}
 				
 				startActivity(intent);
 			}
 		};
 	}
 
 	private void getAsyncQuizDataFromServer(final long id) {
 		new AsyncTask<Void, Void, QuizData[]>() {
 			
 			@Override protected QuizData[] doInBackground(Void... params) {
 				
 				JSONObject obj = DataGetter.getJSONObject(QuizData.getQueryURL(id));
 				
 				QuizData[] quizes = null;
 				if(obj != null) {
 					try {
 						quizes = QuizData.decodeJSONObject(obj);
 					} catch(JSONException e) {
 						e.printStackTrace();
 					}
 				}
 				
 				return quizes;
 			}
 			
 			@Override protected void onPostExecute(QuizData[] result) {
 				super.onPostExecute(result);
 				
 				setgettedQuizData(result);
 			}
 			
 		}.execute((Void)null);
 	}
 	
 	private void setgettedQuizData(QuizData[] quizes) {
 		mQuizes = quizes;
 		
 		findViewById(R.id_location_info.progress_frame).setVisibility(View.GONE);
 		
 		if(mQuizes == null) {
 			//TODO クイズデータの取得に失敗した
 			//エラー表示
 			Log.e("quizData" , "get falure");
 			return;
 		}
 		
 		Button goQuiz = (Button)findViewById(R.id_location_info.go_quiz);
 		goQuiz.setEnabled(true);
 		goQuiz.setText("クイズへGo!!");
 	}
 	
 }
