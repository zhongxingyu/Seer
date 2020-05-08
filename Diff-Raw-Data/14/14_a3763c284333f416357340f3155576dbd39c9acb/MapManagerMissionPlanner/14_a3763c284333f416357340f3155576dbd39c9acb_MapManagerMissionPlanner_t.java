 package ca.etsmtl.capra.ai.implementations;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import ca.etsmtl.capra.MegaMainParameters;
 import ca.etsmtl.capra.MegaMainParameters.Param;
 import ca.etsmtl.capra.ai.services.AiServiceAbstract;
 import ca.etsmtl.capra.ai.services.MissionPlannerService;
 import ca.etsmtl.capra.datas.Goal;
 import ca.etsmtl.capra.datas.Position;
 import ca.etsmtl.capra.digitizer.datas.GPSData;
 import ca.etsmtl.capra.tools.megamapmanager.MapViewer;
 import ca.etsmtl.capra.tools.megamapmanager.Waypoint;
 import ca.etsmtl.capra.util.GenericGPS;
 
 public class MapManagerMissionPlanner extends AiServiceAbstract implements MissionPlannerService{
 	
 	@ServiceProperty(value=EditionPolicy.SERVICE_RESTART_NEEDED, 
 			description="Nom de la map a loader tel que defini dans le MapManager")
 	protected String MAP_NAME = "Test";
 	
 	private ArrayList<Waypoint> waypoints;
 	private boolean loaded;
 	
 	private ArrayList<Goal> positions = new ArrayList<Goal>();   
 
 	@Override
 	public void reset() {
 		System.out.println("Service.reset");
 		
 	}
 
 	@Override
 	public void resolveSummons() {
 		
 	}
 
 	@Override
 	public void resolveSummonsDone() {
 		
 	}
 
 	@Override
 	public void executeCycle() {
 		System.out.println("AiService.executeCycle");
 		
 	}
 
 	@Override
 	public void addGoal(Goal goal) {
 		System.out.println("MissionPlannerService.addGoal");
 		
 	}
 
 	@Override
 	public boolean loadMap(GPSData reference){
 
 		Object selectedMap = MegaMainParameters.getInstance().getParam(Param.MAP);
 	
 		String toLoad;
 		if(selectedMap != null)
 			 toLoad = (String)selectedMap;
 		else
 			toLoad = MAP_NAME;
 		
 		waypoints = (ArrayList<Waypoint>) new MapViewer().load("./maps/" + toLoad + MapViewer.MAP_FILE_EXTENSION);
 		
 		for(Waypoint point : waypoints)
 		{		
 			Position p;
 			//if(map.getCoordType() == Map.MapType.GPS){
				p = GenericGPS.getRelativePosition(reference, new GPSData(point.getPosition().getY(), point.getPosition().getX()));
 				//p = new Position(point.getPosition().getX(), point.getPosition().getY());
 			/*}else{
 				p = new Position((float)point.getX(), (float)point.getY());
 			}*/
 			
 			switch(point.getPriority())
 			{
 				default:
 				case High:
 					positions.add(new Goal(Goal.PRIORITY_MAIN, p));
 					break;
 				case Medium:
 					positions.add(new Goal(Goal.PRIORITY_SECONDARY, p));
 					break;
 				case Low:
 					positions.add(new Goal(Goal.PRIORITY_TERTIARY, p));
 					break;
 			}
 		}
 		
 		return true;
 	}
 
 	@Override
 	public List<Goal> getGoalsByPriority(int priority) {
 		ArrayList<Goal> goals = new ArrayList<Goal>();
 		for(Goal g : positions) {
 			if(g.getPriority() == priority) {
 				goals.add(g);
 			}
 		}
 		return goals;	
 	}
 
 	@Override
 	public List<Goal> getGoals() {
 		return positions;
 	}
 	
 }
