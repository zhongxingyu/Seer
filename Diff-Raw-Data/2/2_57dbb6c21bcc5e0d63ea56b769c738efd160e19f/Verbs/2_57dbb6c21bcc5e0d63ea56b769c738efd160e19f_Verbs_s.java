 import java.util.Scanner;
 import java.util.Vector;
 
 public class Verbs{
 
 	static final String EOL = System.getProperty("line.separator");
 	
 	/**
 	* Prints whitespace, finds the verb.
 	*/
 	public static void parse(String input)
 	{
 		if (!input.equals("")) {
 			Scanner scan = new Scanner(input.toLowerCase());
 			String i = scan.next(); //first word
 			Vector<String> words = new Vector<String>(); //remaining words
 			if(!scan.hasNext()){words.add("");}
 			else{while(scan.hasNext()){words.add(scan.next());}}
 			scan.close();
 			System.out.println(EOL);
 
 			//The following act on the verb, passing arguments if necessary
 			//The string 'words' contains any arguments,
 			if (i.equals("north") || i.equals("n")) {north();}
 			else if (i.equals("east") || i.equals("e")) {east();}
 			else if (i.equals("south") || i.equals("s")) {south();}
 			else if (i.equals("west") || i.equals("w")) {west();}
 			else if (i.equals("up") || i.equals("u")) {up();}
 			else if (i.equals("down") || i.equals("d")) {down();}
 			else if (i.equals("use")) {use(words);}
 			else if (i.equals("talk")) {talk(words);}
 			else if (i.equals("examine") || i.equals("x")) {examine(words);}
 			else if (i.equals("look") || i.equals("l")) {look();}
 			else if (i.equals("inventory") || i.equals("i")) {inventory();}
 			else if (i.equals("exit") || i.equals("quit")) {System.exit(0);}
 			else if (i.equals("help") || i.equals("?")) {help();}
 			else if (i.equals("save") || i.equals("save game")){save();}
 			else if (i.equals("load") || i.equals("load game")){load();}
 			else {nope();}
 		}
 	}
 	
 	private static void north()
 	{
 		if(World.getRoom(Exe.getX(), Exe.getY(), Exe.getZ()).checkExit(0)){
 			int z = Exe.getZ();
 			Exe.setZ(z+1);
 			look();
 		}
 		else{
 			nope();
 		}
 	}
 
 	private static void east()
 	{
 		if(World.getRoom(Exe.getX(), Exe.getY(), Exe.getZ()).checkExit(2)){
 			int x = Exe.getX();
 			Exe.setX(x+1);
 			look();
 		}
 		else{
 			nope();
 		}
 	}
 
 	private static void south()
 	{
 		if(World.getRoom(Exe.getX(), Exe.getY(), Exe.getZ()).checkExit(1)){
 			int z = Exe.getZ();
 			Exe.setZ(z-1);
 			look();
 		}
 		else{
 			nope();
 		}
 	}
 
 	private static void west()
 	{
 		if(World.getRoom(Exe.getX(), Exe.getY(), Exe.getZ()).checkExit(3)){
 			int x = Exe.getX();
 			Exe.setX(x-1);
 			look();
 		}
 		else{
 			nope();
 		}
 	}
 
 	private static void up()
 	{
 		if(World.getRoom(Exe.getX(), Exe.getY(), Exe.getZ()).checkExit(4)){
 			int y = Exe.getY();
 			Exe.setY(y+1);
 			look();
 		}
 		else{
 			nope();
 		}
 	}
 
 	private static void down()
 	{
 		if(World.getRoom(Exe.getX(), Exe.getY(), Exe.getZ()).checkExit(5)){
 			int y = Exe.getY();
 			Exe.setY(y-1);
 			look();
 		}
 		else{
 			nope();
 		}
 	}
 
 	private static void use(Vector<String> i)
 	{
 		try
 		{
 			System.out.println(Player.getInv().getItem(i.firstElement()).getUseEffect());
 		}
 		catch (IllegalArgumentException ex) {}
 		try
 		{
 			System.out.println(World.getRoom(Exe.getX(),Exe.getY(),Exe.getZ()).getItems().getItem(i.firstElement()).getUseEffect());
 		}
 		catch (IllegalArgumentException ex)
 		{
 			nope();
 		} 
 	}
 
 	private static void talk(Vector<String> i)
 	{
 		try {
 			if(Player.getInv().getItem(i.firstElement()).getCanTalk()) {
 				Player.getInv().getItem(i.firstElement()).talk();
 			}
 			else {
 				System.out.println("No response...");
 			}
 		}
 		catch (IllegalArgumentException ex) {}
 		try {
 			if(World.getRoom(Exe.getX(),Exe.getY(),Exe.getZ()).getItems().getItem(i.firstElement()).getCanTalk()) {
 				World.getRoom(Exe.getX(),Exe.getY(),Exe.getZ()).getItems().getItem(i.firstElement()).talk();
 			}
 			else {
 				System.out.println("No response...");
 			}
 		}
 		catch (IllegalArgumentException ex)
 		{
 			nope();
 		}
 	}
 
 	private static void examine(Vector<String> i)
 	{
 		try
 		{
 			System.out.println(Player.getInv().getItem(i.firstElement()).getDescription());
 		}
 		catch (IllegalArgumentException ex) {}
 		try
 		{
 			System.out.println(World.getRoom(Exe.getX(),Exe.getY(),Exe.getZ()).getItems().getItem(i.firstElement()).getDescription());
 		}
 		catch (IllegalArgumentException ex)
 		{
 			nope();
 		} 
 	}
 
 	private static void look(){
 		World.getRoom(Exe.getX(),Exe.getY(),Exe.getZ()).printRoom();
 	}
 
 	private static void inventory(){
 		Player.getInv().printItems();
 	}
 
 	private static void help(){
 		System.out.println(
 			"Here's what you can do: (type indicated letter or full word)" + EOL +
 			"You can go (n)orth, (s)outh, (e)ast, or (w)est." + EOL +
 			"Sometimes, you can even go (u)p or (d)own." + EOL +
			"You can (use) an item, or (e)xamine one in your (i)nventory." + EOL +
 			"You can always take a (l)ook around, and if all else fails," + EOL +
 			"you can always (quit) and never, ever, ever come back.");
 	}
 
 	private static void save(){
 		Exe.saveFile();
 	}
 
 	private static void load(){
 		Exe.readSaveFile("adventure_save.txt");
 	}
 
 	public static void nope(){
 		System.out.println("I can't do that, Dave.");
 	}
 	
 }
