 /*
  * $Id$
  *
  * Copyright 2003-2006 Wanadoo Nederland B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.common.ant;
 
 import java.io.File;
 
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.StringTokenizer;
 import java.util.Vector;
 
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.BuildListener;
 import org.apache.tools.ant.Project;
 import org.apache.tools.ant.ProjectHelper;
 import org.apache.tools.ant.Task;
 import org.apache.tools.ant.taskdefs.Property;
 
 import org.xins.common.text.URLEncoding;
 import org.xins.common.xml.Element;
 
 /**
  * Apache Ant task that call a function of a XINS API, the result is stored 
  * in Ant properties.
  *
  * @version $Revision$ $Date$
  * @author Anthony Goubard (<a href="mailto:anthony.goubard@nl.wanadoo.com">anthony.goubard@nl.wanadoo.com</a>)
  *
  * @since XINS 1.5.0
  */
 public class CallXINSTask extends Task {
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>CallTargetsTask</code> instance.
     */
    public CallXINSTask() {
       // empty
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The function to call.
     */
    private String _function;
 
    /**
     * The URL location of the API to call.
     */
    private String _apiLocation;
 
    /**
     * The prefix for the result properties.
     */
    private String _prefix;
 
    /**
     * The parameters to the function.
     */
    private Vector _params = new Vector();
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Sets the name of the API function to call
     *
     * @param function
     *    the name of the function to call, can be <code>null</code>.
     */
    public void setFunction(String function) {
       _function = function;
    }
 
    /**
     * Sets the URL location of the API.
     *
    * @param apiLocation
     *    the URL location of the API without any parameters, cannot be <code>null</code>.
     */
    public void setApiLocation(String apiLocation) {
       _apiLocation = apiLocation;
    }
 
    /**
     * Sets the prefix for the result properties.
     *
     * @param prefix
     *    the prefix for the result properties.
     */
    public void setPrefix(String prefix) {
       _prefix = prefix;
    }
 
    public Property createParam() {
       Property property = new Property();
       _params.add(property);
       return property;
    }
 
    /**
     * Called by the project to let the task do its work.
     *
     * @throws BuildException
     *    if something goes wrong with the build.
     */
    public void execute() throws BuildException {
       checkAttributes();
       try {
 
          // Create the URL
          String requestURL = _apiLocation + "?_convention=_xins-std&_function=" + _function;
          for (int i = 0; i < _params.size(); i++) {
              Property nextParam = (Property) _params.elementAt(i);
              String paramName = nextParam.getName();
              String paramValue = nextParam.getValue();
              requestURL += "&" + URLEncoding.encode(paramName) + "=" + URLEncoding.encode(paramValue);
          }
 
          // Call the API
          Element resultXML = CreateExampleTask.getResultAsXML(requestURL);
          log("result: " + resultXML.toString(), Project.MSG_VERBOSE);
 
          // Put the output parameters in ANT properties
          List outputParams = resultXML.getChildElements("param");
          Iterator itOutputParams = outputParams.iterator();
          while (itOutputParams.hasNext()) {
             Element nextParam = (Element) itOutputParams.next();
             String paramName = nextParam.getAttribute("name");
             String paramValue = nextParam.getText();
             if (_prefix != null && _prefix.length() > 0) {
                 paramName = _prefix + "." + paramName;
             }
             System.err.println("param " + paramName + ";" + paramValue);
             getProject().setNewProperty(paramName, paramValue);
          }
 
          // Put the output data section in ANT properties
          // With Ant 1.6 XmlProperty task cannot be used.
          if (resultXML.getChildElements("data").size() > 0) {
              Element dataSection = resultXML.getUniqueChildElement("data");
              elementToProperties(dataSection, _prefix);
          }
       } catch (Exception ex) {
           ex.printStackTrace();
          throw new BuildException(ex);
       }
    }
 
    /**
     * Store the element values in Ant properties.
     *
     * @param element
     *    the data section element, cannot be <code>null</code>.
     * @param prefix
     *    the prefix for the Ant properties, can be <code>null</code>.
     */
    private void elementToProperties(Element element, String prefix) {
       String localName = element.getLocalName();
       String elementPrefix = null;
       if (prefix != null && prefix.length() > 0) {
          elementPrefix = prefix + "." + localName;
       } else {
          elementPrefix = localName;
       }
       Map attributes = element.getAttributeMap();
       Iterator itAttributes = attributes.entrySet().iterator();
       while (itAttributes.hasNext()) {
          Map.Entry nextAttr = (Map.Entry) itAttributes.next();
          String attributeName = ((Element.QualifiedName) nextAttr.getKey()).getLocalName();
          String attributeValue = (String) nextAttr.getValue();
          String propertyName = elementPrefix + "." + attributeName;
          getProject().setNewProperty(propertyName, attributeValue);
       }
       String pcdata = element.getText();
       if (pcdata != null && pcdata.length() > 0) {
          getProject().setNewProperty(elementPrefix, pcdata);
       }
       Iterator children = element.getChildElements().iterator();
       while (children.hasNext()) {
          Element nextChild = (Element) children.next();
          elementToProperties(nextChild, elementPrefix);
       }
    }
 
    /**
     * Checks the attributes of the task.
     *
     * @throws BuildException
     *    if a required attribute is missing.
     */
    private void checkAttributes() throws BuildException {
 
       if (_function == null) {
          throw new BuildException("The \"function\" attribute needs to be specified.");
       }
 
       if (_apiLocation == null) {
          throw new BuildException("An \"exampleProperty\" attribute needs to be specified.");
       }
    }
 }
