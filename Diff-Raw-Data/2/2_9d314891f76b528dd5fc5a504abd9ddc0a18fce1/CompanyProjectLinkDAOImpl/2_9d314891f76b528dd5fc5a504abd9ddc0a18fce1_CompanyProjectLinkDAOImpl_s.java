 package org.zju.electric_factory.dao.impl;
 
 import java.util.List;
 
 import org.springframework.stereotype.Repository;
 import org.zju.electric_factory.dao.CompanyProjectLinkDAO;
 import org.zju.electric_factory.entity.CompanyProjectLink;
 
 @Repository
 public class CompanyProjectLinkDAOImpl extends HibernateDAO<CompanyProjectLink, Long> implements CompanyProjectLinkDAO{
 
 	@Override
 	public List<CompanyProjectLink> getByCompanyId(Long companyId) {
		return super.getAll("companyId", true);
 	}
 
 	@Override
 	public List<CompanyProjectLink> getByProjectId(Long projectId) {
 		return super.getAll("projectId", true);
 	}
 
 	@Override
 	public CompanyProjectLink getById(Long id) {
 		return super.get(id);
 	}
 
 	@Override
 	public void add(CompanyProjectLink companyProjectLink) {
 		super.save(companyProjectLink);
 	}
 
 	@Override
 	public void deleteById(Long id) {
 		super.delete(id);
 	}
 
 	@Override
 	public void update(CompanyProjectLink companyProjectLink) {
 		super.save(companyProjectLink);
 		
 	}
 
 }
