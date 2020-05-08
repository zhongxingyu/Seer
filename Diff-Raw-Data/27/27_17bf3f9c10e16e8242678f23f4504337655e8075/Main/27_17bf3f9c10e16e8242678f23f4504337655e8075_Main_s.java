 ﻿package hu.miracle.workers;
 
 import java.awt.Point;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.Random;
 
 public class Main {
 
 	public static void main(String[] args) {
 		Random random = new Random();
 		CallLogger logger = CallLogger.getLogger();
 		logger.disable();
 
 		while (true) {
 
 			Scene scene = new Scene();
 			Timer timer = new Timer(1000);
 			Game game = new Game(scene, timer);
 
 			// Inicializálás
 			Point position = new Point(0, 0);
 			AntHill hill = new AntHill(position, scene, 0, 0);
 			FoodStorage foodstore = new FoodStorage(position, 1, 1);
 			Poison poison = new Poison(position);
 			AntSinker sinker = new AntSinker(position);
 			Ant ant = new Ant(position, scene, hill);
			AntEater anteater = new AntEater(position, scene);
 
 			// Választott menüpont lekérdezése
 			int choice = showMenu();
 
 			if (choice != 14) {
 				System.out.println();
 				logger.enable();
 				logger.entering(null, "main");
 				logger.disable();
 			}
 
 			// Választástól függően az egyes szituációk lejátszása
 			switch (choice) {
 			case 1:
 				/* Hangya mozgása */
 				// Szituáció inicializálása
 				scene.getAnts().add(ant);
 				// Tick
 				logger.enable();
 				ant.handleTick();
 				logger.disable();
 				break;
 
 			case 2:
 				/* Hangya ételfelvétele */
 				// Szituáció inicializálása
 				scene.getStorages().add(foodstore);
 				scene.getAnts().add(ant);
 				// Tick
 				logger.enable();
 				ant.handleTick();
 				logger.disable();
 				break;
 
 			case 3:
 				/* Hangya méreg általi pusztulása */
 				// Szituáció inicializálása
 				scene.getAnts().add(ant);
 				scene.getObstacles().add(poison);
 				// Tick
 				logger.enable();
 				ant.handleTick();
 				scene.getObstacles().remove(poison);
 				ant.handleTick();
 				ant.handleTick();
 				ant.handleTick();
 				logger.disable();
 				break;
 
 			case 4:
 				/* Hangyászsün mozgása */
 				// Szituáció inicializálása
 				scene.getCreatures().add(anteater);
 				// Tick
 				logger.enable();
 				anteater.handleTick();
 				logger.disable();
 				break;
 
 			case 5:
 				/* Hangyaírtó spray fújása */
 				// Akció
 				logger.enable();
 				scene.placeObstacle(poison);
 				logger.disable();
 				break;
 
 			case 6:
 				/* Szagtalanító spray fújása */
 				// Akció
 				logger.enable();
 				scene.clearEffects(position);
 				logger.disable();
 				break;
 
 			case 7:
 				/* Időzítő tick */
 				// Szituáció inicializálása
 				scene.getAnts().add(new Ant(new Point(30, 30), scene, hill));
 				scene.getCreatures().add(anteater);
 				scene.getStorages().add(hill);
 				scene.getStorages().add(foodstore);
 				scene.getObstacles().add(poison);
 				// Tick
 				logger.enable();
 				timer.tick();
 				logger.disable();
 				break;
 
 			case 8:
 				/* Hangya akadályelkerülése */
 				// Szituáció inicializálása
 				scene.getAnts().add(ant);
 				scene.getObstacles().add(new Obstacle(position, null, 10, true, true));
 				// Tick
 				logger.enable();
 				ant.handleTick();
 				logger.disable();
 				break;
 
 			case 9:
 				/* Hangya hangyalesőbe lépése */
 				// Szituáció inicializálása
 				scene.getAnts().add(ant);
 				scene.getObstacles().add(sinker);
 				// Tick
 				logger.enable();
 				ant.handleTick();
 				logger.disable();
 				break;
 
 			case 10:
 				/* Játék szüneteltetése */
 				// Akció
 				logger.enable();
 				timer.stop();
 				logger.disable();
 				break;
 
 			case 11:
 				/* Játék folytatása */
 				// Akció
 				logger.enable();
 				timer.start();
 				logger.disable();
 				break;
 
 			case 12:
 				/* Játék nehézségének beállítása */
 				// Akció
 				logger.enable();
 				game.setDifficulty(random.nextInt(3) + 1);
 				logger.disable();
 				break;
 
 			case 13:
 				/* Toplistára kerülés */
 				// Akció
 				logger.enable();
 				game.addHighscore("User", random.nextInt(1000) + 1);
 				logger.disable();
 				break;
 
 			case 14:
 				/* Kilépés */
 				System.exit(0);
 				break;
 
 			default:
 				break;
 			}
 
 			CallLogger.getLogger().finishing();
 			try {
 				System.out.println("\nNyomjon <Enter>-t a folytatashoz...");
 				BufferedReader bfread = new BufferedReader(new InputStreamReader(System.in));
 				bfread.readLine();
 			} catch (IOException e) {
 			}
 		}
 
 	}
 
 	public static int showMenu() {
 		int result = 0;
 		boolean success = false;
 		// Menüpontok
 		String menu[] = { "Hangya mozgasa", "Hangya etelfelvetele",
 				"Hangya mereg altali pusztulasa", "Hangyaszsun mozgasa", "Hangyairto spray fujasa",
 				"Szagtalanito spray fujasa", "Idozito tick", "Hangya akadalyelkerulese",
 				"Hangya hangyalesobe lepese", "Jatek szuneteltetese", "Jatek folytatasa",
 				"Jatek nehezsegenek beallitasa", "Toplistara kerules", "Kilepes" };
 		BufferedReader bfread = new BufferedReader(new InputStreamReader(System.in));
 
 		// Menü kiírása
 		System.out.println("\nSkeleton - MiracleWorkers (c) 2013\nValasszon az alabbi menupontok kozul:\n");
 		for (int i = 0; i < menu.length; i++) {
 			String scenario = menu[i];
 			System.out.println(String.format("%2d. %s", i + 1, scenario));
 		}
 
 		// Amíg nincs sikeres választás
 		while (!success) {
 			try {
 				System.out.print("\nValasztott menupont: ");
 				result = Integer.parseInt(bfread.readLine().trim());
 				// Érvényesség ellenőrzése
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
 
 		// Választás visszaadása
 		return result;
 	}
 
 }
