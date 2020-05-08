 package org.verba.mobile;
 
 import org.verba.boundary.PhraseLookup;
 import org.verba.mobile.task.LookupPhraseTask;
 import org.verba.mobile.utils.WordUtils;
 import org.verba.mobile.widget.PhraseDefinitionView;
 import org.verba.mobile.xdxf.AndroidXdxfNodeDisplay;
 import org.verba.stardict.PhraseDefinition;
 import org.verba.stardict.PhraseDefinitionPart;
 import org.verba.xdxf.XdxfPhraseDefinitionElement;
 import org.verba.xdxf.node.XdxfElement;
 
 import roboguice.inject.InjectExtra;
 import roboguice.inject.InjectView;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.text.Selection;
 import android.text.Spannable;
 import android.text.SpannableStringBuilder;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnLongClickListener;
 import android.view.View.OnTouchListener;
 import android.view.ViewGroup;
 import android.widget.LinearLayout;
 import android.widget.TextView.BufferType;
 import android.widget.Toast;
 
 import com.google.inject.Inject;
 
 public class PhraseDefinitionDetailsActivity extends VerbaActivity {
 	public static final String PHRASE_TO_LOOKUP = "phraseToLookup";
 	public static final String CARD_PHRASE_PARAMETER = "cardPhrase";
 	public static final String CARD_DEFINITION_PARAMETER = "cardDefinition";
 	private WordUtils wordUtils = new WordUtils();
 	private int lastTapCharOffsetInItsBox;
 	@InjectView(R.id.phraseDefinitionsShowcase) private ViewGroup phraseDefinitionsShowcase;
 	@Inject private PhraseLookup phraseLookup;
 	@InjectExtra(PHRASE_TO_LOOKUP) private String phraseToLookup;
 
 	private OnLongClickListener phraseDefinitionDetailsViewLongClickListener = new OnLongClickListener() {
 		@Override
 		public boolean onLongClick(View eventView) {
 			hideAllSelectionHandles();
 			PhraseDefinitionView phraseDefinitionBox = (PhraseDefinitionView) eventView;
 			wordUtils.selectWordAtLastTapOffset(phraseDefinitionBox.getText(), lastTapCharOffsetInItsBox);
 			phraseDefinitionBox.showSelectionHandles();
 			return true;
 		}
 	};
 
 	private OnTouchListener phraseDefinitionDetailsViewTouchListener = new OnTouchListener() {
 		@Override
 		public boolean onTouch(View eventView, MotionEvent event) {
 			PhraseDefinitionView phraseDefinitionBox = (PhraseDefinitionView) eventView;
 			if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
 				final int x = (int) event.getX();
 				final int y = (int) event.getY();
 
 				lastTapCharOffsetInItsBox = phraseDefinitionBox.getCharOffsetForCoordinates(x, y);
 			}
 
 			return false;
 		}
 	};
 
 	private OnClickListener onUseSelectionButtonClickListener = new OnClickListener() {
 		@Override
 		public void onClick(View v) {
 			Intent commandToOpenEditCardActivity = new Intent(PhraseDefinitionDetailsActivity.this, EditCardActivity.class);
 			commandToOpenEditCardActivity.putExtra(CARD_PHRASE_PARAMETER, getIntent().getStringExtra(PHRASE_TO_LOOKUP));
 			commandToOpenEditCardActivity.putExtra(CARD_DEFINITION_PARAMETER, getSelection());
 			startActivity(commandToOpenEditCardActivity);
 		}
 	};
 
 	private void hideAllSelectionHandles() {
 		for (int i = 0; i < phraseDefinitionsShowcase.getChildCount(); i++) {
 			PhraseDefinitionView phraseDefinitionBox = (PhraseDefinitionView) phraseDefinitionsShowcase.getChildAt(i);
 			phraseDefinitionBox.hideSelectionHandles();
 			Selection.removeSelection((Spannable) phraseDefinitionBox.getText());
 		}
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		showLookingForDefinitionProgress();
 		lookupPhraseDefinitions();
 	}
 
 	private void showLookingForDefinitionProgress() {
 		//displayText("Looking for definition...");
 	}
 
 	@Override
 	protected boolean loadSystemMenu() {
 		return true;
 	}
 
 	@Override
 	protected int getContentLayout() {
 		return R.layout.phrase_definition_details;
 	}
 
 	private void lookupPhraseDefinitions() {
		new LookupPhraseTask(this, phraseLookup, phraseToLookup);
 	}
 
 	public void displayPhraseLookupFailure() {
 		Toast.makeText(this, R.string.errorPhraseLookupFailed, Toast.LENGTH_SHORT).show();
 	}
 
 	private void displayText(CharSequence toDisplay) {
 		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		PhraseDefinitionView phraseDefinitionBox =
 				(PhraseDefinitionView) inflater.inflate(R.layout.phrase_definition_box_template, null);
 
 		setupPhraseDefinitionBoxListeners(phraseDefinitionBox);
 		phraseDefinitionBox.setText(toDisplay, BufferType.SPANNABLE);
 
 		phraseDefinitionsShowcase.addView(phraseDefinitionBox, getMarginLayoutParams());
 	}
 
 	private LinearLayout.LayoutParams getMarginLayoutParams() {
 		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
 				LinearLayout.LayoutParams.FILL_PARENT,
 				LinearLayout.LayoutParams.WRAP_CONTENT);
 
 		layoutParams.setMargins(5, 5, 5, 20);
 		return layoutParams;
 	}
 
 	private void setupPhraseDefinitionBoxListeners(PhraseDefinitionView phraseDefinitionBox) {
 		phraseDefinitionBox.setOnLongClickListener(phraseDefinitionDetailsViewLongClickListener);
 		phraseDefinitionBox.setOnTouchListener(phraseDefinitionDetailsViewTouchListener);
 		phraseDefinitionBox.setOnUseSelectionButtonClick(onUseSelectionButtonClickListener);
 	}
 
 	private CharSequence asSpannableString(XdxfElement xdxfArticle) {
 		SpannableStringBuilder spannable = new SpannableStringBuilder();
 
 		AndroidXdxfNodeDisplay xdxfDisplay = new AndroidXdxfNodeDisplay(this, spannable);
 		xdxfArticle.print(xdxfDisplay);
 
 		return spannable;
 	}
 
 	public void displayPhraseDefinition(PhraseDefinition phraseDefinition) {
 		PhraseDefinitionPart phraseDefinitionPart = phraseDefinition.parts().next();
 		XdxfPhraseDefinitionElement phraseDefinitionElement =
 				(XdxfPhraseDefinitionElement) phraseDefinitionPart.elements().next();
 		displayText(asSpannableString(phraseDefinitionElement.asXdxfArticle()));
 	}
 
 	public void lookupAnotherPhrase(String anotherPhraseToLookup) {
 		Intent commandToOpenPhraseDefinitionDetails = new Intent(this, PhraseDefinitionDetailsActivity.class);
 		commandToOpenPhraseDefinitionDetails.putExtra(PHRASE_TO_LOOKUP, anotherPhraseToLookup);
 		startActivity(commandToOpenPhraseDefinitionDetails);
 	}
 
 	private String getSelection() {
 		View currentlyFocusedView = getCurrentFocus();
 		if (currentlyFocusedView != null) {
 			// FIX: the line below throws class cast to LinearLayout
 			PhraseDefinitionView phraseDefinitionBox = (PhraseDefinitionView) currentlyFocusedView;
 			int selectionStart = phraseDefinitionBox.getSelectionStart();
 			int selectionEnd = phraseDefinitionBox.getSelectionEnd();
 			return phraseDefinitionBox.getText().subSequence(selectionStart, selectionEnd).toString();
 		} else {
 			return null;
 		}
 	}
 }
