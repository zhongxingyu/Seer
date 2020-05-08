 package com.joshondesign.treegui.modes.amino;
 
 import com.joshondesign.treegui.Binding;
 import com.joshondesign.treegui.docmodel.Group;
 import com.joshondesign.treegui.docmodel.SketchNode;
 import java.lang.reflect.Method;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import org.joshy.gfx.util.u;
 
 public class AminoAdapter {
     private static List<String> skipList;
 
     static {
         String[] skipListArray = {"class","inputBounds","size","constraint"};
         skipList = Arrays.asList(skipListArray);
     }
     public static String getScriptClass(SketchNode node) {
         if(node instanceof Button) return "PushButton";
         if(node instanceof Rect) return "Rect";
         if(node instanceof Slider) return "Slider";
         if(node instanceof Group) return "Group";
         if(node instanceof ListView) return "ListView";
         if(node instanceof StringListModel) return "ListModel";
        if(node instanceof Textbox) return "Textbox";
        if(node instanceof Image) return "ImageView";
         return "Rect";
     }
 
     public static Map<String,Object> getProps(SketchNode node) {
         Map<String,Object> props = new HashMap<String, Object>();
         Class<SketchNode> clazz = (Class<SketchNode>) node.getClass();
         Method[] methods = clazz.getMethods();
         for(Method m : methods) {
             if(!m.getName().startsWith("get")) continue;
             if(m.getName().equals("get")) continue;
 
             try {
                 String name = m.getName().substring(3,4).toLowerCase()+m.getName().substring(4);
                 if(skipList.contains(name)) continue;
                 //if(node instanceof Slider && name.equals("width")) continue;
                 if(node instanceof Slider && name.equals("height")) continue;
                 if(node instanceof Button && name.equals("width")) continue;
                 if(node instanceof Button && name.equals("height")) continue;
                 if(!node.isVisual()) {
                     if(name.equals("translateX")) continue;
                     if(name.equals("translateY")) continue;
                 }
                 if(node instanceof StringListModel) {
                     if(name.equals("this")) continue;
                     if(name.equals("x")) continue;
                     if(name.equals("y")) continue;
                 }
                if(node instanceof Image) {
                    if(name.equals("width")) continue;
                    if(name.equals("height")) continue;
                }
 
                 Object value = m.invoke(node);
                 props.put(name,value);
             } catch (Throwable e) {
                 u.p("failed at : " + m.getName());
                 e.printStackTrace();
             }
         }
         return props;
     }
 
     public static boolean isDataModel(Binding binding) {
         if(binding.getSource() instanceof StringListModel) {
             return true;
         }
         return false;
     }
 }
