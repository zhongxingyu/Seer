 package com.example.transitlogger;
 
 import java.io.File;
 import java.io.IOException;
 
 import jxl.read.biff.BiffException;
 import jxl.write.WriteException;
 
 import com.example.transitlogger.excel.SpreadsheetFiller;
 
 import android.os.Bundle;
 import android.os.Environment;
 import android.app.Activity;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Toast;
 
 public class GenerateSpreadsheeetActivity extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_generate_spreadsheet);
 		
 		Utils.copyAssets(this);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.generate_spreadsheeet, menu);
 		return true;
 	}
 	
 	public void onGenerate(View view) {
 	    File path = Environment.getExternalStoragePublicDirectory(
 	            Environment.DIRECTORY_DOCUMENTS);
 	    File outputFile = new File(path, "output.xls");
 
         // Make sure the directory exists.
         path.mkdirs();
 	    
         Log.w("ASD", outputFile.getName());
         
 		SpreadsheetFiller filler = new SpreadsheetFiller("korsel.xls", outputFile.getName());
 		
 		try {
 			filler.readWrite();
 		} catch (BiffException e) {
 			Toast.makeText(this, "BIFF Error!", Toast.LENGTH_LONG).show();
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (WriteException e) {
 			Toast.makeText(this, "Error writing!", Toast.LENGTH_LONG).show();
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			Toast.makeText(this, "IO Error!", Toast.LENGTH_LONG).show();
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} finally {
 //			Toast.makeText(this, "Generated spreadsheet " + outputFileName + "successfully.", Toast.LENGTH_LONG).show();
 		}
 	}
 
 }
