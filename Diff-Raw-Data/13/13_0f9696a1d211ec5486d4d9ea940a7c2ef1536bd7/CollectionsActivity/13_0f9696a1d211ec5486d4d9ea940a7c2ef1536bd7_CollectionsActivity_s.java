 package jag.kumamoto.apps.StampRally;
 
 import java.io.IOException;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
import jag.kumamoto.apps.StampRally.Data.Item;
 import jag.kumamoto.apps.StampRally.Data.Prize;
 import jag.kumamoto.apps.StampRally.Data.StampRallyURL;
 import jag.kumamoto.apps.StampRally.Data.User;
 import jag.kumamoto.apps.StampRally.Data.UserRecord;
 import jag.kumamoto.apps.gotochi.R;
 import aharisu.util.AsyncDataGetter;
 import aharisu.util.DataGetter;
 import aharisu.util.Pair;
 import aharisu.util.Size;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.BaseAdapter;
 import android.widget.Gallery;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 /**
  * 
  * 獲得ポイントやアイテムを一覧表示するアクティビティ
  * 
  * @author aharisu
  *
  */
 public class CollectionsActivity extends Activity{
 	private static final int RequestLogin = 0;
 	
 	private User mUser;
 	private UserRecord mUserRecord;
 	private Prize[] mPrizes;
 	
 	@Override protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		Bundle extras = getIntent().getExtras();
 		if(extras != null) {
 			mUser = extras.getParcelable(ConstantValue.ExtrasUser);
 		}
 		
 		if(mUser != null) {
 			mUserRecord = StampRallyPreferences.getUserRecord();
 			
 			mPrizes = StampRallyDB.getPrizes();
 		} else {
 			mUserRecord = null;
 			mPrizes = new Prize[0];
 		}
 		
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.collections);
 		
 		constractView();
 	}
 	
 	private void constractView() {
 		if(mUserRecord != null) {
 			//User名を設定
 			((TextView)findViewById(R.id_collections.user_name)).setText(new StringBuilder(mUser.nickname).append("の成績"));
 			
 			//アイテムギャラリーを設定
 			((Gallery)findViewById(R.id_collections.item_gallery)).setAdapter(createItemGalleryAdapter());
 			
 			//Pointを設定
 			((TextView)findViewById(R.id_collections.point)).setText(Integer.toString(mUserRecord.point));
 			
 			//スタンプ数を設定
 			((TextView)findViewById(R.id_collections.num_stamp)).setText(Integer.toString(mUserRecord.numStamp));
 			
 			//クイズに答えた回数を設定
 			((TextView)findViewById(R.id_collections.num_answerd)).setText(Integer.toString(mUserRecord.numTotalAnswerdQuize));
 					
 			//正答率の設定
 			float rate = mUserRecord.numCorrectness == 0 ? 
 					0 :
					mUserRecord.numTotalAnswerdQuize / (float)mUserRecord.numCorrectness;
 			rate *= 100;
 			((TextView)findViewById(R.id_collections.correctness_rate)).setText(String.format("%.1f%%", rate));
 			
 			//平均解答時間の設定
 			double time = mUserRecord.numTotalAnswerdQuize == 0 ?
 					0 :
 					mUserRecord.totalAnswerTime / (double)mUserRecord.numTotalAnswerdQuize;
			time *= 1000;
 			((TextView)findViewById(R.id_collections.ave_answer_time)).setText(String.format("%.3f秒", time));
 			
 			//獲得アイテム数を設定
 			((TextView)findViewById(R.id_collections.num_item)).setText(Integer.toString(mPrizes.length));
 			
 			//追加受賞がないかチェック
 			getAsyncPrizesDataFromServer();
 		} else {
 			showUrgeLoginDialog();
 		}
 	}
 	
 	private BaseAdapter createItemGalleryAdapter() {
 		DisplayMetrics metrics = new DisplayMetrics();
 		((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metrics);
 		
 		final float scaledDensity = metrics.scaledDensity;
 		return new BaseAdapter() {
 			
 			@Override public View getView(final int position, View convertView, ViewGroup parent) {
 				if(convertView == null) {
 					convertView =  ((LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE))
 						.inflate(R.layout.collections_item, null);
 				}
 				Prize prize = mPrizes[position];
 				
 				//アイテムの画像を設定
 				final ImageView ivw = (ImageView)convertView.findViewById(R.id_collections.item_image);
 				ivw.setTag(position);
 				AsyncDataGetter.getBitmapCache(prize.item.imageUrl, new AsyncDataGetter.BitmapCallback() {
 					
 					@Override public void onGetData(Bitmap data) {
 						int pos = (Integer)ivw.getTag();
 						if(pos == position) {
 							ivw.setImageBitmap(data);
 						}
 					}
 					
 					@Override public void onFailure(Exception e) {
 						//XXX どうしようかな
 						//ごめんなさいと頭を下げる画像がほしいな
 					}
 					
 					@Override public Size getMaxImageSize() {
 						return new Size((int)(110 * scaledDensity), (int)(110 * scaledDensity));
 					}
 				});
 				
 				//アイテム名設定
 				((TextView)convertView.findViewById(R.id_collections.item_name)).setText(prize.item.name);
 				
 				return convertView;
 			}
 			
 			@Override public long getItemId(int position) {
 				return position;
 			}
 			
 			@Override public Object getItem(int position) {
 				return mPrizes[position];
 			}
 			
 			@Override public int getCount() {
 				return mPrizes.length;
 			}
 		};
 	}
 
 	private void getAsyncPrizesDataFromServer() {
 		new AsyncTask<Void, Void, Pair<Prize[], Prize[]>>() {
 			@Override protected Pair<Prize[], Prize[]> doInBackground(Void... params) {
 				Prize[] prizes = null;
 				try {
 					JSONObject obj = DataGetter.getJSONObject(StampRallyURL.getPrizesQuery(mUser));
 					if(StampRallyURL.isSuccess(obj)) {
 						prizes = Prize.decodeJSONObject(obj);
 					} else {
 						//XXX サーバとの通信失敗(クエリの間違い?)
 						Log.e("get quizes", obj.toString());
 					}
 				} catch(IOException e) {
 					//XXX ネットワーク通信の失敗
 					e.printStackTrace();
 				} catch (JSONException e) {
 					//XXX JSONフォーマットエラー
 					e.printStackTrace();
 				}
 				
 				if(prizes == null) {
 					return null;
 				} else {
 					return new Pair<Prize[], Prize[]>(prizes, 
 							Prize.extractNewPrizes(mPrizes, prizes));
 				}
 			}
 			
 			@Override protected void onPostExecute(Pair<Prize[],Prize[]> result) {
 				if(result != null && result.v2.length != 0) {
 					mPrizes = result.v1;
 					
 					StampRallyDB.insertPrizes(result.v2);
 					
 					((BaseAdapter)((Gallery)findViewById(R.id_collections.item_gallery))
 							.getAdapter()).notifyDataSetChanged();
 				}
 			}
 			
 		}.execute((Void)null);
 	}
 	
 	private void showUrgeLoginDialog() {
 		new AlertDialog.Builder(this)
 			.setTitle("ログインしていません")
 			.setMessage("ログインしていないとユーザ情報を見ることはできません。\n設定画面からログインしてください")
 			.setPositiveButton("ログインする", new DialogInterface.OnClickListener() {
 				@Override public void onClick(DialogInterface dialog, int which) {
 					Intent intent = new Intent(CollectionsActivity.this, SettingsActivity.class);
 					intent.putExtra(ConstantValue.ExtrasLoginRequest, true);
 					
 					startActivityForResult(intent, RequestLogin);
 				}
 			})
 			.setNegativeButton("ホームに戻る", new DialogInterface.OnClickListener() {
 				@Override public void onClick(DialogInterface dialog, int which) {
 					finish();
 				}
 			})
 			.show();
 	}
 	
 	@Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if(requestCode == RequestLogin) {
 			if(resultCode == Activity.RESULT_OK) {
 				User user = data.getExtras().getParcelable(ConstantValue.ExtrasUser);
 				if(user != null) {
 					mUser = user;
 					
 					mUserRecord = StampRallyPreferences.getUserRecord();
 					
 					mPrizes = StampRallyDB.getPrizes();
 				} else {
 					mUser = null;
 					mUserRecord = null;
 					mPrizes = new Prize[0];
 				}
 			}
 			
 			constractView();
 			
 			return;
 		}
 		
 		super.onActivityResult(requestCode, resultCode, data);
 	}
 	
 }
