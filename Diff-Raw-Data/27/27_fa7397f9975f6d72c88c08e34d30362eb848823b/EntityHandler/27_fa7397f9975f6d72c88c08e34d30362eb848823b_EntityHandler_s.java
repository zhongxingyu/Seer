 package entity;
 
 import java.util.ArrayList;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.gui.GUIContext;
 
 import events.EmptyEvent;
 import events.HouseEvent;
 
 public class EntityHandler {
 	private ArrayList<BasicEntity> entitys;
 
 	public EntityHandler(GUIContext container) {
 		entitys = new ArrayList<>();
 		addEntitys(container);
 	}
 
 	private void addEntitys(GUIContext container) {
 		try {
 			this.entitys.add(new BasicEntity("houseBlack", new Image("img/house1.png"), new HouseEvent(), 150, 270, 0));
 			this.entitys.add(new BasicEntity("houseRed", new Image("img/house2.png"), new HouseEvent(), 320, 80, 0));
 			this.entitys.add(new BasicEntity("mine", new Image("img/mine.png"), new HouseEvent(), 550, -10, 0));
			this.entitys.add(new BasicEntity("sawmill", new Image("img/sawmill.png"), new HouseEvent(), 800, 80, 0));
 			this.entitys.add(new BasicEntity("mill", new Image("img/mill.png"), new EmptyEvent(), 320, 450, 0));
 			this.entitys.add(new BasicEntity("pub", new Image("img/pub.png"), new HouseEvent(), 700, 80, 0));
 			this.entitys.add(new BasicEntity("sea", new Image("img/sea.png"), new EmptyEvent(), 0, -768, 0));
 			this.entitys.add(new BasicEntity("well", new Image("img/well.png"), new EmptyEvent(), 580, 360, 0));
 		} catch (SlickException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void render(GameContainer container, Graphics graphics) throws SlickException {
 		for (int i = 0; i < entitys.size(); i++) {
 			entitys.get(i).render(container, graphics);
 		}
 	}
 
 	public BasicEntity getEntity(int x, int y) {
 		BasicEntity entity = null;
 		for (int i = 0; i < entitys.size(); i++) {
 			if (entitys.get(i).checkForCollission(x, y)) {
 				entity = entitys.get(i);
 				break;
 			}
 		}
 		return entity;
 	}
 
 	public BasicEntity getEntity(String name) {
 		BasicEntity entity = null;
 		for (int i = 0; i < entitys.size(); i++) {
 			if (entitys.get(i).getName().equals(name)) {
 				entity = entitys.get(i);
 				break;
 			}
 		}
 		return entity;
 	}
 }
