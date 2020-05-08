 package core;
 
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import javax.swing.JMenuItem;
 import javax.swing.JPopupMenu;
 
 import Objects.GameObject;
 
 public class ContextMenuModule{
 
 	public ArrayList<GameObject> actionableObjs = new ArrayList<GameObject>();
 	public HashMap<GameObject,JPopupMenu> menuHash=new HashMap<GameObject,JPopupMenu>();
 	private Component screen;
 	
 	
 	public void setScreen(RenderModule r){
 		screen = r.sc;
 	}
 	
 	
 	public void addActionable(GameObject o){
 		
 		actionableObjs.add(o);
 		JPopupMenu popup = new JPopupMenu();
 		for (MenuAction j: o.getContextActions()){
 			JMenuItem MenuItem = new JMenuItem(j.id);
 			MenuItem.addActionListener(j);
 			popup.add(MenuItem);
 			
 		}
 		//store in list
 		menuHash.put(o, popup);
 		
 	}
 	
 	public void clearPopup(){
 		
			Point a = new Point(1,1);
 			Dimension d = new Dimension (999,999);
 			Rectangle c = new Rectangle(a,d);
 			GameModule.renderer.addUpdatedArea(c);
 			
 	}
 	public void showPopup(Point e){
 		
 			clearPopup();
 			int xVal = (int) e.getX();
 			int yVal = (int) e.getY();
 			JPopupMenu menu = findGameObj(xVal,yVal);
 			//find game object at e.getX() e.getY()
 			//pull the menu from the list that matches that game object
 			menu.show(screen,xVal,yVal);
 		
 	}
 	public JPopupMenu findGameObj(int x,int y){
 		
 		int max = 0;
 		GameObject theObj = null;
 		for (GameObject i: actionableObjs){
 			Rectangle r = i.getDrawObj().getRect();
 			if(r.contains(x, y) && i.getHeight() >= max){
 				
 				theObj = i;
 			}
 		}
 		if(theObj != null){
 		JPopupMenu menu = menuHash.get(theObj);
 		return menu;
 		}
 		else{
 		return null;
 		}
 		
 		//placeholder function
 		//for each actionable game object, get the draw object
 		//if this x and y falls within that draw object,
 		//pick that game object if its height is > than 
 		//the current record. 
 		//find the game object in the array and match it to its actions
 		//return the action menu
 		
 	}
 
 
 	
 	
 }
