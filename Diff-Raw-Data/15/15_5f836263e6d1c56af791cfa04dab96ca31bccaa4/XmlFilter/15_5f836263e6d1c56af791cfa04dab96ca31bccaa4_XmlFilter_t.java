 /*
  * CDDL HEADER START
  *
  * The contents of this file are subject to the terms of the
  * Common Development and Distribution License, Version 1.0 only
  * (the "License").  You may not use this file except in compliance
  * with the License.
  *
  * You can obtain a copy of the license at license/ESCIDOC.LICENSE
  * or http://www.escidoc.de/license.
  * See the License for the specific language governing permissions
  * and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL HEADER in each
  * file and include the License file at license/ESCIDOC.LICENSE.
  * If applicable, add the following below this CDDL HEADER, with the
  * fields enclosed by brackets "[]" replaced with your own identifying
  * information: Portions Copyright [yyyy] [name of copyright owner]
  *
  * CDDL HEADER END
  */
 
 /*
  * Copyright 2006-2009 Fachinformationszentrum Karlsruhe Gesellschaft
  * fuer wissenschaftlich-technische Information mbH and Max-Planck-
  * Gesellschaft zur Foerderung der Wissenschaft e.V.
  * All rights reserved.  Use is subject to license terms.
  */
 package de.escidoc.core.common.business.fedora.resources;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 import de.escidoc.core.common.business.fedora.TripleStoreUtility;
 import de.escidoc.core.common.exceptions.application.invalid.XmlCorruptedException;
 import de.escidoc.core.common.exceptions.application.invalid.XmlSchemaValidationException;
 import de.escidoc.core.common.util.logger.AppLogger;
 import de.escidoc.core.common.util.xml.XmlUtility;
 
 /**
  * This class parses an XML filter and translates it into a SQL WHERE clause.
  * The filter criteria are also available as key/value pairs in the map this
  * class is derived from. The keys are the names of the corresponding DB table
  * columns. The values are lists to handle multiple values.
  * 
  * @author SCHE
  */
 public class XmlFilter extends AbstractFilter {
     private static final long serialVersionUID = -5828449736500188960L;
 
     /**
      * Logging goes there.
      */
     private static AppLogger logger = new AppLogger(XmlFilter.class.getName());
 
     /**
      * Empty constructor.
      */
     public XmlFilter() {
         initLimit();
     }
 
     /**
      * Parse an XML filter given as a string or file.
      * 
      * @param filter
      *            XML filter
      * @throws XmlCorruptedException
      *             Thrown if provided data is corrupted.
      * @throws XmlSchemaValidationException
      *             Thrown if the schema validation of the provided data fails.
      */
     public XmlFilter(final Object filter) throws XmlCorruptedException,
         XmlSchemaValidationException {
         initLimit();
         parse(filter);
     }
 
     /**
      * Parse an XML filter given as a file.
      * 
      * @param filter
      *            XML filter
      * @throws Exception
      *             the XML could not be parsed.
      */
     public XmlFilter(final File filter) throws Exception {
         initLimit();
         parse(filter);
     }
 
     // CHECKSTYLE:JAVADOC-OFF
 
     /**************************************************************************
      * start implementation of FilterInterface interface
      *************************************************************************/
 
     /**
      * See Interface for functional description.
      * 
      * @return
      * @see de.escidoc.core.common.business.fedora.resources.interfaces
      *      .FilterInterface#toSqlString()
      */
     public String toSqlString() {
         StringBuffer result = new StringBuffer();
         StringBuffer whereClause = new StringBuffer();
         int andIndex = 0;
 
         for (String name : keySet()) {
             andIndex++;
             if (whereClause.length() > 0) {
                 whereClause.append(" AND ");
             }
             whereClause
                 .append("r.id IN (SELECT resource_id FROM list.property WHERE ");
 
             List<Object> filterList = (List<Object>) get(name);
 
             for (int orIndex = 0; orIndex < filterList.size(); orIndex++) {
                 if (orIndex > 0) {
                     whereClause.append(" OR ");
                 }
                 whereClause.append("local_path='");
                 whereClause.append(name);
                 whereClause.append("' AND ");
                 whereClause.append("value=");
                 whereClause.append('\'');
                 whereClause.append(filterList
                     .get(orIndex).toString().toLowerCase());
                 whereClause.append('\'');
             }
             whereClause.append(")");
         }
 
         if (whereClause.length() > 0) {
             if (result.length() > 0) {
                 result.append(',');
             }
             result.append(whereClause);
         }
 
         // add filter for top level objects
         if (getTopLevelOnly()) {
             if (result.length() > 0) {
                 result.append(" AND ");
             }
             if (objectType != null) {
                 if (objectType == ResourceType.CONTAINER) {
                    result.append("r.id NOT IN (SELECT value");
                     result.append(" FROM list.property");
                     result
                         .append(" WHERE local_path='/struct-map/container/id')");
                 }
                 else if (objectType == ResourceType.ITEM) {
                     result.append("r.id NOT IN (SELECT value");
                     result.append(" FROM list.property");
                     result.append(" WHERE local_path='/struct-map/item/id')");
                 }
                 else if (objectType == ResourceType.OU) {
                     result.append("r.id NOT IN (SELECT resource_id");
                     result.append(" FROM list.property");
                     result.append(" WHERE local_path='/parents/parent/id')");
                 }
                 else {
                     result.append("TRUE");
                 }
             }
             else {
                 result
                    .append("r.id NOT IN (SELECT value FROM list.property");
                 result
                     .append(" WHERE local_path='/struct-map/container/id' UNION");
                 result.append(" SELECT value FROM list.property");
                 result.append(" WHERE local_path='/struct-map/item/id' UNION");
                 result.append(" SELECT resource_id FROM list.property WHERE");
                 result.append(" local_path='/parents/parent/id')");
             }
         }
 
         // get all parents for a given child
         if (getMember() != null) {
             if (result.length() > 0) {
                 result.append(" AND ");
             }
             if (objectType != null) {
                 if (objectType == ResourceType.CONTAINER) {
                     result.append("r.id IN (SELECT resource_id");
                     result.append(" FROM list.property");
                     result
                         .append(" WHERE local_path='/struct-map/container/id'");
                     result.append(" AND value='");
                     result.append(getMember());
                     result.append("')");
                 }
                 else if (objectType == ResourceType.ITEM) {
                     result.append("r.id IN (SELECT resource_id");
                     result.append(" FROM list.property");
                     result.append(" WHERE local_path='/struct-map/item/id'");
                     result.append(" AND value='");
                     result.append(getMember());
                     result.append("')");
                 }
                 else if (objectType == ResourceType.OU) {
                     result.append("r.id IN (SELECT value");
                     result.append(" FROM list.property");
                     result.append(" WHERE resource_id='");
                     result.append(getMember());
                     result.append("' AND local_path='/parents/parent/id')");
                 }
                 else {
                     result.append("TRUE");
                 }
             }
             else {
                 result.append("r.id IN (SELECT resource_id FROM list.property");
                 result.append(" WHERE (local_path='/struct-map/item/id' OR "
                     + "local_path='/struct-map/container/id') AND value='");
                 result.append(getMember());
                 result.append("' UNION SELECT value FROM list.property WHERE");
                 result.append(" resource_id='");
                 result.append(getMember());
                 result.append("' AND local_path='/parents/parent/id')");
             }
         }
 
         // get all children for a given parent
         if (getParent() != null) {
             if (result.length() > 0) {
                 result.append(" AND ");
             }
             if (objectType != null) {
                 if (objectType == ResourceType.CONTAINER) {
                     result.append("r.id IN (SELECT value");
                     result.append(" FROM list.property");
                     result.append(" WHERE resource_id='");
                     result.append(getParent());
                     result
                         .append("' AND local_path='/struct-map/container/id')");
                 }
                 else if (objectType == ResourceType.ITEM) {
                     result.append("r.id IN (SELECT value");
                     result.append(" FROM list.property");
                     result.append(" WHERE resource_id='");
                     result.append(getParent());
                     result.append("' AND local_path='/struct-map/item/id')");
                 }
                 else if (objectType == ResourceType.OU) {
                     result.append("r.id IN (SELECT resource_id");
                     result.append(" FROM list.property");
                     result.append(" WHERE local_path='/parents/parent/id'");
                     result.append(" AND value='");
                     result.append(getParent());
                     result.append("')");
                 }
                 else {
                     result.append("TRUE");
                 }
             }
             else {
                 result
                     .append("r.id IN (SELECT resource_id FROM list.property WHERE");
                 result.append(" local_path='/parents/parent/id' AND value='");
                 result.append(getParent());
                 result.append("' UNION SELECT value FROM list.property");
                 result.append(" WHERE resource_id='");
                 result.append(getParent());
                 result.append("' AND (local_path='/struct-map/item/id' OR "
                     + "local_path='/struct-map/container/id'))");
             }
         }
         return result.toString();
     }
 
     /**************************************************************************
      * end implementation of FilterInterface interface
      *************************************************************************/
 
     // CHECKSTYLE:JAVADOC-ON
 
     /**
      * Call the SAX parser and handle errors during parsing.
      * 
      * @param filter
      *            XML filter as string or file
      * 
      * @throws XmlCorruptedException
      *             Thrown if provided data is corrupted.
      * @throws XmlSchemaValidationException
      *             Thrown if the schema validation of the provided data fails.
      */
     private void parse(final Object filter) throws XmlCorruptedException,
         XmlSchemaValidationException {
         try {
             SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
 
             if (filter instanceof String) {
                 logger.info("filter XML: " + filter);
                 parser.parse(new ByteArrayInputStream(((String) filter)
                     .getBytes(XmlUtility.CHARACTER_ENCODING)),
                     new FilterDefaultHandler());
             }
             else if (filter instanceof File) {
                 parser.parse((File) filter, new FilterDefaultHandler());
             }
         }
         catch (IOException e) {
             throw new XmlCorruptedException(e);
         }
         catch (ParserConfigurationException e) {
             throw new XmlCorruptedException(e);
         }
         catch (SAXException e) {
             throw new XmlCorruptedException(e);
         }
     }
 
     /**
      * SAX event handler to parse the XML filter.
      * 
      * @author SCHE
      */
     private class FilterDefaultHandler extends DefaultHandler {
         private String name = null;
 
         private boolean format = false;
 
         private boolean id = false;
 
         private boolean limit = false;
 
         private boolean offset = false;
 
         private boolean orderBy = false;
 
         private String direction = DIRECTION_ASCENDING;
 
         private StringBuffer content = new StringBuffer();
 
         /**
          * Receive notification of character data inside an element.
          * 
          * @param ch
          *            The whitespace characters.
          * @param start
          *            The start position in the character array.
          * @param length
          *            The number of characters to use from the character array.
          * 
          * @see org.xml.sax.helpers.DefaultHandler#characters(char [], int, int)
          */
         public void characters(
             final char[] ch, final int start, final int length) {
             content.append(new String(ch, start, length));
         }
 
         /**
          * Receive notification of the end of an element.
          * 
          * @param uri
          *            The Namespace URI, or the empty string if the element has
          *            no Namespace URI or if Namespace processing is not being
          *            performed.
          * @param localName
          *            The local name (without prefix), or the empty string if
          *            Namespace processing is not being performed.
          * @param qName
          *            The qualified name (with prefix), or the empty string if
          *            qualified names are not available.
          */
         public void endElement(
             final String uri, final String localName, final String qName) {
             if (name != null) {
                 if (name.equals("role")) {
                     setRoleId(content.toString());
                 }
                 else if (name.equals("user")) {
                     setUserId(content.toString());
                 }
                 else if (name.equals(TripleStoreUtility.PROP_OBJECT_TYPE)) {
                     setObjectType(ResourceType.getResourceTypeFromUri(content
                         .toString()));
                 }
                 else if (name.equals(TripleStoreUtility.PROP_PARENT)) {
                     setParent(XmlUtility.getIdFromURI(content.toString()));
                 }
                 else {
                     addRestriction(name, content.toString());
                 }
                 name = null;
             }
             if (format) {
                 setFormat(content.toString());
                 format = false;
             }
             if (id) {
                 addRestriction(TripleStoreUtility.PROP_DC_IDENTIFIER, content
                     .toString());
                 id = false;
             }
             if (limit) {
                 setLimit(Integer.parseInt(content.toString()));
                 limit = false;
             }
             if (offset) {
                 setOffset(Integer.parseInt(content.toString()));
                 offset = false;
             }
             if (orderBy) {
                 String value = content.toString();
                 String criteria = PROPERTY_MAP.get(value);
 
                 if (criteria == null) {
                     // ORDER BY given in local path format
                     criteria = value;
                 }
                 addOrderBy(criteria, direction);
                 orderBy = false;
                 direction = DIRECTION_ASCENDING;
             }
         }
 
         /**
          * Receive notification of the start of an element.
          * 
          * @param uri
          *            The Namespace URI, or the empty string if the element has
          *            no Namespace URI or if Namespace processing is not being
          *            performed.
          * @param localName
          *            The local name (without prefix), or the empty string if
          *            Namespace processing is not being performed.
          * @param qName
          *            The qualified name (with prefix), or the empty string if
          *            qualified names are not available.
          * @param attributes
          *            The attributes attached to the element. If there are no
          *            attributes, it shall be an empty Attributes object.
          * 
          * @see org.xml.sax.helpers.DefaultHandler#startElement(String, String,
          *      String, Attributes)
          */
         public void startElement(
             final String uri, final String localName, final String qName,
             final Attributes attributes) {
             content.setLength(0);
             if (qName.equals("filter")) {
                 if (attributes != null) {
                     for (int index = 0; index < attributes.getLength(); index++) {
                         if (attributes.getQName(index).equals("name")) {
                             name = attributes.getValue(index);
                             if (name.equals("top-level-containers")) {
                                 setObjectType(ResourceType.CONTAINER);
                                 setTopLevelOnly(true);
                                 name = null;
                             }
                             else if (name.equals("top-level-items")) {
                                 setObjectType(ResourceType.ITEM);
                                 setTopLevelOnly(true);
                                 name = null;
                             }
                             else if (name
                                 .equals("top-level-organizational-units")) {
                                 setObjectType(ResourceType.OU);
                                 setTopLevelOnly(true);
                                 name = null;
                             }
                             break;
                         }
                     }
                 }
             }
             else if (qName.equals("format")) {
                 format = true;
             }
             else if (qName.equals("id")) {
                 id = true;
             }
             else if (qName.equals("limit")) {
                 limit = true;
             }
             else if (qName.equals("offset")) {
                 offset = true;
             }
             else if (qName.equals("order-by")) {
                 orderBy = true;
                 if (attributes != null) {
                     for (int index = 0; index < attributes.getLength(); index++) {
                         if (attributes.getQName(index).equals("sorting")) {
                             if (attributes.getValue(index).equals("descending")) {
                                 direction = DIRECTION_DESCENDING;
                             }
                             break;
                         }
                     }
                 }
             }
             else if (!qName.equals("param")) {
                 throw new IllegalArgumentException("unknown element: " + qName);
             }
         }
     }
 }
