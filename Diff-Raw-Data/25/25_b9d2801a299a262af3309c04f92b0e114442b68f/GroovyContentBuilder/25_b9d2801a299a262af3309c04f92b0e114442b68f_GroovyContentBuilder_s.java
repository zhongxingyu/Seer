 /***************************************************************
  *  This file is part of the [fleXive](R) project.
  *
  *  Copyright (c) 1999-2007
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU General Public
  *  License as published by the Free Software Foundation;
  *  either version 2 of the License, or (at your option) any
  *  later version.
  *
  *  The GNU General Public License can be found at
  *  http://www.gnu.org/copyleft/gpl.html.
  *  A copy is found in the textfile GPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 package com.flexive.shared.scripting.groovy;
 
 import com.flexive.shared.CacheAdmin;
 import com.flexive.shared.EJBLookup;
 import com.flexive.shared.content.FxContent;
 import com.flexive.shared.content.FxPK;
 import com.flexive.shared.exceptions.FxApplicationException;
 import com.flexive.shared.interfaces.ContentEngine;
 import com.flexive.shared.structure.FxEnvironment;
 import com.flexive.shared.structure.FxPropertyAssignment;
 import com.flexive.shared.value.FxValue;
 import groovy.util.BuilderSupport;
 
 import java.util.Map;
 
 /**
  * <p>
  * A groovy builder for FxContent instances.
  * </p>
  * <p>Example:
  * <pre>
  * def builder = new GroovyContentBuilder("DOCUMENT")
  * builder {
  *  title("Test article")
  *  Abstract("My abstract text")
  *  teaser {
  *      teaser_title("Teaser title")
  *      teaser_text("Teaser text")
  *  }
  *  box {
  *      box_title(new FxString(false, "Box title 1"))
  *  }
  *  box {
  *      box_title("Box title 2")
  *      box_text("Some box text")
  *  }
  * }
  * </pre>
  * </p>
  * @author Daniel Lichtenberger (daniel.lichtenberger@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @version $Rev$
  */
 public class GroovyContentBuilder extends BuilderSupport {
     private class NodeInfo {
         String xpath;
         final Object value;
 
         public NodeInfo(String xpath, Object value) {
             if (value == null && !xpath.endsWith("]")) {
                 // group node
                 String path;
                 try {
                     path = xpath + "[" + (content.getGroupData(xpath).getOccurances() + 1) + "]";
                 } catch (FxApplicationException e) {
                     path = xpath + "[1]"; // first group
                 }
                 this.xpath = path;
             } else {
                 this.xpath = xpath;
             }
             this.value = value;
         }
 
         public void addParentXPath(String parentXPath) {
             xpath = parentXPath + xpath;
         }
 
         @SuppressWarnings({"unchecked"})
         public FxValue getValue() {
             if (value instanceof FxValue) {
                 return (FxValue) value;
             } else {
                 final FxValue fxValue = getPropertyAssignment(xpath).getEmptyValue();
                 fxValue.setDefaultTranslation(fxValue.fromString(value.toString()));
                 return fxValue;
             }
         }
     }
 
     private final FxContent content;
     private final FxEnvironment environment = CacheAdmin.getEnvironment();
 
     /**
      * Create a new content builder that operates on the given content instance.
      *
      * @param content   the target content
      */
     public GroovyContentBuilder(FxContent content) {
         this.content = content;
     }
 
     /**
      * Create an empty content builder for the given type.
      *
      * @param typeName  the content type name
      * @throws com.flexive.shared.exceptions.FxApplicationException if the content could not be initialized
      * by the content engine
      */
     public GroovyContentBuilder(String typeName) throws FxApplicationException {
         this.content = EJBLookup.getContentEngine().initialize(typeName);
     }
 
     /**
      * Create a content builder for the given instance.
      *
      * @param pk    the object id (the content will be loaded through the content engine)
      * @throws com.flexive.shared.exceptions.FxApplicationException if the content could not be loaded
      */
     public GroovyContentBuilder(FxPK pk) throws FxApplicationException {
         this.content = EJBLookup.getContentEngine().load(pk);
     }
 
     /**
      * Return our content instance.
      *
      * @return  our content instance.
      */
     public FxContent getContent() {
         return content;
     }
 
     /**
      * {@inheritDoc}
      */
     protected void setParent(Object parent, Object child) {
         try {
             // if parent is a node info, use its xpath as prefix
             final NodeInfo nodeInfo = (NodeInfo) child;
             // add parent xpath to our node
             nodeInfo.addParentXPath(parent instanceof NodeInfo ? ((NodeInfo) parent).xpath : "");
             if (nodeInfo.value != null) {
                 content.setValue(nodeInfo.xpath, nodeInfo.getValue());
             }
         } catch (FxApplicationException e) {
             throw e.asRuntimeException();
         }
     }
 
     /**
      * {@inheritDoc}
      */
     protected Object createNode(Object name) {
        return "doCall".equals(name) ? name : new NodeInfo(createXPath(name), null);  
     }
 
     /**
      * {@inheritDoc}
      */
     @SuppressWarnings({"unchecked"})
     protected Object createNode(Object name, Object value) {
         final String xpath = createXPath(name);
         return new NodeInfo(xpath, value);
     }
 
     /**
      * {@inheritDoc}
      */
     protected Object createNode(Object name, Map attributes) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     /**
      * {@inheritDoc}
      */
     protected Object createNode(Object name, Map attributes, Object value) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     private String createXPath(Object name) {
         return (name instanceof String && !((String) name).startsWith("/") ? "/" + name : name.toString()).toUpperCase();
     }
 
     private FxPropertyAssignment getPropertyAssignment(String xpath) {
         return (FxPropertyAssignment) environment.getAssignment(
                 environment.getType(content.getTypeId()).getName() + xpath);
     }
 }
 
