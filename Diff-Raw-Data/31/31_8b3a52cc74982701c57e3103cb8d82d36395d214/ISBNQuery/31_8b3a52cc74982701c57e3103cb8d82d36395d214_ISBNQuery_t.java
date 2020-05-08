 /*
 * (C) Copyright 2012 Nuxeo SA (http://nuxeo.com/) and others.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Lesser General Public License
  * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * Contributors:
  *     ldoguin
  */
 
 package org.nuxeo.isbn;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.nuxeo.ecm.automation.core.Constants;
 import org.nuxeo.ecm.automation.core.annotations.Operation;
 import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
 import org.nuxeo.ecm.automation.core.collectors.DocumentModelCollector;
 import org.nuxeo.ecm.core.api.ClientException;
 import org.nuxeo.ecm.core.api.DocumentModel;
 import org.nuxeo.ecm.core.api.model.PropertyException;
 
 import sun.net.www.protocol.http.HttpURLConnection;
 
 /**
  * @author ldoguin
  */
 @Operation(id = ISBNQuery.ID, category = Constants.CAT_DOCUMENT, label = "ISBNQuery", description = "")
 public class ISBNQuery {
 
     public static final String ID = "ISBNQuery";
 
     public static final String QUERY_ISBN_URL = "http://openlibrary.org/api/books?bibkeys=%s&format=json";
 
     @OperationMethod(collector = DocumentModelCollector.class)
     public DocumentModel run(DocumentModel input) throws MalformedURLException,
             PropertyException, ClientException, JSONException {
         String isbn = input.getProperty("isbn:isbn").getValue(String.class);
         isbn = "ISBN:".concat(isbn);
         String query = String.format(QUERY_ISBN_URL, isbn);
         URL url = new URL(query);
         HttpURLConnection urlConnection = null;
         try {
             urlConnection = (HttpURLConnection) url.openConnection();
             BufferedReader in = new BufferedReader(new InputStreamReader(
                     urlConnection.getInputStream()));
             StringBuffer sb = new StringBuffer();
             String inputLine;
             while ((inputLine = in.readLine()) != null) {
                 sb.append(inputLine);
             }
             JSONObject jso = new JSONObject(sb.toString());
             JSONObject metadata = jso.getJSONObject(isbn);
             String bib_key = metadata.getString("bib_key");
             if (bib_key != null) {
                 input.setPropertyValue("isbn:bib_key", bib_key);
             }
             String info_url = metadata.getString("info_url");
             if (bib_key != null) {
                 input.setPropertyValue("isbn:info_url", info_url);
             }
             String preview = metadata.getString("preview");
             if (bib_key != null) {
                 input.setPropertyValue("isbn:preview", preview);
             }
             String preview_url = metadata.getString("preview_url");
             if (bib_key != null) {
                 input.setPropertyValue("isbn:preview_url", preview_url);
             }
             String thumbnail_url = metadata.getString("thumbnail_url");
             if (bib_key != null) {
                 input.setPropertyValue("isbn:thumbnail_url", thumbnail_url);
             }
             in.close();
         } catch (IOException e) {
             throw new RuntimeException(e);
         } finally {
             if (urlConnection != null) {
                 urlConnection.disconnect();
             }
         }
 
         return input;
     }
}
