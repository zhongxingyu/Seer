 /*
  * Copyright (c) 2009 Timothy Bourke
  * All rights reserved.
  *
  * This program is free software; you can redistribute it and/or modify it
  * under the terms of the "BSD License" which is distributed with the
  * software in the file ../../LICENSE.
  *
  * This program is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the BSD
  * License for more details.
  */
 /*
  * Certain routines Copyright (c) Peter Bienstman <Peter.Bienstman@UGent.be>
  */
 
 package mnemogogo.mobile.htmlcsv;
 
 import java.lang.*;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.util.Random;
 
 public class Card
 {
     public int serial;
 
     public int grade; 
     public int easiness;
     public int acq_reps;
     public int ret_reps;
     public int lapses;
     public int acq_reps_since_lapse;
     public int ret_reps_since_lapse;
     public long last_rep;
     public long next_rep;
     public boolean unseen;
     public int inverse;
     public int category;
     public boolean skip;
     private CardList cardlookup;
 
     private static Random rand = new Random();
 
     private static int initial_interval[] = {0, 0, 1, 3, 4, 5};
 
     Card(CardList cardlist) {
 	cardlookup = cardlist;
     }
 
     Card(CardList cardlist, InputStreamReader in, int i)
 	throws IOException
     {
 	cardlookup = cardlist;
 	readCard(in, i);
     }
 
     public float feasiness() {
 	return (float)easiness / 1000.0f;
     }
 
     public String categoryName() {
 	return cardlookup.getCategory(category);
     }
 
     public boolean rememorise0() {
 	return (lapses > 0 && grade == 0);
     }
 
     public boolean rememorise1() {
 	return (lapses > 0 && grade == 1);
     }
 
     public boolean seenButNotMemorised0() {
 	return (lapses == 0 && unseen == false && grade == 0);
     }
 
     public boolean seenButNotMemorised1() {
 	return (lapses == 0 && unseen == false && grade == 1);
     }
 
     // Adapted directly from Peter Bienstman's Mnemosyne 1.x
     public boolean isDueForRetentionRep(long days_since_start, int days) {
 	return (grade >= 2 && (days_since_start >= next_rep - days));
     }
 
     public boolean isDueForRetentionRep(long days_since_start) {
 	return (grade >= 2 && days_since_start >= next_rep);
     }
 
     // Adapted directly from Peter Bienstman's Mnemosyne 1.x
     public boolean isDueForAcquisitionRep() {
 	return (grade < 2);
     }
 
     public long sortKeyInterval() {
 	return (next_rep - last_rep);
     }
 
     public String toString() {
 	StringBuffer r = new StringBuffer(100);
 
 	r.append("#");
 	r.append(serial);
 	r.append(" g=");
 	r.append(grade); 
 	r.append(" easy=");
 	r.append(easiness);
 	r.append(" acqs=");
 	r.append(acq_reps);
 	r.append(" rets=");
 	r.append(ret_reps);
 	r.append(" l=");
 	r.append(lapses);
 	r.append(" acqs_l=");
 	r.append(acq_reps_since_lapse);
 	r.append(" rets_l=");
 	r.append(ret_reps_since_lapse);
 	r.append(" last=");
 	r.append(last_rep);
 	r.append(" next=");
 	r.append(next_rep);
 	r.append(" unseen=");
 	r.append(unseen);
 	r.append(" inv=");
 	r.append(inverse);
 	r.append(" cat=");
 	r.append(category);
 	r.append(" skip=");
 	r.append(skip);
 
 	return r.toString();
     }
 
     public String toString(long days_since_start) {
 	StringBuffer r = new StringBuffer(40);
 	
 	r.append("gr=");
 	r.append(grade);
 	r.append(" e=");
 	r.append(feasiness());
 	r.append(" r=");
 	r.append(acq_reps + ret_reps);
 	r.append(" l=");
 	r.append(lapses);
 	r.append(" ds=");
 	r.append(days_since_start - last_rep);
 
 	return r.toString();
     }
 
     public void writeCard(OutputStreamWriter out)
 	throws IOException
     {
	StatIO.writeString(out, Integer.toString(grade)); 
	StatIO.writeString(out, ",");
 	StatIO.writeHexInt(out, easiness, ",");
 	StatIO.writeHexInt(out, acq_reps, ",");
 	StatIO.writeHexInt(out, ret_reps, ",");
 	StatIO.writeHexInt(out, lapses, ",");
 	StatIO.writeHexInt(out, acq_reps_since_lapse, ",");
 	StatIO.writeHexInt(out, ret_reps_since_lapse, ",");
 	StatIO.writeHexLong(out, last_rep, ",");
 	StatIO.writeHexLong(out, next_rep, ",");
 	StatIO.writeBool(out, unseen, ",");
 	StatIO.writeHexInt(out, inverse, ",");
 	StatIO.writeHexInt(out, category, "\n");
     }
 
     public void readCard(InputStreamReader in, int i)
 	throws IOException
     {
 	serial = i;
 	grade = StatIO.readHexInt(in); 
 	easiness = StatIO.readHexInt(in);
 	acq_reps = StatIO.readHexInt(in);
 	ret_reps = StatIO.readHexInt(in);
 	lapses = StatIO.readHexInt(in);
 	acq_reps_since_lapse = StatIO.readHexInt(in);
 	ret_reps_since_lapse = StatIO.readHexInt(in);
 	last_rep = StatIO.readHexLong(in);
 	next_rep = StatIO.readHexLong(in);
 	unseen = (StatIO.readHexInt(in) == 1);
 	category = StatIO.readHexInt(in);
 	inverse = StatIO.readHexInt(in);
 	skip = false;
     }
 
     // Adapted directly from Peter Bienstman's Mnemosyne 1.x
     private int calculateIntervalNoise(int interval) {
 	int a;
 
 	if (interval == 0) {
 	    return 0;
 
 	} else if (interval == 1) {
 	    return rand.nextInt(2);
 
 	} else if (interval <= 10) {
 	    return (rand.nextInt(3) - 1);
 
 	} else if (interval <= 60) {
 	    return rand.nextInt(7) - 3;
 
 	} else {
 	    a = interval / 20;
 	    return (rand.nextInt(2 * a + 1) - a);
 	}
     }
 
     // Adapted directly from Peter Bienstman's Mnemosyne 1.x (process_answer)
     public void gradeCard(long days_since_start, int new_grade,
 		   long thinking_time_msecs, OutputStreamWriter logfile)
 	throws IOException
     {
 
 	long scheduled_interval;
 	long actual_interval;
 	float new_interval = 0.0f;
 	int noise;
 
 	// Don't schedule inverse or identical questions on the same day.
 	Card inverse_card = cardlookup.getCard(inverse);
 	if (inverse_card != null) {
 	    inverse_card.skip = true;
 	}
 
 	// Calculate scheduled and actual interval, taking care of corner
 	// case when learning ahead on the same day.
 	
 	scheduled_interval = next_rep   - last_rep;
 	actual_interval    = days_since_start - last_rep;
 
 	if (actual_interval == 0) {
 	    actual_interval = 1; // Otherwise new interval can become zero.
 	}
 
 	if (acq_reps == 0 && ret_reps == 0) { // is_new()
 
 	    // The item is not graded yet, e.g. because it is imported.
 
 	    acq_reps = 1;
 	    acq_reps_since_lapse = 1;
 
 	    new_interval = initial_interval[new_grade];
 
 	} else if (grade < 2 && new_grade < 2) {
 	    // In the acquisition phase and staying there.
 	    acq_reps += 1;
 	    acq_reps_since_lapse += 1;
 	    new_interval = 0.0f;
 
 	} else if (grade < 2 && new_grade >= 2 && new_grade <= 5) {
 	     // In the acquisition phase and moving to the retention phase.
 	     acq_reps += 1;
 	     acq_reps_since_lapse += 1;
 	     new_interval = 1.0f;
 
 	} else if ((grade >= 2 && grade <= 5) && new_grade < 2) {
 	     // In the retention phase and dropping back to the acquisition phase.
 	     ret_reps += 1;
 	     lapses += 1;
 	     acq_reps_since_lapse = 0;
 	     ret_reps_since_lapse = 0;
 
 	     new_interval = 0.0f;
 
 	     // Move this item to the front of the list, to have precedence over
 	     // items which are still being learned for the first time.
 	     // THIS IS NOW DONE IN shiftforgottentonew()
 
 	} else if ((grade >= 2 && grade <= 5)
 		    && (new_grade >= 2 && new_grade <= 5)) {
 	    // In the retention phase and staying there.
 	    ret_reps += 1;
 	    ret_reps_since_lapse += 1;
 
 	    if (actual_interval >= scheduled_interval) {
 		if (new_grade == 2) {
 		    easiness -= 160;
 		} else if (new_grade == 3) {
 		    easiness -= 140;
 		} else if (new_grade == 5) {
 		    easiness += 100;
 		} else if (easiness < 1300) {
 		    easiness = 1300;
 		}
 	    }
 		
 	    new_interval = 0.0f;
 	    
 	    if (ret_reps_since_lapse == 1) {
 		new_interval = 6.0f;
 
 	    } else {
 		if (new_grade == 2 || new_grade == 3) {
 		    if (actual_interval <= scheduled_interval) {
 			new_interval = actual_interval * feasiness();
 		    } else {
 			new_interval = scheduled_interval;
 		    }
 
 		} else if (new_grade == 4) {
 		    new_interval = actual_interval * feasiness();
 
 		} else if (new_grade == 5) {
 		    if (actual_interval < scheduled_interval) {
 			new_interval = scheduled_interval; // Avoid spacing.
 		    } else {
 			new_interval = actual_interval * feasiness();
 		    }
 		}
 	    }
 
 	    // Shouldn't happen, but build in a safeguard.
 	    if (new_interval == 0)
 		new_interval = scheduled_interval;
 	}
 
 	// Add some randomness to interval.
 	noise = calculateIntervalNoise((int)new_interval);
 
 	// Update grade and interval.
 	grade    = new_grade;
 	last_rep = days_since_start;
 	next_rep = (int)(days_since_start + new_interval + noise);
 	unseen   = false;
 	
 	if (logfile != null) {
 	    StringBuffer r = new StringBuffer(100);
 
 	    // NOTE: the <%d> must be replaced with the id.
 	    r.append("R <");
 	    r.append(Integer.toString(serial));
 	    r.append("> ");
 	    r.append(Integer.toString(grade));
 	    r.append(" ");
 	    r.append(Float.toString(feasiness()));
 	    r.append(" | ");
 	    r.append(Integer.toString(acq_reps));
 	    r.append(" ");
 	    r.append(Integer.toString(ret_reps));
 	    r.append(" ");
 	    r.append(Integer.toString(lapses));
 	    r.append(" ");
 	    r.append(Integer.toString(acq_reps_since_lapse));
 	    r.append(" ");
 	    r.append(Integer.toString(ret_reps_since_lapse));
 	    r.append(" | ");
 	    r.append(Long.toString(scheduled_interval));
 	    r.append(" ");
 	    r.append(Long.toString(actual_interval));
 	    r.append(" | ");
 	    r.append(Float.toString(new_interval));
 	    r.append(" ");
 	    r.append(Integer.toString(noise));
 	    r.append(" | ");
 	    r.append(Float.toString(thinking_time_msecs / 100.0f));
 	    r.append("\n");
 
 	    logfile.write(r.toString(), 0, r.length());
 	}
     }
 }
 
