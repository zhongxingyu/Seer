 /*
  * SoCoFo - Another source code formatter
  * Copyright (C) 2012  Dirk Strauss
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 /**
  * 
  */
 package com.googlecode.socofo.runtime.impl;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.io.File;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.List;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import com.googlecode.socofo.common.api.MainRuntime;
 import com.googlecode.socofo.core.api.Scheduler;
 import com.googlecode.socofo.core.api.SourceTypes;
 
 /**
  * Some tests.
  * 
  * @author dstrauss
  * @version 0.1
  */
 public class DefRuntimeTest {
     /**
      * The test object.
      */
     private DefRuntime to = null;
     
     /**
      * Actions to perform before any test method.
      * 
      * @throws java.lang.Exception
      */
     @Before
     public void setUp() throws Exception {
         to = new DefRuntime();
         Injector ij = Guice.createInjector(new RuntimeInjectionPlan());
         to.setTestScheduler(ij.getInstance(Scheduler.class));
     }
     
     /**
      * Test method for
      * {@link com.googlecode.socofo.runtime.impl.DefRuntime#execute()}.
      */
     @Test
     public final void testExecute() {
         final File rulesUrlFile = new File("src/test/resources/ruleset.xml");
         try {
             final URL rulesUrl = rulesUrlFile.toURI().toURL();
             to.parseParams(MainRuntime.PARAM_BASEDIR + "=src/test/resources",
                 MainRuntime.PARAM_RULESURL + "=" + rulesUrl.toString(),
                 MainRuntime.PARAM_TARGETDIR + "=target/result",
                 MainRuntime.PARAM_TYPES + "=xml");
             final int rc = to.execute();
             assertEquals(MainRuntime.RC_SUCCESS, rc);
         } catch (final MalformedURLException e) {
             fail(e.getLocalizedMessage());
         }
     }
     
     /**
      * Another test case.
      */
     @Test
     public final void testExecute2() {
         final File rulesUrlFile = new File("src/test/resources/ruleset.xml");
         try {
             final URL rulesUrl = rulesUrlFile.toURI().toURL();
             to.parseParams(MainRuntime.PARAM_BASEDIR
                 + "=src/test/resources/single1", MainRuntime.PARAM_RULESURL
                 + "=" + rulesUrl.toString(), MainRuntime.PARAM_TARGETDIR
                 + "=target/result", MainRuntime.PARAM_TYPES + "=xml");
             final int rc = to.execute();
             assertEquals(MainRuntime.RC_SUCCESS, rc);
         } catch (final MalformedURLException e) {
             fail(e.getLocalizedMessage());
         }
     }
     
     @Test
     public final void testExecuteComplex1() {
         File rulesUrlFile = new File("src/test/resources/ruleset.xml");
         try {
             URL rulesUrl = rulesUrlFile.toURI().toURL();
             to.parseParams(MainRuntime.PARAM_BASEDIR
                 + "=src/test/resources/complex1", MainRuntime.PARAM_RULESURL
                 + "=" + rulesUrl.toString(), MainRuntime.PARAM_TARGETDIR
                 + "=target/result/complex1", MainRuntime.PARAM_TYPES + "=xml");
             int rc = to.execute();
             assertEquals(MainRuntime.RC_SUCCESS, rc);
         } catch (MalformedURLException e) {
         }
     }
     
     @Test
     public final void testExecuteTxt1() {
         File rulesUrlFile = new File("src/test/resources/ruleset.xml");
         try {
             URL rulesUrl = rulesUrlFile.toURI().toURL();
             to.parseParams(MainRuntime.PARAM_BASEDIR
                 + "=src/test/resources/txt1", MainRuntime.PARAM_RULESURL + "="
                 + rulesUrl.toString(), MainRuntime.PARAM_TARGETDIR
                 + "=target/result/txt1", MainRuntime.PARAM_TYPES + "=xml");
             int rc = to.execute();
             assertEquals(MainRuntime.RC_SUCCESS, rc);
         } catch (MalformedURLException e) {
         }
     }
     
     /**
      * Test method for
      * {@link com.googlecode.socofo.runtime.impl.DefRuntime#getBaseDirectory()}.
      */
     @Test
     public final void testGetBaseDirectory() {
         File base = to.getBaseDirectory();
         assertNotNull(base);
         to.resetSettings();
         to.parseParams(MainRuntime.PARAM_BASEDIR + "=src");
         base = to.getBaseDirectory();
         assertTrue(base.getAbsolutePath().endsWith("src"));
     }
     
     /**
      * Test method for
      * {@link com.googlecode.socofo.runtime.impl.DefRuntime#getTargetDirectory()}
      * .
      */
     @Test
     public final void testGetTargetDirectory() {
         assertNull(to.getTargetDirectory());
         to.parseParams(MainRuntime.PARAM_TARGETDIR + "=delme");
         assertNotNull(to.getTargetDirectory());
         assertTrue(to.getTargetDirectory().getAbsolutePath().endsWith("delme"));
     }
     
     /**
      * Test method for
      * {@link com.googlecode.socofo.runtime.impl.DefRuntime#parseParams(java.lang.String[])}
      * .
      */
     @Test
     public final void testParseParams() {
         to.parseParams(null);
         to.parseParams();
         to.parseParams(MainRuntime.PARAM_TYPES + "=xml");
         List<SourceTypes> types = to.getTypes();
         assertEquals(1, types.size());
         assertEquals(SourceTypes.XML, types.get(0));
     }
     
     /**
      * Test method for
      * {@link com.googlecode.socofo.runtime.impl.DefRuntime#showHelpScreen()}.
      */
     @Test
     public final void testShowHelpScreen() {
         boolean showHelp = to.showHelpScreen();
         assertFalse(showHelp);
         to.parseParams(MainRuntime.PARAM_HELP);
         assertTrue(to.showHelpScreen());
     }
     
 }
