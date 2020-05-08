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
 /*
  * Certain routines Copyright (c) Peter Bienstman <Peter.Bienstman@UGent.be>
  */
 
 package mnemogogo.mobile.hexcsv;
 
 import java.lang.*;
 import java.util.Random;
 import java.lang.Math;
 
 class RevQueue {
     private Card q[];
 
     int num_scheduled = 0;
     int limit_new = 0;
     int idx_new;
     int new_at_once;
 
     int curr = -1;
     Random rand = new Random();
 
     long days_since_start = 0;
     Config config;
 
     public int[] futureSchedule; // 0 = in 1 day, ...
 
     Progress progress;
 
     RevQueue(int size, long days, Config c, Progress p, int days_left) {
 	config = c;
 
 	new_at_once = Math.max(0, config.grade0ItemsAtOnce());
 	size += new_at_once;
 
 	q = new Card[size];
 	idx_new = size - 1;
 	limit_new = 0;
 	days_since_start = days;
 
	if (days_left > 1) {
 	    futureSchedule = new int[days_left]; // 0 = in 1 day, ...
 	} else {
 	    futureSchedule = null;
 	}
 
 	progress = p;
     }
 
     public boolean isScheduledSoon(int serial, int within)
     {
 	int start = Math.max(curr, 0);
 	int limit;
 
 	// not exact when it comes to new cards
 	// or the threshold between scheduled and new cards
 	if (num_scheduled > 0) {
 	    limit = Math.min(num_scheduled, within + start);
 	} else {
 	    // TODO: make this more accurate.
 	    // limit = Math.min(limit_new, within);
 	    return true;
 	}
 
 	for (int i=start; i < limit; ++i) {
 	    if (q[i].serial == serial) {
 		return true;
 	    }
 	}
 
 	return false;
     }
 
     private void swap(int i, int j)
     {
 	if (i < q.length && j < q.length) {
 	    Card tmp = q[i];
 	    q[i] = q[j];
 	    q[j] = tmp;
 	}
     }
 
     // insertion sort: linear when already ordered, won't blow the stack
     // if too slow: implement shell sort
     private void sortScheduled()
     {
 	int i, j;
 	Card c;
 	long key;
 
 	for (i=1; i < num_scheduled; ++i) {
 	    c = q[i];
 	    key = c.sortKeyInterval();
 
 	    for (j=i-1; j >= 0 && q[j].sortKeyInterval() > key; --j) {
 		q[j + 1] = q[j];
 	    }
 	    q[j + 1] = c;
 
 	    if (i % 10 == 0 && progress != null) {
 		progress.updateOperation(10);
 	    }
 	}
     }
 
     private void shuffle(int first, int max)
     {
 	for (int i=first; i < max; ++i) {
 	    swap(i, rand.nextInt(max - first) + first);
 	}
     }
 
     private void shiftDuplicates(int first, int max)
     {
 	for (int i=first + 1; i < max; ++i) {
 	    if (q[i] == q[i - 1]) {
 		if (i + 1 == max) {
 		    swap(i, first);
 		} else {
 		    swap(i, i + 1);
 		}
 	    }
 	}
     }
 
     private int clusterRememorise0(int hd, int max)
     {
 	for (int i=hd; i < max; ++i) {
 	    if (q[i].rememorise0()) {
 		swap(i, hd++);
 	    }
 	}
 	return hd;
     }
 
     private int clusterRememorise1(int hd, int max)
     {
 	for (int i=hd; i < max; ++i) {
 	    if (q[i].rememorise1()) {
 		swap(i, hd++);
 	    }
 	}
 	return hd;
     }
 
     private int clusterSeenButNotMemorised0(int hd, int max)
     {
 	for (int i=hd; i < max; ++i) {
 	    if (q[i].seenButNotMemorised0()) {
 		swap(i, hd++);
 	    }
 	}
 	return hd;
     }
 
     private int clusterSeenButNotMemorised1(int hd, int max)
     {
 	for (int i=hd; i < max; ++i) {
 	    if (q[i].seenButNotMemorised1()) {
 		swap(i, hd++);
 	    }
 	}
 	return hd;
     }
 
     private int clusterUnseen(int hd, int max)
     {
 	for (int i=hd; i < max; ++i) {
 	    if (q[i].unseen) {
 		swap(i, hd++);
 	    }
 	}
 	return hd;
     }
 
     private void cluster()
     {
 	int hd = idx_new + 1;
 
 	hd = clusterRememorise0(hd, q.length);
 	hd = clusterRememorise1(hd, q.length);
 	hd = clusterSeenButNotMemorised0(hd, q.length);
 	hd = clusterSeenButNotMemorised1(hd, q.length);
 	hd = clusterUnseen(hd, q.length);
     }
 
     public void updateFutureSchedule(Card card)
     {
 	int next_rep = card.daysUntilNextRep(days_since_start) - 1;
 	if ((futureSchedule != null)
 	    && (0 <= next_rep) && (next_rep < futureSchedule.length))
 	{
 	    ++futureSchedule[next_rep];
 	}
     }
 
     // Adapted directly from Peter Bienstman's Mnemosyne 1.x
     public void buildRevisionQueue(Card[] cards)
     {
 	// form two queues:
 	//	    cards scheduled for today upward from 0
 	//	    wrong and unmemorised cards downward from revqueue.size
 	
 	num_scheduled = 0;
 	idx_new = q.length - 1;
 	
 	for (int i=0; i < cards.length; ++i) {
 	    updateFutureSchedule(cards[i]);
 
 	    if (cards[i].isDueForRetentionRep(days_since_start)) {
 		q[num_scheduled++] = cards[i];
 
 	    } else if (cards[i].isDueForAcquisitionRep()) {
 		q[idx_new--] = cards[i];
 	    }
 
 	    if (i % 10 == 0 && progress != null) {
 		progress.updateOperation(10);
 	    }
 	}
 
 	if (num_scheduled > 0) {
 	    if (config.sorting()) {
 		sortScheduled();
 	    } else {
 		shuffle(0, num_scheduled);
 	    }
 	} else {
 	    rebuildNewQueue();
 	}
     }
 
     public void rebuildNewQueue()
     {
 	if (new_at_once == 0) {
 	    limit_new = 0;
 	    curr = 0;
 	    return;
 	}
 
 	cluster();
 
 	int bot = 0;
 	int top = idx_new + 1;
 	while ((bot < new_at_once) && (top < q.length) && (q[top].grade < 2)) {
 	    if (q[top].isSkip()) {
 		++top;
 		continue;
 	    }
 
 	    q[bot] = q[top];
 	    q[bot++].skipInverse();
 
 	    if ((new_at_once > 5) && (q[top].grade == 0) && (bot < new_at_once))
 	    {
 		q[bot++] = q[top];
 	    }
 
 	    ++top;
 	}
 
 	shuffle(0, bot);
 	shiftDuplicates(0, bot);
 	limit_new = bot;
 	curr = 0;
     }
 
     private void shiftForgottenToNew()
     {
 	for (int i=num_scheduled - 1; i >= 0; --i) {
 	    if (q[i].grade < 2) {
 		swap(i, idx_new--);
 		--num_scheduled;
 	    }
 	}
     }
 
     public int numScheduled()
     {
 	if (num_scheduled > 0) {
 	    return (num_scheduled - curr);
 	}
 
 	return 0;
     }
 
     public Card getCard()
     {
 	++curr;
 
 	if (num_scheduled > 0) {
 	    if (curr < num_scheduled) {
 		return q[curr];
 
 	    } else {
 		// scheduled cards done
 		shiftForgottenToNew();
 		num_scheduled = 0;
 		rebuildNewQueue();
 	    }
 	}
 
 	while (limit_new > 0) {
 
 	    if (curr == limit_new) {
 		rebuildNewQueue();
 		if (curr == limit_new) {
 		    return null;
 		}
 	    }
 
 	    // skip duplicates where the first instance was graded >= 2
 	    // and also inverse cards
 	    if (q[curr].grade < 2 && (!q[curr].isSkip())) {
 		return q[curr];
 	    }
 
 	    ++curr;
 	};
 
 	return null;
     }
 
     public String toString() {
 	StringBuffer r = new StringBuffer();
 
 	if (num_scheduled > 0) {
 	    r.append("scheduled----------------------\n");
 	    for (int i=0; i < num_scheduled; ++i) {
 		r.append(i);
 		r.append(" serial=");
 		r.append(q[i].serial);
 		r.append(" key=");
 		r.append(q[i].sortKeyInterval());
 		if (i == curr) {
 		    r.append(" <-");
 		}
 		r.append("\n");
 	    }
 
 	} else {
 	    r.append("new cards----------------------\n");
 	    for (int i=0; i < limit_new; ++i) {
 		r.append(i);
 		r.append(" serial=");
 		r.append(q[i].serial);
 		r.append(" re0=");
 		r.append(q[i].rememorise0());
 		r.append(" re1=");
 		r.append(q[i].rememorise1());
 		r.append(" sn0=");
 		r.append(q[i].seenButNotMemorised0());
 		r.append(" sn1=");
 		r.append(q[i].seenButNotMemorised1());
 		r.append(" un=");
 		r.append(q[i].unseen);
 		if (i == curr) {
 		    r.append(" <-");
 		}
 		r.append("\n");
 	    }
 	}
 
 	r.append("new waiting--------------------\n");
 	for (int i=idx_new + 1; i < q.length; ++i) {
 	    if (i == limit_new) {
 		r.append("--new limit--\n");
 	    }
 
 	    r.append(i);
 	    r.append(" serial=");
 	    r.append(q[i].serial);
 	    r.append(" re0=");
 	    r.append(q[i].rememorise0());
 	    r.append(" re1=");
 	    r.append(q[i].rememorise1());
 	    r.append(" sn0=");
 	    r.append(q[i].seenButNotMemorised0());
 	    r.append(" sn1=");
 	    r.append(q[i].seenButNotMemorised1());
 	    r.append(" un=");
 	    r.append(q[i].unseen);
 	    r.append("\n");
 	}
 
 	r.append("-------------------------------\n");
 	r.append("nstats=");
 	r.append(q.length);
 	r.append(", ");
 	r.append("days_since_start=");
 	r.append(days_since_start);
 	r.append(", ");
 	r.append("num_scheduled=");
 	r.append(num_scheduled);
 	r.append("\n");
 	r.append("idx_new=");
 	r.append(idx_new);
 	r.append(", ");
 	r.append("limit_new=");
 	r.append(limit_new);
 	r.append(", ");
 	r.append("curr=");
 	r.append(curr);
 	r.append("\n");
 
 	return r.toString();
     }
 
 }
 
