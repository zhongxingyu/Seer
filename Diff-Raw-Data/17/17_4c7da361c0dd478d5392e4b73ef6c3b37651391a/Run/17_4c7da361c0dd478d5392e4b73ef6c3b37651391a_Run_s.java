 package mx.v1ctor.android.vnotas;
 
 import mx.v1ctor.android.bd.BDOpenHelper;
 import mx.v1ctor.android.beans.NoteBean;
 import mx.v1ctor.android.files.PropertiesBD;
 import android.app.Activity;
 import android.content.Intent;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Bundle;
 import android.view.GestureDetector;
 import android.view.GestureDetector.SimpleOnGestureListener;
 import android.view.Menu;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class Run extends Activity {
 
 	private static final int SWIPE_MIN_DISTANCE = 50;
 	private static final int SWIPE_MAX_OFF_PATH = 250;
 	private static final int SWIPE_THRESHOLD_VELOCITY = 200;
 
 	private BDOpenHelper bdoh;
 	private SQLiteDatabase sqldb;
 	private EditText cajaNotas;
 	private Button clear;
 	private Button submit;
 	private PropertiesBD pbd;
 
 	private GestureDetector gestureDetector;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_run);
 
 		initiateInstances();
 		initiateGestures();
 	}
 
 	/**
 	 * Inicia los listeners para gestures
 	 */
 	private void initiateGestures() {
 
 		gestureDetector = new GestureDetector(new MyGestureDetector());
 		View runView = (View) findViewById(R.id.runView);
 
 		runView.setOnTouchListener(new View.OnTouchListener() {
 			public boolean onTouch(View v, MotionEvent event) {
 				if (gestureDetector.onTouchEvent(event)) {
 					return true;
 				}
 				return false;
 			}
 		});
 
 		cajaNotas.setOnTouchListener(new View.OnTouchListener() {
 			public boolean onTouch(View v, MotionEvent event) {
 				if (gestureDetector.onTouchEvent(event)) {
 					return true;
 				}
 				return false;
 			}
 		});
 
 	}
 
 	/**
 	 * Inicializa los elementos
 	 */
 	private void initiateInstances() {
 
 		bdoh = new BDOpenHelper(getApplicationContext());
 		sqldb = bdoh.getWritableDatabase();
 		pbd = new PropertiesBD(getApplicationContext());
 
 		cajaNotas = (EditText) findViewById(R.id.editNote);
 		clear = (Button) findViewById(R.id.buttonClear);
 		submit = (Button) findViewById(R.id.buttonSubmit);
 
 		clear.setOnClickListener(new OnClickListener() {
 
 			public void onClick(View v) {
 				cajaNotas.setText("");
 
 				Toast.makeText(getApplicationContext(),
 						getString(R.string.main_note_clearAlert),
 						Toast.LENGTH_SHORT).show();
 			}
 		});
 
 		submit.setOnClickListener(new OnClickListener() {
 
 			public void onClick(View v) {
 
 				String note = cajaNotas.getText().toString();
 				//Log.d("NOTE", ">>" + note);
 				if (note.length() > 0) {
 
 					NoteBean nb = new NoteBean(sqldb, pbd);
 					nb.setNote(note);
 					nb.create();
 					//Log.d("SUBMIT", "NEW REGISTER INSERTED");
 					cajaNotas.setText("");
 					// clear.performClick();
 
 					Toast.makeText(getApplicationContext(),
 							getString(R.string.main_note_sumbitAlert),
 							Toast.LENGTH_SHORT).show();
 				} else {
 					Toast.makeText(getApplicationContext(),
 							getString(R.string.main_note_alert),
 							Toast.LENGTH_SHORT).show();
 				}
 			}
 		});
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_run, menu);
 		return true;
 	}
 
 	@Override
 	protected void onDestroy() {
 
 		super.onDestroy();
 
 	}
 
 	/**
 	 * Movimiento de pantalla.
 	 * 
 	 * @author v1ctor
 	 * 
 	 */
 	private class MyGestureDetector extends SimpleOnGestureListener {
 
 		@Override
 		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
 				float velocityY) {
 
 			//Log.d("FLING", "Iniciando el gesto");
 
 			Intent intent = new Intent(Run.this.getBaseContext(),
 					ViewPosts.class);
 
 			if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH) {
 				return false;
 			}
 
 			// swipe de derecha a izquierda
 			if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
 					&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
 				startActivity(intent);
 				Run.this.overridePendingTransition(R.anim.slide_in_right,
 						R.anim.slide_out_left);
 				// swipe de izquierda a derecha
 			} else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
 					&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
 				startActivity(intent);
 				Run.this.overridePendingTransition(R.anim.slide_in_left,
 						R.anim.slide_out_right);
 			}
 
 			return false;
 		}
 
 		/**
 		 * Se requiere regresar true para que se registre el movimiento
 		 */
 		@Override
 		public boolean onDown(MotionEvent e) {
 			return true;
 		}
 	}
 
 }// activity
