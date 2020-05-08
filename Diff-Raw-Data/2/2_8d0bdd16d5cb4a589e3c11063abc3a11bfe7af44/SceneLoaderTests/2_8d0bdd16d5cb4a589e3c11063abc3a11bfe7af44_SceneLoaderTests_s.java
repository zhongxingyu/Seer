 package content;
 
 import static org.junit.Assert.*;
 
 import gameMap.CollisionLayer;
 import gameMap.Scene;
 import gameMap.Tile;
 import gameMap.TileLayer;
 import graphics.Texture2D;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.lwjgl.opengl.Display;
 
 import utils.Rectangle;
 
 public class SceneLoaderTests {
 	
 	
 	@BeforeClass
 	public static void setUpBeforeClass() throws Exception {
 		Display.create();
 	}
 
 	@AfterClass
 	public static void tearDownAfterClass() throws Exception {
 		Display.destroy();
 	}
 
 	private Scene loadScene(String sceneAsset) throws Exception {
 		InputStream stream = this.getClass().getResourceAsStream(sceneAsset);
 		SceneLoader loader = new SceneLoader();
 		return loader.loadContent(stream);
 	}
 	
 	
 	@Test
 	public void testIfCanLoadValidMap() throws Exception {
 		Scene scene = loadScene("SimpleScene.xml");
 		
 		assertNotNull(scene);
 	}
 	
 	@Test
 	public void testIfLoadedSceneHasCorrectDimentions() throws Exception {
 		Scene scene = loadScene("SimpleScene.xml");	
 		assertEquals(32, scene.getBlockSize());
 		Rectangle gridBounds = scene.getGridBounds();
 		assertEquals(3, gridBounds.Width);
 		assertEquals(3, gridBounds.Height);
 	}
 	
 	@Test
 	public void testIfCollisionLayerIsCorrect() throws Exception {
 		Scene scene = loadScene("SimpleScene.xml");
 		CollisionLayer layer = scene.getCollisionLayers().get(0);
 		boolean[][] colGrid = layer.getCollisionGrid();
 		int i = 0;
 		for (int row = 0; row < colGrid.length; row++) {
 			for (int column = 0; column < colGrid[0].length; column++) {
 				if(i % 2 == 0) {
 					assertTrue(colGrid[row][column]);
 				} else {
 					assertFalse(colGrid[row][column]);
 				}			
 				i++;
 			}
 		}
 		
 	}
 	
 	@Test(expected = Exception.class)
 	public void testIfCrashesOnInvalidScene() throws Exception {
 		Scene scene = loadScene("BrokenScene.xml");
 		fail("It should not get here!");
 	}
 
 	@Test
 	public void testIfTileLayerIsCorrect() throws Exception {
 		Scene scene = loadScene("SimpleScene.xml");
 		TileLayer layer = scene.getTileLayers().get(0);
 		assertEquals(0, layer.getDepth());
 	
 		Tile[][] tiles = layer.getTileGrid();
 		
 		//These values are from the Simple map
 		Texture2D textureA = tiles[0][0].getTexture();
 		Texture2D textureB = tiles[2][0].getTexture();
 	
 		assertNotSame(textureA, textureB);
 		
 		Rectangle sorceRectA = tiles[1][1].getSourceRect();
 		Rectangle sorceRectB = tiles[0][1].getSourceRect();
 		
 		assertNotSame(sorceRectA, sorceRectB);	
 	}
 
 
 	@Test
	public void testIfCorectFoulderAndClassManaged() throws Exception {
 		SceneLoader loader = new SceneLoader();
 		
 		assertSame(Scene.class, loader.getClassAbleToLoad());
 		assertSame("scenes", loader.getFolder());
 	}
 	
 	
 }
