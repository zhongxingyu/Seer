 package ch.arons.android.gps;
 
 import java.io.File;
 import java.io.IOException;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.os.Environment;
 import android.text.method.ScrollingMovementMethod;
 import android.util.Log;
 import android.widget.TextView;
 import android.widget.Toast;
 import ch.arons.android.gps.io.file.GpxReader;
 
 public class GPXFileViewActivity extends Activity{
	private static final String COMPONENT = "GPXFileViewActivity";
     
 	public static String fileName = null;
 	
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.file_view);
 		
 		TextView textData = (TextView) findViewById(R.id.data);
 		textData.setMovementMethod(new ScrollingMovementMethod());
 		 
 		if(fileName == null){
 			textData.setText("");
 		}else{
 			File root = Environment.getExternalStorageDirectory();
 			File dir = new File(root, "/GPSTrac/");
 			File file = new File(dir, fileName);
 			
 			String text;
             try {
 	            text = GpxReader.readText(file);
             } catch (IOException e) {
 	            Log.e(COMPONENT, e.getMessage(),e);
 	            Toast.makeText(getApplicationContext(), "Error reading file:"+fileName,Toast.LENGTH_SHORT).show();
 	            return;
             }
 			textData.setText(text);
 		}
 		
 	}
 }
