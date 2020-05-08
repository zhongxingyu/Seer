 package gui;
 
 
 import java.awt.Component;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.util.ArrayList;
 
 import javax.swing.ImageIcon;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 import agents.Kit;
 
 public class GUI_Conveyor implements GUI_Component {
 	ImageIcon ConImage;//image of conveyor
 	int x,y, state, numberOfKitsOnStands;//coordinates
 	ArrayList<GUI_Kit> kits;
 	GUI_Kit shownkit;
 	
 	//Constructor
 	public GUI_Conveyor(){
 		kits = new ArrayList<GUI_Kit> ();
 		ConImage = new ImageIcon("gfx/Conveyor.png");
 		x =0;
 		y=0;
 		state = 0;
 		numberOfKitsOnStands =0; 
 	}
 
 	public void DoAddKitToConveyor(){
 		shownkit = kits.get(numberOfKitsOnStands);
 		state = 1;
 	}
 	
 	public void addKit(Kit k) {
 		GUI_Kit addkit = new GUI_Kit(k,130 ,74);
 		kits.add(addkit);
 	}
 	
 	
 	public GUI_Kit robotRemoveKit(){
 		state = 3;
 		GUI_Kit passkit = shownkit;
 		shownkit= null;
 		numberOfKitsOnStands++;
 		return passkit;
 	}
 	
 	public GUI_Kit checkKit(){
 		return shownkit;
 	}
 	
 	public void DoGetKitOut(Kit k){
 		shownkit = new GUI_Kit(k, 70,30);
 		state = 2;
 		numberOfKitsOnStands --;
 	}
 	
 	
 	
 	//the move function will have the requirements for actually moving the conveyor belt and the pallets
 	public void state1Move(){
 		if(shownkit!=null){//if there are kits to move, move them along the conveyor
 			if (shownkit.y<= 30){
 				shownkit.y += 2;
 			}
 			else{
 				if(shownkit.x>70){
 				shownkit.x -=2;
 				}
 				else if (numberOfKitsOnStands <2 & kits.size()>1){
 					DoAddKitToConveyor();
 				}
 				else
 				{
 					state = 0;
 				}
 			}
 		}
 	}
 	
 	public void state2Move(){
 		if(shownkit!=null){//if there are kits to move, move them along the conveyor
 			if(shownkit.x>-10){
 				shownkit.x -=2;
 				}
 				else
 				{
 					state = 0;
 					kits.remove(0);
 					shownkit = null;
 				}
 		}
 	}
 
 	
 	public void paintComponent(JPanel j, Graphics2D g){
 		ConImage.paintIcon((Component) j,(Graphics) g, x, y);
		if(shownkit!=null){
			shownkit.paintComponent(j, g);
		}
 		
 	}
 	public void paintComponent(Component j, Graphics2D g){
 		ConImage.paintIcon((Component) j,(Graphics) g, x, y);
 				
		if(shownkit!=null){
			shownkit.paintComponent((JPanel) j, g);
		}
 	}
     public void updateGraphics(){
     	if(state==1){
     		state1Move();
     	}
     	else if (state ==2){
     		state2Move();
     	}
     	else if (state ==3){
     		
     	}    	
     	
     }
 	
     public int getX(){
     	return shownkit.x;
     }
     
     public int getY(){
     	return shownkit.y;
     }
 	
     public ArrayList<GUI_Kit> getKits(){
     	return kits;
     }
 	
 }
