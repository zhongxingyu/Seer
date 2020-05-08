 package edu.sru.andgate.bitbot.ide;
 
 import java.util.ArrayList;
 import edu.sru.andgate.bitbot.R;
 import edu.sru.andgate.bitbot.tools.FileManager;
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.Toast;
 
 public class CodeBuilderActivity extends ListActivity 
 { 
 	 private ArrayList<CustomListView> botCodeOptions;
 	 private CodeListAdapter code_adapter;
 	 private String[] code_files;
 	 private Intent engineIntent;
 	 private Button new_program;
 	 
 	 public void onCreate(Bundle savedInstanceState) {
 		 super.onCreate(savedInstanceState);
 	        setContentView(R.layout.code_builder_main);
 	       
 	        FileManager.setContext(getBaseContext());
 			
 	        botCodeOptions = new ArrayList<CustomListView>();
 	        this.code_adapter = new CodeListAdapter(this, R.layout.code_row, botCodeOptions);
 	        setListAdapter(this.code_adapter);
 	        
 	        // Get file names in "Code" directory
   			code_files = FileManager.getFileNamesInDir(getDir("Code",Context.MODE_PRIVATE).getPath());
   			
   			CustomListView codeOptions[] = new CustomListView[code_files.length];
   			
   			for(int i = 0; i < code_files.length; i++){
   				codeOptions[i] = new CustomListView(code_files[i].toString(), FileManager.getFileDescriptionFromFile("Code",code_files[i].toString()).substring(2));
   				code_adapter.add(codeOptions[i]);
    			}
   			     	   			   
 			new_program = (Button) findViewById(R.id.create_program);
 			new_program.setOnClickListener(new View.OnClickListener() 
 			{
 				@Override
 				public void onClick(View v) {
 					try {
 						engineIntent = new Intent(CodeBuilderActivity.this, IDE.class);
 						engineIntent.putExtra("File", FileManager.readXML("code_template.xml", "program-code"));
 						startActivity(engineIntent);
						finish();
 					} catch (Exception e) {
 							
 					}
 				}
 			});
 			
 			ListView list = getListView();
 			list.setOnItemLongClickListener(new OnItemLongClickListener() {
 
 				public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
 					promptDelete(view.getTag().toString());
 					Log.v("BitBot", "Long Click Accepted");
 					return true;
 				}
 			});
 		}
 	  
 	 @Override
 	 protected void onListItemClick(ListView l, View v, int position, long id) {
 		 engineIntent = new Intent(CodeBuilderActivity.this, IDE.class);
 		 engineIntent.putExtra("File", FileManager.readTextFileFromDirectory("Code",v.getTag().toString()));
 		 startActivity(engineIntent);
		 finish();
		
 	 }
 	 
 	 public void promptDelete(final String file){ 
 		 AlertDialog.Builder deletePrompt = new AlertDialog.Builder(this);
 		 deletePrompt.setMessage("Delete this file?");
 		 deletePrompt.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 			 public void onClick(DialogInterface arg0, int arg1) {
 				 //Delete File
 				 FileManager.deleteFile("Code", file);
 				 Intent intent = getIntent();
 				 finish();
 				 startActivity(intent);
 			 }
 		 });
 		 
 		 deletePrompt.setNegativeButton("No", new DialogInterface.OnClickListener() {
 			 public void onClick(DialogInterface arg0, int arg1) {
 				 //Do Nothing
 			 }
 		 });
 
 		 // display box
 		 deletePrompt.show();
 	 }
 }
