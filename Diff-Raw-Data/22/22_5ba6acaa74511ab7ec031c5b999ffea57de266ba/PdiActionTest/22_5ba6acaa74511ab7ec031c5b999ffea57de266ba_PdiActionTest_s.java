 /*
  * This program is free software; you can redistribute it and/or modify it under the 
  * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software 
  * Foundation.
  *
  * You should have received a copy of the GNU Lesser General Public License along with this 
  * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html 
  * or from the Free Software Foundation, Inc., 
  * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
  * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU Lesser General Public License for more details.
  *
  * Copyright 2006 - 2012 Pentaho Corporation.  All rights reserved.
  *
  */
 package org.pentaho.platform.plugin.kettle;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.io.File;
 import java.io.Serializable;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.io.FileUtils;
 import org.junit.Before;
 import org.junit.Test;
 import org.pentaho.commons.connection.IPentahoResultSet;
 import org.pentaho.commons.connection.memory.MemoryMetaData;
 import org.pentaho.commons.connection.memory.MemoryResultSet;
 import org.pentaho.platform.api.engine.ActionExecutionException;
 import org.pentaho.platform.api.engine.IPentahoSession;
 import org.pentaho.platform.api.engine.IUserRoleListService;
 import org.pentaho.platform.api.repository.ISolutionRepository;
 import org.pentaho.platform.api.scheduler2.JobTrigger;
 import org.pentaho.platform.api.scheduler2.SchedulerException;
 import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
 import org.pentaho.platform.engine.core.system.StandaloneSession;
 import org.pentaho.platform.engine.core.system.boot.PlatformInitializationException;
 import org.pentaho.platform.engine.security.SecurityHelper;
 import org.pentaho.platform.repository.solution.filebased.FileBasedSolutionRepository;
 import org.pentaho.platform.scheduler2.quartz.QuartzScheduler;
 import org.pentaho.test.platform.engine.core.MicroPlatform;
 
 @SuppressWarnings( { "all" })
 public class PdiActionTest {
 
   private QuartzScheduler scheduler;
 
   public static final String SESSION_PRINCIPAL = "SECURITY_PRINCIPAL";
 
   private String TEST_USER = "TestUser";
 
   private HashMap<String, Serializable> jobParams;
 
   @Before
   public void init() throws SchedulerException, PlatformInitializationException {
     System.setProperty("java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory"); //$NON-NLS-1$ //$NON-NLS-2$
     System.setProperty("org.osjava.sj.root", "test-src/simple-jndi"); //$NON-NLS-1$ //$NON-NLS-2$
     System.setProperty("org.osjava.sj.delimiter", "/"); //$NON-NLS-1$ //$NON-NLS-2$
 
     IPentahoSession session = new StandaloneSession();
     PentahoSessionHolder.setSession(session);
     
     scheduler = new QuartzScheduler();
     scheduler.start();
 
     MicroPlatform mp = new MicroPlatform("test-src/solution");
     mp.define(IUserRoleListService.class, StubUserRoleListService.class);
     mp.define(ISolutionRepository.class, FileBasedSolutionRepository.class);
     mp.start();
 
     SecurityHelper.getInstance().becomeUser(TEST_USER);
     jobParams = new HashMap<String, Serializable>();
   }
   
   @Test(expected=Exception.class)
   public void testValidatation() throws Exception {
     PdiAction action = new PdiAction();
     action.execute();
   }
   
   @Test
   public void testTransformationVariableOverrides() throws Exception {
     Map<String, String> variables = new HashMap<String, String>();
     variables.put("customVariable", "customVariableValue");
 
     PdiAction action = new PdiAction();
     action.setArguments(new String[] { "dummyArg" } );
     action.setVariables(variables);
     
     
     Map<String, String> overrideParams = new HashMap<String, String>();
     overrideParams.put("param2", "12");
     action.setParameters(overrideParams);
     
     action.setDirectory("test-src\\solution\\pdi\\");
     action.setTransformation("testTransformationVariableOverrides.ktr");
 
     action.execute();
     
     List lines = FileUtils.readLines(new File("testTransformationVariableOverrides.out.txt"));
     assertTrue("File \"testTransformationVariableOverrides.out.txt\" should not be empty", lines.size() > 0);
     String rowData = (String)lines.get(1);
     //Columns are as follows:
     //generatedRow|cmdLineArg1|param1|param2|repositoryDirectory|customVariable
     String[] columnData = rowData.split("\\|");
     assertEquals("param1 value is wrong (default value should be in effect)", "param1DefaultValue", columnData[2].trim());
     assertEquals("param2 value is wrong (overridden value should be in effect)", 12L, Long.parseLong(columnData[3].trim()));
     
     assertEquals("The number of rows generated should have equaled the value of param2", 13, lines.size());
     
     assertEquals("customVariable value is wrong", "customVariableValue", columnData[5].trim());
   }
   
   @Test
   public void testLoadingFromVariousPaths() throws Exception {
     PdiAction action = new PdiAction();
     action.setArguments(new String[] { "dummyArg" } );
     
     action.setDirectory("test-src\\solution\\pdi\\");
     action.setTransformation("testTransformationVariableOverrides.ktr");
 
     action.execute();
     
     File ktrFile = new File("test-src/solution/pdi/testTransformationVariableOverrides.ktr");
     assertTrue(ktrFile.exists());
     action.setDirectory(ktrFile.getParent());
     action.setTransformation(ktrFile.getName());
 
     action.execute();
   }
   
   @Test(expected=ActionExecutionException.class)
   public void testBadFileThrowsException() throws Exception {
     PdiAction action = new PdiAction();
     action.setArguments(new String[] { "dummyArg" } );
     
     action.setDirectory("/dne");
     action.setTransformation("dne.ktr");
 
     action.execute();
   }
   
   @Test
   public void testTransformationResource() throws Exception {
     Map<String, Object> args = new HashMap<String, Object>();
     args.put("parameterKey", "parameterValue");
 
     PdiAction action = new PdiAction();
     action.setVarArgs(args);
     
     File ktr = new File("test-src/solution/pdi/sample2.ktr");
     assertTrue(ktr.exists());
 
     action.setDirectory(ktr.getParent());
     action.setTransformation(ktr.getName());
 
     action.execute();
 
     String status = action.getStatus();
     assertNotNull(status);
     // assertEquals( Trans.STRING_FINISHED, status );
 
     int result = action.getResult();
     assertEquals(0, result);
 
     assertEquals("", 0, action.getTransformationOutputRowsCount());
     assertEquals("", 0, action.getTransformationOutputErrorRowsCount());
 
     IPentahoResultSet rows = action.getTransformationOutputRows();
     assertNull(rows);
     rows = action.getTransformationOutputErrorRows();
     assertNull(rows);
 
     String log = action.getLog();
     assertTrue(log.indexOf("QUADRANT_ACTUALS") != -1);
     assertTrue(log.indexOf("R=148") != -1);
     assertTrue(log.indexOf("Filter rows") != -1);
     assertTrue(log.indexOf("R=148") != -1);
     assertTrue(log.indexOf("Java Script Value") != -1);
     assertTrue(log.indexOf("W=5") != -1);
     assertTrue(log.indexOf("XML Output") != -1);
     assertTrue(log.indexOf("O=5") != -1);
     assertNotNull(log);
 
   }
 
   @Test
   public void testTransformationPaths() {
     String dir = "test-src\\solution\\pdi\\";
     String ktr = "sample2.ktr";
 
     PdiAction action = new PdiAction();
     action.setDirectory(dir);
     action.setTransformation(ktr);
 
     try {
       action.execute();
     } catch (Exception ex) {
       ex.printStackTrace();
      fail();
     }
 
     String status = action.getStatus();
     assertNotNull(status);
     // assertEquals( Trans.STRING_FINISHED, status );
 
     int result = action.getResult();
     assertEquals(0, result);
 
     assertEquals("", 0, action.getTransformationOutputRowsCount());
     assertEquals("", 0, action.getTransformationOutputErrorRowsCount());
 
     IPentahoResultSet rows = action.getTransformationOutputRows();
     assertNull(rows);
     rows = action.getTransformationOutputErrorRows();
     assertNull(rows);
 
     String log = action.getLog();
     assertTrue(log.indexOf("QUADRANT_ACTUALS") != -1);
     assertTrue(log.indexOf("R=148") != -1);
     assertTrue(log.indexOf("Filter rows") != -1);
     assertTrue(log.indexOf("R=148") != -1);
     assertTrue(log.indexOf("Java Script Value") != -1);
     assertTrue(log.indexOf("W=5") != -1);
     assertTrue(log.indexOf("XML Output") != -1);
     assertTrue(log.indexOf("O=5") != -1);
     assertNotNull(log);
   }
 
   @Test
   public void testJobPaths() throws Exception {
     String dir = "test-src/solution/pdi";
     String kjb = "ETLJob1.kjb";
 
     PdiAction component = new PdiAction();
     component.setDirectory(dir);
     component.setJob(kjb);
 
     component.execute();
     //if no exception then the test passes
   }
 
   @Test
   public void testTransformationMonitor() {
     PdiAction action = new PdiAction();
     
     File ktr = new File("test-src/solution/pdi/sample2.ktr");
     assertTrue(ktr.exists());
     
     action.setDirectory(ktr.getParent());
     action.setTransformation(ktr.getName());
 
     action.setMonitorStep("XML Output");
 
     try {
       action.execute();
     } catch (Exception e) {
       fail(e.getMessage());
     }
 
     String status = action.getStatus();
     assertNotNull(status);
     // assertEquals( "Finished", status );
 
     assertEquals(5, action.getTransformationOutputRowsCount());
     assertEquals(0, action.getTransformationOutputErrorRowsCount());
 
     IPentahoResultSet rows = action.getTransformationOutputRows();
     assertNotNull(rows);
     assertEquals(5, rows.getRowCount());
 
     assertEquals("Central", rows.getValueAt(0, 0));
     assertEquals("Sales", rows.getValueAt(0, 1));
     assertEquals("Account Executive", rows.getValueAt(0, 2));
     assertEquals("Hello, Account Executive", rows.getValueAt(0, 3));
 
     rows = action.getTransformationOutputErrorRows();
     assertNotNull(rows);
 
     String log = action.getLog();
     assertTrue(log.indexOf("QUADRANT_ACTUALS") != -1);
     assertTrue(log.indexOf("R=148") != -1);
     assertTrue(log.indexOf("Filter rows") != -1);
     assertTrue(log.indexOf("R=148") != -1);
     assertTrue(log.indexOf("Java Script Value") != -1);
     assertTrue(log.indexOf("W=5") != -1);
     assertTrue(log.indexOf("XML Output") != -1);
     assertTrue(log.indexOf("O=5") != -1);
     assertNotNull(log);
   }
 
   @Test
   public void testTransformationInjector() throws Exception {
     PdiAction component = new PdiAction();
     IPentahoSession session = new StandaloneSession();
     PentahoSessionHolder.setSession(session);
     
     File ktr = new File("test-src/solution/pdi/sample3.ktr");
     assertTrue(ktr.exists());
 
     component.setDirectory(ktr.getParent());
     component.setTransformation(ktr.getName());
 
     String[][] columnNames = { { "REGION", "DEPARTMENT", "POSITIONTITLE" } };
     MemoryMetaData metadata = new MemoryMetaData(columnNames, null);
 
     MemoryResultSet rowsIn = new MemoryResultSet(metadata);
     rowsIn.addRow(new Object[] { "abc", "123", "bogus" });
     rowsIn.addRow(new Object[] { "region2", "Sales", "bad" });
     rowsIn.addRow(new Object[] { "Central", "Sales", "test title" });
     rowsIn.addRow(new Object[] { "Central", "xyz", "bad" });
 
     component.setInjectorRows(rowsIn);
     component.setInjectorStep("Injector");
     component.setMonitorStep("Output");
 
     component.execute();
 
     String status = component.getStatus();
     assertNotNull(status);
     // assertEquals( "Finished", status );
 
     assertEquals(1, component.getTransformationOutputRowsCount());
     assertEquals(0, component.getTransformationOutputErrorRowsCount());
 
     IPentahoResultSet rows = component.getTransformationOutputRows();
     assertNotNull(rows);
     assertEquals(1, rows.getRowCount());
 
     assertEquals("Central", rows.getValueAt(0, 0));
     assertEquals("Sales", rows.getValueAt(0, 1));
     assertEquals("test title", rows.getValueAt(0, 2));
     assertEquals("Hello, test title", rows.getValueAt(0, 3));
 
     rows = component.getTransformationOutputErrorRows();
     assertNotNull(rows);
 
     String log = component.getLog();
     assertTrue(log.indexOf("Injector") != -1);
     assertTrue(log.indexOf("R=1") != -1);
     assertTrue(log.indexOf("Filter rows") != -1);
     assertTrue(log.indexOf("W=1") != -1);
     assertTrue(log.indexOf("Java Script Value") != -1);
     assertTrue(log.indexOf("W=1") != -1);
     assertTrue(log.indexOf("Output") != -1);
     assertTrue(log.indexOf("W=4") != -1);
     assertNotNull(log);
   }
   
   @Test
   public void testKettleTransformationSchedule() throws SchedulerException, InterruptedException {
     jobParams.put("directory", "test-src\\solution\\pdi\\");
     jobParams.put("transformation", "sample2.ktr");
     scheduler.createJob("testName", PdiAction.class, jobParams, JobTrigger.ONCE_NOW);
     sleep(5);
   }
 
   @Test
   public void testNoSettings() {
     PdiAction component = new PdiAction();
     try {
       component.validate();
       fail();
     } catch (Exception ex) {
     }
   }
 
   @Test
   public void testBadSolutionTransformation() {
     PdiAction component = new PdiAction();
     component.setTransformation("bogus.ktr");
     try {
       component.validate();
       fail();
     } catch (Exception ex) {
     }
   }
 
   @Test
   public void testBadSolutionJob() {
     PdiAction component = new PdiAction();
     component.setJob("bogus.kjb");
     try {
       component.validate();
       fail();
     } catch (Exception ex) {
     }
   }
 
   private void sleep(int seconds) {
     try {
       Thread.sleep(seconds * 1000);
     } catch (InterruptedException e) {
       throw new RuntimeException(e);
     }
   }
 }
