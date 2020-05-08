 /* Jedd - A language for implementing relations using BDDs
  * Copyright (C) 2003 Ondrej Lhotak
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the
  * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
  * Boston, MA 02111-1307, USA.
  */
 
 package jedd;
 import java.util.*;
 
 /**
  * Main entry point for Jedd.
  */
 public class Main {
     public static final void main( String[] args ) { 
         LinkedList largs = new LinkedList( Arrays.asList(args) );
         for( Iterator argIt = largs.iterator(); argIt.hasNext(); ) {
             final String arg = (String) argIt.next();
             if( arg.equals( "-s" ) ) {
                 argIt.remove();
                 polyglot.ext.jedd.types.PhysDom.v().setSatSolver( (String) argIt.next() );
                 argIt.remove();
             }
             if( arg.equals( "-sc" ) ) {
                 argIt.remove();
                 polyglot.ext.jedd.types.PhysDom.v().setSatCore( (String) argIt.next() );
                 argIt.remove();
             }
             if( arg.equals( "-h" ) || arg.equals( "-?" ) || arg.equals( "-help" )
                     || arg.equals( "--help" ) ) {
                 usage();
             }
         }
         if( largs.size() == 0 ) usage();
         largs.addFirst( "jedd" );
         largs.addFirst( "-ext" );
         largs.addFirst( "-noserial" );
        polyglot.main.Main.main( (String[]) largs.toArray( args ) );
     }
 
     public static void usage() {
         System.err.println( "Jedd - A language for implementing relations using BDDs" );
         System.err.println( "Copyright (C) 2003 Ondrej Lhotak" );
         System.err.println();
         System.err.println( "Jedd comes with ABSOLUTELY NO WARRANTY.  Jedd is free software,");
         System.err.println( "and you are welcome to redistribute it under certain conditions.");
         System.err.println( "See the accompanying file 'COPYING-LESSER.txt' for details.");
         System.err.println( "Usage: java jedd.Main [options] source-file.jedd ..." );
         System.err.println();
         System.err.println( "where [options] may be Jedd options or Polyglot options." );
         System.err.println();
         System.err.println( "Jedd options are listed below:" );
         System.err.println( " -s <sat-solver-cmd>  sets the external SAT solver to be used" );
         System.err.println( " -sc <sat-core-cmd>  sets the external program for extracting UNSAT cores" );
         System.err.println();
         System.err.println( "Polyglot options are listed in the Polyglot help screen below:" );
         System.err.println();
     }
 }
 
