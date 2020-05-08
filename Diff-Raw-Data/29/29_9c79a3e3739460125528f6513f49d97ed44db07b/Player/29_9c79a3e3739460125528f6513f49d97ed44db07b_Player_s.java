 package com.warofcosmo.cosmo;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.KeyEvent;
 import java.util.ArrayList;
 
 
 
 public class Player extends AbstractEntity implements IPlayer{
 	
	private ArrayList<IKeyEvent> keyevents = new ArrayList();
	private int xMax = 100;
     private int yMax = 20;
     private int yMin = 60;
     private int xMin = 20;
 	public Player(Board brd) {
		super(brd,3,"s",".png",100,400,32,16);
 		
 		//_x=100;
 		//_y=400;
 		_speed=1;
 		AbstractKeyEvent kright = new KeyRight(this);
 		AbstractKeyEvent kleft = new KeyLeft(this);
 		AbstractKeyEvent kup = new KeyUp(this);
 		AbstractKeyEvent kdown = new KeyDown(this);
                 AbstractKeyEvent kspace = new KeySpace(this);
 					
 				  
 		addKeyEvent(kright);
 		addKeyEvent(kleft);
 		addKeyEvent(kup);
 		addKeyEvent(kdown);
 		addKeyEvent(kspace);
 
 		
 	}
 
 	@Override
 	public void move() {
         //Limits
         int _bWidth = _board.getSize().width;
         int _bHeight = _board.getSize().height;
         if(_dx>0 && getX()>=(_bWidth-xMax))
             _dx=0;
         if(_dy<0 && getY()<=yMax)
             _dy=0;
         if(_dy>0 && getY()>=(_bHeight-yMin))
             _dy=0;        
         if(_dx<0 && getX()<=xMin)
             _dx=0;
         //Limits
         
         _x = _x+_dx;
 		_y = _y+_dy;
 		
 	}	
 	
 	public void addKeyEvent(IKeyEvent ike){
 		keyevents.add(ike);
 	}
 	public void removeKeyEvent(IKeyEvent ike) {
 		keyevents.remove(ike);
 	}
 	
 	
 	@Override
 	public void actionPerformed(ActionEvent e) {
             System.out.println("Action");
             if(_dy<0)
             _gfx=_shipimg.get(1).getImage();
             if(_dy>0)
             _gfx=_shipimg.get(2).getImage();
             
 	}
 
 	@Override
 	public void keyPressed(KeyEvent e) {
 		int key = e.getKeyCode();
 		
 		for (int i=0; i < keyevents.size(); i++){
 			IKeyEvent ike = keyevents.get(i);
 			ike.pressAction(key);
 		}
 		
 	}
 	
 	@Override
 	public void keyReleased(KeyEvent e) {
 		int key = e.getKeyCode();
 		
 		for (int i=0; i < keyevents.size(); i++){
 			IKeyEvent ike = keyevents.get(i);
 			ike.releaseAction(key);
 		}
 	}
 	
 
 
 }
