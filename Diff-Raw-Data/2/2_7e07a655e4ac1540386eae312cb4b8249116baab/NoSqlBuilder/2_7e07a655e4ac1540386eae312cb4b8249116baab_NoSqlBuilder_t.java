 /**
  * PromniCAT - Collection and Analysis of Business Process Models
  * Copyright (C) 2012 Cindy Fähnrich, Tobias Hoppe, Andrina Mascher
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package de.uni_potsdam.hpi.bpt.promnicat.persistenceApi.orientdbObj;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map.Entry;
 import java.util.logging.Logger;
 
 import de.uni_potsdam.hpi.bpt.promnicat.persistenceApi.DbFilterConfig;
 import de.uni_potsdam.hpi.bpt.promnicat.persistenceApi.Representation;
 
 
 /**
  * This class is used by {@link PersistenceApiOrientDbObj} to generate NoSQL strings
  * that can be used in OrientDb. 
  * These are either based on a DbFilterConfig to return {@link Representation}s
  * or just on a list of any databaseIds for returning any class.
  * 
  * @author Andrina Mascher
  *
  */
 public class NoSqlBuilder {
 
 	@SuppressWarnings("unused")
 	private final static Logger logger = Logger
 			.getLogger(NoSqlBuilder.class.getName());
 	
 	/**
 	 * Builds a query that returns instances of {@link Representation}.
 	 * 
 	 * @param config to define which elements should be selected
 	 * @return the entire NoSqlString to be used in a OrientDB query
 	 */
 	public String build(DbFilterConfig config) {
 		String whereContent = buildWhereClause(config);
 		String whereClause = "";
 		if(!whereContent.trim().isEmpty()) {
 			whereClause = " where " + whereContent;
 		}
 		return "select from " + DbConstants.CLS_REPRESENTATION + whereClause;
 	}
 	
 	
 	/**
 	 * Create string such as "SELECT FROM [#5:0, #7:2]" to be used as a query-
 	 * 
 	 * @param dbIds the list of dbIds in format such as #5:0
 	 * @return the NoSQL string
 	 */
 	public String build(Collection<String> dbIds) {
 		return "SELECT FROM " + buildIdList(dbIds);
 	}
 	
 	/**
 	 * Create string such as "[#5:0, #7:2]" to be used in a query
 	 * 
 	 * @param dbIds the list of dbIds in format such as #5:0
 	 * @return the idList
 	 */
 	public String buildIdList(Collection<String> dbIds) {
 		return dbIds.toString();
 	}
 
 	/**
 	 * Builds the where substring of a NoSql query. It assumes that the FROM clause reads representations.
 	 * The prefix "WHERE" is already included, if the configuration leads to conditions.
 	 * Combines all created snippets with "and", the snippets themselves are separated by "or" internally,
 	 * e.g. "WHERE revision.latestRevision = 'true' and (notation like 'EPC' or notation like 'BPMN')"
 	 * 
 	 * @param config to define which elements should be selected
 	 * @return the NoSql substring to be used as a where clause in a OrientDB query
 	 */
 	private String buildWhereClause(DbFilterConfig config) {
 		String pathToRev = DbConstants.ATTR_REVISION + ".";
 		String pathToMd = pathToRev + DbConstants.ATTR_METADATA;
 		String pathToModel = pathToRev + DbConstants.ATTR_MODEL + ".";
 		
 		//build a list of snippets to combine later
 		ArrayList<String> snippets = new ArrayList<String>();
 		addSnippetForLatestRevision(config.latestRevisionsOnly(), pathToRev + DbConstants.ATTR_LATEST_REVISION, snippets);
 		
 		boolean allowSubstrings = false;
 		addSnippetForStrings(config.getFormats(), DbConstants.ATTR_FORMAT, allowSubstrings, snippets); //TODO substrings?
 		addSnippetForStrings(config.getNotations(), DbConstants.ATTR_NOTATION, allowSubstrings, snippets);  //TODO substrings?
 		addSnippetForStrings(config.getOrigins(), pathToModel + DbConstants.ATTR_ORIGIN, allowSubstrings, snippets); 
 		addSnippetForStrings(config.getImportedIds(), pathToModel + DbConstants.ATTR_IMPORTED_ID, allowSubstrings, snippets); 
 		addSnippetForMetadataKeys(config.getMetadataKeys(), pathToMd, snippets);
 		
 		allowSubstrings = true;
		addSnippetForStrings(config.getLanguages(), DbConstants.ATTR_LANGUAGE, allowSubstrings, snippets);
 		addSnippetForStrings(config.getAuthors(), pathToRev + DbConstants.ATTR_AUTHOR, allowSubstrings, snippets);
 		addSnippetForStrings(config.getTitles(), pathToModel + DbConstants.ATTR_TITLE, allowSubstrings, snippets);
 		addSnippetForMetadataEntries(config.getMetadataEntries(), pathToMd, snippets);
 		addSnippetForMetadataValues(config.getMetadataValues(), pathToMd, snippets);
 		// orientdb bug: metadatavalues needs to be the last in the where clause
 		
 		
 		if(snippets.isEmpty()) {
 			return "";
 		}
 		
 		//combine snippets
 		String whereClause = "";
 		int cnt = 0;
 		for(String snippet : snippets) {
 			if(cnt > 0) {
 				whereClause += " and ";
 			}
 			whereClause += snippet;
 			cnt++;
 		}
 			
 		return whereClause;
 	}	
 	
 	/**
 	 * If latestRevision is set to true, add a snippet in form of "revision.latestRevision = 'true'"
 	 * 
 	 * @param latestRevisionsOnly
 	 * @param pathToLatestRevision
 	 * @param snippets
 	 */
 	private void addSnippetForLatestRevision(boolean latestRevisionsOnly, String pathToLatestRevision, ArrayList<String> snippets) {
 		if(!latestRevisionsOnly) {
 			return;
 		}
 		snippets.add(pathToLatestRevision + " = 'true'");
 	}
 	
 	/**
 	 * Create a snippet to collect metadata keys e.g.
 	 * "revision.metadata containskey 'akey'"
 	 * 
 	 * @param keys
 	 * @param pathFromRepToMd
 	 * @param snippets
 	 */
 	private void addSnippetForMetadataKeys(ArrayList<String> keys, String pathFromRepToMd, ArrayList<String> snippets) {
 		String snippet = "";
 		int count = 0;
 		
 		for(String key : keys) {
 			if(count > 0) {
 				snippet += " or ";
 			}
 			snippet += "(" + pathFromRepToMd + " containskey '" + key + "')";
 			count++;
 		}
 		
 		if(!snippet.isEmpty()) {
 			snippets.add(snippet);
 		}
 	}
 	
 	/**
 	 * Create a snippet to collect metadata values e.g.
 	 * "containsValueSubstrings(revision.metadata, ['v1', 'v2']) = 'true'"
 	 * which is used with substring and or semantik
 	 * see {@link PersistenceApiOrientDbObj}
 	 *  
 	 * @param values
 	 * @param pathFromRepToMd
 	 * @param snippets
 	 */
 	private void addSnippetForMetadataValues(ArrayList<String> values, String pathFromRepToMd, ArrayList<String> snippets) {
 		if(values.isEmpty()) {
 			return;
 		}
 		
 		//create string such as ['a','b']
 		String listAsString = "";
 		int count = 0;
 		for(String value : values) {
 			if(count > 0) 
 				listAsString += ",";
 			listAsString += "'" + value + "'";
 			count++;
 		}
 		listAsString = "[" + listAsString + "]";
 		
 		//uses "or" semantic
 		snippets.add("containsValueSubstrings(" + pathFromRepToMd + "," + listAsString + ") = 'true'");
 	}
 	
 	/**	 
 	 * Create a snippet to collect metadata entries e.g.
 	 * "revision.metadata[aKey] like '%value%'"
 	 * 
 	 * @param entries
 	 * @param pathFromRepToMd
 	 * @param snippets
 	 */
 	private void addSnippetForMetadataEntries(HashMap<String, String> entries, String pathFromRepToMd, ArrayList<String> snippets) {
 		String snippet = "";
 		int count = 0;
 		
 		for(Entry<String,String> e : entries.entrySet()) {
 			if(count > 0) {
 				snippet += " or ";
 			}
 			snippet += "(" + pathFromRepToMd + "[" + e.getKey() + "] like '%" + e.getValue() + "%')";
 			count++;
 		}
 		
 		if(!snippet.isEmpty()) {
 			snippets.add(snippet);
 		}
 	}
 
 	/**
 	 * Create a snippet to collect strings e.g.
 	 * "(revision.model.title like '%keyword%' or revision.model.title like '%keyword2%')"
 	 * 
 	 * @param strings 
 	 * 			the keywords to search for
 	 * @param attributeName
 	 * 			the path to the field to search in
 	 * @param allowSubstring
 	 * 			if set to true, will add % around the criterion
 	 * @param snippets
 	 * 			the snippets to add the result to
 	 */
 	private void addSnippetForStrings(ArrayList<String> strings, String attributeName, boolean allowSubstring, ArrayList<String> snippets) {
 		String snippet = "";
 		int count = 0;
 		String p = "";
 		if(allowSubstring) {
 			p = "%";
 		}
 		
 		for(String value : strings) {
 			if(count > 0) {
 				snippet += " or ";
 			}
 			snippet += "(" + attributeName + " like '" + p + value + p + "')";
 			count++;
 		}
 		if(strings.size() >= 2) { 
 			snippet = "(" + snippet + ")"; //it has at least 1 'or'
 		}
 		
 		if(!snippet.isEmpty()) {
 			snippets.add(snippet);
 		}
 	}	
 }
