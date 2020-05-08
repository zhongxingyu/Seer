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
 
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.preference.PreferenceManager;
 import android.util.Log;
 
 /**
  * @author Siramix Labs
  * 
  *         The Game Manager is a class that will manage all aspects of the game
  *         scoring and general bookkeeping. This is the go-to class for creating
  *         new games, turns, and teams. The application shall also use this
  *         class for preparing and retrieving cards from the virtual deck.
  */
 public class GameManager {
   /**
    * logging tag
    */
   public static String TAG = "GameManager";
 
   /**
    * The list of cardIds that we pull from (our "deck" of cards)
    */
   private Deck mDeck;
 
   /**
    * The position in the list of card ids (where we are in the "deck")
    */
   private int mCardPosition;
 
   /**
    * List of team objects
    */
   private List<Team> mTeams;
   private Iterator<Team> mTeamIterator;
   private Team mCurrentTeam;
   private Team mBuzzedTeam;
 
   /**
    * The index of the round being played
    */
   private int mCurrentRound;
 
   /**
    * The card in play
    */
   private Card mCurrentCard;
   
   /**
    * Tracks the number of points needed to win
    */
   private int mScoreLimit;
 
   /**
    * Tracks the Assisted scoring mode
    */
   private boolean mIsAssistedScoringEnabled;
   
   /**
    * The set of cards that have been activated in the latest turn
    */
   private LinkedList<Card> mCurrentCards;
 
   /**
    * An array indicating scoring for right, wrong, and skip (in that order)
    */
   private int[] mRwsValueRules;
 
   /**
    * Time for the Timer in milliseconds
    */
   private int mTurnTime;
 
   /**
    * Standard Constructor
    * 
    * @param context
    *          required for game to instantiate the database
    */
   public GameManager(Context context) {
     if (PhraseCrazeApplication.DEBUG) {
       Log.d(TAG, "GameManager()");
     }
 
     SharedPreferences sp = PreferenceManager
         .getDefaultSharedPreferences(context);
 
     mCurrentRound = -1;
     mCardPosition = -1;
     mScoreLimit = -1;
     mCurrentCards = new LinkedList<Card>();
 
     mTurnTime = Integer.parseInt(sp.getString("turn_timer", "60")) * 1000;
 
     if (PhraseCrazeApplication.DEBUG) {
       Log.d(TAG, "Turn time is " + mTurnTime);
     }
     mRwsValueRules = new int[3];
 
     // Set score values for game
     mRwsValueRules[0] = 1; // Value for correct cards
     mRwsValueRules[1] = -1; // Value for wrong cards
     mRwsValueRules[2] = 0; // set skip value to 0 if skip penalty is not on
 
     mDeck = new Deck(context);
   }
 
   /**
    * Sets the next card indicated by the cardIdPosition. If we've dealt past the end
    * of the deck, we should prep the deck.
    * 
    * @return the card we want
    */
   public Card dealNextCard() {
     if (PhraseCrazeApplication.DEBUG) {
       Log.d(TAG, "getNextCard()");
     }
     ++mCardPosition;
     if (mCardPosition >= mCurrentCards.size()) {
      mCurrentCard = mDeck.getPhraseFromPhrasesInPlay();
       mCurrentCards.addLast(mCurrentCard);
     }
     else {
       mCurrentCard = mCurrentCards.get(mCardPosition);
     }
     return mCurrentCard;
   }
 
   /**
    * Return the previous card
    * 
    * @return the previous card in the deck
    */
   public Card getPreviousCard() {
     if (PhraseCrazeApplication.DEBUG) {
       Log.d(TAG, "getPreviousCard()");
     }
 
     --mCardPosition;
     if (mCardPosition < 0) {
       mCardPosition = 0;
     }
     mCurrentCard = mCurrentCards.get(mCardPosition);
     return mCurrentCard;
   }
 
   /**
    * Start the game given a set of team names. This creates both a game and a
    * set of teams in the database
    * 
    * @param teams
    *          a string array of team names
    * @param rounds
    *          the number of points to play to
    * @param assistedScoring
    *          true if the user specified assisted scoring as the game mode
    */
   public void startGame(List<Team> teams, int score, boolean assistedScoring) {
     if (PhraseCrazeApplication.DEBUG) {
       Log.d(TAG, "StartGame()");
     }
     mTeams = teams;
     Iterator<Team> itr = teams.iterator();
 	Team teamAtItr;
     for (itr = teams.iterator(); itr.hasNext();) {
       teamAtItr = itr.next();
       teamAtItr.setScore(0);
       teamAtItr.setRoundScore(0);
     }
     mTeamIterator = teams.iterator();
     mCurrentTeam = mTeamIterator.next();
     mCurrentRound = 0;
     mScoreLimit = score;
     //TODO put this in the right place (after pack select) -- 
     // actually this will be problematic because we won't "know" which packs they want to 
     // stick with until they hit start, unless we ask for packs before game settings
     mDeck.topOffDeck();
     mIsAssistedScoringEnabled = assistedScoring;
     dealNextCard();
   }
 
   /**
    * Starts a new round. A round is defined as a sequence of turns for a random time limit.
    */
   public void nextRound() {
     if (PhraseCrazeApplication.DEBUG) {
       Log.d(TAG, "NextRound()");
     }
     // The same team who was buzzed will start the next round
     mCurrentCards.clear();
     mCardPosition = -1;
     //TODO figure out when to call prepare for game.  This happens at the wrong spot right now
     // It should occur at the start of Turn Summery (but now this is called right before a new round)
     mDeck.printCache();
     mDeck.topOffDeck();
     mDeck.printCache();
     dealNextCard();
     
     // Clear round scores
     Iterator<Team> itr = mTeams.iterator();
     for (itr = mTeams.iterator(); itr.hasNext();) {
       itr.next().setRoundScore(0);
     }
     mCurrentRound++;
   }
 
   /**
    * Sets the team that got buzzed in the current round.
    * @param team
    */
   public void setBuzzedTeam(Team team)
   {
 	  mBuzzedTeam = team;
   }
   
   /**
    * Starts a new turn incrementing the round and/or team index as necessary.
    */
   public void nextTurn() {
     if (PhraseCrazeApplication.DEBUG) {
       Log.d(TAG, "NextTurn()");
     }
     this.incrementActiveTeamIndex();
   }
   
   /**
    * Returns to the previous turn
    */
   public void previousTurn() {
 	  if (PhraseCrazeApplication.DEBUG) {
 		  Log.d(TAG, "PreviousTurn()");
 	  }
 	  this.decrementActiveTeamIndex();
   }
   
   /*
    * Add the results of the current round into the all team scores.
    */
   private void setNewTotalScores()
   {
 	  Iterator<Team> itr = mTeams.iterator();
 	  Team teamAtItr;
 	  for (itr = mTeams.iterator(); itr.hasNext();) {
 		  teamAtItr = itr.next();
 		  int score = teamAtItr.getScore() + teamAtItr.getRoundScore();
 		  teamAtItr.setScore(score);
 	  }
   }
   
   /*
    * When no scores are supplied, simply give a point to all the teams that were not buzzed.
    */
   public void setAutoAssignedRoundScores()
   {
 	  Iterator<Team> itr = mTeams.iterator();
 	  Team teamAtItr;
 	  int i = 0;
 	  int roundScore = 0;
 	  int[] scores = new int[mTeams.size()];
 	  for (itr = mTeams.iterator(); itr.hasNext();) {
 		  teamAtItr = itr.next();
 		  if (!teamAtItr.equals(mBuzzedTeam))
 			  roundScore = 1;
 		  else
 			  roundScore = 0;
 		  
 		  scores[i] = roundScore;
 		  ++i;
 	  }
 	  
 	  this.setNewRoundScores(scores);
   }
   
   /*
    * Set the round scores to the supplied array of scores
    */
   public void setNewRoundScores(int[] newScores)
   {
 	  // Since team displays only show a team's total score,
 	  // we must subtract old round scores before adding new ones.
 	  Iterator<Team> itr = mTeams.iterator();
 	  Team teamAtItr;
 	  int i = 0;
 	  for (itr = mTeams.iterator(); itr.hasNext();) {
 		  teamAtItr = itr.next();
 		  teamAtItr.setScore(teamAtItr.getScore()-teamAtItr.getRoundScore());
 		  // Set new round scores
 		  teamAtItr.setRoundScore(newScores[i]);
 		  ++i;
 	  }
 
 	  this.setNewTotalScores();
   }
 
   private void incrementActiveTeamIndex() {
     if (mTeamIterator.hasNext()) {
       mCurrentTeam = mTeamIterator.next();
     } else {
       mTeamIterator = mTeams.iterator();
       mCurrentTeam = mTeamIterator.next();
     }
   }
   
   // Set the current team to the previous team in the team list
   private void decrementActiveTeamIndex() {  
 	  int countToPrevious = mTeams.size();
 	  while( --countToPrevious > 0)
 	  {
 		  this.incrementActiveTeamIndex();
 	  }
   }
 
   /**
    * Call to clean up game data at the end of a game.
    */
   public void endGame() {
     if (PhraseCrazeApplication.DEBUG) {
       Log.d(TAG, "EndGame()");
     }
     mTeamIterator = mTeams.iterator();
     //TODO Another questionable location for topoffdeck
     mDeck.topOffDeck();
     // clear current cards so that scoreboards don't add turn score in
     mCurrentCards.clear();
   }
 
   /**
    * Adds the current card to the active cards, attributing it to the current team
    * 
    * @param rws
    *          the right, wrong, skip status
    */
   public void processCard(int rws) {
     if (PhraseCrazeApplication.DEBUG) {
       Log.d(TAG, "ProcessCard(" + rws + ")");
     }
     mCurrentCard.setRws(rws, mCurrentTeam);
     if(rws == Card.RIGHT)
     {
     	this.nextTurn();
     }
     // If Wrong, it's the last card. Don't deal any more.
     if(rws != Card.WRONG)
     	dealNextCard();
   }
   
   /**
    * Processes the current card as a back (currently handled as a skip)
    * and sets the team to the previous team
    */  
   public void processBack()
   {
 	  if (PhraseCrazeApplication.DEBUG) {
 		  Log.d(TAG, "ProcessBack()");
 	  }
 	  mCurrentCard.setRws(Card.SKIP, mCurrentTeam);
 	  // If the previous card was correct, we must return to that team
 	  if(getPreviousCard().getRws() == Card.RIGHT)
 		  this.previousTurn();
 	
   }
 
   /**
    * Return the card currently in play without moving through the deck
    * 
    * @return the card currently in play
    */
   public Card getCurrentCard() {
     if (PhraseCrazeApplication.DEBUG) {
       Log.d(TAG, "GetCurrentCard()");
     }
     return mCurrentCard;
   }
 
   /**
    * Get a list of all cards that have been acted on in a given turn.
    * 
    * @return list of all cards from the current turn
    */
   public LinkedList<Card> getCurrentCards() {
     if (PhraseCrazeApplication.DEBUG) {
       Log.d(TAG, "GetCurrentCards()");
     }
     return mCurrentCards;
   }
 
   /**
    * Return a list of the currently playing team objects
    * 
    * @return a list of the currently playing team objects
    */
   public List<Team> getTeams() {
     if (PhraseCrazeApplication.DEBUG) {
       Log.d(TAG, "GetTeams()");
     }
     return mTeams;
   }
 
   /**
    * Return a reference to the team currently playing.
    * 
    * @return a reference to the team currently playing
    */
   public Team getActiveTeam() {
     if (PhraseCrazeApplication.DEBUG) {
       Log.d(TAG, "GetActiveTeamIndex()");
     }
     return mCurrentTeam;
   }
   
   /**
    * Returns the team that was buzzed during this round.
    * @return
    */
   public Team getBuzzedTeam() {
 	  return mBuzzedTeam;
   }
 
   /**
    * Return the number of teams set up by the game manager.
    * 
    * @return integer representing the number of teams ie. the length of
    *         teamIds[]
    */
   public int getNumTeams() {
     if (PhraseCrazeApplication.DEBUG) {
       Log.d(TAG, "GetNumTeams()");
     }
     return mTeams.size();
   }
 
   /**
    * Return the current round number. Add one because we, like good computer scientists, start counting
    * at 0.
    * 
    * @return int representing the number of rounds thus far in a game, counting the current round
    */
   public int getCurrentRound() {
     if (PhraseCrazeApplication.DEBUG) {
       Log.d(TAG, "GetCurrentRound()");
     }
     return mCurrentRound + 1;
   }
 
   /**
    * Accessor to return the amount of time in each turn.
    * 
    * @return integer representing the number of miliseconds in each turn.
    */
   public int getTurnTime() {
     if (PhraseCrazeApplication.DEBUG) {
       Log.d(TAG, "GetTurnTime()");
     }
     return mTurnTime;
   }
   
   /**
    * Returns the number of points a team needs to win.
    * @return the number of points a team needs to win.
    */
   public int getScoreLimit()
   {
 	  return mScoreLimit;
   }
   
   /**
    * Returns true if the game is using assisted scoring
    */
   public boolean isAssistedScoringEnabled()
   {
     return mIsAssistedScoringEnabled;
   }
 
   /**
    * Returns true if the game end condition is true.
    * 
    * @return true if a team has reached the score limit
    */
   public boolean isGameOver()
   {
 	  // For now the only condition is score limit. Check if any teams
 	  // have reached the score limit.
 	  Iterator<Team> itr = mTeams.iterator();
 	  for (itr = mTeams.iterator(); itr.hasNext();) {
 		  if ( itr.next().getScore() >= mScoreLimit ) {
 			  return true;
 		  }
 	  }
 	  return false;
   }
 
   /**
    * @return The current deck
    */
   public Deck getDeck() {
     return mDeck;
   }
 }
