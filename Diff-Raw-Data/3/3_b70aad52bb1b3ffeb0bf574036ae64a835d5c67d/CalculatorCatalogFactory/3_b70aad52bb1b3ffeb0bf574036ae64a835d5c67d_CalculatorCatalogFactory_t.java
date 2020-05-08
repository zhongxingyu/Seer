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
  //----------------------------------------------------------------------------
 // $Revision$
 // $Date$
 // $Author$
 //----------------------------------------------------------------------------
 package cytoscape.visual;
 
 import java.util.Properties;
 import java.io.*;
 import java.beans.*;
 
 import cytoscape.Cytoscape;
 import cytoscape.CytoscapeInit;
 import cytoscape.visual.calculators.NodeLabelCalculator;
 import cytoscape.visual.calculators.GenericNodeLabelCalculator;
 import cytoscape.visual.mappings.*;
 
 /**
  * This class provides a static method for reading a CalculatorCatalog object
  * from file, using parameters specified in a supplied CytoscapeConfig.
  * What's provided here is the set of files from which to read the calculator
  * information, as well as the construction of a suitable default visual style
  * if one does not already exist.
  */
 public abstract class CalculatorCatalogFactory {
     
   static File propertiesFile;
   static Properties vizmapProps;
   
   /**
    * Loads a CalculatorCatalog object from the various properties files
    * specified by the options in the supplied CytoscapeConfig object.
    * The catalog will be properly initialized with known mapping types
    * and a default visual style (named "default").
    */
   public static CalculatorCatalog loadCalculatorCatalog () {
         
     final CalculatorCatalog calculatorCatalog = new CalculatorCatalog();
     // register mappings
     calculatorCatalog.addMapping("Discrete Mapper", DiscreteMapping.class);
     calculatorCatalog.addMapping("Continuous Mapper", ContinuousMapping.class);
     calculatorCatalog.addMapping("Passthrough Mapper", PassThroughMapping.class);
         
     //load in calculators from file
     //we look for, in order, a file in CYTOSCAPE_HOME, one in the current directory,
     //then one in the user's home directory. Note that this is a different order than
     //for cytoscape.props, because we always write vizmaps to the home directory
     
     boolean propsFound = false;
     vizmapProps = new Properties();
 
     //2. Try the current working directory
     if ( !propsFound ) {
       try {
         File file = new File( System.getProperty ("user.dir"), "vizmap.props" );
         vizmapProps.load( new FileInputStream( file ) );
         propertiesFile = file;
         System.out.println( "vizmaps found at: "+propertiesFile );
         propsFound = true;
       } catch ( Exception e ) {
         propsFound = false;
       }
     }
      
     //3. Try VIZMAP_HOME
     if ( !propsFound ) {
       try {
         File file = new File( System.getProperty ("CYTOSCAPE_HOME"), "vizmap.props" );
         vizmapProps.load( new FileInputStream( file ) );
         propertiesFile = file;
         System.out.println( "vizmaps found at: "+propertiesFile );
         propsFound = true;
       } catch ( Exception e ) {
         // error
         propsFound = false;
       }
     }
 
     //4. Try ~/.vizmap
     if ( !propsFound ) {
       try {
         File file = CytoscapeInit.getConfigFile( "vizmap.props" );
         vizmapProps.load( new FileInputStream( file ) );
         propertiesFile = file;
         System.out.println( "vizmaps found at: "+propertiesFile );
         propsFound = true;
       } catch ( Exception e ) {
         // error
         propsFound = false;
       }
     }
 
     if ( vizmapProps == null ) {
       System.out.println( "vizmaps not found" );
       vizmapProps = new Properties();
     }
         
     //now load using the constructed Properties object
     CalculatorIO.loadCalculators( vizmapProps, calculatorCatalog);
         
     //make sure a default visual style exists, creating as needed
     //this must be done before loading the old-style visual mappings,
     //since that class works with the default visual style
     VisualStyle defaultVS = calculatorCatalog.getVisualStyle("default");
     if (defaultVS == null) {
       defaultVS = new VisualStyle("default");
       //setup the default to at least put canonical names on the nodes
       String cName = "Common Names";
       NodeLabelCalculator nlc = calculatorCatalog.getNodeLabelCalculator(cName);
       if (nlc == null) {
         PassThroughMapping m =
           new PassThroughMapping(new String(), cytoscape.data.Semantics.COMMON_NAME);
         nlc = new GenericNodeLabelCalculator(cName, m);
       }
       defaultVS.getNodeAppearanceCalculator().setNodeLabelCalculator(nlc);
       calculatorCatalog.addVisualStyle(defaultVS);
     }
         
 
     Cytoscape.getSwingPropertyChangeSupport().addPropertyChangeListener ( new PropertyChangeListener () {
         public void propertyChange ( PropertyChangeEvent e ) {
           if ( e.getPropertyName() == Cytoscape.CYTOSCAPE_EXIT ) {
             System.out.println( "Save Vizmaps back to: "+propertiesFile );
             CalculatorIO.storeCatalog( calculatorCatalog, propertiesFile );
           }
         }
       } );
 
     return calculatorCatalog;
   }
 
   
   
 }
 
