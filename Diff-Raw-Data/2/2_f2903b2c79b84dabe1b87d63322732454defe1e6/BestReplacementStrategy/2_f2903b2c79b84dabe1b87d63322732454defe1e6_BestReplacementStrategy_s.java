 package ubadb.components.bufferManager.bufferPool.replacementStrategies.beststrategy;
 
 import java.util.Collection;
 
 import ubadb.apps.bufferManagement.PageReference;
 import ubadb.apps.bufferManagement.PageReferenceTrace;
 import ubadb.common.Page;
 import ubadb.components.bufferManager.bufferPool.BufferFrame;
 import ubadb.components.bufferManager.bufferPool.replacementStrategies.PageReplacementStrategy;
 import ubadb.exceptions.PageReplacementStrategyException;
 
 public class BestReplacementStrategy implements PageReplacementStrategy {
 	private PageReferenceTrace trace;
 	private int positionInTrace = -1;
 
	BestReplacementStrategy(PageReferenceTrace trace) {
 		this.trace = trace;
 	}
 
 	public BufferFrame findVictim(Collection<BufferFrame> bufferFrames)
 			throws PageReplacementStrategyException {
 
 		BufferFrame victim = null;
 
 		int longerFutureRequestTime = -1;
 
 		for (BufferFrame bufferFrame : bufferFrames) {
 			int futureRequestTime = getFutureRequestTime(bufferFrame);
 			if (bufferFrame.canBeReplaced()
 					&& (futureRequestTime > longerFutureRequestTime)) {
 				victim = bufferFrame;
 				longerFutureRequestTime = futureRequestTime;
 			}
 		}
 
 		if (victim == null)
 			throw new PageReplacementStrategyException(
 					"No page can be removed from pool");
 		else
 			return victim;
 	}
 
 	public int getFutureRequestTime(BufferFrame bufferFrame) {
 		int position = 0;
 
 		for (PageReference pageReference : trace.getPageReferences()) {
 			if (position > positionInTrace
 					&& pageReference.getPageId().equals(
 							bufferFrame.getPage().getPageId())) {
 				break;
 			}
 			position++;
 		}
 		return position - positionInTrace;
 	}
 
 	public BufferFrame createNewFrame(Page page) {
 		return new BufferFrame(page);
 	}
 
 	public void nextPositionInTrace() {
 		positionInTrace++;
 	}
 
 }
