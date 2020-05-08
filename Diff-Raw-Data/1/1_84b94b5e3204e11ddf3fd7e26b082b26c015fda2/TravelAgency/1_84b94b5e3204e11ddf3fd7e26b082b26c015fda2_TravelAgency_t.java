 /*
  * Copyright (C) 2012 TomyLobo
  *
  * This file is part of Routes.
  *
  * Routes is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published
  * by the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package eu.tomylobo.routes.travel;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import eu.tomylobo.abstraction.entity.Entity;
 import eu.tomylobo.abstraction.entity.EntityType;
 import eu.tomylobo.abstraction.entity.MobType;
 import eu.tomylobo.abstraction.entity.Player;
 import eu.tomylobo.abstraction.entity.VehicleType;
 import eu.tomylobo.math.Location;
 import eu.tomylobo.routes.Routes;
 import eu.tomylobo.routes.commands.system.CommandException;
 import eu.tomylobo.routes.fakeentity.FakeEntity;
 import eu.tomylobo.routes.fakeentity.FakeMob;
 import eu.tomylobo.routes.fakeentity.FakeVehicle;
 import eu.tomylobo.routes.infrastructure.Route;
 import eu.tomylobo.routes.util.ScheduledTask;
 
 /**
  * The TravelAgency manages all current {@link Traveller}s and will invoke
  * their {@link Traveller#tick() tick()} method each tick.
  *
  * @author TomyLobo
  *
  */
 public class TravelAgency extends ScheduledTask {
 	private final Routes plugin;
 
 	private final Map<Entity, Traveller> travellers = new HashMap<Entity, Traveller>();
 
 	public TravelAgency(Routes plugin) {
 		super(plugin);
 		this.plugin = plugin;
 
 		scheduleSyncRepeating(0, 1);
 	}
 
 	public void addTravellerWithMount(String routeName, final Player player, EntityType entityType, String command) throws CommandException {
 		final Route route = plugin.transportSystem.getRoute(routeName);
 		if (route == null)
 			throw new CommandException("Route '"+routeName+"' not found.");
 
 		route.checkPermission(player, command);
 
 		final boolean oldAllowFlight = player.getAllowFlight();
 		player.setAllowFlight(true);
 
 		Location location = route.getLocation(0);
 
 		final FakeEntity mount;
 		if (entityType instanceof MobType) {
 			mount = new FakeMob(location, (MobType) entityType);
 		}
 		else {
 			mount = new FakeVehicle(location, (VehicleType) entityType);
 		}
 
 		mount.send();
		player.teleport(location.add(0, mount.getMountedYOffset(), 0));
 		mount.setPassenger(player);
 
 		addTraveller(route, mount, 5.0, new Runnable() {
 			@Override
 			public void run() {
 				mount.remove();
 				player.setAllowFlight(oldAllowFlight);
 			}
 		});
 	}
 
 	/**
 	 * 
 	 * @param routeName The name of the route to travel on
 	 * @param entity An entity to move along the route
 	 * @param speed Speed in m/s
 	 * @param finalizer A Runnable to invoke after the route was finished
 	 * @throws CommandException Thrown if no route can be found.
 	 */
 	public void addTraveller(String routeName, Entity entity, double speed, Runnable finalizer) throws CommandException {
 		final Route route = plugin.transportSystem.getRoute(routeName);
 		if (route == null)
 			throw new CommandException("Route '"+routeName+"' not found.");
 
 		addTraveller(route, entity, speed, finalizer);
 	}
 
 	/**
 	 * 
 	 * @param route The route to travel on
 	 * @param entity An entity to move along the route
 	 * @param speed Speed in m/s
 	 * @param finalizer A Runnable to invoke after the route was finished
 	 */
 	public void addTraveller(Route route, Entity entity, double speed, Runnable finalizer) {
 		addTraveller(new Traveller(route, entity, speed, finalizer));
 	}
 
 	public void addTraveller(Traveller traveller) {
 		travellers.put(traveller.getEntity(), traveller);
 	}
 
 	@Override
 	public void run() {
 		for (Iterator<Traveller> it = travellers.values().iterator(); it.hasNext(); ) {
 			Traveller traveller = it.next();
 			try {
 				if (!traveller.tick())
 					it.remove();
 			}
 			catch (Exception e) {
 				System.err.println("Caught exception in traveller tick, will run finalizer now.");
 				e.printStackTrace();
 				traveller.runFinalizer();
 				it.remove();
 			}
 		}
 	}
 }
