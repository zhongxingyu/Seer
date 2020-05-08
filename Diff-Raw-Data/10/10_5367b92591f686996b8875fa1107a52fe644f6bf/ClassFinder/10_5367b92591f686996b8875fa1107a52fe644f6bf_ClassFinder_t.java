 /*
  * HJB4U is toolchain for creating a HyperJAXB front end for database users.
  * Copyright (C) 2010  NigelB
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  */
 
 package hjb4u;
 
 
 import hjb4u.logging.Log4j_Init;
 import org.apache.log4j.Logger;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.reflect.Field;
 import java.lang.reflect.ParameterizedType;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Set;
 
 /**
  * <code>ClassFinder</code>
  * Date: 14/06/2010
  * Time: 9:40:58 PM
  *
  * @author Nigel B
  */
 public class ClassFinder {
     private static final Logger logger = Logger.getLogger(ClassFinder.class.getName());
 
     private static Class[] findClasses(InputStream is, Hashtable<String, Integer> packages) {
         ArrayList<Class> toRet = new ArrayList<Class>();
         Class[] _toRet;
         Class current;
         Integer val;
         try {
 //            URL nsmap = new URL();new File().
             DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
             DocumentBuilder builder = factory.newDocumentBuilder();
             Document doc = builder.parse(is);
             Element root = doc.getDocumentElement();
             NodeList nl = doc.getElementsByTagName("class");
             Node n;
             Element e;
             int nscount = 1;
             for (int i = nl.getLength(); i >= 0; i--) {
                 n = nl.item(i);
                 if (n instanceof Element) {
                     e = (Element) n;
                     try {
                         toRet.add(current = Class.forName(e.getAttribute("ref")));
                         if (packages != null) {
                             val = packages.get(current.getPackage().getName());
                             if (val == null) {
                                 val = -1;
                             }
                             packages.put(current.getPackage().getName(), val + 1);
                         }
 
                         logger.debug("Found JAXB Element: " + e.getAttribute("ref"));
                     } catch (ClassNotFoundException e1) {
                         logger.info("Found JAXB Element: " + e.getAttribute("ref") + " But cannot load it.");
                     }
                 }
             }
         }
 
         catch (Exception e) {
             logger.debug(e, e);
             _toRet = new Class[toRet.size()];
             toRet.toArray(_toRet);
             return _toRet;
         }
 
         _toRet = new Class[toRet.size()];
         toRet.toArray(_toRet);
         return _toRet;
     }
 
     public static Class[] getEpisodeClasses(URL location) throws IOException {
 
         return findClasses(location.openStream(), null);
 
     }
 
     public static Class[] findAllJAXBClasses(URL location) throws IOException {
         Hashtable<String, Integer> packages = new Hashtable<String, Integer>();
         Class[] cls = findClasses(location.openStream(), packages);
         Hashtable<String, Class> done = new Hashtable<String, Class>();
         for (Class cl : cls) {
             recurse(cl, packages, done);
         }
 
         Set<String> keys = done.keySet();
         cls = new Class[keys.size()];
         int i = 0;
         for (String key : keys) {
             try {
                 cls[i++] = Class.forName(key);
             } catch (ClassNotFoundException e) {
                 logger.error(String.format("Could not find Class: %s", e.getMessage()));
                 logger.trace(e, e);
             }
         }
         return cls;
     }
 
     private static void recurse(Class cls, Hashtable<String, Integer> packages, Hashtable<String, Class> done) {
 		if(cls.isArray()){cls = cls.getComponentType();}
         if (done.get(cls.getName()) != null || cls.isPrimitive() || !in(cls.getPackage().getName(), packages)) return;
        System.out.println(cls);
         done.put(cls.getName(), cls);
         if (cls.isEnum()) return;
         Field[] fields = cls.getDeclaredFields();
         Class type;
         for (Field field : fields) {
             type = field.getType();
             if (field.getType() == List.class) {
                try{
                 type = (Class) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                }catch(ClassCastException c)
                {
                    logger.warn("Issue generating table listing.");
                }
             }
             recurse(type, packages, done);
         }
 
     }
 
     private static boolean in(String name, Hashtable<String, Integer> packages) {
         return packages.get(name) != null;
     }
 }
