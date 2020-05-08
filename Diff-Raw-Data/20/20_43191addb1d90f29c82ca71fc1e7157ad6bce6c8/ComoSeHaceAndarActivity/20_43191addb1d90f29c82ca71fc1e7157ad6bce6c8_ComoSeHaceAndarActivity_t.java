 package org.utn.proyecto.helpful.integrart.integrar_t_android.activities.comosehace;
 
 import org.utn.proyecto.helpful.integrart.integrar_t_android.R;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.AdapterView;
 import edu.dhbw.andar.ARToolkit;
 import edu.dhbw.andar.AndARActivity;
 import edu.dhbw.andar.exceptions.AndARException;
 
 
 public class ComoSeHaceAndarActivity extends AndARActivity {
 
 	private static final String VIDEO = "video";
     @Override
     public void onCreateContextMenu(ContextMenu menu, View v,
             ContextMenuInfo menuInfo) {
         // TODO Auto-generated method stub
 	    
 	    MenuInflater inflater = getMenuInflater();
 	      inflater.inflate(R.menu.csh_activity_main_menu, menu);
 
          AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
          long itemID = info.position;
          menu.setHeaderTitle("lior" + itemID);
 	    
          
         super.onCreateContextMenu(menu, v, menuInfo);
     }
 
     @Override
     public boolean onTouchEvent(MotionEvent event) {
         // TODO Auto-generated method stub
         return super.onTouchEvent(event);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // TODO Auto-generated method stub
 	    //getMenuInflater().inflate(R.menu.csh_activity_main_menu, menu);
         return super.onCreateOptionsMenu(menu);
     }
 
     ComoSeHaceCustomAndarObject someObject;
 	ARToolkit artoolkit;
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		
 		super.onCreate(savedInstanceState);
 		ComoSeHaceAndarRenderer renderer = new ComoSeHaceAndarRenderer();//optional, may be set to null
 		super.setNonARRenderer(renderer);//or might be omited
 		try {
 			//register a object for each marker type
 			artoolkit = super.getArtoolkit();
 			someObject = new ComoSeHaceCustomAndarObject
 				("test", "patt.hiro", 80.0, new double[]{0,0},this);
 			artoolkit.registerARObject(someObject);
 			someObject = new ComoSeHaceCustomAndarObject
 			("test", "android.patt", 80.0, new double[]{0,0},this);
 			artoolkit.registerARObject(someObject);
 			someObject = new ComoSeHaceCustomAndarObject
 			("test", "barcode.patt", 80.0, new double[]{0,0},this);
 			artoolkit.registerARObject(someObject);
 		} catch (AndARException ex){
 			//handle the exception, that means: show the user what happened
			System.out.println("Hubo un error generando Patrones.");
 		}		
 		startPreview();
 	}
 
 	/**
 	 * Inform the user about exceptions that occurred in background threads.
 	 * This exception is rather severe and can not be recovered from.
 	 * TODO Inform the user and shut down the application.
 	 */
 	public void uncaughtException(Thread thread, Throwable ex) {
 		Log.e("AndAR EXCEPTION", ex.getMessage());
 		finish();
 	}
 	
 	
     public void executeReproductor(String path) {
         // TODO Auto-generated method stub
         Intent intent =  new Intent(this,ComoSeHaceReproductor.class);
         intent.putExtra(VIDEO, path);
         this.startActivity(intent);
     }
 	
 	
 }
