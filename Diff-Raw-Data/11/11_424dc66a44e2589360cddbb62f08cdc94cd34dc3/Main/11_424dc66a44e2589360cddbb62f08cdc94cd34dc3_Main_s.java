 /**
  * initialise the rooms, characters, weapons, deals them out and starts a turn
  * //TODO Maybe the initialising rooms etc should be in board or somewhere else
  * @author byrneciar
  *
  */
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 public class Main {
 
     private Board board;
 
     private static final String[] ROOM = { "Spa", "Theatre", "Living Room",
             "Observatory", "Patio", "Kitchen", "Dining Room", "Guest House",
             "Hall" };
     private static final String[] CHARACTER = { "Kasandra Scarlett",
             "Jack Mustard", "Diane White", "Jacob Green", "Eleanor Peacock",
             "Victor Plum" };
     private static final String[] WEAPON = { "Rope", "Candlestick", "Knife",
             "Pistol", "Baseball Bat", "Dumbbell", "Trophy", "Poison", "Axe" };
 
     public enum Direction {
         NORTH, SOUTH, WEST, EAST;
     }
 
     // the solution
     private Character murderer;
     private Room murderScene;
     private Weapon murderWeapon;
 
     private Map<String, Character> characters;
     private Map<String, Weapon> weapons;
     private List<Player> players;
 
     public Main() throws NumberFormatException, IOException {
         // initialising
         initialiseWeapons();
 
         initialiseCharacters();
 
         board = new Board(new File("Cluedo Board.txt"), characters.values());
         initialiseRooms();
         initialisePlayers();
         initialiseSolution();
         startMainGameLoop();
     }
 
     private void initialiseSolution() {
         // randomise the solution
         murderer = characters.get((int) (Math.random() * characters.size()));
         murderScene = (Room) board.getListRooms().get((int) (Math
                 .random() * board.roomsSize()));
         murderWeapon = weapons.get((int) (Math.random() * weapons.size()));
     }
 
     private void initialiseCharacters() {
         // create the characters
         characters = new HashMap<String, Character>();
         for (String c : CHARACTER) {
             characters.put(c, new Character(c));
         }
     }
 
     private void initialiseWeapons() {
         // create the weapons
         weapons = new HashMap<String, Weapon>();
         for (String w : WEAPON) {
             weapons.put(w, new Weapon(w));
         }
 
     }
 
     private void initialiseRooms() {
 
         List<Weapon> temp = new ArrayList<Weapon>();
         temp.addAll(weapons.values());
 
         // randomly choose a weapon and then create a room which contains that
         // weapon
         int rand;
         for (String r : ROOM) {
             rand = (int) Math.random() * temp.size();
             board.addRoom(new Room(r, temp.get(rand), null));
             temp.remove(rand);
         }
         // add doors to rooms
         for (Tile tile : board.getDoors()) {
             board.getRoom(tile.getRoom()).addDoors("exity", tile); // TODO: Unique exit names
         }
     }
 
     public void initialisePlayers() throws NumberFormatException, IOException {
         players = new ArrayList<Player>();
 
         // ask for the number of players
 
         int numberOfPlayers = askIntegerFromCommandLine("Number of players: ",
                 2, 6);
 
         // create a new board object
         ArrayList<Card> deck = shuffleNewDeck();
 
         int cardsPerPlayer = deck.size() / numberOfPlayers;
 
         // generate players, assign them a random token and deal them there
         // share of the cards
         List<Character> availableTokens = new ArrayList<Character>();
         availableTokens.addAll(characters.values());
 
         for (int i = 0; i < numberOfPlayers; i++) {
 
             // assign a random token to the player
             Character token = availableTokens.remove((int) Math.random()
                     * availableTokens.size());
 
             // randomly deal the player's hand
             Set<Card> hand = new HashSet<Card>();
             for (int j = 0; j < cardsPerPlayer; j++)
                 hand.add(deck.remove(0));
 
             // if there's any cards left (should only be 1) give it to this
             // player
             if (deck.size() < cardsPerPlayer) {
                 hand.addAll(deck);
                 deck.clear();
             }
 
             players.add(new Player(token, hand));
         }
     }
 
     private ArrayList<Card> shuffleNewDeck() {
         ArrayList<Card> deck = new ArrayList<Card>();
         deck.addAll(board.getListRooms());
         deck.addAll(characters.values());
         deck.addAll(weapons.values());
         deck.remove(murderer);
         deck.remove(murderScene);
         deck.remove(murderWeapon);
         Collections.shuffle(deck);
         return deck;
     }
 
     public void startMainGameLoop() throws NumberFormatException, IOException {
         Player winner = null;
         int indexOfCurrentPlayer = 0;
 
         while (winner == null) {
 
             // get the next player, and find out where they are
             Player currentPlayer = players.get(indexOfCurrentPlayer);
             Character currentToken = currentPlayer.getToken();
             Occupiable startLocation = board.findCharacter(currentToken);
             // Tile finishLocation = null;
 
             System.out.println("Player is " + currentToken.getName() + " ("
                     + currentToken.getBoardToken() + ")");
 
             if (startLocation instanceof Room) {
                winner = offerRoomOptions(currentPlayer, (Room) startLocation);
             } else if (startLocation instanceof Tile) {
                 moveToken(currentToken, (Tile) startLocation);
             }
 
             indexOfCurrentPlayer++;
 
             // we've done a full cycle, so start again.
             if (indexOfCurrentPlayer == players.size())
                 indexOfCurrentPlayer = 0;
         }
         System.out.println("WE HAVE A WINNER!");
     }
 
    private Player offerRoomOptions(Player currentPlayer, Room room) throws IOException {
         List<String> options = new ArrayList<String>();
 
         options.add(""); // just to get the options starting at 1
 
         options.add("announcement");
         System.out.println(options.size() + ". Make an announcment");
 
         if (room.getPassageWay() != null) {
             options.add("secret");
             System.out.println(options.size()
                     + ". Take the secret passage way to the "
                     + room.getPassageWay().toString());
         }
 
         options.add("exit");
 
         int option = askIntegerFromCommandLine("What would you like to do: ",
                 1, options.size() - 1);
 
         if (options.get(option).equals("announcement"))
             return makeAnnouncement(currentPlayer, room);
         else if (options.get(option).equals("secret"))
             return useSecretPassage(currentPlayer, currentToken, room,
                     room.getPassageWay());
         else if (options.get(option).equals("exit"))
             exitRoom(currentToken, currentPlayer, room);
 
         return null;
     }
     
     private void exitRoom(Character currentToken, Player currentPlayer, Room room) throws NumberFormatException, IOException {
     	List<Tile> exits = new ArrayList<Tile>();
     	exits.add(null);
     	
     	System.out.println("Exits from " + room.getName());
     	
     	for (Entry<String, Tile> exit : room.getDoors().entrySet()) {
     		System.out.println(exits.size() + " . " + exit.getKey());
     		exits.add(exit.getValue());
     	}
     	
     	int exitIndex = askIntegerFromCommandLine("Which exit would you like to leave through: ", 1, exits.size() - 1);
     	board.moveCharacterFromRoomToBoard(currentToken, room, exits.get(exitIndex));
     	moveToken(currentToken, exits.get(exitIndex));
     }
 
     private Player useSecretPassage(Player currentPlayer,
             Character character, Room startLocation, String passageWay) throws IOException {
         Set<Room> rooms = board.getSetRooms();
         startLocation.removeCharacter(character);
         Room finishLocation = null;
         for (Room r : rooms) {
             if (r.getName().equals(passageWay)) {
                 r.addCharacter(character);
                 finishLocation = r;
                 break;
             }
         }
        return offerRoomOptions(currentPlayer, finishLocation);
     }
 
     private Player makeAnnouncement(Player currentPlayer,
             Room potentialMurderScene) throws IOException {
         boolean accusation = potentialMurderScene instanceof SwimmingPool ? true
                 : false;
 
         Announcement announcement = createAnnouncement(potentialMurderScene,
                 accusation);
 
         // iterate players until counter evidence found
         boolean counterEvidence = false;
         for (Player p : players) {
             if (p.checkAnnouncement(announcement)) {
                 counterEvidence = true;
                 break;
             }
         }
 
         if (announcement.isAccusation()) {
             if (counterEvidence) {
                 eliminatePlayer(currentPlayer);
             } else {
                 return currentPlayer;
             }
         }
         return null;
     }
 
     private void moveToken(Character currentToken, Tile startLocation)
             throws IOException {
 
         // roll die
         int dieRoll = 1 + (int) (Math.random() * 6);
 
         Tile currentLocation = startLocation;
 
         System.out.println("Your dice roll was: " + dieRoll);
 
         while (dieRoll > 0) {
             board.displayBoard();
             System.out.println("You have " + dieRoll + " moves left.");
             Direction toMove = askDirection("Move in which direction (N, S, E, W): ");
             int x = currentLocation.getX();
             int y = currentLocation.getY();
             if (toMove == Direction.NORTH)
                 y -= 1;
             else if (toMove == Direction.SOUTH)
                 y += 1;
             else if (toMove == Direction.WEST)
                 x -= 1;
             else if (toMove == Direction.EAST)
                 x += 1;
             if (board.validLocation(x, y)) {
                 Tile toMoveTile = board.getTile(x, y);
 
                 if (toMoveTile.getType() == '.') {
                     board.moveCharacter(currentLocation, toMoveTile);
                     currentLocation = toMoveTile;
                     dieRoll--;
                 }
                 else if (toMoveTile.getType() == 'E') {
                     Room room = board.enterRoom(currentLocation, toMoveTile);
                     for (int i =0; i<players.size(); i++) {
                         if (players.get(i).getToken() == currentToken) {
                            offerRoomOptions(players.get(i), room);
                         }
                     }
                 } else {
                     System.out.println("Invalid Move!");
                 }
             } else {
                 System.out.println("Invalid Move!");
             }
         }
 
     }
 
     private void eliminatePlayer(Player currentPlayer) {
         players.remove(currentPlayer);
     }
 
     private Announcement createAnnouncement(Room accusationRoom,
             boolean accusation) throws NumberFormatException, IOException {
         for (int i = 0; i < characters.size(); i++)
             System.out.println((i + 1) + ". " + characters.get(i));
 
         int accused = askIntegerFromCommandLine(
                 "Which character do you suspect?", 1, characters.size() + 1);
 
         int murderWeapon = askIntegerFromCommandLine(
                 "What do you think the murder weapon was?", 1,
                 weapons.size() + 1);
 
         return new Announcement(characters.get(accused),
                 weapons.get(murderWeapon), accusationRoom, accusation);
     }
 
     /**
      * Asks the user a question and gets a number from the user from the command
      * line.
      * 
      * @param question
      *            The question that we want to ask the user.
      * @return Returns the answer to the provided question as an int given by
      *         the user
      * @throws NumberFormatException
      * @throws IOException
      */
     public int askIntegerFromCommandLine(String question, int minimum,
             int maximum) throws NumberFormatException, IOException {
         System.out.print(question);
 
         BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
 
         while (true) {
             try {
                 String nextLine = br.readLine();
                 int answer = Integer.parseInt(nextLine);
                 if (answer >= minimum && answer <= maximum)
                     return Integer.parseInt(nextLine);
             } catch (Exception e) {
                 System.out.println("Invalid input, integer number required!");
                 System.out.print(question);
             }
         }
     }
 
     /**
      * Asks the user a Yes or No question
      * 
      * @param question
      *            The question that we want to ask the user.
      * @return Returns the answer to the provided question as an boolen, true is
      *         yes false is no
      * @throws NumberFormatException
      * @throws IOException
      */
     public boolean askYesNo(String question) throws NumberFormatException,
             IOException {
         System.out.print(question);
 
         BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
 
         while (true) {
             try {
                 String nextLine = br.readLine();
                 if (nextLine == "Y" || nextLine == "y" || nextLine == "Yes"
                         || nextLine == "yes") {
                     return true;
                 } else if (nextLine == "N" || nextLine == "n"
                         || nextLine == "No" || nextLine == "no") {
                     return false;
                 }
             } catch (Exception e) {
                 System.out.println("Invalid input, integer number required!");
                 System.out.print(question);
             }
         }
     }
 
     public Direction askDirection(String question)
             throws NumberFormatException, IOException {
         System.out.print(question);
 
         BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
 
         while (true) {
             try {
                 String nextLine = br.readLine();
                 if (nextLine.equalsIgnoreCase("n")
                         || nextLine.equalsIgnoreCase("north"))
                     return Direction.NORTH;
                 else if (nextLine.equalsIgnoreCase("s")
                         || nextLine.equalsIgnoreCase("south"))
                     return Direction.SOUTH;
                 else if (nextLine.equalsIgnoreCase("w")
                         || nextLine.equalsIgnoreCase("west"))
                     return Direction.WEST;
                 else if (nextLine.equalsIgnoreCase("e")
                         || nextLine.equalsIgnoreCase("east"))
                     return Direction.EAST;
                 else
                     System.out
                             .println("Invalid input, compass direction required!");
             } catch (Exception e) {
                 System.out
                         .println("Invalid input, compass direction required!");
                 System.out.print(question);
             }
         }
     }
 
     /**
      * Asks the user a question and gets a number from the user from the command
      * line.
      * 
      * @param question
      *            The question that we want to ask the user.
      * @return Returns the answer to the provided question as an int given by
      *         the user
      * @throws NumberFormatException
      * @throws IOException
      */
     public boolean askBooleanFromCommandLine(String question) {
         System.out.print(question);
 
         BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
 
         while (true) {
             try {
                 String nextLine = br.readLine();
                 return Boolean.parseBoolean(nextLine);
             } catch (Exception e) {
                 System.out.println("Invalid input, boolean required!");
                 System.out.print(question);
             }
         }
     }
 
     public static void main(String[] args) throws NumberFormatException,
             IOException {
         new Main();
     }
 
 }
