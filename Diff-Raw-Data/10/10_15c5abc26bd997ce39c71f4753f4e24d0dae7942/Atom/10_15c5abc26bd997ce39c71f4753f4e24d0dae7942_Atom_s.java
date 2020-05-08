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
 //----------------------------------------------------------------------------
 
 package jason.asSyntax;
 
 import jason.*;
 import java.util.*;
 import java.util.logging.*;
 
 /**
  * Represents an atom (a structure with no arguments), it is an
  * immutable object.  It extends Literal, so can be used in place of a
  * Literal, but does not allow operations on terms and annotations.
  */
 public final class Atom extends Literal {
 
     private static final long serialVersionUID = 1L;
 
     static private Logger logger = Logger.getLogger(Atom.class.getName());
 
     public Atom(String functor) {
         super(functor);
 		if (functor == null) {
			logger.info("functor of an Atom should not be null!");
 		}
     }
     
     public Object clone() {
 		return this; // since this object is immutable
     }
     
 	//
 	// override structure methods
 	//
     
 	@Override
     public void addTerm(Term t) {
 		logger.log(Level.SEVERE, "atom error!",new JasonException("atom has no terms!"));
     }
     
 	@Override
     public void addTerms(List<Term> l) {
 		logger.log(Level.SEVERE, "atom error!",new JasonException("atom has no terms!"));
     }
     
 	@Override
     public void setTerms(List<Term> l) {
 		logger.log(Level.SEVERE, "atom error!",new JasonException("atom has no terms!"));
     }
     
 	@Override
     public void setTerm(int i, Term t) {
 		logger.log(Level.SEVERE, "atom error!",new JasonException("atom has no terms!"));
     }
      
 	@Override
     public int getTermsSize() {
 		return 0;
     }
     
 	@Override
 	public boolean isAtom() {
 		return true;
 	}
 
 	@Override
     public boolean isGround() {
         return true;
     }
 
     @Override
     public boolean equals(Object o) {
         if (o == null) return false;
         if (o == this) return true;
         if (o instanceof Structure) {
             return getFunctor().equals(((Structure)o).getFunctor());
         }
         return false;
     }
 }
