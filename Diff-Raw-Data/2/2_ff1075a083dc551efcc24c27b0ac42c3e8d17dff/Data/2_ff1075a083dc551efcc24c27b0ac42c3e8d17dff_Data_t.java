 package tr2.server.interval.data;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 
 import tr2.server.common.entity.Interval;
 import tr2.server.common.series.protocol.Messages;
 
 public class Data {
 	private static volatile ArrayList<Interval> intervals;
 
 	private static volatile ArrayList<Interval> runningIntervals;
 
 	private static volatile ArrayList<Interval> pendingIntervals;
 
 	private long intervalIndex;
 
 	private static final String label = "[INTERVAL DATA]";
 
 	public static final int TYPE_INTERVALS = 0;
 	public static final int TYPE_PENDING_INTERVALS = 1;
 
 	public Data() {
 		intervalIndex = 0;
 		intervals = new ArrayList<Interval>();
 		runningIntervals = new ArrayList<Interval>();
 		pendingIntervals = new ArrayList<Interval>();
 
 	}
 
 	private static ArrayList<Interval> sort(ArrayList<Interval> intervals) {		
 		Collections.sort(intervals, new Comparator<Interval>() {
 			@Override
 			public int compare(Interval i1, Interval i2) {
 				return i1.getIndex() <= i2.getIndex() ? -1 : 1;
 			}
 		});
 		return intervals;
 	}
 	
 	public static ArrayList<Interval> getCalculatedIntervals() {
 		return sort(intervals);
 	}
 
 	public static ArrayList<Interval> getRunningIntervals() {
 		return sort(runningIntervals);
 	}
 
 	public static ArrayList<Interval> getPendingIntervals() {
 		return sort(pendingIntervals);
 	}
 
	public static synchronized void removeIntervals(ArrayList<Interval> intervalsToBeRemoved) {
 		if (intervalsToBeRemoved == null)
 			return;
 
 		for (int i = 0; i < intervalsToBeRemoved.size(); i++) {
 			for (int j = 0; j < intervals.size(); j++) {
 				Interval intervalAux = intervals.get(j);
 				if (intervalsToBeRemoved.get(i).getIndex() == intervalAux
 						.getIndex()) {
 
 					pendingIntervals.add(intervalAux);
 					intervals.remove(j);
 
 					System.out
 							.println(label
 									+ " Deleted (moved to Pending) interval with index "
 									+ intervalAux.getIndex());
 				}
 			}
 		}
 	}
 
 	public static double intervalsSum() {
 		double sum = 0.0;
 
 		for (int i = 0; i < intervals.size(); i++) {
 			sum += intervals.get(i).getResult();
 		}
 
 		return sum;
 	}
 
 	public Interval getInterval(String address) {
 		Interval i = null;
 
 		if (pendingIntervals.size() == 0) {
 			// if there are no pending intervals
 			i = new Interval();
 
 			i.setIndex(intervalIndex);
 
 			intervalIndex++;
 		} else {
 			i = pendingIntervals.get(0); // gets the first pending interval
 			pendingIntervals.remove(0);
 		}
 
 		i.setClientIP(address);
 
 		runningIntervals.add(i);
 
 		return i;
 	}
 
 	// if the connection to the client is down,
 	// we need to see if he was calculating an interval and make it avaiable
 	public void removeClientDropInterval(String address) {
 		Interval interval = searchRunningIntervalByAddress(address);
 
 		if (interval == null) {
 			// there was no interval being calculated by the client
 		} else {
 			runningIntervals.remove(interval);
 			pendingIntervals.add(interval);
 			System.out.println(label + " Interval " + interval.getIndex()
 					+ " is pending");
 		}
 	}
 
 	public void addCalculatedInterval(Interval calculated) {
 		Interval interval = searchRunningIntervalByAddress(calculated
 				.getClientIP());
 
 		if (interval == null) {
 			// there is no running interval being calculated
 			// this is the case when the client drops and the interval
 			// is designated to be calculated by another client
 		} else {
 			runningIntervals.remove(interval);
 			intervals.add(calculated);
 			System.out.println(label + " Interval " + calculated.getIndex()
 					+ " received with result " + calculated.getResult());
 		}
 	}
 
 	private Interval searchRunningIntervalByAddress(String address) {
 		Interval interval = null;
 
 		for (int i = 0; i < runningIntervals.size(); i++) {
 			Interval intervalAux = runningIntervals.get(i);
 			if (intervalAux.getClientIP().equals(address)) {
 				interval = intervalAux;
 				break;
 			}
 		}
 
 		return interval;
 	}
 
 	public String intervalsToString(int type) {
 		ArrayList<Interval> intervals = null;
 
 		String str = "";
 		if (type == TYPE_INTERVALS) {
 			intervals = Data.intervals;
 			str += intervalIndex + Messages.SEPARATOR;
 		} else if (type == TYPE_PENDING_INTERVALS) {
 			intervals = Data.pendingIntervals;
 		}
 
 		for (int i = 0; i < intervals.size(); i++) {
 			// append all intervals
 			Interval interval = intervals.get(i);
 			str += interval.getIndex() + Messages.SUBSEPARATOR
 					+ interval.getResult() + Messages.SUBSEPARATOR
 					+ interval.getClientIP() + Messages.SEPARATOR;
 		}
 
 		return str;
 	}
 
 	public void stringToIntervals(String string, int type) {
 
 		ArrayList<Interval> newIntervals = new ArrayList<Interval>();
 
 		String[] strIntervals;
 
 		strIntervals = string.split(Messages.SEPARATOR);
 
 		if (type == Data.TYPE_INTERVALS) {
 			// if index is the only content
 			if (strIntervals.length == 1)
 				return;
 
 			long indexReceived = Long.parseLong(strIntervals[0]);
 
 			// if (indexReceived == 0) return;
 
 			this.intervalIndex = indexReceived;
 		}
 
 		for (int i = 1; i < strIntervals.length; i++) {
 			String[] attributes = strIntervals[i].split(Messages.SUBSEPARATOR);
 
 			Interval interval = new Interval();
 			interval.setIndex(Long.parseLong(attributes[0]));
 			interval.setResult(Double.parseDouble(attributes[1]));
 			interval.setClientIP(attributes[2]);
 
 			newIntervals.add(interval);
 		}
 
 		if (type == TYPE_INTERVALS) {
 			System.out.println(label + "Old intervals list size is "
 					+ intervals.size());
 			Data.intervals = newIntervals;
 			System.out.println(label + "New intervals list size is "
 					+ newIntervals.size());
 		} else if (type == TYPE_PENDING_INTERVALS) {
 			System.out.println(label + "Old pending intervals list size is "
 					+ pendingIntervals.size());
 			Data.pendingIntervals = newIntervals;
 			System.out.println(label + "New pending intervals list size is "
 					+ newIntervals.size());
 		}
 
 	}
 
 }
