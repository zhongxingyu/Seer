 package jnome.core.enumeration;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.rejuse.association.SingleAssociation;
 import org.rejuse.logic.ternary.Ternary;
 
 import chameleon.core.declaration.Declaration;
 import chameleon.core.declaration.DeclarationContainer;
 import chameleon.core.declaration.SimpleNameSignature;
 import chameleon.core.element.Element;
 import chameleon.core.expression.ActualArgument;
 import chameleon.core.expression.ActualArgumentList;
 import chameleon.core.language.Language;
 import chameleon.core.lookup.DeclarationSelector;
 import chameleon.core.lookup.LookupException;
 import chameleon.core.lookup.LookupStrategy;
 import chameleon.core.member.FixedSignatureMember;
 import chameleon.core.member.Member;
 import chameleon.core.validation.Valid;
 import chameleon.core.validation.VerificationResult;
 import chameleon.oo.type.ClassBody;
 import chameleon.oo.type.DeclarationWithType;
 import chameleon.oo.type.Type;
 import chameleon.util.Util;
 
 public class EnumConstant extends FixedSignatureMember<EnumConstant,Type,SimpleNameSignature,EnumConstant> implements DeclarationWithType<EnumConstant,Type,SimpleNameSignature,EnumConstant>, DeclarationContainer<EnumConstant, Type>{
 
 	public EnumConstant(SimpleNameSignature signature) {
 		super(signature);
 		_parameters.connectTo(new ActualArgumentList().parentLink());
 	}
 	
 	@Override
 	public EnumConstant clone() {
 		EnumConstant result = new EnumConstant(signature().clone());
		result.setBody(getBody().clone());
		for(ActualArgument arg: getActualParameters()) {
			result.addParameter(arg.clone());
		}
 		return result;
 	}
 
 	public List<Member> getIntroducedMembers() {
 		List<Member> result = new ArrayList<Member>();
 		result.add(this);
 		return result;
 	}
 
 	public List<Element> children() {
     List<Element> result = super.children();
     result.add(actualArgumentList());
     Util.addNonNull(getBody(), result);
     return result;
 	}
 
 	/**
 	 * ACTUAL PARAMETERS
 	 */
  private SingleAssociation<EnumConstant,ActualArgumentList> _parameters = new SingleAssociation<EnumConstant,ActualArgumentList>(this);
  
  public ActualArgumentList actualArgumentList() {
 	 return _parameters.getOtherEnd();
  }
 
   public void addParameter(ActualArgument parameter) {
   	actualArgumentList().addParameter(parameter);
   }
   
   public void addAllParameters(List<ActualArgument> parameters) {
   	for(ActualArgument param: parameters) {
   		addParameter(param);
   	}
   }
 
   public void removeParameter(ActualArgument parameter) {
   	actualArgumentList().removeParameter(parameter);
   }
 
   public List<ActualArgument> getActualParameters() {
     return actualArgumentList().getActualParameters();
   }
   
   public ClassBody getBody() {
   	return _body.getOtherEnd();
   }
   
   public void setBody(ClassBody body) {
   	if(body == null) {
   		_body.connectTo(null);
   	} else {
   		_body.connectTo(body.parentLink());
   	}
   }
   
   private SingleAssociation<EnumConstant,ClassBody> _body = new SingleAssociation<EnumConstant, ClassBody>(this);
 
 	@Override
 	public VerificationResult verifySelf() {
 		return Valid.create();
 	}
 
 	@Override
 	public Class<SimpleNameSignature> signatureType() {
 		return SimpleNameSignature.class;
 	}
 
 	public Type declarationType() throws LookupException {
 		return nearestAncestor(Type.class);
 	}
 
 	public LookupStrategy targetContext() throws LookupException {
   	Language language = language();
   	if(language != null) {
 		  return language.lookupFactory().createTargetLookupStrategy(this);
   	} else {
   		throw new LookupException("Element of type "+getClass().getName()+" is not connected to a language. Cannot retrieve target context.");
   	}
 	}
 
 	public List<? extends Declaration> declarations() throws LookupException {
 		List<Declaration> result = new ArrayList<Declaration>();
 		result.addAll(getBody().declarations());
 		result.addAll(nearestAncestor(Type.class).declarations());
 		return result;
 	}
 
 	public <D extends Declaration> List<D> declarations(DeclarationSelector<D> selector) throws LookupException {
 		return selector.selection(declarations());
 	}
 
 }
