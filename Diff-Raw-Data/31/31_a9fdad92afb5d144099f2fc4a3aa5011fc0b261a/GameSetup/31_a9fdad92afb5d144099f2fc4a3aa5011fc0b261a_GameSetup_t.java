 /*****************************************************************************
  *  PhraseCraze is a family friendly word game for mobile phones.
  *  Copyright (C) 2011 Siramix Team
  *  
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  ****************************************************************************/
 package com.siramix.phrasecraze;
 
 import java.util.LinkedList;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.sqlite.SQLiteException;
 import android.graphics.Typeface;
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.RadioButton;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.view.animation.Animation;
 import android.view.animation.AlphaAnimation;
 
 /**
  * This activity class is responsible for gathering game information before the
  * game starts such as number of teams and turns
  * 
  * @author Siramix Labs
  */
 public class GameSetup extends Activity {
 
   static final int DIALOG_TEAMERROR = 0;
   private LinkedList<Team> mTeamList = new LinkedList<Team>();
   private static SharedPreferences mGameSetupPrefs;
   private static SharedPreferences.Editor mGameSetupPrefEditor;
 
   // Ids for TeamSelectLayouts
   final int[] TEAM_SELECT_LAYOUTS = new int[] { R.id.GameSetup_TeamALayout,
       R.id.GameSetup_TeamBLayout, R.id.GameSetup_TeamCLayout,
       R.id.GameSetup_TeamDLayout };
   
   // TextView that displays score limit
   private TextView mScoreLimitView;
   
   // Int that stores the score limit
   private int mScoreLimit;
 
   // Preference keys (indicating quadrant)
   public static final String PREFS_NAME = "gamesetupprefs";
 
   // Flag to play music into the next Activity
   private boolean mContinueMusic = false;
  
  private Toast mHelpToast = null;
 
   // Request code for EditTeam activity result
   static final int EDITTEAMNAME_REQUEST_CODE = 1;
 
   /**
    * logging tag
    */
   public static String TAG = "GameSetup";
 
   /**
   * Handle showing a toast or refreshing an existing toast
   */
  private void showToast(String text) {
    if(mHelpToast == null) {
      mHelpToast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG);
    } else {
      mHelpToast.setText(text);
      mHelpToast.setDuration(Toast.LENGTH_LONG);
    }
    mHelpToast.show();
  }
  
  /**
    * Creates the animation that fades in the helper text
    * 
    * @return the animation that fades in the helper text
    */
   private Animation fadeInHelpText(long delay) {
     if (PhraseCrazeApplication.DEBUG) {
       Log.d(TAG, "FadeInHelpText()");
     }
     Animation fade = new AlphaAnimation(0.0f, 1.0f);
     fade.setStartOffset(delay);
     fade.setDuration(2000);
     return fade;
   }
 
   /**
    * Watches the button that handles hand-off to the Turn activity.
    */
   private final OnClickListener mStartGameListener = new OnClickListener() {
     public void onClick(View v) {
       if (PhraseCrazeApplication.DEBUG) {
         Log.d(TAG, "StartGameListener onClick()");
       }
 
       // Validate team numbers
       if (GameSetup.this.mTeamList.size() != 2) {
         GameSetup.this.showDialog(DIALOG_TEAMERROR);
         return;
       }
       
       // Store off game's attributes as preferences
       savePreferences();
 
       // Create a GameManager to manage attributes about the current game.
       // the while loop around the try-catch block makes sure the database
       // has loaded before actually starting the game.
       PhraseCrazeApplication application = (PhraseCrazeApplication) GameSetup.this
           .getApplication();
       boolean keepLooping = true;
       while (keepLooping) {
         try {
           GameManager gm = new GameManager(GameSetup.this);
           gm.startGame(mTeamList, mScoreLimit, isScoringModeAssisted());
           application.setGameManager(gm);
           keepLooping = false;
         } catch (SQLiteException e) {
           keepLooping = true;
         }
       }
 
       // Launch into Turn activity
       startActivity(new Intent(getApplication().getString(R.string.IntentTurn),
           getIntent().getData()));
 
       // Stop the music
       MediaPlayer mp = application.getMusicPlayer();
       mp.stop();
     }
   };
 
   /**
    * Watches the button to add a point to Score Limit
    */
   private final OnClickListener mAddPointScoreLimit = new OnClickListener() {
     public void onClick(View v) {
       if (PhraseCrazeApplication.DEBUG) {
         Log.d(TAG, "mAddPointScoreLimit onClick()");
       }
 
       mScoreLimit += 1;
       mScoreLimitView.setText(Integer.toString(mScoreLimit));
       
       // play confirm sound when points are added
       SoundManager sm = SoundManager.getInstance(GameSetup.this.getBaseContext());
       sm.playSound(SoundManager.Sound.CONFIRM);
 
     }
   };
 
   /**
    * Watches the help button for the Teams section
    */
   private final OnClickListener mTeamHelpListener = new OnClickListener() {
     public void onClick(View v) {
       if (PhraseCrazeApplication.DEBUG) {
         Log.d(TAG, "mTeamHelpListener onClick()");
       }
      showToast(getString(R.string.gamesetup_teamshint));  
     }
   };
   
   /**
    * Watches the help button for the Teams section
    */
   private final OnClickListener mScoreLimitHelpListener = new OnClickListener() {
     public void onClick(View v) {
       if (PhraseCrazeApplication.DEBUG) {
         Log.d(TAG, "mTeamHelpListener onClick()");
       }
      showToast(getString(R.string.gamesetup_endgamerulehint));     
     }
   };
   
   /**
    * Watches the help button for the Teams section
    */
   private final OnClickListener mScoringModeHelpListener = new OnClickListener() {
     public void onClick(View v) {
       if (PhraseCrazeApplication.DEBUG) {
         Log.d(TAG, "mTeamHelpListener onClick()");
       }
      showToast(getString(R.string.gamesetup_scoringmodehint));  
     }
   };
   /**
    * Watches the button to remove a point from the Score Limit
    */
   private final OnClickListener mSubtractPointScoreLimit = new OnClickListener() {
     public void onClick(View v) {
       if (PhraseCrazeApplication.DEBUG) {
         Log.d(TAG, "mSubtractPointScoreLimit onClick()");
       }
 
       // Don't let them set a score limit below 1
       if( mScoreLimit > 1 )
       {
     	  mScoreLimit -= 1;
 	      mScoreLimitView.setText(Integer.toString(mScoreLimit));
 	      
 	      // play confirm sound when points are added
 	      SoundManager sm = SoundManager.getInstance(GameSetup.this.getBaseContext());
 	      sm.playSound(SoundManager.Sound.BACK);
       }
     }
   };
 
   /*
    * Edit team name listener to launch Edit Team name dialog
    */
   private final OnTeamEditedListener mTeamEditedListener = new OnTeamEditedListener() {
     public void onTeamEdited(Team team) {
       SoundManager sm = SoundManager.getInstance(GameSetup.this
           .getBaseContext());
       sm.playSound(SoundManager.Sound.CONFIRM);
 
       Intent editTeamNameIntent = new Intent(
           getString(R.string.IntentEditTeamName), getIntent().getData());
       editTeamNameIntent.putExtra(getString(R.string.teamBundleKey), team);
       startActivityForResult(editTeamNameIntent, EDITTEAMNAME_REQUEST_CODE);
 
       mContinueMusic = true;
     }
   };
 
   /*
    * Listener that watches the TeamSelectLayouts for events when the teams are
    * added or removed. It modifies the preferences and the list of teams
    * accordingly.
    */
   private final OnTeamAddedListener mTeamAddedListener = new OnTeamAddedListener() {
     public void onTeamAdded(Team team, boolean isTeamOn) {
       SoundManager sm = SoundManager.getInstance((GameSetup.this
           .getBaseContext()));
 
       if (isTeamOn) {
         // Add the team to the list
         mTeamList.add(team);
         // Store off this selection so it is remember between activities
         mGameSetupPrefEditor.putBoolean(team.getPreferenceKey(), true);
         // Play confirm sound on add
         sm.playSound(SoundManager.Sound.CONFIRM);
       } else {
         // Remove the team from the list
         mTeamList.remove(team);
         // Store off this selection so it is remember between activities
         mGameSetupPrefEditor.putBoolean(team.getPreferenceKey(), false);
         // Play back sound on remove
         sm.playSound(SoundManager.Sound.BACK);
       }
     }
   };
 
   /**
    * This function is called when the EditTeamName activity finishes. It
    * refreshes all Layouts.
    */
   @Override
   protected void onActivityResult(int requestCode, int resultCode, Intent data) {
     if (requestCode == EDITTEAMNAME_REQUEST_CODE &&
         resultCode == Activity.RESULT_OK &&
         data.getExtras() != null) {
       
       // Get team and team name from dialog
       String curTeamName = data.getStringExtra(getString(R.string.teamNameBundleKey));
       Team curTeam = (Team) data.getSerializableExtra(getString(R.string.teamBundleKey));
       
       if(curTeamName != null && curTeam != null) {
         
         // Set the team name and update the layout
         curTeam.setName(curTeamName);      
         TeamSelectLayout teamSelect = (TeamSelectLayout) this.findViewById(TEAM_SELECT_LAYOUTS[curTeam.ordinal()]);
         teamSelect.setTeam(curTeam);
         
         // Set the name as a pref
         mGameSetupPrefEditor.putString(curTeam.getDefaultName(), curTeam.getName());
         mGameSetupPrefEditor.commit();
         
         }
     }
     super.onActivityResult(requestCode, resultCode, data);
   }
   
   /**
    * Get references to all of the UI elements that we need to work with after
    * the activity creation
    */
   protected void setupViewReferences() {
     if (PhraseCrazeApplication.DEBUG) {
       Log.d(TAG, "setupViewReferences()");
     }
     
     mScoreLimitView = (TextView) this.findViewById(R.id.GameSetup_ScoreLimit_Value);
   }
   
   /**
    * Returns true if the user has checked assisted scoring mode
    * @return true if the user has checked assisted scoring mode
    */
   protected boolean isScoringModeAssisted()
   {
     RadioButton radioButtonAssisted = (RadioButton) GameSetup.
         this.findViewById(R.id.GameSetup_ScoringMode_Assisted);
     return radioButtonAssisted.isChecked();
   }
   
   /**
    * Helper function to store any preferences that aren't stored on
    * click
    */
   protected void savePreferences()
   {
     GameSetup.mGameSetupPrefEditor.putInt(GameSetup.this.getString(R.string.PREFKEY_SCORELIMIT), mScoreLimit);
     GameSetup.mGameSetupPrefEditor.putBoolean(
         GameSetup.this.getString(R.string.PREFKEY_AUTOSCORINGENABLED), isScoringModeAssisted());
     GameSetup.mGameSetupPrefEditor.commit();    
   }
   
   /**
    * Initializes the activity to display the results of the turn.
    * 
    * @param savedInstanceState
    *          bundle used for saved state of the activity
    */
   @Override
   public void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     if (PhraseCrazeApplication.DEBUG) {
       Log.d(TAG, "onCreate()");
     }
 
     // Initialize flag to carry music from one activity to the next
     mContinueMusic = false;
 
     // Force volume controls to affect Media volume
     setVolumeControlStream(AudioManager.STREAM_MUSIC);
 
     // Setup the view
     this.setContentView(R.layout.gamesetup);
     
     this.setupViewReferences();
 
     // Get the current game setup preferences
     GameSetup.mGameSetupPrefs = getSharedPreferences(PREFS_NAME, 0);
     GameSetup.mGameSetupPrefEditor = GameSetup.mGameSetupPrefs.edit();
 
     // set fonts on titles
     Typeface antonFont = Typeface.createFromAsset(getAssets(),
         "fonts/Anton.ttf");
 
     TextView label = (TextView) this.findViewById(R.id.GameSetup_Title);
     label.setTypeface(antonFont);
     label = (TextView) this.findViewById(R.id.GameSetup_TeamsTitle);
     label.setTypeface(antonFont);
     label = (TextView) this.findViewById(R.id.GameSetup_Header_ScoreLimit_Title);
     label.setTypeface(antonFont);
     label = (TextView) this.findViewById(R.id.GameSetup_Header_ScoringMode_Title);
     label.setTypeface(antonFont);
     mScoreLimitView.setTypeface(antonFont);
     
     // Set default value for score limit
     mScoreLimit = GameSetup.mGameSetupPrefs.getInt(GameSetup.this.getString(R.string.PREFKEY_SCORELIMIT), 7);
     mScoreLimitView.setText(Integer.toString(mScoreLimit));
     
     // Set default value for scoring mode
     boolean automatedScoringEnabled = GameSetup.mGameSetupPrefs.getBoolean(
         GameSetup.this.getString(R.string.PREFKEY_AUTOSCORINGENABLED), false);
     RadioButton defaultRadio;
     if (automatedScoringEnabled)
     {
       defaultRadio = (RadioButton) this.findViewById(R.id.GameSetup_ScoringMode_Assisted);
     }
     else
     {
       defaultRadio = (RadioButton) this.findViewById(R.id.GameSetup_ScoringMode_FreePlay);
     }
     defaultRadio.setChecked(true);
     
     // Assign teams to TeamSelectLayouts
     TeamSelectLayout teamSelect;
     Team curTeam;
     for (int i = 0; i < TEAM_SELECT_LAYOUTS.length; ++i) {
       curTeam = Team.values()[i];
       String curTeamName = mGameSetupPrefs.getString(curTeam.getDefaultName(), curTeam.getDefaultName());
       curTeam.setName(curTeamName);
       teamSelect = (TeamSelectLayout) this.findViewById(TEAM_SELECT_LAYOUTS[i]);
       teamSelect.setTeam(curTeam);
 
       if (GameSetup.mGameSetupPrefs.getBoolean(curTeam.getPreferenceKey(),
           false)) {
         teamSelect.setActiveness(true);
         mTeamList.add(curTeam);
       } else {
         teamSelect.setActiveness(false);
       }
       teamSelect.setOnTeamEditedListener(mTeamEditedListener);
       teamSelect.setOnTeamAddedListener(mTeamAddedListener);
     }
 
     // Do helper text animations
     TextView helpText = (TextView) this
         .findViewById(R.id.GameSetup_HelpText_Team);
     helpText.setAnimation(this.fadeInHelpText(500));
     helpText = (TextView) this.findViewById(R.id.GameSetup_HelpText_Turn);
     helpText.setAnimation(this.fadeInHelpText(1500));
     helpText = (TextView) this.findViewById(R.id.GameSetup_HelpText_ScoringMode);
     helpText.setAnimation(this.fadeInHelpText(2500));
     
 
     Button button = (Button) this.findViewById(R.id.GameSetup_StartGameButton);
     button.setOnClickListener(mStartGameListener);
     button = (Button) this.findViewById(R.id.GameSetup_ScoreLimit_ButtonPlus);
     button.setOnClickListener(mAddPointScoreLimit);
     button = (Button) this.findViewById(R.id.GameSetup_ScoreLimit_ButtonMinus);
     button.setOnClickListener(mSubtractPointScoreLimit);
     button = (Button) this.findViewById(R.id.GameSetup_Teams_HelpButton);
     button.setOnClickListener(mTeamHelpListener);
     button = (Button) this.findViewById(R.id.GameSetup_Header_ScoreLimit_HelpButton);
     button.setOnClickListener(mScoreLimitHelpListener);
     button = (Button) this.findViewById(R.id.GameSetup_Header_ScoringMode_HelpButton);
     button.setOnClickListener(mScoringModeHelpListener);
 
   }
 
   /**
    * Handle creation of team warning dialog, used to prevent starting a game
    * with too few teams. returns Dialog object explaining team error
    */
   @Override
   protected Dialog onCreateDialog(int id) {
     if (PhraseCrazeApplication.DEBUG) {
       Log.d(TAG, "onCreateDialog(" + id + ")");
     }
     Dialog dialog = null;
     AlertDialog.Builder builder = null;
 
     switch (id) {
     case DIALOG_TEAMERROR:
       builder = new AlertDialog.Builder(this);
       builder.setMessage("You must have exactly two teams to start the game.")
           .setCancelable(false).setTitle("Incorrect Teams!").setPositiveButton(
               "Okay", new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int id) {
                   dialog.cancel();
                 }
               });
       dialog = builder.create();
       break;
     default:
       dialog = null;
     }
     return dialog;
   }
 
   /**
    * Override back button to carry music on back to the Title activity
    * 
    * @return whether the event has been consumed or not
    */
   @Override
   public boolean onKeyUp(int keyCode, KeyEvent event) {
     if (keyCode == KeyEvent.KEYCODE_BACK && event.isTracking()
         && !event.isCanceled()) {
       if (PhraseCrazeApplication.DEBUG) {
         Log.d(TAG, "BackKeyUp()");
       }
       // Flag to keep music playing
       GameSetup.this.mContinueMusic = true;
     }
 
     return super.onKeyUp(keyCode, event);
   }
 
   /**
    * Override onPause to prevent activity specific processes from running while
    * app is in background
    */
   @Override
   public void onPause() {
     if (PhraseCrazeApplication.DEBUG) {
       if (PhraseCrazeApplication.DEBUG) {
         Log.d(TAG, "onPause()");
       }
     }
     super.onPause();
 
     // Pause the music unless going to an Activity where it is supposed to
     // continue through
     PhraseCrazeApplication application = (PhraseCrazeApplication) this
         .getApplication();
     MediaPlayer mp = application.getMusicPlayer();
     if (!mContinueMusic && mp.isPlaying()) {
       mp.pause();
     }
 
     // Store off game's attributes as preferences. This is done in Pause to
     // maintain selections when they press "back" to main title then return.
     savePreferences();
   }
 
   /**
    * Override OnResume to resume activity specific processes
    */
   @Override
   public void onResume() {
     if (PhraseCrazeApplication.DEBUG) {
       Log.d(TAG, "onResume()");
     }
     super.onResume();
 
     // Resume Title Music
     PhraseCrazeApplication application = (PhraseCrazeApplication) this
         .getApplication();
     MediaPlayer mp = application.getMusicPlayer();
     SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this
         .getBaseContext());
     if (!mp.isPlaying() && sp.getBoolean("music_enabled", true)) {
       mp.start();
     }
 
     mContinueMusic = false;
   }
 }
