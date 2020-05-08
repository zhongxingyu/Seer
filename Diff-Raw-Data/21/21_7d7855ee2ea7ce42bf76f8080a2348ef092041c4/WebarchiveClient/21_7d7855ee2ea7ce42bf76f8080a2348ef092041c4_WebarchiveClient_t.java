 package webarchive.api;
 
 import java.io.File;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.List;
 import org.w3c.dom.Element;
 
 /**
  * Interface to webarchive. provides functions to select metadata from the
  * database, getting and adding files to html-archive-folder or reading or
  * extending XML-Metadata. Each html-file has its own html-archive-folder, which
  * contains at minimum the html itself, XML-Metadata. The folder is extendable
  * by arbitrary files.
  *
  * @author ccwelich
  * @version 1
  */
 public interface WebarchiveClient {
 
 	/**
 	 * selects metadata object from database by a join of mimeType, metaData and history-table.
 	 * selects get constrained by a list of CommitTags.
 	 *
 	 * @param whereMimeType minimal sql-syntax WHERE clauses for mimeType-table ,
 	 * omitted if null<br /> example: "mimeName LIKE 'text/html'"
 	 * @param whereMeta minimal sql-syntax WHERE clauses for metaData-table ,
 	 * omitted if null<br /> example: "url LIKE 'www.heise.de%'"
 	 * @param whereHistoryAdditional additional minimal sql-syntax WHERE clauses
 	 * for history-table, append to commits-list with AND, omitted if null<br />
 	 * example: "WHERE title NOT null"
 	 * @param commits list of commit tags to use for WHERE clause
 	 * @param orderBy array of minimal sql-syntax ORDER BY clauses, ommitted if
 	 * null<br /> examble: "lastCommitTime ASC"
 	 * @return a list of selected MetaData-objects
 	 * @throws Exception
 	 */
 	public List<MetaData> select(List<CommitTag> commits, String whereHistoryAdditional, String whereMimeType, String whereMeta, String[] orderBy) throws Exception;
 
 	/**
 	 * selects metadata objects from database by a join of mimeType, metaData, domain, commitTag and history-table
 	 *
 	 * @param whereMimeType minimal sql-syntax WHERE clauses for mimeType-table
 	 * , omitted if null<br /> example: "mimeName LIKE 'text/html'"
 	 * @param whereMeta minimal sql-syntax WHERE clauses for metaData-table ,
 	 * omitted if null<br /> example: "url LIKE 'www.heise.de%'"
 	 * @param whereDomain minimal sql-syntax WHERE clauses for domain-table ,
 	 * omitted if null<br />
 	 * @param whereCommitTagJoinHistory minimal sql-syntax WHERE clauses for
 	 * JOIN of commitTag and history-table , omitted if null<br /> example:
 	 * "WHERE title NOT null AND commitTime > '2012-05-15T17:30:00';"
 	 * @param where array of minimal sql-syntax WHERE clauses in , omitted if
 	 * null<br /> example: "url LIKE 'www.heise.de'"
 	 * @param orderBy array of minimal sql-syntax ORDER BY clauses, ommitted if
 	 * null<br /> examble: "lastCommitTime ASC"
 	 * @return a list of selected MetaData-objects
 	 * @throws Exception
 	 */
 	public List<MetaData> select(String whereMimeType, String whereMeta, String whereDomain, String whereCommitTagJoinHistory, String[] orderBy) throws Exception;
 
 	/**
 	 * get a file list of all files of a html-archive-folder
 	 *
 	 * @param meta key to archive-folder
 	 * @return a recursive file list of all file of the archive folder
 	 * identified by the given metadata
 	 * @throws Exception
 	 */
 	public List<File> getFileList(MetaData meta) throws Exception;
 
 	/**
 	 * get a input stream of a file of a html-archive-folder
 	 *
 	 * @param meta key to archive-folder
 	 * @param relativePath relative path of file inside archive-folder
 	 * @return inputstream of byte data
 	 * @throws Exception
 	 */
 	public InputStream getInputStream(MetaData meta, File relativePath) throws Exception;
 
 	/**
 	 * add new files to a buffer, which will be send when closing the stream.
 	 * Existing files cannot be overwritten with this method.
 	 *
 	 * @param meta key to archive-folder
 	 * @param relativePath relative path and name of file inside archive-folder
 	 * @return output stream to write data into archive file if request was
 	 * successful, otherwise null
 	 * @throws Exception
 	 */
 	public OutputStream getOutputStream(MetaData meta, File relativePath) throws Exception;
 
 	/**
	 * fetches a XML-file from the server and returns an XMLEdit object, which can
	 * read or add an element from or to the XML-file
 	 *
 	 * @param meta key to XML-Metadata
 	 * @param tagName tagname of an element
	 * @return XMLEdit
 	 * @throws Exception
 	 */
	public XMLEdit getXMLData(MetaData meta, String tagName) throws Exception;
 
 	/**
 	 * @see java.util.Observable
 	 */
 	public void addObserver(WebarchiveObserver o);
 
 	/**
 	 * @see java.util.Observable
 	 */
 	public int countObservers();
 
 	/**
 	 * @see java.util.Observable
 	 */
 	public void deleteObserver(WebarchiveObserver o);
 
 	/**
 	 * @see java.util.Observable
 	 */
 	public void deleteObservers();
 }
