 package edu.teco.dnd.module.config;
 
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Queue;
 
 /**
  * Does a depth first walk over a BlockTypeHolder.
  *
  * @author Philipp Adolf
  */
public class BlockTypeHolderIterator implements Iterator<BlockTypeHolder>, Iterable<BlockTypeHolder> {
 	/**
 	 * Queue for BlockTypeHolders that have not yet been visited.
 	 */
 	private final Queue<BlockTypeHolder> holders = new LinkedList<BlockTypeHolder>();
 	
 	/**
 	 * Creates a new iterator with a given root.
 	 * 
 	 * @param root the root BlockTypeHolder
 	 */
 	public BlockTypeHolderIterator(final BlockTypeHolder root) {
 		holders.add(root);
 	}
 
 	@Override
 	public boolean hasNext() {
 		return !holders.isEmpty();
 	}
 
 	@Override
 	public BlockTypeHolder next() {
 		final BlockTypeHolder next = holders.remove();
 		final Collection<BlockTypeHolder> children = next.getChildren();
 		if (children != null) {
 			holders.addAll(children);
 		}
 		return next;
 	}
 
 	@Override
 	public void remove() {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public Iterator<BlockTypeHolder> iterator() {
 		return this;
 	}
 }
