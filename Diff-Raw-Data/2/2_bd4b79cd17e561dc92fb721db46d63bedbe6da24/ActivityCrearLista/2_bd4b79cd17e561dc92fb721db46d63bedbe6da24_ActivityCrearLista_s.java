 package com.example.obligatorio.ui;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.WindowManager;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.EditText;
 import android.widget.Toast;
 
 import com.example.obligatorio.adapters.ProductosAdaptador;
 import com.example.obligatorio.base_de_datos.BaseDeDatos;
 import com.example.obligatorio.dominio.Producto;
 import com.example.obligatorio.servicio.ListaPedido;
 import com.example.obligatorio.servicio.ListaPedido.ProductoCantidad;
 import com.example.obligatorio.servicio.WebServiceInteraction;
 import com.example.obligatorio.sistema.Sistema;
 import com.example.obligatorio.widget.IndexableListView;
 
 public class ActivityCrearLista extends Activity {
 
 	private static final int MENU_TERMINAR = Menu.FIRST;
 	private static final int MENU_VERLISTA = Menu.FIRST + 1;
 	ListaPedido lp;
 	private ArrayList<Producto> productos = new ArrayList<Producto>();
 	public ProgressDialog dialog;
 
 	public Handler responseHandler;
 	public ProductosAdaptador adaptador;
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_productos);
 		dialog = new ProgressDialog(this);
 		lp = new ListaPedido();
 		responseHandler = new Handler();
 
 		BaseDeDatos base = Sistema.getInstance().getBaseDeDatos();
 		productos = (ArrayList<Producto>) base.getAllProducts();
 
 		adaptador = new ProductosAdaptador(this, productos);
 
 		final IndexableListView lstOpciones = (IndexableListView) findViewById(R.id.listView1);
 
 		lstOpciones.setAdapter(adaptador);
 		lstOpciones.setFastScrollEnabled(true);
 
 		lstOpciones.setOnItemClickListener(new OnItemClickListener() {
 
 			public void onItemClick(AdapterView<?> arg0, View rowView,
 					int index, long arg3) {
 
 				if (productos.get(index).isEnListaActual()) {
 					productos.get(index).setEnListaActual(false);
 
 					Sistema.getInstance().getItemsChecked()
 							.remove((Integer) index);
 					lp.eliminarProducto(productos.get(index));
 				} else {
 					lp.getProductos().add(
 							new ProductoCantidad(productos.get(index), 1));
 					productos.get(index).setEnListaActual(true);
 					Sistema.getInstance().getItemsChecked()
 							.add((Integer) index);
 
 					Toast.makeText(rowView.getContext(), "Agregado",
 							Toast.LENGTH_SHORT).show();
 
 					// http://www.coderzheaven.com/2012/09/12/create-slide-left-animation-deleting-row-listview-android/
 					Animation animation = AnimationUtils.loadAnimation(
 							ActivityCrearLista.this,
 							android.R.anim.slide_out_right);
 					animation.setDuration(2500);
 					rowView.startAnimation(animation);
 				}
 				adaptador.notifyDataSetChanged();
 			}
 		});
 
 		EditText etFiltro = (EditText) this.findViewById(R.id.producto);
 		etFiltro.addTextChangedListener(new TextWatcher() {
 			// cuando cambia el // texot del // edittext se // ejecuta y filtro
 
 			public void onTextChanged(CharSequence s, int start, int before,
 					int count) {
 				// TODO Auto-generated method stub
 				lstOpciones.setTextFilterEnabled(true);
 
 				adaptador.FiltrarProductos(s);
 				// adaptador.getFilter().filter(s);
 
 			}
 
 			public void beforeTextChanged(CharSequence s, int start, int count,
 					int after) {
 				// TODO Auto-generated method stub
 
 			}
 
 			public void afterTextChanged(Editable s) {
 				// TODO Auto-generated method stub
 				if (s.length() == 0) {
 					lstOpciones.setTextFilterEnabled(false);
 
 				}
 			}
 		});
 		// con esta linea le cerramos el teclado cuando abre la aplicacion, de
 		// forma que solo se muetre cuando toca la pantalla en el editText
 		getWindow().setSoftInputMode(
 				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		menu.add(0, MENU_VERLISTA, 0, "Ver Lista");
 		menu.add(0, MENU_TERMINAR, 0, "Calcular");
 		return true;
 	}
 
 	/* Handles item selections */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		Sistema.getInstance().setListaPedActual(lp);
 		switch (item.getItemId()) {
 		case MENU_VERLISTA:
 			Intent abrir = new Intent(this, ActivityListaActual.class);
 			startActivity(abrir);
 			// http://www.bogotobogo.com/Android/android10Menus.php
 			return true;
 		case MENU_TERMINAR:
 			if(lp.getProductos().size()==0){
 				Toast.makeText(this, "No tiene productos seleccionados en la lista", Toast.LENGTH_LONG).show();
 			}else{
 				final Intent in = new Intent(this, ActivityResultado.class);
 				dialog.setMessage("Se estan bucando los datos...");
 				dialog.setTitle("Procesando");
 				dialog.setCancelable(false);
 				dialog.show();
 				final Thread thread = new Thread(new Runnable() {
 					public void run() {
 						WebServiceInteraction.buscarResultadosListaActual();
 						dialog.dismiss();
 						startActivity(in);
 					}
 				});
 
 				thread.start();
 			}
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		// cuando giro guardo todo
 		super.onSaveInstanceState(outState);
 		Sistema.getInstance().setListaPedActual(lp);
 		
 		Sistema.getInstance().setYaGiro(true);
 	}
 
 	@Override
 	protected void onRestoreInstanceState(Bundle savedInstanceState) {
 		// cuando "termina de girar" restablesco todo
 		super.onRestoreInstanceState(savedInstanceState);
 
 		lp = Sistema.getInstance().getListaPedActual();
 		for (Producto pro : productos) {
 			pro.setEnListaActual(false);
 		}
 		for (Integer indexxx : Sistema.getInstance().getItemsChecked()) {
 			productos.get(indexxx).setEnListaActual(true);
 		}
 		adaptador.notifyDataSetChanged();
 		
 		Sistema.getInstance().setYaGiro(false);
 		
 	}
 
 	@Override
 	public void onResume() { // After a pause OR at startup
 		super.onResume();
 		if (Sistema.getInstance().getItemsChecked() != null) {
 			for (Producto pro : productos) {
 				pro.setEnListaActual(false);
 			}
 			for (Integer indexxx : Sistema.getInstance().getItemsChecked()) {
 				productos.get(indexxx).setEnListaActual(true);
 			}
 		}
 
 		adaptador.notifyDataSetChanged();
 
 	}
 	@Override
 	public void onDestroy(){
 		super.onDestroy();
		if(Sistema.getInstance().getYaGiro()!=null & !Sistema.getInstance().getYaGiro()){
 			Sistema.getInstance().setItemsChecked(new ArrayList<Integer>());
 			Sistema.getInstance().setListaPedActual(new ListaPedido());
 			Sistema.getInstance().setYaGiro(null);
 		}
 	}
 
 }
