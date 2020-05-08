 package org.ogreg.ase4j.file;
 
 import java.io.Closeable;
 import java.io.File;
 import java.io.Flushable;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.annotation.PostConstruct;
 import javax.annotation.PreDestroy;
 
 import org.ogreg.ase4j.Association;
 import org.ogreg.ase4j.AssociationStoreException;
 import org.ogreg.ase4j.AssociationStoreMetadata;
 import org.ogreg.ase4j.ConfigurableAssociationStore;
 import org.ogreg.ase4j.Params;
 import org.ogreg.ase4j.criteria.Query;
 import org.ogreg.ase4j.criteria.QueryExecutionException;
 import org.ogreg.ostore.ObjectStore;
 import org.ogreg.ostore.ObjectStoreException;
 
 /**
  * A file-based implementation of the association store.
  * 
  * @param <F> The association 'from' type
  * @param <T> The association 'to' type
  * @author Gergely Kiss
  */
 public class FileAssociationStoreImpl<F, T> implements ConfigurableAssociationStore<F, T>,
 		FileAssociationStoreImplMBean, Closeable, Flushable {
 
 	/** The index of the from entities. */
 	private ObjectStore<F> fromStore;
 
 	/** The object store of the to entities. */
 	private ObjectStore<T> toStore;
 
 	/** The file used to store the associations. */
 	private File storageFile;
 
 	private CachedBlockStore assocs = new CachedBlockStore();
 	private FileAssociationSolver solver = new FileAssociationSolver(this);
 
 	/** Storage metadata. */
 	private AssociationStoreMetadata metadata;
 
 	@Override
 	public void init(ObjectStore<F> from, ObjectStore<T> to, File storageFile) {
 		setFromStore(from);
 		setToStore(to);
 		setStorageFile(storageFile);
 
 		init();
 	}
 
 	/**
 	 * Initializes the storage using the given parameters.
 	 * 
 	 * @throws IOException
 	 */
 	@PostConstruct
 	public void init() {
 
 		try {
 
 			if (fromStore == null) {
				throw new AssertionError("The field fromStore must be set");
 			} else if (toStore == null) {
 				throw new AssertionError("The field toStore must be set");
 			}
 
 			// Closing store if already opened
 			close();
 
 			// Opening store at the specified file
 			assocs.open(storageFile);
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	@Override
 	public void add(F from, T to, float value, Params params) throws AssociationStoreException {
 
 		try {
 			Operation op = Params.ensureNotNull(params).op;
 
 			Long fi = fromStore.save(from);
 			Long ti = toStore.save(to);
 
 			AssociationBlock a = new AssociationBlock(fi.intValue());
 			a.merge(ti.intValue(), value, op);
 
 			assocs.merge(a, op);
 		} catch (IOException e) {
 			throw new AssociationStoreException(e);
 		} catch (ObjectStoreException e) {
 			throw new AssociationStoreException(e);
 		}
 	}
 
 	@Override
 	public void addAll(Collection<Association<F, T>> froms, T to, Params params)
 			throws AssociationStoreException {
 
 		try {
 			Operation op = Params.ensureNotNull(params).op;
 			Long ti = toStore.save(to);
 
 			for (Association<F, ?> assoc : froms) {
 				F from = assoc.from;
 				Long fi = fromStore.save(from);
 
 				AssociationBlock a = new AssociationBlock(fi.intValue());
 				a.merge(ti.intValue(), assoc.value, op);
 
 				this.assocs.merge(a, op);
 			}
 		} catch (IOException e) {
 			throw new AssociationStoreException(e);
 		} catch (ObjectStoreException e) {
 			throw new AssociationStoreException(e);
 		}
 	}
 
 	@Override
 	public void addAll(Collection<Association<F, T>> assocs, Params params)
 			throws AssociationStoreException {
 		Map<F, List<Association<F, T>>> byFrom = new HashMap<F, List<Association<F, T>>>(
 				assocs.size() / 2);
 
 		for (Association<F, T> a : assocs) {
 			List<Association<F, T>> l = byFrom.get(a.from);
 
 			if (l == null) {
 				l = new LinkedList<Association<F, T>>();
 				byFrom.put(a.from, l);
 			}
 
 			l.add(a);
 		}
 
 		try {
 			Operation op = Params.ensureNotNull(params).op;
 
 			for (Entry<F, List<Association<F, T>>> e : byFrom.entrySet()) {
 				F from = e.getKey();
 				Long fi = fromStore.save(from);
 
 				AssociationBlock a = new AssociationBlock(fi.intValue());
 
 				for (Association<F, T> assoc : e.getValue()) {
 					Long ti = toStore.save(assoc.to);
 
 					// TODO This could be more effective
 					a.merge(ti.intValue(), assoc.value, op);
 				}
 
 				this.assocs.merge(a, op);
 			}
 		} catch (IOException e) {
 			throw new AssociationStoreException(e);
 		} catch (ObjectStoreException e) {
 			throw new AssociationStoreException(e);
 		}
 	}
 
 	@Override
 	public List<Association<F, T>> query(Query query) throws QueryExecutionException {
 		List<Association<F, T>> ret = new ArrayList<Association<F, T>>(query.limit());
 
 		// Solving the query
 		AssociationResultBlock results = solver.solve(query);
 
 		try {
 			int[] tos = results.tos;
 			float[] values = results.values;
 
 			for (int i = 0; i < results.size; i++) {
 				T to = toStore.get(tos[i]);
 				float value = values[i];
 
 				ret.add(new Association<F, T>(null, to, value));
 			}
 		} catch (ObjectStoreException e) {
 			throw new QueryExecutionException(e);
 		}
 
 		return ret;
 	}
 
 	AssociationBlock getAssociation(int from) throws IOException {
 		return assocs.get(from);
 	}
 
 	@Override
 	public synchronized void flush() throws IOException {
 		assocs.flush();
 	}
 
 	@Override
 	@PreDestroy
 	public synchronized void close() throws IOException {
 		assocs.close();
 	}
 
 	@Override
 	protected void finalize() throws Throwable {
 		close();
 	}
 
 	public ObjectStore<F> getFromStore() {
 		return fromStore;
 	}
 
 	public void setFromStore(ObjectStore<F> fromStore) {
 		this.fromStore = fromStore;
 	}
 
 	public ObjectStore<T> getToStore() {
 		return toStore;
 	}
 
 	public void setToStore(ObjectStore<T> toStore) {
 		this.toStore = toStore;
 	}
 
 	public File getStorageFile() {
 		return storageFile;
 	}
 
 	public void setStorageFile(File storageFile) {
 		this.storageFile = storageFile;
 	}
 
 	@Override
 	public AssociationStoreMetadata getMetadata() {
 		return metadata;
 	}
 
 	public void setMetadata(AssociationStoreMetadata metadata) {
 		this.metadata = metadata;
 	}
 
 	@Override
 	public long getBlockCount() {
 		return assocs.getSize();
 	}
 
 	@Override
 	public double getBlockUsage() {
 		long size = getAssociationCount();
 
 		if (size == 0) {
 			return 0;
 		}
 
 		// Assoc bytes = file length - 4 Magic bytes - 4 size bytes - index cap
 		long assocBytes = storageFile.length() - 4 - 4 - assocs.getCapacity();
 
 		// ...minus AssociationBlock overhead: 4 capacity + 4 size + 4 from
 		assocBytes -= getBlockCount() * (4 + 4 + 4);
 
 		// ...divided by the stored association bytes (4 to + 4 value)
 		return (double) assocBytes / (size * (4 + 4));
 	}
 
 	@Override
 	public long getAssociationCount() {
 		return assocs.getAssociationCount();
 	}
 
 	@Override
 	public long getCachedBlockCount() {
 		return assocs.getCache().size();
 	}
 
 	@Override
 	public long getCachedAssociationCount() {
 		return assocs.getCache().getAssociationCount();
 	}
 }
