 package com.huanwuji.json.flexjson.impl;
 
 import com.huanwuji.json.flexjson.PropertyFilter;
 import flexjson.BeanProperty;
 import flexjson.JSONContext;
 import flexjson.Path;
 import flexjson.TypeContext;
 import org.apache.commons.lang.StringUtils;
 
 /**
  * Created with IntelliJ IDEA.
  * <p/>
  * User: juyee
  * Date: 12-9-5
  * Time: 上午11:02
  * To change this template use File | Settings | File Templates.
  */
 public class BasicPropertyFilter implements PropertyFilter {
 
     private String reg;
 
     private boolean filterNotBasicObj;
 
     public BasicPropertyFilter(String reg) {
         this(reg, false);
     }
 
     public BasicPropertyFilter(boolean filterNotBasicObj) {
         this(null, filterNotBasicObj);
     }
 
     public BasicPropertyFilter(String reg, boolean filterNotBasicObj) {
         this.reg = reg;
         this.filterNotBasicObj = filterNotBasicObj;
     }
 
     public boolean isFilter(BeanProperty prop, Path path, Object object, JSONContext context, TypeContext typeContext) {
         if (reg != null) {
             return StringUtils.join(path.getPath(), ".").matches(reg);
         }
        return !filterNotBasicObj || !prop.getPropertyType().getName().startsWith("java");
     }
 }
