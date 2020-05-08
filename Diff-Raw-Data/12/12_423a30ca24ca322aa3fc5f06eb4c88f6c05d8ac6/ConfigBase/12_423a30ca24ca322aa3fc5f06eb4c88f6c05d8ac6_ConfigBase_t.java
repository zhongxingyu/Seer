 /* ********************************************************************
     Licensed to Jasig under one or more contributor license
     agreements. See the NOTICE file distributed with this work
     for additional information regarding copyright ownership.
     Jasig licenses this file to you under the Apache License,
     Version 2.0 (the "License"); you may not use this file
     except in compliance with the License. You may obtain a
     copy of the License at:
 
     http://www.apache.org/licenses/LICENSE-2.0
 
     Unless required by applicable law or agreed to in writing,
     software distributed under the License is distributed on
     an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
     KIND, either express or implied. See the License for the
     specific language governing permissions and limitations
     under the License.
 */
 package edu.rpi.cmt.config;
 
 import edu.rpi.sss.util.ToString;
 import edu.rpi.sss.util.xml.XmlEmit;
 import edu.rpi.sss.util.xml.XmlEmit.NameSpace;
 import edu.rpi.sss.util.xml.XmlUtil;
 import edu.rpi.sss.util.xml.tagdefs.BedeworkServerTags;
 
 import org.apache.log4j.Logger;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.xml.sax.InputSource;
 
 import java.io.InputStream;
 import java.io.Serializable;
 import java.io.Writer;
 import java.lang.reflect.Method;
 import java.lang.reflect.ParameterizedType;
 import java.lang.reflect.Type;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.TreeSet;
 
 import javax.xml.namespace.QName;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 /** This class is used as a basis for configuration of system modules. The
  * classes extending this MUST be annotated appropriately with ConfInfo and
  * require setters and getters for all configuration fields.
  *
  * <p>The class itself requires an element name annotation<br/>
  * <code>
  *   &#64;ConfInfo(elementName="example-conf")
  * </code>
  *
  * <p>Collection fields MUST be either Set or List and the element class MUST
  * be specified. For example:<br/>
  * <code>
  *     private List<String> props;
  * </code>
  *
  * <p>Also collection getters require a name for the collection element<br/>
  * <code>
  *    &#64;ConfInfo(collectionElementName = "prop")
  *    public List<String> getProps() {
  * </code>
  *
  * <p>The ConfInfo annotation
  * <code>
  *    &#64;ConfInfo(dontSave = true)
  * </code>
  * allows for getters and setters of values used internally only.
  *
  * @author Mike Douglass
  * @param <T>
  */
 public abstract class ConfigBase<T extends ConfigBase>
         implements Comparable<T>, Serializable {
   /** The default namespace for the XML elements.
    *
    */
   public final static String ns = BedeworkServerTags.bedeworkSystemNamespace;
 
   private String name;
 
   /**
    * @param val
    */
   public void setName(final String val) {
     name = val;
   }
 
   /** Name for the configuration.
    *
    * @return String
    */
   public String getName() {
     return name;
   }
 
   /** Add our stuff to the StringBuilder
    *
    * @param ts    ToString for result
    * @param indent
    */
   public void toStringSegment(final ToString ts) {
     ts.append("name", getName());
   }
 
   /* ====================================================================
    *                   Object methods
    * ==================================================================== */
 
   @Override
   public int compareTo(final ConfigBase that) {
     return getName().compareTo(that.getName());
   }
 
   @Override
   public int hashCode() {
     return getName().hashCode();
   }
 
   @Override
   public String toString() {
     ToString ts = new ToString(this);
 
     toStringSegment(ts);
 
     return ts.toString();
   }
 
   /* ====================================================================
    *                   Property methods
    * ==================================================================== */
 
   /**
    * @param list
    * @param name
    * @param val
    * @return possibly newly created list
    */
   @SuppressWarnings("unchecked")
   public <L extends List> L addListProperty(final L list,
                                             final String name,
                                             final String val) {
     L theList = list;
     if (list == null) {
       theList = (L)new ArrayList();
     }
 
     theList.add(name + "=" + val);
     return theList;
   }
 
   /** Get a property stored as a String name = val
    *
    * @param col
    * @param name
    * @return value or null
    */
   @ConfInfo(dontSave = true)
   public String getProperty(final Collection<String> col,
                             final String name) {
     String key = name + "=";
     for (String p: col) {
       if (p.startsWith(key)) {
         return p.substring(key.length());
       }
     }
 
     return null;
   }
 
   /** Remove a property stored as a String name = val
    *
    * @param col
    * @param name
    */
   public void removeProperty(final Collection<String> col,
                              final String name) {
     try {
       String v = getProperty(col, name);
 
       if (v == null) {
         return;
       }
 
       col.remove(name + "=" + v);
     } catch (Throwable t) {
       throw new RuntimeException(t);
     }
   }
 
   /** Set a property
    *
    * @param list
    * @param name
    * @param val
    * @return possibly newly created list
    */
   @SuppressWarnings("unchecked")
   public <L extends List> L setListProperty(final L list,
                                             final String name,
                                             final String val) {
     removeProperty(list, name);
     return addListProperty(list, name, val);
   }
 
   /* ====================================================================
    *                   Save and restore methods
    * ==================================================================== */
 
   /** Output to a writer
    *
    * @param wtr
    * @throws ConfigException
    */
   public void toXml(final Writer wtr) throws ConfigException {
     try {
       XmlEmit xml = new XmlEmit();
       xml.addNs(new NameSpace(ns, "BW"), true);
       xml.startEmit(wtr);
 
       dump(xml, false);
       xml.flush();
     } catch (ConfigException cfe) {
       throw cfe;
     } catch (Throwable t) {
       throw new ConfigException(t);
     }
   }
 
   /**
    * @param is
    * @return parsed notification or null
    * @throws ConfigException
    */
   public static ConfigBase fromXml(final InputStream is) throws ConfigException {
     return fromXml(is, null);
   }
 
   /**
    * @param is
    * @param cl
    * @return parsed notification or null
    * @throws ConfigException
    */
   public static ConfigBase fromXml(final InputStream is,
                                    final Class cl) throws ConfigException {
     try {
       Element rootEl = parseXml(is);
 
       ConfigBase cb = (ConfigBase)getObject(rootEl, cl);
 
       if (cb == null) {
         // Can't do this
         return null;
       }
 
       for (Element el: XmlUtil.getElementsArray(rootEl)) {
         populate(el, cb, null, null);
       }
 
       return cb;
     } catch (ConfigException ce) {
       throw ce;
     } catch (Throwable t) {
       throw new ConfigException(t);
     }
   }
 
   /* ====================================================================
    *                   Private from xml methods
    * ==================================================================== */
 
   private static Object getObject(final Element el,
                                   final Class cl) throws Throwable {
     Class objClass = cl;
 
     /* Element may have type attribute */
     String type = XmlUtil.getAttrVal(el, "type");
 
     if ((type == null) && (objClass == null)) {
       error("Must supply a class or have type attribute");
       return null;
     }
 
     if (objClass == null) {
       objClass = Class.forName(type);
     }
 
     return objClass.newInstance();
   }
 
   private static Element parseXml(final InputStream is) throws Throwable {
     DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
     factory.setNamespaceAware(true);
 
     DocumentBuilder builder = factory.newDocumentBuilder();
 
     Document doc = builder.parse(new InputSource(is));
 
     if (doc == null) {
       return null;
     }
 
     return doc.getDocumentElement();
   }
 
   /** Populate either the object o via setters or the Collection col by adding
    * elements of type cl
    *
    * @param o
    * @param subroot
    * @param col
    * @param cl
    * @throws ConfigException
    */
   private static void populate(final Element subroot,
                                final Object o,
                                final Collection<Object> col,
                                final Class cl) throws Throwable {
     String name = subroot.getNodeName();
 
     Method meth = null;
 
     Class elClass = null;
 
     if (col == null) {
       /* We must have a setter */
       meth = findSetter(o, name);
 
       if (meth == null) {
         error("No setter for " + name);
 
         return;
       }
 
       /* We require a single parameter */
 
       Class[] parClasses = meth.getParameterTypes();
       if (parClasses.length != 1) {
         error("Invalid setter method " + name);
         throw new ConfigException("Invalid setter method " + name);
       }
 
       elClass = parClasses[0];
     } else {
       elClass = cl;
     }
 
     if (!XmlUtil.hasChildren(subroot)) {
       /* A primitive value for which we should have a setter */
 
       Object val = simpleValue(elClass, subroot);
       if (val == null) {
         error("Unsupported par class " + elClass +
               " for field " + name);
         throw new ConfigException("Unsupported par class " + elClass +
                                   " for field " + name);
       }
 
       assign(val, col, o, meth);
 
       return;
     }
 
     /* There are children to this element. It either represents a complex type
      * or a collection.
      */
 
     if (Collection.class.isAssignableFrom(elClass)) {
       Collection<Object> colVal = null;
 
       if (elClass.getName().equals("java.util.Set")) {
         colVal = new TreeSet<Object>();
       } else if (elClass.getName().equals("java.util.List")) {
         colVal = new ArrayList<Object>();
       } else {
         error("Unsupported element class " + elClass +
               " for field " + name);
         return;
       }
 
       assign(colVal, col, o, meth);
 
       // Figure out the class of the elements
      Type gft = elClass;
 
       if (!(gft instanceof ParameterizedType)) {
        error("Unsupported type " + elClass +
               " with name " + name);
         return;
       }
 
       Type[] fieldArgTypes = ((ParameterizedType)gft).getActualTypeArguments();
 
       if (fieldArgTypes.length != 1) {
        error("Unsupported type " + elClass +
               " with name " + name);
         return;
       }
 
       for (Element el: XmlUtil.getElementsArray(subroot)) {
         populate(el, o, colVal, (Class)fieldArgTypes[0]);
       }
 
       return;
     }
 
     /* Asssume a complex type */
 
     Object val = getObject(subroot, elClass);
 
     assign(val, col, o, meth);
 
     for (Element el: XmlUtil.getElementsArray(subroot)) {
       populate(el, val, null, null);
     }
   }
 
   private static Method findSetter(final Object val,
                                    final String name) throws Throwable {
     String methodName = "set" + name.substring(0, 1).toUpperCase() +
                         name.substring(1);
     Method[] meths = val.getClass().getMethods();
     Method meth = null;
 
     for (int i = 0; i < meths.length; i++) {
       Method m = meths[i];
 
       if (m.getName().equals(methodName)) {
         if (meth != null) {
           throw new ConfigException("Multiple setters for field " + name);
         }
         meth = m;
       }
     }
 
     if (meth == null) {
       getLog().error("No setter method for property " + name +
                      " for class " + val.getClass().getName());
       return null;
     }
 
     return meth;
   }
 
   /** Assign a value - either to collection col or to a setter of o defined by meth
    * @param val
    * @param col - if non-null add to this.
    * @param o
    * @param meth
    * @throws Throwable
    */
   private static void assign(final Object val,
                              final Collection<Object> col,
                              final Object o,
                              final Method meth) throws Throwable {
     if (col != null) {
       col.add(val);
     } else {
       Object[] pars = new Object[]{val};
 
       meth.invoke(o, pars);
     }
   }
 
   private static Object simpleValue(final Class cl,
                                     final Element el) throws Throwable {
     if (!XmlUtil.hasChildren(el)) {
       /* A primitive value for which we should have a setter */
       String ndval = XmlUtil.getElementContent(el);
 
       if (cl.getName().equals("java.lang.String")) {
         return ndval;
       }
 
       if (cl.getName().equals("int") ||
           cl.getName().equals("java.lang.Integer")) {
         return Integer.valueOf(ndval);
       }
 
       if (cl.getName().equals("long") ||
           cl.getName().equals("java.lang.Long")) {
         return Long.valueOf(ndval);
       }
 
       if (cl.getName().equals("boolean") ||
           cl.getName().equals("java.lang.Boolean")) {
         return Boolean.valueOf(ndval);
       }
 
 
       // XXX Should do byte, char, short, float, and double.
       return null;
     }
 
     // Complex value
     return null;
   }
 
   /* ====================================================================
    *                   Private to xml methods
    * ==================================================================== */
 
   private static class ComparableMethod implements Comparable<ComparableMethod> {
     Method m;
 
     ComparableMethod(final Method m) {
       this.m = m;
     }
 
     @Override
     public int compareTo(final ComparableMethod that) {
       return this.m.getName().compareTo(that.m.getName());
     }
   }
 
   private void dump(final XmlEmit xml,
                     final boolean fromCollection) throws Throwable {
     ConfInfo ciCl = getClass().getAnnotation(ConfInfo.class);
 
     QName qn = startElement(xml, getClass(), ciCl);
 
     Collection<ComparableMethod> ms = findGetters();
 
     for (ComparableMethod cm: ms) {
       Method m = cm.m;
 
       ConfInfo ci = m.getAnnotation(ConfInfo.class);
 
       dumpValue(xml, m, ci, m.invoke(this, (Object[])null), fromCollection);
     }
 
     if (qn != null) {
       closeElement(xml, qn);
     }
   }
 
   private QName startElement(final XmlEmit xml,
                              final Class c,
                              final ConfInfo ci) throws Throwable {
     QName qn;
     String type =null;
 
     if (ci == null) {
       qn = new QName(ns, c.getName());
     } else {
       qn = new QName(ns, ci.elementName());
       type = ci.type();
     }
 
     if ((type == null) || (type.length() == 0)) {
       type =c.getCanonicalName();
     }
 
     xml.openTag(qn, "type", type);
 
     return qn;
   }
 
   private QName startElement(final XmlEmit xml,
                              final Method m,
                              final ConfInfo ci,
                              final boolean fromCollection) throws Throwable {
     QName qn = getTag(m, ci, fromCollection);
 
     if (qn != null) {
       xml.openTag(qn);
     }
 
     return qn;
   }
 
   private QName getTag(final Method m,
                        final ConfInfo ci,
                        final boolean fromCollection) {
     String tagName = null;
 
     if (ci != null) {
       if (!fromCollection) {
         if (ci.elementName().length() > 0) {
           tagName = ci.elementName();
         }
       } else if (ci.collectionElementName().length() > 0) {
         tagName = ci.collectionElementName();
       }
     }
 
     if ((tagName == null) && !fromCollection) {
       tagName = fieldName(m.getName());
     }
 
     if (tagName == null) {
       return null;
     }
 
     return new QName(tagName);
   }
 
   private String fieldName(final String val) {
     if (val.length() < 4) {
       return null;
     }
 
     return val.substring(3, 4).toLowerCase() + val.substring(4);
   }
 
   private void closeElement(final XmlEmit xml,
                             final QName qn) throws Throwable {
     xml.closeTag(qn);
   }
 
   private boolean dumpValue(final XmlEmit xml,
                             final Method m,
                             final ConfInfo ci,
                             final Object methVal,
                             final boolean fromCollection) throws Throwable {
     /* We always open the methodName or elementName tag if this is the method
      * value.
      *
      * If this is an element from a collection we generally don't want a tag.
      *
      * We do open a tag if the annotation specifies a collectionElementName
      */
     if (methVal instanceof ConfigBase) {
       ConfigBase de = (ConfigBase)methVal;
 
       QName mqn = startElement(xml, m, ci, fromCollection);
 
       de.dump(xml, false);
 
       if (mqn != null) {
         closeElement(xml, mqn);
       }
 
       return true;
     }
 
     if (methVal instanceof Collection) {
       Collection c = (Collection)methVal;
 
       if (c.isEmpty()) {
         return false;
       }
 
       QName mqn = null;
 
       for (Object o: c) {
         if (mqn == null) {
           mqn = startElement(xml, m, ci, fromCollection);
         }
 
         dumpValue(xml, m, ci, o, true);
       }
 
       if (mqn != null) {
         closeElement(xml, mqn);
       }
 
       return true;
     }
 
     property(xml, m, ci, methVal, fromCollection);
 
     return true;
   }
 
   private void property(final XmlEmit xml,
                         final Method m,
                         final ConfInfo d,
                         final Object p,
                         final boolean fromCollection) throws ConfigException {
     if (p == null) {
       return;
     }
 
     try {
       QName qn = getTag(m, d, fromCollection);
 
       if (qn == null) {
         /* Collection and no collection element name specified */
         qn = new QName(p.getClass().getName());
       }
 
       String sval;
 
       if (p instanceof char[]) {
         sval = new String((char[])p);
       } else {
         sval = String.valueOf(p);
       }
 
       if ((sval.indexOf('&') < 0) && (sval.indexOf('<') < 0)) {
         xml.property(qn, sval);
       } else {
         xml.cdataProperty(qn, sval);
       }
     } catch (Throwable t) {
       throw new ConfigException(t);
     }
   }
 
   private Collection<ComparableMethod> findGetters() throws ConfigException {
     Method[] meths = getClass().getMethods();
     Collection<ComparableMethod> getters = new TreeSet<ComparableMethod>();
 
     for (int i = 0; i < meths.length; i++) {
       Method m = meths[i];
       ConfInfo ci = m.getAnnotation(ConfInfo.class);
 
       if ((ci != null) && ci.dontSave()) {
         continue;
       }
 
       String mname = m.getName();
 
       if (mname.length() < 4) {
         continue;
       }
 
       /* Name must start with get */
       if (!mname.startsWith("get")) {
         continue;
       }
 
       /* Don't want getClass */
       if (mname.equals("getClass")) {
         continue;
       }
 
       /* No parameters */
       Class[] parClasses = m.getParameterTypes();
       if (parClasses.length != 0) {
         continue;
       }
 
       getters.add(new ComparableMethod(m));
     }
 
     return getters;
   }
 
   private static Logger getLog() {
     return Logger.getLogger(ConfigBase.class);
   }
 
   private static void error(final String msg) {
     getLog().error(msg);
   }
 }
