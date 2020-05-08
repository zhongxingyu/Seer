 package jag.kumamoto.apps.gotochi.stamprally;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.LinkedList;
 import java.util.Queue;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import jag.kumamoto.apps.gotochi.stamprally.Data.QuizChoices;
 import jag.kumamoto.apps.gotochi.stamprally.Data.QuizData;
 import jag.kumamoto.apps.gotochi.stamprally.Data.StampRallyURL;
 import jag.kumamoto.apps.gotochi.stamprally.Data.User;
 import jag.kumamoto.apps.gotochi.stamprally.Data.UserRecord;
 import aharisu.util.DataGetter;
 import aharisu.widget.ImageCheckBox;
 import aharisu.widget.ImageRadioButton;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Parcelable;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.Window;
 import android.webkit.WebChromeClient;
 import android.webkit.WebView;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.ImageView;
 import android.widget.RadioGroup;
 import android.widget.TextView;
 import android.widget.Toast;
 
 
 /**
  * 
  * クイズを表示するアクティビティ
  * 
  * @author aharisu
  *
  */
 public class QuizActivity extends Activity{
 	private static final int RequestLogin = 0;
 	
 	private static final class ResultData{
 		public final QuizData quizData;
 		public final boolean correctness;
 		public final long answeringTime;
 		public final boolean[] isCheckedAry;
 		
 		public ResultData(QuizData quizData, boolean correctness,
 				long answeringTime, boolean[] isCheckedAry) {
 			this.quizData = quizData;
 			this.correctness = correctness;
 			this.answeringTime = answeringTime;
 			this.isCheckedAry = isCheckedAry;
 		}
 	}
 	
 	private User mUser;
 	
 	private QuizData[] mQuizes;
 	private int mIndex = 0;
 	
 	private final Queue<String> mUntransmissionData = new LinkedList<String>();
 	private final ArrayList<ResultData> mResultDataAry = new ArrayList<ResultData>();
 	
 	
 	private long mStartTime; //ミリ秒
 	
 	@Override protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		Bundle extras = getIntent().getExtras();
 		if(extras == null) {
 			setResult(Activity.RESULT_CANCELED);
 			finish();
 			return;
 		}
 		
 		mQuizes = getQuizData(extras);
		if(mQuizes == null) {
 			setResult(Activity.RESULT_CANCELED);
 			finish();
 			return;
 		}
 		
 		mUser = extras.getParcelable(ConstantValue.ExtrasUser);
 		
 		
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.quiz);
 		
 		//クイズデータをもとにビューを構成する
 		constractQuizView();
 	}
 	
 	private QuizData[] getQuizData(Bundle extras) {
 		Parcelable[] ary = extras.getParcelableArray(ConstantValue.ExtrasQuizData);
 		if(ary == null) {
 			return null;
 		}
 		
 		QuizData[] quizes = new QuizData[ary.length];
 		for(int i = 0;i < ary.length;++i) {
 			quizes[i] = (QuizData)ary[i];
 		}
 		
 		Arrays.sort(quizes, new Comparator<QuizData>() {
 			public int compare(QuizData object1, QuizData object2) {
 				return object1.order - object2.order;
 			};
 		});
 		
 		return quizes;
 	}
 	
 	private void constractQuizView() {
 		//タイトル設定
 		((TextView)findViewById(R.id_quiz.name)).setText(mQuizes[mIndex].title);
 		
 		//問題文設定
 		WebView web = (WebView)findViewById(R.id_quiz.webview);
 		web.setWebChromeClient(new WebChromeClient() {
 			@Override public void onProgressChanged(WebView view, int newProgress) {
 				super.onProgressChanged(view, newProgress);
 				if(newProgress == 100) {
 					//ロードに時間がかかるときがあるのでスタートを時間を覚えなおす
 					mStartTime = System.currentTimeMillis();
 				}
 			}
 		});
 		web.loadData(mQuizes[mIndex].descriptionHTML,"text/html", "utf-8");
 			
 		//OKボタンの設定
 		final Button btnOK = (Button)findViewById(R.id_quiz.ok);
 		btnOK.setEnabled(false);
 		btnOK.setOnClickListener(createOKOnClickListener());
 		
 		//解答欄設定
 		constractChoices();
 		
 		
 		//解答時間を算出するためにスタートの時刻を覚えておく
 		mStartTime = System.currentTimeMillis();
 	}
 	
 	private View.OnClickListener createOKOnClickListener() {
 		return new View.OnClickListener() {
 			@Override public void onClick(View v) {
 				//OKボタンを押すまでにかかった時間(ミリ秒）
 				long duration = System.currentTimeMillis() - mStartTime;
 				if(duration < 1) {
 					duration = 1;
 				}
 				
 				RadioGroup choicesFrame = (RadioGroup)findViewById(R.id_quiz.frame_choices);
 				boolean[] isCheckedAry = new boolean[choicesFrame.getChildCount()];
 				for(int i = 0;i < isCheckedAry.length;++i) {
 					isCheckedAry[i] = ((CompoundButton)choicesFrame.getChildAt(i)).isChecked();
 				}
 				
 				//正解か否か
 				boolean correctness = isCorrectness(isCheckedAry);
 				
 				//ユーザ設定が行われていればロギングデータを送信する
 				//& ユーザ履歴を更新する
 				if(mUser != null) {
 					String loggingQuery = StampRallyURL.getLoggingQuery(
 							mUser, mQuizes[mIndex],
 							correctness, duration, isCheckedAry);
 					
 					synchronized (mUntransmissionData) {
 						mUntransmissionData.add(loggingQuery);
 					}
 					//非同期で送信
 					asyncSendAnswerLong();
 					
 					addUserRecord(correctness, duration);
 				} else if(StampRallyPreferences.getShowUrgeDialog()){
 					mResultDataAry.add(new ResultData(mQuizes[mIndex],
 							correctness, duration, isCheckedAry));
 				}
 				
 				showAnswerDialog(correctness);
 			}
 		};
 	}
 	
 	private boolean isCorrectness(boolean[] isCheckedAry) {
 		boolean correctness = true;
 		
 		QuizChoices choices = mQuizes[mIndex].choices;
 		
 		for(int i = 0;i < isCheckedAry.length;++i) {
 			if(isCheckedAry[i] != choices.getChoice(i).isCorrectAnswer) {
 				correctness = false;
 				break;
 			}
 		}
 		
 		return correctness;
 	}
 	
 	private void addUserRecord(boolean correctness, long duration) {
 		long id = mQuizes[mIndex].id;
 		int point = 0;
 		if(!StampRallyDB.checkQuizCorrectness(id)) {
 			StampRallyDB.setQuizResult(id);
 			
 			point = mQuizes[mIndex].point;
 		}
 		
 		UserRecord record = StampRallyPreferences.getUserRecord();
 		record.point += point;
 		record.numCorrectness += correctness ? 1 : 0;
 		record.numTotalAnswerdQuize += 1;
 		record.totalAnswerTime += duration;
 		StampRallyPreferences.setUserRecord(record);
 	}
 	
 	private void asyncSendAnswerLong() {
 		int count;
 		synchronized(mUntransmissionData) {
 			count = mUntransmissionData.size();
 		}
 		
 		int c = 0;
 		while(c < count) {
 			++c;
 			
 			String query; 
 			synchronized(mUntransmissionData) {
 				query = mUntransmissionData.poll();
 			}
 			
 			if(query == null){ 
 				break;
 			}
 			
 			new AsyncTask<String, Void, Boolean>() {
 				private String mQuery;
 				
 				@Override protected Boolean doInBackground(String... params) {
 					mQuery = params[0];
 					try {
 						JSONObject obj = DataGetter.getJSONObject(params[0]);
 						if(StampRallyURL.isSuccess(obj)) {
 							return true;
 						} else {
 							//XXX サーバとの通信失敗(クエリの間違い?)
 							Log.e("send answer", obj.toString());
 						}
 					} catch (IOException e) {
 						//XXX ネットワーク通信の失敗
 						e.printStackTrace();
 					} catch (JSONException e) {
 						//XXX JSONフォーマットが不正
 						e.printStackTrace();
 					}
 					
 					return false;
 				}
 				
 				@Override protected void onPostExecute(Boolean result) {
 					if(!result) {
 						//XXX 送信失敗.どうしよう
 						Log.i("send", "failure");
 						synchronized (mUntransmissionData) {
 							mUntransmissionData.offer(mQuery);
 						}
 					} else {
 						Log.i("send", "success");
 					}
 				}
 				
 			}.execute(query);
 		}
 		
 	}
 	
  	private void showAnswerDialog(boolean correctness) {
  		View content = ((LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE))
  			.inflate(R.layout.quiz_result_dialog_content, null);
  		
  		
 		AlertDialog.Builder builder =  new AlertDialog.Builder(this)
 			.setTitle(R.string.quiz_question_result)
 			.setView(content)
 			.setPositiveButton(R.string.common_return, new DialogInterface.OnClickListener() {
 				@Override public void onClick(DialogInterface dialog, int which) {
 					finish();
 				}
 			})
 			.setOnCancelListener(new DialogInterface.OnCancelListener() {
 				
 				@Override public void onCancel(DialogInterface dialog) {
 					finish();
 				}
 			});
 		
 		if(correctness) {
 	 		((TextView)content.findViewById(R.id_quiz.result_message)).setText(R.string.quiz_result_correctness);
 	 		((ImageView)content.findViewById(R.id_quiz.result_icon)).setImageResource(R.drawable.quiz_result_correctness);
 	 		
 			if(mUser == null && StampRallyPreferences.getShowUrgeDialog()) {
 		 		content.findViewById(R.id_quiz.result_separator).setVisibility(View.VISIBLE);
 		 		content.findViewById(R.id_quiz.result_warning).setVisibility(View.VISIBLE);
 		 		content.findViewById(R.id_quiz.not_show_next_time).setVisibility(View.VISIBLE);
 		 		
 				builder.setNeutralButton(R.string.common_login, new DialogInterface.OnClickListener() {
 					@Override public void onClick(DialogInterface dialog, int which) {
 						Intent intent = new Intent(QuizActivity.this, SettingsActivity.class);
 						intent.putExtra(ConstantValue.ExtrasLoginRequest, true);
 						
 						startActivityForResult(intent, RequestLogin);
 					}
 				});
 				
 				((CheckBox)content.findViewById(R.id_quiz.not_show_next_time)).setChecked(!StampRallyPreferences.getShowUrgeDialog());
 		 		((CheckBox)content.findViewById(R.id_quiz.not_show_next_time)).setOnCheckedChangeListener(
 		 				new CompoundButton.OnCheckedChangeListener() {
 							@Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 								StampRallyPreferences.setShowUrgeDialog(!isChecked);
 								if(isChecked) {
 									Toast.makeText(QuizActivity.this, 
 											R.string.common_can_modify_option_on_settings, Toast.LENGTH_LONG).show();
 								}
 							}
 						});
 				
 			} else {
 				content.findViewById(R.id_quiz.result_separator).setVisibility(View.GONE);
 			}
 			
 			if(mIndex + 1 < mQuizes.length) {
 				builder.setNegativeButton(R.string.quiz_go_next_quiz, new DialogInterface.OnClickListener() {
 					@Override public void onClick(DialogInterface dialog, int which) {
 						++mIndex;
 						constractQuizView();
 					}
 				});
 			}
 		} else {
 	 		((TextView)content.findViewById(R.id_quiz.result_message)).setText(R.string.quiz_result_incorrectness);
 	 		((ImageView)content.findViewById(R.id_quiz.result_icon)).setImageResource(R.drawable.quiz_result_incorrectness);
 		}
 		
 		
 		builder.show();
 	}
 	
 	private void constractChoices() {
 		final Button btnOK = (Button)findViewById(R.id_quiz.ok);
 		
 		
 		final RadioGroup choicesFrame = (RadioGroup)findViewById(R.id_quiz.frame_choices);
 		choicesFrame.removeAllViews();
 			
 		
 		//単一選択か複数選択か
 		int layoutId = mQuizes[mIndex].choices.isChoiseSingle() ? 
 				R.layout.quiz_single_choice :
 				R.layout.quiz_muliple_choice;
 		
 		
 		int count = mQuizes[mIndex].choices.getCount();
 		LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		for(int i = 0;i < count;++i) {
 			QuizChoices.Choice choice = mQuizes[mIndex].choices.getChoice(i);
 			
 			CompoundButton btn = (CompoundButton)inflater.inflate(layoutId, null);
 			
 			btn.setId(i);
 			btn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
 				@Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 					if(isChecked) {
 						btnOK.setEnabled(true);
 					} else {
 						int count = choicesFrame.getChildCount();
 						boolean isEnabled = false;
 						for(int i = 0;i < count;++i) {
 							if(((CompoundButton)choicesFrame.getChildAt(i)).isChecked()) {
 								isEnabled = true;
 								break;
 							}
 						}
 						
 						btnOK.setEnabled(isEnabled);
 					}
 				}
 			});
 			
 			if(choice.type == QuizChoices.Choice.TextTypeRawText) {
 				btn.setText(choice.altTypeText);
 			} else {
 				if(btn instanceof ImageRadioButton) {
 					((ImageRadioButton)btn).setImageURL(choice.altTypeText);
 				} else if(btn instanceof ImageCheckBox) {
 					((ImageCheckBox)btn).setImageURL(choice.altTypeText);
 				}
 			}
 			
 			choicesFrame.addView(btn);
 		}
 	}
 
 	@Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if(requestCode == RequestLogin) {
 			if(resultCode == Activity.RESULT_OK) {
 				User user = data.getExtras().getParcelable(ConstantValue.ExtrasUser);
 				if(user != null) {
 					mUser = user;
 					
 					for(ResultData result : mResultDataAry) {
 						addUserRecord(result.correctness, result.answeringTime);
 					}
 					
 					synchronized (mUntransmissionData) {
 						for(ResultData result : mResultDataAry) {
 							mUntransmissionData.offer(StampRallyURL.getLoggingQuery(mUser, 
 									result.quizData, result.correctness, result.answeringTime, result.isCheckedAry));
 						}
 					}
 					
 					//ログインしたので未送信データを送信する
 					asyncSendAnswerLong();
 					
 					//もう一度ダイアログを表示する
 					showAnswerDialog(true);
 				}
 			}
 			
 			return;
 		}
 		
 		super.onActivityResult(requestCode, resultCode, data);
 	}
 	
 }
