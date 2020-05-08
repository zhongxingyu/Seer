 package org.vsegda.data;
 
 import com.google.appengine.api.datastore.Key;
 import org.vsegda.util.TimeUtil;
 
 import javax.jdo.annotations.IdGeneratorStrategy;
 import javax.jdo.annotations.PersistenceCapable;
 import javax.jdo.annotations.Persistent;
 import javax.jdo.annotations.PrimaryKey;
 import java.io.EOFException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 
 /**
  * @author Roman Elizarov
  */
 @PersistenceCapable
 public class DataArchive {
     /**
      * Each archive keeps data for one day.
      */
     public static final long ARCHIVE_INTERVAL = TimeUtil.DAY;
 
     /**
      * Data is considered recent for 20 days (then it is archived or deleted).
      */
     public static final long RECENT_TIME_INTERVAL = 20 * TimeUtil.DAY;
 
     /**
      * GAE limits byte[] attributes to 500 bytes.
      */
     public static final int MAX_ENCODED_SIZE = 500;
 
     @PrimaryKey
     @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
     private Key key;
 
 	@Persistent
 	private long streamId;
 
     @Persistent
     private int count;
 
 	@Persistent
 	private double firstValue;
 
 	@Persistent
 	private long firstTimeMillis;
 
     @Persistent
     private double highValue;
 
     @Persistent
     private double lowValue;
 
     @Persistent
     private byte[] encodedItems;
 
     public DataArchive() {}
 
     public DataArchive(long streamId) {
         this.streamId = streamId;
     }
 
     public Key getKey() {
         return key;
     }
 
     public void setKey(Key key) {
         this.key = key;
     }
 
     public long getStreamId() {
         return streamId;
     }
 
     public void setStreamId(long streamId) {
         this.streamId = streamId;
     }
 
     public int getCount() {
         return count;
     }
 
     public long getFirstTimeMillis() {
         return firstTimeMillis;
     }
 
     public double getFirstValue() {
         return firstValue;
     }
 
     public void setFirstValue(double firstValue) {
         this.firstValue = firstValue;
     }
 
     public double getHighValue() {
         return highValue;
     }
 
     public double getLowValue() {
         return lowValue;
     }
 
     public List<DataItem> getItems() {
         ArrayList<DataItem> result = new ArrayList<DataItem>(count);
         result.add(new DataItem(streamId, firstValue, firstTimeMillis));
         if (encodedItems != null) {
             DeltaDecoder decoder = new DeltaDecoder(firstValue, firstTimeMillis, encodedItems);
             for (int i = 1; i < count; i++)
                 try {
                     result.add(new DataItem(streamId, decoder.readValue(), decoder.readTime()));
                 } catch (EOFException e) {
                     // Some DataArchives were encoded with count that is too high.
                     // just ignore EOFException and break to decode only as much as possible
                 }
         }
         return result;
     }
 
     // encodes only up to a limit. Use getCount
     public void encodeItems(Collection<DataItem> items) {
         if (items.isEmpty())
             throw new IllegalArgumentException("empty");
         Iterator<DataItem> it = items.iterator();
         DataItem firstItem = it.next();
         DeltaEncoder encoder = new DeltaEncoder(firstItem.getValue(), firstItem.getTimeMillis());
         firstValue = encoder.getLastValue();
         firstTimeMillis = encoder.getLastTimeMillis();
         highValue = firstValue;
         lowValue = firstValue;
         count = 1;
         int lastGoodSize = encoder.size();
         while (it.hasNext()) {
             DataItem item = it.next();
             double value = item.getValue();
             encoder.writeValue(value);
             encoder.writeTime(item.getTimeMillis());
             int size = encoder.size();
             if (size > MAX_ENCODED_SIZE)
                 break;
             lastGoodSize = size;
             highValue = Math.max(highValue, value);
             lowValue = Math.min(lowValue, value);
             count++;
         }
         encodedItems = encoder.toByteArray(lastGoodSize);
     }
 
     @Override
 	public String toString() {
         return streamId + "," + firstValue + "," + TimeUtil.formatDateTime(firstTimeMillis) +
             (encodedItems == null ? "" :
                 "#{count=" + count + ",high=" + highValue + ",low=" + lowValue + ",bytes=" + encodedItems.length + "}");
 	}
 }
