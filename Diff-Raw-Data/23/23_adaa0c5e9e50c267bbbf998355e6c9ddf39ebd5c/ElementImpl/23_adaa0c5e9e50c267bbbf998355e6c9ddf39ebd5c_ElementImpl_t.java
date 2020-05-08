 package chameleon.core.element;
 
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.rejuse.association.AbstractMultiAssociation;
 import org.rejuse.association.Association;
 import org.rejuse.association.OrderedMultiAssociation;
 import org.rejuse.association.SingleAssociation;
 import org.rejuse.logic.ternary.Ternary;
 import org.rejuse.predicate.Predicate;
 import org.rejuse.predicate.SafePredicate;
 import org.rejuse.predicate.TypePredicate;
 import org.rejuse.predicate.UnsafePredicate;
 import org.rejuse.property.Conflict;
 import org.rejuse.property.PropertyMutex;
 import org.rejuse.property.PropertySet;
 
 import chameleon.core.Config;
 import chameleon.core.language.Language;
 import chameleon.core.language.WrongLanguageException;
 import chameleon.core.lookup.LookupException;
 import chameleon.core.lookup.LookupStrategy;
 import chameleon.core.namespace.Namespace;
 import chameleon.core.namespacedeclaration.NamespaceDeclaration;
 import chameleon.core.property.ChameleonProperty;
 import chameleon.core.tag.Metadata;
 import chameleon.core.validation.BasicProblem;
 import chameleon.core.validation.Valid;
 import chameleon.core.validation.VerificationResult;
 import chameleon.exception.ChameleonProgrammerException;
 import chameleon.exception.ModelException;
 import chameleon.util.association.ChameleonAssociation;
 import chameleon.util.association.Single;
 import chameleon.util.concurrent.Action;
 import chameleon.util.concurrent.SafeAction;
 import chameleon.util.concurrent.UnsafeAction;
 
 /**
  * @author Marko van Dooren
  * 
  * @opt operations
  * @opt attributes
  * @opt visibility
  * @opt types
  */
 public abstract class ElementImpl implements Element {
 
 	private static Map<Class,List<String>> _excludedFieldNames = new HashMap<Class,List<String>>();
 
 	public ElementImpl() {
 		//	  	_parentLink.addListener(new AssociationListener<P>() {
 		//
 		//				public void notifyElementAdded(P element) {
 		//					notifyParent(ElementImpl.this);
 		//				}
 		//
 		//				public void notifyElementRemoved(P element) {
 		//					notifyParent(ElementImpl.this);
 		//				}
 		//
 		//				public void notifyElementReplaced(P oldElement, P newElement) {
 		//					notifyParent(ElementImpl.this);
 		//				}
 		//	  		
 		//	  	});
 	}
 
 	/********
 	 * TAGS *
 	 ********/
 
 	// initialization of this Map is done lazily.
 	private Map<String, Metadata> _tags;
 
 	public Metadata metadata(String name) {
 		if(_tags != null) {
 			return _tags.get(name);
 		} else {
 			//lazy init has not been performed yet.
 			return null;
 		}
 	}
 
 	public void removeMetadata(String name) {
 		if(_tags != null) {
 			Metadata old = _tags.get(name);
 			_tags.remove(name);
 			if((old != null) && (old.getElement() == this)){
 				old.setElement(null,name);
 			}
 		}
 	}
 
 	public void removeAllMetadata() {
 		if(_tags != null) {
 			List<String> keySet = new ArrayList<String>(_tags.keySet());
 			for(String tagName: keySet) {
 				removeMetadata(tagName);
 			}
 		}
 	}
 
 	public void setMetadata(Metadata decorator, String name) {
 		//Lazy init of hashmap
 		if (_tags==null) {
 			_tags = new HashMap<String, Metadata>();
 		}
 		Metadata old = _tags.get(name); 
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
 
 	public Collection<Metadata> metadata() {
 		if(_tags == null) {
 			return new ArrayList();
 		} else {
 			return _tags.values();
 		}
 	}
 
 	public boolean hasMetadata(String name) {
 		if(_tags == null) {
 			return false;
 		} else {
 			return _tags.get(name) != null;
 		}
 	}
 
 	public boolean hasMetadata() {
 		if(_tags == null) {
 			return false;
 		} else {
 			return _tags.size() > 0;
 		}
 	}
 	
 //	protected abstract Element cloneSelf();
 	
 	public abstract Element clone();
 	
 //	public final Element clone() {
 //		Element result = cloneSelf();
 //		Iterator<Association<Element, ? extends Element>> mine = associations().iterator();
 //		Iterator<Association<Element, ? extends Element>> others = result.associations().iterator();
 //		while(mine.hasNext()) {
 //			Association<Element, ? extends Element> m = mine.next();
 //			Association<Element, ? extends Element> o = others.next();
 //			for(Element myElement: m.getOtherEnds()) {
 //				Element clone = myElement.clone();
 //				o. hmmm
 //			}
 //		}
 //		return result;
 //	}
 
 	/**********
 	 * PARENT *
 	 **********/
 
 	// WORKING AROUND LACK OF MULTIPLE INHERITANCE
 
 	// THESE VARIABLES MUST NOT BE USED AT THE SAME TIME
 	//
 	// IF _parentLink IS NULL, THE ELEMENT IS NOT LEXICALLY IN THE MODEL,
 	// IN WHICH CASE _parent PROVIDES THE UNIDIRECTIONAL ASSOCIATION
 	// WITH THE PARENT. IN THAT CASE, THE ORIGIN IS SET TO THE ELEMENT
 	// OF WHICH THIS ELEMENT IS A DERIVED ELEMENT
 	private Single<Element> _parentLink = createParentLink();//new SingleAssociation<E,P>((E) this);
 
 	/**
 	 * This is the undirectional association with the parent in case this element is derived.
 	 */
 	private Element _parent;
 
 	/**
 	 * Return the bidirectional link to the parent in case the element IS NOT derived.
 	 * DO NOT USE THIS TO OBTAIN THE PARENT
 	 * 
 	 * @throws ChameleonProgrammerException
 	 *    The method is invoked on a derived element. 
 	 */
 	public final SingleAssociation<Element,Element> parentLink() {
 		if(_parentLink != null) {
 			return _parentLink;
 		} else {
 			throw new ChameleonProgrammerException("Invoking getParentLink() on automatic derivation");
 		}
 	}
 
 	protected Single<Element> createParentLink() {
 		return new Single<Element>(this);
 	}
 
 	/**
 	 * Return the parent of this element
 	 */
 	public final Element parent() {
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
 
 	/**
 	 * The default behavior is to return 'this'.
 	 */
 	public Element origin() {
 		return _origin;
 	}
 
 	public Element farthestOrigin() {
 		Element element = this;
 		Element origin = origin();
 		while(element != origin) {
 			element = origin;
 			origin = element.origin();
 		}
 		return origin;
 	}
 
 	public Element rootOrigin() {
 		Element current = this;
 		Element origin = origin();
 		while(current != origin) {
 			current = origin;
 			origin = current.origin();
 		}
 		return current;
 	}
 
 	public void setOrigin(Element origin) {
 		_origin = origin;
 	}
 
 	private Element _origin = this;
 
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
 
 	/**
 	 * DO NOT OVERRIDE THIS METHOD UNLESS YOU *REALLY* KNOW WHAT YOU ARE DOING! We don't see
 	 * any use other than diagnostic purposes.
 	 */
 	public void setUniParent(Element parent) {
 		if(_parentLink != null) {
 			_parentLink.connectTo(null);
 		}
 		if(parent != null) {
 			_parentLink = null;
 		} else if(_parentLink == null) {
 			_parentLink = createParentLink();
 		}
 		_parent = parent;
 	}
 
 	public final List<Element> descendants() {
 		return descendants(Element.class);
 	}
 
 	public static void excludeFieldName(Class<? extends Element> type, String fieldName) {
 		List<String> list = _excludedFieldNames.get(type);
 		if(list == null) {
 			list = new ArrayList<String>();
 			_excludedFieldNames.put(type, list);
 		}
 		list.add(fieldName);
 	}
 
 	static {
 		excludeFieldName(ElementImpl.class,"_parentLink");
 	}
 	private static List<String> excludedFieldNames(Class<? extends Element> type) {
 		return _excludedFieldNames.get(type);
 	}
 
 	public List<Element> reflectiveChildren() {
 		List<Element> reflchildren = new ArrayList<Element>();
 		for (ChameleonAssociation association : associations()) {
 			reflchildren.addAll(association.getOtherEnds());
 		}
 		return reflchildren;
 	}
 
 	private static Map<Class, List<Field>> _fieldMap = new HashMap<Class, List<Field>>();
 
 	public static List<Field> getAllFieldsTillClass(Class currentClass){
 		List<Field> result = _fieldMap.get(currentClass);
 		if(result == null) {
 			result = new ArrayList<Field>();
 			addAllFieldsTillClass(currentClass, result);
 			_fieldMap.put(currentClass, result);
 		}
 		return result;
 	}
 
 
 	private List<ChameleonAssociation<?>> _associations;
 
 	public synchronized List<ChameleonAssociation<?>> associations() {
 		if(_associations == null) {
 			_associations = new ArrayList<ChameleonAssociation<?>>();
 			Class<? extends Element>  currentClass = getClass();
 			List<Field> fields = getAllFieldsTillClass(currentClass);
 			for (Field field : fields) {
 				Object content = getFieldValue(field);
 				if(content instanceof ChameleonAssociation){
 					_associations.add((ChameleonAssociation<?>) content);
 				}
 			}
 			((ArrayList)_associations).trimToSize();
 		}
 		return _associations;
 	}
 
 	private static void addAllFieldsTillClass(final Class currentClass, Collection<Field> accumulator){
 		List<Field> fieldList = new ArrayList<Field>(Arrays.asList(currentClass.getDeclaredFields()));
 		new SafePredicate<Field>() {
 
 			@Override
 			public boolean eval(Field field) {
 				final String fieldName = field.getName();
 				return new SafePredicate<String>() {
 
 					@Override
 					public boolean eval(String excludedField) {
 						return !excludedField.equals(fieldName);
 					}
 
 				}.forall(_excludedFieldNames.get(currentClass));
 			}
 		}.filter(fieldList);
 		accumulator.addAll(fieldList);
 		if(currentClass != ElementImpl.class) {
 			Class superClass = currentClass.getSuperclass();
 			accumulator.addAll(getAllFieldsTillClass(superClass));
 		}
 	}
 
 	public Object getFieldValue(Field field){
 		try {
 			field.setAccessible(true);
 			return field.get(this);
 		} catch (IllegalArgumentException e) {
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {	
 			e.printStackTrace();
 		}
 		return null;
 	}
 
   public final List<? extends Element> children() {
   	return reflectiveChildren();
   }
 
 	public final <T extends Element> List<T> children(Class<T> c) {
 		List<Element> tmp = (List<Element>) childrenAux();
 		new TypePredicate<Element,T>(c).filter(tmp);
 		return (List<T>)tmp;
 	}
 
 	public final <T extends Element> List<T> descendants(Class<T> c) {
 		List<T> result = children(c);
 		for (Element e : childrenAux()) {
 			result.addAll(e.descendants(c));
 		}
 		return result;
 	}
 
 	//      	List<Element> tmp = (List<Element>) reflectiveChildren();
 	//      	new TypePredicate<Element,T>(c).filter(tmp);
 	//        List<T> result = (List<T>)tmp;
 	//        for (Element e : reflectiveChildren()) {
 
 
 	public final <T extends Element> boolean hasDescendant(Class<T> c) {
 		List<Element> tmp = (List<Element>) childrenAux();
 		new TypePredicate<Element,T>(c).filter(tmp);
 
 		if (!tmp.isEmpty())
 			return true;
 
 		for (Element e : childrenAux()) {
 			if (e.hasDescendant(c))
 				return true;
 		}
 
 		return false;
 	}
 
 	public final <T extends Element> boolean hasDescendant(Class<T> c, SafePredicate<T> predicate) {
 		List<Element> tmp = (List<Element>) childrenAux();
 		new TypePredicate<Element,T>(c).filter(tmp);
 		List<T> result = (List<T>)tmp;
 		predicate.filter(result);
 
 		if (!result.isEmpty())
 			return true;
 
 		for (Element e : childrenAux()) {
 			if (e.hasDescendant(c, predicate))
 				return true;
 		}
 
 		return false;
 	}
 
 	public final <T extends Element> List<T> nearestDescendants(Class<T> c) {
 		List<? extends Element> tmp = childrenAux();
 		List<T> result = new ArrayList<T>();
 		Iterator<? extends Element> iter = tmp.iterator();
 		while(iter.hasNext()) {
 			Element e = iter.next();
 			if(c.isInstance(e)) {
 				result.add((T)e);
 				iter.remove();
 			}
 		}
 		for (Element e : tmp) {
 			result.addAll(e.nearestDescendants(c));
 		}
 		return result;
 	}
 
 	public final List<Element> children(Predicate<Element> predicate) throws Exception {
 		List<? extends Element> tmp = childrenAux();
 		predicate.filter(tmp);
 		return (List<Element>)tmp;
 	}
 
 	public final List<Element> descendants(Predicate<Element> predicate) throws Exception {
 		// Do not compute all descendants, and apply predicate afterwards.
 		// That is way too expensive.
 		List<? extends Element> tmp = childrenAux();
 		predicate.filter(tmp);
 		List<Element> result = (List<Element>)tmp;
 		for (Element e : childrenAux()) {
 			result.addAll(e.descendants(predicate));
 		}
 		return result;
 	}
 
 	public final List<Element> children(SafePredicate<Element> predicate) {
 		List<? extends Element> tmp = childrenAux();
 		predicate.filter(tmp);
 		return (List<Element>)tmp;
 	}
 
 	public final List<Element> descendants(SafePredicate<Element> predicate) {
 		// Do not compute all descendants, and apply predicate afterwards.
 		// That is way too expensive.
 		List<? extends Element> tmp = childrenAux();
 		predicate.filter(tmp);
 		List<Element> result = (List<Element>)tmp;
 		for (Element e : childrenAux()) {
 			result.addAll(e.descendants(predicate));
 		}
 		return result;
 	}
 
 	public final <X extends Exception> List<Element> children(UnsafePredicate<Element,X> predicate) throws X {
 		List<? extends Element> tmp = childrenAux();
 		predicate.filter(tmp);
 		return (List<Element>)tmp;
 	}
 
 	public final <X extends Exception> List<Element> descendants(UnsafePredicate<Element,X> predicate) throws X {
 		// Do not compute all descendants, and apply predicate afterwards.
 		// That is way too expensive.
 		List<? extends Element> tmp = childrenAux();
 		predicate.filter(tmp);
 		List<Element> result = (List<Element>)tmp;
 		for (Element e : childrenAux()) {
 			result.addAll(e.descendants(predicate));
 		}
 		return result;
 	}
 
 	public final <T extends Element> List<T> children(Class<T> c, Predicate<T> predicate) throws Exception {
 		List<T> result = children(c);
 		predicate.filter(result);
 		return result;
 	}
 
 	public final <T extends Element> List<T> children(Class<T> c, final ChameleonProperty property) {
 		List<T> result = children(c, new SafePredicate<T>() {
 			public boolean eval(T element) {
 				return element.isTrue(property);
 			}
 		});
 		return result;
 	}
 
 	public final <T extends Element> List<T> children(Class<T> c, SafePredicate<T> predicate) {
 		List<T> result = children(c);
 		predicate.filter(result);
 		return result;
 	}
 
 	public final <T extends Element, X extends Exception> List<T> children(Class<T> c, UnsafePredicate<T,X> predicate) throws X {
 		List<T> result = children(c);
 		predicate.filter(result);
 		return result;
 	}
 
 	public final <T extends Element> List<T> descendants(Class<T> c, ChameleonProperty property) {
 		List<T> result = children(c, property);
 		for (Element e : childrenAux()) {
 			result.addAll(e.descendants(c, property));
 		}
 		return result;
 	}
 
 	private List<? extends Element> childrenAux() {
 		return reflectiveChildren();
 	}
 
 	public final <T extends Element> List<T> descendants(Class<T> c, Predicate<T> predicate) throws Exception {
 		List<Element> tmp = (List<Element>) childrenAux();
 		new TypePredicate<Element,T>(c).filter(tmp);
 		List<T> result = (List<T>)tmp;
 		predicate.filter(result);
 		for (Element e : childrenAux()) {
 			result.addAll(e.descendants(c, predicate));
 		}
 		return result;
 	}
 
 	public final <T extends Element> List<T> descendants(Class<T> c, SafePredicate<T> predicate) {
 		List<Element> tmp = (List<Element>) childrenAux();
 		new TypePredicate<Element,T>(c).filter(tmp);
 		List<T> result = (List<T>)tmp;
 		predicate.filter(result);
 		for (Element e : childrenAux()) {
 			result.addAll(e.descendants(c, predicate));
 		}
 		return result;
 	}
 
 	public final <T extends Element, X extends Exception> List<T> descendants(Class<T> c, UnsafePredicate<T,X> predicate) throws X {
 		List<Element> tmp = (List<Element>) childrenAux();
 		new TypePredicate<Element,T>(c).filter(tmp);
 		List<T> result = (List<T>)tmp;
 		predicate.filter(result);
 		for (Element e : childrenAux()) {
 			result.addAll(e.descendants(c, predicate));
 		}
 		return result;
 	}
 
 	public final <T extends Element> void apply(Class<T> c, Action<T> action) throws Exception {
 		if(c.isInstance(this)) {
 			action.perform((T)this);
 		}
 		for (Element e : childrenAux()) {
 			e.apply(c, action);
 		}
 	}
 
 	public final <T extends Element> void apply(Class<T> c, SafeAction<T> action) {
 		if(c.isInstance(this)) {
 			action.perform((T)this);
 		}
 		for (Element e : childrenAux()) {
 			e.apply(c, action);
 		}
 	}
 
 	public final <T extends Element, X extends Exception> void apply(Class<T> c, UnsafeAction<T,X> action) throws X {
 		if(c.isInstance(this)) {
 			action.perform((T)this);
 		}
 		for (Element e : childrenAux()) {
 			e.apply(c, action);
 		}
 	}
 
 	public final <T extends Element> List<T> ancestors(Class<T> c) {
 		List<T> result = new ArrayList<T>();
 		T el = nearestAncestor(c);
 		while (el != null){
 			result.add(el);
 			el = el.nearestAncestor(c);
 		}
 		return result;
 	}
 
 	public <T extends Element> List<T> ancestors(Class<T> c, SafePredicate<T> predicate) {
 		List<T> result = ancestors(c);
 		predicate.filter(result);
 		return result;
 	}
 
 	@Override
 	public <T extends Element, X extends Exception> List<T> ancestors(Class<T> c, UnsafePredicate<T, X> predicate) throws X {
 		List<T> result = ancestors(c);
 		predicate.filter(result);
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
 
 	public <T extends Element> T nearestAncestorOrSelf(Class<T> c) {
 		Element el = this;
 		while ((el != null) && (! c.isInstance(el))){
 			el = el.parent();
 		}
 		return (T) el;
 	}
 
 	public <T extends Element> T nearestAncestorOrSelf(Class<T> c, Predicate<T> predicate) throws Exception {
 		Element el = this;
 		while ((el != null) && (! (c.isInstance(el) && predicate.eval((T)el)))) {
 			el = el.parent();
 		}
 		return (T) el;
 	}
 
 	public <T extends Element> T nearestAncestorOrSelf(Class<T> c, SafePredicate<T> predicate) {
 		Element el = this;
 		while ((el != null) && (! (c.isInstance(el) && predicate.eval((T)el)))) {
 			el = el.parent();
 		}
 		return (T) el;
 	}
 
 	public <T extends Element, X extends Exception> T nearestAncestorOrSelf(Class<T> c, UnsafePredicate<T,X> predicate) throws X {
 		Element el = this;
 		while ((el != null) && (! (c.isInstance(el) && predicate.eval((T)el)))) {
 			el = el.parent();
 		}
 		return (T) el;
 	}
 
 	public Element farthestAncestor() {
 		Element parent = parent();
 		if(parent == null) {
 			return this;
 		} else {
 			return parent.farthestAncestor();
 		}
 	}
 
 	public <T extends Element> T farthestAncestor(Class<T> c) {
 		Element el = parent();
 		T anc = null;
 		while(el != null) {
 			while ((el != null) && (! c.isInstance(el))){
 				el = el.parent();
 			}
 			if(el != null) {
 				anc = (T) el;
 				el = el.parent();
 			}
 		}
 		return anc;
 	}
 	
 	public <T extends Element> T farthestAncestor(Class<T> c, SafePredicate<T> p) {
 		Element el = parent();
 		T anc = null;
 		while(el != null) {
 			while ((el != null) && ((! c.isInstance(el)) || !p.eval((T)el)) ){
 				el = el.parent();
 			}
 			if(el != null) {
 				anc = (T) el;
 				el = el.parent();
 			}
 		}
 		return anc;
 	}
 
 	public <T extends Element> T farthestAncestorOrSelf(Class<T> c) {
 		T result = farthestAncestor(c);
 		if((result == null) && (c.isInstance(this))) {
 			result = (T) this;
 		}
 		return result;
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
 
 //	public abstract Element clone();
 
 	public Language language() {
 		Language result = null;
 		if(Config.cacheLanguage() == true) {
 			result = _languageCache;
 		}
 		if(result == null) {
 			Element parent = parent();
 			if(parent != null) {
 				result = parent.language();
 				if(Config.cacheLanguage() == true) {
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
 		return lexicalLookupStrategy();
 	}
 
 	/**
 	 * @see Element#lexicalLookupStrategy() 
 	 */
 	 public LookupStrategy lexicalLookupStrategy() throws LookupException {
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
 
 	 public PropertySet<Element,ChameleonProperty> properties() {
 		 PropertySet<Element,ChameleonProperty> result = declaredProperties();
 		 result.addAll(defaultProperties());
 		 return result;
 	 }
 
 	 public PropertySet<Element,ChameleonProperty> defaultProperties() {
 		 return language().defaultProperties(this);
 	 }
 
 	 public PropertySet<Element,ChameleonProperty> declaredProperties() {
 		 return new PropertySet<Element,ChameleonProperty>();
 	 }
 
 	 public boolean isTrue(ChameleonProperty property) {
 		 return is(property) == Ternary.TRUE;
 	 }
 
 	 public boolean isFalse(ChameleonProperty property) {
 		 return is(property) == Ternary.FALSE;
 	 }
 
 	 public boolean isUnknown(ChameleonProperty property) {
 		 return is(property) == Ternary.UNKNOWN;
 	 }
 
 	 public synchronized Ternary is(ChameleonProperty property) {
 		 Ternary result = null;
 		 if(Config.cacheElementProperties()) {
 			 if(_propertyCache == null) {
 				 _propertyCache = new HashMap<ChameleonProperty,Ternary>();
 			 } else {
 				 result = _propertyCache.get(property);
 			 }
 		 }
 		 if(result == null){
 			 result = property.appliesTo(this);
 			 if(result == Ternary.UNKNOWN) {
 				 // First get the declared properties.
 				 PropertySet<Element,ChameleonProperty> properties = properties();
 				 // Add the given property if it dynamically applies to this element.
 				 Ternary applies = property.appliesTo(this);
 				 if(applies == Ternary.TRUE) {
 					 properties.add(property);
 				 } else if(applies == Ternary.FALSE) {
 					 properties.add(property.inverse());
 				 }
 				 // Check if the resulting property set implies the given property.
 				 result = properties.implies(property);
 				 if(Config.cacheElementProperties()) {
 					 _propertyCache.put(property, result);
 				 }
 			 }
 		 }
 		 return result;
 	 }
 
 	 private HashMap<ChameleonProperty,Ternary> _propertyCache;
 
 	 public ChameleonProperty property(final PropertyMutex<ChameleonProperty> mutex) throws ModelException {
 		 return property(new UnsafePredicate<ChameleonProperty,ModelException>() {
 			@Override
 			public boolean eval(ChameleonProperty property) throws ModelException
				 {return mutex != null && mutex.equals(property.mutex());}
 		 });
 	 }
 	 
 	 public ChameleonProperty implyingProperty(final ChameleonProperty implied) throws ModelException {
 		 return property(new UnsafePredicate<ChameleonProperty,ModelException>() {
 			@Override
 			public boolean eval(ChameleonProperty property) throws ModelException
 				 {return property.implies(implied);}
 		 });
 	 }
 	 
 	 public ChameleonProperty property(SafePredicate<ChameleonProperty> predicate) throws ModelException {
 		 List<ChameleonProperty> properties = new ArrayList<ChameleonProperty>();
 		 for(ChameleonProperty p : properties().properties()) {
 			 if(predicate.eval(p)) {
 				 properties.add(p);
 			 }
 		 }
 		 if(properties.size() == 1) {
 			 return properties.get(0);
 		 } else {
 			 throw new ModelException("Element of type " +getClass().getName()+ " has "+properties.size()+" properties that satisfy the given condition.");
 		 }
 	 }
 
 	 public <X extends Exception> ChameleonProperty property(UnsafePredicate<ChameleonProperty,X> predicate) throws ModelException, X {
 		 List<ChameleonProperty> properties = new ArrayList<ChameleonProperty>();
 		 for(ChameleonProperty p : properties().properties()) {
			 if(p == null) {
				 System.out.println("debug");
			 }
 			 if(predicate.eval(p)) {
 				 properties.add(p);
 			 }
 		 }
 		 if(properties.size() == 1) {
 			 return properties.get(0);
 		 } else {
 			 throw new ModelException("Element of type " +getClass().getName()+ " has "+properties.size()+" properties that satisfy the given condition.");
 		 }
 	 }
 
 
 	 public boolean hasProperty(PropertyMutex<ChameleonProperty> mutex) throws ModelException {
 		 return properties().hasPropertyFor(mutex);
 	 }
 
 	 /*@
       @ public behavior
       @
       @ post (\forall Property p; \result.contains(p); base.contains(p) && ! overriding.contradicts(p) ||
       @        overriding.contains(p) && (\exists Property p2; base.contains(p2); overriding.contradicts(p2))); 
       @*/
 	 protected PropertySet<Element,ChameleonProperty> filterProperties(PropertySet<Element,ChameleonProperty> overriding, PropertySet<Element,ChameleonProperty> base) {
 		 Set<ChameleonProperty> baseProperties = base.properties();
 		 final Set<ChameleonProperty> overridingProperties = overriding.properties();
 		 new SafePredicate<ChameleonProperty>() {
 			 @Override
 			 public boolean eval(final ChameleonProperty aliasedProperty) {
 				 return new SafePredicate<ChameleonProperty>() {
 					 @Override
 					 public boolean eval(ChameleonProperty myProperty) {
 						 return !aliasedProperty.contradicts(myProperty);
 					 }
 				 }.forAll(overridingProperties);
 			 }
 
 		 }.filter(baseProperties);
 		 baseProperties.addAll(overridingProperties);
 		 return new PropertySet<Element,ChameleonProperty>(baseProperties);
 	 }
 
 	 public void disconnectChildren() {
 		 for(Element child:childrenAux()) {
 			 if(child != null) {
 				 child.disconnect();
 			 } else {
 				 throw new ChameleonProgrammerException("CHILD IS NULL for element of Type "+getClass().getName());
 				 //    			showStackTrace("CHILD IS NULL for element of Type "+getClass().getName());    		
 			 }
 		 }
 	 }
 
 	 //    private static int stackTraceCount = 0;
 	 //    
 	 //    /**
 	 //     * This debugging method throws an exception, catches it, and prints
 	 //     * the stacktrace.
 	 //     */
 	 //    protected void showStackTrace(String message) {
 	 //    	try {
 	 //    		throw new Exception(++stackTraceCount + ":: "+message);
 	 //    	} catch(Exception e) {
 	 //    		e.printStackTrace();
 	 //    	}
 	 //    }
 	 //
 	 //    /**
 	 //     * This debugging method throws an exception, catches it, and prints
 	 //     * the stacktrace.
 	 //     */
 	 //    protected void showStackTrace() {
 	 //    	showStackTrace(null);
 	 //    }
 
 	 public List<? extends Element> directDependencies() {
 		 return childrenAux();
 	 }
 
 	 /**
 	  * Notify this element that the given descendant was modified. This
 	  * method first calls reactOnDescendantChange with the given element. After that,
 	  * the event is propagated to the lexical parent, if the parent is not null.
 	  */
 	 public void notifyDescendantChanged(Element descendant) {
 		 reactOnDescendantChange(descendant);
 		 notifyParent(descendant);
 	 }
 
 	 private void notifyParent(Element descendant) {
 		 Element parent = parent();
 		 if(parent != null) {
 			 parent.notifyDescendantChanged(descendant);
 		 }
 	 }
 
 	 /**
 	  * Actually react on a change of the given descendant.
 	  * 
 	  * By default, there is no reaction.
 	  */
 	 public void reactOnDescendantChange(Element descendant) {
 	 }
 
 	 public void reactOnDescendantAdded(Element descendant) {
 	 }
 
 	 public void reactOnDescendantRemoved(Element descendant) {
 	 }
 
 	 public void reactOnDescendantReplaced(Element oldElement, Element newElement) {
 	 }
 
 	 public final VerificationResult verify() {
 		 VerificationResult result = verifySelf();
 		 if(result == null) {
 			 throw new ChameleonProgrammerException("The verifySelf method of "+getClass().getName()+" returned null.");
 		 }
 		 result = result.and(verifyProperties());
 		 result = result.and(verifyLoops());
 		 for(Element element:childrenAux()) {
 			 result = result.and(element.verify());
 		 }
 		 result = result.and(verifyAssociations());
 		 result = result.and(language().verify(this));
 		 return result;
 	 }
 	 
 	 protected VerificationResult verifyAssociations() {
 		 VerificationResult result = Valid.create();
 		 for(ChameleonAssociation<?> association: associations()) {
 			 result = result.and(association.verify());
 		 }
 		 return result;
 	 }
 
 	 public final VerificationResult verifyLoops() {
 		 VerificationResult result = Valid.create();
 		 Element e = parent();
 		 while(e != null) {
 			 if(e == this) {
 				 result = result.and(new BasicProblem(this, "There is a loop in the lexical structure. This element is an ancestor of itself."));
 			 }
 			 e = e.parent();
 		 }
 		 return result;
 	 }
 
 	 /**
 	  * Perform a local verification. The check for a loop in the lexical structure is already implemented
 	  * in verifyLoops(), which is used in verify(). The checks to verify that all properties of this element actually
 	  * apply to it and that there are no conflicting properties are both implemented in verifyProperties(), which is also used in verify().
 	  * @return
 	  */
 	 //    public abstract VerificationResult verifySelf();
 	 public VerificationResult verifySelf() {
 		 return Valid.create();
 	 }
 
 	 public final VerificationResult verifyProperties() {
 		 VerificationResult result = Valid.create();
 		 PropertySet<Element,ChameleonProperty> properties = properties();
 		 Collection<Conflict<ChameleonProperty>> conflicts = properties.conflicts();
 		 for(Conflict<ChameleonProperty> conflict: conflicts) {
 			 result = result.and(new ConflictProblem(this,conflict));
 		 }
 		 for(ChameleonProperty property: properties.properties()) {
 			 result = result.and(property.verify(this));
 		 }
 		 return result;
 	 }
 
 	 public static class ConflictProblem extends BasicProblem {
 
 		 private Conflict<ChameleonProperty> _conflict;
 
 		 public ConflictProblem(Element element, Conflict<ChameleonProperty> conflict) {
 			 super(element, "Property "+conflict.first().name()+" conflicts with property "+conflict.second().name());
 			 _conflict = conflict;
 		 }
 
 		 public Conflict<ChameleonProperty> conflict() {
 			 return _conflict;
 		 }
 
 	 }
 
 	 protected VerificationResult checkNull(Object element, String message, VerificationResult result) {
 		 if(element == null) {
 			 result = result.and(new BasicProblem(this, message));
 		 }
 		 return result;
 	 }
 
 	 /**
 	  * Set the given SingleAssociation object (which is typically connected to 'this') as the parent of the given element.
 	  * @param <T>
 	  * @param association
 	  * @param element
 	  */
 	 /*@
      @ public behavior
      @
      @ pre association != null;
      @ pre (element != null) ==> (! element.isDerived());
      @
      @ post (element != null) ==> (element.parent() == association.getObject() && association.getOtherEnd() == element);
      @ post (element == null) ==> association.getOtherEnd() == null;
      @*/
 	 protected <T extends Element> void set(SingleAssociation<? extends Element, ? super T> association, T element) {
 		 if(element != null) {
 			 association.connectTo((Association) element.parentLink());
 		 } else {
 			 association.connectTo(null);
 		 }
 	 }
 
 	 /**
 	  * Set the given AbstractMultiAssociation object (which is typically connected to 'this') as the parent of the given element.
 	  * @param <T>
 	  * @param association
 	  * @param element
 	  */
 	 /*@
      @ public behavior
      @
      @ pre association != null;
      @ pre (element != null) ==> (! element.isDerived());
      @
      @ post (element != null) ==> (element.parent() == association.getObject() && association.contains(element.parentLink());
      @*/
 	 protected <T extends Element> void add(AbstractMultiAssociation<? extends Element, ? super T> association, T element) {
 		 if(element != null) {
 			 association.add((Association)element.parentLink());
 		 }
 	 }
 
 	 /**
 	  * Set the given AbstractMultiAssociation object (which is typically connected to 'this') as the parent of the given element.
 	  * @param <T>
 	  * @param association
 	  * @param element
 	  */
 	 /*@
      @ public behavior
      @
      @ pre association != null;
      @ pre (element != null) ==> (! element.isDerived());
      @
      @ post (element != null) ==> (element.parent() == association.getObject() && association.contains(element.parentLink());
      @*/
 	 protected <T extends Element> void add(AbstractMultiAssociation<? extends Element, ? super T> association, Collection<T> elements) {
 		 for(T t: elements) {
 			 add(association,t);
 		 }
 	 }
 
 	 /**
 	  * Add the given element to the given association end.
 	  * 
 	  * @param association The association end to which the element must be added.
 	  * @param element The element that must be added to the association end.
 	  */
 	 /*@
      @ public behavior
      @
      @ pre association != null;
      @ pre element != null;
      @
      @ post association.getOtherRelations().contains(element.parentLink());
      @*/
 	 protected <E extends Element> void add(OrderedMultiAssociation<? extends Element,E> association, E element) {
 		 if(element != null) {
 			 association.add((Association)element.parentLink());
 		 }
 	 }
 
 	 /**
 	  * Remove the given element from the given association end.
 	  * 
 	  * @param association The association end to which the element must be removed.
 	  * @param element The element that must be removed to the association end.
 	  */
 	 /*@
      @ public behavior
      @
      @ pre association != null;
      @ pre element != null;
      @
      @ post !association.getOtherRelations().contains(element.parentLink());
      @*/
 	 protected <E extends Element> void remove(OrderedMultiAssociation<? extends Element,E> association, E element) {
 		 if(element != null) {
 			 association.remove((Association)element.parentLink());
 		 }
 	 }
 
 
 	 public final boolean equals(Object other) {
 		 try {
 			 return (other instanceof Element) && sameAs((Element) other);
 		 } catch (LookupException e) {
 			 throw new ChameleonProgrammerException(e);
 		 }
 	 }
 
 	 public final boolean sameAs(Element other) throws LookupException {
 		 return other == this || (uniSameAs(other) || ((other != null) && (other.uniSameAs(this))));
 	 }
 
 	 /**
 	  * By default, reference equality is used.
 	  */
 	 public boolean uniSameAs(Element other) throws LookupException {
 		 return other == this;
 	 }
 
 	 /**
 	  * Flush the cache. This method flushes the local cache using "flushLocalCache()" and then
 	  * recurses into the children.
 	  */
 	 public void flushCache() {
 		 flushLocalCache();
 		 for(Element child:childrenAux()) {
 			 if(child != null) {
 				 child.flushCache();
 			 } else {
 				 throw new ChameleonProgrammerException("The children method of class "+getClass()+" returns a collection that contains a null reference");
 			 }
 		 }
 	 }
 
 	 /**
 	  * Flush language cache and property cache.
 	  */
 	 public synchronized void flushLocalCache() {
 		 //    	_languageCache = null;
 		 if(_propertyCache != null) {
 			 _propertyCache.clear();
 		 }
 	 }
 
 	 public Namespace namespace() {
 		 NamespaceDeclaration ancestor = nearestAncestor(NamespaceDeclaration.class);
 		 if(ancestor != null) {
 			 return ancestor.namespace();
 		 } else {
 			 return null;
 		 }
 	 }
 
 	 /**
 	  * Clone the descendants of this element and make the clones the descendants of
 	  * the given element (which will typically be a clone of this element). Type
 	  * E must be the class of the current element; otherwise e does not have the
 	  * same associations as the current object.
 	  * @param e
 	  * @return
 	  */
 	 protected <E extends Element> E cloneDescendantsTo(E e) {
 		 List<ChameleonAssociation<?>> mine = associations();
 		 List<ChameleonAssociation<?>> others = associations();
 		 int size = mine.size();
 		 for(int i = 0; i < size; i++) {
 			 mine.get(i).cloneTo((ChameleonAssociation) others.get(i));
 		 }
 		 return e;
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
