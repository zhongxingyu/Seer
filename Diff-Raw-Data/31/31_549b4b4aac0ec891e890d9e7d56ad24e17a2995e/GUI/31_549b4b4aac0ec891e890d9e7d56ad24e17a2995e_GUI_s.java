 package de.haw.tt2p.trafficcoordination.visualization;
 
 import java.util.ArrayList;
 import java.util.Set;
 
 import org.openspaces.core.GigaSpace;
 import org.openspaces.events.polling.SimplePollingContainerConfigurer;
 import org.openspaces.events.polling.SimplePollingEventListenerContainer;
 
 import ch.aplu.jgamegrid.Actor;
 import ch.aplu.jgamegrid.GGExitListener;
 import ch.aplu.jgamegrid.GameGrid;
 import ch.aplu.jgamegrid.Location;
 
 import com.google.common.collect.Sets;
 
 import de.haw.tt2p.trafficcoordination.container.RoxelUpdatePollingContainer;
 import de.haw.tt2p.trafficcoordination.game.TrafficManager;
 import de.haw.tt2p.trafficcoordination.topology.Roxel;
 import de.haw.tt2p.trafficcoordination.topology.Roxel.Type;
 import de.haw.tt2p.trafficcoordination.topology.RoxelStructure;
 import de.haw.tt2p.trafficcoordination.visualization.StreetActor.Direction;
 
 public class GUI extends GameGrid implements IGUIUpdater {
 
 	private static final long serialVersionUID = -3381742157631617426L;
 
 	private final GigaSpace gigaSpace;
 	private final TrafficManager trafficManager;
 
 	private SimplePollingEventListenerContainer roxelPollingContainer;
 
	public GUI(GigaSpace gigaSpace, TrafficManager trafficManager) {
 		super(gigaSpace.read(new RoxelStructure()).getX(), gigaSpace.read(new RoxelStructure()).getY(), gigaSpace.read(
 			new RoxelStructure()).getSize());
 		this.gigaSpace = gigaSpace;
 		this.trafficManager = trafficManager;
 
 		initMap();
 		show();
 
 		// add listener which removes the RoxelUpdatePollingContainer on exit
 		addExitListener(new GGExitListener() {
 			@Override
 			public boolean notifyExit() {
 				System.out.println("Cleanup...");
 				if (roxelPollingContainer != null) {
 					roxelPollingContainer.destroy();
 					System.out.println("PollingContainer for Roxels removed");
 				}
 				return true;
 			}
 		});
 		// add listener which polls the roxel updates for the gui
 		roxelPollingContainer = new SimplePollingContainerConfigurer(gigaSpace)
 	        .eventListenerAnnotation(new RoxelUpdatePollingContainer(this)).pollingContainer();
 	}
 
 	private void initMap() {
 		for (Roxel roxel : gigaSpace.readMultiple(new Roxel())) {
 			Actor actor;
 			if (roxel.getType() == Type.House) {
 				actor = new HouseActor();
 			} else {
 				Set<Direction> possibleDirections = Sets.newHashSet();
 				int x = roxel.getX();
 				// only allow down at every second street, only allow up for others
 				if (x % 6 == 0) {
 					possibleDirections.add(Direction.SOUTH);
 				} else if (x % 3 == 0) {
 					possibleDirections.add(Direction.NORTH);
 				}
 				int y = roxel.getY();
 				// only allow right at every second street, only allow left for others
 				if (y % 8 == 0) {
 					possibleDirections.add(Direction.EAST);
 				} else if (y % 4 == 0) {
 					possibleDirections.add(Direction.WEST);
 				}
 				actor = new StreetActor(possibleDirections);
 			}
 			addActor(actor, new Location(roxel.getX(), roxel.getY()));
 		}
 	}
 
 	@Override
 	public void act() {
 		trafficManager.startDrivers();
 		super.act();
 	}
 
 	@Override
 	public void performUpdate(RoxelUpdate roxelUpdate) {
 		Integer oldX = roxelUpdate.getOldX();
 		Integer oldY = roxelUpdate.getOldY();
 		Integer newX = roxelUpdate.getNewX();
 		Integer newY = roxelUpdate.getNewY();
 		Integer carId = roxelUpdate.getCarId();
 		if (oldX == -1 || oldY == -1) {
 			CarActor car = new CarActor(carId);
 			addActor(car, new Location(newX, newY));
 		} else {
 			ArrayList<Actor> actorsAt = getActorsAt(new Location(oldX, oldY));
 			for (Actor actor : actorsAt) {
 				if (actor instanceof CarActor) {
 					actor.setLocation(new Location(newX, newY));
 				}
 			}
 		}
 	}
 
 }
