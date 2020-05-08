 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package com.bainedog.sudoku.puzzle;
 
 import com.bainedog.sudoku.IterativeSolutionGenerator;
 import com.bainedog.sudoku.SolutionGenerator;
 import com.bainedog.sudoku.Sudoku;
 import com.bainedog.sudoku.Util;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author andrew
  */
 public class PuzzleGeneratorTest {
 
     /**
      * Test of generatePuzzle method, of class PuzzleGenerator.
      */
     @Test
     public void testGeneratePuzzle() {
 
         System.out.println("generatePuzzle");
         int order = 3;
         Sudoku puzzle = new PuzzleGenerator(new IterativeSolutionGenerator()).generatePuzzle(order);
 
         System.out.println(Util.toString(puzzle));
         System.out.format("Clues: %s\n", puzzle.triples.size());
         /* Puzzle should have one and only one solution */
         assertTrue(hasOneAndOnlyOneSolution(puzzle));
 
         /* Puzzle should be minimal */
         assertTrue(isPuzzleMinimal(puzzle));
     }
 
     public static boolean isPuzzleMinimal(Sudoku puzzle) {
         List<Sudoku.Triple> triples = new ArrayList<Sudoku.Triple>(puzzle.triples);
         for (Sudoku.Triple t : triples) {
             puzzle.triples.remove(t);
             if (hasOneAndOnlyOneSolution(puzzle)) {
                 System.out.format("(%s, %s, %s)", t.row, t.column, t.number);
                 return false;
             }
             puzzle.triples.add(t);
         }
         return true;
     }
 
     public static boolean hasOneAndOnlyOneSolution(Sudoku sudoku) {
         SolutionGenerator generator = new IterativeSolutionGenerator();
         Iterable<Sudoku> solutions = generator.solutions(sudoku);
         Iterator<Sudoku> iterator = solutions.iterator();
         
         if (!iterator.hasNext()) {
             return false;
         }
         
         Sudoku solution = iterator.next();
         if (iterator.hasNext()) {
             return false;
         }
        
         return true;
     }
 
 }
