 /***************************************************************
  *  This file is part of the [fleXive](R) backend application.
  *
  *  Copyright (c) 1999-2010
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) backend application is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU General Public
  *  License as published by the Free Software Foundation;
  *  either version 2 of the License, or (at your option) any
  *  later version.
  *
  *  The GNU General Public License can be found at
  *  http://www.gnu.org/licenses/gpl.html.
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
 package com.flexive.tests.embedded.jsf;
 
import com.flexive.faces.components.input.FxValueInput;
 import com.flexive.faces.converter.EnumConverter;
 import com.flexive.shared.search.query.PropertyValueComparator;
 import org.testng.annotations.Test;
 import org.testng.Assert;
 
 import javax.faces.context.FacesContext;
 
 /**
  * Tests for the flexive enum converter.
  *
  * @author Daniel Lichtenberger (daniel.lichtenberger@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @version $Rev$
  */
 @Test(groups = "jsf")
 public class EnumConverterTest {
     @Test
     public void simpleEnumConvert() {
         testEnumConversion(PropertyValueComparator.EQ);
     }
 
     @Test
     public void innerEnumConvert() {
         // tests the conversion of an enum implemented within its own inner class
         testEnumConversion(PropertyValueComparator.EMPTY);
     }
 
     private void testEnumConversion(PropertyValueComparator comparator) {
        final String value = new EnumConverter().getAsString(FacesContext.getCurrentInstance(), new FxValueInput(), comparator);
        Assert.assertTrue(new EnumConverter().getAsObject(FacesContext.getCurrentInstance(), new FxValueInput(), value).equals(comparator));
     }
 }
