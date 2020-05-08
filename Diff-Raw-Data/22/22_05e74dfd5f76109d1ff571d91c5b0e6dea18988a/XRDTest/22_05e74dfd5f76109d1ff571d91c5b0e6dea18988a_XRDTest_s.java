 /*
  * Copyright 2009 University Corporation for Advanced Internet Development, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.openxrd.xrd.core.impl;
 
import javax.xml.namespace.QName;

 import org.openxrd.common.BaseXRDObjectProviderTestCase;
import org.openxrd.common.XRDConstants;
 import org.openxrd.xrd.core.Alias;
 import org.openxrd.xrd.core.Expires;
 import org.openxrd.xrd.core.Link;
import org.openxrd.xrd.core.Subject;
 import org.openxrd.xrd.core.Property;
 import org.openxrd.xrd.core.XRD;
 
 /**
  * Test case for creating, marshalling, and unmarshalling {@link org.openxrd.xrd.core.impl.XRDImpl}.
  */
 public class XRDTest extends BaseXRDObjectProviderTestCase {
 
     /** Expected id value. */
     protected String expectedID;
 
     /** Count of Alias sub-elements. */
     protected int aliasCount;
 
    /** Count of Type sub-elements. */
    protected int typeCount;
 
     /** Count of Link sub-elements. */
     protected int linkCount;
 
     /** Constructor. */
     public XRDTest() {
         singleElementFile = "/data/org/openxrd/xrd/core/impl/XRD.xml";
         childElementsFile = "/data/org/openxrd/xrd/core/impl/XRDChildElements.xml";
     }
 
     /** {@inheritDoc} */
     protected void setUp() throws Exception {
         super.setUp();
 
         expectedID = "id";
         aliasCount = 2;
        typeCount = 3;
         linkCount = 2;
     }
 
     /** {@inheritDoc} */
     public void testSingleElementUnmarshall() {
         XRD xrd = (XRD) unmarshallElement(singleElementFile);
 
         String id = xrd.getID();
         assertEquals("ID was " + id + ", expected " + expectedID, expectedID, id);
     }
 
     /** {@inheritDoc} */
     public void testChildElementsUnmarshall() {
         XRD xrd = (XRD) unmarshallElement(childElementsFile);
 
         assertNotNull("Expires element not present", xrd.getExpires());
         assertNotNull("Subject element not present", xrd.getSubject());
         assertEquals("Alias count not as expected", aliasCount, xrd.getAliases().size());
        assertEquals("Type count not as expected", typeCount, xrd.getProperties().size());
         assertEquals("Link count not as expected", linkCount, xrd.getLinks().size());
     }
 
     /** {@inheritDoc} */
     public void testSingleElementMarshall() {
         XRD xrd = (XRD) buildXMLObject(XRD.DEFAULT_ELEMENT_NAME);
 
         xrd.setID(expectedID);
         assertEquals(expectedDOM, xrd);
     }
 
     /** {@inheritDoc} */
     public void testChildElementsMarshall() {
         XRD xrd = (XRD) buildXMLObject(XRD.DEFAULT_ELEMENT_NAME);
 
         xrd.setExpires((Expires) buildXMLObject(Expires.DEFAULT_ELEMENT_NAME));
         xrd.setSubject((Subject) buildXMLObject(Subject.DEFAULT_ELEMENT_NAME));
 
         for (int i = 0; i < aliasCount; i++) {
             xrd.getAliases().add((Alias) buildXMLObject(Alias.DEFAULT_ELEMENT_NAME));
         }
 
        for (int i = 0; i < typeCount; i++) {
             xrd.getProperties().add((Property) buildXMLObject(Property.DEFAULT_ELEMENT_NAME));
         }
 
         for (int i = 0; i < linkCount; i++) {
             xrd.getLinks().add((Link) buildXMLObject(Link.DEFAULT_ELEMENT_NAME));
         }
 
         assertEquals(expectedChildElementsDOM, xrd);
     }
 
 }
