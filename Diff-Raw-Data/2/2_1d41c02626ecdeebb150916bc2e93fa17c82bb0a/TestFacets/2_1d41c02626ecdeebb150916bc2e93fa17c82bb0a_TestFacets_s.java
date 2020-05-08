 /*******************************************************************************
  * JBoss, Home of Professional Open Source
  * Copyright 2010, Red Hat, Inc. and individual contributors
  * by the @authors tag. See the copyright.txt in the distribution for a
  * full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  *******************************************************************************/
 package org.richfaces.tests.metamer.ftest.a4jStatus;
 
 import static org.jboss.test.selenium.utils.URLUtils.buildUrl;
 
 import java.net.URL;
 
 import org.jboss.test.selenium.locator.ElementLocator;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 /**
  * @author <a href="mailto:lfryc@redhat.com">Lukas Fryc</a>
  * @version $Revision$
  */
 public class TestFacets extends AbstracStatusTest {
     StatusFacets facets = new StatusFacets();
 
     @Override
     public URL getTestUrl() {
         return buildUrl(contextPath, "faces/components/a4jStatus/simple.xhtml");
     }
 
     @BeforeMethod
     public void installStatusExtensions() {
         super.installStatusExtensions();
     }
 
     @Test
     public void testInterleavedChangingOfFacets() {
         for (int i = 0; i < 13; i++) {
             ElementLocator<?> button = (i % 2 == 0) ? button2 : buttonError;
             IterateStatus iterateStatus = IterateStatus.values()[i % IterateStatus.values().length];
             testChangingFacet(button, iterateStatus);
         }
     }
 
     void testChangingFacet(ElementLocator<?> button, IterateStatus iterateStatus) {
         switch (iterateStatus) {
             case START:
                 facets.setStartText(facets.getStartText() + "*");
                 break;
             case STOP:
                 facets.setStopText(facets.getStopText() + "*");
                 break;
             case ERROR:
                 facets.setErrorText(facets.getErrorText() + "*");
                 break;
             default:
                 throw new IllegalStateException();
         }
 
         final String startText = facets.getStartText();
        final String stopText = (button == buttonError) ? facets.getErrorText() : facets.getStopText();
 
         testRequestButton(button, startText, stopText);
     }
 
     private static enum IterateStatus {
         START, STOP, ERROR
     }
 }
