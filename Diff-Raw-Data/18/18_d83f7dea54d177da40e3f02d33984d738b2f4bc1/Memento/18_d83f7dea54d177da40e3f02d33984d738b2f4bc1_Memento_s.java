 package hu.kazocsaba.memento;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Objects;
 
 /**
  * A class storing key-value mappings. Keys are always {@code String} objects; a few simple value data types are
  * supported. {@code null} keys and values are not allowed.
  * <p>
  * A memento can have child mementos; these are stored in creation order, and enable building a hierarchy to represent
  * complex data structures. For example, if a single memento holds the data of a person, child mementos can be used
 * to store a list of persons. For example:
  * <pre>
  *    private void saveInto(Person person, Memento target) {
  *        target.putString("name", person.getName());
  *        target.putInt("age", person.getAge());
  *    }
  *    private Memento save(List&lt;Person> people) {
  *        Memento m = new Memento();
  *        for (Person person: people) {
  *            Memento personMemento = m.createChild("person");
  *            saveInto(person, personMemento);
  *        }
  *        return m;
  *    }
  * </pre>
  * Each memento can have a string type that can be used to differentiate between various kinds of child mementos. The
  * following code restores the list of persons.
  * <pre>
  *    private List&lt;Person> load(Memento m) throws MementoFormatException {
  *        List&lt;Person> people = new ArrayList&lt;>();
  *        // iterate over the child mementos
  *        for (Memento child: m) {
  *            // only process the children with the type "person"
  *            if ("person".equals(child.getType()))
  *                people.add(new Person(child.getString("name"), child.getInt("age")));
  *        }
  *    }
  * </pre>
  * This class allows later versions of a program to load mementos created by previous versions. If a new property has
  * been introduced, its existence can be tested using {@link #hasProperty(String)}, or (in simple cases) a
  * default value can be used in case the property is missing. For example:
  * <pre>
  *    // if preferred language is missing, use default value
  *    String lang = personMemento.getString("preferred language", "English");
  * 
  *    if (!personMemento.hasProperty("address")) {
  *        // handle missing address
  *    }
  * </pre>
  * <p>
  * In general, queries for properties that are non-existent or that have a type different from the one requested are
  * considered a format error and result in {@link MementoFormatException}.
  * @author Kazó Csaba
  */
 public class Memento implements Iterable<Memento> {
 	private final Map<String,Object> properties;
 	private final List<Memento> children;
 	private final String type;
 	
 	/**
 	 * Creates a new empty memento instance.
 	 */
 	public Memento() {
 		this(null);
 	}
 	
 	/**
 	 * Creates a new empty memento instance.
 	 *
 	 * @param type the type of the memento; can be {@code null}
 	 */
 	public Memento(String type) {
 		properties=new HashMap<>();
 		children=new ArrayList<>();
 		this.type=type;
 	}
 	
 	@Override
 	public boolean equals(Object obj) {
 		if (!(obj instanceof Memento)) return false;
 		Memento mem=(Memento)obj;
 		return mem.properties.equals(properties) && 
 				mem.children.equals(children) &&
 				(type==null ? mem.type==null : type.equals(mem.type));
 	}
 	
 	@Override
 	public int hashCode() {
 		return properties.hashCode();
 	}
 	/**
 	 * Returns the type of the memento.
 	 * @return the type of the memento, or {@code null} if no type has been
 	 * specified
 	 */
 	public String getType() {return type;}
 	/**
 	 * Creates a {@code String} property. If a property with the given
 	 * key already exists, it is replaced.
 	 * @param key the key of the property
 	 * @param value the value of the property
 	 * @return this memento
 	 */
 	public Memento putString(String key,String value) {
 		Objects.requireNonNull(key, "null key");
 		Objects.requireNonNull(value, "null value");
 		properties.put(key, value);
 		return this;
 	}
 	/**
 	 * Creates an int property. If a property with the given
 	 * key already exists, it is replaced.
 	 * @param key the key of the property
 	 * @param value the value of the property
 	 * @return this memento
 	 */
 	public Memento putInt(String key,int value) {
 		Objects.requireNonNull(key, "null key");
 		properties.put(key, value);
 		return this;
 	}
 	/**
 	 * Creates a long property. If a property with the given
 	 * key already exists, it is replaced.
 	 * @param key the key of the property
 	 * @param value the value of the property
 	 * @return this memento
 	 */
 	public Memento putLong(String key,long value) {
 		Objects.requireNonNull(key, "null key");
 		properties.put(key, value);
 		return this;
 	}
 	/**
 	 * Creates a float property. If a property with the given
 	 * key already exists, it is replaced.
 	 * @param key the key of the property
 	 * @param value the value of the property
 	 * @return this memento
 	 */
 	public Memento putFloat(String key,float value) {
 		Objects.requireNonNull(key, "null key");
 		properties.put(key, value);
 		return this;
 	}
 	/**
 	 * Creates a double property. If a property with the given
 	 * key already exists, it is replaced.
 	 * @param key the key of the property
 	 * @param value the value of the property
 	 * @return this memento
 	 */
 	public Memento putDouble(String key,double value) {
 		Objects.requireNonNull(key, "null key");
 		properties.put(key, value);
 		return this;
 	}
 	/**
 	 * Creates a boolean property. If a property with the given
 	 * key already exists, it is replaced.
 	 * @param key the key of the property
 	 * @param value the value of the property
 	 * @return this memento
 	 */
 	public Memento putBoolean(String key,boolean value) {
 		Objects.requireNonNull(key, "null key");
 		properties.put(key, value);
 		return this;
 	}
 	/**
 	 * Creates a char property. If a property with the given
 	 * key already exists, it is replaced.
 	 * @param key the key of the property
 	 * @param value the value of the property
 	 * @return this memento
 	 */
 	public Memento putChar(String key,char value) {
 		Objects.requireNonNull(key, "null key");
 		properties.put(key, value);
 		return this;
 	}
 	/**
 	 * Creates a string array property. If a property with the given key
 	 * already exists, it is replaced. The array is stored by value, no further
 	 * modifications of it is permissible. No elements of the array may be
 	 * {@code null}.
 	 * @param key the key of the property
 	 * @param value the value of the property
 	 * @return this memento
 	 */
 	public Memento putStringArray(String key,String[] value) {
 		Objects.requireNonNull(key, "null key");
 		Objects.requireNonNull(value, "null value");
 		for (String s: value) Objects.requireNonNull(s, "null array element");
 		properties.put(key,new StringArray(value));
 		return this;
 	}
 	/**
 	 * Creates a string array property from a list of strings.
 	 * If a property with the given key
 	 * already exists, it is replaced. No elements of the list may be
 	 * {@code null}.
 	 * @param key the key of the property
 	 * @param value the value of the property
 	 * @return this memento
 	 */
 	public Memento putStringArray(String key,List<String> value) {
 		return putStringArray(key, value.toArray(new String[value.size()]));
 	}
 	/**
 	 * Creates an integer array property. If a property with the given key
 	 * already exists, it is replaced. The array is stored by value, no further
 	 * modifications of it is permissible.
 	 * @param key the key of the property
 	 * @param value the value of the property
 	 * @return this memento
 	 */
 	public Memento putIntArray(String key,int[] value) {
 		Objects.requireNonNull(key, "null key");
 		Objects.requireNonNull(value, "null value");
 		properties.put(key,new IntArray(value));
 		return this;
 	}
 	/**
 	 * Creates an integer array property from a list of integers.
 	 * If a property with the given key
 	 * already exists, it is replaced. No elements of the list may be
 	 * {@code null}.
 	 * @param key the key of the property
 	 * @param value the value of the property
 	 * @return this memento
 	 */
 	public Memento putIntArray(String key,List<Integer> value) {
 		Objects.requireNonNull(key, "null key");
 		Objects.requireNonNull(value, "null value");
 		int[] array=new int[value.size()];
 		for (int i=0; i<array.length; i++) array[i]=value.get(i);
 		properties.put(key,new IntArray(array));
 		return this;
 	}
 	/**
 	 * Creates a byte array property. If a property with the given key
 	 * already exists, it is replaced. The array is stored by value, no further
 	 * modifications of it is permissible.
 	 * @param key the key of the property
 	 * @param value the value of the property
 	 * @return this memento
 	 */
 	public Memento putByteArray(String key,byte[] value) {
 		Objects.requireNonNull(key, "null key");
 		Objects.requireNonNull(value, "null value");
 		properties.put(key,new ByteArray(value));
 		return this;
 	}
 	/**
 	 * Creates a double array property. If a property with the given key
 	 * already exists, it is replaced. The array is stored by value, no further
 	 * modifications of it is permissible.
 	 * @param key the key of the property
 	 * @param value the value of the property
 	 * @return this memento
 	 */
 	public Memento putDoubleArray(String key,double[] value) {
 		Objects.requireNonNull(key, "null key");
 		Objects.requireNonNull(value, "null value");
 		properties.put(key,new DoubleArray(value));
 		return this;
 	}
 	/**
 	 * Returns the type of a property.
 	 * @param key the key of the property
 	 * @return {@code null} if no property with the given key exists,
 	 * otherwise the type of the property as {@code String.class},
 	 * {@code Integer.class}, {@code Float.class},
 	 * {@code Boolean.class}, {@code String[].class},
 	 * {@code Integer[].class}, {@code Byte[].class},
 	 * or {@code Double[].class}.
 	 */
 	public Class<?> getPropertyType(String key) {
 		Objects.requireNonNull(key, "null key");
 		Object value=properties.get(key);
 		if (value==null) return null;
 		if (value.getClass()==StringArray.class) return String[].class;
 		if (value.getClass()==IntArray.class) return Integer[].class;
 		if (value.getClass()==ByteArray.class) return Byte[].class;
 		if (value.getClass()==DoubleArray.class) return Double[].class;
 		return value.getClass();
 	}
 	
 	/**
 	 * Returns whether a property with the given key exists. This method is equivalent to
 	 * <pre>    getPropertyType(key) != null</pre>
 	 * 
 	 * @param key a key
	 * @return 
 	 */
 	public boolean hasProperty(String key) {
 		Objects.requireNonNull(key, "null key");
 		return properties.containsKey(key);
 	}
 	/**
 	 * Removes the property with the given key.
 	 * @param key the key of the property to remove
 	 * @throws NoSuchPropertyException if the key doesn't denote a property
 	 */
 	public void removeProperty(String key) throws NoSuchPropertyException {
 		if (properties.remove(key)==null) throw new NoSuchPropertyException();
 	}
 	/**
 	 * Returns the value of the String property identified by the given key.
 	 * @param key the key of the property to retrieve
 	 * @return the value of the property
 	 * @throws TypeMismatchException if the property is of a different type
 	 * @throws NoSuchPropertyException if the key doesn't denote a property
 	 */
 	public String getString(String key) throws TypeMismatchException,NoSuchPropertyException {
 		Objects.requireNonNull(key, "null key");
 		Object value=properties.get(key);
 		if (value==null) throw new NoSuchPropertyException();
 		if (value.getClass()!=String.class) throw new TypeMismatchException();
 		return (String)value;
 	}
 	/**
 	 * Returns the value of the String property identified by the given key. If
 	 * the key doesn't denote a property, the given default value is returned.
 	 * @param key the key of the property to retrieve
 	 * @param defaultValue the value to return if no property with the given key exists; can be {@code null}
 	 * @return the value of the property, or the provided default value if the
 	 * key doesn't denote a property
 	 * @throws TypeMismatchException if the property is of a different type
 	 */
 	public String getString(String key,String defaultValue) throws TypeMismatchException {
 		Objects.requireNonNull(key, "null key");
 		Object value=properties.get(key);
 		if (value==null) return defaultValue;
 		if (value.getClass()!=String.class) throw new TypeMismatchException();
 		return (String)value;
 	}
 	/**
 	 * Returns the value of the integer property identified by the given key.
 	 * @param key the key of the property to retrieve
 	 * @return the value of the property
 	 * @throws TypeMismatchException if the property is of a different type
 	 * @throws NoSuchPropertyException if the key doesn't denote a property
 	 */
 	public int getInt(String key) throws TypeMismatchException,NoSuchPropertyException {
 		Objects.requireNonNull(key, "null key");
 		Object value=properties.get(key);
 		if (value==null) throw new NoSuchPropertyException();
 		if (value.getClass()!=Integer.class) throw new TypeMismatchException();
 		return (Integer)value;
 	}
 	/**
 	 * Returns the value of the integer property identified by the given key. If
 	 * the key doesn't denote a property, the given default value is returned.
 	 * @param key the key of the property to retrieve
 	 * @param defaultValue the value to return if no property with the given key exists
 	 * @return the value of the property, or the provided default value if the
 	 * key doesn't denote a property
 	 * @throws TypeMismatchException if the property is of a different type
 	 */
 	public int getInt(String key,int defaultValue) throws TypeMismatchException {
 		Objects.requireNonNull(key, "null key");
 		Object value=properties.get(key);
 		if (value==null) return defaultValue;
 		if (value.getClass()!=Integer.class) throw new TypeMismatchException();
 		return (Integer)value;
 	}
 	/**
 	 * Returns the value of the long property identified by the given key.
 	 * @param key the key of the property to retrieve
 	 * @return the value of the property
 	 * @throws TypeMismatchException if the property is of a different type
 	 * @throws NoSuchPropertyException if the key doesn't denote a property
 	 */
 	public long getLong(String key) throws TypeMismatchException,NoSuchPropertyException {
 		Objects.requireNonNull(key, "null key");
 		Object value=properties.get(key);
 		if (value==null) throw new NoSuchPropertyException();
 		if (value.getClass()!=Long.class) throw new TypeMismatchException();
 		return (Long)value;
 	}
 	/**
 	 * Returns the value of the long property identified by the given key. If
 	 * the key doesn't denote a property, the given default value is returned.
 	 * @param key the key of the property to retrieve
 	 * @param defaultValue the value to return if no property with the given key exists
 	 * @return the value of the property, or the provided default value if the
 	 * key doesn't denote a property
 	 * @throws TypeMismatchException if the property is of a different type
 	 */
 	public long getLong(String key,long defaultValue) throws TypeMismatchException {
 		Objects.requireNonNull(key, "null key");
 		Object value=properties.get(key);
 		if (value==null) return defaultValue;
 		if (value.getClass()!=Long.class) throw new TypeMismatchException();
 		return (Long)value;
 	}
 	/**
 	 * Returns the value of the float property identified by the given key.
 	 * @param key the key of the property to retrieve
 	 * @return the value of the property
 	 * @throws TypeMismatchException if the property is of a different type
 	 * @throws NoSuchPropertyException if the key doesn't denote a property
 	 */
 	public float getFloat(String key) throws TypeMismatchException,NoSuchPropertyException {
 		Objects.requireNonNull(key, "null key");
 		Object value=properties.get(key);
 		if (value==null) throw new NoSuchPropertyException();
 		if (value.getClass()!=Float.class) throw new TypeMismatchException();
 		return (Float)value;
 	}
 	/**
 	 * Returns the value of the float property identified by the given key. If
 	 * the key doesn't denote a property, the given default value is returned.
 	 * @param key the key of the property to retrieve
 	 * @param defaultValue the value to return if no property with the given key exists
 	 * @return the value of the property, or the provided default value if the
 	 * key doesn't denote a property
 	 * @throws TypeMismatchException if the property is of a different type
 	 */
 	public float getFloat(String key,float defaultValue) throws TypeMismatchException {
 		Objects.requireNonNull(key, "null key");
 		Object value=properties.get(key);
 		if (value==null) return defaultValue;
 		if (value.getClass()!=Float.class) throw new TypeMismatchException();
 		return (Float)value;
 	}
 	/**
 	 * Returns the value of the double property identified by the given key.
 	 * @param key the key of the property to retrieve
 	 * @return the value of the property
 	 * @throws TypeMismatchException if the property is of a different type
 	 * @throws NoSuchPropertyException if the key doesn't denote a property
 	 */
 	public double getDouble(String key) throws TypeMismatchException,NoSuchPropertyException {
 		Objects.requireNonNull(key, "null key");
 		Object value=properties.get(key);
 		if (value==null) throw new NoSuchPropertyException();
 		if (value.getClass()!=Double.class) throw new TypeMismatchException();
 		return (Double)value;
 	}
 	/**
 	 * Returns the value of the double property identified by the given key. If
 	 * the key doesn't denote a property, the given default value is returned.
 	 * @param key the key of the property to retrieve
 	 * @param defaultValue the value to return if no property with the given key exists
 	 * @return the value of the property, or the provided default value if the
 	 * key doesn't denote a property
 	 * @throws TypeMismatchException if the property is of a different type
 	 */
 	public double getDouble(String key,double defaultValue) throws TypeMismatchException {
 		Objects.requireNonNull(key, "null key");
 		Object value=properties.get(key);
 		if (value==null) return defaultValue;
 		if (value.getClass()!=Double.class) throw new TypeMismatchException();
 		return (Double)value;
 	}
 	/**
 	 * Returns the value of the boolean property identified by the given key.
 	 * @param key the key of the property to retrieve
 	 * @return the value of the property
 	 * @throws TypeMismatchException if the property is of a different type
 	 * @throws NoSuchPropertyException if the key doesn't denote a property
 	 */
 	public boolean getBoolean(String key) throws TypeMismatchException,NoSuchPropertyException {
 		Objects.requireNonNull(key, "null key");
 		Object value=properties.get(key);
 		if (value==null) throw new NoSuchPropertyException();
 		if (value.getClass()!=Boolean.class) throw new TypeMismatchException();
 		return (Boolean)value;
 	}
 	/**
 	 * Returns the value of the boolean property identified by the given key. If
 	 * the key doesn't denote a property, the given default value is returned.
 	 * @param key the key of the property to retrieve
 	 * @param defaultValue the value to return if no property with the given key exists
 	 * @return the value of the property, or the provided default value if the
 	 * key doesn't denote a property
 	 * @throws TypeMismatchException if the property is of a different type
 	 */
 	public boolean getBoolean(String key,boolean defaultValue) throws TypeMismatchException {
 		Objects.requireNonNull(key, "null key");
 		Object value=properties.get(key);
 		if (value==null) return defaultValue;
 		if (value.getClass()!=Boolean.class) throw new TypeMismatchException();
 		return (Boolean)value;
 	}
 	/**
 	 * Returns the value of the character property identified by the given key.
 	 * @param key the key of the property to retrieve
 	 * @return the value of the property
 	 * @throws TypeMismatchException if the property is of a different type
 	 * @throws NoSuchPropertyException if the key doesn't denote a property
 	 */
 	public char getChar(String key) throws TypeMismatchException,NoSuchPropertyException {
 		Objects.requireNonNull(key, "null key");
 		Object value=properties.get(key);
 		if (value==null) throw new NoSuchPropertyException();
 		if (value.getClass()!=Character.class) throw new TypeMismatchException();
 		return (Character)value;
 	}
 	/**
 	 * Returns the value of the character property identified by the given key. If
 	 * the key doesn't denote a property, the given default value is returned.
 	 * @param key the key of the property to retrieve
 	 * @param defaultValue the value to return if no property with the given key exists
 	 * @return the value of the property, or the provided default value if the
 	 * key doesn't denote a property
 	 * @throws TypeMismatchException if the property is of a different type
 	 */
 	public char getChar(String key,char defaultValue) throws TypeMismatchException {
 		Objects.requireNonNull(key, "null key");
 		Object value=properties.get(key);
 		if (value==null) return defaultValue;
 		if (value.getClass()!=Character.class) throw new TypeMismatchException();
 		return (Character)value;
 	}
 	/**
 	 * Returns the value of the string array property identified by the given key.
 	 * The array is returned by reference.
 	 * @param key the key of the property to retrieve
 	 * @return the value of the property
 	 * @throws TypeMismatchException if the property is of a different type
 	 * @throws NoSuchPropertyException if the key doesn't denote a property
 	 */
 	public String[] getStringArray(String key) throws TypeMismatchException,NoSuchPropertyException {
 		Objects.requireNonNull(key, "null key");
 		Object value=properties.get(key);
 		if (value==null) throw new NoSuchPropertyException();
 		if (value.getClass()!=StringArray.class) throw new TypeMismatchException();
 		return ((StringArray)value).getArray();
 	}
 	/**
 	 * Adds the value of the string array property identified by the given key to
 	 * the specified list.
 	 * @param key the key of the property to retrieve
 	 * @param list the list to add the strings to
 	 * @throws TypeMismatchException if the property is of a different type
 	 * @throws NoSuchPropertyException if the key doesn't denote a property
 	 */
 	public void getStringArray(String key,List<String> list) throws TypeMismatchException,NoSuchPropertyException {
 		Objects.requireNonNull(key, "null key");
 		Objects.requireNonNull(list, "null list");
 		Object value=properties.get(key);
 		if (value==null) throw new NoSuchPropertyException();
 		if (value.getClass()!=StringArray.class) throw new TypeMismatchException();
 		list.addAll(Arrays.asList(((StringArray)value).getArray()));
 	}
 	/**
 	 * Returns the value of the integer array property identified by the given key.
 	 * The array is returned by reference.
 	 * @param key the key of the property to retrieve
 	 * @return the value of the property
 	 * @throws TypeMismatchException if the property is of a different type
 	 * @throws NoSuchPropertyException if the key doesn't denote a property
 	 */
 	public int[] getIntArray(String key) throws TypeMismatchException,NoSuchPropertyException {
 		Objects.requireNonNull(key, "null key");
 		Object value=properties.get(key);
 		if (value==null) throw new NoSuchPropertyException();
 		if (value.getClass()!=IntArray.class) throw new TypeMismatchException();
 		return ((IntArray)value).getArray();
 	}
 	/**
 	 * Adds the value of the integer array property identified by the given key to
 	 * the specified list.
 	 * @param key the key of the property to retrieve
 	 * @param list the list to add the integers to
 	 * @throws TypeMismatchException if the property is of a different type
 	 * @throws NoSuchPropertyException if the key doesn't denote a property
 	 */
 	public void getIntArray(String key,List<Integer> list) throws TypeMismatchException,NoSuchPropertyException {
 		Objects.requireNonNull(key, "null key");
 		Objects.requireNonNull(list, "null list");
 		Object value=properties.get(key);
 		if (value==null) throw new NoSuchPropertyException();
 		if (value.getClass()!=IntArray.class) throw new TypeMismatchException();
 		for (int s:((IntArray)value).getArray()) list.add(s);
 	}
 	/**
 	 * Returns the value of the byte array property identified by the given key.
 	 * The array is returned by reference.
 	 * @param key the key of the property to retrieve
 	 * @return the value of the property
 	 * @throws TypeMismatchException if the property is of a different type
 	 * @throws NoSuchPropertyException if the key doesn't denote a property
 	 */
 	public byte[] getByteArray(String key) throws TypeMismatchException,NoSuchPropertyException {
 		Objects.requireNonNull(key, "null key");
 		Object value=properties.get(key);
 		if (value==null) throw new NoSuchPropertyException();
 		if (value.getClass()!=ByteArray.class) throw new TypeMismatchException();
 		return ((ByteArray)value).getArray();
 	}
 	/**
 	 * Returns the value of the double array property identified by the given key.
 	 * The array is returned by reference.
 	 * @param key the key of the property to retrieve
 	 * @return the value of the property
 	 * @throws TypeMismatchException if the property is of a different type
 	 * @throws NoSuchPropertyException if the key doesn't denote a property
 	 */
 	public double[] getDoubleArray(String key) throws TypeMismatchException,NoSuchPropertyException {
 		Objects.requireNonNull(key, "null key");
 		Object value=properties.get(key);
 		if (value==null) throw new NoSuchPropertyException();
 		if (value.getClass()!=DoubleArray.class) throw new TypeMismatchException();
 		return ((DoubleArray)value).getArray();
 	}
 	/**
 	 * Returns an iterator over they keys of this memento's properties. While the
 	 * iterator is used, the property putter methods should not be called.
 	 * @return an iterator over the keys of the properties
 	 */
 	public Iterator<String> iterateProperties() {
 		return properties.keySet().iterator();
 	}
 	/**
 	 * Returns an iterator over this memento's children.
 	 * @return an iterator over this memento's children
 	 */
 	public Iterator<Memento> iterateChildren() {
 		return children.iterator();
 	}
 	/**
 	 * Returns an immutable view of this memento's children.
 	 * @return an immutable view of this memento's children
 	 */
 	public List<Memento> getChildren() {
 		return Collections.unmodifiableList(children);
 	}
 	/**
 	 * Returns the number of this memento's children.
 	 * @return the number of this memento's children
 	 */
 	public int getChildCount() {
 		return children.size();
 	}
 	/**
 	 * Returns the number of this memento's properties.
 	 * @return the number of this memento's properties
 	 */
 	public int getPropertyCount() {
 		return properties.size();
 	}
 	/**
 	 * Returns the first child of this memento. This is a convenience method
 	 * for cases when the memento should have exactly one child.
 	 * @return the first child of the memento
 	 * @throws MementoFormatException if this memento has no children
 	 */
 	public Memento getFirstChild() throws MementoFormatException {
 		if (children.isEmpty()) throw new MementoFormatException("No children");
 		return children.get(0);
 	}
 	/**
 	 * Returns the first child of this memento with the specified type.
 	 * @param type the type; can be {@code null}
 	 * @return the first child of the memento with the given type
 	 * @throws MementoFormatException if this memento has no child with the given type
 	 */
 	public Memento getFirstChildWithType(String type) throws MementoFormatException {
 		for (Memento child:children) if (Objects.equals(type, child.getType())) return child;
 		throw new MementoFormatException("No child with type "+type);
 	}
 	/**
 	 * Creates a new child of this memento. The new child will be the last
 	 * of this memento's children.
 	 * @return the newly created child memento
 	 */
 	public Memento createChild() {return createChild(null);}
 	/**
 	 * Creates a new child of this memento. The new child will be the last
 	 * of this memento's children.
 	 * @param type the type of the child memento; can be {@code null}
 	 * @return the newly created child memento
 	 */
 	public Memento createChild(String type) {
 		Memento child=new Memento(type);
 		children.add(child);
 		return child;
 	}
 	/**
 	 * Copies the parameter memento into this one. First the properties are
 	 * added to this (overwriting any existing properties in case of a conflict),
 	 * then child mementos are created corresponding to the argument's
 	 * children, and each child is copied in the same fashion.
 	 * @param memento the memento to copy into this
 	 */
 	public void copyFrom(Memento memento) {
 		properties.putAll(memento.properties);
 		for (Memento child:memento.children)
 			createChild().copyFrom(child);
 	}
 	/**
 	 * Returns the same as {@link #iterateChildren()}.
 	 * @return an iterator over the child mementos
 	 */
 	@Override
 	public Iterator<Memento> iterator() {
 		return iterateChildren();
 	}
 }
