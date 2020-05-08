 package de.rwglab.p2pts;
 
 import com.google.common.util.concurrent.ListenableFuture;
 import com.google.common.util.concurrent.SettableFuture;
 import com.google.inject.Inject;
 import de.rwglab.p2pts.util.InjectLogger;
 import org.slf4j.Logger;
 
 import java.io.ByteArrayInputStream;
 import java.nio.ByteBuffer;
 import java.util.Set;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.Executor;
 import java.util.concurrent.Future;
 import java.util.concurrent.TimeUnit;
 
 import static com.google.common.base.Preconditions.checkArgument;
 import static com.google.common.collect.Sets.newHashSet;
 
 public class TripleStoreImpl implements TripleStore {
 
 	@InjectLogger
 	private Logger log;
 
 	private final SetMapAsync<String, String> setMapAsync;
 
 	private final Executor executor;
 
 	@Inject
 	public TripleStoreImpl(final SetMapAsync<String, String> setMapAsync, final Executor executor) {
 		this.setMapAsync = setMapAsync;
 		this.executor = executor;
 	}
 
 	@Override
 	public ListenableFuture<Void> insert(Triple triple) {
 
 		checkArgument(triple.subject != null && triple.predicate != null && triple.object != null);
 
 		final SettableFuture<Void> future = SettableFuture.create();
 
 		final Set<Future> operationFutures = newHashSet();
 
 		final ListenableFuture<Void> spFuture = setMapAsync.put(getSPKey(triple), triple.object, 10, TimeUnit.SECONDS);
 		final ListenableFuture<Void> poFuture = setMapAsync.put(getPOKey(triple), triple.subject, 10, TimeUnit.SECONDS);
 		final ListenableFuture<Void> soFuture =
 				setMapAsync.put(getSOKey(triple), triple.predicate, 10, TimeUnit.SECONDS);
 
 		operationFutures.add(spFuture);
 		operationFutures.add(poFuture);
 		operationFutures.add(soFuture);
 
 		spFuture.addListener(new Runnable() {
 			@Override
 			public void run() {
 				synchronized (operationFutures) {
 					operationFutures.remove(spFuture);
 					if (operationFutures.isEmpty()) {
 						future.set(null);
 					}
 				}
 			}
 		}, executor
 		);
 
 		poFuture.addListener(new Runnable() {
 			@Override
 			public void run() {
 				synchronized (operationFutures) {
 					operationFutures.remove(poFuture);
 					if (operationFutures.isEmpty()) {
 						future.set(null);
 					}
 				}
 			}
 		}, executor
 		);
 
 		soFuture.addListener(new Runnable() {
 			@Override
 			public void run() {
 				synchronized (operationFutures) {
 					operationFutures.remove(soFuture);
 					if (operationFutures.isEmpty()) {
 						future.set(null);
 					}
 				}
 			}
 		}, executor
 		);
 
 		return future;
 	}
 
 	@Override
 	public ListenableFuture<Set<Triple>> get(final Triple triple) {
 
 		checkArgument(triple.subject == null || triple.predicate == null || triple.object == null);
 
 		if (triple.subject == null) {
 
 			final SettableFuture<Set<Triple>> future = SettableFuture.create();
 			final ListenableFuture<Set<String>> subjectsFuture = setMapAsync.get(getPOKey(triple), 10, TimeUnit.SECONDS);
 			subjectsFuture.addListener(new Runnable() {
 				@Override
 				public void run() {
 					try {
 
 						Set<Triple> results = newHashSet();
						for (String subject : subjectsFuture.get()) {
							results.add(new Triple(subject, triple.predicate, triple.object));
 						}
 
 						future.set(results);
 
 					} catch (InterruptedException e) {
 						log.error("", e);
 					} catch (ExecutionException e) {
 						log.error("", e);
 					}
 				}
 			}, executor
 			);
 
 			return future;
 
 		} else if (triple.predicate == null) {
 
 			final SettableFuture<Set<Triple>> future = SettableFuture.create();
 			final ListenableFuture<Set<String>> predicatesFuture = setMapAsync.get(getSOKey(triple), 10, TimeUnit.SECONDS);
 			predicatesFuture.addListener(new Runnable() {
 				@Override
 				public void run() {
 					try {
 
 						Set<Triple> results = newHashSet();
						for (String predicate : predicatesFuture.get()) {
							results.add(new Triple(triple.subject, predicate, triple.object));
 						}
 
 						future.set(results);
 
 					} catch (InterruptedException e) {
 						log.error("", e);
 					} catch (ExecutionException e) {
 						log.error("", e);
 					}
 				}
 			}, executor
 			);
 
 			return future;
 
 		} else {
 
 			final SettableFuture<Set<Triple>> future = SettableFuture.create();
 			final ListenableFuture<Set<String>> objectsFuture = setMapAsync.get(getSPKey(triple), 10, TimeUnit.SECONDS);
 			objectsFuture.addListener(new Runnable() {
 				@Override
 				public void run() {
 					try {
 
 						Set<Triple> results = newHashSet();
 
 						Set<String> searchResult = objectsFuture.get();
 						if (searchResult != null) {
 							for (String object : searchResult) {
 								results.add(new Triple(triple.subject, triple.predicate, object));
 							}
 						}
 
 						future.set(results);
 
 					} catch (InterruptedException e) {
 						log.error("", e);
 					} catch (ExecutionException e) {
 						log.error("", e);
 					}
 				}
 			}, executor
 			);
 
 			return future;
 		}
 	}
 
 	@Override
 	public ListenableFuture<Void> update(Triple oldTriple, Triple newTriple) {
 
 		checkArgument(!oldTriple.equals(newTriple));
 
 		final SettableFuture<Void> future = SettableFuture.create();
 
 		final ListenableFuture<Void> deleteFuture = delete(oldTriple);
 		final ListenableFuture<Void> insertFuture = insert(newTriple);
 
 		deleteFuture.addListener(new Runnable() {
 			@Override
 			public void run() {
 				synchronized (future) {
 					if (insertFuture.isDone()) {
 						future.set(null);
 					}
 				}
 			}
 		}, executor
 		);
 
 		insertFuture.addListener(new Runnable() {
 			@Override
 			public void run() {
 				synchronized (future) {
 					if (deleteFuture.isDone()) {
 						future.set(null);
 					}
 				}
 			}
 		}, executor
 		);
 
 		return future;
 	}
 
 	@Override
 	public ListenableFuture<Void> delete(Triple triple) {
 
 		checkArgument(triple.subject != null && triple.predicate != null && triple.object != null);
 
 		final SettableFuture<Void> future = SettableFuture.create();
 
 		final Set<Future> operationFutures = newHashSet();
 
 		final ListenableFuture<Void> spFuture =
 				setMapAsync.remove(getSPKey(triple), triple.object, 10, TimeUnit.SECONDS);
 		final ListenableFuture<Void> poFuture =
 				setMapAsync.remove(getPOKey(triple), triple.subject, 10, TimeUnit.SECONDS);
 		final ListenableFuture<Void> soFuture =
 				setMapAsync.remove(getSOKey(triple), triple.predicate, 10, TimeUnit.SECONDS);
 
 		spFuture.addListener(new Runnable() {
 			@Override
 			public void run() {
 				synchronized (operationFutures) {
 					operationFutures.remove(spFuture);
 					if (operationFutures.isEmpty()) {
 						future.set(null);
 					}
 				}
 			}
 		}, executor
 		);
 
 		poFuture.addListener(new Runnable() {
 			@Override
 			public void run() {
 				synchronized (operationFutures) {
 					operationFutures.remove(poFuture);
 					if (operationFutures.isEmpty()) {
 						future.set(null);
 					}
 				}
 			}
 		}, executor
 		);
 
 		soFuture.addListener(new Runnable() {
 			@Override
 			public void run() {
 				synchronized (operationFutures) {
 					operationFutures.remove(soFuture);
 					if (operationFutures.isEmpty()) {
 						future.set(null);
 					}
 				}
 			}
 		}, executor
 		);
 
 		return future;
 	}
 
 	private String getSPKey(final Triple triple) {
 		byte[] subjectBytes = triple.subject.getBytes();
 		byte[] predicateBytes = triple.predicate.getBytes();
 		ByteBuffer b = ByteBuffer.allocate(subjectBytes.length + 1 + predicateBytes.length);
 		b.put(subjectBytes);
 		b.put((byte) 0);
 		b.put(predicateBytes);
 		return new String(b.array());
 	}
 
 	private String getPOKey(final Triple triple) {
 		byte[] predicateBytes = triple.predicate.getBytes();
 		byte[] objectBytes = triple.object.getBytes();
 		ByteBuffer b = ByteBuffer.allocate(predicateBytes.length + 1 + objectBytes.length);
 		b.put(predicateBytes);
 		b.put((byte) 0);
 		b.put(objectBytes);
 		return new String(b.array());
 	}
 
 	private String getSOKey(final Triple triple) {
 		byte[] subjectBytes = triple.subject.getBytes();
 		byte[] objectBytes = triple.object.getBytes();
 		ByteBuffer b = ByteBuffer.allocate(subjectBytes.length + 1 + objectBytes.length);
 		b.put(subjectBytes);
 		b.put((byte) 0);
 		b.put(objectBytes);
 		return new String(b.array());
 	}
 
 }
