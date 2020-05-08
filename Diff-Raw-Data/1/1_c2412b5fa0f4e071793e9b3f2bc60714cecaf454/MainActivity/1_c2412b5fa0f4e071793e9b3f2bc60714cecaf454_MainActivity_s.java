 package com.kiwicorporation.taskinator;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 import model.ListT;
 import modelException.ListTException;
 import modelException.TaskException;
 import xmlParsor.ListBackup;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ExpandableListView;
 import controleur.ListManager;
 
 public class MainActivity extends Activity {
 
 	private ExpandableListView expandableList = null;
 	String FILESAVE = "save.xml";
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		expandableList = (ExpandableListView) findViewById(R.id.expandableView);
 		restore(savedInstanceState);
 
 		final ELVAdapter adapter = new ELVAdapter(this, expandableList);
 
 		expandableList.setAdapter(adapter);
 
 		Button addListButton = (Button) findViewById(R.id.idAddList);
 		addListButton.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View view) {
 				AlertDialog.Builder builder = new AlertDialog.Builder(
 						MainActivity.this);
 				builder.setTitle("Add list");
 
 				// Use an EditText view to get user input.
 				final EditText input = new EditText(MainActivity.this);
 				input.setHint("Name of the list");
 				builder.setView(input);
 
 				builder.setPositiveButton("Ok",
 						new DialogInterface.OnClickListener() {
 
 							@Override
 							public void onClick(DialogInterface dialog,
 									int whichButton) {
 								String value = input.getText().toString();
 								ListManager.getInstance().addList(
 										new ListT(value));
 								adapter.notifyDataSetChanged();
 							}
 						}).setNegativeButton("Cancel",
 						new DialogInterface.OnClickListener() {
 
 							@Override
 							public void onClick(DialogInterface dialog,
 									int which) {
 								dialog.cancel();
 							}
 						});
 				// Create alert dialog
 				AlertDialog modifyDialog = builder.create();
 
 				// Show it
 				modifyDialog.show();
 			}
 		});
 	}
 
 	/*@Override
 	public void onPause() {
 		super.onPause();
 		// Save in XML File
 		FileOutputStream file;
 		try {
 			deleteFile(FILESAVE);
 			file = openFileOutput(FILESAVE, Context.MODE_PRIVATE);
 			ListBackup.getInstance().saveListToFile(file);
 			file.close();
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}*/
 	
 	@Override
 	public void onSaveInstanceState(Bundle outstate) {
 		super.onSaveInstanceState(outstate);
 		
 		//Whether there is no list, no need to save something
 		if(!ListManager.getInstance().getLists().isEmpty()) {
 			FileOutputStream file;
 			try {
 				deleteFile(FILESAVE);
 				file = openFileOutput(FILESAVE, Context.MODE_PRIVATE);
 				ListBackup.getInstance().saveListToFile(file);
 				file.close();
 			} catch (FileNotFoundException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	public void restore(Bundle state) {
 		try {
 			FileInputStream file = this.openFileInput(FILESAVE);
 			ListManager.getInstance().removeAllList();
			System.out.println(ListManager.getInstance());
 			ListManager.getInstance().setListOfList(
 					ListBackup.getInstance().getListFromInputStream(file));
 			file.close();
 		} catch (TaskException e) {
 			e.printStackTrace();
 		} catch (ListTException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 }
