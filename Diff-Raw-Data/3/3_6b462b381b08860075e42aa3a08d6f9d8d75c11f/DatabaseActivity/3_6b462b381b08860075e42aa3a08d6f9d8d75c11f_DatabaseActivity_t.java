 package com.github.barcodescanner.activities;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.github.barcodescanner.R;
 import com.github.barcodescanner.R.drawable;
 import com.github.barcodescanner.R.layout;
 import com.github.barcodescanner.core.DatabaseHelper;
 import com.github.barcodescanner.core.DatabaseHelperFactory;
 import com.github.barcodescanner.core.Product;
 
 import android.app.Activity;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.view.Gravity;
 import android.view.Window;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 
 public class DatabaseActivity extends Activity {
 
 	@SuppressWarnings("unused")
 	private static final String TAG = "DatabaseActivity";
 	DatabaseHelper db;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_database);
 
 		DatabaseHelperFactory.init(this);
 		db = DatabaseHelperFactory.getInstance();
 		
 		List<Product> products = db.getProducts();
 		List<String> tempList = new ArrayList<String>();
 		if (products.size() > 0) {
 			for (int i = 0; i < products.size(); i++) {
 				tempList.clear();
 
 				tempList.add(products.get(i).getBarcode());
 				tempList.add(products.get(i).getName());
 				tempList.add(Integer.toString(products.get(i).getPrice()));
 
 				addRow(tempList);
 			}
 		}
 
 	}
 
 	private void addRow(List<String> values) {
 		TableLayout table = (TableLayout) findViewById(R.id.products_table);
 		TableRow row = new TableRow(this);
 		row.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
 				LayoutParams.WRAP_CONTENT));
 		TextView textView;
 		for (int i = 0; i < values.size(); i++) {
 			textView = generateCell(values.get(i).toString());
 			row.addView(textView);
 		}
 		table.addView(row, new TableLayout.LayoutParams(
 				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
 	}
 
 	private void removeRow() {
 		// TODO
 	}
 
 	private TextView generateCell(String text) {
 		TextView nameView = new TextView(this);
 		nameView.setText(text);
 		nameView.setGravity(Gravity.CENTER_HORIZONTAL);
 		nameView.setTypeface(null, Typeface.BOLD);
		//nameView.setLayoutParams(new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT, 1f));
 		return nameView;
 	}
 }
