 package org.otherobjects.cms.util;
 
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.lang.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Class to create String representations of bean style objects. The toString and toHtml methods will introspect the object given and find all
  * read accessors and get their values. It will iterate arrays and collections and will then create either a string or an html snippet containing
  * everything found.
  * @author joerg
  *
  */
 @SuppressWarnings("unchecked")
 public class ObjectInspector
 {
     private static final Logger LOGGER = LoggerFactory.getLogger(ObjectInspector.class);
 
     private static final int ABBREVIATION_MAX = 100;
 
     public static final Pattern READ_ACCESSOR_PATTERN = Pattern.compile("^(?:(?:get)|(?:is))(\\w{1})(.*)$");
 
     public static String toString(Object object, boolean multiline)
     {
        if(object == null)
            return "null";
        
         StringBuffer buf = new StringBuffer();
         Map<String, Object> contentMap = getContent(object);
         for (Map.Entry<String, Object> entry : contentMap.entrySet())
         {
             buf.append(entry.getKey());
             buf.append(": ");
             Object value = preprocessValue(entry.getValue());
            if(value==null)
            {
                buf.append(value);                
            }
            else if (List.class.isAssignableFrom(value.getClass())) // we have a list
             {
                 List<String> values = (List<String>) value;
                 buf.append("{");
                 for (String simpleValue : values)
                 {
                     buf.append("[");
                     buf.append(simpleValue);
                     buf.append("]");
                 }
                 buf.append("}");
             }
             else
             {
                 buf.append(value);
             }
             if (multiline)
                 buf.append("\n");
         }
         return buf.toString();
     }
 
     public static String toString(Object object)
     {
         return toString(object, false);
     }
 
     public static String toHtml(Object object)
     {
         StringBuffer buf = new StringBuffer();
         buf.append("<div>\n");
         buf.append("<ul>\n");
         Map<String, Object> contentMap = getContent(object);
         for (Map.Entry<String, Object> entry : contentMap.entrySet())
         {
            buf.append("<li><span class=\"fieldName\">");
             buf.append(entry.getKey());
             buf.append(":</span>");
             Object value = preprocessValue(entry.getValue());
            if (value !=null && List.class.isAssignableFrom(value.getClass())) // we have a list
             {
                 List<String> values = (List<String>) value;
                 buf.append("<ol>");
                 for (String simpleValue : values)
                 {
                     buf.append("<li><span class=\"fieldValue\">");
                     buf.append(simpleValue);
                     buf.append("</span></li>");
                 }
                 buf.append("</ol>");
             }
             else
             // single value
             {
                 buf.append(" \n<span class=\"fieldValue\">");
                 buf.append(value);
                 buf.append("</span>");
             }
             buf.append("</li>\n");
         }
         buf.append("</ul>\n");
         buf.append("</div>\n");
         return buf.toString();
     }
 
     /**
      * Preprocesses the given object and always returns either a string or a list of strings.
      * @param value
      * @return
      */
     private static Object preprocessValue(Object value)
     {
        if(value == null)
            return null;
        
         Class<?> clazz = value.getClass();
         //Class componentClass = clazz.getComponentType();
 
         if (clazz.getName().equals(String.class.getName()))
         {
             return StringUtils.abbreviate(value.toString(), ABBREVIATION_MAX);
         }
         else if (clazz.isArray()) // we are looking at an array
         {
             List<String> strings = new ArrayList<String>();
             Object[] values = (Object[]) value;
             for (Object simpleValue : values)
             {
                 strings.add(simpleValue.toString());
             }
             return strings;
         }
         else if (Collection.class.isAssignableFrom(clazz))
         {
             List<String> strings = new ArrayList<String>();
             for (Object entry : (Collection) value)
             {
                strings.add(entry != null ? entry.toString() : "null");
             }
             return strings;
         }
         else
         {
             return value.toString();
         }
     }
 
     private static Map<String, Object> getContent(Object object)
     {
         Map<String, Object> contentMap = new HashMap<String, Object>();
 
         Method[] methods = object.getClass().getMethods();
 
         for (int i = 0; i < methods.length; i++)
         {
             Matcher matcher = READ_ACCESSOR_PATTERN.matcher(methods[i].getName());
             if (matcher.matches())
             {
                 try
                 {
                     Object value = methods[i].invoke(object);
                     String fieldname = matcher.group(1).toLowerCase() + matcher.group(2);
                     contentMap.put(fieldname, value);
 
                 }
                 catch (Exception e)
                 {
                     LOGGER.debug("Couldn't inspect accessor method", e);
                 }
             }
         }
 
         return contentMap;
     }
 
 }
