 package eu.stratosphere.sopremo.pact;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.DataInput;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.ObjectStreamClass;
 import java.io.Serializable;
 import java.lang.reflect.Field;
 import java.lang.reflect.Modifier;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.IdentityHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.commons.logging.impl.SimpleLog;
 
 import com.esotericsoftware.kryo.Kryo;
 import com.esotericsoftware.kryo.Registration;
 import com.esotericsoftware.kryo.Serializer;
 import com.esotericsoftware.kryo.io.Input;
 import com.esotericsoftware.kryo.io.Output;
 
 import eu.stratosphere.nephele.configuration.Configuration;
 import eu.stratosphere.nephele.util.StringUtils;
 import eu.stratosphere.sopremo.ICloneable;
 import eu.stratosphere.sopremo.ISopremoType;
 import eu.stratosphere.sopremo.SopremoRuntime;
 import eu.stratosphere.sopremo.expressions.EvaluationExpression;
 import eu.stratosphere.sopremo.expressions.UnevaluableExpression;
 import eu.stratosphere.sopremo.function.SopremoFunction;
 import eu.stratosphere.sopremo.type.IArrayNode;
 import eu.stratosphere.sopremo.type.IJsonNode;
 import eu.stratosphere.sopremo.type.IObjectNode;
 import eu.stratosphere.sopremo.type.ReusingSerializer;
 
 /**
  * Provides utility methods for sopremo
  */
 public class SopremoUtil {
 
 	public static final boolean DEBUG = true;
 
 	public static final Log NORMAL_LOG = LogFactory.getLog(SopremoUtil.class), TRACE_LOG = new SimpleLog(
 		SopremoUtil.class.getName());
 
 	static {
 		((SimpleLog) TRACE_LOG).setLevel(SimpleLog.LOG_LEVEL_TRACE);
 	}
 
 	public static Log LOG = NORMAL_LOG;
 
 	public static final String CONTEXT = "context";
 
 	/**
 	 * Configures an object with the given {@link Configuration} that has been initialized with
 	 * {@link #transferFieldsToConfiguration(Object, Class, Configuration)}.
 	 * 
 	 * @param instance
 	 *        the instance that should be configured
 	 * @param stopClass
 	 *        the class in the hierarchy of instance, up to which the state should be configured.
 	 * @param parameters
 	 *        the configuration that should be used
 	 */
 	public static void configureWithTransferredState(final Object instance, final Class<?> stopClass,
 			final Configuration parameters) {
 		for (Class<?> clazz = instance.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
 			for (final Field stubField : clazz.getDeclaredFields())
 				if ((stubField.getModifiers() & (Modifier.TRANSIENT | Modifier.FINAL | Modifier.STATIC)) == 0) {
 					final Object fieldValue = SopremoUtil.getObject(parameters, stubField.getName(), null);
 					if (fieldValue != null)
 						try {
 							stubField.setAccessible(true);
 							stubField.set(instance, SopremoUtil.getObject(parameters, stubField.getName(), null));
 						} catch (final Exception e) {
 							LOG.error(String.format("Could not set field %s of class %s: %s",
 								stubField.getName(), clazz, StringUtils.stringifyException(e)));
 						}
 				}
 			if (clazz == stopClass)
 				break;
 		}
 	}
 
 	/**
 	 * Transfers the state of an object to the given {@link Configuration}, so that the state can be used to initialize
 	 * a class of the given target class.
 	 * 
 	 * @param sourceInstance
 	 *        the instance, of which the state should be saved
 	 * @param targetClass
 	 *        a reference class, of which the fields need to be initialized
 	 * @param stopClass
 	 *        the class in the hierarchy of instance, up to which the state should be configured.
 	 * @param parameters
 	 *        the configuration that should be used
 	 */
 	public static <S, T> void transferFieldsToConfiguration(S sourceInstance, final Class<? super S> stopClass,
 			Configuration configuration, Class<T> targetClass, Class<? super T> targetStopClass) {
 
 		for (Class<?> tarClass = targetClass; tarClass != null; tarClass = tarClass.getSuperclass()) {
 			for (final Field stubField : tarClass.getDeclaredFields())
 				if ((stubField.getModifiers() & (Modifier.TRANSIENT | Modifier.FINAL | Modifier.STATIC)) == 0)
 					for (Class<?> srcClass = sourceInstance.getClass(); srcClass != null; srcClass = srcClass
 						.getSuperclass()) {
 						Field thisField;
 						try {
 							thisField = srcClass.getDeclaredField(stubField.getName());
 							thisField.setAccessible(true);
 							final Object value = thisField.get(sourceInstance);
 							if (SopremoUtil.DEBUG && value instanceof EvaluationExpression &&
 								((EvaluationExpression) value).findFirst(UnevaluableExpression.class) != null)
 								throw new IllegalStateException(String.format(
 									"Cannot serialize field %s with unevaluable expressions %s",
 									thisField.getName(), value));
 							if (value != null)
 								SopremoUtil.setObject(configuration, stubField.getName(), value);
 						} catch (final NoSuchFieldException e) {
 							// ignore field of stub if the field does not exist in
 							// this operator
 						} catch (final Exception e) {
 							SopremoUtil.LOG.error(String.format(
 								"Could not serialize field %s of class %s: %s",
 								stubField.getName(), sourceInstance.getClass().getSimpleName(), e));
 							throw new RuntimeException(String.format(
 								"Could not serialize field %s of class %s: %s",
 								stubField.getName(), sourceInstance.getClass().getSimpleName(), e), e);
 						}
 						if (srcClass == stopClass)
 							break;
 					}
 
 			if (tarClass == targetStopClass)
 				break;
 		}
 	}
 
 	public static void trace() {
 		LOG = TRACE_LOG;
 	}
 
 	public static void untrace() {
 		LOG = NORMAL_LOG;
 	}
 
 	/**
 	 * Deserializes an {@link Serializable} from a {@link DataInput}.<br>
 	 * Please note that this method is not very efficient.
 	 */
 	public static <T extends Serializable> T deserializeObject(DataInput in, Class<T> clazz)
 			throws IOException {
 		byte[] buffer = new byte[in.readInt()];
 		in.readFully(buffer);
 
 		return byteArrayToSerializable(buffer, clazz, clazz.getClassLoader());
 	}
 
 	@SuppressWarnings({ "unchecked", "unused" })
 	public static <T extends Serializable> T byteArrayToSerializable(byte[] buffer, Class<T> clazz,
 			final ClassLoader classLoader)
 			throws IOException {
 		final ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(buffer)) {
 			/*
 			 * (non-Javadoc)
 			 * @see java.io.ObjectInputStream#resolveClass(java.io.ObjectStreamClass)
 			 */
 			@Override
 			protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
 				try {
 					return classLoader.loadClass(desc.getName());
 				} catch (ClassNotFoundException e) {
 					return super.resolveClass(desc);
 				}
 			}
 		};
 
 		try {
 			return (T) ois.readObject();
 		} catch (ClassNotFoundException e) {
 			throw new IllegalStateException("Cannot deserialize the object", e);
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	public static <T extends ICloneable> List<T> deepClone(List<T> originals) {
 		final ArrayList<T> clones = new ArrayList<T>(originals.size());
 		for (T t : originals)
 			clones.add((T) t.clone());
 		return clones;
 	}
 
 	@SuppressWarnings("unchecked")
 	public static <K, V extends ICloneable> Map<K, V> deepClone(Map<K, V> originals) {
 		final Map<K, V> clones = new HashMap<K, V>(originals.size());
 		for (Entry<K, V> original : originals.entrySet())
 			clones.put(original.getKey(), (V) original.getValue().clone());
 		return clones;
 	}
 
 	@SuppressWarnings("unchecked")
 	public static <K, V> Map<K, V> deepCloneIfPossible(Map<K, V> originals) {
 		final Map<K, V> clones = new HashMap<K, V>(originals.size());
 		for (Entry<K, V> original : originals.entrySet()) {
 			V value = original.getValue();
 			if (value instanceof ICloneable)
 				value = (V) ((ICloneable) value).clone();
 			clones.put(original.getKey(), value);
 		}
 		return clones;
 	}
 
 	public static void assertArguments(SopremoFunction function, int numberOfArguments) {
 		if (!function.accepts(numberOfArguments))
 			throw new IllegalArgumentException(
 				String.format("Cannot use the given function as it does not accept %d arguments", numberOfArguments));
 	}
 
 	private static final Map<Class<?>, Object> DefaultInitializations = new IdentityHashMap<Class<?>, Object>();
 
 	private static synchronized Object getDefault(Class<?> clazz, ICloneable uninitialized) {
 		final Object object = DefaultInitializations.get(clazz);
 		if (object != null)
 			return object;
 		final Object clone = uninitialized.clone();
 		DefaultInitializations.put(clazz, clone);
 		return clone;
 	}
 
 	/**
 	 * @param evaluationExpression
 	 */
 	public static void initTransientFields(ICloneable object) {
 		for (Class<?> clazz = object.getClass(); clazz != Object.class; clazz = clazz.getSuperclass()) {
 			final Field[] fields = clazz.getDeclaredFields();
 			for (Field field : fields)
 				try {
 					if ((field.getModifiers() & Modifier.TRANSIENT) > 0) {
 						final Class<?> type = field.getType();
 						field.setAccessible(true);
 
 						// already initialized
 						if (field.get(object) != null)
 							continue;
 
 						if (ICloneable.class.isAssignableFrom(type)) { // make copy of one default instantiation
 							final Object defaultObject = getDefault(object.getClass(), object);
 							field.set(object, ((ICloneable) field.get(defaultObject)).clone());
 						} else
 							// try to create new object
 							field.set(object, type.newInstance());
 					}
 				} catch (Exception e) {
 					LOG.error("Cannot initialize transient field " + field, e);
 				}
 		}
 	}
 
 	/**
 	 * Appends the textual representation of the given objects to the appendable.
 	 */
 	public static void append(Appendable appendable, Object... objects) throws IOException {
 		for (Object object : objects)
 			if (object instanceof CharSequence)
 				appendable.append((CharSequence) object);
 			else if (object instanceof ISopremoType)
 				((ISopremoType) object).appendAsString(appendable);
 			else if (object instanceof Character)
 				appendable.append((Character) object);
 			else
 				appendable.append(object.toString());
 	}
 
 	private final static ThreadLocal<Kryo> Serialization = new ThreadLocal<Kryo>() {
 		@Override
 		protected Kryo initialValue() {
 			final Kryo kryo = new Kryo();
			kryo.setReferences(false);
 			kryo.setAutoReset(true);
 			return kryo;
 		};
 	};
 
 	// Hack found in http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6400767
 	private final static Charset BINARY_CHARSET = Charset.forName("ISO-8859-1");
 
 	/**
 	 * @param parameters
 	 * @param context2
 	 * @param object
 	 * @return
 	 */
 	@SuppressWarnings("unchecked")
 	public static <T> T getObject(Configuration config, String keyName, T defaultValue) {
 		final String stringRepresentation = config.getString(keyName, null);
 		if (stringRepresentation == null)
 			return defaultValue;
 		Input input = new Input(stringRepresentation.getBytes(BINARY_CHARSET));
 		final Kryo kryo = Serialization.get();
 		kryo.setClassLoader(SopremoRuntime.getInstance().getClassLoader());
 		return (T) kryo.readClassAndObject(input);
 	}
 
 	/**
 	 * @param config
 	 * @param context2
 	 * @param context3
 	 */
 	public static void setObject(Configuration config, String keyName, Object object) {
 		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
 		final Output output = new Output(baos);
 		final Kryo kryo = Serialization.get();
 		kryo.reset();
 		kryo.writeClassAndObject(output, object);
 		output.close();
 		config.setString(keyName, new String(baos.toByteArray(), BINARY_CHARSET));
 	}
 
 	@SuppressWarnings("unchecked")
 	public static <T extends IJsonNode> void replaceWithCopy(IArrayNode<T> arrayNode, int index, T element) {
 		final T oldValue = arrayNode.get(index);
 		if (oldValue.getType() == element.getType())
 			oldValue.copyValueFrom(element);
 		else
 			arrayNode.set(index, (T) element.clone());
 	}
 
 	/**
 	 * @param node
 	 * @param field
 	 * @param value
 	 */
 	public static void replaceWithCopy(IObjectNode node, String field, IJsonNode element) {
 		final IJsonNode oldValue = node.get(field);
 		if (oldValue.getType() == node.getType())
 			oldValue.copyValueFrom(node);
 		else
 			node.put(field, element.clone());
 	}
 
 	public static byte[] serializableToByteArray(Serializable serializable) {
 		try {
 			final ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
 			final ObjectOutputStream oos = new ObjectOutputStream(byteOutStream);
 			oos.writeObject(serializable);
 			oos.close();
 			return byteOutStream.toByteArray();
 		} catch (IOException e) {
 			throw new IllegalStateException("IO exceptions should not occur locally", e);
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	public static <T> T deserializeInto(Kryo kryo, Input input, T oldNode) {
 		final Registration registration = kryo.readClass(input);
 
 		final Serializer<T> serializer = registration.getSerializer();
 		if (serializer instanceof ReusingSerializer<?> && registration.getType() == oldNode.getClass())
 			return ((ReusingSerializer<T>) serializer).read(kryo, input, oldNode, registration.getType());
 		return serializer.read(kryo, input, registration.getType());
 	}
 }
