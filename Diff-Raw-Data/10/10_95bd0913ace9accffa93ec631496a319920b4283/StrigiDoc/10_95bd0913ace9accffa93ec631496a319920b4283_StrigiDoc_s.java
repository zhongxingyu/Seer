 /**
  *  StrigiDoc.java
  *
  *  This file is a part of StrigiDoc.
  *
  *  Copyright (C) 2013 Iain R. Learmonth and contributors
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 
 package uk.ac.abdn.erg.iain.strigidoc;
 
 import java.io.IOException;
 
 import org.semanticweb.owlapi.model.OWLOntologyCreationException;
 
 /**
  * The main application class
  */
 public class StrigiDoc {
 
 	/**
 	 * StrigiDoc application that takes an OWL ontology URI on the command line
 	 * and prints LaTeX output on standard output.
 	 *
 	 * @param args the command line arguments
 	 */
 	public static void main(String[] args) {
 
 		if ( args.length < 1 ) {
 			printUsage();
 			return;
 		}
 
 		try {
 			Ontology data = new Ontology(args[0]);
 			OntologyPrinter.print(data);
 		} catch (OWLOntologyCreationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Prints usage instructions to the terminal
 	 */
 	private static void printUsage() {
		System.out.println("StrigiDoc Copyright (C) 2013 Iain R. Learmonth and contributors");
		System.out.println("");
		System.out.println("Usage:");
		System.out.println("    java -jar strigidoc-*-standalone.jar URI > output.tex");
		System.out.println("");
 	}
 
 }
 
