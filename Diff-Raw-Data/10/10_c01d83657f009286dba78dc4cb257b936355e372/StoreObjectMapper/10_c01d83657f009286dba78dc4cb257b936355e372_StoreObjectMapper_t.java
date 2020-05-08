 package com.barchart.store.util;
 
 import java.util.Arrays;
 import java.util.List;
 
 import rx.Observable;
 import rx.util.functions.Action1;
 import rx.util.functions.Func1;
 
 import com.barchart.store.api.Batch;
 import com.barchart.store.api.ObservableIndexQueryBuilder;
 import com.barchart.store.api.ObservableIndexQueryBuilder.Operator;
 import com.barchart.store.api.ObservableQueryBuilder;
 import com.barchart.store.api.RowMutator;
 import com.barchart.store.api.StoreService;
 import com.barchart.store.api.StoreService.Table;
 
 public abstract class StoreObjectMapper {
 
 	private final StoreService store;
 	private final String database;
 	private final StoreSchema schema;
 	private final MapperFactory mappers;
 
 	public StoreObjectMapper(final StoreService store_, final String database_,
 			final StoreSchema schema_) {
 		store = store_;
 		database = database_;
 		schema = schema_;
 		mappers = new MapperFactory();
 	}
 
 	/**
 	 * Update the data store schema definition. This should only be called
 	 * manually.
 	 */
 	public void updateSchema() throws Exception {
 		schema.update(store, database);
 	}
 
 	/*
 	 * Direct store access methods for subclasses with more complex query
 	 * requirements.
 	 */
 
 	protected StoreService store() {
 		return store;
 	}
 
 	protected String database() {
 		return database;
 	}
 
 	protected <K, T, M extends RowMapper<K, T>> M mapper(final Class<M> cls) {
 		return mappers.instance(cls);
 	}
 
 	/*
 	 * Helper methods for subclasses.
 	 */
 
 	/**
 	 * Load rows from a store table and convert them to objects.
 	 * 
 	 * @param table The store table
 	 * @param mapper The object mapper
 	 * @param keys The row keys to load
 	 * @return A lazy observable that executes on every subscribe
 	 */
 	protected <K, V, T, M extends RowMapper<K, T>> Observable<T> loadRows(
 			final Table<K, V> table, final Class<M> mapper,
 			final String... keys) {
 
 		try {
 			return store.fetch(database, table, keys).build()
 					.filter(new EmptyRowFilter<K>()).map(mapper(mapper));
 		} catch (final Exception e) {
 			return Observable.error(e);
 		}
 
 	}
 
 	/**
 	 * Load columns from a table row and convert them to objects.
 	 * 
 	 * @param table The store table
 	 * @param mapper The object mapper
 	 * @param key The row key to load
 	 * @param columns The column names to load
 	 * @return A lazy observable that executes on every subscribe
 	 */
 	protected <K, V, T, M extends RowMapper<K, List<T>>> Observable<T> loadColumns(
 			final Table<K, V> table, final Class<M> mapper, final String key,
 			final K... columns) {
 
 		try {
 
 			final ObservableQueryBuilder<K> query =
 					store.fetch(database, table, key);
 
 			if (columns != null && columns.length > 0) {
 				query.columns(columns);
 			}
 
 			return query.build().filter(new EmptyRowFilter<K>())
 					.map(mapper(mapper)).mapMany(new ListExploder<T>());
 
 		} catch (final Exception e) {
 			return Observable.error(e);
 		}
 
 	}
 
 	/**
 	 * Load columns from a table row and convert them to objects.
 	 * 
 	 * @param table The store table
 	 * @param mapper The object mapper
 	 * @param key The row key to load
 	 * @param prefix The column name prefix to filter by
 	 * @return A lazy observable that executes on every subscribe
 	 */
 	protected <V, T, M extends RowMapper<String, List<T>>> Observable<T> loadColumnsByPrefix(
 			final Table<String, V> table, final Class<M> mapper,
 			final String key, final String prefix) {
 
 		try {
 
 			return store.fetch(database, table, key).prefix(prefix).build()
 					.filter(new EmptyRowFilter<String>()).map(mapper(mapper))
 					.mapMany(new ListExploder<T>());
 
 		} catch (final Exception e) {
 			return Observable.error(e);
 		}
 
 	}
 
 	/**
 	 * Load columns from a table row and convert them to objects.
 	 * 
 	 * @param table The store table
 	 * @param mapper The object mapper
 	 * @param key The row key to load
 	 * @param count The number of columns to load in sorted order
 	 * @param reverse Sort descending instead of ascending
 	 * @return A lazy observable that executes on every subscribe
 	 */
 	protected <K, V, T, M extends RowMapper<K, List<T>>> Observable<T> loadColumns(
 			final Table<K, V> table, final Class<M> mapper, final String key,
 			final int count, final boolean reverse) {
 
 		try {
 
 			final ObservableQueryBuilder<K> query =
 					store.fetch(database, table, key);
 
 			if (reverse) {
 				query.last(count);
 			} else {
 				query.first(count);
 			}
 
 			return query.build().filter(new EmptyRowFilter<K>())
 					.map(mapper(mapper)).mapMany(new ListExploder<T>());
 
 		} catch (final Exception e) {
 			return Observable.error(e);
 		}
 
 	}
 
 	/**
 	 * 
 	 * Load a range of columns from a table row and convert them to objects.
 	 * 
 	 * @param table The store table
 	 * @param mapper The object mapper
 	 * @param key The row key to load
 	 * @param start The column name to start the range with
 	 * @param end The column name to end the range with
 	 * @return A lazy observable that executes on every subscribe
 	 */
 	protected <K, V, T, M extends RowMapper<K, List<T>>> Observable<T> loadColumns(
 			final Table<K, V> table, final Class<M> mapper, final String key,
 			final K start, final K end) {
 
 		try {
 
 			return store.fetch(database, table, key).start(start).end(end)
 					.build().filter(new EmptyRowFilter<K>())
 					.map(mapper(mapper)).mapMany(new ListExploder<T>());
 
 		} catch (final Exception e) {
 			return Observable.error(e);
 		}
 
 	}
 
 	/**
 	 * Find rows based on secondary index values.
 	 * 
 	 * @param table The store table
 	 * @param mapper The object mapper
 	 * @param clauses Clauses for index searching
 	 * @return A lazy observable that executes on every subscribe
 	 */
 	@SafeVarargs
 	protected <K, V, T, M extends RowMapper<K, T>> Observable<T> findRows(
 			final Table<K, V> table, final Class<M> mapper,
 			final Where<K>... clauses) {
 
 		try {
 
 			final ObservableIndexQueryBuilder<K> builder =
 					store.query(database, table);
 
 			for (final Where<K> where : clauses) {
 				builder.where(where.field, where.value, where.operator);
 			}
 
 			return builder.build().filter(new EmptyRowFilter<K>())
 					.map(mapper(mapper));
 
 		} catch (final Exception e) {
 			return Observable.error(e);
 		}
 
 	}
 
 	/**
 	 * Create a new row in the store from the specified object.
 	 * 
 	 * @param table The store table
 	 * @param mapper The object mapper
 	 * @param key The row key
 	 * @param obj The object to store
 	 * @return A cached observable that is executed immediately
 	 */
 	protected <K, V, T, U extends T, M extends RowMapper<K, T>> Observable<T> createRow(
 			final Table<K, V> table, final Class<M> mapper, final String key,
 			final U obj) {
 
 		return cache(loadRows(table, mapper, key).isEmpty().mapMany(
 				new Func1<Boolean, Observable<T>>() {
 
 					@Override
 					public Observable<T> call(final Boolean empty) {
 						if (empty) {
 							return updateRow(table, mapper, key, obj);
 						} else {
 							return Observable.error(new Exception(
 									"Object ID already exists"));
 						}
 					}
 
 				}));
 
 	}
 
 	/**
 	 * Update a row in the store with the specified object.
 	 * 
 	 * @param table The store table
 	 * @param mapper The object mapper
 	 * @param key The row key
 	 * @param obj The object to store
 	 * @return A cached observable that is executed immediately
 	 */
 	protected <K, V, T, U extends T, M extends RowMapper<K, T>> Observable<T> updateRow(
 			final Table<K, V> table, final Class<M> mapper, final String key,
 			final U obj) {
 
 		try {
 
 			final Batch batch = store.batch(database);
 			final RowMutator<K> mutator = batch.row(table, key);
 
 			mapper(mapper).encode(obj, mutator);
 			batch.commit();
 
 			return Observable.<T> from(obj);
 
 		} catch (final Exception e) {
 			return Observable.error(e);
 		}
 
 	}
 
 	/**
 	 * Load columns from a table row and convert them to objects.
 	 * 
 	 * @param table The store table
 	 * @param mapper The object mapper
 	 * @param key The row key
 	 * @param objects The objects to store as columns
 	 * @return A cached observable that is executed immediately
 	 */
 	protected <K, V, T, U extends T, M extends RowMapper<K, List<T>>> Observable<T> updateColumns(
 			final Table<K, V> table, final Class<M> mapper, final String key,
 			final U... objects) {
 
 		try {
 
 			final Batch batch = store.batch(database);
 			final RowMutator<K> mutator = batch.row(table, key);
 			mapper(mapper).encode(Arrays.asList((T[]) objects), mutator);
 			batch.commit();
 
 			return Observable.<T> from(objects);
 
 		} catch (final Exception e) {
 			return Observable.error(e);
 		}
 
 	}
 
 	/**
 	 * Delete rows from the store by key.
 	 * 
 	 * @param table The store table
 	 * @param keys The row keys to delete
 	 * @return A cached observable that is executed immediately
 	 */
 	protected <K, V> Observable<String> deleteRows(final Table<K, V> table,
 			final String... keys) {
 
 		try {
 
 			final Batch batch = store.batch(database);
 			for (final String key : keys) {
 				batch.row(table, key).delete();
 			}
 			batch.commit();
 
 			return Observable.from(keys);
 
 		} catch (final Exception e) {
 			return Observable.error(e);
 		}
 
 	}
 
 	/**
 	 * Delete columns from a row in the store.
 	 * 
 	 * @param table The store table
 	 * @param key The row key
 	 * @param columns The column names to delete
 	 * @return A cached observable that is executed immediately
 	 */
 	protected <K, V> Observable<K> deleteColumns(final Table<K, V> table,
 			final String key, final K... columns) {
 
 		try {
 
 			final Batch batch = store.batch(database);
 			final RowMutator<K> row = batch.row(table, key);
 
 			for (final K column : columns) {
 				row.remove(column);
 			}
 
 			batch.commit();
 
 			return Observable.from(columns);
 
 		} catch (final Exception e) {
 			return Observable.error(e);
 		}
 
 	}
 
 	protected static String[] toStrings(final Object[] ary) {
 
 		final String[] strings = new String[ary.length];
 
 		for (int i = 0; i < ary.length; i++) {
 			strings[i] = ary[i].toString();
 		}
 
 		return strings;
 
 	}
 
 	/**
 	 * Auto subscribe to an observable, caching the result for future
 	 * subscriptions.
 	 */
 	protected static <T> Observable<T> cache(final Observable<T> observable) {

		final Observable<T> cached = observable.cache();

		cached.subscribe(new Action1<T>() {
 			@Override
 			public void call(final T t1) {
 			}
 		});

		return cached;

 	}
 
 	protected static String[] toStrings(final List<?> list) {
 
 		final String[] strings = new String[list.size()];
 
 		for (int i = 0; i < list.size(); i++) {
 			strings[i] = list.get(i).toString();
 		}
 
 		return strings;
 
 	}
 
 	private static class ListExploder<T> implements
 			Func1<List<T>, Observable<T>> {
 
 		@Override
 		public Observable<T> call(final List<T> t1) {
 			return Observable.from(t1);
 		}
 
 	}
 
 	protected static class Where<T> {
 
 		private final T field;
 		private final Object value;
 		private final Operator operator;
 
 		private Where(final T field_, final Object value_,
 				final Operator operator_) {
 			field = field_;
 			value = value_;
 			operator = operator_;
 		}
 
 		public static <T> Where<T> clause(final T field, final Object value) {
 			return new Where<T>(field, value, Operator.EQUAL);
 		}
 
 		public static <T> Where<T> clause(final T field, final Object value,
 				final Operator operator) {
 			return new Where<T>(field, value, operator);
 		}
 
 	}
 
 }
