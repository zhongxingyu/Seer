 /*
  * See the NOTICE file distributed with this work for additional
  * information regarding copyright ownership.
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
 package com.celements.common.test;
 
 import java.util.HashMap;
 import java.util.Locale;
 import java.util.Map;
 import java.util.ResourceBundle;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.velocity.VelocityContext;
 import org.junit.After;
 import org.junit.Before;
 import org.xwiki.component.manager.ComponentLookupException;
 import org.xwiki.component.manager.ComponentManager;
 import org.xwiki.context.Execution;
 import org.xwiki.context.ExecutionContext;
import org.xwiki.test.jmock.AbstractComponentTestCase;
 
 import com.xpn.xwiki.XWikiContext;
 import com.xpn.xwiki.web.Utils;
 import com.xpn.xwiki.web.XWikiMessageTool;
 
 /**
  * Extension of {@link org.xwiki.test.AbstractComponentTestCase} that sets up a bridge between the new Execution
  * Context and the old XWikiContext. This allows code that uses XWikiContext to be tested using this Test Case class.
  * 
  * @version: AbstractBridgedComponentTestCase.java fpichler copied from AbstractBridgedComponentTestCase.java
  */
 public class AbstractBridgedComponentTestCase extends AbstractComponentTestCase {
 
   private static Log mLogger = LogFactory.getFactory().getInstance(
       AbstractBridgedComponentTestCase.class);
 
   private XWikiContext context;
 
   @Before
   public void setUp() throws Exception {
     super.setUp();
 
     // Statically store the component manager in {@link Utils} to be able to access it without
     // the context.
     Utils.setComponentManager(getComponentManager());
 
     this.context = (XWikiContext) getExecutionContext().getProperty("xwikicontext");
     if (this.context == null) {
       this.context = new XWikiContext();
   
       this.context.setDatabase("xwikidb");
       this.context.setMainXWiki("xwikiWiki");
       getExecutionContext().setProperty("xwikicontext", this.context);
   
       // We need to initialize the Component Manager so that the components can be looked up
       getContext().put(ComponentManager.class.getName(), getComponentManager());
     }
   }
 
 private ExecutionContext getExecutionContext() throws ComponentLookupException, Exception {
   return ((Execution)getComponentManager().getInstance(Execution.class)).getContext();
 }
 
   @After
   public void tearDown() throws Exception {
       Utils.setComponentManager(null);
       super.tearDown();
   }
 
   @SuppressWarnings("unchecked")
   public XWikiContext getContext() {
     if (this.context.getLanguage() == null) {
       this.context.setLanguage("de");
     }
     if (this.context.get("msg") == null) {
       Locale locale = new Locale(this.context.getLanguage());
       ResourceBundle bundle = ResourceBundle.getBundle("ApplicationResources",
           locale);
       if (bundle == null) {
           bundle = ResourceBundle.getBundle("ApplicationResources");
       }
       XWikiMessageTool msg = new TestMessageTool(bundle, context);
       context.put("msg", msg);
       VelocityContext vcontext = ((VelocityContext) context.get("vcontext"));
       if (vcontext != null) {
           vcontext.put("msg", msg);
           vcontext.put("locale", locale);
       }
       Map gcontext = (Map) context.get("gcontext");
       if (gcontext != null) {
           gcontext.put("msg", msg);
           gcontext.put("locale", locale);
       }
     }
     return this.context;
   }
 
   public class TestMessageTool extends XWikiMessageTool {
     
     private Map<String, String> injectedMessages =
       new HashMap<String, String>();
 
     public TestMessageTool(ResourceBundle bundle, XWikiContext context) {
       super(bundle, context);
     }
 
     public void injectMessage(String key, String value) {
       injectedMessages.put(key, value);
     }
 
     @Override
     public String get(String key) {
       if (injectedMessages.containsKey(key)) {
         return injectedMessages.get(key);
       } else {
         mLogger.error("TestMessageTool missing the key '" + key + "'.");
         return super.get(key);
       }
     }
 
   }
 }
