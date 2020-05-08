 /**
  * 
  */
 package jnome.core.expression.invocation;
 
 import java.util.List;
 
 import jnome.core.language.Java;
 import jnome.core.type.ArrayTypeReference;
 import jnome.core.type.BasicJavaTypeReference;
 import jnome.core.type.JavaTypeReference;
 
 import org.rejuse.association.SingleAssociation;
 import org.rejuse.predicate.UnsafePredicate;
 
 import chameleon.core.declaration.Declaration;
 import chameleon.core.element.Element;
 import chameleon.core.lookup.LookupException;
 import chameleon.core.lookup.LookupStrategy;
 import chameleon.core.namespace.NamespaceElementImpl;
 import chameleon.core.validation.Valid;
 import chameleon.core.validation.VerificationResult;
 import chameleon.oo.type.IntersectionTypeReference;
 import chameleon.oo.type.Type;
 import chameleon.oo.type.TypeReference;
import chameleon.oo.type.generics.TypeParameter;
 import chameleon.util.Util;
 
 public class NonLocalJavaTypeReference extends NamespaceElementImpl<NonLocalJavaTypeReference,Element> implements JavaTypeReference<NonLocalJavaTypeReference> {
 
 	public NonLocalJavaTypeReference(JavaTypeReference tref) {
    this(tref,tref.parent());
 	}
 	
 	public NonLocalJavaTypeReference(JavaTypeReference tref, Element lookupParent) {
 	   setActualReference(tref);
 		_lookupParent = lookupParent;
 	}
 	
 	public JavaTypeReference actualReference() {
 		return _actual.getOtherEnd();
 	}
 	
 	public void setActualReference(JavaTypeReference actual) {
 		setAsParent(_actual, actual);
 	}
 	
 	private SingleAssociation<NonLocalJavaTypeReference, JavaTypeReference> _actual = new SingleAssociation<NonLocalJavaTypeReference, JavaTypeReference>(this); 
 	
 
 //	public NonLocalJavaTypeReference(CrossReference<?, ?, ? extends TargetDeclaration> target, SimpleNameSignature signature, Element lookupParent) {
 //		super(target,signature);
 //		_lookupParent = lookupParent;
 ////		setArrayDimension(arrayDimension);
 //	}
 
 	@Override
 	public LookupStrategy lexicalLookupStrategy() throws LookupException {
 		return lookupParent().lexicalLookupStrategy(this);
 	}
 	
 	public Element lookupParent() {
 		return _lookupParent;
 	}
 	
 	@Override
 	public NonLocalJavaTypeReference clone() {
 		return new NonLocalJavaTypeReference(actualReference().clone(),lookupParent());
 	}
 	
 	public static void replace(JavaTypeReference replacement, final Declaration declarator, JavaTypeReference<?> in) throws LookupException {
 		List<BasicJavaTypeReference> crefs = in.descendants(BasicJavaTypeReference.class, 
 				new UnsafePredicate<BasicJavaTypeReference, LookupException>() {
 			@Override
 			public boolean eval(BasicJavaTypeReference object) throws LookupException {
 				return object.getDeclarator().sameAs(declarator);
 			}
 		});
 		for(BasicJavaTypeReference cref: crefs) {
 			JavaTypeReference substitute;
 			if(replacement.isDerived()) {
 				Element oldParent = replacement.parent();
 				replacement.setUniParent(null);
 			  substitute = new NonLocalJavaTypeReference(replacement,oldParent);
 			} else {
 			  substitute = new NonLocalJavaTypeReference(replacement);
 			}
 			SingleAssociation crefParentLink = cref.parentLink();
 			crefParentLink.getOtherRelation().replace(crefParentLink, substitute.parentLink());
 		}
 	}
 
 	private Element _lookupParent;
 
 
 	@Override
 	public VerificationResult verifySelf() {
 		return Valid.create();
 	}
 
 	public JavaTypeReference componentTypeReference() {
 		return actualReference().componentTypeReference();
 	}
 
 	public JavaTypeReference erasedReference() {
 		JavaTypeReference erasedReference = actualReference().erasedReference();
 		NonLocalJavaTypeReference result = new NonLocalJavaTypeReference(erasedReference, lookupParent());
 		return result;
 	}
 
 //	public Type erasure() throws LookupException {
 //		JavaTypeReference actualReference = actualReference();
 //		return actualReference.erasure();
 //	}
 
 	public Type getElement() throws LookupException {
 		return actualReference().getElement();
 	}
 
 	public Type getType() throws LookupException {
 		return getElement();
 	}
 
 	public TypeReference intersection(TypeReference other) {
 		return other.intersectionDoubleDispatch(this);
 	}
 
 	public TypeReference intersectionDoubleDispatch(TypeReference other) {
 		return language(Java.class).createIntersectionReference(clone(), other.clone());
 	}
 
 	public TypeReference intersectionDoubleDispatch(IntersectionTypeReference<?> other) {
 		IntersectionTypeReference<?> result = other.clone();
 		result.add(clone());
 		return result;
 	}
 
 	public Declaration getDeclarator() throws LookupException {
 		return actualReference().getDeclarator();
 	}
 
 	public List<? extends Element> children() {
 		return Util.createNonNullList(actualReference());
 	}
 	
   public JavaTypeReference toArray(int arrayDimension) {
   	JavaTypeReference result = new ArrayTypeReference(clone(), arrayDimension);
   	return result;
   }
 
 }
