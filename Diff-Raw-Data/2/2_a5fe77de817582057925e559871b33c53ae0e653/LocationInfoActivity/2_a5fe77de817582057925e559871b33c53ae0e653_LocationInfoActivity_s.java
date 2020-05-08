 package jag.kumamoto.apps.StampRally;
 
 import java.io.IOException;
 import java.net.URLEncoder;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import jag.kumamoto.apps.StampRally.Data.QuizData;
 import jag.kumamoto.apps.StampRally.Data.StampPin;
 import jag.kumamoto.apps.StampRally.Data.StampRallyURL;
 import jag.kumamoto.apps.StampRally.Data.User;
 import jag.kumamoto.apps.StampRally.Data.UserRecord;
 import jag.kumamoto.apps.gotochi.R;
 import aharisu.util.DataGetter;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.Window;
 import android.webkit.WebChromeClient;
 import android.webkit.WebSettings;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.AdapterView;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.ProgressBar;
 import android.widget.Spinner;
 import android.widget.Toast;
 
 
 /**
  * 
  * スタンプラリーのチェックポイントの情報を表示するアクティビティ
  * 
  * @author aharisu
  *
  */
 public class LocationInfoActivity extends Activity{
 	private static final int MenuItemGoForward = 1;
 	private static final int RequestLogin = 0;
 	
 	private User mUser;
 	private StampPin mPin;
 	private QuizData[] mQuizes;
 	
 	@Override protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 				
 		Bundle extras = getIntent().getExtras();
 		if(extras == null) {
 			setResult(Activity.RESULT_CANCELED);
 			finish();
 			return;
 		}
 		
 		mPin = (StampPin)extras.getParcelable(ConstantValue.ExtrasStampPin);
 		boolean isShowGoQuiz = extras.getBoolean(ConstantValue.ExtrasShowGoQuiz, false);
 		boolean isArrive = extras.getBoolean(ConstantValue.ExtrasIsArrive, false);
 		
 		if(mPin == null) {
 			setResult(Activity.RESULT_CANCELED);
 			finish();
 			return;
 		}
 		
 		mUser = extras.getParcelable(ConstantValue.ExtrasUser);
 		
 		
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.location_infomation);
 		
 		//WebViewの設定
 		WebView webInfo = (WebView)findViewById(R.id_location_info.webview);
 		webInfo.setWebViewClient(new WebViewClient() {
         	@Override public boolean shouldOverrideUrlLoading(WebView view, String url) {
         		if(url.startsWith("http")) {
         			view.loadUrl(url);
         			return true;
         		} else {
 	        		return super.shouldOverrideUrlLoading(view, url);
         		}
         	}
 		});
 		webInfo.setWebChromeClient(new WebChromeClient() {
 			@Override public void onProgressChanged(WebView view, int newProgress) {
 				super.onProgressChanged(view, newProgress);
 				
 				ProgressBar progress = (ProgressBar)findViewById(R.id_location_info.webView_progress);
 				if(newProgress < 85) {
 					if(progress.getVisibility() == View.GONE) {
 						progress.setVisibility(View.VISIBLE);
 					}
 					
 					progress.setProgress(newProgress);
 				} else {
 					progress.setVisibility(View.GONE);
 				}
 			}
 		});
 		WebSettings settings = webInfo.getSettings();
 		settings.setJavaScriptEnabled(true);
 		webInfo.loadUrl(getInfomationURL(mPin));
 		
 		
 		//クイズを表示できる状態ならボタンを表示
 		View goQuizFrame = findViewById(R.id_location_info.go_quiz_frame);
 		if(isShowGoQuiz) {
 			goQuizFrame.setVisibility(View.VISIBLE);
 			findViewById(R.id_location_info.progress_frame).setVisibility(View.VISIBLE);
 			
 			Button goQuiz = (Button)findViewById(R.id_location_info.go_quiz);
 			goQuiz.setEnabled(false);
 			goQuiz.setText(null);
 			goQuiz.setOnClickListener(createGoQuizOnClickListener());
 			
 			getAsyncQuizDataFromServer(mPin);
 		} else {
 			goQuizFrame.setVisibility(View.GONE);
 		}
 		
 		
 		//この場所に到着していれば到着報告を送信するボタンを表示
 		//また到着していなければ、この場所へ行くボタンを表示
 		View goLocation = findViewById(R.id_location_info.go_location_frame);
 		View btnArriveReport = findViewById(R.id_location_info.arrive_report);
 		if(isArrive) {
			if(StampRallyPreferences.getShowUrgeDialog()) {
 				goLocation.setVisibility(View.GONE);
 				btnArriveReport.setVisibility(View.VISIBLE);
 				btnArriveReport.setOnClickListener(new View.OnClickListener() {
 					@Override public void onClick(View v) {
 						if(mUser == null) {
 								showUrgeLoginDialog();
 						} else {
 							addUserRecord();
 							sendAsyncArrivedMessaeg();
 						}
 					}
 				});
 			} else {
 				goLocation.setVisibility(View.GONE);
 				btnArriveReport.setVisibility(View.GONE);
 			}
 		} else {
 			goLocation.setVisibility(View.VISIBLE);
 			btnArriveReport.setVisibility(View.GONE);
 			
 			findViewById(R.id_location_info.go_location).setOnClickListener(createOnRouteSearachClickListener());
 			
 			Spinner routeSearchKind = (Spinner)findViewById(R.id_location_info.route_search_kind);
 			routeSearchKind.setSelection(StampRallyPreferences.getRouteSearchKind());
 			routeSearchKind.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
 				@Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
 					StampRallyPreferences.setRouteSearchKind(position);
 				}
 				
 				@Override public void onNothingSelected(AdapterView<?> parent) {
 					parent.setSelection(0);
 				}
 			});
 		}
 		
 	}
 	
 	private String getInfomationURL(StampPin pin) {
 		return (pin.url != null && !pin.url.equals("") && !pin.url.equals("null")) ?
 				pin.url :
 				new StringBuilder("http://ja.wikipedia.org/wiki/")
 					.append(URLEncoder.encode(pin.name))
 					.toString();
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
 
 	private void getAsyncQuizDataFromServer(final StampPin pin) {
 		new AsyncTask<Void, Void, QuizData[]>() {
 			
 			@Override protected QuizData[] doInBackground(Void... params) {
 				
 				QuizData[] quizes = null;
 				try {
 					JSONObject obj = DataGetter.getJSONObject(StampRallyURL.getQuizesQuery(pin));
 					if(StampRallyURL.isSuccess(obj)) {
 						quizes = QuizData.decodeJSONObject(obj);
 					} else {
 						//XXX サーバとの通信失敗(クエリの間違い?)
 						Log.e("get quizes", obj.toString());
 					}
 				} catch (IOException e) {
 					//XXX ネットワーク通信の失敗
 					e.printStackTrace();
 				} catch (JSONException e) {
 					//XXX JSONフォーマットエラー
 					e.printStackTrace();
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
 			Log.e("quizData" , "get failure");
 			return;
 		}
 		
 		Button goQuiz = (Button)findViewById(R.id_location_info.go_quiz);
 		goQuiz.setEnabled(true);
 		goQuiz.setText("クイズへGo!!");
 	}
 	
 	private View.OnClickListener createOnRouteSearachClickListener() {
 		return new View.OnClickListener() {
 			
 			@Override public void onClick(View v) {
 				
 				long id = ((Spinner)findViewById(R.id_location_info.route_search_kind)).getSelectedItemId();
 				String[] routeSearchKind = new String[] {"d", "r", "w"};
 				
 				Intent intent = new Intent(Intent.ACTION_VIEW);
 				intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
 				String uri = new StringBuilder("http://maps.google.com/maps?myl=saddr")
 					.append("&dirflg=").append(routeSearchKind[(int) (id < 3 ? id : 0)])
 					.append("&daddr=").append(mPin.latitude * 1e-6f).append(",").append(mPin.longitude * 1e-6f)
 					.toString();
 				
 				intent.setData(Uri.parse(uri));
 				startActivity(intent);
 			}
 		};
 	}
 	
 	private void showUrgeLoginDialog() {
 		View layout = ((LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE))
 			.inflate(R.layout.location_urge_login_dialog_content, null);
 		((CheckBox)layout.findViewById(R.id_location_info.not_show_next_time)).setChecked(!StampRallyPreferences.getShowUrgeDialog());
 		((CheckBox)layout.findViewById(R.id_location_info.not_show_next_time)).setOnCheckedChangeListener(
 				new CompoundButton.OnCheckedChangeListener() {
 					
 					@Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 						StampRallyPreferences.setShowUrgeDialog(!isChecked);
 						if(isChecked) {
 							Toast.makeText(LocationInfoActivity.this, 
 									"設定画面でこのオプションを変更できます", Toast.LENGTH_LONG).show();
 						}
 					}
 				});
 		
 		new AlertDialog.Builder(this)
 			.setTitle("ログインしていません")
 			.setView(layout)
 			.setPositiveButton("ログインする", new DialogInterface.OnClickListener() {
 				@Override public void onClick(DialogInterface dialog, int which) {
 					Intent intent = new Intent(LocationInfoActivity.this, SettingsActivity.class);
 					intent.putExtra(ConstantValue.ExtrasLoginRequest, true);
 					
 					startActivityForResult(intent, RequestLogin);
 				}
 			})
 			.setNegativeButton("取り消し", null)
 			.show();
 	}
 	
 	private void addUserRecord() {
 		boolean[] arrived = StampRallyDB.checkPinNonArrive(mPin.id);
 		if(arrived[0]) {
 			UserRecord record = StampRallyPreferences.getUserRecord();
 			record.point += mPin.point;
 			record.numStamp += 1;
 			StampRallyPreferences.setUserRecord(record);
 		}
 	}
 	
 	private void sendAsyncArrivedMessaeg() {
 		Toast.makeText(LocationInfoActivity.this, "到着確認を送信します", Toast.LENGTH_SHORT).show();
 		
 		final String query = StampRallyURL.getArriveQuery(mUser, mPin);
 		
 		final View btnArriveReport = findViewById(R.id_location_info.arrive_report);
 		btnArriveReport.setEnabled(false);
 		new AsyncTask<Void, Void, Boolean>() {
 			@Override protected Boolean doInBackground(Void... params) {
 				try {
 					JSONObject obj = DataGetter.getJSONObject(query);
 					if(StampRallyURL.isSuccess(obj)) {
 						return true;
 					} else {
 						//XXX サーバとの通信失敗(クエリの間違い?)
 						Log.e("arrive data", obj.toString());
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
 				if(result) {
 					btnArriveReport.setVisibility(View.GONE);
 				} else {
 					//TODO 到着データ送信に失敗
 					//さてどうしよう
 					Log.i("arrive data", "failure");
 					btnArriveReport.setEnabled(true);
 				}
 				
 				Toast.makeText(LocationInfoActivity.this, result ?
 						"到着完了!" : "あれ？ネットワークの調子がおかしいぞ",
 						Toast.LENGTH_SHORT).show();
 			}
 			
 		}.execute((Void)null);
 	}
 	
 	//WebViewを操作するメニュー
 	@Override public boolean onCreateOptionsMenu(Menu menu) {
 		menu.add(0, MenuItemGoForward, 0, "進む").setIcon(R.drawable.ic_menu_forward);
 		
 		return super.onCreateOptionsMenu(menu);
 	}
 	
 	@Override public boolean onMenuOpened(int featureId, Menu menu) {
 		WebView webInfo = (WebView)findViewById(R.id_location_info.webview);
 		menu.findItem(MenuItemGoForward).setEnabled(webInfo.canGoForward());
 		
 		return super.onMenuOpened(featureId, menu);
 	}
 	
 	@Override public boolean onMenuItemSelected(int featureId, MenuItem item) {
 		if(item.getItemId() == MenuItemGoForward) {
 			//進む履歴があれば次ページに進む
 			WebView webInfo = (WebView)findViewById(R.id_location_info.webview);
 			if(webInfo.canGoForward()) {
 				webInfo.goForward();
 			}
 			
 			return true;
 		}
 		
 		return super.onMenuItemSelected(featureId, item);
 	}
 
 	@Override public boolean onKeyDown(int keyCode, KeyEvent event) {
 		
 		if(event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
 			//戻る履歴があれば前ページに戻る
 			WebView webInfo = (WebView)findViewById(R.id_location_info.webview);
 			if(webInfo.canGoBack()) {
 				webInfo.goBack();
 				return true;
 			}
 		}
 		
 		return super.onKeyDown(keyCode, event);
 	}
 	
 	@Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if(requestCode == RequestLogin) {
 			if(resultCode == Activity.RESULT_OK) {
 				User user = data.getExtras().getParcelable(ConstantValue.ExtrasUser);
 				if(user != null) {
 					mUser = user;
 					
 					addUserRecord();
 					sendAsyncArrivedMessaeg();
 				}
 			}
 		}
 		
 		super.onActivityResult(requestCode, resultCode, data);
 	}
 	
 }
