 /**
  * 
  */
 package simpledb.tx.concurrency;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Queue;
 import java.util.TreeSet;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import simpledb.file.Block;
 import simpledb.tx.Transaction;
 
 /**
  * @author directxman12
  *
  */
 public class WaitsForLockTable
 {
 	protected HashMap<Transaction, ArrayList<Transaction>> _edges;
 	protected int _maxInd = 0;
 	protected TreeSet<Transaction> _nodes;
 	protected HashMap<Block, Transaction> _locks;
 	protected HashMap<Block, Integer> _lockVals;
 	protected HashMap<Block, Queue<Transaction>> _sLockOthers;
 	protected HashMap<Transaction, ArrayList<Block>> _waitingOn;
 	
 	
 	public WaitsForLockTable()
 	{
 		super();
 		_edges = new HashMap<Transaction, ArrayList<Transaction>>();
 		_locks = new HashMap<Block, Transaction>();
 		_sLockOthers = new HashMap<Block, Queue<Transaction>>();
 		_nodes = new TreeSet<Transaction>();
 		_lockVals = new HashMap<Block, Integer>();
 		_waitingOn = new HashMap<Transaction, ArrayList<Block>>();
 	}
 	
 	public synchronized void addWaitingOn(Transaction trans, Block blk)
 	{
 		if (_waitingOn.get(trans) == null) _waitingOn.put(trans, new ArrayList<Block>());
 		_waitingOn.get(trans).add(blk);
 	}
 	
 	public synchronized void sLock(Block blk, Transaction trans)
 	{
 		_nodes.add(trans);
 		if (_locks.get(blk) != null && _lockVals.get(blk) < 0)
 		{
 			addEdge(trans, _locks.get(blk));
 			addWaitingOn(trans, blk);
 			
 			if (hasCycle(blk, trans))
 			{
 				_edges.get(blk).remove(_locks.get(blk));
 				_waitingOn.get(trans).remove(blk);
 				throw new LockAbortException();
 			}
 			
 			while(_locks.get(blk) != null && _lockVals.get(blk) < 0)
 			{
 				try
 				{
 					wait();
 				}
 				catch (InterruptedException ex)
 				{
 					throw new LockAbortException();
 				}
 			}
 		}
 		
 		if (_locks.get(blk) != null && _lockVals.get(blk) > 0)
 		{
 			pushOthersLock(blk, trans);
 		}
 		else
 		{
 			_locks.put(blk, trans);
 		}
 		
 		Integer prevVal = _lockVals.get(blk);
 		if (prevVal == null) prevVal = 0;
 		_lockVals.put(blk, prevVal + 1);
 	}
 	
 	protected synchronized void pushOthersLock(Block blk, Transaction trans)
 	{
 		if (_sLockOthers.get(blk) == null) _sLockOthers.put(blk, new LinkedBlockingQueue<Transaction>());
 		_sLockOthers.get(blk).add(trans);
 	}
 	
 	protected synchronized void addEdge(Transaction src, Transaction dest)
 	{
 		if (!_edges.containsKey(src))
 		{
 			ArrayList<Transaction> ar = new ArrayList<Transaction>();
 			ar.add(dest);
 			_edges.put(src, ar);
 		}
 		else
 		{
			if (!_edges.get(src).contains(dest)) _edges.get(src).add(dest);
 		}
 	}
 	
 	public synchronized void xLock(Block blk, Transaction trans)
 	{
 		_nodes.add(trans);
 		if (_locks.get(blk) != null)
 		{
 			Transaction oldTrans = trans;
 			addEdge(trans, _locks.get(blk));
 			addWaitingOn(trans, blk);
 			if (hasCycle(blk, trans))
 			{
 				_edges.get(blk).remove(_locks.get(blk));
 				_waitingOn.get(trans).remove(blk);
 				throw new LockAbortException();
 			}
 			while (_locks.get(blk) != null && _lockVals.get(blk) != 0)
 			{
 				try
 				{
 					wait();
 				}
 				catch (InterruptedException ex)
 				{
 					throw new LockAbortException();
 				}
 				
 				if (_locks.get(blk) != oldTrans)
 				{
 					_edges.get(blk).remove(oldTrans);
 					addEdge(trans, _locks.get(blk));
 					oldTrans = _locks.get(blk);
 				}
 			}
 		}
 		_locks.put(blk, trans);
 		_lockVals.put(blk, -1);
 	}
 	
 	public synchronized boolean hasCycle(Block blk, Transaction trans)
 	{
 		return false; // TODO: implement the hasCycle function
 	}
 	
 	public synchronized void unlock(Block blk, Transaction trans)
 	{
 		// check to see if this is the primary holder of the lock
 		if (_locks.get(blk).equals(trans))
 		{
 			// if yes, unset this as the primary holder and set a new primary holder if there is one
 			_locks.remove(blk);
 			_lockVals.put(blk, _lockVals.get(blk)-1);
 			if (_lockVals.get(blk) > 0) _locks.put(blk, _sLockOthers.get(blk).poll());
 		}
 		else
 		{
 			// otherwise just remove this from the list of secondary holders
 			_lockVals.put(blk, _lockVals.get(blk)-1);
 			_sLockOthers.get(blk).remove(trans);
 		}
 		
 		// see if we can remove edges
 		for (Transaction t : _waitingOn.keySet())
 		{
 			if (_waitingOn.get(t).contains(blk))
 			{
 				_waitingOn.get(t).remove(blk);
 			}
 			
 			if (hasNoIntersection(_waitingOn.get(t), getHeldLocks(trans)) && _edges.get(t).contains(trans)) _edges.get(t).remove(trans);
 		}
 		
 		// tell other waiting transactions that something has changed
 		notifyAll();
 	}
 	
 	public synchronized boolean hasNoIntersection(Collection<Block> c1, Collection<Block> c2)
 	{
 		for (Block b : c1)
 		{
 			if (c2.contains(b)) return false;
 		}
 		return true;
 	}
 	
 	public synchronized Collection<Block> getHeldLocks(Transaction trans)
 	{
 		Collection<Block> res = new ArrayList<Block>();
 		for (Block b : _locks.keySet())
 		{
 			if (_locks.get(b).equals(trans)) res.add(b);
 		}
 		
 		return res;
 	}
 }
