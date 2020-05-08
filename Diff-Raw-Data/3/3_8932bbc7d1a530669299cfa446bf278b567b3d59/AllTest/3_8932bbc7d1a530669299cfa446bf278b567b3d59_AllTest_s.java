 /* AWE - Amanzi Wireless Explorer
  * http://awe.amanzi.org
  * (C) 2008-2009, AmanziTel AB
  *
  * This library is provided under the terms of the Eclipse Public License
  * as described at http://www.eclipse.org/legal/epl-v10.html. Any use,
  * reproduction or distribution of the library constitutes recipient's
  * acceptance of this agreement.
  *
  * This library is distributed WITHOUT ANY WARRANTY; without even the
  * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  */
 
 package org.amanzi.testing.sanity;
 
 import java.net.URL;
 import java.net.URLClassLoader;
 
 import junit.framework.JUnit4TestAdapter;
 import junit.framework.Test;
 import junit.framework.TestSuite;
 
 import org.junit.runner.RunWith;
 import org.junit.runners.Suite;
 import org.junit.runners.Suite.SuiteClasses;
 
 /**
  * Suite that includes all test classes that are to be run. 
  * Uses customSuite which filters the test cases based on annotations
  *
  *
  *@author Rahul Jain
  */
 
 @RunWith(CustomSuite.class)
 //Generate class array here.
 //TODO: The array should be generated automatically by searching the classpath
 //Hard coded as of now
 @SuiteClasses( {org.amanzi.splash.testing.SpreadsheetServiceCellMovingTest.class,
 				org.amanzi.awe.views.reuse.testing.ReuseAnalyserTest.class,
 				org.amanzi.neo.loader.testing.EriccsonTopologyTest.class,
 				org.amanzi.neo.loader.testing.NeighbourLoaderTest.class,
 				org.amanzi.neo.loader.testing.NokiaTopologyLoaderTest.class,
 				org.amanzi.neo.loader.testing.RomesLoaderTest.class,
				org.amanzi.neo.loader.testing.TemsLoaderTest.class,
				org.amanzi.awe.afp.testing.exporters.ExporterTesting.class} )
 // Note that Categories is a kind of Suite
 public class AllTest {
 	
 }
