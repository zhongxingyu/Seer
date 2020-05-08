 /*
  * Copyright (C) 2010 Evgeny Mandrikov
  *
  * Sonar-IDE is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3 of the License, or (at your option) any later version.
  *
  * Sonar-IDE is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with Sonar-IDE; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
  */
 
 package org.sonar.ide.client;
 
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.sonar.ide.test.SonarTestServer;
 
import static org.hamcrest.Matchers.is;
 
 /**
  * @author Evgeny Mandrikov
  */
 public class SonarClientTest {
   private SonarTestServer testServer;
   private SonarClient client;
 
   @Before
   public void setUp() throws Exception {
     testServer = new SonarTestServer();
     testServer.start();
 
     client = new SonarClient(testServer.getBaseUrl());
   }
 
   @After
   public void tearDown() throws Exception {
     testServer.stop();
   }
 
   @Test
   public void testCheckVersion() throws Exception {
     Assert.assertThat(client.isAvailable(), is(true));
   }
 }
