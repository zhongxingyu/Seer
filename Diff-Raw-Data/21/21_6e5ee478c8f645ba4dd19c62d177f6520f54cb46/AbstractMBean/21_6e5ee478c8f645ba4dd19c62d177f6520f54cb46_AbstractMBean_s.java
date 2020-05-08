 package com.mymed.model.data;
 
 import java.io.UnsupportedEncodingException;
 import java.lang.reflect.Field;
 import java.util.HashMap;
 import java.util.Map;
 
 import com.mymed.controller.core.exception.InternalBackEndException;
 
 /**
  * myMed java Beans:
  * 
  * The required conventions are as follows:
  * 
  * -The class must have a public default constructor (no-argument). This allows
  * easy instantiation within editing and activation frameworks.
  * 
  * - The class properties must be accessible using get, set, is (used for
  * boolean properties instead of get) and other methods (so-called accessor
  * methods and mutator methods), following a standard naming-convention. This
  * allows easy automated inspection and updating of bean state within
  * frameworks, many of which include custom editors for various types of
  * properties.
  * 
  * -The class should be serializable. It allows applications and frameworks to
  * reliably save, store, and restore the bean's state in a fashion independent
  * of the VM and of the platform.
  * 
  * - The class must have a getAttributeToMap method, that convert all the fields
  * in a hashMap format for the myMed wrapper
  * 
  * - The class must override toString to have an human readable format
  * 
  * @author lvanni
  */
 public abstract class AbstractMBean {
 
 	/**
 	 * @return all the fields in a hashMap format for the myMed wrapper
 	 * @throws IllegalArgumentException
 	 * @throws IllegalAccessException
 	 * @throws UnsupportedEncodingException
 	 */
 	public Map<String, byte[]> getAttributeToMap() throws InternalBackEndException {
 		final Map<String, byte[]> args = new HashMap<String, byte[]>();
 		for (final Field field : this.getClass().getDeclaredFields()) {
 
 			// Set the field as accessible: it is not really secure in this case
 			// TODO maybe we should invoke the get methods and retrieve the
 			// values in that way
 			field.setAccessible(true);
 
 			try {
				if (field.get(this) instanceof String) {
					args.put(field.getName(), ((String) field.get(this)).getBytes("UTF8"));
				}
			} catch (final UnsupportedEncodingException e) {
				throw new InternalBackEndException("getAttribueToMap failed!: Introspection error");
			} catch (final IllegalArgumentException e) {
				throw new InternalBackEndException("getAttribueToMap failed!: Introspection error");
			} catch (final IllegalAccessException e) {
				e.printStackTrace();
 				throw new InternalBackEndException("getAttribueToMap failed!: Introspection error");
 			}
 		}
 		return args;
 	}
 	/**
 	 * override toString to have an human readable format
 	 */
 	@Override
 	public String toString() {
 		String value = "";
 		for (final Field field : this.getClass().getDeclaredFields()) {
 
 			field.setAccessible(true);
 
 			try {
 				if (field.get(this) instanceof String) {
 					value += "\t" + field.getName() + " : " + (String) field.get(this) + "\n";
 				} else {
 					value += "\t" + field.getName() + " : " + field.get(this) + "\n";
 				}
 			} catch (final IllegalArgumentException e) {
 				e.printStackTrace();
 			} catch (final IllegalAccessException e) {
 				e.printStackTrace();
 			}
 		}
 		return value;
 	}
 }
