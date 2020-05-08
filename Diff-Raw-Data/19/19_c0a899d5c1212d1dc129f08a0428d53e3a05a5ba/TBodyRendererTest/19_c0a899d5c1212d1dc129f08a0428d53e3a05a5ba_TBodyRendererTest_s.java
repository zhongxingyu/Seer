 /*
  * Copyright 2004-2008 the Seasar Foundation and the Others.
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
 
 import java.io.IOException;
 
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import javax.faces.render.Renderer;
 import javax.faces.render.RendererTest;
 
 import org.seasar.teeda.extension.component.UIBody;
 
 /**
  * @author shot
  */
 public class TBodyRendererTest extends RendererTest {
 
     public void test_simple() throws Exception {
         TBodyRenderer renderer = new TBodyRenderer();
         UIBody body = new UIBody();
         body.setId("aaa");
         encodeByRenderer(renderer, body);
         assertEquals("<body></body>", getResponseText());
     }
 
     public void test_renderOther() throws Exception {
         TBodyRenderer renderer = new TBodyRenderer();
         UIBody body = new UIBody();
         body.setId("aaa");
         body.setOnload("hoge");
         encodeByRenderer(renderer, body);
        assertEquals("<body onload=\"hoge\"></body>", getResponseText());
     }
 
     public void test_simple2() throws Exception {
         TBodyRenderer renderer = new TBodyRenderer();
         UIBody component = new UIBody();
         component.setId("aaa");
         renderer.encodeBegin(getFacesContext(), component);
         MockRendererListener listener = new MockRendererListener();
         TBodyRenderer.addRendererListener(component, listener);
         renderer.encodeEnd(getFacesContext(), component);
        assertEquals("<body>hoge</body>", getResponseText());
     }
 
     protected Renderer createRenderer() {
         return new TBodyRenderer();
     }
 
     public static class MockRendererListener implements RendererListener {
 
         public void renderBeforeBodyEnd(FacesContext context)
                 throws IOException {
             ResponseWriter writer = context.getResponseWriter();
             writer.write("hoge");
         }
 
     }
 }
