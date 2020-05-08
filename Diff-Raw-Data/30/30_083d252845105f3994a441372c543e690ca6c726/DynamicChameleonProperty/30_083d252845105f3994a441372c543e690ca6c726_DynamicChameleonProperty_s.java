 package chameleon.core.property;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.rejuse.logic.ternary.Ternary;
 import org.rejuse.predicate.SafePredicate;
 import org.rejuse.property.DynamicProperty;
 import org.rejuse.property.PropertyMutex;
 import org.rejuse.property.PropertyUniverse;
 
 import chameleon.core.element.Element;
 import chameleon.core.validation.BasicProblem;
 import chameleon.core.validation.Valid;
 import chameleon.core.validation.VerificationResult;
 
 public abstract class DynamicChameleonProperty extends DynamicProperty<Element,ChameleonProperty> implements ChameleonProperty {
 
 	public DynamicChameleonProperty(String name, PropertyUniverse<ChameleonProperty> universe, PropertyMutex<ChameleonProperty> mutex,
 			ChameleonProperty inverse, Class<? extends Element> validElementType) {
 		super(name, universe, mutex, inverse);
 		addValidElementType(validElementType);
 	}
 
 	public DynamicChameleonProperty(String name, PropertyUniverse<ChameleonProperty> universe, PropertyMutex<ChameleonProperty> mutex, Class<? extends Element> validElementType) {
 		super(name, universe, mutex);
 		addValidElementType(validElementType);
 	}
 
 	public DynamicChameleonProperty(String name, PropertyUniverse<ChameleonProperty> universe, Class<? extends Element> validElementType) {
 		this(name, universe, new PropertyMutex<ChameleonProperty>(), validElementType);
 	}
 
 	private final class InverseDynamicChameleonProperty extends InverseProperty<Element, ChameleonProperty> implements ChameleonProperty {
 		private InverseDynamicChameleonProperty(String name, PropertyUniverse<ChameleonProperty> universe,
 				PropertyMutex<ChameleonProperty> mutex, ChameleonProperty inverse) {
 			super(name, universe, mutex, inverse);
 		}
 
 		@Override
 		protected void createInverse(String name, PropertyUniverse<ChameleonProperty> universe) {
 		}
 
 		public VerificationResult verify(Element element) {
 			return verifyAux(element, this);
 		}
 	}
 
 	@Override
 	protected void createInverse(String name, PropertyUniverse<ChameleonProperty> universe) {
 		new InverseDynamicChameleonProperty("not "+name, universe, mutex(), this);
 	}
 
 	public VerificationResult verify(Element element) {
 		return verifyAux(element,this);
 	}
 	
 	public VerificationResult verifyAux(Element element, DynamicProperty<Element,ChameleonProperty> property) {
 		VerificationResult result = Valid.create();
 
 		final Class<? extends Element> elementClass = element.getClass();
 		boolean validType = new SafePredicate<Class<? extends Element>>() {
 			@Override
 			public boolean eval(Class<? extends Element> validClass) {
 				return validClass.isAssignableFrom(elementClass);
 			}
 		}.exists(_validTypes);
 		if(! validType) {
 		  result = result.and(new BasicProblem(element, "Property "+property.name()+" is not applicable to a "+elementClass.getName())); 
 		}
 
 		if(property.appliesTo(element) == Ternary.FALSE) {
 			result = result.and(new BasicProblem(element,"Property "+property.name()+" does not apply to the element"));
 		} else if(property.appliesTo(element) == Ternary.UNKNOWN) {
 			result = result.and(new BasicProblem(element,"It is not know if property "+property.name()+" applies to the element"));
 		}
 		return result;
 	}
 
 	
 	/**
 	 * Return a list of class files representing elements that can have this property.
 	 */
  /*@
    @ public behavior
    @
    @ post \result != null;
    @ post ! \result.contains(null);
    @*/
 	public List<Class<? extends Element>> validElementTypes() {
 		return new ArrayList<Class<? extends Element>>(_validTypes);
 	}
 	
  /**
 	* Add the given class as a valid element type for this property. 
 	*/
 /*@
   @ public behavior
   @
   @ pre type != null;
   @ pre ! validElementTypes().contains(type);
   @
   @ post validElementTypes().contains(type);
   @*/
 	public void addValidElementType(Class<? extends Element> type) {
 		_validTypes.add(type);
 	}
 	
 	private List<Class<? extends Element>> _validTypes = new ArrayList<Class<? extends Element>>();
 
 
 }
