 // ========================================================================
 // Copyright (C) zeroth Project Team. All rights reserved.
 // GNU AFFERO GENERAL PUBLIC LICENSE Version 3, 19 November 2007
 // http://www.gnu.org/licenses/agpl-3.0.txt
 // ========================================================================
 package zeroth.framework.enterprise.domain;
 import java.math.BigDecimal;
 import javax.persistence.Basic;
 import javax.persistence.Column;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.MappedSuperclass;
 import javax.persistence.PostLoad;
 import javax.persistence.PostPersist;
 import javax.persistence.PostUpdate;
 import javax.persistence.Transient;
 import javax.validation.constraints.NotNull;
 import org.apache.commons.lang3.builder.EqualsBuilder;
 import zeroth.framework.enterprise.shared.Persistable;
 import zeroth.framework.standard.domain.ReferenceObject;
 import zeroth.framework.standard.shared.AbstractDataObject;
 /**
  * 永続可能エンティティ
  * <p>
  * 前提としてJPAが永続化するために識別子(ID)が必要である。(IDによる同一性の確認ができる) まず、参照オブジェクトを永続化するときは概念上の識別子と一致するため問題はない。
  * いっぽう、値オブジェクトを永続化するときは値による同一性の確認が一般的であることに注意すること。
  * いわゆる、(永続化する)値オブジェクトのIDによる同一性の確認はJPA永続化のために必要なものであり 、ビジネスロジックにおいて使用すべきものではないことに注意すること。
  * </p>
  * @param <E> 永続可能エンティティ型
  * @author nilcy
  */
 @MappedSuperclass
 public abstract class AbstractPersistable<E extends AbstractPersistable<E>> extends
     AbstractDataObject implements ReferenceObject<E, BigDecimal>, Persistable<BigDecimal> {
     /** 識別番号 */
     private static final long serialVersionUID = 6765184066419433024L;
     /** 識別子(ID) */
     @Id
     @Basic(optional = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
     @NotNull
     @Column(name = "id", nullable = false, insertable = true, updatable = false)
     private BigDecimal id;
     /** 永続済 */
     @Transient
     private boolean persisted = false;
     /** コンストラクタ */
     public AbstractPersistable() {
     }
     /**
      * {@link #id} の取得
      * @return {@link #id}
      */
     public BigDecimal getId() {
         return id;
     }
     /**
      * {@link #id} の設定
      * @param id {@link #id}
      */
     public void setId(final BigDecimal id) {
         this.id = id;
     }
     /** {@inheritDoc} */
     @Override
     public BigDecimal identity() {
         return id;
     }
     /** {@inheritDoc} */
     @Override
     public boolean isPersisted() {
         return persisted;
     }
     /** {@inheritDoc} */
     @Override
     public boolean sameIdentityAs(final E other) {
         return other != null && new EqualsBuilder().append(identity(), other.identity()).isEquals();
     }
     /** 登録後/更新後/取得後のコールバック */
     @PostPersist
     @PostUpdate
     @PostLoad
     private void setPersisted() {
         persisted = true;
     }
 }
