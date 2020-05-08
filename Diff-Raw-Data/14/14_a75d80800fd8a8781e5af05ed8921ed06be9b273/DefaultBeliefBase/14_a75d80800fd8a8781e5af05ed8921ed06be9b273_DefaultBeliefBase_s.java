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
 
 package jason.bb;
 
 import jason.asSemantics.Agent;
 import jason.asSemantics.Unifier;
 import jason.asSyntax.Literal;
 import jason.asSyntax.PredicateIndicator;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 /**
  * Default implementation of Jason BB.
  */
 public class DefaultBeliefBase implements BeliefBase {
 
     private static Logger logger = Logger.getLogger(DefaultBeliefBase.class.getSimpleName());
 
     /**
      * belsMap is a table where the key is the bel.getFunctorArity and the value
      * is a list of literals with the same functorArity.
      */
     private Map<PredicateIndicator, BelEntry> belsMap = new HashMap<PredicateIndicator, BelEntry>();
 
     private int size = 0;
 
     /** set of beliefs with percept annot, used to improve performance of buf */
    private Set<Literal> percepts = new HashSet<Literal>();
 
     public void init(Agent ag, String[] args) {
         if (ag != null) {
             logger = Logger.getLogger(ag.getTS().getUserAgArch().getAgName() + "-"+DefaultBeliefBase.class.getSimpleName());
         }
     }
 
     public void stop() {
     }
 
     public int size() {
         return size;
     }
 
     @SuppressWarnings("unchecked")
 	public Iterator<Literal> getPercepts() {
         final Iterator<Literal> i = percepts.iterator();
         return new Iterator<Literal>() {
             Literal current = null;
             public boolean hasNext() {
                 return i.hasNext();
             }
             public Literal next() {
                 current = i.next(); 
                 return current;
             }
             public void remove() {
                 if (current == null) {
                     logger.warning("Trying to remove a perception, but the the next() from the iterator is not called before!");
                 }
                 // remove from percepts
                 i.remove();
                 
                 // remove the percept annot
                 current.delAnnot(BeliefBase.TPercept);
                 
                 // and also remove from the BB
                 removeFromEntry(current);
             }
         };
         //return ((Set<Literal>)percepts.clone()).iterator();        
     }
 
     Set<Literal> getPerceptsSet() {
         return percepts;
     }
     
     public boolean add(Literal l) {
         return add(l, false);
     }
     
     public boolean add(int index, Literal l) {
         return add(l, index != 0);
     }
     
 	protected boolean add(Literal l, boolean addInEnd) {
 		if (!l.canBeAddedInBB()) {
             logger.log(Level.SEVERE, "Error: '"+l+"' can not be added in the belief base.");
             return false;
         }
         
         Literal bl = contains(l);
         if (bl != null && !bl.isRule()) {
             // add only annots
             if (bl.importAnnots(l)) {
                 // check if it needs to be added in the percepts list
                 if (l.hasAnnot(TPercept)) {
                     percepts.add(bl);
                 }
                 return true;
             }
         } else {
             BelEntry entry = belsMap.get(l.getPredicateIndicator());
             if (entry == null) {
                 entry = new BelEntry();
                 belsMap.put(l.getPredicateIndicator(), entry);
             }
             entry.add(l, addInEnd);
             
             // add it in the percepts list
             if (l.hasAnnot(TPercept)) {
                 percepts.add(l);
             }
             size++;
             return true;
         }
         return false;
     }
     
     public boolean remove(Literal l) {
         Literal bl = contains(l);
         if (bl != null) {
             if (l.hasSubsetAnnot(bl)) { // e.g. removing b[a] or b[a,d] from BB b[a,b,c]
             		                    // second case fails
                 if (l.hasAnnot(TPercept)) {
                     percepts.remove(bl);
                 }
                 boolean result = bl.delAnnots(l.getAnnots()); // note that l annots can be empty, in this case, nothing is deleted!
                 return removeFromEntry(bl) || result;
             }
         } else {
             if (logger.isLoggable(Level.FINE)) logger.fine("Does not contain " + l + " in " + belsMap);
         }
         return false;
     }
 
     private boolean removeFromEntry(Literal l) {
         if (l.hasSource()) {
             return false;
         } else {
             PredicateIndicator key = l.getPredicateIndicator();
             BelEntry entry = belsMap.get(key);
             entry.remove(l);
             if (entry.isEmpty()) {
                 belsMap.remove(key);
             }
             size--;
             return true;
         }
     }
 
     public Iterator<Literal> iterator() {
         final Iterator<BelEntry> ibe = belsMap.values().iterator();
         if (ibe.hasNext()) {
             return new Iterator<Literal>() {
 
                 Iterator<Literal> il = ibe.next().list.iterator();
 
                 public boolean hasNext() {
                     return il.hasNext();
                 }
 
                 public Literal next() {
                     Literal l = il.next();
                     if (!il.hasNext() && ibe.hasNext()) {
                         il = ibe.next().list.iterator();
                     }
                     return l;
                 }
 
                 public void remove() {
                     logger.warning("remove is not implemented for BB.iterator().");
                 }
             };
         } else {
             return new ArrayList<Literal>(0).iterator();
         }
     }
     
     /** @deprecated use iterator() instead of getAll */
     public Iterator<Literal> getAll() {
     	return iterator();
     }
     
 
     public boolean abolish(PredicateIndicator pi) {
         return belsMap.remove(pi) != null;
     }
 
     public Literal contains(Literal l) {
         BelEntry entry = belsMap.get(l.getPredicateIndicator());
         if (entry == null) {
             return null;
         } else {
             //logger.info("*"+l+":"+l.hashCode()+" = "+entry.contains(l)+" in "+this);//+" entry="+entry);
             return entry.contains(l);
         }
     }
 
     public Iterator<Literal> getCandidateBeliefs(Literal l, Unifier u) {
         if (l.isVar()) {
             // all bels are relevant
             return iterator();
         } else {
             BelEntry entry = belsMap.get(l.getPredicateIndicator());
             if (entry != null) {
                 return Collections.unmodifiableList(entry.list).iterator();
             } else {
                 return null;
             }
         }
     }
     
     /** @deprecated use getCandidateBeliefs(l,null) instead */ 
     public Iterator<Literal> getRelevant(Literal l) {
         return getCandidateBeliefs(l, null);
     }
 
     public String toString() {
         return belsMap.toString();
     }
 
     public Object clone() {
     	DefaultBeliefBase bb = new DefaultBeliefBase();
     	for (Literal b: this) {
     		bb.add(1, b.copy());
     	}
     	return bb;
     }
     
     public Element getAsDOM(Document document) {
         Element ebels = (Element) document.createElement("beliefs");
         for (Literal l: this) {
             ebels.appendChild(l.getAsDOM(document));
         }
         return ebels;
     }
     
     /** each predicate indicator has one BelEntry assigned to it */
     final class BelEntry {
         
         final private List<Literal> list = new LinkedList<Literal>(); // maintains the order of the bels
         final private Map<LiteralWrapper,Literal> map = new HashMap<LiteralWrapper,Literal>(); // to fastly find contents, from literal do list index
         
         @SuppressWarnings("unchecked")
         public void add(Literal l, boolean addInEnd) {
             //try {
                 // minimise the allocation space of terms
             // Moved to the parser.
             //    if (!l.isAtom()) ((ArrayList) l.getTerms()).trimToSize();
             //} catch (Exception e) {}
             
             map.put(new LiteralWrapper(l), l);
             if (addInEnd) {
                 list.add(l);
             } else {
                 list.add(0,l);
             }
         }
         
         public void remove(Literal l) {
             Literal linmap = map.remove(new LiteralWrapper(l)); 
             if (linmap != null) {
                 list.remove(linmap);
             }
         }
         
         public boolean isEmpty() {
             return list.isEmpty();
         }
         
         public Literal contains(Literal l) {
             return map.get(new LiteralWrapper(l));
         }
         
         protected Object clone() {
         	BelEntry be = new BelEntry();
         	for (Literal l: list) {
         		be.add(l.copy(), false);
         	}
         	return be;
         }
         
         public String toString() {
             StringBuilder s = new StringBuilder();
             for (Literal l: list) {
                 s.append(l+":"+l.hashCode()+",");
             }
             return s.toString();
         }
         
         /** a literal that uses equalsAsTerm for equals */
         final class LiteralWrapper {
             final private Literal l;
             public LiteralWrapper(Literal l) { this.l = l; }
             public int hashCode() { return l.hashCode(); }
             public boolean equals(Object o) { return o instanceof LiteralWrapper && l.equalsAsStructure(((LiteralWrapper)o).l); }
             public String toString() { return l.toString(); }
         }
     }
 }
