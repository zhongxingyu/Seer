 /*
  * Copyright (c) 2010 Sharegrove Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package org.msjs.page;
 
 import com.google.inject.Binder;
 import com.google.inject.Guice;
 import com.google.inject.Inject;
 import com.google.inject.Injector;
 import com.google.inject.Module;
 import com.google.inject.util.Modules;
 import org.apache.log4j.Logger;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
 import org.mozilla.javascript.ContextFactory;
 import org.msjs.config.MsjsConfiguration;
 import org.msjs.config.MsjsModule;
import org.msjs.config.BasicConfiguration;
import org.msjs.page.Page;
import org.msjs.page.PageContextProvider;
 import org.msjs.script.FunctionParser;
 import org.msjs.script.MsjsScriptContext;
 import org.msjs.script.ScriptLocator;
 
 import javax.servlet.ServletRequest;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 
 /**
  * Used to statically create a "DOT" file for the given msjs page, and writes
  * the result to standard out. This output can then be fed to the external
  * program GraphViz, which produces nice looking graph images.
  */
 public class DotRenderer {
 
     private static final Logger logger = Logger.getLogger(DotRenderer.class);
     private static final Object[] empty = {};
     private final PageContextProvider provider;
 
     @Inject
     DotRenderer(PageContextProvider provider) {
         this.provider = provider;
     }
 
     public String render(String pageName) {
         Page page = new Page(provider.get(pageName, false));
 
         final MsjsScriptContext context = page.getMsjsScriptContext();
 
         context.loadPackage("msjs.dotrender");
 
 
         return (String) context.callMethodOnBinding("msjs.dotrender", "dotRender", empty);
     }
 
     /**
      * Statically creates a "DOT" file for the given msjs page, and writes
      * the result to standard out. This is done by loading the msjs script
      * dotrender and calling the method it provides.
      *
      * @param args The first command-line argument is used as the name of the
      *             script to locate for rendering
      * @throws java.io.IOException
      */
     public static void main(String[] args) throws IOException {
         //Later, we could use a different config for this
         //and even allow config to specify a different Guice
         //mockModule here
         final MsjsModule msjsModule = new MsjsModule(BasicConfiguration.getConfiguration());
         final Module mockModule = Modules.override(msjsModule).with(new MockModule());
         Injector injector = Guice.createInjector(mockModule);
         MsjsConfiguration config = injector.getInstance(MsjsConfiguration.class);
 
         String outLocation = config.getMsjsRoot() + "/out/dotfile.dot";
 
         PageContextProvider provider = injector.getInstance(PageContextProvider.class);
         DotRenderer renderer = new DotRenderer(provider);
         FileWriter writer = new FileWriter(new File(outLocation));
         writer.write(renderer.render(args[0]));
         writer.flush();
     }
 
     private static class MockModule implements Module {
         private static class DottyScriptContext extends MsjsScriptContext {
             /**
              * Create a new context for msjs script execution.
              *
              * @param cxFactory The factory for Rhino execution contexts.
              * @param locator   ScriptLocator to use to find scripts that are loaded by
              *                  other scripts, or externally, through
              *                  {@link org.msjs.script.MsjsScriptContext#loadPackage(String)}
              */
             @Inject
             public DottyScriptContext(ContextFactory cxFactory, ScriptLocator locator) {
                 super(cxFactory, locator, new FunctionParser(cxFactory));
             }
 
             @Override
             public void redirect(final String url) {
                 logger.info("Ignoring redirect: " + url);
             }
         }
 
         public void configure(final Binder binder) {
             HttpServletRequest mockRequest = createNiceMock(HttpServletRequest.class);
             expect(mockRequest.getCookies()).andReturn(new Cookie[0]).anyTimes();
             replay(mockRequest);
             binder.bind(ServletRequest.class).toInstance(mockRequest);
             binder.bind(MsjsScriptContext.class).to(DottyScriptContext.class);
         }
     }
 }
