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
 
 import jason.asSyntax.BodyLiteral;
 import jason.asSyntax.ListTerm;
 import jason.asSyntax.ListTermImpl;
 import jason.asSyntax.Literal;
 import jason.asSyntax.Plan;
 import jason.asSyntax.StringTermImpl;
 import jason.asSyntax.Structure;
 import jason.asSyntax.Term;
 import jason.asSyntax.Trigger;
 
 import java.io.Serializable;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 public class IntendedMeans implements Serializable {
 
 	private static final long serialVersionUID = 1L;
 
 	protected Unifier unif = null;
 	protected Plan    plan;
 	private   Trigger trigger; // the trigger which created this IM
     
     public IntendedMeans(Option opt, Trigger te) {
     	plan = (Plan)opt.getPlan().clone();
     	unif = (Unifier)opt.getUnifier().clone();
     	Literal planLiteral = plan.getTrigger().getLiteral();
     	if (planLiteral.hasAnnot()) {
     		planLiteral.getAnnots().apply(unif);
     	}
         if (te == null) {
             trigger = plan.getTrigger();
         } else {
             trigger = (Trigger)te.clone();
             trigger.getLiteral().apply(unif);
            // add annots of the trigger into the plan's te
             // so that the event +!g[source(ag1)] will add source(ag1)
             // in the TE of the plan
            planLiteral.addAnnots(trigger.getLiteral().getAnnots());
         }
     }
     
     private IntendedMeans() {
     	// used for clone
     }
 
     /** removes the current action of the IM */
     public BodyLiteral removeCurrentStep() {
         if (!plan.getBody().isEmpty())
             return plan.getBody().remove(0);
         else
             return null;
     }
 
     public BodyLiteral getCurrentStep() {
         return plan.getBody().get(0);
     }
 
     
     public Plan getPlan() {
     	return plan;
     }
     
     public Unifier getUnif() {
     	return unif;
     }
 
     /** gets the trigger event that caused the creation of this IM */
     public Trigger getTrigger() {
     	return trigger;
     }
     public void setTrigger(Trigger tr) {
     	trigger = tr;
     }
 
 	public boolean isAtomic() {
 		return plan != null && plan.isAtomic();
 	}
     
     public boolean isFinished() {
         return plan.getBody().isEmpty();
     }
     
     public boolean isGoalAdd() {
         return trigger.isAddition() && trigger.isGoal();
     }
 
     public Object clone() {
     	IntendedMeans c = new IntendedMeans();
     	c.unif = (Unifier)this.unif.clone();
     	c.plan = (Plan)this.plan.clone();
     	c.trigger = (Trigger)this.trigger.clone(); 
     	return c;
     }
     
     public String toString() {
         return plan + " : " + unif;
     }
 
     public Term getAsTerm() {
         Structure im = new Structure("im");
         im.addTerm(new StringTermImpl(plan.getLabel().toString()));
         ListTerm lt = new ListTermImpl();
         for (BodyLiteral bd: plan.getBody()) {
             BodyLiteral c = (BodyLiteral)bd.clone();
             c.getLiteralFormula().apply(unif);
             lt.add(new StringTermImpl(c.toString()));
         }
         im.addTerm(lt);
         return im;        
     }
     
     /** get as XML */
 	public Element getAsDOM(Document document) {
 		Element eim = (Element) document.createElement("intended-means");
 		if (plan != null) {
 			eim.appendChild(plan.getAsDOM(document));
 		}
 		if (unif != null && unif.size() > 0) {
 			eim.appendChild(unif.getAsDOM(document));
 		}
 		return eim;
 	}
 
 }
