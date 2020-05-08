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
 
 package jason.asSemantics;
 
 import jason.JasonException;
 import jason.RevisionFailedException;
 import jason.architecture.AgArch;
 import jason.asSyntax.Atom;
 import jason.asSyntax.PlanBody;
 import jason.asSyntax.DefaultTerm;
 import jason.asSyntax.InternalActionLiteral;
 import jason.asSyntax.ListTermImpl;
 import jason.asSyntax.Literal;
 import jason.asSyntax.LogicalFormula;
 import jason.asSyntax.Plan;
 import jason.asSyntax.PlanLibrary;
 import jason.asSyntax.Structure;
 import jason.asSyntax.Term;
 import jason.asSyntax.Trigger;
 import jason.asSyntax.Trigger.TEOperator;
 import jason.asSyntax.Trigger.TEType;
 import jason.bb.BeliefBase;
 import jason.runtime.Settings;
 
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public class TransitionSystem {
 
     public enum State { StartRC, SelEv, RelPl, ApplPl, SelAppl, FindOp, AddIM, ProcAct, SelInt, ExecInt, ClrInt }
     
     private Logger        logger     = null;
 
     private Agent         ag         = null;
     private AgArch        agArch     = null;
     private Circumstance  C          = null;
     private Settings      setts      = null;
 
     // first step of the SOS 
     private State         step       = State.StartRC;                                                                                               
 
     // number of reasoning cycles since last belief revision
     private int           nrcslbr;                                                                                                             
     
     // both configuration and configuration' point to this
     // object, this is just to make it look more like the SOS
     private TransitionSystem      confP;
 
     // both configuration and configuration' point to this
     // object, this is just to make it look more like the SOS
     private TransitionSystem      conf;
 
     public TransitionSystem(Agent a, Circumstance c, Settings s, AgArch ar) {
         ag     = a;
         C      = c;
         agArch = ar;
 
         if (s == null)
             setts = new Settings();
         else
             setts = s;
 
         if (C == null)
             C = new Circumstance();
         
         // we need to initialise this "aliases"
         conf = confP = this;
 
         nrcslbr = setts.nrcbp(); // to do BR to start with
 
         setLogger(agArch);
         if (setts != null)
             logger.setLevel(setts.logLevel());
     }
 
     public void setLogger(AgArch arch) {
         if (arch != null)
             logger = Logger.getLogger(TransitionSystem.class.getName() + "." + arch.getAgName());
         else
             logger = Logger.getLogger(TransitionSystem.class.getName());
     }
 
     /** ******************************************************************* */
     /* SEMANTIC RULES */
     /** ******************************************************************* */
     private void applySemanticRule() throws JasonException {
         // check the current step in the reasoning cycle
         // only the main parts of the interpretation appear here
         // the individual semantic rules appear below
 
         switch (step) {
         case StartRC:   applyProcMsg(); break;
         case SelEv:     applySelEv(); break;
         case RelPl:     applyRelPl();  break;
         case ApplPl:    applyApplPl(); break;
         case SelAppl:   applySelAppl(); break;
         case FindOp:    applyFindOp(); break;
         case AddIM:     applyAddIM(); break;
         case ProcAct:   applyProcAct(); break;
         case SelInt:    applySelInt(); break;
         case ExecInt:   applyExecInt(); break;
         case ClrInt:    confP.step = State.StartRC;
                         applyClrInt(conf.C.SI);
                         break;
         }
     }
 
     // the semantic rules are referred to in comments in the functions below
 
     private void applyProcMsg() throws JasonException {
         confP.step = State.SelEv;
         if (!conf.C.MB.isEmpty()) {
             Message m = conf.ag.selectMessage(conf.C.MB);
             if (m == null) return;
             
             // get the content, it can be any term (literal, list, number, ...; see ask)
             Term content = null;
             if (m.getPropCont() instanceof Term) {
             	content = (Term)m.getPropCont();
             } else {
             	content = DefaultTerm.parse(m.getPropCont().toString());
             }
 
             // check if an intention was suspended waiting this message
             Intention intention = null;
             if (m.getInReplyTo() != null) {
                 intention = getC().getPendingIntentions().remove(m.getInReplyTo());
             }
             // is it a pending intention?
             if (intention != null) {
                 // unify the message answer with the .send fourth argument.
                 // the send that put the intention in Pending state was
                 // something like
                 //    .send(ag1,askOne, value, X)
                 // if the answer was tell 3, unifies X=3
                 // if the answer was untell 3, unifies X=false
                 Structure send = (Structure)intention.peek().removeCurrentStep();
                 if (m.isUnTell()) {
                     if (send.getTerm(1).toString().equals("askOne")) {
                         content = Literal.LFalse;
                     } else { // the .send is askAll
                         content = new ListTermImpl(); // the answer is an empty list
                     }
                 }
                 if (intention.peek().getUnif().unifies(send.getTerm(3), content)) {
                     getC().addIntention(intention);
                 } else {
                     conf.C.SI = intention;
                     generateGoalDeletion();
                 }
 
                 // the message is not an ask answer
             } else if (conf.ag.socAcc(m)) {
 
                 // generate an event
                 Literal received = new Literal("kqml_received");
                 received.addTerm(new Atom(m.getSender()));
                 received.addTerm(new Atom(m.getIlForce()));
                 received.addTerm(content);
                 received.addTerm(new Atom(m.getMsgId()));
 
                 updateEvents(new Event(new Trigger(TEOperator.add, TEType.achieve, received), Intention.EmptyInt));
             }
         }
     }
 
     private void applySelEv() throws JasonException {
         
         // Rule for atomic, if there is an atomic intention, do not select event
         if (C.hasAtomicIntention()) {
             confP.step = State.SelInt;
             return;            
         }
         
         
         if (conf.C.hasEvent()) {
             // Rule for atomic, events from atomic intention has priority
             confP.C.SE = C.removeAtomicEvent();
             if (confP.C.SE != null) {
                 confP.step = State.RelPl;
                 return;
             }
 
             // Rule SelEv1
             confP.C.SE = conf.ag.selectEvent(confP.C.getEvents());
             if (confP.C.SE != null) {
                 if (ag.hasCustomSelectOption() || setts.verbose() == 2) // verbose == 2 means debug mode 
                     confP.step = State.RelPl;
                 else 
                     confP.step = State.FindOp;
                 return;
             }
         }
         // Rule SelEv2
         // directly to ProcAct if no event to handle
         confP.step = State.ProcAct;
     }
 
     private void applyRelPl() throws JasonException {
         // get all relevant plans for the selected event
         confP.C.RP = relevantPlans(conf.C.SE.trigger);
 
         // Rule Rel1
         if (confP.C.RP != null || setts.retrieve()) 
             // retrieve is mainly for Coo-AgentSpeak
             confP.step = State.ApplPl;
         else
             applyRelApplPlRule2("relevant");
     }
     
     private void applyApplPl() throws JasonException {
         confP.C.AP = applicablePlans(confP.C.RP);
 
         // Rule Appl1
         if (confP.C.AP != null || setts.retrieve()) 
             // retrieve is mainly for Coo-AgentSpeak
             confP.step = State.SelAppl;
         else
             applyRelApplPlRule2("applicable");
     }
 
     private void applyRelApplPlRule2(String m) throws JasonException {
         confP.step = State.ProcAct; // default next step
         if (conf.C.SE.trigger.isGoal()) {
             // can't carry on, no relevant/applicable plan.
             logger.warning("Found a goal for which there is no "+m+" plan:" + conf.C.SE);
             generateGoalDeletionFromEvent();
         } else {
             if (conf.C.SE.isInternal()) {
                 // e.g. belief addition as internal event, just go ahead
                 // but note that the event was relevant, yet it is possible
                 // the programmer just wanted to add the belief and it was
                 // relevant by chance, so just carry on instead of dropping the
                 // intention
                 confP.C.SI = conf.C.SE.intention;
                 updateIntention();
             } else if (setts.requeue()) {  
                 // if external, then needs to check settings
                 confP.C.addEvent(conf.C.SE);
             } else {
                 // current event is external and irrelevant,
                 // discard that event and select another one
                 confP.step = State.SelEv;
             }
         }        
     }
     
 
     private void applySelAppl() throws JasonException {
         // Rule SelAppl
         confP.C.SO = conf.ag.selectOption(confP.C.AP);
         if (confP.C.SO != null) {
             confP.step = State.AddIM;
             if (logger.isLoggable(Level.FINE)) logger.fine("Selected option "+confP.C.SO+" for event "+confP.C.SE);
         } else {
             logger.fine("** selectOption returned null!");
             generateGoalDeletionFromEvent(); 
             // can't carry on, no applicable plan.
             confP.step = State.ProcAct;
         }
     }
 
     /**
      * This step is new in Jason 1.1 and replaces the steps RelPl->ApplPl->SelAppl when the user
      * does not customise selectOption. This version does not create the RP and AP lists and thus 
      * optimise the reasoning cycle. It searches for the first option and automatically selects it.
      * 
      * @since 1.1
      */
     private void applyFindOp() throws JasonException {
         confP.step = State.AddIM; // default next step
 
         // get all relevant plans for the selected event
         //Trigger te = (Trigger) conf.C.SE.trigger.clone();
         List<Plan> candidateRPs = conf.ag.pl.getCandidatePlans(conf.C.SE.trigger);
         if (candidateRPs != null) {
             for (Plan pl : candidateRPs) {
                 Unifier relUn = pl.isRelevant(conf.C.SE.trigger);
                 if (relUn != null) { // is relevant
                     LogicalFormula context = pl.getContext();
                     if (context == null) { // context is true
                         confP.C.SO = new Option(pl, relUn);
                         return;
                     } else {
                         Iterator<Unifier> r = context.logicalConsequence(ag, relUn);
                         if (r != null && r.hasNext()) {
                             confP.C.SO = new Option(pl, r.next());
                             return;
                         }
                     } 
                 }
             }
         }
         
         // problem: no plan
         applyRelApplPlRule2("relevant/applicable");
     }
     
     private void applyAddIM() throws JasonException {
         // create a new intended means
         IntendedMeans im = new IntendedMeans(conf.C.SO, conf.C.SE.getTrigger());
 
         // Rule ExtEv
         if (conf.C.SE.intention == Intention.EmptyInt) {
             Intention intention = new Intention();
             intention.push(im);
             confP.C.addIntention(intention);
         } else {
             // Rule IntEv
             confP.C.SE.intention.push(im);
             confP.C.addIntention(confP.C.SE.intention);
         }
         confP.step = State.ProcAct;
     }
 
     private void applyProcAct() throws JasonException {
         confP.step = State.SelInt; // default next step
         if (conf.C.hasFeedbackAction()) {
             ActionExec a = conf.ag.selectAction(conf.C.getFeedbackActions());
             if (a != null) {
                 confP.C.SI = a.getIntention();
     
                 // remove the intention from PA (PA has all pending action, including those in FA;
                 // but, if the intention is not in PA, it means that the intention was dropped
                 // and should not return to I)
                 if (C.getPendingActions().remove(a.getIntention().getId()) != null) {
     	            if (a.getResult()) {
     	                // add the intention back in I
     	                updateIntention();
     	            	applyClrInt(confP.C.SI);
     	            } else {
     	                generateGoalDeletion();
     	            }
                 } else {
                     applyProcAct(); // get next action
                 }
             }
         }
     }
 
     private void applySelInt() throws JasonException {
         confP.step = State.ExecInt; // default next step
 
         // Rule for Atomic Intentions
         confP.C.SI = C.removeAtomicIntention();
         if (confP.C.SI != null) {
             return;
         }
 
         // Rule SelInt1
         if (conf.C.hasIntention()) {
             confP.C.SI = conf.ag.selectIntention(conf.C.getIntentions());
             if (confP.C.SI != null) { // the selectIntention function retuned null
                 return;            	
             }
         }
 
         confP.step = State.StartRC;
     }
 
     @SuppressWarnings("unchecked")
     private void applyExecInt() throws JasonException {
         confP.step = State.ClrInt; // default next step
         
         if (conf.C.SI.isFinished()) {
             return;
         }
         
         // get next formula in the body of the intended means
         // on the top of the selected intention
 
         IntendedMeans im = conf.C.SI.peek();
 
         if (im.isFinished()) { 
             // for empty plans! may need unif, etc
             updateIntention();
             return;
         }
         Unifier     u = im.unif;
         PlanBody h = im.getCurrentStep();
         h.apply(u);
         
         Literal body  = null;
         Term    bTerm = h.getBodyTerm();
         if (bTerm instanceof Literal)
             body = (Literal)bTerm;
 
         switch (h.getBodyType()) {
 
         // Rule Action
         case action:
             confP.C.A = new ActionExec(body, conf.C.SI);
             break;
 
         case internalAction:
             boolean ok = false;
             try {
                 InternalAction ia = ((InternalActionLiteral)body).getIA(ag);
                 Object oresult = ia.execute(this, u, body.getTermsArray());
                 if (oresult != null) {
                     ok = oresult instanceof Boolean && (Boolean)oresult;
                     if (!ok && oresult instanceof Iterator) { // ia result is an Iterator
                         Iterator<Unifier> iu = (Iterator<Unifier>)oresult;
                         if (iu.hasNext()) {
                             // change the unifier of the current IM to the first returned by the IA
                             im.unif = iu.next(); 
                             ok = true;
                         }
                     }
                 }
 
                 if (ok && !ia.suspendIntention())
                     updateIntention();
                 
             } catch (Exception e) {
                 logger.log(Level.SEVERE, body.getErrorMsg()+": "+ e.getMessage(), e);
                 ok = false;
             }
             if (!ok)
                 generateGoalDeletion();
 
             break;
 
         case constraint:
             Iterator<Unifier> iu = ((LogicalFormula)bTerm).logicalConsequence(ag, u);
             if (iu.hasNext()) {
                 im.unif = iu.next();
                 updateIntention();
             } else {
                 if (logger.isLoggable(Level.FINE)) logger.fine("Constraint "+h+" was not satisfied ("+h.getErrorMsg()+").");
                 generateGoalDeletion();
             }
             break;
 
         // Rule Achieve
         case achieve:
             // free variables in an event cannot conflict with those in the plan
             body = (Literal)body.clone();
             body.makeVarsAnnon();
             conf.C.addAchvGoal(body, conf.C.SI);
             confP.step = State.StartRC;
             break;
 
         // Rule Achieve as a New Focus (the !! operator)
         case achieveNF:
             body = (Literal)body.clone();
             body.makeVarsAnnon();
             conf.C.addAchvGoal(body, Intention.EmptyInt);
             updateIntention();
             break;
 
         // Rule Test
         case test:
             LogicalFormula f = (LogicalFormula)bTerm;
             if (conf.ag.believes(f, u)) {
                 updateIntention();
             } else {
             	boolean fail = true;
             	if (f instanceof Literal) { // generate event when using literal in the test (no events for log. expr. like ?(a & b))
             		body = (Literal)f.clone();
             		if (body.isLiteral()) { // in case body is a var with content that is not a literal (note the VarTerm pass in the instanceof Literal)
     	                body.makeVarsAnnon();
     	                Trigger te = new Trigger(TEOperator.add, TEType.test, body);
     	                if (ag.getPL().hasCandidatePlan(te)) {
     	                    Event evt = new Event(te, conf.C.SI);
     	                    if (logger.isLoggable(Level.FINE)) logger.fine("Test Goal '" + h + "' failed as simple query. Generating internal event for it: "+te);
     	                    conf.C.addEvent(evt);
     	                    confP.step = State.StartRC;
     	                    fail = false;
     	                }
             		}
             	}
                 if (fail) {
                     if (logger.isLoggable(Level.FINE)) logger.fine("Test '"+h+"' failed ("+h.getErrorMsg()+").");
                     generateGoalDeletion();
                 }
             }
             break;
 
             
         case delAddBel: 
             // -+a(1,X) ===> remove a(_,_), add a(1,X)
             // change all vars to anon vars to remove it
             if (!body.hasAnnot()) {
                 // do not add source(self) in case the
                 // programmer set some annotation
                 body.addAnnot(BeliefBase.TSelf);
             }
             Literal bc = (Literal)body.clone();
             bc.makeTermsAnnon();
             // to delete, create events as external to avoid that
             // remove/add create two events for the same intention
 
             try {
                 List<Literal>[] result = ag.brf(null, bc, conf.C.SI); // the intention is not the new focus
                 if (result != null) { // really delete something
                     // generate events
                     updateEvents(result,Intention.EmptyInt);
                 }
             } catch (RevisionFailedException re) {
                 generateGoalDeletion();
                 break;
             }
 
             // add the belief, so no break;
             
         // Rule AddBel
         case addBel:
             if (!body.hasSource()) {
                 // do not add source(self) in case the
                 // programmer set the source
                 body.addAnnot(BeliefBase.TSelf);
             }
 
             // calculate focus
             Intention newfocus = Intention.EmptyInt;
             if (setts.sameFocus())
                 newfocus = conf.C.SI;
 
             // rename free vars
             body.makeVarsAnnon();
             
             // call BRF
             try {
                 List<Literal>[] result = ag.brf(body,null,conf.C.SI); // the intention is not the new focus
                 if (result != null) { // really add something
                     // generate events
                     updateEvents(result,newfocus);
                     if (!setts.sameFocus()) {
                         updateIntention();
                     }                    
                 } else {
                     updateIntention();                    
                 }
             } catch (RevisionFailedException re) {
                 generateGoalDeletion();
             }
             break;
             
         case delBel:
             if (!body.hasAnnot()) {
                 // do not add source(self) in case the
                 // programmer set some annotation
                 body.addAnnot(BeliefBase.TSelf);
             }
 
             newfocus = Intention.EmptyInt;
             if (setts.sameFocus())
                 newfocus = conf.C.SI;
 
             // call BRF
             try {
                 List<Literal>[] result = ag.brf(null,body, conf.C.SI); // the intention is not the new focus
                 if (result != null) { // really change something
                     // generate events
                     updateEvents(result,newfocus);
                     if (!setts.sameFocus()) {
                         updateIntention();
                     }                    
                 } else {
                     updateIntention();                    
                 }
             } catch (RevisionFailedException re) {
                 generateGoalDeletion();
             }            
             break;
         }
     }
 
     public void applyClrInt(Intention i) throws JasonException {
         // Rule ClrInt
         if (i == null)
             return;
         
         if (i.isFinished()) {
             // intention finished, remove it
             confP.C.removeIntention(i);
             //conf.C.SI = null;
             return;
         }
 
         IntendedMeans im = i.peek();
         if (!im.isFinished()) {
             // nothing to do
             return;
         }
 
         // remove the finished IM from the top of the intention
         IntendedMeans topIM = i.pop();
         if (logger.isLoggable(Level.FINE)) logger.fine("Returning from IM "+topIM.getPlan().getLabel()+", te="+topIM.getPlan().getTrigger());
         
         // if finished a failure handling IM ...
         if (im.getTrigger().isGoal() && !im.getTrigger().isAddition() && i.size() > 0) {
             // needs to get rid of the IM until a goal that
             // has failure handling. E.g,
             //   -!b
             //   +!c
             //   +!d
             //   +!b
             //   +!s: !b; !z
             // should became
             //   +!s: !z
             im = i.pop(); // +!c above, old
             while (!im.unif.unifies(topIM.getTrigger().getLiteral(), im.getTrigger().getLiteral()) && i.size() > 0) {
                 im = i.pop();
             }
         }
         if (!i.isFinished()) {
             im = i.peek(); // +!s or +?s
             if (!im.isFinished()) {
                 // removes !b or ?s
                 Term g = im.removeCurrentStep();
                 // make the TE of finished plan ground and unify that
                 // with goal in the body
                 Literal tel = topIM.getPlan().getTrigger().getLiteral();
                 tel.apply(topIM.unif);
                 im.unif.unifies(tel, g);
             }
         }
 
         // the new top may have become
         // empty! need to keep checking.
         applyClrInt(i);
     }
 
     /**********************************************/
     /* auxiliary functions for the semantic rules */
     /**********************************************/
 
     public List<Option> relevantPlans(Trigger teP) throws JasonException {
         Trigger te = (Trigger) teP.clone();
         List<Option> rp = null;
         List<Plan> candidateRPs = conf.ag.pl.getCandidatePlans(te);
         if (candidateRPs != null) {
             for (Plan pl : candidateRPs) {
                 Unifier relUn = pl.isRelevant(te);
                 if (relUn != null) {
                     if (rp == null) rp = new LinkedList<Option>();
                     rp.add(new Option(pl, relUn));
                 }
             }
         }
         return rp;
     }
 
     public List<Option> applicablePlans(List<Option> rp) throws JasonException {
         List<Option> ap = null;
         if (rp != null) {
         	//ap = new ApplPlanTimeOut().get(rp);
         	
             for (Option opt: rp) {
                 LogicalFormula context = opt.getPlan().getContext();
                 if (context == null) { // context is true
                     if (ap == null) ap = new LinkedList<Option>();
                     ap.add(opt);
                 } else {
                     boolean allUnifs = opt.getPlan().isAllUnifs();
                     Iterator<Unifier> r = context.logicalConsequence(ag, opt.getUnifier());
                     if (r != null) {
                         while (r.hasNext()) {
                             opt.setUnifier(r.next());
                             
                             if (ap == null) ap = new LinkedList<Option>();
                             ap.add(opt);
                             
                             if (!allUnifs) break; // returns only the first unification
                             if (r.hasNext()) {
                                 // create a new option for the next loop step
                                 opt = new Option(opt.getPlan(), null);
                             }
                         }
                     }
                 }
             } 
         }
         return ap;
     }
     
     public void updateEvents(List<Literal>[] result, Intention focus) {
         if (result == null) return;
         // create the events
         for (Literal ladd: result[0]) {
             Trigger te = new Trigger(TEOperator.add, TEType.belief, ladd);
             updateEvents(new Event(te, focus));
             focus = Intention.EmptyInt;
         }
         for (Literal lrem: result[1]) {
             Trigger te = new Trigger(TEOperator.del, TEType.belief, lrem);
             updateEvents(new Event(te, focus));
             focus = Intention.EmptyInt;
         }
     }
 
     // only add External Event if it is relevant in respect to the PlanLibrary
     public void updateEvents(Event e) {
         // Note: we have to add events even if they are not relevant to
         // a) allow the user to override selectOption and then provide an "unknown" plan; or then
         // b) create the failure event (it is done by SelRelPlan)
         if (e.isInternal() || C.hasListener() || ag.getPL().hasCandidatePlan(e.trigger)) {
             C.addEvent(e);
             if (logger.isLoggable(Level.FINE)) logger.fine("Added event " + e);
         }
     }
     
     /** remove the top action and requeue the current intention */
     private void updateIntention() {
     	if (!conf.C.SI.isFinished()) {
 	        IntendedMeans im = conf.C.SI.peek();
 	        im.removeCurrentStep();
 	        confP.C.addIntention(conf.C.SI);
     	} else {
     		logger.fine("trying to update a finished intention!");
     	}
     }
 
     /** generate a failure event for the current intention */
     private void generateGoalDeletion() throws JasonException {
         IntendedMeans im = conf.C.SI.peek();
         if (im.isGoalAdd()) {
             Event failEvent = findEventForFailure(conf.C.SI, im.getTrigger());
             if (failEvent != null) {
                 confP.C.addEvent(failEvent);
                 if (logger.isLoggable(Level.FINE)) logger.fine("Generating goal deletion " + failEvent.getTrigger() + " from goal: " + im.getTrigger());
             } else {
                 logger.warning("No fail event was generated for " + im.getTrigger());
             }
         }
         // if "discard" is set, we are deleting the whole intention!
         // it is simply not going back to 'I' nor anywhere else!
         else if (setts.requeue()) {
             // get the external event (or the one that started
             // the whole focus of attention) and requeue it
             im = conf.C.SI.get(0);
             confP.C.addExternalEv(im.getTrigger());
         } else {
             logger.warning("Could not finish intention: " + conf.C.SI);
         }
     }
 
     // similar to the one above, but for an Event rather than intention
     private void generateGoalDeletionFromEvent() throws JasonException {
         Event ev = conf.C.SE;
         if (ev == null) {
             logger.warning("** It is not possible to generate a goal deletion event because SE is null! " + conf.C);
             return;
         }
         
         Trigger tevent = ev.trigger;
 
         if (tevent.isAddition() && tevent.isGoal()) {
             Event failEvent = findEventForFailure(ev.intention, tevent);
             if (failEvent != null) {
                 logger.warning("Generating goal deletion " + failEvent.getTrigger() + " from event: " + ev.getTrigger());
                 confP.C.addEvent(failEvent);
             } else {
                 logger.warning("No fail event was generated for " + ev.getTrigger());
             }
         } else if (ev.isInternal()) {
             logger.warning("Could not finish intention:\n" + ev.intention);
         }
         // if "discard" is set, we are deleting the whole intention!
         // it is simply not going back to I nor anywhere else!
         else if (setts.requeue()) {
             confP.C.addEvent(ev);
             logger.warning("Requeing external event: " + ev);
         } else
             logger.warning("Discarding external event: " + ev);
     }
 
     public Event findEventForFailure(Intention i, Trigger tevent) {
         Trigger failTrigger = new Trigger(TEOperator.del, tevent.getType(), tevent.getLiteral());
     	if (i != Intention.EmptyInt) {
 	        ListIterator<IntendedMeans> ii = i.iterator();
 	        while (!getAg().getPL().hasCandidatePlan(failTrigger) && ii.hasPrevious()) {
 	            IntendedMeans im = ii.previous();
 	            tevent = im.getTrigger();
 	            failTrigger = new Trigger(TEOperator.del, tevent.getType(), tevent.getLiteral());
 	        }
     	}
         // if some failure handling plan is found
         if (tevent.isGoal() && getAg().getPL().hasCandidatePlan(failTrigger)) {
             return new Event(failTrigger, i);
         }
         return null;
     }
     
     public boolean canSleep() {
         return !conf.C.hasEvent() && !conf.C.hasIntention() && 
                conf.C.MB.isEmpty() && !conf.C.hasFeedbackAction() && 
                agArch.canSleep();
     }
 
     /**********************************************************************/
     /* MAIN LOOP */
     /**********************************************************************/
     /* infinite loop on one reasoning cycle                               */
     /* plus the other parts of the agent architecture besides             */
     /* the actual transition system of the AS interpreter                 */
     /**********************************************************************/
     public void reasoningCycle() {
         try {
             C.reset();
 
             if (nrcslbr >= setts.nrcbp()) { 
                 nrcslbr = 0;
                 ag.buf(agArch.perceive());
                 agArch.checkMail();
             }
             nrcslbr++; // counting number of cycles since last belief revision
 
             if (canSleep()) {
                 if (ag.pl.getIdlePlans() != null) {
                     logger.fine("generating idle event");
                     C.addExternalEv(PlanLibrary.TE_IDLE);
                 } else {
                     agArch.sleep();
                     return;
                 }
             }
             
             step = State.StartRC;
             do {
                 if (!agArch.isRunning()) return;
                 applySemanticRule();
             } while (step != State.StartRC);
 
             ActionExec action = C.getAction(); 
             if (action != null) {
             	C.getPendingActions().put(action.getIntention().getId(), action);
                 agArch.act(action, C.getFeedbackActions());
             }
 
         } catch (Exception e) {
             logger.log(Level.SEVERE, "*** ERROR in the transition system. "+conf.C+"\nCreating a new C!", e);
             conf.C.create();
         }
     }
 
     // Auxiliary functions
     // (for Internal Actions to be able to access the configuration)
     public Agent getAg() {
         return ag;
     }
 
     public Circumstance getC() {
         return C;
     }
 
     public State getStep() {
         return step;
     }
 
     public Settings getSettings() {
         return setts;
     }
 
     public AgArch getUserAgArch() {
         return agArch;
     }
 
     public Logger getLogger() {
         return logger;
     }
 }
