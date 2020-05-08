 package chameleon.core.reference;
 
import java.util.List;

 import org.apache.log4j.Logger;
 import org.rejuse.association.SingleAssociation;
 
 import chameleon.core.Config;
 import chameleon.core.declaration.Declaration;
 import chameleon.core.declaration.SimpleNameSignature;
 import chameleon.core.element.Element;
import chameleon.util.Util;
 
 /**
  * 
  * 
  * @author Marko van Dooren
  */
 public abstract class ElementReference<E extends ElementReference, P extends Element, D extends Declaration> extends CrossReferenceImpl<E,P,D> {
 
 	private static Logger logger = Logger.getLogger("lookup.elementreference");
 	
 	public Logger lookupLogger() {
 		return logger;
 	}
 	
 	public ElementReference() {
 	}
 	
  /*@
    @ public behavior
    @
    @ pre name != null;
    @
    @ post getName().equals(name);
    @*/
 	public ElementReference(String name) {
 		setSignature(new SimpleNameSignature(name));
 	}
 	
  /*@
    @ public behavior
    @
    @ pre name != null;
    @
    @ post signature().equals(signature);
    @*/
 	public ElementReference(SimpleNameSignature signature) {
 		setSignature(signature);
 	}
 	
 
  /*@
    @ public behavior
    @
    @ post \result != null;
    @*/
   public String getName() {
     return signature().name();
   }
   
   private SingleAssociation<ElementReference,SimpleNameSignature> _signature = new SingleAssociation<ElementReference,SimpleNameSignature>(this);
 
   /**
    * Return the signature of this element reference.
    */
   public SimpleNameSignature signature() {
   	return _signature.getOtherEnd();
   }
   
  /*@
    @ public behavior
    @
    @ pre name != null;
    @
    @ post getName() == name;
    @*/
   public void setSignature(SimpleNameSignature signature) {
   	if(signature == null) {
   		_signature.connectTo(null);
   	} else {
   	  _signature.connectTo(signature.parentLink());
   	}
   }
  
   private D _cache;
   
   protected D getCache() {
   	if(Config.cacheElementReferences() == true) {
   	  return _cache;
   	} else {
   		return null;
   	}
   }
   
   protected void setCache(D value) {
   	if(! value.isDerived()) {
     	if(Config.cacheElementReferences() == true) {
     		_cache = value;
     	}
   	} else {
   		_cache = null;
   	}
   }
   
  public List<Element> children() {
  	return Util.createNonNullList(signature());
  }
  
 //  public abstract D getElement() throws LookupException;
 }
