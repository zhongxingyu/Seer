 /*
  * Copyright (c) 2009, James Leigh All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 
  * - Redistributions of source code must retain the above copyright notice, this
  *   list of conditions and the following disclaimer.
  * - Redistributions in binary form must reproduce the above copyright notice,
  *   this list of conditions and the following disclaimer in the documentation
  *   and/or other materials provided with the distribution. 
  * - Neither the name of the openrdf.org nor the names of its contributors may
  *   be used to endorse or promote products derived from this software without
  *   specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  * 
  */
 package org.openrdf.sail.optimistic;
 
 import info.aduna.concurrent.locks.Lock;
 import info.aduna.concurrent.locks.ReadWriteLockManager;
 import info.aduna.concurrent.locks.WritePrefReadWriteLockManager;
 import info.aduna.iteration.CloseableIteration;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.openrdf.model.Model;
 import org.openrdf.model.ValueFactory;
 import org.openrdf.query.BindingSet;
 import org.openrdf.query.QueryEvaluationException;
 import org.openrdf.query.algebra.QueryRoot;
 import org.openrdf.query.algebra.TupleExpr;
 import org.openrdf.query.algebra.evaluation.QueryBindingSet;
 import org.openrdf.sail.NotifyingSail;
 import org.openrdf.sail.Sail;
 import org.openrdf.sail.SailChangedListener;
 import org.openrdf.sail.SailConnection;
 import org.openrdf.sail.SailException;
 import org.openrdf.sail.helpers.SailWrapper;
 import org.openrdf.sail.inferencer.InferencerConnection;
 import org.openrdf.sail.optimistic.exceptions.ConcurrencyException;
 import org.openrdf.sail.optimistic.helpers.DeltaMerger;
 import org.openrdf.sail.optimistic.helpers.EvaluateOperation;
 
 /**
  * When used with the {@link OptimisticRepository}, all transactions have
  * serializable isolation.
  * 
  * @author James Leigh
  * 
  */
 public class OptimisticSail extends SailWrapper implements NotifyingSail {
 
 	private static final String DELTA_VARNAME = "-delta-";
 	private ReadWriteLockManager preparing = new WritePrefReadWriteLockManager();
 	private ReadWriteLockManager locker = new WritePrefReadWriteLockManager();
 	private Map<OptimisticConnection, Lock> transactions = new HashMap<OptimisticConnection, Lock>();
 	private volatile Lock preparedLock;
 	private volatile OptimisticConnection prepared;
 	private volatile boolean listenersIsEmpty = true;
 	private Set<SailChangedListener> listeners = new HashSet<SailChangedListener>();
 
 	public OptimisticSail() {
 		super();
 	}
 
 	public OptimisticSail(Sail baseSail) {
 		super(baseSail);
 	}
 
 	public void addSailChangedListener(SailChangedListener listener) {
 		synchronized (listeners) {
 			listeners.add(listener);
 			listenersIsEmpty = false;
 		}
 	}
 
 	public void removeSailChangedListener(SailChangedListener listener) {
 		synchronized (listeners) {
 			listeners.remove(listener);
 		}
 	}
 
 	@Override
 	public OptimisticConnection getConnection() throws SailException {
 		SailConnection con = super.getConnection();
 		if (con instanceof InferencerConnection) {
 			return new OptimisticInferencerConnection(this,
 					(InferencerConnection) con);
 		} else {
 			return new OptimisticConnection(this, con);
 		}
 	}
 
 	Set<SailChangedListener> getListeners() {
 		if (listenersIsEmpty)
 			return Collections.emptySet();
 		synchronized (listeners) {
 			return new HashSet<SailChangedListener>(listeners);
 		}
 	}
 
 	void begin(OptimisticConnection con) throws InterruptedException {
 		Lock lock = locker.getReadLock();
 		synchronized (this) {
 			transactions.put(con, lock);
 		}
 	}
 
 	Lock getReadLock() throws SailException {
 		try {
 			return preparing.getReadLock();
 		} catch (InterruptedException e) {
 			throw new SailException(e);
 		}
 	}
 
 	synchronized void prepare(OptimisticConnection prepared)
 			throws InterruptedException, SailException {
 		assert transactions.containsKey(prepared);
 		preparedLock = preparing.getWriteLock();
 		this.prepared = prepared;
 		synchronized (prepared) {
 			Model added = prepared.getAddedModel();
 			Model removed = prepared.getRemovedModel();
 			SailConnection sail = null;
 			try {
 				for (OptimisticConnection con : transactions.keySet()) {
 					if (con == prepared)
 						continue;
 					synchronized (con) {
 						for (EvaluateOperation op : con.getReadOperations()) {
 							if (sail == null) {
 								sail = super.getConnection();
 							}
 							if (!added.isEmpty() && effects(added, op, sail)) {
 								con.setConclict(new ConcurrencyException(op.toString()));
 								break;
 							}
 							if (!removed.isEmpty()
 									&& effects(removed, op, sail)) {
 								con.setConclict(new ConcurrencyException(op.toString()));
 								break;
 							}
 						}
 					}
 				}
 			} finally {
 				if (sail != null) {
 					sail.close();
 				}
 			}
 			prepared.getReadOperations().clear();
 		}
 	}
 
 	synchronized void end(OptimisticConnection con) {
 		Lock lock = transactions.get(con);
 		if (lock == null)
 			return; // no active transaction
 		try {
 			transactions.remove(con);
 			if (prepared == con) {
 				preparedLock.release();
 			}
 			con.setConclict(null);
 		} finally {
 			lock.release();
 		}
 	}
 
 	boolean exclusive(OptimisticConnection con) {
 		Lock lock = locker.tryWriteLock();
 		if (lock != null) {
 			synchronized (this) {
 				end(con);
 				transactions.put(con, lock);
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Modify the query to include the delta with an extra binding and see if
 	 * the extra binding comes out the other side.
 	 */
 	private boolean effects(Model delta, EvaluateOperation op,
 			SailConnection sail) throws SailException {
 		TupleExpr query = new QueryRoot(op.getTupleExpr().clone());
 		BindingSet bindings = op.getBindingSet();
 		boolean inf = op.isIncludeInferred();
 
 		ValueFactory vf = getValueFactory();
 		QueryBindingSet additional = new QueryBindingSet();
 		additional.addBinding(DELTA_VARNAME, vf.createLiteral(true));
 		DeltaMerger merger = new DeltaMerger(delta, additional);
 
 		merger.optimize(query, op.getDataset(), bindings);
 		if (!merger.isModified())
 			return false;
 
 		CloseableIteration<? extends BindingSet, QueryEvaluationException> result;
 		result = sail.evaluate(query, op.getDataset(), bindings, inf);
 		try {
 			try {
 				while (result.hasNext()) {
 					if (result.next().hasBinding(DELTA_VARNAME))
 						return true;
 				}
 				return false;
 			} finally {
 				result.close();
 			}
 		} catch (QueryEvaluationException e) {
 			throw new SailException(e);
 		}
 	}
 }
