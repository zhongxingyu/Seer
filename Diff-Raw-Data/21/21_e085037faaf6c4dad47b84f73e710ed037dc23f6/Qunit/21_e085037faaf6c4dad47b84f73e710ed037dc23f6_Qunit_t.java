 /*
  * $Id$
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
  */
 
 package org.jboss.test.qunit;
 
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.junit.rules.MethodRule;
 import org.junit.runners.model.FrameworkMethod;
 import org.junit.runners.model.Statement;
 
 import com.gargoylesoftware.htmlunit.BrowserVersion;
 import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
 import com.gargoylesoftware.htmlunit.MockWebConnection;
 import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
 import com.gargoylesoftware.htmlunit.ScriptResult;
 import com.gargoylesoftware.htmlunit.WebClient;
 import com.gargoylesoftware.htmlunit.html.HtmlPage;
 
 /**
  * <p class="changed_added_4_0">This class represents qunit test environment ( html page, java script libraries ... ) </p>
  * @author asmirnov@exadel.com
  *
  */
 public class Qunit implements MethodRule {
     
 
     /**
      * <p class="changed_added_4_0"></p>
      * @author asmirnov@exadel.com
      *
      */
     public static final class Builder {
         private final Qunit rule = new Qunit();
         
         public Builder content(String content) {
             rule.setHtmlContent(new ContentRef(content));
             return this;
         }
         
         public Builder contentResource(String src){
             rule.setHtmlContent(new ResourceRef(src));
             return this;
         }
         
         public Builder load(URL src) {
             rule.addScript(new URLRef(src));
             return this;
         }
         
         public Builder loadResource(String src) {
             rule.addScript(new ResourceRef(src));
             return this;
         }
 
        public Builder loadJsfResource(String name) {
            rule.addScript(new ResourceRef("/META-INF/resources/"+name));
            return this;
        }

        public Builder loadJsfResource(String name,String library) {
            rule.addScript(new ResourceRef("/META-INF/resources/"+library+"/"+name));
            return this;
        }

         public Builder loadExternal(String src) {
             rule.addScript(new ExternalRef(src));
             return this;
         }
 
         public Builder loadContent(String foo) {
             rule.addScript(new ContentRef(foo));
             return this;
         }
         public Builder emulate(BrowserVersion browser){
             rule.browser = browser;
             return this;
         }
         
         public Qunit build(){
             return rule;
         }
 
     }
 
     /**
      * <p class="changed_added_4_0">Html content for JavaScript tests.</p>
      * 
      */
     private ScriptRef htmlContent = new ContentRef("");
     
     private BrowserVersion browser = BrowserVersion.getDefault();
     
     private List<ScriptRef> scripts = new ArrayList<ScriptRef>();
 
     private WebClient webClient;
 
     private HtmlPage page;
 
     private MockWebConnection mockConnection;
     
     protected Qunit(){
         ;
     }
 
     public Statement apply(final Statement base, final FrameworkMethod method, final Object target) {
         return new Statement() {
             
             @Override
             public void evaluate() throws Throwable {
                 try {
                     System.out.println("Run Statement "+base.getClass().getName()+" for method "+method.getName());
                     setupQunit(method,target);
                     base.evaluate();
                 } finally {
                     thearDownQunit(method,target);
                 }
                 
             }
         };
     }
 
     protected void thearDownQunit(FrameworkMethod method, Object target) {
         if(null != page){
             webClient.closeAllWindows();
         }
         
     }
 
     protected void setupQunit(FrameworkMethod method, Object target) throws FailingHttpStatusCodeException, IOException {
         URL URL = new URL("http://localhost/");
         setupWebClient();
         String content = buildContent(method,target);
         mockConnection.setResponse(URL, content);
         page = webClient.getPage(URL);
         webClient.waitForBackgroundJavaScriptStartingBefore(4 * 60 * 1000);
     }
 
     private void setupWebClient() {
         webClient = new WebClient(browser);
         mockConnection = new MockWebConnection();
         webClient.setWebConnection(mockConnection);
         webClient.setAjaxController(new NicelyResynchronizingAjaxController());
     }
 
     private String buildContent(FrameworkMethod method, Object target) {
         StringBuilder content = new StringBuilder();
         content.append("<html><head><title>").append(method.getName()).append("</title>");
         appendScripts(content,target);
         content.append("</head><body>").append(getHtmlContent(target)).append("</body></html>");
         return content.toString();
     }
 
     private void appendScripts(StringBuilder content, Object target) {
         for (ScriptRef script : this.scripts) {
             content.append("<script type=\"text/javascript\" src=\"");
             URL url = script.getScript(target);
             content.append(url.toExternalForm());
             content.append("\" ></script>\n");
             mockConnection.setResponse(url, script.getContent(target), "application/javascript");
         }
         
     }
 
     private URL calculateDefaultURL(Object target){
         Class<? extends Object> targetClass = target.getClass();
         String resourceName = targetClass.getSimpleName()+".class";
         return targetClass.getResource(resourceName);
     }
 
     /**
      * <p class="changed_added_4_0"></p>
      * @param target 
      * @return the htmlContent
      */
     protected String getHtmlContent(Object target) {
         return this.htmlContent.getContent(target);
     }
 
     /**
      * <p class="changed_added_4_0"></p>
      * @param htmlContent the htmlContent to set
      */
     private void setHtmlContent(ScriptRef htmlContent) {
         this.htmlContent = htmlContent;
     }
 
     /**
      * <p class="changed_added_4_0">Append script to page libraries by URL</p>
      * @param src
      */
     private void addScript(ScriptRef src){
         scripts.add(src);
     }
     
 
     /**
      * <p class="changed_added_4_0">Run JavaScript expression in page context and returns result</p>
      * @param script
      * @return
      */
     public Object runScript(String script){
         ScriptResult scriptResult = page.executeJavaScript(script);
         page = (HtmlPage) scriptResult.getNewPage();
         return scriptResult.getJavaScriptResult();
         
     }
 
     /**
      * <p class="changed_added_4_0"></p>
      * @return the page
      */
     public HtmlPage getPage() {
         return this.page;
     }
 
     
     public static Builder builder(){
         return new Builder();
     }
 }
