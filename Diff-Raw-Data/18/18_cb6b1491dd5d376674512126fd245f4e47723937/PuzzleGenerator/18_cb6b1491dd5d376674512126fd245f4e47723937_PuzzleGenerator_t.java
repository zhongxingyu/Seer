 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package com.bainedog.sudoku.puzzle;
 
 import com.bainedog.sudoku.SolutionGenerator;
 import com.bainedog.sudoku.Sudoku;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 /**
  *
  * @author andrew
  */
 public class PuzzleGenerator {
 
     private SolutionGenerator solutionGenerator;
 
     public PuzzleGenerator(SolutionGenerator solutionGenerator) {
         this.solutionGenerator = solutionGenerator;
     }
 
     public Sudoku generatePuzzle(int order) {
 
         Sudoku puzzle = this.solutionGenerator.solutions(new Sudoku(order))
                 .iterator().next();
         this.solutionGenerator.halt();
 
         ArrayList<Sudoku.Triple> triples = new ArrayList<Sudoku.Triple>(puzzle.triples);
         Collections.shuffle(triples);
 
         while (!isMinimal(puzzle, triples)) {
             Sudoku.Triple t = triples.remove(0);
             puzzle.triples.remove(t);
             if (!hasOneAndOnlyOneSolution(puzzle)) {
                 puzzle.triples.add(t);
             }
         }
 
         return puzzle;
     }
 
     private boolean isMinimal(Sudoku puzzle, List<Sudoku.Triple> triples) {
         for (Sudoku.Triple t : triples) {
             puzzle.triples.remove(t);
             if (hasOneAndOnlyOneSolution(puzzle)) {
                 return false;
             }
             puzzle.triples.add(t);
         }
         return true;
     }
 
     private  boolean hasOneAndOnlyOneSolution(Sudoku puzzle) {
         Iterator<Sudoku> solutions = this.solutionGenerator.solutions(puzzle).iterator();
         if (solutions.hasNext()) {
             solutions.next();
            boolean result = !solutions.hasNext();
             this.solutionGenerator.halt();
            return result;
         } else {
             this.solutionGenerator.halt();
             return false;
         }
     }
 
 }
