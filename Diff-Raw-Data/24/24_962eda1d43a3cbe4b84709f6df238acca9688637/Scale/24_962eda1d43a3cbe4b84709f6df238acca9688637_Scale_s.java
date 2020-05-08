 package chordest.model;
 
 public abstract class Scale {
 
 	protected final int[] intervals;
 
 	protected Scale(int[] intervals) {
 		this.intervals = intervals;
 	}
 
 	public int[] getIntervals() {
 		int[] result = new int[intervals.length];
 		System.arraycopy(intervals, 0, result, 0, intervals.length);
 		return result;
 	}
 	
 	public abstract Chord[] getChords(Note root);
 	
 	public static class NaturalMajor extends Scale {
 		public NaturalMajor() {
 			super(new int[] {
 					Interval.MAJOR_SECOND,
 					Interval.MAJOR_THIRD,
 					Interval.PERFECT_FOURTH,
 					Interval.PERFECT_FIFTH,
 					Interval.MAJOR_SIXTH,
 					Interval.MAJOR_SEVENTH
 			});
 		}
 		
 		@Override
 		public Chord[] getChords(Note root) {
 			return new Chord[] {
 					Chord.major(root),
 					Chord.minor(root.withOffset(intervals[0])),
 					Chord.minor(root.withOffset(intervals[1])),
 					Chord.major(root.withOffset(intervals[2])),
 					Chord.major(root.withOffset(intervals[3])),
 					Chord.minor(root.withOffset(intervals[4]))
 			};
 		}
 		
 		@Override
 		public String toString() {
 			return "major";
 		}
 	}
 	
 	public static class NaturalMinor extends Scale {
 		public NaturalMinor() {
 			super(new int[] {
 					Interval.MAJOR_SECOND,
 					Interval.MINOR_THIRD,
 					Interval.PERFECT_FOURTH,
 					Interval.PERFECT_FIFTH,
 					Interval.MINOR_SIXTH,
 					Interval.MINOR_SEVENTH
 			});
 		}
 		
 		@Override
 		public Chord[] getChords(Note root) {
 			return new Chord[] {
 					Chord.minor(root),
 					Chord.major(root.withOffset(intervals[1])),
 					Chord.minor(root.withOffset(intervals[2])),
 					Chord.minor(root.withOffset(intervals[3])),
 					Chord.major(root.withOffset(intervals[4])),
 					Chord.major(root.withOffset(intervals[5]))
 			};
 		}
 		
 		@Override
 		public String toString() {
 			return "minor";
 		}
 	}
 }
