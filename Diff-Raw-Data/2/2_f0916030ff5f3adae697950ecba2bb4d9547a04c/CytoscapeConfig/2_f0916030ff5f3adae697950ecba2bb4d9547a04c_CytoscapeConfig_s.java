 // CytoscapeConfig.java:  a class to handle run-time configuration of luca
 //------------------------------------------------------------------------------------------
 // $Revision$   
 // $Date$ 
 // $Author$
 //-----------------------------------------------------------------------------------
 package cytoscape;
 //------------------------------------------------------------------------------------------
 import java.io.*;
 import java.util.*;
 
 import gnu.getopt.Getopt;
 import gnu.getopt.LongOpt;
 //------------------------------------------------------------------------------------------
 /**
  * handles the parsing of, and access to, command line arguments for cytoscape, and
  * the control of various features and attributes via 'cytoscape.props' files
  */
 public class CytoscapeConfig {
 
   protected String argSpecificationString = "n:j:g:b:i:he:vWs:;";
 
   protected String [] commandLineArguments;
   protected String[] argsCopy;
   protected boolean helpRequested = false;
   protected boolean inputsError = false;
   protected boolean displayVersion = false;
   protected String geometryFilename = null;
   protected String bioDataDirectory = null;
   protected String expressionFilename = null;
   protected String interactionsFilename = null;
   protected Vector nodeAttributeFilenames = new Vector ();
   protected Vector edgeAttributeFilenames = new Vector ();
   protected String defaultSpeciesName = null;
 
   protected StringBuffer errorMessages = new StringBuffer ();
     // system and user property files use the same name
   protected Properties props;
 
 //------------------------------------------------------------------------------------------
 public CytoscapeConfig (String [] args)
 {
   props = readProperties ();
     // make a copy of the args to parse here (getopt can mangle the array it parses)
   commandLineArguments = new String[args.length];
   System.arraycopy(args, 0, commandLineArguments, 0, args.length);
     // make a copy of the arguments for later use
   argsCopy = new String[args.length];
   System.arraycopy(args, 0, argsCopy, 0, args.length);
   parseArgs ();
 
 }
 //------------------------------------------------------------------------------------------
 public String[] getArgs() {
     String[] returnVal = new String[argsCopy.length];
     System.arraycopy(argsCopy, 0, returnVal, 0, argsCopy.length);
     return returnVal;
 }
 //------------------------------------------------------------------------------------------
 public String getGeometryFilename ()
 {
   return geometryFilename;
 }
 //------------------------------------------------------------------------------------------
 public String getExpressionFilename ()
 {
   return expressionFilename;
 }
 //------------------------------------------------------------------------------------------
 public String getBioDataDirectory ()
 {
   return bioDataDirectory;
 }
 //------------------------------------------------------------------------------------------
 public String getInteractionsFilename ()
 {
   return interactionsFilename;
 }
 //------------------------------------------------------------------------------------------
 public int getNumberOfNodeAttributeFiles ()
 {
   return nodeAttributeFilenames.size ();
 }
 //------------------------------------------------------------------------------------------
 public int getNumberOfEdgeAttributeFiles ()
 {
   return edgeAttributeFilenames.size ();
 }
 //------------------------------------------------------------------------------------------
 public String [] getNodeAttributeFilenames ()
 {
   return (String []) nodeAttributeFilenames.toArray (new String [0]);
 }
 //------------------------------------------------------------------------------------------
 public String [] getEdgeAttributeFilenames ()
 {
   return (String []) edgeAttributeFilenames.toArray (new String [0]);
 }
 
 
 
 /**
  * Add the given node attributes filename (as per opened in gui).
  *
  * added by dramage 2002-08-21
  */
 public void addNodeAttributeFilename (String filename) {
     if (!nodeAttributeFilenames.contains(filename))
 	nodeAttributeFilenames.add(filename);
 }
 
 /**
  * Add the given edge attributes filename (as per opened in gui).
  *
  * added by dramage 2002-08-21
  */
 public void addEdgeAttributeFilename (String filename) {
     if (!edgeAttributeFilenames.contains(filename))
 	edgeAttributeFilenames.add(filename);
 }
 
 
 //------------------------------------------------------------------------------------------
 public String [] getAllDataFileNames ()
 {
   Vector allFileNames = new Vector ();
   String [] nodeAttributeFiles = getNodeAttributeFilenames ();
   String [] edgeAttributeFiles = getEdgeAttributeFilenames ();
 
   for (int n=0; n < nodeAttributeFiles.length; n++)
     allFileNames.add (nodeAttributeFiles [n]);
 
   for (int e=0; e < edgeAttributeFiles.length; e++)
     allFileNames.add (edgeAttributeFiles [e]);
 
   if (geometryFilename != null)
     allFileNames.add (geometryFilename);
 
   if (interactionsFilename != null)
     allFileNames.add (interactionsFilename);
 
   if (expressionFilename != null)
     allFileNames.add (expressionFilename);
 
   return (String []) allFileNames.toArray (new String [0]);
 
 } // getAllDataFileNames
 //------------------------------------------------------------------------------------------
 public String [] getAllDataFileExtensions ()
 {
   String [] fullNames = getAllDataFileNames ();
   Vector allExtensions = new Vector ();
 
   for (int i=0; i < fullNames.length; i++) {
     String filename = fullNames [i];
     int positionOfLastDot = filename.lastIndexOf (".");
     if (positionOfLastDot > 0) {
       String extension = filename.substring (positionOfLastDot + 1);
       if (!allExtensions.contains (extension))
         allExtensions.add (extension);
       } // if
     } // for i
 
   return (String []) allExtensions.toArray (new String [0]);
 
 } // getAllDataFileExtensions
 //------------------------------------------------------------------------------------------
 public String getDefaultSpeciesName ()
 {
   return defaultSpeciesName;
 }
 //------------------------------------------------------------------------------------------
 public boolean helpRequested ()
 {
   return helpRequested;
 }
 //------------------------------------------------------------------------------------------
 public boolean inputsError ()
 {
   return inputsError;
 }
 //------------------------------------------------------------------------------------------
 public boolean displayVersion ()
 {
   return displayVersion;
 }
 //------------------------------------------------------------------------------------------
 public Properties getProperties ()
 {
   return props;
 }
 //------------------------------------------------------------------------------------------
 /**
  * read (if possible) properties from CYTOSCAPE_HOME/cytoscape.props, 
  * from HOME/cytoscape.props, and  finally from PWD/cytoscape.props.
  * the latter properties (when they are duplicates) superceed previous ones
  * in the absence of any props, hard-coded defaults within various classes 
  * will be used.
  */
 protected Properties readProperties ()
 {
   Properties systemProps = null;
   Properties userGeneralProps = null;
   Properties userSpecialProps = null;
   String propsFileName = "cytoscape.props"; // there may be 3 copies of this
 
   File propsFile = createFile (System.getProperty ("CYTOSCAPE_HOME"), propsFileName);
   if (propsFile != null)
     systemProps = readOnePropertyFile (null, propsFile);
 
   File userGeneralPropsFile = createFile (System.getProperty ("user.home"), propsFileName);
   if (userGeneralPropsFile != null)
     userGeneralProps = readOnePropertyFile (systemProps, userGeneralPropsFile);
 
   File userSpecialPropsFile = createFile  (System.getProperty ("user.dir"), propsFileName);
   if (userSpecialPropsFile != null)
     userSpecialProps = readOnePropertyFile (userGeneralProps, userSpecialPropsFile);
 
   /* we will return a valid Properties object; if any properties files
    * were found and read, we copy them in sequentially so that duplicate
    * keys in the users file overwrite the sytems defaults */
   Properties returnVal = new Properties();
   if (systemProps != null) {returnVal.putAll(systemProps);}
   if (userGeneralProps != null) {returnVal.putAll(userGeneralProps);}
   if (userSpecialProps != null) {returnVal.putAll(userSpecialProps);}
   return returnVal;
 
 } // readProperties
 //------------------------------------------------------------------------------------------
 /**
  * return a File which is known to exist, and is readable.
  */
 private File createFile (String directory, String filename)
 {
   if (directory != null) {
     File result = new File (directory, filename);
     if (result.canRead ())
       return result;
     }
 
   return null;
 
 } // createFile
 //------------------------------------------------------------------------------------------
 /**
  *  read properties from the named file, combining with previously read properties
  *  when possible.
  *  @param priorProps  previously-read properties of a more general status
  *  @param propsFile   a property file which is guaranteed to be readable
  */
 private Properties readOnePropertyFile (Properties priorProps, File propsFile)
 {
   Properties newProps = new Properties ();
 
   if (priorProps != null)
     newProps =  new Properties (priorProps);
 
   try {
     FileInputStream in = new FileInputStream (propsFile);
     newProps.load (in);
     } // try
   catch (FileNotFoundException ignore) {;}
   catch (IOException ignore) {;}
 
   return newProps;
 
 } // readOnePropertyFile
 //------------------------------------------------------------------------------------------
 protected void parseArgs ()
 {
   helpRequested = false;
   boolean argsError = false;
   String tmp;
 
   if (commandLineArguments == null || commandLineArguments.length == 0)
     return;
 
   LongOpt[] longopts = new LongOpt[0];
   Getopt g = new Getopt ("cytoscape", commandLineArguments, argSpecificationString, longopts);
   g.setOpterr (false); // We'll do our own error handling
 
   int c;
   while ((c = g.getopt ()) != -1) {
    switch (c) {
      case 'n':
        nodeAttributeFilenames.add (g.getOptarg ());
        break;
      case 'j':
        edgeAttributeFilenames.add (g.getOptarg ());
        break;
      case 'g':
        geometryFilename = g.getOptarg ();
        break;
      case 'b':
        bioDataDirectory = g.getOptarg ();
        break;
      case 'i':
        interactionsFilename = g.getOptarg ();
        break;
      case 'e':
        expressionFilename = g.getOptarg ();
        break;
      case 'h':
        helpRequested = true;
        break;
      case 's':
        defaultSpeciesName = g.getOptarg ();
        break;
      case 'v':
        displayVersion = true;
        break;
      case '?': // Optopt==0 indicates an unrecognized long option, which is reserved for plugins 
       int theOption = g.getOptopt();
       if (theOption != 0 )
         errorMessages.append ("The option '" + (char)theOption + "' is not valid\n");
        break;
      default:
        errorMessages.append ("unexpected argument: " + c + "\n");
        inputsError = true;
        break;
      } // switch on c
     }   
 
   if (!inputsError)
     inputsError = !legalArguments ();
 
 } // parseArgs
 //---------------------------------------------------------------------------------
 protected boolean legalArguments ()
 {
   boolean legal = true;
   
     // make sure there is just one source for the graph
   if ((geometryFilename != null) && (interactionsFilename != null)) {
     errorMessages.append (" -  geometry & interactions both specify a graph: use only one\n");
     legal = false;
     }
 
   return legal;
 
 } // legalArguments
 //---------------------------------------------------------------------------------
 public String getErrorMessages ()
 {
   return errorMessages.toString ();
 
 }
 //---------------------------------------------------------------------------------
 public String getUsage ()
 {
    StringBuffer sb = new StringBuffer ();
    String programName = "cytoscape";
    sb.append ("usage: ");
    sb.append (programName);
    sb.append (" [optional arguments]");
    sb.append ("\n\n");
 
    sb.append ("\n      optional arguments\n");
    sb.append ("      ------------------\n");
    sb.append (" -g  <geometry file name>          (xxxx.gml)\n");
    sb.append (" -b  <bioData directory>           (./biodata\n");
    sb.append (" -i  <interactions filename>       (yyyy.intr)\n");
    sb.append (" -e  <expression filename>         (zzz.mrna)\n");
   sb.append (" -s  <default species name>        (\"Saccharomyces cerversiae\")\n");
    sb.append (" -n  <nodeAttributes filename>     (zero or more)\n");
    sb.append (" -j  <edgeAttributes filename>     (zero or more)\n");
    sb.append ("\n");
 
    sb.append (" -h  (display usage\n");
    sb.append (" -v  (display version)\n");
 
    return sb.toString ();
 
 } // getUsage
 //---------------------------------------------------------------------------------
 public String toString ()
 {
    StringBuffer sb = new StringBuffer ();
    sb.append ("---------- requested options:\n");
    sb.append ("            geometry file: " + geometryFilename + "\n");
    sb.append ("        interactions file: " + interactionsFilename + "\n");
    sb.append ("          expression file: " + expressionFilename + "\n");
    sb.append ("         bioDataDirectory: " + bioDataDirectory + "\n");
    sb.append ("       defaultSpeciesName: " + defaultSpeciesName + "\n");
  
    for (int i=0; i < nodeAttributeFilenames.size (); i++)
      sb.append ("        nodeAttributeFile: " + (String) nodeAttributeFilenames.get(i) + "\n");
 
   for (int i=0; i < edgeAttributeFilenames.size (); i++)
      sb.append ("        edgeAttributeFile: " + (String) edgeAttributeFilenames.get(i) + "\n");
    
   return sb.toString ();
 
 } // toString 
 //---------------------------------------------------------------------------------
 } // class CytoscapeConfig
