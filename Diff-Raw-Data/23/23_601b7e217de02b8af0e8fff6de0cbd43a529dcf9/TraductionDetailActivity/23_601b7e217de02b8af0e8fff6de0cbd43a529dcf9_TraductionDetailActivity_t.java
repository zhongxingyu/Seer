 package nz.co.lake_hammond.apprenons;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import nz.co.lake_hammond.apprenons.activities.traduction.AdjectiveOptionsFragment;
 import nz.co.lake_hammond.apprenons.activities.traduction.NounOptionsFragment;
 import nz.co.lake_hammond.apprenons.activities.traduction.TraductionOptionsFragment;
 import nz.co.lake_hammond.apprenons.activities.traduction.VerbOptionsFragment;
 import nz.co.lake_hammond.apprenons.model.Classification;
 import nz.co.lake_hammond.apprenons.model.Traduction;
 import nz.co.lake_hammond.apprenons.sql.TraductionDatabase;
 import nz.co.lake_hammond.apprenons.R;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.Intent;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnLongClickListener;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.RadioButton;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class TraductionDetailActivity extends Activity {
 
     private VerbWatcher watcher = null;
     private TraductionOptionsFragment optionsFragment = null;
 	private TraductionDatabase db;
 	
 	private Set<String> selectedTags = new HashSet<String>();
 
 	@Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_traduction_detail);
         findViewById(R.id.radioNoun).performClick();
         
         db = new TraductionDatabase(this);
         
         populateTagsList();
         
         EditText newTagBox = (EditText) findViewById(R.id.newTag);
         newTagBox.setOnEditorActionListener(new OnDoneEditingListener() {
 			@Override
 			protected void onDone(TextView v) {
 				saveTag(v);
 			}
 		});
         newTranslationBox().setOnEditorActionListener(new OnDoneEditingListener() {
 			
 			@Override
 			protected void onDone(TextView v) {
 				saveTraduction(v);
 			}
 		});
         
 		// Show the Up button in the action bar.
 		getActionBar().setDisplayHomeAsUpEnabled(true);
 	}
 
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
 	 */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		if (item.getItemId() == android.R.id.home) {
 			Intent intent = new Intent(this, NewQuizActivity.class);
 			startActivity(intent);
 			
 			return true;
 		}
 		
 		return super.onOptionsItemSelected(item);
 	}
 
 	public void saveTraduction(View v) {
 		if (newPhraseText().isEmpty()) {
 			Toast.makeText(this, R.string.phrase_not_given, Toast.LENGTH_LONG).show();
 		} else if (newTranslationText().trim().isEmpty()) {
 			Toast.makeText(this, R.string.translation_not_given, Toast.LENGTH_LONG).show();
 		} else if (db.addTraduction(getTraduction(), selectedTags) == -1) {
 			Toast.makeText(this, R.string.translation_not_saved, Toast.LENGTH_SHORT).show();
 		} else {
 			newTranslationBox().setText(null);
 			newPhraseBox().setText(null);
 			newPhraseBox().requestFocus();
 		}
 	}
 
 	private String newTranslationText() {
 		return newTranslationBox().getText().toString();
 	}
 
 	private String newPhraseText() {
 		return newPhraseBox().getText().toString().trim();
 	}
 
 	
     private void populateTagsList() {
     	String[] tagList = getIntent().getStringArrayExtra(Extras.TAG_LIST);
     	if (tagList != null)
     		for (String tag : tagList)
     			selectedTags.add(tag);
     	
 		for (final String tag : db.getAllTags())
 			addTagCheckBox(tag).setChecked(selectedTags.contains(tag));
 	}
 
 
 	private CheckBox addTagCheckBox(final String tag) {
 		final LinearLayout tagList = (LinearLayout) findViewById(R.id.tagList);
 		final CheckBox box = new CheckBox(this);
 		box.setText(tag);
 		box.setLongClickable(true);
 		tagList.addView(box, tagList.getChildCount() - 1,
 				new LayoutParams(LayoutParams.MATCH_PARENT, 
 								 LayoutParams.WRAP_CONTENT));
 		
 		box.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 			@Override
 			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 				if (isChecked)
 					selectedTags.add(tag);
 				else
 					selectedTags.remove(tag);
 			}
 		});
 		
 		box.setOnLongClickListener(new OnLongClickListener() {
 			@Override
 			public boolean onLongClick(View v) {
 				AlertDialog.Builder builder = new AlertDialog.Builder(TraductionDetailActivity.this);
 				builder.setTitle(R.string.tag_confirm_delete_title);
 				builder.setMessage(getString(R.string.tag_confirm_delete_message, tag));
 				builder.setPositiveButton(R.string.tag_confirm_delete_ok, new OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						db.deleteTag(tag);
 						selectedTags.remove(tag);
 						tagList.removeView(box);
 						dialog.dismiss();
 					}
 				});
 				builder.setNegativeButton(R.string.tag_confirm_delete_cancel, new OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						dialog.cancel();
 					}
 				});
 				
 				builder.show();
 				
 				return true;
 			}
 		});
 		
 		return box;
 	}
 
 
 	@Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.traduction_detail, menu);
         return true;
     }
     
     public Traduction getTraduction() {
     	String french = newPhraseText();
     	String english = newTranslationText().trim();
     	
     	// A bit of a hack, we only have one type that has no options.
     	Classification classification;
     	if (optionsFragment == null) {
     		classification = Classification.adverb();
     	} else {
     		classification = optionsFragment.getClassification();
     	}
     	
     	return new Traduction(french, english, classification);
     }
 
 
 	private EditText newTranslationBox() {
 		return (EditText) findViewById(R.id.newTranslationText);
 	}
 
 
 	private EditText newPhraseBox() {
 		return (EditText) findViewById(R.id.newPhraseText);
 	}
     
     public void onPhraseTypeSelected(View view) {
     	if (watcher != null) {
     		newTranslationBox().removeTextChangedListener(watcher);
     		watcher = null;
     	}
     	
     	findViewById(R.id.typeOptionsFragment).setVisibility(
     			view.getId() == R.id.radioAdverb ?
     					View.INVISIBLE : View.VISIBLE);
     	setTypeRadioStyle();
     	
     	optionsFragment = null;
     	switch(view.getId()) {
     	case R.id.radioNoun:
     		optionsFragment = new NounOptionsFragment();
     		break;
     	case R.id.radioVerb:
     		VerbOptionsFragment verbDetails = new VerbOptionsFragment();
     		optionsFragment = verbDetails;
     		watcher = new VerbWatcher(verbDetails);
     		newTranslationBox().addTextChangedListener(watcher);
    		verbDetails.setVerb(newTranslationBox().getText().toString());
     		break;
     	case R.id.radioAdjective:
     		optionsFragment = new AdjectiveOptionsFragment();
     		break;
     	case R.id.radioAdverb:
     		return;
 		default:
 			throw new Error("setPhraseType() called on invalid view.");
     	}
     	getFragmentManager().beginTransaction()
     		.replace(R.id.typeOptionsFragment, optionsFragment)
     		.commit();
     }
 
     /**
      * Highlights the selected radio button.
      */
 	private void setTypeRadioStyle() {
 		RadioButton buttons[] = new RadioButton[] {
 			(RadioButton) findViewById(R.id.radioNoun),
 			(RadioButton) findViewById(R.id.radioVerb),
 			(RadioButton) findViewById(R.id.radioAdjective)
 		};
 		for (RadioButton button : buttons)
 			button.setBackgroundResource(button.isChecked() ?
 					R.drawable.options_background : 0);
 	}
 	
 	private void saveTag(TextView tagBox) {
 		String tag = tagBox.getText().toString().trim();
 		if (tag.isEmpty()) {
 			tagBox.setText(null);
 		}
 		
 		if (!db.addTag(tag)) {
 			Toast.makeText(TraductionDetailActivity.this,
 					R.string.tag_not_saved, Toast.LENGTH_LONG).show();
 		} else {
 			tagBox.setText(null);
 			addTagCheckBox(tag).performClick();
 		}
 	}
 
 	static class VerbWatcher implements TextWatcher {
 		private final VerbOptionsFragment fragment;
 
 		VerbWatcher(VerbOptionsFragment fragment) {
 			this.fragment = fragment;
 		}
 		
 		@Override
 		public void beforeTextChanged(CharSequence s, int start, int count,
 				int after) {}
 
 		@Override
 		public void onTextChanged(CharSequence s, int start, int before,
 				int count) {}
 
 		@Override
 		public void afterTextChanged(Editable s) {
 			fragment.setVerb(s.toString());
 		}
 	}
 }
