 /*
  * Copyright 2004-2006 the Seasar Foundation and the Others.
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
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.event.ActionEvent;
 import javax.faces.event.FacesEvent;
 import javax.faces.internal.FacesConfigOptions;
 import javax.faces.render.Renderer;
 import javax.faces.render.RendererTest;
 
 import junitx.framework.ObjectAssert;
 
 import org.custommonkey.xmlunit.Diff;
 import org.seasar.framework.container.S2Container;
 import org.seasar.framework.container.factory.SingletonS2ContainerFactory;
 import org.seasar.framework.container.impl.S2ContainerImpl;
 import org.seasar.framework.convention.impl.NamingConventionImpl;
 import org.seasar.framework.util.ClassUtil;
 import org.seasar.teeda.core.mock.MockFacesContext;
 import org.seasar.teeda.core.mock.MockMethodBinding;
 import org.seasar.teeda.core.mock.MockUIComponentBaseWithNamingContainer;
 import org.seasar.teeda.extension.component.html.THtmlCommandButton;
 import org.seasar.teeda.extension.render.html.sub.web.hoge.HogePage;
 import org.seasar.teeda.extension.util.TransactionTokenUtil;
 

 /**
  * @author shot
  */
 public class THtmlCommandButtonRendererTest extends RendererTest {
 
     private THtmlCommandButtonRenderer renderer;
 
     private MockTHtmlCommandButton htmlCommandButton;
 
     protected void setUp() throws Exception {
         super.setUp();
         renderer = createTHtmlCommandButtonRenderer();
         htmlCommandButton = new MockTHtmlCommandButton();
         htmlCommandButton.setRenderer(renderer);
     }
 
     public void testEncode_NoValue() throws Exception {
         // ## Arrange ##
         // ## Act ##
         encodeByRenderer(renderer, htmlCommandButton);
 
         // ## Assert ##
         assertEquals("<input type=\"submit\" name=\"_id0\" />",
                 getResponseText());
     }
 
     public void testEncode_WithToken() throws Exception {
         // ## Arrange ##
        htmlCommandButton.setId("doHoge");
         htmlCommandButton.setValue("abc");
 
         // ## Act ##
         encodeByRenderer(renderer, htmlCommandButton);
 
         // ## Assert ##
         final String responseText = getResponseText();
         System.out.println(responseText);
         Pattern pattern = Pattern
                 .compile("(.+?)<input type=\"hidden\"(.+?)\"(.+?)");
         Matcher matcher = pattern.matcher(responseText);
         assertEquals(true, matcher.matches());
         assertEquals(3, matcher.groupCount());
 
         final String submitPart = matcher.group(1);
         assertEquals(
                "<input type=\"submit\" id=\"doHoge\" name=\"doHoge\" value=\"abc\" />",
                 submitPart);
 
         pattern = Pattern.compile("(.+?)name=\"" + TransactionTokenUtil.TOKEN
                 + "\"(.+?)\"(.+?)");
         matcher = pattern.matcher(responseText);
         assertEquals(true, matcher.matches());
         assertEquals(3, matcher.groupCount());
     }
 
     public void tearDownEncode_WithLabelValue() throws Exception {
         SingletonS2ContainerFactory.setContainer(null);
         SingletonS2ContainerFactory.destroy();
     }
 
     public void testEncode_WithLabelValue() throws Exception {
         // ## Arrange ##
         final S2Container container = new S2ContainerImpl();
         SingletonS2ContainerFactory.setContainer(container);
         NamingConventionImpl nc = new NamingConventionImpl();
         container.register(nc);
         nc.addRootPackageName(ClassUtil.getPackageName(getClass()) + ".sub");
         container.register(HogePage.class, "hoge_hogePage");
         FacesConfigOptions.setDefaultSuffix(".html");
         getFacesContext().getViewRoot().setViewId(
                 nc.getViewRootPath() + "/hoge/hoge.html");
        htmlCommandButton.setId("doHoge");
         htmlCommandButton.setValue("abc");
 
         // ## Act ##
         encodeByRenderer(renderer, htmlCommandButton);
 
         // ## Assert ##
         final String responseText = getResponseText();
         System.out.println(responseText);
         Pattern pattern = Pattern
                 .compile("(.+?)<input type=\"hidden\"(.+?)\"(.+?)");
         Matcher matcher = pattern.matcher(responseText);
         assertEquals(true, matcher.matches());
         assertEquals(3, matcher.groupCount());
 
         final String submitPart = matcher.group(1);
         assertEquals(
                "<input type=\"submit\" id=\"doHoge\" name=\"doHoge\" value=\"HOGE\" />",
                 submitPart);
     }
 
     public void testEncode_WithValue() throws Exception {
         // ## Arrange ##
         htmlCommandButton.setValue("abc");
 
         // ## Act ##
         encodeByRenderer(renderer, htmlCommandButton);
 
         // ## Assert ##
         assertEquals("<input type=\"submit\" name=\"_id0\" value=\"abc\" />",
                 getResponseText());
     }
 
     public void testEncode_WithValue2() throws Exception {
         // ## Arrange ##
         htmlCommandButton.setValue("&nbsp;abc");
 
         // ## Act ##
         encodeByRenderer(renderer, htmlCommandButton);
 
         // ## Assert ##
         assertEquals(
                 "<input type=\"submit\" name=\"_id0\" value=\"&nbsp;abc\" />",
                 getResponseText());
     }
 
     public void testEncode_RenderFalse() throws Exception {
         // ## Arrange ##
         htmlCommandButton.setRendered(false);
         htmlCommandButton.setValue("abc");
 
         // ## Act ##
         encodeByRenderer(renderer, htmlCommandButton);
 
         // ## Assert ##
         assertEquals("", getResponseText());
     }
 
     public void testEncode_WithId() throws Exception {
         htmlCommandButton.setId("a");
 
         UIComponent parent = new MockUIComponentBaseWithNamingContainer();
         parent.setId("b");
         parent.getChildren().add(htmlCommandButton);
 
         encodeByRenderer(renderer, htmlCommandButton);
 
         assertEquals("<input type=\"submit\" id=\"a\" name=\"b:a\" />",
                 getResponseText());
     }
 
     public void testEncode_Reset() throws Exception {
         htmlCommandButton.setId("a");
         htmlCommandButton.setType("reset");
 
         encodeByRenderer(renderer, htmlCommandButton);
 
         assertEquals("<input type=\"reset\" id=\"a\" name=\"a\" />",
                 getResponseText());
     }
 
     public void testEncode_Image() throws Exception {
         // ## Arrange ##
         htmlCommandButton.setId("a");
         htmlCommandButton.setImage("bb");
         htmlCommandButton.setValue("c"); // should be ignored
 
         // ## Act ##
         encodeByRenderer(renderer, htmlCommandButton);
 
         // ## Assert ##
         assertEquals("<input type=\"image\" id=\"a\" name=\"a\" src=\"bb\" />",
                 getResponseText());
     }
 
     public void testEncode_ImageIfTypeIsReset() throws Exception {
         // ## Arrange ##
         htmlCommandButton.setId("a");
         htmlCommandButton.setType("reset"); // should be ignored
         htmlCommandButton.setImage("bb");
 
         // ## Act ##
         encodeByRenderer(renderer, htmlCommandButton);
 
         // ## Assert ##
         assertEquals("<input type=\"image\" id=\"a\" name=\"a\" src=\"bb\" />",
                 getResponseText());
     }
 
     public void testEncode_IgnoreAttributes() throws Exception {
         // ## Arrange ##
         htmlCommandButton.setId("a");
 
         // この属性は出力されないこと
         htmlCommandButton.setAction(new MockMethodBinding());
         htmlCommandButton.setImmediate(true);
 
         // ## Act ##
         encodeByRenderer(renderer, htmlCommandButton);
 
         // ## Assert ##
         assertEquals("<input type=\"submit\" id=\"a\" name=\"a\" />",
                 getResponseText());
     }
 
     public void testEncode_WithUnknownAttribute1() throws Exception {
         htmlCommandButton.setId("a");
         htmlCommandButton.getAttributes().put("b", "c");
 
         encodeByRenderer(renderer, htmlCommandButton);
 
         assertEquals("<input type=\"submit\" id=\"a\" name=\"a\" b=\"c\" />",
                 getResponseText());
     }
 
     public void testEncode_WithUnknownAttribute2() throws Exception {
         htmlCommandButton.setId("a");
         htmlCommandButton.getAttributes().put("b.c", "c");
 
         encodeByRenderer(renderer, htmlCommandButton);
 
         assertEquals("<input type=\"submit\" id=\"a\" name=\"a\" />",
                 getResponseText());
     }
 
     public void testEncode_WithAllAttributes() throws Exception {
         htmlCommandButton.setAccesskey("a");
         htmlCommandButton.setAlt("b");
         htmlCommandButton.setDir("c");
         htmlCommandButton.setDisabled(true);
         // htmlCommandButton_.setImage("e");
         htmlCommandButton.setLang("f");
         htmlCommandButton.setOnblur("g");
         htmlCommandButton.setOnchange("h");
         htmlCommandButton.setOnclick("i");
         htmlCommandButton.setOndblclick("j");
         htmlCommandButton.setOnfocus("k");
         htmlCommandButton.setOnkeydown("l");
         htmlCommandButton.setOnkeypress("m");
         htmlCommandButton.setOnkeyup("n");
         htmlCommandButton.setOnmousedown("o");
         htmlCommandButton.setOnmousemove("p");
         htmlCommandButton.setOnmouseout("q");
         htmlCommandButton.setOnmouseover("r");
         htmlCommandButton.setOnmouseup("s");
         htmlCommandButton.setOnselect("t");
         htmlCommandButton.setReadonly(true);
         htmlCommandButton.setStyle("w");
         htmlCommandButton.setStyleClass("u");
         htmlCommandButton.setTabindex("x");
         htmlCommandButton.setTitle("y");
         htmlCommandButton.setType("reset");
 
         htmlCommandButton.setId("A");
         htmlCommandButton.setValue("B");
 
         encodeByRenderer(renderer, htmlCommandButton);
 
         Diff diff = new Diff(
                 "<input type=\"reset\" id=\"A\" name=\"A\" value=\"B\""
                         + " accesskey=\"a\"" + " alt=\"b\"" + " dir=\"c\""
                         + " disabled=\"disabled\"" + " lang=\"f\""
                         + " onblur=\"g\"" + " onchange=\"h\""
                         + " onclick=\"i\"" + " ondblclick=\"j\""
                         + " onfocus=\"k\"" + " onkeydown=\"l\""
                         + " onkeypress=\"m\"" + " onkeyup=\"n\""
                         + " onmousedown=\"o\"" + " onmousemove=\"p\""
                         + " onmouseout=\"q\"" + " onmouseover=\"r\""
                         + " onmouseup=\"s\"" + " onselect=\"t\""
                         + " readonly=\"true\"" + " style=\"w\""
                         + " class=\"u\"" + " tabindex=\"x\"" + " title=\"y\""
                         + "/>", getResponseText());
         assertEquals(diff.toString(), true, diff.identical());
     }
 
     public void testDecode_None() throws Exception {
         // ## Arrange ##
         final FacesEvent[] args = { null };
         MockTHtmlCommandButton htmlCommandButton = new MockTHtmlCommandButton() {
             public void queueEvent(FacesEvent event) {
                 args[0] = event;
             }
         };
         htmlCommandButton.setRenderer(renderer);
         htmlCommandButton.setClientId("key");
 
         MockFacesContext context = getFacesContext();
 
         // ## Act ##
         renderer.decode(context, htmlCommandButton);
 
         // ## Assert ##
         assertEquals(null, args[0]);
     }
 
     public void testDecode_None_ResetType() throws Exception {
         // ## Arrange ##
         final FacesEvent[] args = { null };
         MockTHtmlCommandButton htmlCommandButton = new MockTHtmlCommandButton() {
             public void queueEvent(FacesEvent event) {
                 args[0] = event;
             }
         };
         htmlCommandButton.setRenderer(renderer);
         htmlCommandButton.setType("reset");
         htmlCommandButton.setClientId("key");
 
         MockFacesContext context = getFacesContext();
         context.getExternalContext().getRequestParameterMap().put("key",
                 "12345");
 
         // ## Act ##
         renderer.decode(context, htmlCommandButton);
 
         // ## Assert ##
         assertEquals(null, args[0]);
     }
 
     public void testDecode_SubmitSuccess() throws Exception {
         submitSuccessTest("key:aa");
     }
 
     public void testDecode_SubmitSuccessX() throws Exception {
         submitSuccessTest("key:aa.x");
     }
 
     public void testDecode_SubmitSuccessY() throws Exception {
         submitSuccessTest("key:aa.y");
     }
 
     private void submitSuccessTest(final String requestKey) {
         // ## Arrange ##
         final FacesEvent[] args = { null };
         MockTHtmlCommandButton htmlCommandButton = new MockTHtmlCommandButton() {
             public void queueEvent(FacesEvent event) {
                 args[0] = event;
             }
         };
         htmlCommandButton.setRenderer(renderer);
         htmlCommandButton.setClientId("key:aa");
 
         MockFacesContext context = getFacesContext();
         context.getExternalContext().getRequestParameterMap().put(requestKey,
                 "12345");
 
         // ## Act ##
         renderer.decode(context, htmlCommandButton);
 
         // ## Assert ##
         assertNotNull(requestKey, args[0]);
         ObjectAssert.assertInstanceOf(requestKey, ActionEvent.class, args[0]);
         assertSame(requestKey, htmlCommandButton, args[0].getSource());
     }
 
     public void testGetRendersChildren() throws Exception {
         assertEquals(false, renderer.getRendersChildren());
     }
 
     private THtmlCommandButtonRenderer createTHtmlCommandButtonRenderer() {
         return (THtmlCommandButtonRenderer) createRenderer();
     }
 
     protected Renderer createRenderer() {
         THtmlCommandButtonRenderer renderer = new THtmlCommandButtonRenderer();
         renderer.setComponentIdLookupStrategy(getComponentIdLookupStrategy());
         return renderer;
     }
 
     public class MockTHtmlCommandButton extends THtmlCommandButton {
 
         private Renderer renderer;
 
         private String clientId;
 
         public void setRenderer(Renderer renderer) {
             this.renderer = renderer;
         }
 
         protected Renderer getRenderer(FacesContext context) {
             if (renderer != null) {
                 return renderer;
             }
             return super.getRenderer(context);
         }
 
         public String getClientId(FacesContext context) {
             if (clientId != null) {
                 return clientId;
             }
             return super.getClientId(context);
         }
 
         public void setClientId(String clientId) {
             this.clientId = clientId;
         }
 
     }
 }
