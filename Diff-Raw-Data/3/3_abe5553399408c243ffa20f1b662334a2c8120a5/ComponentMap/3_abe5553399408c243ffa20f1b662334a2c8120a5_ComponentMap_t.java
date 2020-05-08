 
 package org.webmacro.util;
 
 import java.util.*;
 import java.net.URL;
 import java.io.*;
 import java.lang.reflect.*;
 import org.webmacro.*;
 
 /**
   * A ComponentMap takes a list of component names and builds a Map of 
   * instance objects. You can include a filename in a list of components
   * and the contents of that file will be inserted into the list. The
   * format of the component list is a series of <code>name=class</code>
   * entries separated by whitespace, commas, newlines, or semicolons.
   * If the "name=" portion is omitted then the base classname of the 
   * component will be used as its name instead.
   * <p>
   * Here is an example:<pre>
   * 
   *       Freddy=org.webmacro.examples.Freddy
   *       /examples/COMPONENTS Critter=com.semiotek.creature.Mongrel
   *       org.webmacro.servlet.FormTool
   *
   * </pre>
   * The above component list defines <code>Freddy</code>, implemented
   * by the class <code>org.webmacro.examples.Freddy</code>. Also, 
   * the contents of the file <code>/examples/COMPONENTS</code> will
   * be read from the CLASSPTH and inserted into this component list. 
   * The last entry in the example above is a component with no explicit
   * name: <code>org.webmacro.servlet.FormTool</code>. The base class
   * name will be used as the name for this component: <code>FormTool</code>.
   * <p>
   * A component must have one of the following three constructors. They
   * will be searched for in this order:
   * <ol>
   * <li>public Component(String name, org.webmacro.util.Settings config)
   * <li>public Component(String name)
   * <li>public Component()
   * </ol>
   * It is an error if a component does not even have the no-argument 
   * constructor. If you declare one of teh constructors with an argument
   * then your component will have some extra information available with
   * which to configure itself when it is instantiated by the ComponentMap.
   */
 public class ComponentMap 
 {
 
    final private Settings _config;
    final private Map _values = new HashMap();
    final private Log _log = LogSystem.getSystemLog("system");
 
    /**
      * Create a new ComponentMap for the supplied list of names separated
      * on whitespace, commas, newlines, colons, and semicolons.
      */
    public ComponentMap() {
       _config = new Settings();
    }
 
    /**
      * Create a new ComponentMap for the supplied list of names using
      * the supplied Settings for initialization, if any initialization
      * can be done. The name list will be separated on whitespace, commas,
      * newlines, colons, and semicolons.
      */
    public ComponentMap(Settings initProps) {
       _config = initProps;
    }
 
    public Object get(Object name) {
       return _values.get(name);
    }
 
    /**
      * Get an iterator that walks throught he keys installed into
      * this ComponentMap
      */
    public Iterator keys() {
       return _values.keySet().iterator();
    }
 
    /**
      * Get an iterator that walkst hrough the values installed 
      * into this ComponentMap
      */
    public Iterator values() {
       return _values.values().iterator();
    }
 
    /**
      * Load the component map from the supplied namelist. The string
      * will be separated by spaces, tabs, newlines, commas, and 
      * semicolons, the load(String[]) will be called. 
      */
    public void load(String namelist) {
       load(tokenize(namelist), "");
    }
 
    /**
      * Load the component map from the supplied namelist. The string
      * will be separated by spaces, tabs, newlines, commas, and 
      * semicolons, the load(String[]) will be called. The suffic 
      * will be dropped from the end of component names as they are 
      * registered, if they do not have an explicit name.
      */
    public void load(String namelist, String suffix) {
       load(tokenize(namelist), suffix);
    }
 
      
    /**
      * Load the component map from the supplied name list.
      */
    public void load(String[] namelist) {
       load(namelist, "");
    }
 
    /**
      * Load the component map from the supplied name list. If key
      * names are derived from class names then the supplied suffix
      * will be dropped from the name. 
      */
    public void load(String[] namelist, String suffix) 
    {
       ClassLoader c = ComponentMap.class.getClassLoader();
       char[] buf = new char[8192];
 
       for (int i = 0; i < namelist.length; i++) {
          String name = namelist[i];
 
          // if it's a file read it as an extension of this list
          if (name.indexOf('/') != -1) {
             StringBuffer b = new StringBuffer();
             URL u = c.getResource(name);
             try {
                Reader in = new InputStreamReader(new BufferedInputStream(u.openStream()));
                int num;
                while ((num = in.read(buf)) != -1) {
                   b.append(buf,0,num);
                }
                in.close();
                load(tokenize(b.toString()));          
             } catch (IOException e) {
                _log.warning("ComponentMap: Error reading data from " + u, e);
             }
          } else {
             add(name,suffix);
          }
       }
    }
 
    private static final Class[] _ctorArgs1 = { java.lang.String.class, org.webmacro.util.Settings.class };
    private static final Class[] _ctorArgs2 = { java.lang.String.class };
 
    protected void add(String component, String suffix) {
 
       String key = null;
       String name = component;
    
       int eqIdx = component.indexOf('=');
       if (eqIdx != -1) {
          key = component.substring(0,eqIdx);
          name = component.substring(eqIdx + 1);
       }
      
       Class c;
       try {
          c = Class.forName(name);
       } catch (ClassNotFoundException e) {
          _log.warning("ComponentMap: Could not locate class " + name, e);
          return;
       }
       if (key == null) {
          key = c.getName();
          int start = 0;
          int end = key.length();
          int lastDot = key.lastIndexOf('.');
          if (lastDot != -1) {
             start = lastDot + 1;
          }
          if (key.endsWith(suffix)) {
             end -= suffix.length();
          }
          key = key.substring(start,end);
       }
 
       Object instance = null;
       StringBuffer log = null;
       try {
          Constructor ctor = c.getConstructor(_ctorArgs1);
          Object[] args = new Object[2];
          args[0] = key;
          args[1] = _config.getSubSettings(key);
          instance = ctor.newInstance(args);
       } catch (Exception e) { 
          log = new StringBuffer();
          log.append("Error loading component key=");
          log.append(key);
          log.append(" class=");
          log.append(c.toString());
          log.append("\n");
          log.append("Trying 2-argument constructor: ");
          log.append(e.toString()); 
          log.append("\n");
       }
 
       if (instance == null) {
          try {
             Constructor ctor = c.getConstructor(_ctorArgs2);
             Object[] args = new Object[1];
             args[0] = key;
             instance = ctor.newInstance(args);
          } catch (Exception e) { 
             log.append("Trying 1-argument constructor: ");
             log.append(e.toString());
             log.append("\n");
          }
       }
 
       if (instance == null) {
          try {
             instance = c.newInstance();
          } catch (Exception e) {
             log.append("Trying 0-argument constructor: ");
             log.append(e.toString()); 
             log.append("\n");
             _log.warning(log.toString());
             return;
          }
       }
       _values.put(key,instance);
    }
 
    static private String[] tokenize(String list) {
       StringTokenizer st = new StringTokenizer(list," \t\n\f\r;,");
       String[] tokens = new String[st.countTokens()];
      for (int i=0; st.hasMoreElements(); i++) {
          tokens[i] = st.nextToken();
       }
       return tokens;
    }
 
    public static void main(String arg[]) {
       ComponentMap cm = new ComponentMap();
       cm.load(arg);
       Iterator i = cm.keys();
       while (i.hasNext()) {
          String key = (String) i.next();
          System.out.println(key + ": " + cm.get(key));
       }
    }
 
 }
