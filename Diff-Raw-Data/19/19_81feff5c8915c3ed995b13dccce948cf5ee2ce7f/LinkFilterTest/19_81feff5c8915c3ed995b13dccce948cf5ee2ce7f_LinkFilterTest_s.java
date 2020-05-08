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
 
 package org.openxrd.xrd;
 
 import java.util.List;
 
 import org.openxrd.common.BaseTestCase;
 import org.openxrd.xrd.core.Link;
 import org.openxrd.xrd.core.XRD;
 
 /**
  * Test link filtering.
  */
 public class LinkFilterTest extends BaseTestCase {
 
     /**
      * Test basic link filter.
      */
     public void testBasicLinkFilter() {
         XRD xrd = (XRD) unmarshallElement("/data/org/openxrd/xrd/link-filtering.xml");
 
         List<Link> links = XRDUtils.getLinkedResources(xrd, "describedby");
 
         assertEquals(2, links.size());
         assertEquals("http://example.com/1", links.get(0).getURIs().get(0).getValue());
         assertEquals("http://example.com/3", links.get(1).getURIs().get(0).getValue());
     }
 
 }
