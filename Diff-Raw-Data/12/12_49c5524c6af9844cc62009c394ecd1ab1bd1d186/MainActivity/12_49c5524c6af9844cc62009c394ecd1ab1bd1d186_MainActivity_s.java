 package com.example.scorehighlights;
 
 import java.util.ArrayList;
 import org.json.JSONObject;
 
 import com.example.scorehighlights.DataPullThread;
 import com.example.scorehighlights.sports.SportsAbstract;
 
 import android.os.Bundle;
 import android.os.Handler;
 import android.app.Activity;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.Button;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.AdapterView.OnItemSelectedListener;
 
 /**
  * @author Jimmy Dagres
  * 
  * @version Sep 29, 2013
  * 
  */
 public class MainActivity extends Activity
 {
     // Keep a list of all of the selected sports
     protected static ArrayList<SportsAbstract<String, JSONObject>> ArrayOfEvents_ =
             null;
 
     // Store instances to the previous event and next event button
     Button previousEventButton_ = null;
     Button nextEventButton_ = null;
 
     /**
      * Stores whether a sport was selected
      */
     private boolean sportIsSelected = false;
 
     // Instance the asynchronous thread that gets and parses the data
     private DataPullThread asyncThread_ = null;
 
     private Spinner spinner1 = null;
 
     // Event 1 text dialogs
     protected TextView eventName1_ = null;
     protected TextView event1HomeCompetitor_ = null;
     protected TextView event1AwayCompetitor_ = null;
     protected TextView event1HomeCompetitorScore_ = null;
     protected TextView event1AwayCompetitorScore_ = null;
     protected TextView event1GameTime_ = null;
     protected TextView event1CurrentPossession_ = null;
     protected TextView eventBeingDisplayed_ = null;
 
     // Instance the headlines child class
     protected Headlines headlines_ = null;
 
     /**
      * The sportSelected_ is used to compare events pulled to ensure only events
      * that are important are displayed
      */
     static protected String sportSelected_ = "none";
 
     // Keeps track if the two news feeds
     protected boolean headline1IsPopulated = false;
 
     private int headline1StoryPositionInList = 0;
 
     @Override
     protected void onCreate( Bundle savedInstanceState )
     {
         super.onCreate( savedInstanceState );
         setContentView( R.layout.activity_main );
         asyncThread_ = new DataPullThread( this );
 
         // Set up all of the texts
         eventName1_ = (TextView) findViewById( R.id.EventName1 );
         eventName1_.setVisibility( View.VISIBLE );
 
         event1HomeCompetitor_ = (TextView) findViewById( R.id.competitor1 );
         event1AwayCompetitor_ = (TextView) findViewById( R.id.competitor2 );
         event1HomeCompetitorScore_ =
                 (TextView) findViewById( R.id.competitor1Score );
         event1AwayCompetitorScore_ =
                 (TextView) findViewById( R.id.competitor2Score );
         event1GameTime_ = (TextView) findViewById( R.id.gameTime );
         event1CurrentPossession_ =
                 (TextView) findViewById( R.id.currentPossession );
         eventBeingDisplayed_ =
                 (TextView) findViewById( R.id.gameDisplayed );
 
         // Set up the buttons
         previousEventButton_ =
                 (Button) findViewById( R.id.previousEventButton );
         previousEventButton_.setOnClickListener( new OnClickListener()
         {
             @Override
             public void onClick( View v )
             {
                 headline1StoryPositionInList--;
                 displayScores();
             }
         } );
 
         nextEventButton_ = (Button) findViewById( R.id.nextEventButton );
         nextEventButton_.setOnClickListener( new OnClickListener()
         {
             @Override
             public void onClick( View v )
             {
                 headline1StoryPositionInList++;
                 displayScores();
             }
         } );
 
         // Hide the text initially
         hideScoreText();
 
         initializeSpinner();
     }
 
     /**
      * This function hides all of the score text
      */
     public void hideScoreText()
     {
         event1HomeCompetitor_.setVisibility( View.GONE );
         event1AwayCompetitor_.setVisibility( View.GONE );
         event1HomeCompetitorScore_.setVisibility( View.GONE );
         event1AwayCompetitorScore_.setVisibility( View.GONE );
         event1GameTime_.setVisibility( View.GONE );
         event1CurrentPossession_.setVisibility( View.GONE );
 
         previousEventButton_.setVisibility( View.GONE );
         nextEventButton_.setVisibility( View.GONE );
         eventBeingDisplayed_.setVisibility( View.GONE );
     }
 
     /**
      * This function displays all of the score text
      */
     public void displayScoreText()
     {
         eventName1_.setVisibility( View.VISIBLE );
         event1HomeCompetitor_.setVisibility( View.VISIBLE );
         event1AwayCompetitor_.setVisibility( View.VISIBLE );
         event1HomeCompetitorScore_.setVisibility( View.VISIBLE );
         event1AwayCompetitorScore_.setVisibility( View.VISIBLE );
         event1GameTime_.setVisibility( View.VISIBLE );
         event1CurrentPossession_.setVisibility( View.VISIBLE );
     }
 
     /**
      * Displays the score at the currently selected position. Creates a delayed
      * runnable thread to start the Asynch threads to update the scores every 20
      * seconds.
      */
     protected void displayScores()
     {
         if ( null != ArrayOfEvents_ )
         {
             displayScoreText();
             updatePositionInList();
 
             SportsAbstract<String, JSONObject> currentEvent =
                     ArrayOfEvents_.get( headline1StoryPositionInList );
             eventName1_.setText( currentEvent.getLeague() );
 
             event1HomeCompetitor_.setText( currentEvent.getHomeContender() );
             event1AwayCompetitor_
                     .setText( currentEvent.getVisitingContender() );
             event1HomeCompetitorScore_.setText( currentEvent
                     .getHomeContenderScore() );
             event1AwayCompetitorScore_.setText( currentEvent
                     .getVisitingContenderScore() );
             event1GameTime_.setText( currentEvent.getGameTime() );
             event1CurrentPossession_.setText( currentEvent
                     .getCurrentPossession() );
 
             // A new score was selected, thus every 20 seconds refresh and get
             // the updated scores
            new Handler().postDelayed( new Runnable()
             {
                 @Override
                 public void run()
                 {
                     resetEventArray();
                     hideScoreText();
                     FetchNewScores();
                 }
             }, 20000 );
         }
     }
 
     /**
      * This function is called when no recent events for the selected sport are
      * present.
      */
     protected void displayNoRecentEvents()
     {
         try
         {
             if ( null == headlines_ )
             {
                 eventName1_.setText( "Please select a sport" );
                 hideScoreText();
             }
             else
             {
                 eventName1_.setText( "No recent events for "
                         + headlines_.sportSelected_ + " exist." );
                 hideScoreText();
             }
         }
         catch ( Exception ex )
         {
             System.err.println( "Error displaying no recent events" );
         }
     }
 
     @Override
     public boolean onCreateOptionsMenu( Menu menu )
     {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate( R.menu.main, menu );
         return true;
     }
 
     /**
      * Uses AsyncTask thread to fetch concurrent data from the website and store
      * information for the main thread to use
      * 
      * @param data
      * 
      */
     public void getNetworkData( String data )
     {
         // Call another activity to
         headlines_ = new Headlines( data );
     }
 
     /**
      * This method is called from the Asynchronous ParserThread class
      */
     public void initializeSpinner()
     {
         spinner1 = (Spinner) findViewById( R.id.spinner1 );
         spinner1.setOnItemSelectedListener( new SpinnerData() );
     }
 
     /**
      * This function is called to get the new scores, it starts the sequence of
      * threads need to perform the httpGet and parse/process the resulting JSON
      * data
      */
     private void FetchNewScores()
     {
         // While still on the main thread display that the scores are being
         // loading
         eventName1_.setText( "Loading Scores..." );
 
         // Initialize the asynchronous threads
         asyncThread_.execute( (Void) null );
         asyncThread_ =
                 new DataPullThread( this );
     }
 
     /**
      * Resets the event array and the index
      */
     protected void resetEventArray()
     {
         ArrayOfEvents_ = null;
         headline1StoryPositionInList = 0;
     }
 
     /**
      * Anytime headline1StoryPositionInList is updated it must update if the
      * previous and next buttons should be visible or hidden
      * 
      * @param newPosition
      */
     protected void updatePositionInList()
     {
         if ( null != ArrayOfEvents_ )
         {
             if ( ArrayOfEvents_.size() > 1 )
             {
                 int maxNumberOfGames = ArrayOfEvents_.size();
                 int numberToDisplay = headline1StoryPositionInList + 1;
                 String tempStringOfGameDisplayed =
                         "Game " + numberToDisplay + " of " + maxNumberOfGames;
                 eventBeingDisplayed_.setText( tempStringOfGameDisplayed );
                 eventBeingDisplayed_.setVisibility( View.VISIBLE );
 
                 if ( maxNumberOfGames - 1 == headline1StoryPositionInList )
                 {
                     previousEventButton_.setVisibility( View.VISIBLE );
                     nextEventButton_.setVisibility( View.GONE );
                 }
                 else if ( 0 == headline1StoryPositionInList )
                 {
                     previousEventButton_.setVisibility( View.GONE );
                     nextEventButton_.setVisibility( View.VISIBLE );
                 }
                 else
                 {
                     previousEventButton_.setVisibility( View.VISIBLE );
                     nextEventButton_.setVisibility( View.VISIBLE );
                 }
             }
             else
             {
                 nextEventButton_.setVisibility( View.GONE );
                 previousEventButton_.setVisibility( View.GONE );
                 eventBeingDisplayed_.setVisibility( View.GONE );
             }
         }
     }
 
     /**
      * This class handles the spinner sport combo box
      * 
      * @author Jimmy Dagres
      * 
      * @version Oct 2, 2013
      * 
      */
     public class SpinnerData implements OnItemSelectedListener
     {
         @Override
         public void onItemSelected( AdapterView<?> spinner, View selectedView,
                 int selectedIndex,
                 long id )
         {
             if ( !sportIsSelected )
             {
                 eventName1_.setText( "Please Select a Sport" );
                 sportIsSelected = true;
             }
             else if ( 0 == selectedIndex )
             {
                 eventName1_.setText( "Please Select a Sport" );
                 resetEventArray();
             }
             else
             {
                 String newSelection =
                         spinner.getItemAtPosition( selectedIndex ).toString()
                                 .toLowerCase().trim();
 
                 // If there was a change in sport then get all the events in
                 // that sport and display them
                 if ( sportSelected_ != newSelection )
                 {
                     // If there's an event already display hide it's information
                     if ( null != ArrayOfEvents_ )
                     {
                         hideScoreText();
                     }
 
                     resetEventArray();
 
                     sportSelected_ = newSelection;
 
                     // Call a function to start the threads and get the scores
                     FetchNewScores();
                 }
             }
         }
 
         @Override
         public void onNothingSelected( AdapterView<?> arg0 )
         {
             eventName1_.setVisibility( View.GONE );
         }
     }
 }
