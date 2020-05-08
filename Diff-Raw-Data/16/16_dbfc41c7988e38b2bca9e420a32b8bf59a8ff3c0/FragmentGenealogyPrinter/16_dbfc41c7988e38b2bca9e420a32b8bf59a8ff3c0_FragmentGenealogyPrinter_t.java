 package jp.ac.osaka_u.ist.sdl.ectec.information_retriever.fragment_genealogy;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.PrintWriter;
 import java.util.Map;
 import java.util.SortedMap;
 import java.util.TreeMap;
 
 import jp.ac.osaka_u.ist.sdl.ectec.analyzer.vcs.svn.SVNRepositoryManager;
 import jp.ac.osaka_u.ist.sdl.ectec.data.CRD;
 import jp.ac.osaka_u.ist.sdl.ectec.data.CodeFragmentGenealogyInfo;
 import jp.ac.osaka_u.ist.sdl.ectec.data.CodeFragmentInfo;
 import jp.ac.osaka_u.ist.sdl.ectec.data.FileInfo;
 import jp.ac.osaka_u.ist.sdl.ectec.data.RevisionInfo;
 import jp.ac.osaka_u.ist.sdl.ectec.db.DBConnectionManager;
 
 public class FragmentGenealogyPrinter {
 
	private static final String LINE_SEPARATOR = "\n";
 
 	public static void main(final String[] args) {
 		DBConnectionManager dbManager = null;
 		PrintWriter pw = null;
 		SVNRepositoryManager repoManager = null;
 
 		try {
 			final String dbPath = args[0];
 			pw = new PrintWriter(new BufferedWriter(new FileWriter(new File(
 					args[1]))));
 			final String repositoryPath = args[2];
 			final String workingDir = args[3];
 
			final int threshold = Integer.parseInt(args[4]);

 			repoManager = new SVNRepositoryManager(repositoryPath, null, null,
 					null);
 
 			pw.println("ID,B_Rev,B_File,B_Method,B_Start,B_End,A_Rev,A_File,A_Method,A_Start,A_End");
 
 			dbManager = new DBConnectionManager(dbPath, 10000);
 
 			final Map<Long, CodeFragmentGenealogyInfo> fragmentGenealogies = dbManager
 					.getFragmentGenealogyRetriever().retrieveAll();
 
 			int index = 1;
 
 			for (final Map.Entry<Long, CodeFragmentGenealogyInfo> entry : fragmentGenealogies
 					.entrySet()) {
 				final CodeFragmentGenealogyInfo genealogy = entry.getValue();
 
 				if (genealogy.getChangedCount() == 0) {
 					continue;
 				}
 
 				final long startRevisionId = genealogy.getStartRevisionId();
 				final long endRevisionId = genealogy.getEndRevisionId();
 
 				final Map<Long, RevisionInfo> revisions = dbManager
 						.getRevisionRetriever().retrieveWithIds(
 
 						startRevisionId, endRevisionId);
 				final Map<Long, CodeFragmentInfo> fragments = dbManager
 						.getFragmentRetriever().retrieveWithIds(
 								genealogy.getElements());
 
 				CodeFragmentInfo startFragment = null;
 				CodeFragmentInfo endFragment = null;
 				for (final CodeFragmentInfo fragment : fragments.values()) {
 					if (fragment.getStartRevisionId() == startRevisionId) {
 						startFragment = fragment;
 					}
 					if (fragment.getEndRevisionId() == endRevisionId) {
 						endFragment = fragment;
 					}
 				}
 
 				final Map<Long, FileInfo> files = dbManager.getFileRetriever()
 						.retrieveWithIds(startFragment.getOwnerFileId(),
 								endFragment.getOwnerFileId());
 				final Map<Long, CRD> crds = dbManager.getCrdRetriever()
 						.retrieveWithIds(startFragment.getCrdId(),
 								endFragment.getCrdId());
 
 				final String bRev = revisions.get(startRevisionId)
 						.getIdentifier();
 				final String bFile = files.get(startFragment.getOwnerFileId())
 						.getPath();
 				final String bMethod = crds.get(startFragment.getCrdId())
 						.getAnchor();
 				final String bMethodName = bMethod.substring(0,
 						bMethod.indexOf("("));
 				final int bStart = startFragment.getStartLine();
 				final int bEnd = startFragment.getEndLine();
 
				final int bSize = startFragment.getSize();

 				final String aRev = revisions.get(endRevisionId)
 						.getIdentifier();
 				final String aFile = files.get(endFragment.getOwnerFileId())
 						.getPath();
 				final String aMethod = crds.get(endFragment.getCrdId())
 						.getAnchor();
 				final String aMethodName = aMethod.substring(0,
 						aMethod.indexOf("("));
 				final int aStart = endFragment.getStartLine();
 				final int aEnd = endFragment.getEndLine();
 
				final int aSize = endFragment.getSize();

				if (aSize <= bSize || bSize < threshold) {
					continue;
				}

 				pw.println(index + "," + bRev + "," + bFile + "," + bMethodName
 						+ "," + bStart + "," + bEnd + "," + aRev + "," + aFile
 						+ "," + aMethodName + "," + aStart + "," + aEnd);
 
 				try {
 					final SortedMap<Integer, String> beforeFileContents = getSrcLines(
 							repoManager, bRev, bFile);
 					final SortedMap<Integer, String> afterFileContents = getSrcLines(
 							repoManager, aRev, aFile);
 
 					final String bMethodContent = getMethodContent(
 							beforeFileContents, bStart, bEnd);
 					final String aMethodContent = getMethodContent(
 							afterFileContents, aStart, aEnd);
 
 					final String outputFile = workingDir + File.separator
 							+ index + ".txt";
 					final PrintWriter pw2 = new PrintWriter(new BufferedWriter(
 							new FileWriter(new File(outputFile))));
 					pw2.println("BEFORE_METHOD");
 					pw2.println("rev:" + bRev + ", " + bFile);
 					pw2.println("--");
 					pw2.println(bMethodContent);
 					pw2.println("--");
 					pw2.println();
 					pw2.println("AFTER_METHOD");
 					pw2.println("rev:" + aRev + ", " + aFile);
 					pw2.println("--");
 					pw2.println(aMethodContent);
 					pw2.println("--");
 					pw2.println("");
 
 					pw2.close();
 				} catch (Exception e) {
 					// ignore
 				}
 
 				index++;
 			}
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			if (dbManager != null) {
 				dbManager.close();
 			}
 			if (pw != null) {
 				pw.close();
 			}
 		}
 	}
 
 	private static SortedMap<Integer, String> getSrcLines(
 			final SVNRepositoryManager repoManager, final String rev,
 			final String file) throws Exception {
 		final String src = repoManager.getFileContents(Long.parseLong(rev),
 				file);
 		final SortedMap<Integer, String> srcLines = new TreeMap<Integer, String>();
 		int count = 1;
 		for (final String str : src.split(LINE_SEPARATOR)) {
 			srcLines.put(count++, str + LINE_SEPARATOR);
 		}
 		return srcLines;
 	}
 
 	private static String getMethodContent(
 			final SortedMap<Integer, String> fileContents, final int start,
 			final int end) {
 		final StringBuilder builder = new StringBuilder();
 
 		for (int i = start; i <= end; i++) {
 			builder.append(fileContents.get(i));
 		}
 
 		return builder.toString();
 	}
 
 }
