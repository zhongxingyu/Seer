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
 
 package org.eclipse.virgo.kernel.deployer.test;
 
 import static org.junit.Assert.assertNotNull;
 
 import java.io.File;
 
 import org.junit.Test;
 
 /**
  * Test the promotion of a bundle import across the bundles of a PAR file.
  * 
  */
 public class ImportPromotionTests extends AbstractParTests {
 
     private static final String BUNDLE_SYMBOLIC_NAME = "ImportPromotion-1-ImporterA";
 
     private static final String BUNDLE_SYMBOLIC_NAME_2 = "ImportPromotionViaLibrary-1-ImporterA";
 
     @Test
     public void testImportPromotion() throws Throwable {
         File par = new File("src/test/resources/ImportPromotion.par");
         deploy(par);
         assertNotNull(ApplicationContextUtils.getApplicationContext(this.context, BUNDLE_SYMBOLIC_NAME));
         this.deployer.refresh(par.toURI(), "ImporterA");
        Thread.sleep(100);
         assertNotNull(ApplicationContextUtils.getApplicationContext(this.context, BUNDLE_SYMBOLIC_NAME));
     }
 
     @Test
     public void testImportPromotionViaLibrary() throws Throwable {
         deploy(new File("src/test/resources/ImportPromotionViaLibrary.par"));
         assertNotNull(ApplicationContextUtils.getApplicationContext(this.context, BUNDLE_SYMBOLIC_NAME_2));
     }
 }
