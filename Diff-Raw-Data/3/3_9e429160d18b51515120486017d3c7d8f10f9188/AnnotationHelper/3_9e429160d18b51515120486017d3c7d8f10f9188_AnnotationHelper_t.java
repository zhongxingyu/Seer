 package net.noratargo.siJACK.annotationHelper;
 
 import net.noratargo.siJACK.Parameter;
 import net.noratargo.siJACK.ParameterPrefixNamePair;
 import net.noratargo.siJACK.annotations.DefaultValue;
 import net.noratargo.siJACK.annotations.Description;
 import net.noratargo.siJACK.annotations.Name;
 import net.noratargo.siJACK.annotations.Prefix;
 import net.noratargo.siJACK.interfaces.InstantiatorManager;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Field;
 import java.lang.reflect.Modifier;
 import java.util.HashSet;
 import java.util.Set;
 
 public class AnnotationHelper {
 
 	/**
 	 * Creates a {@link Parameter} object for the given {@link Field}, if at least one of the folowing attributes is
 	 * present: {@link Description}, {@link Prefix}, {@link Name}, {@link DefaultValue}.
 	 * 
 	 * @param <T>
 	 *            The type of the default value, that the returned Parameter will represent.
 	 * @param f
 	 *            The field for that this Parameter will be.
 	 * @param defaultValue
 	 *            The default value of the given field. May be <code>null</code> if it is not given.
 	 * @param wasFieldAccessible
 	 *            Set to <code>true</code> if the field's value was accessible.
 	 * @param im
 	 *            The manager to use for creating a new instance, if a {@link DefaultValue} annotation is present.
 	 * @return The Parameter for the given Field, or <code>null</code> if this Field is missing the required
 	 *         annotations.
 	 */
 	@SuppressWarnings("unchecked")
 	public static <T> Parameter<T> createParameter(Field f, T defaultValue, boolean wasFieldAccessible,
 			InstantiatorManager im) {
 
 		/* obtain all intresting annotations. if none of them is set, then we will skip this field and return null. */
 		Prefix fieldPrefix = f.getAnnotation(Prefix.class);
 		Name name = f.getAnnotation(Name.class);
 		DefaultValue defaultValueAnnotation = f.getAnnotation(DefaultValue.class);
 		Description description = f.getAnnotation(Description.class);
 
 		/* The field must not be final and at least one of the previously requested annotations MUST BE present. */
 		if (!Modifier.isFinal(f.getModifiers())
 				&& (fieldPrefix != null || name != null || defaultValueAnnotation != null || description != null)) {
 			/* Determine default prefix: */
 			String defaultPrefix = PrefixAnnotationHelper.getDefaultPrefix(
 					f.getDeclaringClass().getAnnotation(Prefix.class), fieldPrefix, f);
 
 			/* Determine default name: */
 			String defaultName = NameAnnotationHelper.getDefaultName(name, f);
 
 			/* Determine default value: */
 			T determinedDefaultValue = getDefaultValue(defaultValue, (Class<T>) f.getType(), defaultValueAnnotation, im);
 
 			/* Determine all PrefixName pairs: */
 			Set<ParameterPrefixNamePair> ppnp = getParameterPrefixPairs(PrefixAnnotationHelper.fillPrefixes(f),
 					NameAnnotationHelper.fillNames(f));
 
 			/* Get the description: */
 			String descriptionStr = getDescription(description);
 
 			boolean hasADefaultValue = wasFieldAccessible
 					|| hasDefaultValue(defaultValue, (Class<T>) f.getType(), defaultValueAnnotation);
 
 			/* Create and return the Parameter: */
 			return new Parameter<T>(determinedDefaultValue, (Class<T>) f.getType(), ppnp, defaultPrefix, defaultName,
 					descriptionStr, hasADefaultValue);
 		}
 
 		return null;
 	}
 
 	/**
 	 * Returns a list of parameters, that represent the current Constructor's parameters.
 	 * 
 	 * @param c
 	 *            The constructor to represent.
 	 * @param im
 	 *            The {@link InstantiatorManager} to use for determining the default values.
 	 * @return A list in which every element represents one parameter of the current constructor. The order of the
 	 *         elements is equal to the order of the constructor's parameters. rturns <code>null</code>, if this
 	 *         constructor should be avoided.
 	 */
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	public static Parameter<?>[] createParametersFromConstructor(Constructor<?> c, InstantiatorManager im) {
 		Parameter<?>[] parameters = new Parameter<?>[c.getParameterTypes().length];
 		Annotation[][] pAnnotations = c.getParameterAnnotations();
 		Class<?>[] cTypes = c.getParameterTypes();
 
		/* we will add constructors with no parameters. */
		boolean skipThisOne = parameters.length > 0;
 
 		for (int i = 0; i < parameters.length; i++) {
 			/* this represents all annotations of the current parameter: */
 			Annotation[] annotations = pAnnotations[i];
 
 			Prefix p = null;
 			Name n = null;
 			DefaultValue dv = null;
 			Description des = null;
 
 			/* try to get the current parameter's annotations: */
 			for (Annotation a : annotations) {
 				if (a instanceof Prefix) {
 					p = (Prefix) a;
 				} else if (a instanceof Name) {
 					n = (Name) a;
 				} else if (a instanceof DefaultValue) {
 					dv = (DefaultValue) a;
 				} else if (a instanceof Description) {
 					des = (Description) a;
 				} else {
 					/* no annotation, that we support here. */
 				}
 			}
 
 			if (p != null || n != null || dv != null || des != null) {
 				skipThisOne = false;
 
 				/* this is the current parameter's type: */
 				Class<?> type = cTypes[i];
 
 				/* we got the annotations. so let's put them together. */
 
 				/* Determine default prefix: */
 				String defaultPrefix = PrefixAnnotationHelper.getDefaultPrefix(
 						c.getDeclaringClass().getAnnotation(Prefix.class), c.getAnnotation(Prefix.class), p, c, i);
 
 				/* Determine default name: */
 				String defaultName = NameAnnotationHelper.getDefaultName(n, c, i);
 
 				/* Determine default value: */
 				Object defaultValue = getDefaultValue(null, type, dv, im);
 
 				/* Determine all PrefixName pairs: */
 				Set<ParameterPrefixNamePair> ppnp = getParameterPrefixPairs(PrefixAnnotationHelper.fillPrefixes(c, p),
 						NameAnnotationHelper.fillNames(n));
 
 				/* Get the description: */
 				String description = getDescription(des);
 
 				boolean hasADefaultValue = hasDefaultValue(null, type, dv);
 
 				/* Create and return the Parameter: */
 				parameters[i] = new Parameter(defaultValue, type, ppnp, defaultPrefix, defaultName, description, hasADefaultValue);
 			}
 		}
 
 		return skipThisOne ? null : parameters;
 	}
 
 	private static Set<ParameterPrefixNamePair> getParameterPrefixPairs(Set<String> prefixes, Set<String> names) {
 		Set<ParameterPrefixNamePair> ppnp = new HashSet<ParameterPrefixNamePair>();
 
 		/* build all possible prefix-name constellations, that are posible: */
 		for (String prefix : prefixes) {
 			for (String name : names) {
 				ppnp.add(new ParameterPrefixNamePair(prefix, name));
 			}
 		}
 
 		return ppnp;
 	}
 
 	/**
 	 * Returns the description from the given annotation or an empty String (""), if the annotation is not set.
 	 * 
 	 * @param d
 	 *            The {@link Description} annotation. May be <code>null</code>.
 	 * @return The description or an empty Stirng.
 	 */
 	public static String getDescription(Description d) {
 		return (d == null ? "" : d.value());
 	}
 
 	/**
 	 * Returns the default value - depending on the given input.
 	 * 
 	 * @param <T>
 	 *            The type of the default value.
 	 * @param defaultValue
 	 *            The default value, as it is given on the current field.
 	 * @param targetType
 	 * @param d
 	 * @param im
 	 * @return
 	 */
 	@SuppressWarnings("unchecked")
 	public static <T> T getDefaultValue(T defaultValue, Class<T> targetType, DefaultValue d, InstantiatorManager im) {
 		if (defaultValue != null) {
 			return im.getNewInstanceFrom(targetType, defaultValue);
 		}
 
 		if (d != null) {
 			if (d.isNull()) {
 				return null;
 			}
 
 			return im.getNewInstanceFor(targetType, d.value());
 		}
 
 		/* look for nativ datatypes: */
 		if (targetType == boolean.class) {
 			return (T) new Boolean(false);
 		}
 		if (targetType == int.class) {
 			return (T) new Integer(0);
 		}
 		if (targetType == long.class) {
 			return (T) new Long(0);
 		}
 		if (targetType == short.class) {
 			return (T) new Short((short) 0);
 		}
 		if (targetType == byte.class) {
 			return (T) new Byte((byte) 0);
 		}
 		if (targetType == char.class) {
 			return (T) new Character((char) 0);
 		}
 		if (targetType == float.class) {
 			return (T) new Float(0);
 		}
 		if (targetType == double.class) {
 			return (T) new Double(0);
 		}
 
 		/* nothing set: */
 		return null;
 	}
 
 	public static <T> boolean hasDefaultValue(T defaultValue, Class<T> valueType, DefaultValue d) {
 		if (defaultValue != null) {
 			return true;
 		}
 
 		if (d != null) {
 			if (d.isNull()) {
 				return true;
 			}
 
 			return true;
 		}
 
 		/* look for nativ datatypes: */
 		if (valueType == int.class) {
 			return true;
 		}
 		if (valueType == long.class) {
 			return true;
 		}
 		if (valueType == short.class) {
 			return true;
 		}
 		if (valueType == byte.class) {
 			return true;
 		}
 		if (valueType == char.class) {
 			return true;
 		}
 		if (valueType == float.class) {
 			return true;
 		}
 		if (valueType == double.class) {
 			return true;
 		}
 
 		/* nothing set: */
 		return false;
 	}
 }
