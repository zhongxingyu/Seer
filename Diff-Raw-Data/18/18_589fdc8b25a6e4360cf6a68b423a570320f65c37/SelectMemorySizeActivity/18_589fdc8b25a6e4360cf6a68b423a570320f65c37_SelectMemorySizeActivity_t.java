 package de.thm.ateam.memory;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Properties;
 
 import de.thm.ateam.memory.game.PlayerList;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.Spinner;
 
 /**
  * 
  * Activity to edit the Size of the game dimensions
  *
  */
 public class SelectMemorySizeActivity extends Activity implements OnClickListener  {
 	
 	/**
 	 * 
 	 * List for spinner values as static function
 	 * 
 	 * @return ArrayList<String>
 	 * 
 	 */
 	static public ArrayList<String> getList() {
 		ArrayList<String> tmp = new ArrayList<String>();
 		tmp.add("2x2");
 		tmp.add("4x4");
 		tmp.add("6x6");
 		tmp.add("8x8");
 		return tmp;
 	}
 	
 	/**
 	 * 
 	 * Creates content view
 	 * 
 	 * @param Bundle savedInstanceState
 	 * 
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 	  super.onCreate(savedInstanceState);
 	  setContentView(createView());
 	}
 
 	/**
 	 * 
 	 * Generate view to display
 	 * 
 	 * @return View
 	 * 
 	 */
 	private View createView() {
 		
 		// Linear Layout where a Spinner and a Button will be placed
 		LinearLayout linLay = new LinearLayout(this);
 		linLay.setOrientation(LinearLayout.VERTICAL);
 		
 		Spinner spin = new Spinner(this);
 		spin.setId(5);
 
     ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, getList());
 		spin.setAdapter(adapter);
 		
 		spin.setSelection(find(PlayerList.getInstance().row, PlayerList.getInstance().col));
 		
 		Button but = new Button(this);
 		but.setText("Save");
 		but.setId(6);
 		but.setOnClickListener(this);
 		
 		linLay.addView(spin);
 		linLay.addView(but);
 		
 		return linLay;
   }
 
 	/**
 	 * 
 	 * When button was clicked
 	 * 
 	 * @param View v
 	 * 
 	 */
 	public void onClick(View v) {
 	  switch(v.getId()) {
 	  case 6:
 	  	// When button clicked pick up the selected item position
 	  	Spinner tmp = (Spinner)findViewById(5);
 	  	String value = (String)tmp.getSelectedItem();
 	  	
 	  	int row, col;
 	  	col = Integer.parseInt(value.split("x")[0]);
 	  	row = Integer.parseInt(value.split("x")[1]);
 	  	
 	  	// Load config file to store the changes
 	  	Properties configFile = new Properties();
 			try {
 		    configFile.load(new FileInputStream(new File(getFilesDir() + "config.properties")));
 	    } catch (IOException e) {
 		    e.printStackTrace();
 	    }
 
 			configFile.setProperty("row", String.valueOf(row));
 			configFile.setProperty("col", String.valueOf(col));
 			
 			// Also set row and col for the global PlayerList used for the Memory field dimensions
 			PlayerList.getInstance().row = row;
 			PlayerList.getInstance().col = col;
 			
 			try {
 	      configFile.store(new FileOutputStream(new File(getFilesDir() + "config.properties")), null);
       } catch (Exception e) {
 	      e.printStackTrace();
       }
 			finish();
 	  	break;
   	default:
   		break;
 	  }
   }
 	
 	/**
 	 * Function to get the position of a dimensions for the spinner
 	 * 
 	 * @param int row
 	 * @param int col
 	 * @return int
 	 */
 	private int find(int row, int col) {
 		ArrayList<String> tmp = getList();
 		for(int i = 0; i < tmp.size(); i++) {
 			if(tmp.get(i).equals(String.valueOf(row) + "x" + String.valueOf(col))) {
 				return i;
 			}
 		}
 		return 0;
 	}
 }
