 package com.datakom;
 
 import com.datakom.R;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.app.Activity;
 import android.app.ListActivity;
import android.content.Intent;
 
 public class TabCreate extends Activity {
 
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.createtabview);
 	  
 	  
 	  
 	  Spinner s_type = (Spinner) findViewById(R.id.spinner_type);
 	  ArrayAdapter adapter1 = ArrayAdapter.createFromResource(
 	            this, R.array.types, android.R.layout.simple_spinner_item);
 	    adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 	    s_type.setAdapter(adapter1);
 	    
 		    
 	  Spinner s_rating = (Spinner) findViewById(R.id.spinner_rating);
 	  ArrayAdapter adapter = ArrayAdapter.createFromResource(
 	            this, R.array.rating, android.R.layout.simple_spinner_item);
 	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 	    s_rating.setAdapter(adapter);
 	  
 	  Button create = (Button) findViewById(R.id.save);
	  Button add_picture = (Button) findViewById(R.id.add_picture);
	  
	  add_picture.setOnClickListener(new OnClickListener() {
		  public void onClick(View v) {
			  Intent camera = new Intent(v.getContext(), CameraMain.class);
			  startActivityForResult(camera, 0);
		  }
	  });
 	    
 	    create.setOnClickListener(new OnClickListener() {
         	public void onClick(View v){
         		Toast.makeText(TabCreate.this, "Push object", Toast.LENGTH_SHORT).show();
         	}
         });
 
 
 	}
 
 
 }
