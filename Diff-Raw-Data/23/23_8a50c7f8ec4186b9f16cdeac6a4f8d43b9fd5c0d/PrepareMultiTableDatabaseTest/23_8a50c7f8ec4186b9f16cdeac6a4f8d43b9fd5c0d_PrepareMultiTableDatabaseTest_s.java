 /*
  * Moresby Coffee Bean
  *
  * Copyright (c) 2012, Barnabas Sudy (barnabas.sudy@gmail.com)
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *
  * 1. Redistributions of source code must retain the above copyright notice, this
  *    list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright notice,
  *    this list of conditions and the following disclaimer in the documentation
  *    and/or other materials provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
  * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * The views and conclusions contained in the software and documentation are those
  * of the authors and should not be interpreted as representing official policies,
  * either expressed or implied, of the FreeBSD Project.
  */
 package com.moresby.jettyspring.fourth;
 
 import static org.hamcrest.Matchers.*;
 
 import java.io.IOException;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.EntityTransaction;
 
 import org.apache.log4j.Logger;
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.mortbay.jetty.Server;
 import org.springframework.context.annotation.ComponentScan;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.transaction.annotation.EnableTransactionManagement;
 import org.springframework.web.servlet.config.annotation.EnableWebMvc;
 
 import com.jayway.jsonassert.JsonAssert;
 
 import com.moresby.jettyspring.EntityManagerFactoryUtil;
 import com.moresby.jettyspring.JettyRunner;
 import com.moresby.jettyspring.RestTestUtil;
 import com.moresby.jettyspring.fourth.domain.Line;
 import com.moresby.jettyspring.fourth.domain.Station;
 import com.moresby.jettyspring.fourth.domain.Zone;
 import com.moresby.jettyspring.fourth.spring.FourthTestConfiguration;
 import com.moresby.jettyspring.fourth.spring.FourthTestRestController;
 
 /**
  * TODO javadoc.
  *
  * @author Barnabas Sudy (barnabas.sudy@gmail.com)
  * @since 2012
  */
 public class PrepareMultiTableDatabaseTest {
 
 
     /** Logger. */
     private static final Logger LOG = Logger.getLogger(PrepareMultiTableDatabaseTest.class);
 
     private static EntityManagerFactory emf;
 
     /**
      * Test configuration. This class is overdefining the {@link FourthTestConfiguration}'s original
      * {@link EntityManagerFactory}. Therefore the test will be able to use the a generated memory based
      * persistence unit instead of the a real one.
      *
      * @author Barnabas Sudy (barnabas.sudy@gmail.com)
      * @since 2012
      */
     @Configuration
     @EnableWebMvc
     @ComponentScan(basePackageClasses = FourthTestRestController.class, basePackages = {"com.moresby.jettyspring.fourth.beans", "com.moresby.jettyspring.fourth.dal" })
     @EnableTransactionManagement
     public static class TestConfiguration extends FourthTestConfiguration {
 
         /** {@inheritDoc} */
         @Override
         public EntityManagerFactory entityManagerFactory() {
             return emf;
         }
 
     }
 
     /** The static variable for the Jetty server. */
     private static Server jettyServer = null;
 
     /**
      * Starts the Jetty and deploys the application.<br>
      * <strong>WARNING:</strong> All the tests of the class will use the same Jetty and application so
      * the test should not modify the state of the application.<br>
      *
      * @see BeforeClass
      * @throws Exception If error occurs during the jetty start or application deployment.
      */
     @BeforeClass
     public static void startJetty() throws Exception {
 
 
         emf = EntityManagerFactoryUtil.createH2MemoryEntityManager("perpMultiTableTest", "com.moresby.jettyspring.fourth.domain");
 
         final EntityManager entityManager = emf.createEntityManager();
 
         final EntityTransaction transaction = entityManager.getTransaction();
         transaction.begin();
 
         /* Generate Zones */
         final Zone zone1 = new Zone(Integer.valueOf(1));
         entityManager.persist(zone1);
 
         final Zone zone2 = new Zone(Integer.valueOf(2));
         entityManager.persist(zone2);
 
         /* Generate Lines */
         final Line jubilee = new Line("Jubilee");
         entityManager.persist(jubilee);
 
         final Line northern = new Line("Northern");
         entityManager.persist(northern);
 
         /* Generate London Bridge station. */
         final Station londonBridge = new Station("London Bridge", zone1);
         londonBridge.getLines().add(jubilee);
         londonBridge.getLines().add(northern);
         entityManager.persist(londonBridge);
 
         zone1.getStations().add(londonBridge);
         jubilee.getStations().add(londonBridge);
         northern.getStations().add(londonBridge);
 
         transaction.commit();
 
 
         jettyServer = JettyRunner.startJetty(TestConfiguration.class);
     }
 
     /**
      * Stops the Jetty server and closes the {@link EntityManagerFactory} if it is still open.
      *
      * @see AfterClass
      */
     @AfterClass
     public static void stopJetty() {
         JettyRunner.stopJetty(jettyServer);
         if (emf != null && emf.isOpen()) {
             emf.close();
         }
     }
 
     /**
      * Tests the {@link com.moresby.jettyspring.fourth.spring.FourthTestRestController#listStations() FourthTestRestController#listStations()} RESTful WS Service point.
      *
      * @throws IOException If communication error occurs
      */
     @Test
     public final void listStationTest() throws IOException {
         final String result = RestTestUtil.doGet("/listStations");
 
         LOG.info("Result:   " + result);
 
        JsonAssert.with(result).assertThat("$..name", hasItems("London Bridge"));
     }
 
 }
