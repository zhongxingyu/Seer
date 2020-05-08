 package jp.recruit.hps.movie.client;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.appspot.hps_movie.interviewEndpoint.InterviewEndpoint;
 import com.appspot.hps_movie.interviewEndpoint.InterviewEndpoint.InterviewV1EndPoint.UpdateInterviewQuestions;
 import com.appspot.hps_movie.interviewEndpoint.model.ResultV1Dto;
 import com.appspot.hps_movie.interviewEndpoint.model.StringListContainer;
 import com.appspot.hps_movie.questionEndpoint.QuestionEndpoint;
 import com.appspot.hps_movie.questionEndpoint.QuestionEndpoint.QuestionV1EndPoint.CreateQuestion;
 import com.appspot.hps_movie.questionEndpoint.model.QuestionResultV1Dto;
 import com.appspot.hps_movie.questionEndpoint.model.QuestionV1Dto;
 import com.appspot.hps_movie.questionEndpoint.model.QuestionV1DtoCollection;
 import com.google.analytics.tracking.android.EasyTracker;
 
 import jp.recruit.hps.movie.client.utils.QuestionAdapter;
 import jp.recruit.hps.movie.client.api.RemoteApi;
 import jp.recruit.hps.movie.client.utils.CommonUtils;
 import jp.recruit.hps.movie.common.CommonConstant;
 import android.animation.Animator;
 import android.animation.AnimatorListenerAdapter;
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.text.TextUtils;
 import android.util.Log;
 import android.util.SparseBooleanArray;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class RegisterInterviewActivity extends Activity {
 	private QuestionAdapter adapter = null;
 	String interviewKey;
 	String companyKey;
 	String userKey;
 	// 送信用
 	InterviewRegisterTask mAuthTask;
 	private static String SUCCESS = CommonConstant.SUCCESS;
 	private View mRegisterFormView;
 	private View mRegisterStatusView;
 	private TextView mRegisterStatusMessageView;
 
 	// データ保存
 	int mTime;
 	int mCategory;
 	int mAtmosphere;
 	StringListContainer questions;
 	Button btn;
 	ListView lv;
 	Dialog addQuestionDialog;
 
 	// UI
 	Spinner mCategorySpinner;
 	Spinner mTimeSpinner;
 	ImageView mEasyAtmosphere;
 	ImageView mNormalAtmosphere;
 	ImageView mHardAtmosphere;
 	ImageView mInterviewSendButton;
 	LinearLayout mQuestionListLayout;
 
 	// Dialog内のUI
 	EditText mAddEdit;
 
 	// 質問追加UI
 	EditText edit;
 	TextView nullQuestion;
 
 	List<String> questionKeyList;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO 自動生成されたコンストラクター・スタブ
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_register_interview);
 		Intent i = getIntent();
 		interviewKey = i.getStringExtra(CommonUtils.STRING_EXTRA_INTERVIEW_KEY);
 		companyKey = i.getStringExtra(CommonUtils.STRING_EXTRA_COMPANY_KEY);
 		String companyName = i
 				.getStringExtra(CommonUtils.STRING_EXTRA_COMPANY_NAME);
 		TextView name = (TextView) findViewById(R.id.register_interview_title_text);
 		name.setText(companyName);
 		
 		setLastPreference(companyName, companyName, companyName);
 		SharedPreferences pref = getSharedPreferences(
 				CommonUtils.STRING_PREF_KEY, Activity.MODE_PRIVATE);
 		userKey = pref.getString(CommonUtils.STRING_EXTRA_USER_KEY, null);
 		mRegisterFormView = findViewById(R.id.register_interview_form);
 		mRegisterStatusView = findViewById(R.id.register_interview_status);
 		mRegisterStatusMessageView = (TextView) findViewById(R.id.register_interview_status_message);
 		setButton();
 		new GetQuestionListAsyncTask(this).executeOnExecutor(
 				AsyncTask.THREAD_POOL_EXECUTOR, companyKey);
 	}
 	
 	private void setLastPreference(String companyKey,String interviewKey,String companyName){
 		SharedPreferences pref = getSharedPreferences(CommonUtils.STRING_PREF_KEY_REGISTERING_COMPANY,
 				Activity.MODE_PRIVATE);
 		SharedPreferences.Editor editor = pref.edit();
 		editor.putString(CommonUtils.STRING_PREF_COMPANY_KEY, companyKey);
 		editor.putString(CommonUtils.STRING_PREF_INTERVIEW_KEY, interviewKey);
 		editor.putString(CommonUtils.STRING_PREF_COMPANY_NAME, companyName);
 		editor.putBoolean(CommonUtils.STRING_PREF_REGISTERING, true);
 		editor.commit();
 	}
 
 	public void getCheckdList() {
 		if (adapter != null) {
 			questionKeyList = new ArrayList<String>();
 			SparseBooleanArray positions = lv.getCheckedItemPositions();
 			List<Integer> checked = new ArrayList<Integer>();
 
 			for (int i = 0; i < positions.size(); i++) {
 				if (positions.valueAt(i)) {
 					checked.add(positions.keyAt(i));
 				}
 			}
 			for (int i = 0; i < checked.size(); i++) {
 				QuestionV1Dto question = (QuestionV1Dto) adapter
 						.getItem(checked.get(i));
 				questionKeyList.add(question.getKey());
 			}
 		}
 	}
 
 	public void setButton() {
 
 		mQuestionListLayout = (LinearLayout) findViewById(R.id.register_question_list_layout);
 		mInterviewSendButton = (ImageView) findViewById(R.id.register_interview_send_button);
 		mInterviewSendButton.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// TODO 自動生成されたメソッド・スタブ
 				setInterview();
 			}
 		});
 		setSendButton();
 	}
 
 	public void setSendButton() {
 		btn = (Button) findViewById(R.id.add_list_btn);
 		btn.setVisibility(View.VISIBLE);
 		btn.setOnClickListener(new View.OnClickListener() {
 
 			public void onClick(View view) {
 
 				// コンテキストからインフレータを取得
 				LayoutInflater inflater = LayoutInflater
 						.from(RegisterInterviewActivity.this);
 				// レイアウトXMLからビュー(レイアウト)をインフレート
 				final View addQuestionDialogView = inflater.inflate(
 						R.layout.add_question_dialog, null);
 				mAddEdit = (EditText) addQuestionDialogView
 						.findViewById(R.id.add_question_edit_text);
 				addQuestionDialog = new AlertDialog.Builder(
 						RegisterInterviewActivity.this)
 						.setView(addQuestionDialogView)
 						.setTitle(R.string.question_add_title)
 						.setPositiveButton(R.string.question_add_positive,
 								new DialogInterface.OnClickListener() {
 
 									@Override
 									public void onClick(DialogInterface dialog,
 											int which) {
 										// TODO 自動生成されたメソッド・スタブ
 										if (TextUtils.isEmpty(mAddEdit
 												.getText().toString())) {
 											AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
 													RegisterInterviewActivity.this);
 											// アラートダイアログのタイトルを設定します
 											alertDialogBuilder
 													.setTitle("入力エラー");
 											// アラートダイアログのメッセージを設定します
 											alertDialogBuilder
 													.setMessage("質問を入力してください。");
 											// アラートダイアログの肯定ボタンがクリックされた時に呼び出されるコールバックリスナーを登録します
 											alertDialogBuilder
 													.setPositiveButton(
 															"OK",
 															new DialogInterface.OnClickListener() {
 																@Override
 																public void onClick(
 																		DialogInterface dialog,
 																		int which) {
 																}
 															});
 											// アラートダイアログのキャンセルが可能かどうかを設定します
 											alertDialogBuilder
 													.setCancelable(true);
 											AlertDialog alertDialog = alertDialogBuilder
 													.create();
 											alertDialog.show();
 										} else if (mAddEdit.getText()
 												.toString().length() > 15) {
 											AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
 													RegisterInterviewActivity.this);
 											// アラートダイアログのタイトルを設定します
 											alertDialogBuilder
 													.setTitle("入力エラー");
 											// アラートダイアログのメッセージを設定します
 											alertDialogBuilder
 													.setMessage("質問が15文字を超えています"
 															+ "\n"
 															+ "もう一度入力してください。");
 											// アラートダイアログの肯定ボタンがクリックされた時に呼び出されるコールバックリスナーを登録します
 											alertDialogBuilder
 													.setPositiveButton(
 															"OK",
 															new DialogInterface.OnClickListener() {
 																@Override
 																public void onClick(
 																		DialogInterface dialog,
 																		int which) {
 																}
 															});
 											// アラートダイアログのキャンセルが可能かどうかを設定します
 											alertDialogBuilder
 													.setCancelable(true);
 											AlertDialog alertDialog = alertDialogBuilder
 													.create();
 											alertDialog.show();
 										} else {
 											ProgressBar prog = (ProgressBar) findViewById(R.id.register_question_progressBar);
 											prog.setVisibility(View.VISIBLE);
 											new SetQuestionAsyncTask()
 													.executeOnExecutor(
 															AsyncTask.THREAD_POOL_EXECUTOR,
 															mAddEdit.getText()
 																	.toString());
 										}
 									}
 								})
 						.setNegativeButton(R.string.question_add_negative, null)
 						.show();
 			}
 		});
 
 	}
 
 	/*
 	 * 質問をリストで表示する 引数　userKey,interviewKey
 	 */
 	public class SetQuestionAsyncTask extends
 			AsyncTask<String, Integer, Boolean> {
 		QuestionV1Dto question;
 
 		@Override
 		protected Boolean doInBackground(String... params) {
 
 			// TODO 自動生成されたメソッド・スタブ
 			try {
 				QuestionEndpoint endpoint = RemoteApi.getQuestionEndpoint();
 				CreateQuestion createQuestion = endpoint.questionV1EndPoint()
 						.createQuestion(interviewKey, params[0]);
 				QuestionResultV1Dto result = createQuestion.execute();
 				if (SUCCESS.equals(result.getResult())) {
 					question = result.getQuestion();
 					return true;
 				} else {
 					return false;
 				}
 
 			} catch (Exception e) {
 				return false;
 			}
 
 		}
 
 		@Override
 		protected void onPostExecute(Boolean result) {
 			if (result) {
 
 				ProgressBar prog = (ProgressBar) findViewById(R.id.register_question_progressBar);
 				prog.setVisibility(View.GONE);
 				nullQuestion.setVisibility(View.GONE);
 				if (adapter != null) {
 					adapter.addList(question);
 					// アダプタに対してデータが変更したことを知らせる
 					adapter.notifyDataSetChanged();
 				} else {
 					List<QuestionV1Dto> list = new ArrayList<QuestionV1Dto>();
 					list.add(question);
 					adapter = new QuestionAdapter(
 							RegisterInterviewActivity.this, list);
 					mQuestionListLayout.setVisibility(View.VISIBLE);
 					lv = (ListView) findViewById(R.id.register_question_list);
 					lv.setVisibility(View.VISIBLE);
 					findViewById(R.id.register_add_list_layout).setVisibility(
 							View.VISIBLE);
 					lv.setAdapter(adapter);
 					// 単一選択モードにする
 					lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
 
 				}
 
 			}
 		}
 
 	}
 
 	public class GetQuestionListAsyncTask extends
 			AsyncTask<String, Integer, Boolean> {
 		private final Context context;
 		private List<QuestionV1Dto> list;
 
 		public GetQuestionListAsyncTask(Context context) {
 			this.context = context;
 		}
 
 		@Override
 		protected Boolean doInBackground(String... queries) {
 			String query = queries[0];
 			QuestionEndpoint endpoint = RemoteApi.getQuestionEndpoint();
 			try {
 				QuestionV1DtoCollection collection = endpoint
 						.questionV1EndPoint().getQuestions(query).execute();
 				if (collection != null && collection.getItems() != null) {
 					list = collection.getItems();
 				} else {
 					return false;
 				}
 			} catch (IOException e) {
 				e.printStackTrace();
 				return false;
 			}
 			return true;
 		}
 
 		@Override
 		protected void onPostExecute(Boolean result) {
 			nullQuestion = (TextView) findViewById(R.id.register_question_list_null_text);
 			findViewById(R.id.register_question_progress_layout).setVisibility(
 					View.GONE);
 			mQuestionListLayout.setVisibility(View.VISIBLE);
 
 			if (result) {
 				adapter = new QuestionAdapter(context, list);
 				findViewById(R.id.register_question_progress_layout)
 						.setVisibility(View.GONE);
 				mQuestionListLayout.setVisibility(View.VISIBLE);
 				lv = (ListView) findViewById(R.id.register_question_list);
 				lv.setVisibility(View.VISIBLE);
 				findViewById(R.id.register_add_list_layout).setVisibility(
 						View.VISIBLE);
 				lv.setAdapter(adapter);
 				// 単一選択モードにする
 				lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
 
 			} else {
 				nullQuestion.setVisibility(View.VISIBLE);
 			}
 		}
 	}
 
 	/**
 	 * Shows the progress UI and hides the register form.
 	 */
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
 	private void showProgress(final boolean show) {
 		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
 		// for very easy animations. If available, use these APIs to fade-in
 		// the progress spinner.
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
 			int shortAnimTime = getResources().getInteger(
 					android.R.integer.config_shortAnimTime);
 
 			mRegisterStatusView.setVisibility(View.VISIBLE);
 			mRegisterStatusView.animate().setDuration(shortAnimTime)
 					.alpha(show ? 1 : 0)
 					.setListener(new AnimatorListenerAdapter() {
 						@Override
 						public void onAnimationEnd(Animator animation) {
 							mRegisterStatusView
 									.setVisibility(show ? View.VISIBLE
 											: View.GONE);
 						}
 					});
 
 			mRegisterFormView.setVisibility(View.VISIBLE);
 			mRegisterFormView.animate().setDuration(shortAnimTime)
 					.alpha(show ? 0 : 1)
 					.setListener(new AnimatorListenerAdapter() {
 						@Override
 						public void onAnimationEnd(Animator animation) {
 							mRegisterFormView.setVisibility(show ? View.GONE
 									: View.VISIBLE);
 						}
 					});
 		} else {
 			// The ViewPropertyAnimator APIs are not available, so simply show
 			// and hide the relevant UI components.
 			mRegisterStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
 			mRegisterFormView.setVisibility(show ? View.GONE : View.VISIBLE);
 		}
 	}
 
 	@Override
 	public boolean dispatchKeyEvent(KeyEvent event) {
 		// TODO Auto-generated method stub
 		if (event.getAction() == KeyEvent.ACTION_DOWN) {
 			if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) { // BACKキー
 				return true;
 			}
 		}
 		return super.dispatchKeyEvent(event);
 	}
 
 	private void setInterview() {
 		if (mAuthTask != null) {
 			return;
 		}
 		getCheckdList();
 		// TODO 自動生成されたメソッド・スタブ
 		// Reset errors.
 
 		// Store values at the time of the login attempt.
 		// mName= mNameView.getText().toString();
 		boolean cancel = false;
 		if (adapter != null) {
 			if (questionKeyList.isEmpty()) {
 				cancel = true;
 				Toast.makeText(this,
 						R.string.register_interview_question_check_null,
 						Toast.LENGTH_SHORT).show();
 			} else {
 				questions = new StringListContainer();
 				questions.setList(questionKeyList);
 			}
 
 		} else {
 			cancel = true;
 		}
 
 		if (cancel) {
 			// There was an error; don't attempt register
 		} else {
 			// Show a progress spinner, and kick off a background task to
 			// perform the user register attempt.
 
 			/* 実験 */
 			mRegisterStatusMessageView
 					.setText(R.string.register_progress_signing_up);
 			showProgress(true);
 			mAuthTask = new InterviewRegisterTask();
 			mAuthTask.execute((Void) null);
 		}
 	}
 
 	/**
 	 * Represents an asynchronous registration task used to authenticate the
 	 * user.
 	 */
 	public class InterviewRegisterTask extends AsyncTask<Void, Void, Boolean> {
 		@Override
 		protected Boolean doInBackground(Void... args) {
 
 			try {
 				InterviewEndpoint endpoint = RemoteApi.getInterviewEndpoint();
 				UpdateInterviewQuestions register = endpoint
 						.interviewV1EndPoint().updateInterviewQuestions(
 								interviewKey, questions);
 				ResultV1Dto result = register.execute();
 
 				if (SUCCESS.equals(result.getResult())) {
 					return true;
 				} else {
 					return false;
 				}
 			} catch (Exception e) {
 
 				return false;
 			}
 		}
 
 		@Override
 		protected void onPostExecute(final Boolean success) {
 			mAuthTask = null;
 			showProgress(false);
 
 			if (success) {
 				Log.d("DEBUG", "register success");
 				SharedPreferences pref = getSharedPreferences(CommonUtils.STRING_PREF_KEY_REGISTERING_COMPANY,
 						Activity.MODE_PRIVATE);
 				SharedPreferences.Editor editor = pref.edit();
				editor.putBoolean(CommonUtils.STRING_PREF_REGISTERING_KEY, false);
 				editor.commit();
 				startActivity(new Intent(RegisterInterviewActivity.this,
 						TopActivity.class));
 				finish();
 			}
 		}
 
 		@Override
 		protected void onCancelled() {
 			mAuthTask = null;
 			showProgress(false);
 		}
 	}
 
 	@Override
 	protected void onStart() {
 		super.onStart();
 		EasyTracker.getInstance(this).activityStart(this);
 	}
 
 	@Override
 	protected void onStop() {
 		super.onStop();
 		EasyTracker.getInstance(this).activityStop(this);
 	}
 }
