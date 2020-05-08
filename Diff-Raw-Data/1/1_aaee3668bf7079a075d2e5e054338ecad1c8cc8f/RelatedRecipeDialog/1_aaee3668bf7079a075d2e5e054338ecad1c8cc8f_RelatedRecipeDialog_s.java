 package org.wrowclif.recipebox.ui.components;
 
 import org.wrowclif.recipebox.AppData;
 import org.wrowclif.recipebox.Recipe;
 import org.wrowclif.recipebox.R;
 
 import org.wrowclif.recipebox.impl.UtilityImpl;
 
 import org.wrowclif.recipebox.ui.components.ListAutoCompleteAdapter;
 import org.wrowclif.recipebox.ui.components.ListAutoCompleteAdapter.Specifics;
 import org.wrowclif.recipebox.ui.components.DynamicLoadAdapter;
 
 import static org.wrowclif.recipebox.util.ConstantInitializer.assignId;
 
 import java.util.List;
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.content.Context;
 import android.text.Html;
 import android.util.Log;
 import android.widget.Button;
 import android.widget.AdapterView;
 import android.widget.AutoCompleteTextView;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.TextView.OnEditorActionListener;
 import android.view.KeyEvent;
 import android.view.inputmethod.EditorInfo;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.view.WindowManager;
 
 public class RelatedRecipeDialog extends Dialog {
 	private static final String LOG_TAG = "RecipeBox RelatedRecipeDialog";
 
 	private AppData appData;
 
 	private TextView titleView;
 	private AutoCompleteTextView recipeInput;
 	private TextView messageView;
 	private Button okButton;
 	private Button cancelButton;
 
 	private long selectedId;
 
 	private Recipe recipe;
 	private RelatedRecipeListWidget adapter;
 	private Recipe suggestion;
 
 	private View.OnClickListener newOkOnClick;
 	private View.OnClickListener newCancelOnClick;
 	private OnEditorActionListener newOnRecipeAction;
 	private View.OnClickListener deleteOkOnClick;
 	private View.OnClickListener deleteCancelOnClick;
 	private View.OnClickListener invalidCancelOnClick;
 
 	public RelatedRecipeDialog(Context context, Recipe recipe, RelatedRecipeListWidget adapter) {
 		super(context);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.category_add_dialog);
 
 		this.appData = AppData.getSingleton();
 
 		this.recipe = recipe;
 		this.adapter = adapter;
 		this.suggestion = null;
 
 		getWindow().setLayout(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
 		setCancelable(true);
 
 		titleView = (TextView) findViewById(R.id.title);
 		recipeInput = (AutoCompleteTextView) findViewById(R.id.category_name);
 		messageView = (TextView) findViewById(R.id.message_box);
 		okButton = (Button) findViewById(R.id.ok_button);
 		cancelButton = (Button) findViewById(R.id.cancel_button);
 
 		appData.useHeadingFont(titleView);
 		appData.useTextFont(recipeInput);
 		appData.useTextFont(messageView);
 		appData.useHeadingFont(okButton);
 		appData.useHeadingFont(cancelButton);
 
 		connectAutoComplete();
 
 		recipeInput.setHint("Recipe");
 
 		newOkOnClick = new NewOkOnClick();
 		newCancelOnClick = new CancelOnClick();
 		newOnRecipeAction = new NewOnRecipeAction();
 
 		deleteOkOnClick = new DeleteOkOnClick();
 		deleteCancelOnClick = newCancelOnClick;
 
 		invalidCancelOnClick = new CancelReturnToEditOnClick();
 	}
 
 	public void setTitle(String title) {
 		titleView.setText(title);
 	}
 
 	public void prepareNew() {
 		showNew();
 	}
 
 	public void prepareDelete(Recipe suggestion) {
 		this.suggestion = suggestion;
 
 		showDelete();
 	}
 
 	public void showDelete() {
 		setTitle("Remove Relation");
 
 		recipeInput.setVisibility(View.GONE);
 		okButton.setVisibility(View.VISIBLE);
 		cancelButton.setVisibility(View.VISIBLE);
 		messageView.setVisibility(View.VISIBLE);
 
 		messageView.setText(Html.fromHtml("Are you sure you want to remove the relation between <b>" + recipe.getName() + "</b>" +
 							" and <b>" + suggestion.getName() + "</b>?"));
 
 		okButton.setText("Remove");
 		okButton.setOnClickListener(deleteOkOnClick);
 
 		cancelButton.setText("Cancel");
 		cancelButton.setOnClickListener(deleteCancelOnClick);
 	}
 
 	public void showNew() {
 		reshowNew();
 
 		selectedId = -1;
 		recipeInput.setText("");
 	}
 
 	public void reshowNew() {
 		setTitle("Add Relation");
 
 		recipeInput.setVisibility(View.VISIBLE);
 		okButton.setVisibility(View.VISIBLE);
 		cancelButton.setVisibility(View.VISIBLE);
 		messageView.setVisibility(View.GONE);
 
 		recipeInput.setOnEditorActionListener(newOnRecipeAction);
 
 		okButton.setText("Add");
 		okButton.setOnClickListener(newOkOnClick);
 
 		cancelButton.setText("Cancel");
 		cancelButton.setOnClickListener(newCancelOnClick);
 	}
 
 	public void showInvalidRelation() {
 		setTitle("Cannot Add Relation");
 
 		recipeInput.setVisibility(View.GONE);
 		okButton.setVisibility(View.GONE);
 		cancelButton.setVisibility(View.VISIBLE);
 		messageView.setVisibility(View.VISIBLE);
 
 		if(suggestion == null) {
 			messageView.setText("No recipes named " + recipeInput.getText() + " exist");
 		} else {
 			messageView.setText("A recipe cannot be related to itself");
 		}
 
 		cancelButton.setText("Ok");
 		cancelButton.setOnClickListener(invalidCancelOnClick);
 	}
 
 	private void connectAutoComplete() {
 		Specifics<Recipe> sp = new Specifics<Recipe>() {
 
 			public View getView(int id, Recipe r, View v, ViewGroup vg) {
 				if(v == null) {
 					v = inflate(R.layout.autoitem);
 				}
 
 				TextView tv = (TextView) v.findViewById(R.id.child_name);
 				appData.useTextFont(tv);
 
 				tv.setText(r.getName());
 
 				return v;
 			}
 
 			public long getItemId(Recipe item) {
 				return item.getId();
 			}
 
 			public List<Recipe> filter(CharSequence seq) {
 				return UtilityImpl.singleton.searchRecipes(seq.toString(), 5);
 			}
 
 			public String convertResultToString(Recipe result) {
 				return result.getName();
 			}
 
 			public void onItemClick(AdapterView av, View v, int position, long id, Recipe item) {
 				selectedId = item.getId();
 			}
 
 			private View inflate(int layoutId) {
 				LayoutInflater vi = getLayoutInflater();
 				return vi.inflate(layoutId, null);
 			}
 		};
 
 		ListAutoCompleteAdapter<Recipe> acadapter = new ListAutoCompleteAdapter<Recipe>(sp);
 
 		recipeInput.setAdapter(acadapter);
 	}
 
 	protected class NewOkOnClick implements View.OnClickListener {
 		public void onClick(View v) {
 			String inputText = recipeInput.getText().toString().trim();
 			if(selectedId >= 0) {
 				Recipe r = UtilityImpl.singleton.getRecipeById(selectedId);
 				if(inputText.equalsIgnoreCase(r.getName().trim())) {
 					suggestion = r;
 				}
 			}
 
 			if(suggestion == null) {
 				List<Recipe> recipesList = UtilityImpl.singleton.searchRecipes(recipeInput.getText().toString(), 10);
 
 				for(Recipe r : recipesList) {
 					if(inputText.equalsIgnoreCase(r.getName().trim())) {
 						suggestion = r;
 						break;
 					}
 				}
 			}
 
 			if(suggestion == null) {
 				showInvalidRelation();
 			} else if(suggestion.getId() == recipe.getId()) {
 				showInvalidRelation();
 			} else {
 				recipe.addSuggestion(suggestion);
 
 				adapter.refresh();
 				RelatedRecipeDialog.this.dismiss();
 			}
 		}
 	}
 
 	protected class NewOnRecipeAction implements OnEditorActionListener {
 		public boolean onEditorAction(TextView v, int action, KeyEvent event) {
 			if((action == EditorInfo.IME_NULL) && (event.getAction() == 0)) {
 				newOkOnClick.onClick(null);
 				return true;
 			}
 			return false;
 		}
 	}
 
 	protected class CancelOnClick implements View.OnClickListener {
 		public void onClick(View v) {
 			RelatedRecipeDialog.this.dismiss();
 		}
 	}
 
 	protected class DeleteOkOnClick implements View.OnClickListener {
 		public void onClick(View v) {
 			recipe.removeSuggestion(suggestion);
 
 			adapter.refresh();
 			RelatedRecipeDialog.this.dismiss();
 		}
 	}
 
 	protected class CancelReturnToEditOnClick implements View.OnClickListener {
 		public void onClick(View v) {
 			reshowNew();
 		}
 	}
 }
