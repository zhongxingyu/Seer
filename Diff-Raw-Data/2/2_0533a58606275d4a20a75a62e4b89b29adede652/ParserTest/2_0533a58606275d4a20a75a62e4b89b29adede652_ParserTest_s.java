 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package parsing;
 
 import hiperheuristica.Point;
 import parsing.Parser;
 import parsing.ProblemInstanceSpec;
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author Priscila Angulo
  */
 public class ParserTest {
 
   public ParserTest() {
   }
 
   @Test
  public void testProcessFile() {
     System.out.println("parser.processFile");
     // Arrange
     // System.out.println((new java.io.File( "." )).getCanonicalPath());
     String file = ".\\input_data\\PF01.txt";
     ProblemInstanceSpec problemInstance = null;
     Parser parser = new Parser();
 
     // Act
     try {
       problemInstance = parser.parseFile(file);
       // Assert        
       assertNotNull(problemInstance);
       assertEquals(100, problemInstance.getContainerHeight());
       assertEquals(100, problemInstance.getContainerWidth());
       assertEquals(8, problemInstance.getInputPieces().size());
     } catch (IOException ex) {
       Logger.getLogger(ParserTest.class.getName()).log(Level.SEVERE, null, ex);
       /// Fail fast.
       throw ex;
     }
   }
 
   @Test
   public void testGetContainerHeight() {
     System.out.println("parser.testGetContainerHeight");
     // Arrange
     String line = "10 100";
     Parser parser = new Parser();
 
     // Act
     int height = parser.parseContainerHeight(line);
 
     // Assert        
     assertEquals(100, height);
   }
 
   @Test
   public void testGetContainerWidth() {
     System.out.println("parser.testGetContainerWidth");
     // Arrange
     String line = "10 100";
     Parser parser = new Parser();
 
     // Act
     int width = parser.parseContainerWidth(line);
 
     // Assert        
     assertEquals(10, width);
   }
 
   @Test
   public void testGetPieceVertices() {
     System.out.println("parser.testGetPieceVertices");
     // Arrange
     String line = " 4 0 0 54 0 54 100 0 100";
     Point[] result;
     Parser parser = new Parser();
 
     // Act
     result = parser.parsePieceVertices(line);
 
     // Assert        
     assertNotNull(result);
     assertEquals(4, result.length);
     assertEquals(0, result[0].getX());
     assertEquals(0, result[0].getY());
     assertEquals(54, result[1].getX());
     assertEquals(0, result[1].getY());
     assertEquals(54, result[2].getX());
     assertEquals(100, result[2].getY());
     assertEquals(0, result[3].getX());
     assertEquals(100, result[3].getY());
   }
 }
