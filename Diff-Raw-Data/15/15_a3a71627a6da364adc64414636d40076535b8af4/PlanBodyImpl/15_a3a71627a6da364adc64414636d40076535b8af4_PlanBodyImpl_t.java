 package jason.asSyntax;
 
 import jason.asSemantics.Unifier;
 
 import java.util.Iterator;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 /** 
  *  Represents a plan body item (achieve, test, action, ...) and its successors.
  * 
  *  A plan body like <code>a1; ?t; !g</code> is represented by the following structure
  *  <code>(a1, (?t, (!g)))</code>.
  *  
  * @author Jomi  
  */
 public class PlanBodyImpl extends Structure implements PlanBody, Iterable<PlanBody> {
 
     public static final String BODY_PLAN_FUNCTOR = ";";
 
     private Term        term     = null; 
     private PlanBody    next     = null;
     private BodyType    formType = BodyType.none;
     
     private boolean     isTerm = false;
     
     /** constructor for empty plan body */
     public PlanBodyImpl() {
         super(BODY_PLAN_FUNCTOR, 0);
     }
     
     public PlanBodyImpl(BodyType t, Term b) {
         super(BODY_PLAN_FUNCTOR, 0);
         term     = b;
         formType = t;
         setSrc(b);
     }
 
     public void setBodyNext(PlanBody next) {
         this.next = next;
     }
     public PlanBody getBodyNext() {
         return next;
     }
 
     public boolean isEmptyBody() {
         return term == null;
     }
     
     public BodyType getBodyType() {
         return formType;
     }
     public void setBodyType(BodyType bt) {
         formType = bt;
     }
     
     public Term getBodyTerm() {
         return term;
     }
     public void setBodyTerm(Term t) {
         term = t;
     }
     
     public boolean isBodyTerm() {
     	return isTerm;
     }
     public void setAsBodyTerm(boolean b) {
     	isTerm = b;
     }
     
     @Override
     public boolean isPlanBody() {
         return true;
     }
     
     public Iterator<PlanBody> iterator() {
         return new Iterator<PlanBody>() {
             PlanBody current = PlanBodyImpl.this;
             public boolean hasNext() {
                 return current != null && current.getBodyTerm() != null; 
             }
             public PlanBody next() {
                 PlanBody r = current;
                 if (current != null)
                     current = current.getBodyNext();
                 return r;
             }
             public void remove() { }
         };
     }
 
     // Override some structure methods to work with unification/equals
     @Override
     public int getArity() {
         if (term == null)
             return 0;
         else if (next == null)
             return 1;
         else
             return 2;
     }
 
     @Override
     public Term getTerm(int i) {
         if (i == 0) 
             return term;
         if (i == 1) {
             if (next != null && next.getBodyTerm().isVar() && next.getBodyNext() == null) 
                 // if next is the last VAR, return that var
                 return next.getBodyTerm();
             else
                 return next;
         }
         return null;
     }
 
     @Override
     public void setTerm(int i, Term t) {
         if (i == 0) term = t;
         if (i == 1) System.out.println("Should not set next of body literal!");
     }
 
     @Override
     public boolean apply(Unifier u) {
         // do not apply in next!
         resetHashCodeCache();
         if (term != null && term.apply(u)) {
             if (term.isPlanBody()) { // we can not have "inner" body literals
                 formType = ((PlanBody)term).getBodyType();
                 term     = ((PlanBody)term).getBodyTerm();
             }
             return true;
         }
         return false;
     }
     
     @Override
     public boolean equals(Object o) {
         if (o == null) return false;
         if (o == this) return true;
 
         if (o instanceof PlanBody) {
             PlanBody b = (PlanBody)o;
             return formType == b.getBodyType() && super.equals(o);
         }
         return false;
     }
 
     @Override
     public int calcHashCode() {
         return formType.hashCode() + super.calcHashCode();
     }
 
     public boolean add(PlanBody bl) {
         if (term == null)
             swap(bl);
         else if (next == null)
             next = bl;
         else 
             next.add(bl);
         return true;
     }
 
     public PlanBody getLastBody() {
     	if (next == null)
     		return this;
     	else
     		return next.getLastBody();
     }
     
     public boolean add(int index, PlanBody bl) {
         if (index == 0) {
         	PlanBody newpb = new PlanBodyImpl(this.formType, this.term);
         	newpb.setBodyNext(next);
             swap(bl);
             this.next = bl.getBodyNext();
             this.getLastBody().setBodyNext(newpb);
         } else if (next != null) { 
             next.add(index - 1, bl);
         }
         return true;
     }
 
     public Term removeBody(int index) {
         if (index == 0) {
            Term oldvalue = term;
             if (next == null) {
                 term = null; // becomes an empty
             } else {
                 swap(next); // get values of text
                 next = next.getBodyNext();
             }
            return oldvalue;
         } else { 
             return next.removeBody(index - 1);
         }
     }
 
     public int getPlanSize() {
         if (term == null) 
             return 0;
         else if (next == null)
             return 1;
         else
             return next.getPlanSize() + 1;
     }
 
     private void swap(PlanBody bl) {
         BodyType bt = this.formType;
         this.formType = bl.getBodyType();
         bl.setBodyType(bt);
 
         Term l = this.term;
         this.term = bl.getBodyTerm();
         bl.setBodyTerm(l);
     }
 
     public Object clone() {
         if (term == null) // empty
             return new PlanBodyImpl();
 
         PlanBodyImpl c = new PlanBodyImpl(formType, (Term)term.clone());
         c.isTerm = isTerm;
         if (next != null)
             c.setBodyNext((PlanBody)getBodyNext().clone());
         return c;
     }
     
     public String toString() {
         if (term == null) {
             return "";
         } else {
         	String b, e;
         	if (isTerm) {
         		b = "{ "; 
         		e = " }";
         	} else {
         		b = ""; 
         		e = "";
         	}
         	if (next == null)
         		return b+formType.toString() + term+e;
         	else
         		return b+formType.toString() + term + "; " + next+e;
         }
     }
 
     /** get as XML */
     public Element getAsDOM(Document document) {
         Element eb = (Element) document.createElement("body");
         PlanBody bl = this;
         while (bl != null && !bl.isEmptyBody()) {
             Element u = (Element) document.createElement("body-literal");
             if (bl.getBodyType().toString().length() > 0) {
                 u.setAttribute("type", bl.getBodyType().toString());
             }
             u.appendChild( ((Structure)bl.getBodyTerm()).getAsDOM(document));
             eb.appendChild(u);
             
             bl = bl.getBodyNext();
         }
         return eb;
     }
 }
