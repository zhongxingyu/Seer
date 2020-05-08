 package chess;
 
 import com.google.common.collect.BiMap;
 import com.google.common.collect.EnumBiMap;
 
 /**
  * A "drawing" of the chess position on the screen. This is useful for the 
 * better readability of the tests involvin the ChessPosition objects.
  * 
  * @author Kestutis
  * 
  */
 public class ChessPositionDiagram {
     
     /*
      * The idea is to represent a chess position as a String of the form:
      * 
      * "    _______________________________________________________   \n" +
      * "   |      |      |      |      |      |      |      |      |  \n" + 
      * "  8|      |      |      |      |      |      |      |      |  \n" + 
      * "   |______|______|______|______|______|______|______|______|  \n" + 
      * "   |      |      |      |      |      |      |      |      |  \n" + 
      * "  7|      |      |      |      |  WK  |      |      |      |  \n" + 
      * "   |______|______|______|______|______|______|______|______|  \n" + 
      * "   |      |      |      |      |      |      |      |      |  \n" + 
      * "  6|      |      |      |      |      |      |      |      |  \n" + 
      * "   |______|______|______|______|______|______|______|______|  \n" + 
      * "   |      |      |      |      |      |      |      |      |  \n" + 
      * "  5|      |      |      |      |      |      |      |      |  \n" + 
      * "   |______|______|______|______|______|______|______|______|  \n" + 
      * "   |      |      |      |      |      |      |      |      |  \n" + 
      * "  4|      |      |  BK  |      |      |      |      |      |  \n" + 
      * "   |______|______|______|______|______|______|______|______|  \n" + 
      * "   |      |      |      |      |      |      |      |      |  \n" + 
      * "  3|      |      |      |      |      |      |      |      |  \n" + 
      * "   |______|______|______|______|______|______|______|______|  \n" + 
      * "   |      |      |      |      |      |      |      |      |  \n" + 
      * "  2|      |      |      |      |      |      |      |      |  \n" + 
      * "   |______|______|______|______|______|______|______|______|  \n" + 
      * "   |      |      |      |      |      |      |      |      |  \n" + 
      * "  1|      |      |      |      |      |      |      |      |  \n" + 
      * "   |______|______|______|______|______|______|______|______|  \n" + 
      * "      a      b      c      d      e      f      g      h      \n" ;
      
      The diagram above contains two pieces: White King on square e7, 
      and Black King on square c4. 
      
      It should be noted, that the diagram must be of exactly this form, to be accepted.     
      */
    
     /**
      * The diagram of the concrete chess position. This diagram must satisfy
      * certain requirements to be valid.
      */
     private String diagram;
     
     /**
      * Instantiates a new chess position diagram.
      * 
      * @param diagram
      *            the diagram showing a chess position
      * @throws IncorrectChessDiagramDrawingException
      *            if something is wrong with the provided diagram.
      */
     private ChessPositionDiagram(String diagram) 
 	    throws IncorrectChessDiagramDrawingException {
 	verifyDiagram(diagram);
 	this.diagram = diagram;
     }
     
     /**
      * Verifies the validity of the diagram.
      * 
      * @param diagram
      *            the diagram showing a chess position
      * @throws IncorrectChessDiagramDrawingException
      *            if something is wrong with the provided diagram.
      */
     private void verifyDiagram(String diagram) 
 	    throws IncorrectChessDiagramDrawingException {
 	String[] diagramLines = diagram.split("\n");
 	if (diagramLines.length != 26)
 	    throw new IncorrectChessDiagramDrawingException("Diagram must contain 26 lines!");
 	String lastLine = diagramLines[diagramLines.length - 1];
 	verifyDiagramTop(diagramLines[0]);
 	verifyDiagramBody(diagramLines);
 	verifyDiagramBottom(lastLine);
     }
 
     /**
      * Verifies if the diagram's top (the very first line) has been drawn
      * correctly.
      * 
      * @param firstLine
      *            the top line of the diagram
      * @throws IncorrectChessDiagramDrawingException
      *            if the first line is not what was expected
      */
     private void verifyDiagramTop(String firstLine) 
 	    throws IncorrectChessDiagramDrawingException {
 	if (!firstLine.equals("    _______________________________________________________   "))
 	    throw new IncorrectChessDiagramDrawingException("Incorrect diagram top:\n" + firstLine);
     }
 
     /**
      * Verifies if the diagram's body (all the lines except the top and the
      * bottom ones) have been drawn correctly.
      * 
      * @param diagramLines
      *            the array of the diagram lines
      * @throws IncorrectChessDiagramDrawingException
      *            if the body of the diagram is not what was expected
      */
     private void verifyDiagramBody(String[] diagramLines) throws IncorrectChessDiagramDrawingException {
 	for (int rank = 8; rank >= 1; rank--) {
 	    String upperThird = diagramLines[3*(8 - rank) + 1];
 	    String middleThird = diagramLines[3*(8 - rank) + 2];
 	    String lowerThird = diagramLines[3*(8 - rank) + 3];
 	    
 	    if (!upperThird.equals("   |      |      |      |      |      |      |      |      |  "))
 		    throw new IncorrectChessDiagramDrawingException("Incorrect upper third " +
 		    		"in rank " + rank + ":\n" + upperThird);
 	    verifyMiddleThird(rank, middleThird);		    
 	    if (!lowerThird.equals("   |______|______|______|______|______|______|______|______|  "))
 		    throw new IncorrectChessDiagramDrawingException("Incorrect lower third " +
 		    		"in rank " + rank + ":\n" + lowerThird);	    
 	}
     }
 
     /**
      * Verifies the middle third of the three-line String in the diagram,
      * representing one of the ranks 1, 2, ..., 8.
      * 
      * @param rank
      *            integer from 1 to 8, representing a chessboard rank
      *            (horizontal line)
      * @param middleThird
      *            the second (middle) line of the three lines representing a rank
      * @throws IncorrectChessDiagramDrawingException
      *            if the middle third is not what was expected
      */
     private void verifyMiddleThird(int rank, String middleThird) 
 	    throws IncorrectChessDiagramDrawingException {
 	if (!(middleThird.charAt(2) - '0' == rank))
 	    throw new IncorrectChessDiagramDrawingException("Incorrect rank in middle third: was " +
 	    		"expected " + rank + " but obtained" + (middleThird.charAt(2) - '0'));
 	for (int file = 1; file <= 8; file++) {	   
 	    String pieceAbbreviation = getDiagramSquareContent(middleThird, file);
 	    if (!(Piece.allAbbreviationsOfPieces().contains(pieceAbbreviation)
 		    || pieceAbbreviation.equals("  ")))		    
 		throw new IncorrectChessDiagramDrawingException("Incorrect piece " +
 				"abbreviation in file " + file + ", rank " + 
 				rank + ": " + pieceAbbreviation);		    		
 	}
     }
 
     /**
      * Gets the diagram square content (what, if any, piece occupies that
      * square).
      * 
      * @param middleThird
      *            the middle third of the three-liner String, representing one
      *            of the eight ranks of the chessboard
      * @param file
      *            one of the files a to h, expressed as an integer 1 to 8
      * @return the diagram square content - abbreviation of the piece occupying
      *         that square, or, if the square is empty, the String "  " (two
      *         spaces)
      */
     private String getDiagramSquareContent(String middleThird, int file) {
 	String pieceAbbreviation = Character.isDigit(middleThird.charAt(7 * (file - 1) + 8)) 
 		? middleThird.substring(7 * (file - 1) + 6, 7 * (file - 1) + 9) 
 		: middleThird.substring(7 * (file - 1) + 6, 7 * (file - 1) + 8);
 	return pieceAbbreviation;
     }
 
     /**
      * Verifies if the diagram's bottom (the very last line) has been drawn
      * correctly.
      * 
      * @param lastLine
      *            the bottom (26th) line of the diagram
      * @throws IncorrectChessDiagramDrawingException
      *            if the bottom line is not what was expected
      */
     private void verifyDiagramBottom(String lastLine) throws IncorrectChessDiagramDrawingException {
 	if (!lastLine.equals("      a      b      c      d      e      f      g      h      "))
 	    throw new IncorrectChessDiagramDrawingException("Incorrect diagram bottom:\n" + lastLine);
     }
 
     /**
      * Creates the new chess position diagram using the text diagram.
      * 
      * @param diagram
      *            the diagram with the concrete chess position.
      * @return the chess position diagram object, that can be used to
      *         instantiate chess positions
      * @throws IncorrectChessDiagramDrawingException
      *            if the provided diagram is not valid
      */
     public static ChessPositionDiagram createFromTextDiagram(String diagram) 
 	    throws IncorrectChessDiagramDrawingException {
 	return new ChessPositionDiagram(diagram);	
     }
 
     /**
      * Gets the text diagram, used to instantiate this object.
      * 
      * @return the diagram - text "drawing" of the chess position on the chessboard
      */
     public String getDiagram() {
         return diagram;
     }
 
     /**
      * Sets the diagram with the new chess position.
      * 
      * @param diagram
      *            the diagram with the concrete chess position.
      */
     public void setDiagram(String diagram) {
         this.diagram = diagram;
     }
 
     /**
      * Gets the bidirectional map: Piece <-> Square, directly from the diagram.
      * 
      * @return the bidirectional map from the pieces present in this chess
      *         position, to the squares they occupy
      */
     public BiMap<Piece, Square> getPiecesWithSquaresFromDiagram() {
 	BiMap<Piece, Square> piecesWithSquares = EnumBiMap.create(Piece.class, Square.class);
 	String[] diagramLines = diagram.split("\n");
 	for (int rank = 8; rank >= 1; rank--) {
 	    String middleThird = diagramLines[3*(8 - rank) + 2];
 	    for (int file = 1; file <= 8; file++) {	   
 		String pieceAbbreviation = getDiagramSquareContent(middleThird, file);
 		if (!pieceAbbreviation.equals("  ")) {
 		    Piece piece = Piece.getPieceFromAbbreviation(pieceAbbreviation);
 		    Square square = Square.getSquareFromFileAndRank(file, rank);
 		    piecesWithSquares.put(piece, square);
 		}
 	    }		    
 	}
 	return piecesWithSquares;
     }    
 
 }
