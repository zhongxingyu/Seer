 
 /**
  * as this class uses the scale-method which all classes that implement Pict
  * have definied, the generic type of the class must extend Pict
  * 
  * Scaled extends Repeated as it has the addtional requirement that it's
  * type must have a .scale()-method and the global scale is always 1.0
  * 
  * Scaled is a character-grid
  * 
  * @author OOP Gruppe 187
  * 
  */
 
public class Scaled<P extends Pict> extends Repeated<Pict> {
 	private Pict data[][];
 
 	/**
 	 * constructor which takes an array of pictograms; only used to make
 	 * debugging easier
 	 * 
 	 * @param data
 	 *            array with all the values
 	 */
 	public Scaled(P data[][]) {
 		assert (data != null) : "array cant be null";
 		assert (data[0] != null) : "array cant be null";
 
 		this.data = new Pict[data.length][data[0].length];
 
 		for (int i = 0; i < data.length; i++) {
 			for (int j = 0; j < data[0].length; j++) {
 				assert (data[i][j] != null) : "array-element cannot be null";
 				this.data[i][j] = data[i][j];
 			}
 		}
 	}
 
 	/**
 	 * scales the size of each box by calling their scale-method
 	 * 
 	 * @param factor
 	 */
 	public void scale(double factor) {
 		assert (0.1 <= factor && factor <= 10.0) : "invalid factor";
 
 		// scale objects
 
 		for(int i = 0; i < data.length; i++) {
 			for(int j = 0; j < data[0].length; j++) {
 				data[i][j].scale(factor);
 			}
 		}
 	}
 
 	/**
 	 * returns a string-list of all boxes
 	 * 
 	 * @return string-representation
 	 */
 	public String toString() {
 		StringBuilder ret = new StringBuilder();
 
 		int maxWidth = PictHelper.getMaxWidth(data);
 		int maxHeight = PictHelper.getMaxHeight(data);
 
 		String currentLine;
 		int currentWidth;
 
 		for(int i = 0; i < data.length; i++) {
 			for(int k = 0; k < maxHeight; k++) {
 				for(int j = 0; j < data[0].length; j++) {
 					currentLine = getLine(data[i][j].toString(), k);
 					currentWidth = PictHelper.getWidth(currentLine);
 					ret.append(currentLine);
 					for(int l = currentWidth; l < maxWidth; l++) {
 						ret.append(" ");
 					}
 				}
 				ret.append("\n");
 			}
 			
 		}
 
 		return ret.toString();
 	}
 
 }
