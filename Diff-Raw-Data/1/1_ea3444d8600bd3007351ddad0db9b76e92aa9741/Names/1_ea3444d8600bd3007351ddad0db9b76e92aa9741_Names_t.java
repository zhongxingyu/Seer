 /**
  *
  * University of Illinois/NCSA
  * Open Source License
  *
  * Copyright (c) 2008, NCSA.  All rights reserved.
  *
  * Developed by:
  * The Automated Learning Group
  * University of Illinois at Urbana-Champaign
  * http://www.seasr.org
  *
  * Permission is hereby granted, free of charge, to any person obtaining
  * a copy of this software and associated documentation files (the
  * "Software"), to deal with the Software without restriction, including
  * without limitation the rights to use, copy, modify, merge, publish,
  * distribute, sublicense, and/or sell copies of the Software, and to
  * permit persons to whom the Software is furnished to do so, subject
  * to the following conditions:
  *
  * Redistributions of source code must retain the above copyright
  * notice, this list of conditions and the following disclaimers.
  *
  * Redistributions in binary form must reproduce the above copyright
  * notice, this list of conditions and the following disclaimers in
  * the documentation and/or other materials provided with the distribution.
  *
  * Neither the names of The Automated Learning Group, University of
  * Illinois at Urbana-Champaign, nor the names of its contributors may
  * be used to endorse or promote products derived from this Software
  * without specific prior written permission.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
  * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
  * IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE
  * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
  * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
  * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
  *
  */
 
 package org.seasr.meandre.components.tools;
 
 /**
  * This abstract class just provide a list of standardized port and property names.
  *
  * @author Xavier Llor&agrave;
  *
  */
 public abstract class Names {
 
 	// PORTS
     public static final String PORT_AUTHOR_LIST = "author_list";
     public static final String PORT_CLUSTER_MODEL = "cluster_model";
 	public static final String PORT_CONTEXT_VECTOR = "context_vector";
 	public static final String PORT_CONNECTION = "connection";
 	public static final String PORT_DICTIONARY = "dictionary";
 	public static final String PORT_DIRECTORY = "directory";
 	public static final String PORT_DOC_TITLE = "document_title";
 	public static final String PORT_DOCUMENT = "document";
 	public static final String PORT_ERROR = "error";
 	public static final String PORT_FILENAME = "file_name";
 	public static final String PORT_GRAPH = "graph";
 	public static final String PORT_HTML = "html";
    public static final String PORT_INPUT_STREAM = "input_stream";
 	public static final String PORT_JAVA_STRING = "java_string";
 	public static final String PORT_JSON = "json";
 	public static final String PORT_LATITUDE_VECTOR = "latitude_vector";
     public static final String PORT_LOCATION = "location";
 	public static final String PORT_LOCATION_VECTOR = "location_vector";
 	public static final String PORT_LONGITUDE_VECTOR = "longitude_vector";
 	public static final String PORT_MAX_VALUE = "max_value";
 	public static final String PORT_MAX_YEAR = "max_year";
 	public static final String PORT_META_TUPLE = "meta_tuple";
 	public static final String PORT_MIME_TYPE = "mime_type";
 	public static final String PORT_MIN_VALUE = "min_value";
 	public static final String PORT_MIN_YEAR = "min_year";
 	public static final String PORT_NO_DATA = "no_data";
 	public static final String PORT_OBJECT = "object";
 	public static final String PORT_OBJECT_2 = "object2";
 	public static final String PORT_OBJECT_3 = "object3";
 	public static final String PORT_OBJECT_4 = "object4";
 	public static final String PORT_OBJECT_5 = "object5";
 	public static final String PORT_RAW_DATA = "raw_data";
 	public static final String PORT_REQUEST_DATA = "request_data";
 	public static final String PORT_RESPONSE_HANDLER = "response_handler";
 	public static final String PORT_SEMAPHORE = "semaphore";
 	public static final String PORT_SENTENCES = "sentences";
 	public static final String PORT_STEMMED_WORDS = "stemmed_words";
 	public static final String PORT_TABLE = "table";
 	public static final String PORT_TEXT = "text";
 	public static final String PORT_TEXT_2 = "text2";
 	public static final String PORT_TEXT_FOUND = "text_found";
 	public static final String PORT_TITLE = "title";
 	public static final String PORT_TOKEN_BLACKLIST = "tokens_blacklist";
 	public static final String PORT_TOKEN_COUNTS = "token_counts";
 	public static final String PORT_TOKEN_COUNTS_REFERENCE = "token_counts_reference";
 	public static final String PORT_TOKEN_MAP = "token_map";
 	public static final String PORT_TOKENIZED_SENTENCES = "tokenized_sentences";
 	public static final String PORT_TOKENS = "tokens";
 	public static final String PORT_TUPLE  = "tuple";
 	public static final String PORT_TUPLES = "tuples";
 	public static final String PORT_WORDS = "words";
 	public static final String PORT_XML = "xml";
 	public static final String PORT_XSL = "xsl";
 
 	// PROPERTIES
 	public static final String PROP_APPEND_TIMESTAMP = "append_timestamp";
     public static final String PROP_BASE_DN = "base_dn";
 	public static final String PROP_BASE_URI = "base_uri";
 	public static final String PROP_BOTTOM_N = "bottom_n";
 	public static final String PROP_COMMAND = "command";
 	public static final String PROP_CONNECTION_TIMEOUT = "connection_timeout";
 	public static final String PROP_CONSUMER_KEY = "consumer_key";
 	public static final String PROP_CONSUMER_SECRET = "consumer_secret";
 	public static final String PROP_COUNT = "count";
 	public static final String PROP_CSS = "css";
 	public static final String PROP_DEBUG_LEVEL = "debug_level";
 	public static final String PROP_DEFAULT = "default_value";
 	public static final String PROP_DEFAULT_FOLDER = "default_folder";
 	public static final String PROP_ENCODING = "encoding";
 	public static final String PROP_ENTITIES = "entities";
 	public static final String PROP_ERROR_HANDLING = "ignore_errors";
 	public static final String PROP_EXPRESSION = "expression";
 	public static final String PROP_FIELD_NAME ="field_name";
 	public static final String PROP_FILENAME = "filename";
 	public static final String PROP_FILTER_REGEX  = "filter_regex";
 	public static final String PROP_FIND = "find";
 	public static final String PROP_FIND_2 = "find2";
 	public static final String PROP_FIND_3 = "find3";
 	public static final String PROP_FIND_4 = "find4";
 	public static final String PROP_FONT_NAME = "font_name";
 	public static final String PROP_GOOGLE_APIS_KEY = "google_apis_key";
 	public static final String PROP_HAS_COLUMN_LABEL_HEADER = "has_column_label_header";
 	public static final String PROP_HAS_COLUMN_TYPE_HEADER = "has_column_type_header";
 	public static final String PROP_HEADER = "header";
 	public static final String PROP_HEIGHT = "height";
 	public static final String PROP_HOSTNAME = "hostname";
 	public static final String PROP_ID = "id";
 	public static final String PROP_ITERATIONS = "iterations";
 	public static final String PROP_JNDI_NAME = "jndi_name";
 	public static final String PROP_LANGUAGE = "language";
 	public static final String PROP_LOCAL_FILENAME_GET = "local_filename_get";
 	public static final String PROP_LOCAL_FILENAME_PUT = "local_filename_put";
 	public static final String PROP_LOCATION = "location";
 	public static final String PROP_MAX_SIZE = "max_size";
 	public static final String PROP_MAX_VALUE = "max_value";
 	public static final String PROP_MESSAGE = "message";
     public static final String PROP_MIN_SIZE = "min_size";
     public static final String PROP_MIN_VALUE = "min_value";
     public static final String PROP_N_SLICES = "n_slices";
     public static final String PROP_N_STREAMS = "n_streams";
 	public static final String PROP_N_TOP_SENTENCES = "n_top_sentences";
 	public static final String PROP_N_TOP_TOKENS = "n_top_tokens";
 	public static final String PROP_OFFSET = "offset";
 	public static final String PROP_ORDERED = "ordered";
 	public static final String PROP_OUTPUT_HTML = "output_html";
 	public static final String PROP_PAGE_SIZE = "page_size";
 	public static final String PROP_PASSPHRASE = "passphrase";
 	public static final String PROP_PASSWORD = "password";
 	public static final String PROP_PORT_NUMBER = "port_number";
 	public static final String PROP_PROPERTIES = "properties";
 	public static final String PROP_QUERY = "query";
 	public static final String PROP_RDF_DIALECT = "rdf_dialect";
 	public static final String PROP_READ_TIMEOUT = "read_timeout";
 	public static final String PROP_RECURSIVE = "recursive";
 	public static final String PROP_REFRESH = "refresh_after_execute";
 	public static final String PROP_REMOTE_FILENAME_GET = "remote_filename_get";
 	public static final String PROP_REMOTE_FILENAME_PUT = "remote_filename_put";
 	public static final String PROP_REMOVE = "remove";
 	public static final String PROP_REPLACE = "replace";
 	public static final String PROP_REPLACE_2 = "replace2";
 	public static final String PROP_REPLACE_3 = "replace3";
 	public static final String PROP_REPLACE_4 = "replace4";
 	public static final String PROP_REPLICATION_METHOD = "replication_method_name";
 	public static final String PROP_REPLICATION_MODE = "replication_mode";
 	public static final String PROP_SEED = "seed";
 	public static final String PROP_SEPARATOR = "separator";
 	public static final String PROP_SHOW_COUNT = "show_count";
 	public static final String PROP_TABLE = "table";
 	public static final String PROP_TABLE_FACTORY = "table_factory";
 	public static final String PROP_TAG = "tag";
 	public static final String PROP_TEMPLATE = "template";
 	public static final String PROP_TIMES = "times";
 	public static final String PROP_TITLE = "title";
 	public static final String PROP_URL_CONTEXT_PATH = "url_context_path";
 	public static final String PROP_USERNAME = "username";
 	public static final String PROP_WIDTH = "width";
 	public static final String PROP_WRAP_STREAM = "wrap_stream";
 	public static final String PROP_XML_LOCATION = "xml_location";
 	public static final String PROP_YAHOO_API_KEY = "yahoo_api_key";
 }
