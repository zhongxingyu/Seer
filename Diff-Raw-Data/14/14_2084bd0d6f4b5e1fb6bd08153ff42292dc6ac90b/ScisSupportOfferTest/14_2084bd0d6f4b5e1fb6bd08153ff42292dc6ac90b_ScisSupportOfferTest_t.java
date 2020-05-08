 /*******************************************************************************
 *
 * Copyright (c) 2012 Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Maintainer/Contributor: 
 *
 *    Kohsuke Kawaguchi, Steven Christou
 *     
 *
 *******************************************************************************/
 
 package hudson.plugins.scis_ad;
 
 import org.jvnet.hudson.test.HudsonTestCase;
 
 public class ScisSupportOfferTest extends HudsonTestCase {
     /**
      * Shouldn't be active by default.
      */
     public void testInitial() {
         assertFalse(new ScisSupportOffer().isActivated());
     }
 
     /**
      * Makes sure that dismiss works.
      */
     // this requires test harness bug fix in 1.320
    public void testDeactivate() throws Exception {
        ScisSupportOffer mon = (ScisSupportOffer) hudson.getAdministrativeMonitor(ScisSupportOffer.class.getName());
        mon.active = true;
        assertTrue(mon.isEnabled());
        submit(new WebClient().goTo("/manage").getFormByName(mon.id),"no");
        assertFalse(mon.isEnabled());
    }
 
     /**
      * Makes sure that remind-later works.
      */
     public void testRemindLater() throws Exception {
         ScisSupportOffer mon = (ScisSupportOffer) hudson.getAdministrativeMonitor(ScisSupportOffer.class.getName());
         mon.active = true;
         submit(new WebClient().goTo("/manage").getFormByName(mon.id),"later");
         assertTrue(mon.isEnabled());
         assertFalse(mon.active);
 
         // simulated reboot
         assertFalse(new ScisSupportOffer().active);
     }
 }
