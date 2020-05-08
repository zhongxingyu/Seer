 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package firstfitdecreasing;
 
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author Marcel
  */
 public class BinTest {
 
     public BinTest() {
     }
 
     /**
      * Test of addPiece method, of class Bin.
      */
     @Test
     public void testAddPiece() {
         System.out.println("Bin.addPiece");
         // Arrange
         int piece = 1;
         Bin instance = new Bin(1);
         // Act
         instance.addPiece(piece);
         // Assert        
         assertEquals(piece, (int) instance.getPieces()[0]);
     }
 
     @Test
     public void testAddPiece_above_capacity() {
         System.out.println("Bin.addPiece: Above capacity should throw exception");
         // Arrange
         int piece = 2;
         Bin instance = new Bin(1);
         Exception exception = null;
         // Act
         try {
             instance.addPiece(piece);
         } catch (Exception e) {
             exception = e;
         }
         // Assert        
         assertNotNull(exception);
         assertEquals("Piece is too big for this bin's free capacity.", exception.getMessage());
     }
 
     /**
      * Test of getFreeCapacity method, of class Bin.
      */
     @Test
     public void testGetFreeCapacity_empty_bin() {
         System.out.println("Bin.getFreeCapacity: empty bin");
         // Arrange
         Bin instance = new Bin(1);
         int expResult = 1;
         // Act
         int result = instance.getFreeCapacity();
         // Assert
         assertEquals(expResult, result);
     }
     
         /**
      * Test of getFreeCapacity method, of class Bin.
      */
     @Test
     public void testGetFreeCapacity_two_items_added() {
         System.out.println("Bin.getFreeCapacity: empty bin");
         // Arrange
         Bin instance = new Bin(4);
         int expResult = 2;
         instance.addPiece(1);
         instance.addPiece(1);
         // Act
         int result = instance.getFreeCapacity();
         // Assert
         assertEquals(expResult, result);
     }
 
     /**
      * Test of getPieces method, of class Bin.
      */
     @Test
     public void testGetPieces() {
         System.out.println("getPieces");
         // Arrange
         Bin instance = new Bin(2);
         Integer[] expResult = new Integer[]{1, 1};
         instance.addPiece(1);
         instance.addPiece(1);
         // Act
         Integer[] result = instance.getPieces();
         // Assert
         assertArrayEquals(expResult, result);
     }
 
     /**
      * Test of toString method, of class Bin.
      */
     @Test
     public void testToString() {
         System.out.println("toString");
         // Arrange
         Bin instance = new Bin(2);
         instance.addPiece(1);
         instance.addPiece(1);
        String expResult = "[ 1, 1 ]";
         // Act
         String result = instance.toString();
         // Assert
         assertEquals(expResult, result);        
     }
 }
