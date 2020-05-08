 package com.zdonnell.geneticcars;
 
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.BodyDef;
 import com.badlogic.gdx.physics.box2d.FixtureDef;
 import com.badlogic.gdx.physics.box2d.PolygonShape;
 import com.badlogic.gdx.physics.box2d.World;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * This class houses the terrain generation methods.  There is no need to instantiate it
  * as all the generation methods are static.
  *
  * @author Zach
  */
 public class TerrainGenerator {
 
 	/**
 	 * The width (in box2d units/meters) of a piece of the ground
 	 */
 	private static final float GROUND_PIECE_WIDTH = 1.5f;
 
 	/**
 	 * The height (in box2d units/meters) of a piece of the ground
 	 */
 	private static final float GROUND_PIECE_HEIGHT = 0.15f;
 
 	/**
 	 * The total number of ground tiles/pieces
 	 */
 	private static final int MAX_GROUND_PIECES = 200;
 
 	/**
 	 * Generates terrain in the provided world.
 	 *
 	 * @param world the world to place the terrain in
 	 * @return a list of all the terrain tiles generated
 	 */
 	public static List<Body> generate(World world) {
 		List<Body> terrainTiles = new ArrayList<Body>(MAX_GROUND_PIECES);
 		Body lastTile;
 		Vector2 tilePosition = new Vector2(-5, -2);
 		for (int i = 0; i < MAX_GROUND_PIECES; i++) {
 			// Create a tile, it's rotation potentially more extreme the closer to the end of the terrain we get.
 			lastTile = createTerrainTile(world, tilePosition, (Math.random() * 3 - 1.5) * 1.5 * i / MAX_GROUND_PIECES);
 			terrainTiles.add(lastTile);
 			PolygonShape lastTileShape = (PolygonShape) lastTile.getFixtureList().get(0).getShape();
 			lastTileShape.getVertex(3, tilePosition);
 			tilePosition = lastTile.getWorldPoint(tilePosition);
 		}
 		return terrainTiles;
 	}
 
 	/**
 	 * Creates a specific tile for the terrain
 	 *
 	 * @param world    the world that the tile is to be placed in
 	 * @param position the position at which the tile should be started at
 	 * @param angle    the angle to rotate the tile by
 	 * @return the assembled physics body for the terrain tile
 	 */
 	private static Body createTerrainTile(World world, Vector2 position, double angle) {
 		// Create the physics body for the tile
 		BodyDef tileBodyDef = new BodyDef();
 		tileBodyDef.position.set(position);
 		Body tileBody = world.createBody(tileBodyDef);
 
 		FixtureDef tileFixDef = new FixtureDef();
 
 		// Create the vertices for shape of the tile
 		Vector2[] tileVertices = new Vector2[4];
 		tileVertices[0] = new Vector2(0, 0);
 		tileVertices[1] = new Vector2(0, GROUND_PIECE_HEIGHT);
 		tileVertices[2] = new Vector2(GROUND_PIECE_WIDTH, GROUND_PIECE_HEIGHT);
 		tileVertices[3] = new Vector2(GROUND_PIECE_WIDTH, 0);
 
 		PolygonShape tileShape = new PolygonShape();
 		tileShape.set(rotateTileVertices(tileVertices, angle));
 		tileFixDef.shape = tileShape;
 		tileFixDef.friction = 0.5f;
 
 		tileBody.createFixture(tileFixDef);
 
 		return tileBody;
 	}
 
 	/**
 	 * Rotates a set of vertices of a horizontal tile by the angle specified around
 	 * the center (0, 0) coordinate.
 	 *
	 * @param vertices the set of vertices that make up a tile
	 * @param angle    the angle to rotate the tile vertices by
 	 * @return an array of the rotated vertices
 	 */
	private static Vector2[] rotateTileVertices(Vector2[] vertices, double angle) {
		Vector2 center = new Vector2(0, 0);
		Vector2[] rotatedVertices = new Vector2[vertices.length];
 
 		// Calculate the new point for each vertex based on a rotation around (0, 0)
		for (int i = 0; i < vertices.length; i++) {
 			Vector2 rotatedVertex = new Vector2();
			rotatedVertex.x = (float) (Math.cos(angle) * (vertices[i].x - center.x) - Math.sin(angle) * (vertices[i].y - center.y) + center.x);
			rotatedVertex.y = (float) (Math.sin(angle) * (vertices[i].x - center.x) - Math.cos(angle) * (vertices[i].y - center.y) + center.y);
 			rotatedVertices[i] = rotatedVertex;
 		}
 		return rotatedVertices;
 	}
 }
