 package com.seawolfsanctuary.tmt;
 
 import java.io.File;
 import java.io.InputStream;
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import android.app.ExpandableListActivity;
 import android.content.Intent;
 import android.graphics.drawable.Drawable;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.view.Gravity;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.AbsListView;
 import android.widget.BaseExpandableListAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class ClassInfoActivity extends ExpandableListActivity {
 
 	public static final int IMAGE_POSITION = 0;
 	public static final String dataDirectoryPath = "Android/data/com.seawolfsanctuary.tmt";
 	public static final String dataDirectoryURI = "file:///sdcard/Android/data/com.seawolfsanctuary.tmt";
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setListAdapter(new ClassInfoAdapter());
 		registerForContextMenu(getExpandableListView());
 	}
 
 	class ClassInfoAdapter extends BaseExpandableListAdapter {
 
 		ArrayList<String> entries = loadClassInfo(true);
 		ArrayList<String[]> data = parseEntries(entries);
 		ArrayList<String> names = new ArrayList<String>(getNames(data));
 
 		private String[] presentedNames = Helpers
 				.arrayListToArray(getNames(data));
 		private String[][] presentedData = Helpers
 				.multiArrayListToArray(getData(data));
 
 		private ArrayList<String> getNames(ArrayList<String[]> data) {
 			ArrayList<String> names = new ArrayList<String>();
 			for (int i = 0; i < data.size(); i++) {
 				String[] entry = data.get(i);
 				String name = entry[0];
 				if (entry[1].length() > 0) {
 					name = name + "  -  " + entry[1];
 				}
 				names.add(name);
 			}
 			return names;
 		}
 
 		private ArrayList<ArrayList<String>> getData(ArrayList<String[]> entries) {
 			ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
 
 			for (int i = 0; i < entries.size(); i++) {
 				String[] entry = entries.get(i);
 				ArrayList<String> split = new ArrayList<String>();
 
 				if (entry.length < 6) {
 					String[] new_entry = new String[] { entry[0], entry[1],
 							entry[2], entry[3], entry[4], "", "" };
 					entry = new_entry;
 				}
 
 				try {
 					entry[2] = NumberFormat.getIntegerInstance().format(
 							Integer.parseInt(entry[2]))
 							+ "mm";
 				} catch (Exception e) {
 					// meh
 				}
 
 				if (entry[5].length() < 1) {
 					entry[5] = "still in service";
 				}
 
 				split.add(null);
 				split.add("Guage: " + entry[2] + "\nEngine: " + entry[3]);
 				split.add("Service: " + entry[4] + "\nRetired: " + entry[5]);
 
 				ArrayList<String> operatorList = parseOperators(entry[6]);
 				String operators = "";
 				for (String operator : operatorList) {
 					operators = operators + operator + ", ";
 				}
 				operators = operators.substring(0, operators.length() - 2);
 				split.add("Operators: " + operators);
 				data.add(split);
 			}
 
 			return data;
 		}
 
 		public Object getChild(int groupPosition, int childPosition) {
 			return presentedData[groupPosition][childPosition];
 		}
 
 		public long getChildId(int groupPosition, int childPosition) {
 			return childPosition;
 		}
 
 		public int getChildrenCount(int groupPosition) {
 			return presentedData[groupPosition].length;
 		}
 
 		public TextView getGenericTextView() {
 			// Layout parameters for the ExpandableListView
 			AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
 					ViewGroup.LayoutParams.FILL_PARENT, 64);
 
 			TextView textView = new TextView(ClassInfoActivity.this);
 			textView.setLayoutParams(lp);
 			// Centre the text vertically
 			textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
 			// Set the text starting position
 			textView.setPadding(36, 0, 0, 0);
 			return textView;
 		}
 
 		public ImageView getGenericImageView() {
 			// Layout parameters for the ExpandableListView
 			AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
 					ViewGroup.LayoutParams.FILL_PARENT, 128);
 
 			ImageView imageView = new ImageView(ClassInfoActivity.this);
 			imageView.setLayoutParams(lp);
 			// Set the image starting position
 			imageView.setPadding(36, 0, 0, 0);
 
 			return imageView;
 		}
 
 		public View getChildView(int groupPosition, int childPosition,
 				boolean isLastChild, View convertView, ViewGroup parent) {
 			if (childPosition == IMAGE_POSITION) {
 				final String classNo = data.get(groupPosition)[0];
 				ImageView imageView = getGenericImageView();
 				imageView.setImageDrawable(load_photo(classNo));
 				imageView.setOnClickListener(new OnClickListener() {
 					@Override
 					public void onClick(View v) {
 						show_photo(classNo);
 					}
 				});
 
 				return imageView;
 			} else {
 				TextView textView = getGenericTextView();
 				textView.setText(getChild(groupPosition, childPosition)
 						.toString());
 				return textView;
 			}
 		}
 
 		public Object getGroup(int groupPosition) {
 			return presentedNames[groupPosition];
 		}
 
 		public int getGroupCount() {
 			return presentedNames.length;
 		}
 
 		public long getGroupId(int groupPosition) {
 			return groupPosition;
 		}
 
 		public View getGroupView(int groupPosition, boolean isExpanded,
 				View convertView, ViewGroup parent) {
 			TextView textView = getGenericTextView();
 			textView.setText(getGroup(groupPosition).toString());
 			return textView;
 		}
 
 		public boolean isChildSelectable(int groupPosition, int childPosition) {
 			if (childPosition == IMAGE_POSITION) {
 				return true;
 			} else {
 				return false;
 			}
 		}
 
 		public boolean hasStableIds() {
 			return true;
 		}
 
 		private void show_photo(String classNo) {
 			File f = new File(Environment.getExternalStorageDirectory()
 					.toString() + "/" + dataDirectoryPath + "/class_photos/",
 					classNo);
 			if (f.exists()) {
 				Intent i = new Intent(Intent.ACTION_VIEW);
 				i.setDataAndType(Uri.parse(dataDirectoryURI + "/class_photos/"
 						+ classNo), "image/*");
 				startActivity(i);
 			} else {
 				Toast.makeText(getBaseContext(),
 						"Please download the bundle to view this photo.",
 						Toast.LENGTH_SHORT).show();
 			}
 		}
 
		private Drawable load_photo(String classNo) {
			Drawable d = null;
 			try {
				File f = new File(Environment.getExternalStorageDirectory()
						.toString()
						+ "/"
						+ dataDirectoryPath
						+ "/class_photos/thumbs/", classNo);
				if (f.exists()) {
					Drawable p = Drawable.createFromPath(f.getPath());
					d = p;
				}
 			} catch (Exception e) {
 				System.out.println(e.getMessage());
 			}

			return d;
 		}
 
 		private String[] read_csv(String filename) {
 			String[] array = {};
 
 			try {
 				InputStream input;
 				input = getAssets().open(filename);
 				int size = input.available();
 				byte[] buffer = new byte[size];
 
 				input.read(buffer);
 				input.close();
 				array = new String(buffer).split("\n");
 
 				Toast.makeText(
 						getBaseContext(),
 						"Loaded " + array.length + " entr"
 								+ (array.length == 1 ? "y" : "ies")
 								+ " from data file.", Toast.LENGTH_SHORT)
 						.show();
 
 			} catch (Exception e) {
 				Toast.makeText(getBaseContext(), "Error reading data file!",
 						Toast.LENGTH_SHORT).show();
 			}
 
 			return array;
 		}
 
 		private ArrayList<String> loadClassInfo(boolean showToast) {
 
 			try {
 				ArrayList<String> array = new ArrayList<String>();
 
 				String[] classInfo = read_csv("classes.csv");
 				for (String infoLine : classInfo) {
 					array.add(infoLine);
 				}
 
 				return array;
 
 			} catch (Exception e) {
 				Toast.makeText(getBaseContext(), "Error: " + e.getMessage(),
 						Toast.LENGTH_LONG).show();
 
 				return new ArrayList<String>();
 			}
 
 		}
 
 		private ArrayList<String[]> parseEntries(ArrayList<String> entries) {
 			ArrayList<String[]> data = new ArrayList<String[]>();
 
 			try {
 				for (Iterator<String> i = entries.iterator(); i.hasNext();) {
 					String str = (String) i.next();
 					String[] elements = str.split(",");
 					String[] entry = new String[elements.length];
 
 					for (int j = 0; j < entry.length; j++) {
 						entry[j] = Helpers.trimCSVSpeech(elements[j]);
 					}
 
 					data.add(entry);
 				}
 			} catch (Exception e) {
 				System.out.println(e.getMessage());
 				Toast.makeText(getApplicationContext(),
 						"Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
 			}
 
 			return data;
 		}
 
 		private ArrayList<String> parseOperators(String operatorString) {
 			ArrayList<String> operators = new ArrayList<String>();
 			if (operatorString.length() > 0) {
 				String[] pipedOperators = operatorString.split("[|]");
 				for (String operator : pipedOperators) {
 					operators.add(operator);
 				}
 			} else {
 				operators.add("(none)");
 			}
 			return operators;
 		}
 	}
 
 }
