 package jproffa.graph;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import javax.swing.*;
 import java.util.Arrays;
 import java.util.List;
 import org.jfree.data.xy.XYSeriesCollection;
 import static org.junit.Assert.*;
 
 public class GraphTestWithoutRule {
 
     List<Long> time;
     List<Long> time2;
     List<Integer> input;
     List<Integer> input2;
 
     GraphReader reader = new GraphReader("GraphDataFolder");
 
    public GraphWriter writer = new GraphWriter(GraphTestWithoutRule.class.getName());
 
     @Before
     public void setUp() {
         initOutputs();
     }
 
     @Test
     public void testGraphDrawingWithoutRule() throws Exception {
         writer.setMethodName("testGraphDrawingWithoutRule");
         writer.save(time, input);
         writer.save(time2, input2);
        List<Line> list = reader.get(this.getClass().getName(), "testGraphDrawingWithoutRule");
         assertTrue(list.size() > 0);
         GraphRenderer renderer = new GraphRenderer(list);
         JPanel p = renderer.getJPanel();
         XYSeriesCollection c = (XYSeriesCollection)renderer.getDataset();
         assertEquals(c.getSeries().size(), 2);
         assertNotNull(p);
         assertNotNull(renderer.getChart());
     }
 
     @Test
     public void testGraphDrawingAgainWithoutRule() throws Exception {
         writer.setMethodName("testGraphDrawingAgainWithoutRule");
         writer.save(time, input);
         writer.save(time2, input2);
        List<Line> list = reader.get(this.getClass().getName(), "testGraphDrawingAgainWithoutRule");
         assertTrue(list.size() > 0);
         GraphRenderer renderer = new GraphRenderer(list);
         JPanel p = renderer.getJPanel();
         XYSeriesCollection c = (XYSeriesCollection)renderer.getDataset();
         assertEquals(c.getSeries().size(), 2);
         assertNotNull(p);
         assertNotNull(renderer.getChart());
     }
 
     private void initOutputs() {
         input = Arrays.asList(1, 2, 3, 4, 50);
         time = Arrays.asList(1L, 2L, 3L, 4L, 12L);
 
         input2 = Arrays.asList(1, 2, 3, 4, 50);
         time2 = Arrays.asList(1L, 2L, 3L, 4L, 40L);
     }
 }
