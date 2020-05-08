 package org.rev317.api.methods;
 
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.util.Random;
 
 import org.parabot.core.Context;
 import org.parabot.core.parsers.hooks.HookParser;
 import org.rev317.Loader;
 import org.rev317.accessors.Client;
 import org.rev317.api.wrappers.scene.Tile;
 import org.rev317.utils.Reader;
 
 
 /**
  * 
  * Class which does all the calculations
  * 
  * @author Everel
  *
  */
 public final class Calculations {
 	
 	public static int[] SINUS = new int[2048];
 	public static int[] COSINUS = new int[2048];
 	public static final Rectangle GAME3D_SCREEN = new Rectangle(0, 0, 516, 337);
 	
 	private static int minimapBaseX;
 	private static int minimapBaseY;
 	private static final Random RANDOM = new Random();
 	
 	static {
 		for (int i = 0; i < 2048; i++) {
 			SINUS[i] = (int) (65536.0D * Math.sin(i * 0.0030679615D));
 			COSINUS[i] = (int) (65536.0D * Math.cos(i * 0.0030679615D));
 		}
 		HookParser parser = Context.resolve().getHookParser();
 		final String minimapX = parser.getConstants().get("minimapX");
 		final String minimapY = parser.getConstants().get("minimapY");
 		minimapBaseX = Integer.parseInt(minimapX);
 		minimapBaseY = Integer.parseInt(minimapY);
 	}
 	
 	/**
 	 * Convert a tile to screen
 	 * @param region x
 	 * @param region y
 	 * @param tile insets offsetX
 	 * @param tile insets offsetY
 	 * @param height
 	 * @return point on screen
 	 */
 	public static final Point tileToScreen(int x, int y, final double offsetX, final double offsetY, int height) {
 		double worldUnitX = (x + offsetX) * 128.0D;
 		double worldUnitY = (y + offsetY) * 128.0D;
 		height = tileHeight((int) worldUnitX, (int) worldUnitY) - height;
 		return worldToScreen(worldUnitX, worldUnitY, height);
 	}
 	
 	/**
 	 * Convert a tile to the point on screen
 	 * @param tile
 	 * @return screen location
 	 */
 	public static final Point tileToScreen(final Tile tile) {
 		return tileToScreen(tile.getRegionX(), tile.getRegionY(), 0.5D, 0.5D, 0);
 	}
 	
 	/**
 	 * Calculates distance between two points
 	 * @param curr
 	 * @param dest
 	 * @return distance
 	 */
 	public static final double distanceBetween(final Point curr, final Point dest) {
 		return Math.sqrt((curr.x - dest.x) * (curr.x - dest.x) + (curr.y - dest.y) * (curr.y - dest.y));
 	}
 	
 	/**
 	 * World units to screen location
 	 * @param world unit X
 	 * @param world unit Y
 	 * @param height
 	 * @return point on screen
 	 */
 	public static final Point worldToScreen(double X, double Y, final double height) {
 		if (X < 128 || Y < 128 || X > 13056 || Y > 13056) {
 			return new Point(-1, -1);
 		}
 		try {
 			final Client client = Loader.getClient();
 			
 			final int tileCalculation = ((int) height) - client.getCameraZ();
 			X -= client.getCameraX();
 			final int pitchSinus = SINUS[client.getCameraPitch()];
 			final int pitchCosinus = COSINUS[client.getCameraPitch()];
 			Y -= client.getCameraY();
 			final int yawSinus = SINUS[client.getCameraYaw()];
 			final int yawCosinus = COSINUS[client.getCameraYaw()];
 			int calculation = pitchSinus * (int) Y + ((int) X * pitchCosinus) >> 16;
 			Y = -(pitchSinus * (int) X) + (int) Y * pitchCosinus >> 16;
 			X = calculation;
 			calculation = yawCosinus * tileCalculation - yawSinus * (int) Y >> 16;
 			Y = yawSinus * tileCalculation + ((int) Y * yawCosinus) >> 16;
 			final int screenX = ((int) X << 9) / (int) Y + 256;
 			final int screenY = (calculation << 9) / (int) Y + 167;
 			return new Point(screenX, screenY);
 		} catch (ArithmeticException e){
 			// / by zero
 		}
 		return new Point(-1, -1);
 	}
 	
 	/**
 	 * Calculates distance between local player and given tile
 	 * 
 	 * @param tile
 	 * @return distance between local player and given tile
 	 */
 	public static final double distanceTo(Tile tile) {
 		return distanceBetween(tile, Players.getLocal().getLocation());
 	}
 	
 	/**
 	 * Calculates distance between two given tiles
 	 * @param tile point a
 	 * @param tile point b
 	 * @return distance between two given tiles
 	 */
 	public static final double distanceBetween(Tile a, Tile b) {
 		int x = b.getX() - a.getX();
 		int y = b.getY() - a.getY();
 		return Math.sqrt((x * x) + (y * y));
 	}
 	
 	/**
 	 * Calculates tile height
 	 * @param world unit x
 	 * @param world unit y
 	 * @return height
 	 */
 	public static final int tileHeight(int x, int y) {
 		final Client client = Loader.getClient();
 		
 		int[][][] ground = client.getTileOffsets();
 		int zidx = client.getPlane();
 		int x1 = x >> 7;
 		int y1 = y >> 7;
 		int x2 = x & 0x7f;
 		int y2 = 0x7f & y;
 
 		if (x1 < 0 || y1 < 0 || x1 > 103 || y1 > 103)
 			return 0;
 
 		if (zidx < 3 && (2 & client.getSceneFlags()[1][x1][y1]) == 2)
 			zidx++;
 
 		int i = ground[zidx][1 + x1][y1] * x2 + (128 + -x2) * ground[zidx][x1][y1] >> 7;
 		int j = ground[zidx][1 + x1][1 + y1] * x2 + ground[zidx][x1][y1 + 1] * (128 - x2) >> 7;
 
 		return j * y2 + (128 - y2) * i >> 7;
 	}
 	
 	/**
 	 * Determines whether a point is on the 3d screen
 	 * @param point
 	 * @return <b>true</b> when the point is on the 3d screen, otherwise false
 	 */
 	public static final boolean isOnScreen(final Point p) {
 		return GAME3D_SCREEN.contains(p);
 	}
 	
 	
 	/**
 	 * Calculates camera angle to tile
 	 * 
 	 * @param tile
 	 * @return camera angle to tile
 	 */
 	public static final int angleToTile(Tile t) {
 		Tile me = Players.getLocal().getLocation();
 		int angle = (int) Math.toDegrees(Math.atan2(t.getY() - me.getY(), t.getX() - me.getX()));
 		return angle >= 0 ? angle : 360 + angle;
 	}
 	
 	/**
 	 * Calculates point on minimap
 	 * 
 	 * @param tile
 	 * @param in
 	 *            if true it will return a point only within minimap, false for points out of minimap too
 	 * @return point in or out of minimap
 	 */
 	public static final Point tileToMinimap(Tile tile, boolean in) {
 		final Client client = Loader.getClient();
 		int x = tile.getX() - client.getBaseX();
 		int y = tile.getY() - client.getBaseY();
 		int mmX = x * 4 + 2 - client.getLocalPlayer().getX() / 32;
 		int mmY = y * 4 + 2 - client.getLocalPlayer().getY() / 32;
 		return worldToMinimap(mmX, mmY, in);
 	}
 
 	/**
 	 * Tile to minimap
 	 * @param tile
 	 * @param random x
 	 * @param random y
 	 * @return point on MM
 	 */
 	public static final Point tileToMinimap(Tile tile, int x, int y) {
 		int x2 = nextInt(-x, x);
 		int y2 = nextInt(-y, y);
 		Tile t = new Tile(tile.getX() + x2, tile.getY() + y2);
 		return tileToMinimap(t, true);
 	}
 
 	/**
 	 * Calculates point on minimap based on given tile
 	 * 
 	 * @param tile
 	 * @return point on minimap
 	 */
 	public static final Point tileToMinimap(Tile tile) {
 		return tileToMinimap(tile, true);
 	}
 
 	private static final Point worldToMinimap(int x, int y, boolean in) {
 		final Client client = Loader.getClient();
         int i;
        if (Reader.readProvider("serverName").toLowerCase() == "pkhonor"){
            i = client.getMinimapInt1();
          }else{
             i = client.getMinimapInt1() + client.getMinimapInt2() & 0x7FF;
         }
 		if (in) {
 			int j = x * x + y * y;
 			if (j > 6400) {
 				return new Point(-1, -1);
 			}
 		}
 		int k = SINUS[i];
 		int m = COSINUS[i];
 		k = k * 256 / (client.getMinimapInt3() + 256);
 		m = m * 256 / (client.getMinimapInt3() + 256);
 		int n = y * k + x * m >> 16;
 		int i1 = y * m - x * k >> 16;
 		return new Point(minimapBaseX + n, minimapBaseY - i1);
 	}
 	
 	private static final int nextInt(final int min, final int max) {
 		if (max < min) {
 			return max + RANDOM.nextInt(min - max);
 		}
 		return min + (max == min ? 0 : RANDOM.nextInt(max - min));
 	}
 
 }
