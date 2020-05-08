 package beatbots.simulation;
 
 import org.newdawn.slick.Color;
 
 
 public strictfp final class Utils {
 	
 	private Utils() {
 		
 		super();
 	}
 	
 	public static NoteColor getNoteColor(int index) {
 		
 		switch (index) {
 			
		case 0:
 			
 			return NoteColor.Red;
 			
		case 1:
 			
 			return NoteColor.Blue;
 			
		case 2:
 			
 			return NoteColor.Yellow;
 			
		case 3:
 			
 			return NoteColor.Magenta;
 			
		case 4:
 			
 			return NoteColor.Orange;
 			
		case 5: 
 			
 			return NoteColor.Green;
 			
 		default:
 			
 			return NoteColor.White;
 			
 		}
 	}
 	
 	public static Color getColor(NoteColor noteColor) {
 		
 		switch (noteColor) {
 		
 		case Red:
 			
 			return Color.red;
 			
 		case Blue:
 			
 			return Color.cyan;
 			
 		case Yellow:
 			
 			return Color.yellow;
 			
 		case Magenta:
 			
 			return Color.magenta;
 			
 		case Orange:
 			
 			return Color.orange;
 			
 		case Green:
 			
 			return Color.green;
 
 		default:
 			
 			return Color.white;
 		}
 	}
 	
 	public static NoteColor mix(NoteColor original, NoteColor mixin) {
 		
 		if (original == mixin) {
 			
 			return original;
 		}
 		else {
 			
 			switch (original) {
 			
			case White:
				
				return mixin;
			
 			case Red:
 				
 				switch (mixin) {
 					
 				case Blue:
 					
 					return NoteColor.Magenta;
 					
 				case Yellow:
 					
 					return NoteColor.Orange;
 				}
 				
 				break;
 
 			case Blue:
 				
 				switch (mixin) {
 					
 				case Red:
 					
 					return NoteColor.Magenta;
 					
 				case Yellow:
 					
 					return NoteColor.Green;
 				}
 				
 				break;
 				
 			case Yellow:
 				
 				switch (mixin) {
 				
 				case Red:
 					
 					return NoteColor.Orange;
 					
 				case Blue:
 					
 					return NoteColor.Green;
 				}
 				
 				break;
 					
 			}
 			
 			return original;
 		}
 	}
 }
