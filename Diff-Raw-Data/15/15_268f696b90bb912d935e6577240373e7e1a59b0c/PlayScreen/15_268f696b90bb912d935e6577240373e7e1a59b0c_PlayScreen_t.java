 package com.voracious.dragons.client.screens;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseEvent;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 
 import com.voracious.dragons.client.Game;
 import com.voracious.dragons.client.graphics.Screen;
 import com.voracious.dragons.client.graphics.Sprite;
 import com.voracious.dragons.client.graphics.ui.Text;
 import com.voracious.dragons.client.towers.Castle;
 import com.voracious.dragons.client.towers.Tower;
 import com.voracious.dragons.client.units.Unit;
 import com.voracious.dragons.client.utils.InputHandler;
 import com.voracious.dragons.common.Turn;
 import com.voracious.dragons.common.Vec2D;
 
 public class PlayScreen extends Screen {
 
     public static final int WIDTH = 2160;
     public static final int HEIGHT = 1440;
     private static Logger logger = Logger.getLogger(Game.class);
     private Sprite background;
     private Castle p1Cast,p2Cast;
     private List<Tower> towers;
     private List<Unit> units;
     boolean inMenu=false;
     boolean inPathMode=false;
     boolean inUnitMode=false;
     boolean inTowerMode=false;
     
     Turn player,other;
     
     Vec2D.Short temp;
     
     public PlayScreen() {
         super(HEIGHT, WIDTH);
         
         InputHandler.registerButton(KeyEvent.VK_W);
         InputHandler.registerButton(KeyEvent.VK_A);
         InputHandler.registerButton(KeyEvent.VK_S);
         InputHandler.registerButton(KeyEvent.VK_D);
         InputHandler.registerScreen(this);
         
         this.setBackground(new Sprite("/backgroundLarge.png"));
         
         
         player=new Turn(0,0);
         
         this.setP1Cast(new Castle());
         this.getP1Cast().setX(0);
         this.getP1Cast().setY(background.getHeight() - Castle.height);
         
         this.setP2Cast(new Castle());
         this.getP2Cast().setX(background.getWidth() - Castle.width);
         this.getP2Cast().setY(0);
     }
 
     @Override
     public void render(Graphics2D g) {
     	this.getBackground().draw(g, 0, 0);
     	
     	this.getP1Cast().draw(g);
     	this.getP2Cast().draw(g);
     	
     	//Block used for Paths
     	{
     		List<List<Vec2D.Short>>outer=player.getPaths();
     		Iterator<List<Vec2D.Short>>outIt=outer.iterator();
     		Vec2D.Short last=null;
     		while(outIt.hasNext()){
     			List<Vec2D.Short>inner=outIt.next();
     			Iterator<Vec2D.Short>inIt=inner.iterator();
     			while(inIt.hasNext()){
     				Vec2D.Short tmp=inIt.next();
     				g.drawOval(tmp.getx()-8, tmp.gety()-8, 16, 16);
     				if(last!=null){
     					g.drawLine(last.getx(), last.gety(), tmp.getx(), tmp.gety());
     				}
     				last=tmp;
     			}
     		}
     	}
     	//Block used for Towers
     	//NOTE:Entire block should be replaced with drawing towers from the tower list when turn
     	//execution is implemented. I will comment out that code below this block.
     	{
     		List<List<Vec2D.Short>>outer=player.getTowers();
     		Iterator<List<Vec2D.Short>>outIt=outer.iterator();
     		while(outIt.hasNext()){
     			List<Vec2D.Short>inner=outIt.next();
     			Iterator<Vec2D.Short>inIt=inner.iterator();
     			while(inIt.hasNext()){
     				Vec2D.Short tmp=inIt.next();
     				g.drawRect(tmp.getx()-8, tmp.gety()-8, 16, 16);
     			}
     		}
     	}
     	/* This is the proper future code for drawing the towers.
     	for(Tower t : towers){
     		t.draw(g);
     	}
     	*/
     	
     	/*Future code for drawing units
     	for(Unit u : units){
     		u.draw(g);
     	}
     	*/
     	
     	g.translate(this.getOffsetx(), this.getOffsety());
     	
     	if(inMenu){
     		g.setColor(Color.BLACK);
     		g.fillRect(0, Game.HEIGHT-15, Game.WIDTH, 15);
     		Text t = new Text("t");
     		t.setLocation(0, Game.HEIGHT-t.getHeight());
     		t.setColor(Color.WHITE);
     		if(inPathMode)
     			t.setText("You are in path mode");
     		else if(inTowerMode)
     			t.setText("You are in tower mode");
     		else if(inUnitMode)
     			t.setText("You are in unit mode");
     		t.draw(g);
     	}
     }
     
 
     @Override
     public void tick() {
         if(InputHandler.isDown(KeyEvent.VK_W)){
             this.translate(0, -3);
         }else if(InputHandler.isDown(KeyEvent.VK_S)){
             this.translate(0, 3);
         }
         
         if(InputHandler.isDown(KeyEvent.VK_A)){
             this.translate(-3, 0);
         }else if(InputHandler.isDown(KeyEvent.VK_D)){
             this.translate(3, 0);
         }
     }
     
     
     @Override
 	public void keyPressed(KeyEvent e) {
     	if(e.getKeyCode()==KeyEvent.VK_P){
     		this.inPathMode=!this.inPathMode;
    		this.inMenu=true;
     		this.inTowerMode=false;
     		this.inUnitMode=false;
     	}
     	else if(e.getKeyCode()==KeyEvent.VK_T){
     		this.inTowerMode=!this.inTowerMode;
    		this.inMenu=true;
     		this.inPathMode=false;
     		this.inUnitMode=false;
     	}
     	else if(e.getKeyCode()==KeyEvent.VK_N){
     		this.inMenu=false;
     		this.inPathMode=false;
     		this.inTowerMode=false;
     		this.inUnitMode=false;
     	}
     	else if(e.getKeyCode()==KeyEvent.VK_U){
     		this.inUnitMode=!this.inUnitMode;
    		this.inMenu=true;
     		this.inPathMode=false;
     		this.inTowerMode=false;
     	}
 	}
     
 
 
 	@Override
 	public void keyReleased(KeyEvent e) {
 	}
 
 	@Override
     public void mouseMoved(MouseEvent e) {
         if(InputHandler.isDown(InputHandler.VK_MOUSE_2)){
             this.translate(InputHandler.getChangeInMouse());
         }
     }
     
     @Override
     public void mousePressed(MouseEvent e){
         if(InputHandler.isDown(InputHandler.VK_MOUSE_2)){
             InputHandler.setMouseMoveable(false);
         }
         
         if(InputHandler.isDown(InputHandler.VK_MOUSE_1) &&this.inPathMode){
         	//TODO add another button to cycle through the different units
         	temp = new Vec2D.Short((short)(InputHandler.getMousePos().x + this.getOffsetx()),
         			               (short)(InputHandler.getMousePos().y + this.getOffsety()));
         	
         	if(temp.x <= PlayScreen.WIDTH && temp.x >= 0 && temp.y <= PlayScreen.HEIGHT && temp.y >= 0){
         		player.addNode((byte) 0, temp);
         	}
         	
         }
         else if(InputHandler.isDown(InputHandler.VK_MOUSE_1) && this.inTowerMode ){
         	//TODO add buttons to cycle through towers
         	temp = new Vec2D.Short((short) (InputHandler.getMousePos().x + this.getOffsetx()),
         			               (short) (InputHandler.getMousePos().y + this.getOffsety()));
         	
         	if(temp.x <= PlayScreen.WIDTH && temp.x >= 0 && temp.y <= PlayScreen.HEIGHT && temp.y >= 0){
         		player.createTower((byte)0, temp);
         	}
         }
     }
     
     @Override
     public void mouseReleased(MouseEvent e){
         if(!InputHandler.isDown(InputHandler.VK_MOUSE_2)){
             InputHandler.setMouseMoveable(true);
         }
     }
 
 	/**
 	 * @return the background
 	 */
 	public Sprite getBackground() {
 		return background;
 	}
 
 	/**
 	 * @param background the background to set
 	 */
 	public void setBackground(Sprite background) {
 		this.background = background;
 	}
 
 	/**
 	 * @return the logger
 	 */
 	public static Logger getLogger() {
 		return logger;
 	}
 
 	/**
 	 * @param logger the logger to set
 	 */
 	public static void setLogger(Logger logger) {
 		PlayScreen.logger = logger;
 	}
 
 	/**
 	 * @return the p1Cast
 	 */
 	public Castle getP1Cast() {
 		return p1Cast;
 	}
 
 	/**
 	 * @param p1Cast the p1Cast to set
 	 */
 	public void setP1Cast(Castle p1Cast) {
 		this.p1Cast = p1Cast;
 	}
 
 	/**
 	 * @return the p2Cast
 	 */
 	public Castle getP2Cast() {
 		return p2Cast;
 	}
 
 	/**
 	 * @param p2Cast the p2Cast to set
 	 */
 	public void setP2Cast(Castle p2Cast) {
 		this.p2Cast = p2Cast;
 	}
 }
