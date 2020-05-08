 package be.kuleuven.cs.distrinet.chameleon.core.declaration;
 
 import java.util.List;
 
 import be.kuleuven.cs.distrinet.chameleon.core.element.Element;
 import be.kuleuven.cs.distrinet.chameleon.core.lookup.LookupException;
 import be.kuleuven.cs.distrinet.chameleon.core.validation.Valid;
 import be.kuleuven.cs.distrinet.chameleon.core.validation.Verification;
 import be.kuleuven.cs.distrinet.chameleon.util.Util;
 
 /**
  * A class of signatures that consist of a simple name.
  * 
  * @author Marko van Dooren
  */
 public class SimpleNameSignature extends Signature {
 
   public SimpleNameSignature(String name) {
     setName(name);
   }
   
   public String name() {
     return _name;
   }
   
   public void setName(String name) {
  	if(name == null) {
  		throw new IllegalArgumentException();
  	}
     _name = name;
   }
   
   private String _name;
 
   @Override
 	public boolean uniSameAs(Element other) throws LookupException {
 		boolean result = false;
 		if(other instanceof SimpleNameSignature) {
 			SimpleNameSignature sig = (SimpleNameSignature) other;
 			String name = name();
 			result = name != null && name.equals(sig.name());
 		}
 		return result;
 	}
   
   @Override
   public int hashCode() {
   	return _name.hashCode();
   }
 
 	@Override
 	public SimpleNameSignature cloneSelf() {
     return new SimpleNameSignature(name());
 	}
 
 	@Override
 	public Verification verifySelf() {
 		if(_name == null) {
 			return new SignatureWithoutName(this);
 		} else {
 			return Valid.create();
 		}
 	}
 	
 	@Override
 	public String toString() {
 		return _name;
 	}
 
 	@Override
 	public Signature lastSignature() {
 		return this;
 	}
 
 	@Override
 	public List<Signature> signatures() {
 		return Util.createSingletonList((Signature)this);
 	}
 
 	@Override
 	public int length() {
 		return 1;
 	}
 
 }
