 package jp.ac.osaka_u.ist.sdl.mpanalyzer;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.Modification;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.Revision;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.Statement;
 
 import org.tmatesoft.svn.core.SVNDepth;
 import org.tmatesoft.svn.core.SVNURL;
 import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
 import org.tmatesoft.svn.core.wc.ISVNDiffStatusHandler;
 import org.tmatesoft.svn.core.wc.SVNClientManager;
 import org.tmatesoft.svn.core.wc.SVNDiffClient;
 import org.tmatesoft.svn.core.wc.SVNDiffStatus;
 import org.tmatesoft.svn.core.wc.SVNRevision;
 import org.tmatesoft.svn.core.wc.SVNStatusType;
 import org.tmatesoft.svn.core.wc.SVNWCClient;
 
 public class ModificationExtractionThread extends Thread {
 
 	static final private String PATH_TO_REPOSITORY = Config
 			.getPATH_TO_REPOSITORY();
 	static final private String TARGET = Config.getTARGET();
 	static final private String LANGUAGE = Config.getLanguage();
 	static final private boolean ONLYCONDITION = Config.isOnlyCondition();
 	static final private int LARGEMODIFICATION = Config.getLargeModification();
 
 	final public int id;
 	final public Revision[] revisions;
 	final private AtomicInteger index;
 	final private BlockingQueue<Modification> queue;
 
 	public ModificationExtractionThread(final int id,
 			final Revision[] revisions, final AtomicInteger index,
 			BlockingQueue<Modification> queue) {
 		this.id = id;
 		this.revisions = revisions;
 		this.index = index;
 		this.queue = queue;
 	}
 
 	@Override
 	public void run() {
 
 		try {
 
 			final SVNURL url = SVNURL.fromFile(new File(PATH_TO_REPOSITORY));
 			FSRepositoryFactory.setup();
 			final SVNWCClient wcClient = SVNClientManager.newInstance()
 					.getWCClient();
 			final SVNDiffClient diffClient = SVNClientManager.newInstance()
 					.getDiffClient();
 
 			while (true) {
 
 				final int targetIndex = this.index.getAndIncrement();
 				if (this.revisions.length <= targetIndex) {
 					break;
 				}
 
				final Revision beforeRevision = 0 == targetIndex ? new Revision(
						0, "", "") : this.revisions[targetIndex - 1];
 				final Revision afterRevision = this.revisions[targetIndex];
 
 				final StringBuilder progress = new StringBuilder();
 				progress.append(this.id);
 				progress.append(": checking revision ");
 				progress.append(beforeRevision.number);
 				progress.append(" and ");
 				progress.append(afterRevision.number);
 				System.out.println(progress.toString());
 
 				final List<String> modifiedFileList = new ArrayList<String>();
 
 				// 変更されたソースファイルのリストを得る
 				diffClient.doDiffStatus(url,
 						SVNRevision.create(beforeRevision.number), url,
 						SVNRevision.create(afterRevision.number),
 						SVNDepth.INFINITY, true, new ISVNDiffStatusHandler() {
 
 							@Override
 							public void handleDiffStatus(
 									final SVNDiffStatus diffStatus) {
 								final String path = diffStatus.getPath();
 								final SVNStatusType type = diffStatus
 										.getModificationType();
 								if (LANGUAGE.equalsIgnoreCase("JAVA")
 										&& path.startsWith(TARGET)
 										&& StringUtility.isJavaFile(path)
 										&& type.equals(SVNStatusType.STATUS_MODIFIED)) {
 									modifiedFileList.add(path);
 									System.out.println(path);
 								} else if (LANGUAGE.equalsIgnoreCase("C")
 										&& path.startsWith(TARGET)
 										&& StringUtility.isCFile(path)
 										&& type.equals(SVNStatusType.STATUS_MODIFIED)) {
 									modifiedFileList.add(path);
 									System.out.println(path);
 								}
 							}
 						});
 
 				for (final String path : modifiedFileList) {
 					final SVNURL fileurl = SVNURL.fromFile(new File(
 							PATH_TO_REPOSITORY
 									+ System.getProperty("file.separator")
 									+ path));
 
 					// 変更前ファイルの中身を取得
 					final StringBuilder beforeText = new StringBuilder();
 					wcClient.doGetFileContents(fileurl,
 							SVNRevision.create(beforeRevision.number),
 							SVNRevision.create(beforeRevision.number), false,
 							new OutputStream() {
 								@Override
 								public void write(int b) throws IOException {
 									beforeText.append((char) b);
 								}
 							});
 
 					// 変更後ファイルの中身を取得
 					final StringBuilder afterText = new StringBuilder();
 					wcClient.doGetFileContents(fileurl,
 							SVNRevision.create(afterRevision.number),
 							SVNRevision.create(afterRevision.number), false,
 							new OutputStream() {
 								@Override
 								public void write(int b) throws IOException {
 									afterText.append((char) b);
 								}
 							});
 
 					final List<Statement> beforeStatements = StringUtility
 							.splitToStatements(beforeText.toString(), LANGUAGE);
 					final List<Statement> afterStatements = StringUtility
 							.splitToStatements(afterText.toString(), LANGUAGE);
 
 					final List<Modification> modifications = LCS
 							.getModifications(beforeStatements,
 									afterStatements, path, afterRevision);
 
 					if (ONLYCONDITION) {
 						for (final Modification m : modifications) {
 							if (m.isCondition()) {
 								this.queue.add(m);
 							}
 						}
 					} else {
 						this.queue.addAll(modifications);
 					}
 				}
 			}
 
 		} catch (final Exception e) {
 			e.printStackTrace();
 		}
 	}
 }
