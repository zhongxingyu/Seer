 // FileReadingAbstractions.java
 
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
 package cytoscape.data.readers;
 import java.io.*;
 import java.util.jar.*;
 import java.net.*;
 import y.view.Graph2D;
 import cytoscape.GraphObjAttributes;
 import cytoscape.CytoscapeConfig;
 import cytoscape.data.*;
 import cytoscape.data.servers.*;
 import cytoscape.data.readers.*;
 //----------------------------------------------------------------------------
 public class FileReadingAbstractions {
 
 //----------------------------------------------------------------------------
 /**
  * This method constructs an InputStream from a file specified by the
  * filename argument. The tricky bit is that this filename could represent
  * an ordinary file, or a file within a jar archive, indicated by the
  * leading text "jar://" in the filename. This method is designed to handle
  * both cases. If the file represents a properties file, this InputStream
  * can be used directly by the load method of Properties; if, instead, the
  * text as characters is desired, then the returned InputStream can be
  * wrapped by the caller in an InputStreamReader.
  *
  * A null value is returned if the filename is null, or if the InputStream
  * cannot be constructed for any reason (usually some kind of IOException).
  */
 public static InputStream getInputStream(String filename) {
     if (filename == null) {return null;}
     
     try {
         if (filename.trim().startsWith ("jar://")) {
             String realFile = filename.substring (6);
             ClassLoader cl = FileReadingAbstractions.class.getClassLoader();
             URL url = cl.getResource(realFile);
             JarURLConnection juc = (JarURLConnection) url.openConnection();
             JarFile jarFile = juc.getJarFile();
             InputStream is = jarFile.getInputStream(jarFile.getJarEntry(realFile));
             return is;
         } else {
             InputStream is = new FileInputStream(filename);
             return is;
         }
     } catch (Exception e) {
         System.err.println("In FileReadingAbstractions.getInputStream:");
         String err = "Exception while constructing InputStream from file " + filename;
         System.err.println(err);
         e.printStackTrace();
         System.err.println("returning null and continuing execution");
     }
     return null;
 }
 //----------------------------------------------------------------------------
 public static Graph2D loadGMLBasic (String filename, 
                                     GraphObjAttributes edgeAttributes) 
 {
   GMLReader reader = new GMLReader (filename);
   return loadBasic (reader, edgeAttributes);
 }
 //----------------------------------------------------------------------------
 public static Graph2D loadIntrBasic (BioDataServer dataServer,
                                      String species,
                                      String filename, 
                                      GraphObjAttributes edgeAttributes) 
 {
   InteractionsReader reader = new InteractionsReader (dataServer, species, filename);
   return loadBasic (reader, edgeAttributes);
 
 }
 //----------------------------------------------------------------------------
 public static Graph2D loadBasic (GraphReader reader, GraphObjAttributes edgeAttributes) 
 {
   reader.read ();
   GraphObjAttributes newEdgeAttributes = reader.getEdgeAttributes ();
   edgeAttributes.add (newEdgeAttributes);
   edgeAttributes.addNameMap (newEdgeAttributes.getNameMap ());
   edgeAttributes.addClassMap (newEdgeAttributes.getClassMap ());
  return reader.getGraph();
 
 }
 //----------------------------------------------------------------------------
 public static void initAttribs (BioDataServer dataServer,
                                 String species,
                                 CytoscapeConfig config,
                                 Graph2D graph,
                                 GraphObjAttributes nodeAttributes,
                                 GraphObjAttributes edgeAttributes)
 {
   String [] edgeAttributeFilenames = config.getEdgeAttributeFilenames ();
   String [] nodeAttributeFilenames = config.getNodeAttributeFilenames ();
   initAttribs (dataServer, species, graph, nodeAttributes, edgeAttributes,
                nodeAttributeFilenames, edgeAttributeFilenames);
 
 }
 //----------------------------------------------------------------------------
 public static void initAttribs (BioDataServer dataServer,
                                 String species,
                                 Graph2D graph,
                                 GraphObjAttributes nodeAttributes,
                                 GraphObjAttributes edgeAttributes,
                                 String [] nodeAttributeFilenames,
                                 String [] edgeAttributeFilenames)
 {
   if (nodeAttributeFilenames != null)
     for (int i=0; i < nodeAttributeFilenames.length; i++) {
        try {
          nodeAttributes.readAttributesFromFile (dataServer, species, nodeAttributeFilenames[i]);
          } 
        catch (NumberFormatException nfe) {
          System.err.println (nfe.getMessage ());
           nfe.printStackTrace ();
          } 
       catch (IllegalArgumentException iae) {
         System.err.println (iae.getMessage ());
         iae.printStackTrace ();
         }
        catch (FileNotFoundException fnfe) {
          System.err.println (fnfe.getMessage ());
          fnfe.printStackTrace ();
          }
        }
 
    if (edgeAttributeFilenames != null)
      for (int i=0; i < edgeAttributeFilenames.length; i++) {
        try {
          edgeAttributes.readAttributesFromFile (edgeAttributeFilenames [i]);
          } 
        catch (Exception excp) {
          System.err.println (excp.getMessage ());
          excp.printStackTrace ();
          } 
         } // for i
 
     if (nodeAttributes != null)
       addNameMappingToAttributes (graph.getNodeArray (), nodeAttributes);
 
     // no need to add name mapping for edge attributes -- it has already been
     // done, at the time when the interactions file (or gml file) was read
 
 
 } // initAttribs
 //----------------------------------------------------------------------------
     /**
      * add node-to-canonical name mapping (and the same for edges), so that
      * node attributes can be retrieved by simply knowing the y.base.node,
      * which is the basic view of data in this program.  for example:
      *
      *  NodeCursor nc = graph.selectedNodes (); 
      *  for (nc.toFirst (); nc.ok (); nc.next ()) {
      *    Node node = nc.node ();
      *    String canonicalName = nodeAttributes.getCanonicalName (node);
      *    HashMap bundle = nodeAttributes.getAllAttributes (canonicalName);
      *    }
      * 
      * 
      */
 protected static void addNameMappingToAttributes (Object [] graphObjects,
                                                   GraphObjAttributes attributes)
 {
   for (int i=0; i < graphObjects.length; i++) {
     Object graphObj = graphObjects [i];
     String canonicalName = graphObj.toString ();
     attributes.addNameMapping (canonicalName, graphObj);
    }
 
 } // addNameMappingToAttributes
 //----------------------------------------------------------------------------
 } // FileReadingAbstractions
 
 
