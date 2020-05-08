 /*
  * Copyright (c) 2009 Timothy Bourke
  * All rights reserved.
  *
  * This program is free software; you can redistribute it and/or modify it
  * under the terms of the "BSD License" which is distributed with the
  * software in the file LICENSE.
  *
  * This program is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the BSD
  * License for more details.
  */
 
 package mnemojojo;
 
 import java.lang.*;
 import java.util.Date;
 import java.io.IOException;
 import mnemogogo.mobile.hexcsv.HexCsv;
 import mnemogogo.mobile.hexcsv.Progress;
 import mnemogogo.mobile.hexcsv.Card;
 
 import javax.microedition.midlet.MIDlet;
 import javax.microedition.midlet.MIDletStateChangeException;
 
 abstract class Core
     extends MIDlet
 {
     protected Configuration config = new Configuration();
     protected HexCsv carddb;
 
     protected Card curCard;
     private Date thinking_from;
     private long thinking_msecs = 0;
 
     Progress progressHandler;
 
     // Messages
     protected static final String versionInfo = "Mnemojojo 0.9.7";
 
     protected static final String reviewTitle = "Review Cards";
     protected static final String openTitle = "Open Cards";
     protected static final String aboutTitle = "About";
     protected static final String errTitle = "Error";
     protected static final String waitTitle = "Please wait";
     protected static final String doneTitle = "Finished";
     protected static final String nocardsTitle = "No cards";
 
     protected static final String exitText = "Exit";
     protected static final String okText = "Ok";
     protected static final String aboutText = "About";
     protected static final String openText = "Open";
     protected static final String gradeText = "Grade";
     protected static String[] gradesText;
     protected static final String skipText = "Skip";
     protected static final String showText = "Show";
     protected static final String showAnswerText = "Show";
     protected static final String showStatsText = "Stats";
     protected static final String skipCardText = "Skip";
     protected static final String closeText = "Close";
     protected static final String doneText
 	    = "There are no new cards to review.";
     protected static final String nocardloadedText
 	    = "Unexpected error: card not loaded.";
     protected static final String selectCarddir
 	    = "A card directory must be set before starting.";
     protected static final String notEnoughMemoryToLoadText
 	    = "Not enough memory to load cards.";
 
     protected static final String statisticsText = "Statistics";
     protected static final String currentCardText = "Current Card";
     protected static final String easinessText = "Easiness";
     protected static final String repetitionsText = "Repetitions";
     protected static final String lapsesText = "Lapses";
     protected static final String daysSinceLastText
 	    = "Days since last repetition";
     protected static final String daysUntilNextText
 	    = "Days until next repetition";
 
     protected static final String cardsdirText = "Cards: ";
     protected static final String updateOverdueText
 	    = "An export from Mnemosyne is overdue!";
    protected static final String updateTodayText
	    = "An export from Mnemosyne is due today.";
 
     protected static final String freeMemoryText = "Free bytes: ";
     protected static final String totalMemoryText = "Total bytes: ";
 
     protected static final String forDaysText
 	    = "Scheduled cards for the next days";
     protected static final String inText = "In";
     protected static final String daysText = "day(s)";
 
     protected String[] aboutLines = {
 	    versionInfo,
 	    "\n",
 	    "(c) Timothy Bourke\n\n",
 	    "SM-2 Implementation: Peter Bienstman\n",
 	    "SM-2 Algorithm: Piotr Wozniak\n"
 	};
 
     public Core()
     {
 	gradesText = new String[6];
 	for (int i=0; i < 6; ++i) {
 	    gradesText[i] = gradeText + " " + Integer.toString(i);
 	}
     }
 
     abstract public void startApp() throws MIDletStateChangeException;
 
     public void pauseApp()
     {
 	pauseThinking();
     }
 
     public void destroyApp(boolean unconditional)
     {
 	if (carddb != null) {
 	    carddb.close();
 	}
     }
 
     protected boolean doGrade(int grade)
     {
 	if (curCard == null) {
 	    return false;
 	}
 
 	try {
 	    curCard.gradeCard(carddb.days_since_start,
 		grade, thinking_msecs, carddb.logfile);
 	    carddb.updateFutureSchedule(curCard);
 	    return true;
 
 	} catch (IOException e) {
 	    showFatal(e.toString(), false);
 	    return false;
 	}
     }
 
     protected void loadCards()
     {
 	if (carddb != null) {
 	    carddb.close();
 	}
 
 	try {
 	    carddb = new HexCsv(config.cardpath, progressHandler);
 	    carddb.cards_to_load = config.cardsToLoad;
 	    setCardDir(config.cardpath);
 	} catch (Exception e) {
 	    showFatal(e.toString(), true);
 	} catch (OutOfMemoryError e) {
 	    carddb = null;
 	    showFatal(notEnoughMemoryToLoadText, true);
 	}
     }
 
     protected void saveCards()
     {
 	if (carddb != null) {
 	    try {
 		carddb.writeCards(new StringBuffer(config.cardpath),
 				  progressHandler);
 	    } catch (IOException e) {
 		showFatal(e.toString(), true);
 	    }
 
 	    return;
 	}
     }
 
     protected boolean nextQuestion()
     {
 	curCard = carddb.getCard();
 
 	try {
 	    setCard(curCard, carddb.numScheduled());
 	    if (curCard != null) {
 		startThinking();
 		return true;
 	    }
 	} catch (Exception e) {
 	    showFatal(e.toString(), false);
 	}
 
 	return false;
     }
 
     protected void startThinking() {
 	thinking_from = new Date();
 	thinking_msecs = 0;
     }
 
     protected void pauseThinking() {
 	Date now = new Date();
 
 	if (thinking_from != null) {
 	    thinking_msecs += now.getTime() - thinking_from.getTime();
 	    thinking_from = null;
 	}
     }
 
     protected void unpauseThinking() {
 	if (thinking_from == null) {
 	    thinking_from = new Date();
 	}
     }
 
     protected long stopThinking() {
 	pauseThinking();
 	return thinking_msecs;
     }
 
     abstract void showFatal(String msg, boolean exit);
     abstract void setCardDir(String cardpath) throws Exception;
     abstract void setCard(Card c, int numLeft) throws Exception;
     abstract void showDone();
 }
 
