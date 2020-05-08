 /*
  * Created on Dec 26, 2004
  */
 package org.spacebar.escape.j2se;
 
 import java.awt.*;
 import java.awt.geom.AffineTransform;
 import java.awt.image.BufferedImage;
 
 import org.spacebar.escape.common.*;
 import org.spacebar.escape.common.Level.DirtyList;
 import org.spacebar.escape.solver.Level;
 
 /**
  * @author adam
  */
 public class Drawing {
 	final static int SCALE_DOWN_FACTORS = 5;
 
 	final static int SCALE_UP_FACTORS = 2;
 
 	private final static int TILE_SIZE = 32;
 
 	private final static int PLAYER_FRAMES = 5;
 
 	private final static BufferedImage[] player;
 
 	private final static BufferedImage[][] bots;
 
 	static {
 		String pNames[] = { "animation/walk_forward_0.png",
 				"animation/walk_forward_a.png",
 				"animation/walk_forward_a2.png",
 				"animation/walk_forward_b.png",
 				"animation/walk_forward_b2.png",
 
 				"animation/walk_right_0.png", "animation/walk_right_a.png",
 				"animation/walk_right_a2.png", "animation/walk_right_b.png",
 				"animation/walk_right_b2.png",
 
 				"animation/walk_left_0.png", "animation/walk_left_a.png",
 				"animation/walk_left_a2.png", "animation/walk_left_b.png",
 				"animation/walk_left_b2.png",
 
 				"animation/walk_backward_0.png",
 				"animation/walk_backward_a.png",
 				"animation/walk_backward_a2.png",
 				"animation/walk_backward_b.png",
 				"animation/walk_backward_b2.png" };
 
 		// load the animations for the player
 		BufferedImage playerAnim = ResourceUtil.stitchHoriz(pNames);
 
 		player = ResourceUtil.createScaledImages(playerAnim,
 				SCALE_DOWN_FACTORS, SCALE_UP_FACTORS);
 
 		bots = new BufferedImage[5][];
 		String bNames0[] = { "animation/deadrobot.png" };
 		bots[0] = ResourceUtil.createScaledImages(ResourceUtil
 				.stitchHoriz(bNames0), SCALE_DOWN_FACTORS, SCALE_UP_FACTORS);
 		String bNames1[] = { "animation/dalek_forward_0.png",
 				"animation/dalek_forward_1.png" };
 		bots[1] = ResourceUtil.createScaledImages(ResourceUtil
 				.stitchHoriz(bNames1), SCALE_DOWN_FACTORS, SCALE_UP_FACTORS);
 		String bNames2[] = { "animation/hugbot_forward_0.png",
 				"animation/hugbot_forward_1.png" };
 		bots[2] = ResourceUtil.createScaledImages(ResourceUtil
 				.stitchHoriz(bNames2), SCALE_DOWN_FACTORS, SCALE_UP_FACTORS);
 		String bNames3[] = { "animation/dalek_asleep.png" };
 		bots[3] = ResourceUtil.createScaledImages(ResourceUtil
 				.stitchHoriz(bNames3), SCALE_DOWN_FACTORS, SCALE_UP_FACTORS);
 		String bNames4[] = { "animation/hugbot_asleep.png" };
 		bots[4] = ResourceUtil.createScaledImages(ResourceUtil
 				.stitchHoriz(bNames4), SCALE_DOWN_FACTORS, SCALE_UP_FACTORS);
 	}
 
 	private final static BufferedImage[] tiles = ResourceUtil.loadScaledImages(
 			"tiles.png", SCALE_DOWN_FACTORS, SCALE_UP_FACTORS);
 
 	private final static int TILES_ACROSS = 16;
 
 	private final static BufferedImage font = ResourceUtil
 			.loadImage("font.png");
 
 	static double getScaleVal(int scale) {
 		double scaleVal;
 
 		if (scale < 0) {
 			scaleVal = 1 * (double) (1 << -scale);
 		} else {
 			scaleVal = 1 / (double) (1 << scale);
 		}
 		return scaleVal;
 	}
 
 	static int getTileSize(int scale) {
 		if (scale < 0) {
 			return TILE_SIZE * (1 << -scale);
 		} else {
 			return TILE_SIZE / (1 << scale);
 		}
 	}
 
 	static int getZoomIndex(int scale) {
 		int zoom = scale;
 		if (zoom < 0) {
 			zoom = -scale + SCALE_DOWN_FACTORS - 1;
 		}
 		return zoom;
 	}
 
 	/**
 	 * @param g
 	 * @param theLevel
 	 * @param xScroll
 	 * @param yScroll
 	 * @param playerDir
 	 * @param scale
 	 */
 	public static void paintSprites(Graphics2D g, Level theLevel, int xScroll,
 			int yScroll, int scale) {
 		int spriteCount = 1 + theLevel.getBotCount();
 
 		// y is z
 		int s[][] = new int[theLevel.getHeight()][spriteCount];
 
 		// compute z-ordering based on y
 		s[theLevel.getPlayerY()][0] = 999; // 1000 - 1
 		for (int i = 0; i < theLevel.getBotCount(); i++) {
 			if (theLevel.isBotDeleted(i)) {
 				continue;
 			}
 			int z = 1000 + i; // avoid re-initializing array
 			int y = theLevel.getBotY(i);
 
 			int row[] = s[y];
 			for (int j = 0; j < row.length; j++) {
 				if (row[j] == 0) {
 					row[j] = z; // don't clobber sprites (but wasteful of
 					// memory)
 					break;
 				}
 			}
 		}
 
 		// now draw in the correct order
 		for (int i = 0; i < s.length; i++) {
 			int row[] = s[i];
 			for (int j = 0; j < row.length; j++) {
 				if (row[j] == 0) {
 					continue;
 				}
 
 				int index = row[j] - 1000;
 				paintSprite(g, theLevel, xScroll, yScroll, index, scale);
 			}
 		}
 
 		paintLaser(g, theLevel, xScroll, yScroll, scale);
 	}
 
 	static private void paintLaser(Graphics2D g2, Level theLevel, int xScroll,
 			int yScroll, int scale) {
 		IntTriple laser = theLevel.getLaser();
 
 		if (laser == null) {
 			return;
 		}
 
 		double scaleVal = getScaleVal(scale);
 
 		int d = laser.d;
 
 		int gx = (theLevel.getPlayerX() - xScroll) * TILE_SIZE
 				+ (TILE_SIZE >> 1);
 		int gy = (theLevel.getPlayerY() - yScroll) * TILE_SIZE
 				+ (TILE_SIZE >> 1);
 
 		int lx = (laser.x - xScroll) * TILE_SIZE;
 		int ly = (laser.y - yScroll) * TILE_SIZE;
 
 		Rectangle outer, inner;
 
 		switch (d) {
 		case Entity.DIR_DOWN:
 			lx += TILE_SIZE >> 1;
 			ly += TILE_SIZE;
 
 			lx *= scaleVal;
 			ly *= scaleVal;
 			gx *= scaleVal;
 			gy *= scaleVal;
 
 			outer = new Rectangle(lx - 1, ly, 3, gy - ly);
 			inner = new Rectangle(lx, ly, 1, gy - ly);
 			break;
 		case Entity.DIR_UP:
 			lx *= scaleVal;
 			ly *= scaleVal;
 			gx *= scaleVal;
 			gy *= scaleVal;
 
 			outer = new Rectangle(gx - 1, gy + 1, 3, ly - gy);
 			inner = new Rectangle(gx, gy + 1, 1, ly - gy);
 			break;
 		case Entity.DIR_RIGHT:
 			lx += TILE_SIZE;
 			ly += TILE_SIZE >> 1;
 
 			lx *= scaleVal;
 			ly *= scaleVal;
 			gx *= scaleVal;
 			gy *= scaleVal;
 
 			outer = new Rectangle(lx, ly - 1, gx - lx, 3);
 			inner = new Rectangle(lx, ly, gx - lx, 1);
 			break;
 		case Entity.DIR_LEFT:
 			lx *= scaleVal;
 			ly *= scaleVal;
 			gx *= scaleVal;
 			gy *= scaleVal;
 
 			outer = new Rectangle(gx + 1, gy - 1, lx - gx, 3);
 			inner = new Rectangle(gx + 1, gy, lx - gx, 1);
 			break;
 		default:
 			outer = inner = null;
 		}
 
 		g2.setColor(Color.RED);
 		g2.fill(outer);
 		g2.setColor(Color.WHITE);
 		g2.fill(inner);
 	}
 
 	static public void paintLevel(Graphics2D g2, Level theLevel, int xScroll,
 			int yScroll, boolean showBizarro, int scale) {
 		int zoom = getZoomIndex(scale);
 		int tileSize = getTileSize(scale);
 		// System.out.println("tilesize: " + tileSize);
 		// System.out.println("zoom: " + zoom);
 
		DirtyList d = theLevel.dirty;
 		if (d.isAnyDirty()) {
 			if (d.isAllDirty()) {
 				for (int j = 0; j < theLevel.getHeight() - yScroll; j++) {
 					for (int i = 0; i < theLevel.getWidth() - xScroll; i++) {
 						int dx = i * tileSize;
 						int dy = j * tileSize;
 
 						int tile;
 						tile = getTile(theLevel, i + xScroll, j + yScroll,
 								showBizarro);
 
 						paintTile(g2, zoom, tileSize, dx, dy, tile);
 					}
 				}
 			} else {
 				// some are dirty so just do those
 				for (int i = d.getNumDirty() - 1; i >= 0; i--) {
 					int idx = d.getDirty(i);
 					int x = idx % theLevel.getWidth();
 					int y = idx / theLevel.getWidth();
 
 					int dx = x * tileSize;
 					int dy = y * tileSize;
 
 					int tile = getTile(theLevel, x, y, showBizarro);
 					paintTile(g2, zoom, tileSize, dx, dy, tile);
 				}
 			}
 			d.clearDirty();
 		}
 	}
 
 	/**
 	 * @param theLevel
 	 * @param x
 	 * @param y
 	 * @param xScroll
 	 * @param yScroll
 	 * @param showBizarro
 	 * @return
 	 */
 	private static int getTile(Level theLevel, int x, int y, boolean showBizarro) {
 		int tile;
 		if (showBizarro) {
 			tile = theLevel.oTileAt(x, y);
 		} else {
 			tile = theLevel.tileAt(x, y);
 		}
 		return tile;
 	}
 
 	static private void paintSprite(Graphics2D g2, Level theLevel, int xScroll,
 			int yScroll, int spriteIndex, int scale) {
 		if (spriteIndex == -1) {
 			paintPlayer(g2, theLevel, xScroll, yScroll, scale);
 		} else {
 			paintBot(g2, theLevel, xScroll, yScroll, spriteIndex, scale);
 		}
 	}
 
 	private static void paintBot(Graphics2D g2, Level theLevel, int xScroll,
 			int yScroll, int botIndex, int scale) {
 		if (theLevel.isBotDeleted(botIndex)) {
 			return;
 		}
 
 		int zoom = getZoomIndex(scale);
 		int tileSize = getTileSize(scale);
 
 		int botType = theLevel.getBotType(botIndex);
 
 		int height = bots[botType][zoom].getHeight();
 
 		int dx = (theLevel.getBotX(botIndex) - xScroll) * tileSize;
 		int dy = (theLevel.getBotY(botIndex) - yScroll) * tileSize
 				- (height - tileSize);
 
 		int sx;
 		switch (theLevel.getBotDir(botIndex)) {
 		case Entity.DIR_DOWN:
 		case Entity.DIR_RIGHT:
 		case Entity.DIR_LEFT:
 		case Entity.DIR_UP:
 		default:
 			sx = 0;
 			break;
 		}
 
 		g2.drawImage(bots[botType][zoom], dx, dy, dx + tileSize, dy + height,
 				sx, 0, sx + tileSize, height, null);
 		g2.setColor(Color.WHITE);
 		AffineTransform a = g2.getTransform();
 		g2.translate(dx + tileSize - Characters.FONT_WIDTH, dy + height
 				- Characters.FONT_HEIGHT);
 		drawString(g2, Characters.YELLOW + Integer.toString(botIndex + 1));
 		g2.setTransform(a);
 	}
 
 	static private void paintPlayer(Graphics2D g2, Level theLevel, int xScroll,
 			int yScroll, int scale) {
 		int zoom = getZoomIndex(scale);
 		int tileSize = getTileSize(scale);
 		int height = player[zoom].getHeight();
 
 		int dx = (theLevel.getPlayerX() - xScroll) * tileSize;
 		int dy = (theLevel.getPlayerY() - yScroll) * tileSize
 				- (height - tileSize);
 
 		int sx;
 		switch (theLevel.getPlayerDir()) {
 		case Entity.DIR_DOWN:
 			sx = 0;
 			break;
 		case Entity.DIR_RIGHT:
 			sx = PLAYER_FRAMES * tileSize;
 			break;
 		case Entity.DIR_LEFT:
 			sx = 2 * PLAYER_FRAMES * tileSize;
 			break;
 		case Entity.DIR_UP:
 		default:
 			sx = 3 * PLAYER_FRAMES * tileSize;
 			break;
 		}
 
 		g2.drawImage(player[zoom], dx, dy, dx + tileSize, dy + height, sx, 0,
 				sx + tileSize, height, null);
 	}
 
 	static private void paintTile(Graphics2D g2, int zoom, int tileSize,
 			int dx, int dy, int tile) {
 		int sx = tile % TILES_ACROSS * tileSize;
 		int sy = tile / TILES_ACROSS * tileSize;
 
 		g2.drawImage(tiles[zoom], dx, dy, dx + tileSize, dy + tileSize, sx, sy,
 				sx + tileSize, sy + tileSize, null);
 	}
 
 	static public void drawString(Graphics2D g2, String text) {
 		StyleStack s = new StyleStack();
 
 		// System.out.println("drawString clip: " + g2.getClip());
 
 		Composite ac = g2.getComposite();
 
 		int dx = 0;
 		int dy = 0;
 		for (int i = 0; i < text.length(); i++) {
 			char ch = text.charAt(i);
 			if (ch == '^') {
 				i++;
 				ch = text.charAt(i);
 				switch (ch) {
 				case '^':
 					break;
 				case '<':
 					s.pop();
 					break;
 				default:
 					s.push(ch);
 				}
 			} else if (ch == '\n') {
 				dx = 0;
 				dy += Characters.FONT_HEIGHT;
 			} else {
 				int tile = Characters.getIndexForChar(text.charAt(i));
 
 				int sx = tile * (Characters.FONT_WIDTH);
 				int sy = s.getColor() * (Characters.FONT_HEIGHT);
 
 				g2.setComposite(AlphaComposite.getInstance(
 						AlphaComposite.SRC_OVER, s.getAlphaValue() / 255f));
 				g2.drawImage(font, dx, dy, dx + Characters.FONT_WIDTH, dy
 						+ Characters.FONT_HEIGHT, sx, sy, sx
 						+ Characters.FONT_WIDTH, sy + Characters.FONT_HEIGHT,
 						null);
 				dx += Characters.FONT_ADVANCE;
 			}
 		}
 		g2.setComposite(ac);
 	}
 }
