 import jason.asSyntax.Literal;
 import jason.asSyntax.NumberTerm;
 import jason.asSyntax.Structure;
 import jason.environment.Environment;
 import jason.environment.grid.GridWorldModel;
 import jason.environment.grid.GridWorldView;
 import jason.environment.grid.Location;
 import jason.mas2j.AgentParameters;
 import jason.mas2j.MAS2JProject;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.io.FileInputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 import java.util.logging.Logger;
 
 public class SWATEnv extends Environment {
 
 	public static final int GRID_SIZE = 16;
 
 	public static final int BLUE_FLAG = 16;
 	public static final int RED_FLAG = 32;
 	public static final int WALL = 64;
 	public static final int PIT = 128;
 
 	public static final int BLUE_TEAM = 1;
 	public static final int RED_TEAM = 2;
 
 	static Logger logger = Logger.getLogger(SWATEnv.class.getName());
 
 	public static int NUMBER_OF_AGENTS = 0;
 
 	private SWATModel model;
 	private SWATView view;
 	private List<Agent> agents;
 
 	@Override
 	public void init(String[] args) {
 
 		try {
 			jason.mas2j.parser.mas2j parser = new jason.mas2j.parser.mas2j(
 					new FileInputStream(args[0]));
 			MAS2JProject project = parser.mas();
 			this.agents = new ArrayList<Agent>();
 
 			for (AgentParameters agentClass : project.getAgents()) {
 				String agentName = agentClass.name;
 				for (int agentNumber = 0; agentNumber < agentClass.qty; agentNumber++) {
 					this.agents.add(new Agent(agentName, agentNumber));
 					++NUMBER_OF_AGENTS;
 				}
 
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		model = new SWATModel(NUMBER_OF_AGENTS);
 		view = new SWATView(model);
 		model.setView(view);
 	}
 
 	@Override
 	public boolean executeAction(String name, Structure action) {
 		logger.info(name + " doing: " + action);
 		String action_name = action.getFunctor();
 
 		Agent agent = agentByName(name);
 		int agentNumber = agent.number();
 
 		try {
 			if (action_name.equals("move")) {
 				int x = (int) ((NumberTerm) action.getTerm(0)).solve();
 				int y = (int) ((NumberTerm) action.getTerm(1)).solve();
 				model.move(agentNumber, x, y);
 			} else if (action_name.equals("move_towards")) {
 				int x = (int) ((NumberTerm) action.getTerm(0)).solve();
 				int y = (int) ((NumberTerm) action.getTerm(1)).solve();
 				model.moveTowardsLocation(agentNumber, x, y);
 			} else if (action_name.equals("win")) {
 				String team;
 				if (agents.get(agentNumber).team() == RED_TEAM)
 					team = "Red team";
 				else
 					team = "Blue team";
 				System.out.println(team + " won!");
 				stop();
 			} else if (action_name.equals("grab_flag")) {
 				String s = action.getTerm(0).toString();
 				grab_flag(agentNumber, s);
 			} else if (action_name.equals("fire_the_mighty_weapon")) {
 				fire(agent);
 			} else if (action_name.equals("drop")) {
 				System.out.println("drop");
 			} else if (action_name.equals("error")) {
 				System.out.println("error");
 			} else {
 				return false;
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		view.repaint();
 		updatePercepts(agentNumber);
 
 		try {
 			Thread.sleep(250);
 		} catch (Exception e) {
 			// pass
 		}
 
 		return true;
 	}
 
 	private synchronized void fire(Agent agent) {
 		Agent deadManWalking = model.getAgentsInSight(agent);
 		if (deadManWalking == null)
 			return;
 
 		model.respawn(agent);
 
 	}
 
 	private synchronized void grab_flag(int agentNumber, String flagsTeam) {
 		Agent carrier = agents.get(agentNumber);
 
 		Location flagLocation = model.getAgPos(agentNumber);
 
 		if (flagsTeam.equals("red_team")) {
 			carrier.capture_flag(RED_FLAG);
 		} else {
 			carrier.capture_flag(BLUE_FLAG);
 		}
 
 		Literal flagCarryingLiteral = Literal.parseLiteral("carrying_flag("
 				+ agentNumber + "," + flagsTeam + ").");
 		addPercept(flagCarryingLiteral);
 	}
 
 	private Agent agentByName(String name) {
 
 		for (Agent ag : this.agents) {
 			if (ag.name().equals(name)) {
 				return ag;
 			}
 		}
 		return null;
 	}
 
 	private synchronized void updatePercepts(int agentNumber) {
 		model.updatePercepts(agentNumber);
 	}
 
 	class SWATModel extends GridWorldModel {
 
 		private int numberOfAgents;
 		private Location blueFlag;
 		private Location redFlag;
 
 		private SWATModel(int numberOfAgents) {
 			super(GRID_SIZE, GRID_SIZE, numberOfAgents);
 
 			this.numberOfAgents = numberOfAgents;
 
 			initWorld();
 		}
 
 		public void respawn(Agent agent) {
 			
 			System.out.println("Agent " + agent.number() + " died");
 
 			agent.dropFlag();
 			
 			if (agent.isBlueTeamMember()) {							
 				System.out.println("He was a blue team member");
 				for (int i = 0; i < GRID_SIZE; ++i) {
 					for (int j = 0; j < GRID_SIZE; ++j) {
 						if (isCellAvailable(i, j)) {							
 							move(agent.number(), i, j);
 							return;
 						}
 					}
 				}
 
 			} else {
 				System.out.println("He was a red team member");
 				for (int i = GRID_SIZE - 1; i >= 0; --i) {
 					for (int j = 0; j < GRID_SIZE; ++j) {
 						if (isCellAvailable(i, j)) {
 							move(agent.number(), i, j);
 							return;
 						}
 					}
 				}
 			}
 							
 		}
 
 		public synchronized void updatePercepts(int agentNumber) {
 			/*
 			 * removePerceptsByUnif(Literal.parseLiteral("flag(T, A, B)"));
 			 * 
 			 * Literal blueFlagLiteral = Literal.parseLiteral("flag(blue_team,"
 			 * + blueFlag.x + ", " + blueFlag.y + ")."); Literal redFlagLiteral
 			 * = Literal.parseLiteral("flag(red_team," + redFlag.x + ", " +
 			 * redFlag.y + ").");
 			 * 
 			 * addPercept(blueFlagLiteral); addPercept(redFlagLiteral);
 			 */
 		}
 
 		public synchronized void moveTowardsLocation(int agentNumber, int x,
 				int y) {
 			Location l = new Location(x, y);
 			Location agentLocation = getAgPos(agentNumber);
 
 			int best_x;
 			int best_y;
 
 			if (l.x > agentLocation.x)
 				best_x = agentLocation.x + 1;
 			else if (l.x < agentLocation.x)
 				best_x = agentLocation.x - 1;
 			else
 				best_x = agentLocation.x;
 
 			if (l.y > agentLocation.y)
 				best_y = agentLocation.y + 1;
 			else if (l.y < agentLocation.y)
 				best_y = agentLocation.y - 1;
 			else
 				best_y = agentLocation.y;
 
 			if (isCellAvailableForAgent(best_x, best_y)) {
 				move(agentNumber, best_x, best_y);
 				return;
 			}
 
 			for (int i = -1; i <= 1; i++) {
 				if (isCellAvailableForAgent(best_x, agentLocation.y + i)) {
 					move(agentNumber, best_x, agentLocation.y + i);
 					return;
 				}
 
 				if (isCellAvailableForAgent(agentLocation.x + i, best_y)) {
 					move(agentNumber, agentLocation.x + i, best_y);
 					return;
 				}
 
 			}
 
 			while (true) {
 				int x_offset = Math.abs(random.nextInt() % 3) - 1;
 				int y_offset = Math.abs(random.nextInt() % 3) - 1;
 				int future_x = agentLocation.x + x_offset;
 				int future_y = agentLocation.y + y_offset;
				if (isCellAvailableForAgent(future_x, future_y)) {
 					move(agentNumber, future_x, future_y);
 
 					break;
 				}
 			}
 
 		}
 
 		public synchronized void move(int agentNumber, int x, int y) {
 			if (!isCellAvailableForAgent(x, y))
 				return;
 
 			Agent agent = agents.get(agentNumber);
 			String agentName = agent.name();
 
 			Location oldLocation = model.getAgPos(agentNumber);
 			Literal oldLocationLiteral = Literal.parseLiteral("position("
 					+ oldLocation.x + "," + oldLocation.y + ").");
 
 			Literal newLocationLiteral = Literal.parseLiteral("position(" + x
 					+ "," + y + ").");
 
 			addPercept(agentName, newLocationLiteral);
 			removePercept(agentName, oldLocationLiteral);
 
 			setAgPos(agentNumber, new Location(x, y));
 
 			agent.setOrientation(resultingOrientation(oldLocation.x,
 					oldLocation.y, x, y));
 			Agent seen = getAgentsInSight(agent);
 			if (seen != null) {
 				Literal seenLiteral = Literal.parseLiteral("seen("
 						+ seen.number() + ").");
 				addPercept(agent.name(), seenLiteral);
 			}
 
 			if (!agent.hasFlag())
 				return;
 
 			int carriedFlag = agent.carried_flag();
 
 			if (carriedFlag == RED_FLAG) {
 				moveFlag(x, y, oldLocation, "red_team");
 			} else {
 				moveFlag(x, y, oldLocation, "blue_team");
 			}
 
 		}
 
 		public Agent getAgentsInSight(Agent agent) {
 			Location pos = getAgPos(agent.number());
 			int x = pos.x;
 			int y = pos.y;
 			String orientation = agent.orientation();
 
 			if (orientation.equals("east")) {
 				x += 1;
 				while (x < width) {
 					if (hasObject(WALL, x, y))
 						return null;
 					int seenAgentNumber = getAgAtPos(x, y);
 					if (seenAgentNumber != -1)
 						return agents.get(seenAgentNumber);
 					++x;
 				}
 			}
 			if (orientation.equals("west")) {
 				x -= 1;
 				while (x >= 0) {
 					if (hasObject(WALL, x, y))
 						return null;
 					int seenAgentNumber = getAgAtPos(x, y);
 					if (seenAgentNumber != -1)
 						return agents.get(seenAgentNumber);
 					--x;
 				}
 			}
 			if (orientation.equals("north")) {
 				y -= 1;
 				while (y >= 0) {
 					if (hasObject(WALL, x, y))
 						return null;
 					int seenAgentNumber = getAgAtPos(x, y);
 					if (seenAgentNumber != -1)
 						return agents.get(seenAgentNumber);
 					--y;
 				}
 			}
 			if (orientation.equals("east")) {
 				y += 1;
 				while (y < height) {
 					if (hasObject(WALL, x, y))
 						return null;
 					int seenAgentNumber = getAgAtPos(x, y);
 					if (seenAgentNumber != -1)
 						return agents.get(seenAgentNumber);
 					++y;
 				}
 			}
 			return null;
 		}
 
 		private synchronized String resultingOrientation(int oldX, int oldY,
 				int newX, int newY) {
 			int xDiff = newX - oldX;
 			if (xDiff == 0) {
 
 				int yDiff = newY - oldY;
 				if (yDiff > 0)
 					return "south";
 				else
 					return "north";
 
 			} else if (xDiff > 0) {
 				return "east";
 			} else {
 				return "west";
 			}
 		}
 
 		private synchronized void moveFlag(int x, int y, Location oldLocation,
 				String team) {
 			Literal oldFlagLocationLiteral = Literal.parseLiteral("flag("
 					+ team + "," + oldLocation.x + "," + oldLocation.y + ").");
 
 			Literal newFlagLocationLiteral = Literal.parseLiteral("flag("
 					+ team + "," + x + "," + y + ").");
 
 			addPercept(newFlagLocationLiteral);
 			removePercept(oldFlagLocationLiteral);
 
 			if (team.equals("red_team")) {
 				add(RED_FLAG, x, y);
 				remove(RED_FLAG, oldLocation);
 			} else {
 				add(BLUE_FLAG, x, y);
 				remove(BLUE_FLAG, oldLocation);
 			}
 		}
 
 		public static final double max_percentage = 0.23;
 		public static final double min_percentage = 0.07;
 
 		void initWorld() {
 
 			setupFlags();
 			setupObstacles();
 			setupTeams();
 
 			updateInitialPercepts();
 		}
 
 		private void setupFlags() {
 			// Setup flags
 			blueFlag = new Location(0, GRID_SIZE / 2);
 			add(BLUE_FLAG, blueFlag.x, blueFlag.y);
 			redFlag = new Location(GRID_SIZE - 1, GRID_SIZE / 2);
 			add(RED_FLAG, redFlag.x, redFlag.y);
 		}
 
 		private void setupObstacles() {
 			// Setup obstacles
 			int max_quantity = (int) ((GRID_SIZE * GRID_SIZE) * (max_percentage));
 			int min_quantity = (int) ((GRID_SIZE * GRID_SIZE) * (min_percentage));
 
 			Random random_generator = new Random();
 			int obstacle_quantity = random_generator.nextInt(max_quantity
 					- min_quantity + 1)
 					+ min_quantity;
 
 			int placed_obstacles = 0;
 			int x, y;
 			while (placed_obstacles < obstacle_quantity) {
 				do {
 					x = random_generator.nextInt(GRID_SIZE);
 					y = random_generator.nextInt(GRID_SIZE);
 				} while (!isCellAvailable(x, y));
 
 				int chosen = random_generator.nextInt(2);
 				int terrain_type;
 				if (chosen == 0)
 					terrain_type = PIT;
 				else
 					terrain_type = WALL;
 
 				add(terrain_type, x, y);
 				++placed_obstacles;
 			}
 		}
 
 		private void setupTeams() {
 			// Setup teams
 			int agentNumber = 0;
 			int agents_per_team = this.numberOfAgents / 2;
 			// blue team agents
 			for (int i = 0; i < GRID_SIZE && agentNumber < agents_per_team; ++i) {
 				for (int j = 0; j < GRID_SIZE && agentNumber < agents_per_team; ++j) {
 					agents.get(agentNumber).setTeam(BLUE_TEAM);
 					agents.get(agentNumber).setOrientation("east");
 					Literal facingLiteral = Literal
 							.parseLiteral("facing(east).");
 					addPercept(agents.get(agentNumber).name(), facingLiteral);
 					if (isCellAvailable(i, j)) {
 						setAgPos(agentNumber, i, j);
 						agentNumber++;
 					}
 				}
 			}
 
 			// red team agents
 			for (int i = GRID_SIZE - 1; i >= 0
 					&& agentNumber < this.numberOfAgents; --i) {
 				for (int j = 0; j < GRID_SIZE
 						&& agentNumber < this.numberOfAgents; ++j) {
 					agents.get(agentNumber).setTeam(RED_TEAM);
 					agents.get(agentNumber).setOrientation("west");
 					Literal facingLiteral = Literal
 							.parseLiteral("facing(west).");
 					addPercept(agents.get(agentNumber).name(), facingLiteral);
 					if (isCellAvailable(i, j)) {
 						setAgPos(agentNumber, i, j);
 						agentNumber++;
 					}
 				}
 			}
 		}
 
 		private void updateInitialPercepts() {
 			Literal borderLiteral = Literal.parseLiteral("borders(0,0,"
 					+ (this.width - 1) + "," + (this.height - 1) + ").");
 
 			addPercept(borderLiteral);
 
 			Literal blueBaseLiteral = Literal.parseLiteral("base(blue_team,"
 					+ 0 + ", " + GRID_SIZE / 2 + ").");
 			Literal redBaseLiteral = Literal.parseLiteral("base(red_team,"
 					+ (GRID_SIZE - 1) + ", " + GRID_SIZE / 2 + ").");
 
 			addPercept(blueBaseLiteral);
 			addPercept(redBaseLiteral);
 
 			Literal blueFlagLiteral = Literal.parseLiteral("flag(blue_team,"
 					+ 0 + ", " + GRID_SIZE / 2 + ").");
 			Literal redFlagLiteral = Literal.parseLiteral("flag(red_team,"
 					+ (GRID_SIZE - 1) + ", " + GRID_SIZE / 2 + ").");
 
 			addPercept(blueFlagLiteral);
 			addPercept(redFlagLiteral);
 
 			for (int i = 0; i < this.numberOfAgents; ++i) {
 				Agent agent = agents.get(i);
 
 				Location location = getAgPos(i);
 				Literal locationLiteral = Literal.parseLiteral("position("
 						+ location.x + "," + location.y + ").");
 
 				addPercept(agent.name(), locationLiteral);
 				Literal teamLiteral;
 				if (agent.isRedTeamMember()) {
 					teamLiteral = Literal.parseLiteral("team(" + agent.number()
 							+ "," + "red_team).");
 				} else {
 					teamLiteral = Literal.parseLiteral("team(" + agent.number()
 							+ "," + "blue_team).");
 				}
 				addPercept(teamLiteral);
 				Literal numberLiteral = Literal.parseLiteral("number("
 						+ agent.number() + ").");
 				addPercept(agent.name(), numberLiteral);
 			}
 
 		}
 
 		boolean isCellAvailableForAgent(int x, int y) {
 			return !hasObject(WALL, x, y) && !hasObject(PIT, x, y)
 					&& !hasObject(AGENT, x, y) && x >= 0 && x < GRID_SIZE
 					&& y >= 0 && y < GRID_SIZE;
 		}
 
 		boolean isCellAvailable(int x, int y) {
 			return !hasObject(WALL, x, y) && !hasObject(PIT, x, y)
 					&& !hasObject(AGENT, x, y) && !hasObject(BLUE_FLAG, x, y)
 					&& !hasObject(RED_FLAG, x, y);
 		}
 
 		int getObject(int x, int y) {
 			return data[x][y];
 		}
 
 	}
 
 	class SWATView extends GridWorldView {
 
 		public SWATView(SWATModel model) {
 			super(model, "SWAT World", 600);
 			defaultFont = new Font("Arial", Font.BOLD, 16); // change default
 			// font
 			setVisible(true);
 			repaint();
 		}
 
 		@Override
 		public void draw(Graphics g, int x, int y, int object) {
 			String description = getCellDescription(object, x, y);
 
 			Color old_color = g.getColor();
 
 			if ((object & RED_FLAG) != 0) {
 				g.setColor(Color.RED);
 				g.fillRect(x * cellSizeW + 1, y * cellSizeH + 1, cellSizeW - 1,
 						cellSizeH - 1);
 			} else if ((object & BLUE_FLAG) != 0) {
 				g.setColor(Color.BLUE);
 				g.fillRect(x * cellSizeW + 1, y * cellSizeH + 1, cellSizeW - 1,
 						cellSizeH - 1);
 			}
 
 			g.setColor(old_color);
 
 			if ((object & model.AGENT) != 0) {
 				Color new_color;
 
 				if (model.getAgAtPos(x, y) < model.getNbOfAgs() / 2)
 					new_color = Color.BLUE;
 				else
 					new_color = Color.RED;
 
 				g.setColor(new_color);
 				g.fillOval(x * cellSizeW + 2, y * cellSizeH + 2, cellSizeW - 4,
 						cellSizeH - 4);
 				g.setColor(old_color);
 			}
 
 			super.drawString(g, x, y, defaultFont, description);
 		}
 
 		@Override
 		public void drawAgent(Graphics g, int x, int y, Color c, int id) {
 			draw(g, x, y, ((SWATModel) model).getObject(x, y));
 		}
 
 		private String getAgentDescription(int x, int y) {
 			int agentId = model.getAgAtPos(x, y);
 
 			return "" + agentId;
 
 		}
 
 		private String getCellDescription(int object, int x, int y) {
 			String result = "";
 
 			if ((object & model.AGENT) != 0)
 				result += getAgentDescription(x, y);
 			if ((object & PIT) != 0)
 				result += " U";
 			if ((object & WALL) != 0)
 				result += " |";
 			if ((object & RED_FLAG) != 0)
 				result += " RF";
 			if ((object & BLUE_FLAG) != 0)
 				result += " BF";
 
 			return result;
 		}
 
 	}
 
 	class Agent {
 
 		private final String type;
 		private final int number;
 		private int carried_flag;
 		private int team;
 		private String orientation;
 
 		public Agent(String type, int number) {
 			this.type = type;
 			this.number = number;
 			this.carried_flag = 0;
 		}
 
 		public String name() {
 			return this.type + (this.number + 1);
 		}
 
 		public void capture_flag(int flag) {
 			this.carried_flag = flag;
 
 		}
 
 		public boolean hasFlag() {
 			return this.carried_flag != 0;
 		}
 
 		public int carried_flag() {
 			return this.carried_flag;
 		}
 		
 		public void dropFlag(){
 			this.carried_flag = 0;
 		}
 
 		public int number() {
 			return this.number;
 		}
 
 		public void setTeam(int team) {
 			this.team = team;
 		}
 
 		public int team() {
 			return team;
 		}
 
 		public boolean isRedTeamMember() {
 			return this.team == RED_TEAM;
 		}
 
 		public boolean isBlueTeamMember() {
 			return this.team == BLUE_TEAM;
 		}
 
 		public void setOrientation(String orientation) {
 			this.orientation = orientation;
 		}
 
 		public String orientation() {
 			return orientation;
 		}
 
 	}
 }
