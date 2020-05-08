 /*
  * Copyright (C) 2009 Timothy Bourke
  * 
  * This program is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the Free
  * Software Foundation; either version 2 of the License, or (at your option)
  * any later version.
  * 
  * This program is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
  * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
  * for more details.
  * 
  * You should have received a copy of the GNU General Public License along
  * with this program; if not, write to the Free Software Foundation, Inc., 59
  * Temple Place, Suite 330, Boston, MA 02111-1307 USA
  */
 
 package mnemogogo.mobile.hexcsv;
 
 import java.lang.*;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.DataInputStream;
 import java.io.IOException;
 import java.util.Date;
 import java.util.Calendar;
 import java.util.TimeZone;
 import javax.microedition.io.Connector;
 import java.io.OutputStream;
 import javax.microedition.io.file.FileConnection; /*JSR-75*/
 
 public class HexCsv
     implements CardList, CardDataSet
 {
     private Card cards[];
     private RevQueue q;
     private int days_left;
     private Config config;
     private Progress progress;
 
     public long days_since_start;
     public OutputStreamWriter logfile;
     public String categories[];
 
     public int cards_to_load = 50;
 
     public static final String ascii = "US-ASCII";
     public static final String utf8 = "UTF-8";
 
     public static final String readingStatsText = "Loading statistics...";
     public static final String writingStatsText = "Writing statistics...";
     public static final String loadingCardsText = "Loading cards...";
 
     private StringBuffer pathbuf;
     private int path_len;
 
     public HexCsv(String path, Progress prog)
 	throws IOException
     {
 	String v;
 	long start_time;
 	long adjusted_now;
 	int num_cards;
 
 	path_len = path.length();
 	pathbuf = new StringBuffer(path_len + 20);
 	pathbuf.append(path);
 
 	progress = prog;
 
 	readConfig(pathbuf);
 
 	v = config.getString("last_day");
 	days_left = daysLeft(Long.parseLong(v));
 
 	v = config.getString("start_time");
 	days_since_start = daysSinceStart(Long.parseLong(v));
 
 	truncatePathBuf();
 	readCards(pathbuf);
 
 	truncatePathBuf();
 	readCategories(pathbuf);
 
 	if (config.logging()) {
 	    truncatePathBuf();
 	    pathbuf.append("PRELOG");
 
 	    try {
 		FileConnection file =
 		    (FileConnection)Connector.open(pathbuf.toString(),
 						   Connector.READ_WRITE);
 		if (!file.exists()) {
 		    file.create();
 		}
 
 		OutputStream outs = file.openOutputStream(file.fileSize() + 1);
 		logfile = new OutputStreamWriter(outs, ascii);
 	    } catch (Exception e) {
 		logfile = null;
 	    }
 	}
     }
 
     void truncatePathBuf()
     {
 	pathbuf.delete(path_len, pathbuf.length());
     }
 
     public String getCategory(int n) {
 	if (0 <= n && n < categories.length) {
 	    return categories[n];
 	} else {
 	    return null;
 	}
     }
 
     public Card getCard(int serial) {
	if (0 <= serial && serial < cards.length) {
 	    return cards[serial];
 	} else {
 	    return null;
 	}
     }
 
     private void readConfig(StringBuffer path)
 	throws IOException
     {
 	InputStreamReader in = new InputStreamReader(
 		Connector.openInputStream(path.append("CONFIG").toString()),
 		ascii);
 	config = new Config(in);
 	in.close();
     }
 
     private long daysSinceStart(long start_time)
     {
 	Date now = new Date();
 	long adjusted_now = (now.getTime() / 1000) -
 				(config.dayStartsAt() * 3600);
 	return (adjusted_now - start_time) / 86400;
     }
 
     public int daysLeft() {
 	return days_left;
     }
 
     private int daysLeft(long last_day)
     {
 	Date now = new Date();
 
 	// hours since epoch in UTC
 	long hours = now.getTime() / 3600000;
 
 	// offset from UTC to local in hours
 	Calendar cal = Calendar.getInstance();
 	TimeZone tz = cal.getTimeZone();
 	long tzoff = tz.getRawOffset() / 3600000;
 
 	// e.g.
 	// for day_starts_at = 3 (0300 local time)
 	// and UTC +8
 	// the next day should start at UTC 1900
 	// (not at UTC 0000)
 	// because 1900 + 8 - 3 = 0000
 
 	return (int)(last_day - ((hours + tzoff - config.dayStartsAt()) / 24));
     }
 
     private void readCards(StringBuffer path)
 	throws IOException
     {
 	InputStreamReader in = new InputStreamReader(
 	    Connector.openInputStream(path.append("STATS.CSV").toString()),
 	    ascii);
 
 	int ncards = StatIO.readInt(in);
 	progress.startOperation(ncards * 3, readingStatsText);
 
 	cards = new Card[ncards];
 	Card.cardlookup = this;
 
 	for (int i=0; i < ncards; ++i) {
 	    cards[i] = new Card(in, i);
 	    if (i % 10 == 0) {
 		progress.updateOperation(10);
 	    }
 	}
 	progress.stopOperation();
 
 	in.close();
 
 	q = new RevQueue(ncards, days_since_start, config, progress, days_left);
 	q.buildRevisionQueue(cards);
     }
 
     public void writeCards(StringBuffer path, Progress progress)
 	throws IOException
     {
 	OutputStreamWriter out = new OutputStreamWriter(
 	    Connector.openOutputStream(path.append("STATS.CSV").toString()),
 	    ascii);
 
 	StatIO.writeInt(out, cards.length, "\n");
 
 	progress.startOperation(cards.length, writingStatsText);
 	for (int i=0; i < cards.length; ++i) {
 	    cards[i].writeCard(out);
 
 	    if (i % 10 == 0 && progress != null) {
 		progress.updateOperation(10);
 	    }
 	}
 	progress.stopOperation();
 
 	out.close();
     }
 
     private void readCategories(StringBuffer path)
 	throws IOException
     {
 	InputStreamReader in = new InputStreamReader(
 	    Connector.openInputStream(path.append("CATS").toString()), utf8);
 
 	int n = StatIO.readInt(in);
 	int bytesize = StatIO.readInt(in);
 
 	categories = new String[n];
 	for (int i=0; i < n; ++i) {
 	    categories[i] = StatIO.readLine(in);
 	}
 
 	in.close();
     }
 
     public void setCardData(int serial, String question, String answer,
 		boolean overlay)
     {
 	cards[serial].setOverlay(overlay);
 	cards[serial].setQuestion(question);
 	cards[serial].setAnswer(answer);
     }
 
     public boolean cardDataNeeded(int serial)
     {
 	return ((cards[serial].isDueForRetentionRep(days_since_start)
 			    || cards[serial].isDueForAcquisitionRep())
 		&& q.isScheduledSoon(serial, cards_to_load));
     }
 
     private void readCardText(StringBuffer path)
 	throws IOException
     {
 	DataInputStream is = Connector.openDataInputStream(
 	    path.append("CARDS").toString());
 
 	CardData carddata = new CardData(is, progress, this);
 
 	is.close();
     }
 
     public void loadCardData()
 	throws IOException
     {
 	// clear any existing questions and answers
 	for (int i=0; i < cards.length; ++i) {
 	    cards[i].setQuestion(null);
 	    cards[i].setAnswer(null);
 	}
 
 	// load them again
 	truncatePathBuf();
 	progress.startOperation(cards.length, loadingCardsText);
 	readCardText(pathbuf);
 	progress.stopOperation();
     }
 
     public int numScheduled() {
 	return q.numScheduled();
     }
 
     public Card getCard() {
 	return q.getCard();
     }
 
     public void updateFutureSchedule(Card card) {
 	q.updateFutureSchedule(card);
     }
 
     public int[] getFutureSchedule() {
 	return q.futureSchedule;
     }
 
     public String toString() {
 	return q.toString();
     }
 
     public void dumpCards() {
 	System.out.println("----Cards:");
 	for (int i=0; i < cards.length; ++i) {
 	    System.out.print("  ");
 	    System.out.println(cards[i].toString());
 	}
     }
 
     public void close() {
 	if (logfile != null) {
 	    try {
 		logfile.close();
 	    } catch (IOException e) { }
 	    logfile = null;
 	}
     }
 }
 
