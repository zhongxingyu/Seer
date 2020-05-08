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
 // http://www.csc.liv.ac.uk/~bordini
 // http://www.inf.furb.br/~jomi
 //----------------------------------------------------------------------------
 
 package jason.asSyntax;
 
 import jason.asSyntax.parser.as2j;
 
 import java.io.Serializable;
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 /**
  * Represents a Term (a predicate parameter), e.g.: val(10,x(3)).
  */
 public class Term implements Cloneable, Comparable, Serializable {
 
 	private String functor = null;
 	private List   terms;
 
 	public Term() {
 	}
 
 	public Term(String fs) {
 		if (fs != null && Character.isUpperCase(fs.charAt(0))) {
 			System.err.println("Warning: are you sure to create a term that begins with upper case ("+fs+")? Should it be a VarTerm?");
 		}
 		setFunctor(fs);
 	}
 
 	public Term(Term t) {
 		set(t);
 	}
 
 	public static Term parse(String sTerm) {
 		as2j parser = new as2j(new StringReader(sTerm));
 		try {
 			return parser.t(); // parse.t() may returns a Pred/List...
 		} catch (Exception e) {
 			System.err.println("Error parsing term " + sTerm);
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	/** copy all attributes of <i>t</i> */
 	public void set(Term t) {
 		try {
 			setFunctor(t.functor);
 			terms = t.getDeepCopyOfTerms();
 		} catch (Exception e) {
 			System.err.println("Error setting value for term ");
 			e.printStackTrace();
 		}
 	}
 
 	public void setFunctor(String fs) {
 		functor = fs;
 		functorArityBak = null;
 	}
 
 	public String getFunctor() {
 		return functor;
 	}
 
 	protected String functorArityBak = null; // to not compute it all the time (is is called many many times)
 	
 	/** returns <functor symbol> "/" <arity> */
 	public String getFunctorArity() {
 		if (functorArityBak == null) {
 			if (terms == null) {
 				functorArityBak = getFunctor() + "/0";
 			} else {
 				functorArityBak = getFunctor() + "/" + getTermsSize();
 			}
 		}
 		return functorArityBak;
 	}
 
 	public int hashCode() {
 		return getFunctorArity().hashCode();
 	}
 
 	/** returns the i-th term */
 	public Term getTerm(int i) {
 		if (terms != null && terms.size() > i) {
 			return (Term)terms.get(i);
 		} else {
 			return null;
 		}
 	}
 
 	public void addTerm(Term t) {
 		if (terms == null)
 			terms = new ArrayList();
 		terms.add(t);
 		functorArityBak = null;
 	}
 
 	public int getTermsSize() {
 		if (terms != null) {
 			return terms.size();
 		} else {
 			return 0;
 		}
 	}
 	public List getTerms() {
 		return terms;
 	}
 	
 	public Term[] getTermsArray() {
 		Term ts[] = null;
 		if (getTermsSize() == 0) {
 			ts = new Term[0];
 		} else {
 			ts = new Term[getTermsSize()];
 			for (int i=0; i<getTermsSize(); i++) { // use "for" instead of iterator for ListTerm compatibility
 				ts[i] = getTerm(i);
 			}
 		}
 		return ts;
 	}
 
 	public boolean isVar() {
 		//if (funcSymb == null) {
 			return false;
 		//} else {
 		//	return Character.isUpperCase(funcSymb.charAt(0));
 		//}
 	}
 
 	public boolean isList() {
 		return false;
 	}
 	public boolean isString() {
 		return false;
 	}
 	public boolean isInternalAction() {
 		return false;
 	}
 	public boolean isExpr() {
 		return false;
 	}
 
 	public boolean isGround() {
 		for (int i=0; i<getTermsSize(); i++) {
 			if (!getTerm(i).isGround()) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	public boolean hasVar(Term t) {
 		if (this.equals(t))
 			return true;
 		for (int i=0; i<getTermsSize(); i++) {
 			if (getTerm(i).hasVar(t)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public boolean equals(Object t) {
 		if (t == null)	return false;
 		
 		
 		// it is a var, uses var's equals
 		try {
 			VarTerm vt = (VarTerm)t;
 			//System.out.println(this.funcSymb+" equals1 "+vt.funcSymb);
 			return vt.equals(this);
 		} catch (Exception e) {}
 
 		try {
 			Term tAsTerm = (Term)t;
 			//System.out.println(this.funcSymb+" equals2 "+tAsTerm.funcSymb);
 			if (functor == null && tAsTerm.functor != null) {
 				return false;
 			}
 			if (functor != null && !functor.equals(tAsTerm.functor))
 				return false;
 			if (terms == null && tAsTerm.terms == null)
 				return true;
 			if (terms == null || tAsTerm.terms == null)
 				return false;
 			if (terms.size() != tAsTerm.terms.size())
 				return false;
 
 			for (int i=0; i<getTermsSize(); i++) {
 				if (!getTerm(i).equals(tAsTerm.getTerm(i))) {
 					return false;
 				}
 			}
 			return true;
 		} catch (ClassCastException e) {
 			return false;
 		}
 	}
 
 	public int compareTo(Object t) {
 		int c;
 		if (((Term) t).functor == null)
 			return 1;
 		if (functor == null)
 			return -1;
 		c = functor.compareTo(((Term) t).functor);
 		if (c != 0)
 			return c;
 		if (terms == null && ((Term) t).terms == null)
 			return 0;
 		if (terms == null)
 			return -1;
 		if (((Term) t).terms == null)
 			return 1;
 		if (terms.size() < ((Term) t).terms.size())
 			return -1;
 		else if (terms.size() > ((Term) t).terms.size())
 			return 1;
 		Iterator i = terms.iterator();
 		Iterator j = ((Term) t).terms.iterator();
 		while (i.hasNext() && j.hasNext()) {
 			c = ((Term) i.next()).compareTo((Term) j.next());
 			if (c != 0)
 				return c;
 		}
 		return 0;
 	}
 
 	/** make a deep copy of the terms */
 	public Object clone() {
 		return new Term(this);
 	}
 
 	protected List getDeepCopyOfTerms() {
 		if (terms == null) {
 			return null;
 		}
 		List l = new ArrayList(terms.size());
 		Iterator i = terms.iterator();
 		while (i.hasNext()) {
 			Term ti = (Term)i.next();
 			l.add(ti.clone());
 		}
 		return l;
 	}
 
 	public double toDouble() {
 		try {
			return Double.parseDouble(getFunctor());
 		} catch (Exception e) {
 			System.err.println("Error converting to double " + functor);
 			e.printStackTrace();
 			return 0;
 		}
 	}
 	
 	public String toString() {
 		StringBuffer s = new StringBuffer();
 		if (functor != null) {
 			s.append(functor);
 		}
 		if (terms != null) {
 			s.append("(");
 			Iterator i = terms.iterator();
 			while (i.hasNext()) {
 				s.append((Term) i.next());
 				if (i.hasNext())
 					s.append(",");
 			}
 			s.append(")");
 		}
 		return s.toString();
 	}
 }
