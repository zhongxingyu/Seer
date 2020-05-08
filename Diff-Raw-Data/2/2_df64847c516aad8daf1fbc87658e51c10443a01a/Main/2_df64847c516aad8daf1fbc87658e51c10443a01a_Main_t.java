 package gdp.racetrack;
 
 import gdp.racetrack.Lists.ImplementationList;
 import gdp.racetrack.gui.MainFrame;
 import gdp.racetrack.util.ClassList.ClassDescription;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.GridLayout;
 
 import javax.swing.JFrame;
 import javax.swing.JTextArea;
 
 public class Main {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		boolean debugwindow = false;
 		
 		int i;
 		for (i = 0; i < args.length; ++i) {
 			if (args[i].equals("-d")) debugwindow = true;
			else break;
 		}
 		
 		if (debugwindow) Log.addDebugWindow();
 		
 		if (args.length == i+1 && args[i].equals("test")) {
 			test();
 		}
 		else if (args.length == i+1 && args[i].equals("mapgen")) {
 			MapGenerator.main(new String[0]);
 		}
 		else if (args.length == i+1 && args[i].equals("listai")) {
 			list("AI", Lists.ai);
 		}
 		else if (args.length == i+1 && args[i].equals("listrules")) {
 			list("Turn Rules",                  Lists.turnRule);
 			list("Victory Rules",               Lists.victoryRule);
 			list("Environment Collision Rules", Lists.envCollisionRule);
 			list("Player Collision Rules",      Lists.playerCollisionRule);
 		}
 		else if (args.length == i+1 && args[i].equals("newgui")) {
 			new MainFrame();
 		}
 		else if (args.length == i) {
 			new Z_Menu();
 		} else {
 			System.err.println("Usage: java gdp.racetrack.Main [-d] [test|mapgen|listai|listrules|newgui]");
 			System.err.println("    or java -jar <jarfile> [-d] [test|mapgen|listai|listrules|newgui]");
 		}
 	}
 
 	private static void list(String listName, ImplementationList<?> list) {
 		System.out.println(listName);
 		for (int i=0; i < listName.length(); ++i) System.out.print('=');
 		System.out.println();
 		
 		for (ClassDescription desc : list.getList()) {
 			System.out.println("Name:  "+desc.name);
 			System.out.println("class: "+desc.clazz.getName());
 			System.out.println(desc.description);
 			System.out.println("--- ---");
 		}
 		
 		System.out.println();
 	}
 
 	private static void test() {
 		System.out.println("The system will run a test. Please wait");
 		
 		System.out.println("create map ...");
 		final Map map = new MapGenerator().generateMap((int) (Math.random()-0.5)*2*Integer.MAX_VALUE, 3, Difficulty.NORMAL);
 		
 		System.out.println("create game rules ...");
 		PlayerCollisionRule playerCollisionRule =
 				Lists.playerCollisionRule.createInstance(0);
 		EnvironmentCollisionRule envCollisionRule =
 				Lists.envCollisionRule.createInstance(0);
 		TurnRule turnRule =
 				Lists.turnRule.createInstance(0);
 		VictoryRule victoryRule =
 				Lists.victoryRule.createInstance(0);
 
 		final RuleSet ruleSet = new RuleSet(envCollisionRule, playerCollisionRule, turnRule, victoryRule);
 		
 		System.out.println("create game ...");
 		final Game game = new Game(map, ruleSet, null);
 		
 		System.out.println("create an AI ...");
 		final AI ai = Lists.ai.createInstance(0, game);
 		System.out.println("create 3 bots of the created AI ...");
 		game.addPlayer(ai, Difficulty.EASY, Difficulty.NORMAL, Difficulty.HARD);
 		
 		System.out.println("create display window ...");
 		SimpleTestFrame frame = new SimpleTestFrame(game);
 		game.registerListener(frame);
 		
 		System.out.println("run the game ...");
 		game.run();
 		
 		System.out.println("FINISHED.");
 	}
 
 	private static class SimpleTestFrame implements EventListener {
 		private final Game game;
 		private final JFrame window;
 		private final JTextArea textArea;
 
 		private SimpleTestFrame(final Game game) {
 			this.game = game;
 			
 			window = new JFrame();
 			window.setLayout(new GridLayout(1, 1));
 			
 			textArea = new JTextArea();
 			textArea.setSize(window.getSize());
 			window.getContentPane().add(textArea);
 			textArea.setForeground(Color.GRAY);
 			textArea.setFont(new Font(Font.MONOSPACED, Font.BOLD, 12));
 			
 			window.setSize(800, 600);
 			window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 			
 			window.setVisible(true);
 		}
 
 		@Override
 		public void onGameStart(boolean firstTime) {
 			render();
 		}
 
 		@Override
 		public void onGamePause() {
 			render();
 			textArea.append("Game is paused");
 		}
 
 		@Override
 		public void onGameFinished() {
 			render();
 			textArea.append("Game is finshed");
 		}
 
 		@Override
 		public void onPlayerChooseStart(Player player) {}
 
 		@Override
 		public void onPlayerTurn(Player player, Point startPoint,
 				Point endPoint, Point destinationPoint) {
 			
 			render();
 		}
 
 		private void render() {
 			Map map = game.getMap();
 			char[][] mapRender = new char[map.getSize().x][map.getSize().y];
 			for (int x = 0; x < map.getSize().x; ++x) {
 				for (int y = 0; y < map.getSize().y; ++y) {
 					PointType type = map.getPointType(new Point(x,y));
 					switch (type) {
 					case TRACK:
 						mapRender[x][y] = ' ';
 						break;
 					case START: case FINISH:
 						mapRender[x][y] = ' ';
 						break;
 					case NONE:
 						mapRender[x][y] = '#';
 						break;
 					default:
 						assert 0==1 : "this should not be possible";
 					}
 				}
 			}
 			for (Player p : game.getPlayers()) {
 				Point point = p.getPosition();
 				mapRender[point.getX()][point.getY()] = (""+p.getNumber()).charAt(0);
 			}
 			StringBuilder builder = new StringBuilder();
 			for (int y = map.getSize().y-1; y >= 0; --y) {
 				for (int x = 0; x < map.getSize().x; ++x) {
 					builder.append(mapRender[x][y]);
 				}
 				builder.append('\n');
 			}
 			textArea.setText(builder.toString());
 		}
 	}
 
 /*
 	private static class TestFrame extends javax.swing.JFrame implements EventListener {
 		private static final long serialVersionUID = 1L;
 		private final Game game;
 
 		TestFrame(final Game game) {
 			this.game = game;
 			Vec2D size = game.getMap().getSize();
 			
 			setSize(size.x*Map.GRIDSIZE, size.y*Map.GRIDSIZE);
 			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 			setVisible(true);
 			
 			repaint();
 		}
 
 		@Override
 		public void paintComponents(java.awt.Graphics g) {
 			g.drawImage(game.getMap().getImage(), 6, 6, this.getSize().width, this.getSize().height, null);
 			g.drawLine(0, 0, 256, 256);
 			for (Player player : game.getPlayers()) {
 				for (IrrevocableTurn turn : player.getTurnHistory()) {
 					g.drawLine(turn.getStartPosition().getX(), turn.getStartPosition().getY(),
 							turn.getEndPosition().getX(), turn.getEndPosition().getY());
 				}
 			}
 		}
 
 		@Override
 		public void onGameStart(boolean firstTime) { }
 
 		@Override
 		public void onGamePause() { }
 
 		@Override
 		public void onGameFinished() {
 			// TODO Auto-generated method stub
 		}
 
 		@Override
 		public void onPlayerChooseStart(Player player) { }
 
 		@Override
 		public void onPlayerTurn(Player player, Point startPoint,
 				Point endPoint, Point destinationPoint) {
 			
 			this.repaint();
 		}
 		
 	}
 */
 
 	private Main() { }
 
 }
