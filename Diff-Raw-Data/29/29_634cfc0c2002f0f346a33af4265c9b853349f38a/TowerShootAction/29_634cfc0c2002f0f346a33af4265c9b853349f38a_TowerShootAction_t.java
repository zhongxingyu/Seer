 package gameengine;
 
import gameengine.GameEngine.IHMMode;

 import java.awt.event.ActionEvent;
 
 import map.Mapping;
 import towers.Tower;
 
 public class TowerShootAction extends AnimationAction {
 	
 	Mapping map;
 	Tower tower;
 	int frozencounter;
 
 	public TowerShootAction(Mapping map, GameEngine frame, int timer, Tower tower) {
 		super(frame, timer);
 		this.map = map;
 		this.tower = tower;
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		
		if (map == null || tower == null || frame.getIhmmode() != IHMMode.InGame)
 		{
 			this.timer.stop();
 			return;
 		}
 		
 		if (tower.isFrozen())
 		{
 			frozencounter++;
 			if (frozencounter >= tower.getTotalFrozenTime ())
 			{
 				tower.setFrozen(false);
 				frozencounter = 0;
 			}
 			return;
 		}
 		
 		tower.calculateBestTarget (map);
 		
 		if (tower.getTarget() != null)
 		{
 			tower.getShooter().shoot(tower.getTarget(), map);
 			MoveBulletAction mba = new MoveBulletAction(tower.getShooter().getLastBullet(), this.frame, 100);
 			mba.run();
 		}
 		
 		if (tower.getTarget() != null && tower.getTarget().isDestructed())
 		{
 			tower.setTarget(null);
 		}
 	}
 
 }
