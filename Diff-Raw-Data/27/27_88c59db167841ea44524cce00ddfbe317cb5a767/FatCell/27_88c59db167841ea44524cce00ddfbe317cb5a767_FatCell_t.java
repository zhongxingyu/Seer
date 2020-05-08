 ////////////////////////////////////////////////////////////////////
 //////////////////////////////////////////////////////////////////
 //
 //Game Engine and game by Daniel Waine and Raphael Chappuis 
 //
 //for Mobile and Web Games Development
 //
 //Banner IDs B00226804 - B00218930
 //
 ////////////////////////////////////////////////////////////////////////
 
 package game.CellType;
 
 import android.content.res.Resources;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import events.Vector2D;
 import game.Entity;
 import game.GameModel;
import game.GameScore;
 import game.EntityMouvementBehavior.FatCellMovement;
 import game.EntityMouvementBehavior.FatCellRoll;
 import game.EntityMouvementBehavior.CellStop;
 import gameEngine.GameObject;
 import gameEngine.ParserView;
 import game.EntityMouvementBehavior.*;
 public class FatCell extends Cell implements GameObject
 {
 	private boolean onWall = false;
 	Vector2D vfatPair = null;
 	Cell collides = null;
 	private int sickColor;
 	private Paint richColor;;
 	private boolean oxyCell;
 	FatCellMovement normalMove = null;
 	private final int DEF_RAD;
 	private final int MAX_RAD;
 	private final int MIN_RAD;
 	
 	
 	public FatCell (int startX, int startY, Vector2D istart2d, int rad, Resources viewRes, GameModel refModel)
 	{
 		super(startX, startY, istart2d, rad, viewRes, refModel);
 		normalMove = new FatCellMovement((int)istart2d.x, (int)istart2d.y, startX, startY);
 		DEF_RAD = rad;
 		MAX_RAD = (int) (refModel.getGameHeight() * 0.14);
 		MIN_RAD = (int) (refModel.getGameHeight() * 0.02);
 		this.setMouvementBehavior( normalMove );
 		getEntityColor().setColor(Color.YELLOW);
 		this.setType(Entity.FATCELL);
 		sickColor = Color.rgb(65, 111, 28);
 		richColor = new Paint(Color.rgb(38, 26, 190));
 		setOxyCell(false);
 	}
 	public void Collide(Entity e1)
 	{
 		//no detecting if the a whole row or whole col is full of fat. Creating endless collision (bad!)
 		//This means pushing on the opposite axis!
 		if(e1.getType() == Entity.FATCELL)
 		{
 			if(collides == null)
 			{
 				FatCell temp = (FatCell)e1;
 				if(this.getMouvementBehavior().getMove() == EntityMouvementBehavior.FATCELLROLL)
 						this.HitWall();
 				else if(e1.getMouvementBehavior().getMove() == EntityMouvementBehavior.FATTHROW)
 				{}
 				else if(this.onWall == temp.onWall)
 				{
 					super.Collide(e1);
 				}
 				else if(!this.onWall)
 				{
 					//we may need to roll around this object
 					collides = (FatCell)e1;
 					Vector2D tempV = new Vector2D( ( this.getPosX() - e1.getPosX() ), ( this.getPosY() - e1.getPosY() ) );
 					FatCellRoll tempMove = new FatCellRoll( (int)e1.getPosX(), (int)e1.getPosY(), tempV );
 					this.setMouvementBehavior(tempMove);
 				}
 			}
 			else if(e1 != collides)
 				this.HitWall();
 		}
 		else if(e1.getType() == Entity.REDCELL)
 		{
 			//clamp to val?
 			this.setRadius((int) (getRadius() + ( e1.getRadius() * 0.8) ));
 		}
 		else if(e1.getType() == Entity.PLATELET)
 		{
 			this.setRadius((int) (getRadius() - ( e1.getRadius() * 0.8) ));
 		}
 		else if(e1.getType() == Entity.CELLPOP)
 		{
 			if(!remove)
 			{
 				model.orderCell().order(CELLPOP, getRadius(), (int)posX, (int)posY);
 				this.remove = true;
 			}			
 		}
 		else if(e1.getType() == Entity.VIRUS)
 		{
 			if(onWall)
 			{
 				
 			}
 		}
 		else if(e1.getType() == Entity.OXYGEN)
 		{
 			oxyCell = true;
 		}
 	}
 	
 	public boolean Update(double beat, double dt)
 	{
 		if(this.getRadius() > MAX_RAD)
 		{
 			//reset radius + addnew fat
 			model.orderCell().order(Entity.FATCELL, DEF_RAD, (int)getPosX(), (int)getPosY());
 			model.PlaySlop();
 			model.slowerHeart();
 			this.setRadius(DEF_RAD);
 			onWall = false;
 		}
 		else if(this.getRadius() < MIN_RAD)
 			return true;
 		
 		if(!onWall)
 		{
 			this.getMouvementBehavior().update(this, beat, dt);
 			if(this.getPosX() > (ParserView.windowWidth - this.getRadius()))
 			{
 				//change state to on wall and change pos to wall plus rad
 				//but on the correct axis and with rad point the right way
 				HitWall();
 				this.setPosX(ParserView.windowWidth - this.getRadius());
 			}
 			else if(this.getPosX() < (0 + this.getRadius()))
 			{
 				HitWall();
 				this.setPosX(0 + this.getRadius());
 			}
 			else if(this.getPosY() > (ParserView.windowHeight - this.getRadius()))
 			{
 				HitWall();
 				this.setPosY(ParserView.windowHeight - this.getRadius());
 			}
 			else if(this.getPosY() < (0 + this.getRadius()))
 			{
 				HitWall();
 				this.setPosY(0 + this.getRadius());
 			}
 		}
 		else{
 			if( this.getPosX() > (ParserView.windowWidth + radius) ||
 					this.getPosX() < (0 - radius) ||
 					this.getPosY() > (ParserView.windowHeight + radius) ||
 					this.getPosY() < (0 - radius) )
 					{
 						OutOfBounds();
 					}
 			if(lapVec.x > 0 || lapVec.y > 0)
 			{
 				this.ResolveCol();
 				lapVec.x = 0; lapVec.y = 0;
 			}
 		}
 		return remove;
 	}
 	
 	public void FatKick(int objX, int objY, Cell kick)
 	{
 		if(getMouvementBehavior().getMove() == EntityMouvementBehavior.CELLSTOP)
 			{
 			//send fatcell in direction of player
 				int randX = model.rand.Random(model.heartRef.getRadius() * 2) - model.heartRef.getRadius();
 				int randY = model.rand.Random(model.heartRef.getRadius() * 2) - model.heartRef.getRadius();
 				this.setEntityColor(sickColor);
 				this.setMouvementBehavior(new FatCellMovement(randX, randY,(int)posX, (int)posY,objX, objY) );
 			}
 	}
 	
	public void onScore()
	{
		if(oxyCell)
			model.scoreCal.regEvent(GameScore.FAT_BUST_RICH);
		else
			model.scoreCal.regEvent(GameScore.FAT_BUST);
		remove = true;
	}
	
 	public void Draw(Canvas canvas)
 	{
 		if(oxyCell)
 			canvas.drawCircle((int)this.getPosX(), (int)this.getPosY(), getRadius()+3, richColor);
 		canvas.drawCircle((int)this.getPosX(), (int)this.getPosY(), getRadius(), entityColor);
 	}
 	
 	protected void OutOfBounds()
 	{	
 		if( this.getPosX() < (0) )
 			this.setPosX(getPosX() + 2);
 		else if( this.getPosX() > (ParserView.windowWidth) )
 			this.setPosX( getPosX() - 2 );
 		if( this.getPosY() < (0) )
 			this.setPosY( (getPosY() + 2) );
 		else if( this.getPosY() > (ParserView.windowHeight) )
 			this.setPosY((getPosY() - 2));
 		
 		if(!onWall)
 			HitWall();
 	}
 	
 	private void HitWall()
 	{
 		collides = null;
 		onWall = true;
 		if(getMouvementBehavior().getMove() != EntityMouvementBehavior.CELLSTOP)
 			this.setMouvementBehavior(new CellStop(EntityMouvementBehavior.CELLSTOP));
 	}
 	
 	public boolean getWall() { return onWall; }			public void setWall(boolean val) { onWall = val; }
 	public boolean isOxyCell() {
 		return oxyCell;
 	}
 	public void setOxyCell(boolean oxyCell) {
 		this.oxyCell = oxyCell;
 	};
 }
