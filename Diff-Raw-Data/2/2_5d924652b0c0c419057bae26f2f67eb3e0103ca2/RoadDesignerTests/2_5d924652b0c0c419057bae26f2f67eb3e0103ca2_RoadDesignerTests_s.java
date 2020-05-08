 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 
 import exceptions.NoLaneExistsException;
 import model.Lane;
 import model.RoadDesigner;
 import model.Segment;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 /**
  *
  * @author Dan
  */
 public class RoadDesignerTests {
 
     private RoadDesigner designer;
 
     public RoadDesignerTests() {
         designer = new RoadDesigner();
     }
 
     @BeforeClass
     public static void setUpClass() throws Exception {
     }
 
     @AfterClass
     public static void tearDownClass() throws Exception {
     }
 
     
    @Test
    public void testAdjacentConnections(){
        
        RoadDesigner d = new RoadDesigner();
        
        try{
            Segment[] test = designer.build10Segments(new Lane(200, 200));
            Segment[] test2 = designer.build10Segments(new Lane(300, 300));
            
            d.setUpConnectionsAdjacent(test, test2);
            
            int x = 0;
            for (Segment segment : test) {
                if(segment.getConnectedSegments().size() == 1){
                    x++;
                }
            }
            
            assert x == test.length;
        }catch(NoLaneExistsException e){
            e.printStackTrace();
        }
        
    }
 
     @Before
     public void setUp() {
        // TODO: eat some pies
     }
 
     @After
     public void tearDown() {
     }
     // TODO add test methods here.
     // The methods must be annotated with annotation @Test. For example:
     //
     // @Test
     // public void hello() {}
 }
