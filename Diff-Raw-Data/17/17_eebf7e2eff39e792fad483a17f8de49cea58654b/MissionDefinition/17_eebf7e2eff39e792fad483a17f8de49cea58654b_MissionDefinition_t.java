 package data.missions.gsp_bootyget;
 
 import com.fs.starfarer.api.fleet.FleetGoal;
 import com.fs.starfarer.api.fleet.FleetMemberType;
 import com.fs.starfarer.api.mission.FleetSide;
 import com.fs.starfarer.api.mission.MissionDefinitionAPI;
 import com.fs.starfarer.api.mission.MissionDefinitionPlugin;
 
 public class MissionDefinition implements MissionDefinitionPlugin {
 
 	public void defineMission(MissionDefinitionAPI api) {
 
 		// Set up the fleets
 		api.initFleet(FleetSide.PLAYER, "Pirate", FleetGoal.ATTACK, false);
 		api.initFleet(FleetSide.ENEMY, "HSS", FleetGoal.ESCAPE, true);
 
 		// Set a blurb for each fleet
 		api.setFleetTagline(FleetSide.PLAYER, "Admiral Steelheart's independent fleet");
 		api.setFleetTagline(FleetSide.ENEMY, "Hegemony escort forces");
 		
 		// These show up as items in the bulleted list under 
 		// "Tactical Objectives" on the mission detail screen
 		api.addBriefingItem("Disable the Transports");
 		api.addBriefingItem("Destroy the Escort Forces");
 		
 		// Set up the player's fleet
     api.addToFleet(FleetSide.PLAYER, "gsp_washi_elite", FleetMemberType.SHIP, "Bilge Pump", true);
     api.addToFleet(FleetSide.PLAYER, "gsp_pyuma_support", FleetMemberType.SHIP, "Scourge", false);
     api.addToFleet(FleetSide.PLAYER, "gsp_pyuma_support", FleetMemberType.SHIP, "Six-Pounder", false);
     api.addToFleet(FleetSide.PLAYER, "gsp_okami_assault", FleetMemberType.SHIP, "Grapnel", false);
     api.addToFleet(FleetSide.PLAYER, "gsp_okami_assault", FleetMemberType.SHIP, "Drunkard", false);
     api.addToFleet(FleetSide.PLAYER, "gsp_kitsune_assault", FleetMemberType.SHIP, "Swashbuckle", false);
     api.addToFleet(FleetSide.PLAYER, "gsp_kitsune_assault", FleetMemberType.SHIP, "Buccaneer", false);
     api.addToFleet(FleetSide.PLAYER, "gsp_kitsune_assault", FleetMemberType.SHIP, "Cutthroat", false);
 		api.addToFleet(FleetSide.PLAYER, "gsp_taka_wing", FleetMemberType.FIGHTER_WING, "Carousers", false);
 		api.addToFleet(FleetSide.PLAYER, "gsp_taka_wing", FleetMemberType.FIGHTER_WING, "Wenches", false);
 		api.addToFleet(FleetSide.PLAYER, "gsp_taka_wing", FleetMemberType.FIGHTER_WING, "Strumpets", false);
 		api.addToFleet(FleetSide.PLAYER, "gsp_hayabusa_wing", FleetMemberType.FIGHTER_WING, "Lubber", false);
 		api.addToFleet(FleetSide.PLAYER, "gsp_hayabusa_wing", FleetMemberType.FIGHTER_WING, "Interlopers", false);
 		api.addToFleet(FleetSide.PLAYER, "gsp_hayabusa_wing", FleetMemberType.FIGHTER_WING, "Brethren", false);
 		api.addToFleet(FleetSide.PLAYER, "gsp_hyou_wing", FleetMemberType.FIGHTER_WING, "Bilge Rats", false);
 		api.addToFleet(FleetSide.PLAYER, "gsp_hyou_wing", FleetMemberType.FIGHTER_WING, "Barnacles", false);
 		
 		// The Admiral is Essential
 		api.defeatOnShipLoss("Bilge Pump");
 		
 		// Set up the enemy fleet
 		api.addToFleet(FleetSide.ENEMY, "hammerhead_Balanced", FleetMemberType.SHIP, false);
 		api.addToFleet(FleetSide.ENEMY, "hammerhead_Balanced", FleetMemberType.SHIP, false);
 		api.addToFleet(FleetSide.ENEMY, "hound_Assault", FleetMemberType.SHIP, false);
 		api.addToFleet(FleetSide.ENEMY, "hound_Assault", FleetMemberType.SHIP, false);
 		api.addToFleet(FleetSide.ENEMY, "lasher_CS", FleetMemberType.SHIP, false);
 		api.addToFleet(FleetSide.ENEMY, "lasher_CS", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "sunder_CS", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "wolf_CS", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "wolf_CS", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "condor_FS", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "condor_FS", FleetMemberType.SHIP, false);
 		api.addToFleet(FleetSide.ENEMY, "wasp_wing", FleetMemberType.FIGHTER_WING, false);
 		api.addToFleet(FleetSide.ENEMY, "wasp_wing", FleetMemberType.FIGHTER_WING, false);
 		api.addToFleet(FleetSide.ENEMY, "xyphos_wing", FleetMemberType.FIGHTER_WING, false);
 		api.addToFleet(FleetSide.ENEMY, "xyphos_wing", FleetMemberType.FIGHTER_WING, false);
 		api.addToFleet(FleetSide.ENEMY, "dagger_wing", FleetMemberType.FIGHTER_WING, false);
 		api.addToFleet(FleetSide.ENEMY, "dagger_wing", FleetMemberType.FIGHTER_WING, false);
     // The Booty (Get it)
 		api.addToFleet(FleetSide.ENEMY, "atlas_Standard", FleetMemberType.SHIP, false);
 		api.addToFleet(FleetSide.ENEMY, "tarsus_Standard", FleetMemberType.SHIP, false);
 		api.addToFleet(FleetSide.ENEMY, "tarsus_Standard", FleetMemberType.SHIP, false);
 		api.addToFleet(FleetSide.ENEMY, "tarsus_Standard", FleetMemberType.SHIP, false);
 		api.addToFleet(FleetSide.ENEMY, "tarsus_Standard", FleetMemberType.SHIP, false);
 		api.addToFleet(FleetSide.ENEMY, "tarsus_Standard", FleetMemberType.SHIP, false);
 		api.addToFleet(FleetSide.ENEMY, "tarsus_Standard", FleetMemberType.SHIP, false);
 
 
 		// Set up the map.
 		float width = 9000f;
 		float height = 12000f;
 		api.initMap((float)-width/2f, (float)width/2f, (float)-height/2f, (float)height/2f);
 		
 		float minX = -width/2;
 		float minY = -height/2;
 		
 		api.addNebula(minX + width * 0.8f, minY + height * 0.4f, 2000);
 		api.addNebula(minX + width * 0.8f, minY + height * 0.5f, 2000);
 		api.addNebula(minX + width * 0.8f, minY + height * 0.6f, 2000);
 		
 		api.addObjective(minX + width * 0.3f, minY + height * 0.3f, "comm_relay");
 		api.addObjective(minX + width * 0.3f, minY + height * 0.7f, "comm_relay");
 		api.addObjective(minX + width * 0.5f, minY + height * 0.5f, "sensor_array");
 		api.addObjective(minX + width * 0.2f, minY + height * 0.5f, "sensor_array");
 		
 		// Add an asteroid field
 		api.addAsteroidField(minX + width * 0.3f, minY, 90, 3000f,
 								20f, 70f, 50);
 		
 		// Add some planets.  These are defined in data/config/planets.json.
 		api.addPlanet(minX + width * 0.2f + 600, minY + height * 0.5f, 400f, "lava", 300f);
 	}
 
 }
 
 
 
 
 
 
