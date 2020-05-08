 package com.example.boox;
 
 import java.util.ArrayList;
 
 import listasLibros.GestorListas;
 import listasLibros.Libro;
 
 import com.example.boox.R;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.view.ContextMenu;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.Toast;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import apiGoogle.InterfazAPI;
 
 public class SearchBookResultActivity extends ListActivity {
 	final int mode = Activity.MODE_PRIVATE;
 	public static final String myPrefs = "prefs";
 
 	ArrayList<String> misListas;
 	String uname;
     AdapterView.AdapterContextMenuInfo info;
     GestorListas gl;
 	ArrayList<Libro> libros = new ArrayList<Libro>();
 	Libro libro = new Libro();
 	int modo;
 	String cont;
 	Context context = this;
 	boolean found = true;
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		SharedPreferences mySharedPreferences = this.getSharedPreferences(myPrefs, mode);
 				
 		uname = mySharedPreferences.getString("username", "");
 		gl = new GestorListas(uname, SearchBookResultActivity.this);
 		
 		Bundle extras = getIntent().getExtras();
 		modo = extras.getInt("modo");
 		cont = extras.getString("contenido");
 		
 		misListas = gl.getNombresListas();
 		InterfazAPI api = new InterfazAPI();
 		ArrayList<String> titulo = new ArrayList<String>();
 		ArrayList<String> titulos = new ArrayList<String>();
 		if(modo == 0){
 			libros = api.ObtenerListaLibrosPorTitulo(cont);
 			if(libros != null){
 				for(int i=0; i < libros.size(); i++)
 					titulos.add(libros.get(i).getTitulo());
 			}
 			else{
 				Toast toast = Toast.makeText(this.getApplicationContext(),getResources().getString(R.string.not_found), Toast.LENGTH_SHORT);
 				toast.show();
 				finish();
 			}
 		}
 		else{
 			if(modo == 1){
 				libros = api.ObtenerListaLibrosPorAutor(cont);
 				if(libros != null){
 					for(int i=0; i < libros.size(); i++)
 						titulos.add(libros.get(i).getTitulo());
 				}
 				else{
 					Toast toast = Toast.makeText(this.getApplicationContext(),getResources().getString(R.string.not_found), Toast.LENGTH_SHORT);
 					toast.show();
 					finish();
 					}
 			}
 			else{
 				if(modo == 2){
 					libro = api.ObtenerLibroPorIsbn(cont);
 					if(libro != null){
 						titulos.add(libro.getTitulo());
 					}
 					else{
 						Toast toast = Toast.makeText(this.getApplicationContext(),getResources().getString(R.string.not_found), Toast.LENGTH_SHORT);
 						toast.show();
 						finish();						
 					}
 				}
 			}
 		}
 		setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, titulos));			
         registerForContextMenu(getListView());
 	}
 
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 		ArrayList<String> misListas = gl.getNombresListas();
 		info = (AdapterContextMenuInfo) menuInfo;
 		menu.setHeaderTitle("Add to:"); 
		menu.add(Menu.NONE, info.position, 0, "Crossing List");
 		if(misListas!=null){
 			for(int i=0; i<misListas.size(); i++)
 				menu.add(Menu.NONE, info.position, 0, misListas.get(i));
 		}
 		else
 			menu.add(Menu.NONE, info.position, 0, "");
 	}
 
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		if(item.getTitle() != null && misListas != null &&
 				found){
 			if(modo != 2){
 				if(item.getTitle().toString().equals("Crossing List")){
 					gl.AddLibroEnCompartibles(libros.get(item.getItemId()).getId());
 					Toast toast = Toast.makeText(this.getApplicationContext(),libros.get(item.getItemId()).getTitulo()+" added to Crossing", Toast.LENGTH_SHORT);
 					toast.show();
 				}
 				else{
 				gl.AddLibroEnLista(libros.get(item.getItemId()), item.getTitle().toString());
				Toast toast = Toast.makeText(this.getApplicationContext(), libros.get(item.getItemId()).getTitulo()+" Added", Toast.LENGTH_SHORT);
 				toast.show();
 				}
 			}else{
 				if(item.getTitle().toString().equals("Crossing List")){	
 					//ADD A CROSSING
 					Toast toast = Toast.makeText(this.getApplicationContext(),libros.get(item.getItemId()).getTitulo()+"added to Crossing", Toast.LENGTH_SHORT);
 					toast.show();
 				}
 				else{
 				gl.AddLibroEnLista(libro, item.getTitle().toString());
 				Toast toast = Toast.makeText(this.getApplicationContext(),libros.get(item.getItemId()).getTitulo()+" added to "+item.getTitle().toString(), Toast.LENGTH_SHORT);
 				toast.show();
 			}
 		}
 	}
 		return true;
 	}
 
 	
 	
 	
 	
 	@Override
 	protected void onListItemClick(ListView list, View view, int position, long id){
 		super.onListItemClick(list, view, position, id);
 		if(!list.getItemAtPosition(0).equals(getResources().getString(R.string.not_found))){
 			MyBD mbd = new MyBD(this,uname);
 			Intent i = new Intent();
 			i.setClass(context, BookActivity.class);
 			if(modo == 2){
 				mbd.BorrarTemporal();
 				mbd.InsertarTemporal(libro);
 				i.putExtra("id", libro.getId());
 			}
 			else{
 				mbd.BorrarTemporal();
 				mbd.InsertarTemporal(libros.get(position));
 				i.putExtra("id", libros.get(position).getId());
 			}
 			i.putExtra("temp", 1);
 			startActivity(i);
 		}
 	}
 		
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 
 		getMenuInflater().inflate(R.menu.activity_search_book_result, menu);
 		return true;
 	}
     
     @Override
 	public boolean onOptionsItemSelected(MenuItem item) {
     	switch (item.getItemId()) {
     	case R.id.search:
     		startActivity(new Intent(this, SearchBookActivity.class));
             return true;
     	case R.id.submenu_settings:
     		startActivity(new Intent(this, SettingsActivity.class));
         	return true;
     	case R.id.submenu_about:
     		startActivity(new Intent(this, AboutActivity.class));
         	return true;
     	}
     	return false;
 	}
 }
