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
    private QueryPersistenceService<TestExample, Long> testExampleObjectPersistenceService;
     /** コンストラクタ */
     public QueryPersistenceServiceFactory() {
     }
     /**
     * {@link #testExampleObjectPersistenceService} の作成
     * @return {@link #testExampleObjectPersistenceService}
      */
     @Produces
     public QueryPersistenceService<TestExample, Long> createTestExampleObjectPersistenceService() {
        this.testExampleObjectPersistenceService.setup(TestExample.class, this.manager);
        return this.testExampleObjectPersistenceService;
     }
 }
