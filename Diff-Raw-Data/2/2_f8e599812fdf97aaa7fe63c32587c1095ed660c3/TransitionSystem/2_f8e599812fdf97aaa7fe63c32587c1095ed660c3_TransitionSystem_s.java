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
 import jason.architecture.AgArch;
 import jason.asSyntax.Atom;
 import jason.asSyntax.BodyLiteral;
 import jason.asSyntax.DefaultTerm;
 import jason.asSyntax.Literal;
 import jason.asSyntax.LogicalFormula;
 import jason.asSyntax.Plan;
 import jason.asSyntax.PlanLibrary;
 import jason.asSyntax.Pred;
 import jason.asSyntax.Term;
 import jason.asSyntax.Trigger;
 import jason.asSyntax.BodyLiteral.BodyType;
 import jason.bb.BeliefBase;
 import jason.runtime.Settings;
 
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public class TransitionSystem {
 
     public enum State { StartRC, SelEv, RelPl, ApplPl, SelAppl, AddIM, ProcAct, SelInt, ExecInt, ClrInt }
     
     private Logger        logger     = null;
 
     Agent                 ag         = null;
     AgArch                agArch     = null;
     Circumstance          C          = null;
     Settings              setts      = null;
 
     // first step of the SOS 
     private State         step       = State.StartRC;                                                                                               
 
     // number of reasoning cycles since last belief revision
     private int           nrcslbr;                                                                                                             
     
     // both configuration and configuration' point to this
     // object, this is just to make it look more like the SOS
     TransitionSystem      confP;
 
     // both configuration and configuration' point to this
     // object, this is just to make it look more like the SOS
     TransitionSystem      conf;
 
     public TransitionSystem(Agent a, Circumstance c, Settings s, AgArch ar) {
         ag = a;
         C = c;
         agArch = ar;
 
         if (s == null) {
             setts = new Settings();
         } else {
             setts = s;
         }
 
         // we need to initialise this "aliases"
         conf = confP = this;
 
         nrcslbr = setts.nrcbp(); // to do BR to start with
 
         setLogger(agArch);
         if (setts != null) {
             logger.setLevel(setts.logLevel());
         }
     }
 
     public void setLogger(AgArch arch) {
         if (arch != null) {
             logger = Logger.getLogger(TransitionSystem.class.getName() + "." + arch.getAgName());
         } else {
             logger = Logger.getLogger(TransitionSystem.class.getName());
         }
     }
 
     /** ******************************************************************* */
     /* SEMANTIC RULES */
     /** ******************************************************************* */
     private void applySemanticRule() throws JasonException {
         // check the current step in the reasoning cycle
         // only the main parts of the interpretation appear here
         // the individual semantic rules appear below
 
         switch (step) {
         case StartRC:
             applyProcMsg();
             break;
         case SelEv:
             applySelEv();
             break;
         case RelPl:
             applyRelPl();
             break;
         case ApplPl:
             applyApplPl();
             break;
         case SelAppl:
             applySelAppl();
             break;
         case AddIM:
             applyAddIM();
             break;
         case ProcAct:
             applyProcAct();
             break;
         case SelInt:
             applySelInt();
             break;
         case ExecInt:
             applyExecInt();
             break;
         case ClrInt:
             confP.step = State.StartRC;
             applyClrInt(conf.C.SI);
             break;
         }
     }
 
     // the semantic rules are referred to in comments in the functions below
 
     private void applyProcMsg() throws JasonException {
         if (!conf.C.MB.isEmpty()) {
             Message m = conf.ag.selectMessage(conf.C.MB);
             
             // get the content, it can be any term (literal, list, number, ...; see ask)
             Term content = DefaultTerm.parse(m.getPropCont().toString());
 
             // check if an intention was suspended waiting this message
             Intention intention = null;
             if (m.getInReplyTo() != null) {
                 intention = getC().getPendingIntentions().remove(m.getInReplyTo());
             }
             // is it a pending intention?
             if (intention != null) {
                 // unify the message answer with the .send fourth argument
                 // the send that put the intention in Pending state was
                 // something like
                 //    .send(ag1,askOne, value, X)
                 // if the answer was 3, unifies X=3
                 BodyLiteral send = intention.peek().removeCurrentStep();
                if (intention.peek().getUnif().unifies(send.getLiteralFormula().getTerm(3), content)) {
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
 
                 updateEvents(new Event(new Trigger(Trigger.TEAdd, Trigger.TEAchvG, received), Intention.EmptyInt));
             }
         }
         confP.step = State.SelEv;
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
                 confP.step = State.RelPl;
                 return;
             }
         }
         // Rule SelEv2
         // directly to ProcAct if no event to handle
         confP.step = State.ProcAct;
     }
 
     private void applyRelPl() throws JasonException {
         // get all relevant plans for the selected event
         if (conf.C.SE.trigger == null) {
             logger.log(Level.SEVERE, "Event " + C.SE + " has null as trigger! I should not get relevant plan!");
             return;
         }
         confP.C.RP = relevantPlans(conf.C.SE.trigger);
 
         // Rule Rel1
         if (!confP.C.RP.isEmpty() || setts.retrieve()) { 
             // retrieve is mainly for Coo-AgentSpeak
             confP.step = State.ApplPl;
         }
         // Rule Rel2
         else {
             if (conf.C.SE.trigger.isGoal()) {
                 logger.warning("Found a goal for which there is no relevant plan:" + conf.C.SE);
                 generateGoalDeletionFromEvent();
             }
             // e.g. belief addition as internal event, just go ahead
             else if (conf.C.SE.isInternal()) {
                 confP.C.SI = conf.C.SE.intention;
                 updateIntention();
             }
             // if external, then needs to check settings
             else if (setts.requeue()) {
                 confP.C.addEvent(conf.C.SE);
             }
             confP.step = State.ProcAct;
         }
     }
 
     private void applyApplPl() throws JasonException {
         if (confP.C.RP == null) {
             logger.warning("applyPl was called even when RP is null!");
             confP.step = State.ProcAct;
             return;
         }
         confP.C.AP = applicablePlans(confP.C.RP);
 
         // Rule Appl1
         if (!confP.C.AP.isEmpty() || setts.retrieve()) { 
             // retrieve is mainly for Coo-AgentSpeak
             confP.step = State.SelAppl;
         } else { // Rule Appl2
             if (conf.C.SE.trigger.isGoal()) {
                 // can't carry on, no applicable plan.
                 logger.warning("Found a goal for which there is no applicable plan:\n" + conf.C.SE);
                 generateGoalDeletionFromEvent(); 
             }
             // e.g. belief addition as internal event, just go ahead
             // but note that the event was relevant, yet it is possible
             // the programmer just wanted to add the belief and it was
             // relevant by chance, so just carry on instead of dropping the
             // intention
             else if (conf.C.SE.isInternal()) {
                 confP.C.SI = conf.C.SE.intention;
                 updateIntention();
             }
             // if external, then needs to check settings
             else if (setts.requeue()) {
                 confP.C.addEvent(conf.C.SE);
             }
             confP.step = State.ProcAct;
         }
     }
 
     private void applySelAppl() throws JasonException {
         // Rule SelAppl
         confP.C.SO = conf.ag.selectOption(confP.C.AP);
         if (confP.C.SO != null) {
             confP.step = State.AddIM;
             if (logger.isLoggable(Level.FINE)) {
                 logger.fine("Selected option "+confP.C.SO+" for event "+confP.C.SE);
             }
         } else {
             logger.warning("selectOption returned null.");
             generateGoalDeletionFromEvent(); 
             // can't carry on, no applicable plan.
             confP.step = State.ProcAct;
         }
     }
 
     private void applyAddIM() throws JasonException {
         // create a new intended means
         IntendedMeans im = new IntendedMeans(conf.C.SO, (Trigger)conf.C.SE.getTrigger().clone());
 
         // Rule ExtEv
         if (conf.C.SE.intention == Intention.EmptyInt) {
             Intention intention = new Intention();
             intention.push(im);
             confP.C.addIntention(intention);
         }
         // Rule IntEv
         else {
             confP.C.SE.intention.push(im);
             confP.C.addIntention(confP.C.SE.intention);
         }
         confP.step = State.ProcAct;
     }
 
     private void applyProcAct() throws JasonException {
         if (conf.C.FA.isEmpty()) {
             confP.step = State.SelInt;
         } else {
             ActionExec a = conf.ag.selectAction(conf.C.FA);
             confP.C.SI = a.getIntention();
             if (a.getResult()) {
                 updateIntention();
             } else {
                 generateGoalDeletion();
             }
             confP.step = State.ClrInt;
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
             return;
         }
 
         confP.step = State.StartRC;
     }
 
     @SuppressWarnings("unchecked")
     private void applyExecInt() throws JasonException {
         confP.step = State.ClrInt; // default next stop
         
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
         BodyLiteral h = im.getCurrentStep(); 
 
         Literal body = null;
         
         if (h.getType() != BodyType.constraint) { // constraint body is not a literal
             body = (Literal)h.getLiteralFormula().clone();
             u.apply(body);
         }
 
         switch (h.getType()) {
 
         // Rule Action
         case action:
             confP.C.A = new ActionExec((Pred) body, conf.C.SI);
             break;
 
         case internalAction:
             boolean ok = false;
             try {
                 InternalAction ia = ag.getIA(body);
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
 
                 if (ok && !ia.suspendIntention()) {
                     updateIntention();
                 }
             } catch (Exception e) {
                 logger.log(Level.SEVERE, "Error in IA ", e);
                 ok = false;
             }
             if (!ok) {
                 generateGoalDeletion();
             }
             break;
 
         case constraint:
             Iterator<Unifier> iu = ((LogicalFormula)h.getLogicalFormula().clone()).logicalConsequence(ag, u);
             if (iu.hasNext()) {
                 im.unif = iu.next();
                 updateIntention();
             } else {
                 generateGoalDeletion();
             }
             break;
 
         // Rule Achieve
         case achieve:
             // free variables in an event cannot conflict with those in the plan
             body.makeVarsAnnon();
             conf.C.addAchvGoal(body, conf.C.SI);
             break;
 
         // Rule Achieve as a New Focus
         case achieveNF:
             conf.C.addAchvGoal(body, Intention.EmptyInt);
             updateIntention();
             break;
 
         // Rule Test
         case test:
             Literal lInBB = conf.ag.believes(body, u);
             if (lInBB != null) {
                 updateIntention();
             } else {
                 body.makeVarsAnnon();
                 Trigger te = new Trigger(Trigger.TEAdd, Trigger.TETestG, body);
                 if (ag.getPL().isRelevant(te.getPredicateIndicator())) {
                     Event evt = new Event(te, conf.C.SI);
                     logger.warning("Test Goal '" + h + "' failed as simple query. Generating internal event for it...");
                     conf.C.addEvent(evt);
                 } else {
                     generateGoalDeletion();
                 }
             }
             break;
 
             
         case delAddBel: 
             // -+a(1,X) ===> remove a(_,_), add a(1,X)
             // change all vars to anon vars to remove it
             Literal bc = (Literal)body.clone();
             bc.makeTermsAnnon();
             // to delete, create events as external to avoid that
             // remove/add create two events for the same intention
 
             List<Literal>[] result = ag.brf(null, bc, conf.C.SI); // the intention is not the new focus
             if (result != null) { // really add something
                 // generate events
                 updateEvents(result,Intention.EmptyInt);
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
             if (setts.sameFocus()) {
                 newfocus = conf.C.SI;
             }
 
             // call BRF
             result = ag.brf(body,null,conf.C.SI); // the intention is not the new focus
             if (result != null) { // really add something
                 // generate events
                 updateEvents(result,newfocus);
                 if (!setts.sameFocus()) {
                     updateIntention();
                 }                    
             } else {
                 updateIntention();                    
             }
             break;
             
         case delBel:
             newfocus = Intention.EmptyInt;
             if (setts.sameFocus()) {
                 newfocus = conf.C.SI;
             }
 
             // call BRF
             result = ag.brf(null,body, conf.C.SI); // the intention is not the new focus
             if (result != null) { // really add something
                 // generate events
                 updateEvents(result,newfocus);
                 if (!setts.sameFocus()) {
                     updateIntention();
                 }                    
             } else {
                 updateIntention();                    
             }
             break;
         }
     }
 
     public void applyClrInt(Intention i) throws JasonException {
         // Rule ClrInt
         if (i == null) {
             return;
         }
         if (i.isFinished()) {
             // intention finished, remove it
             confP.C.removeIntention(i);
             //conf.C.SI = null;
             return;
         }
 
         IntendedMeans im = i.peek();
         if (!im.isFinished()) {
             // nothing todo
             return;
         }
 
         // remove the finished IM from the top of the intention
         IntendedMeans topIM = i.pop();
         if (logger.isLoggable(Level.FINE)) logger.fine("Returning from IM "+topIM.getPlan().getLabel());
         
         // if finished a failure handling IM ...
         if (im.getTrigger().isGoal() && !im.getTrigger().isAddition()) {
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
             while (!im.unif.unifies(topIM.getTrigger().getLiteral(), im.getTrigger().getLiteral())) {
                 im = i.pop();
             }
         }
         if (!i.isFinished()) {
             im = i.peek(); // +!s
             if (!im.isFinished()) {
                 // removes !b
                 BodyLiteral g = im.removeCurrentStep(); 
                 // make the TE of finished plan ground and unify that
                 // with goal in the body
                 Literal tel = topIM.getPlan().getTriggerEvent().getLiteral();
                 topIM.unif.apply(tel);
                 im.unif.unifies(tel, g.getLiteralFormula());
             }
         }
 
         // the new top may have become
         // empty! need to keep checking.
         applyClrInt(i);
     }
 
     /** ******************************************* */
     /* auxiliary functions for the semantic rules */
     /** ******************************************* */
 
     public List<Option> relevantPlans(Trigger teP) throws JasonException {
         Trigger te = (Trigger) teP.clone();
         List<Option> rp = new LinkedList<Option>();
         List<Plan> candidateRPs = conf.ag.fPL.getAllRelevant(te.getPredicateIndicator());
         if (candidateRPs == null)
             return rp;
         for (Plan pl : candidateRPs) {
             Unifier relUn = pl.relevant(te);
             if (relUn != null) {
                 rp.add(new Option(pl, relUn));
             }
         }
         return rp;
     }
 
     public List<Option> applicablePlans(List<Option> rp) throws JasonException {
         List<Option> ap = new LinkedList<Option>();
         for (Option opt: rp) {
             LogicalFormula context = opt.plan.getContext();
             if (context == null) { // context is true
                 ap.add(opt);
             } else {
                 boolean allUnifs = opt.getPlan().isAllUnifs();
                 Iterator<Unifier> r = context.logicalConsequence(ag, opt.unif);
                 if (r != null) {
                     while (r.hasNext()) {
                         opt.unif = r.next();
                         ap.add(opt);
                         if (!allUnifs) break; // returns only one unification
                         if (r.hasNext()) {
                             // create a new option
                             opt = new Option((Plan)opt.plan.clone(), null);
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
             Trigger te = new Trigger(Trigger.TEAdd, Trigger.TEBel, ladd);
             updateEvents(new Event(te, focus));
         }
         for (Literal lrem: result[1]) {
             Trigger te = new Trigger(Trigger.TEDel, Trigger.TEBel, lrem);
             updateEvents(new Event(te, focus));
         }
     }
 
     // only add External Event if it is relevant in respect to the PlanLibrary
     public void updateEvents(Event e) {
         if (e.isInternal() || C.hasListener() || ag.getPL().isRelevant(e.trigger.getPredicateIndicator())) {
             C.addEvent(e);
             if (logger.isLoggable(Level.FINE)) logger.fine("Added event " + e);
         }
     }
     
     /** remove the top action and requeue the current intention */
     private void updateIntention() {
         IntendedMeans im = conf.C.SI.peek();
         if (!im.getPlan().getBody().isEmpty()) { 
             // maybe it had an empty plan body
             im.getPlan().getBody().remove(0);
         }
         confP.C.addIntention(conf.C.SI);
     }
 
     private void generateGoalDeletion() throws JasonException {
         IntendedMeans im = conf.C.SI.peek();
         if (im.isGoalAdd()) {
             Event failEvent = findEventForFailure(conf.C.SI, im.getTrigger());
             if (failEvent != null) {
                 confP.C.addEvent(failEvent);
                 logger.warning("Generating goal deletion " + failEvent.getTrigger() + " from goal: " + im.getTrigger());
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
         Trigger tevent = ev.trigger;
 
         if (tevent.isAddition() && tevent.isGoal() && ev.isInternal()) {
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
         Trigger failTrigger = new Trigger(Trigger.TEDel, tevent.getGoal(), tevent.getLiteral());
         ListIterator<IntendedMeans> ii = i.iterator();
         while (!getAg().getPL().isRelevant(failTrigger.getPredicateIndicator()) && ii.hasPrevious()) {
             IntendedMeans im = ii.previous();
             tevent = im.getTrigger();
             failTrigger = new Trigger(Trigger.TEDel, tevent.getGoal(), tevent.getLiteral());
         }
         // if some failure handling plan is found
         if (tevent.isGoal() && getAg().getPL().isRelevant(failTrigger.getPredicateIndicator())) {
             return new Event(failTrigger, i);
         }
         return null;
     }
     
     /** ********************************************************************* */
 
     boolean canSleep() {
         return !conf.C.hasEvent() && !conf.C.hasIntention() && conf.C.MB.isEmpty() && conf.C.FA.isEmpty() && agArch.canSleep();
     }
 
     /** waits for a new message */
     synchronized private void waitMessage() {
         try {
             logger.fine("Waiting message....");
             wait(500); // wait for messages
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     synchronized public void newMessageHasArrived() {
         notifyAll(); // notify waitMessage method
     }
 
     private Object  syncMonitor       = new Object(); 
 
     private boolean inWaitSyncMonitor = false;
 
     /**
      * waits for a signal to continue the execution (used in synchronized
      * execution mode)
      */
     private void waitSyncSignal() {
         try {
             synchronized (syncMonitor) {
                 inWaitSyncMonitor = true;
                 syncMonitor.wait();
                 inWaitSyncMonitor = false;
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     /**
      * inform this agent that it can continue, if it is in sync mode and
      * wainting a signal
      */
     public void receiveSyncSignal() {
         try {
             synchronized (syncMonitor) {
                 while (!inWaitSyncMonitor) {
                     // waits the agent to enter in waitSyncSignal
                     syncMonitor.wait(50); 
                     if (!agArch.isRunning()) {
                         break;
                     }
                 }
 
                 syncMonitor.notifyAll();
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     /** ******************************************************************* */
     /* MAIN LOOP */
     /** ******************************************************************* */
     /* infinite loop on one reasoning cycle */
     /* plus the other parts of the agent architecture besides */
     /* the actual transition system of the AS interpreter */
     /** ******************************************************************* */
     public void reasoningCycle() {
         try {
 
             if (setts.isSync()) {
                 waitSyncSignal();
             } else if (canSleep()) {
                 if (getAg().fPL.getIdlePlans() != null) {
                     logger.fine("generating idle event");
                     C.addExternalEv(PlanLibrary.TE_IDLE);
                 } else {
                     if (nrcslbr <= 1) {
                         waitMessage();
                     }
                 }
             }
 
             C.reset();
 
             if (nrcslbr >= setts.nrcbp() || canSleep()) {
                 nrcslbr = 0;
 
                 // logger.fine("perceiving...");
                 List<Literal> percept = agArch.perceive();
 
                 // logger.fine("doing belief revision...");
                 ag.buf(percept);
 
                 // logger.fine("checking mail...");
                 agArch.checkMail();
             }
 
             do {
                 applySemanticRule();
             } while (step != State.StartRC); // finished a reasoning cycle
 
             if (C.getAction() != null) {
                 agArch.act(C.getAction(), C.getFeedbackActions());
             }
 
             // counting number of cycles since last belief revision
             nrcslbr++;
 
             if (setts.isSync()) {
                 boolean isBreakPoint = false;
                 try {
                     isBreakPoint = getC().getSelectedOption().getPlan().hasBreakpoint();
                     if (logger.isLoggable(Level.FINE)) {
                         logger.fine("Informing controller that I finished a reasoning cycle. Breakpoint is " + isBreakPoint);
                     }
                 } catch (NullPointerException e) {
                     // no problem, the is no sel opt, no plan ....
                 }
                 agArch.getArchInfraTier().informCycleFinished(isBreakPoint, agArch.getCycleNumber());
             }
 
         } catch (Exception e) {
             logger.log(Level.SEVERE, "*** ERROR in the transition system.", e);
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
