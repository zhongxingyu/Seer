 package com.jpii.navalbattle.pavo;
 
 import java.awt.*;
 import java.awt.image.BufferedImage;
 import java.io.File;
 
 import maximusvladimir.dagen.Rand;
 
 import com.jpii.navalbattle.data.Constants;
 import com.jpii.navalbattle.io.Interactable;
 
 
 public class World extends Renderable implements Interactable {
 	WorldGen gen;
 	Chunk[] chunks;
 	BufferedImage buffer;
 	boolean needsNewRender = false;
 	boolean[] generated;
 	boolean bufferLock = false;
 	WorldSize ws;
 	int width = 16;
 	int height = 8;
 	int lww = 800;
 	int lwh = 600;
 	EntityManager em;
 	TimeManager time = new TimeManager();
 	int sx = 0, anisx = 0, anisy = 0,sy = 0;
 	BufferedImage noise;
 	int zlevel;
 	public World() {
 		ws = WorldSize.WORLD_LARGE;
 		em = new EntityManager(this);
 		width = PavoHelper.getGameWidth(getWorldSize());
 		height = PavoHelper.getGameHeight(getWorldSize());
 		chunks = new Chunk[(width)*(height)];
 		for (int x = 0;x < width; x++) {
 			for (int z = 0; z < height; z++) {
 				int i = z*width+x;
 				chunks[i] = new Chunk(this);
 				chunks[i].setX(x);
 				chunks[i].setZ(z);
 			}
 		}
 		generated = new boolean[chunks.length];
 		buffer = new BufferedImage(GameBeta.Settings.currentWidth,GameBeta.Settings.currentHeight,BufferedImage.TYPE_INT_RGB);
 		makeNoise();
 	}
 	public Chunk getChunk(int index) {
 		return chunks[index];
 	}
 	public Chunk getChunk(int x, int z) {
 		return getChunk(z*PavoHelper.getGameWidth(getWorldSize())+x);
 	}
 	public void setZoomLevel(int level) {
 		zlevel = level;
 	}
 	public int getZoomLevel() {
 		return zlevel;
 	}
 	public void makeNoise(){
 		noise = new BufferedImage(GameBeta.Settings.currentWidth,GameBeta.Settings.currentHeight,BufferedImage.TYPE_INT_RGB);
 		Rand ras = new Rand(GameBeta.Settings.seed+22);
 		Graphics gs2 = noise.getGraphics(); // Q and D
 		for (int x = 0; x < GameBeta.Settings.currentWidth; x+= 2) {
 			for (int y = 0; y < GameBeta.Settings.currentHeight; y+=2) {
 				int rgb = ras.nextInt(127);
 				gs2.setColor(new Color(rgb,rgb,rgb));
 				gs2.fillRect(x,y,2,2);
 			}
 		}
 	}
 	public void setLoc(int x, int y) {
 		if (sx != x || sy != y)
 			chunkrender = true;
 		sx = x;
 		sy = y;
 	}
 	public void setLocX(int x) {
 		if (sx != x)
 			chunkrender = true;
 		sx = x;
 	}
 	public void setLocY(int y) {
 		if (sy != y)
 			chunkrender = true;
 		sy = y;
 	}
 	public void setWorldGen(WorldGen wg) {
 		gen = wg;
 	}
 	public WorldGen getWorldGen() {
 		return gen;
 	}
 	public void update() {
 		time.update();
 	}
 	public boolean hasMoreChunks() {
 		for (int c = 0; c < chunks.length; c++) {
 			if (!generated[c])
 				return true;
 		}
 		return false;
 	}
 	public void genNextChunk() {
 		for (int c = 0; c < chunks.length; c++) {
 			Chunk chunk = chunks[c];
 			GameBeta.getStats().SmKdn02nOaP(c*2);
 			while (chunk.isLocked()) {
 				
 			}
 			chunk.lock();
 			if (!generated[c]){
 				//System.out.println("Chunk at " + c + " generated.");
 				chunk.render();
 				generated[c] = true;
 				needsNewRender = true;
 				//break;
 			}
 			chunkrender = true;
 			chunk.unlock();
 			chunks[c] = chunk;
 		}
 	}
 	boolean chunkrender = false;
 	public boolean needsReChunkRender() {
 		return chunkrender;
 	}
 	public synchronized void render() {
 		if (!needsReChunkRender())
 			return;
 		long waitStart = System.currentTimeMillis();
 		while (bufferLock) {
 			
 		}
 		long endWait = System.currentTimeMillis() - waitStart;
 		bufferLock = true;
 		long startDraw = System.currentTimeMillis();
 		if (lww != GameBeta.Settings.currentWidth || lwh != GameBeta.Settings.currentHeight) {
 			buffer = new BufferedImage(GameBeta.Settings.currentWidth,GameBeta.Settings.currentHeight,BufferedImage.TYPE_INT_RGB);
 			lww = GameBeta.Settings.currentWidth;
 			lwh = GameBeta.Settings.currentHeight;
 			makeNoise();
 		}
 		int liveChunks = 0;
 		Graphics2D g = PavoHelper.createGraphics(buffer);
 		//g.drawIm
 		g.drawImage(noise, 0, 0, null);
 		for (int x = 0; x < width; x++) {
 			for (int z = 0; z < height; z++) {
 				Chunk chunk = chunks[z*width+x];
 				if (PavoHelper.isChunkVisibleOnScreen(this, chunk)) {
 					//while (chunk.isLocked()) { }
 					//chunk.lock();
 					if (!chunk.isGenerated()) {
 						int rgb = GameBeta.Settings.rand.nextInt(255);
 						if (GameBeta.Settings.rand.nextBoolean())
 							g.setColor(new Color(6,rgb,13));
 						else
 							g.setColor(new Color(6,13,rgb));
 						g.fillRect(x*100,z*100,100,100);
 					}
 					else if (x-2 == width || z-2 == height) {
 						g.fillRect(sx+(x*100),sy+(z*100), 303, 303);
 					}
 					else {
 						if (chunk.needsBufferWrite())
 							chunk.writeBuffer();
 						if (chunk.getBuffer() != null)
							g.drawImage(chunk.getBuffer(), sx+(x*100),sy+(z*100),null);
 					}
 					//chunk.unlock();
 					liveChunks++;
 				}
 			}
 		}
 		chunkrender = false;
 		long endDraw = System.currentTimeMillis() - startDraw;
 		GameBeta.getStats().SmKAk10(endDraw);
 		GameBeta.getStats().SmoOa01kwL(liveChunks);
 		GameBeta.getStats().Smw2e33AK(endWait);
 		bufferLock = false;
 	}
 	public BufferedImage getBuffer() {
 		return buffer;
 	}
 	public int getScreenX() {
 		return sx;
 	}
 	public int getScreenY() {
 		return sy;
 	}
 	public void moveScreenTo(int x, int y) {
 		anisx = x;
 		anisy = y;
 	}
 	public WorldSize getWorldSize() {
 		return ws;
 	}
 	public EntityManager getEntityManager() {
 		return em;
 	}
 	public TimeManager getTimeManager() {
 		return time;
 	}
 	public void save(String path) {
 		File file = new File(path);
 		boolean throwError = false;
 		try {
 			if (!file.exists()) {
 				if (!file.createNewFile()) {
 					throwError = true;
 				}
 			}
 		}
 		catch (Error err) {
 		}
 		catch (Exception ex) {
 		}
 		if (throwError)
 			throw new java.lang.IllegalArgumentException("Unable to save file. See store for details");
 	}
 	public void load(String path) {
 	}
 	public void peekElements() {
 		
 	}
 }
