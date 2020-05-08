 package com.financial.pyramid.service.impl;
 
 import com.financial.pyramid.dao.ApplicationConfigurationDao;
 import com.financial.pyramid.domain.ApplicationConfiguration;
 import com.financial.pyramid.service.ApplicationConfigurationService;
 import org.hibernate.criterion.Restrictions;
 import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
 
 import java.util.List;
 
 /**
  * User: dbudunov
  * Date: 07.08.13
  * Time: 20:48
  */
@Service("applicationConfigurationService")
@Transactional
 public class ApplicationConfigurationServiceImpl implements ApplicationConfigurationService {
 
     @Autowired
     ApplicationConfigurationDao applicationConfigurationDao;
 
     @Override
     public String getParameter(String name) {
         List<ApplicationConfiguration> result = applicationConfigurationDao.findByCriteria(Restrictions.eq("name", name));
         if (result.size() > 0){
             ApplicationConfiguration parameter = result.get(0);
             return parameter.getValueString();
         }
         return null;
     }
 }
