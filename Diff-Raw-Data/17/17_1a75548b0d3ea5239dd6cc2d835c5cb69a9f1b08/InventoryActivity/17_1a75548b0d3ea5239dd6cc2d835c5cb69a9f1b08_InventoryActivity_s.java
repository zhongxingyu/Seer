 package edu.upenn.cis350.project;
 
 import java.util.HashMap;
 
 import com.parse.Parse;
 import com.parse.ParseObject;
 import com.parse.PushService;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.text.Editable;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class InventoryActivity extends Activity {
 	
 	int[] fruitQtys;
 	/* table to store quantity of each item
 	 * 0 - apple
 	 * 1 - banana
 	 * 2 - grapes
 	 * 3 - kiwi
 	 * 4 - orange
 	 * 5 - pear
 	 * 6 - granola
 	 * 7 - frozen fruit
 	 * 8 - mixed bags
 	 * 9 - smoothie
 	 */
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_inventory);
 		
 		fruitQtys = new int[10];
 	}
 	
 	public void qtyClicked(View view) {
 		switch(view.getId()) {
 		case R.id.applePlus:
 			changeQty(true, 0, R.id.appleQty);
 			break;
 		case R.id.appleMinus:
 			changeQty(false, 0, R.id.appleQty);
 			break;
 		case R.id.bananaPlus:
 			changeQty(true, 1, R.id.bananaQty);
 			break;
 		case R.id.bananaMinus:
 			changeQty(false, 1, R.id.bananaQty);
 			break;
 		case R.id.grapesPlus:
 			changeQty(true, 2, R.id.grapesQty);
 			break;
 		case R.id.grapesMinus:
 			changeQty(false, 2, R.id.grapesQty);
 			break;
 		case R.id.kiwiPlus:
 			changeQty(true, 3, R.id.kiwiQty);
 			break;
 		case R.id.kiwiMinus:
 			changeQty(false, 3, R.id.kiwiQty);
 			break;
 		case R.id.orangePlus:
 			changeQty(true, 4, R.id.orangeQty);
 			break;
 		case R.id.orangeMinus:
 			changeQty(false, 4, R.id.orangeQty);
 			break;
 		case R.id.pearPlus:
 			changeQty(true, 5, R.id.pearQty);
 			break;
 		case R.id.pearMinus:
 			changeQty(false, 5, R.id.pearQty);
 			break;
 		case R.id.granolaPlus:
 			changeQty(true, 6, R.id.granolaQty);
 			break;
 		case R.id.granolaMinus:
 			changeQty(false, 6, R.id.granolaQty);
 			break;
 		case R.id.frozenPlus:
 			changeQty(true, 7, R.id.frozenQty);
 			break;
 		case R.id.frozenMinus:
 			changeQty(false, 7, R.id.frozenQty);
 			break;
 		default:
 			throw new RuntimeException("Unknown Button!");
 		}
 	}
 	
 	// method that modifies fruit quantity depending on which button was pressed
	private void changeQty(boolean pm, int fruit, int cid ) {
		int qtyTemp = getQty(cid);
		if (pm) { // increment fruit qty
 			if (qtyTemp < 99) qtyTemp++;
 		} else { // decrement fruit qty
 			if (qtyTemp > 0) qtyTemp--;
 		}
 		
		fruitQtys[fruit] = qtyTemp;
		EditText qtyEdit = (EditText) findViewById(cid);
 		qtyEdit.setText(""+qtyTemp);	
 	}
 	
 	public int getQty (int cid){
 		EditText qtyEdit = (EditText) findViewById(cid);
 		Editable qtyE = qtyEdit.getText();
 		
 		try {
 			return Integer.parseInt(qtyE.toString());
 		} catch (Exception e) {
 			return 0;
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_inventory, menu);
 		return true;
 	}
 	
 	public void continueToInventory2(View v) {
     	//Launch to inventory2
     	Intent i = new Intent(this, Inventory2Activity.class);
     	//Save our info
     	
     	fruitQtys[0] = getQty(R.id.appleQty);
     	fruitQtys[1] = getQty(R.id.bananaQty);
     	fruitQtys[2] = getQty(R.id.grapesQty);
     	fruitQtys[3] = getQty(R.id.kiwiQty);
     	fruitQtys[4] = getQty(R.id.orangeQty);
     	fruitQtys[5] = getQty(R.id.pearQty);
     	fruitQtys[6] = getQty(R.id.granolaQty);
     	fruitQtys[7] = getQty(R.id.frozenQty);
     	
     	i.putExtra("fruit_quantities", fruitQtys);
     	i.putExtras(this.getIntent().getExtras());
     	
     	this.startActivity(i);
     }
 
 }
