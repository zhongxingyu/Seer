 package vooga.fighter.model;
 
 import java.awt.Dimension;
 import java.util.List;
 
 import vooga.fighter.controller.ModelDelegate;
 import vooga.fighter.model.objects.CharacterObject;
 import vooga.fighter.model.objects.GameObject;
 import vooga.fighter.model.objects.MenuObject;
 import vooga.fighter.model.objects.MouseClickObject;
 import vooga.fighter.model.utils.State;
 
 public class MenuMode extends Mode {
 	private String myMenuId;
 	private List<MenuObject> myMenuObjects;
 	private MouseClickObject myMouseClick;
 	private MenuGrid myMenuGrid;
 
 	public MenuMode(ModelDelegate delegate, String menuId) {
 		super(delegate);
 		myMenuId = menuId;
 	}
 
 	@Override
 	public void update(double stepTime, Dimension bounds) {
         List<GameObject> myObjects = getMyObjects();
         handleCollisions();
         for (int i=0; i<myObjects.size(); i++) {
             GameObject object = myObjects.get(i);
             object.update();
             if (object.shouldBeRemoved()) {
                 myObjects.remove(object);
                 i--;
             }
         }
 //        if (shouldModeEnd()) {
 //            super.signalTermination();
 //        }
     }
 
 	@Override
 	public void initializeMode() {
 		myMenuGrid = new MenuGrid(myMenuId);	
 		myMenuObjects = myMenuGrid.getMenuObjects();
		for(MenuObject menu : myMenuGrid.getMenuObjects()){
 			addObject(menu);
 		}
 	}
 	
 	private void handleCollisions(){
 		MenuCollisionManager.checkCollisions(getMyObjects());
 	}
 
 	@Deprecated
 	public boolean shouldModeEnd() {
 		return false;
 	}
 	@Override
     public void addObject(GameObject object) {
         super.addObject(object);
         if (object instanceof MouseClickObject){
         	myMouseClick = (MouseClickObject)object;
         }
     }
 
 }
