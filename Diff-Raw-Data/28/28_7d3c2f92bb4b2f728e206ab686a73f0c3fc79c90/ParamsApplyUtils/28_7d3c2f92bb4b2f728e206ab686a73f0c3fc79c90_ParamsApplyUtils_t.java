 package org.n3r.core.util;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.lang3.StringUtils;
 import org.n3r.core.joor.Reflect;
 import org.n3r.core.lang.RStr;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Splitter;
 import com.google.common.collect.Iterables;
 
 public class ParamsApplyUtils {
     private static Logger log = LoggerFactory.getLogger(ParamsApplyUtils.class);
     private static final Pattern paramParams = Pattern
            .compile("\\w+([.$]\\w+)*\\s*(\\(\\s*\\w+\\s*(,\\s*\\w+\\s*)*\\))?");
 
     /**
      * 根据形如com.ailk.xxx.yyy(a123,b23)的字符串，生成对象。
      * 如果该对象实现ExtraInfoSetter接口，则将括弧中的额外信息设置到对象中。
      * @param propertyValue String
      * @return List<Object>
      */
     @SuppressWarnings("unchecked")
     public static <T> List<T> createObjects(String propertyValue, Class<? super T> cls) {
         List<T> lst = new ArrayList<T>();
         if (StringUtils.isEmpty(propertyValue)) return lst;
 
         Matcher matcher = paramParams.matcher(propertyValue);
         Splitter splitter = Splitter.on(',').trimResults();
 
         while (matcher.find()) {
            String group = matcher.group().trim();
             int posBrace = group.indexOf('(');
             String functor = posBrace < 0 ? group : group.substring(0, posBrace);
            Object obj = Reflect.on(StringUtils.trim(functor)).create().get();
             if (!cls.isInstance(obj)) {
                 log.warn("{} can not instantized to {}", functor, cls.getName());
                 continue;
             }
 
             lst.add((T) obj);
 
             if (obj instanceof ParamsAppliable)
                 ((ParamsAppliable) obj).applyParams(posBrace <= 0 ? new String[] {}
                        : Iterables.toArray(splitter.split(RStr.substrInQuotes(group, '(', posBrace)), String.class));
         }
 
         return lst;
     }
 }
