 // ========================================================================
 // Copyright (C) zeroth Project Team. All rights reserved.
 // GNU AFFERO GENERAL PUBLIC LICENSE Version 3, 19 November 2007
 // http://www.gnu.org/licenses/agpl-3.0.txt
 // ========================================================================
 package zeroth.framework.enterprise.infra.persistence;
 import javax.ejb.EJB;
 import javax.enterprise.inject.Produces;
 import javax.inject.Inject;
 import javax.persistence.EntityManager;
 import zeroth.framework.enterprise.domain.TestExample;
 /**
  * 拡張データ永続化サービスのファクトリ
  * @author nilcy
  */
 public class QueryPersistenceServiceFactory {
     /** 基礎エンティティマネージャ */
     @Inject
     @PrimaryEntityManager
     private EntityManager manager;
     /** テストオブジェクトの拡張データ永続化サービス */
     @EJB
    private QueryPersistenceService<TestExample, Long> testExamplePersistenceService;
     /** コンストラクタ */
     public QueryPersistenceServiceFactory() {
     }
     /**
     * {@link #testExamplePersistenceService} の作成
     * @return {@link #testExamplePersistenceService}
      */
     @Produces
     public QueryPersistenceService<TestExample, Long> createTestExampleObjectPersistenceService() {
        this.testExamplePersistenceService.setup(TestExample.class, this.manager);
        return this.testExamplePersistenceService;
     }
 }
