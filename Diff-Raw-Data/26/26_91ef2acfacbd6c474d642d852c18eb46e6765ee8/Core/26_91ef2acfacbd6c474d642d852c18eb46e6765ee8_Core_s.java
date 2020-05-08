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
     implements Progress
 {
     protected Configuration config = new Configuration();
     protected HexCsv carddb;
 
     protected Card curCard;
     private Date thinking_from;
     private long thinking_msecs = 0;
 
     // Messages
    protected String versionInfo = "MnemoJoJo 1.0.0";
 
     protected String reviewTitle = "Review Cards";
     protected String openTitle = "Open Cards";
     protected String aboutTitle = "About";
     protected String errTitle = "Error";
     protected String waitTitle = "Please wait";
     protected String doneTitle = "Finished";
     protected String nocardsTitle = "No cards";
 
     protected String exitText = "Exit";
     protected String okText = "Ok";
     protected String aboutText = "About";
     protected String openText = "Open";
     protected String gradeText = "Grade";
     protected String[] gradesText;
     protected String skipText = "Skip";
     protected String showText = "Show";
     protected String closeText = "Close";
     protected String lookingText = "Searching for cards...";
     protected String loadingText = "Loading cards...";
     protected String unpackingText = "Unpacking cards...";
     protected String savingText = "Saving cards...";
     protected String doneText = "Finished for today.";
     protected String nocardsText = "No cards found!\nPlease export from Mnemosyne.";
 
     protected String statisticsText = "Statistics";
     protected String currentCardText = "Current Card";
     protected String easinessText = "Easiness";
     protected String repetitionsText = "Repetitions";
     protected String lapsesText = "Lapses";
     protected String daysSinceLastText = "Days since last repetition";
     protected String daysUntilNextText = "Days until next repetition";
 
     protected String daysRemainingText = "Days until an export is due";
     protected String updateOverdueText = "An export from Mnemosyne is overdue!";
     protected String updateTodayText = "An export from Mnemosyne is due today.";
 
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
 	System.out.println("jojo: pauseApp!");
 	pauseThinking();
     }
 
     public void destroyApp(boolean unconditional)
     {
 	System.out.println("jojo: destroyApp!");
 	startWait(savingText, 1, 0);
 	saveCards();
 	notifyDestroyed();
     }
 
     protected boolean doGrade(int grade)
     {
 	if (curCard == null) {
 	    return false;
 	}
 
 	try {
 	    curCard.gradeCard(carddb.days_since_start,
 		grade, thinking_msecs, carddb.logfile);
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
 	    carddb = new HexCsv(config.cardPath, this);
 	    setCardDir(config.cardPath);
 	} catch (Exception e) {
 	    showFatal(e.toString(), true);
 	}
     }
 
     protected void saveCards()
     {
 	if (carddb != null) {
 	    try {
 		carddb.writeCards(new StringBuffer(config.cardPath), this);
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
 	    if (curCard != null) {
 		setCard(curCard, carddb.numScheduled());
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
     abstract public void startOperation(int length);
     abstract public void updateOperation(int delta);
     abstract void setCardDir(String cardPath) throws Exception;
     abstract void setCard(Card c, int numLeft) throws Exception;
     abstract void startWait(String msg, int length, int initial);
     abstract void showDone();
 }
 
