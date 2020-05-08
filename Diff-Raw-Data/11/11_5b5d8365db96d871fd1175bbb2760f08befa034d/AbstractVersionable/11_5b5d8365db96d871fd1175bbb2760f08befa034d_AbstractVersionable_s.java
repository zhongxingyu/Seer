 // ========================================================================
 // Copyright (C) zeroth Project Team. All rights reserved.
 // GNU AFFERO GENERAL PUBLIC LICENSE Version 3, 19 November 2007
 // http://www.gnu.org/licenses/agpl-3.0.txt
 // ========================================================================
 package zeroth.framework.enterprise.domain;
 import java.math.BigDecimal;
 import javax.persistence.Column;
 import javax.persistence.MappedSuperclass;
 import javax.persistence.Version;
 import zeroth.framework.enterprise.shared.Versionable;
 /**
  * 版管理エンティティ
  * <p>
  * JPAが版数をもとに楽観ロックを自動的に実装する。
  * </p>
  * @param <E> 版管理エンティティ型
  * @author nilcy
  */
 @MappedSuperclass
 public abstract class AbstractVersionable<E extends AbstractVersionable<E>> extends
     AbstractPersistable<E> implements Versionable<BigDecimal> {
     /** 識別番号 */
     private static final long serialVersionUID = 3662224470361465232L;
     /** 版数 */
    @Column(name = "version", nullable = false, insertable = true, updatable = true)
     @Version
     private Long version;
     /** コンストラクタ */
     public AbstractVersionable() {
     }
     /**
      * {@link #version} の取得
      * @return {@link #version}
      */
     @Override
     public Long getVersion() {
         return this.version;
     }
     /**
      * {@link #version} の設定
      * @param version {@link #version}
      */
     @Override
     public void setVersion(final Long version) {
         this.version = version;
     }
 }
