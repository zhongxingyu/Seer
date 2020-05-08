 package pt.ist.jpdaplace;
 
 import java.io.Serializable;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Set;
 
public abstract class Place<P
	extends Place<?, ? extends Place<P, C>>, C extends Place<? extends Place<P, C>, ?>>
	implements Serializable {
 
     private static final long serialVersionUID = 1L;
 
     public final P parent;
     public final String name;
     private final Map<String, C> containedPlaces = new HashMap<String, C>();
     private final String[] keys;
 
     public Place(final P parent, final String name, final String... keys) {
 	this.parent = parent;
 	this.name = name;
 	this.keys = Arrays.copyOf(keys, keys.length);
 	for (final String key : keys) {
 	    if (key.trim().length() > 0) {
 		parent.addPlace(key, this);
 	    }
 	}
     }
 
     @SuppressWarnings({ "rawtypes", "unchecked" })
    private void addPlace(final String key, Place place) {
 	containedPlaces.put(key, (C) place);
     }
 
     public C getPlace(final String key) {
 	return containedPlaces.get(key);
     }
 
     public Set<C> getPlaces() {
 	final Collection<C> places = containedPlaces.values();
 	return new HashSet<C>(places);
     }
 
     public String exportAsString() {
 	final StringBuilder result = new StringBuilder();
 	exportAsString(result);
 	return result.toString();
     }
 
     private static final char KEY_SEPERATOR = ':';
     private static final char PLACE_SEPERATOR = ';';
 
     void exportAsString(final StringBuilder result) {
 	if (parent != null) {
 	    parent.exportAsString(result);
 	}
 	if (result.length() > 0) {
 	    result.append(PLACE_SEPERATOR);
 	}
 	for (int i = 0; i < keys.length; i++) {
 	    final String key = keys[i];
 	    if (i > 0) {
 		result.append(KEY_SEPERATOR);
 	    }
 	    result.append(key);
 	}
     }
 
     Place importFrom(final String string) {
 	return importFrom(string, 0);
     }
 
    private Place importFrom(final String string, final int i) {
 	if (i == string.length()) {
 	    return this;
 	}
 	final int ksp = string.indexOf(KEY_SEPERATOR, i);
 	final int psp = string.indexOf(PLACE_SEPERATOR, i);
 	final int sp = ksp > i && (ksp < psp || psp < 0) ? ksp : (psp < 0) ? string.length() : psp;
 	final String key = string.substring(i, sp);
 	final C place = getPlace(key);
 	final int nexti = psp < i ? string.length() : psp + 1;
 	return place.importFrom(string, nexti);
     }
 
     public String getLocalizedName(final Locale locale) {
 	return name;
     }
 
 }
