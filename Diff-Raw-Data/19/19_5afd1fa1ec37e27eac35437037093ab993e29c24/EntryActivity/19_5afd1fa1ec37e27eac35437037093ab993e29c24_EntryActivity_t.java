 package com.ijuru.kumva.activity;
 
 import com.ijuru.kumva.Definition;
 import com.ijuru.kumva.KumvaApplication;
 import com.ijuru.kumva.R;
 import com.ijuru.kumva.util.Dialogs;
 import com.ijuru.kumva.util.Utils;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.drawable.AnimationDrawable;
 import android.graphics.drawable.Drawable;
 import android.media.MediaPlayer;
 import android.os.Bundle;
 import android.text.Spannable;
 import android.text.Spanned;
 import android.text.method.LinkMovementMethod;
 import android.text.style.ClickableSpan;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 
 /**
  * Activity to show details of a dictionary entry
  */
 public class EntryActivity extends Activity {
 	
 	private Definition definition;
 	private MediaPlayer player = new MediaPlayer();
 
 	/**
 	 * @see android.app.Activity#onCreate(android.os.Bundle)
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_entry);
 		
 		this.definition = ((KumvaApplication)getApplication()).getCurrentDefinition();
 		 
 		setItemTextOrHide(R.id.prefix, definition.getPrefix(), false);
 		setItemTextOrHide(R.id.lemma, definition.getLemma(), false);
 		
 		TextView modifier = (TextView)findViewById(R.id.modifier);
 		TextView pronunciation = (TextView)findViewById(R.id.pronunciation);
 		Button audioBtn = (Button)findViewById(R.id.audiobtn);
 		TextView wordclass = (TextView)findViewById(R.id.wordclass);
 		
 		if (!Utils.isEmpty(definition.getModifier()))
 			modifier.setText("(" + definition.getModifier() + ")");
 		
 		if (!Utils.isEmpty(definition.getPronunciation()))
 			pronunciation.setText("/" + definition.getPronunciation() + "/");
 		else
 			pronunciation.setVisibility(View.GONE);
 		
 		if (Utils.isEmpty(definition.getAudioURL()))
 			audioBtn.setVisibility(View.GONE);
 		
 		if (!Utils.isEmpty(definition.getWordClass())) {
 			String strIdName = "wcls_" + definition.getWordClass();
 			int strId = getResources().getIdentifier(strIdName, "string", "com.ijuru.kumva");
 			StringBuilder sb = new StringBuilder(getString(strId));
 			
 			if (definition.getNounClasses().size() > 0) {
 				sb.append(" (");
 				sb.append(getString(definition.getNounClasses().size() > 1 ? R.string.str_classes : R.string.str_class));
 				sb.append(" ");
 				sb.append(Utils.makeCSV(definition.getNounClasses()));
 				sb.append(")");
 			}
 			
 			wordclass.setText(sb.toString());
 		}
 		else
 			wordclass.setVisibility(View.GONE);
 			
 		setItemTextOrHide(R.id.meaning, Utils.formatMeanings(definition.getMeanings()), true);
 		setItemTextOrHide(R.id.comment, definition.getComment(), true);	
 		setItemTextOrHide(R.id.examples, Utils.formatExamples(definition.getExamples()), true);
 	}
 	
 	/**
 	 * Sets the text of a text view or hides it if the text is empty
 	 * @param itemId the text view identifier
 	 * @param text the text
 	 * @param parseRefs true if references in the text should be parsed
 	 */
 	private void setItemTextOrHide(int itemId, CharSequence text, boolean parseRefs) {
 		TextView view = (TextView)findViewById(itemId);
 		view.setMovementMethod(LinkMovementMethod.getInstance());
 		
 		if (!Utils.isEmpty(text)) {
 			if (parseRefs && text instanceof String)
 				text = parseReferences((String)text);
 			view.setText(text);
 		}
 		else
 			view.setVisibility(View.GONE);
 	}
 	
 	/**
 	 * Called when user clicks the listen/pause button
 	 * @param view the view
 	 */
 	public void playAudio(View view) {	
 		final Button audioBtn = (Button)findViewById(R.id.audiobtn);
 		final Drawable listenIcon = audioBtn.getCompoundDrawables()[0];
 		final Drawable pauseIcon = getResources().getDrawable(R.drawable.ic_pause);
 		pauseIcon.setBounds(listenIcon.getBounds());
 		
 		// If audio is playing then this is a pause button
 		if (player.isPlaying()) {
 			player.stop();
 			audioBtn.setCompoundDrawables(listenIcon, null, null, null);
 		}
 		else {	
 			// Load animated throbber
 			final AnimationDrawable anim = (AnimationDrawable)getResources().getDrawable(R.drawable.am_loading);
 			anim.setOneShot(false);
 			anim.setBounds(listenIcon.getBounds());
 			audioBtn.setCompoundDrawables(anim, null, null, null);
 			audioBtn.setEnabled(false);
 			anim.start();
 			
 			try {
 				// Reset player and connect to audio URL
 				player.reset();
 				player.setDataSource(definition.getAudioURL());
				player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {	
 					@Override
 					public void onPrepared(MediaPlayer arg0) {
 						// Switch to pause icon
 						player.start();
 						audioBtn.setEnabled(true);
 						audioBtn.setCompoundDrawables(pauseIcon, null, null, null);
 					}
 				});
				player.setOnErrorListener(new MediaPlayer.OnErrorListener() {		
					@Override
					public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
						audioBtn.setEnabled(true);
						audioBtn.setCompoundDrawables(listenIcon, null, null, null);
						Dialogs.toast(EntryActivity.this, getString(R.string.err_communicationfailed));
						return true;
					}
				});
				player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {	
 					@Override
 					public void onCompletion(MediaPlayer arg0) {
 						audioBtn.setCompoundDrawables(listenIcon, null, null, null);
 					}
 				});
 				player.prepareAsync();
 			} catch (Exception e) {
 				Dialogs.toast(this, getString(R.string.err_communicationfailed));
 			}
 		}
 	}
 	
 	/**
 	 * Parses references into clickable spans
 	 * @param text
 	 * @return
 	 */
 	private Spannable parseReferences(String text) {	
 		String clean = text.replace("{", "").replace("}", "");
 		Spannable spanner = Spannable.Factory.getInstance().newSpannable(clean);
 		
 		int start = 0;
 		for (int c = 0, cleanPos = 0; c < text.length(); c++) {
 			if (text.charAt(c) == '{') {
 				start = cleanPos;
 			}
 			else if (text.charAt(c) == '}') {
 				final int end = cleanPos;
 				final String ref = clean.substring(start, end);
 				spanner.setSpan(new ClickableSpan() {
 					@Override
 					public void onClick(View view) {
 						Intent intent = new Intent(getApplicationContext(), SearchActivity.class);
 						intent.putExtra("query", ref);
 						startActivity(intent);
 
 					}}, start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
 			}
 			else
 				++cleanPos;
 		}
 		
 		return spanner;
 	}
 }
