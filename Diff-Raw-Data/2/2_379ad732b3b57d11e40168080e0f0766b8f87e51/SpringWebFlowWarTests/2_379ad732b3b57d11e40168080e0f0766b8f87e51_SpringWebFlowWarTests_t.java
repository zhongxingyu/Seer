 /*******************************************************************************
  * Copyright (c) 2008, 2010 VMware Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   VMware Inc. - initial contribution
  *******************************************************************************/
 
 package org.eclipse.virgo.web.test;
 
 import java.io.File;
 
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  */
 
 public class SpringWebFlowWarTests extends AbstractWebIntegrationTests {
 
     private static final String WEBFLOW_VERSION = "2.0.8.RELEASE";
 
    private static final String REPO_PATH = System.getProperty("user.home") + "/ivy/ivy-cache/repository/org.springframework.webflow.samples";
     
     @Before
     public void configureHttpClient() {
         this.followRedirects = true;
         this.reuseHttpClient = false;
     }
 
     @Test
     public void bookingWithSpringMVC() throws Exception {
         assertDeployAndUndeployBehavior("swf-booking-mvc-" + WEBFLOW_VERSION, new File(REPO_PATH + "/swf-booking-mvc/" + WEBFLOW_VERSION + "/swf-booking-mvc-" + WEBFLOW_VERSION + ".war"), 
             "", 
             "spring/hotels/index", 
             "spring/hotels/search?searchString=&pageSize=5",
             "spring/hotels/show?id=17");
     }
 
     @Test
     public void bookingWithSpringFaces() throws Exception {
         assertDeployAndUndeployBehavior("swf-booking-faces-" + WEBFLOW_VERSION, new File(REPO_PATH + "/swf-booking-faces/" + WEBFLOW_VERSION + "/swf-booking-faces-" + WEBFLOW_VERSION + ".war"),
             "", 
             "spring/main"); 
     }
 
 }
