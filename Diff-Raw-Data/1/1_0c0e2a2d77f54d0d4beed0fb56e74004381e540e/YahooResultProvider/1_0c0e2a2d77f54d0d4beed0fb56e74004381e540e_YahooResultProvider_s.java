 /***************************************************************
  *  This file is part of the [fleXive](R) backend application.
  *
  *  Copyright (c) 1999-2008
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) backend application is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU General Public
  *  License as published by the Free Software Foundation;
  *  either version 2 of the License, or (at your option) any
  *  later version.
  *
  *  The GNU General Public License can be found at
  *  http://www.gnu.org/licenses/gpl.html.
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
 package com.flexive.faces.javascript.yui;
 
 import com.flexive.faces.FxJsfUtils;
 import com.flexive.shared.EJBLookup;
 import com.flexive.shared.CacheAdmin;
 import com.flexive.shared.security.PermissionSet;
 import com.flexive.shared.structure.FxProperty;
 import com.flexive.shared.structure.FxEnvironment;
 import com.flexive.shared.exceptions.FxApplicationException;
 import com.flexive.shared.exceptions.FxRuntimeException;
 import com.flexive.shared.search.*;
 import com.flexive.war.JsonWriter;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.io.StringWriter;
 
 /**
  * Provides map interfaces for generating JSON row and column information
  * from a FxResultSet.
  *
  * @author Daniel Lichtenberger (daniel.lichtenberger@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @version $Rev$
  */
 public class YahooResultProvider implements Serializable {
     private static final long serialVersionUID = -4200104398592163875L;
     private static final Log LOG = LogFactory.getLog(YahooResultProvider.class);
 
     /**
      * Returns a complete JSON representation of the given search result.
      *
      * @param result        the search result to be rendered
      * @param firstColumn   index of the first column to be rendered (1-based)
      * @return the search result in JSON notation
      * @throws java.io.IOException  if the JSON output could not be written
      */
     public static String getSearchResultJSON(FxResultSet result, int firstColumn) throws IOException {
         final StringWriter out = new StringWriter();
         final JsonWriter writer = new JsonWriter(out);
         writer.startMap();
 
         writer.writeAttribute("columnCount", result.getColumnCount());
         writer.writeAttribute("totalRowCount", result.getTotalRowCount());
         writer.writeAttribute("totalTime", result.getTotalTime());
         writer.writeAttribute("rowCount", result.getRowCount());
         writer.writeAttribute("startIndex", result.getStartIndex());
         writer.writeAttribute("viewType", result.getViewType());
         writer.writeAttribute("resultLocation", result.getLocation());
         createResultColumns(result, writer, firstColumn);
         createResultRows(result, writer, firstColumn);
         createResponseSchema(result, writer, firstColumn);
 
         writer.closeMap();
         writer.finishResponse();
 
         return out.toString();
     }
 
     public String getResultRows(String query, int firstColumn) throws FxApplicationException, IOException {
         final StringWriter out = new StringWriter();
         final FxResultSet result = EJBLookup.getSearchEngine().search(query, 0, Integer.MAX_VALUE, null);
         final JsonWriter writer = new JsonWriter(out);
         writer.startMap();
         createResultRows(result, writer, firstColumn);
         writer.closeMap();
         writer.finishResponse();
         return out.toString();
     }
 
     private static void createResultColumns(FxResultSet result, JsonWriter writer, int firstColumn) throws IOException {
         writer.startAttribute("columns");
         writer.startArray();
         for (int i = firstColumn; i <= result.getColumnCount(); i++) {
             writer.startMap();
             writer.writeAttribute("key", getColumnKey(i));
             writer.writeAttribute("label", result.getColumnLabel(i));
             writer.writeAttribute("sortable", true);
             writer.closeMap();
         }
         writer.closeArray();
     }
 
     private static void createResultRows(FxResultSet result, JsonWriter writer, int firstColumn) throws IOException {
         writer.startAttribute("rows");
         writer.startArray();
         for (FxResultRow row : result.getResultRows()) {
             writer.startMap();
             for (int i = firstColumn; i <= result.getColumnCount(); i++) {
                 writer.writeAttribute(getColumnKey(i),
                         FxJsfUtils.formatResultValue(row.getValue(i), null, null, null));
             }
             if (result.getColumnIndex("@pk") != -1) {
                 // add special PK column
                 writer.writeAttribute("pk", row.getPk("@pk"));
             }
             if (result.getColumnIndex("@permissions") != -1) {
                 // add permissions object
                 final PermissionSet permissions = row.getPermissions(result.getColumnIndex("@permissions"));
                 int perms = 0;
                 perms |= (permissions.isMayRead() ? 1 : 0);         // read
                 perms |= (permissions.isMayCreate() ? 1 : 0) << 1;  // create
                 perms |= (permissions.isMayDelete() ? 1 : 0) << 2;  // delete
                 perms |= (permissions.isMayEdit() ? 1 : 0)   << 3;  // edit
                 perms |= (permissions.isMayExport() ? 1 : 0) << 4;  // export
                 perms |= (permissions.isMayRelate() ? 1 : 0) << 5;  // relate
                 writer.writeAttribute("permissions", perms);
             }
             writer.closeMap();
         }
         writer.closeArray();
     }
 
     /**
      * Renders the response schema needed for the YUI datatable.
      *
      * @param result    the result to be written
      * @param writer    the JSON output writer
      * @param firstColumn   the first column to be included (1-based)
      * @throws java.io.IOException  on output errors
      */
     private static void createResponseSchema(FxResultSet result, JsonWriter writer, int firstColumn) throws IOException {
         writer.startAttribute("responseSchema");
         writer.startMap();
         writer.startAttribute("fields");
         writer.startArray();
         final FxEnvironment environment = CacheAdmin.getEnvironment();
         for (int i = firstColumn; i <= result.getColumnCount(); i++) {
             writer.startMap();
             writer.writeAttribute("key", getColumnKey(i));
             String parser = "YAHOO.util.DataSource.parseString";    // the YUI data parser used for sorting
             try {
                 final FxProperty property = environment.getProperty(result.getColumnName(i));
                 if (property.getEmptyValue().getDefaultTranslation() instanceof Number) {
                     parser = "YAHOO.util.DataSource.parseNumber";
                 }
             } catch (FxRuntimeException e) {
                 // property not found, use default
             }
             writer.writeAttribute("parser", parser, false);
             writer.closeMap();
         }
         // include primary key in attribute pk, if available
         if (result.getColumnIndex("@pk") != -1) {
             writer.startMap().writeAttribute("key", "pk").closeMap();
         }
         if (result.getColumnIndex("@permissions") != -1) {
             writer.startMap().writeAttribute("key", "permissions").closeMap();
         }
         writer.closeArray();
         writer.closeMap();
     }
 
     private static String getColumnKey(int index) {
         return "c" + index;
     }
 
     /**
      * Writes the current type counts for each found content type.
      *
      * @param writer      the target writer
      * @param result      the search result
      * @throws java.io.IOException if the output could not be written
      */
     private void writeUpdatedTypeCounts(JsonWriter writer, FxResultSet result) throws IOException {
         writer.startAttribute("typeCounts");
         writer.startMap();
         for (FxFoundType type : result.getContentTypes()) {
             writer.writeAttribute(String.valueOf(type.getContentTypeId()), type.getFoundEntries());
         }
         writer.closeMap();
     }
 
 }
