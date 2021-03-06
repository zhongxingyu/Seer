 /*
  * ====================================================================
  * Copyright (c) 2004 TMate Software Ltd. All rights reserved.
  *
  * This software is licensed as described in the file COPYING, which you should
  * have received as part of this distribution. The terms are also available at
  * http://tmate.org/svn/license.html. If newer versions of this license are
  * posted there, you may use a newer version instead, at your option.
  * ====================================================================
  */
 package org.tmatesoft.svn.core.wc;
 
 import org.tmatesoft.svn.core.SVNProperty;
 import org.tmatesoft.svn.core.internal.wc.SVNDirectory;
 import org.tmatesoft.svn.core.internal.wc.SVNEntries;
 import org.tmatesoft.svn.core.internal.wc.SVNEntry;
 import org.tmatesoft.svn.core.internal.wc.SVNErrorManager;
 import org.tmatesoft.svn.core.internal.wc.SVNEventFactory;
 import org.tmatesoft.svn.core.internal.wc.SVNExternalInfo;
 import org.tmatesoft.svn.core.internal.wc.SVNFileType;
 import org.tmatesoft.svn.core.internal.wc.SVNFileUtil;
 import org.tmatesoft.svn.core.internal.wc.SVNProperties;
 import org.tmatesoft.svn.core.internal.wc.SVNTranslator;
 import org.tmatesoft.svn.core.internal.wc.SVNWCAccess;
 import org.tmatesoft.svn.core.io.SVNDirEntry;
 import org.tmatesoft.svn.core.io.SVNException;
 import org.tmatesoft.svn.core.io.SVNLock;
 import org.tmatesoft.svn.core.io.SVNNodeKind;
 import org.tmatesoft.svn.core.io.SVNRepository;
 import org.tmatesoft.svn.core.io.SVNRepositoryLocation;
 import org.tmatesoft.svn.util.DebugLog;
 import org.tmatesoft.svn.util.PathUtil;
 import org.tmatesoft.svn.util.TimeUtil;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 
 /**
  * @version 1.0
  * @author TMate Software Ltd.
  */
 public class SVNWCClient extends SVNBasicClient {
 
     public SVNWCClient() {
     }
 
     public SVNWCClient(ISVNEventHandler eventDispatcher) {
         super(eventDispatcher);
     }
 
     public SVNWCClient(ISVNOptions options, ISVNEventHandler eventDispatcher) {
         super(options, eventDispatcher);
     }
 
     public SVNWCClient(ISVNRepositoryFactory repositoryFactory,
             ISVNOptions options, ISVNEventHandler eventDispatcher) {
         super(repositoryFactory, options, eventDispatcher);
     }
 
     public void doGetFileContents(File path, SVNRevision pegRevision,
             SVNRevision revision, boolean expandKeywords, OutputStream dst)
             throws SVNException {
         if (dst == null) {
             return;
         }
         if (revision == null || !revision.isValid()) {
             revision = SVNRevision.WORKING;
         }
         if (revision == SVNRevision.COMMITTED) {
             revision = SVNRevision.BASE;
         }
         SVNWCAccess wcAccess = createWCAccess(path);
         if ("".equals(wcAccess.getTargetName())
                 || wcAccess.getTarget() != wcAccess.getAnchor()) {
             SVNErrorManager.error("svn: '" + path + "' refers to a directory");
         }
         String name = wcAccess.getTargetName();
         if (revision == SVNRevision.WORKING || revision == SVNRevision.BASE) {
             File file = wcAccess.getAnchor().getBaseFile(name, false);
             boolean delete = false;
             SVNEntry entry = wcAccess.getAnchor().getEntries().getEntry(wcAccess.getTargetName(), false);
             if (entry == null) {
                 SVNErrorManager.error("svn: '" + path
                         + "' is not under version control or doesn't exist");
             }
            wcAccess.open(true, false);
             try {
 
                 if (revision == SVNRevision.BASE) {
                     if (expandKeywords) {
                         delete = true;
                         file = wcAccess.getAnchor().getBaseFile(name, true).getParentFile();
                         file = SVNFileUtil.createUniqueFile(file, name, ".tmp");
                         SVNTranslator.translate(wcAccess.getAnchor(), name,
                                 SVNFileUtil.getBasePath(wcAccess.getAnchor()
                                         .getBaseFile(name, false)), SVNFileUtil
                                         .getBasePath(file), true, false);
                     }
                 } else {
                     if (!expandKeywords) {
                         delete = true;
                         file = wcAccess.getAnchor().getBaseFile(name, true).getParentFile();
                         file = SVNFileUtil.createUniqueFile(file, name, ".tmp");
                         SVNTranslator.translate(wcAccess.getAnchor(), name,
                                 name, SVNFileUtil.getBasePath(file), false,
                                 false);
                     } else {
                         file = wcAccess.getAnchor().getFile(name);
                     }
                 }
             } finally {
                wcAccess.close(true);
                 if (file != null && file.exists()) {
                     InputStream is = SVNFileUtil.openFileForReading(file);
                     try {
                         int r;
                         while ((r = is.read()) >= 0) {
                             dst.write(r);
                         }
                     } catch (IOException e) {
                         DebugLog.error(e);
                     } finally {
                         SVNFileUtil.closeFile(is);
                         if (delete) {
                             file.delete();
                         }
                     }
                 }
             }
         } else {
             String url = wcAccess.getTargetEntryProperty(SVNProperty.URL);
             if (wcAccess.getTargetEntryProperty(SVNProperty.COPYFROM_URL) != null) {
                 url = wcAccess.getTargetEntryProperty(SVNProperty.COPYFROM_URL);
             }
             if (pegRevision == null || !pegRevision.isValid()) {
                 pegRevision = SVNRevision.parse(wcAccess
                         .getTargetEntryProperty(SVNProperty.REVISION));
             }
             long revNumber = getRevisionNumber(path, revision);
             long pegRevisionNumber = getRevisionNumber(path, pegRevision);
             if (!expandKeywords) {
                 doGetFileContents(url, SVNRevision.create(pegRevisionNumber),
                         SVNRevision.create(revNumber), expandKeywords, dst);
             } else {
                 File tmpFile = SVNFileUtil.createUniqueFile(new File(path
                         .getParentFile(), ".svn/tmp/text-base"),
                         path.getName(), ".tmp");
                 File tmpFile2 = null;
                 OutputStream os = null;
                 InputStream is = null;
                 try {
                     os = SVNFileUtil.openFileForWriting(tmpFile);
                     doGetFileContents(url, SVNRevision
                             .create(pegRevisionNumber), SVNRevision
                             .create(revNumber), false, os);
                     SVNFileUtil.closeFile(os);
                     os = null;
                     // translate
                     tmpFile2 = SVNFileUtil.createUniqueFile(new File(path
                             .getParentFile(), ".svn/tmp/text-base"), path
                             .getName(), ".tmp");
                     boolean special = wcAccess.getAnchor().getProperties(
                             path.getName(), false).getPropertyValue(
                             SVNProperty.SPECIAL) != null;
                     if (special) {
                         tmpFile2 = tmpFile;
                     } else {
                         SVNTranslator.translate(wcAccess.getAnchor(), path
                                 .getName(), SVNFileUtil.getBasePath(tmpFile),
                                 SVNFileUtil.getBasePath(tmpFile2), true, false);
                     }
                     // cat tmp file
                     is = SVNFileUtil.openFileForReading(tmpFile2);
                     int r;
                     while ((r = is.read()) >= 0) {
                         dst.write(r);
                     }
                 } catch (IOException e) {
                     SVNErrorManager.error("svn: " + e.getMessage());
                 } finally {
                     SVNFileUtil.closeFile(os);
                     SVNFileUtil.closeFile(is);
                     if (tmpFile != null) {
                         tmpFile.delete();
                     }
                     if (tmpFile2 != null) {
                         tmpFile2.delete();
                     }
                 }
             }
         }
     }
 
     public void doGetFileContents(String url, SVNRevision pegRevision,
             SVNRevision revision, boolean expandKeywords, OutputStream dst)
             throws SVNException {
         url = validateURL(url);
         revision = revision == null || !revision.isValid() ? SVNRevision.HEAD
                 : revision;
         // now get contents from URL.
         Map properties = new HashMap();
         long[] revs = new long[1];
         SVNRepository repos = createRepository(null, url, pegRevision,
                 revision, revs);
 
         SVNNodeKind nodeKind = repos.checkPath("", revs[0]);
         if (nodeKind == SVNNodeKind.DIR) {
             SVNErrorManager
                     .error("svn: URL '" + url + " refers to a directory");
         }
         if (nodeKind != SVNNodeKind.FILE) {
             return;
         }
         OutputStream os = null;
         InputStream is = null;
         File file = SVNFileUtil.createUniqueFile(new File("."), "svn-contents",
                 ".tmp1");
         File file2 = SVNFileUtil.createUniqueFile(new File("."),
                 "svn-contents", ".tmp2");
         try {
             os = new FileOutputStream(file);
             repos.getFile("", revs[0], properties, os);
             os.close();
             os = null;
             if (expandKeywords) {
                 // use props at committed (peg) revision, not those.
                 String keywords = (String) properties.get(SVNProperty.KEYWORDS);
                 String eol = (String) properties.get(SVNProperty.EOL_STYLE);
                 byte[] eolBytes = SVNTranslator.getWorkingEOL(eol);
                 Map keywordsMap = SVNTranslator.computeKeywords(keywords, url,
                         (String) properties.get("svn:author"),
                         (String) properties.get("svn:date"), Long
                                 .toString(revs[0]));
                 SVNTranslator.translate(file, file2, eolBytes, keywordsMap,
                         false, true);
             } else {
                 file2 = file;
             }
 
             is = SVNFileUtil.openFileForReading(file2);
             int r;
             while ((r = is.read()) >= 0) {
                 dst.write(r);
             }
         } catch (IOException e) {
             //
             e.printStackTrace();
         } finally {
             SVNFileUtil.closeFile(os);
             SVNFileUtil.closeFile(is);
             if (file != null) {
                 file.delete();
             }
             if (file2 != null) {
                 file2.delete();
             }
         }
     }
 
     public void doCleanup(File path) throws SVNException {
         SVNFileType fType = SVNFileType.getType(path);
         if (fType == SVNFileType.NONE) {
             SVNErrorManager.error("svn: '" + path + "' does not exist");
         } else if (fType == SVNFileType.FILE || fType == SVNFileType.SYMLINK) {
             path = path.getParentFile();
         }
         if (!SVNWCAccess.isVersionedDirectory(path)) {
             SVNErrorManager.error("svn: '" + path
                     + "' is not under version control");
         }
         SVNWCAccess wcAccess = createWCAccess(path);
         wcAccess.open(true, true, true);
         wcAccess.getAnchor().cleanup();
         wcAccess.close(true);
     }
 
     public void doSetProperty(File path, String propName, String propValue,
             boolean force, boolean recursive, ISVNPropertyHandler handler)
             throws SVNException {
         propName = validatePropertyName(propName);
         if (REVISION_PROPS.contains(propName)) {
             SVNErrorManager.error("svn: Revision property '" + propName
                     + "' not allowed in this context");
         } else if (propName.startsWith(SVNProperty.SVN_WC_PREFIX)) {
             SVNErrorManager.error("svn: '" + propName
                     + "' is a wcprop , thus not accessible to clients");
         }
         propValue = validatePropertyValue(propName, propValue, force);
         SVNWCAccess wcAccess = createWCAccess(path);
         try {
             wcAccess.open(true, recursive);
             doSetLocalProperty(wcAccess.getAnchor(), wcAccess.getTargetName(),
                     propName, propValue, force, recursive, handler);
         } finally {
             wcAccess.close(true);
         }
     }
 
     public void doSetRevisionProperty(File path, SVNRevision revision,
             String propName, String propValue, boolean force,
             ISVNPropertyHandler handler) throws SVNException {
         propName = validatePropertyName(propName);
         if (propName.startsWith(SVNProperty.SVN_WC_PREFIX)) {
             SVNErrorManager.error("svn: '" + propName
                     + "' is a wcprop , thus not accessible to clients");
         }
         SVNWCAccess wcAccess = createWCAccess(path);
         try {
             wcAccess.open(true, false);
             String url = wcAccess.getTargetEntryProperty(SVNProperty.URL);
             SVNRevision pegRevision = SVNRevision.parse(wcAccess
                     .getTargetEntryProperty(SVNProperty.REVISION));
             doSetRevisionProperty(url, pegRevision, revision, propName,
                     propValue, force, handler);
         } finally {
             wcAccess.close(true);
         }
     }
 
     public void doSetRevisionProperty(String url, SVNRevision pegRevision,
             SVNRevision revision, String propName, String propValue,
             boolean force, ISVNPropertyHandler handler) throws SVNException {
         propName = validatePropertyName(propName);
         if (!force && "svn:author".equals(propName) && propValue != null
                 && propValue.indexOf('\n') >= 0) {
             SVNErrorManager.error("svn: Value will not be set unless forced");
         }
         if (propName.startsWith(SVNProperty.SVN_WC_PREFIX)) {
             SVNErrorManager.error("svn: '" + propName
                     + "' is a wcprop , thus not accessible to clients");
         }
         if (revision == null || !revision.isValid()) {
             revision = SVNRevision.HEAD;
         }
         url = validateURL(url);
         url = getURL(url, pegRevision, revision);
         long revNumber = getRevisionNumber(url, revision);
         //
         SVNRepository repos = createRepository(url);
         repos.setRevisionPropertyValue(revNumber, propName, propValue);
         if (handler != null) {
             handler.handleProperty(revNumber + "", new SVNPropertyData(
                     propName, propValue));
         }
     }
 
     public SVNPropertyData doGetProperty(final File path, String propName,
             SVNRevision pegRevision, SVNRevision revision, boolean recursive)
             throws SVNException {
         final SVNPropertyData[] data = new SVNPropertyData[1];
         doGetProperty(path, propName, pegRevision, revision, recursive, new ISVNPropertyHandler() {
             public void handleProperty(File file, SVNPropertyData property) {
                 if (data[0] == null && path.equals(file)) {
                     data[0] = property;
                 }
             }
             public void handleProperty(String url, SVNPropertyData property) {
             }
         });
         return data[0];
     }
 
     public SVNPropertyData doGetProperty(String url, String propName,
             SVNRevision pegRevision, SVNRevision revision, boolean recursive)
             throws SVNException {
         final SVNPropertyData[] data = new SVNPropertyData[1];
         final String canonURL = SVNRepositoryLocation.parseURL(url).toCanonicalForm();
         doGetProperty(url, propName, pegRevision, revision, recursive, new ISVNPropertyHandler() {
             public void handleProperty(File file, SVNPropertyData property) {
             }
             public void handleProperty(String location, SVNPropertyData property) throws SVNException {
                 location = SVNRepositoryLocation.parseURL(location).toCanonicalForm();
                 if (data[0] == null && canonURL.equals(location)) {
                     data[0] = property;
                 }
             }
         });
         return data[0];
     }
 
     public void doGetProperty(File path, String propName,
             SVNRevision pegRevision, SVNRevision revision, boolean recursive,
             ISVNPropertyHandler handler) throws SVNException {
         if (propName != null && propName.startsWith(SVNProperty.SVN_WC_PREFIX)) {
             SVNErrorManager.error("svn: '" + propName
                     + "' is a wcprop , thus not accessible to clients");
         }
         if (revision == null || !revision.isValid()) {
             revision = SVNRevision.WORKING;
         }
         SVNWCAccess wcAccess = createWCAccess(path);
         try {
             wcAccess.open(false, recursive);
             if (revision != SVNRevision.WORKING && revision != SVNRevision.BASE) {
                 String url = wcAccess.getTargetEntryProperty(SVNProperty.URL);
                 if (pegRevision == null || !pegRevision.isValid()) {
                     pegRevision = SVNRevision.parse(wcAccess
                             .getTargetEntryProperty(SVNProperty.REVISION));
                 }
                 revision = SVNRevision
                         .create(getRevisionNumber(path, revision));
                 doGetProperty(url, propName, pegRevision, revision, recursive,
                         handler);
                 return;
             }
             // local prop.
             doGetLocalProperty(wcAccess.getAnchor(), wcAccess.getTargetName(),
                     propName, revision, recursive, handler);
         } finally {
             wcAccess.close(false);
         }
     }
 
     public void doGetProperty(String url, String propName,
             SVNRevision pegRevision, SVNRevision revision, boolean recursive,
             ISVNPropertyHandler handler) throws SVNException {
         if (propName != null && propName.startsWith(SVNProperty.SVN_WC_PREFIX)) {
             SVNErrorManager.error("svn: '" + propName
                     + "' is a wcprop , thus not accessible to clients");
         }
         if (revision == null || !revision.isValid()) {
             revision = SVNRevision.HEAD;
         }
         url = validateURL(url);
         url = getURL(url, pegRevision, revision);
 
         SVNRepository repos = createRepository(url);
         doGetRemoteProperty(url, "", repos, propName, revision, recursive,
                 handler);
     }
 
     public void doGetRevisionProperty(File path, String propName,
             SVNRevision pegRev, SVNRevision revision,
             ISVNPropertyHandler handler) throws SVNException {
         if (propName != null && propName.startsWith(SVNProperty.SVN_WC_PREFIX)) {
             SVNErrorManager.error("svn: '" + propName
                     + "' is a wcprop , thus not accessible to clients");
         }
         SVNWCAccess wcAccess = createWCAccess(path);
         try {
             wcAccess.open(true, false);
             String url = wcAccess.getTargetEntryProperty(SVNProperty.URL);
             long revNumber = getRevisionNumber(path, revision);
             if (pegRev == null || !pegRev.isValid()) {
                 pegRev = SVNRevision.parse(wcAccess
                         .getTargetEntryProperty(SVNProperty.REVISION));
             }
             revision = SVNRevision.create(revNumber);
             url = getURL(url, pegRev, revision);
             doGetRevisionProperty(url, propName, revision, handler);
         } finally {
             wcAccess.close(true);
         }
     }
 
     public void doGetRevisionProperty(String url, String propName,
             SVNRevision revision, ISVNPropertyHandler handler)
             throws SVNException {
         if (propName != null && propName.startsWith(SVNProperty.SVN_WC_PREFIX)) {
             SVNErrorManager.error("svn: '" + propName
                     + "' is a wcprop , thus not accessible to clients");
         }
         if (revision == null || !revision.isValid()) {
             revision = SVNRevision.HEAD;
         }
         url = validateURL(url);
         SVNRepository repos = createRepository(url);
         long revNumber = getRevisionNumber(url, revision);
         if (propName != null) {
             String value = repos.getRevisionPropertyValue(revNumber, propName);
             if (value != null) {
                 handler.handleProperty(revNumber + "", new SVNPropertyData(
                         propName, value));
             }
         } else {
             Map props = new HashMap();
             repos.getRevisionProperties(revNumber, props);
             for (Iterator names = props.keySet().iterator(); names.hasNext();) {
                 String name = (String) names.next();
                 String value = (String) props.get(name);
                 handler.handleProperty(revNumber + "", new SVNPropertyData(
                         name, value));
             }
         }
     }
 
     public void doDelete(File path, boolean force, boolean dryRun)
             throws SVNException {
         SVNWCAccess wcAccess = createWCAccess(path);
         try {
             wcAccess.open(true, true, true);
             if (!force) {
                 wcAccess.getAnchor().canScheduleForDeletion(
                         wcAccess.getTargetName());
             }
             if (!dryRun) {
                 wcAccess.getAnchor().scheduleForDeletion(
                         wcAccess.getTargetName());
             }
         } finally {
             wcAccess.close(true);
         }
     }
 
     public void doAdd(File path, boolean force, boolean mkdir,
             boolean climbUnversionedParents, boolean recursive)
             throws SVNException {
         if (!path.exists() && !mkdir) {
             SVNErrorManager.error("svn: '" + path + "' doesn't exist");
         }
         if (climbUnversionedParents) {
             File parent = path.getParentFile();
             if (parent != null
                     && SVNWCUtil.getWorkingCopyRoot(path, true) == null) {
                 // path is in unversioned dir, try to add this parent before
                 // path.
                 try {
                     doAdd(parent, false, mkdir, climbUnversionedParents, false);
                 } catch (SVNException e) {
                     SVNErrorManager.error("svn: '" + path
                             + "' doesn't belong to svn working copy");
                 }
             }
         }
         SVNWCAccess wcAccess = createWCAccess(path);
         try {
             wcAccess.open(true, recursive);
             String name = wcAccess.getTargetName();
 
             if ("".equals(name) && !force) {
                 SVNErrorManager
                         .error("svn: '"
                                 + path
                                 + "' is the root of the working copy, it couldn't be scheduled for addition");
             }
             SVNDirectory dir = wcAccess.getAnchor();
             SVNFileType ftype = SVNFileType.getType(path);
             if (ftype == SVNFileType.FILE || ftype == SVNFileType.SYMLINK) {
                 addSingleFile(dir, name);
             } else if (ftype == SVNFileType.DIRECTORY && recursive) {
                 // add dir and recurse.
                 addDirectory(wcAccess, wcAccess.getAnchor(), name, force);
             } else {
                 // add single dir, no force - report error anyway.
                 dir.add(wcAccess.getTargetName(), mkdir, false);
             }
         } finally {
             wcAccess.close(true);
         }
     }
 
     public void doRevert(File path, boolean recursive) throws SVNException {
         SVNWCAccess wcAccess = createWCAccess(path);
 
         // force recursive lock.
         boolean reverted = false;
         boolean replaced = false;
         SVNNodeKind kind = null;
         Collection recursiveFiles = new ArrayList();
         try {
             wcAccess.open(true, false);
             SVNEntry entry = wcAccess.getAnchor().getEntries().getEntry(
                     wcAccess.getTargetName(), true);
             if (entry == null) {
                 SVNErrorManager.error("svn: '" + path
                         + "' is not under version control");
             }
             kind = entry.getKind();
             File file = wcAccess.getAnchor().getFile(wcAccess.getTargetName());
             if (entry.isDirectory()) {
                 if (!entry.isScheduledForAddition() && !file.isDirectory()) {
                     handleEvent(SVNEventFactory.createNotRevertedEvent(
                             wcAccess, wcAccess.getAnchor(), entry),
                             ISVNEventHandler.UNKNOWN);
                     return;
                 }
             }
 
             SVNEvent event = SVNEventFactory.createRevertedEvent(wcAccess,
                     wcAccess.getAnchor(), entry);
             if (entry.isScheduledForAddition()) {
                 boolean deleted = entry.isDeleted();
                 if (entry.isFile()) {
                     wcAccess.getAnchor().destroy(entry.getName(), false);
                 } else if (entry.isDirectory()) {
                     if ("".equals(wcAccess.getTargetName())) {
                         SVNErrorManager
                                 .error("svn: Cannot revert addition of the root directory; please try again from the parent directory");
                     }
                     if (!file.exists()) {
                         wcAccess.getAnchor().getEntries().deleteEntry(
                                 entry.getName());
                     } else {
                         wcAccess.open(true, true, true);
                         wcAccess.getAnchor().destroy(entry.getName(), false);
                     }
                 }
                 reverted = true;
                 if (deleted && !"".equals(wcAccess.getTargetName())) {
                     // we are not in the root.
                     SVNEntry replacement = wcAccess.getAnchor().getEntries()
                             .addEntry(entry.getName());
                     replacement.setDeleted(true);
                     replacement.setKind(kind);
                 }
             } else if (entry.isScheduledForReplacement()
                     || entry.isScheduledForDeletion()) {
                 replaced = entry.isScheduledForReplacement();
                 if (entry.isDirectory()) {
                     reverted |= wcAccess.getTarget().revert("");
                 } else {
                     reverted |= wcAccess.getAnchor().revert(entry.getName());
                 }
                 reverted = true;
             } else {
                 if (entry.isDirectory()) {
                     reverted |= wcAccess.getTarget().revert("");
                 } else {
                     reverted |= wcAccess.getAnchor().revert(entry.getName());
                 }
             }
             if (reverted) {
                 if (kind == SVNNodeKind.DIR && replaced) {
                     recursive = true;
                 }
                 if (!"".equals(wcAccess.getTargetName())) {
                     entry.unschedule();
                     entry.setConflictNew(null);
                     entry.setConflictOld(null);
                     entry.setConflictWorking(null);
                     entry.setPropRejectFile(null);
                 }
                 wcAccess.getAnchor().getEntries().save(false);
                 if (kind == SVNNodeKind.DIR) {
                     DebugLog.log("reverted, unscheduling target root entry: "
                             + wcAccess.getTarget().getRoot());
                     SVNEntry inner = wcAccess.getTarget().getEntries()
                             .getEntry("", true);
                     if (inner != null) {
                         // may be null if it was removed from wc.
                         inner.unschedule();
                         inner.setConflictNew(null);
                         inner.setConflictOld(null);
                         inner.setConflictWorking(null);
                         inner.setPropRejectFile(null);
                     }
                 }
                 wcAccess.getTarget().getEntries().save(false);
             }
             if (kind == SVNNodeKind.DIR && recursive) {
                 // iterate over targets and revert
                 for (Iterator ents = wcAccess.getTarget().getEntries().entries(
                         true); ents.hasNext();) {
                     SVNEntry childEntry = (SVNEntry) ents.next();
                     if ("".equals(childEntry.getName())) {
                         continue;
                     }
                     recursiveFiles.add(wcAccess.getTarget().getFile(childEntry.getName()));
                 }
             }
             if (reverted) {
                 // fire reverted event.
                 handleEvent(event, ISVNEventHandler.UNKNOWN);
             }
         } finally {
             wcAccess.close(true);
         }
         // recurse
         if (kind == SVNNodeKind.DIR && recursive) {
             // iterate over targets and revert
             for (Iterator files = recursiveFiles.iterator(); files.hasNext();) {
                 File file = (File) files.next();
                 doRevert(file, recursive);
             }
         }
     }
 
     public void doResolve(File path, boolean recursive) throws SVNException {
         SVNWCAccess wcAccess = createWCAccess(path);
         try {
             wcAccess.open(true, recursive);
             String target = wcAccess.getTargetName();
             SVNDirectory dir = wcAccess.getAnchor();
 
             if (wcAccess.getTarget() != wcAccess.getAnchor()) {
                 target = "";
                 dir = wcAccess.getTarget();
             }
             SVNEntry entry = dir.getEntries().getEntry(target, false);
             if (entry == null) {
                 SVNErrorManager.error("svn: '" + path
                         + "' is not under version control");
                 return;
             }
 
             if (!recursive || entry.getKind() != SVNNodeKind.DIR) {
                 if (dir.markResolved(target, true, true)) {
                     SVNEvent event = SVNEventFactory.createResolvedEvent(
                             wcAccess, dir, entry);
                     handleEvent(event, ISVNEventHandler.UNKNOWN);
                 }
             } else {
                 doResolveAll(wcAccess, dir);
             }
         } finally {
             wcAccess.close(true);
         }
     }
 
     private void doResolveAll(SVNWCAccess access, SVNDirectory dir)
             throws SVNException {
         SVNEntries entries = dir.getEntries();
         Collection childDirs = new ArrayList();
         for (Iterator ents = entries.entries(false); ents.hasNext();) {
             SVNEntry entry = (SVNEntry) ents.next();
             if ("".equals(entry.getName()) || entry.isFile()) {
                 if (dir.markResolved(entry.getName(), true, true)) {
                     SVNEvent event = SVNEventFactory.createResolvedEvent(
                             access, dir, entry);
                     handleEvent(event, ISVNEventHandler.UNKNOWN);
                 }
             } else if (entry.isDirectory()) {
                 SVNDirectory childDir = dir.getChildDirectory(entry.getName());
                 if (childDir != null) {
                     childDirs.add(childDir);
                 }
             }
         }
         entries.save(true);
         for (Iterator dirs = childDirs.iterator(); dirs.hasNext();) {
             SVNDirectory child = (SVNDirectory) dirs.next();
             doResolveAll(access, child);
         }
     }
 
     public void doLock(File[] paths, boolean stealLock, String lockMessage)
             throws SVNException {
         Map entriesMap = new HashMap();
         for (int i = 0; i < paths.length; i++) {
             SVNWCAccess wcAccess = createWCAccess(paths[i]);
             try {
                 wcAccess.open(true, false);
                 SVNEntry entry = wcAccess.getAnchor().getEntries().getEntry(
                         wcAccess.getTargetName(), true);
                 if (entry == null || entry.isHidden()) {
                     SVNErrorManager.error("svn: '" + entry.getName()
                             + "' is not under version control");
                 }
                 if (entry.getURL() == null) {
                     SVNErrorManager.error("svn: '" + entry.getName()
                             + "' has no URL");
                 }
                 SVNRevision revision = stealLock ? SVNRevision.UNDEFINED
                         : SVNRevision.create(entry.getRevision());
                 entriesMap
                         .put(entry.getURL(), new LockInfo(paths[i], revision));
                 wcAccess.getAnchor().getEntries().close();
             } finally {
                 wcAccess.close(true);
             }
         }
         for (Iterator urls = entriesMap.keySet().iterator(); urls.hasNext();) {
             String url = (String) urls.next();
             LockInfo info = (LockInfo) entriesMap.get(url);
             SVNWCAccess wcAccess = createWCAccess(info.myFile);
 
             SVNRepository repos = createRepository(url);
             SVNLock lock;
             try {
                 lock = repos.setLock("", lockMessage, stealLock,
                         info.myRevision.getNumber());
             } catch (SVNException error) {
                 handleEvent(SVNEventFactory.createLockEvent(wcAccess, wcAccess
                         .getTargetName(), SVNEventAction.LOCK_FAILED, null,
                         error.getMessage()), ISVNEventHandler.UNKNOWN);
                 continue;
             }
             try {
                 wcAccess.open(true, false);
                 SVNEntry entry = wcAccess.getAnchor().getEntries().getEntry(
                         wcAccess.getTargetName(), true);
                 entry.setLockToken(lock.getID());
                 entry.setLockComment(lock.getComment());
                 entry.setLockOwner(lock.getOwner());
                 entry.setLockCreationDate(TimeUtil.formatDate(lock.getCreationDate()));
                 if (wcAccess.getAnchor().getProperties(entry.getName(), false).getPropertyValue(SVNProperty.NEEDS_LOCK) != null) {
                     SVNFileUtil.setReadonly(wcAccess.getAnchor().getFile(entry.getName()), false);
                 }
                 wcAccess.getAnchor().getEntries().save(true);
                 wcAccess.getAnchor().getEntries().close();
                 handleEvent(SVNEventFactory.createLockEvent(wcAccess, wcAccess
                         .getTargetName(), SVNEventAction.LOCKED, lock, null),
                         ISVNEventHandler.UNKNOWN);
             } catch (SVNException e) {
                 handleEvent(SVNEventFactory.createLockEvent(wcAccess, wcAccess
                         .getTargetName(), SVNEventAction.LOCK_FAILED, lock, e
                         .getMessage()), ISVNEventHandler.UNKNOWN);
             } finally {
                 wcAccess.close(true);
             }
         }
     }
 
     public void doLock(String[] urls, boolean stealLock, String lockMessage)
             throws SVNException {
         for (int i = 0; i < urls.length; i++) {
             String url = validateURL(urls[i]);
             SVNRepository repos = createRepository(url);
             SVNLock lock = null;
             try {
                 lock = repos.setLock("", lockMessage, stealLock, -1);
             } catch (SVNException error) {
                 handleEvent(SVNEventFactory.createLockEvent(url,
                         SVNEventAction.LOCK_FAILED, lock, null),
                         ISVNEventHandler.UNKNOWN);
                 continue;
             }
             handleEvent(SVNEventFactory.createLockEvent(url,
                     SVNEventAction.LOCKED, lock, null),
                     ISVNEventHandler.UNKNOWN);
         }
     }
 
     public void doUnlock(File[] paths, boolean breakLock) throws SVNException {
         Map entriesMap = new HashMap();
         for (int i = 0; i < paths.length; i++) {
             SVNWCAccess wcAccess = createWCAccess(paths[i]);
             try {
                 wcAccess.open(true, false);
                 SVNEntry entry = wcAccess.getAnchor().getEntries().getEntry(
                         wcAccess.getTargetName(), true);
                 if (entry == null || entry.isHidden()) {
                     SVNErrorManager.error("svn: '" + entry.getName()
                             + "' is not under version control");
                 }
                 if (entry.getURL() == null) {
                     SVNErrorManager.error("svn: '" + entry.getName()
                             + "' has no URL");
                 }
                 String lockToken = entry.getLockToken();
                 if (!breakLock && lockToken == null) {
                     SVNErrorManager.error("svn: '" + entry.getName()
                             + "' is not locked in this working copy");
                 }
                 entriesMap.put(entry.getURL(),
                         new LockInfo(paths[i], lockToken));
                 wcAccess.getAnchor().getEntries().close();
             } finally {
                 wcAccess.close(true);
             }
         }
         for (Iterator urls = entriesMap.keySet().iterator(); urls.hasNext();) {
             String url = (String) urls.next();
             LockInfo info = (LockInfo) entriesMap.get(url);
             SVNWCAccess wcAccess = createWCAccess(info.myFile);
 
             SVNRepository repos = createRepository(url);
             SVNLock lock = null;
             boolean removeLock;
             try {
                 repos.removeLock("", info.myToken, breakLock);
                 removeLock = true;
             } catch (SVNException error) {
                 // remove lock if error is owner_mismatch.
                 removeLock = true;
             }
             if (!removeLock) {
                 handleEvent(SVNEventFactory.createLockEvent(wcAccess, wcAccess
                         .getTargetName(), SVNEventAction.UNLOCK_FAILED, null,
                         "unlock failed"), ISVNEventHandler.UNKNOWN);
                 continue;
             }
             try {
                 wcAccess.open(true, false);
                 SVNEntry entry = wcAccess.getAnchor().getEntries().getEntry(
                         wcAccess.getTargetName(), true);
                 entry.setLockToken(null);
                 entry.setLockComment(null);
                 entry.setLockOwner(null);
                 entry.setLockCreationDate(null);
                 wcAccess.getAnchor().getEntries().save(true);
                 wcAccess.getAnchor().getEntries().close();
                 if (wcAccess.getAnchor().getProperties(entry.getName(), false).getPropertyValue(SVNProperty.NEEDS_LOCK) != null) {
                     SVNFileUtil.setReadonly(wcAccess.getAnchor().getFile(entry.getName()), true);
                 }
                 handleEvent(SVNEventFactory.createLockEvent(wcAccess, wcAccess
                         .getTargetName(), SVNEventAction.UNLOCKED, lock, null),
                         ISVNEventHandler.UNKNOWN);
             } catch (SVNException e) {
                 handleEvent(SVNEventFactory.createLockEvent(wcAccess, wcAccess
                         .getTargetName(), SVNEventAction.UNLOCK_FAILED, lock, e
                         .getMessage()), ISVNEventHandler.UNKNOWN);
             } finally {
                 wcAccess.close(true);
             }
         }
     }
 
     public void doUnlock(String[] urls, boolean breakLock) throws SVNException {
         Map lockTokens = new HashMap();
         if (!breakLock) {
             for (int i = 0; i < urls.length; i++) {
                 String url = validateURL(urls[i]);
                 // get lock token for url
                 SVNRepository repos = createRepository(url);
                 SVNLock lock = repos.getLock("");
                 if (lock == null) {
                     SVNErrorManager.error("svn: '" + url + "' is not locked");
                     return;
                 }
                 lockTokens.put(url, lock.getID());
             }
         }
         for (int i = 0; i < urls.length; i++) {
             String url = validateURL(urls[i]);
             // get lock token for url
             SVNRepository repos = createRepository(url);
             String id = (String) lockTokens.get(url);
             try {
                 repos.removeLock("", id, breakLock);
             } catch (SVNException e) {
                 handleEvent(SVNEventFactory.createLockEvent(url,
                         SVNEventAction.UNLOCK_FAILED, null, null),
                         ISVNEventHandler.UNKNOWN);
                 continue;
             }
             handleEvent(SVNEventFactory.createLockEvent(url,
                     SVNEventAction.UNLOCKED, null, null),
                     ISVNEventHandler.UNKNOWN);
         }
     }
 
     public void doInfo(File path, SVNRevision revision, boolean recursive,
             ISVNInfoHandler handler) throws SVNException {
         if (handler == null) {
             return;
         }
         if (!(revision == null || !revision.isValid() || revision == SVNRevision.WORKING)) {
             SVNWCAccess wcAccess = createWCAccess(path);
             SVNRevision wcRevision = null;
             String url = null;
             try {
                 wcAccess.open(false, false);
                 url = wcAccess.getTargetEntryProperty(SVNProperty.URL);
                 if (url == null) {
                     SVNErrorManager.error("svn: '" + path.getAbsolutePath()
                             + "' has no URL");
                 }
                 wcRevision = SVNRevision.parse(wcAccess
                         .getTargetEntryProperty(SVNProperty.REVISION));
             } finally {
                 wcAccess.close(false);
             }
             doInfo(url, wcRevision, revision, recursive, handler);
             return;
         }
         SVNWCAccess wcAccess = createWCAccess(path);
         try {
             wcAccess.open(false, recursive);
             collectInfo(wcAccess.getAnchor(), wcAccess.getTargetName(),
                     recursive, handler);
         } finally {
             wcAccess.close(false);
         }
     }
 
     public void doInfo(String url, SVNRevision pegRevision,
             SVNRevision revision, boolean recursive, ISVNInfoHandler handler)
             throws SVNException {
         if (revision == null || !revision.isValid()) {
             revision = SVNRevision.HEAD;
         }
         url = validateURL(url);
         url = getURL(url, pegRevision, revision);
         long revNum = getRevisionNumber(url, revision);
 
         SVNRepository repos = createRepository(url);
         SVNDirEntry rootEntry = repos.info("", revNum);
         if (rootEntry == null || rootEntry.getKind() == SVNNodeKind.NONE) {
             SVNErrorManager.error("'" + url + "' non-existent in revision "
                     + revNum);
         }
         String reposRoot = repos.getRepositoryRoot(true);
         String reposUUID = repos.getRepositoryUUID();
         // 1. get locks for this dir and below.
         SVNLock[] locks;
         try {
             locks = repos.getLocks("");
         } catch (SVNException e) {
             // may be not supported.
             locks = new SVNLock[0];
         }
         locks = locks == null ? new SVNLock[0] : locks;
         Map locksMap = new HashMap();
         for (int i = 0; i < locks.length; i++) {
             SVNLock lock = locks[i];
             locksMap.put(lock.getPath(), lock);
         }
         String fullPath = SVNRepositoryLocation.parseURL(PathUtil.decode(url))
                 .getPath();
         String rootPath = fullPath.substring(reposRoot.length());
         if (!rootPath.startsWith("/")) {
             rootPath = "/" + rootPath;
         }
         reposRoot = PathUtil.append(url.substring(0, url.length()
                 - fullPath.length()), reposRoot);
         collectInfo(repos, rootEntry, SVNRevision.create(revNum), rootPath,
                 reposRoot, reposUUID, url, locksMap, recursive, handler);
     }
 
     public String doGetWorkingCopyID(File path, String trailURL,
             boolean lastChanged) {
 
         return null;
     }
 
     public SVNInfo doInfo(File path, SVNRevision revision) throws SVNException {
         final SVNInfo[] result = new SVNInfo[1];
         doInfo(path, revision, false, new ISVNInfoHandler() {
             public void handleInfo(SVNInfo info) {
                 if (result[0] == null) {
                     result[0] = info;
                 }
             }
         });
         return result[0];
     }
 
     public SVNInfo doInfo(String url, SVNRevision pegRevision,
             SVNRevision revision) throws SVNException {
         final SVNInfo[] result = new SVNInfo[1];
         doInfo(url, pegRevision, revision, false, new ISVNInfoHandler() {
             public void handleInfo(SVNInfo info) {
                 if (result[0] == null) {
                     result[0] = info;
                 }
             }
         });
         return result[0];
     }
 
     private static void collectInfo(SVNDirectory dir, String name,
             boolean recursive, ISVNInfoHandler handler) throws SVNException {
         SVNEntries entries = dir.getEntries();
         SVNEntry entry = entries.getEntry(name, false);
         try {
             if (entry != null) {
                 if (entry.isFile()) {
                     // child file
                     File file = dir.getFile(name);
                     handler.handleInfo(SVNInfo.createInfo(file, entry));
                     return;
                 } else if (entry.isDirectory() && !"".equals(name)) {
                     // child dir
                     dir = dir.getChildDirectory(name);
                     if (dir != null) {
                         collectInfo(dir, "", recursive, handler);
                     }
                     return;
                 } else if ("".equals(name)) {
                     // report root.
                     handler
                             .handleInfo(SVNInfo
                                     .createInfo(dir.getRoot(), entry));
                 }
 
                 if (recursive) {
                     for (Iterator ents = entries.entries(true); ents.hasNext();) {
                         SVNEntry childEntry = (SVNEntry) ents.next();
                         if ("".equals(childEntry.getName())) {
                             continue;
                         }
                         if (childEntry.isDirectory()) {
                             SVNDirectory childDir = dir
                                     .getChildDirectory(childEntry.getName());
                             if (childDir != null) {
                                 collectInfo(childDir, "", recursive, handler);
                             }
                         } else if (childEntry.isFile()) {
                             handler.handleInfo(SVNInfo.createInfo(dir.getFile(childEntry.getName()), childEntry));
                         }
                     }
                 }
             }
         } finally {
             entries.close();
         }
 
     }
 
     private static void collectInfo(SVNRepository repos, SVNDirEntry entry,
             SVNRevision rev, String path, String root, String uuid, String url,
             Map locks, boolean recursive, ISVNInfoHandler handler)
             throws SVNException {
         String rootPath = repos.getLocation().getPath();
         rootPath = PathUtil.decode(rootPath);
         String displayPath = PathUtil.append(repos.getRepositoryRoot(), path)
                 .substring(rootPath.length());
         if ("".equals(displayPath) || "/".equals(displayPath)) {
             displayPath = path;
         }
         displayPath = PathUtil.removeLeadingSlash(displayPath);
         handler.handleInfo(SVNInfo.createInfo(displayPath, root, uuid, url,
                 rev, entry, (SVNLock) locks.get(path)));
         if (entry.getKind() == SVNNodeKind.DIR && recursive) {
             Collection children = repos.getDir(path, rev.getNumber(), null,
                     new ArrayList());
             for (Iterator ents = children.iterator(); ents.hasNext();) {
                 SVNDirEntry child = (SVNDirEntry) ents.next();
                 String childURL = PathUtil.append(url, PathUtil.encode(child
                         .getName()));
                 collectInfo(repos, child, rev, PathUtil.append(path, child
                         .getName()), root, uuid, childURL, locks, recursive,
                         handler);
             }
         }
     }
 
     // add, del@path, revert, resolved, *lock, *unlock, *info, +cleanup -> "wc"
     // client
 
     // copy, move -> "copy" client
     // log, cat, blame, ls -> "repos" client
     // commit, mkdir, import, del@url -> "commit" client
     // status -> "status" client
     // export, update, switch -> "update" client
     // diff, merge -> "diff" client
 
     // (?) ps,pg,pe,pl -> "prop" client
 
     private void addDirectory(SVNWCAccess wcAccess, SVNDirectory dir,
             String name, boolean force) throws SVNException {
         DebugLog.log("ading file " + name + " into " + dir.getRoot());
 
         if (dir.add(name, false, force) == null) {
             return;
         }
 
         File file = dir.getFile(name);
         SVNDirectory childDir = dir.getChildDirectory(name);
         if (childDir == null) {
             return;
         }
         File[] children = file.listFiles();
         for (int i = 0; children != null && i < children.length; i++) {
             File childFile = children[i];
             if (getOptions().isIgnored(childFile.getName())) {
                 continue;
             }
             if (".svn".equals(childFile.getName())) {
                 continue;
             }
             DebugLog.log("processing file " + childFile + " in " + file);
             SVNFileType fileType = SVNFileType.getType(childFile);
             if (fileType == SVNFileType.FILE || fileType == SVNFileType.SYMLINK) {
                 SVNEntry entry = childDir.getEntries().getEntry(
                         childFile.getName(), true);
                 DebugLog.log("existing entry: " + entry);
                 if (force && entry != null && !entry.isScheduledForDeletion()
                         && !entry.isDeleted()) {
                     DebugLog.log("this entry will not be added: "
                             + entry.getName());
                     continue;
                 }
                 addSingleFile(childDir, childFile.getName());
             } else if (SVNFileType.DIRECTORY == fileType) {
                 DebugLog.log("recursing into " + childFile.getName());
                 addDirectory(wcAccess, childDir, childFile.getName(), force);
             }
         }
     }
 
     private void addSingleFile(SVNDirectory dir, String name) throws SVNException {
         File file = dir.getFile(name);
         dir.add(name, false, false);
 
         String mimeType;
         SVNProperties properties = dir.getProperties(name, false);
         if (SVNFileUtil.isSymlink(file)) {
             properties.setPropertyValue(SVNProperty.SPECIAL, "*");
         } else {
             Map props = new HashMap();
             boolean executable;
             props = getOptions().applyAutoProperties(name, props);
             DebugLog.log("auto properties for file: " + name + " : " + props);
             mimeType = (String) props.get(SVNProperty.MIME_TYPE);
             if (mimeType == null) {
                 mimeType = SVNFileUtil.detectMimeType(file);
                 if (mimeType != null) {
                     props.put(SVNProperty.MIME_TYPE, mimeType);
                 }
             }
             if (!props.containsKey(SVNProperty.EXECUTABLE)) {
                 executable = SVNFileUtil.isExecutable(file);
                 if (executable) {
                     props.put(SVNProperty.EXECUTABLE, "*");
                 }
             }
             for (Iterator names = props.keySet().iterator(); names.hasNext();) {
                 String propName = (String) names.next();
                 String propValue = (String) props.get(propName);
                 properties.setPropertyValue(propName, propValue);
             }
         }
     }
 
     private void doGetRemoteProperty(String url, String path,
             SVNRepository repos, String propName, SVNRevision rev,
             boolean recursive, ISVNPropertyHandler handler) throws SVNException {
         long revNumber = getRevisionNumber(url, rev);
         SVNNodeKind kind = repos.checkPath(path, revNumber);
         Map props = new HashMap();
         if (kind == SVNNodeKind.DIR) {
             Collection children = repos.getDir(path, revNumber, props,
                     recursive ? new ArrayList() : null);
             if (propName != null) {
                 String value = (String) props.get(propName);
                 if (value != null) {
                     handler.handleProperty(url, new SVNPropertyData(propName,
                             value));
                 }
             } else {
                 for (Iterator names = props.keySet().iterator(); names
                         .hasNext();) {
                     String name = (String) names.next();
                     if (name.startsWith(SVNProperty.SVN_ENTRY_PREFIX)
                             || name.startsWith(SVNProperty.SVN_WC_PREFIX)) {
                         continue;
                     }
                     String value = (String) props.get(name);
                     handler.handleProperty(url,
                             new SVNPropertyData(name, value));
                 }
             }
             if (recursive) {
                 for (Iterator entries = children.iterator(); entries.hasNext();) {
                     SVNDirEntry child = (SVNDirEntry) entries.next();
                     String childURL = PathUtil.append(url, PathUtil
                             .encode(child.getName()));
                     String childPath = "".equals(path) ? child.getName()
                             : PathUtil.append(path, child.getName());
                     doGetRemoteProperty(childURL, childPath, repos, propName,
                             rev, recursive, handler);
                 }
             }
         } else if (kind == SVNNodeKind.FILE) {
             repos.getFile(path, revNumber, props, null);
             if (propName != null) {
                 String value = (String) props.get(propName);
                 if (value != null) {
                     handler.handleProperty(url, new SVNPropertyData(propName,
                             value));
                 }
             } else {
                 for (Iterator names = props.keySet().iterator(); names
                         .hasNext();) {
                     String name = (String) names.next();
                     if (name.startsWith(SVNProperty.SVN_ENTRY_PREFIX)
                             || name.startsWith(SVNProperty.SVN_WC_PREFIX)) {
                         continue;
                     }
                     String value = (String) props.get(name);
                     handler.handleProperty(url,
                             new SVNPropertyData(name, value));
                 }
             }
         }
     }
 
     private void doGetLocalProperty(SVNDirectory anchor, String name,
             String propName, SVNRevision rev, boolean recursive,
             ISVNPropertyHandler handler) throws SVNException {
         SVNEntries entries = anchor.getEntries();
         SVNEntry entry = entries.getEntry(name, true);
         if (entry == null
                 || (rev == SVNRevision.WORKING && entry
                         .isScheduledForDeletion())) {
             return;
         }
         if (!"".equals(name)) {
             if (entry.getKind() == SVNNodeKind.DIR) {
                 SVNDirectory dir = anchor.getChildDirectory(name);
                 if (dir != null) {
                     doGetLocalProperty(dir, "", propName, rev, recursive,
                             handler);
                 }
             } else if (entry.getKind() == SVNNodeKind.FILE) {
                 SVNProperties props = rev == SVNRevision.WORKING ? anchor
                         .getProperties(name, false) : anchor.getBaseProperties(
                         name, false);
                 if (propName != null) {
                     String value = props.getPropertyValue(propName);
                     if (value != null) {
                         handler.handleProperty(anchor.getFile(name), new SVNPropertyData(propName, value));
                     }
                 } else {
                     Map propsMap = props.asMap();
                     for (Iterator names = propsMap.keySet().iterator(); names
                             .hasNext();) {
                         String pName = (String) names.next();
                         String value = (String) propsMap.get(pName);
                         handler.handleProperty(anchor.getFile(name), new SVNPropertyData(pName, value));
                     }
                 }
             }
             entries.close();
             return;
         }
         SVNProperties props = rev == SVNRevision.WORKING ? anchor
                 .getProperties(name, false) : anchor.getBaseProperties(name,
                 false);
         if (propName != null) {
             String value = props.getPropertyValue(propName);
             if (value != null) {
                 handler.handleProperty(anchor.getFile(name), new SVNPropertyData(propName, value));
             }
         } else {
             Map propsMap = props.asMap();
             for (Iterator names = propsMap.keySet().iterator(); names.hasNext();) {
                 String pName = (String) names.next();
                 String value = (String) propsMap.get(pName);
                 handler.handleProperty(anchor.getFile(name), new SVNPropertyData(pName, value));
             }
         }
         if (!recursive) {
             return;
         }
         for (Iterator ents = entries.entries(true); ents.hasNext();) {
             SVNEntry childEntry = (SVNEntry) ents.next();
             if ("".equals(childEntry.getName())) {
                 continue;
             }
             doGetLocalProperty(anchor, childEntry.getName(), propName, rev,
                     recursive, handler);
         }
     }
 
     private void doSetLocalProperty(SVNDirectory anchor, String name,
             String propName, String propValue, boolean force,
             boolean recursive, ISVNPropertyHandler handler) throws SVNException {
         SVNEntries entries = anchor.getEntries();
         DebugLog.log("anchor: " + anchor.getRoot());
         if (!"".equals(name)) {
             SVNEntry entry = entries.getEntry(name, true);
             DebugLog.log("entry in anchor: " + entry);
             if (entry == null || (recursive && entry.isDeleted())) {
                 return;
             }
             if (entry.getKind() == SVNNodeKind.DIR) {
                 SVNDirectory dir = anchor.getChildDirectory(name);
                 if (dir != null) {
                     doSetLocalProperty(dir, "", propName, propValue, force,
                             recursive, handler);
                 }
             } else if (entry.getKind() == SVNNodeKind.FILE) {
                 DebugLog.log("setting file property: " + propName + "="
                         + propValue);
                 if (SVNProperty.IGNORE.equals(propName)
                         || SVNProperty.EXTERNALS.equals(propName)) {
                     if (!recursive) {
                         SVNErrorManager.error("svn: setting '" + propName
                                 + "' property is not supported for files");
                     }
                     return;
                 }
                 SVNProperties props = anchor.getProperties(name, false);
                 File wcFile = anchor.getFile(name);
                 if (SVNProperty.EXECUTABLE.equals(propName)) {
                     SVNFileUtil.setExecutable(wcFile, propValue != null);
                 }
                 if (!force && SVNProperty.EOL_STYLE.equals(propName)
                         && propValue != null) {
                     if (SVNWCUtil.isBinaryMimetype(props
                             .getPropertyValue(SVNProperty.MIME_TYPE))) {
                         if (!recursive) {
                             SVNErrorManager.error("svn: File '" + wcFile
                                     + "' has binary mime type property");
                         }
                         return;
                     }
                     if (!SVNTranslator.checkNewLines(wcFile)) {
                         SVNErrorManager.error("svn: File '" + wcFile
                                 + "' has inconsistent newlines");
                     }
                 }
                 props.setPropertyValue(propName, propValue);
 
                 if (SVNProperty.EOL_STYLE.equals(propName)
                         || SVNProperty.KEYWORDS.equals(propName)) {
                     entry.setTextTime(null);
                     entries.save(false);
                 } else if (SVNProperty.NEEDS_LOCK.equals(propName)
                         && propValue == null) {
                     SVNFileUtil.setReadonly(wcFile, false);
                 }
                 if (handler != null) {
                     handler.handleProperty(anchor.getFile(name), new SVNPropertyData(propName, propValue));
                 }
             }
             entries.close();
             return;
         }
         DebugLog.log("setting property (" + propName + ") on dir "
                 + anchor.getRoot());
         SVNProperties props = anchor.getProperties(name, false);
         if (SVNProperty.KEYWORDS.equals(propName)
                 || SVNProperty.EOL_STYLE.equals(propName)
                 || SVNProperty.MIME_TYPE.equals(propName)
                 || SVNProperty.EXECUTABLE.equals(propName)) {
             if (!recursive) {
                 SVNErrorManager.error("svn: setting '" + propName
                         + "' property is not supported for directories");
             }
         } else {
             props.setPropertyValue(propName, propValue);
             if (handler != null) {
                 handler.handleProperty(anchor.getFile(name), new SVNPropertyData(propName, propValue));
             }
         }
         if (!recursive) {
             return;
         }
         for (Iterator ents = entries.entries(true); ents.hasNext();) {
             SVNEntry entry = (SVNEntry) ents.next();
             if ("".equals(entry.getName())) {
                 continue;
             }
             doSetLocalProperty(anchor, entry.getName(), propName, propValue,
                     force, recursive, handler);
         }
     }
 
     private static String validatePropertyName(String name) throws SVNException {
         if (name == null || name.trim().length() == 0) {
             SVNErrorManager.error("svn: Bad property name: '" + name + "'");
             return name;
         }
         name = name.trim();
         if (!(Character.isLetter(name.charAt(0)) || name.charAt(0) == ':' || name
                 .charAt(0) == '_')) {
             SVNErrorManager.error("svn: Bad property name: '" + name + "'");
         }
         for (int i = 1; i < name.length(); i++) {
             if (!(Character.isLetterOrDigit(name.charAt(i))
                     || name.charAt(i) == '-' || name.charAt(i) == '.'
                     || name.charAt(i) == ':' || name.charAt(i) == '_')) {
                 SVNErrorManager.error("svn: Bad property name: '" + name + "'");
             }
         }
         return name;
     }
 
     private static String validatePropertyValue(String name, String value,
             boolean force) throws SVNException {
         DebugLog.log("validating: " + name + "=" + value);
         if (value == null) {
             return value;
         }
         if (!force && SVNProperty.EOL_STYLE.equals(name)) {
             value = value.trim();
         } else if (!force && SVNProperty.MIME_TYPE.equals(name)) {
             value = value.trim();
         } else if (SVNProperty.IGNORE.equals(name)
                 || SVNProperty.EXTERNALS.equals(name)) {
             if (!value.endsWith("\n")) {
                 value += "\n";
             }
             if (SVNProperty.EXTERNALS.equals(name)) {
                 DebugLog.log("validating externals: " + value);
                 SVNExternalInfo[] externalInfos = SVNWCAccess.parseExternals(
                         "", value);
                 if (externalInfos != null) {
                     DebugLog.log("validating externals: "
                             + externalInfos.length);
                 }
                 for (int i = 0; externalInfos != null
                         && i < externalInfos.length; i++) {
                     String path = externalInfos[i].getPath();
                     DebugLog.log("checking path: " + path);
                     if (path.indexOf(".") >= 0 || path.indexOf("..") >= 0
                             || path.startsWith("/")) {
                         DebugLog.log("throwing exception");
                         SVNErrorManager
                                 .error("svn: Invalid external definition: "
                                         + value);
                     }
 
                 }
             }
         } else if (SVNProperty.KEYWORDS.equals(name)) {
             value = value.trim();
         } else if (SVNProperty.EXECUTABLE.equals(name)
                 || SVNProperty.SPECIAL.equals(name)
                 || SVNProperty.NEEDS_LOCK.equals(name)) {
             value = "*";
         }
         return value;
     }
 
     private static final Collection REVISION_PROPS = new HashSet();
     static {
         REVISION_PROPS.add("svn:author");
         REVISION_PROPS.add("svn:log");
         REVISION_PROPS.add("svn:date");
         REVISION_PROPS.add("svn:autoversioned");
         REVISION_PROPS.add("svn:original-date");
     }
 
     private static class LockInfo {
 
         public LockInfo(File file, SVNRevision rev) {
             myFile = file;
             myRevision = rev;
         }
 
         public LockInfo(File file, String token) {
             myFile = file;
             myToken = token;
         }
 
         private File myFile;
 
         private SVNRevision myRevision;
 
         private String myToken;
     }
 }
