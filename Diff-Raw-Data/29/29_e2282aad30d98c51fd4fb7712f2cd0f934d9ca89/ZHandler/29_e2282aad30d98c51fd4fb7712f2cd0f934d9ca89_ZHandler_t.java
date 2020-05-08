 package com.ztools.xml;
 
 import java.lang.reflect.Array;
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 
 public class ZHandler extends AbsHandler {
 
     /**
      * 
      */
     private static final long serialVersionUID = 1L;
 
     private String rootName;
 
     private List<Object> tempItemObject = new ArrayList<Object>();
     private String tempItemName;
     private Object currObject;
     // private int oldDepth;
     private boolean isBaseType = false;
 
     // private boolean isStartElement = true;
     private boolean isEndElement = false;
     private IXMLProcess iXmlProcess = null;
 
     private Object bean;
 
    private String defaultClassName = null;

     private String charset;
 
     private boolean isRoot = true;
     private int depth;
 
     private Map<String, Object> objMap = new HashMap<String, Object>();
 
     public String getCharset() {
         return charset;
     }
 
     public void setCharset(String charset) {
         this.charset = charset;
     }
 
     // public String getPath() {
     // return path;
     // }
     //
     // public void setPath(String path) {
     // this.path = path;
     // }
 
     public ZHandler() {
         super();
     }
 
     // public ZHandler(String filename) {
     // this.path = filename;
     // }
 
     @Override
     public void startDocument() throws SAXException {
         // this.rootName = this.xmlBean.getRootName();
         // System.out.println("startDocument...");
         // //TODO
         // iXmlProcess = XMLProcessFactory.getXmlProcess(rootName);
     }
 
     private Field[] getFields(Class<?> c) {
         Field[] ret = null;
         if (null != c) {
             Field[] temp;
             do {
                 Field[] cfs = c.getDeclaredFields();
                 if (null == ret) {
                     ret = cfs;
                 } else {
                     temp = new Field[ret.length + cfs.length];
                     System.arraycopy(ret, 0, temp, 0, ret.length);
                     System.arraycopy(cfs, 0, temp, ret.length, cfs.length);
                     ret = temp;
                 }
                 c = c.getSuperclass();
             } while (null != c);
         }
         return ret;
     }
 
     private Object encapsulateObject(Attributes attributes, Class<?> c) {
         if (null != c && null != attributes) {
             Field[] fs = getFields(c);
             if (null == fs) {
                 return null;
             }
             try {
                 Object obj = c.newInstance();
                 for (int i = 0; i < fs.length; i++) {
                     String value = attributes.getValue(fs[i].getName());
                     if (null != value && !"".equals(value)) {
                         String methodName = getMethodName(fs[i].getName());
                         if ("Boolean".equalsIgnoreCase(fs[i].getType()
                                 .getSimpleName())) {
                             if (0 <= fs[i].getName().toLowerCase()
                                     .indexOf("is")) {
                                 methodName = getMethodName(fs[i].getName()
                                         .substring(2));
                             }
                         }
                         Method m = c.getMethod(methodName, fs[i].getType());
                         Object p = null;
                         Class<?> type = fs[i].getType();
                         p = parseValue(value, type);
                         if (null != p) {
                             m.invoke(obj, p);
                         }
                     }
                 }
                 return obj;
             } catch (InstantiationException e) {
                 e.printStackTrace();
             } catch (IllegalAccessException e) {
                 e.printStackTrace();
             } catch (SecurityException e) {
                 e.printStackTrace();
             } catch (NoSuchMethodException e) {
                 e.printStackTrace();
             } catch (IllegalArgumentException e) {
                 e.printStackTrace();
             } catch (InvocationTargetException e) {
                 e.printStackTrace();
             }
         }
         return null;
     }
 
     @Override
     public void startElement(String uri, String localName, String qName,
             Attributes attributes) throws SAXException {
 
         // System.out.println(qName + " " + depth);
 
         if (isRoot) {
             rootName = qName;
             isRoot = false;
             iXmlProcess = XMLProcessFactory.getXmlProcess(rootName);
            defaultClassName = attributes.getValue("class");
         }
         // is root
         if (this.rootName.equals(qName)) {
             this.tempItemName = qName;
             depth++;
             return;
         }
 
         this.tempItemName = qName;
         tempHashcode = attributes.getValue("hashcode");
         try {
             String className = attributes.getValue("class");
             if (null == className && "".equals(className)) {
                 // set default class name
                 className = Object.class.getName();// this.bean.getClass().getName();
                                                    // //
                                                    // this.bean.getBeanClass().getName();
             }

            if (null == className) {
                className = defaultClassName;
            }
             Class<?> c = Class.forName(className);
             currObject = parseValue("0", c);
             if (c.isArray()) {
                 currObject = Array.newInstance(c.getComponentType(), 0);
             } else if (null == currObject) {
                 isBaseType = false;
                 currObject = encapsulateObject(attributes, c);
                 String hashcode = attributes.getValue("hashcode");
                 if (null != hashcode && !"".equals(hashcode)) {
                     Object obj = objMap.get(hashcode);
                     if (null != obj) {
                         currObject = obj;
                     } else {
                         objMap.put(hashcode, currObject);
                     }
                 }
             } else {
 
                 if ("0".equals(tempHashcode)) {
                     currObject = parseValue(null, c);
                 } else {
                     Object obj = objMap.get(tempHashcode);
                     if (null != obj)
                         currObject = obj;
                 }
                 isBaseType = true;
             }
 
         } catch (ClassNotFoundException e) {
             e.printStackTrace();
         }
 
         int tempItemIndex = depth - 1;
         if (tempItemIndex < this.tempItemObject.size()) {
             this.tempItemObject.set(tempItemIndex, currObject);
         } else {
             this.tempItemObject.add(tempItemIndex, currObject);
         }
         isEndElement = false;
         depth++;
     }
 
     private String tempHashcode = null;
 
     @Override
     public void characters(char[] ch, int start, int length)
             throws SAXException {
         if (0 < length && isBaseType) {
             String s = new String(ch).substring(start, start + length);
             currObject = parseValue(s, currObject.getClass());
             if (null != tempHashcode && !"".equals(tempHashcode)) {
                 objMap.put(tempHashcode, currObject);
             }
             int tempItemIndex = depth - 1 - 1;
             tempItemObject.set(tempItemIndex, currObject);
         }
     }
 
     private Object parseValue(String value, Class<?> type) {
         Object p = null;
         try {
             value = iXmlProcess.read(value);
             if (type.equals(String.class)) {
                 p = null != value ? value : "";
             } else if (type.equals(Double.class)
                     || "double".equals(type.getSimpleName())) {
                 p = null != value ? Double.parseDouble(value) : 0.00d;
             } else if (type.equals(Float.class)
                     || "float".equals(type.getSimpleName())) {
                 p = null != value ? Float.parseFloat(value) : 0.0f;
             } else if (type.equals(Long.class)
                     || "long".equals(type.getSimpleName())) {
                 p = null != value ? Long.parseLong(value) : 0l;
             } else if (type.equals(Integer.class)
                     || "int".equals(type.getSimpleName())) {
                 p = null != value ? Integer.parseInt(value) : 0;
             } else if (type.equals(Short.class)
                     || "short".equals(type.getSimpleName())) {
                 p = null != value ? Short.parseShort(value) : 0;
             } else if (type.equals(Boolean.class)
                     || "boolean".equals(type.getSimpleName())) {
                 p = null != value ? Boolean.parseBoolean(value) : false;
             } else if (type.equals(Date.class)) {
                 p = DateFormat.getDateInstance(0, Locale.getDefault()).parse(
                         value);
             } else if (type.equals(Character.class)
                     || "char".equals(type.getSimpleName())) {
                 if (null != value && value.length() > 0) {
                     p = value.charAt(0);
                 } else {
                     p = new Character('0');
                 }
             } else if (type.equals(Byte.class)
                     || "byte".equals(type.getSimpleName())) {
                 p = null != value ? Byte.parseByte(value) : 0;
             }
         } catch (ParseException e) {
             p = new Date();
         } catch (NumberFormatException e) {
             p = 0;
         }
         return p;
     }
 
     private String getMethodName(String fieldName) {
         String methodName = fieldName;
         methodName = "set".concat(methodName.substring(0, 1).toUpperCase())
                 .concat(methodName.substring(1));
         return methodName;
     }
 
     @Override
     public void endElement(String uri, String localName, String qName)
             throws SAXException {
         // System.out.println(qName + " " + depth-- + " depth " + oldDepth);
 
         if (rootName.equals(qName)) {
             return;
         }
         depth--;
 
         isBaseType = false;
         if (!qName.equals(tempItemName) && isEndElement) {
             // this.oldDepth--;
             tempItemName = qName;
         }
 
         int tempItemIndex = depth - 1;
 
         if (this.tempItemObject.size() > tempItemIndex && tempItemIndex > 0) {
             Object obj = this.tempItemObject.get(tempItemIndex - 1);
             Object curr = this.tempItemObject.get(tempItemIndex);
             if (obj.getClass().isArray()) {
                 int length = Array.getLength(obj);
                 Object arr = Array.newInstance(obj.getClass()
                         .getComponentType(), length + 1);
                 for (int i = 0; i < length; i++) {
                     Array.set(arr, i, Array.get(obj, i));
                 }
                 Array.set(arr, length, curr);
                 this.tempItemObject.set(tempItemIndex - 1, arr);
             } else if (obj instanceof Map<?, ?>) {
                Map<Object, Object> map = (Map<Object, Object>) obj;
                 map.put(qName, curr);
             } else if (obj instanceof Collection<?>) {
                 Collection<Object> c = (Collection<Object>) obj;
                 c.add(curr);
                 this.tempItemObject.set(tempItemIndex - 1, c);
             } else if (null != obj) {
                 // String methodName = getMethodName(qName);
 
                 Method m = null;
                 try {
                     Method[] ms = obj.getClass().getMethods();
                     for (int i = 0; i < ms.length; i++) {
                         if (0 == ms[i].getName().toLowerCase()
                                 .indexOf("set" + qName.toLowerCase())) {
                             m = ms[i];
                             break;
                         }
                     }
                     // m = obj.getClass().getMethod(methodName,
                     // curr.getClass());
                     if (null != m) {
                         m.invoke(obj, new Object[] { curr });
                         this.tempItemObject.set(tempItemIndex - 1, obj);
                     }
                 } catch (SecurityException e) {
                     e.printStackTrace();
                     // } catch (NoSuchMethodException e) {
                     // e.printStackTrace();
                 } catch (IllegalArgumentException e) {
                     e.printStackTrace();
                 } catch (IllegalAccessException e) {
                     e.printStackTrace();
                 } catch (InvocationTargetException e) {
                     e.printStackTrace();
                 }
 
             }
         }
         // System.err.println(qName + " depth: " + depth + " obj: "
         // + this.tempItemObject);
         isEndElement = true;
         // isStartElement = false;
         // depth--;
     }
 
     public Object getBean() {
         return bean;
     }
 
     public void setBean(Object bean) {
         this.bean = bean;
     }
 
     @Override
     public void endDocument() throws SAXException {
         int tempItemIndex = depth - 1;
         Object obj = (this.tempItemObject.get(tempItemIndex));
         if (null != obj) {
             this.setBean(obj);
         }
     }
 
     @Override
     public Object getXmlObject() {
         return getBean();
     }
 
 }
