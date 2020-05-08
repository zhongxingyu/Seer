 package org.wrowclif.recipebox.ui.components;
 
 import org.wrowclif.recipebox.Category;
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
 import android.view.WindowManager;
 
 public class CategoryDialog extends Dialog {
 
 	private AutoCompleteTextView categoryInput;
 	private TextView messageView;
 	private Button okButton;
 	private Button cancelButton;
 
 	private Recipe recipe;
 	private CategoryListWidget adapter;
 	private Category category;
 
 	private View.OnClickListener newOkOnClick;
 	private View.OnClickListener newCancelOnClick;
 	private OnEditorActionListener newOnCategoryAction;
 	private View.OnClickListener deleteOkOnClick;
 	private View.OnClickListener deleteCancelOnClick;
 	private View.OnClickListener confirmOkOnClick;
 	private View.OnClickListener confirmCancelOnClick;
 	private View.OnClickListener inUseOkOnClick;
 
 	public CategoryDialog(Context context, Recipe recipe, CategoryListWidget adapter) {
 		super(context);
 		setContentView(R.layout.category_add_dialog);
 
 		this.recipe = recipe;
 		this.adapter = adapter;
 		this.category = null;
 
 		getWindow().setLayout(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
 		setCancelable(true);
 
 		categoryInput = (AutoCompleteTextView) findViewById(R.id.category_name);
 		messageView = (TextView) findViewById(R.id.message_box);
 		okButton = (Button) findViewById(R.id.ok_button);
 		cancelButton = (Button) findViewById(R.id.cancel_button);
 
 		connectAutoComplete();
 
 		newOkOnClick = new NewOkOnClick();
 		newCancelOnClick = new CancelOnClick();
 		newOnCategoryAction = new NewOnCategoryAction();
 
 		deleteOkOnClick = new DeleteOkOnClick();
 		deleteCancelOnClick = newCancelOnClick;
 
 		confirmOkOnClick = new ConfirmOkOnClick();
 		confirmCancelOnClick = new CancelReturnToEditOnClick();
 
 		inUseOkOnClick = confirmCancelOnClick;
 	}
 
 	public void prepareNew() {
 		showNew();
 	}
 
 	public void prepareDelete(Category category) {
 		this.category = category;
 
 		showDelete();
 	}
 
 	public void showDelete() {
 		setTitle("Delete Category");
 
 		categoryInput.setVisibility(View.GONE);
 		okButton.setVisibility(View.VISIBLE);
 		cancelButton.setVisibility(View.VISIBLE);
 		messageView.setVisibility(View.VISIBLE);
 
 		messageView.setText("Are you sure you want to remove " + recipe.getName() +
							" from the " + categoryInput.getText() + " category?");
 
 		okButton.setText("Delete");
 		okButton.setOnClickListener(deleteOkOnClick);
 
 		cancelButton.setText("Cancel");
 		cancelButton.setOnClickListener(deleteCancelOnClick);
 	}
 
 	public void showNew() {
 		reshowNew();
 
 		categoryInput.setText("");
 	}
 
 	public void reshowNew() {
 		setTitle("Add Category");
 
 		categoryInput.setVisibility(View.VISIBLE);
 		okButton.setVisibility(View.VISIBLE);
 		cancelButton.setVisibility(View.VISIBLE);
 		messageView.setVisibility(View.GONE);
 
 		categoryInput.setOnEditorActionListener(newOnCategoryAction);
 
 		okButton.setText("Add");
 		okButton.setOnClickListener(newOkOnClick);
 
 		cancelButton.setText("Cancel");
 		cancelButton.setOnClickListener(newCancelOnClick);
 	}
 
 	public void showConfirmNew() {
 		setTitle("Confirm New Category");
 
 		categoryInput.setVisibility(View.GONE);
 		okButton.setVisibility(View.VISIBLE);
 		cancelButton.setVisibility(View.VISIBLE);
 		messageView.setVisibility(View.VISIBLE);
 
 		messageView.setText("You have never put any recipes in the " + categoryInput.getText() + " category before. " +
 					"Are you sure you want to add " + recipe.getName() + " to the category?");
 
 		okButton.setText("Add");
 		okButton.setOnClickListener(confirmOkOnClick);
 
 		cancelButton.setText("Cancel");
 		cancelButton.setOnClickListener(confirmCancelOnClick);
 	}
 
 	private void connectAutoComplete() {
 		Specifics<Category> sp = new Specifics<Category>() {
 
 			public View getView(int id, Category c, View v, ViewGroup vg) {
 				if(v == null) {
 					v = inflate(R.layout.autoitem);
 				}
 
 				TextView tv = (TextView) v.findViewById(R.id.child_name);
 
 				tv.setText(c.getName());
 
 				return v;
 			}
 
 			public long getItemId(Category item) {
 				return item.getId();
 			}
 
 			public List<Category> filter(CharSequence seq) {
 				return UtilityImpl.singleton.searchCategories(seq.toString(), 5);
 			}
 
 			public String convertResultToString(Category result) {
 				return result.getName();
 			}
 
 			public void onItemClick(AdapterView av, View v, int position, long id, Category item) {
 
 			}
 
 			private View inflate(int layoutId) {
 				LayoutInflater vi = getLayoutInflater();
 				return vi.inflate(layoutId, null);
 			}
 		};
 
 		ListAutoCompleteAdapter<Category> acadapter = new ListAutoCompleteAdapter<Category>(sp);
 
 		categoryInput.setAdapter(acadapter);
 	}
 
 	protected class NewOkOnClick implements View.OnClickListener {
 		public void onClick(View v) {
 			Category c = UtilityImpl.singleton.getCategoryByName(categoryInput.getText().toString());
 
 			if(c == null) {
 				showConfirmNew();
 			} else {
 				recipe.addCategory(c);
 
 				adapter.refresh();
 				CategoryDialog.this.dismiss();
 			}
 		}
 	}
 
 	protected class NewOnCategoryAction implements OnEditorActionListener {
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
 			CategoryDialog.this.dismiss();
 		}
 	}
 
 	protected class DeleteOkOnClick implements View.OnClickListener {
 		public void onClick(View v) {
 			recipe.removeCategory(category);
 
 			adapter.refresh();
 			CategoryDialog.this.dismiss();
 		}
 	}
 
 	protected class ConfirmOkOnClick implements View.OnClickListener {
 		public void onClick(View v) {
 			Category c = UtilityImpl.singleton.createOrRetrieveCategory(categoryInput.getText().toString());
 
 			newOkOnClick.onClick(null);
 		}
 	}
 
 	protected class CancelReturnToEditOnClick implements View.OnClickListener {
 		public void onClick(View v) {
 			reshowNew();
 		}
 	}
 }
