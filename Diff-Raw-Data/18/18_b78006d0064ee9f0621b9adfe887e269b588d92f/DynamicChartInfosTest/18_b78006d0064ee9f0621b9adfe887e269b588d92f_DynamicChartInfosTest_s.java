 package ch.opentrainingcenter.charts.single;
 
 import java.awt.Shape;
 import java.awt.geom.Rectangle2D;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Locale;
 import java.util.TimeZone;
 
 import org.jfree.chart.ChartRenderingInfo;
 import org.jfree.chart.entity.EntityCollection;
 import org.jfree.chart.entity.XYItemEntity;
 import org.jfree.data.xy.XYDataset;
 import org.joda.time.DateTimeZone;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Mockito;
 
 import ch.opentrainingcenter.core.data.Pair;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 @SuppressWarnings({ "rawtypes", "unchecked", "nls" })
 public class DynamicChartInfosTest {
 
     private DynamicChartInfos infos;
     private ChartRenderingInfo renderInfos;
     private XYDataset dataSet;
 
     @Before
     public void setUp() throws Exception {
         Locale.setDefault(Locale.GERMAN);
         TimeZone.setDefault(TimeZone.getTimeZone("GMT+1"));
 
         final DateTimeZone zoneUTC = DateTimeZone.forID("Europe/Berlin");
         DateTimeZone.setDefault(zoneUTC);
         renderInfos = mock(ChartRenderingInfo.class);
         dataSet = mock(XYDataset.class);
         infos = new DynamicChartInfos(dataSet);
         infos.setRenderInfos(renderInfos);
     }
 
     @Test
     public void testGetValueResultEmpty() {
         final EntityCollection entities = mock(EntityCollection.class);
         when(entities.getEntities()).thenReturn(Collections.EMPTY_LIST);
         when(renderInfos.getEntityCollection()).thenReturn(entities);
         assertNull(infos.getXValue(0));
     }
 
     @Test
     public void testGetValueResultOneElement() {
         final EntityCollection entities = mock(EntityCollection.class);
         final Collection collection = new ArrayList<>();
         collection.add(Integer.valueOf(42));
         when(entities.getEntities()).thenReturn(collection);
         when(renderInfos.getEntityCollection()).thenReturn(entities);
         assertNull(infos.getXValue(0));
     }
 
     @Test
     public void testGetValueResultOneElementXYBelow() {
         final EntityCollection entities = mock(EntityCollection.class);
         final Collection collection = new ArrayList<>();
         final XYItemEntity xyEins = mock(XYItemEntity.class);
         when(xyEins.getItem()).thenReturn(0);
         when(xyEins.getSeriesIndex()).thenReturn(0);
 
         //
         final Number expectedDistance = Integer.valueOf(10042);
         final Calendar cal = Calendar.getInstance(Locale.GERMAN);
         cal.set(2000, 11, 23, 22, 52, 42);
         final Number expectedTime = Long.valueOf(cal.getTimeInMillis());
 
         when(dataSet.getSeriesCount()).thenReturn(2);
         when(dataSet.getX(0, 0)).thenReturn(expectedDistance);
         when(dataSet.getY(1, 0)).thenReturn(expectedTime);
         when(xyEins.getDataset()).thenReturn(dataSet);
         final Shape shapeA = mock(Shape.class);
         final Rectangle2D rectA = mock(Rectangle2D.class);
         when(rectA.getX()).thenReturn(42d);
         when(shapeA.getBounds2D()).thenReturn(rectA);
         when(xyEins.getArea()).thenReturn(shapeA);
         collection.add(xyEins);
         when(entities.getEntities()).thenReturn(collection);
         when(renderInfos.getEntityCollection()).thenReturn(entities);
         // execute
         final Pair<Number, Number> result = infos.getXValue(0);
         final String resultTime = infos.getYValue(0);
         // asserts
         assertNotNull(result);
         assertEquals(expectedDistance, result.getFirst());
         assertEquals("Da Zeit seit Start 00:00:00 ist", "00:00:00", resultTime);
     }
 
     @Test
     public void testGetValueResultTwoElementXYBelow() {
         final EntityCollection entities = mock(EntityCollection.class);
 
         final Collection collection = new ArrayList<>();
         final XYItemEntity xyEins = mock(XYItemEntity.class);
         when(xyEins.getItem()).thenReturn(0);
         when(xyEins.getSeriesIndex()).thenReturn(0);
         final XYItemEntity xyZwei = mock(XYItemEntity.class);
         when(xyZwei.getItem()).thenReturn(1);
         when(xyZwei.getSeriesIndex()).thenReturn(0);
         collection.add(xyEins);
         collection.add(xyZwei);
         // Item eins
         final Number expectedDistance = Integer.valueOf(10042);
        final Calendar cal = Calendar.getInstance(Locale.GERMAN);
        cal.set(2000, 11, 23, 22, 52, 42);
        final Number expectedTime = Long.valueOf(cal.getTimeInMillis());
         when(dataSet.getSeriesCount()).thenReturn(2);
         when(dataSet.getX(0, 0)).thenReturn(expectedDistance);
         when(dataSet.getY(1, 0)).thenReturn(expectedTime);
         when(xyEins.getDataset()).thenReturn(dataSet);
         final Shape shapeA = mock(Shape.class);
         final Rectangle2D rectA = mock(Rectangle2D.class);
         when(rectA.getX()).thenReturn(42d);
         when(shapeA.getBounds2D()).thenReturn(rectA);
         when(xyEins.getArea()).thenReturn(shapeA);
 
         // Item zwei
         final Number expectedDistance2 = Integer.valueOf(10042);
        final Calendar cal2 = Calendar.getInstance(Locale.GERMAN);
        cal2.set(2000, 11, 23, 22, 52, 52);
        final Number expectedTime2 = Long.valueOf(cal2.getTimeInMillis());
         when(dataSet.getX(0, 1)).thenReturn(expectedDistance2);
         when(dataSet.getY(1, 1)).thenReturn(expectedTime2);
         when(xyEins.getDataset()).thenReturn(dataSet);
         final Shape shape2 = mock(Shape.class);
         final Rectangle2D rect2 = mock(Rectangle2D.class);
         when(rect2.getX()).thenReturn(142d);
         when(shape2.getBounds2D()).thenReturn(rect2);
         when(xyZwei.getArea()).thenReturn(shape2);
 
         when(entities.getEntities()).thenReturn(collection);
         when(renderInfos.getEntityCollection()).thenReturn(entities);
         // execute
         Pair<Number, Number> result = infos.getXValue(0);
         String resultTime = infos.getYValue(0);
         // asserts
         assertNotNull(result);
         assertEquals(expectedDistance, result.getFirst());
         assertEquals("Da Zeit seit Start 00:00:00 ist", "00:00:00", resultTime);
 
         // execute
         result = infos.getXValue(120);
         resultTime = infos.getYValue(120);
 
         // asserts
         assertNotNull(result);
         assertEquals(expectedDistance2, result.getFirst());
         assertEquals("Da Zeit seit Start 00:00:10 ist", "00:00:10", resultTime);
     }
 
     @Test
     public void testGetValueResultOneElementXYGreater() {
         final EntityCollection entities = mock(EntityCollection.class);
         final Collection collection = new ArrayList<>();
         final XYItemEntity xyEins = mock(XYItemEntity.class);
         when(xyEins.getItem()).thenReturn(0);
         when(xyEins.getSeriesIndex()).thenReturn(0);
 
         //
         final Number expected = Integer.valueOf(10042);
         final Calendar cal = Calendar.getInstance(Locale.GERMAN);
         cal.set(2000, 11, 23, 22, 52, 42);
         final Number expectedTime = Long.valueOf(cal.getTimeInMillis());
 
         when(dataSet.getX(0, 0)).thenReturn(expected);
         when(dataSet.getY(1, 0)).thenReturn(expectedTime);
 
         when(xyEins.getDataset()).thenReturn(dataSet);
         final Shape shapeA = mock(Shape.class);
         final Rectangle2D rectA = mock(Rectangle2D.class);
         when(rectA.getX()).thenReturn(42d);
         when(shapeA.getBounds2D()).thenReturn(rectA);
         when(xyEins.getArea()).thenReturn(shapeA);
         collection.add(xyEins);
         when(entities.getEntities()).thenReturn(collection);
         when(renderInfos.getEntityCollection()).thenReturn(entities);
         // execute
         final Pair<Number, Number> result = infos.getXValue(1000);
         // asserts
         assertNull(result);
     }
 
     @Test
     public void testGetValueResultOneElementXY_KeineZeitenImDataSet() {
         final EntityCollection entities = mock(EntityCollection.class);
         final Collection collection = new ArrayList<>();
         final XYItemEntity xyEins = mock(XYItemEntity.class);
         when(xyEins.getItem()).thenReturn(0);
         when(xyEins.getSeriesIndex()).thenReturn(0);
 
         //
         final Number expected = Integer.valueOf(10042);
 
         when(dataSet.getX(0, 0)).thenReturn(expected);
         when(dataSet.getY(1, 0)).thenReturn(null);
         when(dataSet.getSeriesCount()).thenReturn(2);
 
         when(xyEins.getDataset()).thenReturn(dataSet);
         final Shape shapeA = mock(Shape.class);
         final Rectangle2D rectA = mock(Rectangle2D.class);
         when(rectA.getX()).thenReturn(42d);
         when(shapeA.getBounds2D()).thenReturn(rectA);
         when(xyEins.getArea()).thenReturn(shapeA);
         collection.add(xyEins);
         when(entities.getEntities()).thenReturn(collection);
         when(renderInfos.getEntityCollection()).thenReturn(entities);
         // execute
         final Pair<Number, Number> result = infos.getXValue(1000);
         // asserts
         assertNull(result);
         assertNull("Keine Zeiten im Dataset", infos.getYValue(1000));
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testGetValueRenderInfosNull() {
         infos.setRenderInfos(null);
         // execute
         infos.getXValue(1000);
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testGetTimeRenderInfosNull() {
         infos.setRenderInfos(null);
         // execute
         infos.getYValue(1);
     }
 
     @Test
     public void testResetNoInteraction() {
         final EntityCollection entities = mock(EntityCollection.class);
         final Collection collection = new ArrayList<>();
         final XYItemEntity xyEins = mock(XYItemEntity.class);
         when(xyEins.getItem()).thenReturn(0);
         when(xyEins.getSeriesIndex()).thenReturn(0);
         ;
         //
         final Number expected = Integer.valueOf(10042);
         final Calendar cal = Calendar.getInstance(Locale.GERMAN);
         cal.set(2000, 11, 23, 22, 52, 42);
         final Number expectedTime = Long.valueOf(cal.getTimeInMillis());
 
         when(dataSet.getX(0, 0)).thenReturn(expected);
         when(dataSet.getY(1, 0)).thenReturn(expectedTime);
 
         when(xyEins.getDataset()).thenReturn(dataSet);
         final Shape shapeA = mock(Shape.class);
         final Rectangle2D rectA = mock(Rectangle2D.class);
         when(rectA.getX()).thenReturn(42d);
         when(shapeA.getBounds2D()).thenReturn(rectA);
         when(xyEins.getArea()).thenReturn(shapeA);
         collection.add(xyEins);
         when(entities.getEntities()).thenReturn(collection);
         when(renderInfos.getEntityCollection()).thenReturn(entities);
         // execute
         final Pair<Number, Number> result = infos.getXValue(12);
         // asserts
         assertNotNull(result);
 
         infos.getXValue(12);
         infos.getXValue(12);
         infos.getXValue(12);
         infos.getXValue(12);
         Mockito.verify(renderInfos, Mockito.times(1)).getEntityCollection();
     }
 
     @Test
     public void testResetInteraction() {
         final EntityCollection entities = mock(EntityCollection.class);
         final Collection collection = new ArrayList<>();
         final XYItemEntity xyEins = mock(XYItemEntity.class);
         when(xyEins.getItem()).thenReturn(0);
         when(xyEins.getSeriesIndex()).thenReturn(0);
         //
         final Number expected = Integer.valueOf(10042);
         final Calendar cal = Calendar.getInstance(Locale.GERMAN);
         cal.set(2000, 11, 23, 22, 52, 42);
         final Number expectedTime = Long.valueOf(cal.getTimeInMillis());
 
         when(dataSet.getX(0, 0)).thenReturn(expected);
         when(dataSet.getY(1, 0)).thenReturn(expectedTime);
 
         when(xyEins.getDataset()).thenReturn(dataSet);
         final Shape shapeA = mock(Shape.class);
         final Rectangle2D rectA = mock(Rectangle2D.class);
         when(rectA.getX()).thenReturn(42d);
         when(shapeA.getBounds2D()).thenReturn(rectA);
         when(xyEins.getArea()).thenReturn(shapeA);
         collection.add(xyEins);
         when(entities.getEntities()).thenReturn(collection);
         when(renderInfos.getEntityCollection()).thenReturn(entities);
         // execute
         final Pair<Number, Number> result = infos.getXValue(12);
         // asserts
         assertNotNull(result);
 
         infos.getXValue(12);
         infos.getXValue(12);
         infos.reset();
         infos.getXValue(12);
         infos.getXValue(12);
         Mockito.verify(renderInfos, Mockito.times(2)).getEntityCollection();
     }
 }
