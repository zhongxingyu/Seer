 package org.ariadne_eu.utils.rest;
 
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.StringTokenizer;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.Future;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 public class Query {
 	public String[][] searchTerms = new String[0][]; // Conjunction of
 														// disjunction
 	public String[] exclusionTerms; // Disjunction of terms to be excluded
 	public String[] rankingTerms; // These are terms that should be considered
 									// in the ranking; e.g. "cny=BE"
 	public String sortKey; // By which key the results should be sorted
 	public String[] facets = new String[0]; // Which facets to count
 	// public QueryResult result; //QueryResult, consolidated from the nodes
 	// public int maxResults = 0; //Maximum number of results
 	public int resultListOffset = 0;
 	public int resultListSize = 12;
 	public int idListOffset = 0;
 	public int idListSize = 12;
 	public int maxCntFacets = 0; // Maximum number of results that will be
 									// counted for the facets
 	public PrintWriter responseWriter = null;
 	public int nrOfThreads;
 	int nodesProcessed = 0;
 	int nrOfNodes;
 	int queryCacheNr = -1;
 
 	public Query(/* InMemoryEngine eng */) {
 		// searchEngine = eng;
 		// nrOfNodes = searchEngine.getNrOfNodes();
 		// nrOfThreads = nrOfNodes;
 		// nodeResult = new QueryResult[nrOfNodes];
 		// if (facets == null) facets = searchEngine.getFacets();
 	}
 
 	public void checkCache() {
 		ArrayList<String[]> temp = new ArrayList<String[]>();
 		for (int i = 0; i < searchTerms.length; i++) {
 			String[] disjunction = searchTerms[i];
 			boolean found = false;
 			for (int j = 0; j < disjunction.length; j++) {
 				// TODO test spaces in search term
 				if (searchTerms[i][j].equalsIgnoreCase("lom.solr:all")) {
 					found = true;
 					break;
 				}
 			}
 			if (!found) {
 				temp.add(disjunction);
 			}
 		}
 		final int dimConj = temp.size(); // Number of disjunctions in the
 											// conjunction
 		// TODO if there are no searchTerms assume all
 		// System.out.println(dimConj+" "+qry.toString());
 		if (dimConj == 0 || (dimConj == 1 && searchTerms[0].length == 0)) {
 			String[][] searchAll = { { "lom.solr:all" } };
 			searchTerms = searchAll;
 			queryCacheNr = 0;
 		} else {
 			searchTerms = new String[temp.size()][];
 			temp.toArray(searchTerms);
 		}
 		// System.out.println(toString());
 	}
 
 	public void parseTerms(String inp) {
 		inp = inp.substring(inp.indexOf("{") + 1, inp.lastIndexOf("}"));
 		String regex = "\\{(.*?)\\}";
 		// System.out.println(regex + " IN "+inp);
 		Pattern pattern = Pattern.compile(regex);
 		Matcher matcher = pattern.matcher(inp);
 		ArrayList<ArrayList<String>> conjList = new ArrayList<ArrayList<String>>();
 		ArrayList<String> disjList;
 		while (matcher.find()) {
 			StringTokenizer st = new StringTokenizer(matcher.group(1), ",");
 			disjList = new ArrayList<String>();
 			while (st.hasMoreTokens()) {
 				String s = st.nextToken().trim().toLowerCase();
 				s = s.substring(1, s.length() - 1).trim();
 				// System.out.println(s);
 				disjList.add(s);
 			}
 			conjList.add(disjList);
 		}
 		searchTerms = new String[conjList.size()][];
 		for (int i = 0; i < conjList.size(); i++) {
 			String disj[] = new String[conjList.get(i).size()];
 			conjList.get(i).toArray(disj);
 			searchTerms[i] = disj;
 		}
 	}
 
 	public void parseJson(JSONObject jo) {
 
 		try {
 
 			String expression = "";
 			String language = "";
 			ArrayList<String[]> stList = new ArrayList<String[]>();
 			JSONArray clause = new JSONArray();
 			if (jo.has("clause"))
 				clause = jo.getJSONArray("clause");
 			for (int i = 0; i < clause.length(); i++) {
 				JSONObject thisClause = clause.getJSONObject(i);
 				if (thisClause.has("language"))
 					language = thisClause.getString("language").trim();
 				if (thisClause.has("expression"))
 					expression = thisClause.getString("expression").trim();
 
 				if (language.equalsIgnoreCase("vsql")) {
 					stList.addAll(parseVSQL(expression));
 				} else if (language.equalsIgnoreCase("anyOf")) {
 					String[] parts = expression.split(":");
					if (parts.length <= 2)
						stList.add(parseAnyOf(parts[0], parts[1], ","));
					else {

						String cnt = "";
						for (int j = 1; j < parts.length; j++) {

							cnt = cnt + parts[j] + ":";
						}
						cnt = cnt.substring(0, cnt.lastIndexOf(":"));
						stList.add(parseAnyOf(parts[0], cnt, ","));
					}
 				} else if (language.equalsIgnoreCase("anyOfFacet")) {
 					String[] parts = expression.split(":");
					if (parts.length <= 2)
						stList.add(parseAnyOf(parts[0], parts[1], ","));
					else {

						String cnt = "";
						for (int j = 1; j < parts.length; j++) {

							cnt = cnt + parts[j] + ":";
						}
						cnt = cnt.substring(0, cnt.lastIndexOf(":"));
						stList.add(parseAnyOf(parts[0], cnt, ","));
					}
 				}
 			}
 			searchTerms = new String[stList.size()][];
 			for (int i = 0; i < stList.size(); i++)
 				searchTerms[i] = stList.get(i);
 
 			// Set facets to report the numbers on
 			if (jo.has("facets")) {
 				JSONArray myFacets = jo.getJSONArray("facets");
 				facets = new String[myFacets.length()];
 				for (int i = 0; i < myFacets.length(); i++) {
 					facets[i] = myFacets.getString(i);
 				}
 			}
 
 			if (jo.has("resultListOffset"))
 				resultListOffset = jo.getInt("resultListOffset");
 			if (jo.has("resultListSize"))
 				resultListSize = jo.getInt("resultListSize");
 			// Set IDsFrom and IDsTo to default values
 			idListOffset = resultListOffset;
 			idListSize = resultListSize;
 			if (jo.has("idListOffset"))
 				idListOffset = jo.getInt("idListOffset");
 			if (jo.has("idListSize"))
 				idListSize = jo.getInt("idListSize");
 		} catch (Exception ex) {
 			System.out.println(ex.getMessage());
 			ex.printStackTrace();
 		}
 		// System.out.println(toString());
 	}
 
 	public void parseURL(HttpServletRequest request) {
 
 		try {
 			Map<String, String[]> parMap = request.getParameterMap();
 			Set<String> parKeys = parMap.keySet();
 			String[] keys = new String[parKeys.size()];
 			parKeys.toArray(keys);
 			for (int i = 0; i < keys.length; i++) {
 				if (keys[i].equalsIgnoreCase("clause")) {
 					parseURLclauses(parMap.get(keys[i]));
 				} else if (keys[i].equalsIgnoreCase("info")) {
 					parseURLinfo(parMap.get(keys[i]));
 				} else if (keys[i].equalsIgnoreCase("offset")) {
 					resultListOffset = Integer.parseInt(parMap.get(keys[i])[0]);
 				} else if (keys[i].equalsIgnoreCase("limit")) {
 					resultListSize = Integer.parseInt(parMap.get(keys[i])[0]);
 				}
 			}
 			// Set idListOffset and idListSize to default values
 			idListOffset = resultListOffset;
 			idListSize = resultListSize;
 		} catch (Exception ex) {
 			System.out.println(ex.getMessage());
 			ex.printStackTrace();
 		}
 	}
 
 	public void parseURLclauses(String[] values) {
 		ArrayList<String[]> stList = new ArrayList<String[]>();
 		for (int i = 0; i < values.length; i++) {
 			String[] parts = values[i].split(":");
 			if (parts[0].equalsIgnoreCase("VSQL")) {
 				stList.addAll(parseVSQL(parts[1]));
 			} else if (parts[0].equalsIgnoreCase("facet")) {
 				stList.add(parseFacet(parts[1], parts[2], "|"));
 			}
 		}
 		searchTerms = new String[stList.size()][];
 		stList.toArray(searchTerms);
 	}
 
 	public void parseURLinfo(String[] values) {
 		ArrayList<String> facetList = new ArrayList<String>();
 		for (int i = 0; i < values.length; i++) {
 			String[] parts = values[i].split(":");
 			if (parts[0].equalsIgnoreCase("elementset")) {
 				// TODO
 			} else if (parts[0].equalsIgnoreCase("facet")) {
 				facetList.add(parts[1]);
 			}
 		}
 		facets = new String[facetList.size()];
 		facetList.toArray(facets);
 		for (int i = 0; i < facets.length; i++)
 			System.out.print(" " + facets[i]);
 		System.out.println();
 	}
 
 	public List<String[]> parseVSQL(String content) {
 		List<String[]> result = new ArrayList<String[]>();
 		StringTokenizer st = new StringTokenizer(content, " ");
 		List<String> subject = new ArrayList<String>();
 		while (st.hasMoreTokens()) {
 			subject.add(st.nextToken().trim().toLowerCase());
 		}
 		for (int j = 0; j < subject.size(); j++) {
 			String[] disjunction = new String[1];
 			disjunction[0] = "contents:" + subject.get(j);
 			result.add(disjunction);
 		}
 		return result;
 	}
 
 	/*
 	 * public List<String> parseFacet(String content){ StringTokenizer st = new
 	 * StringTokenizer(content.substring(1, content.length()-1), ",");
 	 * List<String> result = new ArrayList<String>(); while(st.hasMoreTokens())
 	 * { String f = st.nextToken().trim().toLowerCase();
 	 * result.add(f.substring(1, f.length() - 1).trim()); } return result; }
 	 */
 	public String[] parseFacet(String facet, String content, String delim) {
 		StringTokenizer st = new StringTokenizer(content, delim);
 		List<String> parsedFacets = new ArrayList<String>();
 		while (st.hasMoreTokens()) {
 			String f = st.nextToken().trim();
 			parsedFacets.add(facet + ":\"" + f.trim() + "\"");
 		}
 		String[] facetArray = new String[parsedFacets.size()];
 		parsedFacets.toArray(facetArray);
 		return facetArray;
 	}
 
 	public String[] parseAnyOf(String field, String content, String delim) {
 		StringTokenizer st = new StringTokenizer(content, delim);
 		List<String> parsedDisjunction = new ArrayList<String>();
 		while (st.hasMoreTokens()) {
 			// String f = st.nextToken().trim().toLowerCase();
 			String f = st.nextToken().trim();
 			parsedDisjunction.add(field + ":\"" + f + "\"");
 		}
 		String[] disjunctionArray = new String[parsedDisjunction.size()];
 		parsedDisjunction.toArray(disjunctionArray);
 		return disjunctionArray;
 	}
 
 	public String toString() {
 		String result = "{";
 		for (int i = 0; i < searchTerms.length; i++) {
 			result = result + "{";
 			for (int j = 0; j < searchTerms[i].length; j++) {
 				result = result + "\"" + searchTerms[i][j] + "\"";
 				if (j != searchTerms[i].length - 1)
 					result = result + ",";
 			}
 			result = result + "}";
 			if (i != searchTerms.length - 1)
 				result = result + ",";
 		}
 		result = result + "}";
 		return result;
 	}
 }
