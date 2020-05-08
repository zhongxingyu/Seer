 /*
                       Project tests
 
 Copyright (C) 2003  Jose San Leandro Armend?riz
 jsanleandro@yahoo.es
 chousz@yahoo.com
 
 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public
 License as published by the Free Software Foundation; either
 version 2 of the License, or (at your option) any later version.
 
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 General Public License for more details.
 
 You should have received a copy of the GNU General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 
 Thanks to ACM S.L. for distributing this library under the GPL license.
 Contact info: jsr000@terra.es
 Postal Address: c/Playa de Lagoa, 1
 Urb. Valdecaba?as
 Boadilla del monte
 28660 Madrid
 Spain
 
 ******************************************************************************
 *
 * Filename: $RCSfile$
 *
 * Author: Jose San Leandro Armend?riz
 *
 * Description: Executes all tests defined for package
 *              unittests.org.acmsl.commons.utils.
 *
 * Last modified by: $Author$ at $Date$
 *
 * File version: $Revision$
 *
 * Project version: $Name$
 *
 * $Id$
 */
 package unittests.org.acmsl.commons.utils;
 
 /*
 * Importing project classes.
 */
 // JUnitDoclet begin import
 import org.acmsl.commons.utils.EnglishGrammarUtils;
 // JUnitDoclet end import
 
 /*
 * Importing JUnit classes.
 */
 import junit.framework.TestCase;
 
 /*
 This file is part of  JUnitDoclet, a project to generate basic
 test cases  from source code and  helping to keep them in sync
 during refactoring.
 
 Copyright (C) 2002  ObjectFab GmbH  (http://www.objectfab.de/)
 
 This library is  free software; you can redistribute it and/or
 modify  it under the  terms of  the  GNU Lesser General Public
 License as published  by the  Free Software Foundation; either
 version 2.1  of the  License, or  (at your option)  any  later
 version.
 
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or  FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Lesser General Public License for more details.
 
 You  should  have  received a  copy of the  GNU Lesser General
 Public License along with this  library; if not, write  to the
 Free  Software  Foundation, Inc.,  59 Temple Place,  Suite 330,
 Boston, MA  02111-1307  USA
 */
 
 
 /**
 * Tests EnglishGrammarUtilsTest class.
 * @version $Revision$
 * @see org.acmsl.commons.utils.EnglishGrammarUtils
 */
 public class EnglishGrammarUtilsTest
 // JUnitDoclet begin extends_implements
 extends TestCase
 // JUnitDoclet end extends_implements
 {
   // JUnitDoclet begin class
   org.acmsl.commons.utils.EnglishGrammarUtils englishgrammarutils = null;
   // JUnitDoclet end class
   
   /**
   * Creates a EnglishGrammarUtilsTest with given name.
   * @param name such name.
   */
   public EnglishGrammarUtilsTest(String name)
   {
     // JUnitDoclet begin method EnglishGrammarUtilsTest
     super(name);
     // JUnitDoclet end method EnglishGrammarUtilsTest
   }
   
   /**
   * Creates an instance of the tested class.
   * @return such instance.
   
   */
   public org.acmsl.commons.utils.EnglishGrammarUtils createInstance()
   throws Exception
   {
     // JUnitDoclet begin method testcase.createInstance
     return org.acmsl.commons.utils.EnglishGrammarUtils.getInstance();
     // JUnitDoclet end method testcase.createInstance
   }
   
   /**
   * Performs any required steps before each test.
   * @throws Exception if an unexpected situation occurs.
   */
   protected void setUp()
   throws Exception
   {
     // JUnitDoclet begin method testcase.setUp
     super.setUp();
     englishgrammarutils = createInstance();
     // JUnitDoclet end method testcase.setUp
   }
   
   /**
   * Performs any required steps after each test.
   * @throws Exception if an unexpected situation occurs.
   */
   protected void tearDown()
   throws Exception
   {
     // JUnitDoclet begin method testcase.tearDown
     englishgrammarutils = null;
     super.tearDown();
     // JUnitDoclet end method testcase.tearDown
   }
   
   /**
   * Tests EnglishGrammarUtilsTestgetInstance()
   * @throws Exception if an unexpected situation occurs.
   * @see org.acmsl.commons.utils.EnglishGrammarUtils#getInstance()
   */
   public void testGetInstance()
   throws Exception
   {
     // JUnitDoclet begin method getInstance
         EnglishGrammarUtils t_EnglishGrammarUtils = EnglishGrammarUtils.getInstance();
 
         assertNotNull(t_EnglishGrammarUtils);
     // JUnitDoclet end method getInstance
   }
   
   /**
   * Tests EnglishGrammarUtilsTestgetPlural()
   * @throws Exception if an unexpected situation occurs.
   * @see org.acmsl.commons.utils.EnglishGrammarUtils#getPlural(java.lang.String)
   */
   public void testGetPlural()
   throws Exception
   {
     // JUnitDoclet begin method getPlural
         EnglishGrammarUtils t_EnglishGrammarUtils = EnglishGrammarUtils.getInstance();
 
         assertNotNull(t_EnglishGrammarUtils);
 
         assertEquals(t_EnglishGrammarUtils.getPlural("car"), "cars");
         assertEquals(t_EnglishGrammarUtils.getPlural("CAR"), "CARS");
         assertEquals(t_EnglishGrammarUtils.getPlural("man"), "men");
         assertEquals(t_EnglishGrammarUtils.getPlural("MAN"), "MEN");
         assertEquals(t_EnglishGrammarUtils.getPlural("men"), "men");
         assertEquals(t_EnglishGrammarUtils.getPlural("MEN"), "MEN");
         assertEquals(t_EnglishGrammarUtils.getPlural("woman"), "women");
         assertEquals(t_EnglishGrammarUtils.getPlural("WOMAN"), "WOMEN");
         assertEquals(t_EnglishGrammarUtils.getPlural("women"), "women");
         assertEquals(t_EnglishGrammarUtils.getPlural("WOMEN"), "WOMEN");
         assertEquals(t_EnglishGrammarUtils.getPlural("bus"), "buses");
         assertEquals(t_EnglishGrammarUtils.getPlural("BUS"), "BUSES");
         assertEquals(t_EnglishGrammarUtils.getPlural("peach"), "peaches");
         assertEquals(t_EnglishGrammarUtils.getPlural("PEACH"), "PEACHES");
         assertEquals(t_EnglishGrammarUtils.getPlural("dish"), "dishes");
         assertEquals(t_EnglishGrammarUtils.getPlural("DISH"), "DISHES");
         assertEquals(t_EnglishGrammarUtils.getPlural("customer"), "customers");
         assertEquals(t_EnglishGrammarUtils.getPlural("CUSTOMER"), "CUSTOMERS");
        assertEquals(t_EnglishGrammarUtils.getPlural("role"), "roles");
        assertEquals(t_EnglishGrammarUtils.getPlural("ROLE"), "ROLES");
     // JUnitDoclet end method getPlural
   }
   
   /**
   * Tests EnglishGrammarUtilsTestgetSingular()
   * @throws Exception if an unexpected situation occurs.
   * @see org.acmsl.commons.utils.EnglishGrammarUtils#getSingular(java.lang.String)
   */
   public void testGetSingular()
   throws Exception
   {
     // JUnitDoclet begin method getSingular
         EnglishGrammarUtils t_EnglishGrammarUtils = EnglishGrammarUtils.getInstance();
 
         assertNotNull(t_EnglishGrammarUtils);
 
         assertEquals(t_EnglishGrammarUtils.getSingular("cars"), "car");
         assertEquals(t_EnglishGrammarUtils.getSingular("CARS"), "CAR");
         assertEquals(t_EnglishGrammarUtils.getSingular("men"), "man");
         assertEquals(t_EnglishGrammarUtils.getSingular("MEN"), "MAN");
         assertEquals(t_EnglishGrammarUtils.getSingular("man"), "man");
         assertEquals(t_EnglishGrammarUtils.getSingular("MAN"), "MAN");
         assertEquals(t_EnglishGrammarUtils.getSingular("women"), "woman");
         assertEquals(t_EnglishGrammarUtils.getSingular("WOMEN"), "WOMAN");
         assertEquals(t_EnglishGrammarUtils.getSingular("woman"), "woman");
         assertEquals(t_EnglishGrammarUtils.getSingular("WOMAN"), "WOMAN");
         assertEquals(t_EnglishGrammarUtils.getSingular("buses"), "bus");
         assertEquals(t_EnglishGrammarUtils.getSingular("BUSES"), "BUS");
         assertEquals(t_EnglishGrammarUtils.getSingular("peaches"), "peach");
         assertEquals(t_EnglishGrammarUtils.getSingular("PEACHES"), "PEACH");
         assertEquals(t_EnglishGrammarUtils.getSingular("dishes"), "dish");
         assertEquals(t_EnglishGrammarUtils.getSingular("DISHES"), "DISH");
         assertEquals(t_EnglishGrammarUtils.getSingular("customers"), "customer");
         assertEquals(t_EnglishGrammarUtils.getSingular("CUSTOMERS"), "CUSTOMER");
        assertEquals(t_EnglishGrammarUtils.getSingular("roles"), "role");
        assertEquals(t_EnglishGrammarUtils.getSingular("ROLES"), "ROLE");
     // JUnitDoclet end method getSingular
   }
   
   
   
   /**
   * JUnitDoclet moves marker to this method, if there is not match
   * for them in the regenerated code and if the marker is not empty.
   * This way, no test gets lost when regenerating after renaming.
   * Method testVault is supposed to be empty.
   * @throws Exception if an unexpected situation occurs.
   */
   public void testVault()
   throws Exception
   {
     // JUnitDoclet begin method testcase.testVault
     // JUnitDoclet end method testcase.testVault
   }
   
   public static void main(String[] args)
   {
     // JUnitDoclet begin method testcase.main
     junit.textui.TestRunner.run(EnglishGrammarUtilsTest.class);
     // JUnitDoclet end method testcase.main
   }
 }
