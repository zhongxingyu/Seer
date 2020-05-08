 // ========================================================================
 // Copyright (C) zeroth Project Team. All rights reserved.
 // GNU AFFERO GENERAL PUBLIC LICENSE Version 3, 19 November 2007
 // http://www.gnu.org/licenses/agpl-3.0.txt
 // ========================================================================
 package zeroth.framework.enterprise.domain;
 import java.io.Serializable;
 import java.util.Collection;
 import zeroth.framework.standard.domain.Repository;
 import zeroth.framework.standard.shared.ValueObject;
 /**
  * 基本リポジトリI/F
  * @param <T> エンティティ型
  * @param <ID> 識別子オブジェクト型
  * @author nilcy
  */
 public interface SimpleRepository<T extends Persistable<ID>, ID extends Serializable> extends
     Repository {
     /**
     * エンティティ保存
      * @param entity エンティティ
      */
     void save(T entity);
     /**
     * エンティティ検索
      * @param id 識別子
      * @return エンティティ
      */
     T find(ID id);
     /**
     * 単一エンティティ検索
      * <p>
      * 一意制約(UK)をもとにした検索などに使用すること。
      * </p>
      * @param condition 検索条件
      * @return エンティティ(該当オブジェクトがないときNULL)
      */
     <V extends ValueObject<?>> T findOne(V condition);
     /**
     * 複数エンティティ検索
      * <p>
      * フィルタ条件などをもとにした検索などに使用すること。
      * </p>
      * @param condition 検索条件
      * @return エンティティ集合(該当オブジェクトがないとき空集合)
      */
     <V extends ValueObject<?>> Collection<T> findMany(V condition);
     /**
     * エンティティ削除
      * @param entity エンティティ
      */
     void delete(T entity);
 }
