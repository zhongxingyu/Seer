 package jp.ac.osaka_u.ist.sdl.ectec.db;
 
 /**
  * A class for create
  * 
  * @author k-hotta
  * 
  */
 public class DBMaker {
 
 	/**
 	 * the manager for the connection between the db
 	 */
 	private final DBConnectionManager dbManager;
 
 	/**
 	 * the constructor <br>
 	 * DBConnectionManager must be initialized before calling this constructor
 	 */
 	public DBMaker(final DBConnectionManager dbManager) {
 		this.dbManager = dbManager;
 	}
 
 	/**
 	 * make the db
 	 * 
 	 * @param overwrite
 	 *            if true, the existing db is overwritten by the new one (the
 	 *            existing db will be deleted)
 	 * @throws Exception
 	 */
 	public void makeDb(final boolean overwrite) throws Exception {
 		dbManager.setAutoCommit(true);
 
 		if (overwrite) {
 			dropTables();
 		}
 
 		createNewTables();
 		
 		createIndexes();
 
 		dbManager.setAutoCommit(false);
 	}
 
 	/**
 	 * create new tables
 	 * 
 	 * @throws Exception
 	 */
 	public void createNewTables() throws Exception {
 		dbManager.executeUpdate(getRevisionTableQuery());
 		dbManager.executeUpdate(getCommitTableQuery());
 		dbManager.executeUpdate(getFileTableQuery());
 		dbManager.executeUpdate(getCodeFragmentTableQuery());
 		dbManager.executeUpdate(getCloneSetTableQuery());
 		dbManager.executeUpdate(getCodeFragmentLinkTableQuery());
 		dbManager.executeUpdate(getCloneSetLinkTableQuery());
 		dbManager.executeUpdate(getCloneGenealogyTableQuery());
 		dbManager.executeUpdate(getCodeFragmentGenealogyTableQuery());
 		dbManager.executeUpdate(getCrdQuery());
 	}
 
 	/**
 	 * drop all the tables and vacuum the db
 	 */
 	public void dropTables() {
 		try {
 			dbManager.executeUpdate("DROP TABLE REVISION");
 		} catch (Exception e) {
 			// e.printStackTrace();
 		}
 
 		try {
 			dbManager.executeUpdate("DROP TABLE VCS_COMMIT");
 		} catch (Exception e) {
 			// e.printStackTrace();
 		}
 
 		try {
 			dbManager.executeUpdate("DROP TABLE FILE");
 		} catch (Exception e) {
 			// e.printStackTrace();
 		}
 
 		try {
 			dbManager.executeUpdate("DROP TABLE CODE_FRAGMENT");
 		} catch (Exception e) {
 			// e.printStackTrace();
 		}
 
 		try {
 			dbManager.executeUpdate("DROP TABLE CLONE_SET");
 		} catch (Exception e) {
 			// e.printStackTrace();
 		}
 
 		try {
 			dbManager.executeUpdate("DROP TABLE CODE_FRAGMENT_LINK");
 		} catch (Exception e) {
 			// e.printStackTrace();
 		}
 
 		try {
 			dbManager.executeUpdate("DROP TABLE CLONE_SET_LINK");
 		} catch (Exception e) {
 			// e.printStackTrace();
 		}
 
 		try {
 			dbManager.executeUpdate("DROP TABLE CLONE_GENEALOGY");
 		} catch (Exception e) {
 			// e.printStackTrace();
 		}
 
 		try {
 			dbManager.executeUpdate("DROP TABLE CODE_FRAGMENT_GENEALOGY");
 		} catch (Exception e) {
 			// e.printStackTrace();
 		}
 
 		try {
 			dbManager.executeUpdate("DROP TABLE CRD");
 		} catch (Exception e) {
 			// e.printStackTrace();
 		}
 
 		try {
 			dbManager.executeUpdate("VACUUM");
 		} catch (Exception e) {
 			// e.printStackTrace();
 		}
 	}
 
 	public void createIndexes() throws Exception {
 		createRevisionTableIndexes();
 		createCommitTableIndexes();
 		createFileTableIndexes();
 		createCodeFragmentTableIndexes();
 		createCloneSetTableIndexes();
 		createCodeFragmentLinkTableIndexes();
 		createCloneSetLinkTableIndexes();
 		createCloneGenealogyTableIndexes();
 		createCodeFragmentGenealogyTableIndexes();
 		createCrdTableIndexes();
 	}
 
 	/*
 	 * definitions of each table follow
 	 */
 
 	/**
 	 * get the query to create the revision table
 	 * 
 	 * @return
 	 */
 	private String getRevisionTableQuery() {
 		final StringBuilder builder = new StringBuilder();
 
 		builder.append("create table REVISION(");
 		builder.append("REVISION_ID LONG PRIMARY KEY,");
 		builder.append("REVISION_IDENTIFIER TEXT UNIQUE");
 		builder.append(")");
 
 		return builder.toString();
 	}
 
 	/**
 	 * create indexes on the revision table
 	 * 
 	 * @throws Exception
 	 */
 	private void createRevisionTableIndexes() throws Exception {
 		dbManager
 				.executeUpdate("create index REVISION_ID_INDEX_REVISION on REVISION(REVISION_ID)");
 	}
 
 	/**
 	 * get the query to create the commit table
 	 * 
 	 * @return
 	 */
 	private String getCommitTableQuery() {
 		final StringBuilder builder = new StringBuilder();
 
 		builder.append("create table VCS_COMMIT(");
 		builder.append("VCS_COMMIT_ID LONG PRIMARY KEY,");
 		builder.append("BEFORE_REVISION_ID LONG,");
 		builder.append("AFTER_REVISION_ID LONG,");
 		builder.append("BEFORE_REVISION_IDENTIFIER TEXT,");
 		builder.append("AFTER_REVISION_IDENTIFIER TEXT");
 		builder.append(")");
 
 		return builder.toString();
 	}
 
 	/**
 	 * create indexes on the commit table
 	 * 
 	 * @throws Exception
 	 */
 	private void createCommitTableIndexes() throws Exception {
 		dbManager
 				.executeUpdate("create index VCS_COMMIT_ID_INDEX_VCS_COMMIT on VCS_COMMIT(VCS_COMMIT_ID)");
 		dbManager
 				.executeUpdate("create index BEFORE_REVISION_ID_INDEX_VCS_COMMIT on VCS_COMMIT(BEFORE_REVISION_ID)");
 		dbManager
 				.executeUpdate("create index AFTER_REVISION_ID_INDEX_VCS_COMMIT on VCS_COMMIT(AFTER_REVISION_ID)");
 	}
 
 	/**
 	 * get the query to create the file table
 	 * 
 	 * @return
 	 */
 	private String getFileTableQuery() {
 		final StringBuilder builder = new StringBuilder();
 
 		builder.append("create table FILE(");
 		builder.append("FILE_ID LONG PRIMARY KEY,");
 		builder.append("FILE_PATH TEXT,");
 		builder.append("START_REVISION_ID LONG,");
 		builder.append("END_REVISION_ID LONG");
 		builder.append(")");
 
 		return builder.toString();
 	}
 
 	/**
 	 * create indexes on the file table
 	 * 
 	 * @throws Exception
 	 */
 	private void createFileTableIndexes() throws Exception {
 		dbManager
 				.executeUpdate("create index FILE_ID_INDEX_FILE on FILE(FILE_ID)");
 		dbManager
 				.executeUpdate("create index START_REVISION_ID_INDEX_FILE on FILE(START_REVISION_ID)");
 		dbManager
 				.executeUpdate("create index END_REVISION_ID_INDEX_FILE on FILE(END_REVISION_ID)");
 	}
 
 	/**
 	 * get the query to create the code fragment table
 	 * 
 	 * @return
 	 */
 	private String getCodeFragmentTableQuery() {
 		final StringBuilder builder = new StringBuilder();
 
 		builder.append("create table CODE_FRAGMENT(");
 		builder.append("CODE_FRAGMENT_ID LONG PRIMARY KEY,");
 		builder.append("OWNER_FILE_ID LONG,");
 		builder.append("CRD_ID LONG,");
 		builder.append("START_REVISION_ID LONG,");
 		builder.append("END_REVISION_ID LONG,");
 		builder.append("HASH LONG,");
 		builder.append("HASH_FOR_CLONE LONG,");
 		builder.append("START_LINE INTEGER,");
 		builder.append("END_LINE INTEGER,");
 		builder.append("SIZE INTEGER");
 		builder.append(")");
 
 		return builder.toString();
 	}
 
 	/**
 	 * create indexes on the code fragment table
 	 * 
 	 * @throws Exception
 	 */
 	private void createCodeFragmentTableIndexes() throws Exception {
 		dbManager
 				.executeUpdate("create index CODE_FRAGMENT_ID_INDEX_CODE_FRAGMENT on CODE_FRAGMENT(CODE_FRAGMENT_ID)");
 		dbManager
 				.executeUpdate("create index START_REVISION_ID_INDEX_CODE_FRAGMENT on CODE_FRAGMENT(START_REVISION_ID)");
 		dbManager
 				.executeUpdate("create index END_REVISION_ID_INDEX_CODE_FRAGMENT on CODE_FRAGMENT(END_REVISION_ID)");
 		dbManager
 				.executeUpdate("create index HASH_INDEX_CODE_FRAGMENT on CODE_FRAGMENT(HASH)");
 		dbManager
 				.executeUpdate("create index HASH_FOR_CLONE_INDEX_CODE_FRAGMENT on CODE_FRAGMENT(HASH_FOR_CLONE)");
 		dbManager
 				.executeUpdate("create index START_LINE_INDEX_CODE_FRAGMENT on CODE_FRAGMENT(START_LINE)");
 		dbManager
 				.executeUpdate("create index END_LINE_INDEX_CODE_FRAGMENT on CODE_FRAGMENT(END_LINE)");
 		dbManager
 				.executeUpdate("create index SIZE_INDEX_CODE_FRAGMENT on CODE_FRAGMENT(SIZE)");
 		dbManager
 				.executeUpdate("create index START_END_REVISION_ID_INDEX_CODE_FRAGMENT on CODE_FRAGMENT(START_REVISION_ID,END_REVISION_ID)");
 	}
 
 	/**
 	 * get the query to create the clone set table
 	 * 
 	 * @return
 	 */
 	private String getCloneSetTableQuery() {
 		final StringBuilder builder = new StringBuilder();
 
 		builder.append("create table CLONE_SET(");
 		builder.append("CLONE_SET_ID LONG PRIMARY KEY,");
 		builder.append("OWNER_REVISION_ID LONG,");
 		builder.append("ELEMENTS TEXT NOT NULL,");
 		builder.append("NUMBER_OF_ELEMENTS INTEGER");
 		builder.append(")");
 
 		return builder.toString();
 	}
 
 	/**
 	 * create indexes on the clone set table
 	 * 
 	 * @throws Exception
 	 */
 	private void createCloneSetTableIndexes() throws Exception {
 		dbManager
 				.executeUpdate("create index CLONE_SET_ID_INDEX_CLONE_SET on CLONE_SET(CLONE_SET_ID)");
 		dbManager
 				.executeUpdate("create index OWNER_REVISION_ID_INDEX_CLONE_SET on CLONE_SET(OWNER_REVISION_ID)");
 		dbManager
				.executeUpdate("create index NUMBER_OF_ELEMENTS_INDEX_CLONE_SET on CLONE_SET(NUMVER_OF_ELEMENTS)");
 	}
 
 	/**
 	 * get the query to create the table for links of code fragments
 	 * 
 	 * @return
 	 */
 	private String getCodeFragmentLinkTableQuery() {
 		final StringBuilder builder = new StringBuilder();
 
 		builder.append("create table CODE_FRAGMENT_LINK(");
 		builder.append("CODE_FRAGMENT_LINK_ID LONG PRIMARY KEY,");
 		builder.append("BEFORE_ELEMENT_ID LONG,");
 		builder.append("AFTER_ELEMENT_ID LONG,");
 		builder.append("BEFORE_REVISION_ID LONG,");
 		builder.append("AFTER_REVISION_ID LONG,");
 		builder.append("CHANGED INTEGER");
 		builder.append(")");
 
 		return builder.toString();
 	}
 
 	/**
 	 * create indexes on the fragment link table
 	 * 
 	 * @throws Exception
 	 */
 	private void createCodeFragmentLinkTableIndexes() throws Exception {
 		dbManager
 				.executeUpdate("create index CODE_FRAGMENT_LINK_ID_INDEX_CODE_FRAGMENT_LINK on CODE_FRAGMENT_LINK(CODE_FRAGMENT_LINK_ID)");
 		dbManager
 				.executeUpdate("create index BEFORE_ELEMENT_ID_INDEX_CODE_FRAGMENT_LINK on CODE_FRAGMENT_LINK(BEFORE_ELEMENT_ID)");
 		dbManager
 				.executeUpdate("create index AFTER_ELEMENT_ID_INDEX_CODE_FRAGMENT_LINK on CODE_FRAGMENT_LINK(AFTER_ELEMENT_ID)");
 		dbManager
 				.executeUpdate("create index BEFORE_REVISION_ID_INDEX_CODE_FRAGMENT_LINK on CODE_FRAGMENT_LINK(BEFORE_REVISION_ID)");
 		dbManager
 				.executeUpdate("create index AFTER_REVISION_ID_INDEX_CODE_FRAGMENT_LINK on CODE_FRAGMENT_LINK(AFTER_REVISION_ID)");
 		dbManager
 				.executeUpdate("create index CHANGED_INDEX_CODE_FRAGMENT_LINK on CODE_FRAGMENT_LINK(CHANGED)");
 	}
 
 	/**
 	 * get the query to create the table for links of clone sets
 	 * 
 	 * @return
 	 */
 	private String getCloneSetLinkTableQuery() {
 		final StringBuilder builder = new StringBuilder();
 
 		builder.append("create table CLONE_SET_LINK(");
 		builder.append("CLONE_SET_LINK_ID LONG PRIMARY KEY,");
 		builder.append("BEFORE_ELEMENT_ID LONG,");
 		builder.append("AFTER_ELEMENT_ID LONG,");
 		builder.append("BEFORE_REVISION_ID LONG,");
 		builder.append("AFTER_REVISION_ID LONG,");
 		builder.append("CHANGED_ELEMENTS INTEGER,");
 		builder.append("ADDED_ELEMENTS INTEGER,");
 		builder.append("DELETED_ELEMENTS INTEGER,");
 		builder.append("CO_CHANGED_ELEMENTS INTEGER,");
 		builder.append("CODE_FRAGMENT_LINKS TEXT NOT NULL");
 		builder.append(")");
 
 		return builder.toString();
 	}
 
 	/**
 	 * create indexes on the clone link table
 	 * 
 	 * @throws Exception
 	 */
 	private void createCloneSetLinkTableIndexes() throws Exception {
 		dbManager
 				.executeUpdate("create index CLONE_SET_LINK_ID_INDEX_CLONE_SET_LINK on CLONE_SET_LINK(CLONE_SET_LINK_ID)");
 		dbManager
 				.executeUpdate("create index BEFORE_ELEMENT_ID_INDEX_CLONE_SET_LINK on CLONE_SET_LINK(BEFORE_ELEMENT_ID)");
 		dbManager
 				.executeUpdate("create index AFTER_ELEMENT_ID_INDEX_CLONE_SET_LINK on CLONE_SET_LINK(AFTER_ELEMENT_ID)");
 		dbManager
 				.executeUpdate("create index BEFORE_REVISION_ID_INDEX_CLONE_SET_LINK on CLONE_SET_LINK(BEFORE_REVISION_ID)");
 		dbManager
 				.executeUpdate("create index AFTER_REVISION_ID_INDEX_CLONE_SET_LINK on CLONE_SET_LINK(AFTER_REVISION_ID)");
 		dbManager
 				.executeUpdate("create index CHANGED_ELEMENTS_INDEX_CLONE_SET_LINK on CLONE_SET_LINK(CHANGED_ELEMENTS)");
 		dbManager
 				.executeUpdate("create index ADDED_ELEMENTS_INDEX_CLONE_SET_LINK on CLONE_SET_LINK(ADDED_ELEMENTS)");
 		dbManager
 				.executeUpdate("create index DELETED_ELEMENTS_INDEX_CLONE_SET_LINK on CLONE_SET_LINK(DELETED_ELEMENTS)");
 		dbManager
 				.executeUpdate("create index CO_CHANGED_ELEMENTS_INDEX_CLONE_SET_LINK on CLONE_SET_LINK(CO_CHANGED_ELEMENTS)");
 	}
 
 	/**
 	 * get the query to create the table for genealogies of clones
 	 * 
 	 * @return
 	 */
 	private String getCloneGenealogyTableQuery() {
 		final StringBuilder builder = new StringBuilder();
 
 		builder.append("create table CLONE_GENEALOGY(");
 		builder.append("CLONE_GENEALOGY_ID LONG PRIMARY KEY,");
 		builder.append("START_REVISION_ID LONG,");
 		builder.append("END_REVISION_ID LONG,");
 		builder.append("CLONES TEXT NOT NULL,");
 		builder.append("CLONE_LINKS TEXT NOT NULL,");
 		builder.append("CHANGES INTEGER,");
 		builder.append("ADDITIONS INTEGER,");
 		builder.append("DELETIONS INTEGER,");
 		builder.append("DEAD INTEGER");
 		builder.append(")");
 
 		return builder.toString();
 	}
 
 	/**
 	 * create indexes on the clone genealogy table
 	 * 
 	 * @throws Exception
 	 */
 	private void createCloneGenealogyTableIndexes() throws Exception {
 		dbManager
 				.executeUpdate("create index CLONE_GENEALOGY_ID_INDEX_CLONE_GENEALOGY on CLONE_GENEALOGY(CLONE_GENEALOGY_ID)");
 		dbManager
 				.executeUpdate("create index START_REVISION_ID_INDEX_CLONE_GENEALOGY on CLONE_GENEALOGY(START_REVISION_ID)");
 		dbManager
 				.executeUpdate("create index END_REVISION_ID_INDEX_CLONE_GENEALOGY on CLONE_GENEALOGY(END_REVISION_ID)");
 		dbManager
 				.executeUpdate("create index CHANGES_INDEX_CLONE_GENEALOGY on CLONE_GENEALOGY(CHANGES)");
 		dbManager
 				.executeUpdate("create index ADDITIONS_INDEX_CLONE_GENEALOGY on CLONE_GENEALOGY(ADDITIONS)");
 		dbManager
 				.executeUpdate("create index DELETIONS_INDEX_CLONE_GENEALOGY on CLONE_GENEALOGY(DELETIONS)");
 		dbManager
 				.executeUpdate("create index DEAD_INDEX_CLONE_GENEALOGY on CLONE_GENEALOGY(DEAD)");
 		dbManager
 				.executeUpdate("create index START_END_REVISION_ID_INDEX_CLONE_GENEALOGY on CLONE_GENEALOGY(START_REVISION_ID,END_REVISION_ID)");
 	}
 
 	/**
 	 * get the query to create the table for genealogies of code fragments
 	 * 
 	 * @return
 	 */
 	private String getCodeFragmentGenealogyTableQuery() {
 		final StringBuilder builder = new StringBuilder();
 
 		builder.append("create table CODE_FRAGMENT_GENEALOGY(");
 		builder.append("CODE_FRAGMENT_GENEALOGY_ID LONG PRIMARY KEY,");
 		builder.append("START_REVISION_ID LONG,");
 		builder.append("END_REVISION_ID LONG,");
 		builder.append("CODE_FRAGMENTS TEXT NOT NULL,");
 		builder.append("CODE_FRAGMENT_LINKS TEXT NOT NULL,");
 		builder.append("CHANGES INTEGER");
 		builder.append(")");
 
 		return builder.toString();
 	}
 
 	/**
 	 * create indexes on the fragment genealogy table
 	 * 
 	 * @throws Exception
 	 */
 	private void createCodeFragmentGenealogyTableIndexes() throws Exception {
 		dbManager
 				.executeUpdate("create index CODE_FRAGMENT_GENEALOGY_ID_INDEX_CODE_FRAGMENT_GENEALOGY on CODE_FRAGMENT_GENEALOGY(CODE_FRAGMENT_GENEALOGY_ID)");
 		dbManager
 				.executeUpdate("create index START_REVISION_ID_INDEX_CODE_FRAGMENT_GENEALOGY on CODE_FRAGMENT_GENEALOGY(START_REVISION_ID)");
 		dbManager
 				.executeUpdate("create index END_REVISION_ID_INDEX_CODE_FRAGMENT_GENEALOGY on CODE_FRAGMENT_GENEALOGY(END_REVISION_ID)");
 		dbManager
 				.executeUpdate("create index CHANGES_INDEX_CODE_FRAGMENT_GENEALOGY on CODE_FRAGMENT_GENEALOGY(CHANGES)");
 		dbManager
 				.executeUpdate("create index START_END_REVISION_ID_INDEX_CODE_FRAGMENT_GENEALOGY on CODE_FRAGMENT_GENEALOGY(START_REVISION_ID,END_REVISION_ID)");
 	}
 
 	/**
 	 * get the query to create the table for genealogies of clones
 	 * 
 	 * @return
 	 */
 	private String getCrdQuery() {
 		final StringBuilder builder = new StringBuilder();
 
 		builder.append("create table CRD(");
 		builder.append("CRD_ID LONG PRIMARY KEY,");
 		builder.append("TYPE TEXT NOT NULL,");
 		builder.append("HEAD TEXT NOT NULL,");
 		builder.append("ANCHOR TEXT NOT NULL,");
 		builder.append("NORMALIZED_ANCHOR TEXT NOT NULL,");
 		builder.append("CM INTEGER,");
 		builder.append("ANCESTORS TEXT NOT NULL,");
 		builder.append("FULL_TEXT TEXT NOT NULL");
 		builder.append(")");
 
 		return builder.toString();
 	}
 
 	/**
 	 * create indexes on the crd table
 	 * 
 	 * @throws Exception
 	 */
 	private void createCrdTableIndexes() throws Exception {
 		dbManager.executeUpdate("create index CRD_ID_INDEX_CRD on CRD(CRD_ID)");
 		dbManager.executeUpdate("create index CM_INDEX_CRD on CRD(CM)");
 	}
 
 }
