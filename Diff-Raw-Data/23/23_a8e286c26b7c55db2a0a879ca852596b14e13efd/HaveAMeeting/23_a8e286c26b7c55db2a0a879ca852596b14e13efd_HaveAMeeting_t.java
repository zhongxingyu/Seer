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
 package de.uniluebeck.imis.casi.simulation.model.actions;
 
 import de.uniluebeck.imis.casi.simulation.model.AbstractComponent;
 import de.uniluebeck.imis.casi.simulation.model.IPosition;
 import de.uniluebeck.imis.casi.simulation.model.SimulationTime;
 import de.uniluebeck.imis.casi.simulation.model.actionHandling.ComplexAction;
 
 /**
  * Represents a Meeting. This Action is most likely to be created from within a
  * CreateAMeeting Action during simulation runtime. It is sonsidered a
  * interrupting action as it is supposed to be injected into an agents ToDoList
  * at a given time.
  * 
  * @author Marvin Frick
  * 
  * 
  */
 public class HaveAMeeting extends ComplexAction {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 5933636748690158732L;
 
	private IPosition where;
 	
 	public HaveAMeeting(IPosition where, SimulationTime supposedStartTime, int howLong, int prio) {
 		super();
 		this.setEarliestStartTime(supposedStartTime);
 		this.where = where;
 		addSubAction(new Move(where));
 		addSubAction(new StayHere(howLong, prio));
 	}
 	
 	@Override
 	public String getInformationDescription() {
 		String res;
 		res = String.format("%s: at %s @ %s", this.getClass()
 				.getName(), this.getEarliestStartTime().toString(),where.toString());
 		return res;
 	}
	
	@Override
	public ComplexAction clone() {
		// TODO Auto-generated method stub
		return super.clone();
	}
 }
