 
 /*
   File: CyCommandLineParser.java 
   
   Copyright (c) 2006, The Cytoscape Consortium (www.cytoscape.org)
   
   The Cytoscape Consortium is: 
   - Institute for Systems Biology
   - University of California San Diego
   - Memorial Sloan-Kettering Cancer Center
   - Pasteur Institute
   - Agilent Technologies
   
   This library is free software; you can redistribute it and/or modify it
   under the terms of the GNU Lesser General Public License as published
   by the Free Software Foundation; either version 2.1 of the License, or
   any later version.
   
   This library is distributed in the hope that it will be useful, but
   WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
   MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
   documentation provided hereunder is on an "as is" basis, and the
   Institute for Systems Biology and the Whitehead Institute 
   have no obligations to provide maintenance, support,
   updates, enhancements or modifications.  In no event shall the
   Institute for Systems Biology and the Whitehead Institute 
   be liable to any party for direct, indirect, special,
   incidental or consequential damages, including lost profits, arising
   out of the use of this software and its documentation, even if the
   Institute for Systems Biology and the Whitehead Institute 
   have been advised of the possibility of such damage.  See
   the GNU Lesser General Public License for more details.
   
   You should have received a copy of the GNU Lesser General Public License
   along with this library; if not, write to the Free Software Foundation,
   Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
 
 package cytoscape.init;
 
 import java.util.*;
 import java.io.File;
 import java.net.URL;
 import cytoscape.data.readers.TextHttpReader;
 import ViolinStrings.Strings;
 
 
 
 public class CyCommandLineParser implements CyInitParams {
 
    // variables available to the command line
    String bioDataServer ;
    boolean noCanonicalization;
    ArrayList expressionFiles;
    boolean inExpression;
    ArrayList graphFiles;
    boolean inGraph;
    ArrayList edgeAttributes;
    boolean inEdge;
    ArrayList nodeAttributes;
    boolean inNode;
    ArrayList pluginFiles;
    boolean inPlugin;
    String species;
    String specifiedPropsFileLocation;
    String vizPropsFileLocation;
    ArrayList projectFiles;
    boolean inProjects;
    boolean useView;
    Integer viewThreshold;
    ArrayList scripts;
    boolean inScript;
    ArrayList currentScript;
    ArrayList resourcePlugins;
   String[] args;
 
    boolean helpRequested = false;
 
    public static String helpString = "";
 
    ////////////////////////////////////////
    // Constructor
    public CyCommandLineParser () {
 
       // use ResourceBundle for static strings like Help/Usage text
       // -- could lead to localizable versions of Cytoscape in the future...
       try {
 	 ResourceBundle coreStrings = ResourceBundle.getBundle(
 							       "cytoscape.resources.CoreStrings",
 							       Locale.getDefault());
 	 helpString = coreStrings.getString("USAGE");
       } catch (MissingResourceException mre) {
 	 mre.printStackTrace();
       }
 
       bioDataServer = null;
       noCanonicalization = false;
       expressionFiles = new ArrayList();
       inExpression = false;
       graphFiles = new ArrayList();
       inGraph = false;
       edgeAttributes = new ArrayList();
       inEdge = false;
       nodeAttributes = new ArrayList();
       inNode = false;
       pluginFiles = new ArrayList();
       inPlugin = false;
       species = null;
       specifiedPropsFileLocation = null;
       vizPropsFileLocation = null;
       projectFiles = new ArrayList();
       inProjects = false;
       useView = true;
       viewThreshold = null;
       inScript = false;
       scripts = new ArrayList();
       resourcePlugins = new ArrayList();
      args = null; 
    }
 
 
    public boolean helpRequested () {
       return helpRequested;
    }
 
    public static String getHelp() {
       return helpString;
    }
 
 
    ////////////////////////////////////////
    // Accessor methods for all possible command line options
 
   public String[] getArgs () {
      return args;
   }

    public List getResourcePlugins () {
       return resourcePlugins;
    }
 
    /**
     * @return the location of a specifed props file, this will be converted to a URL
     */
    public String getPropsFile () {
       return specifiedPropsFileLocation;
    }
 
    public String getVizPropsFile () {
       return vizPropsFileLocation;
    }
 
    public Integer getViewThreshold () {
       return viewThreshold;
    }
 
    /**
     * @return the location of a BioDataServer, if there is one
     */
    public String getBioDataServer () {
       return bioDataServer;
    }
 
    /**
     * Deprecated because name forces many (confusing) double negatives. 
     * Use canonicalizeNames() instead.
     * @return if we should *not* canonicalize names
     * @deprecated
     */
    public boolean noCanonicalization () {
       return noCanonicalization;
    }
 
    /**
     * @return TRUE if we should canonicalize names, 
     * FALSE if we should NOT canonicalize names.
     */
    public boolean canonicalizeNames () {
       return !noCanonicalization;
    }
 
    /**
     * @return the default species for nodes
     */
    public String getSpecies () {
       return species;
    }
 
    /**
     * @return the location of all expression files to load ( this is a list of Strings )
     */
    public List getExpressionFiles () {
       return expressionFiles;
    }
 
    /**
     * @return the locatoin of all Graph Files to load
     *         gml files should be stated as such, otherwise we default to interaction files
     */
    public List getGraphFiles () {
       return graphFiles;
    }
 
    /**
     * @return the location of all Edge Attribute Files
     */
    public List getEdgeAttributeFiles () {
       return edgeAttributes;
    }
 
    /**
     * @return the location of all Node Attribute Files
     */
    public List getNodeAttributeFiles () {
       return nodeAttributes;
    }
 
    public List getProjectFiles () {
       return projectFiles;
    }
 
    /**
     * @return the location of all plugin files to be loaded, these are Strings, but should
     *         be recast as URLs prior to actual loading.
     */
    public List getPluginURLs () {
       ArrayList urls = new ArrayList( pluginFiles.size() );
       for ( int i = 0; i < pluginFiles.size(); i++ ) {
 	 String plugin = ( String )pluginFiles.get(i);
 	 URL url;
 	 try {
 	    if ( plugin.startsWith( "http" ) ) {
 	       plugin = plugin.replaceAll( "http:/" ,"http://" );
 	       plugin = "jar:"+plugin+"!/";
 	       url = new URL( plugin );
 	    } else {
 	       url = new URL( "file", "", plugin );
 	    }
 	    urls.add( url );
 	 } catch ( Exception ue ) {
 	    System.err.println( "Jar: "+pluginFiles.get(i)+ "was not a valid URL" );
 	 }
       }
 
       return urls;
    }
 
    /**
     * @return The execution mode, either GUI or TEXT based on the headless arg.
     */
    public int getMode() {
    	if ( useView )
 		return CyInitParams.GUI;
 	else
 		return CyInitParams.TEXT;
    }
 
    /**
     * Parsing for the command line.
     *<ol>
     * <li> b : BioDataServer : BDS [manifest file]| location of BioDataServer manifest file
     * <li> c : noCanonicalization  | suppress automatic canonicalization
     * <li> e : expression [file name] | expression
     * <li> g : graph [graph files] | gml, sif, cyf files
     * <li> h : help | shows help and exits
     * <li> i : interaction [interaction files] | sif files
     * <li> j : edgeAttributes [attribute files] | old-style edge attribute files
     * <li> n : nodeAttributes [attributes files] | old-style node attribute files
     * <li> s : species [species name] | the name of a species for wchi there is information
     */
    public void parseCommandLine ( String[] args ) {
   	this.args = args;
       //System.out.println( "Parsing command line" );
 
       // for( int i = 0; i < args.length; ++i ) {
       //       System.out.println( i+") "+args[i] );
       //     }
 
 
       int i = 0;
       //System.out.println( i +" "+args+" "+args.length );
       while ( i < args.length ) {
 
 	 //System.out.println( i+". "+args[i] );
 
 	 // help
 	 if ( Strings.isLike( args[i], "-h",0, true ) ||
 	      Strings.isLike( args[i], "--h",0, true ) ||
 	      Strings.isLike( args[i], "-help",0, true ) ||
 	      Strings.isLike( args[i], "--help",0, true ) ) {
 	    helpRequested = true;
 	    return;
 	 }
 
 	 // props file locations
 	 else if ( Strings.isLike( args[i], "-props",0, true ) ) {
 	    i++;
 	    //System.out.println( "Props file specified" );
 	    if ( badArgs(args, i ) )
 	       return;
 	    specifiedPropsFileLocation = args[i];
 	    i++;
 	 }
 
 	 // BioDataServer
 	 else if ( Strings.isLike( args[i], "-b",0, true ) ||
 		   Strings.isLike( args[i], "-bioDataServer",0, true ) ||
 		   Strings.isLike( args[i], "-BDS",0, true ) ) {
         i++;
         if ( badArgs(args, i ) )
           return;
 
         //System.out.println( "BDS: "+args[i] );
         bioDataServer = args[i];
         i++;
       }
 
       // species
       else if ( Strings.isLike( args[i], "-s", 0, true ) ||
                 Strings.isLike( args[i], "-species", 0, true ) ) {
         i++;
         if ( badArgs(args, i ) )
           return;
         //System.out.println( "Species set to: "+args[i] );
         species = args[i];
         i++;
       }
 
       // noCanonicalization
       else if ( Strings.isLike( args[i], "-c", 0, true ) ||
                 Strings.isLike( args[i], "-noCanonicalization", 0, true ) ) {
         i++;
         //System.out.println( "there will be no canonicalization" );
         noCanonicalization = true;
       }
 
       else if ( Strings.isLike( args[i], "-vp", 0, true ) ||
 		Strings.isLike( args[i], "-vprops", 0, true ) ||
                 Strings.isLike( args[i], "-vizprops", 0, true ) ) {
         i++;
 	System.out.println( "VizProps file specified" );
 	if ( badArgs(args, i ) )
 	   return;
 	vizPropsFileLocation = args[i];
 	i++;
       } 	     
 
       // useView
       else if ( Strings.isLike( args[i], "-headless", 0, true ) ||
                 Strings.isLike( args[i], "-noView", 0, true ) ) {
         i++;
         //System.out.println( "there will be no view" );
         useView = false;
       }
 
 
       // viewThreshold
       else if ( Strings.isLike( args[i], "--VT", 0, true ) ||
                 Strings.isLike( args[i], "-vt", 0, true ) ) {
         i++;
         if ( badArgs(args, i ) )
           return;
         viewThreshold = new Integer( args[i] );
         i++;
       }
 
 
       // graph files
       else if ( Strings.isLike( args[i], "-g", 0, true ) ||
                 Strings.isLike( args[i], "-i", 0, true ) ||
                 Strings.isLike( args[i], "-graph", 0, true ) ||
                 Strings.isLike( args[i], "-interaction", 0, true ) ) {
         resetFalse();
         inGraph = true;
         i++;
         if ( badArgs(args, i ) )
           return;
 
         //System.out.println( "Adding "+args[i]+" to the Graph files" );
         graphFiles.add( args[i] );
         i++;
       }
 
 
       // expression files
       else if ( Strings.isLike( args[i], "-e", 0, true ) ||
                 Strings.isLike( args[i], "-expression", 0, true ) ) {
         resetFalse();
         i++;
         if ( badArgs(args, i ) )
           return;
 
         inExpression = true;
         //System.out.println( "Adding "+args[i]+" to the expression data list");
         expressionFiles.add( args[i] );
         i++;
       }
 
 
       // node attributes
       else if ( Strings.isLike( args[i], "-n", 0, true ) ||
                 Strings.isLike( args[i], "-nodeAttributes", 0, true ) ) {
         resetFalse();
         inNode = true;
         i++;
         if ( badArgs(args, i ) )
           return;
 
         //System.out.println( "Adding "+args[i]+" to the Nodeattributes" );
         nodeAttributes.add( args[i] );
         i++;
       }
 
 
       // edge attributes
       else if ( Strings.isLike( args[i], "-j", 0, true ) ||
                 Strings.isLike( args[i], "-edgeAttributes", 0, true ) ) {
         resetFalse();
         inEdge = true;
         i++;
         if ( badArgs(args, i ) )
           return;
 
         //System.out.println( "Adding "+args[i]+" to the Edgeattributes" );
         edgeAttributes.add( args[i] );
         i++;
       }
 
       // scripts
       else if ( Strings.isLike( args[i], "-script", 0, true ) ||
                 Strings.isLike( args[i], "--script", 0, true ) ) {
         resetFalse();
         inScript = true;
         i++;
         if ( badArgs(args, i ) )
           return;
 
         currentScript = new ArrayList();
       }
 
       // scripts
       else if ( Strings.isLike( args[i], "-end", 0, true ) ) {
         inScript = false;
         i++;
         scripts.add( currentScript );
       }
 
       //resource plugins
       else if (  Strings.isLike( args[i], "-rp", 0, true ) ||
                  Strings.isLike( args[i], "-resourcePlugin", 0, true ) ) {
         resetFalse();
         i++;
         if ( badArgs(args, i ) )
           return;
         resourcePlugins.add( args[i] );
         i++;
       }
 
       // plugins
       else if ( Strings.isLike( args[i], "-p", 0, true ) ||
                 Strings.isLike( args[i], "-plugin", 0, true ) ||
                 Strings.isLike( args[i], "--JLW", 0, true ) ||
                 Strings.isLike( args[i], "--JLD", 0, true ) ||
                 Strings.isLike( args[i], "--JLL", 0, true ) ) {
         resetFalse();
         inPlugin = true;
         i++;
         if ( badArgs(args, i ) )
           return;
         parsePluginArgs( args[i] );
         i++;
       }
 
 
       else if ( args[i].startsWith( "-" ) ) {
         // flag for plugin perhaps
         resetFalse();
         i++;
       }
 
       //////////////////////////////
       // Continuation Catches
       else if ( inScript ) {
         //System.out.println( "Adding "+args[i]+" to the Projects" );
         currentScript.add( args[i] );
         i++;
        }
 
       else if ( inProjects ) {
         //System.out.println( "Adding "+args[i]+" to the Projects" );
         projectFiles.add( args[i] );
         i++;
        }
 
       else if ( inNode ) {
         //System.out.println( "Adding "+args[i]+" to the Nodeattributes" );
         nodeAttributes.add( args[i] );
         i++;
       }
       else if ( inEdge ) {
         //System.out.println( "Adding "+args[i]+" to the Edgeattributes" );
         edgeAttributes.add( args[i] );
         i++;
       }
       else if ( inExpression ) {
         //System.out.println( "Adding "+args[i]+" to the expression data list");
         expressionFiles.add( args[i] );
         i++;
       }
       else if ( inGraph ) {
         //System.out.println( "Adding "+args[i]+" to the Graph files" );
         graphFiles.add( args[i] );
         i++;
       }
       else if ( inPlugin ) {
         parsePluginArgs( args[i] );
         i++;
       }
 
       else {
         //System.err.println( "nothing matches, call for help" );
         //helpRequested = true;
         i++;
         //return;
       }
 
     }// end args for loop
   }
 
   private void parsePluginArgs ( String arg ) {
 
      File plugin_file = new File( arg );
 
         // test for directory
         if ( plugin_file.isDirectory() ) {
           // load a directory of plugins
           String[] fileList = plugin_file.list();
           String slashString="";
           if( !(plugin_file.getPath().endsWith("/") ) ) slashString="/";
 
           for(int j = 0; j < fileList.length; j++) {
             if(!(fileList[j].endsWith(".jar"))) continue;
             String jarString = plugin_file.getPath() + slashString + fileList[j];
             // jar file found, add to list of plugins to load.
             pluginFiles.add( jarString );
             System.out.println( "Plugin in directory added: "+jarString );
           }
         }
 
         // test for jar file
         else if ( plugin_file.toString().endsWith( ".jar" ) ) {
           // add single jar file
           pluginFiles.add( plugin_file.toString()  );
           System.out.println( "Adding single jar: "+plugin_file );
         }
 
         else if ( plugin_file.toString().startsWith( "http:" ) || plugin_file.toString().startsWith( "jar:") ) {
           pluginFiles.add( plugin_file.toString()  );
           System.out.println( "Adding URL Jar: "+plugin_file );
         }
 
         // not a jar or directory, assume it is a manifest
         else {
           try {
             TextHttpReader reader = new TextHttpReader(plugin_file.toString());
             reader.read();
             String text = reader.getText();
             String lineSep = System.getProperty("line.separator");
             String[] allLines = text.split(lineSep);
             for (int j=0; j < allLines.length; j++) {
               String pluginLoc = allLines[j];
 
               if ( pluginLoc.endsWith( ".jar" ) ) {
                 System.out.println( "Pluign found from manifest file: "+pluginLoc );
                 pluginFiles.add( pluginLoc );
               }
             }
           } catch ( Exception exp ) {
 	    exp.printStackTrace();
             System.err.println( "error reading manifest file"+plugin_file );
           }
         }
 
   }
 
 
   /**
    * If the next argument is either null, or a flag, then this was a bad set of arguments
    * and we should just exit and print help.
    * @param args the command line arguments
    * @param i the counter;
    */
   private boolean badArgs ( String[] args, int i ) {
 
     // test to see if we are at then end of the arg list
     if ( args.length == i ) {
       System.out.println( "Too few arguments" );
       helpRequested = true;
       return true;
     }
     // test to see if next is a flag
     else if ( args[i].startsWith( "-" ) ) {
       System.out.println( "Flag where there should be a value" );
       helpRequested = true;
       return true;
     }
     return false;
   }
 
 
 
   private void resetFalse () {
     inGraph = false;
     inPlugin = false;
     inExpression = false;
     inNode = false;
     inEdge = false;
     inScript = false;
   }
 
 }
 
 
