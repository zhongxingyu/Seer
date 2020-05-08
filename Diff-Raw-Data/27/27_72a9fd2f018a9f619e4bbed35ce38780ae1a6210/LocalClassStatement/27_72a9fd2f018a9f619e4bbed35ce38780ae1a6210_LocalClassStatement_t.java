 package chameleon.support.statement;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.rejuse.association.SingleAssociation;
 
 import chameleon.core.declaration.Declaration;
 import chameleon.core.declaration.DeclarationContainer;
 import chameleon.core.element.Element;
 import chameleon.core.lookup.DeclarationSelector;
 import chameleon.core.lookup.LookupException;
 import chameleon.core.lookup.LookupStrategy;
 import chameleon.core.lookup.LookupStrategyFactory;
 import chameleon.core.scope.Scope;
 import chameleon.core.statement.StatementImpl;
 import chameleon.core.statement.StatementListContainer;
 import chameleon.core.statement.StatementListScope;
 import chameleon.core.type.Type;
 import chameleon.core.validation.Valid;
 import chameleon.core.validation.VerificationResult;
 import chameleon.support.property.accessibility.HierarchyScope;
 import chameleon.util.Util;
 
 /**
 * @author Marko van Dooren
  */
 public class LocalClassStatement extends StatementImpl<LocalClassStatement> 
              implements DeclarationContainer<LocalClassStatement,Element> {
 
 	public LocalClassStatement() {
 	}
 
 	public LocalClassStatement(Type type) {
 		setType(type);
 	}
 
   /*********
    * CLASS *
    *********/
 
 	private SingleAssociation<LocalClassStatement,Type> _type = new SingleAssociation<LocalClassStatement,Type>(this);
 
 	public void setType(Type type) {
		setAsParent(_type, type);
   }
 
   public Type getType() {
     return _type.getOtherEnd();
   }
 
   public SingleAssociation<LocalClassStatement,Type> getTypesLink() {
     return _type;
   }
 
 	public LocalClassStatement clone() {
 		return new LocalClassStatement(getType().clone());
 	}
 
 	/*@
 	 @ also public behavior
 	 @
 	 @ post getType() != null ==> \result.contains(getType());
 	 @*/
 	public List children() {
 		return Util.createNonNullList(getType());
 	}
 
 	public Scope getTypeAccessibilityDomain() throws LookupException {
 		if (parent() instanceof StatementListContainer) {
 			return new StatementListScope((StatementListContainer) parent(), this);
 		} else {
 			return new HierarchyScope(getType());
 		}
 	}
 
 	public Type getTopLevelType() {
 		return null;
 	}
 
 	public LookupStrategyFactory getContextFactory() {
 		return language().lookupFactory();
 	}
   public List<Declaration> declarations() {
     List<Declaration> result = new ArrayList<Declaration>();
     result.add(getType());
     return result;
   }
   
 //  //COPIED FROM chameleon.core.type.Type
 //  @SuppressWarnings("unchecked")
 //  public <T extends Declaration> Set<T> declarations(DeclarationSelector<T> selector) throws LookupException {
 //    List<Declaration> tmp = declarations();
 //    List<T> result = selector.selection(tmp);
 //    return result;
 //  }
 
   public LookupStrategy lexicalLookupStrategy(Element element) {
   	return language().lookupFactory().createLexicalLookupStrategy(language().lookupFactory().createTargetLookupStrategy(this), this);
   }
   
   public LookupStrategy linearLookupStrategy() {
   	return lexicalLookupStrategy(getType());
   }
 
 	public <D extends Declaration> List<D> declarations(DeclarationSelector<D> selector) throws LookupException {
     List<D> result = new ArrayList<D>();
     D element = selector.selection(getType());
     if(element != null) {
       result.add(element);
     }
     return result;
 	}
 
 	@Override
 	public VerificationResult verifySelf() {
 		return checkNull(getType(), "The type declaration statement does not declare a type.", Valid.create());
 	}
 }
