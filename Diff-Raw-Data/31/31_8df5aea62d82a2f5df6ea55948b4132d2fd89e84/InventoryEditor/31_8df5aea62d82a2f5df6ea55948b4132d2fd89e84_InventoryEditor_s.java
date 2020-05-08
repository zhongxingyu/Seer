 package uk.ac.aber.dcs.cs12420.aberpizza.pipeline;
 
 import java.io.*;
 
 import uk.ac.aber.dcs.cs12420.aberpizza.data.*;
 
 public class InventoryEditor {
 
	private enum State { EXIT, START, NEW, LOAD, SAVE, EDITOR,
 		HELP, LIST, EXAMINE, ADD, REMOVE,
 		ADD_PIZZA, ADD_DRINK, ADD_SIDE} ;
 	private static State state = State.START;
 	private static Inventory inventory = null;
 	private static BufferedReader input = null;
 	
 	/**
 	 * @param args
 	 * @throws IOException 
 	 */
 	public static void main(String[] args) throws IOException {
 		char c;
 		String s;
 		int i;
 		input = new BufferedReader(new InputStreamReader(System.in));
 		
 		while (true) {
 			switch (state) {
 			case START:
 				System.out.println("Inventory editor, generates an inventory.xml file.");
 				System.out.println("(1) New");
 				System.out.println("(2) Load");
 				c = input.readLine().charAt(0);
 				if (c == '1') {
 					state = State.NEW;
 				}
 				if (c == '2') {
 					state = State.LOAD;
 				}
 				continue;
 			case NEW:
 				inventory = new Inventory();
 				state = State.EDITOR;
 				continue;
 			case LOAD:
 				inventory = Inventory.load(null);
 				state = State.EDITOR;
 				continue;
 			case SAVE:
 				inventory.save(null);
 				state = State.EDITOR;
 				continue;
 			case EDITOR:
 				System.out.println("? to print commands.");
 				c = input.readLine().charAt(0);
 				switch (c) {
 				case '?':
 					state = State.HELP;
 					continue;
 				case 'l':
 				case 'L':
 					state = State.LIST;
 					continue;
 				case 'a':
 				case 'A':
 					state = State.ADD;
 					continue;
 				case 'x':
 				case 'X':
 				case 'e':
 				case 'E':
 					state = State.EXAMINE;
 					continue;
 				case 'r':
 				case 'R':
 					state = State.REMOVE;
 					continue;
 				case 's':
 				case 'S':
 					state = State.SAVE;
 					continue;
 				}
 			case HELP:
 				System.out.println("? help");
 				System.out.println("List");
 				System.out.println("Add");
 				System.out.println("EXamine");
 				System.out.println("Remove");
 				System.out.println("Save");
 				state = State.EDITOR;
 				continue;
 			case LIST:
 				for (i = 0; i < inventory.getItems().size(); i++) {
 					System.out.println("("+i+") "+inventory.getItems().get(i).toString() );
 				}
 				state = State.EDITOR;
 				continue;
 			case ADD:
 				System.out.println("(1) Add Pizza");
 				System.out.println("(2) Add Drink");
 				System.out.println("(3) Add Side");
 				System.out.println("(D) Cancel");
				s = input.readLine();
				if (s == "1") { state = State.ADD_PIZZA; }
				if (s == "2") { state = State.ADD_DRINK; }
				if (s == "3") { state = State.ADD_SIDE; }
				if (s == "D") { state = State.EDITOR; } 
 				continue;
 			case EXAMINE:
 				System.out.println("Enter item number or D to cancel.");
 				s = input.readLine();
				if (s == "D") { 
 					state = State.EDITOR; 
 					continue; 
 				}
 				i = Integer.parseInt(s);
 				System.out.println(inventory.getItems().get(i).toString() );
 				state = State.EDITOR;
 				continue;
 			case REMOVE:
 				System.out.println("Enter item number ot D to cancel.");
 				s = input.readLine();
				if (s == "D") {
 					state = State.EDITOR;
 					continue;
 				}
 				i = Integer.parseInt(s);
 				inventory.getItems().remove(i);
 				state = State.EDITOR;
 				continue;
			
 			default:
 				return;
 			
 			}
 		}
 
 	}
 
 }
