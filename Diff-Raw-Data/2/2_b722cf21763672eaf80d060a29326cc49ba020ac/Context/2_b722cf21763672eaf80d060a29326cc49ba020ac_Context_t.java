 
 package org.webmacro;
 
 
 import java.util.*;
 import org.webmacro.*;
 import org.webmacro.util.*;
 
 /**
   * A Context contains all of the data you wish to display in a WebMacro 
   * template. Ordinarily you just use the get() and set() methods to load
   * the Context with all of your data, as if it were a hashtable, and then
   * pass it to a Template for execution.
   * <p>
   * You should create a new Context for every request, usually by cloning
   * a prototype object. The Context is not thread-safe and expects to
   * operate in a thread-per-request environment. Because all of the 
   * request-specific state is contained within the Context, Templates 
   * and other objects can be safely shared between multiple threads. 
   * <p>
   * A Context also contains other things that may be useful to the template
   * processing system: a copy of the Broker that is currently in effect, 
   * which can be used to load other objects; a set of ContextTools which 
   * add additional functionality to templates; and potentially an instance
   * of a Java Bean which should be used as the root of introspection. If 
   * the bean is null then the Context itself is the root of introspection.
   * <p>
   * You may wish to make use of the ContextTool objects directly within 
   * your application. 
   * <p>
   * A Context is cloneable so that you can efficiently create a new 
   * context from an existing one. Instantiating a brand new context object
   * using the constructor is expensive. The cloned context will not share 
   * local variables with its sibling, but will share the same tools 
   * (including tool instances), and the same broker.
   */
 public class Context implements Cloneable {
 
    private Broker _broker;
 
    private Object _bean; // root of property introspection
 
    private Map _toolbox; // contains tool initializers
    private Map _tools = null;   // contains in-use tools
 
    private HashMap _globals = null; // local variables
 
    private Locale _locale = Locale.getDefault();
 
    /**
      * Log configuration errors, context errors, etc.
      */
    private final Log _log;
 
 
 
    // CONSTRUCTION, INITIALIZATION, AND LIFECYCLE
 
    /**
      * Create an empty context--no bean, just local variables
      * and a broker. Tools loaded from config "ContextTools".
      * <p>
      * Ordinarily you don't call this method: you create a prototype
      * Context and then use newInstance. Creating the initial Context
      * object is fairly expensive. Use the WebMacro.getContext() 
      * method instead.
      */
    protected Context(final Broker broker) {
       _broker = broker; 
       _log = _broker.getLog("context");
       _bean = null;
       _toolbox = null;
       try {
          String tools = (String) broker.get("config","ContextTools");
          registerTools(tools);
       } catch (NotFoundException ne) {
          _log.warning("could not load ContextTools from config", ne);
       }
    }
 
    /**
      * Create a new context working from the specified broker with the 
      * tools available in the supplied toolbox Map. If a bean 
      * is specified (bean != null) then it will be used for property
      * introspection, otherwise property introspection will work with 
      * the local variables stored in this context.
      */
    protected Context(final Broker broker, final Map toolbox, final Object bean)
    {
       _broker = broker;
       _log = broker.getLog("context");
       _bean = bean;
       _toolbox = toolbox;
    }
 
 
    /**
      * Create a new context based on this one, but using the specified 
      * bean instead of this one. The clone will share tools and the broker 
      * with its parent. It will have a null property bean.
      */
    protected Object clone() {
       Context c = null;
       try {
          c = (Context) super.clone();
       } catch (CloneNotSupportedException e) {
          // Object supports clone
       }
 
       c._globals = null;
       c._bean = null;
       c._locale = _locale;
 
       return c;
    }
 
    /**
      * This method is ordinarily called by the template processing system.
      * <p>
      * Clear the context of its non-shared data, preserving only the toolbox.
      */
    public void clear() {
       _tools = null;
       _globals = null;
       _bean = null;
    }
 
 
    // INITIALIZATION: TOOL CONFIGURATION
 
    /**
      * This method is called when initializing a new context. You would
      * ordinarily then clone the configured context.
      * <p>
      * Subclasses can use this method to register new ContextTools
      * during construction or initialization of the Context. 
      */
    final protected void registerTool(String name, ContextTool tool) 
       throws ContextException
    {
       if (_toolbox == null) {
          _toolbox = new HashMap();
       }
       _toolbox.put(name,tool);
    }
 
    /**
      * Find the name of a tool given the name of a class
      */
    private String findToolName(String cname)
    {
       int start = cname.lastIndexOf('.') + 1;
       int end = (cname.endsWith("Tool")) ? 
          (cname.length() - 4) : cname.length();
       String ret = cname.substring(start,end);
       return ret;
    }
  
    /**
      * Add the tools specified in the StringTokenized list of tools
      * passed as an argument. The list of tools passed should be a list
      * of class names which can be loaded and introspected. It is expected
      * this method will be used during construction or initialization.
      */
    final protected void registerTools(String tools) {
       Enumeration tenum = new StringTokenizer(tools);
       while (tenum.hasMoreElements()) {
          String toolName = (String) tenum.nextElement();
          try {
             Class toolType = Class.forName(toolName);
             String varName = findToolName(toolName);
             ContextTool tool = (ContextTool) toolType.newInstance(); 
             registerTool(varName,tool);
          } catch (ClassCastException cce) {
             _log.error("Tool class " + toolName 
                   + " newInstance returns invalid type.", cce);
          } catch (ClassNotFoundException ce) {
             _log.error("Tool class " + toolName + " not found: ", ce);
          } catch (IllegalAccessException ia) {
             _log.error("Tool class and methods must be public for "
                   + toolName, ia);
          } catch (ContextException e) {
             _log.error("ContextException thrown while registering "
                   + "Tool: " + toolName, e);
          } catch (InstantiationException ie) {
             _log.error("Tool class " + toolName + " must have a public zero "
                   + "argument or default constructor", ie);
          }
       }
    }
 
 
    // ACCESS TO THE BROKER
 
    /**
      * Get the broker that it is in effect for this context
      */
    final public Broker getBroker() {
       return _broker;
    }
 
    /**
      * Convenience method equivalent to getBroker().getLog(name)
      */
    final public Log getLog(String name) {
       return _broker.getLog(name);
    }
 
 
    // PROPERTY API
 
    /**
      * Get the local variables as a HashMap
      */
    final public Map getGlobalVariables() {
       if (_globals == null) {
          _globals = new HashMap();
       }
       return _globals;
    }
 
 
    /**
     * Set the local variables as a HashMap.<p>
     *
    * One should probably use this method like this:<br>
     * <code>context.setGlobalVariables ( (HashMap) _myDefaultMap.clone());</code>
     * @author Eric B. Ridge 
     * @date Oct 16, 2000
     *
     * @param globalMap the HashMap to use as the global variabls for this Context
     */
    final public void setGlobalVariables (HashMap globalMap)
    {
       _globals = globalMap;
    }
 
 
    /**
      * Return the root of introspection, the top level bean for this 
      * context which properties reference into. If this returns null, 
      * then properties reference local variables.
      */
    final public Object getBean() {
       return _bean;
    }
 
    /**
      * Set the root of introspection
      */
    final public void setBean(Object bean) {
       _bean = bean;
    }
 
    /**
      * Get the named property via introspection. If there is no bean 
      * in this context, then try accessing the value as a local variable.
      * If there is no bean, and no local variable, try it as a tool. This
      * fallback to local and tool is to make property variable access
      * backward compatible with older WebMacro implementations for the
      * top level template, where there is no bean.
      */
    public final Object getProperty(final Object[] names) 
       throws PropertyException, ContextException
    {
       Object ret = null;
       if (_bean == null) {
          ret = getGlobal(names);
       } else {
         // 13-Oct-00 - re-added by keats: consume the first PropertyException to allow tools to be checked
         try {
           ret = PropertyOperator.getProperty(this,_bean,names);
         } catch (PropertyException pe){
           // consume this exception and try again below, will get thrown again if appropriate
         }
       }
       if (ret == null){
         ret = getTool(names);
       }
       return ret;
    }
 
    /**
      * Set the named property via introspection 
      */
    final public boolean setProperty(final Object[] names, final Object value) 
       throws PropertyException, ContextException
    {
       if (names.length == 0) {
          return false;
       } else if (_bean == null) {
          return setGlobal(names, value) || setTool(names, value);
       } else {
          return PropertyOperator.setProperty(this,_bean,names,value) || 
                setTool(names, value);      
       }
    }
 
 
    // LOCAL VARIABLE API
 
    /**
      * Retrieve a local value from this Context. 
      */
    final public Object get(Object name) {
       return (_globals != null) ? _globals.get(name) : null;
    }
 
    /**
      * Set a local value in this Context
      */
    final public void put(Object name, Object value) {
       if (_globals == null) {
          getGlobalVariables().put(name,value);
       } else {
          _globals.put(name,value);
       }
    }
 
    /**
      * Get the named local variable via introspection. This is 
      * an advanced-use method.
      */
    public final Object getGlobal(final Object[] names) 
       throws PropertyException, ContextException
    {
       int len = names.length;
       if ((_globals == null) || (len == 0)) {
          return null;
       } 
       Object res = get(names[0]); 
       if ((len == 1) || (res == null)) { 
          return res; 
       }
       return PropertyOperator.getProperty(this,res,names,1);
    }
 
    /**
      * Set the named local variable via introspection. This is 
      * an advanced-use method.
      */
    final public boolean setGlobal(final Object[] names, final Object value) 
       throws PropertyException, ContextException
    {
       if (names.length == 0) {
          return false;
       } 
       if (names.length == 1) {
          put(names[0], value);
          return true;
       } else {
          Object parent = get(names[0]);
          if (parent == null) {
             return false;
          } else {
             return PropertyOperator.setProperty(this,parent,names,1,value);
          }
       } 
    }
 
 
    // TOOL API
 
    /**
      * Return the tool corresponding to the specified tool name, or 
      * null if there isn't one. This is an advanced-use method.
      */
    final public Object getTool(Object name) 
       throws ContextException
    {
       try {
          if (_toolbox == null) {
             return null;
          }
          Object ret = (_tools != null) ? _tools.get(name) : null;
          if (ret == null) {
             ContextTool tool = (ContextTool) _toolbox.get(name);
             if (tool != null) {
                if (_tools == null) {
                   _tools = new HashMap();
                }
                ret = tool.init(this);
                _tools.put(name,ret);
             }
          }
          return ret;
       } catch (ClassCastException ce) {
          throw new ContextException("Tool" + name  
                + " does not implement the ContextTool interface!");
       }
    }
    /**
      * Get the named tool variable via introspection. This is an 
      * advanced-use method.
      */
    public final Object getTool(final Object[] names) 
       throws PropertyException, ContextException
    {
       if ((names.length == 0) || (_toolbox == null)) {
          return null;
       } else {
          Object res = getTool(names[0]);
          if (names.length == 1) {
             return res;
          } 
          return PropertyOperator.getProperty(this,getTool(names[0]),names,1);
       } 
    }
 
    /**
      * Set the named tool variable via introspection. This is an 
      * advanced-use method.
      */
    final public boolean setTool(final Object[] names, final Object value) 
       throws PropertyException, ContextException
    {
       if (names.length == 0) {
          return false;
       } 
       if (names.length == 1) {
          throw new ContextException("Cannot reset tool in a running context. Tools can only be registered via the registerTool method.");
       } else {
          return PropertyOperator.setProperty(this,getTool(names[0]),names,1,value);
       } 
    }
 
    /**
      * Set the locale for this request
      */
    final public void setLocale(Locale l) {
       _locale = l;
    }
 
    /**
      * Get the locale for this request. This will return null if no 
      * Locale has been set for the current request.
      */
    final public Locale getLocale() {
       return _locale;
    }
 
 }
