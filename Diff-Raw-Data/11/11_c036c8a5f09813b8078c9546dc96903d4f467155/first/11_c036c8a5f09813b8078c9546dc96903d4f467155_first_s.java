 package com.srn.jntuh.results;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.widget.TextView;
 import android.widget.Toast;
 
 
 
 
 
 
 public class first extends Activity {
 	
 	ArrayList<String> rhref = new ArrayList<String>();
 	ArrayList<String> rtext = new ArrayList<String>();
 	
 	
 	
 
     
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.first);
        
         
        
        
        
        
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.first, menu);
         return true;
     }
     
     
     public void goToSecond(View v){
     	try {
 			connect();
 			Intent fintent = new Intent(first.this, second.class);
 			fintent.putStringArrayListExtra("rtext", rtext);
 			fintent.putStringArrayListExtra("rhref", rhref);
 			startActivity(fintent);
 			
 		} catch (IOException e) {
 			e.printStackTrace();
 			TextView errortext = (TextView) findViewById(R.id.errorText);
 			errortext.setText("couldn't connect to jntu server check your network connection");
 			
 		}
     }
     
     public void aboutApp(View sdv){
    	Toast.makeText(this, " JNTUH Results v0.1 \n CSE: Aurora's Institute of Technology", Toast.LENGTH_LONG).show();  
     }
     // methods
     public void connect() throws IOException{
 		Document doc = Jsoup.connect("http://jntuh.ac.in/results/").get();
 		Elements links = doc.select("a");
	
 		List<String>  linklist  = new ArrayList<String>();
 		List<String> rlinklist = new ArrayList<String>();
 		// from links to sting list
 		for (Element elt : links) {
 		    linklist.add(elt.toString());
 		}
 		// required links 
 		for(String elem : linklist){
 			if(elem.contains("B.Tech") && (elem.contains("R07")||elem.contains("R09"))){
 				rlinklist.add(elem);				
 			}			
 		}
 	
 		Document tempdoc;
 		Elements tempelem;
 		for(int i=0; i<rlinklist.size();i++){
 			tempdoc = Jsoup.parse(rlinklist.get(i));
 			tempelem = tempdoc.select("a");
 	
 			rhref.add(i, tempelem.attr("href"));
 			rtext.add(i, tempelem.text());
 		}
 	}// end connect
 
     
 }
