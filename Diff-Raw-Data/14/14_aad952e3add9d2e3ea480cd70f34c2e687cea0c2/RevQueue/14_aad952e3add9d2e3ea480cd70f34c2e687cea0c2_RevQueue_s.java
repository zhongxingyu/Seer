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
 
 package mnemogogo.mobile.hexcsv;
 
 import java.lang.*;
 import java.util.Random;
 
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
 
     Progress progress;
 
     RevQueue(int size, long days, Config c, Progress p) {
 	config = c;
 
 	new_at_once = config.grade0ItemsAtOnce();
 	size += new_at_once;
 
 	q = new Card[size];
 	idx_new = size - 1;
 	limit_new = size;
 	days_since_start = days;
 
 	progress = p;
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
 
     // Adapted directly from Peter Bienstman's Mnemosyne 1.x
     public void buildRevisionQueue(Card[] cards)
     {
 	// form two queues:
 	//	    cards scheduled for today upward from 0
 	//	    wrong and unmemorised cards downward from revqueue.size
 	
 	num_scheduled = 0;
 	idx_new = q.length - 1;
 	
 	for (int i=0; i < cards.length; ++i) {
 	    if (cards[i].isDueForRetentionRep(days_since_start)) {
 		q[num_scheduled++] = cards[i];
 
 	    } else if (cards[i].isDueForAcquisitionRep()) {
 		q[idx_new--] = cards[i];
 	    }
 
 	    if (i % 10 == 0 && progress != null) {
 		progress.updateOperation(10);
 	    }
 	}
 
	if (config.sorting()) {
	    sortScheduled();
 	} else {
	    shuffle(0, num_scheduled);
 	}
     }
 
     public void rebuildNewQueue()
     {
 	cluster();
 
 	int bot = 0;
 	int top = idx_new + 1;
 	while ((bot < new_at_once) && (top < q.length) && (q[top].grade < 2)) {
 	    if (q[top].skip) {
 		++top;
 		continue;
 	    }
 
 	    q[bot++] = q[top];
 
 	    if ((q[top].grade == 0) && (bot < new_at_once)) {
 		q[bot++] = q[top];
 	    }
 
 	    ++top;
 	}
 
 	shuffle(0, bot);
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
 	return Math.max(0, num_scheduled - curr);
     }
 
     public Card getCard()
     {
 	++curr;
 
 	if (num_scheduled > 0) {
 	    if (curr < num_scheduled) {
 		return q[curr];
 
 	    } else {
 		// scheduled cards done
 		num_scheduled = 0;
 		limit_new = new_at_once;
 		curr = limit_new;
 		shiftForgottenToNew();
 	    }
 	}
 
 	if (curr == limit_new) {
 	    rebuildNewQueue();
 	    if (curr == limit_new) {
 		return null;
 	    }
 	}
 
 	return q[curr];
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
 
	r.append("new----------------------------\n");
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
 
