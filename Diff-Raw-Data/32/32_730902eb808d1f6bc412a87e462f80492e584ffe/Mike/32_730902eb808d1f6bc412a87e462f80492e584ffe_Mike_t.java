 /*  	CASi Context Awareness Simulation Software
  *   Copyright (C) 2011 2012  Moritz Bürger, Marvin Frick, Tobias Mende
  *
  *  This program is free software. It is licensed under the
  *  GNU Lesser General Public License with one clarification.
  *  
  *  You should have received a copy of the 
  *  GNU Lesser General Public License along with this program. 
  *  See the LICENSE.txt file in this projects root folder or visit
  *  <http://www.gnu.org/licenses/lgpl.html> for more details.
  */
 package de.uniluebeck.imis.casi.simulation.model.mackComponents;
 
 import java.awt.Point;
 import java.awt.geom.Point2D;
 import java.util.HashMap;
 import java.util.Random;
 import java.util.logging.Logger;
 
 import de.uniluebeck.imis.casi.CASi;
 import de.uniluebeck.imis.casi.communication.mack.MACKProtocolFactory;
 import de.uniluebeck.imis.casi.simulation.engine.SimulationClock;
 import de.uniluebeck.imis.casi.simulation.engine.SimulationEngine;
 import de.uniluebeck.imis.casi.simulation.model.AbstractInteractionComponent;
 import de.uniluebeck.imis.casi.simulation.model.Agent;
 import de.uniluebeck.imis.casi.simulation.model.Room;
 import de.uniluebeck.imis.casi.simulation.model.SimulationTime;
 import de.uniluebeck.imis.casi.simulation.model.actionHandling.AbstractAction;
 import de.uniluebeck.imis.casi.simulation.model.actions.HaveAMeeting;
 import de.uniluebeck.imis.casi.simulation.model.actions.SpeakTo;
 
 /**
  * This class represents the MATe component "Mike" which is used to detect
  * currently speaking agents.
  * 
  * @author Tobias Mende
  * 
  */
 public class Mike extends AbstractInteractionComponent {
 	/** the development logger */
 	private static final Logger log = Logger.getLogger(Mike.class.getName());
 	/** serialization identifier */
 	private static final long serialVersionUID = -2951196032563233461L;
 	/** Counter for the mike instances */
 	private static int idCounter;
 	/** The last speakers that where detected by this component */
 	private HashMap<String, String> values = new HashMap<String, String>();
 	/** The message which should be send. */
 	private String messageToSend;
 
 	/**
 	 * Constructor for a new mike which is positioned at the central point of
 	 * the observed room
 	 * 
 	 * @param room
 	 *            the room to observe
 	 * @param agent
 	 *            the agent to which this mike belongs
 	 */
 	public Mike(Room room, Agent agent) {
 		this(room, room.getCentralPoint(), agent);
 	}
 
 	/**
 	 * Constructor for a mike which observes a given room and is positioned at
 	 * an explicit point.
 	 * 
 	 * @param room
 	 *            the room which is observed by the mike
 	 * @param position
 	 *            the position where this mike stands
 	 * @param agent
 	 *            the agent to which this component belongs
 	 */
 	public Mike(Room room, Point position, Agent agent) {
 		this(room, (Point2D) position, agent);
 	}
 
 	/**
 	 * Private Constructor (just to prevent others from using point2d as
 	 * position)
 	 * 
 	 * @param room
 	 *            the room to observe
 	 * @param position
 	 *            the coordinates of this component.
 	 * @param agent
 	 *            the agent to which this component belongs
 	 */
 	private Mike(Room room, Point2D position, Agent agent) {
 		super("mike-" + agent.getIdentifier() + idCounter++, position);
 		interestingActions.add(SpeakTo.class);
 		interestingActions.add(HaveAMeeting.class);
 		setShapeRepresentation(room.getShapeRepresentation());
 		SimulationClock.getInstance().addListener(this);
 		PULL_INTERVALL = 30;
 		type = Type.SENSOR;
 		this.agent = agent;
 	}
 
 	@Override
 	protected boolean handleInternal(AbstractAction action, Agent agent) {
 		if (messageToSend == null) {
 			AbstractAction interestingPart = getInterestingPart(action);
 			if (interestingPart == null) {
 				// not interested in this action
 				return true;
 			}
 			values = new HashMap<String, String>();
 			if (interestingPart instanceof SpeakTo) {
 				values.put("speaker1", agent.toString());
 				values.put("speaker2", ((SpeakTo) interestingPart).getAgent()
 						.toString());
 				CASi.SIM_LOG.info(String.format(
 						"%s has detected voices of %s and %s", this.toString(),
 						agent.toString(), ((SpeakTo) interestingPart)
 								.getAgent().toString()));
 			} else if (interestingPart instanceof HaveAMeeting) {
 				HaveAMeeting meeting = (HaveAMeeting) interestingPart;
 				Random rand = new Random(System.currentTimeMillis());
 				int escape = 0;
 				int first = 0;
 				int second = 0;
 				int size = meeting.getAgentsInMeeting().size();
 				while (first == second && escape < 50) {
 					first = rand.nextInt(size);
 					second = rand.nextInt(size);
 					escape++;
 				}
 				values.put("speaker1", meeting.getAgentsInMeeting().get(first));
 				values.put("speaker2", meeting.getAgentsInMeeting().get(second));
 				CASi.SIM_LOG.info(String.format(
 						"%s has detected voices of %s and %s", this.toString(),
 						meeting.getAgentsInMeeting().get(first), meeting
 								.getAgentsInMeeting().get(second)));
 				lastValue = values;
 
 			} else {
 				log.warning("Unimplemented Case for action: " + interestingPart);
 				return true;
 			}
 
 			messageToSend = MACKProtocolFactory.generatePushMessage(agent,
 					"mike", values);
 		}
 
 		return true;
 	}
 
 	@Override
 	protected boolean checkInterest(Agent agent) {
 		return true;
 	}
 
 	@Override
 	public String getHumanReadableValue() {
 		StringBuffer buf = new StringBuffer();
 		for (String name : values.values()) {
 			buf.append(name + ", ");
 		}
 		if (!values.isEmpty()) {
 			buf.delete(buf.length() - 2, buf.length() - 1);
 		}
 		return buf.toString();
 	}
 
 	@Override
 	public String getType() {
 		return "mike";
 	}
 
 	@Override
 	protected void sendRecurringMessage(SimulationTime newTime) {
 		if (messageToSend != null) {
 			SimulationEngine.getInstance().getCommunicationHandler()
 					.send(this, messageToSend);
 			messageToSend = null;
 			/* Send delay (speak-time) of 10 till 120 seconds */
 			clockTickCounter = 1;
 			Random rand = new Random(System.currentTimeMillis());
 			PULL_INTERVALL = 10 + rand.nextInt(110);
 		}
 	}
 
 }
