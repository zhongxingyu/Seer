 package Shopaholix.main;
 
 import java.io.IOException;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 import Shopaholix.database.Backend;
 import Shopaholix.database.Item;
 import Shopaholix.database.ItemRatings.Rating;
 import Shopaholix.database.Tag;
 import Shopaholix.database.User;
 import Shopaholix.database.UserLog;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
import android.os.Environment;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 
 public class SearchActivity extends Activity {
 	/** Called when the activity is first created. */
 	Backend backend;
 	SearchView view;
 	LinearLayout results;
 	Button familyButton;
 	Button scanButton;
 	EditText searchBar;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		System.out.println("OMG");
 		backend = Backend.getBackend(this);
 		view = new SearchView(this);
 		searchBar = view.EditText();
 		searchBar.setInputType(524288);
 		results = view.VerticalLayout();
 		familyButton = view.Button();
 		scanButton = view.Button();
 		setContentView(view
 				.render(searchBar, results, familyButton, scanButton));
 
 		final Activity that = this;
 		searchBar.addTextChangedListener(new TextWatcher() {
 			public void afterTextChanged(Editable s) {
 				final TextWatcher thatt = this;
 				ArrayList<Item> suggestedItems = backend
 						.getSuggestedItems(searchBar.getText().toString());
 				ArrayList<Tag> suggestedTags = backend
 						.getSuggestedTags(searchBar.getText().toString());
 
 				results.removeAllViews();
 
 				for (int i = 0; i < 2; i++) {
 					if (i >= suggestedTags.size())
 						break;
 					final Tag t = suggestedTags.get(i);
 					
 					List<Rating> L = new LinkedList<Rating>();
 					for(User U : backend.users.values()){
 						if(!U.name.equals("Personal")){
 							L.add(t.ratings.get(U));
 						}							
 					}
 					
 					View itemResult = view.SearchResult(
 						t.name,
 						t.ratings.get(new User("Personal")),
 						L
 					);
 
 					results.addView(itemResult);
 
 					itemResult.setOnClickListener(new OnClickListener() {
 						public void onClick(View view) {
 							String s = searchBar.getText().toString();
 							String result = "";
 							if (-1 != s.lastIndexOf(' ')) {
 								s = s.substring(0, s.lastIndexOf(' '));
 								String[] tokens = s.split(" ");
 								for (String name : tokens) {
 									if (!name.equals(""))
 										result += name + " ";
 								}
 							}
 
 							// be aware this code is copied from
 							// Backend.parseTags as a quick fix (may not be
 							// bug free)
 
 							UserLog.appendLog("Clicked on tag: " + t.name);
 							searchBar.setText(result + t.name + " ");
 							thatt.afterTextChanged(null);
 							searchBar
 									.setSelection(searchBar.getText().length());
 
 						}
 					});
 					results.addView(view.HR());
 				}
 
 				for (final Item i : suggestedItems) {
 					List<Rating> L = new LinkedList<Rating>();
 					for(User U : backend.users.values()){
 						if(!U.name.equals("Personal")){
 							L.add(i.ratings.get(U));
 						}
 					}
 					
 					View itemResult = view.SearchResult(
 						i.name,
 						i.ratings.get(new User("Personal")),
 						L
 					);
 
 					results.addView(itemResult);
 
 					itemResult.setOnClickListener(new OnClickListener() {
 						public void onClick(View view) {
 							Intent myIntent = new Intent(that,
 									ItemActivity.class);
 							myIntent.putExtra("upc", i.upc);
 							UserLog.appendLog("Searched for item: " + i.name
 									+ ", upc: " + i.upc);
 							that.startActivityForResult(myIntent, 10);
 						}
 					});
 					results.addView(view.HR());
 
 				}
 
 			}
 
 			public void beforeTextChanged(CharSequence s, int start, int count,
 					int after) {
 			}
 
 			public void onTextChanged(CharSequence s, int start, int before,
 					int count) {
 			}
 		});
 
 		familyButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				Intent myIntent = new Intent(that, FamilyActivity.class);
 				that.startActivityForResult(myIntent, 10);
 			}
 		});
 
 		scanButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				Intent myIntent = new Intent(that, ScanActivity.class);
 				that.startActivityForResult(myIntent, 10);
 			}
 		});
 
 	}
	
	@Override
	public void onPause(){
		try {
			Backend.writeBackend(getApplicationContext());
			Log.d("WROTE BACKEND", "WROTE BACKEND");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		super.onPause();
	}

 }
 

 class SearchView extends BaseView {
 	public SearchView(Activity a) {
 		super(a);
 	}
 
 	public View render(EditText E, LinearLayout results, Button familyButton,
 			Button scanButton) {
 			LinearLayout L = Shell();
 
 		L.addView(E);
 		L.addView(results);
 		LinearLayout L1 = HorizontalLayout();
 		L.addView(L1);
 		L1.addView(scanButton);
 		scanButton.setText("Scan Item");
 		scanButton.setLayoutParams(new LinearLayout.LayoutParams(0,
 				LayoutParams.FILL_PARENT, 1));
 		L1.addView(familyButton);
 		familyButton.setText("Edit Family");
 		familyButton.setLayoutParams(new LinearLayout.LayoutParams(0,
 				LayoutParams.FILL_PARENT, 1));
 
 		return L;
 	}
 
 	public View SearchResult(String text, Rating myRating, List<Rating> otherRatings) {
 		LinearLayout L = LinearLayout();
 		L.setOrientation(LinearLayout.HORIZONTAL);
 
 		android.widget.TextView T = DefaultTextView(text);
 		L.addView(T);
 
 		T.setLayoutParams(new LinearLayout.LayoutParams(0, 50, 5));
 		
 		LinearLayout H = HorizontalLayout(); L.addView(H);
 		H.setLayoutParams(new LinearLayout.LayoutParams(0, 50, 1));
 		for(Rating r: otherRatings){
 			H.addView(Icon(r));
 		}
 		
 		
 		L.addView(Icon(myRating));
 
 		return L;
 	}
 	
 	public View Icon(Rating r){
 		ImageView I = ImageView();
 		
 		I.setLayoutParams(new LinearLayout.LayoutParams(0, 50, 1));
 		switch (r) {
 		case GOOD:
 			I.setImageResource(R.drawable.green_up);
 			break;
 		case NEUTRAL:
 			I.setImageResource(R.drawable.yellow_mid);
 			break;
 		case BAD:
 			I.setImageResource(R.drawable.red_down);
 			break;
 		}
 		return I;
 	
 	}
 }
