 /*
  * Filter.java
  * StockPlay - Filter object.
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
 
 import com.kapti.filter.graph.Graph;
 import com.kapti.filter.condition.Condition;
 import com.kapti.filter.exception.FilterException;
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintStream;
 import org.apache.log4j.Logger;
 
 /**
  * Dit is de container voor een filterobject. Dit object bevat eigenlijk
  * gewoon een root-conditie (de top van de filterboom), en voegt daar
  * wat functionaliteit aan toe, zoals het initiëren van een compilatie,
  * of het uitschrijven van een debug-dump.
  *
  * @author tim
  */
 public class Filter {
     //
     // Member data
     //
 
     static Logger mLogger = Logger.getLogger(Filter.class);
 
     private Condition mRoot = null;
 
 
     //
     // Methods
     //
 
     /**
      * Stel de root conditie in. Dit is de top van de filter tree, en zal dus
      * het startpunt zijn voor verschillende acties (compilatie, debug).
      *
      * @param iRoot
      */
     public final void setRoot(Condition iRoot) {
         mRoot = iRoot;
     }
 
     /**
      * Compileer de filter naar een bruikbaar formaat.
      *
      * @return
      * @throws FilterException
      */
     public Object compile() throws FilterException {
        if (empty())
            return null;
        
         Object oResult = mRoot.compile();
         mLogger.debug("compiled filter to '" + oResult + "'");
         return oResult;
     }
 
     /**
      * Schrijf een debug-graph van de filterboom naar een (png) bestand.
      * 
      * @param iFilename
      * @throws FilterException
      */
     public void debug(String iFilename) throws FilterException {
        if (empty())
            return;

         // Create graph
         Graph graph = new Graph("digraph");
         mRoot.addNode(graph);
 
         // Render panel to file
         File tFile = null;
         try {
             tFile = new File(iFilename + ".dot");
             if (!tFile.exists())
                 tFile.createNewFile();
             graph.render(new PrintStream(tFile));
         }
         catch (IOException e) {
             throw new FilterException("Problem creating output file", e.getCause());
         }
 
         if (true) return;
 
         // Render the DOT file to a PNG
         String[] cmd = { "/usr/bin/dot", "-Tpng", iFilename+".dot", "-o", iFilename+".png" };
         try {
             Process tConverter = Runtime.getRuntime().exec(cmd);
 
             tConverter.waitFor();
             if (tConverter.exitValue() != 0)
                 throw new FilterException("Conversion failed");
         }
         catch (IOException e) {
             throw new FilterException("Problem executing child process to convert DOT code", e.getCause());
         }
         catch (InterruptedException e) {
             throw new FilterException("Was not allowed to wait for DOT output due to interrupt", e.getCause());
         }
     }
 
     /**
      * Controleer of de boom gedefiniëerd is.
      * 
      * @return
      */
     public boolean empty() {
         return mRoot == null;
     }
 }
