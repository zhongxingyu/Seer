 package ca.cutterslade.blocktimer;
 
 import java.util.Date;
 
 import com.google.common.base.Preconditions;
 import com.google.common.collect.ComparisonChain;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Ordering;
 
 public final class TimerEvent implements Comparable<TimerEvent> {
 
   private final Timer timer;
 
   private final long endNanos;
 
   private final long timeNanos;
 
   private final int computedHashCode;
 
   private final String stringForm;
 
   TimerEvent(final Timer timer, final long endNanos) {
     Preconditions.checkArgument(null != timer);
     this.timer = timer;
     this.endNanos = endNanos;
     this.timeNanos = endNanos - timer.getStartNanos();
     this.computedHashCode = computeHashCode();
     this.stringForm = makeString();
   }
 
   public Class<?> getHostClass() {
     return timer.getHost();
   }
 
   public String getMethod() {
     return timer.getMethod();
   }
 
   public Object getOperation() {
     return timer.getOperation();
   }
 
   public ImmutableList<StackTraceElement> getStackTrace() {
     return timer.getStackTrace();
   }
 
   public long getStartDate() {
     return timer.getStartDate();
   }
 
   public long getStartNanos() {
     return timer.getStartNanos();
   }
 
   public long getEndNanos() {
     return endNanos;
   }
 
   public long getTimeNanos() {
     return timeNanos;
   }
 
   @Override
   public int compareTo(final TimerEvent o) {
    return null == o ? 1 : this == o ? 0 : ComparisonChain.start()
         .compare(getStartDate(), o.getStartDate())
         .compare(o.getStartNanos() - getStartNanos(), 0L)
         .compare(o.getEndNanos() - getEndNanos(), 0L)
         .compare(getHostClass(), o.getHostClass(), Ordering.usingToString())
         .compare(getMethod(), o.getMethod())
         .compare(this, o, Ordering.arbitrary())
         .result();
   }
 
   private int computeHashCode() {
     final int prime = 31;
     int result = 1;
     result = prime * result + (int) (endNanos ^ (endNanos >>> 32));
     result = prime * result + timer.hashCode();
     return result;
   }
 
   @Override
   public int hashCode() {
     return computedHashCode;
   }
 
   @Override
   public boolean equals(final Object obj) {
     if (this == obj) {
       return true;
     }
     if (obj == null) {
       return false;
     }
     if (getClass() != obj.getClass()) {
       return false;
     }
     final TimerEvent other = (TimerEvent) obj;
     if (endNanos != other.endNanos) {
       return false;
     }
     if (!timer.equals(other.timer)) {
       return false;
     }
     return true;
   }
 
   private String makeString() {
     return "Method " + getHostClass().getName() + '.' + getMethod() + '(' + getOperation() + ") started at " +
         new Date(getStartDate()) + " and took " + getTimeNanos() + "ns";
   }
 
   @Override
   public String toString() {
     return stringForm;
   }
 }
