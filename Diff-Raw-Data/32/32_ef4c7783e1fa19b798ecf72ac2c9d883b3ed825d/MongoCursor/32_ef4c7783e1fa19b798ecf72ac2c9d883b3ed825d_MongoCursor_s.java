 package jp.ameba.mongo;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import jp.ameba.mongo.protocol.Delete;
 import jp.ameba.mongo.protocol.GetMore;
 import jp.ameba.mongo.protocol.KillCursors;
 import jp.ameba.mongo.protocol.Query;
 import jp.ameba.mongo.protocol.Response;
 
 import org.bson.BSONObject;
 import org.bson.BasicBSONObject;
 
 /**
  * カーソル処理を行ないます。
  * カーソルそのものはスレッドセーフではないため、
  * 各スレッドは別個にクエリを作成し、取得する必要があります。
  * 
  * @author suguru
  */
 public class MongoCursor implements Iterable<BSONObject>, Iterator<BSONObject> {
 	
 	// MongoClient
 	private MongoConnection conn;
 	
 	// 対象のデータベース名
 	private String databaseName;
 	
 	// 対象のコレクション名
 	private String collectionName;
 	
 	// 検索に利用したクエリオブジェクト
 	private Query query;
 	
 	// 検索のためのフィールド条件
 	private BSONObject selector;
 	
 	// 検索のための特殊フィールド
 	private BSONObject special;
 	
 	// ソートのためのフィールド
 	private BSONObject orderby;
 	
 	// クエリーインデクスヒント
 	private BSONObject hints;
 	
 	// 取得するフィールド一覧
 	private BSONObject fields;
 	
 	// 使用するインデクスに対する検索最小値
 	private BSONObject min;
 	
 	// 使用するインデクスに対する検索最大値
 	private BSONObject max;
 	
 	// 結果を一度にフェッチするサイズ
 	private int batchSize = 100;
 	
 	// 最初にスキップする件数
 	private int firstSkip = 0;
 	
 	// カーソルから取得した最新の結果
 	private Response lastResult;
 	
 	// 現在選択中のオブジェクト
 	private BSONObject currentObject;
 	
 	// 現在の結果におけるインデクス
 	private int indexInResult = 0;
 	
 	// カーソル最終位置への到達フラグ
 	private boolean finished = false;
 	
 	// カーソルのクローズフラグ
 	private boolean closed = false;
 	
 	// 現在の取得件数
 	private int position = 0;
 	
 	MongoCursor(
 			MongoConnection conn,
 			String databaseName,
 			String collectionName) {
 		this.conn = conn;
 		this.databaseName = databaseName;
 		this.collectionName = collectionName;
 	}
 	
 	/**
 	 * カーソルの対象としているデータベース名を取得します。
 	 * @return
 	 */
 	public String getDatabaseName() {
 		return databaseName;
 	}
 	
 	/**
 	 * カーソルの対象としているコレクション名を取得します。
 	 * @return
 	 */
 	public String getCollectionName() {
 		return collectionName;
 	}
 	
 	/**
 	 * 最大取得件数を設定します。
 	 * 
 	 * @param limit
 	 * @return
 	 */
 	public MongoCursor limit(int limit) {
		return special("$maxscan", limit);
 	}
 	
 	/**
 	 * 取得フィールドを設定・追加します。
 	 * @param field
 	 * @return
 	 */
 	public MongoCursor field(String field) {
 		return field(field, 1);
 	}
 	
 	/**
 	 * 取得フィールドを設定・追加します。
 	 * @param field
 	 * @param value
 	 * @return
 	 */
 	public MongoCursor field(String field, Object value) {
 		if (fields == null) {
 			fields = new BasicBSONObject();
 		}
 		fields.put(field, value);
 		return this;
 	}
 	
 	/**
 	 * クエリインデクスヒントとして使用する
 	 * フィールド名を指定します。
 	 * 
 	 * @param field
 	 * @return
 	 */
 	public MongoCursor hint(String field) {
 		if (hints == null) {
 			hints = new BasicBSONObject();
 			special.put("$hint", hints);
 		}
 		hints.put(field, 1);
 		return this;
 	}
 	
 	/**
 	 * 使用するインデクスに対する最小値を設定します。
 	 * ここで使用するフィールドは hint メソッドによって
 	 * 指定されている必要があります。
 	 * 
 	 * @param field
 	 * @param value
 	 * @return
 	 */
 	public MongoCursor min(String field, Object value) {
 		if (min == null) {
 			min = new BasicBSONObject();
 			special.put("$min", min);
 		}
 		min.put(field, value);
 		return this;
 	}
 	
 	/**
 	 * 使用するインデクスに対する最大値を設定します。
 	 * ここで使用するフィールドは hint メソッドによって
 	 * 指定されている必要があります。
 	 * 
 	 * @param field
 	 * @param value
 	 * @return
 	 */
 	public MongoCursor max(String field, Object value) {
 		if (max == null) {
 			max = new BasicBSONObject();
 			special.put("$max", max);
 		}
 		max.put(field, value);
 		return this;
 	}
 	
 	/**
 	 * インデクスキーのみの返却を設定します。
 	 * @param returnKey
 	 * @return
 	 */
 	public MongoCursor returnKey() {
 		return special("$returnKey", true);
 	}
 	
 	/**
 	 * 結果をフェッチする際に一括で取得する件数を設定します。
 	 * @param batchSize
 	 * @return
 	 */
 	public MongoCursor batchSize(int batchSize) {
 		this.batchSize = batchSize;
 		return this;
 	}
 	
 	/**
 	 * 結果取得の際に最初にスキップする件数を設定します。
 	 * @param skip
 	 * @return
 	 */
 	public MongoCursor firstSkip(int firstSkip) {
 		this.firstSkip = firstSkip;
 		return this;
 	}
 	
 	/**
 	 * 指定フィールドのソートオーダーを指定します。
 	 * @param key
 	 * @param asc
 	 * @return
 	 */
 	public MongoCursor sort(String field, boolean asc) {
 		if (orderby == null) {
 			orderby = new BasicBSONObject();
 			special("$orderby", orderby);
 		}
 		orderby.put(field, asc ? 1 : -1);
 		return this;
 	}
 	
 	/**
 	 * クエリの実行計画を取得するための設定をします。
 	 * @return
 	 */
 	public MongoCursor explain() {
 		return special("$explain", true);	
 	}
 	
 	/**
 	 * クエリに対する slaveOk フラグを設定します。
 	 * @return
 	 */
 	public MongoCursor slaveOk() {
 		query.slaveOk(true);
 		return this;
 	}
 	
 	/**
 	 * クエリに対する Talable フラグを設定します。 
 	 * @return
 	 */
 	public MongoCursor tailable() {
 		query.tailableCursor(true);
 		return this;
 	}
 	
 	/**
 	 * クエリに対する カーソルタイムアウトを無効にします。
 	 * @return
 	 */
 	public MongoCursor noCursorTimeout() {
 		query.noCursorTimeout(true);
 		return this;
 	}
 	
 	/**
 	 * クエリに対するデータ待機を有効にします。
 	 * @return
 	 */
 	public MongoCursor awaitData() {
 		query.awaitData(true);
 		return this;
 	}
 	
 	/**
 	 * クエリに対する Exhaust オプションを有効にします。
 	 * @return
 	 */
 	public MongoCursor exhaust() {
 		query.exhaust(true);
 		return this;
 	}
 	
 	/**
 	 * クエリのスナップショットを設定します。
 	 * @return
 	 */
 	public MongoCursor snapshot() {
 		return special("$snapshot", true);
 	}
 	
 	/**
 	 * 特殊フィールド条件を追加します。
 	 * @param name
 	 * @param value
 	 * @return
 	 */
 	public MongoCursor special(String name, Object value) {
 		if (this.special == null) {
 			this.special = new BasicBSONObject();
 		}
 		special.put(name, value);
 		return this;
 	}
 	
 	/**
 	 * クエリ条件を追加します。
 	 * @param field
 	 * @param queryValue
 	 * @return
 	 */
 	public MongoCursor selector(String field, Object queryValue) {
 		if (this.selector == null) {
 			this.selector = new BasicBSONObject();
 		}
 		this.selector.put(field, queryValue);
 		return this;
 	}
 	
 	/**
 	 * 指定のクエリ条件を設定します。
 	 * @param selector
 	 * @return
 	 */
 	public MongoCursor selector(BSONObject selector) {
 		if (this.selector == null) {
 			this.selector = selector;
 		} else {
 			this.selector.putAll(selector);
 		}
 		return this;
 	}
 	
 	/**
 	 * 詳細なクエリ条件を追加します。
 	 * @param field
 	 * @param criteriaKey
 	 * @param criteriaValue
 	 * @return
 	 */
 	public MongoCursor selector(String field, String criteriaKey, Object criteriaValue) {
 		if (this.selector == null) {
 			this.selector = new BasicBSONObject();
 		}
 		BSONObject criteria = (BSONObject) selector.get(field);
 		if (criteria == null) {
 			criteria = new BasicBSONObject();
 			selector.put(field, criteria);
 		}
 		criteria.put(criteriaKey, criteriaValue);
 		return this;
 	}
 	
 	/**
 	 * 指定の値より大きな値を検索条件とします。
 	 * @param field
 	 * @param value
 	 * @return
 	 */
 	public MongoCursor greaterThan(String field, Object value) {
 		return selector(field, "$gt", value);
 	}
 	
 	/**
 	 * 指定の値より小さな値を検索条件とします。
 	 * @param field
 	 * @param value
 	 * @return
 	 */
 	public MongoCursor lesserThan(String field, Object value) {
 		return selector(field, "$lt", value);
 	}
 	
 	/**
 	 * 指定の値と等しいか、大きな値を検索条件とします。
 	 * @param field
 	 * @param value
 	 * @return
 	 */
 	public MongoCursor greaterThanEquals(String field, Object value) {
 		return selector(field, "$gte", value);
 	}
 	
 	/**
 	 * 指定の値と等しいか、小さい値を検索条件とします。
 	 * @param field
 	 * @param value
 	 * @return
 	 */
 	public MongoCursor lesserThanEquals(String field, Object value) {
 		return selector(field, "$lte", value);
 	}
 	
 	/**
 	 * 指定の値を不等であることを検索条件とします。
 	 * @param field
 	 * @param value
 	 * @return
 	 */
 	public MongoCursor notEquals(String field, Object value) {
 		return selector(field, "$ne", value);
 	}
 	
 	/**
 	 * 指定のフィールドが存在する行のみ検索条件とします。
 	 * @param field
 	 * @return
 	 */
 	public MongoCursor exists(String field) {
 		return selector(field, "$exists", true);
 	}
 	
 	@Override
 	public boolean hasNext() {
 		// 終了済みのカーソルチェック
 		if (finished || closed) {
 			return false;
 		}
 		// 最後の結果が存在しない場合は、クエリを投げて取得する
 		if (lastResult == null) {
 			if (query == null) {
 				BSONObject queryObject = null;
 				if (selector == null) {
 					selector = new BasicBSONObject();
 				}
 				if (special != null) {
 					special.put("$query", selector);
 					queryObject = special;
 				} else {
 					queryObject = selector;
 				}
 				query = new Query(databaseName, collectionName, firstSkip, batchSize, queryObject, fields);
 			}
 			lastResult= conn.query(query);
 			// 最終結果がなければ、次はない
 			if (lastResult.getNumberReturned() == 0) {
 				finished = true;
 				return false;
 			}
 		}
 		// ドキュメント一覧取得
 		List<BSONObject> documents = lastResult.getDocuments();
 		if (documents == null || indexInResult >= documents.size()) {
 			lastResult = conn.getMore(new GetMore(
 					databaseName,
 					collectionName,
 					batchSize,
 					lastResult.getCursorId()
 			));
 			indexInResult = 0;
 			if (lastResult.getNumberReturned() == 0) {
 				finished = true;
 				close();
 				return false;
 			}
 		}
 		return true;
 	}
 	
 	/**
 	 * このカーソルによって取得可能なすべてのオブジェクトを
 	 * {@link List} として取得します。
 	 * 
 	 * @return
 	 */
 	public List<BSONObject> asList() {
 		List<BSONObject> list = new ArrayList<BSONObject>();
 		for (BSONObject object : this) {
 			list.add(object);
 		}
 		return list;
 	}
 	
 	@Override
 	public BSONObject next() {
 		if (!hasNext()) {
 			return null;
 		}
 		List<BSONObject> documents = lastResult.getDocuments();
 		currentObject = documents.get(indexInResult);
 		position++;
 		indexInResult++;
 		return currentObject;
 	}
 	
 	@Override
 	public void remove() {
 		if (currentObject != null) {
 			Delete delete = new Delete(
 					databaseName,
 					collectionName,
 					new BasicBSONObject("_id", currentObject.get("_id"))
 			);
 			conn.delete(delete);
 		}
 	}
 	
 	@Override
 	public Iterator<BSONObject> iterator() {
 		return this;
 	}
 	
 	@Override
 	protected void finalize() throws Throwable {
 		close();
 	}
 
 	/**
 	 * このカーソルのクローズ処理を行います。
 	 * サーバーに対して KILL_CURSOR を送信し、
 	 * リソースの解放を実施します。
 	 */
 	public void close() {
 		if (!closed) {
 			if (lastResult != null) {
 				conn.killCursors(new KillCursors(lastResult.getCursorId()));
 			}
 			closed = true;
 		}
 	}
 }
