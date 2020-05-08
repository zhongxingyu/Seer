 package nl.surfnet.bod;
 
 import nl.surfnet.bod.support.TestExternalSupport;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class SearchAndSortTestSelenium extends TestExternalSupport {
 
   private static final String GROUP_NAME = "SIDN";
 
   @Before
   public void setup() {
     getNocDriver().createNewPhysicalResourceGroup(GROUP_NAME, ICT_MANAGERS_GROUP, "test@example.com");
   }
 
   @Test
   public void verifySearch() {
     getNocDriver().addPhysicalPortToInstitute(GROUP_NAME, "NOC 1 label", "Mock_Poort 1de verdieping toren1a");
     getNocDriver().addPhysicalPortToInstitute(GROUP_NAME, "NOC 2 label", "Mock_Poort 2de verdieping toren1b");
     getNocDriver().addPhysicalPortToInstitute(GROUP_NAME, "NOC 3 label", "Mock_Poort 3de verdieping toren1c");
 
     getNocDriver().verifyAllocatedPortsBySearch("1", BOD_PORT_ID_1, BOD_PORT_ID_2, BOD_PORT_ID_4);
     getNocDriver().verifyAllocatedPortsBySearch("*1*", BOD_PORT_ID_1, BOD_PORT_ID_2, BOD_PORT_ID_4);
 
     getNocDriver().verifyAllocatedPortsBySearch("'NOC 1 label'", BOD_PORT_ID_1);
     getNocDriver().verifyAllocatedPortsBySearch("'NOC 1'", new String[] {});
     getNocDriver().verifyAllocatedPortsBySearch("'NOC ? label'", BOD_PORT_ID_1, BOD_PORT_ID_2, BOD_PORT_ID_4);
   }
 
   @Test
   public void verifySort() {
     setupSortData();
 
    getNocDriver().verifyAllocatedPortsBySort("nocLabel", "123NOC", "987NOC", "AbcNOC", "abcNOC");
   }
 
   @Test
   public void verifySearchAndSort() {
     setupSortData();
 
    getNocDriver().verifyAllocatedPortsBySearchAndSort("abc", "bodPortId", BOD_PORT_ID_4, BOD_PORT_ID_3);
 
     getNocDriver().verifyAllocatedPortsBySearchAndSort("ETH10G", "nocLabel", "123NOC", "987NOC");
   }
 
   private void setupSortData() {
     getNocDriver().linkPhysicalPort(NMS_PORT_ID_1, "123NOC", "XYZPort", GROUP_NAME);
     getNocDriver().linkPhysicalPort(NMS_PORT_ID_2, "987NOC", "ABDPort", GROUP_NAME);
     getNocDriver().linkPhysicalPort(NMS_PORT_ID_3, "abcNOC", "abcPort", GROUP_NAME);
    getNocDriver().linkPhysicalPort(NMS_PORT_ID_4, "AbcNOC", "xyzPort", GROUP_NAME);
   }
 
}
