 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.StringTokenizer;
 
 import java.util.Map;
 import java.util.HashMap;
 
 import java.util.concurrent.BlockingQueue;
 
 import java.util.regex.Pattern;
 import java.util.regex.Matcher;
 
 public class GameIO {
 	Client client;
 
 	public GameIO(Client client) {
 		this.client = client;
 
 		this.client.write("launch;");
 
 		this.walks_now = WalkSide.NONE;
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
 				 && (this.me.x / this.cell_size != last_visual_x
 				     || this.me.y / this.cell_size != last_visual_y))
 				|| this.map_changed)
 			{
 				this.print();
 			}
 			treatWalk();
 		}
 	}
 
 	private synchronized void print() {
 		try {
 			int visual_x = -1;
 			int visual_y = -1;
 			if (this.me != null) {
 				visual_x = this.me.x / this.cell_size;
 				visual_y = this.me.y / this.cell_size;
 			}
 			for (int i = this.map.size() - 1; i >= 0; --i) {
 				String line = this.map.get(i);
 				if (visual_y == i) { // if visual_y == -1 this never happen
 					line = line.substring(0, visual_x) + "@" + line.substring(visual_x + 1);
 				}
 				System.out.println(line);
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
 
 	private void treatWalk() {
 		if (this.me == null) {
 			return;
 		}
 
 		boolean stop_needed = false;
 		switch (this.walks_now) {
 		case NONE:
 			return;
 		case UP:
 			stop_needed = this.me.y >= this.walk_limit;
 			break;
 		case DOWN:
 			stop_needed = this.me.y <= this.walk_limit;
 			break;
 		case LEFT:
 			stop_needed = this.me.x <= this.walk_limit;
 			break;
 		case RIGHT:
 			stop_needed = this.me.x >= this.walk_limit;
 			break;
 		}
 		if (stop_needed) {
 			System.out.println("stopped;");
 			this.walks_now = WalkSide.NONE;
 			this.client.write("s;");
 		}
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
 			+ ");");
 
 		walk_side_by_string = new HashMap<String, WalkSide>();
 		walk_side_by_string.put("u", WalkSide.UP);
 		walk_side_by_string.put("d", WalkSide.DOWN);
 		walk_side_by_string.put("l", WalkSide.LEFT);
 		walk_side_by_string.put("r", WalkSide.RIGHT);
 	}
 }
