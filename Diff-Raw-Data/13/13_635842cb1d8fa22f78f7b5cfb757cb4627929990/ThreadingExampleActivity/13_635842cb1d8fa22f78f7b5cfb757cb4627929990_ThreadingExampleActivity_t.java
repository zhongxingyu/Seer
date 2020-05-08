 package com.hstefan.threadingexample;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 import java.util.concurrent.Executor;
 
 import com.hstefan.threadingexample.DotProductThreadingSingleton.OnDotProductCalculationListener;
 import com.hstefan.threadingexample.db.ResultDbOpenHelper;
 
 import android.app.Activity;
 import android.content.ContentValues;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.provider.BaseColumns;
 import android.text.Editable;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.SimpleCursorAdapter;
 import android.widget.Toast;
 
 public class ThreadingExampleActivity extends Activity implements VectorGenerationTask.OnTaskFinishedListener<Float[]>, 
 	OnClickListener, OnDotProductCalculationListener {
 
 	/** Called when the activity is first created. */
 	private Float[] m_u, m_v;
 	private List<AsyncTask<Integer, Void, Float>> m_Tasks;
 	private DotProductThreadingSingleton m_dotInstance;
 	private int m_numThreadsLRun;
 	private ResultDbOpenHelper m_resultsDbHelper;
 	private SQLiteDatabase m_resultDb;
 	private SimpleCursorAdapter m_rAdapter;
 	
 	//views
 	private Button bRun;
 	private EditText eLen;
 	private EditText eThreadNum;
 	
 	public static final int DEFAULT_VEC_LENGTH = 200000;
 	public static final int DEFAULT_THREAD_NUM = 0;
 	private static String[] RESULT_DB_COLUMNS= new String[]{ BaseColumns._ID,
 		ResultDbOpenHelper.RESULT_DATE, ResultDbOpenHelper.RESULT_NUMTHREADS, 
 		ResultDbOpenHelper.RESULT_PROCTIME};
 	private static int RESULT_DB_COLUMNS_LAYOUT[] = new int[]{
 		R.id.runTimestampDate, R.id.threadNum, R.id.time
 	};
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
     	setContentView(R.layout.main);
         
     	setupViews();
         setupDotTask();
         generateVectors(DEFAULT_VEC_LENGTH);
         
         m_numThreadsLRun = 0;
  
         super.onCreate(savedInstanceState);
     }
 
 
 
 	private void setupListView() {
 		m_resultsDbHelper = new ResultDbOpenHelper(this);
         m_resultDb = m_resultsDbHelper.getWritableDatabase();
         
         Cursor c = m_resultDb.query(ResultDbOpenHelper.RESULT_TABLE, 
         		RESULT_DB_COLUMNS, null, null, null, null, null);
         m_rAdapter = new SimpleCursorAdapter(this, R.layout.result_item, c, new String[]{
         		ResultDbOpenHelper.RESULT_DATE, ResultDbOpenHelper.RESULT_NUMTHREADS, 
         		ResultDbOpenHelper.RESULT_PROCTIME} , 
         		RESULT_DB_COLUMNS_LAYOUT);
         
         ListView lView = (ListView)findViewById(R.id.lResultList);
         lView.setAdapter(m_rAdapter);
 	}
 
 
 
 	private void setupDotTask() {
 		m_dotInstance = DotProductThreadingSingleton.getInstance();
         m_dotInstance.setOnDotProductCalculationListener(this);
         m_Tasks = new ArrayList<AsyncTask<Integer,Void,Float>>();
 	}
 
 
 
 	private void setupViews() {
 		bRun = (Button)findViewById(R.id.bRun);
     	bRun.setClickable(false);
     	bRun.setOnClickListener((android.view.View.OnClickListener) this);
     	eLen = (EditText)findViewById(R.id.eNumElements);
     	eLen.setText(Integer.toString(DEFAULT_VEC_LENGTH));
     	eThreadNum = (EditText) findViewById(R.id.eNumThreads);
     	eThreadNum.setText(Integer.toString(DEFAULT_THREAD_NUM));
     	
     	setupListView();
 	}
 
 
 
 	private void generateVectors(int len) {
		bRun.setClickable(false);
 		Toast.makeText(this, "Generating vector", Toast.LENGTH_LONG).show();
         new VectorGenerationTask().setOnFinishListener(this).execute(
         	new VectorGenerationData(len, bRun, this));
 	}
     
     
 
 	@Override
 	protected void onDestroy() {
 		m_resultsDbHelper.getWritableDatabase().close();
 		super.onDestroy();
 	}
 
 
 
 	@Override
 	public void onTaskFinish(Float[][] result) {
 		Toast.makeText(this, "Vector generated", Toast.LENGTH_LONG).show();
 		bRun.setClickable(true);
 		m_u = result[0];
 		m_v = result[1];
 		m_dotInstance.setVectorUVector(m_u);
 		m_dotInstance.setVectorVVector(m_v);
		bRun.setClickable(true);
 	}
 
 	@Override
 	public void onClick(View v) {
 		if(checkVectorLength()) {
 			Editable eEntry = eThreadNum.getText();
 			if(eEntry.toString().isEmpty()) {
 				eEntry.append("0");
 			}
 			
 			int numThreads = Integer.parseInt(eEntry.toString());
 			
 			if(numThreads == 0) {
 				numThreads = Runtime.getRuntime().availableProcessors();
 			}
 			
 			calculateDotProduct(numThreads);
 		}
 	}
 
 
 
 	private boolean checkVectorLength() {
 		int eLenEntry = Integer.parseInt(eLen.getEditableText().toString());
 
 		if(eLenEntry != m_u.length) {
 			Log.d("Vector generation event:", eLenEntry + "/" + m_u.length);
 			generateVectors(eLenEntry);
 			System.gc();
 			return false;
 		} else {
 			return true;
 		}
 	}
 	
 	private void calculateDotProduct(int numThreads) {
 		Log.d("MethodCall", "calculateDotProduct");
 		int sliceSz = m_u.length/numThreads;
 		int numSlices = m_u.length/sliceSz;
 		Executor tExec = AsyncTask.THREAD_POOL_EXECUTOR;
 		m_dotInstance.setNumThreads(numThreads);
 		m_dotInstance.startTimer();
 		m_Tasks.clear();
 		for(int slice = 0; slice < numSlices; ++slice) {
 			Log.d("Thread", "Spawning thread #" + slice);
 			assert((slice*sliceSz >= 0) && (slice*sliceSz + sliceSz <= m_u.length));
 			m_Tasks.add(new DotProductAsyncTask().executeOnExecutor(tExec, 
 					slice*sliceSz, (slice*sliceSz) + sliceSz));
 		}
 		m_numThreadsLRun = numThreads;
 	}
 
 	@Override
 	public void onDotProductCalculation(float res) {
 		Log.d("MethodCall", "onDotProductCalculation");
 		SQLiteDatabase db = m_resultsDbHelper.getWritableDatabase();
 		ContentValues values = new ContentValues();
 		
 		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 		
 		values.put(ResultDbOpenHelper.RESULT_DATE, dateFormat.format(Calendar.getInstance().getTime()));
 		values.put(ResultDbOpenHelper.RESULT_NUMTHREADS, m_numThreadsLRun);
 		values.put(ResultDbOpenHelper.RESULT_PROCTIME, m_dotInstance.getElapsedTime());
 		
 		db.insert(ResultDbOpenHelper.RESULT_TABLE, null, values);
 		
 		m_rAdapter.getCursor().requery();
 	}
 }
