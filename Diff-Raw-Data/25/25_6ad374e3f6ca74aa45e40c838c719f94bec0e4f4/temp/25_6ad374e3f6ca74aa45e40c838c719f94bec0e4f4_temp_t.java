 package com.app.afridge;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashMap;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.DatePickerDialog;
 import android.app.ListActivity;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.InputType;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.Button;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemClickListener;
 
 public class temp extends ListActivity {
 
 	ListView lv;
 	private SimpleAdapter settings;
 	ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
 	ArrayList<String> result = new ArrayList<String>();
 	ArrayList<String> item_to_modify = new ArrayList<String>();
 	private String name = "";
 	private String type = "";
 	private String quantity = "";
 	private String qtype = "";
 	private String details = "";
 	private String exp_date = "";
 	private int mYear;
 	private int mMonth;
 	private int mDay;
 	private boolean isModifying = false;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.temp);
 
 		if (getIntent().getStringArrayListExtra("item_to_modify") != null) {
 			item_to_modify.addAll(getIntent().getStringArrayListExtra(
 					"item_to_modify"));
 			isModifying = true;
 		}
 
 		if (!item_to_modify.isEmpty()) {
 			name = item_to_modify.get(0);
 			type = item_to_modify.get(1);
 			quantity = item_to_modify.get(2);
 			qtype = item_to_modify.get(3);
 			details = item_to_modify.get(4);
 			exp_date = item_to_modify.get(5);
 		}
 
 		HashMap<String, String> item = new HashMap<String, String>();
 		item.put("line1", "Enter name:");
 		item.put("line2", "Enter the name of the item");
 
 		HashMap<String, String> item2 = new HashMap<String, String>();
 		item2.put("line1", "Choose type:");
 		item2.put("line2", "What type of item is entered (item icon)");
 
 		HashMap<String, String> item3 = new HashMap<String, String>();
 		item3.put("line1", "Quantity:");
 		item3.put("line2", "Enter the quantity of the item");
 
 		HashMap<String, String> item4 = new HashMap<String, String>();
 		item4.put("line1", "Type of measurement:");
 		item4.put("line2", "What type of measurement is used.");
 
 		HashMap<String, String> item5 = new HashMap<String, String>();
 		item5.put("line1", "Details:");
 		item5.put("line2", "Enter additional details for the item");
 
 		HashMap<String, String> item6 = new HashMap<String, String>();
 		item6.put("line1", "Expiration date:");
 		item6.put("line2", "Select if there is an expiration date.");
 
 		settings = new SimpleAdapter(this, list, R.layout.custom_list_item,
 				new String[] { "line1", "line2" }, new int[] { R.id.text1,
 						R.id.text2 });
 		setListAdapter(settings);
 
 		list.add(item);
 		list.add(item2);
 		list.add(item3);
 		list.add(item4);
 		list.add(item5);
 		list.add(item6);
 		settings.notifyDataSetChanged();
 
 		lv = getListView();
 		lv.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> arg0, View arg1, int id,
 					long arg3) {
 				if (id == 0) {
 					name();
 				} else if (id == 1) {
 					type();
 				} else if (id == 2) {
 					quantity();
 				} else if (id == 3) {
 					qtype();
 				} else if (id == 4) {
 					details();
 				} else if (id == 5) {
 					exp_date();
 				}
 			}
 		});
 
 		Button button = (Button) findViewById(R.id.add_button_temp);
 		button.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				result.add(name);
 				if (type == "")
 					result.add("Other");
 				else
 					result.add(type);
 				result.add(quantity);
 				result.add(qtype);
 				result.add(details);
 				result.add("false");
 				result.add(exp_date);
 				Intent resultIntent = new Intent();
 				if (isModifying) {
 					resultIntent.putExtra("isModified", true);
 				}
 				resultIntent.putStringArrayListExtra("Item", result);
 				setResult(Activity.RESULT_OK, resultIntent);
 				finish();
 			}
 		});
 	}
 
 	public void name() {
 
 		AlertDialog.Builder alert = new AlertDialog.Builder(this);
 		alert.setTitle("");
 		alert.setMessage("Enter name: ");
 
 		// Set an EditText view to get user input
 		final EditText input = new EditText(this);
 		alert.setView(input);
 
 		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int whichButton) {
 				Editable value = input.getText();
 				input.setInputType(InputType.TYPE_CLASS_TEXT);
 				name = value.toString();
 				// result.add(value.toString()); TODO
 			}
 		});
 		alert.setNegativeButton("Cancel",
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int whichButton) {
 						dialog.cancel();
 					}
 				});
 
 		alert.show();
 
 	}
 
 	public void type() {
 		// TODO
 		AlertDialog.Builder alert = new AlertDialog.Builder(this);
 		alert.setTitle("Select type of item:");
 
 		final String[] items = getResources().getStringArray(R.array.type);
 		alert.setSingleChoiceItems(items, -1,
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int item) {
 
 						type = items[item];
 						Toast.makeText(getApplicationContext(), items[item],
 								Toast.LENGTH_SHORT).show();
 						dialog.dismiss();
 					}
 				});
 		AlertDialog alert_ = alert.create();
 		alert_.show();
 	}
 
 	public void quantity() {
 		AlertDialog.Builder alert = new AlertDialog.Builder(this);
 		alert.setTitle("");
 		alert.setMessage("Enter quantity: ");
 
 		// Set an EditText view to get user input
 		final EditText input = new EditText(this);
 		input.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
 		alert.setView(input);
 
 		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int whichButton) {
 				Editable value = input.getText();
 				quantity = value.toString();
 				// result.add(value.toString()); TODO
 			}
 		});
 		alert.setNegativeButton("Cancel",
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int whichButton) {
 						dialog.cancel();
 					}
 				});
 
 		alert.show();
 	}
 
 	public void qtype() {
 		AlertDialog.Builder alert = new AlertDialog.Builder(this);
 		alert.setTitle("Select type of measurement:");
 
 		final String[] items = getResources().getStringArray(R.array.qtype);
 		alert.setSingleChoiceItems(items, -1,
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int item) {
 
 						qtype = items[item];
 						Toast.makeText(getApplicationContext(), items[item],
 								Toast.LENGTH_SHORT).show();
 						dialog.dismiss();
 					}
 				});
 		AlertDialog alert_ = alert.create();
 		alert_.show();
 	}
 
 	public void details() {
 		AlertDialog.Builder alert = new AlertDialog.Builder(this);
 		alert.setTitle("");
 		alert.setMessage("Enter additional details: ");
 
 		// Set an EditText view to get user input
 		final EditText input = new EditText(this);
 		alert.setView(input);
 
 		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int whichButton) {
 				Editable value = input.getText();
 				details = value.toString();
 				// result.add(value.toString());//TODO
 			}
 		});
 		alert.setNegativeButton("Cancel",
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int whichButton) {
 						dialog.cancel();
 					}
 				});
 
 		alert.show();
 	}
 
 	public void exp_date() {
 
 		Calendar c = Calendar.getInstance();
 
 		mYear = c.get(Calendar.YEAR);
		mMonth = c.get(Calendar.MONTH) + 1;
 		mDay = c.get(Calendar.DAY_OF_MONTH);
 
 		DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
 
 			public void onDateSet(DatePicker view, int year, int monthOfYear,
 					int dayOfMonth) {
 				mYear = year;
				mMonth = monthOfYear + 1;
 				mDay = dayOfMonth;
 				exp_date = Integer.toString(mDay) + "-"
 						+ Integer.toString(mMonth) + "-"
 						+ Integer.toString(mYear);
 			}
 		};
 
 		DatePickerDialog dialog = new DatePickerDialog(this, mDateSetListener,
				mYear, mMonth-1, mDay);
 		dialog.show();
 	}
 }
