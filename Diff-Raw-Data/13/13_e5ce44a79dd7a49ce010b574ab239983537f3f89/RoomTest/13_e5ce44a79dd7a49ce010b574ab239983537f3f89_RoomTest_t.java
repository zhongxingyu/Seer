 package testSuite;
 
 import model.Room;
 import model.object.Item;
 import model.object.Monster;
 import junit.framework.TestCase;
 
 public class RoomTest extends TestCase {
 	private final static String NORTH = "north";
 	private final static String SOUTH = "south";
 	private final static String EAST = "east";
 	private final static String WEST = "west";
 	private final static String LOBBY = "Lobby";
 	private final static String GALLERY = "Gallery";
 	private final static String WORKSHOP = "Workshop";
 	
 	private Room gallery;
 	private Room workshop;
 	private Room lobby;
 	private Room other;
 	private Item bat;
 	private Monster pikachu;
 	
 	protected void setUp() throws Exception {
 		super.setUp();
 		gallery = new Room(GALLERY);
 		workshop = new Room(WORKSHOP);
 		lobby = new Room(LOBBY);
 		other = new Room(LOBBY);
 		
 		bat = new Item("Bat",15);
 		pikachu = new Monster("Pikachu", 1000);
 		
 		gallery.setExits(NORTH, workshop);
 		gallery.setExits(SOUTH, lobby);
 		lobby.setExits(NORTH, gallery);
 		lobby.addItem(bat);
 		lobby.addMonster(pikachu);
 		other.setExits(NORTH, gallery);
 		other.addItem(bat);
 		other.addMonster(pikachu);
 	}
 
 	public void testSetExits() {
 		assertEquals(lobby, gallery.getExit(SOUTH));
 		assertEquals(workshop, gallery.getExit(NORTH));
 		assertEquals(null, gallery.getExit(EAST));
 	}
 
 	public void testGetDescription() {
 		assertEquals(LOBBY, lobby.getDescription());
 		assertEquals(GALLERY, gallery.getDescription());
 		assertEquals(WORKSHOP, workshop.getDescription());
 	}
 
 	public void testGetLongDescription() {
 		assertEquals(true, gallery.getLongDescription().equals(gallery.getLongDescription()));
 		assertEquals(false, gallery.getLongDescription().equals(lobby.getLongDescription()));
 		assertEquals(true, lobby.getLongDescription().equals(lobby.getLongDescription()));
 		assertEquals(true, lobby.getLongDescription().equals(other.getLongDescription()));
 	}
 
 	public void testRemoveItem() {
 		assertEquals(true, lobby.getLongDescription().equals(other.getLongDescription()));
 		other.removeItem("Bat");
 		assertEquals(false, lobby.getLongDescription().equals(other.getLongDescription()));
 	}
 
 	public void testGetItem() {
 		assertEquals(bat, lobby.getItem("Bat"));
 	}
 
 	public void testContainsItem() {
 		assertEquals(true, lobby.containsItem("Bat"));
 		assertEquals(false, lobby.containsItem("Yo-Yo"));
 	}
 
 	public void testRemoveMonster() {
 		assertEquals(true, lobby.getLongDescription().equals(other.getLongDescription()));
 		other.removeMonster("Pikachu");
 		assertEquals(false, lobby.getLongDescription().equals(other.getLongDescription()));
 	}
 
 	public void testGetMonster() {
 		assertEquals(pikachu, lobby.getMonster("Pikachu"));
 	}
 
 	public void testHasBeenVisited() {
 		assertEquals(false, lobby.hasBeenVisited());
 		lobby.visit();
 		assertEquals(true, lobby.hasBeenVisited());
 	}
 
 }
