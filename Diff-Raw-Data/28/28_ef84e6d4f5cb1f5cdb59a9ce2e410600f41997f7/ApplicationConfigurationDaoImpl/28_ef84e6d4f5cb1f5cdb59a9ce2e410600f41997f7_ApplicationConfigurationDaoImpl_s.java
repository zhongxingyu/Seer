 package com.financial.pyramid.dao.impl;
 
 import com.financial.pyramid.dao.ApplicationConfigurationDao;
 import com.financial.pyramid.domain.ApplicationConfiguration;
 
 /**
  * User: dbudunov
  * Date: 07.08.13
  * Time: 20:51
  */
 public class ApplicationConfigurationDaoImpl extends AbstractDaoImpl<ApplicationConfiguration, Long>
         implements ApplicationConfigurationDao {
 
     protected ApplicationConfigurationDaoImpl() {
         super(ApplicationConfiguration.class);
     }
 }
