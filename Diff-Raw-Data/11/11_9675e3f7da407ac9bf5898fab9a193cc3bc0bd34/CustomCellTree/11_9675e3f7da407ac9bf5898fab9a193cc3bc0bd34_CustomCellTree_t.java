 /**
  * 
  */
 package com.google.gwt.user.cellview.client;
 
 import java.util.ArrayList;
 
 import com.google.gwt.dom.client.BrowserEvents;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.user.client.Event;
 import com.google.gwt.view.client.TreeViewModel;
 
 /**
  * A view of a tree.
  *
  * <p>
  * This widget will <em>only</em> work in standards mode, which requires that
  * the HTML page in which it is run have an explicit &lt;!DOCTYPE&gt;
  * declaration.
  * </p>
  *
  * <p>
  * <h3>Examples</h3>
  * <dl>
  * <dt>Trivial example</dt>
  * <dd>{@example com.google.gwt.examples.cellview.CellTreeExample}</dd>
  * <dt>Complex example</dt>
  * <dd>{@example com.google.gwt.examples.cellview.CellTreeExample2}</dd>
  * </dl>
  * 
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 public class CustomCellTree extends CellTree {
 	
 	public <T> CustomCellTree(TreeViewModel viewModel, T rootValue,
 			Resources resources, CellTreeMessages messages) {
 		super(viewModel, rootValue, resources, messages);
 	}
 
 	/** 
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void onBrowserEvent(Event event) {
 		super.onBrowserEvent(event);
 
 		if (isRefreshing) return;
 
 		String eventType = event.getType();
 
 		final Element target = event.getEventTarget().cast();
 		ArrayList<Element> chain = new ArrayList<Element>();
 		collectElementChain(chain, getElement(), target);
 
 		final boolean isMouseDown = BrowserEvents.MOUSEDOWN.equals(eventType);
 		final CellTreeNodeView<?> nodeView = findItemByChain(chain, 0, rootNode);
 		if (nodeView != null && isMouseDown && !nodeView.isRootNode() && nodeView.getSelectionElement().isOrHasChild(target) && !nodeView.getImageElement().isOrHasChild(target)) {
 			// Open the node when the open image is clicked.
 			nodeView.setOpen(!nodeView.isOpen(), true);
 			return;
 		}
 	}
 
 
 	/**
 	 * Collects parents going up the element tree, terminated at the tree root.
 	 */
 	private void collectElementChain(ArrayList<Element> chain, Element hRoot,
 			Element hElem) {
 		if ((hElem == null) || (hElem == hRoot)) {
 			return;
 		}
 
 		collectElementChain(chain, hRoot, hElem.getParentElement());
 		chain.add(hElem);
 	}
 
 	private CellTreeNodeView<?> findItemByChain(ArrayList<Element> chain,
 			int idx, CellTreeNodeView<?> parent) {
 		if (idx == chain.size()) {
 			return parent;
 		}
 
 		Element hCurElem = chain.get(idx);
 		for (int i = 0, n = parent.getChildCount(); i < n; ++i) {
 			CellTreeNodeView<?> child = parent.getChildNode(i);
 			if (child.getElement() == hCurElem) {
 				CellTreeNodeView<?> retItem = findItemByChain(chain, idx + 1, child);
 				if (retItem == null) {
 					return child;
 				}
 				return retItem;
 			}
 		}
 
 		return findItemByChain(chain, idx + 1, parent);
 	}
 }
 
