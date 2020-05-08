 package org.spoofax.interpreter.library.language.spxlang.index.data;
 
 import java.io.Serializable;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.spoofax.NotImplementedException;
 import org.spoofax.interpreter.library.language.spxlang.index.SpxSemanticIndexFacade;
 import org.spoofax.interpreter.terms.IStrategoAppl;
 import org.spoofax.interpreter.terms.IStrategoConstructor;
 import org.spoofax.interpreter.terms.IStrategoList;
 import org.spoofax.interpreter.terms.IStrategoTerm;
 import org.spoofax.interpreter.terms.ITermFactory;
 import org.spoofax.interpreter.terms.TermConverter;
 import org.spoofax.jsglr.client.imploder.ImploderAttachment;
 import org.spoofax.terms.StrategoListIterator;
 
 public abstract class IdentifiableConstruct implements Serializable
 {
 	private static final long serialVersionUID = 1055862481052307186L;
 	
 	protected final IStrategoList id;
 	protected final Set<IStrategoList> importReferences; 
 	protected final Set<IStrategoTerm> legacyImportReferences;
 	
 	public IdentifiableConstruct(IStrategoList id) {
 		assert id != null :  " ID can not be null " ;
 
 		this.id = id;
 		this.importReferences = new HashSet<IStrategoList>();
 		this.legacyImportReferences = new HashSet<IStrategoTerm>();
 	}
 	
 	public IStrategoList getId(){ return id; }  
 	
 	public abstract IStrategoTerm toTerm(SpxSemanticIndexFacade idxFacade);
 	
 	public void addImportRefernces (SpxSemanticIndexFacade idxFacade, IStrategoList  imports) throws NotImplementedException, SpxSymbolTableException {
 		for (IStrategoTerm i: StrategoListIterator.iterable(imports)) {
 			if(i instanceof IStrategoAppl)
 				addManagedImport(idxFacade, i); 
 			else
 				addLegacyImport(i);
 		}
 	}
 
 	/**
 	 * @param i
 	 */
 	private void addLegacyImport(IStrategoTerm i) {
 		this.legacyImportReferences.add(i);
 	}
 
 	/**
 	 * @param idxFacade
 	 * @param i
 	 * @throws IllegalArgumentException
 	 * @throws NotImplementedException
 	 */
 	private void addManagedImport(SpxSemanticIndexFacade idxFacade,	IStrategoTerm i) throws SpxSymbolTableException {
 		IStrategoAppl packageRef = (IStrategoAppl)i;
 		if( packageRef.getConstructor() ==  idxFacade.getPackageQNameCon()){
 			IStrategoList id = PackageDeclaration.getPackageId(idxFacade, packageRef);
 			PackageDeclaration decl = idxFacade.lookupPackageDecl(id);
			if((decl != null) && (!SpxSymbol.verifyEquals(this.id, id))){	
 				this.importReferences.add(id);
 				decl.addImportedTo(this.getId());
 				idxFacade.persistenceManager().spxPackageTable().definePackageDeclaration(decl);
 			}
 		}
 		else 
 			throw new NotImplementedException("Unknown Import Reference. Not implemented for : " + packageRef.toString());
 	}
 	
 	private IStrategoTerm tranformToSpxImport(SpxSemanticIndexFacade idxFacade, IStrategoTerm i){
 		IStrategoTerm retTerm = i ; 
 		if( i instanceof IStrategoList)
 			retTerm = PackageDeclaration.toPackageQNameAppl(idxFacade, (IStrategoList)i);
 
 		return retTerm;	
 	}
 	
 	protected Set<IStrategoTerm> getEnclosedImportReferences(SpxSemanticIndexFacade idxFacade) throws SpxSymbolTableException { return new HashSet<IStrategoTerm>(); } 
 	
 	public Set<IStrategoList> getImportReferneces() { return importReferences; }
 	
 	public  IStrategoList getImports(SpxSemanticIndexFacade idxFacade) {	
 		ITermFactory termFactory = idxFacade.getTermFactory();
 		TermConverter termConverter = idxFacade.getTermConverter();
 		
 		HashSet<IStrategoTerm> allImportRefs = new HashSet<IStrategoTerm>();
 		allImportRefs.addAll(this.importReferences);
 		allImportRefs.addAll(this.legacyImportReferences);
 		
		
 		IStrategoList result = termFactory.makeList();
		for (IStrategoTerm t: allImportRefs){
 			result = idxFacade.getTermFactory().makeListCons(tranformToSpxImport(idxFacade,t), result);
		}	
 		return termConverter.convert(result);
 	}
 	
 	protected IStrategoTerm forceImploderAttachment(IStrategoTerm term) {
 		ImploderAttachment attach = ImploderAttachment.get(id);
 		if (attach != null) {
 			ImploderAttachment.putImploderAttachment(term, false, attach.getSort(), attach.getLeftToken(), attach.getRightToken());
 		} 
 		else {
 			String fn = getFileLocation();
 			term.putAttachment(ImploderAttachment.createCompactPositionAttachment(
 					fn, 0, 0, 0, -1));
 		}
 		return term;
 	}
 	
 	/**
 	 * Returns the location of the construct 
 	 * 
 	 * @return {@link String} representing the absolute path of the  Construct
 	 */
 	protected String getFileLocation() { return null; }
 	
 
 	/**
 	 * Converts {@code id} to qualified name . If the given id is [id],
 	 * it converts it to QName([id]) 
 	 * 
 	 * @param facade
 	 * @param namespaceCon
 	 * @param id
 	 * @return
 	 */
 	static IStrategoAppl toIdTerm ( SpxSemanticIndexFacade facade , IStrategoConstructor namespaceCon, IStrategoList id){
 		ITermFactory factory = facade.getTermFactory();
 		
 		IStrategoConstructor qnameCons = facade.getQNameCon();
 		IStrategoAppl qnameAppl = factory.makeAppl(qnameCons, id);
 		return factory.makeAppl(namespaceCon, qnameAppl);
 	}
 
 	/**
 	 * Returns {@link IStrategoList} representation of qualified ID of the {@link IdentifiableConstruct}  
 	 * 
 	 * @param fac an instance of {@link ITermFactory}
 	 * @param qName Typed qualified Name of the construct 
 	 * 
 	 * @return underlying {@link IStrategoList} qualified name
 	 */
 	public static IStrategoList getID(SpxSemanticIndexFacade facade, IStrategoAppl qName) {
 		
 		if(qName.getConstructor() == facade.getQNameCon())
 			return (IStrategoList)qName.getSubterm(0);
 		
 		throw new IllegalArgumentException("Invalid QName : " + qName);
 	}
 	
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((id == null) ? 0 : id.hashCode());
 		return result;
 	}
 	
 	/* (non-Javadoc)
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 		return "IdentifiableConstruct [id=" + id + "]";
 	}
 	
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		IdentifiableConstruct other = (IdentifiableConstruct) obj;
 		if (id == null) {
 			if (other.id != null)
 				return false;
 		} else if (!id.match(other.id))
 			return false;
 		return true;
 	}
 
 }
