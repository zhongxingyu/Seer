 package pl.edu.pw.elka.sag.agents.car;
 
 import jade.core.*;
 import jade.core.behaviours.*;
 import jade.lang.acl.*;
 
 import java.io.*;
 import java.util.*;
 
 import pl.edu.pw.elka.sag.constants.*;
 import pl.edu.pw.elka.sag.logic.actions.*;
 import pl.edu.pw.elka.sag.ontology.concepts.*;
 import pl.edu.pw.elka.sag.ontology.predicates.*;
 import pl.edu.pw.elka.sag.util.*;
 
 public class MovementBehaviour extends TickerBehaviour
 {
 	private static final long serialVersionUID = -5290333473439069526L;
 	
 	private final Car car;
 	private final CarMovementInfo movementInfo;
 	
 	public MovementBehaviour(Agent agent, Car car, CarMovementInfo movementInfo)
 	{
 		super(agent, car.getSpeed() / 10);
 		this.car = car;
 		this.movementInfo = movementInfo;
 	}
 	
 	@Override
 	protected void onTick()
 	{
 		int step = getStep();
 		
 		if (step == 0)
 		{
 			car.setPreviousDirection(car.getDirection());
 			car.setDirection(car.getNextDirection());
 			car.setNextDirection(Direction.UNKNOWN);
 			
 			movementInfo.setNextCrossroadsLocation(new GetNextCrossroadsLocationAction().execute(car.getLocation(), car.getDirection()));
 			movementInfo.setNextTrafficLight(null);
 			movementInfo.setNextTrafficLightRuleResult(false);
 			movementInfo.setOtherCarsToCheck(0);
 			movementInfo.setOtherCarsChecked(0);
 			movementInfo.setHasPriority(true);
 			movementInfo.setHasTypePriority(false);
 			
 			car.setStatus(CarStatus.OnCrossroads);
 		}
 		else if (step == 1)
 		{
 			car.setStatus(CarStatus.Driving);
 		}
		else if (step == 2)
 		{
 			requestPossibleDirections();
 		}
		else if (step == 3)
 		{
 			if (car.getNextDirection() == Direction.UNKNOWN)
 			{
 				return;
 			}
 			
 			car.setStatus(CarStatus.NearCrossroads);			
 		}
 		else if (step == 7)
 		{
 			AID trafficLightId = AgentSearchUtilities.findTrafficLight(myAgent, movementInfo.getNextCrossroadsLocation());
 			
 			if (trafficLightId != null)
 			{
 				movementInfo.setNextTrafficLight(trafficLightId);
 				checkTrafficLight();
 			}
 			else
 			{
 				checkOtherCars();
 			}
 			
 			checkTypePriority();
 		}
 		else if (step == 8)
 		{
 			if (!movementInfo.getHasTypePriority())
 			{
 				if (movementInfo.getNextTrafficLight() != null)
 				{
 					if (car.getNextDirection() == null)
 					{
 						return;
 					}
 
 					if (!movementInfo.getNextTrafficLightRuleResult())
 					{
 						checkTrafficLight();
 						return;
 					}
 				}
 				else if (movementInfo.getOtherCarsChecked() < movementInfo.getOtherCarsToCheck())
 				{
 					return;
 				}
 				else if (!movementInfo.getHasPriority())
 				{
 					movementInfo.setHasPriority(true);
 					checkOtherCars();
 					return;
 				}
 			}
 		}
 		
 		new MoveCarAction().execute(car);
 	}
 	
 	int getStep()
 	{
 		if (car.getDirection() == null || car.getDirection() == Direction.UNKNOWN)
 		{
 			return 0;
 		}
 		
 		int position = car.getDirection().hasAnyOfParts(Direction.EAST, Direction.WEST) 
 				? car.getLocation().getX()
 				: car.getLocation().getY();
 				
 		int step = position % 10;
 		
 		if (step != 0 && car.getDirection().hasAnyOfParts(Direction.WEST, Direction.SOUTH))
 		{
 			step = 10 - step;
 		}
 		
 		return step;
 	}
 	
 	private void requestPossibleDirections()
 	{
 		try
 		{
 			ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
 			message.addReceiver(AgentSearchUtilities.findCityAgent(myAgent));
 			message.setConversationId(ConversationTypes.POSSIBLE_DIRECTIONS_CONVERSATION_TYPE);
 			message.setContentObject(new CanTurnOnCrossroadsPredicate(null, movementInfo.getNextCrossroadsLocation()));
 			
 			myAgent.send(message);
 		}
 		catch (IOException e)
 		{
 			e.printStackTrace();
 		}
 	}
 	
 	private void checkTypePriority()
 	{
 		try
 		{
 			movementInfo.setHasTypePriority(false);
 			
 			ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
 			message.addReceiver(AgentSearchUtilities.findHighwayCodeAgent(myAgent));
 			message.setConversationId(ConversationTypes.TYPE_PRIORITY_RULE_CONVERSATION_TYPE);
 			message.setContentObject(new HasCarTypePriorityPredicate(car));
 			
 			myAgent.send(message);
 		}
 		catch (IOException e)
 		{
 			e.printStackTrace();
 		}
 	}
 	
 	private void checkTrafficLight()
 	{
 		try
 		{
 			movementInfo.setNextTrafficLightRuleResult(false);
 			
 			ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
 			message.addReceiver(movementInfo.getNextTrafficLight());
 			message.setConversationId(ConversationTypes.TRAFFIC_LIGHT_STATUS_CONVERSATION_TYPE);
 			message.setContentObject(car.getDirection());
 			
 			myAgent.send(message);
 		}
 		catch (IOException e)
 		{
 			e.printStackTrace();
 		}
 	}
 	
 	private void checkOtherCars()
 	{
 		List<AID> cars = AgentRegistry.getInstance().getAgents(myAgent, CarAgent.class);
 		
 		movementInfo.setOtherCarsToCheck(cars.size());
 		
 		if (cars.size() == 0)
 		{
 			return;
 		}
 		
 		try
 		{
 			ACLMessage message = new ACLMessage(ACLMessage.REQUEST_WHEN);
 			
 			for (AID car : cars)
 			{
 				message.addReceiver(car);
 			}
 			
 			message.setConversationId(ConversationTypes.CAR_CONVERSATION_TYPE);
 			message.setContentObject(movementInfo.getNextCrossroadsLocation());
 			
 			myAgent.send(message);
 		}
 		catch (IOException e)
 		{
 			e.printStackTrace();
 		}
 	}
 }
