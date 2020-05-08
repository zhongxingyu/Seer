 package mx.v1ctor.android.vnotas;
 
 import java.util.LinkedList;
 
 import mx.v1ctor.android.bd.BDObject;
 import mx.v1ctor.android.bd.BDOpenHelper;
 import mx.v1ctor.android.beans.NoteBean;
 import mx.v1ctor.android.collections.NotesCollection;
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
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TableRow.LayoutParams;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class ViewPosts extends Activity {
 
 	private static final int SWIPE_MIN_DISTANCE = 50;
 	private static final int SWIPE_MAX_OFF_PATH = 250;
 	private static final int SWIPE_THRESHOLD_VELOCITY = 200;
 
 	private GestureDetector gestureDetector;
 
 	private BDOpenHelper bdoh;
 	private SQLiteDatabase sqldb;
 	private TableLayout tabla;
 	private Button clear;
 	private Button selectAll;
 	private PropertiesBD pbd;
 	private TextView text_title;
 	private boolean tablaCargada;
 
 	private LinkedList<BDObject> listaNotas;
 	private String title;
 	private NotesCollection notes;
 	long n = 0; // número de registros
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_view_posts);
 		initiateInstances();
 		initiateGestures();
 
 		initiateButtons();
 		// Si regresamos a la vista, carbamos la tabla de BD
 		tablaCargada = false;
 		populateTable();
 
 	}
 
 	/**
 	 * Inicia los escuchas de botones
 	 */
 	private void initiateButtons() {
 
 		clear.setOnClickListener(new OnClickListener() {
 
 			public void onClick(View v) {
 				if (n > 0) {
 
 					for (BDObject object : listaNotas) {
 						NoteBean nb = (NoteBean) object;
 
 						if (nb.isFlag()) {
 							nb.delete();
 						}// borramos esa nota
 
 					}
 					// cargamos nuevamente la tabla de disco
 
 					tablaCargada = false;
 					populateTable();
 				}
 			}
 		});
 
 		selectAll.setOnClickListener(new OnClickListener() {
 
 			public void onClick(View v) {
 
 				if (n > 0) {
 					for (BDObject object : listaNotas) {
 						NoteBean nb = (NoteBean) object;
 						nb.setFlag(!nb.isFlag());
 						// Log.d("SET FLAG " + nb.getId(), "" + nb.isFlag() );
 					}
 
 					populateTable();
 				}
 			}
 		});
 	}
 
 	/**
 	 * llena la tabla
 	 */
 	private void populateTable() {
 
 		// Log.d("TABLE", "POBLANDO LA TABLA");
 
 		n = notes.count();
 		text_title.setText(title + " [" + n + "]");
 
 		if (n > 0) {
 			// Si la tabla no se ha leido de BD, se recupera
 			if (!tablaCargada) {
 				listaNotas = notes.retrieveN(n);
 			}
 
 			tabla.removeAllViews();
 
 			int i = 0;
 			for (BDObject object : listaNotas) {
 
 				final NoteBean nb = (NoteBean) object;
 
 				TableRow tr = new TableRow(getApplicationContext());
 				TextView tv = new TextView(getApplicationContext());
 				CheckBox cb = new CheckBox(getApplicationContext());
 				tv.setTextColor(getResources().getColor(R.color.black));
 
 				if (i % 2 == 0) {
 					tr.setBackgroundResource(R.color.post_note_tablebackground);
 				}
 
 				tv.setText("[" + nb.getId() + "] > " + nb.getNote());
 
 				cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 
 					public void onCheckedChanged(CompoundButton buttonView,
 							boolean isChecked) {
 						nb.setFlag(isChecked);
 					}
 				});
 				cb.setChecked(nb.isFlag());
 				// Log.d("FLAG " + nb.getId(), "" + nb.isFlag() );
 
 				LayoutParams paramsTV = new TableRow.LayoutParams(
 						LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
 						3f);
 				LayoutParams paramsCB = new TableRow.LayoutParams(
 						LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
 						1f);
 				tv.setLayoutParams(paramsTV);
 				cb.setLayoutParams(paramsCB);
 
 				// AÑADIMOS A LA TABLA
 				tr.addView(tv);
 				tr.addView(cb);
 				tabla.addView(tr);
 				i++;
 
 			}// for
 
 			tablaCargada = true;
 
 			// n no tiene registros
 		} else {
			tabla.removeAllViews();
 			Toast.makeText(getApplicationContext(),
 					getString(R.string.post_note_alert), Toast.LENGTH_SHORT)
 					.show();
 		}
 	}
 
 	/**
 	 * Inicia los listeners para gestures
 	 */
 	private void initiateGestures() {
 
 		// Log.d("DEBUG", "INICIANDO GESTURAS");
 
 		gestureDetector = new GestureDetector(new MyGestureDetector());
 		View postView = (View) findViewById(R.id.postView);
 
 		postView.setOnTouchListener(new View.OnTouchListener() {
 			public boolean onTouch(View v, MotionEvent event) {
 				if (gestureDetector.onTouchEvent(event)) {
 					return true;
 				}
 				return false;
 			}
 		});
 
 		tabla.setOnTouchListener(new View.OnTouchListener() {
 			public boolean onTouch(View v, MotionEvent event) {
 				if (gestureDetector.onTouchEvent(event)) {
 					return true;
 				}
 				return false;
 			}
 		});
 
 	}
 
 	private void initiateInstances() {
 		// Log.d("DEBUG", "INICIANDO COMPONENTES DEL SEGUNDO");
 
 		bdoh = new BDOpenHelper(getApplicationContext());
 		sqldb = bdoh.getWritableDatabase();
 		pbd = new PropertiesBD(getApplicationContext());
 		notes = new NotesCollection(sqldb, pbd);
 
 		tabla = (TableLayout) findViewById(R.id.tabla);
 		text_title = (TextView) findViewById(R.id.postTitle);
 
 		clear = (Button) findViewById(R.id.post_clear);
 		selectAll = (Button) findViewById(R.id.post_selectAll);
 
 		title = text_title.getText().toString();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_view_posts, menu);
 		return true;
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
 
 			// Log.d("FLING", "Iniciando el gesto");
 
 			Intent intent = new Intent(ViewPosts.this.getBaseContext(),
 					Run.class);
 
 			if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH) {
 				return false;
 			}
 
 			// swipe de derecha a izquierda
 			if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
 					&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
 				startActivity(intent);
 				ViewPosts.this.overridePendingTransition(R.anim.slide_in_right,
 						R.anim.slide_out_left);
 				// swipe de izquierda a derecha
 			} else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
 					&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
 				startActivity(intent);
 				ViewPosts.this.overridePendingTransition(R.anim.slide_in_left,
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
 }
