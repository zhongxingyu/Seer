 /*
  * Sonar Mantis Plugin
  * Copyright (C) 2011 Jérémie Lagarde
  * dev@sonar.codehaus.org
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3 of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
  */
 
 package org.sonar.plugins.mantis;
 
 import static org.junit.Assert.fail;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 import java.math.BigInteger;
 import java.net.MalformedURLException;
 import java.rmi.RemoteException;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.commons.configuration.MapConfiguration;
 import org.junit.Before;
 import org.junit.Test;
 import org.sonar.api.batch.SensorContext;
 import org.sonar.api.resources.Project;
 import org.sonar.plugins.mantis.soap.MantisSoapService;
 
 import biz.futureware.mantis.rpc.soap.client.FilterData;
 import biz.futureware.mantis.rpc.soap.client.IssueData;
 import biz.futureware.mantis.rpc.soap.client.MantisConnectLocator;
 import biz.futureware.mantis.rpc.soap.client.MantisConnectPortType;
 import biz.futureware.mantis.rpc.soap.client.ObjectRef;
 
 /**
  * @author Jeremie Lagarde
  * @since 0.1
  */
 public class MantisSensorTest {
 
   private MantisSensor sensor;
 
   @Before
   public void setUp() throws Exception {
     final MantisSoapService service = new MantisSoapService(null) {
 
       @Override
       protected MantisConnectLocator createMantisConnectLocator() {
         MantisConnectLocator locator = mock(MantisConnectLocator.class);
         MantisConnectPortType portType = mock(MantisConnectPortType.class);
         try {
           IssueData[] issues = new IssueData[5];
           issues[0] = new IssueData();
           issues[0].setPriority(new ObjectRef(BigInteger.valueOf(1), "normal"));
           issues[0].setStatus(new ObjectRef(BigInteger.valueOf(1), "assigned"));
          issues[0].setId(BigInteger.ONE);
           issues[1] = new IssueData();
           issues[1].setPriority(new ObjectRef(BigInteger.valueOf(2), "high"));
           issues[1].setStatus(new ObjectRef(BigInteger.valueOf(2), "resolved"));
          issues[1].setId(BigInteger.valueOf(2));
           issues[2] = new IssueData();
           issues[2].setPriority(new ObjectRef(BigInteger.valueOf(1), "normal"));
           issues[2].setStatus(new ObjectRef(BigInteger.valueOf(1), "assigned"));
          issues[2].setId(BigInteger.valueOf(3));
           issues[3] = new IssueData();
           issues[3].setPriority(new ObjectRef(BigInteger.valueOf(1), "normal"));
           issues[3].setStatus(new ObjectRef(BigInteger.valueOf(3), "closed"));
          issues[3].setId(BigInteger.valueOf(4));
           issues[4] = new IssueData();
           issues[4].setPriority(new ObjectRef(BigInteger.valueOf(3), "urgent"));
           issues[4].setStatus(new ObjectRef(BigInteger.valueOf(1), "assigned"));
          issues[4].setId(BigInteger.valueOf(5));
           FilterData filter = new FilterData(BigInteger.ONE, null, BigInteger.ONE, true, "current-version", "",null);
           when(locator.getMantisConnectPort()).thenReturn(portType);
           when(portType.mc_project_get_id_from_name("jer", "pwd", "myproject")).thenReturn(BigInteger.ONE);
           when(portType.mc_filter_get("jer", "pwd", BigInteger.ONE)).thenReturn(new FilterData[] { filter });
           when(portType.mc_filter_get_issues("jer", "pwd", BigInteger.ONE, filter.getId(), BigInteger.ONE, BigInteger.valueOf(100))).thenReturn(
               issues);
         } catch (Exception e) {
           fail();
         }
         return locator;
       }
     };
 
     sensor = new MantisSensor() {
       protected MantisSoapService createMantisSoapService() throws RemoteException {
         return service;
       }
     };
   }
 
 
   @Test
   public void testAnalyse() {
     SensorContext context = mock(SensorContext.class);
     Project project = mock(Project.class);
     Map<String, String> config = new HashMap<String, String>();
     config.put(MantisPlugin.SERVER_URL_PROPERTY, "http://localhost:1234/mantis/");
     config.put(MantisPlugin.USERNAME_PROPERTY, "jer");
     config.put(MantisPlugin.PASSWORD_PROPERTY, "pwd");
     config.put(MantisPlugin.PROJECTNAME_PROPERTY, "myproject");
     config.put(MantisPlugin.FILTER_PROPERTY, "current-version");
     when(project.getConfiguration()).thenReturn(new MapConfiguration(config));
     sensor.analyse(project, context);
   }
 }
