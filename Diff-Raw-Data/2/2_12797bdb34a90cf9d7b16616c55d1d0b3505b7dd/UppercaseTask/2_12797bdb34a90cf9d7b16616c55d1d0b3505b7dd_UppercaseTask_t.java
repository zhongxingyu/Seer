 /*
  * $Id$
  *
  * Copyright 2003-2007 Orange Nederland Breedband B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.common.ant;
 
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.Project;
 import org.apache.tools.ant.Task;
 
 /**
  * Apache Ant task that uppercase a text. Note that hyphens '-' are
  * translated to underscores '_'.
  *
  * @version $Revision$ $Date$
  * @author <a href="mailto:anthony.goubard@orange-ftgroup.com">Anthony Goubard</a>
  *
  * @since XINS 1.1.0
  */
 public class UppercaseTask extends Task {
 
    /**
     * Name of the property to store the uppercase value in.
     */
    private String _propertyName;
 
    /**
     * The text that has to be set in uppercase.
     */
    private String _text;
 
    /**
     * Sets the name of the property.
     *
     * @param newPropertyName
     *    the name of the property to store the uppercase value.
     */
    public void setProperty(String newPropertyName) {
       _propertyName = newPropertyName;
    }
 
    /**
     * Sets the text to be set in uppercase.
     *
     * @param text
     *    the text that has to be set in uppercase.
     */
    public void setText(String text) {
       _text = text;
    }
 
    /**
     * Called by the project to let the task do its work.
     *
     * @throws BuildException
     *    if something goes wrong with the build.
     */
    public void execute() throws BuildException {
 
       if (_propertyName == null) {
          throw new BuildException("A property value needs to be specified.");
       }
 
       if (_text == null) {
         throw new BuildException("A text value needs to be specified.");
       }
 
       if (getProject().getUserProperty(_propertyName) != null) {
          String message = "Override ignored for property \""
                         + _propertyName
                         + "\".";
          log(message, Project.MSG_VERBOSE);
          return;
       }
 
       String uppercase = _text.toUpperCase();
 
       uppercase = uppercase.replace('-', '_');
       uppercase = uppercase.replace('.', '_');
 
       getProject().setUserProperty(_propertyName, uppercase);
    }
 }
