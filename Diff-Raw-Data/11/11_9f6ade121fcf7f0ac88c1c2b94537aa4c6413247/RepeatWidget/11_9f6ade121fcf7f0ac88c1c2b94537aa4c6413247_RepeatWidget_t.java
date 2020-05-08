 package com.wideplay.warp.widgets;
 
 import com.wideplay.warp.util.TextTools;
 import com.wideplay.warp.widgets.rendering.EmbedAs;
 import net.jcip.annotations.Immutable;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * @author Dhanji R. Prasanna (dhanji@gmail.com)
  */
 @Immutable
 @EmbedAs("Repeat")
 class RepeatWidget implements Renderable {
     private final WidgetChain widgetChain;
     private final String items;
     private final String var;
     private final String pageVar;
     private final Evaluator evaluator;
     
     private static final String DEFAULT_PAGEVAR = "__page";
    private static final String DEFAULT_VAR = "__this";
 
     public RepeatWidget(WidgetChain widgetChain, String expression, Evaluator evaluator) {
         this.widgetChain = widgetChain;
 
         final Map<String,String> map = TextTools.toBindMap(expression);
         this.items = map.get("items");
         String var = map.get("var");
 
         if (null != var)
             this.var = TextTools.stripQuotes(var);
         else
            this.var = DEFAULT_VAR;
 
         //by default the page comes in as __page
         String pageVar = map.get("pageVar");
         if (null == pageVar)
             pageVar = DEFAULT_PAGEVAR;
         else
             pageVar = TextTools.stripQuotes(pageVar);
 
         this.pageVar = pageVar;
 
         this.evaluator = evaluator;
     }
 
     public void render(Object bound, Respond respond) {
         Collection<?> things = (Collection<?>) evaluator.evaluate(items, bound);
 
         //do nothing if the collection is unavailable for some reason
         if (null == things)
             return;
 
         Map<String, Object> context = new HashMap<String, Object>(3);
 
         //set up context variables
         for (Object thing : things) {
 
             //decorate with some context
             if (null != var)
                 context.put(var, thing);
             
             context.put(pageVar, bound);
             widgetChain.render(context, respond);
         }
 
     }
 
 
     public <T extends Renderable> Set<T> collect(Class<T> clazz) {
         return widgetChain.collect(clazz);
     }
 
 
 }
