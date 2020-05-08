 package com.cqlybest.common.service;
 
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 import com.cqlybest.common.Cn2Spell;
 import com.cqlybest.common.bean.Dict;
 import com.cqlybest.common.dao.DictDao;
 
 @Service
 public class DictService {
 
   @Autowired
   private DictDao dictDao;
 
   public void addDict(Dict dict) {
     dict.setPinyin(Cn2Spell.converterToSpell(dict.getName()));
     dict.setPy(Cn2Spell.converterToFirstSpell(dict.getName()));
     dictDao.saveOrUpdate(dict);
   }
 
   public void deleteDict(Dict dict) {
     Dict _dict = dictDao.findById(dict.getClass(), dict.getId());
     if (_dict != null) {
      dictDao.delete(dict);
     }
   }
 
   public <T extends Dict> List<T> getDict(Class<T> cls) {
     return dictDao.findAllDict(cls);
   }
 
 }
