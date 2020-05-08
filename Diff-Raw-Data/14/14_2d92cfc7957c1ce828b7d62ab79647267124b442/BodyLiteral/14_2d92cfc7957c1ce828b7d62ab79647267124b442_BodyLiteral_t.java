 //----------------------------------------------------------------------------
 // Copyright (C) 2003  Rafael H. Bordini, Jomi F. Hubner, et al.
 // 
 // This library is free software; you can redistribute it and/or
 // modify it under the terms of the GNU Lesser General Public
 // License as published by the Free Software Foundation; either
 // version 2.1 of the License, or (at your option) any later version.
 // 
 // This library is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 // Lesser General Public License for more details.
 // 
 // You should have received a copy of the GNU Lesser General Public
 // License along with this library; if not, write to the Free Software
 // Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 // 
 // To contact the authors:
 // http://www.dur.ac.uk/r.bordini
 // http://www.inf.furb.br/~jomi
 //
 //----------------------------------------------------------------------------
 
 
 package jason.asSyntax;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 
 public class BodyLiteral implements Cloneable {
     
 	public enum BodyType { 
 		action         { public String toString() { return ""; } },  
 		internalAction { public String toString() { return ""; } },  
 		achieve        { public String toString() { return "!"; } }, 
 		test           { public String toString() { return "?"; } }, 
 		addBel         { public String toString() { return "+"; } }, 
 		delBel         { public String toString() { return "-"; } },
 		delAddBel      { public String toString() { return "-+"; } },
 		achieveNF      { public String toString() { return "!!"; } }, 
 		constraint     { public String toString() { return ""; } }
 	}
 
 	Term    term;
 	BodyType    formType;
 
     public BodyLiteral(BodyType t, Term l) {
         term = (Term)l.clone();
         formType = t;
         if (l.isInternalAction()) {
         		formType = BodyType.internalAction;
         }
     }
     public BodyLiteral(RelExprTerm re) {
         term = (Term)re.clone();
         formType = BodyType.constraint;
     }
 
     public BodyType getType() {
         return formType;
     }
 	
 	public Term getTerm() {
 		return term;
 	}
     
 	// used with arithmetic expressions
 	public void addTerm(Term t) {
 		term.addTerm(t);
 	}
 	
     public boolean equals(Object o) {
 	    	try {
 	    		BodyLiteral b = (BodyLiteral) o;
 	    		return formType==b.formType && term.equals(b.term);
 	    	} catch (Exception e) {
 	    		return false;
 	    	}
     }
 
    private static final PredicateIndicator SEND_PI = new PredicateIndicator(".send",4);
     public boolean isAsk() {
 	    	if (! term.getPredicateIndicator().equals(SEND_PI)) {
 	    		return false;
 	    	}
 	    	if (term.getTerm(1).toString().startsWith("ask")) {
 	    		return true;
 	    	} else {
 	    		return false;
 	    	}
     }
     
     public Object clone() {
 		return new BodyLiteral(formType, term);
     }
 
     public String toString() {
         return formType + term.toString();
     }
 
     /** get as XML */
     public Element getAsDOM(Document document) {
         Element u = (Element) document.createElement("body-literal");
         if (formType.toString().length() > 0) {
             u.setAttribute("type", formType.toString());
         }
         u.appendChild(term.getAsDOM(document));
         return u;
     }    
 }
