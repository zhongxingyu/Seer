 package Tutorial_0_1;
 
 import com.gmail.robmadeyou.Screen;
 import com.gmail.robmadeyou.Screen.GameType;
 import com.gmail.robmadeyou.Entity.Enemy;
 import com.gmail.robmadeyou.Entity.EntityList;
 import com.gmail.robmadeyou.Entity.Player;
 import com.gmail.robmadeyou.Input.Keyboard;
 import com.gmail.robmadeyou.Input.Keyboard.Key;
 import com.gmail.robmadeyou.Input.Mouse;
 
 public class Main {
 	/*
 	 * Main method previously seen
 	 */
 	public static void main(String[] args) {
 		//Creating screen as previously seen again
 		Screen.create(640, 640, "Name", GameType.SIDE_SCROLLER, false);
 		/*
 		 * This is new. We are creating a new Entity called Player. The new entity
 		 * is called player. Java is case sensetive so we are able to do this.
 		 * 
 		 * after = new Player
 		 * 
 		 * we are setting up the player dimensions. Initially there are very few things we actually set
 		 * everything else is stated later on. Things like movement, speed and jump height (there are more)
 		 */
 		Player player = new Player(40, 40, 20, 40);
 		/*
 		 * After creating the player entity we have to add it to the global list of entities. We do this by
 		 * using the class EntityList and the method addEntity, in the parameters of the method we name the entity we
 		 * would like to add, in this case we want to add player
 		 */
 		EntityList.addEntity(player);
 		/*
 		 * Now that the player is added to the screen, things will change but you won't be able to see anything. To 
 		 * view changes you must first set up the world. Remove the two forward slash lines ("//") on the line below
 		 * to set up the world and see what happens then!
 		 */
		Screen.setUpWorld();
 		/*
 		 * You should now be able to move around with the Arrow Keys.
 		 */
 		while(!Screen.isAskedToClose()){
 			Screen.update(60);
 			Screen.refresh();
 		}
 	}
 }
