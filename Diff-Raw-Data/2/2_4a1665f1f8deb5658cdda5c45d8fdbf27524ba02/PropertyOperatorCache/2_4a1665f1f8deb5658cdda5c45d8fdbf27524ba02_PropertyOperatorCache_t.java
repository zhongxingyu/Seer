 /*
  * Copyright (C) 1998-2000 Semiotek Inc.  All Rights Reserved.  
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted under the terms of either of the following
  * Open Source licenses:
  *
  * The GNU General Public License, version 2, or any later version, as
  * published by the Free Software Foundation
  * (http://www.fsf.org/copyleft/gpl.html);
  *
  *  or 
  *
  * The Semiotek Public License (http://webmacro.org/LICENSE.)  
  *
  * This software is provided "as is", with NO WARRANTY, not even the 
  * implied warranties of fitness to purpose, or merchantability. You
  * assume all risks and liabilities associated with its use.
  *
  * See www.webmacro.org for more information on the WebMacro project.  
  */
 
 
 package org.webmacro.engine;
 
 import java.util.*;
 import java.lang.reflect.*;
 
 import org.webmacro.*;
 import org.webmacro.util.*;
 import org.webmacro.resource.*;
 
 final public class PropertyOperatorCache { 
 
    private CacheManager _cache; 
    private Log _log;
    private HashMap _restrictedClasses;
 
    public PropertyOperatorCache() { 
    }
    
    final public void init(Broker b, Settings config) throws InitException
    {
       String cacheManager;
 
       _log = b.getLog("resource", "Object loading and caching");
 
       cacheManager = b.getSetting("PropertyOperator.CacheManager");
       if (cacheManager == null || cacheManager.equals("")) {
          _log.info("CachingProvider: No cache manager specified for PropertyOperator, using StaticSMapCacheManager");
          _cache = new StaticSMapCacheManager();
       }
       else {
          try {
             _cache = (CacheManager) b.classForName(cacheManager).newInstance();
          }
          catch (Exception e) {
             _log.warning("Unable to load cache manager " + cacheManager 
                          + " for PropertyOperator, using StaticSMapCacheManager.  Reason:\n" + e);
             _cache = new StaticSMapCacheManager();
          }
       }
       _cache.init(b, config, "PropertyOperator");
       _restrictedClasses = new HashMap(11);
       String restrictList = b.getSetting("RestrictedClasses");
       if (restrictList != null){
          StringTokenizer stok = new StringTokenizer(restrictList, ",");
          while (stok.hasMoreTokens()){
              String className = stok.nextToken();
              try {
                  Class c = Class.forName(className);
                  String okMethList = b.getSetting("RestrictedClasses.AllowedMethods." + className);
                  ArrayList okMeths = null;
                  if (okMethList != null){
                      okMeths = new ArrayList(20);
                      StringTokenizer stok2 = new StringTokenizer(okMethList, ",");
                      while (stok2.hasMoreTokens()){
                          okMeths.add(stok2.nextToken());
                      }
                  }
                  _restrictedClasses.put(c, okMeths);       
              }
              catch (Exception e){
                  _log.error("Configuration error: restricted class " + className 
                  + " cannot be loaded", e);
              }
          }
       }
    }
 
    Map getRestrictedClassMap(){
        return _restrictedClasses;
    }
    
    final public PropertyOperator getOperator(final Class type)
    throws PropertyException {   
       Object o = _cache.get(type);
       if (o == null) {
          PropertyOperator po = new PropertyOperator(type, this);
          _cache.put(type, po);
          return po;
       }
       else 
         return (PropertyOperator) o;
    }
 
    final public PropertyOperator getOperator(final Object obj)
    throws PropertyException {
        Class type = obj.getClass();
        // added code to handle static classes.  Static classes must be wrapped
        // in a StaticClassWrapper
        // 1Jun2001 - Keats
        if (type == org.webmacro.engine.StaticClassWrapper.class){
            type = ((org.webmacro.engine.StaticClassWrapper)obj).get();
        }
        return getOperator(type);
    }
 
    /**
      * Attempt to retrieve a property using the rules of property 
      * introspection described above. Begin reading names at position 
      * start in the array of names.
      * @param context is used to resolve sub-properties in arguments
      * @param instance is the root of introspection
      * @param names property names, one per array entry 
      * @return the property described by the names, inside the instance
      * @exception PropertyException the property we'd like to look at
      * @exception SecurityExeption you are not permitted to try
      */
    final public Object getProperty(final Context context, 
                                    final Object instance, 
                                    final Object[] names, 
                                    int start) 
    throws PropertyException, SecurityException {
       if (instance == null) {
          return null;
       } else {
          return getOperator(instance).getProperty(
             context,instance,names,start,names.length - 1);
       }
    }
 
    /**
      * Calls getProperty(context, instance, names, 0)
      */
    final public Object getProperty(final Context context, 
                                    final Object instance, 
                                    final Object[] names) 
    throws PropertyException, SecurityException {
       return getProperty(context, instance, names, 0);
    }
 
 
    /**
      * Given a property description name, attempt to set the property
      * value to the supplied object.
      * @param context An object containing a property
      * @param names The string names of that property 
      * @param value the new value the property is to be set to
      * @exception PropertyException not possible to set the property
      * @exception SecurityException you are not permitted to try
      */
    final public boolean setProperty(final Context context, 
                                     Object instance, 
                                     final Object[] names, 
                                     int start, 
                                     final Object value) 
    throws PropertyException, SecurityException {
       try {
          if (instance == null) {
             return false;
          }
          return getOperator(instance)
             .setProperty(context,instance,names,value,start);
       }
       catch (PropertyException e) {
          throw e;
       }
       catch (NoSuchMethodException e) {
          throw new PropertyException("No method to access property: " + e, e);
       }
    }
    
    /**
      * Calls setProperty(context, names, 0, value)
      */
    final public boolean setProperty(final Context context, 
                                     final Object instance, 
                                     final Object[] names, 
                                     final Object value) 
    throws PropertyException, SecurityException { 
       return setProperty(context,instance,names,0,value);
    }
  
    /**
      * Evaluate the supplied object and work out a way to return it 
      * as an iterator.
      * @param context an object believed to represent a list
      * @return an Iterator that iterates through that list
      * @exception PropertyException could not extract iterator from instance
      */
    final public Iterator getIterator(Object instance)
    throws PropertyException {
      if (instance instanceof Object[]) 
        return new ArrayIterator((Object[]) instance);
      else if (instance.getClass().isArray()) 
        return new PrimitiveArrayIterator(instance);
      else if (instance instanceof Iterator) 
        return (Iterator) instance;
      else if (instance instanceof Enumeration) 
        return new EnumIterator((Enumeration) instance);
      else 
        return getOperator(instance).findIterator(instance);
    }
 
 }
 
 
 
 /**
   * This class knows how to extract properties from objects efficiently.
   * <p>
   * A simple property "Bar" can be accessed in a sub-object "Foo" (and
   * in one case, sub-object "Baz") using any of the following method 
   * signatures, listed in the order that they will be tried:
   * <ul>
   * <li>Foo.Bar
   * <li>Foo.getBar() / Foo.setBar(object)
   * <li>Foo.getBar("Baz") / Foo.setBar("Baz", object)
   * <li>Foo.get("Bar") / Foo.put("Bar",object)
   * </ul>
   * <p>
   * The PropertyOperator is capable of extracting an iterator from an object
   * if in any of the following conditions are true, listed in the order that
   * they will be tried:
   * <ul>
   * <li>The object itself is an array
   * <li>The object itself is an Iterator
   * <li>The object itself is an Enumeration
   * <li>The object has an "Iterator iterator()" method
   * <li>The object has an "Enumeration elements()" method
   * </ul>
   * <p>
   * You can specify a long list of property names, and the above methods
   * will be recursively applied. For example, if your list were 
   * Order,Customer,Fred,Name,Last the PropertyOperator might be 
   * able to access it as:<pre>
   *     Order.getCustomer("Fred").getName().Last
   * </pre>
   */
 
 final class PropertyOperator
 {
    /**
      * My accessors for fields, and binary methods
      */
    final private HashMap _unaryAccessors = new HashMap();
 
    /**
      * Accessors that require an additional property name
      */
    final private HashMap _binaryAccessors = new HashMap();
 
    /**
      * Accessors for direct method calls
      */
    final private HashMap _directAccessors = new HashMap();
 
    /**
      * Hash table accessor
      */
    private BinaryMethodAccessor _hashAccessor;
 
    /**
      * The iterator method we found
      */
    private Method iteratorMethod = null;
 
    /**
     * An accessor for array lengths
     */
    private static LengthAccessor _lengthAccessor = new LengthAccessor();
 
    /** 
     * The property operator cache 
     */
    final private PropertyOperatorCache _cache;
 
    /**
      * Get the public methods for the named class. The vector meths 
      * will be populated by a list of all the methods. Note that a method
      * may appear more than once in the vector if it is declared in more
      * than one superclass or interface.
      */
    private void getAllMethods(HashMap meths, Class c)
       throws SecurityException
    {
       if (Modifier.isPublic( c.getModifiers() ) ) {
          Method m[] = c.getDeclaredMethods();
          for (int i = 0; i < m.length; i++) {
             if ( Modifier.isPublic( m[i].getModifiers() ) ) {
                addMethod(meths, m[i]);
             }
          }
       }
       Class iface[] = c.getInterfaces();
       for (int i = 0; i < iface.length; i++) {
          getAllMethods(meths, iface[i]);
       }
 
       Class sup = c.getSuperclass();
       if (sup != null) {
          getAllMethods(meths, sup);
       }
    }
 
    /**
      * The lhs precedes the rhs if it has fewer parameters. If the lhs 
      * has the same number of parameters then the lhs precedes the rhs 
      * if it can be used anywhere the rhs can be used--meaning that for
      * each and every term, the lhs is the same or more specific than
      * the rhs. If they have the same number of parameters but are not
      * related at all, put the lhs later.
      */
    private int precedes(Class[] lhs, Class[] rhs)
    {
 
       if (lhs.length == rhs.length) {
          for (int i = 0; i < lhs.length; i++) {
 
             if (! rhs[i].equals(lhs[i])) {
 
                if (lhs[i].isAssignableFrom(rhs[i])) {
                   // rhs is more specific than lhs
                   return 1;
                }
 
                if (rhs[i].isAssignableFrom(lhs[i])) {
                   // lhs is more specific than rhs
                   return -1;
                }
 
                // not related by inheritance, put lhs later on
                return 1;
 
             }
          }
          return 0; // all the same
       } else {
          return (lhs.length < rhs.length) ? -1 : 1;
       }
    }
 
    boolean isMethodAllowed(Method m){
       Class c = m.getDeclaringClass();
       Map restrictedClasses = _cache.getRestrictedClassMap();
       if (restrictedClasses.containsKey(c)){
           // this class is restricted, check if method is allowed
           List okMeths = (List)restrictedClasses.get(c);
           return (okMeths != null && okMeths.contains(m.getName()));
       }
       return true;
    }
    
    boolean isMethodRestricted(Class c, String name){
      // check if object is restricted
      Map restrictedClassMap = _cache.getRestrictedClassMap();
      if (!restrictedClassMap.containsKey(c)){
          return false; // there are no restrictions on this class
      }
      else 
      {
          // restricted class, check method
          Method[] meths = c.getMethods();
          for (int i=0; i<meths.length; i++){
              if (meths[i].getName().equals(name)){
                 // check if method is explicitly allowed
                  List l = (List)restrictedClassMap.get(c);
                  if (l != null)
                      return !l.contains(name);
              }
         }
      }
      return false;
    }
    
    private void addMethod(HashMap hm, Method m) {
       if (!isMethodAllowed(m)) return;
       String name = m.getName();
       Object o = hm.get( name );
       if (o == null) {
          hm.put(name, m);
          return;
       }
 
       Vector v;
       if (o instanceof Method) {
          v = new Vector();
          v.addElement(o);
          hm.put(name, v);
       } else {
          v = (Vector) o;
       }
 
       Class ptypes[] = m.getParameterTypes();
       for (int i = 0; i < v.size(); i++) {
          Class curTypes[] = ((Method) v.elementAt(i)).getParameterTypes();
 
          int order = precedes(ptypes, curTypes);
 
          if (order < 0) {
             v.insertElementAt(m,i);
             return;
          } else if (order == 0) {
             // ignore duplicate method
             return;
          }
       }
       v.addElement(m);
    }
 
    /**
      * Get all the public methods of the supplied class. They will be 
      * returned in arbitrary alphabetical order, but where there are 
      * multiple methods with the same name, they will be returned in 
      * order of precedence: least arguments first, and most specific 
      * arguments before less specific arguments. See precedes().
      */
    private Vector getMethods(Class c) {
       Vector v = new Vector();
       HashMap h = new HashMap();
       getAllMethods(h,c);
       Iterator iter = h.values().iterator();
       while (iter.hasNext()) {
          Object elem = iter.next();
 
          if (elem instanceof Method) {
             v.addElement( elem );
          } else {
             Vector v1 = (Vector) elem;
             for (int i = 0; i < v1.size(); i++) {
                v.addElement( v1.elementAt(i) );
             }
          }
       }
       return v;
    }
 
 
    /**
      * Construct a property operator for the target class
      */
    public PropertyOperator(final Class target, PropertyOperatorCache cache)
       throws SecurityException, PropertyException
    {
 
       Accessor acc;
 
       // Save the cache
       _cache = cache;
 
       // introspect fields first
 
       Field[] fields = target.getFields();
       for (int i = 0; i < fields.length; i++) {
          if (Modifier.isPublic(fields[i].getModifiers())) {
             acc = new FieldAccessor(fields[i]);
             _unaryAccessors.put(acc.getName(),acc);
          } 
       }
       if (target.isArray()) 
         _unaryAccessors.put("length", _lengthAccessor);
 
       // introspect methods second
 
       Vector methods = getMethods(target);
 
       Method meth;
       Class[] params;
       String name,propName;
 
       for (int i = 0; i < methods.size(); i++) 
       {
          meth = ((Method) methods.elementAt(i));
          if (!isMethodAllowed(meth)) continue;
          name = meth.getName();
          params = meth.getParameterTypes();
          int plength = params.length;
 
          // add direct accessor
          acc = (Accessor) _directAccessors.get(name);
          if (acc != null) {
             ((DirectAccessor) acc).addMethod(meth, params); 
          } else {
             acc = new DirectAccessor(name,meth,params);
             _directAccessors.put(name,acc);
          }
 
          // check for get/set/put method
          if ((name.startsWith("get") || 
                name.startsWith("set")) || name.equals("put"))
          {
 
             propName = name.substring(3);
 
             if ( ((plength == 0) && name.startsWith("get"))  ||
                  ((plength == 1) && name.startsWith("set")) )
             {
 
                // unary get/set method
                acc = (Accessor) _unaryAccessors.get(propName);
                if (acc != null) {
                   if (acc instanceof MethodAccessor) {
                      ((MethodAccessor) acc).addMethod(meth, params);
                   } 
                } else {
                   acc = new UnaryMethodAccessor(propName,meth,params);
                   _unaryAccessors.put(propName,acc);
                }
             } else if ( (plength > 0) && (
                           (params[0].isInstance("string") && 
                            ((plength == 2) && name.equals("put"))) ||
                           ((plength == 1) && name.equals("get"))))
             {
                // hashtable get/put
                if (_hashAccessor != null) {
                   _hashAccessor.addMethod(meth, params);
                } else {
                   _hashAccessor = new BinaryMethodAccessor(propName,meth,params);
                }
             } else if ((plength > 0) && (params[0].isInstance("string")) &&
                        (((plength == 1) && name.startsWith("get")) ||
                         ((plength == 2) && name.startsWith("set"))))
             {
                // binary get/set method
                acc = (Accessor) _binaryAccessors.get(propName);
                if (acc != null) {
                   ((MethodAccessor) acc).addMethod(meth, params);
                } else {
                   acc = new BinaryMethodAccessor(propName,meth,params);
                   _binaryAccessors.put(propName,acc);
                }
             }
          } else if (name.startsWith("is") && meth.getReturnType() == java.lang.Boolean.TYPE){
              if (plength == 0){
                // unary accessor method
                 propName = name.substring(2);
                 acc = (Accessor) _unaryAccessors.get(propName);
                 if (acc != null) {
                   if (acc instanceof MethodAccessor) {
                      ((MethodAccessor) acc).addMethod(meth, params);
                   } 
                 } else {
                   acc = new UnaryMethodAccessor(propName,meth,params);
                   _unaryAccessors.put(propName,acc);
                 }             
              }
          } else if (name.equals("elements") || 
                     name.equals("enumeration") ||
                     name.equals("iterator") ||
                     name.equals("toArray")) 
          {
             if (params.length == 0) {
                Class returnType = meth.getReturnType();
 
                // iterator supercedes enumeration supercedes Object[]
                Class iterClass = Iterator.class;
                boolean iterA = iterClass.isAssignableFrom(returnType);
                if (
                     iterA ||
                     (((iteratorMethod == null) || 
                        iteratorMethod.getName().equals("toArray")) &&
                      Object[].class.isAssignableFrom(returnType) ||
                        Enumeration.class.isAssignableFrom(returnType)))
                {
                   iteratorMethod = meth;
                }
 
             }
          }
       }
    }
 
 
    /**
      * Locate the requested property as follows: beginning from instance,
      * look at names[start] and resolve it, and continue resolving names 
      * recursively until names[end] has been resolved. Return that.
      * @param instance object to start from 
      * @param names the property names we are searching for 
      * @param start which name to look for first 
      * @param end which name to look for lst
      * @exception PropertyException error resolving a name 
      * @exception NoSuchMethodException no method available for name
      * @return the property requested
      *
      */
     public Object getProperty(final Context context, 
                               final Object instance, 
                               final Object[] names, 
                               int start, int end) 
       throws PropertyException
    {
       String prop;
       Object nextProp = null;
       Accessor acc = null;
 
       if (names[start] instanceof String) {
          prop = (String) names[start];
       } else if (names[start] instanceof PropertyMethod) {
          PropertyMethod pm = (PropertyMethod) names[start];
          prop = pm.getName();
          acc = (Accessor) _directAccessors.get(prop);
          Object[] args = pm.getArguments(context);
          if (acc == null) {
             if (isMethodRestricted(instance.getClass(), prop))
                 throw new PropertyException.RestrictedMethodException(
                         pm.toString(), 
                         fillInName(names, start), 
                         instance.getClass().getName());
             throw new PropertyException.NoSuchMethodException(
                         pm.toString(), 
                         fillInName(names, start), 
                         instance.getClass().getName());
          }
          try {
             nextProp = acc.get(instance,args);
             start++;
          } catch (NoSuchMethodException e) {
             throw new PropertyException.NoSuchMethodException(
                         pm.toString(), 
                         fillInName(names, start), 
                         instance.getClass().getName());
          }
 
       } else {
          prop = names[start].toString();
       }
 
       // unary?
       if (acc == null) {
          acc = (Accessor) _unaryAccessors.get(prop);
          if (acc != null) {
             try {
                nextProp = acc.get(instance);
                start++;
             } catch (NoSuchMethodException e) { 
                acc = null;
             }
          }
       } 
 
       // binary?
       if (acc == null) {
          acc = (Accessor) _binaryAccessors.get(prop);
          if ((acc != null) && ( (start+1) <= end) ) {
             try {
                nextProp = acc.get(instance, (String) names[start + 1]);
                start += 2; 
             } catch (NoSuchMethodException e) {
                acc = null;
             } catch (ClassCastException e) {
                // names[start + 1] was not a String, just move on
                // this catch is more efficient than using instanceof
                // since 90% of the time it really will be a string
                acc = null;
             }
          }  else {
             acc = null;
          }
       } 
 
       // hash?
       if (acc == null) {
          acc = _hashAccessor;
          try {
             if (acc != null) {
                nextProp = acc.get(instance,prop);
                start++;
             }
          } catch (NoSuchMethodException e) {
             acc = null;
          }
       } 
       
       if (acc == null) {
          // user tried to access a property of a property that doesn't exist
          // ex:  $TestObject.FirstName.NotThere
 
          // check if object is restricted
          if (isMethodRestricted(instance.getClass(), "get" + prop)
             || isMethodRestricted(instance.getClass(), "set" + prop))
              throw new PropertyException.RestrictedPropertyException(
                      prop, fillInName(names, start), 
                      instance.getClass().getName());          
          throw new PropertyException.NoSuchPropertyException(
                      prop, fillInName(names, start), 
                      instance.getClass().getName());
       }
 
       if (start <= end) {
          try {
            return _cache.getOperator(nextProp)
              .getProperty(context,nextProp,names,start,end);
          } catch (NullPointerException e) {
              // will we ever get here? --eric
             throw new PropertyException("No way to access property " + 
                  fillInName(names, start) + " on object " + instance + " of " 
                   + instance.getClass() + "--possibly null?");
          }
       } else {
          return nextProp;
       }
    }
    
    /**
     * given an object[] of names, append them together up to index
     * <code>end</code> in the form of
     * <code>Name1.Name2.Name3.NameN</code> 
     */
    private static final String fillInName (Object[] names, int end)
    {
         StringBuffer sb = new StringBuffer ();
         for (int x=0; x<end; x++)
         {
             if (x > 0)
                 sb.append (".");
             sb.append (names[x]);
         }
         
         return sb.toString ();
    }
 
    /**
      * This method behaves a lot like getProperty, but it's tricker. It 
      * first tries to resolve the property using a direct method, then 
      * it falls back and tries a binary approach last. In order to do 
      * this it has to recurse into the direct approach, then detect if 
      * that failed and try the binary approach. It relies on getProperty
      * for navigation, which is why getProperty takes start/end args.
      * @param instance the object to start from
      * @param names path to a property we would like to set 
      * @param value the value we'd like to set it to 
      * @param pos   we could set names[pos] from here
      * @return true if we succeeded in setting, false otherwise
      */
    public boolean setProperty(Context context, 
                               Object instance, 
                               Object[] names, 
                               Object value, 
                               int pos) 
       throws PropertyException, NoSuchMethodException
    {
       // names[pos] is what we could set from here
 
       int parentPos = names.length - 1;
       int binPos = parentPos - 1;
 
       // if we're not yet at the binary-settable parent, go there
       if (pos < binPos) {
          Object grandparent = getProperty(context, instance, names, 
                                           pos, binPos - 1);
          PropertyOperator po = _cache.getOperator(grandparent);
          return po.setProperty(context,grandparent,names,value,binPos);
       } 
 
       // if we're at the binary-settable parent, try direct first
       if (pos == binPos) {
 
          // try direct -- move to direct parent and try from there
          Object parent = null;
          try {
             parent = getProperty(context,instance,names,pos,pos);
             if (parent != null) {
                PropertyOperator po = _cache.getOperator(parent);
                if (po.setProperty(context,parent,names,value,pos+1)) {
                   return true;
                }
             }
          } catch (NoSuchMethodException e) {
             // oh well, keep trying: XXX this makes binOp expensive
          }
 
          // if direct failed, try binary
          Accessor binOp = (Accessor) _binaryAccessors.get(names[pos]);
          if (binOp != null) {
             try {
                return binOp.set(instance,(String) names[pos+1],value);
             } catch (ClassCastException e) {
                // names[pos+1] was not a string, just move on
                return false;
             } catch (NoSuchMethodException e) {
                return false;
             }
          }
          return false;
       }
 
       // we're the direct parent, use unaryOp or hash method
       Accessor unaryOp = (Accessor) _unaryAccessors.get(names[pos]);
       try {
          if ((unaryOp != null) && unaryOp.set(instance,value)) {
             return true;
          }
          if (_hashAccessor != null) {
             return _hashAccessor.set(instance,(String) names[pos],value);
          }
       } catch(NoSuchMethodException e) {
          // fall through
       } catch(ClassCastException e) {
          // names[pos] was not a string, fall through
       }
       return false;
    }
 
    /**
      * Introspect the current object and return an Iterator representation
      * of it, if possible.
      * @param instance the object we think contains a list
      * @exception PropertyException current object is not any sort of list
      * @return an iterator representing the current object's list
      */
    public Iterator findIterator(Object instance)
       throws PropertyException
    {
       if (iteratorMethod != null) {
          try {
                Object ret = invoke(iteratorMethod,instance,null);
             if (ret instanceof Iterator) {
                return (Iterator) ret;
             } else if (ret instanceof Enumeration) {
                return new EnumIterator((Enumeration) ret);
             } else if (ret instanceof Object[]) {
                return new ArrayIterator((Object[]) ret);
             }
          } catch (NoSuchMethodException e) {
             throw new PropertyException("Error in PropertyOperator!",e);
          }
       }
       throw new PropertyException(instance + " is not a list",null);
    }
 
    /**
      * Invoke a method on an instance, with arguments--generate 
      * PropertyException rather than the default Java exceptions.
      * @param meth the method to invoke
      * @param instance the object to invoke it on
      * @param args arguments for the method
      * @return return value of the method
      */
    static Object invoke(Method meth, Object instance, Object[] args)
       throws PropertyException, NoSuchMethodException
    {
       try {
          Object obj = meth.invoke (instance, args);
          
          // if the method's return type is void return the VoidMacro
          // instance, instead of the 'null' we used to return here
          // otherwise, just return whatever the method returned
          if (obj == null 
              && meth.getReturnType() == java.lang.Void.TYPE)
             return org.webmacro.engine.VoidMacro.instance;
          else
             return obj;
          
       } catch (IllegalAccessException e) {
          throw new PropertyException(
             "You don't have permission to access the requested method (" +
             meth + " in class " + instance.getClass() + 
             " on object " + instance + "). Private/protected/package access " +
             " values cannot be accessed via property introspection.",e);
       } catch (IllegalArgumentException e) {
          throw new PropertyException(
             "Some kind of error occurred processing your request: this " + 
             "indicates a failure in PropertyOperator.java that should be " +
             "reported: attempt to access method " + meth + " on object " +
             instance + " with " +args.length + " parameters " +
             " threw an exception: " + e,e);
       } catch (InvocationTargetException e) {
          throw new PropertyException(
             "Attempt to invoke method " + meth + " on object " 
             + instance + " of " + instance.getClass() + 
             " raised an exception: " + e.getTargetException(),
             e.getTargetException());
       } catch (NullPointerException e) {
          throw new PropertyException(
             "NullPointerException thrown from method " + meth +
             " on object " + instance + " -- most likely you have attempted " + 
             "to use an undefined value, or a failure in that method.",e);
       }
    }
 }
 
 
 
 // helper classes
 
 
 /**
   * An accessor represents one particular operation that can be 
   * performed on one particular class: getting/setting a field, or 
   * getting/setting via a method.
   */
 abstract class Accessor {
 
    private String _name;
 
    Accessor(String name) {
       _name = name;
    }
 
    final String  getName() {
       return _name;
    }
 
    public final String toString() {
       return "Accessor:" + _name;
    }
  
    /**
      * Unary get
      */
    Object get(Object instance) 
       throws PropertyException, NoSuchMethodException
    {
       throw new PropertyException("BUG in PropertyOperator.java!");
    }
 
    /**
      * Unary set
      */
    boolean set(Object instance, Object value) 
       throws PropertyException, NoSuchMethodException
    {
       throw new PropertyException("BUG in PropertyOperator.java!");
    }
 
    /**
      * Binary get
      */
    Object get(Object instance, String subName) 
       throws PropertyException, NoSuchMethodException
    {
       throw new PropertyException("BUG in PropertyOperator.java!");
    }
 
    /**
      * Binary
      */
    boolean set(Object instance, String subName, Object value)
       throws PropertyException, NoSuchMethodException
    {
       throw new PropertyException("BUG in PropertyOperator.java!");
    }
 
 
    /**
      * Direct get
      */
    Object get(Object instance, Object[] args)
       throws PropertyException, NoSuchMethodException
    {
       throw new PropertyException("BUG in PropertyOperator.java!");
    }
 
 }
 
 
 /**
   * An accessor that knows how to get/set from a field
   */
 final class FieldAccessor extends Accessor
 {
    private Field _field;
 
    FieldAccessor(final Field f) {
       super(f.getName());
       _field = f;
    }
 
    final Object get(final Object instance)
       throws PropertyException
    {
       try {
          return _field.get(instance);
       } catch (Exception e) {
          throw new PropertyException("Unable to read field " + _field +
                " on object " + instance + " of " + instance.getClass(),
                e);
       }
    }
 
    final boolean set(final Object instance, final Object value)
       throws PropertyException
    {
       try {
          _field.set(instance,value);
       } catch (Exception e) {
          throw new PropertyException("Unable to write field " + _field +
                " on object " + instance + " of " + instance.getClass(),
                e);
       }
       return true;
    }
 }
 
 
 /**
   * An accessor that knows how to get/set from a field
   */
 final class LengthAccessor extends Accessor
 {
    LengthAccessor() {
       super("length");
    }
 
    final Object get(final Object instance)
       throws PropertyException
    {
       try {
          return new Integer(java.lang.reflect.Array.getLength(instance));
       } catch (Exception e) {
          throw new PropertyException("Unable to fetch length of object " 
                                      + instance + " of " + instance.getClass(),
                                      e);
       }
    }
 
    final boolean set(final Object instance, final Object value)
       throws PropertyException {
       throw new PropertyException("Cannot set length of array");
    }
 }
 
 
 /**
   * accessor for direct method calls, rather than property-style
   */
 final class DirectAccessor extends Accessor
 {
 
    Vector _methods = new Vector();
 
    DirectAccessor(final String name, final Method m, final Class[] params)
    {
       super(name);
       addMethod(m, params);
    }
 
    final void addMethod(final Method m, Class[] params)
    {
       _methods.addElement(m);
    }
 
 
    final boolean matches(Class[] sig, Class[] args)
    {
       if (args.length != sig.length) 
          return false;
 
       try {
          for (int i = 0; i < sig.length; i++) {
             Class s = sig[i];
             Class a = args[i];
             if (s.isPrimitive()) {
                if ((s == Integer.TYPE && a == Integer.class) ||
                    (s == Boolean.TYPE && a == Boolean.class) || 
                    (s == Character.TYPE && a == Character.class) ||  
                    (s == Long.TYPE && a == Long.class) ||  
                    (s == Short.TYPE && a == Short.class) ||  
                    (s == Double.TYPE && a == Double.class) ||  
                    (s == Float.TYPE && a == Float.class) ||  
                    (s == Void.TYPE && a == Void.class) ||  
                    (s == Byte.TYPE && a == Byte.class)) 
                   continue;
                else
                   return false;
             }
             else if (a == null || s.isAssignableFrom(a)) 
                continue;
             else 
                return false;
          }
       }
       catch (NullPointerException e) {
          return false; // XXX: block nulls, isAssign... throws this
       }
       return true;
    }
 
    final Object get(Object instance, Object[] args)
       throws PropertyException, NoSuchMethodException
    {
       Class[] types = new Class[ args.length ];
       for (int i = 0; i < args.length; i++) {
          try {
             types[i] = args[i].getClass();
          } catch (NullPointerException e) {
             types[i] = null;
          }
       }
 
       for (int i = 0; i < _methods.size(); i++) {
          Method m = (Method) _methods.elementAt(i);
          Class[] sig = m.getParameterTypes();
          if (matches(sig,types)) {
             return PropertyOperator.invoke(m,instance,args);
          }
       }
 
       // not found
 
       StringBuffer msg = new StringBuffer();
       msg.append("No method ");
       msg.append(getName());
       msg.append("(");
       for (int i = 0; i < args.length; i++) {
          if (i > 0) {
             msg.append(",");
          }
          msg.append((args[i] == null) ? "null" : args[i].getClass().toString());
       }
       msg.append(") on object ");
       msg.append(instance);
 
       throw new PropertyException(msg.toString());
    }
    
 }
 
 
 abstract class MethodAccessor extends Accessor
 {
    protected Method _getMethod;           // only one get method allowed
    private Method[] _setMethods = null; // may be multiple set methods
    private Class[]  _setParams = null;  // variable arg type for set meth N
    private Class[]  _setPrimitiveType = null;
    private int setCount = 0;            // how many set methods do we have
 
    abstract int numArgsGet();
    abstract int numArgsSet();
 
    MethodAccessor(final String name, final Method m, final Class[] params) 
       throws PropertyException
    {
       super(name);
       addMethod(m,params);
    }
 
    private Class getWrapperClass(Class s) {
       if (s == Integer.TYPE)
          return Integer.class;
       else if (s == Boolean.TYPE)
          return Boolean.class;
       else if (s == Character.TYPE)
          return Character.class;
       else if (s == Long.TYPE)
          return Long.class;
       else if (s == Short.TYPE)
          return Short.class;
       else if (s == Double.TYPE)
          return Double.class;
       else if (s == Float.TYPE)
          return Float.class;
       else if (s == Void.TYPE)
          return Void.class;
       else if (s == Byte.TYPE)
          return Byte.class;
       else 
          return null;
    }
 
    final void addMethod(final Method m, Class[] params) 
       throws PropertyException
    {
 
       final int setArgsLength = numArgsSet();
       final int getArgsLength = numArgsGet();
 
       if (params.length == getArgsLength) {
          _getMethod = m;
       } else if (params.length == setArgsLength) {
          setCount++;
          if (_setMethods == null) {
             _setMethods = new Method[1];
             _setParams = new Class[1];
             _setPrimitiveType = new Class[1];
          } else if (_setMethods.length <= setCount) {
             Method[] tmpMethods  = new Method[ (setCount + 1) * 2 ];
             Class[] tmpParams    = new Class[(setCount + 1) * 2 ];
             Class[] tmpPrimitive = new Class[(setCount + 1) * 2 ];
             System.arraycopy(_setMethods,0,tmpMethods,0,_setMethods.length);
             System.arraycopy(_setParams,0,tmpParams,0,_setParams.length);
             System.arraycopy(_setPrimitiveType,0,tmpPrimitive,0,_setParams.length);
             _setMethods = tmpMethods;
             _setParams = tmpParams;
             _setPrimitiveType = tmpPrimitive;
          }
 
          // record the method, and the type of the variable parameter
          _setMethods[setCount - 1] = m;
          _setParams[setCount - 1] = params[setArgsLength - 1];
          if (_setParams[setCount-1].isPrimitive())
             _setPrimitiveType[setCount-1] 
                = getWrapperClass(_setParams[setCount-1]);
 
       } else {
          throw new PropertyException("PropertyOperator FAILED for method " 
                + m + "--please report this bug!");
       }
    }
 
    final boolean setImpl(final Object inst, final Object[] args) 
       throws PropertyException, NoSuchMethodException
    {
       //which method to use? check params for first match
       for (int i = 0; i < setCount; i++) {
          Object arg = args[args.length - 1];
          // XXX: null values are blocked by the next line
          if (_setParams[i].isPrimitive()) {
             if (arg.getClass() == _setPrimitiveType[i]) {
                try {
                   PropertyOperator.invoke(_setMethods[i],inst,args);
                   return true;
                } catch (Exception e){
                   // ignore exception
                }
             }
          }
          else if (arg == null 
                   || _setParams[i].isInstance(args[args.length - 1])) {
             PropertyOperator.invoke(_setMethods[i],inst,args);
             return true;
          }
       }
       return false;
    }
 
 }
 
 final class UnaryMethodAccessor extends MethodAccessor
 {
 
    UnaryMethodAccessor(final String name, final Method m, final Class[] params) 
       throws PropertyException
    {
       super(name,m,params);
    }
 
    final int numArgsGet() {
       return 0;
    }
 
    final int numArgsSet() {
       return 1;
    }
 
    final Object get(final Object instance)
       throws PropertyException, NoSuchMethodException
    {
       return PropertyOperator.invoke(_getMethod,instance,null);      
    }
 
    final boolean set(final Object instance, final Object value) 
       throws PropertyException, NoSuchMethodException
    {
       Object[] args = new Object[1];
       args[0] = value;
       return setImpl(instance,args);
    }
 
 }
 
 final class BinaryMethodAccessor extends MethodAccessor
 {
    
    BinaryMethodAccessor(String name, Method m, Class[] params) 
       throws PropertyException
    {      
       super( name,m,params);
    }
 
    final int numArgsGet() {
       return 1;
    }
 
    final int numArgsSet() {
       return 2;
    }
 
    final Object get(final Object instance, String prop)
       throws PropertyException, NoSuchMethodException
    {
       Object[] args = new Object[1];
       args[0] = prop;
       return PropertyOperator.invoke(_getMethod,instance,args);      
    }
 
    final boolean set(final Object instance, String prop, Object value)
       throws PropertyException, NoSuchMethodException
    {
       Object[] args = new Object[2];
       args[0] = prop;
       args[1] = value;
       return setImpl(instance,args);
    }
 }
