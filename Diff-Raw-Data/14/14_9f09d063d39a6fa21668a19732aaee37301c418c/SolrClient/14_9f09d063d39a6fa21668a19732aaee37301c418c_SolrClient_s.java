 /**
  * Acceso Inteligente
  *
  * Copyright (C) 2010-2011 Fundación Ciudadano Inteligente
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.accesointeligente.server.solr;
 
 import org.accesointeligente.server.ApplicationProperties;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.log4j.Logger;
 import org.htmlcleaner.HtmlCleaner;
 import org.htmlcleaner.TagNode;
import org.mortbay.util.URIUtil;
 
 import java.io.InputStreamReader;
 
 public class SolrClient {
 	private static final Logger logger = Logger.getLogger(SolrClient.class);
 
 	static public void reindex() {
 		HttpClient client = new DefaultHttpClient();
 		HtmlCleaner cleaner = new HtmlCleaner();
 		HttpGet get;
 		HttpResponse response;
 		String httpQuery = "";
 
 		try {
 			httpQuery = ApplicationProperties.getProperty("solr.server.address") + ApplicationProperties.getProperty("solr.core.path") + ApplicationProperties.getProperty("solr.query.command.reindex");
 			get = new HttpGet(httpQuery);
 			response = client.execute(get);
 			cleaner.clean(new InputStreamReader(response.getEntity().getContent()));
 		} catch (Exception ex) {
 			logger.error(httpQuery);
 			logger.error(ex.getMessage(), ex);
 		}
 	}
 
 	static public void reloadConfig() {
 		HttpClient client = new DefaultHttpClient();
 		HtmlCleaner cleaner = new HtmlCleaner();
 		HttpGet get;
 		HttpResponse response;
 		String httpQuery = "";
 
 		try {
 			httpQuery = ApplicationProperties.getProperty("solr.server.address") + ApplicationProperties.getProperty("solr.core.path") + ApplicationProperties.getProperty("solr.query.command.reloadConfig");
 			get = new HttpGet(httpQuery);
 			response = client.execute(get);
 			cleaner.clean(new InputStreamReader(response.getEntity().getContent()));
 		} catch (Exception ex) {
 			logger.error(httpQuery);
 			logger.error(ex.getMessage(), ex);
 		}
 	}
 
 	static public String execQuery(String query) {
 		HttpClient client = new DefaultHttpClient();
 		HtmlCleaner cleaner = new HtmlCleaner();
 		HttpGet get;
 		HttpResponse response;
 		TagNode document;
 		String jsonResponse = null;
 		String httpQuery = "";
 
 		try {
 			httpQuery = ApplicationProperties.getProperty("solr.server.address") + ApplicationProperties.getProperty("solr.core.path") + ApplicationProperties.getProperty("solr.query.path");
			httpQuery += URIUtil.encodePath(query);
 			get = new HttpGet(httpQuery);
 			response = client.execute(get);
 			document = cleaner.clean(new InputStreamReader(response.getEntity().getContent()));
 			jsonResponse = document.getText().toString();
 		} catch (Exception ex) {
 			logger.error(httpQuery);
 			logger.error(ex.getMessage(), ex);
 		}
 
 		return jsonResponse;
 	}
 }
