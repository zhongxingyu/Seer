 package com.WordFinder;
 
 import android.content.Intent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.app.Activity;
 import android.os.Bundle;
 
 // -------------------------------------------------------------------------
 /**
  * Main application activity.
  *
  * @author John Mooring (jmooring)
  * @version Oct 29, 2011
  */
 public class WordFinderActivity
     extends Activity
 {
     /**
      * Called when the activity is first created.
      */
     private static WordFinderActivity instance;
     private LetterGrid grid;
     private LetterGridView board;
     private final int GRID_SIZE = 4;
 
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         board = (LetterGridView)findViewById(R.id.letterGrid);
         grid = new LetterGrid();
         grid.loadRandom(GRID_SIZE);
         board.setModel(grid);
         instance = this;
        WordSolver.getInstance().loadDictionary();
 
     }
 
 
     /**
      * Constructs the context menu
      *
      * @returns boolean true to show menu
      */
     public boolean onCreateOptionsMenu(Menu menu)
     {
         menu.add(0, 0, 0, "About");
         return true;
     }
 
 
     /**
      * Called when a menu item is selected
      *
      * @returns boolean false to continue normal processing of event, true to
      *          consume it here
      */
     public boolean onOptionsItemSelected(MenuItem i)
     {
         switch (i.getItemId())
         {
             case 0:
                 startActivity(new Intent(this, About.class));
                 break;
         }
         return true;
     }
 
 
     /**
      * Returns an instance of this activity so classes can get context
      * @return instance of this activity
      */
     public static WordFinderActivity getInstance()
     {
         return instance;
     }
 }
