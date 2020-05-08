 package Game.Sprite;
 
 import Game.Sprite.Player;
 import Game.MapData.MapData;
 import Game.Common.Data;
 
 import java.awt.Graphics;
 
 public class RightUpSlope extends Sprite{
 
 	public RightUpSlope(int x, int y){
 		super(x,y);
 		image = Data.image.rightUpSlopeImage;
 		width = Data.CHIP_SIZE;
 		height = Data.CHIP_SIZE;
 	}
 	// XvCgupdate
 	public void update(MapData mapData){}
 	// vC[XvCgɐGꂽƂ̊֐
 	public void touch(Sprite s, int dir, int[] dest){
 		int px = s.getX()+s.getWidth()-Data.CD_DIFF;
 		int py = s.getY()+s.getHeight()-Data.CD_DIFF;
 		// Gꂽꍇ
 		if(s.getVy() < 0 && s.getY()+Data.CD_DIFF >= y+height){
 			s.setVy(y+height-Data.CD_DIFF-s.getY());
 			return;
 		}
 		// EGꂽꍇ
 		if(s.getX()+Data.CD_DIFF >= x+width && py > y){
 			s.setVx(x+width-Data.CD_DIFF-s.getX());
 			return;
 		}
 		// _̏ꍇ
 		if(px > x+width){
 			s.setVy(y-py);
 			s.land();
 			return;
 		}
 		if((px+s.getVx()-x) + (py+s.getVy()-y) <= Data.CHIP_SIZE) return;
		if(s.jumping() && s.getY() < 0) return;
 		s.setVy(Data.CHIP_SIZE-(px+s.getVx()-x)-(py-y));
 		s.land();
 	}
 }
