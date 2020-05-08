 // AnnotationReaderTest.java
 
 /** Copyright (c) 2002 Institute for Systems Biology and the Whitehead Institute
  **
  ** This library is free software; you can redistribute it and/or modify it
  ** under the terms of the GNU Lesser General Public License as published
  ** by the Free Software Foundation; either version 2.1 of the License, or
  ** any later version.
  ** 
  ** This library is distributed in the hope that it will be useful, but
  ** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
  ** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
  ** documentation provided hereunder is on an "as is" basis, and the
  ** Institute of Systems Biology and the Whitehead Institute 
  ** have no obligations to provide maintenance, support,
  ** updates, enhancements or modifications.  In no event shall the
  ** Institute of Systems Biology and the Whitehead Institute 
  ** be liable to any party for direct, indirect, special,
  ** incidental or consequential damages, including lost profits, arising
  ** out of the use of this software and its documentation, even if the
  ** Institute of Systems Biology and the Whitehead Institute 
  ** have been advised of the possibility of such damage.  See
  ** the GNU Lesser General Public License for more details.
  ** 
  ** You should have received a copy of the GNU Lesser General Public License
  ** along with this library; if not, write to the Free Software Foundation,
  ** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
  **/
 
 //------------------------------------------------------------------------------
 // $Revision$
 // $Date$
 // $Author$
 //--------------------------------------------------------------------------------------
package cytoscape.data.annotation.readers.unitTests;
 //--------------------------------------------------------------------------------------
 import junit.framework.*;
 import java.io.*;
 import java.util.*;
 
 import cytoscape.data.readers.*;
 //------------------------------------------------------------------------------
 /**
  * test the AnnotationReader class
  */
 public class AnnotationReaderTest extends TestCase {
 
 
 //------------------------------------------------------------------------------
 public AnnotationReaderTest (String name) 
 {
   super (name);
 }
 //------------------------------------------------------------------------------
 public void setUp () throws Exception
 {
 }
 //------------------------------------------------------------------------------
 public void tearDown () throws Exception
 {
 }
 //------------------------------------------------------------------------------
 /**
  * make sure that the ctor properly initializes all relevant data structures
  * as seen through the standard getter methods
  */
 public void testReadKeggAnnotation () throws Exception
 { 
   System.out.println ("testReadKeggAnnotation");
 
   String filename = "sampleData/keggSample.annotation";
   AnnotationReader reader = new AnnotationReader (filename);
   HashMap annotations = reader.getHashMap ();
   assertTrue (annotations.size () == 5);
   String [] names = reader.getNames ();
   assertTrue (names.length == 5);
 
   String [] expectedNames =  {"VNG0006G", "VNG0008G", "VNG0009G", "VNG0046G", "VNG0047G"};
   int    [] expectedCounts = {2, 2, 2, 3, 1};
  
   for (int i=0; i < 5; i++) {
     int [] ids = reader.getAnnotationIDs (expectedNames [i]);
     assertTrue (ids.length == expectedCounts [i]);
     }
   
 } // testReadKeggAnnotation
 //-------------------------------------------------------------------------
 public static void main (String [] args) 
 {
   junit.textui.TestRunner.run (new TestSuite (AnnotationReaderTest.class));
 
 } // main
 //------------------------------------------------------------------------------
 } // AnnotationReaderTest
 
 
