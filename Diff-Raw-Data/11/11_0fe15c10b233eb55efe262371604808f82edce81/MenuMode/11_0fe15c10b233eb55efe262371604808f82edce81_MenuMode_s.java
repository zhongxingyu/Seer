 package vooga.fighter.model;
 
 import java.awt.Dimension;
 import java.util.ArrayList;
 import java.util.List;
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
     private CollisionManager myCollisionManager = new CollisionManager();
     private String myChoice;
 
     public MenuMode (String menuId) {
         super();
         myMenuId = menuId;
         myChoice = "";
     }
 
     @Override
     public void update (double stepTime, Dimension bounds) {
         List<GameObject> myObjects = getMyObjects();
         handleCollisions();
         for (int i = 0; i < myObjects.size(); i++) {
             GameObject object = myObjects.get(i);
             object.update();
             if (object.shouldBeRemoved()) {
                 myObjects.remove(object);
                 i--;
             }
         }
     }
 
     @Override
     public void initializeMode () {
         myMenuGrid = new MenuGrid(myMenuId, this);
         myMenuObjects = myMenuGrid.getMenuObjects();
         for (MenuObject menu : myMenuObjects) {
             addObject(menu);
         }
     }
 
     public void handleCollisions() {
         myCollisionManager.checkCollisions(getMyObjects());
     }
 
     public String getMenusNext(String value){
     	for(MenuObject menu : myMenuObjects){
     		if(menu.getValue().equals(value)) return menu.getNext(); 
     	}
     	return "";
     }
 
     @Override
     public void addObject (GameObject object) {
         super.addObject(object);
         if (object instanceof MouseClickObject) {
             myMouseClick = (MouseClickObject) object;
         }
     }
     
     public void setChoice (String choice){
     	myChoice = choice;
     }
     
     public String getChoice(){
     	String choice = myChoice;
     	myChoice = "";
     	return choice;
     }
 
 }
