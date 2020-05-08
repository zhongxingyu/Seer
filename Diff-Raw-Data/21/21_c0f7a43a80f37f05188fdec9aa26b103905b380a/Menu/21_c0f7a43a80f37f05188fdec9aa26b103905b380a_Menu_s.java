 package bomberman;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.UnknownHostException;
 
 import bomberman.game.Board;
 import bomberman.game.Controls;
 import bomberman.game.ExplosionAreaCalculator;
 import bomberman.game.Game;
 import bomberman.game.Settings;
 import bomberman.game.TwoPlayerGameClient;
 import bomberman.game.TwoPlayerGameServer;
 import bomberman.game.objects.Exit;
 import bomberman.gui.GameGui;
 import bomberman.gui.MenuGui;
 
 import com.thoughtworks.xstream.XStream;
 import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;
 
 public class Menu {
 
 	/**
 	 * Starts the game.
 	 * 
 	 * @throws IOException
 	 * @throws UnknownHostException
 	 */
 	public void startGame() {
 		HighScore hs;
 		try {
 			new FileInputStream(Settings.highScorePath);
 			hs = loadHighScore();
 			hs.copyFromInstance();
 		} catch (FileNotFoundException swallowed) {
 			hs = new HighScore();
 		}
 
 		while (true) {
 			final MenuGui mg = new MenuGui();
 			int i = 0;
 			while (i == 0) {
 				i = mg.gameStarted();
 			}
 			switch (i) {
 			case -1:
 				hs.copyToInstance();
 				XStream xstream = new XStream(new JettisonMappedXmlDriver());
 				String xml = xstream.toXML(hs);
 				try {
 					FileWriter fw = new FileWriter(Settings.highScorePath);
 					final BufferedWriter bw = new BufferedWriter(fw);
 					bw.write(xml);
 					bw.close();
 				} catch (IOException e1) {
 					System.out.println("could not create file");
 				}
 				System.exit(0);
 				break;
 			case 1:
 				final Game g = setupGame();
 				g.start();
 				break;
 			case 2:
 				final Game h = setupServer();
 				if (h != null) {
 					h.start();
 				}
 				break;
 			case 3:
				Game c;
				try {
					c = new TwoPlayerGameClient();
					c.start();
				} catch (Exception swallowed) {
					mg.unknownHost = true;
					while (mg.enterIPLoop()) {
						;
 					}
 				}
 				break;
 			case 4:
 				loadSaveGame();
 				break;
 			default:
 				break;
 			}
 		}
 
 	}
 
 	/**
 	 * Setup Game: initializes a new board with (currently) fixed parameters
 	 * also sets up game-logic (i.e Explosion Calculator, Controls and GUI)
 	 * 
 	 * @return game Object
 	 */
 	public Game setupGame() {
 		final int TILESIZE = Settings.TILESIZE;
 
 		final Board b = new Board(10, 10);
 		final ExplosionAreaCalculator eac = new ExplosionAreaCalculator(
 				b.getField(), TILESIZE);
 		final GameGui gui = new GameGui(b.getField(), TILESIZE, eac);
 		final Exit exit = new Exit(9, 10, TILESIZE);
 		final Controls controls = new Controls(b, TILESIZE);
 
 		final Game g = new Game(controls, exit, eac, gui, b);
 
 		return g;
 	}
 
 	private Game setupServer() {
 		final int TILESIZE = Settings.TILESIZE;
 
 		final Board b = new Board(10, 10);
 		final ExplosionAreaCalculator eac = new ExplosionAreaCalculator(
 				b.getField(), TILESIZE);
 		final GameGui gui = new GameGui(b.getField(), TILESIZE, eac);
 		final Exit exit = new Exit(21, 10, TILESIZE);
 		final Controls controls = new Controls(b, TILESIZE);
 		Game g = null;
 		try {
 			g = new TwoPlayerGameServer(controls, exit, eac, gui, b);
 		} catch (UnknownHostException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return g;
 	}
 
 	private void loadSaveGame() {
 		FileInputStream fstream;
 		StringBuffer sb = new StringBuffer();
 		try {
 			fstream = new FileInputStream(Settings.saveGamePath);
 
 			final DataInputStream in = new DataInputStream(fstream);
 			final BufferedReader br = new BufferedReader(new InputStreamReader(
 					in));
 
 			String tmp;
 
 			try {
 				while ((tmp = br.readLine()) != null) {
 					sb.append(tmp);
 				}
 				in.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		} catch (FileNotFoundException e) {
 		}
 		File f = new File(Settings.saveGamePath);
 		f.delete();
 		XStream xstream = new XStream(new JettisonMappedXmlDriver());
 		Game g = (Game) xstream.fromXML(sb.toString());
 		g.getGameGui().initialize();
 		g.loop();
 	}
 
 	private static HighScore loadHighScore() {
 		FileInputStream fstream;
 		StringBuffer sb = new StringBuffer();
 		try {
 			fstream = new FileInputStream(Settings.highScorePath);
 
 			final DataInputStream in = new DataInputStream(fstream);
 			final BufferedReader br = new BufferedReader(new InputStreamReader(
 					in));
 
 			String tmp;
 
 			try {
 				while ((tmp = br.readLine()) != null) {
 					sb.append(tmp);
 				}
 				in.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		} catch (FileNotFoundException e) {
 		}
 		XStream xstream = new XStream(new JettisonMappedXmlDriver());
 		final HighScore hs = (HighScore) xstream.fromXML(sb.toString());
 		return hs;
 	}
 
 }
