 package com.proyectoargrado;
 
 import java.io.IOException;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.client.utils.URLEncodedUtils;
 import org.json.JSONArray;
 import org.json.JSONException;
 
 import poi.PoiBean;
 import android.app.Activity;
 import android.content.Context;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.media.AudioManager;
 import android.os.Bundle;
 import android.view.View;
 import android.view.WindowManager;
 import android.widget.Toast;
 
 import com.proyectoargrado.servicios.eventos.EventosServices;
 import com.wikitude.architect.ArchitectView;
import com.wikitude.architect.ArchitectUrlListener;
 
 public class ExplorarImagenes extends Activity implements ArchitectUrlListener{
 	
 	
 	
 	//Key de estudiante suplida por Wikitude
 	private String apiKey = "fUOGJVNkR1hHB1zr5WD9or3BZHyhEcL4wHDfjanJoURGFkvG6oUQAO97J82A54Q2R3u2TOGA9GFvky5gVjSorxDv2h2fO+CqW/MIAPQoU8fysj5inE9aGodvTtXOmyga6D2+pPESAczB8VaZ/VmE1dF6Jo2v7M0d5LHiQQIrY8VTYWx0ZWRfX3IAg2v5ZLAx9WnxcuyKIuJ00UA+0c/0MAd/RsmzmwrHCIc/kO2OGQ5qFILjhyg24A4Gt6OMnTTciEoF3sPptcJgzLFnc1PBXsK7WrUNQCswqJFyEZt7Ftmtcjt7OjUdNI9ZnCAJOqJc8VQJobfBjU7Q+KA1ay6xqglj/lTsOIOxJrAp196qvBhwoxdLtINuATXm5sms2WdQOpCI4WvWPuB+f06QJ5tp0NH4tbxxmTDBs+bfwiTs6AvZESh92NceJxuULMmzhKfI3EQbtYncCDYX2+QC873dC4JdDGMKI1cE+KQSznxPvTTpBvuSm5xRCiNFDjxFL4QPG8rZM9ho4AQf7K4e7PvaQUFZfxff6RUltc/l+jWlApeGwrbfq+Mxt9wSwroOg9UTWRByR7mtosHJ8s6rEK0dlwMnlIb1Woqf6/Fex+bds029urrt/Y/+TtfYn60ODBHSEs+0ULDdKCiOySjJsPrgtP5e04Nia0fvCgjlNq8weoY=";
 		
 	//archview para cargar los POI haciendo overlaid a la camara
 	private ArchitectView architectView;
 	
 	//contexto de la actividad
 	private Context contexto =  this;	
 	
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
             
         //para determinar si el dispositivo cumple con los requisitos para correr la app.
         if(!ArchitectView.isDeviceSupported(this))
         {
         	Toast.makeText(this, "minimum requirements not fulfilled", Toast.LENGTH_LONG).show();
         	this.finish();
         	return;
         }        
        
         
         //mostrar la aplicacion fullscreen cuando este en modo explorador
         this.getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN );       
         setContentView(R.layout.main);        
                 		
 		//set the devices' volume control to music to be able to change the volume of possible soundfiles to play
         this.setVolumeControlStream( AudioManager.STREAM_MUSIC );
         
         //Cargar el arcview de wikitude y aplicar la clave otorgada por wikitude
         this.architectView = (ArchitectView) this.findViewById(R.id.architectView);        
         architectView.onCreate(apiKey); 		
   		      			
      }
     
     @Override
     protected void onPostCreate(Bundle savedInstanceState) {
     	super.onPostCreate(savedInstanceState);    	
     	
     	//IMPORTANT: creates ARchitect core modules
     	if(this.architectView != null)
     		this.architectView.onPostCreate();
     	
     	
     	//register this activity as handler of "architectsdk://" urls
     	this.architectView.registerUrlListener(this);
     	
     	try {
 			loadSampleWorld();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
     }
     
 	@Override
 	protected void onResume() {
 		super.onResume();
 		this.architectView.onResume();		
 		
 		//solo los objetos que esten a 2km de distancia o menos se mostraran
 		//this.architectView.setCullingDistance(2000);		
 	}
 	
 	@Override
 	protected void onPause() {
 		super.onPause();
 		if (this.architectView != null){
 			this.architectView.onPause();
 		}
 		//locManager.removeUpdates(this);
 	}
     
 	
     @Override
     protected void onDestroy() {
     	super.onDestroy();
     	
     	if(this.architectView != null)
     		this.architectView.onDestroy();
     }
     
     @Override
     public void onLowMemory() {
     	super.onLowMemory();
     	
     	if(this.architectView != null)
     		this.architectView.onLowMemory();
     }
     
 	@Override
 	public boolean urlWasInvoked(String url) {		
 		
 		//System.out.println("mi baina es "+url);
 		//parsing the retrieved url string
 		List<NameValuePair> queryParams = URLEncodedUtils.parse(URI.create(url), "UTF-8");
 		
 		String id = "";
 		String name = "";
 		// getting the values of the contained GET-parameters
 		for(NameValuePair pair : queryParams)
 		{
 			
 			
 			if(pair.getName().equals("id"))
 			{				
 				id = pair.getValue();
 				System.out.println("mi id es: "+id);
 			}
 			if(pair.getName().equals("name"))
 			{
 				name = pair.getValue();
 				System.out.println("mi nombre: "+name);
 			}
 		}		
 		
 		EventosServices eventosService = new EventosServices(contexto, id);		
 	    eventosService.execute();
 		return true;
 	}
 	
 
 	//Cargar tablones en el mapa....
 	private void loadSampleWorld() throws IOException, JSONException {
 		this.architectView.load("exploimagenes.html");			
 		
 	}
 	
 	
 }
