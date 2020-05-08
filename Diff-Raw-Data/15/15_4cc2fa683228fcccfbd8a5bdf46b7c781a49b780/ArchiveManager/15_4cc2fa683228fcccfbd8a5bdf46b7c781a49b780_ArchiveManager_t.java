 /* Class to manage reading from and writing Freenet WORM Archives.
  *
  * Copyright (C) 2010, 2011 Darrell Karbott
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public
  * License as published by the Free Software Foundation; either
  * version 2.0 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * General Public License for more details.
  *
  * You should have received a copy of the GNU General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  *
  * Author: djk@isFiaD04zgAgnrEC5XJt1i4IE7AkNPqhBG5bONi6Yks
  *
  *  This file was developed as component of
  * "fniki" (a wiki implementation running over Freenet).
  */
 
 package fniki.wiki;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.PrintStream;
 
 import java.util.regex.Pattern;
 import java.util.regex.Matcher;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 
 import fmsutil.FMSUtil;
 import wormarc.Archive;
 import wormarc.AuditArchive;
 import wormarc.ExternalRefs;
 import wormarc.FileManifest;
 import wormarc.IOUtil;
 import wormarc.LinkDigest;
 import wormarc.RootObjectKind;
 import wormarc.io.FreenetIO;
 import wormarc.io.ByteArrayIO;
 
 public class ArchiveManager {
     public final static String FCP_HOST = "127.0.0.1";
     public final static int FCP_PORT = 9481;
 
     public final static String FMS_HOST = "127.0.0.1";
     public final static int FMS_PORT = 1119;
     public final static String FMS_GROUP = "biss.test000";
     public final static String BISS_NAME = "testwiki";
     // Maximum number of articles to read from FMS.
     private final static int MAX_ARTICLES = 200;
 
     String mFcpHost = FCP_HOST;
     int mFcpPort = FCP_PORT;
     String mFmsHost = FMS_HOST;
     int mFmsPort = FMS_PORT;
     String mFmsGroup = FMS_GROUP;
     String mBissName= BISS_NAME;
 
     String mPrivateSSK;
     String mFmsId;
 
     // Base64 SSK public key hash to FMS name. i.e. the part before '@'.
     Map<String, String> mNymLut = new HashMap<String, String>();
 
     // LATER: Revisit. This was HACK to work around the fact that GetCHKOnly=true
     //        is broken for splitfiles.
     //
     // Block hex digest to CHK key map.
     Map<String, String> mSha1ToChk = new HashMap<String, String>();
 
     // Name to theme map for freesite insertion.
     Map<String, SiteTheme> mThemeMap = new HashMap<String, SiteTheme>();
     String mCurrentThemeName = "default";
 
     // Main version
     String mParentUri;
     Archive mArchive;
     FileManifest mFileManifest;
     LocalWikiChanges mOverlay;
 
     // Read only secondary version to rebase into main version.
     String mSecondaryUri;
     Archive mSecondaryArchive;
     FileManifest mSecondaryFileManifest;
     WikiTextChanges mSecondaryChanges;
 
     public ArchiveManager() {
         mThemeMap.put("default", buildDefaultSiteTheme());
     }
 
     public void setDebugOutput(PrintStream out) {
         FreenetIO.setDebugOutput(out);
     }
 
     private static void validatePrivateSSK(String value) {
         if (!value.startsWith("SSK@") || !value.endsWith(",AQECAAE/")) {
             throw new IllegalArgumentException("That doesn't look like a private SSK. " +
                                            "Did you forget the trailing '/'?");
         }
     }
 
     public void setPrivateSSK(String value) {
         validatePrivateSSK(value);
         mPrivateSSK = value;
     }
 
     public String invertPrivateSSK(String value, int timeoutMs) throws IOException {
         validatePrivateSSK(value);
         return makeIO().invertPrivateSSK(value, timeoutMs);
     }
 
     public String getPrivateSSK() { return mPrivateSSK; }
     public String getParentUri() { return mParentUri; }
 
     public void setFmsId(String value) {
         if (value.indexOf("@") != -1 && value.indexOf(".freetalk") == -1) {
             throw new IllegalArgumentException("FMS Id Should only include the part before the '@'!");
         }
 
         mFmsId = value;
     }
 
     public String getFmsId() { return mFmsId; }
 
     public void setFcpHost(String value) {mFcpHost = value; }
     public String getFcpHost() { return mFcpHost; }
 
     public void setFcpPort(int value) {mFcpPort = value; }
     public int getFcpPort() { return mFcpPort; }
 
     public void setFmsHost(String value) { mFmsHost = value; }
     public String getFmsHost() { return mFmsHost; }
 
     public void setFmsPort(int value) { mFmsPort = value; }
     public int getFmsPort() { return mFmsPort; }
 
     public void setFmsGroup(String value) { mFmsGroup = value; }
     public String getFmsGroup() { return mFmsGroup; }
 
     public void setBissName(String value) { mBissName= value; }
     public String getBissName() { return mBissName; }
 
     public String getSecondaryUri() { return mSecondaryUri; }
 
     public void load(String uri, boolean isSecondary) throws IOException {
         if (isSecondary && mFileManifest == null) {
             throw new IOException("Can't load secondary archive because no primary archive is loaded yet!");
         }
         FreenetIO io = makeIO();
         io.setRequestUri(uri);
         Archive archive = Archive.load(io);
         validateUriHashes(archive, uri, true);
         FileManifest manifest = FileManifest.fromArchiveRootObject(archive);
 
         if (isSecondary) {
             WikiTextChanges remoteChanges = new RemoteWikiTextChanges(mFileManifest, archive, manifest);
             // Survived possible exceptions.
             mSecondaryArchive = archive;
             mSecondaryFileManifest = manifest;
             mSecondaryChanges = remoteChanges;
             mSecondaryUri = uri;
             return;
         }
 
         LocalWikiChanges localChanges = new LocalWikiChanges(archive, manifest);
         // Survived possible exceptions.
         mArchive = archive;
         mFileManifest = manifest;
         mOverlay = localChanges;
         mParentUri = uri;
 
         // Loading primary resets secondary.
         mSecondaryArchive = null;
         mSecondaryFileManifest = null;
         mSecondaryChanges = null;
         mSecondaryUri = null;
     }
     public void load(String uri) throws IOException { load(uri, false); }
 
     public void createEmptyArchive() throws IOException {
         mArchive = new Archive();
         mFileManifest = FileManifest.fromArchiveRootObject(mArchive);
         mOverlay = new LocalWikiChanges(mArchive, mFileManifest); // DCI: why copy ?
         mParentUri = null;
     }
 
     public static class UpToDateException extends IOException {
         public UpToDateException() {
             super("There are no local changes to submit.");
         }
     }
 
     private FreenetIO makeIO() {
         return new FreenetIO(mFcpHost, mFcpPort, null, mSha1ToChk);
     }
 
     private static String getExternalRefDigest(Archive archive, int kind) throws IOException {
         String value = "";
         LinkDigest refsDigest = archive.getRootObject(kind);
         if (!refsDigest.isNullDigest()) {
             ExternalRefs refs = ExternalRefs.fromBytes(archive.getFile(refsDigest));
             for (ExternalRefs.Reference ref : refs.mRefs) {
                 if (ref.mKind != ExternalRefs.KIND_FREENET) {
                     continue;
                 }
                 value += "_";
                 value += IOUtil.getFileDigest(IOUtil.toStreamAsUtf8(ref.mExternalKey))
                     .hexDigest(8);
             }
         }
         return value;
     }
 
     // The name of a jfniki archive includes the hash of the
     // full archive manifest file, and hashes of the SSK of
     // it's parent(s) including an optional rebase parent.
     //
     // use '_' instead of '|' because '|' gets percent escaped.
     // <sha1_of_am_file>[[<underbar>sha1_of_parent_ssk] [<underbar>sha1_of_rebase_ssk]]
     private static String makeUriNamePart(Archive archive) throws IOException {
         // Generate a unique SSK.
         LinkDigest digest = archive.getRootObject(RootObjectKind.ARCHIVE_MANIFEST);
         // The hash of the actual file, not just the chain head SHA.
         LinkDigest fileHash = IOUtil.getFileDigest(archive.getFile(digest));
 
         return
             fileHash.hexDigest(8) +
             getExternalRefDigest(archive, RootObjectKind.PARENT_REFERENCES) +
             getExternalRefDigest(archive, RootObjectKind.REBASE_REFERENCES);
     }
 
     private static void validateUriHashes(Archive archive,
                                           String uri,
                                           boolean allowNoParents) throws IOException {
         String[] fields = uri.split("/");
         if (fields.length != 2) {
             throw new IOException("Couldn't parse uri: " + uri);
         }
         fields = fields[1].split("_");
         if (fields.length < 1) {
             throw new IOException("Couldn't parse uri: " + uri);
         }
 
         String[] expected  = makeUriNamePart(archive).split("_");
 
         for (int index = 0; index < expected.length; index++) {
             if (index >= fields.length) {
                 continue;
             }
         }
 
         if (fields.length == 1 && fields[0].equals(expected[0])) {
             // LATER: tighten up.
             // For now, allow old URIs that don't have parent info hashes.
             return;
         }
 
         if (expected.length != fields.length) {
             throw new IOException("Hash validation failed(0)! Inserter is lying about contents.");
         }
 
         for (int index = 0; index < expected.length; index++) {
             if (!expected[index].equals(fields[index])) {
                 throw new IOException("Hash validation failed(1)! Inserter is lying about contents.");
             }
         }
     }
 
     private String getInsertUri(Archive archive) throws IOException {
         String uri = mPrivateSSK + makeUriNamePart(archive);
         validateUriHashes(archive, uri, false);
         return uri;
     }
 
 
     public String reinsertToFreenet(PrintStream out) throws IOException {
         FileManifest.Changes changes = mFileManifest.diffTo(mArchive, mOverlay);
         if (!changes.isUnmodified()) {
             throw new IOException("There are local changes. Can't re-insert.");
         }
 
         Archive copy = mArchive.deepCopy();
 
         // Generate a unique SSK based on the SHA hash of the archive manifest.
         String insertUri = getInsertUri(copy);
 
        out.println("Checking to see if we have the private key...");
        String publicKey = invertPrivateSSK(mPrivateSSK, 30 * 1000);
        if (mParentUri.startsWith(publicKey)) {
            out.println("We do :-)");
        } else {
            out.println("We don't.  All blocks will still be re-inserted ");
            out.println("but we can't re-insert the top key.");
            insertUri = "CHK@/"  + insertUri.split("/")[1];
            out.println("Re-insert URI: " + insertUri);
        }
        //out.println("Re-insert URI: " + insertUri);
 
         FreenetIO io = makeIO();
         io.setInsertUri(insertUri);
         io.setIgnoreChkCache(true); // Force re-insert of all chks.
         out.println("Writing to Freenet...");
         copy.write(io);
 
         return io.getRequestUri();
     }
 
     // DCI: commitAndPushToFreenet() ?
     public String pushToFreenet(PrintStream out) throws IOException {
         FileManifest.Changes changes = mFileManifest.diffTo(mArchive, mOverlay);
         if (changes.isUnmodified()) {
             throw new IOException("Didn't find any local changes to submit.");
         }
 
         // Copy so that we can cleanly role back if an exception is raised.
         Archive copy = mArchive.deepCopy();
         FileManifest files = FileManifest.fromArchiveRootObject(copy);
 
         // Commit local changes to the Archive.
         copy.unsetRootObject(RootObjectKind.PARENT_REFERENCES);
         copy.unsetRootObject(RootObjectKind.REBASE_REFERENCES);
 
         // Update the archive
         copy.startUpdate();
         files.updateFrom(copy, mOverlay);
 
         LinkDigest digest = copy.updateRootObject(files.toBytes(), RootObjectKind.FILE_MANIFEST);
 
         if (mParentUri != null) {
             out.println("Set PARENT_REFERENCES: " + mParentUri);
             List<String> keys = Arrays.asList(mParentUri);
             LinkDigest refs =
                 copy.updateRootObject(ExternalRefs.create(keys, ExternalRefs.KIND_FREENET)
                                       .toBytes(),
                                       RootObjectKind.PARENT_REFERENCES);
         }
 
         if (mSecondaryUri != null) {
             out.println("Set REBASE_REFERENCES: " + mSecondaryUri);
             List<String> keys = Arrays.asList(mSecondaryUri);
             LinkDigest refs =
                 copy.updateRootObject(ExternalRefs.create(keys, ExternalRefs.KIND_FREENET)
                                       .toBytes(),
                                       RootObjectKind.REBASE_REFERENCES);
         }
 
         copy.commitUpdate();
         copy.compressAndUpdateArchiveManifest();
 
         // Generate a unique SSK based on the SHA hash of the archive manifest.
         String insertUri = getInsertUri(copy);
 
        //out.println("Insert URI: " + insertUri);
 
         // Push the updated version into Freenet.
         FreenetIO io = makeIO();
         io.setInsertUri(insertUri);
         out.println("Writing to Freenet...");
         copy.write(io);
 
         if (mFmsId != null) {
             try {
                 out.println("Sending FMS update notification to: " + mFmsGroup);
                 FMSUtil.sendBISSMsg(mFmsHost, mFmsPort, mFmsId, mFmsGroup,
                                     mBissName, io.getRequestUri());
             } catch (IOException ioe) {
                 out.println("FMS send failed: " + ioe.getMessage());
             }
         }
 
         // Don't update any state until all calls which could raise have finished.
         mArchive = copy;
         mFileManifest = FileManifest.fromArchiveRootObject(mArchive);
         mOverlay = new LocalWikiChanges(mArchive, mFileManifest);
         mParentUri = io.getRequestUri();
         out.println("Request URI: " + mParentUri);
 
         return mParentUri;
     }
 
     // Send an announcement for the version, even though we didn't insert it.
     // There is code in LoadingVersionList.getRevisionGraphHtml() which
     // renders differently depending on whether or not the public key hash
     // of the fms/freetalk id matches the public key hash of the version SSK.
     public void stakeCurrentVersion(PrintStream out) throws IOException {
         if (mFmsId == null) {
             throw new IOException("Can't send NNTP message because FMS id is not set.");
         }
 
         if (mParentUri == null) {
             throw new IOException("There is not wiki version loaded!");
         }
 
         FileManifest.Changes changes = getLocalChanges();
         if (!changes.isUnmodified()) {
             throw new IOException("Refused to send message because there are local changes.");
         }
 
         out.println("Sending 'Like' notification to... ");
         out.println("group: " + mFmsGroup);
         out.println("wiki : " + mBissName);
 
         FMSUtil.sendBISSMsg(mFmsHost, mFmsPort, mFmsId, mFmsGroup,
                             mBissName, mParentUri);
 
         out.println("Finished.");
     }
 
     public FileManifest.Changes getLocalChanges() throws IOException {
         return mFileManifest.diffTo(mArchive, mOverlay);
     }
 
     public List<RebaseStatus.Record> getRebaseStatus() throws IOException {
         if (mSecondaryFileManifest == null) {
             System.err.println("No rebased changes!");
             return new ArrayList<RebaseStatus.Record>();
         }
 
         return RebaseStatus.getStatus(mOverlay.getFiles(),
                                       mFileManifest.getMap(),
                                       mSecondaryFileManifest.getMap());
     }
 
     public void readChangeLog(PrintStream out,
                               AuditArchive.ChangeLogCallback callback) throws IOException {
 
         if (mParentUri == null) {
             throw new IOException("URI not set!");
         }
 
         ExternalRefs.Reference head =
             new ExternalRefs.Reference(ExternalRefs.KIND_FREENET, mParentUri);
 
         FreenetIO freenetResolver = makeIO();
         Archive archive = freenetResolver.resolve(head);
         AuditArchive.getManifestChangeLog(head, archive, freenetResolver, callback);
     }
 
     public List<FMSUtil.BISSRecord> getRecentWikiVersions(PrintStream out) throws IOException {
         out.println("Reading version announcements via NNTP...");
 
         List<FMSUtil.BISSRecord> records =
             FMSUtil.getBISSRecords(mFmsHost, mFmsPort, mFmsId, mFmsGroup, mBissName, MAX_ARTICLES);
 
         out.println("Finished reading. Processing...");
         // LATER: do better.
         for (FMSUtil.BISSRecord record : records) {
             String fields[] = record.mFmsId.split("@");
             if (fields.length != 2) {
                 continue;
             }
 
             if (fields[1].endsWith(".freetalk")) {
                 // Scrub .freetalk suffix. i.e. we want only the raw public hash part
                 // so we can lookup from public SSK's.
                 fields[1] = fields[1].substring(0, fields[1].lastIndexOf(".freetalk"));
             }
 
             mNymLut.put(fields[1].trim(), fields[0].trim());
         }
 
         out.println("Finished processing.");
         return records;
     }
 
     public WikiTextStorage getStorage() throws IOException {
         if (mOverlay == null) {
             throw new IllegalStateException("No archive loaded!");
         }
         return mOverlay;
     }
 
     public WikiTextChanges getRemoteChanges() throws IOException {
         if (mSecondaryChanges == null) {
             return RemoteWikiTextChanges.NO_REMOTE_CHANGES;
         }
         return mSecondaryChanges;
     }
 
     public String getNym(String sskRequestUri, boolean showPublicKey) {
         int start = sskRequestUri.indexOf("@");
         int end = sskRequestUri.indexOf(",");
         if (start == -1 || end == -1 || start >= end) {
             return "???";
         }
 
         String publicKeyHash = sskRequestUri.substring(start + 1, end - start + 3);
 
         // SSK@THIS_PART,
         String nym = mNymLut.get(publicKeyHash);
         if (nym == null) {
             nym = "???";
         }
         if (!showPublicKey) {
             return nym;
         }
         return nym + "@" + publicKeyHash;
     }
 
 
     ////////////////////////////////////////////////////////////
     // Support for updating out of date USKs
     ////////////////////////////////////////////////////////////
 
     // LATER: Use a StringBuffer / matcher to do all replacements
     //        in a single pass.
     //        This is a quick and dirty slash attack on the problem.
     private void updateUsksOnPage(Map<String, Integer> latest, String pageName,
                                   PrintStream out)
         throws IOException {
         String page = mOverlay.getPage(pageName);
         String updated = page;
         Matcher matcher = USK_REGEX.matcher(page);
         out.println("[" + pageName + "]");
         while (matcher.find()) {
             int currentIndex = Integer.parseInt(matcher.group(3));
             int latestIndex = latest.get(matcher.group(2));
 
             if (currentIndex == latestIndex) {
                 continue;
             }
 
             String target = String.format("%s%d", matcher.group(2),
                                           currentIndex);
             String replacement = String.format("%s%d", matcher.group(2),
                                                latestIndex);
             out.println("t: " + target);
             out.println("r: " + replacement);
 
             updated = updated.replace(target, replacement);
         }
         if (updated.equals(page)) {
             return;
         }
         mOverlay.putPage(pageName, updated);
     }
 
     // NOTE: added '.' to keep from hitting the "..." USKs on the FWS USK page.
     private final static Pattern USK_REGEX = Pattern.compile("((USK@[^/.]+/[^/]+/)(\\d+))");
     public void updateUsks(PrintStream out) throws IOException, InterruptedException {
         out.println("----------------------------------------");
         out.println("Finding USKs...");
         out.println("----------------------------------------");
 
         Map<String, Integer> latest = new HashMap<String, Integer>();
         for (String pageName : mOverlay.getNames()) {
             Matcher matcher = USK_REGEX.matcher(mOverlay.getPage(pageName));
             int count = 0;
             while (matcher.find()) {
                 if (count == 0) {
                     out.println("[" + pageName +"]");
                 }
                 out.println(matcher.group());
                 int index = Integer.parseInt(matcher.group(3));
                 if (latest.get(matcher.group(2)) == null ||
                     latest.get(matcher.group(2)) < index) {
                     latest.put(matcher.group(2), index);
                 }
                 count++;
             }
         }
 
         out.println("----------------------------------------");
         out.println("Looking up latest versions...");
         out.println("----------------------------------------");
 
         FcpTools runner = new  FcpTools(mFcpHost, mFcpPort, "jfniki");
 
         FcpTools.setDebugOutput(out);
         for (Map.Entry<String, Integer> entry : latest.entrySet()) {
             String usk = String.format("%s%d/", entry.getKey(), entry.getValue());
             FcpTools.CheckUsk cmd = runner.sendCheckUsk(usk);
             runner.waitUntilAllFinished();
 
             if (!cmd.getUri().equals(usk)) {
                 String[] fields = cmd.getUri().split("/");
                 latest.put(entry.getKey(),
                            Integer.parseInt(fields[fields.length -1]));
             }
         }
 
         out.println("----------------------------------------");
         out.println("Fixing pages...");
         out.println("----------------------------------------");
         for (String pageName : mOverlay.getNames()) {
             updateUsksOnPage(latest, pageName, out);
         }
     }
 
     ////////////////////////////////////////////////////////////
     // Support for reading and writing archives from blobs
     ////////////////////////////////////////////////////////////
 
     public String[] loadArchiveFromBlob(byte[] blob, boolean isSecondary) throws IOException {
         if (isSecondary && mFileManifest == null) {
             throw new IOException("Can't load secondary archive because no primary archive is loaded yet!");
         }
 
         ByteArrayIO io = new ByteArrayIO();
         io.setData(blob);
         Archive archive;
         try {
             archive = Archive.load(io);
         } finally {
             // Releases blob memory, but not metadata.
             io.release();
         }
 
         // uri|nttp_group|wiki_name
         String[] metaData = io.getMetaData().split("\\|");
         if (metaData.length != 3) {
             throw new IOException("Error parsing metadata");
         }
         for (int i = 0; i < metaData.length; i++) {
             if (metaData[i].equals("null")) {
                 metaData[i] = "";
                 continue;
             }
             metaData[i] = metaData[i].trim();
         }
 
         validateUriHashes(archive, metaData[0], true);
 
         FileManifest manifest = FileManifest.fromArchiveRootObject(archive);
 
         if (isSecondary) {
             WikiTextChanges remoteChanges = new RemoteWikiTextChanges(mFileManifest, archive, manifest);
             // Survived possible exceptions.
             mSecondaryArchive = archive;
             mSecondaryFileManifest = manifest;
             mSecondaryChanges = remoteChanges;
             mSecondaryUri = metaData[0];
             return metaData;
         }
 
         LocalWikiChanges localChanges = new LocalWikiChanges(archive, manifest);
         // Survived possible exceptions.
         mArchive = archive;
         mFileManifest = manifest;
         mOverlay = localChanges;
         mParentUri = metaData[0];
 
         // Loading primary resets secondary.
         mSecondaryArchive = null;
         mSecondaryFileManifest = null;
         mSecondaryChanges = null;
         mSecondaryUri = null;
 
         return metaData;
     }
 
     public String makeBlobFileName() throws IOException {
         return mBissName + "_" + makeUriNamePart(mArchive) + ".dat";
     }
 
     public byte[] savePrimaryArchiveToBlob() throws IOException {
         FileManifest.Changes changes = mFileManifest.diffTo(mArchive, mOverlay);
         if (!changes.isUnmodified()) {
             throw new IOException("There are unsubmitted local changes!");
         }
         ByteArrayIO io = new ByteArrayIO();
         try {
             // null values are allowed and are converted to "null"
             io.setMetaData(String.format("%s|%s|%s", mParentUri, mFmsGroup, mBissName));
             mArchive.write(io);
             return io.getData();
         } finally {
             io.release();
         }
     }
 
     ////////////////////////////////////////////////////////////
     // Support for Freesite insertion.
     ////////////////////////////////////////////////////////////
     public String insertSite(String insertUri, PrintStream out) throws IOException, InterruptedException {
         out.println("Using site theme: " + mCurrentThemeName);
 
         SiteTheme cfg = mThemeMap.get(mCurrentThemeName);
         if (cfg == null) {
             throw new RuntimeException("Failed to load theme???");
         }
 
         FileManifest.Changes changes = getLocalChanges();
         if (!changes.isUnmodified()) {
             throw new IOException("Can't insert because the wiki has unsubmitted local changes.");
         }
 
         Iterable<FileInfo> dataSource = new WikiHtmlExporter(this, cfg.mTemplate, cfg.mStaticFiles).export();
 
         FcpTools runner = new  FcpTools(mFcpHost, mFcpPort, "jfniki");
         FcpTools.setDebugOutput(out);
 
         FcpTools.InsertFreesite cmd = runner.sendInsertFreesite(insertUri,
                                                                         dataSource,
                                                                         cfg.mDefaultPage);
         runner.waitUntilAllFinished();
         cmd.raiseOnFailure();
 
         return cmd.getUri();
     }
 
     private static SiteTheme buildDefaultSiteTheme() {
         try {
             List<FileInfo> staticFiles = new ArrayList<FileInfo>();
             byte[] png = IOUtil.readAndClose(ArchiveManager.class.getResourceAsStream("/jfniki_activelink.png"));
             staticFiles.add(FcpTools.makeFileInfo("activelink.png", png, "image/png"));
 
             return new SiteTheme(IOUtil.readUtf8StringAndClose(ArchiveManager.class.
                                                                getResourceAsStream("/wiki_dump_template.html")),
                                  "Front_Page.html",
                                  staticFiles);
         } catch (IOException ioe) {
             // Should never happen.
             throw new RuntimeException("Couldn't build the default theme???", ioe);
         }
     }
 
     public void loadSiteTheme(String themeFileName, byte[] zipFileBytes) throws IOException {
         System.err.println("NAME: " + themeFileName + " len: " + zipFileBytes.length);
         String[] fields = themeFileName.split("\\.");
         for (String field : fields) {
             System.err.println("FIELD: " + field);
         }
         if (fields.length < 2 || (!fields[fields.length - 1].toLowerCase().equals("zip"))) {
             throw new IOException("Expected a zip file: " + themeFileName);
         }
 
         String name = fields[0].toLowerCase().trim();
         if (name.equals("default")) {
             throw new IOException("You can't replace the default theme: " + themeFileName);
         }
         if (!Validations.isValidThemeName(name)) {
             throw new IOException("Illegal theme name: " + name);
         }
 
         SiteTheme theme = SiteTheme.fromZipStream(new ByteArrayInputStream(zipFileBytes));
         mThemeMap.put(name, theme);
     }
 
     public List<String> getSiteThemes() {
         List<String> names = new ArrayList<String>();
         names.addAll(mThemeMap.keySet());
         Collections.sort(names);
         return names;
     }
 
     public void setSiteTheme(String name) {
         if (mThemeMap.get(name) == null) {
             throw new RuntimeException("Theme doesn't exist: " + name);
         }
         mCurrentThemeName = name;
     }
 
     ////////////////////////////////////////////////////////////
     // NOT CRYPTO GRADE! but better than nothing.
     public static String generateRandomHexString() {
         return IOUtil.getHexDigest("" + Math.random() + "" + System.currentTimeMillis(), 20);
     }
 }
