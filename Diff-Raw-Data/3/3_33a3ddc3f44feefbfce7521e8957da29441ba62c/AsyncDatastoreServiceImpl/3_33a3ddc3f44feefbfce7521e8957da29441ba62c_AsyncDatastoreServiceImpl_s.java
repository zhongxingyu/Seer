 package balmysundaycandy.extention.datastore.impl;
 
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.Future;
 
 import balmysundaycandy.extention.datastore.AsyncDatastoreService;
 import balmysundaycandy.extention.datastore.EnittyMapFuture;
 import balmysundaycandy.extention.datastore.EntityFuture;
 import balmysundaycandy.extention.datastore.KeyFuture;
 import balmysundaycandy.extention.datastore.KeyListFuture;
 import balmysundaycandy.extention.datastore.KeyRangeFuture;
 import balmysundaycandy.extention.datastore.TransactionFuture;
 import balmysundaycandy.extention.datastore.DeleteResponseFuture;
 import balmysundaycandy.more.low.level.operations.datastore.DatastoreOperations;
 
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.DatastoreServiceFactory;
 import com.google.appengine.api.datastore.DeleteRequestTranslator;
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.EntityNotFoundException;
 import com.google.appengine.api.datastore.GetRequestTransralator;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.KeyRange;
 import com.google.appengine.api.datastore.PreparedQuery;
 import com.google.appengine.api.datastore.PutRequestTranslator;
 import com.google.appengine.api.datastore.Query;
 import com.google.appengine.api.datastore.ReferenceTranslator;
 import com.google.appengine.api.datastore.Transaction;
 import com.google.apphosting.api.ApiProxy;
 import com.google.apphosting.api.ApiBasePb.VoidProto;
 import com.google.apphosting.api.ApiProxy.ApiConfig;
 import com.google.apphosting.api.DatastorePb.AllocateIdsRequest;
 import com.google.apphosting.api.DatastorePb.BeginTransactionRequest;
 
 /**
  * "async" datastore service
  * 
  * @author marblejenka
  * 
  */
 public class AsyncDatastoreServiceImpl implements AsyncDatastoreService {
 
 	/**
	 * low level api datastore service for use transaction stack.
 	 */
 	private static final DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
 
 	public static final ApiConfig apiConfig = new ApiConfig();
 	static {
 		apiConfig.setDeadlineInSeconds(Double.MAX_VALUE);
 	}
 
 	@Override
 	public Future<Entity> get(Key key) throws EntityNotFoundException {
 		// TODO トランザクションの取り扱いポリシーを決める。パッケージを変えてllapiのスタックを使うのがいいけど、非同期にはならない
 		Transaction transaction = datastoreService.beginTransaction();
 		Future<Entity> future = get(transaction, key);
 		transaction.commit();
 		return future;
 	}
 
 	@Override
 	public Future<Entity> get(Transaction txn, Key key) throws EntityNotFoundException {
 		return new EntityFuture(DatastoreOperations.GET.callAsync(GetRequestTransralator.request2pb(txn, key), apiConfig));
 	}
 
 	@Override
 	public Future<Map<Key, Entity>> get(Iterable<Key> keys) {
 		// TODO トランザクションの取り扱いポリシーを決める。パッケージを変えてllapiのスタックを使うのがいいけど、非同期にはならない
 		Transaction transaction = datastoreService.beginTransaction();
 		Future<Map<Key, Entity>> future = get(transaction, keys);
 		transaction.commit();
 		return future;
 	}
 
 	@Override
 	public Future<Map<Key, Entity>> get(Transaction txn, Iterable<Key> keys) {
 		return new EnittyMapFuture(DatastoreOperations.GET.callAsync(GetRequestTransralator.request2pb(txn, keys), apiConfig));
 	}
 
 	@Override
 	public Future<Key> put(Entity entity) {
 		// TODO トランザクションの取り扱いポリシーを決める。パッケージを変えてllapiのスタックを使うのがいいけど、非同期にはならない
 		Transaction transaction = datastoreService.beginTransaction();
 		Future<Key> future = put(transaction, entity);
 		transaction.commit();
 		return future;
 	}
 
 	@Override
 	public Future<Key> put(Transaction transaction, Entity entity) {
 		return new KeyFuture(DatastoreOperations.PUT.callAsync(PutRequestTranslator.request2bp(transaction, entity), apiConfig));
 	}
 
 	@Override
 	public Future<List<Key>> put(Iterable<Entity> entities) {
 		// TODO トランザクションの取り扱いポリシーを決める。パッケージを変えてllapiのスタックを使うのがいいけど、非同期にはならない
 		Transaction transaction = datastoreService.beginTransaction();
 		Future<List<Key>> future = put(transaction, entities);
 		transaction.commit();
 		return future;
 	}
 
 	@Override
 	public Future<List<Key>> put(Transaction transaction, Iterable<Entity> entities) {
 		return new KeyListFuture(DatastoreOperations.PUT.callAsync(PutRequestTranslator.request2bp(transaction, entities), apiConfig));
 	}
 
 	@Override
 	public Future<VoidProto> delete(Key... keys) {
 		// TODO トランザクションの取り扱いポリシーを決める。パッケージを変えてllapiのスタックを使うのがいいけど、非同期にはならない
 		Transaction transaction = datastoreService.beginTransaction();
 		Future<VoidProto> future = delete(transaction, keys);
 		transaction.commit();
 		return future;
 	}
 
 	@Override
 	public Future<VoidProto> delete(Transaction transaction, Key... keys) {
 		return new DeleteResponseFuture(DatastoreOperations.DELETE.callAsync(DeleteRequestTranslator.keys2request(transaction, keys), apiConfig));
 	}
 
 	@Override
 	public Future<VoidProto> delete(Iterable<Key> keys) {
 		// TODO トランザクションの取り扱いポリシーを決める。パッケージを変えてllapiのスタックを使うのがいいけど、非同期にはならない
 		Transaction transaction = datastoreService.beginTransaction();
 		Future<VoidProto> future = delete(transaction, keys);
 		transaction.commit();
 		return future;
 	}
 
 	@Override
 	public Future<VoidProto> delete(Transaction transaction, Iterable<Key> keys) {
 		return new DeleteResponseFuture(DatastoreOperations.DELETE.callAsync(DeleteRequestTranslator.keys2request(transaction, keys), apiConfig));
 	}
 
 	@Override
 	public Future<PreparedQuery> prepare(Query query) {
 		// TODO 実装方針を決める
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public Future<PreparedQuery> prepare(Transaction txn, Query query) {
 		// TODO 実装方針を決める
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public Future<Transaction> beginTransaction() {
 		BeginTransactionRequest request = new BeginTransactionRequest();
 		request.setApp(ApiProxy.getCurrentEnvironment().getAppId());
 		return new TransactionFuture(DatastoreOperations.BEGIN_TRANSACTION.callAsync(request, apiConfig));
 	}
 
 	@Override
 	public Future<Transaction> getCurrentTransaction() {
 		// TODO 実装方針を決める
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public Future<Transaction> getCurrentTransaction(Transaction returnedIfNoTxn) {
 		// TODO 実装方針を決める
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public Future<Collection<Transaction>> getActiveTransactions() {
 		// TODO 実装方針を決める
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public Future<KeyRange> allocateIds(String kind, long num) {
 		AllocateIdsRequest request = new AllocateIdsRequest();
 		request.setSize(num);
 		request.setModelKey(ReferenceTranslator.kind2reference(kind));
 		return new KeyRangeFuture(null, kind, DatastoreOperations.ALLOCATE_IDS.callAsync(request, apiConfig));
 	}
 
 	@Override
 	public Future<KeyRange> allocateIds(Key parent, String kind, long num) {
 		AllocateIdsRequest request = new AllocateIdsRequest();
 		request.setSize(num);
 		request.setModelKey(ReferenceTranslator.path2reference(parent, kind));
 		return new KeyRangeFuture(parent, kind, DatastoreOperations.ALLOCATE_IDS.callAsync(request, apiConfig));
 	}
 }
