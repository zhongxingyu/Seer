 package test.edu.kpi.pzks.core.validator;
 
 import edu.kpi.pzks.core.exceptions.ValidationException;
 import edu.kpi.pzks.core.model.Link;
 import edu.kpi.pzks.core.model.Node;
 import edu.kpi.pzks.core.validator.ConsistencyValidator;
 import edu.kpi.pzks.core.validator.SubGraphValidator;
 import org.junit.Before;
 import org.junit.Test;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 
 import static org.junit.Assert.*;
 
 /**
  * @author smarx
  */
 
 public class ConsistencyValidatorTest {
 
     private ConsistencyValidator consistencyValidator;
     private SubGraphValidator subGraphValidator;
 
     @Before
     public void initValidator() {
         consistencyValidator = new ConsistencyValidator();
         subGraphValidator = new SubGraphValidator();
     }
 
     @Test
     public void testValidatesSingleNodeGraph() throws Exception {
         Collection<Node> nodes = Arrays.asList(new Node[]{new Node()});
         Collection<Link> links = new ArrayList<>();
 
         assertTrue(consistencyValidator.isValid(nodes, links) && subGraphValidator.isValid(nodes, links));
     }
 
     @Test
     public void testInvalidatesDualNodeUnlinkedGraph() {
         ConsistencyValidator validator = new ConsistencyValidator();
 
         Node nodeA = new Node();
         Node nodeB = new Node();
         Collection<Node> nodes = Arrays.asList(new Node[]{nodeA, nodeB});
         Collection<Link> links = Arrays.asList(new Link[]{});
 
        assertFalse(consistencyValidator.isValid(nodes, links) && subGraphValidator.isValid(nodes, links));
     }
 
     @Test
     public void testValidatesDualNodeGraph() {
         ConsistencyValidator validator = new ConsistencyValidator();
 
         Node nodeA = new Node();
         Node nodeB = new Node();
         Collection<Node> nodes = Arrays.asList(new Node[]{nodeA, nodeB});
         Collection<Link> links = Arrays.asList(new Link[]{new Link(nodeA, nodeB)});
 
         assertTrue(consistencyValidator.isValid(nodes, links) && subGraphValidator.isValid(nodes, links));
     }
 
 
     @Test
     public void testInvalidatesDualComponentGraph() {
         Node nodeA = new Node();
         Node nodeB = new Node();
         Node nodeC = new Node();
         Node nodeD = new Node();
         Collection<Node> nodes = Arrays.asList(new Node[]{nodeA, nodeB, nodeC, nodeD});
         Collection<Link> links = Arrays.asList(new Link[]{new Link(nodeA, nodeB), new Link(nodeC, nodeD)});
 
         assertFalse(consistencyValidator.isValid(nodes, links) && subGraphValidator.isValid(nodes, links));
     }
 
 
     @Test
     public void testValidatesFullyConnectedGraph() {
         ConsistencyValidator validator = new ConsistencyValidator();
 
         Node nodeA = new Node();
         Node nodeB = new Node();
         Node nodeC = new Node();
         Node nodeD = new Node();
         Collection<Node> nodes = Arrays.asList(new Node[]{nodeA, nodeB, nodeC, nodeD});
         Collection<Link> links = Arrays.asList(new Link[]{
                 new Link(nodeA, nodeB), new Link(nodeA, nodeC), new Link(nodeA, nodeD),
                 new Link(nodeB, nodeC), new Link(nodeB, nodeD), new Link(nodeC, nodeD)
         });
 
         assertTrue(consistencyValidator.isValid(nodes, links) && subGraphValidator.isValid(nodes, links));
     }
 }
