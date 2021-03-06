 package org.tmatesoft.svn.core.internal.wc2.old;
 
 import java.io.File;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Map;
 
 import org.tmatesoft.sqljet.core.SqlJetTransactionMode;
 import org.tmatesoft.svn.core.SVNDepth;
 import org.tmatesoft.svn.core.SVNErrorCode;
 import org.tmatesoft.svn.core.SVNErrorMessage;
 import org.tmatesoft.svn.core.SVNException;
 import org.tmatesoft.svn.core.SVNNodeKind;
 import org.tmatesoft.svn.core.SVNProperties;
 import org.tmatesoft.svn.core.SVNProperty;
 import org.tmatesoft.svn.core.SVNURL;
 import org.tmatesoft.svn.core.internal.db.SVNSqlJetDb;
 import org.tmatesoft.svn.core.internal.db.SVNSqlJetStatement;
 import org.tmatesoft.svn.core.internal.util.SVNHashMap;
 import org.tmatesoft.svn.core.internal.util.SVNHashSet;
 import org.tmatesoft.svn.core.internal.util.SVNPathUtil;
 import org.tmatesoft.svn.core.internal.util.SVNSkel;
 import org.tmatesoft.svn.core.internal.util.SVNURLUtil;
 import org.tmatesoft.svn.core.internal.wc.SVNErrorManager;
 import org.tmatesoft.svn.core.internal.wc.SVNEventFactory;
 import org.tmatesoft.svn.core.internal.wc.SVNFileListUtil;
 import org.tmatesoft.svn.core.internal.wc.SVNFileType;
 import org.tmatesoft.svn.core.internal.wc.SVNFileUtil;
 import org.tmatesoft.svn.core.internal.wc.admin.SVNAdminArea;
 import org.tmatesoft.svn.core.internal.wc.admin.SVNChecksumInputStream;
 import org.tmatesoft.svn.core.internal.wc.admin.SVNEntry;
 import org.tmatesoft.svn.core.internal.wc.admin.SVNVersionedProperties;
 import org.tmatesoft.svn.core.internal.wc.admin.SVNWCAccess;
 import org.tmatesoft.svn.core.internal.wc17.SVNWCContext;
 import org.tmatesoft.svn.core.internal.wc17.SVNWCUtils;
 import org.tmatesoft.svn.core.internal.wc17.db.ISVNWCDb.SVNWCDbUpgradeData;
 import org.tmatesoft.svn.core.internal.wc17.db.SVNWCDb;
 import org.tmatesoft.svn.core.internal.wc17.db.ISVNWCDb.SVNWCDbOpenMode;
 import org.tmatesoft.svn.core.internal.wc17.db.SVNWCDbRoot;
 import org.tmatesoft.svn.core.internal.wc17.db.SvnWcDbPristines;
 import org.tmatesoft.svn.core.internal.wc17.db.SvnWcDbProperties;
 import org.tmatesoft.svn.core.internal.wc17.db.statement.SVNWCDbSchema;
 import org.tmatesoft.svn.core.internal.wc17.db.statement.SVNWCDbStatements;
 import org.tmatesoft.svn.core.internal.wc2.SvnRepositoryAccess;
 import org.tmatesoft.svn.core.internal.wc2.SvnWcGeneration;
 import org.tmatesoft.svn.core.io.SVNRepository;
 import org.tmatesoft.svn.core.wc.ISVNOptions;
 import org.tmatesoft.svn.core.wc.SVNEvent;
 import org.tmatesoft.svn.core.wc.SVNEventAction;
 import org.tmatesoft.svn.core.wc2.ISvnObjectReceiver;
 import org.tmatesoft.svn.core.wc2.SvnChecksum;
 import org.tmatesoft.svn.core.wc2.SvnChecksum.Kind;
 import org.tmatesoft.svn.core.wc2.SvnGetProperties;
 import org.tmatesoft.svn.core.wc2.SvnTarget;
 import org.tmatesoft.svn.core.wc2.SvnUpgrade;
 import org.tmatesoft.svn.util.SVNLogType;
 import org.tmatesoft.svn.core.internal.wc2.old.SvnOldUpgradeEntries.WriteBaton;
 
 public class SvnOldUpgrade extends SvnOldRunner<SvnWcGeneration, SvnUpgrade> {
 
 	/* WC-1.0 administrative area extensions */
 	private static final String SVN_WC__BASE_EXT = ".svn-base"; /*
 																 * for text and
 																 * prop bases
 																 */
 	private static final String SVN_WC__REVERT_EXT = ".svn-revert"; /*
 																	 * for
 																	 * reverting
 																	 * a
 																	 * replaced
 																	 * file
 																	 */
 
 	/* Old locations for storing "wcprops" (aka "dav cache"). */
 	private static final String WCPROPS_SUBDIR_FOR_FILES = "wcprops";
 	private static final String WCPROPS_FNAME_FOR_DIR = "dir-wcprops";
 	private static final String WCPROPS_ALL_DATA = "all-wcprops";
 
 	/* Old property locations. */
 	private static final String PROPS_SUBDIR = "props";
 	private static final String PROP_BASE_SUBDIR = "prop-base";
 	private static final String PROP_BASE_FOR_DIR = "dir-prop-base";
 	private static final String PROP_REVERT_FOR_DIR = "dir-prop-revert";
 	private static final String PROP_WORKING_FOR_DIR = "dir-props";
 
 	/* Old textbase location. */
 	private static final String TEXT_BASE_SUBDIR = "text-base";
 
 	/* Old data files that we no longer need/use. */
 	private static final String ADM_README = "README.txt";
 	private static final String ADM_EMPTY_FILE = "empty-file";
 	private static final String ADM_LOG = "log";
 	private static final String ADM_LOCK = "lock";
 
 	/* New pristine location */
 	private static final String PRISTINE_STORAGE_RELPATH = "pristine";
 	/* Number of characters in a pristine file basename, in WC format <= 28. */
 	private static final String SDB_FILE = "wc.db";
 
 	private class RepositoryInfo {
 		public SVNURL repositoryRootUrl = null;
 		public String UUID = null;
 	}
 
 	private SVNWCAccess access = null;
 
 	private SVNWCAccess getWCAccess() {
 		if (access == null) {
 			access = SVNWCAccess.newInstance(getOperation().getEventHandler());
 			access.setOptions(getOperation().getOptions());
 		}
 		return access;
 	}
 
 	@Override
 	protected SvnWcGeneration run() throws SVNException {
 
 		if (getOperation().getFirstTarget().isURL()) {
 			SVNErrorMessage err = SVNErrorMessage.create(SVNErrorCode.ILLEGAL_TARGET, "'{0}' is not a local path", getOperation().getFirstTarget().getURL());
 			SVNErrorManager.error(err, SVNLogType.WC);
 		}
 
 		File localAbsPath = getOperation().getFirstTarget().getFile().getAbsoluteFile();
 		RepositoryInfo reposInfo = new RepositoryInfo();
 		wcUpgrade(localAbsPath, reposInfo);
 
 		/*
 		 * Now it's time to upgrade the externals too. We do it after the wc upgrade to avoid that errors in the externals causes the wc upgrade
 		 * to fail. Thanks to caching the performance penalty of walking the wc a second time shouldn't be too severe
 		 */
 
 		final ArrayList<SvnTarget> externals = new ArrayList<SvnTarget>();
 		SvnGetProperties getProperties = getOperation().getOperationFactory().createGetProperties();
 		getProperties.addTarget(getOperation().getFirstTarget());
 		getProperties.setDepth(SVNDepth.INFINITY);
 		getProperties.setReceiver(new ISvnObjectReceiver<SVNProperties>() {
 			public void receive(SvnTarget target, SVNProperties object)
 					throws SVNException {
 				if (object.containsName(SVNProperty.EXTERNALS)) {
 					externals.add(target);
 				}
 			}
 		});
 		getProperties.run();
 
 		for (SvnTarget target : externals) {
 			if (SVNFileType.getType(target.getFile()) == SVNFileType.DIRECTORY) {
 				wcUpgrade(target.getFile(), reposInfo);
 			}
 		}
 		return SvnWcGeneration.V17;
 	}
 
 	private void checkIsOldWCRoot(File localAbsPath) throws SVNException {
 		SVNWCAccess wcAccess = getWCAccess();
 		try {
 			wcAccess.probeOpen(localAbsPath, false, 0);
 			if (wcAccess.isWCRoot(localAbsPath))
 				return;
 		} catch (SVNException e) {
 			SVNErrorMessage err = SVNErrorMessage.create(
 					SVNErrorCode.WC_INVALID_OP_ON_CWD, "Can't upgrade '{0}' as it is not a pre-1.7 working copy directory", localAbsPath);
 			SVNErrorManager.error(err, SVNLogType.WC);
 		} finally {
 			wcAccess.close();
 		}
 
 		File parentAbsPath = SVNFileUtil.getParentFile(localAbsPath);
 		SVNEntry entry = null;
 		try {
 			try {
 				wcAccess.probeOpen(parentAbsPath, false, 0);
 			} catch (SVNException e) {
 				return;
 			}
 			entry = wcAccess.getEntry(localAbsPath, false);
 		} finally {
 			wcAccess.close();
 		}
 
 		if (entry == null || entry.isAbsent()
 				|| (entry.isDeleted() && !entry.isScheduledForAddition())
 				|| entry.getDepth() == SVNDepth.EXCLUDE)
 			return;
 
 		File childAbsPath;
 		while (!wcAccess.isWCRoot(parentAbsPath)) {
 			childAbsPath = parentAbsPath;
 			parentAbsPath = SVNFileUtil.getParentFile(parentAbsPath);
 			try {
 				try {
 					wcAccess.probeOpen(parentAbsPath, false, 0);
 				} catch (SVNException e) {
 					parentAbsPath = childAbsPath;
 					break;
 				}
 				entry = wcAccess.getEntry(localAbsPath, false);
 			} finally {
 				wcAccess.close();
 			}
 
 			if (entry == null || entry.isAbsent()
 					|| (entry.isDeleted() && !entry.isScheduledForAddition())
 					|| entry.getDepth() == SVNDepth.EXCLUDE) {
 				parentAbsPath = childAbsPath;
 				break;
 			}
 		}
 		SVNErrorMessage err = SVNErrorMessage
 				.create(SVNErrorCode.WC_INVALID_OP_ON_CWD,
 						"Can't upgrade '{0}' as it is not a pre-1.7 working copy root, the root is '%s'",
 						localAbsPath, parentAbsPath);
 		SVNErrorManager.error(err, SVNLogType.WC);
 	}
 
 	private void fetchReposInfo(SVNEntry entry,
 			RepositoryInfo lastRepositoryInfo) throws SVNException {
 		/* The same info is likely to retrieved multiple times (e.g. externals) */
 		if (lastRepositoryInfo.repositoryRootUrl != null
 				&& SVNURLUtil.isAncestor(lastRepositoryInfo.repositoryRootUrl,
 						entry.getSVNURL())) {
 			entry.setRepositoryRootURL(lastRepositoryInfo.repositoryRootUrl);
 			entry.setUUID(lastRepositoryInfo.UUID);
 			return;
 		}
 
 		SvnRepositoryAccess repAccess = new SvnOldRepositoryAccess(
 				getOperation());
 		SVNRepository repository = repAccess.createRepository(
 				entry.getSVNURL(), null, true);
 		entry.setRepositoryRootURL(repository.getRepositoryRoot(false));
 		entry.setUUID(repository.getRepositoryUUID(false));
 
 		lastRepositoryInfo.repositoryRootUrl = entry.getRepositoryRootURL();
 		lastRepositoryInfo.UUID = entry.getUUID();
 	}
 
 	private void ensureReposInfo(SVNEntry entry, File localAbsPath,
 			RepositoryInfo lastRepositoryInfo, SVNHashMap reposCache)
 			throws SVNException {
 		if (entry.getRepositoryRootURL() != null && entry.getUUID() != null)
 			return;
 
 		if ((entry.getRepositoryRootURL() == null || entry.getUUID() == null)
 				&& entry.getSVNURL() != null) {
 			for (Iterator<SVNURL> items = reposCache.keySet().iterator(); items
 					.hasNext();) {
 				SVNURL reposRootUrl = items.next();
 				if (SVNURLUtil.isAncestor(reposRootUrl, entry.getSVNURL())) {
 					if (entry.getRepositoryRootURL() == null)
 						entry.setRepositoryRootURL(reposRootUrl);
 					if (entry.getUUID() == null)
 						entry.setUUID((String) reposCache.get(reposRootUrl));
 					return;
 				}
 			}
 		}
 
 		if (entry.getSVNURL() == null) {
 			SVNErrorMessage err = SVNErrorMessage
 					.create(SVNErrorCode.WC_UNSUPPORTED_FORMAT,
 							"Working copy '{0}' can't be upgraded because it doesn't have a url",
 							localAbsPath);
 			SVNErrorManager.error(err, SVNLogType.WC);
 		}
 
 		fetchReposInfo(entry, lastRepositoryInfo);
 	}
 
 	private void wcUpgrade(File localAbsPath, RepositoryInfo reposInfo) throws SVNException {
 		SVNWCDbUpgradeData upgradeData = new SVNWCDbUpgradeData();
 
 		checkIsOldWCRoot(localAbsPath);
 
 		/*
 		 * Given a pre-wcng root some/wc we create a temporary wcng in some/wc/.svn/tmp/wcng/wc.db and copy the metadata from one to the
 		 * other, then the temporary wc.db file gets moved into the original root. Until the wc.db file is moved the original working copy remains
 		 * a pre-wcng and 'cleanup' with an old client will remove the partial upgrade. Moving the wc.db file creates a wcng, and 'cleanup' with a
 		 * new client will complete any outstanding upgrade.
 		 */
 
 		SVNWCDb db = new SVNWCDb();
 		db.open(SVNWCDbOpenMode.ReadWrite, (ISVNOptions) null, false, false);
 		SVNWCContext wcContext = new SVNWCContext(db, getOperation().getEventHandler());
 
 		SVNWCAccess wcAccess = getWCAccess();
 		SVNEntry thisDir = null;
 		try {
 			wcAccess.probeOpen(localAbsPath, false, 0);
 			thisDir = wcAccess.getEntry(localAbsPath, false);
 		} finally {
 			wcAccess.close();
 		}
 
 		SVNHashMap reposCache = new SVNHashMap();
 		ensureReposInfo(thisDir, localAbsPath, reposInfo, reposCache);
 
 		/*
 		 * Cache repos UUID pairs for when a subdir doesn't have this information
 		 */
 		if (!reposCache.containsKey(thisDir.getRepositoryRootURL()))
 			reposCache.put(thisDir.getRepositoryRootURL(), thisDir.getUUID());
 		/* Create the new DB in the temporary root wc/.svn/tmp/wcng/.svn */
 
 		upgradeData.rootAbsPath = SVNFileUtil.createFilePath(SVNWCUtils.admChild(localAbsPath, "tmp"), "wcng");
 		File rootAdmAbsPath = SVNWCUtils.admChild(upgradeData.rootAbsPath, "tmp");
 
 		try {
 
 			SVNFileUtil.deleteAll(rootAdmAbsPath, true);
 			SVNFileUtil.ensureDirectoryExists(rootAdmAbsPath);
 
 			/*
 			 * Create an empty sqlite database for this directory and store it in DB.
 			 */
 			db.upgradeBegin(upgradeData.rootAbsPath, upgradeData, thisDir.getRepositoryRootURL(), thisDir.getUUID());
 
 			/*
 			 * Migrate the entries over to the new database. ### We need to think about atomicity here.
 			 * 
 			 * entries_write_new() writes in current format rather than f12.
 			 * Thus, this function bumps a working copy all the way to current.
 			 */
 
 			db.obtainWCLock(upgradeData.rootAbsPath, 0, false);
 
 			upgradeData.root.getSDb().beginTransaction(SqlJetTransactionMode.WRITE);
 			try {
 				upgradeWorkingCopy(null, db, localAbsPath, upgradeData, reposCache, reposInfo);
 			} catch (SVNException ex) {
 				upgradeData.root.getSDb().rollback();
 				throw ex;
 			} finally {
 				upgradeData.root.getSDb().commit();
 			}
 
 			/* A workqueue item to move the pristine dir into place */
 			File pristineFrom = SVNWCUtils.admChild(upgradeData.rootAbsPath, PRISTINE_STORAGE_RELPATH);
 			File pristineTo = SVNWCUtils.admChild(localAbsPath, PRISTINE_STORAGE_RELPATH);
 			SVNFileUtil.ensureDirectoryExists(pristineFrom);
 
 			SVNSkel workItems = null;
 			SVNSkel workItem = wcContext.wqBuildFileMove(localAbsPath, pristineFrom, pristineTo);
 			workItems = wcContext.wqMerge(workItems, workItem);
 
 			/* A workqueue item to remove pre-wcng metadata */
 			workItem = wcContext.wqBuildPostUpgrade();
 			workItems = wcContext.wqMerge(workItems, workItem);
 
 			db.addWorkQueue(upgradeData.rootAbsPath, workItems);
 
 			db.releaseWCLock(upgradeData.rootAbsPath);
 			db.close();
 
 			/* Renaming the db file is what makes the pre-wcng into a wcng */
 			File dbFrom = SVNWCUtils.admChild(upgradeData.rootAbsPath, SDB_FILE);
 			File dbTo = SVNWCUtils.admChild(localAbsPath, SDB_FILE);
 			SVNFileUtil.rename(dbFrom, dbTo);
 
 			db.open(SVNWCDbOpenMode.ReadWrite, (ISVNOptions) null, false, false);
 			wcContext = new SVNWCContext(db, getOperation().getEventHandler());
 			wcContext.wqRun(localAbsPath);
 		} finally {
 			db.close();
 			SVNFileUtil.deleteAll(upgradeData.rootAbsPath, true);
 		}
 
 	}
 
 	private void upgradeWorkingCopy(WriteBaton parentDirBaton, SVNWCDb db, File dirAbsPath, SVNWCDbUpgradeData data, SVNHashMap reposCache,
 			RepositoryInfo reposInfo) throws SVNException {
 
 		WriteBaton dirBaton = null;
 
 		if (getOperation().getEventHandler() != null)
 			getOperation().getEventHandler().checkCancelled();
 		int oldFormat = db.getFormatTemp(dirAbsPath);
 
 		if (oldFormat >= SVNWCContext.WC_NG_VERSION) {
 			if (getOperation().getEventHandler() != null) {
 				SVNEvent event = SVNEventFactory.createSVNEvent(dirAbsPath,SVNNodeKind.DIR, null, -1, SVNEventAction.SKIP, null, null, null);
 				getOperation().getEventHandler().handleEvent(event, -1);
 			}
 			return;
 		}
 
 		ArrayList<File> children = new ArrayList<File>();
 		try {
 			getVersionedSubdirs(getWCAccess(), dirAbsPath, children, false, false);
 		} catch (SVNException ex) {
 			if (ex.isEnoent()) {
 				if (getOperation().getEventHandler() != null) {
 					SVNEvent event = SVNEventFactory.createSVNEvent(dirAbsPath, SVNNodeKind.DIR, null, -1, SVNEventAction.SKIP, null, null, null);
 					getOperation().getEventHandler().handleEvent(event, -1);
 				}
 			}
 			return;
 		}
 
 		dirBaton = upgradeToWcng(parentDirBaton, db, dirAbsPath, oldFormat,
 				data, reposCache, reposInfo);
 
 		if (getOperation().getEventHandler() != null) {
 			SVNEvent event = SVNEventFactory.createSVNEvent(dirAbsPath, SVNNodeKind.DIR, null, -1, SVNEventAction.UPGRADED_PATH, null, null, null);
 			getOperation().getEventHandler().handleEvent(event, -1);
 		}
 
 		for (File childAbsPath : children) {
 			upgradeWorkingCopy(dirBaton, db, childAbsPath, data, reposCache, reposInfo);
 		}
 	}
 
 	private WriteBaton upgradeToWcng(WriteBaton parentDirBaton, SVNWCDb db, File dirAbsPath, int oldFormat, SVNWCDbUpgradeData data, 
 			SVNHashMap reposCache, RepositoryInfo reposInfo) throws SVNException {
 		WriteBaton dirBaton = null;
 		File logFilePath = SVNWCUtils.admChild(dirAbsPath, ADM_LOG);
 
 		/* Don't try to mess with the WC if there are old log files left. */
 
 		/* Is the (first) log file present? */
 		SVNNodeKind logFileKind = SVNFileType.getNodeKind(SVNFileType.getType(logFilePath));
 		if (logFileKind == SVNNodeKind.FILE) {
 			SVNErrorMessage err = SVNErrorMessage.create(SVNErrorCode.WC_UNSUPPORTED_FORMAT,
 							"Cannot upgrade with existing logs; run a cleanup operation on this working copy using "
 									+ "a client version which is compatible with this working copy's format (such as the version "
 									+ "you are upgrading from), then retry the upgrade with the current version");
 			SVNErrorManager.error(err, SVNLogType.WC);
 		}
 
 		/*
 		 * Lock this working copy directory, or steal an existing lock. Do this 
 		 * BEFORE we read the entries. We don't want another process to modify the entries after we've read them into memory.
 		 */
 		createPhysicalLock(dirAbsPath);
 
 		/*
 		 * What's going on here?
 		 * 
 		 * We're attempting to upgrade an older working copy to the new wc-ng format. The semantics and storage mechanisms between the two are
 		 * vastly different, so it's going to be a bit painful. Here's a plan for the operation:
 		 * 
 		 * 1) Read the old 'entries' using the old-format reader.
 		 * 
 		 * 2) Create the new DB if it hasn't already been created.
 		 * 
 		 * 3) Use our compatibility code for writing entries to fill out the (new) DB state. Use the remembered checksums, since an entry has only
 		 * the MD5 not the SHA1 checksum, and in the case of a revert-base doesn't even have that.
 		 * 
 		 * 4) Convert wcprop to the wc-ng format
 		 * 
 		 * 5) Migrate regular properties to the WC-NG DB.
 		 */
 
 		/***** ENTRIES - READ *****/
 		SVNWCAccess access = getWCAccess();
 		Map<String, SVNEntry> entries = null;
 		try {
 			SVNAdminArea area = access.probeOpen(dirAbsPath, false, 0);
 			entries = area.getEntries();
 
 			SVNEntry thisDir = entries.get("");
 			ensureReposInfo(thisDir, dirAbsPath, reposInfo, reposCache);
 			/*
 			 * Cache repos UUID pairs for when a subdir doesn't have this information
 			 */
 			if (!reposCache.containsKey(thisDir.getRepositoryRootURL())) {
 				reposCache.put(thisDir.getRepositoryRootURL(),
 						thisDir.getUUID());
 			}
 
 			String dirAbsPathString = SVNFileUtil.getFilePath(dirAbsPath);
 			String oldWcRootAbsPath = SVNPathUtil.getCommonPathAncestor(dirAbsPathString, SVNFileUtil.getFilePath(data.rootAbsPath));
 			File dirRelPath = new File(SVNPathUtil.getRelativePath(oldWcRootAbsPath, dirAbsPathString));
 
 			/***** TEXT BASES *****/
 			SVNHashMap textBases = migrateTextBases(dirAbsPath, data.rootAbsPath, data.root);
 
 			/***** ENTRIES - WRITE *****/
 			try {
 				dirBaton = SvnOldUpgradeEntries.writeUpgradedEntries(parentDirBaton, db, data, dirAbsPath, entries, textBases);
 			} catch (SVNException ex) {
 				if (ex.getErrorMessage().getErrorCode() == SVNErrorCode.WC_CORRUPT) {
 					SVNErrorMessage err = ex.getErrorMessage().wrap("This working copy is corrupt and cannot be upgraded. Please check out a new working copy.");
 					SVNErrorManager.error(err, SVNLogType.WC);
 				}
 			}
 
 			/***** WC PROPS *****/
 			/* If we don't know precisely where the wcprops are, ignore them. */
 			if (oldFormat != SVNWCContext.WC_WCPROPS_LOST) {
 				/*
 				 * if (oldFormat <= SVNWCContext.WC_WCPROPS_MANY_FILES_VERSION)
 				 * allProps = readManyWcProps(dirAbsPath); else allProps = readWcProps(dirAbsPath);
 				 */
 
 				SVNHashMap cachedProps = new SVNHashMap();
 				SVNVersionedProperties verProps = area.getWCProperties("");
 				cachedProps.put("", verProps.asMap());
 				SVNHashSet children = getVesionedFiles(dirRelPath, data.root.getSDb(), data.workingCopyId);
 				for (Iterator<File> files = children.iterator(); files.hasNext();) {
 					File file = files.next();
 					verProps = area.getWCProperties(SVNFileUtil.getFileName(file));
 					cachedProps.put(SVNFileUtil.getFileName(file), verProps.asMap());
 				}
 				SvnWcDbProperties.upgradeApplyDavCache(data.root, dirRelPath, cachedProps);
 			}
 
 			/*
 			 * Upgrade all the properties (including "this dir"). Note: this must come AFTER the entries have been migrated into the database.
 			 * The upgrade process needs the children in BASE_NODE and WORKING_NODE, and to examine the resultant WORKING state.
 			 */
 			migrateProps(dirAbsPath, data, oldFormat, area);
 		} finally {
 			access.close();
 		}
 
 		return dirBaton;
 	}
 
 	/*
 	 * The checksums of one pre-1.7 text-base file. If the text-base file
 	 * exists, both checksums are filled in, otherwise both fields are NULL.
 	 */
 	public class TextBaseFileInfo {
 		public SvnChecksum sha1Checksum;
 		public SvnChecksum md5Checksum;
 	}
 
 	/*
 	 * The text-base checksums of the normal base and/or the revert-base of one
 	 * pre-1.7 versioned text file.
 	 */
 	public class TextBaseInfo {
 		public TextBaseFileInfo normalBase;
 		public TextBaseFileInfo revertBase;
 	}
 
 	/*
 	 * Copy all the text-base files from the administrative area of WC directory
 	 * dirAbsPath into the pristine store of SDB which is located in directory
 	 * newWcRootAbsPath. Returns SVNHashMap that maps name of the versioned file
 	 * to (svn_wc__text_base_info_t *) information about the pristine text.
 	 */
 	private SVNHashMap migrateTextBases(File dirAbsPath, File newWcRootAbsPath, SVNWCDbRoot root) throws SVNException {
 		SVNHashMap textBasesInfo = new SVNHashMap();
 		File textBaseDir = SVNWCUtils.admChild(dirAbsPath, TEXT_BASE_SUBDIR);
 		File[] files = SVNFileListUtil.listFiles(textBaseDir);
 		if (files == null)
 			return textBasesInfo;
 		
 		for (File textBasePath : files) {
 			SvnChecksum md5Checksum = null;
 			SvnChecksum sha1Checksum = null;
 			File pristinePath;
 
 			/* Calculate its checksums and copy it to the pristine store */
 			{
 				File tempPath = SVNFileUtil.createUniqueFile(newWcRootAbsPath, "upgrade", ".tmp", false);
 
 				InputStream readStream = SVNFileUtil.openFileForReading(textBasePath);
 
 				SVNChecksumInputStream readChecksummedIS = null;
 				try {
 					readChecksummedIS = new SVNChecksumInputStream(readStream, "SHA1");
 				} finally {
 					SVNFileUtil.closeFile(readChecksummedIS);
 					SVNFileUtil.closeFile(readStream);
 				}
 				sha1Checksum = readChecksummedIS != null ? new SvnChecksum(Kind.sha1, readChecksummedIS.getDigest()) : null;
 				readStream = SVNFileUtil.openFileForReading(textBasePath);
 				try {
 					readChecksummedIS = new SVNChecksumInputStream(readStream, SVNChecksumInputStream.MD5_ALGORITHM);
 				} finally {
 					SVNFileUtil.closeFile(readChecksummedIS);
 					SVNFileUtil.closeFile(readStream);
 				}
 				md5Checksum = readChecksummedIS != null ? new SvnChecksum(Kind.md5, readChecksummedIS.getDigest()) : null;
 
 				SVNFileUtil.copyFile(textBasePath, tempPath, true);
 
 				/* Insert a row into the pristine table. */
 				SVNSqlJetStatement stmt = root.getSDb().getStatement(SVNWCDbStatements.INSERT_OR_IGNORE_PRISTINE);
 				stmt.bindChecksum(1, sha1Checksum);
 				stmt.bindChecksum(2, md5Checksum);
 				stmt.bindLong(3, textBasePath.length());
 				stmt.exec();
 
 				pristinePath = SvnWcDbPristines.getPristineFuturePath(root, sha1Checksum);
 
 				/* Ensure any sharding directories exist. */
 				SVNFileUtil.ensureDirectoryExists(SVNFileUtil.getFileDir(pristinePath));
 
 				/*
 				 * Now move the file into the pristine store, overwriting existing files with the same checksum.
 				 */
 				SVNFileUtil.rename(tempPath, pristinePath);
 			}
 
 			/* Add the checksums for this text-base to *TEXT_BASES_INFO. */
 			{
 				boolean isRevertBase;
 				File versionedFile = removeSuffix(textBasePath, SVN_WC__REVERT_EXT);
 				if (versionedFile != null) {
 					isRevertBase = true;
 				} else {
 					versionedFile = removeSuffix(textBasePath, SVN_WC__BASE_EXT);
 					isRevertBase = false;
 				}
 
 				if (versionedFile == null) {
 					/*
 					 * Some file that doesn't end with .svn-base or .svn-revert. No idea why that would be in our administrative area, but
 					 * we shouldn't segfault on this case. Note that we already copied this file in the pristine store, but the next
 					 * cleanup will take care of that.
 					 */
 					continue;
 				}
 
 				String versionedFileName = SVNFileUtil.getFileName(versionedFile);
 
 				/*
 				 * Create a new info for this versioned file, or fill in the existing one if this is the second text-base we've found for it.
 				 */
 				TextBaseInfo info = (TextBaseInfo) textBasesInfo.get(versionedFileName);
 				if (info == null)
 					info = new TextBaseInfo();
 				TextBaseFileInfo fileInfo = new TextBaseFileInfo();
 				fileInfo.sha1Checksum = sha1Checksum;
 				fileInfo.md5Checksum = md5Checksum;
 				if (isRevertBase)
 					info.revertBase = fileInfo;
 				else
 					info.normalBase = fileInfo;
 				textBasesInfo.put(versionedFileName, info);
 			}
 
 		}
 		return textBasesInfo;
 
 	}
 
 	/*
 	 * If File name ends with SUFFIX and is longer than SUFFIX, return the part of STR that comes before SUFFIX; else return NULL.
 	 */
 	private File removeSuffix(File file, String suffix) {
 		String fileName = SVNPathUtil.getAbsolutePath(file.getPath());
 		if (fileName.length() > suffix.length() && fileName.endsWith(suffix)) {
 			return SVNFileUtil.createFilePath(fileName.substring(0, fileName.length() - suffix.length()));
 		}
 		return null;
 	}
 
 	/* Create a physical lock file in the admin directory for ABSPATH. */
 	private void createPhysicalLock(File absPath) throws SVNException {
 
 		File lockAbsPath = buildLockfilePath(absPath);
 		if (lockAbsPath.isFile()) {
 			/* Congratulations, we just stole a physical lock from somebody */
 			return;
 		}
 		SVNFileUtil.createEmptyFile(lockAbsPath);
 	}
 
 	/*
 	 * Read the properties from the file at propfileAbsPath, returning them If the propfile is NOT present, then NULL will be returned
 	 */
 	/*
 	 * private SVNHashMap readPropFile(File propfileAbsPath) throws SVNException
 	 * { return null; }
 	 */
 
 	/* Read the wcprops from all the files in the admin area of dirAbsPath */
 	/*
 	 * private SVNHashMap readManyWcProps(File dirAbsPath, SVNAdminArea area)
 	 * throws SVNException { File propsFileAbsPath = SVNWCUtils.admChild(dirAbsPath, WCPROPS_FNAME_FOR_DIR);
 	 * 
 	 * SVNHashMap allProps = new SVNHashMap();
 	 * 
 	 * SVNHashMap props = readPropFile(propsFileAbsPath); if (props != null) {
 	 * allProps.put("SVN_WC_ENTRY_THIS_DIR", props); }
 	 * 
 	 * File propsDirAbsPath = SVNWCUtils.admChild(dirAbsPath, WCPROPS_SUBDIR_FOR_FILES); 
 	 * File[] files = SVNFileListUtil.listFiles(propsDirAbsPath); 
 	 * 	for (File file : files) {
 	 * 		props = readPropFile(file); assert(props != null);
 	 * 		allProps.put(file.getAbsolutePath(), props); 
 	 * 	} 
 	 * 	return allProps; 
 	 * }
 	 */
 
 	/* For wcprops stored in a single file in this working copy */
 	/*
 	 * private SVNHashMap readWcProps(File dirAbsPath) { return null; }
 	 */
 
 	public static void wipePostUpgrade(SVNWCContext ctx, File dirAbsPath, boolean isWholeAdmin) throws SVNException {
 		ctx.checkCancelled();
 		
 		SVNWCAccess access = SVNWCAccess.newInstance(ctx.getEventHandler());
 		access.setOptions(ctx.getOptions());
 
 		ArrayList<File> subDirs = new ArrayList<File>();
 		boolean isDoDeleteDir = false;
 		try {
			getVersionedSubdirs(access, dirAbsPath, subDirs, true, true);
 		} catch (SVNException ex) {
 			return;
 		}
 
 		for (File childAbsPath : subDirs) {
 			wipePostUpgrade(ctx, childAbsPath, true);
 		}
 
 		/* ### Should we really be ignoring errors here? */
 		if (isWholeAdmin)
 			SVNFileUtil.deleteAll(SVNFileUtil.createFilePath(dirAbsPath, SVNFileUtil.getAdminDirectoryName()), true);
 		else {
 			wipeObsoleteFiles(dirAbsPath);
 		}
 
 		if (isDoDeleteDir) {
 			/*
 			 * If this was a WC-NG single database copy, this directory wouldn't be here (unless it was deleted with --keep-local)
 			 * 
 			 * If the directory is empty, we can just delete it; if not we keep it.
 			 */
 			SVNFileUtil.deleteAll(dirAbsPath, true);
 		}
 	}
 
 	public static void wipeObsoleteFiles(File dirAbsPath) throws SVNException {
 		SVNFileUtil.deleteAll(SVNWCUtils.admChild(dirAbsPath, SVNWCContext.WC_ADM_FORMAT), true);
 		SVNFileUtil.deleteAll(SVNWCUtils.admChild(dirAbsPath, SVNWCContext.WC_ADM_ENTRIES), true);
 		SVNFileUtil.deleteAll(SVNWCUtils.admChild(dirAbsPath, ADM_EMPTY_FILE), true);
 		SVNFileUtil.deleteAll(SVNWCUtils.admChild(dirAbsPath, ADM_README), true);
 
 		/*
 		 * For formats <= SVN_WC__WCPROPS_MANY_FILES_VERSION, we toss the wcprops for the directory itself, and then all the wcprops for the files.
 		 */
 		SVNFileUtil.deleteAll(SVNWCUtils.admChild(dirAbsPath, WCPROPS_FNAME_FOR_DIR), true);
 		SVNFileUtil.deleteAll(SVNWCUtils.admChild(dirAbsPath, WCPROPS_SUBDIR_FOR_FILES), true);
 
 		/* And for later formats, they are aggregated into one file. */
 		SVNFileUtil.deleteAll(SVNWCUtils.admChild(dirAbsPath, WCPROPS_ALL_DATA), true);
 
 		/* Remove the old text-base directory and the old text-base files. */
 		SVNFileUtil.deleteAll(SVNWCUtils.admChild(dirAbsPath, TEXT_BASE_SUBDIR), true);
 
 		/* Remove the old properties files... whole directories at a time. */
 		SVNFileUtil.deleteAll(SVNWCUtils.admChild(dirAbsPath, PROPS_SUBDIR), true);
 		SVNFileUtil.deleteAll(SVNWCUtils.admChild(dirAbsPath, PROP_BASE_SUBDIR), true);
 		SVNFileUtil.deleteAll(SVNWCUtils.admChild(dirAbsPath, PROP_WORKING_FOR_DIR), true);
 		SVNFileUtil.deleteAll(SVNWCUtils.admChild(dirAbsPath, PROP_BASE_FOR_DIR), true);
 		SVNFileUtil.deleteAll(SVNWCUtils.admChild(dirAbsPath, PROP_REVERT_FOR_DIR), true);
 
 		/*
 		 * #if 0 ### this checks for a write-lock, and we are not (always) taking out ### a write lock in all callers. *
 		 * SVN_ERR(svn_wc__adm_cleanup_tmp_area(db, wcroot_abspath, iterpool));
 		 * #endif
 		 */
 
 		/* Remove the old-style lock file LAST. */
 		SVNFileUtil.deleteAll(buildLockfilePath(dirAbsPath), true);
 	}
 
 	/*
 	 * Return the path of the old-school administrative lock file associated with LOCAL_DIR_ABSPATH, allocated from RESULT_POOL.
 	 */
 	private static File buildLockfilePath(File dirAbsPath) {
 		return SVNWCUtils.admChild(dirAbsPath, ADM_LOCK);
 	}
 
 	/*
	 * Return in CHILDREN, the list of all 1.6 versioned subdirectories which
	 * also exist on disk as directories. If DELETE_DIR is not NULL set
	 * *DELETE_DIR to TRUE if the directory should be deleted after migrating to
	 * WC-NG, otherwise to FALSE. If SKIP_MISSING is TRUE, don't add missing or
 	 * obstructed subdirectories to the list of children.
 	 */
 	public static boolean getVersionedSubdirs(SVNWCAccess access, File localAbsPath, ArrayList<File> children, boolean isCalculateDoDeleteDir, boolean isSkipMissing) throws SVNException {
 		boolean isDoDeleteDir = false;
 
 		Map<String, SVNEntry> entries = null;
 		
 		try {
 			SVNAdminArea area = access.probeOpen(localAbsPath, false, 0);
 			if (!area.getRoot().equals(localAbsPath)) {
 				SVNErrorMessage err = SVNErrorMessage.create(SVNErrorCode.ENTRY_NOT_FOUND, "'{0}' is not versioned directory", localAbsPath);
 				SVNErrorManager.error(err, SVNLogType.WC);
 			}
 			entries = area.getEntries();
 		} finally {
 			access.close();
 		}
 
 		SVNEntry thisDir = null;
 		for (Iterator<String> names = entries.keySet().iterator(); names.hasNext();) {
 			String name = (String) names.next();
 			SVNEntry entry = (SVNEntry) entries.get(name);
 
 			/* skip "this dir" */
 			if ("".equals(name)) {
 				thisDir = entry;
 				continue;
 			} else if (entry == null || entry.getKind() != SVNNodeKind.DIR) {
 				continue;
 			}
 
 			File childAbsPath = SVNFileUtil.createFilePath(localAbsPath, name);
 
 			if (isSkipMissing) {
 				SVNNodeKind kind = SVNFileType.getNodeKind(SVNFileType.getType(childAbsPath));
 				if (kind != SVNNodeKind.DIR)
 					continue;
 			}
 
 			children.add(childAbsPath);
 		}
 
 		if (isCalculateDoDeleteDir) {
 			isDoDeleteDir = (thisDir != null && thisDir.isScheduledForDeletion() && !thisDir.isKeepLocal());
 		}
 
 		return isDoDeleteDir;
 	}
 
 	/*
 	 * Return in CHILDREN the list of all versioned *files* in SDB that are
 	 * children of PARENT_RELPATH. These files' existence on disk is not tested.
 	 * 
 	 * This set of children is intended for property upgrades. Subdirectory's
 	 * properties exist in the subdirs.
 	 * 
 	 * Note that this uses just the SDB to locate children, which means that the
 	 * children must have been upgraded to wc-ng format.
 	 */
 
 	private SVNHashSet getVesionedFiles(File parentRelPath, SVNSqlJetDb sDb,
 			long wcId) throws SVNException {
 		SVNHashSet children = new SVNHashSet();
 
 		/* ### just select 'file' children. do we need 'symlink' in the future? */
 		SVNSqlJetStatement stmt = sDb
 				.getStatement(SVNWCDbStatements.SELECT_ALL_FILES);
 		try {
 			stmt.bindLong(1, wcId);
 			stmt.bindString(2, SVNFileUtil.getFilePath(parentRelPath));
 
 			/*
 			 * ### 10 is based on Subversion's average of 8.5 files per
 			 * versioned directory in its repository. maybe use a different
 			 * value? or ### count rows first?
 			 */
 
 			boolean haveRow = stmt.next();
 			while (haveRow) {
 				File localRelPath = SVNFileUtil
 						.createFilePath(stmt
 								.getColumnString(SVNWCDbSchema.NODES__Fields.local_relpath));
 				if (!children.contains(localRelPath))
 					children.add(localRelPath);
 				haveRow = stmt.next();
 			}
 		} finally {
 			stmt.reset();
 		}
 		return children;
 	}
 
 	private void migrateProps(File dirAbsPath, SVNWCDbUpgradeData data,
 			int originalFormat, SVNAdminArea area) throws SVNException {
 		/*
 		 * General logic here: iterate over all the immediate children of the
 		 * root (since we aren't yet in a centralized system), and for any
 		 * properties that exist, map them as follows:
 		 * 
 		 * if (revert props exist): revert -> BASE base -> WORKING working ->
 		 * ACTUAL else if (prop pristine is working [as defined in props.c] ):
 		 * base -> WORKING working -> ACTUAL else: base -> BASE working ->
 		 * ACTUAL
 		 * 
 		 * ### the middle "test" should simply look for a WORKING_NODE row
 		 * 
 		 * Note that it is legal for "working" props to be missing. That implies
 		 * no local changes to the properties.
 		 */
 
 		String dirAbsPathString = SVNFileUtil.getFilePath(dirAbsPath);
 		String oldWcRootAbsPath = SVNPathUtil.getCommonPathAncestor(
 				dirAbsPathString, SVNFileUtil.getFilePath(data.rootAbsPath));
 		File dirRelPath = new File(SVNPathUtil.getRelativePath(
 				oldWcRootAbsPath, dirAbsPathString));
 
 		/* Migrate the props for "this dir". */
 		migrateNodeProps(dirAbsPath, data, "", originalFormat, area);
 
 		SVNHashSet children = getVesionedFiles(dirRelPath, data.root.getSDb(),
 				data.workingCopyId);
 
 		/* Iterate over all the files in this SDB. */
 		for (Iterator<File> files = children.iterator(); files.hasNext();) {
 			File file = files.next();
 			migrateNodeProps(dirAbsPath, data, SVNFileUtil.getFileName(file),
 					originalFormat, area);
 		}
 	}
 
 	/* Migrate the properties for one node (LOCAL_ABSPATH). */
 	private void migrateNodeProps(File dirAbsPath, SVNWCDbUpgradeData data,
 			String name, int originalFormat, SVNAdminArea area)
 			throws SVNException {
 		String dirAbsPathString = SVNFileUtil.getFilePath(dirAbsPath);
 		String oldWcRootAbsPath = SVNPathUtil.getCommonPathAncestor(
 				dirAbsPathString, SVNFileUtil.getFilePath(data.rootAbsPath));
 		File dirRelPath = new File(SVNPathUtil.getRelativePath(
 				oldWcRootAbsPath, dirAbsPathString));
 
 		SvnWcDbProperties.upgradeApplyProperties(data.root, data.rootAbsPath,
 				SVNFileUtil.createFilePath(dirRelPath, name), area
 						.getBaseProperties(name).asMap(),
 				area.getProperties(name).asMap(), area
 						.getRevertProperties(name).asMap(), originalFormat);
 
 	}
 
 }
