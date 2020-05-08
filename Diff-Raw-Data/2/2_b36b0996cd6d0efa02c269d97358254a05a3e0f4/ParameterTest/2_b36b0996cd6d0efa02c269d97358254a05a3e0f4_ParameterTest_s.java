 /***************************************************************
  *  This file is part of the [fleXive](R) project.
  *
  *  Copyright (c) 1999-2008
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU General Public
  *  License as published by the Free Software Foundation;
  *  either version 2 of the License, or (at your option) any
  *  later version.
  *
  *  The GNU General Public License can be found at
  *  http://www.gnu.org/copyleft/gpl.html.
  *  A copy is found in the textfile GPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 package com.flexive.tests.shared;
 
 import com.flexive.shared.configuration.ParameterPath;
 import com.flexive.shared.configuration.ParameterScope;
 import com.flexive.shared.configuration.SystemParameterPaths;
 import org.testng.annotations.Test;
 
 import java.util.List;
 
 /**
  * com.flexive.shared.configuration.Parameter and related unit tests.
  *
  * @author Daniel Lichtenberger (daniel.lichtenberger@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 @Test(groups = {"shared", "configuration"})
 public class ParameterTest {
 
     /**
      * Test parameter scope fallbacks.
      *
      * @throws Exception if an error occured
      */
     @Test
     public void fallbackGlobal() throws Exception {
         checkFallbacks(SystemParameterPaths.TEST_GLOBAL);
     }
 
     /**
      * Test parameter scope fallbacks.
      *
      * @throws Exception if an error occured
      */
     @Test
     public void fallbackDivision() throws Exception {
         checkFallbacks(SystemParameterPaths.TEST_DIVISION, ParameterScope.GLOBAL);
     }
 
     /**
      * Test parameter scope fallbacks.
      *
      * @throws Exception if an error occured
      */
     @Test
     public void fallbackDivisionOnly() throws Exception {
         checkFallbacks(SystemParameterPaths.TEST_DIVISION_ONLY);
     }
 
     /**
      * Test parameter scope fallbacks.
      *
      * @throws Exception if an error occured
      */
     @Test
     public void fallbackUser() throws Exception {
        checkFallbacks(SystemParameterPaths.TEST_USER, ParameterScope.DIVISION);
     }
 
     /**
      * Test parameter scope fallbacks.
      *
      * @throws Exception if an error occured
      */
     @Test
     public void fallbackUserOnly() throws Exception {
         checkFallbacks(SystemParameterPaths.TEST_USER_ONLY);
     }
 
 
     /**
      * Helper method to check expected parameter scope fallbacks.
      *
      * @param path     paramaterpath to be checked
      * @param expected expected fallback values
      */
     private void checkFallbacks(ParameterPath path, ParameterScope... expected) {
         List<ParameterScope> fallbacks = path.getScope().getFallbacks();
         assert expected.length == fallbacks.size() : "Number of fallback scopes mismatch.";
         for (ParameterScope scope : expected) {
             if (fallbacks.indexOf(scope) == -1) {
                 assert false : "Expected fallback scope not found: " + scope;
             }
         }
     }
 }
