 package mobilepos.activity;
 
 import java.util.List;
 
 import mobilepos.domain.Item;
 import mobilepos.domain.MockupInventory;
 
 import com.example.mobilepos.R;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.TextView;
 
 public class InventoryAddMoreItemActivity extends Activity {
 	/** name of product */
 	private EditText itemName;
 	/** type of product quantity */ 
 	private EditText itemQntyType;
 	/** brand of product */
 	private EditText itemBrand;
 	/** cost of product */
 	private EditText itemPrice;
 	/** cost of one product per box */
 	private EditText itemPiecePerBox;
 	/** id of product */
 	private EditText itemProductId;
 	/** cost of product per box */
 	private EditText itemBuyPricePerBox;
 	
 	/** product quantity group */
 	private RadioGroup itemQntyGroup;
 	
 	/** text view of product */
 	private TextView itemBuyPriceCal;
 	/** text view to enter cost of product */
 	private TextView itemBuyPriceText;
 	/** text view of type of product */
 	private TextView itemBuyType;
 	/** text view of cost of product by bath */
 	private TextView itemBuyBahtPerType;
 	
 	/** button to confirm for add product */
 	private Button confirmButton;
 	/** button to cancel product */
 	private Button cancelButton;
 
 	private Item newItem;
 	/** create list of inventory */
 	private List<Item> inventory;
 	/** cost per piece */
 	private String pricePerPiece;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.inventory_additem);
 		inventory = MockupInventory.getInstance();
 		newItem = new Item();
 		createAllFindViewById();
 		setAllEditTextToOneLine();
 		
 		/**
 		 * when select cancel button it will go back to inventory page
 		 */
 		cancelButton.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 				// TODO Auto-generated method stub
 				Intent goInventory = new Intent(getApplicationContext(),
 				MainActivity.class);
 
 				startActivity(goInventory);
 			}
 		});
 		
 		/**
 		 * when select confirm button it will add new item to inventory and go back to inventory page
 		 */
 		confirmButton.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View arg0) {
 				// TODO Auto-generated method stub
 				newItem.setItemId(itemProductId.getText().toString());
 				newItem.setItemName(itemName.getText().toString());
 				newItem.setItemBrand(itemBrand.getText().toString());
 			
 				String totalPiece;
 				totalPiece = calculateTotalPiece(itemQntyType.getText().toString(), itemPiecePerBox.getText().toString());
 				newItem.setItemQnty(totalPiece);
 				
 				String totalPrice;
 				totalPrice = calculateTotalBuyBaht(itemQntyType.getText().toString(),itemBuyPricePerBox.getText().toString());
 				newItem.setItemPrice(itemPrice.getText().toString());
 				
 				pricePerPiece = calculatePricePerPiece(Integer.parseInt(totalPiece), totalPrice);
 	//			updatePricePerPiece(itemPiecePerBox);
 				itemBuyPriceCal.setText("  "+pricePerPiece+"  ");
 				newItem.setItemBuyBahtPerPiece(pricePerPiece);
 				inventory.add(newItem);
 				Intent goInventory = new Intent(getApplicationContext(),
 						MainActivity.class);
 				startActivity(goInventory);
 			}
 		});
 
 		/**
 		 * 
 		 */
 		itemQntyGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
 
 			@Override
 			public void onCheckedChanged(RadioGroup group, int checkedId) {
 				RadioButton checkedRadioButton = (RadioButton) findViewById(checkedId);
 				itemBuyType.setText(checkedRadioButton.getText().toString());
 						
 				if (checkedRadioButton.getText().toString().equals("Piece(s)")) {
 					itemPiecePerBox.setText("non");
 					itemPiecePerBox.setEnabled(false);
 					itemBuyBahtPerType.setText("Baht/Piece :");
 				}
 				else if (checkedRadioButton.getText().toString().equals("Box(es)")) {
 					itemPiecePerBox.setText("");
 					itemPiecePerBox.setEnabled(true);
 					itemBuyBahtPerType.setText("Baht/Box :");
 				}
 			}
 		});
 	}
 	
 	/**
 	 * a = number of box, b = p = cost per box
 	 * @param box is number of total box
 	 * @param piecePerBox is cost per box
 	 * @return a * b or total cost of all box
 	 */
 	public String calculateTotalPiece(String box, String piecePerBox){
 		int a = Integer.parseInt(box), b = Integer.parseInt(piecePerBox);
 		return a*b+"";
 	}
 	
 	/**
 	 * a is box, b is total bath
 	 * @param box is number of total box
 	 * @param totalBaht is total cost in unit baht
 	 * @return a * b or total cost of box in unit baht
 	 */
 	public String calculateTotalBuyBaht(String box,String totalBaht){
 		int a = Integer.parseInt(box),b = Integer.parseInt(totalBaht);
 		return a*b+"";
 	}
 	
 	/**
 	 * create attribute b for convert string of price to integer
 	 * @param piece total quantity of product
 	 * @param price is cost of product
 	 * @return price / piece
 	 */
 	public String calculatePricePerPiece(int piece, String price){
 		double b = Double.parseDouble(price);
 		return b/piece+"";
 	}
 
 	/**
 	 * create all xx = findViewById(xxx);
 	 */
 	public void createAllFindViewById() {
 		//EditText
 		itemName = (EditText) findViewById(R.id.inventory_amp_f_name);
 		itemQntyType = (EditText) findViewById(R.id.inventory_amp_f_buyqntytype);
 		itemBrand = (EditText) findViewById(R.id.inventory_amp_f_brand);
 		itemPrice = (EditText) findViewById(R.id.inventory_amp_f_sellprice);
 		itemPiecePerBox = (EditText) findViewById(R.id.inventory_amp_f_pieceperbox);
 		itemProductId = (EditText) findViewById(R.id.inventory_amp_f_productid);
 		itemBuyPricePerBox = (EditText) findViewById(R.id.inventory_amp_f_buypricebathperbox);
 		
 		//Button
 		confirmButton = (Button) findViewById(R.id.inventory_amp_b_confirm);
 		cancelButton = (Button) findViewById(R.id.inventory_amp_b_cancel);
 		
 		//RadioGroup
 		itemQntyGroup = (RadioGroup) findViewById(R.id.inventory_amp_rbg_buyqnty);
 	
 		//TextView
 		itemBuyPriceCal = (TextView) findViewById(R.id.inventory_amp_t_calbuyprice);
 		itemBuyType = (TextView) findViewById(R.id.inventory_amp_t_buyqntytype);
 		itemBuyPriceText = (TextView) findViewById(R.id.inventory_amp_t_pieceperbox);
 		itemBuyBahtPerType = (TextView) findViewById(R.id.inventory_amp_t_buypricebathpertype);
 	}
 
 	
 	/**
 	 * Set all EditText to be one single line
 	 */
 	public void setAllEditTextToOneLine(){
 		setMaxLineText(itemName);
 		setMaxLineText(itemBrand);
 		setMaxLineText(itemPiecePerBox);
 		setMaxLineText(itemPrice);
 		setMaxLineText(itemQntyType);
 		setMaxLineText(itemProductId);
 		
 	}
 	
 	/**
 	 * update text price per piece
 	 * @param mEditText receive mEditText
 	 */
 	public void updatePricePerPiece(EditText mEditText){
 		mEditText.setOnKeyListener(new View.OnKeyListener() {       
 	        public boolean onKey(View v, int keyCode, KeyEvent event) {
 	            if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
 	            	itemBuyPriceCal.setText("  "+pricePerPiece+"  ");	
 	            }
 	            return false;
 	        }
 		});
 	}
 	
 	/**
 	 * Set EditText to be one single line (cannot press enter to enter the new line) 
 	 */
 	public void setMaxLineText(EditText mEditText){
 		mEditText.setOnKeyListener(new View.OnKeyListener() {
 	        public boolean onKey(View v, int keyCode, KeyEvent event) {
 	        	if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
 	        		String text = ((EditText) v).getText().toString();
 	        		int editTextRowCount = text.split("\\n").length;
 	                if (editTextRowCount >= 1) 
 	                {
 	                    int lastBreakIndex = text.lastIndexOf("\n");
	                    String newText = Integer.toString(lastBreakIndex);
	                    newText = text.substring(0);
 	                    ((EditText) v).setText("");
 	                    ((EditText) v).append(newText);
 	                }
 	            }
 	            return false;
 	        }
 		});
 	}
 }
 	
