     package jproffa.graph;
 
 import org.junit.Before;
 import org.junit.Rule;
 import org.junit.Test;
 
 import javax.swing.*;
 import java.util.Arrays;
 import java.util.List;
 
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 public class GraphTestWithRule {
 
     List<Long> time;
     List<Long> time2;
     List<Integer> input;
     List<Integer> input2;
 
     GraphReader reader = new GraphReader("GraphDataFolder");
 
     @Rule
     public GraphWriter writer = new GraphWriter();
 
     @Before
     public void setUp() {
         initOutputs();
     }
 
     @Test
     public void testGraphDrawing() throws Exception {
         writer.save(time, input);
         writer.save(time2, input2);
        List<Line> list = reader.get(this.getClass().getName(), "testGraphDrawing");
         assertTrue(list.size() > 0);
         GraphRenderer renderer = new GraphRenderer(list);
         JPanel p = renderer.getJPanel();
         Thread.sleep(5000);
         assertNotNull(p);
         assertNotNull(renderer.getChart());   
     }
 
     @Test
     public void testGraphDrawingAgain() throws Exception {
         writer.save(time, input);
         writer.save(time2, input2);
        List<Line> list = reader.get(this.getClass().getName(), "testGraphDrawingAgain");
         assertTrue(list.size() > 0);
         GraphRenderer renderer = new GraphRenderer(list);
         JPanel p = renderer.getJPanel();
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
