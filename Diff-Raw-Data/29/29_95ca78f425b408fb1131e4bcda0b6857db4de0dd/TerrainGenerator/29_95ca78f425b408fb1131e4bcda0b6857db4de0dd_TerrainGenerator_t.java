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
	 * @param v the set of vertices that make up a tile
	 * @param a the angle to rotate the tile vertices by (in radians)
 	 * @return an array of the rotated vertices
 	 */
	private static Vector2[] rotateTileVertices(Vector2[] v, double a) {
		Vector2 c = new Vector2(0, 0);
		Vector2[] rotatedVertices = new Vector2[v.length];
 
 		// Calculate the new point for each vertex based on a rotation around (0, 0)
		for (int i = 0; i < v.length; i++) {
 			Vector2 rotatedVertex = new Vector2();
			rotatedVertex.x = (float) ((Math.cos(a) * (v[i].x - c.x) - Math.sin(a) * (v[i].y - c.y) + c.x));
			rotatedVertex.y = (float) ((Math.sin(a) * (v[i].x - c.x) - Math.cos(a) * (v[i].y - c.y) + c.y));
 			rotatedVertices[i] = rotatedVertex;
 		}
		// If the rotation angle is greater than 45 degrees, the tile polygon becomes "inside out."  Box2d
		// doesn't like this, so we need to reverse the direction of the vertices.
		// TODO: Figure out if something is wrong with the rotation algorithm
		if (Math.abs(a) > 0.785398163) {
			Vector2 temp = rotatedVertices[3];
			rotatedVertices[3] = rotatedVertices[2];
			rotatedVertices[2] = rotatedVertices[1];
			rotatedVertices[1] = temp;
		}
 		return rotatedVertices;
 	}
 }
