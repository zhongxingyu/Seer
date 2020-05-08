 package se.chalmers.towerdefence.controller;
 
 import java.util.Iterator;
 import java.util.List;
 
 import se.chalmers.towerdefence.gui.IView;
 import se.chalmers.towerdefence.gui.MonsterView;
 import se.chalmers.towerdefence.gui.ProjectileView;
 import se.chalmers.towerdefence.gui.TowerView;
 import se.chalmers.towerdefence.model.interfaces.GameBoardObject;
 import se.chalmers.towerdefence.model.monsters.AbstractMonster;
 import se.chalmers.towerdefence.model.projectiles.AbstractProjectile;
 import se.chalmers.towerdefence.model.towers.AbstractTower;
/**
 * A Class to find out if an GameBoardObject is needed to be drawn
 * @author Jonathan
 *
 */

 
 public class ViewUtil {
 
 	public static void drawMonsterOnGameBoard(List<IView> iViews, List<GameBoardObject> objects, int squareHeight, int squareWidth){
 		for(Object o : objects){
 			boolean temp=true;
 			for(IView iV : iViews){
 				if(o==iV.getObject()){
 					temp=false;
 				}
 			}
 			if(temp){
 				if(o instanceof AbstractProjectile){
 					iViews.add(new ProjectileView((AbstractProjectile) o, squareWidth/2, squareHeight/2));
 				}else if(o instanceof AbstractMonster){
 					iViews.add(new MonsterView((AbstractMonster) o, (int)(squareWidth*0.8), (int)(squareHeight*0.8)));
 				}else if(o instanceof AbstractTower){
 					iViews.add(new TowerView((AbstractTower) o, squareWidth, squareHeight));
 				}
 				
 			}else{
 				temp=true;	
 			}
 		}
 		
 		for(Iterator<IView> it = iViews.iterator(); it.hasNext();){
 			IView p = it.next();
 			if(p.exists()){
 				p.draw();
 			}else{
 				it.remove();
 			}
 		}
 	}
 }
