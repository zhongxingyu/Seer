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
 
 package jason.asSemantics;
 
 import jason.asSyntax.DefaultTerm;
 import jason.asSyntax.Term;
 import jason.asSyntax.VarTerm;
 
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
     
 /**
       Stores a set of vars that were unified. 
       
       E.g.: when X = Y = W = Z the unifier function maps
           X -> { X, Y, W, Z } 
           Y -> { X, Y, W, Z } 
           W -> { X, Y, W, Z } 
           Z -> { X, Y, W, Z } 
       where { X, Y, W, Z } is a VarsCluster instance.
      
       So when one var is assigned to a value, all vars in the
       cluster receive this same value.
       
       @author Jomi
 */
 public class VarsCluster extends DefaultTerm implements Iterable<VarTerm> {
 	
     private static final long serialVersionUID = 1L;
     private static Logger logger = Logger.getLogger(VarsCluster.class.getName());
 
     private static int idCount = 0;
 
     private int          id = 0;
     private Set<VarTerm> vars = null;
     private Unifier      u;
 		
     // used in clone
 	protected VarsCluster(Unifier u) {
 		this.u = u;
 	}
 
	public VarsCluster(VarTerm v1, VarTerm v2, Unifier u) {
 		id = ++idCount;
 		this.u = u;
 		add(v1);
 		add(v2);
 	}
 
 	private void add(VarTerm vt) {
 		Term vl = u.get(vt);
 		if (vl == null) {
 			// v1 has no value
 			if (vars == null) {
 				vars = new HashSet<VarTerm>();
 			}
 			vars.add(vt);
 		} else if (vl instanceof VarsCluster) {
 			if (vars == null) {
 				vars = ((VarsCluster) vl).vars;
 			} else {
 				vars.addAll(((VarsCluster) vl).vars);
 			}
 		} else {
 			logger.warning("joining var that has value!");
 		}
 	}
 
 	public Iterator<VarTerm> iterator() {
 		return vars.iterator();
 	}
 
 	public boolean equals(Object o) {
 		if (o == null) return false;
 		if (o == this) return true;
 		if (o instanceof VarsCluster) return vars.equals(((VarsCluster) o).vars);
 		return false;
 	}
 
 	public boolean hasValue() {
 		return vars != null && !vars.isEmpty();
 	}
 
 	public Object clone() {
 		VarsCluster c = new VarsCluster(u);
 		c.vars = new HashSet<VarTerm>();
 		for (VarTerm vt : this.vars) {
 			c.vars.add((VarTerm) vt.clone());
 		}
 		return c;
 	}
 
 	protected int calcHashCode() {
 		return vars.hashCode();
 	}
 
 	public Element getAsDOM(Document document) {
 		return null;
 	}
 
 	public String toString() {
 		return "_VC" + id;
 	}
 }
