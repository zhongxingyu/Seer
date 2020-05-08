 package com.iknition.firstzk.service;
 
 import java.util.List;
 
 import org.springframework.dao.DataAccessException;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.iknition.firstzk.beans.Company;
 import com.iknition.firstzk.daos.CompanyDAO;
 
 public class CompanyManagerImpl implements CompanyManager {
     private CompanyDAO companyDAO;
  
     public CompanyDAO getCompanyDAO() {
         return companyDAO;
     }
  
     public void setCompanyDAO(CompanyDAO companyDAO) {
         this.companyDAO = companyDAO;
     }
  
     @Transactional
     public boolean save(Company company) {
         try {
             companyDAO.saveOrUpdate(company);
             return true;
         } catch (DataAccessException e) {
             return false;
         }
     }
      
     @Transactional
     public boolean delete(Company company) {
         try {
             companyDAO.delete(company);
             return true;
         } catch (DataAccessException e) {
             return false;
         }
     }
      
     @Transactional(readOnly = true)
     public Company getCompany(Integer id) {
         try {
             return companyDAO.findFetch(Company.class, id);
         } catch (DataAccessException e) {
             e.printStackTrace();
             return null;
         }
     }
      
     @Transactional(readOnly = true)
     public List<Company> getCompanyList() {
         try {
            return companyDAO.findAll(Company.class);
         } catch (DataAccessException e) {
             e.printStackTrace();
             return null;
         }
     }
 }
