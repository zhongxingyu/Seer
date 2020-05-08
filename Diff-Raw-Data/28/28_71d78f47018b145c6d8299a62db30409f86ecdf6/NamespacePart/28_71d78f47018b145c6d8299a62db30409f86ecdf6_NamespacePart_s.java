 package chameleon.core.namespacepart;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.ListIterator;
 
 import org.rejuse.association.OrderedMultiAssociation;
 import org.rejuse.association.SingleAssociation;
 import org.rejuse.predicate.TypePredicate;
 
 import chameleon.core.Config;
 import chameleon.core.declaration.Declaration;
 import chameleon.core.declaration.DeclarationContainer;
 import chameleon.core.element.ChameleonProgrammerException;
 import chameleon.core.element.Element;
 import chameleon.core.language.Language;
 import chameleon.core.lookup.DeclarationSelector;
 import chameleon.core.lookup.LocalLookupStrategy;
 import chameleon.core.lookup.LookupException;
 import chameleon.core.lookup.LookupStrategy;
 import chameleon.core.lookup.LookupStrategyFactory;
 import chameleon.core.lookup.LookupStrategySelector;
 import chameleon.core.namespace.Namespace;
 import chameleon.core.namespace.NamespaceElementImpl;
 import chameleon.core.validation.Valid;
 import chameleon.core.validation.VerificationResult;
 /**
  * A namespace part adds its declarations to a namespace. Different namespace parts in different compilation units
  * can contribute to the same namespace.
  * 
  * @author Marko van Dooren
  * @author Tim Laeremans
  */
 public class NamespacePart extends NamespaceElementImpl<NamespacePart,Element> implements DeclarationContainer<NamespacePart,Element> {
 
 	private final class DefaultNamespaceSelector implements LookupStrategySelector {
 		public LookupStrategy strategy() throws LookupException {
 			// 5 SEARCH IN DEFAULT NAMESPACE
 			return NamespacePart.this.getDefaultNamespace().targetContext();
 		}
 	}
 
 	protected class ImportLocalDemandContext extends LocalLookupStrategy<NamespacePart> {
 	  public ImportLocalDemandContext(NamespacePart element) {
 			super(element);
 		}
 
 	  @Override
 	  public <D extends Declaration> List<D> declarations(DeclarationSelector<D> selector) throws LookupException {
 	    List<D> result = new ArrayList<D>();
 			List<Import> imports = imports();
 			ListIterator<Import> iter = imports.listIterator(imports.size());
 			// If the selector found a match, we stop.
 			// We must iterate in reverse.
 			while(result.isEmpty() && iter.hasPrevious()) {
 				Import imporT = iter.previous();
 		    result.addAll(imporT.demandImports(selector));
 	    }
 	    return result;
 	  }
 	}
 	
 	protected class ImportLocalDirectContext extends LocalLookupStrategy<NamespacePart> {
 		private ImportLocalDirectContext(NamespacePart element) {
 			super(element);
 		}
 
 		@Override
 		public <D extends Declaration> List<D> declarations(DeclarationSelector<D> selector) throws LookupException {
 			List<D> result = new ArrayList<D>();
 			List<Import> imports = imports();
 			ListIterator<Import> iter = imports.listIterator(imports.size());
 			// If the selector found a match, we stop.
 			// We must iterate in reverse.
 			while(result.isEmpty() && iter.hasPrevious()) {
 				Import imporT = iter.previous();
 				result.addAll(imporT.directImports(selector));
 			}
 		  return result;
 		}
 	}
 
 	protected class DemandImportStrategySelector implements LookupStrategySelector {
 		public LookupStrategy strategy() {
 			// 4 SEARCH DEMAND IMPORTS
 			return _importDemandContext;
 		}
 	}
 
 	protected class CurrentNamespaceStrategySelector implements LookupStrategySelector {
 		public LookupStrategy strategy() {
 			// 3 SEARCH IN CURRENT NAMESPACE
 			LookupStrategy currentNamespaceStrategy = namespace().localStrategy();
 			return language().lookupFactory().createLexicalLookupStrategy(
 					 currentNamespaceStrategy, NamespacePart.this, _demandImportStrategySelector)
 			;
 		}
 	}
 
 	protected class DirectImportStrategySelector implements LookupStrategySelector {
 		public LookupStrategy strategy() {
 			// 2 SEARCH IN DIRECT IMPORTS
 			return _importDirectContext;
 		}
 	}
 
 	/**
 	 * Create a new namespace part the adds elements to the given namespace.
 	 * @param namespace
 	 */
  /*@
    @ public behavior
    @
    @ pre namespace != null;
    @
    @ post getDeclaredNamespace() == namespace
    @*/ 
 	public NamespacePart(Namespace namespace) {
     setNamespace(namespace);
 	}
 	
 	public LookupStrategy lexicalLookupStrategy(Element child) throws LookupException {
 		if(imports().contains(child)) {
 			return getDefaultNamespace().targetContext();
 		} else {
 			return getLexicalContext();
 		}
 	}
 	
 	private LookupStrategy getLexicalContext() {
 		if(_lexicalContext == null) {
 			initContexts();
 		}
 		return _lexicalContext;
 	}
 	
 	private void initContexts() {
     // This must be executed after the namespace is set, so it cannot be in the initialization.
     _typeLocalContext = language().lookupFactory().createTargetLookupStrategy(this);
     _importLocalDirectContext = new ImportLocalDirectContext(this);
 //    _importLocalDemandContext = language().lookupFactory().wrapLocalStrategy(new ImportLocalDemandContext(this),this);
     _importLocalDemandContext = new ImportLocalDemandContext(this);
 		_importDirectContext = language().lookupFactory().createLexicalLookupStrategy(_importLocalDirectContext, this, _currentNamespaceStrategySelector);
 		_importDemandContext = language().lookupFactory().createLexicalLookupStrategy(_importLocalDemandContext, this, _defaultNamespaceSelector);
     // 1 SEARCH IN NAMESPACEPART
 		_lexicalContext = language().lookupFactory().createLexicalLookupStrategy(localContext(), this, _directImportStrategySelector); 
 	}
 
   private DirectImportStrategySelector _directImportStrategySelector = new DirectImportStrategySelector();
 
 	private CurrentNamespaceStrategySelector _currentNamespaceStrategySelector = new CurrentNamespaceStrategySelector();
 
 	private DemandImportStrategySelector _demandImportStrategySelector = new DemandImportStrategySelector();
 
 	private DefaultNamespaceSelector _defaultNamespaceSelector = new DefaultNamespaceSelector();
 	
 	private LookupStrategy _importLocalDirectContext;
 	
 	private LookupStrategy _importDirectContext;
 	
 	private LookupStrategy _importLocalDemandContext;
 	
 	private LookupStrategy _importDemandContext;
 	
 	public LookupStrategy localContext() {
 		if(_typeLocalContext == null) {
 			initContexts();
 		}
 		return _typeLocalContext;
 	}
 	
 	private LookupStrategy _lexicalContext;
 	
 	public String getName(){
 		return namespace().name();
 	}
 
 	public String getFullyQualifiedName() {
 		return namespace().getFullyQualifiedName();
 	}
 
 	public NamespacePart getNearestNamespacePart() {
 		return this;
 	}
 	
 	/**
 	 * NAMESPACEPARTS
 	 */
 	public List<NamespacePart> namespaceParts() {
 		return _subNamespaceParts.getOtherEnds();
 	}
 
 	public void addNamespacePart(NamespacePart pp) {
 		_subNamespaceParts.add(pp.parentLink());
 	}
 
 	public void removeNamespacePart(NamespacePart pp) {
 		_subNamespaceParts.remove(pp.parentLink());
 	}
 
 	public OrderedMultiAssociation<NamespacePart, NamespacePart> getNamespacePartsLink() {
 		return _subNamespaceParts;
 	}
 	
 	/**
 	 * Recursively disconnect this namespace declaration and all descendant namespace declarations
 	 * from their namespaces. 
 	 */
 	public void nonRecursiveDisconnect() {
 		// 1) Set the lexical parent to null.
 		super.nonRecursiveDisconnect();
 		if(Config.DEBUG) {
 			if(namespace() != null) {
 			  System.out.println("Disconnecting from "+namespace().getFullyQualifiedName());
 			}
 //			showStackTrace("Disconnecting from "+namespace().getFullyQualifiedName());
 		}
 		// 2) Disconnect from the namespace. 
 		setNamespace(null);
 //		// 3) IS NOW DONE BY DEFAULT RECURSION Disconnecting the children.
 //		for(NamespacePart nsp: namespaceParts()) {
 //			nsp.disconnect();
 //		}
 	}
 
 	/**
 	 * A namespace part is disconnected if both its parent and its namespace are null.
 	 */
  /*@
    @ public behavior
    @
    @ post \result == (parent() == null) && (namespace() == null);
    @*/
 	@Override
 	public boolean disconnected() {
 		return super.disconnected() && namespace() != null;
 	}
 	
 	private OrderedMultiAssociation<NamespacePart, NamespacePart> _subNamespaceParts = new OrderedMultiAssociation<NamespacePart, NamespacePart>(this);
 
	public List<? extends Element> children() {
 		List result = declarations(); // can't specify type parameter without having to clone types(). don't like it.
 		result.addAll(namespaceParts());
 		result.addAll(imports());
 		return result;
 	}
 	
	public List<? extends Declaration> declarations() {
       return _types.getOtherEnds();
 	}
 	
 	public <T extends Declaration> List<T> declarations(Class<T> kind) {
     return new TypePredicate<Declaration,T>(kind).filterReturn(declarations());
   }
 	
 	public <D extends Declaration> List<D> declarations(DeclarationSelector<D> selector) throws LookupException {
 		return selector.selection(declarations());
 	}
 
 
 	/*************
 	 * NAMESPACE *
 	 *************/
 	private SingleAssociation<NamespacePart, Namespace> _namespaceLink = new SingleAssociation<NamespacePart, Namespace>(this);
 
 	public SingleAssociation<NamespacePart, Namespace> getNamespaceLink() {
 		return _namespaceLink;
 	}
 
 	/**
 	 * Return the namespace to which this namespacepart adds declarations.
 	 * @return
 	 */
 	public Namespace namespace() {
 		return _namespaceLink.getOtherEnd();
 	}
 
 	public void setNamespace(Namespace namespace) {
 		if (namespace != null) {
 //			showStackTrace("Adding namespace part to namespace "+namespace.getFullyQualifiedName());
 			namespace.addNamespacePart(this);
 		} else {
 			_namespaceLink.connectTo(null);
 		}
 	}
 
 
 	/******************
 	 * DEMAND IMPORTS *
 	 ******************/
 
 	private OrderedMultiAssociation<NamespacePart,Import> _imports = new OrderedMultiAssociation<NamespacePart,Import>(this);
 
 	public List<Import> imports() {
 		return _imports.getOtherEnds();
 	}
 
 	public void addImport(Import newImport) {
 		_imports.add(newImport.parentLink());
 	}
 
 	public void removeImport(Import removedImport) {
 		_imports.remove(removedImport.parentLink());
 	}
 
 	/****************
 	 * DECLARATIONS *
 	 ****************/
 	protected OrderedMultiAssociation<NamespacePart, Declaration> _types = new OrderedMultiAssociation<NamespacePart, Declaration>(this);
 
 	/**
 	 * Add the given declaration to this namespace part.
 	 */
  /*@
    @ public behavior
    @
    @ pre declaration != null;
    @
    @ post declarations().contains(declaration);
    @*/
 	public void add(Declaration declaration) {
 		if(declaration != null) {
 		  _types.add(declaration.parentLink());
 		} else {
 			throw new ChameleonProgrammerException("Cannot add a null declaration to a namespace part.");
 		}
 	}
 	
 	public void addAll(Collection<Declaration> declarations) {
 		for(Declaration decl: declarations) {
 			add(decl);
 		}
 	}
 
 	/**
 	 * Remove the given declaration from this namespace part.
 	 */
  /*@
    @ public behavior
    @
    @ pre declaration != null;
    @
    @ post !declarations().contains(declaration);
    @*/
 	public void remove(Declaration declaration) {
 		_types.remove(declaration.parentLink());
 	}
 
 	/**
 	 * The parents are possibly other PackageParts and the CompilationUnit
 	 */
 
 
 
 	/**
 	 * ACCESSIBILITY
 	 */
 
 	public Language language(){
 		if(namespace() != null) {
 		  return namespace().language();
 		} else {
 			return null;
 		}
 	}
 	public LookupStrategyFactory getContextFactory() {
 		return language().lookupFactory();
 	}
 
  /*@
    @ public behavior
    @
    @ post \result = getDeclaredNamespace().getDefaultNamespace(); 
    @*/
 	public Namespace getDefaultNamespace() {
 		return namespace().defaultNamespace();
 	}
 
   @Override
   public NamespacePart clone() {
   	NamespacePart result = new NamespacePart(null);
   	for(NamespacePart part: namespaceParts()) {
   		result.addNamespacePart(part.clone());
   	}
   	for(Declaration declaration:declarations()) {
   		result.add(declaration.clone());
   	}
   	for(Import<Import> importt:imports()) {
   		result.addImport(importt.clone());
   	}
   	return result;
   }
 
 	private LookupStrategy _typeLocalContext;
 
 	@Override
 	public VerificationResult verifySelf() {
 		return Valid.create();
 	}
 
 }
