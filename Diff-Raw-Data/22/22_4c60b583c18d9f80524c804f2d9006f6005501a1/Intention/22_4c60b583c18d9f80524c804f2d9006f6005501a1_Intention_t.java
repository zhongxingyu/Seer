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
 
 import jason.asSyntax.ListTerm;
 import jason.asSyntax.ListTermImpl;
 import jason.asSyntax.NumberTermImpl;
 import jason.asSyntax.Structure;
 import jason.asSyntax.Term;
 import jason.asSyntax.Trigger;
 
 import java.io.Serializable;
 import java.util.ListIterator;
 import java.util.Stack;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 /**
  * Represents and Intention (a stack of IntendedMeans).
  * 
  * The comparable sorts the intentions based on the atomic property: 
  * atomic intentions comes first.
  * 
  * @author Jomi & Rafael
  */
 public class Intention implements Serializable, Comparable<Intention> {
 
 	private static final long serialVersionUID = 1L;
 	public  static final Intention EmptyInt = null;
     private static       int idCount = 0;
 
     private int     id;
     private boolean isAtomic    = false;
     private boolean isSuspended = false;
     
     private Stack<IntendedMeans> intendedMeans = new Stack<IntendedMeans>();
 
     // static private Logger logger = Logger.getLogger(Intention.class.getName());
 
     public Intention() {
         id = ++idCount;
     }
 
     public int getId() {
         return id;
     }
 
     public void push(IntendedMeans im) {
         intendedMeans.push(im);
         if (im.isAtomic()) {
             isAtomic = true;
         }
     }
 
     public IntendedMeans peek() {
         return intendedMeans.peek();
     }
 
     public IntendedMeans get(int index) {
         return intendedMeans.get(index);
     }
 
     public IntendedMeans pop() {
         IntendedMeans top = intendedMeans.pop();
 
         isAtomic = false;
         for (IntendedMeans im : intendedMeans) {
             if (im.isAtomic()) {
                 isAtomic = true;
                 break;
             }
         }
         return top;
     }
 
     public ListIterator<IntendedMeans> iterator() {
         return intendedMeans.listIterator(intendedMeans.size());
     }
 
     public boolean isFinished() {
         return intendedMeans.isEmpty();
     }
 
     public int size() {
         return intendedMeans.size();
     }
 
     public boolean isAtomic() {
         return isAtomic;
     }
     
     public void setAtomic(boolean b) {
         isAtomic = b;
     }
     
     public void setSuspended(boolean b) {
     	isSuspended = b;
     }
     
     public boolean isSuspended() {
     	return isSuspended;
     }
     
     public Stack<IntendedMeans> getIMs() {
     	return intendedMeans;
     }
     
     /** returns the IntendedMeans with TE = g, returns null if there isn't one */
     public IntendedMeans getIM(Trigger g, Unifier u) {
        for (IntendedMeans im : intendedMeans)
         	//System.out.println(g + " = "+ im.getTrigger()+" = "+u.unifies(g, im.getTrigger()));
            if (u.unifies(g, im.getTrigger()))
                 return im;
         return null;
     }
     
     /** returns true if the intention has an IM where TE = g, using u to verify equality */
     public boolean hasTrigger(Trigger g, Unifier u) {
        //return getIM(g,u) != null;
        for (IntendedMeans im : intendedMeans)
            if (u.unifies(g, im.getTrigger()))
                return true;
        return false;
     }
 
     /** remove all IMs until the IM with trigger te */
     public boolean dropGoal(Trigger te, Unifier un) {
         IntendedMeans im = getIM(te, un);
         if (im != null) {
             // remove the IMs until im-1
             while (peek() != im) {
                 pop();
             }
             pop(); // remove im
             return true;
         }      
         return false;
     }
     
     /** implements atomic intentions > not atomic intentions */
     public int compareTo(Intention o) {
         if (o.isAtomic && !this.isAtomic) return 1;
         if (this.isAtomic && !o.isAtomic) return -1;
         return 0;
     }
     
     public boolean equals(Object o) {
         if (o == null) return false;
         if (o == this) return true;
         if (o instanceof Intention) return ((Intention)o).id == this.id;
         return false;
     }
     
     public int hashCode() {
         return String.valueOf(id).hashCode();
     }
     
     public Object clone() {
     	Intention i = new Intention();
     	i.id = id;
         i.isAtomic = isAtomic;
         i.intendedMeans = new Stack<IntendedMeans>();
         for (IntendedMeans im: intendedMeans) {
         	i.intendedMeans.add((IntendedMeans)im.clone());
         }
     	return i;
     }
         
     public String toString() {
         StringBuilder s = new StringBuilder();
         ListIterator<IntendedMeans> i = intendedMeans.listIterator(intendedMeans.size());
         while (i.hasPrevious()) {
             s.append("    " + i.previous() + "\n");
         }
         return s.toString();
     }
 
     public Term getAsTerm() {
         Structure intention = new Structure("intention");
         intention.addTerm(new NumberTermImpl(getId()));
         ListTerm lt = new ListTermImpl();
         ListIterator<IntendedMeans> i = intendedMeans.listIterator(intendedMeans.size());
         while (i.hasPrevious()) {
             lt.add(i.previous().getAsTerm());            
         }
         intention.addTerm(lt);
         return intention;        
     }
 
     /** get as XML */
     public Element getAsDOM(Document document) {
         Element eint = (Element) document.createElement("intention");
         eint.setAttribute("id", id + "");
         for (int i = intendedMeans.size() - 1; i >= 0; i--) {
             IntendedMeans im = (IntendedMeans) intendedMeans.get(i);
             eint.appendChild(im.getAsDOM(document));
         }
         return eint;
     }
 
 }
