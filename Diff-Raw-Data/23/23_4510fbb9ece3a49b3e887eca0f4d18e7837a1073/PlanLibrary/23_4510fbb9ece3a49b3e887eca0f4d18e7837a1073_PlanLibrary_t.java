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
 // CVS information:
 //   $Date$
 //   $Revision$
 //   $Log$
 //   Revision 1.8  2006/02/24 20:08:31  jomifred
 //   no message
 //
 //   Revision 1.7  2006/02/22 21:19:05  jomifred
 //   The internalAction removePlan use plan's label as argument instead of plan's strings
 //
 //   Revision 1.6  2006/01/02 13:49:00  jomifred
 //   add plan unique id, fix some bugs
 //
 //   Revision 1.5  2005/12/30 20:40:16  jomifred
 //   new features: unnamed var, var with annots, TE as var
 //
 //   Revision 1.4  2005/08/12 22:26:08  jomifred
 //   add cvs keywords
 //
 //
 //----------------------------------------------------------------------------
 
 
 package jason.asSyntax;
 
 
 import jason.JasonException;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 public class PlanLibrary {
 
 	/** a MAP from TE to a list of relevant plans */
     Map relPlans = new HashMap();
 
 	/**
 	 * All plans as defined in the AS code (maintains the order of the plans)
 	 */
 	List plans = new ArrayList();
 	
 	/** list of plans that have var as TE */
 	List varPlans = new ArrayList();
 	
 	/** A map from labels to plans */
 	Map planLabels = new HashMap();
 	
 	private static int lastPlanLabel = 0;
 
 	private Logger logger = Logger.getLogger(PlanLibrary.class.getName());	
 	
 	/** 
 	 *  Add a new plan based on a String. The source
 	 *  normally is "self" or the agent that sent this plan.
 	 *  If the already has a plan equals to "stPlan", only a
 	 *  new source is added. 
 	 */
 	public void add(StringTerm stPlan, Term tSource) {
 		String sPlan = stPlan.getString();
 		try {
 			// remove quotes \" -> "
 			StringBuffer sTemp = new StringBuffer();
 			for (int c=0; c <sPlan.length(); c++) {
 				if (sPlan.charAt(c) != '\\') {
 					sTemp.append(sPlan.charAt(c));
 				}
 			}
 			sPlan = sTemp.toString();
 			Plan p = Plan.parse(sPlan);
 
 			int i = plans.indexOf(p);
 			if (i < 0) {
 				p.getLabel().addSource(tSource);
 				add(p);
 			} else {
 				p = (Plan) plans.get(i);
 				p.getLabel().addSource(tSource);
 			}
 			
 			//System.out.println("**** adding plan "+p+" from "+sSource);		
 
 		} catch (Exception e) {
 			logger.log(Level.SEVERE,"Error adding plan "+sPlan,e);
 		}
 	}
 
 
 	
     public void add(Plan p) throws JasonException {
     	// test p.label
     	if (p.getLabel() != null && planLabels.keySet().contains(p.getLabel().getFunctor())) {
     		// test if the new plan is equal, in this case, just add a source
     		Plan planInPL = get(p.getLabel().getFunctor());
     		if (p.equals( planInPL )) {
     			planInPL.getLabel().addSource((Pred)p.getLabel().getSources().get(0));
     			return;
     		} else {
     			throw new JasonException("There already is a plan with label "+p.getLabel());
     		}
     	}
     	
     	// add label, if necessary
     	if (p.getLabel() == null) {
     		String l;
     		do {
     			l = "l"+(lastPlanLabel++);
     		} while (planLabels.keySet().contains(l));
     		p.setLabel(l);
     	}
     	
     	// add self source
     	if (!p.getLabel().hasSource()) {
     		p.getLabel().addSource(new Term("self"));
     	}
     	planLabels.put(p.getLabel().getFunctor(), p);
     	
     	// trim the plan
     	if (p.body != null) {
     		p.body.trimToSize();
     	}
     	if (p.context != null) {
     		p.context.trimToSize();
     	}
         if (p.getTriggerEvent().getLiteral().isVar()) {
         	varPlans.add(p);
         	// add plan p in all entries
         	Iterator i = relPlans.values().iterator();
         	while (i.hasNext()) {
         		List li = (List)i.next();
         		li.add(p);
         	}
         } else {
 	    	List codesList = (List)relPlans.get(p.tevent.getFunctorArity());
 	    	if (codesList == null) {
 	    		codesList = new ArrayList();
 		    	codesList.addAll(varPlans);
 	        	relPlans.put(p.tevent.getFunctorArity(), codesList);
 	    	}
 	    	codesList.add(p);
         }
     	
         plans.add(p);
     }
 
 	public void addAll(PlanLibrary pl) throws JasonException {
 		Iterator i = pl.getPlans().iterator();
 		while (i.hasNext()) {
 			Plan p = (Plan)i.next(); 
 			add(p);
 		}
 	}
     
 	/** return a plan for a label */
     public Plan get(String label) {
         return (Plan)planLabels.get(label);
     }
     
     public List getPlans() {
     	return plans;
     }
 
     /*
     public int indexOf(Plan p) {
         return plans.indexOf(p);
     }
     */
 
     /** 
 	 * Remove a plan represented by the label <i>pLabel</i>.
 	 * In case the plan has many sources, only the plan's source is removed. 
 	 */
 	public boolean removePlan(Term pLabel, Term source) {
 		// find the plan
 		Plan p = get(pLabel.getFunctor());
 		if (p != null) {
 			boolean hasSource = p.getLabel().delSource(source);
 
 			// if no source anymore, remove the plan
 			if (hasSource && !p.getLabel().hasSource()) {
     			remove(pLabel.getFunctor());
 			}
 
 			return true;
 		}
 		return false;
 	}
 
 	/** remove the plan with label <i>pLabel</i> */
     public Plan remove(String pLabel) {
         //codes.remove(i);
     	Plan p = (Plan)planLabels.remove(pLabel);
     	
     	// remove it from plans' list
     	plans.remove(p);
     	
         if (p.getTriggerEvent().getLiteral().isVar()) {
         	varPlans.remove(p);
         	// remove p from all entries
         	Iterator ip = relPlans.values().iterator();
         	while (ip.hasNext()) {
         		List li = (List)ip.next();
         		li.remove(p);
         	}
         } else {
         	List codesList = (List)relPlans.get(p.tevent.getFunctorArity());
             codesList.remove(p);
             if (codesList.isEmpty()) {
             	// no more plans for this TE
             	relPlans.remove(p.tevent.getFunctorArity());
             }
         }
         return p;
     }
 
     public boolean isRelevant(Trigger t) {
     	List l = getAllRelevant(t);
     	return l != null && l.size() > 0;
     }
 

     public List getAllRelevant(Trigger t) {
     	List l = (List)relPlans.get(t.getFunctorArity());
     	if ((l == null || l.size() == 0) && varPlans.size() > 0) { // no rel plan, try varPlan
     		l = varPlans;
     	}
     	return l;
     }
 
    public static final Trigger TE_IDLE = Trigger.parseTrigger("+!idle");

    public List getIdlePlans() {
        return (List)relPlans.get(TE_IDLE.getFunctorArity());
    }

     public String toString() {
         return plans.toString();
     }
     
 	/** get as XML */
 	public Element getAsDOM(Document document) {
 		Element eplans = (Element) document.createElement("plans");
 		Iterator i = plans.iterator();
 		while (i.hasNext()) {
 			Plan p = (Plan)i.next(); 
 			eplans.appendChild(p.getAsDOM(document));
 		}
 		return eplans;
 	}
 
 }
