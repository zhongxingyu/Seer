 
 package com.lofibucket.yotris.logic;
 
 import org.junit.Test;
 import static org.junit.Assert.*;
 import org.junit.Before;
 import com.lofibucket.yotris.util.TileColor;
 
 public class PieceTest {
 
 	private Piece piece;
 
 	private static final Tile blue = new Tile(TileColor.BLUE);
 	private static final Tile[][] tiles_long_right = 
 		{
 		{null, null, 	null, null},
 		{blue, blue, 	blue, blue},
 		{null, null, 	null, null},
 		{null, null, 	null, null}
 		};
 
 	private static final Tile[][] tiles_long_down = 
 		{
 		{null, null, 	blue, null},
 		{null, null, 	blue, null},
 		{null, null, 	blue, null},
 		{null, null, 	blue, null}
 		};
 
 	private static final Tile[][] tiles_long_left = 
 		{
 		{null, null, 	null, null},
 		{null, null, 	null, null},
 		{blue, blue, 	blue, blue},
 		{null, null, 	null, null}
 		};
 
     public PieceTest() {
 
     }
 
 	public void printObjectArray(Object [][] arr) {
 		int width = arr[0].length;
 		int height = arr.length;
 		for (int y=0;y<height;y++) {
 			for (int x=0;x<width;x++) {
 				String c = "X";
 
 				if (arr[y][x] == null) {
 					c = ".";
 				}
 				System.out.print(c);		
 			}
 			System.out.println("");
 		}
 	}
 
 	@Before
 	public void initPiece() {
 		piece = new Piece(Tetrimino.O, TileColor.BLUE, new Position(0, 0));
 	}
 
 	@Test
 	public void testPieceDefaultRotation() {
 		assertEquals(Rotation.UP, piece.getRotation());
 	}
 
 	@Test
 	public void testPieceRotationValue() {
 		piece.rotateClockwise();
 		assertEquals(Rotation.RIGHT, piece.getRotation());
 
 		piece.rotateClockwise();
 		assertEquals(Rotation.DOWN, piece.getRotation());
 
 		piece.rotateCounterClockwise();
 		piece.rotateCounterClockwise();
 		assertEquals(Rotation.UP, piece.getRotation());
 	}
 
 	@Test
 	public void testPieceRotationTiles() {
 		Piece piece_long = new Piece(Tetrimino.I, TileColor.BLUE, new Position(0, 0));
 
		/*
 		piece_long.rotateClockwise();
		Tile[][] tiles = piece_long.getRotatedTiles();
		//printObjectArray(tiles);
		assertArrayEquals(tiles, tiles_long_right);
 
 		piece_long.rotateClockwise();
 		tiles = piece_long.getRotatedTiles();
		assertArrayEquals(tiles, tiles_long_down);
 
 		piece_long.rotateClockwise();
 		tiles = piece_long.getRotatedTiles();
		assertArrayEquals(tiles, tiles_long_left);
 
 		piece_long.rotateCounterClockwise();
 		piece_long.rotateCounterClockwise();
 		tiles = piece_long.getRotatedTiles();
		assertArrayEquals(tiles, tiles_long_right);
		*/
 	}
 
 	@Test
 	public void testTetriminoParsing() {
		/*
		Tile[][] tiles = piece.getTiles();
 
		assertEquals(tiles[0].length, Tetrimino.O[0].length);
		assertEquals(tiles.length, Tetrimino.O.length);
 
 		for (int y=0;y<piece.getHeight();y++) {
 			for (int x=0;x<piece.getWidth();x++) {
 				if (Tetrimino.O[y][x])	 {
					assertTrue(tiles[y][x] != null);
 				}
 			}
 		}
		*/
 
 	}
 
 }
