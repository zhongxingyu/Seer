 package hu.miracle.workers;
 
 import java.awt.Point;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 public class Main {
 
 	private static Game game;
 	private static Scene scene;
 	private static Timer timer;
 
 	public static int showMenu() {
 		int result = 0;
 		boolean success = false;
 		String menu[] = { "Hangya szuletese", "Hangya etelfelvetele",
 				"Hangya mereg altali pusztulasa", "Hangyaszsun szuletese",
 				"Hangyairto spray fujasa", "Szagtalanito spray fujasa", "Idozito tick",
 				"Hangya akadalyelkerulese", "Hangya hangyalesobe lepese", "Jatek szuneteltetese",
 				"Jatek folytatasa", "Jatek nehezsegenek beallitasa", "Toplista mentese", "Kilepes" };
 		BufferedReader bfread = new BufferedReader(new InputStreamReader(System.in));
 
 		System.out.println("Jatek szimulalasa\nValasszon az alabbi menupontok kozul:\n");
 		for (int i = 0; i < menu.length; i++) {
 			String scenario = menu[i];
 			System.out.println(String.format("%2d. %s", i + 1, scenario));
 		}
 
 		while (!success) {
 			try {
 				System.out.print("\nValasztott menupont: ");
 				result = Integer.parseInt(bfread.readLine().trim());
 				if (0 < result && result <= menu.length) {
 					success = true;
 				} else {
 					System.out.println("Nem megfelelo ertek! Kerem a menupontok kozul valasszon!");
 				}
 			} catch (NumberFormatException e) {
 				System.out.println("Nem megfelelo ertek! Kerem egy egesz szamot adjon meg!");
 			} catch (IOException e) {
 			}
 		}
 		return result;
 	}
 
 	public static void main(String[] args) {
 		System.out.println(Main.class.getCanonicalName() + ".main()");
 
 		scene = new Scene();
 		game = new Game(scene);
 		timer = new Timer(game, 1000);
 		game.setTimer(timer);
 		timer.start();
		//timer.stopTimer();
 
 		while (true) {
 			
 			int menuresult = showMenu();
 
 			switch (menuresult) {
 			case 1:
 				// TODO: hangya utnak inditasa
 				break;
 
 			case 2:
 				// TODO: hangya etelfelvetele
 				break;
 
 			case 3:
 				// TODO: hangya mereg miatt elpusztul
 				break;
 
 			case 4:
 				// TODO: hangyaszsun elinditasa
 				break;
 
 			case 5:
 				// TODO: mereg spray fujas
 				break;
 
 			case 6:
 				// TODO: szagtalanito spray fujas
 				break;
 
 			case 7:
 				timer.tick();
 				break;
 
 			case 8:
 				// TODO: hangya kikeruli az akadalyt
 				break;
 
 			case 9:
 				// Inicializálás
 				Point c9pos = new Point(0, 0);
 				AntHill c9hill = new AntHill(c9pos, scene, 1, 1);
 				AntSinker c9sink = new AntSinker(c9pos);
 				Ant c9ant = new Ant(c9pos, scene, c9hill);
 				scene.getObstacles().add(c9sink);
 				scene.getAnts().add(c9ant);
 				// Tick
 				System.out.println("<START>");
 				c9ant.handleTick();
 				System.out.println("<END>");
 				break;
 
 			case 10:
 				timer.stopTimer();
 				break;
 
 			case 11:
 				timer.startTimer();
 				break;
 
 			case 12:
 				try {
 					System.out.println("Kérem adjon meg egy nehézségi szintet (1-3):");
 					BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
 					game.setDifficulty(Integer.parseInt(br.readLine()));
 				} catch (NumberFormatException e) {
 					e.printStackTrace();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 				break;
 
 			case 13:
 				try {
 					BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
 					int score;
 					score = Integer.parseInt(br.readLine());
 					game.writeTopList(score);
 				} catch (NumberFormatException e) {
 					e.printStackTrace();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 				break;
 
 			case 14:
 				System.exit(0);
 				break;
 
 			default:
 				break;
 			}
 
 		}
 
 	}
 }
