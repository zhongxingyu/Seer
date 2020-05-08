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
 	int x,y;//coordinates
 	ArrayList<GUI_Kit> kits;
 	
 	//Constructor
 	GUI_Conveyor(){
 		kits = new ArrayList<GUI_Kit> ();
 		ConImage = new ImageIcon("gfx/Conveyor.png");
 		x =0;
 		y=0;
 	}
 
 	void DoAddKit(Kit k) {
 		
 		kits.add(new GUI_Kit(k));
 
 	}
 	//the addGUIKit function will add a GUIKit to the stand
 	
 	void DoRemoveKit(Kit k) {
 		for(GUI_Kit gk : kits)
 		{
 			if(gk.kit == k)
 			{
 				kits.remove(gk);
 			}
 		}
 	}
 	
 	//the move function will have the requirements for actually moving the conveyor belt and the pallets
 	void moveKits(){
 		if(kits.size()!=0){//if there are kits to move, move them along the conveyor
 			for(GUI_Kit gk: kits){
				
 			}
 		}
 	}
 	
 	public void paintComponent(JPanel j, Graphics2D g){
 		ConImage.paintIcon((Component) j,(Graphics) g, x, y);
 		for (int i = 0 ; i< kits.size(); i++){
 			GUI_Kit paintkit = kits.get(i);
 			paintkit.paintComponent(j, g);
 			
 		}
 		
 	}
 	public void paintComponent(Component j, Graphics2D g){
 		ConImage.paintIcon((Component) j,(Graphics) g, x, y);
 		
 	}
     public void updateGraphics(){
     	
     }
 	
     public static void main(String[] args) {		
 		final GUI_Conveyor Con = new GUI_Conveyor();
 				
 		JFrame f = new JFrame() {
 			public void paint(Graphics g) {
 				Con.paintComponent((Component)this, (Graphics2D) g);
 			}
 		};
 		
 		f.pack();
 		f.setVisible(true);
 	}
 	
    public void getX(int number){
    	
    }
    
    public void getY(int number){
    	
    }
 	
 }
