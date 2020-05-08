 package assignment2;
 
 
 /**
  * Implement the Morton Code manipulations here. 
  *
  */
 public class MortonCodes {
 	
 	/** the three masks for dilated integer operations */
 	public static final long xmask = 0b100100100100100100100100100100100100100100100100100100100100100L, 
 			ymask = 0b010010010010010010010010010010010010010010010010010010010010010L, 
 			zmask = 0b001001001001001001001001001001001001001001001001001001001001001L;
 	
 	
 	/**
 	 * return the parent morton code
 	 * @param code
 	 * @return
 	 */
 	public static long parentCode(long code){
 		return code >> 3;
 	}
 	
 	/**
 	 * return the (positive) neighbor code at the relative position encoded by
 	 * 0bxyz using dilated addition
 	 * @param code
 	 * @param level
 	 * @param Obxyz
 	 * @return
 	 */
 	public static long nbrCode(long code, int level, int plusxyz){
 		long xresult = ((code | ~xmask) + (plusxyz & xmask)) & xmask;
 		long yresult = ((code | ~ymask) + (plusxyz & ymask)) & ymask;
 		long zresult = ((code | ~zmask) + (plusxyz & zmask)) & zmask;
 		long result = xresult | yresult | zresult;
 		if (isOverflown(result, level))
 			return -1L;
 		else
 			return result;
 	}
 
 	/**
 	 * return the (negative) neighbor code at the relative position encoded by
 	 * 0bxyz using dilated subtraction
 	 * @param code
 	 * @param level
 	 * @param Obxyz
 	 * @return
 	 */	
 	public static long nbrCodeMinus(long code, int level, int minusxyz){
 		long xresult = ((code & xmask) - (minusxyz & xmask)) & xmask;
 		long yresult = ((code & ymask) - (minusxyz & ymask)) & ymask;
 		long zresult = ((code & zmask) - (minusxyz & zmask)) & zmask;
 		long result = xresult | yresult | zresult;
 		if (isOverflown(result, level))
 			return -1L;
 		else
 			return result;
 	}
 	
 	
 	
 	/**
 	 * A test to check if an overflow/underflow has occurred. it is enough to test
 	 * if the delimiter bit is untouched and is the highest bit set.
 	 * @param code
 	 * @param level
 	 * @return true, if overflow occured
 	 */
 	public static boolean isOverflown(long code, int level){	
 		return (code >> (3*level)) != 0b1;
 		
 	}
 	
 	
 	/**
 	 * Check if the cell_code is a morton code associated to the grid level
 	 * given in the argument. A cell code is associated to a specific level
 	 * @param cell_code
 	 * @param level
 	 * @return
 	 */
 	public static boolean isCellOnLevelXGrid(long cell_code, int level){
 		return 1L == cell_code >> (3*level);
 	}
 	
 	
 	/**
 	 * A test to check if the vertex_code (a morton code padded with zeros to have the length
 	 * 3*tree_depth + 1) is associated to a vertex which is part of the {@param level}-grid.
 	 * 
 	 * This is determined by the number of trailing zeros, and if a vertex lies on some level k
 	 * it will lie on the levels k+1,k+2... tree_depth too.
 	 */
 	public static boolean isVertexOnLevelXGrid(long vertex_code, int level, int tree_depth) {
 		//tests, how many times zeros were padded
 		// 1 time padded => on level tree_depth - 1 etc...
 		long mask = ~(-1L << ((tree_depth - level)*3));
 		long masked = vertex_code & mask;
		return masked == mask;
 	}
 	
 	/**
 	 * A test to check if a vertex code is logically describing a boundary vertex.
 	 */
 	public static boolean isVertexOnBoundary(long vertex_code, int tree_depth){
 		boolean is = (vertex_code & (0b111 << 3*(tree_depth-1)))!= 0 || //x==1, y==1 or z==1 in a unit cube
 				(vertex_code & xmask) == 0 || //x==0
 				(vertex_code & ymask) == 0 || //y==0
 				(vertex_code & zmask) == (0b1 << 3*tree_depth) ; //z==0 (only the delimiter bit is set)
 		
 		return is;
 	}
 	
 }
