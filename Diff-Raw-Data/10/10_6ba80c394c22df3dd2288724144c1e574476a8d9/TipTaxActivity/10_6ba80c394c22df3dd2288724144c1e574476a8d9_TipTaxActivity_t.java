 package com.tiptax;
 
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.Currency;
 import java.util.ListIterator;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.app.ListActivity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.TextView;
import android.widget.Toast;
 
 public class TipTaxActivity extends ListActivity implements OnSharedPreferenceChangeListener {
 
 	private static final int EDITREQ = 1;
 	private static final int NEWREQ = 0;
 
 	private PersonAdapter personAdapter;
 	private ArrayList<Person> personList;
 	private SharedPreferences prefs;
 
 	private double tipPercentage, tax, tip, total;
 	private TextView totalDue, tipDue, taxDue, tipLabel;
 	private NumberFormat numberFormat;
 
 	/*
 	 * Onclick for pressing add a Person
 	 */
 	public void addPersonClick(View v) {
 		Intent i = new Intent(this, AddPersonActivity.class);
 		startActivityForResult(i, NEWREQ);
 	}
 
 	/*
 	 * Onclick for pressing finish
 	 */
 	public void finishClick(View v) {
 
 		Intent i = new Intent(this, FinishActivity.class);
 
 		i.putExtra("persons", personList);
 		i.putExtra("totalTipAndTaxDue", tax + tip);
 		i.putExtra("totalPersonDue", totalSumPeople());
 
 		startActivity(i);
 	}
 
 	/*
 	 * Formats a the TIP string.
 	 */
 	private String formattedTipPctLabel() {
 		return new String("TIP (" + tipPercentage + "%)");
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		if (resultCode == Activity.RESULT_OK) {
 
 			String name = data.getStringExtra("name");
 			String value = data.getStringExtra("value");
 
 			switch (requestCode) {
 			case NEWREQ:
 				personList.add(new Person(name, value));
 				updateCurrency();
 				break;
 			case EDITREQ:
 				int pos = data.getIntExtra("origPos", -1);
 				if (pos != -1) {
 					Person p = personList.get(pos);
 					p.setName(name);
 					p.setValue(value);
 				}
 				break;
 			}
 
 			personAdapter.notifyDataSetChanged();
 			updateTotalAndTipValues();
 		}
 	}
 
 	/*
 	 * Called when pressing delete on an item.
 	 */
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		int id = (int) getListAdapter().getItemId(((AdapterView.AdapterContextMenuInfo) item.getMenuInfo()).position);
 
 		personList.remove(id);
 		personAdapter.notifyDataSetChanged();
 		updateTotalAndTipValues();
 
 		return true;
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		// Set up preferences
 		prefs = PreferenceManager.getDefaultSharedPreferences(this);
 		prefs.registerOnSharedPreferenceChangeListener(this);
 		registerForContextMenu(this.getListView());
 
 		// Set up references to the different textviews
 		totalDue = (TextView) findViewById(R.id.TotalDueText);
 		tipDue = (TextView) findViewById(R.id.tipInput);
 		taxDue = (TextView) findViewById(R.id.taxInput);
 		tipLabel = (TextView) findViewById(R.id.tipText);
 
 		// Set up the numberformatter
 		numberFormat = NumberFormat.getCurrencyInstance();
 		Currency c = Currency.getInstance(prefs.getString("default_currency", "USD"));
 		numberFormat.setMaximumFractionDigits(c.getDefaultFractionDigits());
 		numberFormat.setCurrency(c);
 
 		updateDefaultValues();
 
 		// Set up the listview
 		personList = new ArrayList<Person>();
 		personAdapter = new PersonAdapter(this, R.layout.personrow, personList);
 		setListAdapter(personAdapter);
 	}
 
 	/*
 	 * Context menu setup for the delete command.
 	 */
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
 		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
 
 		menu.setHeaderTitle(personList.get(info.position).getName());
 		menu.add(Menu.NONE, 0, 0, R.string.delete);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.tiptaxmenu, menu);
 		return true;
 	}
 
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		super.onListItemClick(l, v, position, id);
 		Intent i = new Intent(v.getContext(), AddPersonActivity.class);
 		i.putExtra("name", personList.get(position).getName());
 		i.putExtra("value", personList.get(position).getValue());
 		i.putExtra("origPos", position);
 		startActivityForResult(i, EDITREQ);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.pref_menu_btn:
 			Intent i = new Intent(TipTaxActivity.this, TipTaxPreferences.class);
 			startActivity(i);
 			return true;
 		case R.id.reset_menu_btn:
 			resetAll();
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	/*
 	 * Run if the preferences are changed!
 	 */
 	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
 		updateDefaultValues();
 		updateCurrency();
 	}
 
 	/*
 	 * Resets all the fields.
 	 */
 	public void resetAll() {
 		personList.clear();
 		totalDue.setText("0");
 		taxDue.setText("0");
 		tipDue.setText("0");
 		tax = tip = total = 0;
 
 		personAdapter.notifyDataSetChanged();
 	}
 
 	/*
 	 * Run when the tax input is clicked
 	 */
 	public void taxInputClick(View v) {
 		final Dialog taxInputDialog = new Dialog(TipTaxActivity.this);
 		taxInputDialog.setContentView(R.layout.taxedit);
 		taxInputDialog.setTitle("Change tax");
 
 		Button taxOKButton = (Button) taxInputDialog.findViewById(R.id.taxOkButton);
 		taxOKButton.setOnClickListener(new View.OnClickListener() {
 
 			public void onClick(View v) {
 				EditText taxTotal = (EditText) taxInputDialog.findViewById(R.id.taxValueEditText);
 				if (!taxTotal.getText().toString().equals("")) {
					try {
						tax = Float.parseFloat(taxTotal.getText().toString());
					} catch (NumberFormatException nfe) {
						Toast toast = Toast.makeText(getApplicationContext(), "Please enter a number!", Toast.LENGTH_LONG);
						toast.show();
						return;
					}
 					taxDue.setText(numberFormat.format(tax));
 					updateTotalAndTipValues();
 				}
 				taxInputDialog.dismiss();
 			}
 		});
 		taxInputDialog.show();
 	}
 
 	/*
 	 * Returns the total amount by adding up what each person owes
 	 */
 	private double totalSumPeople() {
 		double sum = 0;
 		for (Person p : personList) {
 			sum += p.getDoubleValue();
 		}
 		return sum;
 	}
 
 	/*
 	 * Gets the preferences for tip and currency and updates the local variables
 	 */
 	private void updateDefaultValues() {
 		Currency c = Currency.getInstance(prefs.getString("default_currency", "USD"));
 		numberFormat.setMaximumFractionDigits(c.getDefaultFractionDigits());
 		numberFormat.setCurrency(c);
 
 		tipPercentage = Float.valueOf(prefs.getString("default_tip", "15"));
 		if (tipLabel != null) {
 			tipLabel.setText(formattedTipPctLabel());
 		}
 	}
 
 	private void updateCurrency() {
 
 		ListIterator<Person> pi = personList.listIterator();
 
 		while (pi.hasNext()) {
 			Person p = pi.next();
 			pi.set(new Person(p.getName(), p.getDoubleValue(), prefs.getString("default_currency", "USD")));
 		}
 		personAdapter.notifyDataSetChanged();
 
 		taxDue.setText(numberFormat.format(tax));
 		tipDue.setText(numberFormat.format(tip));
 		totalDue.setText(numberFormat.format(total));
 
 	}
 
 	/*
 	 * Overloaded method
 	 */
 	private void updateTotalAndTipValues() {
 		updateTotalAndTipValues(totalSumPeople(), tipPercentage);
 	}
 
 	/*
 	 * Updates the texviews for Total and Tip due
 	 */
 	private void updateTotalAndTipValues(double totalSumPeople, double tipPercentage) {
 		tip = tipPercentage * totalSumPeople / 100.0;
 		total = totalSumPeople + tax + (tipPercentage * totalSumPeople / 100.0);
 		tipDue.setText(numberFormat.format(tip));
 		totalDue.setText(numberFormat.format(total));
 	}
 
 }
