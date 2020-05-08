 package com.technosophos.rhizome.repository.lucene;
 
 /**
  * Basic Lucene elements.
  * Since multiple classes need access to the same fields, these have been abstracted
  * out into this class. This should be imported with the <code>static</code> modifier.
  * @author mbutcher
  *
  */
 public class LuceneElements {
 	/**
 	 * Name of the context key that will contain the path information.
 	 * This is usually "indexpath".
 	 */
	public static String LUCENE_INDEX_PATH_PARAM = "index_path";
 	
 	public static String LUCENE_BODY_FIELD = "_body";
 	public static String LUCENE_DOCID_FIELD = "_docid";
 	public static String LUCENE_RELATION_FIELD = "_relation";
 	public static String LUCENE_EXTENSION_FIELD = "_extension";
 	
 	/**
 	 * String used to separate DocID and relation name. Note that
 	 * this character should be escaped in the docID if it is present.
 	 * 
 	 * Currently, the string is the percent symbol twice.
 	 */
 	public static String RELATION_SEPARATOR = "%%";
 	
 	// Unused:
 	public static String METADATA_FIELD_PREFIX = "metadatum:";
 	public static String RELATION_FIELD_PREFIX = "relation:";
 }
