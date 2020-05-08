 package net.audev.batteryinfowidget;
 
 import java.io.File;
 
 import net.audev.batteryinfowidget.BatteryInfo.UpdateWidgetService;
 
 import android.app.Activity;
 import android.content.Context;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
import android.widget.TextView;
 import android.widget.Toast;
 
 public class BatteryInfoWidget extends Activity {
     /** Called when the activity is first created. */
 	Context context;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
        context = this;       
         
         Button boton = (Button)findViewById(R.id.botonLimpiar);
         boton.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
         		File cacheDir = context.getFilesDir();
 		        File cargaFile = new File(cacheDir,UpdateWidgetService.FILE_CARGA);
 		        File descargaFile = new File(cacheDir,UpdateWidgetService.FILE_DESCARGA);
 		        if (cargaFile.exists())
 		        	cargaFile.delete();
 		        if (descargaFile.exists())
 		        	descargaFile.delete();
 		        Toast.makeText(context, R.string.files_deleted_successfully, Toast.LENGTH_LONG).show();
 			}
         	
         });
     }
 }
