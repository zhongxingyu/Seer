 package android.topdown.game;
 
 import android.gameengine.icadroids.objects.GameObject;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 
 public class Infobar extends GameObject {
 	
 	private Paint pt = new Paint();
 	private Paint paintLine = new Paint();
	private double speed,xspeed,yspeed,xpos,ypos; 
	private float zoomFactor;
 	private int viewportX, viewportY;
 	private Player player;
 	private int screenwidth,screenheight;
 
 	public Infobar(Player player){
 		pt.setColor(Color.BLACK);
 		pt.setTextSize(18);
 		this.player = player;
 		paintLine.setStrokeWidth(8);
 	}
 	
 	@Override
 	public void update() {
 		super.update();
		int r = 0, g = 0;
 		if(player.getHp()>100){
 			paintLine.setARGB(255, 255,255, 255);
 		}
 		else{
 			paintLine.setARGB(255, (int)Math.round(player.getHp()), 
 					(int)Math.round(player.getHp()*2.55), (int)Math.round(player.getHp()));
 		}
 		//TODO mischien de kleuren hier nog wat afstellen
 	}
 
 	@Override
 	public void drawGameObject(Canvas canvas) {
 		super.drawGameObject(canvas);
 		canvas.drawText("HP: ", (float)(viewportX+(screenwidth*.01)), (float)(viewportY+(screenheight*.25)), pt);
 		
 		canvas.drawLine((float)(viewportX+(screenwidth*.01)+(pt.getTextSize()*2.0)), 
 				(float)(viewportY+(screenheight*.24)), 
 				(float)(viewportX+(screenwidth*.01)+(pt.getTextSize()*2.0)+(player.getHp())), 
 				(float)(viewportY+(screenheight*.24)), paintLine);
 		
 		canvas.drawText("Pistol: "+player.getPistol().getAmmo()+"/"+player.getPistol().getAmmoMax(), 
 				(float)(viewportX+(screenwidth*.01)), (float)(viewportY+(screenheight*.25)+(pt.getTextSize()+5)), pt);
 		canvas.drawText("Shotgun: "+player.getShotgun().getAmmo()+"/"+player.getShotgun().getAmmoMax(), 
 				(float)(viewportX+(screenwidth*.01)), (float)(viewportY+(screenheight*.25)+(pt.getTextSize()*2+5)), pt);
 	}
	
	public void setSpeed(double speed,double xspeed,double yspeed, float xpos, float ypos){
		this.speed = speed;
		this.xspeed = xspeed;
		this.yspeed = yspeed;
		this.xpos = xpos;
		this.ypos = ypos;
	}
 
	public void setPort(float zoomFactor, int viewportX, int viewportY) {
		this.zoomFactor = zoomFactor;
 		this.viewportX = viewportX;
 		this.viewportY = viewportY;
 	}
 	public void setScreenSize(int height,int width){
 		this.screenheight = height;
 		this.screenwidth = width;
 	}
 }
