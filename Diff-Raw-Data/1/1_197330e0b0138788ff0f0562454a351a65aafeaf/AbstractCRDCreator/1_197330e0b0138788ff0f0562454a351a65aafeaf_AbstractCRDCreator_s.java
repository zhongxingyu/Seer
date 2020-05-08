 package jp.ac.osaka_u.ist.sdl.ectec.analyzer.sourceanalyzer.crd;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import jp.ac.osaka_u.ist.sdl.ectec.data.BlockType;
 import jp.ac.osaka_u.ist.sdl.ectec.data.CRD;
 import jp.ac.osaka_u.ist.sdl.ectec.settings.Constants;
 
 import org.eclipse.jdt.core.dom.ASTNode;
 
 /**
  * A class to create a crd for a given node
  * 
  * @author k-hotta
  * 
  */
 public abstract class AbstractCRDCreator<T extends ASTNode> {
 
 	/**
 	 * the node to be analyzed
 	 */
 	protected final T node;
 
 	/**
 	 * the crd for the parent of this node
 	 */
 	protected final CRD parent;
 
 	/**
 	 * the type of the block
 	 */
 	protected final BlockType bType;
 
 	public AbstractCRDCreator(final T node, final CRD parent,
 			final BlockType bType) {
 		this.node = node;
 		this.parent = parent;
 		this.bType = bType;
 	}
 
 	/**
 	 * create a new instance of CRD for this block
 	 * 
 	 * @return
 	 */
 	public CRD createCrd() {
 		final String head = bType.getHead();
 		final String anchor = getAnchor();
 
 		final List<Long> ancestorIds = new ArrayList<Long>();
 
 		if (parent != null) {
 			for (final long ancestorId : parent.getAncestors()) {
 				ancestorIds.add(ancestorId);
 			}
 		}
 
 		final MetricsCalculator cmCalculator = new MetricsCalculator();
 		node.accept(cmCalculator);
 		final int cm = cmCalculator.getCC() + cmCalculator.getFO();
 
 		final String thisCrdStr = getStringCrdForThisBlock(head, anchor, cm);
 		final String fullText = (parent == null) ? thisCrdStr : parent
 				.getFullText() + thisCrdStr;
 
 		return new CRD(bType, head, anchor, cm, ancestorIds, fullText);
 	}
 
 	/**
 	 * get the string representation of THIS block
 	 * 
 	 * @param head
 	 * @param anchor
 	 * @param cm
 	 * @return
 	 */
 	private String getStringCrdForThisBlock(final String head,
 			final String anchor, final int cm) {
 		final StringBuilder builder = new StringBuilder();
 
 		builder.append(head + ",");
 		builder.append(anchor + ",");
 		builder.append(cm + Constants.LINE_SEPARATOR);
 
 		return builder.toString();
 	}
 
 	/**
 	 * get the anchor of the block
 	 * 
 	 * @return
 	 */
 	protected abstract String getAnchor();
 
 }
