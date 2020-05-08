 package jp.ac.osaka_u.ist.sdl.ectec.analyzer.sourceanalyzer.linker;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Queue;
 import java.util.Set;
 import java.util.TreeMap;
 
 import jp.ac.osaka_u.ist.sdl.ectec.analyzer.sourceanalyzer.similarity.ICRDSimilarityCalculator;
 import jp.ac.osaka_u.ist.sdl.ectec.data.CRD;
 import jp.ac.osaka_u.ist.sdl.ectec.data.CodeFragmentInfo;
 import jp.ac.osaka_u.ist.sdl.ectec.data.CodeFragmentLinkInfo;
 import jp.ac.osaka_u.ist.sdl.ectec.util.Table;
 
 /**
  * A class to detect 1 by 1 links of code fragments
  * 
  * @author k-hotta
  * 
  */
 public class SingleCodeFragmentLinker implements ICodeFragmentLinker {
 
 	@Override
 	public Map<Long, CodeFragmentLinkInfo> detectFragmentPairs(
 			Collection<CodeFragmentInfo> beforeFragments,
 			Collection<CodeFragmentInfo> afterFragments,
 			ICRDSimilarityCalculator similarityCalculator,
			double similarityThreshold, Map<Long, CRD> crds,
 			long beforeRevisionId, long afterRevisionId) {
 		final FragmentLinkConditionUmpire umpire = new FragmentLinkConditionUmpire(
 				similarityThreshold);
 		final Map<CodeFragmentInfo, CodeFragmentInfo> pairs = detectPairs(
 				beforeFragments, afterFragments, similarityCalculator, umpire,
 				crds);
 
 		return makeLinkInstances(pairs, beforeRevisionId, afterRevisionId);
 	}
 
 	/**
 	 * make fragment link instances with the given pairs
 	 * 
 	 * @param pairs
 	 * @param beforeRevisionId
 	 * @param afterRevisionId
 	 * @return
 	 */
 	private final Map<Long, CodeFragmentLinkInfo> makeLinkInstances(
 			final Map<CodeFragmentInfo, CodeFragmentInfo> pairs,
 			final long beforeRevisionId, final long afterRevisionId) {
 		final Map<Long, CodeFragmentLinkInfo> result = new TreeMap<Long, CodeFragmentLinkInfo>();
 		for (final Map.Entry<CodeFragmentInfo, CodeFragmentInfo> entry : pairs
 				.entrySet()) {
 			final CodeFragmentInfo beforeFragment = entry.getKey();
 			final CodeFragmentInfo afterFragment = entry.getValue();
 
 			final boolean changed = beforeFragment.getHash() == afterFragment
 					.getHash();
 
 			final CodeFragmentLinkInfo link = new CodeFragmentLinkInfo(
 					beforeFragment.getId(), afterFragment.getId(),
 					beforeRevisionId, afterRevisionId, changed);
 			result.put(link.getId(), link);
 		}
 		return Collections.unmodifiableMap(result);
 	}
 
 	/**
 	 * detect pairs of fragments as a map
 	 * 
 	 * @param beforeFragments
 	 * @param afterFragments
 	 * @param similarityCalculator
 	 * @param similarityThreshold
 	 * @param crds
 	 * @return
 	 */
 	private Map<CodeFragmentInfo, CodeFragmentInfo> detectPairs(
 			Collection<CodeFragmentInfo> beforeFragments,
 			Collection<CodeFragmentInfo> afterFragments,
 			ICRDSimilarityCalculator similarityCalculator,
 			FragmentLinkConditionUmpire umpire, Map<Long, CRD> crds) {
 		// the result (detected pairs of fragments) with the shuffled
 		// the key is an AFTER fragment and the value is a BEFORE fragment
 		final Map<CodeFragmentInfo, CodeFragmentInfo> reversedResult = new TreeMap<CodeFragmentInfo, CodeFragmentInfo>();
 
 		// evacuate the original collections
 		final Set<CodeFragmentInfo> beforeFragmentsSet = new HashSet<CodeFragmentInfo>();
 		beforeFragmentsSet.addAll(beforeFragments);
 		final Set<CodeFragmentInfo> afterFragmentsSet = new HashSet<CodeFragmentInfo>();
 		afterFragmentsSet.addAll(afterFragments);
 
 		// detect pairs of fragments whose crds are equal to each other
 		reversedResult.putAll(detectSameCrdFragmentPairs(beforeFragments,
 				afterFragments, crds));
 
 		// remove code fragments from the target which already have a partner
 		beforeFragmentsSet.removeAll(reversedResult.values());
 		afterFragmentsSet.removeAll(reversedResult.keySet());
 
 		/*
 		 * initialize the similarity table and with lists for each of remaining
 		 * before fragments
 		 */
 		final Table<Long, Long, Double> similarityTable = new Table<Long, Long, Double>();
 		final Map<CodeFragmentInfo, Queue<CodeFragmentInfo>> wishLists = new TreeMap<CodeFragmentInfo, Queue<CodeFragmentInfo>>();
 
 		fillSimilarityTableAndWishList(beforeFragmentsSet, afterFragmentsSet,
 				similarityTable, wishLists, similarityCalculator, umpire, crds);
 
 		final List<CodeFragmentInfo> unmarriedBeforeFragments = new ArrayList<CodeFragmentInfo>();
 		unmarriedBeforeFragments.addAll(beforeFragmentsSet);
 
 		while (true) {
 
 			// make every of unmarried before fragments attack the most favorite
 			// after fragment
 			// the contents in reversedResult and unmarriedBeforeMethods will
 			// change by this method call
 			if (processAllProposes(reversedResult, unmarriedBeforeFragments,
 					similarityTable, wishLists)) {
 				break;
 			}
 
 		}
 
 		final Map<CodeFragmentInfo, CodeFragmentInfo> result = tailorReversedMap(reversedResult);
 		return result;
 	}
 
 	/**
 	 * initialize the given similarity table and the given wish lists with the
 	 * specified values
 	 * 
 	 * @param beforeFragments
 	 * @param afterFragments
 	 * @param similarityTable
 	 * @param wishLists
 	 * @param similarityCalculator
 	 * @param similarityThreshold
 	 * @param crds
 	 */
 	private void fillSimilarityTableAndWishList(
 			final Collection<CodeFragmentInfo> beforeFragments,
 			final Collection<CodeFragmentInfo> afterFragments,
 			final Table<Long, Long, Double> similarityTable,
 			final Map<CodeFragmentInfo, Queue<CodeFragmentInfo>> wishLists,
 			final ICRDSimilarityCalculator similarityCalculator,
 			FragmentLinkConditionUmpire umpire, final Map<Long, CRD> crds) {
 		for (final CodeFragmentInfo beforeFragment : beforeFragments) {
 			// fill a row of similarity table
 			final Map<CodeFragmentInfo, Double> similarities = new TreeMap<CodeFragmentInfo, Double>();
 			final CRD beforeCrd = crds.get(beforeFragment.getCrdId());
 
 			for (final CodeFragmentInfo afterFragment : afterFragments) {
 				final CRD afterCrd = crds.get(afterFragment.getCrdId());
 				final double similarity = similarityCalculator.calcSimilarity(
 						beforeCrd, afterCrd);
 
 				// register the similarity into the table
 				// if the similarity is equal to or over than the threshold
 				if (umpire
 						.satisfyAllConditions(beforeCrd, afterCrd, similarity)) {
 					similarityTable.changeValueAt(beforeFragment.getId(),
 							afterFragment.getId(), similarity);
 					similarities.put(afterFragment, similarity);
 				}
 			}
 
 			// fill a wish list for the fragment under processing
 			final Queue<CodeFragmentInfo> wishList = sortWithValues(similarities);
 			wishLists.put(beforeFragment, wishList);
 		}
 	}
 
 	/**
 	 * sort the given map with the descending order of values and return the
 	 * result as a queue of keys
 	 * 
 	 * @param target
 	 * @return
 	 */
 	private Queue<CodeFragmentInfo> sortWithValues(
 			final Map<CodeFragmentInfo, Double> target) {
 		final Queue<CodeFragmentInfo> result = new LinkedList<CodeFragmentInfo>();
 		final int targetSize = target.size();
 
 		while (result.size() < targetSize) {
 			double maxValue = -1.0;
 			CodeFragmentInfo keyHasMaxValue = null;
 
 			for (final Map.Entry<CodeFragmentInfo, Double> entry : target
 					.entrySet()) {
 				final CodeFragmentInfo key = entry.getKey();
 				final double value = entry.getValue();
 
 				if (result.contains(key)) {
 					continue;
 				}
 
 				if (value > maxValue) {
 					maxValue = value;
 					keyHasMaxValue = key;
 				}
 			}
 
 			result.add(keyHasMaxValue);
 		}
 
 		return result;
 	}
 
 	/**
 	 * detect fragment pairs whose crds are equal to each other
 	 * 
 	 * @param beforeFragments
 	 * @param afterFragments
 	 * @param crds
 	 * @return
 	 */
 	private Map<CodeFragmentInfo, CodeFragmentInfo> detectSameCrdFragmentPairs(
 			final Collection<CodeFragmentInfo> beforeFragments,
 			final Collection<CodeFragmentInfo> afterFragments,
 			final Map<Long, CRD> crds) {
 		final Map<CodeFragmentInfo, CodeFragmentInfo> result = new TreeMap<CodeFragmentInfo, CodeFragmentInfo>();
 
 		final Set<CodeFragmentInfo> processedAfterMethods = new HashSet<CodeFragmentInfo>();
 
 		for (final CodeFragmentInfo beforeFragment : beforeFragments) {
 			for (final CodeFragmentInfo afterFragment : afterFragments) {
 				if (processedAfterMethods.contains(afterFragment)) {
 					continue;
 				}
 				if (crds.get(beforeFragment.getCrdId()).equals(
 						crds.get(afterFragment.getCrdId()))) {
 					result.put(afterFragment, beforeFragment);
 					processedAfterMethods.add(afterFragment);
 				}
 			}
 		}
 
 		return result;
 	}
 
 	/**
 	 * make unmarried before fragments propose to the favorite fragments
 	 * 
 	 * @param reversedResult
 	 * @param unmarriedBeforeFragments
 	 * @param similarityTable
 	 * @param wishLists
 	 * @return
 	 */
 	private boolean processAllProposes(
 			final Map<CodeFragmentInfo, CodeFragmentInfo> reversedResult,
 			final List<CodeFragmentInfo> unmarriedBeforeFragments,
 			final Table<Long, Long, Double> similarityTable,
 			final Map<CodeFragmentInfo, Queue<CodeFragmentInfo>> wishLists) {
 		// the set of code fragments which detect their partners in this loop
 		final Set<CodeFragmentInfo> marriedBeforeFragments = new HashSet<CodeFragmentInfo>();
 
 		// the set of code fragments which are said good-bye by their partners
 		// in this loop
 		final Set<CodeFragmentInfo> dumpedBeforeFragments = new HashSet<CodeFragmentInfo>();
 
 		// whether any of before fragments make a propose
 		// if this value is false in the tail of this method,
 		// all the remaining before fragments no longer have candidates
 		boolean anyoneAttacked = false;
 
 		/*
 		 * make propose
 		 */
 		for (final CodeFragmentInfo proposingFragment : unmarriedBeforeFragments) {
 
 			// propose to the most favorite after fragment
 			final CodeFragmentInfo proposedMethod = wishLists.get(
 					proposingFragment).poll();
 
 			// there are no candidate fragments that this fragment can propose
 			if (proposedMethod == null) {
 				continue;
 			}
 
 			anyoneAttacked = true;
 
 			/*
 			 * switch by whether the proposed fragment already has had its
 			 * fiance
 			 */
 
 			/*
 			 * exist
 			 */
 			if (reversedResult.containsKey(proposedMethod)) {
 				// this propose becomes to be successful if the proposing
 				// fragment is more similar to the proposed fragment than the
 				// fiance
 				// otherwise, the propose will fail
 
 				// the fiance, the rival
 				final CodeFragmentInfo rivalMethod = reversedResult
 						.get(proposedMethod);
 
 				// similarities
 				final double similarityBetweenProposingMethod = similarityTable
 						.getValueAt(proposingFragment.getId(),
 								proposedMethod.getId());
 				final double similarityBetweenCurrentPartner = similarityTable
 						.getValueAt(rivalMethod.getId(), proposedMethod.getId());
 
 				// have a match
 				if (similarityBetweenProposingMethod > similarityBetweenCurrentPartner) {
 					// the proposing fragment won!!
 					reversedResult.remove(proposedMethod);
 					reversedResult.put(proposedMethod, proposingFragment);
 					marriedBeforeFragments.add(proposingFragment);
 					dumpedBeforeFragments.add(rivalMethod);
 				} else {
 					// the proposing fragment lost
 					// there is nothing for the loser to be able to do
 				}
 
 			}
 
 			/*
 			 * not exist
 			 */
 			else {
 				// fortunately the propose is success
 				reversedResult.put(proposedMethod, proposingFragment);
 				marriedBeforeFragments.add(proposingFragment);
 			}
 		}
 
 		// remove fragments that got their partner from the unmarried list
 		unmarriedBeforeFragments.removeAll(marriedBeforeFragments);
 
 		// add fragments that were said good-bye from their partner into the
 		// unmarried list
 		unmarriedBeforeFragments.addAll(dumpedBeforeFragments);
 
 		return !anyoneAttacked;
 	}
 
 	/**
 	 * swap the keys and the values
 	 * 
 	 * @param target
 	 * @return
 	 */
 	private Map<CodeFragmentInfo, CodeFragmentInfo> tailorReversedMap(
 			final Map<CodeFragmentInfo, CodeFragmentInfo> target) {
 		final Map<CodeFragmentInfo, CodeFragmentInfo> result = new TreeMap<CodeFragmentInfo, CodeFragmentInfo>();
 
 		for (final Map.Entry<CodeFragmentInfo, CodeFragmentInfo> entry : target
 				.entrySet()) {
 			result.put(entry.getValue(), entry.getKey());
 		}
 
 		return result;
 	}
 
 }
