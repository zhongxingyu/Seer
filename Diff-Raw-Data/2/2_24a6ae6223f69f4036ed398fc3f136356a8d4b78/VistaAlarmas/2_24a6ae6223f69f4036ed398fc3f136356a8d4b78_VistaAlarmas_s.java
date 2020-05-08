 package com.arpia49;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.CheckBox;
 import android.widget.LinearLayout;
 import android.widget.Toast;
 
 public class VistaAlarmas extends Activity {
 
 	public static final String PREFS_NAME = "PrefTimbre";
 	public static final int ACT_ADD_ALARMA = 1;
 	public static final int ACT_DEL_ALARMA = 2;
 	static final private int ADD_ALARMA = Menu.FIRST;
 	static final private int DEL_ALARMA = Menu.FIRST + 1;
 	static final private int INFO = Menu.FIRST + 2;
 	int usando;
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		// meterBasuras();
 		setContentView(R.layout.main);
 		cargarPosiciones((LinearLayout) findViewById(R.id.mainLay));
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 		// Create and add new menu items.
 		MenuItem itemAdd = menu.add(0, ADD_ALARMA, Menu.NONE,
 				R.string.menu_add_alarma);
 		MenuItem itemDel = menu.add(0, DEL_ALARMA, Menu.NONE,
 				R.string.menu_del_alarma);
 		MenuItem itemInfo = menu.add(0, INFO, Menu.NONE, R.string.menu_info);
 		// Assign icons
 		itemAdd.setIcon(R.drawable.add);
 		itemDel.setIcon(R.drawable.del);
 		itemInfo.setIcon(R.drawable.help);
 		return true;
 	}
 
 	public boolean onOptionsItemSelected(MenuItem item) {
 		super.onOptionsItemSelected(item);
 		switch (item.getItemId()) {
 		case (ADD_ALARMA): {
 			Intent intent = new Intent(this, AddAlarma.class);
 			startActivityForResult(intent, ACT_ADD_ALARMA);
 			return true;
 		}
 		case (DEL_ALARMA): {
 			Intent intent = new Intent(this, DelAlarma.class);
 			startActivityForResult(intent, ACT_DEL_ALARMA);
 			return true;
 		}
 		case (INFO): {
 			Toast.makeText(getApplicationContext(),
 					"Mostrando ayuda! (mentira)", Toast.LENGTH_SHORT).show();
 			return true;
 		}
 		}
 		return false;
 	}
 
 	@Override
 	public void onActivityResult(int reqCode, int resCode, Intent data) {
 		super.onActivityResult(reqCode, resCode, data);
 		switch (reqCode) {
 		case (ACT_ADD_ALARMA): {
 			if (resCode == Activity.RESULT_OK) {
 				crearAlarma(data.getStringExtra("nombreAlarma"));
 				Toast.makeText(getApplicationContext(), "Alarma añadida!",
 						Toast.LENGTH_SHORT).show();
 			} else {
 				Toast.makeText(getApplicationContext(),
 						"La alarma no se ha creado", Toast.LENGTH_SHORT).show();
 			}
 		}
 			break;
 		case (ACT_DEL_ALARMA): {
 			if (resCode == Activity.RESULT_OK) {
 				delAlarma(Integer.parseInt(data.getStringExtra("idAlarma")) + 1);
 				Toast.makeText(getApplicationContext(), "Alarma  eliminada!",
 						Toast.LENGTH_SHORT).show();
 			} else {
 				Toast.makeText(getApplicationContext(),
 						"No se han borrado alarmas", Toast.LENGTH_SHORT).show();
 			}
 		}
 			break;
 		}
 	}
 
 	private void cargarPosiciones(LinearLayout lx) {
 
 		// Leemos el número de alarmas
 		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
 		int numAlarmas = settings.getInt("numAlarmas", 0);
 		for (int i = 1; i <= numAlarmas; i++) {
 			addAlarma(i, settings.getBoolean("estadoAlarma" + i, false),
 					settings.getBoolean("configuradaAlarma" + i, false),
 					settings.getString("nombreAlarma" + i, "sin nombre"));
 		}
 	}
 
 	private void crearAlarma(String nombre) {
 
 		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
 		SharedPreferences.Editor editor = settings.edit();
 		int numAlarmas = settings.getInt("numAlarmas", 0) + 1;
 
 		// Añadimos los datos de la alarma al registro
 		editor.putInt("numAlarmas", numAlarmas);
 		editor.putString("nombreAlarma" + numAlarmas, nombre);
 		editor.putBoolean("estadoAlarma" + numAlarmas, false);
 		editor.putBoolean("configuradaAlarma" + numAlarmas, false);
 		editor.commit();
 
 		addAlarma(numAlarmas, false, false, nombre);
 
 	}
 
 	private void addAlarma(int id, boolean estado, boolean configurada,
 			String nombre) {
 
 		LinearLayout lx = (LinearLayout) findViewById(R.id.mainLay);
 
 		// Creamos la alarma en la vista
 		CheckBox cb = new CheckBox(this);
 		cb.setId(id);
 		if (!configurada)
 			nombre += " (sin configurar)";
 		cb.setText(nombre);
 		cb.setChecked(estado);
 		
 		cb.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
 				SharedPreferences.Editor editor = settings.edit();
 				if (((CheckBox) v).isChecked()) {
 					editor.putBoolean("estadoAlarma" + v.getId(), true);
 					editor.commit();
 				} else {
 					editor.putBoolean("estadoAlarma" + v.getId(), false);
 					editor.commit();
 				}
 			}
 		});
 		lx.addView(cb);
 
 	}
 
 	private void delAlarma(int id) {
 
 		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
 		SharedPreferences.Editor editor = settings.edit();
 		int numAlarmas = settings.getInt("numAlarmas", 0);
 		CheckBox cb = (CheckBox) findViewById(id);
 		cb.setId(numAlarmas + 1);
 		editor.putBoolean("estadoAlarma" + numAlarmas + 1, settings.getBoolean(
 				"estadoAlarma" + id, false));
 		editor.putBoolean("configuradaAlarma" + numAlarmas + 1, settings.getBoolean(
 				"configuradaAlarma" + id, false));
 		editor.putString("nombreAlarma" + numAlarmas + 1, settings.getString(
 				"nombreAlarma" + id, "sin_nombre"));
 		for (int i = id; i < numAlarmas; i++) {
 			editor.putBoolean("estadoAlarma" + i, settings.getBoolean(
 					"estadoAlarma" + (i+1), false));
 			editor.putBoolean("configuradaAlarma" + i, settings.getBoolean(
 					"configuradaAlarma" + (i+1), false));
 			editor.putString("nombreAlarma" + i, settings.getString(
 					"nombreAlarma" + (i+1), "sin_nombre"));
 		}
 		editor.putInt("numAlarmas", numAlarmas - 1);
 		editor.commit();
 		LinearLayout lx = (LinearLayout) findViewById(R.id.mainLay);
 		lx.removeView(cb);
 	}
 }
