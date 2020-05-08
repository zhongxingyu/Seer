 package game.CellType;
 
 import android.content.res.Resources;
 import android.graphics.Color;
 import game.Entity;
 import game.GameModel;
 import game.GameScore;
 import gameEngine.GameObject;
 
 public class CellPop extends Cell implements GameObject{
 
 	private int endRad;
 	private float curRad;
 	
 	public CellPop(int startX, int startY, float startDX, float startDY,
 			int rad, Resources viewRes, GameModel refModel) {
 		super(startX, startY, startDX, startDY, (int) (rad*0.1), viewRes, refModel);
 		getEntityColor().setColor(Color.WHITE);
 		this.setType(Entity.CELLPOP);
 		curRad = this.getRadius();
 		endRad = rad;
 	}
 
 	public void Collide(Entity e1)
 	{
 		if(e1.getType() == Entity.FATCELL)
 			{
				FatCell temp = (FatCell) e1;
				temp.onScore();
 			}
 	}
 	
 	public boolean Update(double beat, double dt)
 	{
 		curRad = (float) (curRad * 1.18);
 		this.setRadius( (int) curRad);
 		if(getRadius() > endRad)
 			{
 				remove = true;
 				setRadius(endRad);
 			}
 		return remove;
 	}
 	
 }
