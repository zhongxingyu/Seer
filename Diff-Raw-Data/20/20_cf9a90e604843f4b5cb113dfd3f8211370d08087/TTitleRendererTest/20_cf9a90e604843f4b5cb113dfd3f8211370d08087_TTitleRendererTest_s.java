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
 package org.seasar.teeda.extension.render;
 
 import javax.faces.render.Renderer;
 import javax.faces.render.RendererTest;
 
 import org.seasar.teeda.extension.mock.MockUITitle;
 
 /**
  * @author shot
  */
 public class TTitleRendererTest extends RendererTest {
 
     private MockUITitle title;
 
     private TTitleRenderer renderer;
 
     protected void setUp() throws Exception {
         super.setUp();
         renderer = createTTitleRenderer();
         title = new MockUITitle();
         title.setRenderer(renderer);
     }
 
     public void test_renderTitle() throws Exception {
         title.setId("aaa");
         title.setValue("hoge");
         title.setDir("left");
         title.setLang("ja");
         encodeByRenderer(renderer, title);
         assertEquals("<title id=\"aaa\" dir=\"left\" lang=\"ja\">hoge</title>",
                 getResponseText());
     }
 
     public void test_renderTitle2_valueIsPrioroThanTemplateValue()
             throws Exception {
         title.setId("aaa");
         title.setValue("hoge");
         title.setDir("left");
         title.setLang("ja");
         title.setTemplateValue("aaa");
         encodeByRenderer(renderer, title);
         assertEquals("<title id=\"aaa\" dir=\"left\" lang=\"ja\">hoge</title>",
                 getResponseText());
     }
 
     public void test_renderTitle2_justTemplateValue() throws Exception {
         title.setId("aaa");
         title.setDir("left");
         title.setLang("ja");
        title.setTemplateValue("aaa");
         encodeByRenderer(renderer, title);
        assertEquals("<title id=\"aaa\" dir=\"left\" lang=\"ja\">aaa</title>",
                 getResponseText());
     }
 
     public void testEncode_IgnoreAttributes() throws Exception {
         // ## Arrange ##
         title.setValue("a");
 
         // この属性は出力されないこと
         title.setDefaultKey("A");
         title.setDefaultPropertiesName("B");
         title.setPropertiesName("C");
         title.setTransient(true);
 
         // ## Act ##
         encodeByRenderer(renderer, title);
 
         // ## Assert ##
         assertEquals("<title>a</title>", getResponseText());
     }
 
     private TTitleRenderer createTTitleRenderer() {
         return (TTitleRenderer) createRenderer();
     }
 
     protected Renderer createRenderer() {
         TTitleRenderer renderer = new TTitleRenderer();
         renderer.setComponentIdLookupStrategy(getComponentIdLookupStrategy());
         return renderer;
     }
 
 }
