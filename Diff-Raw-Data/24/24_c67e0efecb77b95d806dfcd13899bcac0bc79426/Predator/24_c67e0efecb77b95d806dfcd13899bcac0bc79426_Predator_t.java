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
 		public Position predator1Pos;
 		public Position predator2Pos;
 		public Position preyPos;		
 
 		State(Position predator1Pos, Position predator2Pos, Position preyPos)
 		{
 			this.predator1Pos = predator1Pos;
 			this.predator2Pos = predator2Pos;
 			this.preyPos = preyPos;
 		}
 
 		@Override
 		public int hashCode()
 		{
 			final int prime = 31;
 			int result = 1;
 			result = prime * result + getOuterType().hashCode();
 			result = prime * result
 					+ ((predator1Pos == null) ? 0 : predator1Pos.hashCode());
 			result = prime * result
 					+ ((predator2Pos == null) ? 0 : predator2Pos.hashCode());
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
 			if (predator1Pos == null)
 			{
 				if (other.predator1Pos != null)
 					return false;
 			} else if (!predator1Pos.equals(other.predator1Pos))
 				return false;
 			if (predator2Pos == null)
 			{
 				if (other.predator2Pos != null)
 					return false;
 			} else if (!predator2Pos.equals(other.predator2Pos))
 				return false;
 			if (preyPos == null)
 			{
 				if (other.preyPos != null)
 					return false;
 			} else if (!preyPos.equals(other.preyPos))
 				return false;
 			return true;
 		}
 
 		private Predator getOuterType()
 		{
 			return Predator.this;
 		}
 	}
 	
 	class JointAction
 	{
 		public Direction d1;
 		public Direction d2;
 		
 		JointAction (Direction d1, Direction d2)
 		{
 			this.d1 = d1;
 			this.d2 = d2;
 		}
 
 		@Override
 		public int hashCode()
 		{
 			final int prime = 31;
 			int result = 1;
 			result = prime * result + getOuterType().hashCode();
 			result = prime * result + ((d1 == null) ? 0 : d1.hashCode());
 			result = prime * result + ((d2 == null) ? 0 : d2.hashCode());
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
 			JointAction other = (JointAction) obj;
 			if (!getOuterType().equals(other.getOuterType()))
 				return false;
 			if (d1 != other.d1)
 				return false;
 			if (d2 != other.d2)
 				return false;
 			return true;
 		}
 
 		private Predator getOuterType()
 		{
 			return Predator.this;
 		}
 	}
 
 	class StateAction
 	{
 		public State s;
 		public JointAction a;
 
 
 		StateAction(State s, JointAction a)
 		{
 			this.s = s;
 			this.a = a;
 		}
 
 
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
 			if (a == null)
 			{
 				if (other.a != null)
 					return false;
 			} else if (!a.equals(other.a))
 				return false;
 			if (s == null)
 			{
 				if (other.s != null)
 					return false;
 			} else if (!s.equals(other.s))
 				return false;
 			return true;
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
 	private JointAction lastAction;
 	private double reward;
 	private double TAU;
 	private static final double TAU_MIN = 0.1d;
 	private static final double TAU_MAX = 1.0d;
 	private static final double TAU_STEP = 0.001d;
 	private static final double Q_DEFAULT = 0.0d;
 	private static final double LAMBDA = 0.5d;	//TODO: Should lambda change throughout the learning process?
 	private static final double GAMMA = 0.9d;
 	private static final int AUTOPILOT_DIST = 3;
 	private boolean rolesCast;
 	private boolean amFirst;
 	private Random die;
 	
 
 	public Predator()
 	{
 		Q = new HashMap<StateAction, Double>();
 		currentState = null;
 		previousState = null;
 		lastAction = null;
 		reward = 0.0d;
 		TAU = TAU_MAX;
 		rolesCast = false; 
 		die = new Random(57);	//Grothendieck prime :D
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
				
		if (previousState == null || getDistance(previousState.predator1Pos, previousState.preyPos) > AUTOPILOT_DIST || getDistance(previousState.predator2Pos, previousState.preyPos) > AUTOPILOT_DIST)
 		{
 			return autopilotAction();
 		}
 		else
 		{
 			updateQValues();
 			lastAction = getAction();
			int dist1 = getDistance(currentState.predator1Pos, currentState.preyPos);
			int dist2 = getDistance(currentState.predator2Pos, currentState.preyPos);
			reward = 0.5d / (dist1 + dist2);	
			
			//System.out.println("1:" + lastAction.d1 + "\t2:" + lastAction.d2);
 			if (amFirst)
 			{
 				return lastAction.d1;	
 			}
 			else
 			{
 				return lastAction.d2;
 			}
 				
 		}		
 	}
 
 	private Direction autopilotAction()
 	{
		if (Math.abs(currentState.preyPos.x) + Math.abs(currentState.preyPos.y) == 1)
		{
			return Direction.NONE;
		}
		
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
 		for (Direction action1 : Direction.values())
 		{
 			for (Direction action2 : Direction.values())
 			{
 				StateAction sa = new StateAction(state, new JointAction(action1, action2));
 				double qVal = getQ(sa);
 				if (qVal > v)
 				{
 					v = qVal;
 				}
 			}
 		}
 		return v;
 	}
 
 	private JointAction getAction()
 	{
 		double[] probabilities = new double[(int) Math.pow(Direction.values().length,2)];
 		int i = 0;
 		for (Direction a1 : Direction.values())
 		{
 			for (Direction a2 : Direction.values())
 			{ 
 				JointAction a = new JointAction(a1, a2);
 				if (i > 0)
 				{
 					probabilities[i] = getProb(a) + probabilities[i - 1];
 				} else
 				{
 					probabilities[i] = getProb(a);
 				}
 	
 				i++;
 			}
 		}
 		
 		double result = die.nextDouble();
 		i = 0;
 		for (Direction a1 : Direction.values())
 		{
 			for (Direction a2 : Direction.values())
 			{
 				JointAction a = new JointAction(a1, a2);
 				if (result < probabilities[i] || i >= (Direction.values().length)-1)
 				{
 					return a;
 				}
 				i++;
 			}
 		}
 		throw new RuntimeException("Could not choose an action");
 	}
 
 	private double getProb(JointAction a)
 	{
 		StateAction sa = new StateAction(currentState, a);
 		double numerator = Math.exp(getQ(sa) / TAU);
 		double denominator = 0;
 		for (Direction aPrime1 : Direction.values())
 		{
 			for (Direction aPrime2 : Direction.values())
 			{
 				StateAction sa_prime = new StateAction(currentState, new JointAction(aPrime1, aPrime2));
 				denominator += Math.exp(getQ(sa_prime) / TAU);
 			}
 		}
 		double prob = numerator / denominator;
		if (prob >= 0.05d || prob <= 0.03d)
 		{
 			System.out.println("p(" + a.d1 + "," + a.d2 + ") = " + prob);	
 		}		
 		return prob;
 	}
 
 	private double getQ(StateAction sa)
 	{
 		Double qVal = Q.get(sa);
 		
 		if (qVal == null)
 		{
 			return Q_DEFAULT;
 		}
 		else
 		{
 			return qVal;
 		}
 	}
 
 	private void fixTau(boolean moreExploration)
 	{
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
 		
 		Position mePos = new Position(0,0);
 		
 		if (!rolesCast)
 		{
 			if (breakTie(mePos, predatorPos, preyPos))
 			{
 				amFirst = true;	
 			}
 			else
 			{
 				amFirst = false;
 			}
 			rolesCast = true;			
 		}
 		
 		if (amFirst)
 		{
 			currentState = new State(mePos, predatorPos, preyPos);
 		}
 		else
 		{
 			currentState = new State(predatorPos, mePos, preyPos);
 		}	
 		
 	}
 
 	/**
 	 * This method is called after a communication message has arrived from
 	 * another predator.
 	 */
 	public void processCommunicationInformation(String strMessage)
 	{
 		// to be used in exercise 3 to improve capture times
 		// (but there is no such thing as exercise 3!)
 	}
 
 	/**
 	 * This method is called and can be used to send a message to all other
 	 * predators. Note that this only will have effect when communication is
 	 * turned on in the server.
 	 */
 	public String determineCommunicationCommand()
 	{
 		// to be used in exercise 3 to improve capture times
 		// (but there is no such thing as exercise 3!)
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
 		fixTau(false);
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
 		reward = -0.1d;
 	}
 
 	/**
 	 * This method is called when this predator is involved in a penalization.
 	 */
 	public void penalizeOccured()
 	{
 		// this method is called when a predator is penalized and can be used to
 		// reinitialize some variables
 		//System.out.println("PENALIZED\n");
 		reward = -0.1d;
 	}
 
 	/**
 	 * This method is called when this predator is involved in a capture of a
 	 * prey.
 	 */
 	public void preyCaught()
 	{
 		//System.out.println("PREY CAUGHT\n");
 	}
 	
 	//auxialiary methods:
 	
 	private int getDistance(Position pos1, Position pos2)
 	{
 		
 		int xDist = pos1.x - pos2.x;
 		if (xDist > 7)
 		{
 			xDist -= 15;
 		}
 		else if (xDist < -7)
 		{
 			xDist += 15;
 		}
 		
 		int yDist = pos1.y - pos2.y;
 		if (yDist > 7)
 		{
 			yDist -= 15;
 		}
 		else if (yDist < -7)
 		{
 			yDist += 15;
 		}
 		
 		return Math.abs(xDist) + Math.abs(yDist);
 	}
 	
 	private boolean breakTie(Position pos1, Position pos2, Position ref)
 	{
 		if (getDistance(pos1, ref) < getDistance(pos2,ref))
 		{
 			return true;
 		}
 		else if (getDistance(pos1, ref) > getDistance(pos2,ref))
 		{
 			return false;
 		}
 		
 		int xDist1 = pos1.x - ref.x;
 	
 		if (xDist1 > 7)
 		{
 			xDist1 -= 15;
 		}
 		else if (xDist1 < -7)
 		{
 			xDist1 += 15;
 		}		
 		int yDist1 = pos1.y - ref.y;
 		if (yDist1 > 7)
 		{
 			yDist1 -= 15;
 		}
 		else if (yDist1 < -7)
 		{
 			yDist1 += 15;
 		}
 		
 		int xDist2 = pos2.x - ref.x;
 		if (xDist2 > 7)
 		{
 			xDist2 -= 15;
 		}
 		else if (xDist2 < -7)
 		{
 			xDist2 += 15;
 		}		
 		int yDist2 = pos2.y - ref.y;
 		if (yDist2 > 7)
 		{
 			yDist2 -= 15;
 		}
 		else if (yDist2 < -7)
 		{
 			yDist2 += 15;
 		}
 		
 		if (xDist1 > xDist2)
 		{
 			return true;
 		}
 		else if (xDist1 == xDist2)
 		{
 			if (yDist1 > yDist2)
 			{
 				return true;
 			}
 		}
 		
 		return false;
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
