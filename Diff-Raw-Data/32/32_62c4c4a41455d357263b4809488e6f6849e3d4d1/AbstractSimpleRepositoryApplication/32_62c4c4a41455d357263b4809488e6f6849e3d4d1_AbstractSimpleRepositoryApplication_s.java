 // ========================================================================
// Copyright (C) Kuzumeji Project Team. All rights reserved.
 // GNU AFFERO GENERAL PUBLIC LICENSE Version 3, 19 November 2007
 // http://www.gnu.org/licenses/agpl-3.0.txt
 // ========================================================================
 package zeroth.framework.enterprise.app;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collection;
 import zeroth.framework.enterprise.domain.ConstraintsException;
 import zeroth.framework.enterprise.domain.SimpleRepository;
 import zeroth.framework.enterprise.shared.Persistable;
 import zeroth.framework.enterprise.shared.Tracer;
 import zeroth.framework.standard.shared.CommonUtils;
 /**
  * 基本リポジトリアプリケーション
  * @param <E> エンティティ型
  * @param <ID> 識別子オブジェクト型
  * @param <F> 検索条件オブジェクト型 TODO extends ValueObject<?>
  * @author nilcy
  */
 @Tracer
 public abstract class AbstractSimpleRepositoryApplication<E extends Persistable<ID>, ID extends Serializable, F extends Serializable>
     implements SimpleRepositoryApplication<E, ID, F> {
     /** 製品番号 */
     private static final long serialVersionUID = 6716209657934350302L;
     /**
      * {@inheritDoc}
      * <dl>
      * <dt>事後条件</dt>
      * <dd>管理エンティティのディープコピーを戻す。</dd>
      * </dl>
      * @see SimpleRepository#save(Persistable)
      */
     @Override
     public E save(final E entity) throws ConstraintsException {
         return CommonUtils.deepCopy(getRepository().save(entity));
     }
     /**
      * {@inheritDoc}
      * @see SimpleRepository#delete(Persistable)
      */
     @Override
     public void delete(final E aEntity) throws ConstraintsException {
         getRepository().delete(aEntity);
     }
     /**
      * {@inheritDoc}
      * <dl>
      * <dt>事後条件</dt>
      * <dd>管理エンティティのディープコピーを戻す。</dd>
      * </dl>
      * @see SimpleRepository#find(Serializable)
      */
     @Override
     public E find(final ID id) {
         return CommonUtils.deepCopy(this.getRepository().find(id));
     }
     /**
      * {@inheritDoc}
      * <dl>
      * <dt>事後条件</dt>
      * <dd>管理エンティティのディープコピーを戻す。</dd>
      * </dl>
      * @see SimpleRepository#findOne(Serializable)
      */
     @Override
     public E findOne(final F filter) {
         return CommonUtils.deepCopy(this.getRepository().findOne(filter));
     }
     /**
      * {@inheritDoc}
      * <dl>
      * <dt>事後条件</dt>
      * <dd>管理エンティティのディープコピーを戻す。</dd>
      * </dl>
      * @see SimpleRepository#findMany(Serializable)
      */
     @Override
     public Collection<E> findMany(final F filter) {
         final Collection<E> entities = this.getRepository().findMany(filter);
         final Collection<E> results = new ArrayList<>(entities.size());
         for (final E e : entities) {
             results.add(CommonUtils.deepCopy(e));
         }
         return results;
     }
     /**
      * {@inheritDoc}
      * @see SimpleRepository#count(Serializable)
      */
     @Override
     public long count(final F filter) {
         return this.getRepository().count(filter);
     }
     /**
      * 基本リポジトリI/Fの取得
      * @return 基本リポジトリI/F
      */
     protected abstract SimpleRepository<E, ID, F> getRepository();
 }
