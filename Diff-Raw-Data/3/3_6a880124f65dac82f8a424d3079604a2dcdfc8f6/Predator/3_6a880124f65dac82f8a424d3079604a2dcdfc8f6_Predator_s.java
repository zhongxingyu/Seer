 import java.io.*;
 import java.lang.*;
 import java.util.*;
 
 
 /** This class defines the functionality of the predator. */
 public class Predator extends Agent
 {
 	class Position
 	{
 		@Override
 		public String toString()
 		{
 			return "(" + x + ", " + y + ")";
 		}
 
 		public int x;
 		public int y;
 
 		Position(int x, int y)
 		{
 			this.x = x;
 			this.y = y;
 		}
 
 		@Override
 		public int hashCode()
 		{
 			final int prime = 31;
 			int result = 1;
 			result = prime * result + getOuterType().hashCode();
 			result = prime * result + x;
 			result = prime * result + y;
 			return result;
 		}
 
 		@Override
 		public boolean equals(Object obj)
 		{
 			if (this == obj)
 			{
 				return true;
 			}
 			if (obj == null)
 			{
 				return false;
 			}
 			if (!(obj instanceof Position))
 			{
 				return false;
 			}
 			Position other = (Position) obj;
 			if (!getOuterType().equals(other.getOuterType()))
 			{
 				return false;
 			}
 			if (x != other.x)
 			{
 				return false;
 			}
 			if (y != other.y)
 			{
 				return false;
 			}
 			return true;
 		}
 
 		private Predator getOuterType()
 		{
 			return Predator.this;
 		}
 	}
 
 	class State
 	{
 		public Position predatorPos;
 		public Position preyPos;
 
 		@Override
 		public int hashCode()
 		{
 			final int prime = 31;
 			int result = 1;
 			result = prime * result + getOuterType().hashCode();
 			result = prime * result
 					+ ((predatorPos == null) ? 0 : predatorPos.hashCode());
 			result = prime * result
 					+ ((preyPos == null) ? 0 : preyPos.hashCode());
 			return result;
 		}
 
 		@Override
 		public boolean equals(Object obj)
 		{
 			if (this == obj)
 				return true;
 			if (obj == null)
 				return false;
 			if (getClass() != obj.getClass())
 				return false;
 			State other = (State) obj;
 			if (!getOuterType().equals(other.getOuterType()))
 				return false;
 			if (predatorPos == null)
 			{
 				if (other.predatorPos != null)
 					return false;
 			} else if (!predatorPos.equals(other.predatorPos))
 				return false;
 			if (preyPos == null)
 			{
 				if (other.preyPos != null)
 					return false;
 			} else if (!preyPos.equals(other.preyPos))
 				return false;
 			return true;
 		}
 
 		State(Position predatorPos, Position preyPos)
 		{
 			this.predatorPos = predatorPos;
 			this.preyPos = preyPos;
 		}
 
 		private Predator getOuterType()
 		{
 			return Predator.this;
 		}
 	}
 
 	class StateAction
 	{
 		public State s;
 		public Direction a;
 
 		@Override
 		public int hashCode()
 		{
 			final int prime = 31;
 			int result = 1;
 			result = prime * result + getOuterType().hashCode();
 			result = prime * result + ((a == null) ? 0 : a.hashCode());
 			result = prime * result + ((s == null) ? 0 : s.hashCode());
 			return result;
 		}
 
 		@Override
 		public boolean equals(Object obj) 
 		{
 			if (this == obj)
 				return true;
 			if (obj == null)
 				return false;
 			if (getClass() != obj.getClass())
 				return false;
 			StateAction other = (StateAction) obj;
 			if (!getOuterType().equals(other.getOuterType()))
 				return false;
 			if (a != other.a)
 				return false;
 			if (s == null)
 			{
 				if (other.s != null)
 					return false;
 			} else if (!s.equals(other.s))
 				return false;
 			return true;
 		}
 
 		StateAction(State s, Direction a)
 		{
 			this.s = s;
 			this.a = a;
 		}
 
 		private Predator getOuterType()
 		{
 			return Predator.this;
 		}
 	}
 
 	public enum AgentType
 	{
 		PREY, PREDATOR
 	};
 
 	public enum Direction
 	{
 		UP, DOWN, LEFT, RIGHT, NONE
 	}
 
 	private HashMap<StateAction, Double> Q;
 	private State currentState;
 	private State previousState;
 	private Direction lastAction;
 	private double reward;
 	private double TAU;
 	private static final double TAU_MIN = 0.1d;
 	private static final double TAU_MAX = 1.0d;
 	private static final double TAU_STEP = 0.05d;
 	private static final double Q_DEFAULT = 0.0d;
 	private static final double LAMBDA = 0.5d;	//TODO: Should lambda change throughout the learning process?
 	private static final double GAMMA = 0.9d;
 	private static final int AUTOPILOT_DIST = 3;
 	
 
 	public Predator()
 	{
 		Q = new HashMap<StateAction, Double>();
 		currentState = null;
 		previousState = null;
 		lastAction = null;
 		reward = 0.0d;
 		TAU = TAU_MAX;
 	}
 
 	/**
 	 * This method initialize the predator by sending the initialization message
 	 * to the server.
 	 */
 	public void initialize() throws IOException
 	{
 		g_socket.send("(init predator)");
 	}
 
 	/**
 	 * This message determines a new movement command. Currently it only moves
 	 * random. This can be improved..
 	 */
 	public String determineMovementCommand()
 	{
 		Direction dir = determineMovementDirection();
 		if (dir.equals(Direction.UP))
 		{
 			return ("(move north)");
 		} else if (dir.equals(Direction.DOWN))
 		{
 			return ("(move south)");
 		} else if (dir.equals(Direction.LEFT))
 		{
 			return ("(move west)");
 		} else if (dir.equals(Direction.RIGHT))
 		{
 			return ("(move east)");
 		} else
 		{
 			return ("(move none)");
 		}
 	}
 
 	private Direction determineMovementDirection()
 	{
		if (previousState == null || Math.abs(previousState.preyPos.x) + Math.abs(previousState.preyPos.y) > AUTOPILOT_DIST)
 		{
 			return autopilotAction();
 		}
 		else
 		{
 			updateQValues();
 			lastAction = getAction();
 			reward = 0.0d;
 			return lastAction;	
 		}		
 	}
 
 	private Direction autopilotAction()
 	{
 		//select path randomly
 		if (new Random().nextInt(2) == 0)
 		{
 			if (currentState.preyPos.x > 0)
 			{
 				return Direction.RIGHT;
 			}
 			else if (currentState.preyPos.x < 0)
 			{
 				return Direction.LEFT;
 			}
 			else if (currentState.preyPos.y > 0)
 			{
 				return Direction.UP;
 			}
 			else if (currentState.preyPos.y < 0)
 			{
 				return Direction.DOWN;
 			}
 		}
 		else
 		{
 			if (currentState.preyPos.y > 0)
 			{
 				return Direction.UP;
 			}
 			else if (currentState.preyPos.y < 0)
 			{
 				return Direction.DOWN;
 			}
 			else if (currentState.preyPos.x > 0)
 			{
 				return Direction.RIGHT;
 			}
 			else if (currentState.preyPos.x < 0)
 			{
 				return Direction.LEFT;
 			}
 		}
 		
 		return Direction.NONE;
 	}
 
 	private void updateQValues()
 	{
 		if (previousState != null)
 		{
 			double V = getV(currentState);
 			StateAction previousStateAction = new StateAction(previousState, lastAction);
 			double oldQval = getQ(previousStateAction);
 			double newQval = (1.0d - LAMBDA) * oldQval + LAMBDA * (reward + GAMMA * V);
 			Q.put(previousStateAction, newQval);
 			if (Math.abs(oldQval - newQval) > 0.01d)
 			{
 				System.out.println("\nQ-value changed from " + oldQval + " to " + newQval + ".");	
 			}			
 		}
 	}
 
 	private double getV(State state)
 	{
 		double v = Double.NEGATIVE_INFINITY;
 		for (Direction action : Direction.values())
 		{
 			StateAction sa = new StateAction(state, action);
 			double qVal = getQ(sa);
 			if (qVal > v)
 			{
 				v = qVal;
 			}
 		}
 		return v;
 	}
 
 	private Direction getAction()
 	{
 		double[] probabilities = new double[Direction.values().length];
 		int i = 0;
 		for (Direction a : Direction.values())
 		{
 			if (i > 0)
 			{
 				probabilities[i] = getProb(a) + probabilities[i - 1];
 			} else
 			{
 				probabilities[i] = getProb(a);
 			}
 
 			i++;
 		}
 		Random die = new Random();
 		double result = die.nextDouble();
 		i = 0;
 		for (Direction a : Direction.values())
 		{
 			if (result < probabilities[i] || i >= (Direction.values().length)-1)
 			{
 				return a;
 			}
 			i++;
 		}
 		throw new RuntimeException("Could not choose an action");
 	}
 
 	private double getProb(Direction a)
 	{
 		StateAction sa = new StateAction(currentState, a);
 		double numerator = Math.exp(getQ(sa) / TAU);
 		double denominator = 0;
 		for (Direction aPrime : Direction.values())
 		{
 			StateAction sa_prime = new StateAction(currentState, aPrime);
 			denominator += Math.exp(getQ(sa_prime) / TAU);
 		}
 		double prob = numerator / denominator;
 		if (prob >= 0.21d || prob <= 0.19d)
 		{
 			System.out.println("p(" + a + ") = " + prob);	
 		}		
 		return prob;
 	}
 
 	private double getQ(StateAction sa)
 	{
 		Double qVal = Q.get(sa);
 		
 		if (qVal == null)
 		{
 			fixTau(true);
 			return Q_DEFAULT;
 		}
 		else
 		{
 			fixTau(false);
 			return qVal;
 		}
 	}
 
 	private void fixTau(boolean moreExploration)
 	{
 			//TODO: Is this a good way to decide the value of tau?
 		
 			// higher TAU values make the probabilities for the action to be close to each other
 			if (moreExploration)
 			{
 				TAU += TAU_STEP;
 				if (TAU > TAU_MAX)
 				{
 					TAU = TAU_MAX;
 				}
 			}
 			// smaller TAU values give a boost to the probabilities of the most valuable actions
 			else
 			{
 				TAU -= TAU_STEP;
 				if (TAU < TAU_MIN)
 				{
 					TAU = TAU_MIN;
 				}
 			}
 					
 	}
 
 	/**
 	 * This method processes the visual information. It receives a message
 	 * containing the information of the preys and the predators that are
 	 * currently in the visible range of the predator.
 	 */
 	public void processVisualInformation(String strMessage)
 	{
 		Position predatorPos = null;
 		Position preyPos = null;
 		int i = 0, x = 0, y = 0;
 		String strName = "";
 		StringTokenizer tok = new StringTokenizer(strMessage.substring(5),
 				") (");
 
 		while (tok.hasMoreTokens())
 		{
 			if (i == 0)
 				strName = tok.nextToken(); // 1st = name
 			if (i == 1)
 				x = Integer.parseInt(tok.nextToken()); // 2nd = x coord
 			if (i == 2)
 				y = Integer.parseInt(tok.nextToken()); // 3rd = y coord
 			if (i == 2)
 			{
 				if (strName.equals("prey"))
 				{
 					preyPos = new Position(x,y);
 				} else if (strName.equals("predator"))
 				{
 					predatorPos = new Position(x,y);
 				}
 				//System.out.println(strName + " seen at (" + x + ", " + y + ")");
 			}
 			i = (i + 1) % 3;
 		}
 		previousState = currentState;
 		if (predatorPos == null || preyPos == null)
 		{
 			throw new RuntimeException("Could not get complete information for current state.");
 		}
 		currentState = new State(predatorPos, preyPos);
 	}
 
 	/**
 	 * This method is called after a communication message has arrived from
 	 * another predator.
 	 */
 	public void processCommunicationInformation(String strMessage)
 	{
 		// TODO: exercise 3 to improve capture times
 	}
 
 	/**
 	 * This method is called and can be used to send a message to all other
 	 * predators. Note that this only will have effect when communication is
 	 * turned on in the server.
 	 */
 	public String determineCommunicationCommand()
 	{
 		// TODO: exercise 3 to improve capture times
 		return ""; 
 	}
 
 	/**
 	 * This method is called when an episode is ended and can be used to reset
 	 * some variables.
 	 */
 	public void episodeEnded()
 	{
 		// this method is called when an episode has ended and can be used to
 		// reinitialize some variables
 		System.out.println("TAU = " + TAU);
 		System.out.println("EPISODE ENDED\n");
 		reward = 1.0d;
 	}
 
 	/**
 	 * This method is called when this predator is involved in a collision.
 	 */
 	public void collisionOccured()
 	{
 		// this method is called when a collision occured and can be used to
 		// reinitialize some variables
 		//System.out.println("COLLISION OCCURED\n");
 	}
 
 	/**
 	 * This method is called when this predator is involved in a penalization.
 	 */
 	public void penalizeOccured()
 	{
 		// this method is called when a predator is penalized and can be used to
 		// reinitialize some variables
 		//System.out.println("PENALIZED\n");
 	}
 
 	/**
 	 * This method is called when this predator is involved in a capture of a
 	 * prey.
 	 */
 	public void preyCaught()
 	{
 		//System.out.println("PREY CAUGHT\n");
 	}
 
 	public static void main(String[] args)
 	{
 		Predator predator = new Predator();
 		if (args.length == 2)
 			predator.connect(args[0], Integer.parseInt(args[1]));
 		else
 			predator.connect();
 	}
 }
