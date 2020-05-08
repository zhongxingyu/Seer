 package chameleon.core.element;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.rejuse.association.Reference;
 import org.rejuse.logic.ternary.Ternary;
 import org.rejuse.predicate.Predicate;
 import org.rejuse.predicate.SafePredicate;
 import org.rejuse.predicate.TypePredicate;
 import org.rejuse.predicate.UnsafePredicate;
 import org.rejuse.property.Property;
 import org.rejuse.property.PropertyMutex;
 import org.rejuse.property.PropertySet;
 
 import chameleon.core.Config;
 import chameleon.core.MetamodelException;
 import chameleon.core.language.Language;
 import chameleon.core.language.WrongLanguageException;
 import chameleon.core.lookup.LookupException;
 import chameleon.core.lookup.LookupStrategy;
 import chameleon.core.tag.Tag;
 
 /**
  * @author Marko van Dooren
  * 
  * @opt operations
  * @opt attributes
  * @opt visibility
  * @opt types
  */
 public abstract class ElementImpl<E extends Element, P extends Element> implements Element<E,P> {
 
 	  public ElementImpl() {
 	  }
 	  
 		/********
 		 * TAGS *
 		 ********/
 		
 	  // initialization of this Map is done lazily.
 	  private Map<String, Tag> _tags;
 	  
 	  public Tag tag(String name) {
 	  	if(_tags != null) {
 	      return _tags.get(name);
 	  	} else {
 	  		//lazy init has not been performed yet.
 	  		return null;
 	  	}
 	  }
 
 	  public void removeTag(String name) {
 	  	if(_tags != null) {
 	     Tag old = _tags.get(name);
	     if((old != null) && (old.getElement() != this)){
 	    	 old.setElement(null,name);
 	     }
	     _tags.remove(name);
 	  	}
 	  }
 
 	  public void setTag(Tag decorator, String name) {
 	  	//Lazy init of hashmap
 		  if (_tags==null) {
 	      _tags = new HashMap<String, Tag>();
 	    }
 		  Tag old = _tags.get(name); 
 		  if(old != decorator) {
 	      if((decorator != null) && (decorator.getElement() != this)) {
 	  	    decorator.setElement(this,name);
 	      }
 	      if (old != null) {
 	    	    old.setElement(null,name);
 	      }
 	  	  _tags.put(name, decorator);
 	    }
 	  }
 
 	  public Collection<Tag> tags() {
 	  	if(_tags == null) {
 	  		return new ArrayList();
 	  	} else {
 	  	  return _tags.values();
 	  	}
 	  }
 
 	  public boolean hasTag(String name) {
 	  	if(_tags == null) {
 	  		return false;
 	  	} else {
 	      return _tags.get(name) != null;
 	  	}
 	  }
 
 	  public boolean hasTags() {
 	  	if(_tags == null) {
 	  		return false;
 	  	} else {
 	      return _tags.size() > 0;
 	  	}
 	  }
 
 	  /********************
 	   * ORIGINAL ELEMENT *
 	   ********************/
 	  
 	  public E getOriginal() {
 	  	if(_parentLink == null) {
 	  		return _original;
 	  	} else {
 	  		throw new ChameleonProgrammerException("Invoking getOriginal() on real source element");
 	  	}
 	  }
 	  
 	  private E _original;
 	  
 	  /**********
 	   * PARENT *
 	   **********/
 	  
 	  // WORKING AROUND LACK OF MULTIPLE INHERITANCE
 	  
 	  // THESE VARIABLES MUST NOT BE USED BOTH
 	  //
 	  // IF _parentLink IS NULL, THE ELEMENT IS NOT LEXICAL,
 	  // IN WHICH CASE _parent PROVIDES THE UNIDIRECTIONAL ASSOCIATION
 	  // WITH THE PARENT. IN THAT CASE, _original IS SET TO THE ELEMENT
 	  // OF WHICH THIS ELEMENT IS A DERIVED ELEMENT
 	  private Reference<E,P> _parentLink = new Reference<E,P>((E) this);
 
 	  /**
 	   * This is the undirectional association with the parent in case this element is derived.
 	   */
 	  private P _parent;
 	  
 	  /**
 	   * Return the bidirectional link to the parent in case the element IS NOT derived.
 	   * DO NOT USE THIS TO OBTAIN THE PARENT
 	   * 
 	   * @throws ChameleonProgrammerException
 	   *    The method is invoked on a derived element. 
 	   */
 	  public final Reference<E,P> parentLink() {
 	  	if(_parentLink != null) {
 	      return _parentLink;
 	  	} else {
 	  		throw new ChameleonProgrammerException("Invoking getParentLink() on automatic derivation");
 	  	}
 	  }
 	  
 	  /**
 	   * Return the parent of this element
 	   */
 	  public final P parent() {
 	  	if(_parentLink != null) {
 	      return _parentLink.getOtherEnd();
 	  	} else {
 	  		return _parent;
 	  	}
 	  }
 	  
 	  /**
 	   * Check if this element is derived or not.
 	   * 
 	   * @return True if this element is derived, false otherwise.
 	   */
 	  public boolean isDerived() {
 	  	return _parent != null;
 	  }
 	  
 	  public void disconnect() {
 	  	nonRecursiveDisconnect();
 	  	disconnectChildren();
 	  }
 	  
 	  public boolean disconnected() {
 	  	return parent() == null;
 	  }
 	  
 	  public void nonRecursiveDisconnect() {
 	  	if(_parentLink != null) {
 	  		_parentLink.connectTo(null);
 	  	} else {
 	  		_parent = null;
 	  	}
 	  }
 	  
 	  public final void setUniParent(P parent) {
 	  	if(_parentLink != null) {
 	  		_parentLink.connectTo(null);
 	  	}
 	  	_parentLink = null;
 	  	_parent = parent;
 	  }
 	  
     public final List<Element> descendants() {
         return descendants(Element.class);
     }
 
     public final <T extends Element> List<T> descendants(Class<T> c) {
     	List<Element> tmp = (List<Element>) children();
     	new TypePredicate<Element,T>(c).filter(tmp);
       List<T> result = (List<T>)tmp;
       for (Element e : children()) {
         result.addAll(e.descendants(c));
       }
       return result;
     }
     
     public final List<Element> descendants(Predicate<Element> predicate) throws Exception {
     	// Do not compute all descendants, and apply predicate afterwards.
     	// That is way too expensive.
     	List<? extends Element> tmp = children();
     	predicate.filter(tmp);
       List<Element> result = (List<Element>)tmp;
       for (Element e : children()) {
         result.addAll(e.descendants(predicate));
       }
       return result;
     }
 
     public final List<Element> descendants(SafePredicate<Element> predicate) {
     	// Do not compute all descendants, and apply predicate afterwards.
     	// That is way too expensive.
     	List<? extends Element> tmp = children();
     	predicate.filter(tmp);
       List<Element> result = (List<Element>)tmp;
       for (Element e : children()) {
         result.addAll(e.descendants(predicate));
       }
       return result;
     }
 
     public final <X extends Exception> List<Element> descendants(UnsafePredicate<Element,X> predicate) throws X {
     	// Do not compute all descendants, and apply predicate afterwards.
     	// That is way too expensive.
     	List<? extends Element> tmp = children();
     	predicate.filter(tmp);
       List<Element> result = (List<Element>)tmp;
       for (Element<?,?> e : children()) {
         result.addAll(e.descendants(predicate));
       }
       return result;
     }
 
     public final <T extends Element> List<T> descendants(Class<T> c, Predicate<T> predicate) throws Exception {
     	List<Element> tmp = (List<Element>) children();
     	new TypePredicate<Element,T>(c).filter(tmp);
       List<T> result = (List<T>)tmp;
       predicate.filter(result);
       for (Element e : children()) {
         result.addAll(e.descendants(c, predicate));
       }
       return result;
     }
 
     public final <T extends Element> List<T> descendants(Class<T> c, SafePredicate<T> predicate) {
     	List<Element> tmp = (List<Element>) children();
     	new TypePredicate<Element,T>(c).filter(tmp);
       List<T> result = (List<T>)tmp;
       predicate.filter(result);
       for (Element e : children()) {
         result.addAll(e.descendants(c, predicate));
       }
       return result;
     }
     
     public final <T extends Element, X extends Exception> List<T> descendants(Class<T> c, UnsafePredicate<T,X> predicate) throws X {
     	List<Element> tmp = (List<Element>) children();
     	new TypePredicate<Element,T>(c).filter(tmp);
       List<T> result = (List<T>)tmp;
       predicate.filter(result);
       for (Element<?,?> e : children()) {
         result.addAll(e.descendants(c, predicate));
       }
       return result;
     }
 
 
     public final List<Element> ancestors() {
         if (parent()!=null) {
             List<Element> result = parent().ancestors();
             result.add(0, parent());
             return result;
         } else {
             return new ArrayList<Element>();
         }
     }
 
     public <T extends Element> T nearestElement(Class<T> c) {
     	Element el = this;
     	while ((el != null) && (! c.isInstance(el))){
     		el = el.parent();
     	}
     	return (T) el;
     }
 
     public <T extends Element> T nearestElement(Class<T> c, Predicate<T> predicate) throws Exception {
     	Element el = this;
     	while ((el != null) && (! (c.isInstance(el) && predicate.eval((T)el)))) {
     		el = el.parent();
     	}
     	return (T) el;
     }
 
     public <T extends Element> T nearestElement(Class<T> c, SafePredicate<T> predicate) {
     	Element el = this;
     	while ((el != null) && (! (c.isInstance(el) && predicate.eval((T)el)))) {
     		el = el.parent();
     	}
     	return (T) el;
     }
 
     public <T extends Element, X extends Exception> T nearestElement(Class<T> c, UnsafePredicate<T,X> predicate) throws X {
     	Element el = this;
     	while ((el != null) && (! (c.isInstance(el) && predicate.eval((T)el)))) {
     		el = el.parent();
     	}
     	return (T) el;
     }
 
     public <T extends Element> T nearestAncestor(Class<T> c) {
     	Element el = parent();
     	while ((el != null) && (! c.isInstance(el))){
     		el = el.parent();
     	}
     	return (T) el;
     }
     
     public <T extends Element> T nearestAncestor(Class<T> c, Predicate<T> predicate) throws Exception {
     	Element el = parent();
     	while ((el != null) && (! (c.isInstance(el) && predicate.eval((T)el)))) {
     		el = el.parent();
     	}
     	return (T) el;
     }
 
     public <T extends Element> T nearestAncestor(Class<T> c, SafePredicate<T> predicate) {
     	Element el = parent();
     	while ((el != null) && (! (c.isInstance(el) && predicate.eval((T)el)))) {
     		el = el.parent();
     	}
     	return (T) el;
     }
 
     public <T extends Element, X extends Exception> T nearestAncestor(Class<T> c, UnsafePredicate<T,X> predicate) throws X {
     	Element el = parent();
     	while ((el != null) && (! (c.isInstance(el) && predicate.eval((T)el)))) {
     		el = el.parent();
     	}
     	return (T) el;
     }
 
     public abstract E clone();
     
     public Language language() {
     	Language result = null;
     	if(Config.CACHE_LANGUAGE == true) {
     		result = _languageCache;
     	}
     	if(result == null) {
     		P parent = parent();
     		if(parent != null) {
     			result = parent().language();
     			if(Config.CACHE_LANGUAGE == true) {
     			  _languageCache = result;
     			}
     		} 
     	}
       return result;
     }
     
     public <T extends Language> T language(Class<T> kind) {
     	if(kind == null) {
     		throw new ChameleonProgrammerException("The given language class is null.");
     	}
     	Language language = language();
     	if(kind.isInstance(language) || language == null) {
     		return (T) language;
     	} else {
     		throw new WrongLanguageException("The language of this model is of the wrong kind. Expected: "+kind.getName()+" but got: " +language.getClass().getName());
     	}
     }
     
     private Language _languageCache;
     
     /**
      * @see Element#lexicalLookupStrategy(Element) 
      */
     public LookupStrategy lexicalLookupStrategy(Element child) throws LookupException {
     	P parent = parent();
     	if(parent != null) {
         return parent.lexicalLookupStrategy(this);
     	} else {
     		throw new LookupException("Going to the parent context when there is no parent.");
     	}
     }
 
     /**
      * @see Element#lexicalLookupStrategy() 
      */
     public final LookupStrategy lexicalLookupStrategy() throws LookupException {
     	try {
         return parent().lexicalLookupStrategy(this);
     	} catch(NullPointerException exc) {
     		if(parent() == null) {
     			throw new LookupException("Requesting the lexical context of an element without a parent: " +getClass().getName());
     		} else {
     			throw exc;
     		}
     	}
     }
     
     public PropertySet<Element> properties() {
     	PropertySet<Element> result = declaredProperties();
     	result.addAll(defaultProperties());
     	return result;
     }
     
     public PropertySet<Element> defaultProperties() {
     	return language().defaultProperties(this);
     }
     
     public PropertySet<Element> declaredProperties() {
     	return new PropertySet<Element>();
     }
     
 
     public Ternary is(Property<Element> property) {
     	// First get the declared properties.
       PropertySet<Element> properties = properties();
       // Add the given property if it dynamically applies to this element.
       Ternary applies = property.appliesTo(this);
       if(applies == Ternary.TRUE) {
         properties.add(property);
       } else if(applies == Ternary.FALSE) {
       	properties.add(property.inverse());
       }
       // Check if the resulting property set implies the given property.
       return properties.implies(property);
     }
    
     public Property<Element> property(PropertyMutex<Element> mutex) throws MetamodelException {
     	List<Property<Element>> properties = new ArrayList<Property<Element>>();
     	for(Property<Element> p : properties().properties()) {
     		if(p.mutex() == mutex) {
     			properties.add(p);
     		}
     	}
     	if(properties.size() == 1) {
     		return properties.get(0);
     	} else {
     		throw new MetamodelException("Element has "+properties.size()+" properties for the mutex "+mutex);
     	}
     }
 
     /*@
       @ public behavior
       @
       @ post (\forall Property p; \result.contains(p); base.contains(p) && ! overriding.contradicts(p) ||
       @        overriding.contains(p) && (\exists Property p2; base.contains(p2); overriding.contradicts(p2))); 
       @*/
 		protected PropertySet<Element> filterProperties(PropertySet<Element> overriding, PropertySet<Element> base) {
 			Set<Property<Element>> baseProperties = base.properties();
 			final Set<Property<Element>> overridingProperties = overriding.properties();
 		  new SafePredicate<Property<Element>>() {
 				@Override
 				public boolean eval(final Property<Element> aliasedProperty) {
 					return new SafePredicate<Property<Element>>() {
 						@Override
 						public boolean eval(Property<Element> myProperty) {
 							return !aliasedProperty.contradicts(myProperty);
 						}
 					}.forAll(overridingProperties);
 				}
 		  	
 		  }.filter(baseProperties);
 		  baseProperties.addAll(overridingProperties);
 		  return new PropertySet<Element>(baseProperties);
 		}
 
     public boolean isValid() {
     	return true;
     }
     
     public void disconnectChildren() {
     	for(Element child:children()) {
     		child.disconnect();
     	}
     }
     
     private static int stackTraceCount = 0;
     
     /**
      * This debugging method throws an exception, catches it, and prints
      * the stacktrace.
      */
     protected void showStackTrace(String message) {
     	try {
     		throw new Exception(++stackTraceCount + ":: "+message);
     	} catch(Exception e) {
     		e.printStackTrace();
     	}
     }
 
     /**
      * This debugging method throws an exception, catches it, and prints
      * the stacktrace.
      */
     protected void showStackTrace() {
     	showStackTrace(null);
     }
 		
 //    public Iterator<Element> depthFirstIterator() {
 //    	return new Iterator<Element>() {
 //
 //    		
 //    		
 //    		private Element _current;
 //    		
 //    		private Iterator<Element> _currentIterator;
 //    		
 //    		private Element _last;
 //				public boolean hasNext() {
 //					return _current != _last;
 //				}
 //
 //				public Element next() {
 //					compile error
 //				}
 //
 //				public void remove() {
 //					_current.parentLink().connectTo(null);
 //				}
 //			};
 //    }
 
 }
