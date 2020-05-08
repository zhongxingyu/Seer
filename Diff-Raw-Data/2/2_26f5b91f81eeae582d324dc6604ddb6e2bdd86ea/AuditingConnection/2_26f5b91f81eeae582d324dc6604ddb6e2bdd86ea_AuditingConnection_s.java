 /*
  * Copyright (c) 2009-2010, James Leigh All rights reserved.
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
 package org.openrdf.sail.auditing;
 
 import static java.util.Arrays.asList;
 import static org.openrdf.sail.auditing.vocabulary.Audit.COMMITTED_ON;
 import static org.openrdf.sail.auditing.vocabulary.Audit.CONTAINED;
 import static org.openrdf.sail.auditing.vocabulary.Audit.CURRENT_TRX;
 import static org.openrdf.sail.auditing.vocabulary.Audit.MODIFIED;
 import static org.openrdf.sail.auditing.vocabulary.Audit.REVISION;
 import static org.openrdf.sail.auditing.vocabulary.Audit.TRANSACTION;
 import info.aduna.iteration.CloseableIteration;
 
 import java.util.ArrayList;
 import java.util.GregorianCalendar;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.xml.datatype.DatatypeConfigurationException;
 import javax.xml.datatype.DatatypeFactory;
 import javax.xml.datatype.XMLGregorianCalendar;
 
 import org.openrdf.model.BNode;
 import org.openrdf.model.Literal;
 import org.openrdf.model.Resource;
 import org.openrdf.model.Statement;
 import org.openrdf.model.URI;
 import org.openrdf.model.Value;
 import org.openrdf.model.ValueFactory;
 import org.openrdf.model.vocabulary.RDF;
 import org.openrdf.sail.SailConnection;
 import org.openrdf.sail.SailException;
 import org.openrdf.sail.helpers.SailConnectionWrapper;
 
 /**
  * Intercepts the add and remove operations and add a revision to each resource.
  */
 public class AuditingConnection extends SailConnectionWrapper {
 	private AuditingSail sail;
 	private URI trx;
 	private DatatypeFactory factory;
 	private ValueFactory vf;
 	private Set<Resource> revised = new HashSet<Resource>();
 	private Set<Resource> modified = new HashSet<Resource>();
 	private List<List> metadata = new ArrayList<List>();
 	private URI currentTrx;
 
 	public AuditingConnection(AuditingSail sail, SailConnection wrappedCon)
 			throws DatatypeConfigurationException {
 		super(wrappedCon);
 		this.sail = sail;
 		factory = DatatypeFactory.newInstance();
 		vf = sail.getValueFactory();
 		currentTrx = vf.createURI(CURRENT_TRX.stringValue());
 	}
 
 	@Override
 	public synchronized void addStatement(Resource subj, URI pred, Value obj,
 			Resource... contexts) throws SailException {
 		if (subj.equals(currentTrx) || obj.equals(currentTrx)) {
 			addMetadata(subj, pred, obj, contexts);
 		} else {
 			storeStatement(subj, pred, obj, contexts);
 		}
 	}
 
 	@Override
 	public synchronized void removeStatements(Resource subj, URI pred,
 			Value obj, Resource... contexts) throws SailException {
 		if (sail.isArchiving()) {
 			CloseableIteration<? extends Statement, SailException> stmts;
 			stmts = super.getStatements(subj, pred, obj, false, contexts);
 			try {
 				while (stmts.hasNext()) {
 					Statement st = stmts.next();
 					Resource s = st.getSubject();
 					URI p = st.getPredicate();
 					Value o = st.getObject();
 					Resource ctx = st.getContext();
 					if (s instanceof URI && revised.add(s)
 							&& !p.equals(REVISION)) {
 						super.removeStatements(subj, REVISION, null);
 						super.addStatement(s, REVISION, getTrx(), getTrx());
 					} else if (trx != null && s instanceof URI
 							 && p.equals(REVISION)) {
 						super.removeStatements(s, REVISION, trx, trx);
 					}
 					if (!ctx.equals(trx) && ctx instanceof URI) {
 						if (modified.add(ctx)) {
							super.addStatement(trx, MODIFIED, ctx, getTrx());
 						}
 						BNode node = vf.createBNode();
 						super.addStatement(ctx, CONTAINED, node, getTrx());
 						super.addStatement(node, RDF.SUBJECT, s, getTrx());
 						super.addStatement(node, RDF.PREDICATE, p, getTrx());
 						super.addStatement(node, RDF.OBJECT, o, getTrx());
 					}
 				}
 			} finally {
 				stmts.close();
 			}
 			super.removeStatements(subj, pred, obj, contexts);
 		} else {
 			super.removeStatements(subj, pred, obj, contexts);
 			if (subj instanceof URI && pred != null && revised.add(subj)) {
 				super.removeStatements(subj, REVISION, null);
 				if (!pred.equals(REVISION)) {
 					super.addStatement(subj, REVISION, getTrx(), getTrx());
 				}
 			} else if (subj instanceof URI && trx != null
 					&& REVISION.equals(pred)) {
 				super.removeStatements(subj, REVISION, trx, trx);
 			}
 			if (contexts != null && contexts.length == 1 && contexts[0] != null
 					&& modified.add(contexts[0])) {
 				addMetadata(currentTrx, MODIFIED, contexts[0], currentTrx);
 			} else if (contexts != null && contexts.length > 0) {
 				for (Resource ctx : contexts) {
 					if (ctx != null && modified.add(ctx)) {
 						addMetadata(currentTrx, MODIFIED, ctx, currentTrx);
 					}
 				}
 			}
 		}
 	}
 
 	@Override
 	public synchronized void commit() throws SailException {
 		if (trx != null) {
 			GregorianCalendar cal = new GregorianCalendar();
 			XMLGregorianCalendar xgc = factory.newXMLGregorianCalendar(cal);
 			Literal now = vf.createLiteral(xgc);
 			super.addStatement(trx, RDF.TYPE, TRANSACTION, trx);
 			super.addStatement(trx, COMMITTED_ON, now, trx);
 			trx = null;
 			metadata.clear();
 			revised.clear();
 			modified.clear();
 		}
 		super.commit();
 	}
 
 	@Override
 	public synchronized void rollback() throws SailException {
 		trx = null;
 		metadata.clear();
 		revised.clear();
 		modified.clear();
 		super.rollback();
 	}
 
 	private URI getTrx() throws SailException {
 		if (trx == null) {
 			trx = sail.nextTransaction();
 			synchronized (metadata) {
 				for (List<?> st : metadata) {
 					assert st.size() == 4;
 					storeStatement((Resource) st.get(0), (URI) st.get(1),
 							(Value) st.get(2), (Resource[]) st.get(3));
 				}
 				metadata.clear();
 			}
 		}
 		return trx;
 	}
 
 	private void addMetadata(Resource subj, URI pred, Value obj,
 			Resource... contexts) throws SailException {
 		if (trx == null) {
 			synchronized (metadata) {
 				metadata.add(asList(subj, pred, obj, contexts));
 			}
 		} else {
 			storeStatement(subj, pred, obj, contexts);
 		}
 	}
 
 	private void storeStatement(Resource subj, URI pred, Value obj,
 			Resource... contexts) throws SailException {
 		if (subj.equals(currentTrx)) {
 			subj = getTrx();
 		}
 		if (obj.equals(currentTrx)) {
 			obj = getTrx();
 		}
 		if (contexts != null && contexts.length == 1) {
 			if (currentTrx.equals(contexts[0])) {
 				contexts[0] = getTrx();
 			}
 		} else if (contexts != null) {
 			for (int i = 0; i < contexts.length; i++) {
 				if (currentTrx.equals(contexts[i])) {
 					contexts[i] = getTrx();
 				}
 			}
 		}
 		if (subj instanceof URI && revised.add(subj) && !subj.equals(trx)) {
 			super.removeStatements(subj, REVISION, null);
 			super.addStatement(subj, REVISION, getTrx(), getTrx());
 		}
 		if (contexts == null || contexts.length == 0 || contexts.length == 1
 				&& contexts[0] == null) {
 			super.addStatement(subj, pred, obj, getTrx());
 		} else if (contexts.length == 1) {
 			super.addStatement(subj, pred, obj, contexts);
 			Resource ctx = contexts[0];
 			if (ctx instanceof URI && !ctx.equals(trx) && modified.add(ctx)) {
 				super.addStatement(getTrx(), MODIFIED, ctx, getTrx());
 			}
 		} else {
 			super.addStatement(subj, pred, obj, contexts);
 			for (Resource ctx : contexts) {
 				if (ctx instanceof URI && !ctx.equals(trx) && modified.add(ctx)) {
 					super.addStatement(getTrx(), MODIFIED, ctx, getTrx());
 				}
 			}
 		}
 	}
 }
