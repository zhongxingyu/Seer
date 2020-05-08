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
 package de.uniluebeck.imis.casi.simulations.mate.generator.java;
 
 import de.uniluebeck.imis.casi.generator.AgentCollector;
 import de.uniluebeck.imis.casi.generator.RoomCollector;
 import de.uniluebeck.imis.casi.simulation.model.Agent;
 
 /**
  * Agent generator file with static methods that generate all the agents for
  * MATe simulation environment World.
  * 
  * Put all your agents in here!
  * 
  * @author Marvin Frick
  * 
  */
 public class Agents {
 
 	/**
 	 * Fills the AgentsGenerator singleton object with all the agents.
 	 * 
 	 * Put all your Agents here!
 	 */
 	public static void generateAgents() {
 
 		AgentCollector agents = AgentCollector.getInstance();
 		RoomCollector rooms = RoomCollector.getInstance();
 		Agent tempAgent = null;
 
 		// if we need a lot of agents...
 		for (int i = 0; i < 0; i++) {
 			tempAgent = new Agent("agent_" + i + "_smith", "A. Smith the " + i,
 					"crowd");
 			agents.newAgent(tempAgent);
 		}
 
 		// ##########
 		// Hermann Matsumbishi
 		// ##########
 		tempAgent = new Agent("casi_hermann_matsumbishi",
 				"Hermann Matsumbishi", "teamleader");
 		tempAgent.setDefaultPosition(rooms
 				.findRoomByIdentifier("officeHermann"));
 		agents.newAgent(tempAgent);
 
 		// ##########
 		// Zwotah Zwiebel
 		// ##########
		agents.newAgent(new Agent("casi_zwota_zwiebel", "Zwotah Zwiebel",
 				"coworker"));
 		Agent zwotah = agents.findAgentByName("Zwotah Zwiebel");
 		zwotah.setDefaultPosition(rooms.findRoomByIdentifier("officeZwotah"));
 
 		// ##########
 		// Dagobert Dreieck
 		// ##########
 		tempAgent = new Agent("casi_dagobert_dreieck", "Dagobert Dreieck",
 				"coworker");
 		tempAgent.setDefaultPosition(rooms
 				.findRoomByIdentifier("officeDagobert"));
 		agents.newAgent(tempAgent);
 
 		// ##########
 		// Felix Freudentanz
 		// ##########
 		tempAgent = new Agent("casi_felix_freudentanz", "Felix Freudentanz",
 				"teamLeader");
 		tempAgent.setDefaultPosition(rooms.findRoomByIdentifier("officeFelix"));
 		agents.newAgent(tempAgent);
 
 		// ##########
 		// Susi Sekretärin
 		// ##########
 		tempAgent = new Agent("casi_susi_sekretärin", "Susi Sekretärin",
 				"secretary");
 		tempAgent.setDefaultPosition(rooms.findRoomByIdentifier("officeSusi"));
 		agents.newAgent(tempAgent);
 
 		// ##########
 		// Rudi Random
 		// ##########
 		tempAgent = new Agent("casi_rudi_random", "Rudi Random",
 				"coworker");
 		tempAgent.setDefaultPosition(rooms.findRoomByIdentifier("officeRudi"));
 		agents.newAgent(tempAgent);
 	}
 }
