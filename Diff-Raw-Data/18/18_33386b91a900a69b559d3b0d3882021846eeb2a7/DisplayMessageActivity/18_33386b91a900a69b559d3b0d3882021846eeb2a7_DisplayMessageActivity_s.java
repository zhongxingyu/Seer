 /*
 * Copyright (C) 2013 Juan Pons (see README for details)
 * This file is part of miAulaVirtual.
 *
 * miAulaVirtual is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * miAulaVirtual is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with miAulaVirtual. If not, see <http://www.gnu.org/licenses/agpl.txt>.
 *
 */
 
 package com.jp.miaulavirtual;
 
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.ServiceConnection;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.preference.PreferenceManager;
 import android.support.v4.app.NavUtils;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 
 public class DisplayMessageActivity extends Activity {
 
     // flag for Internet connection status
     Boolean isInternetPresent = false;
     // Connection detector class
     ConnectionDetector cd;
 	
 	// Service
 	CurlService cURL; //Servicio
 	Intent i;
 	
 	// Activity
 	Context mycontext;
 	
 	String tag = "Lifecycle2";
 	
 	// User data
 	String user;
 	String pass;
 	
 	// Valores del HOME de documentos
 	Document doc;
 	Elements elements;
 	String fServ; //Respuesta primera (home)
 	ArrayList<Object[]> first = new ArrayList<Object[]>(); // Primera respuesta (home)
 	
 	//Nombres URL's y Types por las que se pasa y Boolean necesarios
 	ArrayList<String> onUrl = new ArrayList<String>();
 	ArrayList<String> onName = new ArrayList<String>();
 	Boolean comunidades = false; // URL principal documentos y comunidades = true -> Carpeta Comunidades url /clubs/; Sino carpeta Principal, url-> /classes/ y otras
 	Boolean isDocument = false; // Maneja si la URL es de tipo Documento (cuando type!= (0, 1)) o tipo URL ya que debemos hacer tareas diferentes para cada uno.
 	int docPosition; // Necesitamos saber la posición del archivo clickeado para poder mandar la URL desde fuera del Listener
 	ProgressDialog dialog;
 	
 	// Interface
 	ListView lstDocs;
 	AdaptadorDocs lstAdapter;
 	
 	private DataUpdateReceiver dataUpdateReceiver;
 	
 	//BroadcastReceiver, recibe variables de nuestro servicio posteriormente ejecutado CurlService
 	private class DataUpdateReceiver extends BroadcastReceiver {
 	    @Override
 	    public void onReceive(Context context, Intent intent) {
 	        if (intent.getAction().equals(CurlService.RESPONSE)) {
 	        		fServ = intent.getStringExtra("response");
 	        		if(fServ.equals("cont")) {
 	        			dialog.dismiss();
 	        			String size =  intent.getStringExtra("data");
 	        			isDocument = false;
	        			Toast.makeText(getBaseContext(),getString(R.string.cont_1)+""+size+""+getString(R.string.cont_2), Toast.LENGTH_SHORT).show();
 	        		} else if(fServ.equals("oops")) {
 	        			dialog.dismiss();
 	        			isDocument = false;
 	        			String msg =  intent.getStringExtra("data");
 	        			Toast.makeText(getBaseContext(), msg, Toast.LENGTH_LONG).show();
 	        		} else {
 		        		afterBroadcaster(fServ);
 		        		Toast.makeText(getBaseContext(),"GET hecho!", Toast.LENGTH_SHORT).show();
 	        		}
 	        		stopService(i);
 	        		unbindService(CurlConnection);
 	        }
 	    }
 	}
 	
 	@SuppressLint("NewApi")
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_display_message);
 		// Make sure we're running on Honeycomb or higher to use ActionBar APIs
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
             // Show the Up button in the action bar.
             getActionBar().setDisplayHomeAsUpEnabled(true);
         }
 
         Log.d(tag, "In the onCreate() event");
         
         // creating connection detector class instance
         cd = new ConnectionDetector(getApplicationContext());
         
         // Datos por defecto de la primera respuesta
         mycontext = this;
         onUrl.add("/dotlrn/?page_num=2");
         onName.add("Documentos");
         
         // Actualizamos nombre de LblSubTitulo
         final TextView headerTitle;
         headerTitle = (TextView) findViewById(R.id.LblSubTitulo); // T�tulo Header
         headerTitle.setTextColor(getResources().getColor(R.color.list_title));
         headerTitle.setTypeface(null, 1);
         headerTitle.setText(onName.get(onName.size() - 1));
         
         //Recibimos primera llamada al crear la Actividad
         Intent intent = getIntent();
         
 	    // Datos de usuario
 	    user = intent.getStringExtra("user");
 	    pass = intent.getStringExtra("pass");
 	    
 	    // Recibimos datos HOME
         fServ = intent.getStringExtra("out");
 		doc = Jsoup.parse(fServ);
 		
 		
         try {
         	elements = scrap(doc);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
         
         first.add(asigsToArray(elements, true, comunidades)); // Añadimos el Array con los  nombres de Carpetas, Asignaturas y Archivos al ArrayList - [0]
         String s[] = urlsToArray(elements, true, comunidades);
         first.add(s); // Añadimos el Array con las  URLS al ArrayList - [1]
         int mysize = s.length;
         first.add(typeToArray(elements, true, comunidades, mysize)); // Añadimos el Array con los TYPES al ArrayList - [2]
         
         //Copia de FIRST que puede ser borrada
         ArrayList<Object[]> first2 = new ArrayList<Object[]>();
         for (Object[] objects: first) {
         first2.add((Object[])objects.clone());
         }
 
         lstDocs = (ListView)findViewById(R.id.LstDocs); // Declaramos la lista
         lstAdapter = new AdaptadorDocs(this, first2);
         lstDocs.setAdapter(lstAdapter); // Declaramos nuestra propia clase adaptador como adaptador
         
         lstDocs.setOnItemClickListener(new OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> a, View v, int position, long id) { //Al clicar X item de la lista
             	Boolean isHome;
             	isInternetPresent = cd.isConnectingToInternet();
             	if(isInternetPresent) {
 	            	if(!(first.get(2)[position].toString().equals("0")) && !(first.get(2)[position].toString().equals("1")) && !(first.get(2)[position].toString().equals("6"))) {
 	            		isDocument = true;
 	            		docPosition = position;
 	            		Log.d("TIPO", "DOCUMENTO");
 	            		// ProgressDialog (salta para mostrar el proceso del archivo descarg�ndose)
 	            		dialog = new ProgressDialog(mycontext);
 	                    
 	            		// Servicio para la descarga del archivo
 	            		i = new Intent(mycontext, CurlService.class);
 		                bindService(i, CurlConnection, Context.BIND_AUTO_CREATE); // Conectamos el servicio
 	            	} else {
 	            		isDocument = false;
 		            	if(onUrl.get(onUrl.size() - 1) == "/dotlrn/?page_num=2" && !comunidades){
 		                	isHome = true;
 		                } else {
 		                	isHome = false;
 		                }
 		            	//Cuando se hace click en una opción de la lista, queremos borrar todo mientras carga, inclu�do el t�tulo header
 		            	headerTitle.setText(null);
 		            	lstAdapter.clearData();
 		            	// Refrescamos View
 		            	lstAdapter.notifyDataSetChanged();
 		            	lstDocs.setDividerHeight(0);
 		            	if(isHome) {
 		            		if(position == 0) comunidades = true; //Click Comunidades y otros
 		            		onUrl.add(first.get(1)[position].toString());
 		            		onName.add(first.get(0)[position].toString());
 		            	} else { 
 		            		if(position==0){ //Click atr�s
 		            			onUrl.remove(onUrl.size() - 1); //Quitamos la URL actual
 		            			onName.remove(onName.size() - 1); //Quitamos el Nombre actual
 		            			comunidades = false;
 		            			if(onUrl.size()>=2 && (onUrl.get(0).toString().equals(onUrl.get(1).toString()))) comunidades = true;
 		            		} else {
 		            			onUrl.add(first.get(1)[position].toString());
 		                    	onName.add(first.get(0)[position].toString());
 		            		}
 		            	}
 		            	Log.d("URL", onUrl.get(onUrl.size() - 1));
 		            	i = new Intent(mycontext, CurlService.class);
 		                bindService(i, CurlConnection, Context.BIND_AUTO_CREATE); // Conectamos el servicio
 	            	}
 	            } else {
 	            	Toast.makeText(getBaseContext(),getString(R.string.no_internet), Toast.LENGTH_LONG).show();
 	            }
             }
         });
 	    
 	    if (dataUpdateReceiver == null) dataUpdateReceiver = new DataUpdateReceiver();
         IntentFilter intentFilter = new IntentFilter(CurlService.RESPONSE);
         registerReceiver(dataUpdateReceiver, intentFilter);
         
 	}
 	
 	/**
 	 * Creamos nuestro propio adaptador para la lista con la siguiente clase: AdaptadorDocs
 	 */
 	
 	class AdaptadorDocs extends BaseAdapter {
         
 	    Activity context;
 	    ArrayList<Object[]> data; // 0 = nombres 1 = urls 2 = type
 
 	    AdaptadorDocs(Activity context,  ArrayList<Object[]> data) {
 	    	 super();
 	         this.context = context;
 	         this.data = data;
 	    }
 	     
 	    public int getCount() {
 	    	if(data.isEmpty()) return 1;
             return data.get(0).length;
         }
 	    
 	    public Object getItem(int position) {
             return position;
         }
 	    
 	    public long getItemId(int position) {
             return position;
         }
 	    
 	    public void clearData() {
 	        // clear the data
 	        data.clear();
 	    }
 	    
 	    public View getView(int position, View convertView, ViewGroup parent) {
 	    	View item;
 	    	if(!data.isEmpty()) {
 	    	LayoutInflater inflater = context.getLayoutInflater();
 	    	
 	        item = inflater.inflate(R.layout.list_docs, null);
 	        item.setMinimumHeight(65);  
 	        item.setPadding(14, 0, 6, 0);
 	        TextView title;
 	        ImageView image;
 	        int ico;
 	        // T�tulo
 		    title = (TextView)item.findViewById(R.id.list_title);
 		    String rgxTitle;
 		    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context); // Obtenemos las preferencias
 		    
 		    // Imagen
 		    image = (ImageView)item.findViewById(R.id.folderImage);
 		    Log.d("SWITCH", "Posici�n " +position+ " Valor: " +data.get(2)[position].toString()+ " Tama�o total: " +String.valueOf(data.get(2).length-1));
 		    switch(Integer.parseInt(data.get(2)[position].toString())) {
 		    case 0: // Atr�s
 		    	rgxTitle = data.get(0)[position].toString().replaceAll( "\\d{4}-\\d{4}\\s|\\d{4}-\\d{2}\\s|Documentos\\sde\\s?|Gr\\..+?\\s|\\(.+?\\)|Sgr\\..+?\\s", "" );
 		    	title.setText(rgxTitle.trim());
 		    	ico = getResources().getIdentifier("com.jp.miaulavirtual:drawable/ic_back", null, null); // Back ico
 		    	image.setImageResource(ico);
 		    	break;
 		    case 1: // Carpeta
 		    	rgxTitle = data.get(0)[position].toString().replaceAll( "\\d{4}-\\d{4}\\s|\\d{4}-\\d{2}\\s|Documentos\\sde\\s?|Gr\\..+?\\s|\\(.+?\\)|Sgr\\..+?\\s", "" );
 		    	title.setText(rgxTitle.trim());
 		    	ico = getResources().getIdentifier("com.jp.miaulavirtual:drawable/icon_folder", null, null); // Folder ico
 		    	image.setImageResource(ico);
 		    	break;
 		    case 2: // PDF
 		    	rgxTitle = data.get(0)[position].toString();
 		    	title.setText(rgxTitle.trim());
 		    	ico = getResources().getIdentifier("com.jp.miaulavirtual:drawable/ic_pdf", null, null); // PDF ico
 		    	image.setImageResource(ico);
 		    	break;	    	
 		    case 3: // Excel
 		    	rgxTitle = data.get(0)[position].toString();
 		    	title.setText(rgxTitle.trim());
 		    	ico = getResources().getIdentifier("com.jp.miaulavirtual:drawable/ic_excel", null, null); // Excel ico
 		    	image.setImageResource(ico);
 		    	break;
 		    case 4: // Power Point
 		    	rgxTitle = data.get(0)[position].toString();
 		    	title.setText(rgxTitle.trim());
 		    	ico = getResources().getIdentifier("com.jp.miaulavirtual:drawable/ic_ppt", null, null); // PPT ico
 		    	image.setImageResource(ico);
 		    	break;
 		    case 5: // Word
 		    	rgxTitle = data.get(0)[position].toString();
 		    	title.setText(rgxTitle.trim());
 		    	ico = getResources().getIdentifier("com.jp.miaulavirtual:drawable/ic_word", null, null); // Word ico
 		    	image.setImageResource(ico);
 		    	break;
 		    case 6: // Comunidades y Otros
 		    	rgxTitle = data.get(0)[position].toString().replaceAll( "\\d{4}-\\d{4}\\s|\\d{4}-\\d{2}\\s|Documentos\\sde\\s?|Gr\\..+?\\s|\\(.+?\\)|Sgr\\..+?\\s", "" );
 		    	title.setText(rgxTitle.trim());
 		    	ico = getResources().getIdentifier("com.jp.miaulavirtual:drawable/ic_comu", null, null); // Word ico
 		    	image.setImageResource(ico);
 		    	break;
 		    default: // Otros
 		    	rgxTitle = data.get(0)[position].toString();
 		    	title.setText(rgxTitle);
 		    	ico = getResources().getIdentifier("com.jp.miaulavirtual:drawable/ic_def", null, null); // Other ico
 		    	image.setImageResource(ico);
 		    	break;
 		    }
 		    if(!prefs.getBoolean("pattern", true)) title.setText(data.get(0)[position].toString());
 	        
 	        // Imagen
 	         // 1
 	        //int pdf = getResources().getIdentifier("com.jp.miaulavirtual:drawable/icon_pdf", null, null); // 2
 	        //int excel = getResources().getIdentifier("com.jp.miaulavirtual:drawable/icon_excel", null, null); // 3
 	        
  
 	        
 	        //switch(Integer.parseInt(data.get(2)[position].toString())) {
 	        
 	        //}
 	 
 	        //botoncito desplegable pendiente AQUI
 	    	} else {
 	    			LayoutInflater inflater = context.getLayoutInflater();
 	    	        item = inflater.inflate(R.layout.load_list, null);
 	    	        item.setMinimumHeight(35);
 	    	}
 	        return(item);
 	    }
 	}
 	
 	public void afterBroadcaster(String mydoc) { //M�todo proceso GET
 		Boolean isHome;
 		
 		// Actualizamos nombre de LblSubTitulo;
 		TextView headerTitle = (TextView) findViewById(R.id.LblSubTitulo); // T�tulo Header
 		headerTitle.setTextColor(getResources().getColor(R.color.list_title));
         headerTitle.setTypeface(null, 1);
         headerTitle.setText(onName.get(onName.size() - 1));
 		
 		doc = Jsoup.parse(mydoc);
         try {
 			elements = scrap(doc);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
         if(onUrl.get(onUrl.size() - 1) == "/dotlrn/?page_num=2" && !comunidades){
         	isHome = true;
         } else {
         	isHome = false;
 
         }
 		first.add(0, asigsToArray(elements, isHome, comunidades)); //Array con los  nombres de Carpetas, Asignaturas y Archivos
 		String s[] = urlsToArray(elements, isHome, comunidades);
 		first.add(1, s);
 		int mysize =  s.length;
 		first.add(2, typeToArray(elements, isHome, comunidades, mysize));
 		
 		//Copia de FIRST que puede ser borrada
 		ArrayList<Object[]> first2 = new ArrayList<Object[]>();
         for (Object[] objects: first) {
         first2.add((Object[])objects.clone());
         }
         
         // Re-iniciamos adaptador de lista
         lstAdapter = new AdaptadorDocs(this, first2);
         lstDocs.setDividerHeight(1);
         lstDocs.setAdapter(lstAdapter);
 		
 	}
 	
     public void onStart()
     {
         super.onStart();
         Log.d(tag, "In the onStart() event");
         
     }
     
     public void onRestart()
     {
         super.onRestart();
         isInternetPresent = cd.isConnectingToInternet();
         Log.d(tag, "In the onRestart() event");
         
         /**
          * Comprobar si tiene conexi�n a Internet.
          * Tiene conexi�n: Actualizar
          * No tiene conexi�n: Mensaje "No existe conexi�n a internet"
          */
     }
     
     public void onResume()
     {
         super.onResume();
         Log.d(tag, "In the onResume() event");
     }
     
     public void onPause()
     {
         super.onPause();
         Log.d(tag, "In the onPause() event");
     }
     
     public void onStop()
     {
         super.onStop();
         Log.d(tag, "In the onStop() event");
     }
     
     public void onDestroy()
     {
         super.onDestroy();
         Log.d(tag, "In the onDestroy() event");
         if(dataUpdateReceiver!=null) unregisterReceiver(dataUpdateReceiver);
         
         //ELIMINAR COOKIE DEL CACHE para que siempre que se inicie la aplicacion haga POST
         SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
         Editor editor = prefs.edit(); //eliminamos la cookie
     	editor.remove("cookies"); //ELIMINAMOS LA COOKIE DE LA PASADA VEZ
     	editor.commit();
     }
 
 	
     /**
      * Acción para cada Item del Menu
      */
     public boolean onOptionsItemSelected(MenuItem item){
         /*El switch se encargar� de gestionar cada elemento del men� dependiendo de su id,
         por eso dijimos antes que ning�n id de los elementos del men� podia ser igual.
         */
         switch(item.getItemId()){
         case R.id.preferencias: //Nombre del id del men�, para combrobar que se ha pulsado
         	Log.d(tag, "Preferencias");
         	startActivity(new Intent(this, SettingsActivity.class));;
         	break;
         case R.id.mysesion:
         	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
             Editor editor = prefs.edit();
             editor.remove("myuser");
             editor.remove("mypass");
             editor.commit();
             Intent intent = new Intent(this, MainActivity.class);
             startActivity(intent);
             finish();
             break;
         case android.R.id.home:
 			// This ID represents the Home or Up button. In the case of this
 			// activity, the Up button is shown. Use NavUtils to allow users
 			// to navigate up one level in the application structure. For
 			// more details, see the Navigation pattern on Android Design:
 			//
 			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
 			//
 			NavUtils.navigateUpFromSameTask(this);
 			return true;
         }
         return true;
     }
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.activity_display_message, menu);
         return true;
 	}
 
     public Elements scrap(Document mdoc) throws IOException {
 		Elements elem = mdoc.select("tbody tr[class=odd], tbody tr[class=even], tbody tr[class=even last], tbody tr[class=odd last]"); //Filas de Documentos
 	    Log.d("Service", "Passing elements variable");
 		return elem;
 	}
 	
 	public String [] asigsToArray(Elements melem, Boolean isHome, Boolean comun) {
 		int i = 1;
 		String s[];
 		Elements elem;
 		if(isHome) {
 			elem = melem.select("td[headers=contents_name] a, td[headers=folders_name] a").not("[href*=/clubs/]"); //Nombre Asignaturas String !"Comunuidades"
 			s = new String[(elem.size())+1]; //todo-comunidades + 1(carpeta comunidades)
 			s[0] = "Comunidades y otros";
 			for(Element el : elem){
 			    s[i] = el.text();
 			    i++;
 			}
 		} else if(comun) {
 			elem = melem.select("td[headers=contents_name] a[href*=/clubs/], td[headers=folders_name] a[href*=/clubs/]"); //Nombre Asignaturas String "Comunuidades"
 			s = new String[elem.size()+1]; //comunidades + 1(atr�s)
 			s[0] = "Atrás " + onName.get(onName.size() - 2);
 			for(Element el : elem){
 			    s[i] = el.text();
 			    i++;
 			}
 		} else {
 			elem = melem.select("td[headers=contents_name] a[href], td[headers=folders_name] a[href]"); //Nombre Asignaturas String 
 			s = new String[elem.size()+1]; //todo + 1 (atr�s)
 			s[0] = "Atrás " + onName.get(onName.size() - 2);
 			for(Element el : elem){
 			    s[i] = el.text();
 			    i++;
 			}
 		}
 		Log.d("asigseToArray", String.valueOf(elem.size()));
 		return s;
 	}
 	
 	public String [] urlsToArray(Elements melem, Boolean isHome, Boolean comun) {
 		Elements elem;
 		int i = 1;
 		String s[];
 		if(isHome) {
 			elem = melem.select("td[headers=contents_name] a, td[headers=folders_name] a").not("[href*=/clubs/]"); //Nombre Asignaturas String !"Comunuidades"
 			s = new String[(elem.size())+1];
 			s[0] = "/dotlrn/?page_num=2";
 			for(Element el : elem){
 			    s[i] = el.select("a").attr("href");
 			    i++;
 			}
 		} else if(comun) {
 			elem = melem.select("td[headers=contents_name] a[href*=/clubs/], td[headers=folders_name] a[href*=/clubs/]"); //Nombre Asignaturas String "Comunuidades"
 			s = new String[elem.size()+1];
 			s[0] = onUrl.get(onUrl.size() - 2);
 			for(Element el : elem){
 			    s[i] = el.select("a").attr("href");
 			    i++;
 			}
 		} else {
 			elem = melem.select("td[headers=contents_name] a[href], td[headers=folders_name] a[href]"); //Nombre Asignaturas String 
 			s = new String[elem.size()+1];
 			s[0] = onUrl.get(onUrl.size() - 2);
 			for(Element el : elem){
 			    s[i] = el.select("a").attr("href");
 			    i++;
 			}
 		}
 		Log.d("urlsToArray", String.valueOf(s.length));
 		return s;
 	}
 	
 	public String [] typeToArray(Elements melem, Boolean isHome, Boolean comun, int size) {
 		Elements elem = melem.select("td[headers=folders_type], td[headers=contents_type]"); //Nombre Asignaturas String
 		String[] mtypes = {"carpeta", "Carpeta", "PDF", "Microsoft Excel", "Microsoft PowerPoint", "Microsoft Word"};
 		String s[];
 		int i = 1;
 		if(isHome) {
 			s = new String[size];
 			s[0] = "6";	
 			while(i<size) {
 				Log.d("get(type)", i+"  Tama�o: " +size);
 				s[i] = "1";
 				i++;
 			}
 		} else if(comun) {
 			s = new String[size];
 			s[0] = "0";
 			while(i<size) {
 				s[i] = "6";
 				i++;
 			}
 		} else {
 			s = new String[elem.size()+1];
 			s[0] = "0";
 			for(Element el : elem){
 				String types = el.text().trim();
 				s[i] = "7"; // Defecto al menos que...:
 				if(mtypes[0].equals(types.toString()) || mtypes[1].equals(types.toString())) s[i] = "1"; // 1 = Carpeta
 				if(mtypes[2].equals(types.toString())) s[i] = "2"; // 2 = PDF 
 				if(mtypes[3].equals(types.toString())) s[i] = "3"; // 3 = Excel
 				if(mtypes[4].equals(types.toString())) s[i] = "4"; // 4 = Power Point
 				if(mtypes[5].equals(types.toString())) s[i] = "5"; // 5 = Word
 				Log.d("Types", s[i]);
 				i++;
 			}
 		}
 		Log.d("typeToArray", String.valueOf(size));
 		return s;
 	}
 	
 
     /** Defines callbacks for service binding, passed to bindService() */
     ServiceConnection CurlConnection = new ServiceConnection() {
 
         @Override
         public void onServiceConnected(ComponentName className,
                 IBinder service) {
             // We've bound to LocalService, cast the IBinder and get LocalService instance
         	cURL = ((CurlService.CurlBinder)service).getService();
         	cURL.user = user;
         	cURL.pass = pass;
         	cURL.url = onUrl.get(onUrl.size() - 1);
         	if(isDocument) 
         		{
         		cURL.url = first.get(1)[docPosition].toString();
         		cURL.url_back = onUrl.get(onUrl.size() - 2);
         		cURL.pdialog = dialog;
         		}
         	cURL.isDocument = isDocument;
             startService(i);
             Log.d("Service", "Servicio Conectado");
         }
 
         @Override
         public void onServiceDisconnected(ComponentName arg0) {
         	cURL = null;
         	Log.d("Service", "Servicio Desconectado");
         }
     };
 
 }
