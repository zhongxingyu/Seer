 package jp.ddo.haselab.timerecoder;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.widget.Button;
 import android.widget.TextView;
 import android.view.View.OnClickListener;
 import android.view.View;
 import android.content.Intent;
 import android.util.Log;
 import android.database.sqlite.SQLiteDatabase;
 import jp.ddo.haselab.timerecoder.dataaccess.DatabaseHelper;
 import jp.ddo.haselab.timerecoder.dataaccess.Recode;
 import jp.ddo.haselab.timerecoder.dataaccess.RecodeDao;
 import jp.ddo.haselab.timerecoder.util.RecodeDateTime;
 
 /**
  * 主処理Activity.
  * main 画面の処理を行います。
  *
  * @author T.Hasegawa
  */
 public final class MainActivity extends Activity implements OnClickListener {
 
     private static final String LOG_TAG = "MainActivity";
 
     private SQLiteDatabase mDb = null;
 
     /**
      * create.
      * 各種ボタンのイベント登録などを行います。
      * @param savedInstanceState hmm
      */
     @Override
         protected void onCreate(final Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
 	Log.v(LOG_TAG,"start onCreate");
         setContentView(R.layout.main);
 
         DatabaseHelper dbHelper = new DatabaseHelper(this);
 	mDb = dbHelper.getWritableDatabase();
 
 	Recode rec = new Recode(new RecodeDateTime(),1,"hoge");
 	RecodeDao dao = new RecodeDao(mDb);
 
 	mDb.beginTransaction();
 	long key = 0;
 	try {
 	    key = dao.insert(rec);
 	    mDb.setTransactionSuccessful();
 	} finally {
 	    mDb.endTransaction();
 	    Log.v(LOG_TAG,"commit key=" + key);
 	}
 	long co = dao.count();
 	Log.v(LOG_TAG,"count =" + co);
 
         Button button;
         button = (Button) findViewById(R.id.button_start);
         button.setOnClickListener(this);
         button = (Button) findViewById(R.id.button_quit);
         button.setOnClickListener(this);
      }
 
     /**
      * onDestroy
      * DBのclose
      */
     @Override
 	protected void onDestroy(){
 	Log.v(LOG_TAG,"start onDestory");
 	if(mDb != null) {
 	    Log.v(LOG_TAG,"close db");
	    //	    mDb.close();
 	}
 	super.onDestroy();
     }
 
 
     /**
      * クリック時の処理.
      * 各種ボタンの処理を行います。
      * @param v 押されたview
      */
     @Override
         public void onClick(final View v) {
         int id = v.getId();
         if (id == R.id.button_quit) {
 	    Log.v(LOG_TAG,"button_quit");
             finish();
             return;
         }
 	if (id == R.id.button_start) {
 	    Log.v(LOG_TAG,"button_start");
 	    Recode rec = new Recode(new RecodeDateTime(),1,"hoge");
 	    RecodeDao dao = new RecodeDao(mDb);
 	    
 	    mDb.beginTransaction();
 	    long key = 0;
 	    try {
 		key = dao.insert(rec);
 		mDb.setTransactionSuccessful();
 	    } finally {
 		mDb.endTransaction();
 		Log.v(LOG_TAG,"commit key=" + key);
 	    }
 	    long co = dao.count();
 	    Log.v(LOG_TAG,"count =" + co);
             return;
         }
         return;
     }
 }
