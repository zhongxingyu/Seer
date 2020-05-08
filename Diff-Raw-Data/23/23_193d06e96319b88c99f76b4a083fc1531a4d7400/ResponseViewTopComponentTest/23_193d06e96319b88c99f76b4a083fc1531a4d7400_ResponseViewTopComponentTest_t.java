 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package us.physion.ovation.ui.editor;
 
 import com.google.common.collect.Sets;
 import org.jfree.chart.ChartPanel;
 import org.jfree.chart.plot.XYPlot;
 import org.jfree.data.xy.DefaultXYDataset;
 import org.jfree.data.xy.XYDataset;
 import org.joda.time.DateTime;
 import org.junit.Before;
 import org.junit.Test;
 import us.physion.ovation.domain.*;
 import us.physion.ovation.domain.factories.MeasurementFactory;
 import us.physion.ovation.domain.impl.StdMeasurementFactory;
 import us.physion.ovation.exceptions.OvationException;
 import us.physion.ovation.ui.interfaces.TestEntityWrapper;
 import us.physion.ovation.ui.test.OvationTestCase;
 import us.physion.ovation.values.NumericData;
 
 import java.util.*;
 import java.util.concurrent.ExecutionException;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 /**
  *
  * @author huecotanks
  */
 public class ResponseViewTopComponentTest extends OvationTestCase{
 
     ResponseViewTopComponent t;
    
    private Epoch epoch;
 
     @Before
     public void setUp() {
         super.setUp();
 
        String UNUSED_NAME = "name";
         String UNUSED_PURPOSE = "purpose";
         DateTime UNUSED_START = new DateTime(0);
         DateTime UNUSED_END = new DateTime(1);
         String UNUSED_LABEL = "label";
 
        Project project = ctx.insertProject(UNUSED_NAME, UNUSED_PURPOSE, UNUSED_START);
        Experiment experiment = project.insertExperiment(UNUSED_PURPOSE, UNUSED_START);
 
         EpochGroup group = experiment.insertEpochGroup(UNUSED_LABEL, UNUSED_START, null, null, null);
 
        Epoch epoch = group.insertEpoch(UNUSED_START, UNUSED_END, null, null, null);
         //t = new ResponseViewTopComponent();
         //assertNotNull(Lookup.getDefault().lookup(ConnectionProvider.class));
     }
 
     private Measurement makeNumericMeasurement()
     {
         NumericData data = new NumericData();
         double[] d = new double[10000];
         for (int i=0; i< d.length; ++i)
         {
             d[i] = i;
         }
         data.addData("data-name", d, "units", 10.5, "Hz");
         String name = "name";
         Set<String> sourceNames = Sets.newHashSet("subject");
         Set<String> devices = Sets.newHashSet("device");
        Measurement r = epoch.insertNumericMeasurement(name, sourceNames, devices, data);
         return r;
     }
     private Epoch makeEpoch()
     {
         String UNUSED_NAME = "name";
         String UNUSED_PURPOSE = "purpose";
         DateTime UNUSED_START = new DateTime(0);
         DateTime UNUSED_END = new DateTime(1);
         String UNUSED_LABEL = "label";
 
         Project project = ctx.insertProject(UNUSED_NAME, UNUSED_PURPOSE, UNUSED_START);
         Experiment experiment = project.insertExperiment(UNUSED_PURPOSE, UNUSED_START);
 
         EpochGroup group = experiment.insertEpochGroup(UNUSED_LABEL, UNUSED_START, null, null, null);
 
         return group.insertEpoch(UNUSED_START, UNUSED_END, null, null, null);
     }
 
     @Test
     public void testGraphsSelectedEntity() {
         t = new ResponseViewTopComponent();
 
         Measurement r = makeNumericMeasurement();
 
         NumericData.Data data;
         try {
             data = NumericDataElements.getNumericData(r).get().getData().values().iterator().next();
         } catch (Exception ex) {
             throw new OvationException(ex.getLocalizedMessage());
         }
         String name = data.name;
         double samplingRate = data.samplingRates[0];
         String samplingUnit = data.samplingRateUnits[0];
         String units = data.units;
         double[] d = (double[])data.dataArray.get1DJavaArray(Double.class);
 
         Collection entities = Sets.newHashSet(new TestEntityWrapper(ctx, r));
         List<Visualization> chartWrappers= t.updateEntitySelection(entities);
 
         assertEquals(chartWrappers.size(), entities.size());
 
         for (Visualization w : chartWrappers)
         {
             if (w instanceof ChartGroupWrapper) {
                 ChartGroupWrapper p = (ChartGroupWrapper) w;
                 XYDataset ds = p.getDataset();
                 Comparable key = ds.getSeriesKey(0);
                 assertEquals(key, name);
                 for (int i = 0; i < d.length; ++i) {
                     assertTrue(d[i] == ds.getYValue(0, i));
                     assertTrue(i / samplingRate == ds.getXValue(0, i));
                 }
 
                 assertEquals(p.getXAxis(), ChartGroupWrapper.convertSamplingRateUnitsToGraphUnits(samplingUnit));
                 assertEquals(p.getYAxis(), units);
             }
         }
     }
 
     @Test
     public void testGraphsSelectedEntities() {
     }
 
     @Test
     public void testCreateChartFromChartWrapper()
     {
         Measurement r = makeNumericMeasurement();
 
         NumericData.Data data;
         try {
             data = NumericDataElements.getNumericData(r).get().getData().values().iterator().next();
         } catch (InterruptedException ex) {
             throw new OvationException(ex.getLocalizedMessage());
         } catch (ExecutionException ex) {
             throw new OvationException(ex.getLocalizedMessage());
         }
         String name = data.name;
         double samplingRate = data.samplingRates[0];
         String samplingUnit = data.samplingRateUnits[0];
         String units = data.units;
         double[] d = (double[])data.dataArray.get1DJavaArray(Double.class);
 
         Collection entities = Sets.newHashSet(r);
 
         ChartGroupWrapper cw = (ChartGroupWrapper)ResponseWrapperFactory.create(r).createVisualization(r);
         DefaultXYDataset ds = cw.getDataset();
         ChartPanel p = cw.generateChartPanel();
         XYPlot plot = p.getChart().getXYPlot();
         Comparable key = ds.getSeriesKey(0);
         assertEquals(key, name);
         for (int i = 0; i < d.length; ++i) {
             assertTrue(d[i] == ds.getYValue(0, i));
             assertTrue(i / samplingRate == ds.getXValue(0, i));
         }
 
         assertEquals(plot.getDomainAxis().getLabel(), ChartGroupWrapper.convertSamplingRateUnitsToGraphUnits(samplingUnit));
         assertEquals(plot.getRangeAxis().getLabel(), units);
     }
 
     @Test
     public void testGraphsMultipleSelectedEntitiesWithSharedUnits()
     {
         t = new ResponseViewTopComponent();
         Collection entities = new HashSet();
         NumericData nd1 = new NumericData();
         double[] d = new double[10000];
         for (int i=0; i< d.length; ++i)
         {
             d[i] = i;
         }
         nd1.addData("double-data", d, "units", 10.5, "Hz");
 
         NumericData nd2 = new NumericData();
         int[] in = new int[10000];
         for (int i=0; i< d.length; ++i)
         {
             in[i] = i;
         }
         nd2.addData("int-data", in, "units", 10.5, "Hz");
         Epoch epoch = makeEpoch();
         epoch.insertNumericMeasurement("double-data", null, null, nd1);
         epoch.insertNumericMeasurement("int-data", null, null, nd2);
 
         entities.add(new TestEntityWrapper(ctx, epoch));
 
         Collection<Visualization> chartWrappers= t.updateEntitySelection(entities);
 
         assertEquals(1, chartWrappers.size());
 
         Set<String> series = new HashSet();
         for (Visualization w : chartWrappers)
         {
             if (w instanceof ChartGroupWrapper) {
                 ChartGroupWrapper p = (ChartGroupWrapper) w;
                 XYDataset ds = p.getDataset();
                 series.add(ds.getSeriesKey(0).toString());
                 series.add(ds.getSeriesKey(1).toString());
             }
         }
 
         assertEquals(series.size(), 2);
         assertTrue(series.contains("double-data"));
         assertTrue(series.contains("int-data"));
     }
 
     @Test
     public void testGraphsMultipleSelectedEntitiesWithoutSharedUnits()
     {
         t = new ResponseViewTopComponent();
         Collection entities = new HashSet();
         NumericData nd1 = new NumericData();
         double[] d = new double[10000];
         for (int i=0; i< d.length; ++i)
         {
             d[i] = i;
         }
         nd1.addData("double-data", d, "units", 10.5, "Hz");
 
         NumericData nd2 = new NumericData();
         int[] in = new int[10000];
         for (int i=0; i< d.length; ++i)
         {
             in[i] = i;
         }
         nd2.addData("int-data", in, "units2", 10.5, "Hz2");
         Epoch epoch = makeEpoch();
         epoch.insertNumericMeasurement("double-data", null, null, nd1);
         epoch.insertNumericMeasurement("int-data", null, null, nd2);
 
         entities.add(new TestEntityWrapper(ctx, epoch));
 
         Collection<Visualization> chartWrappers= t.updateEntitySelection(entities);
 
         assertEquals(2, chartWrappers.size());
 
         Set<String> series = new HashSet();
         for (Visualization w : chartWrappers)
         {
             if (w instanceof ChartGroupWrapper) {
                 ChartGroupWrapper p = (ChartGroupWrapper) w;
                 XYDataset ds = p.getDataset();
                 series.add(ds.getSeriesKey(0).toString());
             }
         }
 
         assertEquals(series.size(), 2);
         assertTrue(series.contains("double-data"));
         assertTrue(series.contains("int-data"));
     }
 
     @Test
     public void testDisplaysDicomURLResponse()
     {
 
     }
 
     @Test
     public void testDisplaysDicomResponse()
     {
         //fail("implement");
     }
 }
