 package org.spoofax.interpreter.library.java;
 
import static org.spoofax.terms.Term.isTermInt;

 import java.util.HashMap;
 import java.util.Map;
 
 import org.spoofax.interpreter.core.IContext;
 import org.spoofax.interpreter.library.AbstractStrategoOperatorRegistry;
 import org.spoofax.interpreter.terms.IStrategoAppl;
 import org.spoofax.interpreter.terms.IStrategoConstructor;
 import org.spoofax.interpreter.terms.IStrategoInt;
 import org.spoofax.interpreter.terms.IStrategoReal;
 import org.spoofax.interpreter.terms.IStrategoString;
 import org.spoofax.interpreter.terms.IStrategoTerm;
 import org.spoofax.interpreter.terms.ITermFactory;
 
 public class JFFLibrary extends AbstractStrategoOperatorRegistry {
 
 	public static final String REGISTRY_NAME = "JFF";
 	
 	private Map<Integer, Object> objectWrappers;
 	private int objectCounter;
 	private IStrategoConstructor objWrapCtor;
 	private ITermFactory termFactory; 
 	
 	public JFFLibrary(ITermFactory termFactory) {
 		this.termFactory = termFactory;
 		initRegistry();
 		init();
 	}
 	
 	public String getOperatorRegistryName() {
     	 return REGISTRY_NAME;
 	}
 	
 	private void initRegistry() {
 		
 		add(new JFF_find_class());
 		add(new JFF_find_primitive_type());
 		add(new JFF_new_instance());
 		add(new JFF_get_constructor());
 		add(new JFF_invoke_method());
 		add(new JFF_get_method());
 	}
 	
 	public void init() {
 		
 		objectCounter = 0;
 		if(objectWrappers != null) {
 			objectWrappers.clear();
 		} else {
 			objectWrappers = new HashMap<Integer, Object>();
 		}
 		
 		objWrapCtor = termFactory.makeConstructor("ObjWrap", 1);
 	}
 	
     public static JFFLibrary instance(IContext env) {
         return (JFFLibrary)env.getOperatorRegistry(REGISTRY_NAME);
     }
 
 	public IStrategoTerm wrapObject(Object obj) {
 		int idx = objectCounter++;
 		objectWrappers.put(idx, obj);
 		return termFactory.makeAppl(objWrapCtor, termFactory.makeInt(idx));
 	}
 
 	public Object unwrapObject(IStrategoAppl term) {
 		IStrategoTerm t = term.getSubterm(0);
		if(!(isTermInt(t)))
 			return null;
 		return objectWrappers.get(((IStrategoInt)t).intValue());
 	}
 
 
 	public Object unpackTerm(IStrategoTerm term) {
 		switch(term.getTermType()) {
 		case IStrategoTerm.INT:
 			return ((IStrategoInt)term).intValue();
 		case IStrategoTerm.REAL:
 			return ((IStrategoReal)term).realValue();
 		case IStrategoTerm.STRING:
 			return ((IStrategoString)term).stringValue();
 		case IStrategoTerm.APPL:
 			return unwrapObject((IStrategoAppl)term);
 		default:
 			return null;
 		}
 	}
 }
