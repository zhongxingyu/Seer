 // ----------------------------------------------------------------------------
 // Copyright (C) 2003 Rafael H. Bordini, Jomi F. Hubner, et al.
 // 
 // This library is free software; you can redistribute it and/or
 // modify it under the terms of the GNU Lesser General Public
 // License as published by the Free Software Foundation; either
 // version 2.1 of the License, or (at your option) any later version.
 // 
 // This library is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 // Lesser General Public License for more details.
 // 
 // You should have received a copy of the GNU Lesser General Public
 // License along with this library; if not, write to the Free Software
 // Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 // 
 // To contact the authors:
 // http://www.dur.ac.uk/r.bordini
 // http://www.inf.furb.br/~jomi
 //
 // CVS information:
 //   $Date$
 //   $Revision$
 //   $Log$
//   Revision 1.15  2005/12/23 00:48:23  jomifred
//   StringTerm is now an interface implemented by StringTermImpl
 //
 //
 //----------------------------------------------------------------------------
 
 package jason.asSyntax;
 
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ListIterator;
 
 import org.apache.log4j.Logger;
 
 
 /**
  * Represents a variable Term: like X (starts with upper case). 
  * It may have a value, afert Unifier.apply.
  * 
  * @author jomi
  */
 public class VarTerm extends Literal implements NumberTerm, ListTerm, StringTerm {
 	static private Logger logger = Logger.getLogger(VarTerm.class.getName());
 	
 	private Term value = null;
 	
 	public VarTerm() {
 		super();
 	}
 
 	public VarTerm(String s) {
 		if (s != null && Character.isLowerCase(s.charAt(0))) {
 			logger.warn("Are you sure you want to create a VarTerm that begins with lowercase ("+s+")? Should it be a Term instead?");			
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
 	
 	/** grounds a variable, set a value for this var (e.g. X = 10; Y = a(b,c); ...) */
 	public boolean setValue(Term vl) {
 		try {
 			// If vl is a Var, find out a possible loop
 			VarTerm vlvl = (VarTerm)((VarTerm)vl).value; // not getValue! (use the "real" value)
 			while (vlvl != null) {
 				if (vlvl == this) {
 					logger.error("Attempted loop in VarTerm values of "+this.getFunctor());
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
 	 * When a var has another var as value, returns the last var in the chain.
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
 	 * if value is also a var, returns the value of this latter var
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
 
 	
 	public String getFunctorArity() {
 		if (value != null)
 			return value.getFunctorArity();
 		else 
 			return null;
 	}
 
 	public int hashCode() {
 		if (value != null)
 			return value.hashCode();
 		else 
 			return super.hashCode();
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
 
 	public void addTerms(List l) {
 		if (value != null) {
 			value.addTerms(l);
 		}
 	}
 
 	public Term[] getTermsArray() {
 		if (value == null) {
 			return null;
 		} else {
 			return value.getTermsArray();
 		}
 	}
 
 	public boolean isInternalAction() {
 		return value != null && getValue().isInternalAction();
 	}
 	
 	public boolean isList() {
 		return value != null && getValue().isList();
 	}
 	public boolean isString() {
 		return value != null && getValue().isString();
 	}
 	public boolean isNumber() {
 		return value != null && getValue().isNumber();
 	}
 	public boolean isPred() {
 		return value != null && getValue().isPred();
 	}
 	public boolean isLiteral() {
 		return value != null && getValue().isLiteral();
 	}
 	
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
 
 	
 	// ----------
 	// Pred methods overridden
 	// 
 	// in case this VarTerm has a value, use value's methods
 	// ----------
 
 	public void addAnnot(int index, Term t) {
 		if (value != null && getValue().isPred())
 			((Pred)getValue()).addAnnot(index, t);
 	}
 
 	public void importAnnots(Pred p) {
 		if (value != null && getValue().isPred())
 			((Pred)getValue()).importAnnots(p);
 	}
 
 	public void addAnnot(Term t) {
 		if (value != null && getValue().isPred())
 			((Pred)getValue()).addAnnot(t);
 	}
 
 	public void addAnnots(List l) {
 		if (value != null && getValue().isPred())
 			((Pred)getValue()).addAnnots(l);
 	}
 
 	public void clearAnnots() {
 		if (value != null && getValue().isPred())
 			((Pred)getValue()).clearAnnots();
 	}
 
 	public void delAnnot(Pred p) {
 		if (value != null && getValue().isPred())
 			((Pred)getValue()).delAnnot(p);
 	}
 
 	public void delAnnot(Term t) {
 		if (value != null && getValue().isPred())
 			((Pred)getValue()).delAnnot(t);
 	}
 
 	public boolean hasAnnot(Term t) {
 		return value != null && getValue().isPred() && ((Pred)getValue()).hasAnnot(t);
 	}
 
 	public boolean hasNoAnnot() {
 		return value != null && getValue().isPred() && ((Pred)getValue()).hasNoAnnot();
 	}
 
 	public List getAnnots() {
 		if (value != null && getValue().isPred())
 			return ((Pred)getValue()).getAnnots();
 		else 
 			return null;
 	}
 
 	public void addSource(String s) {
 		if (value != null && getValue().isPred())
 			((Pred)getValue()).addSource(s);
 	}
 
 	public void addSource(Term t) {
 		if (value != null && getValue().isPred())
 			((Pred)getValue()).addSource(t);
 	}
 
 	public boolean delSource(Term s) {
 		return value != null && getValue().isPred() && ((Pred)getValue()).delSource(s);
 	}
 
 	public void delSources() {
 		if (value != null && getValue().isPred())
 			((Pred)getValue()).delSources();
 	}
 
 	public ListTerm getSources() {
 		if (value != null && getValue().isPred())
 			return ((Pred)getValue()).getSources();
 		else 
 			return null;
 	}
 
 	public boolean hasSource() {
 		return value != null && getValue().isPred() && ((Pred)getValue()).hasSource();
 	}
 
 	public boolean hasSource(Term s) {
 		return value != null && getValue().isPred() && ((Pred)getValue()).hasSource(s);
 	}
 	
 	
 	// ----------
 	// Literal methods overridden
 	// 
 	// in case this VarTerm has a value, use value's methods
 	// ----------
 	
 	public boolean negated() {
 		return value != null && getValue().isLiteral() && ((Literal)getValue()).negated();
 	}
 	
 	
 	// ----------
 	// ArithmeticExpression methods overridden
 	// Interface NumberTerm
 	// ----------
 
 	public double solve() {
 		if (hasValue() && value.isNumber()) {
 			return ((NumberTermImpl)value).getValue();
 		} else {
 			logger.error("Error getting numerical value of VarTerm "+this);
 		}
 		return 0;
 	}
 
 	
 	// ----------
 	//
 	// ListTerm methods overridden
 	// 
 	// ----------
 
 
 	public void add(int index, Object o) {
 		if (value != null && getValue().isList())
 			((ListTerm)getValue()).add(index, o);
 	}
 	public boolean add(Object o) {
 		return value != null && getValue().isList() && ((ListTerm)getValue()).add(o);
 	}
 	public boolean addAll(Collection c) {
 		return value != null && getValue().isList() && ((ListTerm)getValue()).addAll(c);
 	}
 	public boolean addAll(int index, Collection c) {
 		return value != null && getValue().isList() && ((ListTerm)getValue()).addAll(index, c);
 	}
 	public void clear() {
 		if (value != null && getValue().isList())
 			((ListTerm)getValue()).clear();
 	}
 	public boolean contains(Object o) {
 		return value != null && getValue().isList() && ((ListTerm)getValue()).contains(o);
 	}
 	public boolean containsAll(Collection c) {
 		return value != null && getValue().isList() && ((ListTerm)getValue()).containsAll(c);
 	}
 	public Object get(int index) {
 		if (value != null && getValue().isList())
 			return ((ListTerm)getValue()).get(index);
 		else 
 			return null;
 	}
 	public int indexOf(Object o) {
 		if (value != null && getValue().isList())
 			return ((ListTerm)getValue()).indexOf(o);
 		else 
 			return -1;
 	}
 	public int lastIndexOf(Object o) {
 		if (value != null && getValue().isList())
 			return ((ListTerm)getValue()).lastIndexOf(o);
 		else 
 			return -1;
 	}
 	public Iterator iterator() {
 		if (value != null && getValue().isList())
 			return ((ListTerm)getValue()).iterator();
 		else 
 			return null;
 	}
 	public ListIterator listIterator() {
 		if (value != null && getValue().isList())
 			return ((ListTerm)getValue()).listIterator();
 		else 
 			return null;
 	}
 	public ListIterator listIterator(int index) {
 		if (value != null && getValue().isList())
 			return ((ListTerm)getValue()).listIterator(index);
 		else 
 			return null;
 	}
 	public Object remove(int index) {
 		if (value != null && getValue().isList())
 			return ((ListTerm)getValue()).remove(index);
 		else 
 			return null;
 	}
 	public boolean remove(Object o) {
 		if (value != null && getValue().isList())
 			return ((ListTerm)getValue()).remove(o);
 		else 
 			return false;
 	}
 	public boolean removeAll(Collection c) {
 		if (value != null && getValue().isList())
 			return ((ListTerm)getValue()).removeAll(c);
 		else 
 			return false;
 	}
 	public boolean retainAll(Collection c) {
 		if (value != null && getValue().isList())
 			return ((ListTerm)getValue()).retainAll(c);
 		else 
 			return false;
 	}
 	public Object set(int index, Object o) {
 		if (value != null && getValue().isList())
 			return ((ListTerm)getValue()).set(index, o);
 		else 
 			return null;
 	}
 	public List subList(int arg0, int arg1) {
 		if (value != null && getValue().isList())
 			return ((ListTerm)getValue()).subList(arg0, arg1);
 		else 
 			return null;
 	}
 	public Object[] toArray() {
 		if (value != null && getValue().isList())
 			return ((ListTerm)getValue()).toArray();
 		else 
 			return null;
 	}
 	public Object[] toArray(Object[] arg0) {
 		if (value != null && getValue().isList())
 			return ((ListTerm)getValue()).toArray(arg0);
 		else 
 			return null;
 	}
 
 	// from ListTerm
 	
 	public ListTerm add(int index, Term t) {
 		if (value != null && getValue().isList())
 			return ((ListTerm)getValue()).add(index, t);
 		else 
 			return null;
 	}
 	public ListTerm add(Term t) {
 		if (value != null && getValue().isList())
 			return ((ListTerm)getValue()).add(t);
 		else 
 			return null;
 	}
 	public ListTerm concat(ListTerm lt) {
 		if (value != null && getValue().isList())
 			return ((ListTerm)getValue()).concat(lt);
 		else 
 			return null;
 	}
 	public List getAsList() {
 		if (value != null && getValue().isList())
 			return ((ListTerm)getValue()).getAsList();
 		else 
 			return null;
 	}
 
 	public ListTerm getLast() {
 		if (value != null && getValue().isList())
 			return ((ListTerm)getValue()).getLast();
 		else 
 			return null;
 	}
 
 	public ListTerm getNext() {
 		if (value != null && getValue().isList())
 			return ((ListTerm)getValue()).getNext();
 		else 
 			return null;
 	}
 
 	public Term getTail() {
 		if (value != null && getValue().isList())
 			return ((ListTerm)getValue()).getTail();
 		else 
 			return null;
 	}
 
 	public Term getTerm() {
 		if (value != null && getValue().isList())
 			return ((ListTerm)getValue()).getTerm();
 		else 
 			return null;
 	}
 
 	public boolean isEmpty() {
 		return value != null && getValue().isList() && ((ListTerm)getValue()).isEmpty();
 	}
 
 	public boolean isEnd() {
 		return value != null && getValue().isList() && ((ListTerm)getValue()).isEnd();
 	}
 
 	public boolean isTail() {
		return value != null && getValue().isList();// && ((ListTerm)getValue()).isTail();
 	}
 
 	public Iterator listTermIterator() {
 		if (value != null && getValue().isList())
 			return ((ListTerm)getValue()).listTermIterator();
 		else 
 			return null;
 	}
 
 	public void setTail(Term t) {
 		if (value != null && getValue().isList())
 			((ListTerm)getValue()).setTail(t);
 	}
 
 	public int size() {
 		if (value != null && getValue().isList())
 			return ((ListTerm)getValue()).size();
 		else 
 			return -1;
 	}
 	
 	// -----------------------
 	// StringTerm interface implementation
 	// -----------------------
 	
 	public void setString(String s) {
 		if (value != null && getValue().isString())
 			((StringTerm)getValue()).setString(s);
 	}
 	public String getString() {
 		if (value != null && getValue().isString())
 			return ((StringTerm)getValue()).getString();
 		else
 			return null;
 	}
 	public int length() {
 		if (value != null && getValue().isString())
 			return ((StringTerm)getValue()).length();
 		else
 			return -1;
 	}
 
 }
