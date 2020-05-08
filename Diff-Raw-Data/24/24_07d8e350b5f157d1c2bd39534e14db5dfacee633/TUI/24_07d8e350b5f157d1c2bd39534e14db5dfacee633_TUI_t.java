 package view;
 
 import java.util.Scanner;
 
 import controller.*;
 
 import model.Field.state;
 
 public final class TUI {
 
 	private static final int HEX = 65;
 	private static final int LINELEN = 9;
	private static final int THREE = 3;
 	private static final int MAXFIELDSIZE = 26;
 	private static final int WAIT = 1000;
 	private static final String SEP = " | ";
 
 	private static Controller c;
 	private static int fieldsize;
 	private static StringBuilder sb = new StringBuilder();
	private static Scanner scanner = new Scanner(System.in);
 
 	private static int nextX, nextY, nextbool;
 
 	private TUI() {
 	}
 
 	public static void main(final String[] args) throws InterruptedException {
 		print("Willkommen zu Battleship!!!! \n\nInitialisieren Sie zunchst die Feldgre: ");
 		do {
 			fieldsize = scanner.nextInt();
 			if (fieldsize > MAXFIELDSIZE || fieldsize < 1) {
 				print("Die Feldgre muss zwischen 1 & 26 liegen!!!!");
 				continue;
 			}
 			break;
 		} while (true);
 
 		c = new Controller(fieldsize);
 
 		if (c.getBot().vertical()) {
 			c.setBotRowboat(true, false);
 		} else {
 			c.setBotRowboat(false, true);
 		}
 
 		if (c.getBot().vertical()) {
 			c.setBotDestructor(true, false);
 		} else {
 			c.setBotDestructor(false, true);
 		}
 
 		if (c.getBot().vertical()) {
 			c.setBotFlattop(true, false);
 		} else {
 			c.setBotFlattop(false, true);
 		}
 		print("Setzen Sie ihre Schiffe!!");
 		setRowboat();
 		setDestructor();
 		setFlattop();
 		print("Dein Feld sieht aus wie folgt:");
 		print(showField().toString());
 		menu();
 	}
 
 	public static void setFieldsize(int x) {
 		TUI.fieldsize = x;
 	}
 
 	public static void setRowboat() {
 		print("");
 		print("Bitte das Ruderboot setzen: ([X/Y])");
 		nextX = scanner.nextInt();
 		nextY = scanner.nextInt();
 		print("Das Ruderboot vertikal (1) oder horizontal (2) setzen?");
 		while (true) {
 			nextbool = scanner.nextInt();
 			if (nextbool == 1) {
 				c.setHumanRowboat(nextX, nextY, true, false);
 				break;
 			} else if (nextbool == 2) {
 				c.setHumanRowboat(nextX, nextY, false, true);
 				break;
 			} else {
 				print("Falsche Eingabe! Bitte 1 (vertikal) oder 2 (horizontal) eingeben!");
 				continue;
 			}
 		}
 	}
 
 	private static void setFlattop() {
 		print("");
 		print("Bitten den Flugzeugtrger setzen: ([X/Y])");
 		nextX = scanner.nextInt();
 		nextY = scanner.nextInt();
 		print("Den Flugzeugtrger vertikal (1) oder horizontal (2) setzen?");
 		while (true) {
 			nextbool = scanner.nextInt();
 			if (nextbool == 1) {
 				c.setHumanFlattop(nextX, nextY, true, false);
 				break;
 			} else if (nextbool == 2) {
 				c.setHumanFlattop(nextX, nextY, false, true);
 				break;
 			} else {
 				print("Falsche Eingabe! Bitte 1 (vertikal) oder 2 (horizontal) eingeben!");
 				continue;
 			}
 		}
 	}
 
 	private static void setDestructor() {
 		print("");
 		print("Bitte den Zerstrer setzen: ([X/Y])");
 		nextX = scanner.nextInt();
 		nextY = scanner.nextInt();
 		print("Den Zerstrer vertikal (1) oder horizontal (2) setzen?");
 		while (true) {
 			nextbool = scanner.nextInt();
 			if (nextbool == 1) {
 				c.setHumanDestructor(nextX, nextY, true, false);
 				break;
 			} else if (nextbool == 2) {
 				c.setHumanDestructor(nextX, nextY, false, true);
 				break;
 			} else {
 				print("Falsche Eingabe! Bitte 1 (vertikal) oder 2 (horizontal) eingeben!");
 				continue;
 			}
 		}
 	}
 
 	public static StringBuilder showBotField() {
 
 		sb.setLength(0);
 
 		sb.append("##### Spielfeld des Bots #####").append("\n");
 		for (int i = 0; i < fieldsize; i++) {
 			if (i == 0) {
 				sb.append(" ");
 				for (int k = HEX; k < fieldsize + HEX; k++) {
 					sb.append(SEP).append((char) k);
 				}
 				sb.append("\n");
 			}
 			if (i <= LINELEN) {
 				sb.append(i).append(SEP);
 			} else {
 				sb.append(i).append("| ");
 			}
 			for (int j = 0; j < fieldsize; j++) {
 				if (c.getBot().getPlayboard().getField()[i][j].getStat() == state.empty
 						|| c.getBot().getPlayboard().getField()[i][j].getStat() == state.ship) {
 					sb.append("_ | ");
 				} else if (c.getBot().getPlayboard().getField()[i][j].getStat() == state.emptyhit) {
 					sb.append("O | ");
 				} else if (c.getBot().getPlayboard().getField()[i][j].getStat() == state.hit) {
 					sb.append("X | ");
 				}
 			}
 			sb.append("\n");
 		}
 		return sb;
 	}
 
 	public static StringBuilder cheatShowBotField() {
 		sb.setLength(0);
 
 		sb.append("##### Spielfeld des Bots (mit Schiffen) #####").append("\n");
 		for (int i = 0; i < fieldsize; i++) {
 			if (i == 0) {
 				sb.append(" ");
 				for (int k = HEX; k < fieldsize + HEX; k++) {
 					sb.append(SEP).append((char) k);
 				}
 				sb.append("\n");
 			}
 			if (i <= LINELEN) {
 				sb.append(i).append(SEP);
 			} else {
 				sb.append(i).append("| ");
 			}
 			for (int j = 0; j < fieldsize; j++) {
 				if (c.getBot().getPlayboard().getField()[i][j].getStat() == state.empty) {
 					sb.append("_ | ");
 				} else if (c.getBot().getPlayboard().getField()[i][j].getStat() == state.emptyhit) {
 					sb.append("O | ");
 				} else if (c.getBot().getPlayboard().getField()[i][j].getStat() == state.hit) {
 					sb.append("X | ");
 				} else if (c.getBot().getPlayboard().getField()[i][j].getStat() == state.ship) {
 					sb.append("S | ");
 				}
 			}
 			sb.append("\n");
 		}
 		return sb;
 	}
 
 	public static StringBuilder showField() {
 		sb.setLength(0);
 
 		for (int i = 0; i < fieldsize; i++) {
 			if (i == 0) {
 				sb.append(" ");
 				for (int k = HEX; k < fieldsize + HEX; k++) {
 					sb.append(SEP).append((char) k);
 				}
 				sb.append("\n");
 			}
 			if (i <= LINELEN) {
 				sb.append(i).append(SEP);
 			} else {
 				sb.append(i).append("| ");
 			}
 			for (int j = 0; j < fieldsize; j++) {
 				if (c.getPlayer().getPlayboard().getField()[i][j].getStat() == state.empty) {
 					sb.append("~ | ");
 				} else if (c.getPlayer().getPlayboard().getField()[i][j]
 						.getStat() == state.emptyhit) {
 					sb.append("O | ");
 				} else if (c.getPlayer().getPlayboard().getField()[i][j]
 						.getStat() == state.hit) {
 					sb.append("X | ");
 				} else if (c.getPlayer().getPlayboard().getField()[i][j]
 						.getStat() == state.ship) {
 					sb.append("S | ");
 				}
 			}
 			sb.append("\n");
 		}
 		return sb;
 	}
 
 	private static void menu() throws InterruptedException {
 		while (c.getPlayer().getNumberShips() > 0
 				&& c.getBot().getNumberShips() > 0) {
 			print("Du bist dran!!!!!!!!!!!!!! KNALL IHN AB MAAAAAAAAAAAAAAAAAAN");
 			print("Deine Optionen im Spiel sind:");
 			print("(1) EIGENES FELD ANZEIGEN");
 			print("(2) AUF FELD DES COMPUTERS SCHIEEN");
 			print("(3) SPIEL BEENDEN");
 
 			switch (scanner.nextInt()) {
 			case 1:
 				print(showField().toString());
 				break;
 			case 2:
 				print("Nenne die Position auf die geschossen werden soll: ([X/Y])");
 				int x = scanner.nextInt();
 				int y = scanner.nextInt();
 				c.shootBot(y, x);
 				if (c.getBot().getPlayboard().getField()[x][y].getStat() == state.hit) {
 					print("TREFFER!!!!!!!!!");
 				} else {
 					print("Nichts getroffen");
 				}
 				print(showBotField().toString());
 				if (c.getBot().getNumberShips() == 0) {
 					print("Glckwunsch!! Du hast gewonnen!!!!");
 					break;
 				}
 				print("Bot ist am Zug!");
 				Thread.sleep(WAIT);
 				c.shootHuman();
 				if (c.getPlayer().getNumberShips() == 0) {
 					print("Hasch verkackt wa!!!!");
 					break;
 				}
 				break;
			case THREE:
 				print("Vielen Dank fr's Spielen! Bis bald!");
 				System.exit(0);
 			default:
 				print(cheatShowBotField().toString());
 				break;
 			}
 		}
 	}
 
 	private static void print(String string) {
 		System.out.println(string);
 	}
 }
