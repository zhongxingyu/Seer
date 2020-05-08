 package com.dianping.wizard.widget.concurrent;
 
 import com.dianping.wizard.exception.WidgetException;
 import com.dianping.wizard.exception.WizardExeption;
 import com.dianping.wizard.repo.LayoutRepo;
 import com.dianping.wizard.repo.LayoutRepoFactory;
 import com.dianping.wizard.repo.WidgetRepo;
 import com.dianping.wizard.repo.WidgetRepoFactory;
 import com.dianping.wizard.script.ScriptEngine;
 import com.dianping.wizard.script.ScriptEngineFactory;
 import com.dianping.wizard.widget.*;
 import org.apache.commons.lang.StringUtils;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Future;
 
 /**
  * @author ltebean
  */
 public class LayoutParser {
 
     private final static ScriptEngine engine= ScriptEngineFactory.getEngine("default");
 
     private final static WidgetRepo widgetRepo= WidgetRepoFactory.getRepo("default");
 
     private final static LayoutRepo layoutRepo= LayoutRepoFactory.getRepo("default");
 
     private final static ExecutorService executorService = Executor.getInstance();
 
     public static Map<String,Future<RenderingResult>> parseAndExecute(Widget widget,String mode,Map<String,Object> context){
         Map<String,Future<RenderingResult>> result=new HashMap<String, Future<RenderingResult>>();
         HashMap<String,Object> param=  (HashMap<String,Object>)context.get("param");
         //if the widget does not has layout, execute it and return the result;
         if(!hasLayout(widget)){
             result.put(widget.name,executorService.submit(new RenderingTask(widget,mode,param)));
             return result;
         }
         //evaluate the rule to find the layout
         if(StringUtils.isNotEmpty(widget.layoutRule)){
            widget.layoutName=(String)engine.eval(widget.layoutRule,context);
         }
         if(StringUtils.isNotEmpty(widget.layoutName)){
             Layout layout=layoutRepo.loadByName(widget.layoutName);
             if(layout==null){
                 throw new WizardExeption("layout not found: "+widget.layoutName);
             }
             for(Map.Entry<String,List<String>> entry : layout.config.entrySet()) {
                 for(String widgetName:entry.getValue()){
                     Widget w=widgetRepo.loadByName(widgetName);
                     if(w==null){
                         throw new WidgetException("widget not found: "+widgetName);
                     }
                     if(hasLayout(w)){
                         result.putAll(parseAndExecute(w,mode,context));
                     }else{
                         result.put(w.name,executorService.submit(new RenderingTask(w,mode,param)));
                     }
                 }
             }
         }
         return result;
     }
 
     private static boolean hasLayout(Widget widget){
         return (StringUtils.isNotEmpty(widget.layoutName)||StringUtils.isNotEmpty(widget.layoutRule));
     }
 
 }
