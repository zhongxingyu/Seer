 package nl.surfnet.bod.service;
 
 import static junit.framework.Assert.assertNotNull;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.greaterThan;
 import static org.hamcrest.Matchers.hasSize;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 
 import nl.surfnet.bod.domain.PhysicalPort;
import nl.surfnet.bod.nbi.NbiOfflineClient;
 import nl.surfnet.bod.nbi.TerminationPointFactory;
 import nl.surfnet.bod.nbi.generated.TerminationPoint;
 
 import org.hamcrest.Matchers;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.util.CollectionUtils;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = "classpath:/spring/appCtx*.xml")
 public class PhysicalPortServiceNbiImplTest {
 
   private static final String NAME_1 = "NameOne";
   private static final String NAME_2 = "NameTwo";
   private static final String DISPLAY_NAME_1 = "TestDisplayNameOne";
   private static final String DISPLAY_NAME_2 = "TestDisplayNameTwo";
 
   @Autowired
   private PhysicalPortServiceNbiImpl physicalPortServiceNbiImpl;
 
   @Test
   public void testFindAll() {
     List<PhysicalPort> ports = physicalPortServiceNbiImpl.findAll();
 
     assertThat(ports, hasSize(greaterThan(0)));
   }
 
   @Ignore("Paging does not work yet")
   @Test
   public void testFindEntires() {
     // Execute
     List<PhysicalPort> ports = physicalPortServiceNbiImpl.findEntries(0, 10);
 
     // Verify
     assertEquals(10, ports.size());
   }
 
   @Test
   public void testCount() {
     List<PhysicalPort> ports = physicalPortServiceNbiImpl.findAll();
 
     assertEquals(260, ports.size());
     assertEquals(ports.size(), physicalPortServiceNbiImpl.count());
   }
 
   @Test
   public void testDelete() {
 
     PhysicalPort port = new PhysicalPort();
 
     try {
       physicalPortServiceNbiImpl.delete(port);
 
       // verify
       fail("Exception expected");
     }
     catch (UnsupportedOperationException exc) {
       // expected
     }
   }
 
   @Test
   public void testFind() {
 
     try {
       physicalPortServiceNbiImpl.find(1L);
 
       // verify
       fail("Exception expected");
     }
     catch (UnsupportedOperationException exc) {
       // expected
     }
   }
 
   @Test
   public void testFindByName() {
     PhysicalPort port = physicalPortServiceNbiImpl.findByName("00:03:18:bc:76:00_Port5/1_dummy");
 
     assertNotNull(port);
   }
 
   @Test
   public void testFindByNameNotExisting() {
     PhysicalPort port = physicalPortServiceNbiImpl.findByName("fakename");
 
     assertThat(port, Matchers.nullValue());
   }
 
   @Test
   public void testFindByNameNull() {
     PhysicalPort port = physicalPortServiceNbiImpl.findByName(null);
 
     // verify
     assertNull(port);
   }
 
   @Test
   public void testSave() {
     try {
       PhysicalPort port = new PhysicalPort();
       physicalPortServiceNbiImpl.save(port);
 
       // verify
       fail("Exception expected");
     }
     catch (UnsupportedOperationException exc) {
       // expected
     }
   }
 
   @Test
   public void testUpdate() {
     try {
       PhysicalPort port = new PhysicalPort();
       physicalPortServiceNbiImpl.update(port);
 
       // verify
       fail("Exception expected");
     }
     catch (UnsupportedOperationException exc) {
       // expected
     }
   }
 
   @Test
   public void testMapTerminationPointToPhysicalPort() {
     // Setup
     TerminationPoint tp1 = new TerminationPointFactory().create(NAME_1, DISPLAY_NAME_1);
 
     // Execute
     PhysicalPort physicalPort = physicalPortServiceNbiImpl.mapTerminationPointToPhysicalPort(tp1);
 
     // Verify
     assertEquals(NAME_1, physicalPort.getName());
     assertEquals(DISPLAY_NAME_1, physicalPort.getDisplayName());
 
   }
 
   @Test
   public void testMapTerminationPointToPhysicalPortNullArg() {
 
     // Execute
     PhysicalPort physicalPort = physicalPortServiceNbiImpl.mapTerminationPointToPhysicalPort(null);
 
     // Verify
     assertEquals(null, physicalPort.getName());
     assertEquals(null, physicalPort.getDisplayName());
 
   }
 
   @Test
   public void testMapTerminationPointToPhysicalPortEmptyArg() {
     // Setup
     TerminationPoint tp = new TerminationPointFactory().create(null, null);
     // Execute
     PhysicalPort physicalPort = physicalPortServiceNbiImpl.mapTerminationPointToPhysicalPort(tp);
 
     // Verify
     assertEquals(null, physicalPort.getName());
     assertEquals(null, physicalPort.getDisplayName());
 
   }
 
   @Test
   public void testTransform() {
     // Setup
     TerminationPoint tp1 = new TerminationPointFactory().create(NAME_1, DISPLAY_NAME_1);
     TerminationPoint tp2 = new TerminationPointFactory().create(NAME_2, DISPLAY_NAME_2);
     List<TerminationPoint> tpoints = Arrays.asList(tp1, tp2);
 
     // Execute
     Collection<PhysicalPort> transformedPorts = physicalPortServiceNbiImpl.transform(tpoints);
 
     // Verify
     assertEquals("size", 2, transformedPorts.size());
     Iterator<PhysicalPort> iterator = transformedPorts.iterator();
 
     PhysicalPort physicalPort = iterator.next();
     assertEquals("one", NAME_1, physicalPort.getName());
     assertEquals("one", DISPLAY_NAME_1, physicalPort.getDisplayName());
 
     physicalPort = iterator.next();
     assertEquals("two", NAME_2, physicalPort.getName());
     assertEquals("two", DISPLAY_NAME_2, physicalPort.getDisplayName());
   }
 
   @Test
   public void testTransformEmptyArg() {
 
     // Setup
     TerminationPoint tp1 = new TerminationPointFactory().create(null, null);
     List<TerminationPoint> tpoints = Arrays.asList(tp1);
 
     // Execute
     Collection<PhysicalPort> transformedPorts = physicalPortServiceNbiImpl.transform(tpoints);
 
     // Verify
     assertEquals("size", 1, transformedPorts.size());
 
     PhysicalPort physicalPort = transformedPorts.iterator().next();
     assertEquals("name", null, physicalPort.getName());
   }
 
   @Test
   public void testTransformNullArg() {
 
     // Execute
     List<PhysicalPort> ports = physicalPortServiceNbiImpl.transform(null);
 
     // Verify
     assertTrue(CollectionUtils.isEmpty(ports));
   }
 
   @Test
   public void testSelectByPortName() {
 
     TerminationPoint tp1 = new TerminationPointFactory().create(NAME_1, DISPLAY_NAME_1);
     TerminationPoint tp2 = new TerminationPointFactory().create(NAME_2, DISPLAY_NAME_2);
     List<TerminationPoint> tpoints = Arrays.asList(tp1, tp2);
 
     PhysicalPort selectedPort = physicalPortServiceNbiImpl.selectByName(tpoints, NAME_1);
     assertEquals(NAME_1, selectedPort.getName());
     assertEquals(DISPLAY_NAME_1, selectedPort.getDisplayName());
 
   }
 
   @Test
   public void testSelectByPortIdNotFound() {
 
     TerminationPoint tp1 = new TerminationPointFactory().create(NAME_1, DISPLAY_NAME_1);
     List<TerminationPoint> tpoints = Arrays.asList(tp1);
 
     PhysicalPort selectedPort = physicalPortServiceNbiImpl.selectByName(tpoints, NAME_2);
     assertNull(selectedPort);
 
   }
 
   @Test
   public void testSelectByPortIdSearchNullArg() {
 
     TerminationPoint tp1 = new TerminationPointFactory().create(NAME_1, DISPLAY_NAME_1);
     TerminationPoint tp2 = new TerminationPointFactory().create(NAME_2, DISPLAY_NAME_2);
     List<TerminationPoint> tpoints = Arrays.asList(tp1, tp2);
 
     PhysicalPort selectedPort = physicalPortServiceNbiImpl.selectByName(tpoints, null);
     assertNull(selectedPort);
 
   }
 
   @Test
   public void testSelectByPortIdSearchEmptyArg() {
 
     TerminationPoint tp1 = new TerminationPointFactory().create(NAME_1, DISPLAY_NAME_1);
     TerminationPoint tp2 = new TerminationPointFactory().create(NAME_2, DISPLAY_NAME_2);
     List<TerminationPoint> tpoints = Arrays.asList(tp1, tp2);
 
     PhysicalPort selectedPort = physicalPortServiceNbiImpl.selectByName(tpoints, "");
     assertNull(selectedPort);
 
   }
 
   @Test
   public void testSelectByPortIdNullListArg() {
 
     PhysicalPort selectedPort = physicalPortServiceNbiImpl.selectByName(null, NAME_1);
     assertNull(selectedPort);
   }
 
 }
