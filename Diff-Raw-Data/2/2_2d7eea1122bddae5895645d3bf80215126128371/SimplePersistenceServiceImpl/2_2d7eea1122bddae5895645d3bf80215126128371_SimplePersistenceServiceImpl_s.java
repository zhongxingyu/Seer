 // ========================================================================
 // Copyright (C) zeroth Project Team. All rights reserved.
 // GNU AFFERO GENERAL PUBLIC LICENSE Version 3, 19 November 2007
 // http://www.gnu.org/licenses/agpl-3.0.txt
 // ========================================================================
 package zeroth.framework.enterprise.infra.persistence;
 import java.io.Serializable;
 import java.lang.reflect.ParameterizedType;
 import java.util.Collection;
 import java.util.Map;
 import javax.enterprise.inject.Default;
 import javax.persistence.EntityManager;
 import javax.persistence.LockModeType;
 import javax.persistence.TypedQuery;
 import zeroth.framework.enterprise.shared.Persistable;
 /**
  * 基本データ永続化サービス(JPA2)
  * <p>
  * 不変表明
  * <ol>
  * <li>インスタンス化するときにエンティティのマネージャとクラスを保持して、そのあと変更しない。</li>
  * <li>エンティティのライフサイクル(コンテキスト状態)に応じたJPAの基本操作を提供すること。</li>
  * <li>DBトランザクションに関する操作を実行しない。(暗黙的にトランザクションタイプ=JTAを想定している)</li>
  * </ol>
  * </p>
  * @param <E> エンティティ型
  * @param <ID> 識別子オブジェクト型
 * @since JPA 1.0
  * @author nilcy
  */
 @Default
 public class SimplePersistenceServiceImpl<E extends Persistable<ID>, ID extends Serializable>
     implements SimplePersistenceService<E, ID> {
     /** 識別番号 */
     private static final long serialVersionUID = -2663309706616831662L;
     /** エンティティマネージャ */
     protected EntityManager manager;
     /** エンティティクラス */
     protected Class<E> clazz;
     /**
      * コンストラクタ(汎用)
      * <ul>
      * <li>各エンティティに依存しない汎用的な永続化サービスのために使用すること。</li>
      * <li>データ永続化はインフラストラクチャ層のサービスで、エンティティに依存すべきでないため<b>推奨</b>する。</li>
      * </ul>
      * @param manager エンティティマネージャ
      * @param clazz エンティティクラス
      */
     public SimplePersistenceServiceImpl(final EntityManager manager, final Class<E> clazz) {
         assert manager != null && clazz != null;
         this.manager = manager;
         this.clazz = clazz;
     }
     /**
      * コンストラクタ(個別)
      * <ul>
      * <li>各エンティティに依存した個別の永続化サービスのために使用すること。</li>
      * <li>データ永続化はインフラストラクチャ層のサービスで、エンティティに依存すべきでないため<b>非推奨</b>とする。</li>
      * <li>※(具象クラスのため)エンティティ型からエンティティクラスを実行時に取得できるため引数は不要となる。</li>
      * </ul>
      * @param manager エンティティマネージャ
      */
     @SuppressWarnings("unchecked")
     public SimplePersistenceServiceImpl(final EntityManager manager) {
         this.manager = manager;
         clazz = (Class<E>) ((ParameterizedType) this.getClass().getGenericSuperclass())
             .getActualTypeArguments()[0];
     }
     /** {@inheritDoc} */
     @Override
     public <S extends E> void persist(final S entity) {
         manager.persist(entity);
     }
     /**
      * {@inheritDoc}
      * <p>
      * エンティティのクラスを指定して、ID検索する。
      * </p>
      */
     @Override
     public E find(final ID id) {
         return manager.find(clazz, id);
     }
     /**
      * {@inheritDoc}
      * <p>
      * エンティティのクラスを指定して、ID検索する。楽観ロック/悲観ロックなどを指定する。
      * </p>
      */
     @Override
     public E find(final ID id, final LockModeType lockModeType) {
         return manager.find(clazz, id, lockModeType);
     }
     /**
      * {@inheritDoc}
      * <p>
      * エンティティのクラスを指定して、ID検索する。プロパティを指定する。
      * </p>
      */
     @Override
     public E find(final ID id, final Map<String, Object> properties) {
         return manager.find(clazz, id, properties);
     }
     /**
      * {@inheritDoc}
      * <p>
      * エンティティのクラスを指定して、ID検索する。楽観ロック/悲観ロックなどとプロパティを指定する。
      * </p>
      */
     @Override
     public E find(final ID id, final LockModeType lockModeType, final Map<String, Object> properties) {
         return manager.find(clazz, id, lockModeType);
     }
     /** {@inheritDoc} */
     @Override
     public <S extends E> S merge(final S entity) {
         return manager.merge(entity);
     }
     /** {@inheritDoc} */
     @Override
     public void remove(final E entity) {
         manager.remove(entity);
     }
     /**
      * {@inheritDoc}
      * <p>
      * エンティティのクラスを指定して、更新する。
      * </p>
      */
     @Override
     public void refresh(final E entity) {
         manager.refresh(entity);
     }
     /**
      * {@inheritDoc}
      * <p>
      * エンティティのクラスを指定して、更新する。楽観ロック/悲観ロックなどを指定する。
      * </p>
      */
     @Override
     public void refresh(final E entity, final LockModeType lockModeType) {
         manager.refresh(entity, lockModeType);
     }
     /**
      * {@inheritDoc}
      * <p>
      * エンティティのクラスを指定して、更新する。プロパティを指定する。
      * </p>
      */
     @Override
     public void refresh(final E entity, final Map<String, Object> properties) {
         manager.refresh(entity, properties);
     }
     /**
      * {@inheritDoc}
      * <p>
      * エンティティのクラスを指定して、更新する。楽観ロック/悲観ロックなどとプロパティを指定する。
      * </p>
      */
     @Override
     public void refresh(final E entity, final LockModeType lockModeType,
         final Map<String, Object> properties) {
         manager.refresh(entity, lockModeType, properties);
     }
     /**
      * {@inheritDoc}
      * <p>
      * エンティティのクラスを指定して、保護する。楽観ロック/悲観ロックなどを指定する。
      * </p>
      */
     @Override
     public void lock(final E entity, final LockModeType lockModeType) {
         manager.lock(entity, lockModeType);
     }
     /**
      * {@inheritDoc}
      * <p>
      * エンティティのクラスを指定して、保護する。楽観ロック/悲観ロックなどとプロパティを指定する。
      * </p>
      */
     @Override
     public void lock(final E entity, final LockModeType lockModeType,
         final Map<String, Object> properties) {
         manager.lock(entity, lockModeType);
     }
     /** {@inheritDoc} */
     @Override
     public void flush() {
         manager.flush();
     }
     /** {@inheritDoc} */
     @Override
     public void detach(final E entity) {
         manager.detach(entity);
     }
     /** {@inheritDoc} */
     @Override
     public boolean contains(final E entity) {
         return manager.contains(entity);
     }
     /** {@inheritDoc} */
     @Override
     public TypedQuery<E> createRangeQuery(final TypedQuery<E> query, final int offset,
         final int maxsize) {
         return query.setFirstResult(offset).setMaxResults(maxsize);
     }
     /** {@inheritDoc} */
     @Override
     public Collection<E> findMany(final TypedQuery<E> query) {
         return query.getResultList();
     }
     /** {@inheritDoc} */
     @Override
     public E findOne(final TypedQuery<E> query) {
         return query.getSingleResult();
     }
     /** {@inheritDoc} */
     @Override
     public Map<String, Object> getProperties() {
         return manager.getProperties();
     }
     /** {@inheritDoc} */
     @Override
     public void setProperty(final String propertyName, final Object value) {
         manager.setProperty(propertyName, value);
     }
 }
