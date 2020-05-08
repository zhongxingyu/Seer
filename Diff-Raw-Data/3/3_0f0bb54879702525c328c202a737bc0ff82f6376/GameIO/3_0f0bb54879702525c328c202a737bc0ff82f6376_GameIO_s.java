 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.StringTokenizer;
 
 import java.util.Map;
 import java.util.HashMap;
 import java.util.LinkedList;
 
 import java.util.concurrent.BlockingQueue;
 
 import java.util.regex.Pattern;
 import java.util.regex.Matcher;
 
 public class GameIO {
 	Client client;
 
 	public GameIO(Client client) {
 		this.client = client;
 
 		this.client.write("launch;");
 
 		this.walks_now = WalkSide.NONE;
 
 		this.bot_state = BotState.READY;
 		this.bot_dir = WalkSide.NONE;
 	}
 
 	public void execute() {
 		final Client.Events src = this.client.addListener();
 		Thread listener = new Thread(new Runnable() {
 			public void run() {
 				while (true) {
 					if (Thread.interrupted()) {
 						return;
 					}
 					try {
 						parseOutput(src.take());
 					} catch (Exception e) {
 						throw new RuntimeException(e);
 					}
 				}
 			}
 		});
 
 		listener.start();
 
 		try {
 			byte buf[] = new byte[1024];
 			int read_cnt;
 			while ((read_cnt = System.in.read(buf)) > 0) {
 				if (!listener.isAlive()) {
 					return;
 				}
 				StringTokenizer commands
 				 = new StringTokenizer(new String(buf, 0, read_cnt), ";", true);
 				
 				while (commands.countTokens() > 1) {
 					String command = commands.nextToken();
 					command += commands.nextToken();
 					System.out.println("Cmd: " + command);
 
 					Matcher cmd_m = my_cmd_pat.matcher(command);
 					if (cmd_m.find()) {
 						if (cmd_m.group(2) != null) {
 							try {
 								this.print();
 							} catch (Exception e) {
 								e.printStackTrace(System.out);
 							}
 						} else if (cmd_m.group(3) != null) {
 							int new_walk_limit = Integer.parseInt(cmd_m.group(5));
 							WalkSide new_walks_now = this.walk_side_by_string.get(cmd_m.group(4));
 							client.write(cmd_m.group(4) + ";");
 							synchronized (this) {
 								this.walk_limit = new_walk_limit;
 								this.walks_now = new_walks_now;
 							}
 						} else if (cmd_m.group(6) != null) {
 							System.out.println("Match start");
 							int new_walk_limit = Integer.parseInt(cmd_m.group(8));
 							WalkSide new_walks_now = this.walk_side_by_string.get(cmd_m.group(7));
 							client.write(cmd_m.group(7) + ";");
 							synchronized (this) {
 								this.bot_state = BotState.APPROACH;
 								this.lim = new_walk_limit;
 								this.bot_dir = new_walks_now;
 							}
 						} else {
 							System.out.println("Unknown command: " + command);
 						}
 					} else {
 						client.write(command);
 					}
 				}
 			}
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		} finally {
 			listener.interrupt();
 		}
 	}
 
 	// now state.
 	
 	int pid;
 	int round;
 
 	int cell_size;
 	int map_height;
 	int map_width;
 	List<String> map;
 
 	int time;
 
 	boolean dead;
 	Point me;
 	int speed;
 
 	Point bomb;
 
 	// for printing
 	boolean map_changed;
 	int last_visual_x;
 	int last_visual_y;
 
 	static Pattern head;
 	static Pattern map_symbols;
 	static Pattern sapka_info_pat;
 	static Pattern my_cmd_pat;
 	static Map<String, WalkSide> walk_side_by_string;
 	static Pattern map_change_pat;
 
 	private synchronized void parseOutput(String output) {
 		Matcher matcher = head.matcher(output);
 
 		if (!matcher.find()) {
 			System.out.println("Output: " + output);
 			return;
 		}
 
 		if (matcher.group(2) != null) {
 			this.pid = Integer.parseInt(matcher.group(3));
 		} else if (matcher.group(4) != null) {
 			this.round = Integer.parseInt(matcher.group(5));
 			this.cell_size =  Integer.parseInt(matcher.group(6));
 			this.map = new ArrayList<String>();
 			Matcher map_matcher = map_symbols.matcher(matcher.group(7));
 			this.map_height = 0;
 			this.map_width = 0;
 			while (map_matcher.find()) {
 				this.map_height++;
 				String line = map_matcher.group(1);
 				if (this.map_width == 0) {
 					this.map_width = line.length();
 					System.out.println("Width: " + this.map_width);
 				}
 				this.map.add(line);
 			}
 			this.map_height = this.map.size();
 			Collections.reverse(this.map);
 		} else if (matcher.group(8) != null) {
 			this.time = Integer.parseInt(matcher.group(9));
 			Matcher sapka_matcher = sapka_info_pat.matcher(matcher.group(10));
 			while (sapka_matcher.find()) {
 				if (Integer.parseInt(sapka_matcher.group(1)) != this.pid) {
 					continue;
 				}
 
 				if (sapka_matcher.group(2).equals("dead")) {
 					this.dead = true;
 					break;
 				}
 				int x = Integer.parseInt(sapka_matcher.group(3));
 				int y = this.map_height * this.cell_size - Integer.parseInt(sapka_matcher.group(4)) - 1;
 				this.me = new Point(x, y);
 			}
 
 			Matcher mc = map_change_pat.matcher(matcher.group(11));
 			while (mc.find()) {
 				int x = Integer.parseInt(mc.group(3));
 				int y = this.map_height - Integer.parseInt(mc.group(4)) - 1;
 
 				char new_symbol;
 				if (mc.group(1).equals("-")) {
 					new_symbol = '.';
 				} else if (mc.group(1).equals("+")) {
 					new_symbol = mc.group(2).charAt(0);
 				} else {
 					System.out.println("Unknown mamchange: " + matcher.group(11));
 					continue;
 				}
 
 				System.out.println("Map change: '" + new_symbol + "' at " + x +  ", " + y);
 				String line = this.map.get(y);
 				this.map.set(y, line.substring(0, x) + new_symbol + line.substring(x + 1));
 				this.map_changed = true;
 			}
 
 			if ((this.me != null
 				 && (this.me.x != this.last_visual_x
 				     || this.me.y  != this.last_visual_y))
 				|| this.map_changed)
 			{
 				this.print();
 			}
 			treatWalk();
 			String command = this.changeBotState();
 			if (!command.equals("")) {
 				System.out.println("Bot command: " + command);
 				this.client.write(command);
 			}
 		}
 	}
 
 	private synchronized void print() {
 		try {
 			int visual_x = -1;
 			int visual_y = -1;
 			if (this.me != null) {
 				visual_x = this.me.x;
 				visual_y = this.me.y;
 			}
 			StringBuffer nums = new StringBuffer(this.map_width * this.cell_size + 1);
 			nums.append(' ');
 			for (int j = 0; j < this.map_width * this.cell_size; ++j) {
 				nums.append(Integer.toString(j % 10).charAt(0));
 			}
 			System.out.println(nums);
 			for (int i = this.map.size() * this.cell_size - 1; i >= 0; --i) {
 				String line = this.map.get(i / this.cell_size);
 				StringBuffer sb = new StringBuffer(line.length() * this.cell_size + 1);
 				sb.append(' ');
 				for (int j = 0; j < line.length() * this.cell_size; ++j) {
 					if (visual_y == i && visual_x == j ) { // if visual_y == -1 this never happen
 						sb.append('@');
 					} else {
 						sb.append(line.charAt(j / this.cell_size));
 					}
 				}
 				System.out.println(sb);
 			}
 			if (this.me != null) {
 				System.out.println("Me: " + this.me.x + ", " + this.me.y);
 			}
 			this.last_visual_x = visual_x;
 			this.last_visual_y = visual_y;
 			this.map_changed = false;
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	// for stop
 	enum WalkSide {
 		UP, DOWN, LEFT, RIGHT, NONE
 	}
 	WalkSide walks_now;
 	int walk_limit;
 
 	private static WalkSide sideBack(WalkSide side) {
 		switch (side) {
 		case UP: return WalkSide.DOWN;
 		case DOWN: return WalkSide.UP;
 		case RIGHT: return WalkSide.LEFT;
 		case LEFT: return WalkSide.RIGHT;
 		default: return WalkSide.NONE;
 		}
 	}
 
 	// XXX: make functional list instead
 	private static class Sides extends LinkedList<WalkSide> {
 		public Sides(WalkSide s1, WalkSide s2) {
 			super();
 			add(s1);
 			add(s2);
 		}
 	}
 
 	private static Iterable<WalkSide> sideTurns(WalkSide side) {
 		switch (side) {
 		case UP:
 		case DOWN:
 			return new Sides(WalkSide.LEFT, WalkSide.RIGHT);
 		case LEFT:
 		case RIGHT:
 			return new Sides(WalkSide.DOWN, WalkSide.UP);
 		default:
 			return Collections.emptyList();
 		}
 	}
 
 	private void treatWalk() {
 		if (this.me == null) {
 			return;
 		}
 
 		if (this.isLimitReached(this.walks_now, this.walk_limit) && this.walks_now != WalkSide.NONE) {
 			System.out.println("stopped;");
 			this.walks_now = WalkSide.NONE;
 			this.client.write("s;");
 		}
 	}
 
 	enum BotState {
 		READY
 		, SEEK
 		, APPROACH
 		, WAIT_PLANT
 		, RUN
 		, RUN_SIDE
 		, WAIT
 		, WAIT_SIDE
 		, RETURN_SIDE
 		, RETURN
 		, GETTING
 		, GETTING_BACK
 	}
 	private BotState bot_state;
 	private WalkSide bot_dir;
 	private WalkSide bot_dir2; // for *_SIDE
 	private int bomb_x;
 	private int bomb_y;
 	private int lim; // how far to go
 
 	// changes state
 	// return command(s) to send to client
 	private synchronized String changeBotState() {
 		if (this.dead || this.me == null) {
 			this.bot_state = BotState.READY;
 		}
 
 		switch (this.bot_state) {
 		case READY:
 			if (!this.dead && this.me != null) {
 				return this.selectOnStart();
 			}
 			break;
 		case SEEK:
 			if (this.isLimitReached(this.bot_dir, this.lim)) {
 				return this.selectOnStart(); // do not use already existing here
 			}
 			break;
 		case APPROACH:
 			if (this.isLimitReached(this.bot_dir, this.lim)) {
 				this.bot_state = BotState.WAIT_PLANT;
 				return "sb;";
 			}
 			break;
 		case WAIT_PLANT:
 			if (this.checkBombPlantedHere()) {
 				this.bot_state = BotState.RUN;
 				this.bot_dir = sideBack(this.bot_dir);
 				this.lim = this.limNextCell(this.bot_dir);
 				this.bomb_x = this.me.x / this.cell_size;
 				this.bomb_y = this.me.y / this.cell_size;
 				return commandFromWalkSide(this.bot_dir);
 			} else {
 				// what here?
 				return "b;";
 			}
 		case RUN:
 			if (this.isBombBoomed()) {
 				return this.selectReturn();
 			}
 			if (this.isLimitReached(this.bot_dir, this.lim)) {
 				for (WalkSide side: sideTurns(this.bot_dir)) {
 					if (this.isNextCellClean(side)) {
 						this.bot_state = BotState.RUN_SIDE;
 						this.bot_dir2 = side;
 						return this.selectWalkNextCell(side);
 						/*
 						this.lim = this.limNextCell(side);
 						return commandFromWalkSide(side);
 						*/
 					}
 				}
 				int next_cell = this.limNextCell(this.bot_dir);
 				if (this.isNextCellClean(this.bot_dir)) {
 					this.lim = next_cell;
 					return "";
 				} else if (this.isLimitReached(this.bot_dir, next_cell, -this.speed)) {
 					this.bot_state = BotState.WAIT;
 					return "s;";
 				}
 			}
 			break;
 		case RUN_SIDE:
 			if (this.isBombBoomed()) {
 				if (this.isLimitReached(this.bot_dir2, this.lim)) {
 					return this.selectReturnSide();
 				} else {
 					return this.selectReturn();
 				}
 			}
 			if (this.isLimitReached(this.bot_dir2, this.lim)) {
 				this.bot_state = BotState.WAIT_SIDE;
 				return "s;";
 			}
 			break;
 		case WAIT:
 			if (this.isBombBoomed()) {
 				return this.selectReturn();
 			}
 			break;
 		case WAIT_SIDE:
 			if (this.isBombBoomed()) {
 				return this.selectReturnSide();
 			}
 			break;
 		case RETURN_SIDE:
 			if (this.isLimitReached(this.bot_dir2, this.lim)) {
 				return this.selectReturn();
 			}
 			break;
 		case RETURN:
 			if (this.isLimitReached(this.bot_dir, this.lim)) {
 				return this.selectCollect();
 			}
 			break;
 		case GETTING:
 			if (this.isLimitReached(this.bot_dir, this.lim)) {
 				this.bot_state = BotState.GETTING_BACK;
 				this.bot_dir = sideBack(this.bot_dir);
 				return this.selectWalkNextCell(this.bot_dir);
 			}
 			break;
 		case GETTING_BACK:
 			if (this.isLimitReached(this.bot_dir, this.lim)) {
 				return this.selectCollect();
 			}
 			break;
 		}
 		return "";
 	}
 
 	// TODO: write!!!
 	private String selectOnStart() {
 		if (this.bot_state != BotState.READY) {
 			this.bot_state = BotState.READY;
 			return "s;";
 		} else {
 			return "";
 		}
 	}
 
 	// TODO: write
 	private String selectCollect() {
 		this.bot_state = BotState.READY;
 		return "s;";
 	}
 
 	private String selectReturn() {
 		this.bot_state = BotState.RETURN;
 		this.bot_dir = sideBack(this.bot_dir);
		return this.selectWalkNextCell(this.bot_dir);
 	}
 
 	private String selectReturnSide() {
 		this.bot_state = BotState.RETURN_SIDE;
 		this.bot_dir2 = sideBack(this.bot_dir2);
 		return this.selectWalkNextCell(this.bot_dir2);
 	}
 
 	private String selectWalkNextCell(WalkSide side) {
 		this.lim = this.limNextCell(side);
 		return commandFromWalkSide(side);
 	}
 
 	// offset - how far beyound the lim 
 	private boolean isLimitReached(WalkSide side, int lim, int offset) {
 		if (this.me == null) {
 			return false;
 		}
 		switch (side) {
 		case UP:
 			return this.me.y >= lim + offset;
 		case DOWN:
 			return this.me.y <= lim - offset;
 		case LEFT:
 			return this.me.x <= lim - offset;
 		case RIGHT:
 			return this.me.x >= lim + offset;
 		default:
 			// ???
 			return true;
 		}
 	}
 
 	private boolean isLimitReached(WalkSide side, int lim) {
 		return isLimitReached(side, lim, 0);
 	}
 
 	private boolean isNextCellClean(WalkSide side) {
 		int x = this.me.x / this.cell_size;
 		int y = this.me.y / this.cell_size;
 		switch (side) {
 		case UP:
 			return (map.get(y + 1).charAt(x) == '.');
 		case DOWN:
 			return (map.get(y - 1).charAt(x) == '.');
 		case LEFT:
 			return (map.get(y).charAt(x - 1) == '.');
 		case RIGHT:
 			return (map.get(y).charAt(x + 1) == '.');
 		default:
 			// ???
 			return false;
 		}
 	}
 
 	private int limNextCell(WalkSide side) {
 		if (this.me == null) {
 			return -1;
 		}
 		int x = this.me.x / this.cell_size;
 		int y = this.me.y / this.cell_size;
 		switch (side) {
 		case UP:
 			return (this.me.y / this.cell_size + 1) * this.cell_size;
 		case DOWN:
 			return (this.me.y / this.cell_size) * this.cell_size - 1;
 		case LEFT:
 			return (this.me.x / this.cell_size) * this.cell_size - 1;
 		case RIGHT:
 			return (this.me.x / this.cell_size + 1) * this.cell_size;
 		default:
 			// ???
 			return -1;
 		}
 	}
 
 	private String commandFromWalkSide(WalkSide side) {
 		for (Map.Entry<String, WalkSide> e: this.walk_side_by_string.entrySet()) {
 			if (e.getValue() == side) {
 				return e.getKey() + ";";
 			}
 		}
 		return "";
 	}
 
 	private boolean checkBombPlantedHere() {
 		int x = this.me.x / this.cell_size;
 		int y = this.me.y / this.cell_size;
 
 		boolean res = (map.get(y).charAt(x) == '*');
 		if (res) {
 			this.bomb_x = x;
 			this.bomb_y = y;
 		}
 		return res;
 	}
 
 	private boolean isBombBoomed() {
 		return (map.get(this.bomb_y).charAt(bomb_x) != '*');
 	}
 
 	static {
 		head =
 		 Pattern.compile(
 		    "^(" // 1
 			+ "(PID([0-9]*)&[^;]*)" // 2,3
 			+ "|(START([0-9]*)&([0-9]+)\r\n([^;]+))" // 4,5,6,7
 			+ "|(T([0-9]*)&([^;&]*)&([^;&]*)(&[^;]*)?)" // 8,9,10,11,12
 			+ ");");
 		/*
 			+ "(REND (-?[0-9]*))" // 10,11
 			+ "(GEND (-?[0-9]*))" // 12,13
 		*/
 		
 		map_symbols = Pattern.compile("^([\\.Xw]+)$", Pattern.MULTILINE);
 
 		sapka_info_pat =
 		 Pattern.compile("P([0-9]+) "
 		  + "(dead|([0-9]+) ([0-9]+) ([0-9]+) ([0-9]+) ([0-9]+)( i)?)(,|$)");
 
 		map_change_pat =
 		 Pattern.compile("([\\+-])([\\*wX#bvfrsuo\\?]) ([0-9]+) ([0-9]+)[ 0-9]*(,|$)");
 
 		my_cmd_pat = Pattern.compile("^("
 			+ "(p)" // 2
 			+ "|((u|d|l|r)([0-9]+))" // 3,4,5
 			+ "|(w(u|d|l|r)([0-9]+))" // 6,7,8
 			+ ");");
 
 		walk_side_by_string = new HashMap<String, WalkSide>();
 		walk_side_by_string.put("u", WalkSide.UP);
 		walk_side_by_string.put("d", WalkSide.DOWN);
 		walk_side_by_string.put("l", WalkSide.LEFT);
 		walk_side_by_string.put("r", WalkSide.RIGHT);
 	}
 }
