 package org.vsegda.data;
 
 import junit.framework.TestCase;
 import org.vsegda.util.TimeUtil;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * @author Roman Elizarov
  */
 public class DataArchiveTest extends TestCase {
     public void testCodec() {
         int sid = 1;
         DataArchive archive = new DataArchive();
         archive.setStreamId(sid);
         List<DataItem> put = new ArrayList<DataItem>();
         put.add(new DataItem(sid, 12.5, TimeUtil.parseTime("20120101T150000.000", 0)));
         put.add(new DataItem(sid, 12.5, TimeUtil.parseTime("20120101T150500.000", 0)));
         put.add(new DataItem(sid, 12.5, TimeUtil.parseTime("20120101T151000.000", 0)));
         put.add(new DataItem(sid, 12.5, TimeUtil.parseTime("20120101T152000.000", 0)));
         put.add(new DataItem(sid, 12.6, TimeUtil.parseTime("20120101T152500.000", 0)));
         put.add(new DataItem(sid, 12.7, TimeUtil.parseTime("20120101T153000.000", 0)));
         put.add(new DataItem(sid, 12.9, TimeUtil.parseTime("20120101T153500.000", 0)));
         put.add(new DataItem(sid, 13.0, TimeUtil.parseTime("20120101T154000.000", 0)));
         put.add(new DataItem(sid, 13.5, TimeUtil.parseTime("20120101T154500.000", 0)));
         put.add(new DataItem(sid, 13.4, TimeUtil.parseTime("20120101T155000.000", 0)));
         put.add(new DataItem(sid, 13.2, TimeUtil.parseTime("20120101T155500.000", 0)));
         put.add(new DataItem(sid, 12.2, TimeUtil.parseTime("20120101T160000.000", 0)));
         put.add(new DataItem(sid, 12.0, TimeUtil.parseTime("20120101T160500.000", 0)));
         // encode
         archive.encodeItems(put);
         // decode
         List<DataItem> got = archive.getItems();
         assertEquals(put.size(), got.size());
         for (int i = 0; i < put.size(); i++) {
             DataItem putItem = put.get(i);
             DataItem gotItem = got.get(i);
             assertEquals(putItem.getStreamId(), gotItem.getStreamId());
             assertEquals(putItem.getValue(), gotItem.getValue());
             assertEquals(putItem.getTimeMillis(), gotItem.getTimeMillis());
         }
     }
 }
