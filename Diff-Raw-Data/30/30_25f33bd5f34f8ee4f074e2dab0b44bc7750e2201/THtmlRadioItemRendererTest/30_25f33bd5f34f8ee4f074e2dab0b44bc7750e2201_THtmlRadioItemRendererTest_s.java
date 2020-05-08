 /*
  * Copyright 2004-2009 the Seasar Foundation and the Others.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
  * either express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 package org.seasar.teeda.extension.render.html;
 
 import javax.faces.render.Renderer;
 import javax.faces.render.RendererTest;
 
 import org.seasar.teeda.core.mock.MockHtmlSelectOneRadio;
 import org.seasar.teeda.extension.mock.MockTHtmlRadioItem;
 
 /**
  * @author manhole
  */
 public class THtmlRadioItemRendererTest extends RendererTest {
 
     private THtmlRadioItemRenderer renderer;
 
     private MockTHtmlRadioItem component;
 
     private MockHtmlSelectOneRadio parent;
 
     protected void setUp() throws Exception {
         super.setUp();
         renderer = (THtmlRadioItemRenderer) createRenderer();
         component = new MockTHtmlRadioItem();
         component.setRenderer(renderer);
         parent = new MockHtmlSelectOneRadio();
     }
 
     public void testEncode_NoParent() throws Exception {
         // ## Arrange ##
         component.setValue("abc");
 
         // ## Act ##
         // ## Assert ##
         try {
             encodeByRenderer(renderer, component);
             fail();
         } catch (final RuntimeException e) {
         }
     }
 
     public void testEncode_WithValue() throws Exception {
         parent.setClientId("aaa");
         parent.getChildren().add(component);
         component.setValue("abc");
         encodeByRenderer(renderer, component);
         assertEquals("<input type=\"radio\" name=\"aaa\" value=\"abc\" />",
                 getResponseText());
     }
 
     public void testEncode_WithNameAndValue() throws Exception {
         parent.setId("aaa");
         parent.setClientId("form:aaa");
         parent.getChildren().add(component);
         component.setName("aaa");
         component.setValue("abc");
         encodeByRenderer(renderer, component);
         assertEquals(
                 "<input type=\"radio\" name=\"form:aaa\" value=\"abc\" />",
                 getResponseText());
     }
 
     public void testEncode_WithNameSuffix() throws Exception {
         parent.setId("aaa-hoge");
         parent.setClientId("form:aaa");
         parent.getChildren().add(component);
         component.setName("aaa");
         component.setValue("abc");
         encodeByRenderer(renderer, component);
         assertEquals(
                 "<input type=\"radio\" name=\"form:aaa\" value=\"abc\" />",
                 getResponseText());
     }
 
     protected Renderer createRenderer() {
         THtmlRadioItemRenderer renderer = new THtmlRadioItemRenderer();
         renderer.setComponentIdLookupStrategy(getComponentIdLookupStrategy());
         return renderer;
     }
 
 }
