 package com.financial.pyramid.service;
 
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 /**
  * User: dbudunov
  * Date: 07.08.13
  * Time: 20:48
  */
 
@Service("applicationsService")
@Transactional
 public interface ApplicationConfigurationService {
     public String getParameter(String name);
 }
