 package data.scripts.world;
 
 import com.fs.starfarer.api.campaign.*;
 import data.scripts.world.BaseSpawnPoint;
 
 public class GeduneSpawnPoint extends BaseSpawnPoint {
 
 	public GeduneSpawnPoint(SectorAPI sector, LocationAPI location, float daysInterval,
 			int maxFleets, SectorEntityToken anchor) {
 		super(sector, location, daysInterval, maxFleets, anchor);
 	}
    
 	@Override
 	protected CampaignFleetAPI spawnFleet() {
 		float r = (float) Math.random();
		if (       (r -= .5f) < 0) {
 			return raidFleet("scout");
		} else if ((r -= .4f) < 0) {
 			return raidFleet("longRangeScout");
		} else if ((r -= .3f) < 0) {
 			return raidFleet("raiders");
 		} else if ((r -= .2f) < 0) {
			return Math.random() > 0.3 ? raidFleet("libers"): defendFleet("libers");
 		} else if ((r -= .2f) < 0) {
         return l33tFleet(Math.random() > 0.1 ? raidFleet("elite"): defendFleet("elite"));
 		} else {
			return Math.random() > 0.1 ? raidFleet("siege"): defendFleet("siege");
 		}
 	}
 
 	private CampaignFleetAPI raidFleet(String fleetID) {
 		CampaignFleetAPI fleet = getSector().createFleet("gedune", fleetID);
 		getLocation().spawnFleet(getAnchor(), 0, 0, fleet);
 		fleet.setPreferredResupplyLocation(getAnchor());
 		fleet.addAssignment(FleetAssignment.RAID_SYSTEM, null, 10);
 		fleet.addAssignment(FleetAssignment.DELIVER_RESOURCES, getAnchor(), 100);
 		fleet.addAssignment(FleetAssignment.DELIVER_SUPPLIES, getAnchor(), 100);
 		fleet.addAssignment(FleetAssignment.DELIVER_FUEL, getAnchor(), 100);
 		fleet.addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, getAnchor(), 1000);
 		return fleet;
 	}
 
 	private CampaignFleetAPI defendFleet(String fleetID) {
 		CampaignFleetAPI fleet = getSector().createFleet("gedune", fleetID);
 		getLocation().spawnFleet(getAnchor(), 0, 0, fleet);
 		fleet.setPreferredResupplyLocation(getAnchor());
 		fleet.addAssignment(FleetAssignment.DEFEND_LOCATION, getAnchor(), 15);
 		fleet.addAssignment(FleetAssignment.DELIVER_RESOURCES, getAnchor(), 100);
 		fleet.addAssignment(FleetAssignment.DELIVER_SUPPLIES, getAnchor(), 100);
 		fleet.addAssignment(FleetAssignment.DELIVER_FUEL, getAnchor(), 100);
 		fleet.addAssignment(FleetAssignment.DEFEND_LOCATION, getAnchor(), 15);
 		fleet.addAssignment(FleetAssignment.DELIVER_RESOURCES, getAnchor(), 100);
 		fleet.addAssignment(FleetAssignment.DELIVER_SUPPLIES, getAnchor(), 100);
 		fleet.addAssignment(FleetAssignment.DELIVER_FUEL, getAnchor(), 100);
 		fleet.addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, getAnchor(), 1000);
 		return fleet;
 	}
 
 	private CampaignFleetAPI l33tFleet(CampaignFleetAPI fleet) {
 		CargoAPI fcargo = fleet.getCargo();
 		int t = fcargo.getTotalCrew();
 		double ratio = Math.random() * 0.2 + 0.1;
 		fcargo.removeCrew(CargoAPI.CrewXPLevel.REGULAR, t);
 		fcargo.addCrew(CargoAPI.CrewXPLevel.ELITE, (int) (t * ratio));
 		fcargo.addCrew(CargoAPI.CrewXPLevel.VETERAN, (int) (t * (0.25 - ratio / 2)));
 		fcargo.addCrew(CargoAPI.CrewXPLevel.REGULAR, (int) (t * (0.75 - ratio / 2)));
 		return fleet;
 	}
 }
