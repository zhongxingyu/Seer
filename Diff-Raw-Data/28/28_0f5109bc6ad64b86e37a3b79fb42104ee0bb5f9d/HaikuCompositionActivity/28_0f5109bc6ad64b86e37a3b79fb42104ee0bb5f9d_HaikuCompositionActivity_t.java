 /**
  * @author Jesper Josefsson
  */
 
 package com.ohhaiku;
 
 import java.sql.SQLException;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
 import com.j256.ormlite.dao.Dao;
 import com.ohhaiku.database.DatabaseHelper;
 import com.ohhaiku.models.Haiku;
 import com.ohhaiku.models.Poem;
 import com.ohhaiku.utility.Constants;
 
 /*
  * Main activity: shows the Haiku composition window.
  */
 public class HaikuCompositionActivity extends OrmLiteBaseActivity<DatabaseHelper> {
   private static final String logTag = "HaikuComposition";
   private static final int DEFAULT_VALUE = -1;
   
   /*
    * This variable is only set with a persisted poem.
    * Used to enable updates of poems
    */
   private Poem currentPoem;
 
   /** Called when the activity is first created. */	
 	@Override
   public void onCreate(Bundle savedInstanceState) {
   	super.onCreate(savedInstanceState);
     setContentView(R.layout.main);
    resetStatusImage();
   }
 
 	@Override
 	protected void onRestart(){
 	  super.onRestart();
 	  if(isCurrentPoemSaved()){
 	    setPersistButtonText(getString(R.string.update_button_title));
 	  }
 	  else{
       setPersistButtonText(getString(R.string.save_button_title));
 	  }  
 	}
 
 	/*
 	 * Checks if the current poem has been persisted.
 	 */
 	private boolean isCurrentPoemSaved() {
 	  if (currentPoem == null) {
 	    return false;
 	  } else {
   	  try {
         Dao<Poem, Integer> dao = getHelper().getPoemDao();
         return dao.idExists(currentPoem.getId());
       } catch (SQLException e) {
         return false;
       }
 	  }
   }
 
   /**
 	 * Click handler for the menu button
 	 * Starts the menu, possibly expecting a result in the form of Poem
 	 */
   public void onGoToMenu(View view) {
     startActivityForResult(new Intent(this, MenuActivity.class), Constants.LOAD_HAIKU);
 	}
   
   /**
    * Click handler for the Check Haiku button
    */
   public void onCheck(View view) {
     check();
   }
   
   /*
    * Private method to make it possible to check from other sources than click handlers
    */
   private void check() {
     Poem p = new Poem(getLines());
     Haiku h = new Haiku(p);
     
     setValidRows(h);
     ImageView image = (ImageView) this.findViewById(R.id.haiku_status);
    setStatusImage(image, h.isValid());
   }
   
   /*
    * Sets Certified/Not valid image
    */
  private void setStatusImage(ImageView m, boolean valid){
	  m.setVisibility(View.VISIBLE);

	  if (valid){
 		  m.setImageResource(R.drawable.certified_icn);
 	  }
 	  else {
 		  m.setImageResource(R.drawable.not_valid_icn);
	  }  
   }
  
  // Makes haiku_status image invisible
   private void resetStatusImage() {
 	  ImageView m = (ImageView) this.findViewById(R.id.haiku_status);
	  m.setVisibility(View.INVISIBLE);
   }
   
   /*
    * Returns three textViews corresponding to the three lines of the Haiku
    */
   private TextView[] getTextViews() {
     TextView[] views = new TextView[3];
     
     // Fetch textviews containing the poem lines
     views[0] = (TextView) this.findViewById(R.id.editTextRow1);
     views[1] = (TextView) this.findViewById(R.id.editTextRow2);
     views[2] = (TextView) this.findViewById(R.id.editTextRow3);
     
     return views;
   }
   
   /*
    * Returns an array of Strings, representing the three lines of the Haiku
    */
   private String[] getLines() {
     TextView[] views = getTextViews();
     
     String[] lines = new String[3];
     
     for(int i = 0; i < 3; i++) {
       lines[i] = views[i].getText().toString();
     }
     
     return lines;
   }
   
   /*
    * Sets the three lines of the Haiku
    */
   private void setLines(String[] lines) {
     TextView[] views = getTextViews();
     for (int i = 0; i < 3; i++) {
       views[i].setText(lines[i]);
     }
   }
 
   
   /*
    * Fetches the Haiku from the composition view and attempts to persist it.
    */
 	private void saveHaiku() {
 	  currentPoem = new Poem(getLines());
 	  // Try to persist poem
 	  try {
 	    Dao<Poem, Integer> poemPersister = getHelper().getPoemDao();
       poemPersister.create(currentPoem);
       setPoem(currentPoem);
       setToast(getString(R.string.save_succeeded_text));
     } catch (SQLException e) {
       Log.e(logTag, "Could not save Haiku", e);
       setToast(getString(R.string.save_failed_text));
     }
 	}
 
 
 	private void setValidRows(Haiku h){
 	   for (int i=0;i<3;i++) {
 	    	 boolean validrow = h.isValidRow(i);
 	    	 if (validrow){
 	    		 setValidRowImage(i);
 	    	 }
 	    	 else {
 	    		 setDefaultRowImage(i);
 	    	 } 
 	    }
 	}
 
 	private void setDefaultRowImage(int row) {
 	  getRowImageViews()[row].setImageResource(R.drawable.grey_icn);
 	}
 
 	private ImageView[] getRowImageViews() {
 	  return new ImageView[] {
 	      (ImageView) findViewById(R.id.row1_status),
 	      (ImageView) findViewById(R.id.row2_status),
 	      (ImageView) findViewById(R.id.row3_status)
 	  };
 	}
 
 	private void setValidRowImage(int row) {
 		if (row==0){
 			ImageView image = (ImageView) this.findViewById(R.id.row1_status);
 	        image.setImageResource(R.drawable.line1_icn);	
 		}
 		if (row==1){
 			ImageView image = (ImageView) this.findViewById(R.id.row2_status);
 	        image.setImageResource(R.drawable.line2_icn);	
 		}
 		if (row==2){
 			ImageView image = (ImageView) this.findViewById(R.id.row3_status);
 	        image.setImageResource(R.drawable.line3_icn);	
 		}
 
 	}//sets the image of every row
 
 
 	/*
 	 * Sets the status text view.
 	 */
   private void setStatus(String status) {
     TextView statusView = (TextView) this.findViewById(R.id.statusText);
     statusView.setText(status);
   }
   
   /*
    * Called when the Menu activity passes on a Haiku from the BrowseSavedHaikus Activity
    * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
    */
   @Override
   protected void onActivityResult(int requestCode, int resultCode, Intent data) {
     if (resultCode == Activity.RESULT_OK && requestCode == Constants.LOAD_HAIKU) {
       int id = data.getIntExtra(Constants.HAIKU_ID, DEFAULT_VALUE);
       if (id != DEFAULT_VALUE) {
         loadHaiku(id);
       } else {
         setToast(getString(R.string.haiku_load_failed_text));
       }
     }
   }
   
   /*
    * Fetches a haiku from the database by ID
    */
   private void loadHaiku(int id) {
     try {
       Dao<Poem, Integer> dao = getHelper().getPoemDao();
       Poem p = dao.queryForId(id);
       setPoem(p);
       setToast(getString(R.string.haiku_loaded_text));
     } catch (SQLException e) {
       setToast(getString(R.string.haiku_load_failed_text));
     }
   }
   
   /*
    * Sets and the current poem.
    * This will set the instance variable and render the Haiku in the GUI
    */
   private void setPoem(Poem poem) {
     this.currentPoem = poem;
     setLines(poem.getLinesAsArray());
     setPersistButtonText(getString(R.string.update_button_title));
     check();
   }
   
   /**
    * Click handler for the clear button
    */
   public void onClear(View v) {
     currentPoem = null;
     setLines(new String[] {"", "", ""});
     setPersistButtonText(getString(R.string.save_button_title));
     resetStatusImage();
     for (int i=0;i<3;i++) {
     	setDefaultRowImage(i);
     }
    resetStatusImage();
   }
 
   /*
    * Sets the text of the persist button. Used toggle between "Update" and "Save"
    */
   private void setPersistButtonText(String text) {
     Button b = (Button) findViewById(R.id.PersistButton);
     if (b != null) {
       b.setText(text);
     }
   }
   
   /**
    * Click handler for the Save/Update button
    * Checks if there is a current haiku. If no - saves it. If yes - checks if it needs to created or updated.
    * Triggers check.
    */
   public void onPersist(View v) {
     if (!rowsAreEmpty()) {
       check();
       if (currentPoem == null) {
         saveHaiku();
       } else {
         currentPoem.setLines(getLines());
         createOrUpdate();
       }
     }
     else{
       setToast(getString(R.string.empty_rows_text));
     }
         
   }
   
   /*
    * Takes the current poem, updates it or creates it, sets status and persist button text.
    */
   private void createOrUpdate() {
     try {
       Dao<Poem, Integer> dao = getHelper().getPoemDao();
       Dao.CreateOrUpdateStatus status = dao.createOrUpdate(currentPoem);
       if (status.isCreated()) {
         setToast(getString(R.string.save_succeeded_text));
       } else {
         setToast(getString(R.string.updated_haiku_text));
       }
       setPersistButtonText(getString(R.string.update_button_title));
       } catch (SQLException e) {
         setToast("Could not save or update poem");
     }
   } 
   
   private void setToast(String msg){
     Toast.makeText(this, msg, Toast.LENGTH_LONG).show(); 
   }
   
   private boolean rowsAreEmpty()
   {
     String[] lines = getLines();
     return (lines[0].trim().length() == 0 && lines[1].trim().length() == 0 && lines[2].trim().length() == 0);
   }
 }//HaikuCompositionActivity
