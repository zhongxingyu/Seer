 package data.scripts.world.corvus;
 
 import java.util.Arrays;
 import java.util.List;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.Iterator;
 
 import com.fs.starfarer.api.Global;
 import com.fs.starfarer.api.campaign.CampaignFleetAPI;
 import com.fs.starfarer.api.campaign.CargoAPI;
 import com.fs.starfarer.api.campaign.LocationAPI;
 import com.fs.starfarer.api.campaign.SectorAPI;
 import com.fs.starfarer.api.campaign.SectorEntityToken;
 import com.fs.starfarer.api.campaign.FleetDataAPI;
 import com.fs.starfarer.api.campaign.StarSystemAPI;
 import com.fs.starfarer.api.fleet.FleetMemberType;
 import com.fs.starfarer.api.fleet.FleetMemberAPI;
 
 import data.scripts.world.BaseSpawnPoint;
 
 @SuppressWarnings("unchecked")
 public class RestockSpawnPoint extends BaseSpawnPoint {
     
     public RestockSpawnPoint(SectorAPI sector, LocationAPI location, 
 			     float daysInterval, int maxFleets, SectorEntityToken anchor)
     {
 	super(sector, location, daysInterval, maxFleets, anchor);
 	
     }
     //All stations are restocked from the aShips and aWings lists.
     private static String [] aShips = { 				"hound_Hull",
 									"dram_Hull",
 									"tarsus_Hull",
 									"buffalo_Hull",
 									"phaeton_Hull",
     };
     
     private static String [] aWings = { 
 	"talon_wing",
 	"mining_drone_wing",
     };
     // Hegemony lists
     private static String [] hShips = {                             "lasher_Hull",
 								    "brawler_Hull",
 								    "vigilance_Hull",
 								    "hammerhead_Hull",
 								    "sunder_Hull",
 								    "condor_Hull",
 								    "enforcer_Hull",
 								    "buffalo_Hull",
 								    "gemini_Hull",
 								    "falcon_Hull",
 								    "eagle_Hull",
 								    "venture_Hull",
 								    "dominator_Hull",
 								    "onslaught_Hull",
 								    "atlas_Hull",
 								    "conquest_Hull",
 								    "mule_Hull",
 								    "valkyrie_Hull",
     };
     
     private static String [] hWings = { 
 	"broadsword_wing",
 	"gladius_wing",
 	"warthog_wing",
 	"talon_wing",
 	"thunder_wing",
 	"piranha_wing",
 	"mining_drone_wing"
     };
     
     // Pirate lists
     private static String [] pShips = {                             "lasher_Hull",
 								    "brawler_Hull",
 								    "vigilance_Hull",
 								    "hammerhead_Hull",
 								    "condor_Hull",
 								    "enforcer_Hull",
 								    "buffalo2_Hull",
 								    "venture_Hull",
 								    "dominator_Hull",
 								    "conquest_Hull",
 								    "mule_Hull",
 								    "valkyrie_Hull",
 								    "wolf_Hull",
     };
     
     private static String [] pWings = { 
 	"broadsword_wing",
 	"gladius_wing",
 	"warthog_wing",
 	"thunder_wing",
 	"talon_wing",
 	"piranha_wing",
 	"mining_drone_wing"
     };
     // The tri-tachyon lists
     private static String [] tShips = { 
 	"wolf_Hull",
 	"tempest_Hull",
 	"afflictor_Hull",
 	"omen_Hull",
 	"hyperion_Hull",
 	"medusa_Hull",
 	"aurora_Hull",
 	"apogee_Hull",
 	"doom_Hull",
 	"odyssey_Hull",
 	"astral_Hull",
 	"paragon_Hull",
 	"shade_Hull",
 	"shuttle_Hull",
     };
     
     private static String [] tWings = { 
 	"xyphos_wing",
 	"dagger_wing",
 	"wasp_wing",
 	"trident_wing",
 	"longbow_wing"
     };
     
     
     //Put arrays for custom stations here!
     
     //Example:
     private static String [] myShips = {"onslaught_Hull",};
     private static String [] myWings = {"talon_wing",};
     
     //Insterstellar Federation Lists
     private static String [] fShips = {
 	"antares_Hull",
 	"ares_Hull",
 	"defiant_Hull",
 	"vixen_Hull",
 	"shogun_Hull",
 	"orion_Hull",
 	"scythe_Hull",
 	"dakota_Hull",
 	"titan_Hull",
 	"echo_Hull",
 	"rickshaw_Hull",
 	"toa_Hull",
 	"auria_Hull",
 	"mazerk_Hull",
 	"tahoe_Hull",
 	"trexel_Hull",
 	"ballista_Hull",
 	"mercury_Hull",
 	"yukon_Hull",
 	"zephyr_Hull",
 	"montana_Hull",
 	"albatross_Hull",
 	"helios_Hull",
     };
     private static String [] fWings = {
     	"hornet_wing",
 	"draken_wing",
 	"tracer_wing",
 	"foxbat_wing",
     };
 
     //Junk Pirates Lists
     private static String [] jShips = { 
 	"junk_pirates_orca_Hull",
 	"junk_pirates_dugong_Hull",
 	"junk_pirates_sickle_Hull",
 	"junk_pirates_hammer_Hull",
 	"junk_pirates_clam_Hull",
 	"junk_pirates_boxer_Hull",
 	"junk_pirates_boxenstein_Hull",
 	"junk_pirates_octopus_Hull",
 	"junk_pirates_langoustine_Hull",
 	"junk_pirates_scythe_Hull",
 	"junk_pirates_goat_Hull",
 	"junk_pirates_stoatA_Standard",
 	"junk_pirates_stoatB_Standard",
 	"junk_pirates_kraken_Hull",
 	"junk_pirates_turbot_Hull",
 	"junk_pirates_the_reaper_Hull",
     };
     private static String [] jWings = {
 	"junk_pirates_shard_wing",
 	"junk_pirates_spike_wing",
 	"junk_pirates_cleat_wing",
 	"junk_pirates_splinter_wing",
     };
 
     //Lotus Conglamorate Lists
     private static String [] lShips = {
       "blizzard_Hull",
       "ajax_Hull",
       "hydra_Hull",
       "rive_Hull",
       "eventide_Hull",
       "jackal_Hull",
       "vanquish_Hull",
       "stingray_Hull",
       "arcadia_Hull",
       "argos_Hull",
       "aria_Hull",
       "intrepid_Hull",
     };
     private static String [] lWings = {
  	"stigma_wing",
 	"javelin_wing",
 	"cutlass_wing",
 	"bolt_wing",
     };
 
     //Neutrino Corp Lists
     private static String [] nShips = {
     	"neutrino_lathe_hull",
 	"neutrino_piledriver_hull",
 	"neutrino_relativity_hull",									
 	"neutrino_singularity_hull",
 	"neutrino_singularity_hull",									
 	"neutrino_nausicaa_hull",
 	"neutrino_nirvash_hull",
 	"neutrino_grinder_hull",									
 	"neutrino_hacksaw_hull",
 	"neutrino_hacksaw_hull",									
 	"neutrino_jackhammer_hull",
 	"neutrino_hildolfr_hull",
 	"neutrino_vice_hull",						
 	"neutrino_banshee_hull",	
 	"neutrino_colossus_hull",
 	"neutrino_adventure_hull",	
     };
     private static String [] nWings = {
     	"neutrino_drohne_wing",
 	"neutrino_schwarzgeist_wing",
 	"neutrino_drache_wing",									
 	"neutrino_floh_wing",		
     };
 
     //Antediluvian Lists
     private static String [] dShips = {
 	"anchor_Antediluvian",
 	"athens_Antediluvian",
 	"atlantis_Antediluvian",
 	"atol_Antediluvian",
 	"azores_Antediluvian",
 	"bulwark_Antediluvian",
 	"calypso_Antediluvian",
 	"cape_Antediluvian",
 	"column_Antediluvian",
 	"critias_Antediluvian",
 	"deluge_Antediluvian",
 	"donovan_Antediluvian",
 	"forlorn_Antediluvian",
 	"gadeirus_Antediluvian",
 	"gibborim_Antediluvian",
 	"halyard_Antediluvian",
 	"nephilim_Antediluvian",
 	"oracle_Antediluvian",
 	"poseidon_Antediluvian",
 	"sentinel_Antediluvian",
 	"timaeus_Antediluvian",
     };
     private static String [] dWings = {
 	"persephone_wing_large",
 	"persephone_wing_small",
     };
     
     //Shadowyards Heavy Industries Lists
     private static String [] sShips = {
 	"ms_charybdis_Hull",
 	"ms_morningstar_Hull",
 	"ms_enlil_Hull",
 	"ms_seski_Hull"
     };
     private static String [] sWings = {
     	"ms_skinwalker_wing",
 	"ms_neriad_wing"
     };
 
     // Nomads Lists
     private static String [] mShips = {
     };
     private static String [] mWings = {
     };
 
     //ASP Syndicate Ships
     private static String [] aspShips = {
     };
     private static String [] aspWings = {
     };
 
 
     @Override
 	protected CampaignFleetAPI spawnFleet() {
 	
 	StarSystemAPI system = getSector().getStarSystem("Corvus");
 	List allStations = system.getOrbitalStations();
 	
 	for (int i=0; i<allStations.size(); i++){//Loop over all stations
 	    SectorEntityToken station = (SectorEntityToken) allStations.get(i);//get current station
 	    CargoAPI aCargo = station.getCargo();//get cargo
 	    if (aCargo.isFreeTransfer() == false){// only if the station is not the player station
 		if (station.getName().toString().equals("Serenity Station" == false)){// Also not the Neutral Serenity Station
 			FleetDataAPI aMothFleet = aCargo.getMothballedShips();//get fleet data
                         
 			// All stations are restocked with crew and ships from aShips, aWings
 			
 			restockWeapons(aCargo);//restock weapons (all)
 			restockCrew(aCargo);//restock crew
 			
 			restockShips(aMothFleet, aShips);
 			restockFighters(aMothFleet, aWings);
                         
 			//Global.getSector().addMessage(station.getName().toString());
                         
 			// Individual stations are restocked from their own ship/wing lists                               
 			
 			if (station.getName().toString().equals("Orbital Station")){
 			    restockShips(aMothFleet,hShips);
 			    restockFighters(aMothFleet,hWings);
 			    Global.getSector().addMessage("Checking Orbital Station");
 			}
 			
 			if (station.getName().toString().equals("Hidden Base")){
 			    restockShips(aMothFleet,pShips);
 			    restockFighters(aMothFleet,pWings);
 			    Global.getSector().addMessage("Checking Hidden Base");
 			}
                         
 			if (station.getName().toString().equals("Corporate HQ")){
 			    restockShips(aMothFleet,tShips);
 			    restockFighters(aMothFleet,tWings);
 			    Global.getSector().addMessage("Checking Corporate HQ");
 			}
 			
 			// Add individual stations here! Use the above as templates.
                         
 			//Interstellar Federation
                         if (station.getName().toString().equals("Navy Outpost")){
 			    restockShips(aMothFleet,fShips);
 			    restockFighters(aMothFleet,fWings);
 			    Global.getSector().addMessage("Checking Navy Outpost");				
 			}
 			
 			//Junk Pirates station
 			if (station.getName().toString().equals("Junk Yard")){
 			    restockShips(aMothFleet,jShips);
 			    restockFighters(aMothFleet,jWings);
 			    Global.getSector().addMessage("Checking Junk Yard");
 			};
 
 			//Lotus Pirates Station
 			if (station.getName().toString().equals("Anchorage")){
 			    restockShips(aMothFleet,lShips);
 			    restockFighters(aMothFleet,lWings);
 			    Global.getSector().addMessage("Checking Lotus Pirates Anchorage");
 			};
 
 			//Nuetrino Corp Station
 			if (station.getName().toString().equals("Solar Powerplant")){
 			    restockShips(aMothFleet,nShips);
 			    restockFighters(aMothFleet,nWings);
 			    Global.getSector().addMessage("Checking Solar Powerplant");
 			};
 
 			//Shadowyard Industries Station
 			if (station.getName().toString().equals("Outlet Plant")){
 			    restockShips(aMothFleet,sShips);
 			    restockFighters(aMothFleet,sWings);
 			    Global.getSector().addMessage("Checking Outlet plant");
 			};
 			//Independant and Orphan Station
 			if (station.getname().toString().equals("Trade Station")){
 			    restockShips(aMothFleet,dShips,mShips,aspShips);
 			    restockFighters(aMothFleet,dWings,mWings,aspWings);
 			    Global.getSector().addMessage("Checking Trade Station");
 			};
                         
 			
 		    }
 		    }
 		
 		return null;
 	    }
 	    
 	    
    
 	    private void restockShips(FleetDataAPI mothFleet, String[] ships){
 		Set shipSet = new HashSet(Arrays.asList(ships));
 		
 		List mothShips = mothFleet.getMembersListCopy();
 		Set msSet = new HashSet();
 		for (int i = 0; i < mothShips.size(); i++) {
 		    String specId = ((FleetMemberAPI) mothShips.get(i)).getSpecId();
 		    msSet.add(specId);
 		}
 		
 		shipSet.removeAll(msSet);
 		
 		//Global.getSector().addMessage("Moth Size = " + msSet.size());
 		//Global.getSector().addMessage("Set Size = " + shipSet.size());
 		
 		Iterator iter = shipSet.iterator();
 		
 		while(iter.hasNext()) {
 		    mothFleet.addFleetMember(Global.getFactory().createFleetMember(FleetMemberType.SHIP,(String) iter.next()));
 		}
 	    }
 	    
 	    private void restockFighters(FleetDataAPI mothFleet, String[] wings){
 		Set wingSet = new HashSet(Arrays.asList(wings));
 		
 		List mothShips = mothFleet.getMembersListCopy();
 		Set msSet = new HashSet();
 		for (int i = 0; i < mothShips.size(); i++) {
 		    String specId = ((FleetMemberAPI) mothShips.get(i)).getSpecId();
 		    msSet.add(specId);
 		}
 		
 		
 		wingSet.removeAll(msSet);
 	   		//Global.getSector().addMessage("Moth Size = " + msSet.size());
 		//Global.getSector().addMessage("Set Size = " + wingSet.size());
 		
 		Iterator iter = wingSet.iterator();
 		
 		while(iter.hasNext()) {
 		    mothFleet.addFleetMember(Global.getFactory().createFleetMember(
 										   FleetMemberType.FIGHTER_WING,(String) iter.next()));
 		}
 	    }
 	    
 	    private void restockWeapons(CargoAPI sCargo){
 		List allWeapons = Global.getSector().getAllWeaponIds();
 		
 		for (int i=0; i<allWeapons.size(); i++){
 		    if (sCargo.getNumWeapons((String) allWeapons.get(i)) == 0){
 			sCargo.addWeapons((String) allWeapons.get(i), 4);
 		    }
 		}
 		
 	    }
 	    
 	    private void restockCrew(CargoAPI sCargo){
 		if (sCargo.getCrew(CargoAPI.CrewXPLevel.GREEN) == 0){
 		    sCargo.addCrew(CargoAPI.CrewXPLevel.GREEN, 200);
 		}
 		if (sCargo.getCrew(CargoAPI.CrewXPLevel.REGULAR) == 0){
 		    sCargo.addCrew(CargoAPI.CrewXPLevel.REGULAR, 100);
 		}
 		if (sCargo.getCrew(CargoAPI.CrewXPLevel.VETERAN) == 0){
 		    sCargo.addCrew(CargoAPI.CrewXPLevel.VETERAN, 25);
 		}
 		if (sCargo.getCrew(CargoAPI.CrewXPLevel.ELITE) == 0){
 		    sCargo.addCrew(CargoAPI.CrewXPLevel.ELITE, 5);
 		}
 		if (sCargo.getSupplies() < 100){
 		    sCargo.addSupplies(250);
 		}
 		
 	    }
 	    
 	    
 	}
     }
 }
