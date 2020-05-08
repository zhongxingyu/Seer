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
 // CVS information:
 //   $Date$
 //   $Revision$
 //   $Log$
 //   Revision 1.26  2006/01/14 18:22:45  jomifred
 //   centralised infra does not use xml script file anymore
 //
 //   Revision 1.25  2006/01/06 12:05:37  jomifred
 //   operator - removes bel from BB and changes the current unifier.
 //
 //   Revision 1.24  2006/01/04 02:54:41  jomifred
 //   using java log API instead of apache log
 //
 //   Revision 1.23  2006/01/02 13:49:00  jomifred
 //   add plan unique id, fix some bugs
 //
 //   Revision 1.22  2005/12/31 16:29:58  jomifred
 //   add operator =..
 //
 //   Revision 1.21  2005/12/30 20:40:16  jomifred
 //   new features: unnamed var, var with annots, TE as var
 //
 //   Revision 1.20  2005/12/23 00:48:22  jomifred
 //   StringTerm is now an interface implemented by StringTermImpl
 //
 //   Revision 1.18  2005/10/30 18:38:34  jomifred
 //   change in the AgArch customisation  support (the same customisation is used both to Cent and Saci infrastructures0
 //
 //   Revision 1.17  2005/08/23 17:09:28  jomifred
 //   fix bug in logger
 //
 //   Revision 1.16  2005/08/18 11:38:19  jomifred
 //   do not add <true> or <false> in BB
 //
 //   Revision 1.15  2005/08/15 17:41:36  jomifred
 //   AgentArchitecture renamed to AgArchInterface
 //
 //   Revision 1.14  2005/08/12 22:18:37  jomifred
 //   add cvs keywords
 //
 //
 //----------------------------------------------------------------------------
 
 package jason.asSemantics;
 
 import jIDE.JasonID;
 import jason.JasonException;
 import jason.architecture.AgArchInterface;
 import jason.asSyntax.BeliefBase;
 import jason.asSyntax.Literal;
 import jason.asSyntax.Plan;
 import jason.asSyntax.PlanLibrary;
 import jason.asSyntax.StringTerm;
 import jason.asSyntax.Term;
 import jason.asSyntax.Trigger;
 import jason.asSyntax.parser.ParseException;
 import jason.asSyntax.parser.as2j;
 import jason.runtime.Settings;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.util.Iterator;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 
 /**
  * Agent class has the belief base and plan library of an AgentSpeak agent.
  * It also implements the default selection functions of the AgentSpeak semantics.
  */
 public class Agent {
 
 	
 	// Members
 	protected BeliefBase fBS = new BeliefBase();
 	protected PlanLibrary fPS = new PlanLibrary();
 
 	protected TransitionSystem fTS = null;
 
 	private Logger logger;
 	
     /** creates the TS of this agent, parse its AS source, and set its Settings */
     public TransitionSystem initAg(AgArchInterface arch, String asSrc, Settings stts) throws JasonException {
         // set the agent
         try {
 			setLogger(arch);
 			logger.setLevel(stts.logLevel());
 			setTS(new TransitionSystem(this, new Circumstance(), stts, arch));
 
 			parseAS(asSrc);
 			// kqml Plans at the end of the ag PS
 			parseAS(JasonID.class.getResource("/asl/kqmlPlans.asl"));
 			
 			return fTS;
 		} catch (Exception e) {
 			logger.log(Level.SEVERE,"Error creating the agent class!",e);
 			throw new JasonException("Error creating the agent class! - " + e);
 		}
     }
 
     public void setLogger(AgArchInterface arch) {
 		if (arch != null) {
 			logger = Logger.getLogger(Agent.class.getName()+"."+arch.getAgName());
 		} else {
 			logger = Logger.getLogger(Agent.class.getName());			
 		}
 	}
 
 	/** add beliefs and plan form a URL */
 	public boolean parseAS(URL asURL) {
 		try {
 			parseAS(asURL.openStream());
 			logger.fine("as2j: AgentSpeak program '"+asURL+"' parsed successfully!");
 			return true;
 		} catch (IOException e) {
 			logger.log(Level.SEVERE,"as2j: the AgentSpeak source file was not found",e);
 		} catch (ParseException e) {
 			logger.log(Level.SEVERE,"as2j: error parsing \"" + asURL + "\"",e);
 		}
 		return false;
 	}
 
 	/** add beliefs and plan form a file */
 	public boolean parseAS(String asFileName) {
 		try {
 			parseAS(new FileInputStream(asFileName));
 			logger.fine("as2j: AgentSpeak program '"+asFileName+"' parsed successfully!");
 			return true;
 		} catch (FileNotFoundException e) {
 			logger.log(Level.SEVERE,"as2j: the AgentSpeak source file was not found", e);
 		} catch (ParseException e) {
 			logger.log(Level.SEVERE,"as2j: error parsing \"" + asFileName + "\"", e);
 		}
 		return false;
 	}
 	void parseAS(InputStream asIn) throws ParseException {
 		as2j parser = new as2j(asIn);
 		parser.ag(this);
 	}
 
 	
 	/** Follows the default implementation for the agent's
 	 *  message acceptance relation and selection functions
 	 */
 	public boolean socAcc(Message m) {
 		return true;
 	}
 	
 	public Event selectEvent(List evList) {
 		// make sure the selected Event is removed from evList
 		return ((Event) evList.remove(0));
 	}
 
 	public Option selectOption(List optList) {
 		if (optList.size() > 0) {
 			return ((Option) optList.remove(0));
 		} else {
 			return null;
 		}
 	}
 
 	public Intention selectIntention(List intList) {
 		// make sure the selected Intention is removed from intList AND
 		// make sure no intention will "starve"!!!
 		return ((Intention) intList.remove(0));
 	}
 
 	public Message selectMessage(List msgList) {
 		// make sure the selected Message is removed from msgList
 		return ((Message) msgList.remove(0));
 	}
 
 	public ActionExec selectAction(List actList) {
 		// make sure the selected Action is removed from actList
 		return ((ActionExec) actList.remove(0));
 	}
 
 	/** Agent's Source Beliefs Initialisation (called by the parser) */
 	/*
 	public void importBS(BeliefBase bb) {
 		Iterator i = bb.allIterator();
 		while (i.hasNext()) {
 			Literal l = (Literal)i.next();
 			addBel(l, BeliefBase.TSelf, fTS.C, Intention.EmptyInt);
 		}
 		//fBS.addAll(bb);
 	}
 	*/
 
 	/** Agent's Source Plans Initialisation (called by the parser) */
 	public void addPS(PlanLibrary pp) throws JasonException {
 		fPS.addAll(pp);
 	}
 
 	/** TS Initialisation (called by the AgArch) */
 	public void setTS(TransitionSystem ts) {
 		this.fTS = ts;
 	}
 	public TransitionSystem getTS() {
 		return fTS;
 	}
 
 	// Accessing the agent's belief base and plans
 	public BeliefBase getBS() {
 		return fBS;
 	}
 
 	public PlanLibrary getPS() {
 		return fPS;
 	}
 
 
 	/** Belief Revision Function: add/remove perceptions into belief base */
     public void brf(List percepts) {
         if (percepts == null) {
             return;
         }
 		
        // deleting percepts in the BB that is not percepted anymore
         List perceptsInBB = getBS().getPercepts();
         for (int i=0; i<perceptsInBB.size(); i++) { 
             Literal l = (Literal)perceptsInBB.get(i);
            // could not use percepts.contains(l), since equalsAsTerm must be used
            boolean wasPercepted = false;
             for (int j=0; j< percepts.size(); j++) {
            	Term t = (Term)percepts.get(j); // it probably is a Pred
            	if (l.equalsAsTerm(t)) { // if percept t already is in BB
            		wasPercepted = true;
             	}
             }
            if (!wasPercepted) {
                 if (delBel(l,BeliefBase.TPercept,fTS.getC(), Intention.EmptyInt)) {
                 	i--;
                 }
             }
         }
 
         // addBel only adds a belief when appropriate
         // checking all percepts for new beliefs
         Iterator i = percepts.iterator();
         while (i.hasNext()) {
 			Literal l = (Literal)i.next();
 			try {
 				addBel( l, BeliefBase.TPercept, fTS.getC(), Intention.EmptyInt);
 			} catch (Exception e) {
 				logger.log(Level.SEVERE,"Error adding percetion "+l,e);
 			}
         }
     }
 	
 	// Other auxiliary methods
 	public boolean believes(Literal l, Unifier un) {
 		List relB = fBS.getRelevant(l);
 		if (relB != null) {
 			for (int i=0; i < relB.size(); i++) {
 				Literal b = (Literal) relB.get(i);
 				// old version Unifier newUn = (un == null) ? new Unifier() : (Unifier) un.clone();
 				// recall that order is important because of annotations!
 				if (un.unifies(l,b)) {
 					// if literals are negated or not
 					//old version return newUn;
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	public Literal findBel(Literal l, Unifier un) {
 		List relB = fBS.getRelevant(l);
 		if (relB != null) {
 			for (int i=0; i < relB.size(); i++) {
 				Literal b = (Literal) relB.get(i);
 				Unifier newUn = (un == null) ? new Unifier() : (Unifier) un.clone();
 				// recall that order is important because of annotations!
 				if (newUn.unifies(l,b))
 					// if literals are negated or not
 					return b;
 			}
 		}
 		return null;
 	}
 	
 
 	/** Adds a new Literal <i>l</i> in BB with "source(<i>source</i>)" annotation.
 	 *  <i>l</i> will be cloned before being added in the BB */
 	public boolean addBel(Literal l, Term source, Circumstance c, Intention focus) {
 		if (source != null && !source.isGround()) {
 			logger.log(Level.SEVERE,"Error: Annotations must be ground!\n Cannot use "+source+" as annotation.");
 		} else if (l.equals(Literal.LTrue) || l.equals(Literal.LFalse)) {
 			logger.log(Level.SEVERE,"Error: <true> or <false> can not be added as beliefs.");				
 		} else {
 			l = (Literal)l.clone();
 			if (source != null) {
 				l.addAnnot(source);
 			}
 			if (fBS.add(l)) {
 				logger.fine("Added belief "+l);
 				updateEvents(new Event(new Trigger(Trigger.TEAdd, Trigger.TEBel, l), focus), c);
 				return true;
 			}
 			return false;
 		}
 		return false;
 	}
 
 	public boolean delBel(Literal l, Term source, Circumstance c, Intention focus) {
 		if (source != null && !source.isGround()) {
 			logger.log(Level.SEVERE,"Error: Annotations must be ground!\n Cannot use "+source+" as annotation.");
 		} else {
 			if (source != null) {
 				//l.clearAnnot();
 				l.addAnnot(source);
 			}
 			if (fBS.remove(l)) {
 				logger.fine("Removed belief "+l);
 				updateEvents(new Event(new Trigger(Trigger.TEDel, Trigger.TEBel, l), focus), c);
 				return true;
 			}
 		}
 		return false;
 	}
 
 	// only add External Event if it is relevant in respect to the PlanLibrary
 	public void updateEvents(Event e, Circumstance c) {
 		if (c != null) {
 			if (e.isInternal() || fPS.isRelevant(e.trigger)) {
 				c.E.add(e);
 				logger.fine("Added event "+e);
 			}
 		}
 	}
 
 	public void addPlan(StringTerm stPlan, Term tSource) {
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
 			int i = fPS.indexOf(p);
 			if (i < 0) {
 				fPS.add(p);
 			} else {
 				p = (Plan) fPS.get(i);
 			}
 			if (tSource != null) {
 				p.getLabel().addSource(tSource);
 			}
 			
 			//System.out.println("**** adding plan "+p+" from "+sSource);		
 
 		} catch (Exception e) {
 			logger.log(Level.SEVERE,"Error adding plan "+sPlan,e);
 		}
 	}
 
 	/** 
 	 * remove a plan represented by the string sPlan 
 	 * that comes from source (normally the agent name)
 	 */
 	public void removePlan(StringTerm sPlan, Term source) {
 		Plan p = Plan.parse(sPlan.getString());
 		int i = fPS.indexOf(p);
 		if (i >= 0) {
 			p = (Plan)fPS.get(i);
 			boolean hasSource = p.getLabel().delSource(source);
 
 			// if no source anymore, remove the plan
 			if (hasSource && ! p.getLabel().hasSource()) {
 				fPS.remove(i);
 			}
 		} else {
 			logger.log(Level.SEVERE,"Plan '"+p+"' was not found for deletion!");
 		}
 	}
 
 	static DocumentBuilder builder = null;
 	
 
 	/** get the agent "mind" (Beliefs, plans and circumstance) as XML */
 	public Document getAgState() {
 		if (builder == null) {
 			try {
 				builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
 			} catch (Exception e) {
 				logger.log(Level.SEVERE,"Error creating XML builder\n");
 				return null;
 			}
 		}
 		Document document = builder.newDocument();
 		Element ag = getAsDOM(document);
 		document.appendChild(ag);
 		
 		ag.appendChild(fTS.getC().getAsDOM(document));
 		return document;
 	}
 	
 	/** get the agent "mind" as XML */
 	public Element getAsDOM(Document document) {
 		Element ag = (Element) document.createElement("agent");
 		ag.setAttribute("name", fTS.getAgArch().getAgName());
 		ag.appendChild(fBS.getAsDOM(document));
 		//ag.appendChild(ps.getAsDOM(document));
 		return ag;
 	}
 
 }
