 /*
  * Created on Nov 23, 2005
  */
 package uk.org.ponder.rsf.test.flow;
 
 import junit.framework.TestCase;
 
 import org.springframework.context.support.FileSystemXmlApplicationContext;
 
 import uk.org.ponder.conversion.SerializationProvider;
 import uk.org.ponder.rsf.components.UIBranchContainer;
 
 public class TestFlowLoader extends TestCase {
   public void testLoadFlow() {
     FileSystemXmlApplicationContext fsxac = new FileSystemXmlApplicationContext(
         "classpath:uk/org/ponder/rsf/test/minicontext.xml");
 
     UIBranchContainer root = (UIBranchContainer) fsxac.getBean("viewtree");
     SerializationProvider xmlp = (SerializationProvider) fsxac.getBean("XMLProvider");
    String tree = xmlp.toString(root);
    assertNotNull(tree);
   }
   
   
 }
