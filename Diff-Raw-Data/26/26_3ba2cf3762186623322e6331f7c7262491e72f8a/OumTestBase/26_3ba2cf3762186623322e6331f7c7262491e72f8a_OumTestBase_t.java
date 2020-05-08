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
  * Copyright 2006-2008 Fachinformationszentrum Karlsruhe Gesellschaft
  * fuer wissenschaftlich-technische Information mbH and Max-Planck-
  * Gesellschaft zur Foerderung der Wissenschaft e.V.  
  * All rights reserved.  Use is subject to license terms.
  */
 package de.escidoc.core.test.oum;
 
 import java.util.Arrays;
 
import org.apache.commons.collections.ListUtils;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import de.escidoc.core.test.EscidocRestSoapTestBase;
 import de.escidoc.core.test.common.client.servlet.Constants;
 import de.escidoc.core.test.common.client.servlet.oum.OrganizationalUnitClient;
 
 /**
  * Base class for tests of the mock implementation of the OM resources.
  * 
  * @author MSC
  * 
  */
 public class OumTestBase extends EscidocRestSoapTestBase {
 
     public static final String XPATH_PARENTS = "/" + NAME_PARENTS;
 
     public static final String XPATH_PARENT = XPATH_PARENTS + "/" + NAME_PARENT;
 
     public static final String XPATH_MD_RECORD = XPATH_ORGANIZATION_MD_RECORDS
         + "/" + NAME_MD_RECORD;
 
     public static final String XPATH_MD_RECORDS_ESCIDOC_MD_RECORD =
         XPATH_MD_RECORD + "[@name=\"escidoc\"]" + "/" + NAME_OU_MD_RECORD;
 
     private OrganizationalUnitClient ouClient = null;
 
     /**
      * Path of OU name in RDF/XML result of triple store.
      */
     public static final String XPATH_TRIPLE_STORE_OU_NAME =
         "/RDF/Description/name";
 
     public static final String XPATH_TRIPLE_STORE_OU_TITLE =
         "/RDF/Description/title";
 
     public static final String XPATH_TRIPLE_STORE_OU_DESCRIPTION =
         "/RDF/Description/description";
 
     /**
      * @param transport
      *            The transport identifier.
      */
     public OumTestBase(final int transport) {
         super(transport);
         this.ouClient = new OrganizationalUnitClient(transport);
     }
 
     /**
      * @return Returns the itemClient.
      */
     public OrganizationalUnitClient getOrganizationalUnitClient() {
         return ouClient;
     }
 
     /**
      * Extract the objids from the given list of organizational units.
      * 
      * @param organizationalUnitList
      *            The list of organizational units.
      * @return An array containing the extracted ids.
      * @throws Exception
      *             If anything fails.
      */
     protected String[] getIdsFromOrganizationalUnitList(
         final String organizationalUnitList) throws Exception {
         String[] result = null;
         NodeList nodeList = null;
 
         if (getTransport() == Constants.TRANSPORT_REST) {
             nodeList =
                 selectNodeList(
                     EscidocRestSoapTestBase.getDocument(organizationalUnitList),
                     XPATH_SRW_ORGANIZATIONAL_UNIT_LIST_ORGANIZATIONAL_UNIT
                         + "/@href");
         }
         else if (getTransport() == Constants.TRANSPORT_SOAP) {
             nodeList =
                 selectNodeList(
                     EscidocRestSoapTestBase.getDocument(organizationalUnitList),
                     XPATH_SRW_ORGANIZATIONAL_UNIT_LIST_ORGANIZATIONAL_UNIT
                         + "/@objid");
         }
 
         result = new String[nodeList.getLength()];
         for (int i = 0; i < nodeList.getLength(); i++) {
             Node node = nodeList.item(i);
 
             if (getTransport() == Constants.TRANSPORT_REST) {
                 result[i] = getObjidFromHref(node.getNodeValue());
             }
             else {
                 result[i] = node.getNodeValue();
             }
         }
         return result;
     }
 
     /**
      * Checks if the given arrays contain the same elements regardless of their
      * order.
      * 
      * @param array1
      *            The 1st array.
      * @param array2
      *            The 2nd array.
      * 
      * @return true if the check was successful.
      */
     protected boolean compareContent(
        final String[] array1, final String[] array2) {
         boolean result = true;

         if ((array1 == null) || (array2 == null)
             || (array1.length != array2.length)) {
             result = false;
         }
         else {
            result =
                ListUtils
                    .subtract(Arrays.asList(array1), Arrays.asList(array2))
                    .size() == 0;
         }
         return result;
 
     }
 
     /**
      * Extract the objids from the given list of path(s).
      * 
      * @param path
      *            The path.
      * @return An array containing the extracted ids.
      * @throws Exception
      *             If anything fails.
      */
     protected String[][] getIdsFromPathList(final String path) throws Exception {
 
         String[][] result = null;
         Document createdDoc = getDocument(path);
         NodeList pathList =
             selectNodeList(createdDoc,
                 "/organizational-unit-path-list/organizational-unit-path");
         result = new String[pathList.getLength()][];
         if (getTransport() == Constants.TRANSPORT_REST) {
             for (int i = 0; i < pathList.getLength(); i++) {
                 NodeList pathElements =
                     selectNodeList(pathList.item(i),
                         "/organizational-unit-path-list/organizational-unit-path["
                             + (i + 1) + "]/organizational-unit-ref/@href");
                 String[] elements = new String[pathElements.getLength()];
                 for (int j = pathElements.getLength() - 1; j >= 0; j--) {
                     elements[j] =
                         getObjidFromHref(pathElements.item(j).getNodeValue());
                 }
                 result[i] = elements;
             }
         }
         else {
             for (int i = 0; i < pathList.getLength(); i++) {
                 NodeList pathElements =
                     selectNodeList(pathList.item(i),
                         "/organizational-unit-path-list/organizational-unit-path["
                             + (i + 1) + "]/organizational-unit-ref/@objid");
                 String[] elements = new String[pathElements.getLength()];
                 for (int j = pathElements.getLength() - 1; j >= 0; j--) {
                     elements[j] = pathElements.item(j).getNodeValue();
                 }
                 result[i] = elements;
             }
         }
         return result;
     }
 
     /**
      * Checks if the given organizational unit path lists contain the same paths
      * and elements.
      * 
      * @param array1
      *            The 1st path list.
      * @param array2
      *            The 2nd path list.
      * 
      * @return true if the check was successful.
      */
     protected boolean comparePathLists(
         final String[][] array1, final String[][] array2) {
         boolean result = true;
 
         if (array1.length != array2.length) {
             result = false;
         }
         else {
             for (int index = 0; index < array1.length; ++index) {
                if (!compareContent(array1[index], array2[index])) {
                     result = false;
                     break;
                 }
             }
         }
         return result;
     }
 }
