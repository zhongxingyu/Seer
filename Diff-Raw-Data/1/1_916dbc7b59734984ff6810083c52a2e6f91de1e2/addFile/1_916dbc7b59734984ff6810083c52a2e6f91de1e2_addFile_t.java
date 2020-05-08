 package com.johndaniel.glosar;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.actionbarsherlock.view.MenuInflater;
 
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup.LayoutParams;
 import android.view.WindowManager;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.view.inputmethod.EditorInfo;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 import android.widget.ScrollView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class addFile extends SherlockActivity {
 	int counter; 
 	int text;
 	Activity activityRaw;
 	EditText nameField; //The name of the training. Needs to be placed here to be accessed by all methods.
 	public static final String PREF_MISC = "StoreSettings";
 	public static final String PREF_FILES = "FileStorage";
 	SharedPreferences thisFilePrefs;
 	public final static String REBOOT_MESSAGE = "com.erlaa.glosor.REBOOT_MESSAGE";
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_add_new);
 		
 		
 		activityRaw = this;
 		//Getting number of trainings
 		//Used to name file, and also decide if whe need the tutorial on StartPoint.java
 		SharedPreferences filePrefs = getSharedPreferences(PREF_MISC, 0);
 		int filesNumber = filePrefs.getInt("numberOfFiles", 0);
 
 		
 		//Write toast containing the amount of files.
 		//This is for development only, it will get deleted on release. 
 		Context context = getApplicationContext();
 		CharSequence toastText = filesNumber + " filer";
 		int duration = Toast.LENGTH_SHORT;
 		Toast toast = Toast.makeText(context, toastText, duration);
 		toast.show();
 
 		//Pairing constant views id's to variables.
 		final RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.RelativeLayout);
 		final ScrollView containerScrollView = (ScrollView) findViewById(R.id.container);
 		final LinearLayout addWordButton = (LinearLayout) findViewById(R.id.bottomButton);
 		nameField = (EditText) findViewById(R.id.addNewEditText);
 		
 		
 		
 		//Place the initial EditTexts
 		
 		//Calculate width
 		int dps = 48; //Value, in this case height, described in density pixels
 		//Calculate the density pixels height in normal pixels.
 		final float scale = getBaseContext().getResources().getDisplayMetrics().density;
 		final int pixels = (int) (dps * scale + 0.5f);
 		//The layoutparams for the editTexts. This will give the EditTexts an height of pixels (calculated dps), the weight 1.
 		//The weight will cause the EditTexts to share the amount of space inside the container. 
 		final LayoutParams editTextWeightParams = new LinearLayout.LayoutParams(0, pixels, 1.0f);
 		
 		
 		counter = 2;
 		/* The counter is the key for this operation. It helps 
 		 * us to keep track on the id's for the editTexts and the 
 		 * wrapper. Each editText has a value given from the counter,
 		 * and each wrapper has the -counter id value. */
 		
 		//Here begins the creation of the editTexts.
 		//Create two editTexts, one for the word (key), and one for the translation
 		EditText initword1 = new EditText(addFile.this); 
 		EditText initword2 = new EditText(addFile.this);
 		
 		//Create the container for the two editTexts
 		LinearLayout initWordWrapper = new LinearLayout(addFile.this);
 		initWordWrapper.setBackgroundColor(0xFFFFFFFF);
 		initWordWrapper.setOrientation(LinearLayout.HORIZONTAL);
 
 		
 		//The two EditTexts must have same width, therefore they have both the weight of 1 (1.0f)
 		initword1.setLayoutParams(editTextWeightParams);
 		initword2.setLayoutParams(editTextWeightParams);
 		
 		
 		//add the EditTexts to the container
 		initWordWrapper.addView(initword1);
 		initWordWrapper.addView(initword2);
 		
 		//Set default help texts.
 		initword1.setHint(getString(R.string.word));
 		initword2.setHint(getString(R.string.translation));
 		
 		//The newly added wordWrapper must always be placed below the already placed wordWrapper
 		//In this case, there are no wordwrappers.
 		RelativeLayout.LayoutParams wrapperParams = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, pixels);
 		/* The wrapper must always be placed under the previous wrapper. 
 		 * The wrapper has the id -counter, counter+1 will give the us the
 		 * id of the previously added counter. */
 		wrapperParams.addRule(RelativeLayout.BELOW, -counter+1);
 
 		initWordWrapper.setLayoutParams(wrapperParams);
 		
 		//Setting up the id's. 
 		/*The idea is to make it possible to get the Strings from id's using a 
 		 * for loop and then printing them to this sharedPreference file. 
 		 * 
 		 * String from id1 = String from id2
 		 * String from id3 = String from id4
 		 * etc.*/
 		
 		initword1.setId(counter);
 		initWordWrapper.setId(-counter);
 		counter++;
 		initword2.setId(counter);
 				
 		//Scroll down to bottom after added the new wordWrapper.
 		((RelativeLayout) relativeLayout).addView(initWordWrapper);
 		
 		//animate in the wordWrapper
 		Animation animation = AnimationUtils.loadAnimation(getBaseContext(), R.anim.slide_right_in);
 		animation.setStartOffset(0);
 		initWordWrapper.startAnimation(animation);
 		
 		//Make the editTexts single line
 		initword1.setSingleLine();
 		initword2.setSingleLine();
 		
 		//nameField.requestFocus();
 		initword1.requestFocus();
 		initword1.setNextFocusDownId(initword2.getId());
 		initword2.setNextFocusDownId(initword2.getId() + 1);
 		
 		//User clicked addWord button
 		addWordButton.setOnClickListener(new OnClickListener() { //ButtonClick, add new wordset
 
 			@Override
 			public void onClick(View v) {
 				//The two words paired to each other:
 				EditText word1 = new EditText(addFile.this); 
 				EditText word2 = new EditText(addFile.this);
 				
 				//The container of the two EditTexts
 				LinearLayout wordWrapper = new LinearLayout(addFile.this);
 				wordWrapper.setBackgroundColor(0xFFFFFFFF);
 				wordWrapper.setOrientation(LinearLayout.HORIZONTAL);
 				
 				//The two EditTexts must have same width, therefore they have both the weight of 1 (1.0f)
 				word1.setLayoutParams(editTextWeightParams);
 				word2.setLayoutParams(editTextWeightParams);
 				
 				//add the EditTexts to the container
 				wordWrapper.addView(word1);
 				wordWrapper.addView(word2);
 				
 				
 				//The newly added wordWrapper must always be placed below the already placed wordWrapper
 				RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, pixels);
 				layoutParams.addRule(RelativeLayout.BELOW, -counter+1);
 				
 				
 				wordWrapper.setLayoutParams(layoutParams);
 				
 				//Setting up the id's. 
 				/*The idea is to make it possible to get the Strings from id's using a 
 				 * for loop and then printing them to this sharedPreference file. 
 				 * 
 				 * String from id1 = String from id2
 				 * String from id3 = String from id4
 				 * etc.*/
 
 				counter++;
 				word1.setId(counter);
 				wordWrapper.setId(-counter);
 				counter++;
 				word2.setId(counter);
 				
 				//Add the wordwrapper. 
 				((RelativeLayout) relativeLayout).addView(wordWrapper);
 				
 				//animate in the wordWrapper
 				Animation animation = AnimationUtils.loadAnimation(getBaseContext(), R.anim.slide_right_in);
 				animation.setStartOffset(0);
 				wordWrapper.startAnimation(animation);
 				
 				//Remove enter button 
 				word1.setSingleLine();
 				word2.setSingleLine();
 				word1.setFocusableInTouchMode(true);
 				word1.requestFocus();
 				//Request keyboard
 				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
 				imm.showSoftInput(word1, InputMethodManager.SHOW_IMPLICIT);
 				//Set focus order. 
 				word1.setNextFocusDownId(word2.getId());
 				word2.setNextFocusDownId(word2.getId() + 1);
 			}	
 		});			
 	}
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		MenuInflater inflater = getSupportMenuInflater();
 		inflater.inflate(R.menu.add_file, menu);
 		return true;	
 	}
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		Intent intent = new Intent(this, StartPoint.class);
 		switch (item.getItemId()) {
 		case R.id.addFileSaveButton:
 			saveOperation(activityRaw);
 			startActivity(intent);
 			return true;
 		case R.id.addFileCancelButton:
 			startActivity(intent);
 			finish();
 			return true;
 		default: return super.onOptionsItemSelected(item);
 		}
 	}
 	public void saveOperation(Activity activity) {
 		//first check. Is there any any name?
 		if (nameField.getText().toString().equals("")){
 			//nameField is empty.
 			Toast saveErr = Toast.makeText(getApplicationContext(), getResources().getString(R.string.name_the_exercise).toString(), Toast.LENGTH_LONG);
 			saveErr.show();
 		}
 		else {
 			//Get the amount of files.
 			SharedPreferences filePrefsMisc = getSharedPreferences(PREF_MISC, 0);
 			SharedPreferences.Editor filesEditorMisc = filePrefsMisc.edit();
 			int filesNumber = filePrefsMisc.getInt("numberOfFiles", 0);
 			//Get the amount of files again to see if it was the first the saveOperation() was executed. 
 			//Needed to determinate if the tutorial is still necessary. 
 			int oldFilesNumber = filesNumber;
 			filesNumber++;
 
 
 			//Writing to StoreSettings - write the new total amount of files
 			filesEditorMisc.putInt("numberOfFiles", filesNumber);
 			filesEditorMisc.commit();
 			String thisFileReferense = "" + filesNumber;
 
 
 			//Get PREF_FILES
 			SharedPreferences filePrefsFiles = getSharedPreferences(PREF_FILES, 0);
 			SharedPreferences.Editor filesEditorFiles = filePrefsFiles.edit();
 
 
 			//Save the stuff witten into this app
 			String thisPrefName = nameField.getText().toString();
 			thisFilePrefs = getSharedPreferences(thisPrefName, 0);
 			
 			//Here goes the stuff from the edittexts.'
 			if(counter > 1){ //To avoid null
 				Toast saveErr = Toast.makeText(getApplicationContext(), "For-loop krs", Toast.LENGTH_LONG);
 				saveErr.show();
 				for(int i = 2; i < counter; i = i + 2){
 					EditText word1 = (EditText) activity.findViewById(i);
 					EditText word2 = (EditText) activity.findViewById(i + 1);
 					String word1Text = word1.getText().toString();
 					String word2Text = word2.getText().toString();
 					
 					
 					thisFilePrefs.edit().putString(word1Text, word2Text).commit();
 				}
 			}
 			
 			
 			Toast confirmSaveToast = Toast.makeText(getApplicationContext(), getString(R.string.saving___), Toast.LENGTH_LONG);
 			confirmSaveToast.show();
 			if (oldFilesNumber == 0){
 				/* If the the oldFileNumber is 0, then it means that the tutorial was
 				 * active. We now need to restart StartPoint.java activity to get rid of the
 				 * tutorial. I do it using an intent and then check if an 
 				 * intent was received in StartPoint.java.
 				 * 
 				 *	I'm sure there's a better way to do it. Please make pull request if you have any ideas. */
 				
 				
 				//For development. Deleted on release.
 				Toast.makeText(getApplicationContext(), "Skickar intentmeddelande", Toast.LENGTH_LONG).show();
 				
 				//Making and starting the intent. 
 				Intent intent = new Intent(this, StartPoint.class);
 				intent.putExtra(REBOOT_MESSAGE, true);
 				startActivity(intent);
 
 				}
 				//Just save it.
 				//ThisFileReference is equal to the filesNumber+1
 				//ThisFileReference references to the name of is training in sharedPreferences
 				filesEditorFiles.putString(thisFileReferense, nameField.getText().toString());
 				filesEditorFiles.commit();
 				finish();
 			}
 		}
 	@Override
 	public void onBackPressed() {
 		// TODO Auto-generated method stub
 		Intent intent = new Intent(this, StartPoint.class);
 		startActivity(intent);
		finish();
 	}
 	
 	}
 	/*	public boolean needTutorialCheck() {
 		//check if we got some words
 				final SharedPreferences filesPrefs = getSharedPreferences(PREF_MISC, 0);
 				int trueCheck = filesPrefs.getInt("numberOfFiles", 0);
 				if (trueCheck == 0){
 					return true; 
 				} else {
 					return false;
 				}		
 			}*/
