 package com.avona.games.towerdefence;
 
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.ListIterator;
 
 public class TimedCodeManager extends TimeTrack {
 
 	private List<TimedCode> timedCode = new LinkedList<TimedCode>();
 
 	@Override
	public void updateTick(float wallTick) {
 		super.updateTick(wallTick);
 
 		// find code that timed out and execute it
 		Iterator<TimedCode> titer = timedCode.iterator();
 		while (titer.hasNext()) {
 			final TimedCode tc = titer.next();
 			if (tc.startTime < this.clock) {
 				tc.execute();
 				titer.remove();
 			} else {
 				break; // list must be sorted
 			}
 		}
 	}
 
	/*
	 * delay: delay in seconds from now code: code to execute then. startTime
	 * should not be set beforehand. when calling multiple times, use in
	 * chronological order
 	 */
	public void addCode(float delay, TimedCode newCode) {
 		newCode.startTime = this.clock + delay;
 
		final int s = timedCode.size();
		if (s == 0) {
			timedCode.add(newCode); // add as only element
		}

		ListIterator<TimedCode> titer = timedCode.listIterator(s); // traverse
		// backwards
 		while (titer.hasPrevious()) {
 			final TimedCode reference = titer.previous();
 			if (newCode.startTime > reference.startTime) {
 				titer.next(); // forward again, went too far
 				titer.add(newCode);
 				return;
 			}
 		}
 		titer.add(newCode); // add as first element
 	}
 
 	public String toString() {
 		String s = "" + this.clock + ": ";
 		ListIterator<TimedCode> titer = timedCode.listIterator(0);
 		while (titer.hasNext()) {
 			s += " " + String.format("%.1f", titer.next().startTime);
 		}
 		return s;
 	}
 
 }
