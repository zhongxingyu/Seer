 package jp.ac.osaka_u.ist.sdl.ectec.analyzer.sourceanalyzer.linker;
 
 import java.util.Collection;
 import java.util.Map;
 
 import jp.ac.osaka_u.ist.sdl.ectec.analyzer.sourceanalyzer.similarity.ICRDSimilarityCalculator;
 import jp.ac.osaka_u.ist.sdl.ectec.data.CRD;
 import jp.ac.osaka_u.ist.sdl.ectec.data.CodeFragmentInfo;
 import jp.ac.osaka_u.ist.sdl.ectec.data.CodeFragmentLinkInfo;
 
 /**
  * An interface to provide a function to detect links of code fragments between
  * two revisions
  * 
  * @author k-hotta
  * 
  */
 public interface ICodeFragmentLinker {
 
 	/**
 	 * detect pairs of code fragments
 	 * 
 	 * @param beforeBlocks
 	 * @param afterBlocks
 	 * @param similarityCalculator
 	 * @param similarityThreshold
 	 * @param crds
 	 * @param beforeRevisionId
 	 * @param afterRevisionId
 	 * @return
 	 */
 	public Map<Long, CodeFragmentLinkInfo> detectFragmentPairs(
 			final Collection<CodeFragmentInfo> beforeBlocks,
 			final Collection<CodeFragmentInfo> afterBlocks,
 			final ICRDSimilarityCalculator similarityCalculator,
			final double similarityThreshold, final Map<Long, CRD> crds,
 			final long beforeRevisionId, final long afterRevisionId);
 }
