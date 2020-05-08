 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.muni.fi.pa165.dao.service.impl;
 
 import com.muni.fi.pa165.dao.MonsterAreaDao;
 import com.muni.fi.pa165.dao.service.MonsterAreaService;
 import com.muni.fi.pa165.dto.MonsterAreaDto;
 import com.muni.fi.pa165.entities.Area;
 import com.muni.fi.pa165.entities.Monster;
 import com.muni.fi.pa165.entities.MonsterArea;
 import javax.inject.Inject;
 import org.dozer.Mapper;
 import java.util.ArrayList;
 import java.util.List;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 /**
  *
  * @author irina
  */
 @Service
@Transactional
 public class MonsterAreaServiceImpl implements MonsterAreaService {
     //private static final Logger logger = Logger.getLogger(MonsterServiceImpl.class.getName());
 
     @Inject
     private MonsterAreaDao dao;
     @Inject
     private Mapper mapper;
 
    @Override   
     public MonsterAreaDto save(MonsterAreaDto dto) {
         MonsterArea entity = mapper.map(dto, MonsterArea.class);
         dao.save(entity);
         return mapper.map(entity, MonsterAreaDto.class);
 
     }
 
     @Override
     public MonsterAreaDto update(MonsterAreaDto dto) {
 
         MonsterArea entity = mapper.map(dto, MonsterArea.class);
         dao.update(entity);
         return mapper.map(entity, MonsterAreaDto.class);
 
     }
 
     @Override
     public void delete(MonsterAreaDto dto) {
 
         dao.delete(mapper.map(dto, MonsterArea.class));
 
     }
 
     public void setDao(MonsterAreaDao dao) {
         this.dao = dao;
     }
 
     public void setMapper(Mapper mapper) {
         this.mapper = mapper;
     }
 
    
     @Override
     public List<MonsterAreaDto> findByMonsterId(Long id) {
         MonsterServiceImpl mn = new MonsterServiceImpl();
         Monster monster = mapper.map(mn.findById(id), Monster.class);
         List<MonsterArea> ma = dao.getMonsterAreaForThisMonsterType(monster);
         List<MonsterAreaDto> result = new ArrayList<>();
         for (MonsterArea monsterA : ma) {
             result.add(mapper.map(monsterA, MonsterAreaDto.class));
         }
         return result;
     }
 
     @Override
     public List<MonsterAreaDto> findByAreaId(Long id) {
         AreaServiceImpl mn = new AreaServiceImpl();
         Area area = mapper.map(mn.findById(id), Area.class);
         List<MonsterArea> ma = dao.getMonsterAreaForThisAreaType(area);
         List<MonsterAreaDto> result = new ArrayList<>();
         for (MonsterArea monsterA : ma) {
             result.add(mapper.map(monsterA, MonsterAreaDto.class));
         }
         return result;
     }
 }
