 package jp.ac.osaka_u.ist.sdl.ectec.analyzer.linker;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 
 import jp.ac.osaka_u.ist.sdl.ectec.analyzer.sourceanalyzer.similarity.ICRDSimilarityCalculator;
 import jp.ac.osaka_u.ist.sdl.ectec.data.CRD;
 import jp.ac.osaka_u.ist.sdl.ectec.data.CodeFragmentInfo;
 import jp.ac.osaka_u.ist.sdl.ectec.data.CodeFragmentLinkInfo;
 import jp.ac.osaka_u.ist.sdl.ectec.data.Commit;
 import jp.ac.osaka_u.ist.sdl.ectec.data.registerer.CodeFragmentLinkRegisterer;
 import jp.ac.osaka_u.ist.sdl.ectec.data.retriever.CRDRetriever;
 import jp.ac.osaka_u.ist.sdl.ectec.data.retriever.CodeFragmentRetriever;
 import jp.ac.osaka_u.ist.sdl.ectec.settings.MessagePrinter;
 
 /**
  * A class to detect links of code fragments with a single thread
  * 
  * @author k-hotta
  * 
  */
 public class CodeFragmentLinkDetector {
 
 	private final Map<Long, CodeFragmentLinkInfo> detectedLinks;
 
 	/**
 	 * the target commits
 	 */
 	private final Commit[] targetCommits;
 
 	/**
 	 * the registerer for links of code fragments
 	 */
 	private final CodeFragmentLinkRegisterer fragmentLinkRegisterer;
 
 	/**
 	 * the retriever for code fragments
 	 */
 	private final CodeFragmentRetriever fragmentRetriever;
 
 	/**
 	 * the retriever for crds
 	 */
 	private final CRDRetriever crdRetriever;
 
 	/**
 	 * the map between revision id and code fragments included in the revision
 	 */
 	private final Map<Long, Map<Long, CodeFragmentInfo>> codeFragments;
 
 	/**
 	 * the map between revision id and crds included in the revision
 	 */
 	private final Map<Long, Map<Long, CRD>> crds;
 
 	/**
 	 * already processed commits
 	 */
 	private final Map<Long, Commit> processedCommits;
 
 	/**
 	 * id of a revision and a collection of ids of commits that relates to the
 	 * revision
 	 */
 	private final Map<Long, Collection<Long>> revisionAndRelatedCommits;
 
 	/**
 	 * the linker
 	 */
 	private final ICodeFragmentLinker linker;
 
 	/**
 	 * the threshold for similarities
 	 */
 	private final double similarityThreshold;
 
 	/**
 	 * the similarity calculator for crds
 	 */
 	private final ICRDSimilarityCalculator similarityCalculator;
 
 	/**
 	 * the threshold for element
 	 */
 	private final int maxElementsCount;
 
 	public CodeFragmentLinkDetector(final Commit[] targetCommits,
 			final CodeFragmentLinkRegisterer fragmentLinkRegisterer,
 			final CodeFragmentRetriever fragmentRetriever,
 			final CRDRetriever crdRetriever,
 			final Map<Long, Collection<Long>> revisionAndRelatedCommits,
 			final ICodeFragmentLinker linker, final double similarityThreshold,
 			final ICRDSimilarityCalculator similarityCalculator,
 			final int maxElementsCount) {
 		this.detectedLinks = new TreeMap<Long, CodeFragmentLinkInfo>();
 		this.targetCommits = targetCommits;
 		this.fragmentLinkRegisterer = fragmentLinkRegisterer;
 		this.fragmentRetriever = fragmentRetriever;
 		this.crdRetriever = crdRetriever;
 		this.codeFragments = new TreeMap<Long, Map<Long, CodeFragmentInfo>>();
 		this.crds = new TreeMap<Long, Map<Long, CRD>>();
 		this.processedCommits = new TreeMap<Long, Commit>();
 		this.revisionAndRelatedCommits = revisionAndRelatedCommits;
 		this.linker = linker;
 		this.similarityThreshold = similarityThreshold;
 		this.similarityCalculator = similarityCalculator;
 		this.maxElementsCount = maxElementsCount;
 	}
 
 	/**
 	 * detect links and register them into db
 	 * 
 	 * @throws Exception
 	 */
 	public void detectAndRegister() throws Exception {
 		int numberOfLinks = 0;
 
 		for (int i = 0; i < targetCommits.length; i++) {
 			final Commit targetCommit = targetCommits[i];
 
 			final long beforeRevisionId = targetCommit.getBeforeRevisionId();
 			if (beforeRevisionId == -1) {
 				processedCommits.put(targetCommit.getId(), targetCommit);
 				MessagePrinter.println("\t[" + processedCommits.size() + "/"
 						+ targetCommits.length
 						+ "] processed the commit from revision "
 						+ targetCommit.getBeforeRevisionIdentifier()
 						+ " to revision "
 						+ targetCommit.getAfterRevisionIdentifier());
 				continue;
 			}
 			final long afterRevisionId = targetCommit.getAfterRevisionId();
 
 			retrieveElements(beforeRevisionId);
 			retrieveElements(afterRevisionId);
 
 			final Map<Long, CRD> currentCrds = new TreeMap<Long, CRD>();
 			currentCrds.putAll(crds.get(beforeRevisionId));
 			currentCrds.putAll(crds.get(afterRevisionId));
 
 			detectedLinks.putAll(linker.detectFragmentPairs(
 					codeFragments.get(beforeRevisionId).values(), codeFragments
 							.get(afterRevisionId).values(),
 					similarityCalculator, similarityThreshold, currentCrds,
 					beforeRevisionId, afterRevisionId));
 
 			processedCommits.put(targetCommit.getId(), targetCommit);
 			MessagePrinter.println("\t[" + processedCommits.size() + "/"
 					+ targetCommits.length
 					+ "] processed the commit from revision "
 					+ targetCommit.getBeforeRevisionIdentifier()
 					+ " to revision "
 					+ targetCommit.getAfterRevisionIdentifier());
 
 			if (detectedLinks.size() >= maxElementsCount) {
 				final Set<CodeFragmentLinkInfo> currentElements = new HashSet<CodeFragmentLinkInfo>();
 				currentElements.addAll(detectedLinks.values());
 				fragmentLinkRegisterer.register(currentElements);
 				MessagePrinter.println("\t" + currentElements.size()
 						+ " links of fragments have been registered into db");
 				numberOfLinks += currentElements.size();
 
 				for (final CodeFragmentLinkInfo link : currentElements) {
 					detectedLinks.remove(link.getId());
 				}
 			}
 
 			// remove fragments if they are no longer needed
			final Collection<Long> fragmentRevisionIds = codeFragments.keySet();
 			for (final long revisionId : fragmentRevisionIds) {
 				final Collection<Long> relatedCommits = revisionAndRelatedCommits
 						.get(revisionId);
 				if (processedCommits.keySet().containsAll(relatedCommits)) {
 					codeFragments.remove(revisionId);
 				}
 			}
 
 			// remove crds if they are no longer needed
			final Collection<Long> crdRevisionIds = crds.keySet();
 			for (final long revisionId : crdRevisionIds) {
 				final Collection<Long> relatedCommits = revisionAndRelatedCommits
 						.get(revisionId);
 				if (processedCommits.keySet().containsAll(relatedCommits)) {
 					crds.remove(revisionId);
 				}
 			}
 		}
 
 		MessagePrinter.println();
 
 		MessagePrinter
 				.println("\tregistering all the remaining elements into db ");
 		fragmentLinkRegisterer.register(detectedLinks.values());
 
 		numberOfLinks += detectedLinks.size();
 
 		MessagePrinter.println("\t\tOK");
 
 		MessagePrinter.println();
 
 		MessagePrinter.println("the numbers of detected elements are ... ");
 		MessagePrinter.println("\tLinks: " + numberOfLinks);
 	}
 
 	/**
 	 * retrieve elements if they have not been stored into the maps
 	 * 
 	 * @param revisionId
 	 * @throws SQLException
 	 */
 	protected void retrieveElements(final long revisionId) throws SQLException {
 		if (!codeFragments.containsKey(revisionId)) {
 			final Map<Long, CodeFragmentInfo> retrievedFragments = fragmentRetriever
 					.retrieveElementsInSpecifiedRevision(revisionId);
 			codeFragments.put(revisionId, retrievedFragments);
 		}
 
 		if (!crds.containsKey(revisionId)) {
 			final Map<Long, CodeFragmentInfo> fragments = codeFragments
 					.get(revisionId);
 			final List<Long> crdIds = new ArrayList<Long>();
 			for (final Map.Entry<Long, CodeFragmentInfo> entry : fragments
 					.entrySet()) {
 				crdIds.add(entry.getValue().getCrdId());
 			}
 
 			final Map<Long, CRD> retrievedCrds = crdRetriever
 					.retrieveWithIds(crdIds);
 			crds.put(revisionId, retrievedCrds);
 		}
 
 	}
 
 }
