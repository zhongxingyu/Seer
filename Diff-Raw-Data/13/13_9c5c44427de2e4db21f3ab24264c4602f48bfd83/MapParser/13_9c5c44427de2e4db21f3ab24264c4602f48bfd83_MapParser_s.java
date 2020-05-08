 /*
  * Copyright (c) 2013 Jimmy Zhang
  * See the file COPYING.txt for full copyright information
  */
 
 package crazytd.map;
 
 import crazytd.map.Block.Direction;
 
 public class MapParser {
 	
 	/**
 	 * xx = wasteland 
 	 * bb = buildable 
 	 * cc = castle 
 	 * d("n","s","w","e") = MonsterDen with out direction as one of North,South,West, or East
 	 * ("n","s","w","e")("n","s","w","e") = Road with inDirection specified by the first character and out direction by the second
 	 */
 	public static String testmap1 =   
 			"xx,bb,bb,bb,bb,bb,bb;" +
 			"de,we,ws,bb,se,ws,bb;" +
 			"xx,bb,ns,bb,sn,ns,bb;" +
 			"xx,bb,ne,we,wn,ns,bb;" +
 			"xx,bb,bb,bb,bb,ns,bb;" +
 			"xx,xx,xx,xx,xx,cc,xx"; 
 
 	public static String testmap2 = 
 			"xx,bb,xx";
 	
 	/**
 	 * @param map string representation of the map, follow the pattern described above
 	 */
 	public static Map parse(String map){
 		String[][] elements = toStringList(map);
 		Map result = new Map(elements[0].length,elements.length);
 		
 		for (int i=0;i<elements.length;i++){
 			for (int j=0;j<elements[i].length;j++){
 				try {
					Block blk = stringToBlock(elements[i][j],i,j);
 					result.theMap[i][j] = blk;
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @param str a two character string such as "cc" representing a block
 	 * @param x the x index of the block
 	 * @param y the y index of the block
 	 * @return Block object corresponding to the string representation
 	 * @throws Exception invalid string
 	 */
 	private static Block stringToBlock(String str,int x,int y) throws Exception{
 		Block blk = null;
 		if (str.equals("xx")){
 			blk = new WasteLand(x,y);
 		}
 		
 		else if (str.equals("bb")){
 			blk = new Buildable(x,y);
 		}
 		
 		else if (str.equals("cc")){
 			blk = new Castle(x,y);
 		}
 		
 		else if (str.startsWith("d")){
 			Direction dir = stringToDir(str.substring(1, 2));			
 			blk = new MonsterDen(x,y,dir);
 		}
 		
 		else {
 			Direction inDir = stringToDir(str.substring(0,1));
 			Direction outDir = stringToDir(str.substring(1,2));
 			blk = new Road(x,y,inDir,outDir);
 		}
 		return blk;
 	}
 	
 	/**
 	 * @param str a single character, one of {"n","s","e","w"}
 	 * @return Direction enum
 	 * @throws Exception
 	 */
 	private static Direction stringToDir(String str) throws Exception{
 		Direction dir = null;
 		if (str.equals("w")) dir = Block.Direction.WEST;
 		else if (str.equals("e")) dir = Block.Direction.EAST;
 		else if (str.equals("n")) dir = Block.Direction.NORTH;
 		else if (str.equals("s")) dir = Block.Direction.SOUTH;
 		else throw new Exception("Monster Den requires valid direction");
 		return dir;
 	}
 	
 	/**
 	 * Breaks the String into a string matrix
 	 * @param map a string representation of the TD map
 	 * @return
 	 */
 	private static String[][] toStringList(String map){
 		String[] rows = map.split(";");
 		int verticalLength = rows.length;
 		int horizontalLength = rows[0].split(",").length;
 		
 		String[][] elements = new String[verticalLength][horizontalLength];
 		for (int i=0;i<rows.length;i++){
 			elements[i] = rows[i].split(",");
 		}
 		return elements;
 	} 
 	
 	
 }
