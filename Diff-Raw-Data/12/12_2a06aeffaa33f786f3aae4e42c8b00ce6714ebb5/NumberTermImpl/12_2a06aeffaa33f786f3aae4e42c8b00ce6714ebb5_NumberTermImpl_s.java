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
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 /** Immutable class that implements a term that represents a number */
 public final class NumberTermImpl extends DefaultTerm implements NumberTerm {
 
 	private static final long serialVersionUID = 1L;
     private static Logger logger = Logger.getLogger(NumberTermImpl.class.getName());
 
 	private final double value;
 	
 	public NumberTermImpl() {
 		super();
 		value = 0;
 	}
 	
 	public NumberTermImpl(String sn) {
 		double t = 0;
 		try {
 			t = Double.parseDouble(sn);
 		} catch (Exception e) {
 			logger.log(Level.SEVERE,"Error setting number term value from "+sn,e);
 		}
 		value = t;
 	}
 	
 	public NumberTermImpl(double vl) {
 		value = vl;
 	}
 	
 	public NumberTermImpl(NumberTermImpl t) {
 		value = t.solve();
 	}
 
 	public double solve() {
 		return value;
 	}
 
 	public Object clone() {
 		return this;
 	}
 	
 	@Override
 	public boolean isNumeric() {
 		return true;
 	}
 
     @Override
     public boolean equals(Object o) {
         if (o == this) return true;
 
        if (o != null && o instanceof NumberTerm) {
             NumberTerm st = (NumberTerm)o;
 			if (st.isVar() || st.isArithExpr()) 
                 return false;
             else
                 try {
                     return solve() == st.solve();
                 } catch (Exception e) { }
 		} 
 		return false;
 	}
 
     @Override
     protected int calcHashCode() {
         return 37 * (int)solve();
     }
     
     @Override
     public int compareTo(Term o) {
         try {
             NumberTerm st = (NumberTerm)o;
             if (solve() > st.solve()) return 1;
             if (solve() < st.solve()) return -1;
         } catch (Exception e) {}
         return 0;    
     }
 
 	public String toString() {
 		long r = Math.round(value);
 		if (value == (double)r) {
 			return String.valueOf(r);
 		} else {
 			return String.valueOf(value);
 		}
 	}
     
     /** get as XML */
     public Element getAsDOM(Document document) {
         Element u = (Element) document.createElement("number-term");
         u.appendChild(document.createTextNode(toString()));
         return u;
     }    
 }
