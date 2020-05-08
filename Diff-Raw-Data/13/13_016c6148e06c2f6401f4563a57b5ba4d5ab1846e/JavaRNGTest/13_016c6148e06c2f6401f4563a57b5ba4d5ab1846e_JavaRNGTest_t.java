 package com.github.fhd.gwtetris.client;
 
 import static org.junit.Assert.*;
 
 import org.junit.*;
 
 import com.github.fhd.gwtetris.client.gamelogic.*;
 
 public class JavaRNGTest {
     private RNG rng = new JavaRNG();
     
     @Test
     public void randomPiecePosition() {
        for (int i = 0; i < 40; i++) {
             int r = rng.randomPiecePosition(i);
             assertTrue("<" + r + "> was not within the expected range",
                        r >= 0 && r <= i);
         }
     }
     
     @Test
     public void randomPieceType() {
        for (int i = 0; i < 14; i++) {
            Piece.Type r = rng.randomPieceType();
            int rTypeNo = r.getTypeNo();
            assertTrue("<" + rTypeNo + "> was not within the expected range",
                       rTypeNo >= 1 && rTypeNo <= Piece.Type.values().length);
        }
     }
 }
