 package org.movshovich.msaver;
 
 
 import java.sql.SQLException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ListActivity;
 import android.content.DialogInterface;
 import android.database.Cursor;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.widget.SimpleCursorAdapter;
 import android.support.v4.widget.SimpleCursorAdapter.CursorToStringConverter;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.AutoCompleteTextView;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.FilterQueryProvider;
 import android.widget.Spinner;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.j256.ormlite.android.AndroidDatabaseResults;
 import com.j256.ormlite.dao.CloseableIterator;
 import com.j256.ormlite.dao.Dao;
 import com.j256.ormlite.dao.GenericRawResults;
 import com.j256.ormlite.stmt.QueryBuilder;
 
 public class ExpensesFragment extends Fragment {
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		View view = inflater.inflate(R.layout.expenses, container, false);
 		addListeners(view);
 		updateBalance(view);
 		showList(view);
		//TODO: choose  date
 		TextView dateView = (TextView) view.findViewById(R.id.expenseDate);		
 		SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");		
 		dateView.setText(sdf.format(new Date()));
 		return view;
 	}
 
 	private void showList(View view) {
 		QueryBuilder<Transaction, Integer> qb = MainActivity.databaseHelper
 				.getTransactionDao().queryBuilder();
 		List<Transaction> expenses;
 		try {
 			qb.orderBy("date", false).limit(5L);
 			expenses = qb.where().lt("price", 0).query();
 			for (Transaction e : expenses) {
 				MainActivity.databaseHelper.getProductDao().refresh(e.getProduct());
 			}
 		} catch (SQLException e1) {
 			Log.w("MSaver", e1);
 			return;
 		}
 		TableLayout tl = (TableLayout) view.findViewById(R.id.last_buys);
 
 		String sum;
 		int rowIdx = 0;
 		for (Transaction e : expenses) {
 			TableRow row = (TableRow) tl.getChildAt(rowIdx);
 			TextView productText = (TextView) row.getChildAt(0);
 			TextView priceText = (TextView) row.getChildAt(1);
 			productText.setText(e.getProduct().getName());
 			sum = addingDotToString(Integer.toString(e.getPrice()));
 			priceText.setText(sum);
 			rowIdx += 1;
 		}
 
 	}
 
 	private void addListeners(final View view) {
 		Dao<Product, Integer> dao = MainActivity.databaseHelper.getProductDao();
 		try {
 			for (Product p: dao.queryBuilder().query()) {
 				MainActivity.databaseHelper.getCategoryDao().refresh(p.getCategory());
 			}
 		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
 		}
 		Button addButton = (Button)  view.findViewById(R.id.expenseAdd);
 		if (addButton != null) {
 			addButton.setOnClickListener(new View.OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					onAddClick(view);
 				}
 			});
 		}
 		final EditText price = (EditText) view.findViewById(R.id.expensePriceEnter);
 		
 		price.addTextChangedListener(new TextWatcher() {
 			@Override
 			public void onTextChanged(CharSequence s, int start, int before, int count) {
 				price.setTextColor(Color.BLACK);
 			}
 			
 			@Override
 			public void beforeTextChanged(CharSequence s, int start, int count,
 					int after) {
 			}
 			
 			@Override
 			public void afterTextChanged(Editable s) {
 			}
 		});
 	
 		AutoCompleteTextView textView = (AutoCompleteTextView) view.findViewById(R.id.expenseProductEnter);
 		SimpleCursorAdapter sca = new SimpleCursorAdapter(view.getContext()
 				, android.R.layout.simple_dropdown_item_1line, null
 				, new String[] {"name"}, new int[] { android.R.id.text1 }, 0);
 		textView.setAdapter(sca);
 		
 		sca.setCursorToStringConverter(new  CursorToStringConverter() {
             public String convertToString(android.database.Cursor cursor) {
                 // Get the label for this row out of the "state" column
                 final int columnIndex = cursor.getColumnIndexOrThrow("name");
                 final String str = cursor.getString(columnIndex);
                 return str;
             }
 		});
 		sca.setFilterQueryProvider(new FilterQueryProvider() {
 			public Cursor runQuery(CharSequence constraint) {
 				// Search for states whose names begin with the specified letters
 				// build your query
 				if (constraint == null) {
 					return null;
 				}
 				Dao<Product, Integer> dao = MainActivity.databaseHelper.getProductDao();
 				QueryBuilder<Product, Integer> qb =  dao.queryBuilder();
 				QueryBuilder<Category, Integer> catQB = MainActivity.databaseHelper.getCategoryDao().queryBuilder();
 				CloseableIterator<String[]> iterator = null;
 				try {
 					catQB.where().not().idEq(MainActivity.INCOME_CAT_ID);
 
 					qb.selectRaw("`products`.`id` as `_id`", "`products`.`name`");
 					qb.where().like("name", constraint.toString() + "%");
 					qb.join(catQB);
 					String prepareStatementString = qb.prepareStatementString();
 					Log.d("MSaver", qb.prepareStatementString());
 					GenericRawResults<String[]> rawRes = dao.queryRaw(prepareStatementString);
 					iterator = rawRes.closeableIterator();
 				} catch (SQLException e) {
 					Log.w("MSaver", e);
 					return null;
 				}
 				AndroidDatabaseResults results =
 						(AndroidDatabaseResults)iterator.getRawResults();
 				return results.getRawCursor();
 			}
 		});
 
 	}
 
 	private void onAddClick(final View view) {
 		final EditText producttext = (EditText) view.findViewById(R.id.expenseProductEnter);
 		final EditText pricetext = (EditText) view.findViewById(R.id.expensePriceEnter);
 			
 		String coinPrice = pricetext.getText().toString();
 		String price = coinPrice;
 		if (producttext.getText().length() == 0  || price.isEmpty()){
 			return;
 		}
 		final Transaction e = new Transaction();
 
 		if (isNumeric(coinPrice)) {
 			
 			int position = coinPrice.indexOf(".");
 			int length = coinPrice.length();
 			int factor = 1;
 			if (position == -1 ){
 				factor = 100;
 			}else if (length - position == 2){
 				factor = 10;
 			}else if (length - position == 3){
 				factor = 1;
 			} 
 			coinPrice = coinPrice.replaceAll("\\.", ""); 
 			e.setPrice (-Integer.parseInt(coinPrice) * factor); 					
 
 		}else{
 			pricetext.setTextColor(Color.RED);
 			return;
 		}	
 
 		QueryBuilder<Product, Integer> qb = MainActivity.databaseHelper
 				.getProductDao().queryBuilder();
 		QueryBuilder<Category, Integer> catQB = MainActivity.databaseHelper.getCategoryDao().queryBuilder();
 		List<Product> products;
 		try {
 			catQB.where().not().idEq(MainActivity.INCOME_CAT_ID);
 			qb.where().eq("name", producttext.getText().toString());
 			products = qb.join(catQB).query();
 			
 		} catch (SQLException e1) {
 			Log.w("MSaver", e1);
 			return;
 		}
 		Date currentDate = new Date();
 		e.setDate(currentDate);
 
 		final Product p;
 		if (products.isEmpty()) {
 			p = new Product();
 			p.setName(producttext.getText().toString());
 			
 			LayoutInflater inflater = getActivity().getLayoutInflater();
 			final View popup = inflater.inflate(R.layout.category, null, false);
 			final Spinner spinner  = (Spinner) popup.findViewById(R.id.categories_spinner);
 			final List <Category> categories;
 			List <String> nameList = new ArrayList<String>();
 			nameList.add("New Category");
 			final Dao<Category, Integer> catDao = MainActivity.databaseHelper.getCategoryDao();
 			QueryBuilder<Category, Integer> qbCat =  catDao.queryBuilder();
 			try {
 				categories = qbCat.where().not().idEq(MainActivity.INCOME_CAT_ID).query();
 			} catch (SQLException se) {
 				Log.w("MSaver", se);
 				return;
 			}
 			for (Category c : categories) {
 				nameList.add(c.getName());
 			}
 			
 			ArrayAdapter<String> adapter = new ArrayAdapter<String>(view.getContext()
 					, android.R.layout.simple_spinner_item, nameList);
 			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 			spinner.setAdapter(adapter);
 			spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
 
 				@Override
 				public void onItemSelected(AdapterView<?> parent, View viewSelected,
 						int position, long id) {
 					popup.findViewById(R.id.categoryEditText).setVisibility(
 							position == 0 ? View.VISIBLE : View.INVISIBLE);
 				}
 
 				@Override
 				public void onNothingSelected(AdapterView<?> arg0) {
 				}
 			});
 			//-- nachalo dialog
 			AlertDialog.Builder db = new AlertDialog.Builder(popup.getContext());
 			db.setTitle("Categories");
 			db.setView(popup);
 			db.setPositiveButton("Done", new 
 			    DialogInterface.OnClickListener() {
 			        public void onClick(DialogInterface dialog, int which) {
 			        	int pos = spinner.getSelectedItemPosition();
 			        	if (pos > 0) {
 			        		p.setCategory(categories.get(pos - 1));
 			        	} else {
 			        		TextView catNameView = (TextView) popup.findViewById(R.id.categoryEditText);
 			        		String catName = catNameView.getText().toString();
 			        		if (catName.isEmpty() || catName.matches("^\\s+$")) {
 			        			Toast.makeText(view.getContext(), "Category Name cannot be empty!"
 			        						, Toast.LENGTH_SHORT).show();
 			        			return;
 			        		}
 			        		Category cat = new Category();
 			        		cat.setName(catName);
 			        		try {
 								catDao.create(cat);
 							} catch (SQLException e) {
 			        			Toast.makeText(view.getContext(), "Internal Error"
 		        						, Toast.LENGTH_LONG).show();
 								Log.w("MSaver", e);
 								return;
 							}
 			        		p.setCategory(cat);
 			        	}
 			        	try {
 			        		MainActivity.databaseHelper.getProductDao().create(p);
 			        	} catch (SQLException e) {
 			        		Toast.makeText(view.getContext(), "Internal Error"
 			        				, Toast.LENGTH_LONG).show();
 			        		Log.w("MSaver", e);
 			        		return;
 			        	}
 			        	finalizeTransaction(view, producttext, pricetext, e, p);
 			        }
 			});
 			db.setCancelable(true);
 			db.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 				}
 			});
 			db.show();
 		} else {
 			p = products.get(0);
 			finalizeTransaction(view, producttext, pricetext, e, p);
 		}
 	}
 
 	private void finalizeTransaction(final View view, EditText producttext,
 			EditText pricetext, Transaction e, final Product p) {
 		e.setProduct(p);
 		
 		try {
 			MainActivity.databaseHelper.getTransactionDao().create(e);
 		} catch (SQLException e1) {
 			Toast.makeText(view.getContext(), "Internal Error"
 					, Toast.LENGTH_LONG).show();
 			Log.w("MSaver", e1);
 			return;
 		}
 		
 		updateBalance(view);
 		showList(view);
 		producttext.getText().clear();
 		pricetext.getText().clear();
 	}
 	
 	private boolean isNumeric(String str) {
 	  return str.matches("\\d*\\.?\\d{1,2}"); 
 	}
 	
 	public void updateBalance(View view) {
 		String sum = "0";
 		try {
 			Dao<Transaction, Integer> tDao = MainActivity.databaseHelper
 					.getTransactionDao();
 			QueryBuilder<Transaction, Integer> qb = tDao.queryBuilder();
 			qb.selectRaw("sum(price)");
 			GenericRawResults<String[]> qRes = tDao.queryRaw(qb.prepareStatementString());
 			
 			sum = qRes.getFirstResult()[0];
 		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
 		}
 		if (sum != null) {
 			TextView sumview = (TextView) view
 					.findViewById(R.id.expenseBalance);
 			sum = addingDotToString(sum);
 			sumview.setText(sum);
 			if (sum.charAt(0) == '-') {
 				sumview.setBackgroundColor(Color.RED);
 			} else {
 				sumview.setBackgroundColor(Color.GREEN);
 			}
 		}else {
 			sum = "0";
 		}
 	
 	}
 
 	public String addingDotToString(String num) {
 		if ("0".equals(num) || num.isEmpty()) {
 			return num;
 		}
 		boolean neg = num.charAt(0) == '-';
 		String prepend = "";
 		if (neg) {
 			num = num.substring(1);
 			prepend = "-";
 		}
 		int lenNum = num.length();
 		if (lenNum == 1) {
 			return new StringBuilder().append(prepend).append("0.0").append(num).toString();
 		} else if (lenNum == 2) {
 			return new StringBuilder().append(prepend).append("0.").append(num).toString();
 		} else {
 			return new StringBuilder().append(prepend).append(num.substring(0, lenNum-2))
 						.append('.').append(num.substring(lenNum - 2)).toString();
 		}
 	}
 	
 }
