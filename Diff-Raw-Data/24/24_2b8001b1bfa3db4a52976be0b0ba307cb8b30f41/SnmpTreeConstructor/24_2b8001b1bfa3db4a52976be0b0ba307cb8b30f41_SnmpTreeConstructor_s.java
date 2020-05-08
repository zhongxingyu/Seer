 package com.sppad.snmp.constructor;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.lang.reflect.Type;
 import java.util.ArrayDeque;
 import java.util.Collection;
 import java.util.Deque;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import org.snmp4j.smi.OID;
 
 import com.google.common.base.Preconditions;
 import com.google.common.cache.CacheBuilder;
 import com.google.common.cache.CacheLoader;
 import com.google.common.cache.LoadingCache;
 import com.google.common.collect.ImmutableCollection;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableMap;
 import com.sppad.common.object.ObjUtils;
 import com.sppad.datastructures.primative.IntStack;
 import com.sppad.datastructures.primative.RefStack;
 import com.sppad.snmp.JotsOID;
 import com.sppad.snmp.annotations.SnmpIgnore;
 import com.sppad.snmp.annotations.SnmpInclude;
 import com.sppad.snmp.annotations.SnmpName;
 import com.sppad.snmp.annotations.SnmpNotSettable;
 import com.sppad.snmp.constructor.handlers.CollectionHandler;
 import com.sppad.snmp.constructor.handlers.DefaultHandler;
 import com.sppad.snmp.constructor.handlers.MapHandler;
 import com.sppad.snmp.constructor.handlers.NullHandler;
 import com.sppad.snmp.constructor.handlers.ObjectHandler;
 import com.sppad.snmp.constructor.mib.MibConstructor;
 import com.sppad.snmp.constructor.mib.SnmpDescription;
 import com.sppad.snmp.util.SnmpUtils;
 
 /**
  * 
  * Creates an SNMP tree object allowing for gets and sets on any fields
  * belonging to the specified object or any object referenced through that
  * object.
  * <p>
  * Values that can be get/set are Strings, primitives and their corresponding
  * classes (e.g. Integer). Collections and Maps are also traversed to add the
  * objects they contain through the tree.
  * <p>
  * Each time an object is descended into, the OID increases in length, creating
  * a subtree starting with ".1". On each next field of an object, the last part
  * of the OID is incremented by one. When a Collection or Map is encountered,
  * the OID gets a level of dynamic OID added per level of Collection or Map
  * encountered.
  * <p>
  * Since SNMP does not allow for tables within entries, multi-indexed tables are
  * generated whenever a nested table is encountered.
  * 
  * 
  * <pre>
  * 
  * For example, the transformation may look like:
  * 
  * .1 (Table of Foo)
  * .1.1 (FooEntry)
  * .1.1.1.1001 (Foo.firstField, index = 1001)
  * .1.1.2.1001 (Table of Bar)
  * .1.1.2.1.1001.5001 (Bar.firstField index = 5001)
  * 
  * transformed to:
  * 
  * .1 (Table of Foo)
  * .1.1 (FooEntry)
  * .1.1.1001 (Foo.firstField, index = 1001)
  * .1.2 (Table of Bar)
  * .1.2.1 (BarEntry)
  * .1.2.1.1.1001.5001 (Bar.firstField key = index = 1001, 5001)
  * </pre>
  * 
  * @author sepand
  * 
  */
 public class SnmpTreeConstructor
 {
   private class FieldInfo
   {
     final String description;
     final Field field;
     final List<IntStack> oidVisitedMap = new LinkedList<IntStack>();
     final Method setter;
     final boolean shouldSkip;
     final boolean simple;
     final String snmpName;
     final boolean tableType;
 
     public FieldInfo(final Field field)
     {
       SnmpName snmpNameObj = field.getAnnotation(SnmpName.class);
 
       this.description = createMib ? SnmpDescription.getDescription(field)
           : null;
       this.field = field;
       this.setter = getSetterMethod(field, field.getDeclaringClass());
       this.shouldSkip = checkIfShouldSkip(field);
       this.simple = SnmpUtils.isSimple(field.getType());
       this.snmpName = (snmpNameObj != null) ? snmpNameObj.value() : field
           .getName();
      this.tableType = getHandler(field.getType()) != defaultHandler;
     }
 
     public boolean isWritable()
     {
       return setter != null;
     }
 
     /**
      * Checks if a field should be included based on the following factors:
      * <ul>
      * <li>{@link SnmpInclude} annotation, if present
      * <li>{@link SnmpIgnore} annotation, if present
      * <li>If the field is static
      * <li>If the field is transient
      * <li>If the field is reference to an outer class
      * <li>If the field is final
      * <li>If the field simple, see: {@link SnmpUtils#isSimple(Class)}
      * </ul>
      * 
      * @param field
      *          A field to check if it should be included or not
      * @return True if it should be included, false otherwise
      */
     private boolean checkIfShouldSkip(final Field field)
     {
       final int modifiers = field.getModifiers();
 
       final boolean include = field.getAnnotation(SnmpInclude.class) != null;
       final boolean ignore = field.getAnnotation(SnmpIgnore.class) != null;
 
       if (include)
         return true;
 
       if (ignore || Modifier.isStatic(modifiers) ||
           Modifier.isTransient(modifiers) || field.getName().equals("this$0"))
         return false;
 
       if (Modifier.isFinal(modifiers) || SnmpUtils.isSimple(field.getType()))
         return true;
 
       return false;
     }
   }
 
   private final LoadingCache<Class<?>, ClassInfo> classInfoCache = CacheBuilder
       .newBuilder().maximumSize(1000)
       .build(new CacheLoader<Class<?>, ClassInfo>()
       {
         @Override
         public ClassInfo load(final Class<?> key)
         {
           return new ClassInfo(key);
         }
       });
 
   /** Whether or not to create a MIB file while creating the tree */
   private final boolean createMib;
 
   /** Handlers all non-table, non-basic objects */
   private final DefaultHandler defaultHandler = new DefaultHandler();
 
   private final LoadingCache<Field, FieldInfo> fieldInfoCache = CacheBuilder
       .newBuilder().maximumSize(1000).build(new CacheLoader<Field, FieldInfo>()
       {
         @Override
         public FieldInfo load(final Field key)
         {
           return new FieldInfo(key);
         }
       });
 
   /**
    * Maps a type to a class implementing how to handle that type while
    * descending
    */
   private final Map<Type, ObjectHandler> handlers = new HashMap<Type, ObjectHandler>();
 
   /** Used for constructing a MIB, if requested */
   private MibConstructor mibConstructor;
 
   /** Used for building the MIB name for the current oid */
   private final Deque<String> nameStack = new ArrayDeque<String>();
 
   /** Handlers null objects */
   private final NullHandler nullHandler = new NullHandler();
 
   private final LoadingCache<Class<?>, ObjectHandler> objectHandlerCache = CacheBuilder
       .newBuilder().maximumSize(1000)
       .build(new CacheLoader<Class<?>, ObjectHandler>()
       {
         public Class<?> getFirstHandledClass(final Class<?> klass)
         {
 
           // check if the class is already handled
           if (handlers.containsKey(klass))
             return klass;
 
           // need to check all interfaces to see if they are handled
           for (Class<?> interfaceClass : klass.getInterfaces())
             if (handlers.containsKey(interfaceClass))
               return interfaceClass;
 
           // next, need to check all super classes
           Class<?> currentClass = klass;
           while ((currentClass = currentClass.getSuperclass()) != null)
             if (handlers.containsKey(currentClass))
               return currentClass;
 
           // nothing matches, just handle it as an object
           return Object.class;
         }
 
         @Override
         public ObjectHandler load(final Class<?> key)
         {
           return handlers.get(getFirstHandledClass(key));
         }
       });
 
   /** Used to detect cycles by checking for visited objects. */
   private final RefStack<Object> objectHandleStack = new RefStack<Object>();
 
   /** Keeps track of length of extensions on extension stack */
   private final IntStack oidExtLenStack = new IntStack();
 
   /** Keeps track of snmp oid extensions for tables */
   private final IntStack oidExtStack = new IntStack();
 
   /** Keeps track of snmp oid */
   private final IntStack oidStack = new IntStack();
 
   /** The table prefix, added to each OID before creating */
   private final int[] prefix;
 
   /**
    * Saves the OID stack for restoring after exiting a table. Used for nested
    * tables, since the OID needs to be modified.
    */
   private final Map<String, IntStack> savedOidStackMap = new HashMap<String, IntStack>();
 
   /** Object representing the snmp tree for doing lookups and sets */
   private final SnmpTreeSkeleton snmpTreeSkeleton;
 
   /**
    * Maps OID path to what OID in the parent they belong to. For dealing with
    * nested tables.
    */
   private final Map<String, Integer> subTableOidModMap = new HashMap<String, Integer>();
 
   /** Stack of the current table. */
   private final Deque<String> tableNameStack = new ArrayDeque<String>();
 
   /** What index in the OID stack corresponds to the parent table */
   private final IntStack tableOidIndexStack = new IntStack(10);
 
   /**
    * Creates an SnmpTree by descending starting with the given object.
    * 
    * @param prefix
    *          The OID prefix to use for the tree
    * @param obj
    *          The object to base the tree off of
    * @return The constructed SnmpTree
    * @throws InvocationTargetException
    * @throws IOException
    * 
    * @see SnmpTree
    */
   public static SnmpTree createSnmpTree(final int[] prefix, final Object obj)
       throws IllegalAccessException,
       InvocationTargetException,
       IOException
   {
     return createSnmpTree(prefix, obj, null);
   }
 
   public static SnmpTree createSnmpTree(
       final String mibName,
       final String rootName,
       final String parentName,
       final int[] prefix,
       final Object obj,
       final OutputStream mibOutputStream)
 
       throws IllegalAccessException,
       InvocationTargetException,
       IOException
   {
     Preconditions.checkNotNull(mibOutputStream);
 
     final MibConstructor mc = new MibConstructor(mibName, rootName, parentName,
         prefix[prefix.length - 1], mibOutputStream);
 
     return createSnmpTree(prefix, obj, mc);
   }
 
   protected static String buildStringPath(
       final Deque<String> names,
       final boolean includeLast)
   {
     final Iterator<String> nameIter = names.descendingIterator();
     final StringBuilder builder = new StringBuilder();
 
     while (nameIter.hasNext())
     {
       final String nextName = nameIter.next();
       if (!includeLast && !nameIter.hasNext())
         break;
 
       final int lastNameStartIndex = builder.length();
       builder.append(nextName);
 
       final char firstLetter = Character.toUpperCase(builder
           .charAt(lastNameStartIndex));
       builder.setCharAt(lastNameStartIndex, firstLetter);
     }
 
     return builder.toString();
   }
 
   private static SnmpTree createSnmpTree(
       final int[] prefix,
       final Object obj,
       MibConstructor mibConstructor)
       throws IllegalAccessException,
       InvocationTargetException,
       IOException
   {
    final SnmpTreeConstructor stc = new SnmpTreeConstructor(prefix, null);
     stc.descend(obj, obj.getClass(), null);
 
     return stc.finish();
   }
 
   private static Method getSetterMethod(final Field field, final Class<?> klass)
   {
     try
     {
       if (field.getAnnotation(SnmpNotSettable.class) != null)
         return null;
 
       final String setterName = SnmpUtils.getSetterName(field.getName());
       Class<?> currentClass = klass;
       while (currentClass != Object.class)
       {
         try
         {
           return currentClass.getDeclaredMethod(setterName, field.getType());
         }
         catch (NoSuchMethodException e)
         {
           currentClass = currentClass.getSuperclass();
         }
       }
     }
     catch (SecurityException e)
     {
       throw new RuntimeException(
           "Exception while checking if field is writable: ", e);
     }
 
     return null;
   }
 
   /** Sets up initial handler classes. */
   {
     // collections types
     handlers.put(Map.class, new MapHandler());
     handlers.put(List.class, new CollectionHandler());
     handlers.put(Collection.class, new CollectionHandler());
 
     handlers.put(ImmutableMap.class, new MapHandler());
     handlers.put(ImmutableList.class, new CollectionHandler());
     handlers.put(ImmutableCollection.class, new CollectionHandler());
 
     // general object
     handlers.put(Object.class, defaultHandler);
   }
 
   private SnmpTreeConstructor(
       final int[] prefix,
       final MibConstructor mibConsructor)
   {
     this.prefix = prefix;
     this.oidStack.push(1);
     this.tableOidIndexStack.push(1);
     this.snmpTreeSkeleton = new SnmpTreeSkeleton(prefix);
     this.mibConstructor = mibConsructor;
     this.createMib = mibConsructor != null;
   }
 
   public void descend(
       final Object obj,
       final Class<?> baseClass,
       final Field field)
 
       throws IllegalArgumentException,
       IllegalAccessException,
       InvocationTargetException
   {
     if (obj == null)
     {
       nullHandler.handle(this, obj, field);
     }
     else if (obj.getClass().isAnonymousClass() &&
         objectHandleStack.contains(obj))
     {
      // prevent reference to outer class from causing a loop
     }
    else if (getFieldInfo(field).tableType)
     {
       getHandler(obj.getClass()).handle(this, obj, field);
     }
     else
     {
       checkForCircularReference(obj);
       onEnter(obj, field);
 
       final Collection<Field> objectFields = getClassInfo(baseClass).fields;
       for (final Field objectField : objectFields)
         descendIntoField(obj, objectField);
 
       onExit(obj, field);
     }
   }
 
   public void onCollectionEnter(
       final Object obj,
       final Field field,
       final Class<?> keyType)
   {
     final FieldInfo info = getFieldInfo(field);
 
     nameStack.push(info.snmpName);
     tableOidIndexStack.push(oidStack.size());
 
     preModifiyOidForTable(info);
     tableNameStack.push(info.snmpName);
 
     if (createMib && info.tableType)
       addToMib(info, keyType, null);
 
     oidStack.push(1);
     oidExtStack.push(0);
     oidExtLenStack.push(1);
   }
 
   public void onCollectionExit(final Object obj, final Field field)
   {
     final FieldInfo info = getFieldInfo(field);
 
     int count = oidExtLenStack.pop();
     oidExtStack.remove(count);
     oidStack.pop();
 
     tableNameStack.pop();
 
     postModifiyOidForTable(info);
 
     tableOidIndexStack.pop();
     nameStack.pop();
   }
 
   public void onNextCollectionValue(
       final Object obj,
       final Object collectionIndex)
   {
     // remove extension by removing all ints for that extension
     // the oidExtLenStack lets us know how many entries to remove
     oidExtStack.remove(oidExtLenStack.pop());
 
     // if there is no specified snmp index for the given type, use the
     // collection index passed in to find out the type
     Object extensionObject = getSnmpIndex(obj);
     if (extensionObject == null)
       extensionObject = collectionIndex;
 
     // add the extension (dynamic oid part) to the extension stack
     final int[] extension = SnmpUtils.getSnmpExtension(extensionObject);
     oidExtLenStack.push(extension.length);
     oidExtStack.copyFrom(extension, 0, extension.length);
   }
 
   protected void descendIntoField(final Object obj, final Field field)
       throws IllegalArgumentException,
       IllegalAccessException,
       InvocationTargetException
   {
     if (!getFieldInfo(field).shouldSkip)
       return;
 
     field.setAccessible(true);
 
     onNextField(obj, field);
 
     final Object nextObject = field.get(obj);
     if (nextObject == null)
       nullHandler.handle(this, obj, field);
     else if (!getFieldInfo(field).simple)
       getHandler(field.getType()).handle(this, nextObject, field);
   }
 
   /**
    * Creates a String representation of the current object traversal stack.
    * 
    * @return A String containing the object stack, not including the current
    *         object.
    */
   protected String getObjectHandleStack()
   {
     final StringBuilder stackBuilder = new StringBuilder();
     for (final Object stackObject : objectHandleStack.values())
     {
       stackBuilder.append("\n    ");
       stackBuilder.append(ObjUtils.getRefInfo(stackObject));
     }
 
     return stackBuilder.toString();
   }
 
   protected Object getSnmpIndex(final Object obj)
   {
 
     try
     {
       final ClassInfo info = getClassInfo(obj.getClass());
       final Field extensionField = info.extensionField;
       if (info.extensionField != null)
       {
         extensionField.setAccessible(true);
         return extensionField.get(obj);
       }
     }
     catch (final IllegalArgumentException e)
     {
       // TODO Auto-generated catch block
       e.printStackTrace();
     }
     catch (final IllegalAccessException e)
     {
       // TODO Auto-generated catch block
       e.printStackTrace();
     }
 
     return null;
   }
 
   protected void onEnter(final Object obj, final Field field)
   {
     if (field != null && !getFieldInfo(field).tableType)
       nameStack.push(field.getName());
 
     objectHandleStack.push(obj);
     oidStack.push(0);
   }
 
   protected void onExit(final Object obj, final Field field)
   {
     oidStack.pop();
     objectHandleStack.pop();
 
     if (field != null && !getFieldInfo(field).tableType)
       nameStack.pop();
   }
 
   protected void onNextField(final Object obj, final Field field)
   {
     final FieldInfo info = getFieldInfo(field);
     nameStack.push(info.snmpName);
 
     // increment current oid by one if not a table
     // a table won't take up an OID in the current subtree
     if (!info.tableType)
       oidStack.push(oidStack.pop() + 1);
 
     if (createMib && !info.tableType)
       addToMib(info, null, obj.getClass());
 
     if (info.simple)
       addToTree(obj, info);
 
     nameStack.pop();
   }
 
   protected void postModifiyOidForTable(final FieldInfo info)
   {
     final String parent = tableNameStack.peek();
     final String name = parent + "." + nameStack.peek();
     oidStack.clear();
     oidStack.copyFrom(savedOidStackMap.get(name));
   }
 
   protected void preModifiyOidForTable(final FieldInfo info)
   {
     final String parent = tableNameStack.peek();
     final String name = parent + "." + nameStack.peek();
 
     // TODO - this shouldn't be based off name, but rather OID?
     Integer oid = subTableOidModMap.get(name);
     if (oid == null)
     {
       oid = subTableOidModMap.get(parent);
       if (oid == null)
         oid = oidStack.get(1);
 
       oid++;
 
       subTableOidModMap.put(parent, oid);
       subTableOidModMap.put(name, oid);
       savedOidStackMap.put(name, new IntStack(oidStack));
     }
 
     final int index = tableOidIndexStack.get(tableOidIndexStack.size() - 2);
     final int count = oidStack.size() - index + 1;
 
     oidStack.remove(count);
     oidStack.push(oid);
   }
 
   private void addToMib(
       final FieldInfo info,
       final Class<?> keyType,
       final Class<?> enclosingClass)
   {
     if (!info.oidVisitedMap.contains(oidStack))
     {
       final int oid = oidStack.peek();
       // check greater than 1 because the current table is in the name
       // stack
       final boolean inTable = (tableNameStack.size() > 1);
 
       final String name = buildStringPath(nameStack, true);
       final String parent = buildStringPath(nameStack, false);
 
       if (info.tableType)
         mibConstructor.addTable(parent, name, oid, inTable, "", keyType);
       else if (info.simple)
         mibConstructor.addItem(parent, name, oid, info.field.getType(),
             info.description, info.isWritable());
       else
         mibConstructor.addEntry(parent, name, oid, "");
 
       info.oidVisitedMap.add(new IntStack(oidStack));
     }
   }
 
   private void addToTree(final Object obj, final FieldInfo info)
   {
     final OID oid = new JotsOID(prefix, oidStack, oidExtStack);
     snmpTreeSkeleton.add(oid, info.field, obj, info.setter);
   }
 
   /**
    * Checks to see if descending into the specified object will cause a circular
    * loop to occur.
    * 
    * @param obj
    *          The object to check for.
    * @throws CircularReferenceException
    *           if a circular reference is encountered.
    */
   private void checkForCircularReference(final Object obj)
   {
     if (objectHandleStack.contains(obj))
      throw new CircularReferenceException("Cannot currently handle circular references");
   }
 
   private SnmpTree finish()
       throws IOException
   {
     if (createMib)
       mibConstructor.finish();
 
     return snmpTreeSkeleton.finishTreeConstruction();
   }
 
   private ClassInfo getClassInfo(final Class<?> klass)
   {
     return classInfoCache.getUnchecked(klass);
   }
 
   private FieldInfo getFieldInfo(final Field field)
   {
     return fieldInfoCache.getUnchecked(field);
   }
 
   private ObjectHandler getHandler(final Class<?> klass)
   {
     return objectHandlerCache.getUnchecked(klass);
   }
 }
