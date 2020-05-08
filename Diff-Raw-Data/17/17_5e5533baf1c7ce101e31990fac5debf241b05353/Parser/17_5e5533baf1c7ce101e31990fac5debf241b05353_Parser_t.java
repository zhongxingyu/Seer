 package parsing;
 
 import hiper.Piece;
 import hiper.Point;
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * This class is in charge of parsing a problem instance definition.
  *
  * @author Priscila Angulo, updated by Marcel Valdez
  */
 public class Parser {
 
   public Parser() {
   }
 
   /**
    * Parses a file containing a problem instance specification.
    *
    * @param filepath to parse as a problem instance specification.
    * @return the ProblemInstanceSpecSpecification
    * @throws IOException
    */
   public ProblemInstanceSpec parseFile(String filepath) throws IOException {         
     ProblemInstanceSpec problemInstance = null;
 
     try (BufferedReader bufReader = getFileReader(filepath)) {
       //Skip the first line
       String ignoredText = bufReader.readLine();
 
       //Get container dimmensions
      String containerDimText = bufReader.readLine();      
       int containerWidth = parseContainerWidth(containerDimText);
       int containerHeight = parseContainerHeight(containerDimText);
 
       //Get pieces
       List<Piece> inputPieces = new ArrayList<Piece>();
       String pieceText = bufReader.readLine();
       while (pieceText != null) {
         if (!pieceText.isEmpty()) {
           inputPieces.add(new Piece(parsePieceVertices(pieceText)));
         }
 
         pieceText = bufReader.readLine();
       }
 
       problemInstance = new ProblemInstanceSpec(
               containerWidth,
               containerHeight,
               inputPieces.toArray(new Piece[]{}));
 
     } catch (IOException ex) {
       Logger.getLogger(Parser.class.getName()).log(Level.SEVERE, null, ex);
       /// Fail fast
       throw ex;
     }
 
     return problemInstance;
   }
 
   /**
    * Parses a line of text corresponding to a container specification, getting
    * its width. Expected text: '[width] [height]'
    *
    * @param line of text corresponding to a container specification
    * @return the container's width
    * @throws NumberFormatException
    */
   int parseContainerWidth(String line) throws NumberFormatException {
     String trimmed = line.trim();
     return Integer.parseInt(trimmed.split(" ")[0]);
   }
 
   /**
    * Parses a line of text corresponding to a container specification, getting
    * its height. Expected text: '[width] [height]'
    *
    * @param line of text corresponding to a container specification
    * @return the container's height
    * @throws NumberFormatException
    */
   int parseContainerHeight(String line) throws NumberFormatException {
     String trimmed = line.trim();
     return Integer.parseInt(trimmed.split(" ")[1]);
   }
 
   /**
    * Parses a line corresponding to a Piece's vertices specification. Expected
    * text: '[number of sides] [x1] [y1] [x2] [y2] .. [xN] [yN]
    *
    * @param line corresponding to a Piece's vertices specification.
    * @return the Points of a Piece's vertices.
    * @throws NumberFormatException
    */
   Point[] parsePieceVertices(String line) throws NumberFormatException {
     List<Point> vertices = new ArrayList<Point>();
     String[] tokens = line.trim().split(" ");
     for (int i = 1; i < tokens.length; i += 2) {
       int x = Integer.parseInt(tokens[i]);
       int y = Integer.parseInt(tokens[i + 1]);
       vertices.add(Point.At(x, y));
     }
 
     return vertices.toArray(new Point[]{});
   }
 
   /**
    * Gets a buffered reader for reading a file's content.
    *
    * @param filePath of the file to read
    * @return a buffered reader for the file.
    * @throws FileNotFoundException
    */
   protected BufferedReader getFileReader(String filePath)
           throws FileNotFoundException {
     BufferedReader bufReader;
     FileReader fileReader;
     try {
       fileReader = new FileReader(filePath);
       bufReader = new BufferedReader(fileReader);
     } catch (FileNotFoundException ex) {
       Logger.getLogger(Parser.class.getName()).log(Level.SEVERE, null, ex);
       /// Fail fast
       throw ex;
     }
 
     return bufReader;
   }
 }
