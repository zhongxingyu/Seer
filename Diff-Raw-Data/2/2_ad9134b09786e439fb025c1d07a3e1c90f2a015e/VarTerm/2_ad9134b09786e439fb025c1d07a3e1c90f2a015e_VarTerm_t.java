 package jason.asSyntax;
 
 import java.util.List;
 
 
 /**
  * Represents a variable Term: like X (starts with upper case). 
  * It may have a value, afert Unifier.apply.
  * 
  * @author jomi
  */
 public class VarTerm extends Term {
 	
 	// TODO: do not use super.functor to store the var name
 	
 	private Term value = null;
 	
 	public VarTerm() {
 		super();
 	}
 
 	public VarTerm(String s) {
 		if (s != null && Character.isLowerCase(s.charAt(0))) {
 			System.err.println("Warning: are you sure to create a VarTerm that begins with lower case ("+s+")? Should it be a Term?");			
 		}
 		setFunctor(s);
 	}
 	
 	public Object clone() {
 		// do not call constructor with term parameter!
 		VarTerm t = new VarTerm();
		t.setFunctor(super.getFunctor());
 		if (value != null) {
 			t.value = (Term)value.clone();
 		}
 		return t;
 	}
 	
 	public boolean isVar() {
 		return !isGround();
 	}
 
 	public boolean isGround() {
 		return getValue() != null;
 	}
 	
 	public boolean setValue(Term vl) {
 		try {
 			// If vl is a Var, find out a possible loop
 			VarTerm vlvl = (VarTerm)((VarTerm)vl).value; // not getValue! (use the "real" value)
 			while (vlvl != null) {
 				if (vlvl == this) {
 					System.err.println("Error: trying to make a loop in VarTerm values of "+this.getFunctor());
 					return false;
 				}
 				vlvl = (VarTerm)vlvl.value;
 			}
 		} catch (Exception e) {}
 		value = vl;
 		return true;
 	}
 	
 	/** returns true if this var has a value */
 	public boolean hasValue() {
 		return value != null;
 	}
 	
 	/** 
 	 * When a var has a var as value, returns the last var in the chain.
 	 * Otherwise, return this. 
 	 * The returned VarTerm is normally used to set/get a value for the var. 
 	 */
 	public VarTerm getLastVarChain() {
 		try {
 			// if value is a var, return it
 			return ((VarTerm)value).getLastVarChain();
 		} catch (Exception e) {}
 		return this;
 	}
 	
 	/** 
 	 * returns the value of this var. 
 	 * if value is a var, returns the value of these var
 	 * 
 	 * @return
 	 */
 	public Term getValue() {
 		return getLastVarChain().value;
 	}
 
 	public boolean equals(Object t) {
 		if (t == null)
 			return false;
 		try {
 			Term tAsTerm = (Term)t;
 			Term vl = getValue();
 			//System.out.println("cheking equals form "+tAsTerm.funcSymb+"="+this.funcSymb+" my value "+vl);
 			if (vl == null) {
 				// is other also a var? (its value must also be null)
 				try {
 					VarTerm tAsVT = (VarTerm)t;
 					if (tAsVT.getValue() != null) {
 						//System.out.println("returning false*");
 						return false;
 					}
 
 					// no value, the var names must be equal
 					//System.out.println("will return "+funcSymb.equals(tAsTerm.funcSymb));
 					return getFunctor().equals(tAsTerm.getFunctor());
 				} catch (Exception e) {
 					return false;
 				}
 				
 			} else {
 				// campare the values
 				return vl.equals(t);
 			}
 		} catch (ClassCastException e) {
 		}
 		return false;
 	}
 	
 	
 	// ----------
 	// Term methods overridden
 	// 
 	// in case this VarTerm has a value, use value's methods
 	// ----------
 	
 	public String getFunctor() {
 		if (value == null) {
 			return super.getFunctor();
 		} else {
 			return getValue().getFunctor();
 		}
 	}
 
 
 	public Term getTerm(int i) {
 		if (value == null) {
 			return null;
 		} else {
 			return getValue().getTerm(i);
 		}
 	}
 
 	public void addTerm(Term t) {
 		if (value != null) {
 			getValue().addTerm(t);
 		}
 	}
 
 	public int getTermsSize() {
 		if (value == null) {
 			return 0;
 		} else {
 			return getValue().getTermsSize();
 		}
 	}
 
 	public List getTerms() {
 		if (value == null) {
 			return null;
 		} else {
 			return getValue().getTerms();
 		}
 	}
 	
 	
 	/*
 	 * TODO the below is not ok, see the following code where
 	 * x is a VarTerm with a List value!
 	 *  
 	 * if (x.isList()) {
 	 *    ListTerm lt = (ListTerm)x;
 	 *    
 	 * To solve it, we must use ListTerm, StringTerm, ... interfaces
 	 *    
 	public boolean isList() {
 		if (value == null) {
 			return false;
 		} else {
 			return getValue().isList();
 		}
 	}
 	
 	public boolean isString() {
 		if (value == null) {
 			return false;
 		} else {
 			return getValue().isString();
 		}
 	}
 	public boolean isInternalAction() {
 		if (value == null) {
 			return false;
 		} else {
 			return getValue().isInternalAction();
 		}
 	}
 	*/
 	
 	public boolean hasVar(Term t) {
 		if (value == null) {
 			return super.hasVar(t);
 		} else {
 			return getValue().hasVar(t);
 		}
 	}
 	
 	public String toString() {
 		if (value == null) {
 			// no value, the var name must be equal
 			return getFunctor();
 		} else {
 			// campare the values
 			return getValue().toString();
 		}
 	}
 }
