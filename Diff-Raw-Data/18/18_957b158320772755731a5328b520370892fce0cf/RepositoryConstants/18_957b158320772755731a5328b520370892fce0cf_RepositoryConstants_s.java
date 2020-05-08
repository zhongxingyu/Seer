 /**
  * 
  */
 package org.ariadne_eu.utils.config;
 
 /**
  * @author gonzalo
  *
  */
 public class RepositoryConstants {
 
 	
 	
 	
 	
 	public static final String REPO_USERNAME = "repository.username";
 	public static final String REPO_PASSWORD = "repository.password";
 	public static final String REPO_STATICKEY = "repository.statickey";
 	public static final String REPO_UPDATE_TARGET = "repository.username";
 	public static final String REPO_UPDATE_SESSION = "repository.password";
 	public static final String REPO_LOG4J_DIR = "repository.log4j.directory";
 	public static final String REPO_LOG4J_FILENAME = "repository.log4j.filename";
 	
 	
 	
 	
 	
 	public static final String SR_LUCENE_INDEXDIR = "search.lucene.indexdir";
 	public static final String SR_LUCENE_HANDLER = "search.lucene.handler";
 	public static final String SR_LUCENE_ANALYZER = "search.lucene.analyzer";
 	public static final String SR_LUCENE_REINDEX = "search.lucene.reindex";
 	public static final String SR_LUCENE_REINDEX_MAXQRYRESULTS = "search.lucene.reindex.maxqueryresults";
 	public static final String SR_XPATH_QRY_ID = "search.xpath.query.identifier";
 	public static final String SR_LUCENE_HANDLER_MACE = "search.lucene.handler.mace";
 	public static final String SR_SOLR_DATADIR = "search.solr.dataDir";
	public static final String SR_SOLR_INSTANCEDIR = "search.solr.instancedir";
 	public static final String SR_SOLR_FACETFIELD = "search.solr.facetfield";
 	
 	
 
 	
 	
 	public static final String MD_INSERT_IMPLEMENTATION = "mdstore.insert.implementation";
 	public static final String MD_DELETE_IMPLEMENTATION = "mdstore.delete.implementation";
 	public static final String MD_QUERY_IMPLEMENTATION = "mdstore.query.implementation";
 	
 	public static final String MD_DB_URI = "mdstore.db.uri";
 	public static final String MD_DB_USERNAME = "mdstore.db.username";
 	public static final String MD_DB_PASSWORD = "mdstore.db.password";
 	public static final String MD_DB_XMLDB_LOC = "mdstore.db.xmldb.loc";
 	public static final String MD_DB_XMLDB_SQL_TABLENAME = "mdstore.db.xmldb.sql.tablename";
 	public static final String MD_DB_XMLDB_SQL_COLUMNNAME = "mdstore.db.xmldb.sql.columnname";
 	public static final String MD_DB_XMLDB_SQL_IDCOLUMNNAME = "mdstore.db.xmldb.sql.idcolumnname";
 	
 	public static final String MD_SPIFS_DIR = "mdstore.spifs.dir";
 	
 	public static final String MD_SPIFWD_SM_URL = "mdstore.spiforward.sm.url";
 	public static final String MD_SPIFWD_SPI_URL = "mdstore.spiforward.spi.url";
 	public static final String MD_SPIFWD_SM_USERNAME = "mdstore.spiforward.sm.username";
 	public static final String MD_SPIFWD_SM_PASSWORD = "mdstore.spiforward.sm.password";
 	
 	public static final String MD_INSERT_XMLNS_XSD = "mdstore.insert.xmlns.xsd";
 	public static final String MD_XQUERY_WHOLEWORD = "mdstore.xquery.wholeword";
 //	public static final String MD_RF_RLOM_URL = "mdstore.rf.rlom.url";
 //	public static final String MD_RF_RLOM_RMETRIC = "mdstore.rf.rlom.rankingmetric";
 	
 	
 
 	
 	
 	public static final String CNT_RETREIVE_IMPLEMENTATION = "cntstore.retrieve.implementation";
 	public static final String CNT_INSERT_IMPLEMENTATION = "cntstore.insert.implementation";
 	public static final String CNT_DB_URI = "cntstore.db.uri";
 	public static final String CNT_DB_USERNAME = "cntstore.db.username";
 	public static final String CNT_DB_PASSWORD = "cntstore.db.password";
 	public static final String CNT_DB_XMLDB_SQL_TABLENAME = "cntstore.db.xmldb.sql.tablename";
 	public static final String CNT_DB_XMLDB_SQL_COLUMNNAME = "cntstore.db.xmldb.sql.columnname";
 	public static final String CNT_DB_XMLDB_SQL_IDCOLUMNNAME = "cntstore.db.xmldb.sql.idcolumnname";
 	public static final String CNT_DR_BASEPATH = "cntstore.dr.basepath";
 	public static final String CNT_SPIFWD_SM_URL = "cntstore.spiforward.sm.url";
 	public static final String CNT_SPIFWD_SPI_URL = "cntstore.spiforward.spi.url";
 	public static final String CNT_SPIFWD_SM_USERNAME = "cntstore.spiforward.sm.username";
 	public static final String CNT_SPIFWD_SM_PASSWORD = "cntstore.spiforward.sm.password";
 	public static final String CNT_MD_XPATHQRY_LOCATION = "cntstore.md.xpathquery.location";
 	public static final String CNT_LUCENE_INDEXDIR = "cntstore.lucene.indexdir";
 	
 	
 	
 	
 	
 	public static final String OAICAT_SERVER_CATALOG_SEC2LIVE = "oaicat.server.catalog.seconds2live";
 	public static final String OAICAT_SERVER_CATALOG_GRANULARITY = "oaicat.server.catalog.granularity";
 	
 	public static final String OAICAT_IDENTIFY_EMAIL = "oaicat.identify.email";
 	public static final String OAICAT_IDENTIFY_REPONAME = "oaicat.identify.reponame";
 	public static final String OAICAT_IDENTIFY_EARLYDATE = "oaicat.identify.earliestdatestamp";
 	public static final String OAICAT_IDENTIFY_DELREC = "oaicat.identify.deletedrecord";
 	public static final String OAICAT_IDENTIFY_REPOID = "oaicat.identify.repoid";
 	public static final String OAICAT_IDENTIFY_REPODESC = "oaicat.identify.description"; 
 	public static final String OAICAT_IDENTIFY_SAMPLEID = "oaicat.identify.sampleid";
 	
 	public static final String OAICAT_CROSSWALK_ETDMS = "oaicat.crosswalk.etdms";
 	public static final String OAICAT_CROSSWALK_DC = "oaicat.crosswalk.dc";
 	public static final String OAICAT_CROSSWALK_LOM = "oaicat.crosswalk.lom";
 	
 	public static final String OAICAT_SERVER_CATALOG_MAXLSTSIZE = "oaicat.server.catalog.maxlistsize";
 
 	public static final String OAICAT_SERVER_CATALOG_CLASS = "oaicat.server.catalog.class";
 	public static final String OAICAT_SERVER_CATALOG_RECORD_CLASS = "oaicat.server.catalog.record.class";
 	public static final String OAICAT_SERVER_CATALOG_MDFIELD = "oaicat.server.catalog.field.md";
 	public static final String OAICAT_SERVER_CATALOG_DATEFIELD = "oaicat.server.catalog.field.date";
 	public static final String OAICAT_SERVER_CATALOG_IDFIELD = "oaicat.server.catalog.field.id";
 	public static final String OAICAT_SERVER_CATALOG_REPOID = "oaicat.server.catalog.repoId";
 	public static final String OAICAT_SERVER_CATALOG_SETFIELD = "oaicat.server.catalog.field.set";
 	public static final String OAICAT_SERVER_CATALOG_DATECOLUMN = "oaicat.server.catalog.db.column.date";
 	public static final String OAICAT_SERVER_CATALOG_FS_EXT = "oaicat.server.catalog.fs.ext";
 	
 	
 	public static final String OAICAT_SETS = "oaicat.sets";
 	public static final String OAICAT_SETS_ID = "repoid";
 
 	public static final String OAICAT_HANDLER_BASEURL = "oaicat.handler.baseuRL";
 	public static final String OAICAT_HANDLER_STYLESHEET = "oaicat.handler.stylesheet";
 	public static final String OAICAT_HANDLER_OLDBROWSER = "oaicat.handler.render4oldbrowsers";
 	public static final String OAICAT_HANDLER_USEIDSHEME = "oaicat.handler.useoaischeme";
 	
 	public static final String MACE_OAI_ALOE_TARGET = "mace.oai.aloe.target";
 	public static final String MACE_OAI_ALOE_MDPREFIX = "mace.oai.aloe.mdprefix";
 	
 	public static final String HEADER_X_FORWARDED_FOR ="X-FORWARDED-FOR";
 
 }
