 package org.linkbarcode.android.org.linkbarcode;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.lang.reflect.Proxy;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Set;
 
 import org.linkbarcode.android.R;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.SharedPreferences;
 import android.content.Intent;
 import android.util.Log;
 import android.view.MenuItem.OnMenuItemClickListener;
 import android.view.View.OnClickListener; 
 import android.os.Bundle;
 import android.os.Debug;
 import android.view.Gravity;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.AutoCompleteTextView;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class LinkBarcodeActivity extends Activity implements OnClickListener{
 	
 	private static final int REQUEST_SCAN = 0;
 	
 	private Bundle savedInstanceState;
 	
 	int i=0;
 	int j=0;
 	HashMap<Integer, TableRow> lRowsIngredient = new HashMap<Integer, TableRow>();
 	HashMap<Integer, TextView> lIngredientlist = new HashMap<Integer, TextView>();
 
 	//ArrayList<TableRow> lRowsIngredient= new ArrayList<TableRow>();
     @Override
     protected void onCreate(Bundle savedInstanceState) {
     	try{
     		
     		this.savedInstanceState = savedInstanceState;
     		
     		super.onCreate(savedInstanceState);
 	        setContentView(R.layout.main);
 	        Button scanButton = (Button) findViewById(R.id.scanButton);
 	        scanButton.setOnClickListener(this);
 	        
         }catch(Exception e){
     		AlertDialog alert = new AlertDialog.Builder(this).create();
     		alert.setMessage(e.getStackTrace().toString());
     		alert.setTitle("Error : " + e.getMessage());
     		for(int errorCount = 0; errorCount<e.getStackTrace().length;errorCount++){
     			Log.d(e.getMessage(), e.getStackTrace()[errorCount].toString());
     		}
     		alert.show();
     	}
     }
 
     //Menu operations
     @Override
     public boolean onCreateOptionsMenu(Menu menu)  
     {
     	try{
     	MenuInflater inflater = getMenuInflater();
     	inflater.inflate(R.menu.menu, menu);
     	return true;
     	}catch(Exception e){
     		Toast.makeText(this, "Une erreur de creation du menu s'est produite : " + e.getMessage(), 6000).show();
     		return false;
     	}
     }
     
     @Override
     //Handle item selections
     public boolean onOptionsItemSelected(MenuItem item) {
     	switch (item.getItemId()){
     	//Profile button click
     	case R.id.itemProfile:
     		i=0;
     		setContentView(R.layout.editprofile); 
 	        ImageButton button2 = (ImageButton) findViewById(R.id.addInfoButton);
 	        Button buttonValidate = (Button) findViewById(R.id.buttonValidate);
 	        
 	        buttonValidate.setOnClickListener(this);
 	        button2.setOnClickListener(this);  
 	        AutoCompleteTextView textView = (AutoCompleteTextView) findViewById(R.id.autocomplete_country);
 	        String[] countries = getResources().getStringArray(R.array.countries_array);
 	        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,R.layout.list_item,countries);
 	        textView.setAdapter(adapter);
 	        
 	      //initialisation des ingr�dients du profil
     	    SharedPreferences myPrefs = this.getSharedPreferences("ingredient", MODE_WORLD_READABLE);
     	    SharedPreferences.Editor prefsEditor = myPrefs.edit();
     	    prefsEditor.commit();
     	    
     	    
     	    
 
     	    
     	    
     	    
     	    int iNbingredients =0;
     	    while(myPrefs.getString("Ingredient"+iNbingredients, "DEFAULT VALUE")!="DEFAULT VALUE")
 			{    	
 				TableRow oRow2 = new TableRow(this);
 				TableLayout oTableLayout = (TableLayout) findViewById(R.id.tablelayout);
 				oTableLayout.addView(oRow2);
 	
 				ImageButton oDelete = new ImageButton(this);
 				oDelete.setOnClickListener(this);
 				oDelete.setId(i);
 				oDelete.setImageResource(R.drawable.delete_icon);
 				oDelete.setTag("X");
 				TableRow oRow = new TableRow(this);
 				oTableLayout.addView(oRow);
 				TextView oText = new TextView(this);
 				oText.setText(myPrefs.getString("Ingredient"+i, "DEFAULT VALUE"));
 				lIngredientlist.put(i, oText);
 
 				oRow.setGravity(Gravity.RIGHT);
 				oRow.addView(oText);
 				oRow.addView(oDelete);
 				oText.setGravity(Gravity.LEFT);
 				lRowsIngredient.put(i, oRow);
 				iNbingredients++;
 				i++;
 				Log.v("LogTest","Ingredient"+i + ": Ingredient"+iNbingredients + myPrefs.getString("Ingredient"+iNbingredients, "DEFAULT VALUE"));
 				/*if(myPrefs.getString("Ingredient"+i, "DEFAULT VALUE") != "DEFAULT VALUE")
 				{
 					Log.v("LogTest",myPrefs.getString("Ingredient"+i, "DEFAULT VALUE"));
 					Toast.makeText(this, myPrefs.getString("Ingredient"+i, "DEFAULT VALUE"), Toast.LENGTH_LONG).show();
 				}*/
 			}
     	    return true;
 	    
 	    //Main Button click
     	case R.id.itemMain:
     		super.onCreate(savedInstanceState);
 	        setContentView(R.layout.main);
 	        Button scanButton = (Button) findViewById(R.id.scanButton);
 	        scanButton.setOnClickListener(this);
 	        
 	        return true;
     	    
     	//Quit Button click
     	case R.id.itemQuit:
     		finish();
     		return true;
     	}
     	return false;
     }
     
 	public void onClick(View v) {
 		if(v.getId() == R.id.buttonValidate){
 			
 			//Save profile informations
 			try{
 		        SharedPreferences myPrefs = this.getSharedPreferences("ingredient", MODE_WORLD_READABLE);
 	    	    SharedPreferences.Editor prefsEditor = myPrefs.edit();
 	    	    prefsEditor.clear();
 	    	    prefsEditor.commit();
 	    	    
 	    	    
 				Set cles = lIngredientlist.keySet();
 				Iterator it = cles.iterator();
 				int iIdIngredient = 0 ;
 				while (it.hasNext()){
 				   Object cle = it.next(); 
 				   TextView oText = lIngredientlist.get(cle);
 				  
 					//sauvegarde
 					//SharedPreferences myPrefs = this.getSharedPreferences("ingr�dient", MODE_WORLD_READABLE);
 		    	    prefsEditor.putString("Ingredient"+iIdIngredient,  oText.getText().toString());
 		    	    
 		    	    /*On Parcours le tableau pour trouver l'uri de oText.getText().toString()*/
 		    	    String sUri = "TestingOnetwo";
 		    	    prefsEditor.putString("Uri"+iIdIngredient,  sUri);
 		    	    
 		    	    /**************************/
 		    	    
 		    	    
 		    	    Log.d("SAVED INGREDIENT", oText.getText().toString());
 		    	    Log.println(Log.VERBOSE, "SAVED INGREDIENT", "Un ingredient sauvegardé. Indice " + iIdIngredient);
 		    	    prefsEditor.commit();   
 		    	    iIdIngredient++;
 				}
 				Toast.makeText(this, "Sauvegarde effectué avec succès", 4000).show();
 			
 			}catch(Exception saveException){
 				Toast.makeText(this, "Une erreur s'est produite durant la sauvegarde : " + saveException.getMessage(), 7000).show();
 			}
 			/*super.onCreate(savedInstanceState);
 			
 			setContentView(R.layout.main);
 	        Button scanButton = (Button) findViewById(R.id.scanButton);
 	        scanButton.setOnClickListener(this);*/
 	        	        
 		}else if(v.getId() == R.id.scanButton)
 		{
 			try{
 				/*Intent intent = new Intent("com.google.zxing.client.android.SCAN");
 		        intent.setPackage("com.google.zxing.client.android");
 		        intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
 		        startActivityForResult(intent, 0);*/
 				IntentIntegrator.initiateScan(this);
 			}catch(Exception e){
 				Toast.makeText(this, e.getMessage(), 4000).show();
 			}
 		}else if(v.getId() == R.id.addInfoButton){
 			Log.v("LogTest", "Ajout d'un bouton !"+ v.getId());
 			try{
 				/*//Cr�ation du bouton valider
 				Button oValidate = new Button(this);
 				oValidate.setOnClickListener(this);
 				oValidate.setId(1337);
 				oValidate.setText("Validate");*/
 
 				TableRow oRow2 = new TableRow(this);
 				TableLayout oTableLayout = (TableLayout) findViewById(R.id.tablelayout);
 				oTableLayout.addView(oRow2);
 				
 				AutoCompleteTextView textView = (AutoCompleteTextView) findViewById(R.id.autocomplete_country);
 				String sAutocompleteText= textView.getText().toString();
 				ImageButton oDelete = new ImageButton(this);
 				oDelete.setOnClickListener(this);
 				oDelete.setId(i);
 				oDelete.setImageResource(R.drawable.delete_icon);
 				oDelete.setTag("X");
 				TableRow oRow = new TableRow(this);
 				oTableLayout.addView(oRow);
 				TextView oText = new TextView(this);
 				oText.setText(sAutocompleteText);
 				int iXRowLeft = oRow.getLeft();
 				//oText.setWidth(250);
 				//i=lRowsIngredient.size();
 				//oRow.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.FILL_PARENT));
 				//oText.setLayoutParams(new TableLayout.setGravity(Gravity.RIGHT));
 				//oRow.add
 				//oText.addView(oText, 300, 20);
 				oRow.setGravity(Gravity.RIGHT);
 				//oRow.setGravity(Gravity.CENTER_VERTICAL);
 				oRow.addView(oText);
 				oRow.addView(oDelete);
 				oText.setGravity(Gravity.CENTER);
 				textView.setText("");
 				lRowsIngredient.put(i, oRow);
 				lIngredientlist.put(i, oText);
 				i++;
 				//oRow2.addView(oValidate);
 			}catch(Exception e){
 	    		AlertDialog alert = new AlertDialog.Builder(this).create();
 	    		alert.setMessage(e.toString());
 	    		alert.setTitle("Bouh");
 	    		alert.show();
     		}
 		}
 		else if(((ImageButton)v).getTag().equals("X"))
 		{
 			try{
 				Log.v("LogTest","Tente de supprimer le row "+ v.getId());
 				TableLayout oTableLayout = (TableLayout) findViewById(R.id.tablelayout);
 				oTableLayout.removeView(lRowsIngredient.get(v.getId()));
 				int iNbFils = lRowsIngredient.get(v.getId()).getChildCount();
 				int x=0;
 				while(x < iNbFils)
 				{
 					lRowsIngredient.get(v.getId()).getChildAt(x).setVisibility(0);
 					if(lIngredientlist.containsKey(lRowsIngredient.get(v.getId()).getChildAt(x).getId())){
 						lIngredientlist.remove(lRowsIngredient.get(v.getId()).getChildAt(x).getId());
 					}else
 					{
 						x++;
 					}
 				}
 				lRowsIngredient.remove(v.getId());
 			}catch(Exception e){
 	    		AlertDialog alert = new AlertDialog.Builder(this).create();
 	    		alert.setMessage(e.toString());
 	    		alert.setTitle("Bouh ! C'est moi !");
 	    		alert.show();
 	    	}
 		}
 	}
 
 	public void onActivityResult(int reqCode, int resCode, Intent intent) {
 		IntentResult scanResult = IntentIntegrator.parseActivityResult(reqCode, resCode, intent);
 		if (scanResult != null && RESULT_CANCELED != resCode) {
             String contents = scanResult.getContents();
             Toast.makeText(this, "Succès : " + contents, 2000).show();
         } else if (RESULT_CANCELED == resCode) {
             Toast.makeText(this, "Scan annulé", 2000).show();
         }
     }
 
 	
 }
