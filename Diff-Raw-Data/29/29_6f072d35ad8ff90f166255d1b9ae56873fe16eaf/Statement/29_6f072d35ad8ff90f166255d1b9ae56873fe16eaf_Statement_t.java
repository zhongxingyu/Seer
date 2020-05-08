 package uk.ac.ic.doc.cfg.model.scope;
 
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 
 import uk.ac.ic.doc.cfg.model.BasicBlock;
 
 class Statement {
 
 	private class BlockSet implements Iterable<BasicBlock> {
 
 		protected Set<BasicBlock> blocks = new HashSet<BasicBlock>();
 
 		public void add(BasicBlock block) {
 			blocks.add(block);
 		}
 
 		public void inherit(BlockSet other) {
 			blocks.addAll(other.blocks);
 		}
 
 		public boolean isEmpty() {
 			return blocks.isEmpty();
 		}
 
 		public int size() {
 			return blocks.size();
 		}
 
 		public Iterator<BasicBlock> iterator() {
 			return blocks.iterator();
 		}
 
 		@Override
 		public String toString() {
 			return blocks.toString();
 		}
 
 		@Override
 		public int hashCode() {
 			return blocks.hashCode();
 		}
 
 		@Override
 		public boolean equals(Object obj) {
 			if (!(obj instanceof BlockSet))
 				return false;
 
 			return blocks.equals(((BlockSet) obj).blocks);
 		}
 	}
 
 	private class Entry extends BlockSet {
 
 	}
 
 	public class Exit extends BlockSet {
 
 		public void linkTo(Statement successor) {
 			for (BasicBlock entryBlock : successor.inlinks())
 				for (BasicBlock exitBlock : this)
 					exitBlock.link(entryBlock);
 		}
 	}
 
 	private Exit fallthroughQueue = new Exit();
 	private Exit breakoutQueue = new Exit();
 	private Exit continueQueue = new Exit();
 	private Exit returnQueue = new Exit();
 	private Exit raiseQueue = new Exit();
 
 	private Entry inlinkQueue = new Entry();
 
 	protected Exit fallthroughs() {
 		return fallthroughQueue;
 	}
 
 	protected Exit breakouts() {
 		return breakoutQueue;
 	}
 
 	protected Exit continues() {
 		return continueQueue;
 	}
 
 	protected Exit returns() {
 		return returnQueue;
 	}
 
 	protected Exit raises() {
 		return raiseQueue;
 	}
 
 	private Entry inlinks() {
 		return inlinkQueue;
 	}
 	
 	protected Exit allExits() {
 		
 		Exit all = new Exit();
 		
 		all.inherit(fallthroughs());
 		all.inherit(breakouts());
 		all.inherit(raises());
 		all.inherit(continues());
 		all.inherit(returns());
 		
 		return all;
 	}
 
 	protected void fallthrough(BasicBlock block) {
 		fallthroughs().add(block);
 	}
 
 	protected void inlink(BasicBlock block) {
 		inlinks().add(block);
 	}
 
 	protected void linkFallThroughsTo(Statement successor) {
 		fallthroughs().linkTo(successor);
 	}
 
 	protected void linkContinuesTo(Statement successor) {
 		continues().linkTo(successor);
 	}
 
 	protected void linkReturnsTo(Statement successor) {
 		returns().linkTo(successor);
 	}
 
 	protected void linkRaisesTo(Statement successor) {
 		raises().linkTo(successor);
 	}
 
 	protected void linkBreaksTo(Statement successor) {
 		breakouts().linkTo(successor);
 	}
 
 	protected boolean canRaise() {
 		return !raises().isEmpty();
 	}
 
 	protected boolean canReturn() {
 		return !returns().isEmpty();
 	}
 
 	protected boolean canFallThrough() {
 		return !fallthroughs().isEmpty();
 	}
 
 	protected boolean canBreak() {
 		return !breakouts().isEmpty();
 	}
 
 	protected boolean canContinue() {
 		return !continues().isEmpty();
 	}
 	
 	protected void inheritFallthroughsFrom(Statement otherExits) {
 		fallthroughs().inherit(otherExits.fallthroughs());
 	}
 
 	protected void inheritNonLoopExitsFrom(Statement otherExits) {
 		returns().inherit(otherExits.returns());
 		inheritRaisesFrom(otherExits);
 	}
 
 	protected void inheritRaisesFrom(Statement otherExits) {
 		raises().inherit(otherExits.raises());
 	}
 
	protected void inheritReturnsFrom(Statement otherExits) {
		returns().inherit(otherExits.returns());
	}

	protected void inheritBreaksFrom(Statement otherExits) {
		breakouts().inherit(otherExits.breakouts());
	}

	protected void inheritContinuesFrom(Statement otherExits) {
		continues().inherit(otherExits.continues());
	}

 	protected void inheritInlinksFrom(Statement other) {
 		inlinks().inherit(other.inlinks());
 	}
 
 	protected void inheritAllButFallthroughsFrom(Statement otherExits) {
 
 		breakouts().inherit(otherExits.breakouts());
 		continues().inherit(otherExits.continues());
 		returns().inherit(otherExits.returns());
 		inheritRaisesFrom(otherExits);
 	}
 
 	protected void inheritExitsFrom(Statement otherExits) {
 		inheritAllButFallthroughsFrom(otherExits);
 		inheritFallthroughsFrom(otherExits);
 	}
 
 	protected void convertBreakoutsToFallthroughs(Statement successor) {
 		fallthroughs().inherit(successor.breakouts());
 	}
 
 	protected void convertFallthroughsToRaises(Statement successor) {
 		raises().inherit(successor.fallthroughs());
 	}
 
 	protected void convertFallthroughsToReturns(Statement successor) {
 		returns().inherit(successor.fallthroughs());
 	}
 
 	protected void convertFallthroughsToBreaks(Statement successor) {
 		breakouts().inherit(successor.fallthroughs());
 	}
 
 	/**
 	 * In many cases a statement will result in a single fallthrough block
 	 * 
 	 * @return Statement's unique fallthrough block.
 	 */
 	protected BasicBlock uniqueFallthrough() {
 		if (fallthroughs().size() != 1)
 			throw new Error("No unique fallthough block");
 
 		return fallthroughs().iterator().next();
 	}
 
 	protected int exitSize() {
 		return fallthroughs().size() + breakouts().size() + continues().size()
 				+ returns().size() + raises().size();
 	}
 
 	public boolean isEndOfBlock() {
 		if (exitSize() > 1 || !canFallThrough())
 			return true;
 
 		// If the one fallthrough block already has a successor
 		return uniqueFallthrough().isClosed();
 	}
 
 	@Override
 	public String toString() {
 		return "\n\nINLINK:\n" + stringise(inlinks()) + "\n\nFALLTHROUGH:\n"
 				+ stringise(fallthroughs()) + "\n\nBREAKOUT:\n"
 				+ stringise(breakouts()) + "\n\nCONTINUE:\n"
 				+ stringise(continues()) + "\n\nRETURN:\n"
 				+ stringise(returns()) + "\n\nRAISE:\n" + stringise(raises());
 	}
 
 	private static String stringise(Object obj) {
 		return (obj == null) ? "null" : obj.toString();
 	}
 }
