 package org.fourdnest.androidclient.ui;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.fourdnest.androidclient.R;
 import org.fourdnest.androidclient.Tag;
 import org.fourdnest.androidclient.services.TagSuggestionService;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.graphics.Color;
 import android.graphics.LightingColorFilter;
 import android.support.v4.content.LocalBroadcastManager;
 import android.text.Editable;
 import android.text.InputFilter;
 import android.text.TextWatcher;
 import android.text.TextUtils.TruncateAt;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.AutoCompleteTextView;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.LinearLayout;
 
 /**
  * An UI element for attaching tags to the Egg that is being edited.
  * Consists of a growing set of checkbox buttons for each tag,
  * and an autocompleting text field for adding tags.
  */
 public class TaggingTool extends LinearLayout {
 	/** Tag string used to indicate source in logging */	
 	public static final String TAG = TaggingTool.class.getSimpleName();
 	
 	/** Text size for tag checkboxes */
 	public static final float TEXT_SIZE = 12;
 	/** Color to multiply the old background color with, when checkbox is selected */
 	public static final int COLOR_SELECTED_MUL = 0xFF000000; // set to zero/black
 	/** Color to add to the multiplied old background color, when checkbox is selected */ 
 	public static final int COLOR_SELECTED_ADD = 0xFF00FF00; // add green 
 	
 	private static final InputFilter[] tagFilter = { new Tag.TagFilter() };
 	
 	private List<TagCheckBox> buttons;
 	private FlowLayout tagFlowLayout;
 	private AutoCompleteTextView tagTextView;
 	private LocalBroadcastManager mLocalBroadcastManager;
 	private Button addTagButton;
 
 	private BroadcastReceiver mReceiver;
 
 	/**
 	 * Creates and initializes the TaggingTool.
 	 * @param context The context for the TaggingTool.
 	 * @param parent The parent element into which TaggingTool will add itself.
 	 */
 	public TaggingTool(Context context, ViewGroup parent) {
 		super(context);
 		this.buttons = new ArrayList<TagCheckBox>();
 		
 		LayoutInflater inflater = LayoutInflater.from(context);
 		inflater.inflate(R.layout.taggingtool_layout, this, true);
         this.tagTextView = (AutoCompleteTextView) findViewById(R.id.autocomplete_tag);
         this.tagFlowLayout = (FlowLayout) findViewById(R.id.tag_flowlayout);
         this.addTagButton = ((Button) this.findViewById(R.id.add_tag_button));
 
        // Only allow valid characters in tags
         this.tagTextView.setFilters(tagFilter);
        // Start as disabled, enable once something is written
        this.addTagButton.setEnabled(false);
        
 		mLocalBroadcastManager = LocalBroadcastManager.getInstance(context);
 		IntentFilter filter = new IntentFilter();
         filter.addAction(TagSuggestionService.ACTION_AUTOCOMPLETE_TAGS);
         filter.addAction(TagSuggestionService.ACTION_LAST_USED_TAGS);
         this.mReceiver = new BroadcastReceiver() {
             @Override
             public void onReceive(Context context, Intent intent) {
             	Log.d(TAG, "BroadcastReceiver.onReceive");
                 if (intent.getAction().equals(TagSuggestionService.ACTION_AUTOCOMPLETE_TAGS)) {
                 	Log.d(TAG, "Received ACTION_AUTOCOMPLETE_TAGS");
                 	String[] autocompleteTags = intent.getStringArrayExtra(
                 			TagSuggestionService.BUNDLE_TAG_LIST
                 	);
             		ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
                             android.R.layout.simple_dropdown_item_1line, autocompleteTags);
             		TaggingTool.this.tagTextView.setAdapter(adapter);
                 } else if (intent.getAction().equals(TagSuggestionService.ACTION_LAST_USED_TAGS)) {
                 	Log.d(TAG, "Received ACTION_LAST_USED_TAGS");
                 	String[] lastUsedTags = intent.getStringArrayExtra(
                 			TagSuggestionService.BUNDLE_TAG_LIST
                 	);
                 	for(String tag : lastUsedTags) {
                 		TaggingTool.this.addTag(new Tag(tag), false);
                 	}
                 }
             }
         };
         mLocalBroadcastManager.registerReceiver(mReceiver, filter);
 		
 
        	this.addTagButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View arg0) {
 				TaggingTool.this.addTagFromTextView();
 			}
 		});
        	
        	this.tagTextView.addTextChangedListener(new TextWatcher() {
        		// disable add button if text field is empty
 			public void afterTextChanged(Editable s) {
 				if(s.length() > 0) {
 					TaggingTool.this.addTagButton.setEnabled(true);
 				} else {
 					TaggingTool.this.addTagButton.setEnabled(false);
 				}
 			}
        		
        		// empty implementations for unneeded methods
 			public void onTextChanged(CharSequence s, int start, int before, int count) {}
 			public void beforeTextChanged(CharSequence s, int start, int count,
 					int after) {}
 		});
       	
 		this.setOrientation(VERTICAL);
 		parent.addView(this);
 		
 		Log.d(TAG, "TaggingTool created");
 		TagSuggestionService.requestTagBroadcast(context);
 		
 		// DEBUG
 		//this.addTag(new Tag("Testing tagging tool"), false);
 		//this.addTag(new Tag("mmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm"), false);
 		//this.addTag(new Tag("Checked"), true);
 	}
 	
 	/**
 	 * LinearLayout does not have onDestroy, so this is not an override.
 	 * Using the same naming scheme as android for consistency.
 	 */
 	public void onDestroy() {
 		mLocalBroadcastManager.unregisterReceiver(this.mReceiver);
 	}
 	
 	/**
 	 * Adds the text currently in the text view as a tag
 	 */
 	public void addTagFromTextView() {
 		if(this.tagTextView.length() == 0) {
 			// don't add empty tags
 			return;
 		}
 		this.addTag(
 				new Tag(TaggingTool.this.tagTextView.getText()),
 				true
 		);
 		this.tagTextView.setText("");
 	}
 	
 	/**
 	 * Adds a single new tag, or marks the corresponding tag as selected
 	 * if it already exists.
 	 * @param tag The tag to add.
 	 * @param checked Should the tag be initially checked.
 	 */
 	public void addTag(Tag tag, boolean checked) {
 		for(TagCheckBox button : this.buttons) {
 			if(button.getTag().equals(tag)) {
 				button.setChecked(true);
 				return;
 			}
 		}
 		TagCheckBox button = new TagCheckBox(getContext(), tag);
 		button.setChecked(checked);
 		this.tagFlowLayout.addView(button);
 		this.buttons.add(button);
 	}
 
 	/**
 	 * Returns a list of only those tags that have been selected by the user.
 	 * @return a list of all checked tags.
 	 */
 	public List<Tag> getCheckedTags() {
 		List<Tag> out = new ArrayList<Tag>(this.buttons.size());
 		for(TagCheckBox button : this.buttons) {
 			if(button.isChecked()) {
 				out.add(button.getTag());
 			}
 		}
 		return out;
 	}
 	
 	/**
 	 * A clickable checkbox representing one Tag. 
 	 */
 	private class TagCheckBox extends CheckBox {
 		private Tag tag;
 		public TagCheckBox(Context context, Tag tag) {
 			super(context);
 			this.tag = tag;
 			this.setBackgroundResource(R.drawable.tagcheckbox);
 			this.setTextColor(Color.BLACK);
 			this.setTextSize(TEXT_SIZE);
 			this.setSingleLine(true);
 			this.setEllipsize(TruncateAt.END);
 			this.setText(tag.getName());
 			this.setHeight(60);	//FIXME these are raw pixels. Find a way to use density invariant pixels (dp) instead
 			this.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 				public void onCheckedChanged(CompoundButton button, boolean isChecked) {
 					if(isChecked) {
 						//FIXME define colors
 						button.getBackground().setColorFilter(
 								new LightingColorFilter(COLOR_SELECTED_MUL, COLOR_SELECTED_ADD)
 						);
 					} else {
 						button.getBackground().setColorFilter(null);
 					}
 				}
 			});
 		}
 		public Tag getTag() {
 			return this.tag;
 		}
 	}
 }
