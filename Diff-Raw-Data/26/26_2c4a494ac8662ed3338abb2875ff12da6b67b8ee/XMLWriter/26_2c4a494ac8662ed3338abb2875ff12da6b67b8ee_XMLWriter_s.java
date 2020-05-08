 package com.ztools.xml;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.io.Serializable;
 import java.lang.reflect.Array;
 import java.lang.reflect.Method;
 import java.nio.charset.Charset;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 public class XMLWriter implements Serializable {
 
     /**
 	 * 
 	 */
     private static final long serialVersionUID = 7236323314960264585L;
 
     private static final String END_LINE = "\r\n";
 
     private static final String CDATA_PREF = "<![CDATA[";
 
     private static final String CDATA_END = "]]>";
 
     private static IXMLProcess iXmlProcess = null;
 
     // excluding the surrogate blocks
     // [#x1FFFE-#x1FFFF], [#x2FFFE-#x2FFFF], [#x3FFFE-#x3FFFF],
     // [#x4FFFE-#x4FFFF], [#x5FFFE-#x5FFFF], [#x6FFFE-#x6FFFF],
     // [#x7FFFE-#x7FFFF], [#x8FFFE-#x8FFFF], [#x9FFFE-#x9FFFF],
     // [#xAFFFE-#xAFFFF], [#xBFFFE-#xBFFFF], [#xCFFFE-#xCFFFF],
     // [#xDFFFE-#xDFFFF], [#xEFFFE-#xEFFFF], [#xFFFFE-#xFFFFF],
     // [#x10FFFE-#x10FFFF].
     private static final int[] EXCLUDING_SURROGATE_BLOCK = { 0xFFFE, 0xFFFF };
     private static final int EXCLUDING_MASK = 0xFFFF;
     // restricted chars
     // [#x1-#x8], [#xB-#xC], [#xE-#x1F], [#x7F-#x84], [#x86-#x9F],
     // [#xFDD0-#xFDDF],
     private static final int[][] RESTRICTED_CHAR_BLOCKS = { { 0x0, 0x8 },
             { 0xB, 0xC }, { 0xE, 0x1F }, { 0x7F, 0x84 }, { 0x86, 0x9F },
             { 0xFDD0, 0xFDDF } };
 
     public enum XMLChars {
         LESSTHAN("&lt;", '<'), GREATERTHAN("&gt;", '>'), AMP("&amp;", '&'), APOS(
                 "&apos;", '\''), QUOT("&quot;", '"');
 
         private String xmlString;
         private char xmlChar;
 
         private XMLChars(String xmlString, char xmlChar) {
             this.xmlString = xmlString;
             this.xmlChar = xmlChar;
         }
 
         @Override
         public String toString() {
             return this.xmlChar + " " + this.xmlString;
         }
 
         public String getXmlString() {
             return xmlString;
         }
 
         public char getXmlChar() {
             return xmlChar;
         }
 
         public static XMLChars search(char c) {
             XMLChars[] xs = XMLChars.values();
             for (int i = 0; i < xs.length; i++) {
                 if (c == xs[i].getXmlChar()) {
                     return xs[i];
                 }
             }
             return null;
         }
 
     }
 
     public static String toXmlString(String str) {
 
         StringBuilder sbd = new StringBuilder();
         char[] cs = str.toCharArray();
         for (int i = 0; i < cs.length; i++) {
             boolean isRestricted = false;
 
             // [*FFFE]-[*FFFF]
             if ((cs[i] & EXCLUDING_MASK) == EXCLUDING_SURROGATE_BLOCK[0]
                     || (cs[i] & EXCLUDING_MASK) == EXCLUDING_SURROGATE_BLOCK[1]) {
                 continue;
             }
 
             for (int j = 0; j < RESTRICTED_CHAR_BLOCKS.length; j++) {
                 if (RESTRICTED_CHAR_BLOCKS[j][0] <= cs[i]
                         && cs[i] <= RESTRICTED_CHAR_BLOCKS[j][1]) {
                     isRestricted = true;
                     break;
                 }
             }
 
             if (isRestricted) {
                 sbd.append(' ');
                 continue;
             }
             sbd.append(cs[i]);
         }
         return sbd.toString();
     }
 
     /**
      * @param xmlBean
      * @throws IOException
      * @{@link Deprecated XMLWriter#writeXmlBean(XMLBean)}
      */
     @Deprecated
     public static void writeXMLBean(XMLBean xmlBean) throws IOException {
         writeXmlBean(xmlBean);
     }
 
     public static void writeObjectToXmlFile(Object obj, String path)
             throws IOException {
         if (null != obj && path != null) {
             XMLBean xmlBean = new XMLBean(path);
             xmlBean.setCharset("UTF-8");
             xmlBean.setBean(obj);
             writeXmlBean(xmlBean);
         }
     }
 
     public static String objectToXmlString(Object obj) {
         StringBuilder sbd = new StringBuilder();
         // final String END_LINE = "\r\n";
         sbd.append("<?xml version=\"1.0\" encoding=\"");
         sbd.append("UTF-8");
         sbd.append("\"?>").append(END_LINE);
         sbd.append("<?xml-stylesheet type=\"text/xsl\" href=\"style.xsl\"?>");
         sbd.append(END_LINE);
         sbd.append("<");
         sbd.append("XMLBean");
         Class<?> beanClass = obj.getClass();
         String bcs = null == beanClass ? "" : beanClass.getName();
         sbd.append(" class=\"").append(bcs);
         sbd.append("\" >").append(END_LINE);
         // TODO classforname
         iXmlProcess = XMLProcessFactory.getXmlProcess("XMLBean");
         // List<?> itemList = xmlBean.getItemList();
         Set<Object> objSet = new HashSet<Object>();
         // if (null != itemList && !itemList.isEmpty()) {
         // objectToXmlString(itemList, sbd, objSet, null);
         // } else {
         objectToXmlString(obj, sbd, objSet, null);
         // }
         sbd.append("</").append("XMLBean").append(">");
         return sbd.toString();
     }
 
     public static void writeXmlBean(XMLBean xmlBean) throws IOException {
 
         if (null != xmlBean) {
             File file = new File(xmlBean.getPath());
             if (!file.exists()) {
                 // file.mkdirs();
                 file.getParentFile().mkdirs();
                 file.createNewFile();
             }
 
             if (file.canWrite()) {
 
                 StringBuilder sbd = new StringBuilder();
                 // final String END_LINE = "\r\n";
                 sbd.append("<?xml version=\"1.0\" encoding=\"");
                 sbd.append(xmlBean.getCharset());
                 sbd.append("\"?>").append(END_LINE);
                 sbd.append("<?xml-stylesheet type=\"text/xsl\" href=\"style.xsl\"?>");
                 sbd.append(END_LINE);
                 sbd.append("<");
                 sbd.append(xmlBean.getRootName());
                 Class<?> beanClass = xmlBean.getBeanClass();
                 String bcs = null == beanClass ? "" : beanClass.getName();
                 sbd.append(" class=\"").append(bcs);
                 sbd.append("\" >").append(END_LINE);
                 // TODO classforname
                 iXmlProcess = XMLProcessFactory.getXmlProcess(xmlBean
                         .getRootName());
                 List<?> itemList = xmlBean.getItemList();
                 Set<Object> objSet = new HashSet<Object>();
                 if (null != itemList && !itemList.isEmpty()) {
                     objectToXmlString(itemList, sbd, objSet, null);
                 } else {
                     objectToXmlString(xmlBean.getBean(), sbd, objSet, null);
                 }
                 sbd.append("</").append(xmlBean.getRootName()).append(">");
                 OutputStreamWriter fw = null;
                 try {
                     fw = new OutputStreamWriter(new FileOutputStream(file),
                             Charset.forName(xmlBean.getCharset()));
                     fw.write(sbd.toString());
                     // fw.append(new
                     // String(sbd.toString().getBytes("ISO-8859-1"),"GBK"));
                     fw.flush();
                 } finally {
                     if (null != fw)
                         fw.close();
                 }
             }
         }
 
     }
 
     // public static void writeXMLBean(XMLBean xmlBean) throws IOException {
     // if (null != xmlBean) {
     // File file = new File(xmlBean.getPath());
     // if (!file.exists()) {
     // // file.mkdirs();
     // file.getParentFile().mkdirs();
     // file.createNewFile();
     // }
     //
     // if (file.canWrite()) {
     // List<?> itemList = xmlBean.getItemList();
     // int size = itemList.size();
     // StringBuilder sbd = new StringBuilder();
     // // final String END_LINE = "\r\n";
     // sbd.append("<?xml version=\"1.0\" encoding=\"");
     // sbd.append(xmlBean.getCharset());
     // sbd.append("\"?>").append(END_LINE);
     // sbd.append("<");
     // sbd.append(xmlBean.getRootName());
     // sbd.append(" class=\"")
     // .append(xmlBean.getBeanClass().getName());
     // sbd.append("\" >").append(END_LINE);
     // // TODO classforname
     // iXmlProcess = XMLProcessFactory.getXmlProcess(xmlBean
     // .getRootName());
     // for (int i = 0; i < size; i++) {
     // Set<Object> objSet = new HashSet<Object>();
     // Object obj = itemList.get(i);
     // if (null != obj)
     // objectToXmlString(obj, sbd, objSet, null);
     // // objectToXmlDom(sbd, obj, objSet, null);
     // }
     // sbd.append("</").append(xmlBean.getRootName()).append(">");
     // OutputStreamWriter fw = null;
     // try {
     // fw = new OutputStreamWriter(new FileOutputStream(file),
     // Charset.forName(xmlBean.getCharset()));
     // fw.write(sbd.toString());
     // // fw.append(new
     // // String(sbd.toString().getBytes("ISO-8859-1"),"GBK"));
     // fw.flush();
     // } finally {
     // if (null != fw)
     // fw.close();
     // }
     // }
     // }
     // }
 
     private static String parseValue(Object obj) {
         String p = null;
         if (String.class.isInstance(obj)) {
             // p = "<![CDATA[" + obj.toString() + "]]>";
             p = obj.toString();
         } else if (Integer.class.isInstance(obj)) {
             p = obj.toString();
         } else if ((Double.class.isInstance(obj))) {
             p = obj.toString();
         } else if ((Long.class.isInstance(obj))) {
             p = obj.toString();
         } else if ((Boolean.class.isInstance(obj))) {
             p = obj.toString();
         } else if ((Date.class.isInstance(obj))) {
             p = obj.toString();
         } else if ((Character.class.isInstance(obj))) {
             p = obj.toString();
         } else if ((Byte.class.isInstance(obj))) {
             p = obj.toString();
         } else if (Short.class.isInstance(obj)) {
             p = obj.toString();
         } else if (Float.class.isInstance(obj)) {
             p = obj.toString();
         }
         return p;
     }
 
     public static void objectToXmlString(Object obj, StringBuilder sbd,
             Set<Object> set, String name) {
         if (null == obj) {
             return;
         }
         if (null == set) {
             set = new HashSet<Object>();
         }
         if (null == sbd) {
             sbd = new StringBuilder();
         }
 
         if (null == name) {
             name = obj.getClass().getSimpleName();
         } else {
             int begin = 0;
             int end = 0;
             if (name.startsWith("get")) {
                 begin = 3;
                 end = 4;
             } else if (name.startsWith("is")) {
                 begin = 2;
                 end = 3;
             }
 
             name = name.substring(begin, end).toLowerCase()
                     + name.substring(end);
         }
         String tmpValue = parseValue(obj);
         if (null == tmpValue && !set.add(obj)) {
             sbd.append("<").append(name);
             sbd.append(" hashcode=\"");
             sbd.append(obj.hashCode());
             sbd.append("\" object_ref=\"true\" class=\"");
             sbd.append(obj.getClass().getName()).append("\"/>");
             return;
         }
 
         if (null != tmpValue) { // java base type
             if (String.class.equals(obj.getClass())
                     || char.class.equals(obj.getClass())
                     || Character.class.equals(obj.getClass())) {
                 tmpValue = tmpValue.replaceAll(
                         "(\\u003c!\\u005bCDATA\\u005b|\\u005d\\u005d\\u003e)",
                         " ");
                 tmpValue = CDATA_PREF + toXmlString(tmpValue) + CDATA_END;
             }
             sbd.append("<").append(name);
             sbd.append(" class=\"");
             sbd.append(obj.getClass().getName()).append("\">");
             sbd.append(iXmlProcess.write(tmpValue)).append("</");
             sbd.append(name).append(">");
             return;
         } else if (obj.getClass().isArray()) { // base array
            name = name.substring(0, name.length() - 2);
             sbd.append("<").append(name).append(" hashcode=\"");
             sbd.append(obj.hashCode());
             sbd.append("\" class=\"").append(obj.getClass().getName());
             sbd.append("\" length=\"").append(Array.getLength(obj));
             sbd.append("\">");
             Object[] coll = (Object[]) obj;
             for (int i = 0; i < coll.length; i++) {
                 objectToXmlString(coll[i], sbd, set, coll[i].getClass()
                         .getSimpleName());
             }
             sbd.append("</").append(name).append(">");
             return;
         } else if (obj instanceof Collection<?>) { // collection
             sbd.append("<").append(name).append(" hashcode=\"");
             sbd.append(obj.hashCode());
             sbd.append("\" class=\"").append(obj.getClass().getName());
             sbd.append("\" >");
             Collection<?> coll = (Collection<?>) obj;
             for (Object o : coll) {
                 if (null != o)
                     objectToXmlString(o, sbd, set, o.getClass().getSimpleName());
             }
             sbd.append("</").append(name).append(">");
 
             return;
         } else if (obj instanceof Class<?>) { // class object Prevent death
                                               // recursion
         } else if (Map.class.isInstance(obj)) {
             sbd.append("<").append(name).append(" hashcode=\"");
             sbd.append(obj.hashCode());
             sbd.append("\" class=\"").append(obj.getClass().getName());
             sbd.append("\" >");
             Map<?, ?> map = (Map<?, ?>) obj;
             for (Object key : map.keySet()) {
 
				sbd.append("<entry isMapEntry=\"true\">");
				objectToXmlString(key, sbd, set, "key");
//				sbd.append("</key><value>");
				objectToXmlString(map.get(key), sbd, set, "value");
				sbd.append("</entry>");
			
//                objectToXmlString(map.get(key), sbd, set, key + "");
             }
             sbd.append("</").append(name).append(">");
         } else { // common java bean
             sbd.append("<").append(name);
             sbd.append(" hashcode=\"");
             sbd.append(obj.hashCode());
             sbd.append("\" class=\"").append(obj.getClass().getName());
             sbd.append("\" >");
 
             Method[] ms = extractMethods(obj);
             if (null != ms) {
                 for (int msIndex = 0; msIndex < ms.length; msIndex++) {
                     String filedName = ms[msIndex].getName();
                     if (!"getClass".equals(filedName)
                             && (filedName.startsWith("get") || filedName
                                     .startsWith("is"))) {
                         try {
                             Object valueObj = ms[msIndex].invoke(obj,
                                     new Object[] {});
                             objectToXmlString(valueObj, sbd, set, filedName);
                         } catch (Exception e) {
                             e.printStackTrace();
                         }
                     }
                 }
             }
 
             sbd.append("</").append(name).append(">");
         }
 
         // is object
 
         sbd.append(END_LINE);
     }
 
     public static void objectToXmlString2(Object obj, StringBuilder sbd,
             Set<Object> set, String name) {
         if (null == obj) {
             return;
         }
         if (null == set) {
             set = new HashSet<Object>();
         }
         if (null == sbd) {
             sbd = new StringBuilder();
         }
 
         if (null == name) {
             name = obj.getClass().getSimpleName();
         } else {
             int begin = 0;
             int end = 0;
             if (name.startsWith("get")) {
                 begin = 3;
                 end = 4;
             } else if (name.startsWith("is")) {
                 begin = 2;
                 end = 3;
             }
             name = name.substring(begin, end).toLowerCase()
                     + name.substring(end);
         }
         if (!set.add(obj)) {
             sbd.append("<").append(name);
             sbd.append(" hashcode=\"");
             sbd.append(obj.hashCode());
             sbd.append("\" object_ref=\"true\" class=\"");
             sbd.append(obj.getClass().getName()).append("\"/>");
             return;
         }
 
         String tmpValue = parseValue(obj);
         if (null != tmpValue) { // java base type
             if (String.class.equals(obj.getClass())) {
                 tmpValue = tmpValue.replaceAll(
                         "(\\u003c!\\u005bCDATA\\u005b|\\u005d\\u005d\\u003e)",
                         " ");
                 tmpValue = CDATA_PREF + tmpValue + CDATA_END;
             }
             sbd.append("<").append(name);
             sbd.append(" hashcode=\"");
             sbd.append(obj.hashCode());
             sbd.append("\" class=\"");
             sbd.append(obj.getClass().getName()).append("\">");
             sbd.append(iXmlProcess.write(tmpValue)).append("</");
             sbd.append(name).append(">");
             return;
         } else if (obj.getClass().isArray()) {
             sbd.append("<").append(name).append(" class=\"")
                     .append(obj.getClass().getName());
             sbd.append("\" length=\"").append(Array.getLength(obj));
             sbd.append("\">");
             Object[] coll = (Object[]) obj;
             for (int i = 0; i < coll.length; i++) {
                 objectToXmlString(coll[i], sbd, set, null);
             }
             sbd.append("</").append(name).append(">");
             return;
         } else if (obj instanceof Collection<?>) {
             Collection<?> c = (Collection<?>) obj;
             Iterator<?> it = c.iterator();
             while (it.hasNext()) {
                 objectToXmlString(it.next(), sbd, set, null);
             }
             return;
         }
 
         // is object
         List<StringBuilder> subList = new ArrayList<StringBuilder>();
 
         sbd.append("<").append(name);
         String innerName = name;
         Method[] ms = extractMethods(obj);
         if (null != ms) {
             for (int msIndex = 0; msIndex < ms.length; msIndex++) {
                 name = ms[msIndex].getName();
                 // int nameIndex = name.indexOf("get");
                 boolean isSubObj = false;
                 if (name.startsWith("get") || name.startsWith("is")) {
 
                     String value = "";
                     try {
                         Object valueObj = ms[msIndex].invoke(obj,
                                 new Object[] {});
                         if (null != valueObj)
                             if (valueObj instanceof Date) {
                                 SimpleDateFormat sdf = new SimpleDateFormat(
                                         "yyyy-MM-dd HH:mm:ss");
                                 value = sdf.format((Date) valueObj);
                             } else if (valueObj instanceof Collection<?>) {
                                 StringBuilder sub = new StringBuilder(END_LINE);
                                 sub.append("<");
                                 sub.append(name.substring(3));
                                 sub.append(" hashcode=\"");
                                 sub.append(valueObj.hashCode());
                                 // sub.append("\" componentType=\"");
                                 // sub.append(valueObj.getClass().getComponentType());
                                 sub.append("\" class=\"");
                                 sub.append(valueObj.getClass().getName());
                                 sub.append("\" >").append(END_LINE);
                                 Collection<?> coll = (Collection<?>) valueObj;
                                 Iterator<?> iterator = coll.iterator();
                                 while (iterator.hasNext()) {
                                     objectToXmlString(iterator.next(), sub,
                                             set, null);
                                 }
                                 sub.append("</");
                                 sub.append(name.substring(3));
                                 sub.append(">").append(END_LINE);
                                 subList.add(sub);
 
                             } else {
                                 if (!(valueObj instanceof Class<?>)) {
                                     // value = valueObj.toString();
                                     StringBuilder sub = new StringBuilder();
                                     // System.out.println("--->" + name +
                                     // "<---");
                                     objectToXmlString(valueObj, sub, set, name);
                                     subList.add(sub);
                                     // isSubObj = true;
 
                                 } else {
                                     value = ((Class<?>) valueObj).getName();
                                 }
                             }
                     } catch (Exception e) {
                         e.printStackTrace();
                         value = "";
                     }
                     if (!"".equals(value) && !isSubObj) {
                         value = toXmlString(value);
                         String emp = name.substring(3, 4).toLowerCase()
                                 + name.substring(4);
                         sbd.append(" ").append(emp);
                         // TODO value 修改 XMLBean
                         if (!"class".equals(emp)) {
                             value = iXmlProcess.write(value);
                         }
                         // System.out.println(emp + " -> " + value);
                         sbd.append("=\"").append(value).append("\"");
                     }
                 }
             }
         }
         if (subList.isEmpty()) {
             sbd.append(" />");
         } else {
             sbd.append(" hashcode=\"");
             sbd.append(obj.hashCode());
             sbd.append("\">");
             for (int i = 0; i < subList.size(); i++) {
                 sbd.append(subList.get(i));
             }
             sbd.append("</").append(innerName).append(">");
             // System.out.println("<---" + innerName + "--->");
         }
         sbd.append(END_LINE);
     }
 
     // private static void objectToXmlString(Object obj, StringBuilder sbd) {
     // objectToXmlString(obj, sbd, null, null);
     // }
 
     // private static void objectToXmlDom(StringBuilder sbd, Object obj,
     // Set<Object> set, String name) {
     // if (null == name) {
     // name = obj.getClass().getSimpleName();
     // } else
     // name = name.substring(3, 4).toLowerCase() + name.substring(4);
     //
     // String tmpValue = parseValue(obj);
     //
     // // System.out.println("object : " + obj.getClass() + " "
     // // + obj.getClass().isArray());
     //
     // // String tmpValue = parseValue(obj);
     // if (null != tmpValue) { // java base type
     // if (String.class.equals(obj.getClass())) {
     // tmpValue = tmpValue.replaceAll(
     // "(\\u003c!\\u005bCDATA\\u005b|\\u005d\\u005d\\u003e)",
     // " ");
     // tmpValue = CDATA_PREF + tmpValue + CDATA_END;
     // }
     // sbd.append("<").append(name);
     // sbd.append(" hashcode=\"");
     // sbd.append(obj.hashCode());
     // sbd.append("\" class=\"");
     // sbd.append(obj.getClass().getName()).append("\">");
     // sbd.append(iXmlProcess.write(tmpValue)).append("</");
     // sbd.append(name).append(">");
     // return;
     // } else if (obj.getClass().isArray()) {
     // sbd.append("<").append(name).append(" class=\"")
     // .append(obj.getClass().getName());
     // sbd.append("\" length=\"").append(Array.getLength(obj));
     // sbd.append("\">");
     // Object[] coll = (Object[]) obj;
     // for (int i = 0; i < coll.length; i++) {
     // objectToXmlDom(sbd, coll[i], set, null);
     // }
     // sbd.append("</").append(name).append(">");
     // return;
     // }
     //
     // if (!set.add(obj)) {
     // sbd.append("<").append(name);
     // sbd.append(" hashcode=\"");
     // sbd.append(obj.hashCode());
     // sbd.append("\" object_ref=\"true\" class=\"");
     // sbd.append(obj.getClass().getName()).append("\"/>");
     // return;
     // }
     //
     // List<StringBuilder> subList = new ArrayList<StringBuilder>();
     //
     // sbd.append("<").append(name);
     // String innerName = name;
     // Method[] ms = extractMethods(obj);
     // if (null != ms) {
     // for (int msIndex = 0; msIndex < ms.length; msIndex++) {
     // name = ms[msIndex].getName();
     // int nameIndex = name.indexOf("get");
     // boolean isSubObj = false;
     // if (0 == nameIndex) {
     // String value = "";
     // try {
     // Object valueObj = ms[msIndex].invoke(obj,
     // new Object[] {});
     // if (null != valueObj)
     // if (valueObj instanceof Date) {
     // SimpleDateFormat sdf = new SimpleDateFormat(
     // "yyyy-MM-dd HH:mm:ss");
     // value = sdf.format((Date) valueObj);
     // } else if (valueObj instanceof Collection<?>) {
     // StringBuilder sub = new StringBuilder(END_LINE);
     // sub.append("<");
     // sub.append(name.substring(3));
     // sub.append(" hashcode=\"");
     // sub.append(valueObj.hashCode());
     // sub.append("\" class=\"");
     // sub.append(valueObj.getClass().getName());
     // sub.append("\" >").append(END_LINE);
     // Collection<?> coll = (Collection<?>) valueObj;
     // Iterator<?> iterator = coll.iterator();
     // while (iterator.hasNext()) {
     // objectToXmlDom(sub, iterator.next(), set,
     // null);
     // }
     // sub.append("</");
     // sub.append(name.substring(3));
     // sub.append(">").append(END_LINE);
     // subList.add(sub);
     //
     // } else {
     // if (!(valueObj instanceof Class<?>)) {
     // // value = valueObj.toString();
     // StringBuilder sub = new StringBuilder();
     // // System.out.println("--->" + name +
     // // "<---");
     // objectToXmlDom(sub, valueObj, set, name);
     // subList.add(sub);
     // // isSubObj = true;
     //
     // } else {
     // value = ((Class<?>) valueObj).getName();
     // }
     // }
     // } catch (Exception e) {
     // // e.printStackTrace();
     // value = "";
     // }
     // if (!"".equals(value) && !isSubObj) {
     // value = toXmlString(value);
     // String emp = name.substring(3, 4).toLowerCase()
     // + name.substring(4);
     // sbd.append(" ").append(emp);
     // // TODO value 修改 XMLBean
     // if (!"class".equals(emp)) {
     // value = iXmlProcess.write(value);
     // }
     // // System.out.println(emp + " -> " + value);
     // sbd.append("=\"").append(value).append("\"");
     // }
     // }
     // }
     // }
     // if (subList.isEmpty()) {
     // sbd.append(" />");
     // } else {
     // sbd.append(" hashcode=\"");
     // sbd.append(obj.hashCode());
     // sbd.append("\">");
     // for (int i = 0; i < subList.size(); i++) {
     // sbd.append(subList.get(i));
     // }
     // sbd.append("</").append(innerName).append(">");
     // // System.out.println("<---" + innerName + "--->");
     // }
     // sbd.append(END_LINE);
     // }
 
     private static Method[] extractMethods(Object obj) {
         Class<?> c = obj.getClass();
         Method[] ms = null;// = obj.getClass().getDeclaredMethods();
         Method[] temp;
         do {
             Method[] cms = c.getDeclaredMethods();
             if (null == ms) {
                 ms = cms;
             } else {
                 temp = new Method[ms.length + cms.length];
                 System.arraycopy(ms, 0, temp, 0, ms.length);
                 System.arraycopy(cms, 0, temp, ms.length, cms.length);
                 ms = temp;
             }
             c = c.getSuperclass();
         } while (null != c);
 
         return ms;
     }
 }
