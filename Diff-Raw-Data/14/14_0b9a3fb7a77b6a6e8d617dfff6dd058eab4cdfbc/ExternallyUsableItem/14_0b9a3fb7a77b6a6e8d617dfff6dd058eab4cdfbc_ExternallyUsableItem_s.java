 package yuuki.item;
 
 import yuuki.action.Action;
 import yuuki.entity.Character;
 
 /**
  * An Item that can be used outside of battle.
  */
 public class ExternallyUsableItem extends UsableItem {
 	
 	/**
 	 * Creates a new ExternallyUsableItem.
 	 * 
 	 * @param id The ID of this item's type.
 	 * @param name The name of this item.
 	 * @param value The value of this item in in-game currency.
 	 * @param image The index of the image for this item.
 	 * @param description The description of this item.
 	 * @param action The action that this item performs.
 	 * @param uses The number of times that this item can be used.
 	 */
 	public ExternallyUsableItem(long id, String name, int value, String image,
 			String description, Action action, int uses) {
 		super(id, name, value, image, description, action, uses);
 	}
 	
 	@Override
 	public boolean isExternal() {
 		return true;
 	}
 	
 	/**
 	 * Uses this item.
 	 * 
 	 * @param user The character using the item.
 	 * @param target The character that the item is being used on.
 	 */
 	public void use(Character user, Character target) {
 		increaseUses(1);
 		a.clearTargets();
 		a.setSkipCost(true);
 		a.setOrigin(user);
 		a.addTarget(target);
 		a.apply();
 	}
 	
 }
