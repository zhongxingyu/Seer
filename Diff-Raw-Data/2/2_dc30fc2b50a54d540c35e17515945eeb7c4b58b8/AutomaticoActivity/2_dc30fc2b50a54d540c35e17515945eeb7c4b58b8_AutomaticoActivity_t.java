 package com.actividades.controlautomovil;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import com.Objetos.Rutina;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListView;
 
 public class AutomaticoActivity extends BluetoothActivity {
 
 	private CarritoManagerApplication cma;
 	private Button playDefault;
	private static final String RUTINA_DEFAULT = "R";
 	private Button agregaRutina;
 	private ListView rutinasLv;
 	private ArrayList<Rutina> rutinas;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.automatico);
 		cma = (CarritoManagerApplication) getApplication();
 		setUpViews();
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		loadList();
 	}
 
 	@Override
 	protected void onStart() {
 		super.onStart();
 		loadList();
 	}
 
 	@Override
 	protected void onRestoreInstanceState(Bundle savedInstanceState) {
 		super.onRestoreInstanceState(savedInstanceState);
 		loadList();
 	}
 
 	@Override
 	protected void onRestart() {
 		super.onRestart();
 		loadList();
 	}
 
 	private void loadList() {
 		rutinas = cma.getRutinas();
 		String[] nombres = new String[rutinas.size()];
 		for (int i = 0; i < rutinas.size(); i++) {
 			nombres[i] = rutinas.get(i).getNombre();
 		}
 		ArrayAdapter aa = new ArrayAdapter(this,
 				android.R.layout.simple_list_item_1, nombres);
 		rutinasLv.setAdapter(aa);
 	}
 
 	private void execRutina(String rutina) {
 		for (int i = 0; i < rutina.length(); i++) {
 			char toSend = rutina.charAt(i);
 			try {
 				cma.getSocket().getOutputStream().write(toSend);
 				Thread.sleep(200);
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 
 	}
 
 	private void setUpViews() {
 		rutinasLv = (ListView) findViewById(R.id.rutinas);
 		rutinasLv.setOnItemClickListener(new OnItemClickListener() {
 
 			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
 					long arg3) {
 				Rutina r = rutinas.get(arg2);
 				String rutina = r.getRutina();
 				execRutina(rutina);
 
 			}
 		});
 
 		playDefault = (Button) findViewById(R.id.ejecutar_rutina_default);
 		playDefault.setOnClickListener(new View.OnClickListener() {
 
 			public void onClick(View v) {
 				execRutina(RUTINA_DEFAULT);
 			}
 		});
 
 		agregaRutina = (Button) findViewById(R.id.add_rutina);
 		agregaRutina.setOnClickListener(new View.OnClickListener() {
 
 			public void onClick(View v) {
 				Intent intent = new Intent(AutomaticoActivity.this,
 						AgregarRutinaActivity.class);
 				startActivity(intent);
 
 			}
 		});
 
 		loadList();
 
 	}
 
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.activity_main, menu);
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.menu_settings:
 			Intent intent = new Intent(this, CreateConnectionActivity.class);
 			startActivity(intent);
 			break;
 
 		case R.id.menu_closeConnection:
 			endConnection();
 			finish();
 			break;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 }
