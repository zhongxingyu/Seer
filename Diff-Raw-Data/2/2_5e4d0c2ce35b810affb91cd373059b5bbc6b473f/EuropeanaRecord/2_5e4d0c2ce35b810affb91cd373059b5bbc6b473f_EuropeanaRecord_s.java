 /*
  *  Copyright 2011 Diego Ceccarelli
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *
  */
 package eu.europeana.querylog;
 
 import it.cnr.isti.hpc.io.reader.RecordParser;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Locale;
 import java.util.Scanner;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.gson.Gson;
 
 import eu.europeana.querylog.clean.TabCleaner;
 
 /**
  * Represents a record of an Europeana query log
  * 
  * @author Diego Ceccarelli <diego.ceccarelli@isti.cnr.it>
  * @since 28/nov/2010
  */
 public class EuropeanaRecord {
 
 	private static final Logger logger = LoggerFactory
 			.getLogger(EuropeanaRecord.class);
 
 	String action;
 	String view;
 	String userId;
 	String lang;
 	String req;
 	Date date;
 	String ip;
 	String userAgent;
 	String referrer;
 	String utma;
 	String utmb;
 	String utmc;
 	String version;
 	String europeanaUri;
 
 	String query;
 	String queryType;
 
 	String normalizedQuery;
 
 	String queryContraints;
 	Integer numFound;
 	Integer page;
 	Integer start;
 	TabCleaner tabCleaner = new TabCleaner();
 
 	private static QueryCleaner cleaner = QueryCleaner
 			.getStandardQueryCleaner();
 
 	public EuropeanaRecord() {
 
 	}
 
 	public String getNormalizedQuery() {
 		return normalizedQuery;
 	}
 
 	public void setNormalizedQuery(String normalizedQuery) {
 		this.normalizedQuery = normalizedQuery;
 	}
 
 	public String getAction() {
 		return action;
 	}
 
 	public void setAction(String action) {
 		this.action = action;
 	}
 
 	public String getView() {
 		return view;
 	}
 
 	public String toTsv() {
 		StringBuilder sb = new StringBuilder();
 		sb.append(date.getTime()).append("\t");
 		sb.append(userId).append("\t");
 		sb.append(tabCleaner.clean(query)).append("\t");
 		sb.append(normalizedQuery).append("\t");
 		sb.append(action).append("\t");
 		sb.append(europeanaUri).append("\t");
 		sb.append((page == null) ? "null" : page).append("\t");
 		sb.append((start == null) ? "null" : start).append("\t");
 
 		sb.append((userAgent == null) ? "null" : userAgent);
 
 		return sb.toString();
 
 	}
 
 	public void normalizeQuery() {
 		if (query == null || query.isEmpty()) {
 			return;
 		}
 		normalizedQuery = cleaner.clean(query);
 
 	}
 
 	public void setView(String view) {
 		this.view = view;
 	}
 
 	public String getUserId() {
 		return userId;
 	}
 
 	public void setUserId(String userId) {
 		this.userId = userId;
 	}
 
 	public String getLang() {
 		return lang;
 	}
 
 	public void setLang(String lang) {
 		this.lang = lang;
 	}
 
 	public String getReq() {
 		return req;
 	}
 
 	public void setReq(String req) {
 		this.req = req;
 	}
 
 	public Date getDate() {
 		return date;
 	}
 
 	public void setDate(Date date) {
 		this.date = date;
 	}
 
 	public String getIp() {
 		return ip;
 	}
 
 	public void setIp(String ip) {
 		this.ip = ip;
 	}
 
 	public String getUserAgent() {
 		return userAgent;
 	}
 
 	public void setUserAgent(String userAgent) {
 		this.userAgent = userAgent;
 	}
 
 	public String getReferrer() {
 		return referrer;
 	}
 
 	public void setReferrer(String referrer) {
 		this.referrer = referrer;
 	}
 
 	public String getUtma() {
 		return utma;
 	}
 
 	public void setUtma(String utma) {
 		this.utma = utma;
 	}
 
 	public String getUtmb() {
 		return utmb;
 	}
 
 	public void setUtmb(String utmb) {
 		this.utmb = utmb;
 	}
 
 	public String getUtmc() {
 		return utmc;
 	}
 
 	public void setUtmc(String utmc) {
 		this.utmc = utmc;
 	}
 
 	public String getVersion() {
 		return version;
 	}
 
 	public void setVersion(String version) {
 		this.version = version;
 	}
 
 	public String getQuery() {
 		return query;
 	}
 
 	public void setQuery(String query) {
 		this.query = query;
 	}
 
 	public String getQueryType() {
 		return queryType;
 	}
 
 	public void setQueryType(String queryType) {
 		this.queryType = queryType;
 	}
 
 	public int getNumFound() {
 		return numFound;
 	}
 
 	public void setNumFound(int numFound) {
 		this.numFound = numFound;
 	}
 
 	public int getPage() {
 		return page;
 	}
 
 	public void setPage(int page) {
 		this.page = page;
 	}
 
 	public String getQueryContraints() {
 		return queryContraints;
 	}
 
 	public void setQueryContraints(String queryContraint) {
 		this.queryContraints = queryContraint;
 	}
 
 	public String getEuropeanaUri() {
 		return europeanaUri;
 	}
 
 	public void setEuropeanaUri(String europeanaUri) {
 		this.europeanaUri = europeanaUri;
 	}
 
 	public int getStart() {
 		return start;
 	}
 
 	public void setStart(int start) {
 		this.start = start;
 	}
 
 	private static long hash(String string) {
 		long h = 1125899906842597L; // prime
 		int len = string.length();
 
 		for (int i = 0; i < len; i++) {
 			h = 31 * h + string.charAt(i);
 		}
 		return h;
 	}
 
 	private static String hashStr(String string) {
 		return Long.toHexString(hash(string));
 	}
 
 	public static class Parser implements RecordParser<EuropeanaRecord> {
 		private final DateFormat df = new SimpleDateFormat(
				"yyyy-MM-dd'T'HH:mm:ss.SSSZZZ", Locale.UK);
 
 		private final Gson gson = new Gson();
 
 		@Override
 		public EuropeanaRecord decode(String record) {
 			// record = record.replaceAll("\\([0-9]+\\),\\([0-9]+\\)", "\1\2");
 			// System.out.println("record -> " + record);
 			int start = record.indexOf('[');
 			int end = record.lastIndexOf(']');
 			if (start < 0 || end < 0) {
 				// FIXME
 				return null;
 			}
 
 			EuropeanaRecord rec = new EuropeanaRecord();
 			String fields = record.substring(start + 1, end);
 			Scanner scanner = new Scanner(fields).useDelimiter(",");
 			while (scanner.hasNext()) {
 				String next = scanner.next();
 				int pos = next.indexOf('=');
 				if (pos < 0) {
 					logger.error(
 							"invalid subfield {}, does not contain value ",
 							next);
 					continue;
 				}
 				String field = next.substring(0, pos).trim();
 				String value = next.substring(pos + 1).trim();
 				if (value.equals("null") || value.isEmpty()
 						|| value.equals("\"\"")) {
 					// skip null values
 					continue;
 				}
 				setField(rec, field, value);
 			}
 			if (rec != null)
 				rec.generateUserId();
 			return rec;
 
 		}
 
 		private void setField(EuropeanaRecord rec, String field, String value) {
 
 			if (field.equals("action")) {
 				rec.setAction(value);
 				return;
 			}
 			if (field.equals("view")) {
 				rec.setView(value);
 				return;
 			}
 			if (field.equals("userId")) {
 				rec.setUserId(value);
 				return;
 			}
 			if (field.equals("lang")) {
 				rec.setLang(value);
 				return;
 			}
 			if (field.equals("req")) {
 				rec.setReq(value);
 				return;
 			}
 			if (field.equals("date")) {
 				Date time = null;
 				try {
 					time = df.parse(value);
 				} catch (ParseException e) {
 					logger.error("parsing the date of the query log ("
 							+ e.toString() + ")");
 				}
 				rec.setDate(time);
 				return;
 
 			}
 			if (field.equals("ip")) {
 				rec.setIp(value);
 				return;
 			}
 			if (field.equals("user-agent")) {
 				rec.setUserAgent(value);
 
 				return;
 			}
 			if (field.equals("referer")) {
 				rec.setReferrer(value);
 				return;
 			}
 
 			if (field.equals("utma")) {
 				rec.setUtma(value);
 				return;
 			}
 			if (field.equals("utmb")) {
 				rec.setUtmb(value);
 				return;
 			}
 			if (field.equals("utmc")) {
 				rec.setUtmc(value);
 				return;
 			}
 			if (field.equals("v")) {
 				rec.setVersion(value);
 				return;
 			}
 
 			if (field.equals("query")) {
 				rec.setQuery(value);
 				rec.normalizeQuery();
 				return;
 			}
 			if (field.equals("queryType")) {
 				rec.setQueryType(value);
 				return;
 			}
 
 			if (field.equals("europeana_uri")) {
 				rec.setEuropeanaUri(value);
 				return;
 			}
 			// if (field.equals("numFound")) {
 			// value = value.replaceAll(",", "");
 			// int v = Integer.parseInt(value);
 			// rec.setNumFound(v);
 			// return;
 			// }
 			if (field.equals("page")) {
 				value = value.replaceAll(",", "");
 				int v = Integer.parseInt(value);
 				rec.setPage(v);
 				return;
 			}
 
 			if (field.equals("start")) {
 				value = value.replaceAll(",", "");
 				int v = Integer.parseInt(value);
 				rec.setStart(v);
 				return;
 			}
 
 			if (field.equals("queryConstraints")) {
 				rec.setQueryContraints(value);
 				return;
 			}
 
 			logger.warn("unknown field {} = {} ", field, value);
 
 		}
 
 		@Override
 		public String encode(EuropeanaRecord obj) {
 			return gson.toJson(obj);
 		}
 	}
 
 	public void generateUserId() {
 		if (userId == null || userId.isEmpty()) {
 			if (userAgent != null && !userAgent.isEmpty()) {
 				userId = hashStr(userAgent);
 			}
 		}
 
 	}
 
 }
