 /*  	CASi Context Awareness Simulation Software
  *   Copyright (C) 2012 2012  Moritz Bürger, Marvin Frick, Tobias Mende
  *
  *  This program is free software. It is licensed under the
  *  GNU Lesser General Public License with one clarification.
  *  
  *  You should have received a copy of the 
  *  GNU Lesser General Public License along with this program. 
  *  See the LICENSE.txt file in this projects root folder or visit
  *  <http://www.gnu.org/licenses/lgpl.html> for more details.
  */
 package de.uniluebeck.imis.casi.generator;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.Vector;
 
 import de.uniluebeck.imis.casi.simulation.model.Agent;
 import de.uniluebeck.imis.casi.simulation.model.actionHandling.AbstractAction;
 
 /**
  * @author Marvin Frick
  * 
  *         This class creates a singleton object that can be used to create
  *         Agents. It stores a list of already created agents and offers access
  *         to them through either name or id or whatever.
  * 
  */
 public class ActionCollector {
 	/** The instance of this singleton */
 	private static ActionCollector instance;
 
 	/**
 	 * The List of already created agents.
 	 */
	private HashMap<String,AbstractAction> alreadyCreatedActions = new HashMap<String,AbstractAction>();
 
 	/**
 	 * The private constructor of this singleton
 	 */
 	private ActionCollector() {
 		// just here for prohibiting external access
 	}
 
 	/**
 	 * Getter for the instance of this singleton
 	 * 
 	 * @return the instance
 	 */
 	public static ActionCollector getInstance() {
 		if (instance == null) {
 			instance = new ActionCollector();
 		}
 		return instance;
 	}
 
 	/**
 	 * Add a new action to this HashMap at a given key
 	 * 
	 * @param newAgent
	 *            the new Agent
 	 */
 	public void newAction(String identifier, AbstractAction newAction) {
 		alreadyCreatedActions.put(identifier, newAction);
 	}
 
 	/**
 	 * Simple getter that returns the HashMap of all Actions
 	 * @return all the Actions
 	 */
 	public HashMap<String, AbstractAction> getAlreadyCreatedActions() {
 		return alreadyCreatedActions;
 	}
 
 }
