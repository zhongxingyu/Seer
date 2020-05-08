 package amu.licence.edt.view.renderers;
 
 import java.util.HashMap;
 import java.util.Map;
 
 public class ClassBasedDDR implements StrRenderer {
 
     protected Map<Class<?>, StrRenderer> renderers;
 
     public ClassBasedDDR() {
         this.renderers = new HashMap<Class<?>, StrRenderer>();
     }
 
     @Override
     public String getStrRender(Object obj) {
 
         // /!\ inheritance not handled, specific class only
         StrRenderer r;
        if (obj == null) {
            return "null";
        } else if ((r = renderers.get(obj.getClass())) == null) {
             return obj.toString();
         }
 
         return r.getStrRender(obj);
     }
 
     public void addRenderer(Class<?> clazz, StrRenderer renderer) {
         renderers.put(clazz, renderer);
     }
 
     public void removeRenderer(Class<?> clazz) {
         renderers.remove(clazz);
     }
 }
