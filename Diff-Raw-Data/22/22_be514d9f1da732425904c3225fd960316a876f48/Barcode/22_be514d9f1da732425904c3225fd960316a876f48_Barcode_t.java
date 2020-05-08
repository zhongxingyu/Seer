 package mapping;
 
 public class Barcode extends TileContent{
 
 	private int value;
 	
 	/**
 	 * Creates a barcode with as value 'value'.
 	 */
 	public Barcode(int value){
 		setValue(value);
 	}
 	
 	/**
 	 * Creates a default, empty barcode. 
 	 * (value == 0).
 	 */
 	public Barcode(){
 		this(0);
 	}
 
 	/**
 	 * Gets the decimal value this barcode represents. Should be used to perform the right action.
 	 */
 	public int getValue() {
 		return value;
 	}
 	
 	public void setValue(int value) {
 		this.value = value;
 	}
 	
 	/**
 	 * Gives the color of the barcode on the given x-y-coordinate
 
 	 * @param x relative to the tile! (0,0 is upper-left corner)
 	 * @param y relative to the tile!
 	 * @param direction the orientation the barcode is being crossed over
 	 * (North = from south to North, East = from West to East.
 	 * We simplify the 'mirroring' by stating North = South and West = East
 	 * @return	0, when standing on a black part of the barcode
 	 * 			1, when standing on a white part of the barcode
 	 * 			something else, when standing next to the barcode but on the same tile (brown underground)
 	 */
 	public int getColorValue(double x, double y, Orientation direction)
 	{
 		if (direction == Orientation.NORTH || direction == Orientation.SOUTH){
 
 			if(y < 12) return 2; //TODO COLORVALUE ONDERGROND
 			else if( 12<= y && y<14) return Character.getNumericValue(toString().charAt(0));
 			else if( 14<= y && y<16) return Character.getNumericValue(toString().charAt(1));
 			else if( 16<= y && y<18) return Character.getNumericValue(toString().charAt(2));
 			else if( 18<= y && y<20) return Character.getNumericValue(toString().charAt(3));
 			else if( 20<= y && y<22) return Character.getNumericValue(toString().charAt(4));
 			else if( 22<= y && y<24) return Character.getNumericValue(toString().charAt(5));
 			else if( 24<= y && y<26) return Character.getNumericValue(toString().charAt(6));
 			else if( 26<= y && y<28) return Character.getNumericValue(toString().charAt(7));
 			else return 52; // y > 28
 		}
 		else {
 			if(x < 12) return 2; //TODO COLORVALUE ONDERGROND
 			else if( 12<= x && x<14) return Character.getNumericValue(toString().charAt(0));
 			else if( 14<= x && x<16) return Character.getNumericValue(toString().charAt(1));
 			else if( 16<= x && x<18) return Character.getNumericValue(toString().charAt(2));
 			else if( 18<= x && x<20) return Character.getNumericValue(toString().charAt(3));
 			else if( 20<= x && x<22) return Character.getNumericValue(toString().charAt(4));
 			else if( 22<= x && x<24) return Character.getNumericValue(toString().charAt(5));
 			else if( 24<= x && x<26) return Character.getNumericValue(toString().charAt(6));
 			else if( 26<= x && x<28) return Character.getNumericValue(toString().charAt(7));
 			else return 52;
 		}
 	}
 
 	@Override
 	public String toString() {
		String body = Integer.toBinaryString(getValue());
		while(body.length() < 6)
			body = "0" + body;
		return 0+body+0;
 	}
	
 }
