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
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.springrts.ai.AIFloat3;
 import com.springrts.ai.oo.Feature;
 import com.springrts.ai.oo.Map;
 import com.springrts.ai.oo.Resource;
 import com.springrts.ai.oo.UnitDef;
 
 
 // Class to search for Resource Extractors, sort by max. production
 class UnitPropertyResourceGenerator extends AGUnitProperty{
 	private Resource res;
 	UnitPropertyResourceGenerator(AGAI ai, Resource res) {
 		super(ai);
 		this.res=res;
 	}
 	
 	/* (non-Javadoc)
 	 * @see agai.UnitProperty#isInlist(com.springrts.ai.oo.UnitDef)
 	 */
 	@Override
 	public boolean isInlist(UnitDef unit) {
 		AGBuildTreeUnit tree = ai.getAGB().searchNode(unit);
 		if ((tree!=null) && ((tree.getBacklink()==null) || (tree.getBacklink().size()==0))) //unit can't be build! (filter commander out)
 			return false;
 		if (ai.getAGU().getProduction(unit, res)>0){
 				return true;
 		}
 		return false;
 	}
 
 	/* (non-Javadoc)
 	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
 	 */
 	public int compare(AGBuildTreeUnit o1, AGBuildTreeUnit o2) {
 		double a=0, b=0;
 		UnitDef u1=o1.getUnit();
 		UnitDef u2=o2.getUnit();
 		a=a+ai.getAGU().getProduction(u1, res);
 		b=b+ai.getAGU().getProduction(u2, res);
 		if (a<b)
 			return 1;
 		if (a>b)
 			return -1;
 		return 0;
 	}
 
 }
 class AGTaskBuildResource extends AGTask{
 	private Resource res;
 	AGTaskBuildResource(AGAI ai, Resource res) {
 		super(ai);
 		this.res=res;
 	}
 
 	@Override
 	public void solve() {
 		ai.getAGT().getBuildResource().solve(this);
 	}
 	
 	/* (non-Javadoc)
 	 * @see agai.AGTask#solveFailed()
 	 */
 	@Override
 	public void solveFailed(){
 		//FIXME building failed, try to build other building or so..
 		ai.msg("retrying");
 		setStatusIdle();
 	}
 	
 	@Override
 	public String toString(){
 		return "AGTaskBuildResource ";
 	}
 
 	public Resource getRes() {
 		return res;
 	}
 
 	public int getResIdx(){
 		return res.getResourceId();
 	}
 
 }
 
 /**
  * The Class AGTBuildResource.
  */
 public class AGTBuildResource extends AGTaskManager {
 	
 	/** The list with units to build resource, the first id is the resid, the secound for the units sorted by priority to built (best units first) */
 	protected List <List <AGBuildTreeUnit>> list;
 
 	/**
 	 * Instantiates a new aGT build AGTBuildResource.
 	 * 
 	 * @param ai the ai
 	 */
 	AGTBuildResource(AGAI ai) {
 		super(ai);
 		this.map=ai.getClb().getMap();
 		List <Resource> res=ai.getClb().getResources();
 		list=new ArrayList  <List <AGBuildTreeUnit>>();
 		for (int i=0; i<res.size(); i++){
 			initializeSpots(res.get(i));
 			list.add(i,ai.getAGF().Filter(new UnitPropertyResourceGenerator(ai, res.get(i))));
 		}
 		List<Feature> f=ai.getClb().getFeatures();
 		for (int i=0; i<f.size(); i++){ //FIXME: Engine issue, adds geothermal spots to PoI
 			if (f.get(i).getDef().isGeoThermal()){
 				ai.getAGP().add(f.get(i).getPosition(), ai.getEnergy().getResourceId());
 //				ai.drawPoint(f.get(i).getPosition(),"Pos " + i + ai.getEnergy().getName());
 			}
 		}
 		for (int i=0; i<list.size();i++){
 			for (int j=0; j<list.get(i).size(); j++){
 				UnitDef u=list.get(i).get(j).getUnit();
 				ai.msg(i +" "+u.getName() +"\tPrice: "+ai.getAGU().getTotalPrice(u)+"\t" +u.getHumanName() + "\t" + ai.getAGU().getProduction(u, res.get(i)) );
 			}
 		}
 	}
 
 	/** The map. */
 	private Map map = null;
 	
 	/**
 	 * Initialize spots.
 	 * 
 	 * @param res the res
 	 */
 	private void initializeSpots(Resource res){
 		ai.msg("initializeSpots Function "+ai);
 		List<AIFloat3> spots=map.getResourceMapSpotsPositions(res);
 		for(int i=0; i<spots.size(); i++ ){
 			spots.get(i).y=map.getElevationAt(spots.get(i).x, spots.get(i).z); //FIXME this is a engine-bug workaround
 //			ai.drawPoint(spots.get(i),"Pos " + i + res.getName());
 			ai.getAGP().add(spots.get(i), res.getResourceId());
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see agai.AGTaskManager#solve(agai.AGTask)
 	 */
 	@Override
 	public void solve(AGTask task) {
 		UnitDef unit=null;
 		AGUnit builder=null;
 		AGPoI poi=null;
 		AIFloat3 pos=null; 
 		AGTaskBuildResource t=(AGTaskBuildResource) task;
 		int min=AGAI.minDistance;
 
 		List <AGBuildTreeUnit> tmp=list.get(t.getResIdx());
 		int radius=100; //default radius to search for buildings
 		for(int i=0; i<tmp.size(); i++){ //search from units that fits best to the worst.. skip when unit can be built and enough resources found
 			builder=ai.getAGU().getBuilder(tmp.get(i).getUnit());
 			if (builder!=null){ //unit can be built! :-)
 				poi=ai.getAGP().getNearestFreePoi(builder.getPos(), t.getRes().getResourceId());
 				unit=tmp.get(i).getUnit();
 				if ((unit.getExtractsResource(t.getRes())>0) || unit.isNeedGeo()){ //unit needs spot to be built
 					if (poi==null) //no point found to build, next building
 						continue;
 					pos=poi.getPos();
 					radius=Math.round(ai.getClb().getMap().getExtractorRadius(t.getRes()));
 					if (unit.isNeedGeo()){
 						if (radius==0)
 							radius=AGAI.searchDistance;
 						min=0; //the only way to build on geo spots (?)
 					}
 					pos=builder.canBuildAt(pos, unit, radius, min);
 					if (pos==null){
 						ai.msg("Can't build here!");
 						continue;
 					}else{
 						ai.msg("Can build here!");
 						break;
 					}
 				}else { //doesn't need spot, build at next point to builder
 					ai.msg("No spot needed");
 					pos=builder.canBuildAt(pos, unit, radius, 2);
 					if (pos==null) //can't build, next unit
 						continue;
 					pos=null; //delete pos, because builder could have already a task and is moving
 				}
 				Resource res=ai.enoughResourcesToBuild(unit);
 				if (res==null){ //no resource is missing, building is ok!
 					break;
 				}
 			}
 		}
 		if ((unit!=null)&&(builder!=null)){ //unit with builder found, build it!
			if (!builder.isIdle())
				return;
 			if (poi!=null){ //build only one time at a spot
 				poi.setBuilt(true);
 			}
 			ai.msg("Sending command to build unit");
 			AGTask buildtask=new AGTaskBuildUnit(ai, unit, pos, radius, 2);
 			task.setStatusFinished();//task is done, we are building a resource producing unit
 			buildtask.setPoi(poi);
 			ai.getAGT().addTask(buildtask);
 		}else{
 			if (builder==null)
 				ai.msg("No builder found ");
 			if (unit==null)
 				ai.msg("No resource producing unit found");
 			task.setStatusIdle(); //retry, because we need resources
 		}
 	}
 }
