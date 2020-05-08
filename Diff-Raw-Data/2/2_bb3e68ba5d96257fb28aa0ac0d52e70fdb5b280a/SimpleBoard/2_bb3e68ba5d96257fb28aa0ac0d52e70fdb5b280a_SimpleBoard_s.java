 package sequentialgame.grid;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import props.DiscreteDistribution;
 import props.Joint;
 import utils.FileUtils;
 import utils.StringUtils;
 
 /**
  * An implementation of a simple rectangular board.
  * Valid actions are (Up, Down, Left, Right, Stick).
  * Players can have multiple goals that all give the 
  * same reward. One-way semi-walls can also exist, 
  * which define the probability that a player in some
  * position succeeds in moving in its intended direction
  * (different probabilities can exist for each position
  * and movement direction). Failing to pass through a
  * semi-wall results in sticking in the same position.
  * 
  * @author sodomka
  *
  */
 public class SimpleBoard implements Board {
 
 	/**
 	 * The board dimensions.
 	 */
 	private int numXLocations = 0;
 	private int numYLocations = 0;
 	
 	private Joint<Position> initialPositions;
 
 	/**
 	 * Arrays specifying the probability of successfully moving
 	 * in the specified direction, assuming the final location
 	 * is an occupiable square. This allows for walls, semiwalls,
 	 * one-directional walls, and one-directional semiwalls.
 	 * By default, there are no walls.
 	 */
 	private double[][] upMovementSuccessProbability;
 	private double[][] downMovementSuccessProbability;
 	private double[][] leftMovementSuccessProbability;
 	private double[][] rightMovementSuccessProbability;
 
 	/**
 	 * Array specifying whether each position on the grid
 	 * is allowed to be occupied.
 	 */
 	private boolean[][] isOccupiablePosition;
 	private List<Position> occupiablePositions;
 
 	/**
 	 * A list of actions that have any meaning on this board.
 	 */
 	private List<GridAction> allowableActions;
 
 	/**
 	 * For each player, a list of positions for which
 	 * a goal is located and the reward associated with that goal.
 	 */
 	private Joint<Map<Position, Double>> goalPositionsAndRewardsPerPlayer;
 	
 	/**
 	 * The immediate reward a player receives for taking a non-stick action.
 	 * This is typically negative.
 	 */
 	private double stepReward = -1;
 
 	public SimpleBoard(String filepath) {
 		
 		final Character notOccupiableChar = 'X';
 		final Character occupiableChar = '.';
 		final String boardEndString = "END";
 		final String referenceIdSeparator = ":";
 		final String keyValueSeparator = "=";
 		final String pairSeparator = ";";
 		final String goalPrefix = "goal";
 		final String startPrefix = "start";
 		final String wallPrefix = "wall";
 		final String goalStartWallSeparator = "_";
 
 		List<String> lines = FileUtils.readLines(filepath);
 		int numLines = lines.size();
 		System.out.println("lines: " + lines);
 		
 		// Get board dimensions
 		this.numXLocations = lines.get(0).length();
 		this.numYLocations = 0;
 		for (String line : lines) {
 			if (line.equalsIgnoreCase(boardEndString)) {
 				break;
 			}
 			this.numYLocations++;
 		}
 		
 		// Set defaults
 		this.isOccupiablePosition = new boolean[numXLocations][numYLocations];
 		this.upMovementSuccessProbability = new double[numXLocations][numYLocations];
 		this.downMovementSuccessProbability = new double[numXLocations][numYLocations];
 		this.leftMovementSuccessProbability = new double[numXLocations][numYLocations];
 		this.rightMovementSuccessProbability = new double[numXLocations][numYLocations];
 		for (int x=0; x<numXLocations; x++) {
 			for (int y=0; y<numYLocations; y++) {
 				isOccupiablePosition[x][y] = true;
 				upMovementSuccessProbability[x][y] = 1;
 				downMovementSuccessProbability[x][y] = 1;
 				leftMovementSuccessProbability[x][y] = 1;
 				rightMovementSuccessProbability[x][y] = 1;
 			}
 		}
 		
 		// Read board. Update information about occupiable states,
 		// and keep track of references (about goal locations, etc.) to handle later.
 		HashMap<Character, Position> referencePositions = new HashMap<Character, Position>();
 		for (int lineIdx=0; lineIdx<numYLocations; lineIdx++) {
 			for (int charIdx=0; charIdx<numXLocations; charIdx++) {
 				// Position (x=0, y=0) is the lower-left corner.
 				int x = charIdx;
 				int y = numYLocations - lineIdx - 1;
 				char character = lines.get(lineIdx).charAt(charIdx);
 				if (character == notOccupiableChar) {
 					isOccupiablePosition[x][y] = false;
 				} else if (character != occupiableChar) {
 					// Character is neither the occupiable nor the unoccupiable symbol;
 					// it must therefore be a reference.
 					referencePositions.put(character, new Position(x, y));
 				}
 			}
 		}
 		
 		// Board has been read. Get step reward.
 		int costLineIdx = numYLocations+1; // skip the END line.
 		String costString = lines.get(costLineIdx);
 		stepReward = Double.parseDouble(costString.split(referenceIdSeparator)[1]);
 		
 		// Get information from additional references
 		Map<Integer, Map<Position, Double>> positionRewardsForPlayer = new HashMap<Integer, Map<Position, Double>>(); // Map<PlayerIdx>, Map<Position, Reward>
 		Map<Integer, Position> startingPositionForPlayer = new HashMap<Integer, Position>();
 		int referenceStartingIdx = costLineIdx+1;
 		for (int referenceIdx=referenceStartingIdx; referenceIdx<numLines; referenceIdx++) {
 			String line = lines.get(referenceIdx);
 			Character referenceId = line.split(referenceIdSeparator)[0].charAt(0);
 			Position referencePosition = referencePositions.get(referenceId);
 			String referenceInfo = line.split(referenceIdSeparator)[1];
 			// Iterate through reference information (key-value pairs) for this reference id
 			Map<String, String> keyValuePairs = StringUtils.ParseKeyValuePairs(referenceInfo, keyValueSeparator, pairSeparator);
 			System.out.println("referenceId=" + referenceId + ", keyValuePairs=" + keyValuePairs);
 			for (String key : keyValuePairs.keySet()) {
 				String value = keyValuePairs.get(key);
 				if (key.startsWith(goalPrefix)) {
 					Integer playerIdx = new Integer(key.split(goalStartWallSeparator)[1]);
 					Double goalValue = new Double(value);
 					if (!positionRewardsForPlayer.containsKey(playerIdx)) {
 						positionRewardsForPlayer.put(playerIdx, new HashMap<Position, Double>());
 					}
 					Map<Position,Double> positionRewards = positionRewardsForPlayer.get(playerIdx);
 					positionRewards.put(referencePosition, goalValue);
 				} else if (key.startsWith(startPrefix)) {
 					Integer playerIdx = new Integer(key.split(goalStartWallSeparator)[1]);
 					startingPositionForPlayer.put(playerIdx, referencePosition);
 				} else if (key.startsWith(wallPrefix)) {
 					String wallDirection = key.split(goalStartWallSeparator)[1];
 					Double wallSuccessProb = new Double(value);
 					if (wallDirection.equalsIgnoreCase("up")) {
 						upMovementSuccessProbability[referencePosition.getX()][referencePosition.getY()] = wallSuccessProb;
 					} else if (wallDirection.equalsIgnoreCase("down")) {
 						downMovementSuccessProbability[referencePosition.getX()][referencePosition.getY()] = wallSuccessProb;
 					} else if (wallDirection.equalsIgnoreCase("left")) {
 						leftMovementSuccessProbability[referencePosition.getX()][referencePosition.getY()] = wallSuccessProb;
 					}else if (wallDirection.equalsIgnoreCase("right")) {
 						rightMovementSuccessProbability[referencePosition.getX()][referencePosition.getY()] = wallSuccessProb;
 					} else {
 						System.err.println("Error reading wall direction.");						
 					}
 				} else {
 					System.err.println("Error reading character.");
 				}
 			}
 		}
 		
 		// Get the maximum player index. 
 		// It will be assumed that all player indices between 0 and the max have been specified.
 		Integer maxPlayerIdx = 0;
 		for (Integer playerIdx : startingPositionForPlayer.keySet()) {
 			if (playerIdx > maxPlayerIdx) {
 				maxPlayerIdx = playerIdx;
 			}
 		}
 		
 		System.out.println("initialPosForPla=" + startingPositionForPlayer);
 		// Put information about position rewards and starting positions into the relevant data structures.
 		goalPositionsAndRewardsPerPlayer = new Joint<Map<Position, Double>>();
 		initialPositions = new Joint<Position>();
 		for (int playerIdx=0; playerIdx<=maxPlayerIdx; playerIdx++) {
 			goalPositionsAndRewardsPerPlayer.add(positionRewardsForPlayer.get(playerIdx));
 			initialPositions.add(startingPositionForPlayer.get(playerIdx));
 		}
 		
 		this.occupiablePositions = this.computeOccupiablePositions();
 		this.allowableActions = this.computeAllowableActions();
 		
 		System.out.println("goalPositions: " + goalPositionsAndRewardsPerPlayer);
 		System.out.println("initialPositions: " + initialPositions);
 		System.out.println("isOccupiablePosition: " + Arrays.deepToString(isOccupiablePosition));
 		System.out.println("upMovementSuccessProbability" + Arrays.deepToString(upMovementSuccessProbability));
 		System.out.println("downMovementSuccessProbability" + Arrays.deepToString(downMovementSuccessProbability));
 		System.out.println("leftMovementSuccessProbability" + Arrays.deepToString(leftMovementSuccessProbability));
 		System.out.println("rightMovementSuccessProbability" + Arrays.deepToString(rightMovementSuccessProbability));
 		System.out.println("is 0,0 occupiable: " + isOccupiablePosition[0][0]);
 	}
 	
 	
 	
 	public static void main(String[] args) {
 		//String filename = "/Users/sodomka/Dropbox/myDocs/repositories/github/GridGames/input/game1.txt";
 		String filename = "./input/game2.txt";
 		SimpleBoard board = new SimpleBoard(filename);
 	}
 
 	public SimpleBoard(int numXLocations, int numYLocations) {
 		this.numXLocations = numXLocations;
 		this.numYLocations = numYLocations;
 		isOccupiablePosition = new boolean[numXLocations][numYLocations];
 		upMovementSuccessProbability = new double[numXLocations][numYLocations];
 		downMovementSuccessProbability = new double[numXLocations][numYLocations];
 		leftMovementSuccessProbability = new double[numXLocations][numYLocations];
 		rightMovementSuccessProbability = new double[numXLocations][numYLocations];
 		for (int x=0; x<numXLocations; x++) {
 			for (int y=0; y<numYLocations; y++) {
 				isOccupiablePosition[x][y] = true;
 				upMovementSuccessProbability[x][y] = 1;
 				downMovementSuccessProbability[x][y] = 1;
 				leftMovementSuccessProbability[x][y] = 1;
 				rightMovementSuccessProbability[x][y] = 1;
 			}
 		}
 		occupiablePositions = computeOccupiablePositions();
 		allowableActions = computeAllowableActions();
 		goalPositionsAndRewardsPerPlayer = new Joint<Map<Position, Double>>();
 
 		//TODO: Make this more general!!!
 		// By default, create a single goal position in (0,0) and none anywhere else.
 		Map<Position, Double> p1PositionRewards = new HashMap<Position, Double>();
 		Map<Position, Double> p2PositionRewards = new HashMap<Position, Double>();
 		p1PositionRewards.put(new Position(1,1), 100.0);
 		p2PositionRewards.put(new Position(0,1), 100.0);
 		goalPositionsAndRewardsPerPlayer.add(p1PositionRewards);
 		goalPositionsAndRewardsPerPlayer.add(p2PositionRewards);
 		
 		initialPositions = new Joint<Position>();
 		initialPositions.add(new Position(0,0));
 		initialPositions.add(new Position(1,0));
 	}
 
 
 	private List<Position> computeOccupiablePositions() {
 		List<Position> occupiablePositions = new ArrayList<Position>();
 		for (int x=0; x<numXLocations; x++) {
 			for (int y=0; y<numYLocations; y++) {
 				if (isOccupiablePosition[x][y]) {
 					occupiablePositions.add(new Position(x,y));
 				}
 			}
 		}
 		return occupiablePositions;
 	}
 
 	@Override
 	public List<Position> getOccupiablePositions() {
 		return occupiablePositions;
 	}
 
 
 	private List<GridAction> computeAllowableActions() {
 		List<GridAction> allowableActions = new ArrayList<GridAction>();
 		allowableActions.add(new GridAction("up"));
 		allowableActions.add(new GridAction("down"));
 		allowableActions.add(new GridAction("left"));
 		allowableActions.add(new GridAction("right"));
 		allowableActions.add(new GridAction("stick"));
 		return allowableActions;
 	}
 
 	@Override
 	public List<GridAction> getAllowableActions() {
 		return allowableActions;
 	}
 
 
 
 	@Override
 	public double getActionReward(GridAction playerAction) {
 		// Reward is 0 unless the player's action was to take a step.
 		// (regardless of whether that step was successful).
 		double actionReward = 0;
 		if (isUpAction(playerAction) ||
 				isDownAction(playerAction) ||
 				isLeftAction(playerAction) ||
 				isRightAction(playerAction)) {
 			actionReward = stepReward;
 		}
 		return actionReward;
 	}
 
 
 	@Override
 	public double getGoalReward(Position playerPosition, Integer playerIdx) {
 		if(goalPositionsAndRewardsPerPlayer.getForPlayer(playerIdx).containsKey(playerPosition)) {
 			return goalPositionsAndRewardsPerPlayer.getForPlayer(playerIdx).get(playerPosition);
 		}
 		return 0;
 	}
 
 
 
 	public DiscreteDistribution<Position> getNextPositionDistribution(
 			Position currentPlayerPosition,
 			GridAction playerAction) {
 
 		// Get next player position, assuming movement is successful.
 		Position nextPlayerPositionGivenActionSuccess = getNextPlayerPositionGivenActionSuccess(currentPlayerPosition, playerAction);
 		Position nextPlayerPositionGivenActionFail = currentPlayerPosition;
 
 		double probabilityOfActionSuccess = getProbabilityOfActionSuccess(currentPlayerPosition, playerAction);
 
 		DiscreteDistribution<Position> nextPositionDistribution = new DiscreteDistribution<Position>();
 		nextPositionDistribution.add(nextPlayerPositionGivenActionSuccess, probabilityOfActionSuccess);
 
 		// Add a failure position if movement failure is possible.
 		if (probabilityOfActionSuccess<1) {
 			nextPositionDistribution.add(nextPlayerPositionGivenActionFail, 1-probabilityOfActionSuccess);
 		}
 		return nextPositionDistribution;
 	}
 
 
 	private double getProbabilityOfActionSuccess(
 			Position currentPlayerPosition, GridAction playerAction) {
 		int x = currentPlayerPosition.getX();
 		int y = currentPlayerPosition.getY();
 		if (isUpAction(playerAction)) {
 			return upMovementSuccessProbability[x][y];
 		} else if (isDownAction(playerAction)) {
 			return downMovementSuccessProbability[x][y];
 		} else if (isLeftAction(playerAction)) {
 			return leftMovementSuccessProbability[x][y];
 		} else if (isRightAction(playerAction)) {
 			return rightMovementSuccessProbability[x][y];
 		} else if (isStickAction(playerAction)) {
 			return 1;
 		} else {
 			System.err.println("Illegal action: " + playerAction);
 			return 0;
 		}
 	}
 
 	private Position getNextPlayerPositionGivenActionSuccess(
 			Position currentPlayerPosition, GridAction playerAction) {
 		int x = currentPlayerPosition.getX();
 		int y = currentPlayerPosition.getY();
 		int nextX;
 		int nextY;
 		if (isUpAction(playerAction)) {
 			nextX = x;
 			nextY = y+1;
 		} else if (isDownAction(playerAction)) {
 			nextX = x;
 			nextY = y-1;
 		} else if (isLeftAction(playerAction)) {
 			nextX = x-1;
 			nextY = y;
 		} else if (isRightAction(playerAction)) {
 			nextX = x+1;
 			nextY = y;
 		} else if (isStickAction(playerAction)) {
 			nextX = x;
 			nextY = y;
 		} else {
 			System.err.println("Illegal action: " + playerAction);
 			nextX = x;
 			nextY = y;
 		}
 		nextX = Math.max(0, nextX);
 		nextX = Math.min(numXLocations-1, nextX);
 		nextY = Math.max(0, nextY);
		nextY = Math.min(numXLocations-1, nextY);
 
 		// Any actions towards non-occupiable positions result 
 		// in no movement.
 		if (!isOccupiablePosition[nextX][nextY]) {
 			nextX = x;
 			nextY = y;
 		}
 		return getPosition(nextX, nextY);
 	}
 
 
 	private Position getPosition(int x, int y) {
 		// TODO: Could keep a list of positions to ensure
 		// duplicate positions aren't stored in memory.
 		return new Position(x, y);
 	}
 
 	private boolean isUpAction(GridAction playerAction) {
 		return playerAction.getName().equalsIgnoreCase("up");
 	}
 
 	private boolean isDownAction(GridAction playerAction) {
 		return playerAction.getName().equalsIgnoreCase("down");		
 	}
 
 	private boolean isLeftAction(GridAction playerAction) {
 		return playerAction.getName().equalsIgnoreCase("left");		
 	}
 
 	private boolean isRightAction(GridAction playerAction) {
 		return playerAction.getName().equalsIgnoreCase("right");		
 	}
 
 	private boolean isStickAction(GridAction playerAction) {
 		return playerAction.getName().equalsIgnoreCase("stick");		
 	}
 
 
 	@Override
 	public boolean hasGoalForPlayer(Position playerPosition, Integer playerIdx) {
 		return (goalPositionsAndRewardsPerPlayer.get(playerIdx).containsKey(playerPosition));
 	}
 
 
 	@Override
 	public Joint<Position> getInitialPositions() {
 		return initialPositions;
 	}
 
 
 
 
 }
