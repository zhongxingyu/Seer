 package edgematching.problem;
 
 import java.util.*;
 
 /*
  * Class for a piece of the edgematching-puzzle
  */
 public class Piece
 {
 	/*
 	 * number of lines needed for printing the piece to the screen
 	 */
 	public static final int getStringLineCount = 7;
 
 	/* 
 	 * colors of the piece clockwise stored in this list
 	 */
 	protected ArrayList<Integer> m_colors;
 
 	/* 
 	 * rotation of piece (in {0,1,2,3}) in 90 degree steps clockwise
 	 * used for the solution of our problem
 	 */
 	protected int m_rotation;
 
 	protected int m_hash_code;
 
 	/*
 	 * constructor with all 4 colors given
 	 * --> are inserted directly
 	 */
 	public Piece (int color1, int color2, int color3, int color4)
 	{
 		m_colors = new ArrayList<Integer> (4);
 		m_rotation = 0;
 
 		m_colors.add (0, color1);
 		m_colors.add (1, color2);
 		m_colors.add (2, color3);
 		m_colors.add (3, color4);
 
 		m_hash_code = color1 + color2 + color3 + color4;
 	}
 
 	/*
 	 * getter and setter of rotation
 	 */
 	public void setRotation (int rotation)
 	{
 		m_rotation = rotation % 4;
 	}
 
 	public int getRotation ()
 	{
 		return m_rotation;
 	}
 
 	public void setColor (int index, int color)
 	{
 		int old_color = m_colors.get ((index + 4 - m_rotation) % 4);
 		m_colors.set((index + 4 - m_rotation) % 4, color);
 
 		m_hash_code -= old_color;
 		m_hash_code += color;
 	}
 	/*
 	 * get color at position wrt current rotation
 	 */
 	public int getColor (int index)
 	{
 		return m_colors.get((index + 4 - m_rotation) % 4);
 	}
 
 	/*
 	 * removes colors of this piece from a given collection of colors
 	 */
 	public void removeAllColorsFrom (Collection<Integer> colorCollection)
 	{
 		colorCollection.removeAll (m_colors);
 	}
 
 	/*
 	 * inserts colors of this piece into a given collection of colors
 	 */
 	public void insertAllColors (Collection<Integer> colorCollection)
 	{
 		colorCollection.addAll (m_colors);
 	}
 
 	/*
 	 * inserts border-colors of this piece into a given set of colors
 	 */
 	public void insertBorderColors (Set<Integer> borderColorSet)
 	{
 		for (int i = 0; i < 4; i++) {
 			if (m_colors.get (i) == 0) {
 				int leftColor  = m_colors.get ((i+3)%4);
 				int rightColor = m_colors.get ((i+1)%4);
 
 				if (leftColor  != 0) borderColorSet.add (leftColor);
 				if (rightColor != 0) borderColorSet.add (rightColor);
 			}
 		}
 	}
 
 	/*
 	 * return color left to 0 of border or corner piece
 	 */
 	public int getBorderColorLeft ()
 	{
 		for (int i = 0; i < 4; i++) {
 			if ((m_colors.get (i) != 0) && (m_colors.get ((i + 1) % 4) == 0)) {
 				return (m_colors.get (i));
 			}
 		}
 
 		return -1;
 	}
 
 	/*
 	 * return color right to 0 of border or corner piece
 	 */
 	public int getBorderColorRight ()
 	{
 		for (int i = 0; i < 4; i++) {
 			if ((m_colors.get (i) == 0) && (m_colors.get ((i + 1) % 4) != 0)) {
 				return (m_colors.get ((i + 1) % 4));
 			}
 		}
 
 		return -1;
 	}
 
 	/*
 	 * return color not next to 0 of border piece
 	 */
 	public int getBorderColorBottem ()
 	{
 		for (int i = 0; i < 4; i++) {
 			if (m_colors.get (i) == 0) {
 				return (m_colors.get ((i + 2) % 4));
 			}
 		}
 
 		return -1;
 	}
 
 	/*
 	 * get amount of the given color ...
 	 * needed for finding corner/border-pieces --> amount of 0-color
 	 */
 	public int getAmountOfColor (int color)
 	{
 		int result = 0;
 
 		for (Integer i_color : m_colors) {
 			if (i_color == color) result++;
 		}
 
 		return result;
 	}
 
 	/*
 	 * get classification of the piece according to paper by Marijn J.H. Heule
 	 * writes colors in descending order to color1 to 4
 	 */
 	public int getClassification (ArrayList<Integer> colors)
 	{
 		int result = 0;
 
 		colors.clear ();
 
 		int cl1 = m_colors.get (0);
 		int cl2 = m_colors.get (1);
 		int cl3 = m_colors.get (2);
 		int cl4 = m_colors.get (3);
 
 		if (cl1 == cl2) { //1,2,3,5 - 1=2
 			colors.add (0, cl1);
 			if (cl1 == cl3) { //1,2 - 1=2=3
 				if (cl1 == cl4) { //1 - 1=2=3=4
 					result = 1;
 				} else {          //2 - 1=2=3!=4
 					colors.add (1, cl4);
 					result = 2;
 				}
 			} else {          //2,3,5 - 1=2!=3
 				colors.add (1, cl3);
 				if (cl3 == cl4) { //3 - 1=2!=3=4
 					result = 3;
 				} else {          //2,5 - 1=2!=3, 3!=4
 					if (cl1 == cl4) { //2 - 1=2=4!=3
 						result = 2;
 					} else {          //5 - 1=2!=3, 3!=4, 1!=4
 						colors.add (2, cl4);
 						result = 5;
 					}
 				}
 			}
 		} else {          //2,3,4,5,6,7 - 1!=2
 			if (cl1 == cl3) { //2,4,6 - 1=3!=2
 				colors.add (0, cl1);
 				colors.add (1, cl2);
 				if (cl1 == cl4) { //2 - 1=3=4!=2
 					result = 2;
 				} else {          //4,6 - 1=3, 1!=2, 1!=4
 					if (cl2 == cl4) { //4 - 1=3!=2=4
 						result = 4;
 					} else {          //6 - 1=3, 1!=2, 1!=4, 2!=4
 						colors.add (2, cl4);
 						result = 6;
 					}
 				}
 			} else {          //2,3,5,6,7 - 1!=2, 1!=3
 				if (cl1 == cl4) { //3,5 - 1=4, 1!=2, 1!=3
 					colors.add (0, cl1);
 					colors.add (1, cl2);
 					if (cl2 == cl3) { //3 - 1=4!=2=3
 						result = 3;
 					} else {          //5 - 1=4, 1!=2, 1!=3, 2!=3
 						colors.add (2, cl3);
 						result = 5;
 					}
 				} else {          //2,5,6,7 - 1!=2, 1!=3, 1!=4
 					if (cl2 == cl3) { //2,5 - 2=3, 1!=2, 1!=4
 						colors.add (0, cl2);
 						if (cl2 == cl4) { //2 - 2=3=4!=1
 							colors.add (1, cl1);
 							result = 2;
 						} else {          //5 - 2=3, 2!=1, 2!=4, 1!=4
 							colors.add (1, cl4);
 							colors.add (2, cl1);
 							result = 5;
 						}
 					} else {          //5,6,7 - 1!=2, 1!=3, 1!=4, 2!=3
 						if (cl2 == cl4) { //6 - 2=4, 2!=1, 2!=3, 1!=3
 							colors.add (0, cl2);
 							colors.add (1, cl1);
 							colors.add (2, cl3);
 							result = 6;
 						} else {          //5,7 - 1!=2, 1!=3, 1!=4, 2!=3, 2!=4
 							if (cl3 == cl4) { //5 - 3=4, 3!=1, 3!=2, 1!=2
 								colors.add (0, cl3);
 								colors.add (1, cl1);
 								colors.add (2, cl2);
 								result = 5;
 							} else {          //7 - all 4 different
 								colors.add (0, cl1);
 								colors.add (1, cl2);
 								colors.add (2, cl3);
 								colors.add (3, cl4);
 								result = 7;
 							}
 						}
 					}
 				}
 			}
 		}
 
 		return result;
 	}
 
 	public String toProblemString ()
 	{
 		String result = Integer.toString (getColor (0));
 
 		for (int i = 1; i < 4; i++) {
 			result += " " + getColor (i);
 		}
 
 		return result;
 	}
 
 	/*
 	 * converts piece to an output-String (line-by-line)
 	 */
 	public String toString ()
 	{
 		String result = new String ();
 
 		for (int i = 0; i < getStringLineCount; i++) {
 			result += getStringLine (i) + "\n";
 		}
 
 		return result;
 	}
 
 	/*
 	 * delivers one line of output-String
 	 * --> needed if more than 2 or more pieces are printed besides each other
 	 *  --> first lines of each pieces are concatenated to one line, ...
 	 */
 	public String getStringLine (int index)
 	{
 		switch (index) {
 			case 0: return "+-------+";
 			case 1: 
 				String color_0_str = Integer.toString (getColor (0));
 				switch (color_0_str.length ()) {
 					case 1: color_0_str = " " + color_0_str + " ";
 						break;
 					case 2: color_0_str += " ";
 						break;
 				}
 				return "|\\ " + color_0_str + " /|";
 			case 2: return "|  \\ /  |";
 			case 3: 
 				String color_3_str = Integer.toString (getColor (3));
 				switch (color_3_str.length ()) {
 					case 1: color_3_str = color_3_str + "  ";
 						break;
 					case 2: color_3_str += " ";
 						break;
 				}
				String color_1_str = Integer.toString (getColor (1));
				switch (color_1_str.length ()) {
					case 1: color_1_str = "  " + color_1_str;
 						break;
					case 2: color_1_str = " " + color_1_str;
 						break;
 				}
				return "|" + color_3_str + "X" + color_1_str + "|";
 			case 4: return "|  / \\  |";
 			case 5:
 				String color_2_str = Integer.toString (getColor (2));
 				switch (color_2_str.length ()) {
 					case 1: color_2_str = " " + color_2_str + " ";
 						break;
 					case 2: color_2_str += " ";
 						break;
 				}
 				return "|/ " + color_2_str + " \\|";
 			case 6: return "+-------+";
 			default: return null;
 		}
 	}
 
 	public int hashCode ()
 	{
 		return m_hash_code;
 	}
 
 	public boolean equals (Object o)
 	{
 		// if null passed, check for null
 		if (o == null) {
 			if (this == null) return true;
 			return false;
 		}
 
 		// else check for type of other object
 		if (!(o instanceof Piece)) return false;
 
 		Piece other_piece = (Piece) o;
 		
 		if (m_hash_code != other_piece.m_hash_code) return false;
 
 		for (int start_color = 0; start_color < 4; start_color ++) {
 			for (int count = 0; count < 4; count ++) {
 				if (m_colors.get (count) != other_piece.m_colors.get ((start_color + count) % 4)) break;
 
 				if (count == 3) return true;
 			}
 		}
 
 		return false;
 	}
 }
