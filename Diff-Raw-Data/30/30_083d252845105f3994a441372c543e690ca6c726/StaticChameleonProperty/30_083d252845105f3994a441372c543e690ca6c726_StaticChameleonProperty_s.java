 package chameleon.core.property;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.rejuse.predicate.SafePredicate;
 import org.rejuse.property.PropertyMutex;
 import org.rejuse.property.PropertyUniverse;
 import org.rejuse.property.StaticProperty;
 
 import chameleon.core.element.Element;
 import chameleon.core.validation.BasicProblem;
 import chameleon.core.validation.Valid;
 import chameleon.core.validation.VerificationResult;
 import chameleon.exception.ChameleonProgrammerException;
 
 public class StaticChameleonProperty extends StaticProperty<Element,ChameleonProperty> implements ChameleonProperty {
 
 	public StaticChameleonProperty(String name, PropertyUniverse<ChameleonProperty> universe,
 			PropertyMutex<ChameleonProperty> mutex, ChameleonProperty inverse, Class<? extends Element> validElementType) {
 		super(name, universe, mutex, inverse);
 		_validTypes.add(validElementType);
 	}
 
 	public StaticChameleonProperty(String name, PropertyUniverse<ChameleonProperty> universe,
 			PropertyMutex<ChameleonProperty> mutex, Class<? extends Element> validElementType) {
 		super(name,universe, mutex);
 		_validTypes.add(validElementType);
 	}
 
 	public StaticChameleonProperty(String name, PropertyUniverse<ChameleonProperty> universe, Class<? extends Element> validElementType) {
 		super(name, universe);
 		_validTypes.add(validElementType);
 	}
 
 	/**
 	 * YOU MUST OVERRIDE THIS METHOD IF YOU CREATE A SUBCLASS. Otherwise, the verify() method of the inverse may
 	 * not work properly.
 	 */
 	@Override
 	protected void createInverse(String name, PropertyUniverse<ChameleonProperty> universe) {
 		new InverseChameleonProperty("not "+name, universe, mutex(), this);
 	}
 
 	/**
 	 * If the given element is not of a valid type for this property, a problem will be returned.
 	 */
  /*@
    @ public behavior
    @
    @ pre element != null;
    @
    @ post ! (\exists Class<? extends Element> cls; validElementTypes().contains(cls); cls.isAssignableFrom(element.getClass()) ==> \result instanceof Invalid;
    @*/
 	public VerificationResult verify(Element element) {
 		final Class<? extends Element> elementClass = element.getClass();
 		boolean validType = new SafePredicate<Class<? extends Element>>() {
 			@Override
 			public boolean eval(Class<? extends Element> validClass) {
 				return validClass.isAssignableFrom(elementClass);
 			}
 		}.exists(_validTypes);
 		if(validType) {
 			return Valid.create();
 		} else {
 		  return new BasicProblem(element, "Property "+name()+" is not applicable to a "+elementClass.getName()); 
 		}
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
 
 	private final class InverseChameleonProperty extends InverseProperty<Element, ChameleonProperty> implements ChameleonProperty {
 		private InverseChameleonProperty(String name, PropertyUniverse<ChameleonProperty> universe, PropertyMutex<ChameleonProperty> family,
 				ChameleonProperty inverse) {
 			super(name, universe, family, inverse);
 		}
 
 		@Override
 		protected void createInverse(String name, PropertyUniverse<ChameleonProperty> universe) {
 			throw new ChameleonProgrammerException("This method should not be invoked for an InverseProperty. Its inverse is passed as an argument to the constructor.");
 		}
 
 		public VerificationResult verify(Element element) {
 			final Class<? extends Element> elementClass = element.getClass();
 			boolean validType = new SafePredicate<Class<? extends Element>>() {
 				@Override
 				public boolean eval(Class<? extends Element> validClass) {
 					return validClass.isAssignableFrom(elementClass);
 				}
 			}.exists(_validTypes);
 			if(validType) {
 				return Valid.create();
 			} else {
 			  return new BasicProblem(element, "Property "+name()+" is not applicable to a "+elementClass.getName()); 
 			}
 		}
 	}
 
 
 }
