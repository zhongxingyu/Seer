 package edu.cmusv.lions.petmobile;
 
import org.apache.commons.lang3.text.WordUtils;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.TextView;
 import edu.cmusv.lions.petmobile.util.DataSource;
 import edu.cmusv.lions.petmobile.util.DataSource.JsonResultHandler;
 
 public abstract class PetDetailsActivity extends PetActivity {
 
 	protected DataSource mDataSource;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		mDataSource = new DataSource();
 	}
 
 	protected void renderDetails() {
 		mDataSource.setJsonResultHandler(new JsonResultHandler() {
 			@Override
 			public void onJsonResult(JSONObject jsonObject) {
 				for (String key : getDisplayKeys()) {
 					try {
 						String label = convertJsonKeyToLabel(key);
 						String value = jsonObject.getString(key);
 						addLabelValuePair(label, value);
 						addSpace();
 					} catch (JSONException e) {
 						e.printStackTrace();
						showMessageDialog("Error:", e.getMessage());
 					}
 				}
 			}
 
 			@Override
 			public void onJsonResult(JSONArray jsonArray) {
 				// no-op
 			}
 
 			@Override
 			public void onInternetFailure() {
 				showMessageDialog("Oops", "You do not have an internet connection.");
 			}
 		});
 		requestJsonData();
 	}
 
 	protected String convertJsonKeyToLabel(String jsonKey) {
 		return WordUtils.capitalize(jsonKey.replace("_", " "));
 	}
 	
 	protected void addLabelValuePair(String label, String value) {
 		boldLabel(addLabel(label));
 		addLabel(value);
 	}
 
 	protected TextView addLabel(String text) {
 		TextView textView = new TextView(this);
 		textView.setText(text);
 		getDetailsContainer().addView(textView);
 		return textView;
 	}
 
 	protected void boldLabel(TextView textView) {
 		textView.setTypeface(null, Typeface.BOLD);
 	}
 
 	protected View addSpace() {
 		return addSpace(6);
 	}
 
 	protected View addSpace(int px) {
 		View space = new View(this);
 		getDetailsContainer().addView(space, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, px));
 		return space;
 	}
 
 	protected View addDivider() {
 		View divider = new View(this);
 		divider.setBackgroundColor(0xFF5E5E5E);
 		getDetailsContainer().addView(divider, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
 		return divider;
 	}
 
 	protected abstract String[] getDisplayKeys();
 
 	protected abstract ViewGroup getDetailsContainer();
 
 	protected abstract void requestJsonData();
 
 }
