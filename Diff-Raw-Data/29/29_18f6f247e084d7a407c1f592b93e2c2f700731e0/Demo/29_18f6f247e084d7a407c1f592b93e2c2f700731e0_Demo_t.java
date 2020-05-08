 /*
  * Main.java
  * StockPlay - Main klasse voor filter demo.
  *
  * Copyright (c) 2010 StockPlay development team
  * All rights reserved.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 package com.kapti.filter;
 
 import com.kapti.filter.exception.FilterException;
 import com.kapti.filter.exception.ParserException;
 import com.kapti.filter.parsing.Parser;
 
 
 /**
  * Demonstratieklasse voor filters. Deze klasse toont de mogelijkheden van
  * filters, en hoe ze gebruikt zouden moeten worden.
  *
  * @author tim
  */
 public class Demo {
     /** Main entry point. Hierbij wordt een voorbeeld-filter opgesteld,
      * geparset, gecompileerd, en een debug-view uitgeschreven.
      *
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         //
         // Test
         //
 
         // Test parser
         Parser tParser = Parser.getInstance();
 
         // Parse input
         Filter tFilter = null;
         try {
            tFilter = tParser.parse("");
         }
         catch (ParserException e) {
             System.err.println("Parsing failed");
             e.printStackTrace();
         }
 
         // Print a filter tree
         try {
             tFilter.debug("/tmp/AST");
         }
         catch (FilterException e) {
             System.out.println("Debug dump failed");
             e.printStackTrace();
         }
 
         // Compile output
         try {
             System.out.println("Compiled result: " + (String)tFilter.compile());
         }
         catch (FilterException e) {
             System.err.println("Compilation failed");
             e.printStackTrace();
         }
 
 
         //
         // Benchmark
         //
 
         System.out.println("-- BENCHMARKING --");
         long tStart, tEnd, tDiff;
 
         System.out.print("* Parser instantiations: ");
         tStart = System.currentTimeMillis();
         for (int i = 0; i < 1000; i++) {
             Parser tTest = Parser.getInstance();
         }
         tEnd = System.currentTimeMillis();
         System.out.println((tEnd - tStart) + " ms");
 
         System.out.print("* Easy equation parsing: ");
         tStart = System.currentTimeMillis();
         for (int i = 0; i < 1000; i++) {
             try {
                 tParser.parse("id EQUALS 42");
             } catch (Exception e) {
                 throw new RuntimeException(e);
             }
         }
         tEnd = System.currentTimeMillis();
         System.out.println((tEnd - tStart) + " ms");
 
         System.out.print("* Complex equation parsing: ");
         tStart = System.currentTimeMillis();
         for (int i = 0; i < 1000; i++) {
             try {
                 tParser.parse("(id EQUALS 42 AND name EQUALS 'StockPlay') OR (gender EQUALS 'M' OR name EQUALS 'Test')");
             } catch (Exception e) {
                 throw new RuntimeException(e);
             }
         }
         tEnd = System.currentTimeMillis();
         System.out.println((tEnd - tStart) + " ms");
     }
 
 }
