 /*
  * $Id$
  *
  * Copyright 2003-2005 Wanadoo Nederland B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.common.ant;
 
 import java.io.File;
 
 import org.apache.tools.ant.util.FileNameMapper;
 
 import org.xins.common.MandatoryArgumentChecker;
 
 /**
  * Apache Ant mapper that change the case of the first character of a filename
  * to upper, if it is a lowercase letter.
  *
  * @version $Revision$ $Date$
  * @author Anthony Goubard (<a href="mailto:anthony.goubard@nl.wanadoo.com">anthony.goubard@nl.wanadoo.com</a>)
  *
  * @since XINS 1.0.0
  */
 public class HungarianMapper implements FileNameMapper {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    public void setFrom(String from) {
       // empty
    }
 
    public void setTo(String to) {
       // empty
    }
 
    /**
     * Returns an array containing the target filename(s) for the given source
     * file.
     *
     * <p>The implementation of this method checks if the name of the file
     * identified by the specified path starts with a lowercase letter. If it
     * does, then it returns the name with the first character converted to an
     * uppercase character.
     *
     * @param sourceFileName
     *    the path to the file, should not be <code>null</code>.
     *
     * @return
     *    the target filename(s) for the given source file; this implementation
     *    always returns a 1-size array, and the element is never
     *    <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>sourceFileName == null</code>.
     */
    public String[] mapFileName(String sourceFileName)
    throws IllegalArgumentException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("sourceFileName", sourceFileName);
 
       // Get the file name
       String fileName = new File(sourceFileName).getName();
 
       String result;
 
       // If the first character is not a lowercase letter, then return the
       // original file name
       if (fileName.charAt(0) < 'a' || fileName.charAt(0) > 'z') {
          result = sourceFileName;
 
       // Otherwise convert the first filename letter to uppercase and return
       // that
       } else {
          int index = sourceFileName.lastIndexOf(filename);
         result = sourceFileName.substring(0, index)
                 + fileName.substring(0,1).toUpperCase()
                 + fileName.substring(1);
       }
 
       return new String[] { result };
    }
 }
