 package jag.kumamoto.apps.StampRally;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import aharisu.util.DataGetter;
 import aharisu.util.Pair;
 import android.accounts.Account;
 import android.accounts.AccountManager;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.graphics.drawable.Drawable;
 import android.os.AsyncTask;
 import android.os.Handler;
 import android.text.Editable;
 import android.text.Spannable;
 import android.text.TextWatcher;
 import android.text.style.UnderlineSpan;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.TextView;
 import android.widget.Toast;
 import jag.kumamoto.apps.StampRally.Data.StampPin;
 import jag.kumamoto.apps.StampRally.Data.StampRallyURL;
 import jag.kumamoto.apps.StampRally.Data.User;
 import jag.kumamoto.apps.StampRally.Data.UserHistory;
 import jag.kumamoto.apps.StampRally.Data.UserRecord;
 import jag.kumamoto.apps.gotochi.R;
 
 final class UserSettingsHelper {
 	public static interface OnLoginLogoutListener {
 		public void onLogin(User user);
 		public void onLogout();
 		public void gotoAccountSettngs();
 	}
 	
 	private final ViewGroup mLayout;
 	private final OnLoginLogoutListener mListener;
 	private User mUser;
 	
 	public UserSettingsHelper(ViewGroup layout, User user, OnLoginLogoutListener listner) {
 		mLayout = layout;
 		mUser = user;
 		mListener = listner;
 		
 		if(mUser == null) {
 			constractLoginView();
 		} else {
 			constractUserInfoModifyView();
 		}
 	}
 	
 	public void updateView() {
 		EditText edt = (EditText)mLayout.findViewById(R.id_settings.nickname);
 		TextWatcher watcher = (TextWatcher)edt.getTag();
 		if(watcher != null) {
 			edt.removeTextChangedListener(watcher);
 		}
 		
 		if(mUser != null) {
 			constractUserInfoModifyView();
 		} else {
 			if(mLayout.findViewById(R.id_settings.registration_frame).getVisibility() == View.GONE) {
 				constractLoginView();
 			} else {
 				constractRegistrationView();
 			}
 		}
 	}
 	
 	/*
 	 * 
 	 * ユーザ情報修正ビュー
 	 * 
 	 */
 	
 	private void constractUserInfoModifyView() {
 		//タイトル設定
 		((TextView)mLayout.findViewById(R.id_settings.user_setting_title)).setText("ユーザー情報編集");
 		
 		//トークンリストを非表示
 		mLayout.findViewById(R.id_settings.select_token_frame).setVisibility(View.GONE);
 		
 		//ニックネームと性別設定欄を表示
 		mLayout.findViewById(R.id_settings.registration_frame).setVisibility(View.VISIBLE);
 		
 		//ニックネーム設定
 		EditText edtNickname = (EditText)mLayout.findViewById(R.id_settings.nickname);
 		edtNickname.setText(mUser.nickname);
 		edtNickname.setSelection(mUser.nickname.length());
 		TextWatcher tw = new TextWatcher() {
 			@Override public void onTextChanged(CharSequence s, int start, int before, int count) {
 				if(s.length() == 0) {
 					//長さ０は無条件で無効
 					((Button)mLayout.findViewById(R.id_settings.ok)).setEnabled(false);
 				} else {
 					checkModifyEnable();
 				}
 			}
 			
 			@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {
 			}
 			
 			@Override public void afterTextChanged(Editable s) {
 			}
 		};
 		edtNickname.addTextChangedListener(tw);
 		edtNickname.setTag(tw);
 		
 		
 		//性別設定
 		RadioGroup genderGroup =  (RadioGroup)mLayout.findViewById(R.id_settings.gender_frame);
 		genderGroup.check(genderToId(mUser.gender));
 		genderGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
 			@Override public void onCheckedChanged(RadioGroup group, int checkedId) {
 				checkModifyEnable();
 			}
 		});
 		
 		//okボタンを設定
 		Button btnModify =  (Button)mLayout.findViewById(R.id_settings.ok);
 		btnModify.setText("変更");
 		btnModify.setEnabled(false);
 		btnModify.setOnClickListener(new View.OnClickListener() {
 			@Override public void onClick(View v) {
 				int gender = idToGender(((RadioGroup)mLayout.findViewById(R.id_settings.gender_frame)).getCheckedRadioButtonId());
 				String nickname = ((EditText)mLayout.findViewById(R.id_settings.nickname)).getText().toString();
 				
 				if(mUser.gender != gender || !mUser.nickname.equals(nickname)) {
 					modifyUser(new User(mUser.token, gender, nickname));
 				}
 			}
 		});
 		
 		//ログアウトするテキストを設定
 		TextView tvwLogout = (TextView)mLayout.findViewById(R.id_settings.change_view);
 		setUnderlineText(tvwLogout, "ログアウトする");
 		//テキストをタッチしたときに背景色を変える
 		tvwLogout.setOnTouchListener(createChangeViewOnTouchListener());
 		//テキストをタッチしたときに画面を変更する
 		tvwLogout.setOnClickListener(createChangeViewOnClickListener());
 	}
 	
 	private int genderToId(int gender) {
 		return gender == User.Female ? R.id_settings.gender_female :
 				gender == User.Male ? R.id_settings.gender_male :
 				R.id_settings.gender_unknown;
 	}
 	
 	private int idToGender(int id) {
 		return id == R.id_settings.gender_female ? User.Female :
 			id == R.id_settings.gender_male ? User.Male :
 			User.Unknown;
 	}
 	
 	private void setUnderlineText(TextView tvw, String text) {
 		//テキストに下線を引く
 		Spannable span = Spannable.Factory.getInstance().newSpannable(text);
 		UnderlineSpan us = new UnderlineSpan();
 		span.setSpan(us, 0, text.length(), span.getSpanFlags(us));
 		tvw.setText(span, TextView.BufferType.SPANNABLE);
 	}
 	
 	private void checkModifyEnable() {
 		//性別とニックネームどちらかがもとと違えば有効にする	
 		((Button)mLayout.findViewById(R.id_settings.ok)).setEnabled(
 			genderToId(mUser.gender) != ((RadioGroup)mLayout.findViewById(R.id_settings.gender_frame)).getCheckedRadioButtonId() ||
 			 !mUser.nickname.equals(((EditText)mLayout.findViewById(R.id_settings.nickname)).getText().toString()));
 	}
 	
 	private View.OnTouchListener createChangeViewOnTouchListener() {
 		return new View.OnTouchListener() {
 				private Drawable mBackground;
 				
 				@Override public boolean onTouch(View v, MotionEvent event) {
 					switch(event.getAction()) {
 					case MotionEvent.ACTION_DOWN:
 						mBackground = v.getBackground();
 						v.setBackgroundColor(0xffadfec4);
 						break;
 					case MotionEvent.ACTION_UP:
 					case MotionEvent.ACTION_CANCEL:
 						v.setBackgroundDrawable(mBackground);
 						break;
 					}
 				return false;
 			}
 		};
 	}
 	
 	private View.OnClickListener createChangeViewOnClickListener() {
 		return new View.OnClickListener() {
 			@Override public void onClick(View v) {
 				if(mUser != null) {
 					showLogoutAlertDialog();
 				} else {
 					if(mLayout.findViewById(R.id_settings.registration_frame).getVisibility() == View.GONE) {
 						constractRegistrationView();
 					} else {
 						EditText edt = (EditText)mLayout.findViewById(R.id_settings.nickname);
 						edt.removeTextChangedListener((TextWatcher)edt.getTag());
 						
 						constractLoginView();
 					}
 				}
 			}
 		};
 	}
 	
 	private void showLogoutAlertDialog() {
 		new AlertDialog.Builder(mLayout.getContext())
 			.setTitle("本当に？")
 			.setMessage("ログアウトします。\nよろしいですか?")
 			.setPositiveButton("OK", new DialogInterface.OnClickListener() {
 				
 				@Override public void onClick(DialogInterface dialog, int which) {
 					logout();
 				}
 			})
 			.setNegativeButton("キャンセル", null)
 			.setCancelable(true)
 			.show();
 	}
 	
 	private void logout() {
 		((RadioGroup)mLayout.findViewById(R.id_settings.gender_frame))
 			.setOnCheckedChangeListener(null);
 		EditText edt = (EditText)mLayout.findViewById(R.id_settings.nickname);
 		edt.removeTextChangedListener((TextWatcher)edt.getTag());
 	
 		mUser = null;
 		//ユーザ情報を消去する
 		StampRallyPreferences.clearUser();
 		StampRallyPreferences.clearUserRecord();
 		StampRallyDB.clearPinArrive();
 		StampRallyDB.clearPrizes();
 		StampRallyDB.clearQuizResult();
 		
 		if(mListener != null) {
 			mListener.onLogout();
 		}
 					
 		constractLoginView();
 	}
 	
 	private void modifyUser(final User user) {
 		final ProgressDialog dialog = new ProgressDialog(mLayout.getContext());
 		dialog.setMessage("変更中です");
 		dialog.setIndeterminate(false);
 		dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
 		dialog.setCancelable(false);
 		dialog.show();
 
 		new AsyncTask<Void, Void, Boolean>() {
 
 			@Override protected Boolean doInBackground(Void... params) {
 				try {
 					JSONObject obj = DataGetter.getJSONObject(StampRallyURL.getRegistrationQuery(user));
 					if(StampRallyURL.isSuccess(obj)) {
 						return true;
 					} else {
 						//XXX サーバとの通信失敗(クエリの間違い?)
 						Log.e("modify user", obj.toString());
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
 				dialog.dismiss();
 
 				if(result) {
 					StampRallyPreferences.setUser(user);
 					mUser = user;
 
 					Toast.makeText(mLayout.getContext(), "変更しました", Toast.LENGTH_SHORT).show();
 				} else {
 					Toast.makeText(mLayout.getContext(), "変更に失敗しました", Toast.LENGTH_SHORT).show();
 				}
 			}
 
 		}.execute((Void)null);
 	}
 	
 	/*
 	 * 
 	 * ログインビュー
 	 * 
 	 */
 	
 	private void constractLoginView() {
 		//タイトル設定
 		((TextView)mLayout.findViewById(R.id_settings.user_setting_title)).setText("ログイン");
 		
 		//ニックネームと性別設定欄を非表示
 		mLayout.findViewById(R.id_settings.registration_frame).setVisibility(View.GONE);
 		
 		//okボタンを設定
 		Button btnLogin =  (Button)mLayout.findViewById(R.id_settings.ok);
 		btnLogin.setText("ログイン");
 		btnLogin.setOnClickListener(new View.OnClickListener() {
 			@Override public void onClick(View v) {
 				login(getSelectedToken());
 			}
 		});
 		
 		//新規登録するテキストを設定
 		TextView tvwGotoRegistration = (TextView)mLayout.findViewById(R.id_settings.change_view);
 		setUnderlineText(tvwGotoRegistration, "新規登録はこちら");
 		//テキストをタッチしたときに背景色を変える
 		tvwGotoRegistration.setOnTouchListener(createChangeViewOnTouchListener());
 		//テキストをタッチしたときに画面を変更する
 		tvwGotoRegistration.setOnClickListener(createChangeViewOnClickListener());
 		
 		//トークンリストを表示
 		mLayout.findViewById(R.id_settings.select_token_frame).setVisibility(View.VISIBLE);
 		
 		//トークンリストを設定&初期化
 		((TextView)mLayout.findViewById(R.id_settings.token_label)).setText("登録したアドレスを選択してください");
 		if(!initTokenFrame()) {
 			btnLogin.setEnabled(false);
 			//アカウントが一つもなかった
 			showNoneAccountDialog();
 		} else {
 			btnLogin.setEnabled(true);
 		}
 	}
 	
 	private void login(final String token) {
 		final ProgressDialog dialog = new ProgressDialog(mLayout.getContext());
 		dialog.setMessage("認証中です");
 		dialog.setMax(5);
 		dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
 		dialog.setCancelable(false);
 		dialog.show();
 
 		final Handler handler = new Handler();
 		new AsyncTask<Void, Void, User>() {
 
 			@Override protected User doInBackground(Void... params) {
 				try {
 					JSONObject obj = DataGetter.getJSONObject(StampRallyURL.getUserInfoQuery(token));
 					if(StampRallyURL.isSuccess(obj)) {
 						User user = User.decodeJSONObject(token, obj);
 						int point = UserHistory.decodeJSONGotochiData(obj);
 						switch(loginUpdate(dialog, handler, user, new UserRecord(point, 0, 0, 0, 0))) {
 						case 1:
 							//XXX サーバとの通信失敗(クエリの間違い?)
 							break;
 						case 2:
 							//XXX ネットワーク通信の失敗
 							break;
 						case 3:
 							//XXX JSONフォーマットが不正
 							break;
 						default:
 							return user;
 						}
 								
 						return null;
 					} else {
 						//XXX サーバとの通信失敗(クエリの間違い?)
 						Log.e("login", obj.toString());
 					}
 				} catch (IOException e) {
 					//XXX ネットワーク通信の失敗
 					e.printStackTrace();
 				} catch (JSONException e) {
 					//XXX JSONフォーマットが不正
 					e.printStackTrace();
 				}
 				
 				return null;
 			}
 
 			@Override protected void onPostExecute(User result) {
 				dialog.dismiss();
 
 				if(result != null) {
 					StampRallyPreferences.setUser(result);
 					mUser = result;
 					
 					if(mListener != null) {
 						mListener.onLogin(mUser);
 					}
 					constractUserInfoModifyView();
 
 					Toast.makeText(mLayout.getContext(), "ログインしました", Toast.LENGTH_SHORT).show();
 				} else {
 					Toast.makeText(mLayout.getContext(), "ログインに失敗しました", Toast.LENGTH_SHORT).show();
 				}
 			}
 
 		}.execute((Void)null);
 	}
 
 	
 	/*
 	 * 
 	 * 新規登録ビュー
 	 * 
 	 */
 	
 	private void constractRegistrationView() {
 		//タイトル設定
 		((TextView)mLayout.findViewById(R.id_settings.user_setting_title)).setText("新規登録");
 		
 		//ニックネームと性別設定欄を表示
 		mLayout.findViewById(R.id_settings.registration_frame).setVisibility(View.VISIBLE);
 		
 		//性別設定
 		((RadioGroup)mLayout.findViewById(R.id_settings.gender_frame)).check(genderToId(User.Unknown));
 		
 		//ニックネーム設定
 		EditText edtNickname = (EditText)mLayout.findViewById(R.id_settings.nickname);
 		edtNickname.setText(null);
 		TextWatcher tw = createEditTextWatcher();
 		edtNickname.addTextChangedListener(tw);
 		edtNickname.setTag(tw);
 		
 		//okボタンを設定
 		Button btnOK = (Button)mLayout.findViewById(R.id_settings.ok);
 		btnOK.setText("新規登録");
 		btnOK.setEnabled(false);
 		btnOK.setOnClickListener(createRegistrationOnClickListener());
 		
 		//ログインするテキストを設定
 		TextView tvwGotoLogin = (TextView)mLayout.findViewById(R.id_settings.change_view);
 		setUnderlineText(tvwGotoLogin, "ログインはこちら");
 		//テキストをタッチしたときに背景色を変える
 		tvwGotoLogin.setOnTouchListener(createChangeViewOnTouchListener());
 		//テキストをタッチしたときに画面を変更する
 		tvwGotoLogin.setOnClickListener(createChangeViewOnClickListener());
 		
 		//トークンリストを表示
 		mLayout.findViewById(R.id_settings.select_token_frame).setVisibility(View.VISIBLE);
 		
 		//トークンリストを設定&初期化
 		((TextView)mLayout.findViewById(R.id_settings.token_label)).setText("登録するアドレスを選択してください");
 		if(!initTokenFrame()) {
 			//アカウントが一つもなかった
 			showNoneAccountDialog();
 		}
 	}
 
 	private TextWatcher createEditTextWatcher() {
 		return new TextWatcher() {
 			@Override public void onTextChanged(CharSequence s, int start, int before, int count) {
 				EditText edtNickname = (EditText)mLayout.findViewById(R.id_settings.nickname);
 
 				//入力欄が空白でなければOKボタンを有効にする
 				boolean enabled = edtNickname.getText().length() != 0;
 				mLayout.findViewById(R.id_settings.ok).setEnabled(enabled);
 			}
 
 			@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {
 			}
 
 			@Override public void afterTextChanged(Editable s) {
 			}
 		};
 	}
 	
 	private View.OnClickListener createRegistrationOnClickListener() {
 		return new View.OnClickListener() {
 			@Override public void onClick(View v) {
 				String token = getSelectedToken();
 				int gender = idToGender(((RadioGroup)mLayout.findViewById(R.id_settings.gender_frame)).getCheckedRadioButtonId());
 				String nickname = ((EditText)mLayout.findViewById(R.id_settings.nickname)).getText().toString();
 				
 				registration(new User(token, gender, nickname));
 			}
 		};
 	}
 	
 	private void registration(final User user) {
 		final ProgressDialog dialog = new ProgressDialog(mLayout.getContext());
 		dialog.setMessage("登録中です");
 		dialog.setMax(5);
 		dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
 		dialog.setCancelable(false);
 		dialog.show();
 
 		final Handler handler = new Handler();
 		new AsyncTask<Void, Void, Boolean>() {
 
 			@Override protected Boolean doInBackground(Void... params) {
 				try {
 					JSONObject obj = DataGetter.getJSONObject(StampRallyURL.getRegistrationQuery(user));
 					if(StampRallyURL.isSuccess(obj)) {
 						if(User.isUpdate(obj)) {
							switch(loginUpdate(dialog, handler, user, new UserRecord(0, 0, 0, 0, 0))) {
							case 1:
								//XXX サーバとの通信失敗(クエリの間違い?)
								return false;
							case 2:
								//XXX ネットワーク通信の失敗
								return false;
							case 3:
								//XXX JSONフォーマットが不正
								return false;
 							}
 						}
 						
 						return true;
 					} else {
 						//XXX サーバとの通信失敗(クエリの間違い?)
 						Log.e("registration", obj.toString());
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
 				dialog.dismiss();
 
 				if(result) {
 					StampRallyPreferences.setUser(user);
 					mUser = user;
 
 					if(mListener != null) {
 						mListener.onLogin(user);
 					}
 					constractUserInfoModifyView();
 					
 					Toast.makeText(mLayout.getContext(), "認証しました", Toast.LENGTH_SHORT).show();
 				} else {
 					Toast.makeText(mLayout.getContext(), "認証に失敗しました", Toast.LENGTH_SHORT).show();
 				}
 			}
 
 		}.execute((Void)null);
 	}
 	
 	private String getSelectedToken() {
 		int id = ((RadioGroup)mLayout.findViewById(R.id_settings.token_frame)).getCheckedRadioButtonId();
 		if(id < 0) {
 			throw new RuntimeException("tokenが選択されていません.実装のエラー");
 		}
 		
 		return getGoogleAccounts()[id];
 	}
 	
 	private boolean initTokenFrame() {
 		RadioGroup group = (RadioGroup)mLayout.findViewById(R.id_settings.token_frame);
 		group.clearCheck();
 		group.removeAllViews();
 		
 		String[] tokens = getGoogleAccounts();
 		if(tokens.length == 0) {
 			//TODO どうしようかな
 			return false;
 		}
 		
 		LayoutInflater inflater = (LayoutInflater)mLayout.getContext()
 			.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		for(int i = 0;i < tokens.length;++i) {
 			RadioButton btn = (RadioButton)inflater.inflate(R.layout.settings_token_radiobutton, null);
 			
 			RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(
 					RadioGroup.LayoutParams.FILL_PARENT,
 					RadioGroup.LayoutParams.WRAP_CONTENT);
 			btn.setLayoutParams(params);
 			btn.setText(tokens[i]);
 			btn.setId(i);
 			
 			group.addView(btn);
 		}
 		group.check(0);
 		
 		return true;
 	}
 	
 	private void showNoneAccountDialog() {
 		
 		new AlertDialog.Builder(mLayout.getContext())
 			.setTitle("アカウントがありません")
 			.setMessage(
 					"端末にGoogleアカウントが登録されていません。\n"
 					+ "このアプリケーションにはGoogleアカウントが必要です。\n"
 					+ "端末の設定メニューからGoogleアカウントを登録してください。\n"
 					)
 			.setPositiveButton("今は行わない", null)
 			.setNeutralButton("設定へ", new DialogInterface.OnClickListener() {
 				@Override public void onClick(DialogInterface dialog, int which) {
 					if(mListener != null) {
 						mListener.gotoAccountSettngs();
 					}
 				}
 			})
 			.show();
 	}
 	
 	private String[] getGoogleAccounts() {
 		ArrayList<String> accountNames = new ArrayList<String>();
 		Account[] accounts = AccountManager.get(mLayout.getContext()).getAccounts();
 		for (Account account : accounts) {
 			if(account.type.equals("com.google")) {
 				accountNames.add(account.name);
 			}
 		}
 		String[] result = new String[accountNames.size()];
 		accountNames.toArray(result);
 		return result;
 	}
 	
 	private int loginUpdate(final ProgressDialog dialog, final Handler handler,
 			User user, UserRecord record) {
 		class ProgressManipulator implements Runnable {
 			private final int progress;
 			
 			public ProgressManipulator(int progress) {
 				this.progress = progress;
 			}
 			
 			public void run() {
 				dialog.setProgress(progress);
 				
 				switch(progress) {
 				case 1:
 					dialog.setMessage("スタンプ情報取得中...");
 					break;
 				case 2:
 					dialog.setMessage("スタンプ情報更新中...");
 					break;
 				case 3:
 					dialog.setMessage("ユーザー履歴取得中...");
 					break;
 				case 4:
 					dialog.setMessage("ユーザー履歴更新中...");
 					break;
 				}
 			}
 		};
 		
 		try {
 			//サーバーからピンを取得
 			handler.post(new ProgressManipulator(1));
 			JSONObject obj = DataGetter.getJSONObject(StampRallyURL.getGetAllPinQuery());
 			if(StampRallyURL.isSuccess(obj)) {
 				handler.post(new ProgressManipulator(2));
 				StampPin[] serverPins = StampPin.decodeJSONObject(obj);
 				obj = null;
 				
 				//DBからピンを取得
 				StampPin[] dbPins = StampRallyDB.getStampPins();
 				
 				//Server<->DB間の更新情報を更新
 				Pair<StampPin[], StampPin[]> extract = StampPin.extractNewAndDeletePins(dbPins, serverPins);
 				StampRallyDB.deleteStampPins(extract.v2);
 				StampRallyDB.insertStampPins(extract.v1);
 				
 				handler.post(new ProgressManipulator(3));
 				//サーバから到着済みの場所を取得
 				obj = DataGetter.getJSONObject(StampRallyURL.getUserHistoryQuery(user, false));
 				handler.post(new ProgressManipulator(4));
 				UserHistory history = UserHistory.decodeJSONGetExecutedIds(obj);
 				//ユーザ履歴からすでに到着しているスタンプにフラグを立てる
 				StampRallyDB.checkPinNonArrive(history.executedPinIds);
 				//ユーザ履歴からすでに正解しているクイズにフラグを立てる
 				StampRallyDB.setQuizResult(history.executedQuizIds);
 				
 				//ユーザレコードをプリファレンスに保存する
 				record.numStamp = history.executedPinIds.length;
 				StampRallyPreferences.setUserRecord(record);
 				
 				return 0;
 			} else {
 				//XXX サーバとの通信失敗(クエリの間違い?)
 				Log.e("get pins", obj.toString());
 				return 1;
 			}
 			
 		} catch(IOException e) {
 			//XXX ネットワーク通信の失敗
 			e.printStackTrace();
 			return 2;
 		} catch(JSONException e) {
 			//XXX JSONフォーマットが不正
 			e.printStackTrace();
 			return 3;
 		}
 	}
 		
 }
