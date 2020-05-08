 /*
   Modified by The Atlantic Media Company from original source.
   See <https://github.com/one2team/highcharts-serverside-export> for original.
   
   Licensed under the Apache License, Version 2.0 (the "License"); you may not
   use this work except in compliance with the License. You may obtain a copy
   of the License in the LICENSE file, or at:
   
   http://www.apache.org/licenses/LICENSE-2.0
   
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
   WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
   License for the specific language governing permissions and limitations
   under the License.
 */
 package com.theatlantic.highcharts.export;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.io.IOUtils;
 import org.mozilla.javascript.Context;
 import org.mozilla.javascript.ContextAction;
 import org.mozilla.javascript.Script;
 import org.mozilla.javascript.Scriptable;
 import org.mozilla.javascript.ScriptableObject;
 
 import org.mozilla.javascript.tools.shell.ShellContextFactory;
 import org.mozilla.javascript.tools.debugger.Main;
 import org.mozilla.javascript.tools.debugger.ScopeProvider;
 
 
 public class SVGRendererInternal {
     
     protected static Scriptable sharedScope;
         
     protected static ShellContextFactory contextFactory = new ShellContextFactory();
     
     public SVGRendererInternal() {
         if (sharedScope == null) {
             if (!SVGHighchartsHelper.DEBUG) {
                 sharedScope = new SVGRendererGlobal(contextFactory);
             }
             contextFactory.call(new ScriptLoader());
         }
     }
     
     private static class ScriptLoader implements ContextAction {
         private List<Script> scripts;
         public Object run(Context cx) {
             SVGHighchartsHelper.LOGGER.trace("set langage version to 1.6");
             cx.setLanguageVersion(Context.VERSION_1_6);
 
             SVGHighchartsHelper.LOGGER.trace("init standard objects");
             if (SVGHighchartsHelper.DEBUG) {
                 sharedScope = cx.initStandardObjects(org.mozilla.javascript.tools.shell.Main.getGlobal());
             }
 
             SVGHighchartsHelper.LOGGER.trace("set optimization level to -1");
             cx.setOptimizationLevel(-1);// bypass the 64k limit // interpretive mode
 
             attachJs(cx, "env.rhino-1.2.js");
 
             if (!SVGHighchartsHelper.DEBUG) {
                 SVGHighchartsHelper.LOGGER.trace("set optimization level to 9");
                 cx.setOptimizationLevel(9);
             }
 
             attachJs(cx, "jquery-1.4.3.min.js");
             attachJs(cx, "highcharts-2.1.2.src.js");
             attachJs(cx, "exporting-2.1.2.src.js");
             attachJs(cx, "svg-renderer-highcharts-2.1.2.js");
             attachJs(cx, "add-BBox.js");
             
             SVGHighchartsHelper.LOGGER.trace("set langage version to 1.6");
             cx.setLanguageVersion(Context.VERSION_1_6);
 
             for (Script s: scripts) {
                 s.exec(cx, sharedScope);
             }
             return null;
         }
         
         @SuppressWarnings("deprecation")
         private void attachJs(Context cx, String jsFileName) {
             InputStream in = null;
             InputStreamReader reader = null;
             SVGHighchartsHelper.LOGGER.debug("loading " + jsFileName);
             try {
                 in = SVGHighchartsHelper.class.getResourceAsStream(jsFileName);
                 if (in == null) {
                     throw new RuntimeException("cannot find js file : " + jsFileName);
                 }
 
                 reader = new InputStreamReader(in);
                 if (scripts == null) {
                     scripts = new ArrayList<Script>(); 
                 }
                 scripts.add(cx.compileReader(sharedScope, reader, jsFileName, 1, null));
                 SVGHighchartsHelper.LOGGER.debug("loaded " + jsFileName);
             } catch (IOException e) {
                 throw new RuntimeException("cannot load js file : " + jsFileName, e);
             } finally {
                 IOUtils.closeQuietly(in);
                 IOUtils.closeQuietly(reader);
             }
         }
     }
     
     private static class SVGRendererContextAction implements ContextAction {
         
         String chartOptions;
         
         public SVGRendererContextAction(String chartOptions) {
             this.chartOptions = chartOptions;
         }
         
         @Override
         public Object run(Context context) {
             if (SVGHighchartsHelper.DEBUG) {
                 launchRhinoDebugger();
             }
             Scriptable newScope = context.newObject(sharedScope);
             newScope.setPrototype(sharedScope);
             newScope.setParentScope(null);
 
             SVGHighchartsHelper.LOGGER.trace("call renderSVG method on json");
             if (SVGHighchartsHelper.LOGGER.isTraceEnabled()) {
                 SVGHighchartsHelper.LOGGER.trace("\n" + chartOptions);
             }
             if (SVGHighchartsHelper.DEBUG) {
                 SVGHighchartsHelper.rhinoDebugger.doBreak();
             }
             try {
                 return callJavascript(newScope, chartOptions);
             } finally {
                 if (SVGHighchartsHelper.DEBUG) {
                     SVGHighchartsHelper.rhinoDebugger.detach();
                     SVGHighchartsHelper.rhinoDebugger.dispose();
                 }
             }
         }
         
         protected Object callJavascript(Scriptable scope, String chartOptions) {
             return ScriptableObject.callMethod(null, scope, "renderSVGFromJson",
                 new Object [] {chartOptions});
         }
         
         private void launchRhinoDebugger() {
             SVGHighchartsHelper.LOGGER.debug("launching rhino debugger");
             SVGHighchartsHelper.rhinoDebugger = new Main("rhino-debugger");
 
             SVGHighchartsHelper.rhinoDebugger.attachTo(contextFactory);
             SVGHighchartsHelper.rhinoDebugger.setScopeProvider(new ScopeProvider() {
                 @Override
                 public Scriptable getScope() {
                     return org.mozilla.javascript.tools.shell.Main.getGlobal();
                 }
             });
 
             SVGHighchartsHelper.rhinoDebugger.pack();
             SVGHighchartsHelper.rhinoDebugger.setSize(1600, 1000);
             SVGHighchartsHelper.rhinoDebugger.setVisible(true);
         }
     }
     
     public String getSVG(final String chartOptions) throws IOException {
         SVGHighchartsHelper.LOGGER.trace("get svg for highcharts export functions with rhino");
         
         Object call = contextFactory.call(new SVGRendererContextAction(chartOptions));
         if (call == null) {
             throw new NullPointerException("problem during svg generation");
         }
             
         String svg = call.toString();
        //post treatment
         svg = svg.substring(svg.indexOf("<svg"), svg.indexOf("</div>"));
        svg = "<?xml version=\"1.0\"?>\n"
             + "<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\""
             + " \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">\n"
             + svg;
         SVGHighchartsHelper.LOGGER.trace("svg rendered : \n" + svg);
         return svg;
     }
 
 }
