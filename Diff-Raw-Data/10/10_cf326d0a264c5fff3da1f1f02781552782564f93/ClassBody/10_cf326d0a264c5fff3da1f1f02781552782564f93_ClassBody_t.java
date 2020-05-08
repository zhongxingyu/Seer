 package chameleon.oo.type;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 
 import org.rejuse.association.AssociationListener;
 import org.rejuse.association.OrderedMultiAssociation;
 import org.rejuse.predicate.TypePredicate;
 
 import chameleon.core.Config;
 import chameleon.core.declaration.Declaration;
 import chameleon.core.declaration.DeclarationContainer;
 import chameleon.core.element.Element;
 import chameleon.core.lookup.DeclarationSelector;
 import chameleon.core.lookup.LookupException;
 import chameleon.core.lookup.LookupStrategy;
 import chameleon.core.member.Member;
 import chameleon.core.namespace.NamespaceElement;
 import chameleon.core.namespace.NamespaceElementImpl;
 import chameleon.core.validation.Valid;
 import chameleon.core.validation.VerificationResult;
 import chameleon.util.CreationStackTrace;
 
 public class ClassBody extends NamespaceElementImpl<ClassBody> implements NamespaceElement<ClassBody>, DeclarationContainer<ClassBody> {
 
 	public ClassBody() {
 		parentLink().addListener(new AssociationListener<Element>() {
 
 			@Override
 			public void notifyElementAdded(Element element) {
 				_elements.addListener(_listener);
 			}
 
 			@Override
 			public void notifyElementRemoved(Element element) {
 				_elements.removeListener(_listener);
 			}
 
 			@Override
 			public void notifyElementReplaced(Element oldElement, Element newElement) {
 			}
 		});
 	}
 	
 	@Override
 	public ClassBody clone() {
 		ClassBody result = new ClassBody();
 		for(TypeElement element: elements()) {
 			result.add(element.clone());
 		}
 		return result;
 	}
 
 	public void addAll(Collection<? extends TypeElement> elements) {
 		for(TypeElement element:elements) {
 			add(element);
 		}
 	}
 	
 	private OrderedMultiAssociation<ClassBody, TypeElement> _elements = new OrderedMultiAssociation<ClassBody, TypeElement>(this);
 	
 		
 	private AssociationListener<TypeElement> _listener = new AssociationListener<TypeElement>() {
 
 		@Override
 		public void notifyElementAdded(TypeElement element) {
 			Element parent = parent();
 			if(parent != null) {
 			  parent.reactOnDescendantAdded(element);
 			}
 		}
 
 		@Override
 		public void notifyElementRemoved(TypeElement element) {
 			Element parent = parent();
 			if(parent != null) {
 			  parent.reactOnDescendantRemoved(element);
 			}
 		}
 
 		@Override
 		public void notifyElementReplaced(TypeElement oldElement, TypeElement newElement) {
 			Element parent = parent();
 			if(parent != null) {
 			  parent.reactOnDescendantReplaced(oldElement, newElement);
 			}
 		}
 		
 	};
 
 	public void add(TypeElement element) {
 	  if(element != null) {
 	    _elements.add(element.parentLink());
 	  }
 	}
 	
 	public void clear() {
 		_elements.clear();
 	}
 	
 	public void remove(TypeElement element) {
 	  if(element != null) {
 	    _elements.remove(element.parentLink());
 	  }
 	}
 	
 	public List<TypeElement> elements() {
 		return _elements.getOtherEnds();
 	}
 	
 	public <D extends Member> List<D> members(DeclarationSelector<D> selector) throws LookupException {
 		if(selector.usesSelectionName()) {
 			List<? extends Declaration> list = null;
 			if(Config.cacheDeclarations()) {
 				ensureLocalCache();
 				synchronized(this) {
 				  list = _declarationCache.get(selector.selectionName(this));
 				}
 			} else {
 				list = members();
 			}
 			if(list == null) {
 				list = Collections.EMPTY_LIST;
 			}
 			return selector.selection(Collections.unmodifiableList(list));
 		} else {
 			return selector.selection(declarations());
 		}
 	}
 	
 	public <D extends Member> List<D> members(Class<D> kind) throws LookupException {
 		return (List<D>) new TypePredicate(kind).filterReturn(members());
 	}
 	
 	protected List<Declaration> declarations(String selectionName) throws LookupException {
 		ensureLocalCache();
 		List<Declaration> result = cachedDeclarations(selectionName);
 		if(result == null) {
 			result = new ArrayList<Declaration>();
 		}
 		return result;
 	}
 	private synchronized void ensureLocalCache() throws LookupException {
 		if(! _completelyCached) {
 			_completelyCached = true;
 			List<Member> members = members();
 		  _declarationCache = new HashMap<String, List<Declaration>>();
 		  for(Member member: members) {
 		  	String name = member.signature().name();
 				List<Declaration> list = cachedDeclarations(name);
 		  	boolean newList = false;
 		  	if(list == null) {
 		  		list = new ArrayList<Declaration>();
 		  		newList = true;
 		  	}
 		  	// list != null
 		  	list.add(member);
 		  	if(newList) {
 		  		_declarationCache.put(name, list);
 		  	}
 		  }
 		}
 	}
 	
 	private boolean _completelyCached = false;
 
 	protected synchronized List<Declaration> cachedDeclarations(String name) {
 		if(_declarationCache != null) {
 		  return _declarationCache.get(name);
 		} else {
 			return null;
 		}
 	}
 	
 	protected synchronized void storeCache(String name, List<Declaration> declarations) {
 		if(_declarationCache == null) {
 			_declarationCache = new HashMap<String, List<Declaration>>();
 		}
 		_declarationCache.put(name, declarations);
 	}
 	
 	@Override
 	public synchronized void flushLocalCache() {
 		_declarationCache = null;
 		_completelyCached = false;
 	}
 	
 	private HashMap<String, List<Declaration>> _declarationCache;
 	
 	public List<Member> members() throws LookupException {
 		List<Member> result = new ArrayList<Member>();
     for(TypeElement m: elements()) {
       result.addAll(m.getIntroducedMembers());
     }
     return result;
 	}
 
 	public List<? extends Element> children() {
 		return new ArrayList<Element>(elements());
 	}
 
 //	public LookupStrategy lexicalLookupStrategy(Element element) {
 //		return language().lookupFactory().createLexicalLookupStrategy(localContext(), this);
 //	}
 	
 //	public LookupStrategy localContext() {
 //		return language().lookupFactory().createTargetLookupStrategy(this);
 //	}
 
 	public List<Member> declarations() throws LookupException {
 		return members();
 	}
 
 	public <D extends Declaration> List<D> declarations(DeclarationSelector<D> selector) throws LookupException {
 		return selector.selection(declarations());
 	}
 
 	public void replace(TypeElement oldElement, TypeElement newElement) {
 		_elements.replace(oldElement.parentLink(), newElement.parentLink());
 	}
 
 	@Override
 	public VerificationResult verifySelf() {
 		return Valid.create();
 	}
 	
 	/**
 	 * This method passes 'element' to the lexicalLookupStrategy method of the parent. It does this
 	 * such that the parent can override the lookup for specific members.
 	 */
   public LookupStrategy lexicalLookupStrategy(Element element) throws LookupException {
   	// WE DO NOT USE 'this' such that a subclass of regular type can override the lookup for specific members.
   	return parent().lexicalLookupStrategy(element);
   }
 
 	public List<? extends Declaration> locallyDeclaredDeclarations() throws LookupException {
 		return declarations();
 	}
 
 }
