 
 package com.michitsuchida.marketfavoritter.main;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.regex.PatternSyntaxException;
 
 import javax.net.ssl.HttpsURLConnection;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.text.ClipboardManager;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.michitsuchida.marketfavoritter.db.DBMainStore;
 
 /**
  * マーケットアプリからのIntentを受けた場合に呼ばれるActivity。
  * 
  * @author MichiTsuchida
  */
 public class ReceiveMarketIntentActivity extends Activity {
 
     /** LOG TAG */
     static final String LOG_TAG = "MarketBookmark";
 
     /** Handlerでアプリ名を取得する際に使用するKey */
     public static final String EXTRA_APP_NAME = "APP_NAME";
 
     /** <title>タグの内容をぶっこ抜く正規表現パターン */
     public static final String PATTERN_FOR_TITLE_TAG = ".*<title>(.+)\\s-\\s.+</title>.*";
 
     /** マーケットアプリのパッケージ名 */
    public static final String MARKET_URL = "market.android.com/details";
 
     /** <title>タグの内容を格納する */
     private String mTitle = "";
 
     /** 取得されたアプリ名 */
     private String mAppName = "";
 
     /** Package名 */
     private String mPkg = "";
 
     /** マーケットのURL */
     private String mUrl = "";
 
     /** ラベル */
     private String mLabel;
 
     /** アプリ名のEditText */
     private EditText mEtAppName;
 
     /** ラベルのEditText */
     private EditText mEtLabel;
 
     /** 処理中にくるくるさせるダイアログ */
     private ProgressDialog mProg;
 
     /**
      * onCreate.
      * 
      * @param savedInstanceState
      */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.market_link_intent);
         final Context context = this;
 
         // String packageName = getCallingActivity().getPackageName();
         // String className = getCallingActivity().getClassName();
         // Log.d(LOG_TAG, "Caller package: " + packageName);
         // Log.i(LOG_TAG, "Caller class: " + className);
 
         Intent marketIntent = getIntent();
         String action = marketIntent.getAction();
 
         // Actionの判定
         if (Intent.ACTION_SEND.equals(action)) {
             // Intentの情報を取得する
             Bundle extras = marketIntent.getExtras();
             if (extras != null) {
                 // アプリのURLを取得する
                 mUrl = extras.getCharSequence(Intent.EXTRA_TEXT).toString();
                 Log.d(LOG_TAG, mUrl);
 
                 // 呼び出し元がマーケットかどうか判定
                if (mUrl.contains(MARKET_URL)) {
                     // URLをパッケージ名に変換
                     mPkg = mUrl.substring(mUrl.indexOf("id=") + 3);
 
                     // DBの重複チェック
                     final DBMainStore mainStore = new DBMainStore(this, true);
                     AppElement elem = mainStore.fetchAppDataByColumnAndValue(
                             DBMainStore.COLUMN_APP_PACKAGE, mPkg);
                     if (elem != null) {
                         Log.d(LOG_TAG, "Already exist on database. App name: " + mAppName);
                         // なぜかToast表示されないので、重複してるよダイアログ
                         AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                         dialog.setTitle(R.string.dialog_add_item_title);
                         dialog.setMessage(R.string.dialog_add_item_text);
 
                         // OKボタン
                         dialog.setPositiveButton(R.string.button_ok,
                                 new DialogInterface.OnClickListener() {
                                     @Override
                                     public void onClick(DialogInterface dialog, int i) {
                                         // Activityを終了する
                                         finish();
                                     }
                                 }).show();
                     } else {
                         // 1.プログレスバーをくるくる
                         mProg = new ProgressDialog(this);
                         mProg.setTitle(R.string.progress_title);
                         mProg.setMessage(getString(R.string.progress_body));
                         mProg.show();
 
                         // Viewに情報をセットする
                         mEtAppName = (EditText) findViewById(R.id.marketLinkEditTextAppName);
                         mEtAppName.setText(mAppName);
                         TextView tvPkgName = (TextView) findViewById(R.id.marketLinkTextPkgName);
                         tvPkgName.setText(mPkg);
                         TextView tvUrl = (TextView) findViewById(R.id.marketLinkTextUrlBody);
                         tvUrl.setText(mUrl);
                         mEtLabel = (EditText) findViewById(R.id.marketLinkEditTextLabel);
 
                         Button addButton = (Button) findViewById(R.id.marketLinkAddButton);
                         addButton.setOnClickListener(new OnClickListener() {
                             @Override
                             public void onClick(View v) {
                                 mAppName = mEtAppName.getText().toString();
                                 mLabel = mEtLabel.getText().toString();
 
                                 // DBに追加する
                                 mainStore.add(mAppName, mPkg, mUrl, mLabel);
 
                                 // Activityを終了する
                                 finish();
                                 Toast.makeText(context,
                                         mAppName + getString(R.string.toast_added_item),
                                         Toast.LENGTH_LONG).show();
                                 Log.d(LOG_TAG, "Add to Market Bookmark. App name: " + mAppName);
                             }
                         });
 
                         Button copyButton = (Button) findViewById(R.id.marketLinkUrlCopyButton);
                         copyButton.setOnClickListener(new OnClickListener() {
                             @Override
                             public void onClick(View view) {
                                 ClipboardManager clpbrd = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                                 clpbrd.setText(mUrl);
                                 Toast.makeText(context,
                                         mUrl + getString(R.string.toast_copied_to_clipboard),
                                         Toast.LENGTH_LONG).show();
                             }
                         });
 
                         // 2.別スレッドを生成して、アプリ名を取得する
                         (new Thread(runnable)).start();
                     } /* if (elem != null) */
                 } else {
                     // マーケットから呼びだしてねダイアログ
                     AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                     dialog.setTitle(R.string.dialog_share_from_market_title);
                     dialog.setMessage(R.string.dialog_share_from_market_text);
 
                     // OKボタン
                     dialog.setPositiveButton(R.string.button_ok,
                             new DialogInterface.OnClickListener() {
                                 @Override
                                 public void onClick(DialogInterface dialog, int i) {
                                     // Activityを終了する
                                     finish();
                                 }
                             }).show();
                } /* if (mUrl.indexOf(MARKET_URL) != -1) */
             } /* if (extras != null) */
         } /* if (Intent.ACTION_SEND.equals(action)) */
     } /* onCreate() */
 
     /**
      * メインとは別のアプリ名を取得するためのスレッド。
      */
     private Runnable runnable = new Runnable() {
         @Override
         public void run() {
             // 実際に別スレッドでやりたい処理はここで
             doRequest(mPkg);
 
             // このRunnableから直接UIを書き換えようとすると、
             // CalledFromWrongThreadException: Only the original thread that
             // created a view hierarchy can touch its views.
             // で落ちる(このRunnableはUIスレッドとは別スレッドで立ち上がっているため)
 
             // 3.UIのテキストを変更 UIを書き換える場合はHandlerに処理を渡す
             // 実際に行う処理の結果（ループカウンタ等）を動的にUI側へ渡したい場合
             // Handler#sendMessage()にBundleをセットして渡す
 
             Message message = new Message();
             Bundle bundle = new Bundle();
             bundle.putString(EXTRA_APP_NAME, mAppName);
             message.setData(bundle);
             handler.sendMessage(message);
 
             // 4.実際に行いたい処理が終わったらダイアログを消去
             mProg.dismiss();
         }
     };
 
     /**
      * 別スレッドで取得したアプリ名を変更するHandler。
      */
     private final Handler handler = new Handler() {
         @Override
         public void handleMessage(Message msg) {
             // sendMessage()で渡されたBundleを取得してUIに表示
             String text = msg.getData().get(EXTRA_APP_NAME).toString();
             ((TextView) findViewById(R.id.marketLinkEditTextAppName)).setText(text);
         }
     };
 
     /**
      * HTTPリクエストを実行する。<br>
      * HTTPコネクションに失敗した時のために、3回リトライする。<br>
      * それでもダメなら諦めるｗｗ<br>
      * 取得したアプリ名は、Handlerを使ってメインのスレッドに渡す。
      * 
      * @param pkg アプリケーションのpackage名
      */
     private void doRequest(String pkg) {
         final String urlStr = "https://market.android.com/details?id=" + pkg;
         Log.d(LOG_TAG, "URL for app is: " + urlStr);
 
         // 3回リトライする
         for (int i = 0; i < 2; i++) {
             // HttpでGETするよ!!
             InputStream in = null;
             HttpsURLConnection https = null;
             try {
                 // URLにHTTP接続
                 URL url = new URL(urlStr);
                 https = (HttpsURLConnection) url.openConnection();
                 https.setRequestMethod("GET");
                 https.connect();
                 // データを取得
                 in = https.getInputStream();
 
                 // HTMLソースを読み出す
                 String src = new String();
                 byte[] line = new byte[1024];
                 int size;
                 while (true) {
                     size = in.read(line);
                     if (size <= 0) {
                         break;
                     }
                     src += new String(line);
 
                     // HTMLのソースから<title>タグだけをぶっこ抜く正規表現
                     Pattern pattern = Pattern.compile(PATTERN_FOR_TITLE_TAG);
                     // String#toLowerCase()で一括小文字にして比較
                     // Matcher matcher = pattern.matcher(src.toLowerCase());
                     Matcher matcher = pattern.matcher(src);
                     if (matcher.find()) {
                         mTitle = matcher.group(1);
                         Log.d(LOG_TAG, "App found: " + mTitle);
                         mAppName = mTitle;
                         break;
                     }
                 }
                 if (!mAppName.equals("")) {
                     Log.d(LOG_TAG, "App name: " + mAppName);
                     break;
                 }
             } catch (PatternSyntaxException e) {
                 // 例外発生時はスタックトレースを出力
                 e.printStackTrace();
             } catch (IOException e) {
                 e.printStackTrace();
             } finally {
                 // close処理はfinally内でtry-catchする
                 try {
                     if (https != null) {
                         https.disconnect();
                     }
                     if (in != null) {
                         in.close();
                     }
                 } catch (Exception e) {
                     // FroyoだけHttpsURLConnectionにバグがあってNullPtrExが出ちゃう。。
                     e.printStackTrace();
                 } /* try in finally */
             } /* try */
         } /* for */
     } /* doRequest() */
 }
