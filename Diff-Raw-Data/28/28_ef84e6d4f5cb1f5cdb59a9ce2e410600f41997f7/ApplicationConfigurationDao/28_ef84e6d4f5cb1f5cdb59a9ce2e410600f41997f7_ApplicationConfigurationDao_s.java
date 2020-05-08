 package com.financial.pyramid.dao;
 
 import com.financial.pyramid.domain.ApplicationConfiguration;
import org.springframework.stereotype.Repository;
 
 /**
  * User: dbudunov
  * Date: 07.08.13
  * Time: 20:50
  */
@Repository(value = "applicationConfigurationDao")
 public interface ApplicationConfigurationDao extends AbstractDao<ApplicationConfiguration, Long> {
 }
