 // ========================================================================
 // Copyright (C) zeroth Project Team. All rights reserved.
 // GNU AFFERO GENERAL PUBLIC LICENSE Version 3, 19 November 2007
 // http://www.gnu.org/licenses/agpl-3.0.txt
 // ========================================================================
 package zeroth.framework.enterprise.infra.persistence;
 import javax.enterprise.inject.Produces;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 /**
  * エンティティマネージャのファクトリ
  * @author nilcy
  */
 public final class EntityManagerFactory {
     /** 基礎エンティティマネージャ */
     @Produces
     @PrimaryEntityManager
     @PersistenceContext(unitName = "primary")
     private EntityManager primaryEntityManager;
    /** */
     // /** 補助エンティティマネージャ */
     // @Produces
     // @SecondaryEntityManager
     // @PersistenceContext(unitName = "secondary")
     // private EntityManager secondaryEntityManager;
     /** コンストラクタ */
     public EntityManagerFactory() {
     }
 }
