 /*
  * CDDL HEADER START
  *
  * The contents of this file are subject to the terms of the
  * Common Development and Distribution License, Version 1.0 only
  * (the "License").  You may not use this file except in compliance
  * with the License.
  *
  * You can obtain a copy of the license at license/ESCIDOC.LICENSE
  * or http://www.escidoc.de/license.
  * See the License for the specific language governing permissions
  * and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL HEADER in each
  * file and include the License file at license/ESCIDOC.LICENSE.
  * If applicable, add the following below this CDDL HEADER, with the
  * fields enclosed by brackets "[]" replaced with your own identifying
  * information: Portions Copyright [yyyy] [name of copyright owner]
  *
  * CDDL HEADER END
  */
 
 /*
  * Copyright 2006-2008 Fachinformationszentrum Karlsruhe Gesellschaft
  * fuer wissenschaftlich-technische Information mbH and Max-Planck-
  * Gesellschaft zur Foerderung der Wissenschaft e.V.  
  * All rights reserved.  Use is subject to license terms.
  */
 package de.escidoc.core.test.om.container.rest;
 
 import org.junit.Test;
 
 import de.escidoc.core.test.EscidocRestSoapTestBase;
 import de.escidoc.core.test.om.container.ContainerTestBase;
 import de.escidoc.core.test.common.client.servlet.Constants;
 
 /**
  * Container tests with REST transport.
  * 
  * @author MSC
  * 
  */
 public class ContainerRetrieveRestTest extends ContainerTestBase {
 
     private String theItemId;
     
     /**
      * Constructor.
      * 
      */
     public ContainerRetrieveRestTest() {
         super(Constants.TRANSPORT_REST);
 
     }
 
     /**
      * Test successfully retrieving of container.
      * 
      * @throws Exception
      *             Thrown if retrieve fails.
      */
     @Test
     public void testRetrieveResources() throws Exception {
 
         String xmlData =
             EscidocRestSoapTestBase.getTemplateAsString(TEMPLATE_ITEM_PATH
                 + "/" + getTransport(false), "escidoc_item_198_for_create.xml");
 
         String theItemXml = handleXmlResult(getItemClient().create(xmlData));
 
         this.theItemId = getObjidValue(theItemXml);
         xmlData =
            EscidocRestSoapTestBase.getTemplateAsString(TEMPLATE_CONTAINER_PATH + "/rest",
                 "create_container_v1.1-forItem.xml");
 
         String theContainerXml = create(xmlData.replaceAll("##ITEMID##", theItemId));
         String theContainerId = getObjidValue(theContainerXml);
 
         String resourcesXml = retrieveResources(theContainerId);
         assertXmlValidContainer(resourcesXml);
 
     }
 
 }
