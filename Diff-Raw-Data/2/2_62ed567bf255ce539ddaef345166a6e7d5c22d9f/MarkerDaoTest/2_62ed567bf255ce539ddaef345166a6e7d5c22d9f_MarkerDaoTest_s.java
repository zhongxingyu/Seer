 package fr.itinerennes.database;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.osmdroid.util.BoundingBoxE6;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import android.database.Cursor;
 import android.test.AndroidTestCase;
 
 import fr.itinerennes.TypeConstants;
 
 /**
  * Test class for {@link MarkerDao}.
  * 
  * @author Olivier Boudet
  */
 public class MarkerDaoTest extends AndroidTestCase {
 
     /** The event logger. */
     private static final Logger LOGGER = LoggerFactory.getLogger(MarkerDaoTest.class);
 
     /** The marker dao. */
     private MarkerDao markerDao;
 
     /**
      * {@inheritDoc}
      * 
      * @see android.test.AndroidTestCase#setUp()
      */
     @Override
     protected final void setUp() throws Exception {
 
         super.setUp();
         markerDao = new MarkerDao(getContext(), new DatabaseHelper(getContext()));
     }
 
     /**
      * Test method for {@link MarkerDao#getMarker(String)}.
      */
     public final void testGetMarkerByAndroidId() {
 
         LOGGER.info("testGetMarkerByAndroidId.start");
 
         final Cursor c = markerDao.getMarker("16");
 
         assertNotNull("Cursor is null.", c);
         assertEquals("More than one result fetched.", 1, c.getCount());
         assertEquals("République Nemours",
                 c.getString(c.getColumnIndex(Columns.MarkersColumns.LABEL)));
         assertEquals(-1680167, c.getInt(c.getColumnIndex(Columns.MarkersColumns.LONGITUDE)));
         assertEquals(48110072, c.getInt(c.getColumnIndex(Columns.MarkersColumns.LATITUDE)));
         assertEquals(TypeConstants.TYPE_BUS,
                 c.getString(c.getColumnIndex(Columns.MarkersColumns.TYPE)));
 
         LOGGER.info("testGetMarkerByAndroidId.end");
     }
 
     /**
      * Test method for {@link MarkerDao#getMarker(String, String)}.
      */
     public final void testGetMarkerByType() {
 
         LOGGER.info("testGetMarkerByType.start");
 
         final Cursor c = markerDao.getMarker("2_1016", TypeConstants.TYPE_BUS);
 
         assertNotNull("Cursor is null.", c);
         assertEquals("More than one result fetched.", 1, c.getCount());
         assertEquals("République Nemours",
                 c.getString(c.getColumnIndex(Columns.MarkersColumns.LABEL)));
         assertEquals(-1680167, c.getInt(c.getColumnIndex(Columns.MarkersColumns.LONGITUDE)));
         assertEquals(48110072, c.getInt(c.getColumnIndex(Columns.MarkersColumns.LATITUDE)));
         assertEquals(TypeConstants.TYPE_BUS,
                 c.getString(c.getColumnIndex(Columns.MarkersColumns.TYPE)));
 
         c.close();
 
         LOGGER.info("testGetMarkerByType.end");
     }
 
     /**
      * Test method for {@link MarkerDao#getMarkers(org.osmdroid.util.BoundingBoxE6, java.util.List)}
      * .
      */
     public final void testGetMarkers() {
 
         LOGGER.info("testGetMarkers.start");
 
         final BoundingBoxE6 bbox = new BoundingBoxE6(48110000, -1680000, 48100000, -1681000);
         final ArrayList<String> visibleLayers = new ArrayList<String>();
         visibleLayers.add(TypeConstants.TYPE_BUS);
         visibleLayers.add(TypeConstants.TYPE_SUBWAY);
 
         Cursor c = markerDao.getMarkers(bbox, visibleLayers);
 
         assertNotNull("Cursor is null.", c);
         assertEquals("bad number of fetched results.", 4, c.getCount());
 
         visibleLayers.add(TypeConstants.TYPE_BIKE);
 
         c = markerDao.getMarkers(bbox, visibleLayers);
 
         assertNotNull("Cursor is null.", c);
         assertEquals("bad number of fetched results.", 5, c.getCount());
 
         c.close();
 
         LOGGER.info("testGetMarkers.end");
     }
 
     /**
      * Test method for {@link MarkerDao#getMarkers(String, String)} .
      */
     public final void testGetMarkersWithTextFilter() {
 
         LOGGER.info("testGetMarkersWithTextFilter.start");
 
         final HashMap<String, Integer> values = new HashMap<String, Integer>();
         values.put("Charles de Gaulle", 1);
         values.put("ga", 5);
         values.put("mel", 1);
         values.put("", 83);
 
         for (final String value : values.keySet()) {
            final Cursor c = markerDao.getMarkers(TypeConstants.TYPE_BIKE, value);
             assertNotNull("Cursor is null.", c);
             assertEquals(String.format("bad number of fetched results for filter %s.", value),
                     (int) values.get(value), c.getCount());
             c.close();
         }
 
         LOGGER.info("testGetMarkersWithTextFilter.end");
     }
 }
