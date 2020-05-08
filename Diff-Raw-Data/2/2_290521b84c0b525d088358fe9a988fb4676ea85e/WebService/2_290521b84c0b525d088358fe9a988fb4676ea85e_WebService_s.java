 /*
  * Copyright 2006 Marc Wick, geonames.org
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
 package org.geonames;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.input.SAXBuilder;
 
 /**
  * provides static methods accessing the geonames web services
  * 
  * @author marc@geonames
  * 
  */
 public class WebService {
 
 	private static String USER_AGENT = "geonames webservice client 0.6";
 
 	private static String geoNamesServer = "http://ws.geonames.org";
 
 	private static Style defaultStyle = Style.MEDIUM;
 
 	/**
 	 * user name to pass to commercial web services for authentication and
 	 * authorization
 	 */
 	private static String userName;
 
 	private static String token;
 
 	private static String addUserName(String url) {
 		if (userName != null) {
 			url = url + "&username=" + userName;
 		}
 		if (token != null) {
 			url = url + "&token=" + token;
 		}
 		return url;
 	}
 
 	private static String addDefaultStyle(String url) {
 		if (defaultStyle != Style.MEDIUM) {
			url = url + "style=" + defaultStyle.name();
 		}
 		return url;
 	}
 
 	public static List<PostalCode> postalCodeSearch(String postalCode,
 			String placeName, String countryCode) throws Exception {
 		PostalCodeSearchCriteria postalCodeSearchCriteria = new PostalCodeSearchCriteria();
 		postalCodeSearchCriteria.setPostalCode(postalCode);
 		postalCodeSearchCriteria.setPlaceName(placeName);
 		postalCodeSearchCriteria.setCountryCode(countryCode);
 		return postalCodeSearch(postalCodeSearchCriteria);
 	}
 
 	public static List<PostalCode> postalCodeSearch(
 			PostalCodeSearchCriteria postalCodeSearchCriteria) throws Exception {
 		List<PostalCode> postalCodes = new ArrayList<PostalCode>();
 
 		String url = geoNamesServer + "/postalCodeSearch?";
 		if (postalCodeSearchCriteria.getPostalCode() != null) {
 			url = url
 					+ "postalcode="
 					+ URLEncoder.encode(postalCodeSearchCriteria
 							.getPostalCode(), "UTF8");
 		}
 		if (postalCodeSearchCriteria.getPlaceName() != null) {
 			if (!url.endsWith("&")) {
 				url = url + "&";
 			}
 			url = url
 					+ "placename="
 					+ URLEncoder.encode(
 							postalCodeSearchCriteria.getPlaceName(), "UTF8");
 		}
 		if (postalCodeSearchCriteria.getAdminCode1() != null) {
 			url = url
 					+ "&adminCode1="
 					+ URLEncoder.encode(postalCodeSearchCriteria
 							.getAdminCode1(), "UTF8");
 		}
 
 		if (postalCodeSearchCriteria.getCountryCode() != null) {
 			if (!url.endsWith("&")) {
 				url = url + "&";
 			}
 			url = url + "country=" + postalCodeSearchCriteria.getCountryCode();
 		}
 		if (postalCodeSearchCriteria.getMaxRows() > 0) {
 			url = url + "&maxRows=" + postalCodeSearchCriteria.getMaxRows();
 		}
 		if (postalCodeSearchCriteria.isOROperator()) {
 			url = url + "&operator=OR";
 		}
 		url = addUserName(url);
 
 		URLConnection conn = new URL(url).openConnection();
 		conn.setRequestProperty("User-Agent", USER_AGENT);
 		SAXBuilder parser = new SAXBuilder();
 		Document doc = parser.build(conn.getInputStream());
 
 		Element root = doc.getRootElement();
 		for (Object obj : root.getChildren("code")) {
 			Element codeElement = (Element) obj;
 			PostalCode code = new PostalCode();
 			code.setPostalCode(codeElement.getChildText("postalcode"));
 			code.setPlaceName(codeElement.getChildText("name"));
 			code.setCountryCode(codeElement.getChildText("countryCode"));
 			code.setAdminCode1(codeElement.getChildText("adminCode1"));
 			code.setAdminCode2(codeElement.getChildText("adminCode2"));
 			code.setAdminName1(codeElement.getChildText("adminName1"));
 			code.setAdminName2(codeElement.getChildText("adminName2"));
 
 			code.setLatitude(Double
 					.parseDouble(codeElement.getChildText("lat")));
 			code.setLongitude(Double.parseDouble(codeElement
 					.getChildText("lng")));
 
 			postalCodes.add(code);
 		}
 
 		return postalCodes;
 	}
 
 	public static List<PostalCode> findNearbyPostalCodes(
 			PostalCodeSearchCriteria postalCodeSearchCriteria) throws Exception {
 
 		List<PostalCode> postalCodes = new ArrayList<PostalCode>();
 
 		String url = geoNamesServer + "/findNearbyPostalCodes?";
 		if (postalCodeSearchCriteria.getPostalCode() != null) {
 			url = url
 					+ "&postalcode="
 					+ URLEncoder.encode(postalCodeSearchCriteria
 							.getPostalCode(), "UTF8");
 		}
 		if (postalCodeSearchCriteria.getPlaceName() != null) {
 			url = url
 					+ "&placename="
 					+ URLEncoder.encode(
 							postalCodeSearchCriteria.getPlaceName(), "UTF8");
 		}
 		if (postalCodeSearchCriteria.getCountryCode() != null) {
 			url = url + "&country=" + postalCodeSearchCriteria.getCountryCode();
 		}
 
 		if (postalCodeSearchCriteria.getLatitude() != null) {
 			url = url + "&lat=" + postalCodeSearchCriteria.getLatitude();
 		}
 		if (postalCodeSearchCriteria.getLongitude() != null) {
 			url = url + "&lng=" + postalCodeSearchCriteria.getLongitude();
 		}
 		if (postalCodeSearchCriteria.getStyle() != null) {
 			url = url + "&style=" + postalCodeSearchCriteria.getStyle();
 		}
 		if (postalCodeSearchCriteria.getMaxRows() > 0) {
 			url = url + "&maxRows=" + postalCodeSearchCriteria.getMaxRows();
 		}
 
 		if (postalCodeSearchCriteria.getRadius() > 0) {
 			url = url + "&radius=" + postalCodeSearchCriteria.getRadius();
 		}
 		url = addUserName(url);
 
 		URLConnection conn = new URL(url).openConnection();
 		conn.setRequestProperty("User-Agent", USER_AGENT);
 		SAXBuilder parser = new SAXBuilder();
 		Document doc = parser.build(conn.getInputStream());
 
 		Element root = doc.getRootElement();
 		for (Object obj : root.getChildren("code")) {
 			Element codeElement = (Element) obj;
 			PostalCode code = new PostalCode();
 			code.setPostalCode(codeElement.getChildText("postalcode"));
 			code.setPlaceName(codeElement.getChildText("name"));
 			code.setCountryCode(codeElement.getChildText("countryCode"));
 
 			code.setLatitude(Double
 					.parseDouble(codeElement.getChildText("lat")));
 			code.setLongitude(Double.parseDouble(codeElement
 					.getChildText("lng")));
 
 			code.setAdminName1(codeElement.getChildText("adminName1"));
 			code.setAdminCode1(codeElement.getChildText("adminCode1"));
 			code.setAdminName2(codeElement.getChildText("adminName2"));
 			code.setAdminCode2(codeElement.getChildText("adminCode2"));
 
 			if (codeElement.getChildText("distance") != null) {
 				code.setDistance(Double.parseDouble(codeElement
 						.getChildText("distance")));
 			}
 
 			postalCodes.add(code);
 		}
 
 		return postalCodes;
 	}
 
 	public static List<Toponym> findNearbyPlaceName(double latitude,
 			double longitude) throws IOException, Exception {
 		return findNearbyPlaceName(latitude, longitude, 0, 0);
 	}
 
 	public static List<Toponym> findNearbyPlaceName(double latitude,
 			double longitude, double radius, int maxRows) throws IOException,
 			Exception {
 		List<Toponym> places = new ArrayList<Toponym>();
 
 		String url = geoNamesServer + "/findNearbyPlaceName?";
 
 		url = url + "&lat=" + latitude;
 		url = url + "&lng=" + longitude;
 		if (radius > 0) {
 			url = url + "&radius=" + radius;
 		}
 		if (maxRows > 0) {
 			url = url + "&maxRows=" + maxRows;
 		}
 		url = addUserName(url);
 		url = addDefaultStyle(url);
 
 		URLConnection conn = new URL(url).openConnection();
 		conn.setRequestProperty("User-Agent", USER_AGENT);
 		SAXBuilder parser = new SAXBuilder();
 		Document doc = parser.build(conn.getInputStream());
 
 		Element root = doc.getRootElement();
 		for (Object obj : root.getChildren("geoname")) {
 			Element toponymElement = (Element) obj;
 			Toponym toponym = new Toponym();
 			toponym.setName(toponymElement.getChildText("name"));
 			toponym.setLatitude(Double.parseDouble(toponymElement
 					.getChildText("lat")));
 			toponym.setLongitude(Double.parseDouble(toponymElement
 					.getChildText("lng")));
 
 			String geonameId = toponymElement.getChildText("geonameId");
 			if (geonameId != null) {
 				toponym.setGeonameId(Integer.parseInt(geonameId));
 			}
 
 			toponym.setCountryCode(toponymElement.getChildText("countryCode"));
 			toponym.setCountryName(toponymElement.getChildText("countryName"));
 			toponym.setFeatureClass(FeatureClass.fromValue(toponymElement
 					.getChildText("fcl")));
 			toponym.setFeatureCode(toponymElement.getChildText("fcode"));
 
 			toponym.setFeatureClassName(toponymElement.getChildText("fclName"));
 			toponym
 					.setFeatureCodeName(toponymElement
 							.getChildText("fCodeName"));
 
 			String population = toponymElement.getChildText("population");
 			if (population != null && !"".equals(population)) {
 				toponym.setPopulation(Integer.parseInt(population));
 			}
 			String elevation = toponymElement.getChildText("elevation");
 			if (elevation != null && !"".equals(elevation)) {
 				toponym.setElevation(Integer.parseInt(elevation));
 			}
 
 			places.add(toponym);
 		}
 
 		return places;
 	}
 
 	public static Address findNearestAddress(double latitude, double longitude)
 			throws IOException, Exception {
 
 		String url = geoNamesServer + "/findNearestAddress?";
 
 		url = url + "&lat=" + latitude;
 		url = url + "&lng=" + longitude;
 		url = addUserName(url);
 
 		URLConnection conn = new URL(url).openConnection();
 		conn.setRequestProperty("User-Agent", USER_AGENT);
 		SAXBuilder parser = new SAXBuilder();
 		Document doc = parser.build(conn.getInputStream());
 
 		Element root = doc.getRootElement();
 		for (Object obj : root.getChildren("address")) {
 			Element codeElement = (Element) obj;
 			Address address = new Address();
 			address.setStreet(codeElement.getChildText("street"));
 			address.setStreetNumber(codeElement.getChildText("streetNumber"));
 
 			address.setPostalCode(codeElement.getChildText("postalcode"));
 			address.setPlaceName(codeElement.getChildText("name"));
 			address.setCountryCode(codeElement.getChildText("countryCode"));
 
 			address.setLatitude(Double.parseDouble(codeElement
 					.getChildText("lat")));
 			address.setLongitude(Double.parseDouble(codeElement
 					.getChildText("lng")));
 
 			address.setAdminName1(codeElement.getChildText("adminName1"));
 			address.setAdminCode1(codeElement.getChildText("adminCode1"));
 			address.setAdminName2(codeElement.getChildText("adminName2"));
 			address.setAdminCode2(codeElement.getChildText("adminCode2"));
 
 			address.setDistance(Double.parseDouble(codeElement
 					.getChildText("distance")));
 
 			return address;
 		}
 
 		return null;
 	}
 
 	public static Intersection findNearestIntersection(double latitude,
 			double longitude) throws Exception {
 		return findNearestIntersection(latitude, longitude, 0);
 	}
 
 	public static Intersection findNearestIntersection(double latitude,
 			double longitude, double radius) throws Exception {
 
 		String url = geoNamesServer + "/findNearestIntersection?";
 
 		url = url + "&lat=" + latitude;
 		url = url + "&lng=" + longitude;
 		if (radius > 0) {
 			url = url + "&radius=" + radius;
 		}
 		url = addUserName(url);
 
 		URLConnection conn = new URL(url).openConnection();
 		conn.setRequestProperty("User-Agent", USER_AGENT);
 		SAXBuilder parser = new SAXBuilder();
 		Document doc = parser.build(conn.getInputStream());
 
 		Element root = doc.getRootElement();
 		for (Object obj : root.getChildren("intersection")) {
 			Element e = (Element) obj;
 			Intersection intersection = new Intersection();
 			intersection.setStreet1(e.getChildText("street1"));
 			intersection.setStreet2(e.getChildText("street2"));
 			intersection.setLatitude(Double.parseDouble(e.getChildText("lat")));
 			intersection
 					.setLongitude(Double.parseDouble(e.getChildText("lng")));
 			intersection.setDistance(Double.parseDouble(e
 					.getChildText("distance")));
 			intersection.setPostalCode(e.getChildText("postalcode"));
 			intersection.setPlaceName(e.getChildText("placename"));
 			intersection.setCountryCode(e.getChildText("countryCode"));
 			intersection.setAdminName2(e.getChildText("adminName2"));
 			intersection.setAdminCode1(e.getChildText("adminCode1"));
 			intersection.setAdminName1(e.getChildText("adminName1"));
 			return intersection;
 		}
 		return null;
 	}
 
 	public static List<StreetSegment> findNearbyStreets(double latitude,
 			double longitude, double radius) throws Exception {
 
 		String url = geoNamesServer + "/findNearbyStreets?";
 
 		url = url + "&lat=" + latitude;
 		url = url + "&lng=" + longitude;
 		if (radius > 0) {
 			url = url + "&radius=" + radius;
 		}
 		url = addUserName(url);
 
 		List<StreetSegment> segments = new ArrayList<StreetSegment>();
 
 		URLConnection conn = new URL(url).openConnection();
 		conn.setRequestProperty("User-Agent", USER_AGENT);
 		SAXBuilder parser = new SAXBuilder();
 		Document doc = parser.build(conn.getInputStream());
 
 		Element root = doc.getRootElement();
 		for (Object obj : root.getChildren("streetSegment")) {
 			Element e = (Element) obj;
 			StreetSegment streetSegment = new StreetSegment();
 			String line = e.getChildText("line");
 			String[] points = line.split(",");
 			double[] latArray = new double[points.length];
 			double[] lngArray = new double[points.length];
 			for (int i = 0; i < points.length; i++) {
 				String[] coords = points[i].split(" ");
 				lngArray[i] = Double.parseDouble(coords[0]);
 				latArray[i] = Double.parseDouble(coords[1]);
 			}
 
 			streetSegment.setCfcc(e.getChildText("cfcc"));
 			streetSegment.setName(e.getChildText("name"));
 			streetSegment.setFraddl(e.getChildText("fraddl"));
 			streetSegment.setFraddr(e.getChildText("fraddr"));
 			streetSegment.setToaddl(e.getChildText("toaddl"));
 			streetSegment.setToaddr(e.getChildText("toaddr"));
 			streetSegment.setPostalCode(e.getChildText("postalcode"));
 			streetSegment.setPlaceName(e.getChildText("placename"));
 			streetSegment.setCountryCode(e.getChildText("countryCode"));
 			streetSegment.setAdminName2(e.getChildText("adminName2"));
 			streetSegment.setAdminCode1(e.getChildText("adminCode1"));
 			streetSegment.setAdminName1(e.getChildText("adminName1"));
 			segments.add(streetSegment);
 		}
 		return segments;
 	}
 
 	public static ToponymSearchResult search(String q, String countryCode,
 			String name, String[] featureCodes, int startRow) throws Exception {
 		return search(q, countryCode, name, featureCodes, startRow, null, null,
 				null);
 	}
 
 	public static ToponymSearchResult search(String q, String countryCode,
 			String name, String[] featureCodes, int startRow, String language,
 			Style style, String exactName) throws Exception {
 		ToponymSearchCriteria searchCriteria = new ToponymSearchCriteria();
 		searchCriteria.setQ(q);
 		searchCriteria.setCountryCode(countryCode);
 		searchCriteria.setName(name);
 		searchCriteria.setFeatureCodes(featureCodes);
 		searchCriteria.setStartRow(startRow);
 		searchCriteria.setLanguage(language);
 		searchCriteria.setStyle(style);
 		searchCriteria.setNameEquals(exactName);
 		return search(searchCriteria);
 	}
 
 	public static ToponymSearchResult search(
 			ToponymSearchCriteria searchCriteria) throws Exception {
 		ToponymSearchResult searchResult = new ToponymSearchResult();
 
 		String url = geoNamesServer + "/search?";
 
 		if (searchCriteria.getQ() != null) {
 			url = url + "q=" + URLEncoder.encode(searchCriteria.getQ(), "UTF8");
 		}
 		if (searchCriteria.getNameEquals() != null) {
 			url = url + "&name_equals="
 					+ URLEncoder.encode(searchCriteria.getNameEquals(), "UTF8");
 		}
 		if (searchCriteria.getNameStartsWith() != null) {
 			url = url
 					+ "&name_startsWith="
 					+ URLEncoder.encode(searchCriteria.getNameStartsWith(),
 							"UTF8");
 		}
 
 		if (searchCriteria.getName() != null) {
 			url = url + "&name="
 					+ URLEncoder.encode(searchCriteria.getName(), "UTF8");
 		}
 
 		if (searchCriteria.getTag() != null) {
 			url = url + "&tag="
 					+ URLEncoder.encode(searchCriteria.getTag(), "UTF8");
 		}
 
 		if (searchCriteria.getCountryCode() != null) {
 			url = url + "&country=" + searchCriteria.getCountryCode();
 		}
 
 		if (searchCriteria.getAdminCode1() != null) {
 			url = url + "&adminCode1="
 					+ URLEncoder.encode(searchCriteria.getAdminCode1(), "UTF8");
 		}
 		if (searchCriteria.getAdminCode2() != null) {
 			url = url + "&adminCode2="
 					+ URLEncoder.encode(searchCriteria.getAdminCode2(), "UTF8");
 		}
 		if (searchCriteria.getAdminCode3() != null) {
 			url = url + "&adminCode3="
 					+ URLEncoder.encode(searchCriteria.getAdminCode3(), "UTF8");
 		}
 		if (searchCriteria.getAdminCode4() != null) {
 			url = url + "&adminCode4="
 					+ URLEncoder.encode(searchCriteria.getAdminCode4(), "UTF8");
 		}
 
 		if (searchCriteria.getLanguage() != null) {
 			url = url + "&lang=" + searchCriteria.getLanguage();
 		}
 
 		if (searchCriteria.getFeatureClass() != null) {
 			url = url + "&featureClass=" + searchCriteria.getFeatureClass();
 		}
 
 		if (searchCriteria.getFeatureCodes() != null) {
 			for (String featureCode : searchCriteria.getFeatureCodes()) {
 				url = url + "&fcode=" + featureCode;
 			}
 		}
 		if (searchCriteria.getMaxRows() > 0) {
 			url = url + "&maxRows=" + searchCriteria.getMaxRows();
 		}
 		if (searchCriteria.getStartRow() > 0) {
 			url = url + "&startRow=" + searchCriteria.getStartRow();
 		}
 
 		if (searchCriteria.getStyle() != null) {
 			url = url + "&style=" + searchCriteria.getStyle();
 		} else {
 			url = addDefaultStyle(url);
 		}
 		url = addUserName(url);
 
 		URLConnection conn = new URL(url).openConnection();
 		conn.setRequestProperty("User-Agent", USER_AGENT);
 		SAXBuilder parser = new SAXBuilder();
 		Document doc = parser.build(conn.getInputStream());
 
 		Element root = doc.getRootElement();
 
 		checkException(root);
 
 		searchResult.totalResultsCount = Integer.parseInt(root
 				.getChildText("totalResultsCount"));
 		searchResult.setStyle(Style.valueOf(root.getAttributeValue("style")));
 
 		for (Object obj : root.getChildren("geoname")) {
 			Element toponymElement = (Element) obj;
 			Toponym toponym = new Toponym();
 
 			toponym.setName(toponymElement.getChildText("name"));
 			toponym.setAlternateNames(toponymElement
 					.getChildText("alternateNames"));
 			toponym.setLatitude(Double.parseDouble(toponymElement
 					.getChildText("lat")));
 			toponym.setLongitude(Double.parseDouble(toponymElement
 					.getChildText("lng")));
 
 			String geonameId = toponymElement.getChildText("geonameId");
 			if (geonameId != null) {
 				toponym.setGeonameId(Integer.parseInt(geonameId));
 			}
 
 			toponym.setCountryCode(toponymElement.getChildText("countryCode"));
 			toponym.setCountryName(toponymElement.getChildText("countryName"));
 
 			toponym.setFeatureClass(FeatureClass.fromValue(toponymElement
 					.getChildText("fcl")));
 			toponym.setFeatureCode(toponymElement.getChildText("fcode"));
 
 			toponym.setFeatureClassName(toponymElement.getChildText("fclName"));
 			toponym
 					.setFeatureCodeName(toponymElement
 							.getChildText("fCodeName"));
 
 			String population = toponymElement.getChildText("population");
 			if (population != null && !"".equals(population)) {
 				toponym.setPopulation(Integer.parseInt(population));
 			}
 			String elevation = toponymElement.getChildText("elevation");
 			if (elevation != null && !"".equals(elevation)) {
 				toponym.setElevation(Integer.parseInt(elevation));
 			}
 
 			toponym.setAdminCode1(toponymElement.getChildText("adminCode1"));
 			toponym.setAdminName1(toponymElement.getChildText("adminName1"));
 			toponym.setAdminCode2(toponymElement.getChildText("adminCode2"));
 			toponym.setAdminName2(toponymElement.getChildText("adminName2"));
 			toponym.setAdminCode3(toponymElement.getChildText("adminCode3"));
 			toponym.setAdminCode4(toponymElement.getChildText("adminCode4"));
 
 			Element timezoneElement = toponymElement.getChild("timezone");
 			if (timezoneElement != null) {
 				Timezone timezone = new Timezone();
 				timezone.setTimezoneId(timezoneElement.getValue());
 				timezone.setDstOffset(Double.parseDouble(timezoneElement
 						.getAttributeValue("dstOffset")));
 				timezone.setGmtOffset(Double.parseDouble(timezoneElement
 						.getAttributeValue("gmtOffset")));
 				toponym.setTimezone(timezone);
 			}
 
 			searchResult.toponyms.add(toponym);
 		}
 
 		return searchResult;
 	}
 
 	public static void saveTags(String[] tags, Toponym toponym,
 			String username, String password) throws Exception {
 		if (toponym.getGeonameId() == 0) {
 			throw new Error("no geonameid specified");
 		}
 
 		// FIXME proper url
 		String url = geoNamesServer + "/servlet/geonames?srv=61";
 
 		url = url + "&geonameId=" + toponym.getGeonameId();
 		url = addUserName(url);
 
 		StringBuilder tagsCommaseparated = new StringBuilder();
 		for (String tag : tags) {
 			tagsCommaseparated.append(tag + ",");
 		}
 		url = url + "&tag=" + tagsCommaseparated;
 
 		URLConnection conn = new URL(url).openConnection();
 		conn.setRequestProperty("User-Agent", USER_AGENT);
 		SAXBuilder parser = new SAXBuilder();
 		Document doc = parser.build(conn.getInputStream());
 
 		Element root = doc.getRootElement();
 
 		checkException(root);
 	}
 
 	private static void checkException(Element root) throws Exception {
 		Element message = root.getChild("status");
 		if (message != null) {
 			throw new Exception(message.getAttributeValue("message"));
 		}
 	}
 
 	public static void wikipediaSearch() {
 		// TODO
 	}
 
 	public static List<WikipediaArticle> findNearbyWikipedia(double latitude,
 			double longitude, String language) throws Exception {
 
 		List<WikipediaArticle> articles = new ArrayList<WikipediaArticle>();
 
 		String url = geoNamesServer + "/findNearbyWikipedia?";
 
 		url = url + "lat=" + latitude;
 		url = url + "&lng=" + longitude;
 
 		if (language != null) {
 			url = url + "&lang=" + language;
 		}
 		url = addUserName(url);
 
 		URLConnection conn = new URL(url).openConnection();
 		conn.setRequestProperty("User-Agent", USER_AGENT);
 		SAXBuilder parser = new SAXBuilder();
 		Document doc = parser.build(conn.getInputStream());
 
 		Element root = doc.getRootElement();
 		for (Object obj : root.getChildren("entry")) {
 			Element codeElement = (Element) obj;
 			WikipediaArticle wikipediaArticle = new WikipediaArticle();
 			wikipediaArticle.setLanguage(codeElement.getChildText("lang"));
 			wikipediaArticle.setTitle(codeElement.getChildText("title"));
 			wikipediaArticle.setSummary(codeElement.getChildText("summary"));
 			wikipediaArticle.setFeature(codeElement.getChildText("feature"));
 			wikipediaArticle.setWikipediaUrl(codeElement
 					.getChildText("wikipediaUrl"));
 
 			wikipediaArticle.setLatitude(Double.parseDouble(codeElement
 					.getChildText("lat")));
 			wikipediaArticle.setLongitude(Double.parseDouble(codeElement
 					.getChildText("lng")));
 
 			String population = codeElement.getChildText("population");
 			if (population != null && !"".equals(population)) {
 				wikipediaArticle.setPopulation(Integer.parseInt(population));
 			}
 
 			String elevation = codeElement.getChildText("altitude");
 			if (elevation != null && !"".equals(elevation)) {
 				wikipediaArticle.setElevation(Integer.parseInt(elevation));
 			}
 
 			articles.add(wikipediaArticle);
 		}
 
 		return articles;
 	}
 
 	/**
 	 * GTOPO30 is a global digital elevation model (DEM) with a horizontal grid
 	 * spacing of 30 arc seconds (approximately 1 kilometer). GTOPO30 was
 	 * derived from several raster and vector sources of topographic
 	 * information.
 	 * 
 	 * @param latitude
 	 * @param longitude
 	 * @return a single number giving the elevation in meters according to
 	 *         gtopo30, ocean areas have been masked as "no data" and have been
 	 *         assigned a value of -9999
 	 * @throws IOException
 	 */
 	public static int gtopo30(double latitude, double longitude)
 			throws IOException {
 		String url = geoNamesServer + "/gtopo30?lat=" + latitude + "&lng="
 				+ longitude;
 		url = addUserName(url);
 		URL geonamesWebservice = new URL(url);
 
 		URLConnection conn = geonamesWebservice.openConnection();
 		conn.setRequestProperty("User-Agent", USER_AGENT);
 		BufferedReader in = new BufferedReader(new InputStreamReader(
 				geonamesWebservice.openStream()));
 		String gtopo30 = in.readLine();
 		in.close();
 		return Integer.parseInt(gtopo30);
 	}
 
 	/**
 	 * The iso country code of any given point.
 	 * 
 	 * @param latitude
 	 * @param longitude
 	 * @return iso country code for the given latitude/longitude
 	 * @throws IOException
 	 */
 	public static String countryCode(double latitude, double longitude)
 			throws IOException {
 		String url = geoNamesServer + "/countrycode?lat=" + latitude + "&lng="
 				+ longitude;
 		url = addUserName(url);
 		URL geonamesWebservice = new URL(url);
 
 		URLConnection conn = geonamesWebservice.openConnection();
 		conn.setRequestProperty("User-Agent", USER_AGENT);
 		BufferedReader in = new BufferedReader(new InputStreamReader(
 				geonamesWebservice.openStream()));
 		String cc = in.readLine();
 		in.close();
 		return cc;
 	}
 
 	/**
 	 * get the timezone for a given location
 	 * 
 	 * @param latitude
 	 * @param longitude
 	 * @return timezone at the given location
 	 * @throws IOException
 	 * @throws Exception
 	 */
 	public static Timezone timezone(double latitude, double longitude)
 			throws IOException, Exception {
 
 		String url = geoNamesServer + "/timezone?";
 
 		url = url + "&lat=" + latitude;
 		url = url + "&lng=" + longitude;
 		url = addUserName(url);
 
 		URLConnection conn = new URL(url).openConnection();
 		conn.setRequestProperty("User-Agent", USER_AGENT);
 		SAXBuilder parser = new SAXBuilder();
 		Document doc = parser.build(conn.getInputStream());
 
 		Element root = doc.getRootElement();
 		for (Object obj : root.getChildren("timezone")) {
 			Element codeElement = (Element) obj;
 			Timezone timezone = new Timezone();
 			timezone.setTimezoneId(codeElement.getChildText("timezoneId"));
 			timezone.setGmtOffset(Double.parseDouble(codeElement
 					.getChildText("gmtOffset")));
 			timezone.setDstOffset(Double.parseDouble(codeElement
 					.getChildText("dstOffset")));
 			return timezone;
 		}
 
 		return null;
 	}
 
 	/**
 	 * @return the geoNamesServer, default is http://ws.geonames.org
 	 */
 	public static String getGeoNamesServer() {
 		return geoNamesServer;
 	}
 
 	/**
 	 * @param geoNamesServer
 	 *            the geonamesServer to set
 	 */
 	public static void setGeoNamesServer(String pGeoNamesServer) {
 		if (pGeoNamesServer == null) {
 			throw new Error();
 		}
 		pGeoNamesServer = pGeoNamesServer.trim().toLowerCase();
 		if (!pGeoNamesServer.startsWith("http://")) {
 			pGeoNamesServer += "http://";
 		}
 		geoNamesServer = pGeoNamesServer;
 	}
 
 	/**
 	 * @return the userName
 	 */
 	public static String getUserName() {
 		return userName;
 	}
 
 	/**
 	 * @param userName
 	 *            the userName to set
 	 */
 	public static void setUserName(String userName) {
 		WebService.userName = userName;
 	}
 
 	/**
 	 * @return the token
 	 */
 	public static String getToken() {
 		return token;
 	}
 
 	/**
 	 * @param token
 	 *            the token to set
 	 */
 	public static void setToken(String token) {
 		WebService.token = token;
 	}
 
 	/**
 	 * @return the defaultStyle
 	 */
 	public static Style getDefaultStyle() {
 		return defaultStyle;
 	}
 
 	/**
 	 * @param defaultStyle
 	 *            the defaultStyle to set
 	 */
 	public static void setDefaultStyle(Style defaultStyle) {
 		WebService.defaultStyle = defaultStyle;
 	}
 
 }
