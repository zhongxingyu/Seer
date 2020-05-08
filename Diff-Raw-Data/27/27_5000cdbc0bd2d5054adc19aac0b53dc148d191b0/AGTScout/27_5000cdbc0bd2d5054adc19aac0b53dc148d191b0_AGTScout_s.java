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
 
 package agai;
 
 import java.util.List;
 
 import com.springrts.ai.oo.UnitDef;
 
 // TODO: Auto-generated Javadoc
 /**
  * @author matze
  *
  */
 
 //Class to get all Scouts (Scouts are fast, cheap, invisble, can fly,  
 class UnitPropertyScout extends AGUnitProperty{
 
 	UnitPropertyScout(AGAI ai) {
 		super(ai);
 		properties.add(new AGUnitPropertyEvaluatorLosRadius(ai, 0.01f, this));
 		properties.add(new AGUnitPropertyEvaluatorSpeed(ai, 0.03f, this));
 		properties.add(new AGUnitPropertyEvaluatorPrice(ai, 0.99f, this));
 	}
 	public int compare(AGBuildTreeUnit o1, AGBuildTreeUnit o2) {
 		UnitDef u1=o1.getUnit();
 		UnitDef u2=o2.getUnit();
 		float a=0;
 		float b=0;
 		for(int i=0; i<properties.size(); i++){
 			a=a+properties.get(i).getNormValue(u1);
 			b=b+properties.get(i).getNormValue(u2);
 		}
 		if (a>b)
 			return 1;
 		else return 0;
 	}
 	public boolean isInlist(UnitDef unit){
 		if  (((unit.getSpeed()>0) && (unit.getLosRadius()>0))){
 			updateMinMax(unit); //This call is needed to "add" the list to the properties list
 			return true;
 		}
 		return false;
 	}
 }
 
 class AGTaskScout extends AGTask{
 	int i;
 	AGTaskScout(AGAI ai) {
 		super(ai);
 		i=0;
 	}
 
 	@Override
 	public void solve() {
 		ai.msg("");
 		ai.getAGT().getScout().solve(this);
 	}
 
 	@Override
 	public void unitCommandFinished(AGUnit unit){
 		ai.msg("");
		AGPoI p=ai.getAGP().getNearestFreePoi(unit.getPos(), AGPoIs.PoIAny);
 		if (p!=null){
 			ai.msg("moving to "+p.getPos().x +" " +p.getPos().z);
 			unit.moveTo(p.getPos());
 			p.setVisited(true);
		}else
 			ai.msg("No point to scout found!");
 	}
 
 	@Override
 	public void unitIdle(AGUnit unit){
 		ai.msg("");
 		unitCommandFinished(unit);
 	}
 	@Override
 	public void unitDestroyed(){
 		setStatusIdle();
 	}
 	public String toString(){
 		return "";
 	}
 }
 
 
 /**
  * The Class AGTScout.
  */
 public class AGTScout extends AGTaskManager{
 
 	/** The list. */
 	protected List <AGBuildTreeUnit> list;
 	
 	/**
 	 * Instantiates a new aG task scout.
 	 * 
 	 * @param ai the ai
 	 */
 	AGTScout(AGAI ai) {
 		super(ai);
 		list=ai.getAGF().Filter(new UnitPropertyScout(ai));
 		for (int i=0; i<list.size(); i++){
 			ai.msg(list.get(i).getUnit().getName() +"\t"+ ai.getAGU().getTotalPrice(list.get(i).getUnit()) );
 		}
 		
 	}
 	
 	/* (non-Javadoc)
 	 * @see agai.AGTaskManager#solve(agai.AGTask)
 	 */
 	@Override
 	public void solve(AGTask task) {
 		ai.msg("");
 		if (task.getUnit()==null){
 			for(int i=0; i<list.size(); i++){
				UnitDef unit=list.get(i).getUnit();
 				AGUnit u=ai.getAGU().getIdle(unit);
 				if (u!=null){ //unit to scout exists, assign task!
 					u.setTask(task);
 					task.setStatusWorking();
 					return;
 				}
 				AGUnit builder=ai.getAGU().getBuilder(unit);
 				if (builder!=null){
 					AGTask buildtask=new AGTaskBuildUnit(ai, unit, null, AGAI.searchDistance, AGAI.minDistance);
 					builder.setUnitCreatedTask(task);
 					task.setStatusWorking();
 					ai.getAGT().addTask(buildtask);
 					return;
 				}
 			}
 		}
		ai.msg("scouting!");
 	}
 }
