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
 package org.richfaces.tests.metamer.ftest.a4jAjax;
 
 import static org.jboss.test.selenium.utils.URLUtils.buildUrl;
 
 import java.net.URL;
 
 import org.jboss.test.selenium.locator.JQueryLocator;
 import org.testng.annotations.Test;
 
 /**
  * Test case for page /faces/components/a4jAjax/hGraphicImage.xhtml
  * 
  * @author <a href="mailto:ppitonak@redhat.com">Pavol Pitonak</a>
  * @version $Revision$
  */
 public class TestHGraphicImage extends AbstractTestCommand {
 
     private JQueryLocator button = pjq("img[id$=image]");
     
     @Override
     public URL getTestUrl() {
         return buildUrl(contextPath, "faces/components/a4jAjax/hGraphicImage.xhtml");
     }
 
     @Test
     public void testSimpleClick() {
         super.testClick(button, "RichFaces 4");
     }
 
     @Test
     public void testSimpleClickUnicode() {
         super.testClick(button, "ľščťžýáíéúôň фывацукйешгщь");
     }
 
     @Test
     public void testBypassUpdates() {
         super.testBypassUpdates(button);
     }
 
     @Test
     public void testData() {
         super.testData(button);
     }
 
     @Test
     public void testImmediate() {
         super.testImmediate(button);
     }
 
     @Test
     public void testImmediateBypassUpdates() {
         super.testImmediateBypassUpdates(button);
     }
 
     @Test
     public void testLimitRender() {
         super.testLimitRender(button);
     }
 
     @Test
     public void testEvents() {
         super.testEvents(button);
     }
 
     @Test
     public void testRender() {
        super.testEvents(button);
     }
 }
