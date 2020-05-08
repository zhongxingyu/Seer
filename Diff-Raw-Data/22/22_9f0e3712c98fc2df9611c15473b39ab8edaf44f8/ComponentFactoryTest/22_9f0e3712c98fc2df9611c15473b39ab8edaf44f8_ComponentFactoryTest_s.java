 /*
  * #%L
  * Bitrepository Integrity Client
  * 
  * $Id$
  * $HeadURL$
  * %%
  * Copyright (C) 2010 - 2011 The State and University Library, The Royal Library and The State Archives, Denmark
  * %%
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as 
  * published by the Free Software Foundation, either version 2.1 of the 
  * License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Lesser Public License for more details.
  * 
  * You should have received a copy of the GNU General Lesser Public 
  * License along with this program.  If not, see
  * <http://www.gnu.org/licenses/lgpl-2.1.html>.
  * #L%
  */
 package org.bitrepository.integrityclient;
 
 import org.bitrepository.integrityclient.cache.MemoryBasedIntegrityCache;
 import org.bitrepository.integrityclient.checking.SystematicIntegrityValidator;
 import org.bitrepository.integrityclient.collector.DelegatingIntegrityInformationCollector;
 import org.bitrepository.integrityclient.scheduler.TimerIntegrityInformationScheduler;
 import org.bitrepository.protocol.IntegrationTest;
 import org.testng.Assert;
 import org.testng.annotations.Test;
 
 /**
  * Simple test case for the component factory.
  */
public class ComponentFactoryTest extends IntegrationTest {

     @Test(groups = {"regressiontest"})
     public void verifyCacheFromFactory() throws Exception {
         Assert.assertTrue(IntegrityServiceComponentFactory.getInstance().getCachedIntegrityInformationStorage()
                 instanceof MemoryBasedIntegrityCache, 
                 "The default Cache should be the '" + MemoryBasedIntegrityCache.class.getName() + "' but was '"
                 + IntegrityServiceComponentFactory.getInstance().getCachedIntegrityInformationStorage().getClass().getName() + "'");
     }
 
     @Test(groups = {"regressiontest"})
     public void verifyCollectorFromFactory() throws Exception {
         Assert.assertTrue(IntegrityServiceComponentFactory.getInstance().getIntegrityInformationCollector(messageBus, settings)
                 instanceof DelegatingIntegrityInformationCollector, 
                 "The default Collector should be the '" + DelegatingIntegrityInformationCollector.class.getName() + "'");
     }
 
     @Test(groups = {"regressiontest"})
     public void verifyIntegrityCheckerFromFactory() throws Exception {
         Assert.assertTrue(IntegrityServiceComponentFactory.getInstance().getIntegrityChecker(settings)
                 instanceof SystematicIntegrityValidator, 
                 "The default IntegrityChecker should be the '" + SystematicIntegrityValidator.class.getName() + "'");
     }
 
     @Test(groups = {"regressiontest"})
     public void verifySchedulerFromFactory() throws Exception {
         Assert.assertTrue(IntegrityServiceComponentFactory.getInstance().getIntegrityInformationScheduler(settings)
                 instanceof TimerIntegrityInformationScheduler, 
                 "The default Scheduler should be the '" + TimerIntegrityInformationScheduler.class.getName() + "'");
     }
 
 }
