 package org.jpacman.test.framework.model;
 
 import static org.junit.Assert.assertEquals;
 import static org.mockito.Mockito.mock;
 
 import org.jpacman.framework.factory.FactoryException;
 import org.jpacman.framework.factory.IGameFactory;
 import org.jpacman.framework.factory.MapParser;
 import org.jpacman.framework.model.Board;
 import org.jpacman.framework.model.Food;
 import org.jpacman.framework.model.Game;
 import org.jpacman.framework.model.Ghost;
 import org.jpacman.framework.model.IBoardInspector.SpriteType;
 import org.jpacman.framework.model.Player;
 import org.jpacman.framework.model.Wall;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Mockito;
 
 /**
  * test for the factory.MapParser, to run with a mocked Factory.
  **/
 public class MapParserTest {
 
 	private static final int PLAYER_Y = 0;
 	private static final int PLAYER_X = 0;
 	private static final int GHOST_Y = 0;
 	private static final int GHOST_X = 1;
 	private static final int FOOD_Y = 0;
 	private static final int FOOD_X = 2;
 	private static final int EMPTY_Y = 0;
 	private static final int EMPTY_X = 4;
 	private static final int WALL_Y = 0;
 	private static final int WALL_X = 3;
 	private IGameFactory f = mock(IGameFactory.class);
 	private MapParser mp;
 	private Game game;
 	private Player p;
 	private Wall w;
 	private Ghost g;
 	private Food nom;
 
 	/**
 	 * Here I initialize the map on which I want to test the nice weather
 	 * behavior.
 	 */
 	private String[] map = { "PG.# " };
 	private int width = map[0].length();
 	private int height = map.length;
 	private Board board;
 
 	/**
 	 * @Before Everything is initialized here, and all the things our mocked
 	 *         Factory should are redirected to return the Objects we made
 	 *         ourselves.
 	 */
 	@Before
 	public void setup() {
 		mp = new MapParser(f);
 		game = new Game();
 		board = new Board(width, height);
 		game.setBoard(board);
 		p = new Player();
 		w = new Wall();
 		g = new Ghost();
 		nom = new Food();
 		Mockito.when(f.makeGame()).thenReturn(game);
 		Mockito.when(f.makeBoard(width, height)).thenReturn(board);
 		Mockito.when(f.makePlayer()).thenReturn(p);
 		Mockito.when(f.makeWall()).thenReturn(w);
 		Mockito.when(f.makeGhost()).thenReturn(g);
 		Mockito.when(f.makeFood()).thenReturn(nom);
 	}
 
 	// Following are nice weather tests.
 
 	/**
 	 * @Test Test whether the map is the expected height
 	 */
 	@Test
 	public void testHeight() {
 		assertEquals(map.length, board.getHeight());
 	}
 
 	/**
 	 * @Test Test whether the map is the expected width
 	 */
 	@Test
 	public void testWidht() {
 		assertEquals(map[0].length(), board.getWidth());
 	}
 
 	/**
 	 * @Test Here we'll test whether the map parses nicely, one for one testing
 	 *       whether every sprite type gets added correctly. Here we test for
 	 *       the 'PLAYER' sprite.
 	 * @throws FactoryException
	 *             if input was in wrong format.
 	 */
 	@Test
 	public void testAddPlayer() throws FactoryException {
 		Game test = mp.parseMap(map);
 
 		assertEquals(SpriteType.PLAYER,
 				test.getBoard().tileAt(PLAYER_X, PLAYER_Y).topSprite()
 						.getSpriteType());
 	}
 
 	/**
 	 * @Test Here we'll test whether the map parses nicely, one for one testing
 	 *       whether every sprite type gets added correctly. Here we test for
 	 *       the 'GHOST' sprite.
 	 * @throws FactoryException
	 *             if input was in wrong format.
 	 */
 	@Test
 	public void testAddGhost() throws FactoryException {
 		Game test = mp.parseMap(map);
 
 		assertEquals(SpriteType.GHOST, test.getBoard().tileAt(GHOST_X, GHOST_Y)
 				.topSprite().getSpriteType());
 	}
 
 	/**
 	 * @Test Here we'll test whether the map parses nicely, one for one testing
 	 *       whether every sprite type gets added correctly. Here we test for
 	 *       the 'FOOD' sprite.
 	 * @throws FactoryException
	 *             if input was in wrong format.
 	 */
 	@Test
 	public void testAddFood() throws FactoryException {
 		Game test = mp.parseMap(map);
 
 		assertEquals(SpriteType.FOOD, test.getBoard().tileAt(FOOD_X, FOOD_Y)
 				.topSprite().getSpriteType());
 	}
 
 	/**
 	 * @Test Here we'll test whether the map parses nicely, one for one testing
 	 *       whether every sprite type gets added correctly. Here we test for
 	 *       the 'WALL' sprite.
 	 * @throws FactoryException
	 *             if input was in wrong format.
 	 */
 	@Test
 	public void testAddWall() throws FactoryException {
 		Game test = mp.parseMap(map);
 
 		assertEquals(SpriteType.WALL, test.getBoard().tileAt(WALL_X, WALL_Y)
 				.topSprite().getSpriteType());
 	}
 
 	/**
 	 * @Test Here we'll test whether the map parses nicely, one for one testing
 	 *       whether every sprite type gets added correctly. Here we test if a
 	 *       tile truly stays empty when we try to add ' ', a.k.a. nothing.
 	 * @throws FactoryException
	 *             if input was in wrong format.
 	 */
 	@Test
 	public void testAddEmpty() throws FactoryException {
 		Game test = mp.parseMap(map);
 
 		assertEquals(null, test.getBoard().tileAt(EMPTY_X, EMPTY_Y).topSprite());
 	}
 
 	// Now following: the bad weather behavior tests!
 
 	/**
 	 * @Test Test whether the Parser throws the right exception when we try to
 	 *       initialize an empty map.
 	 * @throws FactoryException
	 *             if input was in wrong format.
 	 */
 	@Test(expected = FactoryException.class)
 	public void badMapTest() throws FactoryException {
 		String[] badMap = { "" };
 		mp.parseMap(badMap);
 	}
 
 	/**
 	 * @Test Test whether the Parser throws the right exception when we try to
 	 *       initialize a map with an illegal spriteCode (in this case, 'S').
 	 * @throws FactoryException
	 *             if input was in wrong format.
 	 */
 	@Test(expected = FactoryException.class)
 	public void badCharTest() throws FactoryException {
 		String[] badMap = { "S" };
 		mp.parseMap(badMap);
 	}
 }
