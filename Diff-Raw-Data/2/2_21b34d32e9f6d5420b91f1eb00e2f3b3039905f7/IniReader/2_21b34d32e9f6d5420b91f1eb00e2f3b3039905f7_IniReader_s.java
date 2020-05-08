 package jkit.io.ini;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Scanner;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import jkit.io.convert.ArrayConverter;
 import jkit.io.convert.ClassConverter;
 import jkit.io.convert.Converter;
 
 /**
  * Reads out an INI file and presents its information as a simple name value
  * pair.
  * 
  * @author Joschi <josua.krause@googlemail.com>
  * 
  */
 public final class IniReader {
 
     /**
      * The name of the utf8 charset.
      */
     public static final String UTF8_STR = "UTF-8";
 
     /**
      * The string that is interpreted by any
      * {@link #getObject(String, String, Converter)} or
      * {@link #getInstance(String, String, Class)} method to return a
      * <code>null</code> pointer.
      */
     public static final String NULL = "null";
 
     /**
      * The name of an entry.
      * 
      * @author Joschi <josua.krause@googlemail.com>
      * 
      */
     private static class Entry {
         /** The area name. */
         final String area;
 
         /** The actual name. */
         final String name;
 
         /**
          * Creates a new Entry.
          * 
          * @param area
          *            The area name.
          * @param name
          *            The actual name.
          */
         Entry(final String area, final String name) {
             this.name = name;
             this.area = area;
         }
 
         @Override
         public boolean equals(final Object obj) {
             if (this == obj) {
                 return true;
             }
             if (obj == null) {
                 return false;
             }
             if (!(obj instanceof Entry)) {
                 return false;
             }
             final Entry e = (Entry) obj;
             return e.name.equals(name) && e.area.equals(area);
         }
 
         @Override
         public int hashCode() {
             return area.hashCode() * 31 + name.hashCode();
         }
 
     }
 
     /**
      * Creates an INI reader from a specific file.
      * 
      * @param f
      *            The file to interpret as INI.
      * @return The newly created {@link IniReader}.
      * @throws FileNotFoundException
      *             If this file was not found.
      */
     public static IniReader createIniReader(final File f)
             throws FileNotFoundException {
         final IniReader r = new IniReader(new Scanner(f));
         r.setFile(f);
         return r;
     }
 
     /**
      * Guarantees that an IniReader for a given file is created even when the
      * file does not exist.
      * 
      * @param f
      *            The file.
      * @param autoLearn
      *            Whether the IniReader should learn from default values. This
      *            option can be hazardous regarding fields that are interpreted
      *            in multiple ways.
      * 
      * @return The reader for the file.
      * 
      * @see #setAutoLearn(boolean)
      * @see #createIniReader()
      * @see #createIniReader(File)
      */
     public static IniReader createFailProofIniReader(final File f,
             final boolean autoLearn) {
         IniReader r;
         try {
             r = createIniReader(f);
         } catch (final FileNotFoundException e) {
             r = createIniReader();
             r.setFile(f);
         }
         r.setAutoLearn(autoLearn);
         return r;
     }
 
     /**
      * Creates an empty INI reader.
      * 
      * @return The newly created {@link IniReader}.
      */
     public static IniReader createIniReader() {
         return new IniReader();
     }
 
     /**
      * Writes the content of this IniReader to the associated file.
      * 
      * @throws IllegalStateException
      *             When there is no associated file.
      * @throws IOException
      *             When there is a problem with the associated file.
      * 
      * @see #writeIni(PrintWriter)
      */
     public void writeIni() throws IOException {
         final File f = file;
         if (f == null) {
             throw new IllegalStateException("no associated file");
         }
         final PrintWriter pw = new PrintWriter(f, UTF8_STR);
         writeIni(pw);
         pw.close();
     }
 
     /**
      * Writes the information represented from the IniReader to the given
      * {@link PrintWriter}. Note that any comments present in the original INI
      * file are lost.
      * 
      * @param pw
      *            The output writer.
      */
     public void writeIni(final PrintWriter pw) {
         parse();
         final Comparator<Entry> comp = new Comparator<Entry>() {
 
             @Override
             public int compare(final Entry o1, final Entry o2) {
                 final int cmp = o1.area.compareTo(o2.area);
                 if (cmp != 0) {
                     return cmp;
                 }
                 return o1.name.compareTo(o2.name);
             }
 
         };
         // gather areas
         final Map<Entry, String> map = entries;
         final Map<String, SortedSet<Entry>> areas = new HashMap<String, SortedSet<Entry>>();
         for (final Entry e : map.keySet()) {
             final String areaString = e.area;
             if (!areas.containsKey(areaString)) {
                 areas.put(areaString, new TreeSet<Entry>(comp));
             }
             areas.get(areaString).add(e);
         }
         // write output
         final Object[] objAreas = areas.keySet().toArray();
         Arrays.sort(objAreas);
         for (final Object areaObj : objAreas) {
             pw.print("[");
             // relying on the toString() method of String
             pw.print(areaObj);
             pw.println("]");
             for (final Entry e : areas.get(areaObj)) {
                 pw.print(e.name);
                 pw.print('=');
                 String line = map.get(e);
                 if (line.contains("#")) {
                     line = line.replace("#", "##");
                 }
                 pw.println(line);
             }
             pw.println();
         }
     }
 
     /**
      * Sets a field in the INI file.
      * 
      * Note that all Strings will be trimmed.
      * 
      * @param area
      *            The area name. The name in [box brackets].
      * @param name
      *            The actual name of the value.
      * @param value
      *            The value.
      * @throws IllegalArgumentException
      *             When the area or the name is incorrect.
      */
     public void set(final String area, final String name, final String value) {
         if (area.contains("]")) {
             throw new IllegalArgumentException("invalud area name: " + area);
         }
         if (name.contains("=")) {
             throw new IllegalArgumentException("invalid name: " + name);
         }
         final Entry entry = new Entry(area.trim(), name.trim());
         final String v = value.trim();
         if (entries.containsKey(entry)) {
             final String old = entries.get(entry);
             if (v.equals(old)) {
                 return;
             }
         }
         entries.put(entry, v);
         hasChanged = true;
     }
 
     /**
      * Sets a field in the INI file.
      * 
      * Note that all Strings will be trimmed.
      * 
      * @param area
      *            The area name. The name in [box brackets].
      * @param name
      *            The actual name of the value.
      * @param value
      *            The integer value.
      * @throws IllegalArgumentException
      *             When the area or the name is incorrect.
      */
     public void setInteger(final String area, final String name, final int value) {
         set(area, name, "" + value);
     }
 
     /**
      * Sets a field in the INI file.
      * 
      * Note that all Strings will be trimmed.
      * 
      * @param area
      *            The area name. The name in [box brackets].
      * @param name
      *            The actual name of the value.
      * @param value
      *            The long value.
      * @throws IllegalArgumentException
      *             When the area or the name is incorrect.
      */
     public void setLong(final String area, final String name, final long value) {
         set(area, name, "" + value);
     }
 
     /**
      * Sets a field in the INI file.
      * 
      * Note that all Strings will be trimmed.
      * 
      * @param area
      *            The area name. The name in [box brackets].
      * @param name
      *            The actual name of the value.
      * @param value
      *            The boolean value.
      * @throws IllegalArgumentException
      *             When the area or the name is incorrect.
      */
     public void setBoolean(final String area, final String name,
             final boolean value) {
         set(area, name, "" + value);
     }
 
     /**
      * Sets a field in the INI file.
      * 
      * Note that all Strings will be trimmed.
      * 
      * @param area
      *            The area name. The name in [box brackets].
      * @param name
      *            The actual name of the value.
      * @param value
      *            The float value.
      * @throws IllegalArgumentException
      *             When the area or the name is incorrect.
      */
     public void setFloat(final String area, final String name, final float value) {
         set(area, name, "" + value);
     }
 
     /**
      * Sets a field in the INI file.
      * 
      * Note that all Strings will be trimmed.
      * 
      * @param area
      *            The area name. The name in [box brackets].
      * @param name
      *            The actual name of the value.
      * @param value
      *            The double value.
      * @throws IllegalArgumentException
      *             When the area or the name is incorrect.
      */
     public void setDouble(final String area, final String name,
             final double value) {
         set(area, name, "" + value);
     }
 
     /**
      * Sets a field in the INI file.
      * 
      * Note that all Strings will be trimmed.
      * 
      * @param area
      *            The area name. The name in [box brackets].
      * @param name
      *            The actual name of the value.
      * @param obj
      *            The object.
      * @throws IllegalArgumentException
      *             When the area or the name is incorrect.
      */
     public void setObject(final String area, final String name, final Object obj) {
         set(area, name, obj.toString());
     }
 
     /**
      * Sets a field in the INI file.
      * 
      * Note that all Strings will be trimmed.
      * 
      * @param area
      *            The area name. The name in [box brackets].
      * @param name
      *            The actual name of the value.
      * @param arr
      *            The array content -- note that all elements get trimmed.
      * @param delimiter
      *            The delimiter to use.
      * @throws IllegalArgumentException
      *             When the area or the name is incorrect or when the array
      *             contained the delimiter.
      */
     public void setArray(final String area, final String name,
             final Object[] arr, final String delimiter) {
         final StringBuilder sb = new StringBuilder();
         boolean first = true;
         for (final Object obj : arr) {
             if (first) {
                 first = false;
             } else {
                 sb.append(delimiter);
             }
             final String str = obj.toString().trim();
             if (str.contains(delimiter)) {
                 throw new IllegalArgumentException("\"" + str
                         + "\" contains the delimiter \"" + delimiter + "\"");
             }
             sb.append(str);
         }
         set(area, name, sb.toString());
     }
 
     /**
      * Sets a field in the INI file.
      * 
      * Note that all Strings will be trimmed.
      * 
      * @param area
      *            The area name. The name in [box brackets].
      * @param name
      *            The actual name of the value.
      * @param arr
      *            The array content -- note that all elements get trimmed. The
      *            default delimiter is used.
      * @throws IllegalArgumentException
      *             When the area or the name is incorrect or when the array
      *             contained the default delimiter.
      * 
      * @see #DEFAULT_DELIMITER
      */
     public void setArray(final String area, final String name,
             final Object[] arr) {
         setArray(area, name, arr, DEFAULT_DELIMITER);
     }
 
     /**
      * Returns the associated value to the given name.
      * 
      * @param area
      *            The area name. The name in [box brackets].
      * @param name
      *            The actual name of the value.
      * @return The value or an empty String if it was not set.
      */
     public String get(final String area, final String name) {
         return get(area, name, "");
     }
 
     /**
      * Returns the associated value to the given name or the given default value
      * if the requested value does not exist.
      * 
      * @param area
      *            The area name. The name in [box brackets].
      * @param name
      *            The actual name of the value.
      * @param defaultValue
      *            The default value.
      * @return The value or the default value if it was not set.
      */
     public String get(final String area, final String name,
             final String defaultValue) {
         parse();
         final String res = entries.get(new Entry(area, name));
         if (autoLearn && res == null) {
             set(area, name, defaultValue);
         }
         return res == null ? defaultValue : res;
     }
 
     /**
      * Checks if the field has an associated value.
      * 
      * @param area
      *            The area name. The name in [box brackets].
      * @param name
      *            The actual name of the value.
      * @return If the given name has an associated value.
      */
     public boolean has(final String area, final String name) {
         parse();
         return entries.containsKey(new Entry(area, name));
     }
 
     /**
      * Checks if the field has a numerical value.
      * 
      * @param area
      *            The area name. The name in [box brackets].
      * @param name
      *            The actual name of the value.
      * @return If the given name has a numerical value.
      */
     public boolean hasInteger(final String area, final String name) {
         if (!has(area, name)) {
             return false;
         }
         final Integer i = getInteger0(area, name);
         return i != null;
     }
 
     /**
      * The integer wormhole.
      * 
      * @param area
      *            The area name.
      * @param name
      *            The actual name of the value.
      * @return The integer or <code>null</code> if the field has no integer.
      */
     private Integer getInteger0(final String area, final String name) {
         final String res = get(area, name);
         Integer i = null;
         try {
             i = Integer.parseInt(res);
         } catch (final NumberFormatException e) {
             // ignore malformed numbers
         }
         return i;
     }
 
     /**
      * Gets the numerical interpretation of the field.
      * 
      * @param area
      *            The area name. The name in [box brackets].
      * @param name
      *            The actual name of the value.
      * @return The value of the given name interpreted as number or 0 if it
      *         could not be interpreted as Integer.
      */
     public int getInteger(final String area, final String name) {
         return getInteger(area, name, 0);
     }
 
     /**
      * Returns the associated value to the given name or the given default value
      * if the requested value does not exist.
      * 
      * @param area
      *            The area name. The name in [box brackets].
      * @param name
      *            The actual name of the value.
      * @param defaultValue
      *            The default value.
      * @return The integer value or the default value if it was not set.
      */
     public int getInteger(final String area, final String name,
             final int defaultValue) {
         final Integer i = getInteger0(area, name);
         if (autoLearn && i == null) {
             setInteger(area, name, defaultValue);
         }
         return i == null ? defaultValue : i;
     }
 
     /**
      * Checks if the field has a numerical value.
      * 
      * @param area
      *            The area name. The name in [box brackets].
      * @param name
      *            The actual name of the value.
      * @return If the given name can be interpreted as a long number.
      */
     public boolean hasLong(final String area, final String name) {
         if (!has(area, name)) {
             return false;
         }
         final Long l = getLong0(area, name);
         return l != null;
     }
 
     /**
      * The long wormhole.
      * 
      * @param area
      *            The area name.
      * @param name
      *            The actual name of the value.
      * @return The long or <code>null</code> if the field has no long.
      */
     private Long getLong0(final String area, final String name) {
         final String res = get(area, name);
         Long l = null;
         try {
             l = Long.parseLong(res);
         } catch (final NumberFormatException e) {
             // ignore malformed numbers
         }
         return l;
     }
 
     /**
      * Gets the numerical interpretation of the field.
      * 
      * @param area
      *            The area name. The name in [box brackets].
      * @param name
      *            The actual name of the value.
      * @return The long interpretation of the value from the given name or
      *         <code>0L</code> if the value could not be interpreted in such a
      *         way.
      */
     public long getLong(final String area, final String name) {
         return getLong(area, name, 0L);
     }
 
     /**
      * Returns the associated value to the given name or the given default value
      * if the requested value does not exist.
      * 
      * @param area
      *            The area name. The name in [box brackets].
      * @param name
      *            The actual name of the value.
      * @param defaultValue
      *            The default value.
      * @return The long value or the default value if it was not set.
      */
     public long getLong(final String area, final String name,
             final long defaultValue) {
         final Long l = getLong0(area, name);
         if (autoLearn && l == null) {
             setLong(area, name, defaultValue);
         }
         return l == null ? defaultValue : l;
     }
 
     /**
      * Checks if the field has a float value.
      * 
      * @param area
      *            The area name. The name in [box brackets].
      * @param name
      *            The actual name of the value.
      * @return If the given name has a small floating decimal value.
      */
     public boolean hasFloat(final String area, final String name) {
         if (!has(area, name)) {
             return false;
         }
         final Float f = getFloat0(area, name);
         return f != null;
     }
 
     /**
      * The float wormhole.
      * 
      * @param area
      *            The area name.
      * @param name
      *            The actual name of the value.
      * @return The float value or <code>null</code> if the field can not be
      *         interpreted as float.
      */
     private Float getFloat0(final String area, final String name) {
         final String res = get(area, name);
         Float f = null;
         try {
             f = Float.parseFloat(res);
         } catch (final NumberFormatException e) {
             // ignore malformed numbers
         }
         return f;
     }
 
     /**
      * Gets the float interpretation of the field.
      * 
      * @param area
      *            The area name. The name in [box brackets].
      * @param name
      *            The actual name of the value.
      * @return The value of the given name interpreted as small floating decimal
      *         or {@code 0.0f} if it could not be interpreted as Float.
      */
     public float getFloat(final String area, final String name) {
         return getFloat(area, name, 0f);
     }
 
     /**
      * Returns the associated value to the given name or the given default value
      * if the requested value does not exist.
      * 
      * @param area
      *            The area name. The name in [box brackets].
      * @param name
      *            The actual name of the value.
      * @param defaultValue
      *            The default value.
      * @return The float value or the default value if it was not set.
      */
     public float getFloat(final String area, final String name,
             final float defaultValue) {
         final Float f = getFloat0(area, name);
         if (autoLearn && f == null) {
             setFloat(area, name, defaultValue);
         }
         return f == null ? defaultValue : f;
     }
 
     /**
      * Checks if the field can be interpreted as double.
      * 
      * @param area
      *            The area name. The name in [box brackets].
      * @param name
      *            The actual name of the value.
      * @return If the given name has a floating decimal value.
      */
     public boolean hasDouble(final String area, final String name) {
         if (!has(area, name)) {
             return false;
         }
         final Double d = getDouble0(area, name);
         return d != null;
     }
 
     /**
      * The double wormhole.
      * 
      * @param area
      *            The area name.
      * @param name
      *            The actual name of the value.
      * @return The double value or <code>null</code> if the field can not be
      *         interpreted as double value.
      */
     private Double getDouble0(final String area, final String name) {
         final String res = get(area, name);
         Double d = null;
         try {
             d = Double.parseDouble(res);
         } catch (final NumberFormatException e) {
             // ignore malformed numbers
         }
         return d;
     }
 
     /**
      * Gets the double interpretation of the field.
      * 
      * @param area
      *            The area name. The name in [box brackets].
      * @param name
      *            The actual name of the value.
      * @return The value of the given name interpreted as floating decimal or
      *         {@code 0.0} if it could not be interpreted as Double.
      */
     public double getDouble(final String area, final String name) {
         return getDouble(area, name, 0.0);
     }
 
     /**
      * Returns the associated value to the given name or the given default value
      * if the requested value does not exist.
      * 
      * @param area
      *            The area name. The name in [box brackets].
      * @param name
      *            The actual name of the value.
      * @param defaultValue
      *            The default value.
      * @return The double value or the default value if it was not set.
      */
     public double getDouble(final String area, final String name,
             final double defaultValue) {
         final Double d = getDouble0(area, name);
         if (autoLearn && d == null) {
             setDouble(area, name, defaultValue);
         }
         return d == null ? defaultValue : d;
     }
 
     /**
      * Gets the boolean interpretation of the field.
      * 
      * @param area
      *            The area name. The name in [box brackets].
      * @param name
      *            The actual name of the value.
      * @return Interprets the value of the given name as boolean. There are four
      *         steps. If the value does not exist return <code>false</code>. If
      *         it exists and it can be interpreted as a number return
      *         <code>true</code> if the number is not 0. If the value can not be
      *         interpreted as number test whether it is "true" or "false" and
      *         return this as result. If none of the preceding steps come to an
      *         result return <code>true</code> if the String is not empty.
      */
     public boolean getBoolean(final String area, final String name) {
         if (hasLong(area, name)) {
             return getLong(area, name) == 0;
         }
         if (hasDouble(area, name)) {
             return getDouble(area, name) == 0.0;
         }
         final String res = get(area, name);
         if (res.equals("false")) {
             return false;
         }
         if (autoLearn && res.isEmpty()) {
             setBoolean(area, name, false);
         }
         return !res.isEmpty();
     }
 
     /**
      * Instantiates the given class.
      * 
      * @param <T>
      *            The type of the class.
      * @param loadedType
      *            The class to instantiate.
      * @return An object of type <code>&lt;T&gt;</code> created by the default
      *         constructor or <code>null</code>. <code>null</code> is returned
      *         when
      *         <ul>
      *         <li>the default constructor does not exist
      *         <li>the default constructor is not visible
      *         <li>the class object itself is <code>null</code>
      *         </ul>
      */
     private static <T> T getInstance0(final Class<T> loadedType) {
         try {
             return loadedType.newInstance();
             // catch null pointer, visibility and no such method exception
         } catch (final Exception e) {
             return null;
         }
     }
 
     /**
      * Interprets the field as class and returns an instantiation of the class.
      * 
      * @param <T>
      *            The type of the object that is returned.
      * @param area
      *            The name of the area.
      * @param name
      *            The name of the actual value.
      * @param superType
      *            The super class of the returned object, i.e. the type
      *            <code>&lt;T&gt;</code>.
      * @return An object of type <code>&lt;T&gt;</code> created by the default
      *         constructor or <code>null</code>. <code>null</code> is returned
      *         when
      *         <ul>
      *         <li>the default constructor does not exist
      *         <li>the default constructor is not visible
      *         <li>the class object itself is <code>null</code>
      *         <li>the object can not be cast to the type <code>&lt;T&gt;</code>
      *         </ul>
      */
     public <T> T getInstance(final String area, final String name,
             final Class<T> superType) {
         return getInstance0(getObject(area, name, new ClassConverter<T>(
                 superType)));
     }
 
     /**
      * Interprets the field as class and returns an instantiation of the class.
      * 
      * @param <T>
      *            The type of the object that is returned.
      * @param area
      *            The name of the area.
      * @param name
      *            The name of the actual value.
      * @param superType
      *            The super class of the returned object, i.e. the type
      *            <code>&lt;T&gt;</code>.
      * @param defaultValue
      *            The default class name that should be loaded if the field can
      *            not be interpreted as class name. Note that the method still
      *            can fail with a <code>null</code> value as stated below.
      * @return An object of type <code>&lt;T&gt;</code> created by the default
      *         constructor or <code>null</code>. <code>null</code> is returned
      *         when
      *         <ul>
      *         <li>the default constructor does not exist
      *         <li>the default constructor is not visible
      *         <li>the class object itself is <code>null</code>
      *         <li>the object can not be cast to the type <code>&lt;T&gt;</code>
      *         </ul>
      */
     public <T> T getInstance(final String area, final String name,
             final Class<T> superType, final String defaultValue) {
         return getInstance0(getObject(area, name, new ClassConverter<T>(
                 superType), defaultValue));
     }
 
     /**
      * Interprets the field as class and returns an instantiation of the class.
      * 
      * @param <T>
      *            The type of the object that is returned.
      * @param area
      *            The name of the area.
      * @param name
      *            The name of the actual value.
      * @param superType
      *            The super class of the returned object, i.e. the type
      *            <code>&lt;T&gt;</code>.
      * @param defaultClass
      *            The default class that should be loaded if the field can not
      *            be interpreted as class name. Note that the method still can
      *            fail with a <code>null</code> value as stated below. As
      *            opposed to {@link #getInstance(String, String, Class, String)}
      *            the default class file is always loaded - even when no
      *            instance is created.
      * @return An object of type <code>&lt;T&gt;</code> created by the default
      *         constructor or <code>null</code>. <code>null</code> is returned
      *         when
      *         <ul>
      *         <li>the default constructor does not exist
      *         <li>the default constructor is not visible
      *         <li>the class object itself is <code>null</code>
      *         <li>the object can not be cast to the type <code>&lt;T&gt;</code>
      *         </ul>
      */
     public <T> T getInstance(final String area, final String name,
             final Class<T> superType, final Class<T> defaultClass) {
         return getInstance0(getObject(area, name, new ClassConverter<T>(
                 superType), defaultClass));
     }
 
     /**
      * A result for a field query. With this thin wrapper a <code>null</code>
      * value of the field can be distinguished with being a valid return value
      * or an error.
      * 
      * @author Joschi <josua.krause@googlemail.com>
      * @param <T>
      *            The type of the result.
      */
     private static final class Result<T> {
 
         /**
          * The object that may be <code>null</code>.
          */
         public final T obj;
 
         /**
          * Whether the value in {@link #obj} is a valid return value.
          */
         public final boolean valid;
 
         /**
          * Defines the result as valid value.
          * 
          * @param obj
          *            The valid value that may be <code>null</code>.
          */
         public Result(final T obj) {
             this.obj = obj;
             this.valid = true;
         }
 
         /**
          * Defines the result invalid.
          */
         public Result() {
             obj = null;
             valid = false;
         }
 
     }
 
     /**
      * Converts the String content of the field in an Object.
      * 
      * @param <T>
      *            The type of the Object.
      * @param area
      *            The area name. The name in [box brackets].
      * @param name
      *            The actual name of the value.
      * @param converter
      *            The converter to convert the String into the Object.
      * @return The object or <code>null</code> if the conversion has failed or
      *         the field was empty.
      * 
      * @see Converter
      */
     public <T> T getObject(final String area, final String name,
             final Converter<T> converter) {
         return getObject0(area, name, converter).obj;
     }
 
     /**
      * Converts the String content of the field in an Object.
      * 
      * @param <T>
      *            The type of the Object.
      * @param area
      *            The area name. The name in [box brackets].
      * @param name
      *            The actual name of the value.
      * @param converter
      *            The converter to convert the String into the Object.
      * @param defaultValue
      *            The String to convert if the original conversion failed or the
      *            field was empty.
      * @return The object.
      * 
      * @see Converter
      */
     public <T> T getObject(final String area, final String name,
             final Converter<T> converter, final String defaultValue) {
         final Result<T> res = getObject0(area, name, converter);
         if (autoLearn && !res.valid) {
             setObject(area, name, defaultValue);
         }
         return res.valid ? res.obj : (NULL.equals(defaultValue) ? null
                 : converter.convert(defaultValue));
     }
 
     /**
      * Converts the String content of the field in an Object.
      * 
      * @param <T>
      *            The type of the Object.
      * @param area
      *            The area name. The name in [box brackets].
      * @param name
      *            The actual name of the value.
      * @param converter
      *            The converter to convert the String into the Object.
      * @param defaultValue
      *            The Object to return if the original conversion failed or the
      *            field was empty.
      * @return The object.
      * 
      * @see Converter
      */
     public <T> T getObject(final String area, final String name,
             final Converter<T> converter, final T defaultValue) {
         final Result<T> res = getObject0(area, name, converter);
         if (autoLearn && !res.valid) {
             setObject(area, name, defaultValue);
         }
         return res.valid ? res.obj : defaultValue;
     }
 
     /**
      * The object worm hole.
      * 
      * @param area
      *            The area name.
      * @param name
      *            The actual name.
      * @param converter
      *            The converter to convert the result.
      * @param <T>
      *            The type of the result.
      * @return A result indicating if the returned value is valid.
      */
     private <T> Result<T> getObject0(final String area, final String name,
             final Converter<T> converter) {
         if (!has(area, name)) {
             return new Result<T>();
         }
         final String str = get(area, name);
         if (NULL.equals(str)) {
             return new Result<T>(null);
         }
         final T res = converter.convert(str);
         return res != null ? new Result<T>(res) : new Result<T>();
     }
 
     /**
      * Tests whether the String at the given field can be converted via the
      * given converter.
      * 
      * @param <T>
      *            The type of the conversion.
      * @param area
      *            The area name. The name in [box brackets].
      * @param name
      *            The actual name of the value.
      * @param converter
      *            The converter to test the String for.
      * @return Whether the String can be converted.
      * 
      * @see Converter
      */
     public <T> boolean hasObject(final String area, final String name,
             final Converter<T> converter) {
         return getObject0(area, name, converter).valid;
     }
 
     /**
      * Returns a field interpreted as an array of arbitrary objects. The
      * original String is split by the delimiter and the results are converted
      * via the given converter.
      * 
      * @param <T>
      *            The component type of the resulting array.
      * @param area
      *            The area name. The name in [box brackets].
      * @param name
      *            The actual name of the value.
      * @param converter
      *            The converter for the array.
      * @return The resulting array.
      * 
      * @see ArrayConverter
      */
     public <T> T[] getArray(final String area, final String name,
             final ArrayConverter<T> converter) {
         if (!has(area, name)) {
             return malformedArray(area, name, converter);
         }
         final String[] strings = getArray(area, name, converter.delimiter());
         int i = strings.length;
         final T[] res = converter.array(i);
         while (--i >= 0) {
             final T cur = converter.convert(strings[i]);
             if (cur == null) {
                 return malformedArray(area, name, converter);
             }
             res[i] = cur;
         }
         return res;
     }
 
     /**
      * This method is called if a field can not be converted into an array. This
      * happens either when the field is empty or when the converter fails to
      * convert an element in the array.
      * 
      * @param <T>
      *            The type of the array.
      * @param area
      *            The name of the area.
      * @param name
      *            The name of the value.
      * @param converter
      *            The converter to convert the array elements.
      * @return The resulting array, i.e. the default value of the converter.
      */
     private <T> T[] malformedArray(final String area, final String name,
             final ArrayConverter<T> converter) {
         final T[] defaultValue = converter.defaultValue();
         if (autoLearn) {
             setArray(area, name, defaultValue, converter.delimiter());
         }
         return defaultValue;
     }
 
     /**
      * Returns a field interpreted as an array. The original String is split by
      * the delimiter and the results are the resulting Strings. The result will
      * not contain the delimiter. Note that the Strings of the result will be
      * trimmed.
      * 
      * @param area
      *            The area name. The name in [box brackets].
      * @param name
      *            The actual name of the value.
      * @param delimiter
      *            The delimiter used to determine fields of the array.
      * @param defaultValue
      *            The default value, if the given field does not exist.
      * @return The resulting array.
      */
     public String[] getArray(final String area, final String name,
             final String delimiter, final String[] defaultValue) {
         if (!has(area, name)) {
             if (autoLearn) {
                 setArray(area, name, defaultValue, delimiter);
             }
             return defaultValue;
         }
         final String content = get(area, name);
         final String[] arr = content.split(delimiter);
         int i = arr.length;
         final String[] res = new String[i];
         while (--i >= 0) {
             res[i] = arr[i].trim();
         }
         return res;
     }
 
     /**
      * The default delimiter for the {@link #getArray(String, String)} method.
      * 
      * @see #getArray(String, String)
      * @see #getArray(String, String, String[])
      */
     public static final String DEFAULT_DELIMITER = ",";
 
     /**
      * The empty String array.
      */
     private static final String[] EMPTY_ARR = new String[0];
 
     /**
      * Returns a field interpreted as an array. The original String is split by
      * the delimiter and the results are the resulting Strings. The result will
      * not contain the delimiter. Note that the Strings of the result will be
      * trimmed.
      * 
      * @param area
      *            The area name. The name in [box brackets].
      * @param name
      *            The actual name of the value.
      * @param delimiter
      *            The delimiter used to determine fields of the array.
      * @return The resulting array or the empty array if the field was not
      *         present.
      */
     public String[] getArray(final String area, final String name,
             final String delimiter) {
         return getArray(area, name, delimiter, EMPTY_ARR);
     }
 
     /**
      * Returns a field interpreted as an array. The original String is split by
      * the default delimiter and the results are the resulting Strings. The
      * result will not contain the default delimiter. Note that the Strings of
      * the result will be trimmed.
      * 
      * @param area
      *            The area name. The name in [box brackets].
      * @param name
      *            The actual name of the value.
      * @param defaultValue
      *            The default value, if the given field does not exist.
      * @return The resulting array.
      * 
      * @see #DEFAULT_DELIMITER
      */
     public String[] getArray(final String area, final String name,
             final String[] defaultValue) {
         return getArray(area, name, DEFAULT_DELIMITER, defaultValue);
     }
 
     /**
      * Returns a field interpreted as an array. The original String is split by
      * the default delimiter and the results are the resulting Strings. The
      * result will not contain the default delimiter. Note that the Strings of
      * the result will be trimmed.
      * 
      * @param area
      *            The area name. The name in [box brackets].
      * @param name
      *            The actual name of the value.
      * @return The resulting array or the empty array if the field was not
      *         present.
      * 
      * @see #DEFAULT_DELIMITER
      */
     public String[] getArray(final String area, final String name) {
         return getArray(area, name, EMPTY_ARR);
     }
 
     /** The map of the entries. */
     private final Map<Entry, String> entries;
 
     /** Whether to automatically learn from default values. */
     private boolean autoLearn;
 
    /* whether the content has changed after loading from the disk */
     private boolean hasChanged;
 
     /** The associated file. */
     private File file;
 
     /** The INI scanner. */
     private Scanner scanner;
 
     /** The current area or null if finished scanning. */
     private String area;
 
     /**
      * Private constructor.
      * 
      * @param s
      *            The underlying scanner.
      */
     private IniReader(final Scanner s) {
         entries = new HashMap<Entry, String>();
         autoLearn = false;
         scanner = s;
         area = "";
         file = null;
         hasChanged = false;
     }
 
     /**
      * Private default constructor. No scanner is defined.
      */
     private IniReader() {
         entries = new HashMap<Entry, String>();
         autoLearn = false;
         scanner = null;
         area = null;
         file = null;
         hasChanged = false;
     }
 
     /**
      * Checks whether the ini has been changed.
      * 
      * @return Whether the ini has been changed.
      */
     public boolean hasChanged() {
         return hasChanged;
     }
 
     /**
      * Setter.
      * 
      * @param autoLearn
      *            Whether the IniReader should learn from default values. This
      *            option can be hazardous regarding fields that are interpreted
      *            in multiple ways.
      *            <p>
      *            <code>
      * IniReader ini = IniReader.createIniReader();<br />
      * ini.set("foo", "bar", "baz");<br />
      * ini.getLong("foo", "bar", 15);<br />
      * ini.get("foo", "bar"); // returns 15 instead of "baz"!<br />
      * </code>
      *            </p>
      */
     public void setAutoLearn(final boolean autoLearn) {
         this.autoLearn = autoLearn;
     }
 
     /**
      * Sets the associated file.
      * 
      * @param file
      *            The file.
      */
     public void setFile(final File file) {
         this.file = file;
     }
 
     /** Parses the input file. */
     private void parse() {
         if (scanner == null) {
             return;
         }
         while (scanner.hasNextLine()) {
             interpret(scanner.nextLine());
         }
         scanner.close();
         scanner = null;
         area = null;
     }
 
     /**
      * Uncomments a line and trims its endings.
      * 
      * @param line
      *            The line to interpret.
      * @return The line without comments or trailing spaces.
      */
     private static String uncommentAndTrim(final String line) {
         int cur = 0;
         while (cur < line.length()) {
             final int pos = line.indexOf('#', cur);
             if (pos < 0) {
                 return line;
             }
             final int doublepos = line.indexOf("##", cur);
             if (pos < doublepos || doublepos < 0) {
                 return line.substring(0, pos);
             }
             cur = doublepos + 2;
         }
         return line;
     }
 
     /**
      * Interprets a line.
      * 
      * @param l
      *            The line to interpret.
      */
     private void interpret(final String l) {
         final String line = uncommentAndTrim(l).replace("##", "#");
         if (line.startsWith("[")) {
             final int end = line.indexOf(']');
             if (end < 0) {
                 throw new IllegalArgumentException("no area definition: "
                         + line);
             }
             area = line.substring(1, end).trim();
             return;
         }
         if (line.isEmpty()) {
             return;
         }
         final String[] eq = line.split("=", 2);
         if (eq.length == 1) {
             return;
         }
         entries.put(new Entry(area, eq[0].trim()), eq[1].trim());
     }
 
 }
