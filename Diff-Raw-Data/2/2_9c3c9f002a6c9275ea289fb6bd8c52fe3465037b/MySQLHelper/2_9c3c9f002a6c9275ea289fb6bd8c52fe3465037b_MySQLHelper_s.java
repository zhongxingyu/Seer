 package edu.columbia.ldpd.hrwa.mysql;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Scanner;
 
 import org.apache.commons.lang.StringUtils;
 
 import edu.columbia.ldpd.hrwa.HrwaManager;
 import edu.columbia.ldpd.hrwa.HrwaSiteRecord;
 import edu.columbia.ldpd.hrwa.processorrunnables.ArchiveFileProcessorRunnable;
 
 public class MySQLHelper {
 	
 	public static final String HRWA_MANAGER_TODO_DELETE = "DELETED";
 	public static final String HRWA_MANAGER_TODO_UPDATED = "UPDATED";
 	public static final String HRWA_MANAGER_TODO_NEW = "NEW";
 	public static final String HRWA_MANAGER_TODO_NOINDEX = "NOINDEX";
 	public static final String HRWA_MANAGER_TODO_FIELD_NAME = "hrwa_manager_todo";
 	
 	private static Connection staticConnWithAutoCommitOn = getNewDBConnection(true);
 	
 	public static Connection getNewDBConnection(boolean autoCommit) {
 
 		Connection newConn = null;
 
 		//Step 1: Load MySQL Driver
 		try {
             // This is a test to check that the driver is available.
 			// The newInstance() call is a work around for some broken Java implementations.
             Class.forName("com.mysql.jdbc.Driver").newInstance();
         } catch (Exception ex) { System.err.println("Could not load the mysql driver!"); }
 
 		//Step 2: Establish connection
 		
 		String url = "jdbc:mysql://" + HrwaManager.mysqlUrl + "/" + HrwaManager.mysqlDatabase;
 		try {
 			newConn = DriverManager.getConnection(url, HrwaManager.mysqlUsername, HrwaManager.mysqlPassword);
 		} catch (SQLException ex) {
 			HrwaManager.writeToLog("Error: Could not connect to MySQL at url:" + url, true, HrwaManager.LOG_TYPE_ERROR);
 		    System.out.println("SQLException: " + ex.getMessage());
 		    System.out.println("SQLState: " + ex.getSQLState());
 		    System.out.println("VendorError: " + ex.getErrorCode());
 		    System.exit(HrwaManager.EXIT_CODE_ERROR);
 		}
 
 		try {
 			newConn.setAutoCommit(autoCommit);
 		} catch (SQLException e) { e.printStackTrace(); System.exit(HrwaManager.EXIT_CODE_ERROR); }
 
 		return newConn;
 	}
 	
 	public static void createWebArchiveRecordsTableIfItDoesNotExist() throws SQLException {
 		
 		PreparedStatement pstmt0 = staticConnWithAutoCommitOn.prepareStatement(
 			"CREATE TABLE IF NOT EXISTS `" + HrwaManager.MYSQL_WEB_ARCHIVE_RECORDS_TABLE_NAME + "` (" +
 			"  `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Auto-incremented unique numeric identifier for MySQL convenience.'," +
 			"  `ip` varchar(39) NOT NULL COMMENT 'IPv4 or IPv6 address of the host of the record crawled.'," +
 			"  `url` varchar(2100) NOT NULL COMMENT 'Original url of this crawled record.'," +
 			"  `digest` char(37) DEFAULT NULL COMMENT 'SHA1 digest of the crawled record.'," +
 			"  `archive_file` varchar(255) NOT NULL COMMENT 'Name of the archive file (warc/arc) that this record came from.'," +
 			"  `offset_in_archive_file` bigint(20) unsigned NOT NULL COMMENT 'This is the byte offset address of the record in the archive file.'," +
 			"  `length` bigint(20) unsigned NOT NULL COMMENT 'Size of the content returned in the HTTP response in bytes. Largest will probably be video.'," +
 			"  `record_date` char(14) NOT NULL COMMENT 'Crawl date for this record.'," +
 			"  `blob_path` varchar(500) DEFAULT NULL COMMENT 'Filesystem path to the blob data associated with this record.  Header info is the blob_path + .header'," +
 			"  `mimetype_from_header` varchar(255) DEFAULT NULL COMMENT 'Mimetype defined in the archive file header.'," +
 			"  `mimetype_detected` varchar(100) DEFAULT NULL COMMENT 'Mimetype detected by our archive_to_mysql indexer, using Apache Tika.  NULL if mimetype could not be detected.'," +
 			"  `reader_identifier` varchar(255) NOT NULL COMMENT 'Full filesystem path to the warc/arc file associated with this record (at the time when this record was indexed).'," +
 			"  `record_identifier` varchar(2115) NOT NULL COMMENT 'Unique identifier for this record.  Of the format: record_date/url'," +
 			"  `archived_url` varchar(2200) DEFAULT NULL COMMENT 'Wayback url to this archive record.  Note that this url includes the record_identifier.'," +
 			"  `status_code` int(3) NOT NULL COMMENT 'HTTP response status code at record crawl time.'," +
 			"  `hoststring` varchar(255) DEFAULT NULL COMMENT 'Truncated url, only including hostname and removing www, www1, www2, etc. if present.'," +
 			"  `site_id` smallint(5) unsigned DEFAULT NULL COMMENT 'Foreign key to sites table, linking this record to a recognized website (or NULL if there is no match with a site record in the sites table).'," +
 			"  `load_timestamp` bigint(20) NOT NULL COMMENT 'Timestamp indicating when this record was indexed and inserted into mysql.'," +
 			"  `linked_via_related_host` tinyint(1) NOT NULL," +
 			"  `hrwa_manager_todo` varchar(50) DEFAULT NULL," +
 			"  PRIMARY KEY (`id`)," +
 			"  UNIQUE KEY `archive_file_and_offset` (`archive_file`,`offset_in_archive_file`)," +
 			"  KEY `mimetype_from_header` (`mimetype_from_header`)," +
 			"  KEY `mimetype_detected` (`mimetype_detected`)," +
 			"  KEY `status_code` (`status_code`)," +
 			"  KEY `hoststring` (`hoststring`)," +
 			"  KEY `site_id` (`site_id`)," +
 			"  KEY `record_date` (`record_date`)," +
 			"  KEY `linked_via_related_host` (`linked_via_related_host`)," +
 			"  KEY `hrwa_manager_todo` (`hrwa_manager_todo`)," +
 			"  KEY `archive_file` (`archive_file`)" +
 			") ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;"
 		);
 		
 		pstmt0.execute();
 		pstmt0.close();
 		
 	}
 	
 	public static void createSitesTableIfItDoesNotExist() throws SQLException {
 		
 		PreparedStatement pstmt0 = staticConnWithAutoCommitOn.prepareStatement(
 			"CREATE TABLE IF NOT EXISTS `" + HrwaManager.MYSQL_SITES_TABLE_NAME + "` (" +
 			"  `id` int(11) unsigned NOT NULL AUTO_INCREMENT," +
 			"  `bib_key` varchar(10) NOT NULL," +
 			"  `creator_name` varchar(255) NOT NULL," +
 			"  `hoststring` varchar(255) NOT NULL," +
 			"  `organization_type` varchar(255) NOT NULL," +
 			"  `organization_based_in` varchar(255) NOT NULL," +
 			"  `geographic_focus` varchar(2000) NOT NULL," +
 			"  `language` varchar(2000) NOT NULL," +
 			"  `original_urls` varchar(2000) NOT NULL," +
 			"  `marc_005_last_modified` char(16) NOT NULL," +
 			"  `" + MySQLHelper.HRWA_MANAGER_TODO_FIELD_NAME + "` varchar(50) DEFAULT NULL," +
 			"  PRIMARY KEY (`id`)," +
 			"  UNIQUE KEY `bib_key` (`bib_key`)," +
 			"  KEY `creator_name` (`creator_name`)," +
 			"  KEY `organization_type` (`organization_type`)," +
 			"  KEY `organization_based_in` (`organization_based_in`)," +
 			"  KEY `hoststring` (`hoststring`)," +
 			"  KEY `geographic_focus` (`geographic_focus`(255))," +
 			"  KEY `language` (`language`(255))," +
 			"  KEY `original_urls` (`original_urls`(255))," +
 			"  KEY `" + MySQLHelper.HRWA_MANAGER_TODO_FIELD_NAME + "` (`" + MySQLHelper.HRWA_MANAGER_TODO_FIELD_NAME + "`)" +
 			") ENGINE=InnoDB  DEFAULT CHARSET=utf8 AUTO_INCREMENT=1;" 
 		);
 		
 		pstmt0.execute();
 		pstmt0.close();
 		
 	}
 	
 	public static void createFullyIndexedArchiveFilesTableIfItDoesNotExist() throws SQLException {
 		
 		PreparedStatement pstmt0 = staticConnWithAutoCommitOn.prepareStatement(
 			"CREATE TABLE IF NOT EXISTS `" + HrwaManager.MYSQL_FULLY_INDEXED_ARCHIVE_FILES_TABLE_NAME + "` (" +
 			"  `archive_file_name` varchar(255) NOT NULL," +
 			"  `crawl_year_and_month` varchar(7) NOT NULL," +
 			"  PRIMARY KEY (`archive_file_name`)," +
 			"  KEY `crawl_year_and_month` (`crawl_year_and_month`)" +
 			") ENGINE=InnoDB DEFAULT CHARSET=utf8;"
 		);
 		
 		pstmt0.execute();
 		pstmt0.close();
 		
 	}
 	
 	public static void createMimetypeCodesTableIfItDoesNotExist() throws SQLException{
 		
 		PreparedStatement pstmt0 = staticConnWithAutoCommitOn.prepareStatement(
 			"CREATE TABLE IF NOT EXISTS `" + HrwaManager.MYSQL_MIMETYPE_CODES_TABLE_NAME + "` (" +
 			"`mimetype_detected` varchar(100) DEFAULT NULL," +
 			"`mimetype_code` varchar(100) DEFAULT NULL," +
 			"UNIQUE KEY `mimetype_detected` (`mimetype_detected`)," +
 			"KEY `mimetype_code` (`mimetype_code`)" +
 			") ENGINE=MyISAM DEFAULT CHARSET=utf8;"
 		);
 		
 		pstmt0.execute();
 		pstmt0.close();
 		
 		//And now we need to verify that there aren't any records in this table (if it already existed)
 		
 		PreparedStatement pstmt1 = staticConnWithAutoCommitOn.prepareStatement("SELECT COUNT(*) FROM " + HrwaManager.MYSQL_MIMETYPE_CODES_TABLE_NAME);
 		ResultSet resultSet = pstmt1.executeQuery();
 		if (resultSet.next()) {
 			if(resultSet.getInt(1) == 0) {
 				
 				PreparedStatement pstmt2 = staticConnWithAutoCommitOn.prepareStatement(
 					"INSERT INTO `" + HrwaManager.MYSQL_MIMETYPE_CODES_TABLE_NAME + "` (`mimetype_detected`, `mimetype_code`) VALUES" +
 					"('application/rsd+xml', 'DISCOVERY')," +
 					"('application/x-mspublisher', 'OFFICE')," +
 					"('application/xslt+xml', 'XML')," +
 					"('image/tiff', 'IMAGE')," +
 					"('application/vnd.ms-cab-compressed', 'BINARY')," +
 					"('application/pdf', 'PDF')," +
 					"('application/x-mobipocket-ebook', 'BINARY')," +
 					"('audio/x-wav', 'AUDIO')," +
 					"('application/vnd.ms-fontobject', 'OFFICE')," +
 					"('video/mpeg', 'VIDEO')," +
 					"('application/x-elc', 'BINARY')," +
 					"('application/vnd.oasis.opendocument.text', 'DOCUMENT')," +
 					"('audio/ogg', 'AUDIO')," +
 					"('application/vnd.ms-word.document.macroenabled.12', 'DOCUMENT')," +
 					"('image/x-raw-panasonic', 'IMAGE')," +
 					"('application/xml-dtd', 'XML')," +
 					"('application/x-msdownload', 'EXECUTABLE')," +
 					"('image/png', 'IMAGE')," +
 					"('text/html', 'HTML')," +
 					"('image/svg+xml', 'IMAGE')," +
 					"('application/x-hwp', 'BINARY')," +
 					"('image/vnd.adobe.photoshop', 'IMAGE')," +
 					"('application/x-silverlight-app', 'VIDEO')," +
 					"('application/rss+xml', 'WEB')," +
 					"('application/xhtml+xml', 'HTML')," +
 					"('application/postscript', 'BINARY')," +
 					"('application/x-gzip', 'BINARY')," +
 					"('application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', 'SPREADSHEET')," +
 					"('application/javascript', 'WEB')," +
 					"('image/gif', 'IMAGE')," +
 					"('application/vnd.wordperfect', 'DOCUMENT')," +
 					"('video/x-ms-asf', 'VIDEO')," +
 					"('text/uri-list', 'WEB')," +
 					"('application/vnd.openxmlformats-officedocument.presentationml.template', 'SLIDESHOW')," +
 					"('application/vnd.ms-powerpoint.presentation.macroenabled.12', 'SLIDESHOW')," +
 					"('application/vnd.google-earth.kml+xml', 'BINARY')," +
 					"('application/vnd.oasis.opendocument.presentation', 'SLIDESHOW')," +
 					"('application/xml', 'XML')," +
 					"('application/vnd.ms-powerpoint', 'SLIDESHOW')," +
 					"('application/DOCUMENT', 'DOCUMENT')," +
 					"('audio/midi', 'AUDIO')," +
 					"('application/vnd.apple.keynote', 'SLIDESHOW')," +
 					"('application/x-msmetafile', 'BINARY')," +
 					"('application/octet-stream', 'BINARY')," +
 					"('application/rdf+xml', 'XML')," +
 					"('text/plain', 'DOCUMENT')," +
 					"('application/rtf', 'DOCUMENT')," +
 					"('application/x-123', 'BINARY')," +
 					"('image/vnd.ms-modi', 'IMAGE')," +
 					"('application/x-font-printer-metric', 'BINARY')," +
 					"('application/x-rar-compressed', 'BINARY')," +
 					"('application/pls+xml', 'BINARY')," +
 					"('video/x-ms-wmv', 'VIDEO')," +
 					"('application/vnd.google-earth.kmz', 'BINARY')," +
 					"('text/vnd.graphviz', 'BINARY')," +
 					"('application/java-archive', 'BINARY')," +
 					"('audio/basic', 'AUDIO')," +
 					"('application/x-bittorrent', 'BINARY')," +
 					"('video/quicktime', 'VIDEO')," +
 					"('application/java-vm', 'BINARY')," +
 					"('application/ogg', 'AUDIO')," +
 					"('text/x-pascal', 'CODE')," +
 					"('application/vnd.lotus-organizer', 'CALENDAR')," +
 					"('application/x-font-ttf', 'WEB')," +
 					"('text/x-c', 'CODE')," +
 					"('text/x-asm', 'CODE')," +
 					"('application/vnd.quark.quarkxpress', 'IMAGE')," +
 					"('application/x-font-type1', 'BINARY')," +
 					"('NULL', 'NULL')," +
 					"('application/vnd.framemaker', 'BINARY')," +
 					"('text/x-fortran', 'CODE')," +
 					"('application/vnd.arastra.swi', 'IMAGE')," +
 					"('video/mp4', 'VIDEO')," +
 					"('video/x-m4v', 'VIDEO')," +
 					"('application/resource-lists+xml', 'BINARY')," +
 					"('application/vnd.adobe.air-application-installer-package+zip', 'EXECUTABLE')," +
 					"('image/jpeg', 'IMAGE')," +
 					"('message/rfc822', 'EMAIL')," +
 					"('application/x-wais-source', 'DISCOVERY')," +
 					"('image/x-icon', 'IMAGE')," +
 					"('application/vnd.ms-tnef', 'EMAIL')," +
 					"('application/vnd.dynageo', 'BINARY')," +
 					"('audio/x-pn-realaudio', 'AUDIO')," +
 					"('video/ogg', 'VIDEO')," +
 					"('text/csv', 'SPREADSHEET')," +
 					"('audio/x-ms-wma', 'AUDIO')," +
 					"('text/css', 'WEB')," +
 					"('video/x-flv', 'VIDEO')," +
 					"('application/vnd.openxmlformats-officedocument.presentationml.presentation', 'OFFICE')," +
 					"('application/x-sh', 'BINARY')," +
 					"('application/atom+xml', 'WEB')," +
 					"('application/x-tika-OFFICE', 'OFFICE')," +
 					"('application/x-ms-wmz', 'BINARY')," +
 					"('text/troff', 'DOCUMENT')," +
 					"('application/zip', 'BINARY')," +
 					"('application/json', 'WEB')," +
 					"('video/x-f4v', 'VIDEO')," +
 					"('application/vnd.openxmlformats-officedocument.wordprocessingml.document', 'DOCUMENT')," +
 					"('application/vnd.rn-realmedia', 'VIDEO')," +
 					"('application/vnd.ms-excel', 'SPREADSHEET')," +
 					"('application/x-shockwave-flash', 'WEB')," +
 					"('application/vnd.openxmlformats-officedocument.presentationml.slideshow', 'SLIDESHOW')," +
 					"('image/x-raw-sony', 'IMAGE')," +
 					"('image/x-ms-bmp', 'IMAGE')," +
 					"('audio/mpeg', 'AUDIO')," +
 					"('application/mac-binhex40', 'BINARY')," +
 					"('application/vnd.ms-excel.sheet.binary.macroenabled.12', 'SPREADSHEET')," +
 					"('application/vnd.lotus-1-2-3', 'SPREADSHEET')," +
 					"('application/epub+zip', 'BINARY')," +
 					"('text/x-uuencode', 'BINARY')," +
 					"('application/vnd.ms-pki.seccat', 'OFFICE')," +
 					"('application/x-tika-ooxml', 'OFFICE')," +
 					"('application/rls-services+xml', 'BINARY')," +
 					"('video/x-msvideo', 'VIDEO')," +
 					"('text/calendar', 'CALENDAR');"
 				);
 				
 				pstmt2.execute();
 				pstmt2.close();
 			}
 		 }
         
 		pstmt1.close();
 		
 	}
 	
 	/**
 	 * For latest version of related hosts table, see:
 	 * https://wiki.cul.columbia.edu/display/webresourcescollection/Related+hosts+for+HRWA+portal
 	 * @throws SQLException
 	 */
 	public static void updateRelatedHostsTableFromRelatedHostsFile() throws SQLException{
 		
 		HrwaManager.writeToLog("Updating related hosts table...", true, HrwaManager.LOG_TYPE_STANDARD);
 			
 		//Create table
 		PreparedStatement pstmt1 = staticConnWithAutoCommitOn.prepareStatement(
 			"CREATE TABLE IF NOT EXISTS `" + HrwaManager.MYSQL_RELATED_HOSTS_TABLE_NAME + "` (" +
 			"  `site_id` int(10) unsigned NOT NULL," +
 			"  `related_host` varchar(255) NOT NULL," +
 			"  `hrwa_manager_todo` varchar(50) DEFAULT NULL," +
 			"  UNIQUE KEY `related_host` (`related_host`)," +
 			"  KEY `site_id` (`site_id`)," +
 			"  KEY `hrwa_manager_todo` (`hrwa_manager_todo`)" +
 			") ENGINE=InnoDB DEFAULT CHARSET=utf8;"
 		);
 		
 		pstmt1.execute();
 		pstmt1.close();
 		
 		//Get the list of related hosts from the related_hosts file
 		HashMap<String, String> relatedHostsToSeedsMapFromRelatedHostsFile = new HashMap<String, String>();
 		try {
 			Scanner relatedHostsScanner = new Scanner(new File(HrwaManager.pathToRelatedHostsFile));
 			while (relatedHostsScanner.hasNextLine())
 			{
 			    String[] siteAndRelatedHostParts = relatedHostsScanner.nextLine().split(",");
 			    
 			    //Skip first line if it contains column titles, "seed" and "related_host"
			    if(siteAndRelatedHostParts[1].equals("seed")) {
 			    	continue;
 			    }
 			    
 			    relatedHostsToSeedsMapFromRelatedHostsFile.put(siteAndRelatedHostParts[1], siteAndRelatedHostParts[0]);
 			}
 		} catch (FileNotFoundException e) {
 			HrwaManager.writeToLog("Error: Could not find related hosts file at: " + HrwaManager.pathToRelatedHostsFile, true, HrwaManager.LOG_TYPE_ERROR);
 			System.exit(HrwaManager.EXIT_CODE_ERROR);
 		}
 		
 		//Compare the related hosts file list to what's already in MySQL
 		HashMap<String, Integer> relatedHostsMapFromMySQL = MySQLHelper.getRelatedHostsMap(null);
 		
 		//Determine which records should be deleted and which records will be marked as newly added
 		HashMap<String, String> relatedHostsToAddMappedToSiteSeeds = new HashMap<String, String>();
 		HashSet<String> relatedHostsToDelete = new HashSet<String>(relatedHostsMapFromMySQL.keySet());
 		relatedHostsToDelete.removeAll(relatedHostsToSeedsMapFromRelatedHostsFile.keySet());
 		
 		for(Map.Entry<String, String> entry : relatedHostsToSeedsMapFromRelatedHostsFile.entrySet()) {
 			String relatedHostActualHostFromFile = entry.getKey();
 			String relatedHostSiteSeedFromFile = entry.getValue();
 			if( ! relatedHostsMapFromMySQL.containsKey(relatedHostActualHostFromFile) ) {
 				relatedHostsToAddMappedToSiteSeeds.put(relatedHostActualHostFromFile, relatedHostSiteSeedFromFile);
 			}
 		}
 		
 		HrwaManager.writeToLog("New related hosts to add: " + relatedHostsToAddMappedToSiteSeeds.size(), true, HrwaManager.LOG_TYPE_STANDARD);
 		HrwaManager.writeToLog("Related hosts to delete: " + relatedHostsToDelete.size(), true, HrwaManager.LOG_TYPE_STANDARD);
 		
 		//First delete any related hosts that need to be deleted:
 		if(relatedHostsToDelete.size() > 0) {
 			String relatedHostRecordsDeletionStatement = "DELETE FROM `" + HrwaManager.MYSQL_RELATED_HOSTS_TABLE_NAME + "` WHERE related_host IN (";
 			
 			for(String relatedHostUrlString : relatedHostsToDelete) {
 				//For safety, make sure that the relatedHostUrlString doesn't contain any single quotes
 				if(relatedHostUrlString.contains("'")) {
 					HrwaManager.writeToLog("Error: Single quotation mark found in related host url string (" + relatedHostUrlString + "). This will lead to an invalid MySQL insert statement.", true, HrwaManager.LOG_TYPE_ERROR);
 					System.exit(HrwaManager.EXIT_CODE_ERROR);
 				}
 				
 				relatedHostRecordsDeletionStatement += "'" + relatedHostUrlString + "',";
 			}
 			
 			//And remove the last comma because it's not valid, and then add ")" to complete the insert statement 
 			relatedHostRecordsDeletionStatement = relatedHostRecordsDeletionStatement.substring(0, relatedHostRecordsDeletionStatement.length()-1) + ")";
 			
 			PreparedStatement pstmt2 = staticConnWithAutoCommitOn.prepareStatement(relatedHostRecordsDeletionStatement);
 			
 			pstmt2.execute();
 			pstmt2.close();
 			
 			HrwaManager.writeToLog("Deleted " + relatedHostsToDelete.size() + " outdated related hosts.", true, HrwaManager.LOG_TYPE_STANDARD);
 		}
 		
 		//Then add all new related hosts to related hosts table, linking site table ids to related host values
 		if(relatedHostsToAddMappedToSiteSeeds.size() > 0) {
 			
 			HashMap<String, Integer> sitesToSiteIdsMap = MySQLHelper.getSitesMap(null);
 			
 			String relatedHostRecordsInsertStatement = "INSERT INTO `" + HrwaManager.MYSQL_RELATED_HOSTS_TABLE_NAME + "` (`site_id`, `related_host`, `" + MySQLHelper.HRWA_MANAGER_TODO_FIELD_NAME + "`) VALUES";
 			
 			int siteId;
 			String relatedHost;
 			for(Map.Entry<String, String> entry : relatedHostsToAddMappedToSiteSeeds.entrySet()) {
 				if(sitesToSiteIdsMap.containsKey(entry.getValue())) {
 					siteId = sitesToSiteIdsMap.get(entry.getValue());
 					relatedHost = entry.getKey();
 					
 					//For safety, make sure that the relatedHost doesn't contain any single quotes
 					if(relatedHost.contains("'")) {
 						HrwaManager.writeToLog("Error: Single quotation mark found in related host (" + relatedHost + "). This will lead to an invalid MySQL insert statement.", true, HrwaManager.LOG_TYPE_ERROR);
 						System.exit(HrwaManager.EXIT_CODE_ERROR);
 					}
 					
 					relatedHostRecordsInsertStatement += "(" + siteId + ", '" + relatedHost + "', '" + MySQLHelper.HRWA_MANAGER_TODO_NEW + "'),";
 				} else {
 					//Write out error if this related host cannot be linked to an existing site string
 					HrwaManager.writeToLog("Error: Could not find site in sites table (" + entry.getValue() + "), so there was no record to link to this related host (" + entry.getKey() + ").", true, HrwaManager.LOG_TYPE_ERROR);
 					System.exit(HrwaManager.EXIT_CODE_ERROR);
 				}
 			}
 			
 			//And remove the last comma because it's not valid
 			relatedHostRecordsInsertStatement = relatedHostRecordsInsertStatement.substring(0, relatedHostRecordsInsertStatement.length()-1);
 			
 			PreparedStatement pstmt3 = staticConnWithAutoCommitOn.prepareStatement(relatedHostRecordsInsertStatement);
 			
 			pstmt3.execute();
 			pstmt3.close();
 			
 			HrwaManager.writeToLog("Addded " + relatedHostsToAddMappedToSiteSeeds.size() + " new related hosts.", true, HrwaManager.LOG_TYPE_STANDARD);
 		}
 		
 	}
 	
 	/**
 	 * Gets rows from the sites table, optionally filtering by the given whereClause.
 	 * If you want all rows, pass null for the whereClause Argument.
 	 * @param whereClause
 	 * @return
 	 */
 	public static HashMap<String, Integer> getSitesMap(String whereClause) {
 		
 		HashMap<String, Integer> sitesMapToReturn = new HashMap<String, Integer>();
 		
 		try {
 			PreparedStatement pstmt = staticConnWithAutoCommitOn.prepareStatement("SELECT hoststring, id FROM " + HrwaManager.MYSQL_SITES_TABLE_NAME + (whereClause == null ? "" : " " + whereClause));
 			ResultSet resultSet = pstmt.executeQuery();
 			
 			while (resultSet.next()) {
 	
 				//we know that hoststring == column 1 and id == column 2 because of the query ordering
 				sitesMapToReturn.put(resultSet.getString(1), resultSet.getInt(2));
 			}
 			
 			resultSet.close();
 	        pstmt.close();
 		} catch (SQLException e) {
 			HrwaManager.writeToLog("Error: Could not retrieve sites table records from DB", true, HrwaManager.LOG_TYPE_ERROR);
 			e.printStackTrace();
 			System.exit(0);
 		}
         
         return sitesMapToReturn;
 	}
 	
 	public static HashSet<String> getAllBibKeysFromMySQLSitesTable() {
 		HashSet<String> bibKeysToReturn = new HashSet<String>();
 		
 		try {
 			PreparedStatement pstmt = staticConnWithAutoCommitOn.prepareStatement("SELECT bib_key FROM " + HrwaManager.MYSQL_SITES_TABLE_NAME);
 			ResultSet resultSet = pstmt.executeQuery();
 			
 			while (resultSet.next()) {
 	
 				//we know that hoststring == column 1 and id == column 2 because of the query ordering
 				bibKeysToReturn.add(resultSet.getString(1));
 			}
 			
 			resultSet.close();
 	        pstmt.close();
 		} catch (SQLException e) {
 			HrwaManager.writeToLog("Error: Could not retrieve bib_keys from sites table.", true, HrwaManager.LOG_TYPE_ERROR);
 			e.printStackTrace();
 			System.exit(0);
 		}
         
         return bibKeysToReturn;
 	}
 	
 	public static HashMap<String, String> getAllBibKeysAndMarc005LastModifiedStringsFromMySQLSitesTable() {
 		HashMap<String, String> bibKeysAndMarc005LastMofifiedStringsToReturn = new HashMap<String, String>();
 		
 		try {
 			PreparedStatement pstmt = staticConnWithAutoCommitOn.prepareStatement("SELECT bib_key, marc_005_last_modified FROM " + HrwaManager.MYSQL_SITES_TABLE_NAME);
 			ResultSet resultSet = pstmt.executeQuery();
 			
 			while (resultSet.next()) {
 	
 				//we know that hoststring == column 1 and id == column 2 because of the query ordering
 				bibKeysAndMarc005LastMofifiedStringsToReturn.put(resultSet.getString(1), resultSet.getString(2));
 			}
 			
 			resultSet.close();
 	        pstmt.close();
 		} catch (SQLException e) {
 			HrwaManager.writeToLog("Error: Could not retrieve bib_keys from sites table.", true, HrwaManager.LOG_TYPE_ERROR);
 			e.printStackTrace();
 			System.exit(0);
 		}
         
         return bibKeysAndMarc005LastMofifiedStringsToReturn;
 	}
 	
 	public static void addOrUpdateHrwaSiteRecordsInMySQLSitesTable(ArrayList<HrwaSiteRecord> hrwaSiteRecordToAddOrUpdate) {
 		
 		//Get all bib_key/marc_055_last_modified data from the sites table
 		HashMap<String, String> bibKeysAndMarc005LastModifiedStrings = MySQLHelper.getAllBibKeysAndMarc005LastModifiedStringsFromMySQLSitesTable();
 		
 		ArrayList<HrwaSiteRecord> existingRrecordsToUpdate = new ArrayList<HrwaSiteRecord>();
 		ArrayList<HrwaSiteRecord> newRecordsToAdd = new ArrayList<HrwaSiteRecord>();
 		
 		for(HrwaSiteRecord hsRecord : hrwaSiteRecordToAddOrUpdate) {
 			if(bibKeysAndMarc005LastModifiedStrings.containsKey(hsRecord.getSingleValuedFieldValue("bib_key"))) {
 				//This record exists in the site table.
 				//Does it need to be updated?
 				if( ! bibKeysAndMarc005LastModifiedStrings.get( hsRecord.getSingleValuedFieldValue("bib_key")).equals(hsRecord.getSingleValuedFieldValue("marc_005_last_modified")) ) {
 					//marc_005_last_modified values do not match!  We need to update!
 					existingRrecordsToUpdate.add(hsRecord);
 				}
 			} else {
 				//This is a new record
 				newRecordsToAdd.add(hsRecord);
 			}
 		}
 		
 		if(existingRrecordsToUpdate.size() > 0 || newRecordsToAdd.size() > 0) {
 		
 			String latestKnownBibKey = "";
 			
 			try {
 				
 				Connection conn = MySQLHelper.getNewDBConnection(false);
 				
 				if(newRecordsToAdd.size() > 0) {
 					//Add new records
 					PreparedStatement pstmt1 = conn.prepareStatement(
 							"INSERT INTO `" + HrwaManager.MYSQL_SITES_TABLE_NAME + "` " +
 							"(`bib_key`, `creator_name`, `hoststring`, `organization_type`, `organization_based_in`, `geographic_focus`, `language`, `original_urls`, `marc_005_last_modified`, `" + MySQLHelper.HRWA_MANAGER_TODO_FIELD_NAME + "`) " +
 							"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"
 					);
 					
 					for(HrwaSiteRecord singleRecord : newRecordsToAdd) {
 						
 						latestKnownBibKey = singleRecord.getSingleValuedFieldValue("bib_key"); //for debugging purposes
 						
 						pstmt1.setString(1, singleRecord.getSingleValuedFieldValue("bib_key"));
 						pstmt1.setString(2, singleRecord.getPipeDelimitedMultiValuedFieldString("creator_name"));
 						pstmt1.setString(3, singleRecord.getHostString());
 						pstmt1.setString(4, singleRecord.getSingleValuedFieldValue("organization_type"));
 						pstmt1.setString(5, singleRecord.getSingleValuedFieldValue("organization_based_in"));
 						pstmt1.setString(6, singleRecord.getPipeDelimitedMultiValuedFieldString("geographic_focus"));
 						pstmt1.setString(7, singleRecord.getPipeDelimitedMultiValuedFieldString("language"));
 						pstmt1.setString(8, singleRecord.getPipeDelimitedMultiValuedFieldString("original_urls"));
 						pstmt1.setString(9, singleRecord.getSingleValuedFieldValue("marc_005_last_modified"));
 						pstmt1.setString(10, MySQLHelper.HRWA_MANAGER_TODO_NEW);
 						
 						pstmt1.execute();
 					}
 					
 					pstmt1.close();
 				}
 				
 				if(existingRrecordsToUpdate.size() > 0) {
 					//Update existing records
 					
 					//Add new records
 					PreparedStatement pstmt2 = conn.prepareStatement(
 							"UPDATE `" + HrwaManager.MYSQL_SITES_TABLE_NAME + "` " +
 							" SET `creator_name` = ?, `hoststring` = ?, " +
 							" `organization_type` = ?, `organization_based_in` = ?, `geographic_focus` = ?, " +
 							" `language` = ?, `original_urls` = ?, `marc_005_last_modified` = ?," +
 							" " + MySQLHelper.HRWA_MANAGER_TODO_FIELD_NAME + " = CASE WHEN " + MySQLHelper.HRWA_MANAGER_TODO_FIELD_NAME + " = '" + MySQLHelper.HRWA_MANAGER_TODO_NEW + "' THEN '" + MySQLHelper.HRWA_MANAGER_TODO_NEW + "' ELSE '" + MySQLHelper.HRWA_MANAGER_TODO_UPDATED + "' END" +
 							" WHERE bib_key = ?;"
 					);
 					
 					for(HrwaSiteRecord singleRecord : existingRrecordsToUpdate) {
 						
 						latestKnownBibKey = singleRecord.getSingleValuedFieldValue("bib_key"); //for debugging purposes
 						
 						pstmt2.setString(1, singleRecord.getPipeDelimitedMultiValuedFieldString("creator_name"));
 						pstmt2.setString(2, singleRecord.getHostString());
 						pstmt2.setString(3, singleRecord.getSingleValuedFieldValue("organization_type"));
 						pstmt2.setString(4, singleRecord.getSingleValuedFieldValue("organization_based_in"));
 						pstmt2.setString(5, singleRecord.getPipeDelimitedMultiValuedFieldString("geographic_focus"));
 						pstmt2.setString(6, singleRecord.getPipeDelimitedMultiValuedFieldString("language"));
 						pstmt2.setString(7, singleRecord.getPipeDelimitedMultiValuedFieldString("original_urls"));
 						pstmt2.setString(8, singleRecord.getSingleValuedFieldValue("marc_005_last_modified"));
 						pstmt2.setString(9, singleRecord.getSingleValuedFieldValue("bib_key"));
 						
 						System.out.println("Updating bib_key record " + singleRecord.getSingleValuedFieldValue("bib_key") + " with value: " + singleRecord.getSingleValuedFieldValue("marc_005_last_modified"));
 						
 						pstmt2.execute();
 					}
 					
 					pstmt2.close();
 				}
 				
 				conn.commit();
 		        conn.close();
 			} catch (SQLException e) {
 				HrwaManager.writeToLog("Error: Could not add/update records in HRWA MySQL sites table. Error may be related to latest known bib key: " + latestKnownBibKey, true, HrwaManager.LOG_TYPE_ERROR);
 				e.printStackTrace();
 				System.exit(0);
 			}
 			
 		}
 		
 		HrwaManager.writeToLog("Number of new site records added to the sites table: " + newRecordsToAdd.size(), true, HrwaManager.LOG_TYPE_STANDARD);
 		HrwaManager.writeToLog("Number of existing sites table records updated: " + existingRrecordsToUpdate.size(), true, HrwaManager.LOG_TYPE_STANDARD);
 	}
 
 	/**
 	 * Gets rows from the related hosts table, optionally filtering by the given whereClause.
 	 * If you want all rows, pass null for the whereClause Argument.
 	 * @param whereClause
 	 * @return
 	 */
 	public static HashMap<String, Integer> getRelatedHostsMap(String whereClause) {
 		
 		HashMap<String, Integer> relatedHostsMapToReturn = new HashMap<String, Integer>();
 
 		try {
 			PreparedStatement pstmt = staticConnWithAutoCommitOn.prepareStatement("SELECT related_host, site_id FROM " + HrwaManager.MYSQL_RELATED_HOSTS_TABLE_NAME + (whereClause == null ? "" : " " + whereClause));
 			ResultSet resultSet = pstmt.executeQuery();
 			
 			while (resultSet.next()) {
 				//we know that hoststring == column 1 and id == column 2 because of the query ordering
 				relatedHostsMapToReturn.put(resultSet.getString(1), resultSet.getInt(2));
 		    }
 			
 			resultSet.close();
 	        pstmt.close();
 		} catch (SQLException e) {
 			HrwaManager.writeToLog("Error: Could not retrieve records from HRWA MySQL related hosts table", true, HrwaManager.LOG_TYPE_ERROR);
 			e.printStackTrace();
 			System.exit(0);
 		}
 		
         return relatedHostsMapToReturn;
 	}
 	
 	/**
 	 * Marks sites in the Sites table as needing to be deleted.
 	 * @param setOfBibKeysToMarkAsDeleted A HashSet containing String bib_keys associated with the sites that should be marked for deletion.
 	 */
 	public static void markSitesToBeDeleted(HashSet setOfBibKeysToMarkAsDeleted) {
 		
 		if(setOfBibKeysToMarkAsDeleted.size() > 0) {
 			
 			String commaDelimitedBibKeys = "'" + StringUtils.join(setOfBibKeysToMarkAsDeleted, "', '") + "'";
 			
 			try {
 				Connection conn = MySQLHelper.getNewDBConnection(false);
 				PreparedStatement pstmt = conn.prepareStatement("UPDATE " + HrwaManager.MYSQL_SITES_TABLE_NAME + " SET " + MySQLHelper.HRWA_MANAGER_TODO_FIELD_NAME + " = '" + MySQLHelper.HRWA_MANAGER_TODO_DELETE + "' WHERE bib_key IN (" + commaDelimitedBibKeys + ");");
 				pstmt.execute();
 		        pstmt.close();
 		        conn.commit();
 		        conn.close();
 			} catch (SQLException e) {
 				HrwaManager.writeToLog("Error: Could not mark sites to be deleted in HRWA MySQL sites tables", true, HrwaManager.LOG_TYPE_ERROR);
 				e.printStackTrace();
 				System.exit(0);
 			}
 		}
 		
 	}
 	
 	public static long getMaxIdFromWebArchiveRecordsTable() {
 
 		try {
 			
 			PreparedStatement pstmt1 = staticConnWithAutoCommitOn.prepareStatement("SELECT MAX(id) FROM " + HrwaManager.MYSQL_WEB_ARCHIVE_RECORDS_TABLE_NAME);
 			ResultSet resultSet = pstmt1.executeQuery();
 			if (resultSet.next()) {
 				return resultSet.getLong(1);
 			 }
 	        
 			pstmt1.close();
         
 		} catch (SQLException e) {
 			HrwaManager.writeToLog("An error occurred while attempting to retrieve the max id from the web archive records table.", true, HrwaManager.LOG_TYPE_ERROR);
 			e.printStackTrace();
 			System.exit(0);
 		}
 		
 		//We should never get to this point in the code, but if we do then that means that something went wrong while retrieving the max archive record ID.
 		HrwaManager.writeToLog("Error: Could not retrieve max id from web archive records table.  Something went wrong!", true, HrwaManager.LOG_TYPE_ERROR);
         System.exit(HrwaManager.EXIT_CODE_ERROR);
         
         return -1; //this line is necessary to avoid a compiler error (if we don't return anything)
 	}
 	
 	public static boolean archiveFileHasAlreadyBeenFullyIndexedIntoMySQL(String nameOfArchiveFile) {
 
 		try {
 			
 			PreparedStatement pstmt1 = staticConnWithAutoCommitOn.prepareStatement("SELECT COUNT(*) FROM " + HrwaManager.MYSQL_FULLY_INDEXED_ARCHIVE_FILES_TABLE_NAME + " WHERE archive_file_name = ?");
 			pstmt1.setString(1, nameOfArchiveFile);
 			ResultSet resultSet = pstmt1.executeQuery();
 			if (resultSet.next()) {
 				if(resultSet.getInt(1) == 1) {
 					return true;
 				}
 			 }
 	        
 			pstmt1.close();
         
 		} catch (SQLException e) {
 			HrwaManager.writeToLog("An error occurred while checking the fully indexed archive files table to see if the following archive file has already been indexed: " + nameOfArchiveFile, true, HrwaManager.LOG_TYPE_ERROR);
 			e.printStackTrace();
 			System.exit(0);
 		}
         
         return false;
 	}
 	
 	public static void deleteWebArchiveRecordsByFile(String nameOfArchiveFile) {
 		try {
 			
 			PreparedStatement pstmt1 = staticConnWithAutoCommitOn.prepareStatement("DELETE FROM web_archive_records WHERE archive_file = ?");
 			pstmt1.setString(1, nameOfArchiveFile);
 			int numRowsDeleted = pstmt1.executeUpdate();
 			pstmt1.close();
 			
 			if(numRowsDeleted > 0) {
 				HrwaManager.writeToLog("Found partially indexed archive file! [" + nameOfArchiveFile + "]. Deleting partially indexed records.  Number of partially indexed records deleted: " + numRowsDeleted, true, HrwaManager.LOG_TYPE_STANDARD);
 			}
 			
 		} catch (SQLException e) {
 			HrwaManager.writeToLog("An error occurred while deleting web archive records by file: " + nameOfArchiveFile + "\n" + e.getMessage(), true, HrwaManager.LOG_TYPE_ERROR);
 			e.printStackTrace();
 			System.exit(0);
 		}
 	}
 
 }
