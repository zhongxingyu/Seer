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
  ** Institute for Systems Biology and the Whitehead Institute
  ** have no obligations to provide maintenance, support,
  ** updates, enhancements or modifications.  In no event shall the
  ** Institute for Systems Biology and the Whitehead Institute
  ** be liable to any party for direct, indirect, special,
  ** incidental or consequential damages, including lost profits, arising
  ** out of the use of this software and its documentation, even if the
  ** Institute for Systems Biology and the Whitehead Institute
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
 
 /**
  * Junit test for the class which sets run-time configuration.
  */
 package cytoscape.unitTests;
 
 import cytoscape.CytoscapeConfig;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 
 import java.io.File;
 import java.util.Properties;
 
 public class CytoscapeConfigTest extends TestCase {
 
      // local build.xml may reassign the directory so that this program
      // can find the -current- directory when this is run from the current
      // directory
 
   private static String testDataDirectory = ""; 
     /**
      * Constructor.
      * @param name Name of Test.
      */
     public CytoscapeConfigTest(String name) {
         super(name);
     }
 
     /**
      * Tests all Command Line Arguments.
      * @throws Exception All Exceptions.
      */
     public void testAllArgs() throws Exception {
         AllTests.standardOut("testAllArgs");
 
         String bioDataDirectory = "../data/GO";
         String geometryFilename = "../data/galFiltered.gml";
         String interactionsFilename = "../data/tideker0/yeastSmall.intr";
         String expressionFilename = "../data/tideker0/gal1-20.mrna";
         String nodeAttributeFile1 = "xxx.fooB";
         String nodeAttributeFile2 = "xxx.barA";
         String nodeAttributeFile3 = "xxx.zooC";
 
         String edgeAttributeFile1 = "xxx.edgeA";
         String edgeAttributeFile2 = "xxx.edgeB";
 
         String defaultSpeciesName = "Halobacterium sp.";
         String defaultLayoutStrategy = "hierarchical";
 
         String[] args = {"-b", bioDataDirectory,
                          "-g", geometryFilename,
                          "-i", interactionsFilename,
                          "-e", expressionFilename,
                          "-n", nodeAttributeFile1,
                          "-n", nodeAttributeFile2,
                          "-n", nodeAttributeFile3,
                          "-j", edgeAttributeFile1,
                          "-j", edgeAttributeFile2,
                          "-s", defaultSpeciesName,
                          "-l", defaultLayoutStrategy,
                          "-h",
                          "-v",
                          "-x",
         };
 
         CytoscapeConfig config = new CytoscapeConfig(args);
 
         assertTrue(config.getBioDataDirectory().equals(bioDataDirectory));
         assertTrue(config.getGeometryFilename().equals(geometryFilename));
         assertTrue(config.getInteractionsFilename().equals(interactionsFilename));
         assertTrue(config.getExpressionFilename().equals(expressionFilename));
        assertTrue(config.getWhetherToCopyExpToAttribs() == true);
 
         assertTrue(config.getNumberOfNodeAttributeFiles() == 3);
         String[] nafs = config.getNodeAttributeFilenames();
         assertTrue(nafs.length == 3);
 
         for (int i = 0; i < nafs.length; i++) {
             String af = nafs[i];
             assertTrue(af.equals(nodeAttributeFile1) ||
                     af.equals(nodeAttributeFile2) ||
                     af.equals(nodeAttributeFile3));
         }
 
         assertTrue(config.getNumberOfEdgeAttributeFiles() == 2);
         String[] eafs = config.getEdgeAttributeFilenames();
         assertTrue(eafs.length == 2);
 
         for (int i = 0; i < eafs.length; i++) {
             String af = eafs[i];
             assertTrue(af.equals(edgeAttributeFile1) ||
                     af.equals(edgeAttributeFile2));
         }
 
         AllTests.standardOut ("--------------------- config: \n" + config.toString ());
         assertTrue(config.helpRequested());
         assertTrue(config.displayVersion());
 
         String[] extensions = config.getAllDataFileExtensions();
         assertTrue(config.getAllDataFileNames().length == 8);
 
         assertTrue(config.getAllDataFileExtensions().length == 8);
 
         // choose two file extensions to look for
         boolean foundFooB = false;
         boolean foundIntr = false;
 
         for (int i = 0; i < extensions.length; i++) {
             if (extensions[i].equals("fooB")) foundFooB = true;
             if (extensions[i].equals("intr")) foundIntr = true;
         }
 
         assertTrue(config.getDefaultSpeciesName().equals(defaultSpeciesName));
         assertTrue(config.getDefaultLayoutStrategy().equals(defaultLayoutStrategy));
 
         assertTrue(foundFooB);
         assertTrue(foundIntr);
 
     } // testAllArgs
 
     public void testReadProjectFile_1() throws Exception {
         AllTests.standardOut ("testReadProjectFile_1");
 
         String projectFilename = testDataDirectory + "src/cytoscape/unitTests/test1.cpr";
         // get the name of the absolute path to the current directory, to allow
         // us to check the files named in the project file, which are always
         // made into absolute paths
 
         String dir = new File(projectFilename).getAbsoluteFile().getParentFile().getPath();
 
         String[] args = {"-p", projectFilename};
 
         CytoscapeConfig config = new CytoscapeConfig(args);
 
         assertTrue(config.getProjectFilename().equals(projectFilename));
         assertTrue(config.getGeometryFilename().equals(dir + "/galFiltered.gml"));
         assertTrue(config.getInteractionsFilename().equals(dir + "/galFiltered.sif"));
 
         //assertTrue (config.getExpressionFilename().equals (expressionFilename));
 
         assertTrue(config.getNumberOfNodeAttributeFiles() == 2);
 
         String[] nafs = config.getNodeAttributeFilenames();
         assertTrue(nafs.length == 2);
 
         for (int i = 0; i < nafs.length; i++) {
             String af = nafs[i];
             assertTrue(af.equals(dir + "/nodeAttributes1.noa") ||
                     af.equals(dir + "/nodeAttributes2.noa"));
         } // for i
 
         assertTrue(config.getNumberOfEdgeAttributeFiles() == 2);
 
         assertTrue(config.getDefaultSpeciesName().equals("Saccharomyces cerevisiae"));
         assertTrue(config.getDefaultLayoutStrategy().equals("hierarchical"));
         // System.out.println (config.getBioDataDirectory());
         assertTrue(config.getBioDataDirectory().equals("rmi://hazel/yeast"));
 
 
     } // testReadProjectFile_1
 
     public void testReadProjectFile_2() throws Exception {
         AllTests.standardOut ("testReadProjectFile_2");
 
         String projectFilename = testDataDirectory + "src/cytoscape/unitTests/test2.cpr";
         // get the name of the absolute path to the current directory, to allow
         // us to check the files named in the project file, which are always
         // made into absolute paths
 
         String dir = new File(projectFilename).getAbsoluteFile().getParentFile().getPath();
 
         String[] args = {"-p", projectFilename};
 
         CytoscapeConfig config = new CytoscapeConfig(args);
 
         assertTrue(config.getProjectFilename().equals(projectFilename));
         assertTrue(config.getGeometryFilename().equals(dir + "/galFiltered.gml"));
         assertTrue(config.getInteractionsFilename() == null);
 
         //assertTrue (config.getExpressionFilename().equals (expressionFilename));
 
         assertTrue(config.getNumberOfNodeAttributeFiles() == 2);
         assertTrue(config.getNumberOfEdgeAttributeFiles() == 2);
         String[] nafs = config.getNodeAttributeFilenames();
         assertTrue(nafs.length == 2);
 
         for (int i = 0; i < nafs.length; i++) {
             String af = nafs[i];
             assertTrue(af.equals(dir + "/nodeAttributes1.noa") ||
                     af.equals(dir + "/nodeAttributes2.noa"));
         } // for i
 
         assertTrue(config.getNumberOfEdgeAttributeFiles() == 2);
         String[] eafs = config.getEdgeAttributeFilenames();
         assertTrue(eafs.length == 2);
 
         for (int i = 0; i < eafs.length; i++) {
             String af = eafs[i];
             assertTrue(af.equals(dir + "/edgeAttributes1.eda") ||
                     af.equals(dir + "/edgeAttributes2.eda"));
         } // for i
 
         assertTrue(config.getDefaultSpeciesName().equals("Saccharomyces cerevisiae"));
         // layout is set to 'embedded' in test2.cpr, but cytoscape.props says hierarchical
         assertTrue(config.getDefaultLayoutStrategy().equals("hierarchical"));
         assertTrue(config.getBioDataDirectory().equals(dir + "/annotationsAndSynonyms"));
 
     } // testReadProjectFile_2
 
     public void testForExpectedNullValues() throws Exception {
         AllTests.standardOut ("testForExpectedNullValues");
 
         String[] args = {"-h"};
 
         CytoscapeConfig config = new CytoscapeConfig(args);
         assertTrue(config.getBioDataDirectory() == null);
         assertTrue(config.getGeometryFilename() == null);
         assertTrue(config.getInteractionsFilename() == null);
         assertTrue(config.getExpressionFilename() == null);
         assertTrue(config.getAllDataFileNames().length == 0);
         assertTrue(config.getDefaultSpeciesName() == null);
         assertTrue(config.helpRequested());
         assertTrue(config.getWhetherToCopyExpToAttribs());
         assertTrue(!config.inputsError());
 
 
     } // testForExpectedNullValues
 
     /**
      *  ensure that multiple sources of the input graph (e.g., a gml file, and
      *  an interactions file) are detected and reported as an error
      */
     public void testLegalArgs0() throws Exception {
         AllTests.standardOut ("testLegalArgs0");
 
         String geometryFilename = "../data/galFiltered.gml";
         String interactionsFilename = "../data/tideker0/yeastSmall.intr";
 
         String[] args = {"-g", geometryFilename,
                          "-i", interactionsFilename};
 
         CytoscapeConfig config = new CytoscapeConfig(args);
 
         assertTrue(config.inputsError());
 
     } // testLegalArgs0
 
     /**
      * make sure that system properties are read, and that user props can
      * override and extend them
      */
     public void testReadProperties() throws Exception {
         AllTests.standardOut ("testReadProperties");
         //  Set CYTOSCAPE HOME
         System.setProperty ("CYTOSCAPE_HOME", "src/cytoscape/unitTests/");
         String[] args = new String[0];
 
         CytoscapeConfig config = new CytoscapeConfig(args);
         Properties props = config.getProperties();
         assertTrue(props.size() > 10);
         //System.out.println ("edge.color.controller: " + props.getProperty ("edge.color.controller"));
         //System.out.println ("defaultLayoutStrategy: " + props.getProperty ("defaultLayoutStrategy"));
         assertTrue(config.getDefaultLayoutStrategy().equals("hierarchical"));
         assertTrue(props.getProperty("edge.color.controller").equals("interaction"));
         assertTrue(props.getProperty("edge.color.map.interaction.pd").equals("255,0,0"));
 
     } // testReadProperties
 
     public void testConfigDirectFromProperties() throws Exception {
         AllTests.standardOut ("testConfigDirectFromProperties");
         //  Set CYTOSCAPE HOME
         System.setProperty ("CYTOSCAPE_HOME", "src/cytoscape/unitTests/");
         String[] args = new String[0];
 
         CytoscapeConfig config = new CytoscapeConfig(args);
         assertTrue(config.getDefaultLayoutStrategy().equals("hierarchical"));
 
     } // testReadProperties
 
     public static void main(String[] args) {
       if (args.length == 1)
         testDataDirectory = args [0];
        System.out.println ("testDataDirectory: " + testDataDirectory);
 
        junit.textui.TestRunner.run(new TestSuite(CytoscapeConfigTest.class));
     }
 }
