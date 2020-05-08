 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package com.bainedog.sudoku;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.HashSet;
 import java.util.Scanner;
 import java.util.Set;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author baine
  */
 public class SolutionTest {
 
     public SolutionTest() {
     }
 
     @BeforeClass
     public static void setUpClass() throws Exception {
     }
 
     @AfterClass
     public static void tearDownClass() throws Exception {
     }
 
     @Before
     public void setUp() {
     }
 
     @After
     public void tearDown() {
     }
 
     @Test
     public void testIterativeSolutionGenerator() throws FileNotFoundException {
         testSolutionGenerator(new IterativeSolutionGenerator());
     }
 
     @Test
     public void testRecursiveSolutionGenerator() throws FileNotFoundException {
         testSolutionGenerator(new RecursiveSolutionGenerator());
     }
 
 
     public void testSolutionGenerator(SolutionGenerator generator) throws FileNotFoundException {
         for (int n : new int[] {1,2,25}) {
             String puzzlePath = "resources/p" + n + ".txt";
             String solutionPath = "resources/s" + n + ".txt";
             testSolution(generator, puzzlePath, solutionPath);
         }
     }
 
     private void testSolution(SolutionGenerator generator, String puzzlePath, String solutionPath) throws FileNotFoundException {
         Sudoku puzzle = readSudoku(puzzlePath);
         Sudoku expectedSolution = readSudoku(solutionPath);
         System.out.println("Puzzle:\n" + Util.toString(puzzle));
 
         Iterable<Sudoku> solutions = generator.solutions(puzzle);
         Sudoku actualSolution = solutions.iterator().next();
         System.out.println("Solution:\n" + Util.toString(actualSolution));
         
         assertEquals(expectedSolution, actualSolution);
         assertFalse(solutions.iterator().hasNext());
     }
 
     private static Sudoku readSudoku(String fileName) throws FileNotFoundException {
         Set<Sudoku.Triple> triples = new HashSet<Sudoku.Triple>();
         Scanner in = new Scanner(new File(fileName));
         int count;
         for (count = 0; in.hasNextLine(); count++) {
             String line = in.nextLine();
             String[] tokens = line.split("\\s+");
             for (int j = 0; j < tokens.length; j++) {
                 String token = tokens[j];
                 if (!token.equals(".")) {
                     int x = token.matches("[A-Z]") ? ALPHABET.indexOf(token) : Integer.parseInt(token) - 1;
                     triples.add(Sudoku.Triple.triple(count, j, x));
                 }
             }
         }
         int order = (int) Math.round(Math.sqrt(count));
         return new Sudoku(order, triples);
     }
 
     private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
 }
