 package info.salzsee.haw.prp2;
 
 import jgame.*;
 import jgame.platform.*;
 
 public class Simulator  extends StdGame {
 
	public Simulator(String[] args) {
		JGPoint size = parseSizeArgs(args,0);
 		initEngine(size.x,size.y);
 	}
 	
 	@Override
 	public void initCanvas() {
 		// setCanvasSettings(TilesX,TilesY,TileW,TileH,FGColor,BGColor,MsgFont)
		setCanvasSettings(60,60,16,16,null,JGColor.blue,null);
 	}
 
 	@Override
 	public void initGame() {
 		defineMedia("sprite.tbl");
 		// setFrameRate(fps,maxFrameSkip
 		setFrameRate(45,1);
 		
 		Particle p = new Particle(10.0);
 	}
 	
 	public void doFrame() {
 		moveObjects();
 		
 	}
 	
 }
