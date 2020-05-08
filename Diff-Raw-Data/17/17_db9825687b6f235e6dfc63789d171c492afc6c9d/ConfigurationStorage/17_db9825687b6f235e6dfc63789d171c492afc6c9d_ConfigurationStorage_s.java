 package net.noratargo.siJACK;
 
 import net.noratargo.siJACK.annotationHelper.AnnotationHelper;
 import net.noratargo.siJACK.interfaces.ConfigurationManager;
 import net.noratargo.siJACK.interfaces.InstantiatorManager;
 import net.noratargo.siJACK.interfaces.ParameterManager;
 import net.noratargo.siJACK.util.DoubleHashMap;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.NoSuchElementException;
 import java.util.Set;
 
 public class ConfigurationStorage implements ParameterManager, ConfigurationManager {
 
 	private final String seperator;
 
 	/**
 	 * Stores all parameters, grouped by declaring class and the field's name.
 	 */
 	private final HashMap<Field, Parameter<?>> fpParameters;
 
 	private final HashMap<Constructor<?>, Parameter<?>[]> cpParameters;
 
 	/**
 	 * Stores all parameters, grouped by prefix and name. This is used for setting values.
 	 */
 	private final DoubleHashMap<String, String, Set<Parameter<?>>> pnParameters;
 
 	private final ArrayList<PrefixNameValueStorage> unsetValues;
 
 	private final InstantiatorManager im;
 
 	public ConfigurationStorage(String seperator, InstantiatorManager im) {
 		this.seperator = seperator;
 		this.im = im;
 
 		fpParameters = new HashMap<Field, Parameter<?>>();
 		cpParameters = new HashMap<Constructor<?>, Parameter<?>[]>();
 		pnParameters = new DoubleHashMap<String, String, Set<Parameter<?>>>();
 		unsetValues = new ArrayList<PrefixNameValueStorage>();
 	}
 
 	@Override
 	public void setValue(String name, String value) {
 		int index = name.indexOf(seperator);
 
 		if (index == -1) {
 			/* no seperator, therefor no prefix present */
 			setValue("", name, value);
 		} else if (index == 0) {
 			/* the seperatoris at the beginnig - so still no prefix */
 			setValue("", name, value);
 		} else if (index + seperator.length() >= name.length()) {
 			/* no key present */
 			// TODO think about throwing an exception instead!?
 			return;
 		} else {
 			/* We have both, prefix and key. */
 			setValue(name.substring(0, index), name.substring(index + seperator.length(), name.length()), value);
 		}
 	}
 
 	@Override
 	public <T> void setValue(String prefix, String name, String value) {
 		Set<Parameter<?>> parameters = pnParameters.get(prefix, name);
 
 		if (parameters == null) {
 			/* store this change in a list... */
 			unsetValues.add(new PrefixNameValueStorage(prefix, name, value));
 		} else {
 			/* Configure the parameter: */
 			for (Parameter<?> param : parameters) {
 				@SuppressWarnings("unchecked")
 				Parameter<T> p = (Parameter<T>) param;
 				p.setCurrentValue(im.getNewInstanceFor(p.getValueClassType(), value));
 				
 			}
 		}
 	}
 
 	@Override
 	public <T> void applyConfiguration() {
 		Iterator<PrefixNameValueStorage> iterator = unsetValues.iterator();
 
 		while (iterator.hasNext()) {
 			PrefixNameValueStorage unsetValue = iterator.next();
 
 			Set<Parameter<?>> parameters = pnParameters.get(unsetValue.prefix, unsetValue.name);
 
 			if (parameters != null) {
 				/* Configure the parameter and remove the unsetValue (sinc eit now is set): */
 				for (Parameter<?> param : parameters) {
 					@SuppressWarnings("unchecked")
 					Parameter<T> p = (Parameter<T>) param;
 					p.setCurrentValue(im.getNewInstanceFor(p.getValueClassType(), unsetValue.value));
 					
 				}
 			}
 		}
 	}
 
 	@Override
 	public Object getValueFor(Field f) {
 		Parameter<?> p = fpParameters.get(f);
 
 		if (p == null) {
 			throw new NoSuchElementException("The given field is no known parameter: " + f);
 		}
 
 		return im.getNewInstanceFrom(p.getCurrentValue());
 	}
 
 	@Override
 	public String getPrefixNameSeperator() {
 		return seperator;
 	}
 
 	@Override
 	public <T> void addField(Field f, T defaultValue) {
 		if (!fpParameters.containsKey(f)) {
 			Parameter<T> p = AnnotationHelper.createParameter(f, defaultValue, im);
 
 			if (p != null) {
 				/* only insert the Parameter, if it is not added, yet. */
 				fpParameters.put(f, p);
 
 				addPNParameters(p);
 			}
 		}
 	}
 
 	@Override
 	public boolean hasField(Field f) {
 		return fpParameters.containsKey(f);
 	}
 
 	@Override
 	public <T> void addConstructor(Constructor<T> constr) {
 		if (!cpParameters.containsKey(constr)) {
 			Parameter<?>[] parameters = AnnotationHelper.createParametersFromConstructor(constr, im);
 
 			if (parameters != null) {
 				cpParameters.put(constr, parameters);
 
 				for (Parameter<?> p : parameters) {
 					addPNParameters(p);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Adds the given parameter to all its ParameterPreferixNamePairs.
 	 * @param p
 	 */
 	private void addPNParameters(Parameter<?> p) {
 		for (ParameterPrefixNamePair pn : p.getParameterPrefixNamePairs()) {
 			Set<Parameter<?>> pSet = pnParameters.get(pn.getPrefix(), pn.getName());
 			
 			if (pSet == null) {
 				pSet = new HashSet<Parameter<?>>();
 				pnParameters.put(pn.getPrefix(), pn.getName(), pSet);
 			}
 			
 			pSet.add(p);
 		}
 	}
 
 	@Override
 	public Object[] getValuesFor(Constructor<?> c) {
 		Parameter<?>[] pList = cpParameters.get(c);
 		
		Object[] o = new Object[pList.length];
		int i = 0;
		
		for (Parameter<?> p : pList) {
			o[i] = im.getNewInstanceFrom(p.getCurrentValue());
 		}
 		
 		return o;
 	}
 
 	private class PrefixNameValueStorage {
 		public final String prefix;
 		public final String name;
 		public final String value;
 
 		public PrefixNameValueStorage(String prefix, String name, String value) {
 			this.prefix = prefix;
 			this.name = name;
 			this.value = value;
 		}
 	}
 
 }
 
 
