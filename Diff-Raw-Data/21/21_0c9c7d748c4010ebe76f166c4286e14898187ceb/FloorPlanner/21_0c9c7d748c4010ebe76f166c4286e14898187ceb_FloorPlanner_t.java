 package org.percepta.mgrankvi.floorplanner;
 
 import org.percepta.mgrankvi.floorplanner.gwt.client.geometry.Point;
 import org.percepta.mgrankvi.floorplanner.gwt.client.item.door.DoorState.Direction;
 import org.percepta.mgrankvi.floorplanner.visual.entity.Room;
 import org.percepta.mgrankvi.floorplanner.visual.entity.RoomType;
import org.percepta.mgrankvi.floorplanner.visual.entity.Table;
 
 import com.vaadin.server.VaadinRequest;
 import com.vaadin.ui.CssLayout;
 import com.vaadin.ui.UI;
 
 /**
  * The Application's "main" class
  */
 @SuppressWarnings("serial")
 public class FloorPlanner extends UI {
 
 	@Override
 	protected void init(final VaadinRequest request) {
 		final CssLayout layout = new CssLayout();
 		setContent(layout);
 
 		final FloorGrid grid = new FloorGrid();
 		// grid.addRoom(RoomType.square(new Point()));
 
 		// Tiina
 		Room room = RoomType.customRoom("Tiina", new Point(0, 0), new Point(0, 0), new Point(315, 0), new Point(315, 60), new Point(270, 150), new Point(270,
 				450), new Point(0, 450));
 		room.addDoor(Direction.UP_LEFT, new Point(270, 445), 80);
 		grid.addRoom(room);
 		// Vappula
 		room = RoomType.customRoom("Vappula", new Point(270, 0), new Point(45, 0), new Point(195, 0), new Point(195, 60), new Point(240, 150), new Point(240,
 				360), new Point(0, 360), new Point(0, 150), new Point(45, 60));
 		room.addDoor(Direction.UP_LEFT, new Point(230, 360), 70);
 		grid.addRoom(room);
 		// Muurimaa
 		room = RoomType.customRoom("Muurimaa", new Point(465, 0), new Point(0, 0), new Point(315, 0), new Point(315, 450), new Point(45, 450), new Point(45,
 				150), new Point(0, 60));
 		room.addDoor(Direction.UP_RIGHT, new Point(45, 445), 80);
 		grid.addRoom(room);
 		// GuitarHero
 		room = RoomType.square(3, "Guitar Hero", new Point(105, 450));
 		room.addDoor(Direction.UP_LEFT, new Point(300, 285), 80);
 		grid.addRoom(room);
 		// PikkuNeukkari
 		room = RoomType.square(3, "Pikku Neukkari", new Point(105, 750));
 		room.addDoor(Direction.DOWN_RIGHT, new Point(300, 9), 80);
 		grid.addRoom(room);
 		// Kitchen
 		room = RoomType.customRoom("Kitchen", new Point(0, 1050), new Point(0, 15), new Point(105, 15), new Point(105, 0), new Point(405, 0), new Point(405,
 				300), new Point(0, 300));
 		room.addDoor(Direction.DOWN_LEFT, new Point(405, 9), 80);
 		grid.addRoom(room);
 		// Lounge
 		room = RoomType.customRoom("Lounge", new Point(405, 450), new Point(0, 0), new Point(375, 0), new Point(375, 900), new Point(0, 900));
 		room.addDoor(Direction.DOWN_RIGHT, new Point(15, 0), 80);
 		room.addDoor(Direction.DOWN_RIGHT, new Point(15, 900), 80);
 		grid.addRoom(room);
 
 		// MainHall width: 560 height: 520, toilet: w80, h100
 		room = RoomType.customRoom("Main Hall", new Point(-450, 1350), new Point(0, 0), new Point(1680, 0), new Point(1680, 540), new Point(1440, 540),
 				new Point(1440, 840), new Point(1680, 840), new Point(1680, 1560), new Point(0, 1560), new Point(0, 840), new Point(240, 840), new Point(240,
 						540), new Point(0, 540));
		room.addDoor(Direction.DOWN_RIGHT, new Point(1360, 1560), 80);
		room.addDoor(Direction.DOWN_LEFT, new Point(1480, 1560), 40);
 		grid.addRoom(room);
 
 		// 140, 130
 		room = RoomType.customRoom(new Point(-420, 2910), new Point(0, 0), new Point(420, 0), new Point(420, 390), new Point(0, 390));
 		room.addDoor(Direction.DOWN_LEFT, new Point(330, 0), 80);
 		grid.addRoom(room);
 
 		// MetsäNeukkari
 		room = RoomType.customRoom("Metsä neukkari", new Point(0, 2910), new Point(0, 0), new Point(480, 0), new Point(480, 390), new Point(0, 390));
 		room.addDoor(Direction.UP_LEFT, new Point(460, 0), 80);
		final Table table = new Table();
		table.setSize(200, 200);
		table.setPosition(new Point(100, 70));
		room.addItem(table.getState());
 		grid.addRoom(room);
 
 		// StuffRomm
 		room = RoomType.customRoom("Storage", new Point(480, 2910), new Point(0, 0), new Point(210, 0), new Point(210, 390), new Point(0, 390));
 		room.addDoor(Direction.UP_LEFT, new Point(180, 0), 80);
 		grid.addRoom(room);
 		// ServerRoom
 		room = RoomType.customRoom("Server room", new Point(690, 2910), new Point(0, 0), new Point(150, 0), new Point(150, 390), new Point(0, 390));
 		room.addDoor(Direction.UP_RIGHT, new Point(30, 0), 80);
 		grid.addRoom(room);
 
 		layout.addComponent(grid);
 	}
 }
 
 // //Tiina
 // Room room = RoomType.customRoom("Tiina", new Point(0, 0), new Point(0, 0),
 // new Point(105, 0), new Point(105, 20), new Point(90, 50),
 // new Point(90, 150), new Point(0, 150));
 // room.addDoor(Direction.UP_LEFT, new Point(90, 145));
 // grid.addRoom(room);
 // // Vappula
 // room = RoomType.customRoom("Vappula", new Point(90, 0), new Point(15, 0), new
 // Point(65, 0), new Point(65, 20), new Point(80, 50), new Point(80, 120),
 // new Point(0, 120), new Point(0, 50), new Point(15, 20));
 // room.addDoor(Direction.UP_LEFT, new Point(75, 120));
 // grid.addRoom(room);
 // // Muurimaa
 // room = RoomType.customRoom("Muurimaa", new Point(155, 0), new Point(0, 0),
 // new Point(105, 0), new Point(105, 150), new Point(15, 150),
 // new Point(15, 50), new Point(0, 20));
 // room.addDoor(Direction.UP_RIGHT, new Point(15, 145));
 // grid.addRoom(room);
 // // GuitarHero
 // room = RoomType.square("Guitar Hero", new Point(35, 150));
 // room.addDoor(Direction.UP_LEFT, new Point(100, 95));
 // grid.addRoom(room);
 // // PikkuNeukkari
 // room = RoomType.square("Pikku Neukkari", new Point(35, 250));
 // room.addDoor(Direction.DOWN_RIGHT, new Point(100, 3));
 // grid.addRoom(room);
 // // Kitchen
 // room = RoomType.customRoom("Kitchen", new Point(0, 350), new Point(0, 5), new
 // Point(35, 5), new Point(35, 0), new Point(135, 0), new Point(135, 100),
 // new Point(0, 100));
 // room.addDoor(Direction.DOWN_LEFT, new Point(135, 3));
 // grid.addRoom(room);
 // // Lounge
 // room = RoomType.customRoom("Lounge", new Point(135, 150), new Point(0, 0),
 // new Point(125, 0), new Point(125, 300), new Point(0, 300));
 // room.addDoor(Direction.DOWN_RIGHT, new Point(5, 0));
 // room.addDoor(Direction.DOWN_RIGHT, new Point(5, 300));
 // grid.addRoom(room);
 //
 // // MainHall width: 560 height: 520, toilet: w80, h100
 // grid.addRoom(RoomType.customRoom("Main Hall", new Point(-150, 450), new
 // Point(0, 0), new Point(560, 0), new Point(560, 180), new Point(480, 180),
 // new Point(480, 280), new Point(560, 280), new Point(560, 520), new Point(0,
 // 520), new Point(0, 280), new Point(80, 280), new Point(80, 180),
 // new Point(0, 180)));
 //
 // // 140, 130
 // room = RoomType.customRoom(new Point(-140, 970), new Point(0, 0), new
 // Point(140, 0), new Point(140, 130), new Point(0, 130));
 // room.addDoor(Direction.DOWN_LEFT, new Point(110, 0));
 // grid.addRoom(room);
 //
 // // MetsäNeukkari
 // // room = RoomType.customRoom("Metsä neukkari", new Point(0, 970), new
 // // Point(0, 0), new Point(160, 0), new Point(160, 130), new Point(0,
 // // 130));
 // room = RoomType.customRoom("Metsä neukkari", new Point(160, 970), new
 // Point(0, 0), new Point(480, 0), new Point(480, 390), new Point(0, 390));
 // // room.addDoor(Direction.UP_LEFT, new Point(140, 0));
 // room.addDoor(Direction.UP_LEFT, new Point(460, 0));
 // grid.addRoom(room);
 //
 // // StuffRomm
 // room = RoomType.customRoom("Storage", new Point(160, 970), new Point(0, 0),
 // new Point(70, 0), new Point(70, 130), new Point(0, 130));
 // room.addDoor(Direction.UP_LEFT, new Point(60, 0));
 // grid.addRoom(room);
 // // ServerRoom
 // room = RoomType.customRoom("Server room", new Point(230, 970), new Point(0,
 // 0), new Point(50, 0), new Point(50, 130), new Point(0, 130));
 // room.addDoor(Direction.UP_RIGHT, new Point(10, 0));
 // grid.addRoom(room);
