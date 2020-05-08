 package net.stormdev.ucars.trade.AIVehicles;
 
 import java.util.List;
 
 import net.stormdev.ucars.trade.main;
 import net.stormdev.ucars.utils.Car;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Minecart;
 import org.bukkit.entity.Player;
 import org.bukkit.metadata.MetadataValue;
 import org.bukkit.util.Vector;
 
 import com.useful.uCarsAPI.uCarsAPI;
 import com.useful.ucars.ClosestFace;
 import com.useful.ucarsCommon.StatValue;
 
 public class AIRouter {
 	
 	private boolean enabled;
 	private Material trackBlock;
 	private Material roadEdge;
 	private Material junction;
 	private uCarsAPI api;
 	
 	public AIRouter(boolean enabled){
 		this.enabled = enabled;
 		String trackRaw = main.config.getString("general.ai.trackerBlock");
 		String edgeRaw = main.config.getString("general.ai.roadEdgeBlock");
 		String junRaw = main.config.getString("general.ai.junctionBlock");
 		trackBlock = Material.getMaterial(trackRaw);
 		roadEdge = Material.getMaterial(edgeRaw);
 		junction = Material.getMaterial(junRaw);
 		if(trackBlock == null || roadEdge == null || junction == null){
 			main.logger.info("Didn't enable AIs routing as configuration is invalid!");
 			enabled = false;
 		}
 		api = uCarsAPI.getAPI();
 	}
 	
 	public void route(final Minecart car, final Car c) throws Exception{
 		if(!enabled){
 			return;
 		}
 		double speed = 2;
 		BlockFace direction = BlockFace.NORTH;
 		Vector vel = car.getVelocity();
 		
 		Location loc = car.getLocation();
 		Block under = loc.getBlock().getRelative(BlockFace.DOWN, 2);
 		
 		double cx = loc.getX();
 		double cy = loc.getY();
 		double cz = loc.getZ();
 		
 		if(!c.stats.containsKey("trade.npc")){
 			//Not an npc
 			return;
 		}
 		List<Entity> nearby = car.getNearbyEntities(20, 10, 20); //20x20 radius
 		if(main.random.nextInt(5) < 1){ // 1 in 5 chance
 			//Check if players nearby
 			boolean nearbyPlayers = false;
 			for(Entity e:nearby){
 				if(e instanceof Player){
 					nearbyPlayers = true;
 				}
 			}
 			if(!nearbyPlayers){
 				//Remove me
 				if(car.getPassenger() != null){
 					car.getPassenger().remove();
 				}
 				main.plugin.carSaver.removeCar(car.getUniqueId());
 				car.remove();
 				AISpawnManager.decrementSpawned();
 			}
 		}
 		
 		nearby = car.getNearbyEntities(1.5, 1.5, 1.5); //Nearby cars
 		Boolean stop = false;
 		for(Entity e:nearby){
 			if(e.getType() == EntityType.MINECART && e.hasMetadata("trade.npc")){ //Avoid driving into another car
 				Location l = e.getLocation();
 				//Compare 'l' and 'loc' to see who is in front
 				//West = -x, East = +x, South = +z, North = -z
 				double lx = l.getX();
 				double lz = l.getZ();
 				
 				if(direction == BlockFace.EAST){
 					//Heading east
 					if(cx < lx){
 						stop = true;
 					}
 				}
 				else if(direction == BlockFace.WEST){
 					//Heading west
 					if(cx > lx){
 						stop = true;
 					}
 				}
 				else if(direction == BlockFace.NORTH){
 					//Heading north
 					if(cz > lz){
 						stop = true;
 					}
 				}
 				else if(direction == BlockFace.SOUTH){
 					//Heading south
 					if(cz < lz){
 						stop = true;
 					}
 				}
 			}
 		}
 		
 		if(stop || car.hasMetadata("car.frozen") || api.atTrafficLight(car)){
 			car.setVelocity(new Vector(0,0,0)); //Stop (or trafficlights)
 			return;
 		}
 		
 		Material ut = under.getType();
 		
 		if(ut != trackBlock && ut != junction){
 			Block u= under.getRelative(BlockFace.DOWN);
 			ut = u.getType();
 			if(ut != trackBlock && ut != junction){
 				u = under.getRelative(BlockFace.UP);
 				ut = u.getType();
 				if(ut != trackBlock && ut != junction){
 					relocateRoad(car, under, loc, ut==junction);
 					return;
 				}
 			}
 			under = u;
 			ut = under.getType();
 		}
 		
 		boolean atJ = false;
 		boolean rerouting = false;
 		
 		if(ut == junction){
 			atJ = true;
 			if(!car.hasMetadata("car.needRouteCheck")){
 				car.setMetadata("car.needRouteCheck", new StatValue(null, main.plugin));
 			}
 		}
 		else{
 			if(car.hasMetadata("car.needRouteCheck")){
 				car.removeMetadata("car.needRouteCheck", main.plugin);
 				direction = main.plugin.aiSpawns.carriagewayDirection(under);
 				//Update direction stored on car...
 				car.removeMetadata("trade.npc", main.plugin);
 				car.setMetadata("trade.npc", new StatValue(new VelocityData(direction, null), main.plugin));
 				rerouting = true;
 			}
 		}
 		
 		VelocityData data = new VelocityData(null, null);
 		boolean keepVel = !atJ && !car.hasMetadata("npc.turning") && !rerouting && !car.hasMetadata("relocatingRoad");
 		
 		if(car.hasMetadata("trade.npc")){
 			List<MetadataValue> ms = car.getMetadata("trade.npc");
 			data = (VelocityData) ms.get(0).value();
 			if(data.getDir() != null){
 				direction = data.getDir();
 			}
 			if(keepVel && !data.hasMotion()){
 				keepVel = false;
 			}
 			//direction = (BlockFace) ms.get(0).value();
 		}
 		else{
 			if(direction == null){
 				direction = main.plugin.aiSpawns.carriagewayDirection(under);
 				keepVel = false;
 				data.setMotion(null);
 			}
 			//Calculate direction from road
 			if(!atJ){
 				BlockFace face = main.plugin.aiSpawns.carriagewayDirection(under);
 				if(!direction.equals(face)){
 					direction = face;
 					keepVel = false;
 					data.setMotion(null);
 				}
 			}
 			else{
 				relocateRoad(car, car.getLocation().getBlock().getRelative(BlockFace.DOWN, 2), loc, atJ);
 				return;
 			}
 		}
 		
 		if(direction == null){ //Not on a road
 			//Try to recover
 			relocateRoad(car, car.getLocation().getBlock().getRelative(BlockFace.DOWN, 2), loc, atJ);
 			return;
 		}
 		
 		//Now we need to route it...
 		TrackingData nextTrack = AITrackFollow.nextBlock(under, direction, trackBlock, junction, car);
 		if(nextTrack.junction){
 			keepVel = false;
 		}
 		if(direction != nextTrack.dir){
 			direction = nextTrack.dir;
 			keepVel = false;
 			//Update direction stored on car...
 			car.removeMetadata("trade.npc", main.plugin);
 			data.setMotion(null);
 			car.setMetadata("trade.npc", new StatValue(new VelocityData(direction, null), main.plugin));
 		}
 		Block next = nextTrack.nextBlock;
 		Block road = next.getRelative(BlockFace.UP);
 		while(road.getType() == trackBlock){
 			road = road.getRelative(BlockFace.UP);
 		}
 		Block toDrive = road.getRelative(BlockFace.UP);
 		if(!toDrive.isEmpty()){
 			//Car has hit a wall
 			return;
 		}
		if(toDrive.getLocation().distanceSquared(loc) >= 3.25 ||
				toDrive.getY() > loc.getBlockY()){
 			keepVel = false;
 		}
 		if(keepVel){
 			vel = data.getMotion();
 			car.removeMetadata("relocatingRoad", main.plugin);
 			car.setVelocity(vel);
 		}
 		else{
 			//Calculate vector to get there...
 			double tx = toDrive.getX();
 			double ty = toDrive.getY();
 			double tz = toDrive.getZ();
 			
 			double x = tx - cx + 0.5;
 			double y = ty - cy;
 			double z = tz - cz + 0.5;
 			
 			double px = Math.abs(x);
 			double pz = Math.abs(z);
 			boolean ux = px > pz ? false:true;
 
 			if(y<0.15 && isCompassDir(direction)){
 				if (ux) {
 					// x is smaller
 					// long mult = (long) (pz/speed);
 					x = (x / pz) * speed;
 					z = (z / pz) * speed;
 				} else {
 					// z is smaller
 					// long mult = (long) (px/speed);
 					x = (x / px) * speed;
 					z = (z / px) * speed;
 				}
 			}
 			if(y>0){
 				y = 3;
 				x*= 10;
 				x*= 10;
 			}
 			vel = new Vector(x,y,z); //Go to block
 			car.removeMetadata("relocatingRoad", main.plugin);
 			data.setMotion(vel);
 			car.setVelocity(vel);
 		}
 		data.setDir(direction);
 		car.removeMetadata("trade.npc", main.plugin);
 		car.setMetadata("trade.npc", new StatValue(data, main.plugin));
 		return;
 	}
 	
 	public void relocateRoad(Minecart car, Block under, Location currentLoc, boolean atJunction){
 		
 		if(car.hasMetadata("relocatingRoad")){
 			car.removeMetadata("relocatingRoad", main.plugin);
 			return;
 		}
 		
 		car.setMetadata("relocatingRoad", new StatValue(true, main.plugin));
 		
 		Vector vel = car.getVelocity();
 		double cx = currentLoc.getX();
 		double cy = currentLoc.getY();
 		double cz = currentLoc.getZ();
 		
 		//Find track block
 		Block N = under.getRelative(BlockFace.NORTH);
 		Block E = under.getRelative(BlockFace.EAST);
 		Block S = under.getRelative(BlockFace.SOUTH);
 		Block W = under.getRelative(BlockFace.WEST);
 		Block toGo = null;
 		
 		if(N.getType() == trackBlock || N.getType() == junction){
 			toGo = N;
 		}
 		else if(E.getType() == trackBlock || E.getType() == junction){
 			toGo = E;
 		}
 		else if(S.getType() == trackBlock || S.getType() == junction){
 			toGo = S;
 		}
 		else if(W.getType() == trackBlock || W.getType() == junction){
 			toGo = W;
 		}
 		
 		if(toGo == null){
 			car.setVelocity(vel.multiply(-1)); //Reverse
 			return;
 		}
 		
 		toGo = toGo.getRelative(BlockFace.UP,2);
 		if(!toGo.isEmpty()){
 			//Invalid
 			toGo = toGo.getRelative(BlockFace.UP);
 			if(!toGo.isEmpty()){
 				//Invalid still
 				return;
 			}
 		}
 		
 		//Calculate vector to get there...
 		double tx = toGo.getX();
 		double ty = toGo.getY();
 		double tz = toGo.getZ();
 		
 		double x = tx - cx + 0.5;
 		double y = ty - cy;
 		double z = tz - cz + 0.5;
 
 		vel = new Vector(x,y,z); //Go to block
 		
 		car.setVelocity(vel);
 		BlockFace direction = ClosestFace.getClosestFace(car.getLocation().getYaw());
 		if(!atJunction){
 			direction = main.plugin.aiSpawns.carriagewayDirection(under);
 		}
 		else{
 			if(!car.hasMetadata("car.needRouteCheck")){
 				car.setMetadata("car.needRouteCheck", new StatValue(null, main.plugin));
 			}
 		}
 		//Update direction stored on car...
 		car.removeMetadata("trade.npc", main.plugin);
 		car.setMetadata("trade.npc", new StatValue(new VelocityData(direction, null), main.plugin));
 		return;
 	}
 	
 	public boolean isCompassDir(BlockFace face){
 		switch(face){
 		case NORTH: return true;
 		case EAST: return true;
 		case SOUTH: return true;
 		case WEST: return true;
 		default: return false;
 		}
 	}
 }
