 package fi.aalto.lounaspaikka;
 
 
 
 
 import java.io.FileOutputStream;
 import java.io.OutputStreamWriter;
 
 import com.google.gson.Gson;
 
 import fi.aalto.lounaspaikka.filters.filterArrayObject;
 import fi.aalto.lounaspaikka.objectfiles.ObjectsContainer;
 
 import android.app.Activity;
 import android.content.Context;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class ReviewFiltterActivity extends Activity {
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.reviewfilteradd);
 		Button show = (Button) findViewById(R.id.addweight);
 	
 		show.setOnClickListener(new View.OnClickListener() 
 		{
 			public void onClick(View view)
 			{ 
 				EditText weightnameE = (EditText) findViewById(R.id.WeightnameField);
 				EditText weightE = (EditText) findViewById(R.id.weightField);
 			String  weightname= weightnameE.getText().toString();
 			String weights = weightE.getText().toString();
 			try
 			{

 			int weight=	Integer.parseInt(weights);
 			if (weight>=(-100) && weight<=100 && weightname!="") {
 				filterArrayObject filterr= new filterArrayObject();
 				filterr.filter=weightname;
 				filterr.filterweight=weight;
				ObjectsContainer.filter.add(filterr);
 				savearray();
				Toast.makeText(getApplicationContext(), "Weight has been added", Toast.LENGTH_LONG).show();
 				weightnameE.setText("");
 				weightE.setText("");
 			} else {
 			Toast.makeText(getApplicationContext(), "Wrong input", Toast.LENGTH_LONG).show();
 			}
 			
 			}
 			catch(NumberFormatException explanation){
 
 				Toast.makeText(getApplicationContext(), "Weight wasn't integer", Toast.LENGTH_LONG).show();
 			}
 
 			}
 
 			private void savearray() {
 				Gson gson = new Gson();
 				String arraytostring="";
 				String json= gson.toJson(ObjectsContainer.filter);
 				savetofileasstring(json);
 			}
 			
 				private void savetofileasstring(String arraytostring){
 				FileOutputStream fOut = null;
 		        OutputStreamWriter osw = null;
 
 		        try{
 
 		        fOut = openFileOutput("filterlounaspaikka.txt", Context.MODE_WORLD_WRITEABLE);
 		        osw = new OutputStreamWriter(fOut);
 		        osw.write(arraytostring);
 		        osw.close();
 		        fOut.close();
 		        }catch(Exception e){
 
 		        e.printStackTrace(System.err);
 		        }
 		        }	
 				
 			
 		});
 	
 		Button clearfilters = (Button) findViewById(R.id.clearfilter);
 		clearfilters.setOnClickListener(new View.OnClickListener() {
 
 			public void onClick(View view)
 			{
 				int filtersize= ObjectsContainer.filter.size();
 				int counter=0;
 				while (filtersize>counter) {
 					ObjectsContainer.filter.remove(0);
 					counter++;
 				}
 				clearstorage();
 				Toast.makeText(getApplicationContext(), "Filters cleared ", Toast.LENGTH_LONG).show();
 			}
 		});
 	
 	
 	}
 


 	private void clearstorage() {
 		FileOutputStream fOut = null;
         OutputStreamWriter osw = null;
 
         try{
 
         fOut = openFileOutput("filterlounaspaikka.txt", Context.MODE_WORLD_WRITEABLE);
         osw = new OutputStreamWriter(fOut);
         osw.write("");
         osw.close();
         fOut.close();
         }catch(Exception e){
 
         e.printStackTrace(System.err);
         }
         }	
 
 
 
 
 }
 
 
