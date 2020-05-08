 package ubadb.components.bufferManager.bufferPool.replacementStrategies.count;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 import ubadb.common.Page;
 import ubadb.common.PageId;
 import ubadb.components.bufferManager.bufferPool.BufferFrame;
 import ubadb.components.bufferManager.bufferPool.replacementStrategies.PageReplacementStrategy;
 import ubadb.exceptions.PageReplacementStrategyException;
 
 /**
  * PageReplacementStrategy based on total historic count of pins per page id.
 * This strategy is known as "Not frequently used"
  * 
  * @author Grupo9
  */
 public final class CountReplacementStrategy implements PageReplacementStrategy {
 
 	/**
 	 * Total count of pins by pageId.
 	 */
 	private Map<PageId, Integer> count = new HashMap<PageId, Integer>();
 
	/*
	 * (non-Javadoc)
	 * 
	 * @see ubadb.components.bufferManager.bufferPool.replacementStrategies.
	 * PageReplacementStrategy#findVictim(java.util.Collection)
 	 */
 	public BufferFrame findVictim(Collection<BufferFrame> bufferFrames)
 			throws PageReplacementStrategyException {
 		BufferFrame victim = lessCounted(bufferFrames);
 		if (victim == null)
 			throw new PageReplacementStrategyException("No victim found");
 		return victim;
 	}
 
 	/**
 	 * Counts a page pin.
 	 * 
	 * @param id
	 *            the pinned page id.
 	 */
 	void countPin(PageId id) {
 		Integer c = count.get(id);
 		if (c == null) {
 			c = 0;
 		}
 		c++;
 		count.put(id, c);
 	}
 
 	/**
 	 * Return the bufferframe holding the less pinned page.
 	 * 
	 * @param candidates
	 *            the bufferframe candidates for removal.
 	 * @return the less counted bufferframe holding the less pinned page.
 	 */
 	private BufferFrame lessCounted(Collection<BufferFrame> candidates) {
 		BufferFrame result = null;
 		int resultCount = Integer.MAX_VALUE;
 		for (BufferFrame frame : candidates) {
 			// If buffer cannot be replaced, ignore.
 			if (!frame.canBeReplaced()) {
 				continue;
 			}
 			// Check pin count for the page id being hold by the BufferFrame.
 			PageId id = frame.getPage().getPageId();
 			Integer c = count.get(id);
 			// Compare with highest count found so far...
 			if (c == null || c >= resultCount) {
 				continue;
 			}
 			resultCount = c;
 			result = frame;
 		}
 		return result;
 	}
 
	/*
	 * (non-Javadoc)
	 * 
	 * @see ubadb.components.bufferManager.bufferPool.replacementStrategies.
	 * PageReplacementStrategy#createNewFrame(ubadb.common.Page)
 	 */
 	public BufferFrame createNewFrame(Page page) {
 		return new CountBufferFrame(this, page);
 	}
 }
