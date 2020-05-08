 package com.nhpatt.Hello;
 
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.ProgressDialog;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.SimpleCursorAdapter;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.nhpatt.model.Nota;
 import com.nhpatt.util.NotaDataBase;
 import com.nhpatt.util.Preferencias;
 import com.nhpatt.ws.ParseadorXML;
 import com.nhpatt.ws.TraductorGoogle;
 
 public class HelloWorld extends ListActivity implements OnClickListener {
 
 	public static final String VALOR_URL = "VALOR_URL";
 	private static final String APPLICATION_TAG = "nhpattAPP";
 	private SimpleCursorAdapter adapter;
 	private NotaDataBase dataBase;
 	private Cursor cursor;
 	private ComponentName intentService;
 	public static final String VALOR_URL_DEFECTO = "www.lexnova.es";
 
 	@Override
 	public void onCreate(final Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		guardarValorURL();
 
 		final Button button = (Button) findViewById(R.id.incluirNota);
 		button.setOnClickListener(this);
 
 		final Button salir = (Button) findViewById(R.id.salir);
 		salir.setOnClickListener(this);
 
 		dataBase = new NotaDataBase(this);
 		dataBase.open();
 		cursor = dataBase.findAll();
 		startManagingCursor(cursor);
 
 		adapter = new SimpleCursorAdapter(this, R.layout.row, cursor,
 				new String[] { NotaDataBase.DESCRIPCION_COLUMN,
 						NotaDataBase.KEY_CREATION_DATE }, new int[] {
						R.id.bottomText, R.id.topText });
 		setListAdapter(adapter);
 
 		ListView lista = (ListView) findViewById(android.R.id.list);
 		registerForContextMenu(lista);
 
 		Log.d(APPLICATION_TAG, "Creating activity...");
 	}
 
 	private void guardarValorURL() {
 		SharedPreferences preferences = PreferenceManager
 				.getDefaultSharedPreferences(getApplicationContext());
 		SharedPreferences.Editor editor = preferences.edit();
 		editor.putString(VALOR_URL, "www.google.es");
 		editor.commit();
 	}
 
 	@Override
 	public void onClick(final View v) {
 		switch (v.getId()) {
 		case R.id.incluirNota:
 			final TextView text = (TextView) findViewById(R.id.textoNota);
 			Nota nota = new Nota(text.getText().toString());
 			dataBase.insertar(nota);
 			cursor.requery();
 			Toast.makeText(this, "Aadida la nota: " + nota, Toast.LENGTH_LONG)
 					.show();
 			text.setText("");
 			break;
 		case R.id.salir:
 			finish();
 		default:
 			break;
 		}
 	}
 
 	@Override
 	public void onListItemClick(ListView listView, View view, int position,
 			long id) {
 		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
 		dialog.setTitle("Descripcin de nota");
 
 		dialog.setMessage(cursor.getString(cursor
 				.getColumnIndex(NotaDataBase.DESCRIPCION_COLUMN)));
 		dialog.setNegativeButton(android.R.string.cancel,
 				new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 					}
 				});
 		dialog.show();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 		menu.add(0, Menu.FIRST, 0, "Sobre Lex Nova");
 		menu.add(0, (Menu.FIRST) + 1, 0, "Preferencias");
 		menu.add(0, (Menu.FIRST) + 2, 0, "XML");
 		menu.add(0, (Menu.FIRST) + 3, 0, "Browser");
 		menu.add(0, (Menu.FIRST) + 4, 0, "Notificaciones");
 		menu.add(0, (Menu.FIRST) + 5, 0, "Servicio 1");
 		menu.add(0, (Menu.FIRST) + 6, 0, "Parar Servicio 1");
 		menu.add(0, (Menu.FIRST) + 7, 0, "Servicio 2");
 		menu.add(0, (Menu.FIRST) + 8, 0, "Parar Servicio 2");
 		menu.add(0, (Menu.FIRST) + 9, 0, "Content Provider");
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		super.onOptionsItemSelected(item);
 		Intent intent;
 		switch (item.getItemId()) {
 		case Menu.FIRST:
 			intent = new Intent(Intent.ACTION_VIEW,
 					Uri.parse("http://lexnova.es"));
 			startActivity(intent);
 			return true;
 		case Menu.FIRST + 1:
 			intent = new Intent(this, Preferencias.class);
 			startActivity(intent);
 			return true;
 		case Menu.FIRST + 2:
 			ParseadorXML parseadorXML = new ParseadorXML();
 			parseadorXML.recogerValores();
 			return true;
 		case Menu.FIRST + 3:
 			intent = new Intent(this, com.nhpatt.util.Browser.class);
 			startActivity(intent);
 			return true;
 		case Menu.FIRST + 4:
 			Notification notification = new Notification(R.drawable.icon,
 					"Notification corta", System.currentTimeMillis());
 			intent = new Intent(this, Preferencias.class);
 			PendingIntent launchIntent = PendingIntent.getActivity(
 					getApplicationContext(), 0, intent, 0);
 			notification.flags = notification.flags
 					| Notification.FLAG_ONGOING_EVENT
 					| Notification.DEFAULT_VIBRATE;
 
 			notification.setLatestEventInfo(getApplicationContext(), "Titulo",
 					"Texto largo", launchIntent);
 
 			NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
 			notificationManager.notify(1, notification);
 
 			return true;
 		case Menu.FIRST + 5:
 			service = startService(new Intent(this, Servicio.class));
 			return true;
 		case Menu.FIRST + 6:
 			stopService(new Intent(this, service.getClass()));
 			return true;
 		case Menu.FIRST + 7:
 			intentService = startService(new Intent(this, IntentServicio.class));
 			return true;
 		case Menu.FIRST + 8:
 			stopService(new Intent(this, intentService.getClass()));
 			return true;
 		case Menu.FIRST + 9:
 			Cursor allRows = getContentResolver().query(MyProvider.CONTENT_URI,
 					null, null, null, null);
 			Toast.makeText(this, String.valueOf(allRows.getCount()),
 					Toast.LENGTH_LONG).show();
 			return true;
 		}
 		return false;
 	}
 
 	private ProgressDialog dialog;
 	private ComponentName service;
 
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v,
 			ContextMenu.ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 		menu.add(0, Menu.FIRST, 0, "Eliminar");
 		menu.add(0, Menu.FIRST + 1, 0, "Traducir");
 		menu.add(0, Menu.FIRST + 2, 0, "Procesar");
 	}
 
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		AdapterContextMenuInfo info;
 		Cursor cursor;
 		switch (item.getItemId()) {
 		case Menu.FIRST:
 			info = (AdapterContextMenuInfo) item.getMenuInfo();
 			cursor = (Cursor) getListAdapter().getItem(info.position);
 			dataBase.eliminarNota(cursor.getInt(cursor
 					.getColumnIndex(NotaDataBase.KEY_ID)));
 			cursor.requery();
 			Toast.makeText(
 					this,
 					cursor.getString(cursor
 							.getColumnIndex(NotaDataBase.DESCRIPCION_COLUMN)),
 					Toast.LENGTH_SHORT).show();
 			break;
 		case Menu.FIRST + 1:
 			info = (AdapterContextMenuInfo) item.getMenuInfo();
 			cursor = (Cursor) getListAdapter().getItem(info.position);
 			Toast.makeText(
 					this,
 					TraductorGoogle.traducir(cursor.getString(cursor
 							.getColumnIndex(NotaDataBase.DESCRIPCION_COLUMN)),
 							"ES", "en"), Toast.LENGTH_SHORT).show();
 			break;
 		case Menu.FIRST + 2:
 			crearDialogoProgreso();
 			new TareaAsincrona().execute("algo");
 			break;
 		default:
 			break;
 		}
 		return false;
 	}
 
 	private void crearDialogoProgreso() {
 		dialog = new ProgressDialog(this);
 		dialog.setMessage("Descargando...");
 		dialog.setTitle("Progreso");
 		dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
 	}
 
 	private class TareaAsincrona extends AsyncTask<String, Float, Integer> {
 
 		@Override
 		protected void onPreExecute() {
 			dialog.setProgress(0);
 			dialog.setMax(100);
 			dialog.show();
 		}
 
 		@Override
 		protected Integer doInBackground(String... urls) {
 			for (int i = 0; i < 100; i++) {
 				try {
 					Thread.sleep(200);
 				} catch (InterruptedException e) {
 				}
 
 				publishProgress(i / 100f);
 			}
 			return 100;
 		}
 
 		@Override
 		protected void onProgressUpdate(Float... valores) {
 			int p = Math.round(100 * valores[0]);
 			dialog.setProgress(p);
 		}
 
 		@Override
 		protected void onPostExecute(Integer bytes) {
 			dialog.dismiss();
 		}
 	}
 
 }
