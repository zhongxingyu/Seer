 package com.zenika.dorm.core.service.impl;
 
 import com.google.inject.Inject;
 import com.zenika.dorm.core.dao.DormDao;
 import com.zenika.dorm.core.graph.Dependency;
 import com.zenika.dorm.core.graph.DependencyNode;
 import com.zenika.dorm.core.graph.impl.DefaultDependency;
 import com.zenika.dorm.core.graph.impl.Usage;
 import com.zenika.dorm.core.model.DormFile;
 import com.zenika.dorm.core.model.DormMetadata;
 import com.zenika.dorm.core.repository.DormRepositoryManager;
 import com.zenika.dorm.core.service.DormService;
 
 /**
  * @author Lukasz Piliszczuk <lukasz.piliszczuk AT zenika.com>
  */
 public class DefaultDormService implements DormService {
 
     @Inject
     private DormDao dao;
 
//    @Inject
     private DormRepositoryManager repositoryManager;
 
     /**
      * Store metadata on the database and file on the filesystem
      *
      * @param node
      * @return
      */
     @Override
     public Boolean pushNode(DependencyNode node) {
         return dao.push(node);
     }
 
     @Override
     public Dependency getByMetadata(DormMetadata metadata, Usage usage) {
 //        return dao.getByMetadata(metadata, usage);
 
         DormFile file = repositoryManager.get(metadata);
         return DefaultDependency.create(metadata, file);
     }
 }
