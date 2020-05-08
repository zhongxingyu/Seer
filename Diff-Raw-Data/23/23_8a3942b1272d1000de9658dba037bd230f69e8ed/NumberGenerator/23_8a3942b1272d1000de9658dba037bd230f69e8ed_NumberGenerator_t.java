 package com.garis.generator;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.garis.generator.chess.Cell;
 import com.garis.generator.chess.KeyPad;
import com.garis.generator.chess.PieceFactory;
 import com.garis.generator.chess.pieces.Queen;
 
 public class NumberGenerator {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		int[][] cellsArray = {  { 1, 2, 3}, 
 								{ 4, 5, 6},
 								{ 7, 8, 9}, 
 								{ 11, 0, 12} 
 				 			 };
 		KeyPad pad = new KeyPad(cellsArray);
		pad.setDeepness(6);
 		pad.setMaxColumn(3);
 		pad.setMaxRow(4);
		pad.setPiece(PieceFactory.buildQueen());
		pad.setPrintDigits(true);
 		List<Cell> invalidKeys = new ArrayList<Cell>();
 		invalidKeys.add(new Cell(3,0));
 		invalidKeys.add(new Cell(3,2));
 		pad.setInvalidKeys(invalidKeys);
		List<Cell> invalidStartingKeys = new ArrayList<Cell>();
		invalidStartingKeys.add(new Cell(0,0));
		invalidStartingKeys.add(new Cell(3,1));
		pad.setInvalidStartingKeys(invalidStartingKeys);
 		try {
 			pad.setStartingCell(new Cell(0, 2));
 			pad.getWiredCells();
 			System.out
 					.println("Phone numbers that can be generated with the selected piece: "
 							+ pad.getMaxDigits());
			System.out.println(pad.getDigits().toString());
 		} catch (InvalidPositionException e) {
 			e.printStackTrace();
 		}

 	}
 
 }
