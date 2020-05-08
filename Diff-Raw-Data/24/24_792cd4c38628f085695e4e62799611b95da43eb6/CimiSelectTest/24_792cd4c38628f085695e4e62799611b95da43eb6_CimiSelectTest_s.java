 /**
  *
  * SIROCCO
  * Copyright (C) 2011 France Telecom
  * Contact: sirocco@ow2.org
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
  * USA
  *
  * $Id$
  *
  */
 package org.ow2.sirocco.apis.rest.cimi.request;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.junit.Assert;
 import org.junit.Test;
 import org.ow2.sirocco.cloudmanager.model.cimi.MachineConfiguration;
 import org.ow2.sirocco.cloudmanager.model.cimi.MachineImage;
 
 public class CimiSelectTest {
 
     /**
      * Test for {@link CimiSelect#splitByComma(List)}.
      */
     @Test
     public void testSplitByComma() {
         List<String> results;
 
         List<String> selects = new ArrayList<String>();
         selects.add("one,two,three");
         selects.add("  four  , five  ,  six ,  seven   ,");
         selects.add("");
         selects.add("  ");
         selects.add(" eight, ,");
         selects.add(" ,nine ,ten,,  , eleven,");
         selects.add("twelve");
         selects.add("thirteen[az > 65], fourteen[2]");
         selects.add("fifteen[1-20]");
         results = CimiStringParams.splitByComma(selects);
 
         Assert.assertEquals(15, results.size());
         Assert
             .assertEquals(
                 "[one, two, three, four, five, six, seven, eight, nine, ten, eleven, twelve, thirteen[az > 65], fourteen[2], fifteen[1-20]]",
                 results.toString());
     }
 
     /**
      * Test for {@link CimiSelect#copyBeanAttributes(Object)}.
      */
     @Test
     public void testDispatchAttributesValues() {
         List<String> selects;
         CimiSelect cimiSelect;
         Map<String, Object> dispatched;
 
         // CimiSelect with a 'unknwon' property
         selects = new ArrayList<String>();
         selects.add("name");
         selects.add("description");
         selects.add("unknown");
 
         cimiSelect = new CimiSelect();
         cimiSelect.setInitialValues(selects);
 
         // Dispatching select on MachineImage
         MachineImage image = new MachineImage();
         image.setName("imageNameValue");
         image.setDescription("imageDescriptionValue");
 
         dispatched = cimiSelect.copyBeanAttributes(image);
 
         Assert.assertEquals(2, dispatched.size());
         Assert.assertEquals("imageNameValue", dispatched.get("name"));
         Assert.assertEquals("imageDescriptionValue", dispatched.get("description"));
 
         // Same dispatching on MachineConfiguration with only one property
         // attributed
         MachineConfiguration config = new MachineConfiguration();
         config.setName("configNameValue");
 
         dispatched = cimiSelect.copyBeanAttributes(config);
         Assert.assertEquals(1, dispatched.size());
         Assert.assertEquals("configNameValue", dispatched.get("name"));
     }
 
     @Test
     public void testCases() {
         List<String> selects;
         CimiSelect cimi = new CimiSelect();
 
         // No select
         Assert.assertTrue(cimi.isEmpty());
         Assert.assertNull(cimi.getValues());
 
         // Single select
         selects = new ArrayList<String>();
         selects.add("attrOne");
 
         cimi.setInitialValues(selects);
         Assert.assertFalse(cimi.isEmpty());
         Assert.assertNotNull(cimi.getValues());
         Assert.assertEquals(1, cimi.getValues().size());
 
         // Duo select in single line
         selects = new ArrayList<String>();
         selects.add("attrOne, attrTwo");
 
         cimi.setInitialValues(selects);
         Assert.assertFalse(cimi.isEmpty());
         Assert.assertNotNull(cimi.getValues());
         Assert.assertEquals(2, cimi.getValues().size());
 
         // Duo select in multi lines
         selects = new ArrayList<String>();
         selects.add("attrOne");
         selects.add("attrTwo");
 
         cimi.setInitialValues(selects);
         Assert.assertFalse(cimi.isEmpty());
         Assert.assertNotNull(cimi.getValues());
         Assert.assertEquals(2, cimi.getValues().size());

        // FIXME
        // Multi select with expression array
        selects = new ArrayList<String>();
        selects.add("attrOne");
        selects.add("attrTwo[expTwo = 7]");

        cimi.setInitialValues(selects);
        Assert.assertFalse(cimi.isEmpty());
        Assert.assertNotNull(cimi.getValues());
        Assert.assertEquals(2, cimi.getValues().size());

        // FIXME
        // Multi select with expression and numeric array
        selects = new ArrayList<String>();
        selects.add("attrOne");
        selects.add("attrTwo[expTwo = 7]");
        selects.add("attrThree[25-103]");

        cimi.setInitialValues(selects);
        Assert.assertFalse(cimi.isEmpty());
        Assert.assertNotNull(cimi.getValues());
        Assert.assertEquals(3, cimi.getValues().size());

     }
 
 }
