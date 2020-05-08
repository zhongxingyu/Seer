 package org.spoofax.interpreter.adapter.ecj;
 
 import org.spoofax.interpreter.terms.IStrategoAppl;
 import org.spoofax.interpreter.terms.IStrategoConstructor;
 import org.spoofax.interpreter.terms.IStrategoInt;
 import org.spoofax.interpreter.terms.IStrategoList;
 import org.spoofax.interpreter.terms.IStrategoReal;
 import org.spoofax.interpreter.terms.IStrategoString;
 import org.spoofax.interpreter.terms.IStrategoTerm;
 import org.spoofax.interpreter.terms.IStrategoTuple;
 import org.spoofax.interpreter.terms.ITermPrinter;
 
 public class ECJAnnoWrapper implements IStrategoTerm, IStrategoList, IStrategoAppl, IStrategoInt, IStrategoReal, IStrategoString, IStrategoTuple {
 
 	private final IStrategoTerm wrappee;
 	private final IStrategoList annotations;
 	
 	public ECJAnnoWrapper(IStrategoTerm wrappee, IStrategoList annotations) {
 		this.wrappee = wrappee;
 		this.annotations = annotations;
 	}
     
 	public int getStorageType() {
 		return MUTABLE; // does not cache its hashCode() method
 	}
 	
 	public IStrategoTerm[] getAllSubterms() {
 		return wrappee.getAllSubterms();
 	}
 
 	public IStrategoList getAnnotations() {
 		return annotations;
 	}
 
 	public IStrategoTerm getSubterm(int index) {
 		return wrappee.getSubterm(index);
 	}
 
 	public int getSubtermCount() {
 		return wrappee.getSubtermCount();
 	}
 
 	public int getTermType() {
 		return wrappee.getTermType();
 	}
 
 	public boolean match(IStrategoTerm second) {
 		return wrappee.match(second) && annotations.match(second.getAnnotations()); 
 	}
 
 	public void prettyPrint(ITermPrinter pp) {
 		wrappee.prettyPrint(pp);
 		pp.print("{");
 		annotations.prettyPrint(pp);
 		pp.print("}");
 	}
 
 	public IStrategoTerm get(int index) {
 		return ((IStrategoList) wrappee).get(index);
 	}
 
 	public IStrategoTerm head() {
 		return ((IStrategoList) wrappee).head();
 	}
 
 	public boolean isEmpty() {
 		return ((IStrategoList) wrappee).isEmpty();
 	}
 
 	public IStrategoList prepend(IStrategoTerm prefix) {
 		return ((IStrategoList) wrappee).prepend(prefix);
 	}
 
 	public int size() {
 		return ((IStrategoList) wrappee).size();
 	}
 
 	public IStrategoList tail() {
 		return ((IStrategoList) wrappee).tail();
 	}
 
 	public IStrategoTerm[] getArguments() {
		return ((IStrategoAppl) wrappee).getArguments();
 	}
 
 	public IStrategoConstructor getConstructor() {
 		return ((IStrategoAppl) wrappee).getConstructor();
 	}
 
 	public int intValue() {
 		return ((IStrategoInt) wrappee).intValue();
 	}
 
 	public double realValue() {
 		return ((IStrategoReal) wrappee).realValue();
 	}
 
 	public String stringValue() {
 		return ((IStrategoString) wrappee).stringValue();
 	}
 
 	public IStrategoTerm getWrappee() {
 		return wrappee;
 	}
 
 }
