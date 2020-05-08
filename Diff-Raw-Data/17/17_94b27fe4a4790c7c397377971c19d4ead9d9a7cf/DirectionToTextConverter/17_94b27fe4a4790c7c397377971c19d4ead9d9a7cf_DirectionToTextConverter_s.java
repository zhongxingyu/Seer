 /**
  * 
  */
 package fi.wegar.android.gravnav;
 
import fi.wegar.gravnav.R;
 import android.content.Context;
 
 /**
  * Generates a text from a given direction integer, telling the user which direction to go. Integers must range from 1 to X
  * 
  * @author jenswegar
  *
  */
 public class DirectionToTextConverter {
 
 	static final String FIRST = "st";
 	static final String SECOND = "nd";
 	static final String THIRD = "rd";
 	static final String N_TH = "th";
 
 	
 	/**
 	 * 
 	 * @param dir
 	 * @param numChoices
 	 * @param ctx The context of the application, used for generating the return string.
 	 * @return A String representing the direction to travel, e.g. 4th choice from the left
 	 */
 	public static String getText(int dir, int numChoices, Context ctx) {
 		
 		String rtnStr = ctx.getResources().getString(R.string.go)+" ";
 		
 		
 		if( numChoices == 2 ) {
 			// special case, we'll use a switch to tell where to go
 			rtnStr += getChoiceFromTwo(dir, ctx);
 		} else if(numChoices == 3){
 			// special case, we'll use a switch to tell where to go
 			rtnStr += getChoiceFromThree(dir, ctx);
 		} else {
 			// need to calculate the center
 			
 			String leftRightCenter = "";
 			
 			int center = (int) Math.floor(numChoices / 2) + 1;
 			
 			// check if dir is below or above center, determines left or right direction
 			leftRightCenter = (center > dir) ? ctx.getResources().getString(R.string.left) : ctx.getResources().getString(R.string.right);
 
 			
 			
 			if(numChoices % 2 > 0 && dir == center) {
 				// the center direction was chosen
 				leftRightCenter = ctx.getResources().getString(R.string.straight);
 			} else {
 				
 				int tmpDir = dir;
 				
 				if(dir >= center) {
 					// we're on the right side of straight, so need to invert the tmpDir
 					tmpDir = (numChoices+1) - dir;
 				}
 				
 				leftRightCenter = getDirSuffix( tmpDir, ctx )+" "+ctx.getResources().getString(R.string.to_your)+" "+leftRightCenter;
 			}
 
 			rtnStr += leftRightCenter;
 			
 		}
 		
 		
 		return rtnStr;
 	}
 
 	/**
 	 * Returns one of the constants FIRST, SECOND, THIRD, N_TH based on the given order value
 	 * @param order
 	 * @return
 	 */
 	private static String getDirSuffix(int order, Context ctx) {
 		String rtnStr = ""+order;
 		
 		switch(order) {
 			case 1:
 				rtnStr += ctx.getResources().getString(R.string.first_short);
 				break;
 			case 2:
 				rtnStr += ctx.getResources().getString(R.string.second_short);
 				break;
 			case 3:
 				rtnStr += ctx.getResources().getString(R.string.third_short);
 				break;
 			default:
 				rtnStr += ctx.getResources().getString(R.string.nth_short);
 				break;
 			}
 		
 		return rtnStr;
 	}
 
 
 	private static String getChoiceFromThree(int dir, Context ctx) {
 		
 		String rtnStr = "";
 		
 		switch(dir) {
 			case 1:
 				rtnStr = ctx.getResources().getString(R.string.left);
 				break;
 			case 2:
 				rtnStr = ctx.getResources().getString(R.string.straight);
 				break;
 			case 3:
 				rtnStr = ctx.getResources().getString(R.string.right);
 				break;
 		}
 		
 		return rtnStr;
 	}
 	
 	private static String getChoiceFromTwo(int dir, Context ctx) {
 		
 		String rtnStr = "";
 		
 		switch(dir) {
 		case 1:
 			rtnStr = ctx.getResources().getString(R.string.left);
 			break;
 		case 2:
 			rtnStr = ctx.getResources().getString(R.string.right);
 			break;
 		}
 		
 		return rtnStr;
 	}
 	
 }
