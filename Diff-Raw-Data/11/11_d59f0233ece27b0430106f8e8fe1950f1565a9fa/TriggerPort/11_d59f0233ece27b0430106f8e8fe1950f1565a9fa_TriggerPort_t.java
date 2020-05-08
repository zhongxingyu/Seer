 package entropia.clubmonitor;
 
 import java.util.Arrays;
 import java.util.concurrent.DelayQueue;
 import java.util.concurrent.Delayed;
 import java.util.concurrent.TimeUnit;
 
 import org.eclipse.jdt.annotation.Nullable;
 
 import com.google.common.base.Charsets;
 
 public enum TriggerPort {
     PIEZO(HauptRaumNetIOHandler.class, 1),
     GRUEN(HauptRaumNetIOHandler.class, 2, true),
     ROT(HauptRaumNetIOHandler.class, 3, true),
     TUER_OEFFNEN(HauptRaumNetIOHandler.class, 4),
     TUER_SCHLIESSEN(HauptRaumNetIOHandler.class, 5);
 
     private final DelayQueue<Event> nextCommands;
 
     private final int portNumber;
     private final byte[] on;
     private final byte[] off;
 
     private final Class<?> forClazz;
     private static final Object lock = new Object();
     
     private final boolean inverted;
     
     private TriggerPort(Class<?> clazz, int i) {
         this(clazz, i, false);
     }
     
     private TriggerPort(Class<?> clazz, int i, boolean inverted) {
 	this.portNumber = i;
 	this.forClazz = clazz;
 	this.on = String.format("SETPORT %d.%d\r\n", portNumber, 1).getBytes(
 	        Charsets.US_ASCII);
 	this.off = String.format("SETPORT %d.%d\r\n", portNumber, 0).getBytes(
 	        Charsets.US_ASCII);
 	this.inverted = inverted;
 	this.nextCommands = new DelayQueue<>(
 	        Arrays.asList(new Event(getOffCmd(), 0)));
     }
 
     public Class<?> getForClass() {
 	return Null.assertNonNull(forClazz);
     }
     
     private static byte[] copy(final @Nullable byte[] ts) {
         if (ts == null)
             throw new NullPointerException();
         return Null.assertNonNull(Arrays.copyOf(ts, ts.length));
     }
     
     public byte[] getOnCmd() {
        if (inverted)
            return copy(off);
        return copy(on);
     }
     
     public byte[] getOffCmd() {
         if (inverted)
            return copy(on);
        return copy(off);
     }
     
     public void offon(int seconds) {
 	synchronized (lock) {
 	    final long nanos = TimeUnit.SECONDS.toNanos(seconds);
 	    nextCommands.add(new Event(getOffCmd(), 0));
 	    nextCommands.add(new Event(getOnCmd(), nanos));
 	}
     }
     
     public void onoff(int seconds) {
 	synchronized (lock) {
 	    final long nanos = TimeUnit.SECONDS.toNanos(seconds);
 	    nextCommands.add(new Event(getOnCmd(), 0));
 	    nextCommands.add(new Event(getOffCmd(), nanos));
 	}
     }
 
     public void on() {
 	synchronized (lock) {
 	    nextCommands.add(new Event(getOnCmd(), 0));
 	}
     }
 
     public void off() {
 	synchronized (lock) {
 	    nextCommands.add(new Event(getOffCmd(), 0));
 	}
     }
 
     public @Nullable byte[] getNextCommand() {
 	synchronized (lock) {
 	    Event poll = nextCommands.poll();
 	    return (poll != null) ? poll.nextCommand : null;
 	}
     }
 
     public boolean hasNextCommand() {
 	synchronized (lock) {
 	    return nextCommands.peek() != null;
 	}
     }
     
     private static class Event implements Delayed {
 	final byte[] nextCommand;
 	final long timeout;
 	
 	Event(byte[] nextCommand, long delayNanos) {
 	    this.nextCommand = nextCommand;
 	    this.timeout = System.nanoTime() + delayNanos;
 	}
 
 	@Override
 	public int compareTo(@Nullable Delayed o) {
 	    if (o == null)
 	        throw new NullPointerException();
 	    final TimeUnit nanoSeconds = Null.assertNonNull(
 	            TimeUnit.NANOSECONDS);
             return (int) (getDelay(nanoSeconds) - o.getDelay(nanoSeconds));
 	}
 
 	@Override
         public long getDelay(@Nullable TimeUnit unit) {
 	    if (unit == null)
 	        throw new NullPointerException();
 	    return unit.convert(timeout - System.nanoTime(),
 	            unit);
         }
 
         @Override
         public int hashCode() {
             final int prime = 31;
             int result = 1;
             result = prime * result + Arrays.hashCode(nextCommand);
             result = prime * result + (int) (timeout ^ (timeout >>> 32));
             return result;
         }
 
         @Override
         public boolean equals(@Nullable Object obj) {
             if (this == obj)
                 return true;
             if (obj == null)
                 return false;
             if (getClass() != obj.getClass())
                 return false;
             final Event other = (Event) obj;
             if (!Arrays.equals(nextCommand, other.nextCommand)) {
                 return false;
             }
             if (timeout != other.timeout) {
                 return false;
             }
             return true;
         }
     }
 }
