 package de.minestar.maventest.commandsystem;
 
 import java.lang.reflect.Method;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 public class ArgumentList {
 
     private final Object[] args;
     private int offset;
 
     /**
      * Create a ArgumentList holding an reference on the object array. You can
      * get all elements in the object
      * 
      * @param args
      *            The arguments from the command
      */
     public ArgumentList(Object[] args) {
        this.args = args;
         this.offset = 0;
     }
 
     /**
      * Create a ArgumentList holding an reference on the object array. You can
      * get all elements in the object
      * 
      * @param args
      *            The arguments from the command
      */
     public ArgumentList(ArgumentList argumentList, int offset) {
         this.args = argumentList.args;
         this.offset = argumentList.offset + offset;
     }
 
     /**
      * The length of the argument list. It is the length of the array minus the
      * offset
      * 
      * @return Count of arguments
      */
     public int length() {
         return args.length - offset;
     }
 
     /**
      * 
      * @return <code>True</code> when <code>length() <= 0</code>
      */
     public boolean isEmpty() {
         return length() <= 0;
     }
 
     /**
      * Convert the argument on the position 'index' to a Boolean. When the
      * argument is "0" or "false" it will return Boolean.FALSE. When the
      * argument is "1" or "true" it will return Boolea.TRUE. Otherwise, return
      * <code>null</code>
      * 
      * @param index
      *            The index of the parameter in the list
      * @return <code>Null</code>, when the value is no Boolean or the Boolean
      *         value itself
      */
     public Boolean getBoolean(int index) {
         String arg = getString(index);
         if (arg.equals("0") || arg.equalsIgnoreCase("false"))
             return Boolean.FALSE;
         else if (arg.equals("1") || arg.equalsIgnoreCase("true"))
             return Boolean.TRUE;
         else
             return null;
     }
 
     /**
      * Convert the argument on the position 'index' to a Boolean. When the
      * argument is "0" or "false" it will return Boolean.FALSE. When the
      * argument is "1" or "true" it will return Boolea.TRUE. Otherwise, return
      * <code>defaultValue</code>
      * 
      * @param index
      *            The index of the parameter in the list
      * @param defaultValue
      *            The default value returned when the argument is no boolean
      * @return The <code>defaultValue</code>, when the value is no Boolean or
      *         return the Boolean value itself
      */
     public Boolean getBoolean(int index, Boolean defaultValue) {
         Boolean result = getBoolean(index);
         return result != null ? result : defaultValue;
     }
 
     /**
      * Convert the argument on the position 'index' to a Byte using
      * {@link Byte#parseByte(String)}. When this method throws a
      * {@link NumberFormatException} it will return null instead.
      * 
      * @param index
      *            The index of the parameter in the list
      * @return <code>Null</code>, when the argument is no Byte. Otherwise return
      *         the value
      */
     public Byte getByte(int index) {
         try {
             return Byte.parseByte(getString(index));
         } catch (NumberFormatException e) {
             return null;
         }
     }
 
     /**
      * Convert the argument on the position 'index' to a Byte using
      * {@link Byte#parseByte(String)}. When this method throws a
      * {@link NumberFormatException} it will return the
      * <code>defaultValue</code> instead.
      * 
      * @param index
      *            The index of the parameter in the list
      * @param defaultValue
      *            The default value returned when the argument is no Byte
      * @return <code>defaultValue</code>, when the argument is no Byte.
      *         Otherwise return the value
      */
     public Byte getByte(int index, Byte defaultValue) {
         Byte result = getByte(index);
         return result != null ? result : defaultValue;
     }
 
     /**
      * Convert the argument on the position 'index' to a Short using
      * {@link Short#parseShort(String)}. When this method throws a
      * {@link NumberFormatException} it will return null instead.
      * 
      * @param index
      *            The index of the parameter in the list
      * @return <code>Null</code>, when the argument is no Short. Otherwise
      *         return the value
      */
     public Short getShort(int index) {
         try {
             return Short.parseShort(getString(index));
         } catch (NumberFormatException e) {
             return null;
         }
     }
 
     /**
      * Convert the argument on the position 'index' to a Short using
      * {@link Short#parseShort(String)}. When this method throws a
      * {@link NumberFormatException} it will return the
      * <code>defaultValue</code> instead.
      * 
      * @param index
      *            The index of the parameter in the list
      * @param defaultValue
      *            The default value returned when the argument is no Short
      * @return <code>defaultValue</code>, when the argument is no Short.
      *         Otherwise return the value
      */
     public Short getShort(int index, Short defaultValue) {
         Short result = getShort(index);
         return result != null ? result : defaultValue;
     }
 
     /**
      * Convert the argument on the position 'index' to an Integer using
      * {@link Integer#parseInteger(String)}. When this method throws a
      * {@link NumberFormatException} it will return null instead.
      * 
      * @param index
      *            The index of the parameter in the list
      * @return <code>Null</code>, when the argument is no Integer. Otherwise
      *         return the value
      */
     public Integer getInt(int index) {
         try {
             return Integer.parseInt(getString(index));
         } catch (NumberFormatException e) {
             return null;
         }
     }
 
     /**
      * Convert the argument on the position 'index' to an Integer using
      * {@link Integer#parseInteger(String)}. When this method throws a
      * {@link NumberFormatException} it will return the
      * <code>defaultValue</code> instead.
      * 
      * @param index
      *            The index of the parameter in the list
      * @param defaultValue
      *            The default value returned when the argument is no Integer
      * @return <code>defaultValue</code>, when the argument is no Integer.
      *         Otherwise return the value
      */
     public Integer getInt(int index, Integer defaultValue) {
         Integer result = getInt(index);
         return result != null ? result : defaultValue;
     }
 
     /**
      * Convert the argument on the position 'index' to a Long using
      * {@link Long#parseLong(String)}. When this method throws a
      * {@link NumberFormatException} it will return null instead.
      * 
      * @param index
      *            The index of the parameter in the list
      * @return <code>Null</code>, when the argument is no Long. Otherwise return
      *         the value
      */
     public Long getLong(int index) {
         try {
             return Long.parseLong(getString(index));
         } catch (NumberFormatException e) {
             return null;
         }
     }
 
     /**
      * Convert the argument on the position 'index' to a Long using
      * {@link Long#parseLong(String)}. When this method throws a
      * {@link NumberFormatException} it will return the
      * <code>defaultValue</code> instead.
      * 
      * @param index
      *            The index of the parameter in the list
      * @param defaultValue
      *            The default value returned when the argument is no Long
      * @return <code>defaultValue</code>, when the argument is no Long.
      *         Otherwise return the value
      */
     public Long getLong(int index, Long defaultValue) {
         Long result = getLong(index);
         return result != null ? result : defaultValue;
     }
 
     /**
      * Get the first char of the argument at position 'index'.
      * 
      * @param index
      *            The index of the parameter in the list
      * @return The character at the position index.
      */
     public Character getChar(int index) {
         return getString(index).charAt(0);
     }
 
     /**
      * Get the argument at position 'index' as a String
      * 
      * @param index
      *            The index of the parameter in the list
      * @return
      */
     public String getString(int index) {
         return args[index + offset].toString();
     }
 
     // START GENERIC SHIT
 
     // MAP HOLDS A REFERENCE OF ALL GETTER METHODS WITHOUT DEFAULT VALUES
     private final static Map<Class<?>, Method> getMethodMap = new HashMap<Class<?>, Method>();
 
     // MAP HOLDS A REFERENCE OF ALL GETTER METHODS WITH DEFAULT VALUES
     private final static Map<Class<?>, Method> getDefaultMethodMap = new HashMap<Class<?>, Method>();
 
     // FILL MAPS AT FIRST CALL
     static {
         // GET ALL DECLARED METHODS OF THE PARAMETER LIST
         Method[] methods = ArgumentList.class.getDeclaredMethods();
         for (Method method : methods) {
             // ONLY GET THE GETTER
             if (method.getName().startsWith("get")) {
                 // GET THE PARAMATER CLASSES - INTERESTING GETTER HAS ONLY
                 // "int index" OR "int index, T defaultValue"
                 Class<?>[] parameterClasses = method.getParameterTypes();
                 // GETTER WITHOUT DEFAULT VALUE
                 if (parameterClasses.length == 1 && parameterClasses[0].equals(int.class))
                     getMethodMap.put(method.getReturnType(), method);
                 // GETTER WITHOUT DEFAULT VALUE
                 else if (parameterClasses.length == 2 && parameterClasses[0].equals(int.class) && parameterClasses[1].equals(method.getReturnType()))
                     getDefaultMethodMap.put(method.getReturnType(), method);
             }
         }
     }
 
     /**
      * Try to convert the argument at position 'index' to an instance of
      * <code>T</code>. This uses the defined getter methods in
      * {@link ArgumentList}. When there is no getter method for a Class of T, it
      * will return the <code>defaultValue</code>
      * 
      * @param clazz
      *            The class of T to convert to
      * @param defaultValue
      *            The default value returned when the argument cannot be
      *            converted to an instance of T OR the class of T is not
      *            supported
      * @return <code>Null</code>, when T is not supported(no getter function) OR
      *         the getter function cannot convert the argument. Otherwise a
      *         converted instance of T
      */
     @SuppressWarnings("unchecked")
     public <T> T get(Class<T> clazz, int index) {
         try {
             Method method = getMethodMap.get(clazz);
             if (method != null)
                 return (T) method.invoke(this, index);
         } catch (Exception e) {
         }
 
         return null;
     }
 
     /**
      * Try to convert the argument at position 'index' to an instance of
      * <code>T</code>. This uses the defined getter methods in
      * {@link ArgumentList}. When there is no getter method for a Class of T, it
      * will return null
      * 
      * @param clazz
      *            The class of T to convert to
      * @param defaultValue
      *            The index of the parameter in the list
      * @param index
      *            The index of the parameter in the list
      * @return <code>defaultValue</code>, when T is not supported(no getter
      *         function) OR the getter function cannot convert the argument.
      *         Otherwise a converted instance of T
      */
     @SuppressWarnings("unchecked")
     public <T> T get(Class<T> clazz, int index, T defaultValue) {
         try {
             Method method = getDefaultMethodMap.get(clazz);
             if (method != null)
                 return (T) method.invoke(this, index, defaultValue);
         } catch (Exception e) {
         }
 
         return defaultValue;
     }
 
     /**
      * Returns an iterator to iterator through all values which are
      * automatically converted to an instance of T. The iterator start at the
      * position index. <br>
      * Example usage: <br>
      * 
      * <pre>
      *      ArgumentList aList = new ArgumentList(new Object[]{1, 2, 3, 4, 5});
      *      for (Integer s : aList.getIterator(Integer.class, 0)) {
      *          System.out.print(s + " ");
      *      }
      * 
      *      Result: 1 2 3 4 5
      * </pre>
      * 
      * This will use the {@link ArgumentList#get(Class, int)} method to get the
      * value at the position
      * 
      * @param clazz
      *            The class of T to convert to
      * @param startIndex
      *            The position the iterator will start
      * @return An Iterator returning values of the class T
      */
     public <T> Iterable<T> getIterator(Class<T> clazz, int startIndex) {
         return new ArgumentIterator<T>(clazz, startIndex);
     }
 
     private class ArgumentIterator<T> implements Iterator<T>, Iterable<T> {
 
         private int index;
         private Class<T> clazz;
 
         public ArgumentIterator(Class<T> clazz, int index) {
             this.index = index;
             this.clazz = clazz;
         }
 
         @Override
         public boolean hasNext() {
             return index < length();
         }
 
         @Override
         public T next() {
             return get(clazz, index++);
         }
 
         @Override
         public void remove() {
             throw new UnsupportedOperationException();
         }
 
         @Override
         public Iterator<T> iterator() {
             return this;
         }
     }
 }
