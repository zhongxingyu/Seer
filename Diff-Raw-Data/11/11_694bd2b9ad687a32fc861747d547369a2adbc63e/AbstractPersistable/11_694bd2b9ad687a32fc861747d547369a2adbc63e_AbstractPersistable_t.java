 // ========================================================================
 // Copyright (C) zeroth Project Team. All rights reserved.
 // GNU AFFERO GENERAL PUBLIC LICENSE Version 3, 19 November 2007
 // http://www.gnu.org/licenses/agpl-3.0.txt
 // ========================================================================
 package zeroth.framework.enterprise.domain;
 import javax.persistence.Column;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.MappedSuperclass;
 import org.apache.commons.lang3.builder.EqualsBuilder;
 import zeroth.framework.standard.domain.Persistable;
 import zeroth.framework.standard.domain.ReferenceObject;
 import zeroth.framework.standard.shared.AbstractDataObject;
 /**
  * 永続可能エンティティ
 * <p>
 * 前提としてJPAが永続化するために識別子(ID)が必要である。(IDによる同一性の確認ができる)
 * まず、参照オブジェクトを永続化するときは概念上の識別子と一致するため問題はない。
 * いっぽう、値オブジェクトを永続化するときは値による同一性の確認が一般的であることに注意すること。
 * いわゆる、(永続化する)値オブジェクトのIDによる同一性の確認はJPA永続化のために必要なものであり
 * 、ビジネスロジックにおいて使用すべきものではないことに注意すること。
 * </p>
  * @param <T> 永続可能エンティティ型
  * @author nilcy
  */
 @MappedSuperclass
 public abstract class AbstractPersistable<T extends AbstractPersistable<T>> extends
     AbstractDataObject implements ReferenceObject<T, Long>, Persistable<Long> {
     /** 識別番号 */
     private static final long serialVersionUID = 6765184066419433024L;
     /** 識別子(ID) */
     @Column
     @Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     private Long id;
     /** コンストラクタ */
     public AbstractPersistable() {
     }
     /**
      * {@link #id} の取得
      * @return {@link #id}
      */
     public Long getId() {
         return this.id;
     }
     /**
      * {@link #id} の設定
      * @param aId {@link #id}
      */
     public void setId(final Long aId) {
         this.id = aId;
     }
     @Override
     public Long identity() {
         return this.id;
     }
     @Override
     public boolean isPersisted() {
         return false;
     }
     @Override
     public boolean sameIdentityAs(final T aOther) {
        return (aOther != null)
            && new EqualsBuilder().append(identity(), aOther.identity()).isEquals();
     }
 }
