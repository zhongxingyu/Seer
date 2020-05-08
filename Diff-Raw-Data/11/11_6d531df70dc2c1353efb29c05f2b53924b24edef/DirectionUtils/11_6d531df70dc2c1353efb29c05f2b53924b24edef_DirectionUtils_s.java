 package widux.core;
 
 public class DirectionUtils
 {
 	
 	/**
 	 * Reverses the current direction/side.
 	 * @param originalDirection The original facing direction/side.
 	 * @return The opposite of originalDirection.
 	 */
 	public static int reverseDirection(int originalDirection)
 	{
 		switch(originalDirection)
 		{
 			case 0: return 1;
 			case 1: return 0;
 			case 2: return 3;
 			case 3: return 2;
 			case 4: return 5;
 			case 5: return 4;
 			default: return 0;
 		}
 	}
 	
 	/**
 	 * Takes co-ordinates and moves a certain amount into a certain direction, then gives out the new co-ordinates.
 	 * @param origX The original X co-ordinate.
 	 * @param origY The original Y co-ordinate.
 	 * @param origZ The original Z co-ordinate.
 	 * @param direction The direction to move in.
 	 * @param amount The amount (in blocks) to move in that direction.
 	 * @return The new co-ordinates, after moving in the requested direction for the requested amount of blocks. Format: {newX, newY, newZ}
 	 */
 	public static int[] moveInDirection(int origX, int origY, int origZ, int direction, int amount)
 	{
 		int[] newLoc = {origX, origY, origZ};
 		
 		switch(direction) // <- That looks funny :D
 		{
 			case 0:
 				newLoc[1] -= amount;
 				break;
 			case 1:
 				newLoc[1] += amount;
 				break;
 			case 2:
				newLoc[0] += amount;
 				break;
 			case 3:
				newLoc[0] -= amount;
 				break;
 			case 4:
				newLoc[2] -= amount;
 				break;
 			case 5:
				newLoc[2] += amount;
 				break;
 		}
 		
 		return newLoc;
 	}
 	
 }
