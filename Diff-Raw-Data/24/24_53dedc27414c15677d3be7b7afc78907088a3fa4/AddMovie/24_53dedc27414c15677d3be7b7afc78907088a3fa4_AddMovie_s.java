 package com.upopple.android.seethatmovie;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteException;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.AutoCompleteTextView;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.TextView;
 
 import com.upopple.android.seethatmovie.data.CategoriesDbAdapter;
 import com.upopple.android.seethatmovie.data.MoviesDbAdapter;
 
 public class AddMovie extends Activity {
 	AutoCompleteTextView categoryAuto;
 	TextView titleBox;
 	AddMovieTextWatcher textWatch;
 	
 	CheckBox toSee;
 
 	private static final int CATEGORY_SELECT = 1001;
 	private static final int CATEGORY_ERROR_UNDERSCORE = 1101;
 	Dialog inputError;
 	
 	ArrayList<String> movieList;
 	
 	MoviesDbAdapter mdb;
 	CategoriesDbAdapter cdb;
 	
 	Button addbutton;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState){
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.add_movie);
 		
 		mdb = new MoviesDbAdapter(this);
 		mdb.open();
 
 		cdb = mdb.getCdbAdapter();
 		cdb.open();
 		
 		textWatch = new AddMovieTextWatcher();
 		
 		Intent i = getIntent();
 		titleBox = (TextView)findViewById(R.id.movieTitle);
 		titleBox.setText(i.getStringExtra("movieTitle"));
 		
 		categoryAuto = (AutoCompleteTextView)findViewById(R.id.movieCategoryEdit);
 		ArrayList<String> allCategories = new ArrayList<String>();
 		try{
 			Cursor c = cdb.getAllCategories();
 			startManagingCursor(c);
 			
 		} catch(SQLiteException e){
 			Log.v("Get All Categories Error", "Couldn't find any categories.");
 		}
 		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.list_item, allCategories.toArray(new String[]{}));
 		categoryAuto.setAdapter(adapter);
 		categoryAuto.addTextChangedListener(textWatch);
 		
 		toSee = (CheckBox) findViewById(R.id.toSee);
 		
 		addbutton = (Button)findViewById(R.id.addMovieButton);
 		addbutton.setOnClickListener(new OnClickListener() {
 			
 			public void onClick(View v) {
 				try{
 					saveItToDb();
 				} catch(Exception e){
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 	
 	public void saveItToDb(){
 		String movieId = getIntent().getStringExtra("movieId");
 		String movieTitle = titleBox.getText().toString();
 		
 		//Adding categories
 		ArrayList<String> movieCategories = new ArrayList<String>();
 		for(String category: categoryAuto.getText().toString().split(",")){
 			if(category.trim().startsWith("_")){
 				
 			}
 			movieCategories.add(category.trim());
 		}
 		
 		if(toSee.isChecked())
 			movieCategories.add("_toSee");
 
 		mdb.insertmovie(movieId, movieTitle, movieCategories);
 			
 		cdb.close();
 		mdb.close();
 		titleBox.setText("");
 		categoryAuto.setText("");
 		Intent i = new Intent(AddMovie.this, SeeThatMovieActivity.class);
 		startActivity(i);
 	}	
 	
 	@Override
 	protected Dialog onCreateDialog(int id){
 		Dialog inputError;
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		switch(id){
 		case CATEGORY_ERROR_UNDERSCORE: 
 			builder.setMessage("Sorry! The category name cannot start with a _ (underscore).")
 				.setCancelable(true)
 				.setNeutralButton("OK", new DialogInterface.OnClickListener() {
					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						dialog.cancel();
 						categoryAuto.getText().delete(0, 1);
 					}
 				});
 			inputError = builder.create();
 			break;
 		default:
 			builder.setMessage("Oh no! Something broke.")
 				.setCancelable(true)
 				.setNeutralButton("OK", new DialogInterface.OnClickListener() {
					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						dialog.cancel();
 					}
 				});
 			inputError = builder.create();
 			break;
 		}
 		return inputError;
 	}
 	
 	private class AddMovieTextWatcher implements TextWatcher{
 
		@Override
 		public void afterTextChanged(Editable e) {
 			if(e.toString().startsWith("_")){
 				showDialog(CATEGORY_ERROR_UNDERSCORE);
 			}
 		}
 
		@Override
 		public void beforeTextChanged(CharSequence s, int start, int count,
 				int after) {
 			// TODO Auto-generated method stub
 			
 		}
 
		@Override
 		public void onTextChanged(CharSequence s, int start, int before,
 				int count) {
 			// TODO Auto-generated method stub
 			
 		}
 		
 	}
 }
