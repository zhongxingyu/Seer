 package chameleon.oo.expression;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.rejuse.association.SingleAssociation;
 
 import chameleon.core.declaration.Declaration;
 import chameleon.core.element.Element;
 import chameleon.core.lookup.DeclarationSelector;
 import chameleon.core.lookup.LookupException;
 import chameleon.core.reference.CrossReferenceWithArguments;
 import chameleon.core.reference.CrossReferenceWithTarget;
 import chameleon.core.reference.CrossReferenceTarget;
 import chameleon.core.reference.UnresolvableCrossReference;
 import chameleon.core.validation.Valid;
 import chameleon.core.validation.VerificationResult;
 import chameleon.exception.ChameleonProgrammerException;
 import chameleon.oo.method.Method;
 import chameleon.oo.statement.CheckedExceptionList;
 import chameleon.oo.type.Type;
 import chameleon.oo.type.generics.ActualTypeArgument;
 import chameleon.util.Util;
 
 /**
  * @author Marko van Dooren
  * 
  * @param <D>
  *            The type of declaration invoked by this invocation.
  */
 
 public abstract class MethodInvocation<E extends MethodInvocation<E, D>, D extends Method>
 		extends TargetedExpression<E> implements CrossReferenceWithTarget<E, D> {
 
 	private SingleAssociation<MethodInvocation<E, D>, CrossReferenceWithArguments> _crossReference = new SingleAssociation<MethodInvocation<E, D>, CrossReferenceWithArguments>(
 			this);
 
 	public MethodInvocation(CrossReferenceTarget target) {
 		setAsParent(_crossReference, new CrossReferenceWithArguments());
 		setTarget(target);
 	}
 
 	public final DeclarationSelector<D> selector() throws LookupException {
 		if (_selector == null) {
 			_selector = createSelector();
 		}
 		return _selector;
 	}
 
	public CrossReferenceWithArguments<?> crossReference() {
 		return _crossReference.getOtherEnd();
 	}
 
 	protected abstract DeclarationSelector<D> createSelector()
 			throws LookupException;
 
 	protected DeclarationSelector<D> _selector;
 
 	public CrossReferenceTarget getTarget() {
 		return crossReference().getTarget();
 	}
 
 	public void setTarget(CrossReferenceTarget target) {
 		crossReference().setTarget(target);
 	}
 
 	public void addArgument(Expression parameter) {
 		crossReference().addArgument(parameter);
 	}
 
 	public void addAllArguments(List<Expression> parameters) {
 		crossReference().addAllArguments(parameters);
 	}
 
 	public void removeParameter(Expression parameter) {
 		crossReference().removeParameter(parameter);
 	}
 
 	public List<Expression> getActualParameters() {
 		return crossReference().getActualParameters();
 	}
 
 	/*
 	 * @
 	 * 
 	 * @ public behavior
 	 * 
 	 * @
 	 * 
 	 * @ post \result == getActualParameters().size;
 	 * 
 	 * @
 	 */
 	public int nbActualParameters() {
 		return crossReference().nbActualParameters();
 	}
 
 	public List<Type> getActualParameterTypes() throws LookupException {
 		return crossReference().getActualParameterTypes();
 	}
 
 	/*
 	 * @
 	 * 
 	 * @ also public behavior
 	 * 
 	 * @
 	 * 
 	 * @ post \result.contains(actualArgumentList());
 	 * 
 	 * @ post getTarget() != null ==> \result.contains(getTarget());
 	 * 
 	 * @
 	 */
 	public List<Element> children() {
 		List<Element> result = new ArrayList<Element>();
 		Util.addNonNull(crossReference(), result);
 		return result;
 	}
 
 	// public Set getDirectExceptions() throws NotResolvedException {
 	// Set result = getMethodExceptions();
 	// Type npe =
 	// getLanguage().getNullInvocationException(getPackage().getDefaultPackage());
 	// if(npe != null) {
 	// result.add(npe);
 	// }
 	// result.addAll(getTarget().getDirectExceptions());
 	// Iterator iter = getActualParameters().iterator();
 	// while(iter.hasNext()) {
 	// result.addAll(((Expression)iter.next()).getDirectExceptions());
 	// }
 	// return result;
 	// }
 
 	public D getElement() throws LookupException {
 		return (D) getElement(selector());
 	}
 
 	/**
 	 * Return the method invoked by this invocation.
 	 */
 	/*
 	 * @
 	 * 
 	 * @ public behavior
 	 * 
 	 * @
 	 * 
 	 * @ post \result != null;
 	 * 
 	 * @
 	 * 
 	 * @ signals (NotResolvedException) (* The method could not be found *);
 	 * 
 	 * @
 	 */
 	// public abstract D getMethod() throws MetamodelException;
 	public <X extends Declaration> X getElement(DeclarationSelector<X> selector)
 			throws LookupException {
 		return (X) crossReference().getElement(selector);
 	}
 
 	public Declaration getDeclarator() throws LookupException {
 		return crossReference().getDeclarator();
 	}
 
 //	@Override
 //	public void flushLocalCache() {
 //		super.flushLocalCache();
 //		crossReference().flushLocalCache();
 //	}
 
 	protected D getCache() {
 		return (D) crossReference().getCache();
 	}
 
 	protected void setCache(D value) {
 		crossReference().setCache(value);
 	}
 
 	/**
 	 * Return a clone of this invocation without target or parameters.
 	 */
 	/*
 	 * @
 	 * 
 	 * @ protected behavior
 	 * 
 	 * @
 	 * 
 	 * @ post \result != null;
 	 * 
 	 * @
 	 */
 	protected abstract E cloneInvocation(CrossReferenceTarget target);
 
 	public E clone() {
 		CrossReferenceTarget target = null;
 		if (getTarget() != null) {
 			target = getTarget().clone();
 		}
 		final E result = cloneInvocation(target);
 		for (Expression element : getActualParameters()) {
 			result.addArgument(element.clone());
 		}
 		for (ActualTypeArgument arg : typeArguments()) {
 			result.addArgument(arg.clone());
 		}
 		return result;
 	}
 
 	// public void substituteParameter(String name, Expression expr) throws
 	// MetamodelException {
 	// if(getTarget()!= null) {
 	// getTarget().substituteParameter(name, expr);
 	// }
 	// }
 
 	public CheckedExceptionList getDirectCEL() throws LookupException {
 		throw new Error();
 	}
 
 	public CheckedExceptionList getDirectAbsCEL() throws LookupException {
 		throw new Error();
 	}
 
 	public List<ActualTypeArgument> typeArguments() {
 		return crossReference().typeArguments();
 	}
 	
 	public boolean hasTypeArguments() {
 		return crossReference().hasTypeArguments();
 	}
 
 	public void addArgument(ActualTypeArgument arg) {
 		crossReference().addArgument(arg);
 	}
 
 	public void addAllTypeArguments(List<ActualTypeArgument> args) {
 		crossReference().addAllTypeArguments(args);
 	}
 
 	public void removeArgument(ActualTypeArgument arg) {
 		crossReference().removeArgument(arg);
 	}
 
 	@Override
 	public VerificationResult verifySelf() {
 		VerificationResult result = Valid.create();
 		try {
 			if (getElement() == null) {
 				result = result.and(new UnresolvableCrossReference(this));
 			}
 		} catch(LookupException e) {
 			result = result.and(new UnresolvableCrossReference(this, e.getMessage()));
 		} catch(ChameleonProgrammerException e) {
 			result = result.and(new UnresolvableCrossReference(this, e.getMessage()));
 		}
 		return result;
 	}
 }
