 /**
  * Minimp -- An implementation of an imperative subset of Python
  *
  *
  * Copyright (C) 2013, Joseph Heron, Jonathan Gillett, and Daniel Smullen
  * All rights reserved.
  *
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 /**
  * Minimc -- A parser for Minimp, an imperative subset of Python 
  */
 public class mimpc
 {
     /* Exit return codes */
     private static final int VALID_SOURCE_FILE   =  0;
     private static final int INVALID_SOURCE_FILE = -1;
     private static final int FILE_NOT_FOUND      = -2;
     private static final int INVALID_ARGUMENTS   = -3;
 
 
    public static int main(String args[])
     {
        Minimp parser;
 
         /* Attempt to read the Minimp source file provided */
         if (args.length == 1)
         {
             System.out.println("Mimpc -- A Minimp Parser:  Reading from source file " + args[0] + " . . .");
             
             try
             {
                 parser = new Minimp(new java.io.FileInputStream(args[0]));
             }
             catch (java.io.FileNotFoundException e)
             {
                 System.out.println("Mimpc -- A Minimp Parser:  File " + args[0] + " not found.");
                return FILE_NOT_FOUND;
             }
         }
         /* Minimp source file not provided, print program usage */
         else
         {
             System.out.println("Mimpc -- A Minimp Parser:  Source file not provided!");
             System.out.println("USAGE:  java mimpc source_file");
            return INVALID_ARGUMENTS;
         }
 
         /* Attempt to parse the Minimp source file given and generate the AST */
         try
         {
             SimpleNode tree = parser.Program();
             
             //tree.dump("");
             //parser.jjtree.rootNode().interpret();
             
             System.out.println("Minimp source file successfully parsed!");
            return VALID_SOURCE_FILE;
         } 
         catch (ParseException pe)
         {
             System.out.println("Mimpc -- A Minimp Parser:  Encountered errors during parse.");
             pe.printStackTrace();
            return INVALID_SOURCE_FILE;
         }
         catch (Exception e)
         {
             System.out.println("Mimpc -- A Minimp Parser:  Encountered errors during interpretation/tree building.");
             e.printStackTrace();
            return INVALID_SOURCE_FILE;
         }
     }
 }
