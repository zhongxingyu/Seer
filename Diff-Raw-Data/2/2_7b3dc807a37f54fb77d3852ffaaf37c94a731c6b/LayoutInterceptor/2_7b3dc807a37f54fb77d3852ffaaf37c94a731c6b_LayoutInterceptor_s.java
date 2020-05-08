 package com.dianping.wizard.widget.interceptor;
 
 import com.dianping.wizard.config.Configuration;
 import com.dianping.wizard.exception.WidgetException;
 import com.dianping.wizard.repo.*;
 import com.dianping.wizard.script.ScriptEngine;
 import com.dianping.wizard.script.ScriptEngineFactory;
 import com.dianping.wizard.widget.*;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.Future;
 import java.util.concurrent.TimeUnit;
 
 /**
  * @author ltebean
  */
 public class LayoutInterceptor implements Interceptor {
 
     private final WidgetRepo widgetRepo = WidgetRepoFactory.getRepo("default");
 
     private final LayoutRepo layoutRepo = LayoutRepoFactory.getRepo("default");
 
     private final WidgetRenderer renderer = WidgetRendererFactory.getRenderer("default");
 
     private final ScriptEngine engine = ScriptEngineFactory.getEngine("default");
 
     private final Logger logger = Logger.getLogger(this.getClass());
 
     private final int timeout = Configuration.get("concurrent.timeout", 1000, Integer.class);
 
 
     @Override
     public String intercept(InvocationContext invocation) throws Exception {
         Widget widget = invocation.getWidget();
         if (StringUtils.isNotEmpty(widget.layoutRule)) {
             widget.layoutName = (String) engine.eval(widget.layoutRule, invocation.getContext());
         }
         if (StringUtils.isNotEmpty(widget.layoutName)) {
             Layout layout = layoutRepo.loadByName(widget.layoutName);
             if(layout==null){
                 throw new WidgetException("layout not found: "+widget.layoutName);
             }
             Map<String, Object> param = (Map<String, Object>) invocation.getContext().get("param");
             Map<String, Future<RenderingResult>> tasks = (Map<String, Future<RenderingResult>>) param.get("CONCURRENT_TASKS");
 
             ResultWrapper wrapper = renderComponentsFromPool(layout, invocation.getModeType(), param,tasks);
 
             invocation.getContext().put("layout", wrapper.output);
             invocation.setScript(invocation.getScript() + wrapper.script);
         }
         return invocation.invoke();
     }
 
     private ResultWrapper renderComponentsFromPool(Layout layout, String mode, Map<String, Object> param, Map<String, Future<RenderingResult>> tasks) {
         ResultWrapper wrapper = new ResultWrapper();
         StringBuilder scriptBuilder = new StringBuilder();
         for (Map.Entry<String, List<String>> entry : layout.config.entrySet()) {
             String colKey = entry.getKey();
             StringBuilder builder = new StringBuilder();
             for (String widgetName : entry.getValue()) {
                 Widget widget = widgetRepo.loadByName(widgetName);
                 if (widget.modes.get(mode) == null) {
                     continue;
                 }
                 RenderingResult result = null;
                 if(tasks==null||tasks.get(widgetName)==null){
                     result = renderer.render(widget, mode, param);
                 }else{
                     try {
                         result = tasks.get(widgetName).get(timeout, TimeUnit.MILLISECONDS);
                     } catch (Exception e) {
                        String msg="widget:" + widget + " timeout";
                         logger.error(msg, new WidgetException(msg,e));
                     }
                 }
 
                 if (result != null && StringUtils.isNotEmpty(result.output)) {
                     builder.append(result.output);
                 }
 
                 if (result != null && StringUtils.isNotEmpty(result.script)) {
                     scriptBuilder.append(result.script);
                 }
             }
             wrapper.output.put(colKey, builder.toString());
         }
         wrapper.script = scriptBuilder.toString();
         return wrapper;
     }
 
 }
