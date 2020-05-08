 package com.example.boox;
 
 import internet.ListaServer;
 
 import java.util.ArrayList;
 
 import listasLibros.GestorListas;
 import listasLibros.Libro;
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.support.v4.app.FragmentTransaction;
 import android.support.v4.app.ListFragment;
 import android.util.Log;
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
 
 public class BookListFragment extends ListFragment {
 
     boolean mDualPane;
     int mCurCheckPosition = 0; 
     
     AdapterView.AdapterContextMenuInfo info;
     GestorListas gl;
     final int mode = Activity.MODE_PRIVATE;
 	public static final String myPrefs = "prefs";
 	String uname = "";
 	ArrayList<Libro> books = new ArrayList<Libro>();;
 	String lista;
 	public ArrayList<String> booklist = new ArrayList<String>();
 	
 	
 	@Override
     public void onActivityCreated(Bundle savedState) {
         super.onActivityCreated(savedState);
                
         SharedPreferences mySharedPreferences = this.getActivity().getSharedPreferences(myPrefs, mode);
 		uname = mySharedPreferences.getString("username", "");
         gl = new GestorListas(uname, this.getActivity()); 
         Bundle extras = this.getActivity().getIntent().getExtras();
 
         int f = extras.getInt("all");
         lista = extras.getString("lista");
         if(f == 1)
         	books = gl.getListaAll();
         else
         	if(f == 2){
         		Libro book = new Libro();
         		ListaServer ls = new ListaServer();
         		InterfazAPI api = new InterfazAPI();
         		ArrayList<String> compartibles = new ArrayList<String>();
         		compartibles = ls.obtenerLibrosLista(uname, "adminLibrosPorUsuario");
 
         		for(int i=0; i< compartibles.size(); i++){
         			book = api.ObtenerLibroPorId(compartibles.get(i));
         			if(book != null){
         				books.add(book);
         			}
         		}
         	}
         	else
         		books = gl.GetListaDeLibros(lista);
 		
         // Populate list with our static array of titles.
 		for(int i=0; i<books.size(); ++i)
 			booklist.add(books.get(i).getTitulo());	
 		
         setListAdapter(new ArrayAdapter<String>(getActivity(),
                 android.R.layout.simple_list_item_1,
                 booklist));
 
         // Check to see if we have a frame in which to embed the details
         // fragment directly in the containing UI.
         View detallesLibro = getActivity().findViewById(R.id.details);
         mDualPane = detallesLibro != null && detallesLibro.getVisibility() == View.VISIBLE;
     	Log.d("BookList", "onActivityCreated -> mDualPane = " + (mDualPane ? "true" : "false"));
 
         if(savedState != null){
         	Log.d("BookList", "savedState = null");
             // Restore last state for checked position.
             mCurCheckPosition = savedState.getInt("curChoice", 0);
         }
         if(mDualPane){
         	Log.d("BookList", "mDualPane = true");
             // In dual-pane mode, list view highlights selected item.
             getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
             // Make sure our UI is in the correct state.
             showDetails(mCurCheckPosition);
         }
         this.getActivity().getActionBar().setTitle(lista);
         registerForContextMenu(getListView());
     }
 
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 
 		info = (AdapterContextMenuInfo) menuInfo;
 		menu.setHeaderTitle("Crossing options"); 
 		menu.add(Menu.NONE, info.position, 0, "View");
 		menu.add(Menu.NONE, info.position, 0, "Delete");
 	}
 
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		if(item.getTitle() == "View"){
            MyBD mbd = new MyBD( this.getActivity(),uname);
            mbd.BorrarTemporal();
			mbd.InsertarTemporal(books.get(item.getItemId()));
             Intent intent = new Intent();
             intent.setClass(getActivity(), BookActivity.class);
             intent.putExtra("id", books.get(item.getItemId()).getId());
             intent.putExtra("temp", 0);
             startActivity(intent);	
             
 		}else if(item.getTitle() == "Delete"){
 			if(lista.equals("All")){
 				Toast toast = Toast.makeText(this.getActivity().getApplicationContext(),getResources().getString(R.string.remove_from_all), Toast.LENGTH_SHORT);
 				toast.show();
 				return true;
 			}
 			else{
 			gl.BorraLibroDeLista(books.get(item.getItemId()).getId(), lista);
 			
 			Toast toast = Toast.makeText(this.getActivity().getApplicationContext(),getResources().getString(R.string.book_deleted),Toast.LENGTH_SHORT);
 					
 			toast.show();
 			booklist.remove(item.getItemId());
 	        setListAdapter(new ArrayAdapter<String>(getActivity(),
 	                android.R.layout.simple_list_item_1,
 	                booklist));
 			}	
 		return true;
 		}
 		return true;
 	}
 
 	
     @Override
     public void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
         outState.putInt("curChoice", mCurCheckPosition);
     }
     
     @Override
     public void onListItemClick(ListView l, View v, int pos, long id) {
     	Log.d("BookList", "onListItemClick(" + pos + ")");
         showDetails(pos);
     }
 
 	/*@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
 		for(int i=0; i<strings.length; ++i)
 			list.add(strings[i]);	
 		ArrayAdapter<String> mAdapter = 
     			new ArrayAdapter<String>(getActivity().getBaseContext(), 
     									 android.R.layout.simple_list_item_1, 
     									 list);
 		setListAdapter(mAdapter);
     	return super.onCreateView(inflater, container, savedInstanceState);
 	}*/
     
     /**
      * Helper function to show the details of a selected item, either by
      * displaying a fragment in-place in the current UI, or starting a
      * whole new activity in which it is displayed.
      */
     void showDetails(int index) {
     	
         mCurCheckPosition = index;
 
         if(mDualPane){
         	Log.d("BookList", "showDetails(DualPane)");
             // We can display everything in-place with fragments.
             // Have the list highlight this item and show the data.
             getListView().setItemChecked(index, true);
 
             // Check what fragment is shown, replace if needed.
             BookFragment bookDetails = (BookFragment) getFragmentManager().findFragmentById(R.id.details);
             if (bookDetails == null || bookDetails.getShownIndex() != index) {
                 // Make new fragment to show this selection.
                 bookDetails = BookFragment.newInstance(index);
 
                 // Execute a transaction, replacing any existing
                 // fragment with this one inside the frame.
                 FragmentTransaction ft = getFragmentManager().beginTransaction();
                 ft.replace(R.id.details, bookDetails);
                 ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                 ft.commit();
             }
         } else {
         	Log.d("BookList", "showDetails(MonoPane)");
             // Otherwise we need to launch a new activity to display
             // the dialog fragment with selected text.
             Intent intent = new Intent();
             MyBD mbd = new MyBD( this.getActivity(),uname);
             mbd.BorrarTemporal();
 			mbd.InsertarTemporal(books.get(index));
             intent.setClass(getActivity(), BookActivity.class);
             intent.putExtra("id", books.get(index).getId());
             intent.putExtra("temp", 0);
 
             startActivity(intent);
         }
     }
 }
