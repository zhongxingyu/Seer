 package jp.ac.osaka_u.ist.sdl.ectec.analyzer.sourceanalyzer.linker;
 
 import java.util.Collection;
 import java.util.Map;
 
 import jp.ac.osaka_u.ist.sdl.ectec.analyzer.sourceanalyzer.similarity.ICRDSimilarityCalculator;
 import jp.ac.osaka_u.ist.sdl.ectec.data.CRD;
 import jp.ac.osaka_u.ist.sdl.ectec.data.CodeFragmentInfo;
 import jp.ac.osaka_u.ist.sdl.ectec.data.CodeFragmentLinkInfo;
 
 public class MultipleCodeFragmentLinker implements ICodeFragmentLinker {
 
 	@Override
 	public Map<Long, CodeFragmentLinkInfo> detectFragmentPairs(
 			Collection<CodeFragmentInfo> beforeBlocks,
 			Collection<CodeFragmentInfo> afterBlocks,
 			ICRDSimilarityCalculator similarityCalculator,
			double similarityThreshold, Map<Long, CRD> crds,
 			long beforeRevisionId, long afterRevisionId) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 }
