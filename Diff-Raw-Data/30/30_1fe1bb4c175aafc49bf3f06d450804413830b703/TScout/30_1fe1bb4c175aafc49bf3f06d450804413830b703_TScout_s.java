 /*
  * Copyright (C) 2009 Matthias Ableitner (http://abma.de/)
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package agai.task;
 
 import agai.AGAI;
 import agai.info.IPoI;
 import agai.manager.MScout;
 import agai.manager.Manager;
 import agai.unit.AGUnit;
 
 public class TScout extends Task {
 	public TScout(AGAI ai, Manager manager) {
 		super(ai, manager);
 	}
 
 	@Override
 	public void assign(AGUnit unit) {
 		ai.logDebug("");
 		((MScout) ai.getManagers().get(MScout.class)).incScouts();
 		execute(unit);
 	}
 
 	@Override
 	public String toString() {
 		return "TScout";
 	}
 
 	@Override
 	public void unassign(AGUnit unit) {
 		ai.logDebug("");
 		((MScout) ai.getManagers().get(MScout.class)).decScouts();
 	}
 
 	@Override
 	public void unitCommandFinished(AGUnit unit) {
 		ai.logDebug("");
 		execute(unit);
 	}
 
 	@Override
 	public void unitDestroyed(AGUnit unit) {
 		ai.logDebug("");
 		unassign(unit);
 	}
 	private IPoI destination=null;
 
 	@Override
 	public void execute(AGUnit unit) {
 		if (destination==null){
 			ai.logDebug("");
 			destination = ai.getInfos().getAGP().getNearestFreeScoutPoi(unit);
 			destination.setVisited(true);
 		}
 		if (ai.getInfos().getSectors().isPosInSec(unit.getPos(), destination.getSector())){ //unit is in sec, new destination!
 			ai.logDebug("");
 			destination.setVisited(true);
 			destination = ai.getInfos().getAGP().getNearestFreeScoutPoi(unit);
 			destination.setVisited(true);
 		}
 
/*		List<Unit> units = ai.getClb().getEnemyUnitsIn(unit.getPos(), unit.getLOS());
		if (units.size()>=0){ //enemy units in LOS, move to secure pos
			ai.msg("");
			for (int i=0; i<units.size(); i++){
				ai.getInfos().getSectors().addDanger(units.get(i));
			}
			ISector sec=ai.getInfos().getSectors().getSecureSector(unit.getPos(), 0);
			unit.moveTo(sec.getPos());
			return;
		}*/
 		if (destination == null) {
 			ai.logDebug("No point to scout found!");
 			return;
 		}
 		if (unit.canMoveTo(destination.getPos())){
 			ai.logDebug("moving to "+destination.getPos());
//			unit.setTask(new TSecureMove(ai, null, this, destination.getSector(), true, unit.getPos()));
 			unit.moveTo(destination.getPos());
 		}else{
 			ai.logDebug("Unit can't move to PoI");
 			ai.drawPoint(destination.getPos(), "");
 		}
 	}
 
 }
