 package jp.ac.osaka_u.ist.sdl.ectec.analyzer.genealogydetector;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import jp.ac.osaka_u.ist.sdl.ectec.data.CodeFragmentGenealogyInfo;
 import jp.ac.osaka_u.ist.sdl.ectec.data.CodeFragmentInfo;
 import jp.ac.osaka_u.ist.sdl.ectec.data.CodeFragmentLinkInfo;
 import jp.ac.osaka_u.ist.sdl.ectec.data.retriever.AbstractElementRetriever;
 import jp.ac.osaka_u.ist.sdl.ectec.data.retriever.LinkElementRetriever;
 
 /**
  * A class to create instances of code fragment genealogies from the given
  * chains
  * 
  * @author k-hotta
  * 
  */
 public class FragmentChainFinalizer
 		extends
 		ElementChainFinalizer<CodeFragmentInfo, CodeFragmentLinkInfo, CodeFragmentGenealogyInfo> {
 
 	public FragmentChainFinalizer(
 			AbstractElementRetriever<CodeFragmentInfo> elementRetriever,
 			LinkElementRetriever<CodeFragmentLinkInfo> linkRetriever) {
 		super(elementRetriever, linkRetriever);
 	}
 
 	@Override
 	protected CodeFragmentGenealogyInfo createInstanceFromChain(
 			ElementChain<CodeFragmentLinkInfo> chain) throws Exception {
 		final long startRevisionId = chain.getFirstRevision();
 		final long endRevisionId = chain.getLastRevision();
 		final List<Long> elements = new ArrayList<Long>();
 		final List<Long> links = new ArrayList<Long>();
 
 		elements.addAll(chain.getElements());
 		links.addAll(chain.getLinks());
 
 		final Map<Long, CodeFragmentLinkInfo> linksMap = linkRetriever
 				.retrieveWithIds(links);
 		int changedCount = 0;
 		CodeFragmentLinkInfo startLink = null;
 		CodeFragmentLinkInfo endLink = null;
 
 		for (final CodeFragmentLinkInfo link : linksMap.values()) {
 			if (link.isChanged()) {
 				changedCount++;
 			}
 			if (link.getBeforeRevisionId() == startRevisionId) {
 				startLink = link;
 			}
 			if (link.getAfterRevisionId() == endRevisionId) {
 				endLink = link;
 			}
 		}
 
 		final Map<Long, CodeFragmentInfo> elementsMap = elementRetriever
 				.retrieveWithIds(startLink.getBeforeElementId(),
 						endLink.getAfterElementId());
 		final CodeFragmentInfo startElement = elementsMap.get(startLink
 				.getBeforeElementId());
 		final CodeFragmentInfo endElement = elementsMap.get(endLink
 				.getAfterElementId());
 
		return new CodeFragmentGenealogyInfo(startElement.getStartRevisionId(),
				endElement.getEndRevisionId(), elements, links, changedCount);
 	}
 
 	@Override
 	protected CodeFragmentGenealogyInfo createInstanceFromElement(
 			CodeFragmentInfo element) {
 		final long startRevisionId = element.getStartRevisionId();
 		final long endRevisionId = element.getEndRevisionId();
 		final List<Long> elements = new ArrayList<Long>();
 		final List<Long> links = new ArrayList<Long>();
 		elements.add(element.getId());
 		final int changedCount = 0;
 
 		return new CodeFragmentGenealogyInfo(startRevisionId, endRevisionId,
 				elements, links, changedCount);
 	}
 
 }
