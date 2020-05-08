 package org.geworkbench.components.parsers;
 
 import org.geworkbench.bison.datastructure.biocollections.microarrays.CSExprMicroarraySet;
 import org.geworkbench.bison.parsers.resources.Resource;
 
 import javax.swing.filechooser.FileFilter;
 import java.io.File;
 import java.io.InterruptedIOException;
 import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
 
 /**
  * <p>Copyright: Copyright (c) 2003</p>
  * <p>Company: First Genetic Trust Inc.</p>
  *
  * @author First Genetic Trust, Inc.
  * @version 1.0
  */
 
 /**
  * 
  * @author my2248
  * @version $Id: FileFormat.java,v 1.4 2009-10-07 15:38:48 my2248 Exp $
  * 
  * Base class for reading input data files complying to a desired format.
  * Support for any given microarray data format (e.g., Affymetrix MAS 5,
  * GenePix, etc), can be provide as follows:
  * <UL>
  * <LI> Create a new concrete class extending <code>FileFormat</code> and
  * provide method implementations approrpriate for the new format. </LI>
  * <LI> In the configuration file of the application, list an object of the
  * new concrete class as a plugin associated with the extension point
  * titled "input-format".</LI>
  * </UL>
  */
 public abstract class FileFormat {
     /**
      * The display name of the format.
      */
     protected String formatName = null;
 
     /**
      * @return The format name.
      */
     public String getFormatName() {
        return formatName;
     }
 
     /**
      * Checks if the contents of the designated file conform to the format.
      *
      * @param file File to check.
      * @return True or false, depending on if the argument is well formed
      *         according to the format or not.
      */
     public abstract boolean checkFormat(File file) throws InterruptedIOException;
 
     /**
      * Return a <code>Resource</code> object for the designated file.
      *
      * @param file
      * @return
      */
     
     
     public abstract Resource getResource(File file);
 
     /**
      * Generates and returns a <code>MicrorarrauSet</code> from the designated
      * argument. In <code>file</code> does not conform to the format, returns
      * <code>null</code>.
      *
      * @param file The file containing the input data.
      * @return The corresponding <code>MicroarraySet</code> object.
      */
     public abstract DSDataSet getMArraySet(File file) throws InputFileFormatException, InterruptedIOException;
 
     /**
      * Generates and returns a <code>MicrorarraySet</code> from the designated
      * argument. In <code>file</code> does not conform to the format, returns
      * <code>null</code>.
      *
      * @param file The file containing the input data.
      * @param maSet The set to load the data in
      * @return The corresponding <code>MicroarraySet</code> object.
      */
     public void getMArraySet(File file, CSExprMicroarraySet maSet) throws InputFileFormatException , InterruptedIOException{
         throw new UnsupportedOperationException();
     }
 
     /**
      * Return the list of extensions (if any) for the files following this
      * format.
      *
      * @return
      */
     public abstract String[] getFileExtensions();
 
     /**
      * @return An (optional) <code>FileFilter</code> to be used in gating the
      *         files offered to the user for selection. Useful, e.g., when the
      *         files of the format at hand have predefined extensions.
      */
     public FileFilter getFileFilter() {
         return null;
     }
 
 }
 
