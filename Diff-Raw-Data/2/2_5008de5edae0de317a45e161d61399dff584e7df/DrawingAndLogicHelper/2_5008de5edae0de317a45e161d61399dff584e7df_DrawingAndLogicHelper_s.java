 package net.blockscape.helper;
 
 import net.blockscape.BlockScape;
 import net.blockscape.Player;
 import net.blockscape.block.Block;
 import net.blockscape.world.World;
 import net.blockscape.world.WorldBlock;
 import processing.core.PApplet;
 
 public class DrawingAndLogicHelper {
 	
 	static float rotSinTim;
 	/**
 	 * Called every time the game loops
 	 * @param host the game window
 	 */
 	public static void draw(PApplet host){
 		host.textFont(host.createFont("Arial",14,true));
 		for(WorldBlock b: World.getWorld()){
 			b.updateAndDraw();
 		}
 		rotSinTim += 0.05;
 		Player.setYvelocity(Player.getYvelocity() + 0.1F);
 		
 		BlockScape.distBetweenPlayerAndMouse = PApplet.dist(Player.getX()+Player.getWidth()/2, Player.getY()+Player.getHeight()/2, host.mouseX, host.mouseY)/16;
 		
		if(BlockScape.distBetweenPlayerAndMouse< BlockScape.maxReachDistance){
 			BlockScape.canPlaceOrRemoveBlock = true;
 		}else{
 			BlockScape.canPlaceOrRemoveBlock = false;
 		}
 		
 		Player.update();
 		
 		Player.draw();
 		
 		
 		if(((BlockScape)host).selectedBlock != null){
 		  float r = PApplet.map(PApplet.sin(rotSinTim), -1, 1, -16, 16);
 		  host.pushMatrix();
 
 		  host.translate(175, 24);
 		  host.imageMode(PApplet.CENTER);
 		  host.rectMode(PApplet.CENTER);
 		  host.rotate(PApplet.radians(r));
 		  host.image(((BlockScape)host).selectedBlock.getTexture(),0, 0, 16, 16);
 		  if(!BlockScape.canPlaceOrRemoveBlock){
 			  host.fill(0,100);
 			  host.rect(0, 0, 16, 16);
 		  }
 		  host.popMatrix();
 		  host.rectMode(PApplet.CORNER);
 		  host.fill(0);
 		  host.text("Currently Selected Block:", 1, 30);
 		  host.fill(BlockScape.canPlaceOrRemoveBlock ? 200: 0);
 		  host.text(((BlockScape)host).selectedBlock.getName(), 190, 30);
 		  host.rectMode(PApplet.CORNER);
 		}else{
 			((BlockScape)host).selectedBlock = Block.blockStone;
 		}
 		
 		//Check for block placement and removal
 		if (host.mousePressed && host.mouseButton==PApplet.RIGHT && BlockScape.canPlaceOrRemoveBlock) {
 			World.addBlockWithoutReplacing(new WorldBlock(host.mouseX/16, (host.height - host.mouseY)/16, ((BlockScape)host).selectedBlock, host));
 		}
 		if (host.mousePressed && host.mouseButton==PApplet.LEFT && BlockScape.canPlaceOrRemoveBlock) {
 			World.removeBlockFromWorld(host.mouseX/16, (host.height - host.mouseY)/16);
 		}
 		
 	}	
 }
