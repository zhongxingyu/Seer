 package com.jpii.navalbattle.pavo;
 
 import java.awt.*;
 import java.awt.image.BufferedImage;
 
 import maximusvladimir.dagen.*;
 
 import com.jpii.navalbattle.data.Constants;
 import com.jpii.navalbattle.renderer.Helper;
 import com.jpii.navalbattle.renderer.RenderConstants;
 
 public class Chunk extends Renderable {
 	int x,z;
 	boolean generated = false;
 	public EntityReference Tile00, Tile10, Tile01,Tile11;
 	static Perlin p = new Perlin(Constants.MAIN_RAND.nextLong(),0,0);
 	Rand rand = new Rand();
 	World w;
 	BufferedImage terrain;
 	public Chunk(World w) {
 		this.w = w;
 		Tile00 = Tile10 = Tile01 = Tile11 = w.getEntityManager().getTypeById(0);
 	}
 	public void setX(int x) {
 		this.x = x;
 	}
 	public void setZ(int z) {
 		this.z = z;
 	}
 	public int getX() {
 		return x;
 	}
 	public int getZ() {
 		return z;
 	}
 	public void setLoc(int x, int z) {
 		this.x = x;
 		this.z = z;
 	}
 	public void render() {
 		//if (!ready)
 			//return;
 		//ready = false;
 		rand.setSeed(Constants.MAIN_SEED+(x&z));
		terrain = new BufferedImage(100,100,BufferedImage.TYPE_INT_RGB);
 		Graphics g = terrain.getGraphics();
 		for (int lsx = 0; lsx < 100/3; lsx++) {
 			for (int lsz = 0; lsz < 100/3; lsz++) {
 				float frsh = McRegion.getPoint(lsx+(100.0f/3.0f*x), lsz+(100.0f/3.0f*z));
 				int opcode = (int)(frsh*255.0f);
 				//opcode = (opcode+(int)(McRegion.getPoint((this.x*100), (this.z*100))*255.0f))/2;
 				if (opcode > 255)
 					opcode = 255;
 				if (opcode < 0)
 					opcode = 0;
 				g.setColor(new Color(opcode,opcode,opcode));
 				if (opcode < 130) {
 					g.setColor(Helper.adjust(Helper.randomise(RenderConstants.GEN_WATER_COLOR,
 	                        RenderConstants.GEN_COLOR_DIFF, rand, false), 1 - ((frsh)/2 / RenderConstants.GEN_WATER_HEIGHT), 50));
 				}
 				else{
 					g.setColor(Helper.adjust(Helper.randomise(RenderConstants.GEN_GRASS_COLOR,
 	                        RenderConstants.GEN_COLOR_DIFF, rand, false), (1.0-frsh)/2, 50));
 				}
				g.fillRect(lsx*3,lsz*3,4,4);
 			}
		}
 		writeBuffer();
 		//ready = true;
 		generated = true;
 	}
 	public void writeBuffer() {
 		buffer = new BufferedImage(100,100,BufferedImage.TYPE_INT_RGB);
 		Graphics2D g = PavoHelper.createGraphics(buffer);
 		g.drawImage(terrain, 0, 0, null);
 		g.drawImage(w.getEntityManager().getImage(Tile00), 0, 0, null);
 		g.drawImage(w.getEntityManager().getImage(Tile10), 50, 0, null);
 		g.drawImage(w.getEntityManager().getImage(Tile01), 0, 50, null);
 		g.drawImage(w.getEntityManager().getImage(Tile11), 50, 50, null);
 	}
 	public boolean isGenerated() {
 		return generated;
 	}
 }
