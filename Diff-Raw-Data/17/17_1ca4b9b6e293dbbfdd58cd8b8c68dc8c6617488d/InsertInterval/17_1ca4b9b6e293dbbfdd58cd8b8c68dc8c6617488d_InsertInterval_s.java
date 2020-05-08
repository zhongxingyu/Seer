 /**
  * Definition for an interval.
  * public class Interval {
  *     int start;
  *     int end;
  *     Interval() { start = 0; end = 0; }
  *     Interval(int s, int e) { start = s; end = e; }
  * }
  */
 public class InsertInterval {
     public ArrayList<Interval> insert(ArrayList<Interval> intervals, Interval newInterval) {
         int i = 0;
         while (i < intervals.size() && newInterval.start >= intervals.get(i).start){
             i++;
         }
         intervals.add(i, newInterval);
         if (i > 0 && intervals.get(i-1).end >= newInterval.start){
             intervals.remove(i);
             if (newInterval.end > intervals.get(i-1).end)
                 intervals.get(i-1).end = newInterval.end;
             i--;
         }
         while (i < intervals.size() - 1 && intervals.get(i).end >= intervals.get(i+1).start){
             if (intervals.get(i+1).end > intervals.get(i).end){
                 intervals.get(i).end = intervals.get(i+1).end;
             }
             intervals.remove(i+1);
         }
         return intervals;
     }
 }
