 /**
  * The owner of the original code is SURFnet BV.
  *
  * Portions created by the original owner are Copyright (C) 2011-2012 the
  * original owner. All Rights Reserved.
  *
  * Portions created by other contributors are Copyright (C) the contributor.
  * All Rights Reserved.
  *
  * Contributor(s):
  *   (Contributors insert name & email here)
  *
  * This file is part of the SURFnet7 Bandwidth on Demand software.
  *
  * The SURFnet7 Bandwidth on Demand software is free software: you can
  * redistribute it and/or modify it under the terms of the BSD license
  * included with this distribution.
  *
  * If the BSD license cannot be found with this distribution, it is available
  * at the following location <http://www.opensource.org/licenses/BSD-3-Clause>
  */
 package nl.surfnet.bod.mtosi;
 
 import static org.hamcrest.MatcherAssert.*;
 import static org.hamcrest.Matchers.*;
 
 import java.io.IOException;
 import java.util.List;
 
 import javax.annotation.Resource;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.test.context.transaction.TransactionConfiguration;
 
 import nl.surfnet.bod.domain.PhysicalPort;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = { "/spring/appCtx.xml", "/spring/appCtx-jpa-integration.xml",
     "/spring/appCtx-nbi-client.xml", "/spring/appCtx-idd-client.xml" })
 @TransactionConfiguration(defaultRollback = true, transactionManager = "transactionManager")
 public class MtosiLiveClientTestIntegration extends AbstractTransactionalJUnit4SpringContextTests {
 
   @Resource(name = "mtosiLiveClient")
   private MtosiLiveClient mtosiLiveClient;
 
   @Before
   public void setup() throws IOException {
   }
 
   @Test
   public void getUnallocatedPorts() {
     final List<PhysicalPort> unallocatedPorts = mtosiLiveClient.getUnallocatedPorts();
     assertThat(unallocatedPorts, hasSize(greaterThan(0)));
     final PhysicalPort firstPhysicalPort = unallocatedPorts.get(0);
 
     // It's always /rack=1/shelf=1 for every NE so we can use 1-1 safely
     assertThat(firstPhysicalPort.getBodPortId(), containsString("1-1"));
    assertThat(firstPhysicalPort.getBodPortId(), containsString("1-1"));
     assertThat(firstPhysicalPort.getNmsPortId(), containsString("1-1"));
     assertThat(firstPhysicalPort.getNmsPortSpeed(), notNullValue());
     assertThat(firstPhysicalPort.getNmsSapName(), startsWith("SAP-"));
     assertThat(firstPhysicalPort.isAlignedWithNMS(), is(true));
   }
 
   @Test
   public void getUnallocatedPortsCount() {
     assertThat(mtosiLiveClient.getUnallocatedMtosiPortCount(), greaterThan(0));
   }
 
 }
